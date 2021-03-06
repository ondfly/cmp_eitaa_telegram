package org.telegram.ui.Adapters;

import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsBanned;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsKicked;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsSearch;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_contacts_found;
import org.telegram.tgnet.TLRPC.TL_contacts_search;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.User;

public class SearchAdapterHelper
{
  private boolean allResultsAreGlobal;
  private int channelLastReqId;
  private int channelLastReqId2;
  private int channelReqId = 0;
  private int channelReqId2 = 0;
  private int currentAccount = UserConfig.selectedAccount;
  private SearchAdapterHelperDelegate delegate;
  private ArrayList<TLObject> globalSearch = new ArrayList();
  private SparseArray<TLObject> globalSearchMap = new SparseArray();
  private ArrayList<TLRPC.ChannelParticipant> groupSearch = new ArrayList();
  private ArrayList<TLRPC.ChannelParticipant> groupSearch2 = new ArrayList();
  private ArrayList<HashtagObject> hashtags;
  private HashMap<String, HashtagObject> hashtagsByText;
  private boolean hashtagsLoadedFromDb = false;
  private String lastFoundChannel;
  private String lastFoundChannel2;
  private String lastFoundUsername = null;
  private int lastReqId;
  private ArrayList<TLObject> localSearchResults;
  private ArrayList<TLObject> localServerSearch = new ArrayList();
  private int reqId = 0;
  
  public SearchAdapterHelper(boolean paramBoolean)
  {
    this.allResultsAreGlobal = paramBoolean;
  }
  
  private void putRecentHashtags(final ArrayList<HashtagObject> paramArrayList)
  {
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).getDatabase().beginTransaction();
          SQLitePreparedStatement localSQLitePreparedStatement = MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).getDatabase().executeFast("REPLACE INTO hashtag_recent_v2 VALUES(?, ?)");
          int i = 0;
          for (;;)
          {
            if ((i >= paramArrayList.size()) || (i == 100))
            {
              localSQLitePreparedStatement.dispose();
              MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).getDatabase().commitTransaction();
              if (paramArrayList.size() < 100) {
                break label244;
              }
              MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).getDatabase().beginTransaction();
              i = 100;
              while (i < paramArrayList.size())
              {
                MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).getDatabase().executeFast("DELETE FROM hashtag_recent_v2 WHERE id = '" + ((SearchAdapterHelper.HashtagObject)paramArrayList.get(i)).hashtag + "'").stepThis().dispose();
                i += 1;
              }
            }
            SearchAdapterHelper.HashtagObject localHashtagObject = (SearchAdapterHelper.HashtagObject)paramArrayList.get(i);
            localSQLitePreparedStatement.requery();
            localSQLitePreparedStatement.bindString(1, localHashtagObject.hashtag);
            localSQLitePreparedStatement.bindInteger(2, localHashtagObject.date);
            localSQLitePreparedStatement.step();
            i += 1;
          }
          MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).getDatabase().commitTransaction();
          label244:
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void addHashtagsFromMessage(CharSequence paramCharSequence)
  {
    if (paramCharSequence == null) {}
    int i;
    do
    {
      return;
      i = 0;
      Matcher localMatcher = Pattern.compile("(^|\\s)#[\\w@\\.]+").matcher(paramCharSequence);
      if (localMatcher.find())
      {
        int j = localMatcher.start();
        int k = localMatcher.end();
        i = j;
        if (paramCharSequence.charAt(j) != '@')
        {
          i = j;
          if (paramCharSequence.charAt(j) != '#') {
            i = j + 1;
          }
        }
        String str = paramCharSequence.subSequence(i, k).toString();
        if (this.hashtagsByText == null)
        {
          this.hashtagsByText = new HashMap();
          this.hashtags = new ArrayList();
        }
        HashtagObject localHashtagObject = (HashtagObject)this.hashtagsByText.get(str);
        if (localHashtagObject == null)
        {
          localHashtagObject = new HashtagObject();
          localHashtagObject.hashtag = str;
          this.hashtagsByText.put(localHashtagObject.hashtag, localHashtagObject);
        }
        for (;;)
        {
          localHashtagObject.date = ((int)(System.currentTimeMillis() / 1000L));
          this.hashtags.add(0, localHashtagObject);
          i = 1;
          break;
          this.hashtags.remove(localHashtagObject);
        }
      }
    } while (i == 0);
    putRecentHashtags(this.hashtags);
  }
  
  public void clearRecentHashtags()
  {
    this.hashtags = new ArrayList();
    this.hashtagsByText = new HashMap();
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).getDatabase().executeFast("DELETE FROM hashtag_recent_v2 WHERE 1").stepThis().dispose();
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
  }
  
  public ArrayList<TLObject> getGlobalSearch()
  {
    return this.globalSearch;
  }
  
  public ArrayList<TLRPC.ChannelParticipant> getGroupSearch()
  {
    return this.groupSearch;
  }
  
  public ArrayList<TLRPC.ChannelParticipant> getGroupSearch2()
  {
    return this.groupSearch2;
  }
  
  public ArrayList<HashtagObject> getHashtags()
  {
    return this.hashtags;
  }
  
  public String getLastFoundChannel()
  {
    return this.lastFoundChannel;
  }
  
  public String getLastFoundChannel2()
  {
    return this.lastFoundChannel2;
  }
  
  public String getLastFoundUsername()
  {
    return this.lastFoundUsername;
  }
  
  public ArrayList<TLObject> getLocalServerSearch()
  {
    return this.localServerSearch;
  }
  
  public boolean loadRecentHashtags()
  {
    if (this.hashtagsLoadedFromDb) {
      return true;
    }
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        final ArrayList localArrayList;
        final HashMap localHashMap;
        try
        {
          SQLiteCursor localSQLiteCursor = MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).getDatabase().queryFinalized("SELECT id, date FROM hashtag_recent_v2 WHERE 1", new Object[0]);
          localArrayList = new ArrayList();
          localHashMap = new HashMap();
          while (localSQLiteCursor.next())
          {
            SearchAdapterHelper.HashtagObject localHashtagObject = new SearchAdapterHelper.HashtagObject();
            localHashtagObject.hashtag = localSQLiteCursor.stringValue(0);
            localHashtagObject.date = localSQLiteCursor.intValue(1);
            localArrayList.add(localHashtagObject);
            localHashMap.put(localHashtagObject.hashtag, localHashtagObject);
          }
          localException.dispose();
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        Collections.sort(localArrayList, new Comparator()
        {
          public int compare(SearchAdapterHelper.HashtagObject paramAnonymous2HashtagObject1, SearchAdapterHelper.HashtagObject paramAnonymous2HashtagObject2)
          {
            if (paramAnonymous2HashtagObject1.date < paramAnonymous2HashtagObject2.date) {
              return 1;
            }
            if (paramAnonymous2HashtagObject1.date > paramAnonymous2HashtagObject2.date) {
              return -1;
            }
            return 0;
          }
        });
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            SearchAdapterHelper.this.setHashtags(localArrayList, localHashMap);
          }
        });
      }
    });
    return false;
  }
  
  public void mergeResults(ArrayList<TLObject> paramArrayList)
  {
    this.localSearchResults = paramArrayList;
    if ((this.globalSearchMap.size() == 0) || (paramArrayList == null)) {
      return;
    }
    int j = paramArrayList.size();
    int i = 0;
    label27:
    Object localObject;
    if (i < j)
    {
      localObject = (TLObject)paramArrayList.get(i);
      if (!(localObject instanceof TLRPC.User)) {
        break label118;
      }
      localObject = (TLRPC.User)localObject;
      localObject = (TLRPC.User)this.globalSearchMap.get(((TLRPC.User)localObject).id);
      if (localObject != null)
      {
        this.globalSearch.remove(localObject);
        this.localServerSearch.remove(localObject);
        this.globalSearchMap.remove(((TLRPC.User)localObject).id);
      }
    }
    for (;;)
    {
      i += 1;
      break label27;
      break;
      label118:
      if ((localObject instanceof TLRPC.Chat))
      {
        localObject = (TLRPC.Chat)localObject;
        localObject = (TLRPC.Chat)this.globalSearchMap.get(-((TLRPC.Chat)localObject).id);
        if (localObject != null)
        {
          this.globalSearch.remove(localObject);
          this.localServerSearch.remove(localObject);
          this.globalSearchMap.remove(-((TLRPC.Chat)localObject).id);
        }
      }
    }
  }
  
  public void queryServerSearch(final String paramString, boolean paramBoolean1, final boolean paramBoolean2, final boolean paramBoolean3, final boolean paramBoolean4, final int paramInt, boolean paramBoolean5)
  {
    if (this.reqId != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
      this.reqId = 0;
    }
    if (this.channelReqId != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.channelReqId, true);
      this.channelReqId = 0;
    }
    if (this.channelReqId2 != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.channelReqId2, true);
      this.channelReqId2 = 0;
    }
    if (paramString == null)
    {
      this.groupSearch.clear();
      this.groupSearch2.clear();
      this.globalSearch.clear();
      this.globalSearchMap.clear();
      this.localServerSearch.clear();
      this.lastReqId = 0;
      this.channelLastReqId = 0;
      this.channelLastReqId2 = 0;
      this.delegate.onDataSetChanged();
    }
    label496:
    for (;;)
    {
      return;
      Object localObject;
      if ((paramString.length() > 0) && (paramInt != 0))
      {
        localObject = new TLRPC.TL_channels_getParticipants();
        if (paramBoolean5)
        {
          ((TLRPC.TL_channels_getParticipants)localObject).filter = new TLRPC.TL_channelParticipantsBanned();
          ((TLRPC.TL_channels_getParticipants)localObject).filter.q = paramString;
          ((TLRPC.TL_channels_getParticipants)localObject).limit = 50;
          ((TLRPC.TL_channels_getParticipants)localObject).offset = 0;
          ((TLRPC.TL_channels_getParticipants)localObject).channel = MessagesController.getInstance(this.currentAccount).getInputChannel(paramInt);
          final int i = this.channelLastReqId + 1;
          this.channelLastReqId = i;
          this.channelReqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if ((SearchAdapterHelper.1.this.val$currentReqId == SearchAdapterHelper.this.channelLastReqId) && (paramAnonymousTL_error == null))
                  {
                    TLRPC.TL_channels_channelParticipants localTL_channels_channelParticipants = (TLRPC.TL_channels_channelParticipants)paramAnonymousTLObject;
                    SearchAdapterHelper.access$102(SearchAdapterHelper.this, SearchAdapterHelper.1.this.val$query.toLowerCase());
                    MessagesController.getInstance(SearchAdapterHelper.this.currentAccount).putUsers(localTL_channels_channelParticipants.users, false);
                    SearchAdapterHelper.access$302(SearchAdapterHelper.this, localTL_channels_channelParticipants.participants);
                    SearchAdapterHelper.this.delegate.onDataSetChanged();
                  }
                  SearchAdapterHelper.access$502(SearchAdapterHelper.this, 0);
                }
              });
            }
          }, 2);
          if (paramBoolean5)
          {
            localObject = new TLRPC.TL_channels_getParticipants();
            ((TLRPC.TL_channels_getParticipants)localObject).filter = new TLRPC.TL_channelParticipantsKicked();
            ((TLRPC.TL_channels_getParticipants)localObject).filter.q = paramString;
            ((TLRPC.TL_channels_getParticipants)localObject).limit = 50;
            ((TLRPC.TL_channels_getParticipants)localObject).offset = 0;
            ((TLRPC.TL_channels_getParticipants)localObject).channel = MessagesController.getInstance(this.currentAccount).getInputChannel(paramInt);
            paramInt = this.channelLastReqId2 + 1;
            this.channelLastReqId2 = paramInt;
            this.channelReqId2 = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
            {
              public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
              {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    if ((SearchAdapterHelper.2.this.val$currentReqId2 == SearchAdapterHelper.this.channelLastReqId2) && (paramAnonymousTL_error == null))
                    {
                      TLRPC.TL_channels_channelParticipants localTL_channels_channelParticipants = (TLRPC.TL_channels_channelParticipants)paramAnonymousTLObject;
                      SearchAdapterHelper.access$702(SearchAdapterHelper.this, SearchAdapterHelper.2.this.val$query.toLowerCase());
                      MessagesController.getInstance(SearchAdapterHelper.this.currentAccount).putUsers(localTL_channels_channelParticipants.users, false);
                      SearchAdapterHelper.access$802(SearchAdapterHelper.this, localTL_channels_channelParticipants.participants);
                      SearchAdapterHelper.this.delegate.onDataSetChanged();
                    }
                    SearchAdapterHelper.access$902(SearchAdapterHelper.this, 0);
                  }
                });
              }
            }, 2);
          }
        }
      }
      for (;;)
      {
        if (!paramBoolean1) {
          break label496;
        }
        if (paramString.length() <= 0) {
          break label498;
        }
        localObject = new TLRPC.TL_contacts_search();
        ((TLRPC.TL_contacts_search)localObject).q = paramString;
        ((TLRPC.TL_contacts_search)localObject).limit = 50;
        paramInt = this.lastReqId + 1;
        this.lastReqId = paramInt;
        this.reqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                if ((SearchAdapterHelper.3.this.val$currentReqId == SearchAdapterHelper.this.lastReqId) && (paramAnonymousTL_error == null))
                {
                  TLRPC.TL_contacts_found localTL_contacts_found = (TLRPC.TL_contacts_found)paramAnonymousTLObject;
                  SearchAdapterHelper.this.globalSearch.clear();
                  SearchAdapterHelper.this.globalSearchMap.clear();
                  SearchAdapterHelper.this.localServerSearch.clear();
                  MessagesController.getInstance(SearchAdapterHelper.this.currentAccount).putChats(localTL_contacts_found.chats, false);
                  MessagesController.getInstance(SearchAdapterHelper.this.currentAccount).putUsers(localTL_contacts_found.users, false);
                  MessagesStorage.getInstance(SearchAdapterHelper.this.currentAccount).putUsersAndChats(localTL_contacts_found.users, localTL_contacts_found.chats, true, true);
                  SparseArray localSparseArray1 = new SparseArray();
                  SparseArray localSparseArray2 = new SparseArray();
                  int i = 0;
                  Object localObject1;
                  while (i < localTL_contacts_found.chats.size())
                  {
                    localObject1 = (TLRPC.Chat)localTL_contacts_found.chats.get(i);
                    localSparseArray1.put(((TLRPC.Chat)localObject1).id, localObject1);
                    i += 1;
                  }
                  i = 0;
                  while (i < localTL_contacts_found.users.size())
                  {
                    localObject1 = (TLRPC.User)localTL_contacts_found.users.get(i);
                    localSparseArray2.put(((TLRPC.User)localObject1).id, localObject1);
                    i += 1;
                  }
                  i = 0;
                  Object localObject2;
                  label291:
                  label293:
                  TLRPC.Peer localPeer1;
                  Object localObject3;
                  while (i < 2)
                  {
                    int j;
                    TLRPC.Peer localPeer2;
                    if (i == 0)
                    {
                      if (!SearchAdapterHelper.this.allResultsAreGlobal)
                      {
                        i += 1;
                        continue;
                      }
                      localObject2 = localTL_contacts_found.my_results;
                      j = 0;
                      if (j < ((ArrayList)localObject2).size())
                      {
                        localPeer2 = (TLRPC.Peer)((ArrayList)localObject2).get(j);
                        localPeer1 = null;
                        localObject1 = null;
                        if (localPeer2.user_id == 0) {
                          break label372;
                        }
                        localObject3 = (TLRPC.User)localSparseArray2.get(localPeer2.user_id);
                        label341:
                        if (localObject1 == null) {
                          break label471;
                        }
                        if (SearchAdapterHelper.3.this.val$allowChats) {
                          break label434;
                        }
                      }
                    }
                    for (;;)
                    {
                      j += 1;
                      break label293;
                      break;
                      localObject2 = localTL_contacts_found.results;
                      break label291;
                      label372:
                      if (localPeer2.chat_id != 0)
                      {
                        localObject1 = (TLRPC.Chat)localSparseArray1.get(localPeer2.chat_id);
                        localObject3 = localPeer1;
                        break label341;
                      }
                      localObject3 = localPeer1;
                      if (localPeer2.channel_id == 0) {
                        break label341;
                      }
                      localObject1 = (TLRPC.Chat)localSparseArray1.get(localPeer2.channel_id);
                      localObject3 = localPeer1;
                      break label341;
                      label434:
                      SearchAdapterHelper.this.globalSearch.add(localObject1);
                      SearchAdapterHelper.this.globalSearchMap.put(-((TLRPC.Chat)localObject1).id, localObject1);
                      continue;
                      label471:
                      if ((localObject3 != null) && ((SearchAdapterHelper.3.this.val$allowBots) || (!((TLRPC.User)localObject3).bot)) && ((SearchAdapterHelper.3.this.val$allowSelf) || (!((TLRPC.User)localObject3).self)))
                      {
                        SearchAdapterHelper.this.globalSearch.add(localObject3);
                        SearchAdapterHelper.this.globalSearchMap.put(((TLRPC.User)localObject3).id, localObject3);
                      }
                    }
                  }
                  if (!SearchAdapterHelper.this.allResultsAreGlobal)
                  {
                    i = 0;
                    if (i < localTL_contacts_found.my_results.size())
                    {
                      localPeer1 = (TLRPC.Peer)localTL_contacts_found.my_results.get(i);
                      localObject3 = null;
                      localObject1 = null;
                      if (localPeer1.user_id != 0)
                      {
                        localObject2 = (TLRPC.User)localSparseArray2.get(localPeer1.user_id);
                        label620:
                        if (localObject1 == null) {
                          break label727;
                        }
                        SearchAdapterHelper.this.localServerSearch.add(localObject1);
                        SearchAdapterHelper.this.globalSearchMap.put(-((TLRPC.Chat)localObject1).id, localObject1);
                      }
                      for (;;)
                      {
                        i += 1;
                        break;
                        if (localPeer1.chat_id != 0)
                        {
                          localObject1 = (TLRPC.Chat)localSparseArray1.get(localPeer1.chat_id);
                          localObject2 = localObject3;
                          break label620;
                        }
                        localObject2 = localObject3;
                        if (localPeer1.channel_id == 0) {
                          break label620;
                        }
                        localObject1 = (TLRPC.Chat)localSparseArray1.get(localPeer1.channel_id);
                        localObject2 = localObject3;
                        break label620;
                        label727:
                        if (localObject2 != null)
                        {
                          SearchAdapterHelper.this.localServerSearch.add(localObject2);
                          SearchAdapterHelper.this.globalSearchMap.put(((TLRPC.User)localObject2).id, localObject2);
                        }
                      }
                    }
                  }
                  SearchAdapterHelper.access$1502(SearchAdapterHelper.this, SearchAdapterHelper.3.this.val$query.toLowerCase());
                  if (SearchAdapterHelper.this.localSearchResults != null) {
                    SearchAdapterHelper.this.mergeResults(SearchAdapterHelper.this.localSearchResults);
                  }
                  SearchAdapterHelper.this.delegate.onDataSetChanged();
                }
                SearchAdapterHelper.access$1702(SearchAdapterHelper.this, 0);
              }
            });
          }
        }, 2);
        return;
        ((TLRPC.TL_channels_getParticipants)localObject).filter = new TLRPC.TL_channelParticipantsSearch();
        break;
        this.groupSearch.clear();
        this.groupSearch2.clear();
        this.channelLastReqId = 0;
        this.delegate.onDataSetChanged();
      }
    }
    label498:
    this.globalSearch.clear();
    this.globalSearchMap.clear();
    this.localServerSearch.clear();
    this.lastReqId = 0;
    this.delegate.onDataSetChanged();
  }
  
  public void setDelegate(SearchAdapterHelperDelegate paramSearchAdapterHelperDelegate)
  {
    this.delegate = paramSearchAdapterHelperDelegate;
  }
  
  public void setHashtags(ArrayList<HashtagObject> paramArrayList, HashMap<String, HashtagObject> paramHashMap)
  {
    this.hashtags = paramArrayList;
    this.hashtagsByText = paramHashMap;
    this.hashtagsLoadedFromDb = true;
    this.delegate.onSetHashtags(paramArrayList, paramHashMap);
  }
  
  public void unloadRecentHashtags()
  {
    this.hashtagsLoadedFromDb = false;
  }
  
  protected static final class DialogSearchResult
  {
    public int date;
    public CharSequence name;
    public TLObject object;
  }
  
  public static class HashtagObject
  {
    int date;
    String hashtag;
  }
  
  public static abstract interface SearchAdapterHelperDelegate
  {
    public abstract void onDataSetChanged();
    
    public abstract void onSetHashtags(ArrayList<SearchAdapterHelper.HashtagObject> paramArrayList, HashMap<String, SearchAdapterHelper.HashtagObject> paramHashMap);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/SearchAdapterHelper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */