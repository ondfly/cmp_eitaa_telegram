package ir.eitaa.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import ir.eitaa.messenger.AndroidUtilities;

public class SizeNotifierFrameLayout
  extends FrameLayout
{
  private Drawable backgroundDrawable;
  private int bottomClip;
  private SizeNotifierFrameLayoutDelegate delegate;
  private int keyboardHeight;
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
      float f1 = getMeasuredWidth() / this.backgroundDrawable.getIntrinsicWidth();
      float f2 = (getMeasuredHeight() + this.keyboardHeight) / this.backgroundDrawable.getIntrinsicHeight();
      if (f1 < f2) {
        f1 = f2;
      }
      for (;;)
      {
        int i = (int)Math.ceil(this.backgroundDrawable.getIntrinsicWidth() * f1);
        int j = (int)Math.ceil(this.backgroundDrawable.getIntrinsicHeight() * f1);
        int k = (getMeasuredWidth() - i) / 2;
        int m = (getMeasuredHeight() - j + this.keyboardHeight) / 2;
        if (this.bottomClip != 0)
        {
          paramCanvas.save();
          paramCanvas.clipRect(0, 0, i, getMeasuredHeight() - this.bottomClip);
        }
        this.backgroundDrawable.setBounds(k, m, k + i, m + j);
        this.backgroundDrawable.draw(paramCanvas);
        if (this.bottomClip == 0) {
          break;
        }
        paramCanvas.restore();
        return;
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
  }
  
  public void setBottomClip(int paramInt)
  {
    this.bottomClip = paramInt;
  }
  
  public void setDelegate(SizeNotifierFrameLayoutDelegate paramSizeNotifierFrameLayoutDelegate)
  {
    this.delegate = paramSizeNotifierFrameLayoutDelegate;
  }
  
  public static abstract interface SizeNotifierFrameLayoutDelegate
  {
    public abstract void onSizeChanged(int paramInt, boolean paramBoolean);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/SizeNotifierFrameLayout.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */