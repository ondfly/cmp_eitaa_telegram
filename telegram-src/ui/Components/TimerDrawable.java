package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.Theme;

public class TimerDrawable
  extends Drawable
{
  private Paint linePaint = new Paint(1);
  private Paint paint = new Paint(1);
  private int time = 0;
  private int timeHeight = 0;
  private StaticLayout timeLayout;
  private TextPaint timePaint = new TextPaint(1);
  private float timeWidth = 0.0F;
  
  public TimerDrawable(Context paramContext)
  {
    this.timePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.timePaint.setTextSize(AndroidUtilities.dp(11.0F));
    this.linePaint.setStrokeWidth(AndroidUtilities.dp(1.0F));
    this.linePaint.setStyle(Paint.Style.STROKE);
  }
  
  public void draw(Canvas paramCanvas)
  {
    int j = getIntrinsicWidth();
    int k = getIntrinsicHeight();
    if (this.time == 0)
    {
      this.paint.setColor(Theme.getColor("chat_secretTimerBackground"));
      this.linePaint.setColor(Theme.getColor("chat_secretTimerText"));
      paramCanvas.drawCircle(AndroidUtilities.dpf2(9.0F), AndroidUtilities.dpf2(9.0F), AndroidUtilities.dpf2(7.5F), this.paint);
      paramCanvas.drawCircle(AndroidUtilities.dpf2(9.0F), AndroidUtilities.dpf2(9.0F), AndroidUtilities.dpf2(8.0F), this.linePaint);
      this.paint.setColor(Theme.getColor("chat_secretTimerText"));
      paramCanvas.drawLine(AndroidUtilities.dp(9.0F), AndroidUtilities.dp(9.0F), AndroidUtilities.dp(13.0F), AndroidUtilities.dp(9.0F), this.linePaint);
      paramCanvas.drawLine(AndroidUtilities.dp(9.0F), AndroidUtilities.dp(5.0F), AndroidUtilities.dp(9.0F), AndroidUtilities.dp(9.5F), this.linePaint);
      paramCanvas.drawRect(AndroidUtilities.dpf2(7.0F), AndroidUtilities.dpf2(0.0F), AndroidUtilities.dpf2(11.0F), AndroidUtilities.dpf2(1.5F), this.paint);
    }
    for (;;)
    {
      if ((this.time != 0) && (this.timeLayout != null))
      {
        int i = 0;
        if (AndroidUtilities.density == 3.0F) {
          i = -1;
        }
        paramCanvas.translate((int)(j / 2 - Math.ceil(this.timeWidth / 2.0F)) + i, (k - this.timeHeight) / 2);
        this.timeLayout.draw(paramCanvas);
      }
      return;
      this.paint.setColor(Theme.getColor("chat_secretTimerBackground"));
      this.timePaint.setColor(Theme.getColor("chat_secretTimerText"));
      paramCanvas.drawCircle(AndroidUtilities.dp(9.5F), AndroidUtilities.dp(9.5F), AndroidUtilities.dp(9.5F), this.paint);
    }
  }
  
  public int getIntrinsicHeight()
  {
    return AndroidUtilities.dp(19.0F);
  }
  
  public int getIntrinsicWidth()
  {
    return AndroidUtilities.dp(19.0F);
  }
  
  public int getOpacity()
  {
    return 0;
  }
  
  public void setAlpha(int paramInt) {}
  
  public void setColorFilter(ColorFilter paramColorFilter) {}
  
  public void setTime(int paramInt)
  {
    this.time = paramInt;
    String str2;
    String str1;
    if ((this.time >= 1) && (this.time < 60))
    {
      str2 = "" + paramInt;
      str1 = str2;
      if (str2.length() < 2) {
        str1 = str2 + "s";
      }
    }
    for (;;)
    {
      this.timeWidth = this.timePaint.measureText(str1);
      try
      {
        this.timeLayout = new StaticLayout(str1, this.timePaint, (int)Math.ceil(this.timeWidth), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
        this.timeHeight = this.timeLayout.getHeight();
        invalidateSelf();
        return;
        if ((this.time >= 60) && (this.time < 3600))
        {
          str2 = "" + paramInt / 60;
          str1 = str2;
          if (str2.length() >= 2) {
            continue;
          }
          str1 = str2 + "m";
          continue;
        }
        if ((this.time >= 3600) && (this.time < 86400))
        {
          str2 = "" + paramInt / 60 / 60;
          str1 = str2;
          if (str2.length() >= 2) {
            continue;
          }
          str1 = str2 + "h";
          continue;
        }
        if ((this.time >= 86400) && (this.time < 604800))
        {
          str2 = "" + paramInt / 60 / 60 / 24;
          str1 = str2;
          if (str2.length() >= 2) {
            continue;
          }
          str1 = str2 + "d";
          continue;
        }
        str2 = "" + paramInt / 60 / 60 / 24 / 7;
        if (str2.length() < 2)
        {
          str1 = str2 + "w";
          continue;
        }
        str1 = str2;
        if (str2.length() <= 2) {
          continue;
        }
        str1 = "c";
      }
      catch (Exception localException)
      {
        for (;;)
        {
          this.timeLayout = null;
          FileLog.e(localException);
        }
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/TimerDrawable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */