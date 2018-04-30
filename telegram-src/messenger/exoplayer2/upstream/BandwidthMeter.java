package org.telegram.messenger.exoplayer2.upstream;

public abstract interface BandwidthMeter
{
  public static final long NO_ESTIMATE = -1L;
  
  public abstract long getBitrateEstimate();
  
  public static abstract interface EventListener
  {
    public abstract void onBandwidthSample(int paramInt, long paramLong1, long paramLong2);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/BandwidthMeter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */