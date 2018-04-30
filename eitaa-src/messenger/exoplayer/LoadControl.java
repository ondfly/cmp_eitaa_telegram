package ir.eitaa.messenger.exoplayer;

import ir.eitaa.messenger.exoplayer.upstream.Allocator;

public abstract interface LoadControl
{
  public abstract Allocator getAllocator();
  
  public abstract void register(Object paramObject, int paramInt);
  
  public abstract void trimAllocator();
  
  public abstract void unregister(Object paramObject);
  
  public abstract boolean update(Object paramObject, long paramLong1, long paramLong2, boolean paramBoolean);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/LoadControl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */