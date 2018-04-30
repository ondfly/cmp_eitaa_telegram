package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class TextDetailCell
  extends FrameLayout
{
  private ImageView imageView;
  private boolean multiline;
  private TextView textView;
  private TextView valueTextView;
  
  public TextDetailCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.textView.setTextSize(1, 16.0F);
    TextView localTextView = this.textView;
    int i;
    label79:
    float f1;
    if (LocaleController.isRTL)
    {
      i = 5;
      localTextView.setGravity(i);
      localTextView = this.textView;
      if (!LocaleController.isRTL) {
        break label355;
      }
      i = 5;
      if (!LocaleController.isRTL) {
        break label361;
      }
      f1 = 16.0F;
      label88:
      if (!LocaleController.isRTL) {
        break label367;
      }
      f2 = 71.0F;
      label97:
      addView(localTextView, LayoutHelper.createFrame(-2, -2.0F, i, f1, 10.0F, f2, 0.0F));
      this.valueTextView = new TextView(paramContext);
      this.valueTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      this.valueTextView.setTextSize(1, 13.0F);
      this.valueTextView.setLines(1);
      this.valueTextView.setMaxLines(1);
      this.valueTextView.setSingleLine(true);
      localTextView = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label373;
      }
      i = 5;
      label190:
      localTextView.setGravity(i);
      localTextView = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label379;
      }
      i = 5;
      label212:
      if (!LocaleController.isRTL) {
        break label385;
      }
      f1 = 16.0F;
      label221:
      if (!LocaleController.isRTL) {
        break label391;
      }
      f2 = 71.0F;
      label230:
      addView(localTextView, LayoutHelper.createFrame(-2, -2.0F, i, f1, 35.0F, f2, 0.0F));
      this.imageView = new ImageView(paramContext);
      this.imageView.setScaleType(ImageView.ScaleType.CENTER);
      this.imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("windowBackgroundWhiteGrayIcon"), PorterDuff.Mode.MULTIPLY));
      paramContext = this.imageView;
      if (!LocaleController.isRTL) {
        break label397;
      }
      i = j;
      label309:
      if (!LocaleController.isRTL) {
        break label403;
      }
      f1 = 0.0F;
      label317:
      if (!LocaleController.isRTL) {
        break label409;
      }
    }
    label355:
    label361:
    label367:
    label373:
    label379:
    label385:
    label391:
    label397:
    label403:
    label409:
    for (float f2 = 16.0F;; f2 = 0.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(-2, -2.0F, i | 0x30, f1, 11.0F, f2, 0.0F));
      return;
      i = 3;
      break;
      i = 3;
      break label79;
      f1 = 71.0F;
      break label88;
      f2 = 16.0F;
      break label97;
      i = 3;
      break label190;
      i = 3;
      break label212;
      f1 = 71.0F;
      break label221;
      f2 = 16.0F;
      break label230;
      i = 3;
      break label309;
      f1 = 16.0F;
      break label317;
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
    if (this.multiline)
    {
      paramInt1 = this.textView.getMeasuredHeight() + AndroidUtilities.dp(13.0F);
      this.valueTextView.layout(this.valueTextView.getLeft(), paramInt1, this.valueTextView.getRight(), this.valueTextView.getMeasuredHeight() + paramInt1);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    if (!this.multiline)
    {
      super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64.0F), 1073741824));
      return;
    }
    measureChildWithMargins(this.textView, paramInt1, 0, paramInt2, 0);
    measureChildWithMargins(this.valueTextView, paramInt1, 0, paramInt2, 0);
    measureChildWithMargins(this.imageView, paramInt1, 0, paramInt2, 0);
    setMeasuredDimension(View.MeasureSpec.getSize(paramInt1), Math.max(AndroidUtilities.dp(64.0F), this.textView.getMeasuredHeight() + this.valueTextView.getMeasuredHeight() + AndroidUtilities.dp(20.0F)));
  }
  
  public void setMultiline(boolean paramBoolean)
  {
    this.multiline = paramBoolean;
    if (this.multiline)
    {
      this.textView.setSingleLine(false);
      return;
    }
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
  }
  
  public void setTextAndValue(String paramString1, String paramString2)
  {
    this.textView.setText(paramString1);
    this.valueTextView.setText(paramString2);
    this.imageView.setVisibility(4);
  }
  
  public void setTextAndValueAndIcon(String paramString1, String paramString2, int paramInt1, int paramInt2)
  {
    this.textView.setText(paramString1);
    this.valueTextView.setText(paramString2);
    this.imageView.setVisibility(0);
    this.imageView.setImageResource(paramInt1);
    float f1;
    if (paramInt2 == 0)
    {
      paramString1 = this.imageView;
      if (LocaleController.isRTL)
      {
        paramInt1 = 5;
        if (!LocaleController.isRTL) {
          break label96;
        }
        f1 = 0.0F;
        label59:
        if (!LocaleController.isRTL) {
          break label103;
        }
      }
      label96:
      label103:
      for (f2 = 16.0F;; f2 = 0.0F)
      {
        paramString1.setLayoutParams(LayoutHelper.createFrame(-2, -2.0F, paramInt1 | 0x10, f1, 0.0F, f2, 0.0F));
        return;
        paramInt1 = 3;
        break;
        f1 = 16.0F;
        break label59;
      }
    }
    paramString1 = this.imageView;
    label131:
    float f3;
    if (LocaleController.isRTL)
    {
      paramInt1 = 5;
      if (!LocaleController.isRTL) {
        break label174;
      }
      f1 = 0.0F;
      f3 = paramInt2;
      if (!LocaleController.isRTL) {
        break label181;
      }
    }
    label174:
    label181:
    for (float f2 = 16.0F;; f2 = 0.0F)
    {
      paramString1.setLayoutParams(LayoutHelper.createFrame(-2, -2.0F, paramInt1 | 0x30, f1, f3, f2, 0.0F));
      return;
      paramInt1 = 3;
      break;
      f1 = 16.0F;
      break label131;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/TextDetailCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */