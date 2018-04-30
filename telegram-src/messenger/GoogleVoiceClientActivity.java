package org.telegram.messenger;

import com.google.android.search.verification.client.SearchActionVerificationClientActivity;
import com.google.android.search.verification.client.SearchActionVerificationClientService;

public class GoogleVoiceClientActivity
  extends SearchActionVerificationClientActivity
{
  public Class<? extends SearchActionVerificationClientService> getServiceClass()
  {
    return GoogleVoiceClientService.class;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/GoogleVoiceClientActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */