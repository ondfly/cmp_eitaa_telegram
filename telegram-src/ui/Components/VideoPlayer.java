package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.view.TextureView;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.exoplayer2.DefaultLoadControl;
import org.telegram.messenger.exoplayer2.ExoPlaybackException;
import org.telegram.messenger.exoplayer2.ExoPlayer.EventListener;
import org.telegram.messenger.exoplayer2.ExoPlayerFactory;
import org.telegram.messenger.exoplayer2.PlaybackParameters;
import org.telegram.messenger.exoplayer2.Player.EventListener;
import org.telegram.messenger.exoplayer2.SimpleExoPlayer;
import org.telegram.messenger.exoplayer2.SimpleExoPlayer.VideoListener;
import org.telegram.messenger.exoplayer2.Timeline;
import org.telegram.messenger.exoplayer2.extractor.DefaultExtractorsFactory;
import org.telegram.messenger.exoplayer2.source.ExtractorMediaSource;
import org.telegram.messenger.exoplayer2.source.LoopingMediaSource;
import org.telegram.messenger.exoplayer2.source.MediaSource;
import org.telegram.messenger.exoplayer2.source.TrackGroupArray;
import org.telegram.messenger.exoplayer2.source.dash.DashMediaSource;
import org.telegram.messenger.exoplayer2.source.dash.DefaultDashChunkSource.Factory;
import org.telegram.messenger.exoplayer2.source.hls.HlsMediaSource;
import org.telegram.messenger.exoplayer2.source.smoothstreaming.DefaultSsChunkSource.Factory;
import org.telegram.messenger.exoplayer2.source.smoothstreaming.SsMediaSource;
import org.telegram.messenger.exoplayer2.trackselection.AdaptiveTrackSelection.Factory;
import org.telegram.messenger.exoplayer2.trackselection.DefaultTrackSelector;
import org.telegram.messenger.exoplayer2.trackselection.MappingTrackSelector;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelectionArray;
import org.telegram.messenger.exoplayer2.upstream.DataSource.Factory;
import org.telegram.messenger.exoplayer2.upstream.DefaultBandwidthMeter;
import org.telegram.messenger.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import org.telegram.messenger.secretmedia.ExtendedDefaultDataSourceFactory;

@SuppressLint({"NewApi"})
public class VideoPlayer
  implements NotificationCenter.NotificationCenterDelegate, ExoPlayer.EventListener, SimpleExoPlayer.VideoListener
{
  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
  private static final int RENDERER_BUILDING_STATE_BUILT = 3;
  private static final int RENDERER_BUILDING_STATE_IDLE = 1;
  private SimpleExoPlayer audioPlayer;
  private boolean audioPlayerReady;
  private boolean autoplay;
  private VideoPlayerDelegate delegate;
  private boolean isStreaming;
  private boolean lastReportedPlayWhenReady;
  private int lastReportedPlaybackState = 1;
  private Handler mainHandler = new Handler();
  private DataSource.Factory mediaDataSourceFactory = new ExtendedDefaultDataSourceFactory(ApplicationLoader.applicationContext, BANDWIDTH_METER, new DefaultHttpDataSourceFactory("Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)", BANDWIDTH_METER));
  private boolean mixedAudio;
  private boolean mixedPlayWhenReady;
  private SimpleExoPlayer player;
  private TextureView textureView;
  private MappingTrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(BANDWIDTH_METER));
  private boolean videoPlayerReady;
  
  public VideoPlayer()
  {
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.playerDidStartPlaying);
  }
  
  private void checkPlayersReady()
  {
    if ((this.audioPlayerReady) && (this.videoPlayerReady) && (this.mixedPlayWhenReady)) {
      play();
    }
  }
  
  private void ensurePleyaerCreated()
  {
    if (this.player == null)
    {
      this.player = ExoPlayerFactory.newSimpleInstance(ApplicationLoader.applicationContext, this.trackSelector, new DefaultLoadControl(), null, 2);
      this.player.addListener(this);
      this.player.setVideoListener(this);
      this.player.setVideoTextureView(this.textureView);
      this.player.setPlayWhenReady(this.autoplay);
    }
    if ((this.mixedAudio) && (this.audioPlayer == null))
    {
      this.audioPlayer = ExoPlayerFactory.newSimpleInstance(ApplicationLoader.applicationContext, this.trackSelector, new DefaultLoadControl(), null, 2);
      this.audioPlayer.addListener(new Player.EventListener()
      {
        public void onLoadingChanged(boolean paramAnonymousBoolean) {}
        
        public void onPlaybackParametersChanged(PlaybackParameters paramAnonymousPlaybackParameters) {}
        
        public void onPlayerError(ExoPlaybackException paramAnonymousExoPlaybackException) {}
        
        public void onPlayerStateChanged(boolean paramAnonymousBoolean, int paramAnonymousInt)
        {
          if ((!VideoPlayer.this.audioPlayerReady) && (paramAnonymousInt == 3))
          {
            VideoPlayer.access$002(VideoPlayer.this, true);
            VideoPlayer.this.checkPlayersReady();
          }
        }
        
        public void onPositionDiscontinuity(int paramAnonymousInt) {}
        
        public void onRepeatModeChanged(int paramAnonymousInt) {}
        
        public void onSeekProcessed() {}
        
        public void onShuffleModeEnabledChanged(boolean paramAnonymousBoolean) {}
        
        public void onTimelineChanged(Timeline paramAnonymousTimeline, Object paramAnonymousObject, int paramAnonymousInt) {}
        
        public void onTracksChanged(TrackGroupArray paramAnonymousTrackGroupArray, TrackSelectionArray paramAnonymousTrackSelectionArray) {}
      });
      this.audioPlayer.setPlayWhenReady(this.autoplay);
    }
  }
  
  private void maybeReportPlayerState()
  {
    boolean bool = this.player.getPlayWhenReady();
    int i = this.player.getPlaybackState();
    if ((this.lastReportedPlayWhenReady != bool) || (this.lastReportedPlaybackState != i))
    {
      this.delegate.onStateChanged(bool, i);
      this.lastReportedPlayWhenReady = bool;
      this.lastReportedPlaybackState = i;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if ((paramInt1 == NotificationCenter.playerDidStartPlaying) && ((VideoPlayer)paramVarArgs[0] != this) && (isPlaying())) {
      pause();
    }
  }
  
  public int getBufferedPercentage()
  {
    if (this.isStreaming)
    {
      if (this.player != null) {
        return this.player.getBufferedPercentage();
      }
      return 0;
    }
    return 100;
  }
  
  public long getBufferedPosition()
  {
    if (this.player != null)
    {
      if (this.isStreaming) {
        return this.player.getBufferedPosition();
      }
      return this.player.getDuration();
    }
    return 0L;
  }
  
  public long getCurrentPosition()
  {
    if (this.player != null) {
      return this.player.getCurrentPosition();
    }
    return 0L;
  }
  
  public long getDuration()
  {
    if (this.player != null) {
      return this.player.getDuration();
    }
    return 0L;
  }
  
  public boolean isBuffering()
  {
    return (this.player != null) && (this.lastReportedPlaybackState == 2);
  }
  
  public boolean isMuted()
  {
    return this.player.getVolume() == 0.0F;
  }
  
  public boolean isPlayerPrepared()
  {
    return this.player != null;
  }
  
  public boolean isPlaying()
  {
    return ((this.mixedAudio) && (this.mixedPlayWhenReady)) || ((this.player != null) && (this.player.getPlayWhenReady()));
  }
  
  public boolean isStreaming()
  {
    return this.isStreaming;
  }
  
  public void onLoadingChanged(boolean paramBoolean) {}
  
  public void onPlaybackParametersChanged(PlaybackParameters paramPlaybackParameters) {}
  
  public void onPlayerError(ExoPlaybackException paramExoPlaybackException)
  {
    this.delegate.onError(paramExoPlaybackException);
  }
  
  public void onPlayerStateChanged(boolean paramBoolean, int paramInt)
  {
    maybeReportPlayerState();
    if ((paramBoolean) && (paramInt == 3)) {
      NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.playerDidStartPlaying, new Object[] { this });
    }
    if ((!this.videoPlayerReady) && (paramInt == 3))
    {
      this.videoPlayerReady = true;
      checkPlayersReady();
    }
  }
  
  public void onPositionDiscontinuity(int paramInt) {}
  
  public void onRenderedFirstFrame()
  {
    this.delegate.onRenderedFirstFrame();
  }
  
  public void onRepeatModeChanged(int paramInt) {}
  
  public void onSeekProcessed() {}
  
  public void onShuffleModeEnabledChanged(boolean paramBoolean) {}
  
  public boolean onSurfaceDestroyed(SurfaceTexture paramSurfaceTexture)
  {
    return this.delegate.onSurfaceDestroyed(paramSurfaceTexture);
  }
  
  public void onSurfaceTextureUpdated(SurfaceTexture paramSurfaceTexture)
  {
    this.delegate.onSurfaceTextureUpdated(paramSurfaceTexture);
  }
  
  public void onTimelineChanged(Timeline paramTimeline, Object paramObject, int paramInt) {}
  
  public void onTracksChanged(TrackGroupArray paramTrackGroupArray, TrackSelectionArray paramTrackSelectionArray) {}
  
  public void onVideoSizeChanged(int paramInt1, int paramInt2, int paramInt3, float paramFloat)
  {
    this.delegate.onVideoSizeChanged(paramInt1, paramInt2, paramInt3, paramFloat);
  }
  
  public void pause()
  {
    this.mixedPlayWhenReady = false;
    if (this.player != null) {
      this.player.setPlayWhenReady(false);
    }
    if (this.audioPlayer != null) {
      this.audioPlayer.setPlayWhenReady(false);
    }
  }
  
  public void play()
  {
    this.mixedPlayWhenReady = true;
    if ((this.mixedAudio) && ((!this.audioPlayerReady) || (!this.videoPlayerReady)))
    {
      if (this.player != null) {
        this.player.setPlayWhenReady(false);
      }
      if (this.audioPlayer != null) {
        this.audioPlayer.setPlayWhenReady(false);
      }
    }
    do
    {
      return;
      if (this.player != null) {
        this.player.setPlayWhenReady(true);
      }
    } while (this.audioPlayer == null);
    this.audioPlayer.setPlayWhenReady(true);
  }
  
  public void preparePlayer(Uri paramUri, String paramString)
  {
    int i = 0;
    this.videoPlayerReady = false;
    this.mixedAudio = false;
    String str = paramUri.getScheme();
    boolean bool;
    if ((str != null) && (!str.startsWith("file")))
    {
      bool = true;
      this.isStreaming = bool;
      ensurePleyaerCreated();
      switch (paramString.hashCode())
      {
      default: 
        label84:
        i = -1;
        switch (i)
        {
        default: 
          label86:
          paramUri = new ExtractorMediaSource(paramUri, this.mediaDataSourceFactory, new DefaultExtractorsFactory(), this.mainHandler, null);
        }
        break;
      }
    }
    for (;;)
    {
      this.player.prepare(paramUri, true, true);
      return;
      bool = false;
      break;
      if (!paramString.equals("dash")) {
        break label84;
      }
      break label86;
      if (!paramString.equals("hls")) {
        break label84;
      }
      i = 1;
      break label86;
      if (!paramString.equals("ss")) {
        break label84;
      }
      i = 2;
      break label86;
      paramUri = new DashMediaSource(paramUri, this.mediaDataSourceFactory, new DefaultDashChunkSource.Factory(this.mediaDataSourceFactory), this.mainHandler, null);
      continue;
      paramUri = new HlsMediaSource(paramUri, this.mediaDataSourceFactory, this.mainHandler, null);
      continue;
      paramUri = new SsMediaSource(paramUri, this.mediaDataSourceFactory, new DefaultSsChunkSource.Factory(this.mediaDataSourceFactory), this.mainHandler, null);
    }
  }
  
  public void preparePlayerLoop(Uri paramUri1, String paramString1, Uri paramUri2, String paramString2)
  {
    this.mixedAudio = true;
    this.audioPlayerReady = false;
    this.videoPlayerReady = false;
    ensurePleyaerCreated();
    Object localObject2 = null;
    Object localObject3 = null;
    int j = 0;
    if (j < 2)
    {
      String str;
      Object localObject1;
      label45:
      int i;
      if (j == 0)
      {
        str = paramString1;
        localObject1 = paramUri1;
        i = -1;
        switch (str.hashCode())
        {
        default: 
          switch (i)
          {
          default: 
            label88:
            localObject1 = new ExtractorMediaSource((Uri)localObject1, this.mediaDataSourceFactory, new DefaultExtractorsFactory(), this.mainHandler, null);
            label143:
            localObject1 = new LoopingMediaSource((MediaSource)localObject1);
            if (j != 0) {}
            break;
          }
          break;
        }
      }
      for (;;)
      {
        j += 1;
        localObject2 = localObject1;
        break;
        str = paramString2;
        localObject1 = paramUri2;
        break label45;
        if (!str.equals("dash")) {
          break label88;
        }
        i = 0;
        break label88;
        if (!str.equals("hls")) {
          break label88;
        }
        i = 1;
        break label88;
        if (!str.equals("ss")) {
          break label88;
        }
        i = 2;
        break label88;
        localObject1 = new DashMediaSource((Uri)localObject1, this.mediaDataSourceFactory, new DefaultDashChunkSource.Factory(this.mediaDataSourceFactory), this.mainHandler, null);
        break label143;
        localObject1 = new HlsMediaSource((Uri)localObject1, this.mediaDataSourceFactory, this.mainHandler, null);
        break label143;
        localObject1 = new SsMediaSource((Uri)localObject1, this.mediaDataSourceFactory, new DefaultSsChunkSource.Factory(this.mediaDataSourceFactory), this.mainHandler, null);
        break label143;
        localObject3 = localObject1;
        localObject1 = localObject2;
      }
    }
    this.player.prepare((MediaSource)localObject2, true, true);
    this.audioPlayer.prepare((MediaSource)localObject3, true, true);
  }
  
  public void releasePlayer()
  {
    if (this.player != null)
    {
      this.player.release();
      this.player = null;
    }
    if (this.audioPlayer != null)
    {
      this.audioPlayer.release();
      this.audioPlayer = null;
    }
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.playerDidStartPlaying);
  }
  
  public void seekTo(long paramLong)
  {
    if (this.player != null) {
      this.player.seekTo(paramLong);
    }
  }
  
  public void setDelegate(VideoPlayerDelegate paramVideoPlayerDelegate)
  {
    this.delegate = paramVideoPlayerDelegate;
  }
  
  public void setMute(boolean paramBoolean)
  {
    float f2 = 0.0F;
    SimpleExoPlayer localSimpleExoPlayer;
    if (this.player != null)
    {
      localSimpleExoPlayer = this.player;
      if (paramBoolean)
      {
        f1 = 0.0F;
        localSimpleExoPlayer.setVolume(f1);
      }
    }
    else if (this.audioPlayer != null)
    {
      localSimpleExoPlayer = this.audioPlayer;
      if (!paramBoolean) {
        break label58;
      }
    }
    label58:
    for (float f1 = f2;; f1 = 1.0F)
    {
      localSimpleExoPlayer.setVolume(f1);
      return;
      f1 = 1.0F;
      break;
    }
  }
  
  public void setPlayWhenReady(boolean paramBoolean)
  {
    this.mixedPlayWhenReady = paramBoolean;
    if ((paramBoolean) && (this.mixedAudio) && ((!this.audioPlayerReady) || (!this.videoPlayerReady)))
    {
      if (this.player != null) {
        this.player.setPlayWhenReady(false);
      }
      if (this.audioPlayer != null) {
        this.audioPlayer.setPlayWhenReady(false);
      }
    }
    do
    {
      return;
      this.autoplay = paramBoolean;
      if (this.player != null) {
        this.player.setPlayWhenReady(paramBoolean);
      }
    } while (this.audioPlayer == null);
    this.audioPlayer.setPlayWhenReady(paramBoolean);
  }
  
  public void setStreamType(int paramInt)
  {
    if (this.player != null) {
      this.player.setAudioStreamType(paramInt);
    }
    if (this.audioPlayer != null) {
      this.audioPlayer.setAudioStreamType(paramInt);
    }
  }
  
  public void setTextureView(TextureView paramTextureView)
  {
    if (this.textureView == paramTextureView) {}
    do
    {
      return;
      this.textureView = paramTextureView;
    } while (this.player == null);
    this.player.setVideoTextureView(this.textureView);
  }
  
  public void setVolume(float paramFloat)
  {
    if (this.player != null) {
      this.player.setVolume(paramFloat);
    }
    if (this.audioPlayer != null) {
      this.audioPlayer.setVolume(paramFloat);
    }
  }
  
  public static abstract interface RendererBuilder
  {
    public abstract void buildRenderers(VideoPlayer paramVideoPlayer);
    
    public abstract void cancel();
  }
  
  public static abstract interface VideoPlayerDelegate
  {
    public abstract void onError(Exception paramException);
    
    public abstract void onRenderedFirstFrame();
    
    public abstract void onStateChanged(boolean paramBoolean, int paramInt);
    
    public abstract boolean onSurfaceDestroyed(SurfaceTexture paramSurfaceTexture);
    
    public abstract void onSurfaceTextureUpdated(SurfaceTexture paramSurfaceTexture);
    
    public abstract void onVideoSizeChanged(int paramInt1, int paramInt2, int paramInt3, float paramFloat);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/VideoPlayer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */