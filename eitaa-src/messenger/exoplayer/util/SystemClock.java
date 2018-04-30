package ir.eitaa.messenger.exoplayer.util;

public final class SystemClock
  implements Clock
{
  public long elapsedRealtime()
  {
    return android.os.SystemClock.elapsedRealtime();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/util/SystemClock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */