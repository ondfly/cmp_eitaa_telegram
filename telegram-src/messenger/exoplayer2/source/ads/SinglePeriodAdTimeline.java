package org.telegram.messenger.exoplayer2.source.ads;

import org.telegram.messenger.exoplayer2.Timeline;
import org.telegram.messenger.exoplayer2.Timeline.Period;
import org.telegram.messenger.exoplayer2.Timeline.Window;
import org.telegram.messenger.exoplayer2.source.ForwardingTimeline;
import org.telegram.messenger.exoplayer2.util.Assertions;

final class SinglePeriodAdTimeline
  extends ForwardingTimeline
{
  private final AdPlaybackState adPlaybackState;
  
  public SinglePeriodAdTimeline(Timeline paramTimeline, AdPlaybackState paramAdPlaybackState)
  {
    super(paramTimeline);
    if (paramTimeline.getPeriodCount() == 1)
    {
      bool1 = true;
      Assertions.checkState(bool1);
      if (paramTimeline.getWindowCount() != 1) {
        break label48;
      }
    }
    label48:
    for (boolean bool1 = bool2;; bool1 = false)
    {
      Assertions.checkState(bool1);
      this.adPlaybackState = paramAdPlaybackState;
      return;
      bool1 = false;
      break;
    }
  }
  
  public Timeline.Period getPeriod(int paramInt, Timeline.Period paramPeriod, boolean paramBoolean)
  {
    this.timeline.getPeriod(paramInt, paramPeriod, paramBoolean);
    paramPeriod.set(paramPeriod.id, paramPeriod.uid, paramPeriod.windowIndex, paramPeriod.durationUs, paramPeriod.getPositionInWindowUs(), this.adPlaybackState);
    return paramPeriod;
  }
  
  public Timeline.Window getWindow(int paramInt, Timeline.Window paramWindow, boolean paramBoolean, long paramLong)
  {
    paramWindow = super.getWindow(paramInt, paramWindow, paramBoolean, paramLong);
    if (paramWindow.durationUs == -9223372036854775807L) {
      paramWindow.durationUs = this.adPlaybackState.contentDurationUs;
    }
    return paramWindow;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/ads/SinglePeriodAdTimeline.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */