package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class TextBlockCell
  extends FrameLayout
{
  private boolean needDivider;
  private TextView textView;
  
  public TextBlockCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.textView.setTextSize(1, 16.0F);
    paramContext = this.textView;
    if (LocaleController.isRTL)
    {
      i = 5;
      paramContext.setGravity(i | 0x10);
      paramContext = this.textView;
      if (!LocaleController.isRTL) {
        break label104;
      }
    }
    label104:
    for (int i = j;; i = 3)
    {
      addView(paramContext, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, 17.0F, 10.0F, 17.0F, 10.0F));
      return;
      i = 3;
      break;
    }
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(getPaddingLeft(), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), paramInt2);
  }
  
  public void setText(String paramString, boolean paramBoolean)
  {
    this.textView.setText(paramString);
    this.needDivider = paramBoolean;
    if (!paramBoolean) {}
    for (paramBoolean = true;; paramBoolean = false)
    {
      setWillNotDraw(paramBoolean);
      return;
    }
  }
  
  public void setTextColor(int paramInt)
  {
    this.textView.setTextColor(paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/TextBlockCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */