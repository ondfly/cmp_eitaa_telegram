package org.telegram.messenger.exoplayer2.source.hls;

import org.telegram.messenger.exoplayer2.source.hls.playlist.HlsMasterPlaylist;
import org.telegram.messenger.exoplayer2.source.hls.playlist.HlsMediaPlaylist;

public final class HlsManifest
{
  public final HlsMasterPlaylist masterPlaylist;
  public final HlsMediaPlaylist mediaPlaylist;
  
  HlsManifest(HlsMasterPlaylist paramHlsMasterPlaylist, HlsMediaPlaylist paramHlsMediaPlaylist)
  {
    this.masterPlaylist = paramHlsMasterPlaylist;
    this.mediaPlaylist = paramHlsMediaPlaylist;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/HlsManifest.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */