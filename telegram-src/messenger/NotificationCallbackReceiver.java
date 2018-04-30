package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationCallbackReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    if (paramIntent == null) {
      return;
    }
    ApplicationLoader.postInitApplication();
    int i = paramIntent.getIntExtra("currentAccount", UserConfig.selectedAccount);
    long l = paramIntent.getLongExtra("did", 777000L);
    paramContext = paramIntent.getByteArrayExtra("data");
    int j = paramIntent.getIntExtra("mid", 0);
    SendMessagesHelper.getInstance(i).sendNotificationCallback(l, j, paramContext);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/NotificationCallbackReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */