package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.telegram.tgnet.ConnectionsManager;

public class ScreenReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    if (paramIntent.getAction().equals("android.intent.action.SCREEN_OFF"))
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("screen off");
      }
      ConnectionsManager.getInstance(UserConfig.selectedAccount).setAppPaused(true, true);
      ApplicationLoader.isScreenOn = false;
    }
    while (!paramIntent.getAction().equals("android.intent.action.SCREEN_ON")) {
      return;
    }
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("screen on");
    }
    ConnectionsManager.getInstance(UserConfig.selectedAccount).setAppPaused(false, true);
    ApplicationLoader.isScreenOn = true;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/ScreenReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */