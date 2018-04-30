package ir.eitaa.ui.Components;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.support.widget.LinearLayoutManager;
import ir.eitaa.messenger.support.widget.RecyclerView.Adapter;
import ir.eitaa.messenger.support.widget.RecyclerView.LayoutParams;
import ir.eitaa.messenger.support.widget.RecyclerView.ViewHolder;
import ir.eitaa.tgnet.ConnectionsManager;
import ir.eitaa.tgnet.RequestDelegate;
import ir.eitaa.tgnet.TLObject;
import ir.eitaa.tgnet.TLRPC.Chat;
import ir.eitaa.tgnet.TLRPC.ChatInvite;
import ir.eitaa.tgnet.TLRPC.ChatPhoto;
import ir.eitaa.tgnet.TLRPC.TL_error;
import ir.eitaa.tgnet.TLRPC.TL_messages_importChatInvite;
import ir.eitaa.tgnet.TLRPC.Updates;
import ir.eitaa.tgnet.TLRPC.User;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.ActionBar.BottomSheet;
import ir.eitaa.ui.Cells.JoinSheetUserCell;
import ir.eitaa.ui.ChatActivity;
import java.util.ArrayList;

public class JoinGroupAlert
  extends BottomSheet
{
  private TLRPC.ChatInvite chatInvite;
  private BaseFragment fragment;
  private String hash;
  
  public JoinGroupAlert(Context paramContext, TLRPC.ChatInvite paramChatInvite, String paramString, BaseFragment paramBaseFragment)
  {
    super(paramContext, false);
    setApplyBottomPadding(false);
    setApplyTopPadding(false);
    this.fragment = paramBaseFragment;
    this.chatInvite = paramChatInvite;
    this.hash = paramString;
    LinearLayout localLinearLayout = new LinearLayout(paramContext);
    localLinearLayout.setOrientation(1);
    localLinearLayout.setClickable(true);
    setCustomView(localLinearLayout);
    paramBaseFragment = null;
    paramString = null;
    AvatarDrawable localAvatarDrawable;
    int i;
    if (paramChatInvite.chat != null)
    {
      localAvatarDrawable = new AvatarDrawable(paramChatInvite.chat);
      if (this.chatInvite.chat.photo != null) {
        paramString = this.chatInvite.chat.photo.photo_small;
      }
      paramBaseFragment = paramChatInvite.chat.title;
      i = paramChatInvite.chat.participants_count;
      BackupImageView localBackupImageView = new BackupImageView(paramContext);
      localBackupImageView.setRoundRadius(AndroidUtilities.dp(35.0F));
      localBackupImageView.setImage(paramString, "50_50", localAvatarDrawable);
      localLinearLayout.addView(localBackupImageView, LayoutHelper.createLinear(70, 70, 49, 0, 12, 0, 0));
      paramString = new TextView(paramContext);
      paramString.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      paramString.setTextSize(1, 17.0F);
      paramString.setTextColor(-14606047);
      paramString.setText(paramBaseFragment);
      paramString.setSingleLine(true);
      paramString.setEllipsize(TextUtils.TruncateAt.END);
      if (i <= 0) {
        break label686;
      }
    }
    label686:
    for (int j = 0;; j = 10)
    {
      localLinearLayout.addView(paramString, LayoutHelper.createLinear(-2, -2, 49, 10, 10, 10, j));
      if (i > 0)
      {
        paramString = new TextView(paramContext);
        paramString.setTextSize(1, 14.0F);
        paramString.setTextColor(-6710887);
        paramString.setSingleLine(true);
        paramString.setEllipsize(TextUtils.TruncateAt.END);
        paramString.setText(LocaleController.formatPluralString("Members", i));
        localLinearLayout.addView(paramString, LayoutHelper.createLinear(-2, -2, 49, 10, 4, 10, 10));
      }
      if (!paramChatInvite.participants.isEmpty())
      {
        paramChatInvite = new RecyclerListView(paramContext);
        paramChatInvite.setPadding(0, 0, 0, AndroidUtilities.dp(8.0F));
        paramChatInvite.setNestedScrollingEnabled(false);
        paramChatInvite.setClipToPadding(false);
        paramChatInvite.setLayoutManager(new LinearLayoutManager(getContext(), 0, false));
        paramChatInvite.setHorizontalScrollBarEnabled(false);
        paramChatInvite.setVerticalScrollBarEnabled(false);
        paramChatInvite.setAdapter(new UsersAdapter(paramContext));
        paramChatInvite.setGlowColor(33554431);
        localLinearLayout.addView(paramChatInvite, LayoutHelper.createLinear(-2, 90, 49, 0, 0, 0, 0));
      }
      paramChatInvite = new View(paramContext);
      paramChatInvite.setBackgroundResource(2130837700);
      localLinearLayout.addView(paramChatInvite, LayoutHelper.createLinear(-1, 3));
      paramContext = new PickerBottomLayout(paramContext, false);
      localLinearLayout.addView(paramContext, LayoutHelper.createFrame(-1, 48, 83));
      paramContext.cancelButton.setPadding(AndroidUtilities.dp(18.0F), 0, AndroidUtilities.dp(18.0F), 0);
      paramContext.cancelButton.setTextColor(-12940081);
      paramContext.cancelButton.setText(LocaleController.getString("Cancel", 2131165386).toUpperCase());
      paramContext.cancelButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          JoinGroupAlert.this.dismiss();
        }
      });
      paramContext.doneButton.setPadding(AndroidUtilities.dp(18.0F), 0, AndroidUtilities.dp(18.0F), 0);
      paramContext.doneButton.setVisibility(0);
      paramContext.doneButtonBadgeTextView.setVisibility(8);
      paramContext.doneButtonTextView.setTextColor(-12940081);
      paramContext.doneButtonTextView.setText(LocaleController.getString("JoinGroup", 2131165777));
      paramContext.doneButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          JoinGroupAlert.this.dismiss();
          paramAnonymousView = new TLRPC.TL_messages_importChatInvite();
          paramAnonymousView.hash = JoinGroupAlert.this.hash;
          ConnectionsManager.getInstance().sendRequest(paramAnonymousView, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
            {
              if (paramAnonymous2TL_error == null)
              {
                TLRPC.Updates localUpdates = (TLRPC.Updates)paramAnonymous2TLObject;
                MessagesController.getInstance().processUpdates(localUpdates, false);
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if ((JoinGroupAlert.this.fragment == null) || (JoinGroupAlert.this.fragment.getParentActivity() == null)) {}
                  Object localObject2;
                  do
                  {
                    do
                    {
                      return;
                      if (paramAnonymous2TL_error != null) {
                        break;
                      }
                      localObject2 = (TLRPC.Updates)paramAnonymous2TLObject;
                    } while (((TLRPC.Updates)localObject2).chats.isEmpty());
                    localObject1 = (TLRPC.Chat)((TLRPC.Updates)localObject2).chats.get(0);
                    ((TLRPC.Chat)localObject1).left = false;
                    ((TLRPC.Chat)localObject1).kicked = false;
                    MessagesController.getInstance().putUsers(((TLRPC.Updates)localObject2).users, false);
                    MessagesController.getInstance().putChats(((TLRPC.Updates)localObject2).chats, false);
                    localObject2 = new Bundle();
                    ((Bundle)localObject2).putInt("chat_id", ((TLRPC.Chat)localObject1).id);
                  } while (!MessagesController.checkCanOpenChat((Bundle)localObject2, JoinGroupAlert.this.fragment));
                  Object localObject1 = new ChatActivity((Bundle)localObject2);
                  JoinGroupAlert.this.fragment.presentFragment((BaseFragment)localObject1, JoinGroupAlert.this.fragment instanceof ChatActivity);
                  return;
                  localObject1 = new AlertDialog.Builder(JoinGroupAlert.this.fragment.getParentActivity());
                  ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131165299));
                  if (paramAnonymous2TL_error.text.startsWith("FLOOD_WAIT")) {
                    ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("FloodWait", 2131165642));
                  }
                  for (;;)
                  {
                    ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131166047), null);
                    JoinGroupAlert.this.fragment.showDialog(((AlertDialog.Builder)localObject1).create());
                    return;
                    if (paramAnonymous2TL_error.text.equals("USERS_TOO_MUCH")) {
                      ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("JoinToGroupErrorFull", 2131165779));
                    } else {
                      ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("JoinToGroupErrorNotExist", 2131165780));
                    }
                  }
                }
              });
            }
          }, 2);
        }
      });
      return;
      localAvatarDrawable = new AvatarDrawable();
      localAvatarDrawable.setInfo(0, paramChatInvite.title, null, false);
      paramString = paramBaseFragment;
      if (this.chatInvite.photo != null) {
        paramString = this.chatInvite.photo.photo_small;
      }
      paramBaseFragment = paramChatInvite.title;
      i = paramChatInvite.participants_count;
      break;
    }
  }
  
  private class Holder
    extends RecyclerView.ViewHolder
  {
    public Holder(View paramView)
    {
      super();
    }
  }
  
  private class UsersAdapter
    extends RecyclerView.Adapter
  {
    private Context context;
    
    public UsersAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    public int getItemCount()
    {
      int k = JoinGroupAlert.this.chatInvite.participants.size();
      if (JoinGroupAlert.this.chatInvite.chat != null) {}
      for (int i = JoinGroupAlert.this.chatInvite.chat.participants_count;; i = JoinGroupAlert.this.chatInvite.participants_count)
      {
        int j = k;
        if (k != i) {
          j = k + 1;
        }
        return j;
      }
    }
    
    public long getItemId(int paramInt)
    {
      return paramInt;
    }
    
    public int getItemViewType(int paramInt)
    {
      return 0;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      paramViewHolder = (JoinSheetUserCell)paramViewHolder.itemView;
      if (paramInt < JoinGroupAlert.this.chatInvite.participants.size())
      {
        paramViewHolder.setUser((TLRPC.User)JoinGroupAlert.this.chatInvite.participants.get(paramInt));
        return;
      }
      if (JoinGroupAlert.this.chatInvite.chat != null) {}
      for (paramInt = JoinGroupAlert.this.chatInvite.chat.participants_count;; paramInt = JoinGroupAlert.this.chatInvite.participants_count)
      {
        paramViewHolder.setCount(paramInt - JoinGroupAlert.this.chatInvite.participants.size());
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = new JoinSheetUserCell(this.context);
      paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(100.0F), AndroidUtilities.dp(90.0F)));
      return new JoinGroupAlert.Holder(JoinGroupAlert.this, paramViewGroup);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/JoinGroupAlert.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */