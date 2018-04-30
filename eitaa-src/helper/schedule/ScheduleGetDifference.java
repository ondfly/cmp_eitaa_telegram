package ir.eitaa.helper.schedule;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.BuildVars;
import ir.eitaa.messenger.DispatchQueue;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.Utilities;
import ir.eitaa.tgnet.ConnectionsManager;

public class ScheduleGetDifference
  extends IntentService
{
  public static boolean isDeveloopMode = BuildVars.DEBUG_VERSION;
  
  public ScheduleGetDifference()
  {
    super("ScheduleGetDifference");
  }
  
  protected void onHandleIntent(Intent paramIntent)
  {
    paramIntent = new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance();
        if ((ScheduleController.appPausedbackgroundSchedule) && (ScheduleController.lastPauseTime < System.currentTimeMillis() - MessagesController.getInstance().schedule_period_background_delay_ms))
        {
          ScheduleController.periodBackgroundSchedule(Boolean.valueOf(false));
          ScheduleController.appPausedbackgroundSchedule = false;
        }
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            if ((ConnectionsManager.getInstance().getPingStatus()) && (!ScheduleGetDifference.isDeveloopMode)) {
              MessagesController.getInstance().getDifference();
            }
          }
        });
      }
    };
    ApplicationLoader.applicationHandler.post(paramIntent);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/helper/schedule/ScheduleGetDifference.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */