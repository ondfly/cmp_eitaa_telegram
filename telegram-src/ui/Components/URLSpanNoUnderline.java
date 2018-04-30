package org.telegram.ui.Components;

import android.net.Uri;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import org.telegram.messenger.browser.Browser;

public class URLSpanNoUnderline
  extends URLSpan
{
  public URLSpanNoUnderline(String paramString)
  {
    super(paramString);
  }
  
  public void onClick(View paramView)
  {
    Object localObject = getURL();
    if (((String)localObject).startsWith("@"))
    {
      localObject = Uri.parse("https://t.me/" + ((String)localObject).substring(1));
      Browser.openUrl(paramView.getContext(), (Uri)localObject);
      return;
    }
    Browser.openUrl(paramView.getContext(), (String)localObject);
  }
  
  public void updateDrawState(TextPaint paramTextPaint)
  {
    super.updateDrawState(paramTextPaint);
    paramTextPaint.setUnderlineText(false);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/URLSpanNoUnderline.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */