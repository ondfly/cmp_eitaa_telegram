package org.telegram.messenger;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.InputChannel;
import org.telegram.tgnet.TLRPC.InputMedia;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PeerNotifySettings;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.ReplyMarkup;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_channels_deleteMessages;
import org.telegram.tgnet.TLRPC.TL_chatChannelParticipant;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.TL_chatInviteEmpty;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_chatParticipants;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_documentEmpty;
import org.telegram.tgnet.TLRPC.TL_inputMediaGame;
import org.telegram.tgnet.TLRPC.TL_inputMessageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionGameScore;
import org.telegram.tgnet.TLRPC.TL_messageActionHistoryClear;
import org.telegram.tgnet.TLRPC.TL_messageActionPaymentSent;
import org.telegram.tgnet.TLRPC.TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported_old;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_messages_botCallbackAnswer;
import org.telegram.tgnet.TLRPC.TL_messages_botResults;
import org.telegram.tgnet.TLRPC.TL_messages_deleteMessages;
import org.telegram.tgnet.TLRPC.TL_messages_dialogs;
import org.telegram.tgnet.TLRPC.TL_messages_messages;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettingsEmpty;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.TL_photos_photos;
import org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifferenceTooLong;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.tgnet.TLRPC.WallPaper;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.tgnet.TLRPC.messages_BotResults;
import org.telegram.tgnet.TLRPC.messages_Dialogs;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.tgnet.TLRPC.photos_Photos;

public class MessagesStorage
{
  private static volatile MessagesStorage[] Instance = new MessagesStorage[3];
  private File cacheFile;
  private int currentAccount;
  private SQLiteDatabase database;
  private int lastDateValue = 0;
  private int lastPtsValue = 0;
  private int lastQtsValue = 0;
  private int lastSavedDate = 0;
  private int lastSavedPts = 0;
  private int lastSavedQts = 0;
  private int lastSavedSeq = 0;
  private int lastSecretVersion = 0;
  private int lastSeqValue = 0;
  private AtomicLong lastTaskId = new AtomicLong(System.currentTimeMillis());
  private CountDownLatch openSync = new CountDownLatch(1);
  private int secretG = 0;
  private byte[] secretPBytes = null;
  private File shmCacheFile;
  private DispatchQueue storageQueue = new DispatchQueue("storageQueue");
  private File walCacheFile;
  
  public MessagesStorage(int paramInt)
  {
    this.currentAccount = paramInt;
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesStorage.this.openDatabase(true);
      }
    });
  }
  
  public static void addUsersAndChatsFromMessage(TLRPC.Message paramMessage, ArrayList<Integer> paramArrayList1, ArrayList<Integer> paramArrayList2)
  {
    if (paramMessage.from_id != 0)
    {
      if (paramMessage.from_id <= 0) {
        break label274;
      }
      if (!paramArrayList1.contains(Integer.valueOf(paramMessage.from_id))) {
        paramArrayList1.add(Integer.valueOf(paramMessage.from_id));
      }
    }
    int i;
    Object localObject;
    for (;;)
    {
      if ((paramMessage.via_bot_id != 0) && (!paramArrayList1.contains(Integer.valueOf(paramMessage.via_bot_id)))) {
        paramArrayList1.add(Integer.valueOf(paramMessage.via_bot_id));
      }
      if (paramMessage.action == null) {
        break;
      }
      if ((paramMessage.action.user_id != 0) && (!paramArrayList1.contains(Integer.valueOf(paramMessage.action.user_id)))) {
        paramArrayList1.add(Integer.valueOf(paramMessage.action.user_id));
      }
      if ((paramMessage.action.channel_id != 0) && (!paramArrayList2.contains(Integer.valueOf(paramMessage.action.channel_id)))) {
        paramArrayList2.add(Integer.valueOf(paramMessage.action.channel_id));
      }
      if ((paramMessage.action.chat_id != 0) && (!paramArrayList2.contains(Integer.valueOf(paramMessage.action.chat_id)))) {
        paramArrayList2.add(Integer.valueOf(paramMessage.action.chat_id));
      }
      if (paramMessage.action.users.isEmpty()) {
        break;
      }
      i = 0;
      while (i < paramMessage.action.users.size())
      {
        localObject = (Integer)paramMessage.action.users.get(i);
        if (!paramArrayList1.contains(localObject)) {
          paramArrayList1.add(localObject);
        }
        i += 1;
      }
      label274:
      if (!paramArrayList2.contains(Integer.valueOf(-paramMessage.from_id))) {
        paramArrayList2.add(Integer.valueOf(-paramMessage.from_id));
      }
    }
    if (!paramMessage.entities.isEmpty())
    {
      i = 0;
      if (i < paramMessage.entities.size())
      {
        localObject = (TLRPC.MessageEntity)paramMessage.entities.get(i);
        if ((localObject instanceof TLRPC.TL_messageEntityMentionName)) {
          paramArrayList1.add(Integer.valueOf(((TLRPC.TL_messageEntityMentionName)localObject).user_id));
        }
        for (;;)
        {
          i += 1;
          break;
          if ((localObject instanceof TLRPC.TL_inputMessageEntityMentionName)) {
            paramArrayList1.add(Integer.valueOf(((TLRPC.TL_inputMessageEntityMentionName)localObject).user_id.user_id));
          }
        }
      }
    }
    if ((paramMessage.media != null) && (paramMessage.media.user_id != 0) && (!paramArrayList1.contains(Integer.valueOf(paramMessage.media.user_id)))) {
      paramArrayList1.add(Integer.valueOf(paramMessage.media.user_id));
    }
    if (paramMessage.fwd_from != null)
    {
      if ((paramMessage.fwd_from.from_id != 0) && (!paramArrayList1.contains(Integer.valueOf(paramMessage.fwd_from.from_id)))) {
        paramArrayList1.add(Integer.valueOf(paramMessage.fwd_from.from_id));
      }
      if ((paramMessage.fwd_from.channel_id != 0) && (!paramArrayList2.contains(Integer.valueOf(paramMessage.fwd_from.channel_id)))) {
        paramArrayList2.add(Integer.valueOf(paramMessage.fwd_from.channel_id));
      }
      if (paramMessage.fwd_from.saved_from_peer != null)
      {
        if (paramMessage.fwd_from.saved_from_peer.user_id == 0) {
          break label639;
        }
        if (!paramArrayList2.contains(Integer.valueOf(paramMessage.fwd_from.saved_from_peer.user_id))) {
          paramArrayList1.add(Integer.valueOf(paramMessage.fwd_from.saved_from_peer.user_id));
        }
      }
    }
    for (;;)
    {
      if ((paramMessage.ttl < 0) && (!paramArrayList2.contains(Integer.valueOf(-paramMessage.ttl)))) {
        paramArrayList2.add(Integer.valueOf(-paramMessage.ttl));
      }
      return;
      label639:
      if (paramMessage.fwd_from.saved_from_peer.channel_id != 0)
      {
        if (!paramArrayList2.contains(Integer.valueOf(paramMessage.fwd_from.saved_from_peer.channel_id))) {
          paramArrayList2.add(Integer.valueOf(paramMessage.fwd_from.saved_from_peer.channel_id));
        }
      }
      else if ((paramMessage.fwd_from.saved_from_peer.chat_id != 0) && (!paramArrayList2.contains(Integer.valueOf(paramMessage.fwd_from.saved_from_peer.chat_id)))) {
        paramArrayList2.add(Integer.valueOf(paramMessage.fwd_from.saved_from_peer.chat_id));
      }
    }
  }
  
  private void cleanupInternal()
  {
    this.lastDateValue = 0;
    this.lastSeqValue = 0;
    this.lastPtsValue = 0;
    this.lastQtsValue = 0;
    this.lastSecretVersion = 0;
    this.lastSavedSeq = 0;
    this.lastSavedPts = 0;
    this.lastSavedDate = 0;
    this.lastSavedQts = 0;
    this.secretPBytes = null;
    this.secretG = 0;
    if (this.database != null)
    {
      this.database.close();
      this.database = null;
    }
    if (this.cacheFile != null)
    {
      this.cacheFile.delete();
      this.cacheFile = null;
    }
    if (this.walCacheFile != null)
    {
      this.walCacheFile.delete();
      this.walCacheFile = null;
    }
    if (this.shmCacheFile != null)
    {
      this.shmCacheFile.delete();
      this.shmCacheFile = null;
    }
  }
  
  private void closeHolesInTable(String paramString, long paramLong, int paramInt1, int paramInt2)
    throws Exception
  {
    Object localObject3;
    Object localObject1;
    Object localObject2;
    int i;
    int j;
    try
    {
      localObject3 = this.database.queryFinalized(String.format(Locale.US, "SELECT start, end FROM " + paramString + " WHERE uid = %d AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2) }), new Object[0]);
      localObject1 = null;
      while (((SQLiteCursor)localObject3).next())
      {
        localObject2 = localObject1;
        if (localObject1 == null) {
          localObject2 = new ArrayList();
        }
        i = ((SQLiteCursor)localObject3).intValue(0);
        j = ((SQLiteCursor)localObject3).intValue(1);
        if (i == j)
        {
          localObject1 = localObject2;
          if (i == 1) {
            break;
          }
        }
        else
        {
          ((ArrayList)localObject2).add(new Hole(i, j));
          localObject1 = localObject2;
          continue;
          return;
        }
      }
    }
    catch (Exception paramString)
    {
      FileLog.e(paramString);
    }
    for (;;)
    {
      ((SQLiteCursor)localObject3).dispose();
      if (localObject1 != null)
      {
        i = 0;
        while (i < ((ArrayList)localObject1).size())
        {
          localObject2 = (Hole)((ArrayList)localObject1).get(i);
          if ((paramInt2 >= ((Hole)localObject2).end - 1) && (paramInt1 <= ((Hole)localObject2).start + 1))
          {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM " + paramString + " WHERE uid = %d AND start = %d AND end = %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(((Hole)localObject2).start), Integer.valueOf(((Hole)localObject2).end) })).stepThis().dispose();
          }
          else if (paramInt2 >= ((Hole)localObject2).end - 1)
          {
            j = ((Hole)localObject2).end;
            if (j != paramInt1) {
              try
              {
                this.database.executeFast(String.format(Locale.US, "UPDATE " + paramString + " SET end = %d WHERE uid = %d AND start = %d AND end = %d", new Object[] { Integer.valueOf(paramInt1), Long.valueOf(paramLong), Integer.valueOf(((Hole)localObject2).start), Integer.valueOf(((Hole)localObject2).end) })).stepThis().dispose();
              }
              catch (Exception localException1)
              {
                FileLog.e(localException1);
              }
            }
          }
          else if (paramInt1 <= localException1.start + 1)
          {
            j = localException1.start;
            if (j != paramInt2) {
              try
              {
                this.database.executeFast(String.format(Locale.US, "UPDATE " + paramString + " SET start = %d WHERE uid = %d AND start = %d AND end = %d", new Object[] { Integer.valueOf(paramInt2), Long.valueOf(paramLong), Integer.valueOf(localException1.start), Integer.valueOf(localException1.end) })).stepThis().dispose();
              }
              catch (Exception localException2)
              {
                FileLog.e(localException2);
              }
            }
          }
          else
          {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM " + paramString + " WHERE uid = %d AND start = %d AND end = %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(localException2.start), Integer.valueOf(localException2.end) })).stepThis().dispose();
            localObject3 = this.database.executeFast("REPLACE INTO " + paramString + " VALUES(?, ?, ?)");
            ((SQLitePreparedStatement)localObject3).requery();
            ((SQLitePreparedStatement)localObject3).bindLong(1, paramLong);
            ((SQLitePreparedStatement)localObject3).bindInteger(2, localException2.start);
            ((SQLitePreparedStatement)localObject3).bindInteger(3, paramInt1);
            ((SQLitePreparedStatement)localObject3).step();
            ((SQLitePreparedStatement)localObject3).requery();
            ((SQLitePreparedStatement)localObject3).bindLong(1, paramLong);
            ((SQLitePreparedStatement)localObject3).bindInteger(2, paramInt2);
            ((SQLitePreparedStatement)localObject3).bindInteger(3, localException2.end);
            ((SQLitePreparedStatement)localObject3).step();
            ((SQLitePreparedStatement)localObject3).dispose();
          }
          i += 1;
        }
      }
    }
  }
  
  public static void createFirstHoles(long paramLong, SQLitePreparedStatement paramSQLitePreparedStatement1, SQLitePreparedStatement paramSQLitePreparedStatement2, int paramInt)
    throws Exception
  {
    paramSQLitePreparedStatement1.requery();
    paramSQLitePreparedStatement1.bindLong(1, paramLong);
    int i;
    if (paramInt == 1)
    {
      i = 1;
      paramSQLitePreparedStatement1.bindInteger(2, i);
      paramSQLitePreparedStatement1.bindInteger(3, paramInt);
      paramSQLitePreparedStatement1.step();
      i = 0;
      label41:
      if (i >= 5) {
        return;
      }
      paramSQLitePreparedStatement2.requery();
      paramSQLitePreparedStatement2.bindLong(1, paramLong);
      paramSQLitePreparedStatement2.bindInteger(2, i);
      if (paramInt != 1) {
        break label107;
      }
    }
    label107:
    for (int j = 1;; j = 0)
    {
      paramSQLitePreparedStatement2.bindInteger(3, j);
      paramSQLitePreparedStatement2.bindInteger(4, paramInt);
      paramSQLitePreparedStatement2.step();
      i += 1;
      break label41;
      i = 0;
      break;
    }
  }
  
  private void doneHolesInTable(String paramString, long paramLong, int paramInt)
    throws Exception
  {
    if (paramInt == 0) {
      this.database.executeFast(String.format(Locale.US, "DELETE FROM " + paramString + " WHERE uid = %d", new Object[] { Long.valueOf(paramLong) })).stepThis().dispose();
    }
    for (;;)
    {
      paramString = this.database.executeFast("REPLACE INTO " + paramString + " VALUES(?, ?, ?)");
      paramString.requery();
      paramString.bindLong(1, paramLong);
      paramString.bindInteger(2, 1);
      paramString.bindInteger(3, 1);
      paramString.step();
      paramString.dispose();
      return;
      this.database.executeFast(String.format(Locale.US, "DELETE FROM " + paramString + " WHERE uid = %d AND start = 0", new Object[] { Long.valueOf(paramLong) })).stepThis().dispose();
    }
  }
  
  private void ensureOpened()
  {
    try
    {
      this.openSync.await();
      return;
    }
    catch (Throwable localThrowable) {}
  }
  
  private void fixNotificationSettings()
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          Object localObject1;
          Object localObject2;
          int i;
          long l;
          try
          {
            LongSparseArray localLongSparseArray = new LongSparseArray();
            localObject1 = MessagesController.getNotificationsSettings(MessagesStorage.this.currentAccount).getAll();
            Iterator localIterator = ((Map)localObject1).entrySet().iterator();
            if (!localIterator.hasNext()) {
              break label239;
            }
            localObject2 = (Map.Entry)localIterator.next();
            String str = (String)((Map.Entry)localObject2).getKey();
            if (!str.startsWith("notify2_")) {
              continue;
            }
            localObject2 = (Integer)((Map.Entry)localObject2).getValue();
            if ((((Integer)localObject2).intValue() != 2) && (((Integer)localObject2).intValue() != 3)) {
              break label225;
            }
            str = str.replace("notify2_", "");
            i = ((Integer)localObject2).intValue();
            if (i == 2)
            {
              l = 1L;
              try
              {
                localLongSparseArray.put(Long.parseLong(str), Long.valueOf(l));
              }
              catch (Exception localException2)
              {
                localException2.printStackTrace();
              }
              continue;
            }
            localObject2 = (Integer)((Map)localObject1).get("notifyuntil_" + localException2);
          }
          catch (Throwable localThrowable)
          {
            FileLog.e(localThrowable);
            return;
          }
          if (localObject2 != null)
          {
            l = ((Integer)localObject2).intValue() << 32 | 1L;
            continue;
            label225:
            i = ((Integer)localObject2).intValue();
            if (i == 3)
            {
              continue;
              try
              {
                label239:
                MessagesStorage.this.database.beginTransaction();
                localObject1 = MessagesStorage.this.database.executeFast("REPLACE INTO dialog_settings VALUES(?, ?)");
                i = 0;
                while (i < localThrowable.size())
                {
                  ((SQLitePreparedStatement)localObject1).requery();
                  ((SQLitePreparedStatement)localObject1).bindLong(1, localThrowable.keyAt(i));
                  ((SQLitePreparedStatement)localObject1).bindLong(2, ((Long)localThrowable.valueAt(i)).longValue());
                  ((SQLitePreparedStatement)localObject1).step();
                  i += 1;
                }
                ((SQLitePreparedStatement)localObject1).dispose();
                MessagesStorage.this.database.commitTransaction();
                return;
              }
              catch (Exception localException1)
              {
                FileLog.e(localException1);
                return;
              }
            }
          }
          else
          {
            l = 1L;
          }
        }
      }
    });
  }
  
  private void fixUnsupportedMedia(TLRPC.Message paramMessage)
  {
    if (paramMessage == null) {}
    do
    {
      do
      {
        return;
        if (!(paramMessage.media instanceof TLRPC.TL_messageMediaUnsupported_old)) {
          break;
        }
      } while (paramMessage.media.bytes.length != 0);
      paramMessage.media.bytes = new byte[1];
      paramMessage.media.bytes[0] = 76;
      return;
    } while (!(paramMessage.media instanceof TLRPC.TL_messageMediaUnsupported));
    paramMessage.media = new TLRPC.TL_messageMediaUnsupported_old();
    paramMessage.media.bytes = new byte[1];
    paramMessage.media.bytes[0] = 76;
    paramMessage.flags |= 0x200;
  }
  
  private String formatUserSearchName(TLRPC.User paramUser)
  {
    StringBuilder localStringBuilder = new StringBuilder("");
    if ((paramUser.first_name != null) && (paramUser.first_name.length() > 0)) {
      localStringBuilder.append(paramUser.first_name);
    }
    if ((paramUser.last_name != null) && (paramUser.last_name.length() > 0))
    {
      if (localStringBuilder.length() > 0) {
        localStringBuilder.append(" ");
      }
      localStringBuilder.append(paramUser.last_name);
    }
    localStringBuilder.append(";;;");
    if ((paramUser.username != null) && (paramUser.username.length() > 0)) {
      localStringBuilder.append(paramUser.username);
    }
    return localStringBuilder.toString().toLowerCase();
  }
  
  public static MessagesStorage getInstance(int paramInt)
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
        localObject1 = new MessagesStorage(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (MessagesStorage)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (MessagesStorage)localObject1;
  }
  
  private int getMessageMediaType(TLRPC.Message paramMessage)
  {
    int j = 0;
    int i;
    if ((paramMessage instanceof TLRPC.TL_message_secret))
    {
      if (((!(paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) && (!MessageObject.isGifMessage(paramMessage))) || (((paramMessage.ttl > 0) && (paramMessage.ttl <= 60)) || (MessageObject.isVoiceMessage(paramMessage)) || (MessageObject.isVideoMessage(paramMessage)) || (MessageObject.isRoundVideoMessage(paramMessage)))) {
        i = 1;
      }
      do
      {
        do
        {
          return i;
          i = j;
        } while ((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto));
        i = j;
      } while (MessageObject.isVideoMessage(paramMessage));
    }
    do
    {
      return -1;
      if (((paramMessage instanceof TLRPC.TL_message)) && (((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) || ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument))) && (paramMessage.media.ttl_seconds != 0)) {
        return 1;
      }
      i = j;
      if ((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) {
        break;
      }
    } while (!MessageObject.isVideoMessage(paramMessage));
    return 0;
  }
  
  private static boolean isEmpty(LongSparseArray<?> paramLongSparseArray)
  {
    return (paramLongSparseArray == null) || (paramLongSparseArray.size() == 0);
  }
  
  private static boolean isEmpty(SparseArray<?> paramSparseArray)
  {
    return (paramSparseArray == null) || (paramSparseArray.size() == 0);
  }
  
  private static boolean isEmpty(SparseIntArray paramSparseIntArray)
  {
    return (paramSparseIntArray == null) || (paramSparseIntArray.size() == 0);
  }
  
  private static boolean isEmpty(List<?> paramList)
  {
    return (paramList == null) || (paramList.isEmpty());
  }
  
  private static boolean isEmpty(SparseLongArray paramSparseLongArray)
  {
    return (paramSparseLongArray == null) || (paramSparseLongArray.size() == 0);
  }
  
  private boolean isValidKeyboardToSave(TLRPC.Message paramMessage)
  {
    return (paramMessage.reply_markup != null) && (!(paramMessage.reply_markup instanceof TLRPC.TL_replyInlineMarkup)) && ((!paramMessage.reply_markup.selective) || (paramMessage.mentioned));
  }
  
  private void loadPendingTasks()
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          SQLiteCursor localSQLiteCursor;
          final long l1;
          NativeByteBuffer localNativeByteBuffer;
          final int i;
          try
          {
            localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT id, data FROM pending_tasks WHERE 1", new Object[0]);
            if (!localSQLiteCursor.next()) {
              break label589;
            }
            l1 = localSQLiteCursor.longValue(0);
            localNativeByteBuffer = localSQLiteCursor.byteBufferValue(1);
            if (localNativeByteBuffer == null) {
              continue;
            }
            i = localNativeByteBuffer.readInt32(false);
            switch (i)
            {
            case 0: 
              localNativeByteBuffer.reuse();
              continue;
              localObject1 = TLRPC.Chat.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          final Object localObject1;
          if (localObject1 != null)
          {
            Utilities.stageQueue.postRunnable(new Runnable()
            {
              public void run()
              {
                MessagesController.getInstance(MessagesStorage.this.currentAccount).loadUnknownChannel(localObject1, l1);
              }
            });
            continue;
            i = localNativeByteBuffer.readInt32(false);
            final int j = localNativeByteBuffer.readInt32(false);
            Utilities.stageQueue.postRunnable(new Runnable()
            {
              public void run()
              {
                MessagesController.getInstance(MessagesStorage.this.currentAccount).getChannelDifference(i, j, l1, null);
              }
            });
            continue;
            localObject1 = new TLRPC.TL_dialog();
            ((TLRPC.TL_dialog)localObject1).id = localNativeByteBuffer.readInt64(false);
            ((TLRPC.TL_dialog)localObject1).top_message = localNativeByteBuffer.readInt32(false);
            ((TLRPC.TL_dialog)localObject1).read_inbox_max_id = localNativeByteBuffer.readInt32(false);
            ((TLRPC.TL_dialog)localObject1).read_outbox_max_id = localNativeByteBuffer.readInt32(false);
            ((TLRPC.TL_dialog)localObject1).unread_count = localNativeByteBuffer.readInt32(false);
            ((TLRPC.TL_dialog)localObject1).last_message_date = localNativeByteBuffer.readInt32(false);
            ((TLRPC.TL_dialog)localObject1).pts = localNativeByteBuffer.readInt32(false);
            ((TLRPC.TL_dialog)localObject1).flags = localNativeByteBuffer.readInt32(false);
            if (i >= 5)
            {
              ((TLRPC.TL_dialog)localObject1).pinned = localNativeByteBuffer.readBool(false);
              ((TLRPC.TL_dialog)localObject1).pinnedNum = localNativeByteBuffer.readInt32(false);
            }
            if (i >= 8) {
              ((TLRPC.TL_dialog)localObject1).unread_mentions_count = localNativeByteBuffer.readInt32(false);
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.getInstance(MessagesStorage.this.currentAccount).checkLastDialogMessage(localObject1, this.val$peer, l1);
              }
            });
            continue;
            long l2 = localNativeByteBuffer.readInt64(false);
            localObject1 = TLRPC.InputPeer.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
            Object localObject2 = (TLRPC.TL_inputMediaGame)TLRPC.InputMedia.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
            SendMessagesHelper.getInstance(MessagesStorage.this.currentAccount).sendGame((TLRPC.InputPeer)localObject1, (TLRPC.TL_inputMediaGame)localObject2, l2, l1);
            continue;
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.getInstance(MessagesStorage.this.currentAccount).pinDialog(this.val$did, this.val$pin, l1, this.val$taskId);
              }
            });
            continue;
            i = localNativeByteBuffer.readInt32(false);
            j = localNativeByteBuffer.readInt32(false);
            localObject1 = TLRPC.InputChannel.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
            Utilities.stageQueue.postRunnable(new Runnable()
            {
              public void run()
              {
                MessagesController.getInstance(MessagesStorage.this.currentAccount).getChannelDifference(i, j, l1, this.val$inputChannel);
              }
            });
            continue;
            i = localNativeByteBuffer.readInt32(false);
            j = localNativeByteBuffer.readInt32(false);
            localObject2 = TLRPC.TL_messages_deleteMessages.TLdeserialize(localNativeByteBuffer, j, false);
            localObject1 = localObject2;
            if (localObject2 == null) {
              localObject1 = TLRPC.TL_channels_deleteMessages.TLdeserialize(localNativeByteBuffer, j, false);
            }
            if (localObject1 == null)
            {
              MessagesStorage.this.removePendingTask(l1);
            }
            else
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MessagesController.getInstance(MessagesStorage.this.currentAccount).deleteMessages(null, null, null, i, true, l1, this.val$finalRequest);
                }
              });
              continue;
              label589:
              localSQLiteCursor.dispose();
              return;
            }
          }
        }
      }
    });
  }
  
  private ArrayList<Long> markMessagesAsDeletedInternal(int paramInt1, int paramInt2)
  {
    for (;;)
    {
      int i;
      try
      {
        ArrayList localArrayList1 = new ArrayList();
        LongSparseArray localLongSparseArray = new LongSparseArray();
        long l1 = paramInt2 | paramInt1 << 32;
        ArrayList localArrayList2 = new ArrayList();
        paramInt2 = UserConfig.getInstance(this.currentAccount).getClientUserId();
        SQLiteCursor localSQLiteCursor = this.database.queryFinalized(String.format(Locale.US, "SELECT uid, data, read_state, out, mention FROM messages WHERE uid = %d AND mid <= %d", new Object[] { Integer.valueOf(-paramInt1), Long.valueOf(l1) }), new Object[0]);
        try
        {
          if (localSQLiteCursor.next())
          {
            l2 = localSQLiteCursor.longValue(0);
            if (l2 == paramInt2) {
              continue;
            }
            i = localSQLiteCursor.intValue(2);
            if (localSQLiteCursor.intValue(3) == 0)
            {
              localObject3 = (Integer[])localLongSparseArray.get(l2);
              localObject1 = localObject3;
              if (localObject3 == null)
              {
                localObject1 = new Integer[2];
                localObject1[0] = Integer.valueOf(0);
                localObject1[1] = Integer.valueOf(0);
                localLongSparseArray.put(l2, localObject1);
              }
              if (i >= 2) {
                break label791;
              }
              localObject3 = localObject1[1];
              localObject1[1] = Integer.valueOf(localObject1[1].intValue() + 1);
              break label791;
              localObject3 = localObject1[0];
              localObject1[0] = Integer.valueOf(localObject1[0].intValue() + 1);
            }
            if ((int)l2 != 0) {
              continue;
            }
            localObject3 = localSQLiteCursor.byteBufferValue(1);
            if (localObject3 == null) {
              continue;
            }
            Object localObject1 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject3, ((NativeByteBuffer)localObject3).readInt32(false), false);
            ((TLRPC.Message)localObject1).readAttachPath((AbstractSerializedData)localObject3, UserConfig.getInstance(this.currentAccount).clientUserId);
            ((NativeByteBuffer)localObject3).reuse();
            if (localObject1 == null) {
              continue;
            }
            if (!(((TLRPC.Message)localObject1).media instanceof TLRPC.TL_messageMediaPhoto)) {
              continue;
            }
            localObject1 = ((TLRPC.Message)localObject1).media.photo.sizes.iterator();
            if (!((Iterator)localObject1).hasNext()) {
              continue;
            }
            localObject3 = FileLoader.getPathToAttach((TLRPC.PhotoSize)((Iterator)localObject1).next());
            if ((localObject3 == null) || (((File)localObject3).toString().length() <= 0)) {
              continue;
            }
            localArrayList2.add(localObject3);
            continue;
          }
        }
        catch (Exception localException1)
        {
          long l2;
          Object localObject3;
          FileLog.e(localException1);
          localSQLiteCursor.dispose();
          FileLoader.getInstance(this.currentAccount).deleteFiles(localArrayList2, 0);
          paramInt2 = 0;
          if (paramInt2 < localLongSparseArray.size())
          {
            l2 = localLongSparseArray.keyAt(paramInt2);
            Object localObject2 = (Integer[])localLongSparseArray.valueAt(paramInt2);
            localObject3 = this.database.queryFinalized("SELECT unread_count, unread_count_i FROM dialogs WHERE did = " + l2, new Object[0]);
            int j = 0;
            i = 0;
            if (((SQLiteCursor)localObject3).next())
            {
              j = ((SQLiteCursor)localObject3).intValue(0);
              i = ((SQLiteCursor)localObject3).intValue(1);
            }
            ((SQLiteCursor)localObject3).dispose();
            localArrayList1.add(Long.valueOf(l2));
            localObject3 = this.database.executeFast("UPDATE dialogs SET unread_count = ?, unread_count_i = ? WHERE did = ?");
            ((SQLitePreparedStatement)localObject3).requery();
            ((SQLitePreparedStatement)localObject3).bindInteger(1, Math.max(0, j - localObject2[0].intValue()));
            ((SQLitePreparedStatement)localObject3).bindInteger(2, Math.max(0, i - localObject2[1].intValue()));
            ((SQLitePreparedStatement)localObject3).bindLong(3, l2);
            ((SQLitePreparedStatement)localObject3).step();
            ((SQLitePreparedStatement)localObject3).dispose();
            paramInt2 += 1;
            continue;
            if (!(((TLRPC.Message)localObject2).media instanceof TLRPC.TL_messageMediaDocument)) {
              continue;
            }
            localObject3 = FileLoader.getPathToAttach(((TLRPC.Message)localObject2).media.document);
            if ((localObject3 != null) && (((File)localObject3).toString().length() > 0)) {
              localArrayList2.add(localObject3);
            }
            localObject2 = FileLoader.getPathToAttach(((TLRPC.Message)localObject2).media.document.thumb);
            if ((localObject2 == null) || (((File)localObject2).toString().length() <= 0)) {
              continue;
            }
            localArrayList2.add(localObject2);
            continue;
          }
          this.database.executeFast(String.format(Locale.US, "DELETE FROM messages WHERE uid = %d AND mid <= %d", new Object[] { Integer.valueOf(-paramInt1), Long.valueOf(l1) })).stepThis().dispose();
          this.database.executeFast(String.format(Locale.US, "DELETE FROM media_v2 WHERE uid = %d AND mid <= %d", new Object[] { Integer.valueOf(-paramInt1), Long.valueOf(l1) })).stepThis().dispose();
          this.database.executeFast("DELETE FROM media_counts_v2 WHERE 1").stepThis().dispose();
          return localArrayList1;
        }
        if (i == 0) {
          continue;
        }
      }
      catch (Exception localException2)
      {
        FileLog.e(localException2);
        return null;
      }
      label791:
      if (i != 2) {}
    }
  }
  
  private ArrayList<Long> markMessagesAsDeletedInternal(ArrayList<Integer> paramArrayList, int paramInt)
  {
    for (;;)
    {
      int i;
      try
      {
        ArrayList localArrayList1 = new ArrayList();
        LongSparseArray localLongSparseArray = new LongSparseArray();
        ArrayList localArrayList2;
        if (paramInt != 0)
        {
          localObject1 = new StringBuilder(paramArrayList.size());
          i = 0;
          if (i < paramArrayList.size())
          {
            l1 = ((Integer)paramArrayList.get(i)).intValue();
            long l2 = paramInt;
            if (((StringBuilder)localObject1).length() > 0) {
              ((StringBuilder)localObject1).append(',');
            }
            ((StringBuilder)localObject1).append(l1 | l2 << 32);
            i += 1;
            continue;
          }
          localObject1 = ((StringBuilder)localObject1).toString();
          localArrayList2 = new ArrayList();
          paramInt = UserConfig.getInstance(this.currentAccount).getClientUserId();
          SQLiteCursor localSQLiteCursor = this.database.queryFinalized(String.format(Locale.US, "SELECT uid, data, read_state, out, mention FROM messages WHERE mid IN(%s)", new Object[] { localObject1 }), new Object[0]);
          try
          {
            if (localSQLiteCursor.next())
            {
              l1 = localSQLiteCursor.longValue(0);
              if (l1 == paramInt) {
                continue;
              }
              i = localSQLiteCursor.intValue(2);
              if (localSQLiteCursor.intValue(3) == 0)
              {
                localObject4 = (Integer[])localLongSparseArray.get(l1);
                localObject2 = localObject4;
                if (localObject4 == null)
                {
                  localObject2 = new Integer[2];
                  localObject2[0] = Integer.valueOf(0);
                  localObject2[1] = Integer.valueOf(0);
                  localLongSparseArray.put(l1, localObject2);
                }
                if (i >= 2) {
                  break label921;
                }
                localObject4 = localObject2[1];
                localObject2[1] = Integer.valueOf(localObject2[1].intValue() + 1);
                break label921;
                localObject4 = localObject2[0];
                localObject2[0] = Integer.valueOf(localObject2[0].intValue() + 1);
              }
              if ((int)l1 != 0) {
                continue;
              }
              localObject4 = localSQLiteCursor.byteBufferValue(1);
              if (localObject4 == null) {
                continue;
              }
              Object localObject2 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject4, ((NativeByteBuffer)localObject4).readInt32(false), false);
              ((TLRPC.Message)localObject2).readAttachPath((AbstractSerializedData)localObject4, UserConfig.getInstance(this.currentAccount).clientUserId);
              ((NativeByteBuffer)localObject4).reuse();
              if (localObject2 == null) {
                continue;
              }
              if (!(((TLRPC.Message)localObject2).media instanceof TLRPC.TL_messageMediaPhoto)) {
                continue;
              }
              localObject2 = ((TLRPC.Message)localObject2).media.photo.sizes.iterator();
              if (!((Iterator)localObject2).hasNext()) {
                continue;
              }
              localObject4 = FileLoader.getPathToAttach((TLRPC.PhotoSize)((Iterator)localObject2).next());
              if ((localObject4 == null) || (((File)localObject4).toString().length() <= 0)) {
                continue;
              }
              localArrayList2.add(localObject4);
              continue;
            }
            if (paramInt >= localLongSparseArray.size()) {
              continue;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            localSQLiteCursor.dispose();
            FileLoader.getInstance(this.currentAccount).deleteFiles(localArrayList2, 0);
            paramInt = 0;
          }
          long l1 = localLongSparseArray.keyAt(paramInt);
          localObject3 = (Integer[])localLongSparseArray.valueAt(paramInt);
          localObject4 = this.database.queryFinalized("SELECT unread_count, unread_count_i FROM dialogs WHERE did = " + l1, new Object[0]);
          int j = 0;
          i = 0;
          if (((SQLiteCursor)localObject4).next())
          {
            j = ((SQLiteCursor)localObject4).intValue(0);
            i = ((SQLiteCursor)localObject4).intValue(1);
          }
          ((SQLiteCursor)localObject4).dispose();
          localArrayList1.add(Long.valueOf(l1));
          localObject4 = this.database.executeFast("UPDATE dialogs SET unread_count = ?, unread_count_i = ? WHERE did = ?");
          ((SQLitePreparedStatement)localObject4).requery();
          ((SQLitePreparedStatement)localObject4).bindInteger(1, Math.max(0, j - localObject3[0].intValue()));
          ((SQLitePreparedStatement)localObject4).bindInteger(2, Math.max(0, i - localObject3[1].intValue()));
          ((SQLitePreparedStatement)localObject4).bindLong(3, l1);
          ((SQLitePreparedStatement)localObject4).step();
          ((SQLitePreparedStatement)localObject4).dispose();
          paramInt += 1;
          continue;
        }
        Object localObject1 = TextUtils.join(",", paramArrayList);
        continue;
        if (!(((TLRPC.Message)localObject3).media instanceof TLRPC.TL_messageMediaDocument)) {
          continue;
        }
        Object localObject4 = FileLoader.getPathToAttach(((TLRPC.Message)localObject3).media.document);
        if ((localObject4 != null) && (((File)localObject4).toString().length() > 0)) {
          localArrayList2.add(localObject4);
        }
        Object localObject3 = FileLoader.getPathToAttach(((TLRPC.Message)localObject3).media.document.thumb);
        if ((localObject3 == null) || (((File)localObject3).toString().length() <= 0)) {
          continue;
        }
        localArrayList2.add(localObject3);
        continue;
        this.database.executeFast(String.format(Locale.US, "DELETE FROM messages WHERE mid IN(%s)", new Object[] { localObject1 })).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM bot_keyboard WHERE mid IN(%s)", new Object[] { localObject1 })).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM messages_seq WHERE mid IN(%s)", new Object[] { localObject1 })).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM media_v2 WHERE mid IN(%s)", new Object[] { localObject1 })).stepThis().dispose();
        this.database.executeFast("DELETE FROM media_counts_v2 WHERE 1").stepThis().dispose();
        DataQuery.getInstance(this.currentAccount).clearBotKeyboard(0L, paramArrayList);
        return localArrayList1;
      }
      catch (Exception paramArrayList)
      {
        FileLog.e(paramArrayList);
        return null;
      }
      label921:
      if (i != 0) {
        if (i != 2) {}
      }
    }
  }
  
  private void markMessagesAsReadInternal(SparseLongArray paramSparseLongArray1, SparseLongArray paramSparseLongArray2, SparseIntArray paramSparseIntArray)
  {
    try
    {
      int i;
      int j;
      long l;
      if (!isEmpty(paramSparseLongArray1))
      {
        i = 0;
        while (i < paramSparseLongArray1.size())
        {
          j = paramSparseLongArray1.keyAt(i);
          l = paramSparseLongArray1.get(j);
          this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 1 WHERE uid = %d AND mid > 0 AND mid <= %d AND read_state IN(0,2) AND out = 0", new Object[] { Integer.valueOf(j), Long.valueOf(l) })).stepThis().dispose();
          i += 1;
        }
      }
      if (!isEmpty(paramSparseLongArray2))
      {
        i = 0;
        while (i < paramSparseLongArray2.size())
        {
          j = paramSparseLongArray2.keyAt(i);
          l = paramSparseLongArray2.get(j);
          this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 1 WHERE uid = %d AND mid > 0 AND mid <= %d AND read_state IN(0,2) AND out = 1", new Object[] { Integer.valueOf(j), Long.valueOf(l) })).stepThis().dispose();
          i += 1;
        }
      }
      if ((paramSparseIntArray != null) && (!isEmpty(paramSparseIntArray)))
      {
        i = 0;
        while (i < paramSparseIntArray.size())
        {
          l = paramSparseIntArray.keyAt(i);
          j = paramSparseIntArray.valueAt(i);
          paramSparseLongArray1 = this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND date <= ? AND read_state IN(0,2) AND out = 1");
          paramSparseLongArray1.requery();
          paramSparseLongArray1.bindLong(1, l << 32);
          paramSparseLongArray1.bindInteger(2, j);
          paramSparseLongArray1.step();
          paramSparseLongArray1.dispose();
          i += 1;
        }
      }
      return;
    }
    catch (Exception paramSparseLongArray1)
    {
      FileLog.e(paramSparseLongArray1);
    }
  }
  
  private void putChatsInternal(ArrayList<TLRPC.Chat> paramArrayList)
    throws Exception
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    SQLitePreparedStatement localSQLitePreparedStatement = this.database.executeFast("REPLACE INTO chats VALUES(?, ?, ?)");
    int i = 0;
    Object localObject3;
    if (i < paramArrayList.size())
    {
      localObject3 = (TLRPC.Chat)paramArrayList.get(i);
      Object localObject1 = localObject3;
      SQLiteCursor localSQLiteCursor;
      if (((TLRPC.Chat)localObject3).min)
      {
        localSQLiteCursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM chats WHERE uid = %d", new Object[] { Integer.valueOf(((TLRPC.Chat)localObject3).id) }), new Object[0]);
        localObject1 = localObject3;
        if (!localSQLiteCursor.next()) {}
      }
      for (;;)
      {
        try
        {
          NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
          localObject1 = localObject3;
          if (localNativeByteBuffer != null)
          {
            localChat = TLRPC.Chat.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
            localNativeByteBuffer.reuse();
            localObject1 = localObject3;
            if (localChat != null)
            {
              localChat.title = ((TLRPC.Chat)localObject3).title;
              localChat.photo = ((TLRPC.Chat)localObject3).photo;
              localChat.broadcast = ((TLRPC.Chat)localObject3).broadcast;
              localChat.verified = ((TLRPC.Chat)localObject3).verified;
              localChat.megagroup = ((TLRPC.Chat)localObject3).megagroup;
              localChat.democracy = ((TLRPC.Chat)localObject3).democracy;
              if (((TLRPC.Chat)localObject3).username == null) {
                continue;
              }
              localChat.username = ((TLRPC.Chat)localObject3).username;
              localChat.flags |= 0x40;
              localObject1 = localChat;
            }
          }
        }
        catch (Exception localException)
        {
          TLRPC.Chat localChat;
          FileLog.e(localException);
          Object localObject2 = localObject3;
          continue;
          localSQLitePreparedStatement.bindString(2, "");
          continue;
        }
        localSQLiteCursor.dispose();
        localSQLitePreparedStatement.requery();
        localObject3 = new NativeByteBuffer(((TLRPC.Chat)localObject1).getObjectSize());
        ((TLRPC.Chat)localObject1).serializeToStream((AbstractSerializedData)localObject3);
        localSQLitePreparedStatement.bindInteger(1, ((TLRPC.Chat)localObject1).id);
        if (((TLRPC.Chat)localObject1).title == null) {
          continue;
        }
        localSQLitePreparedStatement.bindString(2, ((TLRPC.Chat)localObject1).title.toLowerCase());
        localSQLitePreparedStatement.bindByteBuffer(3, (NativeByteBuffer)localObject3);
        localSQLitePreparedStatement.step();
        ((NativeByteBuffer)localObject3).reuse();
        i += 1;
        break;
        localChat.username = null;
        localChat.flags &= 0xFFFFFFBF;
      }
    }
    localSQLitePreparedStatement.dispose();
  }
  
  private void putDialogsInternal(TLRPC.messages_Dialogs parammessages_Dialogs, boolean paramBoolean)
  {
    LongSparseArray localLongSparseArray;
    int i;
    Object localObject1;
    SQLitePreparedStatement localSQLitePreparedStatement1;
    SQLitePreparedStatement localSQLitePreparedStatement2;
    SQLitePreparedStatement localSQLitePreparedStatement3;
    SQLitePreparedStatement localSQLitePreparedStatement4;
    SQLitePreparedStatement localSQLitePreparedStatement5;
    TLRPC.TL_dialog localTL_dialog;
    for (;;)
    {
      try
      {
        this.database.beginTransaction();
        localLongSparseArray = new LongSparseArray(parammessages_Dialogs.messages.size());
        i = 0;
        if (i < parammessages_Dialogs.messages.size())
        {
          localObject1 = (TLRPC.Message)parammessages_Dialogs.messages.get(i);
          localLongSparseArray.put(MessageObject.getDialogId((TLRPC.Message)localObject1), localObject1);
          i += 1;
        }
        else
        {
          if (parammessages_Dialogs.dialogs.isEmpty()) {
            break label951;
          }
          localObject1 = this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, ?)");
          localSQLitePreparedStatement1 = this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
          localSQLitePreparedStatement2 = this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
          localSQLitePreparedStatement3 = this.database.executeFast("REPLACE INTO dialog_settings VALUES(?, ?)");
          localSQLitePreparedStatement4 = this.database.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
          localSQLitePreparedStatement5 = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
          i = 0;
          if (i >= parammessages_Dialogs.dialogs.size()) {
            break label921;
          }
          localTL_dialog = (TLRPC.TL_dialog)parammessages_Dialogs.dialogs.get(i);
          if (localTL_dialog.id == 0L)
          {
            if (localTL_dialog.peer.user_id != 0) {
              localTL_dialog.id = localTL_dialog.peer.user_id;
            }
          }
          else
          {
            if (!paramBoolean) {
              break;
            }
            localObject2 = this.database.queryFinalized("SELECT did FROM dialogs WHERE did = " + localTL_dialog.id, new Object[0]);
            boolean bool = ((SQLiteCursor)localObject2).next();
            ((SQLiteCursor)localObject2).dispose();
            if (!bool) {
              break;
            }
            break label975;
          }
          if (localTL_dialog.peer.chat_id != 0) {
            localTL_dialog.id = (-localTL_dialog.peer.chat_id);
          } else {
            localTL_dialog.id = (-localTL_dialog.peer.channel_id);
          }
        }
      }
      catch (Exception parammessages_Dialogs)
      {
        FileLog.e(parammessages_Dialogs);
        return;
      }
    }
    int j = 0;
    Object localObject2 = (TLRPC.Message)localLongSparseArray.get(localTL_dialog.id);
    long l2;
    long l1;
    if (localObject2 != null)
    {
      int k = Math.max(((TLRPC.Message)localObject2).date, 0);
      if (isValidKeyboardToSave((TLRPC.Message)localObject2)) {
        DataQuery.getInstance(this.currentAccount).putBotKeyboard(localTL_dialog.id, (TLRPC.Message)localObject2);
      }
      fixUnsupportedMedia((TLRPC.Message)localObject2);
      NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(((TLRPC.Message)localObject2).getObjectSize());
      ((TLRPC.Message)localObject2).serializeToStream(localNativeByteBuffer);
      l2 = ((TLRPC.Message)localObject2).id;
      l1 = l2;
      if (((TLRPC.Message)localObject2).to_id.channel_id != 0) {
        l1 = l2 | ((TLRPC.Message)localObject2).to_id.channel_id << 32;
      }
      ((SQLitePreparedStatement)localObject1).requery();
      ((SQLitePreparedStatement)localObject1).bindLong(1, l1);
      ((SQLitePreparedStatement)localObject1).bindLong(2, localTL_dialog.id);
      ((SQLitePreparedStatement)localObject1).bindInteger(3, MessageObject.getUnreadFlags((TLRPC.Message)localObject2));
      ((SQLitePreparedStatement)localObject1).bindInteger(4, ((TLRPC.Message)localObject2).send_state);
      ((SQLitePreparedStatement)localObject1).bindInteger(5, ((TLRPC.Message)localObject2).date);
      ((SQLitePreparedStatement)localObject1).bindByteBuffer(6, localNativeByteBuffer);
      if (MessageObject.isOut((TLRPC.Message)localObject2))
      {
        j = 1;
        label529:
        ((SQLitePreparedStatement)localObject1).bindInteger(7, j);
        ((SQLitePreparedStatement)localObject1).bindInteger(8, 0);
        if ((((TLRPC.Message)localObject2).flags & 0x400) == 0) {
          break label988;
        }
        j = ((TLRPC.Message)localObject2).views;
        label565:
        ((SQLitePreparedStatement)localObject1).bindInteger(9, j);
        ((SQLitePreparedStatement)localObject1).bindInteger(10, 0);
        if (!((TLRPC.Message)localObject2).mentioned) {
          break label994;
        }
        j = 1;
        label593:
        ((SQLitePreparedStatement)localObject1).bindInteger(11, j);
        ((SQLitePreparedStatement)localObject1).step();
        if (DataQuery.canAddMessageToMedia((TLRPC.Message)localObject2))
        {
          localSQLitePreparedStatement2.requery();
          localSQLitePreparedStatement2.bindLong(1, l1);
          localSQLitePreparedStatement2.bindLong(2, localTL_dialog.id);
          localSQLitePreparedStatement2.bindInteger(3, ((TLRPC.Message)localObject2).date);
          localSQLitePreparedStatement2.bindInteger(4, DataQuery.getMediaType((TLRPC.Message)localObject2));
          localSQLitePreparedStatement2.bindByteBuffer(5, localNativeByteBuffer);
          localSQLitePreparedStatement2.step();
        }
        localNativeByteBuffer.reuse();
        createFirstHoles(localTL_dialog.id, localSQLitePreparedStatement4, localSQLitePreparedStatement5, ((TLRPC.Message)localObject2).id);
        j = k;
      }
    }
    else
    {
      l2 = localTL_dialog.top_message;
      l1 = l2;
      if (localTL_dialog.peer.channel_id != 0) {
        l1 = l2 | localTL_dialog.peer.channel_id << 32;
      }
      localSQLitePreparedStatement1.requery();
      localSQLitePreparedStatement1.bindLong(1, localTL_dialog.id);
      localSQLitePreparedStatement1.bindInteger(2, j);
      localSQLitePreparedStatement1.bindInteger(3, localTL_dialog.unread_count);
      localSQLitePreparedStatement1.bindLong(4, l1);
      localSQLitePreparedStatement1.bindInteger(5, localTL_dialog.read_inbox_max_id);
      localSQLitePreparedStatement1.bindInteger(6, localTL_dialog.read_outbox_max_id);
      localSQLitePreparedStatement1.bindLong(7, 0L);
      localSQLitePreparedStatement1.bindInteger(8, localTL_dialog.unread_mentions_count);
      localSQLitePreparedStatement1.bindInteger(9, localTL_dialog.pts);
      localSQLitePreparedStatement1.bindInteger(10, 0);
      localSQLitePreparedStatement1.bindInteger(11, localTL_dialog.pinnedNum);
      localSQLitePreparedStatement1.step();
      if (localTL_dialog.notify_settings != null)
      {
        localSQLitePreparedStatement3.requery();
        localSQLitePreparedStatement3.bindLong(1, localTL_dialog.id);
        if (localTL_dialog.notify_settings.mute_until == 0) {
          break label1000;
        }
      }
    }
    label921:
    label951:
    label975:
    label988:
    label994:
    label1000:
    for (j = 1;; j = 0)
    {
      localSQLitePreparedStatement3.bindInteger(2, j);
      localSQLitePreparedStatement3.step();
      break label975;
      ((SQLitePreparedStatement)localObject1).dispose();
      localSQLitePreparedStatement1.dispose();
      localSQLitePreparedStatement2.dispose();
      localSQLitePreparedStatement3.dispose();
      localSQLitePreparedStatement4.dispose();
      localSQLitePreparedStatement5.dispose();
      putUsersInternal(parammessages_Dialogs.users);
      putChatsInternal(parammessages_Dialogs.chats);
      this.database.commitTransaction();
      return;
      i += 1;
      break;
      j = 0;
      break label529;
      j = 0;
      break label565;
      j = 0;
      break label593;
    }
  }
  
  private void putMessagesInternal(ArrayList<TLRPC.Message> paramArrayList, boolean paramBoolean1, boolean paramBoolean2, int paramInt, boolean paramBoolean3)
  {
    if (paramBoolean3) {
      for (;;)
      {
        try
        {
          localObject1 = (TLRPC.Message)paramArrayList.get(0);
          if (((TLRPC.Message)localObject1).dialog_id == 0L)
          {
            if (((TLRPC.Message)localObject1).to_id.user_id != 0) {
              ((TLRPC.Message)localObject1).dialog_id = ((TLRPC.Message)localObject1).to_id.user_id;
            }
          }
          else
          {
            i = -1;
            localObject1 = this.database.queryFinalized("SELECT last_mid FROM dialogs WHERE did = " + ((TLRPC.Message)localObject1).dialog_id, new Object[0]);
            if (((SQLiteCursor)localObject1).next()) {
              i = ((SQLiteCursor)localObject1).intValue(0);
            }
            ((SQLiteCursor)localObject1).dispose();
            if (i == 0) {
              break;
            }
            return;
          }
          if (((TLRPC.Message)localObject1).to_id.chat_id != 0) {
            ((TLRPC.Message)localObject1).dialog_id = (-((TLRPC.Message)localObject1).to_id.chat_id);
          } else {
            ((TLRPC.Message)localObject1).dialog_id = (-((TLRPC.Message)localObject1).to_id.channel_id);
          }
        }
        catch (Exception paramArrayList)
        {
          FileLog.e(paramArrayList);
          return;
        }
      }
    }
    if (paramBoolean1) {
      this.database.beginTransaction();
    }
    LongSparseArray localLongSparseArray3 = new LongSparseArray();
    LongSparseArray localLongSparseArray1 = new LongSparseArray();
    LongSparseArray localLongSparseArray2 = new LongSparseArray();
    Object localObject8 = null;
    LongSparseArray localLongSparseArray6 = new LongSparseArray();
    Object localObject1 = null;
    Object localObject3 = null;
    Object localObject2 = null;
    StringBuilder localStringBuilder = new StringBuilder();
    LongSparseArray localLongSparseArray7 = new LongSparseArray();
    LongSparseArray localLongSparseArray5 = new LongSparseArray();
    LongSparseArray localLongSparseArray4 = new LongSparseArray();
    SQLitePreparedStatement localSQLitePreparedStatement1 = this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, ?)");
    Object localObject7 = null;
    SQLitePreparedStatement localSQLitePreparedStatement2 = this.database.executeFast("REPLACE INTO randoms VALUES(?, ?)");
    SQLitePreparedStatement localSQLitePreparedStatement3 = this.database.executeFast("REPLACE INTO download_queue VALUES(?, ?, ?, ?)");
    SQLitePreparedStatement localSQLitePreparedStatement4 = this.database.executeFast("REPLACE INTO webpage_pending VALUES(?, ?)");
    int i = 0;
    long l2;
    long l1;
    Object localObject5;
    Object localObject4;
    Object localObject6;
    label861:
    label870:
    label1330:
    label1408:
    int k;
    int m;
    label1765:
    label1829:
    final int j;
    label2342:
    label2729:
    label2838:
    int i4;
    int n;
    if (i < paramArrayList.size())
    {
      TLRPC.Message localMessage = (TLRPC.Message)paramArrayList.get(i);
      l2 = localMessage.id;
      if (localMessage.dialog_id == 0L)
      {
        if (localMessage.to_id.user_id != 0) {
          localMessage.dialog_id = localMessage.to_id.user_id;
        }
      }
      else
      {
        l1 = l2;
        if (localMessage.to_id.channel_id != 0) {
          l1 = l2 | localMessage.to_id.channel_id << 32;
        }
        if ((localMessage.mentioned) && (localMessage.media_unread)) {
          localLongSparseArray4.put(l1, Long.valueOf(localMessage.dialog_id));
        }
        if ((!(localMessage.action instanceof TLRPC.TL_messageActionHistoryClear)) && (!MessageObject.isOut(localMessage)) && ((localMessage.id > 0) || (MessageObject.isUnread(localMessage))))
        {
          localObject5 = (Integer)localLongSparseArray7.get(localMessage.dialog_id);
          localObject4 = localObject5;
          if (localObject5 == null)
          {
            localObject5 = this.database.queryFinalized("SELECT inbox_max FROM dialogs WHERE did = " + localMessage.dialog_id, new Object[0]);
            if (!((SQLiteCursor)localObject5).next()) {
              break label861;
            }
          }
        }
      }
      for (localObject4 = Integer.valueOf(((SQLiteCursor)localObject5).intValue(0));; localObject4 = Integer.valueOf(0))
      {
        ((SQLiteCursor)localObject5).dispose();
        localLongSparseArray7.put(localMessage.dialog_id, localObject4);
        if ((localMessage.id < 0) || (((Integer)localObject4).intValue() < localMessage.id))
        {
          if (localStringBuilder.length() > 0) {
            localStringBuilder.append(",");
          }
          localStringBuilder.append(l1);
          localLongSparseArray5.put(l1, Long.valueOf(localMessage.dialog_id));
        }
        localObject6 = localObject2;
        localObject4 = localObject3;
        localObject5 = localObject1;
        if (DataQuery.canAddMessageToMedia(localMessage))
        {
          localObject4 = localObject3;
          if (localObject3 == null)
          {
            localObject4 = new StringBuilder();
            localObject1 = new LongSparseArray();
            localObject2 = new LongSparseArray();
          }
          if (((StringBuilder)localObject4).length() > 0) {
            ((StringBuilder)localObject4).append(",");
          }
          ((StringBuilder)localObject4).append(l1);
          ((LongSparseArray)localObject1).put(l1, Long.valueOf(localMessage.dialog_id));
          ((LongSparseArray)localObject2).put(l1, Integer.valueOf(DataQuery.getMediaType(localMessage)));
          localObject5 = localObject1;
          localObject6 = localObject2;
        }
        if (!isValidKeyboardToSave(localMessage)) {
          break label3530;
        }
        localObject1 = (TLRPC.Message)localLongSparseArray6.get(localMessage.dialog_id);
        if ((localObject1 != null) && (((TLRPC.Message)localObject1).id >= localMessage.id)) {
          break label3530;
        }
        localLongSparseArray6.put(localMessage.dialog_id, localMessage);
        break label3530;
        if (localMessage.to_id.chat_id != 0)
        {
          localMessage.dialog_id = (-localMessage.to_id.chat_id);
          break;
        }
        localMessage.dialog_id = (-localMessage.to_id.channel_id);
        break;
      }
      while (i < localLongSparseArray6.size())
      {
        DataQuery.getInstance(this.currentAccount).putBotKeyboard(localLongSparseArray6.keyAt(i), (TLRPC.Message)localLongSparseArray6.valueAt(i));
        i += 1;
      }
      localObject4 = localObject8;
      if (localObject3 != null)
      {
        localObject3 = this.database.queryFinalized("SELECT mid FROM media_v2 WHERE mid IN(" + ((StringBuilder)localObject3).toString() + ")", new Object[0]);
        while (((SQLiteCursor)localObject3).next()) {
          ((LongSparseArray)localObject1).remove(((SQLiteCursor)localObject3).longValue(0));
        }
        ((SQLiteCursor)localObject3).dispose();
        localObject6 = new SparseArray();
        i = 0;
        localObject4 = localObject6;
        if (i < ((LongSparseArray)localObject1).size())
        {
          l1 = ((LongSparseArray)localObject1).keyAt(i);
          l2 = ((Long)((LongSparseArray)localObject1).valueAt(i)).longValue();
          localObject5 = (Integer)((LongSparseArray)localObject2).get(l1);
          localObject4 = (LongSparseArray)((SparseArray)localObject6).get(((Integer)localObject5).intValue());
          if (localObject4 == null)
          {
            localObject4 = new LongSparseArray();
            localObject3 = Integer.valueOf(0);
            ((SparseArray)localObject6).put(((Integer)localObject5).intValue(), localObject4);
          }
          for (;;)
          {
            localObject5 = localObject3;
            if (localObject3 == null) {
              localObject5 = Integer.valueOf(0);
            }
            ((LongSparseArray)localObject4).put(l2, Integer.valueOf(((Integer)localObject5).intValue() + 1));
            i += 1;
            break;
            localObject3 = (Integer)((LongSparseArray)localObject4).get(l2);
          }
        }
      }
      if (localStringBuilder.length() <= 0) {
        break label3563;
      }
      localObject1 = this.database.queryFinalized("SELECT mid FROM messages WHERE mid IN(" + localStringBuilder.toString() + ")", new Object[0]);
      while (((SQLiteCursor)localObject1).next())
      {
        l1 = ((SQLiteCursor)localObject1).longValue(0);
        localLongSparseArray5.remove(l1);
        localLongSparseArray4.remove(l1);
      }
      ((SQLiteCursor)localObject1).dispose();
      i = 0;
      while (i < localLongSparseArray5.size())
      {
        l1 = ((Long)localLongSparseArray5.valueAt(i)).longValue();
        localObject2 = (Integer)localLongSparseArray1.get(l1);
        localObject1 = localObject2;
        if (localObject2 == null) {
          localObject1 = Integer.valueOf(0);
        }
        localLongSparseArray1.put(l1, Integer.valueOf(((Integer)localObject1).intValue() + 1));
        i += 1;
        continue;
        for (;;)
        {
          if (i < localLongSparseArray4.size())
          {
            l1 = ((Long)localLongSparseArray4.valueAt(i)).longValue();
            localObject2 = (Integer)localLongSparseArray2.get(l1);
            localObject1 = localObject2;
            if (localObject2 == null) {
              localObject1 = Integer.valueOf(0);
            }
            localLongSparseArray2.put(l1, Integer.valueOf(((Integer)localObject1).intValue() + 1));
            i += 1;
            continue;
            if (k < paramArrayList.size())
            {
              localObject5 = (TLRPC.Message)paramArrayList.get(k);
              fixUnsupportedMedia((TLRPC.Message)localObject5);
              localSQLitePreparedStatement1.requery();
              l1 = ((TLRPC.Message)localObject5).id;
              if (((TLRPC.Message)localObject5).local_id != 0) {
                l1 = ((TLRPC.Message)localObject5).local_id;
              }
              l2 = l1;
              if (((TLRPC.Message)localObject5).to_id.channel_id != 0) {
                l2 = l1 | ((TLRPC.Message)localObject5).to_id.channel_id << 32;
              }
              localObject3 = new NativeByteBuffer(((TLRPC.Message)localObject5).getObjectSize());
              ((TLRPC.Message)localObject5).serializeToStream((AbstractSerializedData)localObject3);
              m = 1;
              i = m;
              if (((TLRPC.Message)localObject5).action != null)
              {
                i = m;
                if ((((TLRPC.Message)localObject5).action instanceof TLRPC.TL_messageEncryptedAction))
                {
                  i = m;
                  if (!(((TLRPC.Message)localObject5).action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL))
                  {
                    i = m;
                    if (!(((TLRPC.Message)localObject5).action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages)) {
                      i = 0;
                    }
                  }
                }
              }
              if (i != 0)
              {
                localObject2 = (TLRPC.Message)localLongSparseArray3.get(((TLRPC.Message)localObject5).dialog_id);
                if ((localObject2 == null) || (((TLRPC.Message)localObject5).date > ((TLRPC.Message)localObject2).date) || ((((TLRPC.Message)localObject5).id > 0) && (((TLRPC.Message)localObject2).id > 0) && (((TLRPC.Message)localObject5).id > ((TLRPC.Message)localObject2).id)) || ((((TLRPC.Message)localObject5).id < 0) && (((TLRPC.Message)localObject2).id < 0) && (((TLRPC.Message)localObject5).id < ((TLRPC.Message)localObject2).id))) {
                  localLongSparseArray3.put(((TLRPC.Message)localObject5).dialog_id, localObject5);
                }
              }
              localSQLitePreparedStatement1.bindLong(1, l2);
              localSQLitePreparedStatement1.bindLong(2, ((TLRPC.Message)localObject5).dialog_id);
              localSQLitePreparedStatement1.bindInteger(3, MessageObject.getUnreadFlags((TLRPC.Message)localObject5));
              localSQLitePreparedStatement1.bindInteger(4, ((TLRPC.Message)localObject5).send_state);
              localSQLitePreparedStatement1.bindInteger(5, ((TLRPC.Message)localObject5).date);
              localSQLitePreparedStatement1.bindByteBuffer(6, (NativeByteBuffer)localObject3);
              if (!MessageObject.isOut((TLRPC.Message)localObject5)) {
                break label3593;
              }
              i = 1;
              localSQLitePreparedStatement1.bindInteger(7, i);
              localSQLitePreparedStatement1.bindInteger(8, ((TLRPC.Message)localObject5).ttl);
              if ((((TLRPC.Message)localObject5).flags & 0x400) != 0)
              {
                localSQLitePreparedStatement1.bindInteger(9, ((TLRPC.Message)localObject5).views);
                localSQLitePreparedStatement1.bindInteger(10, 0);
                if (!((TLRPC.Message)localObject5).mentioned) {
                  break label3599;
                }
                i = 1;
                localSQLitePreparedStatement1.bindInteger(11, i);
                localSQLitePreparedStatement1.step();
                if (((TLRPC.Message)localObject5).random_id != 0L)
                {
                  localSQLitePreparedStatement2.requery();
                  localSQLitePreparedStatement2.bindLong(1, ((TLRPC.Message)localObject5).random_id);
                  localSQLitePreparedStatement2.bindLong(2, l2);
                  localSQLitePreparedStatement2.step();
                }
                localObject2 = localObject1;
                if (DataQuery.canAddMessageToMedia((TLRPC.Message)localObject5))
                {
                  localObject2 = localObject1;
                  if (localObject1 == null) {
                    localObject2 = this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
                  }
                  ((SQLitePreparedStatement)localObject2).requery();
                  ((SQLitePreparedStatement)localObject2).bindLong(1, l2);
                  ((SQLitePreparedStatement)localObject2).bindLong(2, ((TLRPC.Message)localObject5).dialog_id);
                  ((SQLitePreparedStatement)localObject2).bindInteger(3, ((TLRPC.Message)localObject5).date);
                  ((SQLitePreparedStatement)localObject2).bindInteger(4, DataQuery.getMediaType((TLRPC.Message)localObject5));
                  ((SQLitePreparedStatement)localObject2).bindByteBuffer(5, (NativeByteBuffer)localObject3);
                  ((SQLitePreparedStatement)localObject2).step();
                }
                if ((((TLRPC.Message)localObject5).media instanceof TLRPC.TL_messageMediaWebPage))
                {
                  localSQLitePreparedStatement4.requery();
                  localSQLitePreparedStatement4.bindLong(1, ((TLRPC.Message)localObject5).media.webpage.id);
                  localSQLitePreparedStatement4.bindLong(2, l2);
                  localSQLitePreparedStatement4.step();
                }
                ((NativeByteBuffer)localObject3).reuse();
                m = j;
                if (paramInt == 0) {
                  break label3576;
                }
                if (((TLRPC.Message)localObject5).to_id.channel_id != 0)
                {
                  m = j;
                  if (!((TLRPC.Message)localObject5).post) {
                    break label3576;
                  }
                }
                m = j;
                if (((TLRPC.Message)localObject5).date < ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - 3600) {
                  break label3576;
                }
                m = j;
                if (!DownloadController.getInstance(this.currentAccount).canDownloadMedia((TLRPC.Message)localObject5)) {
                  break label3576;
                }
                if (!(((TLRPC.Message)localObject5).media instanceof TLRPC.TL_messageMediaPhoto))
                {
                  m = j;
                  if (!(((TLRPC.Message)localObject5).media instanceof TLRPC.TL_messageMediaDocument)) {
                    break label3576;
                  }
                }
                m = 0;
                l2 = 0L;
                localObject3 = null;
                if (!MessageObject.isVoiceMessage((TLRPC.Message)localObject5)) {
                  break label2342;
                }
                l1 = ((TLRPC.Message)localObject5).media.document.id;
                i = 2;
                localObject1 = new TLRPC.TL_messageMediaDocument();
                ((TLRPC.MessageMedia)localObject1).document = ((TLRPC.Message)localObject5).media.document;
                ((TLRPC.MessageMedia)localObject1).flags |= 0x1;
              }
              for (;;)
              {
                m = j;
                if (localObject1 == null) {
                  break label3576;
                }
                if (((TLRPC.Message)localObject5).media.ttl_seconds != 0)
                {
                  ((TLRPC.MessageMedia)localObject1).ttl_seconds = ((TLRPC.Message)localObject5).media.ttl_seconds;
                  ((TLRPC.MessageMedia)localObject1).flags |= 0x4;
                }
                m = j | i;
                localSQLitePreparedStatement3.requery();
                localObject3 = new NativeByteBuffer(((TLRPC.MessageMedia)localObject1).getObjectSize());
                ((TLRPC.MessageMedia)localObject1).serializeToStream((AbstractSerializedData)localObject3);
                localSQLitePreparedStatement3.bindLong(1, l1);
                localSQLitePreparedStatement3.bindInteger(2, i);
                localSQLitePreparedStatement3.bindInteger(3, ((TLRPC.Message)localObject5).date);
                localSQLitePreparedStatement3.bindByteBuffer(4, (NativeByteBuffer)localObject3);
                localSQLitePreparedStatement3.step();
                ((NativeByteBuffer)localObject3).reuse();
                break label3576;
                localSQLitePreparedStatement1.bindInteger(9, getMessageMediaType((TLRPC.Message)localObject5));
                break;
                if (MessageObject.isRoundVideoMessage((TLRPC.Message)localObject5))
                {
                  l1 = ((TLRPC.Message)localObject5).media.document.id;
                  i = 64;
                  localObject1 = new TLRPC.TL_messageMediaDocument();
                  ((TLRPC.MessageMedia)localObject1).document = ((TLRPC.Message)localObject5).media.document;
                  ((TLRPC.MessageMedia)localObject1).flags |= 0x1;
                }
                else if ((((TLRPC.Message)localObject5).media instanceof TLRPC.TL_messageMediaPhoto))
                {
                  l1 = l2;
                  localObject1 = localObject3;
                  i = m;
                  if (FileLoader.getClosestPhotoSizeWithSize(((TLRPC.Message)localObject5).media.photo.sizes, AndroidUtilities.getPhotoSize()) != null)
                  {
                    l1 = ((TLRPC.Message)localObject5).media.photo.id;
                    i = 1;
                    localObject1 = new TLRPC.TL_messageMediaPhoto();
                    ((TLRPC.MessageMedia)localObject1).photo = ((TLRPC.Message)localObject5).media.photo;
                    ((TLRPC.MessageMedia)localObject1).flags |= 0x1;
                  }
                }
                else if (MessageObject.isVideoMessage((TLRPC.Message)localObject5))
                {
                  l1 = ((TLRPC.Message)localObject5).media.document.id;
                  i = 4;
                  localObject1 = new TLRPC.TL_messageMediaDocument();
                  ((TLRPC.MessageMedia)localObject1).document = ((TLRPC.Message)localObject5).media.document;
                  ((TLRPC.MessageMedia)localObject1).flags |= 0x1;
                }
                else
                {
                  l1 = l2;
                  localObject1 = localObject3;
                  i = m;
                  if ((((TLRPC.Message)localObject5).media instanceof TLRPC.TL_messageMediaDocument))
                  {
                    l1 = l2;
                    localObject1 = localObject3;
                    i = m;
                    if (!MessageObject.isMusicMessage((TLRPC.Message)localObject5))
                    {
                      l1 = l2;
                      localObject1 = localObject3;
                      i = m;
                      if (!MessageObject.isGifDocument(((TLRPC.Message)localObject5).media.document))
                      {
                        l1 = ((TLRPC.Message)localObject5).media.document.id;
                        i = 8;
                        localObject1 = new TLRPC.TL_messageMediaDocument();
                        ((TLRPC.MessageMedia)localObject1).document = ((TLRPC.Message)localObject5).media.document;
                        ((TLRPC.MessageMedia)localObject1).flags |= 0x1;
                      }
                    }
                  }
                }
              }
            }
            localSQLitePreparedStatement1.dispose();
            if (localObject1 != null) {
              ((SQLitePreparedStatement)localObject1).dispose();
            }
            localSQLitePreparedStatement2.dispose();
            localSQLitePreparedStatement3.dispose();
            localSQLitePreparedStatement4.dispose();
            localObject2 = this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            i = 0;
            if (i < localLongSparseArray3.size())
            {
              long l3 = localLongSparseArray3.keyAt(i);
              if (l3 == 0L) {
                break label3605;
              }
              localObject3 = (TLRPC.Message)localLongSparseArray3.valueAt(i);
              k = 0;
              if (localObject3 != null) {
                k = ((TLRPC.Message)localObject3).to_id.channel_id;
              }
              paramArrayList = this.database.queryFinalized("SELECT date, unread_count, pts, last_mid, inbox_max, outbox_max, pinned, unread_count_i FROM dialogs WHERE did = " + l3, new Object[0]);
              int i7 = 0;
              int i8 = 0;
              int i9 = 0;
              if (k == 0) {
                break label3637;
              }
              paramInt = 1;
              int i10 = 0;
              int i11 = 0;
              int i12 = 0;
              int i13 = 0;
              int i6;
              int i2;
              int i5;
              int i1;
              int i3;
              if (paramArrayList.next())
              {
                i6 = paramArrayList.intValue(0);
                i2 = paramArrayList.intValue(1);
                m = paramArrayList.intValue(2);
                i4 = paramArrayList.intValue(3);
                i5 = paramArrayList.intValue(4);
                i1 = paramArrayList.intValue(5);
                n = paramArrayList.intValue(6);
                i3 = paramArrayList.intValue(7);
                paramArrayList.dispose();
                localObject1 = (Integer)localLongSparseArray2.get(l3);
                paramArrayList = (Integer)localLongSparseArray1.get(l3);
                if (paramArrayList != null) {
                  break label3225;
                }
                paramArrayList = Integer.valueOf(0);
                label2951:
                if (localObject1 != null) {
                  break label3245;
                }
                localObject1 = Integer.valueOf(0);
                label2962:
                if (localObject3 == null) {
                  break label3643;
                }
                l2 = ((TLRPC.Message)localObject3).id;
                label2975:
                l1 = l2;
                if (localObject3 == null) {
                  break label3614;
                }
                l1 = l2;
                if (((TLRPC.Message)localObject3).local_id == 0) {
                  break label3614;
                }
                l1 = ((TLRPC.Message)localObject3).local_id;
                break label3614;
                label3007:
                ((SQLitePreparedStatement)localObject2).requery();
                ((SQLitePreparedStatement)localObject2).bindLong(1, l3);
                if ((localObject3 == null) || ((paramBoolean2) && (i6 != 0))) {
                  break label3266;
                }
                ((SQLitePreparedStatement)localObject2).bindInteger(2, ((TLRPC.Message)localObject3).date);
              }
              for (;;)
              {
                ((SQLitePreparedStatement)localObject2).bindInteger(3, paramArrayList.intValue() + i2);
                ((SQLitePreparedStatement)localObject2).bindLong(4, l2);
                ((SQLitePreparedStatement)localObject2).bindInteger(5, i5);
                ((SQLitePreparedStatement)localObject2).bindInteger(6, i1);
                ((SQLitePreparedStatement)localObject2).bindLong(7, 0L);
                ((SQLitePreparedStatement)localObject2).bindInteger(8, ((Integer)localObject1).intValue() + i3);
                ((SQLitePreparedStatement)localObject2).bindInteger(9, m);
                ((SQLitePreparedStatement)localObject2).bindInteger(10, 0);
                ((SQLitePreparedStatement)localObject2).bindInteger(11, n);
                ((SQLitePreparedStatement)localObject2).step();
                break label3605;
                i6 = i7;
                i5 = i10;
                i4 = i8;
                i3 = i13;
                i2 = i9;
                i1 = i11;
                n = i12;
                m = paramInt;
                if (k == 0) {
                  break;
                }
                MessagesController.getInstance(this.currentAccount).checkChannelInviter(k);
                i6 = i7;
                i5 = i10;
                i4 = i8;
                i3 = i13;
                i2 = i9;
                i1 = i11;
                n = i12;
                m = paramInt;
                break;
                label3225:
                localLongSparseArray1.put(l3, Integer.valueOf(paramArrayList.intValue() + i2));
                break label2951;
                label3245:
                localLongSparseArray2.put(l3, Integer.valueOf(((Integer)localObject1).intValue() + i3));
                break label2962;
                label3266:
                ((SQLitePreparedStatement)localObject2).bindInteger(2, i6);
              }
            }
            ((SQLitePreparedStatement)localObject2).dispose();
            if (localObject4 != null)
            {
              paramArrayList = this.database.executeFast("REPLACE INTO media_counts_v2 VALUES(?, ?, ?)");
              paramInt = 0;
            }
          }
        }
      }
    }
    for (;;)
    {
      if (paramInt < ((SparseArray)localObject4).size())
      {
        m = ((SparseArray)localObject4).keyAt(paramInt);
        localObject1 = (LongSparseArray)((SparseArray)localObject4).valueAt(paramInt);
        i = 0;
      }
      for (;;)
      {
        if (i >= ((LongSparseArray)localObject1).size()) {
          break label3660;
        }
        l1 = ((LongSparseArray)localObject1).keyAt(i);
        k = (int)l1;
        k = -1;
        localObject2 = this.database.queryFinalized(String.format(Locale.US, "SELECT count FROM media_counts_v2 WHERE uid = %d AND type = %d LIMIT 1", new Object[] { Long.valueOf(l1), Integer.valueOf(m) }), new Object[0]);
        if (((SQLiteCursor)localObject2).next()) {
          k = ((SQLiteCursor)localObject2).intValue(0);
        }
        ((SQLiteCursor)localObject2).dispose();
        if (k != -1)
        {
          paramArrayList.requery();
          n = ((Integer)((LongSparseArray)localObject1).valueAt(i)).intValue();
          paramArrayList.bindLong(1, l1);
          paramArrayList.bindInteger(2, m);
          paramArrayList.bindInteger(3, k + n);
          paramArrayList.step();
          break label3651;
          paramArrayList.dispose();
          if (paramBoolean1) {
            this.database.commitTransaction();
          }
          MessagesController.getInstance(this.currentAccount).processDialogsUpdateRead(localLongSparseArray1, localLongSparseArray2);
          if (j != 0) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                DownloadController.getInstance(MessagesStorage.this.currentAccount).newDownloadObjectsAvailable(j);
              }
            });
          }
          return;
          label3530:
          i += 1;
          localObject2 = localObject6;
          localObject3 = localObject4;
          localObject1 = localObject5;
          break;
          i = 0;
          break label870;
          i = 0;
          break label1330;
          label3563:
          j = 0;
          k = 0;
          localObject1 = localObject7;
          break label1408;
          label3576:
          k += 1;
          j = m;
          localObject1 = localObject2;
          break label1408;
          label3593:
          i = 0;
          break label1765;
          label3599:
          i = 0;
          break label1829;
          label3605:
          i += 1;
          break label2729;
          label3614:
          l2 = l1;
          if (k == 0) {
            break label3007;
          }
          l2 = l1 | k << 32;
          break label3007;
          label3637:
          paramInt = 0;
          break label2838;
          label3643:
          l2 = i4;
          break label2975;
        }
        label3651:
        i += 1;
      }
      label3660:
      paramInt += 1;
    }
  }
  
  private void putUsersAndChatsInternal(ArrayList<TLRPC.User> paramArrayList, ArrayList<TLRPC.Chat> paramArrayList1, boolean paramBoolean)
  {
    if (paramBoolean) {}
    try
    {
      this.database.beginTransaction();
      putUsersInternal(paramArrayList);
      putChatsInternal(paramArrayList1);
      if (paramBoolean) {
        this.database.commitTransaction();
      }
      return;
    }
    catch (Exception paramArrayList)
    {
      FileLog.e(paramArrayList);
    }
  }
  
  private void putUsersInternal(ArrayList<TLRPC.User> paramArrayList)
    throws Exception
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    SQLitePreparedStatement localSQLitePreparedStatement = this.database.executeFast("REPLACE INTO users VALUES(?, ?, ?, ?)");
    int i = 0;
    Object localObject3;
    if (i < paramArrayList.size())
    {
      localObject3 = (TLRPC.User)paramArrayList.get(i);
      Object localObject1 = localObject3;
      SQLiteCursor localSQLiteCursor;
      if (((TLRPC.User)localObject3).min)
      {
        localSQLiteCursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM users WHERE uid = %d", new Object[] { Integer.valueOf(((TLRPC.User)localObject3).id) }), new Object[0]);
        localObject1 = localObject3;
        if (!localSQLiteCursor.next()) {}
      }
      for (;;)
      {
        try
        {
          NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
          localObject1 = localObject3;
          if (localNativeByteBuffer == null) {
            continue;
          }
          localUser = TLRPC.User.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
          localNativeByteBuffer.reuse();
          localObject1 = localObject3;
          if (localUser == null) {
            continue;
          }
          if (((TLRPC.User)localObject3).username == null) {
            continue;
          }
          localUser.username = ((TLRPC.User)localObject3).username;
          localUser.flags |= 0x8;
          if (((TLRPC.User)localObject3).photo == null) {
            continue;
          }
          localUser.photo = ((TLRPC.User)localObject3).photo;
          localUser.flags |= 0x20;
        }
        catch (Exception localException)
        {
          TLRPC.User localUser;
          FileLog.e(localException);
          Object localObject2 = localObject3;
          continue;
          localUser.photo = null;
          localUser.flags &= 0xFFFFFFDF;
          continue;
          if (!(((TLRPC.User)localObject2).status instanceof TLRPC.TL_userStatusLastWeek)) {
            continue;
          }
          ((TLRPC.User)localObject2).status.expires = -101;
          continue;
          if (!(((TLRPC.User)localObject2).status instanceof TLRPC.TL_userStatusLastMonth)) {
            continue;
          }
          ((TLRPC.User)localObject2).status.expires = -102;
          continue;
          localSQLitePreparedStatement.bindInteger(3, 0);
          continue;
        }
        localObject1 = localUser;
        localSQLiteCursor.dispose();
        localSQLitePreparedStatement.requery();
        localObject3 = new NativeByteBuffer(((TLRPC.User)localObject1).getObjectSize());
        ((TLRPC.User)localObject1).serializeToStream((AbstractSerializedData)localObject3);
        localSQLitePreparedStatement.bindInteger(1, ((TLRPC.User)localObject1).id);
        localSQLitePreparedStatement.bindString(2, formatUserSearchName((TLRPC.User)localObject1));
        if (((TLRPC.User)localObject1).status == null) {
          continue;
        }
        if (!(((TLRPC.User)localObject1).status instanceof TLRPC.TL_userStatusRecently)) {
          continue;
        }
        ((TLRPC.User)localObject1).status.expires = -100;
        localSQLitePreparedStatement.bindInteger(3, ((TLRPC.User)localObject1).status.expires);
        localSQLitePreparedStatement.bindByteBuffer(4, (NativeByteBuffer)localObject3);
        localSQLitePreparedStatement.step();
        ((NativeByteBuffer)localObject3).reuse();
        i += 1;
        break;
        localUser.username = null;
        localUser.flags &= 0xFFFFFFF7;
      }
    }
    localSQLitePreparedStatement.dispose();
  }
  
  private void saveDiffParamsInternal(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    try
    {
      if ((this.lastSavedSeq == paramInt1) && (this.lastSavedPts == paramInt2) && (this.lastSavedDate == paramInt3) && (this.lastQtsValue == paramInt4)) {
        return;
      }
      SQLitePreparedStatement localSQLitePreparedStatement = this.database.executeFast("UPDATE params SET seq = ?, pts = ?, date = ?, qts = ? WHERE id = 1");
      localSQLitePreparedStatement.bindInteger(1, paramInt1);
      localSQLitePreparedStatement.bindInteger(2, paramInt2);
      localSQLitePreparedStatement.bindInteger(3, paramInt3);
      localSQLitePreparedStatement.bindInteger(4, paramInt4);
      localSQLitePreparedStatement.step();
      localSQLitePreparedStatement.dispose();
      this.lastSavedSeq = paramInt1;
      this.lastSavedPts = paramInt2;
      this.lastSavedDate = paramInt3;
      this.lastSavedQts = paramInt4;
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  private void updateDbToLastVersion(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        SQLitePreparedStatement localSQLitePreparedStatement;
        int k;
        try
        {
          j = paramInt;
          i = j;
          if (j < 4)
          {
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_photos(uid INTEGER, id INTEGER, data BLOB, PRIMARY KEY (uid, id))").stepThis().dispose();
            MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS read_state_out_idx_messages;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS ttl_idx_messages;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS date_idx_messages;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_out_idx_messages ON messages(mid, out);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS task_idx_messages ON messages(uid, out, read_state, ttl, date, send_state);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_date_mid_idx_messages ON messages(uid, date, mid);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_contacts_v6(uid INTEGER PRIMARY KEY, fname TEXT, sname TEXT)").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_phones_v6(uid INTEGER, phone TEXT, sphone TEXT, deleted INTEGER, PRIMARY KEY (uid, phone))").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS sphone_deleted_idx_user_phones ON user_phones_v6(sphone, deleted);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_idx_randoms ON randoms(mid);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS sent_files_v2(uid TEXT, type INTEGER, data BLOB, PRIMARY KEY (uid, type))").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS blocked_users(uid INTEGER PRIMARY KEY)").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS download_queue(uid INTEGER, type INTEGER, date INTEGER, data BLOB, PRIMARY KEY (uid, type));").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS type_date_idx_download_queue ON download_queue(type, date);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS dialog_settings(did INTEGER PRIMARY KEY, flags INTEGER);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS send_state_idx_messages ON messages(mid, send_state, date) WHERE mid < 0 AND send_state = 1;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_idx_dialogs ON dialogs(unread_count);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("UPDATE messages SET send_state = 2 WHERE mid < 0 AND send_state = 1").stepThis().dispose();
            MessagesStorage.this.fixNotificationSettings();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 4").stepThis().dispose();
            i = 4;
          }
          j = i;
          if (i == 4)
          {
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS enc_tasks_v2(mid INTEGER PRIMARY KEY, date INTEGER)").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_enc_tasks_v2 ON enc_tasks_v2(date);").stepThis().dispose();
            MessagesStorage.this.database.beginTransaction();
            localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT date, data FROM enc_tasks WHERE 1", new Object[0]);
            localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO enc_tasks_v2 VALUES(?, ?)");
            if (localSQLiteCursor.next())
            {
              j = localSQLiteCursor.intValue(0);
              localObject1 = localSQLiteCursor.byteBufferValue(1);
              if (localObject1 != null)
              {
                k = ((NativeByteBuffer)localObject1).limit();
                i = 0;
                while (i < k / 4)
                {
                  localSQLitePreparedStatement.requery();
                  localSQLitePreparedStatement.bindInteger(1, ((NativeByteBuffer)localObject1).readInt32(false));
                  localSQLitePreparedStatement.bindInteger(2, j);
                  localSQLitePreparedStatement.step();
                  i += 1;
                }
                ((NativeByteBuffer)localObject1).reuse();
              }
            }
            localSQLitePreparedStatement.dispose();
            localSQLiteCursor.dispose();
            MessagesStorage.this.database.commitTransaction();
            MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS date_idx_enc_tasks;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS enc_tasks;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN media INTEGER default 0").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 6").stepThis().dispose();
            j = 6;
          }
          k = j;
          if (j != 6) {
            break label3326;
          }
          MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS messages_seq(mid INTEGER PRIMARY KEY, seq_in INTEGER, seq_out INTEGER);").stepThis().dispose();
          MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS seq_idx_messages_seq ON messages_seq(seq_in, seq_out);").stepThis().dispose();
          MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN layer INTEGER default 0").stepThis().dispose();
          MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN seq_in INTEGER default 0").stepThis().dispose();
          MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN seq_out INTEGER default 0").stepThis().dispose();
          MessagesStorage.this.database.executeFast("PRAGMA user_version = 7").stepThis().dispose();
          k = 7;
        }
        catch (Exception localException)
        {
          SQLiteCursor localSQLiteCursor;
          Object localObject1;
          FileLog.e(localException);
        }
        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN use_count INTEGER default 0").stepThis().dispose();
        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN exchange_id INTEGER default 0").stepThis().dispose();
        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN key_date INTEGER default 0").stepThis().dispose();
        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN fprint INTEGER default 0").stepThis().dispose();
        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN fauthkey BLOB default NULL").stepThis().dispose();
        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN khash BLOB default NULL").stepThis().dispose();
        MessagesStorage.this.database.executeFast("PRAGMA user_version = 10").stepThis().dispose();
        int i = 10;
        label897:
        int j = i;
        if (i == 10)
        {
          MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS web_recent_v3(id TEXT, type INTEGER, image_url TEXT, thumb_url TEXT, local_url TEXT, width INTEGER, height INTEGER, size INTEGER, date INTEGER, PRIMARY KEY (id, type));").stepThis().dispose();
          MessagesStorage.this.database.executeFast("PRAGMA user_version = 11").stepThis().dispose();
          j = 11;
          break label3349;
          label947:
          MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_mid_idx_media;").stepThis().dispose();
          MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS mid_idx_media;").stepThis().dispose();
          MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_date_mid_idx_media;").stepThis().dispose();
          MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS media;").stepThis().dispose();
          MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS media_counts;").stepThis().dispose();
          MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_v2(mid INTEGER PRIMARY KEY, uid INTEGER, date INTEGER, type INTEGER, data BLOB)").stepThis().dispose();
          MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_counts_v2(uid INTEGER, type INTEGER, count INTEGER, PRIMARY KEY(uid, type))").stepThis().dispose();
          MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_type_date_idx_media ON media_v2(uid, mid, type, date);").stepThis().dispose();
          MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS keyvalue(id TEXT PRIMARY KEY, value TEXT)").stepThis().dispose();
          MessagesStorage.this.database.executeFast("PRAGMA user_version = 13").stepThis().dispose();
          i = 13;
          label1130:
          j = i;
          if (i == 13)
          {
            MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN replydata BLOB default NULL").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 14").stepThis().dispose();
            j = 14;
          }
          i = j;
          if (j == 14)
          {
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS hashtag_recent_v2(id TEXT PRIMARY KEY, date INTEGER);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 15").stepThis().dispose();
            i = 15;
          }
          j = i;
          if (i == 15)
          {
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS webpage_pending(id INTEGER, mid INTEGER, PRIMARY KEY (id, mid));").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 16").stepThis().dispose();
            j = 16;
          }
          i = j;
          if (j == 16)
          {
            MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN inbox_max INTEGER default 0").stepThis().dispose();
            MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN outbox_max INTEGER default 0").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 17").stepThis().dispose();
            i = 17;
          }
          j = i;
          if (i == 17)
          {
            MessagesStorage.this.database.executeFast("CREATE TABLE bot_info(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 18").stepThis().dispose();
            j = 18;
          }
          i = j;
          if (j == 18)
          {
            MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS stickers;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS stickers_v2(id INTEGER PRIMARY KEY, data BLOB, date INTEGER, hash TEXT);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 19").stepThis().dispose();
            i = 19;
          }
          j = i;
          if (i == 19)
          {
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS bot_keyboard(uid INTEGER PRIMARY KEY, mid INTEGER, info BLOB)").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS bot_keyboard_idx_mid ON bot_keyboard(mid);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 20").stepThis().dispose();
            j = 20;
          }
          i = j;
          if (j == 20)
          {
            MessagesStorage.this.database.executeFast("CREATE TABLE search_recent(did INTEGER PRIMARY KEY, date INTEGER);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 21").stepThis().dispose();
            i = 21;
          }
          j = i;
          if (i == 21)
          {
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS chat_settings_v2(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
            localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT uid, participants FROM chat_settings WHERE uid < 0", new Object[0]);
            localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?)");
            while (localSQLiteCursor.next())
            {
              i = localSQLiteCursor.intValue(0);
              Object localObject2 = localSQLiteCursor.byteBufferValue(1);
              if (localObject2 != null)
              {
                localObject1 = TLRPC.ChatParticipants.TLdeserialize((AbstractSerializedData)localObject2, ((NativeByteBuffer)localObject2).readInt32(false), false);
                ((NativeByteBuffer)localObject2).reuse();
                if (localObject1 != null)
                {
                  localObject2 = new TLRPC.TL_chatFull();
                  ((TLRPC.TL_chatFull)localObject2).id = i;
                  ((TLRPC.TL_chatFull)localObject2).chat_photo = new TLRPC.TL_photoEmpty();
                  ((TLRPC.TL_chatFull)localObject2).notify_settings = new TLRPC.TL_peerNotifySettingsEmpty();
                  ((TLRPC.TL_chatFull)localObject2).exported_invite = new TLRPC.TL_chatInviteEmpty();
                  ((TLRPC.TL_chatFull)localObject2).participants = ((TLRPC.ChatParticipants)localObject1);
                  localObject1 = new NativeByteBuffer(((TLRPC.TL_chatFull)localObject2).getObjectSize());
                  ((TLRPC.TL_chatFull)localObject2).serializeToStream((AbstractSerializedData)localObject1);
                  localSQLitePreparedStatement.requery();
                  localSQLitePreparedStatement.bindInteger(1, i);
                  localSQLitePreparedStatement.bindByteBuffer(2, (NativeByteBuffer)localObject1);
                  localSQLitePreparedStatement.step();
                  ((NativeByteBuffer)localObject1).reuse();
                }
              }
            }
            label1790:
            return;
            localSQLitePreparedStatement.dispose();
            localException.dispose();
            MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS chat_settings;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN last_mid_i INTEGER default 0").stepThis().dispose();
            MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN unread_count_i INTEGER default 0").stepThis().dispose();
            MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN pts INTEGER default 0").stepThis().dispose();
            MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN date_i INTEGER default 0").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_i_idx_dialogs ON dialogs(last_mid_i);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_i_idx_dialogs ON dialogs(unread_count_i);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN imp INTEGER default 0").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS messages_holes(uid INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
            MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_messages_holes ON messages_holes(uid, end);").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 22").stepThis().dispose();
            j = 22;
          }
          i = j;
          if (j != 22) {
            break label3366;
          }
          MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_holes_v2(uid INTEGER, type INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, type, start));").stepThis().dispose();
          MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_media_holes_v2 ON media_holes_v2(uid, type, end);").stepThis().dispose();
          MessagesStorage.this.database.executeFast("PRAGMA user_version = 23").stepThis().dispose();
          i = 23;
          break label3366;
          label2084:
          MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid != 0 AND type >= 0 AND start IN (0, 1)").stepThis().dispose();
          MessagesStorage.this.database.executeFast("PRAGMA user_version = 25").stepThis().dispose();
          j = 25;
          break label3383;
          label2128:
          MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS channel_users_v2(did INTEGER, uid INTEGER, date INTEGER, data BLOB, PRIMARY KEY(did, uid))").stepThis().dispose();
          MessagesStorage.this.database.executeFast("PRAGMA user_version = 27").stepThis().dispose();
          i = 27;
          label2169:
          j = i;
          if (i != 27) {
            break label3400;
          }
          MessagesStorage.this.database.executeFast("ALTER TABLE web_recent_v3 ADD COLUMN document BLOB default NULL").stepThis().dispose();
          MessagesStorage.this.database.executeFast("PRAGMA user_version = 28").stepThis().dispose();
          j = 28;
          break label3400;
        }
        for (;;)
        {
          label2221:
          MessagesStorage.this.database.executeFast("DELETE FROM sent_files_v2 WHERE 1").stepThis().dispose();
          MessagesStorage.this.database.executeFast("DELETE FROM download_queue WHERE 1").stepThis().dispose();
          MessagesStorage.this.database.executeFast("PRAGMA user_version = 30").stepThis().dispose();
          i = 30;
          label3326:
          label3349:
          label3366:
          label3383:
          label3400:
          do
          {
            j = i;
            if (i == 30)
            {
              MessagesStorage.this.database.executeFast("ALTER TABLE chat_settings_v2 ADD COLUMN pinned INTEGER default 0").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_settings_pinned_idx ON chat_settings_v2(uid, pinned) WHERE pinned != 0;").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS chat_pinned(uid INTEGER PRIMARY KEY, pinned INTEGER, data BLOB)").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_pinned_mid_idx ON chat_pinned(uid, pinned) WHERE pinned != 0;").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS users_data(uid INTEGER PRIMARY KEY, about TEXT)").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 31").stepThis().dispose();
              j = 31;
            }
            i = j;
            if (j == 31)
            {
              MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS bot_recent;").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS chat_hints(did INTEGER, type INTEGER, rating REAL, date INTEGER, PRIMARY KEY(did, type))").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_hints_rating_idx ON chat_hints(rating);").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 32").stepThis().dispose();
              i = 32;
            }
            j = i;
            if (i == 32)
            {
              MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_mid_idx_imp_messages;").stepThis().dispose();
              MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_date_mid_imp_idx_messages;").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 33").stepThis().dispose();
              j = 33;
            }
            i = j;
            if (j == 33)
            {
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS pending_tasks(id INTEGER PRIMARY KEY, data BLOB);").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 34").stepThis().dispose();
              i = 34;
            }
            j = i;
            if (i == 34)
            {
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS stickers_featured(id INTEGER PRIMARY KEY, data BLOB, unread BLOB, date INTEGER, hash TEXT);").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 35").stepThis().dispose();
              j = 35;
            }
            i = j;
            if (j == 35)
            {
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS requested_holes(uid INTEGER, seq_out_start INTEGER, seq_out_end INTEGER, PRIMARY KEY (uid, seq_out_start, seq_out_end));").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 36").stepThis().dispose();
              i = 36;
            }
            j = i;
            if (i == 36)
            {
              MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN in_seq_no INTEGER default 0").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 37").stepThis().dispose();
              j = 37;
            }
            i = j;
            if (j == 37)
            {
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS botcache(id TEXT PRIMARY KEY, date INTEGER, data BLOB)").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS botcache_date_idx ON botcache(date);").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 38").stepThis().dispose();
              i = 38;
            }
            j = i;
            if (i == 38)
            {
              MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN pinned INTEGER default 0").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 39").stepThis().dispose();
              j = 39;
            }
            i = j;
            if (j == 39)
            {
              MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN admin_id INTEGER default 0").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 40").stepThis().dispose();
              i = 40;
            }
            j = i;
            if (i == 40)
            {
              MessagesStorage.this.fixNotificationSettings();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 41").stepThis().dispose();
              j = 41;
            }
            i = j;
            if (j == 41)
            {
              MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN mention INTEGER default 0").stepThis().dispose();
              MessagesStorage.this.database.executeFast("ALTER TABLE user_contacts_v6 ADD COLUMN imported INTEGER default 0").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mention_idx_messages ON messages(uid, mention, read_state);").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 42").stepThis().dispose();
              i = 42;
            }
            j = i;
            if (i == 42)
            {
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS sharing_locations(uid INTEGER PRIMARY KEY, mid INTEGER, date INTEGER, period INTEGER, message BLOB);").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 43").stepThis().dispose();
              j = 43;
            }
            i = j;
            if (j == 43)
            {
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS channel_admins(did INTEGER, uid INTEGER, PRIMARY KEY(did, uid))").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 44").stepThis().dispose();
              i = 44;
            }
            j = i;
            if (i == 44)
            {
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_contacts_v7(key TEXT PRIMARY KEY, uid INTEGER, fname TEXT, sname TEXT, imported INTEGER)").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_phones_v7(key TEXT, phone TEXT, sphone TEXT, deleted INTEGER, PRIMARY KEY (key, phone))").stepThis().dispose();
              MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS sphone_deleted_idx_user_phones ON user_phones_v7(sphone, deleted);").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 45").stepThis().dispose();
              j = 45;
            }
            i = j;
            if (j == 45)
            {
              MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN mtproto_seq INTEGER default 0").stepThis().dispose();
              MessagesStorage.this.database.executeFast("PRAGMA user_version = 46").stepThis().dispose();
              i = 46;
            }
            if (i != 46) {
              break label1790;
            }
            MessagesStorage.this.database.executeFast("DELETE FROM botcache WHERE 1").stepThis().dispose();
            MessagesStorage.this.database.executeFast("PRAGMA user_version = 47").stepThis().dispose();
            return;
            if ((k == 7) || (k == 8)) {
              break;
            }
            i = k;
            if (k != 9) {
              break label897;
            }
            break;
            if (j == 11) {
              break label947;
            }
            i = j;
            if (j != 12) {
              break label1130;
            }
            break label947;
            if (i == 23) {
              break label2084;
            }
            j = i;
            if (i == 24) {
              break label2084;
            }
            if (j == 25) {
              break label2128;
            }
            i = j;
            if (j != 26) {
              break label2169;
            }
            break label2128;
            if (j == 28) {
              break label2221;
            }
            i = j;
          } while (j != 29);
        }
      }
    });
  }
  
  private void updateDialogsWithDeletedMessagesInternal(ArrayList<Integer> paramArrayList, ArrayList<Long> paramArrayList1, int paramInt)
  {
    if (Thread.currentThread().getId() != this.storageQueue.getId()) {
      throw new RuntimeException("wrong db thread");
    }
    ArrayList localArrayList1;
    int i;
    for (;;)
    {
      try
      {
        localArrayList1 = new ArrayList();
        if (paramArrayList.isEmpty()) {
          break label282;
        }
        if (paramInt != 0)
        {
          localArrayList1.add(Long.valueOf(-paramInt));
          paramArrayList = this.database.executeFast("UPDATE dialogs SET last_mid = (SELECT mid FROM messages WHERE uid = ? AND date = (SELECT MAX(date) FROM messages WHERE uid = ?)) WHERE did = ?");
          this.database.beginTransaction();
          i = 0;
          if (i >= localArrayList1.size()) {
            break;
          }
          long l = ((Long)localArrayList1.get(i)).longValue();
          paramArrayList.requery();
          paramArrayList.bindLong(1, l);
          paramArrayList.bindLong(2, l);
          paramArrayList.bindLong(3, l);
          paramArrayList.step();
          i += 1;
          continue;
        }
        paramArrayList = TextUtils.join(",", paramArrayList);
        paramArrayList = this.database.queryFinalized(String.format(Locale.US, "SELECT did FROM dialogs WHERE last_mid IN(%s)", new Object[] { paramArrayList }), new Object[0]);
        if (paramArrayList.next())
        {
          localArrayList1.add(Long.valueOf(paramArrayList.longValue(0)));
          continue;
          return;
        }
      }
      catch (Exception paramArrayList)
      {
        FileLog.e(paramArrayList);
      }
      paramArrayList.dispose();
      paramArrayList = this.database.executeFast("UPDATE dialogs SET last_mid = (SELECT mid FROM messages WHERE uid = ? AND date = (SELECT MAX(date) FROM messages WHERE uid = ? AND date != 0)) WHERE did = ?");
    }
    paramArrayList.dispose();
    this.database.commitTransaction();
    break label929;
    label244:
    label282:
    label297:
    Object localObject;
    ArrayList localArrayList2;
    ArrayList localArrayList3;
    label381:
    TLRPC.TL_dialog localTL_dialog;
    if (i < paramArrayList1.size())
    {
      paramArrayList = (Long)paramArrayList1.get(i);
      if (localArrayList1.contains(paramArrayList)) {
        break label939;
      }
      localArrayList1.add(paramArrayList);
      break label939;
      localArrayList1.add(Long.valueOf(-paramInt));
    }
    else
    {
      localObject = TextUtils.join(",", localArrayList1);
      paramArrayList = new TLRPC.TL_messages_dialogs();
      paramArrayList1 = new ArrayList();
      localArrayList1 = new ArrayList();
      localArrayList2 = new ArrayList();
      localArrayList3 = new ArrayList();
      localObject = this.database.queryFinalized(String.format(Locale.US, "SELECT d.did, d.last_mid, d.unread_count, d.date, m.data, m.read_state, m.mid, m.send_state, m.date, d.pts, d.inbox_max, d.outbox_max, d.pinned, d.unread_count_i FROM dialogs as d LEFT JOIN messages as m ON d.last_mid = m.mid WHERE d.did IN(%s)", new Object[] { localObject }), new Object[0]);
      if (((SQLiteCursor)localObject).next())
      {
        localTL_dialog = new TLRPC.TL_dialog();
        localTL_dialog.id = ((SQLiteCursor)localObject).longValue(0);
        localTL_dialog.top_message = ((SQLiteCursor)localObject).intValue(1);
        localTL_dialog.read_inbox_max_id = ((SQLiteCursor)localObject).intValue(10);
        localTL_dialog.read_outbox_max_id = ((SQLiteCursor)localObject).intValue(11);
        localTL_dialog.unread_count = ((SQLiteCursor)localObject).intValue(2);
        localTL_dialog.unread_mentions_count = ((SQLiteCursor)localObject).intValue(13);
        localTL_dialog.last_message_date = ((SQLiteCursor)localObject).intValue(3);
        localTL_dialog.pts = ((SQLiteCursor)localObject).intValue(9);
        if (paramInt != 0) {
          break label948;
        }
        i = 0;
        label497:
        localTL_dialog.flags = i;
        localTL_dialog.pinnedNum = ((SQLiteCursor)localObject).intValue(12);
        if (localTL_dialog.pinnedNum == 0) {
          break label954;
        }
      }
    }
    label929:
    label939:
    label948:
    label954:
    for (boolean bool = true;; bool = false)
    {
      localTL_dialog.pinned = bool;
      paramArrayList.dialogs.add(localTL_dialog);
      NativeByteBuffer localNativeByteBuffer = ((SQLiteCursor)localObject).byteBufferValue(4);
      if (localNativeByteBuffer != null)
      {
        TLRPC.Message localMessage = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
        localMessage.readAttachPath(localNativeByteBuffer, UserConfig.getInstance(this.currentAccount).clientUserId);
        localNativeByteBuffer.reuse();
        MessageObject.setUnreadFlags(localMessage, ((SQLiteCursor)localObject).intValue(5));
        localMessage.id = ((SQLiteCursor)localObject).intValue(6);
        localMessage.send_state = ((SQLiteCursor)localObject).intValue(7);
        i = ((SQLiteCursor)localObject).intValue(8);
        if (i != 0) {
          localTL_dialog.last_message_date = i;
        }
        localMessage.dialog_id = localTL_dialog.id;
        paramArrayList.messages.add(localMessage);
        addUsersAndChatsFromMessage(localMessage, localArrayList1, localArrayList2);
      }
      i = (int)localTL_dialog.id;
      int j = (int)(localTL_dialog.id >> 32);
      if (i != 0)
      {
        if (j == 1)
        {
          if (localArrayList2.contains(Integer.valueOf(i))) {
            break label381;
          }
          localArrayList2.add(Integer.valueOf(i));
          break label381;
        }
        if (i > 0)
        {
          if (localArrayList1.contains(Integer.valueOf(i))) {
            break label381;
          }
          localArrayList1.add(Integer.valueOf(i));
          break label381;
        }
        if (localArrayList2.contains(Integer.valueOf(-i))) {
          break label381;
        }
        localArrayList2.add(Integer.valueOf(-i));
        break label381;
      }
      if (localArrayList3.contains(Integer.valueOf(j))) {
        break label381;
      }
      localArrayList3.add(Integer.valueOf(j));
      break label381;
      ((SQLiteCursor)localObject).dispose();
      if (!localArrayList3.isEmpty()) {
        getEncryptedChatsInternal(TextUtils.join(",", localArrayList3), paramArrayList1, localArrayList1);
      }
      if (!localArrayList2.isEmpty()) {
        getChatsInternal(TextUtils.join(",", localArrayList2), paramArrayList.chats);
      }
      if (!localArrayList1.isEmpty()) {
        getUsersInternal(TextUtils.join(",", localArrayList1), paramArrayList.users);
      }
      if ((paramArrayList.dialogs.isEmpty()) && (paramArrayList1.isEmpty())) {
        break;
      }
      MessagesController.getInstance(this.currentAccount).processDialogsUpdate(paramArrayList, paramArrayList1);
      return;
      if (paramArrayList1 == null) {
        break label297;
      }
      i = 0;
      break label244;
      i += 1;
      break label244;
      i = 1;
      break label497;
    }
  }
  
  private void updateDialogsWithReadMessagesInternal(ArrayList<Integer> paramArrayList, SparseLongArray paramSparseLongArray1, SparseLongArray paramSparseLongArray2, ArrayList<Long> paramArrayList1)
  {
    LongSparseArray localLongSparseArray1;
    LongSparseArray localLongSparseArray2;
    ArrayList localArrayList;
    long l;
    for (;;)
    {
      try
      {
        localLongSparseArray1 = new LongSparseArray();
        localLongSparseArray2 = new LongSparseArray();
        localArrayList = new ArrayList();
        if (isEmpty(paramArrayList)) {
          break label262;
        }
        paramArrayList = TextUtils.join(",", paramArrayList);
        paramArrayList = this.database.queryFinalized(String.format(Locale.US, "SELECT uid, read_state, out FROM messages WHERE mid IN(%s)", new Object[] { paramArrayList }), new Object[0]);
        if (!paramArrayList.next()) {
          break;
        }
        if ((paramArrayList.intValue(2) != 0) || (paramArrayList.intValue(1) != 0)) {
          continue;
        }
        l = paramArrayList.longValue(0);
        paramSparseLongArray1 = (Integer)localLongSparseArray1.get(l);
        if (paramSparseLongArray1 == null)
        {
          localLongSparseArray1.put(l, Integer.valueOf(1));
          continue;
          return;
        }
      }
      catch (Exception paramArrayList)
      {
        FileLog.e(paramArrayList);
      }
      localLongSparseArray1.put(l, Integer.valueOf(paramSparseLongArray1.intValue() + 1));
    }
    paramArrayList.dispose();
    label159:
    int i;
    label262:
    int j;
    if ((localLongSparseArray1.size() > 0) || (localLongSparseArray2.size() > 0))
    {
      this.database.beginTransaction();
      if (localLongSparseArray1.size() > 0)
      {
        paramArrayList = this.database.executeFast("UPDATE dialogs SET unread_count = ? WHERE did = ?");
        i = 0;
        for (;;)
        {
          if (i < localLongSparseArray1.size())
          {
            paramArrayList.requery();
            paramArrayList.bindInteger(1, ((Integer)localLongSparseArray1.valueAt(i)).intValue());
            paramArrayList.bindLong(2, localLongSparseArray1.keyAt(i));
            paramArrayList.step();
            i += 1;
            continue;
            if (!isEmpty(paramSparseLongArray1))
            {
              i = 0;
              while (i < paramSparseLongArray1.size())
              {
                j = paramSparseLongArray1.keyAt(i);
                l = paramSparseLongArray1.get(j);
                paramArrayList = this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(mid) FROM messages WHERE uid = %d AND mid > %d AND read_state IN(0,2) AND out = 0", new Object[] { Integer.valueOf(j), Long.valueOf(l) }), new Object[0]);
                if (paramArrayList.next()) {
                  localLongSparseArray1.put(j, Integer.valueOf(paramArrayList.intValue(0)));
                }
                paramArrayList.dispose();
                paramArrayList = this.database.executeFast("UPDATE dialogs SET inbox_max = max((SELECT inbox_max FROM dialogs WHERE did = ?), ?) WHERE did = ?");
                paramArrayList.requery();
                paramArrayList.bindLong(1, j);
                paramArrayList.bindInteger(2, (int)l);
                paramArrayList.bindLong(3, j);
                paramArrayList.step();
                paramArrayList.dispose();
                i += 1;
              }
            }
            if (!isEmpty(paramArrayList1))
            {
              paramArrayList = new ArrayList(paramArrayList1);
              paramSparseLongArray1 = TextUtils.join(",", paramArrayList1);
              paramSparseLongArray1 = this.database.queryFinalized(String.format(Locale.US, "SELECT uid, read_state, out, mention, mid FROM messages WHERE mid IN(%s)", new Object[] { paramSparseLongArray1 }), new Object[0]);
              while (paramSparseLongArray1.next())
              {
                l = paramSparseLongArray1.longValue(0);
                paramArrayList.remove(Long.valueOf(paramSparseLongArray1.longValue(4)));
                if ((paramSparseLongArray1.intValue(1) < 2) && (paramSparseLongArray1.intValue(2) == 0) && (paramSparseLongArray1.intValue(3) == 1))
                {
                  paramArrayList1 = (Integer)localLongSparseArray2.get(l);
                  if (paramArrayList1 == null)
                  {
                    paramArrayList1 = this.database.queryFinalized("SELECT unread_count_i FROM dialogs WHERE did = " + l, new Object[0]);
                    i = 0;
                    if (paramArrayList1.next()) {
                      i = paramArrayList1.intValue(0);
                    }
                    paramArrayList1.dispose();
                    localLongSparseArray2.put(l, Integer.valueOf(Math.max(0, i - 1)));
                  }
                  else
                  {
                    localLongSparseArray2.put(l, Integer.valueOf(Math.max(0, paramArrayList1.intValue() - 1)));
                  }
                }
              }
              paramSparseLongArray1.dispose();
              i = 0;
            }
          }
        }
      }
    }
    for (;;)
    {
      if (i < paramArrayList.size())
      {
        j = (int)(((Long)paramArrayList.get(i)).longValue() >> 32);
        if ((j > 0) && (!localArrayList.contains(Integer.valueOf(j)))) {
          localArrayList.add(Integer.valueOf(j));
        }
      }
      else
      {
        if (isEmpty(paramSparseLongArray2)) {
          break label159;
        }
        i = 0;
        while (i < paramSparseLongArray2.size())
        {
          j = paramSparseLongArray2.keyAt(i);
          l = paramSparseLongArray2.get(j);
          paramArrayList = this.database.executeFast("UPDATE dialogs SET outbox_max = max((SELECT outbox_max FROM dialogs WHERE did = ?), ?) WHERE did = ?");
          paramArrayList.requery();
          paramArrayList.bindLong(1, j);
          paramArrayList.bindInteger(2, (int)l);
          paramArrayList.bindLong(3, j);
          paramArrayList.step();
          paramArrayList.dispose();
          i += 1;
        }
        break label159;
        paramArrayList.dispose();
        if (localLongSparseArray2.size() > 0)
        {
          paramArrayList = this.database.executeFast("UPDATE dialogs SET unread_count_i = ? WHERE did = ?");
          i = 0;
          while (i < localLongSparseArray2.size())
          {
            paramArrayList.requery();
            paramArrayList.bindInteger(1, ((Integer)localLongSparseArray2.valueAt(i)).intValue());
            paramArrayList.bindLong(2, localLongSparseArray2.keyAt(i));
            paramArrayList.step();
            i += 1;
          }
          paramArrayList.dispose();
        }
        this.database.commitTransaction();
        MessagesController.getInstance(this.currentAccount).processDialogsUpdateRead(localLongSparseArray1, localLongSparseArray2);
        if (localArrayList.isEmpty()) {
          break;
        }
        MessagesController.getInstance(this.currentAccount).reloadMentionsCountForChannels(localArrayList);
        return;
      }
      i += 1;
    }
  }
  
  private long[] updateMessageStateAndIdInternal(long paramLong, Integer paramInteger, int paramInt1, int paramInt2, int paramInt3)
  {
    Object localObject5 = null;
    localObject1 = null;
    SQLiteCursor localSQLiteCursor1 = null;
    long l3 = paramInt1;
    Object localObject6 = paramInteger;
    if (paramInteger == null)
    {
      localObject1 = localSQLiteCursor1;
      try
      {
        localSQLiteCursor1 = this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM randoms WHERE random_id = %d LIMIT 1", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
        localObject6 = paramInteger;
        localObject1 = localSQLiteCursor1;
        localObject5 = localSQLiteCursor1;
        if (localSQLiteCursor1.next())
        {
          localObject1 = localSQLiteCursor1;
          localObject5 = localSQLiteCursor1;
          int i = localSQLiteCursor1.intValue(0);
          localObject6 = Integer.valueOf(i);
        }
        localObject7 = localSQLiteCursor1;
        localObject5 = localObject6;
        if (localSQLiteCursor1 != null)
        {
          localSQLiteCursor1.dispose();
          localObject5 = localObject6;
          localObject7 = localSQLiteCursor1;
        }
      }
      catch (Exception localException1)
      {
        for (;;)
        {
          localObject5 = localObject1;
          FileLog.e(localException1);
          Object localObject7 = localObject1;
          localObject5 = paramInteger;
          if (localObject1 != null)
          {
            ((SQLiteCursor)localObject1).dispose();
            localObject7 = localObject1;
            localObject5 = paramInteger;
          }
        }
      }
      finally
      {
        if (localObject5 == null) {
          break label190;
        }
        ((SQLiteCursor)localObject5).dispose();
      }
      localObject1 = localObject7;
      localObject6 = localObject5;
      if (localObject5 == null) {
        return null;
      }
    }
    label190:
    paramLong = ((Integer)localObject6).intValue();
    long l2 = l3;
    l1 = paramLong;
    if (paramInt3 != 0)
    {
      l1 = paramLong | paramInt3 << 32;
      l2 = l3 | paramInt3 << 32;
    }
    l3 = 0L;
    paramInteger = (Integer)localObject1;
    long l4;
    try
    {
      SQLiteCursor localSQLiteCursor2 = this.database.queryFinalized(String.format(Locale.US, "SELECT uid FROM messages WHERE mid = %d LIMIT 1", new Object[] { Long.valueOf(l1) }), new Object[0]);
      paramLong = l3;
      paramInteger = localSQLiteCursor2;
      localObject1 = localSQLiteCursor2;
      if (localSQLiteCursor2.next())
      {
        paramInteger = localSQLiteCursor2;
        localObject1 = localSQLiteCursor2;
        paramLong = localSQLiteCursor2.longValue(0);
      }
      l4 = paramLong;
      if (localSQLiteCursor2 != null)
      {
        localSQLiteCursor2.dispose();
        l4 = paramLong;
      }
    }
    catch (Exception localException2)
    {
      for (;;)
      {
        localObject1 = paramInteger;
        FileLog.e(localException2);
        l4 = l3;
        if (paramInteger != null)
        {
          paramInteger.dispose();
          l4 = l3;
        }
      }
    }
    finally
    {
      if (localObject1 == null) {
        break label369;
      }
      ((SQLiteCursor)localObject1).dispose();
    }
    if (l4 == 0L) {
      return null;
    }
    label369:
    if ((l1 == l2) && (paramInt2 != 0))
    {
      localObject1 = null;
      paramInteger = null;
      try
      {
        SQLitePreparedStatement localSQLitePreparedStatement1 = this.database.executeFast("UPDATE messages SET send_state = 0, date = ? WHERE mid = ?");
        paramInteger = localSQLitePreparedStatement1;
        localObject1 = localSQLitePreparedStatement1;
        localSQLitePreparedStatement1.bindInteger(1, paramInt2);
        paramInteger = localSQLitePreparedStatement1;
        localObject1 = localSQLitePreparedStatement1;
        localSQLitePreparedStatement1.bindLong(2, l2);
        paramInteger = localSQLitePreparedStatement1;
        localObject1 = localSQLitePreparedStatement1;
        localSQLitePreparedStatement1.step();
        if (localSQLitePreparedStatement1 != null) {
          localSQLitePreparedStatement1.dispose();
        }
      }
      catch (Exception localException3)
      {
        for (;;)
        {
          localObject1 = paramInteger;
          FileLog.e(localException3);
          if (paramInteger != null) {
            paramInteger.dispose();
          }
        }
      }
      finally
      {
        if (localObject1 == null) {
          break label501;
        }
        ((SQLitePreparedStatement)localObject1).dispose();
      }
      return new long[] { l4, paramInt1 };
    }
    label501:
    paramInteger = null;
    localObject1 = null;
    try
    {
      localSQLitePreparedStatement2 = this.database.executeFast("UPDATE messages SET mid = ?, send_state = 0 WHERE mid = ?");
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.bindLong(1, l2);
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.bindLong(2, l1);
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.step();
      paramInteger = localSQLitePreparedStatement2;
      if (localSQLitePreparedStatement2 != null)
      {
        localSQLitePreparedStatement2.dispose();
        paramInteger = null;
      }
    }
    catch (Exception paramInteger)
    {
      for (;;)
      {
        SQLitePreparedStatement localSQLitePreparedStatement2;
        paramInteger = (Integer)localObject1;
        try
        {
          this.database.executeFast(String.format(Locale.US, "DELETE FROM messages WHERE mid = %d", new Object[] { Long.valueOf(l1) })).stepThis().dispose();
          paramInteger = (Integer)localObject1;
          this.database.executeFast(String.format(Locale.US, "DELETE FROM messages_seq WHERE mid = %d", new Object[] { Long.valueOf(l1) })).stepThis().dispose();
        }
        catch (Exception localException4)
        {
          for (;;)
          {
            paramInteger = (Integer)localObject1;
            FileLog.e(localException4);
          }
        }
        paramInteger = (Integer)localObject1;
        if (localObject1 != null)
        {
          ((SQLitePreparedStatement)localObject1).dispose();
          paramInteger = null;
        }
      }
    }
    finally
    {
      if (paramInteger == null) {
        break label853;
      }
      paramInteger.dispose();
    }
    localObject1 = paramInteger;
    try
    {
      localSQLitePreparedStatement2 = this.database.executeFast("UPDATE media_v2 SET mid = ? WHERE mid = ?");
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.bindLong(1, l2);
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.bindLong(2, l1);
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.step();
      paramInteger = localSQLitePreparedStatement2;
      if (localSQLitePreparedStatement2 != null)
      {
        localSQLitePreparedStatement2.dispose();
        paramInteger = null;
      }
    }
    catch (Exception paramInteger)
    {
      for (;;)
      {
        paramInteger = (Integer)localObject2;
        try
        {
          this.database.executeFast(String.format(Locale.US, "DELETE FROM media_v2 WHERE mid = %d", new Object[] { Long.valueOf(l1) })).stepThis().dispose();
          paramInteger = (Integer)localObject2;
          if (localObject2 == null) {
            continue;
          }
          ((SQLitePreparedStatement)localObject2).dispose();
          paramInteger = null;
        }
        catch (Exception localException5)
        {
          for (;;)
          {
            paramInteger = (Integer)localObject2;
            FileLog.e(localException5);
          }
        }
      }
    }
    finally
    {
      if (paramInteger == null) {
        break label935;
      }
      paramInteger.dispose();
    }
    localObject1 = paramInteger;
    try
    {
      localSQLitePreparedStatement2 = this.database.executeFast("UPDATE dialogs SET last_mid = ? WHERE last_mid = ?");
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.bindLong(1, l2);
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.bindLong(2, l1);
      localObject1 = localSQLitePreparedStatement2;
      paramInteger = localSQLitePreparedStatement2;
      localSQLitePreparedStatement2.step();
    }
    catch (Exception localException6)
    {
      for (;;)
      {
        paramInteger = (Integer)localObject3;
        FileLog.e(localException6);
        if (localObject3 != null) {
          ((SQLitePreparedStatement)localObject3).dispose();
        }
      }
    }
    finally
    {
      if (paramInteger == null) {
        break label971;
      }
      paramInteger.dispose();
    }
    return new long[] { l4, ((Integer)localObject6).intValue() };
  }
  
  private void updateUsersInternal(ArrayList<TLRPC.User> paramArrayList, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (Thread.currentThread().getId() != this.storageQueue.getId()) {
      throw new RuntimeException("wrong db thread");
    }
    Object localObject1;
    Object localObject2;
    if (paramBoolean1)
    {
      if (paramBoolean2) {}
      try
      {
        this.database.beginTransaction();
        localObject1 = this.database.executeFast("UPDATE users SET status = ? WHERE uid = ?");
        paramArrayList = paramArrayList.iterator();
        for (;;)
        {
          if (paramArrayList.hasNext())
          {
            localObject2 = (TLRPC.User)paramArrayList.next();
            ((SQLitePreparedStatement)localObject1).requery();
            if (((TLRPC.User)localObject2).status != null)
            {
              ((SQLitePreparedStatement)localObject1).bindInteger(1, ((TLRPC.User)localObject2).status.expires);
              ((SQLitePreparedStatement)localObject1).bindInteger(2, ((TLRPC.User)localObject2).id);
              ((SQLitePreparedStatement)localObject1).step();
              continue;
            }
          }
        }
      }
      catch (Exception paramArrayList)
      {
        FileLog.e(paramArrayList);
      }
    }
    do
    {
      do
      {
        do
        {
          ((SQLitePreparedStatement)localObject1).bindInteger(1, 0);
          break;
          ((SQLitePreparedStatement)localObject1).dispose();
        } while (!paramBoolean2);
        this.database.commitTransaction();
        return;
        localObject2 = new StringBuilder();
        localObject1 = new SparseArray();
        paramArrayList = paramArrayList.iterator();
        TLRPC.User localUser1;
        while (paramArrayList.hasNext())
        {
          localUser1 = (TLRPC.User)paramArrayList.next();
          if (((StringBuilder)localObject2).length() != 0) {
            ((StringBuilder)localObject2).append(",");
          }
          ((StringBuilder)localObject2).append(localUser1.id);
          ((SparseArray)localObject1).put(localUser1.id, localUser1);
        }
        paramArrayList = new ArrayList();
        getUsersInternal(((StringBuilder)localObject2).toString(), paramArrayList);
        localObject2 = paramArrayList.iterator();
        while (((Iterator)localObject2).hasNext())
        {
          localUser1 = (TLRPC.User)((Iterator)localObject2).next();
          TLRPC.User localUser2 = (TLRPC.User)((SparseArray)localObject1).get(localUser1.id);
          if (localUser2 != null) {
            if ((localUser2.first_name != null) && (localUser2.last_name != null))
            {
              if (!UserObject.isContact(localUser1))
              {
                localUser1.first_name = localUser2.first_name;
                localUser1.last_name = localUser2.last_name;
              }
              localUser1.username = localUser2.username;
            }
            else if (localUser2.photo != null)
            {
              localUser1.photo = localUser2.photo;
            }
            else if (localUser2.phone != null)
            {
              localUser1.phone = localUser2.phone;
            }
          }
        }
      } while (paramArrayList.isEmpty());
      if (paramBoolean2) {
        this.database.beginTransaction();
      }
      putUsersInternal(paramArrayList);
    } while (!paramBoolean2);
    this.database.commitTransaction();
  }
  
  public void addRecentLocalFile(final String paramString1, final String paramString2, final TLRPC.Document paramDocument)
  {
    if ((paramString1 == null) || (paramString1.length() == 0) || (((paramString2 == null) || (paramString2.length() == 0)) && (paramDocument == null))) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (paramDocument != null)
          {
            localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE web_recent_v3 SET document = ? WHERE image_url = ?");
            localSQLitePreparedStatement.requery();
            NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(paramDocument.getObjectSize());
            paramDocument.serializeToStream(localNativeByteBuffer);
            localSQLitePreparedStatement.bindByteBuffer(1, localNativeByteBuffer);
            localSQLitePreparedStatement.bindString(2, paramString1);
            localSQLitePreparedStatement.step();
            localSQLitePreparedStatement.dispose();
            localNativeByteBuffer.reuse();
            return;
          }
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE web_recent_v3 SET local_url = ? WHERE image_url = ?");
          localSQLitePreparedStatement.requery();
          localSQLitePreparedStatement.bindString(1, paramString2);
          localSQLitePreparedStatement.bindString(2, paramString1);
          localSQLitePreparedStatement.step();
          localSQLitePreparedStatement.dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void applyPhoneBookUpdates(final String paramString1, final String paramString2)
  {
    if ((paramString1.length() == 0) && (paramString2.length() == 0)) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (paramString1.length() != 0) {
            MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE user_phones_v7 SET deleted = 0 WHERE sphone IN(%s)", new Object[] { paramString1 })).stepThis().dispose();
          }
          if (paramString2.length() != 0) {
            MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE user_phones_v7 SET deleted = 1 WHERE sphone IN(%s)", new Object[] { paramString2 })).stepThis().dispose();
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public boolean checkMessageId(final long paramLong, int paramInt)
  {
    final boolean[] arrayOfBoolean = new boolean[1];
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        localObject3 = null;
        localObject1 = null;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM messages WHERE uid = %d AND mid = %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(arrayOfBoolean) }), new Object[0]);
          localObject1 = localSQLiteCursor;
          localObject3 = localSQLiteCursor;
          if (localSQLiteCursor.next())
          {
            localObject1 = localSQLiteCursor;
            localObject3 = localSQLiteCursor;
            localCountDownLatch[0] = true;
          }
          if (localSQLiteCursor != null) {
            localSQLiteCursor.dispose();
          }
        }
        catch (Exception localException)
        {
          for (;;)
          {
            localObject3 = localObject1;
            FileLog.e(localException);
            if (localObject1 != null) {
              ((SQLiteCursor)localObject1).dispose();
            }
          }
        }
        finally
        {
          if (localObject3 == null) {
            break label116;
          }
          ((SQLiteCursor)localObject3).dispose();
        }
        this.val$countDownLatch.countDown();
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfBoolean[0];
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public void cleanup(final boolean paramBoolean)
  {
    this.storageQueue.cleanupQueue();
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesStorage.this.cleanupInternal();
        MessagesStorage.this.openDatabase(false);
        if (paramBoolean) {
          Utilities.stageQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              MessagesController.getInstance(MessagesStorage.this.currentAccount).getDifference();
            }
          });
        }
      }
    });
  }
  
  public void clearDownloadQueue(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (paramInt == 0)
          {
            MessagesStorage.this.database.executeFast("DELETE FROM download_queue WHERE 1").stepThis().dispose();
            return;
          }
          MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM download_queue WHERE type = %d", new Object[] { Integer.valueOf(paramInt) })).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void clearSentMedia()
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast("DELETE FROM sent_files_v2 WHERE 1").stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void clearUserPhoto(final int paramInt, final long paramLong)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast("DELETE FROM user_photos WHERE uid = " + paramInt + " AND id = " + paramLong).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void clearUserPhotos(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast("DELETE FROM user_photos WHERE uid = " + paramInt).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void clearWebRecent(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast("DELETE FROM web_recent_v3 WHERE type = " + paramInt).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void closeHolesInMedia(long paramLong, int paramInt1, int paramInt2, int paramInt3)
    throws Exception
  {
    Object localObject3;
    Object localObject4;
    int i;
    if (paramInt3 < 0) {
      for (;;)
      {
        try
        {
          localObject3 = this.database.queryFinalized(String.format(Locale.US, "SELECT type, start, end FROM media_holes_v2 WHERE uid = %d AND type >= 0 AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2) }), new Object[0]);
        }
        catch (Exception localException1)
        {
          Object localObject1;
          int j;
          FileLog.e(localException1);
        }
        if (!((SQLiteCursor)localObject3).next()) {
          break;
        }
        localObject4 = localObject1;
        if (localObject1 == null) {
          localObject4 = new ArrayList();
        }
        paramInt3 = ((SQLiteCursor)localObject3).intValue(0);
        i = ((SQLiteCursor)localObject3).intValue(1);
        j = ((SQLiteCursor)localObject3).intValue(2);
        if (i == j)
        {
          localObject1 = localObject4;
          if (i == 1) {}
        }
        else
        {
          ((ArrayList)localObject4).add(new Hole(paramInt3, i, j));
          localObject1 = localObject4;
        }
      }
    }
    label847:
    label853:
    label860:
    for (;;)
    {
      return;
      localObject3 = this.database.queryFinalized(String.format(Locale.US, "SELECT type, start, end FROM media_holes_v2 WHERE uid = %d AND type = %d AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt3), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2) }), new Object[0]);
      break label847;
      ((SQLiteCursor)localObject3).dispose();
      if (localException1 != null)
      {
        paramInt3 = 0;
        for (;;)
        {
          if (paramInt3 >= localException1.size()) {
            break label860;
          }
          localObject3 = (Hole)localException1.get(paramInt3);
          if ((paramInt2 >= ((Hole)localObject3).end - 1) && (paramInt1 <= ((Hole)localObject3).start + 1))
          {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = %d AND end = %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(((Hole)localObject3).type), Integer.valueOf(((Hole)localObject3).start), Integer.valueOf(((Hole)localObject3).end) })).stepThis().dispose();
          }
          else if (paramInt2 >= ((Hole)localObject3).end - 1)
          {
            i = ((Hole)localObject3).end;
            if (i != paramInt1) {
              try
              {
                this.database.executeFast(String.format(Locale.US, "UPDATE media_holes_v2 SET end = %d WHERE uid = %d AND type = %d AND start = %d AND end = %d", new Object[] { Integer.valueOf(paramInt1), Long.valueOf(paramLong), Integer.valueOf(((Hole)localObject3).type), Integer.valueOf(((Hole)localObject3).start), Integer.valueOf(((Hole)localObject3).end) })).stepThis().dispose();
              }
              catch (Exception localException2)
              {
                FileLog.e(localException2);
              }
            }
          }
          else if (paramInt1 <= localException2.start + 1)
          {
            i = localException2.start;
            if (i != paramInt2) {
              try
              {
                this.database.executeFast(String.format(Locale.US, "UPDATE media_holes_v2 SET start = %d WHERE uid = %d AND type = %d AND start = %d AND end = %d", new Object[] { Integer.valueOf(paramInt2), Long.valueOf(paramLong), Integer.valueOf(localException2.type), Integer.valueOf(localException2.start), Integer.valueOf(localException2.end) })).stepThis().dispose();
              }
              catch (Exception localException3)
              {
                FileLog.e(localException3);
              }
            }
          }
          else
          {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = %d AND end = %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(localException3.type), Integer.valueOf(localException3.start), Integer.valueOf(localException3.end) })).stepThis().dispose();
            localObject4 = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
            ((SQLitePreparedStatement)localObject4).requery();
            ((SQLitePreparedStatement)localObject4).bindLong(1, paramLong);
            ((SQLitePreparedStatement)localObject4).bindInteger(2, localException3.type);
            ((SQLitePreparedStatement)localObject4).bindInteger(3, localException3.start);
            ((SQLitePreparedStatement)localObject4).bindInteger(4, paramInt1);
            ((SQLitePreparedStatement)localObject4).step();
            ((SQLitePreparedStatement)localObject4).requery();
            ((SQLitePreparedStatement)localObject4).bindLong(1, paramLong);
            ((SQLitePreparedStatement)localObject4).bindInteger(2, localException3.type);
            ((SQLitePreparedStatement)localObject4).bindInteger(3, paramInt2);
            ((SQLitePreparedStatement)localObject4).bindInteger(4, localException3.end);
            ((SQLitePreparedStatement)localObject4).step();
            ((SQLitePreparedStatement)localObject4).dispose();
            break label853;
            Object localObject2 = null;
            break;
          }
          paramInt3 += 1;
        }
      }
    }
  }
  
  public long createPendingTask(NativeByteBuffer paramNativeByteBuffer)
  {
    if (paramNativeByteBuffer == null) {
      return 0L;
    }
    final long l = this.lastTaskId.getAndAdd(1L);
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO pending_tasks VALUES(?, ?)");
          localSQLitePreparedStatement.bindLong(1, l);
          localSQLitePreparedStatement.bindByteBuffer(2, this.val$data);
          localSQLitePreparedStatement.step();
          localSQLitePreparedStatement.dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        finally
        {
          this.val$data.reuse();
        }
      }
    });
    return l;
  }
  
  public void createTaskForMid(final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4, final int paramInt5, final boolean paramBoolean)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          int i;
          try
          {
            int k;
            SparseArray localSparseArray;
            final Object localObject;
            long l1;
            if (paramInt3 > paramInt4)
            {
              i = paramInt3;
              k = i + paramInt5;
              localSparseArray = new SparseArray();
              localObject = new ArrayList();
              long l2 = paramInt1;
              l1 = l2;
              if (paramInt2 != 0) {
                l1 = l2 | paramInt2 << 32;
              }
              ((ArrayList)localObject).add(Long.valueOf(l1));
              localSparseArray.put(k, localObject);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (!MessagesStorage.34.this.val$inner) {
                    MessagesStorage.this.markMessagesContentAsRead(localObject, 0);
                  }
                  NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.messagesReadContent, new Object[] { localObject });
                }
              });
              localObject = MessagesStorage.this.database.executeFast("REPLACE INTO enc_tasks_v2 VALUES(?, ?)");
              i = 0;
              if (i < localSparseArray.size())
              {
                int m = localSparseArray.keyAt(i);
                ArrayList localArrayList = (ArrayList)localSparseArray.get(m);
                int j = 0;
                if (j >= localArrayList.size()) {
                  break label278;
                }
                ((SQLitePreparedStatement)localObject).requery();
                ((SQLitePreparedStatement)localObject).bindLong(1, ((Long)localArrayList.get(j)).longValue());
                ((SQLitePreparedStatement)localObject).bindInteger(2, m);
                ((SQLitePreparedStatement)localObject).step();
                j += 1;
                continue;
              }
            }
            else
            {
              i = paramInt4;
              continue;
            }
            ((SQLitePreparedStatement)localObject).dispose();
            MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET ttl = 0 WHERE mid = %d", new Object[] { Long.valueOf(l1) })).stepThis().dispose();
            MessagesController.getInstance(MessagesStorage.this.currentAccount).didAddedNewTask(k, localSparseArray);
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          label278:
          i += 1;
        }
      }
    });
  }
  
  public void createTaskForSecretChat(final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4, final ArrayList<Long> paramArrayList)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i = Integer.MAX_VALUE;
        SparseArray localSparseArray;
        StringBuilder localStringBuilder;
        int k;
        ArrayList localArrayList1;
        label301:
        do
        {
          final ArrayList localArrayList3;
          for (;;)
          {
            try
            {
              localSparseArray = new SparseArray();
              localArrayList3 = new ArrayList();
              localStringBuilder = new StringBuilder();
              if (paramArrayList == null)
              {
                SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid, ttl FROM messages WHERE uid = %d AND out = %d AND read_state != 0 AND ttl > 0 AND date <= %d AND send_state = 0 AND media != 1", new Object[] { Long.valueOf(paramInt1 << 32), Integer.valueOf(paramInt4), Integer.valueOf(paramInt2) }), new Object[0]);
                if (!localSQLiteCursor.next()) {
                  break;
                }
                k = localSQLiteCursor.intValue(1);
                long l = localSQLiteCursor.intValue(0);
                if (paramArrayList != null) {
                  localArrayList3.add(Long.valueOf(l));
                }
                if (k <= 0) {
                  continue;
                }
                if (paramInt2 <= paramInt3) {
                  break label301;
                }
                j = paramInt2;
                j += k;
                i = Math.min(i, j);
                ArrayList localArrayList2 = (ArrayList)localSparseArray.get(j);
                localArrayList1 = localArrayList2;
                if (localArrayList2 == null)
                {
                  localArrayList1 = new ArrayList();
                  localSparseArray.put(j, localArrayList1);
                }
                if (localStringBuilder.length() != 0) {
                  localStringBuilder.append(",");
                }
                localStringBuilder.append(l);
                localArrayList1.add(Long.valueOf(l));
                continue;
              }
              localObject = TextUtils.join(",", paramArrayList);
            }
            catch (Exception localException)
            {
              FileLog.e(localException);
              return;
            }
            localObject = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.mid, m.ttl FROM messages as m INNER JOIN randoms as r ON m.mid = r.mid WHERE r.random_id IN (%s)", new Object[] { localObject }), new Object[0]);
            continue;
            j = paramInt3;
          }
          ((SQLiteCursor)localObject).dispose();
          if (paramArrayList != null) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesStorage.this.markMessagesContentAsRead(localArrayList3, 0);
                NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.messagesReadContent, new Object[] { localArrayList3 });
              }
            });
          }
        } while (localSparseArray.size() == 0);
        MessagesStorage.this.database.beginTransaction();
        Object localObject = MessagesStorage.this.database.executeFast("REPLACE INTO enc_tasks_v2 VALUES(?, ?)");
        int j = 0;
        for (;;)
        {
          if (j < localSparseArray.size())
          {
            int m = localSparseArray.keyAt(j);
            localArrayList1 = (ArrayList)localSparseArray.get(m);
            k = 0;
            while (k < localArrayList1.size())
            {
              ((SQLitePreparedStatement)localObject).requery();
              ((SQLitePreparedStatement)localObject).bindLong(1, ((Long)localArrayList1.get(k)).longValue());
              ((SQLitePreparedStatement)localObject).bindInteger(2, m);
              ((SQLitePreparedStatement)localObject).step();
              k += 1;
            }
          }
          ((SQLitePreparedStatement)localObject).dispose();
          MessagesStorage.this.database.commitTransaction();
          MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET ttl = 0 WHERE mid IN(%s)", new Object[] { localStringBuilder.toString() })).stepThis().dispose();
          MessagesController.getInstance(MessagesStorage.this.currentAccount).didAddedNewTask(i, localSparseArray);
          return;
          j += 1;
        }
      }
    });
  }
  
  public void deleteBlockedUser(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast("DELETE FROM blocked_users WHERE uid = " + paramInt).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void deleteContacts(final ArrayList<Integer> paramArrayList)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          String str = TextUtils.join(",", paramArrayList);
          MessagesStorage.this.database.executeFast("DELETE FROM contacts WHERE uid IN(" + str + ")").stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void deleteDialog(final long paramLong, final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject1;
        Object localObject4;
        int j;
        label887:
        Object localObject3;
        label978:
        label985:
        do
        {
          for (;;)
          {
            try
            {
              SQLiteCursor localSQLiteCursor1;
              if (paramInt == 3)
              {
                i = -1;
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized("SELECT last_mid FROM dialogs WHERE did = " + paramLong, new Object[0]);
                if (localSQLiteCursor1.next()) {
                  i = localSQLiteCursor1.intValue(0);
                }
                localSQLiteCursor1.dispose();
                if (i != 0) {
                  return;
                }
              }
              if (((int)paramLong == 0) || (paramInt == 2))
              {
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized("SELECT data FROM messages WHERE uid = " + paramLong, new Object[0]);
                localObject1 = new ArrayList();
                try
                {
                  if (localSQLiteCursor1.next())
                  {
                    localObject4 = localSQLiteCursor1.byteBufferValue(0);
                    if (localObject4 == null) {
                      continue;
                    }
                    Object localObject2 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject4, ((NativeByteBuffer)localObject4).readInt32(false), false);
                    ((TLRPC.Message)localObject2).readAttachPath((AbstractSerializedData)localObject4, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                    ((NativeByteBuffer)localObject4).reuse();
                    if ((localObject2 == null) || (((TLRPC.Message)localObject2).media == null)) {
                      continue;
                    }
                    if (!(((TLRPC.Message)localObject2).media instanceof TLRPC.TL_messageMediaPhoto)) {
                      break label887;
                    }
                    localObject2 = ((TLRPC.Message)localObject2).media.photo.sizes.iterator();
                    if (!((Iterator)localObject2).hasNext()) {
                      continue;
                    }
                    localObject4 = FileLoader.getPathToAttach((TLRPC.PhotoSize)((Iterator)localObject2).next());
                    if ((localObject4 == null) || (((File)localObject4).toString().length() <= 0)) {
                      continue;
                    }
                    ((ArrayList)localObject1).add(localObject4);
                    continue;
                  }
                  if (paramInt == 0) {
                    continue;
                  }
                }
                catch (Exception localException2)
                {
                  FileLog.e(localException2);
                  localSQLiteCursor1.dispose();
                  FileLoader.getInstance(MessagesStorage.this.currentAccount).deleteFiles((ArrayList)localObject1, paramInt);
                }
              }
              if (paramInt != 3) {
                break;
              }
              MessagesStorage.this.database.executeFast("DELETE FROM dialogs WHERE did = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM chat_settings_v2 WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM chat_pinned WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM channel_users_v2 WHERE did = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM search_recent WHERE did = " + paramLong).stepThis().dispose();
              i = (int)paramLong;
              j = (int)(paramLong >> 32);
              if (i == 0) {
                break label985;
              }
              if (j != 1) {
                break label978;
              }
              MessagesStorage.this.database.executeFast("DELETE FROM chats WHERE uid = " + i).stepThis().dispose();
              MessagesStorage.this.database.executeFast("UPDATE dialogs SET unread_count = 0 WHERE did = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM messages WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM media_v2 WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM messages_holes WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + paramLong).stepThis().dispose();
              DataQuery.getInstance(MessagesStorage.this.currentAccount).clearBotKeyboard(paramLong, null);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.needReloadRecentDialogsSearch, new Object[0]);
                }
              });
              return;
            }
            catch (Exception localException1)
            {
              FileLog.e(localException1);
              return;
            }
            if ((localException2.media instanceof TLRPC.TL_messageMediaDocument))
            {
              localObject4 = FileLoader.getPathToAttach(localException2.media.document);
              if ((localObject4 != null) && (((File)localObject4).toString().length() > 0)) {
                ((ArrayList)localObject1).add(localObject4);
              }
              localObject3 = FileLoader.getPathToAttach(localException2.media.document.thumb);
              if ((localObject3 != null) && (((File)localObject3).toString().length() > 0))
              {
                ((ArrayList)localObject1).add(localObject3);
                continue;
                if (i < 0)
                {
                  continue;
                  MessagesStorage.this.database.executeFast("DELETE FROM enc_chats WHERE uid = " + j).stepThis().dispose();
                }
              }
            }
          }
        } while (paramInt != 2);
        SQLiteCursor localSQLiteCursor2 = MessagesStorage.this.database.queryFinalized("SELECT last_mid_i, last_mid FROM dialogs WHERE did = " + paramLong, new Object[0]);
        int i = -1;
        if (localSQLiteCursor2.next())
        {
          long l1 = localSQLiteCursor2.longValue(0);
          long l2 = localSQLiteCursor2.longValue(1);
          localObject1 = MessagesStorage.this.database.queryFinalized("SELECT data FROM messages WHERE uid = " + paramLong + " AND mid IN (" + l1 + "," + l2 + ")", new Object[0]);
          for (;;)
          {
            j = i;
            try
            {
              if (((SQLiteCursor)localObject1).next())
              {
                localObject3 = ((SQLiteCursor)localObject1).byteBufferValue(0);
                i = j;
                if (localObject3 == null) {
                  continue;
                }
                localObject4 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject3, ((NativeByteBuffer)localObject3).readInt32(false), false);
                ((TLRPC.Message)localObject4).readAttachPath((AbstractSerializedData)localObject3, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                ((NativeByteBuffer)localObject3).reuse();
                i = j;
                if (localObject4 == null) {
                  continue;
                }
                i = ((TLRPC.Message)localObject4).id;
              }
            }
            catch (Exception localException3)
            {
              FileLog.e(localException3);
              ((SQLiteCursor)localObject1).dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM messages WHERE uid = " + paramLong + " AND mid != " + l1 + " AND mid != " + l2).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM messages_holes WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM media_v2 WHERE uid = " + paramLong).stepThis().dispose();
              MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + paramLong).stepThis().dispose();
              DataQuery.getInstance(MessagesStorage.this.currentAccount).clearBotKeyboard(paramLong, null);
              localObject1 = MessagesStorage.this.database.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
              SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
              if (j != -1) {
                MessagesStorage.createFirstHoles(paramLong, (SQLitePreparedStatement)localObject1, localSQLitePreparedStatement, j);
              }
              ((SQLitePreparedStatement)localObject1).dispose();
              localSQLitePreparedStatement.dispose();
            }
          }
        }
        localSQLiteCursor2.dispose();
      }
    });
  }
  
  public void deleteUserChannelHistory(final int paramInt1, final int paramInt2)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          long l = -paramInt1;
          final ArrayList localArrayList1 = new ArrayList();
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT data FROM messages WHERE uid = " + l, new Object[0]);
          ArrayList localArrayList2 = new ArrayList();
          for (;;)
          {
            try
            {
              if (localSQLiteCursor.next())
              {
                localObject2 = localSQLiteCursor.byteBufferValue(0);
                if (localObject2 == null) {
                  continue;
                }
                Object localObject1 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject2, ((NativeByteBuffer)localObject2).readInt32(false), false);
                ((TLRPC.Message)localObject1).readAttachPath((AbstractSerializedData)localObject2, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                ((NativeByteBuffer)localObject2).reuse();
                if ((localObject1 == null) || (((TLRPC.Message)localObject1).from_id != paramInt2) || (((TLRPC.Message)localObject1).id == 1)) {
                  continue;
                }
                localArrayList1.add(Integer.valueOf(((TLRPC.Message)localObject1).id));
                if (!(((TLRPC.Message)localObject1).media instanceof TLRPC.TL_messageMediaPhoto)) {
                  break label323;
                }
                localObject1 = ((TLRPC.Message)localObject1).media.photo.sizes.iterator();
                if (!((Iterator)localObject1).hasNext()) {
                  continue;
                }
                localObject2 = FileLoader.getPathToAttach((TLRPC.PhotoSize)((Iterator)localObject1).next());
                if ((localObject2 == null) || (((File)localObject2).toString().length() <= 0)) {
                  continue;
                }
                localArrayList2.add(localObject2);
                continue;
              }
              if (!(localException2.media instanceof TLRPC.TL_messageMediaDocument)) {
                continue;
              }
            }
            catch (Exception localException2)
            {
              FileLog.e(localException2);
              localSQLiteCursor.dispose();
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MessagesController.getInstance(MessagesStorage.this.currentAccount).markChannelDialogMessageAsDeleted(localArrayList1, MessagesStorage.22.this.val$channelId);
                }
              });
              MessagesStorage.this.markMessagesAsDeletedInternal(localArrayList1, paramInt1);
              MessagesStorage.this.updateDialogsWithDeletedMessagesInternal(localArrayList1, null, paramInt1);
              FileLoader.getInstance(MessagesStorage.this.currentAccount).deleteFiles(localArrayList2, 0);
              if (!localArrayList1.isEmpty()) {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.messagesDeleted, new Object[] { localArrayList1, Integer.valueOf(MessagesStorage.22.this.val$channelId) });
                  }
                });
              }
              return;
            }
            label323:
            Object localObject2 = FileLoader.getPathToAttach(localException2.media.document);
            if ((localObject2 != null) && (((File)localObject2).toString().length() > 0)) {
              localArrayList2.add(localObject2);
            }
            File localFile = FileLoader.getPathToAttach(localException2.media.document.thumb);
            if ((localFile != null) && (localFile.toString().length() > 0)) {
              localArrayList2.add(localFile);
            }
          }
          return;
        }
        catch (Exception localException1)
        {
          FileLog.e(localException1);
        }
      }
    });
  }
  
  public void doneHolesInMedia(long paramLong, int paramInt1, int paramInt2)
    throws Exception
  {
    SQLitePreparedStatement localSQLitePreparedStatement;
    if (paramInt2 == -1)
    {
      if (paramInt1 == 0) {
        this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d", new Object[] { Long.valueOf(paramLong) })).stepThis().dispose();
      }
      for (;;)
      {
        localSQLitePreparedStatement = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
        paramInt1 = 0;
        while (paramInt1 < 5)
        {
          localSQLitePreparedStatement.requery();
          localSQLitePreparedStatement.bindLong(1, paramLong);
          localSQLitePreparedStatement.bindInteger(2, paramInt1);
          localSQLitePreparedStatement.bindInteger(3, 1);
          localSQLitePreparedStatement.bindInteger(4, 1);
          localSQLitePreparedStatement.step();
          paramInt1 += 1;
        }
        this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND start = 0", new Object[] { Long.valueOf(paramLong) })).stepThis().dispose();
      }
      localSQLitePreparedStatement.dispose();
      return;
    }
    if (paramInt1 == 0) {
      this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt2) })).stepThis().dispose();
    }
    for (;;)
    {
      localSQLitePreparedStatement = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
      localSQLitePreparedStatement.requery();
      localSQLitePreparedStatement.bindLong(1, paramLong);
      localSQLitePreparedStatement.bindInteger(2, paramInt2);
      localSQLitePreparedStatement.bindInteger(3, 1);
      localSQLitePreparedStatement.bindInteger(4, 1);
      localSQLitePreparedStatement.step();
      localSQLitePreparedStatement.dispose();
      return;
      this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = 0", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt2) })).stepThis().dispose();
    }
  }
  
  public void emptyMessagesMedia(final ArrayList<Integer> paramArrayList)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final ArrayList localArrayList2;
        Object localObject1;
        Object localObject2;
        TLRPC.Message localMessage;
        for (;;)
        {
          try
          {
            ArrayList localArrayList1 = new ArrayList();
            localArrayList2 = new ArrayList();
            localObject1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data, mid, date, uid FROM messages WHERE mid IN (%s)", new Object[] { TextUtils.join(",", paramArrayList) }), new Object[0]);
            if (!((SQLiteCursor)localObject1).next()) {
              break;
            }
            localObject2 = ((SQLiteCursor)localObject1).byteBufferValue(0);
            if (localObject2 == null) {
              continue;
            }
            localMessage = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject2, ((NativeByteBuffer)localObject2).readInt32(false), false);
            localMessage.readAttachPath((AbstractSerializedData)localObject2, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
            ((NativeByteBuffer)localObject2).reuse();
            if (localMessage.media == null) {
              continue;
            }
            if (localMessage.media.document != null)
            {
              localObject2 = FileLoader.getPathToAttach(localMessage.media.document, true);
              if ((localObject2 != null) && (((File)localObject2).toString().length() > 0)) {
                localArrayList1.add(localObject2);
              }
              localObject2 = FileLoader.getPathToAttach(localMessage.media.document.thumb, true);
              if ((localObject2 != null) && (((File)localObject2).toString().length() > 0)) {
                localArrayList1.add(localObject2);
              }
              localMessage.media.document = new TLRPC.TL_documentEmpty();
              localMessage.media.flags &= 0xFFFFFFFE;
              localMessage.id = ((SQLiteCursor)localObject1).intValue(1);
              localMessage.date = ((SQLiteCursor)localObject1).intValue(2);
              localMessage.dialog_id = ((SQLiteCursor)localObject1).longValue(3);
              localArrayList2.add(localMessage);
              continue;
            }
            if (localMessage.media.photo == null) {
              continue;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          localObject2 = localMessage.media.photo.sizes.iterator();
          while (((Iterator)localObject2).hasNext())
          {
            File localFile = FileLoader.getPathToAttach((TLRPC.PhotoSize)((Iterator)localObject2).next(), true);
            if ((localFile != null) && (localFile.toString().length() > 0)) {
              localException.add(localFile);
            }
          }
          localMessage.media.photo = new TLRPC.TL_photoEmpty();
        }
        ((SQLiteCursor)localObject1).dispose();
        int i;
        if (!localArrayList2.isEmpty())
        {
          localObject1 = MessagesStorage.this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, ?)");
          i = 0;
          if (i < localArrayList2.size())
          {
            localMessage = (TLRPC.Message)localArrayList2.get(i);
            localObject2 = new NativeByteBuffer(localMessage.getObjectSize());
            localMessage.serializeToStream((AbstractSerializedData)localObject2);
            ((SQLitePreparedStatement)localObject1).requery();
            ((SQLitePreparedStatement)localObject1).bindLong(1, localMessage.id);
            ((SQLitePreparedStatement)localObject1).bindLong(2, localMessage.dialog_id);
            ((SQLitePreparedStatement)localObject1).bindInteger(3, MessageObject.getUnreadFlags(localMessage));
            ((SQLitePreparedStatement)localObject1).bindInteger(4, localMessage.send_state);
            ((SQLitePreparedStatement)localObject1).bindInteger(5, localMessage.date);
            ((SQLitePreparedStatement)localObject1).bindByteBuffer(6, (NativeByteBuffer)localObject2);
            if (!MessageObject.isOut(localMessage)) {
              break label685;
            }
            j = 1;
            label544:
            ((SQLitePreparedStatement)localObject1).bindInteger(7, j);
            ((SQLitePreparedStatement)localObject1).bindInteger(8, localMessage.ttl);
            if ((localMessage.flags & 0x400) != 0)
            {
              ((SQLitePreparedStatement)localObject1).bindInteger(9, localMessage.views);
              label588:
              ((SQLitePreparedStatement)localObject1).bindInteger(10, 0);
              if (!localMessage.mentioned) {
                break label690;
              }
            }
          }
        }
        label685:
        label690:
        for (int j = 1;; j = 0)
        {
          ((SQLitePreparedStatement)localObject1).bindInteger(11, j);
          ((SQLitePreparedStatement)localObject1).step();
          ((NativeByteBuffer)localObject2).reuse();
          i += 1;
          break;
          ((SQLitePreparedStatement)localObject1).bindInteger(9, MessagesStorage.this.getMessageMediaType(localMessage));
          break label588;
          ((SQLitePreparedStatement)localObject1).dispose();
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              int i = 0;
              while (i < localArrayList2.size())
              {
                NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.updateMessageMedia, new Object[] { localArrayList2.get(i) });
                i += 1;
              }
            }
          });
          FileLoader.getInstance(MessagesStorage.this.currentAccount).deleteFiles(localException, 0);
          return;
          j = 0;
          break label544;
        }
      }
    });
  }
  
  public void getBlockedUsers()
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ArrayList localArrayList2;
        StringBuilder localStringBuilder;
        try
        {
          ArrayList localArrayList1 = new ArrayList();
          localArrayList2 = new ArrayList();
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT * FROM blocked_users WHERE 1", new Object[0]);
          localStringBuilder = new StringBuilder();
          while (localSQLiteCursor.next())
          {
            int i = localSQLiteCursor.intValue(0);
            localArrayList1.add(Integer.valueOf(i));
            if (localStringBuilder.length() != 0) {
              localStringBuilder.append(",");
            }
            localStringBuilder.append(i);
          }
          localSQLiteCursor.dispose();
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        if (localStringBuilder.length() != 0) {
          MessagesStorage.this.getUsersInternal(localStringBuilder.toString(), localArrayList2);
        }
        MessagesController.getInstance(MessagesStorage.this.currentAccount).processLoadedBlockedUsers(localException, localArrayList2, true);
      }
    });
  }
  
  public void getBotCache(final String paramString, final RequestDelegate paramRequestDelegate)
  {
    if ((paramString == null) || (paramRequestDelegate == null)) {
      return;
    }
    final int i = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject6 = null;
        localObject2 = null;
        Object localObject7 = null;
        Object localObject5 = null;
        localObject4 = localObject2;
        localObject1 = localObject7;
        for (;;)
        {
          try
          {
            MessagesStorage.this.database.executeFast("DELETE FROM botcache WHERE date < " + i).stepThis().dispose();
            localObject4 = localObject2;
            localObject1 = localObject7;
            SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM botcache WHERE id = '%s'", new Object[] { paramString }), new Object[0]);
            localObject4 = localObject2;
            localObject1 = localObject7;
            boolean bool = localSQLiteCursor.next();
            localObject2 = localObject5;
            if (bool)
            {
              localObject2 = localObject6;
              localObject1 = localObject7;
            }
            try
            {
              localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
              localObject2 = localObject5;
              if (localNativeByteBuffer != null)
              {
                localObject2 = localObject6;
                localObject1 = localObject7;
                i = localNativeByteBuffer.readInt32(false);
                localObject2 = localObject6;
                localObject1 = localObject7;
                if (i != TLRPC.TL_messages_botCallbackAnswer.constructor) {
                  continue;
                }
                localObject2 = localObject6;
                localObject1 = localObject7;
                localObject4 = TLRPC.TL_messages_botCallbackAnswer.TLdeserialize(localNativeByteBuffer, i, false);
                localObject2 = localObject4;
                localObject1 = localObject4;
                localNativeByteBuffer.reuse();
                localObject2 = localObject4;
              }
            }
            catch (Exception localException2)
            {
              NativeByteBuffer localNativeByteBuffer;
              int i;
              localObject4 = localObject2;
              localObject1 = localObject2;
              FileLog.e(localException2);
              continue;
            }
            localObject4 = localObject2;
            localObject1 = localObject2;
            localSQLiteCursor.dispose();
            return;
          }
          catch (Exception localException1)
          {
            localObject1 = localObject4;
            FileLog.e(localException1);
            return;
          }
          finally
          {
            paramRequestDelegate.run((TLObject)localObject1, null);
          }
          localObject2 = localObject6;
          localObject1 = localObject7;
          localObject4 = TLRPC.messages_BotResults.TLdeserialize(localNativeByteBuffer, i, false);
        }
      }
    });
  }
  
  public void getCachedPhoneBook(final boolean paramBoolean)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject5 = null;
        Object localObject1 = null;
        try
        {
          localObject6 = MessagesStorage.this.database.queryFinalized("SELECT name FROM sqlite_master WHERE type='table' AND name='user_contacts_v6'", new Object[0]);
          localObject1 = localObject6;
          localObject5 = localObject6;
          bool = ((SQLiteCursor)localObject6).next();
          localObject1 = localObject6;
          localObject5 = localObject6;
          ((SQLiteCursor)localObject6).dispose();
          localObject1 = null;
          localObject5 = null;
          localObject6 = null;
          if (bool)
          {
            i = 16;
            localObject6 = MessagesStorage.this.database.queryFinalized("SELECT COUNT(uid) FROM user_contacts_v6 WHERE 1", new Object[0]);
            localObject1 = localObject6;
            localObject5 = localObject6;
            if (((SQLiteCursor)localObject6).next())
            {
              localObject1 = localObject6;
              localObject5 = localObject6;
              i = Math.min(5000, ((SQLiteCursor)localObject6).intValue(0));
            }
            localObject1 = localObject6;
            localObject5 = localObject6;
            ((SQLiteCursor)localObject6).dispose();
            localObject1 = localObject6;
            localObject5 = localObject6;
            localObject10 = new SparseArray(i);
            localObject1 = localObject6;
            localObject5 = localObject6;
            localObject6 = MessagesStorage.this.database.queryFinalized("SELECT us.uid, us.fname, us.sname, up.phone, up.sphone, up.deleted, us.imported FROM user_contacts_v6 as us LEFT JOIN user_phones_v6 as up ON us.uid = up.uid WHERE 1", new Object[0]);
            do
            {
              do
              {
                do
                {
                  localObject1 = localObject6;
                  localObject5 = localObject6;
                  if (!((SQLiteCursor)localObject6).next()) {
                    break;
                  }
                  localObject1 = localObject6;
                  localObject5 = localObject6;
                  i = ((SQLiteCursor)localObject6).intValue(0);
                  localObject1 = localObject6;
                  localObject5 = localObject6;
                  localObject9 = (ContactsController.Contact)((SparseArray)localObject10).get(i);
                  localObject8 = localObject9;
                  if (localObject9 == null)
                  {
                    localObject1 = localObject6;
                    localObject5 = localObject6;
                    localObject8 = new ContactsController.Contact();
                    localObject1 = localObject6;
                    localObject5 = localObject6;
                    ((ContactsController.Contact)localObject8).first_name = ((SQLiteCursor)localObject6).stringValue(1);
                    localObject1 = localObject6;
                    localObject5 = localObject6;
                    ((ContactsController.Contact)localObject8).last_name = ((SQLiteCursor)localObject6).stringValue(2);
                    localObject1 = localObject6;
                    localObject5 = localObject6;
                    ((ContactsController.Contact)localObject8).imported = ((SQLiteCursor)localObject6).intValue(6);
                    localObject1 = localObject6;
                    localObject5 = localObject6;
                    if (((ContactsController.Contact)localObject8).first_name == null)
                    {
                      localObject1 = localObject6;
                      localObject5 = localObject6;
                      ((ContactsController.Contact)localObject8).first_name = "";
                    }
                    localObject1 = localObject6;
                    localObject5 = localObject6;
                    if (((ContactsController.Contact)localObject8).last_name == null)
                    {
                      localObject1 = localObject6;
                      localObject5 = localObject6;
                      ((ContactsController.Contact)localObject8).last_name = "";
                    }
                    localObject1 = localObject6;
                    localObject5 = localObject6;
                    ((ContactsController.Contact)localObject8).contact_id = i;
                    localObject1 = localObject6;
                    localObject5 = localObject6;
                    ((SparseArray)localObject10).put(i, localObject8);
                  }
                  localObject1 = localObject6;
                  localObject5 = localObject6;
                  str2 = ((SQLiteCursor)localObject6).stringValue(3);
                } while (str2 == null);
                localObject1 = localObject6;
                localObject5 = localObject6;
                ((ContactsController.Contact)localObject8).phones.add(str2);
                localObject1 = localObject6;
                localObject5 = localObject6;
                str1 = ((SQLiteCursor)localObject6).stringValue(4);
              } while (str1 == null);
              localObject9 = str1;
              localObject1 = localObject6;
              localObject5 = localObject6;
              if (str1.length() == 8)
              {
                localObject9 = str1;
                localObject1 = localObject6;
                localObject5 = localObject6;
                if (str2.length() != 8)
                {
                  localObject1 = localObject6;
                  localObject5 = localObject6;
                  localObject9 = PhoneFormat.stripExceptNumbers(str2);
                }
              }
              localObject1 = localObject6;
              localObject5 = localObject6;
              ((ContactsController.Contact)localObject8).shortPhones.add(localObject9);
              localObject1 = localObject6;
              localObject5 = localObject6;
              ((ContactsController.Contact)localObject8).phoneDeleted.add(Integer.valueOf(((SQLiteCursor)localObject6).intValue(5)));
              localObject1 = localObject6;
              localObject5 = localObject6;
              ((ContactsController.Contact)localObject8).phoneTypes.add("");
              localObject1 = localObject6;
              localObject5 = localObject6;
            } while (((SparseArray)localObject10).size() != 5000);
            localObject1 = localObject6;
            localObject5 = localObject6;
            ((SQLiteCursor)localObject6).dispose();
            localObject5 = null;
            localObject1 = null;
            ContactsController.getInstance(MessagesStorage.this.currentAccount).migratePhoneBookToV7((SparseArray)localObject10);
            if (0 != 0) {
              throw new NullPointerException();
            }
            return;
          }
          localObject5 = localObject6;
          if (0 != 0) {
            throw new NullPointerException();
          }
        }
        catch (Throwable localThrowable1)
        {
          for (;;)
          {
            Object localObject6;
            Object localObject9;
            Object localObject8;
            String str2;
            String str1;
            int i1;
            int i2;
            int i5;
            localObject5 = localObject1;
            FileLog.e(localThrowable1);
            localObject5 = localObject1;
            if (localObject1 != null)
            {
              ((SQLiteCursor)localObject1).dispose();
              localObject5 = localObject1;
            }
          }
        }
        finally
        {
          if (localObject5 == null) {
            break label1556;
          }
          ((SQLiteCursor)localObject5).dispose();
        }
        int i = 16;
        int i3 = 0;
        i1 = 0;
        i2 = 0;
        int i4 = 0;
        i5 = 0;
        int j = i;
        int m = i3;
        localObject1 = localObject5;
        int n = i4;
        try
        {
          localObject6 = MessagesStorage.this.database.queryFinalized("SELECT COUNT(key) FROM user_contacts_v7 WHERE 1", new Object[0]);
          int k = i;
          j = i;
          m = i3;
          localObject1 = localObject6;
          n = i4;
          localObject5 = localObject6;
          if (((SQLiteCursor)localObject6).next())
          {
            j = i;
            m = i3;
            localObject1 = localObject6;
            n = i4;
            localObject5 = localObject6;
            i3 = ((SQLiteCursor)localObject6).intValue(0);
            j = i;
            m = i3;
            localObject1 = localObject6;
            n = i4;
            localObject5 = localObject6;
            i4 = Math.min(5000, i3);
            i = i5;
            if (i3 > 5000) {
              i = i3 - 5000;
            }
            k = i4;
            i1 = i3;
            i2 = i;
            j = i4;
            m = i3;
            localObject1 = localObject6;
            n = i;
            localObject5 = localObject6;
            if (BuildVars.LOGS_ENABLED)
            {
              j = i4;
              m = i3;
              localObject1 = localObject6;
              n = i;
              localObject5 = localObject6;
              FileLog.d(MessagesStorage.this.currentAccount + " current cached contacts count = " + i3);
              i2 = i;
              i1 = i3;
              k = i4;
            }
          }
          i3 = k;
          i = i1;
          localObject5 = localObject6;
          i4 = i2;
          if (localObject6 != null)
          {
            ((SQLiteCursor)localObject6).dispose();
            i4 = i2;
            localObject5 = localObject6;
            i = i1;
            i3 = k;
          }
        }
        catch (Throwable localThrowable2)
        {
          for (;;)
          {
            localObject5 = localObject2;
            FileLog.e(localThrowable2);
            i3 = j;
            i = m;
            localObject5 = localObject2;
            i4 = n;
            if (localObject2 != null)
            {
              ((SQLiteCursor)localObject2).dispose();
              i3 = j;
              i = m;
              localObject5 = localObject2;
              i4 = n;
            }
          }
        }
        finally
        {
          if (localObject5 == null) {
            break label1623;
          }
          ((SQLiteCursor)localObject5).dispose();
        }
        Object localObject10 = new HashMap(i3);
        if (i4 != 0) {
          localObject6 = localObject5;
        }
        try
        {
          localObject1 = MessagesStorage.this.database.queryFinalized("SELECT us.key, us.uid, us.fname, us.sname, up.phone, up.sphone, up.deleted, us.imported FROM user_contacts_v7 as us LEFT JOIN user_phones_v7 as up ON us.key = up.key WHERE 1 LIMIT 0," + i, new Object[0]);
          do
          {
            do
            {
              do
              {
                localObject6 = localObject1;
                localObject5 = localObject1;
                if (!((SQLiteCursor)localObject1).next()) {
                  break;
                }
                localObject6 = localObject1;
                localObject5 = localObject1;
                str1 = ((SQLiteCursor)localObject1).stringValue(0);
                localObject6 = localObject1;
                localObject5 = localObject1;
                localObject9 = (ContactsController.Contact)((HashMap)localObject10).get(str1);
                localObject8 = localObject9;
                if (localObject9 == null)
                {
                  localObject6 = localObject1;
                  localObject5 = localObject1;
                  localObject8 = new ContactsController.Contact();
                  localObject6 = localObject1;
                  localObject5 = localObject1;
                  ((ContactsController.Contact)localObject8).contact_id = ((SQLiteCursor)localObject1).intValue(1);
                  localObject6 = localObject1;
                  localObject5 = localObject1;
                  ((ContactsController.Contact)localObject8).first_name = ((SQLiteCursor)localObject1).stringValue(2);
                  localObject6 = localObject1;
                  localObject5 = localObject1;
                  ((ContactsController.Contact)localObject8).last_name = ((SQLiteCursor)localObject1).stringValue(3);
                  localObject6 = localObject1;
                  localObject5 = localObject1;
                  ((ContactsController.Contact)localObject8).imported = ((SQLiteCursor)localObject1).intValue(7);
                  localObject6 = localObject1;
                  localObject5 = localObject1;
                  if (((ContactsController.Contact)localObject8).first_name == null)
                  {
                    localObject6 = localObject1;
                    localObject5 = localObject1;
                    ((ContactsController.Contact)localObject8).first_name = "";
                  }
                  localObject6 = localObject1;
                  localObject5 = localObject1;
                  if (((ContactsController.Contact)localObject8).last_name == null)
                  {
                    localObject6 = localObject1;
                    localObject5 = localObject1;
                    ((ContactsController.Contact)localObject8).last_name = "";
                  }
                  localObject6 = localObject1;
                  localObject5 = localObject1;
                  ((HashMap)localObject10).put(str1, localObject8);
                }
                localObject6 = localObject1;
                localObject5 = localObject1;
                str2 = ((SQLiteCursor)localObject1).stringValue(4);
              } while (str2 == null);
              localObject6 = localObject1;
              localObject5 = localObject1;
              ((ContactsController.Contact)localObject8).phones.add(str2);
              localObject6 = localObject1;
              localObject5 = localObject1;
              str1 = ((SQLiteCursor)localObject1).stringValue(5);
            } while (str1 == null);
            localObject9 = str1;
            localObject6 = localObject1;
            localObject5 = localObject1;
            if (str1.length() == 8)
            {
              localObject9 = str1;
              localObject6 = localObject1;
              localObject5 = localObject1;
              if (str2.length() != 8)
              {
                localObject6 = localObject1;
                localObject5 = localObject1;
                localObject9 = PhoneFormat.stripExceptNumbers(str2);
              }
            }
            localObject6 = localObject1;
            localObject5 = localObject1;
            ((ContactsController.Contact)localObject8).shortPhones.add(localObject9);
            localObject6 = localObject1;
            localObject5 = localObject1;
            ((ContactsController.Contact)localObject8).phoneDeleted.add(Integer.valueOf(((SQLiteCursor)localObject1).intValue(6)));
            localObject6 = localObject1;
            localObject5 = localObject1;
            ((ContactsController.Contact)localObject8).phoneTypes.add("");
            localObject6 = localObject1;
            localObject5 = localObject1;
          } while (((HashMap)localObject10).size() != 5000);
          localObject6 = localObject1;
          localObject5 = localObject1;
          ((SQLiteCursor)localObject1).dispose();
          if (0 != 0) {
            throw new NullPointerException();
          }
        }
        catch (Exception localException)
        {
          for (;;)
          {
            Object localObject7;
            localObject5 = localObject7;
            ((HashMap)localObject10).clear();
            localObject5 = localObject7;
            FileLog.e(localException);
            if (localObject7 != null) {
              ((SQLiteCursor)localObject7).dispose();
            }
          }
        }
        finally
        {
          if (localObject5 == null) {
            break label1696;
          }
          ((SQLiteCursor)localObject5).dispose();
        }
        localObject1 = ContactsController.getInstance(MessagesStorage.this.currentAccount);
        if (!paramBoolean) {}
        label1556:
        label1623:
        label1696:
        for (boolean bool = true;; bool = false)
        {
          ((ContactsController)localObject1).performSyncPhoneBook((HashMap)localObject10, true, true, false, false, bool, false);
          return;
          localObject7 = localObject5;
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT us.key, us.uid, us.fname, us.sname, up.phone, up.sphone, up.deleted, us.imported FROM user_contacts_v7 as us LEFT JOIN user_phones_v7 as up ON us.key = up.key WHERE 1", new Object[0]);
          break;
        }
      }
    });
  }
  
  public int getChannelPtsSync(final int paramInt)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final Integer[] arrayOfInteger = new Integer[1];
    arrayOfInteger[0] = Integer.valueOf(0);
    this.storageQueue.postRunnable(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: aconst_null
        //   1: astore_2
        //   2: aconst_null
        //   3: astore_1
        //   4: aload_0
        //   5: getfield 23	org/telegram/messenger/MessagesStorage$95:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   8: invokestatic 40	org/telegram/messenger/MessagesStorage:access$000	(Lorg/telegram/messenger/MessagesStorage;)Lorg/telegram/SQLite/SQLiteDatabase;
        //   11: new 42	java/lang/StringBuilder
        //   14: dup
        //   15: invokespecial 43	java/lang/StringBuilder:<init>	()V
        //   18: ldc 45
        //   20: invokevirtual 49	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   23: aload_0
        //   24: getfield 25	org/telegram/messenger/MessagesStorage$95:val$channelId	I
        //   27: ineg
        //   28: invokevirtual 52	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
        //   31: invokevirtual 56	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   34: iconst_0
        //   35: anewarray 4	java/lang/Object
        //   38: invokevirtual 62	org/telegram/SQLite/SQLiteDatabase:queryFinalized	(Ljava/lang/String;[Ljava/lang/Object;)Lorg/telegram/SQLite/SQLiteCursor;
        //   41: astore_3
        //   42: aload_3
        //   43: astore_1
        //   44: aload_3
        //   45: astore_2
        //   46: aload_3
        //   47: invokevirtual 68	org/telegram/SQLite/SQLiteCursor:next	()Z
        //   50: ifeq +21 -> 71
        //   53: aload_3
        //   54: astore_1
        //   55: aload_3
        //   56: astore_2
        //   57: aload_0
        //   58: getfield 27	org/telegram/messenger/MessagesStorage$95:val$pts	[Ljava/lang/Integer;
        //   61: iconst_0
        //   62: aload_3
        //   63: iconst_0
        //   64: invokevirtual 71	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   67: invokestatic 77	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   70: aastore
        //   71: aload_3
        //   72: ifnull +7 -> 79
        //   75: aload_3
        //   76: invokevirtual 80	org/telegram/SQLite/SQLiteCursor:dispose	()V
        //   79: aload_0
        //   80: getfield 29	org/telegram/messenger/MessagesStorage$95:val$countDownLatch	Ljava/util/concurrent/CountDownLatch;
        //   83: ifnull +10 -> 93
        //   86: aload_0
        //   87: getfield 29	org/telegram/messenger/MessagesStorage$95:val$countDownLatch	Ljava/util/concurrent/CountDownLatch;
        //   90: invokevirtual 85	java/util/concurrent/CountDownLatch:countDown	()V
        //   93: return
        //   94: astore_3
        //   95: aload_1
        //   96: astore_2
        //   97: aload_3
        //   98: invokestatic 91	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   101: aload_1
        //   102: ifnull -23 -> 79
        //   105: aload_1
        //   106: invokevirtual 80	org/telegram/SQLite/SQLiteCursor:dispose	()V
        //   109: goto -30 -> 79
        //   112: astore_1
        //   113: aload_2
        //   114: ifnull +7 -> 121
        //   117: aload_2
        //   118: invokevirtual 80	org/telegram/SQLite/SQLiteCursor:dispose	()V
        //   121: aload_1
        //   122: athrow
        //   123: astore_1
        //   124: aload_1
        //   125: invokestatic 91	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   128: return
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	129	0	this	95
        //   3	103	1	localObject1	Object
        //   112	10	1	localObject2	Object
        //   123	2	1	localException1	Exception
        //   1	117	2	localObject3	Object
        //   41	35	3	localSQLiteCursor	SQLiteCursor
        //   94	4	3	localException2	Exception
        // Exception table:
        //   from	to	target	type
        //   4	42	94	java/lang/Exception
        //   46	53	94	java/lang/Exception
        //   57	71	94	java/lang/Exception
        //   4	42	112	finally
        //   46	53	112	finally
        //   57	71	112	finally
        //   97	101	112	finally
        //   79	93	123	java/lang/Exception
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfInteger[0].intValue();
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public TLRPC.Chat getChat(int paramInt)
  {
    TLRPC.Chat localChat = null;
    try
    {
      ArrayList localArrayList = new ArrayList();
      getChatsInternal("" + paramInt, localArrayList);
      if (!localArrayList.isEmpty()) {
        localChat = (TLRPC.Chat)localArrayList.get(0);
      }
      return localChat;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return null;
  }
  
  public TLRPC.Chat getChatSync(final int paramInt)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final TLRPC.Chat[] arrayOfChat = new TLRPC.Chat[1];
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        arrayOfChat[0] = MessagesStorage.this.getChat(paramInt);
        localCountDownLatch.countDown();
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfChat[0];
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public void getChatsInternal(String paramString, ArrayList<TLRPC.Chat> paramArrayList)
    throws Exception
  {
    if ((paramString == null) || (paramString.length() == 0) || (paramArrayList == null)) {
      return;
    }
    paramString = this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM chats WHERE uid IN(%s)", new Object[] { paramString }), new Object[0]);
    while (paramString.next()) {
      try
      {
        NativeByteBuffer localNativeByteBuffer = paramString.byteBufferValue(0);
        if (localNativeByteBuffer != null)
        {
          TLRPC.Chat localChat = TLRPC.Chat.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
          localNativeByteBuffer.reuse();
          if (localChat != null) {
            paramArrayList.add(localChat);
          }
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    paramString.dispose();
  }
  
  public void getContacts()
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ArrayList localArrayList1 = new ArrayList();
        ArrayList localArrayList2 = new ArrayList();
        StringBuilder localStringBuilder;
        boolean bool;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT * FROM contacts WHERE 1", new Object[0]);
          localStringBuilder = new StringBuilder();
          for (;;)
          {
            if (localSQLiteCursor.next())
            {
              int i = localSQLiteCursor.intValue(0);
              TLRPC.TL_contact localTL_contact = new TLRPC.TL_contact();
              localTL_contact.user_id = i;
              if (localSQLiteCursor.intValue(1) == 1)
              {
                bool = true;
                localTL_contact.mutual = bool;
                if (localStringBuilder.length() != 0) {
                  localStringBuilder.append(",");
                }
                localArrayList1.add(localTL_contact);
                localStringBuilder.append(localTL_contact.user_id);
                continue;
                ContactsController.getInstance(MessagesStorage.this.currentAccount).processLoadedContacts(localArrayList1, localArrayList2, 1);
              }
            }
          }
        }
        catch (Exception localException)
        {
          localArrayList1.clear();
          localArrayList2.clear();
          FileLog.e(localException);
        }
        for (;;)
        {
          return;
          bool = false;
          break;
          localException.dispose();
          if (localStringBuilder.length() != 0) {
            MessagesStorage.this.getUsersInternal(localStringBuilder.toString(), localArrayList2);
          }
        }
      }
    });
  }
  
  public SQLiteDatabase getDatabase()
  {
    return this.database;
  }
  
  public long getDatabaseSize()
  {
    long l1 = 0L;
    if (this.cacheFile != null) {
      l1 = 0L + this.cacheFile.length();
    }
    long l2 = l1;
    if (this.shmCacheFile != null) {
      l2 = l1 + this.shmCacheFile.length();
    }
    return l2;
  }
  
  public void getDialogPhotos(int paramInt1, final int paramInt2, final long paramLong, final int paramInt3)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final TLRPC.TL_photos_photos localTL_photos_photos;
        SQLiteCursor localSQLiteCursor2;
        for (;;)
        {
          try
          {
            if (paramLong != 0L)
            {
              SQLiteCursor localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM user_photos WHERE uid = %d AND id < %d ORDER BY id DESC LIMIT %d", new Object[] { Integer.valueOf(paramInt2), Long.valueOf(paramLong), Integer.valueOf(paramInt3) }), new Object[0]);
              localTL_photos_photos = new TLRPC.TL_photos_photos();
              if (!localSQLiteCursor1.next()) {
                break;
              }
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor1.byteBufferValue(0);
              if (localNativeByteBuffer == null) {
                continue;
              }
              TLRPC.Photo localPhoto = TLRPC.Photo.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
              localNativeByteBuffer.reuse();
              localTL_photos_photos.photos.add(localPhoto);
              continue;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          tmp142_139[0] = Integer.valueOf(paramInt2);
          Object[] tmp152_142 = tmp142_139;
          tmp152_142[1] = Integer.valueOf(paramInt3);
          localSQLiteCursor2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM user_photos WHERE uid = %d ORDER BY id DESC LIMIT %d", tmp152_142), new Object[0]);
        }
        localSQLiteCursor2.dispose();
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            MessagesController.getInstance(MessagesStorage.this.currentAccount).processLoadedUserPhotos(localTL_photos_photos, MessagesStorage.24.this.val$did, MessagesStorage.24.this.val$count, MessagesStorage.24.this.val$max_id, true, MessagesStorage.24.this.val$classGuid);
          }
        });
      }
    });
  }
  
  public int getDialogReadMax(final boolean paramBoolean, final long paramLong)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    Integer[] arrayOfInteger = new Integer[1];
    arrayOfInteger[0] = Integer.valueOf(0);
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject3 = null;
        SQLiteCursor localSQLiteCursor1 = null;
        localSQLiteCursor2 = localSQLiteCursor1;
        localObject2 = localObject3;
        for (;;)
        {
          try
          {
            if (!paramBoolean) {
              continue;
            }
            localSQLiteCursor2 = localSQLiteCursor1;
            localObject2 = localObject3;
            localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized("SELECT outbox_max FROM dialogs WHERE did = " + paramLong, new Object[0]);
            localSQLiteCursor2 = localSQLiteCursor1;
            localObject2 = localSQLiteCursor1;
            if (localSQLiteCursor1.next())
            {
              localSQLiteCursor2 = localSQLiteCursor1;
              localObject2 = localSQLiteCursor1;
              localCountDownLatch[0] = Integer.valueOf(localSQLiteCursor1.intValue(0));
            }
            if (localSQLiteCursor1 != null) {
              localSQLiteCursor1.dispose();
            }
          }
          catch (Exception localException)
          {
            localObject2 = localSQLiteCursor2;
            FileLog.e(localException);
            if (localSQLiteCursor2 == null) {
              continue;
            }
            localSQLiteCursor2.dispose();
            continue;
          }
          finally
          {
            if (localObject2 == null) {
              continue;
            }
            ((SQLiteCursor)localObject2).dispose();
          }
          this.val$countDownLatch.countDown();
          return;
          localSQLiteCursor2 = localSQLiteCursor1;
          localObject2 = localObject3;
          localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized("SELECT inbox_max FROM dialogs WHERE did = " + paramLong, new Object[0]);
        }
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfInteger[0].intValue();
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public void getDialogs(final int paramInt1, final int paramInt2)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: new 33	org/telegram/tgnet/TLRPC$TL_messages_dialogs
        //   3: dup
        //   4: invokespecial 34	org/telegram/tgnet/TLRPC$TL_messages_dialogs:<init>	()V
        //   7: astore 8
        //   9: new 36	java/util/ArrayList
        //   12: dup
        //   13: invokespecial 37	java/util/ArrayList:<init>	()V
        //   16: astore 9
        //   18: new 36	java/util/ArrayList
        //   21: dup
        //   22: invokespecial 37	java/util/ArrayList:<init>	()V
        //   25: astore 10
        //   27: aload 10
        //   29: aload_0
        //   30: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   33: invokestatic 41	org/telegram/messenger/MessagesStorage:access$300	(Lorg/telegram/messenger/MessagesStorage;)I
        //   36: invokestatic 47	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
        //   39: invokevirtual 51	org/telegram/messenger/UserConfig:getClientUserId	()I
        //   42: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   45: invokevirtual 61	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   48: pop
        //   49: new 36	java/util/ArrayList
        //   52: dup
        //   53: invokespecial 37	java/util/ArrayList:<init>	()V
        //   56: astore 11
        //   58: new 36	java/util/ArrayList
        //   61: dup
        //   62: invokespecial 37	java/util/ArrayList:<init>	()V
        //   65: astore 12
        //   67: new 36	java/util/ArrayList
        //   70: dup
        //   71: invokespecial 37	java/util/ArrayList:<init>	()V
        //   74: astore 14
        //   76: new 63	android/util/LongSparseArray
        //   79: dup
        //   80: invokespecial 64	android/util/LongSparseArray:<init>	()V
        //   83: astore 13
        //   85: aload_0
        //   86: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   89: invokestatic 68	org/telegram/messenger/MessagesStorage:access$000	(Lorg/telegram/messenger/MessagesStorage;)Lorg/telegram/SQLite/SQLiteDatabase;
        //   92: getstatic 74	java/util/Locale:US	Ljava/util/Locale;
        //   95: ldc 76
        //   97: iconst_2
        //   98: anewarray 4	java/lang/Object
        //   101: dup
        //   102: iconst_0
        //   103: aload_0
        //   104: getfield 22	org/telegram/messenger/MessagesStorage$90:val$offset	I
        //   107: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   110: aastore
        //   111: dup
        //   112: iconst_1
        //   113: aload_0
        //   114: getfield 24	org/telegram/messenger/MessagesStorage$90:val$count	I
        //   117: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   120: aastore
        //   121: invokestatic 82	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   124: iconst_0
        //   125: anewarray 4	java/lang/Object
        //   128: invokevirtual 88	org/telegram/SQLite/SQLiteDatabase:queryFinalized	(Ljava/lang/String;[Ljava/lang/Object;)Lorg/telegram/SQLite/SQLiteCursor;
        //   131: astore 15
        //   133: aload 15
        //   135: invokevirtual 94	org/telegram/SQLite/SQLiteCursor:next	()Z
        //   138: ifeq +830 -> 968
        //   141: new 96	org/telegram/tgnet/TLRPC$TL_dialog
        //   144: dup
        //   145: invokespecial 97	org/telegram/tgnet/TLRPC$TL_dialog:<init>	()V
        //   148: astore 16
        //   150: aload 16
        //   152: aload 15
        //   154: iconst_0
        //   155: invokevirtual 101	org/telegram/SQLite/SQLiteCursor:longValue	(I)J
        //   158: putfield 105	org/telegram/tgnet/TLRPC$TL_dialog:id	J
        //   161: aload 16
        //   163: aload 15
        //   165: iconst_1
        //   166: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   169: putfield 112	org/telegram/tgnet/TLRPC$TL_dialog:top_message	I
        //   172: aload 16
        //   174: aload 15
        //   176: iconst_2
        //   177: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   180: putfield 115	org/telegram/tgnet/TLRPC$TL_dialog:unread_count	I
        //   183: aload 16
        //   185: aload 15
        //   187: iconst_3
        //   188: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   191: putfield 118	org/telegram/tgnet/TLRPC$TL_dialog:last_message_date	I
        //   194: aload 16
        //   196: aload 15
        //   198: bipush 10
        //   200: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   203: putfield 121	org/telegram/tgnet/TLRPC$TL_dialog:pts	I
        //   206: aload 16
        //   208: getfield 121	org/telegram/tgnet/TLRPC$TL_dialog:pts	I
        //   211: ifeq +1098 -> 1309
        //   214: aload 16
        //   216: getfield 105	org/telegram/tgnet/TLRPC$TL_dialog:id	J
        //   219: l2i
        //   220: ifle +647 -> 867
        //   223: goto +1086 -> 1309
        //   226: aload 16
        //   228: iload_1
        //   229: putfield 124	org/telegram/tgnet/TLRPC$TL_dialog:flags	I
        //   232: aload 16
        //   234: aload 15
        //   236: bipush 11
        //   238: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   241: putfield 127	org/telegram/tgnet/TLRPC$TL_dialog:read_inbox_max_id	I
        //   244: aload 16
        //   246: aload 15
        //   248: bipush 12
        //   250: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   253: putfield 130	org/telegram/tgnet/TLRPC$TL_dialog:read_outbox_max_id	I
        //   256: aload 16
        //   258: aload 15
        //   260: bipush 14
        //   262: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   265: putfield 133	org/telegram/tgnet/TLRPC$TL_dialog:pinnedNum	I
        //   268: aload 16
        //   270: getfield 133	org/telegram/tgnet/TLRPC$TL_dialog:pinnedNum	I
        //   273: ifeq +599 -> 872
        //   276: iconst_1
        //   277: istore_3
        //   278: aload 16
        //   280: iload_3
        //   281: putfield 137	org/telegram/tgnet/TLRPC$TL_dialog:pinned	Z
        //   284: aload 16
        //   286: aload 15
        //   288: bipush 15
        //   290: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   293: putfield 140	org/telegram/tgnet/TLRPC$TL_dialog:unread_mentions_count	I
        //   296: aload 15
        //   298: bipush 8
        //   300: invokevirtual 101	org/telegram/SQLite/SQLiteCursor:longValue	(I)J
        //   303: lstore 4
        //   305: lload 4
        //   307: l2i
        //   308: istore_1
        //   309: aload 16
        //   311: new 142	org/telegram/tgnet/TLRPC$TL_peerNotifySettings
        //   314: dup
        //   315: invokespecial 143	org/telegram/tgnet/TLRPC$TL_peerNotifySettings:<init>	()V
        //   318: putfield 147	org/telegram/tgnet/TLRPC$TL_dialog:notify_settings	Lorg/telegram/tgnet/TLRPC$PeerNotifySettings;
        //   321: iload_1
        //   322: iconst_1
        //   323: iand
        //   324: ifeq +38 -> 362
        //   327: aload 16
        //   329: getfield 147	org/telegram/tgnet/TLRPC$TL_dialog:notify_settings	Lorg/telegram/tgnet/TLRPC$PeerNotifySettings;
        //   332: lload 4
        //   334: bipush 32
        //   336: lshr
        //   337: l2i
        //   338: putfield 152	org/telegram/tgnet/TLRPC$PeerNotifySettings:mute_until	I
        //   341: aload 16
        //   343: getfield 147	org/telegram/tgnet/TLRPC$TL_dialog:notify_settings	Lorg/telegram/tgnet/TLRPC$PeerNotifySettings;
        //   346: getfield 152	org/telegram/tgnet/TLRPC$PeerNotifySettings:mute_until	I
        //   349: ifne +13 -> 362
        //   352: aload 16
        //   354: getfield 147	org/telegram/tgnet/TLRPC$TL_dialog:notify_settings	Lorg/telegram/tgnet/TLRPC$PeerNotifySettings;
        //   357: ldc -103
        //   359: putfield 152	org/telegram/tgnet/TLRPC$PeerNotifySettings:mute_until	I
        //   362: aload 8
        //   364: getfield 159	org/telegram/tgnet/TLRPC$messages_Dialogs:dialogs	Ljava/util/ArrayList;
        //   367: aload 16
        //   369: invokevirtual 61	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   372: pop
        //   373: aload 15
        //   375: iconst_4
        //   376: invokevirtual 163	org/telegram/SQLite/SQLiteCursor:byteBufferValue	(I)Lorg/telegram/tgnet/NativeByteBuffer;
        //   379: astore 18
        //   381: aload 18
        //   383: ifnull +372 -> 755
        //   386: aload 18
        //   388: aload 18
        //   390: iconst_0
        //   391: invokevirtual 169	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
        //   394: iconst_0
        //   395: invokestatic 175	org/telegram/tgnet/TLRPC$Message:TLdeserialize	(Lorg/telegram/tgnet/AbstractSerializedData;IZ)Lorg/telegram/tgnet/TLRPC$Message;
        //   398: astore 17
        //   400: aload 17
        //   402: aload 18
        //   404: aload_0
        //   405: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   408: invokestatic 41	org/telegram/messenger/MessagesStorage:access$300	(Lorg/telegram/messenger/MessagesStorage;)I
        //   411: invokestatic 47	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
        //   414: getfield 178	org/telegram/messenger/UserConfig:clientUserId	I
        //   417: invokevirtual 182	org/telegram/tgnet/TLRPC$Message:readAttachPath	(Lorg/telegram/tgnet/AbstractSerializedData;I)V
        //   420: aload 18
        //   422: invokevirtual 185	org/telegram/tgnet/NativeByteBuffer:reuse	()V
        //   425: aload 17
        //   427: ifnull +328 -> 755
        //   430: aload 17
        //   432: aload 15
        //   434: iconst_5
        //   435: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   438: invokestatic 191	org/telegram/messenger/MessageObject:setUnreadFlags	(Lorg/telegram/tgnet/TLRPC$Message;I)V
        //   441: aload 17
        //   443: aload 15
        //   445: bipush 6
        //   447: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   450: putfield 193	org/telegram/tgnet/TLRPC$Message:id	I
        //   453: aload 15
        //   455: bipush 9
        //   457: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   460: istore_1
        //   461: iload_1
        //   462: ifeq +9 -> 471
        //   465: aload 16
        //   467: iload_1
        //   468: putfield 118	org/telegram/tgnet/TLRPC$TL_dialog:last_message_date	I
        //   471: aload 17
        //   473: aload 15
        //   475: bipush 7
        //   477: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   480: putfield 196	org/telegram/tgnet/TLRPC$Message:send_state	I
        //   483: aload 17
        //   485: aload 16
        //   487: getfield 105	org/telegram/tgnet/TLRPC$TL_dialog:id	J
        //   490: putfield 199	org/telegram/tgnet/TLRPC$Message:dialog_id	J
        //   493: aload 8
        //   495: getfield 202	org/telegram/tgnet/TLRPC$messages_Dialogs:messages	Ljava/util/ArrayList;
        //   498: aload 17
        //   500: invokevirtual 61	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   503: pop
        //   504: aload 17
        //   506: aload 10
        //   508: aload 11
        //   510: invokestatic 206	org/telegram/messenger/MessagesStorage:addUsersAndChatsFromMessage	(Lorg/telegram/tgnet/TLRPC$Message;Ljava/util/ArrayList;Ljava/util/ArrayList;)V
        //   513: aload 17
        //   515: getfield 209	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
        //   518: ifeq +237 -> 755
        //   521: aload 17
        //   523: getfield 213	org/telegram/tgnet/TLRPC$Message:action	Lorg/telegram/tgnet/TLRPC$MessageAction;
        //   526: instanceof 215
        //   529: ifne +25 -> 554
        //   532: aload 17
        //   534: getfield 213	org/telegram/tgnet/TLRPC$Message:action	Lorg/telegram/tgnet/TLRPC$MessageAction;
        //   537: instanceof 217
        //   540: ifne +14 -> 554
        //   543: aload 17
        //   545: getfield 213	org/telegram/tgnet/TLRPC$Message:action	Lorg/telegram/tgnet/TLRPC$MessageAction;
        //   548: instanceof 219
        //   551: ifeq +204 -> 755
        //   554: aload 15
        //   556: bipush 13
        //   558: invokevirtual 223	org/telegram/SQLite/SQLiteCursor:isNull	(I)Z
        //   561: ifne +110 -> 671
        //   564: aload 15
        //   566: bipush 13
        //   568: invokevirtual 163	org/telegram/SQLite/SQLiteCursor:byteBufferValue	(I)Lorg/telegram/tgnet/NativeByteBuffer;
        //   571: astore 18
        //   573: aload 18
        //   575: ifnull +96 -> 671
        //   578: aload 17
        //   580: aload 18
        //   582: aload 18
        //   584: iconst_0
        //   585: invokevirtual 169	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
        //   588: iconst_0
        //   589: invokestatic 175	org/telegram/tgnet/TLRPC$Message:TLdeserialize	(Lorg/telegram/tgnet/AbstractSerializedData;IZ)Lorg/telegram/tgnet/TLRPC$Message;
        //   592: putfield 227	org/telegram/tgnet/TLRPC$Message:replyMessage	Lorg/telegram/tgnet/TLRPC$Message;
        //   595: aload 17
        //   597: getfield 227	org/telegram/tgnet/TLRPC$Message:replyMessage	Lorg/telegram/tgnet/TLRPC$Message;
        //   600: aload 18
        //   602: aload_0
        //   603: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   606: invokestatic 41	org/telegram/messenger/MessagesStorage:access$300	(Lorg/telegram/messenger/MessagesStorage;)I
        //   609: invokestatic 47	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
        //   612: getfield 178	org/telegram/messenger/UserConfig:clientUserId	I
        //   615: invokevirtual 182	org/telegram/tgnet/TLRPC$Message:readAttachPath	(Lorg/telegram/tgnet/AbstractSerializedData;I)V
        //   618: aload 18
        //   620: invokevirtual 185	org/telegram/tgnet/NativeByteBuffer:reuse	()V
        //   623: aload 17
        //   625: getfield 227	org/telegram/tgnet/TLRPC$Message:replyMessage	Lorg/telegram/tgnet/TLRPC$Message;
        //   628: ifnull +43 -> 671
        //   631: aload 17
        //   633: invokestatic 231	org/telegram/messenger/MessageObject:isMegagroup	(Lorg/telegram/tgnet/TLRPC$Message;)Z
        //   636: ifeq +23 -> 659
        //   639: aload 17
        //   641: getfield 227	org/telegram/tgnet/TLRPC$Message:replyMessage	Lorg/telegram/tgnet/TLRPC$Message;
        //   644: astore 18
        //   646: aload 18
        //   648: aload 18
        //   650: getfield 232	org/telegram/tgnet/TLRPC$Message:flags	I
        //   653: ldc -23
        //   655: ior
        //   656: putfield 232	org/telegram/tgnet/TLRPC$Message:flags	I
        //   659: aload 17
        //   661: getfield 227	org/telegram/tgnet/TLRPC$Message:replyMessage	Lorg/telegram/tgnet/TLRPC$Message;
        //   664: aload 10
        //   666: aload 11
        //   668: invokestatic 206	org/telegram/messenger/MessagesStorage:addUsersAndChatsFromMessage	(Lorg/telegram/tgnet/TLRPC$Message;Ljava/util/ArrayList;Ljava/util/ArrayList;)V
        //   671: aload 17
        //   673: getfield 227	org/telegram/tgnet/TLRPC$Message:replyMessage	Lorg/telegram/tgnet/TLRPC$Message;
        //   676: ifnonnull +79 -> 755
        //   679: aload 17
        //   681: getfield 209	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
        //   684: i2l
        //   685: lstore 6
        //   687: lload 6
        //   689: lstore 4
        //   691: aload 17
        //   693: getfield 237	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
        //   696: getfield 242	org/telegram/tgnet/TLRPC$Peer:channel_id	I
        //   699: ifeq +20 -> 719
        //   702: lload 6
        //   704: aload 17
        //   706: getfield 237	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
        //   709: getfield 242	org/telegram/tgnet/TLRPC$Peer:channel_id	I
        //   712: i2l
        //   713: bipush 32
        //   715: lshl
        //   716: lor
        //   717: lstore 4
        //   719: aload 14
        //   721: lload 4
        //   723: invokestatic 247	java/lang/Long:valueOf	(J)Ljava/lang/Long;
        //   726: invokevirtual 250	java/util/ArrayList:contains	(Ljava/lang/Object;)Z
        //   729: ifne +14 -> 743
        //   732: aload 14
        //   734: lload 4
        //   736: invokestatic 247	java/lang/Long:valueOf	(J)Ljava/lang/Long;
        //   739: invokevirtual 61	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   742: pop
        //   743: aload 13
        //   745: aload 16
        //   747: getfield 105	org/telegram/tgnet/TLRPC$TL_dialog:id	J
        //   750: aload 17
        //   752: invokevirtual 254	android/util/LongSparseArray:put	(JLjava/lang/Object;)V
        //   755: aload 16
        //   757: getfield 105	org/telegram/tgnet/TLRPC$TL_dialog:id	J
        //   760: l2i
        //   761: istore_1
        //   762: aload 16
        //   764: getfield 105	org/telegram/tgnet/TLRPC$TL_dialog:id	J
        //   767: bipush 32
        //   769: lshr
        //   770: l2i
        //   771: istore_2
        //   772: iload_1
        //   773: ifeq +170 -> 943
        //   776: iload_2
        //   777: iconst_1
        //   778: if_icmpne +109 -> 887
        //   781: aload 11
        //   783: iload_1
        //   784: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   787: invokevirtual 250	java/util/ArrayList:contains	(Ljava/lang/Object;)Z
        //   790: ifne -657 -> 133
        //   793: aload 11
        //   795: iload_1
        //   796: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   799: invokevirtual 61	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   802: pop
        //   803: goto -670 -> 133
        //   806: astore 10
        //   808: aload 8
        //   810: getfield 159	org/telegram/tgnet/TLRPC$messages_Dialogs:dialogs	Ljava/util/ArrayList;
        //   813: invokevirtual 257	java/util/ArrayList:clear	()V
        //   816: aload 8
        //   818: getfield 260	org/telegram/tgnet/TLRPC$messages_Dialogs:users	Ljava/util/ArrayList;
        //   821: invokevirtual 257	java/util/ArrayList:clear	()V
        //   824: aload 8
        //   826: getfield 263	org/telegram/tgnet/TLRPC$messages_Dialogs:chats	Ljava/util/ArrayList;
        //   829: invokevirtual 257	java/util/ArrayList:clear	()V
        //   832: aload 9
        //   834: invokevirtual 257	java/util/ArrayList:clear	()V
        //   837: aload 10
        //   839: invokestatic 269	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   842: aload_0
        //   843: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   846: invokestatic 41	org/telegram/messenger/MessagesStorage:access$300	(Lorg/telegram/messenger/MessagesStorage;)I
        //   849: invokestatic 274	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
        //   852: aload 8
        //   854: aload 9
        //   856: iconst_0
        //   857: bipush 100
        //   859: iconst_1
        //   860: iconst_1
        //   861: iconst_0
        //   862: iconst_1
        //   863: invokevirtual 278	org/telegram/messenger/MessagesController:processLoadedDialogs	(Lorg/telegram/tgnet/TLRPC$messages_Dialogs;Ljava/util/ArrayList;IIIZZZ)V
        //   866: return
        //   867: iconst_1
        //   868: istore_1
        //   869: goto -643 -> 226
        //   872: iconst_0
        //   873: istore_3
        //   874: goto -596 -> 278
        //   877: astore 17
        //   879: aload 17
        //   881: invokestatic 269	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   884: goto -129 -> 755
        //   887: iload_1
        //   888: ifle +28 -> 916
        //   891: aload 10
        //   893: iload_1
        //   894: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   897: invokevirtual 250	java/util/ArrayList:contains	(Ljava/lang/Object;)Z
        //   900: ifne -767 -> 133
        //   903: aload 10
        //   905: iload_1
        //   906: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   909: invokevirtual 61	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   912: pop
        //   913: goto -780 -> 133
        //   916: aload 11
        //   918: iload_1
        //   919: ineg
        //   920: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   923: invokevirtual 250	java/util/ArrayList:contains	(Ljava/lang/Object;)Z
        //   926: ifne -793 -> 133
        //   929: aload 11
        //   931: iload_1
        //   932: ineg
        //   933: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   936: invokevirtual 61	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   939: pop
        //   940: goto -807 -> 133
        //   943: aload 12
        //   945: iload_2
        //   946: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   949: invokevirtual 250	java/util/ArrayList:contains	(Ljava/lang/Object;)Z
        //   952: ifne -819 -> 133
        //   955: aload 12
        //   957: iload_2
        //   958: invokestatic 57	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   961: invokevirtual 61	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   964: pop
        //   965: goto -832 -> 133
        //   968: aload 15
        //   970: invokevirtual 281	org/telegram/SQLite/SQLiteCursor:dispose	()V
        //   973: aload 14
        //   975: invokevirtual 284	java/util/ArrayList:isEmpty	()Z
        //   978: ifne +218 -> 1196
        //   981: aload_0
        //   982: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   985: invokestatic 68	org/telegram/messenger/MessagesStorage:access$000	(Lorg/telegram/messenger/MessagesStorage;)Lorg/telegram/SQLite/SQLiteDatabase;
        //   988: getstatic 74	java/util/Locale:US	Ljava/util/Locale;
        //   991: ldc_w 286
        //   994: iconst_1
        //   995: anewarray 4	java/lang/Object
        //   998: dup
        //   999: iconst_0
        //   1000: ldc_w 288
        //   1003: aload 14
        //   1005: invokestatic 294	android/text/TextUtils:join	(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;
        //   1008: aastore
        //   1009: invokestatic 82	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   1012: iconst_0
        //   1013: anewarray 4	java/lang/Object
        //   1016: invokevirtual 88	org/telegram/SQLite/SQLiteDatabase:queryFinalized	(Ljava/lang/String;[Ljava/lang/Object;)Lorg/telegram/SQLite/SQLiteCursor;
        //   1019: astore 14
        //   1021: aload 14
        //   1023: invokevirtual 94	org/telegram/SQLite/SQLiteCursor:next	()Z
        //   1026: ifeq +165 -> 1191
        //   1029: aload 14
        //   1031: iconst_0
        //   1032: invokevirtual 163	org/telegram/SQLite/SQLiteCursor:byteBufferValue	(I)Lorg/telegram/tgnet/NativeByteBuffer;
        //   1035: astore 16
        //   1037: aload 16
        //   1039: ifnull -18 -> 1021
        //   1042: aload 16
        //   1044: aload 16
        //   1046: iconst_0
        //   1047: invokevirtual 169	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
        //   1050: iconst_0
        //   1051: invokestatic 175	org/telegram/tgnet/TLRPC$Message:TLdeserialize	(Lorg/telegram/tgnet/AbstractSerializedData;IZ)Lorg/telegram/tgnet/TLRPC$Message;
        //   1054: astore 15
        //   1056: aload 15
        //   1058: aload 16
        //   1060: aload_0
        //   1061: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   1064: invokestatic 41	org/telegram/messenger/MessagesStorage:access$300	(Lorg/telegram/messenger/MessagesStorage;)I
        //   1067: invokestatic 47	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
        //   1070: getfield 178	org/telegram/messenger/UserConfig:clientUserId	I
        //   1073: invokevirtual 182	org/telegram/tgnet/TLRPC$Message:readAttachPath	(Lorg/telegram/tgnet/AbstractSerializedData;I)V
        //   1076: aload 16
        //   1078: invokevirtual 185	org/telegram/tgnet/NativeByteBuffer:reuse	()V
        //   1081: aload 15
        //   1083: aload 14
        //   1085: iconst_1
        //   1086: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   1089: putfield 193	org/telegram/tgnet/TLRPC$Message:id	I
        //   1092: aload 15
        //   1094: aload 14
        //   1096: iconst_2
        //   1097: invokevirtual 109	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
        //   1100: putfield 297	org/telegram/tgnet/TLRPC$Message:date	I
        //   1103: aload 15
        //   1105: aload 14
        //   1107: iconst_3
        //   1108: invokevirtual 101	org/telegram/SQLite/SQLiteCursor:longValue	(I)J
        //   1111: putfield 199	org/telegram/tgnet/TLRPC$Message:dialog_id	J
        //   1114: aload 15
        //   1116: aload 10
        //   1118: aload 11
        //   1120: invokestatic 206	org/telegram/messenger/MessagesStorage:addUsersAndChatsFromMessage	(Lorg/telegram/tgnet/TLRPC$Message;Ljava/util/ArrayList;Ljava/util/ArrayList;)V
        //   1123: aload 13
        //   1125: aload 15
        //   1127: getfield 199	org/telegram/tgnet/TLRPC$Message:dialog_id	J
        //   1130: invokevirtual 301	android/util/LongSparseArray:get	(J)Ljava/lang/Object;
        //   1133: checkcast 171	org/telegram/tgnet/TLRPC$Message
        //   1136: astore 16
        //   1138: aload 16
        //   1140: ifnull -119 -> 1021
        //   1143: aload 16
        //   1145: aload 15
        //   1147: putfield 227	org/telegram/tgnet/TLRPC$Message:replyMessage	Lorg/telegram/tgnet/TLRPC$Message;
        //   1150: aload 15
        //   1152: aload 16
        //   1154: getfield 199	org/telegram/tgnet/TLRPC$Message:dialog_id	J
        //   1157: putfield 199	org/telegram/tgnet/TLRPC$Message:dialog_id	J
        //   1160: aload 16
        //   1162: invokestatic 231	org/telegram/messenger/MessageObject:isMegagroup	(Lorg/telegram/tgnet/TLRPC$Message;)Z
        //   1165: ifeq -144 -> 1021
        //   1168: aload 16
        //   1170: getfield 227	org/telegram/tgnet/TLRPC$Message:replyMessage	Lorg/telegram/tgnet/TLRPC$Message;
        //   1173: astore 15
        //   1175: aload 15
        //   1177: aload 15
        //   1179: getfield 232	org/telegram/tgnet/TLRPC$Message:flags	I
        //   1182: ldc -23
        //   1184: ior
        //   1185: putfield 232	org/telegram/tgnet/TLRPC$Message:flags	I
        //   1188: goto -167 -> 1021
        //   1191: aload 14
        //   1193: invokevirtual 281	org/telegram/SQLite/SQLiteCursor:dispose	()V
        //   1196: aload 12
        //   1198: invokevirtual 284	java/util/ArrayList:isEmpty	()Z
        //   1201: ifne +22 -> 1223
        //   1204: aload_0
        //   1205: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   1208: ldc_w 288
        //   1211: aload 12
        //   1213: invokestatic 294	android/text/TextUtils:join	(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;
        //   1216: aload 9
        //   1218: aload 10
        //   1220: invokevirtual 305	org/telegram/messenger/MessagesStorage:getEncryptedChatsInternal	(Ljava/lang/String;Ljava/util/ArrayList;Ljava/util/ArrayList;)V
        //   1223: aload 11
        //   1225: invokevirtual 284	java/util/ArrayList:isEmpty	()Z
        //   1228: ifne +23 -> 1251
        //   1231: aload_0
        //   1232: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   1235: ldc_w 288
        //   1238: aload 11
        //   1240: invokestatic 294	android/text/TextUtils:join	(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;
        //   1243: aload 8
        //   1245: getfield 263	org/telegram/tgnet/TLRPC$messages_Dialogs:chats	Ljava/util/ArrayList;
        //   1248: invokevirtual 309	org/telegram/messenger/MessagesStorage:getChatsInternal	(Ljava/lang/String;Ljava/util/ArrayList;)V
        //   1251: aload 10
        //   1253: invokevirtual 284	java/util/ArrayList:isEmpty	()Z
        //   1256: ifne +23 -> 1279
        //   1259: aload_0
        //   1260: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   1263: ldc_w 288
        //   1266: aload 10
        //   1268: invokestatic 294	android/text/TextUtils:join	(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;
        //   1271: aload 8
        //   1273: getfield 260	org/telegram/tgnet/TLRPC$messages_Dialogs:users	Ljava/util/ArrayList;
        //   1276: invokevirtual 312	org/telegram/messenger/MessagesStorage:getUsersInternal	(Ljava/lang/String;Ljava/util/ArrayList;)V
        //   1279: aload_0
        //   1280: getfield 20	org/telegram/messenger/MessagesStorage$90:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   1283: invokestatic 41	org/telegram/messenger/MessagesStorage:access$300	(Lorg/telegram/messenger/MessagesStorage;)I
        //   1286: invokestatic 274	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
        //   1289: aload 8
        //   1291: aload 9
        //   1293: aload_0
        //   1294: getfield 22	org/telegram/messenger/MessagesStorage$90:val$offset	I
        //   1297: aload_0
        //   1298: getfield 24	org/telegram/messenger/MessagesStorage$90:val$count	I
        //   1301: iconst_1
        //   1302: iconst_0
        //   1303: iconst_0
        //   1304: iconst_1
        //   1305: invokevirtual 278	org/telegram/messenger/MessagesController:processLoadedDialogs	(Lorg/telegram/tgnet/TLRPC$messages_Dialogs;Ljava/util/ArrayList;IIIZZZ)V
        //   1308: return
        //   1309: iconst_0
        //   1310: istore_1
        //   1311: goto -1085 -> 226
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	1314	0	this	90
        //   228	1083	1	i	int
        //   771	187	2	j	int
        //   277	597	3	bool	boolean
        //   303	432	4	l1	long
        //   685	18	6	l2	long
        //   7	1283	8	localTL_messages_dialogs	TLRPC.TL_messages_dialogs
        //   16	1276	9	localArrayList1	ArrayList
        //   25	640	10	localArrayList2	ArrayList
        //   806	461	10	localException1	Exception
        //   56	1183	11	localArrayList3	ArrayList
        //   65	1147	12	localArrayList4	ArrayList
        //   83	1041	13	localLongSparseArray	LongSparseArray
        //   74	1118	14	localObject1	Object
        //   131	1047	15	localObject2	Object
        //   148	1021	16	localObject3	Object
        //   398	353	17	localMessage	TLRPC.Message
        //   877	3	17	localException2	Exception
        //   379	270	18	localObject4	Object
        // Exception table:
        //   from	to	target	type
        //   18	133	806	java/lang/Exception
        //   133	223	806	java/lang/Exception
        //   226	276	806	java/lang/Exception
        //   278	305	806	java/lang/Exception
        //   309	321	806	java/lang/Exception
        //   327	362	806	java/lang/Exception
        //   362	381	806	java/lang/Exception
        //   386	425	806	java/lang/Exception
        //   430	461	806	java/lang/Exception
        //   465	471	806	java/lang/Exception
        //   471	513	806	java/lang/Exception
        //   755	772	806	java/lang/Exception
        //   781	803	806	java/lang/Exception
        //   879	884	806	java/lang/Exception
        //   891	913	806	java/lang/Exception
        //   916	940	806	java/lang/Exception
        //   943	965	806	java/lang/Exception
        //   968	1021	806	java/lang/Exception
        //   1021	1037	806	java/lang/Exception
        //   1042	1138	806	java/lang/Exception
        //   1143	1188	806	java/lang/Exception
        //   1191	1196	806	java/lang/Exception
        //   1196	1223	806	java/lang/Exception
        //   1223	1251	806	java/lang/Exception
        //   1251	1279	806	java/lang/Exception
        //   1279	1308	806	java/lang/Exception
        //   513	554	877	java/lang/Exception
        //   554	573	877	java/lang/Exception
        //   578	659	877	java/lang/Exception
        //   659	671	877	java/lang/Exception
        //   671	687	877	java/lang/Exception
        //   691	719	877	java/lang/Exception
        //   719	743	877	java/lang/Exception
        //   743	755	877	java/lang/Exception
      }
    });
  }
  
  public void getDownloadQueue(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          SQLiteCursor localSQLiteCursor;
          DownloadObject localDownloadObject;
          TLRPC.MessageMedia localMessageMedia;
          try
          {
            ArrayList localArrayList = new ArrayList();
            localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT uid, type, data FROM download_queue WHERE type = %d ORDER BY date DESC LIMIT 3", new Object[] { Integer.valueOf(paramInt) }), new Object[0]);
            if (!localSQLiteCursor.next()) {
              break label192;
            }
            localDownloadObject = new DownloadObject();
            localDownloadObject.type = localSQLiteCursor.intValue(1);
            localDownloadObject.id = localSQLiteCursor.longValue(0);
            NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(2);
            if (localNativeByteBuffer != null)
            {
              localMessageMedia = TLRPC.MessageMedia.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
              localNativeByteBuffer.reuse();
              if (localMessageMedia.document != null)
              {
                localDownloadObject.object = localMessageMedia.document;
                if (localMessageMedia.ttl_seconds == 0) {
                  break label209;
                }
                bool = true;
                localDownloadObject.secret = bool;
              }
            }
            else
            {
              localArrayList.add(localDownloadObject);
              continue;
            }
            if (localMessageMedia.photo == null) {
              continue;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          localDownloadObject.object = FileLoader.getClosestPhotoSizeWithSize(localMessageMedia.photo.sizes, AndroidUtilities.getPhotoSize());
          continue;
          label192:
          localSQLiteCursor.dispose();
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              DownloadController.getInstance(MessagesStorage.this.currentAccount).processDownloadObjects(MessagesStorage.73.this.val$type, localException);
            }
          });
          return;
          label209:
          boolean bool = false;
        }
      }
    });
  }
  
  public TLRPC.EncryptedChat getEncryptedChat(int paramInt)
  {
    TLRPC.EncryptedChat localEncryptedChat = null;
    try
    {
      ArrayList localArrayList = new ArrayList();
      getEncryptedChatsInternal("" + paramInt, localArrayList, null);
      if (!localArrayList.isEmpty()) {
        localEncryptedChat = (TLRPC.EncryptedChat)localArrayList.get(0);
      }
      return localEncryptedChat;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return null;
  }
  
  public void getEncryptedChat(final int paramInt, final CountDownLatch paramCountDownLatch, final ArrayList<TLObject> paramArrayList)
  {
    if ((paramCountDownLatch == null) || (paramArrayList == null)) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          ArrayList localArrayList1 = new ArrayList();
          ArrayList localArrayList2 = new ArrayList();
          MessagesStorage.this.getEncryptedChatsInternal("" + paramInt, localArrayList2, localArrayList1);
          if ((!localArrayList2.isEmpty()) && (!localArrayList1.isEmpty()))
          {
            ArrayList localArrayList3 = new ArrayList();
            MessagesStorage.this.getUsersInternal(TextUtils.join(",", localArrayList1), localArrayList3);
            if (!localArrayList3.isEmpty())
            {
              paramArrayList.add(localArrayList2.get(0));
              paramArrayList.add(localArrayList3.get(0));
            }
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        finally
        {
          paramCountDownLatch.countDown();
        }
      }
    });
  }
  
  public void getEncryptedChatsInternal(String paramString, ArrayList<TLRPC.EncryptedChat> paramArrayList, ArrayList<Integer> paramArrayList1)
    throws Exception
  {
    if ((paramString == null) || (paramString.length() == 0) || (paramArrayList == null)) {
      return;
    }
    paramString = this.database.queryFinalized(String.format(Locale.US, "SELECT data, user, g, authkey, ttl, layer, seq_in, seq_out, use_count, exchange_id, key_date, fprint, fauthkey, khash, in_seq_no, admin_id, mtproto_seq FROM enc_chats WHERE uid IN(%s)", new Object[] { paramString }), new Object[0]);
    while (paramString.next()) {
      try
      {
        NativeByteBuffer localNativeByteBuffer = paramString.byteBufferValue(0);
        if (localNativeByteBuffer != null)
        {
          TLRPC.EncryptedChat localEncryptedChat = TLRPC.EncryptedChat.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
          localNativeByteBuffer.reuse();
          if (localEncryptedChat != null)
          {
            localEncryptedChat.user_id = paramString.intValue(1);
            if ((paramArrayList1 != null) && (!paramArrayList1.contains(Integer.valueOf(localEncryptedChat.user_id)))) {
              paramArrayList1.add(Integer.valueOf(localEncryptedChat.user_id));
            }
            localEncryptedChat.a_or_b = paramString.byteArrayValue(2);
            localEncryptedChat.auth_key = paramString.byteArrayValue(3);
            localEncryptedChat.ttl = paramString.intValue(4);
            localEncryptedChat.layer = paramString.intValue(5);
            localEncryptedChat.seq_in = paramString.intValue(6);
            localEncryptedChat.seq_out = paramString.intValue(7);
            int i = paramString.intValue(8);
            localEncryptedChat.key_use_count_in = ((short)(i >> 16));
            localEncryptedChat.key_use_count_out = ((short)i);
            localEncryptedChat.exchange_id = paramString.longValue(9);
            localEncryptedChat.key_create_date = paramString.intValue(10);
            localEncryptedChat.future_key_fingerprint = paramString.longValue(11);
            localEncryptedChat.future_auth_key = paramString.byteArrayValue(12);
            localEncryptedChat.key_hash = paramString.byteArrayValue(13);
            localEncryptedChat.in_seq_no = paramString.intValue(14);
            i = paramString.intValue(15);
            if (i != 0) {
              localEncryptedChat.admin_id = i;
            }
            localEncryptedChat.mtproto_seq = paramString.intValue(16);
            paramArrayList.add(localEncryptedChat);
          }
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    paramString.dispose();
  }
  
  public int getLastDateValue()
  {
    ensureOpened();
    return this.lastDateValue;
  }
  
  public int getLastPtsValue()
  {
    ensureOpened();
    return this.lastPtsValue;
  }
  
  public int getLastQtsValue()
  {
    ensureOpened();
    return this.lastQtsValue;
  }
  
  public int getLastSecretVersion()
  {
    ensureOpened();
    return this.lastSecretVersion;
  }
  
  public int getLastSeqValue()
  {
    ensureOpened();
    return this.lastSeqValue;
  }
  
  public void getMessages(final long paramLong, final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4, final int paramInt5, int paramInt6, final boolean paramBoolean, final int paramInt7)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        TLRPC.TL_messages_messages localTL_messages_messages = new TLRPC.TL_messages_messages();
        int i47 = 0;
        int i32 = 0;
        int i41 = 0;
        int i33 = 0;
        int i17 = 0;
        int i43 = 0;
        int i46 = 0;
        int i7 = 0;
        int i48 = 0;
        int i29 = 0;
        int i39 = 0;
        int i30 = 0;
        int i19 = 0;
        int i44 = 0;
        int i8 = 0;
        int i23 = paramInt1;
        int i36 = 0;
        int i31 = 0;
        int i49 = 0;
        int i37 = 0;
        int i42 = 0;
        int i38 = 0;
        int i10 = 0;
        int i52 = 0;
        int i50 = 0;
        int i6 = 0;
        int i28 = 0;
        int i11 = 0;
        int i15 = 0;
        int i12 = 0;
        int i16 = 0;
        int i27 = 0;
        boolean bool7 = false;
        boolean bool12 = false;
        boolean bool8 = false;
        boolean bool5 = false;
        boolean bool13 = false;
        boolean bool6 = false;
        int i51 = 0;
        int i34 = 0;
        int i40 = 0;
        int i35 = 0;
        int i9 = 0;
        int i53 = 0;
        int i45 = 0;
        int i54 = 0;
        long l2 = paramInt2;
        int i18 = paramInt2;
        int i26 = 0;
        int i22 = paramInt2;
        int i24 = 0;
        if (paramBoolean) {
          i24 = -(int)paramLong;
        }
        long l1 = l2;
        if (l2 != 0L)
        {
          l1 = l2;
          if (i24 != 0) {
            l1 = l2 | i24 << 32;
          }
        }
        boolean bool10 = false;
        boolean bool11 = false;
        boolean bool9 = false;
        int i25;
        if (paramLong == 777000L)
        {
          i25 = 10;
          i13 = i23;
          i20 = i22;
          i = i37;
          i2 = i27;
          k = i32;
          n = i34;
          bool1 = bool10;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i38;
          i3 = i28;
          m = i33;
          i1 = i35;
          bool3 = bool11;
          bool4 = bool8;
          i5 = i30;
        }
        ArrayList localArrayList1;
        ArrayList localArrayList2;
        Object localObject6;
        SparseArray localSparseArray;
        LongSparseArray localLongSparseArray;
        int i55;
        label1742:
        label2839:
        label3184:
        label3316:
        long l4;
        label4359:
        label4554:
        Object localObject5;
        label10929:
        Object localObject2;
        label12156:
        label12686:
        Object localObject4;
        for (;;)
        {
          TLRPC.Message localMessage;
          try
          {
            localArrayList1 = new ArrayList();
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            localArrayList2 = new ArrayList();
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            localObject6 = new ArrayList();
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            localSparseArray = new SparseArray();
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            localLongSparseArray = new LongSparseArray();
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            i55 = (int)paramLong;
            if (i55 == 0) {
              break label17810;
            }
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            if (paramInt4 == 3)
            {
              i13 = i23;
              i20 = i22;
              i = i37;
              i2 = i27;
              k = i32;
              n = i34;
              bool1 = bool10;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i38;
              i3 = i28;
              m = i33;
              i1 = i35;
              bool3 = bool11;
              bool4 = bool8;
              i5 = i30;
              if (paramInt3 == 0)
              {
                i13 = i23;
                i20 = i22;
                i = i37;
                i2 = i27;
                k = i32;
                n = i34;
                bool1 = bool10;
                bool2 = bool7;
                i4 = i29;
                i14 = i23;
                i21 = i22;
                j = i38;
                i3 = i28;
                m = i33;
                i1 = i35;
                bool3 = bool11;
                bool4 = bool8;
                i5 = i30;
                SQLiteCursor localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized("SELECT inbox_max, unread_count, date, unread_count_i FROM dialogs WHERE did = " + paramLong, new Object[0]);
                i9 = i54;
                i13 = i23;
                i20 = i22;
                i = i37;
                i2 = i27;
                k = i32;
                n = i34;
                bool1 = bool10;
                bool2 = bool7;
                i4 = i29;
                i14 = i23;
                i21 = i22;
                j = i38;
                i3 = i28;
                m = i33;
                i1 = i35;
                bool3 = bool11;
                bool4 = bool8;
                i5 = i30;
                if (localSQLiteCursor1.next())
                {
                  i13 = i23;
                  i20 = i22;
                  i = i37;
                  i2 = i27;
                  k = i32;
                  n = i34;
                  bool1 = bool10;
                  bool2 = bool7;
                  i4 = i29;
                  i14 = i23;
                  i21 = i22;
                  j = i38;
                  i3 = i28;
                  m = i33;
                  i1 = i35;
                  bool3 = bool11;
                  bool4 = bool8;
                  i5 = i30;
                  i6 = localSQLiteCursor1.intValue(0) + 1;
                  i13 = i23;
                  i20 = i22;
                  i = i6;
                  i2 = i27;
                  k = i32;
                  n = i34;
                  bool1 = bool10;
                  bool2 = bool7;
                  i4 = i29;
                  i14 = i23;
                  i21 = i22;
                  j = i6;
                  i3 = i28;
                  m = i33;
                  i1 = i35;
                  bool3 = bool11;
                  bool4 = bool8;
                  i5 = i30;
                  i7 = localSQLiteCursor1.intValue(1);
                  i13 = i23;
                  i20 = i22;
                  i = i6;
                  i2 = i27;
                  k = i7;
                  n = i34;
                  bool1 = bool10;
                  bool2 = bool7;
                  i4 = i29;
                  i14 = i23;
                  i21 = i22;
                  j = i6;
                  i3 = i28;
                  m = i7;
                  i1 = i35;
                  bool3 = bool11;
                  bool4 = bool8;
                  i5 = i30;
                  i9 = localSQLiteCursor1.intValue(2);
                  i13 = i23;
                  i20 = i22;
                  i = i6;
                  i2 = i27;
                  k = i7;
                  n = i9;
                  bool1 = bool10;
                  bool2 = bool7;
                  i4 = i29;
                  i14 = i23;
                  i21 = i22;
                  j = i6;
                  i3 = i28;
                  m = i7;
                  i1 = i9;
                  bool3 = bool11;
                  bool4 = bool8;
                  i5 = i30;
                  i8 = localSQLiteCursor1.intValue(3);
                }
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i7;
                n = i9;
                bool1 = bool10;
                bool2 = bool7;
                i4 = i8;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i7;
                i1 = i9;
                bool3 = bool11;
                bool4 = bool8;
                i5 = i8;
                localSQLiteCursor1.dispose();
                i19 = i31;
                l2 = l1;
                i17 = i18;
                bool5 = bool6;
                i10 = i23;
                i11 = 0;
                i15 = 0;
                i18 = 0;
                i12 = 0;
                i16 = 0;
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT start FROM messages_holes WHERE uid = %d AND start IN (0, 1)", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i8;
                if (!localSQLiteCursor1.next()) {
                  break label12686;
                }
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i8;
                if (localSQLiteCursor1.intValue(0) != 1) {
                  break label27732;
                }
                bool6 = true;
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1.dispose();
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (paramInt4 != 3)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i6;
                  i2 = i11;
                  k = i7;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i6;
                  i3 = i15;
                  m = i7;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (paramInt4 != 4)
                  {
                    if (!bool5) {
                      break label15088;
                    }
                    i13 = i10;
                    i20 = i22;
                    i = i6;
                    i2 = i11;
                    k = i7;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i6;
                    i3 = i15;
                    m = i7;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    if (paramInt4 != 2) {
                      break label15088;
                    }
                  }
                }
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT max(mid) FROM messages WHERE uid = %d AND mid > 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
                i12 = i16;
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (localSQLiteCursor1.next())
                {
                  i13 = i10;
                  i20 = i22;
                  i = i6;
                  i2 = i11;
                  k = i7;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i6;
                  i3 = i15;
                  m = i7;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  i12 = localSQLiteCursor1.intValue(0);
                }
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1.dispose();
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                i11 = i22;
                i15 = i17;
                l3 = l2;
                if (paramInt4 != 4) {
                  break label27613;
                }
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                i11 = i22;
                i15 = i17;
                l3 = l2;
                if (paramInt5 == 0) {
                  break label27613;
                }
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT max(mid) FROM messages WHERE uid = %d AND date <= %d AND mid > 0", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt5) }), new Object[0]);
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (!localSQLiteCursor1.next()) {
                  break label27738;
                }
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                i16 = localSQLiteCursor1.intValue(0);
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1.dispose();
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND date >= %d AND mid > 0", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt5) }), new Object[0]);
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (!localSQLiteCursor1.next()) {
                  break label27744;
                }
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                i18 = localSQLiteCursor1.intValue(0);
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1.dispose();
                i11 = i22;
                i15 = i17;
                l3 = l2;
                if (i16 == -1) {
                  break label27613;
                }
                i11 = i22;
                i15 = i17;
                l3 = l2;
                if (i18 == -1) {
                  break label27613;
                }
                if (i16 != i18) {
                  break label13647;
                }
                i15 = i16;
                i11 = i22;
                l3 = l2;
                break label27613;
                i = i16;
                if (i16 != 0)
                {
                  i13 = i10;
                  i20 = i11;
                  i = i6;
                  i2 = i12;
                  k = i7;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i11;
                  j = i6;
                  i3 = i12;
                  m = i7;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT start FROM messages_holes WHERE uid = %d AND start < %d AND end > %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i15), Integer.valueOf(i15) }), new Object[0]);
                  i13 = i10;
                  i20 = i11;
                  i = i6;
                  i2 = i12;
                  k = i7;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i11;
                  j = i6;
                  i3 = i12;
                  m = i7;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (localSQLiteCursor1.next()) {
                    i16 = 0;
                  }
                  i13 = i10;
                  i20 = i11;
                  i = i6;
                  i2 = i12;
                  k = i7;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i11;
                  j = i6;
                  i3 = i12;
                  m = i7;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  localSQLiteCursor1.dispose();
                  i = i16;
                }
                if (i == 0) {
                  break label14484;
                }
                l1 = 0L;
                l2 = 1L;
                i13 = i10;
                i20 = i11;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i11;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT start FROM messages_holes WHERE uid = %d AND start >= %d ORDER BY start ASC LIMIT 1", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i15) }), new Object[0]);
                i13 = i10;
                i20 = i11;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i11;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (localSQLiteCursor1.next())
                {
                  i13 = i10;
                  i20 = i11;
                  i = i6;
                  i2 = i12;
                  k = i7;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i11;
                  j = i6;
                  i3 = i12;
                  m = i7;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  l4 = localSQLiteCursor1.intValue(0);
                  l1 = l4;
                  if (i24 != 0) {
                    l1 = l4 | i24 << 32;
                  }
                }
                i13 = i10;
                i20 = i11;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i11;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1.dispose();
                i13 = i10;
                i20 = i11;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i11;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT end FROM messages_holes WHERE uid = %d AND end <= %d ORDER BY end DESC LIMIT 1", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i15) }), new Object[0]);
                i13 = i10;
                i20 = i11;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i11;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (localSQLiteCursor1.next())
                {
                  i13 = i10;
                  i20 = i11;
                  i = i6;
                  i2 = i12;
                  k = i7;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i11;
                  j = i6;
                  i3 = i12;
                  m = i7;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  l4 = localSQLiteCursor1.intValue(0);
                  l2 = l4;
                  if (i24 != 0) {
                    l2 = l4 | i24 << 32;
                  }
                }
                i13 = i10;
                i20 = i11;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i11;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1.dispose();
                if (l1 != 0L) {
                  break label27624;
                }
                if (l2 == 1L) {
                  break label14306;
                }
                break label27624;
                i13 = i10;
                i20 = i11;
                i = i6;
                i2 = i12;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i11;
                j = i6;
                i3 = i12;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localSQLiteCursor1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid <= %d AND m.mid >= %d ORDER BY m.date DESC, m.mid DESC LIMIT %d) UNION SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d AND m.mid <= %d ORDER BY m.date ASC, m.mid ASC LIMIT %d)", new Object[] { Long.valueOf(paramLong), Long.valueOf(l3), Long.valueOf(l2), Integer.valueOf(i10 / 2), Long.valueOf(paramLong), Long.valueOf(l3), Long.valueOf(l4), Integer.valueOf(i10 / 2) }), new Object[0]);
                i22 = i11;
                i11 = i6;
                i6 = i12;
                i12 = i7;
                i7 = i26;
                break label27660;
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (!localSQLiteCursor1.next()) {
                  break label22542;
                }
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                Object localObject3 = localSQLiteCursor1.byteBufferValue(1);
                if (localObject3 == null) {
                  continue;
                }
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localMessage = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject3, ((NativeByteBuffer)localObject3).readInt32(false), false);
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localMessage.readAttachPath((AbstractSerializedData)localObject3, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                ((NativeByteBuffer)localObject3).reuse();
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                MessageObject.setUnreadFlags(localMessage, localSQLiteCursor1.intValue(0));
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localMessage.id = localSQLiteCursor1.intValue(3);
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localMessage.date = localSQLiteCursor1.intValue(4);
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localMessage.dialog_id = paramLong;
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if ((localMessage.flags & 0x400) != 0)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  localMessage.views = localSQLiteCursor1.intValue(7);
                }
                if (i55 != 0)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (localMessage.ttl == 0)
                  {
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    localMessage.ttl = localSQLiteCursor1.intValue(8);
                  }
                }
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (localSQLiteCursor1.intValue(9) != 0)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  localMessage.mentioned = true;
                }
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localTL_messages_messages.messages.add(localMessage);
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                MessagesStorage.addUsersAndChatsFromMessage(localMessage, localArrayList1, localArrayList2);
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (localMessage.reply_to_msg_id == 0)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (localMessage.reply_to_random_id == 0L) {}
                }
                else
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (!localSQLiteCursor1.isNull(6))
                  {
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    localObject3 = localSQLiteCursor1.byteBufferValue(6);
                    if (localObject3 != null)
                    {
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      localMessage.replyMessage = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject3, ((NativeByteBuffer)localObject3).readInt32(false), false);
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      localMessage.replyMessage.readAttachPath((AbstractSerializedData)localObject3, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      ((NativeByteBuffer)localObject3).reuse();
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      if (localMessage.replyMessage != null)
                      {
                        i13 = i10;
                        i20 = i22;
                        i = i11;
                        i2 = i6;
                        k = i12;
                        n = i9;
                        bool1 = bool6;
                        bool2 = bool5;
                        i4 = i8;
                        i14 = i10;
                        i21 = i22;
                        j = i11;
                        i3 = i6;
                        m = i12;
                        i1 = i9;
                        bool3 = bool6;
                        bool4 = bool5;
                        i5 = i8;
                        if (MessageObject.isMegagroup(localMessage))
                        {
                          i13 = i10;
                          i20 = i22;
                          i = i11;
                          i2 = i6;
                          k = i12;
                          n = i9;
                          bool1 = bool6;
                          bool2 = bool5;
                          i4 = i8;
                          i14 = i10;
                          i21 = i22;
                          j = i11;
                          i3 = i6;
                          m = i12;
                          i1 = i9;
                          bool3 = bool6;
                          bool4 = bool5;
                          i5 = i8;
                          localObject3 = localMessage.replyMessage;
                          i13 = i10;
                          i20 = i22;
                          i = i11;
                          i2 = i6;
                          k = i12;
                          n = i9;
                          bool1 = bool6;
                          bool2 = bool5;
                          i4 = i8;
                          i14 = i10;
                          i21 = i22;
                          j = i11;
                          i3 = i6;
                          m = i12;
                          i1 = i9;
                          bool3 = bool6;
                          bool4 = bool5;
                          i5 = i8;
                          ((TLRPC.Message)localObject3).flags |= 0x80000000;
                        }
                        i13 = i10;
                        i20 = i22;
                        i = i11;
                        i2 = i6;
                        k = i12;
                        n = i9;
                        bool1 = bool6;
                        bool2 = bool5;
                        i4 = i8;
                        i14 = i10;
                        i21 = i22;
                        j = i11;
                        i3 = i6;
                        m = i12;
                        i1 = i9;
                        bool3 = bool6;
                        bool4 = bool5;
                        i5 = i8;
                        MessagesStorage.addUsersAndChatsFromMessage(localMessage.replyMessage, localArrayList1, localArrayList2);
                      }
                    }
                  }
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (localMessage.replyMessage == null)
                  {
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    if (localMessage.reply_to_msg_id == 0) {
                      break label22042;
                    }
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    l2 = localMessage.reply_to_msg_id;
                    l1 = l2;
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    if (localMessage.to_id.channel_id != 0)
                    {
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      l1 = l2 | localMessage.to_id.channel_id << 32;
                    }
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    if (!((ArrayList)localObject6).contains(Long.valueOf(l1)))
                    {
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      ((ArrayList)localObject6).add(Long.valueOf(l1));
                    }
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    localObject5 = (ArrayList)localSparseArray.get(localMessage.reply_to_msg_id);
                    localObject3 = localObject5;
                    if (localObject5 == null)
                    {
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      localObject3 = new ArrayList();
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      localSparseArray.put(localMessage.reply_to_msg_id, localObject3);
                    }
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    ((ArrayList)localObject3).add(localMessage);
                  }
                }
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localMessage.send_state = localSQLiteCursor1.intValue(2);
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (localMessage.id > 0)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (localMessage.send_state != 0)
                  {
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    localMessage.send_state = 0;
                  }
                }
                if (i55 == 0)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (!localSQLiteCursor1.isNull(5))
                  {
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    localMessage.random_id = localSQLiteCursor1.longValue(5);
                  }
                }
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                bool7 = MessageObject.isSecretPhotoOrVideo(localMessage);
                if (!bool7) {
                  continue;
                }
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                try
                {
                  localObject3 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT date FROM enc_tasks_v2 WHERE mid = %d", new Object[] { Integer.valueOf(localMessage.id) }), new Object[0]);
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (((SQLiteCursor)localObject3).next())
                  {
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    localMessage.destroyTime = ((SQLiteCursor)localObject3).intValue(0);
                  }
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  ((SQLiteCursor)localObject3).dispose();
                }
                catch (Exception localException2)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  FileLog.e(localException2);
                }
                continue;
              }
            }
          }
          catch (Exception localException1)
          {
            i14 = i13;
            i21 = i20;
            j = i;
            i3 = i2;
            m = k;
            i1 = n;
            bool3 = bool1;
            bool4 = bool2;
            i5 = i4;
            localTL_messages_messages.messages.clear();
            i14 = i13;
            i21 = i20;
            j = i;
            i3 = i2;
            m = k;
            i1 = n;
            bool3 = bool1;
            bool4 = bool2;
            i5 = i4;
            localTL_messages_messages.chats.clear();
            i14 = i13;
            i21 = i20;
            j = i;
            i3 = i2;
            m = k;
            i1 = n;
            bool3 = bool1;
            bool4 = bool2;
            i5 = i4;
            localTL_messages_messages.users.clear();
            i14 = i13;
            i21 = i20;
            j = i;
            i3 = i2;
            m = k;
            i1 = n;
            bool3 = bool1;
            bool4 = bool2;
            i5 = i4;
            FileLog.e(localException1);
            return;
            i25 = 1;
            break;
            i10 = i23;
            i6 = i49;
            i7 = i47;
            i9 = i51;
            bool5 = bool6;
            i8 = i48;
            i17 = i18;
            l2 = l1;
            i19 = i31;
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            if (paramInt4 == 1) {
              continue;
            }
            i10 = i23;
            i6 = i49;
            i7 = i47;
            i9 = i51;
            bool5 = bool6;
            i8 = i48;
            i17 = i18;
            l2 = l1;
            i19 = i31;
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            if (paramInt4 == 3) {
              continue;
            }
            i10 = i23;
            i6 = i49;
            i7 = i47;
            i9 = i51;
            bool5 = bool6;
            i8 = i48;
            i17 = i18;
            l2 = l1;
            i19 = i31;
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            if (paramInt4 == 4) {
              continue;
            }
            i10 = i23;
            i6 = i49;
            i7 = i47;
            i9 = i51;
            bool5 = bool6;
            i8 = i48;
            i17 = i18;
            l2 = l1;
            i19 = i31;
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            if (paramInt3 != 0) {
              continue;
            }
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            i7 = i50;
            i12 = i46;
            i11 = i45;
            bool6 = bool13;
            i15 = i44;
            i16 = i18;
            l2 = l1;
            if (paramInt4 != 2) {
              break label27668;
            }
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            SQLiteCursor localSQLiteCursor2 = MessagesStorage.this.database.queryFinalized("SELECT inbox_max, unread_count, date, unread_count_i FROM dialogs WHERE did = " + paramLong, new Object[0]);
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool10;
            bool2 = bool7;
            i4 = i29;
            i6 = i42;
            i8 = i41;
            i9 = i40;
            bool5 = bool12;
            i10 = i39;
            i17 = i18;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool11;
            bool4 = bool8;
            i5 = i30;
            if (localSQLiteCursor2.next())
            {
              i13 = i23;
              i20 = i22;
              i = i37;
              i2 = i27;
              k = i32;
              n = i34;
              bool1 = bool10;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i38;
              i3 = i28;
              m = i33;
              i1 = i35;
              bool3 = bool11;
              bool4 = bool8;
              i5 = i30;
              i7 = localSQLiteCursor2.intValue(0);
              i15 = i7;
              l2 = i7;
              i13 = i23;
              i20 = i22;
              i = i7;
              i2 = i27;
              k = i32;
              n = i34;
              bool1 = bool10;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i7;
              i3 = i28;
              m = i33;
              i1 = i35;
              bool3 = bool11;
              bool4 = bool8;
              i5 = i30;
              i11 = localSQLiteCursor2.intValue(1);
              i13 = i23;
              i20 = i22;
              i = i7;
              i2 = i27;
              k = i11;
              n = i34;
              bool1 = bool10;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i7;
              i3 = i28;
              m = i11;
              i1 = i35;
              bool3 = bool11;
              bool4 = bool8;
              i5 = i30;
              i12 = localSQLiteCursor2.intValue(2);
              i13 = i23;
              i20 = i22;
              i = i7;
              i2 = i27;
              k = i11;
              n = i12;
              bool1 = bool10;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i7;
              i3 = i28;
              m = i11;
              i1 = i12;
              bool3 = bool11;
              bool4 = bool8;
              i5 = i30;
              i16 = localSQLiteCursor2.intValue(3);
              bool1 = true;
              i6 = i7;
              i8 = i11;
              i9 = i12;
              bool5 = bool1;
              i10 = i16;
              i17 = i15;
              l1 = l2;
              if (l2 != 0L)
              {
                i6 = i7;
                i8 = i11;
                i9 = i12;
                bool5 = bool1;
                i10 = i16;
                i17 = i15;
                l1 = l2;
                if (i24 != 0)
                {
                  l1 = l2 | i24 << 32;
                  i17 = i15;
                  i10 = i16;
                  bool5 = bool1;
                  i9 = i12;
                  i8 = i11;
                  i6 = i7;
                }
              }
            }
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i8;
            n = i9;
            bool1 = bool10;
            bool2 = bool5;
            i4 = i10;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i8;
            i1 = i9;
            bool3 = bool11;
            bool4 = bool5;
            i5 = i10;
            localSQLiteCursor2.dispose();
            if (!bool5)
            {
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i8;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i8;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              localSQLiteCursor2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(mid), max(date) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid > 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i8;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i19 = i6;
              i18 = i9;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i8;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              if (localSQLiteCursor2.next())
              {
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i8;
                n = i9;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i10;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i8;
                i1 = i9;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i10;
                i19 = localSQLiteCursor2.intValue(0);
                i13 = i23;
                i20 = i22;
                i = i19;
                i2 = i27;
                k = i8;
                n = i9;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i10;
                i14 = i23;
                i21 = i22;
                j = i19;
                i3 = i28;
                m = i8;
                i1 = i9;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i10;
                i18 = localSQLiteCursor2.intValue(1);
              }
              i13 = i23;
              i20 = i22;
              i = i19;
              i2 = i27;
              k = i8;
              n = i18;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i14 = i23;
              i21 = i22;
              j = i19;
              i3 = i28;
              m = i8;
              i1 = i18;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              localSQLiteCursor2.dispose();
              i7 = i19;
              i12 = i8;
              i11 = i18;
              bool6 = bool5;
              i15 = i10;
              i16 = i17;
              l2 = l1;
              if (i19 != 0)
              {
                i13 = i23;
                i20 = i22;
                i = i19;
                i2 = i27;
                k = i8;
                n = i18;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i10;
                i14 = i23;
                i21 = i22;
                j = i19;
                i3 = i28;
                m = i8;
                i1 = i18;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i10;
                localSQLiteCursor2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid >= %d AND out = 0 AND read_state IN(0,2)", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i19) }), new Object[0]);
                i13 = i23;
                i20 = i22;
                i = i19;
                i2 = i27;
                k = i8;
                n = i18;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i10;
                i12 = i8;
                i14 = i23;
                i21 = i22;
                j = i19;
                i3 = i28;
                m = i8;
                i1 = i18;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i10;
                if (localSQLiteCursor2.next())
                {
                  i13 = i23;
                  i20 = i22;
                  i = i19;
                  i2 = i27;
                  k = i8;
                  n = i18;
                  bool1 = bool10;
                  bool2 = bool5;
                  i4 = i10;
                  i14 = i23;
                  i21 = i22;
                  j = i19;
                  i3 = i28;
                  m = i8;
                  i1 = i18;
                  bool3 = bool11;
                  bool4 = bool5;
                  i5 = i10;
                  i12 = localSQLiteCursor2.intValue(0);
                }
                i13 = i23;
                i20 = i22;
                i = i19;
                i2 = i27;
                k = i12;
                n = i18;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i10;
                i14 = i23;
                i21 = i22;
                j = i19;
                i3 = i28;
                m = i12;
                i1 = i18;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i10;
                localSQLiteCursor2.dispose();
                i7 = i19;
                i11 = i18;
                bool6 = bool5;
                i15 = i10;
                i16 = i17;
                l2 = l1;
                break label27668;
                i13 = i23;
                i20 = i22;
                i = i7;
                i2 = i27;
                k = i12;
                n = i11;
                bool1 = bool10;
                bool2 = bool6;
                i4 = i15;
                i14 = i23;
                i21 = i22;
                j = i7;
                i3 = i28;
                m = i12;
                i1 = i11;
                bool3 = bool11;
                bool4 = bool6;
                i5 = i15;
                i18 = Math.max(i23, i12 + 10);
                i10 = i18;
                i6 = i7;
                i7 = i12;
                i9 = i11;
                bool5 = bool6;
                i8 = i15;
                i17 = i16;
                i19 = i31;
                if (i12 >= i25) {
                  continue;
                }
                i7 = 0;
                i6 = 0;
                l2 = 0L;
                bool5 = false;
                i10 = i18;
                i9 = i11;
                i8 = i15;
                i17 = i16;
                i19 = i31;
              }
            }
            else if (i17 == 0)
            {
              i18 = 0;
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i8;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i8;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              localSQLiteCursor2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid > 0 AND out = 0 AND read_state IN(0,2)", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i8;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i8;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              if (localSQLiteCursor2.next())
              {
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i8;
                n = i9;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i10;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i8;
                i1 = i9;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i10;
                i18 = localSQLiteCursor2.intValue(0);
              }
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i8;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i8;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              localSQLiteCursor2.dispose();
              i7 = i6;
              i12 = i8;
              i11 = i9;
              bool6 = bool5;
              i15 = i10;
              i16 = i17;
              l2 = l1;
              if (i18 != i8) {
                break label27668;
              }
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i8;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i8;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              localSQLiteCursor2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid > 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i8;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i7 = i6;
              i16 = i17;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i8;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              if (localSQLiteCursor2.next())
              {
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i8;
                n = i9;
                bool1 = bool10;
                bool2 = bool5;
                i4 = i10;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i8;
                i1 = i9;
                bool3 = bool11;
                bool4 = bool5;
                i5 = i10;
                i6 = localSQLiteCursor2.intValue(0);
                i = i6;
                l2 = i6;
                i7 = i6;
                i16 = i;
                l1 = l2;
                if (l2 != 0L)
                {
                  i7 = i6;
                  i16 = i;
                  l1 = l2;
                  if (i24 != 0)
                  {
                    l1 = l2 | i24 << 32;
                    i16 = i;
                    i7 = i6;
                  }
                }
              }
              i13 = i23;
              i20 = i22;
              i = i7;
              i2 = i27;
              k = i8;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i10;
              i14 = i23;
              i21 = i22;
              j = i7;
              i3 = i28;
              m = i8;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i10;
              localSQLiteCursor2.dispose();
              i12 = i8;
              i11 = i9;
              bool6 = bool5;
              i15 = i10;
              l2 = l1;
            }
          }
          finally
          {
            MessagesController.getInstance(MessagesStorage.this.currentAccount).processLoadedMessages(localTL_messages_messages, paramLong, i14, i21, paramInt5, true, paramInt7, j, i3, m, i1, paramInt4, paramBoolean, bool3, this.val$loadIndex, bool4, i5);
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i8;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i10;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i8;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i10;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT start, end FROM messages_holes WHERE uid = %d AND start < %d AND end > %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i17), Integer.valueOf(i17) }), new Object[0]);
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i8;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i10;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i8;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i10;
          if (((SQLiteCursor)localObject2).next()) {
            break label27685;
          }
          i18 = 1;
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i8;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i10;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i8;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i10;
          ((SQLiteCursor)localObject2).dispose();
          i7 = i6;
          i12 = i8;
          i11 = i9;
          bool6 = bool5;
          i15 = i10;
          i16 = i17;
          l2 = l1;
          if (i18 == 0) {
            break label27668;
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i8;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i10;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i8;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i10;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid > %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i17) }), new Object[0]);
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i8;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i10;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i8;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i10;
          i16 = i17;
          if (((SQLiteCursor)localObject2).next())
          {
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i8;
            n = i9;
            bool1 = bool10;
            bool2 = bool5;
            i4 = i10;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i8;
            i1 = i9;
            bool3 = bool11;
            bool4 = bool5;
            i5 = i10;
            i7 = ((SQLiteCursor)localObject2).intValue(0);
            l2 = i7;
            i16 = i7;
            l1 = l2;
            if (l2 != 0L)
            {
              i16 = i7;
              l1 = l2;
              if (i24 != 0)
              {
                l1 = l2 | i24 << 32;
                i16 = i7;
              }
            }
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i8;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i10;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i8;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i10;
          ((SQLiteCursor)localObject2).dispose();
          i7 = i6;
          i12 = i8;
          i11 = i9;
          bool6 = bool5;
          i15 = i10;
          l2 = l1;
          break label27668;
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i11;
          k = i7;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i8;
          ((SQLiteCursor)localObject2).dispose();
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i11;
          k = i7;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND mid > 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i11;
          k = i7;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i8;
          if (((SQLiteCursor)localObject2).next())
          {
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i11;
            k = i7;
            n = i9;
            bool1 = bool10;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i15;
            m = i7;
            i1 = i9;
            bool3 = bool11;
            bool4 = bool5;
            i5 = i8;
            i23 = ((SQLiteCursor)localObject2).intValue(0);
            if (i23 != 0)
            {
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i8;
              localObject4 = MessagesStorage.this.database.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i8;
              ((SQLitePreparedStatement)localObject4).requery();
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i8;
              ((SQLitePreparedStatement)localObject4).bindLong(1, paramLong);
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i8;
              ((SQLitePreparedStatement)localObject4).bindInteger(2, 0);
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i8;
              ((SQLitePreparedStatement)localObject4).bindInteger(3, i23);
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i8;
              ((SQLitePreparedStatement)localObject4).step();
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool10;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool11;
              bool4 = bool5;
              i5 = i8;
              ((SQLitePreparedStatement)localObject4).dispose();
            }
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i11;
          k = i7;
          n = i9;
          bool1 = bool10;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i9;
          bool3 = bool11;
          bool4 = bool5;
          i5 = i8;
          ((SQLiteCursor)localObject2).dispose();
          bool6 = bool9;
          continue;
          label13647:
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT start FROM messages_holes WHERE uid = %d AND start <= %d AND end > %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i16), Integer.valueOf(i16) }), new Object[0]);
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (((SQLiteCursor)localObject2).next()) {
            i16 = -1;
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((SQLiteCursor)localObject2).dispose();
          i11 = i22;
          i15 = i17;
          long l3 = l2;
          if (i16 == -1) {
            break label27613;
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT start FROM messages_holes WHERE uid = %d AND start <= %d AND end > %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i18), Integer.valueOf(i18) }), new Object[0]);
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          i16 = i18;
          if (((SQLiteCursor)localObject2).next()) {
            i16 = -1;
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((SQLiteCursor)localObject2).dispose();
          i11 = i22;
          i15 = i17;
          l3 = l2;
          if (i16 == -1) {
            break label27613;
          }
          i = i16;
          j = i16;
          l1 = i16;
          i11 = i;
          i15 = j;
          l3 = l1;
          if (l1 == 0L) {
            break label27613;
          }
          i11 = i;
          i15 = j;
          l3 = l1;
          if (i24 == 0) {
            break label27613;
          }
          l3 = l1 | i24 << 32;
          i11 = i;
          i15 = j;
          break label27613;
          label14306:
          i13 = i10;
          i20 = i11;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i11;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid <= %d ORDER BY m.date DESC, m.mid DESC LIMIT %d) UNION SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d ORDER BY m.date ASC, m.mid ASC LIMIT %d)", new Object[] { Long.valueOf(paramLong), Long.valueOf(l3), Integer.valueOf(i10 / 2), Long.valueOf(paramLong), Long.valueOf(l3), Integer.valueOf(i10 / 2) }), new Object[0]);
          i22 = i11;
          i11 = i6;
          i6 = i12;
          i12 = i7;
          i7 = i26;
          break label27660;
          label14484:
          i13 = i10;
          i20 = i11;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i11;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (paramInt4 != 2) {
            break label27782;
          }
          i16 = 0;
          i13 = i10;
          i20 = i11;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i11;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid != 0 AND out = 0 AND read_state IN(0,2)", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          i13 = i10;
          i20 = i11;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i11;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (((SQLiteCursor)localObject2).next())
          {
            i13 = i10;
            i20 = i11;
            i = i6;
            i2 = i12;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i11;
            j = i6;
            i3 = i12;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            i16 = ((SQLiteCursor)localObject2).intValue(0);
          }
          i13 = i10;
          i20 = i11;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i11;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((SQLiteCursor)localObject2).dispose();
          if (i16 != i7) {
            break label27756;
          }
          i16 = 1;
          i13 = i10;
          i20 = i11;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i11;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid <= %d ORDER BY m.date DESC, m.mid DESC LIMIT %d) UNION SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d ORDER BY m.date ASC, m.mid ASC LIMIT %d)", new Object[] { Long.valueOf(paramLong), Long.valueOf(l3), Integer.valueOf(i10 / 2), Long.valueOf(paramLong), Long.valueOf(l3), Integer.valueOf(i10 / 2) }), new Object[0]);
          i22 = i11;
          i11 = i6;
          i6 = i12;
          i12 = i7;
          i7 = i16;
          break label27660;
          label15088:
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i11;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (paramInt4 == 1)
          {
            l1 = 0L;
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i11;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i15;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT start, end FROM messages_holes WHERE uid = %d AND start >= %d AND start != 1 AND end != 1 ORDER BY start ASC LIMIT 1", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt2) }), new Object[0]);
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i11;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i15;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            if (((SQLiteCursor)localObject2).next())
            {
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              l3 = ((SQLiteCursor)localObject2).intValue(0);
              l1 = l3;
              if (i24 != 0) {
                l1 = l3 | i24 << 32;
              }
            }
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i11;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i15;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            ((SQLiteCursor)localObject2).dispose();
            if (l1 != 0L)
            {
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date >= %d AND m.mid > %d AND m.mid <= %d ORDER BY m.date ASC, m.mid ASC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt3), Long.valueOf(l2), Long.valueOf(l1), Integer.valueOf(i10) }), new Object[0]);
              i11 = i6;
              i6 = i12;
              i12 = i7;
              i15 = i17;
              i7 = i26;
              break label27660;
            }
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i11;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i15;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date >= %d AND m.mid > %d ORDER BY m.date ASC, m.mid ASC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt3), Long.valueOf(l2), Integer.valueOf(i10) }), new Object[0]);
            i11 = i6;
            i6 = i12;
            i12 = i7;
            i15 = i17;
            i7 = i26;
            break label27660;
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i11;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (paramInt3 != 0)
          {
            if (l2 != 0L)
            {
              l1 = 0L;
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT end FROM messages_holes WHERE uid = %d AND end <= %d ORDER BY end DESC LIMIT 1", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt2) }), new Object[0]);
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              if (((SQLiteCursor)localObject2).next())
              {
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                l3 = ((SQLiteCursor)localObject2).intValue(0);
                l1 = l3;
                if (i24 != 0) {
                  l1 = l3 | i24 << 32;
                }
              }
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              ((SQLiteCursor)localObject2).dispose();
              if (l1 != 0L)
              {
                i13 = i10;
                i20 = i22;
                i = i6;
                i2 = i11;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i6;
                i3 = i15;
                m = i7;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d AND m.mid < %d AND (m.mid >= %d OR m.mid < 0) ORDER BY m.date DESC, m.mid DESC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt3), Long.valueOf(l2), Long.valueOf(l1), Integer.valueOf(i10) }), new Object[0]);
                i11 = i6;
                i6 = i12;
                i12 = i7;
                i15 = i17;
                i7 = i26;
                break label27660;
              }
              i13 = i10;
              i20 = i22;
              i = i6;
              i2 = i11;
              k = i7;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i6;
              i3 = i15;
              m = i7;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d AND m.mid < %d ORDER BY m.date DESC, m.mid DESC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt3), Long.valueOf(l2), Integer.valueOf(i10) }), new Object[0]);
              i11 = i6;
              i6 = i12;
              i12 = i7;
              i15 = i17;
              i7 = i26;
              break label27660;
            }
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i11;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i15;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt3), Integer.valueOf(i19), Integer.valueOf(i10) }), new Object[0]);
            i11 = i6;
            i6 = i12;
            i12 = i7;
            i15 = i17;
            i7 = i26;
            break label27660;
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i11;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT max(mid) FROM messages WHERE uid = %d AND mid > 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i11;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          i12 = i18;
          if (((SQLiteCursor)localObject2).next())
          {
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i11;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i15;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            i12 = ((SQLiteCursor)localObject2).intValue(0);
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((SQLiteCursor)localObject2).dispose();
          l1 = 0L;
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT max(end) FROM messages_holes WHERE uid = %d", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (((SQLiteCursor)localObject2).next())
          {
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i12;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i12;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            l2 = ((SQLiteCursor)localObject2).intValue(0);
            l1 = l2;
            if (i24 != 0) {
              l1 = l2 | i24 << 32;
            }
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((SQLiteCursor)localObject2).dispose();
          if (l1 != 0L)
          {
            i13 = i10;
            i20 = i22;
            i = i6;
            i2 = i12;
            k = i7;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i6;
            i3 = i12;
            m = i7;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND (m.mid >= %d OR m.mid < 0) ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d", new Object[] { Long.valueOf(paramLong), Long.valueOf(l1), Integer.valueOf(i19), Integer.valueOf(i10) }), new Object[0]);
            i11 = i6;
            i6 = i12;
            i12 = i7;
            i15 = i17;
            i7 = i26;
            break label27660;
          }
          i13 = i10;
          i20 = i22;
          i = i6;
          i2 = i12;
          k = i7;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i6;
          i3 = i12;
          m = i7;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i19), Integer.valueOf(i10) }), new Object[0]);
          i11 = i6;
          i6 = i12;
          i12 = i7;
          i15 = i17;
          i7 = i26;
          break label27660;
          label17810:
          bool9 = true;
          bool10 = true;
          bool6 = true;
          i13 = i23;
          i20 = i22;
          i = i37;
          i2 = i27;
          k = i32;
          n = i34;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i38;
          i3 = i28;
          m = i33;
          i1 = i35;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          i6 = i52;
          i7 = i43;
          i8 = i53;
          if (paramInt4 == 3)
          {
            i13 = i23;
            i20 = i22;
            i = i37;
            i2 = i27;
            k = i32;
            n = i34;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i38;
            i3 = i28;
            m = i33;
            i1 = i35;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            i6 = i52;
            i7 = i43;
            i8 = i53;
            if (paramInt3 == 0)
            {
              i13 = i23;
              i20 = i22;
              i = i37;
              i2 = i27;
              k = i32;
              n = i34;
              bool1 = bool6;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i38;
              i3 = i28;
              m = i33;
              i1 = i35;
              bool3 = bool9;
              bool4 = bool8;
              i5 = i30;
              localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND mid < 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
              i13 = i23;
              i20 = i22;
              i = i37;
              i2 = i27;
              k = i32;
              n = i34;
              bool1 = bool6;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i38;
              i3 = i28;
              m = i33;
              i1 = i35;
              bool3 = bool9;
              bool4 = bool8;
              i5 = i30;
              i6 = i10;
              if (((SQLiteCursor)localObject2).next())
              {
                i13 = i23;
                i20 = i22;
                i = i37;
                i2 = i27;
                k = i32;
                n = i34;
                bool1 = bool6;
                bool2 = bool7;
                i4 = i29;
                i14 = i23;
                i21 = i22;
                j = i38;
                i3 = i28;
                m = i33;
                i1 = i35;
                bool3 = bool9;
                bool4 = bool8;
                i5 = i30;
                i6 = ((SQLiteCursor)localObject2).intValue(0);
              }
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i32;
              n = i34;
              bool1 = bool6;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i33;
              i1 = i35;
              bool3 = bool9;
              bool4 = bool8;
              i5 = i30;
              ((SQLiteCursor)localObject2).dispose();
              i10 = 0;
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i32;
              n = i34;
              bool1 = bool6;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i33;
              i1 = i35;
              bool3 = bool9;
              bool4 = bool8;
              i5 = i30;
              localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT max(mid), max(date) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid < 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i32;
              n = i34;
              bool1 = bool6;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i33;
              i1 = i35;
              bool3 = bool9;
              bool4 = bool8;
              i5 = i30;
              if (((SQLiteCursor)localObject2).next())
              {
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i32;
                n = i34;
                bool1 = bool6;
                bool2 = bool7;
                i4 = i29;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i33;
                i1 = i35;
                bool3 = bool9;
                bool4 = bool8;
                i5 = i30;
                i10 = ((SQLiteCursor)localObject2).intValue(0);
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i32;
                n = i34;
                bool1 = bool6;
                bool2 = bool7;
                i4 = i29;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i33;
                i1 = i35;
                bool3 = bool9;
                bool4 = bool8;
                i5 = i30;
                i9 = ((SQLiteCursor)localObject2).intValue(1);
              }
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i32;
              n = i9;
              bool1 = bool6;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i33;
              i1 = i9;
              bool3 = bool9;
              bool4 = bool8;
              i5 = i30;
              ((SQLiteCursor)localObject2).dispose();
              i7 = i43;
              i8 = i9;
              if (i10 != 0)
              {
                i6 = i10;
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i32;
                n = i9;
                bool1 = bool6;
                bool2 = bool7;
                i4 = i29;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i33;
                i1 = i9;
                bool3 = bool9;
                bool4 = bool8;
                i5 = i30;
                localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid <= %d AND out = 0 AND read_state IN(0,2)", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i10) }), new Object[0]);
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i32;
                n = i9;
                bool1 = bool6;
                bool2 = bool7;
                i4 = i29;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i33;
                i1 = i9;
                bool3 = bool9;
                bool4 = bool8;
                i5 = i30;
                i7 = i17;
                if (((SQLiteCursor)localObject2).next())
                {
                  i13 = i23;
                  i20 = i22;
                  i = i6;
                  i2 = i27;
                  k = i32;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool7;
                  i4 = i29;
                  i14 = i23;
                  i21 = i22;
                  j = i6;
                  i3 = i28;
                  m = i33;
                  i1 = i9;
                  bool3 = bool9;
                  bool4 = bool8;
                  i5 = i30;
                  i7 = ((SQLiteCursor)localObject2).intValue(0);
                }
                i13 = i23;
                i20 = i22;
                i = i6;
                i2 = i27;
                k = i7;
                n = i9;
                bool1 = bool6;
                bool2 = bool7;
                i4 = i29;
                i14 = i23;
                i21 = i22;
                j = i6;
                i3 = i28;
                m = i7;
                i1 = i9;
                bool3 = bool9;
                bool4 = bool8;
                i5 = i30;
                ((SQLiteCursor)localObject2).dispose();
                i8 = i9;
              }
            }
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          if (paramInt4 != 3)
          {
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            if (paramInt4 != 4) {}
          }
          else
          {
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND mid < 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            i9 = i11;
            if (((SQLiteCursor)localObject2).next())
            {
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i7;
              n = i8;
              bool1 = bool6;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i7;
              i1 = i8;
              bool3 = bool9;
              bool4 = bool8;
              i5 = i30;
              i9 = ((SQLiteCursor)localObject2).intValue(0);
            }
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i9;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i9;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            ((SQLiteCursor)localObject2).dispose();
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i9;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i9;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid <= %d ORDER BY m.mid DESC LIMIT %d) UNION SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d ORDER BY m.mid ASC LIMIT %d)", new Object[] { Long.valueOf(paramLong), Long.valueOf(l1), Integer.valueOf(i23 / 2), Long.valueOf(paramLong), Long.valueOf(l1), Integer.valueOf(i23 / 2) }), new Object[0]);
            i10 = i23;
            i11 = i6;
            i6 = i9;
            i12 = i7;
            i9 = i8;
            bool6 = bool10;
            i8 = i19;
            i15 = i18;
            i7 = i26;
            break label27660;
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          if (paramInt4 == 1)
          {
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid < %d ORDER BY m.mid DESC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt2), Integer.valueOf(i23) }), new Object[0]);
            i10 = i23;
            i11 = i6;
            i6 = i12;
            i12 = i7;
            i9 = i8;
            bool6 = bool10;
            i8 = i19;
            i15 = i18;
            i7 = i26;
            break label27660;
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          if (paramInt3 != 0)
          {
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            if (paramInt2 != 0)
            {
              i13 = i23;
              i20 = i22;
              i = i6;
              i2 = i27;
              k = i7;
              n = i8;
              bool1 = bool6;
              bool2 = bool7;
              i4 = i29;
              i14 = i23;
              i21 = i22;
              j = i6;
              i3 = i28;
              m = i7;
              i1 = i8;
              bool3 = bool9;
              bool4 = bool8;
              i5 = i30;
              localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d ORDER BY m.mid ASC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt2), Integer.valueOf(i23) }), new Object[0]);
              i10 = i23;
              i11 = i6;
              i6 = i12;
              i12 = i7;
              i9 = i8;
              bool6 = bool10;
              i8 = i19;
              i15 = i18;
              i7 = i26;
              break label27660;
            }
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d ORDER BY m.mid ASC LIMIT %d,%d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt3), Integer.valueOf(0), Integer.valueOf(i23) }), new Object[0]);
            i10 = i23;
            i11 = i6;
            i6 = i12;
            i12 = i7;
            i9 = i8;
            bool6 = bool10;
            i8 = i19;
            i15 = i18;
            i7 = i26;
            break label27660;
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          i11 = i6;
          i12 = i16;
          i10 = i7;
          i9 = i8;
          if (paramInt4 != 2) {
            break label27808;
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM messages WHERE uid = %d AND mid < 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i27;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i28;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          if (((SQLiteCursor)localObject2).next())
          {
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i27;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i28;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            i15 = ((SQLiteCursor)localObject2).intValue(0);
          }
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i15;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          ((SQLiteCursor)localObject2).dispose();
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i15;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT max(mid), max(date) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid < 0", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          i13 = i23;
          i20 = i22;
          i = i6;
          i2 = i15;
          k = i7;
          n = i8;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i6;
          i3 = i15;
          m = i7;
          i1 = i8;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          i17 = i6;
          i16 = i8;
          if (((SQLiteCursor)localObject2).next())
          {
            i13 = i23;
            i20 = i22;
            i = i6;
            i2 = i15;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i6;
            i3 = i15;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            i17 = ((SQLiteCursor)localObject2).intValue(0);
            i13 = i23;
            i20 = i22;
            i = i17;
            i2 = i15;
            k = i7;
            n = i8;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i17;
            i3 = i15;
            m = i7;
            i1 = i8;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            i16 = ((SQLiteCursor)localObject2).intValue(1);
          }
          i13 = i23;
          i20 = i22;
          i = i17;
          i2 = i15;
          k = i7;
          n = i16;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i17;
          i3 = i15;
          m = i7;
          i1 = i16;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          ((SQLiteCursor)localObject2).dispose();
          i11 = i17;
          i12 = i15;
          i10 = i7;
          i9 = i16;
          if (i17 == 0) {
            break label27808;
          }
          i13 = i23;
          i20 = i22;
          i = i17;
          i2 = i15;
          k = i7;
          n = i16;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i17;
          i3 = i15;
          m = i7;
          i1 = i16;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid <= %d AND out = 0 AND read_state IN(0,2)", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i17) }), new Object[0]);
          i13 = i23;
          i20 = i22;
          i = i17;
          i2 = i15;
          k = i7;
          n = i16;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i17;
          i3 = i15;
          m = i7;
          i1 = i16;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          i10 = i7;
          if (((SQLiteCursor)localObject2).next())
          {
            i13 = i23;
            i20 = i22;
            i = i17;
            i2 = i15;
            k = i7;
            n = i16;
            bool1 = bool6;
            bool2 = bool7;
            i4 = i29;
            i14 = i23;
            i21 = i22;
            j = i17;
            i3 = i15;
            m = i7;
            i1 = i16;
            bool3 = bool9;
            bool4 = bool8;
            i5 = i30;
            i10 = ((SQLiteCursor)localObject2).intValue(0);
          }
          i13 = i23;
          i20 = i22;
          i = i17;
          i2 = i15;
          k = i10;
          n = i16;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i17;
          i3 = i15;
          m = i10;
          i1 = i16;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          ((SQLiteCursor)localObject2).dispose();
          i11 = i17;
          i12 = i15;
          i9 = i16;
          break label27808;
          label21774:
          i13 = i23;
          i20 = i22;
          i = i11;
          i2 = i12;
          k = i10;
          n = i9;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i23;
          i21 = i22;
          j = i11;
          i3 = i12;
          m = i10;
          i1 = i9;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          i15 = Math.max(i23, i10 + 10);
          i7 = i15;
          i6 = i12;
          i12 = i10;
          i8 = i36;
          if (i10 < i25)
          {
            i12 = 0;
            i11 = 0;
            i6 = 0;
            i8 = i36;
            i7 = i15;
          }
          label21895:
          i13 = i7;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool7;
          i4 = i29;
          i14 = i7;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool9;
          bool4 = bool8;
          i5 = i30;
          localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media, m.ttl, m.mention FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d ORDER BY m.mid ASC LIMIT %d,%d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(i8), Integer.valueOf(i7) }), new Object[0]);
          i10 = i7;
          bool6 = bool10;
          i8 = i19;
          i15 = i18;
          i7 = i26;
          break label27660;
          label22042:
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (!((ArrayList)localObject6).contains(Long.valueOf(localMessage.reply_to_random_id)))
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            ((ArrayList)localObject6).add(Long.valueOf(localMessage.reply_to_random_id));
          }
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject5 = (ArrayList)localLongSparseArray.get(localMessage.reply_to_random_id);
          localObject4 = localObject5;
          if (localObject5 == null)
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localObject4 = new ArrayList();
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localLongSparseArray.put(localMessage.reply_to_random_id, localObject4);
          }
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((ArrayList)localObject4).add(localMessage);
        }
        label22542:
        int i13 = i10;
        int i20 = i22;
        int i = i11;
        int i2 = i6;
        int k = i12;
        int n = i9;
        boolean bool1 = bool6;
        boolean bool2 = bool5;
        int i4 = i8;
        int i14 = i10;
        int i21 = i22;
        int j = i11;
        int i3 = i6;
        int m = i12;
        int i1 = i9;
        boolean bool3 = bool6;
        boolean bool4 = bool5;
        int i5 = i8;
        ((SQLiteCursor)localObject2).dispose();
        label22616:
        i13 = i10;
        i20 = i22;
        i = i11;
        i2 = i6;
        k = i12;
        n = i9;
        bool1 = bool6;
        bool2 = bool5;
        i4 = i8;
        i14 = i10;
        i21 = i22;
        j = i11;
        i3 = i6;
        m = i12;
        i1 = i9;
        bool3 = bool6;
        bool4 = bool5;
        i5 = i8;
        Collections.sort(localTL_messages_messages.messages, new Comparator()
        {
          public int compare(TLRPC.Message paramAnonymous2Message1, TLRPC.Message paramAnonymous2Message2)
          {
            if ((paramAnonymous2Message1.id > 0) && (paramAnonymous2Message2.id > 0)) {
              if (paramAnonymous2Message1.id <= paramAnonymous2Message2.id) {}
            }
            do
            {
              do
              {
                return -1;
                if (paramAnonymous2Message1.id >= paramAnonymous2Message2.id) {
                  break label102;
                }
                return 1;
                if ((paramAnonymous2Message1.id >= 0) || (paramAnonymous2Message2.id >= 0)) {
                  break;
                }
              } while (paramAnonymous2Message1.id < paramAnonymous2Message2.id);
              if (paramAnonymous2Message1.id <= paramAnonymous2Message2.id) {
                break;
              }
              return 1;
            } while (paramAnonymous2Message1.date > paramAnonymous2Message2.date);
            if (paramAnonymous2Message1.date < paramAnonymous2Message2.date) {
              return 1;
            }
            label102:
            return 0;
          }
        });
        if (i55 != 0)
        {
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (paramInt4 != 3)
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            if (paramInt4 != 4)
            {
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              if ((paramInt4 != 2) || (!bool5) || (i7 != 0)) {
                break label23521;
              }
            }
          }
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (!localTL_messages_messages.messages.isEmpty())
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            i7 = ((TLRPC.Message)localTL_messages_messages.messages.get(localTL_messages_messages.messages.size() - 1)).id;
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            i16 = ((TLRPC.Message)localTL_messages_messages.messages.get(0)).id;
            if ((i7 > i15) || (i16 < i15))
            {
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              ((ArrayList)localObject6).clear();
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localArrayList1.clear();
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localArrayList2.clear();
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localTL_messages_messages.messages.clear();
            }
          }
          label23521:
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (paramInt4 != 4)
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            if (paramInt4 != 3) {}
          }
          else
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            if (localTL_messages_messages.messages.size() == 1)
            {
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localTL_messages_messages.messages.clear();
            }
          }
        }
        i13 = i10;
        i20 = i22;
        i = i11;
        i2 = i6;
        k = i12;
        n = i9;
        bool1 = bool6;
        bool2 = bool5;
        i4 = i8;
        i14 = i10;
        i21 = i22;
        j = i11;
        i3 = i6;
        m = i12;
        i1 = i9;
        bool3 = bool6;
        bool4 = bool5;
        i5 = i8;
        if (!((ArrayList)localObject6).isEmpty())
        {
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (localSparseArray.size() > 0)
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data, mid, date FROM messages WHERE mid IN(%s)", new Object[] { TextUtils.join(",", (Iterable)localObject6) }), new Object[0]);
          }
          label24986:
          label25814:
          do
          {
            do
            {
              for (;;)
              {
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                if (!((SQLiteCursor)localObject2).next()) {
                  break label26367;
                }
                i13 = i10;
                i20 = i22;
                i = i11;
                i2 = i6;
                k = i12;
                n = i9;
                bool1 = bool6;
                bool2 = bool5;
                i4 = i8;
                i14 = i10;
                i21 = i22;
                j = i11;
                i3 = i6;
                m = i12;
                i1 = i9;
                bool3 = bool6;
                bool4 = bool5;
                i5 = i8;
                localObject5 = ((SQLiteCursor)localObject2).byteBufferValue(0);
                if (localObject5 != null)
                {
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  localObject4 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject5, ((NativeByteBuffer)localObject5).readInt32(false), false);
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  ((TLRPC.Message)localObject4).readAttachPath((AbstractSerializedData)localObject5, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  ((NativeByteBuffer)localObject5).reuse();
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  ((TLRPC.Message)localObject4).id = ((SQLiteCursor)localObject2).intValue(1);
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  ((TLRPC.Message)localObject4).date = ((SQLiteCursor)localObject2).intValue(2);
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  ((TLRPC.Message)localObject4).dialog_id = paramLong;
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  MessagesStorage.addUsersAndChatsFromMessage((TLRPC.Message)localObject4, localArrayList1, localArrayList2);
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  if (localSparseArray.size() <= 0) {
                    break;
                  }
                  i13 = i10;
                  i20 = i22;
                  i = i11;
                  i2 = i6;
                  k = i12;
                  n = i9;
                  bool1 = bool6;
                  bool2 = bool5;
                  i4 = i8;
                  i14 = i10;
                  i21 = i22;
                  j = i11;
                  i3 = i6;
                  m = i12;
                  i1 = i9;
                  bool3 = bool6;
                  bool4 = bool5;
                  i5 = i8;
                  localObject5 = (ArrayList)localSparseArray.get(((TLRPC.Message)localObject4).id);
                  if (localObject5 != null)
                  {
                    i7 = 0;
                    i13 = i10;
                    i20 = i22;
                    i = i11;
                    i2 = i6;
                    k = i12;
                    n = i9;
                    bool1 = bool6;
                    bool2 = bool5;
                    i4 = i8;
                    i14 = i10;
                    i21 = i22;
                    j = i11;
                    i3 = i6;
                    m = i12;
                    i1 = i9;
                    bool3 = bool6;
                    bool4 = bool5;
                    i5 = i8;
                    if (i7 < ((ArrayList)localObject5).size())
                    {
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      localObject6 = (TLRPC.Message)((ArrayList)localObject5).get(i7);
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      ((TLRPC.Message)localObject6).replyMessage = ((TLRPC.Message)localObject4);
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      if (!MessageObject.isMegagroup((TLRPC.Message)localObject6)) {
                        break label27850;
                      }
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      localObject6 = ((TLRPC.Message)localObject6).replyMessage;
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      ((TLRPC.Message)localObject6).flags |= 0x80000000;
                      break label27850;
                      i13 = i10;
                      i20 = i22;
                      i = i11;
                      i2 = i6;
                      k = i12;
                      n = i9;
                      bool1 = bool6;
                      bool2 = bool5;
                      i4 = i8;
                      i14 = i10;
                      i21 = i22;
                      j = i11;
                      i3 = i6;
                      m = i12;
                      i1 = i9;
                      bool3 = bool6;
                      bool4 = bool5;
                      i5 = i8;
                      localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.data, m.mid, m.date, r.random_id FROM randoms as r INNER JOIN messages as m ON r.mid = m.mid WHERE r.random_id IN(%s)", new Object[] { TextUtils.join(",", (Iterable)localObject6) }), new Object[0]);
                    }
                  }
                }
              }
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              l1 = ((SQLiteCursor)localObject2).longValue(3);
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localObject5 = (ArrayList)localLongSparseArray.get(l1);
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              localLongSparseArray.remove(l1);
            } while (localObject5 == null);
            i7 = 0;
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
          } while (i7 >= ((ArrayList)localObject5).size());
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject6 = (TLRPC.Message)((ArrayList)localObject5).get(i7);
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((TLRPC.Message)localObject6).replyMessage = ((TLRPC.Message)localObject4);
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((TLRPC.Message)localObject6).reply_to_msg_id = ((TLRPC.Message)localObject4).id;
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (!MessageObject.isMegagroup((TLRPC.Message)localObject6)) {
            break label27859;
          }
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          localObject6 = ((TLRPC.Message)localObject6).replyMessage;
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((TLRPC.Message)localObject6).flags |= 0x80000000;
          break label27859;
          label26367:
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          ((SQLiteCursor)localObject2).dispose();
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (localLongSparseArray.size() > 0) {
            i7 = 0;
          }
        }
        for (;;)
        {
          i13 = i10;
          i20 = i22;
          i = i11;
          i2 = i6;
          k = i12;
          n = i9;
          bool1 = bool6;
          bool2 = bool5;
          i4 = i8;
          i14 = i10;
          i21 = i22;
          j = i11;
          i3 = i6;
          m = i12;
          i1 = i9;
          bool3 = bool6;
          bool4 = bool5;
          i5 = i8;
          if (i7 < localLongSparseArray.size())
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localObject2 = (ArrayList)localLongSparseArray.valueAt(i7);
            i15 = 0;
            for (;;)
            {
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              if (i15 >= ((ArrayList)localObject2).size()) {
                break;
              }
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i8;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i8;
              ((TLRPC.Message)((ArrayList)localObject2).get(i15)).reply_to_random_id = 0L;
              i15 += 1;
            }
          }
          i7 = i8;
          if (i8 != 0)
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(mid) FROM messages WHERE uid = %d AND mention = 1 AND read_state IN(0, 1)", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            if (!((SQLiteCursor)localObject2).next()) {
              break label27604;
            }
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i8;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i8;
            i7 = i8;
            if (i8 == ((SQLiteCursor)localObject2).intValue(0)) {}
          }
          label27604:
          for (i7 = i8 * -1;; i7 = i8 * -1)
          {
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i7;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i7;
            ((SQLiteCursor)localObject2).dispose();
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i7;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i7;
            if (!localArrayList1.isEmpty())
            {
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i7;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i7;
              MessagesStorage.this.getUsersInternal(TextUtils.join(",", localArrayList1), localTL_messages_messages.users);
            }
            i13 = i10;
            i20 = i22;
            i = i11;
            i2 = i6;
            k = i12;
            n = i9;
            bool1 = bool6;
            bool2 = bool5;
            i4 = i7;
            i14 = i10;
            i21 = i22;
            j = i11;
            i3 = i6;
            m = i12;
            i1 = i9;
            bool3 = bool6;
            bool4 = bool5;
            i5 = i7;
            if (!localArrayList2.isEmpty())
            {
              i13 = i10;
              i20 = i22;
              i = i11;
              i2 = i6;
              k = i12;
              n = i9;
              bool1 = bool6;
              bool2 = bool5;
              i4 = i7;
              i14 = i10;
              i21 = i22;
              j = i11;
              i3 = i6;
              m = i12;
              i1 = i9;
              bool3 = bool6;
              bool4 = bool5;
              i5 = i7;
              MessagesStorage.this.getChatsInternal(TextUtils.join(",", localArrayList2), localTL_messages_messages.chats);
            }
            MessagesController.getInstance(MessagesStorage.this.currentAccount).processLoadedMessages(localTL_messages_messages, paramLong, i10, i22, paramInt5, true, paramInt7, i11, i6, i12, i9, paramInt4, paramBoolean, bool6, this.val$loadIndex, bool5, i7);
            return;
          }
          label27613:
          if (i15 != 0)
          {
            i16 = 1;
            break label3316;
            label27624:
            l4 = l1;
            if (l1 != 0L) {
              break label4359;
            }
            l4 = 1000000000L;
            if (i24 == 0) {
              break label4359;
            }
            l4 = 0x3B9ACA00 | i24 << 32;
            break label4359;
          }
          for (;;)
          {
            label27660:
            if (localObject2 == null) {
              break label27806;
            }
            break label4554;
            label27668:
            if (i23 > i12) {
              break label10929;
            }
            if (i12 < i25)
            {
              break label10929;
              label27685:
              i18 = 0;
              break label12156;
            }
            i19 = i12 - i23;
            i10 = i23 + 10;
            i6 = i7;
            i7 = i12;
            i9 = i11;
            bool5 = bool6;
            i8 = i15;
            i17 = i16;
            break;
            label27732:
            bool6 = false;
            break label1742;
            label27738:
            i16 = -1;
            break label2839;
            label27744:
            i18 = -1;
            break label3184;
            i16 = 0;
            break label3316;
            label27756:
            localObject2 = null;
            i22 = i11;
            i11 = i6;
            i6 = i12;
            i12 = i7;
            i7 = i26;
            continue;
            label27782:
            localObject2 = null;
            i22 = i11;
            i11 = i6;
            i6 = i12;
            i12 = i7;
            i7 = i26;
          }
          label27806:
          break label22616;
          label27808:
          if ((i23 > i10) || (i10 < i25)) {
            break label21774;
          }
          i8 = i10 - i23;
          i7 = i23 + 10;
          i6 = i12;
          i12 = i10;
          break label21895;
          label27850:
          i7 += 1;
          break label24986;
          label27859:
          i7 += 1;
          break label25814;
          i7 += 1;
        }
      }
    });
  }
  
  public void getNewTask(final ArrayList<Integer> paramArrayList, int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i;
        int j;
        try
        {
          if (paramArrayList != null)
          {
            localObject1 = TextUtils.join(",", paramArrayList);
            MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM enc_tasks_v2 WHERE mid IN(%s)", new Object[] { localObject1 })).stepThis().dispose();
          }
          i = 0;
          j = -1;
          Object localObject1 = null;
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT mid, date FROM enc_tasks_v2 WHERE date = (SELECT min(date) FROM enc_tasks_v2)", new Object[0]);
          while (localSQLiteCursor.next())
          {
            long l = localSQLiteCursor.longValue(0);
            i = j;
            if (j == -1)
            {
              j = (int)(l >> 32);
              i = j;
              if (j < 0) {
                i = 0;
              }
            }
            int k = localSQLiteCursor.intValue(1);
            Object localObject2 = localObject1;
            if (localObject1 == null) {
              localObject2 = new ArrayList();
            }
            ((ArrayList)localObject2).add(Integer.valueOf((int)l));
            localObject1 = localObject2;
            j = i;
            i = k;
          }
          localSQLiteCursor.dispose();
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        MessagesController.getInstance(MessagesStorage.this.currentAccount).processLoadedDeleteTask(i, localException, j);
      }
    });
  }
  
  public int getSecretG()
  {
    ensureOpened();
    return this.secretG;
  }
  
  public byte[] getSecretPBytes()
  {
    ensureOpened();
    return this.secretPBytes;
  }
  
  public TLObject getSentFile(final String paramString, final int paramInt)
  {
    if ((paramString == null) || (paramString.toLowerCase().endsWith("attheme"))) {
      return null;
    }
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final ArrayList localArrayList = new ArrayList();
    this.storageQueue.postRunnable(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: aload_0
        //   1: getfield 27	org/telegram/messenger/MessagesStorage$60:val$path	Ljava/lang/String;
        //   4: invokestatic 46	org/telegram/messenger/Utilities:MD5	(Ljava/lang/String;)Ljava/lang/String;
        //   7: astore_1
        //   8: aload_1
        //   9: ifnull +102 -> 111
        //   12: aload_0
        //   13: getfield 25	org/telegram/messenger/MessagesStorage$60:this$0	Lorg/telegram/messenger/MessagesStorage;
        //   16: invokestatic 50	org/telegram/messenger/MessagesStorage:access$000	(Lorg/telegram/messenger/MessagesStorage;)Lorg/telegram/SQLite/SQLiteDatabase;
        //   19: getstatic 56	java/util/Locale:US	Ljava/util/Locale;
        //   22: ldc 58
        //   24: iconst_2
        //   25: anewarray 4	java/lang/Object
        //   28: dup
        //   29: iconst_0
        //   30: aload_1
        //   31: aastore
        //   32: dup
        //   33: iconst_1
        //   34: aload_0
        //   35: getfield 29	org/telegram/messenger/MessagesStorage$60:val$type	I
        //   38: invokestatic 64	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
        //   41: aastore
        //   42: invokestatic 70	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
        //   45: iconst_0
        //   46: anewarray 4	java/lang/Object
        //   49: invokevirtual 76	org/telegram/SQLite/SQLiteDatabase:queryFinalized	(Ljava/lang/String;[Ljava/lang/Object;)Lorg/telegram/SQLite/SQLiteCursor;
        //   52: astore_1
        //   53: aload_1
        //   54: invokevirtual 82	org/telegram/SQLite/SQLiteCursor:next	()Z
        //   57: ifeq +50 -> 107
        //   60: aload_1
        //   61: iconst_0
        //   62: invokevirtual 86	org/telegram/SQLite/SQLiteCursor:byteBufferValue	(I)Lorg/telegram/tgnet/NativeByteBuffer;
        //   65: astore_2
        //   66: aload_2
        //   67: ifnull +40 -> 107
        //   70: aload_2
        //   71: aload_2
        //   72: iconst_0
        //   73: invokevirtual 92	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
        //   76: iconst_0
        //   77: invokestatic 98	org/telegram/tgnet/TLRPC$MessageMedia:TLdeserialize	(Lorg/telegram/tgnet/AbstractSerializedData;IZ)Lorg/telegram/tgnet/TLRPC$MessageMedia;
        //   80: astore_3
        //   81: aload_2
        //   82: invokevirtual 101	org/telegram/tgnet/NativeByteBuffer:reuse	()V
        //   85: aload_3
        //   86: instanceof 103
        //   89: ifeq +30 -> 119
        //   92: aload_0
        //   93: getfield 31	org/telegram/messenger/MessagesStorage$60:val$result	Ljava/util/ArrayList;
        //   96: aload_3
        //   97: checkcast 103	org/telegram/tgnet/TLRPC$TL_messageMediaDocument
        //   100: getfield 107	org/telegram/tgnet/TLRPC$TL_messageMediaDocument:document	Lorg/telegram/tgnet/TLRPC$Document;
        //   103: invokevirtual 113	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   106: pop
        //   107: aload_1
        //   108: invokevirtual 116	org/telegram/SQLite/SQLiteCursor:dispose	()V
        //   111: aload_0
        //   112: getfield 33	org/telegram/messenger/MessagesStorage$60:val$countDownLatch	Ljava/util/concurrent/CountDownLatch;
        //   115: invokevirtual 121	java/util/concurrent/CountDownLatch:countDown	()V
        //   118: return
        //   119: aload_3
        //   120: instanceof 123
        //   123: ifeq -16 -> 107
        //   126: aload_0
        //   127: getfield 31	org/telegram/messenger/MessagesStorage$60:val$result	Ljava/util/ArrayList;
        //   130: aload_3
        //   131: checkcast 123	org/telegram/tgnet/TLRPC$TL_messageMediaPhoto
        //   134: getfield 127	org/telegram/tgnet/TLRPC$TL_messageMediaPhoto:photo	Lorg/telegram/tgnet/TLRPC$Photo;
        //   137: invokevirtual 113	java/util/ArrayList:add	(Ljava/lang/Object;)Z
        //   140: pop
        //   141: goto -34 -> 107
        //   144: astore_1
        //   145: aload_1
        //   146: invokestatic 133	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   149: aload_0
        //   150: getfield 33	org/telegram/messenger/MessagesStorage$60:val$countDownLatch	Ljava/util/concurrent/CountDownLatch;
        //   153: invokevirtual 121	java/util/concurrent/CountDownLatch:countDown	()V
        //   156: return
        //   157: astore_1
        //   158: aload_0
        //   159: getfield 33	org/telegram/messenger/MessagesStorage$60:val$countDownLatch	Ljava/util/concurrent/CountDownLatch;
        //   162: invokevirtual 121	java/util/concurrent/CountDownLatch:countDown	()V
        //   165: aload_1
        //   166: athrow
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	167	0	this	60
        //   7	101	1	localObject1	Object
        //   144	2	1	localException	Exception
        //   157	9	1	localObject2	Object
        //   65	17	2	localNativeByteBuffer	NativeByteBuffer
        //   80	51	3	localMessageMedia	TLRPC.MessageMedia
        // Exception table:
        //   from	to	target	type
        //   0	8	144	java/lang/Exception
        //   12	66	144	java/lang/Exception
        //   70	107	144	java/lang/Exception
        //   107	111	144	java/lang/Exception
        //   119	141	144	java/lang/Exception
        //   0	8	157	finally
        //   12	66	157	finally
        //   70	107	157	finally
        //   107	111	157	finally
        //   119	141	157	finally
        //   145	149	157	finally
      }
    });
    try
    {
      localCountDownLatch.await();
      if (!localArrayList.isEmpty()) {
        return (TLObject)localArrayList.get(0);
      }
    }
    catch (Exception paramString)
    {
      for (;;)
      {
        FileLog.e(paramString);
      }
    }
    return null;
  }
  
  public DispatchQueue getStorageQueue()
  {
    return this.storageQueue;
  }
  
  public void getUnreadMention(final long paramLong, IntCallback paramIntCallback)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT MIN(mid) FROM messages WHERE uid = %d AND mention = 1 AND read_state IN(0, 1)", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          if (localSQLiteCursor.next()) {}
          for (final int i = localSQLiteCursor.intValue(0);; i = 0)
          {
            localSQLiteCursor.dispose();
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesStorage.57.this.val$callback.run(i);
              }
            });
            return;
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void getUnsentMessages(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject2;
        ArrayList localArrayList2;
        ArrayList localArrayList3;
        ArrayList localArrayList4;
        Object localObject3;
        Object localObject1;
        ArrayList localArrayList5;
        ArrayList localArrayList6;
        SQLiteCursor localSQLiteCursor;
        int i;
        for (;;)
        {
          try
          {
            localObject2 = new SparseArray();
            ArrayList localArrayList1 = new ArrayList();
            localArrayList2 = new ArrayList();
            localArrayList3 = new ArrayList();
            localArrayList4 = new ArrayList();
            localObject3 = new ArrayList();
            localObject1 = new ArrayList();
            localArrayList5 = new ArrayList();
            localArrayList6 = new ArrayList();
            localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.uid, s.seq_in, s.seq_out, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid LEFT JOIN messages_seq as s ON m.mid = s.mid WHERE m.mid < 0 AND m.send_state = 1 ORDER BY m.mid DESC LIMIT " + paramInt, new Object[0]);
            if (!localSQLiteCursor.next()) {
              break;
            }
            NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(1);
            if (localNativeByteBuffer == null) {
              continue;
            }
            TLRPC.Message localMessage = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
            localMessage.readAttachPath(localNativeByteBuffer, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
            localNativeByteBuffer.reuse();
            if (((SparseArray)localObject2).indexOfKey(localMessage.id) >= 0) {
              continue;
            }
            MessageObject.setUnreadFlags(localMessage, localSQLiteCursor.intValue(0));
            localMessage.id = localSQLiteCursor.intValue(3);
            localMessage.date = localSQLiteCursor.intValue(4);
            if (!localSQLiteCursor.isNull(5)) {
              localMessage.random_id = localSQLiteCursor.longValue(5);
            }
            localMessage.dialog_id = localSQLiteCursor.longValue(6);
            localMessage.seq_in = localSQLiteCursor.intValue(7);
            localMessage.seq_out = localSQLiteCursor.intValue(8);
            localMessage.ttl = localSQLiteCursor.intValue(9);
            localArrayList1.add(localMessage);
            ((SparseArray)localObject2).put(localMessage.id, localMessage);
            i = (int)localMessage.dialog_id;
            j = (int)(localMessage.dialog_id >> 32);
            if (i == 0) {
              break label507;
            }
            if (j == 1)
            {
              if (!localArrayList5.contains(Integer.valueOf(i))) {
                localArrayList5.add(Integer.valueOf(i));
              }
              MessagesStorage.addUsersAndChatsFromMessage(localMessage, (ArrayList)localObject3, (ArrayList)localObject1);
              localMessage.send_state = localSQLiteCursor.intValue(2);
              if (((localMessage.to_id.channel_id == 0) && (!MessageObject.isUnread(localMessage)) && (i != 0)) || (localMessage.id > 0)) {
                localMessage.send_state = 0;
              }
              if ((i != 0) || (localSQLiteCursor.isNull(5))) {
                continue;
              }
              localMessage.random_id = localSQLiteCursor.longValue(5);
              continue;
            }
            if (i >= 0) {
              break label482;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          int j = -i;
          if (!((ArrayList)localObject1).contains(Integer.valueOf(j)))
          {
            ((ArrayList)localObject1).add(Integer.valueOf(-i));
            continue;
            label482:
            if (!((ArrayList)localObject3).contains(Integer.valueOf(i)))
            {
              ((ArrayList)localObject3).add(Integer.valueOf(i));
              continue;
              label507:
              if (!localArrayList6.contains(Integer.valueOf(j))) {
                localArrayList6.add(Integer.valueOf(j));
              }
            }
          }
        }
        localSQLiteCursor.dispose();
        if (!localArrayList6.isEmpty()) {
          MessagesStorage.this.getEncryptedChatsInternal(TextUtils.join(",", localArrayList6), localArrayList4, (ArrayList)localObject3);
        }
        if (!((ArrayList)localObject3).isEmpty()) {
          MessagesStorage.this.getUsersInternal(TextUtils.join(",", (Iterable)localObject3), localArrayList2);
        }
        if ((!((ArrayList)localObject1).isEmpty()) || (!localArrayList5.isEmpty()))
        {
          localObject2 = new StringBuilder();
          i = 0;
          while (i < ((ArrayList)localObject1).size())
          {
            localObject3 = (Integer)((ArrayList)localObject1).get(i);
            if (((StringBuilder)localObject2).length() != 0) {
              ((StringBuilder)localObject2).append(",");
            }
            ((StringBuilder)localObject2).append(localObject3);
            i += 1;
          }
        }
        for (;;)
        {
          if (i < localArrayList5.size())
          {
            localObject1 = (Integer)localArrayList5.get(i);
            if (((StringBuilder)localObject2).length() != 0) {
              ((StringBuilder)localObject2).append(",");
            }
            ((StringBuilder)localObject2).append(-((Integer)localObject1).intValue());
            i += 1;
          }
          else
          {
            MessagesStorage.this.getChatsInternal(((StringBuilder)localObject2).toString(), localArrayList3);
            SendMessagesHelper.getInstance(MessagesStorage.this.currentAccount).processUnsentMessages(localException, localArrayList2, localArrayList3, localArrayList4);
            return;
            i = 0;
          }
        }
      }
    });
  }
  
  public TLRPC.User getUser(int paramInt)
  {
    TLRPC.User localUser = null;
    try
    {
      ArrayList localArrayList = new ArrayList();
      getUsersInternal("" + paramInt, localArrayList);
      if (!localArrayList.isEmpty()) {
        localUser = (TLRPC.User)localArrayList.get(0);
      }
      return localUser;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return null;
  }
  
  public TLRPC.User getUserSync(final int paramInt)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final TLRPC.User[] arrayOfUser = new TLRPC.User[1];
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        arrayOfUser[0] = MessagesStorage.this.getUser(paramInt);
        localCountDownLatch.countDown();
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfUser[0];
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public ArrayList<TLRPC.User> getUsers(ArrayList<Integer> paramArrayList)
  {
    ArrayList localArrayList = new ArrayList();
    try
    {
      getUsersInternal(TextUtils.join(",", paramArrayList), localArrayList);
      return localArrayList;
    }
    catch (Exception paramArrayList)
    {
      localArrayList.clear();
      FileLog.e(paramArrayList);
    }
    return localArrayList;
  }
  
  public void getUsersInternal(String paramString, ArrayList<TLRPC.User> paramArrayList)
    throws Exception
  {
    if ((paramString == null) || (paramString.length() == 0) || (paramArrayList == null)) {
      return;
    }
    paramString = this.database.queryFinalized(String.format(Locale.US, "SELECT data, status FROM users WHERE uid IN(%s)", new Object[] { paramString }), new Object[0]);
    while (paramString.next()) {
      try
      {
        NativeByteBuffer localNativeByteBuffer = paramString.byteBufferValue(0);
        if (localNativeByteBuffer != null)
        {
          TLRPC.User localUser = TLRPC.User.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
          localNativeByteBuffer.reuse();
          if (localUser != null)
          {
            if (localUser.status != null) {
              localUser.status.expires = paramString.intValue(1);
            }
            paramArrayList.add(localUser);
          }
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    paramString.dispose();
  }
  
  public void getWallpapers()
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final ArrayList localArrayList;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT data FROM wallpapers WHERE 1", new Object[0]);
          localArrayList = new ArrayList();
          while (localSQLiteCursor.next())
          {
            NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
            if (localNativeByteBuffer != null)
            {
              TLRPC.WallPaper localWallPaper = TLRPC.WallPaper.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
              localNativeByteBuffer.reuse();
              localArrayList.add(localWallPaper);
            }
          }
          localException.dispose();
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.wallpapersDidLoaded, new Object[] { localArrayList });
          }
        });
      }
    });
  }
  
  public boolean hasAuthMessage(final int paramInt)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final boolean[] arrayOfBoolean = new boolean[1];
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM messages WHERE uid = 777000 AND date = %d AND mid < 0 LIMIT 1", new Object[] { Integer.valueOf(paramInt) }), new Object[0]);
          arrayOfBoolean[0] = localSQLiteCursor.next();
          localSQLiteCursor.dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        finally
        {
          localCountDownLatch.countDown();
        }
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfBoolean[0];
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public boolean isDialogHasMessages(final long paramLong)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    boolean[] arrayOfBoolean = new boolean[1];
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM messages WHERE uid = %d LIMIT 1", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          localCountDownLatch[0] = localSQLiteCursor.next();
          localSQLiteCursor.dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        finally
        {
          this.val$countDownLatch.countDown();
        }
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfBoolean[0];
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public boolean isMigratedChat(final int paramInt)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final boolean[] arrayOfBoolean = new boolean[1];
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int j = 0;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT info FROM chat_settings_v2 WHERE uid = " + paramInt, new Object[0]);
          boolean[] arrayOfBoolean = null;
          new ArrayList();
          Object localObject1 = arrayOfBoolean;
          if (localSQLiteCursor.next())
          {
            NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
            localObject1 = arrayOfBoolean;
            if (localNativeByteBuffer != null)
            {
              localObject1 = TLRPC.ChatFull.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
              localNativeByteBuffer.reuse();
            }
          }
          localSQLiteCursor.dispose();
          arrayOfBoolean = arrayOfBoolean;
          int i = j;
          if ((localObject1 instanceof TLRPC.TL_channelFull))
          {
            i = j;
            if (((TLRPC.ChatFull)localObject1).migrated_from_chat_id != 0) {
              i = 1;
            }
          }
          arrayOfBoolean[0] = i;
          if (localCountDownLatch != null) {
            localCountDownLatch.countDown();
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        finally
        {
          if (localCountDownLatch != null) {
            localCountDownLatch.countDown();
          }
        }
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfBoolean[0];
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public void loadChannelAdmins(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ArrayList localArrayList;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT uid FROM channel_admins WHERE did = " + paramInt, new Object[0]);
          localArrayList = new ArrayList();
          while (localSQLiteCursor.next()) {
            localArrayList.add(Integer.valueOf(localSQLiteCursor.intValue(0)));
          }
          localException.dispose();
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        MessagesController.getInstance(MessagesStorage.this.currentAccount).processLoadedChannelAdmins(localArrayList, paramInt, true);
      }
    });
  }
  
  public void loadChatInfo(final int paramInt, final CountDownLatch paramCountDownLatch, final boolean paramBoolean1, final boolean paramBoolean2)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject6 = null;
        localObject7 = null;
        NativeByteBuffer localNativeByteBuffer1 = null;
        Object localObject5 = null;
        localArrayList = new ArrayList();
        localObject4 = localObject7;
        localObject1 = localNativeByteBuffer1;
        label487:
        do
        {
          try
          {
            SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT info, pinned FROM chat_settings_v2 WHERE uid = " + paramInt, new Object[0]);
            localObject2 = localObject5;
            localObject4 = localObject7;
            localObject1 = localNativeByteBuffer1;
            if (localSQLiteCursor.next())
            {
              localObject4 = localObject7;
              localObject1 = localNativeByteBuffer1;
              NativeByteBuffer localNativeByteBuffer2 = localSQLiteCursor.byteBufferValue(0);
              localObject2 = localObject5;
              if (localNativeByteBuffer2 != null)
              {
                localObject4 = localObject7;
                localObject1 = localNativeByteBuffer1;
                localObject2 = TLRPC.ChatFull.TLdeserialize(localNativeByteBuffer2, localNativeByteBuffer2.readInt32(false), false);
                localObject4 = localObject2;
                localObject1 = localObject2;
                localNativeByteBuffer2.reuse();
                localObject4 = localObject2;
                localObject1 = localObject2;
                ((TLRPC.ChatFull)localObject2).pinned_msg_id = localSQLiteCursor.intValue(1);
              }
            }
            localObject4 = localObject2;
            localObject1 = localObject2;
            localSQLiteCursor.dispose();
            localObject4 = localObject2;
            localObject1 = localObject2;
            if (!(localObject2 instanceof TLRPC.TL_chatFull)) {
              break label487;
            }
            localObject4 = localObject2;
            localObject1 = localObject2;
            localObject5 = new StringBuilder();
            i = 0;
            for (;;)
            {
              localObject4 = localObject2;
              localObject1 = localObject2;
              if (i >= ((TLRPC.ChatFull)localObject2).participants.participants.size()) {
                break;
              }
              localObject4 = localObject2;
              localObject1 = localObject2;
              localObject7 = (TLRPC.ChatParticipant)((TLRPC.ChatFull)localObject2).participants.participants.get(i);
              localObject4 = localObject2;
              localObject1 = localObject2;
              if (((StringBuilder)localObject5).length() != 0)
              {
                localObject4 = localObject2;
                localObject1 = localObject2;
                ((StringBuilder)localObject5).append(",");
              }
              localObject4 = localObject2;
              localObject1 = localObject2;
              ((StringBuilder)localObject5).append(((TLRPC.ChatParticipant)localObject7).user_id);
              i += 1;
            }
            localObject4 = localObject2;
            localObject1 = localObject2;
            if (((StringBuilder)localObject5).length() != 0)
            {
              localObject4 = localObject2;
              localObject1 = localObject2;
              MessagesStorage.this.getUsersInternal(((StringBuilder)localObject5).toString(), localArrayList);
            }
          }
          catch (Exception localException1)
          {
            for (;;)
            {
              Object localObject2;
              localObject1 = localObject4;
              FileLog.e(localException1);
              return;
              localObject4 = localException1;
              localObject1 = localException1;
              ((SQLiteCursor)localObject7).dispose();
              localObject4 = localException1;
              localObject1 = localException1;
              StringBuilder localStringBuilder = new StringBuilder();
              int i = 0;
              for (;;)
              {
                localObject4 = localException1;
                localObject1 = localException1;
                if (i >= localException1.bot_info.size()) {
                  break;
                }
                localObject4 = localException1;
                localObject1 = localException1;
                localObject7 = (TLRPC.BotInfo)localException1.bot_info.get(i);
                localObject4 = localException1;
                localObject1 = localException1;
                if (localStringBuilder.length() != 0)
                {
                  localObject4 = localException1;
                  localObject1 = localException1;
                  localStringBuilder.append(",");
                }
                localObject4 = localException1;
                localObject1 = localException1;
                localStringBuilder.append(((TLRPC.BotInfo)localObject7).user_id);
                i += 1;
              }
              localObject4 = localException1;
              localObject1 = localException1;
              if (localStringBuilder.length() != 0)
              {
                localObject4 = localException1;
                localObject1 = localException1;
                MessagesStorage.this.getUsersInternal(localStringBuilder.toString(), localArrayList);
              }
            }
          }
          finally
          {
            MessagesController.getInstance(MessagesStorage.this.currentAccount).processChatInfo(paramInt, (TLRPC.ChatFull)localObject1, localArrayList, true, paramBoolean1, paramBoolean2, null);
            if (paramCountDownLatch == null) {
              break label1104;
            }
            paramCountDownLatch.countDown();
          }
          localObject4 = localObject2;
          localObject1 = localObject2;
          if (paramCountDownLatch != null)
          {
            localObject4 = localObject2;
            localObject1 = localObject2;
            paramCountDownLatch.countDown();
          }
          localObject5 = localObject6;
          localObject4 = localObject2;
          localObject1 = localObject2;
          if ((localObject2 instanceof TLRPC.TL_channelFull))
          {
            localObject5 = localObject6;
            localObject4 = localObject2;
            localObject1 = localObject2;
            if (((TLRPC.ChatFull)localObject2).pinned_msg_id != 0)
            {
              localObject4 = localObject2;
              localObject1 = localObject2;
              localObject5 = DataQuery.getInstance(MessagesStorage.this.currentAccount).loadPinnedMessage(paramInt, ((TLRPC.ChatFull)localObject2).pinned_msg_id, false);
            }
          }
          MessagesController.getInstance(MessagesStorage.this.currentAccount).processChatInfo(paramInt, (TLRPC.ChatFull)localObject2, localArrayList, true, paramBoolean1, paramBoolean2, (MessageObject)localObject5);
          if (paramCountDownLatch != null) {
            paramCountDownLatch.countDown();
          }
          return;
          localObject4 = localObject2;
          localObject1 = localObject2;
        } while (!(localObject2 instanceof TLRPC.TL_channelFull));
        localObject4 = localObject2;
        localObject1 = localObject2;
        localObject7 = MessagesStorage.this.database.queryFinalized("SELECT us.data, us.status, cu.data, cu.date FROM channel_users_v2 as cu LEFT JOIN users as us ON us.uid = cu.uid WHERE cu.did = " + -paramInt + " ORDER BY cu.date DESC", new Object[0]);
        localObject4 = localObject2;
        localObject1 = localObject2;
        ((TLRPC.ChatFull)localObject2).participants = new TLRPC.TL_chatParticipants();
        for (;;)
        {
          localObject4 = localObject2;
          localObject1 = localObject2;
          boolean bool = ((SQLiteCursor)localObject7).next();
          if (!bool) {
            break;
          }
          localObject4 = null;
          localObject5 = null;
          localObject1 = localObject2;
          try
          {
            localNativeByteBuffer1 = ((SQLiteCursor)localObject7).byteBufferValue(0);
            if (localNativeByteBuffer1 != null)
            {
              localObject1 = localObject2;
              localObject4 = TLRPC.User.TLdeserialize(localNativeByteBuffer1, localNativeByteBuffer1.readInt32(false), false);
              localObject1 = localObject2;
              localNativeByteBuffer1.reuse();
            }
            localObject1 = localObject2;
            localNativeByteBuffer1 = ((SQLiteCursor)localObject7).byteBufferValue(2);
            if (localNativeByteBuffer1 != null)
            {
              localObject1 = localObject2;
              localObject5 = TLRPC.ChannelParticipant.TLdeserialize(localNativeByteBuffer1, localNativeByteBuffer1.readInt32(false), false);
              localObject1 = localObject2;
              localNativeByteBuffer1.reuse();
            }
            if ((localObject4 != null) && (localObject5 != null))
            {
              localObject1 = localObject2;
              if (((TLRPC.User)localObject4).status != null)
              {
                localObject1 = localObject2;
                ((TLRPC.User)localObject4).status.expires = ((SQLiteCursor)localObject7).intValue(1);
              }
              localObject1 = localObject2;
              localArrayList.add(localObject4);
              localObject1 = localObject2;
              ((TLRPC.ChannelParticipant)localObject5).date = ((SQLiteCursor)localObject7).intValue(3);
              localObject1 = localObject2;
              localObject4 = new TLRPC.TL_chatChannelParticipant();
              localObject1 = localObject2;
              ((TLRPC.TL_chatChannelParticipant)localObject4).user_id = ((TLRPC.ChannelParticipant)localObject5).user_id;
              localObject1 = localObject2;
              ((TLRPC.TL_chatChannelParticipant)localObject4).date = ((TLRPC.ChannelParticipant)localObject5).date;
              localObject1 = localObject2;
              ((TLRPC.TL_chatChannelParticipant)localObject4).inviter_id = ((TLRPC.ChannelParticipant)localObject5).inviter_id;
              localObject1 = localObject2;
              ((TLRPC.TL_chatChannelParticipant)localObject4).channelParticipant = ((TLRPC.ChannelParticipant)localObject5);
              localObject1 = localObject2;
              ((TLRPC.ChatFull)localObject2).participants.participants.add(localObject4);
            }
          }
          catch (Exception localException2)
          {
            localObject4 = localObject2;
            localObject1 = localObject2;
            FileLog.e(localException2);
          }
        }
      }
    });
  }
  
  public void loadUnreadMessages()
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ArrayList localArrayList1;
        ArrayList localArrayList2;
        ArrayList localArrayList3;
        final LongSparseArray localLongSparseArray;
        Object localObject3;
        int j;
        long l1;
        int k;
        for (;;)
        {
          try
          {
            localArrayList1 = new ArrayList();
            localArrayList2 = new ArrayList();
            localArrayList3 = new ArrayList();
            localLongSparseArray = new LongSparseArray();
            localObject3 = MessagesStorage.this.database.queryFinalized("SELECT d.did, d.unread_count, s.flags FROM dialogs as d LEFT JOIN dialog_settings as s ON d.did = s.did WHERE d.unread_count != 0", new Object[0]);
            StringBuilder localStringBuilder = new StringBuilder();
            j = ConnectionsManager.getInstance(MessagesStorage.this.currentAccount).getCurrentTime();
            if (!((SQLiteCursor)localObject3).next()) {
              break;
            }
            l1 = ((SQLiteCursor)localObject3).longValue(2);
            if ((1L & l1) != 0L)
            {
              i = 1;
              k = (int)(l1 >> 32);
              if ((!((SQLiteCursor)localObject3).isNull(2)) && (i != 0) && ((k == 0) || (k >= j))) {
                continue;
              }
              l1 = ((SQLiteCursor)localObject3).longValue(0);
              localLongSparseArray.put(l1, Integer.valueOf(((SQLiteCursor)localObject3).intValue(1)));
              if (localStringBuilder.length() != 0) {
                localStringBuilder.append(",");
              }
              localStringBuilder.append(l1);
              i = (int)l1;
              k = (int)(l1 >> 32);
              if (i == 0) {
                break label265;
              }
              if (i >= 0) {
                break label240;
              }
              if (localArrayList2.contains(Integer.valueOf(-i))) {
                continue;
              }
              localArrayList2.add(Integer.valueOf(-i));
              continue;
            }
            i = 0;
          }
          catch (Exception localException1)
          {
            FileLog.e(localException1);
            return;
          }
          continue;
          label240:
          if (!localArrayList1.contains(Integer.valueOf(i)))
          {
            localArrayList1.add(Integer.valueOf(i));
            continue;
            label265:
            if (!localArrayList3.contains(Integer.valueOf(k))) {
              localArrayList3.add(Integer.valueOf(k));
            }
          }
        }
        ((SQLiteCursor)localObject3).dispose();
        Object localObject4 = new ArrayList();
        SparseArray localSparseArray = new SparseArray();
        final ArrayList localArrayList4 = new ArrayList();
        final ArrayList localArrayList5 = new ArrayList();
        final ArrayList localArrayList6 = new ArrayList();
        final ArrayList localArrayList7 = new ArrayList();
        Object localObject2;
        if (localException1.length() > 0)
        {
          Object localObject5 = MessagesStorage.this.database.queryFinalized("SELECT read_state, data, send_state, mid, date, uid, replydata FROM messages WHERE uid IN (" + localException1.toString() + ") AND out = 0 AND read_state IN(0,2) ORDER BY date DESC LIMIT 50", new Object[0]);
          while (((SQLiteCursor)localObject5).next())
          {
            Object localObject1 = ((SQLiteCursor)localObject5).byteBufferValue(1);
            if (localObject1 != null)
            {
              TLRPC.Message localMessage = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject1, ((NativeByteBuffer)localObject1).readInt32(false), false);
              localMessage.readAttachPath((AbstractSerializedData)localObject1, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
              ((NativeByteBuffer)localObject1).reuse();
              MessageObject.setUnreadFlags(localMessage, ((SQLiteCursor)localObject5).intValue(0));
              localMessage.id = ((SQLiteCursor)localObject5).intValue(3);
              localMessage.date = ((SQLiteCursor)localObject5).intValue(4);
              localMessage.dialog_id = ((SQLiteCursor)localObject5).longValue(5);
              localArrayList4.add(localMessage);
              i = (int)localMessage.dialog_id;
              MessagesStorage.addUsersAndChatsFromMessage(localMessage, localArrayList1, localArrayList2);
              localMessage.send_state = ((SQLiteCursor)localObject5).intValue(2);
              if (((localMessage.to_id.channel_id == 0) && (!MessageObject.isUnread(localMessage)) && (i != 0)) || (localMessage.id > 0)) {
                localMessage.send_state = 0;
              }
              if ((i == 0) && (!((SQLiteCursor)localObject5).isNull(5))) {
                localMessage.random_id = ((SQLiteCursor)localObject5).longValue(5);
              }
              try
              {
                if ((localMessage.reply_to_msg_id != 0) && (((localMessage.action instanceof TLRPC.TL_messageActionPinMessage)) || ((localMessage.action instanceof TLRPC.TL_messageActionPaymentSent)) || ((localMessage.action instanceof TLRPC.TL_messageActionGameScore))))
                {
                  if (!((SQLiteCursor)localObject5).isNull(6))
                  {
                    localObject1 = ((SQLiteCursor)localObject5).byteBufferValue(6);
                    if (localObject1 != null)
                    {
                      localMessage.replyMessage = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject1, ((NativeByteBuffer)localObject1).readInt32(false), false);
                      localMessage.replyMessage.readAttachPath((AbstractSerializedData)localObject1, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                      ((NativeByteBuffer)localObject1).reuse();
                      if (localMessage.replyMessage != null)
                      {
                        if (MessageObject.isMegagroup(localMessage))
                        {
                          localObject1 = localMessage.replyMessage;
                          ((TLRPC.Message)localObject1).flags |= 0x80000000;
                        }
                        MessagesStorage.addUsersAndChatsFromMessage(localMessage.replyMessage, localArrayList1, localArrayList2);
                      }
                    }
                  }
                  if (localMessage.replyMessage == null)
                  {
                    long l2 = localMessage.reply_to_msg_id;
                    l1 = l2;
                    if (localMessage.to_id.channel_id != 0) {
                      l1 = l2 | localMessage.to_id.channel_id << 32;
                    }
                    if (!((ArrayList)localObject4).contains(Long.valueOf(l1))) {
                      ((ArrayList)localObject4).add(Long.valueOf(l1));
                    }
                    localObject3 = (ArrayList)localSparseArray.get(localMessage.reply_to_msg_id);
                    localObject1 = localObject3;
                    if (localObject3 == null)
                    {
                      localObject1 = new ArrayList();
                      localSparseArray.put(localMessage.reply_to_msg_id, localObject1);
                    }
                    ((ArrayList)localObject1).add(localMessage);
                  }
                }
              }
              catch (Exception localException2)
              {
                FileLog.e(localException2);
              }
            }
          }
          ((SQLiteCursor)localObject5).dispose();
          if (!((ArrayList)localObject4).isEmpty())
          {
            localObject2 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data, mid, date, uid FROM messages WHERE mid IN(%s)", new Object[] { TextUtils.join(",", (Iterable)localObject4) }), new Object[0]);
            while (((SQLiteCursor)localObject2).next())
            {
              localObject4 = ((SQLiteCursor)localObject2).byteBufferValue(0);
              if (localObject4 != null)
              {
                localObject3 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject4, ((NativeByteBuffer)localObject4).readInt32(false), false);
                ((TLRPC.Message)localObject3).readAttachPath((AbstractSerializedData)localObject4, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                ((NativeByteBuffer)localObject4).reuse();
                ((TLRPC.Message)localObject3).id = ((SQLiteCursor)localObject2).intValue(1);
                ((TLRPC.Message)localObject3).date = ((SQLiteCursor)localObject2).intValue(2);
                ((TLRPC.Message)localObject3).dialog_id = ((SQLiteCursor)localObject2).longValue(3);
                MessagesStorage.addUsersAndChatsFromMessage((TLRPC.Message)localObject3, localArrayList1, localArrayList2);
                localObject4 = (ArrayList)localSparseArray.get(((TLRPC.Message)localObject3).id);
                if (localObject4 != null)
                {
                  i = 0;
                  label1072:
                  if (i >= ((ArrayList)localObject4).size()) {
                    break label1446;
                  }
                  localObject5 = (TLRPC.Message)((ArrayList)localObject4).get(i);
                  ((TLRPC.Message)localObject5).replyMessage = ((TLRPC.Message)localObject3);
                  if (!MessageObject.isMegagroup((TLRPC.Message)localObject5)) {
                    break label1441;
                  }
                  localObject5 = ((TLRPC.Message)localObject5).replyMessage;
                  ((TLRPC.Message)localObject5).flags |= 0x80000000;
                  break label1441;
                }
              }
            }
            ((SQLiteCursor)localObject2).dispose();
          }
          if (!localArrayList3.isEmpty()) {
            MessagesStorage.this.getEncryptedChatsInternal(TextUtils.join(",", localArrayList3), localArrayList7, localArrayList1);
          }
          if (!localArrayList1.isEmpty()) {
            MessagesStorage.this.getUsersInternal(TextUtils.join(",", localArrayList1), localArrayList5);
          }
          if (!localArrayList2.isEmpty()) {
            MessagesStorage.this.getChatsInternal(TextUtils.join(",", localArrayList2), localArrayList6);
          }
        }
        label1441:
        label1446:
        label1455:
        for (int i = 0;; i = j + 1) {
          if (i < localArrayList6.size())
          {
            localObject2 = (TLRPC.Chat)localArrayList6.get(i);
            j = i;
            if (localObject2 != null) {
              if (!((TLRPC.Chat)localObject2).left)
              {
                j = i;
                if (((TLRPC.Chat)localObject2).migrated_to == null) {}
              }
              else
              {
                l1 = -((TLRPC.Chat)localObject2).id;
                MessagesStorage.this.database.executeFast("UPDATE dialogs SET unread_count = 0 WHERE did = " + l1).stepThis().dispose();
                MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = 3 WHERE uid = %d AND mid > 0 AND read_state IN(0,2) AND out = 0", new Object[] { Long.valueOf(l1) })).stepThis().dispose();
                localArrayList6.remove(i);
                k = i - 1;
                localLongSparseArray.remove(-((TLRPC.Chat)localObject2).id);
              }
            }
          }
          else
          {
            for (i = 0;; i = j + 1)
            {
              j = k;
              if (i >= localArrayList4.size()) {
                break label1455;
              }
              j = i;
              if (((TLRPC.Message)localArrayList4.get(i)).dialog_id == -((TLRPC.Chat)localObject2).id)
              {
                localArrayList4.remove(i);
                j = i - 1;
                continue;
                Collections.reverse(localArrayList4);
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    NotificationsController.getInstance(MessagesStorage.this.currentAccount).processLoadedUnreadMessages(localLongSparseArray, localArrayList4, localArrayList5, localArrayList6, localArrayList7);
                  }
                });
                return;
                i += 1;
                break label1072;
                break;
              }
            }
          }
        }
      }
    });
  }
  
  public void loadWebRecent(final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final ArrayList localArrayList;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized("SELECT id, image_url, thumb_url, local_url, width, height, size, date, document FROM web_recent_v3 WHERE type = " + paramInt + " ORDER BY date DESC", new Object[0]);
          localArrayList = new ArrayList();
          while (localSQLiteCursor.next())
          {
            MediaController.SearchImage localSearchImage = new MediaController.SearchImage();
            localSearchImage.id = localSQLiteCursor.stringValue(0);
            localSearchImage.imageUrl = localSQLiteCursor.stringValue(1);
            localSearchImage.thumbUrl = localSQLiteCursor.stringValue(2);
            localSearchImage.localUrl = localSQLiteCursor.stringValue(3);
            localSearchImage.width = localSQLiteCursor.intValue(4);
            localSearchImage.height = localSQLiteCursor.intValue(5);
            localSearchImage.size = localSQLiteCursor.intValue(6);
            localSearchImage.date = localSQLiteCursor.intValue(7);
            if (!localSQLiteCursor.isNull(8))
            {
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(8);
              if (localNativeByteBuffer != null)
              {
                localSearchImage.document = TLRPC.Document.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                localNativeByteBuffer.reuse();
              }
            }
            localSearchImage.type = paramInt;
            localArrayList.add(localSearchImage);
          }
          localThrowable.dispose();
        }
        catch (Throwable localThrowable)
        {
          FileLog.e(localThrowable);
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.recentImagesDidLoaded, new Object[] { Integer.valueOf(MessagesStorage.14.this.val$type), localArrayList });
          }
        });
      }
    });
  }
  
  public void markMentionMessageAsRead(final int paramInt1, final int paramInt2, final long paramLong)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          long l2 = paramInt1;
          long l1 = l2;
          if (paramInt2 != 0) {
            l1 = l2 | paramInt2 << 32;
          }
          MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 2 WHERE mid = %d", new Object[] { Long.valueOf(l1) })).stepThis().dispose();
          Object localObject = MessagesStorage.this.database.queryFinalized("SELECT unread_count_i FROM dialogs WHERE did = " + paramLong, new Object[0]);
          int i = 0;
          if (((SQLiteCursor)localObject).next()) {
            i = Math.max(0, ((SQLiteCursor)localObject).intValue(0) - 1);
          }
          ((SQLiteCursor)localObject).dispose();
          MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE dialogs SET unread_count_i = %d WHERE did = %d", new Object[] { Integer.valueOf(i), Long.valueOf(paramLong) })).stepThis().dispose();
          localObject = new LongSparseArray(1);
          ((LongSparseArray)localObject).put(paramLong, Integer.valueOf(i));
          MessagesController.getInstance(MessagesStorage.this.currentAccount).processDialogsUpdateRead(null, (LongSparseArray)localObject);
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void markMessageAsMention(final long paramLong)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET mention = 1, read_state = read_state & ~2 WHERE mid = %d", new Object[] { Long.valueOf(paramLong) })).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void markMessageAsSendError(final TLRPC.Message paramMessage)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          long l2 = paramMessage.id;
          long l1 = l2;
          if (paramMessage.to_id.channel_id != 0) {
            l1 = l2 | paramMessage.to_id.channel_id << 32;
          }
          MessagesStorage.this.database.executeFast("UPDATE messages SET send_state = 2 WHERE mid = " + l1).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public ArrayList<Long> markMessagesAsDeleted(final int paramInt1, final int paramInt2, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.markMessagesAsDeletedInternal(paramInt1, paramInt2);
        }
      });
      return null;
    }
    return markMessagesAsDeletedInternal(paramInt1, paramInt2);
  }
  
  public ArrayList<Long> markMessagesAsDeleted(final ArrayList<Integer> paramArrayList, boolean paramBoolean, final int paramInt)
  {
    if (paramArrayList.isEmpty()) {
      return null;
    }
    if (paramBoolean)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.markMessagesAsDeletedInternal(paramArrayList, paramInt);
        }
      });
      return null;
    }
    return markMessagesAsDeletedInternal(paramArrayList, paramInt);
  }
  
  public void markMessagesAsDeletedByRandoms(final ArrayList<Long> paramArrayList)
  {
    if (paramArrayList.isEmpty()) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final ArrayList localArrayList;
        do
        {
          try
          {
            Object localObject = TextUtils.join(",", paramArrayList);
            localObject = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM randoms WHERE random_id IN(%s)", new Object[] { localObject }), new Object[0]);
            localArrayList = new ArrayList();
            while (((SQLiteCursor)localObject).next()) {
              localArrayList.add(Integer.valueOf(((SQLiteCursor)localObject).intValue(0)));
            }
            localException.dispose();
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
        } while (localArrayList.isEmpty());
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.messagesDeleted, new Object[] { localArrayList, Integer.valueOf(0) });
          }
        });
        MessagesStorage.this.updateDialogsWithReadMessagesInternal(localArrayList, null, null, null);
        MessagesStorage.this.markMessagesAsDeletedInternal(localArrayList, 0);
        MessagesStorage.this.updateDialogsWithDeletedMessagesInternal(localArrayList, null, 0);
      }
    });
  }
  
  public void markMessagesAsRead(final SparseLongArray paramSparseLongArray1, final SparseLongArray paramSparseLongArray2, final SparseIntArray paramSparseIntArray, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.markMessagesAsReadInternal(paramSparseLongArray1, paramSparseLongArray2, paramSparseIntArray);
        }
      });
      return;
    }
    markMessagesAsReadInternal(paramSparseLongArray1, paramSparseLongArray2, paramSparseIntArray);
  }
  
  public void markMessagesContentAsRead(final ArrayList<Long> paramArrayList, final int paramInt)
  {
    if (isEmpty(paramArrayList)) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        SQLiteCursor localSQLiteCursor;
        try
        {
          Object localObject1 = TextUtils.join(",", paramArrayList);
          MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 2 WHERE mid IN (%s)", new Object[] { localObject1 })).stepThis().dispose();
          if (paramInt != 0)
          {
            localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid, ttl FROM messages WHERE mid IN (%s) AND ttl > 0", new Object[] { localObject1 }), new Object[0]);
            Object localObject2;
            for (localObject1 = null; localSQLiteCursor.next(); localObject1 = localObject2)
            {
              localObject2 = localObject1;
              if (localObject1 == null) {
                localObject2 = new ArrayList();
              }
              ((ArrayList)localObject2).add(Integer.valueOf(localSQLiteCursor.intValue(0)));
            }
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
        if (localException != null) {
          MessagesStorage.this.emptyMessagesMedia(localException);
        }
        localSQLiteCursor.dispose();
      }
    });
  }
  
  public void openDatabase(boolean paramBoolean)
  {
    File localFile2 = ApplicationLoader.getFilesDirFixed();
    File localFile1 = localFile2;
    if (this.currentAccount != 0)
    {
      localFile1 = new File(localFile2, "account" + this.currentAccount + "/");
      localFile1.mkdirs();
    }
    this.cacheFile = new File(localFile1, "cache4.db");
    this.walCacheFile = new File(localFile1, "cache4.db-wal");
    this.shmCacheFile = new File(localFile1, "cache4.db-shm");
    int i = 0;
    if (!this.cacheFile.exists()) {
      i = 1;
    }
    try
    {
      this.database = new SQLiteDatabase(this.cacheFile.getPath());
      this.database.executeFast("PRAGMA secure_delete = ON").stepThis().dispose();
      this.database.executeFast("PRAGMA temp_store = 1").stepThis().dispose();
      this.database.executeFast("PRAGMA journal_mode = WAL").stepThis().dispose();
      if (i == 0) {
        break label1288;
      }
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("create new database");
      }
      this.database.executeFast("CREATE TABLE messages_holes(uid INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_messages_holes ON messages_holes(uid, end);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE media_holes_v2(uid INTEGER, type INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, type, start));").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_media_holes_v2 ON media_holes_v2(uid, type, end);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE messages(mid INTEGER PRIMARY KEY, uid INTEGER, read_state INTEGER, send_state INTEGER, date INTEGER, data BLOB, out INTEGER, ttl INTEGER, media INTEGER, replydata BLOB, imp INTEGER, mention INTEGER)").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_idx_messages ON messages(uid, mid);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_date_mid_idx_messages ON messages(uid, date, mid);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_out_idx_messages ON messages(mid, out);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS task_idx_messages ON messages(uid, out, read_state, ttl, date, send_state);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS send_state_idx_messages ON messages(mid, send_state, date) WHERE mid < 0 AND send_state = 1;").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mention_idx_messages ON messages(uid, mention, read_state);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE download_queue(uid INTEGER, type INTEGER, date INTEGER, data BLOB, PRIMARY KEY (uid, type));").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS type_date_idx_download_queue ON download_queue(type, date);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE user_contacts_v7(key TEXT PRIMARY KEY, uid INTEGER, fname TEXT, sname TEXT, imported INTEGER)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE user_phones_v7(key TEXT, phone TEXT, sphone TEXT, deleted INTEGER, PRIMARY KEY (key, phone))").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS sphone_deleted_idx_user_phones ON user_phones_v7(sphone, deleted);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE dialogs(did INTEGER PRIMARY KEY, date INTEGER, unread_count INTEGER, last_mid INTEGER, inbox_max INTEGER, outbox_max INTEGER, last_mid_i INTEGER, unread_count_i INTEGER, pts INTEGER, date_i INTEGER, pinned INTEGER)").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_dialogs ON dialogs(date);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_idx_dialogs ON dialogs(last_mid);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_idx_dialogs ON dialogs(unread_count);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_i_idx_dialogs ON dialogs(last_mid_i);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_i_idx_dialogs ON dialogs(unread_count_i);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE randoms(random_id INTEGER, mid INTEGER, PRIMARY KEY (random_id, mid))").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_idx_randoms ON randoms(mid);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE enc_tasks_v2(mid INTEGER PRIMARY KEY, date INTEGER)").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_enc_tasks_v2 ON enc_tasks_v2(date);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE messages_seq(mid INTEGER PRIMARY KEY, seq_in INTEGER, seq_out INTEGER);").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS seq_idx_messages_seq ON messages_seq(seq_in, seq_out);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE params(id INTEGER PRIMARY KEY, seq INTEGER, pts INTEGER, date INTEGER, qts INTEGER, lsv INTEGER, sg INTEGER, pbytes BLOB)").stepThis().dispose();
      this.database.executeFast("INSERT INTO params VALUES(1, 0, 0, 0, 0, 0, 0, NULL)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE media_v2(mid INTEGER PRIMARY KEY, uid INTEGER, date INTEGER, type INTEGER, data BLOB)").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_type_date_idx_media ON media_v2(uid, mid, type, date);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE bot_keyboard(uid INTEGER PRIMARY KEY, mid INTEGER, info BLOB)").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS bot_keyboard_idx_mid ON bot_keyboard(mid);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE chat_settings_v2(uid INTEGER PRIMARY KEY, info BLOB, pinned INTEGER)").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_settings_pinned_idx ON chat_settings_v2(uid, pinned) WHERE pinned != 0;").stepThis().dispose();
      this.database.executeFast("CREATE TABLE chat_pinned(uid INTEGER PRIMARY KEY, pinned INTEGER, data BLOB)").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_pinned_mid_idx ON chat_pinned(uid, pinned) WHERE pinned != 0;").stepThis().dispose();
      this.database.executeFast("CREATE TABLE chat_hints(did INTEGER, type INTEGER, rating REAL, date INTEGER, PRIMARY KEY(did, type))").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS chat_hints_rating_idx ON chat_hints(rating);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE botcache(id TEXT PRIMARY KEY, date INTEGER, data BLOB)").stepThis().dispose();
      this.database.executeFast("CREATE INDEX IF NOT EXISTS botcache_date_idx ON botcache(date);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE users_data(uid INTEGER PRIMARY KEY, about TEXT)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE users(uid INTEGER PRIMARY KEY, name TEXT, status INTEGER, data BLOB)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE chats(uid INTEGER PRIMARY KEY, name TEXT, data BLOB)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE enc_chats(uid INTEGER PRIMARY KEY, user INTEGER, name TEXT, data BLOB, g BLOB, authkey BLOB, ttl INTEGER, layer INTEGER, seq_in INTEGER, seq_out INTEGER, use_count INTEGER, exchange_id INTEGER, key_date INTEGER, fprint INTEGER, fauthkey BLOB, khash BLOB, in_seq_no INTEGER, admin_id INTEGER, mtproto_seq INTEGER)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE channel_users_v2(did INTEGER, uid INTEGER, date INTEGER, data BLOB, PRIMARY KEY(did, uid))").stepThis().dispose();
      this.database.executeFast("CREATE TABLE channel_admins(did INTEGER, uid INTEGER, PRIMARY KEY(did, uid))").stepThis().dispose();
      this.database.executeFast("CREATE TABLE contacts(uid INTEGER PRIMARY KEY, mutual INTEGER)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE wallpapers(uid INTEGER PRIMARY KEY, data BLOB)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE user_photos(uid INTEGER, id INTEGER, data BLOB, PRIMARY KEY (uid, id))").stepThis().dispose();
      this.database.executeFast("CREATE TABLE blocked_users(uid INTEGER PRIMARY KEY)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE dialog_settings(did INTEGER PRIMARY KEY, flags INTEGER);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE web_recent_v3(id TEXT, type INTEGER, image_url TEXT, thumb_url TEXT, local_url TEXT, width INTEGER, height INTEGER, size INTEGER, date INTEGER, document BLOB, PRIMARY KEY (id, type));").stepThis().dispose();
      this.database.executeFast("CREATE TABLE stickers_v2(id INTEGER PRIMARY KEY, data BLOB, date INTEGER, hash TEXT);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE stickers_featured(id INTEGER PRIMARY KEY, data BLOB, unread BLOB, date INTEGER, hash TEXT);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE hashtag_recent_v2(id TEXT PRIMARY KEY, date INTEGER);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE webpage_pending(id INTEGER, mid INTEGER, PRIMARY KEY (id, mid));").stepThis().dispose();
      this.database.executeFast("CREATE TABLE sent_files_v2(uid TEXT, type INTEGER, data BLOB, PRIMARY KEY (uid, type))").stepThis().dispose();
      this.database.executeFast("CREATE TABLE search_recent(did INTEGER PRIMARY KEY, date INTEGER);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE media_counts_v2(uid INTEGER, type INTEGER, count INTEGER, PRIMARY KEY(uid, type))").stepThis().dispose();
      this.database.executeFast("CREATE TABLE keyvalue(id TEXT PRIMARY KEY, value TEXT)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE bot_info(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
      this.database.executeFast("CREATE TABLE pending_tasks(id INTEGER PRIMARY KEY, data BLOB);").stepThis().dispose();
      this.database.executeFast("CREATE TABLE requested_holes(uid INTEGER, seq_out_start INTEGER, seq_out_end INTEGER, PRIMARY KEY (uid, seq_out_start, seq_out_end));").stepThis().dispose();
      this.database.executeFast("CREATE TABLE sharing_locations(uid INTEGER PRIMARY KEY, mid INTEGER, date INTEGER, period INTEGER, message BLOB);").stepThis().dispose();
      this.database.executeFast("PRAGMA user_version = 47").stepThis().dispose();
    }
    catch (Exception localException1)
    {
      for (;;)
      {
        FileLog.e(localException1);
        if ((paramBoolean) && (localException1.getMessage().contains("malformed")))
        {
          cleanupInternal();
          UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetId = 0;
          UserConfig.getInstance(this.currentAccount).totalDialogsLoadCount = 0;
          UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetDate = 0;
          UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetUserId = 0;
          UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetChatId = 0;
          UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetChannelId = 0;
          UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetAccess = 0L;
          UserConfig.getInstance(this.currentAccount).saveConfig(false);
          openDatabase(false);
        }
      }
      for (;;)
      {
        try
        {
          localSQLiteCursor = this.database.queryFinalized("SELECT seq, pts, date, qts, lsv, sg, pbytes FROM params WHERE id = 1", new Object[0]);
          if (localSQLiteCursor.next())
          {
            this.lastSeqValue = localSQLiteCursor.intValue(0);
            this.lastPtsValue = localSQLiteCursor.intValue(1);
            this.lastDateValue = localSQLiteCursor.intValue(2);
            this.lastQtsValue = localSQLiteCursor.intValue(3);
            this.lastSecretVersion = localSQLiteCursor.intValue(4);
            this.secretG = localSQLiteCursor.intValue(5);
            if (!localSQLiteCursor.isNull(6)) {
              continue;
            }
            this.secretPBytes = null;
          }
          localSQLiteCursor.dispose();
        }
        catch (Exception localException2)
        {
          SQLiteCursor localSQLiteCursor;
          FileLog.e(localException2);
          try
          {
            this.database.executeFast("CREATE TABLE IF NOT EXISTS params(id INTEGER PRIMARY KEY, seq INTEGER, pts INTEGER, date INTEGER, qts INTEGER, lsv INTEGER, sg INTEGER, pbytes BLOB)").stepThis().dispose();
            this.database.executeFast("INSERT INTO params VALUES(1, 0, 0, 0, 0, 0, 0, NULL)").stepThis().dispose();
          }
          catch (Exception localException3)
          {
            FileLog.e(localException3);
          }
          continue;
        }
        if (i >= 47) {
          break;
        }
        updateDbToLastVersion(i);
        break;
        this.secretPBytes = localSQLiteCursor.byteArrayValue(6);
        if ((this.secretPBytes != null) && (this.secretPBytes.length == 1)) {
          this.secretPBytes = null;
        }
      }
    }
    loadUnreadMessages();
    loadPendingTasks();
    label1288:
    try
    {
      this.openSync.countDown();
      return;
    }
    catch (Throwable localThrowable) {}
    i = this.database.executeInt("PRAGMA user_version", new Object[0]).intValue();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("current db version = " + i);
    }
    if (i == 0) {
      throw new Exception("malformed");
    }
  }
  
  public void overwriteChannel(final int paramInt1, final TLRPC.TL_updates_channelDifferenceTooLong paramTL_updates_channelDifferenceTooLong, final int paramInt2)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        boolean bool = false;
        int i = 0;
        try
        {
          final long l = -paramInt1;
          int k = 0;
          Object localObject = MessagesStorage.this.database.queryFinalized("SELECT pts, pinned FROM dialogs WHERE did = " + l, new Object[0]);
          if (!((SQLiteCursor)localObject).next())
          {
            j = k;
            if (paramInt2 != 0) {
              i = 1;
            }
          }
          for (int j = k;; j = ((SQLiteCursor)localObject).intValue(1))
          {
            ((SQLiteCursor)localObject).dispose();
            MessagesStorage.this.database.executeFast("DELETE FROM messages WHERE uid = " + l).stepThis().dispose();
            MessagesStorage.this.database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + l).stepThis().dispose();
            MessagesStorage.this.database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + l).stepThis().dispose();
            MessagesStorage.this.database.executeFast("DELETE FROM media_v2 WHERE uid = " + l).stepThis().dispose();
            MessagesStorage.this.database.executeFast("DELETE FROM messages_holes WHERE uid = " + l).stepThis().dispose();
            MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + l).stepThis().dispose();
            DataQuery.getInstance(MessagesStorage.this.currentAccount).clearBotKeyboard(l, null);
            localObject = new TLRPC.TL_messages_dialogs();
            ((TLRPC.TL_messages_dialogs)localObject).chats.addAll(paramTL_updates_channelDifferenceTooLong.chats);
            ((TLRPC.TL_messages_dialogs)localObject).users.addAll(paramTL_updates_channelDifferenceTooLong.users);
            ((TLRPC.TL_messages_dialogs)localObject).messages.addAll(paramTL_updates_channelDifferenceTooLong.messages);
            TLRPC.TL_dialog localTL_dialog = new TLRPC.TL_dialog();
            localTL_dialog.id = l;
            localTL_dialog.flags = 1;
            localTL_dialog.peer = new TLRPC.TL_peerChannel();
            localTL_dialog.peer.channel_id = paramInt1;
            localTL_dialog.top_message = paramTL_updates_channelDifferenceTooLong.top_message;
            localTL_dialog.read_inbox_max_id = paramTL_updates_channelDifferenceTooLong.read_inbox_max_id;
            localTL_dialog.read_outbox_max_id = paramTL_updates_channelDifferenceTooLong.read_outbox_max_id;
            localTL_dialog.unread_count = paramTL_updates_channelDifferenceTooLong.unread_count;
            localTL_dialog.unread_mentions_count = paramTL_updates_channelDifferenceTooLong.unread_mentions_count;
            localTL_dialog.notify_settings = null;
            if (j != 0) {
              bool = true;
            }
            localTL_dialog.pinned = bool;
            localTL_dialog.pinnedNum = j;
            localTL_dialog.pts = paramTL_updates_channelDifferenceTooLong.pts;
            ((TLRPC.TL_messages_dialogs)localObject).dialogs.add(localTL_dialog);
            MessagesStorage.this.putDialogsInternal((TLRPC.messages_Dialogs)localObject, false);
            MessagesStorage.this.updateDialogsWithDeletedMessages(new ArrayList(), null, false, paramInt1);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.removeAllMessagesFromDialog, new Object[] { Long.valueOf(l), Boolean.valueOf(true) });
              }
            });
            if (i == 0) {
              return;
            }
            if (paramInt2 != 1) {
              break;
            }
            MessagesController.getInstance(MessagesStorage.this.currentAccount).checkChannelInviter(paramInt1);
            return;
          }
          MessagesController.getInstance(MessagesStorage.this.currentAccount).generateJoinMessage(paramInt1, false);
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void processPendingRead(final long paramLong1, long paramLong2, long paramLong3, int paramInt, final boolean paramBoolean)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        long l1 = 0L;
        int i = 0;
        long l2 = 0L;
        try
        {
          Object localObject = MessagesStorage.this.database.queryFinalized("SELECT unread_count, inbox_max, last_mid FROM dialogs WHERE did = " + paramLong1, new Object[0]);
          if (((SQLiteCursor)localObject).next())
          {
            i = ((SQLiteCursor)localObject).intValue(0);
            l1 = ((SQLiteCursor)localObject).intValue(1);
            l2 = ((SQLiteCursor)localObject).longValue(2);
          }
          ((SQLiteCursor)localObject).dispose();
          MessagesStorage.this.database.beginTransaction();
          int j = (int)paramLong1;
          if (j != 0)
          {
            long l3 = Math.max(l1, (int)paramBoolean);
            l1 = l3;
            if (this.val$isChannel) {
              l1 = l3 | -j << 32;
            }
            localObject = MessagesStorage.this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND mid <= ? AND read_state IN(0,2) AND out = 0");
            ((SQLitePreparedStatement)localObject).requery();
            ((SQLitePreparedStatement)localObject).bindLong(1, paramLong1);
            ((SQLitePreparedStatement)localObject).bindLong(2, l1);
            ((SQLitePreparedStatement)localObject).step();
            ((SQLitePreparedStatement)localObject).dispose();
            if (l1 >= l2) {
              i = 0;
            }
          }
          for (;;)
          {
            localObject = MessagesStorage.this.database.executeFast("UPDATE dialogs SET unread_count = ?, inbox_max = ? WHERE did = ?");
            ((SQLitePreparedStatement)localObject).requery();
            ((SQLitePreparedStatement)localObject).bindInteger(1, i);
            ((SQLitePreparedStatement)localObject).bindInteger(2, (int)l1);
            ((SQLitePreparedStatement)localObject).bindLong(3, paramLong1);
            ((SQLitePreparedStatement)localObject).step();
            ((SQLitePreparedStatement)localObject).dispose();
            MessagesStorage.this.database.commitTransaction();
            return;
            j = 0;
            localObject = MessagesStorage.this.database.queryFinalized("SELECT changes()", new Object[0]);
            if (((SQLiteCursor)localObject).next()) {
              j = ((SQLiteCursor)localObject).intValue(0);
            }
            ((SQLiteCursor)localObject).dispose();
            i = Math.max(0, i - j);
            continue;
            l1 = (int)this.val$maxNegativeId;
            localObject = MessagesStorage.this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND mid >= ? AND read_state IN(0,2) AND out = 0");
            ((SQLitePreparedStatement)localObject).requery();
            ((SQLitePreparedStatement)localObject).bindLong(1, paramLong1);
            ((SQLitePreparedStatement)localObject).bindLong(2, l1);
            ((SQLitePreparedStatement)localObject).step();
            ((SQLitePreparedStatement)localObject).dispose();
            if (l1 <= l2)
            {
              i = 0;
            }
            else
            {
              j = 0;
              localObject = MessagesStorage.this.database.queryFinalized("SELECT changes()", new Object[0]);
              if (((SQLiteCursor)localObject).next()) {
                j = ((SQLiteCursor)localObject).intValue(0);
              }
              ((SQLiteCursor)localObject).dispose();
              i = Math.max(0, i - j);
            }
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void putBlockedUsers(final ArrayList<Integer> paramArrayList, final boolean paramBoolean)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (paramBoolean) {
            MessagesStorage.this.database.executeFast("DELETE FROM blocked_users WHERE 1").stepThis().dispose();
          }
          MessagesStorage.this.database.beginTransaction();
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO blocked_users VALUES(?)");
          Iterator localIterator = paramArrayList.iterator();
          while (localIterator.hasNext())
          {
            Integer localInteger = (Integer)localIterator.next();
            localSQLitePreparedStatement.requery();
            localSQLitePreparedStatement.bindInteger(1, localInteger.intValue());
            localSQLitePreparedStatement.step();
          }
          localException.dispose();
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        MessagesStorage.this.database.commitTransaction();
      }
    });
  }
  
  public void putCachedPhoneBook(final HashMap<String, ContactsController.Contact> paramHashMap, final boolean paramBoolean)
  {
    if ((paramHashMap == null) || ((paramHashMap.isEmpty()) && (!paramBoolean))) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d(MessagesStorage.this.currentAccount + " save contacts to db " + paramHashMap.size());
          }
          MessagesStorage.this.database.executeFast("DELETE FROM user_contacts_v7 WHERE 1").stepThis().dispose();
          MessagesStorage.this.database.executeFast("DELETE FROM user_phones_v7 WHERE 1").stepThis().dispose();
          MessagesStorage.this.database.beginTransaction();
          SQLitePreparedStatement localSQLitePreparedStatement1 = MessagesStorage.this.database.executeFast("REPLACE INTO user_contacts_v7 VALUES(?, ?, ?, ?, ?)");
          SQLitePreparedStatement localSQLitePreparedStatement2 = MessagesStorage.this.database.executeFast("REPLACE INTO user_phones_v7 VALUES(?, ?, ?, ?)");
          Iterator localIterator = paramHashMap.entrySet().iterator();
          while (localIterator.hasNext())
          {
            ContactsController.Contact localContact = (ContactsController.Contact)((Map.Entry)localIterator.next()).getValue();
            if ((!localContact.phones.isEmpty()) && (!localContact.shortPhones.isEmpty()))
            {
              localSQLitePreparedStatement1.requery();
              localSQLitePreparedStatement1.bindString(1, localContact.key);
              localSQLitePreparedStatement1.bindInteger(2, localContact.contact_id);
              localSQLitePreparedStatement1.bindString(3, localContact.first_name);
              localSQLitePreparedStatement1.bindString(4, localContact.last_name);
              localSQLitePreparedStatement1.bindInteger(5, localContact.imported);
              localSQLitePreparedStatement1.step();
              int i = 0;
              while (i < localContact.phones.size())
              {
                localSQLitePreparedStatement2.requery();
                localSQLitePreparedStatement2.bindString(1, localContact.key);
                localSQLitePreparedStatement2.bindString(2, (String)localContact.phones.get(i));
                localSQLitePreparedStatement2.bindString(3, (String)localContact.shortPhones.get(i));
                localSQLitePreparedStatement2.bindInteger(4, ((Integer)localContact.phoneDeleted.get(i)).intValue());
                localSQLitePreparedStatement2.step();
                i += 1;
              }
            }
          }
          localSQLitePreparedStatement1.dispose();
          localSQLitePreparedStatement2.dispose();
          MessagesStorage.this.database.commitTransaction();
          if (paramBoolean)
          {
            MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS user_contacts_v6;").stepThis().dispose();
            MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS user_phones_v6;").stepThis().dispose();
            MessagesStorage.this.getCachedPhoneBook(false);
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void putChannelAdmins(final int paramInt, final ArrayList<Integer> paramArrayList)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast("DELETE FROM channel_admins WHERE did = " + paramInt).stepThis().dispose();
          MessagesStorage.this.database.beginTransaction();
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO channel_admins VALUES(?, ?)");
          int i = (int)(System.currentTimeMillis() / 1000L);
          i = 0;
          while (i < paramArrayList.size())
          {
            localSQLitePreparedStatement.requery();
            localSQLitePreparedStatement.bindInteger(1, paramInt);
            localSQLitePreparedStatement.bindInteger(2, ((Integer)paramArrayList.get(i)).intValue());
            localSQLitePreparedStatement.step();
            i += 1;
          }
          localSQLitePreparedStatement.dispose();
          MessagesStorage.this.database.commitTransaction();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void putChannelViews(final SparseArray<SparseIntArray> paramSparseArray, final boolean paramBoolean)
  {
    if (isEmpty(paramSparseArray)) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          int i;
          try
          {
            MessagesStorage.this.database.beginTransaction();
            SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE messages SET media = max((SELECT media FROM messages WHERE mid = ?), ?) WHERE mid = ?");
            i = 0;
            if (i < paramSparseArray.size())
            {
              int k = paramSparseArray.keyAt(i);
              SparseIntArray localSparseIntArray = (SparseIntArray)paramSparseArray.get(k);
              int j = 0;
              if (j < localSparseIntArray.size())
              {
                int m = localSparseIntArray.get(localSparseIntArray.keyAt(j));
                long l2 = localSparseIntArray.keyAt(j);
                long l1 = l2;
                if (paramBoolean) {
                  l1 = l2 | -k << 32;
                }
                localSQLitePreparedStatement.requery();
                localSQLitePreparedStatement.bindLong(1, l1);
                localSQLitePreparedStatement.bindInteger(2, m);
                localSQLitePreparedStatement.bindLong(3, l1);
                localSQLitePreparedStatement.step();
                j += 1;
                continue;
              }
            }
            else
            {
              localSQLitePreparedStatement.dispose();
              MessagesStorage.this.database.commitTransaction();
              return;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          i += 1;
        }
      }
    });
  }
  
  public void putContacts(final ArrayList<TLRPC.TL_contact> paramArrayList, final boolean paramBoolean)
  {
    if (paramArrayList.isEmpty()) {
      return;
    }
    paramArrayList = new ArrayList(paramArrayList);
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          try
          {
            if (paramBoolean) {
              MessagesStorage.this.database.executeFast("DELETE FROM contacts WHERE 1").stepThis().dispose();
            }
            MessagesStorage.this.database.beginTransaction();
            SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO contacts VALUES(?, ?)");
            int i = 0;
            if (i < paramArrayList.size())
            {
              TLRPC.TL_contact localTL_contact = (TLRPC.TL_contact)paramArrayList.get(i);
              localSQLitePreparedStatement.requery();
              localSQLitePreparedStatement.bindInteger(1, localTL_contact.user_id);
              if (localTL_contact.mutual)
              {
                j = 1;
                localSQLitePreparedStatement.bindInteger(2, j);
                localSQLitePreparedStatement.step();
                i += 1;
              }
            }
            else
            {
              localSQLitePreparedStatement.dispose();
              MessagesStorage.this.database.commitTransaction();
              return;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          int j = 0;
        }
      }
    });
  }
  
  public void putDialogPhotos(final int paramInt, final TLRPC.photos_Photos paramphotos_Photos)
  {
    if ((paramphotos_Photos == null) || (paramphotos_Photos.photos.isEmpty())) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO user_photos VALUES(?, ?, ?)");
          Iterator localIterator = paramphotos_Photos.photos.iterator();
          while (localIterator.hasNext())
          {
            TLRPC.Photo localPhoto = (TLRPC.Photo)localIterator.next();
            if (!(localPhoto instanceof TLRPC.TL_photoEmpty))
            {
              localSQLitePreparedStatement.requery();
              NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(localPhoto.getObjectSize());
              localPhoto.serializeToStream(localNativeByteBuffer);
              localSQLitePreparedStatement.bindInteger(1, paramInt);
              localSQLitePreparedStatement.bindLong(2, localPhoto.id);
              localSQLitePreparedStatement.bindByteBuffer(3, localNativeByteBuffer);
              localSQLitePreparedStatement.step();
              localNativeByteBuffer.reuse();
            }
          }
          localException.dispose();
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
      }
    });
  }
  
  public void putDialogs(final TLRPC.messages_Dialogs parammessages_Dialogs, final boolean paramBoolean)
  {
    if (parammessages_Dialogs.dialogs.isEmpty()) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesStorage.this.putDialogsInternal(parammessages_Dialogs, paramBoolean);
        try
        {
          MessagesStorage.this.loadUnreadMessages();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void putEncryptedChat(final TLRPC.EncryptedChat paramEncryptedChat, final TLRPC.User paramUser, final TLRPC.TL_dialog paramTL_dialog)
  {
    if (paramEncryptedChat == null) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int j = 1;
        try
        {
          if (((paramEncryptedChat.key_hash == null) || (paramEncryptedChat.key_hash.length < 16)) && (paramEncryptedChat.auth_key != null)) {
            paramEncryptedChat.key_hash = AndroidUtilities.calcAuthKeyHash(paramEncryptedChat.auth_key);
          }
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO enc_chats VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
          NativeByteBuffer localNativeByteBuffer1 = new NativeByteBuffer(paramEncryptedChat.getObjectSize());
          NativeByteBuffer localNativeByteBuffer2;
          label129:
          NativeByteBuffer localNativeByteBuffer3;
          if (paramEncryptedChat.a_or_b != null)
          {
            i = paramEncryptedChat.a_or_b.length;
            localNativeByteBuffer2 = new NativeByteBuffer(i);
            if (paramEncryptedChat.auth_key == null) {
              break label723;
            }
            i = paramEncryptedChat.auth_key.length;
            localNativeByteBuffer3 = new NativeByteBuffer(i);
            if (paramEncryptedChat.future_auth_key == null) {
              break label728;
            }
          }
          label723:
          label728:
          for (int i = paramEncryptedChat.future_auth_key.length;; i = 1)
          {
            NativeByteBuffer localNativeByteBuffer4 = new NativeByteBuffer(i);
            i = j;
            if (paramEncryptedChat.key_hash != null) {
              i = paramEncryptedChat.key_hash.length;
            }
            NativeByteBuffer localNativeByteBuffer5 = new NativeByteBuffer(i);
            paramEncryptedChat.serializeToStream(localNativeByteBuffer1);
            localSQLitePreparedStatement.bindInteger(1, paramEncryptedChat.id);
            localSQLitePreparedStatement.bindInteger(2, paramUser.id);
            localSQLitePreparedStatement.bindString(3, MessagesStorage.this.formatUserSearchName(paramUser));
            localSQLitePreparedStatement.bindByteBuffer(4, localNativeByteBuffer1);
            if (paramEncryptedChat.a_or_b != null) {
              localNativeByteBuffer2.writeBytes(paramEncryptedChat.a_or_b);
            }
            if (paramEncryptedChat.auth_key != null) {
              localNativeByteBuffer3.writeBytes(paramEncryptedChat.auth_key);
            }
            if (paramEncryptedChat.future_auth_key != null) {
              localNativeByteBuffer4.writeBytes(paramEncryptedChat.future_auth_key);
            }
            if (paramEncryptedChat.key_hash != null) {
              localNativeByteBuffer5.writeBytes(paramEncryptedChat.key_hash);
            }
            localSQLitePreparedStatement.bindByteBuffer(5, localNativeByteBuffer2);
            localSQLitePreparedStatement.bindByteBuffer(6, localNativeByteBuffer3);
            localSQLitePreparedStatement.bindInteger(7, paramEncryptedChat.ttl);
            localSQLitePreparedStatement.bindInteger(8, paramEncryptedChat.layer);
            localSQLitePreparedStatement.bindInteger(9, paramEncryptedChat.seq_in);
            localSQLitePreparedStatement.bindInteger(10, paramEncryptedChat.seq_out);
            localSQLitePreparedStatement.bindInteger(11, paramEncryptedChat.key_use_count_in << 16 | paramEncryptedChat.key_use_count_out);
            localSQLitePreparedStatement.bindLong(12, paramEncryptedChat.exchange_id);
            localSQLitePreparedStatement.bindInteger(13, paramEncryptedChat.key_create_date);
            localSQLitePreparedStatement.bindLong(14, paramEncryptedChat.future_key_fingerprint);
            localSQLitePreparedStatement.bindByteBuffer(15, localNativeByteBuffer4);
            localSQLitePreparedStatement.bindByteBuffer(16, localNativeByteBuffer5);
            localSQLitePreparedStatement.bindInteger(17, paramEncryptedChat.in_seq_no);
            localSQLitePreparedStatement.bindInteger(18, paramEncryptedChat.admin_id);
            localSQLitePreparedStatement.bindInteger(19, paramEncryptedChat.mtproto_seq);
            localSQLitePreparedStatement.step();
            localSQLitePreparedStatement.dispose();
            localNativeByteBuffer1.reuse();
            localNativeByteBuffer2.reuse();
            localNativeByteBuffer3.reuse();
            localNativeByteBuffer4.reuse();
            localNativeByteBuffer5.reuse();
            if (paramTL_dialog != null)
            {
              localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
              localSQLitePreparedStatement.bindLong(1, paramTL_dialog.id);
              localSQLitePreparedStatement.bindInteger(2, paramTL_dialog.last_message_date);
              localSQLitePreparedStatement.bindInteger(3, paramTL_dialog.unread_count);
              localSQLitePreparedStatement.bindInteger(4, paramTL_dialog.top_message);
              localSQLitePreparedStatement.bindInteger(5, paramTL_dialog.read_inbox_max_id);
              localSQLitePreparedStatement.bindInteger(6, paramTL_dialog.read_outbox_max_id);
              localSQLitePreparedStatement.bindInteger(7, 0);
              localSQLitePreparedStatement.bindInteger(8, paramTL_dialog.unread_mentions_count);
              localSQLitePreparedStatement.bindInteger(9, paramTL_dialog.pts);
              localSQLitePreparedStatement.bindInteger(10, 0);
              localSQLitePreparedStatement.bindInteger(11, paramTL_dialog.pinnedNum);
              localSQLitePreparedStatement.step();
              localSQLitePreparedStatement.dispose();
            }
            return;
            i = 1;
            break;
            i = 1;
            break label129;
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void putMessages(ArrayList<TLRPC.Message> paramArrayList, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt)
  {
    putMessages(paramArrayList, paramBoolean1, paramBoolean2, paramBoolean3, paramInt, false);
  }
  
  public void putMessages(final ArrayList<TLRPC.Message> paramArrayList, final boolean paramBoolean1, boolean paramBoolean2, final boolean paramBoolean3, final int paramInt, final boolean paramBoolean4)
  {
    if (paramArrayList.size() == 0) {
      return;
    }
    if (paramBoolean2)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.putMessagesInternal(paramArrayList, paramBoolean1, paramBoolean3, paramInt, paramBoolean4);
        }
      });
      return;
    }
    putMessagesInternal(paramArrayList, paramBoolean1, paramBoolean3, paramInt, paramBoolean4);
  }
  
  public void putMessages(final TLRPC.messages_Messages parammessages_Messages, final long paramLong, final int paramInt1, int paramInt2, final boolean paramBoolean)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int j = Integer.MAX_VALUE;
        SQLitePreparedStatement localSQLitePreparedStatement1;
        SQLitePreparedStatement localSQLitePreparedStatement2;
        Object localObject4;
        int k;
        int m;
        TLRPC.Message localMessage1;
        int n;
        long l1;
        Object localObject5;
        int i3;
        label419:
        int i2;
        for (;;)
        {
          try
          {
            if (parammessages_Messages.messages.isEmpty())
            {
              if (paramInt1 != 0) {
                break label1570;
              }
              MessagesStorage.this.doneHolesInTable("messages_holes", paramLong, paramBoolean);
              MessagesStorage.this.doneHolesInMedia(paramLong, paramBoolean, -1);
              return;
            }
            MessagesStorage.this.database.beginTransaction();
            if (paramInt1 == 0)
            {
              i = ((TLRPC.Message)parammessages_Messages.messages.get(parammessages_Messages.messages.size() - 1)).id;
              MessagesStorage.this.closeHolesInTable("messages_holes", paramLong, i, paramBoolean);
              MessagesStorage.this.closeHolesInMedia(paramLong, i, paramBoolean, -1);
              int i1 = parammessages_Messages.messages.size();
              localSQLitePreparedStatement1 = MessagesStorage.this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, ?)");
              localSQLitePreparedStatement2 = MessagesStorage.this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
              Object localObject1 = null;
              localObject4 = null;
              k = 0;
              m = 0;
              i = j;
              j = k;
              if (m >= i1) {
                break label1373;
              }
              localMessage1 = (TLRPC.Message)parammessages_Messages.messages.get(m);
              long l2 = localMessage1.id;
              n = j;
              if (j == 0) {
                n = localMessage1.to_id.channel_id;
              }
              l1 = l2;
              if (localMessage1.to_id.channel_id != 0) {
                l1 = l2 | n << 32;
              }
              k = i;
              if (paramInt1 != -2) {
                break;
              }
              localObject3 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid, data, ttl, mention, read_state FROM messages WHERE mid = %d", new Object[] { Long.valueOf(l1) }), new Object[0]);
              boolean bool = ((SQLiteCursor)localObject3).next();
              j = i;
              if (bool)
              {
                localObject5 = ((SQLiteCursor)localObject3).byteBufferValue(1);
                if (localObject5 != null)
                {
                  TLRPC.Message localMessage2 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject5, ((NativeByteBuffer)localObject5).readInt32(false), false);
                  localMessage2.readAttachPath((AbstractSerializedData)localObject5, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                  ((NativeByteBuffer)localObject5).reuse();
                  if (localMessage2 != null)
                  {
                    localMessage1.attachPath = localMessage2.attachPath;
                    localMessage1.ttl = ((SQLiteCursor)localObject3).intValue(2);
                  }
                }
                if (((SQLiteCursor)localObject3).intValue(3) == 0) {
                  break label1607;
                }
                i3 = 1;
                i2 = ((SQLiteCursor)localObject3).intValue(4);
                j = i;
                if (i3 != localMessage1.mentioned)
                {
                  k = i;
                  if (i != Integer.MAX_VALUE) {
                    break label1571;
                  }
                  localObject5 = MessagesStorage.this.database.queryFinalized("SELECT unread_count_i FROM dialogs WHERE did = " + paramLong, new Object[0]);
                  if (((SQLiteCursor)localObject5).next()) {
                    i = ((SQLiteCursor)localObject5).intValue(0);
                  }
                  ((SQLiteCursor)localObject5).dispose();
                  k = i;
                  break label1571;
                }
              }
              label510:
              ((SQLiteCursor)localObject3).dispose();
              k = j;
              if (bool) {
                break;
              }
              localObject5 = localObject4;
              i = j;
              break label1591;
            }
            if (paramInt1 == 1)
            {
              i = ((TLRPC.Message)parammessages_Messages.messages.get(0)).id;
              MessagesStorage.this.closeHolesInTable("messages_holes", paramLong, paramBoolean, i);
              MessagesStorage.this.closeHolesInMedia(paramLong, paramBoolean, i, -1);
              continue;
            }
            if (paramInt1 == 3) {
              break label627;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          if ((paramInt1 == 2) || (paramInt1 == 4))
          {
            label627:
            if ((paramBoolean == 0) && (paramInt1 != 4)) {}
            for (i = Integer.MAX_VALUE;; i = ((TLRPC.Message)parammessages_Messages.messages.get(0)).id)
            {
              k = ((TLRPC.Message)parammessages_Messages.messages.get(parammessages_Messages.messages.size() - 1)).id;
              MessagesStorage.this.closeHolesInTable("messages_holes", paramLong, k, i);
              MessagesStorage.this.closeHolesInMedia(paramLong, k, i, -1);
              break;
            }
            label727:
            j = k;
            if (localMessage1.media_unread) {
              j = k + 1;
            }
          }
        }
        if ((m == 0) && (this.val$createDialog))
        {
          j = 0;
          i = 0;
          localObject3 = MessagesStorage.this.database.queryFinalized("SELECT pinned, unread_count_i FROM dialogs WHERE did = " + paramLong, new Object[0]);
          if (((SQLiteCursor)localObject3).next())
          {
            j = ((SQLiteCursor)localObject3).intValue(0);
            i = ((SQLiteCursor)localObject3).intValue(1);
          }
          ((SQLiteCursor)localObject3).dispose();
          localObject3 = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
          ((SQLitePreparedStatement)localObject3).bindLong(1, paramLong);
          ((SQLitePreparedStatement)localObject3).bindInteger(2, localMessage1.date);
          ((SQLitePreparedStatement)localObject3).bindInteger(3, 0);
          ((SQLitePreparedStatement)localObject3).bindLong(4, l1);
          ((SQLitePreparedStatement)localObject3).bindInteger(5, localMessage1.id);
          ((SQLitePreparedStatement)localObject3).bindInteger(6, 0);
          ((SQLitePreparedStatement)localObject3).bindLong(7, l1);
          ((SQLitePreparedStatement)localObject3).bindInteger(8, i);
          ((SQLitePreparedStatement)localObject3).bindInteger(9, parammessages_Messages.pts);
          ((SQLitePreparedStatement)localObject3).bindInteger(10, localMessage1.date);
          ((SQLitePreparedStatement)localObject3).bindInteger(11, j);
          ((SQLitePreparedStatement)localObject3).step();
          ((SQLitePreparedStatement)localObject3).dispose();
        }
        MessagesStorage.this.fixUnsupportedMedia(localMessage1);
        localSQLitePreparedStatement1.requery();
        Object localObject3 = new NativeByteBuffer(localMessage1.getObjectSize());
        localMessage1.serializeToStream((AbstractSerializedData)localObject3);
        localSQLitePreparedStatement1.bindLong(1, l1);
        localSQLitePreparedStatement1.bindLong(2, paramLong);
        localSQLitePreparedStatement1.bindInteger(3, MessageObject.getUnreadFlags(localMessage1));
        localSQLitePreparedStatement1.bindInteger(4, localMessage1.send_state);
        localSQLitePreparedStatement1.bindInteger(5, localMessage1.date);
        localSQLitePreparedStatement1.bindByteBuffer(6, (NativeByteBuffer)localObject3);
        if (MessageObject.isOut(localMessage1))
        {
          i = 1;
          label1061:
          localSQLitePreparedStatement1.bindInteger(7, i);
          localSQLitePreparedStatement1.bindInteger(8, localMessage1.ttl);
          if ((localMessage1.flags & 0x400) != 0)
          {
            localSQLitePreparedStatement1.bindInteger(9, localMessage1.views);
            label1105:
            localSQLitePreparedStatement1.bindInteger(10, 0);
            if (!localMessage1.mentioned) {
              break label1631;
            }
          }
        }
        label1373:
        label1570:
        label1571:
        label1591:
        label1607:
        label1613:
        label1631:
        for (int i = 1;; i = 0)
        {
          localSQLitePreparedStatement1.bindInteger(11, i);
          localSQLitePreparedStatement1.step();
          if (DataQuery.canAddMessageToMedia(localMessage1))
          {
            localSQLitePreparedStatement2.requery();
            localSQLitePreparedStatement2.bindLong(1, l1);
            localSQLitePreparedStatement2.bindLong(2, paramLong);
            localSQLitePreparedStatement2.bindInteger(3, localMessage1.date);
            localSQLitePreparedStatement2.bindInteger(4, DataQuery.getMediaType(localMessage1));
            localSQLitePreparedStatement2.bindByteBuffer(5, (NativeByteBuffer)localObject3);
            localSQLitePreparedStatement2.step();
          }
          ((NativeByteBuffer)localObject3).reuse();
          localObject3 = localException;
          if ((localMessage1.media instanceof TLRPC.TL_messageMediaWebPage))
          {
            localObject3 = localException;
            if (localException == null) {
              localObject3 = MessagesStorage.this.database.executeFast("REPLACE INTO webpage_pending VALUES(?, ?)");
            }
            ((SQLitePreparedStatement)localObject3).requery();
            ((SQLitePreparedStatement)localObject3).bindLong(1, localMessage1.media.webpage.id);
            ((SQLitePreparedStatement)localObject3).bindLong(2, l1);
            ((SQLitePreparedStatement)localObject3).step();
          }
          localObject5 = localObject4;
          i = k;
          Object localObject2 = localObject3;
          if (paramInt1 == 0)
          {
            localObject5 = localObject4;
            i = k;
            localObject2 = localObject3;
            if (MessagesStorage.this.isValidKeyboardToSave(localMessage1))
            {
              if (localObject4 == null) {
                break label1613;
              }
              localObject5 = localObject4;
              i = k;
              localObject2 = localObject3;
              if (((TLRPC.Message)localObject4).id < localMessage1.id)
              {
                break label1613;
                localSQLitePreparedStatement1.bindInteger(9, MessagesStorage.this.getMessageMediaType(localMessage1));
                break label1105;
                localSQLitePreparedStatement1.dispose();
                localSQLitePreparedStatement2.dispose();
                if (localObject2 != null) {
                  ((SQLitePreparedStatement)localObject2).dispose();
                }
                if (localObject4 != null) {
                  DataQuery.getInstance(MessagesStorage.this.currentAccount).putBotKeyboard(paramLong, (TLRPC.Message)localObject4);
                }
                MessagesStorage.this.putUsersInternal(parammessages_Messages.users);
                MessagesStorage.this.putChatsInternal(parammessages_Messages.chats);
                if (i != Integer.MAX_VALUE)
                {
                  MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE dialogs SET unread_count_i = %d WHERE did = %d", new Object[] { Integer.valueOf(i), Long.valueOf(paramLong) })).stepThis().dispose();
                  localObject2 = new LongSparseArray(1);
                  ((LongSparseArray)localObject2).put(paramLong, Integer.valueOf(i));
                  MessagesController.getInstance(MessagesStorage.this.currentAccount).processDialogsUpdateRead(null, (LongSparseArray)localObject2);
                }
                MessagesStorage.this.database.commitTransaction();
                if (this.val$createDialog) {
                  MessagesStorage.this.updateDialogsWithDeletedMessages(new ArrayList(), null, false, j);
                }
                return;
                if (i3 == 0) {
                  break label727;
                }
                j = k;
                if (i2 > 1) {
                  break label510;
                }
                j = k - 1;
                break label510;
              }
            }
          }
          for (;;)
          {
            m += 1;
            localObject4 = localObject5;
            j = n;
            break;
            i3 = 0;
            break label419;
            localObject5 = localMessage1;
            i = k;
            localObject2 = localObject3;
          }
          i = 0;
          break label1061;
        }
      }
    });
  }
  
  public void putSentFile(final String paramString, final TLObject paramTLObject, final int paramInt)
  {
    if ((paramString == null) || (paramTLObject == null)) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        localSQLitePreparedStatement = null;
        localNativeByteBuffer = null;
        localObject5 = null;
        localObject2 = localObject5;
        localObject1 = localNativeByteBuffer;
        for (;;)
        {
          try
          {
            str = Utilities.MD5(paramString);
            if (str == null) {
              continue;
            }
            localObject4 = null;
            localObject2 = localObject5;
            localObject1 = localNativeByteBuffer;
            if (!(paramTLObject instanceof TLRPC.Photo)) {
              continue;
            }
            localObject2 = localObject5;
            localObject1 = localNativeByteBuffer;
            localObject4 = new TLRPC.TL_messageMediaPhoto();
            localObject2 = localObject5;
            localObject1 = localNativeByteBuffer;
            ((TLRPC.MessageMedia)localObject4).photo = ((TLRPC.Photo)paramTLObject);
            localObject2 = localObject5;
            localObject1 = localNativeByteBuffer;
            ((TLRPC.MessageMedia)localObject4).flags |= 0x1;
            if (localObject4 != null) {
              continue;
            }
            if (0 != 0) {
              throw new NullPointerException();
            }
          }
          catch (Exception localException)
          {
            String str;
            Object localObject4;
            localObject1 = localObject2;
            FileLog.e(localException);
            return;
            localObject2 = localObject5;
            localObject1 = localNativeByteBuffer;
            localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO sent_files_v2 VALUES(?, ?, ?)");
            localObject2 = localSQLitePreparedStatement;
            localObject1 = localSQLitePreparedStatement;
            localSQLitePreparedStatement.requery();
            localObject2 = localSQLitePreparedStatement;
            localObject1 = localSQLitePreparedStatement;
            localNativeByteBuffer = new NativeByteBuffer(localException.getObjectSize());
            localObject2 = localSQLitePreparedStatement;
            localObject1 = localSQLitePreparedStatement;
            localException.serializeToStream(localNativeByteBuffer);
            localObject2 = localSQLitePreparedStatement;
            localObject1 = localSQLitePreparedStatement;
            localSQLitePreparedStatement.bindString(1, str);
            localObject2 = localSQLitePreparedStatement;
            localObject1 = localSQLitePreparedStatement;
            localSQLitePreparedStatement.bindInteger(2, paramInt);
            localObject2 = localSQLitePreparedStatement;
            localObject1 = localSQLitePreparedStatement;
            localSQLitePreparedStatement.bindByteBuffer(3, localNativeByteBuffer);
            localObject2 = localSQLitePreparedStatement;
            localObject1 = localSQLitePreparedStatement;
            localSQLitePreparedStatement.step();
            localObject2 = localSQLitePreparedStatement;
            localObject1 = localSQLitePreparedStatement;
            localNativeByteBuffer.reuse();
            if (localSQLitePreparedStatement == null) {
              continue;
            }
            localSQLitePreparedStatement.dispose();
            return;
          }
          finally
          {
            if (localObject1 == null) {
              continue;
            }
            ((SQLitePreparedStatement)localObject1).dispose();
          }
          return;
          localObject2 = localObject5;
          localObject1 = localNativeByteBuffer;
          if ((paramTLObject instanceof TLRPC.Document))
          {
            localObject2 = localObject5;
            localObject1 = localNativeByteBuffer;
            localObject4 = new TLRPC.TL_messageMediaDocument();
            localObject2 = localObject5;
            localObject1 = localNativeByteBuffer;
            ((TLRPC.MessageMedia)localObject4).document = ((TLRPC.Document)paramTLObject);
            localObject2 = localObject5;
            localObject1 = localNativeByteBuffer;
            ((TLRPC.MessageMedia)localObject4).flags |= 0x1;
          }
        }
      }
    });
  }
  
  public void putUsersAndChats(final ArrayList<TLRPC.User> paramArrayList, final ArrayList<TLRPC.Chat> paramArrayList1, final boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramArrayList != null) && (paramArrayList.isEmpty()) && (paramArrayList1 != null) && (paramArrayList1.isEmpty())) {
      return;
    }
    if (paramBoolean2)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.putUsersAndChatsInternal(paramArrayList, paramArrayList1, paramBoolean1);
        }
      });
      return;
    }
    putUsersAndChatsInternal(paramArrayList, paramArrayList1, paramBoolean1);
  }
  
  public void putWallpapers(final ArrayList<TLRPC.WallPaper> paramArrayList)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i = 0;
        try
        {
          MessagesStorage.this.database.executeFast("DELETE FROM wallpapers WHERE 1").stepThis().dispose();
          MessagesStorage.this.database.beginTransaction();
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO wallpapers VALUES(?, ?)");
          Iterator localIterator = paramArrayList.iterator();
          while (localIterator.hasNext())
          {
            TLRPC.WallPaper localWallPaper = (TLRPC.WallPaper)localIterator.next();
            localSQLitePreparedStatement.requery();
            NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(localWallPaper.getObjectSize());
            localWallPaper.serializeToStream(localNativeByteBuffer);
            localSQLitePreparedStatement.bindInteger(1, i);
            localSQLitePreparedStatement.bindByteBuffer(2, localNativeByteBuffer);
            localSQLitePreparedStatement.step();
            i += 1;
            localNativeByteBuffer.reuse();
          }
          localException.dispose();
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        MessagesStorage.this.database.commitTransaction();
      }
    });
  }
  
  public void putWebPages(final LongSparseArray<TLRPC.WebPage> paramLongSparseArray)
  {
    if (isEmpty(paramLongSparseArray)) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          int i;
          Object localObject1;
          Object localObject2;
          try
          {
            ArrayList localArrayList = new ArrayList();
            i = 0;
            if (i >= paramLongSparseArray.size()) {
              break label283;
            }
            localObject1 = MessagesStorage.this.database.queryFinalized("SELECT mid FROM webpage_pending WHERE id = " + paramLongSparseArray.keyAt(i), new Object[0]);
            localObject2 = new ArrayList();
            if (((SQLiteCursor)localObject1).next())
            {
              ((ArrayList)localObject2).add(Long.valueOf(((SQLiteCursor)localObject1).longValue(0)));
              continue;
            }
            ((SQLiteCursor)localObject1).dispose();
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          if (!((ArrayList)localObject2).isEmpty())
          {
            localObject1 = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid, data FROM messages WHERE mid IN (%s)", new Object[] { TextUtils.join(",", (Iterable)localObject2) }), new Object[0]);
            TLRPC.Message localMessage;
            while (((SQLiteCursor)localObject1).next())
            {
              int j = ((SQLiteCursor)localObject1).intValue(0);
              localObject2 = ((SQLiteCursor)localObject1).byteBufferValue(1);
              if (localObject2 != null)
              {
                localMessage = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject2, ((NativeByteBuffer)localObject2).readInt32(false), false);
                localMessage.readAttachPath((AbstractSerializedData)localObject2, UserConfig.getInstance(MessagesStorage.this.currentAccount).clientUserId);
                ((NativeByteBuffer)localObject2).reuse();
                if ((localMessage.media instanceof TLRPC.TL_messageMediaWebPage))
                {
                  localMessage.id = j;
                  localMessage.media.webpage = ((TLRPC.WebPage)paramLongSparseArray.valueAt(i));
                  localException.add(localMessage);
                }
              }
            }
            ((SQLiteCursor)localObject1).dispose();
            break label508;
            label283:
            if (localException.isEmpty()) {
              continue;
            }
            MessagesStorage.this.database.beginTransaction();
            localObject1 = MessagesStorage.this.database.executeFast("UPDATE messages SET data = ? WHERE mid = ?");
            localObject2 = MessagesStorage.this.database.executeFast("UPDATE media_v2 SET data = ? WHERE mid = ?");
            i = 0;
            while (i < localException.size())
            {
              localMessage = (TLRPC.Message)localException.get(i);
              NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(localMessage.getObjectSize());
              localMessage.serializeToStream(localNativeByteBuffer);
              long l2 = localMessage.id;
              long l1 = l2;
              if (localMessage.to_id.channel_id != 0) {
                l1 = l2 | localMessage.to_id.channel_id << 32;
              }
              ((SQLitePreparedStatement)localObject1).requery();
              ((SQLitePreparedStatement)localObject1).bindByteBuffer(1, localNativeByteBuffer);
              ((SQLitePreparedStatement)localObject1).bindLong(2, l1);
              ((SQLitePreparedStatement)localObject1).step();
              ((SQLitePreparedStatement)localObject2).requery();
              ((SQLitePreparedStatement)localObject2).bindByteBuffer(1, localNativeByteBuffer);
              ((SQLitePreparedStatement)localObject2).bindLong(2, l1);
              ((SQLitePreparedStatement)localObject2).step();
              localNativeByteBuffer.reuse();
              i += 1;
            }
            ((SQLitePreparedStatement)localObject1).dispose();
            ((SQLitePreparedStatement)localObject2).dispose();
            MessagesStorage.this.database.commitTransaction();
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.didReceivedWebpages, new Object[] { localException });
              }
            });
            return;
          }
          label508:
          i += 1;
        }
      }
    });
  }
  
  public void putWebRecent(final ArrayList<MediaController.SearchImage> paramArrayList)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          int i;
          try
          {
            MessagesStorage.this.database.beginTransaction();
            SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO web_recent_v3 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            i = 0;
            if ((i >= paramArrayList.size()) || (i == 200))
            {
              localSQLitePreparedStatement.dispose();
              MessagesStorage.this.database.commitTransaction();
              if (paramArrayList.size() >= 200)
              {
                MessagesStorage.this.database.beginTransaction();
                i = 200;
                if (i >= paramArrayList.size()) {
                  break label369;
                }
                MessagesStorage.this.database.executeFast("DELETE FROM web_recent_v3 WHERE id = '" + ((MediaController.SearchImage)paramArrayList.get(i)).id + "'").stepThis().dispose();
                i += 1;
                continue;
              }
            }
            else
            {
              MediaController.SearchImage localSearchImage = (MediaController.SearchImage)paramArrayList.get(i);
              localSQLitePreparedStatement.requery();
              localSQLitePreparedStatement.bindString(1, localSearchImage.id);
              localSQLitePreparedStatement.bindInteger(2, localSearchImage.type);
              if (localSearchImage.imageUrl == null) {
                break label387;
              }
              Object localObject = localSearchImage.imageUrl;
              localSQLitePreparedStatement.bindString(3, (String)localObject);
              if (localSearchImage.thumbUrl == null) {
                break label393;
              }
              localObject = localSearchImage.thumbUrl;
              localSQLitePreparedStatement.bindString(4, (String)localObject);
              if (localSearchImage.localUrl == null) {
                break label399;
              }
              localObject = localSearchImage.localUrl;
              localSQLitePreparedStatement.bindString(5, (String)localObject);
              localSQLitePreparedStatement.bindInteger(6, localSearchImage.width);
              localSQLitePreparedStatement.bindInteger(7, localSearchImage.height);
              localSQLitePreparedStatement.bindInteger(8, localSearchImage.size);
              localSQLitePreparedStatement.bindInteger(9, localSearchImage.date);
              localObject = null;
              if (localSearchImage.document != null)
              {
                localObject = new NativeByteBuffer(localSearchImage.document.getObjectSize());
                localSearchImage.document.serializeToStream((AbstractSerializedData)localObject);
                localSQLitePreparedStatement.bindByteBuffer(10, (NativeByteBuffer)localObject);
                localSQLitePreparedStatement.step();
                if (localObject == null) {
                  break label380;
                }
                ((NativeByteBuffer)localObject).reuse();
                break label380;
              }
              localSQLitePreparedStatement.bindNull(10);
              continue;
            }
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
          }
          label369:
          MessagesStorage.this.database.commitTransaction();
          return;
          label380:
          i += 1;
          continue;
          label387:
          String str = "";
          continue;
          label393:
          str = "";
          continue;
          label399:
          str = "";
        }
      }
    });
  }
  
  public void removeFromDownloadQueue(final long paramLong, final int paramInt, final boolean paramBoolean)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (paramBoolean)
          {
            int i = -1;
            SQLiteCursor localSQLiteCursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(date) FROM download_queue WHERE type = %d", new Object[] { Integer.valueOf(paramInt) }), new Object[0]);
            if (localSQLiteCursor.next()) {
              i = localSQLiteCursor.intValue(0);
            }
            localSQLiteCursor.dispose();
            if (i != -1) {
              MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE download_queue SET date = %d WHERE uid = %d AND type = %d", new Object[] { Integer.valueOf(i - 1), Long.valueOf(paramLong), Integer.valueOf(paramInt) })).stepThis().dispose();
            }
          }
          else
          {
            MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM download_queue WHERE uid = %d AND type = %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt) })).stepThis().dispose();
            return;
          }
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void removePendingTask(final long paramLong)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast("DELETE FROM pending_tasks WHERE id = " + paramLong).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void resetDialogs(final TLRPC.messages_Dialogs parammessages_Dialogs, final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4, final int paramInt5, final LongSparseArray<TLRPC.TL_dialog> paramLongSparseArray, final LongSparseArray<MessageObject> paramLongSparseArray1, final TLRPC.Message paramMessage, final int paramInt6)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int j = 0;
        int k;
        ArrayList localArrayList1;
        ArrayList localArrayList2;
        int i;
        Object localObject3;
        int m;
        try
        {
          localObject2 = new ArrayList();
          k = parammessages_Dialogs.dialogs.size() - paramInt6;
          LongSparseArray localLongSparseArray = new LongSparseArray();
          localArrayList1 = new ArrayList();
          localArrayList2 = new ArrayList();
          i = paramInt6;
          while (i < parammessages_Dialogs.dialogs.size())
          {
            localArrayList2.add(Long.valueOf(((TLRPC.TL_dialog)parammessages_Dialogs.dialogs.get(i)).id));
            i += 1;
          }
          localObject3 = MessagesStorage.this.database.queryFinalized("SELECT did, pinned FROM dialogs WHERE 1", new Object[0]);
          i = j;
          while (((SQLiteCursor)localObject3).next())
          {
            long l = ((SQLiteCursor)localObject3).longValue(0);
            j = ((SQLiteCursor)localObject3).intValue(1);
            m = (int)l;
            if (m != 0)
            {
              ((ArrayList)localObject2).add(Integer.valueOf(m));
              if (j > 0)
              {
                i = Math.max(j, i);
                localLongSparseArray.put(l, Integer.valueOf(j));
                localArrayList1.add(Long.valueOf(l));
              }
            }
          }
          Collections.sort(localArrayList1, new Comparator()
          {
            public int compare(Long paramAnonymous2Long1, Long paramAnonymous2Long2)
            {
              paramAnonymous2Long1 = (Integer)localException.get(paramAnonymous2Long1.longValue());
              paramAnonymous2Long2 = (Integer)localException.get(paramAnonymous2Long2.longValue());
              if (paramAnonymous2Long1.intValue() < paramAnonymous2Long2.intValue()) {
                return 1;
              }
              if (paramAnonymous2Long1.intValue() > paramAnonymous2Long2.intValue()) {
                return -1;
              }
              return 0;
            }
          });
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        while (localArrayList1.size() < k) {
          localArrayList1.add(0, Long.valueOf(0L));
        }
        ((SQLiteCursor)localObject3).dispose();
        Object localObject2 = "(" + TextUtils.join(",", (Iterable)localObject2) + ")";
        MessagesStorage.this.database.beginTransaction();
        MessagesStorage.this.database.executeFast("DELETE FROM dialogs WHERE did IN " + (String)localObject2).stepThis().dispose();
        MessagesStorage.this.database.executeFast("DELETE FROM messages WHERE uid IN " + (String)localObject2).stepThis().dispose();
        MessagesStorage.this.database.executeFast("DELETE FROM bot_keyboard WHERE uid IN " + (String)localObject2).stepThis().dispose();
        MessagesStorage.this.database.executeFast("DELETE FROM media_counts_v2 WHERE uid IN " + (String)localObject2).stepThis().dispose();
        MessagesStorage.this.database.executeFast("DELETE FROM media_v2 WHERE uid IN " + (String)localObject2).stepThis().dispose();
        MessagesStorage.this.database.executeFast("DELETE FROM messages_holes WHERE uid IN " + (String)localObject2).stepThis().dispose();
        MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid IN " + (String)localObject2).stepThis().dispose();
        MessagesStorage.this.database.commitTransaction();
        j = 0;
        if (j < k)
        {
          localObject2 = (TLRPC.TL_dialog)parammessages_Dialogs.dialogs.get(paramInt6 + j);
          m = localArrayList1.indexOf(Long.valueOf(((TLRPC.TL_dialog)localObject2).id));
          int n = localArrayList2.indexOf(Long.valueOf(((TLRPC.TL_dialog)localObject2).id));
          if ((m != -1) && (n != -1))
          {
            if (m != n) {
              break label687;
            }
            localObject3 = (Integer)localException.get(((TLRPC.TL_dialog)localObject2).id);
            if (localObject3 != null) {
              ((TLRPC.TL_dialog)localObject2).pinnedNum = ((Integer)localObject3).intValue();
            }
          }
          while (((TLRPC.TL_dialog)localObject2).pinnedNum == 0)
          {
            ((TLRPC.TL_dialog)localObject2).pinnedNum = (k - j + i);
            break;
            label687:
            localObject3 = (Integer)localException.get(((Long)localArrayList1.get(n)).longValue());
            if (localObject3 != null) {
              ((TLRPC.TL_dialog)localObject2).pinnedNum = ((Integer)localObject3).intValue();
            }
          }
        }
        MessagesStorage.this.putDialogsInternal(parammessages_Dialogs, false);
        MessagesStorage.this.saveDiffParamsInternal(paramInt2, paramInt3, paramInt4, paramInt5);
        label922:
        Object localObject1;
        if ((paramMessage != null) && (paramMessage.id != UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetId))
        {
          UserConfig.getInstance(MessagesStorage.this.currentAccount).totalDialogsLoadCount = parammessages_Dialogs.dialogs.size();
          UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetId = paramMessage.id;
          UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetDate = paramMessage.date;
          if (paramMessage.to_id.channel_id != 0)
          {
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetChannelId = paramMessage.to_id.channel_id;
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetChatId = 0;
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetUserId = 0;
            i = 0;
            if (i < parammessages_Dialogs.chats.size())
            {
              localObject1 = (TLRPC.Chat)parammessages_Dialogs.chats.get(i);
              if (((TLRPC.Chat)localObject1).id != UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetChannelId) {
                break label1357;
              }
              UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetAccess = ((TLRPC.Chat)localObject1).access_hash;
            }
          }
        }
        label991:
        label1121:
        label1357:
        label1369:
        label1376:
        for (;;)
        {
          UserConfig.getInstance(MessagesStorage.this.currentAccount).saveConfig(false);
          MessagesController.getInstance(MessagesStorage.this.currentAccount).completeDialogsReset(parammessages_Dialogs, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramLongSparseArray, paramLongSparseArray1, paramMessage);
          return;
          if (paramMessage.to_id.chat_id != 0)
          {
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetChatId = paramMessage.to_id.chat_id;
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetChannelId = 0;
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetUserId = 0;
            i = 0;
            if (i >= parammessages_Dialogs.chats.size()) {
              break label1369;
            }
            localObject1 = (TLRPC.Chat)parammessages_Dialogs.chats.get(i);
            if (((TLRPC.Chat)localObject1).id == UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetChatId) {
              UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetAccess = ((TLRPC.Chat)localObject1).access_hash;
            }
          }
          else
          {
            if (paramMessage.to_id.user_id == 0) {
              continue;
            }
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetUserId = paramMessage.to_id.user_id;
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetChatId = 0;
            UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetChannelId = 0;
            i = 0;
          }
          for (;;)
          {
            if (i >= parammessages_Dialogs.users.size()) {
              break label1376;
            }
            localObject1 = (TLRPC.User)parammessages_Dialogs.users.get(i);
            if (((TLRPC.User)localObject1).id == UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetUserId)
            {
              UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetAccess = ((TLRPC.User)localObject1).access_hash;
              break label991;
              UserConfig.getInstance(MessagesStorage.this.currentAccount).dialogsLoadOffsetId = Integer.MAX_VALUE;
              break label991;
              j += 1;
              break;
              i += 1;
              break label922;
              i += 1;
              break label1121;
              break label991;
            }
            i += 1;
          }
        }
      }
    });
  }
  
  public void resetMentionsCount(final long paramLong, final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (paramInt == 0) {
            MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 2 WHERE uid = %d AND mention = 1 AND read_state IN(0, 1)", new Object[] { Long.valueOf(paramLong) })).stepThis().dispose();
          }
          MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE dialogs SET unread_count_i = %d WHERE did = %d", new Object[] { Integer.valueOf(paramInt), Long.valueOf(paramLong) })).stepThis().dispose();
          LongSparseArray localLongSparseArray = new LongSparseArray(1);
          localLongSparseArray.put(paramLong, Integer.valueOf(paramInt));
          MessagesController.getInstance(MessagesStorage.this.currentAccount).processDialogsUpdateRead(null, localLongSparseArray);
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void saveBotCache(final String paramString, final TLObject paramTLObject)
  {
    if ((paramTLObject == null) || (TextUtils.isEmpty(paramString))) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          int j = ConnectionsManager.getInstance(MessagesStorage.this.currentAccount).getCurrentTime();
          int i;
          if ((paramTLObject instanceof TLRPC.TL_messages_botCallbackAnswer)) {
            i = j + ((TLRPC.TL_messages_botCallbackAnswer)paramTLObject).cache_time;
          }
          for (;;)
          {
            SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO botcache VALUES(?, ?, ?)");
            NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(paramTLObject.getObjectSize());
            paramTLObject.serializeToStream(localNativeByteBuffer);
            localSQLitePreparedStatement.bindString(1, paramString);
            localSQLitePreparedStatement.bindInteger(2, i);
            localSQLitePreparedStatement.bindByteBuffer(3, localNativeByteBuffer);
            localSQLitePreparedStatement.step();
            localSQLitePreparedStatement.dispose();
            localNativeByteBuffer.reuse();
            return;
            i = j;
            if ((paramTLObject instanceof TLRPC.TL_messages_botResults))
            {
              i = ((TLRPC.TL_messages_botResults)paramTLObject).cache_time;
              i = j + i;
            }
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void saveChannelPts(final int paramInt1, final int paramInt2)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE dialogs SET pts = ? WHERE did = ?");
          localSQLitePreparedStatement.bindInteger(1, paramInt2);
          localSQLitePreparedStatement.bindInteger(2, -paramInt1);
          localSQLitePreparedStatement.step();
          localSQLitePreparedStatement.dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void saveDiffParams(final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesStorage.this.saveDiffParamsInternal(paramInt1, paramInt2, paramInt3, paramInt4);
      }
    });
  }
  
  public void saveSecretParams(final int paramInt1, final int paramInt2, final byte[] paramArrayOfByte)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i = 1;
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE params SET lsv = ?, sg = ?, pbytes = ? WHERE id = 1");
          localSQLitePreparedStatement.bindInteger(1, paramInt1);
          localSQLitePreparedStatement.bindInteger(2, paramInt2);
          if (paramArrayOfByte != null) {
            i = paramArrayOfByte.length;
          }
          NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(i);
          if (paramArrayOfByte != null) {
            localNativeByteBuffer.writeBytes(paramArrayOfByte);
          }
          localSQLitePreparedStatement.bindByteBuffer(3, localNativeByteBuffer);
          localSQLitePreparedStatement.step();
          localSQLitePreparedStatement.dispose();
          localNativeByteBuffer.reuse();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void setDialogFlags(final long paramLong1, long paramLong2)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.this.database.executeFast(String.format(Locale.US, "REPLACE INTO dialog_settings VALUES(%d, %d)", new Object[] { Long.valueOf(paramLong1), Long.valueOf(this.val$flags) })).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void setDialogPinned(final long paramLong, final int paramInt)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE dialogs SET pinned = ? WHERE did = ?");
          localSQLitePreparedStatement.bindInteger(1, paramInt);
          localSQLitePreparedStatement.bindLong(2, paramLong);
          localSQLitePreparedStatement.step();
          localSQLitePreparedStatement.dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void setLastDateValue(int paramInt)
  {
    ensureOpened();
    this.lastDateValue = paramInt;
  }
  
  public void setLastPtsValue(int paramInt)
  {
    ensureOpened();
    this.lastPtsValue = paramInt;
  }
  
  public void setLastQtsValue(int paramInt)
  {
    ensureOpened();
    this.lastQtsValue = paramInt;
  }
  
  public void setLastSecretVersion(int paramInt)
  {
    ensureOpened();
    this.lastSecretVersion = paramInt;
  }
  
  public void setLastSeqValue(int paramInt)
  {
    ensureOpened();
    this.lastSeqValue = paramInt;
  }
  
  public void setMessageSeq(final int paramInt1, final int paramInt2, final int paramInt3)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO messages_seq VALUES(?, ?, ?)");
          localSQLitePreparedStatement.requery();
          localSQLitePreparedStatement.bindInteger(1, paramInt1);
          localSQLitePreparedStatement.bindInteger(2, paramInt2);
          localSQLitePreparedStatement.bindInteger(3, paramInt3);
          localSQLitePreparedStatement.step();
          localSQLitePreparedStatement.dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void setSecretG(int paramInt)
  {
    ensureOpened();
    this.secretG = paramInt;
  }
  
  public void setSecretPBytes(byte[] paramArrayOfByte)
  {
    ensureOpened();
    this.secretPBytes = paramArrayOfByte;
  }
  
  public void unpinAllDialogsExceptNew(final ArrayList<Long> paramArrayList)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        do
        {
          try
          {
            ArrayList localArrayList = new ArrayList();
            localObject = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT did FROM dialogs WHERE pinned != 0 AND did NOT IN (%s)", new Object[] { TextUtils.join(",", paramArrayList) }), new Object[0]);
            while (((SQLiteCursor)localObject).next()) {
              if ((int)((SQLiteCursor)localObject).longValue(0) != 0) {
                localArrayList.add(Long.valueOf(((SQLiteCursor)localObject).longValue(0)));
              }
            }
            ((SQLiteCursor)localObject).dispose();
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
        } while (localException.isEmpty());
        Object localObject = MessagesStorage.this.database.executeFast("UPDATE dialogs SET pinned = ? WHERE did = ?");
        int i = 0;
        while (i < localException.size())
        {
          long l = ((Long)localException.get(i)).longValue();
          ((SQLitePreparedStatement)localObject).requery();
          ((SQLitePreparedStatement)localObject).bindInteger(1, 0);
          ((SQLitePreparedStatement)localObject).bindLong(2, l);
          ((SQLitePreparedStatement)localObject).step();
          i += 1;
        }
        ((SQLitePreparedStatement)localObject).dispose();
      }
    });
  }
  
  public void updateChannelPinnedMessage(final int paramInt1, final int paramInt2)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          Object localObject2 = MessagesStorage.this.database.queryFinalized("SELECT info, pinned FROM chat_settings_v2 WHERE uid = " + paramInt1, new Object[0]);
          SQLitePreparedStatement localSQLitePreparedStatement = null;
          new ArrayList();
          final Object localObject1 = localSQLitePreparedStatement;
          if (((SQLiteCursor)localObject2).next())
          {
            NativeByteBuffer localNativeByteBuffer = ((SQLiteCursor)localObject2).byteBufferValue(0);
            localObject1 = localSQLitePreparedStatement;
            if (localNativeByteBuffer != null)
            {
              localObject1 = TLRPC.ChatFull.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
              localNativeByteBuffer.reuse();
              ((TLRPC.ChatFull)localObject1).pinned_msg_id = ((SQLiteCursor)localObject2).intValue(1);
            }
          }
          ((SQLiteCursor)localObject2).dispose();
          if ((localObject1 instanceof TLRPC.TL_channelFull))
          {
            ((TLRPC.ChatFull)localObject1).pinned_msg_id = paramInt2;
            ((TLRPC.ChatFull)localObject1).flags |= 0x20;
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { localObject1, Integer.valueOf(0), Boolean.valueOf(false), null });
              }
            });
            localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?, ?)");
            localObject2 = new NativeByteBuffer(((TLRPC.ChatFull)localObject1).getObjectSize());
            ((TLRPC.ChatFull)localObject1).serializeToStream((AbstractSerializedData)localObject2);
            localSQLitePreparedStatement.bindInteger(1, paramInt1);
            localSQLitePreparedStatement.bindByteBuffer(2, (NativeByteBuffer)localObject2);
            localSQLitePreparedStatement.bindInteger(3, ((TLRPC.ChatFull)localObject1).pinned_msg_id);
            localSQLitePreparedStatement.step();
            localSQLitePreparedStatement.dispose();
            ((NativeByteBuffer)localObject2).reuse();
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void updateChannelUsers(final int paramInt, final ArrayList<TLRPC.ChannelParticipant> paramArrayList)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          long l = -paramInt;
          MessagesStorage.this.database.executeFast("DELETE FROM channel_users_v2 WHERE did = " + l).stepThis().dispose();
          MessagesStorage.this.database.beginTransaction();
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO channel_users_v2 VALUES(?, ?, ?, ?)");
          int j = (int)(System.currentTimeMillis() / 1000L);
          int i = 0;
          while (i < paramArrayList.size())
          {
            TLRPC.ChannelParticipant localChannelParticipant = (TLRPC.ChannelParticipant)paramArrayList.get(i);
            localSQLitePreparedStatement.requery();
            localSQLitePreparedStatement.bindLong(1, l);
            localSQLitePreparedStatement.bindInteger(2, localChannelParticipant.user_id);
            localSQLitePreparedStatement.bindInteger(3, j);
            NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(localChannelParticipant.getObjectSize());
            localChannelParticipant.serializeToStream(localNativeByteBuffer);
            localSQLitePreparedStatement.bindByteBuffer(4, localNativeByteBuffer);
            localNativeByteBuffer.reuse();
            localSQLitePreparedStatement.step();
            j -= 1;
            i += 1;
          }
          localSQLitePreparedStatement.dispose();
          MessagesStorage.this.database.commitTransaction();
          MessagesStorage.this.loadChatInfo(paramInt, null, false, true);
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void updateChatInfo(final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4, final int paramInt5)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          Object localObject3;
          Object localObject2;
          try
          {
            localObject3 = MessagesStorage.this.database.queryFinalized("SELECT info, pinned FROM chat_settings_v2 WHERE uid = " + paramInt1, new Object[0]);
            localObject2 = null;
            new ArrayList();
            final Object localObject1 = localObject2;
            if (((SQLiteCursor)localObject3).next())
            {
              NativeByteBuffer localNativeByteBuffer = ((SQLiteCursor)localObject3).byteBufferValue(0);
              localObject1 = localObject2;
              if (localNativeByteBuffer != null)
              {
                localObject1 = TLRPC.ChatFull.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                localNativeByteBuffer.reuse();
                ((TLRPC.ChatFull)localObject1).pinned_msg_id = ((SQLiteCursor)localObject3).intValue(1);
              }
            }
            ((SQLiteCursor)localObject3).dispose();
            if (!(localObject1 instanceof TLRPC.TL_chatFull)) {
              break label533;
            }
            if (paramInt3 == 1)
            {
              i = 0;
              if (i < ((TLRPC.ChatFull)localObject1).participants.participants.size())
              {
                if (((TLRPC.ChatParticipant)((TLRPC.ChatFull)localObject1).participants.participants.get(i)).user_id != paramInt2) {
                  break label534;
                }
                ((TLRPC.ChatFull)localObject1).participants.participants.remove(i);
              }
              ((TLRPC.ChatFull)localObject1).participants.version = paramInt5;
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { localObject1, Integer.valueOf(0), Boolean.valueOf(false), null });
                }
              });
              localObject2 = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?, ?)");
              localObject3 = new NativeByteBuffer(((TLRPC.ChatFull)localObject1).getObjectSize());
              ((TLRPC.ChatFull)localObject1).serializeToStream((AbstractSerializedData)localObject3);
              ((SQLitePreparedStatement)localObject2).bindInteger(1, paramInt1);
              ((SQLitePreparedStatement)localObject2).bindByteBuffer(2, (NativeByteBuffer)localObject3);
              ((SQLitePreparedStatement)localObject2).bindInteger(3, ((TLRPC.ChatFull)localObject1).pinned_msg_id);
              ((SQLitePreparedStatement)localObject2).step();
              ((SQLitePreparedStatement)localObject2).dispose();
              ((NativeByteBuffer)localObject3).reuse();
              return;
            }
            if (paramInt3 == 0)
            {
              localObject2 = ((TLRPC.ChatFull)localObject1).participants.participants.iterator();
              if (((Iterator)localObject2).hasNext())
              {
                if (((TLRPC.ChatParticipant)((Iterator)localObject2).next()).user_id != paramInt2) {
                  continue;
                }
                return;
              }
              localObject2 = new TLRPC.TL_chatParticipant();
              ((TLRPC.TL_chatParticipant)localObject2).user_id = paramInt2;
              ((TLRPC.TL_chatParticipant)localObject2).inviter_id = paramInt4;
              ((TLRPC.TL_chatParticipant)localObject2).date = ConnectionsManager.getInstance(MessagesStorage.this.currentAccount).getCurrentTime();
              ((TLRPC.ChatFull)localObject1).participants.participants.add(localObject2);
              continue;
            }
            if (paramInt3 != 2) {
              continue;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          int i = 0;
          while (i < localException.participants.participants.size())
          {
            localObject3 = (TLRPC.ChatParticipant)localException.participants.participants.get(i);
            if (((TLRPC.ChatParticipant)localObject3).user_id == paramInt2)
            {
              if (paramInt4 == 1)
              {
                localObject2 = new TLRPC.TL_chatParticipantAdmin();
                ((TLRPC.ChatParticipant)localObject2).user_id = ((TLRPC.ChatParticipant)localObject3).user_id;
                ((TLRPC.ChatParticipant)localObject2).date = ((TLRPC.ChatParticipant)localObject3).date;
              }
              for (((TLRPC.ChatParticipant)localObject2).inviter_id = ((TLRPC.ChatParticipant)localObject3).inviter_id;; ((TLRPC.ChatParticipant)localObject2).inviter_id = ((TLRPC.ChatParticipant)localObject3).inviter_id)
              {
                localException.participants.participants.set(i, localObject2);
                break;
                localObject2 = new TLRPC.TL_chatParticipant();
                ((TLRPC.ChatParticipant)localObject2).user_id = ((TLRPC.ChatParticipant)localObject3).user_id;
                ((TLRPC.ChatParticipant)localObject2).date = ((TLRPC.ChatParticipant)localObject3).date;
              }
            }
            i += 1;
          }
          label533:
          return;
          label534:
          i += 1;
        }
      }
    });
  }
  
  public void updateChatInfo(final TLRPC.ChatFull paramChatFull, final boolean paramBoolean)
  {
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          if (paramBoolean)
          {
            localObject1 = MessagesStorage.this.database.queryFinalized("SELECT uid FROM chat_settings_v2 WHERE uid = " + paramChatFull.id, new Object[0]);
            boolean bool = ((SQLiteCursor)localObject1).next();
            ((SQLiteCursor)localObject1).dispose();
            if (!bool) {
              return;
            }
          }
          Object localObject1 = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?, ?)");
          Object localObject2 = new NativeByteBuffer(paramChatFull.getObjectSize());
          paramChatFull.serializeToStream((AbstractSerializedData)localObject2);
          ((SQLitePreparedStatement)localObject1).bindInteger(1, paramChatFull.id);
          ((SQLitePreparedStatement)localObject1).bindByteBuffer(2, (NativeByteBuffer)localObject2);
          ((SQLitePreparedStatement)localObject1).bindInteger(3, paramChatFull.pinned_msg_id);
          ((SQLitePreparedStatement)localObject1).step();
          ((SQLitePreparedStatement)localObject1).dispose();
          ((NativeByteBuffer)localObject2).reuse();
          if ((paramChatFull instanceof TLRPC.TL_channelFull))
          {
            localObject1 = MessagesStorage.this.database.queryFinalized("SELECT date, pts, last_mid, inbox_max, outbox_max, pinned, unread_count_i FROM dialogs WHERE did = " + -paramChatFull.id, new Object[0]);
            if ((((SQLiteCursor)localObject1).next()) && (((SQLiteCursor)localObject1).intValue(3) < paramChatFull.read_inbox_max_id))
            {
              int i = ((SQLiteCursor)localObject1).intValue(0);
              int j = ((SQLiteCursor)localObject1).intValue(1);
              long l = ((SQLiteCursor)localObject1).longValue(2);
              int k = ((SQLiteCursor)localObject1).intValue(4);
              int m = ((SQLiteCursor)localObject1).intValue(5);
              int n = ((SQLiteCursor)localObject1).intValue(6);
              localObject2 = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
              ((SQLitePreparedStatement)localObject2).bindLong(1, -paramChatFull.id);
              ((SQLitePreparedStatement)localObject2).bindInteger(2, i);
              ((SQLitePreparedStatement)localObject2).bindInteger(3, paramChatFull.unread_count);
              ((SQLitePreparedStatement)localObject2).bindLong(4, l);
              ((SQLitePreparedStatement)localObject2).bindInteger(5, paramChatFull.read_inbox_max_id);
              ((SQLitePreparedStatement)localObject2).bindInteger(6, Math.max(k, paramChatFull.read_outbox_max_id));
              ((SQLitePreparedStatement)localObject2).bindLong(7, 0L);
              ((SQLitePreparedStatement)localObject2).bindInteger(8, n);
              ((SQLitePreparedStatement)localObject2).bindInteger(9, j);
              ((SQLitePreparedStatement)localObject2).bindInteger(10, 0);
              ((SQLitePreparedStatement)localObject2).bindInteger(11, m);
              ((SQLitePreparedStatement)localObject2).step();
              ((SQLitePreparedStatement)localObject2).dispose();
            }
            ((SQLiteCursor)localObject1).dispose();
            return;
          }
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void updateChatParticipants(final TLRPC.ChatParticipants paramChatParticipants)
  {
    if (paramChatParticipants == null) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          Object localObject2 = MessagesStorage.this.database.queryFinalized("SELECT info, pinned FROM chat_settings_v2 WHERE uid = " + paramChatParticipants.chat_id, new Object[0]);
          SQLitePreparedStatement localSQLitePreparedStatement = null;
          new ArrayList();
          final Object localObject1 = localSQLitePreparedStatement;
          if (((SQLiteCursor)localObject2).next())
          {
            NativeByteBuffer localNativeByteBuffer = ((SQLiteCursor)localObject2).byteBufferValue(0);
            localObject1 = localSQLitePreparedStatement;
            if (localNativeByteBuffer != null)
            {
              localObject1 = TLRPC.ChatFull.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
              localNativeByteBuffer.reuse();
              ((TLRPC.ChatFull)localObject1).pinned_msg_id = ((SQLiteCursor)localObject2).intValue(1);
            }
          }
          ((SQLiteCursor)localObject2).dispose();
          if ((localObject1 instanceof TLRPC.TL_chatFull))
          {
            ((TLRPC.ChatFull)localObject1).participants = paramChatParticipants;
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(MessagesStorage.this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { localObject1, Integer.valueOf(0), Boolean.valueOf(false), null });
              }
            });
            localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?, ?)");
            localObject2 = new NativeByteBuffer(((TLRPC.ChatFull)localObject1).getObjectSize());
            ((TLRPC.ChatFull)localObject1).serializeToStream((AbstractSerializedData)localObject2);
            localSQLitePreparedStatement.bindInteger(1, ((TLRPC.ChatFull)localObject1).id);
            localSQLitePreparedStatement.bindByteBuffer(2, (NativeByteBuffer)localObject2);
            localSQLitePreparedStatement.bindInteger(3, ((TLRPC.ChatFull)localObject1).pinned_msg_id);
            localSQLitePreparedStatement.step();
            localSQLitePreparedStatement.dispose();
            ((NativeByteBuffer)localObject2).reuse();
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void updateDialogsWithDeletedMessages(final ArrayList<Integer> paramArrayList, final ArrayList<Long> paramArrayList1, boolean paramBoolean, final int paramInt)
  {
    if ((paramArrayList.isEmpty()) && (paramInt == 0)) {
      return;
    }
    if (paramBoolean)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.updateDialogsWithDeletedMessagesInternal(paramArrayList, paramArrayList1, paramInt);
        }
      });
      return;
    }
    updateDialogsWithDeletedMessagesInternal(paramArrayList, paramArrayList1, paramInt);
  }
  
  public void updateDialogsWithReadMessages(final SparseLongArray paramSparseLongArray1, final SparseLongArray paramSparseLongArray2, final ArrayList<Long> paramArrayList, boolean paramBoolean)
  {
    if ((isEmpty(paramSparseLongArray1)) && (isEmpty(paramArrayList))) {
      return;
    }
    if (paramBoolean)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.updateDialogsWithReadMessagesInternal(null, paramSparseLongArray1, paramSparseLongArray2, paramArrayList);
        }
      });
      return;
    }
    updateDialogsWithReadMessagesInternal(null, paramSparseLongArray1, paramSparseLongArray2, paramArrayList);
  }
  
  public void updateEncryptedChat(final TLRPC.EncryptedChat paramEncryptedChat)
  {
    if (paramEncryptedChat == null) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int j = 1;
        NativeByteBuffer localNativeByteBuffer1 = null;
        SQLitePreparedStatement localSQLitePreparedStatement2 = null;
        localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
        localObject1 = localNativeByteBuffer1;
        for (;;)
        {
          try
          {
            if (paramEncryptedChat.key_hash != null)
            {
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localNativeByteBuffer1;
              if (paramEncryptedChat.key_hash.length >= 16) {}
            }
            else
            {
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localNativeByteBuffer1;
              if (paramEncryptedChat.auth_key != null)
              {
                localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
                localObject1 = localNativeByteBuffer1;
                paramEncryptedChat.key_hash = AndroidUtilities.calcAuthKeyHash(paramEncryptedChat.auth_key);
              }
            }
            localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
            localObject1 = localNativeByteBuffer1;
            localSQLitePreparedStatement2 = MessagesStorage.this.database.executeFast("UPDATE enc_chats SET data = ?, g = ?, authkey = ?, ttl = ?, layer = ?, seq_in = ?, seq_out = ?, use_count = ?, exchange_id = ?, key_date = ?, fprint = ?, fauthkey = ?, khash = ?, in_seq_no = ?, admin_id = ?, mtproto_seq = ? WHERE uid = ?");
            localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
            localObject1 = localSQLitePreparedStatement2;
            localNativeByteBuffer1 = new NativeByteBuffer(paramEncryptedChat.getObjectSize());
            localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
            localObject1 = localSQLitePreparedStatement2;
            if (paramEncryptedChat.a_or_b != null)
            {
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              i = paramEncryptedChat.a_or_b.length;
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              NativeByteBuffer localNativeByteBuffer2 = new NativeByteBuffer(i);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              if (paramEncryptedChat.auth_key == null) {
                continue;
              }
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              i = paramEncryptedChat.auth_key.length;
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              NativeByteBuffer localNativeByteBuffer3 = new NativeByteBuffer(i);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              if (paramEncryptedChat.future_auth_key == null) {
                continue;
              }
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              i = paramEncryptedChat.future_auth_key.length;
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              NativeByteBuffer localNativeByteBuffer4 = new NativeByteBuffer(i);
              i = j;
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              if (paramEncryptedChat.key_hash != null)
              {
                localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
                localObject1 = localSQLitePreparedStatement2;
                i = paramEncryptedChat.key_hash.length;
              }
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              NativeByteBuffer localNativeByteBuffer5 = new NativeByteBuffer(i);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              paramEncryptedChat.serializeToStream(localNativeByteBuffer1);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindByteBuffer(1, localNativeByteBuffer1);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              if (paramEncryptedChat.a_or_b != null)
              {
                localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
                localObject1 = localSQLitePreparedStatement2;
                localNativeByteBuffer2.writeBytes(paramEncryptedChat.a_or_b);
              }
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              if (paramEncryptedChat.auth_key != null)
              {
                localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
                localObject1 = localSQLitePreparedStatement2;
                localNativeByteBuffer3.writeBytes(paramEncryptedChat.auth_key);
              }
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              if (paramEncryptedChat.future_auth_key != null)
              {
                localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
                localObject1 = localSQLitePreparedStatement2;
                localNativeByteBuffer4.writeBytes(paramEncryptedChat.future_auth_key);
              }
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              if (paramEncryptedChat.key_hash != null)
              {
                localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
                localObject1 = localSQLitePreparedStatement2;
                localNativeByteBuffer5.writeBytes(paramEncryptedChat.key_hash);
              }
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindByteBuffer(2, localNativeByteBuffer2);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindByteBuffer(3, localNativeByteBuffer3);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(4, paramEncryptedChat.ttl);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(5, paramEncryptedChat.layer);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(6, paramEncryptedChat.seq_in);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(7, paramEncryptedChat.seq_out);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(8, paramEncryptedChat.key_use_count_in << 16 | paramEncryptedChat.key_use_count_out);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindLong(9, paramEncryptedChat.exchange_id);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(10, paramEncryptedChat.key_create_date);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindLong(11, paramEncryptedChat.future_key_fingerprint);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindByteBuffer(12, localNativeByteBuffer4);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindByteBuffer(13, localNativeByteBuffer5);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(14, paramEncryptedChat.in_seq_no);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(15, paramEncryptedChat.admin_id);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(16, paramEncryptedChat.mtproto_seq);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.bindInteger(17, paramEncryptedChat.id);
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localSQLitePreparedStatement2.step();
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localNativeByteBuffer1.reuse();
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localNativeByteBuffer2.reuse();
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localNativeByteBuffer3.reuse();
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localNativeByteBuffer4.reuse();
              localSQLitePreparedStatement1 = localSQLitePreparedStatement2;
              localObject1 = localSQLitePreparedStatement2;
              localNativeByteBuffer5.reuse();
              if (localSQLitePreparedStatement2 != null) {
                localSQLitePreparedStatement2.dispose();
              }
              return;
            }
          }
          catch (Exception localException)
          {
            int i;
            localObject1 = localSQLitePreparedStatement1;
            FileLog.e(localException);
            if (localSQLitePreparedStatement1 == null) {
              continue;
            }
            localSQLitePreparedStatement1.dispose();
            return;
          }
          finally
          {
            if (localObject1 == null) {
              continue;
            }
            ((SQLitePreparedStatement)localObject1).dispose();
          }
          i = 1;
          continue;
          i = 1;
          continue;
          i = 1;
        }
      }
    });
  }
  
  public void updateEncryptedChatLayer(final TLRPC.EncryptedChat paramEncryptedChat)
  {
    if (paramEncryptedChat == null) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject3 = null;
        Object localObject1 = null;
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE enc_chats SET layer = ? WHERE uid = ?");
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(1, paramEncryptedChat.layer);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(2, paramEncryptedChat.id);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.step();
          if (localSQLitePreparedStatement != null) {
            localSQLitePreparedStatement.dispose();
          }
          return;
        }
        catch (Exception localException)
        {
          localObject3 = localObject1;
          FileLog.e(localException);
          return;
        }
        finally
        {
          if (localObject3 != null) {
            ((SQLitePreparedStatement)localObject3).dispose();
          }
        }
      }
    });
  }
  
  public void updateEncryptedChatSeq(final TLRPC.EncryptedChat paramEncryptedChat, final boolean paramBoolean)
  {
    if (paramEncryptedChat == null) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject3 = null;
        Object localObject1 = null;
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE enc_chats SET seq_in = ?, seq_out = ?, use_count = ?, in_seq_no = ?, mtproto_seq = ? WHERE uid = ?");
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(1, paramEncryptedChat.seq_in);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(2, paramEncryptedChat.seq_out);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(3, paramEncryptedChat.key_use_count_in << 16 | paramEncryptedChat.key_use_count_out);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(4, paramEncryptedChat.in_seq_no);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(5, paramEncryptedChat.mtproto_seq);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(6, paramEncryptedChat.id);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.step();
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          if (paramBoolean)
          {
            localObject1 = localSQLitePreparedStatement;
            localObject3 = localSQLitePreparedStatement;
            long l = paramEncryptedChat.id;
            localObject1 = localSQLitePreparedStatement;
            localObject3 = localSQLitePreparedStatement;
            MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM messages WHERE mid IN (SELECT m.mid FROM messages as m LEFT JOIN messages_seq as s ON m.mid = s.mid WHERE m.uid = %d AND m.date = 0 AND m.mid < 0 AND s.seq_out <= %d)", new Object[] { Long.valueOf(l << 32), Integer.valueOf(paramEncryptedChat.in_seq_no) })).stepThis().dispose();
          }
          if (localSQLitePreparedStatement != null) {
            localSQLitePreparedStatement.dispose();
          }
          return;
        }
        catch (Exception localException)
        {
          localObject3 = localObject1;
          FileLog.e(localException);
          return;
        }
        finally
        {
          if (localObject3 != null) {
            ((SQLitePreparedStatement)localObject3).dispose();
          }
        }
      }
    });
  }
  
  public void updateEncryptedChatTTL(final TLRPC.EncryptedChat paramEncryptedChat)
  {
    if (paramEncryptedChat == null) {
      return;
    }
    this.storageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject3 = null;
        Object localObject1 = null;
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.this.database.executeFast("UPDATE enc_chats SET ttl = ? WHERE uid = ?");
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(1, paramEncryptedChat.ttl);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.bindInteger(2, paramEncryptedChat.id);
          localObject1 = localSQLitePreparedStatement;
          localObject3 = localSQLitePreparedStatement;
          localSQLitePreparedStatement.step();
          if (localSQLitePreparedStatement != null) {
            localSQLitePreparedStatement.dispose();
          }
          return;
        }
        catch (Exception localException)
        {
          localObject3 = localObject1;
          FileLog.e(localException);
          return;
        }
        finally
        {
          if (localObject3 != null) {
            ((SQLitePreparedStatement)localObject3).dispose();
          }
        }
      }
    });
  }
  
  public long[] updateMessageStateAndId(final long paramLong, Integer paramInteger, final int paramInt1, final int paramInt2, boolean paramBoolean, final int paramInt3)
  {
    if (paramBoolean)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.updateMessageStateAndIdInternal(paramLong, paramInt1, paramInt2, paramInt3, this.val$channelId);
        }
      });
      return null;
    }
    return updateMessageStateAndIdInternal(paramLong, paramInteger, paramInt1, paramInt2, paramInt3);
  }
  
  public void updateUsers(final ArrayList<TLRPC.User> paramArrayList, final boolean paramBoolean1, final boolean paramBoolean2, boolean paramBoolean3)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    if (paramBoolean3)
    {
      this.storageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          MessagesStorage.this.updateUsersInternal(paramArrayList, paramBoolean1, paramBoolean2);
        }
      });
      return;
    }
    updateUsersInternal(paramArrayList, paramBoolean1, paramBoolean2);
  }
  
  private class Hole
  {
    public int end;
    public int start;
    public int type;
    
    public Hole(int paramInt1, int paramInt2)
    {
      this.start = paramInt1;
      this.end = paramInt2;
    }
    
    public Hole(int paramInt1, int paramInt2, int paramInt3)
    {
      this.type = paramInt1;
      this.start = paramInt2;
      this.end = paramInt3;
    }
  }
  
  public static abstract interface IntCallback
  {
    public abstract void run(int paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/MessagesStorage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */