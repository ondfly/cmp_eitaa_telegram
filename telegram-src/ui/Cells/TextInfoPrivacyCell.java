package org.telegram.ui.Cells;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class TextInfoPrivacyCell
  extends FrameLayout
{
  private TextView textView;
  
  public TextInfoPrivacyCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextSize(1, 14.0F);
    paramContext = this.textView;
    if (LocaleController.isRTL)
    {
      i = 5;
      paramContext.setGravity(i);
      this.textView.setPadding(0, AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(17.0F));
      this.textView.setMovementMethod(LinkMovementMethod.getInstance());
      paramContext = this.textView;
      if (!LocaleController.isRTL) {
        break label117;
      }
    }
    label117:
    for (int i = j;; i = 3)
    {
      addView(paramContext, LayoutHelper.createFrame(-2, -2.0F, i | 0x30, 17.0F, 0.0F, 17.0F, 0.0F));
      return;
      i = 3;
      break;
    }
  }
  
  public TextView getTextView()
  {
    return this.textView;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText4"));
    this.textView.setLinkTextColor(Theme.getColor("windowBackgroundWhiteLinkText"));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(0, 0));
  }
  
  public void setEnabled(boolean paramBoolean, ArrayList<Animator> paramArrayList)
  {
    float f = 1.0F;
    if (paramArrayList != null)
    {
      TextView localTextView = this.textView;
      if (paramBoolean) {}
      for (;;)
      {
        paramArrayList.add(ObjectAnimator.ofFloat(localTextView, "alpha", new float[] { f }));
        return;
        f = 0.5F;
      }
    }
    paramArrayList = this.textView;
    if (paramBoolean) {}
    for (;;)
    {
      paramArrayList.setAlpha(f);
      return;
      f = 0.5F;
    }
  }
  
  public void setText(CharSequence paramCharSequence)
  {
    if (paramCharSequence == null) {
      this.textView.setPadding(0, AndroidUtilities.dp(2.0F), 0, 0);
    }
    for (;;)
    {
      this.textView.setText(paramCharSequence);
      return;
      this.textView.setPadding(0, AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(17.0F));
    }
  }
  
  public void setTextColor(int paramInt)
  {
    this.textView.setTextColor(paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/TextInfoPrivacyCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */