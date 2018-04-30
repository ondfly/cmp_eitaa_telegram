package org.telegram.messenger.exoplayer2;

import android.util.Pair;
import org.telegram.messenger.exoplayer2.source.ads.AdPlaybackState;
import org.telegram.messenger.exoplayer2.source.ads.AdPlaybackState.AdGroup;
import org.telegram.messenger.exoplayer2.util.Assertions;

public abstract class Timeline
{
  public static final Timeline EMPTY = new Timeline()
  {
    public int getIndexOfPeriod(Object paramAnonymousObject)
    {
      return -1;
    }
    
    public Timeline.Period getPeriod(int paramAnonymousInt, Timeline.Period paramAnonymousPeriod, boolean paramAnonymousBoolean)
    {
      throw new IndexOutOfBoundsException();
    }
    
    public int getPeriodCount()
    {
      return 0;
    }
    
    public Timeline.Window getWindow(int paramAnonymousInt, Timeline.Window paramAnonymousWindow, boolean paramAnonymousBoolean, long paramAnonymousLong)
    {
      throw new IndexOutOfBoundsException();
    }
    
    public int getWindowCount()
    {
      return 0;
    }
  };
  
  public int getFirstWindowIndex(boolean paramBoolean)
  {
    if (isEmpty()) {
      return -1;
    }
    return 0;
  }
  
  public abstract int getIndexOfPeriod(Object paramObject);
  
  public int getLastWindowIndex(boolean paramBoolean)
  {
    if (isEmpty()) {
      return -1;
    }
    return getWindowCount() - 1;
  }
  
  public final int getNextPeriodIndex(int paramInt1, Period paramPeriod, Window paramWindow, int paramInt2, boolean paramBoolean)
  {
    int i = getPeriod(paramInt1, paramPeriod).windowIndex;
    if (getWindow(i, paramWindow).lastPeriodIndex == paramInt1)
    {
      paramInt1 = getNextWindowIndex(i, paramInt2, paramBoolean);
      if (paramInt1 == -1) {
        return -1;
      }
      return getWindow(paramInt1, paramWindow).firstPeriodIndex;
    }
    return paramInt1 + 1;
  }
  
  public int getNextWindowIndex(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    switch (paramInt2)
    {
    default: 
      throw new IllegalStateException();
    case 0: 
      if (paramInt1 == getLastWindowIndex(paramBoolean)) {
        return -1;
      }
      return paramInt1 + 1;
    case 1: 
      return paramInt1;
    }
    if (paramInt1 == getLastWindowIndex(paramBoolean)) {
      return getFirstWindowIndex(paramBoolean);
    }
    return paramInt1 + 1;
  }
  
  public final Period getPeriod(int paramInt, Period paramPeriod)
  {
    return getPeriod(paramInt, paramPeriod, false);
  }
  
  public abstract Period getPeriod(int paramInt, Period paramPeriod, boolean paramBoolean);
  
  public abstract int getPeriodCount();
  
  public final Pair<Integer, Long> getPeriodPosition(Window paramWindow, Period paramPeriod, int paramInt, long paramLong)
  {
    return getPeriodPosition(paramWindow, paramPeriod, paramInt, paramLong, 0L);
  }
  
  public final Pair<Integer, Long> getPeriodPosition(Window paramWindow, Period paramPeriod, int paramInt, long paramLong1, long paramLong2)
  {
    Assertions.checkIndex(paramInt, 0, getWindowCount());
    getWindow(paramInt, paramWindow, false, paramLong2);
    paramLong2 = paramLong1;
    if (paramLong1 == -9223372036854775807L)
    {
      paramLong1 = paramWindow.getDefaultPositionUs();
      paramLong2 = paramLong1;
      if (paramLong1 == -9223372036854775807L) {
        return null;
      }
    }
    paramInt = paramWindow.firstPeriodIndex;
    paramLong2 = paramWindow.getPositionInFirstPeriodUs() + paramLong2;
    for (paramLong1 = getPeriod(paramInt, paramPeriod).getDurationUs(); (paramLong1 != -9223372036854775807L) && (paramLong2 >= paramLong1) && (paramInt < paramWindow.lastPeriodIndex); paramLong1 = getPeriod(paramInt, paramPeriod).getDurationUs())
    {
      paramLong2 -= paramLong1;
      paramInt += 1;
    }
    return Pair.create(Integer.valueOf(paramInt), Long.valueOf(paramLong2));
  }
  
  public int getPreviousWindowIndex(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    switch (paramInt2)
    {
    default: 
      throw new IllegalStateException();
    case 0: 
      if (paramInt1 == getFirstWindowIndex(paramBoolean)) {
        return -1;
      }
      return paramInt1 - 1;
    case 1: 
      return paramInt1;
    }
    if (paramInt1 == getFirstWindowIndex(paramBoolean)) {
      return getLastWindowIndex(paramBoolean);
    }
    return paramInt1 - 1;
  }
  
  public final Window getWindow(int paramInt, Window paramWindow)
  {
    return getWindow(paramInt, paramWindow, false);
  }
  
  public final Window getWindow(int paramInt, Window paramWindow, boolean paramBoolean)
  {
    return getWindow(paramInt, paramWindow, paramBoolean, 0L);
  }
  
  public abstract Window getWindow(int paramInt, Window paramWindow, boolean paramBoolean, long paramLong);
  
  public abstract int getWindowCount();
  
  public final boolean isEmpty()
  {
    return getWindowCount() == 0;
  }
  
  public final boolean isLastPeriod(int paramInt1, Period paramPeriod, Window paramWindow, int paramInt2, boolean paramBoolean)
  {
    return getNextPeriodIndex(paramInt1, paramPeriod, paramWindow, paramInt2, paramBoolean) == -1;
  }
  
  public static final class Period
  {
    private AdPlaybackState adPlaybackState;
    public long durationUs;
    public Object id;
    private long positionInWindowUs;
    public Object uid;
    public int windowIndex;
    
    public int getAdCountInAdGroup(int paramInt)
    {
      return this.adPlaybackState.adGroups[paramInt].count;
    }
    
    public long getAdDurationUs(int paramInt1, int paramInt2)
    {
      return this.adPlaybackState.adGroups[paramInt1].durationsUs[paramInt2];
    }
    
    public int getAdGroupCount()
    {
      return this.adPlaybackState.adGroupCount;
    }
    
    public int getAdGroupIndexAfterPositionUs(long paramLong)
    {
      long[] arrayOfLong = this.adPlaybackState.adGroupTimesUs;
      if (arrayOfLong == null) {
        return -1;
      }
      int i = 0;
      while ((i < arrayOfLong.length) && (arrayOfLong[i] != Long.MIN_VALUE) && ((paramLong >= arrayOfLong[i]) || (hasPlayedAdGroup(i)))) {
        i += 1;
      }
      if (i < arrayOfLong.length) {}
      for (;;)
      {
        return i;
        i = -1;
      }
    }
    
    public int getAdGroupIndexForPositionUs(long paramLong)
    {
      long[] arrayOfLong = this.adPlaybackState.adGroupTimesUs;
      if (arrayOfLong == null) {
        return -1;
      }
      int i = arrayOfLong.length - 1;
      while ((i >= 0) && ((arrayOfLong[i] == Long.MIN_VALUE) || (arrayOfLong[i] > paramLong))) {
        i -= 1;
      }
      if ((i >= 0) && (!hasPlayedAdGroup(i))) {}
      for (;;)
      {
        return i;
        i = -1;
      }
    }
    
    public long getAdGroupTimeUs(int paramInt)
    {
      return this.adPlaybackState.adGroupTimesUs[paramInt];
    }
    
    public long getAdResumePositionUs()
    {
      return this.adPlaybackState.adResumePositionUs;
    }
    
    public long getDurationMs()
    {
      return C.usToMs(this.durationUs);
    }
    
    public long getDurationUs()
    {
      return this.durationUs;
    }
    
    public int getNextAdIndexToPlay(int paramInt)
    {
      return this.adPlaybackState.adGroups[paramInt].nextAdIndexToPlay;
    }
    
    public long getPositionInWindowMs()
    {
      return C.usToMs(this.positionInWindowUs);
    }
    
    public long getPositionInWindowUs()
    {
      return this.positionInWindowUs;
    }
    
    public boolean hasPlayedAdGroup(int paramInt)
    {
      AdPlaybackState.AdGroup localAdGroup = this.adPlaybackState.adGroups[paramInt];
      return localAdGroup.nextAdIndexToPlay == localAdGroup.count;
    }
    
    public boolean isAdAvailable(int paramInt1, int paramInt2)
    {
      AdPlaybackState.AdGroup localAdGroup = this.adPlaybackState.adGroups[paramInt1];
      return (localAdGroup.count != -1) && (localAdGroup.states[paramInt2] != 0);
    }
    
    public Period set(Object paramObject1, Object paramObject2, int paramInt, long paramLong1, long paramLong2)
    {
      return set(paramObject1, paramObject2, paramInt, paramLong1, paramLong2, AdPlaybackState.NONE);
    }
    
    public Period set(Object paramObject1, Object paramObject2, int paramInt, long paramLong1, long paramLong2, AdPlaybackState paramAdPlaybackState)
    {
      this.id = paramObject1;
      this.uid = paramObject2;
      this.windowIndex = paramInt;
      this.durationUs = paramLong1;
      this.positionInWindowUs = paramLong2;
      this.adPlaybackState = paramAdPlaybackState;
      return this;
    }
  }
  
  public static final class Window
  {
    public long defaultPositionUs;
    public long durationUs;
    public int firstPeriodIndex;
    public Object id;
    public boolean isDynamic;
    public boolean isSeekable;
    public int lastPeriodIndex;
    public long positionInFirstPeriodUs;
    public long presentationStartTimeMs;
    public long windowStartTimeMs;
    
    public long getDefaultPositionMs()
    {
      return C.usToMs(this.defaultPositionUs);
    }
    
    public long getDefaultPositionUs()
    {
      return this.defaultPositionUs;
    }
    
    public long getDurationMs()
    {
      return C.usToMs(this.durationUs);
    }
    
    public long getDurationUs()
    {
      return this.durationUs;
    }
    
    public long getPositionInFirstPeriodMs()
    {
      return C.usToMs(this.positionInFirstPeriodUs);
    }
    
    public long getPositionInFirstPeriodUs()
    {
      return this.positionInFirstPeriodUs;
    }
    
    public Window set(Object paramObject, long paramLong1, long paramLong2, boolean paramBoolean1, boolean paramBoolean2, long paramLong3, long paramLong4, int paramInt1, int paramInt2, long paramLong5)
    {
      this.id = paramObject;
      this.presentationStartTimeMs = paramLong1;
      this.windowStartTimeMs = paramLong2;
      this.isSeekable = paramBoolean1;
      this.isDynamic = paramBoolean2;
      this.defaultPositionUs = paramLong3;
      this.durationUs = paramLong4;
      this.firstPeriodIndex = paramInt1;
      this.lastPeriodIndex = paramInt2;
      this.positionInFirstPeriodUs = paramLong5;
      return this;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/Timeline.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */