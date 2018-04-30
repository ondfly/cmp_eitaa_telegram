package org.telegram.messenger.exoplayer2;

import org.telegram.messenger.exoplayer2.util.Assertions;

public final class PlaybackParameters
{
  public static final PlaybackParameters DEFAULT = new PlaybackParameters(1.0F, 1.0F);
  public final float pitch;
  private final int scaledUsPerMs;
  public final float speed;
  
  public PlaybackParameters(float paramFloat1, float paramFloat2)
  {
    if (paramFloat1 > 0.0F)
    {
      bool1 = true;
      Assertions.checkArgument(bool1);
      if (paramFloat2 <= 0.0F) {
        break label59;
      }
    }
    label59:
    for (boolean bool1 = bool2;; bool1 = false)
    {
      Assertions.checkArgument(bool1);
      this.speed = paramFloat1;
      this.pitch = paramFloat2;
      this.scaledUsPerMs = Math.round(1000.0F * paramFloat1);
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
      paramObject = (PlaybackParameters)paramObject;
    } while ((this.speed == ((PlaybackParameters)paramObject).speed) && (this.pitch == ((PlaybackParameters)paramObject).pitch));
    return false;
  }
  
  public long getMediaTimeUsForPlayoutTimeMs(long paramLong)
  {
    return this.scaledUsPerMs * paramLong;
  }
  
  public int hashCode()
  {
    return (Float.floatToRawIntBits(this.speed) + 527) * 31 + Float.floatToRawIntBits(this.pitch);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/PlaybackParameters.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */