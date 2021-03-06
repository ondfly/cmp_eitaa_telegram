package org.telegram.ui.ActionBar;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.Keep;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.ui.Components.LayoutHelper;

public class ActionBarPopupWindow
  extends PopupWindow
{
  private static final ViewTreeObserver.OnScrollChangedListener NOP;
  private static final boolean allowAnimation;
  private static DecelerateInterpolator decelerateInterpolator;
  private static final Field superListenerField;
  private boolean animationEnabled = allowAnimation;
  private ViewTreeObserver.OnScrollChangedListener mSuperScrollListener;
  private ViewTreeObserver mViewTreeObserver;
  private AnimatorSet windowAnimatorSet;
  
  static
  {
    boolean bool = true;
    if (Build.VERSION.SDK_INT >= 18) {}
    for (;;)
    {
      allowAnimation = bool;
      decelerateInterpolator = new DecelerateInterpolator();
      Object localObject = null;
      try
      {
        Field localField = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
        localObject = localField;
        localField.setAccessible(true);
        localObject = localField;
      }
      catch (NoSuchFieldException localNoSuchFieldException)
      {
        for (;;) {}
      }
      superListenerField = (Field)localObject;
      NOP = new ViewTreeObserver.OnScrollChangedListener()
      {
        public void onScrollChanged() {}
      };
      return;
      bool = false;
    }
  }
  
  public ActionBarPopupWindow()
  {
    init();
  }
  
  public ActionBarPopupWindow(int paramInt1, int paramInt2)
  {
    super(paramInt1, paramInt2);
    init();
  }
  
  public ActionBarPopupWindow(Context paramContext)
  {
    super(paramContext);
    init();
  }
  
  public ActionBarPopupWindow(View paramView)
  {
    super(paramView);
    init();
  }
  
  public ActionBarPopupWindow(View paramView, int paramInt1, int paramInt2)
  {
    super(paramView, paramInt1, paramInt2);
    init();
  }
  
  public ActionBarPopupWindow(View paramView, int paramInt1, int paramInt2, boolean paramBoolean)
  {
    super(paramView, paramInt1, paramInt2, paramBoolean);
    init();
  }
  
  private void init()
  {
    if (superListenerField != null) {}
    try
    {
      this.mSuperScrollListener = ((ViewTreeObserver.OnScrollChangedListener)superListenerField.get(this));
      superListenerField.set(this, NOP);
      return;
    }
    catch (Exception localException)
    {
      this.mSuperScrollListener = null;
    }
  }
  
  private void registerListener(View paramView)
  {
    if (this.mSuperScrollListener != null) {
      if (paramView.getWindowToken() == null) {
        break label73;
      }
    }
    label73:
    for (paramView = paramView.getViewTreeObserver();; paramView = null)
    {
      if (paramView != this.mViewTreeObserver)
      {
        if ((this.mViewTreeObserver != null) && (this.mViewTreeObserver.isAlive())) {
          this.mViewTreeObserver.removeOnScrollChangedListener(this.mSuperScrollListener);
        }
        this.mViewTreeObserver = paramView;
        if (paramView != null) {
          paramView.addOnScrollChangedListener(this.mSuperScrollListener);
        }
      }
      return;
    }
  }
  
  private void unregisterListener()
  {
    if ((this.mSuperScrollListener != null) && (this.mViewTreeObserver != null))
    {
      if (this.mViewTreeObserver.isAlive()) {
        this.mViewTreeObserver.removeOnScrollChangedListener(this.mSuperScrollListener);
      }
      this.mViewTreeObserver = null;
    }
  }
  
  public void dismiss()
  {
    dismiss(true);
  }
  
  public void dismiss(boolean paramBoolean)
  {
    setFocusable(false);
    if ((this.animationEnabled) && (paramBoolean))
    {
      if (this.windowAnimatorSet != null) {
        this.windowAnimatorSet.cancel();
      }
      ActionBarPopupWindowLayout localActionBarPopupWindowLayout = (ActionBarPopupWindowLayout)getContentView();
      this.windowAnimatorSet = new AnimatorSet();
      AnimatorSet localAnimatorSet = this.windowAnimatorSet;
      if (localActionBarPopupWindowLayout.showedFromBotton) {}
      for (float f = 5.0F;; f = -5.0F)
      {
        localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(localActionBarPopupWindowLayout, "translationY", new float[] { AndroidUtilities.dp(f) }), ObjectAnimator.ofFloat(localActionBarPopupWindowLayout, "alpha", new float[] { 0.0F }) });
        this.windowAnimatorSet.setDuration(150L);
        this.windowAnimatorSet.addListener(new Animator.AnimatorListener()
        {
          public void onAnimationCancel(Animator paramAnonymousAnimator)
          {
            onAnimationEnd(paramAnonymousAnimator);
          }
          
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            ActionBarPopupWindow.access$502(ActionBarPopupWindow.this, null);
            ActionBarPopupWindow.this.setFocusable(false);
            try
            {
              ActionBarPopupWindow.this.dismiss();
              ActionBarPopupWindow.this.unregisterListener();
              return;
            }
            catch (Exception paramAnonymousAnimator)
            {
              for (;;) {}
            }
          }
          
          public void onAnimationRepeat(Animator paramAnonymousAnimator) {}
          
          public void onAnimationStart(Animator paramAnonymousAnimator) {}
        });
        this.windowAnimatorSet.start();
        return;
      }
    }
    try
    {
      super.dismiss();
      unregisterListener();
      return;
    }
    catch (Exception localException)
    {
      for (;;) {}
    }
  }
  
  public void setAnimationEnabled(boolean paramBoolean)
  {
    this.animationEnabled = paramBoolean;
  }
  
  public void showAsDropDown(View paramView, int paramInt1, int paramInt2)
  {
    try
    {
      super.showAsDropDown(paramView, paramInt1, paramInt2);
      registerListener(paramView);
      return;
    }
    catch (Exception paramView)
    {
      FileLog.e(paramView);
    }
  }
  
  public void showAtLocation(View paramView, int paramInt1, int paramInt2, int paramInt3)
  {
    super.showAtLocation(paramView, paramInt1, paramInt2, paramInt3);
    unregisterListener();
  }
  
  public void startAnimation()
  {
    if ((!this.animationEnabled) || (this.windowAnimatorSet != null)) {
      return;
    }
    ActionBarPopupWindowLayout localActionBarPopupWindowLayout = (ActionBarPopupWindowLayout)getContentView();
    localActionBarPopupWindowLayout.setTranslationY(0.0F);
    localActionBarPopupWindowLayout.setAlpha(1.0F);
    localActionBarPopupWindowLayout.setPivotX(localActionBarPopupWindowLayout.getMeasuredWidth());
    localActionBarPopupWindowLayout.setPivotY(0.0F);
    int k = localActionBarPopupWindowLayout.getItemsCount();
    localActionBarPopupWindowLayout.positions.clear();
    int j = 0;
    int i = 0;
    if (i < k)
    {
      View localView = localActionBarPopupWindowLayout.getItemAt(i);
      if (localView.getVisibility() != 0) {}
      for (;;)
      {
        i += 1;
        break;
        localActionBarPopupWindowLayout.positions.put(localView, Integer.valueOf(j));
        localView.setAlpha(0.0F);
        j += 1;
      }
    }
    if (localActionBarPopupWindowLayout.showedFromBotton) {
      ActionBarPopupWindowLayout.access$402(localActionBarPopupWindowLayout, k - 1);
    }
    for (;;)
    {
      this.windowAnimatorSet = new AnimatorSet();
      this.windowAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(localActionBarPopupWindowLayout, "backScaleY", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofInt(localActionBarPopupWindowLayout, "backAlpha", new int[] { 0, 255 }) });
      this.windowAnimatorSet.setDuration(j * 16 + 150);
      this.windowAnimatorSet.addListener(new Animator.AnimatorListener()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          onAnimationEnd(paramAnonymousAnimator);
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          ActionBarPopupWindow.access$502(ActionBarPopupWindow.this, null);
        }
        
        public void onAnimationRepeat(Animator paramAnonymousAnimator) {}
        
        public void onAnimationStart(Animator paramAnonymousAnimator) {}
      });
      this.windowAnimatorSet.start();
      return;
      ActionBarPopupWindowLayout.access$402(localActionBarPopupWindowLayout, 0);
    }
  }
  
  public void update(View paramView, int paramInt1, int paramInt2)
  {
    super.update(paramView, paramInt1, paramInt2);
    registerListener(paramView);
  }
  
  public void update(View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.update(paramView, paramInt1, paramInt2, paramInt3, paramInt4);
    registerListener(paramView);
  }
  
  public static class ActionBarPopupWindowLayout
    extends FrameLayout
  {
    private boolean animationEnabled = ActionBarPopupWindow.allowAnimation;
    private int backAlpha = 255;
    private float backScaleX = 1.0F;
    private float backScaleY = 1.0F;
    protected Drawable backgroundDrawable = getResources().getDrawable(2131165609).mutate();
    private int lastStartedChild = 0;
    protected LinearLayout linearLayout;
    private ActionBarPopupWindow.OnDispatchKeyEventListener mOnDispatchKeyEventListener;
    private HashMap<View, Integer> positions = new HashMap();
    private ScrollView scrollView;
    private boolean showedFromBotton;
    
    public ActionBarPopupWindowLayout(Context paramContext)
    {
      super();
      this.backgroundDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("actionBarDefaultSubmenuBackground"), PorterDuff.Mode.MULTIPLY));
      setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F));
      setWillNotDraw(false);
      try
      {
        this.scrollView = new ScrollView(paramContext);
        this.scrollView.setVerticalScrollBarEnabled(false);
        addView(this.scrollView, LayoutHelper.createFrame(-2, -2.0F));
        this.linearLayout = new LinearLayout(paramContext);
        this.linearLayout.setOrientation(1);
        if (this.scrollView != null)
        {
          this.scrollView.addView(this.linearLayout, new FrameLayout.LayoutParams(-2, -2));
          return;
        }
      }
      catch (Throwable localThrowable)
      {
        for (;;)
        {
          FileLog.e(localThrowable);
        }
        addView(this.linearLayout, LayoutHelper.createFrame(-2, -2.0F));
      }
    }
    
    private void startChildAnimation(View paramView)
    {
      AnimatorSet localAnimatorSet;
      ObjectAnimator localObjectAnimator;
      if (this.animationEnabled)
      {
        localAnimatorSet = new AnimatorSet();
        localObjectAnimator = ObjectAnimator.ofFloat(paramView, "alpha", new float[] { 0.0F, 1.0F });
        if (!this.showedFromBotton) {
          break label101;
        }
      }
      label101:
      for (float f = 6.0F;; f = -6.0F)
      {
        localAnimatorSet.playTogether(new Animator[] { localObjectAnimator, ObjectAnimator.ofFloat(paramView, "translationY", new float[] { AndroidUtilities.dp(f), 0.0F }) });
        localAnimatorSet.setDuration(180L);
        localAnimatorSet.setInterpolator(ActionBarPopupWindow.decelerateInterpolator);
        localAnimatorSet.start();
        return;
      }
    }
    
    public void addView(View paramView)
    {
      this.linearLayout.addView(paramView);
    }
    
    public boolean dispatchKeyEvent(KeyEvent paramKeyEvent)
    {
      if (this.mOnDispatchKeyEventListener != null) {
        this.mOnDispatchKeyEventListener.onDispatchKeyEvent(paramKeyEvent);
      }
      return super.dispatchKeyEvent(paramKeyEvent);
    }
    
    @Keep
    public int getBackAlpha()
    {
      return this.backAlpha;
    }
    
    public float getBackScaleX()
    {
      return this.backScaleX;
    }
    
    public float getBackScaleY()
    {
      return this.backScaleY;
    }
    
    public View getItemAt(int paramInt)
    {
      return this.linearLayout.getChildAt(paramInt);
    }
    
    public int getItemsCount()
    {
      return this.linearLayout.getChildCount();
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      if (this.backgroundDrawable != null)
      {
        this.backgroundDrawable.setAlpha(this.backAlpha);
        getMeasuredHeight();
        if (!this.showedFromBotton) {
          break label75;
        }
        this.backgroundDrawable.setBounds(0, (int)(getMeasuredHeight() * (1.0F - this.backScaleY)), (int)(getMeasuredWidth() * this.backScaleX), getMeasuredHeight());
      }
      for (;;)
      {
        this.backgroundDrawable.draw(paramCanvas);
        return;
        label75:
        this.backgroundDrawable.setBounds(0, 0, (int)(getMeasuredWidth() * this.backScaleX), (int)(getMeasuredHeight() * this.backScaleY));
      }
    }
    
    public void removeInnerViews()
    {
      this.linearLayout.removeAllViews();
    }
    
    public void scrollToTop()
    {
      if (this.scrollView != null) {
        this.scrollView.scrollTo(0, 0);
      }
    }
    
    public void setAnimationEnabled(boolean paramBoolean)
    {
      this.animationEnabled = paramBoolean;
    }
    
    @Keep
    public void setBackAlpha(int paramInt)
    {
      this.backAlpha = paramInt;
    }
    
    @Keep
    public void setBackScaleX(float paramFloat)
    {
      this.backScaleX = paramFloat;
      invalidate();
    }
    
    @Keep
    public void setBackScaleY(float paramFloat)
    {
      this.backScaleY = paramFloat;
      int m;
      int j;
      View localView;
      if (this.animationEnabled)
      {
        m = getItemsCount();
        j = 0;
        i = 0;
        if (i < m)
        {
          if (getItemAt(i).getVisibility() == 0) {}
          for (int k = 1;; k = 0)
          {
            j += k;
            i += 1;
            break;
          }
        }
        j = getMeasuredHeight() - AndroidUtilities.dp(16.0F);
        if (!this.showedFromBotton) {
          break label180;
        }
        i = this.lastStartedChild;
        if (i >= 0)
        {
          localView = getItemAt(i);
          if (localView.getVisibility() == 0) {}
        }
      }
      Integer localInteger;
      for (;;)
      {
        i -= 1;
        break;
        localInteger = (Integer)this.positions.get(localView);
        if ((localInteger != null) && (j - (localInteger.intValue() * AndroidUtilities.dp(48.0F) + AndroidUtilities.dp(32.0F)) > j * paramFloat))
        {
          invalidate();
          return;
        }
        this.lastStartedChild = (i - 1);
        startChildAnimation(localView);
      }
      label180:
      int i = this.lastStartedChild;
      label185:
      if (i < m)
      {
        localView = getItemAt(i);
        if (localView.getVisibility() == 0) {
          break label213;
        }
      }
      for (;;)
      {
        i += 1;
        break label185;
        break;
        label213:
        localInteger = (Integer)this.positions.get(localView);
        if ((localInteger != null) && ((localInteger.intValue() + 1) * AndroidUtilities.dp(48.0F) - AndroidUtilities.dp(24.0F) > j * paramFloat)) {
          break;
        }
        this.lastStartedChild = (i + 1);
        startChildAnimation(localView);
      }
    }
    
    public void setBackgroundDrawable(Drawable paramDrawable)
    {
      this.backgroundDrawable = paramDrawable;
    }
    
    public void setDispatchKeyEventListener(ActionBarPopupWindow.OnDispatchKeyEventListener paramOnDispatchKeyEventListener)
    {
      this.mOnDispatchKeyEventListener = paramOnDispatchKeyEventListener;
    }
    
    public void setShowedFromBotton(boolean paramBoolean)
    {
      this.showedFromBotton = paramBoolean;
    }
  }
  
  public static abstract interface OnDispatchKeyEventListener
  {
    public abstract void onDispatchKeyEvent(KeyEvent paramKeyEvent);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ActionBar/ActionBarPopupWindow.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */