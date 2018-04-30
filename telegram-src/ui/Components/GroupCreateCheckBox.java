package org.telegram.ui.Components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Keep;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class GroupCreateCheckBox
  extends View
{
  private static Paint eraser;
  private static Paint eraser2;
  private static final float progressBounceDiff = 0.2F;
  private boolean attachedToWindow;
  private Paint backgroundInnerPaint;
  private String backgroundKey = "groupcreate_checkboxCheck";
  private Paint backgroundPaint;
  private Canvas bitmapCanvas;
  private ObjectAnimator checkAnimator;
  private String checkKey = "groupcreate_checkboxCheck";
  private Paint checkPaint;
  private float checkScale = 1.0F;
  private Bitmap drawBitmap;
  private String innerKey = "groupcreate_checkbox";
  private int innerRadDiff;
  private boolean isCheckAnimation = true;
  private boolean isChecked;
  private float progress;
  
  public GroupCreateCheckBox(Context paramContext)
  {
    super(paramContext);
    if (eraser == null)
    {
      eraser = new Paint(1);
      eraser.setColor(0);
      eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      eraser2 = new Paint(1);
      eraser2.setColor(0);
      eraser2.setStyle(Paint.Style.STROKE);
      eraser2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }
    this.backgroundPaint = new Paint(1);
    this.backgroundInnerPaint = new Paint(1);
    this.checkPaint = new Paint(1);
    this.checkPaint.setStyle(Paint.Style.STROKE);
    this.innerRadDiff = AndroidUtilities.dp(2.0F);
    this.checkPaint.setStrokeWidth(AndroidUtilities.dp(1.5F));
    eraser2.setStrokeWidth(AndroidUtilities.dp(28.0F));
    this.drawBitmap = Bitmap.createBitmap(AndroidUtilities.dp(24.0F), AndroidUtilities.dp(24.0F), Bitmap.Config.ARGB_4444);
    this.bitmapCanvas = new Canvas(this.drawBitmap);
    updateColors();
  }
  
  private void animateToCheckedState(boolean paramBoolean)
  {
    this.isCheckAnimation = paramBoolean;
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
    updateColors();
    this.attachedToWindow = true;
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    this.attachedToWindow = false;
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (getVisibility() != 0) {}
    while (this.progress == 0.0F) {
      return;
    }
    int j = getMeasuredWidth() / 2;
    int i = getMeasuredHeight() / 2;
    eraser2.setStrokeWidth(AndroidUtilities.dp(30.0F));
    this.drawBitmap.eraseColor(0);
    float f2;
    float f3;
    label78:
    float f1;
    if (this.progress >= 0.5F)
    {
      f2 = 1.0F;
      if (this.progress >= 0.5F) {
        break label350;
      }
      f3 = 0.0F;
      if (!this.isCheckAnimation) {
        break label365;
      }
      f1 = this.progress;
      label90:
      if (f1 >= 0.2F) {
        break label375;
      }
      f1 = AndroidUtilities.dp(2.0F) * f1 / 0.2F;
    }
    for (;;)
    {
      if (f3 != 0.0F) {
        paramCanvas.drawCircle(j, i, j - AndroidUtilities.dp(2.0F) + AndroidUtilities.dp(2.0F) * f3 - f1, this.backgroundPaint);
      }
      f1 = j - this.innerRadDiff - f1;
      this.bitmapCanvas.drawCircle(j, i, f1, this.backgroundInnerPaint);
      this.bitmapCanvas.drawCircle(j, i, (1.0F - f2) * f1, eraser);
      paramCanvas.drawBitmap(this.drawBitmap, 0.0F, 0.0F, null);
      f1 = AndroidUtilities.dp(10.0F) * f3 * this.checkScale;
      f2 = AndroidUtilities.dp(5.0F) * f3 * this.checkScale;
      j -= AndroidUtilities.dp(1.0F);
      i += AndroidUtilities.dp(4.0F);
      f2 = (float)Math.sqrt(f2 * f2 / 2.0F);
      paramCanvas.drawLine(j, i, j - f2, i - f2, this.checkPaint);
      f1 = (float)Math.sqrt(f1 * f1 / 2.0F);
      j -= AndroidUtilities.dp(1.2F);
      paramCanvas.drawLine(j, i, j + f1, i - f1, this.checkPaint);
      return;
      f2 = this.progress / 0.5F;
      break;
      label350:
      f3 = (this.progress - 0.5F) / 0.5F;
      break label78;
      label365:
      f1 = 1.0F - this.progress;
      break label90;
      label375:
      if (f1 < 0.4F) {
        f1 = AndroidUtilities.dp(2.0F) - AndroidUtilities.dp(2.0F) * (f1 - 0.2F) / 0.2F;
      } else {
        f1 = 0.0F;
      }
    }
  }
  
  public void setCheckScale(float paramFloat)
  {
    this.checkScale = paramFloat;
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
  
  public void setColorKeysOverrides(String paramString1, String paramString2, String paramString3)
  {
    this.checkKey = paramString1;
    this.innerKey = paramString2;
    this.backgroundKey = paramString3;
    updateColors();
  }
  
  public void setInnerRadDiff(int paramInt)
  {
    this.innerRadDiff = paramInt;
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
  
  public void updateColors()
  {
    this.backgroundInnerPaint.setColor(Theme.getColor(this.innerKey));
    this.backgroundPaint.setColor(Theme.getColor(this.backgroundKey));
    this.checkPaint.setColor(Theme.getColor(this.checkKey));
    invalidate();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/GroupCreateCheckBox.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */