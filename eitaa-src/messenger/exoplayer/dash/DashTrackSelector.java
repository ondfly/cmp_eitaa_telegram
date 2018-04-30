package ir.eitaa.messenger.exoplayer.dash;

import ir.eitaa.messenger.exoplayer.dash.mpd.MediaPresentationDescription;
import java.io.IOException;

public abstract interface DashTrackSelector
{
  public abstract void selectTracks(MediaPresentationDescription paramMediaPresentationDescription, int paramInt, Output paramOutput)
    throws IOException;
  
  public static abstract interface Output
  {
    public abstract void adaptiveTrack(MediaPresentationDescription paramMediaPresentationDescription, int paramInt1, int paramInt2, int[] paramArrayOfInt);
    
    public abstract void fixedTrack(MediaPresentationDescription paramMediaPresentationDescription, int paramInt1, int paramInt2, int paramInt3);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/dash/DashTrackSelector.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */