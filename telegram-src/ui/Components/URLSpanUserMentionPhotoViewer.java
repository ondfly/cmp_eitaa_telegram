package org.telegram.ui.Components;

import android.text.TextPaint;

public class URLSpanUserMentionPhotoViewer
  extends URLSpanUserMention
{
  public URLSpanUserMentionPhotoViewer(String paramString, boolean paramBoolean)
  {
    super(paramString, 2);
  }
  
  public void updateDrawState(TextPaint paramTextPaint)
  {
    super.updateDrawState(paramTextPaint);
    paramTextPaint.setColor(-1);
    paramTextPaint.setUnderlineText(false);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/URLSpanUserMentionPhotoViewer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */