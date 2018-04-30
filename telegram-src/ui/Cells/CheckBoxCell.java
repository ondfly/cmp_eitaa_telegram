package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CheckBoxSquare;
import org.telegram.ui.Components.LayoutHelper;

public class CheckBoxCell
  extends FrameLayout
{
  private CheckBoxSquare checkBox;
  private boolean needDivider;
  private TextView textView;
  private TextView valueTextView;
  
  public CheckBoxCell(Context paramContext, int paramInt)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    TextView localTextView = this.textView;
    Object localObject;
    int j;
    label107:
    label137:
    int k;
    label146:
    float f;
    label160:
    label210:
    label279:
    label304:
    boolean bool;
    if (paramInt == 1)
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
        break label396;
      }
      j = 5;
      ((TextView)localObject).setGravity(j | 0x10);
      if (paramInt != 2) {
        break label421;
      }
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label402;
      }
      j = 5;
      if (!LocaleController.isRTL) {
        break label408;
      }
      k = 0;
      f = k;
      if (!LocaleController.isRTL) {
        break label415;
      }
      k = 29;
      addView((View)localObject, LayoutHelper.createFrame(-1, -1.0F, j | 0x30, f, 0.0F, k, 0.0F));
      this.valueTextView = new TextView(paramContext);
      localTextView = this.valueTextView;
      if (paramInt != 1) {
        break label506;
      }
      localObject = "dialogTextBlue";
      localTextView.setTextColor(Theme.getColor((String)localObject));
      this.valueTextView.setTextSize(1, 16.0F);
      this.valueTextView.setLines(1);
      this.valueTextView.setMaxLines(1);
      this.valueTextView.setSingleLine(true);
      this.valueTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label513;
      }
      j = 3;
      ((TextView)localObject).setGravity(j | 0x10);
      localObject = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label519;
      }
      j = 3;
      addView((View)localObject, LayoutHelper.createFrame(-2, -1.0F, j | 0x30, 17.0F, 0.0F, 17.0F, 0.0F));
      if (paramInt != 1) {
        break label525;
      }
      bool = true;
      label336:
      this.checkBox = new CheckBoxSquare(paramContext, bool);
      if (paramInt != 2) {
        break label537;
      }
      paramContext = this.checkBox;
      if (!LocaleController.isRTL) {
        break label531;
      }
    }
    for (;;)
    {
      addView(paramContext, LayoutHelper.createFrame(18, 18.0F, i | 0x30, 0.0F, 15.0F, 0.0F, 0.0F));
      return;
      localObject = "windowBackgroundWhiteBlackText";
      break;
      label396:
      j = 3;
      break label107;
      label402:
      j = 3;
      break label137;
      label408:
      k = 29;
      break label146;
      label415:
      k = 0;
      break label160;
      label421:
      localObject = this.textView;
      if (LocaleController.isRTL)
      {
        j = 5;
        label436:
        if (!LocaleController.isRTL) {
          break label492;
        }
        k = 17;
        label446:
        f = k;
        if (!LocaleController.isRTL) {
          break label499;
        }
      }
      label492:
      label499:
      for (k = 46;; k = 17)
      {
        addView((View)localObject, LayoutHelper.createFrame(-1, -1.0F, j | 0x30, f, 0.0F, k, 0.0F));
        break;
        j = 3;
        break label436;
        k = 46;
        break label446;
      }
      label506:
      localObject = "windowBackgroundWhiteValueText";
      break label210;
      label513:
      j = 5;
      break label279;
      label519:
      j = 5;
      break label304;
      label525:
      bool = false;
      break label336;
      label531:
      i = 3;
    }
    label537:
    paramContext = this.checkBox;
    if (LocaleController.isRTL) {
      if (!LocaleController.isRTL) {
        break label601;
      }
    }
    label601:
    for (paramInt = 0;; paramInt = 17)
    {
      f = paramInt;
      paramInt = m;
      if (LocaleController.isRTL) {
        paramInt = 17;
      }
      addView(paramContext, LayoutHelper.createFrame(18, 18.0F, i | 0x30, f, 15.0F, paramInt, 0.0F));
      return;
      i = 3;
      break;
    }
  }
  
  public CheckBoxSquare getCheckBox()
  {
    return this.checkBox;
  }
  
  public TextView getTextView()
  {
    return this.textView;
  }
  
  public TextView getValueTextView()
  {
    return this.valueTextView;
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
    paramInt2 = View.MeasureSpec.getSize(paramInt1);
    int i = AndroidUtilities.dp(48.0F);
    if (this.needDivider) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      setMeasuredDimension(paramInt2, paramInt1 + i);
      paramInt1 = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - AndroidUtilities.dp(34.0F);
      this.valueTextView.measure(View.MeasureSpec.makeMeasureSpec(paramInt1 / 2, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
      this.textView.measure(View.MeasureSpec.makeMeasureSpec(paramInt1 - this.valueTextView.getMeasuredWidth() - AndroidUtilities.dp(8.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
      this.checkBox.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(18.0F), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(18.0F), 1073741824));
      return;
    }
  }
  
  public void setChecked(boolean paramBoolean1, boolean paramBoolean2)
  {
    this.checkBox.setChecked(paramBoolean1, paramBoolean2);
  }
  
  public void setEnabled(boolean paramBoolean)
  {
    float f2 = 1.0F;
    super.setEnabled(paramBoolean);
    Object localObject = this.textView;
    if (paramBoolean)
    {
      f1 = 1.0F;
      ((TextView)localObject).setAlpha(f1);
      localObject = this.valueTextView;
      if (!paramBoolean) {
        break label68;
      }
      f1 = 1.0F;
      label37:
      ((TextView)localObject).setAlpha(f1);
      localObject = this.checkBox;
      if (!paramBoolean) {
        break label74;
      }
    }
    label68:
    label74:
    for (float f1 = f2;; f1 = 0.5F)
    {
      ((CheckBoxSquare)localObject).setAlpha(f1);
      return;
      f1 = 0.5F;
      break;
      f1 = 0.5F;
      break label37;
    }
  }
  
  public void setText(String paramString1, String paramString2, boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean bool = false;
    this.textView.setText(paramString1);
    this.checkBox.setChecked(paramBoolean1, false);
    this.valueTextView.setText(paramString2);
    this.needDivider = paramBoolean2;
    paramBoolean1 = bool;
    if (!paramBoolean2) {
      paramBoolean1 = true;
    }
    setWillNotDraw(paramBoolean1);
  }
  
  public void setTextColor(int paramInt)
  {
    this.textView.setTextColor(paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/CheckBoxCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */