package org.telegram.messenger.exoplayer2.util;

import org.telegram.messenger.exoplayer2.PlaybackParameters;

public abstract interface MediaClock
{
  public abstract PlaybackParameters getPlaybackParameters();
  
  public abstract long getPositionUs();
  
  public abstract PlaybackParameters setPlaybackParameters(PlaybackParameters paramPlaybackParameters);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/util/MediaClock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */