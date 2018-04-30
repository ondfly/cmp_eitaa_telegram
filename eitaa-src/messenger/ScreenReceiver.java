package ir.eitaa.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ir.eitaa.tgnet.ConnectionsManager;

public class ScreenReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    if (paramIntent.getAction().equals("android.intent.action.SCREEN_OFF"))
    {
      FileLog.e("TSMS", "screen off");
      ConnectionsManager.getInstance().setAppPaused(true, true);
      ApplicationLoader.isScreenOn = false;
    }
    for (;;)
    {
      NotificationCenter.getInstance().postNotificationName(NotificationCenter.screenStateChanged, new Object[0]);
      return;
      if (paramIntent.getAction().equals("android.intent.action.SCREEN_ON"))
      {
        FileLog.e("TSMS", "screen on");
        ConnectionsManager.getInstance().setAppPaused(false, true);
        ApplicationLoader.isScreenOn = true;
      }
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/ScreenReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */