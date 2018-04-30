package ir.eitaa.ui.Components;

import android.text.TextPaint;
import ir.eitaa.messenger.AndroidUtilities;

public class URLSpanNoUnderlineBold
  extends URLSpanNoUnderline
{
  public URLSpanNoUnderlineBold(String paramString)
  {
    super(paramString);
  }
  
  public void updateDrawState(TextPaint paramTextPaint)
  {
    super.updateDrawState(paramTextPaint);
    paramTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    paramTextPaint.setUnderlineText(false);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/URLSpanNoUnderlineBold.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */