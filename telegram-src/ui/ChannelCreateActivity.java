package org.telegram.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ExportedChatInvite;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_channels_checkUsername;
import org.telegram.tgnet.TLRPC.TL_channels_exportInvite;
import org.telegram.tgnet.TLRPC.TL_channels_getAdminedPublicChannels;
import org.telegram.tgnet.TLRPC.TL_channels_updateUsername;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputChannelEmpty;
import org.telegram.tgnet.TLRPC.TL_messages_chats;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Cells.AdminedChannelCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.RadioButtonCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextBlockCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.AvatarUpdater.AvatarUpdaterDelegate;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

public class ChannelCreateActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate, AvatarUpdater.AvatarUpdaterDelegate
{
  private static final int done_button = 1;
  private ArrayList<AdminedChannelCell> adminedChannelCells = new ArrayList();
  private TextInfoPrivacyCell adminedInfoCell;
  private LinearLayout adminnedChannelsLayout;
  private TLRPC.FileLocation avatar;
  private AvatarDrawable avatarDrawable;
  private BackupImageView avatarImage;
  private AvatarUpdater avatarUpdater;
  private boolean canCreatePublic = true;
  private int chatId;
  private int checkReqId;
  private Runnable checkRunnable;
  private TextView checkTextView;
  private boolean createAfterUpload;
  private int currentStep;
  private EditTextBoldCursor descriptionTextView;
  private View doneButton;
  private boolean donePressed;
  private EditText editText;
  private HeaderCell headerCell;
  private TextView helpTextView;
  private TLRPC.ExportedChatInvite invite;
  private boolean isPrivate;
  private String lastCheckName;
  private boolean lastNameAvailable;
  private LinearLayout linearLayout;
  private LinearLayout linearLayout2;
  private LinearLayout linkContainer;
  private LoadingCell loadingAdminedCell;
  private boolean loadingAdminedChannels;
  private boolean loadingInvite;
  private EditTextBoldCursor nameTextView;
  private String nameToSet;
  private TextBlockCell privateContainer;
  private AlertDialog progressDialog;
  private LinearLayout publicContainer;
  private RadioButtonCell radioButtonCell1;
  private RadioButtonCell radioButtonCell2;
  private ShadowSectionCell sectionCell;
  private TextInfoPrivacyCell typeInfoCell;
  private TLRPC.InputFile uploadedAvatar;
  
  public ChannelCreateActivity(Bundle paramBundle)
  {
    super(paramBundle);
    this.currentStep = paramBundle.getInt("step", 0);
    if (this.currentStep == 0)
    {
      this.avatarDrawable = new AvatarDrawable();
      this.avatarUpdater = new AvatarUpdater();
      paramBundle = new TLRPC.TL_channels_checkUsername();
      paramBundle.username = "1";
      paramBundle.channel = new TLRPC.TL_inputChannelEmpty();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramBundle, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              ChannelCreateActivity localChannelCreateActivity = ChannelCreateActivity.this;
              if ((paramAnonymousTL_error == null) || (!paramAnonymousTL_error.text.equals("CHANNELS_ADMIN_PUBLIC_TOO_MUCH"))) {}
              for (boolean bool = true;; bool = false)
              {
                ChannelCreateActivity.access$002(localChannelCreateActivity, bool);
                return;
              }
            }
          });
        }
      });
      return;
    }
    if (this.currentStep == 1)
    {
      this.canCreatePublic = paramBundle.getBoolean("canCreatePublic", true);
      if (this.canCreatePublic) {
        break label163;
      }
    }
    for (;;)
    {
      this.isPrivate = bool;
      if (!this.canCreatePublic) {
        loadAdminedChannels();
      }
      this.chatId = paramBundle.getInt("chat_id", 0);
      return;
      label163:
      bool = false;
    }
  }
  
  private boolean checkUserName(final String paramString)
  {
    if ((paramString != null) && (paramString.length() > 0)) {
      this.checkTextView.setVisibility(0);
    }
    for (;;)
    {
      if (this.checkRunnable != null)
      {
        AndroidUtilities.cancelRunOnUIThread(this.checkRunnable);
        this.checkRunnable = null;
        this.lastCheckName = null;
        if (this.checkReqId != 0) {
          ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.checkReqId, true);
        }
      }
      this.lastNameAvailable = false;
      if (paramString == null) {
        break label310;
      }
      if ((!paramString.startsWith("_")) && (!paramString.endsWith("_"))) {
        break;
      }
      this.checkTextView.setText(LocaleController.getString("LinkInvalid", 2131493751));
      this.checkTextView.setTag("windowBackgroundWhiteRedText4");
      this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
      return false;
      this.checkTextView.setVisibility(8);
    }
    int i = 0;
    while (i < paramString.length())
    {
      int j = paramString.charAt(i);
      if ((i == 0) && (j >= 48) && (j <= 57))
      {
        this.checkTextView.setText(LocaleController.getString("LinkInvalidStartNumber", 2131493755));
        this.checkTextView.setTag("windowBackgroundWhiteRedText4");
        this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
        return false;
      }
      if (((j < 48) || (j > 57)) && ((j < 97) || (j > 122)) && ((j < 65) || (j > 90)) && (j != 95))
      {
        this.checkTextView.setText(LocaleController.getString("LinkInvalid", 2131493751));
        this.checkTextView.setTag("windowBackgroundWhiteRedText4");
        this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
        return false;
      }
      i += 1;
    }
    label310:
    if ((paramString == null) || (paramString.length() < 5))
    {
      this.checkTextView.setText(LocaleController.getString("LinkInvalidShort", 2131493753));
      this.checkTextView.setTag("windowBackgroundWhiteRedText4");
      this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
      return false;
    }
    if (paramString.length() > 32)
    {
      this.checkTextView.setText(LocaleController.getString("LinkInvalidLong", 2131493752));
      this.checkTextView.setTag("windowBackgroundWhiteRedText4");
      this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
      return false;
    }
    this.checkTextView.setText(LocaleController.getString("LinkChecking", 2131493747));
    this.checkTextView.setTag("windowBackgroundWhiteGrayText8");
    this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText8"));
    this.lastCheckName = paramString;
    this.checkRunnable = new Runnable()
    {
      public void run()
      {
        TLRPC.TL_channels_checkUsername localTL_channels_checkUsername = new TLRPC.TL_channels_checkUsername();
        localTL_channels_checkUsername.username = paramString;
        localTL_channels_checkUsername.channel = MessagesController.getInstance(ChannelCreateActivity.this.currentAccount).getInputChannel(ChannelCreateActivity.this.chatId);
        ChannelCreateActivity.access$3402(ChannelCreateActivity.this, ConnectionsManager.getInstance(ChannelCreateActivity.this.currentAccount).sendRequest(localTL_channels_checkUsername, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                ChannelCreateActivity.access$3402(ChannelCreateActivity.this, 0);
                if ((ChannelCreateActivity.this.lastCheckName != null) && (ChannelCreateActivity.this.lastCheckName.equals(ChannelCreateActivity.14.this.val$name)))
                {
                  if ((paramAnonymous2TL_error == null) && ((paramAnonymous2TLObject instanceof TLRPC.TL_boolTrue)))
                  {
                    ChannelCreateActivity.this.checkTextView.setText(LocaleController.formatString("LinkAvailable", 2131493746, new Object[] { ChannelCreateActivity.14.this.val$name }));
                    ChannelCreateActivity.this.checkTextView.setTag("windowBackgroundWhiteGreenText");
                    ChannelCreateActivity.this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGreenText"));
                    ChannelCreateActivity.access$1102(ChannelCreateActivity.this, true);
                  }
                }
                else {
                  return;
                }
                if ((paramAnonymous2TL_error != null) && (paramAnonymous2TL_error.text.equals("CHANNELS_ADMIN_PUBLIC_TOO_MUCH")))
                {
                  ChannelCreateActivity.access$002(ChannelCreateActivity.this, false);
                  ChannelCreateActivity.this.loadAdminedChannels();
                }
                for (;;)
                {
                  ChannelCreateActivity.this.checkTextView.setTag("windowBackgroundWhiteRedText4");
                  ChannelCreateActivity.this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
                  ChannelCreateActivity.access$1102(ChannelCreateActivity.this, false);
                  return;
                  ChannelCreateActivity.this.checkTextView.setText(LocaleController.getString("LinkInUse", 2131493749));
                }
              }
            });
          }
        }, 2));
      }
    };
    AndroidUtilities.runOnUIThread(this.checkRunnable, 300L);
    return true;
  }
  
  private void generateLink()
  {
    if ((this.loadingInvite) || (this.invite != null)) {
      return;
    }
    this.loadingInvite = true;
    TLRPC.TL_channels_exportInvite localTL_channels_exportInvite = new TLRPC.TL_channels_exportInvite();
    localTL_channels_exportInvite.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chatId);
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_exportInvite, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (paramAnonymousTL_error == null) {
              ChannelCreateActivity.access$2302(ChannelCreateActivity.this, (TLRPC.ExportedChatInvite)paramAnonymousTLObject);
            }
            ChannelCreateActivity.access$2402(ChannelCreateActivity.this, false);
            TextBlockCell localTextBlockCell = ChannelCreateActivity.this.privateContainer;
            if (ChannelCreateActivity.this.invite != null) {}
            for (String str = ChannelCreateActivity.this.invite.link;; str = LocaleController.getString("Loading", 2131493762))
            {
              localTextBlockCell.setText(str, false);
              return;
            }
          }
        });
      }
    });
  }
  
  private void loadAdminedChannels()
  {
    if (this.loadingAdminedChannels) {
      return;
    }
    this.loadingAdminedChannels = true;
    updatePrivatePublic();
    TLRPC.TL_channels_getAdminedPublicChannels localTL_channels_getAdminedPublicChannels = new TLRPC.TL_channels_getAdminedPublicChannels();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_getAdminedPublicChannels, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            ChannelCreateActivity.access$2602(ChannelCreateActivity.this, false);
            if ((paramAnonymousTLObject == null) || (ChannelCreateActivity.this.getParentActivity() == null)) {
              return;
            }
            int i = 0;
            while (i < ChannelCreateActivity.this.adminedChannelCells.size())
            {
              ChannelCreateActivity.this.linearLayout.removeView((View)ChannelCreateActivity.this.adminedChannelCells.get(i));
              i += 1;
            }
            ChannelCreateActivity.this.adminedChannelCells.clear();
            TLRPC.TL_messages_chats localTL_messages_chats = (TLRPC.TL_messages_chats)paramAnonymousTLObject;
            i = 0;
            if (i < localTL_messages_chats.chats.size())
            {
              AdminedChannelCell localAdminedChannelCell = new AdminedChannelCell(ChannelCreateActivity.this.getParentActivity(), new View.OnClickListener()
              {
                public void onClick(final View paramAnonymous3View)
                {
                  paramAnonymous3View = ((AdminedChannelCell)paramAnonymous3View.getParent()).getCurrentChannel();
                  AlertDialog.Builder localBuilder = new AlertDialog.Builder(ChannelCreateActivity.this.getParentActivity());
                  localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
                  if (paramAnonymous3View.megagroup) {
                    localBuilder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("RevokeLinkAlert", 2131494280, new Object[] { MessagesController.getInstance(ChannelCreateActivity.this.currentAccount).linkPrefix + "/" + paramAnonymous3View.username, paramAnonymous3View.title })));
                  }
                  for (;;)
                  {
                    localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                    localBuilder.setPositiveButton(LocaleController.getString("RevokeButton", 2131494278), new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface paramAnonymous4DialogInterface, int paramAnonymous4Int)
                      {
                        paramAnonymous4DialogInterface = new TLRPC.TL_channels_updateUsername();
                        paramAnonymous4DialogInterface.channel = MessagesController.getInputChannel(paramAnonymous3View);
                        paramAnonymous4DialogInterface.username = "";
                        ConnectionsManager.getInstance(ChannelCreateActivity.this.currentAccount).sendRequest(paramAnonymous4DialogInterface, new RequestDelegate()
                        {
                          public void run(TLObject paramAnonymous5TLObject, TLRPC.TL_error paramAnonymous5TL_error)
                          {
                            if ((paramAnonymous5TLObject instanceof TLRPC.TL_boolTrue)) {
                              AndroidUtilities.runOnUIThread(new Runnable()
                              {
                                public void run()
                                {
                                  ChannelCreateActivity.access$002(ChannelCreateActivity.this, true);
                                  if (ChannelCreateActivity.this.nameTextView.length() > 0) {
                                    ChannelCreateActivity.this.checkUserName(ChannelCreateActivity.this.nameTextView.getText().toString());
                                  }
                                  ChannelCreateActivity.this.updatePrivatePublic();
                                }
                              });
                            }
                          }
                        }, 64);
                      }
                    });
                    ChannelCreateActivity.this.showDialog(localBuilder.create());
                    return;
                    localBuilder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("RevokeLinkAlertChannel", 2131494281, new Object[] { MessagesController.getInstance(ChannelCreateActivity.this.currentAccount).linkPrefix + "/" + paramAnonymous3View.username, paramAnonymous3View.title })));
                  }
                }
              });
              TLRPC.Chat localChat = (TLRPC.Chat)localTL_messages_chats.chats.get(i);
              if (i == localTL_messages_chats.chats.size() - 1) {}
              for (boolean bool = true;; bool = false)
              {
                localAdminedChannelCell.setChannel(localChat, bool);
                ChannelCreateActivity.this.adminedChannelCells.add(localAdminedChannelCell);
                ChannelCreateActivity.this.adminnedChannelsLayout.addView(localAdminedChannelCell, LayoutHelper.createLinear(-1, 72));
                i += 1;
                break;
              }
            }
            ChannelCreateActivity.this.updatePrivatePublic();
          }
        });
      }
    });
  }
  
  private void showErrorAlert(String paramString)
  {
    if (getParentActivity() == null) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    int i = -1;
    switch (paramString.hashCode())
    {
    default: 
      switch (i)
      {
      default: 
        localBuilder.setMessage(LocaleController.getString("ErrorOccurred", 2131493453));
      }
      break;
    }
    for (;;)
    {
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
      showDialog(localBuilder.create());
      return;
      if (!paramString.equals("USERNAME_INVALID")) {
        break;
      }
      i = 0;
      break;
      if (!paramString.equals("USERNAME_OCCUPIED")) {
        break;
      }
      i = 1;
      break;
      localBuilder.setMessage(LocaleController.getString("LinkInvalid", 2131493751));
      continue;
      localBuilder.setMessage(LocaleController.getString("LinkInUse", 2131493749));
    }
  }
  
  private void updatePrivatePublic()
  {
    int j = 8;
    boolean bool = false;
    if (this.sectionCell == null) {
      return;
    }
    if ((!this.isPrivate) && (!this.canCreatePublic))
    {
      this.typeInfoCell.setText(LocaleController.getString("ChangePublicLimitReached", 2131493142));
      this.typeInfoCell.setTag("windowBackgroundWhiteRedText4");
      this.typeInfoCell.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
      this.linkContainer.setVisibility(8);
      this.sectionCell.setVisibility(8);
      if (this.loadingAdminedChannels)
      {
        this.loadingAdminedCell.setVisibility(0);
        this.adminnedChannelsLayout.setVisibility(8);
        this.typeInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(this.typeInfoCell.getContext(), 2131165332, "windowBackgroundGrayShadow"));
        this.adminedInfoCell.setVisibility(8);
      }
      for (;;)
      {
        localObject1 = this.radioButtonCell1;
        if (!this.isPrivate) {
          bool = true;
        }
        ((RadioButtonCell)localObject1).setChecked(bool, true);
        this.radioButtonCell2.setChecked(this.isPrivate, true);
        this.nameTextView.clearFocus();
        AndroidUtilities.hideKeyboard(this.nameTextView);
        return;
        this.typeInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(this.typeInfoCell.getContext(), 2131165331, "windowBackgroundGrayShadow"));
        this.loadingAdminedCell.setVisibility(8);
        this.adminnedChannelsLayout.setVisibility(0);
        this.adminedInfoCell.setVisibility(0);
      }
    }
    this.typeInfoCell.setTag("windowBackgroundWhiteGrayText4");
    this.typeInfoCell.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText4"));
    this.sectionCell.setVisibility(0);
    this.adminedInfoCell.setVisibility(8);
    this.adminnedChannelsLayout.setVisibility(8);
    this.typeInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(this.typeInfoCell.getContext(), 2131165332, "windowBackgroundGrayShadow"));
    this.linkContainer.setVisibility(0);
    this.loadingAdminedCell.setVisibility(8);
    Object localObject2 = this.typeInfoCell;
    label353:
    label384:
    int i;
    if (this.isPrivate)
    {
      localObject1 = LocaleController.getString("ChannelPrivateLinkHelp", 2131493199);
      ((TextInfoPrivacyCell)localObject2).setText((CharSequence)localObject1);
      localObject2 = this.headerCell;
      if (!this.isPrivate) {
        break label540;
      }
      localObject1 = LocaleController.getString("ChannelInviteLinkTitle", 2131493168);
      ((HeaderCell)localObject2).setText((String)localObject1);
      localObject1 = this.publicContainer;
      if (!this.isPrivate) {
        break label554;
      }
      i = 8;
      label407:
      ((LinearLayout)localObject1).setVisibility(i);
      localObject1 = this.privateContainer;
      if (!this.isPrivate) {
        break label559;
      }
      i = 0;
      label428:
      ((TextBlockCell)localObject1).setVisibility(i);
      localObject1 = this.linkContainer;
      if (!this.isPrivate) {
        break label565;
      }
      i = 0;
      label449:
      ((LinearLayout)localObject1).setPadding(0, 0, 0, i);
      localObject2 = this.privateContainer;
      if (this.invite == null) {
        break label575;
      }
    }
    label540:
    label554:
    label559:
    label565:
    label575:
    for (Object localObject1 = this.invite.link;; localObject1 = LocaleController.getString("Loading", 2131493762))
    {
      ((TextBlockCell)localObject2).setText((String)localObject1, false);
      localObject1 = this.checkTextView;
      i = j;
      if (!this.isPrivate)
      {
        i = j;
        if (this.checkTextView.length() != 0) {
          i = 0;
        }
      }
      ((TextView)localObject1).setVisibility(i);
      break;
      localObject1 = LocaleController.getString("ChannelUsernameHelp", 2131493217);
      break label353;
      localObject1 = LocaleController.getString("ChannelLinkTitle", 2131493175);
      break label384;
      i = 0;
      break label407;
      i = 8;
      break label428;
      i = AndroidUtilities.dp(7.0F);
      break label449;
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(final int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          ChannelCreateActivity.this.finishFragment();
        }
        do
        {
          do
          {
            do
            {
              return;
            } while (paramAnonymousInt != 1);
            if (ChannelCreateActivity.this.currentStep != 0) {
              break;
            }
          } while (ChannelCreateActivity.this.donePressed);
          if (ChannelCreateActivity.this.nameTextView.length() == 0)
          {
            localObject = (Vibrator)ChannelCreateActivity.this.getParentActivity().getSystemService("vibrator");
            if (localObject != null) {
              ((Vibrator)localObject).vibrate(200L);
            }
            AndroidUtilities.shakeView(ChannelCreateActivity.this.nameTextView, 2.0F, 0);
            return;
          }
          ChannelCreateActivity.access$202(ChannelCreateActivity.this, true);
          if (ChannelCreateActivity.this.avatarUpdater.uploadingAvatar != null)
          {
            ChannelCreateActivity.access$502(ChannelCreateActivity.this, true);
            ChannelCreateActivity.access$602(ChannelCreateActivity.this, new AlertDialog(ChannelCreateActivity.this.getParentActivity(), 1));
            ChannelCreateActivity.this.progressDialog.setMessage(LocaleController.getString("Loading", 2131493762));
            ChannelCreateActivity.this.progressDialog.setCanceledOnTouchOutside(false);
            ChannelCreateActivity.this.progressDialog.setCancelable(false);
            ChannelCreateActivity.this.progressDialog.setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                ChannelCreateActivity.access$502(ChannelCreateActivity.this, false);
                ChannelCreateActivity.access$602(ChannelCreateActivity.this, null);
                ChannelCreateActivity.access$202(ChannelCreateActivity.this, false);
                try
                {
                  paramAnonymous2DialogInterface.dismiss();
                  return;
                }
                catch (Exception paramAnonymous2DialogInterface)
                {
                  FileLog.e(paramAnonymous2DialogInterface);
                }
              }
            });
            ChannelCreateActivity.this.progressDialog.show();
            return;
          }
          paramAnonymousInt = MessagesController.getInstance(ChannelCreateActivity.this.currentAccount).createChat(ChannelCreateActivity.this.nameTextView.getText().toString(), new ArrayList(), ChannelCreateActivity.this.descriptionTextView.getText().toString(), 2, ChannelCreateActivity.this);
          ChannelCreateActivity.access$602(ChannelCreateActivity.this, new AlertDialog(ChannelCreateActivity.this.getParentActivity(), 1));
          ChannelCreateActivity.this.progressDialog.setMessage(LocaleController.getString("Loading", 2131493762));
          ChannelCreateActivity.this.progressDialog.setCanceledOnTouchOutside(false);
          ChannelCreateActivity.this.progressDialog.setCancelable(false);
          ChannelCreateActivity.this.progressDialog.setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              ConnectionsManager.getInstance(ChannelCreateActivity.this.currentAccount).cancelRequest(paramAnonymousInt, true);
              ChannelCreateActivity.access$202(ChannelCreateActivity.this, false);
              try
              {
                paramAnonymous2DialogInterface.dismiss();
                return;
              }
              catch (Exception paramAnonymous2DialogInterface)
              {
                FileLog.e(paramAnonymous2DialogInterface);
              }
            }
          });
          ChannelCreateActivity.this.progressDialog.show();
          return;
        } while (ChannelCreateActivity.this.currentStep != 1);
        if (!ChannelCreateActivity.this.isPrivate)
        {
          if (ChannelCreateActivity.this.nameTextView.length() == 0)
          {
            localObject = new AlertDialog.Builder(ChannelCreateActivity.this.getParentActivity());
            ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131492981));
            ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("ChannelPublicEmptyUsername", 2131493201));
            ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("Close", 2131493265), null);
            ChannelCreateActivity.this.showDialog(((AlertDialog.Builder)localObject).create());
            return;
          }
          if (!ChannelCreateActivity.this.lastNameAvailable)
          {
            localObject = (Vibrator)ChannelCreateActivity.this.getParentActivity().getSystemService("vibrator");
            if (localObject != null) {
              ((Vibrator)localObject).vibrate(200L);
            }
            AndroidUtilities.shakeView(ChannelCreateActivity.this.checkTextView, 2.0F, 0);
            return;
          }
          MessagesController.getInstance(ChannelCreateActivity.this.currentAccount).updateChannelUserName(ChannelCreateActivity.this.chatId, ChannelCreateActivity.this.lastCheckName);
        }
        Object localObject = new Bundle();
        ((Bundle)localObject).putInt("step", 2);
        ((Bundle)localObject).putInt("chatId", ChannelCreateActivity.this.chatId);
        ((Bundle)localObject).putInt("chatType", 2);
        ChannelCreateActivity.this.presentFragment(new GroupCreateActivity((Bundle)localObject), true);
      }
    });
    this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    this.fragmentView = new ScrollView(paramContext);
    Object localObject1 = (ScrollView)this.fragmentView;
    ((ScrollView)localObject1).setFillViewport(true);
    this.linearLayout = new LinearLayout(paramContext);
    this.linearLayout.setOrientation(1);
    ((ScrollView)localObject1).addView(this.linearLayout, new FrameLayout.LayoutParams(-1, -2));
    if (this.currentStep == 0)
    {
      this.actionBar.setTitle(LocaleController.getString("NewChannel", 2131493867));
      this.fragmentView.setTag("windowBackgroundWhite");
      this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      localObject1 = new FrameLayout(paramContext);
      this.linearLayout.addView((View)localObject1, LayoutHelper.createLinear(-1, -2));
      this.avatarImage = new BackupImageView(paramContext);
      this.avatarImage.setRoundRadius(AndroidUtilities.dp(32.0F));
      this.avatarDrawable.setInfo(5, null, null, false);
      this.avatarDrawable.setDrawPhoto(true);
      this.avatarImage.setImageDrawable(this.avatarDrawable);
      localObject2 = this.avatarImage;
      if (LocaleController.isRTL)
      {
        i = 5;
        if (!LocaleController.isRTL) {
          break label998;
        }
        f1 = 0.0F;
        if (!LocaleController.isRTL) {
          break label1005;
        }
        f2 = 16.0F;
        ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(64, 64.0F, i | 0x30, f1, 12.0F, f2, 12.0F));
        this.avatarImage.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (ChannelCreateActivity.this.getParentActivity() == null) {
              return;
            }
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(ChannelCreateActivity.this.getParentActivity());
            if (ChannelCreateActivity.this.avatar != null)
            {
              paramAnonymousView = new CharSequence[3];
              paramAnonymousView[0] = LocaleController.getString("FromCamera", 2131493613);
              paramAnonymousView[1] = LocaleController.getString("FromGalley", 2131493621);
              paramAnonymousView[2] = LocaleController.getString("DeletePhoto", 2131493374);
            }
            for (;;)
            {
              localBuilder.setItems(paramAnonymousView, new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                {
                  if (paramAnonymous2Int == 0) {
                    ChannelCreateActivity.this.avatarUpdater.openCamera();
                  }
                  do
                  {
                    return;
                    if (paramAnonymous2Int == 1)
                    {
                      ChannelCreateActivity.this.avatarUpdater.openGallery();
                      return;
                    }
                  } while (paramAnonymous2Int != 2);
                  ChannelCreateActivity.access$1602(ChannelCreateActivity.this, null);
                  ChannelCreateActivity.access$1702(ChannelCreateActivity.this, null);
                  ChannelCreateActivity.this.avatarImage.setImage(ChannelCreateActivity.this.avatar, "50_50", ChannelCreateActivity.this.avatarDrawable);
                }
              });
              ChannelCreateActivity.this.showDialog(localBuilder.create());
              return;
              paramAnonymousView = new CharSequence[2];
              paramAnonymousView[0] = LocaleController.getString("FromCamera", 2131493613);
              paramAnonymousView[1] = LocaleController.getString("FromGalley", 2131493621);
            }
          }
        });
        this.nameTextView = new EditTextBoldCursor(paramContext);
        this.nameTextView.setHint(LocaleController.getString("EnterChannelName", 2131493445));
        if (this.nameToSet != null)
        {
          this.nameTextView.setText(this.nameToSet);
          this.nameToSet = null;
        }
        this.nameTextView.setMaxLines(4);
        localObject2 = this.nameTextView;
        if (!LocaleController.isRTL) {
          break label1010;
        }
        i = 5;
        ((EditTextBoldCursor)localObject2).setGravity(i | 0x10);
        this.nameTextView.setTextSize(1, 16.0F);
        this.nameTextView.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
        this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.nameTextView.setImeOptions(268435456);
        this.nameTextView.setInputType(16385);
        this.nameTextView.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        localObject2 = new InputFilter.LengthFilter(100);
        this.nameTextView.setFilters(new InputFilter[] { localObject2 });
        this.nameTextView.setPadding(0, 0, 0, AndroidUtilities.dp(8.0F));
        this.nameTextView.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.nameTextView.setCursorSize(AndroidUtilities.dp(20.0F));
        this.nameTextView.setCursorWidth(1.5F);
        localObject2 = this.nameTextView;
        if (!LocaleController.isRTL) {
          break label1016;
        }
        f1 = 16.0F;
        if (!LocaleController.isRTL) {
          break label1023;
        }
        f2 = 96.0F;
        ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(-1, -2.0F, 16, f1, 0.0F, f2, 0.0F));
        this.nameTextView.addTextChangedListener(new TextWatcher()
        {
          public void afterTextChanged(Editable paramAnonymousEditable)
          {
            AvatarDrawable localAvatarDrawable = ChannelCreateActivity.this.avatarDrawable;
            if (ChannelCreateActivity.this.nameTextView.length() > 0) {}
            for (paramAnonymousEditable = ChannelCreateActivity.this.nameTextView.getText().toString();; paramAnonymousEditable = null)
            {
              localAvatarDrawable.setInfo(5, paramAnonymousEditable, null, false);
              ChannelCreateActivity.this.avatarImage.invalidate();
              return;
            }
          }
          
          public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
          
          public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        });
        this.descriptionTextView = new EditTextBoldCursor(paramContext);
        this.descriptionTextView.setTextSize(1, 18.0F);
        this.descriptionTextView.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
        this.descriptionTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.descriptionTextView.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        this.descriptionTextView.setPadding(0, 0, 0, AndroidUtilities.dp(6.0F));
        localObject1 = this.descriptionTextView;
        if (!LocaleController.isRTL) {
          break label1030;
        }
        i = 5;
        ((EditTextBoldCursor)localObject1).setGravity(i);
        this.descriptionTextView.setInputType(180225);
        this.descriptionTextView.setImeOptions(6);
        localObject1 = new InputFilter.LengthFilter(120);
        this.descriptionTextView.setFilters(new InputFilter[] { localObject1 });
        this.descriptionTextView.setHint(LocaleController.getString("DescriptionPlaceholder", 2131493381));
        this.descriptionTextView.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.descriptionTextView.setCursorSize(AndroidUtilities.dp(20.0F));
        this.descriptionTextView.setCursorWidth(1.5F);
        this.linearLayout.addView(this.descriptionTextView, LayoutHelper.createLinear(-1, -2, 24.0F, 18.0F, 24.0F, 0.0F));
        this.descriptionTextView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
          {
            if ((paramAnonymousInt == 6) && (ChannelCreateActivity.this.doneButton != null))
            {
              ChannelCreateActivity.this.doneButton.performClick();
              return true;
            }
            return false;
          }
        });
        this.descriptionTextView.addTextChangedListener(new TextWatcher()
        {
          public void afterTextChanged(Editable paramAnonymousEditable) {}
          
          public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
          
          public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        });
        this.helpTextView = new TextView(paramContext);
        this.helpTextView.setTextSize(1, 15.0F);
        this.helpTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText8"));
        paramContext = this.helpTextView;
        if (!LocaleController.isRTL) {
          break label1036;
        }
        i = 5;
        paramContext.setGravity(i);
        this.helpTextView.setText(LocaleController.getString("DescriptionInfo", 2131493379));
        paramContext = this.linearLayout;
        localObject1 = this.helpTextView;
        if (!LocaleController.isRTL) {
          break label1042;
        }
        i = 5;
        paramContext.addView((View)localObject1, LayoutHelper.createLinear(-2, -2, i, 24, 10, 24, 20));
      }
    }
    label998:
    label1005:
    label1010:
    label1016:
    label1023:
    label1030:
    label1036:
    label1042:
    while (this.currentStep != 1) {
      for (;;)
      {
        return this.fragmentView;
        i = 3;
        continue;
        float f1 = 16.0F;
        continue;
        float f2 = 0.0F;
        continue;
        i = 3;
        continue;
        f1 = 96.0F;
        continue;
        f2 = 16.0F;
        continue;
        i = 3;
        continue;
        i = 3;
        continue;
        i = 3;
      }
    }
    this.actionBar.setTitle(LocaleController.getString("ChannelSettings", 2131493206));
    this.fragmentView.setTag("windowBackgroundGray");
    this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.linearLayout2 = new LinearLayout(paramContext);
    this.linearLayout2.setOrientation(1);
    this.linearLayout2.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
    this.linearLayout.addView(this.linearLayout2, LayoutHelper.createLinear(-1, -2));
    this.radioButtonCell1 = new RadioButtonCell(paramContext);
    this.radioButtonCell1.setBackgroundDrawable(Theme.getSelectorDrawable(false));
    localObject1 = this.radioButtonCell1;
    Object localObject2 = LocaleController.getString("ChannelPublic", 2131493200);
    String str = LocaleController.getString("ChannelPublicInfo", 2131493202);
    boolean bool;
    if (!this.isPrivate)
    {
      bool = true;
      label1206:
      ((RadioButtonCell)localObject1).setTextAndValue((String)localObject2, str, bool);
      this.linearLayout2.addView(this.radioButtonCell1, LayoutHelper.createLinear(-1, -2));
      this.radioButtonCell1.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (!ChannelCreateActivity.this.isPrivate) {
            return;
          }
          ChannelCreateActivity.access$1002(ChannelCreateActivity.this, false);
          ChannelCreateActivity.this.updatePrivatePublic();
        }
      });
      this.radioButtonCell2 = new RadioButtonCell(paramContext);
      this.radioButtonCell2.setBackgroundDrawable(Theme.getSelectorDrawable(false));
      this.radioButtonCell2.setTextAndValue(LocaleController.getString("ChannelPrivate", 2131493197), LocaleController.getString("ChannelPrivateInfo", 2131493198), this.isPrivate);
      this.linearLayout2.addView(this.radioButtonCell2, LayoutHelper.createLinear(-1, -2));
      this.radioButtonCell2.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ChannelCreateActivity.this.isPrivate) {
            return;
          }
          ChannelCreateActivity.access$1002(ChannelCreateActivity.this, true);
          ChannelCreateActivity.this.updatePrivatePublic();
        }
      });
      this.sectionCell = new ShadowSectionCell(paramContext);
      this.linearLayout.addView(this.sectionCell, LayoutHelper.createLinear(-1, -2));
      this.linkContainer = new LinearLayout(paramContext);
      this.linkContainer.setOrientation(1);
      this.linkContainer.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      this.linearLayout.addView(this.linkContainer, LayoutHelper.createLinear(-1, -2));
      this.headerCell = new HeaderCell(paramContext);
      this.linkContainer.addView(this.headerCell);
      this.publicContainer = new LinearLayout(paramContext);
      this.publicContainer.setOrientation(0);
      this.linkContainer.addView(this.publicContainer, LayoutHelper.createLinear(-1, 36, 17.0F, 7.0F, 17.0F, 0.0F));
      this.editText = new EditText(paramContext);
      this.editText.setText(MessagesController.getInstance(this.currentAccount).linkPrefix + "/");
      this.editText.setTextSize(1, 18.0F);
      this.editText.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
      this.editText.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.editText.setMaxLines(1);
      this.editText.setLines(1);
      this.editText.setEnabled(false);
      this.editText.setBackgroundDrawable(null);
      this.editText.setPadding(0, 0, 0, 0);
      this.editText.setSingleLine(true);
      this.editText.setInputType(163840);
      this.editText.setImeOptions(6);
      this.publicContainer.addView(this.editText, LayoutHelper.createLinear(-2, 36));
      this.nameTextView = new EditTextBoldCursor(paramContext);
      this.nameTextView.setTextSize(1, 18.0F);
      this.nameTextView.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
      this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setMaxLines(1);
      this.nameTextView.setLines(1);
      this.nameTextView.setBackgroundDrawable(null);
      this.nameTextView.setPadding(0, 0, 0, 0);
      this.nameTextView.setSingleLine(true);
      this.nameTextView.setInputType(163872);
      this.nameTextView.setImeOptions(6);
      this.nameTextView.setHint(LocaleController.getString("ChannelUsernamePlaceholder", 2131493218));
      this.nameTextView.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setCursorSize(AndroidUtilities.dp(20.0F));
      this.nameTextView.setCursorWidth(1.5F);
      this.publicContainer.addView(this.nameTextView, LayoutHelper.createLinear(-1, 36));
      this.nameTextView.addTextChangedListener(new TextWatcher()
      {
        public void afterTextChanged(Editable paramAnonymousEditable) {}
        
        public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
        {
          ChannelCreateActivity.this.checkUserName(ChannelCreateActivity.this.nameTextView.getText().toString());
        }
      });
      this.privateContainer = new TextBlockCell(paramContext);
      this.privateContainer.setBackgroundDrawable(Theme.getSelectorDrawable(false));
      this.linkContainer.addView(this.privateContainer);
      this.privateContainer.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ChannelCreateActivity.this.invite == null) {
            return;
          }
          try
          {
            ((ClipboardManager)ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", ChannelCreateActivity.this.invite.link));
            Toast.makeText(ChannelCreateActivity.this.getParentActivity(), LocaleController.getString("LinkCopied", 2131493748), 0).show();
            return;
          }
          catch (Exception paramAnonymousView)
          {
            FileLog.e(paramAnonymousView);
          }
        }
      });
      this.checkTextView = new TextView(paramContext);
      this.checkTextView.setTextSize(1, 15.0F);
      localObject1 = this.checkTextView;
      if (!LocaleController.isRTL) {
        break label2181;
      }
      i = 5;
      label1937:
      ((TextView)localObject1).setGravity(i);
      this.checkTextView.setVisibility(8);
      localObject1 = this.linkContainer;
      localObject2 = this.checkTextView;
      if (!LocaleController.isRTL) {
        break label2187;
      }
    }
    label2181:
    label2187:
    for (int i = 5;; i = 3)
    {
      ((LinearLayout)localObject1).addView((View)localObject2, LayoutHelper.createLinear(-2, -2, i, 17, 3, 17, 7));
      this.typeInfoCell = new TextInfoPrivacyCell(paramContext);
      this.typeInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(paramContext, 2131165332, "windowBackgroundGrayShadow"));
      this.linearLayout.addView(this.typeInfoCell, LayoutHelper.createLinear(-1, -2));
      this.loadingAdminedCell = new LoadingCell(paramContext);
      this.linearLayout.addView(this.loadingAdminedCell, LayoutHelper.createLinear(-1, -2));
      this.adminnedChannelsLayout = new LinearLayout(paramContext);
      this.adminnedChannelsLayout.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      this.adminnedChannelsLayout.setOrientation(1);
      this.linearLayout.addView(this.adminnedChannelsLayout, LayoutHelper.createLinear(-1, -2));
      this.adminedInfoCell = new TextInfoPrivacyCell(paramContext);
      this.adminedInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(paramContext, 2131165332, "windowBackgroundGrayShadow"));
      this.linearLayout.addView(this.adminedInfoCell, LayoutHelper.createLinear(-1, -2));
      updatePrivatePublic();
      break;
      bool = false;
      break label1206;
      i = 3;
      break label1937;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.chatDidFailCreate) {
      if (this.progressDialog == null) {}
    }
    while (paramInt1 != NotificationCenter.chatDidCreated) {
      try
      {
        this.progressDialog.dismiss();
        this.donePressed = false;
        return;
      }
      catch (Exception paramVarArgs)
      {
        for (;;)
        {
          FileLog.e(paramVarArgs);
        }
      }
    }
    if (this.progressDialog != null) {}
    try
    {
      this.progressDialog.dismiss();
      paramInt1 = ((Integer)paramVarArgs[0]).intValue();
      paramVarArgs = new Bundle();
      paramVarArgs.putInt("step", 1);
      paramVarArgs.putInt("chat_id", paramInt1);
      paramVarArgs.putBoolean("canCreatePublic", this.canCreatePublic);
      if (this.uploadedAvatar != null) {
        MessagesController.getInstance(this.currentAccount).changeChatAvatar(paramInt1, this.uploadedAvatar);
      }
      presentFragment(new ChannelCreateActivity(paramVarArgs), true);
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
  
  public void didUploadedPhoto(final TLRPC.InputFile paramInputFile, final TLRPC.PhotoSize paramPhotoSize1, TLRPC.PhotoSize paramPhotoSize2)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        ChannelCreateActivity.access$1702(ChannelCreateActivity.this, paramInputFile);
        ChannelCreateActivity.access$1602(ChannelCreateActivity.this, paramPhotoSize1.location);
        ChannelCreateActivity.this.avatarImage.setImage(ChannelCreateActivity.this.avatar, "50_50", ChannelCreateActivity.this.avatarDrawable);
        if (ChannelCreateActivity.this.createAfterUpload) {}
        try
        {
          if ((ChannelCreateActivity.this.progressDialog != null) && (ChannelCreateActivity.this.progressDialog.isShowing()))
          {
            ChannelCreateActivity.this.progressDialog.dismiss();
            ChannelCreateActivity.access$602(ChannelCreateActivity.this, null);
          }
          ChannelCreateActivity.access$202(ChannelCreateActivity.this, false);
          ChannelCreateActivity.this.doneButton.performClick();
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
    });
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local15 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (ChannelCreateActivity.this.adminnedChannelsLayout != null)
        {
          int j = ChannelCreateActivity.this.adminnedChannelsLayout.getChildCount();
          int i = 0;
          while (i < j)
          {
            localObject = ChannelCreateActivity.this.adminnedChannelsLayout.getChildAt(i);
            if ((localObject instanceof AdminedChannelCell)) {
              ((AdminedChannelCell)localObject).update();
            }
            i += 1;
          }
        }
        AvatarDrawable localAvatarDrawable;
        if (ChannelCreateActivity.this.avatarImage != null)
        {
          localAvatarDrawable = ChannelCreateActivity.this.avatarDrawable;
          if (ChannelCreateActivity.this.nameTextView.length() <= 0) {
            break label127;
          }
        }
        label127:
        for (Object localObject = ChannelCreateActivity.this.nameTextView.getText().toString();; localObject = null)
        {
          localAvatarDrawable.setInfo(5, (String)localObject, null, false);
          ChannelCreateActivity.this.avatarImage.invalidate();
          return;
        }
      }
    };
    return new ThemeDescription[] { new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundGray"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(this.nameTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.nameTextView, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.nameTextView, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.nameTextView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(this.descriptionTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.descriptionTextView, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.descriptionTextView, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.descriptionTextView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(this.helpTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText8"), new ThemeDescription(this.linearLayout2, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.linkContainer, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.sectionCell, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.headerCell, 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader"), new ThemeDescription(this.editText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.editText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhiteRedText4"), new ThemeDescription(this.checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhiteGrayText8"), new ThemeDescription(this.checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhiteGreenText"), new ThemeDescription(this.typeInfoCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.typeInfoCell, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.typeInfoCell, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteRedText4"), new ThemeDescription(this.adminedInfoCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.adminnedChannelsLayout, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.privateContainer, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21"), new ThemeDescription(this.privateContainer, 0, new Class[] { TextBlockCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.loadingAdminedCell, 0, new Class[] { LoadingCell.class }, new String[] { "progressBar" }, null, null, null, "progressCircle"), new ThemeDescription(this.radioButtonCell1, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21"), new ThemeDescription(this.radioButtonCell1, ThemeDescription.FLAG_CHECKBOX, new Class[] { RadioButtonCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackground"), new ThemeDescription(this.radioButtonCell1, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { RadioButtonCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackgroundChecked"), new ThemeDescription(this.radioButtonCell1, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { RadioButtonCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.radioButtonCell1, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { RadioButtonCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.radioButtonCell2, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21"), new ThemeDescription(this.radioButtonCell2, ThemeDescription.FLAG_CHECKBOX, new Class[] { RadioButtonCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackground"), new ThemeDescription(this.radioButtonCell2, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { RadioButtonCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackgroundChecked"), new ThemeDescription(this.radioButtonCell2, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { RadioButtonCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.radioButtonCell2, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { RadioButtonCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.adminnedChannelsLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { AdminedChannelCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.adminnedChannelsLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { AdminedChannelCell.class }, new String[] { "statusTextView" }, null, null, null, "windowBackgroundWhiteGrayText"), new ThemeDescription(this.adminnedChannelsLayout, ThemeDescription.FLAG_LINKCOLOR, new Class[] { AdminedChannelCell.class }, new String[] { "statusTextView" }, null, null, null, "windowBackgroundWhiteLinkText"), new ThemeDescription(this.adminnedChannelsLayout, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { AdminedChannelCell.class }, new String[] { "deleteButton" }, null, null, null, "windowBackgroundWhiteGrayText"), new ThemeDescription(null, 0, null, null, new Drawable[] { Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable }, local15, "avatar_text"), new ThemeDescription(null, 0, null, null, null, local15, "avatar_backgroundRed"), new ThemeDescription(null, 0, null, null, null, local15, "avatar_backgroundOrange"), new ThemeDescription(null, 0, null, null, null, local15, "avatar_backgroundViolet"), new ThemeDescription(null, 0, null, null, null, local15, "avatar_backgroundGreen"), new ThemeDescription(null, 0, null, null, null, local15, "avatar_backgroundCyan"), new ThemeDescription(null, 0, null, null, null, local15, "avatar_backgroundBlue"), new ThemeDescription(null, 0, null, null, null, local15, "avatar_backgroundPink") };
  }
  
  public void onActivityResultFragment(int paramInt1, int paramInt2, Intent paramIntent)
  {
    this.avatarUpdater.onActivityResult(paramInt1, paramInt2, paramIntent);
  }
  
  public boolean onFragmentCreate()
  {
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatDidCreated);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatDidFailCreate);
    if (this.currentStep == 1) {
      generateLink();
    }
    if (this.avatarUpdater != null)
    {
      this.avatarUpdater.parentFragment = this;
      this.avatarUpdater.delegate = this;
    }
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatDidCreated);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatDidFailCreate);
    if (this.avatarUpdater != null) {
      this.avatarUpdater.clear();
    }
    AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
  }
  
  public void onResume()
  {
    super.onResume();
    AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
  }
  
  public void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean1) && (this.currentStep != 1))
    {
      this.nameTextView.requestFocus();
      AndroidUtilities.showKeyboard(this.nameTextView);
    }
  }
  
  public void restoreSelfArgs(Bundle paramBundle)
  {
    if (this.currentStep == 0)
    {
      if (this.avatarUpdater != null) {
        this.avatarUpdater.currentPicturePath = paramBundle.getString("path");
      }
      paramBundle = paramBundle.getString("nameTextView");
      if (paramBundle != null)
      {
        if (this.nameTextView == null) {
          break label56;
        }
        this.nameTextView.setText(paramBundle);
      }
    }
    return;
    label56:
    this.nameToSet = paramBundle;
  }
  
  public void saveSelfArgs(Bundle paramBundle)
  {
    if (this.currentStep == 0)
    {
      if ((this.avatarUpdater != null) && (this.avatarUpdater.currentPicturePath != null)) {
        paramBundle.putString("path", this.avatarUpdater.currentPicturePath);
      }
      if (this.nameTextView != null)
      {
        String str = this.nameTextView.getText().toString();
        if ((str != null) && (str.length() != 0)) {
          paramBundle.putString("nameTextView", str);
        }
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ChannelCreateActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */