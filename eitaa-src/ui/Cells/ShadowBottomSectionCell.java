package ir.eitaa.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import ir.eitaa.messenger.AndroidUtilities;

public class ShadowBottomSectionCell
  extends View
{
  public ShadowBottomSectionCell(Context paramContext)
  {
    super(paramContext);
    setBackgroundResource(2130837695);
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(paramInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(6.0F), 1073741824));
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/ShadowBottomSectionCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */