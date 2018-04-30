package ir.eitaa.messenger.volley.toolbox;

import ir.eitaa.messenger.volley.NetworkResponse;
import ir.eitaa.messenger.volley.ParseError;
import ir.eitaa.messenger.volley.Response;
import ir.eitaa.messenger.volley.Response.ErrorListener;
import ir.eitaa.messenger.volley.Response.Listener;
import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonObjectRequest
  extends JsonRequest<JSONObject>
{
  public JsonObjectRequest(int paramInt, String paramString, JSONObject paramJSONObject, Response.Listener<JSONObject> paramListener, Response.ErrorListener paramErrorListener) {}
  
  public JsonObjectRequest(String paramString, JSONObject paramJSONObject, Response.Listener<JSONObject> paramListener, Response.ErrorListener paramErrorListener) {}
  
  protected Response<JSONObject> parseNetworkResponse(NetworkResponse paramNetworkResponse)
  {
    try
    {
      paramNetworkResponse = Response.success(new JSONObject(new String(paramNetworkResponse.data, HttpHeaderParser.parseCharset(paramNetworkResponse.headers, "utf-8"))), HttpHeaderParser.parseCacheHeaders(paramNetworkResponse));
      return paramNetworkResponse;
    }
    catch (UnsupportedEncodingException paramNetworkResponse)
    {
      return Response.error(new ParseError(paramNetworkResponse));
    }
    catch (JSONException paramNetworkResponse) {}
    return Response.error(new ParseError(paramNetworkResponse));
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/toolbox/JsonObjectRequest.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */