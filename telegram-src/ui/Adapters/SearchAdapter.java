package org.telegram.ui.Adapters;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class SearchAdapter
  extends RecyclerListView.SelectionAdapter
{
  private boolean allowBots;
  private boolean allowChats;
  private boolean allowUsernameSearch;
  private int channelId;
  private SparseArray<?> checkedMap;
  private SparseArray<TLRPC.User> ignoreUsers;
  private Context mContext;
  private boolean onlyMutual;
  private SearchAdapterHelper searchAdapterHelper;
  private ArrayList<TLRPC.User> searchResult = new ArrayList();
  private ArrayList<CharSequence> searchResultNames = new ArrayList();
  private Timer searchTimer;
  private boolean useUserCell;
  
  public SearchAdapter(Context paramContext, SparseArray<TLRPC.User> paramSparseArray, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, int paramInt)
  {
    this.mContext = paramContext;
    this.ignoreUsers = paramSparseArray;
    this.onlyMutual = paramBoolean2;
    this.allowUsernameSearch = paramBoolean1;
    this.allowChats = paramBoolean3;
    this.allowBots = paramBoolean4;
    this.channelId = paramInt;
    this.searchAdapterHelper = new SearchAdapterHelper(true);
    this.searchAdapterHelper.setDelegate(new SearchAdapterHelper.SearchAdapterHelperDelegate()
    {
      public void onDataSetChanged()
      {
        SearchAdapter.this.notifyDataSetChanged();
      }
      
      public void onSetHashtags(ArrayList<SearchAdapterHelper.HashtagObject> paramAnonymousArrayList, HashMap<String, SearchAdapterHelper.HashtagObject> paramAnonymousHashMap) {}
    });
  }
  
  private void processSearch(final String paramString)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (SearchAdapter.this.allowUsernameSearch) {
          SearchAdapter.this.searchAdapterHelper.queryServerSearch(paramString, true, SearchAdapter.this.allowChats, SearchAdapter.this.allowBots, true, SearchAdapter.this.channelId, false);
        }
        final int i = UserConfig.selectedAccount;
        final ArrayList localArrayList = new ArrayList();
        localArrayList.addAll(ContactsController.getInstance(i).contacts);
        Utilities.searchQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            Object localObject2 = SearchAdapter.3.this.val$query.trim().toLowerCase();
            if (((String)localObject2).length() == 0)
            {
              SearchAdapter.this.updateSearchResults(new ArrayList(), new ArrayList());
              return;
            }
            String str1 = LocaleController.getInstance().getTranslitString((String)localObject2);
            Object localObject1;
            if (!((String)localObject2).equals(str1))
            {
              localObject1 = str1;
              if (str1.length() != 0) {}
            }
            else
            {
              localObject1 = null;
            }
            int i;
            String[] arrayOfString;
            ArrayList localArrayList;
            int j;
            label135:
            TLRPC.User localUser;
            if (localObject1 != null)
            {
              i = 1;
              arrayOfString = new String[i + 1];
              arrayOfString[0] = localObject2;
              if (localObject1 != null) {
                arrayOfString[1] = localObject1;
              }
              localObject2 = new ArrayList();
              localArrayList = new ArrayList();
              j = 0;
              if (j >= localArrayList.size()) {
                break label519;
              }
              localObject1 = (TLRPC.TL_contact)localArrayList.get(j);
              localUser = MessagesController.getInstance(i).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject1).user_id));
              if ((localUser.id != UserConfig.getInstance(i).getClientUserId()) && ((!SearchAdapter.this.onlyMutual) || (localUser.mutual_contact))) {
                break label230;
              }
            }
            label230:
            label380:
            label453:
            label509:
            label517:
            for (;;)
            {
              j += 1;
              break label135;
              i = 0;
              break;
              String str2 = ContactsController.formatName(localUser.first_name, localUser.last_name).toLowerCase();
              str1 = LocaleController.getInstance().getTranslitString(str2);
              localObject1 = str1;
              if (str2.equals(str1)) {
                localObject1 = null;
              }
              int m = 0;
              int n = arrayOfString.length;
              int k = 0;
              for (;;)
              {
                if (k >= n) {
                  break label517;
                }
                str1 = arrayOfString[k];
                if ((str2.startsWith(str1)) || (str2.contains(" " + str1)) || ((localObject1 != null) && ((((String)localObject1).startsWith(str1)) || (((String)localObject1).contains(" " + str1)))))
                {
                  i = 1;
                  if (i == 0) {
                    break label509;
                  }
                  if (i != 1) {
                    break label453;
                  }
                  localArrayList.add(AndroidUtilities.generateSearchName(localUser.first_name, localUser.last_name, str1));
                }
                for (;;)
                {
                  ((ArrayList)localObject2).add(localUser);
                  break;
                  i = m;
                  if (localUser.username == null) {
                    break label380;
                  }
                  i = m;
                  if (!localUser.username.startsWith(str1)) {
                    break label380;
                  }
                  i = 2;
                  break label380;
                  localArrayList.add(AndroidUtilities.generateSearchName("@" + localUser.username, null, "@" + str1));
                }
                k += 1;
                m = i;
              }
            }
            label519:
            SearchAdapter.this.updateSearchResults((ArrayList)localObject2, localArrayList);
          }
        });
      }
    });
  }
  
  private void updateSearchResults(final ArrayList<TLRPC.User> paramArrayList, final ArrayList<CharSequence> paramArrayList1)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        SearchAdapter.access$902(SearchAdapter.this, paramArrayList);
        SearchAdapter.access$1002(SearchAdapter.this, paramArrayList1);
        SearchAdapter.this.notifyDataSetChanged();
      }
    });
  }
  
  public TLObject getItem(int paramInt)
  {
    int i = this.searchResult.size();
    int j = this.searchAdapterHelper.getGlobalSearch().size();
    if ((paramInt >= 0) && (paramInt < i)) {
      return (TLObject)this.searchResult.get(paramInt);
    }
    if ((paramInt > i) && (paramInt <= j + i)) {
      return (TLObject)this.searchAdapterHelper.getGlobalSearch().get(paramInt - i - 1);
    }
    return null;
  }
  
  public int getItemCount()
  {
    int j = this.searchResult.size();
    int k = this.searchAdapterHelper.getGlobalSearch().size();
    int i = j;
    if (k != 0) {
      i = j + (k + 1);
    }
    return i;
  }
  
  public int getItemViewType(int paramInt)
  {
    if (paramInt == this.searchResult.size()) {
      return 1;
    }
    return 0;
  }
  
  public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
  {
    return paramViewHolder.getAdapterPosition() != this.searchResult.size();
  }
  
  public boolean isGlobalSearch(int paramInt)
  {
    int i = this.searchResult.size();
    int j = this.searchAdapterHelper.getGlobalSearch().size();
    if ((paramInt >= 0) && (paramInt < i)) {}
    while ((paramInt <= i) || (paramInt > j + i)) {
      return false;
    }
    return true;
  }
  
  public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
  {
    TLObject localTLObject;
    int i;
    String str1;
    Object localObject3;
    CharSequence localCharSequence;
    Object localObject2;
    Object localObject1;
    if (paramViewHolder.getItemViewType() == 0)
    {
      localTLObject = getItem(paramInt);
      if (localTLObject != null)
      {
        i = 0;
        str1 = null;
        if (!(localTLObject instanceof TLRPC.User)) {
          break label226;
        }
        str1 = ((TLRPC.User)localTLObject).username;
        i = ((TLRPC.User)localTLObject).id;
        localObject3 = null;
        localCharSequence = null;
        if (paramInt >= this.searchResult.size()) {
          break label256;
        }
        localCharSequence = (CharSequence)this.searchResultNames.get(paramInt);
        localObject2 = localCharSequence;
        localObject1 = localObject3;
        if (localCharSequence != null)
        {
          localObject2 = localCharSequence;
          localObject1 = localObject3;
          if (str1 != null)
          {
            localObject2 = localCharSequence;
            localObject1 = localObject3;
            if (str1.length() > 0)
            {
              localObject2 = localCharSequence;
              localObject1 = localObject3;
              if (localCharSequence.toString().startsWith("@" + str1))
              {
                localObject1 = localCharSequence;
                localObject2 = null;
              }
            }
          }
        }
        label171:
        if (!this.useUserCell) {
          break label450;
        }
        paramViewHolder = (UserCell)paramViewHolder.itemView;
        paramViewHolder.setData(localTLObject, (CharSequence)localObject2, (CharSequence)localObject1, 0);
        if (this.checkedMap != null) {
          if (this.checkedMap.indexOfKey(i) < 0) {
            break label444;
          }
        }
      }
    }
    label226:
    label256:
    label417:
    String str2;
    label444:
    for (boolean bool = true;; bool = false)
    {
      paramViewHolder.setChecked(bool, false);
      return;
      if (!(localTLObject instanceof TLRPC.Chat)) {
        break;
      }
      str1 = ((TLRPC.Chat)localTLObject).username;
      i = ((TLRPC.Chat)localTLObject).id;
      break;
      localObject2 = localCharSequence;
      localObject1 = localObject3;
      if (paramInt <= this.searchResult.size()) {
        break label171;
      }
      localObject2 = localCharSequence;
      localObject1 = localObject3;
      if (str1 == null) {
        break label171;
      }
      localObject2 = this.searchAdapterHelper.getLastFoundUsername();
      localObject1 = localObject2;
      if (((String)localObject2).startsWith("@")) {
        localObject1 = ((String)localObject2).substring(1);
      }
      try
      {
        localObject2 = new SpannableStringBuilder();
        ((SpannableStringBuilder)localObject2).append("@");
        ((SpannableStringBuilder)localObject2).append(str1);
        int j = str1.toLowerCase().indexOf((String)localObject1);
        int k;
        if (j != -1)
        {
          k = ((String)localObject1).length();
          if (j != 0) {
            break label417;
          }
          k += 1;
        }
        for (;;)
        {
          ((SpannableStringBuilder)localObject2).setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), j, j + k, 33);
          localObject1 = localObject2;
          localObject2 = localCharSequence;
          break;
          j += 1;
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
        localObject2 = localCharSequence;
        str2 = str1;
      }
    }
    label450:
    paramViewHolder = (ProfileSearchCell)paramViewHolder.itemView;
    paramViewHolder.setData(localTLObject, null, (CharSequence)localObject2, str2, false, false);
    if ((paramInt != getItemCount() - 1) && (paramInt != this.searchResult.size() - 1)) {}
    for (bool = true;; bool = false)
    {
      paramViewHolder.useSeparator = bool;
      return;
    }
  }
  
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    switch (paramInt)
    {
    default: 
      paramViewGroup = new GraySectionCell(this.mContext);
      ((GraySectionCell)paramViewGroup).setText(LocaleController.getString("GlobalSearch", 2131493628));
    }
    for (;;)
    {
      return new RecyclerListView.Holder(paramViewGroup);
      if (this.useUserCell)
      {
        UserCell localUserCell = new UserCell(this.mContext, 1, 1, false);
        paramViewGroup = localUserCell;
        if (this.checkedMap != null)
        {
          ((UserCell)localUserCell).setChecked(false, false);
          paramViewGroup = localUserCell;
        }
      }
      else
      {
        paramViewGroup = new ProfileSearchCell(this.mContext);
      }
    }
  }
  
  public void searchDialogs(final String paramString)
  {
    try
    {
      if (this.searchTimer != null) {
        this.searchTimer.cancel();
      }
      if (paramString == null)
      {
        this.searchResult.clear();
        this.searchResultNames.clear();
        if (this.allowUsernameSearch) {
          this.searchAdapterHelper.queryServerSearch(null, true, this.allowChats, this.allowBots, true, this.channelId, false);
        }
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
      this.searchTimer = new Timer();
      this.searchTimer.schedule(new TimerTask()
      {
        public void run()
        {
          try
          {
            SearchAdapter.this.searchTimer.cancel();
            SearchAdapter.access$002(SearchAdapter.this, null);
            SearchAdapter.this.processSearch(paramString);
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
    }
  }
  
  public void setCheckedMap(SparseArray<?> paramSparseArray)
  {
    this.checkedMap = paramSparseArray;
  }
  
  public void setUseUserCell(boolean paramBoolean)
  {
    this.useUserCell = paramBoolean;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/SearchAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */