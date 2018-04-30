package ir.eitaa.messenger.video;

public class Sample
{
  private long offset = 0L;
  private long size = 0L;
  
  public Sample(long paramLong1, long paramLong2)
  {
    this.offset = paramLong1;
    this.size = paramLong2;
  }
  
  public long getOffset()
  {
    return this.offset;
  }
  
  public long getSize()
  {
    return this.size;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/video/Sample.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */