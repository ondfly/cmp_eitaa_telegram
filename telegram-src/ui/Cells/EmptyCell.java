package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;

public class EmptyCell
  extends FrameLayout
{
  int cellHeight;
  
  public EmptyCell(Context paramContext)
  {
    this(paramContext, 8);
  }
  
  public EmptyCell(Context paramContext, int paramInt)
  {
    super(paramContext);
    this.cellHeight = paramInt;
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(this.cellHeight, 1073741824));
  }
  
  public void setHeight(int paramInt)
  {
    this.cellHeight = paramInt;
    requestLayout();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/EmptyCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */