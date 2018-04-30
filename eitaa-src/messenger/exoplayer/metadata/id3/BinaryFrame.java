package ir.eitaa.messenger.exoplayer.metadata.id3;

public final class BinaryFrame
  extends Id3Frame
{
  public final byte[] data;
  
  public BinaryFrame(String paramString, byte[] paramArrayOfByte)
  {
    super(paramString);
    this.data = paramArrayOfByte;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/metadata/id3/BinaryFrame.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */