package org.telegram.messenger.exoplayer2.upstream;

import java.io.IOException;

public abstract interface DataSink
{
  public abstract void close()
    throws IOException;
  
  public abstract void open(DataSpec paramDataSpec)
    throws IOException;
  
  public abstract void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;
  
  public static abstract interface Factory
  {
    public abstract DataSink createDataSink();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/DataSink.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */