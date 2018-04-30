package org.telegram.ui.Components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import org.telegram.messenger.AndroidUtilities;

public class ShareLocationDrawable
  extends Drawable
{
  private Drawable drawable;
  private Drawable drawableLeft;
  private Drawable drawableRight;
  private boolean isSmall;
  private long lastUpdateTime = 0L;
  private float[] progress = { 0.0F, -0.5F };
  
  public ShareLocationDrawable(Context paramContext, boolean paramBoolean)
  {
    this.isSmall = paramBoolean;
    if (paramBoolean)
    {
      this.drawable = paramContext.getResources().getDrawable(2131165646);
      this.drawableLeft = paramContext.getResources().getDrawable(2131165647);
      this.drawableRight = paramContext.getResources().getDrawable(2131165648);
      return;
    }
    this.drawable = paramContext.getResources().getDrawable(2131165190);
    this.drawableLeft = paramContext.getResources().getDrawable(2131165191);
    this.drawableRight = paramContext.getResources().getDrawable(2131165192);
  }
  
  private void update()
  {
    long l1 = System.currentTimeMillis();
    long l2 = l1 - this.lastUpdateTime;
    this.lastUpdateTime = l1;
    l1 = l2;
    if (l2 > 16L) {
      l1 = 16L;
    }
    int i = 0;
    while (i < 2)
    {
      if (this.progress[i] >= 1.0F) {
        this.progress[i] = 0.0F;
      }
      float[] arrayOfFloat = this.progress;
      arrayOfFloat[i] += (float)l1 / 1300.0F;
      if (this.progress[i] > 1.0F) {
        this.progress[i] = 1.0F;
      }
      i += 1;
    }
    invalidateSelf();
  }
  
  public void draw(Canvas paramCanvas)
  {
    if (this.isSmall) {}
    int i;
    int k;
    int m;
    for (float f1 = 30.0F;; f1 = 120.0F)
    {
      i = AndroidUtilities.dp(f1);
      k = getBounds().top + (getIntrinsicHeight() - i) / 2;
      m = getBounds().left + (getIntrinsicWidth() - i) / 2;
      this.drawable.setBounds(m, k, this.drawable.getIntrinsicWidth() + m, this.drawable.getIntrinsicHeight() + k);
      this.drawable.draw(paramCanvas);
      i = 0;
      for (;;)
      {
        if (i >= 2) {
          break label470;
        }
        if (this.progress[i] >= 0.0F) {
          break;
        }
        i += 1;
      }
    }
    float f2 = 0.5F + 0.5F * this.progress[i];
    label153:
    int n;
    label171:
    int i1;
    label189:
    int i2;
    label227:
    label237:
    int i3;
    int j;
    if (this.isSmall)
    {
      f1 = 2.5F;
      n = AndroidUtilities.dp(f1 * f2);
      if (!this.isSmall) {
        break label417;
      }
      f1 = 6.5F;
      i1 = AndroidUtilities.dp(f1 * f2);
      if (!this.isSmall) {
        break label423;
      }
      f1 = 6.0F;
      i2 = AndroidUtilities.dp(f1 * this.progress[i]);
      if (this.progress[i] >= 0.5F) {
        break label429;
      }
      f1 = this.progress[i] / 0.5F;
      if (!this.isSmall) {
        break label448;
      }
      f2 = 7.0F;
      i3 = AndroidUtilities.dp(f2) + m - i2;
      int i4 = this.drawable.getIntrinsicHeight() / 2;
      if (!this.isSmall) {
        break label454;
      }
      j = 0;
      label270:
      j = k + i4 - j;
      this.drawableLeft.setAlpha((int)(255.0F * f1));
      this.drawableLeft.setBounds(i3 - n, j - i1, i3 + n, j + i1);
      this.drawableLeft.draw(paramCanvas);
      i3 = this.drawable.getIntrinsicWidth();
      if (!this.isSmall) {
        break label464;
      }
    }
    label417:
    label423:
    label429:
    label448:
    label454:
    label464:
    for (f2 = 7.0F;; f2 = 42.0F)
    {
      i2 = m + i3 - AndroidUtilities.dp(f2) + i2;
      this.drawableRight.setAlpha((int)(255.0F * f1));
      this.drawableRight.setBounds(i2 - n, j - i1, i2 + n, j + i1);
      this.drawableRight.draw(paramCanvas);
      break;
      f1 = 5.0F;
      break label153;
      f1 = 18.0F;
      break label171;
      f1 = 15.0F;
      break label189;
      f1 = 1.0F - (this.progress[i] - 0.5F) / 0.5F;
      break label227;
      f2 = 42.0F;
      break label237;
      j = AndroidUtilities.dp(7.0F);
      break label270;
    }
    label470:
    update();
  }
  
  public int getIntrinsicHeight()
  {
    if (this.isSmall) {}
    for (float f = 40.0F;; f = 180.0F) {
      return AndroidUtilities.dp(f);
    }
  }
  
  public int getIntrinsicWidth()
  {
    if (this.isSmall) {}
    for (float f = 40.0F;; f = 120.0F) {
      return AndroidUtilities.dp(f);
    }
  }
  
  public int getOpacity()
  {
    return 0;
  }
  
  public void setAlpha(int paramInt) {}
  
  public void setColorFilter(ColorFilter paramColorFilter)
  {
    this.drawable.setColorFilter(paramColorFilter);
    this.drawableLeft.setColorFilter(paramColorFilter);
    this.drawableRight.setColorFilter(paramColorFilter);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/ShareLocationDrawable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */