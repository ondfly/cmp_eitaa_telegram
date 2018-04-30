package org.telegram.messenger;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class GcmInstanceIDListenerService
  extends FirebaseInstanceIdService
{
  public static void sendRegistrationToServer(String paramString)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        SharedConfig.pushString = this.val$token;
        final int i = 0;
        while (i < 3)
        {
          UserConfig localUserConfig = UserConfig.getInstance(i);
          localUserConfig.registeredForPush = false;
          localUserConfig.saveConfig(false);
          if (localUserConfig.getClientUserId() != 0) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.getInstance(i).registerForPush(GcmInstanceIDListenerService.2.this.val$token);
              }
            });
          }
          i += 1;
        }
      }
    });
  }
  
  public void onTokenRefresh()
  {
    try
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("Refreshed token: " + this.val$refreshedToken);
          }
          ApplicationLoader.postInitApplication();
          GcmInstanceIDListenerService.sendRegistrationToServer(this.val$refreshedToken);
        }
      });
      return;
    }
    catch (Throwable localThrowable)
    {
      FileLog.e(localThrowable);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/GcmInstanceIDListenerService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */