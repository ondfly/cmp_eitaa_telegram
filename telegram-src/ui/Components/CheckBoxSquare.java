package org.telegram.ui.Components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Keep;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class CheckBoxSquare
  extends View
{
  private static final float progressBounceDiff = 0.2F;
  private boolean attachedToWindow;
  private ObjectAnimator checkAnimator;
  private Bitmap drawBitmap = Bitmap.createBitmap(AndroidUtilities.dp(18.0F), AndroidUtilities.dp(18.0F), Bitmap.Config.ARGB_4444);
  private Canvas drawCanvas = new Canvas(this.drawBitmap);
  private boolean isAlert;
  private boolean isChecked;
  private boolean isDisabled;
  private float progress;
  private RectF rectF = new RectF();
  
  public CheckBoxSquare(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    this.isAlert = paramBoolean;
  }
  
  private void animateToCheckedState(boolean paramBoolean)
  {
    if (paramBoolean) {}
    for (float f = 1.0F;; f = 0.0F)
    {
      this.checkAnimator = ObjectAnimator.ofFloat(this, "progress", new float[] { f });
      this.checkAnimator.setDuration(300L);
      this.checkAnimator.start();
      return;
    }
  }
  
  private void cancelCheckAnimator()
  {
    if (this.checkAnimator != null) {
      this.checkAnimator.cancel();
    }
  }
  
  public float getProgress()
  {
    return this.progress;
  }
  
  public boolean isChecked()
  {
    return this.isChecked;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.attachedToWindow = true;
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    this.attachedToWindow = false;
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (getVisibility() != 0) {
      return;
    }
    int i;
    label37:
    int m;
    float f2;
    float f1;
    int j;
    label152:
    Paint localPaint;
    if (this.isAlert)
    {
      str = "dialogCheckboxSquareUnchecked";
      i = Theme.getColor(str);
      if (!this.isAlert) {
        break label524;
      }
      str = "dialogCheckboxSquareBackground";
      m = Theme.getColor(str);
      if (this.progress > 0.5F) {
        break label531;
      }
      f2 = this.progress / 0.5F;
      f1 = f2;
      j = (int)((Color.red(m) - Color.red(i)) * f2);
      int k = (int)((Color.green(m) - Color.green(i)) * f2);
      m = (int)((Color.blue(m) - Color.blue(i)) * f2);
      i = Color.rgb(Color.red(i) + j, Color.green(i) + k, Color.blue(i) + m);
      Theme.checkboxSquare_backgroundPaint.setColor(i);
      if (this.isDisabled)
      {
        localPaint = Theme.checkboxSquare_backgroundPaint;
        if (!this.isAlert) {
          break label554;
        }
        str = "dialogCheckboxSquareDisabled";
        label175:
        localPaint.setColor(Theme.getColor(str));
      }
      float f3 = AndroidUtilities.dp(1.0F) * f1;
      this.rectF.set(f3, f3, AndroidUtilities.dp(18.0F) - f3, AndroidUtilities.dp(18.0F) - f3);
      this.drawBitmap.eraseColor(0);
      this.drawCanvas.drawRoundRect(this.rectF, AndroidUtilities.dp(2.0F), AndroidUtilities.dp(2.0F), Theme.checkboxSquare_backgroundPaint);
      if (f2 != 1.0F)
      {
        f2 = Math.min(AndroidUtilities.dp(7.0F), AndroidUtilities.dp(7.0F) * f2 + f3);
        this.rectF.set(AndroidUtilities.dp(2.0F) + f2, AndroidUtilities.dp(2.0F) + f2, AndroidUtilities.dp(16.0F) - f2, AndroidUtilities.dp(16.0F) - f2);
        this.drawCanvas.drawRect(this.rectF, Theme.checkboxSquare_eraserPaint);
      }
      if (this.progress > 0.5F)
      {
        localPaint = Theme.checkboxSquare_checkPaint;
        if (!this.isAlert) {
          break label561;
        }
      }
    }
    label524:
    label531:
    label554:
    label561:
    for (String str = "dialogCheckboxSquareCheck";; str = "checkboxSquareCheck")
    {
      localPaint.setColor(Theme.getColor(str));
      i = (int)(AndroidUtilities.dp(7.5F) - AndroidUtilities.dp(5.0F) * (1.0F - f1));
      j = (int)(AndroidUtilities.dpf2(13.5F) - AndroidUtilities.dp(5.0F) * (1.0F - f1));
      this.drawCanvas.drawLine(AndroidUtilities.dp(7.5F), (int)AndroidUtilities.dpf2(13.5F), i, j, Theme.checkboxSquare_checkPaint);
      i = (int)(AndroidUtilities.dpf2(6.5F) + AndroidUtilities.dp(9.0F) * (1.0F - f1));
      j = (int)(AndroidUtilities.dpf2(13.5F) - AndroidUtilities.dp(9.0F) * (1.0F - f1));
      this.drawCanvas.drawLine((int)AndroidUtilities.dpf2(6.5F), (int)AndroidUtilities.dpf2(13.5F), i, j, Theme.checkboxSquare_checkPaint);
      paramCanvas.drawBitmap(this.drawBitmap, 0.0F, 0.0F, null);
      return;
      str = "checkboxSquareUnchecked";
      break;
      str = "checkboxSquareBackground";
      break label37;
      f1 = 2.0F - this.progress / 0.5F;
      f2 = 1.0F;
      Theme.checkboxSquare_backgroundPaint.setColor(m);
      break label152;
      str = "checkboxSquareDisabled";
      break label175;
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
  }
  
  public void setChecked(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean1 == this.isChecked) {
      return;
    }
    this.isChecked = paramBoolean1;
    if ((this.attachedToWindow) && (paramBoolean2))
    {
      animateToCheckedState(paramBoolean1);
      return;
    }
    cancelCheckAnimator();
    if (paramBoolean1) {}
    for (float f = 1.0F;; f = 0.0F)
    {
      setProgress(f);
      return;
    }
  }
  
  public void setDisabled(boolean paramBoolean)
  {
    this.isDisabled = paramBoolean;
    invalidate();
  }
  
  @Keep
  public void setProgress(float paramFloat)
  {
    if (this.progress == paramFloat) {
      return;
    }
    this.progress = paramFloat;
    invalidate();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/CheckBoxSquare.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */