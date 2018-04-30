package org.telegram.ui.Cells;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class DrawerActionCell
  extends FrameLayout
{
  private TextView textView;
  
  public DrawerActionCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("chats_menuItemText"));
    this.textView.setTextSize(1, 15.0F);
    this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
    this.textView.setGravity(19);
    this.textView.setCompoundDrawablePadding(AndroidUtilities.dp(29.0F));
    addView(this.textView, LayoutHelper.createFrame(-1, -1.0F, 51, 19.0F, 0.0F, 16.0F, 0.0F));
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.textView.setTextColor(Theme.getColor("chats_menuItemText"));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0F), 1073741824));
  }
  
  public void setTextAndIcon(String paramString, int paramInt)
  {
    try
    {
      this.textView.setText(paramString);
      paramString = getResources().getDrawable(paramInt);
      if (paramString != null) {
        paramString.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chats_menuItemIcon"), PorterDuff.Mode.MULTIPLY));
      }
      this.textView.setCompoundDrawablesWithIntrinsicBounds(paramString, null, null, null);
      return;
    }
    catch (Throwable paramString)
    {
      FileLog.e(paramString);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/DrawerActionCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */