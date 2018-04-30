package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

public class ChatLoadingCell
  extends FrameLayout
{
  private FrameLayout frameLayout;
  private RadialProgressView progressBar;
  
  public ChatLoadingCell(Context paramContext)
  {
    super(paramContext);
    this.frameLayout = new FrameLayout(paramContext);
    this.frameLayout.setBackgroundResource(2131165672);
    this.frameLayout.getBackground().setColorFilter(Theme.colorFilter);
    addView(this.frameLayout, LayoutHelper.createFrame(36, 36, 17));
    this.progressBar = new RadialProgressView(paramContext);
    this.progressBar.setSize(AndroidUtilities.dp(28.0F));
    this.progressBar.setProgressColor(Theme.getColor("chat_serviceText"));
    this.frameLayout.addView(this.progressBar, LayoutHelper.createFrame(32, 32, 17));
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


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ChatLoadingCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */