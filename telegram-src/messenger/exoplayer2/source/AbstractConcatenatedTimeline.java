package org.telegram.messenger.exoplayer2.source;

import android.util.Pair;
import org.telegram.messenger.exoplayer2.Timeline;
import org.telegram.messenger.exoplayer2.Timeline.Period;
import org.telegram.messenger.exoplayer2.Timeline.Window;

abstract class AbstractConcatenatedTimeline
  extends Timeline
{
  private final int childCount;
  private final ShuffleOrder shuffleOrder;
  
  public AbstractConcatenatedTimeline(ShuffleOrder paramShuffleOrder)
  {
    this.shuffleOrder = paramShuffleOrder;
    this.childCount = paramShuffleOrder.getLength();
  }
  
  private int getNextChildIndex(int paramInt, boolean paramBoolean)
  {
    if (paramBoolean) {
      return this.shuffleOrder.getNextIndex(paramInt);
    }
    if (paramInt < this.childCount - 1) {
      return paramInt + 1;
    }
    return -1;
  }
  
  private int getPreviousChildIndex(int paramInt, boolean paramBoolean)
  {
    if (paramBoolean) {
      return this.shuffleOrder.getPreviousIndex(paramInt);
    }
    if (paramInt > 0) {
      return paramInt - 1;
    }
    return -1;
  }
  
  protected abstract int getChildIndexByChildUid(Object paramObject);
  
  protected abstract int getChildIndexByPeriodIndex(int paramInt);
  
  protected abstract int getChildIndexByWindowIndex(int paramInt);
  
  protected abstract Object getChildUidByChildIndex(int paramInt);
  
  protected abstract int getFirstPeriodIndexByChildIndex(int paramInt);
  
  public int getFirstWindowIndex(boolean paramBoolean)
  {
    if (this.childCount == 0) {
      return -1;
    }
    int i;
    if (paramBoolean) {
      i = this.shuffleOrder.getFirstIndex();
    }
    while (getTimelineByChildIndex(i).isEmpty())
    {
      int j = getNextChildIndex(i, paramBoolean);
      i = j;
      if (j == -1)
      {
        return -1;
        i = 0;
      }
    }
    return getFirstWindowIndexByChildIndex(i) + getTimelineByChildIndex(i).getFirstWindowIndex(paramBoolean);
  }
  
  protected abstract int getFirstWindowIndexByChildIndex(int paramInt);
  
  public final int getIndexOfPeriod(Object paramObject)
  {
    if (!(paramObject instanceof Pair)) {}
    int i;
    int j;
    do
    {
      Object localObject;
      do
      {
        return -1;
        localObject = (Pair)paramObject;
        paramObject = ((Pair)localObject).first;
        localObject = ((Pair)localObject).second;
        i = getChildIndexByChildUid(paramObject);
      } while (i == -1);
      j = getTimelineByChildIndex(i).getIndexOfPeriod(localObject);
    } while (j == -1);
    return getFirstPeriodIndexByChildIndex(i) + j;
  }
  
  public int getLastWindowIndex(boolean paramBoolean)
  {
    if (this.childCount == 0) {
      return -1;
    }
    int i;
    if (paramBoolean) {
      i = this.shuffleOrder.getLastIndex();
    }
    while (getTimelineByChildIndex(i).isEmpty())
    {
      int j = getPreviousChildIndex(i, paramBoolean);
      i = j;
      if (j == -1)
      {
        return -1;
        i = this.childCount - 1;
      }
    }
    return getFirstWindowIndexByChildIndex(i) + getTimelineByChildIndex(i).getLastWindowIndex(paramBoolean);
  }
  
  public int getNextWindowIndex(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    int j = getChildIndexByWindowIndex(paramInt1);
    int k = getFirstWindowIndexByChildIndex(j);
    Timeline localTimeline = getTimelineByChildIndex(j);
    if (paramInt2 == 2) {}
    for (int i = 0;; i = paramInt2)
    {
      paramInt1 = localTimeline.getNextWindowIndex(paramInt1 - k, i, paramBoolean);
      if (paramInt1 == -1) {
        break;
      }
      return k + paramInt1;
    }
    for (paramInt1 = getNextChildIndex(j, paramBoolean); (paramInt1 != -1) && (getTimelineByChildIndex(paramInt1).isEmpty()); paramInt1 = getNextChildIndex(paramInt1, paramBoolean)) {}
    if (paramInt1 != -1) {
      return getFirstWindowIndexByChildIndex(paramInt1) + getTimelineByChildIndex(paramInt1).getFirstWindowIndex(paramBoolean);
    }
    if (paramInt2 == 2) {
      return getFirstWindowIndex(paramBoolean);
    }
    return -1;
  }
  
  public final Timeline.Period getPeriod(int paramInt, Timeline.Period paramPeriod, boolean paramBoolean)
  {
    int i = getChildIndexByPeriodIndex(paramInt);
    int j = getFirstWindowIndexByChildIndex(i);
    int k = getFirstPeriodIndexByChildIndex(i);
    getTimelineByChildIndex(i).getPeriod(paramInt - k, paramPeriod, paramBoolean);
    paramPeriod.windowIndex += j;
    if (paramBoolean) {
      paramPeriod.uid = Pair.create(getChildUidByChildIndex(i), paramPeriod.uid);
    }
    return paramPeriod;
  }
  
  public int getPreviousWindowIndex(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    int j = getChildIndexByWindowIndex(paramInt1);
    int k = getFirstWindowIndexByChildIndex(j);
    Timeline localTimeline = getTimelineByChildIndex(j);
    if (paramInt2 == 2) {}
    for (int i = 0;; i = paramInt2)
    {
      paramInt1 = localTimeline.getPreviousWindowIndex(paramInt1 - k, i, paramBoolean);
      if (paramInt1 == -1) {
        break;
      }
      return k + paramInt1;
    }
    for (paramInt1 = getPreviousChildIndex(j, paramBoolean); (paramInt1 != -1) && (getTimelineByChildIndex(paramInt1).isEmpty()); paramInt1 = getPreviousChildIndex(paramInt1, paramBoolean)) {}
    if (paramInt1 != -1) {
      return getFirstWindowIndexByChildIndex(paramInt1) + getTimelineByChildIndex(paramInt1).getLastWindowIndex(paramBoolean);
    }
    if (paramInt2 == 2) {
      return getLastWindowIndex(paramBoolean);
    }
    return -1;
  }
  
  protected abstract Timeline getTimelineByChildIndex(int paramInt);
  
  public final Timeline.Window getWindow(int paramInt, Timeline.Window paramWindow, boolean paramBoolean, long paramLong)
  {
    int i = getChildIndexByWindowIndex(paramInt);
    int j = getFirstWindowIndexByChildIndex(i);
    int k = getFirstPeriodIndexByChildIndex(i);
    getTimelineByChildIndex(i).getWindow(paramInt - j, paramWindow, paramBoolean, paramLong);
    paramWindow.firstPeriodIndex += k;
    paramWindow.lastPeriodIndex += k;
    return paramWindow;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/AbstractConcatenatedTimeline.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */