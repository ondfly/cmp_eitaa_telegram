package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.Action.Builder;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.CarExtender;
import android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation.Builder;
import android.support.v4.app.NotificationCompat.Extender;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.app.NotificationCompat.MessagingStyle;
import android.support.v4.app.NotificationCompat.Style;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.RemoteInput.Builder;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.ReplyMarkup;
import org.telegram.tgnet.TLRPC.TL_account_updateNotifySettings;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_game;
import org.telegram.tgnet.TLRPC.TL_inputNotifyPeer;
import org.telegram.tgnet.TLRPC.TL_inputPeerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRow;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelMigrateFrom;
import org.telegram.tgnet.TLRPC.TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeletePhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditPhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditTitle;
import org.telegram.tgnet.TLRPC.TL_messageActionChatJoinedByLink;
import org.telegram.tgnet.TLRPC.TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC.TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionGameScore;
import org.telegram.tgnet.TLRPC.TL_messageActionLoginUnknownLocation;
import org.telegram.tgnet.TLRPC.TL_messageActionPaymentSent;
import org.telegram.tgnet.TLRPC.TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC.TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC.TL_messageActionScreenshotTaken;
import org.telegram.tgnet.TLRPC.TL_messageActionUserJoined;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PopupNotificationActivity;

public class NotificationsController
{
  public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
  private static volatile NotificationsController[] Instance = new NotificationsController[3];
  public static final String OTHER_NOTIFICATIONS_CHANNEL = "Other3";
  protected static AudioManager audioManager;
  public static long lastNoDataNotificationTime;
  private static NotificationManagerCompat notificationManager;
  private static DispatchQueue notificationsQueue = new DispatchQueue("notificationsQueue");
  private static NotificationManager systemNotificationManager;
  private AlarmManager alarmManager;
  private int currentAccount;
  private ArrayList<MessageObject> delayedPushMessages = new ArrayList();
  private boolean inChatSoundEnabled = true;
  private int lastBadgeCount = -1;
  private int lastButtonId = 5000;
  private boolean lastNotificationIsNoData;
  private int lastOnlineFromOtherDevice = 0;
  private long lastSoundOutPlay;
  private long lastSoundPlay;
  private LongSparseArray<Integer> lastWearNotifiedMessageId = new LongSparseArray();
  private String launcherClassName;
  private Runnable notificationDelayRunnable;
  private PowerManager.WakeLock notificationDelayWakelock;
  private String notificationGroup;
  private int notificationId;
  private boolean notifyCheck = false;
  private long opened_dialog_id = 0L;
  private int personal_count = 0;
  public ArrayList<MessageObject> popupMessages = new ArrayList();
  public ArrayList<MessageObject> popupReplyMessages = new ArrayList();
  private LongSparseArray<Integer> pushDialogs = new LongSparseArray();
  private LongSparseArray<Integer> pushDialogsOverrideMention = new LongSparseArray();
  private ArrayList<MessageObject> pushMessages = new ArrayList();
  private LongSparseArray<MessageObject> pushMessagesDict = new LongSparseArray();
  public boolean showBadgeNumber;
  private LongSparseArray<Point> smartNotificationsDialogs = new LongSparseArray();
  private int soundIn;
  private boolean soundInLoaded;
  private int soundOut;
  private boolean soundOutLoaded;
  private SoundPool soundPool;
  private int soundRecord;
  private boolean soundRecordLoaded;
  private int total_unread_count = 0;
  private LongSparseArray<Integer> wearNotificationsIds = new LongSparseArray();
  
  static
  {
    notificationManager = null;
    systemNotificationManager = null;
    if ((Build.VERSION.SDK_INT >= 26) && (ApplicationLoader.applicationContext != null))
    {
      notificationManager = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
      systemNotificationManager = (NotificationManager)ApplicationLoader.applicationContext.getSystemService("notification");
      NotificationChannel localNotificationChannel = new NotificationChannel("Other3", "Other", 3);
      localNotificationChannel.enableLights(false);
      localNotificationChannel.enableVibration(false);
      localNotificationChannel.setSound(null, null);
      systemNotificationManager.createNotificationChannel(localNotificationChannel);
    }
    audioManager = (AudioManager)ApplicationLoader.applicationContext.getSystemService("audio");
  }
  
  public NotificationsController(int paramInt)
  {
    this.currentAccount = paramInt;
    this.notificationId = (this.currentAccount + 1);
    StringBuilder localStringBuilder = new StringBuilder().append("messages");
    Object localObject;
    if (this.currentAccount == 0) {
      localObject = "";
    }
    for (;;)
    {
      this.notificationGroup = localObject;
      localObject = MessagesController.getNotificationsSettings(this.currentAccount);
      this.inChatSoundEnabled = ((SharedPreferences)localObject).getBoolean("EnableInChatSound", true);
      this.showBadgeNumber = ((SharedPreferences)localObject).getBoolean("badgeNumber", true);
      notificationManager = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
      systemNotificationManager = (NotificationManager)ApplicationLoader.applicationContext.getSystemService("notification");
      try
      {
        audioManager = (AudioManager)ApplicationLoader.applicationContext.getSystemService("audio");
      }
      catch (Exception localException2)
      {
        try
        {
          this.alarmManager = ((AlarmManager)ApplicationLoader.applicationContext.getSystemService("alarm"));
        }
        catch (Exception localException2)
        {
          try
          {
            for (;;)
            {
              this.notificationDelayWakelock = ((PowerManager)ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(1, "lock");
              this.notificationDelayWakelock.setReferenceCounted(false);
              this.notificationDelayRunnable = new Runnable()
              {
                public void run()
                {
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("delay reached");
                  }
                  if (!NotificationsController.this.delayedPushMessages.isEmpty())
                  {
                    NotificationsController.this.showOrUpdateNotification(true);
                    NotificationsController.this.delayedPushMessages.clear();
                  }
                  for (;;)
                  {
                    try
                    {
                      if (NotificationsController.this.notificationDelayWakelock.isHeld()) {
                        NotificationsController.this.notificationDelayWakelock.release();
                      }
                      return;
                    }
                    catch (Exception localException)
                    {
                      FileLog.e(localException);
                    }
                    if (NotificationsController.this.lastNotificationIsNoData) {
                      NotificationsController.notificationManager.cancel(NotificationsController.this.notificationId);
                    }
                  }
                }
              };
              return;
              localObject = Integer.valueOf(this.currentAccount);
              break;
              localException1 = localException1;
              FileLog.e(localException1);
              continue;
              localException2 = localException2;
              FileLog.e(localException2);
            }
          }
          catch (Exception localException3)
          {
            for (;;)
            {
              FileLog.e(localException3);
            }
          }
        }
      }
    }
  }
  
  /* Error */
  private void dismissNotification()
  {
    // Byte code:
    //   0: aload_0
    //   1: iconst_0
    //   2: putfield 371	org/telegram/messenger/NotificationsController:lastNotificationIsNoData	Z
    //   5: getstatic 147	org/telegram/messenger/NotificationsController:notificationManager	Landroid/support/v4/app/NotificationManagerCompat;
    //   8: aload_0
    //   9: getfield 258	org/telegram/messenger/NotificationsController:notificationId	I
    //   12: invokevirtual 449	android/support/v4/app/NotificationManagerCompat:cancel	(I)V
    //   15: aload_0
    //   16: getfield 217	org/telegram/messenger/NotificationsController:pushMessages	Ljava/util/ArrayList;
    //   19: invokevirtual 452	java/util/ArrayList:clear	()V
    //   22: aload_0
    //   23: getfield 224	org/telegram/messenger/NotificationsController:pushMessagesDict	Landroid/util/LongSparseArray;
    //   26: invokevirtual 453	android/util/LongSparseArray:clear	()V
    //   29: aload_0
    //   30: getfield 232	org/telegram/messenger/NotificationsController:lastWearNotifiedMessageId	Landroid/util/LongSparseArray;
    //   33: invokevirtual 453	android/util/LongSparseArray:clear	()V
    //   36: iconst_0
    //   37: istore_1
    //   38: iload_1
    //   39: aload_0
    //   40: getfield 230	org/telegram/messenger/NotificationsController:wearNotificationsIds	Landroid/util/LongSparseArray;
    //   43: invokevirtual 456	android/util/LongSparseArray:size	()I
    //   46: if_icmpge +30 -> 76
    //   49: getstatic 147	org/telegram/messenger/NotificationsController:notificationManager	Landroid/support/v4/app/NotificationManagerCompat;
    //   52: aload_0
    //   53: getfield 230	org/telegram/messenger/NotificationsController:wearNotificationsIds	Landroid/util/LongSparseArray;
    //   56: iload_1
    //   57: invokevirtual 460	android/util/LongSparseArray:valueAt	(I)Ljava/lang/Object;
    //   60: checkcast 326	java/lang/Integer
    //   63: invokevirtual 463	java/lang/Integer:intValue	()I
    //   66: invokevirtual 449	android/support/v4/app/NotificationManagerCompat:cancel	(I)V
    //   69: iload_1
    //   70: iconst_1
    //   71: iadd
    //   72: istore_1
    //   73: goto -35 -> 38
    //   76: aload_0
    //   77: getfield 230	org/telegram/messenger/NotificationsController:wearNotificationsIds	Landroid/util/LongSparseArray;
    //   80: invokevirtual 453	android/util/LongSparseArray:clear	()V
    //   83: new 20	org/telegram/messenger/NotificationsController$13
    //   86: dup
    //   87: aload_0
    //   88: invokespecial 464	org/telegram/messenger/NotificationsController$13:<init>	(Lorg/telegram/messenger/NotificationsController;)V
    //   91: invokestatic 470	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
    //   94: invokestatic 476	org/telegram/messenger/WearDataLayerListenerService:isWatchConnected	()Z
    //   97: istore_2
    //   98: iload_2
    //   99: ifeq +57 -> 156
    //   102: new 478	org/json/JSONObject
    //   105: dup
    //   106: invokespecial 479	org/json/JSONObject:<init>	()V
    //   109: astore_3
    //   110: aload_3
    //   111: ldc_w 481
    //   114: aload_0
    //   115: getfield 256	org/telegram/messenger/NotificationsController:currentAccount	I
    //   118: invokestatic 487	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   121: invokevirtual 490	org/telegram/messenger/UserConfig:getClientUserId	()I
    //   124: invokevirtual 494	org/json/JSONObject:put	(Ljava/lang/String;I)Lorg/json/JSONObject;
    //   127: pop
    //   128: aload_3
    //   129: ldc_w 496
    //   132: iconst_1
    //   133: invokevirtual 499	org/json/JSONObject:put	(Ljava/lang/String;Z)Lorg/json/JSONObject;
    //   136: pop
    //   137: ldc_w 501
    //   140: aload_3
    //   141: invokevirtual 502	org/json/JSONObject:toString	()Ljava/lang/String;
    //   144: ldc_w 504
    //   147: invokevirtual 510	java/lang/String:getBytes	(Ljava/lang/String;)[B
    //   150: ldc_w 512
    //   153: invokestatic 516	org/telegram/messenger/WearDataLayerListenerService:sendMessageToWatch	(Ljava/lang/String;[BLjava/lang/String;)V
    //   156: return
    //   157: astore_3
    //   158: aload_3
    //   159: invokestatic 336	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   162: return
    //   163: astore_3
    //   164: return
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	165	0	this	NotificationsController
    //   37	36	1	i	int
    //   97	2	2	bool	boolean
    //   109	32	3	localJSONObject	JSONObject
    //   157	2	3	localException	Exception
    //   163	1	3	localJSONException	JSONException
    // Exception table:
    //   from	to	target	type
    //   0	36	157	java/lang/Exception
    //   38	69	157	java/lang/Exception
    //   76	98	157	java/lang/Exception
    //   102	156	157	java/lang/Exception
    //   102	156	163	org/json/JSONException
  }
  
  public static NotificationsController getInstance(int paramInt)
  {
    Object localObject1 = Instance[paramInt];
    if (localObject1 == null) {}
    try
    {
      Object localObject3 = Instance[paramInt];
      localObject1 = localObject3;
      if (localObject3 == null)
      {
        localObject3 = Instance;
        localObject1 = new NotificationsController(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (NotificationsController)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (NotificationsController)localObject1;
  }
  
  private int getNotifyOverride(SharedPreferences paramSharedPreferences, long paramLong)
  {
    int j = paramSharedPreferences.getInt("notify2_" + paramLong, 0);
    int i = j;
    if (j == 3)
    {
      i = j;
      if (paramSharedPreferences.getInt("notifyuntil_" + paramLong, 0) >= ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()) {
        i = 2;
      }
    }
    return i;
  }
  
  private String getShortStringForMessage(MessageObject paramMessageObject)
  {
    if ((!paramMessageObject.isMediaEmpty()) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
    {
      if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) {
        return "🖼 " + paramMessageObject.messageOwner.message;
      }
      if (paramMessageObject.isVideo()) {
        return "📹 " + paramMessageObject.messageOwner.message;
      }
      if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument))
      {
        if (paramMessageObject.isGif()) {
          return "🎬 " + paramMessageObject.messageOwner.message;
        }
        return "📎 " + paramMessageObject.messageOwner.message;
      }
    }
    return paramMessageObject.messageText.toString();
  }
  
  private String getStringForMessage(MessageObject paramMessageObject, boolean paramBoolean, boolean[] paramArrayOfBoolean)
  {
    if ((AndroidUtilities.needShowPasscode(false)) || (SharedConfig.isWaitingForPasscodeEnter)) {
      return LocaleController.getString("YouHaveNewMessage", 2131494657);
    }
    long l2 = paramMessageObject.messageOwner.dialog_id;
    if (paramMessageObject.messageOwner.to_id.chat_id != 0) {}
    int k;
    for (int j = paramMessageObject.messageOwner.to_id.chat_id;; j = paramMessageObject.messageOwner.to_id.channel_id)
    {
      k = paramMessageObject.messageOwner.to_id.user_id;
      if (!paramMessageObject.isFcmMessage()) {
        break label246;
      }
      if ((j != 0) || (k == 0)) {
        break;
      }
      if (MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewAll", true)) {
        break label234;
      }
      return LocaleController.formatString("NotificationMessageNoText", 2131493993, new Object[] { paramMessageObject.localName });
    }
    if ((j != 0) && (!MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewGroup", true)))
    {
      if ((!paramMessageObject.isMegagroup()) && (paramMessageObject.messageOwner.to_id.channel_id != 0)) {
        return LocaleController.formatString("ChannelMessageNoText", 2131493187, new Object[] { paramMessageObject.localName });
      }
      return LocaleController.formatString("NotificationMessageGroupNoText", 2131493982, new Object[] { paramMessageObject.localUserName, paramMessageObject.localName });
    }
    label234:
    paramArrayOfBoolean[0] = true;
    return (String)paramMessageObject.messageText;
    label246:
    int i;
    long l1;
    label296:
    String str;
    Object localObject1;
    if (k == 0) {
      if ((paramMessageObject.isFromUser()) || (paramMessageObject.getId() < 0))
      {
        i = paramMessageObject.messageOwner.from_id;
        l1 = l2;
        if (l2 == 0L)
        {
          if (j == 0) {
            break label379;
          }
          l1 = -j;
        }
        str = null;
        if (i <= 0) {
          break label396;
        }
        localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
        if (localObject1 != null) {
          str = UserObject.getUserName((TLRPC.User)localObject1);
        }
      }
    }
    for (;;)
    {
      if (str != null) {
        break label429;
      }
      return null;
      i = -j;
      break;
      i = k;
      if (k != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
        break;
      }
      i = paramMessageObject.messageOwner.from_id;
      break;
      label379:
      l1 = l2;
      if (i == 0) {
        break label296;
      }
      l1 = i;
      break label296;
      label396:
      localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
      if (localObject1 != null) {
        str = ((TLRPC.Chat)localObject1).title;
      }
    }
    label429:
    Object localObject2 = null;
    if (j != 0)
    {
      localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(j));
      localObject2 = localObject1;
      if (localObject1 == null) {
        return null;
      }
    }
    Object localObject3 = null;
    if ((int)l1 == 0) {
      localObject1 = LocaleController.getString("YouHaveNewMessage", 2131494657);
    }
    for (;;)
    {
      return (String)localObject1;
      if ((j == 0) && (i != 0))
      {
        if (MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewAll", true))
        {
          if ((paramMessageObject.messageOwner instanceof TLRPC.TL_messageService))
          {
            if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionUserJoined))
            {
              localObject1 = LocaleController.formatString("NotificationContactJoined", 2131493952, new Object[] { str });
            }
            else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto))
            {
              localObject1 = LocaleController.formatString("NotificationContactNewPhoto", 2131493953, new Object[] { str });
            }
            else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionLoginUnknownLocation))
            {
              paramArrayOfBoolean = LocaleController.formatString("formatDateAtTime", 2131494696, new Object[] { LocaleController.getInstance().formatterYear.format(paramMessageObject.messageOwner.date * 1000L), LocaleController.getInstance().formatterDay.format(paramMessageObject.messageOwner.date * 1000L) });
              localObject1 = LocaleController.formatString("NotificationUnrecognizedDevice", 2131494003, new Object[] { UserConfig.getInstance(this.currentAccount).getCurrentUser().first_name, paramArrayOfBoolean, paramMessageObject.messageOwner.action.title, paramMessageObject.messageOwner.action.address });
            }
            else if (((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionGameScore)) || ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionPaymentSent)))
            {
              localObject1 = paramMessageObject.messageText.toString();
            }
            else
            {
              localObject1 = localObject3;
              if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionPhoneCall))
              {
                paramArrayOfBoolean = paramMessageObject.messageOwner.action.reason;
                localObject1 = localObject3;
                if (!paramMessageObject.isOut())
                {
                  localObject1 = localObject3;
                  if ((paramArrayOfBoolean instanceof TLRPC.TL_phoneCallDiscardReasonMissed)) {
                    localObject1 = LocaleController.getString("CallMessageIncomingMissed", 2131493111);
                  }
                }
              }
            }
          }
          else if (paramMessageObject.isMediaEmpty())
          {
            if (!paramBoolean)
            {
              if (!TextUtils.isEmpty(paramMessageObject.messageOwner.message))
              {
                localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, paramMessageObject.messageOwner.message });
                paramArrayOfBoolean[0] = true;
              }
              else
              {
                localObject1 = LocaleController.formatString("NotificationMessageNoText", 2131493993, new Object[] { str });
              }
            }
            else {
              localObject1 = LocaleController.formatString("NotificationMessageNoText", 2131493993, new Object[] { str });
            }
          }
          else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
          {
            if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
            {
              localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, "🖼 " + paramMessageObject.messageOwner.message });
              paramArrayOfBoolean[0] = true;
            }
            else if (paramMessageObject.messageOwner.media.ttl_seconds != 0)
            {
              localObject1 = LocaleController.formatString("NotificationMessageSDPhoto", 2131493996, new Object[] { str });
            }
            else
            {
              localObject1 = LocaleController.formatString("NotificationMessagePhoto", 2131493994, new Object[] { str });
            }
          }
          else if (paramMessageObject.isVideo())
          {
            if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
            {
              localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, "📹 " + paramMessageObject.messageOwner.message });
              paramArrayOfBoolean[0] = true;
            }
            else if (paramMessageObject.messageOwner.media.ttl_seconds != 0)
            {
              localObject1 = LocaleController.formatString("NotificationMessageSDVideo", 2131493997, new Object[] { str });
            }
            else
            {
              localObject1 = LocaleController.formatString("NotificationMessageVideo", 2131494001, new Object[] { str });
            }
          }
          else if (paramMessageObject.isGame())
          {
            localObject1 = LocaleController.formatString("NotificationMessageGame", 2131493971, new Object[] { str, paramMessageObject.messageOwner.media.game.title });
          }
          else if (paramMessageObject.isVoice())
          {
            localObject1 = LocaleController.formatString("NotificationMessageAudio", 2131493966, new Object[] { str });
          }
          else if (paramMessageObject.isRoundVideo())
          {
            localObject1 = LocaleController.formatString("NotificationMessageRound", 2131493995, new Object[] { str });
          }
          else if (paramMessageObject.isMusic())
          {
            localObject1 = LocaleController.formatString("NotificationMessageMusic", 2131493992, new Object[] { str });
          }
          else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact))
          {
            localObject1 = LocaleController.formatString("NotificationMessageContact", 2131493967, new Object[] { str });
          }
          else if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVenue)))
          {
            localObject1 = LocaleController.formatString("NotificationMessageMap", 2131493991, new Object[] { str });
          }
          else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
          {
            localObject1 = LocaleController.formatString("NotificationMessageLiveLocation", 2131493990, new Object[] { str });
          }
          else
          {
            localObject1 = localObject3;
            if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument)) {
              if (paramMessageObject.isSticker())
              {
                paramMessageObject = paramMessageObject.getStickerEmoji();
                if (paramMessageObject != null) {
                  localObject1 = LocaleController.formatString("NotificationMessageStickerEmoji", 2131493999, new Object[] { str, paramMessageObject });
                } else {
                  localObject1 = LocaleController.formatString("NotificationMessageSticker", 2131493998, new Object[] { str });
                }
              }
              else if (paramMessageObject.isGif())
              {
                if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                {
                  localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, "🎬 " + paramMessageObject.messageOwner.message });
                  paramArrayOfBoolean[0] = true;
                }
                else
                {
                  localObject1 = LocaleController.formatString("NotificationMessageGif", 2131493972, new Object[] { str });
                }
              }
              else if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
              {
                localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, "📎 " + paramMessageObject.messageOwner.message });
                paramArrayOfBoolean[0] = true;
              }
              else
              {
                localObject1 = LocaleController.formatString("NotificationMessageDocument", 2131493968, new Object[] { str });
              }
            }
          }
        }
        else {
          localObject1 = LocaleController.formatString("NotificationMessageNoText", 2131493993, new Object[] { str });
        }
      }
      else
      {
        localObject1 = localObject3;
        if (j != 0) {
          if (MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("EnablePreviewGroup", true))
          {
            if ((paramMessageObject.messageOwner instanceof TLRPC.TL_messageService))
            {
              if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatAddUser))
              {
                k = paramMessageObject.messageOwner.action.user_id;
                j = k;
                if (k == 0)
                {
                  j = k;
                  if (paramMessageObject.messageOwner.action.users.size() == 1) {
                    j = ((Integer)paramMessageObject.messageOwner.action.users.get(0)).intValue();
                  }
                }
                if (j != 0)
                {
                  if ((paramMessageObject.messageOwner.to_id.channel_id != 0) && (!((TLRPC.Chat)localObject2).megagroup))
                  {
                    localObject1 = LocaleController.formatString("ChannelAddedByNotification", 2131493148, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                  }
                  else if (j == UserConfig.getInstance(this.currentAccount).getClientUserId())
                  {
                    localObject1 = LocaleController.formatString("NotificationInvitedToGroup", 2131493964, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                  }
                  else
                  {
                    paramMessageObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(j));
                    if (paramMessageObject == null) {
                      return null;
                    }
                    if (i == paramMessageObject.id)
                    {
                      if (((TLRPC.Chat)localObject2).megagroup) {
                        localObject1 = LocaleController.formatString("NotificationGroupAddSelfMega", 2131493958, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                      } else {
                        localObject1 = LocaleController.formatString("NotificationGroupAddSelf", 2131493957, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                      }
                    }
                    else {
                      localObject1 = LocaleController.formatString("NotificationGroupAddMember", 2131493956, new Object[] { str, ((TLRPC.Chat)localObject2).title, UserObject.getUserName(paramMessageObject) });
                    }
                  }
                }
                else
                {
                  paramArrayOfBoolean = new StringBuilder("");
                  i = 0;
                  while (i < paramMessageObject.messageOwner.action.users.size())
                  {
                    localObject1 = MessagesController.getInstance(this.currentAccount).getUser((Integer)paramMessageObject.messageOwner.action.users.get(i));
                    if (localObject1 != null)
                    {
                      localObject1 = UserObject.getUserName((TLRPC.User)localObject1);
                      if (paramArrayOfBoolean.length() != 0) {
                        paramArrayOfBoolean.append(", ");
                      }
                      paramArrayOfBoolean.append((String)localObject1);
                    }
                    i += 1;
                  }
                  localObject1 = LocaleController.formatString("NotificationGroupAddMember", 2131493956, new Object[] { str, ((TLRPC.Chat)localObject2).title, paramArrayOfBoolean.toString() });
                }
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatJoinedByLink))
              {
                localObject1 = LocaleController.formatString("NotificationInvitedToGroupByLink", 2131493965, new Object[] { str, ((TLRPC.Chat)localObject2).title });
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatEditTitle))
              {
                localObject1 = LocaleController.formatString("NotificationEditedGroupName", 2131493954, new Object[] { str, paramMessageObject.messageOwner.action.title });
              }
              else if (((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatEditPhoto)) || ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatDeletePhoto)))
              {
                if ((paramMessageObject.messageOwner.to_id.channel_id != 0) && (!((TLRPC.Chat)localObject2).megagroup)) {
                  localObject1 = LocaleController.formatString("ChannelPhotoEditNotification", 2131493196, new Object[] { ((TLRPC.Chat)localObject2).title });
                } else {
                  localObject1 = LocaleController.formatString("NotificationEditedGroupPhoto", 2131493955, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                }
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatDeleteUser))
              {
                if (paramMessageObject.messageOwner.action.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId())
                {
                  localObject1 = LocaleController.formatString("NotificationGroupKickYou", 2131493962, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                }
                else if (paramMessageObject.messageOwner.action.user_id == i)
                {
                  localObject1 = LocaleController.formatString("NotificationGroupLeftMember", 2131493963, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                }
                else
                {
                  paramMessageObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramMessageObject.messageOwner.action.user_id));
                  if (paramMessageObject == null) {
                    return null;
                  }
                  localObject1 = LocaleController.formatString("NotificationGroupKickMember", 2131493961, new Object[] { str, ((TLRPC.Chat)localObject2).title, UserObject.getUserName(paramMessageObject) });
                }
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatCreate))
              {
                localObject1 = paramMessageObject.messageText.toString();
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChannelCreate))
              {
                localObject1 = paramMessageObject.messageText.toString();
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChatMigrateTo))
              {
                localObject1 = LocaleController.formatString("ActionMigrateFromGroupNotify", 2131492891, new Object[] { ((TLRPC.Chat)localObject2).title });
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionChannelMigrateFrom))
              {
                localObject1 = LocaleController.formatString("ActionMigrateFromGroupNotify", 2131492891, new Object[] { paramMessageObject.messageOwner.action.title });
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionScreenshotTaken))
              {
                localObject1 = paramMessageObject.messageText.toString();
              }
              else if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionPinMessage))
              {
                if ((localObject2 != null) && (((TLRPC.Chat)localObject2).megagroup))
                {
                  if (paramMessageObject.replyMessageObject == null)
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedNoText", 2131493936, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                  }
                  else
                  {
                    paramMessageObject = paramMessageObject.replyMessageObject;
                    if (paramMessageObject.isMusic())
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedMusic", 2131493934, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                    }
                    else if (paramMessageObject.isVideo())
                    {
                      if ((Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message))) {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedText", 2131493946, new Object[] { str, "📹 " + paramMessageObject.messageOwner.message, ((TLRPC.Chat)localObject2).title });
                      } else {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedVideo", 2131493948, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                      }
                    }
                    else if (paramMessageObject.isGif())
                    {
                      if ((Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message))) {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedText", 2131493946, new Object[] { str, "🎬 " + paramMessageObject.messageOwner.message, ((TLRPC.Chat)localObject2).title });
                      } else {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedGif", 2131493930, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                      }
                    }
                    else if (paramMessageObject.isVoice())
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedVoice", 2131493950, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                    }
                    else if (paramMessageObject.isRoundVideo())
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedRound", 2131493940, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                    }
                    else if (paramMessageObject.isSticker())
                    {
                      paramMessageObject = paramMessageObject.getStickerEmoji();
                      if (paramMessageObject != null) {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedStickerEmoji", 2131493944, new Object[] { str, ((TLRPC.Chat)localObject2).title, paramMessageObject });
                      } else {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedSticker", 2131493942, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                      }
                    }
                    else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument))
                    {
                      if ((Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message))) {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedText", 2131493946, new Object[] { str, "📎 " + paramMessageObject.messageOwner.message, ((TLRPC.Chat)localObject2).title });
                      } else {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedFile", 2131493922, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                      }
                    }
                    else if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVenue)))
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedGeo", 2131493926, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                    }
                    else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedGeoLive", 2131493928, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                    }
                    else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact))
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedContact", 2131493920, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                    }
                    else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
                    {
                      if ((Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message))) {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedText", 2131493946, new Object[] { str, "🖼 " + paramMessageObject.messageOwner.message, ((TLRPC.Chat)localObject2).title });
                      } else {
                        localObject1 = LocaleController.formatString("NotificationActionPinnedPhoto", 2131493938, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                      }
                    }
                    else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame))
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedGame", 2131493924, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                    }
                    else if ((paramMessageObject.messageText != null) && (paramMessageObject.messageText.length() > 0))
                    {
                      paramArrayOfBoolean = paramMessageObject.messageText;
                      paramMessageObject = paramArrayOfBoolean;
                      if (paramArrayOfBoolean.length() > 20) {
                        paramMessageObject = paramArrayOfBoolean.subSequence(0, 20) + "...";
                      }
                      localObject1 = LocaleController.formatString("NotificationActionPinnedText", 2131493946, new Object[] { str, paramMessageObject, ((TLRPC.Chat)localObject2).title });
                    }
                    else
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedNoText", 2131493936, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                    }
                  }
                }
                else if (paramMessageObject.replyMessageObject == null)
                {
                  localObject1 = LocaleController.formatString("NotificationActionPinnedNoTextChannel", 2131493937, new Object[] { ((TLRPC.Chat)localObject2).title });
                }
                else
                {
                  paramMessageObject = paramMessageObject.replyMessageObject;
                  if (paramMessageObject.isMusic())
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedMusicChannel", 2131493935, new Object[] { ((TLRPC.Chat)localObject2).title });
                  }
                  else if (paramMessageObject.isVideo())
                  {
                    if ((Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                    {
                      paramMessageObject = "📹 " + paramMessageObject.messageOwner.message;
                      localObject1 = LocaleController.formatString("NotificationActionPinnedTextChannel", 2131493947, new Object[] { ((TLRPC.Chat)localObject2).title, paramMessageObject });
                    }
                    else
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedVideoChannel", 2131493949, new Object[] { ((TLRPC.Chat)localObject2).title });
                    }
                  }
                  else if (paramMessageObject.isGif())
                  {
                    if ((Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                    {
                      paramMessageObject = "🎬 " + paramMessageObject.messageOwner.message;
                      localObject1 = LocaleController.formatString("NotificationActionPinnedTextChannel", 2131493947, new Object[] { ((TLRPC.Chat)localObject2).title, paramMessageObject });
                    }
                    else
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedGifChannel", 2131493931, new Object[] { ((TLRPC.Chat)localObject2).title });
                    }
                  }
                  else if (paramMessageObject.isVoice())
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedVoiceChannel", 2131493951, new Object[] { ((TLRPC.Chat)localObject2).title });
                  }
                  else if (paramMessageObject.isRoundVideo())
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedRoundChannel", 2131493941, new Object[] { ((TLRPC.Chat)localObject2).title });
                  }
                  else if (paramMessageObject.isSticker())
                  {
                    paramMessageObject = paramMessageObject.getStickerEmoji();
                    if (paramMessageObject != null) {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedStickerEmojiChannel", 2131493945, new Object[] { ((TLRPC.Chat)localObject2).title, paramMessageObject });
                    } else {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedStickerChannel", 2131493943, new Object[] { ((TLRPC.Chat)localObject2).title });
                    }
                  }
                  else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument))
                  {
                    if ((Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                    {
                      paramMessageObject = "📎 " + paramMessageObject.messageOwner.message;
                      localObject1 = LocaleController.formatString("NotificationActionPinnedTextChannel", 2131493947, new Object[] { ((TLRPC.Chat)localObject2).title, paramMessageObject });
                    }
                    else
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedFileChannel", 2131493923, new Object[] { ((TLRPC.Chat)localObject2).title });
                    }
                  }
                  else if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVenue)))
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedGeoChannel", 2131493927, new Object[] { ((TLRPC.Chat)localObject2).title });
                  }
                  else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedGeoLiveChannel", 2131493929, new Object[] { ((TLRPC.Chat)localObject2).title });
                  }
                  else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact))
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedContactChannel", 2131493921, new Object[] { ((TLRPC.Chat)localObject2).title });
                  }
                  else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
                  {
                    if ((Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                    {
                      paramMessageObject = "🖼 " + paramMessageObject.messageOwner.message;
                      localObject1 = LocaleController.formatString("NotificationActionPinnedTextChannel", 2131493947, new Object[] { ((TLRPC.Chat)localObject2).title, paramMessageObject });
                    }
                    else
                    {
                      localObject1 = LocaleController.formatString("NotificationActionPinnedPhotoChannel", 2131493939, new Object[] { ((TLRPC.Chat)localObject2).title });
                    }
                  }
                  else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame))
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedGameChannel", 2131493925, new Object[] { ((TLRPC.Chat)localObject2).title });
                  }
                  else if ((paramMessageObject.messageText != null) && (paramMessageObject.messageText.length() > 0))
                  {
                    paramArrayOfBoolean = paramMessageObject.messageText;
                    paramMessageObject = paramArrayOfBoolean;
                    if (paramArrayOfBoolean.length() > 20) {
                      paramMessageObject = paramArrayOfBoolean.subSequence(0, 20) + "...";
                    }
                    localObject1 = LocaleController.formatString("NotificationActionPinnedTextChannel", 2131493947, new Object[] { ((TLRPC.Chat)localObject2).title, paramMessageObject });
                  }
                  else
                  {
                    localObject1 = LocaleController.formatString("NotificationActionPinnedNoTextChannel", 2131493937, new Object[] { ((TLRPC.Chat)localObject2).title });
                  }
                }
              }
              else
              {
                localObject1 = localObject3;
                if ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionGameScore)) {
                  localObject1 = paramMessageObject.messageText.toString();
                }
              }
            }
            else if ((ChatObject.isChannel((TLRPC.Chat)localObject2)) && (!((TLRPC.Chat)localObject2).megagroup))
            {
              if (paramMessageObject.isMediaEmpty())
              {
                if ((!paramBoolean) && (paramMessageObject.messageOwner.message != null) && (paramMessageObject.messageOwner.message.length() != 0))
                {
                  localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, paramMessageObject.messageOwner.message });
                  paramArrayOfBoolean[0] = true;
                }
                else
                {
                  localObject1 = LocaleController.formatString("ChannelMessageNoText", 2131493187, new Object[] { str });
                }
              }
              else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
              {
                if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                {
                  localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, "🖼 " + paramMessageObject.messageOwner.message });
                  paramArrayOfBoolean[0] = true;
                }
                else
                {
                  localObject1 = LocaleController.formatString("ChannelMessagePhoto", 2131493188, new Object[] { str });
                }
              }
              else if (paramMessageObject.isVideo())
              {
                if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                {
                  localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, "📹 " + paramMessageObject.messageOwner.message });
                  paramArrayOfBoolean[0] = true;
                }
                else
                {
                  localObject1 = LocaleController.formatString("ChannelMessageVideo", 2131493192, new Object[] { str });
                }
              }
              else if (paramMessageObject.isVoice())
              {
                localObject1 = LocaleController.formatString("ChannelMessageAudio", 2131493179, new Object[] { str });
              }
              else if (paramMessageObject.isRoundVideo())
              {
                localObject1 = LocaleController.formatString("ChannelMessageRound", 2131493189, new Object[] { str });
              }
              else if (paramMessageObject.isMusic())
              {
                localObject1 = LocaleController.formatString("ChannelMessageMusic", 2131493186, new Object[] { str });
              }
              else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact))
              {
                localObject1 = LocaleController.formatString("ChannelMessageContact", 2131493180, new Object[] { str });
              }
              else if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVenue)))
              {
                localObject1 = LocaleController.formatString("ChannelMessageMap", 2131493185, new Object[] { str });
              }
              else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
              {
                localObject1 = LocaleController.formatString("ChannelMessageLiveLocation", 2131493184, new Object[] { str });
              }
              else
              {
                localObject1 = localObject3;
                if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument)) {
                  if (paramMessageObject.isSticker())
                  {
                    paramMessageObject = paramMessageObject.getStickerEmoji();
                    if (paramMessageObject != null) {
                      localObject1 = LocaleController.formatString("ChannelMessageStickerEmoji", 2131493191, new Object[] { str, paramMessageObject });
                    } else {
                      localObject1 = LocaleController.formatString("ChannelMessageSticker", 2131493190, new Object[] { str });
                    }
                  }
                  else if (paramMessageObject.isGif())
                  {
                    if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                    {
                      localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, "🎬 " + paramMessageObject.messageOwner.message });
                      paramArrayOfBoolean[0] = true;
                    }
                    else
                    {
                      localObject1 = LocaleController.formatString("ChannelMessageGIF", 2131493183, new Object[] { str });
                    }
                  }
                  else if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                  {
                    localObject1 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { str, "📎 " + paramMessageObject.messageOwner.message });
                    paramArrayOfBoolean[0] = true;
                  }
                  else
                  {
                    localObject1 = LocaleController.formatString("ChannelMessageDocument", 2131493181, new Object[] { str });
                  }
                }
              }
            }
            else if (paramMessageObject.isMediaEmpty())
            {
              if ((!paramBoolean) && (paramMessageObject.messageOwner.message != null) && (paramMessageObject.messageOwner.message.length() != 0)) {
                localObject1 = LocaleController.formatString("NotificationMessageGroupText", 2131493987, new Object[] { str, ((TLRPC.Chat)localObject2).title, paramMessageObject.messageOwner.message });
              } else {
                localObject1 = LocaleController.formatString("NotificationMessageGroupNoText", 2131493982, new Object[] { str, ((TLRPC.Chat)localObject2).title });
              }
            }
            else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
            {
              if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message))) {
                localObject1 = LocaleController.formatString("NotificationMessageGroupText", 2131493987, new Object[] { str, ((TLRPC.Chat)localObject2).title, "🖼 " + paramMessageObject.messageOwner.message });
              } else {
                localObject1 = LocaleController.formatString("NotificationMessageGroupPhoto", 2131493983, new Object[] { str, ((TLRPC.Chat)localObject2).title });
              }
            }
            else if (paramMessageObject.isVideo())
            {
              if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message))) {
                localObject1 = LocaleController.formatString("NotificationMessageGroupText", 2131493987, new Object[] { str, ((TLRPC.Chat)localObject2).title, "📹 " + paramMessageObject.messageOwner.message });
              } else {
                localObject1 = LocaleController.formatString("NotificationMessageGroupVideo", 2131493988, new Object[] { str, ((TLRPC.Chat)localObject2).title });
              }
            }
            else if (paramMessageObject.isVoice())
            {
              localObject1 = LocaleController.formatString("NotificationMessageGroupAudio", 2131493973, new Object[] { str, ((TLRPC.Chat)localObject2).title });
            }
            else if (paramMessageObject.isRoundVideo())
            {
              localObject1 = LocaleController.formatString("NotificationMessageGroupRound", 2131493984, new Object[] { str, ((TLRPC.Chat)localObject2).title });
            }
            else if (paramMessageObject.isMusic())
            {
              localObject1 = LocaleController.formatString("NotificationMessageGroupMusic", 2131493981, new Object[] { str, ((TLRPC.Chat)localObject2).title });
            }
            else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact))
            {
              localObject1 = LocaleController.formatString("NotificationMessageGroupContact", 2131493974, new Object[] { str, ((TLRPC.Chat)localObject2).title });
            }
            else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame))
            {
              localObject1 = LocaleController.formatString("NotificationMessageGroupGame", 2131493976, new Object[] { str, ((TLRPC.Chat)localObject2).title, paramMessageObject.messageOwner.media.game.title });
            }
            else if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVenue)))
            {
              localObject1 = LocaleController.formatString("NotificationMessageGroupMap", 2131493980, new Object[] { str, ((TLRPC.Chat)localObject2).title });
            }
            else if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
            {
              localObject1 = LocaleController.formatString("NotificationMessageGroupLiveLocation", 2131493979, new Object[] { str, ((TLRPC.Chat)localObject2).title });
            }
            else
            {
              localObject1 = localObject3;
              if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument)) {
                if (paramMessageObject.isSticker())
                {
                  paramMessageObject = paramMessageObject.getStickerEmoji();
                  if (paramMessageObject != null) {
                    localObject1 = LocaleController.formatString("NotificationMessageGroupStickerEmoji", 2131493986, new Object[] { str, ((TLRPC.Chat)localObject2).title, paramMessageObject });
                  } else {
                    localObject1 = LocaleController.formatString("NotificationMessageGroupSticker", 2131493985, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                  }
                }
                else if (paramMessageObject.isGif())
                {
                  if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message))) {
                    localObject1 = LocaleController.formatString("NotificationMessageGroupText", 2131493987, new Object[] { str, ((TLRPC.Chat)localObject2).title, "🎬 " + paramMessageObject.messageOwner.message });
                  } else {
                    localObject1 = LocaleController.formatString("NotificationMessageGroupGif", 2131493977, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                  }
                }
                else if ((!paramBoolean) && (Build.VERSION.SDK_INT >= 19) && (!TextUtils.isEmpty(paramMessageObject.messageOwner.message)))
                {
                  localObject1 = LocaleController.formatString("NotificationMessageGroupText", 2131493987, new Object[] { str, ((TLRPC.Chat)localObject2).title, "📎 " + paramMessageObject.messageOwner.message });
                }
                else
                {
                  localObject1 = LocaleController.formatString("NotificationMessageGroupDocument", 2131493975, new Object[] { str, ((TLRPC.Chat)localObject2).title });
                }
              }
            }
          }
          else if ((ChatObject.isChannel((TLRPC.Chat)localObject2)) && (!((TLRPC.Chat)localObject2).megagroup)) {
            localObject1 = LocaleController.formatString("ChannelMessageNoText", 2131493187, new Object[] { str });
          } else {
            localObject1 = LocaleController.formatString("NotificationMessageGroupNoText", 2131493982, new Object[] { str, ((TLRPC.Chat)localObject2).title });
          }
        }
      }
    }
  }
  
  private int getTotalAllUnreadCount()
  {
    int j = 0;
    int i = 0;
    while (i < 3)
    {
      int k = j;
      if (UserConfig.getInstance(i).isClientActivated())
      {
        NotificationsController localNotificationsController = getInstance(i);
        k = j;
        if (localNotificationsController.showBadgeNumber) {
          k = j + localNotificationsController.total_unread_count;
        }
      }
      i += 1;
      j = k;
    }
    return j;
  }
  
  private boolean isEmptyVibration(long[] paramArrayOfLong)
  {
    if ((paramArrayOfLong == null) || (paramArrayOfLong.length == 0)) {
      return false;
    }
    int i = 0;
    for (;;)
    {
      if (i >= paramArrayOfLong.length) {
        break label34;
      }
      if (paramArrayOfLong[0] != 0L) {
        break;
      }
      i += 1;
    }
    label34:
    return true;
  }
  
  private boolean isPersonalMessage(MessageObject paramMessageObject)
  {
    return (paramMessageObject.messageOwner.to_id != null) && (paramMessageObject.messageOwner.to_id.chat_id == 0) && (paramMessageObject.messageOwner.to_id.channel_id == 0) && ((paramMessageObject.messageOwner.action == null) || ((paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionEmpty)));
  }
  
  private void playInChatSound()
  {
    if ((!this.inChatSoundEnabled) || (MediaController.getInstance().isRecordingAudio())) {}
    for (;;)
    {
      return;
      for (;;)
      {
        try
        {
          int i = audioManager.getRingerMode();
          if (i == 0) {
            break;
          }
        }
        catch (Exception localException2)
        {
          FileLog.e(localException2);
          continue;
        }
        try
        {
          if (getNotifyOverride(MessagesController.getNotificationsSettings(this.currentAccount), this.opened_dialog_id) == 2) {
            break;
          }
          notificationsQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              if (Math.abs(System.currentTimeMillis() - NotificationsController.this.lastSoundPlay) <= 500L) {}
              for (;;)
              {
                return;
                try
                {
                  if (NotificationsController.this.soundPool == null)
                  {
                    NotificationsController.access$2802(NotificationsController.this, new SoundPool(3, 1, 0));
                    NotificationsController.this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
                    {
                      public void onLoadComplete(SoundPool paramAnonymous2SoundPool, int paramAnonymous2Int1, int paramAnonymous2Int2)
                      {
                        if (paramAnonymous2Int2 == 0) {}
                        try
                        {
                          paramAnonymous2SoundPool.play(paramAnonymous2Int1, 1.0F, 1.0F, 1, 0, 1.0F);
                          return;
                        }
                        catch (Exception paramAnonymous2SoundPool)
                        {
                          FileLog.e(paramAnonymous2SoundPool);
                        }
                      }
                    });
                  }
                  if ((NotificationsController.this.soundIn == 0) && (!NotificationsController.this.soundInLoaded))
                  {
                    NotificationsController.access$3002(NotificationsController.this, true);
                    NotificationsController.access$2902(NotificationsController.this, NotificationsController.this.soundPool.load(ApplicationLoader.applicationContext, 2131427328, 1));
                  }
                  int i = NotificationsController.this.soundIn;
                  if (i == 0) {
                    continue;
                  }
                  try
                  {
                    NotificationsController.this.soundPool.play(NotificationsController.this.soundIn, 1.0F, 1.0F, 1, 0, 1.0F);
                    return;
                  }
                  catch (Exception localException1)
                  {
                    FileLog.e(localException1);
                    return;
                  }
                  return;
                }
                catch (Exception localException2)
                {
                  FileLog.e(localException2);
                }
              }
            }
          });
          return;
        }
        catch (Exception localException1)
        {
          FileLog.e(localException1);
          return;
        }
      }
    }
  }
  
  private void scheduleNotificationDelay(boolean paramBoolean)
  {
    try
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("delay notification start, onlineReason = " + paramBoolean);
      }
      this.notificationDelayWakelock.acquire(10000L);
      notificationsQueue.cancelRunnable(this.notificationDelayRunnable);
      DispatchQueue localDispatchQueue = notificationsQueue;
      Runnable localRunnable = this.notificationDelayRunnable;
      if (paramBoolean) {}
      for (int i = 3000;; i = 1000)
      {
        localDispatchQueue.postRunnable(localRunnable, i);
        return;
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
      showOrUpdateNotification(this.notifyCheck);
    }
  }
  
  private void scheduleNotificationRepeat()
  {
    try
    {
      Object localObject = new Intent(ApplicationLoader.applicationContext, NotificationRepeat.class);
      ((Intent)localObject).putExtra("currentAccount", this.currentAccount);
      localObject = PendingIntent.getService(ApplicationLoader.applicationContext, 0, (Intent)localObject, 0);
      int i = MessagesController.getNotificationsSettings(this.currentAccount).getInt("repeat_messages", 60);
      if ((i > 0) && (this.personal_count > 0))
      {
        this.alarmManager.set(2, SystemClock.elapsedRealtime() + i * 60 * 1000, (PendingIntent)localObject);
        return;
      }
      this.alarmManager.cancel((PendingIntent)localObject);
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  private void setBadge(int paramInt)
  {
    if (this.lastBadgeCount == paramInt) {
      return;
    }
    this.lastBadgeCount = paramInt;
    NotificationBadge.applyCount(paramInt);
  }
  
  @SuppressLint({"InlinedApi"})
  private void showExtraNotifications(NotificationCompat.Builder paramBuilder, boolean paramBoolean, String paramString)
  {
    Notification localNotification = paramBuilder.build();
    if (Build.VERSION.SDK_INT < 18)
    {
      notificationManager.notify(this.notificationId, localNotification);
      return;
    }
    ArrayList localArrayList1 = new ArrayList();
    LongSparseArray localLongSparseArray1 = new LongSparseArray();
    int i = 0;
    Object localObject3;
    long l1;
    Object localObject2;
    Object localObject1;
    while (i < this.pushMessages.size())
    {
      localObject3 = (MessageObject)this.pushMessages.get(i);
      l1 = ((MessageObject)localObject3).getDialogId();
      localObject2 = (ArrayList)localLongSparseArray1.get(l1);
      localObject1 = localObject2;
      if (localObject2 == null)
      {
        localObject1 = new ArrayList();
        localLongSparseArray1.put(l1, localObject1);
        localArrayList1.add(0, Long.valueOf(l1));
      }
      ((ArrayList)localObject1).add(localObject3);
      i += 1;
    }
    LongSparseArray localLongSparseArray2 = this.wearNotificationsIds.clone();
    this.wearNotificationsIds.clear();
    ArrayList localArrayList2 = new ArrayList();
    JSONArray localJSONArray = null;
    if (WearDataLayerListenerService.isWatchConnected()) {
      localJSONArray = new JSONArray();
    }
    i = 0;
    int n = localArrayList1.size();
    int i1;
    int i2;
    Object localObject5;
    int i3;
    Object localObject8;
    Object localObject7;
    boolean bool2;
    boolean bool1;
    label421:
    label832:
    int j;
    if (i < n)
    {
      l1 = ((Long)localArrayList1.get(i)).longValue();
      Object localObject12 = (ArrayList)localLongSparseArray1.get(l1);
      i1 = ((MessageObject)((ArrayList)localObject12).get(0)).getId();
      i2 = (int)l1;
      int i4 = (int)(l1 >> 32);
      localObject3 = (Integer)localLongSparseArray2.get(l1);
      Object localObject10;
      Object localObject9;
      boolean bool4;
      Object localObject4;
      Object localObject6;
      if (localObject3 == null) {
        if (i2 != 0)
        {
          localObject3 = Integer.valueOf(i2);
          localObject5 = null;
          if (localJSONArray != null) {
            localObject5 = new JSONObject();
          }
          localObject2 = (MessageObject)((ArrayList)localObject12).get(0);
          i3 = ((MessageObject)localObject2).messageOwner.date;
          localObject8 = null;
          localObject10 = null;
          bool3 = false;
          bool5 = false;
          localObject9 = null;
          if (i2 == 0) {
            break label1626;
          }
          bool4 = true;
          if (i2 <= 0) {
            break label1300;
          }
          localObject10 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i2));
          if (localObject10 != null) {
            break label1080;
          }
          if (!((MessageObject)localObject2).isFcmMessage()) {
            break label1651;
          }
          localObject1 = ((MessageObject)localObject2).localName;
          localObject4 = localObject10;
          localObject7 = localObject5;
          localObject2 = localObject9;
          bool2 = bool5;
          paramBoolean = bool3;
          localObject6 = localObject8;
          bool1 = bool4;
        }
      }
      Object localObject11;
      Object localObject13;
      StringBuilder localStringBuilder;
      Object localObject14;
      int k;
      Object localObject15;
      Object localObject16;
      label1030:
      int m;
      for (;;)
      {
        if (!AndroidUtilities.needShowPasscode(false))
        {
          localObject5 = localObject2;
          if (!SharedConfig.isWaitingForPasscodeEnter) {}
        }
        else
        {
          localObject1 = LocaleController.getString("AppName", 2131492981);
          localObject5 = null;
          bool1 = false;
        }
        localObject11 = new NotificationCompat.CarExtender.UnreadConversation.Builder((String)localObject1).setLatestTimestamp(i3 * 1000L);
        localObject2 = new Intent(ApplicationLoader.applicationContext, AutoMessageHeardReceiver.class);
        ((Intent)localObject2).addFlags(32);
        ((Intent)localObject2).setAction("org.telegram.messenger.ACTION_MESSAGE_HEARD");
        ((Intent)localObject2).putExtra("dialog_id", l1);
        ((Intent)localObject2).putExtra("max_id", i1);
        ((Intent)localObject2).putExtra("currentAccount", this.currentAccount);
        ((NotificationCompat.CarExtender.UnreadConversation.Builder)localObject11).setReadPendingIntent(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, ((Integer)localObject3).intValue(), (Intent)localObject2, 134217728));
        localObject2 = null;
        if (paramBoolean)
        {
          localObject10 = localObject2;
          if (!bool2) {}
        }
        else
        {
          localObject10 = localObject2;
          if (bool1)
          {
            localObject10 = localObject2;
            if (!SharedConfig.isWaitingForPasscodeEnter)
            {
              localObject2 = new Intent(ApplicationLoader.applicationContext, AutoMessageReplyReceiver.class);
              ((Intent)localObject2).addFlags(32);
              ((Intent)localObject2).setAction("org.telegram.messenger.ACTION_MESSAGE_REPLY");
              ((Intent)localObject2).putExtra("dialog_id", l1);
              ((Intent)localObject2).putExtra("max_id", i1);
              ((Intent)localObject2).putExtra("currentAccount", this.currentAccount);
              ((NotificationCompat.CarExtender.UnreadConversation.Builder)localObject11).setReplyAction(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, ((Integer)localObject3).intValue(), (Intent)localObject2, 134217728), new RemoteInput.Builder("extra_voice_reply").setLabel(LocaleController.getString("Reply", 2131494235)).build());
              localObject2 = new Intent(ApplicationLoader.applicationContext, WearReplyReceiver.class);
              ((Intent)localObject2).putExtra("dialog_id", l1);
              ((Intent)localObject2).putExtra("max_id", i1);
              ((Intent)localObject2).putExtra("currentAccount", this.currentAccount);
              localObject8 = PendingIntent.getBroadcast(ApplicationLoader.applicationContext, ((Integer)localObject3).intValue(), (Intent)localObject2, 134217728);
              localObject9 = new RemoteInput.Builder("extra_voice_reply").setLabel(LocaleController.getString("Reply", 2131494235)).build();
              if (i2 >= 0) {
                break label1716;
              }
              localObject2 = LocaleController.formatString("ReplyToGroup", 2131494236, new Object[] { localObject1 });
              localObject10 = new NotificationCompat.Action.Builder(2131165406, (CharSequence)localObject2, (PendingIntent)localObject8).setAllowGeneratedReplies(true).addRemoteInput((RemoteInput)localObject9).build();
            }
          }
        }
        localObject8 = (Integer)this.pushDialogs.get(l1);
        localObject2 = localObject8;
        if (localObject8 == null) {
          localObject2 = Integer.valueOf(0);
        }
        localObject13 = new NotificationCompat.MessagingStyle("").setConversationTitle(String.format("%1$s (%2$s)", new Object[] { localObject1, LocaleController.formatPluralString("NewMessages", Math.max(((Integer)localObject2).intValue(), ((ArrayList)localObject12).size())) }));
        localStringBuilder = new StringBuilder();
        localObject14 = new boolean[1];
        localObject9 = null;
        j = 0;
        localObject8 = null;
        if (localObject7 != null) {
          localObject8 = new JSONArray();
        }
        k = ((ArrayList)localObject12).size() - 1;
        for (;;)
        {
          if (k < 0) {
            break label2091;
          }
          localObject15 = (MessageObject)((ArrayList)localObject12).get(k);
          localObject16 = getStringForMessage((MessageObject)localObject15, false, (boolean[])localObject14);
          if (!((MessageObject)localObject15).isFcmMessage()) {
            break;
          }
          localObject2 = ((MessageObject)localObject15).localName;
          if (localObject16 != null) {
            break label1746;
          }
          m = j;
          localObject2 = localObject9;
          k -= 1;
          localObject9 = localObject2;
          j = m;
        }
        localObject3 = Integer.valueOf(i4);
        break;
        localLongSparseArray2.remove(l1);
        break;
        label1080:
        localObject11 = UserObject.getUserName((TLRPC.User)localObject10);
        bool1 = bool4;
        localObject6 = localObject8;
        paramBoolean = bool3;
        bool2 = bool5;
        localObject1 = localObject11;
        localObject2 = localObject9;
        localObject7 = localObject5;
        localObject4 = localObject10;
        if (((TLRPC.User)localObject10).photo != null)
        {
          bool1 = bool4;
          localObject6 = localObject8;
          paramBoolean = bool3;
          bool2 = bool5;
          localObject1 = localObject11;
          localObject2 = localObject9;
          localObject7 = localObject5;
          localObject4 = localObject10;
          if (((TLRPC.User)localObject10).photo.photo_small != null)
          {
            bool1 = bool4;
            localObject6 = localObject8;
            paramBoolean = bool3;
            bool2 = bool5;
            localObject1 = localObject11;
            localObject2 = localObject9;
            localObject7 = localObject5;
            localObject4 = localObject10;
            if (((TLRPC.User)localObject10).photo.photo_small.volume_id != 0L)
            {
              bool1 = bool4;
              localObject6 = localObject8;
              paramBoolean = bool3;
              bool2 = bool5;
              localObject1 = localObject11;
              localObject2 = localObject9;
              localObject7 = localObject5;
              localObject4 = localObject10;
              if (((TLRPC.User)localObject10).photo.photo_small.local_id != 0)
              {
                localObject2 = ((TLRPC.User)localObject10).photo.photo_small;
                bool1 = bool4;
                localObject6 = localObject8;
                paramBoolean = bool3;
                bool2 = bool5;
                localObject1 = localObject11;
                localObject7 = localObject5;
                localObject4 = localObject10;
                continue;
                label1300:
                localObject8 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i2));
                if (localObject8 != null) {
                  break label1374;
                }
                if (!((MessageObject)localObject2).isFcmMessage()) {
                  break label1651;
                }
                bool2 = ((MessageObject)localObject2).isMegagroup();
                localObject1 = ((MessageObject)localObject2).localName;
                paramBoolean = ((MessageObject)localObject2).localChannel;
                bool1 = bool4;
                localObject6 = localObject8;
                localObject2 = localObject9;
                localObject7 = localObject5;
                localObject4 = localObject10;
              }
            }
          }
        }
      }
      label1374:
      boolean bool5 = ((TLRPC.Chat)localObject8).megagroup;
      if ((ChatObject.isChannel((TLRPC.Chat)localObject8)) && (!((TLRPC.Chat)localObject8).megagroup)) {}
      for (boolean bool3 = true;; bool3 = false)
      {
        localObject11 = ((TLRPC.Chat)localObject8).title;
        bool1 = bool4;
        localObject6 = localObject8;
        paramBoolean = bool3;
        bool2 = bool5;
        localObject1 = localObject11;
        localObject2 = localObject9;
        localObject7 = localObject5;
        localObject4 = localObject10;
        if (((TLRPC.Chat)localObject8).photo == null) {
          break;
        }
        bool1 = bool4;
        localObject6 = localObject8;
        paramBoolean = bool3;
        bool2 = bool5;
        localObject1 = localObject11;
        localObject2 = localObject9;
        localObject7 = localObject5;
        localObject4 = localObject10;
        if (((TLRPC.Chat)localObject8).photo.photo_small == null) {
          break;
        }
        bool1 = bool4;
        localObject6 = localObject8;
        paramBoolean = bool3;
        bool2 = bool5;
        localObject1 = localObject11;
        localObject2 = localObject9;
        localObject7 = localObject5;
        localObject4 = localObject10;
        if (((TLRPC.Chat)localObject8).photo.photo_small.volume_id == 0L) {
          break;
        }
        bool1 = bool4;
        localObject6 = localObject8;
        paramBoolean = bool3;
        bool2 = bool5;
        localObject1 = localObject11;
        localObject2 = localObject9;
        localObject7 = localObject5;
        localObject4 = localObject10;
        if (((TLRPC.Chat)localObject8).photo.photo_small.local_id == 0) {
          break;
        }
        localObject2 = ((TLRPC.Chat)localObject8).photo.photo_small;
        bool1 = bool4;
        localObject6 = localObject8;
        paramBoolean = bool3;
        bool2 = bool5;
        localObject1 = localObject11;
        localObject7 = localObject5;
        localObject4 = localObject10;
        break;
      }
      label1626:
      bool1 = false;
      localObject1 = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(i4));
      if (localObject1 == null) {}
      label1651:
      label1716:
      label1746:
      label2091:
      label2263:
      label2854:
      label2892:
      label2929:
      label3017:
      label3034:
      do
      {
        do
        {
          i += 1;
          break;
          localObject4 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.EncryptedChat)localObject1).user_id));
        } while (localObject4 == null);
        localObject1 = LocaleController.getString("SecretChatName", 2131494323);
        localObject2 = null;
        localObject7 = null;
        localObject6 = localObject8;
        paramBoolean = bool3;
        bool2 = bool5;
        break label421;
        localObject2 = LocaleController.formatString("ReplyToUser", 2131494237, new Object[] { localObject1 });
        break label832;
        localObject2 = localObject1;
        break label1030;
        if (i2 < 0) {
          localObject2 = ((String)localObject16).replace(" @ " + (String)localObject2, "");
        }
        for (;;)
        {
          if (localStringBuilder.length() > 0) {
            localStringBuilder.append("\n\n");
          }
          localStringBuilder.append((String)localObject2);
          ((NotificationCompat.CarExtender.UnreadConversation.Builder)localObject11).addMessage((String)localObject2);
          ((NotificationCompat.MessagingStyle)localObject13).addMessage((CharSequence)localObject2, ((MessageObject)localObject15).messageOwner.date * 1000L, null);
          if (localObject8 != null) {}
          try
          {
            localObject2 = new JSONObject();
            ((JSONObject)localObject2).put("text", getShortStringForMessage((MessageObject)localObject15));
            ((JSONObject)localObject2).put("date", ((MessageObject)localObject15).messageOwner.date);
            if ((((MessageObject)localObject15).isFromUser()) && (i2 < 0))
            {
              localObject16 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((MessageObject)localObject15).getFromId()));
              if (localObject16 != null)
              {
                ((JSONObject)localObject2).put("fname", ((TLRPC.User)localObject16).first_name);
                ((JSONObject)localObject2).put("lname", ((TLRPC.User)localObject16).last_name);
              }
            }
            ((JSONArray)localObject8).put(localObject2);
          }
          catch (JSONException localJSONException2)
          {
            long l2;
            for (;;) {}
          }
          localObject2 = localObject9;
          m = j;
          if (l1 != 777000L) {
            break;
          }
          localObject2 = localObject9;
          m = j;
          if (((MessageObject)localObject15).messageOwner.reply_markup == null) {
            break;
          }
          localObject2 = ((MessageObject)localObject15).messageOwner.reply_markup.rows;
          m = ((MessageObject)localObject15).getId();
          break;
          if (localObject14[0] != 0) {
            localObject2 = ((String)localObject16).replace((String)localObject2 + ": ", "");
          } else {
            localObject2 = ((String)localObject16).replace((String)localObject2 + " ", "");
          }
        }
        localObject2 = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        ((Intent)localObject2).setAction("com.tmessages.openchat" + Math.random() + Integer.MAX_VALUE);
        ((Intent)localObject2).setFlags(32768);
        if (i2 != 0) {
          if (i2 > 0)
          {
            ((Intent)localObject2).putExtra("userId", i2);
            ((Intent)localObject2).putExtra("currentAccount", this.currentAccount);
            localObject14 = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, (Intent)localObject2, 1073741824);
            localObject15 = new NotificationCompat.WearableExtender();
            if (localObject10 != null) {
              ((NotificationCompat.WearableExtender)localObject15).addAction((NotificationCompat.Action)localObject10);
            }
            if (i2 == 0) {
              break label2892;
            }
            if (i2 <= 0) {
              break label2854;
            }
            localObject2 = "tguser" + i2 + "_" + i1;
            ((NotificationCompat.WearableExtender)localObject15).setDismissalId((String)localObject2);
            ((NotificationCompat.WearableExtender)localObject15).setBridgeTag("tgaccount" + UserConfig.getInstance(this.currentAccount).getClientUserId());
            localObject10 = new NotificationCompat.WearableExtender();
            ((NotificationCompat.WearableExtender)localObject10).setDismissalId("summary_" + (String)localObject2);
            paramBuilder.extend((NotificationCompat.Extender)localObject10);
            l2 = ((MessageObject)((ArrayList)localObject12).get(0)).messageOwner.date * 1000L;
            localObject2 = new NotificationCompat.Builder(ApplicationLoader.applicationContext).setContentTitle((CharSequence)localObject1).setSmallIcon(2131165543).setGroup(this.notificationGroup).setContentText(localStringBuilder.toString()).setAutoCancel(true).setNumber(((ArrayList)localObject12).size()).setColor(-13851168).setGroupSummary(false).setWhen(l2).setShowWhen(true).setShortcutId("sdid_" + l1).setGroupAlertBehavior(1).setStyle((NotificationCompat.Style)localObject13).setContentIntent((PendingIntent)localObject14).extend((NotificationCompat.Extender)localObject15).setSortKey("" + (Long.MAX_VALUE - l2)).extend(new NotificationCompat.CarExtender().setUnreadConversation(((NotificationCompat.CarExtender.UnreadConversation.Builder)localObject11).build())).setCategory("msg");
            if ((this.pushDialogs.size() == 1) && (!TextUtils.isEmpty(paramString))) {
              ((NotificationCompat.Builder)localObject2).setSubText(paramString);
            }
            if (i2 == 0) {
              ((NotificationCompat.Builder)localObject2).setLocalOnly(true);
            }
            if (localObject5 != null)
            {
              localObject10 = ImageLoader.getInstance().getImageFromMemory((TLObject)localObject5, null, "50_50");
              if (localObject10 == null) {
                break label2929;
              }
              ((NotificationCompat.Builder)localObject2).setLargeIcon(((BitmapDrawable)localObject10).getBitmap());
            }
            if ((AndroidUtilities.needShowPasscode(false)) || (SharedConfig.isWaitingForPasscodeEnter) || (localObject9 == null)) {
              break label3034;
            }
            k = 0;
            i4 = ((ArrayList)localObject9).size();
          }
        }
        for (;;)
        {
          if (k >= i4) {
            break label3034;
          }
          localObject10 = (TLRPC.TL_keyboardButtonRow)((ArrayList)localObject9).get(k);
          m = 0;
          int i5 = ((TLRPC.TL_keyboardButtonRow)localObject10).buttons.size();
          for (;;)
          {
            if (m < i5)
            {
              localObject12 = (TLRPC.KeyboardButton)((TLRPC.TL_keyboardButtonRow)localObject10).buttons.get(m);
              if ((localObject12 instanceof TLRPC.TL_keyboardButtonCallback))
              {
                localObject11 = new Intent(ApplicationLoader.applicationContext, NotificationCallbackReceiver.class);
                ((Intent)localObject11).putExtra("currentAccount", this.currentAccount);
                ((Intent)localObject11).putExtra("did", l1);
                if (((TLRPC.KeyboardButton)localObject12).data != null) {
                  ((Intent)localObject11).putExtra("data", ((TLRPC.KeyboardButton)localObject12).data);
                }
                ((Intent)localObject11).putExtra("mid", j);
                localObject12 = ((TLRPC.KeyboardButton)localObject12).text;
                localObject13 = ApplicationLoader.applicationContext;
                int i6 = this.lastButtonId;
                this.lastButtonId = (i6 + 1);
                ((NotificationCompat.Builder)localObject2).addAction(0, (CharSequence)localObject12, PendingIntent.getBroadcast((Context)localObject13, i6, (Intent)localObject11, 134217728));
              }
              m += 1;
              continue;
              ((Intent)localObject2).putExtra("chatId", -i2);
              break;
              ((Intent)localObject2).putExtra("encId", i4);
              break;
              localObject2 = "tgchat" + -i2 + "_" + i1;
              break label2263;
              localObject2 = "tgenc" + i4 + "_" + i1;
              break label2263;
              for (;;)
              {
                float f;
                try
                {
                  localObject10 = FileLoader.getPathToAttach((TLObject)localObject5, true);
                  if (!((File)localObject10).exists()) {
                    break;
                  }
                  f = 160.0F / AndroidUtilities.dp(50.0F);
                  localObject11 = new BitmapFactory.Options();
                  if (f >= 1.0F) {
                    break label3017;
                  }
                  k = 1;
                  ((BitmapFactory.Options)localObject11).inSampleSize = k;
                  localObject10 = BitmapFactory.decodeFile(((File)localObject10).getAbsolutePath(), (BitmapFactory.Options)localObject11);
                  if (localObject10 == null) {
                    break;
                  }
                  ((NotificationCompat.Builder)localObject2).setLargeIcon((Bitmap)localObject10);
                }
                catch (Throwable localThrowable) {}
                break;
                k = (int)f;
              }
            }
          }
          k += 1;
        }
        if ((localObject6 == null) && (localObject4 != null) && (((TLRPC.User)localObject4).phone != null) && (((TLRPC.User)localObject4).phone.length() > 0)) {
          ((NotificationCompat.Builder)localObject2).addPerson("tel:+" + ((TLRPC.User)localObject4).phone);
        }
        if (Build.VERSION.SDK_INT >= 26) {
          ((NotificationCompat.Builder)localObject2).setChannelId("Other3");
        }
        localArrayList2.add(new Object()
        {
          int id;
          Notification notification;
          
          void call()
          {
            NotificationsController.notificationManager.notify(this.id, this.notification);
          }
        });
        this.wearNotificationsIds.put(l1, localObject3);
      } while (localObject7 == null);
    }
    for (;;)
    {
      try
      {
        ((JSONObject)localObject7).put("reply", bool1);
        ((JSONObject)localObject7).put("name", localObject1);
        ((JSONObject)localObject7).put("max_id", i1);
        ((JSONObject)localObject7).put("max_date", i3);
        ((JSONObject)localObject7).put("id", Math.abs(i2));
        if (localObject5 != null) {
          ((JSONObject)localObject7).put("photo", ((TLRPC.FileLocation)localObject5).dc_id + "_" + ((TLRPC.FileLocation)localObject5).volume_id + "_" + ((TLRPC.FileLocation)localObject5).secret);
        }
        if (localObject8 != null) {
          ((JSONObject)localObject7).put("msgs", localObject8);
        }
        if (i2 <= 0) {
          break label3506;
        }
        ((JSONObject)localObject7).put("type", "user");
        localJSONArray.put(localObject7);
      }
      catch (JSONException localJSONException1) {}
      ((JSONObject)localObject7).put("type", "channel");
      continue;
      ((JSONObject)localObject7).put("type", "group");
      continue;
      notificationManager.notify(this.notificationId, localNotification);
      i = 0;
      j = localArrayList2.size();
      if (i < j)
      {
        ((1NotificationHolder)localArrayList2.get(i)).call();
        i += 1;
      }
      else
      {
        i = 0;
        if (i < localLongSparseArray2.size())
        {
          notificationManager.cancel(((Integer)localLongSparseArray2.valueAt(i)).intValue());
          i += 1;
        }
        else
        {
          if (localJSONArray == null) {
            break;
          }
          try
          {
            paramBuilder = new JSONObject();
            paramBuilder.put("id", UserConfig.getInstance(this.currentAccount).getClientUserId());
            paramBuilder.put("n", localJSONArray);
            WearDataLayerListenerService.sendMessageToWatch("/notify", paramBuilder.toString().getBytes("UTF-8"), "remote_notifications");
            return;
          }
          catch (Exception paramBuilder)
          {
            return;
          }
          break label1651;
          label3506:
          if (i2 < 0) {
            if (!paramBoolean) {
              if (!bool2) {}
            }
          }
        }
      }
    }
  }
  
  private void showOrUpdateNotification(boolean paramBoolean)
  {
    if ((!UserConfig.getInstance(this.currentAccount).isClientActivated()) || (this.pushMessages.isEmpty()))
    {
      dismissNotification();
      return;
    }
    MessageObject localMessageObject1;
    Object localObject5;
    int i8;
    try
    {
      ConnectionsManager.getInstance(this.currentAccount).resumeNetworkMaybe();
      localMessageObject1 = (MessageObject)this.pushMessages.get(0);
      localObject5 = MessagesController.getNotificationsSettings(this.currentAccount);
      i8 = ((SharedPreferences)localObject5).getInt("dismissDate", 0);
      if (localMessageObject1.messageOwner.date <= i8)
      {
        dismissNotification();
        return;
      }
    }
    catch (Exception localException1)
    {
      FileLog.e(localException1);
      return;
    }
    long l2 = localMessageObject1.getDialogId();
    long l1 = l2;
    if (localMessageObject1.messageOwner.mentioned) {
      l1 = localMessageObject1.messageOwner.from_id;
    }
    localMessageObject1.getId();
    int i4;
    label165:
    int i;
    int m;
    label191:
    Object localObject13;
    Object localObject10;
    Object localObject6;
    int i5;
    int i6;
    int n;
    int k;
    label312:
    int i3;
    label493:
    String str;
    boolean bool3;
    int i2;
    label682:
    int i7;
    Object localObject4;
    if (localMessageObject1.messageOwner.to_id.chat_id != 0)
    {
      i4 = localMessageObject1.messageOwner.to_id.chat_id;
      i = localMessageObject1.messageOwner.to_id.user_id;
      if (i != 0) {
        break label2250;
      }
      m = localMessageObject1.messageOwner.from_id;
      localObject13 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(m));
      localObject10 = null;
      if (i4 != 0) {
        localObject10 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(i4));
      }
      localObject6 = null;
      j = 0;
      i5 = 0;
      i6 = -16776961;
      n = 0;
      k = getNotifyOverride((SharedPreferences)localObject5, l1);
      if ((!paramBoolean) || (k == 2)) {
        break label3936;
      }
      if (!((SharedPreferences)localObject5).getBoolean("EnableAll", true)) {
        break label3928;
      }
      i = j;
      if (i4 != 0)
      {
        i = j;
        if (!((SharedPreferences)localObject5).getBoolean("EnableGroup", true)) {
          break label3928;
        }
      }
      i3 = i;
      if (i == 0)
      {
        i3 = i;
        if (l2 == l1)
        {
          i3 = i;
          if (localObject10 != null)
          {
            if (!((SharedPreferences)localObject5).getBoolean("custom_" + l2, false)) {
              break label4107;
            }
            j = ((SharedPreferences)localObject5).getInt("smart_max_count_" + l2, 2);
            k = ((SharedPreferences)localObject5).getInt("smart_delay_" + l2, 180);
            i3 = i;
            if (j != 0)
            {
              localObject1 = (Point)this.smartNotificationsDialogs.get(l2);
              if (localObject1 != null) {
                break label2280;
              }
              localObject1 = new Point(1, (int)(System.currentTimeMillis() / 1000L));
              this.smartNotificationsDialogs.put(l2, localObject1);
              i3 = i;
            }
          }
        }
      }
      str = Settings.System.DEFAULT_NOTIFICATION_URI.getPath();
      boolean bool1 = ((SharedPreferences)localObject5).getBoolean("EnableInAppSounds", true);
      boolean bool2 = ((SharedPreferences)localObject5).getBoolean("EnableInAppVibrate", true);
      bool3 = ((SharedPreferences)localObject5).getBoolean("EnableInAppPreview", true);
      boolean bool4 = ((SharedPreferences)localObject5).getBoolean("EnableInAppPriority", false);
      boolean bool5 = ((SharedPreferences)localObject5).getBoolean("custom_" + l2, false);
      if (!bool5) {
        break label4124;
      }
      i1 = ((SharedPreferences)localObject5).getInt("vibrate_" + l2, 0);
      i2 = ((SharedPreferences)localObject5).getInt("priority_" + l2, 3);
      localObject1 = ((SharedPreferences)localObject5).getString("sound_path_" + l2, null);
      i7 = 0;
      if (i4 == 0) {
        break label2383;
      }
      if ((localObject1 == null) || (!((String)localObject1).equals(str))) {
        break label2357;
      }
      localObject4 = null;
      label708:
      i = ((SharedPreferences)localObject5).getInt("vibrate_group", 0);
      k = ((SharedPreferences)localObject5).getInt("priority_group", 1);
      j = ((SharedPreferences)localObject5).getInt("GroupLed", -16776961);
      label748:
      n = j;
      if (!bool5) {
        break label3941;
      }
      n = j;
      if (!((SharedPreferences)localObject5).contains("color_" + l2)) {
        break label3941;
      }
      n = ((SharedPreferences)localObject5).getInt("color_" + l2, 0);
      break label3941;
      label826:
      bool5 = ApplicationLoader.mainInterfacePaused;
      localObject5 = localObject4;
      i1 = j;
      i = k;
      if (!bool5)
      {
        if (!bool1) {
          localObject4 = null;
        }
        if (!bool2) {
          j = 2;
        }
        if (bool4) {
          break label4136;
        }
        i = 0;
        i1 = j;
        localObject5 = localObject4;
      }
      label878:
      k = i1;
      if (i5 != 0)
      {
        k = i1;
        if (i1 == 2) {}
      }
    }
    label975:
    label976:
    label1018:
    label1109:
    label1125:
    label1168:
    label1183:
    Object localObject12;
    label1227:
    label1260:
    label1324:
    NotificationCompat.Builder localBuilder;
    Object localObject14;
    Object localObject16;
    Object localObject15;
    label1542:
    label1592:
    label1625:
    label1726:
    label1746:
    Object localObject11;
    try
    {
      j = audioManager.getRingerMode();
      k = i1;
      if (j != 0)
      {
        k = i1;
        if (j != 1) {
          k = 2;
        }
      }
    }
    catch (Exception localException2)
    {
      for (;;)
      {
        FileLog.e(localException2);
        k = i1;
      }
    }
    Object localObject9 = null;
    Object localObject7 = null;
    Object localObject8 = null;
    Object localObject1 = null;
    int i1 = 0;
    int j = i1;
    Object localObject17;
    if (Build.VERSION.SDK_INT >= 26) {
      if (k == 2)
      {
        localObject1 = new long[2];
        Object tmp967_965 = localObject1;
        tmp967_965[0] = 0L;
        Object tmp971_967 = tmp967_965;
        tmp971_967[1] = 0L;
        tmp971_967;
        break label2549;
        localObject4 = localObject7;
        if (localObject5 == null) {
          break label4025;
        }
        localObject4 = localObject7;
        if (((String)localObject5).equals("NoSound")) {
          break label4025;
        }
        if (!((String)localObject5).equals(str)) {
          break label2576;
        }
        localObject4 = Settings.System.DEFAULT_NOTIFICATION_URI;
        break label4025;
        localObject4 = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        ((Intent)localObject4).setAction("com.tmessages.openchat" + Math.random() + Integer.MAX_VALUE);
        ((Intent)localObject4).setFlags(32768);
        if ((int)l2 == 0) {
          break label2790;
        }
        if (this.pushDialogs.size() == 1)
        {
          if (i4 == 0) {
            break label2586;
          }
          ((Intent)localObject4).putExtra("chatId", i4);
        }
        if (AndroidUtilities.needShowPasscode(false)) {
          break label4071;
        }
        if (!SharedConfig.isWaitingForPasscodeEnter) {
          break label2605;
        }
        break label4071;
        ((Intent)localObject4).putExtra("currentAccount", this.currentAccount);
        localObject7 = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, (Intent)localObject4, 1073741824);
        m = 1;
        if ((i4 == 0) || (localObject10 != null)) {
          break label4077;
        }
        if (!localMessageObject1.isFcmMessage()) {
          break label2827;
        }
        localObject4 = localMessageObject1.localName;
        if (((int)l2 != 0) && (this.pushDialogs.size() <= 1) && (!AndroidUtilities.needShowPasscode(false)) && (!SharedConfig.isWaitingForPasscodeEnter)) {
          break label4254;
        }
        localObject12 = LocaleController.getString("AppName", 2131492981);
        m = 0;
        if (UserConfig.getActivatedAccountsCount() <= 1) {
          break label4261;
        }
        if (this.pushDialogs.size() != 1) {
          break label2852;
        }
        localObject5 = UserObject.getFirstName(UserConfig.getInstance(this.currentAccount).getCurrentUser());
        if (this.pushDialogs.size() == 1)
        {
          localObject6 = localObject5;
          if (Build.VERSION.SDK_INT >= 23) {}
        }
        else
        {
          if (this.pushDialogs.size() != 1) {
            break label2889;
          }
          localObject6 = (String)localObject5 + LocaleController.formatPluralString("NewMessages", this.total_unread_count);
        }
        localBuilder = new NotificationCompat.Builder(ApplicationLoader.applicationContext).setContentTitle((CharSequence)localObject12).setSmallIcon(2131165543).setAutoCancel(true).setNumber(this.total_unread_count).setContentIntent((PendingIntent)localObject7).setGroup(this.notificationGroup).setGroupSummary(true).setShowWhen(true).setWhen(localMessageObject1.messageOwner.date * 1000L).setColor(-13851168);
        localObject14 = null;
        i5 = 0;
        localObject16 = null;
        localObject15 = null;
        localBuilder.setCategory("msg");
        if ((localObject10 == null) && (localObject13 != null) && (((TLRPC.User)localObject13).phone != null) && (((TLRPC.User)localObject13).phone.length() > 0)) {
          localBuilder.addPerson("tel:+" + ((TLRPC.User)localObject13).phone);
        }
        i = 2;
        localObject5 = null;
        if (this.pushMessages.size() != 1) {
          break label3029;
        }
        localObject5 = (MessageObject)this.pushMessages.get(0);
        localObject17 = new boolean[1];
        localObject7 = getStringForMessage((MessageObject)localObject5, false, (boolean[])localObject17);
        localObject13 = localObject7;
        if (!((MessageObject)localObject5).messageOwner.silent) {
          break label4269;
        }
        i = 1;
        if (localObject13 == null) {
          break label4272;
        }
        localObject5 = localObject13;
        if (m != 0)
        {
          if (localObject10 == null) {
            break label2954;
          }
          localObject5 = ((String)localObject13).replace(" @ " + (String)localObject12, "");
        }
        localBuilder.setContentText((CharSequence)localObject5);
        localBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText((CharSequence)localObject5));
        k = i;
        localObject5 = localObject7;
        localObject7 = new Intent(ApplicationLoader.applicationContext, NotificationDismissReceiver.class);
        ((Intent)localObject7).putExtra("messageDate", localMessageObject1.messageOwner.date);
        ((Intent)localObject7).putExtra("currentAccount", this.currentAccount);
        localBuilder.setDeleteIntent(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 1, (Intent)localObject7, 134217728));
        if (localObject1 == null) {
          break label4085;
        }
        localObject7 = ImageLoader.getInstance().getImageFromMemory((TLObject)localObject1, null, "50_50");
        if (localObject7 == null) {
          break label3341;
        }
        localBuilder.setLargeIcon(((BitmapDrawable)localObject7).getBitmap());
        break label4085;
        localBuilder.setPriority(-1);
        i = i5;
        if (Build.VERSION.SDK_INT >= 26) {
          i = 2;
        }
        if ((k == 1) || (i3 != 0)) {
          break label3715;
        }
        if ((ApplicationLoader.mainInterfacePaused) || (bool3))
        {
          localObject1 = localObject5;
          if (((String)localObject5).length() > 100) {
            localObject1 = ((String)localObject5).substring(0, 100).replace('\n', ' ').trim() + "...";
          }
          localBuilder.setTicker((CharSequence)localObject1);
        }
        localObject5 = localObject15;
        if (!MediaController.getInstance().isRecordingAudio())
        {
          localObject5 = localObject15;
          if (localObject11 != null)
          {
            localObject5 = localObject15;
            if (!((String)localObject11).equals("NoSound"))
            {
              if (Build.VERSION.SDK_INT < 26) {
                break label3554;
              }
              if (!((String)localObject11).equals(str)) {
                break label3544;
              }
              localObject5 = Settings.System.DEFAULT_NOTIFICATION_URI;
            }
          }
        }
        label1889:
        if (n != 0) {
          localBuilder.setLights(n, 1000, 1000);
        }
        if ((i1 != 2) && (!MediaController.getInstance().isRecordingAudio())) {
          break label3600;
        }
        localObject1 = new long[2];
        Object tmp1930_1928 = localObject1;
        tmp1930_1928[0] = 0L;
        Object tmp1934_1930 = tmp1930_1928;
        tmp1934_1930[1] = 0L;
        tmp1934_1930;
        localBuilder.setVibrate((long[])localObject1);
        localObject7 = localObject5;
        label1951:
        m = 0;
        k = 0;
        i1 = m;
        if (AndroidUtilities.needShowPasscode(false)) {
          break label3746;
        }
        i1 = m;
        if (SharedConfig.isWaitingForPasscodeEnter) {
          break label3746;
        }
        i1 = m;
        if (localMessageObject1.getDialogId() != 777000L) {
          break label3746;
        }
        i1 = m;
        if (localMessageObject1.messageOwner.reply_markup == null) {
          break label3746;
        }
        localObject5 = localMessageObject1.messageOwner.reply_markup.rows;
        m = 0;
        i3 = ((ArrayList)localObject5).size();
      }
    }
    for (;;)
    {
      i1 = k;
      label2076:
      label2250:
      label2280:
      label2357:
      label2383:
      label2541:
      label2549:
      label2576:
      label2586:
      label2605:
      label2790:
      label2827:
      label2852:
      label2889:
      label2954:
      label3029:
      label3076:
      label3173:
      label3244:
      label3341:
      label3431:
      label3459:
      label3482:
      label3544:
      label3554:
      label3600:
      Object localObject3;
      if (m < i3)
      {
        localObject10 = (TLRPC.TL_keyboardButtonRow)((ArrayList)localObject5).get(m);
        i2 = 0;
        i4 = ((TLRPC.TL_keyboardButtonRow)localObject10).buttons.size();
        i1 = k;
        k = i2;
        if (k >= i4) {
          break label4325;
        }
        localObject12 = (TLRPC.KeyboardButton)((TLRPC.TL_keyboardButtonRow)localObject10).buttons.get(k);
        if (!(localObject12 instanceof TLRPC.TL_keyboardButtonCallback)) {
          break label4098;
        }
        localObject11 = new Intent(ApplicationLoader.applicationContext, NotificationCallbackReceiver.class);
        ((Intent)localObject11).putExtra("currentAccount", this.currentAccount);
        ((Intent)localObject11).putExtra("did", l2);
        if (((TLRPC.KeyboardButton)localObject12).data != null) {
          ((Intent)localObject11).putExtra("data", ((TLRPC.KeyboardButton)localObject12).data);
        }
        ((Intent)localObject11).putExtra("mid", localMessageObject1.getId());
        localObject12 = ((TLRPC.KeyboardButton)localObject12).text;
        localObject13 = ApplicationLoader.applicationContext;
        i1 = this.lastButtonId;
        this.lastButtonId = (i1 + 1);
        localBuilder.addAction(0, (CharSequence)localObject12, PendingIntent.getBroadcast((Context)localObject13, i1, (Intent)localObject11, 134217728));
        i1 = 1;
        break label4098;
        i4 = localMessageObject1.messageOwner.to_id.channel_id;
        break label165;
        m = i;
        if (i != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
          break label191;
        }
        m = localMessageObject1.messageOwner.from_id;
        break label191;
        if (((Point)localObject1).y + k < System.currentTimeMillis() / 1000L)
        {
          ((Point)localObject1).set(1, (int)(System.currentTimeMillis() / 1000L));
          i3 = i;
          break label493;
        }
        k = ((Point)localObject1).x;
        if (k >= j) {
          break label4118;
        }
        ((Point)localObject1).set(k + 1, (int)(System.currentTimeMillis() / 1000L));
        i3 = i;
        break label493;
        localObject4 = localObject1;
        if (localObject1 != null) {
          break label708;
        }
        localObject4 = ((SharedPreferences)localObject5).getString("GroupSoundPath", str);
        break label708;
        j = i6;
        localObject4 = localObject1;
        i = i5;
        k = n;
        if (m == 0) {
          break label748;
        }
        if ((localObject1 != null) && (((String)localObject1).equals(str))) {
          localObject4 = null;
        }
        for (;;)
        {
          i = ((SharedPreferences)localObject5).getInt("vibrate_messages", 0);
          k = ((SharedPreferences)localObject5).getInt("priority_group", 1);
          j = ((SharedPreferences)localObject5).getInt("MessagesLed", -16776961);
          break;
          localObject4 = localObject1;
          if (localObject1 == null) {
            localObject4 = ((SharedPreferences)localObject5).getString("GlobalSoundPath", str);
          }
        }
        if (k != 1) {
          break label4166;
        }
        Object localObject2 = new long[4];
        Object tmp2517_2515 = localObject2;
        tmp2517_2515[0] = 0L;
        Object tmp2521_2517 = tmp2517_2515;
        tmp2521_2517[1] = 100L;
        Object tmp2527_2521 = tmp2521_2517;
        tmp2527_2521[2] = 0L;
        Object tmp2531_2527 = tmp2527_2521;
        tmp2531_2527[3] = 100L;
        tmp2531_2527;
        break label976;
        localObject2 = new long[0];
        break label976;
        if (k != 3) {
          break label976;
        }
        localObject2 = new long[2];
        Object tmp2562_2560 = localObject2;
        tmp2562_2560[0] = 0L;
        Object tmp2566_2562 = tmp2562_2560;
        tmp2566_2562[1] = 1000L;
        tmp2566_2562;
        break label976;
        localObject4 = Uri.parse((String)localObject5);
        break label4025;
        if (m == 0) {
          break label1109;
        }
        ((Intent)localObject4).putExtra("userId", m);
        break label1109;
        localObject2 = localObject6;
        if (this.pushDialogs.size() != 1) {
          break label1125;
        }
        if (localObject10 != null)
        {
          localObject2 = localObject6;
          if (((TLRPC.Chat)localObject10).photo == null) {
            break label1125;
          }
          localObject2 = localObject6;
          if (((TLRPC.Chat)localObject10).photo.photo_small == null) {
            break label1125;
          }
          localObject2 = localObject6;
          if (((TLRPC.Chat)localObject10).photo.photo_small.volume_id == 0L) {
            break label1125;
          }
          localObject2 = localObject6;
          if (((TLRPC.Chat)localObject10).photo.photo_small.local_id == 0) {
            break label1125;
          }
          localObject2 = ((TLRPC.Chat)localObject10).photo.photo_small;
          break label1125;
        }
        localObject2 = localObject6;
        if (localObject13 == null) {
          break label1125;
        }
        localObject2 = localObject6;
        if (((TLRPC.User)localObject13).photo == null) {
          break label1125;
        }
        localObject2 = localObject6;
        if (((TLRPC.User)localObject13).photo.photo_small == null) {
          break label1125;
        }
        localObject2 = localObject6;
        if (((TLRPC.User)localObject13).photo.photo_small.volume_id == 0L) {
          break label1125;
        }
        localObject2 = localObject6;
        if (((TLRPC.User)localObject13).photo.photo_small.local_id == 0) {
          break label1125;
        }
        localObject2 = ((TLRPC.User)localObject13).photo.photo_small;
        break label1125;
        localObject2 = localObject6;
        if (this.pushDialogs.size() != 1) {
          break label1125;
        }
        ((Intent)localObject4).putExtra("encId", (int)(l2 >> 32));
        localObject2 = localObject6;
        break label1125;
        if (localObject10 != null)
        {
          localObject4 = ((TLRPC.Chat)localObject10).title;
          break label1183;
        }
        localObject4 = UserObject.getUserName((TLRPC.User)localObject13);
        break label1183;
        localObject5 = UserObject.getFirstName(UserConfig.getInstance(this.currentAccount).getCurrentUser()) + "・";
        break label1260;
        localObject6 = (String)localObject5 + LocaleController.formatString("NotificationMessagesPeopleDisplayOrder", 2131494002, new Object[] { LocaleController.formatPluralString("NewMessages", this.total_unread_count), LocaleController.formatPluralString("FromChats", this.pushDialogs.size()) });
        break label1324;
        if (localObject17[0] != 0)
        {
          localObject5 = ((String)localObject13).replace((String)localObject12 + ": ", "");
          break label1592;
        }
        localObject5 = ((String)localObject13).replace((String)localObject12 + " ", "");
        break label1592;
        localBuilder.setContentText((CharSequence)localObject6);
        localObject17 = new NotificationCompat.InboxStyle();
        ((NotificationCompat.InboxStyle)localObject17).setBigContentTitle((CharSequence)localObject12);
        i6 = Math.min(10, this.pushMessages.size());
        boolean[] arrayOfBoolean = new boolean[1];
        i4 = 0;
        if (i4 < i6)
        {
          MessageObject localMessageObject2 = (MessageObject)this.pushMessages.get(i4);
          localObject13 = getStringForMessage(localMessageObject2, false, arrayOfBoolean);
          localObject7 = localObject5;
          k = i;
          if (localObject13 == null) {
            break label4274;
          }
          if (localMessageObject2.messageOwner.date <= i8)
          {
            localObject7 = localObject5;
            k = i;
            break label4274;
          }
          localObject7 = localObject5;
          k = i;
          if (i == 2)
          {
            localObject7 = localObject13;
            if (!localMessageObject2.messageOwner.silent) {
              break label4290;
            }
            k = 1;
          }
          localObject5 = localObject13;
          if (this.pushDialogs.size() == 1)
          {
            localObject5 = localObject13;
            if (m != 0)
            {
              if (localObject10 == null) {
                break label3244;
              }
              localObject5 = ((String)localObject13).replace(" @ " + (String)localObject12, "");
            }
          }
          for (;;)
          {
            ((NotificationCompat.InboxStyle)localObject17).addLine((CharSequence)localObject5);
            break;
            if (arrayOfBoolean[0] != 0) {
              localObject5 = ((String)localObject13).replace((String)localObject12 + ": ", "");
            } else {
              localObject5 = ((String)localObject13).replace((String)localObject12 + " ", "");
            }
          }
        }
        ((NotificationCompat.InboxStyle)localObject17).setSummaryText((CharSequence)localObject6);
        localBuilder.setStyle((NotificationCompat.Style)localObject17);
        k = i;
        break label1625;
        for (;;)
        {
          float f;
          try
          {
            localObject2 = FileLoader.getPathToAttach((TLObject)localObject2, true);
            if (!((File)localObject2).exists()) {
              break;
            }
            f = 160.0F / AndroidUtilities.dp(50.0F);
            localObject7 = new BitmapFactory.Options();
            if (f < 1.0F)
            {
              i = 1;
              ((BitmapFactory.Options)localObject7).inSampleSize = i;
              localObject2 = BitmapFactory.decodeFile(((File)localObject2).getAbsolutePath(), (BitmapFactory.Options)localObject7);
              if (localObject2 == null) {
                break;
              }
              localBuilder.setLargeIcon((Bitmap)localObject2);
            }
          }
          catch (Throwable localThrowable) {}
          i = (int)f;
        }
        if (i2 != 0) {
          break label4296;
        }
        localBuilder.setPriority(0);
        i = i5;
        if (Build.VERSION.SDK_INT < 26) {
          break label1746;
        }
        i = 3;
        break label1746;
        localBuilder.setPriority(1);
        i = i5;
        if (Build.VERSION.SDK_INT < 26) {
          break label1746;
        }
        i = 4;
        break label1746;
        if (i2 == 4)
        {
          localBuilder.setPriority(-2);
          i = i5;
          if (Build.VERSION.SDK_INT < 26) {
            break label1746;
          }
          i = 1;
          break label1746;
        }
        i = i5;
        if (i2 != 5) {
          break label1746;
        }
        localBuilder.setPriority(-1);
        i = i5;
        if (Build.VERSION.SDK_INT < 26) {
          break label1746;
        }
        i = 2;
        break label1746;
        localObject5 = Uri.parse((String)localObject11);
        break label1889;
        if (((String)localObject11).equals(str))
        {
          localBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, 5);
          localObject5 = localObject15;
          break label1889;
        }
        localBuilder.setSound(Uri.parse((String)localObject11), 5);
        localObject5 = localObject15;
        break label1889;
        if (i1 != 1) {
          break label4311;
        }
        localObject3 = new long[4];
        Object tmp3613_3611 = localObject3;
        tmp3613_3611[0] = 0L;
        Object tmp3617_3613 = tmp3613_3611;
        tmp3617_3613[1] = 100L;
        Object tmp3623_3617 = tmp3617_3613;
        tmp3623_3617[2] = 0L;
        Object tmp3627_3623 = tmp3623_3617;
        tmp3627_3623[3] = 100L;
        tmp3627_3623;
        localBuilder.setVibrate((long[])localObject3);
        localObject7 = localObject5;
        break label1951;
      }
      for (;;)
      {
        label3649:
        localBuilder.setDefaults(2);
        localObject3 = new long[0];
        localObject7 = localObject5;
        break label1951;
        label3715:
        label3746:
        label3895:
        label3928:
        label3936:
        label3941:
        label4025:
        label4071:
        label4077:
        label4085:
        label4098:
        label4107:
        label4118:
        label4124:
        label4136:
        label4166:
        label4254:
        label4261:
        label4269:
        label4272:
        label4274:
        label4290:
        label4296:
        label4311:
        do
        {
          localObject3 = localObject14;
          localObject7 = localObject5;
          if (i1 != 3) {
            break label1951;
          }
          localObject3 = new long[2];
          tmp3689_3687 = localObject3;
          tmp3689_3687[0] = 0L;
          tmp3693_3689 = tmp3689_3687;
          tmp3693_3689[1] = 1000L;
          tmp3693_3689;
          localBuilder.setVibrate((long[])localObject3);
          localObject7 = localObject5;
          break label1951;
          localObject3 = new long[2];
          tmp3722_3720 = localObject3;
          tmp3722_3720[0] = 0L;
          tmp3726_3722 = tmp3722_3720;
          tmp3726_3722[1] = 0L;
          tmp3726_3722;
          localBuilder.setVibrate((long[])localObject3);
          localObject7 = localObject16;
          break label1951;
          if ((i1 == 0) && (Build.VERSION.SDK_INT < 24) && (SharedConfig.passcodeHash.length() == 0) && (hasMessagesToReply()))
          {
            localObject5 = new Intent(ApplicationLoader.applicationContext, PopupReplyReceiver.class);
            ((Intent)localObject5).putExtra("currentAccount", this.currentAccount);
            if (Build.VERSION.SDK_INT > 19) {
              break label3895;
            }
            localBuilder.addAction(2131165355, LocaleController.getString("Reply", 2131494235), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 2, (Intent)localObject5, 134217728));
          }
          for (;;)
          {
            if (Build.VERSION.SDK_INT >= 26) {
              localBuilder.setChannelId(validateChannelId(l2, (String)localObject4, (long[])localObject3, n, (Uri)localObject7, i, (long[])localObject8, (Uri)localObject9, j));
            }
            showExtraNotifications(localBuilder, paramBoolean, (String)localObject6);
            this.lastNotificationIsNoData = false;
            scheduleNotificationRepeat();
            return;
            localBuilder.addAction(2131165354, LocaleController.getString("Reply", 2131494235), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 2, (Intent)localObject5, 134217728));
          }
          i = j;
          if (k != 0) {
            break label312;
          }
          i = 1;
          break label312;
          if (i2 != 3) {
            k = i2;
          }
          i2 = i;
          i5 = i7;
          if (i == 4)
          {
            i5 = 1;
            i2 = 0;
          }
          if (((i2 != 2) || ((i1 != 1) && (i1 != 3))) && ((i2 == 2) || (i1 != 2)))
          {
            j = i2;
            if (i1 == 0) {
              break label826;
            }
            j = i2;
            if (i1 == 4) {
              break label826;
            }
          }
          j = i1;
          break label826;
          if (i == 0)
          {
            j = 3;
            localObject9 = localObject4;
            localObject8 = localObject3;
          }
          for (;;)
          {
            localObject11 = localObject5;
            i1 = k;
            i2 = i;
            if (i3 == 0) {
              break label1018;
            }
            i1 = 0;
            i2 = 0;
            n = 0;
            localObject11 = null;
            break label1018;
            localObject3 = null;
            break label1125;
            if (localObject13 != null) {
              break label2827;
            }
            break label1168;
            if (!paramBoolean) {
              break label1726;
            }
            if (k != 1) {
              break label3431;
            }
            break label1726;
            k += 1;
            break label2076;
            j = 2;
            k = 180;
            break;
            i3 = 1;
            break label493;
            i1 = 0;
            i2 = 3;
            localObject3 = null;
            break label682;
            localObject5 = localObject4;
            i1 = j;
            i = k;
            if (k != 2) {
              break label878;
            }
            i = 1;
            localObject5 = localObject4;
            i1 = j;
            break label878;
            if (k == 0) {
              break label2541;
            }
            if (k != 4) {
              break label975;
            }
            break label2541;
            if ((i == 1) || (i == 2))
            {
              j = 4;
              localObject8 = localObject3;
              localObject9 = localObject4;
            }
            else if (i == 4)
            {
              j = 1;
              localObject8 = localObject3;
              localObject9 = localObject4;
            }
            else
            {
              localObject8 = localObject3;
              localObject9 = localObject4;
              j = i1;
              if (i == 5)
              {
                j = 2;
                localObject8 = localObject3;
                localObject9 = localObject4;
              }
            }
          }
          localObject12 = localObject4;
          break label1227;
          localObject5 = "";
          break label1260;
          i = 0;
          break label1542;
          break;
          i4 += 1;
          localObject5 = localObject7;
          i = k;
          break label3076;
          k = 0;
          break label3173;
          if (i2 == 1) {
            break label3459;
          }
          if (i2 != 2) {
            break label3482;
          }
          break label3459;
          if (i1 == 0) {
            break label3649;
          }
        } while (i1 != 4);
      }
      label4325:
      m += 1;
      k = i1;
    }
  }
  
  @TargetApi(26)
  private String validateChannelId(long paramLong, String paramString, long[] paramArrayOfLong1, int paramInt1, Uri paramUri1, int paramInt2, long[] paramArrayOfLong2, Uri paramUri2, int paramInt3)
  {
    SharedPreferences localSharedPreferences = MessagesController.getNotificationsSettings(this.currentAccount);
    String str1 = "org.telegram.key" + paramLong;
    paramUri2 = localSharedPreferences.getString(str1, null);
    String str3 = localSharedPreferences.getString(str1 + "_s", null);
    paramArrayOfLong2 = new StringBuilder();
    paramInt3 = 0;
    while (paramInt3 < paramArrayOfLong1.length)
    {
      paramArrayOfLong2.append(paramArrayOfLong1[paramInt3]);
      paramInt3 += 1;
    }
    paramArrayOfLong2.append(paramInt1);
    if (paramUri1 != null) {
      paramArrayOfLong2.append(paramUri1.toString());
    }
    paramArrayOfLong2.append(paramInt2);
    String str2 = Utilities.MD5(paramArrayOfLong2.toString());
    paramArrayOfLong2 = paramUri2;
    if (paramUri2 != null)
    {
      paramArrayOfLong2 = paramUri2;
      if (!str3.equals(str2))
      {
        if (0 == 0) {
          break label449;
        }
        localSharedPreferences.edit().putString(str1, paramUri2).putString(str1 + "_s", str2).commit();
        paramArrayOfLong2 = paramUri2;
      }
    }
    paramUri2 = paramArrayOfLong2;
    if (paramArrayOfLong2 == null)
    {
      paramUri2 = this.currentAccount + "channel" + paramLong + "_" + Utilities.random.nextLong();
      paramString = new NotificationChannel(paramUri2, paramString, paramInt2);
      if (paramInt1 != 0)
      {
        paramString.enableLights(true);
        paramString.setLightColor(paramInt1);
      }
      if (isEmptyVibration(paramArrayOfLong1)) {
        break label463;
      }
      paramString.enableVibration(true);
      if ((paramArrayOfLong1 != null) && (paramArrayOfLong1.length > 0)) {
        paramString.setVibrationPattern(paramArrayOfLong1);
      }
      label350:
      if (paramUri1 == null) {
        break label471;
      }
      paramArrayOfLong1 = new AudioAttributes.Builder();
      paramArrayOfLong1.setContentType(4);
      paramArrayOfLong1.setUsage(5);
      paramString.setSound(paramUri1, paramArrayOfLong1.build());
    }
    for (;;)
    {
      systemNotificationManager.createNotificationChannel(paramString);
      localSharedPreferences.edit().putString(str1, paramUri2).putString(str1 + "_s", str2).commit();
      return paramUri2;
      label449:
      systemNotificationManager.deleteNotificationChannel(paramUri2);
      paramArrayOfLong2 = null;
      break;
      label463:
      paramString.enableVibration(false);
      break label350;
      label471:
      paramString.setSound(null, null);
    }
  }
  
  public void cleanup()
  {
    this.popupMessages.clear();
    this.popupReplyMessages.clear();
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        NotificationsController.access$602(NotificationsController.this, 0L);
        NotificationsController.access$702(NotificationsController.this, 0);
        NotificationsController.access$802(NotificationsController.this, 0);
        NotificationsController.this.pushMessages.clear();
        NotificationsController.this.pushMessagesDict.clear();
        NotificationsController.this.pushDialogs.clear();
        NotificationsController.this.wearNotificationsIds.clear();
        NotificationsController.this.lastWearNotifiedMessageId.clear();
        NotificationsController.this.delayedPushMessages.clear();
        NotificationsController.access$1402(NotificationsController.this, false);
        NotificationsController.access$1502(NotificationsController.this, 0);
        try
        {
          if (NotificationsController.this.notificationDelayWakelock.isHeld()) {
            NotificationsController.this.notificationDelayWakelock.release();
          }
          NotificationsController.this.setBadge(NotificationsController.access$1600(NotificationsController.this));
          localObject = MessagesController.getNotificationsSettings(NotificationsController.this.currentAccount).edit();
          ((SharedPreferences.Editor)localObject).clear();
          ((SharedPreferences.Editor)localObject).commit();
          if (Build.VERSION.SDK_INT < 26) {}
        }
        catch (Exception localException)
        {
          try
          {
            Object localObject = NotificationsController.this.currentAccount + "channel";
            List localList = NotificationsController.systemNotificationManager.getNotificationChannels();
            int j = localList.size();
            int i = 0;
            while (i < j)
            {
              String str = ((NotificationChannel)localList.get(i)).getId();
              if (str.startsWith((String)localObject)) {
                NotificationsController.systemNotificationManager.deleteNotificationChannel(str);
              }
              i += 1;
              continue;
              localException = localException;
              FileLog.e(localException);
            }
          }
          catch (Throwable localThrowable)
          {
            FileLog.e(localThrowable);
          }
        }
      }
    });
  }
  
  protected void forceShowPopupForReply()
  {
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final ArrayList localArrayList = new ArrayList();
        int i = 0;
        if (i < NotificationsController.this.pushMessages.size())
        {
          MessageObject localMessageObject = (MessageObject)NotificationsController.this.pushMessages.get(i);
          long l = localMessageObject.getDialogId();
          if (((localMessageObject.messageOwner.mentioned) && ((localMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionPinMessage))) || ((int)l == 0) || ((localMessageObject.messageOwner.to_id.channel_id != 0) && (!localMessageObject.isMegagroup()))) {}
          for (;;)
          {
            i += 1;
            break;
            localArrayList.add(0, localMessageObject);
          }
        }
        if ((!localArrayList.isEmpty()) && (!AndroidUtilities.needShowPasscode(false))) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationsController.this.popupReplyMessages = localArrayList;
              Intent localIntent = new Intent(ApplicationLoader.applicationContext, PopupNotificationActivity.class);
              localIntent.putExtra("force", true);
              localIntent.putExtra("currentAccount", NotificationsController.this.currentAccount);
              localIntent.setFlags(268763140);
              ApplicationLoader.applicationContext.startActivity(localIntent);
              localIntent = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
              ApplicationLoader.applicationContext.sendBroadcast(localIntent);
            }
          });
        }
      }
    });
  }
  
  public int getTotalUnreadCount()
  {
    return this.total_unread_count;
  }
  
  public boolean hasMessagesToReply()
  {
    int i = 0;
    while (i < this.pushMessages.size())
    {
      MessageObject localMessageObject = (MessageObject)this.pushMessages.get(i);
      long l = localMessageObject.getDialogId();
      if (((localMessageObject.messageOwner.mentioned) && ((localMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionPinMessage))) || ((int)l == 0) || ((localMessageObject.messageOwner.to_id.channel_id != 0) && (!localMessageObject.isMegagroup()))) {
        i += 1;
      } else {
        return true;
      }
    }
    return false;
  }
  
  public void playOutChatSound()
  {
    if ((!this.inChatSoundEnabled) || (MediaController.getInstance().isRecordingAudio())) {}
    for (;;)
    {
      return;
      try
      {
        int i = audioManager.getRingerMode();
        if (i == 0) {}
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
        }
      }
    }
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (Math.abs(System.currentTimeMillis() - NotificationsController.this.lastSoundOutPlay) <= 100L) {
            return;
          }
          NotificationsController.access$3202(NotificationsController.this, System.currentTimeMillis());
          if (NotificationsController.this.soundPool == null)
          {
            NotificationsController.access$2802(NotificationsController.this, new SoundPool(3, 1, 0));
            NotificationsController.this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
            {
              public void onLoadComplete(SoundPool paramAnonymous2SoundPool, int paramAnonymous2Int1, int paramAnonymous2Int2)
              {
                if (paramAnonymous2Int2 == 0) {}
                try
                {
                  paramAnonymous2SoundPool.play(paramAnonymous2Int1, 1.0F, 1.0F, 1, 0, 1.0F);
                  return;
                }
                catch (Exception paramAnonymous2SoundPool)
                {
                  FileLog.e(paramAnonymous2SoundPool);
                }
              }
            });
          }
          if ((NotificationsController.this.soundOut == 0) && (!NotificationsController.this.soundOutLoaded))
          {
            NotificationsController.access$3402(NotificationsController.this, true);
            NotificationsController.access$3302(NotificationsController.this, NotificationsController.this.soundPool.load(ApplicationLoader.applicationContext, 2131427329, 1));
          }
          int i = NotificationsController.this.soundOut;
          if (i != 0) {
            try
            {
              NotificationsController.this.soundPool.play(NotificationsController.this.soundOut, 1.0F, 1.0F, 1, 0, 1.0F);
              return;
            }
            catch (Exception localException1)
            {
              FileLog.e(localException1);
              return;
            }
          }
          return;
        }
        catch (Exception localException2)
        {
          FileLog.e(localException2);
        }
      }
    });
  }
  
  public void processDialogsUpdateRead(final LongSparseArray<Integer> paramLongSparseArray)
  {
    final ArrayList localArrayList = new ArrayList();
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int m = NotificationsController.this.total_unread_count;
        SharedPreferences localSharedPreferences = MessagesController.getNotificationsSettings(NotificationsController.this.currentAccount);
        int j = 0;
        if (j < paramLongSparseArray.size())
        {
          long l3 = paramLongSparseArray.keyAt(j);
          int k = NotificationsController.this.getNotifyOverride(localSharedPreferences, l3);
          int i = k;
          if (NotificationsController.this.notifyCheck)
          {
            localObject = (Integer)NotificationsController.this.pushDialogsOverrideMention.get(l3);
            i = k;
            if (localObject != null)
            {
              i = k;
              if (((Integer)localObject).intValue() == 1)
              {
                NotificationsController.this.pushDialogsOverrideMention.put(l3, Integer.valueOf(0));
                i = 1;
              }
            }
          }
          label164:
          Integer localInteger2;
          Integer localInteger1;
          if ((i != 2) && (((localSharedPreferences.getBoolean("EnableAll", true)) && (((int)l3 >= 0) || (localSharedPreferences.getBoolean("EnableGroup", true)))) || (i != 0)))
          {
            i = 1;
            localInteger2 = (Integer)NotificationsController.this.pushDialogs.get(l3);
            localInteger1 = (Integer)paramLongSparseArray.get(l3);
            if (localInteger1.intValue() == 0) {
              NotificationsController.this.smartNotificationsDialogs.remove(l3);
            }
            localObject = localInteger1;
            if (localInteger1.intValue() >= 0) {
              break label260;
            }
            if (localInteger2 != null) {
              break label244;
            }
          }
          for (;;)
          {
            j += 1;
            break;
            i = 0;
            break label164;
            label244:
            localObject = Integer.valueOf(localInteger2.intValue() + localInteger1.intValue());
            label260:
            if (((i != 0) || (((Integer)localObject).intValue() == 0)) && (localInteger2 != null)) {
              NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count - localInteger2.intValue());
            }
            if (((Integer)localObject).intValue() == 0)
            {
              NotificationsController.this.pushDialogs.remove(l3);
              NotificationsController.this.pushDialogsOverrideMention.remove(l3);
              for (i = 0; i < NotificationsController.this.pushMessages.size(); i = k + 1)
              {
                localObject = (MessageObject)NotificationsController.this.pushMessages.get(i);
                k = i;
                if (((MessageObject)localObject).getDialogId() == l3)
                {
                  if (NotificationsController.this.isPersonalMessage((MessageObject)localObject)) {
                    NotificationsController.access$810(NotificationsController.this);
                  }
                  NotificationsController.this.pushMessages.remove(i);
                  k = i - 1;
                  NotificationsController.this.delayedPushMessages.remove(localObject);
                  long l2 = ((MessageObject)localObject).getId();
                  long l1 = l2;
                  if (((MessageObject)localObject).messageOwner.to_id.channel_id != 0) {
                    l1 = l2 | ((MessageObject)localObject).messageOwner.to_id.channel_id << 32;
                  }
                  NotificationsController.this.pushMessagesDict.remove(l1);
                  localArrayList.add(localObject);
                }
              }
            }
            else if (i != 0)
            {
              NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count + ((Integer)localObject).intValue());
              NotificationsController.this.pushDialogs.put(l3, localObject);
            }
          }
        }
        if (!localArrayList.isEmpty()) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              int i = 0;
              int j = NotificationsController.10.this.val$popupArrayToRemove.size();
              while (i < j)
              {
                NotificationsController.this.popupMessages.remove(NotificationsController.10.this.val$popupArrayToRemove.get(i));
                i += 1;
              }
            }
          });
        }
        if (m != NotificationsController.this.total_unread_count)
        {
          if (!NotificationsController.this.notifyCheck)
          {
            NotificationsController.this.delayedPushMessages.clear();
            NotificationsController.this.showOrUpdateNotification(NotificationsController.this.notifyCheck);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, new Object[] { Integer.valueOf(NotificationsController.this.currentAccount) });
              }
            });
          }
        }
        else
        {
          NotificationsController.access$1402(NotificationsController.this, false);
          if (NotificationsController.this.showBadgeNumber) {
            NotificationsController.this.setBadge(NotificationsController.access$1600(NotificationsController.this));
          }
          return;
        }
        Object localObject = NotificationsController.this;
        if (NotificationsController.this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance(NotificationsController.this.currentAccount).getCurrentTime()) {}
        for (boolean bool = true;; bool = false)
        {
          ((NotificationsController)localObject).scheduleNotificationDelay(bool);
          break;
        }
      }
    });
  }
  
  public void processLoadedUnreadMessages(final LongSparseArray<Integer> paramLongSparseArray, final ArrayList<TLRPC.Message> paramArrayList, ArrayList<TLRPC.User> paramArrayList1, ArrayList<TLRPC.Chat> paramArrayList2, ArrayList<TLRPC.EncryptedChat> paramArrayList3)
  {
    MessagesController.getInstance(this.currentAccount).putUsers(paramArrayList1, true);
    MessagesController.getInstance(this.currentAccount).putChats(paramArrayList2, true);
    MessagesController.getInstance(this.currentAccount).putEncryptedChats(paramArrayList3, true);
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        NotificationsController.this.pushDialogs.clear();
        NotificationsController.this.pushMessages.clear();
        NotificationsController.this.pushMessagesDict.clear();
        NotificationsController.access$702(NotificationsController.this, 0);
        NotificationsController.access$802(NotificationsController.this, 0);
        Object localObject1 = MessagesController.getNotificationsSettings(NotificationsController.this.currentAccount);
        LongSparseArray localLongSparseArray = new LongSparseArray();
        Object localObject2;
        long l1;
        int j;
        if (paramArrayList != null)
        {
          i = 0;
          if (i < paramArrayList.size())
          {
            localObject2 = (TLRPC.Message)paramArrayList.get(i);
            l1 = ((TLRPC.Message)localObject2).id;
            long l2 = l1;
            if (((TLRPC.Message)localObject2).to_id.channel_id != 0) {
              l2 = l1 | ((TLRPC.Message)localObject2).to_id.channel_id << 32;
            }
            if (NotificationsController.this.pushMessagesDict.indexOfKey(l2) >= 0) {}
            for (;;)
            {
              i += 1;
              break;
              localObject2 = new MessageObject(NotificationsController.this.currentAccount, (TLRPC.Message)localObject2, false);
              if (NotificationsController.this.isPersonalMessage((MessageObject)localObject2)) {
                NotificationsController.access$808(NotificationsController.this);
              }
              long l3 = ((MessageObject)localObject2).getDialogId();
              l1 = l3;
              if (((MessageObject)localObject2).messageOwner.mentioned) {
                l1 = ((MessageObject)localObject2).messageOwner.from_id;
              }
              j = localLongSparseArray.indexOfKey(l1);
              if (j < 0) {
                break label340;
              }
              bool = ((Boolean)localLongSparseArray.valueAt(j)).booleanValue();
              label262:
              if ((!bool) || ((l1 == NotificationsController.this.opened_dialog_id) && (ApplicationLoader.isScreenOn))) {
                break label409;
              }
              NotificationsController.this.pushMessagesDict.put(l2, localObject2);
              NotificationsController.this.pushMessages.add(0, localObject2);
              if (l3 != l1) {
                NotificationsController.this.pushDialogsOverrideMention.put(l3, Integer.valueOf(1));
              }
            }
            label340:
            j = NotificationsController.this.getNotifyOverride((SharedPreferences)localObject1, l1);
            if ((j != 2) && (((((SharedPreferences)localObject1).getBoolean("EnableAll", true)) && (((int)l1 >= 0) || (((SharedPreferences)localObject1).getBoolean("EnableGroup", true)))) || (j != 0))) {}
            for (bool = true;; bool = false)
            {
              localLongSparseArray.put(l1, Boolean.valueOf(bool));
              break label262;
              label409:
              break;
            }
          }
        }
        int i = 0;
        if (i < paramLongSparseArray.size())
        {
          l1 = paramLongSparseArray.keyAt(i);
          j = localLongSparseArray.indexOfKey(l1);
          if (j >= 0)
          {
            bool = ((Boolean)localLongSparseArray.valueAt(j)).booleanValue();
            if (bool) {
              break label608;
            }
          }
          for (;;)
          {
            i += 1;
            break;
            int k = NotificationsController.this.getNotifyOverride((SharedPreferences)localObject1, l1);
            localObject2 = (Integer)NotificationsController.this.pushDialogsOverrideMention.get(l1);
            j = k;
            if (localObject2 != null)
            {
              j = k;
              if (((Integer)localObject2).intValue() == 1)
              {
                NotificationsController.this.pushDialogsOverrideMention.put(l1, Integer.valueOf(0));
                j = 1;
              }
            }
            if ((j != 2) && (((((SharedPreferences)localObject1).getBoolean("EnableAll", true)) && (((int)l1 >= 0) || (((SharedPreferences)localObject1).getBoolean("EnableGroup", true)))) || (j != 0))) {}
            for (bool = true;; bool = false)
            {
              localLongSparseArray.put(l1, Boolean.valueOf(bool));
              break;
            }
            label608:
            j = ((Integer)paramLongSparseArray.valueAt(i)).intValue();
            NotificationsController.this.pushDialogs.put(l1, Integer.valueOf(j));
            NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count + j);
          }
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (NotificationsController.this.total_unread_count == 0)
            {
              NotificationsController.this.popupMessages.clear();
              NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
            }
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, new Object[] { Integer.valueOf(NotificationsController.this.currentAccount) });
          }
        });
        localObject1 = NotificationsController.this;
        if (SystemClock.uptimeMillis() / 1000L < 60L) {}
        for (boolean bool = true;; bool = false)
        {
          ((NotificationsController)localObject1).showOrUpdateNotification(bool);
          if (NotificationsController.this.showBadgeNumber) {
            NotificationsController.this.setBadge(NotificationsController.access$1600(NotificationsController.this));
          }
          return;
        }
      }
    });
  }
  
  public void processNewMessages(final ArrayList<MessageObject> paramArrayList, final boolean paramBoolean1, final boolean paramBoolean2)
  {
    if (paramArrayList.isEmpty()) {
      return;
    }
    final ArrayList localArrayList = new ArrayList(0);
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int j = 0;
        LongSparseArray localLongSparseArray = new LongSparseArray();
        Object localObject2 = MessagesController.getNotificationsSettings(NotificationsController.this.currentAccount);
        boolean bool2 = ((SharedPreferences)localObject2).getBoolean("PinnedMessages", true);
        final int i = 0;
        int k = 0;
        long l1;
        if (k < paramArrayList.size())
        {
          MessageObject localMessageObject = (MessageObject)paramArrayList.get(k);
          l1 = localMessageObject.getId();
          long l2 = l1;
          if (localMessageObject.messageOwner.to_id.channel_id != 0) {
            l2 = l1 | localMessageObject.messageOwner.to_id.channel_id << 32;
          }
          localObject1 = (MessageObject)NotificationsController.this.pushMessagesDict.get(l2);
          int n;
          if (localObject1 != null)
          {
            m = j;
            n = i;
            if (((MessageObject)localObject1).isFcmMessage())
            {
              NotificationsController.this.pushMessagesDict.put(l2, localMessageObject);
              i1 = NotificationsController.this.pushMessages.indexOf(localObject1);
              m = j;
              n = i;
              if (i1 >= 0)
              {
                NotificationsController.this.pushMessages.set(i1, localMessageObject);
                n = i;
                m = j;
              }
            }
          }
          long l3;
          label273:
          do
          {
            for (;;)
            {
              k += 1;
              j = m;
              i = n;
              break;
              l3 = localMessageObject.getDialogId();
              if ((l3 != NotificationsController.this.opened_dialog_id) || (!ApplicationLoader.isScreenOn)) {
                break label273;
              }
              m = j;
              n = i;
              if (!paramBoolean2)
              {
                NotificationsController.this.playInChatSound();
                m = j;
                n = i;
              }
            }
            l1 = l3;
            if (!localMessageObject.messageOwner.mentioned) {
              break label324;
            }
            if (bool2) {
              break label313;
            }
            m = j;
            n = i;
          } while ((localMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionPinMessage));
          label313:
          l1 = localMessageObject.messageOwner.from_id;
          label324:
          if (NotificationsController.this.isPersonalMessage(localMessageObject)) {
            NotificationsController.access$808(NotificationsController.this);
          }
          int i1 = 1;
          int m = (int)l1;
          if (m < 0)
          {
            j = 1;
            label359:
            n = localLongSparseArray.indexOfKey(l1);
            if (n < 0) {
              break label624;
            }
            bool1 = ((Boolean)localLongSparseArray.valueAt(n)).booleanValue();
            if (m != 0)
            {
              if (!((SharedPreferences)localObject2).getBoolean("custom_" + l1, false)) {
                break label702;
              }
              j = ((SharedPreferences)localObject2).getInt("popup_" + l1, 0);
              label453:
              if (j != 0) {
                break label714;
              }
              if ((int)l1 >= 0) {
                break label707;
              }
              localObject1 = "popupGroup";
              label467:
              i = ((SharedPreferences)localObject2).getInt((String)localObject1, 0);
            }
          }
          for (;;)
          {
            j = i;
            if (i != 0)
            {
              j = i;
              if (localMessageObject.messageOwner.to_id.channel_id != 0)
              {
                j = i;
                if (!localMessageObject.isMegagroup()) {
                  j = 0;
                }
              }
            }
            m = i1;
            n = j;
            if (!bool1) {
              break;
            }
            if (j != 0) {
              localArrayList.add(0, localMessageObject);
            }
            NotificationsController.this.delayedPushMessages.add(localMessageObject);
            NotificationsController.this.pushMessages.add(0, localMessageObject);
            NotificationsController.this.pushMessagesDict.put(l2, localMessageObject);
            m = i1;
            n = j;
            if (l3 == l1) {
              break;
            }
            NotificationsController.this.pushDialogsOverrideMention.put(l3, Integer.valueOf(1));
            m = i1;
            n = j;
            break;
            j = 0;
            break label359;
            label624:
            n = NotificationsController.this.getNotifyOverride((SharedPreferences)localObject2, l1);
            if ((n != 2) && (((((SharedPreferences)localObject2).getBoolean("EnableAll", true)) && ((j == 0) || (((SharedPreferences)localObject2).getBoolean("EnableGroup", true)))) || (n != 0))) {}
            for (bool1 = true;; bool1 = false)
            {
              localLongSparseArray.put(l1, Boolean.valueOf(bool1));
              break;
            }
            label702:
            j = 0;
            break label453;
            label707:
            localObject1 = "popupAll";
            break label467;
            label714:
            if (j == 1)
            {
              i = 3;
            }
            else
            {
              i = j;
              if (j == 2) {
                i = 0;
              }
            }
          }
        }
        if (j != 0) {
          NotificationsController.access$1402(NotificationsController.this, paramBoolean1);
        }
        if ((!localArrayList.isEmpty()) && (!AndroidUtilities.needShowPasscode(false))) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationsController.this.popupMessages.addAll(0, NotificationsController.9.this.val$popupArrayAdd);
              if (((ApplicationLoader.mainInterfacePaused) || ((!ApplicationLoader.isScreenOn) && (!SharedConfig.isWaitingForPasscodeEnter))) && ((i == 3) || ((i == 1) && (ApplicationLoader.isScreenOn)) || ((i == 2) && (!ApplicationLoader.isScreenOn))))
              {
                Intent localIntent = new Intent(ApplicationLoader.applicationContext, PopupNotificationActivity.class);
                localIntent.setFlags(268763140);
                ApplicationLoader.applicationContext.startActivity(localIntent);
              }
            }
          });
        }
        if ((j != 0) && (paramBoolean2))
        {
          l1 = ((MessageObject)paramArrayList.get(0)).getDialogId();
          k = NotificationsController.this.total_unread_count;
          j = NotificationsController.this.getNotifyOverride((SharedPreferences)localObject2, l1);
          i = j;
          if (NotificationsController.this.notifyCheck)
          {
            localObject1 = (Integer)NotificationsController.this.pushDialogsOverrideMention.get(l1);
            i = j;
            if (localObject1 != null)
            {
              i = j;
              if (((Integer)localObject1).intValue() == 1)
              {
                NotificationsController.this.pushDialogsOverrideMention.put(l1, Integer.valueOf(0));
                i = 1;
              }
            }
          }
          if ((i == 2) || (((!((SharedPreferences)localObject2).getBoolean("EnableAll", true)) || (((int)l1 < 0) && (!((SharedPreferences)localObject2).getBoolean("EnableGroup", true)))) && (i == 0))) {
            break label1127;
          }
          i = 1;
          localObject1 = (Integer)NotificationsController.this.pushDialogs.get(l1);
          if (localObject1 == null) {
            break label1132;
          }
        }
        label1127:
        label1132:
        for (j = ((Integer)localObject1).intValue() + 1;; j = 1)
        {
          localObject2 = Integer.valueOf(j);
          if (i != 0)
          {
            if (localObject1 != null) {
              NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count - ((Integer)localObject1).intValue());
            }
            NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count + ((Integer)localObject2).intValue());
            NotificationsController.this.pushDialogs.put(l1, localObject2);
          }
          if (k != NotificationsController.this.total_unread_count)
          {
            if (NotificationsController.this.notifyCheck) {
              break label1137;
            }
            NotificationsController.this.delayedPushMessages.clear();
            NotificationsController.this.showOrUpdateNotification(NotificationsController.this.notifyCheck);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, new Object[] { Integer.valueOf(NotificationsController.this.currentAccount) });
              }
            });
          }
          NotificationsController.access$1402(NotificationsController.this, false);
          if (NotificationsController.this.showBadgeNumber) {
            NotificationsController.this.setBadge(NotificationsController.access$1600(NotificationsController.this));
          }
          return;
          i = 0;
          break;
        }
        label1137:
        Object localObject1 = NotificationsController.this;
        if (NotificationsController.this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance(NotificationsController.this.currentAccount).getCurrentTime()) {}
        for (boolean bool1 = true;; bool1 = false)
        {
          ((NotificationsController)localObject1).scheduleNotificationDelay(bool1);
          break;
        }
      }
    });
  }
  
  public void processReadMessages(final SparseLongArray paramSparseLongArray, final long paramLong, final int paramInt1, int paramInt2, final boolean paramBoolean)
  {
    final ArrayList localArrayList = new ArrayList(0);
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int j;
        int i;
        MessageObject localMessageObject;
        int k;
        long l2;
        long l1;
        if (paramSparseLongArray != null)
        {
          j = 0;
          while (j < paramSparseLongArray.size())
          {
            int m = paramSparseLongArray.keyAt(j);
            long l3 = paramSparseLongArray.get(m);
            for (i = 0; i < NotificationsController.this.pushMessages.size(); i = k + 1)
            {
              localMessageObject = (MessageObject)NotificationsController.this.pushMessages.get(i);
              k = i;
              if (localMessageObject.getDialogId() == m)
              {
                k = i;
                if (localMessageObject.getId() <= (int)l3)
                {
                  if (NotificationsController.this.isPersonalMessage(localMessageObject)) {
                    NotificationsController.access$810(NotificationsController.this);
                  }
                  localArrayList.add(localMessageObject);
                  l2 = localMessageObject.getId();
                  l1 = l2;
                  if (localMessageObject.messageOwner.to_id.channel_id != 0) {
                    l1 = l2 | localMessageObject.messageOwner.to_id.channel_id << 32;
                  }
                  NotificationsController.this.pushMessagesDict.remove(l1);
                  NotificationsController.this.delayedPushMessages.remove(localMessageObject);
                  NotificationsController.this.pushMessages.remove(i);
                  k = i - 1;
                }
              }
            }
            j += 1;
          }
        }
        if ((paramLong != 0L) && ((paramInt1 != 0) || (paramBoolean != 0)))
        {
          j = 0;
          if (j < NotificationsController.this.pushMessages.size())
          {
            localMessageObject = (MessageObject)NotificationsController.this.pushMessages.get(j);
            k = j;
            if (localMessageObject.getDialogId() == paramLong)
            {
              i = 0;
              if (paramBoolean == 0) {
                break label457;
              }
              if (localMessageObject.messageOwner.date <= paramBoolean) {
                i = 1;
              }
            }
            for (;;)
            {
              k = j;
              if (i != 0)
              {
                if (NotificationsController.this.isPersonalMessage(localMessageObject)) {
                  NotificationsController.access$810(NotificationsController.this);
                }
                NotificationsController.this.pushMessages.remove(j);
                NotificationsController.this.delayedPushMessages.remove(localMessageObject);
                localArrayList.add(localMessageObject);
                l2 = localMessageObject.getId();
                l1 = l2;
                if (localMessageObject.messageOwner.to_id.channel_id != 0) {
                  l1 = l2 | localMessageObject.messageOwner.to_id.channel_id << 32;
                }
                NotificationsController.this.pushMessagesDict.remove(l1);
                k = j - 1;
              }
              j = k + 1;
              break;
              label457:
              if (!this.val$isPopup)
              {
                if ((localMessageObject.getId() <= paramInt1) || (paramInt1 < 0)) {
                  i = 1;
                }
              }
              else if ((localMessageObject.getId() == paramInt1) || (paramInt1 < 0)) {
                i = 1;
              }
            }
          }
        }
        if (!localArrayList.isEmpty()) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              int i = 0;
              int j = NotificationsController.8.this.val$popupArrayRemove.size();
              while (i < j)
              {
                NotificationsController.this.popupMessages.remove(NotificationsController.8.this.val$popupArrayRemove.get(i));
                i += 1;
              }
              NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
            }
          });
        }
      }
    });
  }
  
  public void removeDeletedHisoryFromNotifications(final SparseIntArray paramSparseIntArray)
  {
    final ArrayList localArrayList = new ArrayList(0);
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int m = NotificationsController.this.total_unread_count;
        MessagesController.getNotificationsSettings(NotificationsController.this.currentAccount);
        int j = 0;
        while (j < paramSparseIntArray.size())
        {
          int i = paramSparseIntArray.keyAt(j);
          long l = -i;
          int n = paramSparseIntArray.get(i);
          Object localObject2 = (Integer)NotificationsController.this.pushDialogs.get(l);
          localObject1 = localObject2;
          if (localObject2 == null) {
            localObject1 = Integer.valueOf(0);
          }
          localObject2 = localObject1;
          i = 0;
          while (i < NotificationsController.this.pushMessages.size())
          {
            MessageObject localMessageObject = (MessageObject)NotificationsController.this.pushMessages.get(i);
            int k = i;
            localObject3 = localObject2;
            if (localMessageObject.getDialogId() == l)
            {
              k = i;
              localObject3 = localObject2;
              if (localMessageObject.getId() <= n)
              {
                NotificationsController.this.pushMessagesDict.remove(localMessageObject.getIdWithChannel());
                NotificationsController.this.delayedPushMessages.remove(localMessageObject);
                NotificationsController.this.pushMessages.remove(localMessageObject);
                k = i - 1;
                if (NotificationsController.this.isPersonalMessage(localMessageObject)) {
                  NotificationsController.access$810(NotificationsController.this);
                }
                localArrayList.add(localMessageObject);
                localObject3 = Integer.valueOf(((Integer)localObject2).intValue() - 1);
              }
            }
            i = k + 1;
            localObject2 = localObject3;
          }
          Object localObject3 = localObject2;
          if (((Integer)localObject2).intValue() <= 0)
          {
            localObject3 = Integer.valueOf(0);
            NotificationsController.this.smartNotificationsDialogs.remove(l);
          }
          if (!((Integer)localObject3).equals(localObject1))
          {
            NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count - ((Integer)localObject1).intValue());
            NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count + ((Integer)localObject3).intValue());
            NotificationsController.this.pushDialogs.put(l, localObject3);
          }
          if (((Integer)localObject3).intValue() == 0)
          {
            NotificationsController.this.pushDialogs.remove(l);
            NotificationsController.this.pushDialogsOverrideMention.remove(l);
          }
          j += 1;
        }
        if (localArrayList.isEmpty()) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              int i = 0;
              int j = NotificationsController.7.this.val$popupArrayRemove.size();
              while (i < j)
              {
                NotificationsController.this.popupMessages.remove(NotificationsController.7.this.val$popupArrayRemove.get(i));
                i += 1;
              }
            }
          });
        }
        if (m != NotificationsController.this.total_unread_count)
        {
          if (!NotificationsController.this.notifyCheck)
          {
            NotificationsController.this.delayedPushMessages.clear();
            NotificationsController.this.showOrUpdateNotification(NotificationsController.this.notifyCheck);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, new Object[] { Integer.valueOf(NotificationsController.this.currentAccount) });
              }
            });
          }
        }
        else
        {
          NotificationsController.access$1402(NotificationsController.this, false);
          if (NotificationsController.this.showBadgeNumber) {
            NotificationsController.this.setBadge(NotificationsController.access$1600(NotificationsController.this));
          }
          return;
        }
        Object localObject1 = NotificationsController.this;
        if (NotificationsController.this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance(NotificationsController.this.currentAccount).getCurrentTime()) {}
        for (boolean bool = true;; bool = false)
        {
          ((NotificationsController)localObject1).scheduleNotificationDelay(bool);
          break;
        }
      }
    });
  }
  
  public void removeDeletedMessagesFromNotifications(final SparseArray<ArrayList<Integer>> paramSparseArray)
  {
    final ArrayList localArrayList = new ArrayList(0);
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int k = NotificationsController.this.total_unread_count;
        MessagesController.getNotificationsSettings(NotificationsController.this.currentAccount);
        int i = 0;
        while (i < paramSparseArray.size())
        {
          int m = paramSparseArray.keyAt(i);
          long l1 = -m;
          ArrayList localArrayList = (ArrayList)paramSparseArray.get(m);
          Object localObject2 = (Integer)NotificationsController.this.pushDialogs.get(l1);
          localObject1 = localObject2;
          if (localObject2 == null) {
            localObject1 = Integer.valueOf(0);
          }
          localObject2 = localObject1;
          int j = 0;
          while (j < localArrayList.size())
          {
            long l2 = ((Integer)localArrayList.get(j)).intValue() | m << 32;
            MessageObject localMessageObject = (MessageObject)NotificationsController.this.pushMessagesDict.get(l2);
            localObject3 = localObject2;
            if (localMessageObject != null)
            {
              NotificationsController.this.pushMessagesDict.remove(l2);
              NotificationsController.this.delayedPushMessages.remove(localMessageObject);
              NotificationsController.this.pushMessages.remove(localMessageObject);
              if (NotificationsController.this.isPersonalMessage(localMessageObject)) {
                NotificationsController.access$810(NotificationsController.this);
              }
              localArrayList.add(localMessageObject);
              localObject3 = Integer.valueOf(((Integer)localObject2).intValue() - 1);
            }
            j += 1;
            localObject2 = localObject3;
          }
          Object localObject3 = localObject2;
          if (((Integer)localObject2).intValue() <= 0)
          {
            localObject3 = Integer.valueOf(0);
            NotificationsController.this.smartNotificationsDialogs.remove(l1);
          }
          if (!((Integer)localObject3).equals(localObject1))
          {
            NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count - ((Integer)localObject1).intValue());
            NotificationsController.access$702(NotificationsController.this, NotificationsController.this.total_unread_count + ((Integer)localObject3).intValue());
            NotificationsController.this.pushDialogs.put(l1, localObject3);
          }
          if (((Integer)localObject3).intValue() == 0)
          {
            NotificationsController.this.pushDialogs.remove(l1);
            NotificationsController.this.pushDialogsOverrideMention.remove(l1);
          }
          i += 1;
        }
        if (!localArrayList.isEmpty()) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              int i = 0;
              int j = NotificationsController.6.this.val$popupArrayRemove.size();
              while (i < j)
              {
                NotificationsController.this.popupMessages.remove(NotificationsController.6.this.val$popupArrayRemove.get(i));
                i += 1;
              }
            }
          });
        }
        if (k != NotificationsController.this.total_unread_count)
        {
          if (!NotificationsController.this.notifyCheck)
          {
            NotificationsController.this.delayedPushMessages.clear();
            NotificationsController.this.showOrUpdateNotification(NotificationsController.this.notifyCheck);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.notificationsCountUpdated, new Object[] { Integer.valueOf(NotificationsController.this.currentAccount) });
              }
            });
          }
        }
        else
        {
          NotificationsController.access$1402(NotificationsController.this, false);
          if (NotificationsController.this.showBadgeNumber) {
            NotificationsController.this.setBadge(NotificationsController.access$1600(NotificationsController.this));
          }
          return;
        }
        Object localObject1 = NotificationsController.this;
        if (NotificationsController.this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance(NotificationsController.this.currentAccount).getCurrentTime()) {}
        for (boolean bool = true;; bool = false)
        {
          ((NotificationsController)localObject1).scheduleNotificationDelay(bool);
          break;
        }
      }
    });
  }
  
  public void removeNotificationsForDialog(long paramLong)
  {
    getInstance(this.currentAccount).processReadMessages(null, paramLong, 0, Integer.MAX_VALUE, false);
    LongSparseArray localLongSparseArray = new LongSparseArray();
    localLongSparseArray.put(paramLong, Integer.valueOf(0));
    getInstance(this.currentAccount).processDialogsUpdateRead(localLongSparseArray);
  }
  
  protected void repeatNotificationMaybe()
  {
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i = Calendar.getInstance().get(11);
        if ((i >= 11) && (i <= 22))
        {
          NotificationsController.notificationManager.cancel(NotificationsController.this.notificationId);
          NotificationsController.this.showOrUpdateNotification(true);
          return;
        }
        NotificationsController.this.scheduleNotificationRepeat();
      }
    });
  }
  
  public void setBadgeEnabled(boolean paramBoolean)
  {
    this.showBadgeNumber = paramBoolean;
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        NotificationsController.this.setBadge(NotificationsController.access$1600(NotificationsController.this));
      }
    });
  }
  
  public void setInChatSoundEnabled(boolean paramBoolean)
  {
    this.inChatSoundEnabled = paramBoolean;
  }
  
  public void setLastOnlineFromOtherDevice(final int paramInt)
  {
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("set last online from other device = " + paramInt);
        }
        NotificationsController.access$2002(NotificationsController.this, paramInt);
      }
    });
  }
  
  public void setOpenedDialogId(final long paramLong)
  {
    notificationsQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        NotificationsController.access$602(NotificationsController.this, paramLong);
      }
    });
  }
  
  public void updateServerNotificationsSettings(long paramLong)
  {
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
    if ((int)paramLong == 0) {
      return;
    }
    SharedPreferences localSharedPreferences = MessagesController.getNotificationsSettings(this.currentAccount);
    TLRPC.TL_account_updateNotifySettings localTL_account_updateNotifySettings = new TLRPC.TL_account_updateNotifySettings();
    localTL_account_updateNotifySettings.settings = new TLRPC.TL_inputPeerNotifySettings();
    localTL_account_updateNotifySettings.settings.sound = "default";
    int i = localSharedPreferences.getInt("notify2_" + paramLong, 0);
    if (i == 3)
    {
      localTL_account_updateNotifySettings.settings.mute_until = localSharedPreferences.getInt("notifyuntil_" + paramLong, 0);
      localTL_account_updateNotifySettings.settings.show_previews = localSharedPreferences.getBoolean("preview_" + paramLong, true);
      localTL_account_updateNotifySettings.settings.silent = localSharedPreferences.getBoolean("silent_" + paramLong, false);
      localTL_account_updateNotifySettings.peer = new TLRPC.TL_inputNotifyPeer();
      ((TLRPC.TL_inputNotifyPeer)localTL_account_updateNotifySettings.peer).peer = MessagesController.getInstance(this.currentAccount).getInputPeer((int)paramLong);
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_updateNotifySettings, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
      });
      return;
    }
    TLRPC.TL_inputPeerNotifySettings localTL_inputPeerNotifySettings = localTL_account_updateNotifySettings.settings;
    if (i != 2) {}
    for (i = 0;; i = Integer.MAX_VALUE)
    {
      localTL_inputPeerNotifySettings.mute_until = i;
      break;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/NotificationsController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */