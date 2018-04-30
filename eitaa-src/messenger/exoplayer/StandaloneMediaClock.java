package ir.eitaa.messenger.exoplayer;

import android.os.SystemClock;

final class StandaloneMediaClock
  implements MediaClock
{
  private long deltaUs;
  private long positionUs;
  private boolean started;
  
  private long elapsedRealtimeMinus(long paramLong)
  {
    return SystemClock.elapsedRealtime() * 1000L - paramLong;
  }
  
  public long getPositionUs()
  {
    if (this.started) {
      return elapsedRealtimeMinus(this.deltaUs);
    }
    return this.positionUs;
  }
  
  public void setPositionUs(long paramLong)
  {
    this.positionUs = paramLong;
    this.deltaUs = elapsedRealtimeMinus(paramLong);
  }
  
  public void start()
  {
    if (!this.started)
    {
      this.started = true;
      this.deltaUs = elapsedRealtimeMinus(this.positionUs);
    }
  }
  
  public void stop()
  {
    if (this.started)
    {
      this.positionUs = elapsedRealtimeMinus(this.deltaUs);
      this.started = false;
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/StandaloneMediaClock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */