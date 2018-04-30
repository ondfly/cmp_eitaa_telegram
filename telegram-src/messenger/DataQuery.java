package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutInfo.Builder;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Icon;
import android.os.Build.VERSION;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.widget.Toast;
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
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.DraftMessage;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;
import org.telegram.tgnet.TLRPC.TL_channels_getMessages;
import org.telegram.tgnet.TLRPC.TL_contacts_getTopPeers;
import org.telegram.tgnet.TLRPC.TL_contacts_resetTopPeerRating;
import org.telegram.tgnet.TLRPC.TL_contacts_topPeers;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_documentEmpty;
import org.telegram.tgnet.TLRPC.TL_draftMessage;
import org.telegram.tgnet.TLRPC.TL_draftMessageEmpty;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputDocument;
import org.telegram.tgnet.TLRPC.TL_inputMessageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterDocument;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterEmpty;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterMusic;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterPhotoVideo;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterUrl;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterVoice;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetID;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionGameScore;
import org.telegram.tgnet.TLRPC.TL_messageActionHistoryClear;
import org.telegram.tgnet.TLRPC.TL_messageActionPaymentSent;
import org.telegram.tgnet.TLRPC.TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC.TL_messageEmpty;
import org.telegram.tgnet.TLRPC.TL_messageEntityBold;
import org.telegram.tgnet.TLRPC.TL_messageEntityCode;
import org.telegram.tgnet.TLRPC.TL_messageEntityEmail;
import org.telegram.tgnet.TLRPC.TL_messageEntityItalic;
import org.telegram.tgnet.TLRPC.TL_messageEntityPre;
import org.telegram.tgnet.TLRPC.TL_messageEntityTextUrl;
import org.telegram.tgnet.TLRPC.TL_messageEntityUrl;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_messages_allStickers;
import org.telegram.tgnet.TLRPC.TL_messages_archivedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_channelMessages;
import org.telegram.tgnet.TLRPC.TL_messages_faveSticker;
import org.telegram.tgnet.TLRPC.TL_messages_favedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_featuredStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getAllDrafts;
import org.telegram.tgnet.TLRPC.TL_messages_getAllStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getArchivedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getFavedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getFeaturedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getMaskStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getMessages;
import org.telegram.tgnet.TLRPC.TL_messages_getRecentStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getSavedGifs;
import org.telegram.tgnet.TLRPC.TL_messages_getStickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_installStickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_messages;
import org.telegram.tgnet.TLRPC.TL_messages_messagesSlice;
import org.telegram.tgnet.TLRPC.TL_messages_readFeaturedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_recentStickers;
import org.telegram.tgnet.TLRPC.TL_messages_saveDraft;
import org.telegram.tgnet.TLRPC.TL_messages_saveGif;
import org.telegram.tgnet.TLRPC.TL_messages_savedGifs;
import org.telegram.tgnet.TLRPC.TL_messages_search;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSetInstallResultArchive;
import org.telegram.tgnet.TLRPC.TL_messages_uninstallStickerSet;
import org.telegram.tgnet.TLRPC.TL_peerChat;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_stickerPack;
import org.telegram.tgnet.TLRPC.TL_topPeer;
import org.telegram.tgnet.TLRPC.TL_topPeerCategoryBotsInline;
import org.telegram.tgnet.TLRPC.TL_topPeerCategoryCorrespondents;
import org.telegram.tgnet.TLRPC.TL_topPeerCategoryPeers;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.StickersArchiveAlert;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanUserMention;
import org.telegram.ui.LaunchActivity;

public class DataQuery
{
  private static volatile DataQuery[] Instance = new DataQuery[3];
  public static final int MEDIA_AUDIO = 2;
  public static final int MEDIA_FILE = 1;
  public static final int MEDIA_MUSIC = 4;
  public static final int MEDIA_PHOTOVIDEO = 0;
  public static final int MEDIA_TYPES_COUNT = 5;
  public static final int MEDIA_URL = 3;
  public static final int TYPE_FAVE = 2;
  public static final int TYPE_FEATURED = 3;
  public static final int TYPE_IMAGE = 0;
  public static final int TYPE_MASK = 1;
  private static RectF bitmapRect;
  private static Comparator<TLRPC.MessageEntity> entityComparator = new Comparator()
  {
    public int compare(TLRPC.MessageEntity paramAnonymousMessageEntity1, TLRPC.MessageEntity paramAnonymousMessageEntity2)
    {
      if (paramAnonymousMessageEntity1.offset > paramAnonymousMessageEntity2.offset) {
        return 1;
      }
      if (paramAnonymousMessageEntity1.offset < paramAnonymousMessageEntity2.offset) {
        return -1;
      }
      return 0;
    }
  };
  private static Paint erasePaint;
  private static Paint roundPaint;
  private static Path roundPath;
  private HashMap<String, ArrayList<TLRPC.Document>> allStickers = new HashMap();
  private HashMap<String, ArrayList<TLRPC.Document>> allStickersFeatured = new HashMap();
  private int[] archivedStickersCount = new int[2];
  private SparseArray<TLRPC.BotInfo> botInfos = new SparseArray();
  private LongSparseArray<TLRPC.Message> botKeyboards = new LongSparseArray();
  private SparseLongArray botKeyboardsByMids = new SparseLongArray();
  private int currentAccount;
  private LongSparseArray<TLRPC.Message> draftMessages = new LongSparseArray();
  private LongSparseArray<TLRPC.DraftMessage> drafts = new LongSparseArray();
  private ArrayList<TLRPC.StickerSetCovered> featuredStickerSets = new ArrayList();
  private LongSparseArray<TLRPC.StickerSetCovered> featuredStickerSetsById = new LongSparseArray();
  private boolean featuredStickersLoaded;
  private LongSparseArray<TLRPC.TL_messages_stickerSet> groupStickerSets = new LongSparseArray();
  public ArrayList<TLRPC.TL_topPeer> hints = new ArrayList();
  private boolean inTransaction;
  public ArrayList<TLRPC.TL_topPeer> inlineBots = new ArrayList();
  private LongSparseArray<TLRPC.TL_messages_stickerSet> installedStickerSetsById = new LongSparseArray();
  private long lastMergeDialogId;
  private int lastReqId;
  private int lastReturnedNum;
  private String lastSearchQuery;
  private int[] loadDate = new int[4];
  private int loadFeaturedDate;
  private int loadFeaturedHash;
  private int[] loadHash = new int[4];
  boolean loaded;
  boolean loading;
  private boolean loadingDrafts;
  private boolean loadingFeaturedStickers;
  private boolean loadingRecentGifs;
  private boolean[] loadingRecentStickers = new boolean[3];
  private boolean[] loadingStickers = new boolean[4];
  private int mergeReqId;
  private int[] messagesSearchCount = { 0, 0 };
  private boolean[] messagesSearchEndReached = { 0, 0 };
  private SharedPreferences preferences;
  private ArrayList<Long> readingStickerSets = new ArrayList();
  private ArrayList<TLRPC.Document> recentGifs = new ArrayList();
  private boolean recentGifsLoaded;
  private ArrayList<TLRPC.Document>[] recentStickers = { new ArrayList(), new ArrayList(), new ArrayList() };
  private boolean[] recentStickersLoaded = new boolean[3];
  private int reqId;
  private ArrayList<MessageObject> searchResultMessages = new ArrayList();
  private SparseArray<MessageObject>[] searchResultMessagesMap = { new SparseArray(), new SparseArray() };
  private ArrayList<TLRPC.TL_messages_stickerSet>[] stickerSets = { new ArrayList(), new ArrayList(), new ArrayList(0), new ArrayList() };
  private LongSparseArray<TLRPC.TL_messages_stickerSet> stickerSetsById = new LongSparseArray();
  private HashMap<String, TLRPC.TL_messages_stickerSet> stickerSetsByName = new HashMap();
  private LongSparseArray<String> stickersByEmoji = new LongSparseArray();
  private boolean[] stickersLoaded = new boolean[4];
  private ArrayList<Long> unreadStickerSets = new ArrayList();
  
  public DataQuery(int paramInt)
  {
    this.currentAccount = paramInt;
    Iterator localIterator;
    if (this.currentAccount == 0)
    {
      this.preferences = ApplicationLoader.applicationContext.getSharedPreferences("drafts", 0);
      localIterator = this.preferences.getAll().entrySet().iterator();
    }
    for (;;)
    {
      if (!localIterator.hasNext()) {
        return;
      }
      Object localObject2 = (Map.Entry)localIterator.next();
      long l;
      try
      {
        Object localObject1 = (String)((Map.Entry)localObject2).getKey();
        l = Utilities.parseLong((String)localObject1).longValue();
        localObject2 = new SerializedData(Utilities.hexToBytes((String)((Map.Entry)localObject2).getValue()));
        if (!((String)localObject1).startsWith("r_")) {
          break label634;
        }
        localObject1 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject2, ((SerializedData)localObject2).readInt32(true), true);
        ((TLRPC.Message)localObject1).readAttachPath((AbstractSerializedData)localObject2, UserConfig.getInstance(this.currentAccount).clientUserId);
        if (localObject1 == null) {
          continue;
        }
        this.draftMessages.put(l, localObject1);
      }
      catch (Exception localException) {}
      continue;
      this.preferences = ApplicationLoader.applicationContext.getSharedPreferences("drafts" + this.currentAccount, 0);
      break;
      label634:
      TLRPC.DraftMessage localDraftMessage = TLRPC.DraftMessage.TLdeserialize((AbstractSerializedData)localObject2, ((SerializedData)localObject2).readInt32(true), true);
      if (localDraftMessage != null) {
        this.drafts.put(l, localDraftMessage);
      }
    }
  }
  
  private MessageObject broadcastPinnedMessage(final TLRPC.Message paramMessage, final ArrayList<TLRPC.User> paramArrayList, final ArrayList<TLRPC.Chat> paramArrayList1, final boolean paramBoolean1, boolean paramBoolean2)
  {
    final SparseArray localSparseArray = new SparseArray();
    int i = 0;
    while (i < paramArrayList.size())
    {
      localObject = (TLRPC.User)paramArrayList.get(i);
      localSparseArray.put(((TLRPC.User)localObject).id, localObject);
      i += 1;
    }
    final Object localObject = new SparseArray();
    i = 0;
    while (i < paramArrayList1.size())
    {
      TLRPC.Chat localChat = (TLRPC.Chat)paramArrayList1.get(i);
      ((SparseArray)localObject).put(localChat.id, localChat);
      i += 1;
    }
    if (paramBoolean2) {
      return new MessageObject(this.currentAccount, paramMessage, localSparseArray, (SparseArray)localObject, false);
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance(DataQuery.this.currentAccount).putUsers(paramArrayList, paramBoolean1);
        MessagesController.getInstance(DataQuery.this.currentAccount).putChats(paramArrayList1, paramBoolean1);
        NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.didLoadedPinnedMessage, new Object[] { new MessageObject(DataQuery.this.currentAccount, paramMessage, localSparseArray, localObject, false) });
      }
    });
    return null;
  }
  
  private void broadcastReplyMessages(final ArrayList<TLRPC.Message> paramArrayList, final SparseArray<ArrayList<MessageObject>> paramSparseArray, final ArrayList<TLRPC.User> paramArrayList1, final ArrayList<TLRPC.Chat> paramArrayList2, final long paramLong, final boolean paramBoolean)
  {
    final SparseArray localSparseArray = new SparseArray();
    int i = 0;
    while (i < paramArrayList1.size())
    {
      localObject = (TLRPC.User)paramArrayList1.get(i);
      localSparseArray.put(((TLRPC.User)localObject).id, localObject);
      i += 1;
    }
    final Object localObject = new SparseArray();
    i = 0;
    while (i < paramArrayList2.size())
    {
      TLRPC.Chat localChat = (TLRPC.Chat)paramArrayList2.get(i);
      ((SparseArray)localObject).put(localChat.id, localChat);
      i += 1;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance(DataQuery.this.currentAccount).putUsers(paramArrayList1, paramBoolean);
        MessagesController.getInstance(DataQuery.this.currentAccount).putChats(paramArrayList2, paramBoolean);
        int j = 0;
        int i = 0;
        while (i < paramArrayList.size())
        {
          Object localObject1 = (TLRPC.Message)paramArrayList.get(i);
          ArrayList localArrayList = (ArrayList)paramSparseArray.get(((TLRPC.Message)localObject1).id);
          if (localArrayList != null)
          {
            localObject1 = new MessageObject(DataQuery.this.currentAccount, (TLRPC.Message)localObject1, localSparseArray, localObject, false);
            j = 0;
            if (j < localArrayList.size())
            {
              Object localObject2 = (MessageObject)localArrayList.get(j);
              ((MessageObject)localObject2).replyMessageObject = ((MessageObject)localObject1);
              if ((((MessageObject)localObject2).messageOwner.action instanceof TLRPC.TL_messageActionPinMessage)) {
                ((MessageObject)localObject2).generatePinMessageText(null, null);
              }
              for (;;)
              {
                if (((MessageObject)localObject2).isMegagroup())
                {
                  localObject2 = ((MessageObject)localObject2).replyMessageObject.messageOwner;
                  ((TLRPC.Message)localObject2).flags |= 0x80000000;
                }
                j += 1;
                break;
                if ((((MessageObject)localObject2).messageOwner.action instanceof TLRPC.TL_messageActionGameScore)) {
                  ((MessageObject)localObject2).generateGameMessageText(null);
                } else if ((((MessageObject)localObject2).messageOwner.action instanceof TLRPC.TL_messageActionPaymentSent)) {
                  ((MessageObject)localObject2).generatePaymentSentMessageText(null);
                }
              }
            }
            j = 1;
          }
          i += 1;
        }
        if (j != 0) {
          NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.didLoadedReplyMessages, new Object[] { Long.valueOf(paramLong) });
        }
      }
    });
  }
  
  private static int calcDocumentsHash(ArrayList<TLRPC.Document> paramArrayList)
  {
    if (paramArrayList == null) {
      return 0;
    }
    long l = 0L;
    int i = 0;
    if (i < Math.min(200, paramArrayList.size()))
    {
      TLRPC.Document localDocument = (TLRPC.Document)paramArrayList.get(i);
      if (localDocument == null) {}
      for (;;)
      {
        i += 1;
        break;
        int j = (int)(localDocument.id >> 32);
        int k = (int)localDocument.id;
        l = ((l * 20261L + 2147483648L + j) % 2147483648L * 20261L + 2147483648L + k) % 2147483648L;
      }
    }
    return (int)l;
  }
  
  private int calcFeaturedStickersHash(ArrayList<TLRPC.StickerSetCovered> paramArrayList)
  {
    long l1 = 0L;
    int i = 0;
    if (i < paramArrayList.size())
    {
      TLRPC.StickerSet localStickerSet = ((TLRPC.StickerSetCovered)paramArrayList.get(i)).set;
      if (localStickerSet.archived) {}
      for (;;)
      {
        i += 1;
        break;
        int j = (int)(localStickerSet.id >> 32);
        int k = (int)localStickerSet.id;
        long l2 = ((l1 * 20261L + 2147483648L + j) % 2147483648L * 20261L + 2147483648L + k) % 2147483648L;
        l1 = l2;
        if (this.unreadStickerSets.contains(Long.valueOf(localStickerSet.id))) {
          l1 = (l2 * 20261L + 2147483648L + 1L) % 2147483648L;
        }
      }
    }
    return (int)l1;
  }
  
  private static int calcStickersHash(ArrayList<TLRPC.TL_messages_stickerSet> paramArrayList)
  {
    long l = 0L;
    int i = 0;
    if (i < paramArrayList.size())
    {
      TLRPC.StickerSet localStickerSet = ((TLRPC.TL_messages_stickerSet)paramArrayList.get(i)).set;
      if (localStickerSet.archived) {}
      for (;;)
      {
        i += 1;
        break;
        l = (20261L * l + 2147483648L + localStickerSet.hash) % 2147483648L;
      }
    }
    return (int)l;
  }
  
  public static boolean canAddMessageToMedia(TLRPC.Message paramMessage)
  {
    if (((paramMessage instanceof TLRPC.TL_message_secret)) && (((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) || (MessageObject.isVideoMessage(paramMessage)) || (MessageObject.isGifMessage(paramMessage))) && (paramMessage.media.ttl_seconds != 0) && (paramMessage.media.ttl_seconds <= 60)) {}
    for (;;)
    {
      return false;
      if (((paramMessage instanceof TLRPC.TL_message_secret)) || (!(paramMessage instanceof TLRPC.TL_message)) || ((!(paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) && (!(paramMessage.media instanceof TLRPC.TL_messageMediaDocument))) || (paramMessage.media.ttl_seconds == 0))
      {
        if (((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) || (((paramMessage.media instanceof TLRPC.TL_messageMediaDocument)) && (!MessageObject.isGifDocument(paramMessage.media.document)))) {
          return true;
        }
        if (!paramMessage.entities.isEmpty())
        {
          int i = 0;
          while (i < paramMessage.entities.size())
          {
            TLRPC.MessageEntity localMessageEntity = (TLRPC.MessageEntity)paramMessage.entities.get(i);
            if (((localMessageEntity instanceof TLRPC.TL_messageEntityUrl)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityTextUrl)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityEmail))) {
              return true;
            }
            i += 1;
          }
        }
      }
    }
  }
  
  private static boolean checkInclusion(int paramInt, ArrayList<TLRPC.MessageEntity> paramArrayList)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {}
    for (;;)
    {
      return false;
      int j = paramArrayList.size();
      int i = 0;
      while (i < j)
      {
        TLRPC.MessageEntity localMessageEntity = (TLRPC.MessageEntity)paramArrayList.get(i);
        if ((localMessageEntity.offset <= paramInt) && (localMessageEntity.offset + localMessageEntity.length > paramInt)) {
          return true;
        }
        i += 1;
      }
    }
  }
  
  private static boolean checkIntersection(int paramInt1, int paramInt2, ArrayList<TLRPC.MessageEntity> paramArrayList)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {}
    for (;;)
    {
      return false;
      int j = paramArrayList.size();
      int i = 0;
      while (i < j)
      {
        TLRPC.MessageEntity localMessageEntity = (TLRPC.MessageEntity)paramArrayList.get(i);
        if ((localMessageEntity.offset > paramInt1) && (localMessageEntity.offset + localMessageEntity.length <= paramInt2)) {
          return true;
        }
        i += 1;
      }
    }
  }
  
  private Intent createIntrnalShortcutIntent(long paramLong)
  {
    Intent localIntent = new Intent(ApplicationLoader.applicationContext, OpenChatReceiver.class);
    int i = (int)paramLong;
    int j = (int)(paramLong >> 32);
    if (i == 0)
    {
      localIntent.putExtra("encId", j);
      if (MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(j)) == null) {
        return null;
      }
    }
    else
    {
      if (i <= 0) {
        break label125;
      }
      localIntent.putExtra("userId", i);
    }
    for (;;)
    {
      localIntent.putExtra("currentAccount", this.currentAccount);
      localIntent.setAction("com.tmessages.openchat" + paramLong);
      localIntent.addFlags(67108864);
      return localIntent;
      label125:
      if (i >= 0) {
        break;
      }
      localIntent.putExtra("chatId", -i);
    }
    return null;
  }
  
  private void deletePeer(final int paramInt1, final int paramInt2)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast(String.format(Locale.US, "DELETE FROM chat_hints WHERE did = %d AND type = %d", new Object[] { Integer.valueOf(paramInt1), Integer.valueOf(paramInt2) })).stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public static DataQuery getInstance(int paramInt)
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
        localObject1 = new DataQuery(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (DataQuery)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (DataQuery)localObject1;
  }
  
  private int getMask()
  {
    int i = 0;
    if ((this.lastReturnedNum < this.searchResultMessages.size() - 1) || (this.messagesSearchEndReached[0] == 0) || (this.messagesSearchEndReached[1] == 0)) {
      i = 0x0 | 0x1;
    }
    int j = i;
    if (this.lastReturnedNum > 0) {
      j = i | 0x2;
    }
    return j;
  }
  
  private void getMediaCountDatabase(final long paramLong, int paramInt1, final int paramInt2)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        int i = -1;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT count FROM media_counts_v2 WHERE uid = %d AND type = %d LIMIT 1", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt2) }), new Object[0]);
          if (localSQLiteCursor.next()) {
            i = localSQLiteCursor.intValue(0);
          }
          localSQLiteCursor.dispose();
          int k = (int)paramLong;
          int j = i;
          if (i == -1)
          {
            j = i;
            if (k == 0)
            {
              localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT COUNT(mid) FROM media_v2 WHERE uid = %d AND type = %d LIMIT 1", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt2) }), new Object[0]);
              if (localSQLiteCursor.next()) {
                i = localSQLiteCursor.intValue(0);
              }
              localSQLiteCursor.dispose();
              j = i;
              if (i != -1)
              {
                DataQuery.this.putMediaCountDatabase(paramLong, paramInt2, i);
                j = i;
              }
            }
          }
          DataQuery.this.processLoadedMediaCount(j, paramLong, paramInt2, this.val$classGuid, true);
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public static int getMediaType(TLRPC.Message paramMessage)
  {
    if (paramMessage == null) {}
    for (;;)
    {
      return -1;
      if ((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) {
        return 0;
      }
      if ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument))
      {
        if ((MessageObject.isVoiceMessage(paramMessage)) || (MessageObject.isRoundVideoMessage(paramMessage))) {
          return 2;
        }
        if (MessageObject.isVideoMessage(paramMessage)) {
          return 0;
        }
        if (!MessageObject.isStickerMessage(paramMessage))
        {
          if (MessageObject.isMusicMessage(paramMessage)) {
            return 4;
          }
          return 1;
        }
      }
      else if (!paramMessage.entities.isEmpty())
      {
        int i = 0;
        while (i < paramMessage.entities.size())
        {
          TLRPC.MessageEntity localMessageEntity = (TLRPC.MessageEntity)paramMessage.entities.get(i);
          if (((localMessageEntity instanceof TLRPC.TL_messageEntityUrl)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityTextUrl)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityEmail))) {
            return 3;
          }
          i += 1;
        }
      }
    }
  }
  
  public static long getStickerSetId(TLRPC.Document paramDocument)
  {
    int i = 0;
    while (i < paramDocument.attributes.size())
    {
      TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
      if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeSticker))
      {
        if (!(localDocumentAttribute.stickerset instanceof TLRPC.TL_inputStickerSetID)) {
          break;
        }
        return localDocumentAttribute.stickerset.id;
      }
      i += 1;
    }
    return -1L;
  }
  
  private void loadGroupStickerSet(final TLRPC.StickerSet paramStickerSet, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          try
          {
            SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized("SELECT document FROM web_recent_v3 WHERE id = 's_" + paramStickerSet.id + "'", new Object[0]);
            final TLRPC.TL_messages_stickerSet localTL_messages_stickerSet;
            if ((localSQLiteCursor.next()) && (!localSQLiteCursor.isNull(0)))
            {
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
              if (localNativeByteBuffer != null)
              {
                localTL_messages_stickerSet = TLRPC.TL_messages_stickerSet.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                localNativeByteBuffer.reuse();
              }
            }
            for (;;)
            {
              localSQLiteCursor.dispose();
              if ((localTL_messages_stickerSet == null) || (localTL_messages_stickerSet.set == null) || (localTL_messages_stickerSet.set.hash != paramStickerSet.hash)) {
                DataQuery.this.loadGroupStickerSet(paramStickerSet, false);
              }
              if ((localTL_messages_stickerSet != null) && (localTL_messages_stickerSet.set != null)) {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    DataQuery.this.groupStickerSets.put(localTL_messages_stickerSet.set.id, localTL_messages_stickerSet);
                    NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.groupStickersDidLoaded, new Object[] { Long.valueOf(localTL_messages_stickerSet.set.id) });
                  }
                });
              }
              return;
              localTL_messages_stickerSet = null;
              continue;
              localTL_messages_stickerSet = null;
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
    }
    TLRPC.TL_messages_getStickerSet localTL_messages_getStickerSet = new TLRPC.TL_messages_getStickerSet();
    localTL_messages_getStickerSet.stickerset = new TLRPC.TL_inputStickerSetID();
    localTL_messages_getStickerSet.stickerset.id = paramStickerSet.id;
    localTL_messages_getStickerSet.stickerset.access_hash = paramStickerSet.access_hash;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getStickerSet, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTLObject != null)
        {
          paramAnonymousTLObject = (TLRPC.TL_messages_stickerSet)paramAnonymousTLObject;
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
          {
            public void run()
            {
              try
              {
                SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO web_recent_v3 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                localSQLitePreparedStatement.requery();
                localSQLitePreparedStatement.bindString(1, "s_" + paramAnonymousTLObject.set.id);
                localSQLitePreparedStatement.bindInteger(2, 6);
                localSQLitePreparedStatement.bindString(3, "");
                localSQLitePreparedStatement.bindString(4, "");
                localSQLitePreparedStatement.bindString(5, "");
                localSQLitePreparedStatement.bindInteger(6, 0);
                localSQLitePreparedStatement.bindInteger(7, 0);
                localSQLitePreparedStatement.bindInteger(8, 0);
                localSQLitePreparedStatement.bindInteger(9, 0);
                NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(paramAnonymousTLObject.getObjectSize());
                paramAnonymousTLObject.serializeToStream(localNativeByteBuffer);
                localSQLitePreparedStatement.bindByteBuffer(10, localNativeByteBuffer);
                localSQLitePreparedStatement.step();
                localNativeByteBuffer.reuse();
                localSQLitePreparedStatement.dispose();
                return;
              }
              catch (Exception localException)
              {
                FileLog.e(localException);
              }
            }
          });
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              DataQuery.this.groupStickerSets.put(paramAnonymousTLObject.set.id, paramAnonymousTLObject);
              NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.groupStickersDidLoaded, new Object[] { Long.valueOf(paramAnonymousTLObject.set.id) });
            }
          });
        }
      }
    });
  }
  
  private void loadMediaDatabase(final long paramLong, final int paramInt1, int paramInt2, final int paramInt3, final int paramInt4, final boolean paramBoolean)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        TLRPC.TL_messages_messages localTL_messages_messages = new TLRPC.TL_messages_messages();
        for (;;)
        {
          ArrayList localArrayList1;
          ArrayList localArrayList2;
          int j;
          boolean bool;
          int i;
          try
          {
            localArrayList1 = new ArrayList();
            localArrayList2 = new ArrayList();
            j = paramInt1 + 1;
            localObject2 = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase();
            bool = false;
            if ((int)paramLong == 0) {
              break label997;
            }
            i = 0;
            l1 = paramBoolean;
            if (!paramInt3) {
              break label1306;
            }
            i = -(int)paramLong;
          }
          catch (Exception localException)
          {
            Object localObject2;
            long l3;
            localTL_messages_messages.messages.clear();
            localTL_messages_messages.chats.clear();
            localTL_messages_messages.users.clear();
            FileLog.e(localException);
            return;
            bool = false;
            continue;
            ((SQLiteCursor)localObject4).dispose();
            localObject4 = localException.queryFinalized(String.format(Locale.US, "SELECT min(mid) FROM media_v2 WHERE uid = %d AND type = %d AND mid > 0", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt4) }), new Object[0]);
            if (!((SQLiteCursor)localObject4).next()) {
              continue;
            }
            int k = ((SQLiteCursor)localObject4).intValue(0);
            if (k == 0) {
              continue;
            }
            localObject5 = localException.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
            ((SQLitePreparedStatement)localObject5).requery();
            ((SQLitePreparedStatement)localObject5).bindLong(1, paramLong);
            ((SQLitePreparedStatement)localObject5).bindInteger(2, paramInt4);
            ((SQLitePreparedStatement)localObject5).bindInteger(3, 0);
            ((SQLitePreparedStatement)localObject5).bindInteger(4, k);
            ((SQLitePreparedStatement)localObject5).step();
            ((SQLitePreparedStatement)localObject5).dispose();
            ((SQLiteCursor)localObject4).dispose();
            continue;
          }
          finally
          {
            DataQuery.this.processLoadedMedia(localTL_messages_messages, paramLong, paramInt1, paramBoolean, paramInt4, true, this.val$classGuid, paramInt3, false);
          }
          Object localObject4 = ((SQLiteDatabase)localObject2).queryFinalized(String.format(Locale.US, "SELECT start FROM media_holes_v2 WHERE uid = %d AND type = %d AND start IN (0, 1)", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt4) }), new Object[0]);
          Object localObject1;
          Object localObject5;
          if (((SQLiteCursor)localObject4).next()) {
            if (((SQLiteCursor)localObject4).intValue(0) == 1)
            {
              bool = true;
              ((SQLiteCursor)localObject4).dispose();
              if (localObject1 == 0L) {
                break label790;
              }
              l1 = 0L;
              localObject4 = ((SQLiteDatabase)localObject2).queryFinalized(String.format(Locale.US, "SELECT end FROM media_holes_v2 WHERE uid = %d AND type = %d AND end <= %d ORDER BY end DESC LIMIT 1", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt4), Integer.valueOf(paramBoolean) }), new Object[0]);
              if (((SQLiteCursor)localObject4).next())
              {
                l3 = ((SQLiteCursor)localObject4).intValue(0);
                l1 = l3;
                if (i != 0) {
                  l1 = l3 | i << 32;
                }
              }
              ((SQLiteCursor)localObject4).dispose();
              if (l1 <= 1L) {
                break label729;
              }
              localObject2 = ((SQLiteDatabase)localObject2).queryFinalized(String.format(Locale.US, "SELECT data, mid FROM media_v2 WHERE uid = %d AND mid > 0 AND mid < %d AND mid >= %d AND type = %d ORDER BY date DESC, mid DESC LIMIT %d", new Object[] { Long.valueOf(paramLong), Long.valueOf(localObject1), Long.valueOf(l1), Integer.valueOf(paramInt4), Integer.valueOf(j) }), new Object[0]);
              if (!((SQLiteCursor)localObject2).next()) {
                break label1158;
              }
              localObject4 = ((SQLiteCursor)localObject2).byteBufferValue(0);
              if (localObject4 == null) {
                continue;
              }
              localObject5 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject4, ((NativeByteBuffer)localObject4).readInt32(false), false);
              ((TLRPC.Message)localObject5).readAttachPath((AbstractSerializedData)localObject4, UserConfig.getInstance(DataQuery.this.currentAccount).clientUserId);
              ((NativeByteBuffer)localObject4).reuse();
              ((TLRPC.Message)localObject5).id = ((SQLiteCursor)localObject2).intValue(1);
              ((TLRPC.Message)localObject5).dialog_id = paramLong;
              if ((int)paramLong == 0) {
                ((TLRPC.Message)localObject5).random_id = ((SQLiteCursor)localObject2).longValue(2);
              }
              localTL_messages_messages.messages.add(localObject5);
              if (((TLRPC.Message)localObject5).from_id <= 0) {
                break label1123;
              }
              if (localArrayList1.contains(Integer.valueOf(((TLRPC.Message)localObject5).from_id))) {
                continue;
              }
              localArrayList1.add(Integer.valueOf(((TLRPC.Message)localObject5).from_id));
              continue;
            }
          }
          label729:
          SQLiteCursor localSQLiteCursor = ((SQLiteDatabase)localObject3).queryFinalized(String.format(Locale.US, "SELECT data, mid FROM media_v2 WHERE uid = %d AND mid > 0 AND mid < %d AND type = %d ORDER BY date DESC, mid DESC LIMIT %d", new Object[] { Long.valueOf(paramLong), Long.valueOf(localObject1), Integer.valueOf(paramInt4), Integer.valueOf(j) }), new Object[0]);
          continue;
          label790:
          long l1 = 0L;
          localObject4 = localSQLiteCursor.queryFinalized(String.format(Locale.US, "SELECT max(end) FROM media_holes_v2 WHERE uid = %d AND type = %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt4) }), new Object[0]);
          long l2;
          if (((SQLiteCursor)localObject4).next())
          {
            l2 = ((SQLiteCursor)localObject4).intValue(0);
            l1 = l2;
            if (i != 0) {
              l1 = l2 | i << 32;
            }
          }
          ((SQLiteCursor)localObject4).dispose();
          if (l1 > 1L)
          {
            localSQLiteCursor = localSQLiteCursor.queryFinalized(String.format(Locale.US, "SELECT data, mid FROM media_v2 WHERE uid = %d AND mid >= %d AND type = %d ORDER BY date DESC, mid DESC LIMIT %d", new Object[] { Long.valueOf(paramLong), Long.valueOf(l1), Integer.valueOf(paramInt4), Integer.valueOf(j) }), new Object[0]);
          }
          else
          {
            localSQLiteCursor = localSQLiteCursor.queryFinalized(String.format(Locale.US, "SELECT data, mid FROM media_v2 WHERE uid = %d AND mid > 0 AND type = %d ORDER BY date DESC, mid DESC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt4), Integer.valueOf(j) }), new Object[0]);
            continue;
            label997:
            bool = true;
            if (paramBoolean != 0)
            {
              localSQLiteCursor = localSQLiteCursor.queryFinalized(String.format(Locale.US, "SELECT m.data, m.mid, r.random_id FROM media_v2 as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d AND type = %d ORDER BY m.mid ASC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramBoolean), Integer.valueOf(paramInt4), Integer.valueOf(j) }), new Object[0]);
            }
            else
            {
              localSQLiteCursor = localSQLiteCursor.queryFinalized(String.format(Locale.US, "SELECT m.data, m.mid, r.random_id FROM media_v2 as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND type = %d ORDER BY m.mid ASC LIMIT %d", new Object[] { Long.valueOf(paramLong), Integer.valueOf(paramInt4), Integer.valueOf(j) }), new Object[0]);
              continue;
              label1123:
              if (!localArrayList2.contains(Integer.valueOf(-((TLRPC.Message)localObject5).from_id)))
              {
                localArrayList2.add(Integer.valueOf(-((TLRPC.Message)localObject5).from_id));
                continue;
                label1158:
                localSQLiteCursor.dispose();
                if (!localArrayList1.isEmpty()) {
                  MessagesStorage.getInstance(DataQuery.this.currentAccount).getUsersInternal(TextUtils.join(",", localArrayList1), localTL_messages_messages.users);
                }
                if (!localArrayList2.isEmpty()) {
                  MessagesStorage.getInstance(DataQuery.this.currentAccount).getChatsInternal(TextUtils.join(",", localArrayList2), localTL_messages_messages.chats);
                }
                if (localTL_messages_messages.messages.size() > paramInt1)
                {
                  bool = false;
                  localTL_messages_messages.messages.remove(localTL_messages_messages.messages.size() - 1);
                }
                for (;;)
                {
                  DataQuery.this.processLoadedMedia(localTL_messages_messages, paramLong, paramInt1, paramBoolean, paramInt4, true, this.val$classGuid, paramInt3, bool);
                  return;
                }
                label1306:
                l2 = l1;
                if (l1 != 0L)
                {
                  l2 = l1;
                  if (i != 0) {
                    l2 = l1 | i << 32;
                  }
                }
              }
            }
          }
        }
      }
    });
  }
  
  private MessageObject loadPinnedMessageInternal(final int paramInt1, int paramInt2, boolean paramBoolean)
  {
    long l1 = paramInt2;
    long l2 = paramInt1;
    Object localObject2 = null;
    for (;;)
    {
      try
      {
        localArrayList1 = new ArrayList();
        localArrayList2 = new ArrayList();
        localArrayList3 = new ArrayList();
        localArrayList4 = new ArrayList();
        localSQLiteCursor = MessagesStorage.getInstance(this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, mid, date FROM messages WHERE mid = %d", new Object[] { Long.valueOf(l1 | l2 << 32) }), new Object[0]);
        localObject1 = localObject2;
        NativeByteBuffer localNativeByteBuffer;
        if (localSQLiteCursor.next())
        {
          localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
          localObject1 = localObject2;
          if (localNativeByteBuffer != null)
          {
            localObject1 = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
            ((TLRPC.Message)localObject1).readAttachPath(localNativeByteBuffer, UserConfig.getInstance(this.currentAccount).clientUserId);
            localNativeByteBuffer.reuse();
            if (!(((TLRPC.Message)localObject1).action instanceof TLRPC.TL_messageActionHistoryClear)) {
              continue;
            }
            localObject1 = null;
          }
        }
        localSQLiteCursor.dispose();
        localObject2 = localObject1;
        if (localObject1 != null) {
          continue;
        }
        localSQLiteCursor = MessagesStorage.getInstance(this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT data FROM chat_pinned WHERE uid = %d", new Object[] { Integer.valueOf(paramInt1) }), new Object[0]);
        localObject2 = localObject1;
        if (localSQLiteCursor.next())
        {
          localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
          localObject2 = localObject1;
          if (localNativeByteBuffer != null)
          {
            localObject2 = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
            ((TLRPC.Message)localObject2).readAttachPath(localNativeByteBuffer, UserConfig.getInstance(this.currentAccount).clientUserId);
            localNativeByteBuffer.reuse();
            if (((TLRPC.Message)localObject2).id != paramInt2) {
              continue;
            }
            if (!(((TLRPC.Message)localObject2).action instanceof TLRPC.TL_messageActionHistoryClear)) {
              continue;
            }
          }
        }
      }
      catch (Exception localException)
      {
        ArrayList localArrayList1;
        ArrayList localArrayList2;
        ArrayList localArrayList3;
        ArrayList localArrayList4;
        SQLiteCursor localSQLiteCursor;
        Object localObject1;
        FileLog.e(localException);
        break label557;
        l1 = -paramInt1;
        ((TLRPC.Message)localObject2).dialog_id = l1;
        MessagesStorage.addUsersAndChatsFromMessage((TLRPC.Message)localObject2, localArrayList3, localArrayList4);
        continue;
        if (!paramBoolean) {
          continue;
        }
        return broadcastPinnedMessage((TLRPC.Message)localObject2, localArrayList1, localArrayList2, true, paramBoolean);
        if (localArrayList3.isEmpty()) {
          continue;
        }
        MessagesStorage.getInstance(this.currentAccount).getUsersInternal(TextUtils.join(",", localArrayList3), localArrayList1);
        if (localArrayList4.isEmpty()) {
          continue;
        }
        MessagesStorage.getInstance(this.currentAccount).getChatsInternal(TextUtils.join(",", localArrayList4), localArrayList2);
        broadcastPinnedMessage((TLRPC.Message)localObject2, localArrayList1, localArrayList2, true, false);
        break label557;
        localObject2 = null;
        continue;
      }
      localSQLiteCursor.dispose();
      if (localObject2 != null) {
        continue;
      }
      localObject1 = new TLRPC.TL_channels_getMessages();
      ((TLRPC.TL_channels_getMessages)localObject1).channel = MessagesController.getInstance(this.currentAccount).getInputChannel(paramInt1);
      ((TLRPC.TL_channels_getMessages)localObject1).id.add(Integer.valueOf(paramInt2));
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          int j = 0;
          int i = j;
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.messages_Messages)paramAnonymousTLObject;
            DataQuery.removeEmptyMessages(paramAnonymousTLObject.messages);
            i = j;
            if (!paramAnonymousTLObject.messages.isEmpty())
            {
              ImageLoader.saveMessagesThumbs(paramAnonymousTLObject.messages);
              DataQuery.this.broadcastPinnedMessage((TLRPC.Message)paramAnonymousTLObject.messages.get(0), paramAnonymousTLObject.users, paramAnonymousTLObject.chats, false, false);
              MessagesStorage.getInstance(DataQuery.this.currentAccount).putUsersAndChats(paramAnonymousTLObject.users, paramAnonymousTLObject.chats, true, true);
              DataQuery.this.savePinnedMessage((TLRPC.Message)paramAnonymousTLObject.messages.get(0));
              i = 1;
            }
          }
          if (i == 0) {
            MessagesStorage.getInstance(DataQuery.this.currentAccount).updateChannelPinnedMessage(paramInt1, 0);
          }
        }
      });
      break label557;
      ((TLRPC.Message)localObject1).id = localSQLiteCursor.intValue(1);
      ((TLRPC.Message)localObject1).date = localSQLiteCursor.intValue(2);
      ((TLRPC.Message)localObject1).dialog_id = (-paramInt1);
      MessagesStorage.addUsersAndChatsFromMessage((TLRPC.Message)localObject1, localArrayList3, localArrayList4);
    }
    label557:
    return null;
  }
  
  private void processLoadStickersResponse(final int paramInt, final TLRPC.TL_messages_allStickers paramTL_messages_allStickers)
  {
    final ArrayList localArrayList = new ArrayList();
    if (paramTL_messages_allStickers.sets.isEmpty())
    {
      processLoadedStickers(paramInt, localArrayList, false, (int)(System.currentTimeMillis() / 1000L), paramTL_messages_allStickers.hash);
      return;
    }
    final LongSparseArray localLongSparseArray = new LongSparseArray();
    final int i = 0;
    label51:
    final TLRPC.StickerSet localStickerSet;
    Object localObject;
    if (i < paramTL_messages_allStickers.sets.size())
    {
      localStickerSet = (TLRPC.StickerSet)paramTL_messages_allStickers.sets.get(i);
      localObject = (TLRPC.TL_messages_stickerSet)this.stickerSetsById.get(localStickerSet.id);
      if ((localObject == null) || (((TLRPC.TL_messages_stickerSet)localObject).set.hash != localStickerSet.hash)) {
        break label217;
      }
      ((TLRPC.TL_messages_stickerSet)localObject).set.archived = localStickerSet.archived;
      ((TLRPC.TL_messages_stickerSet)localObject).set.installed = localStickerSet.installed;
      ((TLRPC.TL_messages_stickerSet)localObject).set.official = localStickerSet.official;
      localLongSparseArray.put(((TLRPC.TL_messages_stickerSet)localObject).set.id, localObject);
      localArrayList.add(localObject);
      if (localLongSparseArray.size() == paramTL_messages_allStickers.sets.size()) {
        processLoadedStickers(paramInt, localArrayList, false, (int)(System.currentTimeMillis() / 1000L), paramTL_messages_allStickers.hash);
      }
    }
    for (;;)
    {
      i += 1;
      break label51;
      break;
      label217:
      localArrayList.add(null);
      localObject = new TLRPC.TL_messages_getStickerSet();
      ((TLRPC.TL_messages_getStickerSet)localObject).stickerset = new TLRPC.TL_inputStickerSetID();
      ((TLRPC.TL_messages_getStickerSet)localObject).stickerset.id = localStickerSet.id;
      ((TLRPC.TL_messages_getStickerSet)localObject).stickerset.access_hash = localStickerSet.access_hash;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              TLRPC.TL_messages_stickerSet localTL_messages_stickerSet = (TLRPC.TL_messages_stickerSet)paramAnonymousTLObject;
              DataQuery.23.this.val$newStickerArray.set(DataQuery.23.this.val$index, localTL_messages_stickerSet);
              DataQuery.23.this.val$newStickerSets.put(DataQuery.23.this.val$stickerSet.id, localTL_messages_stickerSet);
              if (DataQuery.23.this.val$newStickerSets.size() == DataQuery.23.this.val$res.sets.size())
              {
                int i = 0;
                while (i < DataQuery.23.this.val$newStickerArray.size())
                {
                  if (DataQuery.23.this.val$newStickerArray.get(i) == null) {
                    DataQuery.23.this.val$newStickerArray.remove(i);
                  }
                  i += 1;
                }
                DataQuery.this.processLoadedStickers(DataQuery.23.this.val$type, DataQuery.23.this.val$newStickerArray, false, (int)(System.currentTimeMillis() / 1000L), DataQuery.23.this.val$res.hash);
              }
            }
          });
        }
      });
    }
  }
  
  private void processLoadedFeaturedStickers(final ArrayList<TLRPC.StickerSetCovered> paramArrayList, final ArrayList<Long> paramArrayList1, final boolean paramBoolean, final int paramInt1, final int paramInt2)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        DataQuery.access$1202(DataQuery.this, false);
        DataQuery.access$1302(DataQuery.this, true);
      }
    });
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        long l = 1000L;
        final Object localObject;
        if (((paramBoolean) && ((paramArrayList == null) || (Math.abs(System.currentTimeMillis() / 1000L - paramInt1) >= 3600L))) || ((!paramBoolean) && (paramArrayList == null) && (paramInt2 == 0)))
        {
          localObject = new Runnable()
          {
            public void run()
            {
              if ((DataQuery.17.this.val$res != null) && (DataQuery.17.this.val$hash != 0)) {
                DataQuery.access$1402(DataQuery.this, DataQuery.17.this.val$hash);
              }
              DataQuery.this.loadFeaturedStickers(false, false);
            }
          };
          if ((paramArrayList == null) && (!paramBoolean))
          {
            AndroidUtilities.runOnUIThread((Runnable)localObject, l);
            if (paramArrayList != null) {
              break label105;
            }
          }
        }
        label105:
        do
        {
          return;
          l = 0L;
          break;
          if (paramArrayList != null) {
            try
            {
              localObject = new ArrayList();
              final LongSparseArray localLongSparseArray = new LongSparseArray();
              int i = 0;
              while (i < paramArrayList.size())
              {
                TLRPC.StickerSetCovered localStickerSetCovered = (TLRPC.StickerSetCovered)paramArrayList.get(i);
                ((ArrayList)localObject).add(localStickerSetCovered);
                localLongSparseArray.put(localStickerSetCovered.set.id, localStickerSetCovered);
                i += 1;
              }
              if (!paramBoolean) {
                DataQuery.this.putFeaturedStickersToCache((ArrayList)localObject, paramArrayList1, paramInt1, paramInt2);
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  DataQuery.access$1602(DataQuery.this, DataQuery.17.this.val$unreadStickers);
                  DataQuery.access$1702(DataQuery.this, localLongSparseArray);
                  DataQuery.access$1802(DataQuery.this, localObject);
                  DataQuery.access$1402(DataQuery.this, DataQuery.17.this.val$hash);
                  DataQuery.access$1902(DataQuery.this, DataQuery.17.this.val$date);
                  DataQuery.this.loadStickers(3, true, false);
                  NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.featuredStickersDidLoaded, new Object[0]);
                }
              });
              return;
            }
            catch (Throwable localThrowable)
            {
              FileLog.e(localThrowable);
              return;
            }
          }
        } while (paramBoolean);
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            DataQuery.access$1902(DataQuery.this, DataQuery.17.this.val$date);
          }
        });
        DataQuery.this.putFeaturedStickersToCache(null, null, paramInt1, 0);
      }
    });
  }
  
  private void processLoadedMedia(final TLRPC.messages_Messages parammessages_Messages, final long paramLong, int paramInt1, int paramInt2, final int paramInt3, final boolean paramBoolean1, final int paramInt4, boolean paramBoolean2, final boolean paramBoolean3)
  {
    int i = (int)paramLong;
    if ((paramBoolean1) && (parammessages_Messages.messages.isEmpty()) && (i != 0))
    {
      loadMedia(paramLong, paramInt1, paramInt2, paramInt3, false, paramInt4);
      return;
    }
    if (!paramBoolean1)
    {
      ImageLoader.saveMessagesThumbs(parammessages_Messages.messages);
      MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(parammessages_Messages.users, parammessages_Messages.chats, true, true);
      putMediaDatabase(paramLong, paramInt3, parammessages_Messages.messages, paramInt2, paramBoolean3);
    }
    SparseArray localSparseArray = new SparseArray();
    paramInt1 = 0;
    while (paramInt1 < parammessages_Messages.users.size())
    {
      localObject = (TLRPC.User)parammessages_Messages.users.get(paramInt1);
      localSparseArray.put(((TLRPC.User)localObject).id, localObject);
      paramInt1 += 1;
    }
    Object localObject = new ArrayList();
    paramInt1 = 0;
    while (paramInt1 < parammessages_Messages.messages.size())
    {
      TLRPC.Message localMessage = (TLRPC.Message)parammessages_Messages.messages.get(paramInt1);
      ((ArrayList)localObject).add(new MessageObject(this.currentAccount, localMessage, localSparseArray, true));
      paramInt1 += 1;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        int i = parammessages_Messages.count;
        MessagesController.getInstance(DataQuery.this.currentAccount).putUsers(parammessages_Messages.users, paramBoolean1);
        MessagesController.getInstance(DataQuery.this.currentAccount).putChats(parammessages_Messages.chats, paramBoolean1);
        NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.mediaDidLoaded, new Object[] { Long.valueOf(paramLong), Integer.valueOf(i), paramInt4, Integer.valueOf(paramInt3), Integer.valueOf(paramBoolean3), Boolean.valueOf(this.val$topReached) });
      }
    });
  }
  
  private void processLoadedMediaCount(final int paramInt1, final long paramLong, final int paramInt2, final int paramInt3, boolean paramBoolean)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        int i = 0;
        int j = (int)paramLong;
        if ((paramInt1) && (paramInt2 == -1) && (j != 0))
        {
          DataQuery.this.getMediaCount(paramLong, paramInt3, this.val$classGuid, false);
          return;
        }
        if (!paramInt1) {
          DataQuery.this.putMediaCountDatabase(paramLong, paramInt3, paramInt2);
        }
        NotificationCenter localNotificationCenter = NotificationCenter.getInstance(DataQuery.this.currentAccount);
        j = NotificationCenter.mediaCountDidLoaded;
        long l = paramLong;
        if ((paramInt1) && (paramInt2 == -1)) {}
        for (;;)
        {
          localNotificationCenter.postNotificationName(j, new Object[] { Long.valueOf(l), Integer.valueOf(i), Boolean.valueOf(paramInt1), Integer.valueOf(paramInt3) });
          return;
          i = paramInt2;
        }
      }
    });
  }
  
  private void processLoadedRecentDocuments(final int paramInt1, final ArrayList<TLRPC.Document> paramArrayList, final boolean paramBoolean, final int paramInt2)
  {
    if (paramArrayList != null) {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          for (;;)
          {
            int i;
            int n;
            int j;
            try
            {
              SQLiteDatabase localSQLiteDatabase = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase();
              SQLitePreparedStatement localSQLitePreparedStatement;
              if (paramBoolean)
              {
                i = MessagesController.getInstance(DataQuery.this.currentAccount).maxRecentGifsCount;
                localSQLiteDatabase.beginTransaction();
                localSQLitePreparedStatement = localSQLiteDatabase.executeFast("REPLACE INTO web_recent_v3 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                n = paramArrayList.size();
                if (paramBoolean)
                {
                  j = 2;
                  break label421;
                  localSQLitePreparedStatement.dispose();
                  localSQLiteDatabase.commitTransaction();
                  if (paramArrayList.size() < i) {
                    continue;
                  }
                  localSQLiteDatabase.beginTransaction();
                  if (i >= paramArrayList.size()) {
                    continue;
                  }
                  localSQLiteDatabase.executeFast("DELETE FROM web_recent_v3 WHERE id = '" + ((TLRPC.Document)paramArrayList.get(i)).id + "' AND type = " + j).stepThis().dispose();
                  i += 1;
                  continue;
                }
              }
              else
              {
                if (paramInt1 == 2)
                {
                  i = MessagesController.getInstance(DataQuery.this.currentAccount).maxFaveStickersCount;
                  continue;
                }
                i = MessagesController.getInstance(DataQuery.this.currentAccount).maxRecentStickersCount;
                continue;
              }
              if (paramInt1 == 0)
              {
                j = 3;
              }
              else
              {
                if (paramInt1 != 1) {
                  break label437;
                }
                j = 4;
                break label421;
                label234:
                TLRPC.Document localDocument = (TLRPC.Document)paramArrayList.get(k);
                localSQLitePreparedStatement.requery();
                localSQLitePreparedStatement.bindString(1, "" + localDocument.id);
                localSQLitePreparedStatement.bindInteger(2, j);
                localSQLitePreparedStatement.bindString(3, "");
                localSQLitePreparedStatement.bindString(4, "");
                localSQLitePreparedStatement.bindString(5, "");
                localSQLitePreparedStatement.bindInteger(6, 0);
                localSQLitePreparedStatement.bindInteger(7, 0);
                localSQLitePreparedStatement.bindInteger(8, 0);
                if (paramInt2 == 0) {
                  break label449;
                }
                m = paramInt2;
                localSQLitePreparedStatement.bindInteger(9, m);
                NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(localDocument.getObjectSize());
                localDocument.serializeToStream(localNativeByteBuffer);
                localSQLitePreparedStatement.bindByteBuffer(10, localNativeByteBuffer);
                localSQLitePreparedStatement.step();
                if (localNativeByteBuffer == null) {
                  break label442;
                }
                localNativeByteBuffer.reuse();
                break label442;
                localSQLiteDatabase.commitTransaction();
                return;
              }
            }
            catch (Exception localException)
            {
              FileLog.e(localException);
              return;
            }
            label421:
            int k = 0;
            while (k < n)
            {
              if (k != i) {
                break label234;
              }
              break;
              label437:
              j = 5;
              break label421;
              label442:
              k += 1;
            }
            label449:
            int m = n - k;
          }
        }
      });
    }
    if (paramInt2 == 0) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          SharedPreferences.Editor localEditor = MessagesController.getEmojiSettings(DataQuery.this.currentAccount).edit();
          if (paramBoolean)
          {
            DataQuery.access$402(DataQuery.this, false);
            DataQuery.access$502(DataQuery.this, true);
            localEditor.putLong("lastGifLoadTime", System.currentTimeMillis()).commit();
            if (paramArrayList != null)
            {
              if (!paramBoolean) {
                break label226;
              }
              DataQuery.access$302(DataQuery.this, paramArrayList);
            }
          }
          for (;;)
          {
            NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.recentDocumentsDidLoaded, new Object[] { Boolean.valueOf(paramBoolean), Integer.valueOf(paramInt1) });
            return;
            DataQuery.this.loadingRecentStickers[paramInt1] = 0;
            DataQuery.this.recentStickersLoaded[paramInt1] = 1;
            if (paramInt1 == 0)
            {
              localEditor.putLong("lastStickersLoadTime", System.currentTimeMillis()).commit();
              break;
            }
            if (paramInt1 == 1)
            {
              localEditor.putLong("lastStickersLoadTimeMask", System.currentTimeMillis()).commit();
              break;
            }
            localEditor.putLong("lastStickersLoadTimeFavs", System.currentTimeMillis()).commit();
            break;
            label226:
            DataQuery.this.recentStickers[paramInt1] = paramArrayList;
          }
        }
      });
    }
  }
  
  private void processLoadedStickers(final int paramInt1, final ArrayList<TLRPC.TL_messages_stickerSet> paramArrayList, final boolean paramBoolean, final int paramInt2, final int paramInt3)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        DataQuery.this.loadingStickers[paramInt1] = 0;
        DataQuery.this.stickersLoaded[paramInt1] = 1;
      }
    });
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject1;
        if (((paramBoolean) && ((paramArrayList == null) || (Math.abs(System.currentTimeMillis() / 1000L - paramInt2) >= 3600L))) || ((!paramBoolean) && (paramArrayList == null) && (paramInt3 == 0)))
        {
          localObject1 = new Runnable()
          {
            public void run()
            {
              if ((DataQuery.28.this.val$res != null) && (DataQuery.28.this.val$hash != 0)) {
                DataQuery.this.loadHash[DataQuery.28.this.val$type] = DataQuery.28.this.val$hash;
              }
              DataQuery.this.loadStickers(DataQuery.28.this.val$type, false, false);
            }
          };
          if ((paramArrayList == null) && (!paramBoolean)) {}
          for (long l = 1000L;; l = 0L)
          {
            AndroidUtilities.runOnUIThread((Runnable)localObject1, l);
            if (paramArrayList != null) {
              break;
            }
            return;
          }
        }
        final ArrayList localArrayList;
        final LongSparseArray localLongSparseArray1;
        final HashMap localHashMap1;
        final LongSparseArray localLongSparseArray2;
        LongSparseArray localLongSparseArray3;
        final HashMap localHashMap2;
        int i;
        label171:
        int j;
        label244:
        label322:
        TLRPC.TL_stickerPack localTL_stickerPack;
        Object localObject3;
        Object localObject2;
        if (paramArrayList != null)
        {
          TLRPC.TL_messages_stickerSet localTL_messages_stickerSet;
          try
          {
            localArrayList = new ArrayList();
            localLongSparseArray1 = new LongSparseArray();
            localHashMap1 = new HashMap();
            localLongSparseArray2 = new LongSparseArray();
            localLongSparseArray3 = new LongSparseArray();
            localHashMap2 = new HashMap();
            i = 0;
            if (i >= paramArrayList.size()) {
              break label515;
            }
            localTL_messages_stickerSet = (TLRPC.TL_messages_stickerSet)paramArrayList.get(i);
            if (localTL_messages_stickerSet == null) {
              break label601;
            }
            localArrayList.add(localTL_messages_stickerSet);
            localLongSparseArray1.put(localTL_messages_stickerSet.set.id, localTL_messages_stickerSet);
            localHashMap1.put(localTL_messages_stickerSet.set.short_name, localTL_messages_stickerSet);
            j = 0;
            if (j < localTL_messages_stickerSet.documents.size())
            {
              localObject1 = (TLRPC.Document)localTL_messages_stickerSet.documents.get(j);
              if ((localObject1 == null) || ((localObject1 instanceof TLRPC.TL_documentEmpty))) {
                break label608;
              }
              localLongSparseArray3.put(((TLRPC.Document)localObject1).id, localObject1);
            }
          }
          catch (Throwable localThrowable)
          {
            FileLog.e(localThrowable);
            return;
          }
          if (localTL_messages_stickerSet.set.archived) {
            break label601;
          }
          j = 0;
          if (j >= localTL_messages_stickerSet.packs.size()) {
            break label601;
          }
          localTL_stickerPack = (TLRPC.TL_stickerPack)localTL_messages_stickerSet.packs.get(j);
          if ((localTL_stickerPack == null) || (localTL_stickerPack.emoticon == null)) {
            break label615;
          }
          localTL_stickerPack.emoticon = localTL_stickerPack.emoticon.replace("️", "");
          localObject3 = (ArrayList)localHashMap2.get(localTL_stickerPack.emoticon);
          localObject2 = localObject3;
          if (localObject3 != null) {
            break label622;
          }
          localObject2 = new ArrayList();
          localHashMap2.put(localTL_stickerPack.emoticon, localObject2);
          break label622;
        }
        for (;;)
        {
          if (k < localTL_stickerPack.documents.size())
          {
            localObject3 = (Long)localTL_stickerPack.documents.get(k);
            if (localLongSparseArray2.indexOfKey(((Long)localObject3).longValue()) < 0) {
              localLongSparseArray2.put(((Long)localObject3).longValue(), localTL_stickerPack.emoticon);
            }
            localObject3 = (TLRPC.Document)localLongSparseArray3.get(((Long)localObject3).longValue());
            if (localObject3 == null) {
              break label627;
            }
            ((ArrayList)localObject2).add(localObject3);
            break label627;
            label515:
            if (!paramBoolean) {
              DataQuery.this.putStickersToCache(paramInt1, localArrayList, paramInt2, paramInt3);
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                int i = 0;
                while (i < DataQuery.this.stickerSets[DataQuery.28.this.val$type].size())
                {
                  TLRPC.StickerSet localStickerSet = ((TLRPC.TL_messages_stickerSet)DataQuery.this.stickerSets[DataQuery.28.this.val$type].get(i)).set;
                  DataQuery.this.stickerSetsById.remove(localStickerSet.id);
                  DataQuery.this.installedStickerSetsById.remove(localStickerSet.id);
                  DataQuery.this.stickerSetsByName.remove(localStickerSet.short_name);
                  i += 1;
                }
                i = 0;
                while (i < localLongSparseArray1.size())
                {
                  DataQuery.this.stickerSetsById.put(localLongSparseArray1.keyAt(i), localLongSparseArray1.valueAt(i));
                  if (DataQuery.28.this.val$type != 3) {
                    DataQuery.this.installedStickerSetsById.put(localLongSparseArray1.keyAt(i), localLongSparseArray1.valueAt(i));
                  }
                  i += 1;
                }
                DataQuery.this.stickerSetsByName.putAll(localHashMap1);
                DataQuery.this.stickerSets[DataQuery.28.this.val$type] = localArrayList;
                DataQuery.this.loadHash[DataQuery.28.this.val$type] = DataQuery.28.this.val$hash;
                DataQuery.this.loadDate[DataQuery.28.this.val$type] = DataQuery.28.this.val$date;
                if (DataQuery.28.this.val$type == 0)
                {
                  DataQuery.access$3402(DataQuery.this, localHashMap2);
                  DataQuery.access$3502(DataQuery.this, localLongSparseArray2);
                }
                for (;;)
                {
                  NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.stickersDidLoaded, new Object[] { Integer.valueOf(DataQuery.28.this.val$type) });
                  return;
                  if (DataQuery.28.this.val$type == 3) {
                    DataQuery.access$3602(DataQuery.this, localHashMap2);
                  }
                }
              }
            });
            return;
            if (paramBoolean) {
              break;
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                DataQuery.this.loadDate[DataQuery.28.this.val$type] = DataQuery.28.this.val$date;
              }
            });
            DataQuery.this.putStickersToCache(paramInt1, null, paramInt2, 0);
            return;
            label601:
            i += 1;
            break label171;
            label608:
            j += 1;
            break label244;
          }
          label615:
          j += 1;
          break label322;
          label622:
          int k = 0;
          continue;
          label627:
          k += 1;
        }
      }
    });
  }
  
  private void putFeaturedStickersToCache(final ArrayList<TLRPC.StickerSetCovered> paramArrayList, final ArrayList<Long> paramArrayList1, final int paramInt1, final int paramInt2)
  {
    if (paramArrayList != null) {}
    for (paramArrayList = new ArrayList(paramArrayList);; paramArrayList = null)
    {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          try
          {
            if (paramArrayList != null)
            {
              localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO stickers_featured VALUES(?, ?, ?, ?, ?)");
              localSQLitePreparedStatement.requery();
              int j = 4;
              int i = 0;
              while (i < paramArrayList.size())
              {
                j += ((TLRPC.StickerSetCovered)paramArrayList.get(i)).getObjectSize();
                i += 1;
              }
              NativeByteBuffer localNativeByteBuffer1 = new NativeByteBuffer(j);
              NativeByteBuffer localNativeByteBuffer2 = new NativeByteBuffer(paramArrayList1.size() * 8 + 4);
              localNativeByteBuffer1.writeInt32(paramArrayList.size());
              i = 0;
              while (i < paramArrayList.size())
              {
                ((TLRPC.StickerSetCovered)paramArrayList.get(i)).serializeToStream(localNativeByteBuffer1);
                i += 1;
              }
              localNativeByteBuffer2.writeInt32(paramArrayList1.size());
              i = 0;
              while (i < paramArrayList1.size())
              {
                localNativeByteBuffer2.writeInt64(((Long)paramArrayList1.get(i)).longValue());
                i += 1;
              }
              localSQLitePreparedStatement.bindInteger(1, 1);
              localSQLitePreparedStatement.bindByteBuffer(2, localNativeByteBuffer1);
              localSQLitePreparedStatement.bindByteBuffer(3, localNativeByteBuffer2);
              localSQLitePreparedStatement.bindInteger(4, paramInt1);
              localSQLitePreparedStatement.bindInteger(5, paramInt2);
              localSQLitePreparedStatement.step();
              localNativeByteBuffer1.reuse();
              localNativeByteBuffer2.reuse();
              localSQLitePreparedStatement.dispose();
              return;
            }
            SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("UPDATE stickers_featured SET date = ?");
            localSQLitePreparedStatement.requery();
            localSQLitePreparedStatement.bindInteger(1, paramInt1);
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
      return;
    }
  }
  
  private void putMediaCountDatabase(final long paramLong, int paramInt1, final int paramInt2)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO media_counts_v2 VALUES(?, ?, ?)");
          localSQLitePreparedStatement.requery();
          localSQLitePreparedStatement.bindLong(1, paramLong);
          localSQLitePreparedStatement.bindInteger(2, paramInt2);
          localSQLitePreparedStatement.bindInteger(3, this.val$count);
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
  
  private void putMediaDatabase(final long paramLong, final int paramInt1, final ArrayList<TLRPC.Message> paramArrayList, int paramInt2, final boolean paramBoolean)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        int i = 1;
        try
        {
          if ((paramArrayList.isEmpty()) || (paramBoolean))
          {
            MessagesStorage.getInstance(DataQuery.this.currentAccount).doneHolesInMedia(paramLong, paramInt1, this.val$type);
            if (paramArrayList.isEmpty()) {
              return;
            }
          }
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().beginTransaction();
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
          Iterator localIterator = paramArrayList.iterator();
          while (localIterator.hasNext())
          {
            TLRPC.Message localMessage = (TLRPC.Message)localIterator.next();
            if (DataQuery.canAddMessageToMedia(localMessage))
            {
              long l2 = localMessage.id;
              long l1 = l2;
              if (localMessage.to_id.channel_id != 0) {
                l1 = l2 | localMessage.to_id.channel_id << 32;
              }
              localSQLitePreparedStatement.requery();
              NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(localMessage.getObjectSize());
              localMessage.serializeToStream(localNativeByteBuffer);
              localSQLitePreparedStatement.bindLong(1, l1);
              localSQLitePreparedStatement.bindLong(2, paramLong);
              localSQLitePreparedStatement.bindInteger(3, localMessage.date);
              localSQLitePreparedStatement.bindInteger(4, this.val$type);
              localSQLitePreparedStatement.bindByteBuffer(5, localNativeByteBuffer);
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
        if ((!paramBoolean) || (paramInt1 != 0))
        {
          if (!paramBoolean) {
            break label338;
          }
          if (paramInt1 == 0) {
            break label364;
          }
          MessagesStorage.getInstance(DataQuery.this.currentAccount).closeHolesInMedia(paramLong, i, paramInt1, this.val$type);
        }
        for (;;)
        {
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().commitTransaction();
          return;
          label338:
          i = ((TLRPC.Message)paramArrayList.get(paramArrayList.size() - 1)).id;
          break;
          label364:
          MessagesStorage.getInstance(DataQuery.this.currentAccount).closeHolesInMedia(paramLong, i, Integer.MAX_VALUE, this.val$type);
        }
      }
    });
  }
  
  private void putStickersToCache(final int paramInt1, final ArrayList<TLRPC.TL_messages_stickerSet> paramArrayList, final int paramInt2, final int paramInt3)
  {
    if (paramArrayList != null) {}
    for (paramArrayList = new ArrayList(paramArrayList);; paramArrayList = null)
    {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          try
          {
            if (paramArrayList != null)
            {
              localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO stickers_v2 VALUES(?, ?, ?, ?)");
              localSQLitePreparedStatement.requery();
              int j = 4;
              int i = 0;
              while (i < paramArrayList.size())
              {
                j += ((TLRPC.TL_messages_stickerSet)paramArrayList.get(i)).getObjectSize();
                i += 1;
              }
              NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(j);
              localNativeByteBuffer.writeInt32(paramArrayList.size());
              i = 0;
              while (i < paramArrayList.size())
              {
                ((TLRPC.TL_messages_stickerSet)paramArrayList.get(i)).serializeToStream(localNativeByteBuffer);
                i += 1;
              }
              localSQLitePreparedStatement.bindInteger(1, paramInt1 + 1);
              localSQLitePreparedStatement.bindByteBuffer(2, localNativeByteBuffer);
              localSQLitePreparedStatement.bindInteger(3, paramInt2);
              localSQLitePreparedStatement.bindInteger(4, paramInt3);
              localSQLitePreparedStatement.step();
              localNativeByteBuffer.reuse();
              localSQLitePreparedStatement.dispose();
              return;
            }
            SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("UPDATE stickers_v2 SET date = ?");
            localSQLitePreparedStatement.requery();
            localSQLitePreparedStatement.bindInteger(1, paramInt2);
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
      return;
    }
  }
  
  private static void removeEmptyMessages(ArrayList<TLRPC.Message> paramArrayList)
  {
    int j;
    for (int i = 0; i < paramArrayList.size(); i = j + 1)
    {
      TLRPC.Message localMessage = (TLRPC.Message)paramArrayList.get(i);
      if ((localMessage != null) && (!(localMessage instanceof TLRPC.TL_messageEmpty)))
      {
        j = i;
        if (!(localMessage.action instanceof TLRPC.TL_messageActionHistoryClear)) {}
      }
      else
      {
        paramArrayList.remove(i);
        j = i - 1;
      }
    }
  }
  
  private static void removeOffsetAfter(int paramInt1, int paramInt2, ArrayList<TLRPC.MessageEntity> paramArrayList)
  {
    int j = paramArrayList.size();
    int i = 0;
    while (i < j)
    {
      TLRPC.MessageEntity localMessageEntity = (TLRPC.MessageEntity)paramArrayList.get(i);
      if (localMessageEntity.offset > paramInt1) {
        localMessageEntity.offset -= paramInt2;
      }
      i += 1;
    }
  }
  
  private void saveDraftReplyMessage(final long paramLong, TLRPC.Message paramMessage)
  {
    if (paramMessage == null) {
      return;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        Object localObject = (TLRPC.DraftMessage)DataQuery.this.drafts.get(paramLong);
        if ((localObject != null) && (((TLRPC.DraftMessage)localObject).reply_to_msg_id == this.val$message.id))
        {
          DataQuery.this.draftMessages.put(paramLong, this.val$message);
          localObject = new SerializedData(this.val$message.getObjectSize());
          this.val$message.serializeToStream((AbstractSerializedData)localObject);
          DataQuery.this.preferences.edit().putString("r_" + paramLong, Utilities.bytesToHex(((SerializedData)localObject).toByteArray())).commit();
          NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.newDraftReceived, new Object[] { Long.valueOf(paramLong) });
        }
      }
    });
  }
  
  private void savePeer(final int paramInt1, final int paramInt2, final double paramDouble)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO chat_hints VALUES(?, ?, ?, ?)");
          localSQLitePreparedStatement.requery();
          localSQLitePreparedStatement.bindInteger(1, paramInt1);
          localSQLitePreparedStatement.bindInteger(2, paramInt2);
          localSQLitePreparedStatement.bindDouble(3, paramDouble);
          localSQLitePreparedStatement.bindInteger(4, (int)System.currentTimeMillis() / 1000);
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
  
  private void savePinnedMessage(final TLRPC.Message paramMessage)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().beginTransaction();
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO chat_pinned VALUES(?, ?, ?)");
          NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(paramMessage.getObjectSize());
          paramMessage.serializeToStream(localNativeByteBuffer);
          localSQLitePreparedStatement.requery();
          localSQLitePreparedStatement.bindInteger(1, paramMessage.to_id.channel_id);
          localSQLitePreparedStatement.bindInteger(2, paramMessage.id);
          localSQLitePreparedStatement.bindByteBuffer(3, localNativeByteBuffer);
          localSQLitePreparedStatement.step();
          localNativeByteBuffer.reuse();
          localSQLitePreparedStatement.dispose();
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().commitTransaction();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  private void saveReplyMessages(final SparseArray<ArrayList<MessageObject>> paramSparseArray, final ArrayList<TLRPC.Message> paramArrayList)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          int i;
          try
          {
            MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().beginTransaction();
            SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("UPDATE messages SET replydata = ? WHERE mid = ?");
            i = 0;
            if (i < paramArrayList.size())
            {
              Object localObject = (TLRPC.Message)paramArrayList.get(i);
              ArrayList localArrayList = (ArrayList)paramSparseArray.get(((TLRPC.Message)localObject).id);
              if (localArrayList != null)
              {
                NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(((TLRPC.Message)localObject).getObjectSize());
                ((TLRPC.Message)localObject).serializeToStream(localNativeByteBuffer);
                int j = 0;
                if (j < localArrayList.size())
                {
                  localObject = (MessageObject)localArrayList.get(j);
                  localSQLitePreparedStatement.requery();
                  long l2 = ((MessageObject)localObject).getId();
                  long l1 = l2;
                  if (((MessageObject)localObject).messageOwner.to_id.channel_id != 0) {
                    l1 = l2 | ((MessageObject)localObject).messageOwner.to_id.channel_id << 32;
                  }
                  localSQLitePreparedStatement.bindByteBuffer(1, localNativeByteBuffer);
                  localSQLitePreparedStatement.bindLong(2, l1);
                  localSQLitePreparedStatement.step();
                  j += 1;
                  continue;
                }
                localNativeByteBuffer.reuse();
              }
            }
            else
            {
              localSQLitePreparedStatement.dispose();
              MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().commitTransaction();
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
  
  private void searchMessagesInChat(String paramString, final long paramLong1, final long paramLong2, final int paramInt1, final int paramInt2, boolean paramBoolean, final TLRPC.User paramUser)
  {
    int j = 0;
    int m = 0;
    final long l2 = paramLong1;
    int i;
    if (!paramBoolean)
    {
      i = 1;
      if (this.reqId != 0)
      {
        ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
        this.reqId = 0;
      }
      if (this.mergeReqId != 0)
      {
        ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.mergeReqId, true);
        this.mergeReqId = 0;
      }
      if (paramString != null) {
        break label695;
      }
      if (!this.searchResultMessages.isEmpty()) {
        break label92;
      }
    }
    label92:
    Object localObject1;
    label315:
    final Object localObject2;
    label508:
    label538:
    label695:
    label827:
    label846:
    do
    {
      int k;
      long l1;
      do
      {
        return;
        i = 0;
        break;
        if (paramInt2 != 1) {
          break label538;
        }
        this.lastReturnedNum += 1;
        if (this.lastReturnedNum < this.searchResultMessages.size())
        {
          paramString = (MessageObject)this.searchResultMessages.get(this.lastReturnedNum);
          NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatSearchResultsAvailable, new Object[] { Integer.valueOf(paramInt1), Integer.valueOf(paramString.getId()), Integer.valueOf(getMask()), Long.valueOf(paramString.getDialogId()), Integer.valueOf(this.lastReturnedNum), Integer.valueOf(this.messagesSearchCount[0] + this.messagesSearchCount[1]) });
          return;
        }
        if ((this.messagesSearchEndReached[0] != 0) && (paramLong2 == 0L) && (this.messagesSearchEndReached[1] != 0))
        {
          this.lastReturnedNum -= 1;
          return;
        }
        k = 0;
        localObject1 = this.lastSearchQuery;
        paramString = (MessageObject)this.searchResultMessages.get(this.searchResultMessages.size() - 1);
        if ((paramString.getDialogId() != paramLong1) || (this.messagesSearchEndReached[0] != 0)) {
          break label508;
        }
        j = paramString.getId();
        l1 = paramLong1;
        l2 = l1;
        if (this.messagesSearchEndReached[0] != 0)
        {
          l2 = l1;
          if (this.messagesSearchEndReached[1] == 0)
          {
            l2 = l1;
            if (paramLong2 != 0L) {
              l2 = paramLong2;
            }
          }
        }
        if ((l2 != paramLong1) || (k == 0)) {
          break label846;
        }
        if (paramLong2 == 0L) {
          break label827;
        }
        localObject2 = MessagesController.getInstance(this.currentAccount).getInputPeer((int)paramLong2);
      } while (localObject2 == null);
      paramString = new TLRPC.TL_messages_search();
      paramString.peer = ((TLRPC.InputPeer)localObject2);
      this.lastMergeDialogId = paramLong2;
      paramString.limit = 1;
      if (localObject1 != null) {}
      for (;;)
      {
        paramString.q = ((String)localObject1);
        if (paramUser != null)
        {
          paramString.from_id = MessagesController.getInstance(this.currentAccount).getInputUser(paramUser);
          paramString.flags |= 0x1;
        }
        paramString.filter = new TLRPC.TL_inputMessagesFilterEmpty();
        this.mergeReqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramString, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                TLRPC.messages_Messages localmessages_Messages;
                int[] arrayOfInt;
                if (DataQuery.this.lastMergeDialogId == DataQuery.31.this.val$mergeDialogId)
                {
                  DataQuery.access$3802(DataQuery.this, 0);
                  if (paramAnonymousTLObject != null)
                  {
                    localmessages_Messages = (TLRPC.messages_Messages)paramAnonymousTLObject;
                    DataQuery.this.messagesSearchEndReached[1] = localmessages_Messages.messages.isEmpty();
                    arrayOfInt = DataQuery.this.messagesSearchCount;
                    if (!(localmessages_Messages instanceof TLRPC.TL_messages_messagesSlice)) {
                      break label151;
                    }
                  }
                }
                label151:
                for (int i = localmessages_Messages.count;; i = localmessages_Messages.messages.size())
                {
                  arrayOfInt[1] = i;
                  DataQuery.this.searchMessagesInChat(DataQuery.31.this.val$req.q, DataQuery.31.this.val$dialog_id, DataQuery.31.this.val$mergeDialogId, DataQuery.31.this.val$guid, DataQuery.31.this.val$direction, true, DataQuery.31.this.val$user);
                  return;
                }
              }
            });
          }
        }, 2);
        return;
        if (paramString.getDialogId() == paramLong2) {
          j = paramString.getId();
        }
        l1 = paramLong2;
        this.messagesSearchEndReached[1] = false;
        break label315;
        if (paramInt2 != 2) {
          break;
        }
        this.lastReturnedNum -= 1;
        if (this.lastReturnedNum < 0)
        {
          this.lastReturnedNum = 0;
          return;
        }
        if (this.lastReturnedNum >= this.searchResultMessages.size()) {
          this.lastReturnedNum = (this.searchResultMessages.size() - 1);
        }
        paramString = (MessageObject)this.searchResultMessages.get(this.lastReturnedNum);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatSearchResultsAvailable, new Object[] { Integer.valueOf(paramInt1), Integer.valueOf(paramString.getId()), Integer.valueOf(getMask()), Long.valueOf(paramString.getDialogId()), Integer.valueOf(this.lastReturnedNum), Integer.valueOf(this.messagesSearchCount[0] + this.messagesSearchCount[1]) });
        return;
        k = i;
        j = m;
        l1 = l2;
        localObject1 = paramString;
        if (i == 0) {
          break label315;
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatSearchResultsLoading, new Object[] { Integer.valueOf(paramInt1) });
        localObject1 = this.messagesSearchEndReached;
        this.messagesSearchEndReached[1] = false;
        localObject1[0] = 0;
        localObject1 = this.messagesSearchCount;
        this.messagesSearchCount[1] = 0;
        localObject1[0] = 0;
        this.searchResultMessages.clear();
        this.searchResultMessagesMap[0].clear();
        this.searchResultMessagesMap[1].clear();
        k = i;
        j = m;
        l1 = l2;
        localObject1 = paramString;
        break label315;
        localObject1 = "";
      }
      this.lastMergeDialogId = 0L;
      this.messagesSearchEndReached[1] = true;
      this.messagesSearchCount[1] = 0;
      localObject2 = new TLRPC.TL_messages_search();
      ((TLRPC.TL_messages_search)localObject2).peer = MessagesController.getInstance(this.currentAccount).getInputPeer((int)l2);
    } while (((TLRPC.TL_messages_search)localObject2).peer == null);
    ((TLRPC.TL_messages_search)localObject2).limit = 21;
    if (localObject1 != null) {}
    for (paramString = (String)localObject1;; paramString = "")
    {
      ((TLRPC.TL_messages_search)localObject2).q = paramString;
      ((TLRPC.TL_messages_search)localObject2).offset_id = j;
      if (paramUser != null)
      {
        ((TLRPC.TL_messages_search)localObject2).from_id = MessagesController.getInstance(this.currentAccount).getInputUser(paramUser);
        ((TLRPC.TL_messages_search)localObject2).flags |= 0x1;
      }
      ((TLRPC.TL_messages_search)localObject2).filter = new TLRPC.TL_inputMessagesFilterEmpty();
      paramInt2 = this.lastReqId + 1;
      this.lastReqId = paramInt2;
      this.lastSearchQuery = ((String)localObject1);
      this.reqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject2, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              Object localObject1;
              int i;
              int j;
              int k;
              int m;
              if (DataQuery.32.this.val$currentReqId == DataQuery.this.lastReqId)
              {
                DataQuery.access$4302(DataQuery.this, 0);
                if (paramAnonymousTLObject != null)
                {
                  localObject1 = (TLRPC.messages_Messages)paramAnonymousTLObject;
                  for (i = 0; i < ((TLRPC.messages_Messages)localObject1).messages.size(); i = j + 1)
                  {
                    localObject2 = (TLRPC.Message)((TLRPC.messages_Messages)localObject1).messages.get(i);
                    if (!(localObject2 instanceof TLRPC.TL_messageEmpty))
                    {
                      j = i;
                      if (!(((TLRPC.Message)localObject2).action instanceof TLRPC.TL_messageActionHistoryClear)) {}
                    }
                    else
                    {
                      ((TLRPC.messages_Messages)localObject1).messages.remove(i);
                      j = i - 1;
                    }
                  }
                  MessagesStorage.getInstance(DataQuery.this.currentAccount).putUsersAndChats(((TLRPC.messages_Messages)localObject1).users, ((TLRPC.messages_Messages)localObject1).chats, true, true);
                  MessagesController.getInstance(DataQuery.this.currentAccount).putUsers(((TLRPC.messages_Messages)localObject1).users, false);
                  MessagesController.getInstance(DataQuery.this.currentAccount).putChats(((TLRPC.messages_Messages)localObject1).chats, false);
                  if ((DataQuery.32.this.val$req.offset_id == 0) && (DataQuery.32.this.val$queryWithDialogFinal == DataQuery.32.this.val$dialog_id))
                  {
                    DataQuery.access$4402(DataQuery.this, 0);
                    DataQuery.this.searchResultMessages.clear();
                    DataQuery.this.searchResultMessagesMap[0].clear();
                    DataQuery.this.searchResultMessagesMap[1].clear();
                    DataQuery.this.messagesSearchCount[0] = 0;
                  }
                  i = 0;
                  j = 0;
                  if (j < Math.min(((TLRPC.messages_Messages)localObject1).messages.size(), 20))
                  {
                    localObject2 = (TLRPC.Message)((TLRPC.messages_Messages)localObject1).messages.get(j);
                    k = 1;
                    localObject2 = new MessageObject(DataQuery.this.currentAccount, (TLRPC.Message)localObject2, false);
                    DataQuery.this.searchResultMessages.add(localObject2);
                    SparseArray[] arrayOfSparseArray = DataQuery.this.searchResultMessagesMap;
                    if (DataQuery.32.this.val$queryWithDialogFinal == DataQuery.32.this.val$dialog_id) {}
                    for (i = 0;; i = 1)
                    {
                      arrayOfSparseArray[i].put(((MessageObject)localObject2).getId(), localObject2);
                      j += 1;
                      i = k;
                      break;
                    }
                  }
                  Object localObject2 = DataQuery.this.messagesSearchEndReached;
                  if (DataQuery.32.this.val$queryWithDialogFinal != DataQuery.32.this.val$dialog_id) {
                    break label745;
                  }
                  j = 0;
                  if (((TLRPC.messages_Messages)localObject1).messages.size() == 21) {
                    break label750;
                  }
                  m = 1;
                  label472:
                  localObject2[j] = m;
                  localObject2 = DataQuery.this.messagesSearchCount;
                  if (DataQuery.32.this.val$queryWithDialogFinal != DataQuery.32.this.val$dialog_id) {
                    break label756;
                  }
                  j = 0;
                  label510:
                  if ((!(localObject1 instanceof TLRPC.TL_messages_messagesSlice)) && (!(localObject1 instanceof TLRPC.TL_messages_channelMessages))) {
                    break label761;
                  }
                  k = ((TLRPC.messages_Messages)localObject1).count;
                  label532:
                  localObject2[j] = k;
                  if (!DataQuery.this.searchResultMessages.isEmpty()) {
                    break label773;
                  }
                  NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.chatSearchResultsAvailable, new Object[] { Integer.valueOf(DataQuery.32.this.val$guid), Integer.valueOf(0), Integer.valueOf(DataQuery.this.getMask()), Long.valueOf(0L), Integer.valueOf(0), Integer.valueOf(0) });
                }
              }
              for (;;)
              {
                if ((DataQuery.32.this.val$queryWithDialogFinal == DataQuery.32.this.val$dialog_id) && (DataQuery.this.messagesSearchEndReached[0] != 0) && (DataQuery.32.this.val$mergeDialogId != 0L) && (DataQuery.this.messagesSearchEndReached[1] == 0)) {
                  DataQuery.this.searchMessagesInChat(DataQuery.this.lastSearchQuery, DataQuery.32.this.val$dialog_id, DataQuery.32.this.val$mergeDialogId, DataQuery.32.this.val$guid, 0, true, DataQuery.32.this.val$user);
                }
                return;
                label745:
                j = 1;
                break;
                label750:
                m = 0;
                break label472;
                label756:
                j = 1;
                break label510;
                label761:
                k = ((TLRPC.messages_Messages)localObject1).messages.size();
                break label532;
                label773:
                if (i != 0)
                {
                  if (DataQuery.this.lastReturnedNum >= DataQuery.this.searchResultMessages.size()) {
                    DataQuery.access$4402(DataQuery.this, DataQuery.this.searchResultMessages.size() - 1);
                  }
                  localObject1 = (MessageObject)DataQuery.this.searchResultMessages.get(DataQuery.this.lastReturnedNum);
                  NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.chatSearchResultsAvailable, new Object[] { Integer.valueOf(DataQuery.32.this.val$guid), Integer.valueOf(((MessageObject)localObject1).getId()), Integer.valueOf(DataQuery.this.getMask()), Long.valueOf(((MessageObject)localObject1).getDialogId()), Integer.valueOf(DataQuery.this.lastReturnedNum), Integer.valueOf(DataQuery.this.messagesSearchCount[0] + DataQuery.this.messagesSearchCount[1]) });
                }
              }
            }
          });
        }
      }, 2);
      return;
    }
  }
  
  public static void sortEntities(ArrayList<TLRPC.MessageEntity> paramArrayList)
  {
    Collections.sort(paramArrayList, entityComparator);
  }
  
  public void addNewStickerSet(TLRPC.TL_messages_stickerSet paramTL_messages_stickerSet)
  {
    if ((this.stickerSetsById.indexOfKey(paramTL_messages_stickerSet.set.id) >= 0) || (this.stickerSetsByName.containsKey(paramTL_messages_stickerSet.set.short_name))) {
      return;
    }
    if (paramTL_messages_stickerSet.set.masks) {}
    LongSparseArray localLongSparseArray;
    Object localObject1;
    for (int i = 1;; i = 0)
    {
      this.stickerSets[i].add(0, paramTL_messages_stickerSet);
      this.stickerSetsById.put(paramTL_messages_stickerSet.set.id, paramTL_messages_stickerSet);
      this.installedStickerSetsById.put(paramTL_messages_stickerSet.set.id, paramTL_messages_stickerSet);
      this.stickerSetsByName.put(paramTL_messages_stickerSet.set.short_name, paramTL_messages_stickerSet);
      localLongSparseArray = new LongSparseArray();
      j = 0;
      while (j < paramTL_messages_stickerSet.documents.size())
      {
        localObject1 = (TLRPC.Document)paramTL_messages_stickerSet.documents.get(j);
        localLongSparseArray.put(((TLRPC.Document)localObject1).id, localObject1);
        j += 1;
      }
    }
    int j = 0;
    while (j < paramTL_messages_stickerSet.packs.size())
    {
      TLRPC.TL_stickerPack localTL_stickerPack = (TLRPC.TL_stickerPack)paramTL_messages_stickerSet.packs.get(j);
      localTL_stickerPack.emoticon = localTL_stickerPack.emoticon.replace("️", "");
      Object localObject2 = (ArrayList)this.allStickers.get(localTL_stickerPack.emoticon);
      localObject1 = localObject2;
      if (localObject2 == null)
      {
        localObject1 = new ArrayList();
        this.allStickers.put(localTL_stickerPack.emoticon, localObject1);
      }
      int k = 0;
      while (k < localTL_stickerPack.documents.size())
      {
        localObject2 = (Long)localTL_stickerPack.documents.get(k);
        if (this.stickersByEmoji.indexOfKey(((Long)localObject2).longValue()) < 0) {
          this.stickersByEmoji.put(((Long)localObject2).longValue(), localTL_stickerPack.emoticon);
        }
        localObject2 = (TLRPC.Document)localLongSparseArray.get(((Long)localObject2).longValue());
        if (localObject2 != null) {
          ((ArrayList)localObject1).add(localObject2);
        }
        k += 1;
      }
      j += 1;
    }
    this.loadHash[i] = calcStickersHash(this.stickerSets[i]);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.stickersDidLoaded, new Object[] { Integer.valueOf(i) });
    loadStickers(i, false, true);
  }
  
  public void addRecentGif(TLRPC.Document paramDocument, int paramInt)
  {
    int j = 0;
    int i = 0;
    while (i < this.recentGifs.size())
    {
      localObject = (TLRPC.Document)this.recentGifs.get(i);
      if (((TLRPC.Document)localObject).id == paramDocument.id)
      {
        this.recentGifs.remove(i);
        this.recentGifs.add(0, localObject);
        j = 1;
      }
      i += 1;
    }
    if (j == 0) {
      this.recentGifs.add(0, paramDocument);
    }
    if (this.recentGifs.size() > MessagesController.getInstance(this.currentAccount).maxRecentGifsCount)
    {
      localObject = (TLRPC.Document)this.recentGifs.remove(this.recentGifs.size() - 1);
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          try
          {
            MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("DELETE FROM web_recent_v3 WHERE id = '" + localObject.id + "' AND type = 2").stepThis().dispose();
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
          }
        }
      });
    }
    final Object localObject = new ArrayList();
    ((ArrayList)localObject).add(paramDocument);
    processLoadedRecentDocuments(0, (ArrayList)localObject, true, paramInt);
  }
  
  public void addRecentSticker(final int paramInt1, TLRPC.Document paramDocument, int paramInt2, boolean paramBoolean)
  {
    int j = 0;
    int i = 0;
    while (i < this.recentStickers[paramInt1].size())
    {
      localObject = (TLRPC.Document)this.recentStickers[paramInt1].get(i);
      if (((TLRPC.Document)localObject).id == paramDocument.id)
      {
        this.recentStickers[paramInt1].remove(i);
        if (!paramBoolean) {
          this.recentStickers[paramInt1].add(0, localObject);
        }
        j = 1;
      }
      i += 1;
    }
    if ((j == 0) && (!paramBoolean)) {
      this.recentStickers[paramInt1].add(0, paramDocument);
    }
    if (paramInt1 == 2) {
      if (paramBoolean)
      {
        Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("RemovedFromFavorites", 2131494231), 0).show();
        localObject = new TLRPC.TL_messages_faveSticker();
        ((TLRPC.TL_messages_faveSticker)localObject).id = new TLRPC.TL_inputDocument();
        ((TLRPC.TL_messages_faveSticker)localObject).id.id = paramDocument.id;
        ((TLRPC.TL_messages_faveSticker)localObject).id.access_hash = paramDocument.access_hash;
        ((TLRPC.TL_messages_faveSticker)localObject).unfave = paramBoolean;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
        });
        i = MessagesController.getInstance(this.currentAccount).maxFaveStickersCount;
        label225:
        if ((this.recentStickers[paramInt1].size() > i) || (paramBoolean)) {
          if (!paramBoolean) {
            break label380;
          }
        }
      }
    }
    label380:
    for (final Object localObject = paramDocument;; localObject = (TLRPC.Document)this.recentStickers[paramInt1].remove(this.recentStickers[paramInt1].size() - 1))
    {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          int i;
          if (paramInt1 == 0) {
            i = 3;
          }
          for (;;)
          {
            try
            {
              MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("DELETE FROM web_recent_v3 WHERE id = '" + localObject.id + "' AND type = " + i).stepThis().dispose();
              return;
            }
            catch (Exception localException)
            {
              FileLog.e(localException);
            }
            if (paramInt1 == 1) {
              i = 4;
            } else {
              i = 5;
            }
          }
        }
      });
      if (!paramBoolean)
      {
        localObject = new ArrayList();
        ((ArrayList)localObject).add(paramDocument);
        processLoadedRecentDocuments(paramInt1, (ArrayList)localObject, false, paramInt2);
      }
      if (paramInt1 == 2) {
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.recentDocumentsDidLoaded, new Object[] { Boolean.valueOf(false), Integer.valueOf(paramInt1) });
      }
      return;
      Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("AddedToFavorites", 2131492947), 0).show();
      break;
      i = MessagesController.getInstance(this.currentAccount).maxRecentStickersCount;
      break label225;
    }
  }
  
  public void beginTransaction()
  {
    this.inTransaction = true;
  }
  
  public void buildShortcuts()
  {
    if (Build.VERSION.SDK_INT < 25) {
      return;
    }
    final ArrayList localArrayList = new ArrayList();
    int i = 0;
    for (;;)
    {
      if (i < this.hints.size())
      {
        localArrayList.add(this.hints.get(i));
        if (localArrayList.size() != 3) {}
      }
      else
      {
        Utilities.globalQueue.postRunnable(new Runnable()
        {
          @SuppressLint({"NewApi"})
          public void run()
          {
            try
            {
              ShortcutManager localShortcutManager = (ShortcutManager)ApplicationLoader.applicationContext.getSystemService(ShortcutManager.class);
              Object localObject2 = localShortcutManager.getDynamicShortcuts();
              ArrayList localArrayList1 = new ArrayList();
              Object localObject3 = new ArrayList();
              Object localObject1 = new ArrayList();
              int i;
              Object localObject4;
              long l1;
              if ((localObject2 != null) && (!((List)localObject2).isEmpty()))
              {
                ((ArrayList)localObject3).add("compose");
                i = 0;
                if (i >= localArrayList.size()) {
                  break label1168;
                }
                localObject4 = (TLRPC.TL_topPeer)localArrayList.get(i);
                if (((TLRPC.TL_topPeer)localObject4).peer.user_id != 0) {
                  l1 = ((TLRPC.TL_topPeer)localObject4).peer.user_id;
                }
                for (;;)
                {
                  ((ArrayList)localObject3).add("did" + l1);
                  i += 1;
                  break;
                  long l2 = -((TLRPC.TL_topPeer)localObject4).peer.chat_id;
                  l1 = l2;
                  if (l2 == 0L) {
                    l1 = -((TLRPC.TL_topPeer)localObject4).peer.channel_id;
                  }
                }
                while (i < ((List)localObject2).size())
                {
                  localObject4 = ((ShortcutInfo)((List)localObject2).get(i)).getId();
                  if (!((ArrayList)localObject3).remove(localObject4)) {
                    ((ArrayList)localObject1).add(localObject4);
                  }
                  localArrayList1.add(localObject4);
                  i += 1;
                }
                if ((((ArrayList)localObject3).isEmpty()) && (((ArrayList)localObject1).isEmpty())) {
                  return;
                }
              }
              localObject2 = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
              ((Intent)localObject2).setAction("new_dialog");
              ArrayList localArrayList2 = new ArrayList();
              localArrayList2.add(new ShortcutInfo.Builder(ApplicationLoader.applicationContext, "compose").setShortLabel(LocaleController.getString("NewConversationShortcut", 2131493868)).setLongLabel(LocaleController.getString("NewConversationShortcut", 2131493868)).setIcon(Icon.createWithResource(ApplicationLoader.applicationContext, 2131165641)).setIntent((Intent)localObject2).build());
              if (localArrayList1.contains("compose"))
              {
                localShortcutManager.updateShortcuts(localArrayList2);
                label372:
                localArrayList2.clear();
                if (((ArrayList)localObject1).isEmpty()) {
                  break label1174;
                }
                localShortcutManager.removeDynamicShortcuts((List)localObject1);
              }
              for (;;)
              {
                label395:
                Intent localIntent;
                int j;
                if (i < localArrayList.size())
                {
                  localIntent = new Intent(ApplicationLoader.applicationContext, OpenChatReceiver.class);
                  localObject2 = (TLRPC.TL_topPeer)localArrayList.get(i);
                  localObject4 = null;
                  localObject1 = null;
                  if (((TLRPC.TL_topPeer)localObject2).peer.user_id != 0)
                  {
                    localIntent.putExtra("userId", ((TLRPC.TL_topPeer)localObject2).peer.user_id);
                    localObject4 = MessagesController.getInstance(DataQuery.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_topPeer)localObject2).peer.user_id));
                    l1 = ((TLRPC.TL_topPeer)localObject2).peer.user_id;
                    break label1179;
                    localShortcutManager.addDynamicShortcuts(localArrayList2);
                    break label372;
                  }
                  int k = ((TLRPC.TL_topPeer)localObject2).peer.chat_id;
                  j = k;
                  if (k == 0) {
                    j = ((TLRPC.TL_topPeer)localObject2).peer.channel_id;
                  }
                  localObject1 = MessagesController.getInstance(DataQuery.this.currentAccount).getChat(Integer.valueOf(j));
                  localIntent.putExtra("chatId", j);
                  l1 = -j;
                }
                label967:
                label1051:
                label1152:
                label1168:
                label1174:
                label1179:
                while ((localObject4 != null) || (localException != null))
                {
                  localObject3 = null;
                  if (localObject4 != null)
                  {
                    localObject1 = ContactsController.formatName(((TLRPC.User)localObject4).first_name, ((TLRPC.User)localObject4).last_name);
                    localObject2 = localObject1;
                    if (((TLRPC.User)localObject4).photo != null)
                    {
                      localObject3 = ((TLRPC.User)localObject4).photo.photo_small;
                      localObject2 = localObject1;
                    }
                  }
                  for (;;)
                  {
                    localIntent.putExtra("currentAccount", DataQuery.this.currentAccount);
                    localIntent.setAction("com.tmessages.openchat" + l1);
                    localIntent.addFlags(67108864);
                    localObject4 = null;
                    localObject1 = null;
                    if (localObject3 != null) {
                      localObject1 = localObject4;
                    }
                    try
                    {
                      localObject3 = BitmapFactory.decodeFile(FileLoader.getPathToAttach((TLObject)localObject3, true).toString());
                      localObject1 = localObject3;
                      if (localObject3 != null)
                      {
                        localObject1 = localObject3;
                        j = AndroidUtilities.dp(48.0F);
                        localObject1 = localObject3;
                        localObject4 = Bitmap.createBitmap(j, j, Bitmap.Config.ARGB_8888);
                        localObject1 = localObject3;
                        localCanvas = new Canvas((Bitmap)localObject4);
                        localObject1 = localObject3;
                        if (DataQuery.roundPaint == null)
                        {
                          localObject1 = localObject3;
                          DataQuery.access$5202(new Paint(3));
                          localObject1 = localObject3;
                          DataQuery.access$5302(new RectF());
                          localObject1 = localObject3;
                          DataQuery.access$5402(new Paint(1));
                          localObject1 = localObject3;
                          DataQuery.erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                          localObject1 = localObject3;
                          DataQuery.access$5502(new Path());
                          localObject1 = localObject3;
                          DataQuery.roundPath.addCircle(j / 2, j / 2, j / 2 - AndroidUtilities.dp(2.0F), Path.Direction.CW);
                          localObject1 = localObject3;
                          DataQuery.roundPath.toggleInverseFillType();
                        }
                        localObject1 = localObject3;
                        DataQuery.bitmapRect.set(AndroidUtilities.dp(2.0F), AndroidUtilities.dp(2.0F), AndroidUtilities.dp(46.0F), AndroidUtilities.dp(46.0F));
                        localObject1 = localObject3;
                        localCanvas.drawBitmap((Bitmap)localObject3, null, DataQuery.bitmapRect, DataQuery.roundPaint);
                        localObject1 = localObject3;
                        localCanvas.drawPath(DataQuery.roundPath, DataQuery.erasePaint);
                        localObject1 = localObject3;
                      }
                    }
                    catch (Throwable localThrowable2)
                    {
                      try
                      {
                        Canvas localCanvas;
                        localCanvas.setBitmap(null);
                        localObject1 = localObject4;
                        localObject4 = "did" + l1;
                        localObject3 = localObject2;
                        if (TextUtils.isEmpty((CharSequence)localObject2)) {
                          localObject3 = " ";
                        }
                        localObject2 = new ShortcutInfo.Builder(ApplicationLoader.applicationContext, (String)localObject4).setShortLabel((CharSequence)localObject3).setLongLabel((CharSequence)localObject3).setIntent(localIntent);
                        if (localObject1 != null)
                        {
                          ((ShortcutInfo.Builder)localObject2).setIcon(Icon.createWithBitmap((Bitmap)localObject1));
                          localArrayList2.add(((ShortcutInfo.Builder)localObject2).build());
                          if (!localArrayList1.contains(localObject4)) {
                            break label1152;
                          }
                          localShortcutManager.updateShortcuts(localArrayList2);
                        }
                        for (;;)
                        {
                          localArrayList2.clear();
                          break label1189;
                          localObject4 = ((TLRPC.Chat)localObject1).title;
                          localObject2 = localObject4;
                          if (((TLRPC.Chat)localObject1).photo == null) {
                            break;
                          }
                          localObject3 = ((TLRPC.Chat)localObject1).photo.photo_small;
                          localObject2 = localObject4;
                          break;
                          localThrowable2 = localThrowable2;
                          FileLog.e(localThrowable2);
                          break label967;
                          ((ShortcutInfo.Builder)localObject2).setIcon(Icon.createWithResource(ApplicationLoader.applicationContext, 2131165642));
                          break label1051;
                          localShortcutManager.addDynamicShortcuts(localArrayList2);
                        }
                      }
                      catch (Exception localException)
                      {
                        for (;;) {}
                      }
                    }
                  }
                  i = 0;
                  break;
                  return;
                  i = 0;
                  break label395;
                }
                label1189:
                i += 1;
              }
              return;
            }
            catch (Throwable localThrowable1) {}
          }
        });
        return;
      }
      i += 1;
    }
  }
  
  public void calcNewHash(int paramInt)
  {
    this.loadHash[paramInt] = calcStickersHash(this.stickerSets[paramInt]);
  }
  
  public boolean canAddStickerToFavorites()
  {
    boolean bool = false;
    if ((this.stickersLoaded[0] == 0) || (this.stickerSets[0].size() >= 5) || (!this.recentStickers[2].isEmpty())) {
      bool = true;
    }
    return bool;
  }
  
  public void checkFeaturedStickers()
  {
    if ((!this.loadingFeaturedStickers) && ((!this.featuredStickersLoaded) || (Math.abs(System.currentTimeMillis() / 1000L - this.loadFeaturedDate) >= 3600L))) {
      loadFeaturedStickers(true, false);
    }
  }
  
  public void checkStickers(int paramInt)
  {
    if ((this.loadingStickers[paramInt] == 0) && ((this.stickersLoaded[paramInt] == 0) || (Math.abs(System.currentTimeMillis() / 1000L - this.loadDate[paramInt]) >= 3600L))) {
      loadStickers(paramInt, true, false);
    }
  }
  
  public void cleanDraft(long paramLong, boolean paramBoolean)
  {
    TLRPC.DraftMessage localDraftMessage = (TLRPC.DraftMessage)this.drafts.get(paramLong);
    if (localDraftMessage == null) {}
    do
    {
      return;
      if (!paramBoolean)
      {
        this.drafts.remove(paramLong);
        this.draftMessages.remove(paramLong);
        this.preferences.edit().remove("" + paramLong).remove("r_" + paramLong).commit();
        MessagesController.getInstance(this.currentAccount).sortDialogs(null);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
        return;
      }
    } while (localDraftMessage.reply_to_msg_id == 0);
    localDraftMessage.reply_to_msg_id = 0;
    localDraftMessage.flags &= 0xFFFFFFFE;
    saveDraft(paramLong, localDraftMessage.message, localDraftMessage.entities, null, localDraftMessage.no_webpage, true);
  }
  
  public void cleanup()
  {
    int i = 0;
    while (i < 3)
    {
      this.recentStickers[i].clear();
      this.loadingRecentStickers[i] = false;
      this.recentStickersLoaded[i] = false;
      i += 1;
    }
    i = 0;
    while (i < 4)
    {
      this.loadHash[i] = 0;
      this.loadDate[i] = 0;
      this.stickerSets[i].clear();
      this.loadingStickers[i] = false;
      this.stickersLoaded[i] = false;
      i += 1;
    }
    this.featuredStickerSets.clear();
    this.loadFeaturedDate = 0;
    this.loadFeaturedHash = 0;
    this.allStickers.clear();
    this.allStickersFeatured.clear();
    this.stickersByEmoji.clear();
    this.featuredStickerSetsById.clear();
    this.featuredStickerSets.clear();
    this.unreadStickerSets.clear();
    this.recentGifs.clear();
    this.stickerSetsById.clear();
    this.installedStickerSetsById.clear();
    this.stickerSetsByName.clear();
    this.loadingFeaturedStickers = false;
    this.featuredStickersLoaded = false;
    this.loadingRecentGifs = false;
    this.recentGifsLoaded = false;
    this.loading = false;
    this.loaded = false;
    this.hints.clear();
    this.inlineBots.clear();
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.reloadHints, new Object[0]);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.reloadInlineHints, new Object[0]);
    this.drafts.clear();
    this.draftMessages.clear();
    this.preferences.edit().clear().commit();
    this.botInfos.clear();
    this.botKeyboards.clear();
    this.botKeyboardsByMids.clear();
  }
  
  public void clearBotKeyboard(final long paramLong, final ArrayList<Integer> paramArrayList)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (paramArrayList != null)
        {
          int i = 0;
          while (i < paramArrayList.size())
          {
            long l = DataQuery.this.botKeyboardsByMids.get(((Integer)paramArrayList.get(i)).intValue());
            if (l != 0L)
            {
              DataQuery.this.botKeyboards.remove(l);
              DataQuery.this.botKeyboardsByMids.delete(((Integer)paramArrayList.get(i)).intValue());
              NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.botKeyboardDidLoaded, new Object[] { null, Long.valueOf(l) });
            }
            i += 1;
          }
        }
        DataQuery.this.botKeyboards.remove(paramLong);
        NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.botKeyboardDidLoaded, new Object[] { null, Long.valueOf(paramLong) });
      }
    });
  }
  
  public void endTransaction()
  {
    this.inTransaction = false;
  }
  
  public HashMap<String, ArrayList<TLRPC.Document>> getAllStickers()
  {
    return this.allStickers;
  }
  
  public HashMap<String, ArrayList<TLRPC.Document>> getAllStickersFeatured()
  {
    return this.allStickersFeatured;
  }
  
  public int getArchivedStickersCount(int paramInt)
  {
    return this.archivedStickersCount[paramInt];
  }
  
  public TLRPC.DraftMessage getDraft(long paramLong)
  {
    return (TLRPC.DraftMessage)this.drafts.get(paramLong);
  }
  
  public TLRPC.Message getDraftMessage(long paramLong)
  {
    return (TLRPC.Message)this.draftMessages.get(paramLong);
  }
  
  public String getEmojiForSticker(long paramLong)
  {
    String str = (String)this.stickersByEmoji.get(paramLong);
    if (str != null) {
      return str;
    }
    return "";
  }
  
  public ArrayList<TLRPC.MessageEntity> getEntities(CharSequence[] paramArrayOfCharSequence)
  {
    if ((paramArrayOfCharSequence == null) || (paramArrayOfCharSequence[0] == null))
    {
      localObject1 = null;
      return (ArrayList<TLRPC.MessageEntity>)localObject1;
    }
    Object localObject2 = null;
    int j = -1;
    int i = 0;
    int k = 0;
    Object localObject3 = paramArrayOfCharSequence[0];
    label41:
    int m;
    if (k == 0)
    {
      localObject1 = "`";
      m = TextUtils.indexOf((CharSequence)localObject3, (CharSequence)localObject1, i);
      if (m == -1) {
        break label722;
      }
      if (j != -1) {
        break label151;
      }
      if ((paramArrayOfCharSequence[0].length() - m <= 2) || (paramArrayOfCharSequence[0].charAt(m + 1) != '`') || (paramArrayOfCharSequence[0].charAt(m + 2) != '`')) {
        break label140;
      }
      k = 1;
      label114:
      j = m;
      if (k == 0) {
        break label146;
      }
    }
    label140:
    label146:
    for (i = 3;; i = 1)
    {
      i = m + i;
      break;
      localObject1 = "```";
      break label41;
      k = 0;
      break label114;
    }
    label151:
    Object localObject1 = localObject2;
    if (localObject2 == null) {
      localObject1 = new ArrayList();
    }
    if (k != 0) {}
    for (i = 3;; i = 1)
    {
      i = m + i;
      while ((i < paramArrayOfCharSequence[0].length()) && (paramArrayOfCharSequence[0].charAt(i) == '`'))
      {
        m += 1;
        i += 1;
      }
    }
    label232:
    int n;
    label259:
    label273:
    label285:
    Object localObject5;
    label339:
    label361:
    Object localObject4;
    if (k != 0)
    {
      i = 3;
      n = m + i;
      if (k == 0) {
        break label613;
      }
      if (j <= 0) {
        break label569;
      }
      i = paramArrayOfCharSequence[0].charAt(j - 1);
      if ((i != 32) && (i != 10)) {
        break label574;
      }
      i = 1;
      localObject2 = paramArrayOfCharSequence[0];
      if (i == 0) {
        break label579;
      }
      k = 1;
      localObject2 = TextUtils.substring((CharSequence)localObject2, 0, j - k);
      localObject5 = TextUtils.substring(paramArrayOfCharSequence[0], j + 3, m);
      if (m + 3 >= paramArrayOfCharSequence[0].length()) {
        break label585;
      }
      k = paramArrayOfCharSequence[0].charAt(m + 3);
      localObject3 = paramArrayOfCharSequence[0];
      if ((k != 32) && (k != 10)) {
        break label591;
      }
      k = 1;
      localObject4 = TextUtils.substring((CharSequence)localObject3, k + (m + 3), paramArrayOfCharSequence[0].length());
      if (((CharSequence)localObject2).length() == 0) {
        break label597;
      }
      localObject2 = TextUtils.concat(new CharSequence[] { localObject2, "\n" });
      k = i;
      label416:
      localObject3 = localObject4;
      if (((CharSequence)localObject4).length() != 0) {
        localObject3 = TextUtils.concat(new CharSequence[] { "\n", localObject4 });
      }
      i = n;
      if (!TextUtils.isEmpty((CharSequence)localObject5))
      {
        paramArrayOfCharSequence[0] = TextUtils.concat(new CharSequence[] { localObject2, localObject5, localObject3 });
        localObject2 = new TLRPC.TL_messageEntityPre();
        if (k == 0) {
          break label603;
        }
        i = 0;
        label502:
        ((TLRPC.TL_messageEntityPre)localObject2).offset = (i + j);
        if (k == 0) {
          break label608;
        }
        i = 0;
        label517:
        ((TLRPC.TL_messageEntityPre)localObject2).length = (i + (m - j - 3));
        ((TLRPC.TL_messageEntityPre)localObject2).language = "";
        ((ArrayList)localObject1).add(localObject2);
        i = n - 6;
      }
    }
    for (;;)
    {
      j = -1;
      k = 0;
      localObject2 = localObject1;
      break;
      i = 1;
      break label232;
      label569:
      i = 0;
      break label259;
      label574:
      i = 0;
      break label273;
      label579:
      k = 0;
      break label285;
      label585:
      k = 0;
      break label339;
      label591:
      k = 0;
      break label361;
      label597:
      k = 1;
      break label416;
      label603:
      i = 1;
      break label502;
      label608:
      i = 1;
      break label517;
      label613:
      i = n;
      if (j + 1 != m)
      {
        paramArrayOfCharSequence[0] = TextUtils.concat(new CharSequence[] { TextUtils.substring(paramArrayOfCharSequence[0], 0, j), TextUtils.substring(paramArrayOfCharSequence[0], j + 1, m), TextUtils.substring(paramArrayOfCharSequence[0], m + 1, paramArrayOfCharSequence[0].length()) });
        localObject2 = new TLRPC.TL_messageEntityCode();
        ((TLRPC.TL_messageEntityCode)localObject2).offset = j;
        ((TLRPC.TL_messageEntityCode)localObject2).length = (m - j - 1);
        ((ArrayList)localObject1).add(localObject2);
        i = n - 2;
      }
    }
    label722:
    localObject1 = localObject2;
    if (j != -1)
    {
      localObject1 = localObject2;
      if (k != 0)
      {
        paramArrayOfCharSequence[0] = TextUtils.concat(new CharSequence[] { TextUtils.substring(paramArrayOfCharSequence[0], 0, j), TextUtils.substring(paramArrayOfCharSequence[0], j + 2, paramArrayOfCharSequence[0].length()) });
        localObject1 = localObject2;
        if (localObject2 == null) {
          localObject1 = new ArrayList();
        }
        localObject2 = new TLRPC.TL_messageEntityCode();
        ((TLRPC.TL_messageEntityCode)localObject2).offset = j;
        ((TLRPC.TL_messageEntityCode)localObject2).length = 1;
        ((ArrayList)localObject1).add(localObject2);
      }
    }
    localObject3 = localObject1;
    if ((paramArrayOfCharSequence[0] instanceof Spannable))
    {
      localObject4 = (Spannable)paramArrayOfCharSequence[0];
      localObject3 = (TypefaceSpan[])((Spannable)localObject4).getSpans(0, paramArrayOfCharSequence[0].length(), TypefaceSpan.class);
      localObject2 = localObject1;
      if (localObject3 != null)
      {
        localObject2 = localObject1;
        if (localObject3.length > 0)
        {
          i = 0;
          for (;;)
          {
            localObject2 = localObject1;
            if (i >= localObject3.length) {
              break label1058;
            }
            localObject5 = localObject3[i];
            j = ((Spannable)localObject4).getSpanStart(localObject5);
            k = ((Spannable)localObject4).getSpanEnd(localObject5);
            localObject2 = localObject1;
            if (!checkInclusion(j, (ArrayList)localObject1))
            {
              localObject2 = localObject1;
              if (!checkInclusion(k, (ArrayList)localObject1))
              {
                if (!checkIntersection(j, k, (ArrayList)localObject1)) {
                  break;
                }
                localObject2 = localObject1;
              }
            }
            i += 1;
            localObject1 = localObject2;
          }
          localObject2 = localObject1;
          if (localObject1 == null) {
            localObject2 = new ArrayList();
          }
          if (((TypefaceSpan)localObject5).isBold()) {}
          for (localObject1 = new TLRPC.TL_messageEntityBold();; localObject1 = new TLRPC.TL_messageEntityItalic())
          {
            ((TLRPC.MessageEntity)localObject1).offset = j;
            ((TLRPC.MessageEntity)localObject1).length = (k - j);
            ((ArrayList)localObject2).add(localObject1);
            break;
          }
        }
      }
      label1058:
      localObject5 = (URLSpanUserMention[])((Spannable)localObject4).getSpans(0, paramArrayOfCharSequence[0].length(), URLSpanUserMention.class);
      localObject3 = localObject2;
      if (localObject5 != null)
      {
        localObject3 = localObject2;
        if (localObject5.length > 0)
        {
          localObject1 = localObject2;
          if (localObject2 == null) {
            localObject1 = new ArrayList();
          }
          i = 0;
          for (;;)
          {
            localObject3 = localObject1;
            if (i >= localObject5.length) {
              break;
            }
            localObject2 = new TLRPC.TL_inputMessageEntityMentionName();
            ((TLRPC.TL_inputMessageEntityMentionName)localObject2).user_id = MessagesController.getInstance(this.currentAccount).getInputUser(Utilities.parseInt(localObject5[i].getURL()).intValue());
            if (((TLRPC.TL_inputMessageEntityMentionName)localObject2).user_id != null)
            {
              ((TLRPC.TL_inputMessageEntityMentionName)localObject2).offset = ((Spannable)localObject4).getSpanStart(localObject5[i]);
              ((TLRPC.TL_inputMessageEntityMentionName)localObject2).length = (Math.min(((Spannable)localObject4).getSpanEnd(localObject5[i]), paramArrayOfCharSequence[0].length()) - ((TLRPC.TL_inputMessageEntityMentionName)localObject2).offset);
              if (paramArrayOfCharSequence[0].charAt(((TLRPC.TL_inputMessageEntityMentionName)localObject2).offset + ((TLRPC.TL_inputMessageEntityMentionName)localObject2).length - 1) == ' ') {
                ((TLRPC.TL_inputMessageEntityMentionName)localObject2).length -= 1;
              }
              ((ArrayList)localObject1).add(localObject2);
            }
            i += 1;
          }
        }
      }
    }
    k = 0;
    for (;;)
    {
      localObject1 = localObject3;
      if (k >= 2) {
        break;
      }
      j = 0;
      i = -1;
      if (k == 0)
      {
        localObject2 = "**";
        if (k != 0) {
          break label1393;
        }
        m = 42;
      }
      label1315:
      int i1;
      for (;;)
      {
        j = TextUtils.indexOf(paramArrayOfCharSequence[0], (CharSequence)localObject2, j);
        if (j == -1) {
          break label1672;
        }
        if (i == -1)
        {
          if (j == 0) {}
          for (i1 = 32;; i1 = paramArrayOfCharSequence[0].charAt(j - 1))
          {
            n = i;
            if (!checkInclusion(j, (ArrayList)localObject3)) {
              if (i1 != 32)
              {
                n = i;
                if (i1 != 10) {}
              }
              else
              {
                n = j;
              }
            }
            j += 2;
            i = n;
            break label1315;
            localObject2 = "__";
            break;
            label1393:
            m = 95;
            break label1315;
          }
        }
        i1 = j + 2;
        n = j;
        j = i1;
        while ((j < paramArrayOfCharSequence[0].length()) && (paramArrayOfCharSequence[0].charAt(j) == m))
        {
          n += 1;
          j += 1;
        }
        i1 = n + 2;
        if ((!checkInclusion(n, (ArrayList)localObject3)) && (!checkIntersection(i, n, (ArrayList)localObject3))) {
          break label1501;
        }
        i = -1;
        j = i1;
      }
      label1501:
      localObject1 = localObject3;
      j = i1;
      if (i + 2 != n)
      {
        localObject1 = localObject3;
        if (localObject3 == null) {
          localObject1 = new ArrayList();
        }
        paramArrayOfCharSequence[0] = TextUtils.concat(new CharSequence[] { TextUtils.substring(paramArrayOfCharSequence[0], 0, i), TextUtils.substring(paramArrayOfCharSequence[0], i + 2, n), TextUtils.substring(paramArrayOfCharSequence[0], n + 2, paramArrayOfCharSequence[0].length()) });
        if (k != 0) {
          break label1660;
        }
      }
      label1660:
      for (localObject3 = new TLRPC.TL_messageEntityBold();; localObject3 = new TLRPC.TL_messageEntityItalic())
      {
        ((TLRPC.MessageEntity)localObject3).offset = i;
        ((TLRPC.MessageEntity)localObject3).length = (n - i - 2);
        removeOffsetAfter(((TLRPC.MessageEntity)localObject3).offset + ((TLRPC.MessageEntity)localObject3).length, 4, (ArrayList)localObject1);
        ((ArrayList)localObject1).add(localObject3);
        j = i1 - 4;
        i = -1;
        localObject3 = localObject1;
        break;
      }
      label1672:
      k += 1;
    }
  }
  
  public ArrayList<TLRPC.StickerSetCovered> getFeaturedStickerSets()
  {
    return this.featuredStickerSets;
  }
  
  public int getFeaturesStickersHashWithoutUnread()
  {
    long l = 0L;
    int i = 0;
    if (i < this.featuredStickerSets.size())
    {
      TLRPC.StickerSet localStickerSet = ((TLRPC.StickerSetCovered)this.featuredStickerSets.get(i)).set;
      if (localStickerSet.archived) {}
      for (;;)
      {
        i += 1;
        break;
        int j = (int)(localStickerSet.id >> 32);
        int k = (int)localStickerSet.id;
        l = ((l * 20261L + 2147483648L + j) % 2147483648L * 20261L + 2147483648L + k) % 2147483648L;
      }
    }
    return (int)l;
  }
  
  public TLRPC.TL_messages_stickerSet getGroupStickerSetById(TLRPC.StickerSet paramStickerSet)
  {
    TLRPC.TL_messages_stickerSet localTL_messages_stickerSet2 = (TLRPC.TL_messages_stickerSet)this.stickerSetsById.get(paramStickerSet.id);
    TLRPC.TL_messages_stickerSet localTL_messages_stickerSet1 = localTL_messages_stickerSet2;
    if (localTL_messages_stickerSet2 == null)
    {
      localTL_messages_stickerSet2 = (TLRPC.TL_messages_stickerSet)this.groupStickerSets.get(paramStickerSet.id);
      if ((localTL_messages_stickerSet2 != null) && (localTL_messages_stickerSet2.set != null)) {
        break label57;
      }
      loadGroupStickerSet(paramStickerSet, true);
      localTL_messages_stickerSet1 = localTL_messages_stickerSet2;
    }
    label57:
    do
    {
      return localTL_messages_stickerSet1;
      localTL_messages_stickerSet1 = localTL_messages_stickerSet2;
    } while (localTL_messages_stickerSet2.set.hash == paramStickerSet.hash);
    loadGroupStickerSet(paramStickerSet, false);
    return localTL_messages_stickerSet2;
  }
  
  public String getLastSearchQuery()
  {
    return this.lastSearchQuery;
  }
  
  public void getMediaCount(final long paramLong, int paramInt1, final int paramInt2, boolean paramBoolean)
  {
    int i = (int)paramLong;
    if ((paramBoolean) || (i == 0))
    {
      getMediaCountDatabase(paramLong, paramInt1, paramInt2);
      return;
    }
    TLRPC.TL_messages_search localTL_messages_search = new TLRPC.TL_messages_search();
    localTL_messages_search.limit = 1;
    localTL_messages_search.offset_id = 0;
    if (paramInt1 == 0) {
      localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterPhotoVideo();
    }
    for (;;)
    {
      localTL_messages_search.q = "";
      localTL_messages_search.peer = MessagesController.getInstance(this.currentAccount).getInputPeer(i);
      if (localTL_messages_search.peer == null) {
        break;
      }
      paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_search, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.messages_Messages)paramAnonymousTLObject;
            MessagesStorage.getInstance(DataQuery.this.currentAccount).putUsersAndChats(paramAnonymousTLObject.users, paramAnonymousTLObject.chats, true, true);
            if (!(paramAnonymousTLObject instanceof TLRPC.TL_messages_messages)) {
              break label81;
            }
          }
          label81:
          for (int i = paramAnonymousTLObject.messages.size();; i = paramAnonymousTLObject.count)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.getInstance(DataQuery.this.currentAccount).putUsers(paramAnonymousTLObject.users, false);
                MessagesController.getInstance(DataQuery.this.currentAccount).putChats(paramAnonymousTLObject.chats, false);
              }
            });
            DataQuery.this.processLoadedMediaCount(i, paramLong, paramInt2, this.val$classGuid, false);
            return;
          }
        }
      });
      ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, paramInt2);
      return;
      if (paramInt1 == 1) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterDocument();
      } else if (paramInt1 == 2) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterVoice();
      } else if (paramInt1 == 3) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterUrl();
      } else if (paramInt1 == 4) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterMusic();
      }
    }
  }
  
  public ArrayList<TLRPC.Document> getRecentGifs()
  {
    return new ArrayList(this.recentGifs);
  }
  
  public ArrayList<TLRPC.Document> getRecentStickers(int paramInt)
  {
    ArrayList localArrayList = this.recentStickers[paramInt];
    return new ArrayList(localArrayList.subList(0, Math.min(localArrayList.size(), 20)));
  }
  
  public ArrayList<TLRPC.Document> getRecentStickersNoCopy(int paramInt)
  {
    return this.recentStickers[paramInt];
  }
  
  public TLRPC.TL_messages_stickerSet getStickerSetById(long paramLong)
  {
    return (TLRPC.TL_messages_stickerSet)this.stickerSetsById.get(paramLong);
  }
  
  public TLRPC.TL_messages_stickerSet getStickerSetByName(String paramString)
  {
    return (TLRPC.TL_messages_stickerSet)this.stickerSetsByName.get(paramString);
  }
  
  public String getStickerSetName(long paramLong)
  {
    Object localObject = (TLRPC.TL_messages_stickerSet)this.stickerSetsById.get(paramLong);
    if (localObject != null) {
      return ((TLRPC.TL_messages_stickerSet)localObject).set.short_name;
    }
    localObject = (TLRPC.StickerSetCovered)this.featuredStickerSetsById.get(paramLong);
    if (localObject != null) {
      return ((TLRPC.StickerSetCovered)localObject).set.short_name;
    }
    return null;
  }
  
  public ArrayList<TLRPC.TL_messages_stickerSet> getStickerSets(int paramInt)
  {
    if (paramInt == 3) {
      return this.stickerSets[2];
    }
    return this.stickerSets[paramInt];
  }
  
  public ArrayList<Long> getUnreadStickerSets()
  {
    return this.unreadStickerSets;
  }
  
  public void increaseInlineRaiting(int paramInt)
  {
    int i;
    Object localObject2;
    int j;
    if (UserConfig.getInstance(this.currentAccount).botRatingLoadTime != 0)
    {
      i = Math.max(1, (int)(System.currentTimeMillis() / 1000L) - UserConfig.getInstance(this.currentAccount).botRatingLoadTime);
      localObject2 = null;
      j = 0;
    }
    for (;;)
    {
      Object localObject1 = localObject2;
      if (j < this.inlineBots.size())
      {
        localObject1 = (TLRPC.TL_topPeer)this.inlineBots.get(j);
        if (((TLRPC.TL_topPeer)localObject1).peer.user_id != paramInt) {}
      }
      else
      {
        localObject2 = localObject1;
        if (localObject1 == null)
        {
          localObject2 = new TLRPC.TL_topPeer();
          ((TLRPC.TL_topPeer)localObject2).peer = new TLRPC.TL_peerUser();
          ((TLRPC.TL_topPeer)localObject2).peer.user_id = paramInt;
          this.inlineBots.add(localObject2);
        }
        ((TLRPC.TL_topPeer)localObject2).rating += Math.exp(i / MessagesController.getInstance(this.currentAccount).ratingDecay);
        Collections.sort(this.inlineBots, new Comparator()
        {
          public int compare(TLRPC.TL_topPeer paramAnonymousTL_topPeer1, TLRPC.TL_topPeer paramAnonymousTL_topPeer2)
          {
            if (paramAnonymousTL_topPeer1.rating > paramAnonymousTL_topPeer2.rating) {
              return -1;
            }
            if (paramAnonymousTL_topPeer1.rating < paramAnonymousTL_topPeer2.rating) {
              return 1;
            }
            return 0;
          }
        });
        if (this.inlineBots.size() > 20) {
          this.inlineBots.remove(this.inlineBots.size() - 1);
        }
        savePeer(paramInt, 1, ((TLRPC.TL_topPeer)localObject2).rating);
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.reloadInlineHints, new Object[0]);
        return;
        i = 60;
        break;
      }
      j += 1;
    }
  }
  
  public void increasePeerRaiting(final long paramLong)
  {
    int i = (int)paramLong;
    if (i <= 0) {}
    for (;;)
    {
      return;
      if (i > 0) {}
      for (TLRPC.User localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i)); (localUser != null) && (!localUser.bot); localUser = null)
      {
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
        {
          public void run()
          {
            d2 = 0.0D;
            int i = 0;
            int j = 0;
            try
            {
              SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT MAX(mid), MAX(date) FROM messages WHERE uid = %d AND out = 1", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
              if (localSQLiteCursor.next())
              {
                j = localSQLiteCursor.intValue(0);
                i = localSQLiteCursor.intValue(1);
              }
              localSQLiteCursor.dispose();
              d1 = d2;
              if (j > 0)
              {
                d1 = d2;
                if (UserConfig.getInstance(DataQuery.this.currentAccount).ratingLoadTime != 0)
                {
                  j = UserConfig.getInstance(DataQuery.this.currentAccount).ratingLoadTime;
                  d1 = i - j;
                }
              }
            }
            catch (Exception localException)
            {
              for (;;)
              {
                FileLog.e(localException);
                final double d1 = d2;
              }
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                Object localObject2 = null;
                int i = 0;
                Object localObject1 = localObject2;
                if (i < DataQuery.this.hints.size())
                {
                  localObject1 = (TLRPC.TL_topPeer)DataQuery.this.hints.get(i);
                  if (((DataQuery.48.this.val$lower_id >= 0) || ((((TLRPC.TL_topPeer)localObject1).peer.chat_id != -DataQuery.48.this.val$lower_id) && (((TLRPC.TL_topPeer)localObject1).peer.channel_id != -DataQuery.48.this.val$lower_id))) && ((DataQuery.48.this.val$lower_id <= 0) || (((TLRPC.TL_topPeer)localObject1).peer.user_id != DataQuery.48.this.val$lower_id))) {}
                }
                else
                {
                  localObject2 = localObject1;
                  if (localObject1 == null)
                  {
                    localObject2 = new TLRPC.TL_topPeer();
                    if (DataQuery.48.this.val$lower_id <= 0) {
                      break label287;
                    }
                    ((TLRPC.TL_topPeer)localObject2).peer = new TLRPC.TL_peerUser();
                    ((TLRPC.TL_topPeer)localObject2).peer.user_id = DataQuery.48.this.val$lower_id;
                  }
                }
                for (;;)
                {
                  DataQuery.this.hints.add(localObject2);
                  ((TLRPC.TL_topPeer)localObject2).rating += Math.exp(d1 / MessagesController.getInstance(DataQuery.this.currentAccount).ratingDecay);
                  Collections.sort(DataQuery.this.hints, new Comparator()
                  {
                    public int compare(TLRPC.TL_topPeer paramAnonymous3TL_topPeer1, TLRPC.TL_topPeer paramAnonymous3TL_topPeer2)
                    {
                      if (paramAnonymous3TL_topPeer1.rating > paramAnonymous3TL_topPeer2.rating) {
                        return -1;
                      }
                      if (paramAnonymous3TL_topPeer1.rating < paramAnonymous3TL_topPeer2.rating) {
                        return 1;
                      }
                      return 0;
                    }
                  });
                  DataQuery.this.savePeer((int)DataQuery.48.this.val$did, 0, ((TLRPC.TL_topPeer)localObject2).rating);
                  NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.reloadHints, new Object[0]);
                  return;
                  i += 1;
                  break;
                  label287:
                  ((TLRPC.TL_topPeer)localObject2).peer = new TLRPC.TL_peerChat();
                  ((TLRPC.TL_topPeer)localObject2).peer.chat_id = (-DataQuery.48.this.val$lower_id);
                }
              }
            });
          }
        });
        return;
      }
    }
  }
  
  /* Error */
  public void installShortcut(long paramLong)
  {
    // Byte code:
    //   0: aload_0
    //   1: lload_1
    //   2: invokespecial 1783	org/telegram/messenger/DataQuery:createIntrnalShortcutIntent	(J)Landroid/content/Intent;
    //   5: astore 14
    //   7: lload_1
    //   8: l2i
    //   9: istore 4
    //   11: lload_1
    //   12: bipush 32
    //   14: lshr
    //   15: l2i
    //   16: istore 5
    //   18: aconst_null
    //   19: astore 9
    //   21: aconst_null
    //   22: astore 12
    //   24: iload 4
    //   26: ifne +443 -> 469
    //   29: aload_0
    //   30: getfield 415	org/telegram/messenger/DataQuery:currentAccount	I
    //   33: invokestatic 944	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   36: iload 5
    //   38: invokestatic 949	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   41: invokevirtual 953	org/telegram/messenger/MessagesController:getEncryptedChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   44: astore 7
    //   46: aload 7
    //   48: ifnonnull +4 -> 52
    //   51: return
    //   52: aload_0
    //   53: getfield 415	org/telegram/messenger/DataQuery:currentAccount	I
    //   56: invokestatic 944	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   59: aload 7
    //   61: getfield 1786	org/telegram/tgnet/TLRPC$EncryptedChat:user_id	I
    //   64: invokestatic 949	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   67: invokevirtual 1772	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   70: astore 9
    //   72: goto +1031 -> 1103
    //   75: aconst_null
    //   76: astore 11
    //   78: iconst_0
    //   79: istore 5
    //   81: aload 9
    //   83: ifnull +489 -> 572
    //   86: aload 9
    //   88: invokestatic 1792	org/telegram/messenger/UserObject:isUserSelf	(Lorg/telegram/tgnet/TLRPC$User;)Z
    //   91: ifeq +429 -> 520
    //   94: ldc_w 1794
    //   97: ldc_w 1795
    //   100: invokestatic 1432	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   103: astore 10
    //   105: iconst_1
    //   106: istore 4
    //   108: aconst_null
    //   109: astore 13
    //   111: aconst_null
    //   112: astore 7
    //   114: aconst_null
    //   115: astore 8
    //   117: iload 4
    //   119: ifne +8 -> 127
    //   122: aload 11
    //   124: ifnull +248 -> 372
    //   127: iload 4
    //   129: ifne +987 -> 1116
    //   132: aload 13
    //   134: astore 8
    //   136: aload 11
    //   138: iconst_1
    //   139: invokestatic 1801	org/telegram/messenger/FileLoader:getPathToAttach	(Lorg/telegram/tgnet/TLObject;Z)Ljava/io/File;
    //   142: invokevirtual 1804	java/io/File:toString	()Ljava/lang/String;
    //   145: invokestatic 1810	android/graphics/BitmapFactory:decodeFile	(Ljava/lang/String;)Landroid/graphics/Bitmap;
    //   148: astore 7
    //   150: goto +966 -> 1116
    //   153: aload 7
    //   155: astore 8
    //   157: ldc_w 1811
    //   160: invokestatic 1815	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   163: istore 5
    //   165: aload 7
    //   167: astore 8
    //   169: iload 5
    //   171: iload 5
    //   173: getstatic 1821	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
    //   176: invokestatic 1827	android/graphics/Bitmap:createBitmap	(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;
    //   179: astore 11
    //   181: aload 7
    //   183: astore 8
    //   185: aload 11
    //   187: iconst_0
    //   188: invokevirtual 1830	android/graphics/Bitmap:eraseColor	(I)V
    //   191: aload 7
    //   193: astore 8
    //   195: new 1832	android/graphics/Canvas
    //   198: dup
    //   199: aload 11
    //   201: invokespecial 1835	android/graphics/Canvas:<init>	(Landroid/graphics/Bitmap;)V
    //   204: astore 13
    //   206: iload 4
    //   208: ifeq +408 -> 616
    //   211: aload 7
    //   213: astore 8
    //   215: new 1837	org/telegram/ui/Components/AvatarDrawable
    //   218: dup
    //   219: aload 9
    //   221: invokespecial 1840	org/telegram/ui/Components/AvatarDrawable:<init>	(Lorg/telegram/tgnet/TLRPC$User;)V
    //   224: astore 15
    //   226: aload 7
    //   228: astore 8
    //   230: aload 15
    //   232: iconst_1
    //   233: invokevirtual 1843	org/telegram/ui/Components/AvatarDrawable:setSavedMessages	(I)V
    //   236: aload 7
    //   238: astore 8
    //   240: aload 15
    //   242: iconst_0
    //   243: iconst_0
    //   244: iload 5
    //   246: iload 5
    //   248: invokevirtual 1847	org/telegram/ui/Components/AvatarDrawable:setBounds	(IIII)V
    //   251: aload 7
    //   253: astore 8
    //   255: aload 15
    //   257: aload 13
    //   259: invokevirtual 1851	org/telegram/ui/Components/AvatarDrawable:draw	(Landroid/graphics/Canvas;)V
    //   262: aload 7
    //   264: astore 8
    //   266: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   269: invokevirtual 1855	android/content/Context:getResources	()Landroid/content/res/Resources;
    //   272: ldc_w 1856
    //   275: invokevirtual 1862	android/content/res/Resources:getDrawable	(I)Landroid/graphics/drawable/Drawable;
    //   278: astore 15
    //   280: aload 7
    //   282: astore 8
    //   284: ldc_w 1863
    //   287: invokestatic 1815	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   290: istore 4
    //   292: aload 7
    //   294: astore 8
    //   296: iload 5
    //   298: iload 4
    //   300: isub
    //   301: fconst_2
    //   302: invokestatic 1815	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   305: isub
    //   306: istore 6
    //   308: aload 7
    //   310: astore 8
    //   312: iload 5
    //   314: iload 4
    //   316: isub
    //   317: fconst_2
    //   318: invokestatic 1815	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   321: isub
    //   322: istore 5
    //   324: aload 7
    //   326: astore 8
    //   328: aload 15
    //   330: iload 6
    //   332: iload 5
    //   334: iload 6
    //   336: iload 4
    //   338: iadd
    //   339: iload 5
    //   341: iload 4
    //   343: iadd
    //   344: invokevirtual 1866	android/graphics/drawable/Drawable:setBounds	(IIII)V
    //   347: aload 7
    //   349: astore 8
    //   351: aload 15
    //   353: aload 13
    //   355: invokevirtual 1867	android/graphics/drawable/Drawable:draw	(Landroid/graphics/Canvas;)V
    //   358: aload 7
    //   360: astore 8
    //   362: aload 13
    //   364: aconst_null
    //   365: invokevirtual 1870	android/graphics/Canvas:setBitmap	(Landroid/graphics/Bitmap;)V
    //   368: aload 11
    //   370: astore 8
    //   372: getstatic 1488	android/os/Build$VERSION:SDK_INT	I
    //   375: bipush 26
    //   377: if_icmplt +527 -> 904
    //   380: new 1872	android/content/pm/ShortcutInfo$Builder
    //   383: dup
    //   384: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   387: new 524	java/lang/StringBuilder
    //   390: dup
    //   391: invokespecial 525	java/lang/StringBuilder:<init>	()V
    //   394: ldc_w 1874
    //   397: invokevirtual 529	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   400: lload_1
    //   401: invokevirtual 961	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   404: invokevirtual 536	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   407: invokespecial 1877	android/content/pm/ShortcutInfo$Builder:<init>	(Landroid/content/Context;Ljava/lang/String;)V
    //   410: aload 10
    //   412: invokevirtual 1881	android/content/pm/ShortcutInfo$Builder:setShortLabel	(Ljava/lang/CharSequence;)Landroid/content/pm/ShortcutInfo$Builder;
    //   415: aload 14
    //   417: invokevirtual 1885	android/content/pm/ShortcutInfo$Builder:setIntent	(Landroid/content/Intent;)Landroid/content/pm/ShortcutInfo$Builder;
    //   420: astore 7
    //   422: aload 8
    //   424: ifnull +374 -> 798
    //   427: aload 7
    //   429: aload 8
    //   431: invokestatic 1891	android/graphics/drawable/Icon:createWithBitmap	(Landroid/graphics/Bitmap;)Landroid/graphics/drawable/Icon;
    //   434: invokevirtual 1895	android/content/pm/ShortcutInfo$Builder:setIcon	(Landroid/graphics/drawable/Icon;)Landroid/content/pm/ShortcutInfo$Builder;
    //   437: pop
    //   438: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   441: ldc_w 1897
    //   444: invokevirtual 1901	android/content/Context:getSystemService	(Ljava/lang/Class;)Ljava/lang/Object;
    //   447: checkcast 1897	android/content/pm/ShortcutManager
    //   450: aload 7
    //   452: invokevirtual 1905	android/content/pm/ShortcutInfo$Builder:build	()Landroid/content/pm/ShortcutInfo;
    //   455: aconst_null
    //   456: invokevirtual 1909	android/content/pm/ShortcutManager:requestPinShortcut	(Landroid/content/pm/ShortcutInfo;Landroid/content/IntentSender;)Z
    //   459: pop
    //   460: return
    //   461: astore 7
    //   463: aload 7
    //   465: invokestatic 1146	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   468: return
    //   469: iload 4
    //   471: ifle +23 -> 494
    //   474: aload_0
    //   475: getfield 415	org/telegram/messenger/DataQuery:currentAccount	I
    //   478: invokestatic 944	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   481: iload 4
    //   483: invokestatic 949	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   486: invokevirtual 1772	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   489: astore 9
    //   491: goto +612 -> 1103
    //   494: iload 4
    //   496: ifge +606 -> 1102
    //   499: aload_0
    //   500: getfield 415	org/telegram/messenger/DataQuery:currentAccount	I
    //   503: invokestatic 944	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   506: iload 4
    //   508: ineg
    //   509: invokestatic 949	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   512: invokevirtual 1913	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   515: astore 12
    //   517: goto +586 -> 1103
    //   520: aload 9
    //   522: getfield 1916	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   525: aload 9
    //   527: getfield 1919	org/telegram/tgnet/TLRPC$User:last_name	Ljava/lang/String;
    //   530: invokestatic 1925	org/telegram/messenger/ContactsController:formatName	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   533: astore 7
    //   535: aload 7
    //   537: astore 10
    //   539: iload 5
    //   541: istore 4
    //   543: aload 9
    //   545: getfield 1929	org/telegram/tgnet/TLRPC$User:photo	Lorg/telegram/tgnet/TLRPC$UserProfilePhoto;
    //   548: ifnull -440 -> 108
    //   551: aload 9
    //   553: getfield 1929	org/telegram/tgnet/TLRPC$User:photo	Lorg/telegram/tgnet/TLRPC$UserProfilePhoto;
    //   556: getfield 1935	org/telegram/tgnet/TLRPC$UserProfilePhoto:photo_small	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   559: astore 11
    //   561: aload 7
    //   563: astore 10
    //   565: iload 5
    //   567: istore 4
    //   569: goto -461 -> 108
    //   572: aload 12
    //   574: getfield 1938	org/telegram/tgnet/TLRPC$Chat:title	Ljava/lang/String;
    //   577: astore 7
    //   579: aload 7
    //   581: astore 10
    //   583: iload 5
    //   585: istore 4
    //   587: aload 12
    //   589: getfield 1941	org/telegram/tgnet/TLRPC$Chat:photo	Lorg/telegram/tgnet/TLRPC$ChatPhoto;
    //   592: ifnull -484 -> 108
    //   595: aload 12
    //   597: getfield 1941	org/telegram/tgnet/TLRPC$Chat:photo	Lorg/telegram/tgnet/TLRPC$ChatPhoto;
    //   600: getfield 1944	org/telegram/tgnet/TLRPC$ChatPhoto:photo_small	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   603: astore 11
    //   605: aload 7
    //   607: astore 10
    //   609: iload 5
    //   611: istore 4
    //   613: goto -505 -> 108
    //   616: aload 7
    //   618: astore 8
    //   620: new 1946	android/graphics/BitmapShader
    //   623: dup
    //   624: aload 7
    //   626: getstatic 1952	android/graphics/Shader$TileMode:CLAMP	Landroid/graphics/Shader$TileMode;
    //   629: getstatic 1952	android/graphics/Shader$TileMode:CLAMP	Landroid/graphics/Shader$TileMode;
    //   632: invokespecial 1955	android/graphics/BitmapShader:<init>	(Landroid/graphics/Bitmap;Landroid/graphics/Shader$TileMode;Landroid/graphics/Shader$TileMode;)V
    //   635: astore 15
    //   637: aload 7
    //   639: astore 8
    //   641: getstatic 699	org/telegram/messenger/DataQuery:roundPaint	Landroid/graphics/Paint;
    //   644: ifnonnull +32 -> 676
    //   647: aload 7
    //   649: astore 8
    //   651: new 1957	android/graphics/Paint
    //   654: dup
    //   655: iconst_1
    //   656: invokespecial 1958	android/graphics/Paint:<init>	(I)V
    //   659: putstatic 699	org/telegram/messenger/DataQuery:roundPaint	Landroid/graphics/Paint;
    //   662: aload 7
    //   664: astore 8
    //   666: new 1960	android/graphics/RectF
    //   669: dup
    //   670: invokespecial 1961	android/graphics/RectF:<init>	()V
    //   673: putstatic 705	org/telegram/messenger/DataQuery:bitmapRect	Landroid/graphics/RectF;
    //   676: aload 7
    //   678: astore 8
    //   680: iload 5
    //   682: i2f
    //   683: aload 7
    //   685: invokevirtual 1964	android/graphics/Bitmap:getWidth	()I
    //   688: i2f
    //   689: fdiv
    //   690: fstore_3
    //   691: aload 7
    //   693: astore 8
    //   695: aload 13
    //   697: invokevirtual 1967	android/graphics/Canvas:save	()I
    //   700: pop
    //   701: aload 7
    //   703: astore 8
    //   705: aload 13
    //   707: fload_3
    //   708: fload_3
    //   709: invokevirtual 1971	android/graphics/Canvas:scale	(FF)V
    //   712: aload 7
    //   714: astore 8
    //   716: getstatic 699	org/telegram/messenger/DataQuery:roundPaint	Landroid/graphics/Paint;
    //   719: aload 15
    //   721: invokevirtual 1975	android/graphics/Paint:setShader	(Landroid/graphics/Shader;)Landroid/graphics/Shader;
    //   724: pop
    //   725: aload 7
    //   727: astore 8
    //   729: getstatic 705	org/telegram/messenger/DataQuery:bitmapRect	Landroid/graphics/RectF;
    //   732: fconst_0
    //   733: fconst_0
    //   734: aload 7
    //   736: invokevirtual 1964	android/graphics/Bitmap:getWidth	()I
    //   739: i2f
    //   740: aload 7
    //   742: invokevirtual 1978	android/graphics/Bitmap:getHeight	()I
    //   745: i2f
    //   746: invokevirtual 1981	android/graphics/RectF:set	(FFFF)V
    //   749: aload 7
    //   751: astore 8
    //   753: aload 13
    //   755: getstatic 705	org/telegram/messenger/DataQuery:bitmapRect	Landroid/graphics/RectF;
    //   758: aload 7
    //   760: invokevirtual 1964	android/graphics/Bitmap:getWidth	()I
    //   763: i2f
    //   764: aload 7
    //   766: invokevirtual 1978	android/graphics/Bitmap:getHeight	()I
    //   769: i2f
    //   770: getstatic 699	org/telegram/messenger/DataQuery:roundPaint	Landroid/graphics/Paint;
    //   773: invokevirtual 1985	android/graphics/Canvas:drawRoundRect	(Landroid/graphics/RectF;FFLandroid/graphics/Paint;)V
    //   776: aload 7
    //   778: astore 8
    //   780: aload 13
    //   782: invokevirtual 1988	android/graphics/Canvas:restore	()V
    //   785: goto -523 -> 262
    //   788: astore 7
    //   790: aload 7
    //   792: invokestatic 1146	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   795: goto -423 -> 372
    //   798: aload 9
    //   800: ifnull +47 -> 847
    //   803: aload 9
    //   805: getfield 1775	org/telegram/tgnet/TLRPC$User:bot	Z
    //   808: ifeq +21 -> 829
    //   811: aload 7
    //   813: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   816: ldc_w 1989
    //   819: invokestatic 1993	android/graphics/drawable/Icon:createWithResource	(Landroid/content/Context;I)Landroid/graphics/drawable/Icon;
    //   822: invokevirtual 1895	android/content/pm/ShortcutInfo$Builder:setIcon	(Landroid/graphics/drawable/Icon;)Landroid/content/pm/ShortcutInfo$Builder;
    //   825: pop
    //   826: goto -388 -> 438
    //   829: aload 7
    //   831: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   834: ldc_w 1994
    //   837: invokestatic 1993	android/graphics/drawable/Icon:createWithResource	(Landroid/content/Context;I)Landroid/graphics/drawable/Icon;
    //   840: invokevirtual 1895	android/content/pm/ShortcutInfo$Builder:setIcon	(Landroid/graphics/drawable/Icon;)Landroid/content/pm/ShortcutInfo$Builder;
    //   843: pop
    //   844: goto -406 -> 438
    //   847: aload 12
    //   849: ifnull -411 -> 438
    //   852: aload 12
    //   854: invokestatic 2000	org/telegram/messenger/ChatObject:isChannel	(Lorg/telegram/tgnet/TLRPC$Chat;)Z
    //   857: ifeq +29 -> 886
    //   860: aload 12
    //   862: getfield 2003	org/telegram/tgnet/TLRPC$Chat:megagroup	Z
    //   865: ifne +21 -> 886
    //   868: aload 7
    //   870: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   873: ldc_w 2004
    //   876: invokestatic 1993	android/graphics/drawable/Icon:createWithResource	(Landroid/content/Context;I)Landroid/graphics/drawable/Icon;
    //   879: invokevirtual 1895	android/content/pm/ShortcutInfo$Builder:setIcon	(Landroid/graphics/drawable/Icon;)Landroid/content/pm/ShortcutInfo$Builder;
    //   882: pop
    //   883: goto -445 -> 438
    //   886: aload 7
    //   888: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   891: ldc_w 2005
    //   894: invokestatic 1993	android/graphics/drawable/Icon:createWithResource	(Landroid/content/Context;I)Landroid/graphics/drawable/Icon;
    //   897: invokevirtual 1895	android/content/pm/ShortcutInfo$Builder:setIcon	(Landroid/graphics/drawable/Icon;)Landroid/content/pm/ShortcutInfo$Builder;
    //   900: pop
    //   901: goto -463 -> 438
    //   904: new 928	android/content/Intent
    //   907: dup
    //   908: invokespecial 2006	android/content/Intent:<init>	()V
    //   911: astore 7
    //   913: aload 8
    //   915: ifnull +64 -> 979
    //   918: aload 7
    //   920: ldc_w 2008
    //   923: aload 8
    //   925: invokevirtual 2011	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   928: pop
    //   929: aload 7
    //   931: ldc_w 2013
    //   934: aload 14
    //   936: invokevirtual 2011	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   939: pop
    //   940: aload 7
    //   942: ldc_w 2015
    //   945: aload 10
    //   947: invokevirtual 2018	android/content/Intent:putExtra	(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
    //   950: pop
    //   951: aload 7
    //   953: ldc_w 2020
    //   956: iconst_0
    //   957: invokevirtual 2023	android/content/Intent:putExtra	(Ljava/lang/String;Z)Landroid/content/Intent;
    //   960: pop
    //   961: aload 7
    //   963: ldc_w 2025
    //   966: invokevirtual 965	android/content/Intent:setAction	(Ljava/lang/String;)Landroid/content/Intent;
    //   969: pop
    //   970: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   973: aload 7
    //   975: invokevirtual 2029	android/content/Context:sendBroadcast	(Landroid/content/Intent;)V
    //   978: return
    //   979: aload 9
    //   981: ifnull +53 -> 1034
    //   984: aload 9
    //   986: getfield 1775	org/telegram/tgnet/TLRPC$User:bot	Z
    //   989: ifeq +24 -> 1013
    //   992: aload 7
    //   994: ldc_w 2031
    //   997: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   1000: ldc_w 1989
    //   1003: invokestatic 2037	android/content/Intent$ShortcutIconResource:fromContext	(Landroid/content/Context;I)Landroid/content/Intent$ShortcutIconResource;
    //   1006: invokevirtual 2011	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   1009: pop
    //   1010: goto -81 -> 929
    //   1013: aload 7
    //   1015: ldc_w 2031
    //   1018: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   1021: ldc_w 1994
    //   1024: invokestatic 2037	android/content/Intent$ShortcutIconResource:fromContext	(Landroid/content/Context;I)Landroid/content/Intent$ShortcutIconResource;
    //   1027: invokevirtual 2011	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   1030: pop
    //   1031: goto -102 -> 929
    //   1034: aload 12
    //   1036: ifnull -107 -> 929
    //   1039: aload 12
    //   1041: invokestatic 2000	org/telegram/messenger/ChatObject:isChannel	(Lorg/telegram/tgnet/TLRPC$Chat;)Z
    //   1044: ifeq +32 -> 1076
    //   1047: aload 12
    //   1049: getfield 2003	org/telegram/tgnet/TLRPC$Chat:megagroup	Z
    //   1052: ifne +24 -> 1076
    //   1055: aload 7
    //   1057: ldc_w 2031
    //   1060: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   1063: ldc_w 2004
    //   1066: invokestatic 2037	android/content/Intent$ShortcutIconResource:fromContext	(Landroid/content/Context;I)Landroid/content/Intent$ShortcutIconResource;
    //   1069: invokevirtual 2011	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   1072: pop
    //   1073: goto -144 -> 929
    //   1076: aload 7
    //   1078: ldc_w 2031
    //   1081: getstatic 421	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   1084: ldc_w 2005
    //   1087: invokestatic 2037	android/content/Intent$ShortcutIconResource:fromContext	(Landroid/content/Context;I)Landroid/content/Intent$ShortcutIconResource;
    //   1090: invokevirtual 2011	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   1093: pop
    //   1094: goto -165 -> 929
    //   1097: astore 7
    //   1099: goto -731 -> 368
    //   1102: return
    //   1103: aload 9
    //   1105: ifnonnull -1030 -> 75
    //   1108: aload 12
    //   1110: ifnull -8 -> 1102
    //   1113: goto -1038 -> 75
    //   1116: iload 4
    //   1118: ifne -965 -> 153
    //   1121: aload 7
    //   1123: astore 8
    //   1125: aload 7
    //   1127: ifnull -755 -> 372
    //   1130: goto -977 -> 153
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1133	0	this	DataQuery
    //   0	1133	1	paramLong	long
    //   690	19	3	f	float
    //   9	1108	4	i	int
    //   16	665	5	j	int
    //   306	33	6	k	int
    //   44	407	7	localObject1	Object
    //   461	3	7	localException1	Exception
    //   533	244	7	str1	String
    //   788	99	7	localThrowable	Throwable
    //   911	166	7	localIntent1	Intent
    //   1097	29	7	localException2	Exception
    //   115	1009	8	localObject2	Object
    //   19	1085	9	localUser	TLRPC.User
    //   103	843	10	str2	String
    //   76	528	11	localObject3	Object
    //   22	1087	12	localChat	TLRPC.Chat
    //   109	672	13	localCanvas	Canvas
    //   5	930	14	localIntent2	Intent
    //   224	496	15	localObject4	Object
    // Exception table:
    //   from	to	target	type
    //   0	7	461	java/lang/Exception
    //   29	46	461	java/lang/Exception
    //   52	72	461	java/lang/Exception
    //   86	105	461	java/lang/Exception
    //   136	150	461	java/lang/Exception
    //   157	165	461	java/lang/Exception
    //   169	181	461	java/lang/Exception
    //   185	191	461	java/lang/Exception
    //   195	206	461	java/lang/Exception
    //   215	226	461	java/lang/Exception
    //   230	236	461	java/lang/Exception
    //   240	251	461	java/lang/Exception
    //   255	262	461	java/lang/Exception
    //   266	280	461	java/lang/Exception
    //   284	292	461	java/lang/Exception
    //   296	308	461	java/lang/Exception
    //   312	324	461	java/lang/Exception
    //   328	347	461	java/lang/Exception
    //   351	358	461	java/lang/Exception
    //   372	422	461	java/lang/Exception
    //   427	438	461	java/lang/Exception
    //   438	460	461	java/lang/Exception
    //   474	491	461	java/lang/Exception
    //   499	517	461	java/lang/Exception
    //   520	535	461	java/lang/Exception
    //   543	561	461	java/lang/Exception
    //   572	579	461	java/lang/Exception
    //   587	605	461	java/lang/Exception
    //   620	637	461	java/lang/Exception
    //   641	647	461	java/lang/Exception
    //   651	662	461	java/lang/Exception
    //   666	676	461	java/lang/Exception
    //   680	691	461	java/lang/Exception
    //   695	701	461	java/lang/Exception
    //   705	712	461	java/lang/Exception
    //   716	725	461	java/lang/Exception
    //   729	749	461	java/lang/Exception
    //   753	776	461	java/lang/Exception
    //   780	785	461	java/lang/Exception
    //   790	795	461	java/lang/Exception
    //   803	826	461	java/lang/Exception
    //   829	844	461	java/lang/Exception
    //   852	883	461	java/lang/Exception
    //   886	901	461	java/lang/Exception
    //   904	913	461	java/lang/Exception
    //   918	929	461	java/lang/Exception
    //   929	978	461	java/lang/Exception
    //   984	1010	461	java/lang/Exception
    //   1013	1031	461	java/lang/Exception
    //   1039	1073	461	java/lang/Exception
    //   1076	1094	461	java/lang/Exception
    //   136	150	788	java/lang/Throwable
    //   157	165	788	java/lang/Throwable
    //   169	181	788	java/lang/Throwable
    //   185	191	788	java/lang/Throwable
    //   195	206	788	java/lang/Throwable
    //   215	226	788	java/lang/Throwable
    //   230	236	788	java/lang/Throwable
    //   240	251	788	java/lang/Throwable
    //   255	262	788	java/lang/Throwable
    //   266	280	788	java/lang/Throwable
    //   284	292	788	java/lang/Throwable
    //   296	308	788	java/lang/Throwable
    //   312	324	788	java/lang/Throwable
    //   328	347	788	java/lang/Throwable
    //   351	358	788	java/lang/Throwable
    //   362	368	788	java/lang/Throwable
    //   620	637	788	java/lang/Throwable
    //   641	647	788	java/lang/Throwable
    //   651	662	788	java/lang/Throwable
    //   666	676	788	java/lang/Throwable
    //   680	691	788	java/lang/Throwable
    //   695	701	788	java/lang/Throwable
    //   705	712	788	java/lang/Throwable
    //   716	725	788	java/lang/Throwable
    //   729	749	788	java/lang/Throwable
    //   753	776	788	java/lang/Throwable
    //   780	785	788	java/lang/Throwable
    //   362	368	1097	java/lang/Exception
  }
  
  public boolean isLoadingStickers(int paramInt)
  {
    return this.loadingStickers[paramInt];
  }
  
  public boolean isMessageFound(int paramInt, boolean paramBoolean)
  {
    SparseArray[] arrayOfSparseArray = this.searchResultMessagesMap;
    if (paramBoolean) {}
    for (int i = 1; arrayOfSparseArray[i].indexOfKey(paramInt) >= 0; i = 0) {
      return true;
    }
    return false;
  }
  
  public boolean isStickerInFavorites(TLRPC.Document paramDocument)
  {
    int i = 0;
    while (i < this.recentStickers[2].size())
    {
      TLRPC.Document localDocument = (TLRPC.Document)this.recentStickers[2].get(i);
      if ((localDocument.id == paramDocument.id) && (localDocument.dc_id == paramDocument.dc_id)) {
        return true;
      }
      i += 1;
    }
    return false;
  }
  
  public boolean isStickerPackInstalled(long paramLong)
  {
    return this.installedStickerSetsById.indexOfKey(paramLong) >= 0;
  }
  
  public boolean isStickerPackInstalled(String paramString)
  {
    return this.stickerSetsByName.containsKey(paramString);
  }
  
  public boolean isStickerPackUnread(long paramLong)
  {
    return this.unreadStickerSets.contains(Long.valueOf(paramLong));
  }
  
  public void loadArchivedStickersCount(final int paramInt, boolean paramBoolean)
  {
    boolean bool = true;
    if (paramBoolean)
    {
      int i = MessagesController.getNotificationsSettings(this.currentAccount).getInt("archivedStickersCount" + paramInt, -1);
      if (i == -1)
      {
        loadArchivedStickersCount(paramInt, false);
        return;
      }
      this.archivedStickersCount[paramInt] = i;
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.archivedStickersCountDidLoaded, new Object[] { Integer.valueOf(paramInt) });
      return;
    }
    TLRPC.TL_messages_getArchivedStickers localTL_messages_getArchivedStickers = new TLRPC.TL_messages_getArchivedStickers();
    localTL_messages_getArchivedStickers.limit = 0;
    if (paramInt == 1) {}
    for (paramBoolean = bool;; paramBoolean = false)
    {
      localTL_messages_getArchivedStickers.masks = paramBoolean;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getArchivedStickers, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error == null)
              {
                TLRPC.TL_messages_archivedStickers localTL_messages_archivedStickers = (TLRPC.TL_messages_archivedStickers)paramAnonymousTLObject;
                DataQuery.this.archivedStickersCount[DataQuery.22.this.val$type] = localTL_messages_archivedStickers.count;
                MessagesController.getNotificationsSettings(DataQuery.this.currentAccount).edit().putInt("archivedStickersCount" + DataQuery.22.this.val$type, localTL_messages_archivedStickers.count).commit();
                NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.archivedStickersCountDidLoaded, new Object[] { Integer.valueOf(DataQuery.22.this.val$type) });
              }
            }
          });
        }
      });
      return;
    }
  }
  
  public void loadBotInfo(final int paramInt1, boolean paramBoolean, final int paramInt2)
  {
    if (paramBoolean)
    {
      TLRPC.BotInfo localBotInfo = (TLRPC.BotInfo)this.botInfos.get(paramInt1);
      if (localBotInfo != null)
      {
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.botInfoDidLoaded, new Object[] { localBotInfo, Integer.valueOf(paramInt2) });
        return;
      }
    }
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject2 = null;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT info FROM bot_info WHERE uid = %d", new Object[] { Integer.valueOf(paramInt1) }), new Object[0]);
          final Object localObject1 = localObject2;
          if (localSQLiteCursor.next())
          {
            localObject1 = localObject2;
            if (!localSQLiteCursor.isNull(0))
            {
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
              localObject1 = localObject2;
              if (localNativeByteBuffer != null)
              {
                localObject1 = TLRPC.BotInfo.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                localNativeByteBuffer.reuse();
              }
            }
          }
          localSQLiteCursor.dispose();
          if (localObject1 != null) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.botInfoDidLoaded, new Object[] { localObject1, Integer.valueOf(DataQuery.66.this.val$classGuid) });
              }
            });
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
  
  public void loadBotKeyboard(final long paramLong)
  {
    TLRPC.Message localMessage = (TLRPC.Message)this.botKeyboards.get(paramLong);
    if (localMessage != null)
    {
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.botKeyboardDidLoaded, new Object[] { localMessage, Long.valueOf(paramLong) });
      return;
    }
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject2 = null;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT info FROM bot_keyboard WHERE uid = %d", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
          final Object localObject1 = localObject2;
          if (localSQLiteCursor.next())
          {
            localObject1 = localObject2;
            if (!localSQLiteCursor.isNull(0))
            {
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
              localObject1 = localObject2;
              if (localNativeByteBuffer != null)
              {
                localObject1 = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                localNativeByteBuffer.reuse();
              }
            }
          }
          localSQLiteCursor.dispose();
          if (localObject1 != null) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.botKeyboardDidLoaded, new Object[] { localObject1, Long.valueOf(DataQuery.65.this.val$did) });
              }
            });
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
  
  public void loadDrafts()
  {
    if ((UserConfig.getInstance(this.currentAccount).draftsLoaded) || (this.loadingDrafts)) {
      return;
    }
    this.loadingDrafts = true;
    TLRPC.TL_messages_getAllDrafts localTL_messages_getAllDrafts = new TLRPC.TL_messages_getAllDrafts();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getAllDrafts, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error != null) {
          return;
        }
        MessagesController.getInstance(DataQuery.this.currentAccount).processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            UserConfig.getInstance(DataQuery.this.currentAccount).draftsLoaded = true;
            DataQuery.access$6302(DataQuery.this, false);
            UserConfig.getInstance(DataQuery.this.currentAccount).saveConfig(false);
          }
        });
      }
    });
  }
  
  public void loadFeaturedStickers(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.loadingFeaturedStickers) {
      return;
    }
    this.loadingFeaturedStickers = true;
    if (paramBoolean1)
    {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        /* Error */
        public void run()
        {
          // Byte code:
          //   0: aconst_null
          //   1: astore 9
          //   3: aconst_null
          //   4: astore 12
          //   6: aconst_null
          //   7: astore 13
          //   9: new 26	java/util/ArrayList
          //   12: dup
          //   13: invokespecial 27	java/util/ArrayList:<init>	()V
          //   16: astore 14
          //   18: iconst_0
          //   19: istore 4
          //   21: iconst_0
          //   22: istore 5
          //   24: iconst_0
          //   25: istore_2
          //   26: iconst_0
          //   27: istore 6
          //   29: iconst_0
          //   30: istore_3
          //   31: aconst_null
          //   32: astore 8
          //   34: aconst_null
          //   35: astore 11
          //   37: aload 12
          //   39: astore 10
          //   41: iload 4
          //   43: istore_1
          //   44: aload_0
          //   45: getfield 17	org/telegram/messenger/DataQuery$14:this$0	Lorg/telegram/messenger/DataQuery;
          //   48: invokestatic 31	org/telegram/messenger/DataQuery:access$000	(Lorg/telegram/messenger/DataQuery;)I
          //   51: invokestatic 37	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
          //   54: invokevirtual 41	org/telegram/messenger/MessagesStorage:getDatabase	()Lorg/telegram/SQLite/SQLiteDatabase;
          //   57: ldc 43
          //   59: iconst_0
          //   60: anewarray 4	java/lang/Object
          //   63: invokevirtual 49	org/telegram/SQLite/SQLiteDatabase:queryFinalized	(Ljava/lang/String;[Ljava/lang/Object;)Lorg/telegram/SQLite/SQLiteCursor;
          //   66: astore 7
          //   68: aload 12
          //   70: astore 10
          //   72: iload 4
          //   74: istore_1
          //   75: aload 7
          //   77: astore 11
          //   79: aload 7
          //   81: astore 8
          //   83: aload 7
          //   85: invokevirtual 55	org/telegram/SQLite/SQLiteCursor:next	()Z
          //   88: ifeq +263 -> 351
          //   91: aload 12
          //   93: astore 10
          //   95: iload 4
          //   97: istore_1
          //   98: aload 7
          //   100: astore 11
          //   102: aload 7
          //   104: astore 8
          //   106: aload 7
          //   108: iconst_0
          //   109: invokevirtual 59	org/telegram/SQLite/SQLiteCursor:byteBufferValue	(I)Lorg/telegram/tgnet/NativeByteBuffer;
          //   112: astore 15
          //   114: aload 13
          //   116: astore 9
          //   118: aload 15
          //   120: ifnull +71 -> 191
          //   123: aload 12
          //   125: astore 10
          //   127: iload 4
          //   129: istore_1
          //   130: aload 7
          //   132: astore 11
          //   134: aload 7
          //   136: astore 8
          //   138: new 26	java/util/ArrayList
          //   141: dup
          //   142: invokespecial 27	java/util/ArrayList:<init>	()V
          //   145: astore 9
          //   147: aload 15
          //   149: iconst_0
          //   150: invokevirtual 65	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
          //   153: istore_2
          //   154: iconst_0
          //   155: istore_1
          //   156: iload_1
          //   157: iload_2
          //   158: if_icmpge +28 -> 186
          //   161: aload 9
          //   163: aload 15
          //   165: aload 15
          //   167: iconst_0
          //   168: invokevirtual 65	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
          //   171: iconst_0
          //   172: invokestatic 71	org/telegram/tgnet/TLRPC$StickerSetCovered:TLdeserialize	(Lorg/telegram/tgnet/AbstractSerializedData;IZ)Lorg/telegram/tgnet/TLRPC$StickerSetCovered;
          //   175: invokevirtual 75	java/util/ArrayList:add	(Ljava/lang/Object;)Z
          //   178: pop
          //   179: iload_1
          //   180: iconst_1
          //   181: iadd
          //   182: istore_1
          //   183: goto -27 -> 156
          //   186: aload 15
          //   188: invokevirtual 78	org/telegram/tgnet/NativeByteBuffer:reuse	()V
          //   191: aload 9
          //   193: astore 10
          //   195: iload 4
          //   197: istore_1
          //   198: aload 7
          //   200: astore 11
          //   202: aload 7
          //   204: astore 8
          //   206: aload 7
          //   208: iconst_1
          //   209: invokevirtual 59	org/telegram/SQLite/SQLiteCursor:byteBufferValue	(I)Lorg/telegram/tgnet/NativeByteBuffer;
          //   212: astore 12
          //   214: aload 12
          //   216: ifnull +89 -> 305
          //   219: aload 9
          //   221: astore 10
          //   223: iload 4
          //   225: istore_1
          //   226: aload 7
          //   228: astore 11
          //   230: aload 7
          //   232: astore 8
          //   234: aload 12
          //   236: iconst_0
          //   237: invokevirtual 65	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
          //   240: istore_3
          //   241: iconst_0
          //   242: istore_2
          //   243: iload_2
          //   244: iload_3
          //   245: if_icmpge +40 -> 285
          //   248: aload 9
          //   250: astore 10
          //   252: iload 4
          //   254: istore_1
          //   255: aload 7
          //   257: astore 11
          //   259: aload 7
          //   261: astore 8
          //   263: aload 14
          //   265: aload 12
          //   267: iconst_0
          //   268: invokevirtual 82	org/telegram/tgnet/NativeByteBuffer:readInt64	(Z)J
          //   271: invokestatic 88	java/lang/Long:valueOf	(J)Ljava/lang/Long;
          //   274: invokevirtual 75	java/util/ArrayList:add	(Ljava/lang/Object;)Z
          //   277: pop
          //   278: iload_2
          //   279: iconst_1
          //   280: iadd
          //   281: istore_2
          //   282: goto -39 -> 243
          //   285: aload 9
          //   287: astore 10
          //   289: iload 4
          //   291: istore_1
          //   292: aload 7
          //   294: astore 11
          //   296: aload 7
          //   298: astore 8
          //   300: aload 12
          //   302: invokevirtual 78	org/telegram/tgnet/NativeByteBuffer:reuse	()V
          //   305: aload 9
          //   307: astore 10
          //   309: iload 4
          //   311: istore_1
          //   312: aload 7
          //   314: astore 11
          //   316: aload 7
          //   318: astore 8
          //   320: aload 7
          //   322: iconst_2
          //   323: invokevirtual 92	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
          //   326: istore_2
          //   327: aload 9
          //   329: astore 10
          //   331: iload_2
          //   332: istore_1
          //   333: aload 7
          //   335: astore 11
          //   337: aload 7
          //   339: astore 8
          //   341: aload_0
          //   342: getfield 17	org/telegram/messenger/DataQuery$14:this$0	Lorg/telegram/messenger/DataQuery;
          //   345: aload 9
          //   347: invokestatic 96	org/telegram/messenger/DataQuery:access$1000	(Lorg/telegram/messenger/DataQuery;Ljava/util/ArrayList;)I
          //   350: istore_3
          //   351: aload 9
          //   353: astore 8
          //   355: iload_2
          //   356: istore 4
          //   358: iload_3
          //   359: istore 5
          //   361: aload 7
          //   363: ifnull +18 -> 381
          //   366: aload 7
          //   368: invokevirtual 99	org/telegram/SQLite/SQLiteCursor:dispose	()V
          //   371: iload_3
          //   372: istore 5
          //   374: iload_2
          //   375: istore 4
          //   377: aload 9
          //   379: astore 8
          //   381: aload_0
          //   382: getfield 17	org/telegram/messenger/DataQuery$14:this$0	Lorg/telegram/messenger/DataQuery;
          //   385: aload 8
          //   387: aload 14
          //   389: iconst_1
          //   390: iload 4
          //   392: iload 5
          //   394: invokestatic 103	org/telegram/messenger/DataQuery:access$1100	(Lorg/telegram/messenger/DataQuery;Ljava/util/ArrayList;Ljava/util/ArrayList;ZII)V
          //   397: return
          //   398: astore 9
          //   400: aload 11
          //   402: astore 7
          //   404: aload 7
          //   406: astore 8
          //   408: aload 9
          //   410: invokestatic 109	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
          //   413: aload 10
          //   415: astore 8
          //   417: iload_1
          //   418: istore 4
          //   420: iload 6
          //   422: istore 5
          //   424: aload 7
          //   426: ifnull -45 -> 381
          //   429: aload 7
          //   431: invokevirtual 99	org/telegram/SQLite/SQLiteCursor:dispose	()V
          //   434: aload 10
          //   436: astore 8
          //   438: iload_1
          //   439: istore 4
          //   441: iload 6
          //   443: istore 5
          //   445: goto -64 -> 381
          //   448: astore 9
          //   450: aload 8
          //   452: astore 7
          //   454: aload 9
          //   456: astore 8
          //   458: aload 7
          //   460: ifnull +8 -> 468
          //   463: aload 7
          //   465: invokevirtual 99	org/telegram/SQLite/SQLiteCursor:dispose	()V
          //   468: aload 8
          //   470: athrow
          //   471: astore 8
          //   473: goto -15 -> 458
          //   476: astore 8
          //   478: aload 9
          //   480: astore 10
          //   482: iload 5
          //   484: istore_1
          //   485: aload 8
          //   487: astore 9
          //   489: goto -85 -> 404
          // Local variable table:
          //   start	length	slot	name	signature
          //   0	492	0	this	14
          //   43	442	1	i	int
          //   25	350	2	j	int
          //   30	342	3	k	int
          //   19	421	4	m	int
          //   22	461	5	n	int
          //   27	415	6	i1	int
          //   66	398	7	localObject1	Object
          //   32	437	8	localObject2	Object
          //   471	1	8	localObject3	Object
          //   476	10	8	localThrowable1	Throwable
          //   1	377	9	localObject4	Object
          //   398	11	9	localThrowable2	Throwable
          //   448	31	9	localObject5	Object
          //   487	1	9	localObject6	Object
          //   39	442	10	localObject7	Object
          //   35	366	11	localObject8	Object
          //   4	297	12	localNativeByteBuffer1	NativeByteBuffer
          //   7	108	13	localObject9	Object
          //   16	372	14	localArrayList	ArrayList
          //   112	75	15	localNativeByteBuffer2	NativeByteBuffer
          // Exception table:
          //   from	to	target	type
          //   44	68	398	java/lang/Throwable
          //   83	91	398	java/lang/Throwable
          //   106	114	398	java/lang/Throwable
          //   138	147	398	java/lang/Throwable
          //   206	214	398	java/lang/Throwable
          //   234	241	398	java/lang/Throwable
          //   263	278	398	java/lang/Throwable
          //   300	305	398	java/lang/Throwable
          //   320	327	398	java/lang/Throwable
          //   341	351	398	java/lang/Throwable
          //   44	68	448	finally
          //   83	91	448	finally
          //   106	114	448	finally
          //   138	147	448	finally
          //   206	214	448	finally
          //   234	241	448	finally
          //   263	278	448	finally
          //   300	305	448	finally
          //   320	327	448	finally
          //   341	351	448	finally
          //   408	413	448	finally
          //   147	154	471	finally
          //   161	179	471	finally
          //   186	191	471	finally
          //   147	154	476	java/lang/Throwable
          //   161	179	476	java/lang/Throwable
          //   186	191	476	java/lang/Throwable
        }
      });
      return;
    }
    final TLRPC.TL_messages_getFeaturedStickers localTL_messages_getFeaturedStickers = new TLRPC.TL_messages_getFeaturedStickers();
    if (paramBoolean2) {}
    for (int i = 0;; i = this.loadFeaturedHash)
    {
      localTL_messages_getFeaturedStickers.hash = i;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getFeaturedStickers, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if ((paramAnonymousTLObject instanceof TLRPC.TL_messages_featuredStickers))
              {
                TLRPC.TL_messages_featuredStickers localTL_messages_featuredStickers = (TLRPC.TL_messages_featuredStickers)paramAnonymousTLObject;
                DataQuery.this.processLoadedFeaturedStickers(localTL_messages_featuredStickers.sets, localTL_messages_featuredStickers.unread, false, (int)(System.currentTimeMillis() / 1000L), localTL_messages_featuredStickers.hash);
                return;
              }
              DataQuery.this.processLoadedFeaturedStickers(null, null, false, (int)(System.currentTimeMillis() / 1000L), DataQuery.15.this.val$req.hash);
            }
          });
        }
      });
      return;
    }
  }
  
  public void loadHints(boolean paramBoolean)
  {
    if (this.loading) {}
    do
    {
      return;
      if (!paramBoolean) {
        break;
      }
    } while (this.loaded);
    this.loading = true;
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        ArrayList localArrayList1 = new ArrayList();
        final ArrayList localArrayList2 = new ArrayList();
        final ArrayList localArrayList3 = new ArrayList();
        final ArrayList localArrayList4 = new ArrayList();
        int i = UserConfig.getInstance(DataQuery.this.currentAccount).getClientUserId();
        ArrayList localArrayList5;
        ArrayList localArrayList6;
        SQLiteCursor localSQLiteCursor;
        for (;;)
        {
          int j;
          int k;
          TLRPC.TL_topPeer localTL_topPeer;
          try
          {
            localArrayList5 = new ArrayList();
            localArrayList6 = new ArrayList();
            localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized("SELECT did, type, rating FROM chat_hints WHERE 1 ORDER BY rating DESC", new Object[0]);
            if (!localSQLiteCursor.next()) {
              break;
            }
            j = localSQLiteCursor.intValue(0);
            if (j == i) {
              continue;
            }
            k = localSQLiteCursor.intValue(1);
            localTL_topPeer = new TLRPC.TL_topPeer();
            localTL_topPeer.rating = localSQLiteCursor.doubleValue(2);
            if (j > 0)
            {
              localTL_topPeer.peer = new TLRPC.TL_peerUser();
              localTL_topPeer.peer.user_id = j;
              localArrayList5.add(Integer.valueOf(j));
              if (k != 0) {
                break label233;
              }
              localArrayList1.add(localTL_topPeer);
              continue;
            }
            localTL_topPeer.peer = new TLRPC.TL_peerChat();
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          localTL_topPeer.peer.chat_id = (-j);
          localArrayList6.add(Integer.valueOf(-j));
          continue;
          label233:
          if (k == 1) {
            localArrayList2.add(localTL_topPeer);
          }
        }
        localSQLiteCursor.dispose();
        if (!localArrayList5.isEmpty()) {
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getUsersInternal(TextUtils.join(",", localArrayList5), localArrayList3);
        }
        if (!localArrayList6.isEmpty()) {
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getChatsInternal(TextUtils.join(",", localArrayList6), localArrayList4);
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MessagesController.getInstance(DataQuery.this.currentAccount).putUsers(localArrayList3, true);
            MessagesController.getInstance(DataQuery.this.currentAccount).putChats(localArrayList4, true);
            DataQuery.this.loading = false;
            DataQuery.this.loaded = true;
            DataQuery.this.hints = localException;
            DataQuery.this.inlineBots = localArrayList2;
            DataQuery.this.buildShortcuts();
            NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.reloadHints, new Object[0]);
            NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.reloadInlineHints, new Object[0]);
            if (Math.abs(UserConfig.getInstance(DataQuery.this.currentAccount).lastHintsSyncTime - (int)(System.currentTimeMillis() / 1000L)) >= 86400) {
              DataQuery.this.loadHints(false);
            }
          }
        });
      }
    });
    this.loaded = true;
    return;
    this.loading = true;
    TLRPC.TL_contacts_getTopPeers localTL_contacts_getTopPeers = new TLRPC.TL_contacts_getTopPeers();
    localTL_contacts_getTopPeers.hash = 0;
    localTL_contacts_getTopPeers.bots_pm = false;
    localTL_contacts_getTopPeers.correspondents = true;
    localTL_contacts_getTopPeers.groups = false;
    localTL_contacts_getTopPeers.channels = false;
    localTL_contacts_getTopPeers.bots_inline = true;
    localTL_contacts_getTopPeers.offset = 0;
    localTL_contacts_getTopPeers.limit = 20;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_getTopPeers, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if ((paramAnonymousTLObject instanceof TLRPC.TL_contacts_topPeers)) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              final TLRPC.TL_contacts_topPeers localTL_contacts_topPeers = (TLRPC.TL_contacts_topPeers)paramAnonymousTLObject;
              MessagesController.getInstance(DataQuery.this.currentAccount).putUsers(localTL_contacts_topPeers.users, false);
              MessagesController.getInstance(DataQuery.this.currentAccount).putChats(localTL_contacts_topPeers.chats, false);
              int i = 0;
              while (i < localTL_contacts_topPeers.categories.size())
              {
                TLRPC.TL_topPeerCategoryPeers localTL_topPeerCategoryPeers = (TLRPC.TL_topPeerCategoryPeers)localTL_contacts_topPeers.categories.get(i);
                if ((localTL_topPeerCategoryPeers.category instanceof TLRPC.TL_topPeerCategoryBotsInline))
                {
                  DataQuery.this.inlineBots = localTL_topPeerCategoryPeers.peers;
                  UserConfig.getInstance(DataQuery.this.currentAccount).botRatingLoadTime = ((int)(System.currentTimeMillis() / 1000L));
                  i += 1;
                }
                else
                {
                  DataQuery.this.hints = localTL_topPeerCategoryPeers.peers;
                  int k = UserConfig.getInstance(DataQuery.this.currentAccount).getClientUserId();
                  int j = 0;
                  for (;;)
                  {
                    if (j < DataQuery.this.hints.size())
                    {
                      if (((TLRPC.TL_topPeer)DataQuery.this.hints.get(j)).peer.user_id == k) {
                        DataQuery.this.hints.remove(j);
                      }
                    }
                    else
                    {
                      UserConfig.getInstance(DataQuery.this.currentAccount).ratingLoadTime = ((int)(System.currentTimeMillis() / 1000L));
                      break;
                    }
                    j += 1;
                  }
                }
              }
              UserConfig.getInstance(DataQuery.this.currentAccount).saveConfig(false);
              DataQuery.this.buildShortcuts();
              NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.reloadHints, new Object[0]);
              NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.reloadInlineHints, new Object[0]);
              MessagesStorage.getInstance(DataQuery.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
              {
                public void run()
                {
                  for (;;)
                  {
                    int j;
                    int k;
                    int m;
                    try
                    {
                      MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("DELETE FROM chat_hints WHERE 1").stepThis().dispose();
                      MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().beginTransaction();
                      MessagesStorage.getInstance(DataQuery.this.currentAccount).putUsersAndChats(localTL_contacts_topPeers.users, localTL_contacts_topPeers.chats, false, false);
                      SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO chat_hints VALUES(?, ?, ?, ?)");
                      j = 0;
                      if (j < localTL_contacts_topPeers.categories.size())
                      {
                        TLRPC.TL_topPeerCategoryPeers localTL_topPeerCategoryPeers = (TLRPC.TL_topPeerCategoryPeers)localTL_contacts_topPeers.categories.get(j);
                        if (!(localTL_topPeerCategoryPeers.category instanceof TLRPC.TL_topPeerCategoryBotsInline)) {
                          break label351;
                        }
                        k = 1;
                        break label345;
                        if (m >= localTL_topPeerCategoryPeers.peers.size()) {
                          break label356;
                        }
                        TLRPC.TL_topPeer localTL_topPeer = (TLRPC.TL_topPeer)localTL_topPeerCategoryPeers.peers.get(m);
                        if ((localTL_topPeer.peer instanceof TLRPC.TL_peerUser))
                        {
                          i = localTL_topPeer.peer.user_id;
                          localSQLitePreparedStatement.requery();
                          localSQLitePreparedStatement.bindInteger(1, i);
                          localSQLitePreparedStatement.bindInteger(2, k);
                          localSQLitePreparedStatement.bindDouble(3, localTL_topPeer.rating);
                          localSQLitePreparedStatement.bindInteger(4, 0);
                          localSQLitePreparedStatement.step();
                          m += 1;
                          continue;
                        }
                        if ((localTL_topPeer.peer instanceof TLRPC.TL_peerChat))
                        {
                          i = -localTL_topPeer.peer.chat_id;
                          continue;
                        }
                        int i = -localTL_topPeer.peer.channel_id;
                        continue;
                      }
                      else
                      {
                        localSQLitePreparedStatement.dispose();
                        MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().commitTransaction();
                        AndroidUtilities.runOnUIThread(new Runnable()
                        {
                          public void run()
                          {
                            UserConfig.getInstance(DataQuery.this.currentAccount).lastHintsSyncTime = ((int)(System.currentTimeMillis() / 1000L));
                            UserConfig.getInstance(DataQuery.this.currentAccount).saveConfig(false);
                          }
                        });
                        return;
                      }
                    }
                    catch (Exception localException)
                    {
                      FileLog.e(localException);
                      return;
                    }
                    for (;;)
                    {
                      label345:
                      m = 0;
                      break;
                      label351:
                      k = 0;
                    }
                    label356:
                    j += 1;
                  }
                }
              });
            }
          });
        }
      }
    });
  }
  
  public void loadMedia(final long paramLong, final int paramInt1, int paramInt2, final int paramInt3, boolean paramBoolean, final int paramInt4)
  {
    if (((int)paramLong < 0) && (ChatObject.isChannel(-(int)paramLong, this.currentAccount))) {}
    int i;
    for (final boolean bool = true;; bool = false)
    {
      i = (int)paramLong;
      if ((!paramBoolean) && (i != 0)) {
        break;
      }
      loadMediaDatabase(paramLong, paramInt1, paramInt2, paramInt3, paramInt4, bool);
      return;
    }
    TLRPC.TL_messages_search localTL_messages_search = new TLRPC.TL_messages_search();
    localTL_messages_search.limit = (paramInt1 + 1);
    localTL_messages_search.offset_id = paramInt2;
    if (paramInt3 == 0) {
      localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterPhotoVideo();
    }
    for (;;)
    {
      localTL_messages_search.q = "";
      localTL_messages_search.peer = MessagesController.getInstance(this.currentAccount).getInputPeer(i);
      if (localTL_messages_search.peer == null) {
        break;
      }
      paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_search, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          boolean bool;
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.messages_Messages)paramAnonymousTLObject;
            if (paramAnonymousTLObject.messages.size() <= paramInt1) {
              break label77;
            }
            bool = false;
            paramAnonymousTLObject.messages.remove(paramAnonymousTLObject.messages.size() - 1);
          }
          for (;;)
          {
            DataQuery.this.processLoadedMedia(paramAnonymousTLObject, paramLong, paramInt1, paramInt3, paramInt4, false, bool, this.val$isChannel, bool);
            return;
            label77:
            bool = true;
          }
        }
      });
      ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, paramInt4);
      return;
      if (paramInt3 == 1) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterDocument();
      } else if (paramInt3 == 2) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterVoice();
      } else if (paramInt3 == 3) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterUrl();
      } else if (paramInt3 == 4) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterMusic();
      }
    }
  }
  
  public void loadMusic(final long paramLong1, long paramLong2)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        final ArrayList localArrayList = new ArrayList();
        try
        {
          if ((int)paramLong1 != 0)
          {
            SQLiteCursor localSQLiteCursor1 = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, mid FROM media_v2 WHERE uid = %d AND mid < %d AND type = %d ORDER BY date DESC, mid DESC LIMIT 1000", new Object[] { Long.valueOf(paramLong1), Long.valueOf(this.val$max_id), Integer.valueOf(4) }), new Object[0]);
            while (localSQLiteCursor1.next())
            {
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor1.byteBufferValue(0);
              if (localNativeByteBuffer != null)
              {
                TLRPC.Message localMessage = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                localMessage.readAttachPath(localNativeByteBuffer, UserConfig.getInstance(DataQuery.this.currentAccount).clientUserId);
                localNativeByteBuffer.reuse();
                if (MessageObject.isMusicMessage(localMessage))
                {
                  localMessage.id = localSQLiteCursor1.intValue(1);
                  localMessage.dialog_id = paramLong1;
                  localArrayList.add(0, new MessageObject(DataQuery.this.currentAccount, localMessage, false));
                  continue;
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.musicDidLoaded, new Object[] { Long.valueOf(DataQuery.41.this.val$uid), localArrayList });
                    }
                  });
                }
              }
            }
          }
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
        for (;;)
        {
          return;
          SQLiteCursor localSQLiteCursor2 = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, mid FROM media_v2 WHERE uid = %d AND mid > %d AND type = %d ORDER BY date DESC, mid DESC LIMIT 1000", new Object[] { Long.valueOf(paramLong1), Long.valueOf(this.val$max_id), Integer.valueOf(4) }), new Object[0]);
          break;
          localSQLiteCursor2.dispose();
        }
      }
    });
  }
  
  public MessageObject loadPinnedMessage(final int paramInt1, final int paramInt2, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          DataQuery.this.loadPinnedMessageInternal(paramInt1, paramInt2, false);
        }
      });
      return null;
    }
    return loadPinnedMessageInternal(paramInt1, paramInt2, true);
  }
  
  public void loadRecents(final int paramInt, final boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    if (paramBoolean1)
    {
      if (this.loadingRecentGifs) {
        return;
      }
      this.loadingRecentGifs = true;
      if (this.recentGifsLoaded) {
        paramBoolean2 = false;
      }
    }
    for (;;)
    {
      if (!paramBoolean2) {
        break label84;
      }
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          for (;;)
          {
            final ArrayList localArrayList;
            try
            {
              if (paramBoolean1)
              {
                i = 2;
                SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized("SELECT document FROM web_recent_v3 WHERE type = " + i + " ORDER BY date DESC", new Object[0]);
                localArrayList = new ArrayList();
                if (!localSQLiteCursor.next()) {
                  break label154;
                }
                if (localSQLiteCursor.isNull(0)) {
                  continue;
                }
                NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
                if (localNativeByteBuffer == null) {
                  continue;
                }
                TLRPC.Document localDocument = TLRPC.Document.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                if (localDocument != null) {
                  localArrayList.add(localDocument);
                }
                localNativeByteBuffer.reuse();
                continue;
              }
              if (paramInt != 0) {
                break label141;
              }
            }
            catch (Throwable localThrowable)
            {
              FileLog.e(localThrowable);
              return;
            }
            int i = 3;
            continue;
            label141:
            if (paramInt == 1)
            {
              i = 4;
              continue;
              label154:
              localThrowable.dispose();
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (DataQuery.8.this.val$gif)
                  {
                    DataQuery.access$302(DataQuery.this, localArrayList);
                    DataQuery.access$402(DataQuery.this, false);
                    DataQuery.access$502(DataQuery.this, true);
                  }
                  for (;;)
                  {
                    NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.recentDocumentsDidLoaded, new Object[] { Boolean.valueOf(DataQuery.8.this.val$gif), Integer.valueOf(DataQuery.8.this.val$type) });
                    DataQuery.this.loadRecents(DataQuery.8.this.val$type, DataQuery.8.this.val$gif, false, false);
                    return;
                    DataQuery.this.recentStickers[DataQuery.8.this.val$type] = localArrayList;
                    DataQuery.this.loadingRecentStickers[DataQuery.8.this.val$type] = 0;
                    DataQuery.this.recentStickersLoaded[DataQuery.8.this.val$type] = 1;
                  }
                }
              });
            }
            else
            {
              i = 5;
            }
          }
        }
      });
      return;
      if (this.loadingRecentStickers[paramInt] != 0) {
        break;
      }
      this.loadingRecentStickers[paramInt] = true;
      if (this.recentStickersLoaded[paramInt] != 0) {
        paramBoolean2 = false;
      }
    }
    label84:
    Object localObject = MessagesController.getEmojiSettings(this.currentAccount);
    if (!paramBoolean3)
    {
      long l;
      if (paramBoolean1) {
        l = ((SharedPreferences)localObject).getLong("lastGifLoadTime", 0L);
      }
      while (Math.abs(System.currentTimeMillis() - l) < 3600000L) {
        if (paramBoolean1)
        {
          this.loadingRecentGifs = false;
          return;
          if (paramInt == 0) {
            l = ((SharedPreferences)localObject).getLong("lastStickersLoadTime", 0L);
          } else if (paramInt == 1) {
            l = ((SharedPreferences)localObject).getLong("lastStickersLoadTimeMask", 0L);
          } else {
            l = ((SharedPreferences)localObject).getLong("lastStickersLoadTimeFavs", 0L);
          }
        }
        else
        {
          this.loadingRecentStickers[paramInt] = false;
          return;
        }
      }
    }
    if (paramBoolean1)
    {
      localObject = new TLRPC.TL_messages_getSavedGifs();
      ((TLRPC.TL_messages_getSavedGifs)localObject).hash = calcDocumentsHash(this.recentGifs);
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          paramAnonymousTL_error = null;
          if ((paramAnonymousTLObject instanceof TLRPC.TL_messages_savedGifs)) {
            paramAnonymousTL_error = ((TLRPC.TL_messages_savedGifs)paramAnonymousTLObject).gifs;
          }
          DataQuery.this.processLoadedRecentDocuments(paramInt, paramAnonymousTL_error, paramBoolean1, 0);
        }
      });
      return;
    }
    if (paramInt == 2)
    {
      localObject = new TLRPC.TL_messages_getFavedStickers();
      ((TLRPC.TL_messages_getFavedStickers)localObject).hash = calcDocumentsHash(this.recentStickers[paramInt]);
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          paramAnonymousTL_error = null;
          if (paramInt == 2) {
            if ((paramAnonymousTLObject instanceof TLRPC.TL_messages_favedStickers)) {
              paramAnonymousTL_error = ((TLRPC.TL_messages_favedStickers)paramAnonymousTLObject).stickers;
            }
          }
          for (;;)
          {
            DataQuery.this.processLoadedRecentDocuments(paramInt, paramAnonymousTL_error, paramBoolean1, 0);
            return;
            if ((paramAnonymousTLObject instanceof TLRPC.TL_messages_recentStickers)) {
              paramAnonymousTL_error = ((TLRPC.TL_messages_recentStickers)paramAnonymousTLObject).stickers;
            }
          }
        }
      });
      return;
    }
    localObject = new TLRPC.TL_messages_getRecentStickers();
    ((TLRPC.TL_messages_getRecentStickers)localObject).hash = calcDocumentsHash(this.recentStickers[paramInt]);
    if (paramInt == 1) {}
    for (paramBoolean2 = true;; paramBoolean2 = false)
    {
      ((TLRPC.TL_messages_getRecentStickers)localObject).attached = paramBoolean2;
      break;
    }
  }
  
  public void loadReplyMessagesForMessages(ArrayList<MessageObject> paramArrayList, final long paramLong)
  {
    final ArrayList localArrayList3;
    final Object localObject1;
    final int i;
    final Object localObject2;
    long l1;
    ArrayList localArrayList2;
    ArrayList localArrayList1;
    if ((int)paramLong == 0)
    {
      localArrayList3 = new ArrayList();
      localObject1 = new LongSparseArray();
      i = 0;
      while (i < paramArrayList.size())
      {
        localObject2 = (MessageObject)paramArrayList.get(i);
        if ((((MessageObject)localObject2).isReply()) && (((MessageObject)localObject2).replyMessageObject == null))
        {
          l1 = ((MessageObject)localObject2).messageOwner.reply_to_random_id;
          localArrayList2 = (ArrayList)((LongSparseArray)localObject1).get(l1);
          localArrayList1 = localArrayList2;
          if (localArrayList2 == null)
          {
            localArrayList1 = new ArrayList();
            ((LongSparseArray)localObject1).put(l1, localArrayList1);
          }
          localArrayList1.add(localObject2);
          if (!localArrayList3.contains(Long.valueOf(l1))) {
            localArrayList3.add(Long.valueOf(l1));
          }
        }
        i += 1;
      }
      if (!localArrayList3.isEmpty()) {}
    }
    do
    {
      return;
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          for (;;)
          {
            int i;
            try
            {
              Object localObject1 = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT m.data, m.mid, m.date, r.random_id FROM randoms as r INNER JOIN messages as m ON r.mid = m.mid WHERE r.random_id IN(%s)", new Object[] { TextUtils.join(",", localArrayList3) }), new Object[0]);
              if (((SQLiteCursor)localObject1).next())
              {
                Object localObject2 = ((SQLiteCursor)localObject1).byteBufferValue(0);
                if (localObject2 == null) {
                  continue;
                }
                Object localObject3 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject2, ((NativeByteBuffer)localObject2).readInt32(false), false);
                ((TLRPC.Message)localObject3).readAttachPath((AbstractSerializedData)localObject2, UserConfig.getInstance(DataQuery.this.currentAccount).clientUserId);
                ((NativeByteBuffer)localObject2).reuse();
                ((TLRPC.Message)localObject3).id = ((SQLiteCursor)localObject1).intValue(1);
                ((TLRPC.Message)localObject3).date = ((SQLiteCursor)localObject1).intValue(2);
                ((TLRPC.Message)localObject3).dialog_id = paramLong;
                long l = ((SQLiteCursor)localObject1).longValue(3);
                localObject2 = (ArrayList)this.val$replyMessageRandomOwners.get(l);
                this.val$replyMessageRandomOwners.remove(l);
                if (localObject2 == null) {
                  continue;
                }
                localObject3 = new MessageObject(DataQuery.this.currentAccount, (TLRPC.Message)localObject3, false);
                i = 0;
                if (i >= ((ArrayList)localObject2).size()) {
                  continue;
                }
                Object localObject4 = (MessageObject)((ArrayList)localObject2).get(i);
                ((MessageObject)localObject4).replyMessageObject = ((MessageObject)localObject3);
                ((MessageObject)localObject4).messageOwner.reply_to_msg_id = ((MessageObject)localObject3).getId();
                if (((MessageObject)localObject4).isMegagroup())
                {
                  localObject4 = ((MessageObject)localObject4).replyMessageObject.messageOwner;
                  ((TLRPC.Message)localObject4).flags |= 0x80000000;
                }
              }
              else
              {
                ((SQLiteCursor)localObject1).dispose();
                if (this.val$replyMessageRandomOwners.size() != 0)
                {
                  i = 0;
                  if (i < this.val$replyMessageRandomOwners.size())
                  {
                    localObject1 = (ArrayList)this.val$replyMessageRandomOwners.valueAt(i);
                    int j = 0;
                    if (j >= ((ArrayList)localObject1).size()) {
                      break label367;
                    }
                    ((MessageObject)((ArrayList)localObject1).get(j)).messageOwner.reply_to_random_id = 0L;
                    j += 1;
                    continue;
                  }
                }
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.didLoadedReplyMessages, new Object[] { Long.valueOf(DataQuery.56.this.val$dialogId) });
                  }
                });
                return;
              }
            }
            catch (Exception localException)
            {
              FileLog.e(localException);
              return;
            }
            i += 1;
            continue;
            label367:
            i += 1;
          }
        }
      });
      return;
      localArrayList3 = new ArrayList();
      localObject1 = new SparseArray();
      localObject2 = new StringBuilder();
      i = 0;
      int j = 0;
      while (j < paramArrayList.size())
      {
        MessageObject localMessageObject = (MessageObject)paramArrayList.get(j);
        int k = i;
        if (localMessageObject.getId() > 0)
        {
          k = i;
          if (localMessageObject.isReply())
          {
            k = i;
            if (localMessageObject.replyMessageObject == null)
            {
              int m = localMessageObject.messageOwner.reply_to_msg_id;
              long l2 = m;
              l1 = l2;
              if (localMessageObject.messageOwner.to_id.channel_id != 0)
              {
                l1 = l2 | localMessageObject.messageOwner.to_id.channel_id << 32;
                i = localMessageObject.messageOwner.to_id.channel_id;
              }
              if (((StringBuilder)localObject2).length() > 0) {
                ((StringBuilder)localObject2).append(',');
              }
              ((StringBuilder)localObject2).append(l1);
              localArrayList2 = (ArrayList)((SparseArray)localObject1).get(m);
              localArrayList1 = localArrayList2;
              if (localArrayList2 == null)
              {
                localArrayList1 = new ArrayList();
                ((SparseArray)localObject1).put(m, localArrayList1);
              }
              localArrayList1.add(localMessageObject);
              k = i;
              if (!localArrayList3.contains(Integer.valueOf(m)))
              {
                localArrayList3.add(Integer.valueOf(m));
                k = i;
              }
            }
          }
        }
        j += 1;
        i = k;
      }
    } while (localArrayList3.isEmpty());
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        do
        {
          ArrayList localArrayList2;
          ArrayList localArrayList3;
          ArrayList localArrayList4;
          ArrayList localArrayList5;
          try
          {
            ArrayList localArrayList1 = new ArrayList();
            localArrayList2 = new ArrayList();
            localArrayList3 = new ArrayList();
            localArrayList4 = new ArrayList();
            localArrayList5 = new ArrayList();
            SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, mid, date FROM messages WHERE mid IN(%s)", new Object[] { localObject2.toString() }), new Object[0]);
            while (localSQLiteCursor.next())
            {
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
              if (localNativeByteBuffer != null)
              {
                TLRPC.Message localMessage = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                localMessage.readAttachPath(localNativeByteBuffer, UserConfig.getInstance(DataQuery.this.currentAccount).clientUserId);
                localNativeByteBuffer.reuse();
                localMessage.id = localSQLiteCursor.intValue(1);
                localMessage.date = localSQLiteCursor.intValue(2);
                localMessage.dialog_id = paramLong;
                MessagesStorage.addUsersAndChatsFromMessage(localMessage, localArrayList4, localArrayList5);
                localArrayList1.add(localMessage);
                localObject1.remove(Integer.valueOf(localMessage.id));
              }
            }
            localSQLiteCursor.dispose();
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          if (!localArrayList4.isEmpty()) {
            MessagesStorage.getInstance(DataQuery.this.currentAccount).getUsersInternal(TextUtils.join(",", localArrayList4), localArrayList2);
          }
          if (!localArrayList5.isEmpty()) {
            MessagesStorage.getInstance(DataQuery.this.currentAccount).getChatsInternal(TextUtils.join(",", localArrayList5), localArrayList3);
          }
          DataQuery.this.broadcastReplyMessages(localException, i, localArrayList2, localArrayList3, paramLong, true);
        } while (localObject1.isEmpty());
        if (this.val$channelIdFinal != 0)
        {
          localObject = new TLRPC.TL_channels_getMessages();
          ((TLRPC.TL_channels_getMessages)localObject).channel = MessagesController.getInstance(DataQuery.this.currentAccount).getInputChannel(this.val$channelIdFinal);
          ((TLRPC.TL_channels_getMessages)localObject).id = localObject1;
          ConnectionsManager.getInstance(DataQuery.this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
          {
            public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
            {
              if (paramAnonymous2TL_error == null)
              {
                paramAnonymous2TLObject = (TLRPC.messages_Messages)paramAnonymous2TLObject;
                DataQuery.removeEmptyMessages(paramAnonymous2TLObject.messages);
                ImageLoader.saveMessagesThumbs(paramAnonymous2TLObject.messages);
                DataQuery.this.broadcastReplyMessages(paramAnonymous2TLObject.messages, DataQuery.57.this.val$replyMessageOwners, paramAnonymous2TLObject.users, paramAnonymous2TLObject.chats, DataQuery.57.this.val$dialogId, false);
                MessagesStorage.getInstance(DataQuery.this.currentAccount).putUsersAndChats(paramAnonymous2TLObject.users, paramAnonymous2TLObject.chats, true, true);
                DataQuery.this.saveReplyMessages(DataQuery.57.this.val$replyMessageOwners, paramAnonymous2TLObject.messages);
              }
            }
          });
          return;
        }
        Object localObject = new TLRPC.TL_messages_getMessages();
        ((TLRPC.TL_messages_getMessages)localObject).id = localObject1;
        ConnectionsManager.getInstance(DataQuery.this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
        {
          public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
          {
            if (paramAnonymous2TL_error == null)
            {
              paramAnonymous2TLObject = (TLRPC.messages_Messages)paramAnonymous2TLObject;
              DataQuery.removeEmptyMessages(paramAnonymous2TLObject.messages);
              ImageLoader.saveMessagesThumbs(paramAnonymous2TLObject.messages);
              DataQuery.this.broadcastReplyMessages(paramAnonymous2TLObject.messages, DataQuery.57.this.val$replyMessageOwners, paramAnonymous2TLObject.users, paramAnonymous2TLObject.chats, DataQuery.57.this.val$dialogId, false);
              MessagesStorage.getInstance(DataQuery.this.currentAccount).putUsersAndChats(paramAnonymous2TLObject.users, paramAnonymous2TLObject.chats, true, true);
              DataQuery.this.saveReplyMessages(DataQuery.57.this.val$replyMessageOwners, paramAnonymous2TLObject.messages);
            }
          }
        });
      }
    });
  }
  
  public void loadStickers(final int paramInt, boolean paramBoolean1, boolean paramBoolean2)
  {
    int j = 0;
    final int i = 0;
    if (this.loadingStickers[paramInt] != 0) {}
    do
    {
      return;
      if (paramInt != 3) {
        break;
      }
    } while ((this.featuredStickerSets.isEmpty()) || (!MessagesController.getInstance(this.currentAccount).preloadFeaturedStickers));
    for (;;)
    {
      this.loadingStickers[paramInt] = true;
      if (!paramBoolean1) {
        break;
      }
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        /* Error */
        public void run()
        {
          // Byte code:
          //   0: aconst_null
          //   1: astore 9
          //   3: aconst_null
          //   4: astore 12
          //   6: aconst_null
          //   7: astore 13
          //   9: iconst_0
          //   10: istore 4
          //   12: iconst_0
          //   13: istore 5
          //   15: iconst_0
          //   16: istore_2
          //   17: iconst_0
          //   18: istore 6
          //   20: iconst_0
          //   21: istore_3
          //   22: aconst_null
          //   23: astore 8
          //   25: aconst_null
          //   26: astore 11
          //   28: aload 12
          //   30: astore 10
          //   32: iload 4
          //   34: istore_1
          //   35: aload_0
          //   36: getfield 19	org/telegram/messenger/DataQuery$24:this$0	Lorg/telegram/messenger/DataQuery;
          //   39: invokestatic 32	org/telegram/messenger/DataQuery:access$000	(Lorg/telegram/messenger/DataQuery;)I
          //   42: invokestatic 38	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
          //   45: invokevirtual 42	org/telegram/messenger/MessagesStorage:getDatabase	()Lorg/telegram/SQLite/SQLiteDatabase;
          //   48: new 44	java/lang/StringBuilder
          //   51: dup
          //   52: invokespecial 45	java/lang/StringBuilder:<init>	()V
          //   55: ldc 47
          //   57: invokevirtual 51	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
          //   60: aload_0
          //   61: getfield 21	org/telegram/messenger/DataQuery$24:val$type	I
          //   64: iconst_1
          //   65: iadd
          //   66: invokevirtual 54	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
          //   69: invokevirtual 58	java/lang/StringBuilder:toString	()Ljava/lang/String;
          //   72: iconst_0
          //   73: anewarray 4	java/lang/Object
          //   76: invokevirtual 64	org/telegram/SQLite/SQLiteDatabase:queryFinalized	(Ljava/lang/String;[Ljava/lang/Object;)Lorg/telegram/SQLite/SQLiteCursor;
          //   79: astore 7
          //   81: aload 12
          //   83: astore 10
          //   85: iload 4
          //   87: istore_1
          //   88: aload 7
          //   90: astore 11
          //   92: aload 7
          //   94: astore 8
          //   96: aload 7
          //   98: invokevirtual 70	org/telegram/SQLite/SQLiteCursor:next	()Z
          //   101: ifeq +145 -> 246
          //   104: aload 12
          //   106: astore 10
          //   108: iload 4
          //   110: istore_1
          //   111: aload 7
          //   113: astore 11
          //   115: aload 7
          //   117: astore 8
          //   119: aload 7
          //   121: iconst_0
          //   122: invokevirtual 74	org/telegram/SQLite/SQLiteCursor:byteBufferValue	(I)Lorg/telegram/tgnet/NativeByteBuffer;
          //   125: astore 14
          //   127: aload 13
          //   129: astore 9
          //   131: aload 14
          //   133: ifnull +71 -> 204
          //   136: aload 12
          //   138: astore 10
          //   140: iload 4
          //   142: istore_1
          //   143: aload 7
          //   145: astore 11
          //   147: aload 7
          //   149: astore 8
          //   151: new 76	java/util/ArrayList
          //   154: dup
          //   155: invokespecial 77	java/util/ArrayList:<init>	()V
          //   158: astore 9
          //   160: aload 14
          //   162: iconst_0
          //   163: invokevirtual 83	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
          //   166: istore_2
          //   167: iconst_0
          //   168: istore_1
          //   169: iload_1
          //   170: iload_2
          //   171: if_icmpge +28 -> 199
          //   174: aload 9
          //   176: aload 14
          //   178: aload 14
          //   180: iconst_0
          //   181: invokevirtual 83	org/telegram/tgnet/NativeByteBuffer:readInt32	(Z)I
          //   184: iconst_0
          //   185: invokestatic 89	org/telegram/tgnet/TLRPC$TL_messages_stickerSet:TLdeserialize	(Lorg/telegram/tgnet/AbstractSerializedData;IZ)Lorg/telegram/tgnet/TLRPC$TL_messages_stickerSet;
          //   188: invokevirtual 93	java/util/ArrayList:add	(Ljava/lang/Object;)Z
          //   191: pop
          //   192: iload_1
          //   193: iconst_1
          //   194: iadd
          //   195: istore_1
          //   196: goto -27 -> 169
          //   199: aload 14
          //   201: invokevirtual 96	org/telegram/tgnet/NativeByteBuffer:reuse	()V
          //   204: aload 9
          //   206: astore 10
          //   208: iload 4
          //   210: istore_1
          //   211: aload 7
          //   213: astore 11
          //   215: aload 7
          //   217: astore 8
          //   219: aload 7
          //   221: iconst_1
          //   222: invokevirtual 100	org/telegram/SQLite/SQLiteCursor:intValue	(I)I
          //   225: istore_2
          //   226: aload 9
          //   228: astore 10
          //   230: iload_2
          //   231: istore_1
          //   232: aload 7
          //   234: astore 11
          //   236: aload 7
          //   238: astore 8
          //   240: aload 9
          //   242: invokestatic 104	org/telegram/messenger/DataQuery:access$2300	(Ljava/util/ArrayList;)I
          //   245: istore_3
          //   246: aload 9
          //   248: astore 8
          //   250: iload_2
          //   251: istore 4
          //   253: iload_3
          //   254: istore 5
          //   256: aload 7
          //   258: ifnull +18 -> 276
          //   261: aload 7
          //   263: invokevirtual 107	org/telegram/SQLite/SQLiteCursor:dispose	()V
          //   266: iload_3
          //   267: istore 5
          //   269: iload_2
          //   270: istore 4
          //   272: aload 9
          //   274: astore 8
          //   276: aload_0
          //   277: getfield 19	org/telegram/messenger/DataQuery$24:this$0	Lorg/telegram/messenger/DataQuery;
          //   280: aload_0
          //   281: getfield 21	org/telegram/messenger/DataQuery$24:val$type	I
          //   284: aload 8
          //   286: iconst_1
          //   287: iload 4
          //   289: iload 5
          //   291: invokestatic 111	org/telegram/messenger/DataQuery:access$2200	(Lorg/telegram/messenger/DataQuery;ILjava/util/ArrayList;ZII)V
          //   294: return
          //   295: astore 9
          //   297: aload 11
          //   299: astore 7
          //   301: aload 7
          //   303: astore 8
          //   305: aload 9
          //   307: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
          //   310: aload 10
          //   312: astore 8
          //   314: iload_1
          //   315: istore 4
          //   317: iload 6
          //   319: istore 5
          //   321: aload 7
          //   323: ifnull -47 -> 276
          //   326: aload 7
          //   328: invokevirtual 107	org/telegram/SQLite/SQLiteCursor:dispose	()V
          //   331: aload 10
          //   333: astore 8
          //   335: iload_1
          //   336: istore 4
          //   338: iload 6
          //   340: istore 5
          //   342: goto -66 -> 276
          //   345: astore 9
          //   347: aload 8
          //   349: astore 7
          //   351: aload 9
          //   353: astore 8
          //   355: aload 7
          //   357: ifnull +8 -> 365
          //   360: aload 7
          //   362: invokevirtual 107	org/telegram/SQLite/SQLiteCursor:dispose	()V
          //   365: aload 8
          //   367: athrow
          //   368: astore 8
          //   370: goto -15 -> 355
          //   373: astore 8
          //   375: aload 9
          //   377: astore 10
          //   379: iload 5
          //   381: istore_1
          //   382: aload 8
          //   384: astore 9
          //   386: goto -85 -> 301
          // Local variable table:
          //   start	length	slot	name	signature
          //   0	389	0	this	24
          //   34	348	1	i	int
          //   16	254	2	j	int
          //   21	246	3	k	int
          //   10	327	4	m	int
          //   13	367	5	n	int
          //   18	321	6	i1	int
          //   79	282	7	localObject1	Object
          //   23	343	8	localObject2	Object
          //   368	1	8	localObject3	Object
          //   373	10	8	localThrowable1	Throwable
          //   1	272	9	localObject4	Object
          //   295	11	9	localThrowable2	Throwable
          //   345	31	9	localObject5	Object
          //   384	1	9	localObject6	Object
          //   30	348	10	localObject7	Object
          //   26	272	11	localObject8	Object
          //   4	133	12	localObject9	Object
          //   7	121	13	localObject10	Object
          //   125	75	14	localNativeByteBuffer	NativeByteBuffer
          // Exception table:
          //   from	to	target	type
          //   35	81	295	java/lang/Throwable
          //   96	104	295	java/lang/Throwable
          //   119	127	295	java/lang/Throwable
          //   151	160	295	java/lang/Throwable
          //   219	226	295	java/lang/Throwable
          //   240	246	295	java/lang/Throwable
          //   35	81	345	finally
          //   96	104	345	finally
          //   119	127	345	finally
          //   151	160	345	finally
          //   219	226	345	finally
          //   240	246	345	finally
          //   305	310	345	finally
          //   160	167	368	finally
          //   174	192	368	finally
          //   199	204	368	finally
          //   160	167	373	java/lang/Throwable
          //   174	192	373	java/lang/Throwable
          //   199	204	373	java/lang/Throwable
        }
      });
      return;
      loadArchivedStickersCount(paramInt, paramBoolean1);
    }
    if (paramInt == 3)
    {
      localObject1 = new TLRPC.TL_messages_allStickers();
      ((TLRPC.TL_messages_allStickers)localObject1).hash = this.loadFeaturedHash;
      i = 0;
      j = this.featuredStickerSets.size();
      while (i < j)
      {
        ((TLRPC.TL_messages_allStickers)localObject1).sets.add(((TLRPC.StickerSetCovered)this.featuredStickerSets.get(i)).set);
        i += 1;
      }
      processLoadStickersResponse(paramInt, (TLRPC.TL_messages_allStickers)localObject1);
      return;
    }
    if (paramInt == 0)
    {
      localObject1 = new TLRPC.TL_messages_getAllStickers();
      localObject2 = (TLRPC.TL_messages_getAllStickers)localObject1;
      if (paramBoolean2) {}
      for (;;)
      {
        ((TLRPC.TL_messages_getAllStickers)localObject2).hash = i;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                if ((paramAnonymousTLObject instanceof TLRPC.TL_messages_allStickers))
                {
                  DataQuery.this.processLoadStickersResponse(DataQuery.25.this.val$type, (TLRPC.TL_messages_allStickers)paramAnonymousTLObject);
                  return;
                }
                DataQuery.this.processLoadedStickers(DataQuery.25.this.val$type, null, false, (int)(System.currentTimeMillis() / 1000L), DataQuery.25.this.val$hash);
              }
            });
          }
        });
        return;
        i = this.loadHash[paramInt];
      }
    }
    Object localObject1 = new TLRPC.TL_messages_getMaskStickers();
    Object localObject2 = (TLRPC.TL_messages_getMaskStickers)localObject1;
    if (paramBoolean2) {}
    for (i = j;; i = this.loadHash[paramInt])
    {
      ((TLRPC.TL_messages_getMaskStickers)localObject2).hash = i;
      break;
    }
  }
  
  public void markFaturedStickersAsRead(boolean paramBoolean)
  {
    if (this.unreadStickerSets.isEmpty()) {}
    do
    {
      return;
      this.unreadStickerSets.clear();
      this.loadFeaturedHash = calcFeaturedStickersHash(this.featuredStickerSets);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.featuredStickersDidLoaded, new Object[0]);
      putFeaturedStickersToCache(this.featuredStickerSets, this.unreadStickerSets, this.loadFeaturedDate, this.loadFeaturedHash);
    } while (!paramBoolean);
    TLRPC.TL_messages_readFeaturedStickers localTL_messages_readFeaturedStickers = new TLRPC.TL_messages_readFeaturedStickers();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_readFeaturedStickers, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  public void markFaturedStickersByIdAsRead(final long paramLong)
  {
    if ((!this.unreadStickerSets.contains(Long.valueOf(paramLong))) || (this.readingStickerSets.contains(Long.valueOf(paramLong)))) {
      return;
    }
    this.readingStickerSets.add(Long.valueOf(paramLong));
    TLRPC.TL_messages_readFeaturedStickers localTL_messages_readFeaturedStickers = new TLRPC.TL_messages_readFeaturedStickers();
    localTL_messages_readFeaturedStickers.id.add(Long.valueOf(paramLong));
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_readFeaturedStickers, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        DataQuery.this.unreadStickerSets.remove(Long.valueOf(paramLong));
        DataQuery.this.readingStickerSets.remove(Long.valueOf(paramLong));
        DataQuery.access$1402(DataQuery.this, DataQuery.this.calcFeaturedStickersHash(DataQuery.this.featuredStickerSets));
        NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.featuredStickersDidLoaded, new Object[0]);
        DataQuery.this.putFeaturedStickersToCache(DataQuery.this.featuredStickerSets, DataQuery.this.unreadStickerSets, DataQuery.this.loadFeaturedDate, DataQuery.this.loadFeaturedHash);
      }
    }, 1000L);
  }
  
  public void putBotInfo(final TLRPC.BotInfo paramBotInfo)
  {
    if (paramBotInfo == null) {
      return;
    }
    this.botInfos.put(paramBotInfo.user_id, paramBotInfo);
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("REPLACE INTO bot_info(uid, info) VALUES(?, ?)");
          localSQLitePreparedStatement.requery();
          NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(paramBotInfo.getObjectSize());
          paramBotInfo.serializeToStream(localNativeByteBuffer);
          localSQLitePreparedStatement.bindInteger(1, paramBotInfo.user_id);
          localSQLitePreparedStatement.bindByteBuffer(2, localNativeByteBuffer);
          localSQLitePreparedStatement.step();
          localNativeByteBuffer.reuse();
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
  
  public void putBotKeyboard(final long paramLong, TLRPC.Message paramMessage)
  {
    if (paramMessage == null) {}
    for (;;)
    {
      return;
      int i = 0;
      try
      {
        Object localObject = MessagesStorage.getInstance(this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT mid FROM bot_keyboard WHERE uid = %d", new Object[] { Long.valueOf(paramLong) }), new Object[0]);
        if (((SQLiteCursor)localObject).next()) {
          i = ((SQLiteCursor)localObject).intValue(0);
        }
        ((SQLiteCursor)localObject).dispose();
        if (i < paramMessage.id)
        {
          localObject = MessagesStorage.getInstance(this.currentAccount).getDatabase().executeFast("REPLACE INTO bot_keyboard VALUES(?, ?, ?)");
          ((SQLitePreparedStatement)localObject).requery();
          NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(paramMessage.getObjectSize());
          paramMessage.serializeToStream(localNativeByteBuffer);
          ((SQLitePreparedStatement)localObject).bindLong(1, paramLong);
          ((SQLitePreparedStatement)localObject).bindInteger(2, paramMessage.id);
          ((SQLitePreparedStatement)localObject).bindByteBuffer(3, localNativeByteBuffer);
          ((SQLitePreparedStatement)localObject).step();
          localNativeByteBuffer.reuse();
          ((SQLitePreparedStatement)localObject).dispose();
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              TLRPC.Message localMessage = (TLRPC.Message)DataQuery.this.botKeyboards.get(paramLong);
              DataQuery.this.botKeyboards.put(paramLong, this.val$message);
              if (localMessage != null) {
                DataQuery.this.botKeyboardsByMids.delete(localMessage.id);
              }
              DataQuery.this.botKeyboardsByMids.put(this.val$message.id, paramLong);
              NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.botKeyboardDidLoaded, new Object[] { this.val$message, Long.valueOf(paramLong) });
            }
          });
          return;
        }
      }
      catch (Exception paramMessage)
      {
        FileLog.e(paramMessage);
      }
    }
  }
  
  public void putGroupStickerSet(TLRPC.TL_messages_stickerSet paramTL_messages_stickerSet)
  {
    this.groupStickerSets.put(paramTL_messages_stickerSet.set.id, paramTL_messages_stickerSet);
  }
  
  public void removeInline(int paramInt)
  {
    int i = 0;
    for (;;)
    {
      if (i < this.inlineBots.size())
      {
        if (((TLRPC.TL_topPeer)this.inlineBots.get(i)).peer.user_id == paramInt)
        {
          this.inlineBots.remove(i);
          TLRPC.TL_contacts_resetTopPeerRating localTL_contacts_resetTopPeerRating = new TLRPC.TL_contacts_resetTopPeerRating();
          localTL_contacts_resetTopPeerRating.category = new TLRPC.TL_topPeerCategoryBotsInline();
          localTL_contacts_resetTopPeerRating.peer = MessagesController.getInstance(this.currentAccount).getInputPeer(paramInt);
          ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_resetTopPeerRating, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
          });
          deletePeer(paramInt, 1);
          NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.reloadInlineHints, new Object[0]);
        }
      }
      else {
        return;
      }
      i += 1;
    }
  }
  
  public void removePeer(int paramInt)
  {
    int i = 0;
    for (;;)
    {
      if (i < this.hints.size())
      {
        if (((TLRPC.TL_topPeer)this.hints.get(i)).peer.user_id == paramInt)
        {
          this.hints.remove(i);
          NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.reloadHints, new Object[0]);
          TLRPC.TL_contacts_resetTopPeerRating localTL_contacts_resetTopPeerRating = new TLRPC.TL_contacts_resetTopPeerRating();
          localTL_contacts_resetTopPeerRating.category = new TLRPC.TL_topPeerCategoryCorrespondents();
          localTL_contacts_resetTopPeerRating.peer = MessagesController.getInstance(this.currentAccount).getInputPeer(paramInt);
          deletePeer(paramInt, 0);
          ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_resetTopPeerRating, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
          });
        }
      }
      else {
        return;
      }
      i += 1;
    }
  }
  
  public void removeRecentGif(final TLRPC.Document paramDocument)
  {
    this.recentGifs.remove(paramDocument);
    TLRPC.TL_messages_saveGif localTL_messages_saveGif = new TLRPC.TL_messages_saveGif();
    localTL_messages_saveGif.id = new TLRPC.TL_inputDocument();
    localTL_messages_saveGif.id.id = paramDocument.id;
    localTL_messages_saveGif.id.access_hash = paramDocument.access_hash;
    localTL_messages_saveGif.unsave = true;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_saveGif, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().executeFast("DELETE FROM web_recent_v3 WHERE id = '" + paramDocument.id + "' AND type = 2").stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void removeStickersSet(final Context paramContext, final TLRPC.StickerSet paramStickerSet, final int paramInt, final BaseFragment paramBaseFragment, final boolean paramBoolean)
  {
    final int i;
    TLRPC.TL_inputStickerSetID localTL_inputStickerSetID;
    label49:
    int j;
    if (paramStickerSet.masks)
    {
      i = 1;
      localTL_inputStickerSetID = new TLRPC.TL_inputStickerSetID();
      localTL_inputStickerSetID.access_hash = paramStickerSet.access_hash;
      localTL_inputStickerSetID.id = paramStickerSet.id;
      if (paramInt == 0) {
        break label332;
      }
      if (paramInt != 1) {
        break label265;
      }
      bool = true;
      paramStickerSet.archived = bool;
      j = 0;
      label58:
      if (j < this.stickerSets[i].size())
      {
        paramContext = (TLRPC.TL_messages_stickerSet)this.stickerSets[i].get(j);
        if (paramContext.set.id != paramStickerSet.id) {
          break label317;
        }
        this.stickerSets[i].remove(j);
        if (paramInt != 2) {
          break label271;
        }
        this.stickerSets[i].add(0, paramContext);
      }
      label134:
      this.loadHash[i] = calcStickersHash(this.stickerSets[i]);
      putStickersToCache(i, this.stickerSets[i], this.loadDate[i], this.loadHash[i]);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.stickersDidLoaded, new Object[] { Integer.valueOf(i) });
      paramContext = new TLRPC.TL_messages_installStickerSet();
      paramContext.stickerset = localTL_inputStickerSetID;
      if (paramInt != 1) {
        break label326;
      }
    }
    label265:
    label271:
    label317:
    label326:
    for (boolean bool = true;; bool = false)
    {
      paramContext.archived = bool;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramContext, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              Activity localActivity;
              if ((paramAnonymousTLObject instanceof TLRPC.TL_messages_stickerSetInstallResultArchive))
              {
                NotificationCenter.getInstance(DataQuery.this.currentAccount).postNotificationName(NotificationCenter.needReloadArchivedStickers, new Object[] { Integer.valueOf(DataQuery.29.this.val$type) });
                if ((DataQuery.29.this.val$hide != 1) && (DataQuery.29.this.val$baseFragment != null) && (DataQuery.29.this.val$baseFragment.getParentActivity() != null))
                {
                  localActivity = DataQuery.29.this.val$baseFragment.getParentActivity();
                  if (!DataQuery.29.this.val$showSettings) {
                    break label145;
                  }
                }
              }
              label145:
              for (Object localObject = DataQuery.29.this.val$baseFragment;; localObject = null)
              {
                localObject = new StickersArchiveAlert(localActivity, (BaseFragment)localObject, ((TLRPC.TL_messages_stickerSetInstallResultArchive)paramAnonymousTLObject).sets);
                DataQuery.29.this.val$baseFragment.showDialog(((StickersArchiveAlert)localObject).create());
                return;
              }
            }
          });
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              DataQuery.this.loadStickers(DataQuery.29.this.val$type, false, false);
            }
          }, 1000L);
        }
      });
      return;
      i = 0;
      break;
      bool = false;
      break label49;
      this.stickerSetsById.remove(paramContext.set.id);
      this.installedStickerSetsById.remove(paramContext.set.id);
      this.stickerSetsByName.remove(paramContext.set.short_name);
      break label134;
      j += 1;
      break label58;
    }
    label332:
    paramBaseFragment = new TLRPC.TL_messages_uninstallStickerSet();
    paramBaseFragment.stickerset = localTL_inputStickerSetID;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramBaseFragment, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            try
            {
              if (paramAnonymousTL_error == null)
              {
                if (DataQuery.30.this.val$stickerSet.masks) {
                  Toast.makeText(DataQuery.30.this.val$context, LocaleController.getString("MasksRemoved", 2131493791), 0).show();
                }
                for (;;)
                {
                  DataQuery.this.loadStickers(DataQuery.30.this.val$type, false, true);
                  return;
                  Toast.makeText(DataQuery.30.this.val$context, LocaleController.getString("StickersRemoved", 2131494428), 0).show();
                }
              }
            }
            catch (Exception localException)
            {
              for (;;)
              {
                FileLog.e(localException);
                continue;
                Toast.makeText(DataQuery.30.this.val$context, LocaleController.getString("ErrorOccurred", 2131493453), 0).show();
              }
            }
          }
        });
      }
    });
  }
  
  public void reorderStickers(int paramInt, final ArrayList<Long> paramArrayList)
  {
    Collections.sort(this.stickerSets[paramInt], new Comparator()
    {
      public int compare(TLRPC.TL_messages_stickerSet paramAnonymousTL_messages_stickerSet1, TLRPC.TL_messages_stickerSet paramAnonymousTL_messages_stickerSet2)
      {
        int i = paramArrayList.indexOf(Long.valueOf(paramAnonymousTL_messages_stickerSet1.set.id));
        int j = paramArrayList.indexOf(Long.valueOf(paramAnonymousTL_messages_stickerSet2.set.id));
        if (i > j) {
          return 1;
        }
        if (i < j) {
          return -1;
        }
        return 0;
      }
    });
    this.loadHash[paramInt] = calcStickersHash(this.stickerSets[paramInt]);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.stickersDidLoaded, new Object[] { Integer.valueOf(paramInt) });
    loadStickers(paramInt, false, true);
  }
  
  public void saveDraft(long paramLong, CharSequence paramCharSequence, ArrayList<TLRPC.MessageEntity> paramArrayList, TLRPC.Message paramMessage, boolean paramBoolean)
  {
    saveDraft(paramLong, paramCharSequence, paramArrayList, paramMessage, paramBoolean, false);
  }
  
  public void saveDraft(long paramLong, CharSequence paramCharSequence, ArrayList<TLRPC.MessageEntity> paramArrayList, TLRPC.Message paramMessage, boolean paramBoolean1, boolean paramBoolean2)
  {
    Object localObject;
    if ((!TextUtils.isEmpty(paramCharSequence)) || (paramMessage != null))
    {
      localObject = new TLRPC.TL_draftMessage();
      ((TLRPC.DraftMessage)localObject).date = ((int)(System.currentTimeMillis() / 1000L));
      if (paramCharSequence != null) {
        break label211;
      }
      paramCharSequence = "";
      label42:
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
      paramCharSequence = (TLRPC.DraftMessage)this.drafts.get(paramLong);
      if ((paramBoolean2) || (((paramCharSequence == null) || (!paramCharSequence.message.equals(((TLRPC.DraftMessage)localObject).message)) || (paramCharSequence.reply_to_msg_id != ((TLRPC.DraftMessage)localObject).reply_to_msg_id) || (paramCharSequence.no_webpage != ((TLRPC.DraftMessage)localObject).no_webpage)) && ((paramCharSequence != null) || (!TextUtils.isEmpty(((TLRPC.DraftMessage)localObject).message)) || (((TLRPC.DraftMessage)localObject).reply_to_msg_id != 0)))) {
        break label221;
      }
    }
    label211:
    label221:
    do
    {
      return;
      localObject = new TLRPC.TL_draftMessageEmpty();
      break;
      paramCharSequence = paramCharSequence.toString();
      break label42;
      saveDraft(paramLong, (TLRPC.DraftMessage)localObject, paramMessage, false);
      int i = (int)paramLong;
      if (i == 0) {
        break label336;
      }
      paramCharSequence = new TLRPC.TL_messages_saveDraft();
      paramCharSequence.peer = MessagesController.getInstance(this.currentAccount).getInputPeer(i);
    } while (paramCharSequence.peer == null);
    paramCharSequence.message = ((TLRPC.DraftMessage)localObject).message;
    paramCharSequence.no_webpage = ((TLRPC.DraftMessage)localObject).no_webpage;
    paramCharSequence.reply_to_msg_id = ((TLRPC.DraftMessage)localObject).reply_to_msg_id;
    paramCharSequence.entities = ((TLRPC.DraftMessage)localObject).entities;
    paramCharSequence.flags = ((TLRPC.DraftMessage)localObject).flags;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramCharSequence, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
    label336:
    MessagesController.getInstance(this.currentAccount).sortDialogs(null);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
  }
  
  public void saveDraft(final long paramLong, TLRPC.DraftMessage paramDraftMessage, TLRPC.Message paramMessage, boolean paramBoolean)
  {
    Object localObject = this.preferences.edit();
    label144:
    label201:
    final long l;
    if ((paramDraftMessage == null) || ((paramDraftMessage instanceof TLRPC.TL_draftMessageEmpty)))
    {
      this.drafts.remove(paramLong);
      this.draftMessages.remove(paramLong);
      this.preferences.edit().remove("" + paramLong).remove("r_" + paramLong).commit();
      if (paramMessage != null) {
        break label375;
      }
      this.draftMessages.remove(paramLong);
      ((SharedPreferences.Editor)localObject).remove("r_" + paramLong);
      ((SharedPreferences.Editor)localObject).commit();
      if (paramBoolean) {
        if ((paramDraftMessage.reply_to_msg_id != 0) && (paramMessage == null))
        {
          i = (int)paramLong;
          localObject = null;
          paramMessage = null;
          if (i <= 0) {
            break label445;
          }
          localObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
          if ((localObject != null) || (paramMessage != null))
          {
            l = paramDraftMessage.reply_to_msg_id;
            if (!ChatObject.isChannel(paramMessage)) {
              break label466;
            }
            l |= paramMessage.id << 32;
          }
        }
      }
    }
    label375:
    label445:
    label466:
    for (int i = paramMessage.id;; i = 0)
    {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          Object localObject2 = null;
          try
          {
            SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(DataQuery.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT data FROM messages WHERE mid = %d", new Object[] { Long.valueOf(l) }), new Object[0]);
            Object localObject1 = localObject2;
            if (localSQLiteCursor.next())
            {
              NativeByteBuffer localNativeByteBuffer = localSQLiteCursor.byteBufferValue(0);
              localObject1 = localObject2;
              if (localNativeByteBuffer != null)
              {
                localObject1 = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                ((TLRPC.Message)localObject1).readAttachPath(localNativeByteBuffer, UserConfig.getInstance(DataQuery.this.currentAccount).clientUserId);
                localNativeByteBuffer.reuse();
              }
            }
            localSQLiteCursor.dispose();
            if (localObject1 == null)
            {
              if (paramLong != 0)
              {
                localObject1 = new TLRPC.TL_channels_getMessages();
                ((TLRPC.TL_channels_getMessages)localObject1).channel = MessagesController.getInstance(DataQuery.this.currentAccount).getInputChannel(paramLong);
                ((TLRPC.TL_channels_getMessages)localObject1).id.add(Integer.valueOf((int)l));
                ConnectionsManager.getInstance(DataQuery.this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
                {
                  public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
                  {
                    if (paramAnonymous2TL_error == null)
                    {
                      paramAnonymous2TLObject = (TLRPC.messages_Messages)paramAnonymous2TLObject;
                      if (!paramAnonymous2TLObject.messages.isEmpty()) {
                        DataQuery.this.saveDraftReplyMessage(DataQuery.62.this.val$did, (TLRPC.Message)paramAnonymous2TLObject.messages.get(0));
                      }
                    }
                  }
                });
                return;
              }
              localObject1 = new TLRPC.TL_messages_getMessages();
              ((TLRPC.TL_messages_getMessages)localObject1).id.add(Integer.valueOf((int)l));
              ConnectionsManager.getInstance(DataQuery.this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
              {
                public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
                {
                  if (paramAnonymous2TL_error == null)
                  {
                    paramAnonymous2TLObject = (TLRPC.messages_Messages)paramAnonymous2TLObject;
                    if (!paramAnonymous2TLObject.messages.isEmpty()) {
                      DataQuery.this.saveDraftReplyMessage(DataQuery.62.this.val$did, (TLRPC.Message)paramAnonymous2TLObject.messages.get(0));
                    }
                  }
                }
              });
              return;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          DataQuery.this.saveDraftReplyMessage(this.val$did, localException);
        }
      });
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.newDraftReceived, new Object[] { Long.valueOf(paramLong) });
      return;
      this.drafts.put(paramLong, paramDraftMessage);
      try
      {
        SerializedData localSerializedData1 = new SerializedData(paramDraftMessage.getObjectSize());
        paramDraftMessage.serializeToStream(localSerializedData1);
        ((SharedPreferences.Editor)localObject).putString("" + paramLong, Utilities.bytesToHex(localSerializedData1.toByteArray()));
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
      break;
      this.draftMessages.put(paramLong, paramMessage);
      SerializedData localSerializedData2 = new SerializedData(paramMessage.getObjectSize());
      paramMessage.serializeToStream(localSerializedData2);
      ((SharedPreferences.Editor)localObject).putString("r_" + paramLong, Utilities.bytesToHex(localSerializedData2.toByteArray()));
      break label144;
      paramMessage = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
      break label201;
    }
  }
  
  public void searchMessagesInChat(String paramString, long paramLong1, long paramLong2, int paramInt1, int paramInt2, TLRPC.User paramUser)
  {
    searchMessagesInChat(paramString, paramLong1, paramLong2, paramInt1, paramInt2, false, paramUser);
  }
  
  public void uninstallShortcut(long paramLong)
  {
    for (;;)
    {
      Object localObject3;
      int i;
      try
      {
        if (Build.VERSION.SDK_INT >= 26)
        {
          localObject1 = (ShortcutManager)ApplicationLoader.applicationContext.getSystemService(ShortcutManager.class);
          localObject3 = new ArrayList();
          ((ArrayList)localObject3).add("sdid_" + paramLong);
          ((ShortcutManager)localObject1).removeDynamicShortcuts((List)localObject3);
          return;
        }
        i = (int)paramLong;
        int j = (int)(paramLong >> 32);
        localObject1 = null;
        localObject3 = null;
        if (i != 0) {
          break label220;
        }
        localObject1 = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(j));
        if (localObject1 == null) {
          break label277;
        }
        localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.EncryptedChat)localObject1).user_id));
      }
      catch (Exception localException)
      {
        Object localObject1;
        FileLog.e(localException);
        return;
      }
      label220:
      Object localObject2;
      if (localObject1 != null)
      {
        localObject1 = ContactsController.formatName(((TLRPC.User)localObject1).first_name, ((TLRPC.User)localObject1).last_name);
        localObject3 = new Intent();
        ((Intent)localObject3).putExtra("android.intent.extra.shortcut.INTENT", createIntrnalShortcutIntent(paramLong));
        ((Intent)localObject3).putExtra("android.intent.extra.shortcut.NAME", (String)localObject1);
        ((Intent)localObject3).putExtra("duplicate", false);
        ((Intent)localObject3).setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        ApplicationLoader.applicationContext.sendBroadcast((Intent)localObject3);
        return;
        if (i > 0)
        {
          localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
          break label278;
        }
        if (i < 0)
        {
          localObject3 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
          break label278;
        }
      }
      else
      {
        localObject2 = ((TLRPC.Chat)localObject3).title;
        continue;
      }
      label277:
      label278:
      do
      {
        return;
        if (localObject2 != null) {
          break;
        }
      } while (localObject3 == null);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/DataQuery.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */