package ir.eitaa.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import ir.eitaa.messenger.AndroidUtilities;

public class ShadowSectionCell
  extends View
{
  private int size = 12;
  
  public ShadowSectionCell(Context paramContext)
  {
    super(paramContext);
    setBackgroundResource(2130837694);
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(this.size), 1073741824));
  }
  
  public void setSize(int paramInt)
  {
    this.size = paramInt;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/ShadowSectionCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */