package ir.eitaa.messenger.exoplayer.dash;

import ir.eitaa.messenger.exoplayer.dash.mpd.RangedUri;

public abstract interface DashSegmentIndex
{
  public static final int INDEX_UNBOUNDED = -1;
  
  public abstract long getDurationUs(int paramInt, long paramLong);
  
  public abstract int getFirstSegmentNum();
  
  public abstract int getLastSegmentNum(long paramLong);
  
  public abstract int getSegmentNum(long paramLong1, long paramLong2);
  
  public abstract RangedUri getSegmentUrl(int paramInt);
  
  public abstract long getTimeUs(int paramInt);
  
  public abstract boolean isExplicit();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/dash/DashSegmentIndex.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */