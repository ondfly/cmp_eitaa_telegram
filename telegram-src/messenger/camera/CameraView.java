package org.telegram.messenger.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Display;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.AndroidUtilities;

@SuppressLint({"NewApi"})
public class CameraView
  extends FrameLayout
  implements TextureView.SurfaceTextureListener
{
  private CameraSession cameraSession;
  private boolean circleShape = false;
  private int clipLeft;
  private int clipTop;
  private int cx;
  private int cy;
  private CameraViewDelegate delegate;
  private int focusAreaSize;
  private float focusProgress = 1.0F;
  private boolean initialFrontface;
  private boolean initied;
  private float innerAlpha;
  private Paint innerPaint = new Paint(1);
  private DecelerateInterpolator interpolator = new DecelerateInterpolator();
  private boolean isFrontface;
  private long lastDrawTime;
  private Matrix matrix = new Matrix();
  private boolean mirror;
  private float outerAlpha;
  private Paint outerPaint = new Paint(1);
  private Size previewSize;
  private TextureView textureView;
  private Matrix txform = new Matrix();
  
  public CameraView(Context paramContext, boolean paramBoolean)
  {
    super(paramContext, null);
    this.isFrontface = paramBoolean;
    this.initialFrontface = paramBoolean;
    this.textureView = new TextureView(paramContext);
    this.textureView.setSurfaceTextureListener(this);
    addView(this.textureView);
    this.focusAreaSize = AndroidUtilities.dp(96.0F);
    this.outerPaint.setColor(-1);
    this.outerPaint.setStyle(Paint.Style.STROKE);
    this.outerPaint.setStrokeWidth(AndroidUtilities.dp(2.0F));
    this.innerPaint.setColor(Integer.MAX_VALUE);
  }
  
  private void adjustAspectRatio(int paramInt1, int paramInt2, int paramInt3)
  {
    this.txform.reset();
    int i = getWidth();
    int j = getHeight();
    float f2 = i / 2;
    float f3 = j / 2;
    float f1;
    if ((paramInt3 == 0) || (paramInt3 == 2))
    {
      f1 = Math.max((this.clipTop + j) / paramInt1, (this.clipLeft + i) / paramInt2);
      float f5 = paramInt1;
      float f4 = paramInt2 * f1 / i;
      f1 = f5 * f1 / j;
      this.txform.postScale(f4, f1, f2, f3);
      if ((1 != paramInt3) && (3 != paramInt3)) {
        break label309;
      }
      this.txform.postRotate((paramInt3 - 2) * 90, f2, f3);
    }
    for (;;)
    {
      if (this.mirror) {
        this.txform.postScale(-1.0F, 1.0F, f2, f3);
      }
      if ((this.clipTop != 0) || (this.clipLeft != 0)) {
        this.txform.postTranslate(-this.clipLeft / 2, -this.clipTop / 2);
      }
      this.textureView.setTransform(this.txform);
      Matrix localMatrix = new Matrix();
      localMatrix.postRotate(this.cameraSession.getDisplayOrientation());
      localMatrix.postScale(i / 2000.0F, j / 2000.0F);
      localMatrix.postTranslate(i / 2.0F, j / 2.0F);
      localMatrix.invert(this.matrix);
      return;
      f1 = Math.max((this.clipTop + j) / paramInt2, (this.clipLeft + i) / paramInt1);
      break;
      label309:
      if (2 == paramInt3) {
        this.txform.postRotate(180.0F, f2, f3);
      }
    }
  }
  
  private Rect calculateTapArea(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    int i = Float.valueOf(this.focusAreaSize * paramFloat3).intValue();
    int j = clamp((int)paramFloat1 - i / 2, 0, getWidth() - i);
    int k = clamp((int)paramFloat2 - i / 2, 0, getHeight() - i);
    RectF localRectF = new RectF(j, k, j + i, k + i);
    this.matrix.mapRect(localRectF);
    return new Rect(Math.round(localRectF.left), Math.round(localRectF.top), Math.round(localRectF.right), Math.round(localRectF.bottom));
  }
  
  private void checkPreviewMatrix()
  {
    if (this.previewSize == null) {
      return;
    }
    adjustAspectRatio(this.previewSize.getWidth(), this.previewSize.getHeight(), ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRotation());
  }
  
  private int clamp(int paramInt1, int paramInt2, int paramInt3)
  {
    if (paramInt1 > paramInt3) {
      return paramInt3;
    }
    if (paramInt1 < paramInt2) {
      return paramInt2;
    }
    return paramInt1;
  }
  
  private void initCamera(boolean paramBoolean)
  {
    Object localObject1 = null;
    Object localObject3 = CameraController.getInstance().getCameras();
    if (localObject3 == null) {}
    int i;
    label19:
    Object localObject2;
    do
    {
      return;
      i = 0;
      localObject2 = localObject1;
      if (i < ((ArrayList)localObject3).size())
      {
        localObject2 = (CameraInfo)((ArrayList)localObject3).get(i);
        if (((!this.isFrontface) || (((CameraInfo)localObject2).frontCamera == 0)) && ((this.isFrontface) || (((CameraInfo)localObject2).frontCamera != 0))) {
          break;
        }
      }
    } while (localObject2 == null);
    float f = Math.max(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) / Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
    int j;
    if (this.initialFrontface)
    {
      localObject1 = new Size(16, 9);
      j = 480;
      i = 270;
      label141:
      if ((this.textureView.getWidth() > 0) && (this.textureView.getHeight() > 0))
      {
        int k = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
        int m = ((Size)localObject1).getHeight() * k / ((Size)localObject1).getWidth();
        this.previewSize = CameraController.chooseOptimalSize(((CameraInfo)localObject2).getPreviewSizes(), k, m, (Size)localObject1);
      }
      localObject3 = CameraController.chooseOptimalSize(((CameraInfo)localObject2).getPictureSizes(), j, i, (Size)localObject1);
      localObject1 = localObject3;
      if (((Size)localObject3).getWidth() >= 1280)
      {
        localObject1 = localObject3;
        if (((Size)localObject3).getHeight() >= 1280) {
          if (Math.abs(f - 1.3333334F) >= 0.1F) {
            break label489;
          }
        }
      }
    }
    label489:
    for (localObject1 = new Size(3, 4);; localObject1 = new Size(9, 16))
    {
      Size localSize = CameraController.chooseOptimalSize(((CameraInfo)localObject2).getPictureSizes(), i, j, (Size)localObject1);
      if (localSize.getWidth() >= 1280)
      {
        localObject1 = localObject3;
        if (localSize.getHeight() >= 1280) {}
      }
      else
      {
        localObject1 = localSize;
      }
      localObject3 = this.textureView.getSurfaceTexture();
      if ((this.previewSize == null) || (localObject3 == null)) {
        break;
      }
      ((SurfaceTexture)localObject3).setDefaultBufferSize(this.previewSize.getWidth(), this.previewSize.getHeight());
      this.cameraSession = new CameraSession((CameraInfo)localObject2, this.previewSize, (Size)localObject1, 256);
      CameraController.getInstance().open(this.cameraSession, (SurfaceTexture)localObject3, new Runnable()new Runnable
      {
        public void run()
        {
          if (CameraView.this.cameraSession != null) {
            CameraView.this.cameraSession.setInitied();
          }
          CameraView.this.checkPreviewMatrix();
        }
      }, new Runnable()
      {
        public void run()
        {
          if (CameraView.this.delegate != null) {
            CameraView.this.delegate.onCameraCreated(CameraView.this.cameraSession.cameraInfo.camera);
          }
        }
      });
      return;
      i += 1;
      break label19;
      if (Math.abs(f - 1.3333334F) < 0.1F)
      {
        localObject1 = new Size(4, 3);
        j = 1280;
        i = 960;
        break label141;
      }
      localObject1 = new Size(16, 9);
      j = 1280;
      i = 720;
      break label141;
    }
  }
  
  public void destroy(boolean paramBoolean, Runnable paramRunnable)
  {
    CameraController localCameraController;
    CameraSession localCameraSession;
    if (this.cameraSession != null)
    {
      this.cameraSession.destroy();
      localCameraController = CameraController.getInstance();
      localCameraSession = this.cameraSession;
      if (paramBoolean) {
        break label48;
      }
    }
    label48:
    for (CountDownLatch localCountDownLatch = new CountDownLatch(1);; localCountDownLatch = null)
    {
      localCameraController.close(localCameraSession, localCountDownLatch, paramRunnable);
      return;
    }
  }
  
  protected boolean drawChild(Canvas paramCanvas, View paramView, long paramLong)
  {
    boolean bool = super.drawChild(paramCanvas, paramView, paramLong);
    if ((this.focusProgress != 1.0F) || (this.innerAlpha != 0.0F) || (this.outerAlpha != 0.0F))
    {
      int i = AndroidUtilities.dp(30.0F);
      long l2 = System.currentTimeMillis();
      long l1 = l2 - this.lastDrawTime;
      if (l1 >= 0L)
      {
        paramLong = l1;
        if (l1 <= 17L) {}
      }
      else
      {
        paramLong = 17L;
      }
      this.lastDrawTime = l2;
      this.outerPaint.setAlpha((int)(this.interpolator.getInterpolation(this.outerAlpha) * 255.0F));
      this.innerPaint.setAlpha((int)(this.interpolator.getInterpolation(this.innerAlpha) * 127.0F));
      float f = this.interpolator.getInterpolation(this.focusProgress);
      paramCanvas.drawCircle(this.cx, this.cy, i + i * (1.0F - f), this.outerPaint);
      paramCanvas.drawCircle(this.cx, this.cy, i * f, this.innerPaint);
      if (this.focusProgress >= 1.0F) {
        break label245;
      }
      this.focusProgress += (float)paramLong / 200.0F;
      if (this.focusProgress > 1.0F) {
        this.focusProgress = 1.0F;
      }
      invalidate();
    }
    label245:
    do
    {
      return bool;
      if (this.innerAlpha != 0.0F)
      {
        this.innerAlpha -= (float)paramLong / 150.0F;
        if (this.innerAlpha < 0.0F) {
          this.innerAlpha = 0.0F;
        }
        invalidate();
        return bool;
      }
    } while (this.outerAlpha == 0.0F);
    this.outerAlpha -= (float)paramLong / 150.0F;
    if (this.outerAlpha < 0.0F) {
      this.outerAlpha = 0.0F;
    }
    invalidate();
    return bool;
  }
  
  public void focusToPoint(int paramInt1, int paramInt2)
  {
    Rect localRect1 = calculateTapArea(paramInt1, paramInt2, 1.0F);
    Rect localRect2 = calculateTapArea(paramInt1, paramInt2, 1.5F);
    if (this.cameraSession != null) {
      this.cameraSession.focusToRect(localRect1, localRect2);
    }
    this.focusProgress = 0.0F;
    this.innerAlpha = 1.0F;
    this.outerAlpha = 1.0F;
    this.cx = paramInt1;
    this.cy = paramInt2;
    this.lastDrawTime = System.currentTimeMillis();
    invalidate();
  }
  
  public CameraSession getCameraSession()
  {
    return this.cameraSession;
  }
  
  public Size getPreviewSize()
  {
    return this.previewSize;
  }
  
  public boolean hasFrontFaceCamera()
  {
    ArrayList localArrayList = CameraController.getInstance().getCameras();
    int i = 0;
    while (i < localArrayList.size())
    {
      if (((CameraInfo)localArrayList.get(i)).frontCamera != 0) {
        return true;
      }
      i += 1;
    }
    return false;
  }
  
  public boolean isFrontface()
  {
    return this.isFrontface;
  }
  
  public boolean isInitied()
  {
    return this.initied;
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
    checkPreviewMatrix();
  }
  
  public void onSurfaceTextureAvailable(SurfaceTexture paramSurfaceTexture, int paramInt1, int paramInt2)
  {
    initCamera(this.isFrontface);
  }
  
  public boolean onSurfaceTextureDestroyed(SurfaceTexture paramSurfaceTexture)
  {
    if (this.cameraSession != null) {
      CameraController.getInstance().close(this.cameraSession, null, null);
    }
    return false;
  }
  
  public void onSurfaceTextureSizeChanged(SurfaceTexture paramSurfaceTexture, int paramInt1, int paramInt2)
  {
    checkPreviewMatrix();
  }
  
  public void onSurfaceTextureUpdated(SurfaceTexture paramSurfaceTexture)
  {
    if ((!this.initied) && (this.cameraSession != null) && (this.cameraSession.isInitied()))
    {
      if (this.delegate != null) {
        this.delegate.onCameraInit();
      }
      this.initied = true;
    }
  }
  
  public void setClipLeft(int paramInt)
  {
    this.clipLeft = paramInt;
  }
  
  public void setClipTop(int paramInt)
  {
    this.clipTop = paramInt;
  }
  
  public void setDelegate(CameraViewDelegate paramCameraViewDelegate)
  {
    this.delegate = paramCameraViewDelegate;
  }
  
  public void setMirror(boolean paramBoolean)
  {
    this.mirror = paramBoolean;
  }
  
  public void switchCamera()
  {
    boolean bool = false;
    if (this.cameraSession != null)
    {
      CameraController.getInstance().close(this.cameraSession, null, null);
      this.cameraSession = null;
    }
    this.initied = false;
    if (!this.isFrontface) {
      bool = true;
    }
    this.isFrontface = bool;
    initCamera(this.isFrontface);
  }
  
  public static abstract interface CameraViewDelegate
  {
    public abstract void onCameraCreated(Camera paramCamera);
    
    public abstract void onCameraInit();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/camera/CameraView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */