package org.telegram.messenger.voip;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import org.telegram.messenger.ApplicationLoader;

public class JNIUtilities
{
  @TargetApi(23)
  public static String getCurrentNetworkInterfaceName()
  {
    Object localObject = (ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity");
    Network localNetwork = ((ConnectivityManager)localObject).getActiveNetwork();
    if (localNetwork == null) {}
    do
    {
      return null;
      localObject = ((ConnectivityManager)localObject).getLinkProperties(localNetwork);
    } while (localObject == null);
    return ((LinkProperties)localObject).getInterfaceName();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/JNIUtilities.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */