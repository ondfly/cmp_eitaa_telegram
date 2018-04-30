package ir.eitaa.helper.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.MessagesController;

public class ScheduleController
{
  private static final int START_PERIOD = 1000;
  public static boolean appPausedbackgroundSchedule = false;
  public static long lastPauseTime;
  
  public static void periodBackgroundSchedule(Boolean paramBoolean)
  {
    try
    {
      AlarmManager localAlarmManager = (AlarmManager)ApplicationLoader.applicationContext.getSystemService("alarm");
      Object localObject = new Intent(ApplicationLoader.applicationContext, ScheduleGetDifference.class);
      ((Intent)localObject).setAction("ir.eitaa.helper.schedule.periodBackgroundSchedule");
      localObject = PendingIntent.getService(ApplicationLoader.applicationContext, 0, (Intent)localObject, 134217728);
      localAlarmManager.cancel((PendingIntent)localObject);
      if (paramBoolean.booleanValue())
      {
        appPausedbackgroundSchedule = true;
        lastPauseTime = System.currentTimeMillis();
        localAlarmManager.setRepeating(2, SystemClock.elapsedRealtime() + 1000L, MessagesController.getInstance().schedule_period_forground_ms, (PendingIntent)localObject);
      }
      return;
    }
    catch (Exception paramBoolean) {}
  }
  
  public static void scheduleGetDifference(Boolean paramBoolean1, Boolean paramBoolean2)
  {
    try
    {
      paramBoolean1 = (AlarmManager)ApplicationLoader.applicationContext.getSystemService("alarm");
      Object localObject = new Intent(ApplicationLoader.applicationContext, ScheduleGetDifference.class);
      ((Intent)localObject).setAction("ir.eitaa.helper.schedule.ScheduleGetDifference");
      localObject = PendingIntent.getService(ApplicationLoader.applicationContext, 0, (Intent)localObject, 134217728);
      paramBoolean1.cancel((PendingIntent)localObject);
      if (!paramBoolean2.booleanValue()) {
        paramBoolean1.setRepeating(2, SystemClock.elapsedRealtime() + 1000L, MessagesController.getInstance().schedule_period_background_ms, (PendingIntent)localObject);
      }
      return;
    }
    catch (Exception paramBoolean1) {}
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/helper/schedule/ScheduleController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */