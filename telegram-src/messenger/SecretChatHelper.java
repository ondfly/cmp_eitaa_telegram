package org.telegram.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.LongSparseArray;
import android.util.SparseArray;
import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLClassStore;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.DecryptedMessage;
import org.telegram.tgnet.TLRPC.DecryptedMessageAction;
import org.telegram.tgnet.TLRPC.DecryptedMessageMedia;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.EncryptedFile;
import org.telegram.tgnet.TLRPC.EncryptedMessage;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_decryptedMessage;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionAbortKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionAcceptKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionCommitKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionDeleteMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionFlushHistory;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionNoop;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionNotifyLayer;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionReadMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionRequestKey;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionResend;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageLayer;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaContact;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaDocument_layer8;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaExternalDocument;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaGeoPoint;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageService;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_documentEncrypted;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_encryptedChatDiscarded;
import org.telegram.tgnet.TLRPC.TL_encryptedChatRequested;
import org.telegram.tgnet.TLRPC.TL_encryptedChatWaiting;
import org.telegram.tgnet.TLRPC.TL_encryptedFile;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_fileEncryptedLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_geoPoint;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedChat;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_messages_acceptEncryption;
import org.telegram.tgnet.TLRPC.TL_messages_dhConfig;
import org.telegram.tgnet.TLRPC.TL_messages_discardEncryption;
import org.telegram.tgnet.TLRPC.TL_messages_getDhConfig;
import org.telegram.tgnet.TLRPC.TL_messages_requestEncryption;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncrypted;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncryptedFile;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncryptedMultiMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendEncryptedService;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photo;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_updateEncryption;
import org.telegram.tgnet.TLRPC.TL_webPageUrlPending;
import org.telegram.tgnet.TLRPC.Update;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.messages_DhConfig;
import org.telegram.tgnet.TLRPC.messages_SentEncryptedMessage;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;

public class SecretChatHelper
{
  public static final int CURRENT_SECRET_CHAT_LAYER = 73;
  private static volatile SecretChatHelper[] Instance = new SecretChatHelper[3];
  private SparseArray<TLRPC.EncryptedChat> acceptingChats = new SparseArray();
  private int currentAccount;
  public ArrayList<TLRPC.Update> delayedEncryptedChatUpdates = new ArrayList();
  private ArrayList<Long> pendingEncMessagesToDelete = new ArrayList();
  private SparseArray<ArrayList<TL_decryptedMessageHolder>> secretHolesQueue = new SparseArray();
  private ArrayList<Integer> sendingNotifyLayer = new ArrayList();
  private boolean startingSecretChat = false;
  
  public SecretChatHelper(int paramInt)
  {
    this.currentAccount = paramInt;
  }
  
  private void applyPeerLayer(final TLRPC.EncryptedChat paramEncryptedChat, int paramInt)
  {
    int i = AndroidUtilities.getPeerLayerVersion(paramEncryptedChat.layer);
    if (paramInt <= i) {
      return;
    }
    if ((paramEncryptedChat.key_hash.length == 16) && (i >= 46)) {}
    try
    {
      byte[] arrayOfByte1 = Utilities.computeSHA256(paramEncryptedChat.auth_key, 0, paramEncryptedChat.auth_key.length);
      byte[] arrayOfByte2 = new byte[36];
      System.arraycopy(paramEncryptedChat.key_hash, 0, arrayOfByte2, 0, 16);
      System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 16, 20);
      paramEncryptedChat.key_hash = arrayOfByte2;
      MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
      paramEncryptedChat.layer = AndroidUtilities.setPeerLayerVersion(paramEncryptedChat.layer, paramInt);
      MessagesStorage.getInstance(this.currentAccount).updateEncryptedChatLayer(paramEncryptedChat);
      if (i < 73) {
        sendNotifyLayerMessage(paramEncryptedChat, null);
      }
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.encryptedChatUpdated, new Object[] { paramEncryptedChat });
        }
      });
      return;
    }
    catch (Throwable localThrowable)
    {
      for (;;)
      {
        FileLog.e(localThrowable);
      }
    }
  }
  
  private TLRPC.Message createDeleteMessage(int paramInt1, int paramInt2, int paramInt3, long paramLong, TLRPC.EncryptedChat paramEncryptedChat)
  {
    TLRPC.TL_messageService localTL_messageService = new TLRPC.TL_messageService();
    localTL_messageService.action = new TLRPC.TL_messageEncryptedAction();
    localTL_messageService.action.encryptedAction = new TLRPC.TL_decryptedMessageActionDeleteMessages();
    localTL_messageService.action.encryptedAction.random_ids.add(Long.valueOf(paramLong));
    localTL_messageService.id = paramInt1;
    localTL_messageService.local_id = paramInt1;
    localTL_messageService.from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
    localTL_messageService.unread = true;
    localTL_messageService.out = true;
    localTL_messageService.flags = 256;
    localTL_messageService.dialog_id = (paramEncryptedChat.id << 32);
    localTL_messageService.to_id = new TLRPC.TL_peerUser();
    localTL_messageService.send_state = 1;
    localTL_messageService.seq_in = paramInt3;
    localTL_messageService.seq_out = paramInt2;
    if (paramEncryptedChat.participant_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {}
    for (localTL_messageService.to_id.user_id = paramEncryptedChat.admin_id;; localTL_messageService.to_id.user_id = paramEncryptedChat.participant_id)
    {
      localTL_messageService.date = 0;
      localTL_messageService.random_id = paramLong;
      return localTL_messageService;
    }
  }
  
  private TLRPC.TL_messageService createServiceSecretMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.DecryptedMessageAction paramDecryptedMessageAction)
  {
    TLRPC.TL_messageService localTL_messageService = new TLRPC.TL_messageService();
    localTL_messageService.action = new TLRPC.TL_messageEncryptedAction();
    localTL_messageService.action.encryptedAction = paramDecryptedMessageAction;
    int i = UserConfig.getInstance(this.currentAccount).getNewMessageId();
    localTL_messageService.id = i;
    localTL_messageService.local_id = i;
    localTL_messageService.from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
    localTL_messageService.unread = true;
    localTL_messageService.out = true;
    localTL_messageService.flags = 256;
    localTL_messageService.dialog_id = (paramEncryptedChat.id << 32);
    localTL_messageService.to_id = new TLRPC.TL_peerUser();
    localTL_messageService.send_state = 1;
    if (paramEncryptedChat.participant_id == UserConfig.getInstance(this.currentAccount).getClientUserId())
    {
      localTL_messageService.to_id.user_id = paramEncryptedChat.admin_id;
      if ((!(paramDecryptedMessageAction instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages)) && (!(paramDecryptedMessageAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL))) {
        break label251;
      }
    }
    label251:
    for (localTL_messageService.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();; localTL_messageService.date = 0)
    {
      localTL_messageService.random_id = SendMessagesHelper.getInstance(this.currentAccount).getNextRandomId();
      UserConfig.getInstance(this.currentAccount).saveConfig(false);
      paramEncryptedChat = new ArrayList();
      paramEncryptedChat.add(localTL_messageService);
      MessagesStorage.getInstance(this.currentAccount).putMessages(paramEncryptedChat, false, true, true, 0);
      return localTL_messageService;
      localTL_messageService.to_id.user_id = paramEncryptedChat.participant_id;
      break;
    }
  }
  
  private boolean decryptWithMtProtoVersion(NativeByteBuffer paramNativeByteBuffer, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramInt == 1) {
      paramBoolean1 = false;
    }
    MessageKeyData localMessageKeyData = MessageKeyData.generateMessageKeyData(paramArrayOfByte1, paramArrayOfByte2, paramBoolean1, paramInt);
    Utilities.aesIgeEncryption(paramNativeByteBuffer.buffer, localMessageKeyData.aesKey, localMessageKeyData.aesIv, false, false, 24, paramNativeByteBuffer.limit() - 24);
    int k = paramNativeByteBuffer.readInt32(false);
    if (paramInt == 2)
    {
      if (paramBoolean1) {}
      for (i = 8; !Utilities.arraysEquals(paramArrayOfByte2, 0, Utilities.computeSHA256(paramArrayOfByte1, i + 88, 32, paramNativeByteBuffer.buffer, 24, paramNativeByteBuffer.buffer.limit()), 8); i = 0)
      {
        if (paramBoolean2)
        {
          Utilities.aesIgeEncryption(paramNativeByteBuffer.buffer, localMessageKeyData.aesKey, localMessageKeyData.aesIv, true, false, 24, paramNativeByteBuffer.limit() - 24);
          paramNativeByteBuffer.position(24);
        }
        return false;
      }
    }
    int j = k + 28;
    if (j >= paramNativeByteBuffer.buffer.limit() - 15)
    {
      i = j;
      if (j <= paramNativeByteBuffer.buffer.limit()) {}
    }
    else
    {
      i = paramNativeByteBuffer.buffer.limit();
    }
    paramArrayOfByte1 = Utilities.computeSHA1(paramNativeByteBuffer.buffer, 24, i);
    if (!Utilities.arraysEquals(paramArrayOfByte2, 0, paramArrayOfByte1, paramArrayOfByte1.length - 16))
    {
      if (paramBoolean2)
      {
        Utilities.aesIgeEncryption(paramNativeByteBuffer.buffer, localMessageKeyData.aesKey, localMessageKeyData.aesIv, true, false, 24, paramNativeByteBuffer.limit() - 24);
        paramNativeByteBuffer.position(24);
      }
      return false;
    }
    if ((k <= 0) || (k > paramNativeByteBuffer.limit() - 28)) {
      return false;
    }
    int i = paramNativeByteBuffer.limit() - 28 - k;
    return ((paramInt != 2) || ((i >= 12) && (i <= 1024))) && ((paramInt != 1) || (i <= 15));
  }
  
  public static SecretChatHelper getInstance(int paramInt)
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
        localObject1 = new SecretChatHelper(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (SecretChatHelper)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (SecretChatHelper)localObject1;
  }
  
  public static boolean isSecretInvisibleMessage(TLRPC.Message paramMessage)
  {
    return ((paramMessage.action instanceof TLRPC.TL_messageEncryptedAction)) && (!(paramMessage.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages)) && (!(paramMessage.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL));
  }
  
  public static boolean isSecretVisibleMessage(TLRPC.Message paramMessage)
  {
    return ((paramMessage.action instanceof TLRPC.TL_messageEncryptedAction)) && (((paramMessage.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages)) || ((paramMessage.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL)));
  }
  
  private void resendMessages(final int paramInt1, final int paramInt2, final TLRPC.EncryptedChat paramEncryptedChat)
  {
    if ((paramEncryptedChat == null) || (paramInt2 - paramInt1 < 0)) {
      return;
    }
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        int j;
        int i;
        SparseArray localSparseArray;
        final ArrayList localArrayList;
        SQLiteCursor localSQLiteCursor;
        for (;;)
        {
          try
          {
            j = paramInt1;
            i = j;
            if (paramEncryptedChat.admin_id == UserConfig.getInstance(SecretChatHelper.this.currentAccount).getClientUserId())
            {
              i = j;
              if (j % 2 == 0) {
                i = j + 1;
              }
            }
            Object localObject1 = MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT uid FROM requested_holes WHERE uid = %d AND ((seq_out_start >= %d AND %d <= seq_out_end) OR (seq_out_start >= %d AND %d <= seq_out_end))", new Object[] { Integer.valueOf(paramEncryptedChat.id), Integer.valueOf(i), Integer.valueOf(i), Integer.valueOf(paramInt2), Integer.valueOf(paramInt2) }), new Object[0]);
            boolean bool = ((SQLiteCursor)localObject1).next();
            ((SQLiteCursor)localObject1).dispose();
            if (bool) {
              return;
            }
            long l3 = paramEncryptedChat.id << 32;
            localSparseArray = new SparseArray();
            localArrayList = new ArrayList();
            j = i;
            if (j < paramInt2)
            {
              localSparseArray.put(j, null);
              j += 2;
            }
            else
            {
              localSQLiteCursor = MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT m.data, r.random_id, s.seq_in, s.seq_out, m.ttl, s.mid FROM messages_seq as s LEFT JOIN randoms as r ON r.mid = s.mid LEFT JOIN messages as m ON m.mid = s.mid WHERE m.uid = %d AND m.out = 1 AND s.seq_out >= %d AND s.seq_out <= %d ORDER BY seq_out ASC", new Object[] { Long.valueOf(l3), Integer.valueOf(i), Integer.valueOf(paramInt2) }), new Object[0]);
              if (!localSQLiteCursor.next()) {
                break;
              }
              long l2 = localSQLiteCursor.longValue(1);
              long l1 = l2;
              if (l2 == 0L) {
                l1 = Utilities.random.nextLong();
              }
              j = localSQLiteCursor.intValue(2);
              int k = localSQLiteCursor.intValue(3);
              int m = localSQLiteCursor.intValue(5);
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
              if (localNativeByteBuffer != null)
              {
                localObject1 = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                ((TLRPC.Message)localObject1).readAttachPath(localNativeByteBuffer, UserConfig.getInstance(SecretChatHelper.this.currentAccount).clientUserId);
                localNativeByteBuffer.reuse();
                ((TLRPC.Message)localObject1).random_id = l1;
                ((TLRPC.Message)localObject1).dialog_id = l3;
                ((TLRPC.Message)localObject1).seq_in = j;
                ((TLRPC.Message)localObject1).seq_out = k;
                ((TLRPC.Message)localObject1).ttl = localSQLiteCursor.intValue(4);
                localArrayList.add(localObject1);
                localSparseArray.remove(k);
              }
              else
              {
                localObject2 = SecretChatHelper.this.createDeleteMessage(m, k, j, l1, paramEncryptedChat);
              }
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
        }
        localSQLiteCursor.dispose();
        if (localSparseArray.size() != 0)
        {
          j = 0;
          while (j < localSparseArray.size())
          {
            localArrayList.add(SecretChatHelper.this.createDeleteMessage(UserConfig.getInstance(SecretChatHelper.this.currentAccount).getNewMessageId(), localSparseArray.keyAt(j), 0, Utilities.random.nextLong(), paramEncryptedChat));
            j += 1;
          }
          UserConfig.getInstance(SecretChatHelper.this.currentAccount).saveConfig(false);
        }
        Collections.sort(localArrayList, new Comparator()
        {
          public int compare(TLRPC.Message paramAnonymous2Message1, TLRPC.Message paramAnonymous2Message2)
          {
            return AndroidUtilities.compare(paramAnonymous2Message1.seq_out, paramAnonymous2Message2.seq_out);
          }
        });
        Object localObject2 = new ArrayList();
        ((ArrayList)localObject2).add(paramEncryptedChat);
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            int i = 0;
            while (i < localArrayList.size())
            {
              Object localObject = (TLRPC.Message)localArrayList.get(i);
              localObject = new MessageObject(SecretChatHelper.this.currentAccount, (TLRPC.Message)localObject, false);
              ((MessageObject)localObject).resendAsIs = true;
              SendMessagesHelper.getInstance(SecretChatHelper.this.currentAccount).retrySendMessage((MessageObject)localObject, true);
              i += 1;
            }
          }
        });
        SendMessagesHelper.getInstance(SecretChatHelper.this.currentAccount).processUnsentMessages(localArrayList, new ArrayList(), new ArrayList(), (ArrayList)localObject2);
        MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getDatabase().executeFast(String.format(Locale.US, "REPLACE INTO requested_holes VALUES(%d, %d, %d)", new Object[] { Integer.valueOf(paramEncryptedChat.id), Integer.valueOf(i), Integer.valueOf(paramInt2) })).stepThis().dispose();
      }
    });
  }
  
  private void updateMediaPaths(MessageObject paramMessageObject, TLRPC.EncryptedFile paramEncryptedFile, TLRPC.DecryptedMessage paramDecryptedMessage, String paramString)
  {
    paramString = paramMessageObject.messageOwner;
    if (paramEncryptedFile != null)
    {
      if ((!(paramString.media instanceof TLRPC.TL_messageMediaPhoto)) || (paramString.media.photo == null)) {
        break label309;
      }
      paramMessageObject = (TLRPC.PhotoSize)paramString.media.photo.sizes.get(paramString.media.photo.sizes.size() - 1);
      localObject = paramMessageObject.location.volume_id + "_" + paramMessageObject.location.local_id;
      paramMessageObject.location = new TLRPC.TL_fileEncryptedLocation();
      paramMessageObject.location.key = paramDecryptedMessage.media.key;
      paramMessageObject.location.iv = paramDecryptedMessage.media.iv;
      paramMessageObject.location.dc_id = paramEncryptedFile.dc_id;
      paramMessageObject.location.volume_id = paramEncryptedFile.id;
      paramMessageObject.location.secret = paramEncryptedFile.access_hash;
      paramMessageObject.location.local_id = paramEncryptedFile.key_fingerprint;
      paramEncryptedFile = paramMessageObject.location.volume_id + "_" + paramMessageObject.location.local_id;
      new File(FileLoader.getDirectory(4), (String)localObject + ".jpg").renameTo(FileLoader.getPathToAttach(paramMessageObject));
      ImageLoader.getInstance().replaceImageInCache((String)localObject, paramEncryptedFile, paramMessageObject.location, true);
      paramMessageObject = new ArrayList();
      paramMessageObject.add(paramString);
      MessagesStorage.getInstance(this.currentAccount).putMessages(paramMessageObject, false, true, false, 0);
    }
    label309:
    while ((!(paramString.media instanceof TLRPC.TL_messageMediaDocument)) || (paramString.media.document == null)) {
      return;
    }
    Object localObject = paramString.media.document;
    paramString.media.document = new TLRPC.TL_documentEncrypted();
    paramString.media.document.id = paramEncryptedFile.id;
    paramString.media.document.access_hash = paramEncryptedFile.access_hash;
    paramString.media.document.date = ((TLRPC.Document)localObject).date;
    paramString.media.document.attributes = ((TLRPC.Document)localObject).attributes;
    paramString.media.document.mime_type = ((TLRPC.Document)localObject).mime_type;
    paramString.media.document.size = paramEncryptedFile.size;
    paramString.media.document.key = paramDecryptedMessage.media.key;
    paramString.media.document.iv = paramDecryptedMessage.media.iv;
    paramString.media.document.thumb = ((TLRPC.Document)localObject).thumb;
    paramString.media.document.dc_id = paramEncryptedFile.dc_id;
    if ((paramString.attachPath != null) && (paramString.attachPath.startsWith(FileLoader.getDirectory(4).getAbsolutePath())) && (new File(paramString.attachPath).renameTo(FileLoader.getPathToAttach(paramString.media.document))))
    {
      paramMessageObject.mediaExists = paramMessageObject.attachPathExists;
      paramMessageObject.attachPathExists = false;
      paramString.attachPath = "";
    }
    paramMessageObject = new ArrayList();
    paramMessageObject.add(paramString);
    MessagesStorage.getInstance(this.currentAccount).putMessages(paramMessageObject, false, true, false, 0);
  }
  
  public void acceptSecretChat(final TLRPC.EncryptedChat paramEncryptedChat)
  {
    if (this.acceptingChats.get(paramEncryptedChat.id) != null) {
      return;
    }
    this.acceptingChats.put(paramEncryptedChat.id, paramEncryptedChat);
    TLRPC.TL_messages_getDhConfig localTL_messages_getDhConfig = new TLRPC.TL_messages_getDhConfig();
    localTL_messages_getDhConfig.random_length = 256;
    localTL_messages_getDhConfig.version = MessagesStorage.getInstance(this.currentAccount).getLastSecretVersion();
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
              SecretChatHelper.this.acceptingChats.remove(paramEncryptedChat.id);
              SecretChatHelper.this.declineSecretChat(paramEncryptedChat.id);
              return;
            }
            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).setSecretPBytes(paramAnonymousTL_error.p);
            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).setSecretG(paramAnonymousTL_error.g);
            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).setLastSecretVersion(paramAnonymousTL_error.version);
            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).saveSecretParams(MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getLastSecretVersion(), MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getSecretG(), MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getSecretPBytes());
          }
          byte[] arrayOfByte = new byte['Ā'];
          int i = 0;
          while (i < 256)
          {
            arrayOfByte[i] = ((byte)((byte)(int)(Utilities.random.nextDouble() * 256.0D) ^ paramAnonymousTL_error.random[i]));
            i += 1;
          }
          paramEncryptedChat.a_or_b = arrayOfByte;
          paramEncryptedChat.seq_in = -1;
          paramEncryptedChat.seq_out = 0;
          Object localObject = new BigInteger(1, MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getSecretPBytes());
          paramAnonymousTLObject = BigInteger.valueOf(MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getSecretG()).modPow(new BigInteger(1, arrayOfByte), (BigInteger)localObject);
          BigInteger localBigInteger = new BigInteger(1, paramEncryptedChat.g_a);
          if (!Utilities.isGoodGaAndGb(localBigInteger, (BigInteger)localObject))
          {
            SecretChatHelper.this.acceptingChats.remove(paramEncryptedChat.id);
            SecretChatHelper.this.declineSecretChat(paramEncryptedChat.id);
            return;
          }
          paramAnonymousTLObject = paramAnonymousTLObject.toByteArray();
          paramAnonymousTL_error = paramAnonymousTLObject;
          if (paramAnonymousTLObject.length > 256)
          {
            paramAnonymousTL_error = new byte['Ā'];
            System.arraycopy(paramAnonymousTLObject, 1, paramAnonymousTL_error, 0, 256);
          }
          arrayOfByte = localBigInteger.modPow(new BigInteger(1, arrayOfByte), (BigInteger)localObject).toByteArray();
          if (arrayOfByte.length > 256)
          {
            paramAnonymousTLObject = new byte['Ā'];
            System.arraycopy(arrayOfByte, arrayOfByte.length - 256, paramAnonymousTLObject, 0, 256);
          }
          for (;;)
          {
            localObject = Utilities.computeSHA1(paramAnonymousTLObject);
            arrayOfByte = new byte[8];
            System.arraycopy(localObject, localObject.length - 8, arrayOfByte, 0, 8);
            paramEncryptedChat.auth_key = paramAnonymousTLObject;
            paramEncryptedChat.key_create_date = ConnectionsManager.getInstance(SecretChatHelper.this.currentAccount).getCurrentTime();
            paramAnonymousTLObject = new TLRPC.TL_messages_acceptEncryption();
            paramAnonymousTLObject.g_b = paramAnonymousTL_error;
            paramAnonymousTLObject.peer = new TLRPC.TL_inputEncryptedChat();
            paramAnonymousTLObject.peer.chat_id = paramEncryptedChat.id;
            paramAnonymousTLObject.peer.access_hash = paramEncryptedChat.access_hash;
            paramAnonymousTLObject.key_fingerprint = Utilities.bytesToLong(arrayOfByte);
            ConnectionsManager.getInstance(SecretChatHelper.this.currentAccount).sendRequest(paramAnonymousTLObject, new RequestDelegate()
            {
              public void run(final TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
              {
                SecretChatHelper.this.acceptingChats.remove(SecretChatHelper.13.this.val$encryptedChat.id);
                if (paramAnonymous2TL_error == null)
                {
                  paramAnonymous2TLObject = (TLRPC.EncryptedChat)paramAnonymous2TLObject;
                  paramAnonymous2TLObject.auth_key = SecretChatHelper.13.this.val$encryptedChat.auth_key;
                  paramAnonymous2TLObject.user_id = SecretChatHelper.13.this.val$encryptedChat.user_id;
                  paramAnonymous2TLObject.seq_in = SecretChatHelper.13.this.val$encryptedChat.seq_in;
                  paramAnonymous2TLObject.seq_out = SecretChatHelper.13.this.val$encryptedChat.seq_out;
                  paramAnonymous2TLObject.key_create_date = SecretChatHelper.13.this.val$encryptedChat.key_create_date;
                  paramAnonymous2TLObject.key_use_count_in = SecretChatHelper.13.this.val$encryptedChat.key_use_count_in;
                  paramAnonymous2TLObject.key_use_count_out = SecretChatHelper.13.this.val$encryptedChat.key_use_count_out;
                  MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).updateEncryptedChat(paramAnonymous2TLObject);
                  MessagesController.getInstance(SecretChatHelper.this.currentAccount).putEncryptedChat(paramAnonymous2TLObject, false);
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.encryptedChatUpdated, new Object[] { paramAnonymous2TLObject });
                      SecretChatHelper.this.sendNotifyLayerMessage(paramAnonymous2TLObject, null);
                    }
                  });
                }
              }
            });
            return;
            paramAnonymousTLObject = arrayOfByte;
            if (arrayOfByte.length < 256)
            {
              paramAnonymousTLObject = new byte['Ā'];
              System.arraycopy(arrayOfByte, 0, paramAnonymousTLObject, 256 - arrayOfByte.length, arrayOfByte.length);
              i = 0;
              while (i < 256 - arrayOfByte.length)
              {
                arrayOfByte[i] = 0;
                i += 1;
              }
            }
          }
        }
        SecretChatHelper.this.acceptingChats.remove(paramEncryptedChat.id);
      }
    });
  }
  
  public void checkSecretHoles(TLRPC.EncryptedChat paramEncryptedChat, ArrayList<TLRPC.Message> paramArrayList)
  {
    ArrayList localArrayList = (ArrayList)this.secretHolesQueue.get(paramEncryptedChat.id);
    if (localArrayList == null) {}
    int j;
    do
    {
      return;
      Collections.sort(localArrayList, new Comparator()
      {
        public int compare(SecretChatHelper.TL_decryptedMessageHolder paramAnonymousTL_decryptedMessageHolder1, SecretChatHelper.TL_decryptedMessageHolder paramAnonymousTL_decryptedMessageHolder2)
        {
          if (paramAnonymousTL_decryptedMessageHolder1.layer.out_seq_no > paramAnonymousTL_decryptedMessageHolder2.layer.out_seq_no) {
            return 1;
          }
          if (paramAnonymousTL_decryptedMessageHolder1.layer.out_seq_no < paramAnonymousTL_decryptedMessageHolder2.layer.out_seq_no) {
            return -1;
          }
          return 0;
        }
      });
      j = 0;
      for (int i = 0; localArrayList.size() > 0; i = i - 1 + 1)
      {
        Object localObject = (TL_decryptedMessageHolder)localArrayList.get(i);
        if ((((TL_decryptedMessageHolder)localObject).layer.out_seq_no != paramEncryptedChat.seq_in) && (paramEncryptedChat.seq_in != ((TL_decryptedMessageHolder)localObject).layer.out_seq_no - 2)) {
          break;
        }
        applyPeerLayer(paramEncryptedChat, ((TL_decryptedMessageHolder)localObject).layer.layer);
        paramEncryptedChat.seq_in = ((TL_decryptedMessageHolder)localObject).layer.out_seq_no;
        paramEncryptedChat.in_seq_no = ((TL_decryptedMessageHolder)localObject).layer.in_seq_no;
        localArrayList.remove(i);
        j = 1;
        if (((TL_decryptedMessageHolder)localObject).decryptedWithVersion == 2) {
          paramEncryptedChat.mtproto_seq = Math.min(paramEncryptedChat.mtproto_seq, paramEncryptedChat.seq_in);
        }
        localObject = processDecryptedObject(paramEncryptedChat, ((TL_decryptedMessageHolder)localObject).file, ((TL_decryptedMessageHolder)localObject).date, ((TL_decryptedMessageHolder)localObject).layer.message, ((TL_decryptedMessageHolder)localObject).new_key_used);
        if (localObject != null) {
          paramArrayList.add(localObject);
        }
      }
      if (localArrayList.isEmpty()) {
        this.secretHolesQueue.remove(paramEncryptedChat.id);
      }
    } while (j == 0);
    MessagesStorage.getInstance(this.currentAccount).updateEncryptedChatSeq(paramEncryptedChat, true);
  }
  
  public void cleanup()
  {
    this.sendingNotifyLayer.clear();
    this.acceptingChats.clear();
    this.secretHolesQueue.clear();
    this.delayedEncryptedChatUpdates.clear();
    this.pendingEncMessagesToDelete.clear();
    this.startingSecretChat = false;
  }
  
  public void declineSecretChat(int paramInt)
  {
    TLRPC.TL_messages_discardEncryption localTL_messages_discardEncryption = new TLRPC.TL_messages_discardEncryption();
    localTL_messages_discardEncryption.chat_id = paramInt;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_discardEncryption, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  protected ArrayList<TLRPC.Message> decryptMessage(final TLRPC.EncryptedMessage paramEncryptedMessage)
  {
    TLRPC.EncryptedChat localEncryptedChat = MessagesController.getInstance(this.currentAccount).getEncryptedChatDB(paramEncryptedMessage.chat_id, true);
    if ((localEncryptedChat == null) || ((localEncryptedChat instanceof TLRPC.TL_encryptedChatDiscarded))) {
      return null;
    }
    for (;;)
    {
      Object localObject3;
      long l;
      Object localObject2;
      boolean bool2;
      boolean bool1;
      int i;
      int j;
      try
      {
        localObject3 = new NativeByteBuffer(paramEncryptedMessage.bytes.length);
        ((NativeByteBuffer)localObject3).writeBytes(paramEncryptedMessage.bytes);
        ((NativeByteBuffer)localObject3).position(0);
        l = ((NativeByteBuffer)localObject3).readInt64(false);
        localObject2 = null;
        bool2 = false;
        boolean bool3;
        if (localEncryptedChat.key_fingerprint == l)
        {
          localObject1 = localEncryptedChat.auth_key;
          bool1 = bool2;
          if (AndroidUtilities.getPeerLayerVersion(localEncryptedChat.layer) < 73) {
            break label1034;
          }
          i = 2;
          j = i;
          if (localObject1 == null) {
            break label997;
          }
          localObject2 = ((NativeByteBuffer)localObject3).readData(16, false);
          if (localEncryptedChat.admin_id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            break label1039;
          }
          bool2 = true;
          boolean bool4 = true;
          bool3 = bool4;
          if (j == 2)
          {
            bool3 = bool4;
            if (localEncryptedChat.mtproto_seq != 0) {
              bool3 = false;
            }
          }
          if (decryptWithMtProtoVersion((NativeByteBuffer)localObject3, (byte[])localObject1, (byte[])localObject2, i, bool2, bool3)) {
            continue;
          }
          if (i == 2)
          {
            j = 1;
            if (!bool3) {
              break label1032;
            }
            if (decryptWithMtProtoVersion((NativeByteBuffer)localObject3, (byte[])localObject1, (byte[])localObject2, 1, bool2, false)) {
              continue;
            }
            break label1032;
          }
        }
        else
        {
          localObject1 = localObject2;
          bool1 = bool2;
          if (localEncryptedChat.future_key_fingerprint == 0L) {
            continue;
          }
          localObject1 = localObject2;
          bool1 = bool2;
          if (localEncryptedChat.future_key_fingerprint != l) {
            continue;
          }
          localObject1 = localEncryptedChat.future_auth_key;
          bool1 = true;
          continue;
        }
        j = 2;
        if (!decryptWithMtProtoVersion((NativeByteBuffer)localObject3, (byte[])localObject1, (byte[])localObject2, 2, bool2, bool3)) {
          return null;
        }
        localObject2 = TLClassStore.Instance().TLdeserialize((NativeByteBuffer)localObject3, ((NativeByteBuffer)localObject3).readInt32(false), false);
        ((NativeByteBuffer)localObject3).reuse();
        if ((!bool1) && (AndroidUtilities.getPeerLayerVersion(localEncryptedChat.layer) >= 20)) {
          localEncryptedChat.key_use_count_in = ((short)(localEncryptedChat.key_use_count_in + 1));
        }
        if (!(localObject2 instanceof TLRPC.TL_decryptedMessageLayer)) {
          break label968;
        }
        localObject3 = (TLRPC.TL_decryptedMessageLayer)localObject2;
        if ((localEncryptedChat.seq_in == 0) && (localEncryptedChat.seq_out == 0))
        {
          if (localEncryptedChat.admin_id == UserConfig.getInstance(this.currentAccount).getClientUserId())
          {
            localEncryptedChat.seq_out = 1;
            localEncryptedChat.seq_in = -2;
          }
        }
        else
        {
          if (((TLRPC.TL_decryptedMessageLayer)localObject3).random_bytes.length >= 15) {
            break label456;
          }
          if (!BuildVars.LOGS_ENABLED) {
            break;
          }
          FileLog.e("got random bytes less than needed");
          break;
        }
        localEncryptedChat.seq_in = -1;
        continue;
        if (!BuildVars.LOGS_ENABLED) {
          break label544;
        }
      }
      catch (Exception paramEncryptedMessage)
      {
        FileLog.e(paramEncryptedMessage);
        return null;
      }
      label456:
      FileLog.d("current chat in_seq = " + localEncryptedChat.seq_in + " out_seq = " + localEncryptedChat.seq_out);
      FileLog.d("got message with in_seq = " + ((TLRPC.TL_decryptedMessageLayer)localObject3).in_seq_no + " out_seq = " + ((TLRPC.TL_decryptedMessageLayer)localObject3).out_seq_no);
      label544:
      if (((TLRPC.TL_decryptedMessageLayer)localObject3).out_seq_no <= localEncryptedChat.seq_in) {
        return null;
      }
      if ((j == 1) && (localEncryptedChat.mtproto_seq != 0) && (((TLRPC.TL_decryptedMessageLayer)localObject3).out_seq_no >= localEncryptedChat.mtproto_seq)) {
        return null;
      }
      if (localEncryptedChat.seq_in != ((TLRPC.TL_decryptedMessageLayer)localObject3).out_seq_no - 2)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("got hole");
        }
        localObject2 = (ArrayList)this.secretHolesQueue.get(localEncryptedChat.id);
        localObject1 = localObject2;
        if (localObject2 == null)
        {
          localObject1 = new ArrayList();
          this.secretHolesQueue.put(localEncryptedChat.id, localObject1);
        }
        if (((ArrayList)localObject1).size() >= 4)
        {
          this.secretHolesQueue.remove(localEncryptedChat.id);
          paramEncryptedMessage = new TLRPC.TL_encryptedChatDiscarded();
          paramEncryptedMessage.id = localEncryptedChat.id;
          paramEncryptedMessage.user_id = localEncryptedChat.user_id;
          paramEncryptedMessage.auth_key = localEncryptedChat.auth_key;
          paramEncryptedMessage.key_create_date = localEncryptedChat.key_create_date;
          paramEncryptedMessage.key_use_count_in = localEncryptedChat.key_use_count_in;
          paramEncryptedMessage.key_use_count_out = localEncryptedChat.key_use_count_out;
          paramEncryptedMessage.seq_in = localEncryptedChat.seq_in;
          paramEncryptedMessage.seq_out = localEncryptedChat.seq_out;
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.getInstance(SecretChatHelper.this.currentAccount).putEncryptedChat(paramEncryptedMessage, false);
              MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).updateEncryptedChat(paramEncryptedMessage);
              NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.encryptedChatUpdated, new Object[] { paramEncryptedMessage });
            }
          });
          declineSecretChat(localEncryptedChat.id);
          return null;
        }
        localObject2 = new TL_decryptedMessageHolder();
        ((TL_decryptedMessageHolder)localObject2).layer = ((TLRPC.TL_decryptedMessageLayer)localObject3);
        ((TL_decryptedMessageHolder)localObject2).file = paramEncryptedMessage.file;
        ((TL_decryptedMessageHolder)localObject2).date = paramEncryptedMessage.date;
        ((TL_decryptedMessageHolder)localObject2).new_key_used = bool1;
        ((TL_decryptedMessageHolder)localObject2).decryptedWithVersion = j;
        ((ArrayList)localObject1).add(localObject2);
        return null;
      }
      if (j == 2) {
        localEncryptedChat.mtproto_seq = Math.min(localEncryptedChat.mtproto_seq, localEncryptedChat.seq_in);
      }
      applyPeerLayer(localEncryptedChat, ((TLRPC.TL_decryptedMessageLayer)localObject3).layer);
      localEncryptedChat.seq_in = ((TLRPC.TL_decryptedMessageLayer)localObject3).out_seq_no;
      localEncryptedChat.in_seq_no = ((TLRPC.TL_decryptedMessageLayer)localObject3).in_seq_no;
      MessagesStorage.getInstance(this.currentAccount).updateEncryptedChatSeq(localEncryptedChat, true);
      Object localObject1 = ((TLRPC.TL_decryptedMessageLayer)localObject3).message;
      label968:
      do
      {
        localObject2 = new ArrayList();
        paramEncryptedMessage = processDecryptedObject(localEncryptedChat, paramEncryptedMessage.file, paramEncryptedMessage.date, (TLObject)localObject1, bool1);
        if (paramEncryptedMessage != null) {
          ((ArrayList)localObject2).add(paramEncryptedMessage);
        }
        checkSecretHoles(localEncryptedChat, (ArrayList)localObject2);
        return (ArrayList<TLRPC.Message>)localObject2;
        if (!(localObject2 instanceof TLRPC.TL_decryptedMessageService)) {
          break;
        }
        localObject1 = localObject2;
      } while ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionNotifyLayer));
      break label1047;
      label997:
      ((NativeByteBuffer)localObject3).reuse();
      if (BuildVars.LOGS_ENABLED)
      {
        FileLog.e(String.format("fingerprint mismatch %x", new Object[] { Long.valueOf(l) }));
        continue;
        label1032:
        return null;
        label1034:
        i = 1;
        continue;
        label1039:
        bool2 = false;
      }
    }
    return null;
    label1047:
    return null;
  }
  
  protected void performSendEncryptedRequest(final TLRPC.DecryptedMessage paramDecryptedMessage, final TLRPC.Message paramMessage, final TLRPC.EncryptedChat paramEncryptedChat, final TLRPC.InputEncryptedFile paramInputEncryptedFile, final String paramString, final MessageObject paramMessageObject)
  {
    if ((paramDecryptedMessage == null) || (paramEncryptedChat.auth_key == null) || ((paramEncryptedChat instanceof TLRPC.TL_encryptedChatRequested)) || ((paramEncryptedChat instanceof TLRPC.TL_encryptedChatWaiting))) {
      return;
    }
    SendMessagesHelper.getInstance(this.currentAccount).putToSendingMessages(paramMessage);
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          Object localObject3;
          byte[] arrayOfByte1;
          try
          {
            Object localObject1 = new TLRPC.TL_decryptedMessageLayer();
            ((TLRPC.TL_decryptedMessageLayer)localObject1).layer = Math.min(Math.max(46, AndroidUtilities.getMyLayerVersion(paramEncryptedChat.layer)), Math.max(46, AndroidUtilities.getPeerLayerVersion(paramEncryptedChat.layer)));
            ((TLRPC.TL_decryptedMessageLayer)localObject1).message = paramDecryptedMessage;
            ((TLRPC.TL_decryptedMessageLayer)localObject1).random_bytes = new byte[15];
            Utilities.random.nextBytes(((TLRPC.TL_decryptedMessageLayer)localObject1).random_bytes);
            if (AndroidUtilities.getPeerLayerVersion(paramEncryptedChat.layer) < 73) {
              break label1189;
            }
            j = 2;
            if ((paramEncryptedChat.seq_in == 0) && (paramEncryptedChat.seq_out == 0))
            {
              if (paramEncryptedChat.admin_id == UserConfig.getInstance(SecretChatHelper.this.currentAccount).getClientUserId())
              {
                paramEncryptedChat.seq_out = 1;
                paramEncryptedChat.seq_in = -2;
              }
            }
            else
            {
              if ((paramMessage.seq_in != 0) || (paramMessage.seq_out != 0)) {
                break label978;
              }
              if (paramEncryptedChat.seq_in <= 0) {
                break label965;
              }
              i = paramEncryptedChat.seq_in;
              ((TLRPC.TL_decryptedMessageLayer)localObject1).in_seq_no = i;
              ((TLRPC.TL_decryptedMessageLayer)localObject1).out_seq_no = paramEncryptedChat.seq_out;
              localObject3 = paramEncryptedChat;
              ((TLRPC.EncryptedChat)localObject3).seq_out += 2;
              if (AndroidUtilities.getPeerLayerVersion(paramEncryptedChat.layer) >= 20)
              {
                if (paramEncryptedChat.key_create_date == 0) {
                  paramEncryptedChat.key_create_date = ConnectionsManager.getInstance(SecretChatHelper.this.currentAccount).getCurrentTime();
                }
                localObject3 = paramEncryptedChat;
                ((TLRPC.EncryptedChat)localObject3).key_use_count_out = ((short)(((TLRPC.EncryptedChat)localObject3).key_use_count_out + 1));
                if (((paramEncryptedChat.key_use_count_out >= 100) || (paramEncryptedChat.key_create_date < ConnectionsManager.getInstance(SecretChatHelper.this.currentAccount).getCurrentTime() - 604800)) && (paramEncryptedChat.exchange_id == 0L) && (paramEncryptedChat.future_key_fingerprint == 0L)) {
                  SecretChatHelper.this.requestNewSecretChatKey(paramEncryptedChat);
                }
              }
              MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).updateEncryptedChatSeq(paramEncryptedChat, false);
              if (paramMessage != null)
              {
                paramMessage.seq_in = ((TLRPC.TL_decryptedMessageLayer)localObject1).in_seq_no;
                paramMessage.seq_out = ((TLRPC.TL_decryptedMessageLayer)localObject1).out_seq_no;
                MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).setMessageSeq(paramMessage.id, paramMessage.seq_in, paramMessage.seq_out);
              }
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d(paramDecryptedMessage + " send message with in_seq = " + ((TLRPC.TL_decryptedMessageLayer)localObject1).in_seq_no + " out_seq = " + ((TLRPC.TL_decryptedMessageLayer)localObject1).out_seq_no);
              }
              i = ((TLObject)localObject1).getObjectSize();
              localObject3 = new NativeByteBuffer(i + 4);
              ((NativeByteBuffer)localObject3).writeInt32(i);
              ((TLObject)localObject1).serializeToStream((AbstractSerializedData)localObject3);
              int m = ((NativeByteBuffer)localObject3).length();
              if (m % 16 == 0) {
                break label1194;
              }
              i = 16 - m % 16;
              int k = i;
              if (j == 2) {
                k = i + (Utilities.random.nextInt(3) + 2) * 16;
              }
              localObject1 = new NativeByteBuffer(m + k);
              ((NativeByteBuffer)localObject3).position(0);
              ((NativeByteBuffer)localObject1).writeBytes((NativeByteBuffer)localObject3);
              if (k != 0)
              {
                arrayOfByte1 = new byte[k];
                Utilities.random.nextBytes(arrayOfByte1);
                ((NativeByteBuffer)localObject1).writeBytes(arrayOfByte1);
              }
              arrayOfByte1 = new byte[16];
              if ((j != 2) || (paramEncryptedChat.admin_id == UserConfig.getInstance(SecretChatHelper.this.currentAccount).getClientUserId())) {
                break label1199;
              }
              bool = true;
              if (j != 2) {
                break label1005;
              }
              arrayOfByte2 = paramEncryptedChat.auth_key;
              if (!bool) {
                break label1205;
              }
              i = 8;
              System.arraycopy(Utilities.computeSHA256(arrayOfByte2, i + 88, 32, ((NativeByteBuffer)localObject1).buffer, 0, ((NativeByteBuffer)localObject1).buffer.limit()), 8, arrayOfByte1, 0, 16);
              ((NativeByteBuffer)localObject3).reuse();
              localObject3 = MessageKeyData.generateMessageKeyData(paramEncryptedChat.auth_key, arrayOfByte1, bool, j);
              Utilities.aesIgeEncryption(((NativeByteBuffer)localObject1).buffer, ((MessageKeyData)localObject3).aesKey, ((MessageKeyData)localObject3).aesIv, true, false, 0, ((NativeByteBuffer)localObject1).limit());
              localObject3 = new NativeByteBuffer(arrayOfByte1.length + 8 + ((NativeByteBuffer)localObject1).length());
              ((NativeByteBuffer)localObject1).position(0);
              ((NativeByteBuffer)localObject3).writeInt64(paramEncryptedChat.key_fingerprint);
              ((NativeByteBuffer)localObject3).writeBytes(arrayOfByte1);
              ((NativeByteBuffer)localObject3).writeBytes((NativeByteBuffer)localObject1);
              ((NativeByteBuffer)localObject1).reuse();
              ((NativeByteBuffer)localObject3).position(0);
              if (paramInputEncryptedFile != null) {
                break label1107;
              }
              if (!(paramDecryptedMessage instanceof TLRPC.TL_decryptedMessageService)) {
                break label1034;
              }
              localObject1 = new TLRPC.TL_messages_sendEncryptedService();
              ((TLRPC.TL_messages_sendEncryptedService)localObject1).data = ((NativeByteBuffer)localObject3);
              ((TLRPC.TL_messages_sendEncryptedService)localObject1).random_id = paramDecryptedMessage.random_id;
              ((TLRPC.TL_messages_sendEncryptedService)localObject1).peer = new TLRPC.TL_inputEncryptedChat();
              ((TLRPC.TL_messages_sendEncryptedService)localObject1).peer.chat_id = paramEncryptedChat.id;
              ((TLRPC.TL_messages_sendEncryptedService)localObject1).peer.access_hash = paramEncryptedChat.access_hash;
              ConnectionsManager.getInstance(SecretChatHelper.this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
              {
                public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
                {
                  Object localObject2;
                  Object localObject1;
                  if ((paramAnonymous2TL_error == null) && ((SecretChatHelper.4.this.val$req.action instanceof TLRPC.TL_decryptedMessageActionNotifyLayer)))
                  {
                    localObject2 = MessagesController.getInstance(SecretChatHelper.this.currentAccount).getEncryptedChat(Integer.valueOf(SecretChatHelper.4.this.val$chat.id));
                    localObject1 = localObject2;
                    if (localObject2 == null) {
                      localObject1 = SecretChatHelper.4.this.val$chat;
                    }
                    if (((TLRPC.EncryptedChat)localObject1).key_hash == null) {
                      ((TLRPC.EncryptedChat)localObject1).key_hash = AndroidUtilities.calcAuthKeyHash(((TLRPC.EncryptedChat)localObject1).auth_key);
                    }
                    if ((AndroidUtilities.getPeerLayerVersion(((TLRPC.EncryptedChat)localObject1).layer) < 46) || (((TLRPC.EncryptedChat)localObject1).key_hash.length != 16)) {}
                  }
                  try
                  {
                    localObject2 = Utilities.computeSHA256(SecretChatHelper.4.this.val$chat.auth_key, 0, SecretChatHelper.4.this.val$chat.auth_key.length);
                    byte[] arrayOfByte = new byte[36];
                    System.arraycopy(SecretChatHelper.4.this.val$chat.key_hash, 0, arrayOfByte, 0, 16);
                    System.arraycopy(localObject2, 0, arrayOfByte, 16, 20);
                    ((TLRPC.EncryptedChat)localObject1).key_hash = arrayOfByte;
                    MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).updateEncryptedChat((TLRPC.EncryptedChat)localObject1);
                    SecretChatHelper.this.sendingNotifyLayer.remove(Integer.valueOf(((TLRPC.EncryptedChat)localObject1).id));
                    ((TLRPC.EncryptedChat)localObject1).layer = AndroidUtilities.setMyLayerVersion(((TLRPC.EncryptedChat)localObject1).layer, 73);
                    MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).updateEncryptedChatLayer((TLRPC.EncryptedChat)localObject1);
                    if (SecretChatHelper.4.this.val$newMsgObj != null)
                    {
                      if (paramAnonymous2TL_error == null)
                      {
                        paramAnonymous2TL_error = SecretChatHelper.4.this.val$newMsgObj.attachPath;
                        paramAnonymous2TLObject = (TLRPC.messages_SentEncryptedMessage)paramAnonymous2TLObject;
                        if (SecretChatHelper.isSecretVisibleMessage(SecretChatHelper.4.this.val$newMsgObj)) {
                          SecretChatHelper.4.this.val$newMsgObj.date = paramAnonymous2TLObject.date;
                        }
                        if ((SecretChatHelper.4.this.val$newMsg != null) && ((paramAnonymous2TLObject.file instanceof TLRPC.TL_encryptedFile))) {
                          SecretChatHelper.this.updateMediaPaths(SecretChatHelper.4.this.val$newMsg, paramAnonymous2TLObject.file, SecretChatHelper.4.this.val$req, SecretChatHelper.4.this.val$originalPath);
                        }
                        MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                        {
                          public void run()
                          {
                            if (SecretChatHelper.isSecretInvisibleMessage(SecretChatHelper.4.this.val$newMsgObj)) {
                              paramAnonymous2TLObject.date = 0;
                            }
                            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).updateMessageStateAndId(SecretChatHelper.4.this.val$newMsgObj.random_id, Integer.valueOf(SecretChatHelper.4.this.val$newMsgObj.id), SecretChatHelper.4.this.val$newMsgObj.id, paramAnonymous2TLObject.date, false, 0);
                            AndroidUtilities.runOnUIThread(new Runnable()
                            {
                              public void run()
                              {
                                SecretChatHelper.4.this.val$newMsgObj.send_state = 0;
                                NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, new Object[] { Integer.valueOf(SecretChatHelper.4.this.val$newMsgObj.id), Integer.valueOf(SecretChatHelper.4.this.val$newMsgObj.id), SecretChatHelper.4.this.val$newMsgObj, Long.valueOf(SecretChatHelper.4.this.val$newMsgObj.dialog_id) });
                                SendMessagesHelper.getInstance(SecretChatHelper.this.currentAccount).processSentMessage(SecretChatHelper.4.this.val$newMsgObj.id);
                                if ((MessageObject.isVideoMessage(SecretChatHelper.4.this.val$newMsgObj)) || (MessageObject.isNewGifMessage(SecretChatHelper.4.this.val$newMsgObj)) || (MessageObject.isRoundVideoMessage(SecretChatHelper.4.this.val$newMsgObj))) {
                                  SendMessagesHelper.getInstance(SecretChatHelper.this.currentAccount).stopVideoService(SecretChatHelper.4.1.1.this.val$attachPath);
                                }
                                SendMessagesHelper.getInstance(SecretChatHelper.this.currentAccount).removeFromSendingMessages(SecretChatHelper.4.this.val$newMsgObj.id);
                              }
                            });
                          }
                        });
                      }
                    }
                    else {
                      return;
                    }
                  }
                  catch (Throwable localThrowable)
                  {
                    for (;;)
                    {
                      FileLog.e(localThrowable);
                    }
                    MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).markMessageAsSendError(SecretChatHelper.4.this.val$newMsgObj);
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        SecretChatHelper.4.this.val$newMsgObj.send_state = 2;
                        NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.messageSendError, new Object[] { Integer.valueOf(SecretChatHelper.4.this.val$newMsgObj.id) });
                        SendMessagesHelper.getInstance(SecretChatHelper.this.currentAccount).processSentMessage(SecretChatHelper.4.this.val$newMsgObj.id);
                        if ((MessageObject.isVideoMessage(SecretChatHelper.4.this.val$newMsgObj)) || (MessageObject.isNewGifMessage(SecretChatHelper.4.this.val$newMsgObj)) || (MessageObject.isRoundVideoMessage(SecretChatHelper.4.this.val$newMsgObj))) {
                          SendMessagesHelper.getInstance(SecretChatHelper.this.currentAccount).stopVideoService(SecretChatHelper.4.this.val$newMsgObj.attachPath);
                        }
                        SendMessagesHelper.getInstance(SecretChatHelper.this.currentAccount).removeFromSendingMessages(SecretChatHelper.4.this.val$newMsgObj.id);
                      }
                    });
                  }
                }
              }, 64);
              return;
            }
            paramEncryptedChat.seq_in = -1;
            continue;
            i = paramEncryptedChat.seq_in + 2;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          label965:
          continue;
          label978:
          localException.in_seq_no = paramMessage.seq_in;
          localException.out_seq_no = paramMessage.seq_out;
          continue;
          label1005:
          byte[] arrayOfByte2 = Utilities.computeSHA1(((NativeByteBuffer)localObject3).buffer);
          System.arraycopy(arrayOfByte2, arrayOfByte2.length - 16, arrayOfByte1, 0, 16);
          continue;
          label1034:
          Object localObject2 = new TLRPC.TL_messages_sendEncrypted();
          ((TLRPC.TL_messages_sendEncrypted)localObject2).data = ((NativeByteBuffer)localObject3);
          ((TLRPC.TL_messages_sendEncrypted)localObject2).random_id = paramDecryptedMessage.random_id;
          ((TLRPC.TL_messages_sendEncrypted)localObject2).peer = new TLRPC.TL_inputEncryptedChat();
          ((TLRPC.TL_messages_sendEncrypted)localObject2).peer.chat_id = paramEncryptedChat.id;
          ((TLRPC.TL_messages_sendEncrypted)localObject2).peer.access_hash = paramEncryptedChat.access_hash;
          continue;
          label1107:
          localObject2 = new TLRPC.TL_messages_sendEncryptedFile();
          ((TLRPC.TL_messages_sendEncryptedFile)localObject2).data = ((NativeByteBuffer)localObject3);
          ((TLRPC.TL_messages_sendEncryptedFile)localObject2).random_id = paramDecryptedMessage.random_id;
          ((TLRPC.TL_messages_sendEncryptedFile)localObject2).peer = new TLRPC.TL_inputEncryptedChat();
          ((TLRPC.TL_messages_sendEncryptedFile)localObject2).peer.chat_id = paramEncryptedChat.id;
          ((TLRPC.TL_messages_sendEncryptedFile)localObject2).peer.access_hash = paramEncryptedChat.access_hash;
          ((TLRPC.TL_messages_sendEncryptedFile)localObject2).file = paramInputEncryptedFile;
          continue;
          label1189:
          int j = 1;
          continue;
          label1194:
          int i = 0;
          continue;
          label1199:
          boolean bool = false;
          continue;
          label1205:
          i = 0;
        }
      }
    });
  }
  
  protected void performSendEncryptedRequest(TLRPC.TL_messages_sendEncryptedMultiMedia paramTL_messages_sendEncryptedMultiMedia, SendMessagesHelper.DelayedMessage paramDelayedMessage)
  {
    int i = 0;
    while (i < paramTL_messages_sendEncryptedMultiMedia.files.size())
    {
      performSendEncryptedRequest((TLRPC.DecryptedMessage)paramTL_messages_sendEncryptedMultiMedia.messages.get(i), (TLRPC.Message)paramDelayedMessage.messages.get(i), paramDelayedMessage.encryptedChat, (TLRPC.InputEncryptedFile)paramTL_messages_sendEncryptedMultiMedia.files.get(i), (String)paramDelayedMessage.originalPaths.get(i), (MessageObject)paramDelayedMessage.messageObjects.get(i));
      i += 1;
    }
  }
  
  public void processAcceptedSecretChat(final TLRPC.EncryptedChat paramEncryptedChat)
  {
    final Object localObject1 = new BigInteger(1, MessagesStorage.getInstance(this.currentAccount).getSecretPBytes());
    Object localObject2 = new BigInteger(1, paramEncryptedChat.g_a_or_b);
    if (!Utilities.isGoodGaAndGb((BigInteger)localObject2, (BigInteger)localObject1))
    {
      declineSecretChat(paramEncryptedChat.id);
      return;
    }
    localObject2 = ((BigInteger)localObject2).modPow(new BigInteger(1, paramEncryptedChat.a_or_b), (BigInteger)localObject1).toByteArray();
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
      if (paramEncryptedChat.key_fingerprint != l) {
        break;
      }
      paramEncryptedChat.auth_key = ((byte[])localObject1);
      paramEncryptedChat.key_create_date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
      paramEncryptedChat.seq_in = -2;
      paramEncryptedChat.seq_out = 1;
      MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
      MessagesController.getInstance(this.currentAccount).putEncryptedChat(paramEncryptedChat, false);
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.encryptedChatUpdated, new Object[] { paramEncryptedChat });
          SecretChatHelper.this.sendNotifyLayerMessage(paramEncryptedChat, null);
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
    localObject1 = new TLRPC.TL_encryptedChatDiscarded();
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).id = paramEncryptedChat.id;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).user_id = paramEncryptedChat.user_id;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).auth_key = paramEncryptedChat.auth_key;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).key_create_date = paramEncryptedChat.key_create_date;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).key_use_count_in = paramEncryptedChat.key_use_count_in;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).key_use_count_out = paramEncryptedChat.key_use_count_out;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).seq_in = paramEncryptedChat.seq_in;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).seq_out = paramEncryptedChat.seq_out;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).admin_id = paramEncryptedChat.admin_id;
    ((TLRPC.TL_encryptedChatDiscarded)localObject1).mtproto_seq = paramEncryptedChat.mtproto_seq;
    MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat((TLRPC.EncryptedChat)localObject1);
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance(SecretChatHelper.this.currentAccount).putEncryptedChat(localObject1, false);
        NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.encryptedChatUpdated, new Object[] { localObject1 });
      }
    });
    declineSecretChat(paramEncryptedChat.id);
  }
  
  public TLRPC.Message processDecryptedObject(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.EncryptedFile paramEncryptedFile, int paramInt, TLObject paramTLObject, boolean paramBoolean)
  {
    Object localObject1;
    label184:
    label590:
    label611:
    Object localObject2;
    if (paramTLObject != null)
    {
      int j = paramEncryptedChat.admin_id;
      int i = j;
      if (j == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
        i = paramEncryptedChat.participant_id;
      }
      if ((AndroidUtilities.getPeerLayerVersion(paramEncryptedChat.layer) >= 20) && (paramEncryptedChat.exchange_id == 0L) && (paramEncryptedChat.future_key_fingerprint == 0L) && (paramEncryptedChat.key_use_count_in >= 120)) {
        requestNewSecretChatKey(paramEncryptedChat);
      }
      if ((paramEncryptedChat.exchange_id == 0L) && (paramEncryptedChat.future_key_fingerprint != 0L) && (!paramBoolean))
      {
        paramEncryptedChat.future_auth_key = new byte['Ā'];
        paramEncryptedChat.future_key_fingerprint = 0L;
        MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
        if (!(paramTLObject instanceof TLRPC.TL_decryptedMessage)) {
          break label3398;
        }
        localObject1 = (TLRPC.TL_decryptedMessage)paramTLObject;
        if (AndroidUtilities.getPeerLayerVersion(paramEncryptedChat.layer) < 17) {
          break label590;
        }
        paramTLObject = new TLRPC.TL_message_secret();
        paramTLObject.ttl = ((TLRPC.TL_decryptedMessage)localObject1).ttl;
        paramTLObject.entities = ((TLRPC.TL_decryptedMessage)localObject1).entities;
        paramTLObject.message = ((TLRPC.TL_decryptedMessage)localObject1).message;
        paramTLObject.date = paramInt;
        j = UserConfig.getInstance(this.currentAccount).getNewMessageId();
        paramTLObject.id = j;
        paramTLObject.local_id = j;
        UserConfig.getInstance(this.currentAccount).saveConfig(false);
        paramTLObject.from_id = i;
        paramTLObject.to_id = new TLRPC.TL_peerUser();
        paramTLObject.random_id = ((TLRPC.TL_decryptedMessage)localObject1).random_id;
        paramTLObject.to_id.user_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
        paramTLObject.unread = true;
        paramTLObject.flags = 768;
        if ((((TLRPC.TL_decryptedMessage)localObject1).via_bot_name != null) && (((TLRPC.TL_decryptedMessage)localObject1).via_bot_name.length() > 0))
        {
          paramTLObject.via_bot_name = ((TLRPC.TL_decryptedMessage)localObject1).via_bot_name;
          paramTLObject.flags |= 0x800;
        }
        if (((TLRPC.TL_decryptedMessage)localObject1).grouped_id != 0L)
        {
          paramTLObject.grouped_id = ((TLRPC.TL_decryptedMessage)localObject1).grouped_id;
          paramTLObject.flags |= 0x20000;
        }
        paramTLObject.dialog_id = (paramEncryptedChat.id << 32);
        if (((TLRPC.TL_decryptedMessage)localObject1).reply_to_random_id != 0L)
        {
          paramTLObject.reply_to_random_id = ((TLRPC.TL_decryptedMessage)localObject1).reply_to_random_id;
          paramTLObject.flags |= 0x8;
        }
        if ((((TLRPC.TL_decryptedMessage)localObject1).media != null) && (!(((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaEmpty))) {
          break label611;
        }
        paramTLObject.media = new TLRPC.TL_messageMediaEmpty();
      }
      for (;;)
      {
        if ((paramTLObject.ttl != 0) && (paramTLObject.media.ttl_seconds == 0))
        {
          paramTLObject.media.ttl_seconds = paramTLObject.ttl;
          paramEncryptedChat = paramTLObject.media;
          paramEncryptedChat.flags |= 0x4;
        }
        return paramTLObject;
        if ((paramEncryptedChat.exchange_id == 0L) || (!paramBoolean)) {
          break;
        }
        paramEncryptedChat.key_fingerprint = paramEncryptedChat.future_key_fingerprint;
        paramEncryptedChat.auth_key = paramEncryptedChat.future_auth_key;
        paramEncryptedChat.key_create_date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
        paramEncryptedChat.future_auth_key = new byte['Ā'];
        paramEncryptedChat.future_key_fingerprint = 0L;
        paramEncryptedChat.key_use_count_in = 0;
        paramEncryptedChat.key_use_count_out = 0;
        paramEncryptedChat.exchange_id = 0L;
        MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
        break;
        paramTLObject = new TLRPC.TL_message();
        paramTLObject.ttl = paramEncryptedChat.ttl;
        break label184;
        if ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaWebPage))
        {
          paramTLObject.media = new TLRPC.TL_messageMediaWebPage();
          paramTLObject.media.webpage = new TLRPC.TL_webPageUrlPending();
          paramTLObject.media.webpage.url = ((TLRPC.TL_decryptedMessage)localObject1).media.url;
        }
        else if ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaContact))
        {
          paramTLObject.media = new TLRPC.TL_messageMediaContact();
          paramTLObject.media.last_name = ((TLRPC.TL_decryptedMessage)localObject1).media.last_name;
          paramTLObject.media.first_name = ((TLRPC.TL_decryptedMessage)localObject1).media.first_name;
          paramTLObject.media.phone_number = ((TLRPC.TL_decryptedMessage)localObject1).media.phone_number;
          paramTLObject.media.user_id = ((TLRPC.TL_decryptedMessage)localObject1).media.user_id;
        }
        else if ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaGeoPoint))
        {
          paramTLObject.media = new TLRPC.TL_messageMediaGeo();
          paramTLObject.media.geo = new TLRPC.TL_geoPoint();
          paramTLObject.media.geo.lat = ((TLRPC.TL_decryptedMessage)localObject1).media.lat;
          paramTLObject.media.geo._long = ((TLRPC.TL_decryptedMessage)localObject1).media._long;
        }
        else
        {
          if ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaPhoto))
          {
            if ((((TLRPC.TL_decryptedMessage)localObject1).media.key == null) || (((TLRPC.TL_decryptedMessage)localObject1).media.key.length != 32) || (((TLRPC.TL_decryptedMessage)localObject1).media.iv == null) || (((TLRPC.TL_decryptedMessage)localObject1).media.iv.length != 32)) {
              return null;
            }
            paramTLObject.media = new TLRPC.TL_messageMediaPhoto();
            paramEncryptedChat = paramTLObject.media;
            paramEncryptedChat.flags |= 0x3;
            if (((TLRPC.TL_decryptedMessage)localObject1).media.caption != null) {}
            for (paramEncryptedChat = ((TLRPC.TL_decryptedMessage)localObject1).media.caption;; paramEncryptedChat = "")
            {
              paramTLObject.message = paramEncryptedChat;
              paramTLObject.media.photo = new TLRPC.TL_photo();
              paramTLObject.media.photo.date = paramTLObject.date;
              paramEncryptedChat = ((TLRPC.TL_decryptedMessageMediaPhoto)((TLRPC.TL_decryptedMessage)localObject1).media).thumb;
              if ((paramEncryptedChat != null) && (paramEncryptedChat.length != 0) && (paramEncryptedChat.length <= 6000) && (((TLRPC.TL_decryptedMessage)localObject1).media.thumb_w <= 100) && (((TLRPC.TL_decryptedMessage)localObject1).media.thumb_h <= 100))
              {
                localObject2 = new TLRPC.TL_photoCachedSize();
                ((TLRPC.TL_photoCachedSize)localObject2).w = ((TLRPC.TL_decryptedMessage)localObject1).media.thumb_w;
                ((TLRPC.TL_photoCachedSize)localObject2).h = ((TLRPC.TL_decryptedMessage)localObject1).media.thumb_h;
                ((TLRPC.TL_photoCachedSize)localObject2).bytes = paramEncryptedChat;
                ((TLRPC.TL_photoCachedSize)localObject2).type = "s";
                ((TLRPC.TL_photoCachedSize)localObject2).location = new TLRPC.TL_fileLocationUnavailable();
                paramTLObject.media.photo.sizes.add(localObject2);
              }
              if (paramTLObject.ttl != 0)
              {
                paramTLObject.media.ttl_seconds = paramTLObject.ttl;
                paramEncryptedChat = paramTLObject.media;
                paramEncryptedChat.flags |= 0x4;
              }
              paramEncryptedChat = new TLRPC.TL_photoSize();
              paramEncryptedChat.w = ((TLRPC.TL_decryptedMessage)localObject1).media.w;
              paramEncryptedChat.h = ((TLRPC.TL_decryptedMessage)localObject1).media.h;
              paramEncryptedChat.type = "x";
              paramEncryptedChat.size = paramEncryptedFile.size;
              paramEncryptedChat.location = new TLRPC.TL_fileEncryptedLocation();
              paramEncryptedChat.location.key = ((TLRPC.TL_decryptedMessage)localObject1).media.key;
              paramEncryptedChat.location.iv = ((TLRPC.TL_decryptedMessage)localObject1).media.iv;
              paramEncryptedChat.location.dc_id = paramEncryptedFile.dc_id;
              paramEncryptedChat.location.volume_id = paramEncryptedFile.id;
              paramEncryptedChat.location.secret = paramEncryptedFile.access_hash;
              paramEncryptedChat.location.local_id = paramEncryptedFile.key_fingerprint;
              paramTLObject.media.photo.sizes.add(paramEncryptedChat);
              break;
            }
          }
          if ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaVideo))
          {
            if ((((TLRPC.TL_decryptedMessage)localObject1).media.key == null) || (((TLRPC.TL_decryptedMessage)localObject1).media.key.length != 32) || (((TLRPC.TL_decryptedMessage)localObject1).media.iv == null) || (((TLRPC.TL_decryptedMessage)localObject1).media.iv.length != 32)) {
              return null;
            }
            paramTLObject.media = new TLRPC.TL_messageMediaDocument();
            paramEncryptedChat = paramTLObject.media;
            paramEncryptedChat.flags |= 0x3;
            paramTLObject.media.document = new TLRPC.TL_documentEncrypted();
            paramTLObject.media.document.key = ((TLRPC.TL_decryptedMessage)localObject1).media.key;
            paramTLObject.media.document.iv = ((TLRPC.TL_decryptedMessage)localObject1).media.iv;
            paramTLObject.media.document.dc_id = paramEncryptedFile.dc_id;
            if (((TLRPC.TL_decryptedMessage)localObject1).media.caption != null)
            {
              paramEncryptedChat = ((TLRPC.TL_decryptedMessage)localObject1).media.caption;
              label1495:
              paramTLObject.message = paramEncryptedChat;
              paramTLObject.media.document.date = paramInt;
              paramTLObject.media.document.size = paramEncryptedFile.size;
              paramTLObject.media.document.id = paramEncryptedFile.id;
              paramTLObject.media.document.access_hash = paramEncryptedFile.access_hash;
              paramTLObject.media.document.mime_type = ((TLRPC.TL_decryptedMessage)localObject1).media.mime_type;
              if (paramTLObject.media.document.mime_type == null) {
                paramTLObject.media.document.mime_type = "video/mp4";
              }
              paramEncryptedChat = ((TLRPC.TL_decryptedMessageMediaVideo)((TLRPC.TL_decryptedMessage)localObject1).media).thumb;
              if ((paramEncryptedChat == null) || (paramEncryptedChat.length == 0) || (paramEncryptedChat.length > 6000) || (((TLRPC.TL_decryptedMessage)localObject1).media.thumb_w > 100) || (((TLRPC.TL_decryptedMessage)localObject1).media.thumb_h > 100)) {
                break label1918;
              }
              paramTLObject.media.document.thumb = new TLRPC.TL_photoCachedSize();
              paramTLObject.media.document.thumb.bytes = paramEncryptedChat;
              paramTLObject.media.document.thumb.w = ((TLRPC.TL_decryptedMessage)localObject1).media.thumb_w;
              paramTLObject.media.document.thumb.h = ((TLRPC.TL_decryptedMessage)localObject1).media.thumb_h;
              paramTLObject.media.document.thumb.type = "s";
              paramTLObject.media.document.thumb.location = new TLRPC.TL_fileLocationUnavailable();
            }
            for (;;)
            {
              paramEncryptedChat = new TLRPC.TL_documentAttributeVideo();
              paramEncryptedChat.w = ((TLRPC.TL_decryptedMessage)localObject1).media.w;
              paramEncryptedChat.h = ((TLRPC.TL_decryptedMessage)localObject1).media.h;
              paramEncryptedChat.duration = ((TLRPC.TL_decryptedMessage)localObject1).media.duration;
              paramEncryptedChat.supports_streaming = false;
              paramTLObject.media.document.attributes.add(paramEncryptedChat);
              if (paramTLObject.ttl != 0)
              {
                paramTLObject.media.ttl_seconds = paramTLObject.ttl;
                paramEncryptedChat = paramTLObject.media;
                paramEncryptedChat.flags |= 0x4;
              }
              if (paramTLObject.ttl == 0) {
                break;
              }
              paramTLObject.ttl = Math.max(((TLRPC.TL_decryptedMessage)localObject1).media.duration + 1, paramTLObject.ttl);
              break;
              paramEncryptedChat = "";
              break label1495;
              label1918:
              paramTLObject.media.document.thumb = new TLRPC.TL_photoSizeEmpty();
              paramTLObject.media.document.thumb.type = "s";
            }
          }
          if ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaDocument))
          {
            if ((((TLRPC.TL_decryptedMessage)localObject1).media.key == null) || (((TLRPC.TL_decryptedMessage)localObject1).media.key.length != 32) || (((TLRPC.TL_decryptedMessage)localObject1).media.iv == null) || (((TLRPC.TL_decryptedMessage)localObject1).media.iv.length != 32)) {
              return null;
            }
            paramTLObject.media = new TLRPC.TL_messageMediaDocument();
            paramEncryptedChat = paramTLObject.media;
            paramEncryptedChat.flags |= 0x3;
            if (((TLRPC.TL_decryptedMessage)localObject1).media.caption != null)
            {
              paramEncryptedChat = ((TLRPC.TL_decryptedMessage)localObject1).media.caption;
              label2067:
              paramTLObject.message = paramEncryptedChat;
              paramTLObject.media.document = new TLRPC.TL_documentEncrypted();
              paramTLObject.media.document.id = paramEncryptedFile.id;
              paramTLObject.media.document.access_hash = paramEncryptedFile.access_hash;
              paramTLObject.media.document.date = paramInt;
              if (!(((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaDocument_layer8)) {
                break label2520;
              }
              paramEncryptedChat = new TLRPC.TL_documentAttributeFilename();
              paramEncryptedChat.file_name = ((TLRPC.TL_decryptedMessage)localObject1).media.file_name;
              paramTLObject.media.document.attributes.add(paramEncryptedChat);
              label2177:
              paramTLObject.media.document.mime_type = ((TLRPC.TL_decryptedMessage)localObject1).media.mime_type;
              paramEncryptedChat = paramTLObject.media.document;
              if (((TLRPC.TL_decryptedMessage)localObject1).media.size == 0) {
                break label2542;
              }
              paramInt = Math.min(((TLRPC.TL_decryptedMessage)localObject1).media.size, paramEncryptedFile.size);
              label2232:
              paramEncryptedChat.size = paramInt;
              paramTLObject.media.document.key = ((TLRPC.TL_decryptedMessage)localObject1).media.key;
              paramTLObject.media.document.iv = ((TLRPC.TL_decryptedMessage)localObject1).media.iv;
              if (paramTLObject.media.document.mime_type == null) {
                paramTLObject.media.document.mime_type = "";
              }
              paramEncryptedChat = ((TLRPC.TL_decryptedMessageMediaDocument)((TLRPC.TL_decryptedMessage)localObject1).media).thumb;
              if ((paramEncryptedChat == null) || (paramEncryptedChat.length == 0) || (paramEncryptedChat.length > 6000) || (((TLRPC.TL_decryptedMessage)localObject1).media.thumb_w > 100) || (((TLRPC.TL_decryptedMessage)localObject1).media.thumb_h > 100)) {
                break label2550;
              }
              paramTLObject.media.document.thumb = new TLRPC.TL_photoCachedSize();
              paramTLObject.media.document.thumb.bytes = paramEncryptedChat;
              paramTLObject.media.document.thumb.w = ((TLRPC.TL_decryptedMessage)localObject1).media.thumb_w;
              paramTLObject.media.document.thumb.h = ((TLRPC.TL_decryptedMessage)localObject1).media.thumb_h;
              paramTLObject.media.document.thumb.type = "s";
              paramTLObject.media.document.thumb.location = new TLRPC.TL_fileLocationUnavailable();
            }
            for (;;)
            {
              paramTLObject.media.document.dc_id = paramEncryptedFile.dc_id;
              if ((!MessageObject.isVoiceMessage(paramTLObject)) && (!MessageObject.isRoundVideoMessage(paramTLObject))) {
                break;
              }
              paramTLObject.media_unread = true;
              break;
              paramEncryptedChat = "";
              break label2067;
              label2520:
              paramTLObject.media.document.attributes = ((TLRPC.TL_decryptedMessage)localObject1).media.attributes;
              break label2177;
              label2542:
              paramInt = paramEncryptedFile.size;
              break label2232;
              label2550:
              paramTLObject.media.document.thumb = new TLRPC.TL_photoSizeEmpty();
              paramTLObject.media.document.thumb.type = "s";
            }
          }
          if ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaExternalDocument))
          {
            paramTLObject.media = new TLRPC.TL_messageMediaDocument();
            paramEncryptedChat = paramTLObject.media;
            paramEncryptedChat.flags |= 0x3;
            paramTLObject.message = "";
            paramTLObject.media.document = new TLRPC.TL_document();
            paramTLObject.media.document.id = ((TLRPC.TL_decryptedMessage)localObject1).media.id;
            paramTLObject.media.document.access_hash = ((TLRPC.TL_decryptedMessage)localObject1).media.access_hash;
            paramTLObject.media.document.date = ((TLRPC.TL_decryptedMessage)localObject1).media.date;
            paramTLObject.media.document.attributes = ((TLRPC.TL_decryptedMessage)localObject1).media.attributes;
            paramTLObject.media.document.mime_type = ((TLRPC.TL_decryptedMessage)localObject1).media.mime_type;
            paramTLObject.media.document.dc_id = ((TLRPC.TL_decryptedMessage)localObject1).media.dc_id;
            paramTLObject.media.document.size = ((TLRPC.TL_decryptedMessage)localObject1).media.size;
            paramTLObject.media.document.thumb = ((TLRPC.TL_decryptedMessageMediaExternalDocument)((TLRPC.TL_decryptedMessage)localObject1).media).thumb;
            if (paramTLObject.media.document.mime_type == null) {
              paramTLObject.media.document.mime_type = "";
            }
          }
          else
          {
            if ((((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaAudio))
            {
              if ((((TLRPC.TL_decryptedMessage)localObject1).media.key == null) || (((TLRPC.TL_decryptedMessage)localObject1).media.key.length != 32) || (((TLRPC.TL_decryptedMessage)localObject1).media.iv == null) || (((TLRPC.TL_decryptedMessage)localObject1).media.iv.length != 32)) {
                return null;
              }
              paramTLObject.media = new TLRPC.TL_messageMediaDocument();
              paramEncryptedChat = paramTLObject.media;
              paramEncryptedChat.flags |= 0x3;
              paramTLObject.media.document = new TLRPC.TL_documentEncrypted();
              paramTLObject.media.document.key = ((TLRPC.TL_decryptedMessage)localObject1).media.key;
              paramTLObject.media.document.iv = ((TLRPC.TL_decryptedMessage)localObject1).media.iv;
              paramTLObject.media.document.id = paramEncryptedFile.id;
              paramTLObject.media.document.access_hash = paramEncryptedFile.access_hash;
              paramTLObject.media.document.date = paramInt;
              paramTLObject.media.document.size = paramEncryptedFile.size;
              paramTLObject.media.document.dc_id = paramEncryptedFile.dc_id;
              paramTLObject.media.document.mime_type = ((TLRPC.TL_decryptedMessage)localObject1).media.mime_type;
              paramTLObject.media.document.thumb = new TLRPC.TL_photoSizeEmpty();
              paramTLObject.media.document.thumb.type = "s";
              if (((TLRPC.TL_decryptedMessage)localObject1).media.caption != null) {}
              for (paramEncryptedChat = ((TLRPC.TL_decryptedMessage)localObject1).media.caption;; paramEncryptedChat = "")
              {
                paramTLObject.message = paramEncryptedChat;
                if (paramTLObject.media.document.mime_type == null) {
                  paramTLObject.media.document.mime_type = "audio/ogg";
                }
                paramEncryptedChat = new TLRPC.TL_documentAttributeAudio();
                paramEncryptedChat.duration = ((TLRPC.TL_decryptedMessage)localObject1).media.duration;
                paramEncryptedChat.voice = true;
                paramTLObject.media.document.attributes.add(paramEncryptedChat);
                if (paramTLObject.ttl == 0) {
                  break;
                }
                paramTLObject.ttl = Math.max(((TLRPC.TL_decryptedMessage)localObject1).media.duration + 1, paramTLObject.ttl);
                break;
              }
            }
            if (!(((TLRPC.TL_decryptedMessage)localObject1).media instanceof TLRPC.TL_decryptedMessageMediaVenue)) {
              break label3396;
            }
            paramTLObject.media = new TLRPC.TL_messageMediaVenue();
            paramTLObject.media.geo = new TLRPC.TL_geoPoint();
            paramTLObject.media.geo.lat = ((TLRPC.TL_decryptedMessage)localObject1).media.lat;
            paramTLObject.media.geo._long = ((TLRPC.TL_decryptedMessage)localObject1).media._long;
            paramTLObject.media.title = ((TLRPC.TL_decryptedMessage)localObject1).media.title;
            paramTLObject.media.address = ((TLRPC.TL_decryptedMessage)localObject1).media.address;
            paramTLObject.media.provider = ((TLRPC.TL_decryptedMessage)localObject1).media.provider;
            paramTLObject.media.venue_id = ((TLRPC.TL_decryptedMessage)localObject1).media.venue_id;
            paramTLObject.media.venue_type = "";
          }
        }
      }
      label3396:
      return null;
      label3398:
      if ((paramTLObject instanceof TLRPC.TL_decryptedMessageService))
      {
        localObject2 = (TLRPC.TL_decryptedMessageService)paramTLObject;
        if (((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL)) || ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages)))
        {
          paramEncryptedFile = new TLRPC.TL_messageService();
          if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL))
          {
            paramEncryptedFile.action = new TLRPC.TL_messageEncryptedAction();
            if ((((TLRPC.TL_decryptedMessageService)localObject2).action.ttl_seconds < 0) || (((TLRPC.TL_decryptedMessageService)localObject2).action.ttl_seconds > 31536000)) {
              ((TLRPC.TL_decryptedMessageService)localObject2).action.ttl_seconds = 31536000;
            }
            paramEncryptedChat.ttl = ((TLRPC.TL_decryptedMessageService)localObject2).action.ttl_seconds;
            paramEncryptedFile.action.encryptedAction = ((TLRPC.TL_decryptedMessageService)localObject2).action;
            MessagesStorage.getInstance(this.currentAccount).updateEncryptedChatTTL(paramEncryptedChat);
          }
          for (;;)
          {
            j = UserConfig.getInstance(this.currentAccount).getNewMessageId();
            paramEncryptedFile.id = j;
            paramEncryptedFile.local_id = j;
            UserConfig.getInstance(this.currentAccount).saveConfig(false);
            paramEncryptedFile.unread = true;
            paramEncryptedFile.flags = 256;
            paramEncryptedFile.date = paramInt;
            paramEncryptedFile.from_id = i;
            paramEncryptedFile.to_id = new TLRPC.TL_peerUser();
            paramEncryptedFile.to_id.user_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
            paramEncryptedFile.dialog_id = (paramEncryptedChat.id << 32);
            return paramEncryptedFile;
            if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages))
            {
              paramEncryptedFile.action = new TLRPC.TL_messageEncryptedAction();
              paramEncryptedFile.action.encryptedAction = ((TLRPC.TL_decryptedMessageService)localObject2).action;
            }
          }
        }
        if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionFlushHistory))
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)MessagesController.getInstance(SecretChatHelper.this.currentAccount).dialogs_dict.get(this.val$did);
              if (localTL_dialog != null)
              {
                localTL_dialog.unread_count = 0;
                MessagesController.getInstance(SecretChatHelper.this.currentAccount).dialogMessage.remove(localTL_dialog.id);
              }
              MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
              {
                public void run()
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      NotificationsController.getInstance(SecretChatHelper.this.currentAccount).processReadMessages(null, SecretChatHelper.6.this.val$did, 0, Integer.MAX_VALUE, false);
                      LongSparseArray localLongSparseArray = new LongSparseArray(1);
                      localLongSparseArray.put(SecretChatHelper.6.this.val$did, Integer.valueOf(0));
                      NotificationsController.getInstance(SecretChatHelper.this.currentAccount).processDialogsUpdateRead(localLongSparseArray);
                    }
                  });
                }
              });
              MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).deleteDialog(this.val$did, 1);
              NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
              NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.removeAllMessagesFromDialog, new Object[] { Long.valueOf(this.val$did), Boolean.valueOf(false) });
            }
          });
          return null;
        }
        if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionDeleteMessages))
        {
          if (!((TLRPC.TL_decryptedMessageService)localObject2).action.random_ids.isEmpty()) {
            this.pendingEncMessagesToDelete.addAll(((TLRPC.TL_decryptedMessageService)localObject2).action.random_ids);
          }
          return null;
        }
        if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionReadMessages)) {
          if (!((TLRPC.TL_decryptedMessageService)localObject2).action.random_ids.isEmpty())
          {
            paramInt = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
            MessagesStorage.getInstance(this.currentAccount).createTaskForSecretChat(paramEncryptedChat.id, paramInt, paramInt, 1, ((TLRPC.TL_decryptedMessageService)localObject2).action.random_ids);
          }
        }
      }
    }
    for (;;)
    {
      return null;
      if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionNotifyLayer))
      {
        applyPeerLayer(paramEncryptedChat, ((TLRPC.TL_decryptedMessageService)localObject2).action.layer);
      }
      else
      {
        if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionRequestKey))
        {
          if (paramEncryptedChat.exchange_id != 0L)
          {
            if (paramEncryptedChat.exchange_id > ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id)
            {
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("we already have request key with higher exchange_id");
              }
              return null;
            }
            sendAbortKeyMessage(paramEncryptedChat, null, paramEncryptedChat.exchange_id);
          }
          localObject1 = new byte['Ā'];
          Utilities.random.nextBytes((byte[])localObject1);
          Object localObject3 = new BigInteger(1, MessagesStorage.getInstance(this.currentAccount).getSecretPBytes());
          paramEncryptedFile = BigInteger.valueOf(MessagesStorage.getInstance(this.currentAccount).getSecretG()).modPow(new BigInteger(1, (byte[])localObject1), (BigInteger)localObject3);
          BigInteger localBigInteger = new BigInteger(1, ((TLRPC.TL_decryptedMessageService)localObject2).action.g_a);
          if (!Utilities.isGoodGaAndGb(localBigInteger, (BigInteger)localObject3))
          {
            sendAbortKeyMessage(paramEncryptedChat, null, ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id);
            return null;
          }
          paramEncryptedFile = paramEncryptedFile.toByteArray();
          paramTLObject = paramEncryptedFile;
          if (paramEncryptedFile.length > 256)
          {
            paramTLObject = new byte['Ā'];
            System.arraycopy(paramEncryptedFile, 1, paramTLObject, 0, 256);
          }
          localObject1 = localBigInteger.modPow(new BigInteger(1, (byte[])localObject1), (BigInteger)localObject3).toByteArray();
          if (localObject1.length > 256)
          {
            paramEncryptedFile = new byte['Ā'];
            System.arraycopy(localObject1, localObject1.length - 256, paramEncryptedFile, 0, 256);
          }
          for (;;)
          {
            localObject1 = Utilities.computeSHA1(paramEncryptedFile);
            localObject3 = new byte[8];
            System.arraycopy(localObject1, localObject1.length - 8, localObject3, 0, 8);
            paramEncryptedChat.exchange_id = ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id;
            paramEncryptedChat.future_auth_key = paramEncryptedFile;
            paramEncryptedChat.future_key_fingerprint = Utilities.bytesToLong((byte[])localObject3);
            paramEncryptedChat.g_a_or_b = paramTLObject;
            MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
            sendAcceptKeyMessage(paramEncryptedChat, null);
            break;
            paramEncryptedFile = (TLRPC.EncryptedFile)localObject1;
            if (localObject1.length < 256)
            {
              paramEncryptedFile = new byte['Ā'];
              System.arraycopy(localObject1, 0, paramEncryptedFile, 256 - localObject1.length, localObject1.length);
              paramInt = 0;
              while (paramInt < 256 - localObject1.length)
              {
                localObject1[paramInt] = 0;
                paramInt += 1;
              }
            }
          }
        }
        long l;
        if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionAcceptKey))
        {
          if (paramEncryptedChat.exchange_id == ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id)
          {
            paramEncryptedFile = new BigInteger(1, MessagesStorage.getInstance(this.currentAccount).getSecretPBytes());
            paramTLObject = new BigInteger(1, ((TLRPC.TL_decryptedMessageService)localObject2).action.g_b);
            if (!Utilities.isGoodGaAndGb(paramTLObject, paramEncryptedFile))
            {
              paramEncryptedChat.future_auth_key = new byte['Ā'];
              paramEncryptedChat.future_key_fingerprint = 0L;
              paramEncryptedChat.exchange_id = 0L;
              MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
              sendAbortKeyMessage(paramEncryptedChat, null, ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id);
              return null;
            }
            paramTLObject = paramTLObject.modPow(new BigInteger(1, paramEncryptedChat.a_or_b), paramEncryptedFile).toByteArray();
            if (paramTLObject.length > 256)
            {
              paramEncryptedFile = new byte['Ā'];
              System.arraycopy(paramTLObject, paramTLObject.length - 256, paramEncryptedFile, 0, 256);
            }
            for (;;)
            {
              paramTLObject = Utilities.computeSHA1(paramEncryptedFile);
              localObject1 = new byte[8];
              System.arraycopy(paramTLObject, paramTLObject.length - 8, localObject1, 0, 8);
              l = Utilities.bytesToLong((byte[])localObject1);
              if (((TLRPC.TL_decryptedMessageService)localObject2).action.key_fingerprint != l) {
                break label4555;
              }
              paramEncryptedChat.future_auth_key = paramEncryptedFile;
              paramEncryptedChat.future_key_fingerprint = l;
              MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
              sendCommitKeyMessage(paramEncryptedChat, null);
              break;
              paramEncryptedFile = paramTLObject;
              if (paramTLObject.length < 256)
              {
                paramEncryptedFile = new byte['Ā'];
                System.arraycopy(paramTLObject, 0, paramEncryptedFile, 256 - paramTLObject.length, paramTLObject.length);
                paramInt = 0;
                while (paramInt < 256 - paramTLObject.length)
                {
                  paramTLObject[paramInt] = 0;
                  paramInt += 1;
                }
              }
            }
            label4555:
            paramEncryptedChat.future_auth_key = new byte['Ā'];
            paramEncryptedChat.future_key_fingerprint = 0L;
            paramEncryptedChat.exchange_id = 0L;
            MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
            sendAbortKeyMessage(paramEncryptedChat, null, ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id);
          }
          else
          {
            paramEncryptedChat.future_auth_key = new byte['Ā'];
            paramEncryptedChat.future_key_fingerprint = 0L;
            paramEncryptedChat.exchange_id = 0L;
            MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
            sendAbortKeyMessage(paramEncryptedChat, null, ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id);
          }
        }
        else if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionCommitKey))
        {
          if ((paramEncryptedChat.exchange_id == ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id) && (paramEncryptedChat.future_key_fingerprint == ((TLRPC.TL_decryptedMessageService)localObject2).action.key_fingerprint))
          {
            l = paramEncryptedChat.key_fingerprint;
            paramEncryptedFile = paramEncryptedChat.auth_key;
            paramEncryptedChat.key_fingerprint = paramEncryptedChat.future_key_fingerprint;
            paramEncryptedChat.auth_key = paramEncryptedChat.future_auth_key;
            paramEncryptedChat.key_create_date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
            paramEncryptedChat.future_auth_key = paramEncryptedFile;
            paramEncryptedChat.future_key_fingerprint = l;
            paramEncryptedChat.key_use_count_in = 0;
            paramEncryptedChat.key_use_count_out = 0;
            paramEncryptedChat.exchange_id = 0L;
            MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
            sendNoopMessage(paramEncryptedChat, null);
          }
          else
          {
            paramEncryptedChat.future_auth_key = new byte['Ā'];
            paramEncryptedChat.future_key_fingerprint = 0L;
            paramEncryptedChat.exchange_id = 0L;
            MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
            sendAbortKeyMessage(paramEncryptedChat, null, ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id);
          }
        }
        else if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionAbortKey))
        {
          if (paramEncryptedChat.exchange_id == ((TLRPC.TL_decryptedMessageService)localObject2).action.exchange_id)
          {
            paramEncryptedChat.future_auth_key = new byte['Ā'];
            paramEncryptedChat.future_key_fingerprint = 0L;
            paramEncryptedChat.exchange_id = 0L;
            MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
          }
        }
        else if (!(((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionNoop)) {
          if ((((TLRPC.TL_decryptedMessageService)localObject2).action instanceof TLRPC.TL_decryptedMessageActionResend))
          {
            if ((((TLRPC.TL_decryptedMessageService)localObject2).action.end_seq_no < paramEncryptedChat.in_seq_no) || (((TLRPC.TL_decryptedMessageService)localObject2).action.end_seq_no < ((TLRPC.TL_decryptedMessageService)localObject2).action.start_seq_no)) {
              return null;
            }
            if (((TLRPC.TL_decryptedMessageService)localObject2).action.start_seq_no < paramEncryptedChat.in_seq_no) {
              ((TLRPC.TL_decryptedMessageService)localObject2).action.start_seq_no = paramEncryptedChat.in_seq_no;
            }
            resendMessages(((TLRPC.TL_decryptedMessageService)localObject2).action.start_seq_no, ((TLRPC.TL_decryptedMessageService)localObject2).action.end_seq_no, paramEncryptedChat);
          }
          else
          {
            return null;
            if (BuildVars.LOGS_ENABLED)
            {
              FileLog.e("unknown message " + paramTLObject);
              continue;
              if (BuildVars.LOGS_ENABLED) {
                FileLog.e("unknown TLObject");
              }
            }
          }
        }
      }
    }
  }
  
  protected void processPendingEncMessages()
  {
    if (!this.pendingEncMessagesToDelete.isEmpty())
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          int i = 0;
          while (i < this.val$pendingEncMessagesToDeleteCopy.size())
          {
            MessageObject localMessageObject = (MessageObject)MessagesController.getInstance(SecretChatHelper.this.currentAccount).dialogMessagesByRandomIds.get(((Long)this.val$pendingEncMessagesToDeleteCopy.get(i)).longValue());
            if (localMessageObject != null) {
              localMessageObject.deleted = true;
            }
            i += 1;
          }
        }
      });
      ArrayList localArrayList = new ArrayList(this.pendingEncMessagesToDelete);
      MessagesStorage.getInstance(this.currentAccount).markMessagesAsDeletedByRandoms(localArrayList);
      this.pendingEncMessagesToDelete.clear();
    }
  }
  
  protected void processUpdateEncryption(TLRPC.TL_updateEncryption paramTL_updateEncryption, final ConcurrentHashMap<Integer, TLRPC.User> paramConcurrentHashMap)
  {
    final TLRPC.EncryptedChat localEncryptedChat = paramTL_updateEncryption.chat;
    long l = localEncryptedChat.id;
    final Object localObject = MessagesController.getInstance(this.currentAccount).getEncryptedChatDB(localEncryptedChat.id, false);
    if (((localEncryptedChat instanceof TLRPC.TL_encryptedChatRequested)) && (localObject == null))
    {
      int j = localEncryptedChat.participant_id;
      int i = j;
      if (j == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
        i = localEncryptedChat.admin_id;
      }
      TLRPC.User localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
      localObject = localUser;
      if (localUser == null) {
        localObject = (TLRPC.User)paramConcurrentHashMap.get(Integer.valueOf(i));
      }
      localEncryptedChat.user_id = i;
      paramConcurrentHashMap = new TLRPC.TL_dialog();
      paramConcurrentHashMap.id = (l << 32);
      paramConcurrentHashMap.unread_count = 0;
      paramConcurrentHashMap.top_message = 0;
      paramConcurrentHashMap.last_message_date = paramTL_updateEncryption.date;
      MessagesController.getInstance(this.currentAccount).putEncryptedChat(localEncryptedChat, false);
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          MessagesController.getInstance(SecretChatHelper.this.currentAccount).dialogs_dict.put(paramConcurrentHashMap.id, paramConcurrentHashMap);
          MessagesController.getInstance(SecretChatHelper.this.currentAccount).dialogs.add(paramConcurrentHashMap);
          MessagesController.getInstance(SecretChatHelper.this.currentAccount).sortDialogs(null);
          NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
        }
      });
      MessagesStorage.getInstance(this.currentAccount).putEncryptedChat(localEncryptedChat, (TLRPC.User)localObject, paramConcurrentHashMap);
      acceptSecretChat(localEncryptedChat);
    }
    do
    {
      return;
      if (!(localEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
        break;
      }
      if ((localObject != null) && ((localObject instanceof TLRPC.TL_encryptedChatWaiting)) && ((((TLRPC.EncryptedChat)localObject).auth_key == null) || (((TLRPC.EncryptedChat)localObject).auth_key.length == 1)))
      {
        localEncryptedChat.a_or_b = ((TLRPC.EncryptedChat)localObject).a_or_b;
        localEncryptedChat.user_id = ((TLRPC.EncryptedChat)localObject).user_id;
        processAcceptedSecretChat(localEncryptedChat);
        return;
      }
    } while ((localObject != null) || (!this.startingSecretChat));
    this.delayedEncryptedChatUpdates.add(paramTL_updateEncryption);
    return;
    if (localObject != null)
    {
      localEncryptedChat.user_id = ((TLRPC.EncryptedChat)localObject).user_id;
      localEncryptedChat.auth_key = ((TLRPC.EncryptedChat)localObject).auth_key;
      localEncryptedChat.key_create_date = ((TLRPC.EncryptedChat)localObject).key_create_date;
      localEncryptedChat.key_use_count_in = ((TLRPC.EncryptedChat)localObject).key_use_count_in;
      localEncryptedChat.key_use_count_out = ((TLRPC.EncryptedChat)localObject).key_use_count_out;
      localEncryptedChat.ttl = ((TLRPC.EncryptedChat)localObject).ttl;
      localEncryptedChat.seq_in = ((TLRPC.EncryptedChat)localObject).seq_in;
      localEncryptedChat.seq_out = ((TLRPC.EncryptedChat)localObject).seq_out;
      localEncryptedChat.admin_id = ((TLRPC.EncryptedChat)localObject).admin_id;
      localEncryptedChat.mtproto_seq = ((TLRPC.EncryptedChat)localObject).mtproto_seq;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (localObject != null) {
          MessagesController.getInstance(SecretChatHelper.this.currentAccount).putEncryptedChat(localEncryptedChat, false);
        }
        MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).updateEncryptedChat(localEncryptedChat);
        NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.encryptedChatUpdated, new Object[] { localEncryptedChat });
      }
    });
  }
  
  public void requestNewSecretChatKey(TLRPC.EncryptedChat paramEncryptedChat)
  {
    if (AndroidUtilities.getPeerLayerVersion(paramEncryptedChat.layer) < 20) {
      return;
    }
    byte[] arrayOfByte3 = new byte['Ā'];
    Utilities.random.nextBytes(arrayOfByte3);
    byte[] arrayOfByte2 = BigInteger.valueOf(MessagesStorage.getInstance(this.currentAccount).getSecretG()).modPow(new BigInteger(1, arrayOfByte3), new BigInteger(1, MessagesStorage.getInstance(this.currentAccount).getSecretPBytes())).toByteArray();
    byte[] arrayOfByte1 = arrayOfByte2;
    if (arrayOfByte2.length > 256)
    {
      arrayOfByte1 = new byte['Ā'];
      System.arraycopy(arrayOfByte2, 1, arrayOfByte1, 0, 256);
    }
    paramEncryptedChat.exchange_id = SendMessagesHelper.getInstance(this.currentAccount).getNextRandomId();
    paramEncryptedChat.a_or_b = arrayOfByte3;
    paramEncryptedChat.g_a = arrayOfByte1;
    MessagesStorage.getInstance(this.currentAccount).updateEncryptedChat(paramEncryptedChat);
    sendRequestKeyMessage(paramEncryptedChat, null);
  }
  
  public void sendAbortKeyMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.Message paramMessage, long paramLong)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionAbortKey();
      localTL_decryptedMessageService.action.exchange_id = paramLong;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendAcceptKeyMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionAcceptKey();
      localTL_decryptedMessageService.action.exchange_id = paramEncryptedChat.exchange_id;
      localTL_decryptedMessageService.action.key_fingerprint = paramEncryptedChat.future_key_fingerprint;
      localTL_decryptedMessageService.action.g_b = paramEncryptedChat.g_a_or_b;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendClearHistoryMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionFlushHistory();
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendCommitKeyMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionCommitKey();
      localTL_decryptedMessageService.action.exchange_id = paramEncryptedChat.exchange_id;
      localTL_decryptedMessageService.action.key_fingerprint = paramEncryptedChat.future_key_fingerprint;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendMessagesDeleteMessage(TLRPC.EncryptedChat paramEncryptedChat, ArrayList<Long> paramArrayList, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionDeleteMessages();
      localTL_decryptedMessageService.action.random_ids = paramArrayList;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendMessagesReadMessage(TLRPC.EncryptedChat paramEncryptedChat, ArrayList<Long> paramArrayList, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionReadMessages();
      localTL_decryptedMessageService.action.random_ids = paramArrayList;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendNoopMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionNoop();
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendNotifyLayerMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {}
    while (this.sendingNotifyLayer.contains(Integer.valueOf(paramEncryptedChat.id))) {
      return;
    }
    this.sendingNotifyLayer.add(Integer.valueOf(paramEncryptedChat.id));
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionNotifyLayer();
      localTL_decryptedMessageService.action.layer = 73;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendRequestKeyMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionRequestKey();
      localTL_decryptedMessageService.action.exchange_id = paramEncryptedChat.exchange_id;
      localTL_decryptedMessageService.action.g_a = paramEncryptedChat.g_a;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
    }
  }
  
  public void sendScreenshotMessage(TLRPC.EncryptedChat paramEncryptedChat, ArrayList<Long> paramArrayList, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionScreenshotMessages();
      localTL_decryptedMessageService.action.random_ids = paramArrayList;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
      paramArrayList = new MessageObject(this.currentAccount, paramMessage, false);
      paramArrayList.messageOwner.send_state = 1;
      ArrayList localArrayList = new ArrayList();
      localArrayList.add(paramArrayList);
      MessagesController.getInstance(this.currentAccount).updateInterfaceWithMessages(paramMessage.dialog_id, localArrayList);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
    }
  }
  
  public void sendTTLMessage(TLRPC.EncryptedChat paramEncryptedChat, TLRPC.Message paramMessage)
  {
    if (!(paramEncryptedChat instanceof TLRPC.TL_encryptedChat)) {
      return;
    }
    TLRPC.TL_decryptedMessageService localTL_decryptedMessageService = new TLRPC.TL_decryptedMessageService();
    if (paramMessage != null) {
      localTL_decryptedMessageService.action = paramMessage.action.encryptedAction;
    }
    for (;;)
    {
      localTL_decryptedMessageService.random_id = paramMessage.random_id;
      performSendEncryptedRequest(localTL_decryptedMessageService, paramMessage, paramEncryptedChat, null, null, null);
      return;
      localTL_decryptedMessageService.action = new TLRPC.TL_decryptedMessageActionSetMessageTTL();
      localTL_decryptedMessageService.action.ttl_seconds = paramEncryptedChat.ttl;
      paramMessage = createServiceSecretMessage(paramEncryptedChat, localTL_decryptedMessageService.action);
      MessageObject localMessageObject = new MessageObject(this.currentAccount, paramMessage, false);
      localMessageObject.messageOwner.send_state = 1;
      ArrayList localArrayList = new ArrayList();
      localArrayList.add(localMessageObject);
      MessagesController.getInstance(this.currentAccount).updateInterfaceWithMessages(paramMessage.dialog_id, localArrayList);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
    }
  }
  
  public void startSecretChat(final Context paramContext, final TLRPC.User paramUser)
  {
    if ((paramUser == null) || (paramContext == null)) {
      return;
    }
    this.startingSecretChat = true;
    final AlertDialog localAlertDialog = new AlertDialog(paramContext, 1);
    localAlertDialog.setMessage(LocaleController.getString("Loading", 2131493762));
    localAlertDialog.setCanceledOnTouchOutside(false);
    localAlertDialog.setCancelable(false);
    TLRPC.TL_messages_getDhConfig localTL_messages_getDhConfig = new TLRPC.TL_messages_getDhConfig();
    localTL_messages_getDhConfig.random_length = 256;
    localTL_messages_getDhConfig.version = MessagesStorage.getInstance(this.currentAccount).getLastSecretVersion();
    final int i = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getDhConfig, new RequestDelegate()
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
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  try
                  {
                    if (!((Activity)SecretChatHelper.14.this.val$context).isFinishing()) {
                      SecretChatHelper.14.this.val$progressDialog.dismiss();
                    }
                    return;
                  }
                  catch (Exception localException)
                  {
                    FileLog.e(localException);
                  }
                }
              });
              return;
            }
            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).setSecretPBytes(paramAnonymousTL_error.p);
            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).setSecretG(paramAnonymousTL_error.g);
            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).setLastSecretVersion(paramAnonymousTL_error.version);
            MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).saveSecretParams(MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getLastSecretVersion(), MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getSecretG(), MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getSecretPBytes());
          }
          final byte[] arrayOfByte = new byte['Ā'];
          int i = 0;
          while (i < 256)
          {
            arrayOfByte[i] = ((byte)((byte)(int)(Utilities.random.nextDouble() * 256.0D) ^ paramAnonymousTL_error.random[i]));
            i += 1;
          }
          paramAnonymousTL_error = BigInteger.valueOf(MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getSecretG()).modPow(new BigInteger(1, arrayOfByte), new BigInteger(1, MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).getSecretPBytes())).toByteArray();
          paramAnonymousTLObject = paramAnonymousTL_error;
          if (paramAnonymousTL_error.length > 256)
          {
            paramAnonymousTLObject = new byte['Ā'];
            System.arraycopy(paramAnonymousTL_error, 1, paramAnonymousTLObject, 0, 256);
          }
          paramAnonymousTL_error = new TLRPC.TL_messages_requestEncryption();
          paramAnonymousTL_error.g_a = paramAnonymousTLObject;
          paramAnonymousTL_error.user_id = MessagesController.getInstance(SecretChatHelper.this.currentAccount).getInputUser(paramUser);
          paramAnonymousTL_error.random_id = Utilities.random.nextInt();
          ConnectionsManager.getInstance(SecretChatHelper.this.currentAccount).sendRequest(paramAnonymousTL_error, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
            {
              if (paramAnonymous2TL_error == null)
              {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    SecretChatHelper.access$502(SecretChatHelper.this, false);
                    if (!((Activity)SecretChatHelper.14.this.val$context).isFinishing()) {}
                    try
                    {
                      SecretChatHelper.14.this.val$progressDialog.dismiss();
                      TLRPC.EncryptedChat localEncryptedChat = (TLRPC.EncryptedChat)paramAnonymous2TLObject;
                      localEncryptedChat.user_id = localEncryptedChat.participant_id;
                      localEncryptedChat.seq_in = -2;
                      localEncryptedChat.seq_out = 1;
                      localEncryptedChat.a_or_b = SecretChatHelper.14.2.this.val$salt;
                      MessagesController.getInstance(SecretChatHelper.this.currentAccount).putEncryptedChat(localEncryptedChat, false);
                      TLRPC.TL_dialog localTL_dialog = new TLRPC.TL_dialog();
                      localTL_dialog.id = (localEncryptedChat.id << 32);
                      localTL_dialog.unread_count = 0;
                      localTL_dialog.top_message = 0;
                      localTL_dialog.last_message_date = ConnectionsManager.getInstance(SecretChatHelper.this.currentAccount).getCurrentTime();
                      MessagesController.getInstance(SecretChatHelper.this.currentAccount).dialogs_dict.put(localTL_dialog.id, localTL_dialog);
                      MessagesController.getInstance(SecretChatHelper.this.currentAccount).dialogs.add(localTL_dialog);
                      MessagesController.getInstance(SecretChatHelper.this.currentAccount).sortDialogs(null);
                      MessagesStorage.getInstance(SecretChatHelper.this.currentAccount).putEncryptedChat(localEncryptedChat, SecretChatHelper.14.this.val$user, localTL_dialog);
                      NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                      NotificationCenter.getInstance(SecretChatHelper.this.currentAccount).postNotificationName(NotificationCenter.encryptedChatCreated, new Object[] { localEncryptedChat });
                      Utilities.stageQueue.postRunnable(new Runnable()
                      {
                        public void run()
                        {
                          if (!SecretChatHelper.this.delayedEncryptedChatUpdates.isEmpty())
                          {
                            MessagesController.getInstance(SecretChatHelper.this.currentAccount).processUpdateArray(SecretChatHelper.this.delayedEncryptedChatUpdates, null, null, false);
                            SecretChatHelper.this.delayedEncryptedChatUpdates.clear();
                          }
                        }
                      });
                      return;
                    }
                    catch (Exception localException)
                    {
                      for (;;)
                      {
                        FileLog.e(localException);
                      }
                    }
                  }
                });
                return;
              }
              SecretChatHelper.this.delayedEncryptedChatUpdates.clear();
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (!((Activity)SecretChatHelper.14.this.val$context).isFinishing()) {
                    SecretChatHelper.access$502(SecretChatHelper.this, false);
                  }
                  try
                  {
                    SecretChatHelper.14.this.val$progressDialog.dismiss();
                    AlertDialog.Builder localBuilder = new AlertDialog.Builder(SecretChatHelper.14.this.val$context);
                    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
                    localBuilder.setMessage(LocaleController.getString("CreateEncryptedChatError", 2131493307));
                    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
                    localBuilder.show().setCanceledOnTouchOutside(true);
                    return;
                  }
                  catch (Exception localException)
                  {
                    for (;;)
                    {
                      FileLog.e(localException);
                    }
                  }
                }
              });
            }
          }, 2);
          return;
        }
        SecretChatHelper.this.delayedEncryptedChatUpdates.clear();
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            SecretChatHelper.access$502(SecretChatHelper.this, false);
            if (!((Activity)SecretChatHelper.14.this.val$context).isFinishing()) {}
            try
            {
              SecretChatHelper.14.this.val$progressDialog.dismiss();
              return;
            }
            catch (Exception localException)
            {
              FileLog.e(localException);
            }
          }
        });
      }
    }, 2);
    localAlertDialog.setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        ConnectionsManager.getInstance(SecretChatHelper.this.currentAccount).cancelRequest(i, true);
        try
        {
          paramAnonymousDialogInterface.dismiss();
          return;
        }
        catch (Exception paramAnonymousDialogInterface)
        {
          FileLog.e(paramAnonymousDialogInterface);
        }
      }
    });
    try
    {
      localAlertDialog.show();
      return;
    }
    catch (Exception paramContext) {}
  }
  
  public static class TL_decryptedMessageHolder
    extends TLObject
  {
    public static int constructor = 1431655929;
    public int date;
    public int decryptedWithVersion;
    public TLRPC.EncryptedFile file;
    public TLRPC.TL_decryptedMessageLayer layer;
    public boolean new_key_used;
    
    public void readParams(AbstractSerializedData paramAbstractSerializedData, boolean paramBoolean)
    {
      paramAbstractSerializedData.readInt64(paramBoolean);
      this.date = paramAbstractSerializedData.readInt32(paramBoolean);
      this.layer = TLRPC.TL_decryptedMessageLayer.TLdeserialize(paramAbstractSerializedData, paramAbstractSerializedData.readInt32(paramBoolean), paramBoolean);
      if (paramAbstractSerializedData.readBool(paramBoolean)) {
        this.file = TLRPC.EncryptedFile.TLdeserialize(paramAbstractSerializedData, paramAbstractSerializedData.readInt32(paramBoolean), paramBoolean);
      }
      this.new_key_used = paramAbstractSerializedData.readBool(paramBoolean);
    }
    
    public void serializeToStream(AbstractSerializedData paramAbstractSerializedData)
    {
      paramAbstractSerializedData.writeInt32(constructor);
      paramAbstractSerializedData.writeInt64(0L);
      paramAbstractSerializedData.writeInt32(this.date);
      this.layer.serializeToStream(paramAbstractSerializedData);
      if (this.file != null) {}
      for (boolean bool = true;; bool = false)
      {
        paramAbstractSerializedData.writeBool(bool);
        if (this.file != null) {
          this.file.serializeToStream(paramAbstractSerializedData);
        }
        paramAbstractSerializedData.writeBool(this.new_key_used);
        return;
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/SecretChatHelper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */