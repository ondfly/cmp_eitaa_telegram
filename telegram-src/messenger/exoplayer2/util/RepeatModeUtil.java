package org.telegram.messenger.exoplayer2.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class RepeatModeUtil
{
  public static final int REPEAT_TOGGLE_MODE_ALL = 2;
  public static final int REPEAT_TOGGLE_MODE_NONE = 0;
  public static final int REPEAT_TOGGLE_MODE_ONE = 1;
  
  public static int getNextRepeatMode(int paramInt1, int paramInt2)
  {
    int i = 1;
    while (i <= 2)
    {
      int j = (paramInt1 + i) % 3;
      if (isRepeatModeEnabled(j, paramInt2)) {
        return j;
      }
      i += 1;
    }
    return paramInt1;
  }
  
  public static boolean isRepeatModeEnabled(int paramInt1, int paramInt2)
  {
    boolean bool2 = true;
    boolean bool1 = bool2;
    switch (paramInt1)
    {
    default: 
      bool1 = false;
    }
    do
    {
      do
      {
        return bool1;
        bool1 = bool2;
      } while ((paramInt2 & 0x1) != 0);
      return false;
      bool1 = bool2;
    } while ((paramInt2 & 0x2) != 0);
    return false;
  }
  
  @Retention(RetentionPolicy.SOURCE)
  public static @interface RepeatToggleModes {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/util/RepeatModeUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */