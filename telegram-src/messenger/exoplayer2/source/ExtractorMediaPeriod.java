package org.telegram.messenger.exoplayer2.source;

import android.net.Uri;
import android.os.Handler;
import java.io.IOException;
import java.util.Arrays;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.FormatHolder;
import org.telegram.messenger.exoplayer2.SeekParameters;
import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;
import org.telegram.messenger.exoplayer2.extractor.DefaultExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorOutput;
import org.telegram.messenger.exoplayer2.extractor.PositionHolder;
import org.telegram.messenger.exoplayer2.extractor.SeekMap;
import org.telegram.messenger.exoplayer2.extractor.SeekMap.SeekPoints;
import org.telegram.messenger.exoplayer2.extractor.SeekPoint;
import org.telegram.messenger.exoplayer2.extractor.TrackOutput;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.upstream.Allocator;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.Loader;
import org.telegram.messenger.exoplayer2.upstream.Loader.Callback;
import org.telegram.messenger.exoplayer2.upstream.Loader.Loadable;
import org.telegram.messenger.exoplayer2.upstream.Loader.ReleaseCallback;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.ConditionVariable;
import org.telegram.messenger.exoplayer2.util.MimeTypes;
import org.telegram.messenger.exoplayer2.util.Util;

final class ExtractorMediaPeriod
  implements ExtractorOutput, MediaPeriod, SampleQueue.UpstreamFormatChangedListener, Loader.Callback<ExtractingLoadable>, Loader.ReleaseCallback
{
  private static final long DEFAULT_LAST_SAMPLE_DURATION_US = 10000L;
  private int actualMinLoadableRetryCount;
  private final Allocator allocator;
  private MediaPeriod.Callback callback;
  private final long continueLoadingCheckIntervalBytes;
  private final String customCacheKey;
  private final DataSource dataSource;
  private long durationUs;
  private int enabledTrackCount;
  private final MediaSourceEventListener.EventDispatcher eventDispatcher;
  private int extractedSamplesCountAtStartOfLoad;
  private final ExtractorHolder extractorHolder;
  private final Handler handler;
  private boolean haveAudioVideoTracks;
  private long lastSeekPositionUs;
  private long length;
  private final Listener listener;
  private final ConditionVariable loadCondition;
  private final Loader loader;
  private boolean loadingFinished;
  private final Runnable maybeFinishPrepareRunnable;
  private final int minLoadableRetryCount;
  private boolean notifyDiscontinuity;
  private final Runnable onContinueLoadingRequestedRunnable;
  private boolean pendingDeferredRetry;
  private long pendingResetPositionUs;
  private boolean prepared;
  private boolean released;
  private int[] sampleQueueTrackIds;
  private SampleQueue[] sampleQueues;
  private boolean sampleQueuesBuilt;
  private SeekMap seekMap;
  private boolean seenFirstTrackSelection;
  private boolean[] trackEnabledStates;
  private boolean[] trackFormatNotificationSent;
  private boolean[] trackIsAudioVideoFlags;
  private TrackGroupArray tracks;
  private final Uri uri;
  
  public ExtractorMediaPeriod(Uri paramUri, DataSource paramDataSource, Extractor[] paramArrayOfExtractor, int paramInt1, MediaSourceEventListener.EventDispatcher paramEventDispatcher, Listener paramListener, Allocator paramAllocator, String paramString, int paramInt2)
  {
    this.uri = paramUri;
    this.dataSource = paramDataSource;
    this.minLoadableRetryCount = paramInt1;
    this.eventDispatcher = paramEventDispatcher;
    this.listener = paramListener;
    this.allocator = paramAllocator;
    this.customCacheKey = paramString;
    this.continueLoadingCheckIntervalBytes = paramInt2;
    this.loader = new Loader("Loader:ExtractorMediaPeriod");
    this.extractorHolder = new ExtractorHolder(paramArrayOfExtractor, this);
    this.loadCondition = new ConditionVariable();
    this.maybeFinishPrepareRunnable = new Runnable()
    {
      public void run()
      {
        ExtractorMediaPeriod.this.maybeFinishPrepare();
      }
    };
    this.onContinueLoadingRequestedRunnable = new Runnable()
    {
      public void run()
      {
        if (!ExtractorMediaPeriod.this.released) {
          ExtractorMediaPeriod.this.callback.onContinueLoadingRequested(ExtractorMediaPeriod.this);
        }
      }
    };
    this.handler = new Handler();
    this.sampleQueueTrackIds = new int[0];
    this.sampleQueues = new SampleQueue[0];
    this.pendingResetPositionUs = -9223372036854775807L;
    this.length = -1L;
    this.durationUs = -9223372036854775807L;
    paramInt2 = paramInt1;
    if (paramInt1 == -1) {
      paramInt2 = 3;
    }
    this.actualMinLoadableRetryCount = paramInt2;
  }
  
  private boolean configureRetry(ExtractingLoadable paramExtractingLoadable, int paramInt)
  {
    int i = 0;
    if ((this.length != -1L) || ((this.seekMap != null) && (this.seekMap.getDurationUs() != -9223372036854775807L)))
    {
      this.extractedSamplesCountAtStartOfLoad = paramInt;
      return true;
    }
    if ((this.prepared) && (!suppressRead()))
    {
      this.pendingDeferredRetry = true;
      return false;
    }
    this.notifyDiscontinuity = this.prepared;
    this.lastSeekPositionUs = 0L;
    this.extractedSamplesCountAtStartOfLoad = 0;
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int j = arrayOfSampleQueue.length;
    paramInt = i;
    while (paramInt < j)
    {
      arrayOfSampleQueue[paramInt].reset();
      paramInt += 1;
    }
    paramExtractingLoadable.setLoadPosition(0L, 0L);
    return true;
  }
  
  private void copyLengthFromLoader(ExtractingLoadable paramExtractingLoadable)
  {
    if (this.length == -1L) {
      this.length = paramExtractingLoadable.length;
    }
  }
  
  private int getExtractedSamplesCount()
  {
    int j = 0;
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int k = arrayOfSampleQueue.length;
    int i = 0;
    while (i < k)
    {
      j += arrayOfSampleQueue[i].getWriteIndex();
      i += 1;
    }
    return j;
  }
  
  private long getLargestQueuedTimestampUs()
  {
    long l = Long.MIN_VALUE;
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int j = arrayOfSampleQueue.length;
    int i = 0;
    while (i < j)
    {
      l = Math.max(l, arrayOfSampleQueue[i].getLargestQueuedTimestampUs());
      i += 1;
    }
    return l;
  }
  
  private static boolean isLoadableExceptionFatal(IOException paramIOException)
  {
    return paramIOException instanceof UnrecognizedInputFormatException;
  }
  
  private boolean isPendingReset()
  {
    return this.pendingResetPositionUs != -9223372036854775807L;
  }
  
  private void maybeFinishPrepare()
  {
    if ((this.released) || (this.prepared) || (this.seekMap == null) || (!this.sampleQueuesBuilt)) {
      return;
    }
    Object localObject1 = this.sampleQueues;
    int j = localObject1.length;
    int i = 0;
    for (;;)
    {
      if (i >= j) {
        break label63;
      }
      if (localObject1[i].getUpstreamFormat() == null) {
        break;
      }
      i += 1;
    }
    label63:
    this.loadCondition.close();
    j = this.sampleQueues.length;
    localObject1 = new TrackGroup[j];
    this.trackIsAudioVideoFlags = new boolean[j];
    this.trackEnabledStates = new boolean[j];
    this.trackFormatNotificationSent = new boolean[j];
    this.durationUs = this.seekMap.getDurationUs();
    i = 0;
    if (i < j)
    {
      Object localObject2 = this.sampleQueues[i].getUpstreamFormat();
      localObject1[i] = new TrackGroup(new Format[] { localObject2 });
      localObject2 = ((Format)localObject2).sampleMimeType;
      if ((MimeTypes.isVideo((String)localObject2)) || (MimeTypes.isAudio((String)localObject2))) {}
      for (int k = 1;; k = 0)
      {
        this.trackIsAudioVideoFlags[i] = k;
        this.haveAudioVideoTracks |= k;
        i += 1;
        break;
      }
    }
    this.tracks = new TrackGroupArray((TrackGroup[])localObject1);
    if ((this.minLoadableRetryCount == -1) && (this.length == -1L) && (this.seekMap.getDurationUs() == -9223372036854775807L)) {
      this.actualMinLoadableRetryCount = 6;
    }
    this.prepared = true;
    this.listener.onSourceInfoRefreshed(this.durationUs, this.seekMap.isSeekable());
    this.callback.onPrepared(this);
  }
  
  private void maybeNotifyTrackFormat(int paramInt)
  {
    if (this.trackFormatNotificationSent[paramInt] == 0)
    {
      Format localFormat = this.tracks.get(paramInt).getFormat(0);
      this.eventDispatcher.downstreamFormatChanged(MimeTypes.getTrackType(localFormat.sampleMimeType), localFormat, 0, null, this.lastSeekPositionUs);
      this.trackFormatNotificationSent[paramInt] = true;
    }
  }
  
  private void maybeStartDeferredRetry(int paramInt)
  {
    int i = 0;
    if ((!this.pendingDeferredRetry) || (this.trackIsAudioVideoFlags[paramInt] == 0) || (this.sampleQueues[paramInt].hasNextSample())) {
      return;
    }
    this.pendingResetPositionUs = 0L;
    this.pendingDeferredRetry = false;
    this.notifyDiscontinuity = true;
    this.lastSeekPositionUs = 0L;
    this.extractedSamplesCountAtStartOfLoad = 0;
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int j = arrayOfSampleQueue.length;
    paramInt = i;
    while (paramInt < j)
    {
      arrayOfSampleQueue[paramInt].reset();
      paramInt += 1;
    }
    this.callback.onContinueLoadingRequested(this);
  }
  
  private boolean seekInsideBufferUs(long paramLong)
  {
    int k = this.sampleQueues.length;
    int i = 0;
    while (i < k)
    {
      SampleQueue localSampleQueue = this.sampleQueues[i];
      localSampleQueue.rewind();
      if (localSampleQueue.advanceTo(paramLong, true, false) != -1) {}
      for (int j = 1; (j == 0) && ((this.trackIsAudioVideoFlags[i] != 0) || (!this.haveAudioVideoTracks)); j = 0) {
        return false;
      }
      i += 1;
    }
    return true;
  }
  
  private void startLoading()
  {
    ExtractingLoadable localExtractingLoadable = new ExtractingLoadable(this.uri, this.dataSource, this.extractorHolder, this.loadCondition);
    if (this.prepared)
    {
      Assertions.checkState(isPendingReset());
      if ((this.durationUs != -9223372036854775807L) && (this.pendingResetPositionUs >= this.durationUs))
      {
        this.loadingFinished = true;
        this.pendingResetPositionUs = -9223372036854775807L;
        return;
      }
      localExtractingLoadable.setLoadPosition(this.seekMap.getSeekPoints(this.pendingResetPositionUs).first.position, this.pendingResetPositionUs);
      this.pendingResetPositionUs = -9223372036854775807L;
    }
    this.extractedSamplesCountAtStartOfLoad = getExtractedSamplesCount();
    long l = this.loader.startLoading(localExtractingLoadable, this, this.actualMinLoadableRetryCount);
    this.eventDispatcher.loadStarted(localExtractingLoadable.dataSpec, 1, -1, null, 0, null, localExtractingLoadable.seekTimeUs, this.durationUs, l);
  }
  
  private boolean suppressRead()
  {
    return (this.notifyDiscontinuity) || (isPendingReset());
  }
  
  public boolean continueLoading(long paramLong)
  {
    boolean bool;
    if ((this.loadingFinished) || (this.pendingDeferredRetry) || ((this.prepared) && (this.enabledTrackCount == 0))) {
      bool = false;
    }
    do
    {
      return bool;
      bool = this.loadCondition.open();
    } while (this.loader.isLoading());
    startLoading();
    return true;
  }
  
  public void discardBuffer(long paramLong, boolean paramBoolean)
  {
    int j = this.sampleQueues.length;
    int i = 0;
    while (i < j)
    {
      this.sampleQueues[i].discardTo(paramLong, paramBoolean, this.trackEnabledStates[i]);
      i += 1;
    }
  }
  
  public void endTracks()
  {
    this.sampleQueuesBuilt = true;
    this.handler.post(this.maybeFinishPrepareRunnable);
  }
  
  public long getAdjustedSeekPositionUs(long paramLong, SeekParameters paramSeekParameters)
  {
    if (!this.seekMap.isSeekable()) {
      return 0L;
    }
    SeekMap.SeekPoints localSeekPoints = this.seekMap.getSeekPoints(paramLong);
    return Util.resolveSeekPositionUs(paramLong, paramSeekParameters, localSeekPoints.first.timeUs, localSeekPoints.second.timeUs);
  }
  
  public long getBufferedPositionUs()
  {
    long l1;
    if (this.loadingFinished) {
      l1 = Long.MIN_VALUE;
    }
    long l2;
    do
    {
      return l1;
      if (isPendingReset()) {
        return this.pendingResetPositionUs;
      }
      if (this.haveAudioVideoTracks)
      {
        l1 = Long.MAX_VALUE;
        int j = this.sampleQueues.length;
        int i = 0;
        for (;;)
        {
          l2 = l1;
          if (i >= j) {
            break;
          }
          l2 = l1;
          if (this.trackIsAudioVideoFlags[i] != 0) {
            l2 = Math.min(l1, this.sampleQueues[i].getLargestQueuedTimestampUs());
          }
          i += 1;
          l1 = l2;
        }
      }
      l2 = getLargestQueuedTimestampUs();
      l1 = l2;
    } while (l2 != Long.MIN_VALUE);
    return this.lastSeekPositionUs;
  }
  
  public long getNextLoadPositionUs()
  {
    if (this.enabledTrackCount == 0) {
      return Long.MIN_VALUE;
    }
    return getBufferedPositionUs();
  }
  
  public TrackGroupArray getTrackGroups()
  {
    return this.tracks;
  }
  
  boolean isReady(int paramInt)
  {
    return (!suppressRead()) && ((this.loadingFinished) || (this.sampleQueues[paramInt].hasNextSample()));
  }
  
  void maybeThrowError()
    throws IOException
  {
    this.loader.maybeThrowError(this.actualMinLoadableRetryCount);
  }
  
  public void maybeThrowPrepareError()
    throws IOException
  {
    maybeThrowError();
  }
  
  public void onLoadCanceled(ExtractingLoadable paramExtractingLoadable, long paramLong1, long paramLong2, boolean paramBoolean)
  {
    this.eventDispatcher.loadCanceled(paramExtractingLoadable.dataSpec, 1, -1, null, 0, null, paramExtractingLoadable.seekTimeUs, this.durationUs, paramLong1, paramLong2, paramExtractingLoadable.bytesLoaded);
    if (!paramBoolean)
    {
      copyLengthFromLoader(paramExtractingLoadable);
      paramExtractingLoadable = this.sampleQueues;
      int j = paramExtractingLoadable.length;
      int i = 0;
      while (i < j)
      {
        paramExtractingLoadable[i].reset();
        i += 1;
      }
      if (this.enabledTrackCount > 0) {
        this.callback.onContinueLoadingRequested(this);
      }
    }
  }
  
  public void onLoadCompleted(ExtractingLoadable paramExtractingLoadable, long paramLong1, long paramLong2)
  {
    if (this.durationUs == -9223372036854775807L)
    {
      l = getLargestQueuedTimestampUs();
      if (l != Long.MIN_VALUE) {
        break label109;
      }
    }
    label109:
    for (long l = 0L;; l = 10000L + l)
    {
      this.durationUs = l;
      this.listener.onSourceInfoRefreshed(this.durationUs, this.seekMap.isSeekable());
      this.eventDispatcher.loadCompleted(paramExtractingLoadable.dataSpec, 1, -1, null, 0, null, paramExtractingLoadable.seekTimeUs, this.durationUs, paramLong1, paramLong2, paramExtractingLoadable.bytesLoaded);
      copyLengthFromLoader(paramExtractingLoadable);
      this.loadingFinished = true;
      this.callback.onContinueLoadingRequested(this);
      return;
    }
  }
  
  public int onLoadError(ExtractingLoadable paramExtractingLoadable, long paramLong1, long paramLong2, IOException paramIOException)
  {
    boolean bool = isLoadableExceptionFatal(paramIOException);
    this.eventDispatcher.loadError(paramExtractingLoadable.dataSpec, 1, -1, null, 0, null, paramExtractingLoadable.seekTimeUs, this.durationUs, paramLong1, paramLong2, paramExtractingLoadable.bytesLoaded, paramIOException, bool);
    copyLengthFromLoader(paramExtractingLoadable);
    if (bool) {
      return 3;
    }
    int j = getExtractedSamplesCount();
    int i;
    if (j > this.extractedSamplesCountAtStartOfLoad) {
      i = 1;
    }
    while (configureRetry(paramExtractingLoadable, j)) {
      if (i != 0)
      {
        return 1;
        i = 0;
      }
      else
      {
        return 0;
      }
    }
    return 2;
  }
  
  public void onLoaderReleased()
  {
    this.extractorHolder.release();
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int j = arrayOfSampleQueue.length;
    int i = 0;
    while (i < j)
    {
      arrayOfSampleQueue[i].reset();
      i += 1;
    }
  }
  
  public void onUpstreamFormatChanged(Format paramFormat)
  {
    this.handler.post(this.maybeFinishPrepareRunnable);
  }
  
  public void prepare(MediaPeriod.Callback paramCallback, long paramLong)
  {
    this.callback = paramCallback;
    this.loadCondition.open();
    startLoading();
  }
  
  int readData(int paramInt, FormatHolder paramFormatHolder, DecoderInputBuffer paramDecoderInputBuffer, boolean paramBoolean)
  {
    int i;
    if (suppressRead()) {
      i = -3;
    }
    int j;
    do
    {
      return i;
      j = this.sampleQueues[paramInt].read(paramFormatHolder, paramDecoderInputBuffer, paramBoolean, this.loadingFinished, this.lastSeekPositionUs);
      if (j == -4)
      {
        maybeNotifyTrackFormat(paramInt);
        return j;
      }
      i = j;
    } while (j != -3);
    maybeStartDeferredRetry(paramInt);
    return j;
  }
  
  public long readDiscontinuity()
  {
    if ((this.notifyDiscontinuity) && ((this.loadingFinished) || (getExtractedSamplesCount() > this.extractedSamplesCountAtStartOfLoad)))
    {
      this.notifyDiscontinuity = false;
      return this.lastSeekPositionUs;
    }
    return -9223372036854775807L;
  }
  
  public void reevaluateBuffer(long paramLong) {}
  
  public void release()
  {
    boolean bool = this.loader.release(this);
    if ((this.prepared) && (!bool))
    {
      SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
      int j = arrayOfSampleQueue.length;
      int i = 0;
      while (i < j)
      {
        arrayOfSampleQueue[i].discardToEnd();
        i += 1;
      }
    }
    this.handler.removeCallbacksAndMessages(null);
    this.released = true;
  }
  
  public void seekMap(SeekMap paramSeekMap)
  {
    this.seekMap = paramSeekMap;
    this.handler.post(this.maybeFinishPrepareRunnable);
  }
  
  public long seekToUs(long paramLong)
  {
    int i = 0;
    if (this.seekMap.isSeekable())
    {
      this.lastSeekPositionUs = paramLong;
      this.notifyDiscontinuity = false;
      if ((isPendingReset()) || (!seekInsideBufferUs(paramLong))) {
        break label46;
      }
    }
    for (;;)
    {
      return paramLong;
      paramLong = 0L;
      break;
      label46:
      this.pendingDeferredRetry = false;
      this.pendingResetPositionUs = paramLong;
      this.loadingFinished = false;
      if (this.loader.isLoading())
      {
        this.loader.cancelLoading();
        return paramLong;
      }
      SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
      int j = arrayOfSampleQueue.length;
      while (i < j)
      {
        arrayOfSampleQueue[i].reset();
        i += 1;
      }
    }
  }
  
  public long selectTracks(TrackSelection[] paramArrayOfTrackSelection, boolean[] paramArrayOfBoolean1, SampleStream[] paramArrayOfSampleStream, boolean[] paramArrayOfBoolean2, long paramLong)
  {
    Assertions.checkState(this.prepared);
    int j = this.enabledTrackCount;
    int i = 0;
    int k;
    while (i < paramArrayOfTrackSelection.length)
    {
      if ((paramArrayOfSampleStream[i] != null) && ((paramArrayOfTrackSelection[i] == null) || (paramArrayOfBoolean1[i] == 0)))
      {
        k = ((SampleStreamImpl)paramArrayOfSampleStream[i]).track;
        Assertions.checkState(this.trackEnabledStates[k]);
        this.enabledTrackCount -= 1;
        this.trackEnabledStates[k] = false;
        paramArrayOfSampleStream[i] = null;
      }
      i += 1;
    }
    label120:
    boolean bool;
    if (this.seenFirstTrackSelection) {
      if (j == 0)
      {
        i = 1;
        j = 0;
        k = i;
        if (j >= paramArrayOfTrackSelection.length) {
          break label366;
        }
        i = k;
        if (paramArrayOfSampleStream[j] == null)
        {
          i = k;
          if (paramArrayOfTrackSelection[j] != null)
          {
            paramArrayOfBoolean1 = paramArrayOfTrackSelection[j];
            if (paramArrayOfBoolean1.length() != 1) {
              break label342;
            }
            bool = true;
            label167:
            Assertions.checkState(bool);
            if (paramArrayOfBoolean1.getIndexInTrackGroup(0) != 0) {
              break label348;
            }
            bool = true;
            label185:
            Assertions.checkState(bool);
            int m = this.tracks.indexOf(paramArrayOfBoolean1.getTrackGroup());
            if (this.trackEnabledStates[m] != 0) {
              break label354;
            }
            bool = true;
            label218:
            Assertions.checkState(bool);
            this.enabledTrackCount += 1;
            this.trackEnabledStates[m] = true;
            paramArrayOfSampleStream[j] = new SampleStreamImpl(m);
            paramArrayOfBoolean2[j] = true;
            i = k;
            if (k == 0)
            {
              paramArrayOfBoolean1 = this.sampleQueues[m];
              paramArrayOfBoolean1.rewind();
              if ((paramArrayOfBoolean1.advanceTo(paramLong, true, true) != -1) || (paramArrayOfBoolean1.getReadIndex() == 0)) {
                break label360;
              }
            }
          }
        }
      }
    }
    label342:
    label348:
    label354:
    label360:
    for (i = 1;; i = 0)
    {
      j += 1;
      k = i;
      break label120;
      i = 0;
      break;
      if (paramLong != 0L)
      {
        i = 1;
        break;
      }
      i = 0;
      break;
      bool = false;
      break label167;
      bool = false;
      break label185;
      bool = false;
      break label218;
    }
    label366:
    long l;
    if (this.enabledTrackCount == 0)
    {
      this.pendingDeferredRetry = false;
      this.notifyDiscontinuity = false;
      if (this.loader.isLoading())
      {
        paramArrayOfTrackSelection = this.sampleQueues;
        j = paramArrayOfTrackSelection.length;
        i = 0;
        while (i < j)
        {
          paramArrayOfTrackSelection[i].discardToEnd();
          i += 1;
        }
        this.loader.cancelLoading();
        l = paramLong;
      }
    }
    do
    {
      this.seenFirstTrackSelection = true;
      return l;
      paramArrayOfTrackSelection = this.sampleQueues;
      j = paramArrayOfTrackSelection.length;
      i = 0;
      for (;;)
      {
        l = paramLong;
        if (i >= j) {
          break;
        }
        paramArrayOfTrackSelection[i].reset();
        i += 1;
      }
      l = paramLong;
    } while (k == 0);
    paramLong = seekToUs(paramLong);
    i = 0;
    for (;;)
    {
      l = paramLong;
      if (i >= paramArrayOfSampleStream.length) {
        break;
      }
      if (paramArrayOfSampleStream[i] != null) {
        paramArrayOfBoolean2[i] = true;
      }
      i += 1;
    }
  }
  
  int skipData(int paramInt, long paramLong)
  {
    if (suppressRead()) {
      return 0;
    }
    SampleQueue localSampleQueue = this.sampleQueues[paramInt];
    int i;
    if ((this.loadingFinished) && (paramLong > localSampleQueue.getLargestQueuedTimestampUs())) {
      i = localSampleQueue.advanceToEnd();
    }
    while (i > 0)
    {
      maybeNotifyTrackFormat(paramInt);
      return i;
      int j = localSampleQueue.advanceTo(paramLong, true, true);
      i = j;
      if (j == -1) {
        i = 0;
      }
    }
    maybeStartDeferredRetry(paramInt);
    return i;
  }
  
  public TrackOutput track(int paramInt1, int paramInt2)
  {
    int i = this.sampleQueues.length;
    paramInt2 = 0;
    while (paramInt2 < i)
    {
      if (this.sampleQueueTrackIds[paramInt2] == paramInt1) {
        return this.sampleQueues[paramInt2];
      }
      paramInt2 += 1;
    }
    SampleQueue localSampleQueue = new SampleQueue(this.allocator);
    localSampleQueue.setUpstreamFormatChangeListener(this);
    this.sampleQueueTrackIds = Arrays.copyOf(this.sampleQueueTrackIds, i + 1);
    this.sampleQueueTrackIds[i] = paramInt1;
    this.sampleQueues = ((SampleQueue[])Arrays.copyOf(this.sampleQueues, i + 1));
    this.sampleQueues[i] = localSampleQueue;
    return localSampleQueue;
  }
  
  final class ExtractingLoadable
    implements Loader.Loadable
  {
    private long bytesLoaded;
    private final DataSource dataSource;
    private DataSpec dataSpec;
    private final ExtractorMediaPeriod.ExtractorHolder extractorHolder;
    private long length;
    private volatile boolean loadCanceled;
    private final ConditionVariable loadCondition;
    private boolean pendingExtractorSeek;
    private final PositionHolder positionHolder;
    private long seekTimeUs;
    private final Uri uri;
    
    public ExtractingLoadable(Uri paramUri, DataSource paramDataSource, ExtractorMediaPeriod.ExtractorHolder paramExtractorHolder, ConditionVariable paramConditionVariable)
    {
      this.uri = ((Uri)Assertions.checkNotNull(paramUri));
      this.dataSource = ((DataSource)Assertions.checkNotNull(paramDataSource));
      this.extractorHolder = ((ExtractorMediaPeriod.ExtractorHolder)Assertions.checkNotNull(paramExtractorHolder));
      this.loadCondition = paramConditionVariable;
      this.positionHolder = new PositionHolder();
      this.pendingExtractorSeek = true;
      this.length = -1L;
    }
    
    public void cancelLoad()
    {
      this.loadCanceled = true;
    }
    
    public boolean isLoadCanceled()
    {
      return this.loadCanceled;
    }
    
    public void load()
      throws IOException, InterruptedException
    {
      int i = 0;
      if ((i == 0) && (!this.loadCanceled)) {
        for (;;)
        {
          try
          {
            long l2 = this.positionHolder.position;
            this.dataSpec = new DataSpec(this.uri, l2, -1L, ExtractorMediaPeriod.this.customCacheKey);
            this.length = this.dataSource.open(this.dataSpec);
            if (this.length != -1L) {
              this.length += l2;
            }
            localDefaultExtractorInput = new DefaultExtractorInput(this.dataSource, l2, this.length);
            int k = i;
            try
            {
              Extractor localExtractor = this.extractorHolder.selectExtractor(localDefaultExtractorInput, this.dataSource.getUri());
              long l1 = l2;
              j = i;
              k = i;
              if (this.pendingExtractorSeek)
              {
                k = i;
                localExtractor.seek(l2, this.seekTimeUs);
                k = i;
                this.pendingExtractorSeek = false;
                j = i;
                l1 = l2;
              }
              if (j != 0) {
                continue;
              }
              k = j;
              if (this.loadCanceled) {
                continue;
              }
              k = j;
              this.loadCondition.block();
              k = j;
              i = localExtractor.read(localDefaultExtractorInput, this.positionHolder);
              j = i;
              k = i;
              if (localDefaultExtractorInput.getPosition() <= ExtractorMediaPeriod.this.continueLoadingCheckIntervalBytes + l1) {
                continue;
              }
              k = i;
              l1 = localDefaultExtractorInput.getPosition();
              k = i;
              this.loadCondition.close();
              k = i;
              ExtractorMediaPeriod.this.handler.post(ExtractorMediaPeriod.this.onContinueLoadingRequestedRunnable);
              j = i;
              continue;
              if (i != 1) {
                continue;
              }
            }
            finally
            {
              i = k;
            }
          }
          finally
          {
            int j;
            DefaultExtractorInput localDefaultExtractorInput = null;
            continue;
          }
          Util.closeQuietly(this.dataSource);
          throw ((Throwable)localObject1);
          if (j == 1)
          {
            i = 0;
            Util.closeQuietly(this.dataSource);
            break;
          }
          i = j;
          if (localDefaultExtractorInput != null)
          {
            this.positionHolder.position = localDefaultExtractorInput.getPosition();
            this.bytesLoaded = (this.positionHolder.position - this.dataSpec.absoluteStreamPosition);
            i = j;
            continue;
            if (localDefaultExtractorInput != null)
            {
              this.positionHolder.position = localDefaultExtractorInput.getPosition();
              this.bytesLoaded = (this.positionHolder.position - this.dataSpec.absoluteStreamPosition);
            }
          }
        }
      }
    }
    
    public void setLoadPosition(long paramLong1, long paramLong2)
    {
      this.positionHolder.position = paramLong1;
      this.seekTimeUs = paramLong2;
      this.pendingExtractorSeek = true;
    }
  }
  
  private static final class ExtractorHolder
  {
    private Extractor extractor;
    private final ExtractorOutput extractorOutput;
    private final Extractor[] extractors;
    
    public ExtractorHolder(Extractor[] paramArrayOfExtractor, ExtractorOutput paramExtractorOutput)
    {
      this.extractors = paramArrayOfExtractor;
      this.extractorOutput = paramExtractorOutput;
    }
    
    public void release()
    {
      if (this.extractor != null)
      {
        this.extractor.release();
        this.extractor = null;
      }
    }
    
    /* Error */
    public Extractor selectExtractor(ExtractorInput paramExtractorInput, Uri paramUri)
      throws IOException, InterruptedException
    {
      // Byte code:
      //   0: aload_0
      //   1: getfield 26	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractor	Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
      //   4: ifnull +8 -> 12
      //   7: aload_0
      //   8: getfield 26	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractor	Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
      //   11: areturn
      //   12: aload_0
      //   13: getfield 20	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractors	[Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
      //   16: astore 5
      //   18: aload 5
      //   20: arraylength
      //   21: istore 4
      //   23: iconst_0
      //   24: istore_3
      //   25: iload_3
      //   26: iload 4
      //   28: if_icmpge +32 -> 60
      //   31: aload 5
      //   33: iload_3
      //   34: aaload
      //   35: astore 6
      //   37: aload 6
      //   39: aload_1
      //   40: invokeinterface 42 2 0
      //   45: ifeq +61 -> 106
      //   48: aload_0
      //   49: aload 6
      //   51: putfield 26	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractor	Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
      //   54: aload_1
      //   55: invokeinterface 47 1 0
      //   60: aload_0
      //   61: getfield 26	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractor	Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
      //   64: ifnonnull +75 -> 139
      //   67: new 49	org/telegram/messenger/exoplayer2/source/UnrecognizedInputFormatException
      //   70: dup
      //   71: new 51	java/lang/StringBuilder
      //   74: dup
      //   75: invokespecial 52	java/lang/StringBuilder:<init>	()V
      //   78: ldc 54
      //   80: invokevirtual 58	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
      //   83: aload_0
      //   84: getfield 20	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractors	[Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
      //   87: invokestatic 64	org/telegram/messenger/exoplayer2/util/Util:getCommaDelimitedSimpleClassNames	([Ljava/lang/Object;)Ljava/lang/String;
      //   90: invokevirtual 58	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
      //   93: ldc 66
      //   95: invokevirtual 58	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
      //   98: invokevirtual 70	java/lang/StringBuilder:toString	()Ljava/lang/String;
      //   101: aload_2
      //   102: invokespecial 73	org/telegram/messenger/exoplayer2/source/UnrecognizedInputFormatException:<init>	(Ljava/lang/String;Landroid/net/Uri;)V
      //   105: athrow
      //   106: aload_1
      //   107: invokeinterface 47 1 0
      //   112: iload_3
      //   113: iconst_1
      //   114: iadd
      //   115: istore_3
      //   116: goto -91 -> 25
      //   119: astore 6
      //   121: aload_1
      //   122: invokeinterface 47 1 0
      //   127: goto -15 -> 112
      //   130: astore_2
      //   131: aload_1
      //   132: invokeinterface 47 1 0
      //   137: aload_2
      //   138: athrow
      //   139: aload_0
      //   140: getfield 26	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractor	Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
      //   143: aload_0
      //   144: getfield 22	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractorOutput	Lorg/telegram/messenger/exoplayer2/extractor/ExtractorOutput;
      //   147: invokeinterface 77 2 0
      //   152: aload_0
      //   153: getfield 26	org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod$ExtractorHolder:extractor	Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
      //   156: areturn
      // Local variable table:
      //   start	length	slot	name	signature
      //   0	157	0	this	ExtractorHolder
      //   0	157	1	paramExtractorInput	ExtractorInput
      //   0	157	2	paramUri	Uri
      //   24	92	3	i	int
      //   21	8	4	j	int
      //   16	16	5	arrayOfExtractor	Extractor[]
      //   35	15	6	localExtractor	Extractor
      //   119	1	6	localEOFException	java.io.EOFException
      // Exception table:
      //   from	to	target	type
      //   37	54	119	java/io/EOFException
      //   37	54	130	finally
    }
  }
  
  static abstract interface Listener
  {
    public abstract void onSourceInfoRefreshed(long paramLong, boolean paramBoolean);
  }
  
  private final class SampleStreamImpl
    implements SampleStream
  {
    private final int track;
    
    public SampleStreamImpl(int paramInt)
    {
      this.track = paramInt;
    }
    
    public boolean isReady()
    {
      return ExtractorMediaPeriod.this.isReady(this.track);
    }
    
    public void maybeThrowError()
      throws IOException
    {
      ExtractorMediaPeriod.this.maybeThrowError();
    }
    
    public int readData(FormatHolder paramFormatHolder, DecoderInputBuffer paramDecoderInputBuffer, boolean paramBoolean)
    {
      return ExtractorMediaPeriod.this.readData(this.track, paramFormatHolder, paramDecoderInputBuffer, paramBoolean);
    }
    
    public int skipData(long paramLong)
    {
      return ExtractorMediaPeriod.this.skipData(this.track, paramLong);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/ExtractorMediaPeriod.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */