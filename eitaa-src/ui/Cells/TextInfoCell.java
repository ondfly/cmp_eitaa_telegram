package ir.eitaa.ui.Cells;

import android.content.Context;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.ui.Components.LayoutHelper;

public class TextInfoCell
  extends FrameLayout
{
  private TextView textView;
  
  public TextInfoCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(-6052957);
    this.textView.setTextSize(1, 13.0F);
    this.textView.setGravity(17);
    this.textView.setPadding(0, AndroidUtilities.dp(19.0F), 0, AndroidUtilities.dp(19.0F));
    addView(this.textView, LayoutHelper.createFrame(-2, -2.0F, 17, 17.0F, 0.0F, 17.0F, 0.0F));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(paramInt1, View.MeasureSpec.makeMeasureSpec(0, 0));
  }
  
  public void setText(String paramString)
  {
    this.textView.setText(paramString);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/TextInfoCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */