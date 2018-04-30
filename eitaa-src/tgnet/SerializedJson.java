package ir.eitaa.tgnet;

import ir.eitaa.messenger.FileLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SerializedJson
  extends AbsSerializedJson
{
  private Boolean isJasonObject = Boolean.valueOf(false);
  private JSONObject jsonObject;
  private Object mJsonResult;
  
  public SerializedJson()
  {
    this.jsonObject = new JSONObject();
    this.isJasonObject = Boolean.valueOf(true);
  }
  
  public SerializedJson(String paramString)
  {
    try
    {
      this.mJsonResult = new JSONTokener(paramString).nextValue();
      if ((this.mJsonResult instanceof JSONObject))
      {
        this.jsonObject = ((JSONObject)this.mJsonResult);
        this.isJasonObject = Boolean.valueOf(true);
      }
      return;
    }
    catch (JSONException paramString)
    {
      FileLog.e("TSMS", "Create JSONObject error.");
    }
  }
  
  public SerializedJson(JSONObject paramJSONObject)
  {
    this.jsonObject = paramJSONObject;
    this.isJasonObject = Boolean.valueOf(true);
  }
  
  public void clear()
  {
    this.jsonObject = null;
    this.jsonObject = new JSONObject();
    this.isJasonObject = Boolean.valueOf(true);
  }
  
  public JSONArray getJsonArray(boolean paramBoolean)
  {
    if (this.mJsonResult == null) {}
    while (!(this.mJsonResult instanceof JSONArray)) {
      return null;
    }
    return (JSONArray)this.mJsonResult;
  }
  
  public Boolean isJsonArray()
  {
    if (this.mJsonResult == null) {
      return Boolean.valueOf(false);
    }
    return Boolean.valueOf(this.mJsonResult instanceof JSONArray);
  }
  
  public boolean readBool(String paramString, boolean paramBoolean)
  {
    try
    {
      if (!this.isJasonObject.booleanValue()) {
        throw new JSONException("read error.");
      }
    }
    catch (JSONException localJSONException)
    {
      if (paramBoolean)
      {
        throw new RuntimeException("read boolean error: " + paramString, localJSONException);
        boolean bool = this.jsonObject.getBoolean(paramString);
        return bool;
      }
      FileLog.e("TSMS", "read boolean error");
    }
    return false;
  }
  
  public double readDouble(String paramString, boolean paramBoolean)
  {
    try
    {
      if (!this.isJasonObject.booleanValue()) {
        throw new JSONException("read error.");
      }
    }
    catch (JSONException localJSONException)
    {
      if (paramBoolean)
      {
        throw new RuntimeException("read double error: " + paramString, localJSONException);
        double d = this.jsonObject.getDouble(paramString);
        return d;
      }
      FileLog.e("TSMS", "read double error");
    }
    return 0.0D;
  }
  
  public int readInt32(String paramString, boolean paramBoolean)
  {
    try
    {
      if (!this.isJasonObject.booleanValue()) {
        throw new JSONException("read error.");
      }
    }
    catch (JSONException localJSONException)
    {
      if (paramBoolean)
      {
        throw new RuntimeException("read int32 error: " + paramString, localJSONException);
        int i = this.jsonObject.getInt(paramString);
        return i;
      }
      FileLog.e("TSMS", "read int32 error");
    }
    return 0;
  }
  
  public long readInt64(String paramString, boolean paramBoolean)
  {
    try
    {
      if (!this.isJasonObject.booleanValue()) {
        throw new JSONException("read error.");
      }
    }
    catch (JSONException localJSONException)
    {
      if (paramBoolean)
      {
        throw new RuntimeException("read long error: " + paramString, localJSONException);
        long l = this.jsonObject.getLong(paramString);
        return l;
      }
      FileLog.e("TSMS", "read long error");
    }
    return 0L;
  }
  
  public JSONArray readJsonArray(String paramString, boolean paramBoolean)
  {
    try
    {
      if (!this.isJasonObject.booleanValue()) {
        throw new JSONException("read error.");
      }
    }
    catch (JSONException localJSONException)
    {
      if (paramBoolean)
      {
        throw new RuntimeException("read JsonArray error: " + paramString, localJSONException);
        JSONArray localJSONArray = this.jsonObject.getJSONArray(paramString);
        return localJSONArray;
      }
      FileLog.e("TSMS", "read JsonArray error: " + paramString);
    }
    return new JSONArray();
  }
  
  public AbsSerializedJson readObject(String paramString, boolean paramBoolean)
  {
    try
    {
      if (!this.isJasonObject.booleanValue()) {
        throw new JSONException("read error.");
      }
    }
    catch (JSONException localJSONException)
    {
      if (paramBoolean)
      {
        throw new RuntimeException("read JSONObject error: " + paramString, localJSONException);
        SerializedJson localSerializedJson = new SerializedJson(readString(paramString, paramBoolean));
        return localSerializedJson;
      }
      FileLog.e("TSMS", "read JSONObject error");
    }
    return null;
  }
  
  public JSONObject readObject(int paramInt, boolean paramBoolean)
  {
    if (this.mJsonResult == null) {
      return null;
    }
    try
    {
      if (!this.isJasonObject.booleanValue()) {
        throw new JSONException("read error.");
      }
    }
    catch (JSONException localJSONException)
    {
      if (paramBoolean)
      {
        throw new RuntimeException("read JSONObject error", localJSONException);
        if ((this.mJsonResult instanceof JSONArray))
        {
          JSONObject localJSONObject = ((JSONArray)this.mJsonResult).getJSONObject(paramInt);
          return localJSONObject;
        }
      }
      else
      {
        FileLog.e("TSMS", "read JSONObject error");
      }
    }
    return null;
  }
  
  public String readString(String paramString, boolean paramBoolean)
  {
    try
    {
      if (!this.isJasonObject.booleanValue()) {
        throw new JSONException("read error.");
      }
    }
    catch (JSONException localJSONException)
    {
      if (paramBoolean)
      {
        throw new RuntimeException("read String error: " + paramString, localJSONException);
        if (this.jsonObject.isNull(paramString)) {
          return null;
        }
        String str = this.jsonObject.getString(paramString);
        return str;
      }
      FileLog.e("TSMS", "read String error: " + paramString);
    }
    return null;
  }
  
  public JSONObject toJSONObject()
  {
    return this.jsonObject;
  }
  
  public String toURL()
  {
    return this.jsonObject.toString();
  }
  
  public void write(String paramString, double paramDouble)
  {
    try
    {
      this.jsonObject.put(paramString, paramDouble);
      return;
    }
    catch (JSONException localJSONException)
    {
      FileLog.e("TSMS", "Json write double error: " + paramString);
    }
  }
  
  public void write(String paramString, int paramInt)
  {
    try
    {
      this.jsonObject.put(paramString, paramInt);
      return;
    }
    catch (JSONException localJSONException)
    {
      FileLog.e("TSMS", "Json write Int error: " + paramString);
    }
  }
  
  public void write(String paramString, long paramLong)
  {
    try
    {
      this.jsonObject.put(paramString, paramLong);
      return;
    }
    catch (JSONException localJSONException)
    {
      FileLog.e("TSMS", "Json write long error: " + paramString);
    }
  }
  
  public void write(String paramString, TLObject paramTLObject)
  {
    try
    {
      SerializedJson localSerializedJson = new SerializedJson();
      paramTLObject.serializeToJson(localSerializedJson);
      this.jsonObject.put(paramString, localSerializedJson.toJSONObject());
      return;
    }
    catch (JSONException paramTLObject)
    {
      FileLog.e("TSMS", "Json write TLObject error: " + paramString);
    }
  }
  
  public void write(String paramString, Object paramObject)
  {
    try
    {
      this.jsonObject.put(paramString, paramObject);
      return;
    }
    catch (JSONException paramObject)
    {
      FileLog.e("TSMS", "Json write Object error: " + paramString);
    }
  }
  
  public void write(String paramString, boolean paramBoolean)
  {
    try
    {
      this.jsonObject.put(paramString, paramBoolean);
      return;
    }
    catch (JSONException localJSONException)
    {
      FileLog.e("TSMS", "Json write boolean error: " + paramString);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/tgnet/SerializedJson.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */