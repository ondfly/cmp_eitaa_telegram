package org.telegram.messenger.exoplayer2.source.hls;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.FormatHolder;
import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;
import org.telegram.messenger.exoplayer2.source.SampleStream;
import org.telegram.messenger.exoplayer2.source.TrackGroup;
import org.telegram.messenger.exoplayer2.source.TrackGroupArray;

final class HlsSampleStream
  implements SampleStream
{
  private int sampleQueueIndex;
  private final HlsSampleStreamWrapper sampleStreamWrapper;
  private final int trackGroupIndex;
  
  public HlsSampleStream(HlsSampleStreamWrapper paramHlsSampleStreamWrapper, int paramInt)
  {
    this.sampleStreamWrapper = paramHlsSampleStreamWrapper;
    this.trackGroupIndex = paramInt;
    this.sampleQueueIndex = -1;
  }
  
  private boolean ensureBoundSampleQueue()
  {
    if (this.sampleQueueIndex != -1) {}
    do
    {
      return true;
      this.sampleQueueIndex = this.sampleStreamWrapper.bindSampleQueueToSampleStream(this.trackGroupIndex);
    } while (this.sampleQueueIndex != -1);
    return false;
  }
  
  public boolean isReady()
  {
    return (ensureBoundSampleQueue()) && (this.sampleStreamWrapper.isReady(this.sampleQueueIndex));
  }
  
  public void maybeThrowError()
    throws IOException
  {
    if ((!ensureBoundSampleQueue()) && (this.sampleStreamWrapper.isMappingFinished())) {
      throw new SampleQueueMappingException(this.sampleStreamWrapper.getTrackGroups().get(this.trackGroupIndex).getFormat(0).sampleMimeType);
    }
    this.sampleStreamWrapper.maybeThrowError();
  }
  
  public int readData(FormatHolder paramFormatHolder, DecoderInputBuffer paramDecoderInputBuffer, boolean paramBoolean)
  {
    if (!ensureBoundSampleQueue()) {
      return -3;
    }
    return this.sampleStreamWrapper.readData(this.sampleQueueIndex, paramFormatHolder, paramDecoderInputBuffer, paramBoolean);
  }
  
  public int skipData(long paramLong)
  {
    if (!ensureBoundSampleQueue()) {
      return 0;
    }
    return this.sampleStreamWrapper.skipData(this.sampleQueueIndex, paramLong);
  }
  
  public void unbindSampleQueue()
  {
    if (this.sampleQueueIndex != -1)
    {
      this.sampleStreamWrapper.unbindSampleQueue(this.trackGroupIndex);
      this.sampleQueueIndex = -1;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/HlsSampleStream.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */