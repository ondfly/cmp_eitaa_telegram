package ir.eitaa.ui.Cells;

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
    super.onMeasure(paramInt1, View.MeasureSpec.makeMeasureSpec(this.cellHeight, 1073741824));
  }
  
  public void setHeight(int paramInt)
  {
    this.cellHeight = paramInt;
    requestLayout();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/EmptyCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */