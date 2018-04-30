package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopLiveLocationReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    int i = 0;
    while (i < 3)
    {
      LocationController.getInstance(i).removeAllLocationSharings();
      i += 1;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/StopLiveLocationReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */