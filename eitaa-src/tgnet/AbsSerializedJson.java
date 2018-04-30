package ir.eitaa.tgnet;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbsSerializedJson
{
  public abstract JSONArray getJsonArray(boolean paramBoolean);
  
  public abstract Boolean isJsonArray();
  
  public abstract boolean readBool(String paramString, boolean paramBoolean);
  
  public abstract double readDouble(String paramString, boolean paramBoolean);
  
  public abstract int readInt32(String paramString, boolean paramBoolean);
  
  public abstract long readInt64(String paramString, boolean paramBoolean);
  
  public abstract JSONArray readJsonArray(String paramString, boolean paramBoolean);
  
  public abstract AbsSerializedJson readObject(String paramString, boolean paramBoolean);
  
  public abstract JSONObject readObject(int paramInt, boolean paramBoolean);
  
  public abstract String readString(String paramString, boolean paramBoolean);
  
  public abstract void write(String paramString, double paramDouble);
  
  public abstract void write(String paramString, int paramInt);
  
  public abstract void write(String paramString, long paramLong);
  
  public abstract void write(String paramString, TLObject paramTLObject);
  
  public abstract void write(String paramString, Object paramObject);
  
  public abstract void write(String paramString, boolean paramBoolean);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/tgnet/AbsSerializedJson.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */