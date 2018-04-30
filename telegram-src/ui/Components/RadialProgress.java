package org.telegram.ui.Components;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class RadialProgress
{
  private static DecelerateInterpolator decelerateInterpolator;
  private boolean alphaForMiniPrevious = true;
  private boolean alphaForPrevious = true;
  private float animatedAlphaValue = 1.0F;
  private float animatedProgressValue = 0.0F;
  private float animationProgressStart = 0.0F;
  private Drawable checkBackgroundDrawable;
  private CheckDrawable checkDrawable;
  private RectF cicleRect = new RectF();
  private Drawable currentDrawable;
  private Drawable currentMiniDrawable;
  private boolean currentMiniWithRound;
  private float currentProgress = 0.0F;
  private long currentProgressTime = 0L;
  private boolean currentWithRound;
  private int diff = AndroidUtilities.dp(4.0F);
  private boolean drawMiniProgress;
  private boolean hideCurrentDrawable;
  private long lastUpdateTime = 0L;
  private Bitmap miniDrawBitmap;
  private Canvas miniDrawCanvas;
  private Paint miniProgressBackgroundPaint;
  private Paint miniProgressPaint;
  private float overrideAlpha = 1.0F;
  private View parent;
  private boolean previousCheckDrawable;
  private Drawable previousDrawable;
  private Drawable previousMiniDrawable;
  private boolean previousMiniWithRound;
  private boolean previousWithRound;
  private int progressColor = -1;
  private Paint progressPaint;
  private RectF progressRect = new RectF();
  private float radOffset = 0.0F;
  
  public RadialProgress(View paramView)
  {
    if (decelerateInterpolator == null) {
      decelerateInterpolator = new DecelerateInterpolator();
    }
    this.progressPaint = new Paint(1);
    this.progressPaint.setStyle(Paint.Style.STROKE);
    this.progressPaint.setStrokeCap(Paint.Cap.ROUND);
    this.progressPaint.setStrokeWidth(AndroidUtilities.dp(3.0F));
    this.miniProgressPaint = new Paint(1);
    this.miniProgressPaint.setStyle(Paint.Style.STROKE);
    this.miniProgressPaint.setStrokeCap(Paint.Cap.ROUND);
    this.miniProgressPaint.setStrokeWidth(AndroidUtilities.dp(2.0F));
    this.miniProgressBackgroundPaint = new Paint(1);
    this.parent = paramView;
  }
  
  private void invalidateParent()
  {
    int i = AndroidUtilities.dp(2.0F);
    this.parent.invalidate((int)this.progressRect.left - i, (int)this.progressRect.top - i, (int)this.progressRect.right + i * 2, (int)this.progressRect.bottom + i * 2);
  }
  
  private void updateAnimation(boolean paramBoolean)
  {
    boolean bool2 = false;
    boolean bool1 = false;
    long l1 = System.currentTimeMillis();
    long l2 = l1 - this.lastUpdateTime;
    this.lastUpdateTime = l1;
    if ((this.checkBackgroundDrawable != null) && ((this.currentDrawable == this.checkBackgroundDrawable) || (this.previousDrawable == this.checkBackgroundDrawable)) && (this.checkDrawable.updateAnimation(l2))) {
      invalidateParent();
    }
    float f;
    if (paramBoolean) {
      if (this.animatedProgressValue != 1.0F)
      {
        this.radOffset += (float)(360L * l2) / 3000.0F;
        f = this.currentProgress - this.animationProgressStart;
        if (f > 0.0F)
        {
          this.currentProgressTime += l2;
          if (this.currentProgressTime >= 300L)
          {
            this.animatedProgressValue = this.currentProgress;
            this.animationProgressStart = this.currentProgress;
            this.currentProgressTime = 0L;
          }
        }
        else
        {
          invalidateParent();
        }
      }
      else
      {
        if (!this.drawMiniProgress) {
          break label271;
        }
        if ((this.animatedProgressValue >= 1.0F) && (this.previousMiniDrawable != null))
        {
          this.animatedAlphaValue -= (float)l2 / 200.0F;
          if (this.animatedAlphaValue <= 0.0F)
          {
            this.animatedAlphaValue = 0.0F;
            this.previousMiniDrawable = null;
            paramBoolean = bool1;
            if (this.currentMiniDrawable != null) {
              paramBoolean = true;
            }
            this.drawMiniProgress = paramBoolean;
          }
          invalidateParent();
        }
      }
    }
    label271:
    label396:
    do
    {
      do
      {
        do
        {
          return;
          this.animatedProgressValue = (this.animationProgressStart + decelerateInterpolator.getInterpolation((float)this.currentProgressTime / 300.0F) * f);
          break;
        } while ((this.animatedProgressValue < 1.0F) || (this.previousDrawable == null));
        this.animatedAlphaValue -= (float)l2 / 200.0F;
        if (this.animatedAlphaValue <= 0.0F)
        {
          this.animatedAlphaValue = 0.0F;
          this.previousDrawable = null;
        }
        invalidateParent();
        return;
        if (!this.drawMiniProgress) {
          break label396;
        }
      } while (this.previousMiniDrawable == null);
      this.animatedAlphaValue -= (float)l2 / 200.0F;
      if (this.animatedAlphaValue <= 0.0F)
      {
        this.animatedAlphaValue = 0.0F;
        this.previousMiniDrawable = null;
        paramBoolean = bool2;
        if (this.currentMiniDrawable != null) {
          paramBoolean = true;
        }
        this.drawMiniProgress = paramBoolean;
      }
      invalidateParent();
      return;
    } while (this.previousDrawable == null);
    this.animatedAlphaValue -= (float)l2 / 200.0F;
    if (this.animatedAlphaValue <= 0.0F)
    {
      this.animatedAlphaValue = 0.0F;
      this.previousDrawable = null;
    }
    invalidateParent();
  }
  
  public void draw(Canvas paramCanvas)
  {
    if ((this.drawMiniProgress) && (this.currentDrawable != null))
    {
      if (this.miniDrawCanvas != null) {
        this.miniDrawBitmap.eraseColor(0);
      }
      this.currentDrawable.setAlpha((int)(255.0F * this.overrideAlpha));
      int i;
      int j;
      float f1;
      float f2;
      label150:
      int k;
      float f3;
      if (this.miniDrawCanvas != null)
      {
        this.currentDrawable.setBounds(0, 0, (int)this.progressRect.width(), (int)this.progressRect.height());
        this.currentDrawable.draw(this.miniDrawCanvas);
        if (Math.abs(this.progressRect.width() - AndroidUtilities.dp(44.0F)) >= AndroidUtilities.density) {
          break label685;
        }
        i = 0;
        j = 20;
        f1 = this.progressRect.centerX() + AndroidUtilities.dp(16);
        f2 = this.progressRect.centerY() + AndroidUtilities.dp(16);
        k = j / 2;
        float f4 = 1.0F;
        f3 = f4;
        if (this.previousMiniDrawable != null)
        {
          f3 = f4;
          if (this.alphaForMiniPrevious) {
            f3 = this.animatedAlphaValue * this.overrideAlpha;
          }
        }
        if (this.miniDrawCanvas == null) {
          break label727;
        }
        this.miniDrawCanvas.drawCircle(AndroidUtilities.dp(j + 18 + i), AndroidUtilities.dp(j + 18 + i), AndroidUtilities.dp(k + 1) * f3, Theme.checkboxSquare_eraserPaint);
        if (this.miniDrawCanvas != null) {
          paramCanvas.drawBitmap(this.miniDrawBitmap, (int)this.progressRect.left, (int)this.progressRect.top, null);
        }
        if (this.previousMiniDrawable != null)
        {
          if (!this.alphaForMiniPrevious) {
            break label837;
          }
          this.previousMiniDrawable.setAlpha((int)(255.0F * this.animatedAlphaValue * this.overrideAlpha));
          label315:
          this.previousMiniDrawable.setBounds((int)(f1 - AndroidUtilities.dp(k) * f3), (int)(f2 - AndroidUtilities.dp(k) * f3), (int)(AndroidUtilities.dp(k) * f3 + f1), (int)(AndroidUtilities.dp(k) * f3 + f2));
          this.previousMiniDrawable.draw(paramCanvas);
        }
        if ((!this.hideCurrentDrawable) && (this.currentMiniDrawable != null))
        {
          if (this.previousMiniDrawable == null) {
            break label855;
          }
          this.currentMiniDrawable.setAlpha((int)(255.0F * (1.0F - this.animatedAlphaValue) * this.overrideAlpha));
          label425:
          this.currentMiniDrawable.setBounds((int)(f1 - AndroidUtilities.dp(k)), (int)(f2 - AndroidUtilities.dp(k)), (int)(AndroidUtilities.dp(k) + f1), (int)(AndroidUtilities.dp(k) + f2));
          this.currentMiniDrawable.draw(paramCanvas);
        }
        if ((!this.currentMiniWithRound) && (!this.previousMiniWithRound)) {
          break label891;
        }
        this.miniProgressPaint.setColor(this.progressColor);
        if (!this.previousMiniWithRound) {
          break label873;
        }
        this.miniProgressPaint.setAlpha((int)(255.0F * this.animatedAlphaValue * this.overrideAlpha));
      }
      for (;;)
      {
        this.cicleRect.set(f1 - AndroidUtilities.dp(k - 2) * f3, f2 - AndroidUtilities.dp(k - 2) * f3, AndroidUtilities.dp(k - 2) * f3 + f1, AndroidUtilities.dp(k - 2) * f3 + f2);
        paramCanvas.drawArc(this.cicleRect, this.radOffset - 90.0F, Math.max(4.0F, 360.0F * this.animatedProgressValue), false, this.miniProgressPaint);
        updateAnimation(true);
        return;
        this.currentDrawable.setBounds((int)this.progressRect.left, (int)this.progressRect.top, (int)this.progressRect.right, (int)this.progressRect.bottom);
        this.currentDrawable.draw(paramCanvas);
        break;
        label685:
        i = 2;
        j = 22;
        f1 = this.progressRect.centerX() + AndroidUtilities.dp(18.0F);
        f2 = this.progressRect.centerY() + AndroidUtilities.dp(18.0F);
        break label150;
        label727:
        this.miniProgressBackgroundPaint.setColor(this.progressColor);
        if ((this.previousMiniDrawable != null) && (this.currentMiniDrawable == null)) {
          this.miniProgressBackgroundPaint.setAlpha((int)(255.0F * this.animatedAlphaValue * this.overrideAlpha));
        }
        for (;;)
        {
          paramCanvas.drawCircle(f1, f2, AndroidUtilities.dp(12.0F), this.miniProgressBackgroundPaint);
          break;
          if ((this.previousMiniDrawable != null) && (this.currentMiniDrawable == null)) {
            this.miniProgressBackgroundPaint.setAlpha((int)(255.0F * this.overrideAlpha));
          } else {
            this.miniProgressBackgroundPaint.setAlpha(255);
          }
        }
        label837:
        this.previousMiniDrawable.setAlpha((int)(255.0F * this.overrideAlpha));
        break label315;
        label855:
        this.currentMiniDrawable.setAlpha((int)(255.0F * this.overrideAlpha));
        break label425;
        label873:
        this.miniProgressPaint.setAlpha((int)(255.0F * this.overrideAlpha));
      }
      label891:
      updateAnimation(false);
      return;
    }
    if (this.previousDrawable != null)
    {
      if (this.alphaForPrevious)
      {
        this.previousDrawable.setAlpha((int)(255.0F * this.animatedAlphaValue * this.overrideAlpha));
        this.previousDrawable.setBounds((int)this.progressRect.left, (int)this.progressRect.top, (int)this.progressRect.right, (int)this.progressRect.bottom);
        this.previousDrawable.draw(paramCanvas);
      }
    }
    else
    {
      if ((!this.hideCurrentDrawable) && (this.currentDrawable != null))
      {
        if (this.previousDrawable == null) {
          break label1237;
        }
        this.currentDrawable.setAlpha((int)(255.0F * (1.0F - this.animatedAlphaValue) * this.overrideAlpha));
        label1021:
        this.currentDrawable.setBounds((int)this.progressRect.left, (int)this.progressRect.top, (int)this.progressRect.right, (int)this.progressRect.bottom);
        this.currentDrawable.draw(paramCanvas);
      }
      if ((!this.currentWithRound) && (!this.previousWithRound)) {
        break label1273;
      }
      this.progressPaint.setColor(this.progressColor);
      if (!this.previousWithRound) {
        break label1255;
      }
      this.progressPaint.setAlpha((int)(255.0F * this.animatedAlphaValue * this.overrideAlpha));
    }
    for (;;)
    {
      this.cicleRect.set(this.progressRect.left + this.diff, this.progressRect.top + this.diff, this.progressRect.right - this.diff, this.progressRect.bottom - this.diff);
      paramCanvas.drawArc(this.cicleRect, this.radOffset - 90.0F, Math.max(4.0F, 360.0F * this.animatedProgressValue), false, this.progressPaint);
      updateAnimation(true);
      return;
      this.previousDrawable.setAlpha((int)(255.0F * this.overrideAlpha));
      break;
      label1237:
      this.currentDrawable.setAlpha((int)(255.0F * this.overrideAlpha));
      break label1021;
      label1255:
      this.progressPaint.setAlpha((int)(255.0F * this.overrideAlpha));
    }
    label1273:
    updateAnimation(false);
  }
  
  public float getAlpha()
  {
    if ((this.previousDrawable != null) || (this.currentDrawable != null)) {
      return this.animatedAlphaValue;
    }
    return 0.0F;
  }
  
  public RectF getProgressRect()
  {
    return this.progressRect;
  }
  
  public boolean isDrawCheckDrawable()
  {
    return this.currentDrawable == this.checkBackgroundDrawable;
  }
  
  public void setAlphaForMiniPrevious(boolean paramBoolean)
  {
    this.alphaForMiniPrevious = paramBoolean;
  }
  
  public void setAlphaForPrevious(boolean paramBoolean)
  {
    this.alphaForPrevious = paramBoolean;
  }
  
  public void setBackground(Drawable paramDrawable, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.lastUpdateTime = System.currentTimeMillis();
    if ((paramBoolean2) && (this.currentDrawable != paramDrawable))
    {
      this.previousDrawable = this.currentDrawable;
      this.previousWithRound = this.currentWithRound;
      this.animatedAlphaValue = 1.0F;
      setProgress(1.0F, paramBoolean2);
    }
    for (;;)
    {
      this.currentWithRound = paramBoolean1;
      this.currentDrawable = paramDrawable;
      if (paramBoolean2) {
        break;
      }
      this.parent.invalidate();
      return;
      this.previousDrawable = null;
      this.previousWithRound = false;
    }
    invalidateParent();
  }
  
  public void setCheckBackground(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.checkDrawable == null)
    {
      this.checkDrawable = new CheckDrawable();
      this.checkBackgroundDrawable = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(48.0F), this.checkDrawable, 0);
    }
    Theme.setCombinedDrawableColor(this.checkBackgroundDrawable, Theme.getColor("chat_mediaLoaderPhoto"), false);
    Theme.setCombinedDrawableColor(this.checkBackgroundDrawable, Theme.getColor("chat_mediaLoaderPhotoIcon"), true);
    if (this.currentDrawable != this.checkBackgroundDrawable)
    {
      setBackground(this.checkBackgroundDrawable, paramBoolean1, paramBoolean2);
      this.checkDrawable.resetProgress(paramBoolean2);
    }
  }
  
  public void setDiff(int paramInt)
  {
    this.diff = paramInt;
  }
  
  public void setHideCurrentDrawable(boolean paramBoolean)
  {
    this.hideCurrentDrawable = paramBoolean;
  }
  
  public void setMiniBackground(Drawable paramDrawable, boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean bool = false;
    this.lastUpdateTime = System.currentTimeMillis();
    if ((paramBoolean2) && (this.currentMiniDrawable != paramDrawable))
    {
      this.previousMiniDrawable = this.currentMiniDrawable;
      this.previousMiniWithRound = this.currentMiniWithRound;
      this.animatedAlphaValue = 1.0F;
      setProgress(1.0F, paramBoolean2);
    }
    for (;;)
    {
      this.currentMiniWithRound = paramBoolean1;
      this.currentMiniDrawable = paramDrawable;
      if (this.previousMiniDrawable == null)
      {
        paramBoolean1 = bool;
        if (this.currentMiniDrawable == null) {}
      }
      else
      {
        paramBoolean1 = true;
      }
      this.drawMiniProgress = paramBoolean1;
      if ((this.drawMiniProgress) && (this.miniDrawBitmap == null)) {}
      try
      {
        this.miniDrawBitmap = Bitmap.createBitmap(AndroidUtilities.dp(48.0F), AndroidUtilities.dp(48.0F), Bitmap.Config.ARGB_8888);
        this.miniDrawCanvas = new Canvas(this.miniDrawBitmap);
        if (!paramBoolean2)
        {
          this.parent.invalidate();
          return;
          this.previousMiniDrawable = null;
          this.previousMiniWithRound = false;
          continue;
        }
        invalidateParent();
        return;
      }
      catch (Throwable paramDrawable)
      {
        for (;;) {}
      }
    }
  }
  
  public void setMiniProgressBackgroundColor(int paramInt)
  {
    this.miniProgressBackgroundPaint.setColor(paramInt);
  }
  
  public void setOverrideAlpha(float paramFloat)
  {
    this.overrideAlpha = paramFloat;
  }
  
  public void setProgress(float paramFloat, boolean paramBoolean)
  {
    boolean bool;
    if (this.drawMiniProgress) {
      if ((paramFloat != 1.0F) && (this.animatedAlphaValue != 0.0F) && (this.previousMiniDrawable != null))
      {
        this.animatedAlphaValue = 0.0F;
        this.previousMiniDrawable = null;
        if (this.currentMiniDrawable != null)
        {
          bool = true;
          this.drawMiniProgress = bool;
        }
      }
      else
      {
        label53:
        if (paramBoolean) {
          break label122;
        }
        this.animatedProgressValue = paramFloat;
      }
    }
    for (this.animationProgressStart = paramFloat;; this.animationProgressStart = this.animatedProgressValue)
    {
      this.currentProgress = paramFloat;
      this.currentProgressTime = 0L;
      invalidateParent();
      return;
      bool = false;
      break;
      if ((paramFloat == 1.0F) || (this.animatedAlphaValue == 0.0F) || (this.previousDrawable == null)) {
        break label53;
      }
      this.animatedAlphaValue = 0.0F;
      this.previousDrawable = null;
      break label53;
      label122:
      if (this.animatedProgressValue > paramFloat) {
        this.animatedProgressValue = paramFloat;
      }
    }
  }
  
  public void setProgressColor(int paramInt)
  {
    this.progressColor = paramInt;
  }
  
  public void setProgressRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.progressRect.set(paramInt1, paramInt2, paramInt3, paramInt4);
  }
  
  public void setStrikeWidth(int paramInt)
  {
    this.progressPaint.setStrokeWidth(paramInt);
  }
  
  public boolean swapBackground(Drawable paramDrawable)
  {
    if (this.currentDrawable != paramDrawable)
    {
      this.currentDrawable = paramDrawable;
      return true;
    }
    return false;
  }
  
  public boolean swapMiniBackground(Drawable paramDrawable)
  {
    boolean bool = false;
    if (this.currentMiniDrawable != paramDrawable)
    {
      this.currentMiniDrawable = paramDrawable;
      if ((this.previousMiniDrawable != null) || (this.currentMiniDrawable != null)) {
        bool = true;
      }
      this.drawMiniProgress = bool;
      return true;
    }
    return false;
  }
  
  private class CheckDrawable
    extends Drawable
  {
    private Paint paint = new Paint(1);
    private float progress;
    
    public CheckDrawable()
    {
      this.paint.setStyle(Paint.Style.STROKE);
      this.paint.setStrokeWidth(AndroidUtilities.dp(3.0F));
      this.paint.setStrokeCap(Paint.Cap.ROUND);
      this.paint.setColor(-1);
    }
    
    public void draw(Canvas paramCanvas)
    {
      float f = 1.0F;
      int i = getBounds().centerX() - AndroidUtilities.dp(12.0F);
      int j = getBounds().centerY() - AndroidUtilities.dp(6.0F);
      if (this.progress != 1.0F) {
        f = RadialProgress.decelerateInterpolator.getInterpolation(this.progress);
      }
      int k = (int)(AndroidUtilities.dp(7.0F) - AndroidUtilities.dp(6.0F) * f);
      int m = (int)(AndroidUtilities.dpf2(13.0F) - AndroidUtilities.dp(6.0F) * f);
      paramCanvas.drawLine(AndroidUtilities.dp(7.0F) + i, (int)AndroidUtilities.dpf2(13.0F) + j, i + k, j + m, this.paint);
      k = (int)(AndroidUtilities.dpf2(7.0F) + AndroidUtilities.dp(13.0F) * f);
      m = (int)(AndroidUtilities.dpf2(13.0F) - AndroidUtilities.dp(13.0F) * f);
      paramCanvas.drawLine((int)AndroidUtilities.dpf2(7.0F) + i, (int)AndroidUtilities.dpf2(13.0F) + j, i + k, j + m, this.paint);
    }
    
    public int getIntrinsicHeight()
    {
      return AndroidUtilities.dp(48.0F);
    }
    
    public int getIntrinsicWidth()
    {
      return AndroidUtilities.dp(48.0F);
    }
    
    public int getOpacity()
    {
      return -2;
    }
    
    public void resetProgress(boolean paramBoolean)
    {
      if (paramBoolean) {}
      for (float f = 0.0F;; f = 1.0F)
      {
        this.progress = f;
        return;
      }
    }
    
    public void setAlpha(int paramInt)
    {
      this.paint.setAlpha(paramInt);
    }
    
    public void setColorFilter(ColorFilter paramColorFilter)
    {
      this.paint.setColorFilter(paramColorFilter);
    }
    
    public boolean updateAnimation(long paramLong)
    {
      if (this.progress < 1.0F)
      {
        this.progress += (float)paramLong / 700.0F;
        if (this.progress > 1.0F) {
          this.progress = 1.0F;
        }
        return true;
      }
      return false;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/RadialProgress.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */