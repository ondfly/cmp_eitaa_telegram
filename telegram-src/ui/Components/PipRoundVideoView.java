package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.Keep;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.Collection;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.exoplayer2.ui.AspectRatioFrameLayout;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;

public class PipRoundVideoView
  implements NotificationCenter.NotificationCenterDelegate
{
  @SuppressLint({"StaticFieldLeak"})
  private static PipRoundVideoView instance;
  private AspectRatioFrameLayout aspectRatioFrameLayout;
  private Bitmap bitmap;
  private int currentAccount;
  private DecelerateInterpolator decelerateInterpolator;
  private AnimatorSet hideShowAnimation;
  private ImageView imageView;
  private Runnable onCloseRunnable;
  private Activity parentActivity;
  private SharedPreferences preferences;
  private RectF rect = new RectF();
  private TextureView textureView;
  private int videoHeight;
  private int videoWidth;
  private WindowManager.LayoutParams windowLayoutParams;
  private WindowManager windowManager;
  private FrameLayout windowView;
  
  private void animateToBoundsMaybe()
  {
    int n = getSideCoord(true, 0, 0.0F, this.videoWidth);
    int i1 = getSideCoord(true, 1, 0.0F, this.videoWidth);
    int j = getSideCoord(false, 0, 0.0F, this.videoHeight);
    int k = getSideCoord(false, 1, 0.0F, this.videoHeight);
    Object localObject2 = null;
    Object localObject3 = null;
    Object localObject4 = null;
    Object localObject1 = null;
    SharedPreferences.Editor localEditor = this.preferences.edit();
    int m = AndroidUtilities.dp(20.0F);
    int i = 0;
    if ((Math.abs(n - this.windowLayoutParams.x) <= m) || ((this.windowLayoutParams.x < 0) && (this.windowLayoutParams.x > -this.videoWidth / 4)))
    {
      if (0 == 0) {
        localObject1 = new ArrayList();
      }
      localEditor.putInt("sidex", 0);
      if (this.windowView.getAlpha() != 1.0F) {
        ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.windowView, "alpha", new float[] { 1.0F }));
      }
      ((ArrayList)localObject1).add(ObjectAnimator.ofInt(this, "x", new int[] { n }));
      localObject2 = localObject1;
      if (i == 0)
      {
        if ((Math.abs(j - this.windowLayoutParams.y) > m) && (this.windowLayoutParams.y > ActionBar.getCurrentActionBarHeight())) {
          break label694;
        }
        localObject2 = localObject1;
        if (localObject1 == null) {
          localObject2 = new ArrayList();
        }
        localEditor.putInt("sidey", 0);
        ((ArrayList)localObject2).add(ObjectAnimator.ofInt(this, "y", new int[] { j }));
        localObject1 = localObject2;
      }
    }
    for (;;)
    {
      localEditor.commit();
      localObject2 = localObject1;
      if (localObject2 != null)
      {
        if (this.decelerateInterpolator == null) {
          this.decelerateInterpolator = new DecelerateInterpolator();
        }
        localObject1 = new AnimatorSet();
        ((AnimatorSet)localObject1).setInterpolator(this.decelerateInterpolator);
        ((AnimatorSet)localObject1).setDuration(150L);
        if (i != 0)
        {
          ((ArrayList)localObject2).add(ObjectAnimator.ofFloat(this.windowView, "alpha", new float[] { 0.0F }));
          ((AnimatorSet)localObject1).addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              PipRoundVideoView.this.close(false);
              if (PipRoundVideoView.this.onCloseRunnable != null) {
                PipRoundVideoView.this.onCloseRunnable.run();
              }
            }
          });
        }
        ((AnimatorSet)localObject1).playTogether((Collection)localObject2);
        ((AnimatorSet)localObject1).start();
      }
      return;
      if ((Math.abs(i1 - this.windowLayoutParams.x) <= m) || ((this.windowLayoutParams.x > AndroidUtilities.displaySize.x - this.videoWidth) && (this.windowLayoutParams.x < AndroidUtilities.displaySize.x - this.videoWidth / 4 * 3)))
      {
        localObject1 = localObject3;
        if (0 == 0) {
          localObject1 = new ArrayList();
        }
        localEditor.putInt("sidex", 1);
        if (this.windowView.getAlpha() != 1.0F) {
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.windowView, "alpha", new float[] { 1.0F }));
        }
        ((ArrayList)localObject1).add(ObjectAnimator.ofInt(this, "x", new int[] { i1 }));
        break;
      }
      if (this.windowView.getAlpha() != 1.0F)
      {
        localObject1 = localObject4;
        if (0 == 0) {
          localObject1 = new ArrayList();
        }
        if (this.windowLayoutParams.x < 0) {
          ((ArrayList)localObject1).add(ObjectAnimator.ofInt(this, "x", new int[] { -this.videoWidth }));
        }
        for (;;)
        {
          i = 1;
          break;
          ((ArrayList)localObject1).add(ObjectAnimator.ofInt(this, "x", new int[] { AndroidUtilities.displaySize.x }));
        }
      }
      localEditor.putFloat("px", (this.windowLayoutParams.x - n) / (i1 - n));
      localEditor.putInt("sidex", 2);
      localObject1 = localObject2;
      break;
      label694:
      if (Math.abs(k - this.windowLayoutParams.y) <= m)
      {
        localObject2 = localObject1;
        if (localObject1 == null) {
          localObject2 = new ArrayList();
        }
        localEditor.putInt("sidey", 1);
        ((ArrayList)localObject2).add(ObjectAnimator.ofInt(this, "y", new int[] { k }));
        localObject1 = localObject2;
      }
      else
      {
        localEditor.putFloat("py", (this.windowLayoutParams.y - j) / (k - j));
        localEditor.putInt("sidey", 2);
      }
    }
  }
  
  public static PipRoundVideoView getInstance()
  {
    return instance;
  }
  
  private static int getSideCoord(boolean paramBoolean, int paramInt1, float paramFloat, int paramInt2)
  {
    if (paramBoolean)
    {
      paramInt2 = AndroidUtilities.displaySize.x - paramInt2;
      if (paramInt1 != 0) {
        break label53;
      }
      paramInt1 = AndroidUtilities.dp(10.0F);
    }
    for (;;)
    {
      paramInt2 = paramInt1;
      if (!paramBoolean) {
        paramInt2 = paramInt1 + ActionBar.getCurrentActionBarHeight();
      }
      return paramInt2;
      paramInt2 = AndroidUtilities.displaySize.y - paramInt2 - ActionBar.getCurrentActionBarHeight();
      break;
      label53:
      if (paramInt1 == 1) {
        paramInt1 = paramInt2 - AndroidUtilities.dp(10.0F);
      } else {
        paramInt1 = Math.round((paramInt2 - AndroidUtilities.dp(20.0F)) * paramFloat) + AndroidUtilities.dp(10.0F);
      }
    }
  }
  
  private void runShowHideAnimation(final boolean paramBoolean)
  {
    float f2 = 1.0F;
    if (this.hideShowAnimation != null) {
      this.hideShowAnimation.cancel();
    }
    this.hideShowAnimation = new AnimatorSet();
    AnimatorSet localAnimatorSet = this.hideShowAnimation;
    Object localObject1 = this.windowView;
    Object localObject2;
    label73:
    FrameLayout localFrameLayout;
    if (paramBoolean)
    {
      f1 = 1.0F;
      localObject1 = ObjectAnimator.ofFloat(localObject1, "alpha", new float[] { f1 });
      localObject2 = this.windowView;
      if (!paramBoolean) {
        break label206;
      }
      f1 = 1.0F;
      localObject2 = ObjectAnimator.ofFloat(localObject2, "scaleX", new float[] { f1 });
      localFrameLayout = this.windowView;
      if (!paramBoolean) {
        break label213;
      }
    }
    label206:
    label213:
    for (float f1 = f2;; f1 = 0.8F)
    {
      localAnimatorSet.playTogether(new Animator[] { localObject1, localObject2, ObjectAnimator.ofFloat(localFrameLayout, "scaleY", new float[] { f1 }) });
      this.hideShowAnimation.setDuration(150L);
      if (this.decelerateInterpolator == null) {
        this.decelerateInterpolator = new DecelerateInterpolator();
      }
      this.hideShowAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PipRoundVideoView.this.hideShowAnimation)) {
            PipRoundVideoView.access$702(PipRoundVideoView.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PipRoundVideoView.this.hideShowAnimation))
          {
            if (!paramBoolean) {
              PipRoundVideoView.this.close(false);
            }
            PipRoundVideoView.access$702(PipRoundVideoView.this, null);
          }
        }
      });
      this.hideShowAnimation.setInterpolator(this.decelerateInterpolator);
      this.hideShowAnimation.start();
      return;
      f1 = 0.0F;
      break;
      f1 = 0.8F;
      break label73;
    }
  }
  
  /* Error */
  public void close(boolean paramBoolean)
  {
    // Byte code:
    //   0: iload_1
    //   1: ifeq +121 -> 122
    //   4: aload_0
    //   5: getfield 90	org/telegram/ui/Components/PipRoundVideoView:textureView	Landroid/view/TextureView;
    //   8: ifnull +104 -> 112
    //   11: aload_0
    //   12: getfield 90	org/telegram/ui/Components/PipRoundVideoView:textureView	Landroid/view/TextureView;
    //   15: invokevirtual 268	android/view/TextureView:getParent	()Landroid/view/ViewParent;
    //   18: ifnull +94 -> 112
    //   21: aload_0
    //   22: getfield 90	org/telegram/ui/Components/PipRoundVideoView:textureView	Landroid/view/TextureView;
    //   25: invokevirtual 271	android/view/TextureView:getWidth	()I
    //   28: ifle +37 -> 65
    //   31: aload_0
    //   32: getfield 90	org/telegram/ui/Components/PipRoundVideoView:textureView	Landroid/view/TextureView;
    //   35: invokevirtual 274	android/view/TextureView:getHeight	()I
    //   38: ifle +27 -> 65
    //   41: aload_0
    //   42: aload_0
    //   43: getfield 90	org/telegram/ui/Components/PipRoundVideoView:textureView	Landroid/view/TextureView;
    //   46: invokevirtual 271	android/view/TextureView:getWidth	()I
    //   49: aload_0
    //   50: getfield 90	org/telegram/ui/Components/PipRoundVideoView:textureView	Landroid/view/TextureView;
    //   53: invokevirtual 274	android/view/TextureView:getHeight	()I
    //   56: getstatic 280	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
    //   59: invokestatic 286	org/telegram/messenger/Bitmaps:createBitmap	(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;
    //   62: putfield 288	org/telegram/ui/Components/PipRoundVideoView:bitmap	Landroid/graphics/Bitmap;
    //   65: aload_0
    //   66: getfield 90	org/telegram/ui/Components/PipRoundVideoView:textureView	Landroid/view/TextureView;
    //   69: aload_0
    //   70: getfield 288	org/telegram/ui/Components/PipRoundVideoView:bitmap	Landroid/graphics/Bitmap;
    //   73: invokevirtual 292	android/view/TextureView:getBitmap	(Landroid/graphics/Bitmap;)Landroid/graphics/Bitmap;
    //   76: pop
    //   77: aload_0
    //   78: getfield 294	org/telegram/ui/Components/PipRoundVideoView:imageView	Landroid/widget/ImageView;
    //   81: aload_0
    //   82: getfield 288	org/telegram/ui/Components/PipRoundVideoView:bitmap	Landroid/graphics/Bitmap;
    //   85: invokevirtual 300	android/widget/ImageView:setImageBitmap	(Landroid/graphics/Bitmap;)V
    //   88: aload_0
    //   89: getfield 302	org/telegram/ui/Components/PipRoundVideoView:aspectRatioFrameLayout	Lorg/telegram/messenger/exoplayer2/ui/AspectRatioFrameLayout;
    //   92: aload_0
    //   93: getfield 90	org/telegram/ui/Components/PipRoundVideoView:textureView	Landroid/view/TextureView;
    //   96: invokevirtual 308	org/telegram/messenger/exoplayer2/ui/AspectRatioFrameLayout:removeView	(Landroid/view/View;)V
    //   99: aload_0
    //   100: getfield 294	org/telegram/ui/Components/PipRoundVideoView:imageView	Landroid/widget/ImageView;
    //   103: iconst_0
    //   104: invokevirtual 312	android/widget/ImageView:setVisibility	(I)V
    //   107: aload_0
    //   108: iconst_0
    //   109: invokespecial 314	org/telegram/ui/Components/PipRoundVideoView:runShowHideAnimation	(Z)V
    //   112: return
    //   113: astore_2
    //   114: aload_0
    //   115: aconst_null
    //   116: putfield 288	org/telegram/ui/Components/PipRoundVideoView:bitmap	Landroid/graphics/Bitmap;
    //   119: goto -42 -> 77
    //   122: aload_0
    //   123: getfield 288	org/telegram/ui/Components/PipRoundVideoView:bitmap	Landroid/graphics/Bitmap;
    //   126: ifnull +23 -> 149
    //   129: aload_0
    //   130: getfield 294	org/telegram/ui/Components/PipRoundVideoView:imageView	Landroid/widget/ImageView;
    //   133: aconst_null
    //   134: invokevirtual 318	android/widget/ImageView:setImageDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   137: aload_0
    //   138: getfield 288	org/telegram/ui/Components/PipRoundVideoView:bitmap	Landroid/graphics/Bitmap;
    //   141: invokevirtual 323	android/graphics/Bitmap:recycle	()V
    //   144: aload_0
    //   145: aconst_null
    //   146: putfield 288	org/telegram/ui/Components/PipRoundVideoView:bitmap	Landroid/graphics/Bitmap;
    //   149: aload_0
    //   150: getfield 81	org/telegram/ui/Components/PipRoundVideoView:windowManager	Landroid/view/WindowManager;
    //   153: aload_0
    //   154: getfield 77	org/telegram/ui/Components/PipRoundVideoView:windowView	Landroid/widget/FrameLayout;
    //   157: invokeinterface 326 2 0
    //   162: getstatic 234	org/telegram/ui/Components/PipRoundVideoView:instance	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   165: aload_0
    //   166: if_acmpne +7 -> 173
    //   169: aconst_null
    //   170: putstatic 234	org/telegram/ui/Components/PipRoundVideoView:instance	Lorg/telegram/ui/Components/PipRoundVideoView;
    //   173: aload_0
    //   174: aconst_null
    //   175: putfield 328	org/telegram/ui/Components/PipRoundVideoView:parentActivity	Landroid/app/Activity;
    //   178: aload_0
    //   179: getfield 330	org/telegram/ui/Components/PipRoundVideoView:currentAccount	I
    //   182: invokestatic 335	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   185: aload_0
    //   186: getstatic 338	org/telegram/messenger/NotificationCenter:messagePlayingProgressDidChanged	I
    //   189: invokevirtual 342	org/telegram/messenger/NotificationCenter:removeObserver	(Ljava/lang/Object;I)V
    //   192: return
    //   193: astore_2
    //   194: goto -32 -> 162
    //   197: astore_2
    //   198: goto -99 -> 99
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	201	0	this	PipRoundVideoView
    //   0	201	1	paramBoolean	boolean
    //   113	1	2	localThrowable	Throwable
    //   193	1	2	localException1	Exception
    //   197	1	2	localException2	Exception
    // Exception table:
    //   from	to	target	type
    //   65	77	113	java/lang/Throwable
    //   149	162	193	java/lang/Exception
    //   88	99	197	java/lang/Exception
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if ((paramInt1 == NotificationCenter.messagePlayingProgressDidChanged) && (this.aspectRatioFrameLayout != null)) {
      this.aspectRatioFrameLayout.invalidate();
    }
  }
  
  public TextureView getTextureView()
  {
    return this.textureView;
  }
  
  public int getX()
  {
    return this.windowLayoutParams.x;
  }
  
  public int getY()
  {
    return this.windowLayoutParams.y;
  }
  
  public void onConfigurationChanged()
  {
    int i = this.preferences.getInt("sidex", 1);
    int j = this.preferences.getInt("sidey", 0);
    float f1 = this.preferences.getFloat("px", 0.0F);
    float f2 = this.preferences.getFloat("py", 0.0F);
    this.windowLayoutParams.x = getSideCoord(true, i, f1, this.videoWidth);
    this.windowLayoutParams.y = getSideCoord(false, j, f2, this.videoHeight);
    this.windowManager.updateViewLayout(this.windowView, this.windowLayoutParams);
  }
  
  @Keep
  public void setX(int paramInt)
  {
    this.windowLayoutParams.x = paramInt;
    try
    {
      this.windowManager.updateViewLayout(this.windowView, this.windowLayoutParams);
      return;
    }
    catch (Exception localException) {}
  }
  
  @Keep
  public void setY(int paramInt)
  {
    this.windowLayoutParams.y = paramInt;
    try
    {
      this.windowManager.updateViewLayout(this.windowView, this.windowLayoutParams);
      return;
    }
    catch (Exception localException) {}
  }
  
  public void show(Activity paramActivity, final Runnable paramRunnable)
  {
    if (paramActivity == null) {
      return;
    }
    instance = this;
    this.onCloseRunnable = paramRunnable;
    this.windowView = new FrameLayout(paramActivity)
    {
      private boolean dragging;
      private boolean startDragging;
      private float startX;
      private float startY;
      
      protected void onDraw(Canvas paramAnonymousCanvas)
      {
        if (Theme.chat_roundVideoShadow != null)
        {
          Theme.chat_roundVideoShadow.setAlpha((int)(getAlpha() * 255.0F));
          Theme.chat_roundVideoShadow.setBounds(AndroidUtilities.dp(1.0F), AndroidUtilities.dp(2.0F), AndroidUtilities.dp(125.0F), AndroidUtilities.dp(125.0F));
          Theme.chat_roundVideoShadow.draw(paramAnonymousCanvas);
        }
      }
      
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if (paramAnonymousMotionEvent.getAction() == 0)
        {
          this.startX = paramAnonymousMotionEvent.getRawX();
          this.startY = paramAnonymousMotionEvent.getRawY();
          this.startDragging = true;
        }
        return true;
      }
      
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if ((!this.startDragging) && (!this.dragging)) {
          return false;
        }
        float f2 = paramAnonymousMotionEvent.getRawX();
        float f3 = paramAnonymousMotionEvent.getRawY();
        if (paramAnonymousMotionEvent.getAction() == 2)
        {
          f1 = f2 - this.startX;
          f4 = f3 - this.startY;
          if (this.startDragging) {
            if ((Math.abs(f1) >= AndroidUtilities.getPixelsInCM(0.3F, true)) || (Math.abs(f4) >= AndroidUtilities.getPixelsInCM(0.3F, false)))
            {
              this.dragging = true;
              this.startDragging = false;
            }
          }
        }
        label187:
        label223:
        label382:
        label454:
        while (paramAnonymousMotionEvent.getAction() != 1)
        {
          float f1;
          float f4;
          do
          {
            return true;
          } while (!this.dragging);
          paramAnonymousMotionEvent = PipRoundVideoView.this.windowLayoutParams;
          paramAnonymousMotionEvent.x = ((int)(paramAnonymousMotionEvent.x + f1));
          paramAnonymousMotionEvent = PipRoundVideoView.this.windowLayoutParams;
          paramAnonymousMotionEvent.y = ((int)(paramAnonymousMotionEvent.y + f4));
          int i = PipRoundVideoView.this.videoWidth / 2;
          if (PipRoundVideoView.this.windowLayoutParams.x < -i)
          {
            PipRoundVideoView.this.windowLayoutParams.x = (-i);
            f1 = 1.0F;
            if (PipRoundVideoView.this.windowLayoutParams.x >= 0) {
              break label382;
            }
            f1 = 1.0F + PipRoundVideoView.this.windowLayoutParams.x / i * 0.5F;
            if (PipRoundVideoView.this.windowView.getAlpha() != f1) {
              PipRoundVideoView.this.windowView.setAlpha(f1);
            }
            if (PipRoundVideoView.this.windowLayoutParams.y >= -0) {
              break label454;
            }
            PipRoundVideoView.this.windowLayoutParams.y = (-0);
          }
          for (;;)
          {
            PipRoundVideoView.this.windowManager.updateViewLayout(PipRoundVideoView.this.windowView, PipRoundVideoView.this.windowLayoutParams);
            this.startX = f2;
            this.startY = f3;
            break;
            if (PipRoundVideoView.this.windowLayoutParams.x <= AndroidUtilities.displaySize.x - PipRoundVideoView.this.windowLayoutParams.width + i) {
              break label187;
            }
            PipRoundVideoView.this.windowLayoutParams.x = (AndroidUtilities.displaySize.x - PipRoundVideoView.this.windowLayoutParams.width + i);
            break label187;
            if (PipRoundVideoView.this.windowLayoutParams.x <= AndroidUtilities.displaySize.x - PipRoundVideoView.this.windowLayoutParams.width) {
              break label223;
            }
            f1 = 1.0F - (PipRoundVideoView.this.windowLayoutParams.x - AndroidUtilities.displaySize.x + PipRoundVideoView.this.windowLayoutParams.width) / i * 0.5F;
            break label223;
            if (PipRoundVideoView.this.windowLayoutParams.y > AndroidUtilities.displaySize.y - PipRoundVideoView.this.windowLayoutParams.height + 0) {
              PipRoundVideoView.this.windowLayoutParams.y = (AndroidUtilities.displaySize.y - PipRoundVideoView.this.windowLayoutParams.height + 0);
            }
          }
        }
        if ((this.startDragging) && (!this.dragging))
        {
          paramAnonymousMotionEvent = MediaController.getInstance().getPlayingMessageObject();
          if (paramAnonymousMotionEvent != null)
          {
            if (!MediaController.getInstance().isMessagePaused()) {
              break label588;
            }
            MediaController.getInstance().playMessage(paramAnonymousMotionEvent);
          }
        }
        for (;;)
        {
          this.dragging = false;
          this.startDragging = false;
          PipRoundVideoView.this.animateToBoundsMaybe();
          break;
          label588:
          MediaController.getInstance().pauseMessage(paramAnonymousMotionEvent);
        }
      }
      
      public void requestDisallowInterceptTouchEvent(boolean paramAnonymousBoolean)
      {
        super.requestDisallowInterceptTouchEvent(paramAnonymousBoolean);
      }
    };
    this.windowView.setWillNotDraw(false);
    this.videoWidth = AndroidUtilities.dp(126.0F);
    this.videoHeight = AndroidUtilities.dp(126.0F);
    if (Build.VERSION.SDK_INT >= 21)
    {
      this.aspectRatioFrameLayout = new AspectRatioFrameLayout(paramActivity)
      {
        protected boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
        {
          boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
          if (paramAnonymousView == PipRoundVideoView.this.textureView)
          {
            paramAnonymousView = MediaController.getInstance().getPlayingMessageObject();
            if (paramAnonymousView != null)
            {
              PipRoundVideoView.this.rect.set(AndroidUtilities.dpf2(1.5F), AndroidUtilities.dpf2(1.5F), getMeasuredWidth() - AndroidUtilities.dpf2(1.5F), getMeasuredHeight() - AndroidUtilities.dpf2(1.5F));
              paramAnonymousCanvas.drawArc(PipRoundVideoView.this.rect, -90.0F, paramAnonymousView.audioProgress * 360.0F, false, Theme.chat_radialProgressPaint);
            }
          }
          return bool;
        }
      };
      this.aspectRatioFrameLayout.setOutlineProvider(new ViewOutlineProvider()
      {
        @TargetApi(21)
        public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
        {
          paramAnonymousOutline.setOval(0, 0, AndroidUtilities.dp(120.0F), AndroidUtilities.dp(120.0F));
        }
      });
      this.aspectRatioFrameLayout.setClipToOutline(true);
    }
    for (;;)
    {
      this.aspectRatioFrameLayout.setAspectRatio(1.0F, 0);
      this.windowView.addView(this.aspectRatioFrameLayout, LayoutHelper.createFrame(120, 120.0F, 51, 3.0F, 3.0F, 0.0F, 0.0F));
      this.windowView.setAlpha(1.0F);
      this.windowView.setScaleX(0.8F);
      this.windowView.setScaleY(0.8F);
      this.textureView = new TextureView(paramActivity);
      this.aspectRatioFrameLayout.addView(this.textureView, LayoutHelper.createFrame(-1, -1.0F));
      this.imageView = new ImageView(paramActivity);
      this.aspectRatioFrameLayout.addView(this.imageView, LayoutHelper.createFrame(-1, -1.0F));
      this.imageView.setVisibility(4);
      this.windowManager = ((WindowManager)paramActivity.getSystemService("window"));
      this.preferences = ApplicationLoader.applicationContext.getSharedPreferences("pipconfig", 0);
      int i = this.preferences.getInt("sidex", 1);
      int j = this.preferences.getInt("sidey", 0);
      float f1 = this.preferences.getFloat("px", 0.0F);
      float f2 = this.preferences.getFloat("py", 0.0F);
      try
      {
        this.windowLayoutParams = new WindowManager.LayoutParams();
        this.windowLayoutParams.width = this.videoWidth;
        this.windowLayoutParams.height = this.videoHeight;
        this.windowLayoutParams.x = getSideCoord(true, i, f1, this.videoWidth);
        this.windowLayoutParams.y = getSideCoord(false, j, f2, this.videoHeight);
        this.windowLayoutParams.format = -3;
        this.windowLayoutParams.gravity = 51;
        this.windowLayoutParams.type = 99;
        this.windowLayoutParams.flags = 16777736;
        this.windowManager.addView(this.windowView, this.windowLayoutParams);
        this.parentActivity = paramActivity;
        this.currentAccount = UserConfig.selectedAccount;
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
        runShowHideAnimation(true);
        return;
      }
      catch (Exception paramActivity)
      {
        FileLog.e(paramActivity);
      }
      paramRunnable = new Paint(1);
      paramRunnable.setColor(-16777216);
      paramRunnable.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      this.aspectRatioFrameLayout = new AspectRatioFrameLayout(paramActivity)
      {
        private Path aspectPath = new Path();
        
        protected void dispatchDraw(Canvas paramAnonymousCanvas)
        {
          super.dispatchDraw(paramAnonymousCanvas);
          paramAnonymousCanvas.drawPath(this.aspectPath, paramRunnable);
        }
        
        protected boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
        {
          try
          {
            bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
            if (paramAnonymousView == PipRoundVideoView.this.textureView)
            {
              paramAnonymousView = MediaController.getInstance().getPlayingMessageObject();
              if (paramAnonymousView != null)
              {
                PipRoundVideoView.this.rect.set(AndroidUtilities.dpf2(1.5F), AndroidUtilities.dpf2(1.5F), getMeasuredWidth() - AndroidUtilities.dpf2(1.5F), getMeasuredHeight() - AndroidUtilities.dpf2(1.5F));
                paramAnonymousCanvas.drawArc(PipRoundVideoView.this.rect, -90.0F, paramAnonymousView.audioProgress * 360.0F, false, Theme.chat_radialProgressPaint);
              }
            }
            return bool;
          }
          catch (Throwable localThrowable)
          {
            for (;;)
            {
              boolean bool = false;
            }
          }
        }
        
        protected void onSizeChanged(int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          super.onSizeChanged(paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
          this.aspectPath.reset();
          this.aspectPath.addCircle(paramAnonymousInt1 / 2, paramAnonymousInt2 / 2, paramAnonymousInt1 / 2, Path.Direction.CW);
          this.aspectPath.toggleInverseFillType();
        }
      };
      this.aspectRatioFrameLayout.setLayerType(2, null);
    }
  }
  
  public void showTemporary(boolean paramBoolean)
  {
    float f2 = 1.0F;
    if (this.hideShowAnimation != null) {
      this.hideShowAnimation.cancel();
    }
    this.hideShowAnimation = new AnimatorSet();
    AnimatorSet localAnimatorSet = this.hideShowAnimation;
    Object localObject1 = this.windowView;
    Object localObject2;
    label73:
    FrameLayout localFrameLayout;
    if (paramBoolean)
    {
      f1 = 1.0F;
      localObject1 = ObjectAnimator.ofFloat(localObject1, "alpha", new float[] { f1 });
      localObject2 = this.windowView;
      if (!paramBoolean) {
        break label205;
      }
      f1 = 1.0F;
      localObject2 = ObjectAnimator.ofFloat(localObject2, "scaleX", new float[] { f1 });
      localFrameLayout = this.windowView;
      if (!paramBoolean) {
        break label212;
      }
    }
    label205:
    label212:
    for (float f1 = f2;; f1 = 0.8F)
    {
      localAnimatorSet.playTogether(new Animator[] { localObject1, localObject2, ObjectAnimator.ofFloat(localFrameLayout, "scaleY", new float[] { f1 }) });
      this.hideShowAnimation.setDuration(150L);
      if (this.decelerateInterpolator == null) {
        this.decelerateInterpolator = new DecelerateInterpolator();
      }
      this.hideShowAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PipRoundVideoView.this.hideShowAnimation)) {
            PipRoundVideoView.access$702(PipRoundVideoView.this, null);
          }
        }
      });
      this.hideShowAnimation.setInterpolator(this.decelerateInterpolator);
      this.hideShowAnimation.start();
      return;
      f1 = 0.0F;
      break;
      f1 = 0.8F;
      break label73;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/PipRoundVideoView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */