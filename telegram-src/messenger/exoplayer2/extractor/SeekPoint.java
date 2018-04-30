package org.telegram.messenger.exoplayer2.extractor;

public final class SeekPoint
{
  public static final SeekPoint START = new SeekPoint(0L, 0L);
  public final long position;
  public final long timeUs;
  
  public SeekPoint(long paramLong1, long paramLong2)
  {
    this.timeUs = paramLong1;
    this.position = paramLong2;
  }
  
  public boolean equals(Object paramObject)
  {
    if (this == paramObject) {}
    do
    {
      return true;
      if ((paramObject == null) || (getClass() != paramObject.getClass())) {
        return false;
      }
      paramObject = (SeekPoint)paramObject;
    } while ((this.timeUs == ((SeekPoint)paramObject).timeUs) && (this.position == ((SeekPoint)paramObject).position));
    return false;
  }
  
  public int hashCode()
  {
    return (int)this.timeUs * 31 + (int)this.position;
  }
  
  public String toString()
  {
    return "[timeUs=" + this.timeUs + ", position=" + this.position + "]";
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/SeekPoint.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */