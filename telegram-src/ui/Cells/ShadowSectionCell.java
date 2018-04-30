package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class ShadowSectionCell
  extends View
{
  private int size = 12;
  
  public ShadowSectionCell(Context paramContext)
  {
    super(paramContext);
    setBackgroundDrawable(Theme.getThemedDrawable(paramContext, 2131165331, "windowBackgroundGrayShadow"));
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


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ShadowSectionCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */