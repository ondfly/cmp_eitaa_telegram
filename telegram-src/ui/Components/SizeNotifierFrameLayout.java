package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.view.View;
import android.widget.FrameLayout;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;

public class SizeNotifierFrameLayout
  extends FrameLayout
{
  private Drawable backgroundDrawable;
  private int bottomClip;
  private SizeNotifierFrameLayoutDelegate delegate;
  private int keyboardHeight;
  private boolean occupyStatusBar = true;
  private Rect rect = new Rect();
  
  public SizeNotifierFrameLayout(Context paramContext)
  {
    super(paramContext);
    setWillNotDraw(false);
  }
  
  public Drawable getBackgroundImage()
  {
    return this.backgroundDrawable;
  }
  
  public int getKeyboardHeight()
  {
    View localView = getRootView();
    getWindowVisibleDisplayFrame(this.rect);
    int j = localView.getHeight();
    if (this.rect.top != 0) {}
    for (int i = AndroidUtilities.statusBarHeight;; i = 0) {
      return j - i - AndroidUtilities.getViewInset(localView) - (this.rect.bottom - this.rect.top);
    }
  }
  
  protected boolean isActionBarVisible()
  {
    return true;
  }
  
  public void notifyHeightChanged()
  {
    if (this.delegate != null)
    {
      this.keyboardHeight = getKeyboardHeight();
      if (AndroidUtilities.displaySize.x <= AndroidUtilities.displaySize.y) {
        break label47;
      }
    }
    label47:
    for (final boolean bool = true;; bool = false)
    {
      post(new Runnable()
      {
        public void run()
        {
          if (SizeNotifierFrameLayout.this.delegate != null) {
            SizeNotifierFrameLayout.this.delegate.onSizeChanged(SizeNotifierFrameLayout.this.keyboardHeight, bool);
          }
        }
      });
      return;
    }
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.backgroundDrawable != null)
    {
      if ((this.backgroundDrawable instanceof ColorDrawable))
      {
        if (this.bottomClip != 0)
        {
          paramCanvas.save();
          paramCanvas.clipRect(0, 0, getMeasuredWidth(), getMeasuredHeight() - this.bottomClip);
        }
        this.backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        this.backgroundDrawable.draw(paramCanvas);
        if (this.bottomClip != 0) {
          paramCanvas.restore();
        }
      }
      while (!(this.backgroundDrawable instanceof BitmapDrawable)) {
        return;
      }
      float f1;
      if (((BitmapDrawable)this.backgroundDrawable).getTileModeX() == Shader.TileMode.REPEAT)
      {
        paramCanvas.save();
        f1 = 2.0F / AndroidUtilities.density;
        paramCanvas.scale(f1, f1);
        this.backgroundDrawable.setBounds(0, 0, (int)Math.ceil(getMeasuredWidth() / f1), (int)Math.ceil(getMeasuredHeight() / f1));
        this.backgroundDrawable.draw(paramCanvas);
        paramCanvas.restore();
        return;
      }
      int i;
      int j;
      label207:
      int n;
      if (isActionBarVisible())
      {
        i = ActionBar.getCurrentActionBarHeight();
        if ((Build.VERSION.SDK_INT < 21) || (!this.occupyStatusBar)) {
          break label390;
        }
        j = AndroidUtilities.statusBarHeight;
        i += j;
        n = getMeasuredHeight() - i;
        f1 = getMeasuredWidth() / this.backgroundDrawable.getIntrinsicWidth();
        float f2 = (this.keyboardHeight + n) / this.backgroundDrawable.getIntrinsicHeight();
        if (f1 >= f2) {
          break label396;
        }
        f1 = f2;
      }
      label390:
      label396:
      for (;;)
      {
        j = (int)Math.ceil(this.backgroundDrawable.getIntrinsicWidth() * f1);
        int k = (int)Math.ceil(this.backgroundDrawable.getIntrinsicHeight() * f1);
        int m = (getMeasuredWidth() - j) / 2;
        n = (n - k + this.keyboardHeight) / 2 + i;
        paramCanvas.save();
        paramCanvas.clipRect(0, i, j, getMeasuredHeight() - this.bottomClip);
        this.backgroundDrawable.setBounds(m, n, m + j, n + k);
        this.backgroundDrawable.draw(paramCanvas);
        paramCanvas.restore();
        return;
        i = 0;
        break;
        j = 0;
        break label207;
      }
    }
    super.onDraw(paramCanvas);
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
    notifyHeightChanged();
  }
  
  public void setBackgroundImage(Drawable paramDrawable)
  {
    this.backgroundDrawable = paramDrawable;
    invalidate();
  }
  
  public void setBottomClip(int paramInt)
  {
    this.bottomClip = paramInt;
  }
  
  public void setDelegate(SizeNotifierFrameLayoutDelegate paramSizeNotifierFrameLayoutDelegate)
  {
    this.delegate = paramSizeNotifierFrameLayoutDelegate;
  }
  
  public void setOccupyStatusBar(boolean paramBoolean)
  {
    this.occupyStatusBar = paramBoolean;
  }
  
  public static abstract interface SizeNotifierFrameLayoutDelegate
  {
    public abstract void onSizeChanged(int paramInt, boolean paramBoolean);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/SizeNotifierFrameLayout.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */