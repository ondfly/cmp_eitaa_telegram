package ir.eitaa.messenger.volley;

public class ParseError
  extends VolleyError
{
  public ParseError() {}
  
  public ParseError(NetworkResponse paramNetworkResponse)
  {
    super(paramNetworkResponse);
  }
  
  public ParseError(Throwable paramThrowable)
  {
    super(paramThrowable);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/ParseError.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */