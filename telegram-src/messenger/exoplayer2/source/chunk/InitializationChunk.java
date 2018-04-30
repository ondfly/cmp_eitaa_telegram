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

public final class InitializationChunk
  extends Chunk
{
  private volatile int bytesLoaded;
  private final ChunkExtractorWrapper extractorWrapper;
  private volatile boolean loadCanceled;
  
  public InitializationChunk(DataSource paramDataSource, DataSpec paramDataSpec, Format paramFormat, int paramInt, Object paramObject, ChunkExtractorWrapper paramChunkExtractorWrapper)
  {
    super(paramDataSource, paramDataSpec, 2, paramFormat, paramInt, paramObject, -9223372036854775807L, -9223372036854775807L);
    this.extractorWrapper = paramChunkExtractorWrapper;
  }
  
  public long bytesLoaded()
  {
    return this.bytesLoaded;
  }
  
  public void cancelLoad()
  {
    this.loadCanceled = true;
  }
  
  public boolean isLoadCanceled()
  {
    return this.loadCanceled;
  }
  
  public void load()
    throws IOException, InterruptedException
  {
    Object localObject1 = this.dataSpec.subrange(this.bytesLoaded);
    for (;;)
    {
      try
      {
        localObject1 = new DefaultExtractorInput(this.dataSource, ((DataSpec)localObject1).absoluteStreamPosition, this.dataSource.open((DataSpec)localObject1));
        if (this.bytesLoaded == 0) {
          this.extractorWrapper.init(null);
        }
        int i;
        try
        {
          Extractor localExtractor = this.extractorWrapper.extractor;
          i = 0;
          if ((i != 0) || (this.loadCanceled)) {
            break label159;
          }
          i = localExtractor.read((ExtractorInput)localObject1, null);
          continue;
          Assertions.checkState(bool);
          this.bytesLoaded = ((int)(((ExtractorInput)localObject1).getPosition() - this.dataSpec.absoluteStreamPosition));
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
      label159:
      boolean bool = true;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/chunk/InitializationChunk.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */