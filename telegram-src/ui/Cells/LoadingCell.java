package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

public class LoadingCell
  extends FrameLayout
{
  private RadialProgressView progressBar;
  
  public LoadingCell(Context paramContext)
  {
    super(paramContext);
    this.progressBar = new RadialProgressView(paramContext);
    addView(this.progressBar, LayoutHelper.createFrame(-2, -2, 17));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(54.0F), 1073741824));
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/LoadingCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */