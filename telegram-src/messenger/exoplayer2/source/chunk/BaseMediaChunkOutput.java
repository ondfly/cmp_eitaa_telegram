package org.telegram.messenger.exoplayer2.source.chunk;

import android.util.Log;
import org.telegram.messenger.exoplayer2.extractor.DummyTrackOutput;
import org.telegram.messenger.exoplayer2.extractor.TrackOutput;
import org.telegram.messenger.exoplayer2.source.SampleQueue;

final class BaseMediaChunkOutput
  implements ChunkExtractorWrapper.TrackOutputProvider
{
  private static final String TAG = "BaseMediaChunkOutput";
  private final SampleQueue[] sampleQueues;
  private final int[] trackTypes;
  
  public BaseMediaChunkOutput(int[] paramArrayOfInt, SampleQueue[] paramArrayOfSampleQueue)
  {
    this.trackTypes = paramArrayOfInt;
    this.sampleQueues = paramArrayOfSampleQueue;
  }
  
  public int[] getWriteIndices()
  {
    int[] arrayOfInt = new int[this.sampleQueues.length];
    int i = 0;
    while (i < this.sampleQueues.length)
    {
      if (this.sampleQueues[i] != null) {
        arrayOfInt[i] = this.sampleQueues[i].getWriteIndex();
      }
      i += 1;
    }
    return arrayOfInt;
  }
  
  public void setSampleOffsetUs(long paramLong)
  {
    SampleQueue[] arrayOfSampleQueue = this.sampleQueues;
    int j = arrayOfSampleQueue.length;
    int i = 0;
    while (i < j)
    {
      SampleQueue localSampleQueue = arrayOfSampleQueue[i];
      if (localSampleQueue != null) {
        localSampleQueue.setSampleOffsetUs(paramLong);
      }
      i += 1;
    }
  }
  
  public TrackOutput track(int paramInt1, int paramInt2)
  {
    paramInt1 = 0;
    while (paramInt1 < this.trackTypes.length)
    {
      if (paramInt2 == this.trackTypes[paramInt1]) {
        return this.sampleQueues[paramInt1];
      }
      paramInt1 += 1;
    }
    Log.e("BaseMediaChunkOutput", "Unmatched track of type: " + paramInt2);
    return new DummyTrackOutput();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/chunk/BaseMediaChunkOutput.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */