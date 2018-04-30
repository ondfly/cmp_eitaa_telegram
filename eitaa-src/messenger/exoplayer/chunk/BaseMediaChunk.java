package ir.eitaa.messenger.exoplayer.chunk;

import ir.eitaa.messenger.exoplayer.MediaFormat;
import ir.eitaa.messenger.exoplayer.drm.DrmInitData;
import ir.eitaa.messenger.exoplayer.extractor.DefaultTrackOutput;
import ir.eitaa.messenger.exoplayer.upstream.DataSource;
import ir.eitaa.messenger.exoplayer.upstream.DataSpec;

public abstract class BaseMediaChunk
  extends MediaChunk
{
  private int firstSampleIndex;
  public final boolean isMediaFormatFinal;
  private DefaultTrackOutput output;
  
  public BaseMediaChunk(DataSource paramDataSource, DataSpec paramDataSpec, int paramInt1, Format paramFormat, long paramLong1, long paramLong2, int paramInt2, boolean paramBoolean, int paramInt3)
  {
    super(paramDataSource, paramDataSpec, paramInt1, paramFormat, paramLong1, paramLong2, paramInt2, paramInt3);
    this.isMediaFormatFinal = paramBoolean;
  }
  
  public abstract DrmInitData getDrmInitData();
  
  public final int getFirstSampleIndex()
  {
    return this.firstSampleIndex;
  }
  
  public abstract MediaFormat getMediaFormat();
  
  protected final DefaultTrackOutput getOutput()
  {
    return this.output;
  }
  
  public void init(DefaultTrackOutput paramDefaultTrackOutput)
  {
    this.output = paramDefaultTrackOutput;
    this.firstSampleIndex = paramDefaultTrackOutput.getWriteIndex();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/chunk/BaseMediaChunk.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */