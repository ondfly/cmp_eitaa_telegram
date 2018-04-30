package org.telegram.messenger.exoplayer2.extractor;

import org.telegram.messenger.exoplayer2.util.Assertions;

public abstract interface SeekMap
{
  public abstract long getDurationUs();
  
  public abstract SeekPoints getSeekPoints(long paramLong);
  
  public abstract boolean isSeekable();
  
  public static final class SeekPoints
  {
    public final SeekPoint first;
    public final SeekPoint second;
    
    public SeekPoints(SeekPoint paramSeekPoint)
    {
      this(paramSeekPoint, paramSeekPoint);
    }
    
    public SeekPoints(SeekPoint paramSeekPoint1, SeekPoint paramSeekPoint2)
    {
      this.first = ((SeekPoint)Assertions.checkNotNull(paramSeekPoint1));
      this.second = ((SeekPoint)Assertions.checkNotNull(paramSeekPoint2));
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
        paramObject = (SeekPoints)paramObject;
      } while ((this.first.equals(((SeekPoints)paramObject).first)) && (this.second.equals(((SeekPoints)paramObject).second)));
      return false;
    }
    
    public int hashCode()
    {
      return this.first.hashCode() * 31 + this.second.hashCode();
    }
    
    public String toString()
    {
      StringBuilder localStringBuilder = new StringBuilder().append("[").append(this.first);
      if (this.first.equals(this.second)) {}
      for (String str = "";; str = ", " + this.second) {
        return str + "]";
      }
    }
  }
  
  public static final class Unseekable
    implements SeekMap
  {
    private final long durationUs;
    private final SeekMap.SeekPoints startSeekPoints;
    
    public Unseekable(long paramLong)
    {
      this(paramLong, 0L);
    }
    
    public Unseekable(long paramLong1, long paramLong2)
    {
      this.durationUs = paramLong1;
      if (paramLong2 == 0L) {}
      for (SeekPoint localSeekPoint = SeekPoint.START;; localSeekPoint = new SeekPoint(0L, paramLong2))
      {
        this.startSeekPoints = new SeekMap.SeekPoints(localSeekPoint);
        return;
      }
    }
    
    public long getDurationUs()
    {
      return this.durationUs;
    }
    
    public SeekMap.SeekPoints getSeekPoints(long paramLong)
    {
      return this.startSeekPoints;
    }
    
    public boolean isSeekable()
    {
      return false;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/SeekMap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */