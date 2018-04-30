package ir.eitaa.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.ChatObject;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.NotificationCenter;
import ir.eitaa.messenger.NotificationCenter.NotificationCenterDelegate;
import ir.eitaa.tgnet.ConnectionsManager;
import ir.eitaa.tgnet.RequestDelegate;
import ir.eitaa.tgnet.TLObject;
import ir.eitaa.tgnet.TLRPC.Chat;
import ir.eitaa.tgnet.TLRPC.ChatFull;
import ir.eitaa.tgnet.TLRPC.ExportedChatInvite;
import ir.eitaa.tgnet.TLRPC.TL_channels_exportInvite;
import ir.eitaa.tgnet.TLRPC.TL_chatInviteExported;
import ir.eitaa.tgnet.TLRPC.TL_error;
import ir.eitaa.tgnet.TLRPC.TL_messages_exportChatInvite;
import ir.eitaa.ui.ActionBar.ActionBar;
import ir.eitaa.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.Adapters.BaseFragmentAdapter;
import ir.eitaa.ui.Cells.TextBlockCell;
import ir.eitaa.ui.Cells.TextInfoPrivacyCell;
import ir.eitaa.ui.Cells.TextSettingsCell;
import ir.eitaa.ui.Components.LayoutHelper;

public class GroupInviteActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private int chat_id;
  private int copyLinkRow;
  private TLRPC.ExportedChatInvite invite;
  private int linkInfoRow;
  private int linkRow;
  private ListAdapter listAdapter;
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
    if (ChatObject.isChannel(this.chat_id))
    {
      localObject = new TLRPC.TL_channels_exportInvite();
      ((TLRPC.TL_channels_exportInvite)localObject).channel = MessagesController.getInputChannel(this.chat_id);
    }
    for (;;)
    {
      int i = ConnectionsManager.getInstance().sendRequest((TLObject)localObject, new RequestDelegate()
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
                  localBuilder.setMessage(LocaleController.getString("RevokeAlertNewLink", 2131166194));
                  localBuilder.setTitle(LocaleController.getString("RevokeLink", 2131166196));
                  localBuilder.setNegativeButton(LocaleController.getString("OK", 2131166047), null);
                  GroupInviteActivity.this.showDialog(localBuilder.create());
                }
              }
              GroupInviteActivity.access$602(GroupInviteActivity.this, false);
              GroupInviteActivity.this.listAdapter.notifyDataSetChanged();
            }
          });
        }
      });
      ConnectionsManager.getInstance().bindRequestToGuid(i, this.classGuid);
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
    this.actionBar.setBackButtonImage(2130837705);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("InviteLink", 2131165765));
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
    FrameLayout localFrameLayout1 = (FrameLayout)this.fragmentView;
    localFrameLayout1.setBackgroundColor(-986896);
    FrameLayout localFrameLayout2 = new FrameLayout(paramContext);
    localFrameLayout1.addView(localFrameLayout2, LayoutHelper.createFrame(-1, -1.0F));
    localFrameLayout2.addView(new ProgressBar(paramContext), LayoutHelper.createFrame(-2, -2, 17));
    paramContext = new ListView(paramContext);
    paramContext.setDivider(null);
    paramContext.setDividerHeight(0);
    paramContext.setEmptyView(localFrameLayout2);
    paramContext.setVerticalScrollBarEnabled(false);
    paramContext.setDrawSelectorOnTop(true);
    localFrameLayout1.addView(paramContext, LayoutHelper.createFrame(-1, -1, 51));
    paramContext.setAdapter(this.listAdapter);
    paramContext.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong)
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
              Toast.makeText(GroupInviteActivity.this.getParentActivity(), LocaleController.getString("LinkCopied", 2131165826), 0).show();
              return;
            }
            catch (Exception paramAnonymousAdapterView)
            {
              FileLog.e("TSMS", paramAnonymousAdapterView);
              return;
            }
            if (paramAnonymousInt != GroupInviteActivity.this.shareLinkRow) {
              break;
            }
          } while (GroupInviteActivity.this.invite == null);
          try
          {
            paramAnonymousAdapterView = new Intent("android.intent.action.SEND");
            paramAnonymousAdapterView.setType("text/plain");
            paramAnonymousAdapterView.putExtra("android.intent.extra.TEXT", GroupInviteActivity.this.invite.link);
            GroupInviteActivity.this.getParentActivity().startActivityForResult(Intent.createChooser(paramAnonymousAdapterView, LocaleController.getString("InviteToGroupByLink", 2131165768)), 500);
            return;
          }
          catch (Exception paramAnonymousAdapterView)
          {
            FileLog.e("TSMS", paramAnonymousAdapterView);
            return;
          }
        } while (paramAnonymousInt != GroupInviteActivity.this.revokeLinkRow);
        paramAnonymousAdapterView = new AlertDialog.Builder(GroupInviteActivity.this.getParentActivity());
        paramAnonymousAdapterView.setMessage(LocaleController.getString("RevokeAlert", 2131166193));
        paramAnonymousAdapterView.setTitle(LocaleController.getString("RevokeLink", 2131166196));
        paramAnonymousAdapterView.setPositiveButton(LocaleController.getString("RevokeButton", 2131166195), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
          {
            GroupInviteActivity.this.generateLink(true);
          }
        });
        paramAnonymousAdapterView.setNegativeButton(LocaleController.getString("Cancel", 2131165386), null);
        GroupInviteActivity.this.showDialog(paramAnonymousAdapterView.create());
      }
    });
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt, Object... paramVarArgs)
  {
    if (paramInt == NotificationCenter.chatInfoDidLoaded)
    {
      TLRPC.ChatFull localChatFull = (TLRPC.ChatFull)paramVarArgs[0];
      paramInt = ((Integer)paramVarArgs[1]).intValue();
      if ((localChatFull.id == this.chat_id) && (paramInt == this.classGuid))
      {
        this.invite = MessagesController.getInstance().getExportedInvite(this.chat_id);
        if ((this.invite instanceof TLRPC.TL_chatInviteExported)) {
          break label73;
        }
        generateLink(false);
      }
    }
    label73:
    do
    {
      return;
      this.loading = false;
    } while (this.listAdapter == null);
    this.listAdapter.notifyDataSetChanged();
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoDidLoaded);
    MessagesController.getInstance().loadFullChat(this.chat_id, this.classGuid, true);
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
    NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
  }
  
  private class ListAdapter
    extends BaseFragmentAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public boolean areAllItemsEnabled()
    {
      return false;
    }
    
    public int getCount()
    {
      if (GroupInviteActivity.this.loading) {
        return 0;
      }
      return GroupInviteActivity.this.rowCount;
    }
    
    public Object getItem(int paramInt)
    {
      return null;
    }
    
    public long getItemId(int paramInt)
    {
      return paramInt;
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
    
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
    {
      int i = getItemViewType(paramInt);
      if (i == 0)
      {
        localObject = paramView;
        if (paramView == null)
        {
          localObject = new TextSettingsCell(this.mContext);
          ((View)localObject).setBackgroundColor(-1);
        }
        paramView = (TextSettingsCell)localObject;
        if (paramInt == GroupInviteActivity.this.copyLinkRow)
        {
          paramView.setText(LocaleController.getString("CopyLink", 2131165532), true);
          paramViewGroup = (ViewGroup)localObject;
        }
      }
      do
      {
        do
        {
          do
          {
            return paramViewGroup;
            if (paramInt == GroupInviteActivity.this.shareLinkRow)
            {
              paramView.setText(LocaleController.getString("ShareLink", 2131166278), false);
              return (View)localObject;
            }
            paramViewGroup = (ViewGroup)localObject;
          } while (paramInt != GroupInviteActivity.this.revokeLinkRow);
          paramView.setText(LocaleController.getString("RevokeLink", 2131166196), true);
          return (View)localObject;
          if (i != 1) {
            break;
          }
          localObject = paramView;
          if (paramView == null) {
            localObject = new TextInfoPrivacyCell(this.mContext);
          }
          if (paramInt == GroupInviteActivity.this.shadowRow)
          {
            ((TextInfoPrivacyCell)localObject).setText("");
            ((View)localObject).setBackgroundResource(2130837695);
            return (View)localObject;
          }
          paramViewGroup = (ViewGroup)localObject;
        } while (paramInt != GroupInviteActivity.this.linkInfoRow);
        paramView = MessagesController.getInstance().getChat(Integer.valueOf(GroupInviteActivity.this.chat_id));
        if ((ChatObject.isChannel(paramView)) && (!paramView.megagroup)) {
          ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("ChannelLinkInfo", 2131165433));
        }
        for (;;)
        {
          ((View)localObject).setBackgroundResource(2130837694);
          return (View)localObject;
          ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("LinkInfo", 2131165828));
        }
        paramViewGroup = paramView;
      } while (i != 2);
      paramViewGroup = paramView;
      if (paramView == null)
      {
        paramViewGroup = new TextBlockCell(this.mContext);
        paramViewGroup.setBackgroundColor(-1);
      }
      Object localObject = (TextBlockCell)paramViewGroup;
      if (GroupInviteActivity.this.invite != null) {}
      for (paramView = GroupInviteActivity.this.invite.link;; paramView = "error")
      {
        ((TextBlockCell)localObject).setText(paramView, false);
        return paramViewGroup;
      }
    }
    
    public int getViewTypeCount()
    {
      return 3;
    }
    
    public boolean hasStableIds()
    {
      return false;
    }
    
    public boolean isEmpty()
    {
      return GroupInviteActivity.this.loading;
    }
    
    public boolean isEnabled(int paramInt)
    {
      return (paramInt == GroupInviteActivity.this.revokeLinkRow) || (paramInt == GroupInviteActivity.this.copyLinkRow) || (paramInt == GroupInviteActivity.this.shareLinkRow) || (paramInt == GroupInviteActivity.this.linkRow);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/GroupInviteActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */