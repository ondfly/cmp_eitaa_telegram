package ir.eitaa.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.ui.Components.LayoutHelper;

@SuppressLint({"NewApi"})
public class PhotoAttachCameraCell
  extends FrameLayout
{
  public PhotoAttachCameraCell(Context paramContext)
  {
    super(paramContext);
    paramContext = new ImageView(paramContext);
    paramContext.setScaleType(ImageView.ScaleType.CENTER);
    paramContext.setImageResource(2130837779);
    paramContext.setBackgroundColor(-16777216);
    addView(paramContext, LayoutHelper.createFrame(80, 80.0F));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(86.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(80.0F), 1073741824));
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/PhotoAttachCameraCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */