package ir.eitaa.messenger.exoplayer.dash.mpd;

public final class UtcTimingElement
{
  public final String schemeIdUri;
  public final String value;
  
  public UtcTimingElement(String paramString1, String paramString2)
  {
    this.schemeIdUri = paramString1;
    this.value = paramString2;
  }
  
  public String toString()
  {
    return this.schemeIdUri + ", " + this.value;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/dash/mpd/UtcTimingElement.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */