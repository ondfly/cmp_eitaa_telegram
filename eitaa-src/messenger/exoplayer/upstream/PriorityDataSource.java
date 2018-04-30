package ir.eitaa.messenger.exoplayer.upstream;

import ir.eitaa.messenger.exoplayer.util.Assertions;
import java.io.IOException;

public final class PriorityDataSource
  implements DataSource
{
  private final int priority;
  private final DataSource upstream;
  
  public PriorityDataSource(int paramInt, DataSource paramDataSource)
  {
    this.priority = paramInt;
    this.upstream = ((DataSource)Assertions.checkNotNull(paramDataSource));
  }
  
  public void close()
    throws IOException
  {
    this.upstream.close();
  }
  
  public long open(DataSpec paramDataSpec)
    throws IOException
  {
    NetworkLock.instance.proceedOrThrow(this.priority);
    return this.upstream.open(paramDataSpec);
  }
  
  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    NetworkLock.instance.proceedOrThrow(this.priority);
    return this.upstream.read(paramArrayOfByte, paramInt1, paramInt2);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/upstream/PriorityDataSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */