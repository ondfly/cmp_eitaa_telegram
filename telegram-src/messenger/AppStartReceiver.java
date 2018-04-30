package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppStartReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run() {}
    });
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/AppStartReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */