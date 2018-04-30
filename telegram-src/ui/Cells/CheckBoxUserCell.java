package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBoxSquare;
import org.telegram.ui.Components.LayoutHelper;

public class CheckBoxUserCell
  extends FrameLayout
{
  private AvatarDrawable avatarDrawable;
  private CheckBoxSquare checkBox;
  private TLRPC.User currentUser;
  private BackupImageView imageView;
  private boolean needDivider;
  private TextView textView;
  
  public CheckBoxUserCell(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    TextView localTextView = this.textView;
    Object localObject;
    int i;
    label100:
    label125:
    label135:
    float f;
    if (paramBoolean)
    {
      localObject = "dialogTextBlack";
      localTextView.setTextColor(Theme.getColor((String)localObject));
      this.textView.setTextSize(1, 16.0F);
      this.textView.setLines(1);
      this.textView.setMaxLines(1);
      this.textView.setSingleLine(true);
      this.textView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label329;
      }
      i = 5;
      ((TextView)localObject).setGravity(i | 0x10);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label335;
      }
      i = 5;
      if (!LocaleController.isRTL) {
        break label341;
      }
      j = 17;
      f = j;
      if (!LocaleController.isRTL) {
        break label348;
      }
      j = 94;
      label149:
      addView((View)localObject, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, f, 0.0F, j, 0.0F));
      this.avatarDrawable = new AvatarDrawable();
      this.imageView = new BackupImageView(paramContext);
      this.imageView.setRoundRadius(AndroidUtilities.dp(36.0F));
      localObject = this.imageView;
      if (!LocaleController.isRTL) {
        break label355;
      }
      i = 5;
      label222:
      addView((View)localObject, LayoutHelper.createFrame(36, 36.0F, i | 0x30, 48.0F, 6.0F, 48.0F, 0.0F));
      this.checkBox = new CheckBoxSquare(paramContext, paramBoolean);
      paramContext = this.checkBox;
      if (!LocaleController.isRTL) {
        break label361;
      }
      i = 5;
      label274:
      if (!LocaleController.isRTL) {
        break label367;
      }
      j = 0;
      label283:
      f = j;
      if (!LocaleController.isRTL) {
        break label374;
      }
    }
    label329:
    label335:
    label341:
    label348:
    label355:
    label361:
    label367:
    label374:
    for (int j = 17;; j = 0)
    {
      addView(paramContext, LayoutHelper.createFrame(18, 18.0F, i | 0x30, f, 15.0F, j, 0.0F));
      return;
      localObject = "windowBackgroundWhiteBlackText";
      break;
      i = 3;
      break label100;
      i = 3;
      break label125;
      j = 94;
      break label135;
      j = 17;
      break label149;
      i = 3;
      break label222;
      i = 3;
      break label274;
      j = 17;
      break label283;
    }
  }
  
  public CheckBoxSquare getCheckBox()
  {
    return this.checkBox;
  }
  
  public TLRPC.User getCurrentUser()
  {
    return this.currentUser;
  }
  
  public TextView getTextView()
  {
    return this.textView;
  }
  
  public boolean isChecked()
  {
    return this.checkBox.isChecked();
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(getPaddingLeft(), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    int i = AndroidUtilities.dp(48.0F);
    if (this.needDivider) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(paramInt1 + i, 1073741824));
      return;
    }
  }
  
  public void setChecked(boolean paramBoolean1, boolean paramBoolean2)
  {
    this.checkBox.setChecked(paramBoolean1, paramBoolean2);
  }
  
  public void setTextColor(int paramInt)
  {
    this.textView.setTextColor(paramInt);
  }
  
  public void setUser(TLRPC.User paramUser, boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean bool = false;
    this.currentUser = paramUser;
    this.textView.setText(ContactsController.formatName(paramUser.first_name, paramUser.last_name));
    this.checkBox.setChecked(paramBoolean1, false);
    Object localObject2 = null;
    this.avatarDrawable.setInfo(paramUser);
    Object localObject1 = localObject2;
    if (paramUser != null)
    {
      localObject1 = localObject2;
      if (paramUser.photo != null) {
        localObject1 = paramUser.photo.photo_small;
      }
    }
    this.imageView.setImage((TLObject)localObject1, "50_50", this.avatarDrawable);
    this.needDivider = paramBoolean2;
    paramBoolean1 = bool;
    if (!paramBoolean2) {
      paramBoolean1 = true;
    }
    setWillNotDraw(paramBoolean1);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/CheckBoxUserCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */