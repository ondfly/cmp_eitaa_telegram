package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.GroupCreateCheckBox;
import org.telegram.ui.Components.LayoutHelper;

public class InviteUserCell
  extends FrameLayout
{
  private AvatarDrawable avatarDrawable = new AvatarDrawable();
  private BackupImageView avatarImageView;
  private GroupCreateCheckBox checkBox;
  private ContactsController.Contact currentContact;
  private CharSequence currentName;
  private SimpleTextView nameTextView;
  private SimpleTextView statusTextView;
  
  public InviteUserCell(Context paramContext, boolean paramBoolean)
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
        break label417;
      }
      f1 = 0.0F;
      label66:
      if (!LocaleController.isRTL) {
        break label423;
      }
      f2 = 11.0F;
      label76:
      addView((View)localObject, LayoutHelper.createFrame(50, 50.0F, i | 0x30, f1, 11.0F, f2, 0.0F));
      this.nameTextView = new SimpleTextView(paramContext);
      this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.nameTextView.setTextSize(17);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label429;
      }
      i = 5;
      label160:
      ((SimpleTextView)localObject).setGravity(i | 0x30);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label435;
      }
      i = 5;
      label185:
      if (!LocaleController.isRTL) {
        break label441;
      }
      f1 = 28.0F;
      label194:
      if (!LocaleController.isRTL) {
        break label447;
      }
      f2 = 72.0F;
      label204:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 14.0F, f2, 0.0F));
      this.statusTextView = new SimpleTextView(paramContext);
      this.statusTextView.setTextSize(16);
      localObject = this.statusTextView;
      if (!LocaleController.isRTL) {
        break label454;
      }
      i = 5;
      label263:
      ((SimpleTextView)localObject).setGravity(i | 0x30);
      localObject = this.statusTextView;
      if (!LocaleController.isRTL) {
        break label460;
      }
      i = 5;
      label288:
      if (!LocaleController.isRTL) {
        break label466;
      }
      f1 = 28.0F;
      label297:
      if (!LocaleController.isRTL) {
        break label472;
      }
      f2 = 72.0F;
      label307:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 39.0F, f2, 0.0F));
      if (paramBoolean)
      {
        this.checkBox = new GroupCreateCheckBox(paramContext);
        this.checkBox.setVisibility(0);
        paramContext = this.checkBox;
        if (!LocaleController.isRTL) {
          break label479;
        }
        i = j;
        label369:
        if (!LocaleController.isRTL) {
          break label485;
        }
        f1 = 0.0F;
        label377:
        if (!LocaleController.isRTL) {
          break label491;
        }
      }
    }
    label417:
    label423:
    label429:
    label435:
    label441:
    label447:
    label454:
    label460:
    label466:
    label472:
    label479:
    label485:
    label491:
    for (float f2 = 41.0F;; f2 = 0.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(24, 24.0F, i | 0x30, f1, 41.0F, f2, 0.0F));
      return;
      i = 3;
      break;
      f1 = 11.0F;
      break label66;
      f2 = 0.0F;
      break label76;
      i = 3;
      break label160;
      i = 3;
      break label185;
      f1 = 72.0F;
      break label194;
      f2 = 28.0F;
      break label204;
      i = 3;
      break label263;
      i = 3;
      break label288;
      f1 = 72.0F;
      break label297;
      f2 = 28.0F;
      break label307;
      i = 3;
      break label369;
      f1 = 41.0F;
      break label377;
    }
  }
  
  public ContactsController.Contact getContact()
  {
    return this.currentContact;
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
  
  public void setUser(ContactsController.Contact paramContact, CharSequence paramCharSequence)
  {
    this.currentContact = paramContact;
    this.currentName = paramCharSequence;
    update(0);
  }
  
  public void update(int paramInt)
  {
    if (this.currentContact == null) {
      return;
    }
    this.avatarDrawable.setInfo(this.currentContact.contact_id, this.currentContact.first_name, this.currentContact.last_name, false);
    if (this.currentName != null)
    {
      this.nameTextView.setText(this.currentName, true);
      this.statusTextView.setTag("groupcreate_offlineText");
      this.statusTextView.setTextColor(Theme.getColor("groupcreate_offlineText"));
      if (this.currentContact.imported <= 0) {
        break label145;
      }
      this.statusTextView.setText(LocaleController.formatPluralString("TelegramContacts", this.currentContact.imported));
    }
    for (;;)
    {
      this.avatarImageView.setImageDrawable(this.avatarDrawable);
      return;
      this.nameTextView.setText(ContactsController.formatName(this.currentContact.first_name, this.currentContact.last_name));
      break;
      label145:
      this.statusTextView.setText((CharSequence)this.currentContact.phones.get(0));
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/InviteUserCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */