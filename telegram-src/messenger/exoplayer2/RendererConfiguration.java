package org.telegram.messenger.exoplayer2;

public final class RendererConfiguration
{
  public static final RendererConfiguration DEFAULT = new RendererConfiguration(0);
  public final int tunnelingAudioSessionId;
  
  public RendererConfiguration(int paramInt)
  {
    this.tunnelingAudioSessionId = paramInt;
  }
  
  public boolean equals(Object paramObject)
  {
    if (this == paramObject) {}
    do
    {
      return true;
      if ((paramObject == null) || (getClass() != paramObject.getClass())) {
        return false;
      }
      paramObject = (RendererConfiguration)paramObject;
    } while (this.tunnelingAudioSessionId == ((RendererConfiguration)paramObject).tunnelingAudioSessionId);
    return false;
  }
  
  public int hashCode()
  {
    return this.tunnelingAudioSessionId;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/RendererConfiguration.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */