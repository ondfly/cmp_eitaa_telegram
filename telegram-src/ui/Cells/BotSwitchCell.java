package org.telegram.ui.Cells;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class BotSwitchCell
  extends FrameLayout
{
  private TextView textView;
  
  public BotSwitchCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextSize(1, 15.0F);
    this.textView.setTextColor(Theme.getColor("chat_botSwitchToInlineText"));
    this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.textView.setSingleLine(true);
    this.textView.setEllipsize(TextUtils.TruncateAt.END);
    this.textView.setMaxLines(1);
    paramContext = this.textView;
    if (LocaleController.isRTL)
    {
      i = 5;
      paramContext.setGravity(i);
      paramContext = this.textView;
      if (!LocaleController.isRTL) {
        break label138;
      }
    }
    label138:
    for (int i = j;; i = 3)
    {
      addView(paramContext, LayoutHelper.createFrame(-2, -2.0F, i | 0x10, 14.0F, 0.0F, 14.0F, 0.0F));
      return;
      i = 3;
      break;
    }
  }
  
  public TextView getTextView()
  {
    return this.textView;
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(36.0F), 1073741824));
  }
  
  public void setText(String paramString)
  {
    this.textView.setText(paramString);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/BotSwitchCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */