package ir.eitaa.messenger.exoplayer.upstream;

public abstract interface BandwidthMeter
  extends TransferListener
{
  public static final long NO_ESTIMATE = -1L;
  
  public abstract long getBitrateEstimate();
  
  public static abstract interface EventListener
  {
    public abstract void onBandwidthSample(int paramInt, long paramLong1, long paramLong2);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/upstream/BandwidthMeter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */