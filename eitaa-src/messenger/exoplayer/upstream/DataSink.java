package ir.eitaa.messenger.exoplayer.upstream;

import java.io.IOException;

public abstract interface DataSink
{
  public abstract void close()
    throws IOException;
  
  public abstract DataSink open(DataSpec paramDataSpec)
    throws IOException;
  
  public abstract void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/upstream/DataSink.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */