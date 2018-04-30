package org.telegram.messenger.exoplayer2.upstream;

public abstract interface TransferListener<S>
{
  public abstract void onBytesTransferred(S paramS, int paramInt);
  
  public abstract void onTransferEnd(S paramS);
  
  public abstract void onTransferStart(S paramS, DataSpec paramDataSpec);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/TransferListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */