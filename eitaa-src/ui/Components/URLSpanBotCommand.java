package ir.eitaa.ui.Components;

import android.text.TextPaint;

public class URLSpanBotCommand
  extends URLSpanNoUnderline
{
  public static boolean enabled = true;
  
  public URLSpanBotCommand(String paramString)
  {
    super(paramString);
  }
  
  public void updateDrawState(TextPaint paramTextPaint)
  {
    super.updateDrawState(paramTextPaint);
    if (enabled) {}
    for (int i = -14255946;; i = -16777216)
    {
      paramTextPaint.setColor(i);
      paramTextPaint.setUnderlineText(false);
      return;
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/URLSpanBotCommand.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */