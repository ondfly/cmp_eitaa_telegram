package ir.eitaa.messenger.volley.toolbox;

import ir.eitaa.messenger.volley.AuthFailureError;

public abstract interface Authenticator
{
  public abstract String getAuthToken()
    throws AuthFailureError;
  
  public abstract void invalidateAuthToken(String paramString);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/toolbox/Authenticator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */