package org.telegram.ui.Components.Crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class CropRotationWheel
  extends FrameLayout
{
  private static final int DELTA_ANGLE = 5;
  private static final int MAX_ANGLE = 45;
  private ImageView aspectRatioButton;
  private Paint bluePaint;
  private TextView degreesLabel;
  private float prevX;
  protected float rotation;
  private ImageView rotation90Button;
  private RotationWheelListener rotationListener;
  private RectF tempRect = new RectF(0.0F, 0.0F, 0.0F, 0.0F);
  private Paint whitePaint = new Paint();
  
  public CropRotationWheel(Context paramContext)
  {
    super(paramContext);
    this.whitePaint.setStyle(Paint.Style.FILL);
    this.whitePaint.setColor(-1);
    this.whitePaint.setAlpha(255);
    this.whitePaint.setAntiAlias(true);
    this.bluePaint = new Paint();
    this.bluePaint.setStyle(Paint.Style.FILL);
    this.bluePaint.setColor(-11420173);
    this.bluePaint.setAlpha(255);
    this.bluePaint.setAntiAlias(true);
    this.aspectRatioButton = new ImageView(paramContext);
    this.aspectRatioButton.setImageResource(2131165680);
    this.aspectRatioButton.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.aspectRatioButton.setScaleType(ImageView.ScaleType.CENTER);
    this.aspectRatioButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (CropRotationWheel.this.rotationListener != null) {
          CropRotationWheel.this.rotationListener.aspectRatioPressed();
        }
      }
    });
    addView(this.aspectRatioButton, LayoutHelper.createFrame(70, 64, 19));
    this.rotation90Button = new ImageView(paramContext);
    this.rotation90Button.setImageResource(2131165682);
    this.rotation90Button.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.rotation90Button.setScaleType(ImageView.ScaleType.CENTER);
    this.rotation90Button.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (CropRotationWheel.this.rotationListener != null) {
          CropRotationWheel.this.rotationListener.rotate90Pressed();
        }
      }
    });
    addView(this.rotation90Button, LayoutHelper.createFrame(70, 64, 21));
    this.degreesLabel = new TextView(paramContext);
    this.degreesLabel.setTextColor(-1);
    addView(this.degreesLabel, LayoutHelper.createFrame(-2, -2, 49));
    setWillNotDraw(false);
    setRotation(0.0F, false);
  }
  
  protected void drawLine(Canvas paramCanvas, int paramInt1, float paramFloat, int paramInt2, int paramInt3, boolean paramBoolean, Paint paramPaint)
  {
    int j = (int)(paramInt2 / 2.0F - AndroidUtilities.dp(70.0F));
    float f = paramInt1 * 5;
    paramInt1 = (int)(j * Math.cos(Math.toRadians(90.0F - (f + paramFloat))));
    int i = paramInt2 / 2 + paramInt1;
    paramFloat = Math.abs(paramInt1) / j;
    paramInt1 = Math.min(255, Math.max(0, (int)((1.0F - paramFloat * paramFloat) * 255.0F)));
    if (paramBoolean) {
      paramPaint = this.bluePaint;
    }
    paramPaint.setAlpha(paramInt1);
    if (paramBoolean)
    {
      paramInt1 = 4;
      if (!paramBoolean) {
        break label157;
      }
    }
    label157:
    for (paramInt2 = AndroidUtilities.dp(16.0F);; paramInt2 = AndroidUtilities.dp(12.0F))
    {
      paramCanvas.drawRect(i - paramInt1 / 2, (paramInt3 - paramInt2) / 2, paramInt1 / 2 + i, (paramInt3 + paramInt2) / 2, paramPaint);
      return;
      paramInt1 = 2;
      break;
    }
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    super.onDraw(paramCanvas);
    int j = getWidth();
    int k = getHeight();
    float f1 = -this.rotation * 2.0F;
    float f2 = f1 % 5.0F;
    int m = (int)Math.floor(f1 / 5.0F);
    int i = 0;
    if (i < 16)
    {
      Paint localPaint2 = this.whitePaint;
      Paint localPaint1;
      if (i >= m)
      {
        localPaint1 = localPaint2;
        if (i == 0)
        {
          localPaint1 = localPaint2;
          if (f2 >= 0.0F) {}
        }
      }
      else
      {
        localPaint1 = this.bluePaint;
      }
      label110:
      int n;
      if ((i == m) || ((i == 0) && (m == -1)))
      {
        bool = true;
        drawLine(paramCanvas, i, f2, j, k, bool, localPaint1);
        if (i != 0)
        {
          n = -i;
          if (n <= m) {
            break label192;
          }
          localPaint1 = this.bluePaint;
          label149:
          if (n != m + 1) {
            break label201;
          }
        }
      }
      label192:
      label201:
      for (boolean bool = true;; bool = false)
      {
        drawLine(paramCanvas, n, f2, j, k, bool, localPaint1);
        i += 1;
        break;
        bool = false;
        break label110;
        localPaint1 = this.whitePaint;
        break label149;
      }
    }
    this.bluePaint.setAlpha(255);
    this.tempRect.left = ((j - AndroidUtilities.dp(2.5F)) / 2);
    this.tempRect.top = ((k - AndroidUtilities.dp(22.0F)) / 2);
    this.tempRect.right = ((AndroidUtilities.dp(2.5F) + j) / 2);
    this.tempRect.bottom = ((AndroidUtilities.dp(22.0F) + k) / 2);
    paramCanvas.drawRoundRect(this.tempRect, AndroidUtilities.dp(2.0F), AndroidUtilities.dp(2.0F), this.bluePaint);
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(Math.min(View.MeasureSpec.getSize(paramInt1), AndroidUtilities.dp(400.0F)), 1073741824), paramInt2);
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = paramMotionEvent.getActionMasked();
    float f3 = paramMotionEvent.getX();
    if (i == 0)
    {
      this.prevX = f3;
      if (this.rotationListener != null) {
        this.rotationListener.onStart();
      }
    }
    float f2;
    do
    {
      do
      {
        do
        {
          return true;
          if ((i != 1) && (i != 3)) {
            break;
          }
        } while (this.rotationListener == null);
        this.rotationListener.onEnd(this.rotation);
        return true;
      } while (i != 2);
      f1 = this.prevX;
      f2 = Math.max(-45.0F, Math.min(45.0F, this.rotation + (float)((f1 - f3) / AndroidUtilities.density / 3.141592653589793D / 1.649999976158142D)));
    } while (Math.abs(f2 - this.rotation) <= 0.001D);
    float f1 = f2;
    if (Math.abs(f2) < 0.05D) {
      f1 = 0.0F;
    }
    setRotation(f1, false);
    if (this.rotationListener != null) {
      this.rotationListener.onChange(this.rotation);
    }
    this.prevX = f3;
    return true;
  }
  
  public void reset()
  {
    setRotation(0.0F, false);
  }
  
  public void setAspectLock(boolean paramBoolean)
  {
    ImageView localImageView = this.aspectRatioButton;
    if (paramBoolean) {}
    for (PorterDuffColorFilter localPorterDuffColorFilter = new PorterDuffColorFilter(-11420173, PorterDuff.Mode.MULTIPLY);; localPorterDuffColorFilter = null)
    {
      localImageView.setColorFilter(localPorterDuffColorFilter);
      return;
    }
  }
  
  public void setFreeform(boolean paramBoolean)
  {
    ImageView localImageView = this.aspectRatioButton;
    if (paramBoolean) {}
    for (int i = 0;; i = 8)
    {
      localImageView.setVisibility(i);
      return;
    }
  }
  
  public void setListener(RotationWheelListener paramRotationWheelListener)
  {
    this.rotationListener = paramRotationWheelListener;
  }
  
  public void setRotation(float paramFloat, boolean paramBoolean)
  {
    this.rotation = paramFloat;
    float f = this.rotation;
    paramFloat = f;
    if (Math.abs(f) < 0.099D) {
      paramFloat = Math.abs(f);
    }
    this.degreesLabel.setText(String.format("%.1fº", new Object[] { Float.valueOf(paramFloat) }));
    invalidate();
  }
  
  public static abstract interface RotationWheelListener
  {
    public abstract void aspectRatioPressed();
    
    public abstract void onChange(float paramFloat);
    
    public abstract void onEnd(float paramFloat);
    
    public abstract void onStart();
    
    public abstract void rotate90Pressed();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/Crop/CropRotationWheel.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */