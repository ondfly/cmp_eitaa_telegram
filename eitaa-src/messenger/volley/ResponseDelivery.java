package ir.eitaa.messenger.volley;

public abstract interface ResponseDelivery
{
  public abstract void postError(Request<?> paramRequest, VolleyError paramVolleyError);
  
  public abstract void postResponse(Request<?> paramRequest, Response<?> paramResponse);
  
  public abstract void postResponse(Request<?> paramRequest, Response<?> paramResponse, Runnable paramRunnable);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/ResponseDelivery.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */