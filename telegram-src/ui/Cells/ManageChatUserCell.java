package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class ManageChatUserCell
  extends FrameLayout
{
  private AvatarDrawable avatarDrawable = new AvatarDrawable();
  private BackupImageView avatarImageView;
  private int currentAccount = UserConfig.selectedAccount;
  private CharSequence currentName;
  private TLRPC.User currentUser;
  private CharSequence currrntStatus;
  private ManageChatUserCellDelegate delegate;
  private boolean isAdmin;
  private TLRPC.FileLocation lastAvatar;
  private String lastName;
  private int lastStatus;
  private SimpleTextView nameTextView;
  private ImageView optionsButton;
  private int statusColor = Theme.getColor("windowBackgroundWhiteGrayText");
  private int statusOnlineColor = Theme.getColor("windowBackgroundWhiteBlueText");
  private SimpleTextView statusTextView;
  
  public ManageChatUserCell(Context paramContext, int paramInt, boolean paramBoolean)
  {
    super(paramContext);
    this.avatarImageView = new BackupImageView(paramContext);
    this.avatarImageView.setRoundRadius(AndroidUtilities.dp(24.0F));
    Object localObject = this.avatarImageView;
    int i;
    float f1;
    label92:
    float f2;
    if (LocaleController.isRTL)
    {
      i = 5;
      if (!LocaleController.isRTL) {
        break label502;
      }
      f1 = 0.0F;
      if (!LocaleController.isRTL) {
        break label512;
      }
      f2 = paramInt + 7;
      label105:
      addView((View)localObject, LayoutHelper.createFrame(48, 48.0F, i | 0x30, f1, 8.0F, f2, 0.0F));
      this.nameTextView = new SimpleTextView(paramContext);
      this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setTextSize(17);
      this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label518;
      }
      i = 5;
      label190:
      ((SimpleTextView)localObject).setGravity(i | 0x30);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label524;
      }
      i = 5;
      label215:
      if (!LocaleController.isRTL) {
        break label530;
      }
      f1 = 46.0F;
      label225:
      if (!LocaleController.isRTL) {
        break label540;
      }
      f2 = paramInt + 68;
      label238:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 11.5F, f2, 0.0F));
      this.statusTextView = new SimpleTextView(paramContext);
      this.statusTextView.setTextSize(14);
      localObject = this.statusTextView;
      if (!LocaleController.isRTL) {
        break label547;
      }
      i = 5;
      label298:
      ((SimpleTextView)localObject).setGravity(i | 0x30);
      localObject = this.statusTextView;
      if (!LocaleController.isRTL) {
        break label553;
      }
      i = 5;
      label323:
      if (!LocaleController.isRTL) {
        break label559;
      }
      f1 = 28.0F;
      label333:
      if (!LocaleController.isRTL) {
        break label569;
      }
      f2 = paramInt + 68;
      label346:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 34.5F, f2, 0.0F));
      if (paramBoolean)
      {
        this.optionsButton = new ImageView(paramContext);
        this.optionsButton.setFocusable(false);
        this.optionsButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor("stickers_menuSelector")));
        this.optionsButton.setImageResource(2131165353);
        this.optionsButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("stickers_menu"), PorterDuff.Mode.MULTIPLY));
        this.optionsButton.setScaleType(ImageView.ScaleType.CENTER);
        paramContext = this.optionsButton;
        if (!LocaleController.isRTL) {
          break label576;
        }
      }
    }
    label502:
    label512:
    label518:
    label524:
    label530:
    label540:
    label547:
    label553:
    label559:
    label569:
    label576:
    for (paramInt = j;; paramInt = 5)
    {
      addView(paramContext, LayoutHelper.createFrame(48, 64, paramInt | 0x30));
      this.optionsButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          ManageChatUserCell.this.delegate.onOptionsButtonCheck(ManageChatUserCell.this, true);
        }
      });
      return;
      i = 3;
      break;
      f1 = paramInt + 7;
      break label92;
      f2 = 0.0F;
      break label105;
      i = 3;
      break label190;
      i = 3;
      break label215;
      f1 = paramInt + 68;
      break label225;
      f2 = 46.0F;
      break label238;
      i = 3;
      break label298;
      i = 3;
      break label323;
      f1 = paramInt + 68;
      break label333;
      f2 = 28.0F;
      break label346;
    }
  }
  
  public boolean hasOverlappingRendering()
  {
    return false;
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64.0F), 1073741824));
  }
  
  public void recycle()
  {
    this.avatarImageView.getImageReceiver().cancelLoadImage();
  }
  
  public void setData(TLRPC.User paramUser, CharSequence paramCharSequence1, CharSequence paramCharSequence2)
  {
    if (paramUser == null)
    {
      this.currrntStatus = null;
      this.currentName = null;
      this.currentUser = null;
      this.nameTextView.setText("");
      this.statusTextView.setText("");
      this.avatarImageView.setImageDrawable(null);
      return;
    }
    this.currrntStatus = paramCharSequence2;
    this.currentName = paramCharSequence1;
    this.currentUser = paramUser;
    if (this.optionsButton != null)
    {
      paramUser = this.optionsButton;
      if (!this.delegate.onOptionsButtonCheck(this, false)) {
        break label102;
      }
    }
    label102:
    for (int i = 0;; i = 4)
    {
      paramUser.setVisibility(i);
      update(0);
      return;
    }
  }
  
  public void setDelegate(ManageChatUserCellDelegate paramManageChatUserCellDelegate)
  {
    this.delegate = paramManageChatUserCellDelegate;
  }
  
  public void setIsAdmin(boolean paramBoolean)
  {
    this.isAdmin = paramBoolean;
  }
  
  public void setStatusColors(int paramInt1, int paramInt2)
  {
    this.statusColor = paramInt1;
    this.statusOnlineColor = paramInt2;
  }
  
  public void update(int paramInt)
  {
    if (this.currentUser == null) {}
    TLRPC.FileLocation localFileLocation;
    Object localObject1;
    Object localObject2;
    int i;
    label132:
    do
    {
      return;
      localFileLocation = null;
      localObject1 = null;
      localObject2 = null;
      if (this.currentUser.photo != null) {
        localFileLocation = this.currentUser.photo.photo_small;
      }
      if (paramInt == 0) {
        break;
      }
      int j = 0;
      i = j;
      if ((paramInt & 0x2) != 0)
      {
        if ((this.lastAvatar == null) || (localFileLocation != null))
        {
          i = j;
          if (this.lastAvatar != null) {
            break label132;
          }
          i = j;
          if (localFileLocation == null) {
            break label132;
          }
          i = j;
          if (this.lastAvatar == null) {
            break label132;
          }
          i = j;
          if (localFileLocation == null) {
            break label132;
          }
          if (this.lastAvatar.volume_id == localFileLocation.volume_id)
          {
            i = j;
            if (this.lastAvatar.local_id == localFileLocation.local_id) {
              break label132;
            }
          }
        }
        i = 1;
      }
      j = i;
      if (this.currentUser != null)
      {
        j = i;
        if (i == 0)
        {
          j = i;
          if ((paramInt & 0x4) != 0)
          {
            int k = 0;
            if (this.currentUser.status != null) {
              k = this.currentUser.status.expires;
            }
            j = i;
            if (k != this.lastStatus) {
              j = 1;
            }
          }
        }
      }
      i = j;
      localObject1 = localObject2;
      if (j == 0)
      {
        i = j;
        localObject1 = localObject2;
        if (this.currentName == null)
        {
          i = j;
          localObject1 = localObject2;
          if (this.lastName != null)
          {
            i = j;
            localObject1 = localObject2;
            if ((paramInt & 0x1) != 0)
            {
              localObject2 = UserObject.getUserName(this.currentUser);
              i = j;
              localObject1 = localObject2;
              if (!((String)localObject2).equals(this.lastName))
              {
                i = 1;
                localObject1 = localObject2;
              }
            }
          }
        }
      }
    } while (i == 0);
    this.avatarDrawable.setInfo(this.currentUser);
    if (this.currentUser.status != null)
    {
      this.lastStatus = this.currentUser.status.expires;
      if (this.currentName == null) {
        break label390;
      }
      this.lastName = null;
      this.nameTextView.setText(this.currentName);
      label336:
      if (this.currrntStatus == null) {
        break label428;
      }
      this.statusTextView.setTextColor(this.statusColor);
      this.statusTextView.setText(this.currrntStatus);
    }
    for (;;)
    {
      this.avatarImageView.setImage(localFileLocation, "50_50", this.avatarDrawable);
      return;
      this.lastStatus = 0;
      break;
      label390:
      localObject2 = localObject1;
      if (localObject1 == null) {
        localObject2 = UserObject.getUserName(this.currentUser);
      }
      this.lastName = ((String)localObject2);
      this.nameTextView.setText(this.lastName);
      break label336;
      label428:
      if (this.currentUser != null) {
        if (this.currentUser.bot)
        {
          this.statusTextView.setTextColor(this.statusColor);
          if ((this.currentUser.bot_chat_history) || (this.isAdmin)) {
            this.statusTextView.setText(LocaleController.getString("BotStatusRead", 2131493097));
          } else {
            this.statusTextView.setText(LocaleController.getString("BotStatusCantRead", 2131493096));
          }
        }
        else if ((this.currentUser.id == UserConfig.getInstance(this.currentAccount).getClientUserId()) || ((this.currentUser.status != null) && (this.currentUser.status.expires > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime())) || (MessagesController.getInstance(this.currentAccount).onlinePrivacy.containsKey(Integer.valueOf(this.currentUser.id))))
        {
          this.statusTextView.setTextColor(this.statusOnlineColor);
          this.statusTextView.setText(LocaleController.getString("Online", 2131494030));
        }
        else
        {
          this.statusTextView.setTextColor(this.statusColor);
          this.statusTextView.setText(LocaleController.formatUserStatus(this.currentAccount, this.currentUser));
        }
      }
    }
  }
  
  public static abstract interface ManageChatUserCellDelegate
  {
    public abstract boolean onOptionsButtonCheck(ManageChatUserCell paramManageChatUserCell, boolean paramBoolean);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ManageChatUserCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */