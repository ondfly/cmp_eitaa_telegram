package org.telegram.messenger.exoplayer2.source.chunk;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.extractor.DefaultExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.Util;

public class ContainerMediaChunk
  extends BaseMediaChunk
{
  private volatile int bytesLoaded;
  private final int chunkCount;
  private final ChunkExtractorWrapper extractorWrapper;
  private volatile boolean loadCanceled;
  private volatile boolean loadCompleted;
  private final long sampleOffsetUs;
  
  public ContainerMediaChunk(DataSource paramDataSource, DataSpec paramDataSpec, Format paramFormat, int paramInt1, Object paramObject, long paramLong1, long paramLong2, int paramInt2, int paramInt3, long paramLong3, ChunkExtractorWrapper paramChunkExtractorWrapper)
  {
    super(paramDataSource, paramDataSpec, paramFormat, paramInt1, paramObject, paramLong1, paramLong2, paramInt2);
    this.chunkCount = paramInt3;
    this.sampleOffsetUs = paramLong3;
    this.extractorWrapper = paramChunkExtractorWrapper;
  }
  
  public final long bytesLoaded()
  {
    return this.bytesLoaded;
  }
  
  public final void cancelLoad()
  {
    this.loadCanceled = true;
  }
  
  public int getNextChunkIndex()
  {
    return this.chunkIndex + this.chunkCount;
  }
  
  public final boolean isLoadCanceled()
  {
    return this.loadCanceled;
  }
  
  public boolean isLoadCompleted()
  {
    return this.loadCompleted;
  }
  
  public final void load()
    throws IOException, InterruptedException
  {
    Object localObject1 = this.dataSpec.subrange(this.bytesLoaded);
    for (;;)
    {
      try
      {
        localObject1 = new DefaultExtractorInput(this.dataSource, ((DataSpec)localObject1).absoluteStreamPosition, this.dataSource.open((DataSpec)localObject1));
        Object localObject3;
        if (this.bytesLoaded == 0)
        {
          localObject3 = getOutput();
          ((BaseMediaChunkOutput)localObject3).setSampleOffsetUs(this.sampleOffsetUs);
          this.extractorWrapper.init((ChunkExtractorWrapper.TrackOutputProvider)localObject3);
        }
        int i;
        try
        {
          localObject3 = this.extractorWrapper.extractor;
          i = 0;
          if ((i != 0) || (this.loadCanceled)) {
            break label180;
          }
          i = ((Extractor)localObject3).read((ExtractorInput)localObject1, null);
          continue;
          Assertions.checkState(bool);
          this.bytesLoaded = ((int)(((ExtractorInput)localObject1).getPosition() - this.dataSpec.absoluteStreamPosition));
          Util.closeQuietly(this.dataSource);
          this.loadCompleted = true;
          return;
        }
        finally
        {
          this.bytesLoaded = ((int)(((ExtractorInput)localObject1).getPosition() - this.dataSpec.absoluteStreamPosition));
        }
        bool = false;
        continue;
        if (i == 1) {
          continue;
        }
      }
      finally
      {
        Util.closeQuietly(this.dataSource);
      }
      label180:
      boolean bool = true;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/chunk/ContainerMediaChunk.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */