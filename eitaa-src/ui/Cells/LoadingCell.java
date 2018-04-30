package ir.eitaa.ui.Cells;

import android.content.Context;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.ui.Components.LayoutHelper;

public class LoadingCell
  extends FrameLayout
{
  public LoadingCell(Context paramContext)
  {
    super(paramContext);
    addView(new ProgressBar(paramContext), LayoutHelper.createFrame(-2, -2, 17));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(54.0F), 1073741824));
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/LoadingCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */