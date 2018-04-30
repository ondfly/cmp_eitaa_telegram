package org.telegram.messenger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.CountDownLatch;

public class DispatchQueue
  extends Thread
{
  private volatile Handler handler = null;
  private CountDownLatch syncLatch = new CountDownLatch(1);
  
  public DispatchQueue(String paramString)
  {
    setName(paramString);
    start();
  }
  
  public void cancelRunnable(Runnable paramRunnable)
  {
    try
    {
      this.syncLatch.await();
      this.handler.removeCallbacks(paramRunnable);
      return;
    }
    catch (Exception paramRunnable)
    {
      FileLog.e(paramRunnable);
    }
  }
  
  public void cleanupQueue()
  {
    try
    {
      this.syncLatch.await();
      this.handler.removeCallbacksAndMessages(null);
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public void handleMessage(Message paramMessage) {}
  
  public void postRunnable(Runnable paramRunnable)
  {
    postRunnable(paramRunnable, 0L);
  }
  
  public void postRunnable(Runnable paramRunnable, long paramLong)
  {
    try
    {
      this.syncLatch.await();
      if (paramLong <= 0L)
      {
        this.handler.post(paramRunnable);
        return;
      }
      this.handler.postDelayed(paramRunnable, paramLong);
      return;
    }
    catch (Exception paramRunnable)
    {
      FileLog.e(paramRunnable);
    }
  }
  
  public void run()
  {
    Looper.prepare();
    this.handler = new Handler()
    {
      public void handleMessage(Message paramAnonymousMessage)
      {
        DispatchQueue.this.handleMessage(paramAnonymousMessage);
      }
    };
    this.syncLatch.countDown();
    Looper.loop();
  }
  
  public void sendMessage(Message paramMessage, int paramInt)
  {
    try
    {
      this.syncLatch.await();
      if (paramInt <= 0)
      {
        this.handler.sendMessage(paramMessage);
        return;
      }
      this.handler.sendMessageDelayed(paramMessage, paramInt);
      return;
    }
    catch (Exception paramMessage)
    {
      FileLog.e(paramMessage);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/DispatchQueue.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */