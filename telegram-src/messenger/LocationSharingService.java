package org.telegram.messenger;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import java.util.ArrayList;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.ui.LaunchActivity;

public class LocationSharingService
  extends Service
  implements NotificationCenter.NotificationCenterDelegate
{
  private NotificationCompat.Builder builder;
  private Handler handler;
  private Runnable runnable;
  
  public LocationSharingService()
  {
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsChanged);
  }
  
  private ArrayList<LocationController.SharingLocationInfo> getInfos()
  {
    ArrayList localArrayList1 = new ArrayList();
    int i = 0;
    while (i < 3)
    {
      ArrayList localArrayList2 = LocationController.getInstance(i).sharingLocationsUI;
      if (!localArrayList2.isEmpty()) {
        localArrayList1.addAll(localArrayList2);
      }
      i += 1;
    }
    return localArrayList1;
  }
  
  private void updateNotification(boolean paramBoolean)
  {
    if (this.builder == null) {
      return;
    }
    Object localObject = getInfos();
    int i;
    int j;
    if (((ArrayList)localObject).size() == 1)
    {
      localObject = (LocationController.SharingLocationInfo)((ArrayList)localObject).get(0);
      i = (int)((LocationController.SharingLocationInfo)localObject).messageObject.getDialogId();
      j = ((LocationController.SharingLocationInfo)localObject).messageObject.currentAccount;
      if (i > 0) {
        localObject = UserObject.getFirstName(MessagesController.getInstance(j).getUser(Integer.valueOf(i)));
      }
    }
    for (;;)
    {
      localObject = String.format(LocaleController.getString("AttachLiveLocationIsSharing", 2131493032), new Object[] { LocaleController.getString("AttachLiveLocation", 2131493031), localObject });
      this.builder.setTicker((CharSequence)localObject);
      this.builder.setContentText((CharSequence)localObject);
      if (!paramBoolean) {
        break;
      }
      NotificationManagerCompat.from(ApplicationLoader.applicationContext).notify(6, this.builder.build());
      return;
      localObject = MessagesController.getInstance(j).getChat(Integer.valueOf(-i));
      if (localObject != null)
      {
        localObject = ((TLRPC.Chat)localObject).title;
      }
      else
      {
        localObject = "";
        continue;
        localObject = LocaleController.formatPluralString("Chats", ((ArrayList)localObject).size());
      }
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if ((paramInt1 == NotificationCenter.liveLocationsChanged) && (this.handler != null)) {
      this.handler.post(new Runnable()
      {
        public void run()
        {
          if (LocationSharingService.this.getInfos().isEmpty())
          {
            LocationSharingService.this.stopSelf();
            return;
          }
          LocationSharingService.this.updateNotification(true);
        }
      });
    }
  }
  
  public IBinder onBind(Intent paramIntent)
  {
    return null;
  }
  
  public void onCreate()
  {
    super.onCreate();
    this.handler = new Handler();
    this.runnable = new Runnable()
    {
      public void run()
      {
        LocationSharingService.this.handler.postDelayed(LocationSharingService.this.runnable, 60000L);
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            int i = 0;
            while (i < 3)
            {
              LocationController.getInstance(i).update();
              i += 1;
            }
          }
        });
      }
    };
    this.handler.postDelayed(this.runnable, 60000L);
  }
  
  public void onDestroy()
  {
    if (this.handler != null) {
      this.handler.removeCallbacks(this.runnable);
    }
    stopForeground(true);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsChanged);
  }
  
  public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
  {
    if (getInfos().isEmpty()) {
      stopSelf();
    }
    if (this.builder == null)
    {
      paramIntent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
      paramIntent.setAction("org.tmessages.openlocations");
      paramIntent.setFlags(32768);
      paramIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, paramIntent, 0);
      this.builder = new NotificationCompat.Builder(ApplicationLoader.applicationContext);
      this.builder.setWhen(System.currentTimeMillis());
      this.builder.setSmallIcon(2131165466);
      this.builder.setContentIntent(paramIntent);
      this.builder.setChannelId("Other3");
      this.builder.setContentTitle(LocaleController.getString("AppName", 2131492981));
      paramIntent = new Intent(ApplicationLoader.applicationContext, StopLiveLocationReceiver.class);
      this.builder.addAction(0, LocaleController.getString("StopLiveLocation", 2131494438), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 2, paramIntent, 134217728));
    }
    updateNotification(false);
    startForeground(6, this.builder.build());
    return 2;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/LocationSharingService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */