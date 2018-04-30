package ir.eitaa.messenger.exoplayer.hls;

import android.util.SparseArray;
import ir.eitaa.messenger.exoplayer.extractor.ts.PtsTimestampAdjuster;

public final class PtsTimestampAdjusterProvider
{
  private final SparseArray<PtsTimestampAdjuster> ptsTimestampAdjusters = new SparseArray();
  
  public PtsTimestampAdjuster getAdjuster(boolean paramBoolean, int paramInt, long paramLong)
  {
    PtsTimestampAdjuster localPtsTimestampAdjuster2 = (PtsTimestampAdjuster)this.ptsTimestampAdjusters.get(paramInt);
    PtsTimestampAdjuster localPtsTimestampAdjuster1 = localPtsTimestampAdjuster2;
    if (paramBoolean)
    {
      localPtsTimestampAdjuster1 = localPtsTimestampAdjuster2;
      if (localPtsTimestampAdjuster2 == null)
      {
        localPtsTimestampAdjuster1 = new PtsTimestampAdjuster(paramLong);
        this.ptsTimestampAdjusters.put(paramInt, localPtsTimestampAdjuster1);
      }
    }
    if ((paramBoolean) || ((localPtsTimestampAdjuster1 != null) && (localPtsTimestampAdjuster1.isInitialized()))) {
      return localPtsTimestampAdjuster1;
    }
    return null;
  }
  
  public void reset()
  {
    this.ptsTimestampAdjusters.clear();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/hls/PtsTimestampAdjusterProvider.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */