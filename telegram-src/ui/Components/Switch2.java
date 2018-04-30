package org.telegram.ui.Components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Keep;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;

public class Switch2
  extends View
{
  private static Bitmap drawBitmap;
  private boolean attachedToWindow;
  private ObjectAnimator checkAnimator;
  private boolean isChecked;
  private boolean isDisabled;
  private Paint paint;
  private Paint paint2;
  private float progress;
  private RectF rectF = new RectF();
  
  public Switch2(Context paramContext)
  {
    super(paramContext);
    if ((drawBitmap == null) || (drawBitmap.getWidth() != AndroidUtilities.dp(24.0F)))
    {
      drawBitmap = Bitmap.createBitmap(AndroidUtilities.dp(24.0F), AndroidUtilities.dp(24.0F), Bitmap.Config.ARGB_8888);
      paramContext = new Canvas(drawBitmap);
      Paint localPaint = new Paint(1);
      localPaint.setShadowLayer(AndroidUtilities.dp(2.0F), 0.0F, 0.0F, 2130706432);
      paramContext.drawCircle(AndroidUtilities.dp(12.0F), AndroidUtilities.dp(12.0F), AndroidUtilities.dp(9.0F), localPaint);
    }
    try
    {
      paramContext.setBitmap(null);
      this.paint = new Paint(1);
      this.paint2 = new Paint(1);
      this.paint2.setStyle(Paint.Style.STROKE);
      this.paint2.setStrokeCap(Paint.Cap.ROUND);
      this.paint2.setStrokeWidth(AndroidUtilities.dp(2.0F));
      return;
    }
    catch (Exception paramContext)
    {
      for (;;) {}
    }
  }
  
  private void animateToCheckedState(boolean paramBoolean)
  {
    if (paramBoolean) {}
    for (float f = 1.0F;; f = 0.0F)
    {
      this.checkAnimator = ObjectAnimator.ofFloat(this, "progress", new float[] { f });
      this.checkAnimator.setDuration(250L);
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
    int k = AndroidUtilities.dp(36.0F);
    AndroidUtilities.dp(20.0F);
    int m = (getMeasuredWidth() - k) / 2;
    int n = (getMeasuredHeight() - AndroidUtilities.dp(14.0F)) / 2;
    int i = (int)((k - AndroidUtilities.dp(14.0F)) * this.progress) + m + AndroidUtilities.dp(7.0F);
    int j = getMeasuredHeight() / 2;
    int i1 = (int)(255.0F + -95.0F * this.progress);
    int i2 = (int)(176.0F + 38.0F * this.progress);
    int i3 = (int)(173.0F + 77.0F * this.progress);
    this.paint.setColor(0xFF000000 | (i1 & 0xFF) << 16 | (i2 & 0xFF) << 8 | i3 & 0xFF);
    this.rectF.set(m, n, m + k, AndroidUtilities.dp(14.0F) + n);
    paramCanvas.drawRoundRect(this.rectF, AndroidUtilities.dp(7.0F), AndroidUtilities.dp(7.0F), this.paint);
    k = (int)(219.0F + -151.0F * this.progress);
    m = (int)(88.0F + 80.0F * this.progress);
    n = (int)(92.0F + 142.0F * this.progress);
    this.paint.setColor(0xFF000000 | (k & 0xFF) << 16 | (m & 0xFF) << 8 | n & 0xFF);
    paramCanvas.drawBitmap(drawBitmap, i - AndroidUtilities.dp(12.0F), j - AndroidUtilities.dp(11.0F), null);
    paramCanvas.drawCircle(i, j, AndroidUtilities.dp(10.0F), this.paint);
    this.paint2.setColor(-1);
    i = (int)(i - (AndroidUtilities.dp(10.8F) - AndroidUtilities.dp(1.3F) * this.progress));
    j = (int)(j - (AndroidUtilities.dp(8.5F) - AndroidUtilities.dp(0.5F) * this.progress));
    i2 = (int)AndroidUtilities.dpf2(4.6F) + i;
    k = (int)(AndroidUtilities.dpf2(9.5F) + j);
    i3 = AndroidUtilities.dp(2.0F);
    m = AndroidUtilities.dp(2.0F);
    i1 = (int)AndroidUtilities.dpf2(7.5F) + i;
    int i5 = (int)AndroidUtilities.dpf2(5.4F) + j;
    int i4 = i1 + AndroidUtilities.dp(7.0F);
    n = i5 + AndroidUtilities.dp(7.0F);
    i1 = (int)(i1 + (i2 - i1) * this.progress);
    i5 = (int)(i5 + (k - i5) * this.progress);
    i2 = (int)(i4 + (i2 + i3 - i4) * this.progress);
    k = (int)(n + (k + m - n) * this.progress);
    paramCanvas.drawLine(i1, i5, i2, k, this.paint2);
    i = (int)AndroidUtilities.dpf2(7.5F) + i;
    j = (int)AndroidUtilities.dpf2(12.5F) + j;
    k = AndroidUtilities.dp(7.0F);
    m = AndroidUtilities.dp(7.0F);
    paramCanvas.drawLine(i, j, i + k, j - m, this.paint2);
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


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/Switch2.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */