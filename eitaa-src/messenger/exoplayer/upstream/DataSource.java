package ir.eitaa.messenger.exoplayer.upstream;

import java.io.IOException;

public abstract interface DataSource
{
  public abstract void close()
    throws IOException;
  
  public abstract long open(DataSpec paramDataSpec)
    throws IOException;
  
  public abstract int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/upstream/DataSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */