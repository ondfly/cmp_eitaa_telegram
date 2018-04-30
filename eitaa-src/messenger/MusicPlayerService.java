package ir.eitaa.messenger;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;
import ir.eitaa.messenger.audioinfo.AudioInfo;
import ir.eitaa.ui.LaunchActivity;

public class MusicPlayerService
  extends Service
  implements NotificationCenter.NotificationCenterDelegate
{
  public static final String NOTIFY_CLOSE = "ir.eitaa.android.musicplayer.close";
  public static final String NOTIFY_NEXT = "ir.eitaa.android.musicplayer.next";
  public static final String NOTIFY_PAUSE = "ir.eitaa.android.musicplayer.pause";
  public static final String NOTIFY_PLAY = "ir.eitaa.android.musicplayer.play";
  public static final String NOTIFY_PREVIOUS = "ir.eitaa.android.musicplayer.previous";
  private static boolean supportBigNotifications;
  private static boolean supportLockScreenControls;
  private AudioManager audioManager;
  private RemoteControlClient remoteControlClient;
  
  static
  {
    boolean bool2 = true;
    if (Build.VERSION.SDK_INT >= 16)
    {
      bool1 = true;
      supportBigNotifications = bool1;
      if (Build.VERSION.SDK_INT < 14) {
        break label36;
      }
    }
    label36:
    for (boolean bool1 = bool2;; bool1 = false)
    {
      supportLockScreenControls = bool1;
      return;
      bool1 = false;
      break;
    }
  }
  
  @SuppressLint({"NewApi"})
  private void createNotification(MessageObject paramMessageObject)
  {
    String str1 = paramMessageObject.getMusicTitle();
    String str2 = paramMessageObject.getMusicAuthor();
    AudioInfo localAudioInfo = MediaController.getInstance().getAudioInfo();
    RemoteViews localRemoteViews = new RemoteViews(getApplicationContext().getPackageName(), 2130903047);
    paramMessageObject = null;
    if (supportBigNotifications) {
      paramMessageObject = new RemoteViews(getApplicationContext().getPackageName(), 2130903046);
    }
    Object localObject = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
    ((Intent)localObject).setAction("com.tmessages.openplayer");
    ((Intent)localObject).setFlags(32768);
    localObject = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, (Intent)localObject, 0);
    localObject = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(2130837937).setContentIntent((PendingIntent)localObject).setContentTitle(str1).build();
    ((Notification)localObject).contentView = localRemoteViews;
    if (supportBigNotifications) {
      ((Notification)localObject).bigContentView = paramMessageObject;
    }
    setListeners(localRemoteViews);
    if (supportBigNotifications) {
      setListeners(paramMessageObject);
    }
    if (localAudioInfo != null) {
      paramMessageObject = localAudioInfo.getSmallCover();
    }
    for (;;)
    {
      if (paramMessageObject != null)
      {
        ((Notification)localObject).contentView.setImageViewBitmap(2131492888, paramMessageObject);
        if (supportBigNotifications) {
          ((Notification)localObject).bigContentView.setImageViewBitmap(2131492888, paramMessageObject);
        }
        label212:
        if (!MediaController.getInstance().isDownloadingCurrentMessage()) {
          break label513;
        }
        ((Notification)localObject).contentView.setViewVisibility(2131492894, 8);
        ((Notification)localObject).contentView.setViewVisibility(2131492895, 8);
        ((Notification)localObject).contentView.setViewVisibility(2131492896, 8);
        ((Notification)localObject).contentView.setViewVisibility(2131492893, 8);
        ((Notification)localObject).contentView.setViewVisibility(2131492892, 0);
        if (supportBigNotifications)
        {
          ((Notification)localObject).bigContentView.setViewVisibility(2131492894, 8);
          ((Notification)localObject).bigContentView.setViewVisibility(2131492895, 8);
          ((Notification)localObject).bigContentView.setViewVisibility(2131492896, 8);
          ((Notification)localObject).bigContentView.setViewVisibility(2131492893, 8);
          ((Notification)localObject).bigContentView.setViewVisibility(2131492892, 0);
        }
        label345:
        ((Notification)localObject).contentView.setTextViewText(2131492889, str1);
        ((Notification)localObject).contentView.setTextViewText(2131492891, str2);
        if (supportBigNotifications)
        {
          ((Notification)localObject).bigContentView.setTextViewText(2131492889, str1);
          ((Notification)localObject).bigContentView.setTextViewText(2131492891, str2);
        }
        ((Notification)localObject).flags |= 0x2;
        startForeground(5, (Notification)localObject);
        if (this.remoteControlClient != null)
        {
          paramMessageObject = this.remoteControlClient.editMetadata(true);
          paramMessageObject.putString(2, str2);
          paramMessageObject.putString(7, str1);
          if ((localAudioInfo == null) || (localAudioInfo.getCover() == null)) {}
        }
      }
      try
      {
        paramMessageObject.putBitmap(100, localAudioInfo.getCover());
        paramMessageObject.apply();
        return;
        paramMessageObject = null;
        continue;
        ((Notification)localObject).contentView.setImageViewResource(2131492888, 2130837858);
        if (!supportBigNotifications) {
          break label212;
        }
        ((Notification)localObject).bigContentView.setImageViewResource(2131492888, 2130837857);
        break label212;
        label513:
        ((Notification)localObject).contentView.setViewVisibility(2131492892, 8);
        ((Notification)localObject).contentView.setViewVisibility(2131492896, 0);
        ((Notification)localObject).contentView.setViewVisibility(2131492893, 0);
        if (supportBigNotifications)
        {
          ((Notification)localObject).bigContentView.setViewVisibility(2131492896, 0);
          ((Notification)localObject).bigContentView.setViewVisibility(2131492893, 0);
          ((Notification)localObject).bigContentView.setViewVisibility(2131492892, 8);
        }
        if (MediaController.getInstance().isAudioPaused())
        {
          ((Notification)localObject).contentView.setViewVisibility(2131492894, 8);
          ((Notification)localObject).contentView.setViewVisibility(2131492895, 0);
          if (!supportBigNotifications) {
            break label345;
          }
          ((Notification)localObject).bigContentView.setViewVisibility(2131492894, 8);
          ((Notification)localObject).bigContentView.setViewVisibility(2131492895, 0);
          break label345;
        }
        ((Notification)localObject).contentView.setViewVisibility(2131492894, 0);
        ((Notification)localObject).contentView.setViewVisibility(2131492895, 8);
        if (!supportBigNotifications) {
          break label345;
        }
        ((Notification)localObject).bigContentView.setViewVisibility(2131492894, 0);
        ((Notification)localObject).bigContentView.setViewVisibility(2131492895, 8);
      }
      catch (Throwable localThrowable)
      {
        for (;;)
        {
          FileLog.e("TSMS", localThrowable);
        }
      }
    }
  }
  
  public void didReceivedNotification(int paramInt, Object... paramVarArgs)
  {
    if (paramInt == NotificationCenter.audioPlayStateChanged)
    {
      paramVarArgs = MediaController.getInstance().getPlayingMessageObject();
      if (paramVarArgs != null) {
        createNotification(paramVarArgs);
      }
    }
    else
    {
      return;
    }
    stopSelf();
  }
  
  public IBinder onBind(Intent paramIntent)
  {
    return null;
  }
  
  public void onCreate()
  {
    this.audioManager = ((AudioManager)getSystemService("audio"));
    NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioProgressDidChanged);
    NotificationCenter.getInstance().addObserver(this, NotificationCenter.audioPlayStateChanged);
    super.onCreate();
  }
  
  @SuppressLint({"NewApi"})
  public void onDestroy()
  {
    super.onDestroy();
    if (this.remoteControlClient != null)
    {
      RemoteControlClient.MetadataEditor localMetadataEditor = this.remoteControlClient.editMetadata(true);
      localMetadataEditor.clear();
      localMetadataEditor.apply();
      this.audioManager.unregisterRemoteControlClient(this.remoteControlClient);
    }
    NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioProgressDidChanged);
    NotificationCenter.getInstance().removeObserver(this, NotificationCenter.audioPlayStateChanged);
  }
  
  @SuppressLint({"NewApi"})
  public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
  {
    try
    {
      paramIntent = MediaController.getInstance().getPlayingMessageObject();
      if (paramIntent == null)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MusicPlayerService.this.stopSelf();
          }
        });
        return 1;
      }
      if (supportLockScreenControls) {
        localComponentName = new ComponentName(getApplicationContext(), MusicPlayerReceiver.class.getName());
      }
    }
    catch (Exception paramIntent)
    {
      ComponentName localComponentName;
      label125:
      paramIntent.printStackTrace();
      return 1;
    }
    try
    {
      if (this.remoteControlClient == null)
      {
        this.audioManager.registerMediaButtonEventReceiver(localComponentName);
        Intent localIntent = new Intent("android.intent.action.MEDIA_BUTTON");
        localIntent.setComponent(localComponentName);
        this.remoteControlClient = new RemoteControlClient(PendingIntent.getBroadcast(this, 0, localIntent, 0));
        this.audioManager.registerRemoteControlClient(this.remoteControlClient);
      }
      this.remoteControlClient.setTransportControlFlags(189);
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
      break label125;
    }
    createNotification(paramIntent);
    return 1;
  }
  
  public void setListeners(RemoteViews paramRemoteViews)
  {
    paramRemoteViews.setOnClickPendingIntent(2131492893, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("ir.eitaa.android.musicplayer.previous"), 134217728));
    paramRemoteViews.setOnClickPendingIntent(2131492890, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("ir.eitaa.android.musicplayer.close"), 134217728));
    paramRemoteViews.setOnClickPendingIntent(2131492894, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("ir.eitaa.android.musicplayer.pause"), 134217728));
    paramRemoteViews.setOnClickPendingIntent(2131492896, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("ir.eitaa.android.musicplayer.next"), 134217728));
    paramRemoteViews.setOnClickPendingIntent(2131492895, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("ir.eitaa.android.musicplayer.play"), 134217728));
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/MusicPlayerService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */