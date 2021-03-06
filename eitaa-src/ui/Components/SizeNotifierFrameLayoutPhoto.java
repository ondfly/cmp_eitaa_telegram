package ir.eitaa.ui.Components;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import ir.eitaa.messenger.AndroidUtilities;

public class SizeNotifierFrameLayoutPhoto
  extends FrameLayout
{
  private SizeNotifierFrameLayoutPhotoDelegate delegate;
  private int keyboardHeight;
  private Rect rect = new Rect();
  private WindowManager windowManager;
  private boolean withoutWindow;
  
  public SizeNotifierFrameLayoutPhoto(Context paramContext)
  {
    super(paramContext);
  }
  
  public int getKeyboardHeight()
  {
    View localView = getRootView();
    getWindowVisibleDisplayFrame(this.rect);
    int j;
    int i;
    if (this.withoutWindow)
    {
      j = localView.getHeight();
      if (this.rect.top != 0)
      {
        i = AndroidUtilities.statusBarHeight;
        i = j - i - AndroidUtilities.getViewInset(localView) - (this.rect.bottom - this.rect.top);
      }
    }
    do
    {
      return i;
      i = 0;
      break;
      i = localView.getHeight();
      j = AndroidUtilities.getViewInset(localView);
      int k = this.rect.top;
      j = AndroidUtilities.displaySize.y - k - (i - j);
      i = j;
    } while (j > AndroidUtilities.dp(10.0F));
    return 0;
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
          if (SizeNotifierFrameLayoutPhoto.this.delegate != null) {
            SizeNotifierFrameLayoutPhoto.this.delegate.onSizeChanged(SizeNotifierFrameLayoutPhoto.this.keyboardHeight, bool);
          }
        }
      });
      return;
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
    notifyHeightChanged();
  }
  
  public void setDelegate(SizeNotifierFrameLayoutPhotoDelegate paramSizeNotifierFrameLayoutPhotoDelegate)
  {
    this.delegate = paramSizeNotifierFrameLayoutPhotoDelegate;
  }
  
  public void setWithoutWindow(boolean paramBoolean)
  {
    this.withoutWindow = paramBoolean;
  }
  
  public static abstract interface SizeNotifierFrameLayoutPhotoDelegate
  {
    public abstract void onSizeChanged(int paramInt, boolean paramBoolean);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/SizeNotifierFrameLayoutPhoto.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */