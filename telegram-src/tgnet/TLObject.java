package org.telegram.tgnet;

public class TLObject
{
  private static final ThreadLocal<NativeByteBuffer> sizeCalculator = new ThreadLocal()
  {
    protected NativeByteBuffer initialValue()
    {
      return new NativeByteBuffer(true);
    }
  };
  public boolean disableFree = false;
  public int networkType;
  
  public TLObject deserializeResponse(AbstractSerializedData paramAbstractSerializedData, int paramInt, boolean paramBoolean)
  {
    return null;
  }
  
  public void freeResources() {}
  
  public int getObjectSize()
  {
    NativeByteBuffer localNativeByteBuffer = (NativeByteBuffer)sizeCalculator.get();
    localNativeByteBuffer.rewind();
    serializeToStream((AbstractSerializedData)sizeCalculator.get());
    return localNativeByteBuffer.length();
  }
  
  public void readParams(AbstractSerializedData paramAbstractSerializedData, boolean paramBoolean) {}
  
  public void serializeToStream(AbstractSerializedData paramAbstractSerializedData) {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/tgnet/TLObject.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */