package org.telegram.messenger.exoplayer2.upstream;

import java.io.IOException;

public final class DataSourceException
  extends IOException
{
  public static final int POSITION_OUT_OF_RANGE = 0;
  public final int reason;
  
  public DataSourceException(int paramInt)
  {
    this.reason = paramInt;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/DataSourceException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */