package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PopupReplyReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    if (paramIntent == null) {
      return;
    }
    ApplicationLoader.postInitApplication();
    NotificationsController.getInstance(paramIntent.getIntExtra("currentAccount", UserConfig.selectedAccount)).forceShowPopupForReply();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/PopupReplyReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */