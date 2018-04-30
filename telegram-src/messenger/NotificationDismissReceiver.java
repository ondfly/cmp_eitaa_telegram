package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class NotificationDismissReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    if (paramIntent == null) {
      return;
    }
    MessagesController.getNotificationsSettings(paramIntent.getIntExtra("currentAccount", UserConfig.selectedAccount)).edit().putInt("dismissDate", paramIntent.getIntExtra("messageDate", 0)).commit();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/NotificationDismissReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */