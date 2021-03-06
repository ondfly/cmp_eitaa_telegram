package org.telegram.messenger.voip;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.telecom.TelecomManager;
import android.view.KeyEvent;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.PhoneCall;
import org.telegram.tgnet.TLRPC.TL_dataJSON;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPhoneCall;
import org.telegram.tgnet.TLRPC.TL_messages_dhConfig;
import org.telegram.tgnet.TLRPC.TL_messages_getDhConfig;
import org.telegram.tgnet.TLRPC.TL_phoneCall;
import org.telegram.tgnet.TLRPC.TL_phoneCallAccepted;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonDisconnect;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonHangup;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscarded;
import org.telegram.tgnet.TLRPC.TL_phoneCallProtocol;
import org.telegram.tgnet.TLRPC.TL_phoneConnection;
import org.telegram.tgnet.TLRPC.TL_phone_acceptCall;
import org.telegram.tgnet.TLRPC.TL_phone_confirmCall;
import org.telegram.tgnet.TLRPC.TL_phone_discardCall;
import org.telegram.tgnet.TLRPC.TL_phone_getCallConfig;
import org.telegram.tgnet.TLRPC.TL_phone_phoneCall;
import org.telegram.tgnet.TLRPC.TL_phone_receivedCall;
import org.telegram.tgnet.TLRPC.TL_phone_requestCall;
import org.telegram.tgnet.TLRPC.TL_phone_saveCallDebug;
import org.telegram.tgnet.TLRPC.TL_updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.messages_DhConfig;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.VoIPActivity;
import org.telegram.ui.VoIPFeedbackActivity;

public class VoIPService
  extends VoIPBaseService
{
  public static final int CALL_MAX_LAYER = 74;
  public static final int CALL_MIN_LAYER = 65;
  public static final int STATE_BUSY = 17;
  public static final int STATE_EXCHANGING_KEYS = 12;
  public static final int STATE_HANGING_UP = 10;
  public static final int STATE_REQUESTING = 14;
  public static final int STATE_RINGING = 16;
  public static final int STATE_WAITING = 13;
  public static final int STATE_WAITING_INCOMING = 15;
  public static TLRPC.PhoneCall callIShouldHavePutIntoIntent;
  private byte[] a_or_b;
  private byte[] authKey;
  private TLRPC.PhoneCall call;
  private int callReqId;
  private Runnable delayedStartOutgoingCall;
  private boolean endCallAfterRequest = false;
  private boolean forceRating;
  private byte[] g_a;
  private byte[] g_a_hash;
  private byte[] groupCallEncryptionKey;
  private long groupCallKeyFingerprint;
  private List<Integer> groupUsersToAdd = new ArrayList();
  private boolean joiningGroupCall;
  private long keyFingerprint;
  private boolean needSendDebugLog = false;
  private int peerCapabilities;
  private ArrayList<TLRPC.PhoneCall> pendingUpdates = new ArrayList();
  private boolean upgrading;
  private TLRPC.User user;
  
  private void acknowledgeCall(final boolean paramBoolean)
  {
    if ((this.call instanceof TLRPC.TL_phoneCallDiscarded))
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.w("Call " + this.call.id + " was discarded before the service started, stopping");
      }
      stopSelf();
      return;
    }
    TLRPC.TL_phone_receivedCall localTL_phone_receivedCall = new TLRPC.TL_phone_receivedCall();
    localTL_phone_receivedCall.peer = new TLRPC.TL_inputPhoneCall();
    localTL_phone_receivedCall.peer.id = this.call.id;
    localTL_phone_receivedCall.peer.access_hash = this.call.access_hash;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_phone_receivedCall, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (VoIPBaseService.sharedInstance == null) {}
            do
            {
              return;
              if (BuildVars.LOGS_ENABLED) {
                FileLog.w("receivedCall response = " + paramAnonymousTLObject);
              }
              if (paramAnonymousTL_error != null)
              {
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.e("error on receivedCall: " + paramAnonymousTL_error);
                }
                VoIPService.this.stopSelf();
                return;
              }
              if (VoIPBaseService.USE_CONNECTION_SERVICE)
              {
                TelecomManager localTelecomManager = (TelecomManager)VoIPService.this.getSystemService("telecom");
                Bundle localBundle = new Bundle();
                localBundle.putInt("call_type", 1);
                localTelecomManager.addNewIncomingCall(VoIPService.this.addAccountToTelecomManager(), localBundle);
              }
            } while (!VoIPService.6.this.val$startRinging);
            VoIPService.this.startRinging();
          }
        });
      }
    }, 2);
  }
  
  private void dumpCallObject()
  {
    try
    {
      if (BuildVars.LOGS_ENABLED)
      {
        Field[] arrayOfField = TLRPC.PhoneCall.class.getFields();
        int j = arrayOfField.length;
        int i = 0;
        while (i < j)
        {
          Field localField = arrayOfField[i];
          FileLog.d(localField.getName() + " = " + localField.get(this.call));
          i += 1;
        }
      }
      return;
    }
    catch (Exception localException)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.e(localException);
      }
    }
  }
  
  private String[] getEmoji()
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    try
    {
      localByteArrayOutputStream.write(this.authKey);
      localByteArrayOutputStream.write(this.g_a);
      return EncryptionKeyEmojifier.emojifyForCall(Utilities.computeSHA256(localByteArrayOutputStream.toByteArray(), 0, localByteArrayOutputStream.size()));
    }
    catch (IOException localIOException)
    {
      for (;;) {}
    }
  }
  
  public static VoIPService getSharedInstance()
  {
    if ((sharedInstance instanceof VoIPService)) {
      return (VoIPService)sharedInstance;
    }
    return null;
  }
  
  private void initiateActualEncryptedCall()
  {
    if (this.timeoutRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.timeoutRunnable);
      this.timeoutRunnable = null;
    }
    HashSet localHashSet;
    for (;;)
    {
      try
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("InitCall: keyID=" + this.keyFingerprint);
        }
        localObject4 = MessagesController.getNotificationsSettings(this.currentAccount);
        localHashSet = new HashSet(((SharedPreferences)localObject4).getStringSet("calls_access_hashes", Collections.EMPTY_SET));
        localHashSet.add(this.call.id + " " + this.call.access_hash + " " + System.currentTimeMillis());
        if (localHashSet.size() <= 20) {
          break;
        }
        Object localObject1 = null;
        long l1 = Long.MAX_VALUE;
        Iterator localIterator = localHashSet.iterator();
        if (!localIterator.hasNext()) {
          break label268;
        }
        String str = (String)localIterator.next();
        String[] arrayOfString = str.split(" ");
        if (arrayOfString.length < 2)
        {
          localIterator.remove();
          continue;
        }
        try
        {
          long l2 = Long.parseLong(arrayOfString[2]);
          if (l2 >= l1) {
            continue;
          }
          l1 = l2;
          localObject2 = str;
        }
        catch (Exception localException2)
        {
          localIterator.remove();
        }
      }
      catch (Exception localException1)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("error starting call", localException1);
        }
        callFailed();
        return;
      }
      continue;
      label268:
      if (localObject2 != null) {
        localHashSet.remove(localObject2);
      }
    }
    ((SharedPreferences)localObject4).edit().putStringSet("calls_access_hashes", localHashSet).commit();
    Object localObject2 = MessagesController.getGlobalMainSettings();
    this.controller.setConfig(MessagesController.getInstance(this.currentAccount).callPacketTimeout / 1000.0D, MessagesController.getInstance(this.currentAccount).callConnectTimeout / 1000.0D, ((SharedPreferences)localObject2).getInt("VoipDataSaving", 0), this.call.id);
    this.controller.setEncryptionKey(this.authKey, this.isOutgoing);
    Object localObject3 = new TLRPC.TL_phoneConnection[this.call.alternative_connections.size() + 1];
    localObject3[0] = this.call.connection;
    int i = 0;
    while (i < this.call.alternative_connections.size())
    {
      localObject3[(i + 1)] = ((TLRPC.TL_phoneConnection)this.call.alternative_connections.get(i));
      i += 1;
    }
    localObject2 = MessagesController.getGlobalMainSettings();
    VoIPHelper.upgradeP2pSetting(this.currentAccount);
    int j = 1;
    Object localObject4 = MessagesController.getMainSettings(this.currentAccount);
    if (MessagesController.getInstance(this.currentAccount).defaultP2pContacts)
    {
      i = 1;
      switch (((SharedPreferences)localObject4).getInt("calls_p2p_new", i))
      {
      }
    }
    for (;;)
    {
      localObject4 = this.controller;
      boolean bool1;
      if ((this.call.protocol.udp_p2p) && (i != 0))
      {
        bool1 = true;
        label554:
        if ((!BuildVars.DEBUG_VERSION) || (!((SharedPreferences)localObject2).getBoolean("dbg_force_tcp_in_calls", false))) {
          break label801;
        }
      }
      label801:
      for (boolean bool2 = true;; bool2 = false)
      {
        ((VoIPController)localObject4).setRemoteEndpoints((TLRPC.TL_phoneConnection[])localObject3, bool1, bool2, this.call.protocol.max_layer);
        if ((BuildVars.DEBUG_VERSION) && (((SharedPreferences)localObject2).getBoolean("dbg_force_tcp_in_calls", false))) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              Toast.makeText(VoIPService.this, "This call uses TCP which will degrade its quality.", 0).show();
            }
          });
        }
        if ((((SharedPreferences)localObject2).getBoolean("proxy_enabled", false)) && (((SharedPreferences)localObject2).getBoolean("proxy_enabled_calls", false)))
        {
          localObject3 = ((SharedPreferences)localObject2).getString("proxy_ip", null);
          if (localObject3 != null) {
            this.controller.setProxy((String)localObject3, ((SharedPreferences)localObject2).getInt("proxy_port", 0), ((SharedPreferences)localObject2).getString("proxy_user", null), ((SharedPreferences)localObject2).getString("proxy_pass", null));
          }
        }
        this.controller.start();
        updateNetworkType();
        this.controller.connect();
        this.controllerStarted = true;
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (VoIPService.this.controller == null) {
              return;
            }
            VoIPService.this.updateStats();
            AndroidUtilities.runOnUIThread(this, 5000L);
          }
        }, 5000L);
        return;
        localObject4 = ContactsController.getInstance(this.currentAccount).contactsDict.get(Integer.valueOf(this.user.id));
        if (localObject4 != null) {}
        for (i = 1;; i = 0) {
          break;
        }
        bool1 = false;
        break label554;
      }
      i = j;
      continue;
      i = 0;
      break;
      i = 1;
      continue;
      i = 0;
    }
  }
  
  private void processAcceptedCall()
  {
    dispatchStateChanged(12);
    Object localObject1 = new BigInteger(1, MessagesStorage.getInstance(this.currentAccount).getSecretPBytes());
    Object localObject2 = new BigInteger(1, this.call.g_b);
    if (!Utilities.isGoodGaAndGb((BigInteger)localObject2, (BigInteger)localObject1))
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.w("stopping VoIP service, bad Ga and Gb");
      }
      callFailed();
      return;
    }
    localObject2 = ((BigInteger)localObject2).modPow(new BigInteger(1, this.a_or_b), (BigInteger)localObject1).toByteArray();
    if (localObject2.length > 256)
    {
      localObject1 = new byte['Ā'];
      System.arraycopy(localObject2, localObject2.length - 256, localObject1, 0, 256);
    }
    for (;;)
    {
      localObject2 = Utilities.computeSHA1((byte[])localObject1);
      byte[] arrayOfByte = new byte[8];
      System.arraycopy(localObject2, localObject2.length - 8, arrayOfByte, 0, 8);
      long l = Utilities.bytesToLong(arrayOfByte);
      this.authKey = ((byte[])localObject1);
      this.keyFingerprint = l;
      localObject1 = new TLRPC.TL_phone_confirmCall();
      ((TLRPC.TL_phone_confirmCall)localObject1).g_a = this.g_a;
      ((TLRPC.TL_phone_confirmCall)localObject1).key_fingerprint = l;
      ((TLRPC.TL_phone_confirmCall)localObject1).peer = new TLRPC.TL_inputPhoneCall();
      ((TLRPC.TL_phone_confirmCall)localObject1).peer.id = this.call.id;
      ((TLRPC.TL_phone_confirmCall)localObject1).peer.access_hash = this.call.access_hash;
      ((TLRPC.TL_phone_confirmCall)localObject1).protocol = new TLRPC.TL_phoneCallProtocol();
      ((TLRPC.TL_phone_confirmCall)localObject1).protocol.max_layer = 74;
      ((TLRPC.TL_phone_confirmCall)localObject1).protocol.min_layer = 65;
      localObject2 = ((TLRPC.TL_phone_confirmCall)localObject1).protocol;
      ((TLRPC.TL_phone_confirmCall)localObject1).protocol.udp_reflector = true;
      ((TLRPC.TL_phoneCallProtocol)localObject2).udp_p2p = true;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error != null)
              {
                VoIPService.this.callFailed();
                return;
              }
              VoIPService.access$502(VoIPService.this, ((TLRPC.TL_phone_phoneCall)paramAnonymousTLObject).phone_call);
              VoIPService.this.initiateActualEncryptedCall();
            }
          });
        }
      });
      return;
      localObject1 = localObject2;
      if (localObject2.length < 256)
      {
        localObject1 = new byte['Ā'];
        System.arraycopy(localObject2, 0, localObject1, 256 - localObject2.length, localObject2.length);
        int i = 0;
        while (i < 256 - localObject2.length)
        {
          localObject2[i] = 0;
          i += 1;
        }
      }
    }
  }
  
  private void startConnectingSound()
  {
    if (this.spPlayID != 0) {
      this.soundPool.stop(this.spPlayID);
    }
    this.spPlayID = this.soundPool.play(this.spConnectingId, 1.0F, 1.0F, 0, -1, 1.0F);
    if (this.spPlayID == 0) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (VoIPBaseService.sharedInstance == null) {}
          do
          {
            return;
            if (VoIPService.this.spPlayID == 0) {
              VoIPService.this.spPlayID = VoIPService.this.soundPool.play(VoIPService.this.spConnectingId, 1.0F, 1.0F, 0, -1, 1.0F);
            }
          } while (VoIPService.this.spPlayID != 0);
          AndroidUtilities.runOnUIThread(this, 100L);
        }
      }, 100L);
    }
  }
  
  private void startOutgoingCall()
  {
    if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null)) {
      this.systemCallConnection.setDialing();
    }
    configureDeviceForCall();
    showNotification();
    startConnectingSound();
    dispatchStateChanged(14);
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didStartedCall, new Object[0]);
      }
    });
    Object localObject = new byte['Ā'];
    Utilities.random.nextBytes((byte[])localObject);
    localObject = new TLRPC.TL_messages_getDhConfig();
    ((TLRPC.TL_messages_getDhConfig)localObject).random_length = 256;
    final MessagesStorage localMessagesStorage = MessagesStorage.getInstance(this.currentAccount);
    ((TLRPC.TL_messages_getDhConfig)localObject).version = localMessagesStorage.getLastSecretVersion();
    this.callReqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        VoIPService.access$202(VoIPService.this, 0);
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTL_error = (TLRPC.messages_DhConfig)paramAnonymousTLObject;
          if ((paramAnonymousTLObject instanceof TLRPC.TL_messages_dhConfig))
          {
            if (!Utilities.isGoodPrime(paramAnonymousTL_error.p, paramAnonymousTL_error.g))
            {
              VoIPService.this.callFailed();
              return;
            }
            localMessagesStorage.setSecretPBytes(paramAnonymousTL_error.p);
            localMessagesStorage.setSecretG(paramAnonymousTL_error.g);
            localMessagesStorage.setLastSecretVersion(paramAnonymousTL_error.version);
            localMessagesStorage.saveSecretParams(localMessagesStorage.getLastSecretVersion(), localMessagesStorage.getSecretG(), localMessagesStorage.getSecretPBytes());
          }
          final byte[] arrayOfByte = new byte['Ā'];
          int i = 0;
          while (i < 256)
          {
            arrayOfByte[i] = ((byte)((byte)(int)(Utilities.random.nextDouble() * 256.0D) ^ paramAnonymousTL_error.random[i]));
            i += 1;
          }
          paramAnonymousTL_error = BigInteger.valueOf(localMessagesStorage.getSecretG()).modPow(new BigInteger(1, arrayOfByte), new BigInteger(1, localMessagesStorage.getSecretPBytes())).toByteArray();
          paramAnonymousTLObject = paramAnonymousTL_error;
          if (paramAnonymousTL_error.length > 256)
          {
            paramAnonymousTLObject = new byte['Ā'];
            System.arraycopy(paramAnonymousTL_error, 1, paramAnonymousTLObject, 0, 256);
          }
          paramAnonymousTL_error = new TLRPC.TL_phone_requestCall();
          paramAnonymousTL_error.user_id = MessagesController.getInstance(VoIPService.this.currentAccount).getInputUser(VoIPService.this.user);
          paramAnonymousTL_error.protocol = new TLRPC.TL_phoneCallProtocol();
          paramAnonymousTL_error.protocol.udp_p2p = true;
          paramAnonymousTL_error.protocol.udp_reflector = true;
          paramAnonymousTL_error.protocol.min_layer = 65;
          paramAnonymousTL_error.protocol.max_layer = 74;
          VoIPService.access$402(VoIPService.this, paramAnonymousTLObject);
          paramAnonymousTL_error.g_a_hash = Utilities.computeSHA256(paramAnonymousTLObject, 0, paramAnonymousTLObject.length);
          paramAnonymousTL_error.random_id = Utilities.random.nextInt();
          ConnectionsManager.getInstance(VoIPService.this.currentAccount).sendRequest(paramAnonymousTL_error, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (paramAnonymous2TL_error == null)
                  {
                    VoIPService.access$502(VoIPService.this, ((TLRPC.TL_phone_phoneCall)paramAnonymous2TLObject).phone_call);
                    VoIPService.access$602(VoIPService.this, VoIPService.5.1.this.val$salt);
                    VoIPService.this.dispatchStateChanged(13);
                    if (VoIPService.this.endCallAfterRequest)
                    {
                      VoIPService.this.hangUp();
                      return;
                    }
                    if ((VoIPService.this.pendingUpdates.size() > 0) && (VoIPService.this.call != null))
                    {
                      Iterator localIterator = VoIPService.this.pendingUpdates.iterator();
                      while (localIterator.hasNext())
                      {
                        TLRPC.PhoneCall localPhoneCall = (TLRPC.PhoneCall)localIterator.next();
                        VoIPService.this.onCallUpdated(localPhoneCall);
                      }
                      VoIPService.this.pendingUpdates.clear();
                    }
                    VoIPService.this.timeoutRunnable = new Runnable()
                    {
                      public void run()
                      {
                        VoIPService.this.timeoutRunnable = null;
                        TLRPC.TL_phone_discardCall localTL_phone_discardCall = new TLRPC.TL_phone_discardCall();
                        localTL_phone_discardCall.peer = new TLRPC.TL_inputPhoneCall();
                        localTL_phone_discardCall.peer.access_hash = VoIPService.this.call.access_hash;
                        localTL_phone_discardCall.peer.id = VoIPService.this.call.id;
                        localTL_phone_discardCall.reason = new TLRPC.TL_phoneCallDiscardReasonMissed();
                        ConnectionsManager.getInstance(VoIPService.this.currentAccount).sendRequest(localTL_phone_discardCall, new RequestDelegate()
                        {
                          public void run(TLObject paramAnonymous5TLObject, TLRPC.TL_error paramAnonymous5TL_error)
                          {
                            if (BuildVars.LOGS_ENABLED)
                            {
                              if (paramAnonymous5TL_error == null) {
                                break label44;
                              }
                              FileLog.e("error on phone.discardCall: " + paramAnonymous5TL_error);
                            }
                            for (;;)
                            {
                              AndroidUtilities.runOnUIThread(new Runnable()
                              {
                                public void run()
                                {
                                  VoIPService.this.callFailed();
                                }
                              });
                              return;
                              label44:
                              FileLog.d("phone.discardCall " + paramAnonymous5TLObject);
                            }
                          }
                        }, 2);
                      }
                    };
                    AndroidUtilities.runOnUIThread(VoIPService.this.timeoutRunnable, MessagesController.getInstance(VoIPService.this.currentAccount).callReceiveTimeout);
                    return;
                  }
                  if ((paramAnonymous2TL_error.code == 400) && ("PARTICIPANT_VERSION_OUTDATED".equals(paramAnonymous2TL_error.text)))
                  {
                    VoIPService.this.callFailed(-1);
                    return;
                  }
                  if ((paramAnonymous2TL_error.code == 403) && ("USER_PRIVACY_RESTRICTED".equals(paramAnonymous2TL_error.text)))
                  {
                    VoIPService.this.callFailed(-2);
                    return;
                  }
                  if (paramAnonymous2TL_error.code == 406)
                  {
                    VoIPService.this.callFailed(-3);
                    return;
                  }
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("Error on phone.requestCall: " + paramAnonymous2TL_error);
                  }
                  VoIPService.this.callFailed();
                }
              });
            }
          }, 2);
          return;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("Error on getDhConfig " + paramAnonymousTL_error);
        }
        VoIPService.this.callFailed();
      }
    }, 2);
  }
  
  private void startRatingActivity()
  {
    try
    {
      PendingIntent.getActivity(this, 0, new Intent(this, VoIPFeedbackActivity.class).putExtra("call_id", this.call.id).putExtra("call_access_hash", this.call.access_hash).putExtra("account", this.currentAccount).addFlags(805306368), 0).send();
      return;
    }
    catch (Exception localException)
    {
      while (!BuildVars.LOGS_ENABLED) {}
      FileLog.e("Error starting incall activity", localException);
    }
  }
  
  public void acceptIncomingCall()
  {
    stopRinging();
    showNotification();
    configureDeviceForCall();
    startConnectingSound();
    dispatchStateChanged(12);
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didStartedCall, new Object[0]);
      }
    });
    final MessagesStorage localMessagesStorage = MessagesStorage.getInstance(this.currentAccount);
    TLRPC.TL_messages_getDhConfig localTL_messages_getDhConfig = new TLRPC.TL_messages_getDhConfig();
    localTL_messages_getDhConfig.random_length = 256;
    localTL_messages_getDhConfig.version = localMessagesStorage.getLastSecretVersion();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getDhConfig, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTL_error = (TLRPC.messages_DhConfig)paramAnonymousTLObject;
          if ((paramAnonymousTLObject instanceof TLRPC.TL_messages_dhConfig))
          {
            if (!Utilities.isGoodPrime(paramAnonymousTL_error.p, paramAnonymousTL_error.g))
            {
              if (BuildVars.LOGS_ENABLED) {
                FileLog.e("stopping VoIP service, bad prime");
              }
              VoIPService.this.callFailed();
              return;
            }
            localMessagesStorage.setSecretPBytes(paramAnonymousTL_error.p);
            localMessagesStorage.setSecretG(paramAnonymousTL_error.g);
            localMessagesStorage.setLastSecretVersion(paramAnonymousTL_error.version);
            MessagesStorage.getInstance(VoIPService.this.currentAccount).saveSecretParams(localMessagesStorage.getLastSecretVersion(), localMessagesStorage.getSecretG(), localMessagesStorage.getSecretPBytes());
          }
          paramAnonymousTLObject = new byte['Ā'];
          int i = 0;
          while (i < 256)
          {
            paramAnonymousTLObject[i] = ((byte)((byte)(int)(Utilities.random.nextDouble() * 256.0D) ^ paramAnonymousTL_error.random[i]));
            i += 1;
          }
          if (VoIPService.this.call == null)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.e("call is null");
            }
            VoIPService.this.callFailed();
            return;
          }
          VoIPService.access$602(VoIPService.this, paramAnonymousTLObject);
          paramAnonymousTL_error = BigInteger.valueOf(localMessagesStorage.getSecretG());
          BigInteger localBigInteger = new BigInteger(1, localMessagesStorage.getSecretPBytes());
          paramAnonymousTLObject = paramAnonymousTL_error.modPow(new BigInteger(1, paramAnonymousTLObject), localBigInteger);
          VoIPService.access$902(VoIPService.this, VoIPService.this.call.g_a_hash);
          paramAnonymousTL_error = paramAnonymousTLObject.toByteArray();
          paramAnonymousTLObject = paramAnonymousTL_error;
          if (paramAnonymousTL_error.length > 256)
          {
            paramAnonymousTLObject = new byte['Ā'];
            System.arraycopy(paramAnonymousTL_error, 1, paramAnonymousTLObject, 0, 256);
          }
          paramAnonymousTL_error = new TLRPC.TL_phone_acceptCall();
          paramAnonymousTL_error.g_b = paramAnonymousTLObject;
          paramAnonymousTL_error.peer = new TLRPC.TL_inputPhoneCall();
          paramAnonymousTL_error.peer.id = VoIPService.this.call.id;
          paramAnonymousTL_error.peer.access_hash = VoIPService.this.call.access_hash;
          paramAnonymousTL_error.protocol = new TLRPC.TL_phoneCallProtocol();
          paramAnonymousTLObject = paramAnonymousTL_error.protocol;
          paramAnonymousTL_error.protocol.udp_reflector = true;
          paramAnonymousTLObject.udp_p2p = true;
          paramAnonymousTL_error.protocol.min_layer = 65;
          paramAnonymousTL_error.protocol.max_layer = 74;
          ConnectionsManager.getInstance(VoIPService.this.currentAccount).sendRequest(paramAnonymousTL_error, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (paramAnonymous2TL_error == null)
                  {
                    if (BuildVars.LOGS_ENABLED) {
                      FileLog.w("accept call ok! " + paramAnonymous2TLObject);
                    }
                    VoIPService.access$502(VoIPService.this, ((TLRPC.TL_phone_phoneCall)paramAnonymous2TLObject).phone_call);
                    if ((VoIPService.this.call instanceof TLRPC.TL_phoneCallDiscarded)) {
                      VoIPService.this.onCallUpdated(VoIPService.this.call);
                    }
                    return;
                  }
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.e("Error on phone.acceptCall: " + paramAnonymous2TL_error);
                  }
                  VoIPService.this.callFailed();
                }
              });
            }
          }, 2);
          return;
        }
        VoIPService.this.callFailed();
      }
    });
  }
  
  protected void callFailed(int paramInt)
  {
    TLRPC.TL_phone_discardCall localTL_phone_discardCall;
    int i;
    if (this.call != null)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("Discarding failed call");
      }
      localTL_phone_discardCall = new TLRPC.TL_phone_discardCall();
      localTL_phone_discardCall.peer = new TLRPC.TL_inputPhoneCall();
      localTL_phone_discardCall.peer.access_hash = this.call.access_hash;
      localTL_phone_discardCall.peer.id = this.call.id;
      if ((this.controller == null) || (!this.controllerStarted)) {
        break label170;
      }
      i = (int)(this.controller.getCallDuration() / 1000L);
      localTL_phone_discardCall.duration = i;
      if ((this.controller == null) || (!this.controllerStarted)) {
        break label175;
      }
    }
    label170:
    label175:
    for (long l = this.controller.getPreferredRelayID();; l = 0L)
    {
      localTL_phone_discardCall.connection_id = l;
      localTL_phone_discardCall.reason = new TLRPC.TL_phoneCallDiscardReasonDisconnect();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_phone_discardCall, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error != null) {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.e("error on phone.discardCall: " + paramAnonymousTL_error);
            }
          }
          while (!BuildVars.LOGS_ENABLED) {
            return;
          }
          FileLog.d("phone.discardCall " + paramAnonymousTLObject);
        }
      });
      super.callFailed(paramInt);
      return;
      i = 0;
      break;
    }
  }
  
  public boolean canUpgrate()
  {
    return (this.peerCapabilities & 0x1) == 1;
  }
  
  public void debugCtl(int paramInt1, int paramInt2)
  {
    if (this.controller != null) {
      this.controller.debugCtl(paramInt1, paramInt2);
    }
  }
  
  public void declineIncomingCall()
  {
    declineIncomingCall(1, null);
  }
  
  public void declineIncomingCall(int paramInt, final Runnable paramRunnable)
  {
    final boolean bool = true;
    stopRinging();
    this.callDiscardReason = paramInt;
    if (this.currentState == 14) {
      if (this.delayedStartOutgoingCall != null)
      {
        AndroidUtilities.cancelRunOnUIThread(this.delayedStartOutgoingCall);
        callEnded();
      }
    }
    do
    {
      do
      {
        return;
        dispatchStateChanged(10);
        this.endCallAfterRequest = true;
        return;
      } while ((this.currentState == 10) || (this.currentState == 11));
      dispatchStateChanged(10);
      if (this.call != null) {
        break;
      }
      if (paramRunnable != null) {
        paramRunnable.run();
      }
      callEnded();
    } while (this.callReqId == 0);
    ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.callReqId, false);
    this.callReqId = 0;
    return;
    TLRPC.TL_phone_discardCall localTL_phone_discardCall = new TLRPC.TL_phone_discardCall();
    localTL_phone_discardCall.peer = new TLRPC.TL_inputPhoneCall();
    localTL_phone_discardCall.peer.access_hash = this.call.access_hash;
    localTL_phone_discardCall.peer.id = this.call.id;
    int i;
    long l;
    label232:
    label280:
    label294:
    final Object localObject;
    if ((this.controller != null) && (this.controllerStarted))
    {
      i = (int)(this.controller.getCallDuration() / 1000L);
      localTL_phone_discardCall.duration = i;
      if ((this.controller == null) || (!this.controllerStarted)) {
        break label349;
      }
      l = this.controller.getPreferredRelayID();
      localTL_phone_discardCall.connection_id = l;
      switch (paramInt)
      {
      default: 
        localTL_phone_discardCall.reason = new TLRPC.TL_phoneCallDiscardReasonHangup();
        if (ConnectionsManager.getInstance(this.currentAccount).getConnectionState() != 3)
        {
          if (!bool) {
            break label406;
          }
          if (paramRunnable != null) {
            paramRunnable.run();
          }
          callEnded();
          localObject = null;
        }
        break;
      }
    }
    for (;;)
    {
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_phone_discardCall, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error != null) {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.e("error on phone.discardCall: " + paramAnonymousTL_error);
            }
          }
          for (;;)
          {
            if (!bool)
            {
              AndroidUtilities.cancelRunOnUIThread(localObject);
              if (paramRunnable != null) {
                paramRunnable.run();
              }
            }
            return;
            if ((paramAnonymousTLObject instanceof TLRPC.TL_updates))
            {
              paramAnonymousTL_error = (TLRPC.TL_updates)paramAnonymousTLObject;
              MessagesController.getInstance(VoIPService.this.currentAccount).processUpdates(paramAnonymousTL_error, false);
            }
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("phone.discardCall " + paramAnonymousTLObject);
            }
          }
        }
      }, 2);
      return;
      i = 0;
      break;
      label349:
      l = 0L;
      break label232;
      localTL_phone_discardCall.reason = new TLRPC.TL_phoneCallDiscardReasonDisconnect();
      break label280;
      localTL_phone_discardCall.reason = new TLRPC.TL_phoneCallDiscardReasonMissed();
      break label280;
      localTL_phone_discardCall.reason = new TLRPC.TL_phoneCallDiscardReasonBusy();
      break label280;
      bool = false;
      break label294;
      label406:
      localObject = new Runnable()
      {
        private boolean done = false;
        
        public void run()
        {
          if (this.done) {
            return;
          }
          this.done = true;
          if (paramRunnable != null) {
            paramRunnable.run();
          }
          VoIPService.this.callEnded();
        }
      };
      AndroidUtilities.runOnUIThread((Runnable)localObject, (int)(VoIPServerConfig.getDouble("hangup_ui_timeout", 5.0D) * 1000.0D));
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.appDidLogout) {
      callEnded();
    }
  }
  
  public void forceRating()
  {
    this.forceRating = true;
  }
  
  public long getCallID()
  {
    if (this.call != null) {
      return this.call.id;
    }
    return 0L;
  }
  
  @TargetApi(26)
  public VoIPBaseService.CallConnection getConnectionAndStartCall()
  {
    if (this.systemCallConnection == null)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("creating call connection");
      }
      this.systemCallConnection = new VoIPBaseService.CallConnection(this);
      this.systemCallConnection.setInitializing();
      if (this.isOutgoing)
      {
        this.delayedStartOutgoingCall = new Runnable()
        {
          public void run()
          {
            VoIPService.access$002(VoIPService.this, null);
            VoIPService.this.startOutgoingCall();
          }
        };
        AndroidUtilities.runOnUIThread(this.delayedStartOutgoingCall, 2000L);
      }
      this.systemCallConnection.setCallerDisplayName(ContactsController.formatName(this.user.first_name, this.user.last_name), 1);
    }
    return this.systemCallConnection;
  }
  
  public byte[] getEncryptionKey()
  {
    return this.authKey;
  }
  
  public byte[] getGA()
  {
    return this.g_a;
  }
  
  protected Class<? extends Activity> getUIActivityClass()
  {
    return VoIPActivity.class;
  }
  
  public TLRPC.User getUser()
  {
    return this.user;
  }
  
  public void hangUp()
  {
    if ((this.currentState == 16) || ((this.currentState == 13) && (this.isOutgoing))) {}
    for (int i = 3;; i = 1)
    {
      declineIncomingCall(i, null);
      return;
    }
  }
  
  public void hangUp(Runnable paramRunnable)
  {
    if ((this.currentState == 16) || ((this.currentState == 13) && (this.isOutgoing))) {}
    for (int i = 3;; i = 1)
    {
      declineIncomingCall(i, paramRunnable);
      return;
    }
  }
  
  public IBinder onBind(Intent paramIntent)
  {
    return null;
  }
  
  public void onCallUpdated(TLRPC.PhoneCall paramPhoneCall)
  {
    if (this.call == null) {}
    label254:
    do
    {
      this.pendingUpdates.add(paramPhoneCall);
      for (;;)
      {
        return;
        if (paramPhoneCall != null)
        {
          if (paramPhoneCall.id != this.call.id)
          {
            if (!BuildVars.LOGS_ENABLED) {
              break;
            }
            FileLog.w("onCallUpdated called with wrong call id (got " + paramPhoneCall.id + ", expected " + this.call.id + ")");
            return;
          }
          if (paramPhoneCall.access_hash == 0L) {
            paramPhoneCall.access_hash = this.call.access_hash;
          }
          if (BuildVars.LOGS_ENABLED)
          {
            FileLog.d("Call updated: " + paramPhoneCall);
            dumpCallObject();
          }
          this.call = paramPhoneCall;
          if (!(paramPhoneCall instanceof TLRPC.TL_phoneCallDiscarded)) {
            break label254;
          }
          this.needSendDebugLog = paramPhoneCall.need_debug;
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("call discarded, stopping service");
          }
          if ((paramPhoneCall.reason instanceof TLRPC.TL_phoneCallDiscardReasonBusy))
          {
            dispatchStateChanged(17);
            this.playingSound = true;
            this.soundPool.play(this.spBusyId, 1.0F, 1.0F, 0, -1, 1.0F);
            AndroidUtilities.runOnUIThread(this.afterSoundRunnable, 1500L);
            stopSelf();
          }
          while ((paramPhoneCall.need_rating) || (this.forceRating))
          {
            startRatingActivity();
            return;
            callEnded();
          }
        }
      }
      if (((paramPhoneCall instanceof TLRPC.TL_phoneCall)) && (this.authKey == null))
      {
        if (paramPhoneCall.g_a_or_b == null)
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.w("stopping VoIP service, Ga == null");
          }
          callFailed();
          return;
        }
        if (!Arrays.equals(this.g_a_hash, Utilities.computeSHA256(paramPhoneCall.g_a_or_b, 0, paramPhoneCall.g_a_or_b.length)))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.w("stopping VoIP service, Ga hash doesn't match");
          }
          callFailed();
          return;
        }
        this.g_a = paramPhoneCall.g_a_or_b;
        Object localObject1 = new BigInteger(1, paramPhoneCall.g_a_or_b);
        Object localObject2 = new BigInteger(1, MessagesStorage.getInstance(this.currentAccount).getSecretPBytes());
        if (!Utilities.isGoodGaAndGb((BigInteger)localObject1, (BigInteger)localObject2))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.w("stopping VoIP service, bad Ga and Gb (accepting)");
          }
          callFailed();
          return;
        }
        localObject2 = ((BigInteger)localObject1).modPow(new BigInteger(1, this.a_or_b), (BigInteger)localObject2).toByteArray();
        if (localObject2.length > 256)
        {
          localObject1 = new byte['Ā'];
          System.arraycopy(localObject2, localObject2.length - 256, localObject1, 0, 256);
        }
        for (;;)
        {
          localObject2 = Utilities.computeSHA1((byte[])localObject1);
          byte[] arrayOfByte = new byte[8];
          System.arraycopy(localObject2, localObject2.length - 8, arrayOfByte, 0, 8);
          this.authKey = ((byte[])localObject1);
          this.keyFingerprint = Utilities.bytesToLong(arrayOfByte);
          if (this.keyFingerprint == paramPhoneCall.key_fingerprint) {
            break;
          }
          if (BuildVars.LOGS_ENABLED) {
            FileLog.w("key fingerprints don't match");
          }
          callFailed();
          return;
          localObject1 = localObject2;
          if (localObject2.length < 256)
          {
            localObject1 = new byte['Ā'];
            System.arraycopy(localObject2, 0, localObject1, 256 - localObject2.length, localObject2.length);
            int i = 0;
            while (i < 256 - localObject2.length)
            {
              localObject2[i] = 0;
              i += 1;
            }
          }
        }
        initiateActualEncryptedCall();
        return;
      }
      if (((paramPhoneCall instanceof TLRPC.TL_phoneCallAccepted)) && (this.authKey == null))
      {
        processAcceptedCall();
        return;
      }
    } while ((this.currentState != 13) || (paramPhoneCall.receive_date == 0));
    dispatchStateChanged(16);
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("!!!!!! CALL RECEIVED");
    }
    if (this.spPlayID != 0) {
      this.soundPool.stop(this.spPlayID);
    }
    this.spPlayID = this.soundPool.play(this.spRingbackID, 1.0F, 1.0F, 0, -1, 1.0F);
    if (this.timeoutRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.timeoutRunnable);
      this.timeoutRunnable = null;
    }
    this.timeoutRunnable = new Runnable()
    {
      public void run()
      {
        VoIPService.this.timeoutRunnable = null;
        VoIPService.this.declineIncomingCall(3, null);
      }
    };
    AndroidUtilities.runOnUIThread(this.timeoutRunnable, MessagesController.getInstance(this.currentAccount).callRingTimeout);
  }
  
  public void onCallUpgradeRequestReceived()
  {
    upgradeToGroupCall(new ArrayList());
  }
  
  public void onConnectionStateChanged(int paramInt)
  {
    if (paramInt == 3) {
      this.peerCapabilities = this.controller.getPeerCapabilities();
    }
    super.onConnectionStateChanged(paramInt);
  }
  
  protected void onControllerPreRelease()
  {
    if (this.needSendDebugLog)
    {
      String str = this.controller.getDebugLog();
      TLRPC.TL_phone_saveCallDebug localTL_phone_saveCallDebug = new TLRPC.TL_phone_saveCallDebug();
      localTL_phone_saveCallDebug.debug = new TLRPC.TL_dataJSON();
      localTL_phone_saveCallDebug.debug.data = str;
      localTL_phone_saveCallDebug.peer = new TLRPC.TL_inputPhoneCall();
      localTL_phone_saveCallDebug.peer.access_hash = this.call.access_hash;
      localTL_phone_saveCallDebug.peer.id = this.call.id;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_phone_saveCallDebug, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("Sent debug logs, response=" + paramAnonymousTLObject);
          }
        }
      });
    }
  }
  
  public void onCreate()
  {
    super.onCreate();
    if ((callIShouldHavePutIntoIntent != null) && (Build.VERSION.SDK_INT >= 26)) {
      startForeground(201, new Notification.Builder(this, "Other3").setSmallIcon(2131165543).setContentTitle(LocaleController.getString("VoipOutgoingCall", 2131494609)).setShowWhen(false).build());
    }
  }
  
  public void onGroupCallKeyReceived(byte[] paramArrayOfByte)
  {
    this.joiningGroupCall = true;
    this.groupCallEncryptionKey = paramArrayOfByte;
    paramArrayOfByte = Utilities.computeSHA1(this.groupCallEncryptionKey);
    byte[] arrayOfByte = new byte[8];
    System.arraycopy(paramArrayOfByte, paramArrayOfByte.length - 8, arrayOfByte, 0, 8);
    this.groupCallKeyFingerprint = Utilities.bytesToLong(arrayOfByte);
  }
  
  public void onGroupCallKeySent()
  {
    if (this.isOutgoing) {}
  }
  
  void onMediaButtonEvent(KeyEvent paramKeyEvent)
  {
    boolean bool = true;
    if ((paramKeyEvent.getKeyCode() == 79) && (paramKeyEvent.getAction() == 1))
    {
      if (this.currentState == 15) {
        acceptIncomingCall();
      }
    }
    else {
      return;
    }
    if (!isMicMute()) {}
    for (;;)
    {
      setMicMute(bool);
      paramKeyEvent = this.stateListeners.iterator();
      while (paramKeyEvent.hasNext()) {
        ((VoIPBaseService.StateListener)paramKeyEvent.next()).onAudioSettingsChanged();
      }
      break;
      bool = false;
    }
  }
  
  @SuppressLint({"MissingPermission"})
  public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
  {
    if (sharedInstance != null)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.e("Tried to start the VoIP service when it's already started");
      }
      return 2;
    }
    this.currentAccount = paramIntent.getIntExtra("account", -1);
    if (this.currentAccount == -1) {
      throw new IllegalStateException("No account specified when starting VoIP service");
    }
    paramInt1 = paramIntent.getIntExtra("user_id", 0);
    this.isOutgoing = paramIntent.getBooleanExtra("is_outgoing", false);
    this.user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramInt1));
    if (this.user == null)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.w("VoIPService: user==null");
      }
      stopSelf();
      return 2;
    }
    sharedInstance = this;
    if (this.isOutgoing)
    {
      dispatchStateChanged(14);
      if (USE_CONNECTION_SERVICE)
      {
        TelecomManager localTelecomManager = (TelecomManager)getSystemService("telecom");
        Bundle localBundle1 = new Bundle();
        Bundle localBundle2 = new Bundle();
        localBundle1.putParcelable("android.telecom.extra.PHONE_ACCOUNT_HANDLE", addAccountToTelecomManager());
        localBundle2.putInt("call_type", 1);
        localBundle1.putBundle("android.telecom.extra.OUTGOING_CALL_EXTRAS", localBundle2);
        localTelecomManager.placeCall(Uri.fromParts("sip", UserConfig.getInstance(this.currentAccount).getClientUserId() + ";user=" + this.user.id, null), localBundle1);
        if (paramIntent.getBooleanExtra("start_incall_activity", false)) {
          startActivity(new Intent(this, VoIPActivity.class).addFlags(268435456));
        }
      }
    }
    for (;;)
    {
      initializeAccountRelatedThings();
      return 2;
      this.delayedStartOutgoingCall = new Runnable()
      {
        public void run()
        {
          VoIPService.access$002(VoIPService.this, null);
          VoIPService.this.startOutgoingCall();
        }
      };
      AndroidUtilities.runOnUIThread(this.delayedStartOutgoingCall, 2000L);
      break;
      NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeInCallActivity, new Object[0]);
      this.call = callIShouldHavePutIntoIntent;
      callIShouldHavePutIntoIntent = null;
      if (USE_CONNECTION_SERVICE)
      {
        acknowledgeCall(false);
        showNotification();
      }
      else
      {
        acknowledgeCall(true);
      }
    }
  }
  
  public void onUIForegroundStateChanged(boolean paramBoolean)
  {
    if (this.currentState == 15)
    {
      if (paramBoolean) {
        stopForeground(true);
      }
    }
    else {
      return;
    }
    if (!((KeyguardManager)getSystemService("keyguard")).inKeyguardRestrictedInputMode())
    {
      if (NotificationManagerCompat.from(this).areNotificationsEnabled())
      {
        showIncomingNotification(ContactsController.formatName(this.user.first_name, this.user.last_name), null, this.user, null, 0, VoIPActivity.class);
        return;
      }
      declineIncomingCall(4, null);
      return;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        Intent localIntent = new Intent(VoIPService.this, VoIPActivity.class);
        localIntent.addFlags(805306368);
        try
        {
          PendingIntent.getActivity(VoIPService.this, 0, localIntent, 0).send();
          if (Build.VERSION.SDK_INT >= 26) {
            VoIPService.this.showNotification();
          }
          return;
        }
        catch (PendingIntent.CanceledException localCanceledException)
        {
          for (;;)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.e("error restarting activity", localCanceledException);
            }
            VoIPService.this.declineIncomingCall(4, null);
          }
        }
      }
    }, 500L);
  }
  
  protected void showNotification()
  {
    String str = ContactsController.formatName(this.user.first_name, this.user.last_name);
    if (this.user.photo != null) {}
    for (TLRPC.FileLocation localFileLocation = this.user.photo.photo_small;; localFileLocation = null)
    {
      showNotification(str, localFileLocation, VoIPActivity.class);
      return;
    }
  }
  
  protected void startRinging()
  {
    if (this.currentState == 15) {}
    for (;;)
    {
      return;
      if ((USE_CONNECTION_SERVICE) && (this.systemCallConnection != null)) {
        this.systemCallConnection.setRinging();
      }
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("starting ringing for call " + this.call.id);
      }
      dispatchStateChanged(15);
      startRingtoneAndVibration(this.user.id);
      if ((Build.VERSION.SDK_INT >= 21) && (!((KeyguardManager)getSystemService("keyguard")).inKeyguardRestrictedInputMode()) && (NotificationManagerCompat.from(this).areNotificationsEnabled()))
      {
        showIncomingNotification(ContactsController.formatName(this.user.first_name, this.user.last_name), null, this.user, null, 0, VoIPActivity.class);
        if (!BuildVars.LOGS_ENABLED) {
          continue;
        }
        FileLog.d("Showing incoming call notification");
        return;
      }
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("Starting incall activity for incoming call");
      }
      try
      {
        PendingIntent.getActivity(this, 12345, new Intent(this, VoIPActivity.class).addFlags(268435456), 0).send();
        if (Build.VERSION.SDK_INT < 26) {
          continue;
        }
        showNotification();
        return;
      }
      catch (Exception localException)
      {
        for (;;)
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.e("Error starting incall activity", localException);
          }
        }
      }
    }
  }
  
  protected void updateServerConfig()
  {
    final SharedPreferences localSharedPreferences = MessagesController.getMainSettings(this.currentAccount);
    VoIPServerConfig.setConfig(localSharedPreferences.getString("voip_server_config", "{}"));
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(new TLRPC.TL_phone_getCallConfig(), new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = ((TLRPC.TL_dataJSON)paramAnonymousTLObject).data;
          VoIPServerConfig.setConfig(paramAnonymousTLObject);
          localSharedPreferences.edit().putString("voip_server_config", paramAnonymousTLObject).commit();
        }
      }
    });
  }
  
  public void upgradeToGroupCall(List<Integer> paramList)
  {
    if (this.upgrading) {
      return;
    }
    this.groupUsersToAdd = paramList;
    if (!this.isOutgoing)
    {
      this.controller.requestCallUpgrade();
      return;
    }
    this.upgrading = true;
    this.groupCallEncryptionKey = new byte['Ā'];
    Utilities.random.nextBytes(this.groupCallEncryptionKey);
    paramList = this.groupCallEncryptionKey;
    paramList[0] = ((byte)(paramList[0] & 0x7F));
    paramList = Utilities.computeSHA1(this.groupCallEncryptionKey);
    byte[] arrayOfByte = new byte[8];
    System.arraycopy(paramList, paramList.length - 8, arrayOfByte, 0, 8);
    this.groupCallKeyFingerprint = Utilities.bytesToLong(arrayOfByte);
    this.controller.sendGroupCallKey(this.groupCallEncryptionKey);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/VoIPService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */