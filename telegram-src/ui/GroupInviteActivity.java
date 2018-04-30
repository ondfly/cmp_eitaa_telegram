package org.telegram.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ExportedChatInvite;
import org.telegram.tgnet.TLRPC.TL_channels_exportInvite;
import org.telegram.tgnet.TLRPC.TL_chatInviteExported;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_exportChatInvite;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.TextBlockCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class GroupInviteActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private int chat_id;
  private int copyLinkRow;
  private EmptyTextProgressView emptyView;
  private TLRPC.ExportedChatInvite invite;
  private int linkInfoRow;
  private int linkRow;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private boolean loading;
  private int revokeLinkRow;
  private int rowCount;
  private int shadowRow;
  private int shareLinkRow;
  
  public GroupInviteActivity(int paramInt)
  {
    this.chat_id = paramInt;
  }
  
  private void generateLink(final boolean paramBoolean)
  {
    this.loading = true;
    Object localObject;
    if (ChatObject.isChannel(this.chat_id, this.currentAccount))
    {
      localObject = new TLRPC.TL_channels_exportInvite();
      ((TLRPC.TL_channels_exportInvite)localObject).channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chat_id);
    }
    for (;;)
    {
      int i = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error == null)
              {
                GroupInviteActivity.access$202(GroupInviteActivity.this, (TLRPC.ExportedChatInvite)paramAnonymousTLObject);
                if (GroupInviteActivity.3.this.val$newRequest)
                {
                  if (GroupInviteActivity.this.getParentActivity() == null) {
                    return;
                  }
                  AlertDialog.Builder localBuilder = new AlertDialog.Builder(GroupInviteActivity.this.getParentActivity());
                  localBuilder.setMessage(LocaleController.getString("RevokeAlertNewLink", 2131494277));
                  localBuilder.setTitle(LocaleController.getString("RevokeLink", 2131494279));
                  localBuilder.setNegativeButton(LocaleController.getString("OK", 2131494028), null);
                  GroupInviteActivity.this.showDialog(localBuilder.create());
                }
              }
              GroupInviteActivity.access$602(GroupInviteActivity.this, false);
              GroupInviteActivity.this.listAdapter.notifyDataSetChanged();
            }
          });
        }
      });
      ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(i, this.classGuid);
      if (this.listAdapter != null) {
        this.listAdapter.notifyDataSetChanged();
      }
      return;
      localObject = new TLRPC.TL_messages_exportChatInvite();
      ((TLRPC.TL_messages_exportChatInvite)localObject).chat_id = this.chat_id;
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("InviteLink", 2131493687));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          GroupInviteActivity.this.finishFragment();
        }
      }
    });
    this.listAdapter = new ListAdapter(paramContext);
    this.fragmentView = new FrameLayout(paramContext);
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    localFrameLayout.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.emptyView = new EmptyTextProgressView(paramContext);
    this.emptyView.showProgress();
    localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1, 51));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
    this.listView.setEmptyView(this.emptyView);
    this.listView.setVerticalScrollBarEnabled(false);
    localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
    this.listView.setAdapter(this.listAdapter);
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        if (GroupInviteActivity.this.getParentActivity() == null) {}
        do
        {
          do
          {
            do
            {
              return;
              if ((paramAnonymousInt != GroupInviteActivity.this.copyLinkRow) && (paramAnonymousInt != GroupInviteActivity.this.linkRow)) {
                break;
              }
            } while (GroupInviteActivity.this.invite == null);
            try
            {
              ((ClipboardManager)ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", GroupInviteActivity.this.invite.link));
              Toast.makeText(GroupInviteActivity.this.getParentActivity(), LocaleController.getString("LinkCopied", 2131493748), 0).show();
              return;
            }
            catch (Exception paramAnonymousView)
            {
              FileLog.e(paramAnonymousView);
              return;
            }
            if (paramAnonymousInt != GroupInviteActivity.this.shareLinkRow) {
              break;
            }
          } while (GroupInviteActivity.this.invite == null);
          try
          {
            paramAnonymousView = new Intent("android.intent.action.SEND");
            paramAnonymousView.setType("text/plain");
            paramAnonymousView.putExtra("android.intent.extra.TEXT", GroupInviteActivity.this.invite.link);
            GroupInviteActivity.this.getParentActivity().startActivityForResult(Intent.createChooser(paramAnonymousView, LocaleController.getString("InviteToGroupByLink", 2131493696)), 500);
            return;
          }
          catch (Exception paramAnonymousView)
          {
            FileLog.e(paramAnonymousView);
            return;
          }
        } while (paramAnonymousInt != GroupInviteActivity.this.revokeLinkRow);
        paramAnonymousView = new AlertDialog.Builder(GroupInviteActivity.this.getParentActivity());
        paramAnonymousView.setMessage(LocaleController.getString("RevokeAlert", 2131494276));
        paramAnonymousView.setTitle(LocaleController.getString("RevokeLink", 2131494279));
        paramAnonymousView.setPositiveButton(LocaleController.getString("RevokeButton", 2131494278), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
          {
            GroupInviteActivity.this.generateLink(true);
          }
        });
        paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
        GroupInviteActivity.this.showDialog(paramAnonymousView.create());
      }
    });
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.chatInfoDidLoaded)
    {
      TLRPC.ChatFull localChatFull = (TLRPC.ChatFull)paramVarArgs[0];
      paramInt1 = ((Integer)paramVarArgs[1]).intValue();
      if ((localChatFull.id == this.chat_id) && (paramInt1 == this.classGuid))
      {
        this.invite = MessagesController.getInstance(this.currentAccount).getExportedInvite(this.chat_id);
        if ((this.invite instanceof TLRPC.TL_chatInviteExported)) {
          break label79;
        }
        generateLink(false);
      }
    }
    label79:
    do
    {
      return;
      this.loading = false;
    } while (this.listAdapter == null);
    this.listAdapter.notifyDataSetChanged();
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextSettingsCell.class, TextBlockCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.listView, 0, new Class[] { TextBlockCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
    MessagesController.getInstance(this.currentAccount).loadFullChat(this.chat_id, this.classGuid, true);
    this.loading = true;
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.linkRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.linkInfoRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.copyLinkRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.revokeLinkRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.shareLinkRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.shadowRow = i;
    return true;
  }
  
  public void onFragmentDestroy()
  {
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoaded);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
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
      if (GroupInviteActivity.this.loading) {
        return 0;
      }
      return GroupInviteActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == GroupInviteActivity.this.copyLinkRow) || (paramInt == GroupInviteActivity.this.shareLinkRow) || (paramInt == GroupInviteActivity.this.revokeLinkRow)) {}
      do
      {
        return 0;
        if ((paramInt == GroupInviteActivity.this.shadowRow) || (paramInt == GroupInviteActivity.this.linkInfoRow)) {
          return 1;
        }
      } while (paramInt != GroupInviteActivity.this.linkRow);
      return 2;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getAdapterPosition();
      return (i == GroupInviteActivity.this.revokeLinkRow) || (i == GroupInviteActivity.this.copyLinkRow) || (i == GroupInviteActivity.this.shareLinkRow) || (i == GroupInviteActivity.this.linkRow);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      default: 
      case 0: 
      case 1: 
        do
        {
          do
          {
            return;
            paramViewHolder = (TextSettingsCell)paramViewHolder.itemView;
            if (paramInt == GroupInviteActivity.this.copyLinkRow)
            {
              paramViewHolder.setText(LocaleController.getString("CopyLink", 2131493304), true);
              return;
            }
            if (paramInt == GroupInviteActivity.this.shareLinkRow)
            {
              paramViewHolder.setText(LocaleController.getString("ShareLink", 2131494384), false);
              return;
            }
          } while (paramInt != GroupInviteActivity.this.revokeLinkRow);
          paramViewHolder.setText(LocaleController.getString("RevokeLink", 2131494279), true);
          return;
          paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
          if (paramInt == GroupInviteActivity.this.shadowRow)
          {
            paramViewHolder.setText("");
            paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
            return;
          }
        } while (paramInt != GroupInviteActivity.this.linkInfoRow);
        localObject = MessagesController.getInstance(GroupInviteActivity.this.currentAccount).getChat(Integer.valueOf(GroupInviteActivity.this.chat_id));
        if ((ChatObject.isChannel((TLRPC.Chat)localObject)) && (!((TLRPC.Chat)localObject).megagroup)) {
          paramViewHolder.setText(LocaleController.getString("ChannelLinkInfo", 2131493174));
        }
        for (;;)
        {
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow"));
          return;
          paramViewHolder.setText(LocaleController.getString("LinkInfo", 2131493750));
        }
      }
      Object localObject = (TextBlockCell)paramViewHolder.itemView;
      if (GroupInviteActivity.this.invite != null) {}
      for (paramViewHolder = GroupInviteActivity.this.invite.link;; paramViewHolder = "error")
      {
        ((TextBlockCell)localObject).setText(paramViewHolder, false);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new TextBlockCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new TextSettingsCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/GroupInviteActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */