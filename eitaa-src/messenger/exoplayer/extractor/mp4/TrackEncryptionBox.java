package ir.eitaa.messenger.exoplayer.extractor.mp4;

public final class TrackEncryptionBox
{
  public final int initializationVectorSize;
  public final boolean isEncrypted;
  public final byte[] keyId;
  
  public TrackEncryptionBox(boolean paramBoolean, int paramInt, byte[] paramArrayOfByte)
  {
    this.isEncrypted = paramBoolean;
    this.initializationVectorSize = paramInt;
    this.keyId = paramArrayOfByte;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/mp4/TrackEncryptionBox.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */