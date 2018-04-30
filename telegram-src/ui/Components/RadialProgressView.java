package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class RadialProgressView
  extends View
{
  private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
  private RectF cicleRect = new RectF();
  private float currentCircleLength;
  private float currentProgressTime;
  private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
  private long lastUpdateTime;
  private int progressColor = Theme.getColor("progressCircle");
  private Paint progressPaint = new Paint(1);
  private float radOffset;
  private boolean risingCircleLength;
  private final float risingTime = 500.0F;
  private final float rotationTime = 2000.0F;
  private int size = AndroidUtilities.dp(40.0F);
  private boolean useSelfAlpha;
  
  public RadialProgressView(Context paramContext)
  {
    super(paramContext);
    this.progressPaint.setStyle(Paint.Style.STROKE);
    this.progressPaint.setStrokeCap(Paint.Cap.ROUND);
    this.progressPaint.setStrokeWidth(AndroidUtilities.dp(3.0F));
    this.progressPaint.setColor(this.progressColor);
  }
  
  private void updateAnimation()
  {
    long l3 = System.currentTimeMillis();
    long l2 = l3 - this.lastUpdateTime;
    long l1 = l2;
    if (l2 > 17L) {
      l1 = 17L;
    }
    this.lastUpdateTime = l3;
    this.radOffset += (float)(360L * l1) / 2000.0F;
    int i = (int)(this.radOffset / 360.0F);
    this.radOffset -= i * 360;
    this.currentProgressTime += (float)l1;
    if (this.currentProgressTime >= 500.0F) {
      this.currentProgressTime = 500.0F;
    }
    if (this.risingCircleLength)
    {
      this.currentCircleLength = (266.0F * this.accelerateInterpolator.getInterpolation(this.currentProgressTime / 500.0F) + 4.0F);
      if (this.currentProgressTime == 500.0F)
      {
        if (this.risingCircleLength)
        {
          this.radOffset += 270.0F;
          this.currentCircleLength = -266.0F;
        }
        if (this.risingCircleLength) {
          break label225;
        }
      }
    }
    label225:
    for (boolean bool = true;; bool = false)
    {
      this.risingCircleLength = bool;
      this.currentProgressTime = 0.0F;
      invalidate();
      return;
      this.currentCircleLength = (4.0F - (1.0F - this.decelerateInterpolator.getInterpolation(this.currentProgressTime / 500.0F)) * 270.0F);
      break;
    }
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    int i = (getMeasuredWidth() - this.size) / 2;
    int j = (getMeasuredHeight() - this.size) / 2;
    this.cicleRect.set(i, j, this.size + i, this.size + j);
    paramCanvas.drawArc(this.cicleRect, this.radOffset, this.currentCircleLength, false, this.progressPaint);
    updateAnimation();
  }
  
  @Keep
  public void setAlpha(float paramFloat)
  {
    super.setAlpha(paramFloat);
    if (this.useSelfAlpha)
    {
      Drawable localDrawable = getBackground();
      int i = (int)(255.0F * paramFloat);
      if (localDrawable != null) {
        localDrawable.setAlpha(i);
      }
      this.progressPaint.setAlpha(i);
    }
  }
  
  public void setProgressColor(int paramInt)
  {
    this.progressColor = paramInt;
    this.progressPaint.setColor(this.progressColor);
  }
  
  public void setSize(int paramInt)
  {
    this.size = paramInt;
    invalidate();
  }
  
  public void setUseSelfAlpha(boolean paramBoolean)
  {
    this.useSelfAlpha = paramBoolean;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/RadialProgressView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */