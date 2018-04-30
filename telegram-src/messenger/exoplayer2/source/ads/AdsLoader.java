package org.telegram.messenger.exoplayer2.source.ads;

import android.view.ViewGroup;
import java.io.IOException;
import org.telegram.messenger.exoplayer2.ExoPlayer;

public abstract interface AdsLoader
{
  public abstract void attachPlayer(ExoPlayer paramExoPlayer, EventListener paramEventListener, ViewGroup paramViewGroup);
  
  public abstract void detachPlayer();
  
  public abstract void release();
  
  public abstract void setSupportedContentTypes(int... paramVarArgs);
  
  public static abstract interface EventListener
  {
    public abstract void onAdClicked();
    
    public abstract void onAdPlaybackState(AdPlaybackState paramAdPlaybackState);
    
    public abstract void onAdTapped();
    
    public abstract void onLoadError(IOException paramIOException);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/ads/AdsLoader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */