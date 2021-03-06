package org.telegram.messenger.exoplayer2.source.hls.playlist;

import java.util.Collections;
import java.util.List;

public abstract class HlsPlaylist
{
  public final String baseUri;
  public final List<String> tags;
  
  protected HlsPlaylist(String paramString, List<String> paramList)
  {
    this.baseUri = paramString;
    this.tags = Collections.unmodifiableList(paramList);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/playlist/HlsPlaylist.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */