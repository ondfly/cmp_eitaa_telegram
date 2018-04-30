package org.telegram.messenger.exoplayer2.source.hls;

import android.util.SparseArray;
import org.telegram.messenger.exoplayer2.util.TimestampAdjuster;

public final class TimestampAdjusterProvider
{
  private final SparseArray<TimestampAdjuster> timestampAdjusters = new SparseArray();
  
  public TimestampAdjuster getAdjuster(int paramInt)
  {
    TimestampAdjuster localTimestampAdjuster2 = (TimestampAdjuster)this.timestampAdjusters.get(paramInt);
    TimestampAdjuster localTimestampAdjuster1 = localTimestampAdjuster2;
    if (localTimestampAdjuster2 == null)
    {
      localTimestampAdjuster1 = new TimestampAdjuster(Long.MAX_VALUE);
      this.timestampAdjusters.put(paramInt, localTimestampAdjuster1);
    }
    return localTimestampAdjuster1;
  }
  
  public void reset()
  {
    this.timestampAdjusters.clear();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/TimestampAdjusterProvider.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */