package ir.eitaa.ui.Components;

import android.text.TextPaint;
import android.text.style.URLSpan;

public class URLSpanNoUnderline
  extends URLSpan
{
  public URLSpanNoUnderline(String paramString)
  {
    super(paramString);
  }
  
  public void updateDrawState(TextPaint paramTextPaint)
  {
    super.updateDrawState(paramTextPaint);
    paramTextPaint.setUnderlineText(false);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/URLSpanNoUnderline.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */