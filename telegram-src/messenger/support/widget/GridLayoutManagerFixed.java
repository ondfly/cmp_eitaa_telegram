package org.telegram.messenger.support.widget;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.View.MeasureSpec;
import java.util.ArrayList;
import java.util.Arrays;

public class GridLayoutManagerFixed
  extends GridLayoutManager
{
  private ArrayList<View> additionalViews = new ArrayList(4);
  private boolean canScrollVertically = true;
  
  public GridLayoutManagerFixed(Context paramContext, int paramInt)
  {
    super(paramContext, paramInt);
  }
  
  public GridLayoutManagerFixed(Context paramContext, int paramInt1, int paramInt2, boolean paramBoolean)
  {
    super(paramContext, paramInt1, paramInt2, paramBoolean);
  }
  
  protected int[] calculateItemBorders(int[] paramArrayOfInt, int paramInt1, int paramInt2)
  {
    int[] arrayOfInt;
    if ((paramArrayOfInt != null) && (paramArrayOfInt.length == paramInt1 + 1))
    {
      arrayOfInt = paramArrayOfInt;
      if (paramArrayOfInt[(paramArrayOfInt.length - 1)] == paramInt2) {}
    }
    else
    {
      arrayOfInt = new int[paramInt1 + 1];
    }
    arrayOfInt[0] = 0;
    int i = 1;
    while (i <= paramInt1)
    {
      arrayOfInt[i] = ((int)Math.ceil(i / paramInt1 * paramInt2));
      i += 1;
    }
    return arrayOfInt;
  }
  
  public boolean canScrollVertically()
  {
    return this.canScrollVertically;
  }
  
  protected boolean hasSiblingChild(int paramInt)
  {
    return false;
  }
  
  void layoutChunk(RecyclerView.Recycler paramRecycler, RecyclerView.State paramState, LinearLayoutManager.LayoutState paramLayoutState, LinearLayoutManager.LayoutChunkResult paramLayoutChunkResult)
  {
    int i4 = this.mOrientationHelper.getModeInOther();
    boolean bool1;
    int j;
    int k;
    if (paramLayoutState.mItemDirection == 1)
    {
      bool1 = true;
      j = 1;
      paramLayoutChunkResult.mConsumed = 0;
      k = paramLayoutState.mCurrentPosition;
      i = j;
      if (paramLayoutState.mLayoutDirection == -1) {
        break label235;
      }
      i = j;
      if (!hasSiblingChild(paramLayoutState.mCurrentPosition)) {
        break label235;
      }
      i = j;
      if (findViewByPosition(paramLayoutState.mCurrentPosition + 1) != null) {
        break label235;
      }
      if (!hasSiblingChild(paramLayoutState.mCurrentPosition + 1)) {
        break label212;
      }
    }
    int m;
    View localView;
    int n;
    label212:
    for (paramLayoutState.mCurrentPosition += 3;; paramLayoutState.mCurrentPosition += 2)
    {
      m = paramLayoutState.mCurrentPosition;
      i = paramLayoutState.mCurrentPosition;
      while (i > k)
      {
        localView = paramLayoutState.next(paramRecycler);
        this.additionalViews.add(localView);
        if (i != m)
        {
          calculateItemDecorationsForChild(localView, this.mDecorInsets);
          measureChild(localView, i4, false);
          n = this.mOrientationHelper.getDecoratedMeasurement(localView);
          paramLayoutState.mOffset -= n;
          paramLayoutState.mAvailable += n;
        }
        i -= 1;
      }
      bool1 = false;
      break;
    }
    paramLayoutState.mCurrentPosition = m;
    int i = j;
    for (;;)
    {
      label235:
      int i3;
      int i1;
      if (i != 0)
      {
        m = 0;
        j = 0;
        k = this.mSpanCount;
        if (this.additionalViews.isEmpty()) {
          break label333;
        }
        i = 1;
        n = paramLayoutState.mCurrentPosition;
        if ((m < this.mSpanCount) && (paramLayoutState.hasMore(paramState)) && (k > 0))
        {
          i3 = paramLayoutState.mCurrentPosition;
          i1 = getSpanSize(paramRecycler, paramState, i3);
          n = k - i1;
          if (n >= 0) {
            break label339;
          }
        }
      }
      label333:
      label339:
      int i2;
      label492:
      for (;;)
      {
        if (m != 0) {
          break label494;
        }
        paramLayoutChunkResult.mFinished = true;
        return;
        i = 0;
        break;
        if (!this.additionalViews.isEmpty())
        {
          localView = (View)this.additionalViews.get(0);
          this.additionalViews.remove(0);
          paramLayoutState.mCurrentPosition -= 1;
        }
        for (;;)
        {
          if (localView == null) {
            break label492;
          }
          i1 = j + i1;
          this.mSet[m] = localView;
          i2 = m + 1;
          m = i2;
          j = i1;
          k = n;
          if (paramLayoutState.mLayoutDirection != -1) {
            break;
          }
          m = i2;
          j = i1;
          k = n;
          if (n > 0) {
            break;
          }
          m = i2;
          j = i1;
          k = n;
          if (!hasSiblingChild(i3)) {
            break;
          }
          i = 1;
          m = i2;
          j = i1;
          k = n;
          break;
          localView = paramLayoutState.next(paramRecycler);
        }
      }
      label494:
      n = 0;
      float f1 = 0.0F;
      assignSpans(paramRecycler, paramState, m, j, bool1);
      j = 0;
      GridLayoutManager.LayoutParams localLayoutParams;
      if (j < m)
      {
        localView = this.mSet[j];
        if (paramLayoutState.mScrapList == null) {
          if (bool1) {
            addView(localView);
          }
        }
        for (;;)
        {
          calculateItemDecorationsForChild(localView, this.mDecorInsets);
          measureChild(localView, i4, false);
          i1 = this.mOrientationHelper.getDecoratedMeasurement(localView);
          k = n;
          if (i1 > n) {
            k = i1;
          }
          localLayoutParams = (GridLayoutManager.LayoutParams)localView.getLayoutParams();
          float f3 = 1.0F * this.mOrientationHelper.getDecoratedMeasurementInOther(localView) / localLayoutParams.mSpanSize;
          float f2 = f1;
          if (f3 > f1) {
            f2 = f3;
          }
          j += 1;
          n = k;
          f1 = f2;
          break;
          addView(localView, 0);
          continue;
          if (bool1) {
            addDisappearingView(localView);
          } else {
            addDisappearingView(localView, 0);
          }
        }
      }
      j = 0;
      int i5;
      while (j < m)
      {
        localView = this.mSet[j];
        if (this.mOrientationHelper.getDecoratedMeasurement(localView) != n)
        {
          localLayoutParams = (GridLayoutManager.LayoutParams)localView.getLayoutParams();
          Rect localRect = localLayoutParams.mDecorInsets;
          k = localRect.top;
          i1 = localRect.bottom;
          i2 = localLayoutParams.topMargin;
          i3 = localLayoutParams.bottomMargin;
          i5 = localRect.left;
          int i6 = localRect.right;
          int i7 = localLayoutParams.leftMargin;
          int i8 = localLayoutParams.rightMargin;
          measureChildWithDecorationsAndMargin(localView, getChildMeasureSpec(this.mCachedBorders[localLayoutParams.mSpanSize], 1073741824, i5 + i6 + i7 + i8, localLayoutParams.width, false), View.MeasureSpec.makeMeasureSpec(n - (k + i1 + i2 + i3), 1073741824), true);
        }
        j += 1;
      }
      boolean bool2 = shouldLayoutChildFromOpositeSide(this.mSet[0]);
      if (((bool2) && (paramLayoutState.mLayoutDirection == -1)) || ((!bool2) && (paramLayoutState.mLayoutDirection == 1)))
      {
        if (paramLayoutState.mLayoutDirection == -1)
        {
          i2 = paramLayoutState.mOffset - paramLayoutChunkResult.mConsumed;
          i1 = i2 - n;
        }
        for (j = 0;; j = getWidth())
        {
          m -= 1;
          while (m >= 0)
          {
            localView = this.mSet[m];
            localLayoutParams = (GridLayoutManager.LayoutParams)localView.getLayoutParams();
            i3 = this.mOrientationHelper.getDecoratedMeasurementInOther(localView);
            k = j;
            if (paramLayoutState.mLayoutDirection == 1) {
              k = j - i3;
            }
            layoutDecoratedWithMargins(localView, k, i1, k + i3, i2);
            j = k;
            if (paramLayoutState.mLayoutDirection == -1) {
              j = k + i3;
            }
            if ((localLayoutParams.isItemRemoved()) || (localLayoutParams.isItemChanged())) {
              paramLayoutChunkResult.mIgnoreConsumed = true;
            }
            paramLayoutChunkResult.mFocusable |= localView.hasFocusable();
            m -= 1;
          }
          i1 = paramLayoutState.mOffset + paramLayoutChunkResult.mConsumed;
          i2 = i1 + n;
        }
      }
      if (paramLayoutState.mLayoutDirection == -1)
      {
        i2 = paramLayoutState.mOffset - paramLayoutChunkResult.mConsumed;
        i1 = i2 - n;
      }
      for (j = getWidth();; j = 0)
      {
        i3 = 0;
        while (i3 < m)
        {
          localView = this.mSet[i3];
          localLayoutParams = (GridLayoutManager.LayoutParams)localView.getLayoutParams();
          i5 = this.mOrientationHelper.getDecoratedMeasurementInOther(localView);
          k = j;
          if (paramLayoutState.mLayoutDirection == -1) {
            k = j - i5;
          }
          layoutDecoratedWithMargins(localView, k, i1, k + i5, i2);
          j = k;
          if (paramLayoutState.mLayoutDirection != -1) {
            j = k + i5;
          }
          if ((localLayoutParams.isItemRemoved()) || (localLayoutParams.isItemChanged())) {
            paramLayoutChunkResult.mIgnoreConsumed = true;
          }
          paramLayoutChunkResult.mFocusable |= localView.hasFocusable();
          i3 += 1;
        }
        i1 = paramLayoutState.mOffset + paramLayoutChunkResult.mConsumed;
        i2 = i1 + n;
      }
      paramLayoutChunkResult.mConsumed += n;
      Arrays.fill(this.mSet, null);
    }
  }
  
  protected void measureChild(View paramView, int paramInt, boolean paramBoolean)
  {
    GridLayoutManager.LayoutParams localLayoutParams = (GridLayoutManager.LayoutParams)paramView.getLayoutParams();
    Rect localRect = localLayoutParams.mDecorInsets;
    int i = localRect.top;
    int j = localRect.bottom;
    int k = localLayoutParams.topMargin;
    int m = localLayoutParams.bottomMargin;
    int n = localRect.left;
    int i1 = localRect.right;
    int i2 = localLayoutParams.leftMargin;
    int i3 = localLayoutParams.rightMargin;
    measureChildWithDecorationsAndMargin(paramView, getChildMeasureSpec(this.mCachedBorders[localLayoutParams.mSpanSize], paramInt, n + i1 + i2 + i3, localLayoutParams.width, false), getChildMeasureSpec(this.mOrientationHelper.getTotalSpace(), getHeightMode(), i + j + k + m, localLayoutParams.height, true), paramBoolean);
  }
  
  protected void recycleViewsFromStart(RecyclerView.Recycler paramRecycler, int paramInt)
  {
    if (paramInt < 0) {}
    for (;;)
    {
      return;
      int j = getChildCount();
      int i;
      View localView;
      if (this.mShouldReverseLayout)
      {
        i = j - 1;
        while (i >= 0)
        {
          localView = getChildAt(i);
          RecyclerView.LayoutParams localLayoutParams = (RecyclerView.LayoutParams)localView.getLayoutParams();
          if ((localView.getBottom() + localLayoutParams.bottomMargin > paramInt) || (localView.getTop() + localView.getHeight() > paramInt))
          {
            recycleChildren(paramRecycler, j - 1, i);
            return;
          }
          i -= 1;
        }
      }
      else
      {
        i = 0;
        while (i < j)
        {
          localView = getChildAt(i);
          if ((this.mOrientationHelper.getDecoratedEnd(localView) > paramInt) || (this.mOrientationHelper.getTransformedEndWithDecoration(localView) > paramInt))
          {
            recycleChildren(paramRecycler, 0, i);
            return;
          }
          i += 1;
        }
      }
    }
  }
  
  public void setCanScrollVertically(boolean paramBoolean)
  {
    this.canScrollVertically = paramBoolean;
  }
  
  public boolean shouldLayoutChildFromOpositeSide(View paramView)
  {
    return false;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/support/widget/GridLayoutManagerFixed.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */