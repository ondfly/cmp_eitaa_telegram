package org.telegram.messenger.voip;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount.Builder;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.StatsController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.BottomSheet.BottomSheetCell;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.VoIPPermissionActivity;

public abstract class VoIPBaseService
  extends Service
  implements SensorEventListener, AudioManager.OnAudioFocusChangeListener, NotificationCenter.NotificationCenterDelegate, VoIPController.ConnectionStateListener
{
  public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
  public static final int AUDIO_ROUTE_BLUETOOTH = 2;
  public static final int AUDIO_ROUTE_EARPIECE = 0;
  public static final int AUDIO_ROUTE_SPEAKER = 1;
  public static final int DISCARD_REASON_DISCONNECT = 2;
  public static final int DISCARD_REASON_HANGUP = 1;
  public static final int DISCARD_REASON_LINE_BUSY = 4;
  public static final int DISCARD_REASON_MISSED = 3;
  protected static final int ID_INCOMING_CALL_NOTIFICATION = 202;
  protected static final int ID_ONGOING_CALL_NOTIFICATION = 201;
  protected static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
  public static final int STATE_ENDED = 11;
  public static final int STATE_ESTABLISHED = 3;
  public static final int STATE_FAILED = 4;
  public static final int STATE_RECONNECTING = 5;
  public static final int STATE_WAIT_INIT = 1;
  public static final int STATE_WAIT_INIT_ACK = 2;
  protected static final boolean USE_CONNECTION_SERVICE = ;
  protected static VoIPBaseService sharedInstance;
  protected Runnable afterSoundRunnable = new Runnable()
  {
    public void run()
    {
      VoIPBaseService.this.soundPool.release();
      if (VoIPBaseService.USE_CONNECTION_SERVICE) {
        return;
      }
      if (VoIPBaseService.this.isBtHeadsetConnected) {
        ((AudioManager)ApplicationLoader.applicationContext.getSystemService("audio")).stopBluetoothSco();
      }
      ((AudioManager)ApplicationLoader.applicationContext.getSystemService("audio")).setSpeakerphoneOn(false);
    }
  };
  protected boolean audioConfigured;
  protected int audioRouteToSet = 2;
  protected boolean bluetoothScoActive = false;
  protected BluetoothAdapter btAdapter;
  protected int callDiscardReason;
  protected VoIPController controller;
  protected boolean controllerStarted;
  protected PowerManager.WakeLock cpuWakelock;
  protected int currentAccount = -1;
  protected int currentState = 0;
  protected boolean haveAudioFocus;
  protected boolean isBtHeadsetConnected;
  protected boolean isHeadsetPlugged;
  protected boolean isOutgoing;
  protected boolean isProximityNear;
  protected int lastError;
  protected long lastKnownDuration = 0L;
  protected NetworkInfo lastNetInfo;
  private Boolean mHasEarpiece = null;
  protected boolean micMute;
  protected boolean needPlayEndSound;
  protected boolean needSwitchToBluetoothAfterScoActivates = false;
  protected Notification ongoingCallNotification;
  protected boolean playingSound;
  protected VoIPController.Stats prevStats = new VoIPController.Stats();
  protected PowerManager.WakeLock proximityWakelock;
  protected BroadcastReceiver receiver = new BroadcastReceiver()
  {
    public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
    {
      boolean bool2 = true;
      boolean bool1 = true;
      if ("android.intent.action.HEADSET_PLUG".equals(paramAnonymousIntent.getAction()))
      {
        paramAnonymousContext = VoIPBaseService.this;
        if (paramAnonymousIntent.getIntExtra("state", 0) == 1)
        {
          paramAnonymousContext.isHeadsetPlugged = bool1;
          if ((VoIPBaseService.this.isHeadsetPlugged) && (VoIPBaseService.this.proximityWakelock != null) && (VoIPBaseService.this.proximityWakelock.isHeld())) {
            VoIPBaseService.this.proximityWakelock.release();
          }
          VoIPBaseService.this.isProximityNear = false;
          VoIPBaseService.this.updateOutputGainControlState();
        }
      }
      do
      {
        do
        {
          return;
          bool1 = false;
          break;
          if ("android.net.conn.CONNECTIVITY_CHANGE".equals(paramAnonymousIntent.getAction()))
          {
            VoIPBaseService.this.updateNetworkType();
            return;
          }
          if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(paramAnonymousIntent.getAction()))
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.e("bt headset state = " + paramAnonymousIntent.getIntExtra("android.bluetooth.profile.extra.STATE", 0));
            }
            paramAnonymousContext = VoIPBaseService.this;
            if (paramAnonymousIntent.getIntExtra("android.bluetooth.profile.extra.STATE", 0) == 2) {}
            for (bool1 = bool2;; bool1 = false)
            {
              paramAnonymousContext.updateBluetoothHeadsetState(bool1);
              return;
            }
          }
          if ("android.media.ACTION_SCO_AUDIO_STATE_UPDATED".equals(paramAnonymousIntent.getAction()))
          {
            int i = paramAnonymousIntent.getIntExtra("android.media.extra.SCO_AUDIO_STATE", 0);
            if (BuildVars.LOGS_ENABLED) {
              FileLog.e("Bluetooth SCO state updated: " + i);
            }
            if ((i == 0) && (VoIPBaseService.this.isBtHeadsetConnected) && ((!VoIPBaseService.this.btAdapter.isEnabled()) || (VoIPBaseService.this.btAdapter.getProfileConnectionState(1) != 2)))
            {
              VoIPBaseService.this.updateBluetoothHeadsetState(false);
              return;
            }
            paramAnonymousContext = VoIPBaseService.this;
            if (i == 1) {}
            for (bool1 = true;; bool1 = false)
            {
              paramAnonymousContext.bluetoothScoActive = bool1;
              if ((VoIPBaseService.this.bluetoothScoActive) && (VoIPBaseService.this.needSwitchToBluetoothAfterScoActivates))
              {
                VoIPBaseService.this.needSwitchToBluetoothAfterScoActivates = false;
                paramAnonymousContext = (AudioManager)VoIPBaseService.this.getSystemService("audio");
                paramAnonymousContext.setSpeakerphoneOn(false);
                paramAnonymousContext.setBluetoothScoOn(true);
              }
              paramAnonymousContext = VoIPBaseService.this.stateListeners.iterator();
              while (paramAnonymousContext.hasNext()) {
                ((VoIPBaseService.StateListener)paramAnonymousContext.next()).onAudioSettingsChanged();
              }
              break;
            }
          }
        } while (!"android.intent.action.PHONE_STATE".equals(paramAnonymousIntent.getAction()));
        paramAnonymousContext = paramAnonymousIntent.getStringExtra("state");
      } while (!TelephonyManager.EXTRA_STATE_OFFHOOK.equals(paramAnonymousContext));
      VoIPBaseService.this.hangUp();
    }
  };
  protected MediaPlayer ringtonePlayer;
  protected int signalBarCount;
  protected SoundPool soundPool;
  protected int spBusyId;
  protected int spConnectingId;
  protected int spEndId;
  protected int spFailedID;
  protected int spPlayID;
  protected int spRingbackID;
  protected boolean speakerphoneStateToSet;
  protected ArrayList<StateListener> stateListeners = new ArrayList();
  protected VoIPController.Stats stats = new VoIPController.Stats();
  protected CallConnection systemCallConnection;
  protected Runnable timeoutRunnable;
  protected Vibrator vibrator;
  private boolean wasEstablished;
  
  private void acceptIncomingCallFromNotification()
  {
    showNotification();
    if ((Build.VERSION.SDK_INT >= 23) && (checkSelfPermission("android.permission.RECORD_AUDIO") != 0)) {}
    do
    {
      try
      {
        PendingIntent.getActivity(this, 0, new Intent(this, VoIPPermissionActivity.class).addFlags(268435456), 0).send();
        return;
      }
      catch (Exception localException1)
      {
        while (!BuildVars.LOGS_ENABLED) {}
        FileLog.e("Error starting permission activity", localException1);
        return;
      }
      acceptIncomingCall();
      try
      {
        PendingIntent.getActivity(this, 0, new Intent(this, getUIActivityClass()).addFlags(805306368), 0).send();
        return;
      }
      catch (Exception localException2) {}
    } while (!BuildVars.LOGS_ENABLED);
    FileLog.e("Error starting incall activity", localException2);
  }
  
  public static VoIPBaseService getSharedInstance()
  {
    return sharedInstance;
  }
  
  public static boolean isAnyKindOfCallActive()
  {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (VoIPService.getSharedInstance() != null)
    {
      bool1 = bool2;
      if (VoIPService.getSharedInstance().getCallState() != 15) {
        bool1 = true;
      }
    }
    return bool1;
  }
  
  private static boolean isDeviceCompatibleWithConnectionServiceAPI()
  {
    if (Build.VERSION.SDK_INT < 26) {}
    while ((!"angler".equals(Build.PRODUCT)) && (!"bullhead".equals(Build.PRODUCT)) && (!"sailfish".equals(Build.PRODUCT)) && (!"marlin".equals(Build.PRODUCT)) && (!"walleye".equals(Build.PRODUCT)) && (!"taimen".equals(Build.PRODUCT))) {
      return false;
    }
    return true;
  }
  
  public abstract void acceptIncomingCall();
  
  @TargetApi(26)
  protected PhoneAccountHandle addAccountToTelecomManager()
  {
    TelecomManager localTelecomManager = (TelecomManager)getSystemService("telecom");
    TLRPC.User localUser = UserConfig.getInstance(this.currentAccount).getCurrentUser();
    PhoneAccountHandle localPhoneAccountHandle = new PhoneAccountHandle(new ComponentName(this, TelegramConnectionService.class), "" + localUser.id);
    localTelecomManager.registerPhoneAccount(new PhoneAccount.Builder(localPhoneAccountHandle, ContactsController.formatName(localUser.first_name, localUser.last_name)).setCapabilities(2048).setIcon(Icon.createWithResource(this, 2131165384)).setHighlightColor(-13851168).addSupportedUriScheme("sip").build());
    return localPhoneAccountHandle;
  }
  
  protected void callEnded()
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("Call " + getCallID() + " ended");
    }
    dispatchStateChanged(11);
    if (this.needPlayEndSound)
    {
      this.playingSound = true;
      this.soundPool.play(this.spEndId, 1.0F, 1.0F, 0, 0, 1.0F);
      AndroidUtilities.runOnUIThread(this.afterSoundRunnable, 700L);
    }
    if (this.timeoutRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.timeoutRunnable);
      this.timeoutRunnable = null;
    }
    if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null)) {}
    switch (this.callDiscardReason)
    {
    default: 
      this.systemCallConnection.setDisconnected(new DisconnectCause(3));
    case 1: 
    case 2: 
    case 4: 
      for (;;)
      {
        this.systemCallConnection.destroy();
        this.systemCallConnection = null;
        stopSelf();
        return;
        localCallConnection = this.systemCallConnection;
        if (this.isOutgoing) {}
        for (i = 2;; i = 6)
        {
          localCallConnection.setDisconnected(new DisconnectCause(i));
          break;
        }
        this.systemCallConnection.setDisconnected(new DisconnectCause(1));
        continue;
        this.systemCallConnection.setDisconnected(new DisconnectCause(7));
      }
    }
    CallConnection localCallConnection = this.systemCallConnection;
    if (this.isOutgoing) {}
    for (int i = 4;; i = 5)
    {
      localCallConnection.setDisconnected(new DisconnectCause(i));
      break;
    }
  }
  
  protected void callFailed()
  {
    if ((this.controller != null) && (this.controllerStarted)) {}
    for (int i = this.controller.getLastError();; i = 0)
    {
      callFailed(i);
      return;
    }
  }
  
  protected void callFailed(int paramInt)
  {
    try
    {
      throw new Exception("Call " + getCallID() + " failed with error code " + paramInt);
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
      this.lastError = paramInt;
      dispatchStateChanged(4);
      if ((paramInt != -3) && (this.soundPool != null))
      {
        this.playingSound = true;
        this.soundPool.play(this.spFailedID, 1.0F, 1.0F, 0, 0, 1.0F);
        AndroidUtilities.runOnUIThread(this.afterSoundRunnable, 1000L);
      }
      if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null))
      {
        this.systemCallConnection.setDisconnected(new DisconnectCause(1));
        this.systemCallConnection.destroy();
        this.systemCallConnection = null;
      }
      stopSelf();
    }
  }
  
  void callFailedFromConnectionService()
  {
    if (this.isOutgoing)
    {
      callFailed(-5);
      return;
    }
    hangUp();
  }
  
  protected void configureDeviceForCall()
  {
    int i = 5;
    this.needPlayEndSound = true;
    Object localObject = (AudioManager)getSystemService("audio");
    if (!USE_CONNECTION_SERVICE)
    {
      ((AudioManager)localObject).setMode(3);
      ((AudioManager)localObject).requestAudioFocus(this, 0, 1);
      if ((isBluetoothHeadsetConnected()) && (hasEarpiece())) {
        switch (this.audioRouteToSet)
        {
        }
      }
    }
    for (;;)
    {
      updateOutputGainControlState();
      this.audioConfigured = true;
      localObject = (SensorManager)getSystemService("sensor");
      Sensor localSensor = ((SensorManager)localObject).getDefaultSensor(8);
      if (localSensor != null) {}
      try
      {
        this.proximityWakelock = ((PowerManager)getSystemService("power")).newWakeLock(32, "telegram-voip-prx");
        ((SensorManager)localObject).registerListener(this, localSensor, 3);
        return;
        ((AudioManager)localObject).setBluetoothScoOn(true);
        ((AudioManager)localObject).setSpeakerphoneOn(false);
        continue;
        ((AudioManager)localObject).setBluetoothScoOn(false);
        ((AudioManager)localObject).setSpeakerphoneOn(false);
        continue;
        ((AudioManager)localObject).setBluetoothScoOn(false);
        ((AudioManager)localObject).setSpeakerphoneOn(true);
        continue;
        if (isBluetoothHeadsetConnected())
        {
          ((AudioManager)localObject).setBluetoothScoOn(this.speakerphoneStateToSet);
        }
        else
        {
          ((AudioManager)localObject).setSpeakerphoneOn(this.speakerphoneStateToSet);
          continue;
          if ((isBluetoothHeadsetConnected()) && (hasEarpiece())) {}
          switch (this.audioRouteToSet)
          {
          default: 
            break;
          case 0: 
            this.systemCallConnection.setAudioRoute(5);
            break;
          case 2: 
            this.systemCallConnection.setAudioRoute(2);
            break;
          case 1: 
            this.systemCallConnection.setAudioRoute(8);
            continue;
            if (hasEarpiece())
            {
              localObject = this.systemCallConnection;
              if (!this.speakerphoneStateToSet) {}
              for (;;)
              {
                ((CallConnection)localObject).setAudioRoute(i);
                break;
                i = 8;
              }
            }
            localObject = this.systemCallConnection;
            if (!this.speakerphoneStateToSet) {}
            for (;;)
            {
              ((CallConnection)localObject).setAudioRoute(i);
              break;
              i = 2;
            }
          }
        }
      }
      catch (Exception localException)
      {
        while (!BuildVars.LOGS_ENABLED) {}
        FileLog.e("Error initializing proximity sensor", localException);
      }
    }
  }
  
  protected VoIPController createController()
  {
    return new VoIPController();
  }
  
  public abstract void declineIncomingCall();
  
  public abstract void declineIncomingCall(int paramInt, Runnable paramRunnable);
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.appDidLogout) {
      callEnded();
    }
  }
  
  protected void dispatchStateChanged(int paramInt)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("== Call " + getCallID() + " state changed to " + paramInt + " ==");
    }
    this.currentState = paramInt;
    if ((USE_CONNECTION_SERVICE) && (paramInt == 3) && (this.systemCallConnection != null)) {
      this.systemCallConnection.setActive();
    }
    int i = 0;
    while (i < this.stateListeners.size())
    {
      ((StateListener)this.stateListeners.get(i)).onStateChanged(paramInt);
      i += 1;
    }
  }
  
  public int getAccount()
  {
    return this.currentAccount;
  }
  
  public long getCallDuration()
  {
    if ((!this.controllerStarted) || (this.controller == null)) {
      return this.lastKnownDuration;
    }
    long l = this.controller.getCallDuration();
    this.lastKnownDuration = l;
    return l;
  }
  
  public abstract long getCallID();
  
  public int getCallState()
  {
    return this.currentState;
  }
  
  public abstract CallConnection getConnectionAndStartCall();
  
  public int getCurrentAudioRoute()
  {
    int i = 2;
    if (USE_CONNECTION_SERVICE)
    {
      if ((this.systemCallConnection != null) && (this.systemCallConnection.getCallAudioState() != null)) {}
      switch (this.systemCallConnection.getCallAudioState().getRoute())
      {
      case 3: 
      case 5: 
      case 6: 
      case 7: 
      default: 
        i = this.audioRouteToSet;
      }
    }
    AudioManager localAudioManager;
    do
    {
      return i;
      return 0;
      return 1;
      if (!this.audioConfigured) {
        break;
      }
      localAudioManager = (AudioManager)getSystemService("audio");
    } while (localAudioManager.isBluetoothScoOn());
    if (localAudioManager.isSpeakerphoneOn()) {
      return 1;
    }
    return 0;
    return this.audioRouteToSet;
  }
  
  public String getDebugString()
  {
    return this.controller.getDebugString();
  }
  
  public int getLastError()
  {
    return this.lastError;
  }
  
  protected Bitmap getRoundAvatarBitmap(TLObject paramTLObject)
  {
    Object localObject4 = null;
    Object localObject5;
    Object localObject1;
    if ((paramTLObject instanceof TLRPC.User))
    {
      localObject5 = (TLRPC.User)paramTLObject;
      localObject1 = localObject4;
      if (((TLRPC.User)localObject5).photo != null)
      {
        localObject1 = localObject4;
        if (((TLRPC.User)localObject5).photo.photo_small != null)
        {
          localObject1 = ImageLoader.getInstance().getImageFromMemory(((TLRPC.User)localObject5).photo.photo_small, null, "50_50");
          if (localObject1 == null) {
            break label231;
          }
          localObject1 = ((BitmapDrawable)localObject1).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        }
      }
      localObject4 = localObject1;
      if (localObject1 == null)
      {
        Theme.createDialogsResources(this);
        if (!(paramTLObject instanceof TLRPC.User)) {
          break label390;
        }
      }
    }
    label231:
    label390:
    for (paramTLObject = new AvatarDrawable((TLRPC.User)paramTLObject);; paramTLObject = new AvatarDrawable((TLRPC.Chat)paramTLObject))
    {
      localObject4 = Bitmap.createBitmap(AndroidUtilities.dp(42.0F), AndroidUtilities.dp(42.0F), Bitmap.Config.ARGB_8888);
      paramTLObject.setBounds(0, 0, ((Bitmap)localObject4).getWidth(), ((Bitmap)localObject4).getHeight());
      paramTLObject.draw(new Canvas((Bitmap)localObject4));
      paramTLObject = new Canvas((Bitmap)localObject4);
      localObject1 = new Path();
      ((Path)localObject1).addCircle(((Bitmap)localObject4).getWidth() / 2, ((Bitmap)localObject4).getHeight() / 2, ((Bitmap)localObject4).getWidth() / 2, Path.Direction.CW);
      ((Path)localObject1).toggleInverseFillType();
      localObject5 = new Paint(1);
      ((Paint)localObject5).setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      paramTLObject.drawPath((Path)localObject1, (Paint)localObject5);
      return (Bitmap)localObject4;
      try
      {
        localObject1 = new BitmapFactory.Options();
        ((BitmapFactory.Options)localObject1).inMutable = true;
        localObject1 = BitmapFactory.decodeFile(FileLoader.getPathToAttach(((TLRPC.User)localObject5).photo.photo_small, true).toString(), (BitmapFactory.Options)localObject1);
      }
      catch (Throwable localThrowable1)
      {
        FileLog.e(localThrowable1);
        localObject2 = localObject4;
      }
      break;
      localObject5 = (TLRPC.Chat)paramTLObject;
      Object localObject2 = localObject4;
      if (((TLRPC.Chat)localObject5).photo == null) {
        break;
      }
      localObject2 = localObject4;
      if (((TLRPC.Chat)localObject5).photo.photo_small == null) {
        break;
      }
      localObject2 = ImageLoader.getInstance().getImageFromMemory(((TLRPC.Chat)localObject5).photo.photo_small, null, "50_50");
      if (localObject2 != null)
      {
        localObject2 = ((BitmapDrawable)localObject2).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        break;
      }
      try
      {
        localObject2 = new BitmapFactory.Options();
        ((BitmapFactory.Options)localObject2).inMutable = true;
        localObject2 = BitmapFactory.decodeFile(FileLoader.getPathToAttach(((TLRPC.Chat)localObject5).photo.photo_small, true).toString(), (BitmapFactory.Options)localObject2);
      }
      catch (Throwable localThrowable2)
      {
        FileLog.e(localThrowable2);
        Object localObject3 = localObject4;
      }
      break;
    }
  }
  
  protected int getStatsNetworkType()
  {
    int j = 1;
    int i = j;
    if (this.lastNetInfo != null)
    {
      i = j;
      if (this.lastNetInfo.getType() == 0)
      {
        if (!this.lastNetInfo.isRoaming()) {
          break label37;
        }
        i = 2;
      }
    }
    return i;
    label37:
    return 0;
  }
  
  protected abstract Class<? extends Activity> getUIActivityClass();
  
  public void handleNotificationAction(Intent paramIntent)
  {
    if ((getPackageName() + ".END_CALL").equals(paramIntent.getAction()))
    {
      stopForeground(true);
      hangUp();
    }
    do
    {
      return;
      if ((getPackageName() + ".DECLINE_CALL").equals(paramIntent.getAction()))
      {
        stopForeground(true);
        declineIncomingCall(4, null);
        return;
      }
    } while (!(getPackageName() + ".ANSWER_CALL").equals(paramIntent.getAction()));
    acceptIncomingCallFromNotification();
  }
  
  public abstract void hangUp();
  
  public abstract void hangUp(Runnable paramRunnable);
  
  public boolean hasEarpiece()
  {
    boolean bool = false;
    if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null) && (this.systemCallConnection.getCallAudioState() != null))
    {
      if ((this.systemCallConnection.getCallAudioState().getSupportedRouteMask() & 0x5) != 0) {
        bool = true;
      }
      return bool;
    }
    if (((TelephonyManager)getSystemService("phone")).getPhoneType() != 0) {
      return true;
    }
    if (this.mHasEarpiece != null) {
      return this.mHasEarpiece.booleanValue();
    }
    try
    {
      AudioManager localAudioManager = (AudioManager)getSystemService("audio");
      Method localMethod = AudioManager.class.getMethod("getDevicesForStream", new Class[] { Integer.TYPE });
      int i = AudioManager.class.getField("DEVICE_OUT_EARPIECE").getInt(null);
      if ((((Integer)localMethod.invoke(localAudioManager, new Object[] { Integer.valueOf(0) })).intValue() & i) == i) {}
      for (this.mHasEarpiece = Boolean.TRUE;; this.mHasEarpiece = Boolean.FALSE) {
        return this.mHasEarpiece.booleanValue();
      }
    }
    catch (Throwable localThrowable)
    {
      for (;;)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("Error while checking earpiece! ", localThrowable);
        }
        this.mHasEarpiece = Boolean.TRUE;
      }
    }
  }
  
  protected void initializeAccountRelatedThings()
  {
    updateServerConfig();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.appDidLogout);
    ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
    this.controller = createController();
    this.controller.setConnectionStateListener(this);
  }
  
  public boolean isBluetoothHeadsetConnected()
  {
    if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null) && (this.systemCallConnection.getCallAudioState() != null)) {
      return (this.systemCallConnection.getCallAudioState().getSupportedRouteMask() & 0x2) != 0;
    }
    return this.isBtHeadsetConnected;
  }
  
  protected boolean isFinished()
  {
    return (this.currentState == 11) || (this.currentState == 4);
  }
  
  public boolean isMicMute()
  {
    return this.micMute;
  }
  
  public boolean isOutgoing()
  {
    return this.isOutgoing;
  }
  
  public boolean isSpeakerphoneOn()
  {
    if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null) && (this.systemCallConnection.getCallAudioState() != null))
    {
      int i = this.systemCallConnection.getCallAudioState().getRoute();
      if (hasEarpiece()) {
        if (i != 8) {}
      }
      while (i == 2)
      {
        return true;
        return false;
      }
      return false;
    }
    if ((this.audioConfigured) && (!USE_CONNECTION_SERVICE))
    {
      AudioManager localAudioManager = (AudioManager)getSystemService("audio");
      if (hasEarpiece()) {
        return localAudioManager.isSpeakerphoneOn();
      }
      return localAudioManager.isBluetoothScoOn();
    }
    return this.speakerphoneStateToSet;
  }
  
  public void onAccuracyChanged(Sensor paramSensor, int paramInt) {}
  
  public void onAudioFocusChange(int paramInt)
  {
    if (paramInt == 1)
    {
      this.haveAudioFocus = true;
      return;
    }
    this.haveAudioFocus = false;
  }
  
  public void onConnectionStateChanged(int paramInt)
  {
    if (paramInt == 4)
    {
      callFailed();
      return;
    }
    if (paramInt == 3)
    {
      if (this.spPlayID != 0)
      {
        this.soundPool.stop(this.spPlayID);
        this.spPlayID = 0;
      }
      if (!this.wasEstablished)
      {
        this.wasEstablished = true;
        if (!this.isProximityNear)
        {
          Vibrator localVibrator = (Vibrator)getSystemService("vibrator");
          if (localVibrator.hasVibrator()) {
            localVibrator.vibrate(100L);
          }
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (VoIPBaseService.this.controller == null) {
              return;
            }
            int i = VoIPBaseService.this.getStatsNetworkType();
            StatsController.getInstance(VoIPBaseService.this.currentAccount).incrementTotalCallsTime(i, 5);
            AndroidUtilities.runOnUIThread(this, 5000L);
          }
        }, 5000L);
        if (!this.isOutgoing) {
          break label168;
        }
        StatsController.getInstance(this.currentAccount).incrementSentItemsCount(getStatsNetworkType(), 0, 1);
      }
    }
    for (;;)
    {
      if (paramInt == 5)
      {
        if (this.spPlayID != 0) {
          this.soundPool.stop(this.spPlayID);
        }
        this.spPlayID = this.soundPool.play(this.spConnectingId, 1.0F, 1.0F, 0, -1, 1.0F);
      }
      dispatchStateChanged(paramInt);
      return;
      label168:
      StatsController.getInstance(this.currentAccount).incrementReceivedItemsCount(getStatsNetworkType(), 0, 1);
    }
  }
  
  protected void onControllerPreRelease() {}
  
  public void onCreate()
  {
    super.onCreate();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("=============== VoIPService STARTING ===============");
    }
    AudioManager localAudioManager = (AudioManager)getSystemService("audio");
    if ((Build.VERSION.SDK_INT >= 17) && (localAudioManager.getProperty("android.media.property.OUTPUT_FRAMES_PER_BUFFER") != null)) {
      VoIPController.setNativeBufferSize(Integer.parseInt(localAudioManager.getProperty("android.media.property.OUTPUT_FRAMES_PER_BUFFER")));
    }
    for (;;)
    {
      try
      {
        this.cpuWakelock = ((PowerManager)getSystemService("power")).newWakeLock(1, "telegram-voip");
        this.cpuWakelock.acquire();
        if (!localAudioManager.isBluetoothScoAvailableOffCall()) {
          break label387;
        }
        Object localObject1 = BluetoothAdapter.getDefaultAdapter();
        this.btAdapter = ((BluetoothAdapter)localObject1);
        localObject1 = new IntentFilter();
        ((IntentFilter)localObject1).addAction("android.net.conn.CONNECTIVITY_CHANGE");
        if (!USE_CONNECTION_SERVICE)
        {
          ((IntentFilter)localObject1).addAction("android.intent.action.HEADSET_PLUG");
          if (this.btAdapter != null)
          {
            ((IntentFilter)localObject1).addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
            ((IntentFilter)localObject1).addAction("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
          }
          ((IntentFilter)localObject1).addAction("android.intent.action.PHONE_STATE");
        }
        registerReceiver(this.receiver, (IntentFilter)localObject1);
        this.soundPool = new SoundPool(1, 0, 0);
        this.spConnectingId = this.soundPool.load(this, 2131427331, 1);
        this.spRingbackID = this.soundPool.load(this, 2131427334, 1);
        this.spFailedID = this.soundPool.load(this, 2131427333, 1);
        this.spEndId = this.soundPool.load(this, 2131427332, 1);
        this.spBusyId = this.soundPool.load(this, 2131427330, 1);
        localAudioManager.registerMediaButtonEventReceiver(new ComponentName(this, VoIPMediaButtonReceiver.class));
        if ((!USE_CONNECTION_SERVICE) && (this.btAdapter != null) && (this.btAdapter.isEnabled()))
        {
          if (this.btAdapter.getProfileConnectionState(1) != 2) {
            break label392;
          }
          bool = true;
          updateBluetoothHeadsetState(bool);
          localObject1 = this.stateListeners.iterator();
          if (((Iterator)localObject1).hasNext())
          {
            ((StateListener)((Iterator)localObject1).next()).onAudioSettingsChanged();
            continue;
          }
        }
        return;
      }
      catch (Exception localException)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("error initializing voip controller", localException);
        }
        callFailed();
      }
      VoIPController.setNativeBufferSize(AudioTrack.getMinBufferSize(48000, 4, 2) / 2);
      continue;
      label387:
      Object localObject2 = null;
      continue;
      label392:
      boolean bool = false;
    }
  }
  
  public void onDestroy()
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("=============== VoIPService STOPPING ===============");
    }
    stopForeground(true);
    stopRinging();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
    Object localObject = (SensorManager)getSystemService("sensor");
    if (((SensorManager)localObject).getDefaultSensor(8) != null) {
      ((SensorManager)localObject).unregisterListener(this);
    }
    if ((this.proximityWakelock != null) && (this.proximityWakelock.isHeld())) {
      this.proximityWakelock.release();
    }
    unregisterReceiver(this.receiver);
    if (this.timeoutRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.timeoutRunnable);
      this.timeoutRunnable = null;
    }
    super.onDestroy();
    sharedInstance = null;
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didEndedCall, new Object[0]);
      }
    });
    if ((this.controller != null) && (this.controllerStarted))
    {
      this.lastKnownDuration = this.controller.getCallDuration();
      updateStats();
      StatsController.getInstance(this.currentAccount).incrementTotalCallsTime(getStatsNetworkType(), (int)(this.lastKnownDuration / 1000L) % 5);
      onControllerPreRelease();
      this.controller.release();
      this.controller = null;
    }
    this.cpuWakelock.release();
    localObject = (AudioManager)getSystemService("audio");
    if (!USE_CONNECTION_SERVICE) {
      if ((this.isBtHeadsetConnected) && (!this.playingSound))
      {
        ((AudioManager)localObject).stopBluetoothSco();
        ((AudioManager)localObject).setSpeakerphoneOn(false);
      }
    }
    try
    {
      ((AudioManager)localObject).setMode(0);
      ((AudioManager)localObject).abandonAudioFocus(this);
      ((AudioManager)localObject).unregisterMediaButtonEventReceiver(new ComponentName(this, VoIPMediaButtonReceiver.class));
      if (this.haveAudioFocus) {
        ((AudioManager)localObject).abandonAudioFocus(this);
      }
      if (!this.playingSound) {
        this.soundPool.release();
      }
      if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null)) {
        this.systemCallConnection.destroy();
      }
      ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
      org.telegram.ui.Components.voip.VoIPHelper.lastCallTime = System.currentTimeMillis();
      return;
    }
    catch (SecurityException localSecurityException)
    {
      for (;;)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("Error setting audio more to normal", localSecurityException);
        }
      }
    }
  }
  
  @SuppressLint({"NewApi"})
  public void onSensorChanged(SensorEvent paramSensorEvent)
  {
    boolean bool = true;
    if (paramSensorEvent.sensor.getType() == 8)
    {
      AudioManager localAudioManager = (AudioManager)getSystemService("audio");
      if ((!this.isHeadsetPlugged) && (!localAudioManager.isSpeakerphoneOn()) && ((!isBluetoothHeadsetConnected()) || (!localAudioManager.isBluetoothScoOn()))) {
        break label54;
      }
    }
    for (;;)
    {
      return;
      label54:
      if (paramSensorEvent.values[0] < Math.min(paramSensorEvent.sensor.getMaximumRange(), 3.0F)) {}
      while (bool != this.isProximityNear)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("proximity " + bool);
        }
        this.isProximityNear = bool;
        try
        {
          if (!this.isProximityNear) {
            break label145;
          }
          this.proximityWakelock.acquire();
          return;
        }
        catch (Exception paramSensorEvent)
        {
          FileLog.e(paramSensorEvent);
          return;
        }
        bool = false;
      }
    }
    label145:
    this.proximityWakelock.release(1);
  }
  
  public void onSignalBarCountChanged(int paramInt)
  {
    this.signalBarCount = paramInt;
    int i = 0;
    while (i < this.stateListeners.size())
    {
      ((StateListener)this.stateListeners.get(i)).onSignalBarsCountChanged(paramInt);
      i += 1;
    }
  }
  
  public void registerStateListener(StateListener paramStateListener)
  {
    this.stateListeners.add(paramStateListener);
    if (this.currentState != 0) {
      paramStateListener.onStateChanged(this.currentState);
    }
    if (this.signalBarCount != 0) {
      paramStateListener.onSignalBarsCountChanged(this.signalBarCount);
    }
  }
  
  public void setMicMute(boolean paramBoolean)
  {
    this.micMute = paramBoolean;
    if (this.controller != null) {
      this.controller.setMicMute(paramBoolean);
    }
  }
  
  protected void showIncomingNotification(String paramString, CharSequence paramCharSequence, TLObject paramTLObject, List<TLRPC.User> paramList, int paramInt, Class<? extends Activity> paramClass)
  {
    Object localObject1 = new Intent(this, paramClass);
    ((Intent)localObject1).addFlags(805306368);
    Notification.Builder localBuilder = new Notification.Builder(this).setContentTitle(LocaleController.getString("VoipInCallBranding", 2131494597)).setContentText(paramString).setSmallIcon(2131165543).setSubText(paramCharSequence).setContentIntent(PendingIntent.getActivity(this, 0, (Intent)localObject1, 0));
    int j;
    Object localObject2;
    int i;
    if (Build.VERSION.SDK_INT >= 26)
    {
      paramClass = MessagesController.getGlobalNotificationsSettings();
      j = paramClass.getInt("calls_notification_channel", 0);
      paramList = (NotificationManager)getSystemService("notification");
      localObject2 = paramList.getNotificationChannel("incoming_calls" + j);
      int k = 1;
      paramInt = j;
      i = k;
      if (localObject2 != null)
      {
        if ((((NotificationChannel)localObject2).getImportance() < 4) || (((NotificationChannel)localObject2).getSound() != null) || (((NotificationChannel)localObject2).getVibrationPattern() != null))
        {
          FileLog.d("User messed up the notification channel; deleting it and creating a proper one");
          paramList.deleteNotificationChannel("incoming_calls" + j);
          paramInt = j + 1;
          paramClass.edit().putInt("calls_notification_channel", paramInt).commit();
          i = k;
        }
      }
      else
      {
        if (i != 0)
        {
          paramClass = new NotificationChannel("incoming_calls" + paramInt, LocaleController.getString("IncomingCalls", 2131493672), 4);
          paramClass.setSound(null, null);
          paramClass.enableVibration(false);
          paramClass.enableLights(false);
          paramList.createNotificationChannel(paramClass);
        }
        localBuilder.setChannelId("incoming_calls" + paramInt);
      }
    }
    else
    {
      localObject2 = new Intent(this, VoIPActionsReceiver.class);
      ((Intent)localObject2).setAction(getPackageName() + ".DECLINE_CALL");
      ((Intent)localObject2).putExtra("call_id", getCallID());
      paramClass = LocaleController.getString("VoipDeclineCall", 2131494590);
      paramList = paramClass;
      if (Build.VERSION.SDK_INT >= 24)
      {
        paramList = new SpannableString(paramClass);
        ((SpannableString)paramList).setSpan(new ForegroundColorSpan(-769226), 0, paramList.length(), 0);
      }
      localObject2 = PendingIntent.getBroadcast(this, 0, (Intent)localObject2, 268435456);
      localBuilder.addAction(2131165366, paramList, (PendingIntent)localObject2);
      Intent localIntent = new Intent(this, VoIPActionsReceiver.class);
      localIntent.setAction(getPackageName() + ".ANSWER_CALL");
      localIntent.putExtra("call_id", getCallID());
      paramClass = LocaleController.getString("VoipAnswerCall", 2131494582);
      paramList = paramClass;
      if (Build.VERSION.SDK_INT >= 24)
      {
        paramList = new SpannableString(paramClass);
        ((SpannableString)paramList).setSpan(new ForegroundColorSpan(-16733696), 0, paramList.length(), 0);
      }
      paramClass = PendingIntent.getBroadcast(this, 0, localIntent, 268435456);
      localBuilder.addAction(2131165372, paramList, paramClass);
      localBuilder.setPriority(2);
      if (Build.VERSION.SDK_INT >= 17) {
        localBuilder.setShowWhen(false);
      }
      if (Build.VERSION.SDK_INT >= 21)
      {
        localBuilder.setColor(-13851168);
        localBuilder.setVibrate(new long[0]);
        localBuilder.setCategory("call");
        localBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, (Intent)localObject1, 0), true);
      }
      paramList = localBuilder.getNotification();
      if (Build.VERSION.SDK_INT >= 21)
      {
        localObject1 = getPackageName();
        if (!LocaleController.isRTL) {
          break label914;
        }
        paramInt = 2131361793;
        label721:
        localObject1 = new RemoteViews((String)localObject1, paramInt);
        ((RemoteViews)localObject1).setTextViewText(2131230806, paramString);
        if (!TextUtils.isEmpty(paramCharSequence)) {
          break label942;
        }
        ((RemoteViews)localObject1).setViewVisibility(2131230834, 8);
        if (UserConfig.getActivatedAccountsCount() <= 1) {
          break label922;
        }
        paramString = UserConfig.getInstance(this.currentAccount).getCurrentUser();
        ((RemoteViews)localObject1).setTextViewText(2131230843, LocaleController.formatString("VoipInCallBrandingWithName", 2131494598, new Object[] { ContactsController.formatName(paramString.first_name, paramString.last_name) }));
      }
    }
    for (;;)
    {
      ((RemoteViews)localObject1).setTextViewText(2131230733, LocaleController.getString("VoipAnswerCall", 2131494582));
      ((RemoteViews)localObject1).setTextViewText(2131230759, LocaleController.getString("VoipDeclineCall", 2131494590));
      ((RemoteViews)localObject1).setImageViewBitmap(2131230812, getRoundAvatarBitmap(paramTLObject));
      ((RemoteViews)localObject1).setOnClickPendingIntent(2131230732, paramClass);
      ((RemoteViews)localObject1).setOnClickPendingIntent(2131230758, (PendingIntent)localObject2);
      paramList.bigContentView = ((RemoteViews)localObject1);
      paramList.headsUpContentView = ((RemoteViews)localObject1);
      startForeground(202, paramList);
      return;
      i = 0;
      paramInt = j;
      break;
      label914:
      paramInt = 2131361792;
      break label721;
      label922:
      ((RemoteViews)localObject1).setTextViewText(2131230843, LocaleController.getString("VoipInCallBranding", 2131494597));
    }
    label942:
    if (UserConfig.getActivatedAccountsCount() > 1)
    {
      paramString = UserConfig.getInstance(this.currentAccount).getCurrentUser();
      ((RemoteViews)localObject1).setTextViewText(2131230834, LocaleController.formatString("VoipAnsweringAsAccount", 2131494583, new Object[] { ContactsController.formatName(paramString.first_name, paramString.last_name) }));
    }
    for (;;)
    {
      ((RemoteViews)localObject1).setTextViewText(2131230843, paramCharSequence);
      break;
      ((RemoteViews)localObject1).setViewVisibility(2131230834, 8);
    }
  }
  
  protected abstract void showNotification();
  
  protected void showNotification(String paramString, TLRPC.FileLocation paramFileLocation, Class<? extends Activity> paramClass)
  {
    paramClass = new Intent(this, paramClass);
    paramClass.addFlags(805306368);
    paramString = new Notification.Builder(this).setContentTitle(LocaleController.getString("VoipOutgoingCall", 2131494609)).setContentText(paramString).setSmallIcon(2131165543).setContentIntent(PendingIntent.getActivity(this, 0, paramClass, 0));
    if (Build.VERSION.SDK_INT >= 16)
    {
      paramClass = new Intent(this, VoIPActionsReceiver.class);
      paramClass.setAction(getPackageName() + ".END_CALL");
      paramString.addAction(2131165366, LocaleController.getString("VoipEndCall", 2131494591), PendingIntent.getBroadcast(this, 0, paramClass, 134217728));
      paramString.setPriority(2);
    }
    if (Build.VERSION.SDK_INT >= 17) {
      paramString.setShowWhen(false);
    }
    if (Build.VERSION.SDK_INT >= 21) {
      paramString.setColor(-13851168);
    }
    if (Build.VERSION.SDK_INT >= 26) {
      paramString.setChannelId("Other3");
    }
    if (paramFileLocation != null)
    {
      paramClass = ImageLoader.getInstance().getImageFromMemory(paramFileLocation, null, "50_50");
      if (paramClass != null) {
        paramString.setLargeIcon(paramClass.getBitmap());
      }
    }
    else
    {
      this.ongoingCallNotification = paramString.getNotification();
      startForeground(201, this.ongoingCallNotification);
      return;
    }
    for (;;)
    {
      float f;
      try
      {
        f = 160.0F / AndroidUtilities.dp(50.0F);
        paramClass = new BitmapFactory.Options();
        if (f >= 1.0F) {
          break label304;
        }
        i = 1;
        paramClass.inSampleSize = i;
        paramFileLocation = BitmapFactory.decodeFile(FileLoader.getPathToAttach(paramFileLocation, true).toString(), paramClass);
        if (paramFileLocation == null) {
          break;
        }
        paramString.setLargeIcon(paramFileLocation);
      }
      catch (Throwable paramFileLocation)
      {
        FileLog.e(paramFileLocation);
      }
      break;
      label304:
      int i = (int)f;
    }
  }
  
  protected abstract void startRinging();
  
  protected void startRingtoneAndVibration(int paramInt)
  {
    localSharedPreferences = MessagesController.getNotificationsSettings(this.currentAccount);
    AudioManager localAudioManager = (AudioManager)getSystemService("audio");
    if (localAudioManager.getRingerMode() != 0)
    {
      i = 1;
      j = i;
      if (Build.VERSION.SDK_INT < 21) {}
    }
    try
    {
      int k = Settings.Global.getInt(getContentResolver(), "zen_mode");
      j = i;
      if (i != 0)
      {
        if (k != 0) {
          break label381;
        }
        j = 1;
      }
    }
    catch (Exception localException2)
    {
      for (;;)
      {
        label66:
        j = i;
      }
    }
    if (j != 0)
    {
      if (!USE_CONNECTION_SERVICE) {
        localAudioManager.requestAudioFocus(this, 2, 1);
      }
      this.ringtonePlayer = new MediaPlayer();
      this.ringtonePlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
      {
        public void onPrepared(MediaPlayer paramAnonymousMediaPlayer)
        {
          VoIPBaseService.this.ringtonePlayer.start();
        }
      });
      this.ringtonePlayer.setLooping(true);
      this.ringtonePlayer.setAudioStreamType(2);
    }
    for (;;)
    {
      try
      {
        if (!localSharedPreferences.getBoolean("custom_" + paramInt, false)) {
          continue;
        }
        str = localSharedPreferences.getString("ringtone_path_" + paramInt, RingtoneManager.getDefaultUri(1).toString());
        this.ringtonePlayer.setDataSource(this, Uri.parse(str));
        this.ringtonePlayer.prepareAsync();
      }
      catch (Exception localException1)
      {
        String str;
        label381:
        FileLog.e(localException1);
        if (this.ringtonePlayer == null) {
          continue;
        }
        this.ringtonePlayer.release();
        this.ringtonePlayer = null;
        continue;
        paramInt = localSharedPreferences.getInt("vibrate_calls", 0);
        continue;
        if (paramInt != 3) {
          continue;
        }
        long l = 700L * 2L;
        continue;
      }
      if (!localSharedPreferences.getBoolean("custom_" + paramInt, false)) {
        continue;
      }
      paramInt = localSharedPreferences.getInt("calls_vibrate_" + paramInt, 0);
      if (((paramInt != 2) && (paramInt != 4) && ((localAudioManager.getRingerMode() == 1) || (localAudioManager.getRingerMode() == 2))) || ((paramInt == 4) && (localAudioManager.getRingerMode() == 1)))
      {
        this.vibrator = ((Vibrator)getSystemService("vibrator"));
        l = 700L;
        if (paramInt != 1) {
          continue;
        }
        l = 700L / 2L;
        this.vibrator.vibrate(new long[] { 0L, l, 500L }, 0);
      }
      return;
      i = 0;
      break;
      j = 0;
      break label66;
      str = localSharedPreferences.getString("CallsRingtonePath", RingtoneManager.getDefaultUri(1).toString());
    }
  }
  
  public void stopRinging()
  {
    if (this.ringtonePlayer != null)
    {
      this.ringtonePlayer.stop();
      this.ringtonePlayer.release();
      this.ringtonePlayer = null;
    }
    if (this.vibrator != null)
    {
      this.vibrator.cancel();
      this.vibrator = null;
    }
  }
  
  public void toggleSpeakerphoneOrShowRouteSheet(Activity paramActivity)
  {
    int i;
    if ((isBluetoothHeadsetConnected()) && (hasEarpiece()))
    {
      paramActivity = new BottomSheet.Builder(paramActivity);
      String str1 = LocaleController.getString("VoipAudioRoutingBluetooth", 2131494584);
      String str2 = LocaleController.getString("VoipAudioRoutingEarpiece", 2131494585);
      String str3 = LocaleController.getString("VoipAudioRoutingSpeaker", 2131494586);
      DialogInterface.OnClickListener local3 = new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          paramAnonymousDialogInterface = (AudioManager)VoIPBaseService.this.getSystemService("audio");
          if (VoIPBaseService.getSharedInstance() == null) {
            return;
          }
          if ((VoIPBaseService.USE_CONNECTION_SERVICE) && (VoIPBaseService.this.systemCallConnection != null)) {
            switch (paramAnonymousInt)
            {
            }
          }
          for (;;)
          {
            paramAnonymousDialogInterface = VoIPBaseService.this.stateListeners.iterator();
            while (paramAnonymousDialogInterface.hasNext()) {
              ((VoIPBaseService.StateListener)paramAnonymousDialogInterface.next()).onAudioSettingsChanged();
            }
            VoIPBaseService.this.systemCallConnection.setAudioRoute(2);
            continue;
            VoIPBaseService.this.systemCallConnection.setAudioRoute(5);
            continue;
            VoIPBaseService.this.systemCallConnection.setAudioRoute(8);
            continue;
            if ((VoIPBaseService.this.audioConfigured) && (!VoIPBaseService.USE_CONNECTION_SERVICE))
            {
              switch (paramAnonymousInt)
              {
              }
              for (;;)
              {
                VoIPBaseService.this.updateOutputGainControlState();
                break;
                if (!VoIPBaseService.this.bluetoothScoActive)
                {
                  VoIPBaseService.this.needSwitchToBluetoothAfterScoActivates = true;
                  paramAnonymousDialogInterface.startBluetoothSco();
                }
                else
                {
                  paramAnonymousDialogInterface.setBluetoothScoOn(true);
                  paramAnonymousDialogInterface.setSpeakerphoneOn(false);
                  continue;
                  if (VoIPBaseService.this.bluetoothScoActive) {
                    paramAnonymousDialogInterface.stopBluetoothSco();
                  }
                  paramAnonymousDialogInterface.setSpeakerphoneOn(false);
                  paramAnonymousDialogInterface.setBluetoothScoOn(false);
                  continue;
                  if (VoIPBaseService.this.bluetoothScoActive) {
                    paramAnonymousDialogInterface.stopBluetoothSco();
                  }
                  paramAnonymousDialogInterface.setBluetoothScoOn(false);
                  paramAnonymousDialogInterface.setSpeakerphoneOn(true);
                }
              }
            }
            switch (paramAnonymousInt)
            {
            default: 
              break;
            case 0: 
              VoIPBaseService.this.audioRouteToSet = 2;
              break;
            case 1: 
              VoIPBaseService.this.audioRouteToSet = 0;
              break;
            case 2: 
              VoIPBaseService.this.audioRouteToSet = 1;
            }
          }
        }
      };
      paramActivity = paramActivity.setItems(new CharSequence[] { str1, str2, str3 }, new int[] { 2131165365, 2131165400, 2131165426 }, local3).create();
      paramActivity.setBackgroundColor(-13948117);
      paramActivity.show();
      paramActivity = paramActivity.getSheetContainer();
      i = 0;
    }
    while (i < paramActivity.getChildCount())
    {
      ((BottomSheet.BottomSheetCell)paramActivity.getChildAt(i)).setTextColor(-1);
      i += 1;
      continue;
      if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null) && (this.systemCallConnection.getCallAudioState() != null))
      {
        if (hasEarpiece())
        {
          paramActivity = this.systemCallConnection;
          if (this.systemCallConnection.getCallAudioState().getRoute() == 8) {}
          for (i = 5;; i = 8)
          {
            paramActivity.setAudioRoute(i);
            paramActivity = this.stateListeners.iterator();
            while (paramActivity.hasNext()) {
              ((StateListener)paramActivity.next()).onAudioSettingsChanged();
            }
          }
        }
        paramActivity = this.systemCallConnection;
        if (this.systemCallConnection.getCallAudioState().getRoute() == 2) {}
        for (i = 5;; i = 2)
        {
          paramActivity.setAudioRoute(i);
          break;
        }
      }
      if ((this.audioConfigured) && (!USE_CONNECTION_SERVICE))
      {
        paramActivity = (AudioManager)getSystemService("audio");
        if (hasEarpiece())
        {
          if (!paramActivity.isSpeakerphoneOn()) {}
          for (bool = true;; bool = false)
          {
            paramActivity.setSpeakerphoneOn(bool);
            updateOutputGainControlState();
            break;
          }
        }
        if (!paramActivity.isBluetoothScoOn()) {}
        for (bool = true;; bool = false)
        {
          paramActivity.setBluetoothScoOn(bool);
          break;
        }
      }
      if (!this.speakerphoneStateToSet) {}
      for (boolean bool = true;; bool = false)
      {
        this.speakerphoneStateToSet = bool;
        break;
      }
    }
  }
  
  public void unregisterStateListener(StateListener paramStateListener)
  {
    this.stateListeners.remove(paramStateListener);
  }
  
  protected void updateBluetoothHeadsetState(boolean paramBoolean)
  {
    if (paramBoolean == this.isBtHeadsetConnected) {
      return;
    }
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("updateBluetoothHeadsetState: " + paramBoolean);
    }
    this.isBtHeadsetConnected = paramBoolean;
    final Object localObject = (AudioManager)getSystemService("audio");
    if (paramBoolean) {
      if (this.bluetoothScoActive)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("SCO already active, setting audio routing");
        }
        ((AudioManager)localObject).setSpeakerphoneOn(false);
        ((AudioManager)localObject).setBluetoothScoOn(true);
      }
    }
    for (;;)
    {
      localObject = this.stateListeners.iterator();
      while (((Iterator)localObject).hasNext()) {
        ((StateListener)((Iterator)localObject).next()).onAudioSettingsChanged();
      }
      break;
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("startBluetoothSco");
      }
      this.needSwitchToBluetoothAfterScoActivates = true;
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          localObject.startBluetoothSco();
        }
      }, 500L);
      continue;
      this.bluetoothScoActive = false;
    }
  }
  
  protected void updateNetworkType()
  {
    NetworkInfo localNetworkInfo = ((ConnectivityManager)getSystemService("connectivity")).getActiveNetworkInfo();
    this.lastNetInfo = localNetworkInfo;
    int j = 0;
    int i = j;
    if (localNetworkInfo != null) {
      switch (localNetworkInfo.getType())
      {
      default: 
        i = j;
      }
    }
    for (;;)
    {
      if (this.controller != null) {
        this.controller.setNetworkType(i);
      }
      return;
      switch (localNetworkInfo.getSubtype())
      {
      case 4: 
      case 11: 
      case 14: 
      default: 
        i = 11;
        break;
      case 1: 
        i = 1;
        break;
      case 2: 
      case 7: 
        i = 2;
        break;
      case 3: 
      case 5: 
        i = 3;
        break;
      case 6: 
      case 8: 
      case 9: 
      case 10: 
      case 12: 
      case 15: 
        i = 4;
        break;
      case 13: 
        i = 5;
        continue;
        i = 6;
        continue;
        i = 7;
      }
    }
  }
  
  public void updateOutputGainControlState()
  {
    int i = 0;
    int j = 1;
    if ((this.controller == null) || (!this.controllerStarted)) {
      return;
    }
    Object localObject;
    boolean bool;
    if (!USE_CONNECTION_SERVICE)
    {
      localObject = (AudioManager)getSystemService("audio");
      VoIPController localVoIPController = this.controller;
      if ((hasEarpiece()) && (!((AudioManager)localObject).isSpeakerphoneOn()) && (!((AudioManager)localObject).isBluetoothScoOn()) && (!this.isHeadsetPlugged)) {}
      for (bool = true;; bool = false)
      {
        localVoIPController.setAudioOutputGainControlEnabled(bool);
        localVoIPController = this.controller;
        if (!this.isHeadsetPlugged)
        {
          i = j;
          if (hasEarpiece())
          {
            i = j;
            if (!((AudioManager)localObject).isSpeakerphoneOn())
            {
              i = j;
              if (!((AudioManager)localObject).isBluetoothScoOn())
              {
                i = j;
                if (this.isHeadsetPlugged) {}
              }
            }
          }
        }
        else
        {
          i = 0;
        }
        localVoIPController.setEchoCancellationStrength(i);
        return;
      }
    }
    if (this.systemCallConnection.getCallAudioState().getRoute() == 1)
    {
      bool = true;
      this.controller.setAudioOutputGainControlEnabled(bool);
      localObject = this.controller;
      if (!bool) {
        break label192;
      }
    }
    for (;;)
    {
      ((VoIPController)localObject).setEchoCancellationStrength(i);
      return;
      bool = false;
      break;
      label192:
      i = 1;
    }
  }
  
  protected abstract void updateServerConfig();
  
  protected void updateStats()
  {
    this.controller.getStats(this.stats);
    long l1 = this.stats.bytesSentWifi - this.prevStats.bytesSentWifi;
    long l2 = this.stats.bytesRecvdWifi - this.prevStats.bytesRecvdWifi;
    long l3 = this.stats.bytesSentMobile - this.prevStats.bytesSentMobile;
    long l4 = this.stats.bytesRecvdMobile - this.prevStats.bytesRecvdMobile;
    Object localObject = this.stats;
    this.stats = this.prevStats;
    this.prevStats = ((VoIPController.Stats)localObject);
    if (l1 > 0L) {
      StatsController.getInstance(this.currentAccount).incrementSentBytesCount(1, 0, l1);
    }
    if (l2 > 0L) {
      StatsController.getInstance(this.currentAccount).incrementReceivedBytesCount(1, 0, l2);
    }
    if (l3 > 0L)
    {
      localObject = StatsController.getInstance(this.currentAccount);
      if ((this.lastNetInfo != null) && (this.lastNetInfo.isRoaming()))
      {
        i = 2;
        ((StatsController)localObject).incrementSentBytesCount(i, 0, l3);
      }
    }
    else if (l4 > 0L)
    {
      localObject = StatsController.getInstance(this.currentAccount);
      if ((this.lastNetInfo == null) || (!this.lastNetInfo.isRoaming())) {
        break label232;
      }
    }
    label232:
    for (int i = 2;; i = 0)
    {
      ((StatsController)localObject).incrementReceivedBytesCount(i, 0, l4);
      return;
      i = 0;
      break;
    }
  }
  
  @TargetApi(26)
  public class CallConnection
    extends Connection
  {
    public CallConnection()
    {
      setConnectionProperties(128);
      setAudioModeIsVoip(true);
    }
    
    public void onAnswer()
    {
      VoIPBaseService.this.acceptIncomingCallFromNotification();
    }
    
    public void onCallAudioStateChanged(CallAudioState paramCallAudioState)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("ConnectionService call audio state changed: " + paramCallAudioState);
      }
      paramCallAudioState = VoIPBaseService.this.stateListeners.iterator();
      while (paramCallAudioState.hasNext()) {
        ((VoIPBaseService.StateListener)paramCallAudioState.next()).onAudioSettingsChanged();
      }
    }
    
    public void onCallEvent(String paramString, Bundle paramBundle)
    {
      super.onCallEvent(paramString, paramBundle);
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("ConnectionService onCallEvent " + paramString);
      }
    }
    
    public void onDisconnect()
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("ConnectionService onDisconnect");
      }
      setDisconnected(new DisconnectCause(2));
      destroy();
      VoIPBaseService.this.systemCallConnection = null;
      VoIPBaseService.this.hangUp();
    }
    
    public void onReject()
    {
      VoIPBaseService.this.declineIncomingCall(1, null);
    }
    
    public void onShowIncomingCallUi()
    {
      VoIPBaseService.this.startRinging();
    }
    
    public void onStateChanged(int paramInt)
    {
      super.onStateChanged(paramInt);
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("ConnectionService onStateChanged " + paramInt);
      }
    }
  }
  
  public static abstract interface StateListener
  {
    public abstract void onAudioSettingsChanged();
    
    public abstract void onSignalBarsCountChanged(int paramInt);
    
    public abstract void onStateChanged(int paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/VoIPBaseService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */