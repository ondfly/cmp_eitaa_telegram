package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.MediaActivity;
import org.telegram.ui.ProfileActivity;

public class ChatAvatarContainer
  extends FrameLayout
  implements NotificationCenter.NotificationCenterDelegate
{
  private AvatarDrawable avatarDrawable = new AvatarDrawable();
  private BackupImageView avatarImageView;
  private int currentAccount = UserConfig.selectedAccount;
  private int currentConnectionState;
  private CharSequence lastSubtitle;
  private boolean occupyStatusBar = true;
  private int onlineCount = -1;
  private ChatActivity parentFragment;
  private StatusDrawable[] statusDrawables = new StatusDrawable[5];
  private SimpleTextView subtitleTextView;
  private ImageView timeItem;
  private TimerDrawable timerDrawable;
  private SimpleTextView titleTextView;
  
  public ChatAvatarContainer(Context paramContext, ChatActivity paramChatActivity, boolean paramBoolean)
  {
    super(paramContext);
    this.parentFragment = paramChatActivity;
    this.avatarImageView = new BackupImageView(paramContext);
    this.avatarImageView.setRoundRadius(AndroidUtilities.dp(21.0F));
    addView(this.avatarImageView);
    this.titleTextView = new SimpleTextView(paramContext);
    this.titleTextView.setTextColor(Theme.getColor("actionBarDefaultTitle"));
    this.titleTextView.setTextSize(18);
    this.titleTextView.setGravity(3);
    this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.titleTextView.setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3F));
    addView(this.titleTextView);
    this.subtitleTextView = new SimpleTextView(paramContext);
    this.subtitleTextView.setTextColor(Theme.getColor("actionBarDefaultSubtitle"));
    this.subtitleTextView.setTextSize(14);
    this.subtitleTextView.setGravity(3);
    addView(this.subtitleTextView);
    if (paramBoolean)
    {
      this.timeItem = new ImageView(paramContext);
      this.timeItem.setPadding(AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(5.0F), AndroidUtilities.dp(5.0F));
      this.timeItem.setScaleType(ImageView.ScaleType.CENTER);
      paramChatActivity = this.timeItem;
      paramContext = new TimerDrawable(paramContext);
      this.timerDrawable = paramContext;
      paramChatActivity.setImageDrawable(paramContext);
      addView(this.timeItem);
      this.timeItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          ChatAvatarContainer.this.parentFragment.showDialog(AlertsCreator.createTTLAlert(ChatAvatarContainer.this.getContext(), ChatAvatarContainer.this.parentFragment.getCurrentEncryptedChat()).create());
        }
      });
    }
    if (this.parentFragment != null)
    {
      setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = ChatAvatarContainer.this.parentFragment.getCurrentUser();
          Object localObject = ChatAvatarContainer.this.parentFragment.getCurrentChat();
          if (paramAnonymousView != null)
          {
            localObject = new Bundle();
            if (UserObject.isUserSelf(paramAnonymousView))
            {
              ((Bundle)localObject).putLong("dialog_id", ChatAvatarContainer.this.parentFragment.getDialogId());
              paramAnonymousView = new MediaActivity((Bundle)localObject);
              paramAnonymousView.setChatInfo(ChatAvatarContainer.this.parentFragment.getCurrentChatInfo());
              ChatAvatarContainer.this.parentFragment.presentFragment(paramAnonymousView);
            }
          }
          while (localObject == null)
          {
            return;
            ((Bundle)localObject).putInt("user_id", paramAnonymousView.id);
            if (ChatAvatarContainer.this.timeItem != null) {
              ((Bundle)localObject).putLong("dialog_id", ChatAvatarContainer.this.parentFragment.getDialogId());
            }
            paramAnonymousView = new ProfileActivity((Bundle)localObject);
            paramAnonymousView.setPlayProfileAnimation(true);
            ChatAvatarContainer.this.parentFragment.presentFragment(paramAnonymousView);
            return;
          }
          paramAnonymousView = new Bundle();
          paramAnonymousView.putInt("chat_id", ((TLRPC.Chat)localObject).id);
          paramAnonymousView = new ProfileActivity(paramAnonymousView);
          paramAnonymousView.setChatInfo(ChatAvatarContainer.this.parentFragment.getCurrentChatInfo());
          paramAnonymousView.setPlayProfileAnimation(true);
          ChatAvatarContainer.this.parentFragment.presentFragment(paramAnonymousView);
        }
      });
      paramContext = this.parentFragment.getCurrentChat();
      this.statusDrawables[0] = new TypingDotsDrawable();
      this.statusDrawables[1] = new RecordStatusDrawable();
      this.statusDrawables[2] = new SendingFileDrawable();
      this.statusDrawables[3] = new PlayingGameDrawable();
      this.statusDrawables[4] = new RoundStatusDrawable();
      int i = 0;
      if (i < this.statusDrawables.length)
      {
        paramChatActivity = this.statusDrawables[i];
        if (paramContext != null) {}
        for (paramBoolean = true;; paramBoolean = false)
        {
          paramChatActivity.setIsChat(paramBoolean);
          i += 1;
          break;
        }
      }
    }
  }
  
  private void setTypingAnimation(boolean paramBoolean)
  {
    if (paramBoolean) {}
    for (;;)
    {
      int i;
      try
      {
        Integer localInteger = (Integer)MessagesController.getInstance(this.currentAccount).printingStringsTypes.get(this.parentFragment.getDialogId());
        this.subtitleTextView.setLeftDrawable(this.statusDrawables[localInteger.intValue()]);
        i = 0;
        if (i < this.statusDrawables.length) {
          if (i == localInteger.intValue()) {
            this.statusDrawables[i].start();
          } else {
            this.statusDrawables[i].stop();
          }
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
      for (;;)
      {
        return;
        this.subtitleTextView.setLeftDrawable(null);
        i = 0;
        while (i < this.statusDrawables.length)
        {
          this.statusDrawables[i].stop();
          i += 1;
        }
      }
      i += 1;
    }
  }
  
  private void updateCurrentConnectionState()
  {
    String str = null;
    if (this.currentConnectionState == 2) {
      str = LocaleController.getString("WaitingForNetwork", 2131494621);
    }
    while (str == null)
    {
      if (this.lastSubtitle != null)
      {
        this.subtitleTextView.setText(this.lastSubtitle);
        this.lastSubtitle = null;
      }
      return;
      if (this.currentConnectionState == 1) {
        str = LocaleController.getString("Connecting", 2131493282);
      } else if (this.currentConnectionState == 5) {
        str = LocaleController.getString("Updating", 2131494527);
      } else if (this.currentConnectionState == 4) {
        str = LocaleController.getString("ConnectingToProxy", 2131493283);
      }
    }
    this.lastSubtitle = this.subtitleTextView.getText();
    this.subtitleTextView.setText(str);
  }
  
  public void checkAndUpdateAvatar()
  {
    if (this.parentFragment == null) {}
    for (;;)
    {
      return;
      Object localObject2 = null;
      Object localObject1 = null;
      TLRPC.User localUser = this.parentFragment.getCurrentUser();
      TLRPC.Chat localChat = this.parentFragment.getCurrentChat();
      if (localUser != null)
      {
        this.avatarDrawable.setInfo(localUser);
        if (UserObject.isUserSelf(localUser)) {
          this.avatarDrawable.setSavedMessages(2);
        }
      }
      while (this.avatarImageView != null)
      {
        this.avatarImageView.setImage((TLObject)localObject1, "50_50", this.avatarDrawable);
        return;
        if (localUser.photo != null)
        {
          localObject1 = localUser.photo.photo_small;
          continue;
          if (localChat != null)
          {
            localObject1 = localObject2;
            if (localChat.photo != null) {
              localObject1 = localChat.photo.photo_small;
            }
            this.avatarDrawable.setInfo(localChat);
          }
        }
      }
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.didUpdatedConnectionState)
    {
      paramInt1 = ConnectionsManager.getInstance(this.currentAccount).getConnectionState();
      if (this.currentConnectionState != paramInt1)
      {
        this.currentConnectionState = paramInt1;
        updateCurrentConnectionState();
      }
    }
  }
  
  public SimpleTextView getSubtitleTextView()
  {
    return this.subtitleTextView;
  }
  
  public ImageView getTimeItem()
  {
    return this.timeItem;
  }
  
  public SimpleTextView getTitleTextView()
  {
    return this.titleTextView;
  }
  
  public void hideTimeItem()
  {
    if (this.timeItem == null) {
      return;
    }
    this.timeItem.setVisibility(8);
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    if (this.parentFragment != null)
    {
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didUpdatedConnectionState);
      this.currentConnectionState = ConnectionsManager.getInstance(this.currentAccount).getConnectionState();
      updateCurrentConnectionState();
    }
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    if (this.parentFragment != null) {
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdatedConnectionState);
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    paramInt2 = (ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(42.0F)) / 2;
    if ((Build.VERSION.SDK_INT >= 21) && (this.occupyStatusBar))
    {
      paramInt1 = AndroidUtilities.statusBarHeight;
      paramInt1 = paramInt2 + paramInt1;
      this.avatarImageView.layout(AndroidUtilities.dp(8.0F), paramInt1, AndroidUtilities.dp(50.0F), AndroidUtilities.dp(42.0F) + paramInt1);
      if (this.subtitleTextView.getVisibility() != 0) {
        break label222;
      }
      this.titleTextView.layout(AndroidUtilities.dp(62.0F), AndroidUtilities.dp(1.3F) + paramInt1, AndroidUtilities.dp(62.0F) + this.titleTextView.getMeasuredWidth(), this.titleTextView.getTextHeight() + paramInt1 + AndroidUtilities.dp(1.3F));
    }
    for (;;)
    {
      if (this.timeItem != null) {
        this.timeItem.layout(AndroidUtilities.dp(24.0F), AndroidUtilities.dp(15.0F) + paramInt1, AndroidUtilities.dp(58.0F), AndroidUtilities.dp(49.0F) + paramInt1);
      }
      this.subtitleTextView.layout(AndroidUtilities.dp(62.0F), AndroidUtilities.dp(24.0F) + paramInt1, AndroidUtilities.dp(62.0F) + this.subtitleTextView.getMeasuredWidth(), this.subtitleTextView.getTextHeight() + paramInt1 + AndroidUtilities.dp(24.0F));
      return;
      paramInt1 = 0;
      break;
      label222:
      this.titleTextView.layout(AndroidUtilities.dp(62.0F), AndroidUtilities.dp(11.0F) + paramInt1, AndroidUtilities.dp(62.0F) + this.titleTextView.getMeasuredWidth(), this.titleTextView.getTextHeight() + paramInt1 + AndroidUtilities.dp(11.0F));
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt1 = View.MeasureSpec.getSize(paramInt1);
    int i = paramInt1 - AndroidUtilities.dp(70.0F);
    this.avatarImageView.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(42.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(42.0F), 1073741824));
    this.titleTextView.measure(View.MeasureSpec.makeMeasureSpec(i, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24.0F), Integer.MIN_VALUE));
    this.subtitleTextView.measure(View.MeasureSpec.makeMeasureSpec(i, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(20.0F), Integer.MIN_VALUE));
    if (this.timeItem != null) {
      this.timeItem.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(34.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(34.0F), 1073741824));
    }
    setMeasuredDimension(paramInt1, View.MeasureSpec.getSize(paramInt2));
  }
  
  public void setChatAvatar(TLRPC.Chat paramChat)
  {
    TLRPC.FileLocation localFileLocation = null;
    if (paramChat.photo != null) {
      localFileLocation = paramChat.photo.photo_small;
    }
    this.avatarDrawable.setInfo(paramChat);
    if (this.avatarImageView != null) {
      this.avatarImageView.setImage(localFileLocation, "50_50", this.avatarDrawable);
    }
  }
  
  public void setOccupyStatusBar(boolean paramBoolean)
  {
    this.occupyStatusBar = paramBoolean;
  }
  
  public void setSubtitle(CharSequence paramCharSequence)
  {
    if (this.lastSubtitle == null)
    {
      this.subtitleTextView.setText(paramCharSequence);
      return;
    }
    this.lastSubtitle = paramCharSequence;
  }
  
  public void setTime(int paramInt)
  {
    if (this.timerDrawable == null) {
      return;
    }
    this.timerDrawable.setTime(paramInt);
  }
  
  public void setTitle(CharSequence paramCharSequence)
  {
    this.titleTextView.setText(paramCharSequence);
  }
  
  public void setTitleColors(int paramInt1, int paramInt2)
  {
    this.titleTextView.setTextColor(paramInt1);
    this.subtitleTextView.setTextColor(paramInt1);
  }
  
  public void setTitleIcons(int paramInt1, int paramInt2)
  {
    this.titleTextView.setLeftDrawable(paramInt1);
    this.titleTextView.setRightDrawable(paramInt2);
  }
  
  public void setTitleIcons(Drawable paramDrawable1, Drawable paramDrawable2)
  {
    this.titleTextView.setLeftDrawable(paramDrawable1);
    this.titleTextView.setRightDrawable(paramDrawable2);
  }
  
  public void setUserAvatar(TLRPC.User paramUser)
  {
    Object localObject = null;
    this.avatarDrawable.setInfo(paramUser);
    if (UserObject.isUserSelf(paramUser)) {
      this.avatarDrawable.setSavedMessages(2);
    }
    for (;;)
    {
      if (this.avatarImageView != null) {
        this.avatarImageView.setImage((TLObject)localObject, "50_50", this.avatarDrawable);
      }
      return;
      if (paramUser.photo != null) {
        localObject = paramUser.photo.photo_small;
      }
    }
  }
  
  public void showTimeItem()
  {
    if (this.timeItem == null) {
      return;
    }
    this.timeItem.setVisibility(0);
  }
  
  public void updateOnlineCount()
  {
    if (this.parentFragment == null) {}
    for (;;)
    {
      return;
      this.onlineCount = 0;
      TLRPC.ChatFull localChatFull = this.parentFragment.getCurrentChatInfo();
      if (localChatFull != null)
      {
        int j = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
        if (((localChatFull instanceof TLRPC.TL_chatFull)) || (((localChatFull instanceof TLRPC.TL_channelFull)) && (localChatFull.participants_count <= 200) && (localChatFull.participants != null)))
        {
          int i = 0;
          while (i < localChatFull.participants.participants.size())
          {
            Object localObject = (TLRPC.ChatParticipant)localChatFull.participants.participants.get(i);
            localObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChatParticipant)localObject).user_id));
            if ((localObject != null) && (((TLRPC.User)localObject).status != null) && ((((TLRPC.User)localObject).status.expires > j) || (((TLRPC.User)localObject).id == UserConfig.getInstance(this.currentAccount).getClientUserId())) && (((TLRPC.User)localObject).status.expires > 10000)) {
              this.onlineCount += 1;
            }
            i += 1;
          }
        }
      }
    }
  }
  
  public void updateSubtitle()
  {
    if (this.parentFragment == null) {}
    Object localObject2;
    do
    {
      return;
      localObject2 = this.parentFragment.getCurrentUser();
      if (!UserObject.isUserSelf((TLRPC.User)localObject2)) {
        break;
      }
    } while (this.subtitleTextView.getVisibility() == 8);
    this.subtitleTextView.setVisibility(8);
    return;
    TLRPC.Chat localChat = this.parentFragment.getCurrentChat();
    Object localObject3 = (CharSequence)MessagesController.getInstance(this.currentAccount).printingStrings.get(this.parentFragment.getDialogId());
    Object localObject1 = localObject3;
    if (localObject3 != null) {
      localObject1 = TextUtils.replace((CharSequence)localObject3, new String[] { "..." }, new String[] { "" });
    }
    if ((localObject1 == null) || (((CharSequence)localObject1).length() == 0) || ((ChatObject.isChannel(localChat)) && (!localChat.megagroup)))
    {
      setTypingAnimation(false);
      if (localChat != null)
      {
        localObject2 = this.parentFragment.getCurrentChatInfo();
        if (ChatObject.isChannel(localChat)) {
          if ((localObject2 != null) && (((TLRPC.ChatFull)localObject2).participants_count != 0)) {
            if ((localChat.megagroup) && (((TLRPC.ChatFull)localObject2).participants_count <= 200)) {
              if ((this.onlineCount > 1) && (((TLRPC.ChatFull)localObject2).participants_count != 0)) {
                localObject1 = String.format("%s, %s", new Object[] { LocaleController.formatPluralString("Members", ((TLRPC.ChatFull)localObject2).participants_count), LocaleController.formatPluralString("OnlineCount", this.onlineCount) });
              }
            }
          }
        }
      }
    }
    while (this.lastSubtitle == null)
    {
      this.subtitleTextView.setText((CharSequence)localObject1);
      return;
      localObject1 = LocaleController.formatPluralString("Members", ((TLRPC.ChatFull)localObject2).participants_count);
      continue;
      localObject1 = new int[1];
      localObject2 = LocaleController.formatShortNumber(((TLRPC.ChatFull)localObject2).participants_count, (int[])localObject1);
      if (localChat.megagroup)
      {
        localObject1 = LocaleController.formatPluralString("Members", localObject1[0]).replace(String.format("%d", new Object[] { Integer.valueOf(localObject1[0]) }), (CharSequence)localObject2);
      }
      else
      {
        localObject1 = LocaleController.formatPluralString("Subscribers", localObject1[0]).replace(String.format("%d", new Object[] { Integer.valueOf(localObject1[0]) }), (CharSequence)localObject2);
        continue;
        if (localChat.megagroup)
        {
          localObject1 = LocaleController.getString("Loading", 2131493762).toLowerCase();
        }
        else if ((localChat.flags & 0x40) != 0)
        {
          localObject1 = LocaleController.getString("ChannelPublic", 2131493200).toLowerCase();
        }
        else
        {
          localObject1 = LocaleController.getString("ChannelPrivate", 2131493197).toLowerCase();
          continue;
          if (ChatObject.isKickedFromChat(localChat))
          {
            localObject1 = LocaleController.getString("YouWereKicked", 2131494659);
          }
          else if (ChatObject.isLeftFromChat(localChat))
          {
            localObject1 = LocaleController.getString("YouLeft", 2131494658);
          }
          else
          {
            int j = localChat.participants_count;
            int i = j;
            if (localObject2 != null)
            {
              i = j;
              if (((TLRPC.ChatFull)localObject2).participants != null) {
                i = ((TLRPC.ChatFull)localObject2).participants.participants.size();
              }
            }
            if ((this.onlineCount > 1) && (i != 0))
            {
              localObject1 = String.format("%s, %s", new Object[] { LocaleController.formatPluralString("Members", i), LocaleController.formatPluralString("OnlineCount", this.onlineCount) });
            }
            else
            {
              localObject1 = LocaleController.formatPluralString("Members", i);
              continue;
              if (localObject2 != null)
              {
                localObject3 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.User)localObject2).id));
                localObject1 = localObject2;
                if (localObject3 != null) {
                  localObject1 = localObject3;
                }
                if (((TLRPC.User)localObject1).id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                  localObject1 = LocaleController.getString("ChatYourSelf", 2131493233);
                }
                for (;;)
                {
                  break;
                  if ((((TLRPC.User)localObject1).id == 333000) || (((TLRPC.User)localObject1).id == 777000)) {
                    localObject1 = LocaleController.getString("ServiceNotifications", 2131494365);
                  } else if (((TLRPC.User)localObject1).bot) {
                    localObject1 = LocaleController.getString("Bot", 2131493086);
                  } else {
                    localObject1 = LocaleController.formatUserStatus(this.currentAccount, (TLRPC.User)localObject1);
                  }
                }
              }
              localObject1 = "";
              continue;
              setTypingAnimation(true);
            }
          }
        }
      }
    }
    this.lastSubtitle = ((CharSequence)localObject1);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/ChatAvatarContainer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */