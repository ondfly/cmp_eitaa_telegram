package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocaleController.LocaleInfo;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class LanguageCell
  extends FrameLayout
{
  private ImageView checkImage;
  private LocaleController.LocaleInfo currentLocale;
  private boolean isDialog;
  private boolean needDivider;
  private TextView textView;
  private TextView textView2;
  
  public LanguageCell(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    setWillNotDraw(false);
    this.isDialog = paramBoolean;
    this.textView = new TextView(paramContext);
    TextView localTextView = this.textView;
    Object localObject;
    label113:
    label138:
    float f1;
    int j;
    label157:
    label176:
    float f2;
    if (paramBoolean)
    {
      localObject = "dialogTextBlack";
      localTextView.setTextColor(Theme.getColor((String)localObject));
      this.textView.setTextSize(1, 16.0F);
      this.textView.setLines(1);
      this.textView.setMaxLines(1);
      this.textView.setSingleLine(true);
      this.textView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label480;
      }
      i = 5;
      ((TextView)localObject).setGravity(i | 0x30);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label486;
      }
      i = 5;
      if (!LocaleController.isRTL) {
        break label492;
      }
      f1 = 71.0F;
      if (!this.isDialog) {
        break label514;
      }
      j = 4;
      float f3 = j;
      if (!LocaleController.isRTL) {
        break label528;
      }
      if (!paramBoolean) {
        break label521;
      }
      j = 23;
      f2 = j;
      label181:
      addView((View)localObject, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, f1, f3, f2, 0.0F));
      this.textView2 = new TextView(paramContext);
      localTextView = this.textView2;
      if (!paramBoolean) {
        break label535;
      }
      localObject = "dialogTextGray3";
      label230:
      localTextView.setTextColor(Theme.getColor((String)localObject));
      this.textView2.setTextSize(1, 13.0F);
      this.textView2.setLines(1);
      this.textView2.setMaxLines(1);
      this.textView2.setSingleLine(true);
      this.textView2.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.textView2;
      if (!LocaleController.isRTL) {
        break label542;
      }
      i = 5;
      label299:
      ((TextView)localObject).setGravity(i | 0x30);
      localObject = this.textView2;
      if (!LocaleController.isRTL) {
        break label548;
      }
      i = 5;
      label324:
      if (!LocaleController.isRTL) {
        break label554;
      }
      f1 = 71.0F;
      if (!this.isDialog) {
        break label576;
      }
      j = 25;
      label344:
      f3 = j;
      if (!LocaleController.isRTL) {
        break label590;
      }
      if (!paramBoolean) {
        break label583;
      }
      j = 23;
      label363:
      f2 = j;
      label368:
      addView((View)localObject, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, f1, f3, f2, 0.0F));
      this.checkImage = new ImageView(paramContext);
      this.checkImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor("featuredStickers_addedIcon"), PorterDuff.Mode.MULTIPLY));
      this.checkImage.setImageResource(2131165652);
      paramContext = this.checkImage;
      if (!LocaleController.isRTL) {
        break label597;
      }
    }
    label480:
    label486:
    label492:
    label514:
    label521:
    label528:
    label535:
    label542:
    label548:
    label554:
    label576:
    label583:
    label590:
    label597:
    for (int i = k;; i = 5)
    {
      addView(paramContext, LayoutHelper.createFrame(19, 14.0F, i | 0x10, 23.0F, 0.0F, 23.0F, 0.0F));
      return;
      localObject = "windowBackgroundWhiteBlackText";
      break;
      i = 3;
      break label113;
      i = 3;
      break label138;
      if (paramBoolean) {}
      for (j = 23;; j = 16)
      {
        f1 = j;
        break;
      }
      j = 6;
      break label157;
      j = 16;
      break label176;
      f2 = 71.0F;
      break label181;
      localObject = "windowBackgroundWhiteGrayText3";
      break label230;
      i = 3;
      break label299;
      i = 3;
      break label324;
      if (paramBoolean) {}
      for (j = 23;; j = 16)
      {
        f1 = j;
        break;
      }
      j = 28;
      break label344;
      j = 16;
      break label363;
      f2 = 71.0F;
      break label368;
    }
  }
  
  public LocaleController.LocaleInfo getCurrentLocale()
  {
    return this.currentLocale;
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(getPaddingLeft(), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    float f;
    int i;
    if (this.isDialog)
    {
      f = 48.0F;
      i = AndroidUtilities.dp(f);
      if (!this.needDivider) {
        break label56;
      }
    }
    label56:
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(paramInt1 + i, 1073741824));
      return;
      f = 54.0F;
      break;
    }
  }
  
  public void setLanguage(LocaleController.LocaleInfo paramLocaleInfo, String paramString, boolean paramBoolean)
  {
    TextView localTextView = this.textView;
    if (paramString != null) {}
    for (;;)
    {
      localTextView.setText(paramString);
      this.textView2.setText(paramLocaleInfo.nameEnglish);
      this.currentLocale = paramLocaleInfo;
      this.needDivider = paramBoolean;
      return;
      paramString = paramLocaleInfo.name;
    }
  }
  
  public void setLanguageSelected(boolean paramBoolean)
  {
    ImageView localImageView = this.checkImage;
    if (paramBoolean) {}
    for (int i = 0;; i = 4)
    {
      localImageView.setVisibility(i);
      return;
    }
  }
  
  public void setValue(String paramString1, String paramString2)
  {
    this.textView.setText(paramString1);
    this.textView2.setText(paramString2);
    this.checkImage.setVisibility(4);
    this.currentLocale = null;
    this.needDivider = false;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/LanguageCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */