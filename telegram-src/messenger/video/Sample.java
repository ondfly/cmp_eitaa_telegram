package org.telegram.messenger.video;

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


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/video/Sample.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */