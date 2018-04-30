package org.telegram.messenger;

import android.content.Intent;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.support.JobIntentService;

public class KeepAliveJob
  extends JobIntentService
{
  private static volatile CountDownLatch countDownLatch;
  private static Runnable finishJobByTimeoutRunnable = new Runnable()
  {
    public void run() {}
  };
  private static volatile boolean startingJob;
  private static final Object sync = new Object();
  
  public static void finishJob()
  {
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        synchronized (KeepAliveJob.sync)
        {
          if (KeepAliveJob.countDownLatch != null)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("finish keep-alive job");
            }
            KeepAliveJob.countDownLatch.countDown();
          }
          if (KeepAliveJob.startingJob)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("finish queued keep-alive job");
            }
            KeepAliveJob.access$002(false);
          }
          return;
        }
      }
    });
  }
  
  public static void startJob()
  {
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: invokestatic 22	org/telegram/messenger/KeepAliveJob:access$000	()Z
        //   3: ifne +9 -> 12
        //   6: invokestatic 26	org/telegram/messenger/KeepAliveJob:access$100	()Ljava/util/concurrent/CountDownLatch;
        //   9: ifnull +4 -> 13
        //   12: return
        //   13: getstatic 32	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
        //   16: ifeq +8 -> 24
        //   19: ldc 34
        //   21: invokestatic 40	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
        //   24: invokestatic 44	org/telegram/messenger/KeepAliveJob:access$200	()Ljava/lang/Object;
        //   27: astore_1
        //   28: aload_1
        //   29: monitorenter
        //   30: iconst_1
        //   31: invokestatic 48	org/telegram/messenger/KeepAliveJob:access$002	(Z)Z
        //   34: pop
        //   35: aload_1
        //   36: monitorexit
        //   37: getstatic 54	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
        //   40: ldc 8
        //   42: sipush 1000
        //   45: new 56	android/content/Intent
        //   48: dup
        //   49: invokespecial 57	android/content/Intent:<init>	()V
        //   52: invokestatic 63	org/telegram/messenger/support/JobIntentService:enqueueWork	(Landroid/content/Context;Ljava/lang/Class;ILandroid/content/Intent;)V
        //   55: return
        //   56: astore_1
        //   57: return
        //   58: astore_2
        //   59: aload_1
        //   60: monitorexit
        //   61: aload_2
        //   62: athrow
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	63	0	this	1
        //   56	4	1	localException	Exception
        //   58	4	2	localObject2	Object
        // Exception table:
        //   from	to	target	type
        //   13	24	56	java/lang/Exception
        //   24	30	56	java/lang/Exception
        //   37	55	56	java/lang/Exception
        //   61	63	56	java/lang/Exception
        //   30	37	58	finally
        //   59	61	58	finally
      }
    });
  }
  
  protected void onHandleWork(Intent arg1)
  {
    synchronized (sync)
    {
      if (!startingJob) {
        return;
      }
      countDownLatch = new CountDownLatch(1);
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("started keep-alive job");
      }
      Utilities.globalQueue.postRunnable(finishJobByTimeoutRunnable, 60000L);
    }
    try
    {
      countDownLatch.await();
      Utilities.globalQueue.cancelRunnable(finishJobByTimeoutRunnable);
      synchronized (sync)
      {
        countDownLatch = null;
        if (BuildVars.LOGS_ENABLED)
        {
          FileLog.d("ended keep-alive job");
          return;
          localObject1 = finally;
          throw ((Throwable)localObject1);
        }
      }
    }
    catch (Throwable ???)
    {
      for (;;) {}
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/KeepAliveJob.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */