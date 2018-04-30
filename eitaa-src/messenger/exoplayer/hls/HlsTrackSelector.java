package ir.eitaa.messenger.exoplayer.hls;

import java.io.IOException;

public abstract interface HlsTrackSelector
{
  public abstract void selectTracks(HlsMasterPlaylist paramHlsMasterPlaylist, Output paramOutput)
    throws IOException;
  
  public static abstract interface Output
  {
    public abstract void adaptiveTrack(HlsMasterPlaylist paramHlsMasterPlaylist, Variant[] paramArrayOfVariant);
    
    public abstract void fixedTrack(HlsMasterPlaylist paramHlsMasterPlaylist, Variant paramVariant);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/hls/HlsTrackSelector.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */