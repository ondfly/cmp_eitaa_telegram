package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.CharacterStyle;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.MeasureSpec;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;

public class EditTextCaption
  extends EditTextBoldCursor
{
  private String caption;
  private StaticLayout captionLayout;
  private boolean copyPasteShowed;
  private int hintColor;
  private int triesCount = 0;
  private int userNameLength;
  private int xOffset;
  private int yOffset;
  
  public EditTextCaption(Context paramContext)
  {
    super(paramContext);
  }
  
  private void applyTextStyleToSelection(TypefaceSpan paramTypefaceSpan)
  {
    int j = getSelectionStart();
    int k = getSelectionEnd();
    Editable localEditable = getText();
    CharacterStyle[] arrayOfCharacterStyle = (CharacterStyle[])localEditable.getSpans(j, k, CharacterStyle.class);
    if ((arrayOfCharacterStyle != null) && (arrayOfCharacterStyle.length > 0))
    {
      int i = 0;
      while (i < arrayOfCharacterStyle.length)
      {
        CharacterStyle localCharacterStyle = arrayOfCharacterStyle[i];
        int m = localEditable.getSpanStart(localCharacterStyle);
        int n = localEditable.getSpanEnd(localCharacterStyle);
        localEditable.removeSpan(localCharacterStyle);
        if (m < j) {
          localEditable.setSpan(localCharacterStyle, m, j, 33);
        }
        if (n > k) {
          localEditable.setSpan(localCharacterStyle, k, n, 33);
        }
        i += 1;
      }
    }
    if (paramTypefaceSpan != null) {
      localEditable.setSpan(paramTypefaceSpan, j, k, 33);
    }
  }
  
  private void makeSelectedBold()
  {
    applyTextStyleToSelection(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")));
  }
  
  private void makeSelectedItalic()
  {
    applyTextStyleToSelection(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/ritalic.ttf")));
  }
  
  private void makeSelectedRegular()
  {
    applyTextStyleToSelection(null);
  }
  
  private ActionMode.Callback overrideCallback(final ActionMode.Callback paramCallback)
  {
    new ActionMode.Callback()
    {
      public boolean onActionItemClicked(ActionMode paramAnonymousActionMode, MenuItem paramAnonymousMenuItem)
      {
        if (paramAnonymousMenuItem.getItemId() == 2131230804)
        {
          EditTextCaption.this.makeSelectedRegular();
          paramAnonymousActionMode.finish();
          return true;
        }
        if (paramAnonymousMenuItem.getItemId() == 2131230801)
        {
          EditTextCaption.this.makeSelectedBold();
          paramAnonymousActionMode.finish();
          return true;
        }
        if (paramAnonymousMenuItem.getItemId() == 2131230803)
        {
          EditTextCaption.this.makeSelectedItalic();
          paramAnonymousActionMode.finish();
          return true;
        }
        try
        {
          boolean bool = paramCallback.onActionItemClicked(paramAnonymousActionMode, paramAnonymousMenuItem);
          return bool;
        }
        catch (Exception paramAnonymousActionMode) {}
        return true;
      }
      
      public boolean onCreateActionMode(ActionMode paramAnonymousActionMode, Menu paramAnonymousMenu)
      {
        EditTextCaption.access$002(EditTextCaption.this, true);
        return paramCallback.onCreateActionMode(paramAnonymousActionMode, paramAnonymousMenu);
      }
      
      public void onDestroyActionMode(ActionMode paramAnonymousActionMode)
      {
        EditTextCaption.access$002(EditTextCaption.this, false);
        paramCallback.onDestroyActionMode(paramAnonymousActionMode);
      }
      
      public boolean onPrepareActionMode(ActionMode paramAnonymousActionMode, Menu paramAnonymousMenu)
      {
        return paramCallback.onPrepareActionMode(paramAnonymousActionMode, paramAnonymousMenu);
      }
    };
  }
  
  public String getCaption()
  {
    return this.caption;
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    super.onDraw(paramCanvas);
    try
    {
      if ((this.captionLayout != null) && (this.userNameLength == length()))
      {
        TextPaint localTextPaint = getPaint();
        int i = getPaint().getColor();
        localTextPaint.setColor(this.hintColor);
        paramCanvas.save();
        paramCanvas.translate(this.xOffset, this.yOffset);
        this.captionLayout.draw(paramCanvas);
        paramCanvas.restore();
        localTextPaint.setColor(i);
      }
      return;
    }
    catch (Exception paramCanvas)
    {
      FileLog.e(paramCanvas);
    }
  }
  
  @SuppressLint({"DrawAllocation"})
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    try
    {
      super.onMeasure(paramInt1, paramInt2);
      this.captionLayout = null;
      if ((this.caption != null) && (this.caption.length() > 0))
      {
        localObject = getText();
        if ((((CharSequence)localObject).length() > 1) && (((CharSequence)localObject).charAt(0) == '@'))
        {
          paramInt1 = TextUtils.indexOf((CharSequence)localObject, ' ');
          if (paramInt1 != -1)
          {
            TextPaint localTextPaint = getPaint();
            CharSequence localCharSequence = ((CharSequence)localObject).subSequence(0, paramInt1 + 1);
            paramInt1 = (int)Math.ceil(localTextPaint.measureText((CharSequence)localObject, 0, paramInt1 + 1));
            paramInt2 = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            this.userNameLength = localCharSequence.length();
            localObject = TextUtils.ellipsize(this.caption, localTextPaint, paramInt2 - paramInt1, TextUtils.TruncateAt.END);
            this.xOffset = paramInt1;
          }
        }
      }
    }
    catch (Exception localException1)
    {
      for (;;)
      {
        try
        {
          Object localObject;
          this.captionLayout = new StaticLayout((CharSequence)localObject, getPaint(), paramInt2 - paramInt1, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
          if (this.captionLayout.getLineCount() > 0) {
            this.xOffset = ((int)(this.xOffset + -this.captionLayout.getLineLeft(0)));
          }
          this.yOffset = ((getMeasuredHeight() - this.captionLayout.getLineBottom(0)) / 2 + AndroidUtilities.dp(0.5F));
          return;
        }
        catch (Exception localException2)
        {
          FileLog.e(localException2);
        }
        localException1 = localException1;
        setMeasuredDimension(View.MeasureSpec.getSize(paramInt1), AndroidUtilities.dp(51.0F));
        FileLog.e(localException1);
      }
    }
  }
  
  public void onWindowFocusChanged(boolean paramBoolean)
  {
    if ((Build.VERSION.SDK_INT < 23) && (!paramBoolean) && (this.copyPasteShowed)) {
      return;
    }
    super.onWindowFocusChanged(paramBoolean);
  }
  
  public void setCaption(String paramString)
  {
    if (((this.caption != null) && (this.caption.length() != 0)) || ((paramString == null) || (paramString.length() == 0) || ((this.caption != null) && (paramString != null) && (this.caption.equals(paramString))))) {
      return;
    }
    this.caption = paramString;
    if (this.caption != null) {
      this.caption = this.caption.replace('\n', ' ');
    }
    requestLayout();
  }
  
  public void setHintColor(int paramInt)
  {
    super.setHintColor(paramInt);
    this.hintColor = paramInt;
    invalidate();
  }
  
  public ActionMode startActionMode(ActionMode.Callback paramCallback)
  {
    return super.startActionMode(overrideCallback(paramCallback));
  }
  
  public ActionMode startActionMode(ActionMode.Callback paramCallback, int paramInt)
  {
    return super.startActionMode(overrideCallback(paramCallback), paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/EditTextCaption.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */