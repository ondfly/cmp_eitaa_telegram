package org.telegram.messenger.exoplayer2.upstream;

import java.io.IOException;

public abstract interface LoaderErrorThrower
{
  public abstract void maybeThrowError()
    throws IOException;
  
  public abstract void maybeThrowError(int paramInt)
    throws IOException;
  
  public static final class Dummy
    implements LoaderErrorThrower
  {
    public void maybeThrowError()
      throws IOException
    {}
    
    public void maybeThrowError(int paramInt)
      throws IOException
    {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/LoaderErrorThrower.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */