package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class SendingFileDrawable
  extends StatusDrawable
{
  private boolean isChat = false;
  private long lastUpdateTime = 0L;
  private float progress;
  private boolean started = false;
  
  private void update()
  {
    long l1 = System.currentTimeMillis();
    long l2 = l1 - this.lastUpdateTime;
    this.lastUpdateTime = l1;
    l1 = l2;
    if (l2 > 50L) {
      l1 = 50L;
    }
    for (this.progress += (float)l1 / 500.0F; this.progress > 1.0F; this.progress -= 1.0F) {}
    invalidateSelf();
  }
  
  public void draw(Canvas paramCanvas)
  {
    int i = 0;
    if (i < 3)
    {
      label28:
      float f2;
      label60:
      float f3;
      float f4;
      if (i == 0)
      {
        Theme.chat_statusRecordPaint.setAlpha((int)(this.progress * 255.0F));
        f2 = AndroidUtilities.dp(5.0F) * i + AndroidUtilities.dp(5.0F) * this.progress;
        if (!this.isChat) {
          break label204;
        }
        f1 = 3.0F;
        f3 = AndroidUtilities.dp(f1);
        f4 = AndroidUtilities.dp(4.0F);
        if (!this.isChat) {
          break label210;
        }
        f1 = 7.0F;
        label85:
        paramCanvas.drawLine(f2, f3, f2 + f4, AndroidUtilities.dp(f1), Theme.chat_statusRecordPaint);
        if (!this.isChat) {
          break label216;
        }
        f1 = 11.0F;
        label114:
        f3 = AndroidUtilities.dp(f1);
        f4 = AndroidUtilities.dp(4.0F);
        if (!this.isChat) {
          break label222;
        }
      }
      label204:
      label210:
      label216:
      label222:
      for (float f1 = 7.0F;; f1 = 8.0F)
      {
        paramCanvas.drawLine(f2, f3, f2 + f4, AndroidUtilities.dp(f1), Theme.chat_statusRecordPaint);
        i += 1;
        break;
        if (i == 2)
        {
          Theme.chat_statusRecordPaint.setAlpha((int)((1.0F - this.progress) * 255.0F));
          break label28;
        }
        Theme.chat_statusRecordPaint.setAlpha(255);
        break label28;
        f1 = 4.0F;
        break label60;
        f1 = 8.0F;
        break label85;
        f1 = 12.0F;
        break label114;
      }
    }
    if (this.started) {
      update();
    }
  }
  
  public int getIntrinsicHeight()
  {
    return AndroidUtilities.dp(14.0F);
  }
  
  public int getIntrinsicWidth()
  {
    return AndroidUtilities.dp(18.0F);
  }
  
  public int getOpacity()
  {
    return 0;
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
    this.started = false;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/SendingFileDrawable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */