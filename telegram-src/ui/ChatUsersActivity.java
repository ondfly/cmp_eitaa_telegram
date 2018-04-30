package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.ManageChatUserCell.ManageChatUserCellDelegate;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class ChatUsersActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int search_button = 0;
  private int chatId = this.arguments.getInt("chat_id");
  private TLRPC.Chat currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
  private EmptyTextProgressView emptyView;
  private boolean firstLoaded;
  private TLRPC.ChatFull info;
  private RecyclerListView listView;
  private ListAdapter listViewAdapter;
  private boolean loadingUsers;
  private ArrayList<TLRPC.ChatParticipant> participants = new ArrayList();
  private int participantsEndRow;
  private int participantsInfoRow;
  private int participantsStartRow;
  private int rowCount;
  private ActionBarMenuItem searchItem;
  private SearchAdapter searchListViewAdapter;
  private boolean searchWas;
  private boolean searching;
  
  public ChatUsersActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  private boolean createMenuForParticipant(final TLRPC.ChatParticipant paramChatParticipant, boolean paramBoolean)
  {
    if (paramChatParticipant == null) {}
    for (;;)
    {
      return false;
      int k = UserConfig.getInstance(this.currentAccount).getClientUserId();
      if (paramChatParticipant.user_id != k)
      {
        int j = 0;
        int i;
        if (this.currentChat.creator) {
          i = 1;
        }
        while (i != 0)
        {
          if (!paramBoolean) {
            break label99;
          }
          return true;
          i = j;
          if ((paramChatParticipant instanceof TLRPC.TL_chatParticipant)) {
            if ((!this.currentChat.admin) || (!this.currentChat.admins_enabled))
            {
              i = j;
              if (paramChatParticipant.inviter_id != k) {}
            }
            else
            {
              i = 1;
            }
          }
        }
      }
    }
    label99:
    ArrayList localArrayList1 = new ArrayList();
    final ArrayList localArrayList2 = new ArrayList();
    localArrayList1.add(LocaleController.getString("KickFromGroup", 2131493719));
    localArrayList2.add(Integer.valueOf(0));
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setItems((CharSequence[])localArrayList1.toArray(new CharSequence[localArrayList2.size()]), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        if (((Integer)localArrayList2.get(paramAnonymousInt)).intValue() == 0) {
          MessagesController.getInstance(ChatUsersActivity.this.currentAccount).deleteUserFromChat(ChatUsersActivity.this.chatId, MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(paramChatParticipant.user_id)), ChatUsersActivity.this.info);
        }
      }
    });
    showDialog(localBuilder.create());
    return true;
  }
  
  private void fetchUsers()
  {
    if (this.info == null) {
      this.loadingUsers = true;
    }
    do
    {
      return;
      this.loadingUsers = false;
      this.participants = new ArrayList(this.info.participants.participants);
    } while (this.listViewAdapter == null);
    this.listViewAdapter.notifyDataSetChanged();
  }
  
  private void updateRows()
  {
    this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
    if (this.currentChat == null) {}
    for (;;)
    {
      return;
      this.participantsStartRow = -1;
      this.participantsEndRow = -1;
      this.participantsInfoRow = -1;
      this.rowCount = 0;
      if (!this.participants.isEmpty())
      {
        this.participantsStartRow = this.rowCount;
        this.rowCount += this.participants.size();
      }
      for (this.participantsEndRow = this.rowCount; this.rowCount != 0; this.participantsEndRow = -1)
      {
        int i = this.rowCount;
        this.rowCount = (i + 1);
        this.participantsInfoRow = i;
        return;
        this.participantsStartRow = -1;
      }
    }
  }
  
  public View createView(Context paramContext)
  {
    int i = 1;
    this.searching = false;
    this.searchWas = false;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("GroupMembers", 2131493632));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          ChatUsersActivity.this.finishFragment();
        }
      }
    });
    this.searchListViewAdapter = new SearchAdapter(paramContext);
    this.searchItem = this.actionBar.createMenu().addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
    {
      public void onSearchCollapse()
      {
        ChatUsersActivity.this.searchListViewAdapter.searchDialogs(null);
        ChatUsersActivity.access$002(ChatUsersActivity.this, false);
        ChatUsersActivity.access$302(ChatUsersActivity.this, false);
        ChatUsersActivity.this.listView.setAdapter(ChatUsersActivity.this.listViewAdapter);
        ChatUsersActivity.this.listViewAdapter.notifyDataSetChanged();
        ChatUsersActivity.this.listView.setFastScrollVisible(true);
        ChatUsersActivity.this.listView.setVerticalScrollBarEnabled(false);
        ChatUsersActivity.this.emptyView.setShowAtCenter(false);
      }
      
      public void onSearchExpand()
      {
        ChatUsersActivity.access$002(ChatUsersActivity.this, true);
        ChatUsersActivity.this.emptyView.setShowAtCenter(true);
      }
      
      public void onTextChanged(EditText paramAnonymousEditText)
      {
        if (ChatUsersActivity.this.searchListViewAdapter == null) {
          return;
        }
        paramAnonymousEditText = paramAnonymousEditText.getText().toString();
        if (paramAnonymousEditText.length() != 0)
        {
          ChatUsersActivity.access$302(ChatUsersActivity.this, true);
          if (ChatUsersActivity.this.listView != null)
          {
            ChatUsersActivity.this.listView.setAdapter(ChatUsersActivity.this.searchListViewAdapter);
            ChatUsersActivity.this.searchListViewAdapter.notifyDataSetChanged();
            ChatUsersActivity.this.listView.setFastScrollVisible(false);
            ChatUsersActivity.this.listView.setVerticalScrollBarEnabled(true);
          }
        }
        ChatUsersActivity.this.searchListViewAdapter.searchDialogs(paramAnonymousEditText);
      }
    });
    this.searchItem.getSearchField().setHint(LocaleController.getString("Search", 2131494298));
    this.fragmentView = new FrameLayout(paramContext);
    this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    this.emptyView = new EmptyTextProgressView(paramContext);
    this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
    localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setEmptyView(this.emptyView);
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
    RecyclerListView localRecyclerListView = this.listView;
    paramContext = new ListAdapter(paramContext);
    this.listViewAdapter = paramContext;
    localRecyclerListView.setAdapter(paramContext);
    paramContext = this.listView;
    if (LocaleController.isRTL)
    {
      paramContext.setVerticalScrollbarPosition(i);
      localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          int i = 0;
          if (ChatUsersActivity.this.listView.getAdapter() == ChatUsersActivity.this.listViewAdapter)
          {
            paramAnonymousView = ChatUsersActivity.this.listViewAdapter.getItem(paramAnonymousInt);
            paramAnonymousInt = i;
            if (paramAnonymousView != null) {
              paramAnonymousInt = paramAnonymousView.user_id;
            }
            if (paramAnonymousInt != 0)
            {
              paramAnonymousView = new Bundle();
              paramAnonymousView.putInt("user_id", paramAnonymousInt);
              ChatUsersActivity.this.presentFragment(new ProfileActivity(paramAnonymousView));
            }
            return;
          }
          paramAnonymousView = ChatUsersActivity.this.searchListViewAdapter.getItem(paramAnonymousInt);
          if ((paramAnonymousView instanceof TLRPC.ChatParticipant)) {}
          for (paramAnonymousView = (TLRPC.ChatParticipant)paramAnonymousView;; paramAnonymousView = null)
          {
            paramAnonymousInt = i;
            if (paramAnonymousView == null) {
              break;
            }
            paramAnonymousInt = paramAnonymousView.user_id;
            break;
          }
        }
      });
      this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          boolean bool2 = false;
          boolean bool1 = bool2;
          if (ChatUsersActivity.this.getParentActivity() != null)
          {
            bool1 = bool2;
            if (ChatUsersActivity.this.listView.getAdapter() == ChatUsersActivity.this.listViewAdapter)
            {
              bool1 = bool2;
              if (ChatUsersActivity.this.createMenuForParticipant(ChatUsersActivity.this.listViewAdapter.getItem(paramAnonymousInt), false)) {
                bool1 = true;
              }
            }
          }
          return bool1;
        }
      });
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt == 1) && (ChatUsersActivity.this.searching) && (ChatUsersActivity.this.searchWas)) {
            AndroidUtilities.hideKeyboard(ChatUsersActivity.this.getParentActivity().getCurrentFocus());
          }
        }
        
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          super.onScrolled(paramAnonymousRecyclerView, paramAnonymousInt1, paramAnonymousInt2);
        }
      });
      if (!this.loadingUsers) {
        break label373;
      }
      this.emptyView.showProgress();
    }
    for (;;)
    {
      updateRows();
      return this.fragmentView;
      i = 2;
      break;
      label373:
      this.emptyView.showTextView();
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.chatInfoDidLoaded)
    {
      TLRPC.ChatFull localChatFull = (TLRPC.ChatFull)paramVarArgs[0];
      boolean bool = ((Boolean)paramVarArgs[2]).booleanValue();
      if ((localChatFull.id == this.chatId) && (!bool))
      {
        this.info = localChatFull;
        fetchUsers();
        updateRows();
      }
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local7 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (ChatUsersActivity.this.listView != null)
        {
          int j = ChatUsersActivity.this.listView.getChildCount();
          int i = 0;
          while (i < j)
          {
            View localView = ChatUsersActivity.this.listView.getChildAt(i);
            if ((localView instanceof ManageChatUserCell)) {
              ((ManageChatUserCell)localView).update(0);
            }
            i += 1;
          }
        }
      }
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { ManageChatUserCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    Object localObject1 = this.listView;
    Object localObject2 = Theme.dividerPaint;
    localObject1 = new ThemeDescription((View)localObject1, 0, new Class[] { View.class }, (Paint)localObject2, null, null, "divider");
    localObject2 = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.listView, 0, new Class[] { ManageChatUserCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, 0, new Class[] { ManageChatUserCell.class }, new String[] { "statusColor" }, null, null, local7, "windowBackgroundWhiteGrayText");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.listView, 0, new Class[] { ManageChatUserCell.class }, new String[] { "statusOnlineColor" }, null, null, local7, "windowBackgroundWhiteBlueText");
    RecyclerListView localRecyclerListView = this.listView;
    Drawable localDrawable1 = Theme.avatar_photoDrawable;
    Drawable localDrawable2 = Theme.avatar_broadcastDrawable;
    Drawable localDrawable3 = Theme.avatar_savedDrawable;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localObject1, localObject2, localThemeDescription9, localThemeDescription10, localThemeDescription11, localThemeDescription12, new ThemeDescription(localRecyclerListView, 0, new Class[] { ManageChatUserCell.class }, null, new Drawable[] { localDrawable1, localDrawable2, localDrawable3 }, null, "avatar_text"), new ThemeDescription(null, 0, null, null, null, local7, "avatar_backgroundRed"), new ThemeDescription(null, 0, null, null, null, local7, "avatar_backgroundOrange"), new ThemeDescription(null, 0, null, null, null, local7, "avatar_backgroundViolet"), new ThemeDescription(null, 0, null, null, null, local7, "avatar_backgroundGreen"), new ThemeDescription(null, 0, null, null, null, local7, "avatar_backgroundCyan"), new ThemeDescription(null, 0, null, null, null, local7, "avatar_backgroundBlue"), new ThemeDescription(null, 0, null, null, null, local7, "avatar_backgroundPink") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
    fetchUsers();
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoaded);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listViewAdapter != null) {
      this.listViewAdapter.notifyDataSetChanged();
    }
  }
  
  protected void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean1) && (!paramBoolean2)) {
      this.searchItem.openSearch(true);
    }
  }
  
  public void setInfo(TLRPC.ChatFull paramChatFull)
  {
    this.info = paramChatFull;
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public TLRPC.ChatParticipant getItem(int paramInt)
    {
      if ((ChatUsersActivity.this.participantsStartRow != -1) && (paramInt >= ChatUsersActivity.this.participantsStartRow) && (paramInt < ChatUsersActivity.this.participantsEndRow)) {
        return (TLRPC.ChatParticipant)ChatUsersActivity.this.participants.get(paramInt - ChatUsersActivity.this.participantsStartRow);
      }
      return null;
    }
    
    public int getItemCount()
    {
      if (ChatUsersActivity.this.loadingUsers) {
        return 0;
      }
      return ChatUsersActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt >= ChatUsersActivity.this.participantsStartRow) && (paramInt < ChatUsersActivity.this.participantsEndRow)) {}
      while (paramInt != ChatUsersActivity.this.participantsInfoRow) {
        return 0;
      }
      return 1;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getItemViewType();
      return (i == 0) || (i == 2) || (i == 6);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      do
      {
        Object localObject;
        do
        {
          return;
          paramViewHolder = (ManageChatUserCell)paramViewHolder.itemView;
          paramViewHolder.setTag(Integer.valueOf(paramInt));
          localObject = getItem(paramInt);
          localObject = MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChatParticipant)localObject).user_id));
        } while (localObject == null);
        paramViewHolder.setData((TLRPC.User)localObject, null, null);
        return;
        paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
      } while (paramInt != ChatUsersActivity.this.participantsInfoRow);
      paramViewHolder.setText("");
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
        paramViewGroup.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new ManageChatUserCell(this.mContext, 1, true);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        ((ManageChatUserCell)paramViewGroup).setDelegate(new ManageChatUserCell.ManageChatUserCellDelegate()
        {
          public boolean onOptionsButtonCheck(ManageChatUserCell paramAnonymousManageChatUserCell, boolean paramAnonymousBoolean)
          {
            paramAnonymousManageChatUserCell = ChatUsersActivity.this.listViewAdapter.getItem(((Integer)paramAnonymousManageChatUserCell.getTag()).intValue());
            ChatUsersActivity localChatUsersActivity = ChatUsersActivity.this;
            if (!paramAnonymousBoolean) {}
            for (paramAnonymousBoolean = true;; paramAnonymousBoolean = false) {
              return localChatUsersActivity.createMenuForParticipant(paramAnonymousManageChatUserCell, paramAnonymousBoolean);
            }
          }
        });
      }
    }
    
    public void onViewRecycled(RecyclerView.ViewHolder paramViewHolder)
    {
      if ((paramViewHolder.itemView instanceof ManageChatUserCell)) {
        ((ManageChatUserCell)paramViewHolder.itemView).recycle();
      }
    }
  }
  
  private class SearchAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    private ArrayList<TLRPC.ChatParticipant> searchResult = new ArrayList();
    private ArrayList<CharSequence> searchResultNames = new ArrayList();
    private Timer searchTimer;
    
    public SearchAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    private void processSearch(final String paramString)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          final ArrayList localArrayList = new ArrayList();
          localArrayList.addAll(ChatUsersActivity.this.participants);
          Utilities.searchQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              Object localObject = ChatUsersActivity.SearchAdapter.2.this.val$query.trim().toLowerCase();
              if (((String)localObject).length() == 0)
              {
                ChatUsersActivity.SearchAdapter.this.updateSearchResults(new ArrayList(), new ArrayList());
                return;
              }
              String str2 = LocaleController.getInstance().getTranslitString((String)localObject);
              String str1;
              if (!((String)localObject).equals(str2))
              {
                str1 = str2;
                if (str2.length() != 0) {}
              }
              else
              {
                str1 = null;
              }
              int i;
              String[] arrayOfString;
              ArrayList localArrayList;
              int j;
              label135:
              TLRPC.ChatParticipant localChatParticipant;
              TLRPC.User localUser;
              String str3;
              int m;
              int n;
              int k;
              if (str1 != null)
              {
                i = 1;
                arrayOfString = new String[i + 1];
                arrayOfString[0] = localObject;
                if (str1 != null) {
                  arrayOfString[1] = str1;
                }
                localObject = new ArrayList();
                localArrayList = new ArrayList();
                j = 0;
                if (j < localArrayList.size())
                {
                  localChatParticipant = (TLRPC.ChatParticipant)localArrayList.get(j);
                  localUser = MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(localChatParticipant.user_id));
                  str3 = ContactsController.formatName(localUser.first_name, localUser.last_name).toLowerCase();
                  str2 = LocaleController.getInstance().getTranslitString(str3);
                  str1 = str2;
                  if (str3.equals(str2)) {
                    str1 = null;
                  }
                  m = 0;
                  n = arrayOfString.length;
                  k = 0;
                }
              }
              else
              {
                for (;;)
                {
                  if (k < n)
                  {
                    str2 = arrayOfString[k];
                    if ((!str3.startsWith(str2)) && (!str3.contains(" " + str2)) && ((str1 == null) || ((!str1.startsWith(str2)) && (!str1.contains(" " + str2))))) {
                      break label388;
                    }
                    i = 1;
                    label338:
                    if (i == 0) {
                      break label476;
                    }
                    if (i != 1) {
                      break label420;
                    }
                    localArrayList.add(AndroidUtilities.generateSearchName(localUser.first_name, localUser.last_name, str2));
                  }
                  for (;;)
                  {
                    ((ArrayList)localObject).add(localChatParticipant);
                    j += 1;
                    break label135;
                    i = 0;
                    break;
                    label388:
                    i = m;
                    if (localUser.username == null) {
                      break label338;
                    }
                    i = m;
                    if (!localUser.username.startsWith(str2)) {
                      break label338;
                    }
                    i = 2;
                    break label338;
                    label420:
                    localArrayList.add(AndroidUtilities.generateSearchName("@" + localUser.username, null, "@" + str2));
                  }
                  label476:
                  k += 1;
                  m = i;
                }
              }
              ChatUsersActivity.SearchAdapter.this.updateSearchResults((ArrayList)localObject, localArrayList);
            }
          });
        }
      });
    }
    
    private void updateSearchResults(final ArrayList<TLRPC.ChatParticipant> paramArrayList, final ArrayList<CharSequence> paramArrayList1)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          ChatUsersActivity.SearchAdapter.access$1602(ChatUsersActivity.SearchAdapter.this, paramArrayList);
          ChatUsersActivity.SearchAdapter.access$1702(ChatUsersActivity.SearchAdapter.this, paramArrayList1);
          ChatUsersActivity.SearchAdapter.this.notifyDataSetChanged();
        }
      });
    }
    
    public TLObject getItem(int paramInt)
    {
      return (TLObject)this.searchResult.get(paramInt);
    }
    
    public int getItemCount()
    {
      return this.searchResult.size();
    }
    
    public int getItemViewType(int paramInt)
    {
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      Object localObject1 = getItem(paramInt);
      if ((localObject1 instanceof TLRPC.User)) {}
      for (localObject1 = (TLRPC.User)localObject1;; localObject1 = MessagesController.getInstance(ChatUsersActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChatParticipant)localObject1).user_id)))
      {
        String str = ((TLRPC.User)localObject1).username;
        CharSequence localCharSequence2 = (CharSequence)this.searchResultNames.get(paramInt);
        Object localObject3 = null;
        CharSequence localCharSequence1 = localCharSequence2;
        Object localObject2 = localObject3;
        if (localCharSequence2 != null)
        {
          localCharSequence1 = localCharSequence2;
          localObject2 = localObject3;
          if (str != null)
          {
            localCharSequence1 = localCharSequence2;
            localObject2 = localObject3;
            if (str.length() > 0)
            {
              localCharSequence1 = localCharSequence2;
              localObject2 = localObject3;
              if (localCharSequence2.toString().startsWith("@" + str))
              {
                localObject2 = localCharSequence2;
                localCharSequence1 = null;
              }
            }
          }
        }
        paramViewHolder = (ManageChatUserCell)paramViewHolder.itemView;
        paramViewHolder.setTag(Integer.valueOf(paramInt));
        paramViewHolder.setData((TLRPC.User)localObject1, localCharSequence1, (CharSequence)localObject2);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = new ManageChatUserCell(this.mContext, 2, true);
      paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      ((ManageChatUserCell)paramViewGroup).setDelegate(new ManageChatUserCell.ManageChatUserCellDelegate()
      {
        public boolean onOptionsButtonCheck(ManageChatUserCell paramAnonymousManageChatUserCell, boolean paramAnonymousBoolean)
        {
          boolean bool = false;
          ChatUsersActivity localChatUsersActivity;
          if ((ChatUsersActivity.SearchAdapter.this.getItem(((Integer)paramAnonymousManageChatUserCell.getTag()).intValue()) instanceof TLRPC.ChatParticipant))
          {
            paramAnonymousManageChatUserCell = (TLRPC.ChatParticipant)ChatUsersActivity.SearchAdapter.this.getItem(((Integer)paramAnonymousManageChatUserCell.getTag()).intValue());
            localChatUsersActivity = ChatUsersActivity.this;
            if (paramAnonymousBoolean) {
              break label71;
            }
          }
          label71:
          for (paramAnonymousBoolean = true;; paramAnonymousBoolean = false)
          {
            bool = localChatUsersActivity.createMenuForParticipant(paramAnonymousManageChatUserCell, paramAnonymousBoolean);
            return bool;
          }
        }
      });
      return new RecyclerListView.Holder(paramViewGroup);
    }
    
    public void onViewRecycled(RecyclerView.ViewHolder paramViewHolder)
    {
      if ((paramViewHolder.itemView instanceof ManageChatUserCell)) {
        ((ManageChatUserCell)paramViewHolder.itemView).recycle();
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
              ChatUsersActivity.SearchAdapter.this.searchTimer.cancel();
              ChatUsersActivity.SearchAdapter.access$1102(ChatUsersActivity.SearchAdapter.this, null);
              ChatUsersActivity.SearchAdapter.this.processSearch(paramString);
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
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ChatUsersActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */