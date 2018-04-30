package ir.eitaa.tgnet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import ir.eitaa.messenger.FileLog;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(include=JsonTypeInfo.As.WRAPPER_OBJECT, use=JsonTypeInfo.Id.NAME)
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
  
  public static int getConstructor(AbsSerializedJson paramAbsSerializedJson)
  {
    if (paramAbsSerializedJson == null) {
      return 0;
    }
    try
    {
      long l = Long.parseLong(paramAbsSerializedJson.readString("constructor", false), 16);
      return (int)l;
    }
    catch (NumberFormatException paramAbsSerializedJson)
    {
      FileLog.e("TSMS", "can't get constructor value.");
    }
    return 0;
  }
  
  public TLObject deserializeResponse(AbsSerializedJson paramAbsSerializedJson, boolean paramBoolean)
  {
    return null;
  }
  
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
  
  public void readParams(AbsSerializedJson paramAbsSerializedJson, boolean paramBoolean) {}
  
  public void readParams(AbstractSerializedData paramAbstractSerializedData, boolean paramBoolean) {}
  
  public void serializeToJson(AbsSerializedJson paramAbsSerializedJson) {}
  
  public void serializeToStream(AbstractSerializedData paramAbstractSerializedData) {}
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/tgnet/TLObject.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */