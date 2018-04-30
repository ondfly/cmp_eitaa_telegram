package org.telegram.messenger.support.customtabs;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public class PostMessageService
  extends Service
{
  private IPostMessageService.Stub mBinder = new IPostMessageService.Stub()
  {
    public void onMessageChannelReady(ICustomTabsCallback paramAnonymousICustomTabsCallback, Bundle paramAnonymousBundle)
      throws RemoteException
    {
      paramAnonymousICustomTabsCallback.onMessageChannelReady(paramAnonymousBundle);
    }
    
    public void onPostMessage(ICustomTabsCallback paramAnonymousICustomTabsCallback, String paramAnonymousString, Bundle paramAnonymousBundle)
      throws RemoteException
    {
      paramAnonymousICustomTabsCallback.onPostMessage(paramAnonymousString, paramAnonymousBundle);
    }
  };
  
  public IBinder onBind(Intent paramIntent)
  {
    return this.mBinder;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/support/customtabs/PostMessageService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */