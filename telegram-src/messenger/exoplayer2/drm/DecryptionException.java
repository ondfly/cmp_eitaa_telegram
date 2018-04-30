package org.telegram.messenger.exoplayer2.drm;

public class DecryptionException
  extends Exception
{
  public final int errorCode;
  
  public DecryptionException(int paramInt, String paramString)
  {
    super(paramString);
    this.errorCode = paramInt;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/drm/DecryptionException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */