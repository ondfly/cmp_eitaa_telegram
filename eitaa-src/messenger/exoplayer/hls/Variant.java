package ir.eitaa.messenger.exoplayer.hls;

import ir.eitaa.messenger.exoplayer.chunk.Format;
import ir.eitaa.messenger.exoplayer.chunk.FormatWrapper;

public final class Variant
  implements FormatWrapper
{
  public final Format format;
  public final String url;
  
  public Variant(String paramString, Format paramFormat)
  {
    this.url = paramString;
    this.format = paramFormat;
  }
  
  public Format getFormat()
  {
    return this.format;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/hls/Variant.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */