package ir.eitaa.messenger;

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
    ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit().putInt("dismissDate", paramIntent.getIntExtra("messageDate", 0)).commit();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/NotificationDismissReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */