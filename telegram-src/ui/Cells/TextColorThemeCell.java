package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.Components.LayoutHelper;

public class TextColorThemeCell
  extends FrameLayout
{
  private static Paint colorPaint;
  private float alpha = 1.0F;
  private int currentColor;
  private boolean needDivider;
  private TextView textView;
  
  public TextColorThemeCell(Context paramContext)
  {
    super(paramContext);
    if (colorPaint == null) {
      colorPaint = new Paint(1);
    }
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(-14606047);
    this.textView.setTextSize(1, 16.0F);
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
    paramContext = this.textView;
    int i;
    label135:
    label145:
    float f;
    if (LocaleController.isRTL)
    {
      i = 5;
      paramContext.setGravity(i | 0x10);
      this.textView.setPadding(0, 0, 0, AndroidUtilities.dp(3.0F));
      paramContext = this.textView;
      if (!LocaleController.isRTL) {
        break label186;
      }
      i = j;
      if (!LocaleController.isRTL) {
        break label191;
      }
      j = 17;
      f = j;
      if (!LocaleController.isRTL) {
        break label198;
      }
    }
    label186:
    label191:
    label198:
    for (j = 53;; j = 17)
    {
      addView(paramContext, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, f, 0.0F, j, 0.0F));
      return;
      i = 3;
      break;
      i = 3;
      break label135;
      j = 53;
      break label145;
    }
  }
  
  public float getAlpha()
  {
    return this.alpha;
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.currentColor != 0)
    {
      colorPaint.setColor(this.currentColor);
      colorPaint.setAlpha((int)(255.0F * this.alpha));
      if (LocaleController.isRTL) {
        break label66;
      }
    }
    label66:
    for (float f = AndroidUtilities.dp(28.0F);; f = getMeasuredWidth() - AndroidUtilities.dp(28.0F))
    {
      paramCanvas.drawCircle(f, getMeasuredHeight() / 2, AndroidUtilities.dp(10.0F), colorPaint);
      return;
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
  
  public void setAlpha(float paramFloat)
  {
    this.alpha = paramFloat;
    invalidate();
  }
  
  public void setTextAndColor(String paramString, int paramInt)
  {
    this.textView.setText(paramString);
    this.currentColor = paramInt;
    if ((!this.needDivider) && (this.currentColor == 0)) {}
    for (boolean bool = true;; bool = false)
    {
      setWillNotDraw(bool);
      invalidate();
      return;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/TextColorThemeCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */