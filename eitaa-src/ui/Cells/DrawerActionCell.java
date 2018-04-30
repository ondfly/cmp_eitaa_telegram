package ir.eitaa.ui.Cells;

import android.content.Context;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.ui.Components.LayoutHelper;

public class DrawerActionCell
  extends FrameLayout
{
  private TextView textView;
  
  public DrawerActionCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(-12303292);
    this.textView.setTextSize(1, 15.0F);
    this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
    paramContext = this.textView;
    if (LocaleController.isRTL)
    {
      i = 5;
      paramContext.setGravity(i | 0x10);
      this.textView.setCompoundDrawablePadding(AndroidUtilities.dp(34.0F));
      paramContext = this.textView;
      if (!LocaleController.isRTL) {
        break label147;
      }
    }
    label147:
    for (int i = j;; i = 3)
    {
      addView(paramContext, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, 14.0F, 0.0F, 16.0F, 0.0F));
      return;
      i = 3;
      break;
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(paramInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0F), 1073741824));
  }
  
  public void setTextAndIcon(String paramString, int paramInt)
  {
    try
    {
      this.textView.setText(paramString);
      paramString = this.textView;
      int i;
      if (LocaleController.isRTL)
      {
        i = 0;
        if (!LocaleController.isRTL) {
          break label41;
        }
      }
      for (;;)
      {
        paramString.setCompoundDrawablesWithIntrinsicBounds(i, 0, paramInt, 0);
        return;
        i = paramInt;
        break;
        label41:
        paramInt = 0;
      }
      return;
    }
    catch (Throwable paramString)
    {
      FileLog.e("tmessages", paramString);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/DrawerActionCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */