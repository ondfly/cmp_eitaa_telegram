package ir.eitaa.messenger.query;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import ir.eitaa.SQLite.SQLiteCursor;
import ir.eitaa.SQLite.SQLiteDatabase;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.ChatObject;
import ir.eitaa.messenger.DispatchQueue;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.MessagesStorage;
import ir.eitaa.messenger.NotificationCenter;
import ir.eitaa.messenger.UserConfig;
import ir.eitaa.messenger.Utilities;
import ir.eitaa.tgnet.AbstractSerializedData;
import ir.eitaa.tgnet.ConnectionsManager;
import ir.eitaa.tgnet.NativeByteBuffer;
import ir.eitaa.tgnet.RequestDelegate;
import ir.eitaa.tgnet.SerializedData;
import ir.eitaa.tgnet.TLObject;
import ir.eitaa.tgnet.TLRPC.Chat;
import ir.eitaa.tgnet.TLRPC.DraftMessage;
import ir.eitaa.tgnet.TLRPC.Message;
import ir.eitaa.tgnet.TLRPC.MessageEntity;
import ir.eitaa.tgnet.TLRPC.TL_channels_getMessages;
import ir.eitaa.tgnet.TLRPC.TL_draftMessage;
import ir.eitaa.tgnet.TLRPC.TL_draftMessageEmpty;
import ir.eitaa.tgnet.TLRPC.TL_error;
import ir.eitaa.tgnet.TLRPC.TL_messages_getAllDrafts;
import ir.eitaa.tgnet.TLRPC.TL_messages_getMessages;
import ir.eitaa.tgnet.TLRPC.TL_messages_saveDraft;
import ir.eitaa.tgnet.TLRPC.Updates;
import ir.eitaa.tgnet.TLRPC.messages_Messages;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DraftQuery
{
  private static HashMap<Long, TLRPC.Message> draftMessages;
  private static HashMap<Long, TLRPC.DraftMessage> drafts = new HashMap();
  private static boolean inTransaction;
  private static boolean loadingDrafts;
  private static SharedPreferences preferences;
  
  static
  {
    draftMessages = new HashMap();
    preferences = ApplicationLoader.applicationContext.getSharedPreferences("drafts", 0);
    Iterator localIterator = preferences.getAll().entrySet().iterator();
    for (;;)
    {
      Object localObject2;
      if (localIterator.hasNext()) {
        localObject2 = (Map.Entry)localIterator.next();
      }
      try
      {
        Object localObject1 = (String)((Map.Entry)localObject2).getKey();
        long l = Utilities.parseLong((String)localObject1).longValue();
        localObject2 = new SerializedData(Utilities.hexToBytes((String)((Map.Entry)localObject2).getValue()));
        if (((String)localObject1).startsWith("r_"))
        {
          localObject1 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject2, ((SerializedData)localObject2).readInt32(true), true);
          if (localObject1 == null) {
            continue;
          }
          draftMessages.put(Long.valueOf(l), localObject1);
          continue;
        }
        localObject1 = TLRPC.DraftMessage.TLdeserialize((AbstractSerializedData)localObject2, ((SerializedData)localObject2).readInt32(true), true);
        if (localObject1 == null) {
          continue;
        }
        drafts.put(Long.valueOf(l), localObject1);
      }
      catch (Exception localException) {}
      return;
    }
  }
  
  public static void beginTransaction()
  {
    inTransaction = true;
  }
  
  public static void cleanDraft(long paramLong, boolean paramBoolean)
  {
    TLRPC.DraftMessage localDraftMessage = (TLRPC.DraftMessage)drafts.get(Long.valueOf(paramLong));
    if (localDraftMessage == null) {}
    do
    {
      return;
      if (!paramBoolean)
      {
        drafts.remove(Long.valueOf(paramLong));
        draftMessages.remove(Long.valueOf(paramLong));
        preferences.edit().remove("" + paramLong).remove("r_" + paramLong).commit();
        MessagesController.getInstance().sortDialogs(null);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
        return;
      }
    } while (localDraftMessage.reply_to_msg_id == 0);
    localDraftMessage.reply_to_msg_id = 0;
    localDraftMessage.flags &= 0xFFFFFFFE;
    saveDraft(paramLong, localDraftMessage.message, localDraftMessage.entities, null, localDraftMessage.no_webpage, true);
  }
  
  public static void cleanup()
  {
    drafts.clear();
    draftMessages.clear();
    preferences.edit().clear().commit();
  }
  
  public static void endTransaction()
  {
    inTransaction = false;
  }
  
  public static TLRPC.DraftMessage getDraft(long paramLong)
  {
    return (TLRPC.DraftMessage)drafts.get(Long.valueOf(paramLong));
  }
  
  public static TLRPC.Message getDraftMessage(long paramLong)
  {
    return (TLRPC.Message)draftMessages.get(Long.valueOf(paramLong));
  }
  
  public static void loadDrafts()
  {
    if ((UserConfig.draftsLoaded) || (loadingDrafts)) {
      return;
    }
    loadingDrafts = true;
    TLRPC.TL_messages_getAllDrafts localTL_messages_getAllDrafts = new TLRPC.TL_messages_getAllDrafts();
    ConnectionsManager.getInstance().sendRequest(localTL_messages_getAllDrafts, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error != null) {
          return;
        }
        MessagesController.getInstance().processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            UserConfig.draftsLoaded = true;
            DraftQuery.access$002(false);
            UserConfig.saveConfig(false);
          }
        });
      }
    });
  }
  
  public static void saveDraft(final long paramLong, TLRPC.DraftMessage paramDraftMessage, TLRPC.Message paramMessage, boolean paramBoolean)
  {
    Object localObject = preferences.edit();
    label147:
    label198:
    long l;
    if ((paramDraftMessage == null) || ((paramDraftMessage instanceof TLRPC.TL_draftMessageEmpty)))
    {
      drafts.remove(Long.valueOf(paramLong));
      draftMessages.remove(Long.valueOf(paramLong));
      preferences.edit().remove("" + paramLong).remove("r_" + paramLong).commit();
      if (paramMessage != null) {
        break label364;
      }
      draftMessages.remove(Long.valueOf(paramLong));
      ((SharedPreferences.Editor)localObject).remove("r_" + paramLong);
      ((SharedPreferences.Editor)localObject).commit();
      if (paramBoolean) {
        if ((paramDraftMessage.reply_to_msg_id != 0) && (paramMessage == null))
        {
          i = (int)paramLong;
          localObject = null;
          paramMessage = null;
          if (i <= 0) {
            break label433;
          }
          localObject = MessagesController.getInstance().getUser(Integer.valueOf(i));
          if ((localObject != null) || (paramMessage != null))
          {
            l = paramDraftMessage.reply_to_msg_id;
            if (!ChatObject.isChannel(paramMessage)) {
              break label449;
            }
            l |= paramMessage.id << 32;
          }
        }
      }
    }
    label364:
    label433:
    label449:
    for (int i = paramMessage.id;; i = 0)
    {
      MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          Object localObject2 = null;
          try
          {
            SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance().getDatabase().queryFinalized(String.format(Locale.US, "SELECT data FROM messages WHERE mid = %d", new Object[] { Long.valueOf(this.val$messageIdFinal) }), new Object[0]);
            Object localObject1 = localObject2;
            if (localSQLiteCursor.next())
            {
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
              localObject1 = localObject2;
              if (localNativeByteBuffer != null)
              {
                localObject1 = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                localNativeByteBuffer.reuse();
              }
            }
            localSQLiteCursor.dispose();
            if (localObject1 == null)
            {
              if (paramLong != 0)
              {
                localObject1 = new TLRPC.TL_channels_getMessages();
                ((TLRPC.TL_channels_getMessages)localObject1).channel = MessagesController.getInputChannel(paramLong);
                ((TLRPC.TL_channels_getMessages)localObject1).id.add(Integer.valueOf((int)this.val$messageIdFinal));
                ConnectionsManager.getInstance().sendRequest((TLObject)localObject1, new RequestDelegate()
                {
                  public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
                  {
                    if (paramAnonymous2TL_error == null)
                    {
                      paramAnonymous2TLObject = (TLRPC.messages_Messages)paramAnonymous2TLObject;
                      if (!paramAnonymous2TLObject.messages.isEmpty()) {
                        DraftQuery.saveDraftReplyMessage(DraftQuery.3.this.val$did, (TLRPC.Message)paramAnonymous2TLObject.messages.get(0));
                      }
                    }
                  }
                });
                return;
              }
              localObject1 = new TLRPC.TL_messages_getMessages();
              ((TLRPC.TL_messages_getMessages)localObject1).id.add(Integer.valueOf((int)this.val$messageIdFinal));
              ConnectionsManager.getInstance().sendRequest((TLObject)localObject1, new RequestDelegate()
              {
                public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
                {
                  if (paramAnonymous2TL_error == null)
                  {
                    paramAnonymous2TLObject = (TLRPC.messages_Messages)paramAnonymous2TLObject;
                    if (!paramAnonymous2TLObject.messages.isEmpty()) {
                      DraftQuery.saveDraftReplyMessage(DraftQuery.3.this.val$did, (TLRPC.Message)paramAnonymous2TLObject.messages.get(0));
                    }
                  }
                }
              });
              return;
            }
          }
          catch (Exception localException)
          {
            FileLog.e("TSMS", localException);
            return;
          }
          DraftQuery.saveDraftReplyMessage(this.val$did, localException);
        }
      });
      NotificationCenter.getInstance().postNotificationName(NotificationCenter.newDraftReceived, new Object[] { Long.valueOf(paramLong) });
      return;
      drafts.put(Long.valueOf(paramLong), paramDraftMessage);
      try
      {
        SerializedData localSerializedData1 = new SerializedData(paramDraftMessage.getObjectSize());
        paramDraftMessage.serializeToStream(localSerializedData1);
        ((SharedPreferences.Editor)localObject).putString("" + paramLong, Utilities.bytesToHex(localSerializedData1.toByteArray()));
      }
      catch (Exception localException)
      {
        FileLog.e("TSMS", localException);
      }
      break;
      draftMessages.put(Long.valueOf(paramLong), paramMessage);
      SerializedData localSerializedData2 = new SerializedData(paramMessage.getObjectSize());
      paramMessage.serializeToStream(localSerializedData2);
      ((SharedPreferences.Editor)localObject).putString("r_" + paramLong, Utilities.bytesToHex(localSerializedData2.toByteArray()));
      break label147;
      paramMessage = MessagesController.getInstance().getChat(Integer.valueOf(-i));
      break label198;
    }
  }
  
  public static void saveDraft(long paramLong, CharSequence paramCharSequence, ArrayList<TLRPC.MessageEntity> paramArrayList, TLRPC.Message paramMessage, boolean paramBoolean)
  {
    saveDraft(paramLong, paramCharSequence, paramArrayList, paramMessage, paramBoolean, false);
  }
  
  public static void saveDraft(long paramLong, CharSequence paramCharSequence, ArrayList<TLRPC.MessageEntity> paramArrayList, TLRPC.Message paramMessage, boolean paramBoolean1, boolean paramBoolean2)
  {
    Object localObject;
    if ((!TextUtils.isEmpty(paramCharSequence)) || (paramMessage != null))
    {
      localObject = new TLRPC.TL_draftMessage();
      ((TLRPC.DraftMessage)localObject).date = ((int)(System.currentTimeMillis() / 1000L));
      if (paramCharSequence != null) {
        break label209;
      }
      paramCharSequence = "";
      label41:
      ((TLRPC.DraftMessage)localObject).message = paramCharSequence;
      ((TLRPC.DraftMessage)localObject).no_webpage = paramBoolean1;
      if (paramMessage != null)
      {
        ((TLRPC.DraftMessage)localObject).reply_to_msg_id = paramMessage.id;
        ((TLRPC.DraftMessage)localObject).flags |= 0x1;
      }
      if ((paramArrayList != null) && (!paramArrayList.isEmpty()))
      {
        ((TLRPC.DraftMessage)localObject).entities = paramArrayList;
        ((TLRPC.DraftMessage)localObject).flags |= 0x8;
      }
      paramCharSequence = (TLRPC.DraftMessage)drafts.get(Long.valueOf(paramLong));
      if ((paramBoolean2) || (((paramCharSequence == null) || (!paramCharSequence.message.equals(((TLRPC.DraftMessage)localObject).message)) || (paramCharSequence.reply_to_msg_id != ((TLRPC.DraftMessage)localObject).reply_to_msg_id) || (paramCharSequence.no_webpage != ((TLRPC.DraftMessage)localObject).no_webpage)) && ((paramCharSequence != null) || (!TextUtils.isEmpty(((TLRPC.DraftMessage)localObject).message)) || (((TLRPC.DraftMessage)localObject).reply_to_msg_id != 0)))) {
        break label219;
      }
    }
    label209:
    label219:
    do
    {
      return;
      localObject = new TLRPC.TL_draftMessageEmpty();
      break;
      paramCharSequence = paramCharSequence.toString();
      break label41;
      saveDraft(paramLong, (TLRPC.DraftMessage)localObject, paramMessage, false);
      int i = (int)paramLong;
      if (i == 0) {
        break label321;
      }
      paramCharSequence = new TLRPC.TL_messages_saveDraft();
      paramCharSequence.peer = MessagesController.getInputPeer(i);
    } while (paramCharSequence.peer == null);
    paramCharSequence.message = ((TLRPC.DraftMessage)localObject).message;
    paramCharSequence.no_webpage = ((TLRPC.DraftMessage)localObject).no_webpage;
    paramCharSequence.reply_to_msg_id = ((TLRPC.DraftMessage)localObject).reply_to_msg_id;
    paramCharSequence.entities = ((TLRPC.DraftMessage)localObject).entities;
    paramCharSequence.flags = ((TLRPC.DraftMessage)localObject).flags;
    ConnectionsManager.getInstance().sendRequest(paramCharSequence, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
    label321:
    MessagesController.getInstance().sortDialogs(null);
    NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
  }
  
  private static void saveDraftReplyMessage(long paramLong, TLRPC.Message paramMessage)
  {
    if (paramMessage == null) {
      return;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        Object localObject = (TLRPC.DraftMessage)DraftQuery.drafts.get(Long.valueOf(this.val$did));
        if ((localObject != null) && (((TLRPC.DraftMessage)localObject).reply_to_msg_id == this.val$message.id))
        {
          DraftQuery.draftMessages.put(Long.valueOf(this.val$did), this.val$message);
          localObject = new SerializedData(this.val$message.getObjectSize());
          this.val$message.serializeToStream((AbstractSerializedData)localObject);
          DraftQuery.preferences.edit().putString("r_" + this.val$did, Utilities.bytesToHex(((SerializedData)localObject).toByteArray())).commit();
          NotificationCenter.getInstance().postNotificationName(NotificationCenter.newDraftReceived, new Object[] { Long.valueOf(this.val$did) });
        }
      }
    });
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/query/DraftQuery.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */