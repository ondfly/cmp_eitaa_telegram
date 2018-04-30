package org.telegram.messenger.exoplayer2.source.hls;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.FormatHolder;
import org.telegram.messenger.exoplayer2.ParserException;
import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;
import org.telegram.messenger.exoplayer2.extractor.DummyTrackOutput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorOutput;
import org.telegram.messenger.exoplayer2.extractor.SeekMap;
import org.telegram.messenger.exoplayer2.extractor.TrackOutput;
import org.telegram.messenger.exoplayer2.source.MediaSourceEventListener.EventDispatcher;
import org.telegram.messenger.exoplayer2.source.SampleQueue;
import org.telegram.messenger.exoplayer2.source.SampleQueue.UpstreamFormatChangedListener;
import org.telegram.messenger.exoplayer2.source.SampleStream;
import org.telegram.messenger.exoplayer2.source.SequenceableLoader;
import org.telegram.messenger.exoplayer2.source.SequenceableLoader.Callback;
import org.telegram.messenger.exoplayer2.source.TrackGroup;
import org.telegram.messenger.exoplayer2.source.TrackGroupArray;
import org.telegram.messenger.exoplayer2.source.chunk.Chunk;
import org.telegram.messenger.exoplayer2.source.hls.playlist.HlsMasterPlaylist.HlsUrl;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.upstream.Allocator;
import org.telegram.messenger.exoplayer2.upstream.Loader;
import org.telegram.messenger.exoplayer2.upstream.Loader.Callback;
import org.telegram.messenger.exoplayer2.upstream.Loader.Loadable;
import org.telegram.messenger.exoplayer2.upstream.Loader.ReleaseCallback;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.MimeTypes;
import org.telegram.messenger.exoplayer2.util.Util;

final class HlsSampleStreamWrapper
  implements ExtractorOutput, SampleQueue.UpstreamFormatChangedListener, SequenceableLoader, Loader.Callback<Chunk>, Loader.ReleaseCallback
{
  private static final int PRIMARY_TYPE_AUDIO = 2;
  private static final int PRIMARY_TYPE_NONE = 0;
  private static final int PRIMARY_TYPE_TEXT = 1;
  private static final int PRIMARY_TYPE_VIDEO = 3;
  private static final String TAG = "HlsSampleStreamWrapper";
  private final Allocator allocator;
  private int audioSampleQueueIndex;
  private boolean audioSampleQueueMappingDone;
  private final Callback callback;
  private final HlsChunkSource chunkSource;
  private Format downstreamTrackFormat;
  private int enabledTrackGroupCount;
  private final MediaSourceEventListener.EventDispatcher eventDispatcher;
  private final Handler handler;
  private boolean haveAudioVideoSampleQueues;
  private long lastSeekPositionUs;
  private final Loader loader;
  private boolean loadingFinished;
  private final Runnable maybeFinishPrepareRunnable;
  private final ArrayList<HlsMediaChunk> mediaChunks;
  private final int minLoadableRetryCount;
  private final Format muxedAudioFormat;
  private final HlsChunkSource.HlsChunkHolder nextChunkHolder;
  private final Runnable onTracksEndedRunnable;
  private long pendingResetPositionUs;
  private boolean pendingResetUpstreamFormats;
  private boolean prepared;
  private int primaryTrackGroupIndex;
  private boolean released;
  private long sampleOffsetUs;
  private boolean[] sampleQueueIsAudioVideoFlags;
  private int[] sampleQueueTrackIds;
  private SampleQueue[] sampleQueues;
  private boolean sampleQueuesBuilt;
  private boolean[] sampleQueuesEnabledStates;
  private boolean seenFirstTrackSelection;
  private int[] trackGroupToSampleQueueIndex;
  private TrackGroupArray trackGroups;
  private final int trackType;
  private boolean tracksEnded;
  private int videoSampleQueueIndex;
  private boolean videoSampleQueueMappingDone;
  
  public HlsSampleStreamWrapper(int paramInt1, Callback paramCallback, HlsChunkSource paramHlsChunkSource, Allocator paramAllocator, long paramLong, Format paramFormat, int paramInt2, MediaSourceEventListener.EventDispatcher paramEventDispatcher)
  {
    this.trackType = paramInt1;
    this.callback = paramCallback;
    this.chunkSource = paramHlsChunkSource;
    this.allocator = paramAllocator;
    this.muxedAudioFormat = paramFormat;
    this.minLoadableRetryCount = paramInt2;
    this.eventDispatcher = paramEventDispatcher;
    this.loader = new Loader("Loader:HlsSampleStreamWrapper");
    this.nextChunkHolder = new HlsChunkSource.HlsChunkHolder();
    this.sampleQueueTrackIds = new int[0];
    this.audioSampleQueueIndex = -1;
    this.videoSampleQueueIndex = -1;
    this.sampleQueues = new SampleQueue[0];
    this.sampleQueueIsAudioVideoFlags = new boolean[0];
    this.sampleQueuesEnabledStates = new boolean[0];
    this.mediaChunks = new ArrayList();
    this.maybeFinishPrepareRunnable = new Runnable()
    {
      public void run()
      {
        HlsSampleStreamWrapper.this.maybeFinishPrepare();
      }
    };
    this.onTracksEndedRunnable = new Runnable()
    {
      public void run()
      {
        HlsSampleStreamWrapper.this.onTracksEnded();
      }
    };
    this.handler = new Handler();
    this.lastSeekPositionUs = paramLong;
    this.pendingResetPositionUs = paramLong;
  }
  
  private void buildTracks()
  {
    int m = 0;
    int n = -1;
    int i2 = this.sampleQueues.length;
    int j = 0;
    Object localObject;
    if (j < i2)
    {
      localObject = this.sampleQueues[j].getUpstreamFormat().sampleMimeType;
      label45:
      int i1;
      if (MimeTypes.isVideo((String)localObject))
      {
        i = 3;
        if (i <= m) {
          break label101;
        }
        k = j;
        i1 = i;
      }
      for (;;)
      {
        j += 1;
        n = k;
        m = i1;
        break;
        if (MimeTypes.isAudio((String)localObject))
        {
          i = 2;
          break label45;
        }
        if (MimeTypes.isText((String)localObject))
        {
          i = 1;
          break label45;
        }
        i = 0;
        break label45;
        label101:
        k = n;
        i1 = m;
        if (i == m)
        {
          k = n;
          i1 = m;
          if (n != -1)
          {
            k = -1;
            i1 = m;
          }
        }
      }
    }
    TrackGroup localTrackGroup = this.chunkSource.getTrackGroup();
    int k = localTrackGroup.length;
    this.primaryTrackGroupIndex = -1;
    this.trackGroupToSampleQueueIndex = new int[i2];
    int i = 0;
    while (i < i2)
    {
      this.trackGroupToSampleQueueIndex[i] = i;
      i += 1;
    }
    TrackGroup[] arrayOfTrackGroup = new TrackGroup[i2];
    i = 0;
    while (i < i2)
    {
      Format localFormat = this.sampleQueues[i].getUpstreamFormat();
      if (i == n)
      {
        localObject = new Format[k];
        j = 0;
        while (j < k)
        {
          localObject[j] = deriveFormat(localTrackGroup.getFormat(j), localFormat, true);
          j += 1;
        }
        arrayOfTrackGroup[i] = new TrackGroup((Format[])localObject);
        this.primaryTrackGroupIndex = i;
        i += 1;
      }
      else
      {
        if ((m == 3) && (MimeTypes.isAudio(localFormat.sampleMimeType))) {}
        for (localObject = this.muxedAudioFormat;; localObject = null)
        {
          arrayOfTrackGroup[i] = new TrackGroup(new Format[] { deriveFormat((Format)localObject, localFormat, false) });
          break;
        }
      }
    }
    this.trackGroups = new TrackGroupArray(arrayOfTrackGroup);
  }
  
  private static DummyTrackOutput createDummyTrackOutput(int paramInt1, int paramInt2)
  {
    Log.w("HlsSampleStreamWrapper", "Unmapped track with id " + paramInt1 + " of type " + paramInt2);
    return new DummyTrackOutput();
  }
  
  private static Format deriveFormat(Format paramFormat1, Format paramFormat2, boolean paramBoolean)
  {
    if (paramFormat1 == null) {
      return paramFormat2;
    }
    if (paramBoolean) {}
    for (int i = paramFormat1.bitrate;; i = -1)
    {
      int j = MimeTypes.getTrackType(paramFormat2.sampleMimeType);
      String str3 = Util.getCodecsOfType(paramFormat1.codecs, j);
      String str2 = MimeTypes.getMediaMimeType(str3);
      String str1 = str2;
      if (str2 == null) {
        str1 = paramFormat2.sampleMimeType;
      }
      return paramFormat2.copyWithContainerInfo(paramFormat1.id, str1, str3, i, paramFormat1.width, paramFormat1.height, paramFormat1.selectionFlags, paramFormat1.language);
    }
  }
  
  private boolean finishedReadingChunk(HlsMediaChunk paramHlsMediaChunk)
  {
    int j = paramHlsMediaChunk.uid;
    int k = this.sampleQueues.length;
    int i = 0;
    while (i < k)
    {
      if ((this.sampleQueuesEnabledStates[i] != 0) && (this.sampleQueues[i].peekSourceId() == j)) {
        return false;
      }
      i += 1;
    }
    return true;
  }
  
  private static boolean formatsMatch(Format paramFormat1, Format paramFormat2)
  {
    String str1 = paramFormat1.sampleMimeType;
    String str2 = paramFormat2.sampleMimeType;
    int i = MimeTypes.getTrackType(str1);
    if (i != 3) {
      if (i != MimeTypes.getTrackType(str2)) {}
    }
    do
    {
      return true;
      return false;
      if (!Util.areEqual(str1, str2)) {
        return false;
      }
    } while (((!"application/cea-608".equals(str1)) && (!"application/cea-708".equals(str1))) || (paramFormat1.accessibilityChannel == paramFormat2.accessibilityChannel));
    return false;
  }
  
  private HlsMediaChunk getLastMediaChunk()
  {
    return (HlsMediaChunk)this.mediaChunks.get(this.mediaChunks.size() - 1);
  }
  
  private static boolean isMediaChunk(Chunk paramChunk)
  {
    return paramChunk instanceof HlsMediaChunk;
  }
  
  private boolean isPendingReset()
  {
    return this.pendingResetPositionUs != -9223372036854775807L;
  }
  
  private void mapSampleQueuesToMatchTrackGroups()
  {
    int k = this.trackGroups.length;
    this.trackGroupToSampleQueueIndex = new int[k];
    Arrays.fill(this.trackGroupToSampleQueueIndex, -1);
    int i = 0;
    if (i < k)
    {
      int j = 0;
      for (;;)
      {
        if (j < this.sampleQueues.length)
        {
          if (formatsMatch(this.sampleQueues[j].getUpstreamFormat(), this.trackGroups.get(i).getFormat(0))) {
            this.trackGroupToSampleQueueIndex[i] = j;
          }
        }
        else
        {
          i += 1;
          break;
        }
        j += 1;
      }
    }
  }
  
  private void maybeFinishPrepare()
  {
    if ((this.released) || (this.trackGroupToSampleQueueIndex != null) || (!this.sampleQueuesBuilt)) {
      return;
    }
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int j = arrayOfSampleQueue.length;
    int i = 0;
    for (;;)
    {
      if (i >= j) {
        break label53;
      }
      if (arrayOfSampleQueue[i].getUpstreamFormat() == null) {
        break;
      }
      i += 1;
    }
    label53:
    if (this.trackGroups != null)
    {
      mapSampleQueuesToMatchTrackGroups();
      return;
    }
    buildTracks();
    this.prepared = true;
    this.callback.onPrepared();
  }
  
  private void onTracksEnded()
  {
    this.sampleQueuesBuilt = true;
    maybeFinishPrepare();
  }
  
  private void resetSampleQueues()
  {
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int j = arrayOfSampleQueue.length;
    int i = 0;
    while (i < j)
    {
      arrayOfSampleQueue[i].reset(this.pendingResetUpstreamFormats);
      i += 1;
    }
    this.pendingResetUpstreamFormats = false;
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
      for (int j = 1; (j == 0) && ((this.sampleQueueIsAudioVideoFlags[i] != 0) || (!this.haveAudioVideoSampleQueues)); j = 0) {
        return false;
      }
      i += 1;
    }
    return true;
  }
  
  public int bindSampleQueueToSampleStream(int paramInt)
  {
    if (!isMappingFinished()) {
      return -1;
    }
    paramInt = this.trackGroupToSampleQueueIndex[paramInt];
    if (paramInt == -1) {
      return -1;
    }
    if (this.sampleQueuesEnabledStates[paramInt] != 0) {
      return -1;
    }
    this.sampleQueuesEnabledStates[paramInt] = true;
    return paramInt;
  }
  
  public boolean continueLoading(long paramLong)
  {
    if ((this.loadingFinished) || (this.loader.isLoading())) {
      return false;
    }
    Object localObject1;
    if (isPendingReset()) {
      localObject1 = null;
    }
    Object localObject2;
    for (long l = this.pendingResetPositionUs;; l = ((HlsMediaChunk)localObject1).endTimeUs)
    {
      this.chunkSource.getNextChunk((HlsMediaChunk)localObject1, paramLong, l, this.nextChunkHolder);
      boolean bool = this.nextChunkHolder.endOfStream;
      localObject1 = this.nextChunkHolder.chunk;
      localObject2 = this.nextChunkHolder.playlist;
      this.nextChunkHolder.clear();
      if (!bool) {
        break;
      }
      this.pendingResetPositionUs = -9223372036854775807L;
      this.loadingFinished = true;
      return true;
      localObject1 = getLastMediaChunk();
    }
    if (localObject1 == null)
    {
      if (localObject2 != null) {
        this.callback.onPlaylistRefreshRequired((HlsMasterPlaylist.HlsUrl)localObject2);
      }
      return false;
    }
    if (isMediaChunk((Chunk)localObject1))
    {
      this.pendingResetPositionUs = -9223372036854775807L;
      localObject2 = (HlsMediaChunk)localObject1;
      ((HlsMediaChunk)localObject2).init(this);
      this.mediaChunks.add(localObject2);
    }
    paramLong = this.loader.startLoading((Loader.Loadable)localObject1, this, this.minLoadableRetryCount);
    this.eventDispatcher.loadStarted(((Chunk)localObject1).dataSpec, ((Chunk)localObject1).type, this.trackType, ((Chunk)localObject1).trackFormat, ((Chunk)localObject1).trackSelectionReason, ((Chunk)localObject1).trackSelectionData, ((Chunk)localObject1).startTimeUs, ((Chunk)localObject1).endTimeUs, paramLong);
    return true;
  }
  
  public void continuePreparing()
  {
    if (!this.prepared) {
      continueLoading(this.lastSeekPositionUs);
    }
  }
  
  public void discardBuffer(long paramLong, boolean paramBoolean)
  {
    if (!this.sampleQueuesBuilt) {}
    for (;;)
    {
      return;
      int j = this.sampleQueues.length;
      int i = 0;
      while (i < j)
      {
        this.sampleQueues[i].discardTo(paramLong, paramBoolean, this.sampleQueuesEnabledStates[i]);
        i += 1;
      }
    }
  }
  
  public void endTracks()
  {
    this.tracksEnded = true;
    this.handler.post(this.onTracksEndedRunnable);
  }
  
  public long getBufferedPositionUs()
  {
    if (this.loadingFinished)
    {
      l2 = Long.MIN_VALUE;
      return l2;
    }
    if (isPendingReset()) {
      return this.pendingResetPositionUs;
    }
    long l2 = this.lastSeekPositionUs;
    Object localObject = getLastMediaChunk();
    if (((HlsMediaChunk)localObject).isLoadCompleted()) {}
    for (;;)
    {
      long l1 = l2;
      if (localObject != null) {
        l1 = Math.max(l2, ((HlsMediaChunk)localObject).endTimeUs);
      }
      l2 = l1;
      if (!this.sampleQueuesBuilt) {
        break;
      }
      localObject = this.sampleQueues;
      int j = localObject.length;
      int i = 0;
      for (;;)
      {
        l2 = l1;
        if (i >= j) {
          break;
        }
        l1 = Math.max(l1, localObject[i].getLargestQueuedTimestampUs());
        i += 1;
      }
      if (this.mediaChunks.size() > 1) {
        localObject = (HlsMediaChunk)this.mediaChunks.get(this.mediaChunks.size() - 2);
      } else {
        localObject = null;
      }
    }
  }
  
  public long getNextLoadPositionUs()
  {
    if (isPendingReset()) {
      return this.pendingResetPositionUs;
    }
    if (this.loadingFinished) {
      return Long.MIN_VALUE;
    }
    return getLastMediaChunk().endTimeUs;
  }
  
  public TrackGroupArray getTrackGroups()
  {
    return this.trackGroups;
  }
  
  public void init(int paramInt, boolean paramBoolean1, boolean paramBoolean2)
  {
    int j = 0;
    if (!paramBoolean2)
    {
      this.audioSampleQueueMappingDone = false;
      this.videoSampleQueueMappingDone = false;
    }
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int k = arrayOfSampleQueue.length;
    int i = 0;
    while (i < k)
    {
      arrayOfSampleQueue[i].sourceId(paramInt);
      i += 1;
    }
    if (paramBoolean1)
    {
      arrayOfSampleQueue = this.sampleQueues;
      i = arrayOfSampleQueue.length;
      paramInt = j;
      while (paramInt < i)
      {
        arrayOfSampleQueue[paramInt].splice();
        paramInt += 1;
      }
    }
  }
  
  public boolean isMappingFinished()
  {
    return this.trackGroupToSampleQueueIndex != null;
  }
  
  public boolean isReady(int paramInt)
  {
    return (this.loadingFinished) || ((!isPendingReset()) && (this.sampleQueues[paramInt].hasNextSample()));
  }
  
  public void maybeThrowError()
    throws IOException
  {
    this.loader.maybeThrowError();
    this.chunkSource.maybeThrowError();
  }
  
  public void maybeThrowPrepareError()
    throws IOException
  {
    maybeThrowError();
  }
  
  public void onLoadCanceled(Chunk paramChunk, long paramLong1, long paramLong2, boolean paramBoolean)
  {
    this.eventDispatcher.loadCanceled(paramChunk.dataSpec, paramChunk.type, this.trackType, paramChunk.trackFormat, paramChunk.trackSelectionReason, paramChunk.trackSelectionData, paramChunk.startTimeUs, paramChunk.endTimeUs, paramLong1, paramLong2, paramChunk.bytesLoaded());
    if (!paramBoolean)
    {
      resetSampleQueues();
      if (this.enabledTrackGroupCount > 0) {
        this.callback.onContinueLoadingRequested(this);
      }
    }
  }
  
  public void onLoadCompleted(Chunk paramChunk, long paramLong1, long paramLong2)
  {
    this.chunkSource.onChunkLoadCompleted(paramChunk);
    this.eventDispatcher.loadCompleted(paramChunk.dataSpec, paramChunk.type, this.trackType, paramChunk.trackFormat, paramChunk.trackSelectionReason, paramChunk.trackSelectionData, paramChunk.startTimeUs, paramChunk.endTimeUs, paramLong1, paramLong2, paramChunk.bytesLoaded());
    if (!this.prepared)
    {
      continueLoading(this.lastSeekPositionUs);
      return;
    }
    this.callback.onContinueLoadingRequested(this);
  }
  
  public int onLoadError(Chunk paramChunk, long paramLong1, long paramLong2, IOException paramIOException)
  {
    long l = paramChunk.bytesLoaded();
    boolean bool3 = isMediaChunk(paramChunk);
    boolean bool1;
    if ((!bool3) || (l == 0L))
    {
      bool1 = true;
      boolean bool2 = false;
      if (this.chunkSource.onChunkLoadError(paramChunk, bool1, paramIOException))
      {
        if (bool3)
        {
          if ((HlsMediaChunk)this.mediaChunks.remove(this.mediaChunks.size() - 1) != paramChunk) {
            break label181;
          }
          bool1 = true;
          label76:
          Assertions.checkState(bool1);
          if (this.mediaChunks.isEmpty()) {
            this.pendingResetPositionUs = this.lastSeekPositionUs;
          }
        }
        bool2 = true;
      }
      this.eventDispatcher.loadError(paramChunk.dataSpec, paramChunk.type, this.trackType, paramChunk.trackFormat, paramChunk.trackSelectionReason, paramChunk.trackSelectionData, paramChunk.startTimeUs, paramChunk.endTimeUs, paramLong1, paramLong2, paramChunk.bytesLoaded(), paramIOException, bool2);
      if (!bool2) {
        break label200;
      }
      if (this.prepared) {
        break label187;
      }
      continueLoading(this.lastSeekPositionUs);
    }
    for (;;)
    {
      return 2;
      bool1 = false;
      break;
      label181:
      bool1 = false;
      break label76;
      label187:
      this.callback.onContinueLoadingRequested(this);
    }
    label200:
    if ((paramIOException instanceof ParserException)) {
      return 3;
    }
    return 0;
  }
  
  public void onLoaderReleased()
  {
    resetSampleQueues();
  }
  
  public void onPlaylistBlacklisted(HlsMasterPlaylist.HlsUrl paramHlsUrl, long paramLong)
  {
    this.chunkSource.onPlaylistBlacklisted(paramHlsUrl, paramLong);
  }
  
  public void onUpstreamFormatChanged(Format paramFormat)
  {
    this.handler.post(this.maybeFinishPrepareRunnable);
  }
  
  public void prepareWithMasterPlaylistInfo(TrackGroupArray paramTrackGroupArray, int paramInt)
  {
    this.prepared = true;
    this.trackGroups = paramTrackGroupArray;
    this.primaryTrackGroupIndex = paramInt;
    this.callback.onPrepared();
  }
  
  public int readData(int paramInt, FormatHolder paramFormatHolder, DecoderInputBuffer paramDecoderInputBuffer, boolean paramBoolean)
  {
    if (isPendingReset()) {
      return -3;
    }
    if (!this.mediaChunks.isEmpty())
    {
      int i = 0;
      while ((i < this.mediaChunks.size() - 1) && (finishedReadingChunk((HlsMediaChunk)this.mediaChunks.get(i)))) {
        i += 1;
      }
      if (i > 0) {
        Util.removeRange(this.mediaChunks, 0, i);
      }
      HlsMediaChunk localHlsMediaChunk = (HlsMediaChunk)this.mediaChunks.get(0);
      Format localFormat = localHlsMediaChunk.trackFormat;
      if (!localFormat.equals(this.downstreamTrackFormat)) {
        this.eventDispatcher.downstreamFormatChanged(this.trackType, localFormat, localHlsMediaChunk.trackSelectionReason, localHlsMediaChunk.trackSelectionData, localHlsMediaChunk.startTimeUs);
      }
      this.downstreamTrackFormat = localFormat;
    }
    return this.sampleQueues[paramInt].read(paramFormatHolder, paramDecoderInputBuffer, paramBoolean, this.loadingFinished, this.lastSeekPositionUs);
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
  
  public void seekMap(SeekMap paramSeekMap) {}
  
  public boolean seekToUs(long paramLong, boolean paramBoolean)
  {
    this.lastSeekPositionUs = paramLong;
    if ((this.sampleQueuesBuilt) && (!paramBoolean) && (!isPendingReset()) && (seekInsideBufferUs(paramLong))) {
      return false;
    }
    this.pendingResetPositionUs = paramLong;
    this.loadingFinished = false;
    this.mediaChunks.clear();
    if (this.loader.isLoading()) {
      this.loader.cancelLoading();
    }
    for (;;)
    {
      return true;
      resetSampleQueues();
    }
  }
  
  public boolean selectTracks(TrackSelection[] paramArrayOfTrackSelection, boolean[] paramArrayOfBoolean1, SampleStream[] paramArrayOfSampleStream, boolean[] paramArrayOfBoolean2, long paramLong, boolean paramBoolean)
  {
    Assertions.checkState(this.prepared);
    int j = this.enabledTrackGroupCount;
    int i = 0;
    while (i < paramArrayOfTrackSelection.length)
    {
      if ((paramArrayOfSampleStream[i] != null) && ((paramArrayOfTrackSelection[i] == null) || (paramArrayOfBoolean1[i] == 0)))
      {
        this.enabledTrackGroupCount -= 1;
        ((HlsSampleStream)paramArrayOfSampleStream[i]).unbindSampleQueue();
        paramArrayOfSampleStream[i] = null;
      }
      i += 1;
    }
    boolean bool1;
    label98:
    TrackSelection localTrackSelection;
    if (!paramBoolean)
    {
      if (this.seenFirstTrackSelection) {
        if (j != 0) {
          break label319;
        }
      }
    }
    else
    {
      bool1 = true;
      localTrackSelection = this.chunkSource.getTrackSelection();
      paramArrayOfBoolean1 = localTrackSelection;
      i = 0;
      label113:
      if (i >= paramArrayOfTrackSelection.length) {
        break label334;
      }
      localObject = paramArrayOfBoolean1;
      bool2 = bool1;
      if (paramArrayOfSampleStream[i] == null)
      {
        localObject = paramArrayOfBoolean1;
        bool2 = bool1;
        if (paramArrayOfTrackSelection[i] != null)
        {
          this.enabledTrackGroupCount += 1;
          localObject = paramArrayOfTrackSelection[i];
          j = this.trackGroups.indexOf(((TrackSelection)localObject).getTrackGroup());
          if (j == this.primaryTrackGroupIndex)
          {
            paramArrayOfBoolean1 = (boolean[])localObject;
            this.chunkSource.selectTracks((TrackSelection)localObject);
          }
          paramArrayOfSampleStream[i] = new HlsSampleStream(this, j);
          paramArrayOfBoolean2[i] = true;
          localObject = paramArrayOfBoolean1;
          bool2 = bool1;
          if (this.sampleQueuesBuilt)
          {
            localObject = paramArrayOfBoolean1;
            bool2 = bool1;
            if (!bool1)
            {
              localObject = this.sampleQueues[this.trackGroupToSampleQueueIndex[j]];
              ((SampleQueue)localObject).rewind();
              if ((((SampleQueue)localObject).advanceTo(paramLong, true, true) != -1) || (((SampleQueue)localObject).getReadIndex() == 0)) {
                break label325;
              }
              bool2 = true;
            }
          }
        }
      }
    }
    for (Object localObject = paramArrayOfBoolean1;; localObject = paramArrayOfBoolean1)
    {
      i += 1;
      paramArrayOfBoolean1 = (boolean[])localObject;
      bool1 = bool2;
      break label113;
      if (paramLong != this.lastSeekPositionUs) {
        break;
      }
      label319:
      bool1 = false;
      break label98;
      label325:
      bool2 = false;
    }
    label334:
    if (this.enabledTrackGroupCount == 0)
    {
      this.chunkSource.reset();
      this.downstreamTrackFormat = null;
      this.mediaChunks.clear();
      if (this.loader.isLoading())
      {
        if (this.sampleQueuesBuilt)
        {
          paramArrayOfTrackSelection = this.sampleQueues;
          j = paramArrayOfTrackSelection.length;
          i = 0;
          while (i < j)
          {
            paramArrayOfTrackSelection[i].discardToEnd();
            i += 1;
          }
        }
        this.loader.cancelLoading();
      }
      for (;;)
      {
        this.seenFirstTrackSelection = true;
        return bool1;
        resetSampleQueues();
      }
    }
    boolean bool2 = bool1;
    boolean bool3 = paramBoolean;
    long l;
    if (!this.mediaChunks.isEmpty())
    {
      bool2 = bool1;
      bool3 = paramBoolean;
      if (!Util.areEqual(paramArrayOfBoolean1, localTrackSelection))
      {
        i = 0;
        if (this.seenFirstTrackSelection) {
          break label621;
        }
        if (paramLong >= 0L) {
          break label615;
        }
        l = -paramLong;
        label491:
        paramArrayOfBoolean1.updateSelectedTrack(paramLong, l, -9223372036854775807L);
        j = this.chunkSource.getTrackGroup().indexOf(getLastMediaChunk().trackFormat);
        if (paramArrayOfBoolean1.getSelectedIndexInTrackGroup() == j) {}
      }
    }
    label615:
    label621:
    for (i = 1;; i = 1)
    {
      bool2 = bool1;
      bool3 = paramBoolean;
      if (i != 0)
      {
        bool3 = true;
        bool2 = true;
        this.pendingResetUpstreamFormats = true;
      }
      bool1 = bool2;
      if (!bool2) {
        break;
      }
      seekToUs(paramLong, bool3);
      i = 0;
      for (;;)
      {
        bool1 = bool2;
        if (i >= paramArrayOfSampleStream.length) {
          break;
        }
        if (paramArrayOfSampleStream[i] != null) {
          paramArrayOfBoolean2[i] = true;
        }
        i += 1;
      }
      l = 0L;
      break label491;
    }
  }
  
  public void setIsTimestampMaster(boolean paramBoolean)
  {
    this.chunkSource.setIsTimestampMaster(paramBoolean);
  }
  
  public void setSampleOffsetUs(long paramLong)
  {
    this.sampleOffsetUs = paramLong;
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int j = arrayOfSampleQueue.length;
    int i = 0;
    while (i < j)
    {
      arrayOfSampleQueue[i].setSampleOffsetUs(paramLong);
      i += 1;
    }
  }
  
  public int skipData(int paramInt, long paramLong)
  {
    if (isPendingReset()) {
      return 0;
    }
    SampleQueue localSampleQueue = this.sampleQueues[paramInt];
    if ((this.loadingFinished) && (paramLong > localSampleQueue.getLargestQueuedTimestampUs())) {
      return localSampleQueue.advanceToEnd();
    }
    int i = localSampleQueue.advanceTo(paramLong, true, true);
    paramInt = i;
    if (i == -1) {
      paramInt = 0;
    }
    return paramInt;
  }
  
  public TrackOutput track(int paramInt1, int paramInt2)
  {
    int j = this.sampleQueues.length;
    if (paramInt2 == 1)
    {
      if (this.audioSampleQueueIndex != -1)
      {
        if (this.audioSampleQueueMappingDone)
        {
          if (this.sampleQueueTrackIds[this.audioSampleQueueIndex] == paramInt1) {
            return this.sampleQueues[this.audioSampleQueueIndex];
          }
          return createDummyTrackOutput(paramInt1, paramInt2);
        }
        this.audioSampleQueueMappingDone = true;
        this.sampleQueueTrackIds[this.audioSampleQueueIndex] = paramInt1;
        return this.sampleQueues[this.audioSampleQueueIndex];
      }
      if (this.tracksEnded) {
        return createDummyTrackOutput(paramInt1, paramInt2);
      }
    }
    else if (paramInt2 == 2)
    {
      if (this.videoSampleQueueIndex != -1)
      {
        if (this.videoSampleQueueMappingDone)
        {
          if (this.sampleQueueTrackIds[this.videoSampleQueueIndex] == paramInt1) {
            return this.sampleQueues[this.videoSampleQueueIndex];
          }
          return createDummyTrackOutput(paramInt1, paramInt2);
        }
        this.videoSampleQueueMappingDone = true;
        this.sampleQueueTrackIds[this.videoSampleQueueIndex] = paramInt1;
        return this.sampleQueues[this.videoSampleQueueIndex];
      }
      if (this.tracksEnded) {
        return createDummyTrackOutput(paramInt1, paramInt2);
      }
    }
    else
    {
      int i = 0;
      while (i < j)
      {
        if (this.sampleQueueTrackIds[i] == paramInt1) {
          return this.sampleQueues[i];
        }
        i += 1;
      }
      if (this.tracksEnded) {
        return createDummyTrackOutput(paramInt1, paramInt2);
      }
    }
    SampleQueue localSampleQueue = new SampleQueue(this.allocator);
    localSampleQueue.setSampleOffsetUs(this.sampleOffsetUs);
    localSampleQueue.setUpstreamFormatChangeListener(this);
    this.sampleQueueTrackIds = Arrays.copyOf(this.sampleQueueTrackIds, j + 1);
    this.sampleQueueTrackIds[j] = paramInt1;
    this.sampleQueues = ((SampleQueue[])Arrays.copyOf(this.sampleQueues, j + 1));
    this.sampleQueues[j] = localSampleQueue;
    this.sampleQueueIsAudioVideoFlags = Arrays.copyOf(this.sampleQueueIsAudioVideoFlags, j + 1);
    boolean[] arrayOfBoolean = this.sampleQueueIsAudioVideoFlags;
    int k;
    if ((paramInt2 == 1) || (paramInt2 == 2))
    {
      k = 1;
      arrayOfBoolean[j] = k;
      this.haveAudioVideoSampleQueues |= this.sampleQueueIsAudioVideoFlags[j];
      if (paramInt2 != 1) {
        break label401;
      }
      this.audioSampleQueueMappingDone = true;
      this.audioSampleQueueIndex = j;
    }
    for (;;)
    {
      this.sampleQueuesEnabledStates = Arrays.copyOf(this.sampleQueuesEnabledStates, j + 1);
      return localSampleQueue;
      k = 0;
      break;
      label401:
      if (paramInt2 == 2)
      {
        this.videoSampleQueueMappingDone = true;
        this.videoSampleQueueIndex = j;
      }
    }
  }
  
  public void unbindSampleQueue(int paramInt)
  {
    paramInt = this.trackGroupToSampleQueueIndex[paramInt];
    Assertions.checkState(this.sampleQueuesEnabledStates[paramInt]);
    this.sampleQueuesEnabledStates[paramInt] = false;
  }
  
  public static abstract interface Callback
    extends SequenceableLoader.Callback<HlsSampleStreamWrapper>
  {
    public abstract void onPlaylistRefreshRequired(HlsMasterPlaylist.HlsUrl paramHlsUrl);
    
    public abstract void onPrepared();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/HlsSampleStreamWrapper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */