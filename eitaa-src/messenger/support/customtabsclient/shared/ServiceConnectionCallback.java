package ir.eitaa.messenger.support.customtabsclient.shared;

import ir.eitaa.messenger.support.customtabs.CustomTabsClient;

public abstract interface ServiceConnectionCallback
{
  public abstract void onServiceConnected(CustomTabsClient paramCustomTabsClient);
  
  public abstract void onServiceDisconnected();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/support/customtabsclient/shared/ServiceConnectionCallback.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */