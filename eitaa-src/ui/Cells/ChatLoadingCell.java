package ir.eitaa.ui.Cells;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.ui.ActionBar.Theme;
import ir.eitaa.ui.Components.LayoutHelper;

public class ChatLoadingCell
  extends FrameLayout
{
  private FrameLayout frameLayout;
  
  public ChatLoadingCell(Context paramContext)
  {
    super(paramContext);
    this.frameLayout = new FrameLayout(paramContext);
    this.frameLayout.setBackgroundResource(2130838001);
    this.frameLayout.getBackground().setColorFilter(Theme.colorFilter);
    addView(this.frameLayout, LayoutHelper.createFrame(36, 36, 17));
    paramContext = new ProgressBar(paramContext);
    try
    {
      paramContext.setIndeterminateDrawable(getResources().getDrawable(2130837803));
      paramContext.setIndeterminate(true);
      AndroidUtilities.setProgressBarAnimationDuration(paramContext, 1500);
      this.frameLayout.addView(paramContext, LayoutHelper.createFrame(32, 32, 17));
      return;
    }
    catch (Exception localException)
    {
      for (;;) {}
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(44.0F), 1073741824));
  }
  
  public void setProgressVisible(boolean paramBoolean)
  {
    FrameLayout localFrameLayout = this.frameLayout;
    if (paramBoolean) {}
    for (int i = 0;; i = 4)
    {
      localFrameLayout.setVisibility(i);
      return;
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/ChatLoadingCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */