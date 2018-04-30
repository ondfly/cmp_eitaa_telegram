package org.telegram.messenger.exoplayer2;

import org.telegram.messenger.exoplayer2.util.Assertions;

public final class SeekParameters
{
  public static final SeekParameters CLOSEST_SYNC;
  public static final SeekParameters DEFAULT = EXACT;
  public static final SeekParameters EXACT = new SeekParameters(0L, 0L);
  public static final SeekParameters NEXT_SYNC;
  public static final SeekParameters PREVIOUS_SYNC;
  public final long toleranceAfterUs;
  public final long toleranceBeforeUs;
  
  static
  {
    CLOSEST_SYNC = new SeekParameters(Long.MAX_VALUE, Long.MAX_VALUE);
    PREVIOUS_SYNC = new SeekParameters(Long.MAX_VALUE, 0L);
    NEXT_SYNC = new SeekParameters(0L, Long.MAX_VALUE);
  }
  
  public SeekParameters(long paramLong1, long paramLong2)
  {
    if (paramLong1 >= 0L)
    {
      bool1 = true;
      Assertions.checkArgument(bool1);
      if (paramLong2 < 0L) {
        break label53;
      }
    }
    label53:
    for (boolean bool1 = bool2;; bool1 = false)
    {
      Assertions.checkArgument(bool1);
      this.toleranceBeforeUs = paramLong1;
      this.toleranceAfterUs = paramLong2;
      return;
      bool1 = false;
      break;
    }
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
      paramObject = (SeekParameters)paramObject;
    } while ((this.toleranceBeforeUs == ((SeekParameters)paramObject).toleranceBeforeUs) && (this.toleranceAfterUs == ((SeekParameters)paramObject).toleranceAfterUs));
    return false;
  }
  
  public int hashCode()
  {
    return (int)this.toleranceBeforeUs * 31 + (int)this.toleranceAfterUs;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/SeekParameters.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */