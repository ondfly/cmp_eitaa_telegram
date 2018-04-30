package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
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
import org.telegram.ui.Components.GroupCreateCheckBox;
import org.telegram.ui.Components.LayoutHelper;

public class GroupCreateUserCell
  extends FrameLayout
{
  private AvatarDrawable avatarDrawable = new AvatarDrawable();
  private BackupImageView avatarImageView;
  private GroupCreateCheckBox checkBox;
  private int currentAccount = UserConfig.selectedAccount;
  private CharSequence currentName;
  private CharSequence currentStatus;
  private TLRPC.User currentUser;
  private TLRPC.FileLocation lastAvatar;
  private String lastName;
  private int lastStatus;
  private SimpleTextView nameTextView;
  private SimpleTextView statusTextView;
  
  public GroupCreateUserCell(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    this.avatarImageView = new BackupImageView(paramContext);
    this.avatarImageView.setRoundRadius(AndroidUtilities.dp(24.0F));
    Object localObject = this.avatarImageView;
    int i;
    float f1;
    if (LocaleController.isRTL)
    {
      i = 5;
      if (!LocaleController.isRTL) {
        break label424;
      }
      f1 = 0.0F;
      label73:
      if (!LocaleController.isRTL) {
        break label430;
      }
      f2 = 11.0F;
      label83:
      addView((View)localObject, LayoutHelper.createFrame(50, 50.0F, i | 0x30, f1, 11.0F, f2, 0.0F));
      this.nameTextView = new SimpleTextView(paramContext);
      this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.nameTextView.setTextSize(17);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label436;
      }
      i = 5;
      label167:
      ((SimpleTextView)localObject).setGravity(i | 0x30);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label442;
      }
      i = 5;
      label192:
      if (!LocaleController.isRTL) {
        break label448;
      }
      f1 = 28.0F;
      label201:
      if (!LocaleController.isRTL) {
        break label454;
      }
      f2 = 72.0F;
      label211:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 14.0F, f2, 0.0F));
      this.statusTextView = new SimpleTextView(paramContext);
      this.statusTextView.setTextSize(16);
      localObject = this.statusTextView;
      if (!LocaleController.isRTL) {
        break label461;
      }
      i = 5;
      label270:
      ((SimpleTextView)localObject).setGravity(i | 0x30);
      localObject = this.statusTextView;
      if (!LocaleController.isRTL) {
        break label467;
      }
      i = 5;
      label295:
      if (!LocaleController.isRTL) {
        break label473;
      }
      f1 = 28.0F;
      label304:
      if (!LocaleController.isRTL) {
        break label479;
      }
      f2 = 72.0F;
      label314:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 39.0F, f2, 0.0F));
      if (paramBoolean)
      {
        this.checkBox = new GroupCreateCheckBox(paramContext);
        this.checkBox.setVisibility(0);
        paramContext = this.checkBox;
        if (!LocaleController.isRTL) {
          break label486;
        }
        i = j;
        label376:
        if (!LocaleController.isRTL) {
          break label492;
        }
        f1 = 0.0F;
        label384:
        if (!LocaleController.isRTL) {
          break label498;
        }
      }
    }
    label424:
    label430:
    label436:
    label442:
    label448:
    label454:
    label461:
    label467:
    label473:
    label479:
    label486:
    label492:
    label498:
    for (float f2 = 41.0F;; f2 = 0.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(24, 24.0F, i | 0x30, f1, 41.0F, f2, 0.0F));
      return;
      i = 3;
      break;
      f1 = 11.0F;
      break label73;
      f2 = 0.0F;
      break label83;
      i = 3;
      break label167;
      i = 3;
      break label192;
      f1 = 72.0F;
      break label201;
      f2 = 28.0F;
      break label211;
      i = 3;
      break label270;
      i = 3;
      break label295;
      f1 = 72.0F;
      break label304;
      f2 = 28.0F;
      break label314;
      i = 3;
      break label376;
      f1 = 41.0F;
      break label384;
    }
  }
  
  public TLRPC.User getUser()
  {
    return this.currentUser;
  }
  
  public boolean hasOverlappingRendering()
  {
    return false;
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(72.0F), 1073741824));
  }
  
  public void recycle()
  {
    this.avatarImageView.getImageReceiver().cancelLoadImage();
  }
  
  public void setChecked(boolean paramBoolean1, boolean paramBoolean2)
  {
    this.checkBox.setChecked(paramBoolean1, paramBoolean2);
  }
  
  public void setUser(TLRPC.User paramUser, CharSequence paramCharSequence1, CharSequence paramCharSequence2)
  {
    this.currentUser = paramUser;
    this.currentStatus = paramCharSequence2;
    this.currentName = paramCharSequence1;
    update(0);
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
        if (this.currentStatus == null)
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
      paramInt = this.currentUser.status.expires;
      this.lastStatus = paramInt;
      if (this.currentName == null) {
        break label409;
      }
      this.lastName = null;
      this.nameTextView.setText(this.currentName, true);
      label348:
      if (this.currentStatus == null) {
        break label447;
      }
      this.statusTextView.setText(this.currentStatus, true);
      this.statusTextView.setTag("groupcreate_offlineText");
      this.statusTextView.setTextColor(Theme.getColor("groupcreate_offlineText"));
    }
    for (;;)
    {
      this.avatarImageView.setImage(localFileLocation, "50_50", this.avatarDrawable);
      return;
      paramInt = 0;
      break;
      label409:
      localObject2 = localObject1;
      if (localObject1 == null) {
        localObject2 = UserObject.getUserName(this.currentUser);
      }
      this.lastName = ((String)localObject2);
      this.nameTextView.setText(this.lastName);
      break label348;
      label447:
      if (this.currentUser.bot)
      {
        this.statusTextView.setTag("groupcreate_offlineText");
        this.statusTextView.setTextColor(Theme.getColor("groupcreate_offlineText"));
        this.statusTextView.setText(LocaleController.getString("Bot", 2131493086));
      }
      else if ((this.currentUser.id == UserConfig.getInstance(this.currentAccount).getClientUserId()) || ((this.currentUser.status != null) && (this.currentUser.status.expires > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime())) || (MessagesController.getInstance(this.currentAccount).onlinePrivacy.containsKey(Integer.valueOf(this.currentUser.id))))
      {
        this.statusTextView.setTag("groupcreate_offlineText");
        this.statusTextView.setTextColor(Theme.getColor("groupcreate_onlineText"));
        this.statusTextView.setText(LocaleController.getString("Online", 2131494030));
      }
      else
      {
        this.statusTextView.setTag("groupcreate_offlineText");
        this.statusTextView.setTextColor(Theme.getColor("groupcreate_offlineText"));
        this.statusTextView.setText(LocaleController.formatUserStatus(this.currentAccount, this.currentUser));
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/GroupCreateUserCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */