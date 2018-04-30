package org.telegram.messenger.voip;

import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

public class VoIPServerConfig
{
  private static JSONObject config;
  
  public static boolean getBoolean(String paramString, boolean paramBoolean)
  {
    return config.optBoolean(paramString, paramBoolean);
  }
  
  public static double getDouble(String paramString, double paramDouble)
  {
    return config.optDouble(paramString, paramDouble);
  }
  
  public static int getInt(String paramString, int paramInt)
  {
    return config.optInt(paramString, paramInt);
  }
  
  public static String getString(String paramString1, String paramString2)
  {
    return config.optString(paramString1, paramString2);
  }
  
  private static native void nativeSetConfig(String[] paramArrayOfString1, String[] paramArrayOfString2);
  
  public static void setConfig(String paramString)
  {
    try
    {
      paramString = new JSONObject(paramString);
      config = paramString;
      String[] arrayOfString1 = new String[paramString.length()];
      String[] arrayOfString2 = new String[paramString.length()];
      Iterator localIterator = paramString.keys();
      int i = 0;
      while (localIterator.hasNext())
      {
        arrayOfString1[i] = ((String)localIterator.next());
        arrayOfString2[i] = paramString.getString(arrayOfString1[i]);
        i += 1;
      }
      nativeSetConfig(arrayOfString1, arrayOfString2);
      return;
    }
    catch (JSONException paramString)
    {
      while (!BuildVars.LOGS_ENABLED) {}
      FileLog.e("Error parsing VoIP config", paramString);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/VoIPServerConfig.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */