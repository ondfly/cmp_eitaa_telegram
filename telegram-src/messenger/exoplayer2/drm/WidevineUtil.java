package org.telegram.messenger.exoplayer2.drm;

import android.util.Pair;
import java.util.Map;

public final class WidevineUtil
{
  public static final String PROPERTY_LICENSE_DURATION_REMAINING = "LicenseDurationRemaining";
  public static final String PROPERTY_PLAYBACK_DURATION_REMAINING = "PlaybackDurationRemaining";
  
  private static long getDurationRemainingSec(Map<String, String> paramMap, String paramString)
  {
    if (paramMap != null) {
      try
      {
        paramMap = (String)paramMap.get(paramString);
        if (paramMap != null)
        {
          long l = Long.parseLong(paramMap);
          return l;
        }
      }
      catch (NumberFormatException paramMap) {}
    }
    return -9223372036854775807L;
  }
  
  public static Pair<Long, Long> getLicenseDurationRemainingSec(DrmSession<?> paramDrmSession)
  {
    paramDrmSession = paramDrmSession.queryKeyStatus();
    if (paramDrmSession == null) {
      return null;
    }
    return new Pair(Long.valueOf(getDurationRemainingSec(paramDrmSession, "LicenseDurationRemaining")), Long.valueOf(getDurationRemainingSec(paramDrmSession, "PlaybackDurationRemaining")));
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/drm/WidevineUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */