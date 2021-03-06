package org.telegram.messenger.exoplayer2.source.dash;

import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.messenger.exoplayer2.C;
import org.telegram.messenger.exoplayer2.ExoPlayer;
import org.telegram.messenger.exoplayer2.ExoPlayerLibraryInfo;
import org.telegram.messenger.exoplayer2.ParserException;
import org.telegram.messenger.exoplayer2.Timeline;
import org.telegram.messenger.exoplayer2.Timeline.Period;
import org.telegram.messenger.exoplayer2.Timeline.Window;
import org.telegram.messenger.exoplayer2.source.CompositeSequenceableLoaderFactory;
import org.telegram.messenger.exoplayer2.source.DefaultCompositeSequenceableLoaderFactory;
import org.telegram.messenger.exoplayer2.source.MediaPeriod;
import org.telegram.messenger.exoplayer2.source.MediaSource;
import org.telegram.messenger.exoplayer2.source.MediaSource.Listener;
import org.telegram.messenger.exoplayer2.source.MediaSource.MediaPeriodId;
import org.telegram.messenger.exoplayer2.source.MediaSourceEventListener;
import org.telegram.messenger.exoplayer2.source.MediaSourceEventListener.EventDispatcher;
import org.telegram.messenger.exoplayer2.source.ads.AdsMediaSource.MediaSourceFactory;
import org.telegram.messenger.exoplayer2.source.dash.manifest.AdaptationSet;
import org.telegram.messenger.exoplayer2.source.dash.manifest.DashManifest;
import org.telegram.messenger.exoplayer2.source.dash.manifest.DashManifestParser;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Period;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Representation;
import org.telegram.messenger.exoplayer2.source.dash.manifest.UtcTimingElement;
import org.telegram.messenger.exoplayer2.upstream.Allocator;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSource.Factory;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.Loader;
import org.telegram.messenger.exoplayer2.upstream.Loader.Callback;
import org.telegram.messenger.exoplayer2.upstream.LoaderErrorThrower;
import org.telegram.messenger.exoplayer2.upstream.LoaderErrorThrower.Dummy;
import org.telegram.messenger.exoplayer2.upstream.ParsingLoadable;
import org.telegram.messenger.exoplayer2.upstream.ParsingLoadable.Parser;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.Util;

public final class DashMediaSource
  implements MediaSource
{
  public static final long DEFAULT_LIVE_PRESENTATION_DELAY_FIXED_MS = 30000L;
  public static final long DEFAULT_LIVE_PRESENTATION_DELAY_PREFER_MANIFEST_MS = -1L;
  public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT = 3;
  private static final long MIN_LIVE_DEFAULT_START_POSITION_US = 5000000L;
  private static final int NOTIFY_MANIFEST_INTERVAL_MS = 5000;
  private static final String TAG = "DashMediaSource";
  private final DashChunkSource.Factory chunkSourceFactory;
  private final CompositeSequenceableLoaderFactory compositeSequenceableLoaderFactory;
  private DataSource dataSource;
  private boolean dynamicMediaPresentationEnded;
  private long elapsedRealtimeOffsetMs;
  private final MediaSourceEventListener.EventDispatcher eventDispatcher;
  private long expiredManifestPublishTimeUs;
  private int firstPeriodId;
  private Handler handler;
  private final long livePresentationDelayMs;
  private Loader loader;
  private DashManifest manifest;
  private final ManifestCallback manifestCallback;
  private final DataSource.Factory manifestDataSourceFactory;
  private IOException manifestFatalError;
  private long manifestLoadEndTimestampMs;
  private LoaderErrorThrower manifestLoadErrorThrower;
  private boolean manifestLoadPending;
  private long manifestLoadStartTimestampMs;
  private final ParsingLoadable.Parser<? extends DashManifest> manifestParser;
  private Uri manifestUri;
  private final Object manifestUriLock;
  private final int minLoadableRetryCount;
  private final SparseArray<DashMediaPeriod> periodsById;
  private final PlayerEmsgHandler.PlayerEmsgCallback playerEmsgCallback;
  private final Runnable refreshManifestRunnable;
  private final boolean sideloadedManifest;
  private final Runnable simulateManifestRefreshRunnable;
  private MediaSource.Listener sourceListener;
  private int staleManifestReloadAttempt;
  
  static
  {
    ExoPlayerLibraryInfo.registerModule("goog.exo.dash");
  }
  
  @Deprecated
  public DashMediaSource(Uri paramUri, DataSource.Factory paramFactory, DashChunkSource.Factory paramFactory1, int paramInt, long paramLong, Handler paramHandler, MediaSourceEventListener paramMediaSourceEventListener)
  {
    this(paramUri, paramFactory, new DashManifestParser(), paramFactory1, paramInt, paramLong, paramHandler, paramMediaSourceEventListener);
  }
  
  @Deprecated
  public DashMediaSource(Uri paramUri, DataSource.Factory paramFactory, DashChunkSource.Factory paramFactory1, Handler paramHandler, MediaSourceEventListener paramMediaSourceEventListener)
  {
    this(paramUri, paramFactory, paramFactory1, 3, -1L, paramHandler, paramMediaSourceEventListener);
  }
  
  @Deprecated
  public DashMediaSource(Uri paramUri, DataSource.Factory paramFactory, ParsingLoadable.Parser<? extends DashManifest> paramParser, DashChunkSource.Factory paramFactory1, int paramInt, long paramLong, Handler paramHandler, MediaSourceEventListener paramMediaSourceEventListener)
  {
    this(null, paramUri, paramFactory, paramParser, paramFactory1, new DefaultCompositeSequenceableLoaderFactory(), paramInt, paramLong, paramHandler, paramMediaSourceEventListener);
  }
  
  private DashMediaSource(DashManifest paramDashManifest, Uri paramUri, DataSource.Factory paramFactory, ParsingLoadable.Parser<? extends DashManifest> paramParser, DashChunkSource.Factory paramFactory1, CompositeSequenceableLoaderFactory paramCompositeSequenceableLoaderFactory, int paramInt, long paramLong, Handler paramHandler, MediaSourceEventListener paramMediaSourceEventListener)
  {
    this.manifest = paramDashManifest;
    this.manifestUri = paramUri;
    this.manifestDataSourceFactory = paramFactory;
    this.manifestParser = paramParser;
    this.chunkSourceFactory = paramFactory1;
    this.minLoadableRetryCount = paramInt;
    this.livePresentationDelayMs = paramLong;
    this.compositeSequenceableLoaderFactory = paramCompositeSequenceableLoaderFactory;
    if (paramDashManifest != null)
    {
      bool = true;
      this.sideloadedManifest = bool;
      this.eventDispatcher = new MediaSourceEventListener.EventDispatcher(paramHandler, paramMediaSourceEventListener);
      this.manifestUriLock = new Object();
      this.periodsById = new SparseArray();
      this.playerEmsgCallback = new DefaultPlayerEmsgCallback(null);
      this.expiredManifestPublishTimeUs = -9223372036854775807L;
      if (!this.sideloadedManifest) {
        break label169;
      }
      if (paramDashManifest.dynamic) {
        break label163;
      }
    }
    label163:
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      this.manifestCallback = null;
      this.refreshManifestRunnable = null;
      this.simulateManifestRefreshRunnable = null;
      return;
      bool = false;
      break;
    }
    label169:
    this.manifestCallback = new ManifestCallback(null);
    this.refreshManifestRunnable = new Runnable()
    {
      public void run()
      {
        DashMediaSource.this.startLoadingManifest();
      }
    };
    this.simulateManifestRefreshRunnable = new Runnable()
    {
      public void run()
      {
        DashMediaSource.this.processManifest(false);
      }
    };
  }
  
  @Deprecated
  public DashMediaSource(DashManifest paramDashManifest, DashChunkSource.Factory paramFactory, int paramInt, Handler paramHandler, MediaSourceEventListener paramMediaSourceEventListener)
  {
    this(paramDashManifest, null, null, null, paramFactory, new DefaultCompositeSequenceableLoaderFactory(), paramInt, -1L, paramHandler, paramMediaSourceEventListener);
  }
  
  @Deprecated
  public DashMediaSource(DashManifest paramDashManifest, DashChunkSource.Factory paramFactory, Handler paramHandler, MediaSourceEventListener paramMediaSourceEventListener)
  {
    this(paramDashManifest, paramFactory, 3, paramHandler, paramMediaSourceEventListener);
  }
  
  private long getManifestLoadRetryDelayMillis()
  {
    return Math.min((this.staleManifestReloadAttempt - 1) * 1000, 5000);
  }
  
  private long getNowUnixTimeUs()
  {
    if (this.elapsedRealtimeOffsetMs != 0L) {
      return C.msToUs(SystemClock.elapsedRealtime() + this.elapsedRealtimeOffsetMs);
    }
    return C.msToUs(System.currentTimeMillis());
  }
  
  private void onUtcTimestampResolutionError(IOException paramIOException)
  {
    Log.e("DashMediaSource", "Failed to resolve UtcTiming element.", paramIOException);
    processManifest(true);
  }
  
  private void onUtcTimestampResolved(long paramLong)
  {
    this.elapsedRealtimeOffsetMs = paramLong;
    processManifest(true);
  }
  
  private void processManifest(boolean paramBoolean)
  {
    int i = 0;
    while (i < this.periodsById.size())
    {
      j = this.periodsById.keyAt(i);
      if (j >= this.firstPeriodId) {
        ((DashMediaPeriod)this.periodsById.valueAt(i)).updateManifest(this.manifest, j - this.firstPeriodId);
      }
      i += 1;
    }
    int j = 0;
    int k = this.manifest.getPeriodCount() - 1;
    Object localObject = PeriodSeekInfo.createPeriodSeekInfo(this.manifest.getPeriod(0), this.manifest.getPeriodDurationUs(0));
    PeriodSeekInfo localPeriodSeekInfo = PeriodSeekInfo.createPeriodSeekInfo(this.manifest.getPeriod(k), this.manifest.getPeriodDurationUs(k));
    long l3 = ((PeriodSeekInfo)localObject).availableStartTimeUs;
    long l1 = localPeriodSeekInfo.availableEndTimeUs;
    long l2 = l3;
    long l4 = l1;
    i = j;
    if (this.manifest.dynamic)
    {
      l2 = l3;
      l4 = l1;
      i = j;
      if (!localPeriodSeekInfo.isIndexExplicit)
      {
        l4 = Math.min(getNowUnixTimeUs() - C.msToUs(this.manifest.availabilityStartTimeMs) - C.msToUs(this.manifest.getPeriod(k).startMs), l1);
        l1 = l3;
        if (this.manifest.timeShiftBufferDepthMs != -9223372036854775807L)
        {
          l1 = l4 - C.msToUs(this.manifest.timeShiftBufferDepthMs);
          i = k;
          while ((l1 < 0L) && (i > 0))
          {
            localObject = this.manifest;
            i -= 1;
            l1 += ((DashManifest)localObject).getPeriodDurationUs(i);
          }
          if (i != 0) {
            break label340;
          }
        }
      }
    }
    label340:
    for (l1 = Math.max(l3, l1);; l1 = this.manifest.getPeriodDurationUs(0))
    {
      i = 1;
      l2 = l1;
      l3 = l4 - l2;
      j = 0;
      while (j < this.manifest.getPeriodCount() - 1)
      {
        l3 += this.manifest.getPeriodDurationUs(j);
        j += 1;
      }
    }
    l1 = 0L;
    if (this.manifest.dynamic)
    {
      l4 = this.livePresentationDelayMs;
      l1 = l4;
      if (l4 == -1L)
      {
        if (this.manifest.suggestedPresentationDelayMs == -9223372036854775807L) {
          break label577;
        }
        l1 = this.manifest.suggestedPresentationDelayMs;
      }
      l4 = l3 - C.msToUs(l1);
      l1 = l4;
      if (l4 < 5000000L) {
        l1 = Math.min(5000000L, l3 / 2L);
      }
    }
    l4 = this.manifest.availabilityStartTimeMs;
    long l5 = this.manifest.getPeriod(0).startMs;
    long l6 = C.usToMs(l2);
    localObject = new DashTimeline(this.manifest.availabilityStartTimeMs, l4 + l5 + l6, this.firstPeriodId, l2, l3, l1, this.manifest);
    this.sourceListener.onSourceInfoRefreshed(this, (Timeline)localObject, this.manifest);
    if (!this.sideloadedManifest)
    {
      this.handler.removeCallbacks(this.simulateManifestRefreshRunnable);
      if (i != 0) {
        this.handler.postDelayed(this.simulateManifestRefreshRunnable, 5000L);
      }
      if (!this.manifestLoadPending) {
        break label585;
      }
      startLoadingManifest();
    }
    label577:
    label585:
    while ((!paramBoolean) || (!this.manifest.dynamic))
    {
      return;
      l1 = 30000L;
      break;
    }
    l2 = this.manifest.minUpdatePeriodMs;
    l1 = l2;
    if (l2 == 0L) {
      l1 = 5000L;
    }
    scheduleManifestRefresh(Math.max(0L, this.manifestLoadStartTimestampMs + l1 - SystemClock.elapsedRealtime()));
  }
  
  private void resolveUtcTimingElement(UtcTimingElement paramUtcTimingElement)
  {
    String str = paramUtcTimingElement.schemeIdUri;
    if ((Util.areEqual(str, "urn:mpeg:dash:utc:direct:2014")) || (Util.areEqual(str, "urn:mpeg:dash:utc:direct:2012")))
    {
      resolveUtcTimingElementDirect(paramUtcTimingElement);
      return;
    }
    if ((Util.areEqual(str, "urn:mpeg:dash:utc:http-iso:2014")) || (Util.areEqual(str, "urn:mpeg:dash:utc:http-iso:2012")))
    {
      resolveUtcTimingElementHttp(paramUtcTimingElement, new Iso8601Parser());
      return;
    }
    if ((Util.areEqual(str, "urn:mpeg:dash:utc:http-xsdate:2014")) || (Util.areEqual(str, "urn:mpeg:dash:utc:http-xsdate:2012")))
    {
      resolveUtcTimingElementHttp(paramUtcTimingElement, new XsDateTimeParser(null));
      return;
    }
    onUtcTimestampResolutionError(new IOException("Unsupported UTC timing scheme"));
  }
  
  private void resolveUtcTimingElementDirect(UtcTimingElement paramUtcTimingElement)
  {
    try
    {
      onUtcTimestampResolved(Util.parseXsDateTime(paramUtcTimingElement.value) - this.manifestLoadEndTimestampMs);
      return;
    }
    catch (ParserException paramUtcTimingElement)
    {
      onUtcTimestampResolutionError(paramUtcTimingElement);
    }
  }
  
  private void resolveUtcTimingElementHttp(UtcTimingElement paramUtcTimingElement, ParsingLoadable.Parser<Long> paramParser)
  {
    startLoading(new ParsingLoadable(this.dataSource, Uri.parse(paramUtcTimingElement.value), 5, paramParser), new UtcTimestampCallback(null), 1);
  }
  
  private void scheduleManifestRefresh(long paramLong)
  {
    this.handler.postDelayed(this.refreshManifestRunnable, paramLong);
  }
  
  private <T> void startLoading(ParsingLoadable<T> paramParsingLoadable, Loader.Callback<ParsingLoadable<T>> paramCallback, int paramInt)
  {
    long l = this.loader.startLoading(paramParsingLoadable, paramCallback, paramInt);
    this.eventDispatcher.loadStarted(paramParsingLoadable.dataSpec, paramParsingLoadable.type, l);
  }
  
  private void startLoadingManifest()
  {
    this.handler.removeCallbacks(this.refreshManifestRunnable);
    if (this.loader.isLoading())
    {
      this.manifestLoadPending = true;
      return;
    }
    synchronized (this.manifestUriLock)
    {
      Uri localUri = this.manifestUri;
      this.manifestLoadPending = false;
      startLoading(new ParsingLoadable(this.dataSource, localUri, 4, this.manifestParser), this.manifestCallback, this.minLoadableRetryCount);
      return;
    }
  }
  
  public MediaPeriod createPeriod(MediaSource.MediaPeriodId paramMediaPeriodId, Allocator paramAllocator)
  {
    int i = paramMediaPeriodId.periodIndex;
    paramMediaPeriodId = this.eventDispatcher.copyWithMediaTimeOffsetMs(this.manifest.getPeriod(i).startMs);
    paramMediaPeriodId = new DashMediaPeriod(this.firstPeriodId + i, this.manifest, i, this.chunkSourceFactory, this.minLoadableRetryCount, paramMediaPeriodId, this.elapsedRealtimeOffsetMs, this.manifestLoadErrorThrower, paramAllocator, this.compositeSequenceableLoaderFactory, this.playerEmsgCallback);
    this.periodsById.put(paramMediaPeriodId.id, paramMediaPeriodId);
    return paramMediaPeriodId;
  }
  
  public void maybeThrowSourceInfoRefreshError()
    throws IOException
  {
    this.manifestLoadErrorThrower.maybeThrowError();
  }
  
  void onDashLiveMediaPresentationEndSignalEncountered()
  {
    this.dynamicMediaPresentationEnded = true;
  }
  
  void onDashManifestPublishTimeExpired(long paramLong)
  {
    if ((this.expiredManifestPublishTimeUs == -9223372036854775807L) || (this.expiredManifestPublishTimeUs < paramLong)) {
      this.expiredManifestPublishTimeUs = paramLong;
    }
  }
  
  void onDashManifestRefreshRequested()
  {
    this.handler.removeCallbacks(this.simulateManifestRefreshRunnable);
    startLoadingManifest();
  }
  
  void onLoadCanceled(ParsingLoadable<?> paramParsingLoadable, long paramLong1, long paramLong2)
  {
    this.eventDispatcher.loadCanceled(paramParsingLoadable.dataSpec, paramParsingLoadable.type, paramLong1, paramLong2, paramParsingLoadable.bytesLoaded());
  }
  
  void onManifestLoadCompleted(ParsingLoadable<DashManifest> paramParsingLoadable, long paramLong1, long paramLong2)
  {
    this.eventDispatcher.loadCompleted(paramParsingLoadable.dataSpec, paramParsingLoadable.type, paramLong1, paramLong2, paramParsingLoadable.bytesLoaded());
    ??? = (DashManifest)paramParsingLoadable.getResult();
    if (this.manifest == null) {}
    int k;
    for (int j = 0;; j = this.manifest.getPeriodCount())
    {
      k = 0;
      long l = ((DashManifest)???).getPeriod(0).startMs;
      while ((k < j) && (this.manifest.getPeriod(k).startMs < l)) {
        k += 1;
      }
    }
    if (((DashManifest)???).dynamic)
    {
      int i = 0;
      if (j - k > ((DashManifest)???).getPeriodCount())
      {
        Log.w("DashMediaSource", "Loaded out of sync manifest");
        i = 1;
      }
      while (i != 0)
      {
        i = this.staleManifestReloadAttempt;
        this.staleManifestReloadAttempt = (i + 1);
        if (i < this.minLoadableRetryCount)
        {
          scheduleManifestRefresh(getManifestLoadRetryDelayMillis());
          return;
          if ((this.dynamicMediaPresentationEnded) || (((DashManifest)???).publishTimeMs <= this.expiredManifestPublishTimeUs))
          {
            Log.w("DashMediaSource", "Loaded stale dynamic manifest: " + ((DashManifest)???).publishTimeMs + ", " + this.dynamicMediaPresentationEnded + ", " + this.expiredManifestPublishTimeUs);
            i = 1;
          }
        }
        else
        {
          this.manifestFatalError = new DashManifestStaleException();
          return;
        }
      }
      this.staleManifestReloadAttempt = 0;
    }
    this.manifest = ((DashManifest)???);
    this.manifestLoadPending &= this.manifest.dynamic;
    this.manifestLoadStartTimestampMs = (paramLong1 - paramLong2);
    this.manifestLoadEndTimestampMs = paramLong1;
    if (this.manifest.location != null) {}
    synchronized (this.manifestUriLock)
    {
      if (paramParsingLoadable.dataSpec.uri == this.manifestUri) {
        this.manifestUri = this.manifest.location;
      }
      if (j != 0) {
        break label394;
      }
      if (this.manifest.utcTiming != null)
      {
        resolveUtcTimingElement(this.manifest.utcTiming);
        return;
      }
    }
    processManifest(true);
    return;
    label394:
    this.firstPeriodId += k;
    processManifest(true);
  }
  
  int onManifestLoadError(ParsingLoadable<DashManifest> paramParsingLoadable, long paramLong1, long paramLong2, IOException paramIOException)
  {
    boolean bool = paramIOException instanceof ParserException;
    this.eventDispatcher.loadError(paramParsingLoadable.dataSpec, paramParsingLoadable.type, paramLong1, paramLong2, paramParsingLoadable.bytesLoaded(), paramIOException, bool);
    if (bool) {
      return 3;
    }
    return 0;
  }
  
  void onUtcTimestampLoadCompleted(ParsingLoadable<Long> paramParsingLoadable, long paramLong1, long paramLong2)
  {
    this.eventDispatcher.loadCompleted(paramParsingLoadable.dataSpec, paramParsingLoadable.type, paramLong1, paramLong2, paramParsingLoadable.bytesLoaded());
    onUtcTimestampResolved(((Long)paramParsingLoadable.getResult()).longValue() - paramLong1);
  }
  
  int onUtcTimestampLoadError(ParsingLoadable<Long> paramParsingLoadable, long paramLong1, long paramLong2, IOException paramIOException)
  {
    this.eventDispatcher.loadError(paramParsingLoadable.dataSpec, paramParsingLoadable.type, paramLong1, paramLong2, paramParsingLoadable.bytesLoaded(), paramIOException, true);
    onUtcTimestampResolutionError(paramIOException);
    return 2;
  }
  
  public void prepareSource(ExoPlayer paramExoPlayer, boolean paramBoolean, MediaSource.Listener paramListener)
  {
    if (this.sourceListener == null) {}
    for (paramBoolean = true;; paramBoolean = false)
    {
      Assertions.checkState(paramBoolean, "MediaSource instances are not allowed to be reused.");
      this.sourceListener = paramListener;
      if (!this.sideloadedManifest) {
        break;
      }
      this.manifestLoadErrorThrower = new LoaderErrorThrower.Dummy();
      processManifest(false);
      return;
    }
    this.dataSource = this.manifestDataSourceFactory.createDataSource();
    this.loader = new Loader("Loader:DashMediaSource");
    this.manifestLoadErrorThrower = new ManifestLoadErrorThrower();
    this.handler = new Handler();
    startLoadingManifest();
  }
  
  public void releasePeriod(MediaPeriod paramMediaPeriod)
  {
    paramMediaPeriod = (DashMediaPeriod)paramMediaPeriod;
    paramMediaPeriod.release();
    this.periodsById.remove(paramMediaPeriod.id);
  }
  
  public void releaseSource()
  {
    this.manifestLoadPending = false;
    this.dataSource = null;
    this.manifestLoadErrorThrower = null;
    if (this.loader != null)
    {
      this.loader.release();
      this.loader = null;
    }
    this.manifestLoadStartTimestampMs = 0L;
    this.manifestLoadEndTimestampMs = 0L;
    this.manifest = null;
    if (this.handler != null)
    {
      this.handler.removeCallbacksAndMessages(null);
      this.handler = null;
    }
    this.elapsedRealtimeOffsetMs = 0L;
    this.periodsById.clear();
  }
  
  public void replaceManifestUri(Uri paramUri)
  {
    synchronized (this.manifestUriLock)
    {
      this.manifestUri = paramUri;
      return;
    }
  }
  
  private static final class DashTimeline
    extends Timeline
  {
    private final int firstPeriodId;
    private final DashManifest manifest;
    private final long offsetInFirstPeriodUs;
    private final long presentationStartTimeMs;
    private final long windowDefaultStartPositionUs;
    private final long windowDurationUs;
    private final long windowStartTimeMs;
    
    public DashTimeline(long paramLong1, long paramLong2, int paramInt, long paramLong3, long paramLong4, long paramLong5, DashManifest paramDashManifest)
    {
      this.presentationStartTimeMs = paramLong1;
      this.windowStartTimeMs = paramLong2;
      this.firstPeriodId = paramInt;
      this.offsetInFirstPeriodUs = paramLong3;
      this.windowDurationUs = paramLong4;
      this.windowDefaultStartPositionUs = paramLong5;
      this.manifest = paramDashManifest;
    }
    
    private long getAdjustedWindowDefaultStartPositionUs(long paramLong)
    {
      long l2 = this.windowDefaultStartPositionUs;
      if (!this.manifest.dynamic) {
        return l2;
      }
      long l1 = l2;
      if (paramLong > 0L)
      {
        paramLong = l2 + paramLong;
        l1 = paramLong;
        if (paramLong > this.windowDurationUs) {
          return -9223372036854775807L;
        }
      }
      int i = 0;
      paramLong = this.offsetInFirstPeriodUs + l1;
      for (l2 = this.manifest.getPeriodDurationUs(0); (i < this.manifest.getPeriodCount() - 1) && (paramLong >= l2); l2 = this.manifest.getPeriodDurationUs(i))
      {
        paramLong -= l2;
        i += 1;
      }
      Object localObject = this.manifest.getPeriod(i);
      i = ((Period)localObject).getAdaptationSetIndex(2);
      if (i == -1) {
        return l1;
      }
      localObject = ((Representation)((AdaptationSet)((Period)localObject).adaptationSets.get(i)).representations.get(0)).getIndex();
      if ((localObject == null) || (((DashSegmentIndex)localObject).getSegmentCount(l2) == 0)) {
        return l1;
      }
      return ((DashSegmentIndex)localObject).getTimeUs(((DashSegmentIndex)localObject).getSegmentNum(paramLong, l2)) + l1 - paramLong;
    }
    
    public int getIndexOfPeriod(Object paramObject)
    {
      if (!(paramObject instanceof Integer)) {}
      int i;
      do
      {
        return -1;
        i = ((Integer)paramObject).intValue();
      } while ((i < this.firstPeriodId) || (i >= this.firstPeriodId + getPeriodCount()));
      return i - this.firstPeriodId;
    }
    
    public Timeline.Period getPeriod(int paramInt, Timeline.Period paramPeriod, boolean paramBoolean)
    {
      Integer localInteger = null;
      Assertions.checkIndex(paramInt, 0, this.manifest.getPeriodCount());
      if (paramBoolean) {}
      for (String str = this.manifest.getPeriod(paramInt).id;; str = null)
      {
        if (paramBoolean) {
          localInteger = Integer.valueOf(this.firstPeriodId + Assertions.checkIndex(paramInt, 0, this.manifest.getPeriodCount()));
        }
        return paramPeriod.set(str, localInteger, 0, this.manifest.getPeriodDurationUs(paramInt), C.msToUs(this.manifest.getPeriod(paramInt).startMs - this.manifest.getPeriod(0).startMs) - this.offsetInFirstPeriodUs);
      }
    }
    
    public int getPeriodCount()
    {
      return this.manifest.getPeriodCount();
    }
    
    public Timeline.Window getWindow(int paramInt, Timeline.Window paramWindow, boolean paramBoolean, long paramLong)
    {
      Assertions.checkIndex(paramInt, 0, 1);
      paramLong = getAdjustedWindowDefaultStartPositionUs(paramLong);
      return paramWindow.set(null, this.presentationStartTimeMs, this.windowStartTimeMs, true, this.manifest.dynamic, paramLong, this.windowDurationUs, 0, this.manifest.getPeriodCount() - 1, this.offsetInFirstPeriodUs);
    }
    
    public int getWindowCount()
    {
      return 1;
    }
  }
  
  private final class DefaultPlayerEmsgCallback
    implements PlayerEmsgHandler.PlayerEmsgCallback
  {
    private DefaultPlayerEmsgCallback() {}
    
    public void onDashLiveMediaPresentationEndSignalEncountered()
    {
      DashMediaSource.this.onDashLiveMediaPresentationEndSignalEncountered();
    }
    
    public void onDashManifestPublishTimeExpired(long paramLong)
    {
      DashMediaSource.this.onDashManifestPublishTimeExpired(paramLong);
    }
    
    public void onDashManifestRefreshRequested()
    {
      DashMediaSource.this.onDashManifestRefreshRequested();
    }
  }
  
  public static final class Factory
    implements AdsMediaSource.MediaSourceFactory
  {
    private final DashChunkSource.Factory chunkSourceFactory;
    private CompositeSequenceableLoaderFactory compositeSequenceableLoaderFactory;
    private boolean isCreateCalled;
    private long livePresentationDelayMs;
    private final DataSource.Factory manifestDataSourceFactory;
    private ParsingLoadable.Parser<? extends DashManifest> manifestParser;
    private int minLoadableRetryCount;
    
    public Factory(DashChunkSource.Factory paramFactory, DataSource.Factory paramFactory1)
    {
      this.chunkSourceFactory = ((DashChunkSource.Factory)Assertions.checkNotNull(paramFactory));
      this.manifestDataSourceFactory = paramFactory1;
      this.minLoadableRetryCount = 3;
      this.livePresentationDelayMs = -1L;
      this.compositeSequenceableLoaderFactory = new DefaultCompositeSequenceableLoaderFactory();
    }
    
    public DashMediaSource createMediaSource(Uri paramUri)
    {
      return createMediaSource(paramUri, null, null);
    }
    
    public DashMediaSource createMediaSource(Uri paramUri, Handler paramHandler, MediaSourceEventListener paramMediaSourceEventListener)
    {
      this.isCreateCalled = true;
      if (this.manifestParser == null) {
        this.manifestParser = new DashManifestParser();
      }
      return new DashMediaSource(null, (Uri)Assertions.checkNotNull(paramUri), this.manifestDataSourceFactory, this.manifestParser, this.chunkSourceFactory, this.compositeSequenceableLoaderFactory, this.minLoadableRetryCount, this.livePresentationDelayMs, paramHandler, paramMediaSourceEventListener, null);
    }
    
    public DashMediaSource createMediaSource(DashManifest paramDashManifest, Handler paramHandler, MediaSourceEventListener paramMediaSourceEventListener)
    {
      if (!paramDashManifest.dynamic) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkArgument(bool);
        this.isCreateCalled = true;
        return new DashMediaSource(paramDashManifest, null, null, null, this.chunkSourceFactory, this.compositeSequenceableLoaderFactory, this.minLoadableRetryCount, this.livePresentationDelayMs, paramHandler, paramMediaSourceEventListener, null);
      }
    }
    
    public int[] getSupportedTypes()
    {
      return new int[] { 0 };
    }
    
    public Factory setCompositeSequenceableLoaderFactory(CompositeSequenceableLoaderFactory paramCompositeSequenceableLoaderFactory)
    {
      if (!this.isCreateCalled) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkState(bool);
        this.compositeSequenceableLoaderFactory = ((CompositeSequenceableLoaderFactory)Assertions.checkNotNull(paramCompositeSequenceableLoaderFactory));
        return this;
      }
    }
    
    public Factory setLivePresentationDelayMs(long paramLong)
    {
      if (!this.isCreateCalled) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkState(bool);
        this.livePresentationDelayMs = paramLong;
        return this;
      }
    }
    
    public Factory setManifestParser(ParsingLoadable.Parser<? extends DashManifest> paramParser)
    {
      if (!this.isCreateCalled) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkState(bool);
        this.manifestParser = ((ParsingLoadable.Parser)Assertions.checkNotNull(paramParser));
        return this;
      }
    }
    
    public Factory setMinLoadableRetryCount(int paramInt)
    {
      if (!this.isCreateCalled) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkState(bool);
        this.minLoadableRetryCount = paramInt;
        return this;
      }
    }
  }
  
  static final class Iso8601Parser
    implements ParsingLoadable.Parser<Long>
  {
    private static final Pattern TIMESTAMP_WITH_TIMEZONE_PATTERN = Pattern.compile("(.+?)(Z|((\\+|-|−)(\\d\\d)(:?(\\d\\d))?))");
    
    public Long parse(Uri paramUri, InputStream paramInputStream)
      throws IOException
    {
      paramInputStream = new BufferedReader(new InputStreamReader(paramInputStream)).readLine();
      try
      {
        paramUri = TIMESTAMP_WITH_TIMEZONE_PATTERN.matcher(paramInputStream);
        if (!paramUri.matches()) {
          throw new ParserException("Couldn't parse timestamp: " + paramInputStream);
        }
      }
      catch (ParseException paramUri)
      {
        throw new ParserException(paramUri);
      }
      paramInputStream = paramUri.group(1);
      SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
      localSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      long l3 = localSimpleDateFormat.parse(paramInputStream).getTime();
      if ("Z".equals(paramUri.group(2)))
      {
        l1 = l3;
        return Long.valueOf(l1);
      }
      if ("+".equals(paramUri.group(4))) {}
      for (long l1 = 1L;; l1 = -1L)
      {
        long l4 = Long.parseLong(paramUri.group(5));
        paramUri = paramUri.group(7);
        long l2;
        if (TextUtils.isEmpty(paramUri)) {
          l2 = 0L;
        } else {
          l2 = Long.parseLong(paramUri);
        }
        l1 = l3 - l1 * ((60L * l4 + l2) * 60L * 1000L);
        break;
      }
    }
  }
  
  private final class ManifestCallback
    implements Loader.Callback<ParsingLoadable<DashManifest>>
  {
    private ManifestCallback() {}
    
    public void onLoadCanceled(ParsingLoadable<DashManifest> paramParsingLoadable, long paramLong1, long paramLong2, boolean paramBoolean)
    {
      DashMediaSource.this.onLoadCanceled(paramParsingLoadable, paramLong1, paramLong2);
    }
    
    public void onLoadCompleted(ParsingLoadable<DashManifest> paramParsingLoadable, long paramLong1, long paramLong2)
    {
      DashMediaSource.this.onManifestLoadCompleted(paramParsingLoadable, paramLong1, paramLong2);
    }
    
    public int onLoadError(ParsingLoadable<DashManifest> paramParsingLoadable, long paramLong1, long paramLong2, IOException paramIOException)
    {
      return DashMediaSource.this.onManifestLoadError(paramParsingLoadable, paramLong1, paramLong2, paramIOException);
    }
  }
  
  final class ManifestLoadErrorThrower
    implements LoaderErrorThrower
  {
    ManifestLoadErrorThrower() {}
    
    private void maybeThrowManifestError()
      throws IOException
    {
      if (DashMediaSource.this.manifestFatalError != null) {
        throw DashMediaSource.this.manifestFatalError;
      }
    }
    
    public void maybeThrowError()
      throws IOException
    {
      DashMediaSource.this.loader.maybeThrowError();
      maybeThrowManifestError();
    }
    
    public void maybeThrowError(int paramInt)
      throws IOException
    {
      DashMediaSource.this.loader.maybeThrowError(paramInt);
      maybeThrowManifestError();
    }
  }
  
  private static final class PeriodSeekInfo
  {
    public final long availableEndTimeUs;
    public final long availableStartTimeUs;
    public final boolean isIndexExplicit;
    
    private PeriodSeekInfo(boolean paramBoolean, long paramLong1, long paramLong2)
    {
      this.isIndexExplicit = paramBoolean;
      this.availableStartTimeUs = paramLong1;
      this.availableEndTimeUs = paramLong2;
    }
    
    public static PeriodSeekInfo createPeriodSeekInfo(Period paramPeriod, long paramLong)
    {
      int m = paramPeriod.adaptationSets.size();
      long l4 = 0L;
      long l3 = Long.MAX_VALUE;
      boolean bool = false;
      int j = 0;
      int i = 0;
      if (i < m)
      {
        DashSegmentIndex localDashSegmentIndex = ((Representation)((AdaptationSet)paramPeriod.adaptationSets.get(i)).representations.get(0)).getIndex();
        if (localDashSegmentIndex == null) {
          return new PeriodSeekInfo(true, 0L, paramLong);
        }
        bool |= localDashSegmentIndex.isExplicit();
        int n = localDashSegmentIndex.getSegmentCount(paramLong);
        int k;
        long l1;
        long l2;
        if (n == 0)
        {
          k = 1;
          l1 = 0L;
          l2 = 0L;
        }
        for (;;)
        {
          i += 1;
          l4 = l1;
          l3 = l2;
          j = k;
          break;
          l1 = l4;
          l2 = l3;
          k = j;
          if (j == 0)
          {
            int i1 = localDashSegmentIndex.getFirstSegmentNum();
            l4 = Math.max(l4, localDashSegmentIndex.getTimeUs(i1));
            l1 = l4;
            l2 = l3;
            k = j;
            if (n != -1)
            {
              k = i1 + n - 1;
              l2 = Math.min(l3, localDashSegmentIndex.getTimeUs(k) + localDashSegmentIndex.getDurationUs(k, paramLong));
              l1 = l4;
              k = j;
            }
          }
        }
      }
      return new PeriodSeekInfo(bool, l4, l3);
    }
  }
  
  private final class UtcTimestampCallback
    implements Loader.Callback<ParsingLoadable<Long>>
  {
    private UtcTimestampCallback() {}
    
    public void onLoadCanceled(ParsingLoadable<Long> paramParsingLoadable, long paramLong1, long paramLong2, boolean paramBoolean)
    {
      DashMediaSource.this.onLoadCanceled(paramParsingLoadable, paramLong1, paramLong2);
    }
    
    public void onLoadCompleted(ParsingLoadable<Long> paramParsingLoadable, long paramLong1, long paramLong2)
    {
      DashMediaSource.this.onUtcTimestampLoadCompleted(paramParsingLoadable, paramLong1, paramLong2);
    }
    
    public int onLoadError(ParsingLoadable<Long> paramParsingLoadable, long paramLong1, long paramLong2, IOException paramIOException)
    {
      return DashMediaSource.this.onUtcTimestampLoadError(paramParsingLoadable, paramLong1, paramLong2, paramIOException);
    }
  }
  
  private static final class XsDateTimeParser
    implements ParsingLoadable.Parser<Long>
  {
    public Long parse(Uri paramUri, InputStream paramInputStream)
      throws IOException
    {
      return Long.valueOf(Util.parseXsDateTime(new BufferedReader(new InputStreamReader(paramInputStream)).readLine()));
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/DashMediaSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */