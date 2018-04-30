package ir.eitaa.messenger.volley;

public class NetworkError
  extends VolleyError
{
  public NetworkError() {}
  
  public NetworkError(NetworkResponse paramNetworkResponse)
  {
    super(paramNetworkResponse);
  }
  
  public NetworkError(Throwable paramThrowable)
  {
    super(paramThrowable);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/NetworkError.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */