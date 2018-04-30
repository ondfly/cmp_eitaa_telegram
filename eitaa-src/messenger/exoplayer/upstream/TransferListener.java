package ir.eitaa.messenger.exoplayer.upstream;

public abstract interface TransferListener
{
  public abstract void onBytesTransferred(int paramInt);
  
  public abstract void onTransferEnd();
  
  public abstract void onTransferStart();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/upstream/TransferListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */