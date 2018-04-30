package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils.TruncateAt;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class ThemeTypeCell
  extends FrameLayout
{
  private ImageView checkImage;
  private boolean needDivider;
  private TextView textView;
  
  public ThemeTypeCell(Context paramContext)
  {
    super(paramContext);
    setWillNotDraw(false);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.textView.setTextSize(1, 16.0F);
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
    this.textView.setEllipsize(TextUtils.TruncateAt.END);
    TextView localTextView = this.textView;
    label121:
    float f1;
    label130:
    float f2;
    if (LocaleController.isRTL)
    {
      i = 5;
      localTextView.setGravity(i | 0x10);
      localTextView = this.textView;
      if (!LocaleController.isRTL) {
        break label248;
      }
      i = 5;
      if (!LocaleController.isRTL) {
        break label254;
      }
      f1 = 71.0F;
      if (!LocaleController.isRTL) {
        break label260;
      }
      f2 = 17.0F;
      label139:
      addView(localTextView, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, f1, 0.0F, f2, 0.0F));
      this.checkImage = new ImageView(paramContext);
      this.checkImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor("featuredStickers_addedIcon"), PorterDuff.Mode.MULTIPLY));
      this.checkImage.setImageResource(2131165652);
      paramContext = this.checkImage;
      if (!LocaleController.isRTL) {
        break label266;
      }
    }
    label248:
    label254:
    label260:
    label266:
    for (int i = j;; i = 5)
    {
      addView(paramContext, LayoutHelper.createFrame(19, 14.0F, i | 0x10, 23.0F, 0.0F, 23.0F, 0.0F));
      return;
      i = 3;
      break;
      i = 3;
      break label121;
      f1 = 17.0F;
      break label130;
      f2 = 23.0F;
      break label139;
    }
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
  
  public void setTypeChecked(boolean paramBoolean)
  {
    ImageView localImageView = this.checkImage;
    if (paramBoolean) {}
    for (int i = 0;; i = 4)
    {
      localImageView.setVisibility(i);
      return;
    }
  }
  
  public void setValue(String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.textView.setText(paramString);
    paramString = this.checkImage;
    if (paramBoolean1) {}
    for (int i = 0;; i = 4)
    {
      paramString.setVisibility(i);
      this.needDivider = paramBoolean2;
      return;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ThemeTypeCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */