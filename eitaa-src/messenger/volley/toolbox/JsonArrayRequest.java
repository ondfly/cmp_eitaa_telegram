package ir.eitaa.messenger.volley.toolbox;

import ir.eitaa.messenger.volley.NetworkResponse;
import ir.eitaa.messenger.volley.ParseError;
import ir.eitaa.messenger.volley.Response;
import ir.eitaa.messenger.volley.Response.ErrorListener;
import ir.eitaa.messenger.volley.Response.Listener;
import java.io.UnsupportedEncodingException;
import org.json.JSONArray;
import org.json.JSONException;

public class JsonArrayRequest
  extends JsonRequest<JSONArray>
{
  public JsonArrayRequest(int paramInt, String paramString, JSONArray paramJSONArray, Response.Listener<JSONArray> paramListener, Response.ErrorListener paramErrorListener) {}
  
  public JsonArrayRequest(String paramString, Response.Listener<JSONArray> paramListener, Response.ErrorListener paramErrorListener)
  {
    super(0, paramString, null, paramListener, paramErrorListener);
  }
  
  protected Response<JSONArray> parseNetworkResponse(NetworkResponse paramNetworkResponse)
  {
    try
    {
      paramNetworkResponse = Response.success(new JSONArray(new String(paramNetworkResponse.data, HttpHeaderParser.parseCharset(paramNetworkResponse.headers, "utf-8"))), HttpHeaderParser.parseCacheHeaders(paramNetworkResponse));
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


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/toolbox/JsonArrayRequest.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */