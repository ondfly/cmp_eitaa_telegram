package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Action.Builder;
import android.app.Notification.Builder;
import android.app.Notification.MediaStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata.Builder;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Callback;
import android.media.session.PlaybackState.Builder;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.widget.RemoteViews;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.ui.LaunchActivity;

public class MusicPlayerService
  extends Service
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int ID_NOTIFICATION = 5;
  public static final String NOTIFY_CLOSE = "org.telegram.android.musicplayer.close";
  public static final String NOTIFY_NEXT = "org.telegram.android.musicplayer.next";
  public static final String NOTIFY_PAUSE = "org.telegram.android.musicplayer.pause";
  public static final String NOTIFY_PLAY = "org.telegram.android.musicplayer.play";
  public static final String NOTIFY_PREVIOUS = "org.telegram.android.musicplayer.previous";
  private static boolean supportBigNotifications;
  private static boolean supportLockScreenControls;
  private Bitmap albumArtPlaceholder;
  private AudioManager audioManager;
  private MediaSession mediaSession;
  private PlaybackState.Builder playbackState;
  private RemoteControlClient remoteControlClient;
  
  static
  {
    boolean bool2 = true;
    if (Build.VERSION.SDK_INT >= 16)
    {
      bool1 = true;
      supportBigNotifications = bool1;
      if (Build.VERSION.SDK_INT >= 21) {
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
    paramMessageObject = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
    paramMessageObject.setAction("com.tmessages.openplayer");
    paramMessageObject.setFlags(32768);
    PendingIntent localPendingIntent1 = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, paramMessageObject, 0);
    if (Build.VERSION.SDK_INT >= 21) {
      if (localAudioInfo != null) {
        paramMessageObject = localAudioInfo.getSmallCover();
      }
    }
    for (;;)
    {
      Object localObject1;
      label89:
      boolean bool;
      label101:
      PendingIntent localPendingIntent2;
      Object localObject3;
      Object localObject2;
      label199:
      PendingIntent localPendingIntent4;
      PendingIntent localPendingIntent5;
      if (localAudioInfo != null)
      {
        localObject1 = localAudioInfo.getCover();
        if (MediaController.getInstance().isMessagePaused()) {
          break label674;
        }
        bool = true;
        localPendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("org.telegram.android.musicplayer.previous").setComponent(new ComponentName(this, MusicPlayerReceiver.class)), 268435456);
        PendingIntent localPendingIntent3 = PendingIntent.getService(getApplicationContext(), 0, new Intent(this, getClass()).setAction(getPackageName() + ".STOP_PLAYER"), 268435456);
        localObject3 = getApplicationContext();
        if (!bool) {
          break label680;
        }
        localObject2 = "org.telegram.android.musicplayer.pause";
        localPendingIntent4 = PendingIntent.getBroadcast((Context)localObject3, 0, new Intent((String)localObject2).setComponent(new ComponentName(this, MusicPlayerReceiver.class)), 268435456);
        localPendingIntent5 = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("org.telegram.android.musicplayer.next").setComponent(new ComponentName(this, MusicPlayerReceiver.class)), 268435456);
        localObject3 = new Notification.Builder(this);
        Notification.Builder localBuilder = ((Notification.Builder)localObject3).setSmallIcon(2131165607).setOngoing(bool).setContentTitle(str1).setContentText(str2);
        if (localAudioInfo == null) {
          break label687;
        }
        localObject2 = localAudioInfo.getAlbum();
        label311:
        localBuilder.setSubText((CharSequence)localObject2).setContentIntent(localPendingIntent1).setDeleteIntent(localPendingIntent3).setShowWhen(false).setCategory("transport").setPriority(2).setStyle(new Notification.MediaStyle().setMediaSession(this.mediaSession.getSessionToken()).setShowActionsInCompactView(new int[] { 0, 1, 2 }));
        if (Build.VERSION.SDK_INT >= 26) {
          ((Notification.Builder)localObject3).setChannelId("Other3");
        }
        if (paramMessageObject == null) {
          break label693;
        }
        ((Notification.Builder)localObject3).setLargeIcon(paramMessageObject);
        label407:
        if (!MediaController.getInstance().isDownloadingCurrentMessage()) {
          break label706;
        }
        this.playbackState.setState(6, 0L, 1.0F).setActions(0L);
        ((Notification.Builder)localObject3).addAction(new Notification.Action.Builder(2131165361, "", localPendingIntent2).build()).addAction(new Notification.Action.Builder(2131165471, "", null).build()).addAction(new Notification.Action.Builder(2131165358, "", localPendingIntent5).build());
        this.mediaSession.setPlaybackState(this.playbackState.build());
        localObject1 = new MediaMetadata.Builder().putBitmap("android.media.metadata.ALBUM_ART", (Bitmap)localObject1).putString("android.media.metadata.ALBUM_ARTIST", str2).putString("android.media.metadata.TITLE", str1);
        if (localAudioInfo == null) {
          break label851;
        }
        paramMessageObject = localAudioInfo.getAlbum();
        label555:
        paramMessageObject = ((MediaMetadata.Builder)localObject1).putString("android.media.metadata.ALBUM", paramMessageObject);
        this.mediaSession.setMetadata(paramMessageObject.build());
        ((Notification.Builder)localObject3).setVisibility(1);
        paramMessageObject = ((Notification.Builder)localObject3).build();
        if (!bool) {
          break label856;
        }
        startForeground(5, paramMessageObject);
        label600:
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
        localObject1 = null;
        break label89;
        label674:
        bool = false;
        break label101;
        label680:
        localObject2 = "org.telegram.android.musicplayer.play";
        break label199;
        label687:
        localObject2 = null;
        break label311;
        label693:
        ((Notification.Builder)localObject3).setLargeIcon(this.albumArtPlaceholder);
        break label407;
        label706:
        paramMessageObject = this.playbackState;
        label718:
        float f;
        if (bool)
        {
          i = 3;
          long l = MediaController.getInstance().getPlayingMessageObject().audioProgressSec;
          if (!bool) {
            break label839;
          }
          f = 1.0F;
          label737:
          paramMessageObject.setState(i, l * 1000L, f).setActions(566L);
          paramMessageObject = ((Notification.Builder)localObject3).addAction(new Notification.Action.Builder(2131165361, "", localPendingIntent2).build());
          if (!bool) {
            break label844;
          }
        }
        label839:
        label844:
        for (int i = 2131165359;; i = 2131165360)
        {
          paramMessageObject.addAction(new Notification.Action.Builder(i, "", localPendingIntent4).build()).addAction(new Notification.Action.Builder(2131165358, "", localPendingIntent5).build());
          break;
          i = 2;
          break label718;
          f = 0.0F;
          break label737;
        }
        label851:
        paramMessageObject = null;
        break label555;
        label856:
        stopForeground(false);
        ((NotificationManager)getSystemService("notification")).notify(5, paramMessageObject);
        break label600;
        localObject2 = new RemoteViews(getApplicationContext().getPackageName(), 2131361806);
        paramMessageObject = null;
        if (supportBigNotifications) {
          paramMessageObject = new RemoteViews(getApplicationContext().getPackageName(), 2131361805);
        }
        localObject1 = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(2131165607).setContentIntent(localPendingIntent1).setChannelId("Other3").setContentTitle(str1).build();
        ((Notification)localObject1).contentView = ((RemoteViews)localObject2);
        if (supportBigNotifications) {
          ((Notification)localObject1).bigContentView = paramMessageObject;
        }
        setListeners((RemoteViews)localObject2);
        if (supportBigNotifications) {
          setListeners(paramMessageObject);
        }
        if (localAudioInfo != null)
        {
          paramMessageObject = localAudioInfo.getSmallCover();
          label1007:
          if (paramMessageObject == null) {
            break label1307;
          }
          ((Notification)localObject1).contentView.setImageViewBitmap(2131230813, paramMessageObject);
          if (supportBigNotifications) {
            ((Notification)localObject1).bigContentView.setImageViewBitmap(2131230813, paramMessageObject);
          }
          label1041:
          if (!MediaController.getInstance().isDownloadingCurrentMessage()) {
            break label1344;
          }
          ((Notification)localObject1).contentView.setViewVisibility(2131230818, 8);
          ((Notification)localObject1).contentView.setViewVisibility(2131230819, 8);
          ((Notification)localObject1).contentView.setViewVisibility(2131230817, 8);
          ((Notification)localObject1).contentView.setViewVisibility(2131230820, 8);
          ((Notification)localObject1).contentView.setViewVisibility(2131230821, 0);
          if (supportBigNotifications)
          {
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230818, 8);
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230819, 8);
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230817, 8);
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230820, 8);
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230821, 0);
          }
          label1184:
          ((Notification)localObject1).contentView.setTextViewText(2131230822, str1);
          ((Notification)localObject1).contentView.setTextViewText(2131230815, str2);
          if (supportBigNotifications)
          {
            ((Notification)localObject1).bigContentView.setTextViewText(2131230822, str1);
            ((Notification)localObject1).bigContentView.setTextViewText(2131230815, str2);
            localObject2 = ((Notification)localObject1).bigContentView;
            if ((localAudioInfo == null) || (TextUtils.isEmpty(localAudioInfo.getAlbum()))) {
              break label1551;
            }
          }
        }
        label1307:
        label1344:
        label1551:
        for (paramMessageObject = localAudioInfo.getAlbum();; paramMessageObject = "")
        {
          ((RemoteViews)localObject2).setTextViewText(2131230814, paramMessageObject);
          ((Notification)localObject1).flags |= 0x2;
          startForeground(5, (Notification)localObject1);
          break;
          paramMessageObject = null;
          break label1007;
          ((Notification)localObject1).contentView.setImageViewResource(2131230813, 2131165541);
          if (!supportBigNotifications) {
            break label1041;
          }
          ((Notification)localObject1).bigContentView.setImageViewResource(2131230813, 2131165540);
          break label1041;
          ((Notification)localObject1).contentView.setViewVisibility(2131230821, 8);
          ((Notification)localObject1).contentView.setViewVisibility(2131230817, 0);
          ((Notification)localObject1).contentView.setViewVisibility(2131230820, 0);
          if (supportBigNotifications)
          {
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230817, 0);
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230820, 0);
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230821, 8);
          }
          if (MediaController.getInstance().isMessagePaused())
          {
            ((Notification)localObject1).contentView.setViewVisibility(2131230818, 8);
            ((Notification)localObject1).contentView.setViewVisibility(2131230819, 0);
            if (!supportBigNotifications) {
              break label1184;
            }
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230818, 8);
            ((Notification)localObject1).bigContentView.setViewVisibility(2131230819, 0);
            break label1184;
          }
          ((Notification)localObject1).contentView.setViewVisibility(2131230818, 0);
          ((Notification)localObject1).contentView.setViewVisibility(2131230819, 8);
          if (!supportBigNotifications) {
            break label1184;
          }
          ((Notification)localObject1).bigContentView.setViewVisibility(2131230818, 0);
          ((Notification)localObject1).bigContentView.setViewVisibility(2131230819, 8);
          break label1184;
        }
      }
      catch (Throwable localThrowable)
      {
        for (;;)
        {
          FileLog.e(localThrowable);
        }
      }
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.messagePlayingPlayStateChanged)
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
    int i = 0;
    while (i < 3)
    {
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
      i += 1;
    }
    if (Build.VERSION.SDK_INT >= 21)
    {
      this.mediaSession = new MediaSession(this, "telegramAudioPlayer");
      this.playbackState = new PlaybackState.Builder();
      this.albumArtPlaceholder = Bitmap.createBitmap(AndroidUtilities.dp(102.0F), AndroidUtilities.dp(102.0F), Bitmap.Config.ARGB_8888);
      Drawable localDrawable = getResources().getDrawable(2131165540);
      localDrawable.setBounds(0, 0, this.albumArtPlaceholder.getWidth(), this.albumArtPlaceholder.getHeight());
      localDrawable.draw(new Canvas(this.albumArtPlaceholder));
      this.mediaSession.setCallback(new MediaSession.Callback()
      {
        public void onPause()
        {
          MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
        }
        
        public void onPlay()
        {
          MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
        }
        
        public void onSkipToNext()
        {
          MediaController.getInstance().playNextMessage();
        }
        
        public void onSkipToPrevious()
        {
          MediaController.getInstance().playPreviousMessage();
        }
        
        public void onStop() {}
      });
      this.mediaSession.setActive(true);
    }
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
    if (Build.VERSION.SDK_INT >= 21) {
      this.mediaSession.release();
    }
    int i = 0;
    while (i < 3)
    {
      NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
      NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
      i += 1;
    }
  }
  
  @SuppressLint({"NewApi"})
  public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
  {
    if (paramIntent != null) {}
    try
    {
      if ((getPackageName() + ".STOP_PLAYER").equals(paramIntent.getAction()))
      {
        MediaController.getInstance().cleanupPlayer(true, true);
        return 2;
      }
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
    }
    catch (Exception paramIntent)
    {
      paramIntent.printStackTrace();
      return 1;
    }
    ComponentName localComponentName;
    if (supportLockScreenControls) {
      localComponentName = new ComponentName(getApplicationContext(), MusicPlayerReceiver.class.getName());
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
      for (;;)
      {
        FileLog.e(localException);
      }
    }
    createNotification(paramIntent);
    return 1;
  }
  
  public void setListeners(RemoteViews paramRemoteViews)
  {
    paramRemoteViews.setOnClickPendingIntent(2131230820, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("org.telegram.android.musicplayer.previous"), 134217728));
    paramRemoteViews.setOnClickPendingIntent(2131230816, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("org.telegram.android.musicplayer.close"), 134217728));
    paramRemoteViews.setOnClickPendingIntent(2131230818, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("org.telegram.android.musicplayer.pause"), 134217728));
    paramRemoteViews.setOnClickPendingIntent(2131230817, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("org.telegram.android.musicplayer.next"), 134217728));
    paramRemoteViews.setOnClickPendingIntent(2131230819, PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("org.telegram.android.musicplayer.play"), 134217728));
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/MusicPlayerService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */