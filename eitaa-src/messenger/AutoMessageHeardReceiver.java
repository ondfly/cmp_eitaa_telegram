package ir.eitaa.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoMessageHeardReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    ApplicationLoader.postInitApplication();
    long l = paramIntent.getLongExtra("dialog_id", 0L);
    int i = paramIntent.getIntExtra("max_id", 0);
    if ((l == 0L) || (i == 0)) {
      return;
    }
    MessagesController.getInstance().markDialogAsRead(l, i, i, 0, true, false);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/AutoMessageHeardReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */