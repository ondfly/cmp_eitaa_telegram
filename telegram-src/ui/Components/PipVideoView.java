package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.support.annotation.Keep;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.ArrayList;
import java.util.Collection;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.exoplayer2.ui.AspectRatioFrameLayout;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.PhotoViewer;

public class PipVideoView
{
  private View controlsView;
  private DecelerateInterpolator decelerateInterpolator;
  private Activity parentActivity;
  private EmbedBottomSheet parentSheet;
  private PhotoViewer photoViewer;
  private SharedPreferences preferences;
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
              if (PipVideoView.this.parentSheet != null) {
                PipVideoView.this.parentSheet.destroy();
              }
              while (PipVideoView.this.photoViewer == null) {
                return;
              }
              PipVideoView.this.photoViewer.destroyPhotoViewer();
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
  
  public static Rect getPipRect(float paramFloat)
  {
    SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("pipconfig", 0);
    int k = localSharedPreferences.getInt("sidex", 1);
    int m = localSharedPreferences.getInt("sidey", 0);
    float f1 = localSharedPreferences.getFloat("px", 0.0F);
    float f2 = localSharedPreferences.getFloat("py", 0.0F);
    int j;
    int i;
    if (paramFloat > 1.0F)
    {
      j = AndroidUtilities.dp(192.0F);
      i = (int)(j / paramFloat);
    }
    for (;;)
    {
      return new Rect(getSideCoord(true, k, f1, j), getSideCoord(false, m, f2, i), j, i);
      i = AndroidUtilities.dp(192.0F);
      j = (int)(i * paramFloat);
    }
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
  
  public void close()
  {
    try
    {
      this.windowManager.removeView(this.windowView);
      this.parentSheet = null;
      this.photoViewer = null;
      this.parentActivity = null;
      return;
    }
    catch (Exception localException)
    {
      for (;;) {}
    }
  }
  
  @Keep
  public int getX()
  {
    return this.windowLayoutParams.x;
  }
  
  @Keep
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
  
  public void onVideoCompleted()
  {
    if ((this.controlsView instanceof MiniControlsView))
    {
      MiniControlsView localMiniControlsView = (MiniControlsView)this.controlsView;
      MiniControlsView.access$1102(localMiniControlsView, true);
      MiniControlsView.access$1202(localMiniControlsView, 0.0F);
      MiniControlsView.access$1302(localMiniControlsView, 0.0F);
      localMiniControlsView.updatePlayButton();
      localMiniControlsView.invalidate();
      localMiniControlsView.show(true, true);
    }
  }
  
  public void setBufferedProgress(float paramFloat)
  {
    if ((this.controlsView instanceof MiniControlsView)) {
      ((MiniControlsView)this.controlsView).setBufferedProgress(paramFloat);
    }
  }
  
  @Keep
  public void setX(int paramInt)
  {
    this.windowLayoutParams.x = paramInt;
    this.windowManager.updateViewLayout(this.windowView, this.windowLayoutParams);
  }
  
  @Keep
  public void setY(int paramInt)
  {
    this.windowLayoutParams.y = paramInt;
    this.windowManager.updateViewLayout(this.windowView, this.windowLayoutParams);
  }
  
  public TextureView show(Activity paramActivity, EmbedBottomSheet paramEmbedBottomSheet, View paramView, float paramFloat, int paramInt, WebView paramWebView)
  {
    return show(paramActivity, null, paramEmbedBottomSheet, paramView, paramFloat, paramInt, paramWebView);
  }
  
  public TextureView show(Activity paramActivity, PhotoViewer paramPhotoViewer, float paramFloat, int paramInt)
  {
    return show(paramActivity, paramPhotoViewer, null, null, paramFloat, paramInt, null);
  }
  
  public TextureView show(Activity paramActivity, PhotoViewer paramPhotoViewer, EmbedBottomSheet paramEmbedBottomSheet, View paramView, float paramFloat, int paramInt, WebView paramWebView)
  {
    this.parentSheet = paramEmbedBottomSheet;
    this.parentActivity = paramActivity;
    this.photoViewer = paramPhotoViewer;
    this.windowView = new FrameLayout(paramActivity)
    {
      private boolean dragging;
      private float startX;
      private float startY;
      
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        float f1 = paramAnonymousMotionEvent.getRawX();
        float f2 = paramAnonymousMotionEvent.getRawY();
        if (paramAnonymousMotionEvent.getAction() == 0)
        {
          this.startX = f1;
          this.startY = f2;
        }
        while ((paramAnonymousMotionEvent.getAction() != 2) || (this.dragging) || ((Math.abs(this.startX - f1) < AndroidUtilities.getPixelsInCM(0.3F, true)) && (Math.abs(this.startY - f2) < AndroidUtilities.getPixelsInCM(0.3F, false)))) {
          return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
        }
        this.dragging = true;
        this.startX = f1;
        this.startY = f2;
        if (PipVideoView.this.controlsView != null) {
          ((ViewParent)PipVideoView.this.controlsView).requestDisallowInterceptTouchEvent(true);
        }
        return true;
      }
      
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if (!this.dragging) {
          return false;
        }
        float f2 = paramAnonymousMotionEvent.getRawX();
        float f3 = paramAnonymousMotionEvent.getRawY();
        float f1;
        int i;
        if (paramAnonymousMotionEvent.getAction() == 2)
        {
          f1 = this.startX;
          float f4 = this.startY;
          paramAnonymousMotionEvent = PipVideoView.this.windowLayoutParams;
          paramAnonymousMotionEvent.x = ((int)(paramAnonymousMotionEvent.x + (f2 - f1)));
          paramAnonymousMotionEvent = PipVideoView.this.windowLayoutParams;
          paramAnonymousMotionEvent.y = ((int)(paramAnonymousMotionEvent.y + (f3 - f4)));
          i = PipVideoView.this.videoWidth / 2;
          if (PipVideoView.this.windowLayoutParams.x < -i)
          {
            PipVideoView.this.windowLayoutParams.x = (-i);
            f1 = 1.0F;
            if (PipVideoView.this.windowLayoutParams.x >= 0) {
              break label319;
            }
            f1 = 1.0F + PipVideoView.this.windowLayoutParams.x / i * 0.5F;
            label161:
            if (PipVideoView.this.windowView.getAlpha() != f1) {
              PipVideoView.this.windowView.setAlpha(f1);
            }
            if (PipVideoView.this.windowLayoutParams.y >= -0) {
              break label391;
            }
            PipVideoView.this.windowLayoutParams.y = (-0);
            label214:
            PipVideoView.this.windowManager.updateViewLayout(PipVideoView.this.windowView, PipVideoView.this.windowLayoutParams);
            this.startX = f2;
            this.startY = f3;
          }
        }
        for (;;)
        {
          return true;
          if (PipVideoView.this.windowLayoutParams.x <= AndroidUtilities.displaySize.x - PipVideoView.this.windowLayoutParams.width + i) {
            break;
          }
          PipVideoView.this.windowLayoutParams.x = (AndroidUtilities.displaySize.x - PipVideoView.this.windowLayoutParams.width + i);
          break;
          label319:
          if (PipVideoView.this.windowLayoutParams.x <= AndroidUtilities.displaySize.x - PipVideoView.this.windowLayoutParams.width) {
            break label161;
          }
          f1 = 1.0F - (PipVideoView.this.windowLayoutParams.x - AndroidUtilities.displaySize.x + PipVideoView.this.windowLayoutParams.width) / i * 0.5F;
          break label161;
          label391:
          if (PipVideoView.this.windowLayoutParams.y <= AndroidUtilities.displaySize.y - PipVideoView.this.windowLayoutParams.height + 0) {
            break label214;
          }
          PipVideoView.this.windowLayoutParams.y = (AndroidUtilities.displaySize.y - PipVideoView.this.windowLayoutParams.height + 0);
          break label214;
          if (paramAnonymousMotionEvent.getAction() == 1)
          {
            this.dragging = false;
            PipVideoView.this.animateToBoundsMaybe();
          }
        }
      }
      
      public void requestDisallowInterceptTouchEvent(boolean paramAnonymousBoolean)
      {
        super.requestDisallowInterceptTouchEvent(paramAnonymousBoolean);
      }
    };
    if (paramFloat > 1.0F)
    {
      this.videoWidth = AndroidUtilities.dp(192.0F);
      this.videoHeight = ((int)(this.videoWidth / paramFloat));
    }
    for (;;)
    {
      AspectRatioFrameLayout localAspectRatioFrameLayout = new AspectRatioFrameLayout(paramActivity);
      localAspectRatioFrameLayout.setAspectRatio(paramFloat, paramInt);
      this.windowView.addView(localAspectRatioFrameLayout, LayoutHelper.createFrame(-1, -1, 17));
      label132:
      boolean bool;
      label144:
      label159:
      int i;
      float f;
      if (paramWebView != null)
      {
        paramEmbedBottomSheet = (ViewGroup)paramWebView.getParent();
        if (paramEmbedBottomSheet != null) {
          paramEmbedBottomSheet.removeView(paramWebView);
        }
        localAspectRatioFrameLayout.addView(paramWebView, LayoutHelper.createFrame(-1, -1.0F));
        paramEmbedBottomSheet = null;
        if (paramView != null) {
          break label454;
        }
        if (paramPhotoViewer == null) {
          break label448;
        }
        bool = true;
        this.controlsView = new MiniControlsView(paramActivity, bool);
        this.windowView.addView(this.controlsView, LayoutHelper.createFrame(-1, -1.0F));
        this.windowManager = ((WindowManager)ApplicationLoader.applicationContext.getSystemService("window"));
        this.preferences = ApplicationLoader.applicationContext.getSharedPreferences("pipconfig", 0);
        paramInt = this.preferences.getInt("sidex", 1);
        i = this.preferences.getInt("sidey", 0);
        paramFloat = this.preferences.getFloat("px", 0.0F);
        f = this.preferences.getFloat("py", 0.0F);
      }
      try
      {
        this.windowLayoutParams = new WindowManager.LayoutParams();
        this.windowLayoutParams.width = this.videoWidth;
        this.windowLayoutParams.height = this.videoHeight;
        this.windowLayoutParams.x = getSideCoord(true, paramInt, paramFloat, this.videoWidth);
        this.windowLayoutParams.y = getSideCoord(false, i, f, this.videoHeight);
        this.windowLayoutParams.format = -3;
        this.windowLayoutParams.gravity = 51;
        if (Build.VERSION.SDK_INT >= 26) {}
        for (this.windowLayoutParams.type = 2038;; this.windowLayoutParams.type = 2003)
        {
          this.windowLayoutParams.flags = 16777736;
          this.windowManager.addView(this.windowView, this.windowLayoutParams);
          return paramEmbedBottomSheet;
          this.videoHeight = AndroidUtilities.dp(192.0F);
          this.videoWidth = ((int)(this.videoHeight * paramFloat));
          break;
          paramEmbedBottomSheet = new TextureView(paramActivity);
          localAspectRatioFrameLayout.addView(paramEmbedBottomSheet, LayoutHelper.createFrame(-1, -1.0F));
          break label132;
          label448:
          bool = false;
          break label144;
          label454:
          this.controlsView = paramView;
          break label159;
        }
        return null;
      }
      catch (Exception paramActivity)
      {
        FileLog.e(paramActivity);
      }
    }
  }
  
  public void updatePlayButton()
  {
    if ((this.controlsView instanceof MiniControlsView))
    {
      MiniControlsView localMiniControlsView = (MiniControlsView)this.controlsView;
      localMiniControlsView.updatePlayButton();
      localMiniControlsView.invalidate();
    }
  }
  
  private class MiniControlsView
    extends FrameLayout
  {
    private float bufferedPosition;
    private AnimatorSet currentAnimation;
    private Runnable hideRunnable = new Runnable()
    {
      public void run()
      {
        PipVideoView.MiniControlsView.this.show(false, true);
      }
    };
    private ImageView inlineButton;
    private boolean isCompleted;
    private boolean isVisible = true;
    private ImageView playButton;
    private float progress;
    private Paint progressInnerPaint;
    private Paint progressPaint;
    private Runnable progressRunnable = new Runnable()
    {
      public void run()
      {
        if (PipVideoView.this.photoViewer == null) {}
        VideoPlayer localVideoPlayer;
        do
        {
          return;
          localVideoPlayer = PipVideoView.this.photoViewer.getVideoPlayer();
        } while (localVideoPlayer == null);
        PipVideoView.MiniControlsView.this.setProgress((float)localVideoPlayer.getCurrentPosition() / (float)localVideoPlayer.getDuration());
        if (PipVideoView.this.photoViewer == null) {
          PipVideoView.MiniControlsView.this.setBufferedProgress((float)localVideoPlayer.getBufferedPosition() / (float)localVideoPlayer.getDuration());
        }
        AndroidUtilities.runOnUIThread(PipVideoView.MiniControlsView.this.progressRunnable, 1000L);
      }
    };
    
    public MiniControlsView(Context paramContext, boolean paramBoolean)
    {
      super();
      this.inlineButton = new ImageView(paramContext);
      this.inlineButton.setScaleType(ImageView.ScaleType.CENTER);
      this.inlineButton.setImageResource(2131165397);
      addView(this.inlineButton, LayoutHelper.createFrame(56, 48, 53));
      this.inlineButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (PipVideoView.this.parentSheet != null) {
            PipVideoView.this.parentSheet.exitFromPip();
          }
          while (PipVideoView.this.photoViewer == null) {
            return;
          }
          PipVideoView.this.photoViewer.exitFromPip();
        }
      });
      if (paramBoolean)
      {
        this.progressPaint = new Paint();
        this.progressPaint.setColor(-15095832);
        this.progressInnerPaint = new Paint();
        this.progressInnerPaint.setColor(-6975081);
        setWillNotDraw(false);
        this.playButton = new ImageView(paramContext);
        this.playButton.setScaleType(ImageView.ScaleType.CENTER);
        addView(this.playButton, LayoutHelper.createFrame(48, 48, 17));
        this.playButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (PipVideoView.this.photoViewer == null) {}
            do
            {
              return;
              paramAnonymousView = PipVideoView.this.photoViewer.getVideoPlayer();
            } while (paramAnonymousView == null);
            if (paramAnonymousView.isPlaying()) {
              paramAnonymousView.pause();
            }
            for (;;)
            {
              PipVideoView.MiniControlsView.this.updatePlayButton();
              return;
              paramAnonymousView.play();
            }
          }
        });
      }
      setOnTouchListener(new View.OnTouchListener()
      {
        public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
        {
          return true;
        }
      });
      updatePlayButton();
      show(false, false);
    }
    
    private void checkNeedHide()
    {
      AndroidUtilities.cancelRunOnUIThread(this.hideRunnable);
      if (this.isVisible) {
        AndroidUtilities.runOnUIThread(this.hideRunnable, 3000L);
      }
    }
    
    private void updatePlayButton()
    {
      if (PipVideoView.this.photoViewer == null) {}
      VideoPlayer localVideoPlayer;
      do
      {
        return;
        localVideoPlayer = PipVideoView.this.photoViewer.getVideoPlayer();
      } while (localVideoPlayer == null);
      AndroidUtilities.cancelRunOnUIThread(this.progressRunnable);
      if (!localVideoPlayer.isPlaying())
      {
        if (this.isCompleted)
        {
          this.playButton.setImageResource(2131165363);
          return;
        }
        this.playButton.setImageResource(2131165403);
        return;
      }
      this.playButton.setImageResource(2131165399);
      AndroidUtilities.runOnUIThread(this.progressRunnable, 500L);
    }
    
    protected void onAttachedToWindow()
    {
      super.onAttachedToWindow();
      checkNeedHide();
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      int i = getMeasuredWidth();
      int j = getMeasuredHeight();
      j -= AndroidUtilities.dp(3.0F);
      AndroidUtilities.dp(7.0F);
      int k = (int)((i - 0) * this.progress);
      if (this.bufferedPosition != 0.0F)
      {
        float f1 = 0;
        float f2 = j;
        float f3 = 0;
        paramCanvas.drawRect(f1, f2, (i - 0) * this.bufferedPosition + f3, AndroidUtilities.dp(3.0F) + j, this.progressInnerPaint);
      }
      paramCanvas.drawRect(0, j, 0 + k, AndroidUtilities.dp(3.0F) + j, this.progressPaint);
    }
    
    public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
    {
      if (paramMotionEvent.getAction() == 0)
      {
        if (!this.isVisible)
        {
          show(true, true);
          return true;
        }
        checkNeedHide();
      }
      return super.onInterceptTouchEvent(paramMotionEvent);
    }
    
    public void requestDisallowInterceptTouchEvent(boolean paramBoolean)
    {
      super.requestDisallowInterceptTouchEvent(paramBoolean);
      checkNeedHide();
    }
    
    public void setBufferedProgress(float paramFloat)
    {
      this.bufferedPosition = paramFloat;
      invalidate();
    }
    
    public void setProgress(float paramFloat)
    {
      this.progress = paramFloat;
      invalidate();
    }
    
    public void show(boolean paramBoolean1, boolean paramBoolean2)
    {
      if (this.isVisible == paramBoolean1) {
        return;
      }
      this.isVisible = paramBoolean1;
      if (this.currentAnimation != null) {
        this.currentAnimation.cancel();
      }
      if (this.isVisible) {
        if (paramBoolean2)
        {
          this.currentAnimation = new AnimatorSet();
          this.currentAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "alpha", new float[] { 1.0F }) });
          this.currentAnimation.setDuration(150L);
          this.currentAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              PipVideoView.MiniControlsView.access$402(PipVideoView.MiniControlsView.this, null);
            }
          });
          this.currentAnimation.start();
        }
      }
      for (;;)
      {
        checkNeedHide();
        return;
        setAlpha(1.0F);
        continue;
        if (paramBoolean2)
        {
          this.currentAnimation = new AnimatorSet();
          this.currentAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "alpha", new float[] { 0.0F }) });
          this.currentAnimation.setDuration(150L);
          this.currentAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              PipVideoView.MiniControlsView.access$402(PipVideoView.MiniControlsView.this, null);
            }
          });
          this.currentAnimation.start();
        }
        else
        {
          setAlpha(0.0F);
        }
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/PipVideoView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */