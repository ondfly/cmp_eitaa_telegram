package org.telegram.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_chatParticipantCreator;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class SetAdminsActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private int allAdminsInfoRow;
  private int allAdminsRow;
  private TLRPC.Chat chat;
  private int chat_id;
  private EmptyTextProgressView emptyView;
  private TLRPC.ChatFull info;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private ArrayList<TLRPC.ChatParticipant> participants = new ArrayList();
  private int rowCount;
  private SearchAdapter searchAdapter;
  private ActionBarMenuItem searchItem;
  private boolean searchWas;
  private boolean searching;
  private int usersEndRow;
  private int usersStartRow;
  
  public SetAdminsActivity(Bundle paramBundle)
  {
    super(paramBundle);
    this.chat_id = paramBundle.getInt("chat_id");
  }
  
  private int getChatAdminParticipantType(TLRPC.ChatParticipant paramChatParticipant)
  {
    if ((paramChatParticipant instanceof TLRPC.TL_chatParticipantCreator)) {
      return 0;
    }
    if ((paramChatParticipant instanceof TLRPC.TL_chatParticipantAdmin)) {
      return 1;
    }
    return 2;
  }
  
  private void updateChatParticipants()
  {
    if (this.info == null) {}
    while (this.participants.size() == this.info.participants.participants.size()) {
      return;
    }
    this.participants.clear();
    this.participants.addAll(this.info.participants.participants);
    try
    {
      Collections.sort(this.participants, new Comparator()
      {
        public int compare(TLRPC.ChatParticipant paramAnonymousChatParticipant1, TLRPC.ChatParticipant paramAnonymousChatParticipant2)
        {
          int i = SetAdminsActivity.this.getChatAdminParticipantType(paramAnonymousChatParticipant1);
          int j = SetAdminsActivity.this.getChatAdminParticipantType(paramAnonymousChatParticipant2);
          if (i > j) {}
          do
          {
            do
            {
              do
              {
                return 1;
                if (i < j) {
                  return -1;
                }
                if (i != j) {
                  break label230;
                }
                paramAnonymousChatParticipant2 = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymousChatParticipant2.user_id));
                paramAnonymousChatParticipant1 = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymousChatParticipant1.user_id));
                j = 0;
                int k = 0;
                i = j;
                if (paramAnonymousChatParticipant2 != null)
                {
                  i = j;
                  if (paramAnonymousChatParticipant2.status != null) {
                    i = paramAnonymousChatParticipant2.status.expires;
                  }
                }
                j = k;
                if (paramAnonymousChatParticipant1 != null)
                {
                  j = k;
                  if (paramAnonymousChatParticipant1.status != null) {
                    j = paramAnonymousChatParticipant1.status.expires;
                  }
                }
                if ((i <= 0) || (j <= 0)) {
                  break;
                }
              } while (i > j);
              if (i < j) {
                return -1;
              }
              return 0;
              if ((i >= 0) || (j >= 0)) {
                break;
              }
            } while (i > j);
            if (i < j) {
              return -1;
            }
            return 0;
            if (((i < 0) && (j > 0)) || ((i == 0) && (j != 0))) {
              return -1;
            }
          } while (((j < 0) && (i > 0)) || ((j == 0) && (i != 0)));
          label230:
          return 0;
        }
      });
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  private void updateRowsIds()
  {
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.allAdminsRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.allAdminsInfoRow = i;
    if (this.info != null)
    {
      this.usersStartRow = this.rowCount;
      this.rowCount += this.participants.size();
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.usersEndRow = i;
      if ((this.searchItem != null) && (!this.searchWas)) {
        this.searchItem.setVisibility(0);
      }
    }
    for (;;)
    {
      if (this.listAdapter != null) {
        this.listAdapter.notifyDataSetChanged();
      }
      return;
      this.usersStartRow = -1;
      this.usersEndRow = -1;
      if (this.searchItem != null) {
        this.searchItem.setVisibility(8);
      }
    }
  }
  
  public View createView(Context paramContext)
  {
    this.searching = false;
    this.searchWas = false;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("SetAdminsTitle", 2131494375));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          SetAdminsActivity.this.finishFragment();
        }
      }
    });
    this.searchItem = this.actionBar.createMenu().addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
    {
      public void onSearchCollapse()
      {
        SetAdminsActivity.access$002(SetAdminsActivity.this, false);
        SetAdminsActivity.access$302(SetAdminsActivity.this, false);
        if (SetAdminsActivity.this.listView != null)
        {
          SetAdminsActivity.this.listView.setEmptyView(null);
          SetAdminsActivity.this.emptyView.setVisibility(8);
          if (SetAdminsActivity.this.listView.getAdapter() != SetAdminsActivity.this.listAdapter) {
            SetAdminsActivity.this.listView.setAdapter(SetAdminsActivity.this.listAdapter);
          }
        }
        if (SetAdminsActivity.this.searchAdapter != null) {
          SetAdminsActivity.this.searchAdapter.search(null);
        }
      }
      
      public void onSearchExpand()
      {
        SetAdminsActivity.access$002(SetAdminsActivity.this, true);
        SetAdminsActivity.this.listView.setEmptyView(SetAdminsActivity.this.emptyView);
      }
      
      public void onTextChanged(EditText paramAnonymousEditText)
      {
        paramAnonymousEditText = paramAnonymousEditText.getText().toString();
        if (paramAnonymousEditText.length() != 0)
        {
          SetAdminsActivity.access$302(SetAdminsActivity.this, true);
          if ((SetAdminsActivity.this.searchAdapter != null) && (SetAdminsActivity.this.listView.getAdapter() != SetAdminsActivity.this.searchAdapter))
          {
            SetAdminsActivity.this.listView.setAdapter(SetAdminsActivity.this.searchAdapter);
            SetAdminsActivity.this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
          }
          if ((SetAdminsActivity.this.emptyView != null) && (SetAdminsActivity.this.listView.getEmptyView() != SetAdminsActivity.this.emptyView))
          {
            SetAdminsActivity.this.emptyView.showTextView();
            SetAdminsActivity.this.listView.setEmptyView(SetAdminsActivity.this.emptyView);
          }
        }
        if (SetAdminsActivity.this.searchAdapter != null) {
          SetAdminsActivity.this.searchAdapter.search(paramAnonymousEditText);
        }
      }
    });
    this.searchItem.getSearchField().setHint(LocaleController.getString("Search", 2131494298));
    this.listAdapter = new ListAdapter(paramContext);
    this.searchAdapter = new SearchAdapter(paramContext);
    this.fragmentView = new FrameLayout(paramContext);
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
    this.listView.setVerticalScrollBarEnabled(false);
    localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView.setAdapter(this.listAdapter);
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        boolean bool3 = true;
        boolean bool2 = true;
        int i;
        int j;
        if ((SetAdminsActivity.this.listView.getAdapter() == SetAdminsActivity.this.searchAdapter) || ((paramAnonymousInt >= SetAdminsActivity.this.usersStartRow) && (paramAnonymousInt < SetAdminsActivity.this.usersEndRow)))
        {
          UserCell localUserCell = (UserCell)paramAnonymousView;
          SetAdminsActivity.access$902(SetAdminsActivity.this, MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getChat(Integer.valueOf(SetAdminsActivity.this.chat_id)));
          int k = -1;
          if (SetAdminsActivity.this.listView.getAdapter() == SetAdminsActivity.this.searchAdapter)
          {
            localObject = SetAdminsActivity.this.searchAdapter.getItem(paramAnonymousInt);
            i = 0;
            j = k;
            paramAnonymousView = (View)localObject;
            if (i < SetAdminsActivity.this.participants.size())
            {
              if (((TLRPC.ChatParticipant)SetAdminsActivity.this.participants.get(i)).user_id == ((TLRPC.ChatParticipant)localObject).user_id)
              {
                paramAnonymousView = (View)localObject;
                j = i;
              }
            }
            else {
              label175:
              if ((j != -1) && (!(paramAnonymousView instanceof TLRPC.TL_chatParticipantCreator)))
              {
                if (!(paramAnonymousView instanceof TLRPC.TL_chatParticipant)) {
                  break label476;
                }
                localObject = new TLRPC.TL_chatParticipantAdmin();
                ((TLRPC.ChatParticipant)localObject).user_id = paramAnonymousView.user_id;
                ((TLRPC.ChatParticipant)localObject).date = paramAnonymousView.date;
                ((TLRPC.ChatParticipant)localObject).inviter_id = paramAnonymousView.inviter_id;
                label231:
                SetAdminsActivity.this.participants.set(j, localObject);
                i = SetAdminsActivity.this.info.participants.participants.indexOf(paramAnonymousView);
                if (i != -1) {
                  SetAdminsActivity.this.info.participants.participants.set(i, localObject);
                }
                if (SetAdminsActivity.this.listView.getAdapter() == SetAdminsActivity.this.searchAdapter) {
                  SetAdminsActivity.SearchAdapter.access$1400(SetAdminsActivity.this.searchAdapter).set(paramAnonymousInt, localObject);
                }
                if (((localObject instanceof TLRPC.TL_chatParticipant)) && ((SetAdminsActivity.this.chat == null) || (SetAdminsActivity.this.chat.admins_enabled))) {
                  break label515;
                }
                bool1 = true;
                label360:
                localUserCell.setChecked(bool1, true);
                if ((SetAdminsActivity.this.chat != null) && (SetAdminsActivity.this.chat.admins_enabled))
                {
                  paramAnonymousView = MessagesController.getInstance(SetAdminsActivity.this.currentAccount);
                  paramAnonymousInt = SetAdminsActivity.this.chat_id;
                  i = ((TLRPC.ChatParticipant)localObject).user_id;
                  if ((localObject instanceof TLRPC.TL_chatParticipant)) {
                    break label521;
                  }
                  bool1 = bool2;
                  label428:
                  paramAnonymousView.toggleUserAdmin(paramAnonymousInt, i, bool1);
                }
              }
            }
          }
        }
        label476:
        label515:
        label521:
        do
        {
          do
          {
            return;
            i += 1;
            break;
            paramAnonymousView = SetAdminsActivity.this.participants;
            j = paramAnonymousInt - SetAdminsActivity.this.usersStartRow;
            paramAnonymousView = (TLRPC.ChatParticipant)paramAnonymousView.get(j);
            break label175;
            localObject = new TLRPC.TL_chatParticipant();
            ((TLRPC.ChatParticipant)localObject).user_id = paramAnonymousView.user_id;
            ((TLRPC.ChatParticipant)localObject).date = paramAnonymousView.date;
            ((TLRPC.ChatParticipant)localObject).inviter_id = paramAnonymousView.inviter_id;
            break label231;
            bool1 = false;
            break label360;
            bool1 = false;
            break label428;
          } while (paramAnonymousInt != SetAdminsActivity.this.allAdminsRow);
          SetAdminsActivity.access$902(SetAdminsActivity.this, MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getChat(Integer.valueOf(SetAdminsActivity.this.chat_id)));
        } while (SetAdminsActivity.this.chat == null);
        Object localObject = SetAdminsActivity.this.chat;
        if (!SetAdminsActivity.this.chat.admins_enabled)
        {
          bool1 = true;
          ((TLRPC.Chat)localObject).admins_enabled = bool1;
          paramAnonymousView = (TextCheckCell)paramAnonymousView;
          if (SetAdminsActivity.this.chat.admins_enabled) {
            break label676;
          }
        }
        label676:
        for (boolean bool1 = bool3;; bool1 = false)
        {
          paramAnonymousView.setChecked(bool1);
          MessagesController.getInstance(SetAdminsActivity.this.currentAccount).toggleAdminMode(SetAdminsActivity.this.chat_id, SetAdminsActivity.this.chat.admins_enabled);
          return;
          bool1 = false;
          break;
        }
      }
    });
    this.emptyView = new EmptyTextProgressView(paramContext);
    this.emptyView.setVisibility(8);
    this.emptyView.setShowAtCenter(true);
    this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
    localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
    this.emptyView.showTextView();
    updateRowsIds();
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.chatInfoDidLoaded)
    {
      paramVarArgs = (TLRPC.ChatFull)paramVarArgs[0];
      if (paramVarArgs.id == this.chat_id)
      {
        this.info = paramVarArgs;
        updateChatParticipants();
        updateRowsIds();
      }
    }
    for (;;)
    {
      return;
      if (paramInt1 == NotificationCenter.updateInterfaces)
      {
        paramInt2 = ((Integer)paramVarArgs[0]).intValue();
        if ((((paramInt2 & 0x2) == 0) && ((paramInt2 & 0x1) == 0) && ((paramInt2 & 0x4) == 0)) || (this.listView == null)) {
          break;
        }
        int i = this.listView.getChildCount();
        paramInt1 = 0;
        while (paramInt1 < i)
        {
          paramVarArgs = this.listView.getChildAt(paramInt1);
          if ((paramVarArgs instanceof UserCell)) {
            ((UserCell)paramVarArgs).update(paramInt2);
          }
          paramInt1 += 1;
        }
      }
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local5 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (SetAdminsActivity.this.listView != null)
        {
          int j = SetAdminsActivity.this.listView.getChildCount();
          int i = 0;
          while (i < j)
          {
            View localView = SetAdminsActivity.this.listView.getChildAt(i);
            if ((localView instanceof UserCell)) {
              ((UserCell)localView).update(0);
            }
            i += 1;
          }
        }
      }
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextCheckCell.class, UserCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, "actionBarDefaultSearch");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, "actionBarDefaultSearchPlaceholder");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    Object localObject1 = this.listView;
    Object localObject2 = Theme.dividerPaint;
    localObject1 = new ThemeDescription((View)localObject1, 0, new Class[] { View.class }, (Paint)localObject2, null, null, "divider");
    localObject2 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumb");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrack");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumbChecked");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrackChecked");
    ThemeDescription localThemeDescription16 = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow");
    ThemeDescription localThemeDescription17 = new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4");
    ThemeDescription localThemeDescription18 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, null, null, null, "checkboxSquareUnchecked");
    ThemeDescription localThemeDescription19 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, null, null, null, "checkboxSquareDisabled");
    ThemeDescription localThemeDescription20 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, null, null, null, "checkboxSquareBackground");
    ThemeDescription localThemeDescription21 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, null, null, null, "checkboxSquareCheck");
    ThemeDescription localThemeDescription22 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription23 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "statusColor" }, null, null, local5, "windowBackgroundWhiteGrayText");
    ThemeDescription localThemeDescription24 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "statusOnlineColor" }, null, null, local5, "windowBackgroundWhiteBlueText");
    RecyclerListView localRecyclerListView = this.listView;
    Drawable localDrawable1 = Theme.avatar_photoDrawable;
    Drawable localDrawable2 = Theme.avatar_broadcastDrawable;
    Drawable localDrawable3 = Theme.avatar_savedDrawable;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localObject1, localObject2, localThemeDescription11, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localThemeDescription16, localThemeDescription17, localThemeDescription18, localThemeDescription19, localThemeDescription20, localThemeDescription21, localThemeDescription22, localThemeDescription23, localThemeDescription24, new ThemeDescription(localRecyclerListView, 0, new Class[] { UserCell.class }, null, new Drawable[] { localDrawable1, localDrawable2, localDrawable3 }, null, "avatar_text"), new ThemeDescription(null, 0, null, null, null, local5, "avatar_backgroundRed"), new ThemeDescription(null, 0, null, null, null, local5, "avatar_backgroundOrange"), new ThemeDescription(null, 0, null, null, null, local5, "avatar_backgroundViolet"), new ThemeDescription(null, 0, null, null, null, local5, "avatar_backgroundGreen"), new ThemeDescription(null, 0, null, null, null, local5, "avatar_backgroundCyan"), new ThemeDescription(null, 0, null, null, null, local5, "avatar_backgroundBlue"), new ThemeDescription(null, 0, null, null, null, local5, "avatar_backgroundPink") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
  }
  
  public void setChatInfo(TLRPC.ChatFull paramChatFull)
  {
    this.info = paramChatFull;
    updateChatParticipants();
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public int getItemCount()
    {
      return SetAdminsActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if (paramInt == SetAdminsActivity.this.allAdminsRow) {
        return 0;
      }
      if ((paramInt == SetAdminsActivity.this.allAdminsInfoRow) || (paramInt == SetAdminsActivity.this.usersEndRow)) {
        return 1;
      }
      return 2;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getAdapterPosition();
      if (i == SetAdminsActivity.this.allAdminsRow) {}
      while ((i >= SetAdminsActivity.this.usersStartRow) && (i < SetAdminsActivity.this.usersEndRow) && (!((TLRPC.ChatParticipant)SetAdminsActivity.this.participants.get(i - SetAdminsActivity.this.usersStartRow) instanceof TLRPC.TL_chatParticipantCreator))) {
        return true;
      }
      return false;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool1 = true;
      boolean bool2 = false;
      switch (paramViewHolder.getItemViewType())
      {
      default: 
      case 0: 
      case 1: 
        do
        {
          return;
          paramViewHolder = (TextCheckCell)paramViewHolder.itemView;
          SetAdminsActivity.access$902(SetAdminsActivity.this, MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getChat(Integer.valueOf(SetAdminsActivity.this.chat_id)));
          localObject = LocaleController.getString("SetAdminsAll", 2131494372);
          if ((SetAdminsActivity.this.chat != null) && (!SetAdminsActivity.this.chat.admins_enabled)) {}
          for (;;)
          {
            paramViewHolder.setTextAndCheck((String)localObject, bool1, false);
            return;
            bool1 = false;
          }
          paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
          if (paramInt == SetAdminsActivity.this.allAdminsInfoRow)
          {
            if (SetAdminsActivity.this.chat.admins_enabled) {
              paramViewHolder.setText(LocaleController.getString("SetAdminsNotAllInfo", 2131494374));
            }
            while (SetAdminsActivity.this.usersStartRow != -1)
            {
              paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow"));
              return;
              paramViewHolder.setText(LocaleController.getString("SetAdminsAllInfo", 2131494373));
            }
            paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
            return;
          }
        } while (paramInt != SetAdminsActivity.this.usersEndRow);
        paramViewHolder.setText("");
        paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
        return;
      }
      paramViewHolder = (UserCell)paramViewHolder.itemView;
      Object localObject = (TLRPC.ChatParticipant)SetAdminsActivity.this.participants.get(paramInt - SetAdminsActivity.this.usersStartRow);
      paramViewHolder.setData(MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChatParticipant)localObject).user_id)), null, null, 0);
      SetAdminsActivity.access$902(SetAdminsActivity.this, MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getChat(Integer.valueOf(SetAdminsActivity.this.chat_id)));
      if ((!(localObject instanceof TLRPC.TL_chatParticipant)) || ((SetAdminsActivity.this.chat != null) && (!SetAdminsActivity.this.chat.admins_enabled))) {}
      for (bool1 = true;; bool1 = false)
      {
        paramViewHolder.setChecked(bool1, false);
        if ((SetAdminsActivity.this.chat != null) && (SetAdminsActivity.this.chat.admins_enabled))
        {
          bool1 = bool2;
          if (((TLRPC.ChatParticipant)localObject).user_id != UserConfig.getInstance(SetAdminsActivity.this.currentAccount).getClientUserId()) {}
        }
        else
        {
          bool1 = true;
        }
        paramViewHolder.setCheckDisabled(bool1);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new UserCell(this.mContext, 1, 2, false);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new TextCheckCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
      }
    }
  }
  
  public class SearchAdapter
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
          localArrayList.addAll(SetAdminsActivity.this.participants);
          Utilities.searchQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              Object localObject = SetAdminsActivity.SearchAdapter.2.this.val$query.trim().toLowerCase();
              if (((String)localObject).length() == 0)
              {
                SetAdminsActivity.SearchAdapter.this.updateSearchResults(new ArrayList(), new ArrayList());
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
                if (j >= localArrayList.size()) {
                  break label516;
                }
                localChatParticipant = (TLRPC.ChatParticipant)localArrayList.get(j);
                localUser = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getUser(Integer.valueOf(localChatParticipant.user_id));
                if (localUser.id != UserConfig.getInstance(SetAdminsActivity.this.currentAccount).getClientUserId()) {
                  break label227;
                }
              }
              label227:
              label377:
              label450:
              label506:
              label514:
              for (;;)
              {
                j += 1;
                break label135;
                i = 0;
                break;
                String str3 = ContactsController.formatName(localUser.first_name, localUser.last_name).toLowerCase();
                str2 = LocaleController.getInstance().getTranslitString(str3);
                str1 = str2;
                if (str3.equals(str2)) {
                  str1 = null;
                }
                int m = 0;
                int n = arrayOfString.length;
                int k = 0;
                for (;;)
                {
                  if (k >= n) {
                    break label514;
                  }
                  str2 = arrayOfString[k];
                  if ((str3.startsWith(str2)) || (str3.contains(" " + str2)) || ((str1 != null) && ((str1.startsWith(str2)) || (str1.contains(" " + str2)))))
                  {
                    i = 1;
                    if (i == 0) {
                      break label506;
                    }
                    if (i != 1) {
                      break label450;
                    }
                    localArrayList.add(AndroidUtilities.generateSearchName(localUser.first_name, localUser.last_name, str2));
                  }
                  for (;;)
                  {
                    ((ArrayList)localObject).add(localChatParticipant);
                    break;
                    i = m;
                    if (localUser.username == null) {
                      break label377;
                    }
                    i = m;
                    if (!localUser.username.startsWith(str2)) {
                      break label377;
                    }
                    i = 2;
                    break label377;
                    localArrayList.add(AndroidUtilities.generateSearchName("@" + localUser.username, null, "@" + str2));
                  }
                  k += 1;
                  m = i;
                }
              }
              label516:
              SetAdminsActivity.SearchAdapter.this.updateSearchResults((ArrayList)localObject, localArrayList);
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
          SetAdminsActivity.SearchAdapter.access$1402(SetAdminsActivity.SearchAdapter.this, paramArrayList);
          SetAdminsActivity.SearchAdapter.access$3302(SetAdminsActivity.SearchAdapter.this, paramArrayList1);
          SetAdminsActivity.SearchAdapter.this.notifyDataSetChanged();
        }
      });
    }
    
    public TLRPC.ChatParticipant getItem(int paramInt)
    {
      return (TLRPC.ChatParticipant)this.searchResult.get(paramInt);
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
      boolean bool2 = false;
      TLRPC.ChatParticipant localChatParticipant = getItem(paramInt);
      TLRPC.User localUser = MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getUser(Integer.valueOf(localChatParticipant.user_id));
      String str = localUser.username;
      Object localObject3 = null;
      Object localObject1 = null;
      Object localObject2 = localObject3;
      if (paramInt < this.searchResult.size())
      {
        CharSequence localCharSequence = (CharSequence)this.searchResultNames.get(paramInt);
        localObject1 = localCharSequence;
        localObject2 = localObject3;
        if (localCharSequence != null)
        {
          localObject1 = localCharSequence;
          localObject2 = localObject3;
          if (str != null)
          {
            localObject1 = localCharSequence;
            localObject2 = localObject3;
            if (str.length() > 0)
            {
              localObject1 = localCharSequence;
              localObject2 = localObject3;
              if (localCharSequence.toString().startsWith("@" + str))
              {
                localObject2 = localCharSequence;
                localObject1 = null;
              }
            }
          }
        }
      }
      paramViewHolder = (UserCell)paramViewHolder.itemView;
      paramViewHolder.setData(localUser, (CharSequence)localObject1, (CharSequence)localObject2, 0);
      SetAdminsActivity.access$902(SetAdminsActivity.this, MessagesController.getInstance(SetAdminsActivity.this.currentAccount).getChat(Integer.valueOf(SetAdminsActivity.this.chat_id)));
      if ((!(localChatParticipant instanceof TLRPC.TL_chatParticipant)) || ((SetAdminsActivity.this.chat != null) && (!SetAdminsActivity.this.chat.admins_enabled))) {}
      for (boolean bool1 = true;; bool1 = false)
      {
        paramViewHolder.setChecked(bool1, false);
        if ((SetAdminsActivity.this.chat != null) && (SetAdminsActivity.this.chat.admins_enabled))
        {
          bool1 = bool2;
          if (localChatParticipant.user_id != UserConfig.getInstance(SetAdminsActivity.this.currentAccount).getClientUserId()) {}
        }
        else
        {
          bool1 = true;
        }
        paramViewHolder.setCheckDisabled(bool1);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      return new RecyclerListView.Holder(new UserCell(this.mContext, 1, 2, false));
    }
    
    public void search(final String paramString)
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
              SetAdminsActivity.SearchAdapter.this.searchTimer.cancel();
              SetAdminsActivity.SearchAdapter.access$2802(SetAdminsActivity.SearchAdapter.this, null);
              SetAdminsActivity.SearchAdapter.this.processSearch(paramString);
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


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/SetAdminsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */