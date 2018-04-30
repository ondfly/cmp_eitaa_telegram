package org.telegram.messenger.support.widget;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.View;

public class PagerSnapHelper
  extends SnapHelper
{
  private static final int MAX_SCROLL_ON_FLING_DURATION = 100;
  private OrientationHelper mHorizontalHelper;
  private OrientationHelper mVerticalHelper;
  
  private int distanceToCenter(RecyclerView.LayoutManager paramLayoutManager, View paramView, OrientationHelper paramOrientationHelper)
  {
    int j = paramOrientationHelper.getDecoratedStart(paramView);
    int k = paramOrientationHelper.getDecoratedMeasurement(paramView) / 2;
    if (paramLayoutManager.getClipToPadding()) {}
    for (int i = paramOrientationHelper.getStartAfterPadding() + paramOrientationHelper.getTotalSpace() / 2;; i = paramOrientationHelper.getEnd() / 2) {
      return j + k - i;
    }
  }
  
  private View findCenterView(RecyclerView.LayoutManager paramLayoutManager, OrientationHelper paramOrientationHelper)
  {
    int i1 = paramLayoutManager.getChildCount();
    Object localObject2;
    if (i1 == 0)
    {
      localObject2 = null;
      return (View)localObject2;
    }
    Object localObject1 = null;
    if (paramLayoutManager.getClipToPadding()) {}
    for (int i = paramOrientationHelper.getStartAfterPadding() + paramOrientationHelper.getTotalSpace() / 2;; i = paramOrientationHelper.getEnd() / 2)
    {
      int k = Integer.MAX_VALUE;
      int j = 0;
      for (;;)
      {
        localObject2 = localObject1;
        if (j >= i1) {
          break;
        }
        localObject2 = paramLayoutManager.getChildAt(j);
        int n = Math.abs(paramOrientationHelper.getDecoratedStart((View)localObject2) + paramOrientationHelper.getDecoratedMeasurement((View)localObject2) / 2 - i);
        int m = k;
        if (n < k)
        {
          m = n;
          localObject1 = localObject2;
        }
        j += 1;
        k = m;
      }
    }
  }
  
  private View findStartView(RecyclerView.LayoutManager paramLayoutManager, OrientationHelper paramOrientationHelper)
  {
    int n = paramLayoutManager.getChildCount();
    Object localObject2;
    if (n == 0)
    {
      localObject2 = null;
      return (View)localObject2;
    }
    Object localObject1 = null;
    int j = Integer.MAX_VALUE;
    int i = 0;
    for (;;)
    {
      localObject2 = localObject1;
      if (i >= n) {
        break;
      }
      localObject2 = paramLayoutManager.getChildAt(i);
      int m = paramOrientationHelper.getDecoratedStart((View)localObject2);
      int k = j;
      if (m < j)
      {
        k = m;
        localObject1 = localObject2;
      }
      i += 1;
      j = k;
    }
  }
  
  private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager paramLayoutManager)
  {
    if ((this.mHorizontalHelper == null) || (this.mHorizontalHelper.mLayoutManager != paramLayoutManager)) {
      this.mHorizontalHelper = OrientationHelper.createHorizontalHelper(paramLayoutManager);
    }
    return this.mHorizontalHelper;
  }
  
  private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager paramLayoutManager)
  {
    if ((this.mVerticalHelper == null) || (this.mVerticalHelper.mLayoutManager != paramLayoutManager)) {
      this.mVerticalHelper = OrientationHelper.createVerticalHelper(paramLayoutManager);
    }
    return this.mVerticalHelper;
  }
  
  public int[] calculateDistanceToFinalSnap(RecyclerView.LayoutManager paramLayoutManager, View paramView)
  {
    int[] arrayOfInt = new int[2];
    if (paramLayoutManager.canScrollHorizontally()) {
      arrayOfInt[0] = distanceToCenter(paramLayoutManager, paramView, getHorizontalHelper(paramLayoutManager));
    }
    while (paramLayoutManager.canScrollVertically())
    {
      arrayOfInt[1] = distanceToCenter(paramLayoutManager, paramView, getVerticalHelper(paramLayoutManager));
      return arrayOfInt;
      arrayOfInt[0] = 0;
    }
    arrayOfInt[1] = 0;
    return arrayOfInt;
  }
  
  protected LinearSmoothScroller createSnapScroller(RecyclerView.LayoutManager paramLayoutManager)
  {
    if (!(paramLayoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
      return null;
    }
    new LinearSmoothScroller(this.mRecyclerView.getContext())
    {
      protected float calculateSpeedPerPixel(DisplayMetrics paramAnonymousDisplayMetrics)
      {
        return 100.0F / paramAnonymousDisplayMetrics.densityDpi;
      }
      
      protected int calculateTimeForScrolling(int paramAnonymousInt)
      {
        return Math.min(100, super.calculateTimeForScrolling(paramAnonymousInt));
      }
      
      protected void onTargetFound(View paramAnonymousView, RecyclerView.State paramAnonymousState, RecyclerView.SmoothScroller.Action paramAnonymousAction)
      {
        paramAnonymousView = PagerSnapHelper.this.calculateDistanceToFinalSnap(PagerSnapHelper.this.mRecyclerView.getLayoutManager(), paramAnonymousView);
        int i = paramAnonymousView[0];
        int j = paramAnonymousView[1];
        int k = calculateTimeForDeceleration(Math.max(Math.abs(i), Math.abs(j)));
        if (k > 0) {
          paramAnonymousAction.update(i, j, k, this.mDecelerateInterpolator);
        }
      }
    };
  }
  
  public View findSnapView(RecyclerView.LayoutManager paramLayoutManager)
  {
    if (paramLayoutManager.canScrollVertically()) {
      return findCenterView(paramLayoutManager, getVerticalHelper(paramLayoutManager));
    }
    if (paramLayoutManager.canScrollHorizontally()) {
      return findCenterView(paramLayoutManager, getHorizontalHelper(paramLayoutManager));
    }
    return null;
  }
  
  public int findTargetSnapPosition(RecyclerView.LayoutManager paramLayoutManager, int paramInt1, int paramInt2)
  {
    int k = paramLayoutManager.getItemCount();
    if (k == 0) {
      paramInt2 = -1;
    }
    int i;
    label95:
    label184:
    label189:
    do
    {
      return paramInt2;
      View localView = null;
      if (paramLayoutManager.canScrollVertically()) {
        localView = findStartView(paramLayoutManager, getVerticalHelper(paramLayoutManager));
      }
      while (localView == null)
      {
        return -1;
        if (paramLayoutManager.canScrollHorizontally()) {
          localView = findStartView(paramLayoutManager, getHorizontalHelper(paramLayoutManager));
        }
      }
      i = paramLayoutManager.getPosition(localView);
      if (i == -1) {
        return -1;
      }
      if (paramLayoutManager.canScrollHorizontally()) {
        if (paramInt1 > 0)
        {
          paramInt1 = 1;
          int j = 0;
          paramInt2 = j;
          if ((paramLayoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider))
          {
            paramLayoutManager = ((RecyclerView.SmoothScroller.ScrollVectorProvider)paramLayoutManager).computeScrollVectorForPosition(k - 1);
            paramInt2 = j;
            if (paramLayoutManager != null) {
              if ((paramLayoutManager.x >= 0.0F) && (paramLayoutManager.y >= 0.0F)) {
                break label184;
              }
            }
          }
        }
      }
      for (paramInt2 = 1;; paramInt2 = 0)
      {
        if (paramInt2 == 0) {
          break label189;
        }
        paramInt2 = i;
        if (paramInt1 == 0) {
          break;
        }
        return i - 1;
        paramInt1 = 0;
        break label95;
        if (paramInt2 > 0) {}
        for (paramInt1 = 1;; paramInt1 = 0) {
          break;
        }
      }
      paramInt2 = i;
    } while (paramInt1 == 0);
    return i + 1;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/support/widget/PagerSnapHelper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */