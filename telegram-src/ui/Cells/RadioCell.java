package org.telegram.ui.Cells;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils.TruncateAt;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;

public class RadioCell
  extends FrameLayout
{
  private boolean needDivider;
  private RadioButton radioButton;
  private TextView textView;
  
  public RadioCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.textView.setTextSize(1, 16.0F);
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
    this.textView.setEllipsize(TextUtils.TruncateAt.END);
    TextView localTextView = this.textView;
    int i;
    label113:
    label190:
    label200:
    float f;
    if (LocaleController.isRTL)
    {
      i = 5;
      localTextView.setGravity(i | 0x10);
      localTextView = this.textView;
      if (!LocaleController.isRTL) {
        break label242;
      }
      i = 5;
      addView(localTextView, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, 17.0F, 0.0F, 17.0F, 0.0F));
      this.radioButton = new RadioButton(paramContext);
      this.radioButton.setSize(AndroidUtilities.dp(20.0F));
      this.radioButton.setColor(Theme.getColor("radioBackground"), Theme.getColor("radioBackgroundChecked"));
      paramContext = this.radioButton;
      if (!LocaleController.isRTL) {
        break label247;
      }
      i = j;
      if (!LocaleController.isRTL) {
        break label252;
      }
      j = 18;
      f = j;
      if (!LocaleController.isRTL) {
        break label258;
      }
    }
    label242:
    label247:
    label252:
    label258:
    for (j = 0;; j = 18)
    {
      addView(paramContext, LayoutHelper.createFrame(22, 22.0F, i | 0x30, f, 13.0F, j, 0.0F));
      return;
      i = 3;
      break;
      i = 3;
      break label113;
      i = 5;
      break label190;
      j = 0;
      break label200;
    }
  }
  
  public boolean isChecked()
  {
    return this.radioButton.isChecked();
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
      paramInt1 = getMeasuredWidth();
      paramInt2 = getPaddingLeft();
      i = getPaddingRight();
      int j = AndroidUtilities.dp(34.0F);
      this.radioButton.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(22.0F), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(22.0F), 1073741824));
      this.textView.measure(View.MeasureSpec.makeMeasureSpec(paramInt1 - paramInt2 - i - j, 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
      return;
    }
  }
  
  public void setChecked(boolean paramBoolean1, boolean paramBoolean2)
  {
    this.radioButton.setChecked(paramBoolean1, paramBoolean2);
  }
  
  public void setEnabled(boolean paramBoolean, ArrayList<Animator> paramArrayList)
  {
    float f1 = 1.0F;
    float f2;
    if (paramArrayList != null)
    {
      Object localObject = this.textView;
      if (paramBoolean)
      {
        f2 = 1.0F;
        paramArrayList.add(ObjectAnimator.ofFloat(localObject, "alpha", new float[] { f2 }));
        localObject = this.radioButton;
        if (!paramBoolean) {
          break label76;
        }
      }
      for (;;)
      {
        paramArrayList.add(ObjectAnimator.ofFloat(localObject, "alpha", new float[] { f1 }));
        return;
        f2 = 0.5F;
        break;
        label76:
        f1 = 0.5F;
      }
    }
    paramArrayList = this.textView;
    if (paramBoolean)
    {
      f2 = 1.0F;
      paramArrayList.setAlpha(f2);
      paramArrayList = this.radioButton;
      if (!paramBoolean) {
        break label122;
      }
    }
    for (;;)
    {
      paramArrayList.setAlpha(f1);
      return;
      f2 = 0.5F;
      break;
      label122:
      f1 = 0.5F;
    }
  }
  
  public void setText(String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean bool = false;
    this.textView.setText(paramString);
    this.radioButton.setChecked(paramBoolean1, false);
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


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/RadioCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */