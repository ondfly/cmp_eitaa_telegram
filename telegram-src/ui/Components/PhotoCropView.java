package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.widget.FrameLayout;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.Crop.CropRotationWheel;
import org.telegram.ui.Components.Crop.CropRotationWheel.RotationWheelListener;
import org.telegram.ui.Components.Crop.CropView;
import org.telegram.ui.Components.Crop.CropView.CropViewListener;

public class PhotoCropView
  extends FrameLayout
{
  private RectF animationEndValues;
  private Runnable animationRunnable;
  private RectF animationStartValues;
  private float bitmapGlobalScale = 1.0F;
  private float bitmapGlobalX = 0.0F;
  private float bitmapGlobalY = 0.0F;
  private int bitmapHeight = 1;
  private Bitmap bitmapToEdit;
  private int bitmapWidth = 1;
  private int bitmapX;
  private int bitmapY;
  private CropView cropView;
  private PhotoCropViewDelegate delegate;
  private int draggingState = 0;
  private boolean freeformCrop = true;
  private float oldX = 0.0F;
  private float oldY = 0.0F;
  private int orientation;
  private float rectSizeX = 600.0F;
  private float rectSizeY = 600.0F;
  private float rectX = -1.0F;
  private float rectY = -1.0F;
  private boolean showOnSetBitmap;
  private CropRotationWheel wheelView;
  
  public PhotoCropView(Context paramContext)
  {
    super(paramContext);
  }
  
  public void cancelAnimationRunnable()
  {
    if (this.animationRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.animationRunnable);
      this.animationRunnable = null;
      this.animationStartValues = null;
      this.animationEndValues = null;
    }
  }
  
  public Bitmap getBitmap()
  {
    if (this.cropView != null) {
      return this.cropView.getResult();
    }
    return null;
  }
  
  public float getBitmapX()
  {
    return this.bitmapX - AndroidUtilities.dp(14.0F);
  }
  
  public float getBitmapY()
  {
    if (Build.VERSION.SDK_INT >= 21) {}
    for (int i = AndroidUtilities.statusBarHeight;; i = 0)
    {
      float f = i;
      return this.bitmapY - AndroidUtilities.dp(14.0F) - f;
    }
  }
  
  public float getLimitHeight()
  {
    if (Build.VERSION.SDK_INT >= 21) {}
    for (int i = AndroidUtilities.statusBarHeight;; i = 0)
    {
      float f = i;
      return getHeight() - AndroidUtilities.dp(14.0F) - f - this.rectY - (int)Math.max(0.0D, Math.ceil((getHeight() - AndroidUtilities.dp(28.0F) - this.bitmapHeight * this.bitmapGlobalScale - f) / 2.0F)) - this.rectSizeY;
    }
  }
  
  public float getLimitWidth()
  {
    return getWidth() - AndroidUtilities.dp(14.0F) - this.rectX - (int)Math.max(0.0D, Math.ceil((getWidth() - AndroidUtilities.dp(28.0F) - this.bitmapWidth * this.bitmapGlobalScale) / 2.0F)) - this.rectSizeX;
  }
  
  public float getLimitX()
  {
    return this.rectX - Math.max(0.0F, (float)Math.ceil((getWidth() - this.bitmapWidth * this.bitmapGlobalScale) / 2.0F));
  }
  
  public float getLimitY()
  {
    if (Build.VERSION.SDK_INT >= 21) {}
    for (int i = AndroidUtilities.statusBarHeight;; i = 0)
    {
      float f = i;
      return this.rectY - Math.max(0.0F, (float)Math.ceil((getHeight() - this.bitmapHeight * this.bitmapGlobalScale + f) / 2.0F));
    }
  }
  
  public float getRectSizeX()
  {
    return this.cropView.getCropWidth();
  }
  
  public float getRectSizeY()
  {
    return this.cropView.getCropHeight();
  }
  
  public float getRectX()
  {
    return this.cropView.getCropLeft() - AndroidUtilities.dp(14.0F);
  }
  
  public float getRectY()
  {
    float f1 = this.cropView.getCropTop();
    float f2 = AndroidUtilities.dp(14.0F);
    if (Build.VERSION.SDK_INT >= 21) {}
    for (int i = AndroidUtilities.statusBarHeight;; i = 0) {
      return f1 - f2 - i;
    }
  }
  
  public boolean isReady()
  {
    return this.cropView.isReady();
  }
  
  public void moveToFill(boolean paramBoolean)
  {
    float f2 = this.bitmapWidth / this.rectSizeX;
    float f1 = this.bitmapHeight / this.rectSizeY;
    label56:
    float f4;
    float f5;
    if (f2 > f1)
    {
      f2 = f1;
      if ((f2 <= 1.0F) || (this.bitmapGlobalScale * f2 <= 3.0F)) {
        break label259;
      }
      f1 = 3.0F / this.bitmapGlobalScale;
      f4 = this.rectSizeX * f1;
      f5 = this.rectSizeY * f1;
      if (Build.VERSION.SDK_INT < 21) {
        break label290;
      }
    }
    label259:
    label290:
    for (int i = AndroidUtilities.statusBarHeight;; i = 0)
    {
      float f7 = i;
      f2 = (getWidth() - f4) / 2.0F;
      float f3 = (getHeight() - f5 + f7) / 2.0F;
      this.animationStartValues = new RectF(this.rectX, this.rectY, this.rectSizeX, this.rectSizeY);
      this.animationEndValues = new RectF(f2, f3, f4, f5);
      f4 = getWidth() / 2;
      f5 = this.bitmapGlobalX;
      float f6 = this.rectX;
      f7 = (getHeight() + f7) / 2.0F;
      float f8 = this.bitmapGlobalY;
      float f9 = this.rectY;
      this.delegate.needMoveImageTo(f4 * (f1 - 1.0F) + f2 + (f5 - f6) * f1, f7 * (f1 - 1.0F) + f3 + (f8 - f9) * f1, this.bitmapGlobalScale * f1, paramBoolean);
      return;
      break;
      f1 = f2;
      if (f2 >= 1.0F) {
        break label56;
      }
      f1 = f2;
      if (this.bitmapGlobalScale * f2 >= 1.0F) {
        break label56;
      }
      f1 = 1.0F / this.bitmapGlobalScale;
      break label56;
    }
  }
  
  public void onAppear()
  {
    if (this.cropView != null) {
      this.cropView.willShow();
    }
  }
  
  public void onAppeared()
  {
    if (this.cropView != null)
    {
      this.cropView.show();
      return;
    }
    this.showOnSetBitmap = true;
  }
  
  public void onDisappear()
  {
    this.cropView.hide();
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
    Bitmap localBitmap = this.delegate.getBitmap();
    if (localBitmap != null) {
      this.bitmapToEdit = localBitmap;
    }
    if (this.cropView != null) {
      this.cropView.updateLayout();
    }
  }
  
  public void reset()
  {
    this.wheelView.reset();
    this.cropView.reset();
  }
  
  public void setAnimationProgress(float paramFloat)
  {
    if (this.animationStartValues != null)
    {
      if (paramFloat != 1.0F) {
        break label72;
      }
      this.rectX = this.animationEndValues.left;
      this.rectY = this.animationEndValues.top;
      this.rectSizeX = this.animationEndValues.right;
      this.rectSizeY = this.animationEndValues.bottom;
      this.animationStartValues = null;
      this.animationEndValues = null;
    }
    for (;;)
    {
      invalidate();
      return;
      label72:
      this.rectX = (this.animationStartValues.left + (this.animationEndValues.left - this.animationStartValues.left) * paramFloat);
      this.rectY = (this.animationStartValues.top + (this.animationEndValues.top - this.animationStartValues.top) * paramFloat);
      this.rectSizeX = (this.animationStartValues.right + (this.animationEndValues.right - this.animationStartValues.right) * paramFloat);
      this.rectSizeY = (this.animationStartValues.bottom + (this.animationEndValues.bottom - this.animationStartValues.bottom) * paramFloat);
    }
  }
  
  public void setBitmap(Bitmap paramBitmap, int paramInt, boolean paramBoolean)
  {
    this.bitmapToEdit = paramBitmap;
    this.rectSizeX = 600.0F;
    this.rectSizeY = 600.0F;
    this.draggingState = 0;
    this.oldX = 0.0F;
    this.oldY = 0.0F;
    this.bitmapWidth = 1;
    this.bitmapHeight = 1;
    this.rectX = -1.0F;
    this.rectY = -1.0F;
    this.freeformCrop = paramBoolean;
    this.orientation = paramInt;
    requestLayout();
    if (this.cropView == null)
    {
      this.cropView = new CropView(getContext());
      this.cropView.setListener(new CropView.CropViewListener()
      {
        public void onAspectLock(boolean paramAnonymousBoolean)
        {
          PhotoCropView.this.wheelView.setAspectLock(paramAnonymousBoolean);
        }
        
        public void onChange(boolean paramAnonymousBoolean)
        {
          if (PhotoCropView.this.delegate != null) {
            PhotoCropView.this.delegate.onChange(paramAnonymousBoolean);
          }
        }
      });
      this.cropView.setBottomPadding(AndroidUtilities.dp(64.0F));
      addView(this.cropView);
      this.wheelView = new CropRotationWheel(getContext());
      this.wheelView.setListener(new CropRotationWheel.RotationWheelListener()
      {
        public void aspectRatioPressed()
        {
          PhotoCropView.this.cropView.showAspectRatioDialog();
        }
        
        public void onChange(float paramAnonymousFloat)
        {
          PhotoCropView.this.cropView.setRotation(paramAnonymousFloat);
          if (PhotoCropView.this.delegate != null) {
            PhotoCropView.this.delegate.onChange(false);
          }
        }
        
        public void onEnd(float paramAnonymousFloat)
        {
          PhotoCropView.this.cropView.onRotationEnded();
        }
        
        public void onStart()
        {
          PhotoCropView.this.cropView.onRotationBegan();
        }
        
        public void rotate90Pressed()
        {
          PhotoCropView.this.wheelView.reset();
          PhotoCropView.this.cropView.rotate90Degrees();
        }
      });
      addView(this.wheelView, LayoutHelper.createFrame(-1, -2.0F, 81, 0.0F, 0.0F, 0.0F, 0.0F));
    }
    this.cropView.setVisibility(0);
    this.cropView.setBitmap(paramBitmap, paramInt, paramBoolean);
    if (this.showOnSetBitmap)
    {
      this.showOnSetBitmap = false;
      this.cropView.show();
    }
    this.wheelView.setFreeform(paramBoolean);
    this.wheelView.reset();
  }
  
  public void setBitmapParams(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    this.bitmapGlobalScale = paramFloat1;
    this.bitmapGlobalX = paramFloat2;
    this.bitmapGlobalY = paramFloat3;
  }
  
  public void setDelegate(PhotoCropViewDelegate paramPhotoCropViewDelegate)
  {
    this.delegate = paramPhotoCropViewDelegate;
  }
  
  public void setOrientation(int paramInt)
  {
    this.orientation = paramInt;
    this.rectX = -1.0F;
    this.rectY = -1.0F;
    this.rectSizeX = 600.0F;
    this.rectSizeY = 600.0F;
    this.delegate.needMoveImageTo(0.0F, 0.0F, 1.0F, false);
    requestLayout();
  }
  
  public void startAnimationRunnable()
  {
    if (this.animationRunnable != null) {
      return;
    }
    this.animationRunnable = new Runnable()
    {
      public void run()
      {
        if (PhotoCropView.this.animationRunnable == this)
        {
          PhotoCropView.access$302(PhotoCropView.this, null);
          PhotoCropView.this.moveToFill(true);
        }
      }
    };
    AndroidUtilities.runOnUIThread(this.animationRunnable, 1500L);
  }
  
  public static abstract interface PhotoCropViewDelegate
  {
    public abstract Bitmap getBitmap();
    
    public abstract void needMoveImageTo(float paramFloat1, float paramFloat2, float paramFloat3, boolean paramBoolean);
    
    public abstract void onChange(boolean paramBoolean);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/PhotoCropView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */