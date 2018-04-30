package org.telegram.messenger.exoplayer2.source.chunk;

import android.util.Log;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException;

public final class ChunkedTrackBlacklistUtil
{
  public static final long DEFAULT_TRACK_BLACKLIST_MS = 60000L;
  private static final String TAG = "ChunkedTrackBlacklist";
  
  public static boolean maybeBlacklistTrack(TrackSelection paramTrackSelection, int paramInt, Exception paramException)
  {
    return maybeBlacklistTrack(paramTrackSelection, paramInt, paramException, 60000L);
  }
  
  public static boolean maybeBlacklistTrack(TrackSelection paramTrackSelection, int paramInt, Exception paramException, long paramLong)
  {
    if (shouldBlacklist(paramException))
    {
      boolean bool = paramTrackSelection.blacklist(paramInt, paramLong);
      int i = ((HttpDataSource.InvalidResponseCodeException)paramException).responseCode;
      if (bool)
      {
        Log.w("ChunkedTrackBlacklist", "Blacklisted: duration=" + paramLong + ", responseCode=" + i + ", format=" + paramTrackSelection.getFormat(paramInt));
        return bool;
      }
      Log.w("ChunkedTrackBlacklist", "Blacklisting failed (cannot blacklist last enabled track): responseCode=" + i + ", format=" + paramTrackSelection.getFormat(paramInt));
      return bool;
    }
    return false;
  }
  
  public static boolean shouldBlacklist(Exception paramException)
  {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if ((paramException instanceof HttpDataSource.InvalidResponseCodeException))
    {
      int i = ((HttpDataSource.InvalidResponseCodeException)paramException).responseCode;
      if (i != 404)
      {
        bool1 = bool2;
        if (i != 410) {}
      }
      else
      {
        bool1 = true;
      }
    }
    return bool1;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/chunk/ChunkedTrackBlacklistUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */