package org.telegram.messenger.support.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class LinearSmoothScrollerMiddle
  extends RecyclerView.SmoothScroller
{
  private static final float MILLISECONDS_PER_INCH = 25.0F;
  private static final float TARGET_SEEK_EXTRA_SCROLL_RATIO = 1.2F;
  private static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;
  private final float MILLISECONDS_PER_PX;
  protected final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator(1.5F);
  protected int mInterimTargetDx = 0;
  protected int mInterimTargetDy = 0;
  protected final LinearInterpolator mLinearInterpolator = new LinearInterpolator();
  protected PointF mTargetVector;
  
  public LinearSmoothScrollerMiddle(Context paramContext)
  {
    this.MILLISECONDS_PER_PX = (25.0F / paramContext.getResources().getDisplayMetrics().densityDpi);
  }
  
  private int clampApplyScroll(int paramInt1, int paramInt2)
  {
    int i = paramInt1 - paramInt2;
    paramInt2 = i;
    if (paramInt1 * i <= 0) {
      paramInt2 = 0;
    }
    return paramInt2;
  }
  
  public int calculateDyToMakeVisible(View paramView)
  {
    RecyclerView.LayoutManager localLayoutManager = getLayoutManager();
    if ((localLayoutManager == null) || (!localLayoutManager.canScrollVertically())) {
      i = 0;
    }
    int k;
    int j;
    int n;
    int m;
    do
    {
      return i;
      RecyclerView.LayoutParams localLayoutParams = (RecyclerView.LayoutParams)paramView.getLayoutParams();
      i = localLayoutManager.getDecoratedTop(paramView) - localLayoutParams.topMargin;
      k = localLayoutManager.getDecoratedBottom(paramView) + localLayoutParams.bottomMargin;
      j = localLayoutManager.getPaddingTop();
      n = localLayoutManager.getHeight() - localLayoutManager.getPaddingBottom();
      if ((i > j) && (k < n)) {
        return 0;
      }
      m = k - i;
      n = (n - j - m) / 2;
      j = n - i;
      i = j;
    } while (j > 0);
    int i = n + m - k;
    if (i < 0) {
      return i;
    }
    return 0;
  }
  
  protected int calculateTimeForDeceleration(int paramInt)
  {
    return (int)Math.ceil(calculateTimeForScrolling(paramInt) / 0.3356D);
  }
  
  protected int calculateTimeForScrolling(int paramInt)
  {
    return (int)Math.ceil(Math.abs(paramInt) * this.MILLISECONDS_PER_PX);
  }
  
  public PointF computeScrollVectorForPosition(int paramInt)
  {
    RecyclerView.LayoutManager localLayoutManager = getLayoutManager();
    if ((localLayoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
      return ((RecyclerView.SmoothScroller.ScrollVectorProvider)localLayoutManager).computeScrollVectorForPosition(paramInt);
    }
    return null;
  }
  
  protected void onSeekTargetStep(int paramInt1, int paramInt2, RecyclerView.State paramState, RecyclerView.SmoothScroller.Action paramAction)
  {
    if (getChildCount() == 0) {
      stop();
    }
    do
    {
      return;
      this.mInterimTargetDx = clampApplyScroll(this.mInterimTargetDx, paramInt1);
      this.mInterimTargetDy = clampApplyScroll(this.mInterimTargetDy, paramInt2);
    } while ((this.mInterimTargetDx != 0) || (this.mInterimTargetDy != 0));
    updateActionForInterimTarget(paramAction);
  }
  
  protected void onStart() {}
  
  protected void onStop()
  {
    this.mInterimTargetDy = 0;
    this.mInterimTargetDx = 0;
    this.mTargetVector = null;
  }
  
  protected void onTargetFound(View paramView, RecyclerView.State paramState, RecyclerView.SmoothScroller.Action paramAction)
  {
    int i = calculateDyToMakeVisible(paramView);
    int j = calculateTimeForDeceleration(i);
    if (j > 0) {
      paramAction.update(0, -i, Math.max(400, j), this.mDecelerateInterpolator);
    }
  }
  
  protected void updateActionForInterimTarget(RecyclerView.SmoothScroller.Action paramAction)
  {
    PointF localPointF = computeScrollVectorForPosition(getTargetPosition());
    if ((localPointF == null) || ((localPointF.x == 0.0F) && (localPointF.y == 0.0F)))
    {
      paramAction.jumpTo(getTargetPosition());
      stop();
      return;
    }
    normalize(localPointF);
    this.mTargetVector = localPointF;
    this.mInterimTargetDx = ((int)(localPointF.x * 10000.0F));
    this.mInterimTargetDy = ((int)(localPointF.y * 10000.0F));
    int i = calculateTimeForScrolling(10000);
    paramAction.update((int)(this.mInterimTargetDx * 1.2F), (int)(this.mInterimTargetDy * 1.2F), (int)(i * 1.2F), this.mLinearInterpolator);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/support/widget/LinearSmoothScrollerMiddle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */