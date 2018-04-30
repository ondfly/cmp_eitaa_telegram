package ir.eitaa.messenger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.CountDownLatch;

public class DispatchQueue
  extends Thread
{
  public volatile Handler handler = null;
  private CountDownLatch syncLatch = new CountDownLatch(1);
  
  public DispatchQueue(String paramString)
  {
    setName(paramString);
    start();
  }
  
  private void sendMessage(Message paramMessage, int paramInt)
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
      FileLog.e("TSMS", paramMessage);
    }
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
      FileLog.e("TSMS", paramRunnable);
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
      FileLog.e("TSMS", localException);
    }
  }
  
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
      FileLog.e("TSMS", paramRunnable);
    }
  }
  
  public void run()
  {
    Looper.prepare();
    this.handler = new Handler();
    this.syncLatch.countDown();
    Looper.loop();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/DispatchQueue.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */