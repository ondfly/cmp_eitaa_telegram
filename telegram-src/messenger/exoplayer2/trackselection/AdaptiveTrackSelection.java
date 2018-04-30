package org.telegram.messenger.exoplayer2.trackselection;

import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.source.TrackGroup;
import org.telegram.messenger.exoplayer2.source.chunk.MediaChunk;
import org.telegram.messenger.exoplayer2.upstream.BandwidthMeter;
import org.telegram.messenger.exoplayer2.util.Clock;
import org.telegram.messenger.exoplayer2.util.Util;

public class AdaptiveTrackSelection
  extends BaseTrackSelection
{
  public static final float DEFAULT_BANDWIDTH_FRACTION = 0.75F;
  public static final float DEFAULT_BUFFERED_FRACTION_TO_LIVE_EDGE_FOR_QUALITY_INCREASE = 0.75F;
  public static final int DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS = 25000;
  public static final int DEFAULT_MAX_INITIAL_BITRATE = 800000;
  public static final int DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS = 10000;
  public static final int DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS = 25000;
  public static final long DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS = 2000L;
  private final float bandwidthFraction;
  private final BandwidthMeter bandwidthMeter;
  private final float bufferedFractionToLiveEdgeForQualityIncrease;
  private final Clock clock;
  private long lastBufferEvaluationMs;
  private final long maxDurationForQualityDecreaseUs;
  private final int maxInitialBitrate;
  private final long minDurationForQualityIncreaseUs;
  private final long minDurationToRetainAfterDiscardUs;
  private final long minTimeBetweenBufferReevaluationMs;
  private float playbackSpeed;
  private int reason;
  private int selectedIndex;
  
  public AdaptiveTrackSelection(TrackGroup paramTrackGroup, int[] paramArrayOfInt, BandwidthMeter paramBandwidthMeter)
  {
    this(paramTrackGroup, paramArrayOfInt, paramBandwidthMeter, 800000, 10000L, 25000L, 25000L, 0.75F, 0.75F, 2000L, Clock.DEFAULT);
  }
  
  public AdaptiveTrackSelection(TrackGroup paramTrackGroup, int[] paramArrayOfInt, BandwidthMeter paramBandwidthMeter, int paramInt, long paramLong1, long paramLong2, long paramLong3, float paramFloat1, float paramFloat2, long paramLong4, Clock paramClock)
  {
    super(paramTrackGroup, paramArrayOfInt);
    this.bandwidthMeter = paramBandwidthMeter;
    this.maxInitialBitrate = paramInt;
    this.minDurationForQualityIncreaseUs = (1000L * paramLong1);
    this.maxDurationForQualityDecreaseUs = (1000L * paramLong2);
    this.minDurationToRetainAfterDiscardUs = (1000L * paramLong3);
    this.bandwidthFraction = paramFloat1;
    this.bufferedFractionToLiveEdgeForQualityIncrease = paramFloat2;
    this.minTimeBetweenBufferReevaluationMs = paramLong4;
    this.clock = paramClock;
    this.playbackSpeed = 1.0F;
    this.selectedIndex = determineIdealSelectedIndex(Long.MIN_VALUE);
    this.reason = 1;
    this.lastBufferEvaluationMs = -9223372036854775807L;
  }
  
  private int determineIdealSelectedIndex(long paramLong)
  {
    long l = this.bandwidthMeter.getBitrateEstimate();
    int j;
    int i;
    if (l == -1L)
    {
      l = this.maxInitialBitrate;
      j = 0;
      i = 0;
    }
    for (;;)
    {
      if (i >= this.length) {
        break label107;
      }
      if ((paramLong == Long.MIN_VALUE) || (!isBlacklisted(i, paramLong)))
      {
        if (Math.round(getFormat(i).bitrate * this.playbackSpeed) <= l)
        {
          return i;
          l = ((float)l * this.bandwidthFraction);
          break;
        }
        j = i;
      }
      i += 1;
    }
    label107:
    return j;
  }
  
  private long minDurationForQualityIncreaseUs(long paramLong)
  {
    if ((paramLong != -9223372036854775807L) && (paramLong <= this.minDurationForQualityIncreaseUs)) {}
    for (int i = 1; i != 0; i = 0) {
      return ((float)paramLong * this.bufferedFractionToLiveEdgeForQualityIncrease);
    }
    return this.minDurationForQualityIncreaseUs;
  }
  
  public void enable()
  {
    this.lastBufferEvaluationMs = -9223372036854775807L;
  }
  
  public int evaluateQueueSize(long paramLong, List<? extends MediaChunk> paramList)
  {
    long l = this.clock.elapsedRealtime();
    int i;
    if ((this.lastBufferEvaluationMs != -9223372036854775807L) && (l - this.lastBufferEvaluationMs < this.minTimeBetweenBufferReevaluationMs)) {
      i = paramList.size();
    }
    int k;
    do
    {
      return i;
      this.lastBufferEvaluationMs = l;
      if (paramList.isEmpty()) {
        return 0;
      }
      k = paramList.size();
      i = k;
    } while (Util.getPlayoutDurationForMediaDuration(((MediaChunk)paramList.get(k - 1)).startTimeUs - paramLong, this.playbackSpeed) < this.minDurationToRetainAfterDiscardUs);
    Format localFormat1 = getFormat(determineIdealSelectedIndex(l));
    int j = 0;
    for (;;)
    {
      i = k;
      if (j >= k) {
        break;
      }
      MediaChunk localMediaChunk = (MediaChunk)paramList.get(j);
      Format localFormat2 = localMediaChunk.trackFormat;
      if ((Util.getPlayoutDurationForMediaDuration(localMediaChunk.startTimeUs - paramLong, this.playbackSpeed) >= this.minDurationToRetainAfterDiscardUs) && (localFormat2.bitrate < localFormat1.bitrate) && (localFormat2.height != -1) && (localFormat2.height < 720) && (localFormat2.width != -1) && (localFormat2.width < 1280) && (localFormat2.height < localFormat1.height)) {
        return j;
      }
      j += 1;
    }
  }
  
  public int getSelectedIndex()
  {
    return this.selectedIndex;
  }
  
  public Object getSelectionData()
  {
    return null;
  }
  
  public int getSelectionReason()
  {
    return this.reason;
  }
  
  public void onPlaybackSpeed(float paramFloat)
  {
    this.playbackSpeed = paramFloat;
  }
  
  public void updateSelectedTrack(long paramLong1, long paramLong2, long paramLong3)
  {
    paramLong1 = this.clock.elapsedRealtime();
    int i = this.selectedIndex;
    this.selectedIndex = determineIdealSelectedIndex(paramLong1);
    if (this.selectedIndex == i) {}
    for (;;)
    {
      return;
      Format localFormat1;
      Format localFormat2;
      if (!isBlacklisted(i, paramLong1))
      {
        localFormat1 = getFormat(i);
        localFormat2 = getFormat(this.selectedIndex);
        if ((localFormat2.bitrate <= localFormat1.bitrate) || (paramLong2 >= minDurationForQualityIncreaseUs(paramLong3))) {
          break label108;
        }
      }
      for (this.selectedIndex = i; this.selectedIndex != i; this.selectedIndex = i)
      {
        label93:
        this.reason = 3;
        return;
        label108:
        if ((localFormat2.bitrate >= localFormat1.bitrate) || (paramLong2 < this.maxDurationForQualityDecreaseUs)) {
          break label93;
        }
      }
    }
  }
  
  public static final class Factory
    implements TrackSelection.Factory
  {
    private final float bandwidthFraction;
    private final BandwidthMeter bandwidthMeter;
    private final float bufferedFractionToLiveEdgeForQualityIncrease;
    private final Clock clock;
    private final int maxDurationForQualityDecreaseMs;
    private final int maxInitialBitrate;
    private final int minDurationForQualityIncreaseMs;
    private final int minDurationToRetainAfterDiscardMs;
    private final long minTimeBetweenBufferReevaluationMs;
    
    public Factory(BandwidthMeter paramBandwidthMeter)
    {
      this(paramBandwidthMeter, 800000, 10000, 25000, 25000, 0.75F, 0.75F, 2000L, Clock.DEFAULT);
    }
    
    public Factory(BandwidthMeter paramBandwidthMeter, int paramInt1, int paramInt2, int paramInt3, int paramInt4, float paramFloat)
    {
      this(paramBandwidthMeter, paramInt1, paramInt2, paramInt3, paramInt4, paramFloat, 0.75F, 2000L, Clock.DEFAULT);
    }
    
    public Factory(BandwidthMeter paramBandwidthMeter, int paramInt1, int paramInt2, int paramInt3, int paramInt4, float paramFloat1, float paramFloat2, long paramLong, Clock paramClock)
    {
      this.bandwidthMeter = paramBandwidthMeter;
      this.maxInitialBitrate = paramInt1;
      this.minDurationForQualityIncreaseMs = paramInt2;
      this.maxDurationForQualityDecreaseMs = paramInt3;
      this.minDurationToRetainAfterDiscardMs = paramInt4;
      this.bandwidthFraction = paramFloat1;
      this.bufferedFractionToLiveEdgeForQualityIncrease = paramFloat2;
      this.minTimeBetweenBufferReevaluationMs = paramLong;
      this.clock = paramClock;
    }
    
    public AdaptiveTrackSelection createTrackSelection(TrackGroup paramTrackGroup, int... paramVarArgs)
    {
      return new AdaptiveTrackSelection(paramTrackGroup, paramVarArgs, this.bandwidthMeter, this.maxInitialBitrate, this.minDurationForQualityIncreaseMs, this.maxDurationForQualityDecreaseMs, this.minDurationToRetainAfterDiscardMs, this.bandwidthFraction, this.bufferedFractionToLiveEdgeForQualityIncrease, this.minTimeBetweenBufferReevaluationMs, this.clock);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/trackselection/AdaptiveTrackSelection.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */