package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CustomTabsCopyReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    paramIntent = paramIntent.getDataString();
    if (paramIntent != null)
    {
      AndroidUtilities.addToClipboard(paramIntent);
      Toast.makeText(paramContext, LocaleController.getString("LinkCopied", 2131493748), 0).show();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/CustomTabsCopyReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */