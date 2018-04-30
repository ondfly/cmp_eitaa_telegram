package ir.eitaa.messenger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class NotificationsService
  extends Service
{
  public IBinder onBind(Intent paramIntent)
  {
    return null;
  }
  
  public void onCreate()
  {
    FileLog.e("TSMS", "service started");
    ApplicationLoader.postInitApplication();
  }
  
  public void onDestroy()
  {
    FileLog.e("TSMS", "service destroyed");
    if (ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getBoolean("pushService", true)) {
      sendBroadcast(new Intent("ir.eitaa.start"));
    }
  }
  
  public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
  {
    return 1;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/NotificationsService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */