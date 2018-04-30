package ir.eitaa.messenger.exoplayer.util;

public final class VerboseLogUtil
{
  private static volatile boolean enableAllTags;
  private static volatile String[] enabledTags;
  
  public static boolean areAllTagsEnabled()
  {
    return enableAllTags;
  }
  
  public static boolean isTagEnabled(String paramString)
  {
    if (enableAllTags) {
      return true;
    }
    String[] arrayOfString = enabledTags;
    if ((arrayOfString == null) || (arrayOfString.length == 0)) {
      return false;
    }
    int i = 0;
    for (;;)
    {
      if (i >= arrayOfString.length) {
        break label48;
      }
      if (arrayOfString[i].equals(paramString)) {
        break;
      }
      i += 1;
    }
    label48:
    return false;
  }
  
  public static void setEnableAllTags(boolean paramBoolean)
  {
    enableAllTags = paramBoolean;
  }
  
  public static void setEnabledTags(String... paramVarArgs)
  {
    enabledTags = paramVarArgs;
    enableAllTags = false;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/util/VerboseLogUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */