package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.animation.DecelerateInterpolator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;

public class PlayingGameDrawable
  extends StatusDrawable
{
  private int currentAccount = UserConfig.selectedAccount;
  private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
  private boolean isChat = false;
  private long lastUpdateTime = 0L;
  private Paint paint = new Paint(1);
  private float progress;
  private RectF rect = new RectF();
  private boolean started = false;
  
  private void checkUpdate()
  {
    if (this.started)
    {
      if (!NotificationCenter.getInstance(this.currentAccount).isAnimationInProgress()) {
        update();
      }
    }
    else {
      return;
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        PlayingGameDrawable.this.checkUpdate();
      }
    }, 100L);
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
    if (this.progress >= 1.0F) {
      this.progress = 0.0F;
    }
    this.progress += (float)l1 / 300.0F;
    if (this.progress > 1.0F) {
      this.progress = 1.0F;
    }
    invalidateSelf();
  }
  
  public void draw(Canvas paramCanvas)
  {
    int m = AndroidUtilities.dp(10.0F);
    int i = getBounds().top + (getIntrinsicHeight() - m) / 2;
    int j;
    label90:
    int k;
    label93:
    float f1;
    float f2;
    float f3;
    if (this.isChat)
    {
      this.paint.setColor(Theme.getColor("actionBarDefaultSubtitle"));
      this.rect.set(0.0F, i, m, i + m);
      if (this.progress >= 0.5F) {
        break label207;
      }
      j = (int)(35.0F * (1.0F - this.progress / 0.5F));
      k = 0;
      if (k >= 3) {
        break label293;
      }
      f1 = AndroidUtilities.dp(5.0F) * k + AndroidUtilities.dp(9.2F);
      f2 = AndroidUtilities.dp(5.0F);
      f3 = this.progress;
      if (k != 2) {
        break label226;
      }
      this.paint.setAlpha(Math.min(255, (int)(255.0F * this.progress / 0.5F)));
    }
    for (;;)
    {
      paramCanvas.drawCircle(f1 - f2 * f3, m / 2 + i, AndroidUtilities.dp(1.2F), this.paint);
      k += 1;
      break label93;
      i += AndroidUtilities.dp(1.0F);
      break;
      label207:
      j = (int)(35.0F * (this.progress - 0.5F) / 0.5F);
      break label90;
      label226:
      if (k == 0)
      {
        if (this.progress > 0.5F) {
          this.paint.setAlpha((int)(255.0F * (1.0F - (this.progress - 0.5F) / 0.5F)));
        } else {
          this.paint.setAlpha(255);
        }
      }
      else {
        this.paint.setAlpha(255);
      }
    }
    label293:
    this.paint.setAlpha(255);
    paramCanvas.drawArc(this.rect, j, 360 - j * 2, true, this.paint);
    this.paint.setColor(Theme.getColor("actionBarDefault"));
    paramCanvas.drawCircle(AndroidUtilities.dp(4.0F), m / 2 + i - AndroidUtilities.dp(2.0F), AndroidUtilities.dp(1.0F), this.paint);
    checkUpdate();
  }
  
  public int getIntrinsicHeight()
  {
    return AndroidUtilities.dp(18.0F);
  }
  
  public int getIntrinsicWidth()
  {
    return AndroidUtilities.dp(20.0F);
  }
  
  public int getOpacity()
  {
    return -2;
  }
  
  public void setAlpha(int paramInt) {}
  
  public void setColorFilter(ColorFilter paramColorFilter) {}
  
  public void setIsChat(boolean paramBoolean)
  {
    this.isChat = paramBoolean;
  }
  
  public void start()
  {
    this.lastUpdateTime = System.currentTimeMillis();
    this.started = true;
    invalidateSelf();
  }
  
  public void stop()
  {
    this.progress = 0.0F;
    this.started = false;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/PlayingGameDrawable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */