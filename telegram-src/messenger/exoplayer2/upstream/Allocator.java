package org.telegram.messenger.exoplayer2.upstream;

public abstract interface Allocator
{
  public abstract Allocation allocate();
  
  public abstract int getIndividualAllocationLength();
  
  public abstract int getTotalBytesAllocated();
  
  public abstract void release(Allocation paramAllocation);
  
  public abstract void release(Allocation[] paramArrayOfAllocation);
  
  public abstract void trim();
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/Allocator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */