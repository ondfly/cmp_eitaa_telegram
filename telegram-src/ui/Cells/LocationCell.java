package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class LocationCell
  extends FrameLayout
{
  private TextView addressTextView;
  private BackupImageView imageView;
  private TextView nameTextView;
  private boolean needDivider;
  
  public LocationCell(Context paramContext)
  {
    super(paramContext);
    this.imageView = new BackupImageView(paramContext);
    this.imageView.setBackgroundResource(2131165624);
    this.imageView.setSize(AndroidUtilities.dp(30.0F), AndroidUtilities.dp(30.0F));
    this.imageView.getImageReceiver().setColorFilter(new PorterDuffColorFilter(Theme.getColor("windowBackgroundWhiteGrayText3"), PorterDuff.Mode.MULTIPLY));
    Object localObject = this.imageView;
    int i;
    float f1;
    label98:
    float f2;
    if (LocaleController.isRTL)
    {
      i = 5;
      if (!LocaleController.isRTL) {
        break label442;
      }
      f1 = 0.0F;
      if (!LocaleController.isRTL) {
        break label448;
      }
      f2 = 17.0F;
      label107:
      addView((View)localObject, LayoutHelper.createFrame(40, 40.0F, i | 0x30, f1, 8.0F, f2, 0.0F));
      this.nameTextView = new TextView(paramContext);
      this.nameTextView.setTextSize(1, 16.0F);
      this.nameTextView.setMaxLines(1);
      this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
      this.nameTextView.setSingleLine(true);
      this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label453;
      }
      i = 5;
      label217:
      ((TextView)localObject).setGravity(i);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label459;
      }
      i = 5;
      label239:
      if (!LocaleController.isRTL) {
        break label465;
      }
      j = 16;
      label249:
      f1 = j;
      if (!LocaleController.isRTL) {
        break label472;
      }
      j = 72;
      label263:
      addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, i | 0x30, f1, 5.0F, j, 0.0F));
      this.addressTextView = new TextView(paramContext);
      this.addressTextView.setTextSize(1, 14.0F);
      this.addressTextView.setMaxLines(1);
      this.addressTextView.setEllipsize(TextUtils.TruncateAt.END);
      this.addressTextView.setSingleLine(true);
      this.addressTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText3"));
      paramContext = this.addressTextView;
      if (!LocaleController.isRTL) {
        break label479;
      }
      i = 5;
      label362:
      paramContext.setGravity(i);
      paramContext = this.addressTextView;
      if (!LocaleController.isRTL) {
        break label485;
      }
      i = m;
      label383:
      if (!LocaleController.isRTL) {
        break label491;
      }
    }
    label442:
    label448:
    label453:
    label459:
    label465:
    label472:
    label479:
    label485:
    label491:
    for (int j = 16;; j = 72)
    {
      f1 = j;
      j = k;
      if (LocaleController.isRTL) {
        j = 72;
      }
      addView(paramContext, LayoutHelper.createFrame(-2, -2.0F, i | 0x30, f1, 30.0F, j, 0.0F));
      return;
      i = 3;
      break;
      f1 = 17.0F;
      break label98;
      f2 = 0.0F;
      break label107;
      i = 3;
      break label217;
      i = 3;
      break label239;
      j = 72;
      break label249;
      j = 16;
      break label263;
      i = 3;
      break label362;
      i = 3;
      break label383;
    }
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(AndroidUtilities.dp(72.0F), getHeight() - 1, getWidth(), getHeight() - 1, Theme.dividerPaint);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    int i = AndroidUtilities.dp(56.0F);
    if (this.needDivider) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(paramInt1 + i, 1073741824));
      return;
    }
  }
  
  public void setLocation(TLRPC.TL_messageMediaVenue paramTL_messageMediaVenue, String paramString, boolean paramBoolean)
  {
    this.needDivider = paramBoolean;
    this.nameTextView.setText(paramTL_messageMediaVenue.title);
    this.addressTextView.setText(paramTL_messageMediaVenue.address);
    this.imageView.setImage(paramString, null, null);
    if (!paramBoolean) {}
    for (paramBoolean = true;; paramBoolean = false)
    {
      setWillNotDraw(paramBoolean);
      return;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/LocationCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */