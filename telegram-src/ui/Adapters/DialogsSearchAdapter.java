package org.telegram.ui.Adapters;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.LongSparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_messages_searchGlobal;
import org.telegram.tgnet.TLRPC.TL_topPeer;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class DialogsSearchAdapter
  extends RecyclerListView.SelectionAdapter
{
  private int currentAccount = UserConfig.selectedAccount;
  private DialogsSearchAdapterDelegate delegate;
  private int dialogsType;
  private RecyclerListView innerListView;
  private String lastMessagesSearchString;
  private int lastReqId;
  private int lastSearchId = 0;
  private String lastSearchText;
  private Context mContext;
  private boolean messagesSearchEndReached;
  private int needMessagesSearch;
  private ArrayList<RecentSearchObject> recentSearchObjects = new ArrayList();
  private LongSparseArray<RecentSearchObject> recentSearchObjectsById = new LongSparseArray();
  private int reqId = 0;
  private SearchAdapterHelper searchAdapterHelper = new SearchAdapterHelper(false);
  private ArrayList<TLObject> searchResult = new ArrayList();
  private ArrayList<String> searchResultHashtags = new ArrayList();
  private ArrayList<MessageObject> searchResultMessages = new ArrayList();
  private ArrayList<CharSequence> searchResultNames = new ArrayList();
  private Timer searchTimer;
  private int selfUserId;
  
  public DialogsSearchAdapter(Context paramContext, int paramInt1, int paramInt2)
  {
    this.searchAdapterHelper.setDelegate(new SearchAdapterHelper.SearchAdapterHelperDelegate()
    {
      public void onDataSetChanged()
      {
        DialogsSearchAdapter.this.notifyDataSetChanged();
      }
      
      public void onSetHashtags(ArrayList<SearchAdapterHelper.HashtagObject> paramAnonymousArrayList, HashMap<String, SearchAdapterHelper.HashtagObject> paramAnonymousHashMap)
      {
        int i = 0;
        while (i < paramAnonymousArrayList.size())
        {
          DialogsSearchAdapter.this.searchResultHashtags.add(((SearchAdapterHelper.HashtagObject)paramAnonymousArrayList.get(i)).hashtag);
          i += 1;
        }
        if (DialogsSearchAdapter.this.delegate != null) {
          DialogsSearchAdapter.this.delegate.searchStateChanged(false);
        }
        DialogsSearchAdapter.this.notifyDataSetChanged();
      }
    });
    this.mContext = paramContext;
    this.needMessagesSearch = paramInt1;
    this.dialogsType = paramInt2;
    this.selfUserId = UserConfig.getInstance(this.currentAccount).getClientUserId();
    loadRecentSearch();
    DataQuery.getInstance(this.currentAccount).loadHints(true);
  }
  
  private void searchDialogsInternal(final String paramString, final int paramInt)
  {
    if (this.needMessagesSearch == 2) {
      return;
    }
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject6;
        Object localObject7;
        long l;
        int k;
        try
        {
          localObject6 = LocaleController.getString("SavedMessages", 2131494293).toLowerCase();
          localObject7 = paramString.trim().toLowerCase();
          if (((String)localObject7).length() == 0)
          {
            DialogsSearchAdapter.access$1002(DialogsSearchAdapter.this, -1);
            DialogsSearchAdapter.this.updateSearchResults(new ArrayList(), new ArrayList(), new ArrayList(), DialogsSearchAdapter.this.lastSearchId);
            return;
          }
          localObject3 = LocaleController.getInstance().getTranslitString((String)localObject7);
          if (((String)localObject7).equals(localObject3)) {
            break label2581;
          }
          localObject1 = localObject3;
          if (((String)localObject3).length() != 0) {
            break label2584;
          }
        }
        catch (Exception localException)
        {
          Object localObject1;
          FileLog.e(localException);
          return;
        }
        String[] arrayOfString = new String[i + 1];
        arrayOfString[0] = localObject7;
        if (localObject1 != null) {
          arrayOfString[1] = localObject1;
        }
        localObject1 = new ArrayList();
        Object localObject5 = new ArrayList();
        Object localObject4 = new ArrayList();
        ArrayList localArrayList = new ArrayList();
        int j = 0;
        LongSparseArray localLongSparseArray = new LongSparseArray();
        Object localObject3 = MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDatabase().queryFinalized("SELECT did, date FROM dialogs ORDER BY date DESC LIMIT 600", new Object[0]);
        Object localObject8;
        for (;;)
        {
          if (!((SQLiteCursor)localObject3).next()) {
            break label428;
          }
          l = ((SQLiteCursor)localObject3).longValue(0);
          localObject8 = new DialogsSearchAdapter.DialogSearchResult(DialogsSearchAdapter.this, null);
          ((DialogsSearchAdapter.DialogSearchResult)localObject8).date = ((SQLiteCursor)localObject3).intValue(1);
          localLongSparseArray.put(l, localObject8);
          i = (int)l;
          k = (int)(l >> 32);
          if (i != 0)
          {
            if (k == 1)
            {
              if ((DialogsSearchAdapter.this.dialogsType != 0) || (((ArrayList)localObject5).contains(Integer.valueOf(i)))) {
                continue;
              }
              ((ArrayList)localObject5).add(Integer.valueOf(i));
              continue;
              label321:
              i = 0;
              break;
            }
            if (i > 0)
            {
              if ((DialogsSearchAdapter.this.dialogsType == 2) || (localException.contains(Integer.valueOf(i)))) {
                continue;
              }
              localException.add(Integer.valueOf(i));
              continue;
            }
            if (((ArrayList)localObject5).contains(Integer.valueOf(-i))) {
              continue;
            }
            ((ArrayList)localObject5).add(Integer.valueOf(-i));
            continue;
          }
          if ((DialogsSearchAdapter.this.dialogsType == 0) && (!((ArrayList)localObject4).contains(Integer.valueOf(k)))) {
            ((ArrayList)localObject4).add(Integer.valueOf(k));
          }
        }
        label428:
        ((SQLiteCursor)localObject3).dispose();
        if (((String)localObject6).startsWith((String)localObject7))
        {
          localObject3 = UserConfig.getInstance(DialogsSearchAdapter.this.currentAccount).getCurrentUser();
          localObject7 = new DialogsSearchAdapter.DialogSearchResult(DialogsSearchAdapter.this, null);
          ((DialogsSearchAdapter.DialogSearchResult)localObject7).date = Integer.MAX_VALUE;
          ((DialogsSearchAdapter.DialogSearchResult)localObject7).name = ((CharSequence)localObject6);
          ((DialogsSearchAdapter.DialogSearchResult)localObject7).object = ((TLObject)localObject3);
          localLongSparseArray.put(((TLRPC.User)localObject3).id, localObject7);
          j = 0 + 1;
        }
        int i = j;
        label566:
        int m;
        if (!localException.isEmpty())
        {
          localObject6 = MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, status, name FROM users WHERE uid IN(%s)", new Object[] { TextUtils.join(",", localException) }), new Object[0]);
          i = j;
          while (((SQLiteCursor)localObject6).next())
          {
            localObject7 = ((SQLiteCursor)localObject6).stringValue(2);
            localObject3 = LocaleController.getInstance().getTranslitString((String)localObject7);
            localObject2 = localObject3;
            if (((String)localObject7).equals(localObject3)) {
              localObject2 = null;
            }
            localObject3 = null;
            j = ((String)localObject7).lastIndexOf(";;;");
            if (j != -1) {
              localObject3 = ((String)localObject7).substring(j + 3);
            }
            m = 0;
            int n = arrayOfString.length;
            k = 0;
            label645:
            if (k >= n) {
              break label2607;
            }
            localObject8 = arrayOfString[k];
            if ((((String)localObject7).startsWith((String)localObject8)) || (((String)localObject7).contains(" " + (String)localObject8))) {
              break label2594;
            }
            if (localObject2 != null)
            {
              if (((String)localObject2).startsWith((String)localObject8)) {
                break label2594;
              }
              if (((String)localObject2).contains(" " + (String)localObject8))
              {
                break label2594;
                label741:
                if (j == 0) {
                  break label2599;
                }
                localObject3 = ((SQLiteCursor)localObject6).byteBufferValue(0);
                if (localObject3 == null) {
                  continue;
                }
                localObject2 = TLRPC.User.TLdeserialize((AbstractSerializedData)localObject3, ((NativeByteBuffer)localObject3).readInt32(false), false);
                ((NativeByteBuffer)localObject3).reuse();
                localObject3 = (DialogsSearchAdapter.DialogSearchResult)localLongSparseArray.get(((TLRPC.User)localObject2).id);
                if (((TLRPC.User)localObject2).status != null) {
                  ((TLRPC.User)localObject2).status.expires = ((SQLiteCursor)localObject6).intValue(1);
                }
                if (j != 1) {
                  break label880;
                }
              }
            }
            label880:
            for (((DialogsSearchAdapter.DialogSearchResult)localObject3).name = AndroidUtilities.generateSearchName(((TLRPC.User)localObject2).first_name, ((TLRPC.User)localObject2).last_name, (String)localObject8);; ((DialogsSearchAdapter.DialogSearchResult)localObject3).name = AndroidUtilities.generateSearchName("@" + ((TLRPC.User)localObject2).username, null, "@" + (String)localObject8))
            {
              ((DialogsSearchAdapter.DialogSearchResult)localObject3).object = ((TLObject)localObject2);
              i += 1;
              break;
              j = m;
              if (localObject3 == null) {
                break label741;
              }
              j = m;
              if (!((String)localObject3).startsWith((String)localObject8)) {
                break label741;
              }
              j = 2;
              break label741;
            }
          }
          ((SQLiteCursor)localObject6).dispose();
        }
        j = i;
        if (!((ArrayList)localObject5).isEmpty())
        {
          localObject5 = MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, name FROM chats WHERE uid IN(%s)", new Object[] { TextUtils.join(",", (Iterable)localObject5) }), new Object[0]);
          label997:
          while (((SQLiteCursor)localObject5).next())
          {
            localObject6 = ((SQLiteCursor)localObject5).stringValue(1);
            localObject3 = LocaleController.getInstance().getTranslitString((String)localObject6);
            localObject2 = localObject3;
            if (((String)localObject6).equals(localObject3)) {
              localObject2 = null;
            }
            k = arrayOfString.length;
            j = 0;
            label1046:
            if (j >= k) {
              break label2614;
            }
            localObject3 = arrayOfString[j];
            if ((!((String)localObject6).startsWith((String)localObject3)) && (!((String)localObject6).contains(" " + (String)localObject3)) && ((localObject2 == null) || ((!((String)localObject2).startsWith((String)localObject3)) && (!((String)localObject2).contains(" " + (String)localObject3))))) {
              break label2609;
            }
            localObject6 = ((SQLiteCursor)localObject5).byteBufferValue(0);
            if (localObject6 != null)
            {
              localObject2 = TLRPC.Chat.TLdeserialize((AbstractSerializedData)localObject6, ((NativeByteBuffer)localObject6).readInt32(false), false);
              ((NativeByteBuffer)localObject6).reuse();
              if ((localObject2 != null) && (!((TLRPC.Chat)localObject2).deactivated) && ((!ChatObject.isChannel((TLRPC.Chat)localObject2)) || (!ChatObject.isNotInChat((TLRPC.Chat)localObject2))))
              {
                if (((TLRPC.Chat)localObject2).id > 0) {}
                for (l = -((TLRPC.Chat)localObject2).id;; l = AndroidUtilities.makeBroadcastId(((TLRPC.Chat)localObject2).id))
                {
                  localObject6 = (DialogsSearchAdapter.DialogSearchResult)localLongSparseArray.get(l);
                  ((DialogsSearchAdapter.DialogSearchResult)localObject6).name = AndroidUtilities.generateSearchName(((TLRPC.Chat)localObject2).title, null, (String)localObject3);
                  ((DialogsSearchAdapter.DialogSearchResult)localObject6).object = ((TLObject)localObject2);
                  i += 1;
                  break;
                }
              }
            }
          }
          ((SQLiteCursor)localObject5).dispose();
          j = i;
        }
        i = j;
        if (!((ArrayList)localObject4).isEmpty())
        {
          localObject4 = MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT q.data, u.name, q.user, q.g, q.authkey, q.ttl, u.data, u.status, q.layer, q.seq_in, q.seq_out, q.use_count, q.exchange_id, q.key_date, q.fprint, q.fauthkey, q.khash, q.in_seq_no, q.admin_id, q.mtproto_seq FROM enc_chats as q INNER JOIN users as u ON q.user = u.uid WHERE q.uid IN(%s)", new Object[] { TextUtils.join(",", (Iterable)localObject4) }), new Object[0]);
          label1333:
          while (((SQLiteCursor)localObject4).next())
          {
            localObject6 = ((SQLiteCursor)localObject4).stringValue(1);
            localObject3 = LocaleController.getInstance().getTranslitString((String)localObject6);
            localObject2 = localObject3;
            if (((String)localObject6).equals(localObject3)) {
              localObject2 = null;
            }
            localObject3 = null;
            i = ((String)localObject6).lastIndexOf(";;;");
            if (i == -1) {
              break label2616;
            }
            localObject3 = ((String)localObject6).substring(i + 2);
            break label2616;
            label1405:
            if (k >= arrayOfString.length) {
              break label2637;
            }
            localObject5 = arrayOfString[k];
            if ((((String)localObject6).startsWith((String)localObject5)) || (((String)localObject6).contains(" " + (String)localObject5))) {
              break label2624;
            }
            if (localObject2 != null)
            {
              if (((String)localObject2).startsWith((String)localObject5)) {
                break label2624;
              }
              if (((String)localObject2).contains(" " + (String)localObject5))
              {
                break label2624;
                label1502:
                if (i == 0) {
                  break label2629;
                }
                localObject2 = null;
                localObject3 = null;
                localObject6 = ((SQLiteCursor)localObject4).byteBufferValue(0);
                if (localObject6 != null)
                {
                  localObject2 = TLRPC.EncryptedChat.TLdeserialize((AbstractSerializedData)localObject6, ((NativeByteBuffer)localObject6).readInt32(false), false);
                  ((NativeByteBuffer)localObject6).reuse();
                }
                localObject6 = ((SQLiteCursor)localObject4).byteBufferValue(6);
                if (localObject6 != null)
                {
                  localObject3 = TLRPC.User.TLdeserialize((AbstractSerializedData)localObject6, ((NativeByteBuffer)localObject6).readInt32(false), false);
                  ((NativeByteBuffer)localObject6).reuse();
                }
                if ((localObject2 == null) || (localObject3 == null)) {
                  continue;
                }
                localObject6 = (DialogsSearchAdapter.DialogSearchResult)localLongSparseArray.get(((TLRPC.EncryptedChat)localObject2).id << 32);
                ((TLRPC.EncryptedChat)localObject2).user_id = ((SQLiteCursor)localObject4).intValue(2);
                ((TLRPC.EncryptedChat)localObject2).a_or_b = ((SQLiteCursor)localObject4).byteArrayValue(3);
                ((TLRPC.EncryptedChat)localObject2).auth_key = ((SQLiteCursor)localObject4).byteArrayValue(4);
                ((TLRPC.EncryptedChat)localObject2).ttl = ((SQLiteCursor)localObject4).intValue(5);
                ((TLRPC.EncryptedChat)localObject2).layer = ((SQLiteCursor)localObject4).intValue(8);
                ((TLRPC.EncryptedChat)localObject2).seq_in = ((SQLiteCursor)localObject4).intValue(9);
                ((TLRPC.EncryptedChat)localObject2).seq_out = ((SQLiteCursor)localObject4).intValue(10);
                k = ((SQLiteCursor)localObject4).intValue(11);
                ((TLRPC.EncryptedChat)localObject2).key_use_count_in = ((short)(k >> 16));
                ((TLRPC.EncryptedChat)localObject2).key_use_count_out = ((short)k);
                ((TLRPC.EncryptedChat)localObject2).exchange_id = ((SQLiteCursor)localObject4).longValue(12);
                ((TLRPC.EncryptedChat)localObject2).key_create_date = ((SQLiteCursor)localObject4).intValue(13);
                ((TLRPC.EncryptedChat)localObject2).future_key_fingerprint = ((SQLiteCursor)localObject4).longValue(14);
                ((TLRPC.EncryptedChat)localObject2).future_auth_key = ((SQLiteCursor)localObject4).byteArrayValue(15);
                ((TLRPC.EncryptedChat)localObject2).key_hash = ((SQLiteCursor)localObject4).byteArrayValue(16);
                ((TLRPC.EncryptedChat)localObject2).in_seq_no = ((SQLiteCursor)localObject4).intValue(17);
                k = ((SQLiteCursor)localObject4).intValue(18);
                if (k != 0) {
                  ((TLRPC.EncryptedChat)localObject2).admin_id = k;
                }
                ((TLRPC.EncryptedChat)localObject2).mtproto_seq = ((SQLiteCursor)localObject4).intValue(19);
                if (((TLRPC.User)localObject3).status != null) {
                  ((TLRPC.User)localObject3).status.expires = ((SQLiteCursor)localObject4).intValue(7);
                }
                if (i != 1) {
                  break label1951;
                }
                ((DialogsSearchAdapter.DialogSearchResult)localObject6).name = new SpannableStringBuilder(ContactsController.formatName(((TLRPC.User)localObject3).first_name, ((TLRPC.User)localObject3).last_name));
                ((SpannableStringBuilder)((DialogsSearchAdapter.DialogSearchResult)localObject6).name).setSpan(new ForegroundColorSpan(Theme.getColor("chats_secretName")), 0, ((DialogsSearchAdapter.DialogSearchResult)localObject6).name.length(), 33);
              }
            }
            for (;;)
            {
              ((DialogsSearchAdapter.DialogSearchResult)localObject6).object = ((TLObject)localObject2);
              localArrayList.add(localObject3);
              j += 1;
              break;
              i = m;
              if (localObject3 == null) {
                break label1502;
              }
              i = m;
              if (!((String)localObject3).startsWith((String)localObject5)) {
                break label1502;
              }
              i = 2;
              break label1502;
              label1951:
              ((DialogsSearchAdapter.DialogSearchResult)localObject6).name = AndroidUtilities.generateSearchName("@" + ((TLRPC.User)localObject3).username, null, "@" + (String)localObject5);
            }
          }
          ((SQLiteCursor)localObject4).dispose();
          i = j;
        }
        Object localObject2 = new ArrayList(i);
        i = 0;
        label2027:
        if (i < localLongSparseArray.size())
        {
          localObject3 = (DialogsSearchAdapter.DialogSearchResult)localLongSparseArray.valueAt(i);
          if ((((DialogsSearchAdapter.DialogSearchResult)localObject3).object != null) && (((DialogsSearchAdapter.DialogSearchResult)localObject3).name != null)) {
            ((ArrayList)localObject2).add(localObject3);
          }
        }
        else
        {
          Collections.sort((List)localObject2, new Comparator()
          {
            public int compare(DialogsSearchAdapter.DialogSearchResult paramAnonymous2DialogSearchResult1, DialogsSearchAdapter.DialogSearchResult paramAnonymous2DialogSearchResult2)
            {
              if (paramAnonymous2DialogSearchResult1.date < paramAnonymous2DialogSearchResult2.date) {
                return 1;
              }
              if (paramAnonymous2DialogSearchResult1.date > paramAnonymous2DialogSearchResult2.date) {
                return -1;
              }
              return 0;
            }
          });
          localObject4 = new ArrayList();
          localObject5 = new ArrayList();
          i = 0;
          while (i < ((ArrayList)localObject2).size())
          {
            localObject3 = (DialogsSearchAdapter.DialogSearchResult)((ArrayList)localObject2).get(i);
            ((ArrayList)localObject4).add(((DialogsSearchAdapter.DialogSearchResult)localObject3).object);
            ((ArrayList)localObject5).add(((DialogsSearchAdapter.DialogSearchResult)localObject3).name);
            i += 1;
          }
          if (DialogsSearchAdapter.this.dialogsType != 2) {
            localObject6 = MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDatabase().queryFinalized("SELECT u.data, u.status, u.name, u.uid FROM users as u INNER JOIN contacts as c ON u.uid = c.uid", new Object[0]);
          }
        }
        label2192:
        label2381:
        label2581:
        label2584:
        label2594:
        label2599:
        label2607:
        label2609:
        label2614:
        label2616:
        label2624:
        label2629:
        label2637:
        label2646:
        label2651:
        label2658:
        for (;;)
        {
          if (((SQLiteCursor)localObject6).next())
          {
            if (localLongSparseArray.indexOfKey(((SQLiteCursor)localObject6).intValue(3)) < 0)
            {
              localObject7 = ((SQLiteCursor)localObject6).stringValue(2);
              localObject3 = LocaleController.getInstance().getTranslitString((String)localObject7);
              localObject2 = localObject3;
              if (((String)localObject7).equals(localObject3)) {
                localObject2 = null;
              }
              localObject3 = null;
              i = ((String)localObject7).lastIndexOf(";;;");
              if (i != -1) {
                localObject3 = ((String)localObject7).substring(i + 3);
              }
              k = 0;
              m = arrayOfString.length;
              j = 0;
            }
          }
          else {
            for (;;)
            {
              if (j >= m) {
                break label2658;
              }
              localObject8 = arrayOfString[j];
              if ((!((String)localObject7).startsWith((String)localObject8)) && (!((String)localObject7).contains(" " + (String)localObject8))) {
                if (localObject2 != null)
                {
                  if (((String)localObject2).startsWith((String)localObject8)) {
                    break label2646;
                  }
                  if (((String)localObject2).contains(" " + (String)localObject8)) {
                    break label2646;
                  }
                }
              }
              for (;;)
              {
                if (i == 0) {
                  break label2651;
                }
                localObject2 = ((SQLiteCursor)localObject6).byteBufferValue(0);
                if (localObject2 == null) {
                  break label2192;
                }
                localObject3 = TLRPC.User.TLdeserialize((AbstractSerializedData)localObject2, ((NativeByteBuffer)localObject2).readInt32(false), false);
                ((NativeByteBuffer)localObject2).reuse();
                if (((TLRPC.User)localObject3).status != null) {
                  ((TLRPC.User)localObject3).status.expires = ((SQLiteCursor)localObject6).intValue(1);
                }
                if (i == 1) {
                  ((ArrayList)localObject5).add(AndroidUtilities.generateSearchName(((TLRPC.User)localObject3).first_name, ((TLRPC.User)localObject3).last_name, (String)localObject8));
                }
                for (;;)
                {
                  ((ArrayList)localObject4).add(localObject3);
                  break;
                  i = k;
                  if (localObject3 == null) {
                    break label2381;
                  }
                  i = k;
                  if (!((String)localObject3).startsWith((String)localObject8)) {
                    break label2381;
                  }
                  i = 2;
                  break label2381;
                  ((ArrayList)localObject5).add(AndroidUtilities.generateSearchName("@" + ((TLRPC.User)localObject3).username, null, "@" + (String)localObject8));
                }
                ((SQLiteCursor)localObject6).dispose();
                DialogsSearchAdapter.this.updateSearchResults((ArrayList)localObject4, (ArrayList)localObject5, localArrayList, paramInt);
                return;
                localObject2 = null;
                if (localObject2 == null) {
                  break label321;
                }
                i = 1;
                break;
                j = 1;
                break label741;
                k += 1;
                m = j;
                break label645;
                break label566;
                j += 1;
                break label1046;
                break label997;
                m = 0;
                k = 0;
                break label1405;
                i = 1;
                break label1502;
                k += 1;
                m = i;
                break label1405;
                break label1333;
                i += 1;
                break label2027;
                i = 1;
              }
              j += 1;
              k = i;
            }
          }
        }
      }
    });
  }
  
  private void searchMessagesInternal(String paramString)
  {
    if ((this.needMessagesSearch == 0) || (((this.lastMessagesSearchString == null) || (this.lastMessagesSearchString.length() == 0)) && ((paramString == null) || (paramString.length() == 0)))) {}
    do
    {
      return;
      if (this.reqId != 0)
      {
        ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
        this.reqId = 0;
      }
      if ((paramString != null) && (paramString.length() != 0)) {
        break;
      }
      this.searchResultMessages.clear();
      this.lastReqId = 0;
      this.lastMessagesSearchString = null;
      notifyDataSetChanged();
    } while (this.delegate == null);
    this.delegate.searchStateChanged(false);
    return;
    final TLRPC.TL_messages_searchGlobal localTL_messages_searchGlobal = new TLRPC.TL_messages_searchGlobal();
    localTL_messages_searchGlobal.limit = 20;
    localTL_messages_searchGlobal.q = paramString;
    MessageObject localMessageObject;
    final int i;
    if ((this.lastMessagesSearchString != null) && (paramString.equals(this.lastMessagesSearchString)) && (!this.searchResultMessages.isEmpty()))
    {
      localMessageObject = (MessageObject)this.searchResultMessages.get(this.searchResultMessages.size() - 1);
      localTL_messages_searchGlobal.offset_id = localMessageObject.getId();
      localTL_messages_searchGlobal.offset_date = localMessageObject.messageOwner.date;
      if (localMessageObject.messageOwner.to_id.channel_id != 0) {
        i = -localMessageObject.messageOwner.to_id.channel_id;
      }
    }
    for (localTL_messages_searchGlobal.offset_peer = MessagesController.getInstance(this.currentAccount).getInputPeer(i);; localTL_messages_searchGlobal.offset_peer = new TLRPC.TL_inputPeerEmpty())
    {
      this.lastMessagesSearchString = paramString;
      i = this.lastReqId + 1;
      this.lastReqId = i;
      if (this.delegate != null) {
        this.delegate.searchStateChanged(true);
      }
      this.reqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_searchGlobal, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              boolean bool2 = true;
              Object localObject;
              if ((DialogsSearchAdapter.2.this.val$currentReqId == DialogsSearchAdapter.this.lastReqId) && (paramAnonymousTL_error == null))
              {
                TLRPC.messages_Messages localmessages_Messages = (TLRPC.messages_Messages)paramAnonymousTLObject;
                MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).putUsersAndChats(localmessages_Messages.users, localmessages_Messages.chats, true, true);
                MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).putUsers(localmessages_Messages.users, false);
                MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).putChats(localmessages_Messages.chats, false);
                if (DialogsSearchAdapter.2.this.val$req.offset_id == 0) {
                  DialogsSearchAdapter.this.searchResultMessages.clear();
                }
                int i = 0;
                if (i < localmessages_Messages.messages.size())
                {
                  TLRPC.Message localMessage = (TLRPC.Message)localmessages_Messages.messages.get(i);
                  DialogsSearchAdapter.this.searchResultMessages.add(new MessageObject(DialogsSearchAdapter.this.currentAccount, localMessage, false));
                  long l = MessageObject.getDialogId(localMessage);
                  if (localMessage.out)
                  {
                    localObject = MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).dialogs_read_outbox_max;
                    label231:
                    Integer localInteger2 = (Integer)((ConcurrentHashMap)localObject).get(Long.valueOf(l));
                    Integer localInteger1 = localInteger2;
                    if (localInteger2 == null)
                    {
                      localInteger1 = Integer.valueOf(MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDialogReadMax(localMessage.out, l));
                      ((ConcurrentHashMap)localObject).put(Long.valueOf(l), localInteger1);
                    }
                    if (localInteger1.intValue() >= localMessage.id) {
                      break label345;
                    }
                  }
                  label345:
                  for (bool1 = true;; bool1 = false)
                  {
                    localMessage.unread = bool1;
                    i += 1;
                    break;
                    localObject = MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).dialogs_read_inbox_max;
                    break label231;
                  }
                }
                localObject = DialogsSearchAdapter.this;
                if (localmessages_Messages.messages.size() == 20) {
                  break label433;
                }
              }
              label433:
              for (boolean bool1 = bool2;; bool1 = false)
              {
                DialogsSearchAdapter.access$602((DialogsSearchAdapter)localObject, bool1);
                DialogsSearchAdapter.this.notifyDataSetChanged();
                if (DialogsSearchAdapter.this.delegate != null) {
                  DialogsSearchAdapter.this.delegate.searchStateChanged(false);
                }
                DialogsSearchAdapter.access$702(DialogsSearchAdapter.this, 0);
                return;
              }
            }
          });
        }
      }, 2);
      return;
      if (localMessageObject.messageOwner.to_id.chat_id != 0)
      {
        i = -localMessageObject.messageOwner.to_id.chat_id;
        break;
      }
      i = localMessageObject.messageOwner.to_id.user_id;
      break;
      localTL_messages_searchGlobal.offset_date = 0;
      localTL_messages_searchGlobal.offset_id = 0;
    }
  }
  
  private void setRecentSearch(ArrayList<RecentSearchObject> paramArrayList, LongSparseArray<RecentSearchObject> paramLongSparseArray)
  {
    this.recentSearchObjects = paramArrayList;
    this.recentSearchObjectsById = paramLongSparseArray;
    int i = 0;
    if (i < this.recentSearchObjects.size())
    {
      paramArrayList = (RecentSearchObject)this.recentSearchObjects.get(i);
      if ((paramArrayList.object instanceof TLRPC.User)) {
        MessagesController.getInstance(this.currentAccount).putUser((TLRPC.User)paramArrayList.object, true);
      }
      for (;;)
      {
        i += 1;
        break;
        if ((paramArrayList.object instanceof TLRPC.Chat)) {
          MessagesController.getInstance(this.currentAccount).putChat((TLRPC.Chat)paramArrayList.object, true);
        } else if ((paramArrayList.object instanceof TLRPC.EncryptedChat)) {
          MessagesController.getInstance(this.currentAccount).putEncryptedChat((TLRPC.EncryptedChat)paramArrayList.object, true);
        }
      }
    }
    notifyDataSetChanged();
  }
  
  private void updateSearchResults(final ArrayList<TLObject> paramArrayList, final ArrayList<CharSequence> paramArrayList1, final ArrayList<TLRPC.User> paramArrayList2, final int paramInt)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (paramInt != DialogsSearchAdapter.this.lastSearchId) {
          return;
        }
        int i = 0;
        if (i < paramArrayList.size())
        {
          Object localObject = (TLObject)paramArrayList.get(i);
          if ((localObject instanceof TLRPC.User))
          {
            localObject = (TLRPC.User)localObject;
            MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).putUser((TLRPC.User)localObject, true);
          }
          for (;;)
          {
            i += 1;
            break;
            if ((localObject instanceof TLRPC.Chat))
            {
              localObject = (TLRPC.Chat)localObject;
              MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).putChat((TLRPC.Chat)localObject, true);
            }
            else if ((localObject instanceof TLRPC.EncryptedChat))
            {
              localObject = (TLRPC.EncryptedChat)localObject;
              MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).putEncryptedChat((TLRPC.EncryptedChat)localObject, true);
            }
          }
        }
        MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).putUsers(paramArrayList2, true);
        DialogsSearchAdapter.access$1302(DialogsSearchAdapter.this, paramArrayList);
        DialogsSearchAdapter.access$1402(DialogsSearchAdapter.this, paramArrayList1);
        DialogsSearchAdapter.this.searchAdapterHelper.mergeResults(DialogsSearchAdapter.this.searchResult);
        DialogsSearchAdapter.this.notifyDataSetChanged();
      }
    });
  }
  
  public void addHashtagsFromMessage(CharSequence paramCharSequence)
  {
    this.searchAdapterHelper.addHashtagsFromMessage(paramCharSequence);
  }
  
  public void clearRecentHashtags()
  {
    this.searchAdapterHelper.clearRecentHashtags();
    this.searchResultHashtags.clear();
    notifyDataSetChanged();
  }
  
  public void clearRecentSearch()
  {
    this.recentSearchObjectsById = new LongSparseArray();
    this.recentSearchObjects = new ArrayList();
    notifyDataSetChanged();
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDatabase().executeFast("DELETE FROM search_recent WHERE 1").stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public RecyclerListView getInnerListView()
  {
    return this.innerListView;
  }
  
  public Object getItem(int paramInt)
  {
    TLObject localTLObject = null;
    int i;
    Object localObject1;
    Object localObject2;
    if (isRecentSearchDisplayed()) {
      if (!DataQuery.getInstance(this.currentAccount).hints.isEmpty())
      {
        i = 2;
        localObject1 = localTLObject;
        if (paramInt > i)
        {
          localObject1 = localTLObject;
          if (paramInt - 1 - i < this.recentSearchObjects.size())
          {
            localTLObject = ((RecentSearchObject)this.recentSearchObjects.get(paramInt - 1 - i)).object;
            if (!(localTLObject instanceof TLRPC.User)) {
              break label128;
            }
            localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.User)localTLObject).id));
            localObject1 = localTLObject;
            if (localObject2 != null) {
              localObject1 = localObject2;
            }
          }
        }
      }
    }
    label128:
    label204:
    int k;
    int m;
    int j;
    label290:
    label303:
    do
    {
      do
      {
        do
        {
          do
          {
            do
            {
              return localObject1;
              i = 0;
              break;
              localObject1 = localTLObject;
            } while (!(localTLObject instanceof TLRPC.Chat));
            localObject2 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(((TLRPC.Chat)localTLObject).id));
            localObject1 = localTLObject;
          } while (localObject2 == null);
          return localObject2;
          if (this.searchResultHashtags.isEmpty()) {
            break label204;
          }
          localObject1 = localTLObject;
        } while (paramInt <= 0);
        return this.searchResultHashtags.get(paramInt - 1);
        localObject1 = this.searchAdapterHelper.getGlobalSearch();
        localObject2 = this.searchAdapterHelper.getLocalServerSearch();
        k = this.searchResult.size();
        m = ((ArrayList)localObject2).size();
        if (((ArrayList)localObject1).isEmpty())
        {
          i = 0;
          if (!this.searchResultMessages.isEmpty()) {
            break label290;
          }
        }
        for (j = 0;; j = this.searchResultMessages.size() + 1)
        {
          if ((paramInt < 0) || (paramInt >= k)) {
            break label303;
          }
          return this.searchResult.get(paramInt);
          i = ((ArrayList)localObject1).size() + 1;
          break;
        }
        if ((paramInt >= k) && (paramInt < m + k)) {
          return ((ArrayList)localObject2).get(paramInt - k);
        }
        if ((paramInt > k + m) && (paramInt < i + k + m)) {
          return ((ArrayList)localObject1).get(paramInt - k - m - 1);
        }
        localObject1 = localTLObject;
      } while (paramInt <= i + k + m);
      localObject1 = localTLObject;
    } while (paramInt >= i + k + j + m);
    return this.searchResultMessages.get(paramInt - k - i - m - 1);
  }
  
  public int getItemCount()
  {
    int k = 0;
    int j = 0;
    int i;
    if (isRecentSearchDisplayed()) {
      if (!this.recentSearchObjects.isEmpty())
      {
        i = this.recentSearchObjects.size() + 1;
        if (!DataQuery.getInstance(this.currentAccount).hints.isEmpty()) {
          j = 2;
        }
        j = i + j;
      }
    }
    int m;
    do
    {
      return j;
      i = 0;
      break;
      if (!this.searchResultHashtags.isEmpty()) {
        return this.searchResultHashtags.size() + 1;
      }
      i = this.searchResult.size();
      j = this.searchAdapterHelper.getLocalServerSearch().size();
      int n = this.searchAdapterHelper.getGlobalSearch().size();
      m = this.searchResultMessages.size();
      j = i + j;
      i = j;
      if (n != 0) {
        i = j + (n + 1);
      }
      j = i;
    } while (m == 0);
    if (this.messagesSearchEndReached) {}
    for (j = k;; j = 1) {
      return i + (m + 1 + j);
    }
  }
  
  public long getItemId(int paramInt)
  {
    return paramInt;
  }
  
  public int getItemViewType(int paramInt)
  {
    int i;
    if (isRecentSearchDisplayed()) {
      if (!DataQuery.getInstance(this.currentAccount).hints.isEmpty())
      {
        i = 2;
        if (paramInt > i) {
          break label50;
        }
        if ((paramInt != i) && (paramInt % 2 != 0)) {
          break label48;
        }
      }
    }
    label48:
    label50:
    label68:
    int k;
    int m;
    int j;
    label166:
    label179:
    do
    {
      do
      {
        return 1;
        i = 0;
        break;
        return 5;
        return 0;
        if (this.searchResultHashtags.isEmpty()) {
          break label68;
        }
      } while (paramInt == 0);
      return 4;
      ArrayList localArrayList = this.searchAdapterHelper.getGlobalSearch();
      k = this.searchResult.size();
      m = this.searchAdapterHelper.getLocalServerSearch().size();
      if (localArrayList.isEmpty())
      {
        i = 0;
        if (!this.searchResultMessages.isEmpty()) {
          break label166;
        }
      }
      for (j = 0;; j = this.searchResultMessages.size() + 1)
      {
        if (((paramInt < 0) || (paramInt >= k + m)) && ((paramInt <= k + m) || (paramInt >= i + k + m))) {
          break label179;
        }
        return 0;
        i = localArrayList.size() + 1;
        break;
      }
      if ((paramInt > i + k + m) && (paramInt < i + k + j + m)) {
        return 2;
      }
    } while ((j == 0) || (paramInt != i + k + j + m));
    return 3;
  }
  
  public String getLastSearchString()
  {
    return this.lastMessagesSearchString;
  }
  
  public boolean hasRecentRearch()
  {
    return (!this.recentSearchObjects.isEmpty()) || (!DataQuery.getInstance(this.currentAccount).hints.isEmpty());
  }
  
  public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
  {
    int i = paramViewHolder.getItemViewType();
    return (i != 1) && (i != 3);
  }
  
  public boolean isMessagesSearchEndReached()
  {
    return this.messagesSearchEndReached;
  }
  
  public boolean isRecentSearchDisplayed()
  {
    return (this.needMessagesSearch != 2) && ((this.lastSearchText == null) || (this.lastSearchText.length() == 0)) && ((!this.recentSearchObjects.isEmpty()) || (!DataQuery.getInstance(this.currentAccount).hints.isEmpty()));
  }
  
  public void loadMoreSearchMessages()
  {
    searchMessagesInternal(this.lastMessagesSearchString);
  }
  
  public void loadRecentSearch()
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject1;
        Object localObject3;
        ArrayList localArrayList2;
        final LongSparseArray localLongSparseArray;
        long l;
        int i;
        Object localObject4;
        for (;;)
        {
          int j;
          int k;
          int m;
          try
          {
            localObject2 = MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDatabase().queryFinalized("SELECT did, date FROM search_recent WHERE 1", new Object[0]);
            localObject1 = new ArrayList();
            localObject3 = new ArrayList();
            localArrayList2 = new ArrayList();
            new ArrayList();
            ArrayList localArrayList1 = new ArrayList();
            localLongSparseArray = new LongSparseArray();
            if (!((SQLiteCursor)localObject2).next()) {
              break;
            }
            l = ((SQLiteCursor)localObject2).longValue(0);
            j = 0;
            k = (int)l;
            m = (int)(l >> 32);
            if (k == 0) {
              break label294;
            }
            if (m == 1)
            {
              i = j;
              if (DialogsSearchAdapter.this.dialogsType == 0)
              {
                i = j;
                if (!((ArrayList)localObject3).contains(Integer.valueOf(k)))
                {
                  ((ArrayList)localObject3).add(Integer.valueOf(k));
                  i = 1;
                }
              }
              if (i == 0) {
                continue;
              }
              localObject4 = new DialogsSearchAdapter.RecentSearchObject();
              ((DialogsSearchAdapter.RecentSearchObject)localObject4).did = l;
              ((DialogsSearchAdapter.RecentSearchObject)localObject4).date = ((SQLiteCursor)localObject2).intValue(1);
              localArrayList1.add(localObject4);
              localLongSparseArray.put(((DialogsSearchAdapter.RecentSearchObject)localObject4).did, localObject4);
              continue;
            }
            if (k <= 0) {
              break label263;
            }
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            return;
          }
          i = j;
          if (DialogsSearchAdapter.this.dialogsType != 2)
          {
            i = j;
            if (!((ArrayList)localObject1).contains(Integer.valueOf(k)))
            {
              ((ArrayList)localObject1).add(Integer.valueOf(k));
              i = 1;
              continue;
              label263:
              i = j;
              if (!((ArrayList)localObject3).contains(Integer.valueOf(-k)))
              {
                ((ArrayList)localObject3).add(Integer.valueOf(-k));
                i = 1;
                continue;
                label294:
                i = j;
                if (DialogsSearchAdapter.this.dialogsType == 0)
                {
                  i = j;
                  if (!localArrayList2.contains(Integer.valueOf(m)))
                  {
                    localArrayList2.add(Integer.valueOf(m));
                    i = 1;
                  }
                }
              }
            }
          }
        }
        ((SQLiteCursor)localObject2).dispose();
        Object localObject2 = new ArrayList();
        if (!localArrayList2.isEmpty())
        {
          localObject4 = new ArrayList();
          MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getEncryptedChatsInternal(TextUtils.join(",", localArrayList2), (ArrayList)localObject4, (ArrayList)localObject1);
          i = 0;
          while (i < ((ArrayList)localObject4).size())
          {
            ((DialogsSearchAdapter.RecentSearchObject)localLongSparseArray.get(((TLRPC.EncryptedChat)((ArrayList)localObject4).get(i)).id << 32)).object = ((TLObject)((ArrayList)localObject4).get(i));
            i += 1;
          }
        }
        if (!((ArrayList)localObject3).isEmpty())
        {
          localArrayList2 = new ArrayList();
          MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getChatsInternal(TextUtils.join(",", (Iterable)localObject3), localArrayList2);
          i = 0;
          if (i < localArrayList2.size())
          {
            localObject3 = (TLRPC.Chat)localArrayList2.get(i);
            if (((TLRPC.Chat)localObject3).id > 0) {}
            for (l = -((TLRPC.Chat)localObject3).id; ((TLRPC.Chat)localObject3).migrated_to != null; l = AndroidUtilities.makeBroadcastId(((TLRPC.Chat)localObject3).id))
            {
              localObject3 = (DialogsSearchAdapter.RecentSearchObject)localLongSparseArray.get(l);
              localLongSparseArray.remove(l);
              if (localObject3 == null) {
                break label710;
              }
              localException.remove(localObject3);
              break label710;
            }
            ((DialogsSearchAdapter.RecentSearchObject)localLongSparseArray.get(l)).object = ((TLObject)localObject3);
            break label710;
          }
        }
        if (!((ArrayList)localObject1).isEmpty())
        {
          MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getUsersInternal(TextUtils.join(",", (Iterable)localObject1), (ArrayList)localObject2);
          i = 0;
        }
        for (;;)
        {
          if (i < ((ArrayList)localObject2).size())
          {
            localObject1 = (TLRPC.User)((ArrayList)localObject2).get(i);
            localObject3 = (DialogsSearchAdapter.RecentSearchObject)localLongSparseArray.get(((TLRPC.User)localObject1).id);
            if (localObject3 != null) {
              ((DialogsSearchAdapter.RecentSearchObject)localObject3).object = ((TLObject)localObject1);
            }
          }
          else
          {
            Collections.sort(localException, new Comparator()
            {
              public int compare(DialogsSearchAdapter.RecentSearchObject paramAnonymous2RecentSearchObject1, DialogsSearchAdapter.RecentSearchObject paramAnonymous2RecentSearchObject2)
              {
                if (paramAnonymous2RecentSearchObject1.date < paramAnonymous2RecentSearchObject2.date) {
                  return 1;
                }
                if (paramAnonymous2RecentSearchObject1.date > paramAnonymous2RecentSearchObject2.date) {
                  return -1;
                }
                return 0;
              }
            });
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                DialogsSearchAdapter.this.setRecentSearch(localException, localLongSparseArray);
              }
            });
            return;
            label710:
            i += 1;
            break;
          }
          i += 1;
        }
      }
    });
  }
  
  public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
  {
    Object localObject1;
    boolean bool1;
    label129:
    label147:
    label253:
    label280:
    label424:
    label430:
    int i;
    switch (paramViewHolder.getItemViewType())
    {
    case 3: 
    default: 
      return;
    case 0: 
      ProfileSearchCell localProfileSearchCell = (ProfileSearchCell)paramViewHolder.itemView;
      Object localObject2 = null;
      paramViewHolder = null;
      TLRPC.EncryptedChat localEncryptedChat = null;
      Object localObject6 = null;
      Object localObject5 = null;
      boolean bool2 = false;
      localObject1 = null;
      Object localObject7 = getItem(paramInt);
      Object localObject3;
      Object localObject4;
      if ((localObject7 instanceof TLRPC.User))
      {
        localObject2 = (TLRPC.User)localObject7;
        localObject3 = ((TLRPC.User)localObject2).username;
        localObject4 = paramViewHolder;
        if (!isRecentSearchDisplayed()) {
          break label430;
        }
        bool2 = true;
        if (paramInt == getItemCount() - 1) {
          break label424;
        }
        bool1 = true;
        localProfileSearchCell.useSeparator = bool1;
        bool1 = bool2;
        localObject1 = localObject6;
        paramViewHolder = (RecyclerView.ViewHolder)localObject5;
        boolean bool3 = false;
        localObject5 = paramViewHolder;
        localObject3 = localObject1;
        bool2 = bool3;
        if (localObject2 != null)
        {
          localObject5 = paramViewHolder;
          localObject3 = localObject1;
          bool2 = bool3;
          if (((TLRPC.User)localObject2).id == this.selfUserId)
          {
            localObject5 = LocaleController.getString("SavedMessages", 2131494293);
            localObject3 = null;
            bool2 = true;
          }
        }
        paramViewHolder = (RecyclerView.ViewHolder)localObject3;
        if (localObject4 != null)
        {
          paramViewHolder = (RecyclerView.ViewHolder)localObject3;
          if (((TLRPC.Chat)localObject4).participants_count != 0)
          {
            if ((!ChatObject.isChannel((TLRPC.Chat)localObject4)) || (((TLRPC.Chat)localObject4).megagroup)) {
              break label979;
            }
            paramViewHolder = LocaleController.formatPluralString("Subscribers", ((TLRPC.Chat)localObject4).participants_count);
            if (!(localObject3 instanceof SpannableStringBuilder)) {
              break label994;
            }
            ((SpannableStringBuilder)localObject3).append(", ").append(paramViewHolder);
            paramViewHolder = (RecyclerView.ViewHolder)localObject3;
          }
        }
        if (localObject2 == null) {
          break label1031;
        }
      }
      for (;;)
      {
        localProfileSearchCell.setData((TLObject)localObject2, localEncryptedChat, (CharSequence)localObject5, paramViewHolder, bool1, bool2);
        return;
        if ((localObject7 instanceof TLRPC.Chat))
        {
          paramViewHolder = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(((TLRPC.Chat)localObject7).id));
          localObject4 = paramViewHolder;
          if (paramViewHolder == null) {
            localObject4 = (TLRPC.Chat)localObject7;
          }
          localObject3 = ((TLRPC.Chat)localObject4).username;
          break;
        }
        localObject4 = paramViewHolder;
        localObject3 = localObject1;
        if (!(localObject7 instanceof TLRPC.EncryptedChat)) {
          break;
        }
        localEncryptedChat = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(((TLRPC.EncryptedChat)localObject7).id));
        localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(localEncryptedChat.user_id));
        localObject4 = paramViewHolder;
        localObject3 = localObject1;
        break;
        bool1 = false;
        break label129;
        paramViewHolder = this.searchAdapterHelper.getGlobalSearch();
        int j = this.searchResult.size();
        int k = this.searchAdapterHelper.getLocalServerSearch().size();
        if (paramViewHolder.isEmpty())
        {
          i = 0;
          if ((paramInt == getItemCount() - 1) || (paramInt == j + k - 1) || (paramInt == j + i + k - 1)) {
            break label680;
          }
        }
        for (bool1 = true;; bool1 = false)
        {
          localProfileSearchCell.useSeparator = bool1;
          if (paramInt >= this.searchResult.size()) {
            break label686;
          }
          localObject3 = (CharSequence)this.searchResultNames.get(paramInt);
          paramViewHolder = (RecyclerView.ViewHolder)localObject3;
          localObject1 = localObject6;
          bool1 = bool2;
          if (localObject3 == null) {
            break;
          }
          paramViewHolder = (RecyclerView.ViewHolder)localObject3;
          localObject1 = localObject6;
          bool1 = bool2;
          if (localObject2 == null) {
            break;
          }
          paramViewHolder = (RecyclerView.ViewHolder)localObject3;
          localObject1 = localObject6;
          bool1 = bool2;
          if (((TLRPC.User)localObject2).username == null) {
            break;
          }
          paramViewHolder = (RecyclerView.ViewHolder)localObject3;
          localObject1 = localObject6;
          bool1 = bool2;
          if (((TLRPC.User)localObject2).username.length() <= 0) {
            break;
          }
          paramViewHolder = (RecyclerView.ViewHolder)localObject3;
          localObject1 = localObject6;
          bool1 = bool2;
          if (!((CharSequence)localObject3).toString().startsWith("@" + ((TLRPC.User)localObject2).username)) {
            break;
          }
          localObject1 = localObject3;
          paramViewHolder = null;
          bool1 = bool2;
          break;
          i = paramViewHolder.size() + 1;
          break label468;
        }
        localObject7 = this.searchAdapterHelper.getLastFoundUsername();
        paramViewHolder = (RecyclerView.ViewHolder)localObject5;
        localObject1 = localObject6;
        bool1 = bool2;
        if (TextUtils.isEmpty((CharSequence)localObject7)) {
          break label147;
        }
        paramViewHolder = null;
        localObject1 = null;
        if (localObject2 != null)
        {
          paramViewHolder = ContactsController.formatName(((TLRPC.User)localObject2).first_name, ((TLRPC.User)localObject2).last_name);
          localObject1 = paramViewHolder.toLowerCase();
        }
        for (;;)
        {
          if (paramViewHolder == null) {
            break label828;
          }
          paramInt = ((String)localObject1).indexOf((String)localObject7);
          if (paramInt == -1) {
            break label828;
          }
          paramViewHolder = new SpannableStringBuilder(paramViewHolder);
          paramViewHolder.setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), paramInt, ((String)localObject7).length() + paramInt, 33);
          localObject1 = localObject6;
          bool1 = bool2;
          break;
          if (localObject4 != null)
          {
            paramViewHolder = ((TLRPC.Chat)localObject4).title;
            localObject1 = paramViewHolder.toLowerCase();
          }
        }
        paramViewHolder = (RecyclerView.ViewHolder)localObject5;
        localObject1 = localObject6;
        bool1 = bool2;
        if (localObject3 == null) {
          break label147;
        }
        paramViewHolder = (RecyclerView.ViewHolder)localObject7;
        if (((String)localObject7).startsWith("@")) {
          paramViewHolder = ((String)localObject7).substring(1);
        }
        try
        {
          localObject1 = new SpannableStringBuilder();
          ((SpannableStringBuilder)localObject1).append("@");
          ((SpannableStringBuilder)localObject1).append((CharSequence)localObject3);
          paramInt = ((String)localObject3).toLowerCase().indexOf(paramViewHolder);
          if (paramInt != -1)
          {
            i = paramViewHolder.length();
            if (paramInt != 0) {
              break label953;
            }
            i += 1;
          }
          for (;;)
          {
            ((SpannableStringBuilder)localObject1).setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), paramInt, paramInt + i, 33);
            paramViewHolder = (RecyclerView.ViewHolder)localObject5;
            bool1 = bool2;
            break;
            paramInt += 1;
          }
        }
        catch (Exception paramViewHolder)
        {
          localObject1 = localObject3;
          FileLog.e(paramViewHolder);
          paramViewHolder = (RecyclerView.ViewHolder)localObject5;
          bool1 = bool2;
        }
        paramViewHolder = LocaleController.formatPluralString("Members", ((TLRPC.Chat)localObject4).participants_count);
        break label253;
        if (!TextUtils.isEmpty((CharSequence)localObject3))
        {
          paramViewHolder = TextUtils.concat(new CharSequence[] { localObject3, ", ", paramViewHolder });
          break label280;
        }
        break label280;
        localObject2 = localObject4;
      }
    case 1: 
      paramViewHolder = (GraySectionCell)paramViewHolder.itemView;
      if (isRecentSearchDisplayed())
      {
        if (!DataQuery.getInstance(this.currentAccount).hints.isEmpty()) {}
        for (i = 2; paramInt < i; i = 0)
        {
          paramViewHolder.setText(LocaleController.getString("ChatHints", 2131493224).toUpperCase());
          return;
        }
        paramViewHolder.setText(LocaleController.getString("Recent", 2131494215).toUpperCase());
        return;
      }
      if (!this.searchResultHashtags.isEmpty())
      {
        paramViewHolder.setText(LocaleController.getString("Hashtags", 2131493647).toUpperCase());
        return;
      }
      if ((!this.searchAdapterHelper.getGlobalSearch().isEmpty()) && (paramInt == this.searchResult.size() + this.searchAdapterHelper.getLocalServerSearch().size()))
      {
        paramViewHolder.setText(LocaleController.getString("GlobalSearch", 2131493628));
        return;
      }
      paramViewHolder.setText(LocaleController.getString("SearchMessages", 2131494307));
      return;
    case 2: 
      paramViewHolder = (DialogCell)paramViewHolder.itemView;
      if (paramInt != getItemCount() - 1) {}
      for (bool1 = true;; bool1 = false)
      {
        paramViewHolder.useSeparator = bool1;
        localObject1 = (MessageObject)getItem(paramInt);
        paramViewHolder.setDialog(((MessageObject)localObject1).getDialogId(), (MessageObject)localObject1, ((MessageObject)localObject1).messageOwner.date);
        return;
      }
    case 4: 
      label468:
      label680:
      label686:
      label828:
      label953:
      label979:
      label994:
      label1031:
      paramViewHolder = (HashtagSearchCell)paramViewHolder.itemView;
      paramViewHolder.setText((CharSequence)this.searchResultHashtags.get(paramInt - 1));
      if (paramInt != this.searchResultHashtags.size()) {}
      for (bool1 = true;; bool1 = false)
      {
        paramViewHolder.setNeedDivider(bool1);
        return;
      }
    }
    ((CategoryAdapterRecycler)((RecyclerListView)paramViewHolder.itemView).getAdapter()).setIndex(paramInt / 2);
  }
  
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    paramViewGroup = null;
    switch (paramInt)
    {
    default: 
      if (paramInt == 5) {
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, AndroidUtilities.dp(100.0F)));
      }
      break;
    }
    for (;;)
    {
      return new RecyclerListView.Holder(paramViewGroup);
      paramViewGroup = new ProfileSearchCell(this.mContext);
      break;
      paramViewGroup = new GraySectionCell(this.mContext);
      break;
      paramViewGroup = new DialogCell(this.mContext, false);
      break;
      paramViewGroup = new LoadingCell(this.mContext);
      break;
      paramViewGroup = new HashtagSearchCell(this.mContext);
      break;
      RecyclerListView local9 = new RecyclerListView(this.mContext)
      {
        public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          if ((getParent() != null) && (getParent().getParent() != null)) {
            getParent().getParent().requestDisallowInterceptTouchEvent(true);
          }
          return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
        }
      };
      local9.setTag(Integer.valueOf(9));
      local9.setItemAnimator(null);
      local9.setLayoutAnimation(null);
      paramViewGroup = new LinearLayoutManager(this.mContext)
      {
        public boolean supportsPredictiveItemAnimations()
        {
          return false;
        }
      };
      paramViewGroup.setOrientation(0);
      local9.setLayoutManager(paramViewGroup);
      local9.setAdapter(new CategoryAdapterRecycler(null));
      local9.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (DialogsSearchAdapter.this.delegate != null) {
            DialogsSearchAdapter.this.delegate.didPressedOnSubDialog(((Integer)paramAnonymousView.getTag()).intValue());
          }
        }
      });
      local9.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (DialogsSearchAdapter.this.delegate != null) {
            DialogsSearchAdapter.this.delegate.needRemoveHint(((Integer)paramAnonymousView.getTag()).intValue());
          }
          return true;
        }
      });
      paramViewGroup = local9;
      this.innerListView = local9;
      break;
      paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
    }
  }
  
  public void putRecentSearch(final long paramLong, TLObject paramTLObject)
  {
    RecentSearchObject localRecentSearchObject = (RecentSearchObject)this.recentSearchObjectsById.get(paramLong);
    if (localRecentSearchObject == null)
    {
      localRecentSearchObject = new RecentSearchObject();
      this.recentSearchObjectsById.put(paramLong, localRecentSearchObject);
    }
    for (;;)
    {
      this.recentSearchObjects.add(0, localRecentSearchObject);
      localRecentSearchObject.did = paramLong;
      localRecentSearchObject.object = paramTLObject;
      localRecentSearchObject.date = ((int)(System.currentTimeMillis() / 1000L));
      notifyDataSetChanged();
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          try
          {
            SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(DialogsSearchAdapter.this.currentAccount).getDatabase().executeFast("REPLACE INTO search_recent VALUES(?, ?)");
            localSQLitePreparedStatement.requery();
            localSQLitePreparedStatement.bindLong(1, paramLong);
            localSQLitePreparedStatement.bindInteger(2, (int)(System.currentTimeMillis() / 1000L));
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
      this.recentSearchObjects.remove(localRecentSearchObject);
    }
  }
  
  public void searchDialogs(final String paramString)
  {
    if ((paramString != null) && (this.lastSearchText != null) && (paramString.equals(this.lastSearchText))) {
      return;
    }
    this.lastSearchText = paramString;
    try
    {
      if (this.searchTimer != null)
      {
        this.searchTimer.cancel();
        this.searchTimer = null;
      }
      if ((paramString == null) || (paramString.length() == 0))
      {
        this.searchAdapterHelper.unloadRecentHashtags();
        this.searchResult.clear();
        this.searchResultNames.clear();
        this.searchResultHashtags.clear();
        this.searchAdapterHelper.mergeResults(null);
        if (this.needMessagesSearch != 2) {
          this.searchAdapterHelper.queryServerSearch(null, true, true, true, true, 0, false);
        }
        searchMessagesInternal(null);
        notifyDataSetChanged();
        return;
      }
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
      if (this.needMessagesSearch == 2) {
        break label321;
      }
    }
    final int i;
    if ((paramString.startsWith("#")) && (paramString.length() == 1))
    {
      this.messagesSearchEndReached = true;
      if (this.searchAdapterHelper.loadRecentHashtags())
      {
        this.searchResultMessages.clear();
        this.searchResultHashtags.clear();
        ArrayList localArrayList = this.searchAdapterHelper.getHashtags();
        i = 0;
        while (i < localArrayList.size())
        {
          this.searchResultHashtags.add(((SearchAdapterHelper.HashtagObject)localArrayList.get(i)).hashtag);
          i += 1;
        }
        if (this.delegate != null) {
          this.delegate.searchStateChanged(false);
        }
        notifyDataSetChanged();
      }
    }
    for (;;)
    {
      i = this.lastSearchId + 1;
      this.lastSearchId = i;
      this.searchTimer = new Timer();
      this.searchTimer.schedule(new TimerTask()
      {
        public void run()
        {
          try
          {
            cancel();
            DialogsSearchAdapter.this.searchTimer.cancel();
            DialogsSearchAdapter.access$1602(DialogsSearchAdapter.this, null);
            DialogsSearchAdapter.this.searchDialogsInternal(paramString, i);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                if (DialogsSearchAdapter.this.needMessagesSearch != 2) {
                  DialogsSearchAdapter.this.searchAdapterHelper.queryServerSearch(DialogsSearchAdapter.8.this.val$query, true, true, true, true, 0, false);
                }
                DialogsSearchAdapter.this.searchMessagesInternal(DialogsSearchAdapter.8.this.val$query);
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
      }, 200L, 300L);
      return;
      if (this.delegate == null) {
        break;
      }
      this.delegate.searchStateChanged(true);
      break;
      label321:
      this.searchResultHashtags.clear();
      notifyDataSetChanged();
    }
  }
  
  public void setDelegate(DialogsSearchAdapterDelegate paramDialogsSearchAdapterDelegate)
  {
    this.delegate = paramDialogsSearchAdapterDelegate;
  }
  
  private class CategoryAdapterRecycler
    extends RecyclerListView.SelectionAdapter
  {
    private CategoryAdapterRecycler() {}
    
    public int getItemCount()
    {
      return DataQuery.getInstance(DialogsSearchAdapter.this.currentAccount).hints.size();
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      HintDialogCell localHintDialogCell = (HintDialogCell)paramViewHolder.itemView;
      TLRPC.TL_topPeer localTL_topPeer = (TLRPC.TL_topPeer)DataQuery.getInstance(DialogsSearchAdapter.this.currentAccount).hints.get(paramInt);
      new TLRPC.TL_dialog();
      paramViewHolder = null;
      String str = null;
      paramInt = 0;
      Object localObject;
      if (localTL_topPeer.peer.user_id != 0)
      {
        paramInt = localTL_topPeer.peer.user_id;
        localObject = MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).getUser(Integer.valueOf(localTL_topPeer.peer.user_id));
        localHintDialogCell.setTag(Integer.valueOf(paramInt));
        str = "";
        if (localObject == null) {
          break label236;
        }
        localObject = ContactsController.formatName(((TLRPC.User)localObject).first_name, ((TLRPC.User)localObject).last_name);
      }
      for (;;)
      {
        localHintDialogCell.setDialog(paramInt, true, (CharSequence)localObject);
        return;
        if (localTL_topPeer.peer.channel_id != 0)
        {
          paramInt = -localTL_topPeer.peer.channel_id;
          paramViewHolder = MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).getChat(Integer.valueOf(localTL_topPeer.peer.channel_id));
          localObject = str;
          break;
        }
        localObject = str;
        if (localTL_topPeer.peer.chat_id == 0) {
          break;
        }
        paramInt = -localTL_topPeer.peer.chat_id;
        paramViewHolder = MessagesController.getInstance(DialogsSearchAdapter.this.currentAccount).getChat(Integer.valueOf(localTL_topPeer.peer.chat_id));
        localObject = str;
        break;
        label236:
        localObject = str;
        if (paramViewHolder != null) {
          localObject = paramViewHolder.title;
        }
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = new HintDialogCell(DialogsSearchAdapter.this.mContext);
      paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(80.0F), AndroidUtilities.dp(100.0F)));
      return new RecyclerListView.Holder(paramViewGroup);
    }
    
    public void setIndex(int paramInt)
    {
      notifyDataSetChanged();
    }
  }
  
  private class DialogSearchResult
  {
    public int date;
    public CharSequence name;
    public TLObject object;
    
    private DialogSearchResult() {}
  }
  
  public static abstract interface DialogsSearchAdapterDelegate
  {
    public abstract void didPressedOnSubDialog(long paramLong);
    
    public abstract void needRemoveHint(int paramInt);
    
    public abstract void searchStateChanged(boolean paramBoolean);
  }
  
  protected static class RecentSearchObject
  {
    int date;
    long did;
    TLObject object;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/DialogsSearchAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */