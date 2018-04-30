package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.URLSpanNoUnderline;

public class AdminedChannelCell
  extends FrameLayout
{
  private AvatarDrawable avatarDrawable = new AvatarDrawable();
  private BackupImageView avatarImageView;
  private int currentAccount = UserConfig.selectedAccount;
  private TLRPC.Chat currentChannel;
  private ImageView deleteButton;
  private boolean isLast;
  private SimpleTextView nameTextView;
  private SimpleTextView statusTextView;
  
  public AdminedChannelCell(Context paramContext, View.OnClickListener paramOnClickListener)
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
        break label469;
      }
      f1 = 0.0F;
      label70:
      if (!LocaleController.isRTL) {
        break label475;
      }
      f2 = 12.0F;
      label80:
      addView((View)localObject, LayoutHelper.createFrame(48, 48.0F, i | 0x30, f1, 12.0F, f2, 0.0F));
      this.nameTextView = new SimpleTextView(paramContext);
      this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setTextSize(17);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label481;
      }
      i = 5;
      label152:
      ((SimpleTextView)localObject).setGravity(i | 0x30);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label487;
      }
      i = 5;
      label177:
      if (!LocaleController.isRTL) {
        break label493;
      }
      f1 = 62.0F;
      label186:
      if (!LocaleController.isRTL) {
        break label499;
      }
      f2 = 73.0F;
      label196:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 15.5F, f2, 0.0F));
      this.statusTextView = new SimpleTextView(paramContext);
      this.statusTextView.setTextSize(14);
      this.statusTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText"));
      this.statusTextView.setLinkTextColor(Theme.getColor("windowBackgroundWhiteLinkText"));
      localObject = this.statusTextView;
      if (!LocaleController.isRTL) {
        break label506;
      }
      i = 5;
      label279:
      ((SimpleTextView)localObject).setGravity(i | 0x30);
      localObject = this.statusTextView;
      if (!LocaleController.isRTL) {
        break label512;
      }
      i = 5;
      label304:
      if (!LocaleController.isRTL) {
        break label518;
      }
      f1 = 62.0F;
      label313:
      if (!LocaleController.isRTL) {
        break label524;
      }
      f2 = 73.0F;
      label323:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 38.5F, f2, 0.0F));
      this.deleteButton = new ImageView(paramContext);
      this.deleteButton.setScaleType(ImageView.ScaleType.CENTER);
      this.deleteButton.setImageResource(2131165519);
      this.deleteButton.setOnClickListener(paramOnClickListener);
      this.deleteButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("windowBackgroundWhiteGrayText"), PorterDuff.Mode.MULTIPLY));
      paramContext = this.deleteButton;
      if (!LocaleController.isRTL) {
        break label531;
      }
      i = 3;
      label421:
      if (!LocaleController.isRTL) {
        break label537;
      }
      f1 = 7.0F;
      label430:
      if (!LocaleController.isRTL) {
        break label542;
      }
    }
    label469:
    label475:
    label481:
    label487:
    label493:
    label499:
    label506:
    label512:
    label518:
    label524:
    label531:
    label537:
    label542:
    for (float f2 = 0.0F;; f2 = 7.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(48, 48.0F, i | 0x30, f1, 12.0F, f2, 0.0F));
      return;
      i = 3;
      break;
      f1 = 12.0F;
      break label70;
      f2 = 0.0F;
      break label80;
      i = 3;
      break label152;
      i = 3;
      break label177;
      f1 = 73.0F;
      break label186;
      f2 = 62.0F;
      break label196;
      i = 3;
      break label279;
      i = 3;
      break label304;
      f1 = 73.0F;
      break label313;
      f2 = 62.0F;
      break label323;
      i = 5;
      break label421;
      f1 = 0.0F;
      break label430;
    }
  }
  
  public TLRPC.Chat getCurrentChannel()
  {
    return this.currentChannel;
  }
  
  public ImageView getDeleteButton()
  {
    return this.deleteButton;
  }
  
  public SimpleTextView getNameTextView()
  {
    return this.nameTextView;
  }
  
  public SimpleTextView getStatusTextView()
  {
    return this.statusTextView;
  }
  
  public boolean hasOverlappingRendering()
  {
    return false;
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    if (this.isLast) {}
    for (paramInt1 = 12;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(paramInt1 + 60), 1073741824));
      return;
    }
  }
  
  public void setChannel(TLRPC.Chat paramChat, boolean paramBoolean)
  {
    TLRPC.FileLocation localFileLocation = null;
    if (paramChat.photo != null) {
      localFileLocation = paramChat.photo.photo_small;
    }
    String str = MessagesController.getInstance(this.currentAccount).linkPrefix + "/";
    this.currentChannel = paramChat;
    this.avatarDrawable.setInfo(paramChat);
    this.nameTextView.setText(paramChat.title);
    paramChat = new SpannableStringBuilder(str + paramChat.username);
    paramChat.setSpan(new URLSpanNoUnderline(""), str.length(), paramChat.length(), 33);
    this.statusTextView.setText(paramChat);
    this.avatarImageView.setImage(localFileLocation, "50_50", this.avatarDrawable);
    this.isLast = paramBoolean;
  }
  
  public void update()
  {
    this.avatarDrawable.setInfo(this.currentChannel);
    this.avatarImageView.invalidate();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/AdminedChannelCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */