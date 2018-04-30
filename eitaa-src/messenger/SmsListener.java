package ir.eitaa.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsListener
  extends BroadcastReceiver
{
  private SharedPreferences preferences;
  
  public void onReceive(final Context paramContext, Intent paramIntent)
  {
    if ((!paramIntent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) || (!AndroidUtilities.isWaitingForSms())) {}
    for (;;)
    {
      return;
      paramContext = paramIntent.getExtras();
      if (paramContext == null) {
        continue;
      }
      try
      {
        paramIntent = (Object[])paramContext.get("pdus");
        SmsMessage[] arrayOfSmsMessage = new SmsMessage[paramIntent.length];
        paramContext = "";
        int i = 0;
        while (i < arrayOfSmsMessage.length)
        {
          arrayOfSmsMessage[i] = SmsMessage.createFromPdu((byte[])(byte[])paramIntent[i]);
          paramContext = paramContext + arrayOfSmsMessage[i].getMessageBody();
          i += 1;
        }
        try
        {
          paramContext = Pattern.compile("[0-9]+").matcher(paramContext);
          if ((!paramContext.find()) || (paramContext.group(0).length() < 3)) {
            continue;
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReceiveSmsCode, new Object[] { paramContext.group(0) });
            }
          });
          return;
        }
        catch (Throwable paramContext)
        {
          FileLog.e("TSMS", paramContext);
          return;
        }
        return;
      }
      catch (Throwable paramContext)
      {
        FileLog.e("TSMS", paramContext);
      }
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/SmsListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */