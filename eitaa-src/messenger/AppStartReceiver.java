package ir.eitaa.messenger;

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


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/AppStartReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */