package org.telegram.ui;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.TL_channelAdminRights;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioButtonCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell2;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class ChannelPermissionsActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int done_button = 1;
  private int addUsersRow;
  private TLRPC.TL_channelAdminRights adminRights;
  private int changeInfoRow;
  private int chatId;
  private int embedLinksRow;
  private int forwardRow;
  private int forwardShadowRow;
  private HeaderCell headerCell2;
  private boolean historyHidden;
  private TLRPC.ChatFull info;
  private LinearLayout linearLayout;
  private RecyclerListView listView;
  private ListAdapter listViewAdapter;
  private int permissionsHeaderRow;
  private RadioButtonCell radioButtonCell3;
  private RadioButtonCell radioButtonCell4;
  private int rightsShadowRow;
  private int rowCount;
  private int sendMediaRow;
  private int sendStickersRow;
  
  public ChannelPermissionsActivity(int paramInt)
  {
    this.chatId = paramInt;
    this.adminRights = new TLRPC.TL_channelAdminRights();
    this.rowCount = 0;
    paramInt = this.rowCount;
    this.rowCount = (paramInt + 1);
    this.permissionsHeaderRow = paramInt;
    paramInt = this.rowCount;
    this.rowCount = (paramInt + 1);
    this.sendMediaRow = paramInt;
    paramInt = this.rowCount;
    this.rowCount = (paramInt + 1);
    this.sendStickersRow = paramInt;
    paramInt = this.rowCount;
    this.rowCount = (paramInt + 1);
    this.embedLinksRow = paramInt;
    paramInt = this.rowCount;
    this.rowCount = (paramInt + 1);
    this.addUsersRow = paramInt;
    paramInt = this.rowCount;
    this.rowCount = (paramInt + 1);
    this.changeInfoRow = paramInt;
    paramInt = this.rowCount;
    this.rowCount = (paramInt + 1);
    this.rightsShadowRow = paramInt;
    TLRPC.Chat localChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
    if ((localChat != null) && (TextUtils.isEmpty(localChat.username)))
    {
      paramInt = this.rowCount;
      this.rowCount = (paramInt + 1);
      this.forwardRow = paramInt;
      paramInt = this.rowCount;
      this.rowCount = (paramInt + 1);
      this.forwardShadowRow = paramInt;
      return;
    }
    this.forwardRow = -1;
    this.forwardShadowRow = -1;
  }
  
  public View createView(Context paramContext)
  {
    boolean bool = true;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          ChannelPermissionsActivity.this.finishFragment();
        }
        while (paramAnonymousInt != 1) {
          return;
        }
        if ((ChannelPermissionsActivity.this.headerCell2 != null) && (ChannelPermissionsActivity.this.headerCell2.getVisibility() == 0) && (ChannelPermissionsActivity.this.info != null) && (ChannelPermissionsActivity.this.info.hidden_prehistory != ChannelPermissionsActivity.this.historyHidden))
        {
          ChannelPermissionsActivity.this.info.hidden_prehistory = ChannelPermissionsActivity.this.historyHidden;
          MessagesController.getInstance(ChannelPermissionsActivity.this.currentAccount).toogleChannelInvitesHistory(ChannelPermissionsActivity.this.chatId, ChannelPermissionsActivity.this.historyHidden);
        }
        ChannelPermissionsActivity.this.finishFragment();
      }
    });
    this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    this.fragmentView = new FrameLayout(paramContext);
    this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    Object localObject1 = (FrameLayout)this.fragmentView;
    this.listView = new RecyclerListView(paramContext);
    Object localObject2 = new LinearLayoutManager(paramContext, 1, false)
    {
      public boolean supportsPredictiveItemAnimations()
      {
        return false;
      }
    };
    this.listView.setItemAnimator(null);
    this.listView.setLayoutAnimation(null);
    this.listView.setLayoutManager((RecyclerView.LayoutManager)localObject2);
    localObject2 = this.listView;
    Object localObject3 = new ListAdapter(paramContext);
    this.listViewAdapter = ((ListAdapter)localObject3);
    ((RecyclerListView)localObject2).setAdapter((RecyclerView.Adapter)localObject3);
    localObject2 = this.listView;
    int i;
    if (LocaleController.isRTL)
    {
      i = 1;
      ((RecyclerListView)localObject2).setVerticalScrollbarPosition(i);
      ((FrameLayout)localObject1).addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          boolean bool3 = true;
          boolean bool4 = true;
          boolean bool5 = true;
          boolean bool6 = true;
          boolean bool2 = true;
          if ((paramAnonymousView instanceof TextCheckCell2))
          {
            paramAnonymousView = (TextCheckCell2)paramAnonymousView;
            if (paramAnonymousView.isEnabled()) {
              break label35;
            }
          }
          label35:
          label95:
          label100:
          do
          {
            return;
            if (!paramAnonymousView.isChecked())
            {
              bool1 = true;
              paramAnonymousView.setChecked(bool1);
              if (paramAnonymousInt != ChannelPermissionsActivity.this.changeInfoRow) {
                break label100;
              }
              paramAnonymousView = ChannelPermissionsActivity.this.adminRights;
              if (ChannelPermissionsActivity.this.adminRights.change_info) {
                break label95;
              }
            }
            for (bool1 = bool2;; bool1 = false)
            {
              paramAnonymousView.change_info = bool1;
              return;
              bool1 = false;
              break;
            }
            if (paramAnonymousInt == ChannelPermissionsActivity.this.addUsersRow)
            {
              paramAnonymousView = ChannelPermissionsActivity.this.adminRights;
              if (!ChannelPermissionsActivity.this.adminRights.invite_users) {}
              for (bool1 = bool3;; bool1 = false)
              {
                paramAnonymousView.invite_users = bool1;
                return;
              }
            }
            if (paramAnonymousInt == ChannelPermissionsActivity.this.sendMediaRow)
            {
              paramAnonymousView = ChannelPermissionsActivity.this.adminRights;
              if (!ChannelPermissionsActivity.this.adminRights.ban_users) {}
              for (bool1 = bool4;; bool1 = false)
              {
                paramAnonymousView.ban_users = bool1;
                return;
              }
            }
            if (paramAnonymousInt == ChannelPermissionsActivity.this.sendStickersRow)
            {
              paramAnonymousView = ChannelPermissionsActivity.this.adminRights;
              if (!ChannelPermissionsActivity.this.adminRights.add_admins) {}
              for (bool1 = bool5;; bool1 = false)
              {
                paramAnonymousView.add_admins = bool1;
                return;
              }
            }
          } while (paramAnonymousInt != ChannelPermissionsActivity.this.embedLinksRow);
          paramAnonymousView = ChannelPermissionsActivity.this.adminRights;
          if (!ChannelPermissionsActivity.this.adminRights.pin_messages) {}
          for (boolean bool1 = bool6;; bool1 = false)
          {
            paramAnonymousView.pin_messages = bool1;
            return;
          }
        }
      });
      this.linearLayout = new LinearLayout(paramContext);
      this.linearLayout.setOrientation(1);
      this.linearLayout.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      this.linearLayout.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
      this.headerCell2 = new HeaderCell(paramContext);
      this.headerCell2.setText(LocaleController.getString("ChatHistory", 2131493226));
      this.headerCell2.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      this.linearLayout.addView(this.headerCell2);
      this.radioButtonCell3 = new RadioButtonCell(paramContext);
      this.radioButtonCell3.setBackgroundDrawable(Theme.getSelectorDrawable(false));
      localObject1 = this.radioButtonCell3;
      localObject2 = LocaleController.getString("ChatHistoryVisible", 2131493229);
      localObject3 = LocaleController.getString("ChatHistoryVisibleInfo", 2131493230);
      if (this.historyHidden) {
        break label512;
      }
    }
    for (;;)
    {
      ((RadioButtonCell)localObject1).setTextAndValue((String)localObject2, (String)localObject3, bool);
      this.linearLayout.addView(this.radioButtonCell3, LayoutHelper.createLinear(-1, -2));
      this.radioButtonCell3.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          ChannelPermissionsActivity.this.radioButtonCell3.setChecked(true, true);
          ChannelPermissionsActivity.this.radioButtonCell4.setChecked(false, true);
          ChannelPermissionsActivity.access$202(ChannelPermissionsActivity.this, false);
        }
      });
      this.radioButtonCell4 = new RadioButtonCell(paramContext);
      this.radioButtonCell4.setBackgroundDrawable(Theme.getSelectorDrawable(false));
      this.radioButtonCell4.setTextAndValue(LocaleController.getString("ChatHistoryHidden", 2131493227), LocaleController.getString("ChatHistoryHiddenInfo", 2131493228), this.historyHidden);
      this.linearLayout.addView(this.radioButtonCell4, LayoutHelper.createLinear(-1, -2));
      this.radioButtonCell4.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          ChannelPermissionsActivity.this.radioButtonCell3.setChecked(false, true);
          ChannelPermissionsActivity.this.radioButtonCell4.setChecked(true, true);
          ChannelPermissionsActivity.access$202(ChannelPermissionsActivity.this, true);
        }
      });
      return this.fragmentView;
      i = 2;
      break;
      label512:
      bool = false;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    RadioButtonCell localRadioButtonCell;
    if (paramInt1 == NotificationCenter.chatInfoDidLoaded)
    {
      paramVarArgs = (TLRPC.ChatFull)paramVarArgs[0];
      if (paramVarArgs.id == this.chatId) {
        if (this.info == null)
        {
          this.historyHidden = paramVarArgs.hidden_prehistory;
          if (this.radioButtonCell3 != null)
          {
            localRadioButtonCell = this.radioButtonCell3;
            if (this.historyHidden) {
              break label89;
            }
          }
        }
      }
    }
    label89:
    for (boolean bool = true;; bool = false)
    {
      localRadioButtonCell.setChecked(bool, false);
      this.radioButtonCell4.setChecked(this.historyHidden, false);
      this.info = paramVarArgs;
      return;
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextCheckCell2.class, HeaderCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell2.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell2.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell2.class }, new String[] { "checkBox" }, null, null, null, "switchThumb"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell2.class }, new String[] { "checkBox" }, null, null, null, "switchTrack"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell2.class }, new String[] { "checkBox" }, null, null, null, "switchThumbChecked"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell2.class }, new String[] { "checkBox" }, null, null, null, "switchTrackChecked"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader"), new ThemeDescription(this.linearLayout, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.radioButtonCell3, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21"), new ThemeDescription(this.radioButtonCell3, ThemeDescription.FLAG_CHECKBOX, new Class[] { RadioButtonCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackground"), new ThemeDescription(this.radioButtonCell3, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { RadioButtonCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackgroundChecked"), new ThemeDescription(this.radioButtonCell3, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { RadioButtonCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.radioButtonCell3, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { RadioButtonCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.radioButtonCell4, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21"), new ThemeDescription(this.radioButtonCell4, ThemeDescription.FLAG_CHECKBOX, new Class[] { RadioButtonCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackground"), new ThemeDescription(this.radioButtonCell4, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { RadioButtonCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackgroundChecked"), new ThemeDescription(this.radioButtonCell4, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { RadioButtonCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.radioButtonCell4, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { RadioButtonCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2") };
  }
  
  public boolean onFragmentCreate()
  {
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
    return super.onFragmentCreate();
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
  
  public void setInfo(TLRPC.ChatFull paramChatFull)
  {
    if ((this.info == null) && (paramChatFull != null)) {
      this.historyHidden = paramChatFull.hidden_prehistory;
    }
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
    
    public int getItemCount()
    {
      return ChannelPermissionsActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      int i = 0;
      if ((paramInt == ChannelPermissionsActivity.this.rightsShadowRow) || (paramInt == ChannelPermissionsActivity.this.forwardShadowRow)) {
        i = 2;
      }
      do
      {
        return i;
        if ((paramInt == ChannelPermissionsActivity.this.changeInfoRow) || (paramInt == ChannelPermissionsActivity.this.addUsersRow) || (paramInt == ChannelPermissionsActivity.this.sendMediaRow) || (paramInt == ChannelPermissionsActivity.this.sendStickersRow) || (paramInt == ChannelPermissionsActivity.this.embedLinksRow)) {
          return 1;
        }
        if (paramInt == ChannelPermissionsActivity.this.forwardRow) {
          return 3;
        }
      } while (paramInt != ChannelPermissionsActivity.this.permissionsHeaderRow);
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return paramViewHolder.getItemViewType() == 1;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      int i = 2131165332;
      switch (paramViewHolder.getItemViewType())
      {
      default: 
        return;
      }
      paramViewHolder = (ShadowSectionCell)paramViewHolder.itemView;
      if (paramInt == ChannelPermissionsActivity.this.rightsShadowRow)
      {
        Context localContext = this.mContext;
        if (ChannelPermissionsActivity.this.forwardShadowRow == -1) {}
        for (paramInt = i;; paramInt = 2131165331)
        {
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(localContext, paramInt, "windowBackgroundGrayShadow"));
          return;
        }
      }
      paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = ChannelPermissionsActivity.this.linearLayout;
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new HeaderCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextCheckCell2(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new ShadowSectionCell(this.mContext);
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ChannelPermissionsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */