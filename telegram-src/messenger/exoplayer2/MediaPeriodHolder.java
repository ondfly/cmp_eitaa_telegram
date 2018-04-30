package org.telegram.messenger.exoplayer2;

import android.util.Log;
import org.telegram.messenger.exoplayer2.source.ClippingMediaPeriod;
import org.telegram.messenger.exoplayer2.source.EmptySampleStream;
import org.telegram.messenger.exoplayer2.source.MediaPeriod;
import org.telegram.messenger.exoplayer2.source.MediaSource;
import org.telegram.messenger.exoplayer2.source.SampleStream;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelectionArray;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelector;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelectorResult;
import org.telegram.messenger.exoplayer2.upstream.Allocator;
import org.telegram.messenger.exoplayer2.util.Assertions;

final class MediaPeriodHolder
{
  private static final String TAG = "MediaPeriodHolder";
  public boolean hasEnabledTracks;
  public MediaPeriodInfo info;
  public final boolean[] mayRetainStreamFlags;
  public final MediaPeriod mediaPeriod;
  private final MediaSource mediaSource;
  public MediaPeriodHolder next;
  private TrackSelectorResult periodTrackSelectorResult;
  public boolean prepared;
  private final RendererCapabilities[] rendererCapabilities;
  public long rendererPositionOffsetUs;
  public final SampleStream[] sampleStreams;
  private final TrackSelector trackSelector;
  public TrackSelectorResult trackSelectorResult;
  public final Object uid;
  
  public MediaPeriodHolder(RendererCapabilities[] paramArrayOfRendererCapabilities, long paramLong, TrackSelector paramTrackSelector, Allocator paramAllocator, MediaSource paramMediaSource, Object paramObject, MediaPeriodInfo paramMediaPeriodInfo)
  {
    this.rendererCapabilities = paramArrayOfRendererCapabilities;
    this.rendererPositionOffsetUs = (paramLong - paramMediaPeriodInfo.startPositionUs);
    this.trackSelector = paramTrackSelector;
    this.mediaSource = paramMediaSource;
    this.uid = Assertions.checkNotNull(paramObject);
    this.info = paramMediaPeriodInfo;
    this.sampleStreams = new SampleStream[paramArrayOfRendererCapabilities.length];
    this.mayRetainStreamFlags = new boolean[paramArrayOfRendererCapabilities.length];
    paramTrackSelector = paramMediaSource.createPeriod(paramMediaPeriodInfo.id, paramAllocator);
    paramArrayOfRendererCapabilities = paramTrackSelector;
    if (paramMediaPeriodInfo.endPositionUs != Long.MIN_VALUE)
    {
      paramArrayOfRendererCapabilities = new ClippingMediaPeriod(paramTrackSelector, true);
      paramArrayOfRendererCapabilities.setClipping(0L, paramMediaPeriodInfo.endPositionUs);
    }
    this.mediaPeriod = paramArrayOfRendererCapabilities;
  }
  
  private void associateNoSampleRenderersWithEmptySampleStream(SampleStream[] paramArrayOfSampleStream)
  {
    int i = 0;
    while (i < this.rendererCapabilities.length)
    {
      if ((this.rendererCapabilities[i].getTrackType() == 5) && (this.trackSelectorResult.renderersEnabled[i] != 0)) {
        paramArrayOfSampleStream[i] = new EmptySampleStream();
      }
      i += 1;
    }
  }
  
  private void disableTrackSelectionsInResult(TrackSelectorResult paramTrackSelectorResult)
  {
    int i = 0;
    while (i < paramTrackSelectorResult.renderersEnabled.length)
    {
      int j = paramTrackSelectorResult.renderersEnabled[i];
      TrackSelection localTrackSelection = paramTrackSelectorResult.selections.get(i);
      if ((j != 0) && (localTrackSelection != null)) {
        localTrackSelection.disable();
      }
      i += 1;
    }
  }
  
  private void disassociateNoSampleRenderersWithEmptySampleStream(SampleStream[] paramArrayOfSampleStream)
  {
    int i = 0;
    while (i < this.rendererCapabilities.length)
    {
      if (this.rendererCapabilities[i].getTrackType() == 5) {
        paramArrayOfSampleStream[i] = null;
      }
      i += 1;
    }
  }
  
  private void enableTrackSelectionsInResult(TrackSelectorResult paramTrackSelectorResult)
  {
    int i = 0;
    while (i < paramTrackSelectorResult.renderersEnabled.length)
    {
      int j = paramTrackSelectorResult.renderersEnabled[i];
      TrackSelection localTrackSelection = paramTrackSelectorResult.selections.get(i);
      if ((j != 0) && (localTrackSelection != null)) {
        localTrackSelection.enable();
      }
      i += 1;
    }
  }
  
  private void updatePeriodTrackSelectorResult(TrackSelectorResult paramTrackSelectorResult)
  {
    if (this.periodTrackSelectorResult != null) {
      disableTrackSelectionsInResult(this.periodTrackSelectorResult);
    }
    this.periodTrackSelectorResult = paramTrackSelectorResult;
    if (this.periodTrackSelectorResult != null) {
      enableTrackSelectionsInResult(this.periodTrackSelectorResult);
    }
  }
  
  public long applyTrackSelection(long paramLong, boolean paramBoolean)
  {
    return applyTrackSelection(paramLong, paramBoolean, new boolean[this.rendererCapabilities.length]);
  }
  
  public long applyTrackSelection(long paramLong, boolean paramBoolean, boolean[] paramArrayOfBoolean)
  {
    TrackSelectionArray localTrackSelectionArray = this.trackSelectorResult.selections;
    int i = 0;
    if (i < localTrackSelectionArray.length)
    {
      boolean[] arrayOfBoolean = this.mayRetainStreamFlags;
      if ((!paramBoolean) && (this.trackSelectorResult.isEquivalent(this.periodTrackSelectorResult, i))) {}
      for (int j = 1;; j = 0)
      {
        arrayOfBoolean[i] = j;
        i += 1;
        break;
      }
    }
    disassociateNoSampleRenderersWithEmptySampleStream(this.sampleStreams);
    updatePeriodTrackSelectorResult(this.trackSelectorResult);
    paramLong = this.mediaPeriod.selectTracks(localTrackSelectionArray.getAll(), this.mayRetainStreamFlags, this.sampleStreams, paramArrayOfBoolean, paramLong);
    associateNoSampleRenderersWithEmptySampleStream(this.sampleStreams);
    this.hasEnabledTracks = false;
    i = 0;
    while (i < this.sampleStreams.length) {
      if (this.sampleStreams[i] != null)
      {
        Assertions.checkState(this.trackSelectorResult.renderersEnabled[i]);
        if (this.rendererCapabilities[i].getTrackType() != 5) {
          this.hasEnabledTracks = true;
        }
        i += 1;
      }
      else
      {
        if (localTrackSelectionArray.get(i) == null) {}
        for (paramBoolean = true;; paramBoolean = false)
        {
          Assertions.checkState(paramBoolean);
          break;
        }
      }
    }
    return paramLong;
  }
  
  public void continueLoading(long paramLong)
  {
    paramLong = toPeriodTime(paramLong);
    this.mediaPeriod.continueLoading(paramLong);
  }
  
  public long getBufferedPositionUs(boolean paramBoolean)
  {
    long l1;
    if (!this.prepared) {
      l1 = this.info.startPositionUs;
    }
    do
    {
      long l2;
      do
      {
        return l1;
        l2 = this.mediaPeriod.getBufferedPositionUs();
        l1 = l2;
      } while (l2 != Long.MIN_VALUE);
      l1 = l2;
    } while (!paramBoolean);
    return this.info.durationUs;
  }
  
  public long getDurationUs()
  {
    return this.info.durationUs;
  }
  
  public long getNextLoadPositionUs()
  {
    if (!this.prepared) {
      return 0L;
    }
    return this.mediaPeriod.getNextLoadPositionUs();
  }
  
  public long getRendererOffset()
  {
    return this.rendererPositionOffsetUs;
  }
  
  public TrackSelectorResult handlePrepared(float paramFloat)
    throws ExoPlaybackException
  {
    this.prepared = true;
    selectTracks(paramFloat);
    long l = applyTrackSelection(this.info.startPositionUs, false);
    this.rendererPositionOffsetUs += this.info.startPositionUs - l;
    this.info = this.info.copyWithStartPositionUs(l);
    return this.trackSelectorResult;
  }
  
  public boolean isFullyBuffered()
  {
    return (this.prepared) && ((!this.hasEnabledTracks) || (this.mediaPeriod.getBufferedPositionUs() == Long.MIN_VALUE));
  }
  
  public void reevaluateBuffer(long paramLong)
  {
    if (this.prepared) {
      this.mediaPeriod.reevaluateBuffer(toPeriodTime(paramLong));
    }
  }
  
  public void release()
  {
    updatePeriodTrackSelectorResult(null);
    try
    {
      if (this.info.endPositionUs != Long.MIN_VALUE)
      {
        this.mediaSource.releasePeriod(((ClippingMediaPeriod)this.mediaPeriod).mediaPeriod);
        return;
      }
      this.mediaSource.releasePeriod(this.mediaPeriod);
      return;
    }
    catch (RuntimeException localRuntimeException)
    {
      Log.e("MediaPeriodHolder", "Period release failed.", localRuntimeException);
    }
  }
  
  public boolean selectTracks(float paramFloat)
    throws ExoPlaybackException
  {
    int i = 0;
    Object localObject1 = this.trackSelector.selectTracks(this.rendererCapabilities, this.mediaPeriod.getTrackGroups());
    if (((TrackSelectorResult)localObject1).isEquivalent(this.periodTrackSelectorResult)) {
      return false;
    }
    this.trackSelectorResult = ((TrackSelectorResult)localObject1);
    localObject1 = this.trackSelectorResult.selections.getAll();
    int j = localObject1.length;
    while (i < j)
    {
      Object localObject2 = localObject1[i];
      if (localObject2 != null) {
        ((TrackSelection)localObject2).onPlaybackSpeed(paramFloat);
      }
      i += 1;
    }
    return true;
  }
  
  public long toPeriodTime(long paramLong)
  {
    return paramLong - getRendererOffset();
  }
  
  public long toRendererTime(long paramLong)
  {
    return getRendererOffset() + paramLong;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/MediaPeriodHolder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */