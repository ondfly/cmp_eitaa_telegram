package ir.eitaa.messenger.exoplayer.hls;

import java.util.Collections;
import java.util.List;

public final class HlsMasterPlaylist
  extends HlsPlaylist
{
  public final List<Variant> audios;
  public final String muxedAudioLanguage;
  public final String muxedCaptionLanguage;
  public final List<Variant> subtitles;
  public final List<Variant> variants;
  
  public HlsMasterPlaylist(String paramString1, List<Variant> paramList1, List<Variant> paramList2, List<Variant> paramList3, String paramString2, String paramString3)
  {
    super(paramString1, 0);
    this.variants = Collections.unmodifiableList(paramList1);
    this.audios = Collections.unmodifiableList(paramList2);
    this.subtitles = Collections.unmodifiableList(paramList3);
    this.muxedAudioLanguage = paramString2;
    this.muxedCaptionLanguage = paramString3;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/hls/HlsMasterPlaylist.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */