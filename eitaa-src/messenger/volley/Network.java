package ir.eitaa.messenger.volley;

public abstract interface Network
{
  public abstract NetworkResponse performRequest(Request<?> paramRequest)
    throws VolleyError;
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/Network.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */