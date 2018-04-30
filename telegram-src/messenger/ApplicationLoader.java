package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import java.io.File;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.Components.ForegroundDetector;

public class ApplicationLoader
  extends Application
{
  @SuppressLint({"StaticFieldLeak"})
  public static volatile Context applicationContext;
  public static volatile Handler applicationHandler;
  private static volatile boolean applicationInited = false;
  public static volatile boolean isScreenOn = false;
  public static volatile boolean mainInterfacePaused = true;
  public static volatile boolean mainInterfacePausedStageQueue = true;
  public static volatile long mainInterfacePausedStageQueueTime;
  
  private boolean checkPlayServices()
  {
    try
    {
      int i = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
      return i == 0;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return true;
  }
  
  public static File getFilesDirFixed()
  {
    int i = 0;
    File localFile;
    while (i < 10)
    {
      localFile = applicationContext.getFilesDir();
      if (localFile != null) {
        return localFile;
      }
      i += 1;
    }
    try
    {
      localFile = new File(applicationContext.getApplicationInfo().dataDir, "files");
      localFile.mkdirs();
      return localFile;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return new File("/data/data/org.telegram.messenger/files");
  }
  
  private void initPlayServices()
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (ApplicationLoader.this.checkPlayServices())
        {
          str = SharedConfig.pushString;
          if (!TextUtils.isEmpty(str)) {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("GCM regId = " + str);
            }
          }
        }
        while (!BuildVars.LOGS_ENABLED)
        {
          String str;
          for (;;)
          {
            Utilities.globalQueue.postRunnable(new Runnable()
            {
              public void run()
              {
                try
                {
                  String str = FirebaseInstanceId.getInstance().getToken();
                  if (!TextUtils.isEmpty(str)) {
                    GcmInstanceIDListenerService.sendRegistrationToServer(str);
                  }
                  return;
                }
                catch (Throwable localThrowable)
                {
                  FileLog.e(localThrowable);
                }
              }
            });
            return;
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("GCM Registration not found.");
            }
          }
        }
        FileLog.d("No valid Google Play Services APK found.");
      }
    }, 1000L);
  }
  
  public static void postInitApplication()
  {
    if (applicationInited) {
      return;
    }
    applicationInited = true;
    try
    {
      LocaleController.getInstance();
    }
    catch (Exception localException2)
    {
      try
      {
        localObject = new IntentFilter("android.intent.action.SCREEN_ON");
        ((IntentFilter)localObject).addAction("android.intent.action.SCREEN_OFF");
        ScreenReceiver localScreenReceiver = new ScreenReceiver();
        applicationContext.registerReceiver(localScreenReceiver, (IntentFilter)localObject);
      }
      catch (Exception localException2)
      {
        try
        {
          for (;;)
          {
            Object localObject;
            isScreenOn = ((PowerManager)applicationContext.getSystemService("power")).isScreenOn();
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("screen state = " + isScreenOn);
            }
            SharedConfig.loadConfig();
            i = 0;
            while (i < 3)
            {
              UserConfig.getInstance(i).loadConfig();
              MessagesController.getInstance(i);
              ConnectionsManager.getInstance(i);
              localObject = UserConfig.getInstance(i).getCurrentUser();
              if (localObject != null)
              {
                MessagesController.getInstance(i).putUser((TLRPC.User)localObject, true);
                MessagesController.getInstance(i).getBlockedUsers(true);
                SendMessagesHelper.getInstance(i).checkUnsentMessages();
              }
              i += 1;
            }
            localException1 = localException1;
            localException1.printStackTrace();
            continue;
            localException2 = localException2;
            localException2.printStackTrace();
          }
        }
        catch (Exception localException3)
        {
          for (;;)
          {
            FileLog.e(localException3);
          }
          ((ApplicationLoader)applicationContext).initPlayServices();
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("app initied");
          }
          MediaController.getInstance();
          int i = 0;
          while (i < 3)
          {
            ContactsController.getInstance(i).checkAppAccount();
            DownloadController.getInstance(i);
            i += 1;
          }
          WearDataLayerListenerService.updateWatchConnectionState();
        }
      }
    }
  }
  
  public static void startPushService()
  {
    if (MessagesController.getGlobalNotificationsSettings().getBoolean("pushService", true)) {
      try
      {
        applicationContext.startService(new Intent(applicationContext, NotificationsService.class));
        return;
      }
      catch (Throwable localThrowable)
      {
        FileLog.e(localThrowable);
        return;
      }
    }
    stopPushService();
  }
  
  public static void stopPushService()
  {
    applicationContext.stopService(new Intent(applicationContext, NotificationsService.class));
    PendingIntent localPendingIntent = PendingIntent.getService(applicationContext, 0, new Intent(applicationContext, NotificationsService.class), 0);
    ((AlarmManager)applicationContext.getSystemService("alarm")).cancel(localPendingIntent);
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    try
    {
      LocaleController.getInstance().onDeviceConfigurationChange(paramConfiguration);
      AndroidUtilities.checkDisplaySize(applicationContext, paramConfiguration);
      return;
    }
    catch (Exception paramConfiguration)
    {
      paramConfiguration.printStackTrace();
    }
  }
  
  public void onCreate()
  {
    super.onCreate();
    applicationContext = getApplicationContext();
    NativeLoader.initNativeLibs(applicationContext);
    ConnectionsManager.native_setJava(false);
    new ForegroundDetector(this);
    applicationHandler = new Handler(applicationContext.getMainLooper());
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run() {}
    });
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/ApplicationLoader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */