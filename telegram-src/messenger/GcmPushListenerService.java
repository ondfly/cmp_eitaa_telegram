package org.telegram.messenger;

import android.text.TextUtils;
import android.util.Base64;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerChat;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_updateReadChannelInbox;
import org.telegram.tgnet.TLRPC.TL_updateReadHistoryInbox;
import org.telegram.tgnet.TLRPC.TL_updateServiceNotification;
import org.telegram.tgnet.TLRPC.TL_updates;

public class GcmPushListenerService
  extends FirebaseMessagingService
{
  public static final int NOTIFICATION_ID = 1;
  
  private void onDecryptError()
  {
    int i = 0;
    while (i < 3)
    {
      if (UserConfig.getInstance(i).isClientActivated())
      {
        ConnectionsManager.onInternalPushReceived(i);
        ConnectionsManager.getInstance(i).resumeNetworkMaybe();
      }
      i += 1;
    }
  }
  
  public void onMessageReceived(RemoteMessage paramRemoteMessage)
  {
    String str = paramRemoteMessage.getFrom();
    final Map localMap = paramRemoteMessage.getData();
    final long l = paramRemoteMessage.getSentTime();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("GCM received data: " + localMap + " from: " + str);
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        ApplicationLoader.postInitApplication();
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            Object localObject6 = null;
            final Object localObject4 = null;
            Object localObject7;
            Object localObject8;
            label319:
            label334:
            int m;
            label341:
            final int j;
            label500:
            int n;
            label532:
            int i1;
            label565:
            label598:
            int i2;
            label730:
            boolean bool2;
            label761:
            Object localObject9;
            label902:
            label1064:
            label1260:
            boolean bool4;
            boolean bool1;
            try
            {
              localObject1 = GcmPushListenerService.1.this.val$data.get("p");
              if (!(localObject1 instanceof String))
              {
                GcmPushListenerService.this.onDecryptError();
                return;
              }
              localObject5 = Base64.decode((String)localObject1, 8);
              localObject1 = new NativeByteBuffer(localObject5.length);
              ((NativeByteBuffer)localObject1).writeBytes((byte[])localObject5);
              ((NativeByteBuffer)localObject1).position(0);
              if (SharedConfig.pushAuthKeyId == null)
              {
                SharedConfig.pushAuthKeyId = new byte[8];
                localObject7 = Utilities.computeSHA1(SharedConfig.pushAuthKey);
                System.arraycopy(localObject7, localObject7.length - 8, SharedConfig.pushAuthKeyId, 0, 8);
              }
              localObject7 = new byte[8];
              ((NativeByteBuffer)localObject1).readBytes((byte[])localObject7, true);
              if (!Arrays.equals(SharedConfig.pushAuthKeyId, (byte[])localObject7))
              {
                GcmPushListenerService.this.onDecryptError();
                return;
              }
              localObject7 = new byte[16];
              ((NativeByteBuffer)localObject1).readBytes((byte[])localObject7, true);
              localObject8 = MessageKeyData.generateMessageKeyData(SharedConfig.pushAuthKey, (byte[])localObject7, true, 2);
              Utilities.aesIgeEncryption(((NativeByteBuffer)localObject1).buffer, ((MessageKeyData)localObject8).aesKey, ((MessageKeyData)localObject8).aesIv, false, false, 24, localObject5.length - 24);
              if (!Utilities.arraysEquals((byte[])localObject7, 0, Utilities.computeSHA256(SharedConfig.pushAuthKey, 96, 32, ((NativeByteBuffer)localObject1).buffer, 24, ((NativeByteBuffer)localObject1).buffer.limit()), 8))
              {
                GcmPushListenerService.this.onDecryptError();
                return;
              }
              localObject5 = new byte[((NativeByteBuffer)localObject1).readInt32(true)];
              ((NativeByteBuffer)localObject1).readBytes((byte[])localObject5, true);
              localObject7 = new JSONObject(new String((byte[])localObject5, "UTF-8"));
              localObject8 = ((JSONObject)localObject7).getJSONObject("custom");
              if (((JSONObject)localObject7).has("user_id")) {
                localObject1 = ((JSONObject)localObject7).get("user_id");
              }
              for (;;)
              {
                if (localObject1 == null)
                {
                  i = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                  m = UserConfig.selectedAccount;
                  k = 0;
                  j = m;
                  if (k < 3)
                  {
                    j = UserConfig.getInstance(k).getClientUserId();
                    if (j != i) {
                      break label902;
                    }
                    j = k;
                  }
                  k = j;
                  localObject6 = localObject4;
                }
                try
                {
                  if (!UserConfig.getInstance(k).isClientActivated()) {
                    break label9350;
                  }
                  localObject6 = localObject4;
                  if (((JSONObject)localObject7).has("loc_key")) {
                    localObject6 = localObject4;
                  }
                  for (localObject5 = ((JSONObject)localObject7).getString("loc_key");; localObject5 = "")
                  {
                    localObject6 = localObject5;
                    GcmPushListenerService.1.this.val$data.get("google.sent_time");
                    i = -1;
                    localObject6 = localObject5;
                    switch (((String)localObject5).hashCode())
                    {
                    case -920689527: 
                      l = 0L;
                      localObject6 = localObject5;
                      if (!((JSONObject)localObject8).has("channel_id")) {
                        break label9379;
                      }
                      localObject6 = localObject5;
                      m = ((JSONObject)localObject8).getInt("channel_id");
                      l = -m;
                      localObject6 = localObject5;
                      if (!((JSONObject)localObject8).has("from_id")) {
                        break label9385;
                      }
                      localObject6 = localObject5;
                      n = ((JSONObject)localObject8).getInt("from_id");
                      l = n;
                      localObject6 = localObject5;
                      if (!((JSONObject)localObject8).has("chat_id")) {
                        break label9391;
                      }
                      localObject6 = localObject5;
                      i1 = ((JSONObject)localObject8).getInt("chat_id");
                      l = -i1;
                      if (l == 0L) {
                        break label9350;
                      }
                      localObject6 = localObject5;
                      if (!((JSONObject)localObject7).has("badge")) {
                        break label9397;
                      }
                      localObject6 = localObject5;
                      i = ((JSONObject)localObject7).getInt("badge");
                      if (i == 0) {
                        break label9079;
                      }
                      localObject6 = localObject5;
                      i5 = ((JSONObject)localObject8).getInt("msg_id");
                      localObject6 = localObject5;
                      localObject4 = (Integer)MessagesController.getInstance(k).dialogs_read_inbox_max.get(Long.valueOf(l));
                      localObject1 = localObject4;
                      if (localObject4 == null)
                      {
                        localObject6 = localObject5;
                        localObject1 = Integer.valueOf(MessagesStorage.getInstance(k).getDialogReadMax(false, l));
                        localObject6 = localObject5;
                        MessagesController.getInstance(j).dialogs_read_inbox_max.put(Long.valueOf(l), localObject1);
                      }
                      localObject6 = localObject5;
                      if (i5 <= ((Integer)localObject1).intValue()) {
                        break label9350;
                      }
                      localObject6 = localObject5;
                      if (!((JSONObject)localObject8).has("chat_from_id")) {
                        break label9402;
                      }
                      localObject6 = localObject5;
                      i2 = ((JSONObject)localObject8).getInt("chat_from_id");
                      localObject6 = localObject5;
                      if (!((JSONObject)localObject8).has("mention")) {
                        break label9408;
                      }
                      localObject6 = localObject5;
                      if (((JSONObject)localObject8).getInt("mention") == 0) {
                        break label9408;
                      }
                      bool2 = true;
                      localObject6 = localObject5;
                      if (!((JSONObject)localObject7).has("loc_args")) {
                        break label9414;
                      }
                      localObject6 = localObject5;
                      localObject4 = ((JSONObject)localObject7).getJSONArray("loc_args");
                      localObject6 = localObject5;
                      localObject1 = new String[((JSONArray)localObject4).length()];
                      i = 0;
                      for (;;)
                      {
                        localObject6 = localObject5;
                        localObject9 = localObject1;
                        if (i >= localObject1.length) {
                          break;
                        }
                        localObject6 = localObject5;
                        localObject1[i] = ((JSONArray)localObject4).getString(i);
                        i += 1;
                      }
                      localObject1 = null;
                      break label319;
                      if ((localObject1 instanceof Integer))
                      {
                        i = ((Integer)localObject1).intValue();
                        break label334;
                      }
                      if ((localObject1 instanceof String))
                      {
                        i = Utilities.parseInt((String)localObject1).intValue();
                        break label334;
                      }
                      i = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                      break label334;
                      k += 1;
                      break label341;
                    }
                  }
                  localObject6 = localObject5;
                  if (!((String)localObject5).equals("DC_UPDATE")) {
                    break label9351;
                  }
                  i = 0;
                }
                catch (Throwable localThrowable1) {}
              }
              localObject6 = localObject5;
              if (!((String)localObject5).equals("MESSAGE_ANNOUNCEMENT")) {
                break label9351;
              }
              i = 1;
            }
            catch (Throwable localThrowable2)
            {
              for (;;)
              {
                Object localObject1;
                Object localObject5;
                long l;
                int i5;
                Object localObject2;
                int i4;
                boolean bool3;
                int k = -1;
              }
            }
            localObject6 = localObject5;
            int i = ((JSONObject)localObject8).getInt("dc");
            localObject6 = localObject5;
            localObject1 = ((JSONObject)localObject8).getString("addr").split(":");
            localObject6 = localObject5;
            Object localObject11;
            Object localObject10;
            if (localObject1.length == 2)
            {
              localObject4 = localObject1[0];
              localObject6 = localObject5;
              j = Integer.parseInt(localObject1[1]);
              localObject6 = localObject5;
              ConnectionsManager.getInstance(k).applyDatacenterAddress(i, (String)localObject4, j);
              localObject6 = localObject5;
              ConnectionsManager.getInstance(k).resumeNetworkMaybe();
              return;
              if (k != -1)
              {
                ConnectionsManager.onInternalPushReceived(k);
                ConnectionsManager.getInstance(k).resumeNetworkMaybe();
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.e("error in loc_key = " + (String)localObject6);
                }
                FileLog.e(localThrowable1);
                return;
                localObject6 = localObject5;
                localObject2 = new TLRPC.TL_updateServiceNotification();
                localObject6 = localObject5;
                ((TLRPC.TL_updateServiceNotification)localObject2).popup = false;
                localObject6 = localObject5;
                ((TLRPC.TL_updateServiceNotification)localObject2).flags = 2;
                localObject6 = localObject5;
                ((TLRPC.TL_updateServiceNotification)localObject2).inbox_date = ((int)(GcmPushListenerService.1.this.val$time / 1000L));
                localObject6 = localObject5;
                ((TLRPC.TL_updateServiceNotification)localObject2).message = ((JSONObject)localObject7).getString("message");
                localObject6 = localObject5;
                ((TLRPC.TL_updateServiceNotification)localObject2).type = "announcement";
                localObject6 = localObject5;
                ((TLRPC.TL_updateServiceNotification)localObject2).media = new TLRPC.TL_messageMediaEmpty();
                localObject6 = localObject5;
                localObject4 = new TLRPC.TL_updates();
                localObject6 = localObject5;
                ((TLRPC.TL_updates)localObject4).updates.add(localObject2);
                localObject6 = localObject5;
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    MessagesController.getInstance(j).processUpdates(localObject4, false);
                  }
                });
                localObject6 = localObject5;
                ConnectionsManager.getInstance(k).resumeNetworkMaybe();
                return;
                localObject11 = null;
                localObject10 = null;
                localObject2 = localObject9[0];
                localObject4 = null;
                bool4 = false;
                i4 = 0;
                i = 0;
                bool3 = false;
                localObject6 = localObject5;
                if (((String)localObject5).startsWith("CHAT_"))
                {
                  if (m == 0) {
                    break label9811;
                  }
                  j = 1;
                  break label9420;
                  localObject6 = localObject5;
                  if (BuildVars.LOGS_ENABLED)
                  {
                    localObject6 = localObject5;
                    FileLog.d("GCM received message notification " + (String)localObject5 + " for dialogId = " + l + " mid = " + i5);
                  }
                  i = -1;
                  localObject6 = localObject5;
                  switch (((String)localObject5).hashCode())
                  {
                  case 1060749957: 
                    label2080:
                    localObject6 = localObject5;
                    localObject2 = localObject11;
                    bool1 = bool4;
                    localObject4 = localObject10;
                    if (BuildVars.LOGS_ENABLED)
                    {
                      localObject6 = localObject5;
                      FileLog.w("unhandled loc_key = " + (String)localObject5);
                      localObject4 = localObject10;
                      bool1 = bool4;
                      localObject2 = localObject11;
                    }
                    label2142:
                    if (localObject2 != null)
                    {
                      localObject6 = localObject5;
                      localObject9 = new TLRPC.TL_message();
                      localObject6 = localObject5;
                      ((TLRPC.TL_message)localObject9).id = i5;
                      if (localObject4 == null) {
                        break label9835;
                      }
                    }
                    break;
                  }
                }
              }
            }
            for (;;)
            {
              localObject6 = localObject5;
              ((TLRPC.TL_message)localObject9).message = ((String)localObject4);
              localObject6 = localObject5;
              ((TLRPC.TL_message)localObject9).date = ((int)(GcmPushListenerService.1.this.val$time / 1000L));
              int i3;
              if (i3 != 0)
              {
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).action = new TLRPC.TL_messageActionPinMessage();
              }
              if (j != 0)
              {
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).flags |= 0x80000000;
              }
              if (m != 0)
              {
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).to_id = new TLRPC.TL_peerChannel();
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).to_id.channel_id = m;
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).dialog_id = (-m);
                label2299:
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).from_id = i2;
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).mentioned = bool2;
                localObject6 = localObject5;
                localObject2 = new MessageObject(k, (TLRPC.Message)localObject9, (String)localObject2, (String)localObject7, (String)localObject8, bool1, bool3);
                localObject6 = localObject5;
                localObject4 = new ArrayList();
                localObject6 = localObject5;
                ((ArrayList)localObject4).add(localObject2);
                localObject6 = localObject5;
                NotificationsController.getInstance(k).processNewMessages((ArrayList)localObject4, true, true);
              }
              for (;;)
              {
                localObject6 = localObject5;
                ConnectionsManager.onInternalPushReceived(k);
                localObject6 = localObject5;
                ConnectionsManager.getInstance(k).resumeNetworkMaybe();
                return;
                localObject6 = localObject5;
                if (((String)localObject5).startsWith("PINNED_"))
                {
                  if (i2 == 0) {
                    break label9830;
                  }
                  j = 1;
                  break label9816;
                }
                localObject6 = localObject5;
                localObject7 = localObject2;
                localObject8 = localObject4;
                i3 = i;
                j = i4;
                if (!((String)localObject5).startsWith("CHANNEL_")) {
                  break;
                }
                bool3 = true;
                localObject7 = localObject2;
                localObject8 = localObject4;
                i3 = i;
                j = i4;
                break;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_TEXT")) {
                  break label9436;
                }
                i = 0;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_NOTEXT")) {
                  break label9436;
                }
                i = 1;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_PHOTO")) {
                  break label9436;
                }
                i = 2;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_PHOTO_SECRET")) {
                  break label9436;
                }
                i = 3;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_VIDEO")) {
                  break label9436;
                }
                i = 4;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_VIDEO_SECRET")) {
                  break label9436;
                }
                i = 5;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_SCREENSHOT")) {
                  break label9436;
                }
                i = 6;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_ROUND")) {
                  break label9436;
                }
                i = 7;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_DOC")) {
                  break label9436;
                }
                i = 8;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_STICKER")) {
                  break label9436;
                }
                i = 9;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_AUDIO")) {
                  break label9436;
                }
                i = 10;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_CONTACT")) {
                  break label9436;
                }
                i = 11;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_GEO")) {
                  break label9436;
                }
                i = 12;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_GEOLIVE")) {
                  break label9436;
                }
                i = 13;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_GIF")) {
                  break label9436;
                }
                i = 14;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_GAME")) {
                  break label9436;
                }
                i = 15;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_INVOICE")) {
                  break label9436;
                }
                i = 16;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_FWDS")) {
                  break label9436;
                }
                i = 17;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGE_PHOTOS")) {
                  break label9436;
                }
                i = 18;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("MESSAGES")) {
                  break label9436;
                }
                i = 19;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_TEXT")) {
                  break label9436;
                }
                i = 20;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_NOTEXT")) {
                  break label9436;
                }
                i = 21;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_PHOTO")) {
                  break label9436;
                }
                i = 22;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_VIDEO")) {
                  break label9436;
                }
                i = 23;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_ROUND")) {
                  break label9436;
                }
                i = 24;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_DOC")) {
                  break label9436;
                }
                i = 25;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_STICKER")) {
                  break label9436;
                }
                i = 26;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_AUDIO")) {
                  break label9436;
                }
                i = 27;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_CONTACT")) {
                  break label9436;
                }
                i = 28;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_GEO")) {
                  break label9436;
                }
                i = 29;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_GEOLIVE")) {
                  break label9436;
                }
                i = 30;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_GIF")) {
                  break label9436;
                }
                i = 31;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_GAME")) {
                  break label9436;
                }
                i = 32;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_FWDS")) {
                  break label9436;
                }
                i = 33;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGE_PHOTOS")) {
                  break label9436;
                }
                i = 34;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHANNEL_MESSAGES")) {
                  break label9436;
                }
                i = 35;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_TEXT")) {
                  break label9436;
                }
                i = 36;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_NOTEXT")) {
                  break label9436;
                }
                i = 37;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_PHOTO")) {
                  break label9436;
                }
                i = 38;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_VIDEO")) {
                  break label9436;
                }
                i = 39;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_ROUND")) {
                  break label9436;
                }
                i = 40;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_DOC")) {
                  break label9436;
                }
                i = 41;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_STICKER")) {
                  break label9436;
                }
                i = 42;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_AUDIO")) {
                  break label9436;
                }
                i = 43;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_CONTACT")) {
                  break label9436;
                }
                i = 44;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_GEO")) {
                  break label9436;
                }
                i = 45;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_GEOLIVE")) {
                  break label9436;
                }
                i = 46;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_GIF")) {
                  break label9436;
                }
                i = 47;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_GAME")) {
                  break label9436;
                }
                i = 48;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_INVOICE")) {
                  break label9436;
                }
                i = 49;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_CREATED")) {
                  break label9436;
                }
                i = 50;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_TITLE_EDITED")) {
                  break label9436;
                }
                i = 51;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_PHOTO_EDITED")) {
                  break label9436;
                }
                i = 52;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_ADD_MEMBER")) {
                  break label9436;
                }
                i = 53;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_ADD_YOU")) {
                  break label9436;
                }
                i = 54;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_DELETE_MEMBER")) {
                  break label9436;
                }
                i = 55;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_DELETE_YOU")) {
                  break label9436;
                }
                i = 56;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_LEFT")) {
                  break label9436;
                }
                i = 57;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_RETURNED")) {
                  break label9436;
                }
                i = 58;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_JOINED")) {
                  break label9436;
                }
                i = 59;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_FWDS")) {
                  break label9436;
                }
                i = 60;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGE_PHOTOS")) {
                  break label9436;
                }
                i = 61;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CHAT_MESSAGES")) {
                  break label9436;
                }
                i = 62;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_TEXT")) {
                  break label9436;
                }
                i = 63;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_NOTEXT")) {
                  break label9436;
                }
                i = 64;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_PHOTO")) {
                  break label9436;
                }
                i = 65;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_VIDEO")) {
                  break label9436;
                }
                i = 66;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_ROUND")) {
                  break label9436;
                }
                i = 67;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_DOC")) {
                  break label9436;
                }
                i = 68;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_STICKER")) {
                  break label9436;
                }
                i = 69;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_AUDIO")) {
                  break label9436;
                }
                i = 70;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_CONTACT")) {
                  break label9436;
                }
                i = 71;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_GEO")) {
                  break label9436;
                }
                i = 72;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_GEOLIVE")) {
                  break label9436;
                }
                i = 73;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_GAME")) {
                  break label9436;
                }
                i = 74;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_INVOICE")) {
                  break label9436;
                }
                i = 75;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PINNED_GIF")) {
                  break label9436;
                }
                i = 76;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("CONTACT_JOINED")) {
                  break label9436;
                }
                i = 77;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("AUTH_UNKNOWN")) {
                  break label9436;
                }
                i = 78;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("AUTH_REGION")) {
                  break label9436;
                }
                i = 79;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("ENCRYPTION_REQUEST")) {
                  break label9436;
                }
                i = 80;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("ENCRYPTION_ACCEPT")) {
                  break label9436;
                }
                i = 81;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("ENCRYPTED_MESSAGE")) {
                  break label9436;
                }
                i = 82;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("LOCKED_MESSAGE")) {
                  break label9436;
                }
                i = 83;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PHONE_CALL_REQUEST")) {
                  break label9436;
                }
                i = 84;
                break label9436;
                localObject6 = localObject5;
                if (!((String)localObject5).equals("PHONE_CALL_MISSED")) {
                  break label9436;
                }
                i = 85;
                break label9436;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { localObject9[0], localObject9[1] });
                localObject4 = localObject9[1];
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageNoText", 2131493993, new Object[] { localObject9[0] });
                localObject4 = "";
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessagePhoto", 2131493994, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachPhoto", 2131493037);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageSDPhoto", 2131493996, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachPhoto", 2131493037);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageVideo", 2131494001, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachVideo", 2131493043);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageSDVideo", 2131493997, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachVideo", 2131493043);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.getString("ActionTakeScreenshoot", 2131492907).replace("un1", localObject9[0]);
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageRound", 2131493995, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachRound", 2131493039);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageDocument", 2131493968, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachDocument", 2131493026);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                if (localObject9.length > 1)
                {
                  localObject6 = localObject5;
                  if (!TextUtils.isEmpty(localObject9[1])) {
                    localObject6 = localObject5;
                  }
                }
                for (localObject2 = LocaleController.formatString("NotificationMessageStickerEmoji", 2131493999, new Object[] { localObject9[0], localObject9[1] });; localObject2 = LocaleController.formatString("NotificationMessageSticker", 2131493998, new Object[] { localObject9[0] }))
                {
                  localObject6 = localObject5;
                  localObject4 = LocaleController.getString("AttachSticker", 2131493040);
                  bool1 = bool4;
                  break;
                  localObject6 = localObject5;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageAudio", 2131493966, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachAudio", 2131493023);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageContact", 2131493967, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachContact", 2131493025);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageMap", 2131493991, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachLocation", 2131493033);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageLiveLocation", 2131493990, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachLiveLocation", 2131493031);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGif", 2131493972, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachGif", 2131493028);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGame", 2131493971, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachGame", 2131493027);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageInvoice", 2131493989, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("PaymentInvoice", 2131494102);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageForwardFew", 2131493970, new Object[] { localObject9[0], LocaleController.formatPluralString("messages", Utilities.parseInt(localObject9[1]).intValue()) });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageFew", 2131493969, new Object[] { localObject9[0], LocaleController.formatPluralString("Photos", Utilities.parseInt(localObject9[1]).intValue()) });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageFew", 2131493969, new Object[] { localObject9[0], LocaleController.formatPluralString("messages", Utilities.parseInt(localObject9[1]).intValue()) });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageText", 2131494000, new Object[] { localObject9[0], localObject9[1] });
                localObject4 = localObject9[1];
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageNoText", 2131493187, new Object[] { localObject9[0] });
                localObject4 = "";
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessagePhoto", 2131493188, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachPhoto", 2131493037);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageVideo", 2131493192, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachVideo", 2131493043);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageRound", 2131493189, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachRound", 2131493039);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageDocument", 2131493181, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachDocument", 2131493026);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                if (localObject9.length > 1)
                {
                  localObject6 = localObject5;
                  if (!TextUtils.isEmpty(localObject9[1])) {
                    localObject6 = localObject5;
                  }
                }
                for (localObject2 = LocaleController.formatString("ChannelMessageStickerEmoji", 2131493191, new Object[] { localObject9[0], localObject9[1] });; localObject2 = LocaleController.formatString("ChannelMessageSticker", 2131493190, new Object[] { localObject9[0] }))
                {
                  localObject6 = localObject5;
                  localObject4 = LocaleController.getString("AttachSticker", 2131493040);
                  bool1 = bool4;
                  break;
                  localObject6 = localObject5;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageAudio", 2131493179, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachAudio", 2131493023);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageContact", 2131493180, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachContact", 2131493025);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageMap", 2131493185, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachLocation", 2131493033);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageLiveLocation", 2131493184, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachLiveLocation", 2131493031);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageGIF", 2131493183, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachGif", 2131493028);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGame", 2131493971, new Object[] { localObject9[0] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachGame", 2131493027);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageFew", 2131493182, new Object[] { localObject9[0], LocaleController.formatPluralString("ForwardedMessageCount", Utilities.parseInt(localObject9[1]).intValue()).toLowerCase() });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageFew", 2131493182, new Object[] { localObject9[0], LocaleController.formatPluralString("Photos", Utilities.parseInt(localObject9[1]).intValue()) });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("ChannelMessageFew", 2131493182, new Object[] { localObject9[0], LocaleController.formatPluralString("messages", Utilities.parseInt(localObject9[1]).intValue()) });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupText", 2131493987, new Object[] { localObject9[0], localObject9[1], localObject9[2] });
                localObject4 = localObject9[1];
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupNoText", 2131493982, new Object[] { localObject9[0], localObject9[1] });
                localObject4 = "";
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupPhoto", 2131493983, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachPhoto", 2131493037);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupVideo", 2131493988, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachVideo", 2131493043);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupRound", 2131493984, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachRound", 2131493039);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupDocument", 2131493975, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachDocument", 2131493026);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                if (localObject9.length > 2)
                {
                  localObject6 = localObject5;
                  if (!TextUtils.isEmpty(localObject9[2])) {
                    localObject6 = localObject5;
                  }
                }
                for (localObject2 = LocaleController.formatString("NotificationMessageGroupStickerEmoji", 2131493986, new Object[] { localObject9[0], localObject9[1], localObject9[2] });; localObject2 = LocaleController.formatString("NotificationMessageGroupSticker", 2131493985, new Object[] { localObject9[0], localObject9[1] }))
                {
                  localObject6 = localObject5;
                  localObject4 = LocaleController.getString("AttachSticker", 2131493040);
                  bool1 = bool4;
                  break;
                  localObject6 = localObject5;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupAudio", 2131493973, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachAudio", 2131493023);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupContact", 2131493974, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachContact", 2131493025);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupMap", 2131493980, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachLocation", 2131493033);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupLiveLocation", 2131493979, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachLiveLocation", 2131493031);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupGif", 2131493977, new Object[] { localObject9[0], localObject9[1] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachGif", 2131493028);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupGame", 2131493976, new Object[] { localObject9[0], localObject9[1], localObject9[2] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("AttachGame", 2131493027);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationMessageGroupInvoice", 2131493978, new Object[] { localObject9[0], localObject9[1], localObject9[2] });
                localObject6 = localObject5;
                localObject4 = LocaleController.getString("PaymentInvoice", 2131494102);
                bool1 = bool4;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationInvitedToGroup", 2131493964, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationEditedGroupName", 2131493954, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationEditedGroupPhoto", 2131493955, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupAddMember", 2131493956, new Object[] { localObject9[0], localObject9[1], localObject9[2] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationInvitedToGroup", 2131493964, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupKickMember", 2131493961, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupKickYou", 2131493962, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupLeftMember", 2131493963, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupAddSelf", 2131493957, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupAddSelfMega", 2131493958, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupForwardedFew", 2131493960, new Object[] { localObject9[0], localObject9[1], LocaleController.formatPluralString("messages", Utilities.parseInt(localObject9[2]).intValue()) });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupFew", 2131493959, new Object[] { localObject9[0], localObject9[1], LocaleController.formatPluralString("Photos", Utilities.parseInt(localObject9[2]).intValue()) });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationGroupFew", 2131493959, new Object[] { localObject9[0], localObject9[1], LocaleController.formatPluralString("messages", Utilities.parseInt(localObject9[2]).intValue()) });
                bool1 = true;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedText", 2131493946, new Object[] { localObject9[0], localObject9[1], localObject9[2] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedTextChannel", 2131493947, new Object[] { localObject9[0], localObject9[1] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedNoText", 2131493936, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedNoTextChannel", 2131493937, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedPhoto", 2131493938, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedPhotoChannel", 2131493939, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedVideo", 2131493948, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedVideoChannel", 2131493949, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedRound", 2131493940, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedRoundChannel", 2131493941, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedFile", 2131493922, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedFileChannel", 2131493923, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  if (localObject9.length > 2)
                  {
                    localObject6 = localObject5;
                    if (!TextUtils.isEmpty(localObject9[2]))
                    {
                      localObject6 = localObject5;
                      localObject2 = LocaleController.formatString("NotificationActionPinnedStickerEmoji", 2131493944, new Object[] { localObject9[0], localObject9[1], localObject9[2] });
                      bool1 = bool4;
                      localObject4 = localObject10;
                      break label2142;
                    }
                  }
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedSticker", 2131493942, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                if (localObject9.length > 1)
                {
                  localObject6 = localObject5;
                  if (!TextUtils.isEmpty(localObject9[1]))
                  {
                    localObject6 = localObject5;
                    localObject2 = LocaleController.formatString("NotificationActionPinnedStickerEmojiChannel", 2131493945, new Object[] { localObject9[0], localObject9[1] });
                    bool1 = bool4;
                    localObject4 = localObject10;
                    break label2142;
                  }
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedStickerChannel", 2131493943, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedVoice", 2131493950, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedVoiceChannel", 2131493951, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedContact", 2131493920, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedContactChannel", 2131493921, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedGeo", 2131493926, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedGeoChannel", 2131493927, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedGeoLive", 2131493928, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedGeoLiveChannel", 2131493929, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedGame", 2131493924, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedGameChannel", 2131493925, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedInvoice", 2131493932, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedInvoiceChannel", 2131493933, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i2 != 0)
                {
                  localObject6 = localObject5;
                  localObject2 = LocaleController.formatString("NotificationActionPinnedGif", 2131493930, new Object[] { localObject9[0], localObject9[1] });
                  bool1 = bool4;
                  localObject4 = localObject10;
                  break label2142;
                }
                localObject6 = localObject5;
                localObject2 = LocaleController.formatString("NotificationActionPinnedGifChannel", 2131493931, new Object[] { localObject9[0] });
                bool1 = bool4;
                localObject4 = localObject10;
                break label2142;
                if (i1 != 0)
                {
                  localObject6 = localObject5;
                  ((TLRPC.TL_message)localObject9).to_id = new TLRPC.TL_peerChat();
                  localObject6 = localObject5;
                  ((TLRPC.TL_message)localObject9).to_id.chat_id = i1;
                  localObject6 = localObject5;
                  ((TLRPC.TL_message)localObject9).dialog_id = (-i1);
                  break label2299;
                }
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).to_id = new TLRPC.TL_peerUser();
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).to_id.user_id = n;
                localObject6 = localObject5;
                ((TLRPC.TL_message)localObject9).dialog_id = n;
                break label2299;
                label9079:
                localObject6 = localObject5;
                i = ((JSONObject)localObject8).getInt("max_id");
                localObject6 = localObject5;
                localObject2 = new ArrayList();
                localObject6 = localObject5;
                if (BuildVars.LOGS_ENABLED)
                {
                  localObject6 = localObject5;
                  FileLog.d("GCM received read notification max_id = " + i + " for dialogId = " + l);
                }
                if (m == 0) {
                  break label9224;
                }
                localObject6 = localObject5;
                localObject4 = new TLRPC.TL_updateReadChannelInbox();
                localObject6 = localObject5;
                ((TLRPC.TL_updateReadChannelInbox)localObject4).channel_id = m;
                localObject6 = localObject5;
                ((TLRPC.TL_updateReadChannelInbox)localObject4).max_id = i;
                localObject6 = localObject5;
                ((ArrayList)localObject2).add(localObject4);
                localObject6 = localObject5;
                MessagesController.getInstance(j).processUpdateArray((ArrayList)localObject2, null, null, false);
              }
              label9224:
              localObject6 = localObject5;
              localObject4 = new TLRPC.TL_updateReadHistoryInbox();
              if (n != 0)
              {
                localObject6 = localObject5;
                ((TLRPC.TL_updateReadHistoryInbox)localObject4).peer = new TLRPC.TL_peerUser();
                localObject6 = localObject5;
                ((TLRPC.TL_updateReadHistoryInbox)localObject4).peer.user_id = n;
              }
              for (;;)
              {
                localObject6 = localObject5;
                ((TLRPC.TL_updateReadHistoryInbox)localObject4).max_id = i;
                localObject6 = localObject5;
                ((ArrayList)localObject2).add(localObject4);
                break;
                localObject6 = localObject5;
                ((TLRPC.TL_updateReadHistoryInbox)localObject4).peer = new TLRPC.TL_peerChat();
                localObject6 = localObject5;
                ((TLRPC.TL_updateReadHistoryInbox)localObject4).peer.chat_id = i1;
              }
              GcmPushListenerService.this.onDecryptError();
              break label1064;
              label9350:
              return;
              label9351:
              switch (i)
              {
              }
              break;
              label9379:
              m = 0;
              break label500;
              label9385:
              n = 0;
              break label532;
              label9391:
              i1 = 0;
              break label565;
              label9397:
              i = 0;
              break label598;
              label9402:
              i2 = 0;
              break label730;
              label9408:
              bool2 = false;
              break label761;
              label9414:
              localObject9 = null;
              break label1260;
              label9420:
              label9436:
              Object localObject3;
              for (;;)
              {
                localObject7 = localObject9[1];
                localObject8 = localThrowable2;
                i3 = i;
                break;
                localObject3 = localObject11;
                bool1 = bool4;
                localObject4 = localObject10;
                switch (i)
                {
                }
                break label2080;
                label9811:
                j = 0;
              }
              for (;;)
              {
                label9816:
                i3 = 1;
                localObject7 = localObject3;
                localObject8 = localObject4;
                break;
                label9830:
                j = 0;
              }
              label9835:
              localObject4 = localObject3;
            }
          }
        });
      }
    });
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/GcmPushListenerService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */