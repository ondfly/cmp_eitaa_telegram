package ir.eitaa.messenger.volley.toolbox;

import ir.eitaa.messenger.volley.AuthFailureError;
import ir.eitaa.messenger.volley.Request;
import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpResponse;

public abstract interface HttpStack
{
  public abstract HttpResponse performRequest(Request<?> paramRequest, Map<String, String> paramMap)
    throws IOException, AuthFailureError;
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/toolbox/HttpStack.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */