package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SecretChatHelper;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_channelAdminRights;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_channelParticipant;
import org.telegram.tgnet.TLRPC.TL_channelParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_channelParticipantBanned;
import org.telegram.tgnet.TLRPC.TL_channelParticipantCreator;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipant;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipant;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_chatChannelParticipant;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_chatParticipantCreator;
import org.telegram.tgnet.TLRPC.TL_chatParticipants;
import org.telegram.tgnet.TLRPC.TL_chatParticipantsForbidden;
import org.telegram.tgnet.TLRPC.TL_chatPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_userEmpty;
import org.telegram.tgnet.TLRPC.TL_userFull;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.AboutLinkCell.AboutLinkCellDelegate;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.AvatarUpdater.AvatarUpdaterDelegate;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.IdenticonDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.voip.VoIPHelper;

public class ProfileActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate, DialogsActivity.DialogsActivityDelegate
{
  private static final int add_contact = 1;
  private static final int add_shortcut = 14;
  private static final int block_contact = 2;
  private static final int call_item = 15;
  private static final int convert_to_supergroup = 13;
  private static final int delete_contact = 5;
  private static final int edit_channel = 12;
  private static final int edit_contact = 4;
  private static final int edit_name = 8;
  private static final int invite_to_group = 9;
  private static final int leave_group = 7;
  private static final int search_members = 16;
  private static final int set_admins = 11;
  private static final int share = 10;
  private static final int share_contact = 3;
  private int addMemberRow;
  private boolean allowProfileAnimation = true;
  private ActionBarMenuItem animatingItem;
  private float animationProgress;
  private AvatarDrawable avatarDrawable;
  private BackupImageView avatarImage;
  private AvatarUpdater avatarUpdater;
  private int banFromGroup;
  private TLRPC.BotInfo botInfo;
  private ActionBarMenuItem callItem;
  private int channelInfoRow;
  private int channelNameRow;
  private int chat_id;
  private int convertHelpRow;
  private int convertRow;
  private boolean creatingChat;
  private TLRPC.ChannelParticipant currentChannelParticipant;
  private TLRPC.Chat currentChat;
  private TLRPC.EncryptedChat currentEncryptedChat;
  private long dialog_id;
  private ActionBarMenuItem editItem;
  private int emptyRow;
  private int emptyRowChat;
  private int emptyRowChat2;
  private int extraHeight;
  private int groupsInCommonRow;
  private TLRPC.ChatFull info;
  private int initialAnimationExtraHeight;
  private boolean isBot;
  private LinearLayoutManager layoutManager;
  private int leaveChannelRow;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private int loadMoreMembersRow;
  private boolean loadingUsers;
  private int membersEndRow;
  private int membersRow;
  private int membersSectionRow;
  private long mergeDialogId;
  private SimpleTextView[] nameTextView = new SimpleTextView[2];
  private int onlineCount = -1;
  private SimpleTextView[] onlineTextView = new SimpleTextView[2];
  private boolean openAnimationInProgress;
  private SparseArray<TLRPC.ChatParticipant> participantsMap = new SparseArray();
  private int phoneRow;
  private boolean playProfileAnimation;
  private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider()
  {
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject paramAnonymousMessageObject, TLRPC.FileLocation paramAnonymousFileLocation, int paramAnonymousInt)
    {
      paramAnonymousInt = 0;
      if (paramAnonymousFileLocation == null) {
        return null;
      }
      Object localObject1 = null;
      Object localObject2;
      if (ProfileActivity.this.user_id != 0)
      {
        localObject2 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
        paramAnonymousMessageObject = (MessageObject)localObject1;
        if (localObject2 != null)
        {
          paramAnonymousMessageObject = (MessageObject)localObject1;
          if (((TLRPC.User)localObject2).photo != null)
          {
            paramAnonymousMessageObject = (MessageObject)localObject1;
            if (((TLRPC.User)localObject2).photo.photo_big != null) {
              paramAnonymousMessageObject = ((TLRPC.User)localObject2).photo.photo_big;
            }
          }
        }
        label88:
        if ((paramAnonymousMessageObject == null) || (paramAnonymousMessageObject.local_id != paramAnonymousFileLocation.local_id) || (paramAnonymousMessageObject.volume_id != paramAnonymousFileLocation.volume_id) || (paramAnonymousMessageObject.dc_id != paramAnonymousFileLocation.dc_id)) {
          break label353;
        }
        paramAnonymousMessageObject = new int[2];
        ProfileActivity.this.avatarImage.getLocationInWindow(paramAnonymousMessageObject);
        paramAnonymousFileLocation = new PhotoViewer.PlaceProviderObject();
        paramAnonymousFileLocation.viewX = paramAnonymousMessageObject[0];
        int i = paramAnonymousMessageObject[1];
        if (Build.VERSION.SDK_INT < 21) {
          break label355;
        }
        label169:
        paramAnonymousFileLocation.viewY = (i - paramAnonymousInt);
        paramAnonymousFileLocation.parentView = ProfileActivity.this.avatarImage;
        paramAnonymousFileLocation.imageReceiver = ProfileActivity.this.avatarImage.getImageReceiver();
        if (ProfileActivity.this.user_id == 0) {
          break label362;
        }
        paramAnonymousFileLocation.dialogId = ProfileActivity.this.user_id;
      }
      for (;;)
      {
        paramAnonymousFileLocation.thumb = paramAnonymousFileLocation.imageReceiver.getBitmapSafe();
        paramAnonymousFileLocation.size = -1;
        paramAnonymousFileLocation.radius = ProfileActivity.this.avatarImage.getImageReceiver().getRoundRadius();
        paramAnonymousFileLocation.scale = ProfileActivity.this.avatarImage.getScaleX();
        return paramAnonymousFileLocation;
        paramAnonymousMessageObject = (MessageObject)localObject1;
        if (ProfileActivity.this.chat_id == 0) {
          break label88;
        }
        localObject2 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getChat(Integer.valueOf(ProfileActivity.this.chat_id));
        paramAnonymousMessageObject = (MessageObject)localObject1;
        if (localObject2 == null) {
          break label88;
        }
        paramAnonymousMessageObject = (MessageObject)localObject1;
        if (((TLRPC.Chat)localObject2).photo == null) {
          break label88;
        }
        paramAnonymousMessageObject = (MessageObject)localObject1;
        if (((TLRPC.Chat)localObject2).photo.photo_big == null) {
          break label88;
        }
        paramAnonymousMessageObject = ((TLRPC.Chat)localObject2).photo.photo_big;
        break label88;
        label353:
        break;
        label355:
        paramAnonymousInt = AndroidUtilities.statusBarHeight;
        break label169;
        label362:
        if (ProfileActivity.this.chat_id != 0) {
          paramAnonymousFileLocation.dialogId = (-ProfileActivity.this.chat_id);
        }
      }
    }
    
    public void willHidePhotoViewer()
    {
      ProfileActivity.this.avatarImage.getImageReceiver().setVisible(true, true);
    }
  };
  private boolean recreateMenuAfterAnimation;
  private int rowCount = 0;
  private int sectionRow;
  private int selectedUser;
  private int settingsKeyRow;
  private int settingsNotificationsRow;
  private int settingsTimerRow;
  private int sharedMediaRow;
  private ArrayList<Integer> sortedUsers;
  private int startSecretChatRow;
  private TopView topView;
  private int totalMediaCount = -1;
  private int totalMediaCountMerge = -1;
  private boolean userBlocked;
  private int userInfoDetailedRow;
  private int userInfoRow;
  private int userSectionRow;
  private int user_id;
  private int usernameRow;
  private boolean usersEndReached;
  private ImageView writeButton;
  private AnimatorSet writeButtonAnimation;
  
  public ProfileActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  private void checkListViewScroll()
  {
    boolean bool = false;
    if ((this.listView.getChildCount() <= 0) || (this.openAnimationInProgress)) {}
    int i;
    do
    {
      return;
      View localView = this.listView.getChildAt(0);
      RecyclerListView.Holder localHolder = (RecyclerListView.Holder)this.listView.findContainingViewHolder(localView);
      int j = localView.getTop();
      int k = 0;
      i = k;
      if (j >= 0)
      {
        i = k;
        if (localHolder != null)
        {
          i = k;
          if (localHolder.getAdapterPosition() == 0) {
            i = j;
          }
        }
      }
    } while (this.extraHeight == i);
    this.extraHeight = i;
    this.topView.invalidate();
    if (this.playProfileAnimation)
    {
      if (this.extraHeight != 0) {
        bool = true;
      }
      this.allowProfileAnimation = bool;
    }
    needLayout();
  }
  
  private void createActionBarMenu()
  {
    ActionBarMenu localActionBarMenu = this.actionBar.createMenu();
    localActionBarMenu.clearItems();
    this.animatingItem = null;
    Object localObject3 = null;
    Object localObject2 = null;
    Object localObject1 = null;
    if (this.user_id != 0) {
      if (UserConfig.getInstance(this.currentAccount).getClientUserId() != this.user_id)
      {
        localObject1 = MessagesController.getInstance(this.currentAccount).getUserFull(this.user_id);
        if ((localObject1 != null) && (((TLRPC.TL_userFull)localObject1).phone_calls_available)) {
          this.callItem = localActionBarMenu.addItem(15, 2131165372);
        }
        if (ContactsController.getInstance(this.currentAccount).contactsDict.get(Integer.valueOf(this.user_id)) == null)
        {
          localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
          if (localObject1 == null) {
            return;
          }
          localObject2 = localActionBarMenu.addItem(10, 2131165353);
          if (this.isBot)
          {
            if (!((TLRPC.User)localObject1).bot_nochats) {
              ((ActionBarMenuItem)localObject2).addSubItem(9, LocaleController.getString("BotInvite", 2131493090));
            }
            ((ActionBarMenuItem)localObject2).addSubItem(10, LocaleController.getString("BotShare", 2131493094));
          }
          if ((((TLRPC.User)localObject1).phone != null) && (((TLRPC.User)localObject1).phone.length() != 0))
          {
            ((ActionBarMenuItem)localObject2).addSubItem(1, LocaleController.getString("AddContact", 2131492926));
            ((ActionBarMenuItem)localObject2).addSubItem(3, LocaleController.getString("ShareContact", 2131494382));
            if (!this.userBlocked)
            {
              localObject1 = LocaleController.getString("BlockContact", 2131493080);
              ((ActionBarMenuItem)localObject2).addSubItem(2, (String)localObject1);
              localObject1 = localObject2;
            }
          }
        }
      }
    }
    for (;;)
    {
      localObject2 = localObject1;
      if (localObject1 == null) {
        localObject2 = localActionBarMenu.addItem(10, 2131165353);
      }
      ((ActionBarMenuItem)localObject2).addSubItem(14, LocaleController.getString("AddShortcut", 2131492935));
      return;
      localObject1 = LocaleController.getString("Unblock", 2131494506);
      break;
      if (this.isBot)
      {
        if (!this.userBlocked) {}
        for (localObject1 = LocaleController.getString("BotStop", 2131493098);; localObject1 = LocaleController.getString("BotRestart", 2131493092))
        {
          ((ActionBarMenuItem)localObject2).addSubItem(2, (String)localObject1);
          localObject1 = localObject2;
          break;
        }
      }
      if (!this.userBlocked) {}
      for (localObject1 = LocaleController.getString("BlockContact", 2131493080);; localObject1 = LocaleController.getString("Unblock", 2131494506))
      {
        ((ActionBarMenuItem)localObject2).addSubItem(2, (String)localObject1);
        localObject1 = localObject2;
        break;
      }
      localObject2 = localActionBarMenu.addItem(10, 2131165353);
      ((ActionBarMenuItem)localObject2).addSubItem(3, LocaleController.getString("ShareContact", 2131494382));
      if (!this.userBlocked) {}
      for (localObject1 = LocaleController.getString("BlockContact", 2131493080);; localObject1 = LocaleController.getString("Unblock", 2131494506))
      {
        ((ActionBarMenuItem)localObject2).addSubItem(2, (String)localObject1);
        ((ActionBarMenuItem)localObject2).addSubItem(4, LocaleController.getString("EditContact", 2131493414));
        ((ActionBarMenuItem)localObject2).addSubItem(5, LocaleController.getString("DeleteContact", 2131493366));
        localObject1 = localObject2;
        break;
      }
      localObject1 = localActionBarMenu.addItem(10, 2131165353);
      ((ActionBarMenuItem)localObject1).addSubItem(3, LocaleController.getString("ShareContact", 2131494382));
      continue;
      if (this.chat_id != 0) {
        if (this.chat_id > 0)
        {
          TLRPC.Chat localChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
          if (this.writeButton != null)
          {
            boolean bool = ChatObject.isChannel(this.currentChat);
            if (((bool) && (!ChatObject.canChangeChatInfo(this.currentChat))) || ((!bool) && (!this.currentChat.admin) && (!this.currentChat.creator) && (this.currentChat.admins_enabled)))
            {
              this.writeButton.setImageResource(2131165320);
              this.writeButton.setPadding(0, AndroidUtilities.dp(3.0F), 0, 0);
            }
          }
          else
          {
            label653:
            if (!ChatObject.isChannel(localChat)) {
              break label870;
            }
            if (ChatObject.hasAdminRights(localChat))
            {
              this.editItem = localActionBarMenu.addItem(12, 2131165500);
              localObject2 = localObject3;
              if (0 == 0) {
                localObject2 = localActionBarMenu.addItem(10, 2131165353);
              }
              if (!localChat.megagroup) {
                break label851;
              }
              ((ActionBarMenuItem)localObject2).addSubItem(12, LocaleController.getString("ManageGroupMenu", 2131493787));
            }
          }
          for (;;)
          {
            localObject1 = localObject2;
            if (!localChat.megagroup) {
              break;
            }
            localObject3 = localObject2;
            if (localObject2 == null) {
              localObject3 = localActionBarMenu.addItem(10, 2131165353);
            }
            ((ActionBarMenuItem)localObject3).addSubItem(16, LocaleController.getString("SearchMembers", 2131494306));
            localObject1 = localObject3;
            if (localChat.creator) {
              break;
            }
            localObject1 = localObject3;
            if (localChat.left) {
              break;
            }
            localObject1 = localObject3;
            if (localChat.kicked) {
              break;
            }
            ((ActionBarMenuItem)localObject3).addSubItem(7, LocaleController.getString("LeaveMegaMenu", 2131493743));
            localObject1 = localObject3;
            break;
            this.writeButton.setImageResource(2131165319);
            this.writeButton.setPadding(0, 0, 0, 0);
            break label653;
            label851:
            ((ActionBarMenuItem)localObject2).addSubItem(12, LocaleController.getString("ManageChannelMenu", 2131493785));
          }
          label870:
          if ((!localChat.admins_enabled) || (localChat.creator) || (localChat.admin)) {
            this.editItem = localActionBarMenu.addItem(8, 2131165340);
          }
          localObject1 = localActionBarMenu.addItem(10, 2131165353);
          if ((localChat.creator) && (this.chat_id > 0)) {
            ((ActionBarMenuItem)localObject1).addSubItem(11, LocaleController.getString("SetAdmins", 2131494371));
          }
          if ((!localChat.admins_enabled) || (localChat.creator) || (localChat.admin)) {
            ((ActionBarMenuItem)localObject1).addSubItem(8, LocaleController.getString("ChannelEdit", 2131493167));
          }
          ((ActionBarMenuItem)localObject1).addSubItem(16, LocaleController.getString("SearchMembers", 2131494306));
          if ((localChat.creator) && ((this.info == null) || (this.info.participants.participants.size() > 0))) {
            ((ActionBarMenuItem)localObject1).addSubItem(13, LocaleController.getString("ConvertGroupMenu", 2131493302));
          }
          ((ActionBarMenuItem)localObject1).addSubItem(7, LocaleController.getString("DeleteAndExit", 2131493361));
        }
        else
        {
          localObject1 = localActionBarMenu.addItem(10, 2131165353);
          ((ActionBarMenuItem)localObject1).addSubItem(8, LocaleController.getString("EditName", 2131493416));
        }
      }
    }
  }
  
  private void fetchUsersFromChannelInfo()
  {
    if ((this.currentChat == null) || (!this.currentChat.megagroup)) {}
    for (;;)
    {
      return;
      if (((this.info instanceof TLRPC.TL_channelFull)) && (this.info.participants != null))
      {
        int i = 0;
        while (i < this.info.participants.participants.size())
        {
          TLRPC.ChatParticipant localChatParticipant = (TLRPC.ChatParticipant)this.info.participants.participants.get(i);
          this.participantsMap.put(localChatParticipant.user_id, localChatParticipant);
          i += 1;
        }
      }
    }
  }
  
  private void fixLayout()
  {
    if (this.fragmentView == null) {
      return;
    }
    this.fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
    {
      public boolean onPreDraw()
      {
        if (ProfileActivity.this.fragmentView != null)
        {
          ProfileActivity.this.checkListViewScroll();
          ProfileActivity.this.needLayout();
          ProfileActivity.this.fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
        }
        return true;
      }
    });
  }
  
  private void getChannelParticipants(boolean paramBoolean)
  {
    int j = 0;
    if ((this.loadingUsers) || (this.participantsMap == null) || (this.info == null)) {
      return;
    }
    this.loadingUsers = true;
    final int i;
    final TLRPC.TL_channels_getParticipants localTL_channels_getParticipants;
    if ((this.participantsMap.size() != 0) && (paramBoolean))
    {
      i = 300;
      localTL_channels_getParticipants = new TLRPC.TL_channels_getParticipants();
      localTL_channels_getParticipants.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chat_id);
      localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsRecent();
      if (!paramBoolean) {
        break label150;
      }
    }
    for (;;)
    {
      localTL_channels_getParticipants.offset = j;
      localTL_channels_getParticipants.limit = 200;
      i = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_getParticipants, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error == null)
              {
                TLRPC.TL_channels_channelParticipants localTL_channels_channelParticipants = (TLRPC.TL_channels_channelParticipants)paramAnonymousTLObject;
                MessagesController.getInstance(ProfileActivity.this.currentAccount).putUsers(localTL_channels_channelParticipants.users, false);
                if (localTL_channels_channelParticipants.users.size() < 200) {
                  ProfileActivity.access$11202(ProfileActivity.this, true);
                }
                if (ProfileActivity.22.this.val$req.offset == 0)
                {
                  ProfileActivity.this.participantsMap.clear();
                  ProfileActivity.this.info.participants = new TLRPC.TL_chatParticipants();
                  MessagesStorage.getInstance(ProfileActivity.this.currentAccount).putUsersAndChats(localTL_channels_channelParticipants.users, null, true, true);
                  MessagesStorage.getInstance(ProfileActivity.this.currentAccount).updateChannelUsers(ProfileActivity.this.chat_id, localTL_channels_channelParticipants.participants);
                }
                int i = 0;
                while (i < localTL_channels_channelParticipants.participants.size())
                {
                  TLRPC.TL_chatChannelParticipant localTL_chatChannelParticipant = new TLRPC.TL_chatChannelParticipant();
                  localTL_chatChannelParticipant.channelParticipant = ((TLRPC.ChannelParticipant)localTL_channels_channelParticipants.participants.get(i));
                  localTL_chatChannelParticipant.inviter_id = localTL_chatChannelParticipant.channelParticipant.inviter_id;
                  localTL_chatChannelParticipant.user_id = localTL_chatChannelParticipant.channelParticipant.user_id;
                  localTL_chatChannelParticipant.date = localTL_chatChannelParticipant.channelParticipant.date;
                  if (ProfileActivity.this.participantsMap.indexOfKey(localTL_chatChannelParticipant.user_id) < 0)
                  {
                    ProfileActivity.this.info.participants.participants.add(localTL_chatChannelParticipant);
                    ProfileActivity.this.participantsMap.put(localTL_chatChannelParticipant.user_id, localTL_chatChannelParticipant);
                  }
                  i += 1;
                }
              }
              ProfileActivity.this.updateOnlineCount();
              ProfileActivity.access$11502(ProfileActivity.this, false);
              ProfileActivity.this.updateRowsIds();
              if (ProfileActivity.this.listAdapter != null) {
                ProfileActivity.this.listAdapter.notifyDataSetChanged();
              }
            }
          }, i);
        }
      });
      ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(i, this.classGuid);
      return;
      i = 0;
      break;
      label150:
      j = this.participantsMap.size();
    }
  }
  
  private void kickUser(int paramInt)
  {
    if (paramInt != 0)
    {
      MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chat_id, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramInt)), this.info);
      return;
    }
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
    if (AndroidUtilities.isTablet()) {
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[] { Long.valueOf(-this.chat_id) });
    }
    for (;;)
    {
      MessagesController.getInstance(this.currentAccount).deleteUserFromChat(this.chat_id, MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId())), this.info);
      this.playProfileAnimation = false;
      finishFragment();
      return;
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
    }
  }
  
  private void leaveChatPressed()
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    String str;
    if ((ChatObject.isChannel(this.chat_id, this.currentAccount)) && (!this.currentChat.megagroup)) {
      if (ChatObject.isChannel(this.chat_id, this.currentAccount))
      {
        str = LocaleController.getString("ChannelLeaveAlert", 2131493173);
        localBuilder.setMessage(str);
      }
    }
    for (;;)
    {
      localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          ProfileActivity.this.kickUser(0);
        }
      });
      localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
      showDialog(localBuilder.create());
      return;
      str = LocaleController.getString("AreYouSureDeleteAndExit", 2131493003);
      break;
      localBuilder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", 2131493003));
    }
  }
  
  private void needLayout()
  {
    int i;
    Object localObject;
    float f1;
    label135:
    label177:
    int j;
    label190:
    label210:
    label334:
    label382:
    float f2;
    if (this.actionBar.getOccupyStatusBar())
    {
      i = AndroidUtilities.statusBarHeight;
      i += ActionBar.getCurrentActionBarHeight();
      if ((this.listView != null) && (!this.openAnimationInProgress))
      {
        localObject = (FrameLayout.LayoutParams)this.listView.getLayoutParams();
        if (((FrameLayout.LayoutParams)localObject).topMargin != i)
        {
          ((FrameLayout.LayoutParams)localObject).topMargin = i;
          this.listView.setLayoutParams((ViewGroup.LayoutParams)localObject);
        }
      }
      if (this.avatarImage == null) {
        return;
      }
      f1 = this.extraHeight / AndroidUtilities.dp(88.0F);
      this.listView.setTopGlowOffset(this.extraHeight);
      if (this.writeButton != null)
      {
        localObject = this.writeButton;
        if (!this.actionBar.getOccupyStatusBar()) {
          break label519;
        }
        i = AndroidUtilities.statusBarHeight;
        ((ImageView)localObject).setTranslationY(i + ActionBar.getCurrentActionBarHeight() + this.extraHeight - AndroidUtilities.dp(29.5F));
        if (!this.openAnimationInProgress)
        {
          if (f1 <= 0.2F) {
            break label525;
          }
          i = 1;
          if (this.writeButton.getTag() != null) {
            break label531;
          }
          j = 1;
          if (i != j)
          {
            if (i == 0) {
              break label537;
            }
            this.writeButton.setTag(null);
            if (this.writeButtonAnimation != null)
            {
              localObject = this.writeButtonAnimation;
              this.writeButtonAnimation = null;
              ((AnimatorSet)localObject).cancel();
            }
            this.writeButtonAnimation = new AnimatorSet();
            if (i == 0) {
              break label551;
            }
            this.writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
            this.writeButtonAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[] { 1.0F }) });
            this.writeButtonAnimation.setDuration(150L);
            this.writeButtonAnimation.addListener(new AnimatorListenerAdapter()
            {
              public void onAnimationEnd(Animator paramAnonymousAnimator)
              {
                if ((ProfileActivity.this.writeButtonAnimation != null) && (ProfileActivity.this.writeButtonAnimation.equals(paramAnonymousAnimator))) {
                  ProfileActivity.access$11702(ProfileActivity.this, null);
                }
              }
            });
            this.writeButtonAnimation.start();
          }
        }
      }
      if (!this.actionBar.getOccupyStatusBar()) {
        break label643;
      }
      i = AndroidUtilities.statusBarHeight;
      f2 = i + ActionBar.getCurrentActionBarHeight() / 2.0F * (1.0F + f1) - 21.0F * AndroidUtilities.density + 27.0F * AndroidUtilities.density * f1;
      this.avatarImage.setScaleX((42.0F + 18.0F * f1) / 42.0F);
      this.avatarImage.setScaleY((42.0F + 18.0F * f1) / 42.0F);
      this.avatarImage.setTranslationX(-AndroidUtilities.dp(47.0F) * f1);
      this.avatarImage.setTranslationY((float)Math.ceil(f2));
      i = 0;
      label488:
      if (i >= 2) {
        return;
      }
      if (this.nameTextView[i] != null) {
        break label649;
      }
    }
    label519:
    label525:
    label531:
    label537:
    label551:
    label643:
    label649:
    do
    {
      i += 1;
      break label488;
      i = 0;
      break;
      i = 0;
      break label135;
      i = 0;
      break label177;
      j = 0;
      break label190;
      this.writeButton.setTag(Integer.valueOf(0));
      break label210;
      this.writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
      this.writeButtonAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[] { 0.2F }), ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[] { 0.2F }), ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[] { 0.0F }) });
      break label334;
      i = 0;
      break label382;
      this.nameTextView[i].setTranslationX(-21.0F * AndroidUtilities.density * f1);
      this.nameTextView[i].setTranslationY((float)Math.floor(f2) + AndroidUtilities.dp(1.3F) + AndroidUtilities.dp(7.0F) * f1);
      this.onlineTextView[i].setTranslationX(-21.0F * AndroidUtilities.density * f1);
      this.onlineTextView[i].setTranslationY((float)Math.floor(f2) + AndroidUtilities.dp(24.0F) + (float)Math.floor(11.0F * AndroidUtilities.density) * f1);
      this.nameTextView[i].setScaleX(1.0F + 0.12F * f1);
      this.nameTextView[i].setScaleY(1.0F + 0.12F * f1);
    } while ((i != 1) || (this.openAnimationInProgress));
    label821:
    int k;
    if (AndroidUtilities.isTablet())
    {
      j = AndroidUtilities.dp(490.0F);
      if ((this.callItem == null) && (this.editItem == null)) {
        break label1074;
      }
      k = 48;
      label839:
      j = (int)(j - AndroidUtilities.dp((k + 40) * (1.0F - f1) + 126.0F) - this.nameTextView[i].getTranslationX());
      float f3 = this.nameTextView[i].getPaint().measureText(this.nameTextView[i].getText().toString());
      float f4 = this.nameTextView[i].getScaleX();
      float f5 = this.nameTextView[i].getSideDrawablesSize();
      localObject = (FrameLayout.LayoutParams)this.nameTextView[i].getLayoutParams();
      if (j >= f3 * f4 + f5) {
        break label1080;
      }
    }
    label1074:
    label1080:
    for (((FrameLayout.LayoutParams)localObject).width = ((int)Math.ceil(j / this.nameTextView[i].getScaleX()));; ((FrameLayout.LayoutParams)localObject).width = -2)
    {
      this.nameTextView[i].setLayoutParams((ViewGroup.LayoutParams)localObject);
      localObject = (FrameLayout.LayoutParams)this.onlineTextView[i].getLayoutParams();
      ((FrameLayout.LayoutParams)localObject).rightMargin = ((int)Math.ceil(this.onlineTextView[i].getTranslationX() + AndroidUtilities.dp(8.0F) + AndroidUtilities.dp(40.0F) * (1.0F - f1)));
      this.onlineTextView[i].setLayoutParams((ViewGroup.LayoutParams)localObject);
      break;
      j = AndroidUtilities.displaySize.x;
      break label821;
      k = 0;
      break label839;
    }
  }
  
  private void openAddMember()
  {
    boolean bool = true;
    Object localObject = new Bundle();
    ((Bundle)localObject).putBoolean("onlyUsers", true);
    ((Bundle)localObject).putBoolean("destroyAfterSelect", true);
    ((Bundle)localObject).putBoolean("returnAsResult", true);
    if (!ChatObject.isChannel(this.currentChat)) {}
    SparseArray localSparseArray;
    for (;;)
    {
      ((Bundle)localObject).putBoolean("needForwardCount", bool);
      if (this.chat_id > 0)
      {
        if (ChatObject.canAddViaLink(this.currentChat)) {
          ((Bundle)localObject).putInt("chat_id", this.currentChat.id);
        }
        ((Bundle)localObject).putString("selectAlertString", LocaleController.getString("AddToTheGroup", 2131492944));
      }
      localObject = new ContactsActivity((Bundle)localObject);
      ((ContactsActivity)localObject).setDelegate(new ContactsActivity.ContactsActivityDelegate()
      {
        public void didSelectContact(TLRPC.User paramAnonymousUser, String paramAnonymousString, ContactsActivity paramAnonymousContactsActivity)
        {
          paramAnonymousContactsActivity = MessagesController.getInstance(ProfileActivity.this.currentAccount);
          int j = ProfileActivity.this.chat_id;
          TLRPC.ChatFull localChatFull = ProfileActivity.this.info;
          if (paramAnonymousString != null) {}
          for (int i = Utilities.parseInt(paramAnonymousString).intValue();; i = 0)
          {
            paramAnonymousContactsActivity.addUserToChat(j, paramAnonymousUser, localChatFull, i, null, ProfileActivity.this);
            return;
          }
        }
      });
      if ((this.info == null) || (this.info.participants == null)) {
        break label209;
      }
      localSparseArray = new SparseArray();
      int i = 0;
      while (i < this.info.participants.participants.size())
      {
        localSparseArray.put(((TLRPC.ChatParticipant)this.info.participants.participants.get(i)).user_id, null);
        i += 1;
      }
      bool = false;
    }
    ((ContactsActivity)localObject).setIgnoreUsers(localSparseArray);
    label209:
    presentFragment((BaseFragment)localObject);
  }
  
  private boolean processOnClickOrPress(final int paramInt)
  {
    if ((paramInt == this.usernameRow) || (paramInt == this.channelNameRow)) {
      if (paramInt == this.usernameRow)
      {
        localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
        if ((localObject1 != null) && (((TLRPC.User)localObject1).username != null)) {}
      }
    }
    do
    {
      do
      {
        return false;
        for (localObject1 = ((TLRPC.User)localObject1).username;; localObject1 = ((TLRPC.Chat)localObject1).username)
        {
          localObject2 = new AlertDialog.Builder(getParentActivity());
          localObject3 = LocaleController.getString("Copy", 2131493303);
          localObject1 = new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
            {
              if (paramAnonymousInt == 0) {}
              try
              {
                ((ClipboardManager)ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", "@" + localObject1));
                return;
              }
              catch (Exception paramAnonymousDialogInterface)
              {
                FileLog.e(paramAnonymousDialogInterface);
              }
            }
          };
          ((AlertDialog.Builder)localObject2).setItems(new CharSequence[] { localObject3 }, (DialogInterface.OnClickListener)localObject1);
          showDialog(((AlertDialog.Builder)localObject2).create());
          return true;
          localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
          if ((localObject1 == null) || (((TLRPC.Chat)localObject1).username == null)) {
            break;
          }
        }
        if (paramInt != this.phoneRow) {
          break;
        }
        localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
      } while ((localObject1 == null) || (((TLRPC.User)localObject1).phone == null) || (((TLRPC.User)localObject1).phone.length() == 0) || (getParentActivity() == null));
      localObject2 = new AlertDialog.Builder(getParentActivity());
      localObject3 = new ArrayList();
      final ArrayList localArrayList = new ArrayList();
      TLRPC.TL_userFull localTL_userFull = MessagesController.getInstance(this.currentAccount).getUserFull(((TLRPC.User)localObject1).id);
      if ((localTL_userFull != null) && (localTL_userFull.phone_calls_available))
      {
        ((ArrayList)localObject3).add(LocaleController.getString("CallViaTelegram", 2131493122));
        localArrayList.add(Integer.valueOf(2));
      }
      ((ArrayList)localObject3).add(LocaleController.getString("Call", 2131493105));
      localArrayList.add(Integer.valueOf(0));
      ((ArrayList)localObject3).add(LocaleController.getString("Copy", 2131493303));
      localArrayList.add(Integer.valueOf(1));
      ((AlertDialog.Builder)localObject2).setItems((CharSequence[])((ArrayList)localObject3).toArray(new CharSequence[((ArrayList)localObject3).size()]), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          paramAnonymousInt = ((Integer)localArrayList.get(paramAnonymousInt)).intValue();
          if (paramAnonymousInt == 0) {}
          do
          {
            try
            {
              paramAnonymousDialogInterface = new Intent("android.intent.action.DIAL", Uri.parse("tel:+" + localObject1.phone));
              paramAnonymousDialogInterface.addFlags(268435456);
              ProfileActivity.this.getParentActivity().startActivityForResult(paramAnonymousDialogInterface, 500);
              return;
            }
            catch (Exception paramAnonymousDialogInterface)
            {
              FileLog.e(paramAnonymousDialogInterface);
              return;
            }
            if (paramAnonymousInt == 1) {
              try
              {
                ((ClipboardManager)ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", "+" + localObject1.phone));
                return;
              }
              catch (Exception paramAnonymousDialogInterface)
              {
                FileLog.e(paramAnonymousDialogInterface);
                return;
              }
            }
          } while (paramAnonymousInt != 2);
          VoIPHelper.startCall(localObject1, ProfileActivity.this.getParentActivity(), MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(localObject1.id));
        }
      });
      showDialog(((AlertDialog.Builder)localObject2).create());
      return true;
    } while ((paramInt != this.channelInfoRow) && (paramInt != this.userInfoRow) && (paramInt != this.userInfoDetailedRow));
    final Object localObject1 = new AlertDialog.Builder(getParentActivity());
    Object localObject2 = LocaleController.getString("Copy", 2131493303);
    Object localObject3 = new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        try
        {
          if (paramInt == ProfileActivity.this.channelInfoRow)
          {
            paramAnonymousDialogInterface = ProfileActivity.this.info.about;
            if (!TextUtils.isEmpty(paramAnonymousDialogInterface)) {}
          }
          else
          {
            paramAnonymousDialogInterface = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.user_id);
            if (paramAnonymousDialogInterface == null) {
              break label80;
            }
            paramAnonymousDialogInterface = paramAnonymousDialogInterface.about;
            break label77;
          }
          AndroidUtilities.addToClipboard(paramAnonymousDialogInterface);
          return;
        }
        catch (Exception paramAnonymousDialogInterface)
        {
          FileLog.e(paramAnonymousDialogInterface);
          return;
        }
        for (;;)
        {
          label77:
          break;
          label80:
          paramAnonymousDialogInterface = null;
        }
      }
    };
    ((AlertDialog.Builder)localObject1).setItems(new CharSequence[] { localObject2 }, (DialogInterface.OnClickListener)localObject3);
    showDialog(((AlertDialog.Builder)localObject1).create());
    return true;
  }
  
  private void updateOnlineCount()
  {
    this.onlineCount = 0;
    int j = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    this.sortedUsers.clear();
    if (((this.info instanceof TLRPC.TL_chatFull)) || (((this.info instanceof TLRPC.TL_channelFull)) && (this.info.participants_count <= 200) && (this.info.participants != null)))
    {
      int i = 0;
      while (i < this.info.participants.participants.size())
      {
        Object localObject = (TLRPC.ChatParticipant)this.info.participants.participants.get(i);
        localObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChatParticipant)localObject).user_id));
        if ((localObject != null) && (((TLRPC.User)localObject).status != null) && ((((TLRPC.User)localObject).status.expires > j) || (((TLRPC.User)localObject).id == UserConfig.getInstance(this.currentAccount).getClientUserId())) && (((TLRPC.User)localObject).status.expires > 10000)) {
          this.onlineCount += 1;
        }
        this.sortedUsers.add(Integer.valueOf(i));
        i += 1;
      }
    }
    try
    {
      Collections.sort(this.sortedUsers, new Comparator()
      {
        public int compare(Integer paramAnonymousInteger1, Integer paramAnonymousInteger2)
        {
          paramAnonymousInteger2 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(paramAnonymousInteger2.intValue())).user_id));
          paramAnonymousInteger1 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(paramAnonymousInteger1.intValue())).user_id));
          int j = 0;
          int k = 0;
          int i = j;
          if (paramAnonymousInteger2 != null)
          {
            i = j;
            if (paramAnonymousInteger2.status != null)
            {
              if (paramAnonymousInteger2.id != UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId()) {
                break label220;
              }
              i = ConnectionsManager.getInstance(ProfileActivity.this.currentAccount).getCurrentTime() + 50000;
            }
          }
          j = k;
          if (paramAnonymousInteger1 != null)
          {
            j = k;
            if (paramAnonymousInteger1.status != null)
            {
              if (paramAnonymousInteger1.id != UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId()) {
                break label231;
              }
              j = ConnectionsManager.getInstance(ProfileActivity.this.currentAccount).getCurrentTime() + 50000;
            }
          }
          for (;;)
          {
            if ((i > 0) && (j > 0))
            {
              if (i > j)
              {
                return 1;
                label220:
                i = paramAnonymousInteger2.status.expires;
                break;
                label231:
                j = paramAnonymousInteger1.status.expires;
                continue;
              }
              if (i < j) {
                return -1;
              }
              return 0;
            }
          }
          if ((i < 0) && (j < 0))
          {
            if (i > j) {
              return 1;
            }
            if (i < j) {
              return -1;
            }
            return 0;
          }
          if (((i < 0) && (j > 0)) || ((i == 0) && (j != 0))) {
            return -1;
          }
          if (((j < 0) && (i > 0)) || ((j == 0) && (i != 0))) {
            return 1;
          }
          return 0;
        }
      });
      if (this.listAdapter != null) {
        this.listAdapter.notifyItemRangeChanged(this.emptyRowChat2 + 1, this.sortedUsers.size());
      }
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
  
  private void updateProfileData()
  {
    if ((this.avatarImage == null) || (this.nameTextView == null)) {}
    int i;
    Object localObject4;
    Object localObject5;
    label260:
    label515:
    label535:
    label547:
    label577:
    label647:
    label678:
    label684:
    label693:
    label699:
    label725:
    do
    {
      return;
      i = ConnectionsManager.getInstance(this.currentAccount).getConnectionState();
      TLRPC.User localUser;
      if (i == 2)
      {
        localObject2 = LocaleController.getString("WaitingForNetwork", 2131494621);
        if (this.user_id == 0) {
          continue;
        }
        localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
        localObject1 = null;
        localObject4 = null;
        if (localUser.photo != null)
        {
          localObject1 = localUser.photo.photo_small;
          localObject4 = localUser.photo.photo_big;
        }
        this.avatarDrawable.setInfo(localUser);
        this.avatarImage.setImage((TLObject)localObject1, "50_50", this.avatarDrawable);
        localObject5 = UserObject.getUserName(localUser);
        if (localUser.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
          break label260;
        }
        localObject1 = LocaleController.getString("ChatYourSelf", 2131493233);
        localObject5 = LocaleController.getString("ChatYourSelfName", 2131493238);
      }
      for (;;)
      {
        i = 0;
        for (;;)
        {
          if (i >= 2) {
            break label725;
          }
          if (this.nameTextView[i] != null) {
            break;
          }
          i += 1;
        }
        if (i == 1)
        {
          localObject2 = LocaleController.getString("Connecting", 2131493282);
          break;
        }
        if (i == 5)
        {
          localObject2 = LocaleController.getString("Updating", 2131494527);
          break;
        }
        if (i == 4)
        {
          localObject2 = LocaleController.getString("ConnectingToProxy", 2131493283);
          break;
        }
        localObject2 = null;
        break;
        if ((localUser.id == 333000) || (localUser.id == 777000)) {
          localObject1 = LocaleController.getString("ServiceNotifications", 2131494365);
        } else if (this.isBot) {
          localObject1 = LocaleController.getString("Bot", 2131493086);
        } else {
          localObject1 = LocaleController.formatUserStatus(this.currentAccount, localUser);
        }
      }
      Drawable localDrawable;
      long l;
      if ((i == 0) && (localUser.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) && (localUser.id / 1000 != 777) && (localUser.id / 1000 != 333) && (localUser.phone != null) && (localUser.phone.length() != 0) && (ContactsController.getInstance(this.currentAccount).contactsDict.get(Integer.valueOf(localUser.id)) == null) && ((ContactsController.getInstance(this.currentAccount).contactsDict.size() != 0) || (!ContactsController.getInstance(this.currentAccount).isLoadingContacts())))
      {
        localObject3 = PhoneFormat.getInstance().format("+" + localUser.phone);
        if (!this.nameTextView[i].getText().equals(localObject3)) {
          this.nameTextView[i].setText((CharSequence)localObject3);
        }
        if ((i != 0) || (localObject2 == null)) {
          break label647;
        }
        this.onlineTextView[i].setText((CharSequence)localObject2);
        if (this.currentEncryptedChat == null) {
          break label678;
        }
        localDrawable = Theme.chat_lockIconDrawable;
        localObject3 = null;
        if (i != 0) {
          break label699;
        }
        localObject3 = MessagesController.getInstance(this.currentAccount);
        if (this.dialog_id == 0L) {
          break label684;
        }
        l = this.dialog_id;
        if (!((MessagesController)localObject3).isDialogMuted(l)) {
          break label693;
        }
        localObject3 = Theme.chat_muteIconDrawable;
      }
      for (;;)
      {
        this.nameTextView[i].setLeftDrawable(localDrawable);
        this.nameTextView[i].setRightDrawable((Drawable)localObject3);
        break;
        if (this.nameTextView[i].getText().equals(localObject5)) {
          break label515;
        }
        this.nameTextView[i].setText((CharSequence)localObject5);
        break label515;
        if (this.onlineTextView[i].getText().equals(localObject1)) {
          break label535;
        }
        this.onlineTextView[i].setText((CharSequence)localObject1);
        break label535;
        localDrawable = null;
        break label547;
        l = this.user_id;
        break label577;
        localObject3 = null;
        continue;
        if (localUser.verified) {
          localObject3 = new CombinedDrawable(Theme.profile_verifiedDrawable, Theme.profile_verifiedCheckDrawable);
        }
      }
      localObject1 = this.avatarImage.getImageReceiver();
      if (!PhotoViewer.isShowingImage((TLRPC.FileLocation)localObject4)) {}
      for (bool = true;; bool = false)
      {
        ((ImageReceiver)localObject1).setVisible(bool, false);
        return;
      }
    } while (this.chat_id == 0);
    Object localObject3 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
    if (localObject3 != null)
    {
      this.currentChat = ((TLRPC.Chat)localObject3);
      if (!ChatObject.isChannel((TLRPC.Chat)localObject3)) {
        break label1165;
      }
      if ((this.info != null) && ((this.currentChat.megagroup) || ((this.info.participants_count != 0) && (!this.currentChat.admin) && (!this.info.can_view_participants)))) {
        break label953;
      }
      if (!this.currentChat.megagroup) {
        break label908;
      }
      localObject1 = LocaleController.getString("Loading", 2131493762).toLowerCase();
      label876:
      i = 0;
      label878:
      if (i >= 2) {
        break label1697;
      }
      if (this.nameTextView[i] != null) {
        break label1254;
      }
    }
    for (;;)
    {
      i += 1;
      break label878;
      localObject3 = this.currentChat;
      break;
      label908:
      if ((((TLRPC.Chat)localObject3).flags & 0x40) != 0)
      {
        localObject1 = LocaleController.getString("ChannelPublic", 2131493200).toLowerCase();
        break label876;
      }
      localObject1 = LocaleController.getString("ChannelPrivate", 2131493197).toLowerCase();
      break label876;
      label953:
      if ((this.currentChat.megagroup) && (this.info.participants_count <= 200))
      {
        if ((this.onlineCount > 1) && (this.info.participants_count != 0))
        {
          localObject1 = String.format("%s, %s", new Object[] { LocaleController.formatPluralString("Members", this.info.participants_count), LocaleController.formatPluralString("OnlineCount", this.onlineCount) });
          break label876;
        }
        localObject1 = LocaleController.formatPluralString("Members", this.info.participants_count);
        break label876;
      }
      localObject1 = new int[1];
      localObject4 = LocaleController.formatShortNumber(this.info.participants_count, (int[])localObject1);
      if (this.currentChat.megagroup)
      {
        localObject1 = LocaleController.formatPluralString("Members", localObject1[0]).replace(String.format("%d", new Object[] { Integer.valueOf(localObject1[0]) }), (CharSequence)localObject4);
        break label876;
      }
      localObject1 = LocaleController.formatPluralString("Subscribers", localObject1[0]).replace(String.format("%d", new Object[] { Integer.valueOf(localObject1[0]) }), (CharSequence)localObject4);
      break label876;
      label1165:
      i = ((TLRPC.Chat)localObject3).participants_count;
      if (this.info != null) {
        i = this.info.participants.participants.size();
      }
      if ((i != 0) && (this.onlineCount > 1))
      {
        localObject1 = String.format("%s, %s", new Object[] { LocaleController.formatPluralString("Members", i), LocaleController.formatPluralString("OnlineCount", this.onlineCount) });
        break label876;
      }
      localObject1 = LocaleController.formatPluralString("Members", i);
      break label876;
      label1254:
      if ((((TLRPC.Chat)localObject3).title != null) && (!this.nameTextView[i].getText().equals(((TLRPC.Chat)localObject3).title))) {
        this.nameTextView[i].setText(((TLRPC.Chat)localObject3).title);
      }
      this.nameTextView[i].setLeftDrawable(null);
      if (i != 0)
      {
        if (((TLRPC.Chat)localObject3).verified) {
          this.nameTextView[i].setRightDrawable(new CombinedDrawable(Theme.profile_verifiedDrawable, Theme.profile_verifiedCheckDrawable));
        }
        for (;;)
        {
          if ((i != 0) || (localObject2 == null)) {
            break label1424;
          }
          this.onlineTextView[i].setText((CharSequence)localObject2);
          break;
          this.nameTextView[i].setRightDrawable(null);
        }
      }
      localObject5 = this.nameTextView[i];
      if (MessagesController.getInstance(this.currentAccount).isDialogMuted(-this.chat_id)) {}
      for (localObject4 = Theme.chat_muteIconDrawable;; localObject4 = null)
      {
        ((SimpleTextView)localObject5).setRightDrawable((Drawable)localObject4);
        break;
      }
      label1424:
      if ((this.currentChat.megagroup) && (this.info != null) && (this.info.participants_count <= 200) && (this.onlineCount > 0))
      {
        if (!this.onlineTextView[i].getText().equals(localObject1)) {
          this.onlineTextView[i].setText((CharSequence)localObject1);
        }
      }
      else if ((i == 0) && (ChatObject.isChannel(this.currentChat)) && (this.info != null) && (this.info.participants_count != 0) && ((this.currentChat.megagroup) || (this.currentChat.broadcast)))
      {
        localObject4 = new int[1];
        localObject5 = LocaleController.formatShortNumber(this.info.participants_count, (int[])localObject4);
        if (this.currentChat.megagroup) {
          this.onlineTextView[i].setText(LocaleController.formatPluralString("Members", localObject4[0]).replace(String.format("%d", new Object[] { Integer.valueOf(localObject4[0]) }), (CharSequence)localObject5));
        } else {
          this.onlineTextView[i].setText(LocaleController.formatPluralString("Subscribers", localObject4[0]).replace(String.format("%d", new Object[] { Integer.valueOf(localObject4[0]) }), (CharSequence)localObject5));
        }
      }
      else if (!this.onlineTextView[i].getText().equals(localObject1))
      {
        this.onlineTextView[i].setText((CharSequence)localObject1);
      }
    }
    label1697:
    Object localObject2 = null;
    Object localObject1 = null;
    if (((TLRPC.Chat)localObject3).photo != null)
    {
      localObject2 = ((TLRPC.Chat)localObject3).photo.photo_small;
      localObject1 = ((TLRPC.Chat)localObject3).photo.photo_big;
    }
    this.avatarDrawable.setInfo((TLRPC.Chat)localObject3);
    this.avatarImage.setImage((TLObject)localObject2, "50_50", this.avatarDrawable);
    localObject2 = this.avatarImage.getImageReceiver();
    if (!PhotoViewer.isShowingImage((TLRPC.FileLocation)localObject1)) {}
    for (boolean bool = true;; bool = false)
    {
      ((ImageReceiver)localObject2).setVisible(bool, false);
      return;
    }
  }
  
  private void updateRowsIds()
  {
    int j = 0;
    this.emptyRow = -1;
    this.phoneRow = -1;
    this.userInfoRow = -1;
    this.userInfoDetailedRow = -1;
    this.userSectionRow = -1;
    this.sectionRow = -1;
    this.sharedMediaRow = -1;
    this.settingsNotificationsRow = -1;
    this.usernameRow = -1;
    this.settingsTimerRow = -1;
    this.settingsKeyRow = -1;
    this.startSecretChatRow = -1;
    this.membersEndRow = -1;
    this.emptyRowChat2 = -1;
    this.addMemberRow = -1;
    this.channelInfoRow = -1;
    this.channelNameRow = -1;
    this.convertRow = -1;
    this.convertHelpRow = -1;
    this.emptyRowChat = -1;
    this.membersSectionRow = -1;
    this.membersRow = -1;
    this.leaveChannelRow = -1;
    this.loadMoreMembersRow = -1;
    this.groupsInCommonRow = -1;
    this.rowCount = 0;
    if (this.user_id != 0)
    {
      TLRPC.User localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.emptyRow = i;
      if ((!this.isBot) && (!TextUtils.isEmpty(localUser.phone)))
      {
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.phoneRow = i;
      }
      TLRPC.TL_userFull localTL_userFull = MessagesController.getInstance(this.currentAccount).getUserFull(this.user_id);
      i = j;
      if (localUser != null)
      {
        i = j;
        if (!TextUtils.isEmpty(localUser.username)) {
          i = 1;
        }
      }
      if ((localTL_userFull != null) && (!TextUtils.isEmpty(localTL_userFull.about)))
      {
        if (this.phoneRow != -1)
        {
          j = this.rowCount;
          this.rowCount = (j + 1);
          this.userSectionRow = j;
        }
        if ((i != 0) || (this.isBot))
        {
          j = this.rowCount;
          this.rowCount = (j + 1);
          this.userInfoRow = j;
        }
      }
      else
      {
        if (i != 0)
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.usernameRow = i;
        }
        if ((this.phoneRow != -1) || (this.userInfoRow != -1) || (this.userInfoDetailedRow != -1) || (this.usernameRow != -1))
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.sectionRow = i;
        }
        if (this.user_id != UserConfig.getInstance(this.currentAccount).getClientUserId())
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.settingsNotificationsRow = i;
        }
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.sharedMediaRow = i;
        if ((this.currentEncryptedChat instanceof TLRPC.TL_encryptedChat))
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.settingsTimerRow = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.settingsKeyRow = i;
        }
        if ((localTL_userFull != null) && (localTL_userFull.common_chats_count != 0))
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.groupsInCommonRow = i;
        }
        if ((localUser != null) && (!this.isBot) && (this.currentEncryptedChat == null) && (localUser.id != UserConfig.getInstance(this.currentAccount).getClientUserId()))
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.startSecretChatRow = i;
        }
      }
    }
    label1456:
    do
    {
      for (;;)
      {
        return;
        j = this.rowCount;
        this.rowCount = (j + 1);
        this.userInfoDetailedRow = j;
        break;
        if (this.chat_id != 0)
        {
          if (this.chat_id <= 0) {
            break label1456;
          }
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.emptyRow = i;
          if ((ChatObject.isChannel(this.currentChat)) && (((this.info != null) && (this.info.about != null) && (this.info.about.length() > 0)) || ((this.currentChat.username != null) && (this.currentChat.username.length() > 0))))
          {
            if ((this.info != null) && (this.info.about != null) && (this.info.about.length() > 0))
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.channelInfoRow = i;
            }
            if ((this.currentChat.username != null) && (this.currentChat.username.length() > 0))
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.channelNameRow = i;
            }
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.sectionRow = i;
          }
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.settingsNotificationsRow = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.sharedMediaRow = i;
          if (ChatObject.isChannel(this.currentChat))
          {
            if ((!this.currentChat.megagroup) && (this.info != null) && ((this.currentChat.creator) || (this.info.can_view_participants)))
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.membersRow = i;
            }
            if ((!this.currentChat.creator) && (!this.currentChat.left) && (!this.currentChat.kicked) && (!this.currentChat.megagroup))
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.leaveChannelRow = i;
            }
            if ((this.currentChat.megagroup) && (((this.currentChat.admin_rights != null) && (this.currentChat.admin_rights.invite_users)) || (((this.currentChat.creator) || (this.currentChat.democracy)) && ((this.info == null) || (this.info.participants_count < MessagesController.getInstance(this.currentAccount).maxMegagroupCount)))))
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.addMemberRow = i;
            }
            if ((this.info != null) && (this.currentChat.megagroup) && (this.info.participants != null) && (!this.info.participants.participants.isEmpty()))
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.emptyRowChat = i;
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.membersSectionRow = i;
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.emptyRowChat2 = i;
              this.rowCount += this.info.participants.participants.size();
              this.membersEndRow = this.rowCount;
              if (!this.usersEndReached)
              {
                i = this.rowCount;
                this.rowCount = (i + 1);
                this.loadMoreMembersRow = i;
              }
            }
          }
          else
          {
            if (this.info != null)
            {
              if ((!(this.info.participants instanceof TLRPC.TL_chatParticipantsForbidden)) && (this.info.participants.participants.size() < MessagesController.getInstance(this.currentAccount).maxGroupCount) && ((this.currentChat.admin) || (this.currentChat.creator) || (!this.currentChat.admins_enabled)))
              {
                i = this.rowCount;
                this.rowCount = (i + 1);
                this.addMemberRow = i;
              }
              if ((this.currentChat.creator) && (this.info.participants.participants.size() >= MessagesController.getInstance(this.currentAccount).minGroupConvertSize))
              {
                i = this.rowCount;
                this.rowCount = (i + 1);
                this.convertRow = i;
              }
            }
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.emptyRowChat = i;
            if (this.convertRow != -1)
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.convertHelpRow = i;
            }
            while ((this.info != null) && (!(this.info.participants instanceof TLRPC.TL_chatParticipantsForbidden)))
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.emptyRowChat2 = i;
              this.rowCount += this.info.participants.participants.size();
              this.membersEndRow = this.rowCount;
              return;
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.membersSectionRow = i;
            }
          }
        }
      }
    } while ((ChatObject.isChannel(this.currentChat)) || (this.info == null) || ((this.info.participants instanceof TLRPC.TL_chatParticipantsForbidden)));
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.addMemberRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.emptyRowChat2 = i;
    this.rowCount += this.info.participants.participants.size();
    this.membersEndRow = this.rowCount;
  }
  
  protected ActionBar createActionBar(Context paramContext)
  {
    paramContext = new ActionBar(paramContext)
    {
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return super.onTouchEvent(paramAnonymousMotionEvent);
      }
    };
    int i;
    if ((this.user_id != 0) || ((ChatObject.isChannel(this.chat_id, this.currentAccount)) && (!this.currentChat.megagroup)))
    {
      i = 5;
      paramContext.setItemsBackgroundColor(AvatarDrawable.getButtonColorForId(i), false);
      paramContext.setItemsColor(Theme.getColor("actionBarDefaultIcon"), false);
      paramContext.setItemsColor(Theme.getColor("actionBarActionModeDefaultIcon"), true);
      paramContext.setBackButtonDrawable(new BackDrawable(false));
      paramContext.setCastShadows(false);
      paramContext.setAddToContainer(false);
      if ((Build.VERSION.SDK_INT < 21) || (AndroidUtilities.isTablet())) {
        break label127;
      }
    }
    label127:
    for (boolean bool = true;; bool = false)
    {
      paramContext.setOccupyStatusBar(bool);
      return paramContext;
      i = this.chat_id;
      break;
    }
  }
  
  public View createView(Context paramContext)
  {
    Theme.createProfileResources(paramContext);
    this.hasOwnBackground = true;
    this.extraHeight = AndroidUtilities.dp(88.0F);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (ProfileActivity.this.getParentActivity() == null) {}
        do
        {
          do
          {
            final Object localObject1;
            do
            {
              do
              {
                do
                {
                  return;
                  if (paramAnonymousInt == -1)
                  {
                    ProfileActivity.this.finishFragment();
                    return;
                  }
                  if (paramAnonymousInt != 2) {
                    break;
                  }
                } while (MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id)) == null);
                if (!ProfileActivity.this.isBot)
                {
                  localObject1 = new AlertDialog.Builder(ProfileActivity.this.getParentActivity());
                  if (!ProfileActivity.this.userBlocked) {
                    ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("AreYouSureBlockContact", 2131492999));
                  }
                  for (;;)
                  {
                    ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
                    ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                      {
                        if (!ProfileActivity.this.userBlocked)
                        {
                          MessagesController.getInstance(ProfileActivity.this.currentAccount).blockUser(ProfileActivity.this.user_id);
                          return;
                        }
                        MessagesController.getInstance(ProfileActivity.this.currentAccount).unblockUser(ProfileActivity.this.user_id);
                      }
                    });
                    ((AlertDialog.Builder)localObject1).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                    ProfileActivity.this.showDialog(((AlertDialog.Builder)localObject1).create());
                    return;
                    ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("AreYouSureUnblockContact", 2131493016));
                  }
                }
                if (!ProfileActivity.this.userBlocked)
                {
                  MessagesController.getInstance(ProfileActivity.this.currentAccount).blockUser(ProfileActivity.this.user_id);
                  return;
                }
                MessagesController.getInstance(ProfileActivity.this.currentAccount).unblockUser(ProfileActivity.this.user_id);
                SendMessagesHelper.getInstance(ProfileActivity.this.currentAccount).sendMessage("/start", ProfileActivity.this.user_id, null, null, false, null, null, null);
                ProfileActivity.this.finishFragment();
                return;
                if (paramAnonymousInt == 1)
                {
                  localObject1 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                  localObject4 = new Bundle();
                  ((Bundle)localObject4).putInt("user_id", ((TLRPC.User)localObject1).id);
                  ((Bundle)localObject4).putBoolean("addContact", true);
                  ProfileActivity.this.presentFragment(new ContactAddActivity((Bundle)localObject4));
                  return;
                }
                if (paramAnonymousInt == 3)
                {
                  localObject1 = new Bundle();
                  ((Bundle)localObject1).putBoolean("onlySelect", true);
                  ((Bundle)localObject1).putString("selectAlertString", LocaleController.getString("SendContactTo", 2131494335));
                  ((Bundle)localObject1).putString("selectAlertStringGroup", LocaleController.getString("SendContactToGroup", 2131494336));
                  localObject1 = new DialogsActivity((Bundle)localObject1);
                  ((DialogsActivity)localObject1).setDelegate(ProfileActivity.this);
                  ProfileActivity.this.presentFragment((BaseFragment)localObject1);
                  return;
                }
                if (paramAnonymousInt == 4)
                {
                  localObject1 = new Bundle();
                  ((Bundle)localObject1).putInt("user_id", ProfileActivity.this.user_id);
                  ProfileActivity.this.presentFragment(new ContactAddActivity((Bundle)localObject1));
                  return;
                }
                if (paramAnonymousInt != 5) {
                  break;
                }
                localObject1 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
              } while ((localObject1 == null) || (ProfileActivity.this.getParentActivity() == null));
              localObject4 = new AlertDialog.Builder(ProfileActivity.this.getParentActivity());
              ((AlertDialog.Builder)localObject4).setMessage(LocaleController.getString("AreYouSureDeleteContact", 2131493004));
              ((AlertDialog.Builder)localObject4).setTitle(LocaleController.getString("AppName", 2131492981));
              ((AlertDialog.Builder)localObject4).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                {
                  paramAnonymous2DialogInterface = new ArrayList();
                  paramAnonymous2DialogInterface.add(localObject1);
                  ContactsController.getInstance(ProfileActivity.this.currentAccount).deleteContact(paramAnonymous2DialogInterface);
                }
              });
              ((AlertDialog.Builder)localObject4).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
              ProfileActivity.this.showDialog(((AlertDialog.Builder)localObject4).create());
              return;
              if (paramAnonymousInt == 7)
              {
                ProfileActivity.this.leaveChatPressed();
                return;
              }
              if (paramAnonymousInt == 8)
              {
                localObject1 = new Bundle();
                ((Bundle)localObject1).putInt("chat_id", ProfileActivity.this.chat_id);
                ProfileActivity.this.presentFragment(new ChangeChatNameActivity((Bundle)localObject1));
                return;
              }
              if (paramAnonymousInt == 12)
              {
                localObject1 = new Bundle();
                ((Bundle)localObject1).putInt("chat_id", ProfileActivity.this.chat_id);
                localObject1 = new ChannelEditActivity((Bundle)localObject1);
                ((ChannelEditActivity)localObject1).setInfo(ProfileActivity.this.info);
                ProfileActivity.this.presentFragment((BaseFragment)localObject1);
                return;
              }
              if (paramAnonymousInt != 9) {
                break;
              }
              localObject1 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
            } while (localObject1 == null);
            Object localObject4 = new Bundle();
            ((Bundle)localObject4).putBoolean("onlySelect", true);
            ((Bundle)localObject4).putInt("dialogsType", 2);
            ((Bundle)localObject4).putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupTitle", 2131492946, new Object[] { UserObject.getUserName((TLRPC.User)localObject1), "%1$s" }));
            localObject4 = new DialogsActivity((Bundle)localObject4);
            ((DialogsActivity)localObject4).setDelegate(new DialogsActivity.DialogsActivityDelegate()
            {
              public void didSelectDialogs(DialogsActivity paramAnonymous2DialogsActivity, ArrayList<Long> paramAnonymous2ArrayList, CharSequence paramAnonymous2CharSequence, boolean paramAnonymous2Boolean)
              {
                long l = ((Long)paramAnonymous2ArrayList.get(0)).longValue();
                paramAnonymous2ArrayList = new Bundle();
                paramAnonymous2ArrayList.putBoolean("scrollToTopOnResume", true);
                paramAnonymous2ArrayList.putInt("chat_id", -(int)l);
                if (!MessagesController.getInstance(ProfileActivity.this.currentAccount).checkCanOpenChat(paramAnonymous2ArrayList, paramAnonymous2DialogsActivity)) {
                  return;
                }
                NotificationCenter.getInstance(ProfileActivity.this.currentAccount).removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                NotificationCenter.getInstance(ProfileActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                MessagesController.getInstance(ProfileActivity.this.currentAccount).addUserToChat(-(int)l, localObject1, null, 0, null, ProfileActivity.this);
                ProfileActivity.this.presentFragment(new ChatActivity(paramAnonymous2ArrayList), true);
                ProfileActivity.this.removeSelfFromStack();
              }
            });
            ProfileActivity.this.presentFragment((BaseFragment)localObject4);
            return;
            if (paramAnonymousInt == 10) {
              for (;;)
              {
                try
                {
                  localObject1 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                  if (localObject1 == null) {
                    break;
                  }
                  localObject4 = new Intent("android.intent.action.SEND");
                  ((Intent)localObject4).setType("text/plain");
                  TLRPC.TL_userFull localTL_userFull = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.botInfo.user_id);
                  if ((ProfileActivity.this.botInfo != null) && (localTL_userFull != null) && (!TextUtils.isEmpty(localTL_userFull.about)))
                  {
                    ((Intent)localObject4).putExtra("android.intent.extra.TEXT", String.format("%s https://" + MessagesController.getInstance(ProfileActivity.this.currentAccount).linkPrefix + "/%s", new Object[] { localTL_userFull.about, ((TLRPC.User)localObject1).username }));
                    ProfileActivity.this.startActivityForResult(Intent.createChooser((Intent)localObject4, LocaleController.getString("BotShare", 2131493094)), 500);
                    return;
                  }
                }
                catch (Exception localException1)
                {
                  FileLog.e(localException1);
                  return;
                }
                ((Intent)localObject4).putExtra("android.intent.extra.TEXT", String.format("https://" + MessagesController.getInstance(ProfileActivity.this.currentAccount).linkPrefix + "/%s", new Object[] { localException1.username }));
              }
            }
            Object localObject2;
            if (paramAnonymousInt == 11)
            {
              localObject2 = new Bundle();
              ((Bundle)localObject2).putInt("chat_id", ProfileActivity.this.chat_id);
              localObject2 = new SetAdminsActivity((Bundle)localObject2);
              ((SetAdminsActivity)localObject2).setChatInfo(ProfileActivity.this.info);
              ProfileActivity.this.presentFragment((BaseFragment)localObject2);
              return;
            }
            if (paramAnonymousInt == 13)
            {
              localObject2 = new Bundle();
              ((Bundle)localObject2).putInt("chat_id", ProfileActivity.this.chat_id);
              ProfileActivity.this.presentFragment(new ConvertGroupActivity((Bundle)localObject2));
              return;
            }
            if (paramAnonymousInt == 14) {
              for (;;)
              {
                long l;
                try
                {
                  if (ProfileActivity.this.currentEncryptedChat != null)
                  {
                    l = ProfileActivity.this.currentEncryptedChat.id << 32;
                    DataQuery.getInstance(ProfileActivity.this.currentAccount).installShortcut(l);
                    return;
                  }
                }
                catch (Exception localException2)
                {
                  FileLog.e(localException2);
                  return;
                }
                if (ProfileActivity.this.user_id != 0)
                {
                  l = ProfileActivity.this.user_id;
                }
                else
                {
                  if (ProfileActivity.this.chat_id == 0) {
                    break;
                  }
                  paramAnonymousInt = ProfileActivity.this.chat_id;
                  l = -paramAnonymousInt;
                }
              }
            }
            if (paramAnonymousInt != 15) {
              break;
            }
            localObject3 = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
          } while (localObject3 == null);
          VoIPHelper.startCall((TLRPC.User)localObject3, ProfileActivity.this.getParentActivity(), MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(((TLRPC.User)localObject3).id));
          return;
        } while (paramAnonymousInt != 16);
        Object localObject3 = new Bundle();
        ((Bundle)localObject3).putInt("chat_id", ProfileActivity.this.chat_id);
        if (ChatObject.isChannel(ProfileActivity.this.currentChat))
        {
          ((Bundle)localObject3).putInt("type", 2);
          ((Bundle)localObject3).putBoolean("open_search", true);
          ProfileActivity.this.presentFragment(new ChannelUsersActivity((Bundle)localObject3));
          return;
        }
        localObject3 = new ChatUsersActivity((Bundle)localObject3);
        ((ChatUsersActivity)localObject3).setInfo(ProfileActivity.this.info);
        ProfileActivity.this.presentFragment((BaseFragment)localObject3);
      }
    });
    createActionBarMenu();
    this.listAdapter = new ListAdapter(paramContext);
    this.avatarDrawable = new AvatarDrawable();
    this.avatarDrawable.setProfile(true);
    this.fragmentView = new FrameLayout(paramContext)
    {
      public boolean hasOverlappingRendering()
      {
        return false;
      }
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
        ProfileActivity.this.checkListViewScroll();
      }
    };
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    this.listView = new RecyclerListView(paramContext)
    {
      public boolean hasOverlappingRendering()
      {
        return false;
      }
    };
    this.listView.setTag(Integer.valueOf(6));
    this.listView.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
    this.listView.setVerticalScrollBarEnabled(false);
    this.listView.setItemAnimator(null);
    this.listView.setLayoutAnimation(null);
    this.listView.setClipToPadding(false);
    this.layoutManager = new LinearLayoutManager(paramContext)
    {
      public boolean supportsPredictiveItemAnimations()
      {
        return false;
      }
    };
    this.layoutManager.setOrientation(1);
    this.listView.setLayoutManager(this.layoutManager);
    Object localObject1 = this.listView;
    Object localObject2;
    if ((this.user_id != 0) || ((ChatObject.isChannel(this.chat_id, this.currentAccount)) && (!this.currentChat.megagroup)))
    {
      i = 5;
      ((RecyclerListView)localObject1).setGlowColor(AvatarDrawable.getProfileBackColorForId(i));
      localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
      this.listView.setAdapter(this.listAdapter);
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (ProfileActivity.this.getParentActivity() == null) {}
          for (;;)
          {
            return;
            final long l;
            if (paramAnonymousInt == ProfileActivity.this.sharedMediaRow)
            {
              paramAnonymousView = new Bundle();
              if (ProfileActivity.this.user_id != 0) {
                if (ProfileActivity.this.dialog_id != 0L)
                {
                  l = ProfileActivity.this.dialog_id;
                  paramAnonymousView.putLong("dialog_id", l);
                }
              }
              for (;;)
              {
                paramAnonymousView = new MediaActivity(paramAnonymousView);
                paramAnonymousView.setChatInfo(ProfileActivity.this.info);
                ProfileActivity.this.presentFragment(paramAnonymousView);
                return;
                l = ProfileActivity.this.user_id;
                break;
                paramAnonymousView.putLong("dialog_id", -ProfileActivity.this.chat_id);
              }
            }
            if (paramAnonymousInt == ProfileActivity.this.groupsInCommonRow)
            {
              ProfileActivity.this.presentFragment(new CommonGroupsActivity(ProfileActivity.this.user_id));
              return;
            }
            if (paramAnonymousInt == ProfileActivity.this.settingsKeyRow)
            {
              paramAnonymousView = new Bundle();
              paramAnonymousView.putInt("chat_id", (int)(ProfileActivity.this.dialog_id >> 32));
              ProfileActivity.this.presentFragment(new IdenticonActivity(paramAnonymousView));
              return;
            }
            if (paramAnonymousInt == ProfileActivity.this.settingsTimerRow)
            {
              ProfileActivity.this.showDialog(AlertsCreator.createTTLAlert(ProfileActivity.this.getParentActivity(), ProfileActivity.this.currentEncryptedChat).create());
              return;
            }
            if (paramAnonymousInt == ProfileActivity.this.settingsNotificationsRow)
            {
              if (ProfileActivity.this.dialog_id != 0L) {
                l = ProfileActivity.this.dialog_id;
              }
              for (;;)
              {
                localObject = new String[5];
                localObject[0] = LocaleController.getString("NotificationsTurnOn", 2131494026);
                localObject[1] = LocaleController.formatString("MuteFor", 2131493857, new Object[] { LocaleController.formatPluralString("Hours", 1) });
                localObject[2] = LocaleController.formatString("MuteFor", 2131493857, new Object[] { LocaleController.formatPluralString("Days", 2) });
                localObject[3] = LocaleController.getString("NotificationsCustomize", 2131494007);
                localObject[4] = LocaleController.getString("NotificationsTurnOff", 2131494025);
                paramAnonymousView = new LinearLayout(ProfileActivity.this.getParentActivity());
                paramAnonymousView.setOrientation(1);
                paramAnonymousInt = 0;
                while (paramAnonymousInt < localObject.length)
                {
                  TextView localTextView = new TextView(ProfileActivity.this.getParentActivity());
                  localTextView.setTextColor(Theme.getColor("dialogTextBlack"));
                  localTextView.setTextSize(1, 16.0F);
                  localTextView.setLines(1);
                  localTextView.setMaxLines(1);
                  Drawable localDrawable = ProfileActivity.this.getParentActivity().getResources().getDrawable(new int[] { 2131165559, 2131165555, 2131165556, 2131165557, 2131165558 }[paramAnonymousInt]);
                  localDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("dialogIcon"), PorterDuff.Mode.MULTIPLY));
                  localTextView.setCompoundDrawablesWithIntrinsicBounds(localDrawable, null, null, null);
                  localTextView.setTag(Integer.valueOf(paramAnonymousInt));
                  localTextView.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                  localTextView.setPadding(AndroidUtilities.dp(24.0F), 0, AndroidUtilities.dp(24.0F), 0);
                  localTextView.setSingleLine(true);
                  localTextView.setGravity(19);
                  localTextView.setCompoundDrawablePadding(AndroidUtilities.dp(26.0F));
                  localTextView.setText(localObject[paramAnonymousInt]);
                  paramAnonymousView.addView(localTextView, LayoutHelper.createLinear(-1, 48, 51));
                  localTextView.setOnClickListener(new View.OnClickListener()
                  {
                    public void onClick(View paramAnonymous2View)
                    {
                      int j = ((Integer)paramAnonymous2View.getTag()).intValue();
                      if (j == 0)
                      {
                        paramAnonymous2View = MessagesController.getNotificationsSettings(ProfileActivity.this.currentAccount).edit();
                        paramAnonymous2View.putInt("notify2_" + l, 0);
                        MessagesStorage.getInstance(ProfileActivity.this.currentAccount).setDialogFlags(l, 0L);
                        paramAnonymous2View.commit();
                        paramAnonymous2View = (TLRPC.TL_dialog)MessagesController.getInstance(ProfileActivity.this.currentAccount).dialogs_dict.get(l);
                        if (paramAnonymous2View != null) {
                          paramAnonymous2View.notify_settings = new TLRPC.TL_peerNotifySettings();
                        }
                        NotificationsController.getInstance(ProfileActivity.this.currentAccount).updateServerNotificationsSettings(l);
                      }
                      for (;;)
                      {
                        ProfileActivity.this.listAdapter.notifyItemChanged(ProfileActivity.this.settingsNotificationsRow);
                        ProfileActivity.this.dismissCurrentDialig();
                        return;
                        if (j != 3) {
                          break;
                        }
                        paramAnonymous2View = new Bundle();
                        paramAnonymous2View.putLong("dialog_id", l);
                        ProfileActivity.this.presentFragment(new ProfileNotificationsActivity(paramAnonymous2View));
                      }
                      int i = ConnectionsManager.getInstance(ProfileActivity.this.currentAccount).getCurrentTime();
                      if (j == 1)
                      {
                        i += 3600;
                        label261:
                        paramAnonymous2View = MessagesController.getNotificationsSettings(ProfileActivity.this.currentAccount).edit();
                        if (j != 4) {
                          break label464;
                        }
                        paramAnonymous2View.putInt("notify2_" + l, 2);
                      }
                      for (long l = 1L;; l = i << 32 | 1L)
                      {
                        NotificationsController.getInstance(ProfileActivity.this.currentAccount).removeNotificationsForDialog(l);
                        MessagesStorage.getInstance(ProfileActivity.this.currentAccount).setDialogFlags(l, l);
                        paramAnonymous2View.commit();
                        paramAnonymous2View = (TLRPC.TL_dialog)MessagesController.getInstance(ProfileActivity.this.currentAccount).dialogs_dict.get(l);
                        if (paramAnonymous2View != null)
                        {
                          paramAnonymous2View.notify_settings = new TLRPC.TL_peerNotifySettings();
                          paramAnonymous2View.notify_settings.mute_until = i;
                        }
                        NotificationsController.getInstance(ProfileActivity.this.currentAccount).updateServerNotificationsSettings(l);
                        break;
                        if (j == 2)
                        {
                          i += 172800;
                          break label261;
                        }
                        if (j != 4) {
                          break label261;
                        }
                        i = Integer.MAX_VALUE;
                        break label261;
                        label464:
                        paramAnonymous2View.putInt("notify2_" + l, 3);
                        paramAnonymous2View.putInt("notifyuntil_" + l, i);
                      }
                    }
                  });
                  paramAnonymousInt += 1;
                }
                if (ProfileActivity.this.user_id != 0) {
                  l = ProfileActivity.this.user_id;
                } else {
                  l = -ProfileActivity.this.chat_id;
                }
              }
              Object localObject = new AlertDialog.Builder(ProfileActivity.this.getParentActivity());
              ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("Notifications", 2131494004));
              ((AlertDialog.Builder)localObject).setView(paramAnonymousView);
              ProfileActivity.this.showDialog(((AlertDialog.Builder)localObject).create());
              return;
            }
            if (paramAnonymousInt == ProfileActivity.this.startSecretChatRow)
            {
              paramAnonymousView = new AlertDialog.Builder(ProfileActivity.this.getParentActivity());
              paramAnonymousView.setMessage(LocaleController.getString("AreYouSureSecretChat", 2131493011));
              paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
              paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                {
                  ProfileActivity.access$5802(ProfileActivity.this, true);
                  SecretChatHelper.getInstance(ProfileActivity.this.currentAccount).startSecretChat(ProfileActivity.this.getParentActivity(), MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id)));
                }
              });
              paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
              ProfileActivity.this.showDialog(paramAnonymousView.create());
              return;
            }
            if ((paramAnonymousInt <= ProfileActivity.this.emptyRowChat2) || (paramAnonymousInt >= ProfileActivity.this.membersEndRow)) {
              break;
            }
            if (!ProfileActivity.this.sortedUsers.isEmpty()) {}
            for (paramAnonymousInt = ((TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(((Integer)ProfileActivity.this.sortedUsers.get(paramAnonymousInt - ProfileActivity.this.emptyRowChat2 - 1)).intValue())).user_id; paramAnonymousInt != UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId(); paramAnonymousInt = ((TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(paramAnonymousInt - ProfileActivity.this.emptyRowChat2 - 1)).user_id)
            {
              paramAnonymousView = new Bundle();
              paramAnonymousView.putInt("user_id", paramAnonymousInt);
              ProfileActivity.this.presentFragment(new ProfileActivity(paramAnonymousView));
              return;
            }
          }
          if (paramAnonymousInt == ProfileActivity.this.addMemberRow)
          {
            ProfileActivity.this.openAddMember();
            return;
          }
          if (paramAnonymousInt == ProfileActivity.this.channelNameRow) {
            for (;;)
            {
              try
              {
                paramAnonymousView = new Intent("android.intent.action.SEND");
                paramAnonymousView.setType("text/plain");
                if ((ProfileActivity.this.info.about != null) && (ProfileActivity.this.info.about.length() > 0))
                {
                  paramAnonymousView.putExtra("android.intent.extra.TEXT", ProfileActivity.this.currentChat.title + "\n" + ProfileActivity.this.info.about + "\nhttps://" + MessagesController.getInstance(ProfileActivity.this.currentAccount).linkPrefix + "/" + ProfileActivity.this.currentChat.username);
                  ProfileActivity.this.getParentActivity().startActivityForResult(Intent.createChooser(paramAnonymousView, LocaleController.getString("BotShare", 2131493094)), 500);
                  return;
                }
              }
              catch (Exception paramAnonymousView)
              {
                FileLog.e(paramAnonymousView);
                return;
              }
              paramAnonymousView.putExtra("android.intent.extra.TEXT", ProfileActivity.this.currentChat.title + "\nhttps://" + MessagesController.getInstance(ProfileActivity.this.currentAccount).linkPrefix + "/" + ProfileActivity.this.currentChat.username);
            }
          }
          if (paramAnonymousInt == ProfileActivity.this.leaveChannelRow)
          {
            ProfileActivity.this.leaveChatPressed();
            return;
          }
          if (paramAnonymousInt == ProfileActivity.this.membersRow)
          {
            paramAnonymousView = new Bundle();
            paramAnonymousView.putInt("chat_id", ProfileActivity.this.chat_id);
            paramAnonymousView.putInt("type", 2);
            ProfileActivity.this.presentFragment(new ChannelUsersActivity(paramAnonymousView));
            return;
          }
          if (paramAnonymousInt == ProfileActivity.this.convertRow)
          {
            paramAnonymousView = new AlertDialog.Builder(ProfileActivity.this.getParentActivity());
            paramAnonymousView.setMessage(LocaleController.getString("ConvertGroupAlert", 2131493297));
            paramAnonymousView.setTitle(LocaleController.getString("ConvertGroupAlertWarning", 2131493298));
            paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                MessagesController.getInstance(ProfileActivity.this.currentAccount).convertToMegaGroup(ProfileActivity.this.getParentActivity(), ProfileActivity.this.chat_id);
              }
            });
            paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            ProfileActivity.this.showDialog(paramAnonymousView.create());
            return;
          }
          ProfileActivity.this.processOnClickOrPress(paramAnonymousInt);
        }
      });
      this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(final View paramAnonymousView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt > ProfileActivity.this.emptyRowChat2) && (paramAnonymousInt < ProfileActivity.this.membersEndRow))
          {
            if (ProfileActivity.this.getParentActivity() == null) {
              return false;
            }
            int k = 0;
            int j = 0;
            int i = 0;
            if (!ProfileActivity.this.sortedUsers.isEmpty()) {}
            final TLRPC.ChannelParticipant localChannelParticipant;
            for (paramAnonymousView = (TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(((Integer)ProfileActivity.this.sortedUsers.get(paramAnonymousInt - ProfileActivity.this.emptyRowChat2 - 1)).intValue());; paramAnonymousView = (TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(paramAnonymousInt - ProfileActivity.this.emptyRowChat2 - 1))
            {
              ProfileActivity.access$7502(ProfileActivity.this, paramAnonymousView.user_id);
              if (!ChatObject.isChannel(ProfileActivity.this.currentChat)) {
                break label434;
              }
              localChannelParticipant = ((TLRPC.TL_chatChannelParticipant)paramAnonymousView).channelParticipant;
              if (paramAnonymousView.user_id != UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId()) {
                break;
              }
              return false;
            }
            MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymousView.user_id));
            if (((localChannelParticipant instanceof TLRPC.TL_channelParticipant)) || ((localChannelParticipant instanceof TLRPC.TL_channelParticipantBanned)))
            {
              paramAnonymousInt = 1;
              if ((((localChannelParticipant instanceof TLRPC.TL_channelParticipantAdmin)) || ((localChannelParticipant instanceof TLRPC.TL_channelParticipantCreator))) && (!localChannelParticipant.can_edit)) {
                break label426;
              }
              i = 1;
              j = paramAnonymousInt;
            }
            AlertDialog.Builder localBuilder;
            ArrayList localArrayList1;
            final ArrayList localArrayList2;
            for (;;)
            {
              localBuilder = new AlertDialog.Builder(ProfileActivity.this.getParentActivity());
              localArrayList1 = new ArrayList();
              localArrayList2 = new ArrayList();
              if (!ProfileActivity.this.currentChat.megagroup) {
                break label545;
              }
              if ((j != 0) && (ChatObject.canAddAdmins(ProfileActivity.this.currentChat)))
              {
                localArrayList1.add(LocaleController.getString("SetAsAdmin", 2131494376));
                localArrayList2.add(Integer.valueOf(0));
              }
              if ((ChatObject.canBlockUsers(ProfileActivity.this.currentChat)) && (i != 0))
              {
                localArrayList1.add(LocaleController.getString("KickFromSupergroup", 2131493720));
                localArrayList2.add(Integer.valueOf(1));
                localArrayList1.add(LocaleController.getString("KickFromGroup", 2131493719));
                localArrayList2.add(Integer.valueOf(2));
              }
              if (!localArrayList1.isEmpty()) {
                break label597;
              }
              return false;
              paramAnonymousInt = 0;
              break;
              label426:
              i = 0;
              j = paramAnonymousInt;
              continue;
              label434:
              localChannelParticipant = null;
              paramAnonymousInt = k;
              if (paramAnonymousView.user_id != UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId())
              {
                if (!ProfileActivity.this.currentChat.creator) {
                  break label481;
                }
                paramAnonymousInt = 1;
              }
              while (paramAnonymousInt == 0)
              {
                return false;
                label481:
                paramAnonymousInt = k;
                if ((paramAnonymousView instanceof TLRPC.TL_chatParticipant)) {
                  if ((!ProfileActivity.this.currentChat.admin) || (!ProfileActivity.this.currentChat.admins_enabled))
                  {
                    paramAnonymousInt = k;
                    if (paramAnonymousView.inviter_id != UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId()) {}
                  }
                  else
                  {
                    paramAnonymousInt = 1;
                  }
                }
              }
            }
            label545:
            if (ProfileActivity.this.chat_id > 0) {}
            for (String str = LocaleController.getString("KickFromGroup", 2131493719);; str = LocaleController.getString("KickFromBroadcast", 2131493718))
            {
              localArrayList1.add(str);
              localArrayList2.add(Integer.valueOf(2));
              break;
            }
            label597:
            localBuilder.setItems((CharSequence[])localArrayList1.toArray(new CharSequence[localArrayList1.size()]), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, final int paramAnonymous2Int)
              {
                if (((Integer)localArrayList2.get(paramAnonymous2Int)).intValue() == 2)
                {
                  ProfileActivity.this.kickUser(ProfileActivity.this.selectedUser);
                  return;
                }
                paramAnonymous2DialogInterface = new ChannelRightsEditActivity(paramAnonymousView.user_id, ProfileActivity.this.chat_id, localChannelParticipant.admin_rights, localChannelParticipant.banned_rights, ((Integer)localArrayList2.get(paramAnonymous2Int)).intValue(), true);
                paramAnonymous2DialogInterface.setDelegate(new ChannelRightsEditActivity.ChannelRightsEditActivityDelegate()
                {
                  public void didSetRights(int paramAnonymous3Int, TLRPC.TL_channelAdminRights paramAnonymous3TL_channelAdminRights, TLRPC.TL_channelBannedRights paramAnonymous3TL_channelBannedRights)
                  {
                    if (((Integer)ProfileActivity.10.1.this.val$actions.get(paramAnonymous2Int)).intValue() == 0)
                    {
                      localTL_chatChannelParticipant = (TLRPC.TL_chatChannelParticipant)ProfileActivity.10.1.this.val$user;
                      if (paramAnonymous3Int == 1)
                      {
                        localTL_chatChannelParticipant.channelParticipant = new TLRPC.TL_channelParticipantAdmin();
                        localTL_chatChannelParticipant.channelParticipant.inviter_id = UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId();
                        localTL_chatChannelParticipant.channelParticipant.user_id = ProfileActivity.10.1.this.val$user.user_id;
                        localTL_chatChannelParticipant.channelParticipant.date = ProfileActivity.10.1.this.val$user.date;
                        localTL_chatChannelParticipant.channelParticipant.banned_rights = paramAnonymous3TL_channelBannedRights;
                        localTL_chatChannelParticipant.channelParticipant.admin_rights = paramAnonymous3TL_channelAdminRights;
                      }
                    }
                    while ((((Integer)ProfileActivity.10.1.this.val$actions.get(paramAnonymous2Int)).intValue() != 1) || (paramAnonymous3Int != 0) || (!ProfileActivity.this.currentChat.megagroup) || (ProfileActivity.this.info == null) || (ProfileActivity.this.info.participants == null)) {
                      for (;;)
                      {
                        TLRPC.TL_chatChannelParticipant localTL_chatChannelParticipant;
                        return;
                        localTL_chatChannelParticipant.channelParticipant = new TLRPC.TL_channelParticipant();
                      }
                    }
                    int j = 0;
                    int i = 0;
                    label237:
                    paramAnonymous3Int = j;
                    if (i < ProfileActivity.this.info.participants.participants.size())
                    {
                      if (((TLRPC.TL_chatChannelParticipant)ProfileActivity.this.info.participants.participants.get(i)).channelParticipant.user_id == ProfileActivity.10.1.this.val$user.user_id)
                      {
                        if (ProfileActivity.this.info != null)
                        {
                          paramAnonymous3TL_channelAdminRights = ProfileActivity.this.info;
                          paramAnonymous3TL_channelAdminRights.participants_count -= 1;
                        }
                        ProfileActivity.this.info.participants.participants.remove(i);
                        paramAnonymous3Int = 1;
                      }
                    }
                    else
                    {
                      j = paramAnonymous3Int;
                      if (ProfileActivity.this.info != null)
                      {
                        j = paramAnonymous3Int;
                        if (ProfileActivity.this.info.participants != null) {
                          i = 0;
                        }
                      }
                    }
                    for (;;)
                    {
                      j = paramAnonymous3Int;
                      if (i < ProfileActivity.this.info.participants.participants.size())
                      {
                        if (((TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(i)).user_id == ProfileActivity.10.1.this.val$user.user_id)
                        {
                          ProfileActivity.this.info.participants.participants.remove(i);
                          j = 1;
                        }
                      }
                      else
                      {
                        if (j == 0) {
                          break;
                        }
                        ProfileActivity.this.updateOnlineCount();
                        ProfileActivity.this.updateRowsIds();
                        ProfileActivity.this.listAdapter.notifyDataSetChanged();
                        return;
                        i += 1;
                        break label237;
                      }
                      i += 1;
                    }
                  }
                });
                ProfileActivity.this.presentFragment(paramAnonymous2DialogInterface);
              }
            });
            ProfileActivity.this.showDialog(localBuilder.create());
            return true;
          }
          return ProfileActivity.this.processOnClickOrPress(paramAnonymousInt);
        }
      });
      if (this.banFromGroup == 0) {
        break label736;
      }
      if (this.currentChannelParticipant == null)
      {
        localObject1 = new TLRPC.TL_channels_getParticipant();
        ((TLRPC.TL_channels_getParticipant)localObject1).channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.banFromGroup);
        ((TLRPC.TL_channels_getParticipant)localObject1).user_id = MessagesController.getInstance(this.currentAccount).getInputUser(this.user_id);
        ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTLObject != null) {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  ProfileActivity.access$8402(ProfileActivity.this, ((TLRPC.TL_channels_channelParticipant)paramAnonymousTLObject).participant);
                }
              });
            }
          }
        });
      }
      localObject1 = new FrameLayout(paramContext)
      {
        protected void onDraw(Canvas paramAnonymousCanvas)
        {
          int i = Theme.chat_composeShadowDrawable.getIntrinsicHeight();
          Theme.chat_composeShadowDrawable.setBounds(0, 0, getMeasuredWidth(), i);
          Theme.chat_composeShadowDrawable.draw(paramAnonymousCanvas);
          paramAnonymousCanvas.drawRect(0.0F, i, getMeasuredWidth(), getMeasuredHeight(), Theme.chat_composeBackgroundPaint);
        }
      };
      ((FrameLayout)localObject1).setWillNotDraw(false);
      localFrameLayout.addView((View)localObject1, LayoutHelper.createFrame(-1, 51, 83));
      ((FrameLayout)localObject1).setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          int i = ProfileActivity.this.user_id;
          int j = ProfileActivity.this.banFromGroup;
          if (ProfileActivity.this.currentChannelParticipant != null) {}
          for (paramAnonymousView = ProfileActivity.this.currentChannelParticipant.banned_rights;; paramAnonymousView = null)
          {
            paramAnonymousView = new ChannelRightsEditActivity(i, j, null, paramAnonymousView, 1, true);
            paramAnonymousView.setDelegate(new ChannelRightsEditActivity.ChannelRightsEditActivityDelegate()
            {
              public void didSetRights(int paramAnonymous2Int, TLRPC.TL_channelAdminRights paramAnonymous2TL_channelAdminRights, TLRPC.TL_channelBannedRights paramAnonymous2TL_channelBannedRights)
              {
                ProfileActivity.this.removeSelfFromStack();
              }
            });
            ProfileActivity.this.presentFragment(paramAnonymousView);
            return;
          }
        }
      });
      localObject2 = new TextView(paramContext);
      ((TextView)localObject2).setTextColor(Theme.getColor("windowBackgroundWhiteRedText"));
      ((TextView)localObject2).setTextSize(1, 15.0F);
      ((TextView)localObject2).setGravity(17);
      ((TextView)localObject2).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      ((TextView)localObject2).setText(LocaleController.getString("BanFromTheGroup", 2131493078));
      ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(-2, -2.0F, 17, 0.0F, 1.0F, 0.0F, 0.0F));
      this.listView.setPadding(0, AndroidUtilities.dp(88.0F), 0, AndroidUtilities.dp(48.0F));
      this.listView.setBottomGlowOffset(AndroidUtilities.dp(48.0F));
      label543:
      this.topView = new TopView(paramContext);
      localObject1 = this.topView;
      if ((this.user_id == 0) && ((!ChatObject.isChannel(this.chat_id, this.currentAccount)) || (this.currentChat.megagroup))) {
        break label755;
      }
    }
    label736:
    label755:
    for (int i = 5;; i = this.chat_id)
    {
      ((TopView)localObject1).setBackgroundColor(AvatarDrawable.getProfileBackColorForId(i));
      localFrameLayout.addView(this.topView);
      localFrameLayout.addView(this.actionBar);
      this.avatarImage = new BackupImageView(paramContext);
      this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0F));
      this.avatarImage.setPivotX(0.0F);
      this.avatarImage.setPivotY(0.0F);
      localFrameLayout.addView(this.avatarImage, LayoutHelper.createFrame(42, 42.0F, 51, 64.0F, 0.0F, 0.0F, 0.0F));
      this.avatarImage.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ProfileActivity.this.user_id != 0)
          {
            paramAnonymousView = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
            if ((paramAnonymousView.photo != null) && (paramAnonymousView.photo.photo_big != null)) {
              PhotoViewer.getInstance().setParentActivity(ProfileActivity.this.getParentActivity());
            }
          }
          do
          {
            PhotoViewer.getInstance().openPhoto(paramAnonymousView.photo.photo_big, ProfileActivity.this.provider);
            do
            {
              return;
            } while (ProfileActivity.this.chat_id == 0);
            paramAnonymousView = MessagesController.getInstance(ProfileActivity.this.currentAccount).getChat(Integer.valueOf(ProfileActivity.this.chat_id));
          } while ((paramAnonymousView.photo == null) || (paramAnonymousView.photo.photo_big == null));
          PhotoViewer.getInstance().setParentActivity(ProfileActivity.this.getParentActivity());
          PhotoViewer.getInstance().openPhoto(paramAnonymousView.photo.photo_big, ProfileActivity.this.provider);
        }
      });
      i = 0;
      for (;;)
      {
        if (i >= 2) {
          break label1126;
        }
        if ((this.playProfileAnimation) || (i != 0)) {
          break;
        }
        i += 1;
      }
      i = this.chat_id;
      break;
      this.listView.setPadding(0, AndroidUtilities.dp(88.0F), 0, 0);
      break label543;
    }
    this.nameTextView[i] = new SimpleTextView(paramContext);
    label797:
    label883:
    label905:
    int j;
    if (i == 1)
    {
      this.nameTextView[i].setTextColor(Theme.getColor("profile_title"));
      this.nameTextView[i].setTextSize(18);
      this.nameTextView[i].setGravity(3);
      this.nameTextView[i].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.nameTextView[i].setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3F));
      this.nameTextView[i].setPivotX(0.0F);
      this.nameTextView[i].setPivotY(0.0F);
      localObject1 = this.nameTextView[i];
      if (i != 0) {
        break label1095;
      }
      f = 0.0F;
      ((SimpleTextView)localObject1).setAlpha(f);
      localObject1 = this.nameTextView[i];
      if (i != 0) {
        break label1100;
      }
      f = 48.0F;
      localFrameLayout.addView((View)localObject1, LayoutHelper.createFrame(-2, -2.0F, 51, 118.0F, 0.0F, f, 0.0F));
      this.onlineTextView[i] = new SimpleTextView(paramContext);
      localObject1 = this.onlineTextView[i];
      if ((this.user_id == 0) && ((!ChatObject.isChannel(this.chat_id, this.currentAccount)) || (this.currentChat.megagroup))) {
        break label1105;
      }
      j = 5;
      label984:
      ((SimpleTextView)localObject1).setTextColor(AvatarDrawable.getProfileTextColorForId(j));
      this.onlineTextView[i].setTextSize(14);
      this.onlineTextView[i].setGravity(3);
      localObject1 = this.onlineTextView[i];
      if (i != 0) {
        break label1114;
      }
      f = 0.0F;
      label1029:
      ((SimpleTextView)localObject1).setAlpha(f);
      localObject1 = this.onlineTextView[i];
      if (i != 0) {
        break label1119;
      }
    }
    label1095:
    label1100:
    label1105:
    label1114:
    label1119:
    for (float f = 48.0F;; f = 8.0F)
    {
      localFrameLayout.addView((View)localObject1, LayoutHelper.createFrame(-2, -2.0F, 51, 118.0F, 0.0F, f, 0.0F));
      break;
      this.nameTextView[i].setTextColor(Theme.getColor("actionBarDefaultTitle"));
      break label797;
      f = 1.0F;
      break label883;
      f = 0.0F;
      break label905;
      j = this.chat_id;
      break label984;
      f = 1.0F;
      break label1029;
    }
    label1126:
    if ((this.user_id != 0) || ((this.chat_id >= 0) && ((!ChatObject.isLeftFromChat(this.currentChat)) || (ChatObject.isChannel(this.currentChat)))))
    {
      this.writeButton = new ImageView(paramContext);
      localObject2 = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0F), Theme.getColor("profile_actionBackground"), Theme.getColor("profile_actionPressedBackground"));
      localObject1 = localObject2;
      if (Build.VERSION.SDK_INT < 21)
      {
        paramContext = paramContext.getResources().getDrawable(2131165323).mutate();
        paramContext.setColorFilter(new PorterDuffColorFilter(-16777216, PorterDuff.Mode.MULTIPLY));
        localObject1 = new CombinedDrawable(paramContext, (Drawable)localObject2, 0, 0);
        ((CombinedDrawable)localObject1).setIconSize(AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
      }
      this.writeButton.setBackgroundDrawable((Drawable)localObject1);
      this.writeButton.setScaleType(ImageView.ScaleType.CENTER);
      this.writeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("profile_actionIcon"), PorterDuff.Mode.MULTIPLY));
      if (this.user_id == 0) {
        break label1571;
      }
      this.writeButton.setImageResource(2131165320);
      this.writeButton.setPadding(0, AndroidUtilities.dp(3.0F), 0, 0);
      paramContext = this.writeButton;
      if (Build.VERSION.SDK_INT < 21) {
        break label1679;
      }
      i = 56;
      label1360:
      if (Build.VERSION.SDK_INT < 21) {
        break label1685;
      }
    }
    label1571:
    label1679:
    label1685:
    for (f = 56.0F;; f = 60.0F)
    {
      localFrameLayout.addView(paramContext, LayoutHelper.createFrame(i, f, 53, 0.0F, 0.0F, 16.0F, 0.0F));
      if (Build.VERSION.SDK_INT >= 21)
      {
        paramContext = new StateListAnimator();
        localObject1 = ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[] { AndroidUtilities.dp(2.0F), AndroidUtilities.dp(4.0F) }).setDuration(200L);
        paramContext.addState(new int[] { 16842919 }, (Animator)localObject1);
        localObject1 = ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[] { AndroidUtilities.dp(4.0F), AndroidUtilities.dp(2.0F) }).setDuration(200L);
        paramContext.addState(new int[0], (Animator)localObject1);
        this.writeButton.setStateListAnimator(paramContext);
        this.writeButton.setOutlineProvider(new ViewOutlineProvider()
        {
          @SuppressLint({"NewApi"})
          public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
          {
            paramAnonymousOutline.setOval(0, 0, AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
          }
        });
      }
      this.writeButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ProfileActivity.this.getParentActivity() == null) {}
          do
          {
            do
            {
              do
              {
                do
                {
                  return;
                  if (ProfileActivity.this.user_id == 0) {
                    break;
                  }
                  if ((ProfileActivity.this.playProfileAnimation) && ((ProfileActivity.this.parentLayout.fragmentsStack.get(ProfileActivity.this.parentLayout.fragmentsStack.size() - 2) instanceof ChatActivity)))
                  {
                    ProfileActivity.this.finishFragment();
                    return;
                  }
                  paramAnonymousView = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
                } while ((paramAnonymousView == null) || ((paramAnonymousView instanceof TLRPC.TL_userEmpty)));
                paramAnonymousView = new Bundle();
                paramAnonymousView.putInt("user_id", ProfileActivity.this.user_id);
              } while (!MessagesController.getInstance(ProfileActivity.this.currentAccount).checkCanOpenChat(paramAnonymousView, ProfileActivity.this));
              NotificationCenter.getInstance(ProfileActivity.this.currentAccount).removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
              NotificationCenter.getInstance(ProfileActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
              ProfileActivity.this.presentFragment(new ChatActivity(paramAnonymousView), true);
              return;
            } while (ProfileActivity.this.chat_id == 0);
            boolean bool = ChatObject.isChannel(ProfileActivity.this.currentChat);
            if (((!bool) || (ChatObject.canEditInfo(ProfileActivity.this.currentChat))) && ((bool) || (ProfileActivity.this.currentChat.admin) || (ProfileActivity.this.currentChat.creator) || (!ProfileActivity.this.currentChat.admins_enabled))) {
              break;
            }
            if ((ProfileActivity.this.playProfileAnimation) && ((ProfileActivity.this.parentLayout.fragmentsStack.get(ProfileActivity.this.parentLayout.fragmentsStack.size() - 2) instanceof ChatActivity)))
            {
              ProfileActivity.this.finishFragment();
              return;
            }
            paramAnonymousView = new Bundle();
            paramAnonymousView.putInt("chat_id", ProfileActivity.this.currentChat.id);
          } while (!MessagesController.getInstance(ProfileActivity.this.currentAccount).checkCanOpenChat(paramAnonymousView, ProfileActivity.this));
          NotificationCenter.getInstance(ProfileActivity.this.currentAccount).removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
          NotificationCenter.getInstance(ProfileActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
          ProfileActivity.this.presentFragment(new ChatActivity(paramAnonymousView), true);
          return;
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(ProfileActivity.this.getParentActivity());
          paramAnonymousView = MessagesController.getInstance(ProfileActivity.this.currentAccount).getChat(Integer.valueOf(ProfileActivity.this.chat_id));
          if ((paramAnonymousView.photo == null) || (paramAnonymousView.photo.photo_big == null) || ((paramAnonymousView.photo instanceof TLRPC.TL_chatPhotoEmpty)))
          {
            paramAnonymousView = new CharSequence[2];
            paramAnonymousView[0] = LocaleController.getString("FromCamera", 2131493613);
            paramAnonymousView[1] = LocaleController.getString("FromGalley", 2131493621);
          }
          for (;;)
          {
            localBuilder.setItems(paramAnonymousView, new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                if (paramAnonymous2Int == 0) {
                  ProfileActivity.this.avatarUpdater.openCamera();
                }
                do
                {
                  return;
                  if (paramAnonymous2Int == 1)
                  {
                    ProfileActivity.this.avatarUpdater.openGallery();
                    return;
                  }
                } while (paramAnonymous2Int != 2);
                MessagesController.getInstance(ProfileActivity.this.currentAccount).changeChatAvatar(ProfileActivity.this.chat_id, null);
              }
            });
            ProfileActivity.this.showDialog(localBuilder.create());
            return;
            paramAnonymousView = new CharSequence[3];
            paramAnonymousView[0] = LocaleController.getString("FromCamera", 2131493613);
            paramAnonymousView[1] = LocaleController.getString("FromGalley", 2131493621);
            paramAnonymousView[2] = LocaleController.getString("DeletePhoto", 2131493374);
          }
        }
      });
      needLayout();
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          ProfileActivity.this.checkListViewScroll();
          if ((ProfileActivity.this.participantsMap != null) && (ProfileActivity.this.loadMoreMembersRow != -1) && (ProfileActivity.this.layoutManager.findLastVisibleItemPosition() > ProfileActivity.this.loadMoreMembersRow - 8)) {
            ProfileActivity.this.getChannelParticipants(false);
          }
        }
      });
      return this.fragmentView;
      if (this.chat_id == 0) {
        break;
      }
      boolean bool = ChatObject.isChannel(this.currentChat);
      if (((bool) && (!ChatObject.canEditInfo(this.currentChat))) || ((!bool) && (!this.currentChat.admin) && (!this.currentChat.creator) && (this.currentChat.admins_enabled)))
      {
        this.writeButton.setImageResource(2131165320);
        this.writeButton.setPadding(0, AndroidUtilities.dp(3.0F), 0, 0);
        break;
      }
      this.writeButton.setImageResource(2131165319);
      break;
      i = 60;
      break label1360;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, final Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.updateInterfaces)
    {
      paramInt2 = ((Integer)paramVarArgs[0]).intValue();
      if (this.user_id != 0)
      {
        if (((paramInt2 & 0x2) != 0) || ((paramInt2 & 0x1) != 0) || ((paramInt2 & 0x4) != 0)) {
          updateProfileData();
        }
        if (((paramInt2 & 0x400) != 0) && (this.listView != null))
        {
          paramVarArgs = (RecyclerListView.Holder)this.listView.findViewHolderForPosition(this.phoneRow);
          if (paramVarArgs != null)
          {
            this.listAdapter.onBindViewHolder(paramVarArgs, this.phoneRow);
            break label92;
            break label92;
            break label92;
            break label92;
            break label92;
            break label92;
            break label92;
            break label92;
            break label92;
            break label92;
            break label92;
          }
        }
      }
    }
    for (;;)
    {
      label92:
      return;
      if (this.chat_id != 0)
      {
        if ((paramInt2 & 0x4000) != 0)
        {
          paramVarArgs = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
          if (paramVarArgs != null)
          {
            this.currentChat = paramVarArgs;
            createActionBarMenu();
            updateRowsIds();
            if (this.listAdapter != null) {
              this.listAdapter.notifyDataSetChanged();
            }
          }
        }
        if (((paramInt2 & 0x2000) != 0) || ((paramInt2 & 0x8) != 0) || ((paramInt2 & 0x10) != 0) || ((paramInt2 & 0x20) != 0) || ((paramInt2 & 0x4) != 0))
        {
          updateOnlineCount();
          updateProfileData();
        }
        if ((paramInt2 & 0x2000) != 0)
        {
          updateRowsIds();
          if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
          }
        }
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
        continue;
        if (paramInt1 == NotificationCenter.contactsDidLoaded)
        {
          createActionBarMenu();
          return;
        }
        if (paramInt1 == NotificationCenter.mediaCountDidLoaded)
        {
          long l3 = ((Long)paramVarArgs[0]).longValue();
          long l2 = this.dialog_id;
          long l1 = l2;
          if (l2 == 0L)
          {
            if (this.user_id != 0) {
              l1 = this.user_id;
            }
          }
          else
          {
            label360:
            if ((l3 != l1) && (l3 != this.mergeDialogId)) {
              break label486;
            }
            if (l3 != l1) {
              break label488;
            }
            this.totalMediaCount = ((Integer)paramVarArgs[1]).intValue();
            label399:
            if (this.listView == null) {
              break label502;
            }
            paramInt2 = this.listView.getChildCount();
            paramInt1 = 0;
          }
          for (;;)
          {
            if (paramInt1 >= paramInt2) {
              break label509;
            }
            paramVarArgs = this.listView.getChildAt(paramInt1);
            paramVarArgs = (RecyclerListView.Holder)this.listView.getChildViewHolder(paramVarArgs);
            if (paramVarArgs.getAdapterPosition() == this.sharedMediaRow)
            {
              this.listAdapter.onBindViewHolder(paramVarArgs, this.sharedMediaRow);
              return;
              l1 = l2;
              if (this.chat_id == 0) {
                break label360;
              }
              l1 = -this.chat_id;
              break label360;
              label486:
              break;
              label488:
              this.totalMediaCountMerge = ((Integer)paramVarArgs[1]).intValue();
              break label399;
              label502:
              break;
            }
            paramInt1 += 1;
          }
        }
        else
        {
          label509:
          if (paramInt1 == NotificationCenter.encryptedChatCreated)
          {
            if (!this.creatingChat) {
              break;
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(ProfileActivity.this.currentAccount).removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                NotificationCenter.getInstance(ProfileActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                TLRPC.EncryptedChat localEncryptedChat = (TLRPC.EncryptedChat)paramVarArgs[0];
                Bundle localBundle = new Bundle();
                localBundle.putInt("enc_id", localEncryptedChat.id);
                ProfileActivity.this.presentFragment(new ChatActivity(localBundle), true);
              }
            });
            return;
          }
          if (paramInt1 == NotificationCenter.encryptedChatUpdated)
          {
            paramVarArgs = (TLRPC.EncryptedChat)paramVarArgs[0];
            if ((this.currentEncryptedChat == null) || (paramVarArgs.id != this.currentEncryptedChat.id)) {
              break;
            }
            this.currentEncryptedChat = paramVarArgs;
            updateRowsIds();
            if (this.listAdapter == null) {
              break;
            }
            this.listAdapter.notifyDataSetChanged();
            return;
          }
          boolean bool;
          if (paramInt1 == NotificationCenter.blockedUsersDidLoaded)
          {
            bool = this.userBlocked;
            this.userBlocked = MessagesController.getInstance(this.currentAccount).blockedUsers.contains(Integer.valueOf(this.user_id));
            if (bool == this.userBlocked) {
              break;
            }
            createActionBarMenu();
            return;
          }
          Object localObject;
          if (paramInt1 == NotificationCenter.chatInfoDidLoaded)
          {
            localObject = (TLRPC.ChatFull)paramVarArgs[0];
            if (((TLRPC.ChatFull)localObject).id != this.chat_id) {
              break;
            }
            bool = ((Boolean)paramVarArgs[2]).booleanValue();
            if (((this.info instanceof TLRPC.TL_channelFull)) && (((TLRPC.ChatFull)localObject).participants == null) && (this.info != null)) {
              ((TLRPC.ChatFull)localObject).participants = this.info.participants;
            }
            if ((this.info == null) && ((localObject instanceof TLRPC.TL_channelFull))) {}
            for (paramInt1 = 1;; paramInt1 = 0)
            {
              this.info = ((TLRPC.ChatFull)localObject);
              if ((this.mergeDialogId == 0L) && (this.info.migrated_from_chat_id != 0))
              {
                this.mergeDialogId = (-this.info.migrated_from_chat_id);
                DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 0, this.classGuid, true);
              }
              fetchUsersFromChannelInfo();
              updateOnlineCount();
              updateRowsIds();
              if (this.listAdapter != null) {
                this.listAdapter.notifyDataSetChanged();
              }
              paramVarArgs = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
              if (paramVarArgs != null)
              {
                this.currentChat = paramVarArgs;
                createActionBarMenu();
              }
              if ((!this.currentChat.megagroup) || ((paramInt1 == 0) && (bool))) {
                break;
              }
              getChannelParticipants(true);
              return;
            }
          }
          if (paramInt1 == NotificationCenter.closeChats)
          {
            removeSelfFromStack();
            return;
          }
          if (paramInt1 == NotificationCenter.botInfoDidLoaded)
          {
            paramVarArgs = (TLRPC.BotInfo)paramVarArgs[0];
            if (paramVarArgs.user_id != this.user_id) {
              break;
            }
            this.botInfo = paramVarArgs;
            updateRowsIds();
            if (this.listAdapter == null) {
              break;
            }
            this.listAdapter.notifyDataSetChanged();
            return;
          }
          if (paramInt1 == NotificationCenter.userInfoDidLoaded)
          {
            if (((Integer)paramVarArgs[0]).intValue() != this.user_id) {
              break;
            }
            if ((!this.openAnimationInProgress) && (this.callItem == null)) {
              createActionBarMenu();
            }
            for (;;)
            {
              updateRowsIds();
              if (this.listAdapter == null) {
                break;
              }
              this.listAdapter.notifyDataSetChanged();
              return;
              this.recreateMenuAfterAnimation = true;
            }
          }
          if ((paramInt1 != NotificationCenter.didReceivedNewMessages) || (((Long)paramVarArgs[0]).longValue() != this.dialog_id)) {
            break;
          }
          paramVarArgs = (ArrayList)paramVarArgs[1];
          paramInt1 = 0;
          while (paramInt1 < paramVarArgs.size())
          {
            localObject = (MessageObject)paramVarArgs.get(paramInt1);
            if ((this.currentEncryptedChat != null) && (((MessageObject)localObject).messageOwner.action != null) && ((((MessageObject)localObject).messageOwner.action instanceof TLRPC.TL_messageEncryptedAction)) && ((((MessageObject)localObject).messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL)))
            {
              localObject = (TLRPC.TL_decryptedMessageActionSetMessageTTL)((MessageObject)localObject).messageOwner.action.encryptedAction;
              if (this.listAdapter != null) {
                this.listAdapter.notifyDataSetChanged();
              }
            }
            paramInt1 += 1;
          }
        }
      }
    }
  }
  
  public void didSelectDialogs(DialogsActivity paramDialogsActivity, ArrayList<Long> paramArrayList, CharSequence paramCharSequence, boolean paramBoolean)
  {
    long l = ((Long)paramArrayList.get(0)).longValue();
    paramArrayList = new Bundle();
    paramArrayList.putBoolean("scrollToTopOnResume", true);
    int i = (int)l;
    if (i != 0) {
      if (i > 0) {
        paramArrayList.putInt("user_id", i);
      }
    }
    while (!MessagesController.getInstance(this.currentAccount).checkCanOpenChat(paramArrayList, paramDialogsActivity))
    {
      return;
      if (i < 0)
      {
        paramArrayList.putInt("chat_id", -i);
        continue;
        paramArrayList.putInt("enc_id", (int)(l >> 32));
      }
    }
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
    presentFragment(new ChatActivity(paramArrayList), true);
    removeSelfFromStack();
    paramDialogsActivity = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
    SendMessagesHelper.getInstance(this.currentAccount).sendMessage(paramDialogsActivity, l, null, null, null);
  }
  
  public float getAnimationProgress()
  {
    return this.animationProgress;
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    Object localObject7 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (ProfileActivity.this.listView != null)
        {
          int j = ProfileActivity.this.listView.getChildCount();
          int i = 0;
          while (i < j)
          {
            View localView = ProfileActivity.this.listView.getChildAt(i);
            if ((localView instanceof UserCell)) {
              ((UserCell)localView).update(0);
            }
            i += 1;
          }
        }
      }
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, "actionBarDefaultSubmenuBackground");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, "actionBarDefaultSubmenuItem");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarBlue");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "avatar_backgroundActionBarBlue");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarBlue");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "avatar_actionBarSelectorBlue");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.nameTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "profile_title");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "avatar_subtitleInProfileBlue");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarRed");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "avatar_backgroundActionBarRed");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarRed");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "avatar_actionBarSelectorRed");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "avatar_subtitleInProfileRed");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "avatar_actionBarIconRed");
    ThemeDescription localThemeDescription16 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarOrange");
    ThemeDescription localThemeDescription17 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "avatar_backgroundActionBarOrange");
    ThemeDescription localThemeDescription18 = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarOrange");
    ThemeDescription localThemeDescription19 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "avatar_actionBarSelectorOrange");
    ThemeDescription localThemeDescription20 = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "avatar_subtitleInProfileOrange");
    ThemeDescription localThemeDescription21 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "avatar_actionBarIconOrange");
    ThemeDescription localThemeDescription22 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarViolet");
    ThemeDescription localThemeDescription23 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "avatar_backgroundActionBarViolet");
    ThemeDescription localThemeDescription24 = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarViolet");
    ThemeDescription localThemeDescription25 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "avatar_actionBarSelectorViolet");
    ThemeDescription localThemeDescription26 = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "avatar_subtitleInProfileViolet");
    ThemeDescription localThemeDescription27 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "avatar_actionBarIconViolet");
    ThemeDescription localThemeDescription28 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarGreen");
    ThemeDescription localThemeDescription29 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "avatar_backgroundActionBarGreen");
    ThemeDescription localThemeDescription30 = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarGreen");
    ThemeDescription localThemeDescription31 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "avatar_actionBarSelectorGreen");
    ThemeDescription localThemeDescription32 = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "avatar_subtitleInProfileGreen");
    ThemeDescription localThemeDescription33 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "avatar_actionBarIconGreen");
    ThemeDescription localThemeDescription34 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarCyan");
    ThemeDescription localThemeDescription35 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "avatar_backgroundActionBarCyan");
    ThemeDescription localThemeDescription36 = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarCyan");
    ThemeDescription localThemeDescription37 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "avatar_actionBarSelectorCyan");
    ThemeDescription localThemeDescription38 = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "avatar_subtitleInProfileCyan");
    ThemeDescription localThemeDescription39 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "avatar_actionBarIconCyan");
    ThemeDescription localThemeDescription40 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarPink");
    ThemeDescription localThemeDescription41 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "avatar_backgroundActionBarPink");
    ThemeDescription localThemeDescription42 = new ThemeDescription(this.topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarPink");
    ThemeDescription localThemeDescription43 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "avatar_actionBarSelectorPink");
    ThemeDescription localThemeDescription44 = new ThemeDescription(this.onlineTextView[1], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "avatar_subtitleInProfilePink");
    ThemeDescription localThemeDescription45 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "avatar_actionBarIconPink");
    ThemeDescription localThemeDescription46 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    Object localObject1 = this.listView;
    Object localObject2 = Theme.dividerPaint;
    localObject1 = new ThemeDescription((View)localObject1, 0, new Class[] { View.class }, (Paint)localObject2, null, null, "divider");
    localObject2 = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow");
    ThemeDescription localThemeDescription47 = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable }, null, "avatar_text");
    ThemeDescription localThemeDescription48 = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { this.avatarDrawable }, null, "avatar_backgroundInProfileRed");
    ThemeDescription localThemeDescription49 = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { this.avatarDrawable }, null, "avatar_backgroundInProfileOrange");
    ThemeDescription localThemeDescription50 = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { this.avatarDrawable }, null, "avatar_backgroundInProfileViolet");
    ThemeDescription localThemeDescription51 = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { this.avatarDrawable }, null, "avatar_backgroundInProfileGreen");
    ThemeDescription localThemeDescription52 = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { this.avatarDrawable }, null, "avatar_backgroundInProfileCyan");
    ThemeDescription localThemeDescription53 = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { this.avatarDrawable }, null, "avatar_backgroundInProfileBlue");
    ThemeDescription localThemeDescription54 = new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { this.avatarDrawable }, null, "avatar_backgroundInProfilePink");
    ThemeDescription localThemeDescription55 = new ThemeDescription(this.writeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, "profile_actionIcon");
    ThemeDescription localThemeDescription56 = new ThemeDescription(this.writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "profile_actionBackground");
    ThemeDescription localThemeDescription57 = new ThemeDescription(this.writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "profile_actionPressedBackground");
    ThemeDescription localThemeDescription58 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription59 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGreenText2");
    ThemeDescription localThemeDescription60 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteRedText5");
    ThemeDescription localThemeDescription61 = new ThemeDescription(this.listView, 0, new Class[] { TextCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText");
    ThemeDescription localThemeDescription62 = new ThemeDescription(this.listView, 0, new Class[] { TextCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayIcon");
    ThemeDescription localThemeDescription63 = new ThemeDescription(this.listView, 0, new Class[] { TextDetailCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription64 = new ThemeDescription(this.listView, 0, new Class[] { TextDetailCell.class }, new String[] { "valueImageView" }, null, null, null, "windowBackgroundWhiteGrayIcon");
    ThemeDescription localThemeDescription65 = new ThemeDescription(this.listView, 0, new Class[] { TextDetailCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayIcon");
    ThemeDescription localThemeDescription66 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[] { UserCell.class }, new String[] { "adminImage" }, null, null, null, "profile_creatorIcon");
    ThemeDescription localThemeDescription67 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[] { UserCell.class }, new String[] { "adminImage" }, null, null, null, "profile_adminIcon");
    ThemeDescription localThemeDescription68 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription69 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "statusColor" }, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "windowBackgroundWhiteGrayText");
    ThemeDescription localThemeDescription70 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "statusOnlineColor" }, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "windowBackgroundWhiteBlueText");
    Object localObject3 = this.listView;
    Object localObject4 = Theme.avatar_photoDrawable;
    Object localObject5 = Theme.avatar_broadcastDrawable;
    Object localObject6 = Theme.avatar_savedDrawable;
    localObject3 = new ThemeDescription((View)localObject3, 0, new Class[] { UserCell.class }, null, new Drawable[] { localObject4, localObject5, localObject6 }, null, "avatar_text");
    localObject4 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundRed");
    localObject5 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundOrange");
    localObject6 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundViolet");
    ThemeDescription localThemeDescription71 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundGreen");
    ThemeDescription localThemeDescription72 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundCyan");
    ThemeDescription localThemeDescription73 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundBlue");
    localObject7 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundPink");
    ThemeDescription localThemeDescription74 = new ThemeDescription(this.listView, 0, new Class[] { LoadingCell.class }, new String[] { "progressBar" }, null, null, null, "progressCircle");
    ThemeDescription localThemeDescription75 = new ThemeDescription(this.listView, 0, new Class[] { AboutLinkCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayIcon");
    Object localObject8 = this.listView;
    int i = ThemeDescription.FLAG_TEXTCOLOR;
    Object localObject9 = Theme.profile_aboutTextPaint;
    localObject8 = new ThemeDescription((View)localObject8, i, new Class[] { AboutLinkCell.class }, (Paint)localObject9, null, null, "windowBackgroundWhiteBlackText");
    localObject9 = this.listView;
    i = ThemeDescription.FLAG_LINKCOLOR;
    Object localObject10 = Theme.profile_aboutTextPaint;
    localObject9 = new ThemeDescription((View)localObject9, i, new Class[] { AboutLinkCell.class }, (Paint)localObject10, null, null, "windowBackgroundWhiteLinkText");
    localObject10 = this.listView;
    Paint localPaint = Theme.linkSelectionPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localThemeDescription11, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localThemeDescription16, localThemeDescription17, localThemeDescription18, localThemeDescription19, localThemeDescription20, localThemeDescription21, localThemeDescription22, localThemeDescription23, localThemeDescription24, localThemeDescription25, localThemeDescription26, localThemeDescription27, localThemeDescription28, localThemeDescription29, localThemeDescription30, localThemeDescription31, localThemeDescription32, localThemeDescription33, localThemeDescription34, localThemeDescription35, localThemeDescription36, localThemeDescription37, localThemeDescription38, localThemeDescription39, localThemeDescription40, localThemeDescription41, localThemeDescription42, localThemeDescription43, localThemeDescription44, localThemeDescription45, localThemeDescription46, localObject1, localObject2, localThemeDescription47, localThemeDescription48, localThemeDescription49, localThemeDescription50, localThemeDescription51, localThemeDescription52, localThemeDescription53, localThemeDescription54, localThemeDescription55, localThemeDescription56, localThemeDescription57, localThemeDescription58, localThemeDescription59, localThemeDescription60, localThemeDescription61, localThemeDescription62, localThemeDescription63, localThemeDescription64, localThemeDescription65, localThemeDescription66, localThemeDescription67, localThemeDescription68, localThemeDescription69, localThemeDescription70, localObject3, localObject4, localObject5, localObject6, localThemeDescription71, localThemeDescription72, localThemeDescription73, localObject7, localThemeDescription74, localThemeDescription75, localObject8, localObject9, new ThemeDescription((View)localObject10, 0, new Class[] { AboutLinkCell.class }, localPaint, null, null, "windowBackgroundWhiteLinkSelection"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGray"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGray"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.nameTextView[1], 0, null, null, new Drawable[] { Theme.profile_verifiedCheckDrawable }, null, "profile_verifiedCheck"), new ThemeDescription(this.nameTextView[1], 0, null, null, new Drawable[] { Theme.profile_verifiedDrawable }, null, "profile_verifiedBackground") };
  }
  
  public boolean isChat()
  {
    return this.chat_id != 0;
  }
  
  public void onActivityResultFragment(int paramInt1, int paramInt2, Intent paramIntent)
  {
    if (this.chat_id != 0) {
      this.avatarUpdater.onActivityResult(paramInt1, paramInt2, paramIntent);
    }
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    fixLayout();
  }
  
  protected AnimatorSet onCustomTransitionAnimation(boolean paramBoolean, final Runnable paramRunnable)
  {
    if ((this.playProfileAnimation) && (this.allowProfileAnimation))
    {
      final AnimatorSet localAnimatorSet = new AnimatorSet();
      localAnimatorSet.setDuration(180L);
      this.listView.setLayerType(2, null);
      Object localObject = this.actionBar.createMenu();
      if ((((ActionBarMenu)localObject).getItem(10) == null) && (this.animatingItem == null)) {
        this.animatingItem = ((ActionBarMenu)localObject).addItem(10, 2131165353);
      }
      int i;
      float f1;
      label419:
      SimpleTextView localSimpleTextView;
      if (paramBoolean)
      {
        localObject = (FrameLayout.LayoutParams)this.onlineTextView[1].getLayoutParams();
        ((FrameLayout.LayoutParams)localObject).rightMargin = ((int)(-21.0F * AndroidUtilities.density + AndroidUtilities.dp(8.0F)));
        this.onlineTextView[1].setLayoutParams((ViewGroup.LayoutParams)localObject);
        i = (int)Math.ceil(AndroidUtilities.displaySize.x - AndroidUtilities.dp(126.0F) + 21.0F * AndroidUtilities.density);
        f1 = this.nameTextView[1].getPaint().measureText(this.nameTextView[1].getText().toString());
        float f2 = this.nameTextView[1].getSideDrawablesSize();
        localObject = (FrameLayout.LayoutParams)this.nameTextView[1].getLayoutParams();
        if (i < f1 * 1.12F + f2)
        {
          ((FrameLayout.LayoutParams)localObject).width = ((int)Math.ceil(i / 1.12F));
          this.nameTextView[1].setLayoutParams((ViewGroup.LayoutParams)localObject);
          this.initialAnimationExtraHeight = AndroidUtilities.dp(88.0F);
          this.fragmentView.setBackgroundColor(0);
          setAnimationProgress(0.0F);
          localObject = new ArrayList();
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this, "animationProgress", new float[] { 0.0F, 1.0F }));
          if (this.writeButton != null)
          {
            this.writeButton.setScaleX(0.2F);
            this.writeButton.setScaleY(0.2F);
            this.writeButton.setAlpha(0.0F);
            ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[] { 1.0F }));
            ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[] { 1.0F }));
            ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[] { 1.0F }));
          }
          i = 0;
          if (i >= 2) {
            break label582;
          }
          localSimpleTextView = this.onlineTextView[i];
          if (i != 0) {
            break label562;
          }
          f1 = 1.0F;
          label441:
          localSimpleTextView.setAlpha(f1);
          localSimpleTextView = this.nameTextView[i];
          if (i != 0) {
            break label567;
          }
          f1 = 1.0F;
          label463:
          localSimpleTextView.setAlpha(f1);
          localSimpleTextView = this.onlineTextView[i];
          if (i != 0) {
            break label572;
          }
          f1 = 0.0F;
          label485:
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(localSimpleTextView, "alpha", new float[] { f1 }));
          localSimpleTextView = this.nameTextView[i];
          if (i != 0) {
            break label577;
          }
        }
        label562:
        label567:
        label572:
        label577:
        for (f1 = 0.0F;; f1 = 1.0F)
        {
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(localSimpleTextView, "alpha", new float[] { f1 }));
          i += 1;
          break label419;
          ((FrameLayout.LayoutParams)localObject).width = -2;
          break;
          f1 = 0.0F;
          break label441;
          f1 = 0.0F;
          break label463;
          f1 = 1.0F;
          break label485;
        }
        label582:
        if (this.animatingItem != null)
        {
          this.animatingItem.setAlpha(1.0F);
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.animatingItem, "alpha", new float[] { 0.0F }));
        }
        if (this.callItem != null)
        {
          this.callItem.setAlpha(0.0F);
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.callItem, "alpha", new float[] { 1.0F }));
        }
        if (this.editItem != null)
        {
          this.editItem.setAlpha(0.0F);
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.editItem, "alpha", new float[] { 1.0F }));
        }
        localAnimatorSet.playTogether((Collection)localObject);
      }
      for (;;)
      {
        localAnimatorSet.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            ProfileActivity.this.listView.setLayerType(0, null);
            if (ProfileActivity.this.animatingItem != null)
            {
              ProfileActivity.this.actionBar.createMenu().clearItems();
              ProfileActivity.access$12402(ProfileActivity.this, null);
            }
            paramRunnable.run();
          }
        });
        localAnimatorSet.setInterpolator(new DecelerateInterpolator());
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            localAnimatorSet.start();
          }
        }, 50L);
        return localAnimatorSet;
        this.initialAnimationExtraHeight = this.extraHeight;
        localObject = new ArrayList();
        ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this, "animationProgress", new float[] { 1.0F, 0.0F }));
        if (this.writeButton != null)
        {
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[] { 0.2F }));
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[] { 0.2F }));
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[] { 0.0F }));
        }
        i = 0;
        if (i < 2)
        {
          localSimpleTextView = this.onlineTextView[i];
          if (i == 0)
          {
            f1 = 1.0F;
            label894:
            ((ArrayList)localObject).add(ObjectAnimator.ofFloat(localSimpleTextView, "alpha", new float[] { f1 }));
            localSimpleTextView = this.nameTextView[i];
            if (i != 0) {
              break label966;
            }
          }
          label966:
          for (f1 = 1.0F;; f1 = 0.0F)
          {
            ((ArrayList)localObject).add(ObjectAnimator.ofFloat(localSimpleTextView, "alpha", new float[] { f1 }));
            i += 1;
            break;
            f1 = 0.0F;
            break label894;
          }
        }
        if (this.animatingItem != null)
        {
          this.animatingItem.setAlpha(0.0F);
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.animatingItem, "alpha", new float[] { 1.0F }));
        }
        if (this.callItem != null)
        {
          this.callItem.setAlpha(1.0F);
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.callItem, "alpha", new float[] { 0.0F }));
        }
        if (this.editItem != null)
        {
          this.editItem.setAlpha(1.0F);
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.editItem, "alpha", new float[] { 0.0F }));
        }
        localAnimatorSet.playTogether((Collection)localObject);
      }
    }
    return null;
  }
  
  protected void onDialogDismiss(Dialog paramDialog)
  {
    if (this.listView != null) {
      this.listView.invalidateViews();
    }
  }
  
  public boolean onFragmentCreate()
  {
    this.user_id = this.arguments.getInt("user_id", 0);
    this.chat_id = this.arguments.getInt("chat_id", 0);
    this.banFromGroup = this.arguments.getInt("ban_chat_id", 0);
    final Object localObject;
    if (this.user_id != 0)
    {
      this.dialog_id = this.arguments.getLong("dialog_id", 0L);
      if (this.dialog_id != 0L) {
        this.currentEncryptedChat = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf((int)(this.dialog_id >> 32)));
      }
      localObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
      if (localObject == null) {
        return false;
      }
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.contactsDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.encryptedChatCreated);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.encryptedChatUpdated);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.blockedUsersDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.botInfoDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.userInfoDidLoaded);
      if (this.currentEncryptedChat != null) {
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didReceivedNewMessages);
      }
      this.userBlocked = MessagesController.getInstance(this.currentAccount).blockedUsers.contains(Integer.valueOf(this.user_id));
      if (((TLRPC.User)localObject).bot)
      {
        this.isBot = true;
        DataQuery.getInstance(this.currentAccount).loadBotInfo(((TLRPC.User)localObject).id, true, this.classGuid);
      }
      MessagesController.getInstance(this.currentAccount).loadFullUser(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id)), this.classGuid, true);
      this.participantsMap = null;
      label336:
      if (this.dialog_id == 0L) {
        break label631;
      }
      DataQuery.getInstance(this.currentAccount).getMediaCount(this.dialog_id, 0, this.classGuid, true);
    }
    for (;;)
    {
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mediaCountDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
      updateRowsIds();
      return true;
      if (this.chat_id == 0) {
        break;
      }
      this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat_id));
      if (this.currentChat == null)
      {
        localObject = new CountDownLatch(1);
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
        {
          public void run()
          {
            ProfileActivity.access$902(ProfileActivity.this, MessagesStorage.getInstance(ProfileActivity.this.currentAccount).getChat(ProfileActivity.this.chat_id));
            localObject.countDown();
          }
        });
      }
      try
      {
        ((CountDownLatch)localObject).await();
        if (this.currentChat == null) {
          break;
        }
        MessagesController.getInstance(this.currentAccount).putChat(this.currentChat, true);
        if (this.currentChat.megagroup)
        {
          getChannelParticipants(true);
          NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
          this.sortedUsers = new ArrayList();
          updateOnlineCount();
          this.avatarUpdater = new AvatarUpdater();
          this.avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate()
          {
            public void didUploadedPhoto(TLRPC.InputFile paramAnonymousInputFile, TLRPC.PhotoSize paramAnonymousPhotoSize1, TLRPC.PhotoSize paramAnonymousPhotoSize2)
            {
              if (ProfileActivity.this.chat_id != 0) {
                MessagesController.getInstance(ProfileActivity.this.currentAccount).changeChatAvatar(ProfileActivity.this.chat_id, paramAnonymousInputFile);
              }
            }
          };
          this.avatarUpdater.parentFragment = this;
          if (!ChatObject.isChannel(this.currentChat)) {
            break label336;
          }
          MessagesController.getInstance(this.currentAccount).loadFullChat(this.chat_id, this.classGuid, true);
        }
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
          continue;
          this.participantsMap = null;
        }
      }
      label631:
      if (this.user_id != 0)
      {
        DataQuery.getInstance(this.currentAccount).getMediaCount(this.user_id, 0, this.classGuid, true);
      }
      else if (this.chat_id > 0)
      {
        DataQuery.getInstance(this.currentAccount).getMediaCount(-this.chat_id, 0, this.classGuid, true);
        if (this.mergeDialogId != 0L) {
          DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 0, this.classGuid, true);
        }
      }
    }
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mediaCountDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
    if (this.user_id != 0)
    {
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.contactsDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.encryptedChatCreated);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.encryptedChatUpdated);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.blockedUsersDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.botInfoDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.userInfoDidLoaded);
      MessagesController.getInstance(this.currentAccount).cancelLoadFullUser(this.user_id);
      if (this.currentEncryptedChat != null) {
        NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didReceivedNewMessages);
      }
    }
    while (this.chat_id == 0) {
      return;
    }
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoaded);
    this.avatarUpdater.clear();
  }
  
  public void onRequestPermissionsResultFragment(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if (paramInt == 101)
    {
      paramArrayOfString = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.user_id));
      if (paramArrayOfString != null) {}
    }
    else
    {
      return;
    }
    if ((paramArrayOfInt.length > 0) && (paramArrayOfInt[0] == 0))
    {
      VoIPHelper.startCall(paramArrayOfString, getParentActivity(), MessagesController.getInstance(this.currentAccount).getUserFull(paramArrayOfString.id));
      return;
    }
    VoIPHelper.permissionDenied(getParentActivity(), null);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    updateProfileData();
    fixLayout();
  }
  
  protected void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((!paramBoolean2) && (this.playProfileAnimation) && (this.allowProfileAnimation))
    {
      this.openAnimationInProgress = false;
      if (this.recreateMenuAfterAnimation) {
        createActionBarMenu();
      }
    }
    NotificationCenter.getInstance(this.currentAccount).setAnimationInProgress(false);
  }
  
  protected void onTransitionAnimationStart(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((!paramBoolean2) && (this.playProfileAnimation) && (this.allowProfileAnimation)) {
      this.openAnimationInProgress = true;
    }
    NotificationCenter.getInstance(this.currentAccount).setAllowedNotificationsDutingAnimation(new int[] { NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoaded });
    NotificationCenter.getInstance(this.currentAccount).setAnimationInProgress(true);
  }
  
  public void restoreSelfArgs(Bundle paramBundle)
  {
    if (this.chat_id != 0)
    {
      MessagesController.getInstance(this.currentAccount).loadChatInfo(this.chat_id, null, false);
      if (this.avatarUpdater != null) {
        this.avatarUpdater.currentPicturePath = paramBundle.getString("path");
      }
    }
  }
  
  public void saveSelfArgs(Bundle paramBundle)
  {
    if ((this.chat_id != 0) && (this.avatarUpdater != null) && (this.avatarUpdater.currentPicturePath != null)) {
      paramBundle.putString("path", this.avatarUpdater.currentPicturePath);
    }
  }
  
  @Keep
  public void setAnimationProgress(float paramFloat)
  {
    this.animationProgress = paramFloat;
    this.listView.setAlpha(paramFloat);
    this.listView.setTranslationX(AndroidUtilities.dp(48.0F) - AndroidUtilities.dp(48.0F) * paramFloat);
    int k;
    int m;
    int j;
    int n;
    int i1;
    label199:
    int i2;
    int i3;
    int i4;
    if ((this.user_id != 0) || ((ChatObject.isChannel(this.chat_id, this.currentAccount)) && (!this.currentChat.megagroup)))
    {
      i = 5;
      k = AvatarDrawable.getProfileBackColorForId(i);
      m = Theme.getColor("actionBarDefault");
      i = Color.red(m);
      j = Color.green(m);
      m = Color.blue(m);
      n = (int)((Color.red(k) - i) * paramFloat);
      i1 = (int)((Color.green(k) - j) * paramFloat);
      k = (int)((Color.blue(k) - m) * paramFloat);
      this.topView.setBackgroundColor(Color.rgb(i + n, j + i1, m + k));
      if ((this.user_id == 0) && ((!ChatObject.isChannel(this.chat_id, this.currentAccount)) || (this.currentChat.megagroup))) {
        break label420;
      }
      i = 5;
      k = AvatarDrawable.getIconColorForId(i);
      m = Theme.getColor("actionBarDefaultIcon");
      i = Color.red(m);
      j = Color.green(m);
      m = Color.blue(m);
      n = (int)((Color.red(k) - i) * paramFloat);
      i1 = (int)((Color.green(k) - j) * paramFloat);
      k = (int)((Color.blue(k) - m) * paramFloat);
      this.actionBar.setItemsColor(Color.rgb(i + n, j + i1, m + k), false);
      i = Theme.getColor("profile_title");
      n = Theme.getColor("actionBarDefaultTitle");
      j = Color.red(n);
      k = Color.green(n);
      m = Color.blue(n);
      n = Color.alpha(n);
      i1 = (int)((Color.red(i) - j) * paramFloat);
      i2 = (int)((Color.green(i) - k) * paramFloat);
      i3 = (int)((Color.blue(i) - m) * paramFloat);
      i4 = (int)((Color.alpha(i) - n) * paramFloat);
      i = 0;
      label391:
      if (i >= 2) {
        break label462;
      }
      if (this.nameTextView[i] != null) {
        break label428;
      }
    }
    for (;;)
    {
      i += 1;
      break label391;
      i = this.chat_id;
      break;
      label420:
      i = this.chat_id;
      break label199;
      label428:
      this.nameTextView[i].setTextColor(Color.argb(n + i4, j + i1, k + i2, m + i3));
    }
    label462:
    if ((this.user_id != 0) || ((ChatObject.isChannel(this.chat_id, this.currentAccount)) && (!this.currentChat.megagroup)))
    {
      i = 5;
      i = AvatarDrawable.getProfileTextColorForId(i);
      n = Theme.getColor("actionBarDefaultSubtitle");
      j = Color.red(n);
      k = Color.green(n);
      m = Color.blue(n);
      n = Color.alpha(n);
      i1 = (int)((Color.red(i) - j) * paramFloat);
      i2 = (int)((Color.green(i) - k) * paramFloat);
      i3 = (int)((Color.blue(i) - m) * paramFloat);
      i4 = (int)((Color.alpha(i) - n) * paramFloat);
      i = 0;
      label588:
      if (i >= 2) {
        break label651;
      }
      if (this.onlineTextView[i] != null) {
        break label617;
      }
    }
    for (;;)
    {
      i += 1;
      break label588;
      i = this.chat_id;
      break;
      label617:
      this.onlineTextView[i].setTextColor(Color.argb(n + i4, j + i1, k + i2, m + i3));
    }
    label651:
    this.extraHeight = ((int)(this.initialAnimationExtraHeight * paramFloat));
    if (this.user_id != 0)
    {
      i = this.user_id;
      j = AvatarDrawable.getProfileColorForId(i);
      if (this.user_id == 0) {
        break label796;
      }
    }
    label796:
    for (int i = this.user_id;; i = this.chat_id)
    {
      i = AvatarDrawable.getColorForId(i);
      if (j != i)
      {
        k = (int)((Color.red(j) - Color.red(i)) * paramFloat);
        m = (int)((Color.green(j) - Color.green(i)) * paramFloat);
        j = (int)((Color.blue(j) - Color.blue(i)) * paramFloat);
        this.avatarDrawable.setColor(Color.rgb(Color.red(i) + k, Color.green(i) + m, Color.blue(i) + j));
        this.avatarImage.invalidate();
      }
      needLayout();
      return;
      i = this.chat_id;
      break;
    }
  }
  
  public void setChatInfo(TLRPC.ChatFull paramChatFull)
  {
    this.info = paramChatFull;
    if ((this.info != null) && (this.info.migrated_from_chat_id != 0)) {
      this.mergeDialogId = (-this.info.migrated_from_chat_id);
    }
    fetchUsersFromChannelInfo();
  }
  
  public void setPlayProfileAnimation(boolean paramBoolean)
  {
    SharedPreferences localSharedPreferences = MessagesController.getGlobalMainSettings();
    if ((!AndroidUtilities.isTablet()) && (localSharedPreferences.getBoolean("view_animations", true))) {
      this.playProfileAnimation = paramBoolean;
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
      return ProfileActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == ProfileActivity.this.emptyRow) || (paramInt == ProfileActivity.this.emptyRowChat) || (paramInt == ProfileActivity.this.emptyRowChat2)) {}
      do
      {
        return 0;
        if ((paramInt == ProfileActivity.this.sectionRow) || (paramInt == ProfileActivity.this.userSectionRow)) {
          return 1;
        }
        if ((paramInt == ProfileActivity.this.phoneRow) || (paramInt == ProfileActivity.this.usernameRow) || (paramInt == ProfileActivity.this.channelNameRow) || (paramInt == ProfileActivity.this.userInfoDetailedRow)) {
          return 2;
        }
        if ((paramInt == ProfileActivity.this.leaveChannelRow) || (paramInt == ProfileActivity.this.sharedMediaRow) || (paramInt == ProfileActivity.this.settingsTimerRow) || (paramInt == ProfileActivity.this.settingsNotificationsRow) || (paramInt == ProfileActivity.this.startSecretChatRow) || (paramInt == ProfileActivity.this.settingsKeyRow) || (paramInt == ProfileActivity.this.convertRow) || (paramInt == ProfileActivity.this.addMemberRow) || (paramInt == ProfileActivity.this.groupsInCommonRow) || (paramInt == ProfileActivity.this.membersRow)) {
          return 3;
        }
        if ((paramInt > ProfileActivity.this.emptyRowChat2) && (paramInt < ProfileActivity.this.membersEndRow)) {
          return 4;
        }
        if (paramInt == ProfileActivity.this.membersSectionRow) {
          return 5;
        }
        if (paramInt == ProfileActivity.this.convertHelpRow) {
          return 6;
        }
        if (paramInt == ProfileActivity.this.loadMoreMembersRow) {
          return 7;
        }
      } while ((paramInt != ProfileActivity.this.userInfoRow) && (paramInt != ProfileActivity.this.channelInfoRow));
      return 8;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      boolean bool2 = false;
      int i = paramViewHolder.getAdapterPosition();
      boolean bool1;
      if (ProfileActivity.this.user_id != 0) {
        if ((i != ProfileActivity.this.phoneRow) && (i != ProfileActivity.this.settingsTimerRow) && (i != ProfileActivity.this.settingsKeyRow) && (i != ProfileActivity.this.settingsNotificationsRow) && (i != ProfileActivity.this.sharedMediaRow) && (i != ProfileActivity.this.startSecretChatRow) && (i != ProfileActivity.this.usernameRow) && (i != ProfileActivity.this.userInfoRow) && (i != ProfileActivity.this.groupsInCommonRow))
        {
          bool1 = bool2;
          if (i != ProfileActivity.this.userInfoDetailedRow) {}
        }
        else
        {
          bool1 = true;
        }
      }
      do
      {
        do
        {
          return bool1;
          bool1 = bool2;
        } while (ProfileActivity.this.chat_id == 0);
        if ((i == ProfileActivity.this.convertRow) || (i == ProfileActivity.this.settingsNotificationsRow) || (i == ProfileActivity.this.sharedMediaRow) || ((i > ProfileActivity.this.emptyRowChat2) && (i < ProfileActivity.this.membersEndRow)) || (i == ProfileActivity.this.addMemberRow) || (i == ProfileActivity.this.channelNameRow) || (i == ProfileActivity.this.leaveChannelRow) || (i == ProfileActivity.this.channelInfoRow)) {
          break;
        }
        bool1 = bool2;
      } while (i != ProfileActivity.this.membersRow);
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      Object localObject1;
      label1188:
      label1201:
      label1343:
      label1442:
      label2006:
      label2040:
      label2120:
      label2122:
      label2148:
      label2202:
      do
      {
        Object localObject2;
        do
        {
          do
          {
            return;
            if ((paramInt == ProfileActivity.this.emptyRowChat) || (paramInt == ProfileActivity.this.emptyRowChat2))
            {
              ((EmptyCell)paramViewHolder.itemView).setHeight(AndroidUtilities.dp(8.0F));
              return;
            }
            ((EmptyCell)paramViewHolder.itemView).setHeight(AndroidUtilities.dp(36.0F));
            return;
            localObject1 = (TextDetailCell)paramViewHolder.itemView;
            ((TextDetailCell)localObject1).setMultiline(false);
            if (paramInt == ProfileActivity.this.phoneRow)
            {
              paramViewHolder = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
              if ((paramViewHolder.phone != null) && (paramViewHolder.phone.length() != 0)) {}
              for (paramViewHolder = PhoneFormat.getInstance().format("+" + paramViewHolder.phone);; paramViewHolder = LocaleController.getString("NumberUnknown", 2131494027))
              {
                ((TextDetailCell)localObject1).setTextAndValueAndIcon(paramViewHolder, LocaleController.getString("PhoneMobile", 2131494152), 2131165614, 0);
                return;
              }
            }
            if (paramInt == ProfileActivity.this.usernameRow)
            {
              paramViewHolder = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(ProfileActivity.this.user_id));
              if ((paramViewHolder != null) && (!TextUtils.isEmpty(paramViewHolder.username))) {}
              for (paramViewHolder = "@" + paramViewHolder.username; (ProfileActivity.this.phoneRow == -1) && (ProfileActivity.this.userInfoRow == -1) && (ProfileActivity.this.userInfoDetailedRow == -1); paramViewHolder = "-")
              {
                ((TextDetailCell)localObject1).setTextAndValueAndIcon(paramViewHolder, LocaleController.getString("Username", 2131494556), 2131165612, 11);
                return;
              }
              ((TextDetailCell)localObject1).setTextAndValue(paramViewHolder, LocaleController.getString("Username", 2131494556));
              return;
            }
            if (paramInt == ProfileActivity.this.channelNameRow)
            {
              if ((ProfileActivity.this.currentChat != null) && (!TextUtils.isEmpty(ProfileActivity.this.currentChat.username))) {}
              for (paramViewHolder = "@" + ProfileActivity.this.currentChat.username;; paramViewHolder = "-")
              {
                ((TextDetailCell)localObject1).setTextAndValue(paramViewHolder, MessagesController.getInstance(ProfileActivity.this.currentAccount).linkPrefix + "/" + ProfileActivity.this.currentChat.username);
                return;
              }
            }
          } while (paramInt != ProfileActivity.this.userInfoDetailedRow);
          paramViewHolder = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.user_id);
          ((TextDetailCell)localObject1).setMultiline(true);
          if (paramViewHolder != null) {}
          for (paramViewHolder = paramViewHolder.about;; paramViewHolder = null)
          {
            ((TextDetailCell)localObject1).setTextAndValueAndIcon(paramViewHolder, LocaleController.getString("UserBio", 2131494541), 2131165612, 11);
            return;
          }
          localObject1 = (TextCell)paramViewHolder.itemView;
          ((TextCell)localObject1).setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
          ((TextCell)localObject1).setTag("windowBackgroundWhiteBlackText");
          int i;
          if (paramInt == ProfileActivity.this.sharedMediaRow)
          {
            if (ProfileActivity.this.totalMediaCount == -1)
            {
              paramViewHolder = LocaleController.getString("Loading", 2131493762);
              if ((ProfileActivity.this.user_id != 0) && (UserConfig.getInstance(ProfileActivity.this.currentAccount).getClientUserId() == ProfileActivity.this.user_id)) {
                ((TextCell)localObject1).setTextAndValueAndIcon(LocaleController.getString("SharedMedia", 2131494395), paramViewHolder, 2131165613);
              }
            }
            else
            {
              i = ProfileActivity.this.totalMediaCount;
              if (ProfileActivity.this.totalMediaCountMerge != -1) {}
              for (paramInt = ProfileActivity.this.totalMediaCountMerge;; paramInt = 0)
              {
                paramViewHolder = String.format("%d", new Object[] { Integer.valueOf(paramInt + i) });
                break;
              }
            }
            ((TextCell)localObject1).setTextAndValue(LocaleController.getString("SharedMedia", 2131494395), paramViewHolder);
            return;
          }
          if (paramInt == ProfileActivity.this.groupsInCommonRow)
          {
            paramViewHolder = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.user_id);
            localObject2 = LocaleController.getString("GroupsInCommon", 2131493645);
            if (paramViewHolder != null) {}
            for (paramInt = paramViewHolder.common_chats_count;; paramInt = 0)
            {
              ((TextCell)localObject1).setTextAndValue((String)localObject2, String.format("%d", new Object[] { Integer.valueOf(paramInt) }));
              return;
            }
          }
          if (paramInt == ProfileActivity.this.settingsTimerRow)
          {
            paramViewHolder = MessagesController.getInstance(ProfileActivity.this.currentAccount).getEncryptedChat(Integer.valueOf((int)(ProfileActivity.this.dialog_id >> 32)));
            if (paramViewHolder.ttl == 0) {}
            for (paramViewHolder = LocaleController.getString("ShortMessageLifetimeForever", 2131494402);; paramViewHolder = LocaleController.formatTTLString(paramViewHolder.ttl))
            {
              ((TextCell)localObject1).setTextAndValue(LocaleController.getString("MessageLifetime", 2131493818), paramViewHolder);
              return;
            }
          }
          if (paramInt == ProfileActivity.this.settingsNotificationsRow)
          {
            paramViewHolder = MessagesController.getNotificationsSettings(ProfileActivity.this.currentAccount);
            long l;
            boolean bool2;
            boolean bool1;
            if (ProfileActivity.this.dialog_id != 0L)
            {
              l = ProfileActivity.this.dialog_id;
              bool2 = paramViewHolder.getBoolean("custom_" + l, false);
              bool1 = paramViewHolder.contains("notify2_" + l);
              paramInt = paramViewHolder.getInt("notify2_" + l, 0);
              i = paramViewHolder.getInt("notifyuntil_" + l, 0);
              if ((paramInt != 3) || (i == Integer.MAX_VALUE)) {
                break label1343;
              }
              paramInt = i - ConnectionsManager.getInstance(ProfileActivity.this.currentAccount).getCurrentTime();
              if (paramInt > 0) {
                break label1201;
              }
              if (!bool2) {
                break label1188;
              }
              paramViewHolder = LocaleController.getString("NotificationsCustom", 2131494006);
            }
            for (;;)
            {
              if (paramViewHolder != null)
              {
                ((TextCell)localObject1).setTextAndValueAndIcon(LocaleController.getString("Notifications", 2131494004), paramViewHolder, 2131165613);
                return;
                if (ProfileActivity.this.user_id != 0)
                {
                  l = ProfileActivity.this.user_id;
                  break;
                }
                l = -ProfileActivity.this.chat_id;
                break;
                paramViewHolder = LocaleController.getString("NotificationsOn", 2131494014);
                continue;
                if (paramInt < 3600)
                {
                  paramViewHolder = LocaleController.formatString("WillUnmuteIn", 2131494641, new Object[] { LocaleController.formatPluralString("Minutes", paramInt / 60) });
                }
                else if (paramInt < 86400)
                {
                  paramViewHolder = LocaleController.formatString("WillUnmuteIn", 2131494641, new Object[] { LocaleController.formatPluralString("Hours", (int)Math.ceil(paramInt / 60.0F / 60.0F)) });
                }
                else if (paramInt < 31536000)
                {
                  paramViewHolder = LocaleController.formatString("WillUnmuteIn", 2131494641, new Object[] { LocaleController.formatPluralString("Days", (int)Math.ceil(paramInt / 60.0F / 60.0F / 24.0F)) });
                }
                else
                {
                  paramViewHolder = null;
                  continue;
                  if (paramInt == 0) {
                    if (bool1) {
                      bool1 = true;
                    }
                  }
                  for (;;)
                  {
                    if ((!bool1) || (!bool2)) {
                      break label1442;
                    }
                    paramViewHolder = LocaleController.getString("NotificationsCustom", 2131494006);
                    break;
                    if ((int)l < 0)
                    {
                      bool1 = paramViewHolder.getBoolean("EnableGroup", true);
                    }
                    else
                    {
                      bool1 = paramViewHolder.getBoolean("EnableAll", true);
                      continue;
                      if (paramInt == 1) {
                        bool1 = true;
                      } else if (paramInt == 2) {
                        bool1 = false;
                      } else {
                        bool1 = false;
                      }
                    }
                  }
                  if (bool1) {}
                  for (paramViewHolder = LocaleController.getString("NotificationsOn", 2131494014);; paramViewHolder = LocaleController.getString("NotificationsOff", 2131494013)) {
                    break;
                  }
                }
              }
            }
            ((TextCell)localObject1).setTextAndValueAndIcon(LocaleController.getString("Notifications", 2131494004), LocaleController.getString("NotificationsOff", 2131494013), 2131165613);
            return;
          }
          if (paramInt == ProfileActivity.this.startSecretChatRow)
          {
            ((TextCell)localObject1).setText(LocaleController.getString("StartEncryptedChat", 2131494421));
            ((TextCell)localObject1).setTag("windowBackgroundWhiteGreenText2");
            ((TextCell)localObject1).setTextColor(Theme.getColor("windowBackgroundWhiteGreenText2"));
            return;
          }
          if (paramInt == ProfileActivity.this.settingsKeyRow)
          {
            paramViewHolder = new IdenticonDrawable();
            paramViewHolder.setEncryptedChat(MessagesController.getInstance(ProfileActivity.this.currentAccount).getEncryptedChat(Integer.valueOf((int)(ProfileActivity.this.dialog_id >> 32))));
            ((TextCell)localObject1).setTextAndValueDrawable(LocaleController.getString("EncryptionKey", 2131493438), paramViewHolder);
            return;
          }
          if (paramInt == ProfileActivity.this.leaveChannelRow)
          {
            ((TextCell)localObject1).setTag("windowBackgroundWhiteRedText5");
            ((TextCell)localObject1).setTextColor(Theme.getColor("windowBackgroundWhiteRedText5"));
            ((TextCell)localObject1).setText(LocaleController.getString("LeaveChannel", 2131493741));
            return;
          }
          if (paramInt == ProfileActivity.this.convertRow)
          {
            ((TextCell)localObject1).setText(LocaleController.getString("UpgradeGroup", 2131494528));
            ((TextCell)localObject1).setTag("windowBackgroundWhiteGreenText2");
            ((TextCell)localObject1).setTextColor(Theme.getColor("windowBackgroundWhiteGreenText2"));
            return;
          }
          if (paramInt == ProfileActivity.this.addMemberRow)
          {
            if (ProfileActivity.this.chat_id > 0)
            {
              ((TextCell)localObject1).setText(LocaleController.getString("AddMember", 2131492932));
              return;
            }
            ((TextCell)localObject1).setText(LocaleController.getString("AddRecipient", 2131492934));
            return;
          }
        } while (paramInt != ProfileActivity.this.membersRow);
        if (ProfileActivity.this.info != null)
        {
          if ((ChatObject.isChannel(ProfileActivity.this.currentChat)) && (!ProfileActivity.this.currentChat.megagroup))
          {
            ((TextCell)localObject1).setTextAndValue(LocaleController.getString("ChannelSubscribers", 2131493210), String.format("%d", new Object[] { Integer.valueOf(ProfileActivity.this.info.participants_count) }));
            return;
          }
          ((TextCell)localObject1).setTextAndValue(LocaleController.getString("ChannelMembers", 2131493177), String.format("%d", new Object[] { Integer.valueOf(ProfileActivity.this.info.participants_count) }));
          return;
        }
        if ((ChatObject.isChannel(ProfileActivity.this.currentChat)) && (!ProfileActivity.this.currentChat.megagroup))
        {
          ((TextCell)localObject1).setText(LocaleController.getString("ChannelSubscribers", 2131493210));
          return;
        }
        ((TextCell)localObject1).setText(LocaleController.getString("ChannelMembers", 2131493177));
        return;
        localObject1 = (UserCell)paramViewHolder.itemView;
        if (!ProfileActivity.this.sortedUsers.isEmpty())
        {
          paramViewHolder = (TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(((Integer)ProfileActivity.this.sortedUsers.get(paramInt - ProfileActivity.this.emptyRowChat2 - 1)).intValue());
          if (paramViewHolder == null) {
            break label2120;
          }
          if (!(paramViewHolder instanceof TLRPC.TL_chatChannelParticipant)) {
            break label2148;
          }
          localObject2 = ((TLRPC.TL_chatChannelParticipant)paramViewHolder).channelParticipant;
          if (!(localObject2 instanceof TLRPC.TL_channelParticipantCreator)) {
            break label2122;
          }
          ((UserCell)localObject1).setIsAdmin(1);
          paramViewHolder = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUser(Integer.valueOf(paramViewHolder.user_id));
          if (paramInt != ProfileActivity.this.emptyRowChat2 + 1) {
            break label2202;
          }
        }
        for (paramInt = 2131165497;; paramInt = 0)
        {
          ((UserCell)localObject1).setData(paramViewHolder, null, null, paramInt);
          return;
          paramViewHolder = (TLRPC.ChatParticipant)ProfileActivity.this.info.participants.participants.get(paramInt - ProfileActivity.this.emptyRowChat2 - 1);
          break label2006;
          break;
          if ((localObject2 instanceof TLRPC.TL_channelParticipantAdmin))
          {
            ((UserCell)localObject1).setIsAdmin(2);
            break label2040;
          }
          ((UserCell)localObject1).setIsAdmin(0);
          break label2040;
          if ((paramViewHolder instanceof TLRPC.TL_chatParticipantCreator))
          {
            ((UserCell)localObject1).setIsAdmin(1);
            break label2040;
          }
          if ((ProfileActivity.this.currentChat.admins_enabled) && ((paramViewHolder instanceof TLRPC.TL_chatParticipantAdmin)))
          {
            ((UserCell)localObject1).setIsAdmin(2);
            break label2040;
          }
          ((UserCell)localObject1).setIsAdmin(0);
          break label2040;
        }
        localObject1 = (AboutLinkCell)paramViewHolder.itemView;
        if (paramInt == ProfileActivity.this.userInfoRow)
        {
          paramViewHolder = MessagesController.getInstance(ProfileActivity.this.currentAccount).getUserFull(ProfileActivity.this.user_id);
          if (paramViewHolder != null) {}
          for (paramViewHolder = paramViewHolder.about;; paramViewHolder = null)
          {
            ((AboutLinkCell)localObject1).setTextAndIcon(paramViewHolder, 2131165612, ProfileActivity.this.isBot);
            return;
          }
        }
      } while (paramInt != ProfileActivity.this.channelInfoRow);
      for (paramViewHolder = ProfileActivity.this.info.about; paramViewHolder.contains("\n\n\n"); paramViewHolder = paramViewHolder.replace("\n\n\n", "\n\n")) {}
      ((AboutLinkCell)localObject1).setTextAndIcon(paramViewHolder, 2131165612, true);
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      switch (paramInt)
      {
      }
      for (;;)
      {
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new EmptyCell(this.mContext);
        continue;
        paramViewGroup = new DividerCell(this.mContext);
        paramViewGroup.setPadding(AndroidUtilities.dp(72.0F), 0, 0, 0);
        continue;
        paramViewGroup = new TextDetailCell(this.mContext);
        continue;
        paramViewGroup = new TextCell(this.mContext);
        continue;
        paramViewGroup = new UserCell(this.mContext, 61, 0, true);
        continue;
        paramViewGroup = new ShadowSectionCell(this.mContext);
        Object localObject1 = Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow");
        localObject1 = new CombinedDrawable(new ColorDrawable(Theme.getColor("windowBackgroundGray")), (Drawable)localObject1);
        ((CombinedDrawable)localObject1).setFullsize(true);
        paramViewGroup.setBackgroundDrawable((Drawable)localObject1);
        continue;
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
        localObject1 = (TextInfoPrivacyCell)paramViewGroup;
        Object localObject2 = Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow");
        localObject2 = new CombinedDrawable(new ColorDrawable(Theme.getColor("windowBackgroundGray")), (Drawable)localObject2);
        ((CombinedDrawable)localObject2).setFullsize(true);
        ((TextInfoPrivacyCell)localObject1).setBackgroundDrawable((Drawable)localObject2);
        ((TextInfoPrivacyCell)localObject1).setText(AndroidUtilities.replaceTags(LocaleController.formatString("ConvertGroupInfo", 2131493299, new Object[] { LocaleController.formatPluralString("Members", MessagesController.getInstance(ProfileActivity.this.currentAccount).maxMegagroupCount) })));
        continue;
        paramViewGroup = new LoadingCell(this.mContext);
        continue;
        paramViewGroup = new AboutLinkCell(this.mContext);
        ((AboutLinkCell)paramViewGroup).setDelegate(new AboutLinkCell.AboutLinkCellDelegate()
        {
          public void didPressUrl(String paramAnonymousString)
          {
            if (paramAnonymousString.startsWith("@")) {
              MessagesController.getInstance(ProfileActivity.this.currentAccount).openByUserName(paramAnonymousString.substring(1), ProfileActivity.this, 0);
            }
            Object localObject;
            do
            {
              do
              {
                return;
                if (paramAnonymousString.startsWith("#"))
                {
                  localObject = new DialogsActivity(null);
                  ((DialogsActivity)localObject).setSearchString(paramAnonymousString);
                  ProfileActivity.this.presentFragment((BaseFragment)localObject);
                  return;
                }
              } while ((!paramAnonymousString.startsWith("/")) || (ProfileActivity.this.parentLayout.fragmentsStack.size() <= 1));
              localObject = (BaseFragment)ProfileActivity.this.parentLayout.fragmentsStack.get(ProfileActivity.this.parentLayout.fragmentsStack.size() - 2);
            } while (!(localObject instanceof ChatActivity));
            ProfileActivity.this.finishFragment();
            ((ChatActivity)localObject).chatActivityEnterView.setCommand(null, paramAnonymousString, false, false);
          }
        });
      }
    }
  }
  
  private class TopView
    extends View
  {
    private int currentColor;
    private Paint paint = new Paint();
    
    public TopView(Context paramContext)
    {
      super();
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      int i = getMeasuredHeight() - AndroidUtilities.dp(91.0F);
      paramCanvas.drawRect(0.0F, 0.0F, getMeasuredWidth(), ProfileActivity.this.extraHeight + i, this.paint);
      if (ProfileActivity.this.parentLayout != null) {
        ProfileActivity.this.parentLayout.drawHeaderShadow(paramCanvas, ProfileActivity.this.extraHeight + i);
      }
    }
    
    protected void onMeasure(int paramInt1, int paramInt2)
    {
      paramInt2 = View.MeasureSpec.getSize(paramInt1);
      int i = ActionBar.getCurrentActionBarHeight();
      if (ProfileActivity.this.actionBar.getOccupyStatusBar()) {}
      for (paramInt1 = AndroidUtilities.statusBarHeight;; paramInt1 = 0)
      {
        setMeasuredDimension(paramInt2, paramInt1 + i + AndroidUtilities.dp(91.0F));
        return;
      }
    }
    
    public void setBackgroundColor(int paramInt)
    {
      if (paramInt != this.currentColor)
      {
        this.paint.setColor(paramInt);
        invalidate();
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ProfileActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */