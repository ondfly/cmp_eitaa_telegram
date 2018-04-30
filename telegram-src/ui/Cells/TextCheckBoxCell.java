package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils.TruncateAt;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CheckBoxSquare;
import org.telegram.ui.Components.LayoutHelper;

public class TextCheckBoxCell
  extends FrameLayout
{
  private CheckBoxSquare checkBox;
  private boolean needDivider;
  private TextView textView;
  
  public TextCheckBoxCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.textView.setTextSize(1, 16.0F);
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
    TextView localTextView = this.textView;
    label116:
    float f1;
    label125:
    float f2;
    if (LocaleController.isRTL)
    {
      i = 5;
      localTextView.setGravity(i | 0x10);
      this.textView.setEllipsize(TextUtils.TruncateAt.END);
      localTextView = this.textView;
      if (!LocaleController.isRTL) {
        break label245;
      }
      i = 5;
      if (!LocaleController.isRTL) {
        break label251;
      }
      f1 = 64.0F;
      if (!LocaleController.isRTL) {
        break label257;
      }
      f2 = 17.0F;
      label134:
      addView(localTextView, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, f1, 0.0F, f2, 0.0F));
      this.checkBox = new CheckBoxSquare(paramContext, false);
      this.checkBox.setDuplicateParentStateEnabled(false);
      this.checkBox.setFocusable(false);
      this.checkBox.setFocusableInTouchMode(false);
      this.checkBox.setClickable(false);
      paramContext = this.checkBox;
      if (!LocaleController.isRTL) {
        break label263;
      }
    }
    label245:
    label251:
    label257:
    label263:
    for (int i = j;; i = 5)
    {
      addView(paramContext, LayoutHelper.createFrame(18, 18.0F, i | 0x10, 19.0F, 0.0F, 19.0F, 0.0F));
      return;
      i = 3;
      break;
      i = 3;
      break label116;
      f1 = 17.0F;
      break label125;
      f2 = 64.0F;
      break label134;
    }
  }
  
  public void invalidate()
  {
    super.invalidate();
    this.checkBox.invalidate();
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
  
  public void setChecked(boolean paramBoolean)
  {
    this.checkBox.setChecked(paramBoolean, true);
  }
  
  public void setTextAndCheck(String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean bool = false;
    this.textView.setText(paramString);
    this.checkBox.setChecked(paramBoolean1, false);
    this.needDivider = paramBoolean2;
    paramBoolean1 = bool;
    if (!paramBoolean2) {
      paramBoolean1 = true;
    }
    setWillNotDraw(paramBoolean1);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/TextCheckBoxCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */