package ir.eitaa.messenger.exoplayer.upstream;

public abstract interface Allocator
{
  public abstract Allocation allocate();
  
  public abstract void blockWhileTotalBytesAllocatedExceeds(int paramInt)
    throws InterruptedException;
  
  public abstract int getIndividualAllocationLength();
  
  public abstract int getTotalBytesAllocated();
  
  public abstract void release(Allocation paramAllocation);
  
  public abstract void release(Allocation[] paramArrayOfAllocation);
  
  public abstract void trim(int paramInt);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/upstream/Allocator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */