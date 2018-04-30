package ir.eitaa.messenger.volley;

public abstract interface RetryPolicy
{
  public abstract int getCurrentRetryCount();
  
  public abstract int getCurrentTimeout();
  
  public abstract void retry(VolleyError paramVolleyError)
    throws VolleyError;
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/RetryPolicy.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */