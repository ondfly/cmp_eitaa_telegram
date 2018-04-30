package ir.eitaa.messenger.support.customtabs;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public abstract class CustomTabsServiceConnection
  implements ServiceConnection
{
  public abstract void onCustomTabsServiceConnected(ComponentName paramComponentName, CustomTabsClient paramCustomTabsClient);
  
  public final void onServiceConnected(ComponentName paramComponentName, IBinder paramIBinder)
  {
    onCustomTabsServiceConnected(paramComponentName, new CustomTabsClient(ICustomTabsService.Stub.asInterface(paramIBinder), paramComponentName) {});
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/support/customtabs/CustomTabsServiceConnection.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */