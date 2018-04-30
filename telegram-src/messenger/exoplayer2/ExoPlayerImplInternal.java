package org.telegram.messenger.exoplayer2;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.telegram.messenger.exoplayer2.source.MediaPeriod;
import org.telegram.messenger.exoplayer2.source.MediaPeriod.Callback;
import org.telegram.messenger.exoplayer2.source.MediaSource;
import org.telegram.messenger.exoplayer2.source.MediaSource.Listener;
import org.telegram.messenger.exoplayer2.source.MediaSource.MediaPeriodId;
import org.telegram.messenger.exoplayer2.source.SampleStream;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelectionArray;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelector;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelector.InvalidationListener;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelectorResult;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.Clock;
import org.telegram.messenger.exoplayer2.util.HandlerWrapper;
import org.telegram.messenger.exoplayer2.util.TraceUtil;
import org.telegram.messenger.exoplayer2.util.Util;

final class ExoPlayerImplInternal
  implements Handler.Callback, DefaultMediaClock.PlaybackParameterListener, PlayerMessage.Sender, MediaPeriod.Callback, MediaSource.Listener, TrackSelector.InvalidationListener
{
  private static final int IDLE_INTERVAL_MS = 1000;
  private static final int MSG_DO_SOME_WORK = 2;
  public static final int MSG_ERROR = 2;
  private static final int MSG_PERIOD_PREPARED = 9;
  public static final int MSG_PLAYBACK_INFO_CHANGED = 0;
  public static final int MSG_PLAYBACK_PARAMETERS_CHANGED = 1;
  private static final int MSG_PREPARE = 0;
  private static final int MSG_REFRESH_SOURCE_INFO = 8;
  private static final int MSG_RELEASE = 7;
  private static final int MSG_SEEK_TO = 3;
  private static final int MSG_SEND_MESSAGE = 14;
  private static final int MSG_SEND_MESSAGE_TO_TARGET_THREAD = 15;
  private static final int MSG_SET_PLAYBACK_PARAMETERS = 4;
  private static final int MSG_SET_PLAY_WHEN_READY = 1;
  private static final int MSG_SET_REPEAT_MODE = 12;
  private static final int MSG_SET_SEEK_PARAMETERS = 5;
  private static final int MSG_SET_SHUFFLE_ENABLED = 13;
  private static final int MSG_SOURCE_CONTINUE_LOADING_REQUESTED = 10;
  private static final int MSG_STOP = 6;
  private static final int MSG_TRACK_SELECTION_INVALIDATED = 11;
  private static final int PREPARING_SOURCE_INTERVAL_MS = 10;
  private static final int RENDERER_TIMESTAMP_OFFSET_US = 60000000;
  private static final int RENDERING_INTERVAL_MS = 10;
  private static final String TAG = "ExoPlayerImplInternal";
  private final long backBufferDurationUs;
  private final Clock clock;
  private final TrackSelectorResult emptyTrackSelectorResult;
  private Renderer[] enabledRenderers;
  private final Handler eventHandler;
  private final HandlerWrapper handler;
  private final HandlerThread internalPlaybackThread;
  private final LoadControl loadControl;
  private final DefaultMediaClock mediaClock;
  private MediaSource mediaSource;
  private int nextPendingMessageIndex;
  private SeekPosition pendingInitialSeekPosition;
  private final ArrayList<PendingMessageInfo> pendingMessages;
  private int pendingPrepareCount;
  private final Timeline.Period period;
  private boolean playWhenReady;
  private PlaybackInfo playbackInfo;
  private final PlaybackInfoUpdate playbackInfoUpdate;
  private final ExoPlayer player;
  private final MediaPeriodQueue queue;
  private boolean rebuffering;
  private boolean released;
  private final RendererCapabilities[] rendererCapabilities;
  private long rendererPositionUs;
  private final Renderer[] renderers;
  private int repeatMode;
  private final boolean retainBackBufferFromKeyframe;
  private SeekParameters seekParameters;
  private boolean shuffleModeEnabled;
  private final TrackSelector trackSelector;
  private final Timeline.Window window;
  
  public ExoPlayerImplInternal(Renderer[] paramArrayOfRenderer, TrackSelector paramTrackSelector, TrackSelectorResult paramTrackSelectorResult, LoadControl paramLoadControl, boolean paramBoolean1, int paramInt, boolean paramBoolean2, Handler paramHandler, ExoPlayer paramExoPlayer, Clock paramClock)
  {
    this.renderers = paramArrayOfRenderer;
    this.trackSelector = paramTrackSelector;
    this.emptyTrackSelectorResult = paramTrackSelectorResult;
    this.loadControl = paramLoadControl;
    this.playWhenReady = paramBoolean1;
    this.repeatMode = paramInt;
    this.shuffleModeEnabled = paramBoolean2;
    this.eventHandler = paramHandler;
    this.player = paramExoPlayer;
    this.clock = paramClock;
    this.queue = new MediaPeriodQueue();
    this.backBufferDurationUs = paramLoadControl.getBackBufferDurationUs();
    this.retainBackBufferFromKeyframe = paramLoadControl.retainBackBufferFromKeyframe();
    this.seekParameters = SeekParameters.DEFAULT;
    this.playbackInfo = new PlaybackInfo(null, -9223372036854775807L, paramTrackSelectorResult);
    this.playbackInfoUpdate = new PlaybackInfoUpdate(null);
    this.rendererCapabilities = new RendererCapabilities[paramArrayOfRenderer.length];
    paramInt = 0;
    while (paramInt < paramArrayOfRenderer.length)
    {
      paramArrayOfRenderer[paramInt].setIndex(paramInt);
      this.rendererCapabilities[paramInt] = paramArrayOfRenderer[paramInt].getCapabilities();
      paramInt += 1;
    }
    this.mediaClock = new DefaultMediaClock(this, paramClock);
    this.pendingMessages = new ArrayList();
    this.enabledRenderers = new Renderer[0];
    this.window = new Timeline.Window();
    this.period = new Timeline.Period();
    paramTrackSelector.init(this);
    this.internalPlaybackThread = new HandlerThread("ExoPlayerImplInternal:Handler", -16);
    this.internalPlaybackThread.start();
    this.handler = paramClock.createHandler(this.internalPlaybackThread.getLooper(), this);
  }
  
  private void deliverMessage(PlayerMessage paramPlayerMessage)
  {
    try
    {
      paramPlayerMessage.getTarget().handleMessage(paramPlayerMessage.getType(), paramPlayerMessage.getPayload());
      return;
    }
    catch (ExoPlaybackException localExoPlaybackException)
    {
      this.eventHandler.obtainMessage(2, localExoPlaybackException).sendToTarget();
      return;
    }
    finally
    {
      paramPlayerMessage.markAsProcessed(true);
    }
  }
  
  private void disableRenderer(Renderer paramRenderer)
    throws ExoPlaybackException
  {
    this.mediaClock.onRendererDisabled(paramRenderer);
    ensureStopped(paramRenderer);
    paramRenderer.disable();
  }
  
  private void doSomeWork()
    throws ExoPlaybackException, IOException
  {
    long l1 = this.clock.uptimeMillis();
    updatePeriods();
    if (!this.queue.hasPlayingPeriod())
    {
      maybeThrowPeriodPrepareError();
      scheduleNextWork(l1, 10L);
      return;
    }
    Object localObject = this.queue.getPlayingPeriod();
    TraceUtil.beginSection("doSomeWork");
    updatePlaybackPositions();
    long l2 = SystemClock.elapsedRealtime();
    ((MediaPeriodHolder)localObject).mediaPeriod.discardBuffer(this.playbackInfo.positionUs - this.backBufferDurationUs, this.retainBackBufferFromKeyframe);
    int i = 1;
    boolean bool = true;
    Renderer[] arrayOfRenderer = this.enabledRenderers;
    int m = arrayOfRenderer.length;
    int j = 0;
    if (j < m)
    {
      Renderer localRenderer = arrayOfRenderer[j];
      localRenderer.render(this.rendererPositionUs, l2 * 1000L);
      label152:
      int k;
      if ((i != 0) && (localRenderer.isEnded()))
      {
        i = 1;
        if ((!localRenderer.isReady()) && (!localRenderer.isEnded()) && (!rendererWaitingForNextStream(localRenderer))) {
          break label218;
        }
        k = 1;
        label183:
        if (k == 0) {
          localRenderer.maybeThrowStreamError();
        }
        if ((!bool) || (k == 0)) {
          break label223;
        }
      }
      label218:
      label223:
      for (bool = true;; bool = false)
      {
        j += 1;
        break;
        i = 0;
        break label152;
        k = 0;
        break label183;
      }
    }
    if (!bool) {
      maybeThrowPeriodPrepareError();
    }
    l2 = ((MediaPeriodHolder)localObject).info.durationUs;
    if ((i != 0) && ((l2 == -9223372036854775807L) || (l2 <= this.playbackInfo.positionUs)) && (((MediaPeriodHolder)localObject).info.isFinal))
    {
      setState(4);
      stopRenderers();
    }
    while (this.playbackInfo.playbackState == 2)
    {
      localObject = this.enabledRenderers;
      j = localObject.length;
      i = 0;
      for (;;)
      {
        if (i < j)
        {
          localObject[i].maybeThrowStreamError();
          i += 1;
          continue;
          if ((this.playbackInfo.playbackState == 2) && (shouldTransitionToReadyState(bool)))
          {
            setState(3);
            if (!this.playWhenReady) {
              break;
            }
            startRenderers();
            break;
          }
          if (this.playbackInfo.playbackState != 3) {
            break;
          }
          if (this.enabledRenderers.length == 0) {
            if (isTimelineReady()) {
              break;
            }
          }
          for (;;)
          {
            this.rebuffering = this.playWhenReady;
            setState(2);
            stopRenderers();
            break;
            if (bool) {
              break;
            }
          }
        }
      }
    }
    if (((this.playWhenReady) && (this.playbackInfo.playbackState == 3)) || (this.playbackInfo.playbackState == 2)) {
      scheduleNextWork(l1, 10L);
    }
    for (;;)
    {
      TraceUtil.endSection();
      return;
      if ((this.enabledRenderers.length != 0) && (this.playbackInfo.playbackState != 4)) {
        scheduleNextWork(l1, 1000L);
      } else {
        this.handler.removeMessages(2);
      }
    }
  }
  
  private void enableRenderer(int paramInt1, boolean paramBoolean, int paramInt2)
    throws ExoPlaybackException
  {
    boolean bool = true;
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getPlayingPeriod();
    Renderer localRenderer = this.renderers[paramInt1];
    this.enabledRenderers[paramInt2] = localRenderer;
    RendererConfiguration localRendererConfiguration;
    Format[] arrayOfFormat;
    if (localRenderer.getState() == 0)
    {
      localRendererConfiguration = localMediaPeriodHolder.trackSelectorResult.rendererConfigurations[paramInt1];
      arrayOfFormat = getFormats(localMediaPeriodHolder.trackSelectorResult.selections.get(paramInt1));
      if ((!this.playWhenReady) || (this.playbackInfo.playbackState != 3)) {
        break label147;
      }
      paramInt2 = 1;
      if ((paramBoolean) || (paramInt2 == 0)) {
        break label152;
      }
    }
    label147:
    label152:
    for (paramBoolean = bool;; paramBoolean = false)
    {
      localRenderer.enable(localRendererConfiguration, arrayOfFormat, localMediaPeriodHolder.sampleStreams[paramInt1], this.rendererPositionUs, paramBoolean, localMediaPeriodHolder.getRendererOffset());
      this.mediaClock.onRendererEnabled(localRenderer);
      if (paramInt2 != 0) {
        localRenderer.start();
      }
      return;
      paramInt2 = 0;
      break;
    }
  }
  
  private void enableRenderers(boolean[] paramArrayOfBoolean, int paramInt)
    throws ExoPlaybackException
  {
    this.enabledRenderers = new Renderer[paramInt];
    int i = 0;
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getPlayingPeriod();
    paramInt = 0;
    while (paramInt < this.renderers.length)
    {
      int j = i;
      if (localMediaPeriodHolder.trackSelectorResult.renderersEnabled[paramInt] != 0)
      {
        enableRenderer(paramInt, paramArrayOfBoolean[paramInt], i);
        j = i + 1;
      }
      paramInt += 1;
      i = j;
    }
  }
  
  private void ensureStopped(Renderer paramRenderer)
    throws ExoPlaybackException
  {
    if (paramRenderer.getState() == 2) {
      paramRenderer.stop();
    }
  }
  
  private int getFirstPeriodIndex()
  {
    Timeline localTimeline = this.playbackInfo.timeline;
    if ((localTimeline == null) || (localTimeline.isEmpty())) {
      return 0;
    }
    return localTimeline.getWindow(localTimeline.getFirstWindowIndex(this.shuffleModeEnabled), this.window).firstPeriodIndex;
  }
  
  private static Format[] getFormats(TrackSelection paramTrackSelection)
  {
    if (paramTrackSelection != null) {}
    Format[] arrayOfFormat;
    for (int i = paramTrackSelection.length();; i = 0)
    {
      arrayOfFormat = new Format[i];
      int j = 0;
      while (j < i)
      {
        arrayOfFormat[j] = paramTrackSelection.getFormat(j);
        j += 1;
      }
    }
    return arrayOfFormat;
  }
  
  private Pair<Integer, Long> getPeriodPosition(Timeline paramTimeline, int paramInt, long paramLong)
  {
    return paramTimeline.getPeriodPosition(this.window, this.period, paramInt, paramLong);
  }
  
  private void handleContinueLoadingRequested(MediaPeriod paramMediaPeriod)
  {
    if (!this.queue.isLoading(paramMediaPeriod)) {
      return;
    }
    this.queue.reevaluateBuffer(this.rendererPositionUs);
    maybeContinueLoading();
  }
  
  private void handlePeriodPrepared(MediaPeriod paramMediaPeriod)
    throws ExoPlaybackException
  {
    if (!this.queue.isLoading(paramMediaPeriod)) {
      return;
    }
    updateLoadControlTrackSelection(this.queue.handleLoadingPeriodPrepared(this.mediaClock.getPlaybackParameters().speed));
    if (!this.queue.hasPlayingPeriod())
    {
      resetRendererPosition(this.queue.advancePlayingPeriod().info.startPositionUs);
      updatePlayingPeriodRenderers(null);
    }
    maybeContinueLoading();
  }
  
  private void handleSourceInfoRefreshEndedPlayback()
  {
    setState(4);
    resetInternal(false, true, false);
  }
  
  private void handleSourceInfoRefreshed(MediaSourceRefreshInfo paramMediaSourceRefreshInfo)
    throws ExoPlaybackException
  {
    if (paramMediaSourceRefreshInfo.source != this.mediaSource) {}
    label579:
    label794:
    do
    {
      for (;;)
      {
        return;
        Timeline localTimeline = this.playbackInfo.timeline;
        Object localObject2 = paramMediaSourceRefreshInfo.timeline;
        paramMediaSourceRefreshInfo = paramMediaSourceRefreshInfo.manifest;
        this.queue.setTimeline((Timeline)localObject2);
        this.playbackInfo = this.playbackInfo.copyWithTimeline((Timeline)localObject2, paramMediaSourceRefreshInfo);
        resolvePendingMessagePositions();
        int i;
        long l2;
        Object localObject1;
        if (localTimeline == null)
        {
          this.playbackInfoUpdate.incrementPendingOperationAcks(this.pendingPrepareCount);
          this.pendingPrepareCount = 0;
          if (this.pendingInitialSeekPosition != null)
          {
            paramMediaSourceRefreshInfo = resolveSeekPosition(this.pendingInitialSeekPosition, true);
            this.pendingInitialSeekPosition = null;
            if (paramMediaSourceRefreshInfo == null)
            {
              handleSourceInfoRefreshEndedPlayback();
              return;
            }
            i = ((Integer)paramMediaSourceRefreshInfo.first).intValue();
            l2 = ((Long)paramMediaSourceRefreshInfo.second).longValue();
            paramMediaSourceRefreshInfo = this.queue.resolveMediaPeriodIdForAds(i, l2);
            localObject1 = this.playbackInfo;
            if (paramMediaSourceRefreshInfo.isAd()) {}
            for (l1 = 0L;; l1 = l2)
            {
              this.playbackInfo = ((PlaybackInfo)localObject1).fromNewPosition(paramMediaSourceRefreshInfo, l1, l2);
              return;
            }
          }
          if (this.playbackInfo.startPositionUs == -9223372036854775807L)
          {
            if (((Timeline)localObject2).isEmpty())
            {
              handleSourceInfoRefreshEndedPlayback();
              return;
            }
            paramMediaSourceRefreshInfo = getPeriodPosition((Timeline)localObject2, ((Timeline)localObject2).getFirstWindowIndex(this.shuffleModeEnabled), -9223372036854775807L);
            i = ((Integer)paramMediaSourceRefreshInfo.first).intValue();
            l2 = ((Long)paramMediaSourceRefreshInfo.second).longValue();
            paramMediaSourceRefreshInfo = this.queue.resolveMediaPeriodIdForAds(i, l2);
            localObject1 = this.playbackInfo;
            if (paramMediaSourceRefreshInfo.isAd()) {}
            for (l1 = 0L;; l1 = l2)
            {
              this.playbackInfo = ((PlaybackInfo)localObject1).fromNewPosition(paramMediaSourceRefreshInfo, l1, l2);
              return;
            }
          }
        }
        else
        {
          int j = this.playbackInfo.periodId.periodIndex;
          localObject1 = this.queue.getFrontPeriod();
          if ((localObject1 != null) || (j < localTimeline.getPeriodCount()))
          {
            if (localObject1 == null) {}
            for (paramMediaSourceRefreshInfo = localTimeline.getPeriod(j, this.period, true).uid;; paramMediaSourceRefreshInfo = ((MediaPeriodHolder)localObject1).uid)
            {
              i = ((Timeline)localObject2).getIndexOfPeriod(paramMediaSourceRefreshInfo);
              if (i != -1) {
                break label579;
              }
              i = resolveSubsequentPeriod(j, localTimeline, (Timeline)localObject2);
              if (i != -1) {
                break;
              }
              handleSourceInfoRefreshEndedPlayback();
              return;
            }
            paramMediaSourceRefreshInfo = getPeriodPosition((Timeline)localObject2, ((Timeline)localObject2).getPeriod(i, this.period).windowIndex, -9223372036854775807L);
            i = ((Integer)paramMediaSourceRefreshInfo.first).intValue();
            l1 = ((Long)paramMediaSourceRefreshInfo.second).longValue();
            ((Timeline)localObject2).getPeriod(i, this.period, true);
            if (localObject1 != null)
            {
              localObject2 = this.period.uid;
              ((MediaPeriodHolder)localObject1).info = ((MediaPeriodHolder)localObject1).info.copyWithPeriodIndex(-1);
              paramMediaSourceRefreshInfo = (MediaSourceRefreshInfo)localObject1;
              while (paramMediaSourceRefreshInfo.next != null)
              {
                paramMediaSourceRefreshInfo = paramMediaSourceRefreshInfo.next;
                if (paramMediaSourceRefreshInfo.uid.equals(localObject2)) {
                  paramMediaSourceRefreshInfo.info = this.queue.getUpdatedMediaPeriodInfo(paramMediaSourceRefreshInfo.info, i);
                } else {
                  paramMediaSourceRefreshInfo.info = paramMediaSourceRefreshInfo.info.copyWithPeriodIndex(-1);
                }
              }
            }
            paramMediaSourceRefreshInfo = new MediaSource.MediaPeriodId(i);
            l1 = seekToPeriodPosition(paramMediaSourceRefreshInfo, l1);
            this.playbackInfo = this.playbackInfo.fromNewPosition(paramMediaSourceRefreshInfo, l1, -9223372036854775807L);
            return;
            if (i != j) {
              this.playbackInfo = this.playbackInfo.copyWithPeriodIndex(i);
            }
            if (this.playbackInfo.periodId.isAd())
            {
              paramMediaSourceRefreshInfo = this.queue.resolveMediaPeriodIdForAds(i, this.playbackInfo.contentPositionUs);
              if ((!paramMediaSourceRefreshInfo.isAd()) || (paramMediaSourceRefreshInfo.adIndexInAdGroup != this.playbackInfo.periodId.adIndexInAdGroup))
              {
                l2 = seekToPeriodPosition(paramMediaSourceRefreshInfo, this.playbackInfo.contentPositionUs);
                if (paramMediaSourceRefreshInfo.isAd()) {}
                for (l1 = this.playbackInfo.contentPositionUs;; l1 = -9223372036854775807L)
                {
                  this.playbackInfo = this.playbackInfo.fromNewPosition(paramMediaSourceRefreshInfo, l2, l1);
                  return;
                }
              }
            }
            if (localObject1 != null) {
              for (paramMediaSourceRefreshInfo = updatePeriodInfo((MediaPeriodHolder)localObject1, i); paramMediaSourceRefreshInfo.next != null; paramMediaSourceRefreshInfo = updatePeriodInfo((MediaPeriodHolder)localObject1, i))
              {
                localObject1 = paramMediaSourceRefreshInfo.next;
                i = ((Timeline)localObject2).getNextPeriodIndex(i, this.period, this.window, this.repeatMode, this.shuffleModeEnabled);
                if ((i == -1) || (!((MediaPeriodHolder)localObject1).uid.equals(((Timeline)localObject2).getPeriod(i, this.period, true).uid))) {
                  break label794;
                }
              }
            }
          }
        }
      }
    } while (!this.queue.removeAfter(paramMediaSourceRefreshInfo));
    paramMediaSourceRefreshInfo = this.queue.getPlayingPeriod().info.id;
    long l1 = seekToPeriodPosition(paramMediaSourceRefreshInfo, this.playbackInfo.positionUs, true);
    this.playbackInfo = this.playbackInfo.fromNewPosition(paramMediaSourceRefreshInfo, l1, this.playbackInfo.contentPositionUs);
  }
  
  private boolean isTimelineReady()
  {
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getPlayingPeriod();
    long l = localMediaPeriodHolder.info.durationUs;
    return (l == -9223372036854775807L) || (this.playbackInfo.positionUs < l) || ((localMediaPeriodHolder.next != null) && ((localMediaPeriodHolder.next.prepared) || (localMediaPeriodHolder.next.info.id.isAd())));
  }
  
  private void maybeContinueLoading()
  {
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getLoadingPeriod();
    long l1 = localMediaPeriodHolder.getNextLoadPositionUs();
    if (l1 == Long.MIN_VALUE) {
      setIsLoading(false);
    }
    boolean bool;
    do
    {
      return;
      long l2 = localMediaPeriodHolder.toPeriodTime(this.rendererPositionUs);
      bool = this.loadControl.shouldContinueLoading(l1 - l2, this.mediaClock.getPlaybackParameters().speed);
      setIsLoading(bool);
    } while (!bool);
    localMediaPeriodHolder.continueLoading(this.rendererPositionUs);
  }
  
  private void maybeNotifyPlaybackInfoChanged()
  {
    Handler localHandler;
    int j;
    if (this.playbackInfoUpdate.hasPendingUpdate(this.playbackInfo))
    {
      localHandler = this.eventHandler;
      j = this.playbackInfoUpdate.operationAcks;
      if (!this.playbackInfoUpdate.positionDiscontinuity) {
        break label71;
      }
    }
    label71:
    for (int i = this.playbackInfoUpdate.discontinuityReason;; i = -1)
    {
      localHandler.obtainMessage(0, j, i, this.playbackInfo).sendToTarget();
      this.playbackInfoUpdate.reset(this.playbackInfo);
      return;
    }
  }
  
  private void maybeThrowPeriodPrepareError()
    throws IOException
  {
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getLoadingPeriod();
    Object localObject = this.queue.getReadingPeriod();
    int j;
    int i;
    if ((localMediaPeriodHolder != null) && (!localMediaPeriodHolder.prepared) && ((localObject == null) || (((MediaPeriodHolder)localObject).next == localMediaPeriodHolder)))
    {
      localObject = this.enabledRenderers;
      j = localObject.length;
      i = 0;
    }
    while (i < j)
    {
      if (!localObject[i].hasReadStreamToEnd()) {
        return;
      }
      i += 1;
    }
    localMediaPeriodHolder.mediaPeriod.maybeThrowPrepareError();
  }
  
  private void maybeTriggerPendingMessages(long paramLong1, long paramLong2)
  {
    if ((this.pendingMessages.isEmpty()) || (this.playbackInfo.periodId.isAd())) {
      return;
    }
    long l = paramLong1;
    if (this.playbackInfo.startPositionUs == paramLong1) {
      l = paramLong1 - 1L;
    }
    int i = this.playbackInfo.periodId.periodIndex;
    if (this.nextPendingMessageIndex > 0)
    {
      localPendingMessageInfo1 = (PendingMessageInfo)this.pendingMessages.get(this.nextPendingMessageIndex - 1);
      if ((localPendingMessageInfo1 == null) || ((localPendingMessageInfo1.resolvedPeriodIndex <= i) && ((localPendingMessageInfo1.resolvedPeriodIndex != i) || (localPendingMessageInfo1.resolvedPeriodTimeUs <= l)))) {
        break label167;
      }
      this.nextPendingMessageIndex -= 1;
      if (this.nextPendingMessageIndex <= 0) {
        break label161;
      }
    }
    label161:
    for (PendingMessageInfo localPendingMessageInfo1 = (PendingMessageInfo)this.pendingMessages.get(this.nextPendingMessageIndex - 1);; localPendingMessageInfo1 = null)
    {
      break;
      localPendingMessageInfo1 = null;
      break;
    }
    label167:
    PendingMessageInfo localPendingMessageInfo2;
    if (this.nextPendingMessageIndex < this.pendingMessages.size())
    {
      localPendingMessageInfo1 = (PendingMessageInfo)this.pendingMessages.get(this.nextPendingMessageIndex);
      localPendingMessageInfo2 = localPendingMessageInfo1;
      if (localPendingMessageInfo1 == null) {
        break label312;
      }
      localPendingMessageInfo2 = localPendingMessageInfo1;
      if (localPendingMessageInfo1.resolvedPeriodUid == null) {
        break label312;
      }
      if (localPendingMessageInfo1.resolvedPeriodIndex >= i)
      {
        localPendingMessageInfo2 = localPendingMessageInfo1;
        if (localPendingMessageInfo1.resolvedPeriodIndex != i) {
          break label312;
        }
        localPendingMessageInfo2 = localPendingMessageInfo1;
        if (localPendingMessageInfo1.resolvedPeriodTimeUs > l) {
          break label312;
        }
      }
      this.nextPendingMessageIndex += 1;
      if (this.nextPendingMessageIndex >= this.pendingMessages.size()) {
        break label306;
      }
    }
    label306:
    for (localPendingMessageInfo1 = (PendingMessageInfo)this.pendingMessages.get(this.nextPendingMessageIndex);; localPendingMessageInfo1 = null)
    {
      break;
      localPendingMessageInfo1 = null;
      break;
    }
    label312:
    if ((localPendingMessageInfo2 != null) && (localPendingMessageInfo2.resolvedPeriodUid != null) && (localPendingMessageInfo2.resolvedPeriodIndex == i) && (localPendingMessageInfo2.resolvedPeriodTimeUs > l) && (localPendingMessageInfo2.resolvedPeriodTimeUs <= paramLong2))
    {
      sendMessageToTarget(localPendingMessageInfo2.message);
      if (!localPendingMessageInfo2.message.getDeleteAfterDelivery()) {
        break label425;
      }
      this.pendingMessages.remove(this.nextPendingMessageIndex);
      label388:
      if (this.nextPendingMessageIndex >= this.pendingMessages.size()) {
        break label438;
      }
    }
    label425:
    label438:
    for (localPendingMessageInfo1 = (PendingMessageInfo)this.pendingMessages.get(this.nextPendingMessageIndex);; localPendingMessageInfo1 = null)
    {
      localPendingMessageInfo2 = localPendingMessageInfo1;
      break label312;
      break;
      this.nextPendingMessageIndex += 1;
      break label388;
    }
  }
  
  private void maybeUpdateLoadingPeriod()
    throws IOException
  {
    this.queue.reevaluateBuffer(this.rendererPositionUs);
    MediaPeriodInfo localMediaPeriodInfo;
    if (this.queue.shouldLoadNextMediaPeriod())
    {
      localMediaPeriodInfo = this.queue.getNextMediaPeriodInfo(this.rendererPositionUs, this.playbackInfo);
      if (localMediaPeriodInfo == null) {
        this.mediaSource.maybeThrowSourceInfoRefreshError();
      }
    }
    else
    {
      return;
    }
    Object localObject = this.playbackInfo.timeline.getPeriod(localMediaPeriodInfo.id.periodIndex, this.period, true).uid;
    this.queue.enqueueNextMediaPeriod(this.rendererCapabilities, 60000000L, this.trackSelector, this.loadControl.getAllocator(), this.mediaSource, localObject, localMediaPeriodInfo).prepare(this, localMediaPeriodInfo.startPositionUs);
    setIsLoading(true);
  }
  
  private void prepareInternal(MediaSource paramMediaSource, boolean paramBoolean)
  {
    this.pendingPrepareCount += 1;
    resetInternal(true, paramBoolean, true);
    this.loadControl.onPrepared();
    this.mediaSource = paramMediaSource;
    setState(2);
    paramMediaSource.prepareSource(this.player, true, this);
    this.handler.sendEmptyMessage(2);
  }
  
  private void releaseInternal()
  {
    resetInternal(true, true, true);
    this.loadControl.onReleased();
    setState(1);
    this.internalPlaybackThread.quit();
    try
    {
      this.released = true;
      notifyAll();
      return;
    }
    finally {}
  }
  
  private boolean rendererWaitingForNextStream(Renderer paramRenderer)
  {
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getReadingPeriod();
    return (localMediaPeriodHolder.next != null) && (localMediaPeriodHolder.next.prepared) && (paramRenderer.hasReadStreamToEnd());
  }
  
  private void reselectTracksInternal()
    throws ExoPlaybackException
  {
    if (!this.queue.hasPlayingPeriod()) {}
    for (;;)
    {
      return;
      float f = this.mediaClock.getPlaybackParameters().speed;
      MediaPeriodHolder localMediaPeriodHolder = this.queue.getPlayingPeriod();
      Object localObject = this.queue.getReadingPeriod();
      int i = 1;
      label42:
      boolean bool;
      int j;
      boolean[] arrayOfBoolean;
      label202:
      Renderer localRenderer;
      label232:
      int k;
      if ((localMediaPeriodHolder != null) && (localMediaPeriodHolder.prepared)) {
        if (localMediaPeriodHolder.selectTracks(f))
        {
          if (i == 0) {
            break label395;
          }
          localMediaPeriodHolder = this.queue.getPlayingPeriod();
          bool = this.queue.removeAfter(localMediaPeriodHolder);
          localObject = new boolean[this.renderers.length];
          long l = localMediaPeriodHolder.applyTrackSelection(this.playbackInfo.positionUs, bool, (boolean[])localObject);
          updateLoadControlTrackSelection(localMediaPeriodHolder.trackSelectorResult);
          if ((this.playbackInfo.playbackState != 4) && (l != this.playbackInfo.positionUs))
          {
            this.playbackInfo = this.playbackInfo.fromNewPosition(this.playbackInfo.periodId, l, this.playbackInfo.contentPositionUs);
            this.playbackInfoUpdate.setPositionDiscontinuity(4);
            resetRendererPosition(l);
          }
          j = 0;
          arrayOfBoolean = new boolean[this.renderers.length];
          i = 0;
          if (i >= this.renderers.length) {
            break label341;
          }
          localRenderer = this.renderers[i];
          if (localRenderer.getState() == 0) {
            break label314;
          }
          bool = true;
          arrayOfBoolean[i] = bool;
          SampleStream localSampleStream = localMediaPeriodHolder.sampleStreams[i];
          k = j;
          if (localSampleStream != null) {
            k = j + 1;
          }
          if (arrayOfBoolean[i] != 0)
          {
            if (localSampleStream == localRenderer.getStream()) {
              break label320;
            }
            disableRenderer(localRenderer);
          }
        }
      }
      for (;;)
      {
        i += 1;
        j = k;
        break label202;
        if (localMediaPeriodHolder == localObject) {
          i = 0;
        }
        localMediaPeriodHolder = localMediaPeriodHolder.next;
        break label42;
        break;
        label314:
        bool = false;
        break label232;
        label320:
        if (localObject[i] != 0) {
          localRenderer.resetPosition(this.rendererPositionUs);
        }
      }
      label341:
      this.playbackInfo = this.playbackInfo.copyWithTrackSelectorResult(localMediaPeriodHolder.trackSelectorResult);
      enableRenderers(arrayOfBoolean, j);
      while (this.playbackInfo.playbackState != 4)
      {
        maybeContinueLoading();
        updatePlaybackPositions();
        this.handler.sendEmptyMessage(2);
        return;
        label395:
        this.queue.removeAfter(localMediaPeriodHolder);
        if (localMediaPeriodHolder.prepared)
        {
          localMediaPeriodHolder.applyTrackSelection(Math.max(localMediaPeriodHolder.info.startPositionUs, localMediaPeriodHolder.toPeriodTime(this.rendererPositionUs)), false);
          updateLoadControlTrackSelection(localMediaPeriodHolder.trackSelectorResult);
        }
      }
    }
  }
  
  private void resetInternal(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    this.handler.removeMessages(2);
    this.rebuffering = false;
    this.mediaClock.stop();
    this.rendererPositionUs = 60000000L;
    Object localObject2 = this.enabledRenderers;
    int j = localObject2.length;
    int i = 0;
    for (;;)
    {
      Renderer localRenderer;
      if (i < j) {
        localRenderer = localObject2[i];
      }
      try
      {
        disableRenderer(localRenderer);
        i += 1;
      }
      catch (ExoPlaybackException localExoPlaybackException)
      {
        for (;;)
        {
          Log.e("ExoPlayerImplInternal", "Stop failed.", localExoPlaybackException);
        }
        this.enabledRenderers = new Renderer[0];
        this.queue.clear();
        setIsLoading(false);
        if (paramBoolean2) {
          this.pendingInitialSeekPosition = null;
        }
        Object localObject1;
        if (paramBoolean3)
        {
          this.queue.setTimeline(null);
          localObject1 = this.pendingMessages.iterator();
          while (((Iterator)localObject1).hasNext()) {
            ((PendingMessageInfo)((Iterator)localObject1).next()).message.markAsProcessed(false);
          }
          this.pendingMessages.clear();
          this.nextPendingMessageIndex = 0;
        }
        MediaSource.MediaPeriodId localMediaPeriodId;
        long l1;
        long l2;
        if (paramBoolean3)
        {
          localObject1 = null;
          if (!paramBoolean3) {
            break label312;
          }
          localObject2 = null;
          if (!paramBoolean2) {
            break label324;
          }
          localMediaPeriodId = new MediaSource.MediaPeriodId(getFirstPeriodIndex());
          if (!paramBoolean2) {
            break label336;
          }
          l1 = -9223372036854775807L;
          if (!paramBoolean2) {
            break label348;
          }
          l2 = -9223372036854775807L;
          i = this.playbackInfo.playbackState;
          if (!paramBoolean3) {
            break label360;
          }
        }
        for (TrackSelectorResult localTrackSelectorResult = this.emptyTrackSelectorResult;; localTrackSelectorResult = this.playbackInfo.trackSelectorResult)
        {
          this.playbackInfo = new PlaybackInfo((Timeline)localObject1, localObject2, localMediaPeriodId, l1, l2, i, false, localTrackSelectorResult);
          if ((paramBoolean1) && (this.mediaSource != null))
          {
            this.mediaSource.releaseSource();
            this.mediaSource = null;
          }
          return;
          localObject1 = this.playbackInfo.timeline;
          break;
          localObject2 = this.playbackInfo.manifest;
          break label194;
          localMediaPeriodId = this.playbackInfo.periodId;
          break label211;
          l1 = this.playbackInfo.startPositionUs;
          break label220;
          l2 = this.playbackInfo.contentPositionUs;
          break label229;
        }
      }
      catch (RuntimeException localRuntimeException)
      {
        label194:
        label211:
        label220:
        label229:
        label312:
        label324:
        label336:
        label348:
        label360:
        for (;;) {}
      }
    }
  }
  
  private void resetRendererPosition(long paramLong)
    throws ExoPlaybackException
  {
    if (!this.queue.hasPlayingPeriod()) {}
    for (paramLong = 60000000L + paramLong;; paramLong = this.queue.getPlayingPeriod().toRendererTime(paramLong))
    {
      this.rendererPositionUs = paramLong;
      this.mediaClock.resetPosition(this.rendererPositionUs);
      Renderer[] arrayOfRenderer = this.enabledRenderers;
      int j = arrayOfRenderer.length;
      int i = 0;
      while (i < j)
      {
        arrayOfRenderer[i].resetPosition(this.rendererPositionUs);
        i += 1;
      }
    }
  }
  
  private boolean resolvePendingMessagePosition(PendingMessageInfo paramPendingMessageInfo)
  {
    if (paramPendingMessageInfo.resolvedPeriodUid == null)
    {
      Pair localPair = resolveSeekPosition(new SeekPosition(paramPendingMessageInfo.message.getTimeline(), paramPendingMessageInfo.message.getWindowIndex(), C.msToUs(paramPendingMessageInfo.message.getPositionMs())), false);
      if (localPair == null) {
        return false;
      }
      paramPendingMessageInfo.setResolvedPosition(((Integer)localPair.first).intValue(), ((Long)localPair.second).longValue(), this.playbackInfo.timeline.getPeriod(((Integer)localPair.first).intValue(), this.period, true).uid);
    }
    for (;;)
    {
      return true;
      int i = this.playbackInfo.timeline.getIndexOfPeriod(paramPendingMessageInfo.resolvedPeriodUid);
      if (i == -1) {
        break;
      }
      paramPendingMessageInfo.resolvedPeriodIndex = i;
    }
  }
  
  private void resolvePendingMessagePositions()
  {
    int i = this.pendingMessages.size() - 1;
    while (i >= 0)
    {
      if (!resolvePendingMessagePosition((PendingMessageInfo)this.pendingMessages.get(i)))
      {
        ((PendingMessageInfo)this.pendingMessages.get(i)).message.markAsProcessed(false);
        this.pendingMessages.remove(i);
      }
      i -= 1;
    }
    Collections.sort(this.pendingMessages);
  }
  
  private Pair<Integer, Long> resolveSeekPosition(SeekPosition paramSeekPosition, boolean paramBoolean)
  {
    Timeline localTimeline = this.playbackInfo.timeline;
    Object localObject2 = paramSeekPosition.timeline;
    if (localTimeline == null) {
      paramSeekPosition = null;
    }
    int i;
    for (;;)
    {
      return paramSeekPosition;
      Object localObject1 = localObject2;
      if (((Timeline)localObject2).isEmpty()) {
        localObject1 = localTimeline;
      }
      try
      {
        localObject2 = ((Timeline)localObject1).getPeriodPosition(this.window, this.period, paramSeekPosition.windowIndex, paramSeekPosition.windowPositionUs);
        paramSeekPosition = (SeekPosition)localObject2;
        if (localTimeline != localObject1)
        {
          i = localTimeline.getIndexOfPeriod(((Timeline)localObject1).getPeriod(((Integer)((Pair)localObject2).first).intValue(), this.period, true).uid);
          if (i != -1) {
            return Pair.create(Integer.valueOf(i), ((Pair)localObject2).second);
          }
        }
      }
      catch (IndexOutOfBoundsException localIndexOutOfBoundsException)
      {
        throw new IllegalSeekPositionException(localTimeline, paramSeekPosition.windowIndex, paramSeekPosition.windowPositionUs);
      }
    }
    if (paramBoolean)
    {
      i = resolveSubsequentPeriod(((Integer)((Pair)localObject2).first).intValue(), localIndexOutOfBoundsException, localTimeline);
      if (i != -1) {
        return getPeriodPosition(localTimeline, localTimeline.getPeriod(i, this.period).windowIndex, -9223372036854775807L);
      }
    }
    return null;
  }
  
  private int resolveSubsequentPeriod(int paramInt, Timeline paramTimeline1, Timeline paramTimeline2)
  {
    int i = -1;
    int m = paramTimeline1.getPeriodCount();
    int k = 0;
    int j = paramInt;
    paramInt = k;
    for (;;)
    {
      if ((paramInt < m) && (i == -1))
      {
        j = paramTimeline1.getNextPeriodIndex(j, this.period, this.window, this.repeatMode, this.shuffleModeEnabled);
        if (j != -1) {}
      }
      else
      {
        return i;
      }
      i = paramTimeline2.getIndexOfPeriod(paramTimeline1.getPeriod(j, this.period, true).uid);
      paramInt += 1;
    }
  }
  
  private void scheduleNextWork(long paramLong1, long paramLong2)
  {
    this.handler.removeMessages(2);
    this.handler.sendEmptyMessageAtTime(2, paramLong1 + paramLong2);
  }
  
  private void seekToInternal(SeekPosition paramSeekPosition)
    throws ExoPlaybackException
  {
    Timeline localTimeline = this.playbackInfo.timeline;
    this.playbackInfoUpdate.incrementPendingOperationAcks(1);
    Pair localPair = resolveSeekPosition(paramSeekPosition, true);
    MediaSource.MediaPeriodId localMediaPeriodId;
    long l1;
    long l4;
    int i;
    if (localPair == null)
    {
      localMediaPeriodId = new MediaSource.MediaPeriodId(getFirstPeriodIndex());
      l1 = -9223372036854775807L;
      l4 = -9223372036854775807L;
      i = 1;
    }
    label245:
    long l5;
    do
    {
      try
      {
        if ((this.mediaSource == null) || (localTimeline == null)) {
          this.pendingInitialSeekPosition = paramSeekPosition;
        }
        for (;;)
        {
          return;
          i = ((Integer)localPair.first).intValue();
          l4 = ((Long)localPair.second).longValue();
          localMediaPeriodId = this.queue.resolveMediaPeriodIdForAds(i, l4);
          if (localMediaPeriodId.isAd())
          {
            l1 = 0L;
            i = 1;
            break;
          }
          l1 = ((Long)localPair.second).longValue();
          if (paramSeekPosition.windowPositionUs == -9223372036854775807L) {}
          for (i = 1;; i = 0) {
            break;
          }
          if (l1 != -9223372036854775807L) {
            break label245;
          }
          setState(4);
          resetInternal(false, true, false);
        }
        l2 = l1;
      }
      finally
      {
        this.playbackInfo = this.playbackInfo.fromNewPosition(localMediaPeriodId, l1, l4);
        if (i != 0) {
          this.playbackInfoUpdate.setPositionDiscontinuity(2);
        }
      }
      l5 = l2;
      if (!localMediaPeriodId.equals(this.playbackInfo.periodId)) {
        break;
      }
      paramSeekPosition = this.queue.getPlayingPeriod();
      long l3 = l2;
      if (paramSeekPosition != null)
      {
        l3 = l2;
        if (l2 != 0L) {
          l3 = paramSeekPosition.mediaPeriod.getAdjustedSeekPositionUs(l2, this.seekParameters);
        }
      }
      l5 = l3;
      if (C.usToMs(l3) != C.usToMs(this.playbackInfo.positionUs)) {
        break;
      }
      l2 = this.playbackInfo.positionUs;
      this.playbackInfo = this.playbackInfo.fromNewPosition(localMediaPeriodId, l2, l4);
    } while (i == 0);
    this.playbackInfoUpdate.setPositionDiscontinuity(2);
    return;
    long l2 = seekToPeriodPosition(localMediaPeriodId, l5);
    if (l1 != l2) {}
    for (int j = 1;; j = 0)
    {
      i |= j;
      l1 = l2;
      break;
    }
  }
  
  private long seekToPeriodPosition(MediaSource.MediaPeriodId paramMediaPeriodId, long paramLong)
    throws ExoPlaybackException
  {
    if (this.queue.getPlayingPeriod() != this.queue.getReadingPeriod()) {}
    for (boolean bool = true;; bool = false) {
      return seekToPeriodPosition(paramMediaPeriodId, paramLong, bool);
    }
  }
  
  private long seekToPeriodPosition(MediaSource.MediaPeriodId paramMediaPeriodId, long paramLong, boolean paramBoolean)
    throws ExoPlaybackException
  {
    stopRenderers();
    this.rebuffering = false;
    setState(2);
    MediaPeriodHolder localMediaPeriodHolder2 = this.queue.getPlayingPeriod();
    for (MediaPeriodHolder localMediaPeriodHolder1 = localMediaPeriodHolder2;; localMediaPeriodHolder1 = this.queue.advancePlayingPeriod()) {
      if (localMediaPeriodHolder1 != null)
      {
        if (shouldKeepPeriodHolder(paramMediaPeriodId, paramLong, localMediaPeriodHolder1)) {
          this.queue.removeAfter(localMediaPeriodHolder1);
        }
      }
      else
      {
        if ((localMediaPeriodHolder2 == localMediaPeriodHolder1) && (!paramBoolean)) {
          break label124;
        }
        paramMediaPeriodId = this.enabledRenderers;
        int j = paramMediaPeriodId.length;
        int i = 0;
        while (i < j)
        {
          disableRenderer(paramMediaPeriodId[i]);
          i += 1;
        }
      }
    }
    this.enabledRenderers = new Renderer[0];
    localMediaPeriodHolder2 = null;
    label124:
    if (localMediaPeriodHolder1 != null)
    {
      updatePlayingPeriodRenderers(localMediaPeriodHolder2);
      long l = paramLong;
      if (localMediaPeriodHolder1.hasEnabledTracks)
      {
        l = localMediaPeriodHolder1.mediaPeriod.seekToUs(paramLong);
        localMediaPeriodHolder1.mediaPeriod.discardBuffer(l - this.backBufferDurationUs, this.retainBackBufferFromKeyframe);
      }
      resetRendererPosition(l);
      maybeContinueLoading();
      paramLong = l;
    }
    for (;;)
    {
      this.handler.sendEmptyMessage(2);
      return paramLong;
      this.queue.clear();
      resetRendererPosition(paramLong);
    }
  }
  
  private void sendMessageInternal(PlayerMessage paramPlayerMessage)
  {
    if (paramPlayerMessage.getPositionMs() == -9223372036854775807L)
    {
      sendMessageToTarget(paramPlayerMessage);
      return;
    }
    if (this.playbackInfo.timeline == null)
    {
      this.pendingMessages.add(new PendingMessageInfo(paramPlayerMessage));
      return;
    }
    PendingMessageInfo localPendingMessageInfo = new PendingMessageInfo(paramPlayerMessage);
    if (resolvePendingMessagePosition(localPendingMessageInfo))
    {
      this.pendingMessages.add(localPendingMessageInfo);
      Collections.sort(this.pendingMessages);
      return;
    }
    paramPlayerMessage.markAsProcessed(false);
  }
  
  private void sendMessageToTarget(PlayerMessage paramPlayerMessage)
  {
    if (paramPlayerMessage.getHandler().getLooper() == this.handler.getLooper())
    {
      deliverMessage(paramPlayerMessage);
      if ((this.playbackInfo.playbackState == 3) || (this.playbackInfo.playbackState == 2)) {
        this.handler.sendEmptyMessage(2);
      }
      return;
    }
    this.handler.obtainMessage(15, paramPlayerMessage).sendToTarget();
  }
  
  private void sendMessageToTargetThread(final PlayerMessage paramPlayerMessage)
  {
    paramPlayerMessage.getHandler().post(new Runnable()
    {
      public void run()
      {
        ExoPlayerImplInternal.this.deliverMessage(paramPlayerMessage);
      }
    });
  }
  
  private void setIsLoading(boolean paramBoolean)
  {
    if (this.playbackInfo.isLoading != paramBoolean) {
      this.playbackInfo = this.playbackInfo.copyWithIsLoading(paramBoolean);
    }
  }
  
  private void setPlayWhenReadyInternal(boolean paramBoolean)
    throws ExoPlaybackException
  {
    this.rebuffering = false;
    this.playWhenReady = paramBoolean;
    if (!paramBoolean)
    {
      stopRenderers();
      updatePlaybackPositions();
    }
    do
    {
      return;
      if (this.playbackInfo.playbackState == 3)
      {
        startRenderers();
        this.handler.sendEmptyMessage(2);
        return;
      }
    } while (this.playbackInfo.playbackState != 2);
    this.handler.sendEmptyMessage(2);
  }
  
  private void setPlaybackParametersInternal(PlaybackParameters paramPlaybackParameters)
  {
    this.mediaClock.setPlaybackParameters(paramPlaybackParameters);
  }
  
  private void setRepeatModeInternal(int paramInt)
    throws ExoPlaybackException
  {
    this.repeatMode = paramInt;
    this.queue.setRepeatMode(paramInt);
    validateExistingPeriodHolders();
  }
  
  private void setSeekParametersInternal(SeekParameters paramSeekParameters)
  {
    this.seekParameters = paramSeekParameters;
  }
  
  private void setShuffleModeEnabledInternal(boolean paramBoolean)
    throws ExoPlaybackException
  {
    this.shuffleModeEnabled = paramBoolean;
    this.queue.setShuffleModeEnabled(paramBoolean);
    validateExistingPeriodHolders();
  }
  
  private void setState(int paramInt)
  {
    if (this.playbackInfo.playbackState != paramInt) {
      this.playbackInfo = this.playbackInfo.copyWithPlaybackState(paramInt);
    }
  }
  
  private boolean shouldKeepPeriodHolder(MediaSource.MediaPeriodId paramMediaPeriodId, long paramLong, MediaPeriodHolder paramMediaPeriodHolder)
  {
    if ((paramMediaPeriodId.equals(paramMediaPeriodHolder.info.id)) && (paramMediaPeriodHolder.prepared))
    {
      this.playbackInfo.timeline.getPeriod(paramMediaPeriodHolder.info.id.periodIndex, this.period);
      int i = this.period.getAdGroupIndexAfterPositionUs(paramLong);
      if ((i == -1) || (this.period.getAdGroupTimeUs(i) == paramMediaPeriodHolder.info.endPositionUs)) {
        return true;
      }
    }
    return false;
  }
  
  private boolean shouldTransitionToReadyState(boolean paramBoolean)
  {
    boolean bool2 = false;
    boolean bool1;
    if (this.enabledRenderers.length == 0) {
      bool1 = isTimelineReady();
    }
    do
    {
      return bool1;
      bool1 = bool2;
    } while (!paramBoolean);
    if (!this.playbackInfo.isLoading) {
      return true;
    }
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getLoadingPeriod();
    if (!localMediaPeriodHolder.info.isFinal) {}
    for (paramBoolean = true;; paramBoolean = false)
    {
      long l = localMediaPeriodHolder.getBufferedPositionUs(paramBoolean);
      if (l != Long.MIN_VALUE)
      {
        bool1 = bool2;
        if (!this.loadControl.shouldStartPlayback(l - localMediaPeriodHolder.toPeriodTime(this.rendererPositionUs), this.mediaClock.getPlaybackParameters().speed, this.rebuffering)) {
          break;
        }
      }
      return true;
    }
  }
  
  private void startRenderers()
    throws ExoPlaybackException
  {
    int i = 0;
    this.rebuffering = false;
    this.mediaClock.start();
    Renderer[] arrayOfRenderer = this.enabledRenderers;
    int j = arrayOfRenderer.length;
    while (i < j)
    {
      arrayOfRenderer[i].start();
      i += 1;
    }
  }
  
  private void stopInternal(boolean paramBoolean1, boolean paramBoolean2)
  {
    resetInternal(true, paramBoolean1, paramBoolean1);
    PlaybackInfoUpdate localPlaybackInfoUpdate = this.playbackInfoUpdate;
    int j = this.pendingPrepareCount;
    if (paramBoolean2) {}
    for (int i = 1;; i = 0)
    {
      localPlaybackInfoUpdate.incrementPendingOperationAcks(i + j);
      this.pendingPrepareCount = 0;
      this.loadControl.onStopped();
      setState(1);
      return;
    }
  }
  
  private void stopRenderers()
    throws ExoPlaybackException
  {
    this.mediaClock.stop();
    Renderer[] arrayOfRenderer = this.enabledRenderers;
    int j = arrayOfRenderer.length;
    int i = 0;
    while (i < j)
    {
      ensureStopped(arrayOfRenderer[i]);
      i += 1;
    }
  }
  
  private void updateLoadControlTrackSelection(TrackSelectorResult paramTrackSelectorResult)
  {
    this.loadControl.onTracksSelected(this.renderers, paramTrackSelectorResult.groups, paramTrackSelectorResult.selections);
  }
  
  private MediaPeriodHolder updatePeriodInfo(MediaPeriodHolder paramMediaPeriodHolder, int paramInt)
  {
    for (;;)
    {
      paramMediaPeriodHolder.info = this.queue.getUpdatedMediaPeriodInfo(paramMediaPeriodHolder.info, paramInt);
      if ((paramMediaPeriodHolder.info.isLastInTimelinePeriod) || (paramMediaPeriodHolder.next == null)) {
        return paramMediaPeriodHolder;
      }
      paramMediaPeriodHolder = paramMediaPeriodHolder.next;
    }
  }
  
  private void updatePeriods()
    throws ExoPlaybackException, IOException
  {
    if (this.mediaSource == null) {}
    label59:
    label89:
    label226:
    label228:
    label233:
    do
    {
      for (;;)
      {
        return;
        if (this.playbackInfo.timeline == null)
        {
          this.mediaSource.maybeThrowSourceInfoRefreshError();
          return;
        }
        maybeUpdateLoadingPeriod();
        localObject1 = this.queue.getLoadingPeriod();
        if ((localObject1 == null) || (((MediaPeriodHolder)localObject1).isFullyBuffered()))
        {
          setIsLoading(false);
          if (!this.queue.hasPlayingPeriod()) {
            break label226;
          }
          localObject1 = this.queue.getPlayingPeriod();
          localObject3 = this.queue.getReadingPeriod();
          i = 0;
          if ((!this.playWhenReady) || (localObject1 == localObject3) || (this.rendererPositionUs < ((MediaPeriodHolder)localObject1).next.rendererPositionOffsetUs)) {
            break label233;
          }
          if (i != 0) {
            maybeNotifyPlaybackInfoChanged();
          }
          if (!((MediaPeriodHolder)localObject1).info.isLastInTimelinePeriod) {
            break label228;
          }
        }
        for (i = 0;; i = 3)
        {
          localObject2 = this.queue.advancePlayingPeriod();
          updatePlayingPeriodRenderers((MediaPeriodHolder)localObject1);
          this.playbackInfo = this.playbackInfo.fromNewPosition(((MediaPeriodHolder)localObject2).info.id, ((MediaPeriodHolder)localObject2).info.startPositionUs, ((MediaPeriodHolder)localObject2).info.contentPositionUs);
          this.playbackInfoUpdate.setPositionDiscontinuity(i);
          updatePlaybackPositions();
          i = 1;
          localObject1 = localObject2;
          break label89;
          if (this.playbackInfo.isLoading) {
            break label59;
          }
          maybeContinueLoading();
          break label59;
          break;
        }
        if (!((MediaPeriodHolder)localObject3).info.isFinal) {
          break;
        }
        i = 0;
        while (i < this.renderers.length)
        {
          localObject1 = this.renderers[i];
          localObject2 = localObject3.sampleStreams[i];
          if ((localObject2 != null) && (((Renderer)localObject1).getStream() == localObject2) && (((Renderer)localObject1).hasReadStreamToEnd())) {
            ((Renderer)localObject1).setCurrentStreamFinal();
          }
          i += 1;
        }
      }
    } while ((((MediaPeriodHolder)localObject3).next == null) || (!((MediaPeriodHolder)localObject3).next.prepared));
    int i = 0;
    for (;;)
    {
      if (i >= this.renderers.length) {
        break label394;
      }
      localObject1 = this.renderers[i];
      localObject2 = localObject3.sampleStreams[i];
      if ((((Renderer)localObject1).getStream() != localObject2) || ((localObject2 != null) && (!((Renderer)localObject1).hasReadStreamToEnd()))) {
        break;
      }
      i += 1;
    }
    label394:
    Object localObject1 = ((MediaPeriodHolder)localObject3).trackSelectorResult;
    Object localObject2 = this.queue.advanceReadingPeriod();
    Object localObject3 = ((MediaPeriodHolder)localObject2).trackSelectorResult;
    label436:
    int j;
    label438:
    Renderer localRenderer;
    if (((MediaPeriodHolder)localObject2).mediaPeriod.readDiscontinuity() != -9223372036854775807L)
    {
      i = 1;
      j = 0;
      if (j < this.renderers.length)
      {
        localRenderer = this.renderers[j];
        if (localObject1.renderersEnabled[j] != 0) {
          break label477;
        }
      }
    }
    for (;;)
    {
      j += 1;
      break label438;
      break;
      i = 0;
      break label436;
      label477:
      if (i != 0)
      {
        localRenderer.setCurrentStreamFinal();
      }
      else if (!localRenderer.isCurrentStreamFinal())
      {
        TrackSelection localTrackSelection = ((TrackSelectorResult)localObject3).selections.get(j);
        int m = localObject3.renderersEnabled[j];
        if (this.rendererCapabilities[j].getTrackType() == 5) {}
        for (int k = 1;; k = 0)
        {
          RendererConfiguration localRendererConfiguration1 = localObject1.rendererConfigurations[j];
          RendererConfiguration localRendererConfiguration2 = localObject3.rendererConfigurations[j];
          if ((m == 0) || (!localRendererConfiguration2.equals(localRendererConfiguration1)) || (k != 0)) {
            break label607;
          }
          localRenderer.replaceStream(getFormats(localTrackSelection), localObject2.sampleStreams[j], ((MediaPeriodHolder)localObject2).getRendererOffset());
          break;
        }
        label607:
        localRenderer.setCurrentStreamFinal();
      }
    }
  }
  
  private void updatePlaybackPositions()
    throws ExoPlaybackException
  {
    if (!this.queue.hasPlayingPeriod()) {
      return;
    }
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getPlayingPeriod();
    long l = localMediaPeriodHolder.mediaPeriod.readDiscontinuity();
    PlaybackInfo localPlaybackInfo;
    if (l != -9223372036854775807L)
    {
      resetRendererPosition(l);
      if (l != this.playbackInfo.positionUs)
      {
        this.playbackInfo = this.playbackInfo.fromNewPosition(this.playbackInfo.periodId, l, this.playbackInfo.contentPositionUs);
        this.playbackInfoUpdate.setPositionDiscontinuity(4);
      }
      localPlaybackInfo = this.playbackInfo;
      if (this.enabledRenderers.length != 0) {
        break label160;
      }
    }
    label160:
    for (l = localMediaPeriodHolder.info.durationUs;; l = localMediaPeriodHolder.getBufferedPositionUs(true))
    {
      localPlaybackInfo.bufferedPositionUs = l;
      return;
      this.rendererPositionUs = this.mediaClock.syncAndGetPositionUs();
      l = localMediaPeriodHolder.toPeriodTime(this.rendererPositionUs);
      maybeTriggerPendingMessages(this.playbackInfo.positionUs, l);
      this.playbackInfo.positionUs = l;
      break;
    }
  }
  
  private void updatePlayingPeriodRenderers(MediaPeriodHolder paramMediaPeriodHolder)
    throws ExoPlaybackException
  {
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getPlayingPeriod();
    if ((localMediaPeriodHolder == null) || (paramMediaPeriodHolder == localMediaPeriodHolder)) {
      return;
    }
    int j = 0;
    boolean[] arrayOfBoolean = new boolean[this.renderers.length];
    int i = 0;
    if (i < this.renderers.length)
    {
      Renderer localRenderer = this.renderers[i];
      if (localRenderer.getState() != 0) {}
      for (int m = 1;; m = 0)
      {
        arrayOfBoolean[i] = m;
        int k = j;
        if (localMediaPeriodHolder.trackSelectorResult.renderersEnabled[i] != 0) {
          k = j + 1;
        }
        if ((arrayOfBoolean[i] != 0) && ((localMediaPeriodHolder.trackSelectorResult.renderersEnabled[i] == 0) || ((localRenderer.isCurrentStreamFinal()) && (localRenderer.getStream() == paramMediaPeriodHolder.sampleStreams[i])))) {
          disableRenderer(localRenderer);
        }
        i += 1;
        j = k;
        break;
      }
    }
    this.playbackInfo = this.playbackInfo.copyWithTrackSelectorResult(localMediaPeriodHolder.trackSelectorResult);
    enableRenderers(arrayOfBoolean, j);
  }
  
  private void updateTrackSelectionPlaybackSpeed(float paramFloat)
  {
    for (MediaPeriodHolder localMediaPeriodHolder = this.queue.getFrontPeriod(); localMediaPeriodHolder != null; localMediaPeriodHolder = localMediaPeriodHolder.next) {
      if (localMediaPeriodHolder.trackSelectorResult != null)
      {
        TrackSelection[] arrayOfTrackSelection = localMediaPeriodHolder.trackSelectorResult.selections.getAll();
        int j = arrayOfTrackSelection.length;
        int i = 0;
        while (i < j)
        {
          TrackSelection localTrackSelection = arrayOfTrackSelection[i];
          if (localTrackSelection != null) {
            localTrackSelection.onPlaybackSpeed(paramFloat);
          }
          i += 1;
        }
      }
    }
  }
  
  private void validateExistingPeriodHolders()
    throws ExoPlaybackException
  {
    MediaPeriodHolder localMediaPeriodHolder = this.queue.getFrontPeriod();
    Object localObject = localMediaPeriodHolder;
    if (localMediaPeriodHolder == null) {}
    long l;
    do
    {
      boolean bool;
      do
      {
        return;
        int i;
        do
        {
          localObject = ((MediaPeriodHolder)localObject).next;
          i = this.playbackInfo.timeline.getNextPeriodIndex(((MediaPeriodHolder)localObject).info.id.periodIndex, this.period, this.window, this.repeatMode, this.shuffleModeEnabled);
          while ((((MediaPeriodHolder)localObject).next != null) && (!((MediaPeriodHolder)localObject).info.isLastInTimelinePeriod)) {
            localObject = ((MediaPeriodHolder)localObject).next;
          }
        } while ((i != -1) && (((MediaPeriodHolder)localObject).next != null) && (((MediaPeriodHolder)localObject).next.info.id.periodIndex == i));
        bool = this.queue.removeAfter((MediaPeriodHolder)localObject);
        ((MediaPeriodHolder)localObject).info = this.queue.getUpdatedMediaPeriodInfo(((MediaPeriodHolder)localObject).info);
      } while ((!bool) || (!this.queue.hasPlayingPeriod()));
      localObject = this.queue.getPlayingPeriod().info.id;
      l = seekToPeriodPosition((MediaSource.MediaPeriodId)localObject, this.playbackInfo.positionUs, true);
    } while (l == this.playbackInfo.positionUs);
    this.playbackInfo = this.playbackInfo.fromNewPosition((MediaSource.MediaPeriodId)localObject, l, this.playbackInfo.contentPositionUs);
    this.playbackInfoUpdate.setPositionDiscontinuity(4);
  }
  
  public Looper getPlaybackLooper()
  {
    return this.internalPlaybackThread.getLooper();
  }
  
  public boolean handleMessage(Message paramMessage)
  {
    for (;;)
    {
      try
      {
        switch (paramMessage.what)
        {
        case 0: 
          MediaSource localMediaSource = (MediaSource)paramMessage.obj;
          if (paramMessage.arg1 == 0) {
            break label437;
          }
          bool = true;
          prepareInternal(localMediaSource, bool);
        }
      }
      catch (ExoPlaybackException paramMessage)
      {
        Log.e("ExoPlayerImplInternal", "Renderer error.", paramMessage);
        stopInternal(false, false);
        this.eventHandler.obtainMessage(2, paramMessage).sendToTarget();
        maybeNotifyPlaybackInfoChanged();
        break;
        bool = false;
        continue;
        setRepeatModeInternal(paramMessage.arg1);
        continue;
      }
      catch (IOException paramMessage)
      {
        Log.e("ExoPlayerImplInternal", "Source error.", paramMessage);
        stopInternal(false, false);
        this.eventHandler.obtainMessage(2, ExoPlaybackException.createForSource(paramMessage)).sendToTarget();
        maybeNotifyPlaybackInfoChanged();
        break;
        if (paramMessage.arg1 == 0) {
          break label278;
        }
        bool = true;
        setShuffleModeEnabledInternal(bool);
        continue;
      }
      catch (RuntimeException paramMessage)
      {
        label107:
        Log.e("ExoPlayerImplInternal", "Internal runtime error.", paramMessage);
        stopInternal(false, false);
        this.eventHandler.obtainMessage(2, ExoPlaybackException.createForUnexpected(paramMessage)).sendToTarget();
        maybeNotifyPlaybackInfoChanged();
      }
      maybeNotifyPlaybackInfoChanged();
      break;
      if (paramMessage.arg1 != 0)
      {
        bool = true;
        setPlayWhenReadyInternal(bool);
      }
      else
      {
        label278:
        bool = false;
        continue;
        doSomeWork();
        continue;
        seekToInternal((SeekPosition)paramMessage.obj);
        continue;
        setPlaybackParametersInternal((PlaybackParameters)paramMessage.obj);
        continue;
        setSeekParametersInternal((SeekParameters)paramMessage.obj);
      }
    }
    if (paramMessage.arg1 != 0) {}
    for (boolean bool = true;; bool = false)
    {
      stopInternal(bool, true);
      break label107;
      handlePeriodPrepared((MediaPeriod)paramMessage.obj);
      break label107;
      handleSourceInfoRefreshed((MediaSourceRefreshInfo)paramMessage.obj);
      break label107;
      handleContinueLoadingRequested((MediaPeriod)paramMessage.obj);
      break label107;
      reselectTracksInternal();
      break label107;
      sendMessageInternal((PlayerMessage)paramMessage.obj);
      break label107;
      sendMessageToTargetThread((PlayerMessage)paramMessage.obj);
      break label107;
      releaseInternal();
      return true;
      return false;
      return true;
      label437:
      bool = false;
      break;
    }
  }
  
  public void onContinueLoadingRequested(MediaPeriod paramMediaPeriod)
  {
    this.handler.obtainMessage(10, paramMediaPeriod).sendToTarget();
  }
  
  public void onPlaybackParametersChanged(PlaybackParameters paramPlaybackParameters)
  {
    this.eventHandler.obtainMessage(1, paramPlaybackParameters).sendToTarget();
    updateTrackSelectionPlaybackSpeed(paramPlaybackParameters.speed);
  }
  
  public void onPrepared(MediaPeriod paramMediaPeriod)
  {
    this.handler.obtainMessage(9, paramMediaPeriod).sendToTarget();
  }
  
  public void onSourceInfoRefreshed(MediaSource paramMediaSource, Timeline paramTimeline, Object paramObject)
  {
    this.handler.obtainMessage(8, new MediaSourceRefreshInfo(paramMediaSource, paramTimeline, paramObject)).sendToTarget();
  }
  
  public void onTrackSelectionsInvalidated()
  {
    this.handler.sendEmptyMessage(11);
  }
  
  public void prepare(MediaSource paramMediaSource, boolean paramBoolean)
  {
    HandlerWrapper localHandlerWrapper = this.handler;
    if (paramBoolean) {}
    for (int i = 1;; i = 0)
    {
      localHandlerWrapper.obtainMessage(0, i, 0, paramMediaSource).sendToTarget();
      return;
    }
  }
  
  /* Error */
  public void release()
  {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield 869	org/telegram/messenger/exoplayer2/ExoPlayerImplInternal:released	Z
    //   6: istore_2
    //   7: iload_2
    //   8: ifeq +6 -> 14
    //   11: aload_0
    //   12: monitorexit
    //   13: return
    //   14: aload_0
    //   15: getfield 262	org/telegram/messenger/exoplayer2/ExoPlayerImplInternal:handler	Lorg/telegram/messenger/exoplayer2/util/HandlerWrapper;
    //   18: bipush 7
    //   20: invokeinterface 860 2 0
    //   25: pop
    //   26: iconst_0
    //   27: istore_1
    //   28: aload_0
    //   29: getfield 869	org/telegram/messenger/exoplayer2/ExoPlayerImplInternal:released	Z
    //   32: istore_2
    //   33: iload_2
    //   34: ifne +16 -> 50
    //   37: aload_0
    //   38: invokevirtual 1237	java/lang/Object:wait	()V
    //   41: goto -13 -> 28
    //   44: astore_3
    //   45: iconst_1
    //   46: istore_1
    //   47: goto -19 -> 28
    //   50: iload_1
    //   51: ifeq -40 -> 11
    //   54: invokestatic 1243	java/lang/Thread:currentThread	()Ljava/lang/Thread;
    //   57: invokevirtual 1246	java/lang/Thread:interrupt	()V
    //   60: goto -49 -> 11
    //   63: astore_3
    //   64: aload_0
    //   65: monitorexit
    //   66: aload_3
    //   67: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	68	0	this	ExoPlayerImplInternal
    //   27	24	1	i	int
    //   6	28	2	bool	boolean
    //   44	1	3	localInterruptedException	InterruptedException
    //   63	4	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   37	41	44	java/lang/InterruptedException
    //   2	7	63	finally
    //   14	26	63	finally
    //   28	33	63	finally
    //   37	41	63	finally
    //   54	60	63	finally
  }
  
  public void seekTo(Timeline paramTimeline, int paramInt, long paramLong)
  {
    this.handler.obtainMessage(3, new SeekPosition(paramTimeline, paramInt, paramLong)).sendToTarget();
  }
  
  /* Error */
  public void sendMessage(PlayerMessage paramPlayerMessage)
  {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield 869	org/telegram/messenger/exoplayer2/ExoPlayerImplInternal:released	Z
    //   6: ifeq +20 -> 26
    //   9: ldc 76
    //   11: ldc_w 1250
    //   14: invokestatic 1254	android/util/Log:w	(Ljava/lang/String;Ljava/lang/String;)I
    //   17: pop
    //   18: aload_1
    //   19: iconst_0
    //   20: invokevirtual 295	org/telegram/messenger/exoplayer2/PlayerMessage:markAsProcessed	(Z)V
    //   23: aload_0
    //   24: monitorexit
    //   25: return
    //   26: aload_0
    //   27: getfield 262	org/telegram/messenger/exoplayer2/ExoPlayerImplInternal:handler	Lorg/telegram/messenger/exoplayer2/util/HandlerWrapper;
    //   30: bipush 14
    //   32: aload_1
    //   33: invokeinterface 1036 3 0
    //   38: invokevirtual 306	android/os/Message:sendToTarget	()V
    //   41: goto -18 -> 23
    //   44: astore_1
    //   45: aload_0
    //   46: monitorexit
    //   47: aload_1
    //   48: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	49	0	this	ExoPlayerImplInternal
    //   0	49	1	paramPlayerMessage	PlayerMessage
    // Exception table:
    //   from	to	target	type
    //   2	23	44	finally
    //   26	41	44	finally
  }
  
  public void setPlayWhenReady(boolean paramBoolean)
  {
    HandlerWrapper localHandlerWrapper = this.handler;
    if (paramBoolean) {}
    for (int i = 1;; i = 0)
    {
      localHandlerWrapper.obtainMessage(1, i, 0).sendToTarget();
      return;
    }
  }
  
  public void setPlaybackParameters(PlaybackParameters paramPlaybackParameters)
  {
    this.handler.obtainMessage(4, paramPlaybackParameters).sendToTarget();
  }
  
  public void setRepeatMode(int paramInt)
  {
    this.handler.obtainMessage(12, paramInt, 0).sendToTarget();
  }
  
  public void setSeekParameters(SeekParameters paramSeekParameters)
  {
    this.handler.obtainMessage(5, paramSeekParameters).sendToTarget();
  }
  
  public void setShuffleModeEnabled(boolean paramBoolean)
  {
    HandlerWrapper localHandlerWrapper = this.handler;
    if (paramBoolean) {}
    for (int i = 1;; i = 0)
    {
      localHandlerWrapper.obtainMessage(13, i, 0).sendToTarget();
      return;
    }
  }
  
  public void stop(boolean paramBoolean)
  {
    HandlerWrapper localHandlerWrapper = this.handler;
    if (paramBoolean) {}
    for (int i = 1;; i = 0)
    {
      localHandlerWrapper.obtainMessage(6, i, 0).sendToTarget();
      return;
    }
  }
  
  private static final class MediaSourceRefreshInfo
  {
    public final Object manifest;
    public final MediaSource source;
    public final Timeline timeline;
    
    public MediaSourceRefreshInfo(MediaSource paramMediaSource, Timeline paramTimeline, Object paramObject)
    {
      this.source = paramMediaSource;
      this.timeline = paramTimeline;
      this.manifest = paramObject;
    }
  }
  
  private static final class PendingMessageInfo
    implements Comparable<PendingMessageInfo>
  {
    public final PlayerMessage message;
    public int resolvedPeriodIndex;
    public long resolvedPeriodTimeUs;
    public Object resolvedPeriodUid;
    
    public PendingMessageInfo(PlayerMessage paramPlayerMessage)
    {
      this.message = paramPlayerMessage;
    }
    
    public int compareTo(PendingMessageInfo paramPendingMessageInfo)
    {
      int k = 1;
      if (this.resolvedPeriodUid == null)
      {
        i = 1;
        if (paramPendingMessageInfo.resolvedPeriodUid != null) {
          break label45;
        }
      }
      label45:
      for (int j = 1;; j = 0)
      {
        if (i == j) {
          break label50;
        }
        i = k;
        if (this.resolvedPeriodUid != null) {
          i = -1;
        }
        return i;
        i = 0;
        break;
      }
      label50:
      if (this.resolvedPeriodUid == null) {
        return 0;
      }
      int i = this.resolvedPeriodIndex - paramPendingMessageInfo.resolvedPeriodIndex;
      if (i != 0) {
        return i;
      }
      return Util.compareLong(this.resolvedPeriodTimeUs, paramPendingMessageInfo.resolvedPeriodTimeUs);
    }
    
    public void setResolvedPosition(int paramInt, long paramLong, Object paramObject)
    {
      this.resolvedPeriodIndex = paramInt;
      this.resolvedPeriodTimeUs = paramLong;
      this.resolvedPeriodUid = paramObject;
    }
  }
  
  private static final class PlaybackInfoUpdate
  {
    private int discontinuityReason;
    private PlaybackInfo lastPlaybackInfo;
    private int operationAcks;
    private boolean positionDiscontinuity;
    
    public boolean hasPendingUpdate(PlaybackInfo paramPlaybackInfo)
    {
      return (paramPlaybackInfo != this.lastPlaybackInfo) || (this.operationAcks > 0) || (this.positionDiscontinuity);
    }
    
    public void incrementPendingOperationAcks(int paramInt)
    {
      this.operationAcks += paramInt;
    }
    
    public void reset(PlaybackInfo paramPlaybackInfo)
    {
      this.lastPlaybackInfo = paramPlaybackInfo;
      this.operationAcks = 0;
      this.positionDiscontinuity = false;
    }
    
    public void setPositionDiscontinuity(int paramInt)
    {
      boolean bool = true;
      if ((this.positionDiscontinuity) && (this.discontinuityReason != 4))
      {
        if (paramInt == 4) {}
        for (;;)
        {
          Assertions.checkArgument(bool);
          return;
          bool = false;
        }
      }
      this.positionDiscontinuity = true;
      this.discontinuityReason = paramInt;
    }
  }
  
  private static final class SeekPosition
  {
    public final Timeline timeline;
    public final int windowIndex;
    public final long windowPositionUs;
    
    public SeekPosition(Timeline paramTimeline, int paramInt, long paramLong)
    {
      this.timeline = paramTimeline;
      this.windowIndex = paramInt;
      this.windowPositionUs = paramLong;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/ExoPlayerImplInternal.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */