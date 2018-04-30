package org.telegram.ui.Components;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import org.telegram.messenger.AndroidUtilities;

public class TypefaceSpan
  extends MetricAffectingSpan
{
  private int color;
  private int textSize;
  private Typeface typeface;
  
  public TypefaceSpan(Typeface paramTypeface)
  {
    this.typeface = paramTypeface;
  }
  
  public TypefaceSpan(Typeface paramTypeface, int paramInt)
  {
    this.typeface = paramTypeface;
    this.textSize = paramInt;
  }
  
  public TypefaceSpan(Typeface paramTypeface, int paramInt1, int paramInt2)
  {
    this.typeface = paramTypeface;
    this.textSize = paramInt1;
    this.color = paramInt2;
  }
  
  public Typeface getTypeface()
  {
    return this.typeface;
  }
  
  public boolean isBold()
  {
    return this.typeface == AndroidUtilities.getTypeface("fonts/rmedium.ttf");
  }
  
  public boolean isItalic()
  {
    return this.typeface == AndroidUtilities.getTypeface("fonts/ritalic.ttf");
  }
  
  public void setColor(int paramInt)
  {
    this.color = paramInt;
  }
  
  public void updateDrawState(TextPaint paramTextPaint)
  {
    if (this.typeface != null) {
      paramTextPaint.setTypeface(this.typeface);
    }
    if (this.textSize != 0) {
      paramTextPaint.setTextSize(this.textSize);
    }
    if (this.color != 0) {
      paramTextPaint.setColor(this.color);
    }
    paramTextPaint.setFlags(paramTextPaint.getFlags() | 0x80);
  }
  
  public void updateMeasureState(TextPaint paramTextPaint)
  {
    if (this.typeface != null) {
      paramTextPaint.setTypeface(this.typeface);
    }
    if (this.textSize != 0) {
      paramTextPaint.setTextSize(this.textSize);
    }
    paramTextPaint.setFlags(paramTextPaint.getFlags() | 0x80);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/TypefaceSpan.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */