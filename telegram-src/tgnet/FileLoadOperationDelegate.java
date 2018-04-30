package org.telegram.tgnet;

public abstract interface FileLoadOperationDelegate
{
  public abstract void onFailed(int paramInt);
  
  public abstract void onFinished(String paramString);
  
  public abstract void onProgressChanged(float paramFloat);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/tgnet/FileLoadOperationDelegate.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */