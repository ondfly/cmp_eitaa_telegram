package ir.eitaa.messenger.exoplayer.chunk;

import ir.eitaa.messenger.exoplayer.MediaFormat;
import java.io.IOException;
import java.util.List;

public abstract interface ChunkSource
{
  public abstract void continueBuffering(long paramLong);
  
  public abstract void disable(List<? extends MediaChunk> paramList);
  
  public abstract void enable(int paramInt);
  
  public abstract void getChunkOperation(List<? extends MediaChunk> paramList, long paramLong, ChunkOperationHolder paramChunkOperationHolder);
  
  public abstract MediaFormat getFormat(int paramInt);
  
  public abstract int getTrackCount();
  
  public abstract void maybeThrowError()
    throws IOException;
  
  public abstract void onChunkLoadCompleted(Chunk paramChunk);
  
  public abstract void onChunkLoadError(Chunk paramChunk, Exception paramException);
  
  public abstract boolean prepare();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/chunk/ChunkSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */