package org.telegram.messenger;

import android.app.IntentService;
import android.content.Intent;
import org.telegram.ui.LaunchActivity;

public class BringAppForegroundService
  extends IntentService
{
  public BringAppForegroundService()
  {
    super("BringAppForegroundService");
  }
  
  protected void onHandleIntent(Intent paramIntent)
  {
    paramIntent = new Intent(this, LaunchActivity.class);
    paramIntent.setFlags(268435456);
    startActivity(paramIntent);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/BringAppForegroundService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */