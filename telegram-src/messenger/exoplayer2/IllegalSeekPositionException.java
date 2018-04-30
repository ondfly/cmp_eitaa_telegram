package org.telegram.messenger.exoplayer2;

public final class IllegalSeekPositionException
  extends IllegalStateException
{
  public final long positionMs;
  public final Timeline timeline;
  public final int windowIndex;
  
  public IllegalSeekPositionException(Timeline paramTimeline, int paramInt, long paramLong)
  {
    this.timeline = paramTimeline;
    this.windowIndex = paramInt;
    this.positionMs = paramLong;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/IllegalSeekPositionException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */