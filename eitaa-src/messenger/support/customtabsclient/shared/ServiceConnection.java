package ir.eitaa.messenger.support.customtabsclient.shared;

import android.content.ComponentName;
import ir.eitaa.messenger.support.customtabs.CustomTabsClient;
import ir.eitaa.messenger.support.customtabs.CustomTabsServiceConnection;
import java.lang.ref.WeakReference;

public class ServiceConnection
  extends CustomTabsServiceConnection
{
  private WeakReference<ServiceConnectionCallback> mConnectionCallback;
  
  public ServiceConnection(ServiceConnectionCallback paramServiceConnectionCallback)
  {
    this.mConnectionCallback = new WeakReference(paramServiceConnectionCallback);
  }
  
  public void onCustomTabsServiceConnected(ComponentName paramComponentName, CustomTabsClient paramCustomTabsClient)
  {
    paramComponentName = (ServiceConnectionCallback)this.mConnectionCallback.get();
    if (paramComponentName != null) {
      paramComponentName.onServiceConnected(paramCustomTabsClient);
    }
  }
  
  public void onServiceDisconnected(ComponentName paramComponentName)
  {
    paramComponentName = (ServiceConnectionCallback)this.mConnectionCallback.get();
    if (paramComponentName != null) {
      paramComponentName.onServiceDisconnected();
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/support/customtabsclient/shared/ServiceConnection.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */