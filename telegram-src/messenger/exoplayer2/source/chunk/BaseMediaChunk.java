package org.telegram.messenger.exoplayer2.source.chunk;

import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;

public abstract class BaseMediaChunk
  extends MediaChunk
{
  private int[] firstSampleIndices;
  private BaseMediaChunkOutput output;
  
  public BaseMediaChunk(DataSource paramDataSource, DataSpec paramDataSpec, Format paramFormat, int paramInt1, Object paramObject, long paramLong1, long paramLong2, int paramInt2)
  {
    super(paramDataSource, paramDataSpec, paramFormat, paramInt1, paramObject, paramLong1, paramLong2, paramInt2);
  }
  
  public final int getFirstSampleIndex(int paramInt)
  {
    return this.firstSampleIndices[paramInt];
  }
  
  protected final BaseMediaChunkOutput getOutput()
  {
    return this.output;
  }
  
  public void init(BaseMediaChunkOutput paramBaseMediaChunkOutput)
  {
    this.output = paramBaseMediaChunkOutput;
    this.firstSampleIndices = paramBaseMediaChunkOutput.getWriteIndices();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/chunk/BaseMediaChunk.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */