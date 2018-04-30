package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class RefererReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    try
    {
      MessagesController.getInstance(UserConfig.selectedAccount).setReferer(paramIntent.getExtras().getString("referrer"));
      return;
    }
    catch (Exception paramContext) {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/RefererReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */