package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class TextPriceCell
  extends FrameLayout
{
  private int dotLength;
  private String dotstring;
  private TextView textView;
  private TextView valueTextView;
  
  public TextPriceCell(Context paramContext)
  {
    super(paramContext);
    Object localObject;
    if (LocaleController.isRTL)
    {
      localObject = " .";
      this.dotstring = ((String)localObject);
      setWillNotDraw(false);
      this.textView = new TextView(paramContext);
      this.textView.setTextSize(1, 16.0F);
      this.textView.setLines(1);
      this.textView.setMaxLines(1);
      this.textView.setSingleLine(true);
      this.textView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label276;
      }
      i = 5;
      label98:
      ((TextView)localObject).setGravity(i | 0x10);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label281;
      }
      i = 5;
      label121:
      addView((View)localObject, LayoutHelper.createFrame(-2, -1.0F, i | 0x30, 17.0F, 0.0F, 17.0F, 0.0F));
      this.valueTextView = new TextView(paramContext);
      this.valueTextView.setTextSize(1, 16.0F);
      this.valueTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.valueTextView.setLines(1);
      this.valueTextView.setMaxLines(1);
      this.valueTextView.setSingleLine(true);
      this.valueTextView.setEllipsize(TextUtils.TruncateAt.END);
      paramContext = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label286;
      }
      i = 3;
      label225:
      paramContext.setGravity(i | 0x10);
      paramContext = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label291;
      }
    }
    label276:
    label281:
    label286:
    label291:
    for (int i = j;; i = 5)
    {
      addView(paramContext, LayoutHelper.createFrame(-2, -1.0F, i | 0x30, 17.0F, 0.0F, 17.0F, 0.0F));
      return;
      localObject = ". ";
      break;
      i = 3;
      break label98;
      i = 3;
      break label121;
      i = 5;
      break label225;
    }
  }
  
  protected void onDraw(Canvas paramCanvas) {}
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    setMeasuredDimension(View.MeasureSpec.getSize(paramInt1), AndroidUtilities.dp(40.0F));
    paramInt1 = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - AndroidUtilities.dp(34.0F);
    paramInt2 = paramInt1 / 2;
    this.valueTextView.measure(View.MeasureSpec.makeMeasureSpec(paramInt2, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
    paramInt2 = this.valueTextView.getMeasuredWidth();
    int i = AndroidUtilities.dp(8.0F);
    this.textView.measure(View.MeasureSpec.makeMeasureSpec(paramInt1 - paramInt2 - i, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
    this.dotLength = ((int)Math.ceil(this.textView.getPaint().measureText(this.dotstring)));
  }
  
  public void setTextAndValue(String paramString1, String paramString2, boolean paramBoolean)
  {
    this.textView.setText(paramString1);
    if (paramString2 != null)
    {
      this.valueTextView.setText(paramString2);
      this.valueTextView.setVisibility(0);
      if (!paramBoolean) {
        break label102;
      }
      setTag("windowBackgroundWhiteBlackText");
      this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.valueTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.valueTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    }
    for (;;)
    {
      requestLayout();
      return;
      this.valueTextView.setVisibility(4);
      break;
      label102:
      setTag("windowBackgroundWhiteGrayText2");
      this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      this.valueTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      this.textView.setTypeface(Typeface.DEFAULT);
      this.valueTextView.setTypeface(Typeface.DEFAULT);
    }
  }
  
  public void setTextColor(int paramInt)
  {
    this.textView.setTextColor(paramInt);
  }
  
  public void setTextValueColor(int paramInt)
  {
    this.valueTextView.setTextColor(paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/TextPriceCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */