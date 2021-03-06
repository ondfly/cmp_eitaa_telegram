package org.telegram.messenger.exoplayer2;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.source.SampleStream;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.MediaClock;

public abstract class NoSampleRenderer
  implements Renderer, RendererCapabilities
{
  private RendererConfiguration configuration;
  private int index;
  private int state;
  private SampleStream stream;
  private boolean streamIsFinal;
  
  public final void disable()
  {
    boolean bool = true;
    if (this.state == 1) {}
    for (;;)
    {
      Assertions.checkState(bool);
      this.state = 0;
      this.stream = null;
      this.streamIsFinal = false;
      onDisabled();
      return;
      bool = false;
    }
  }
  
  public final void enable(RendererConfiguration paramRendererConfiguration, Format[] paramArrayOfFormat, SampleStream paramSampleStream, long paramLong1, boolean paramBoolean, long paramLong2)
    throws ExoPlaybackException
  {
    if (this.state == 0) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      this.configuration = paramRendererConfiguration;
      this.state = 1;
      onEnabled(paramBoolean);
      replaceStream(paramArrayOfFormat, paramSampleStream, paramLong2);
      onPositionReset(paramLong1, paramBoolean);
      return;
    }
  }
  
  public final RendererCapabilities getCapabilities()
  {
    return this;
  }
  
  protected final RendererConfiguration getConfiguration()
  {
    return this.configuration;
  }
  
  protected final int getIndex()
  {
    return this.index;
  }
  
  public MediaClock getMediaClock()
  {
    return null;
  }
  
  public final int getState()
  {
    return this.state;
  }
  
  public final SampleStream getStream()
  {
    return this.stream;
  }
  
  public final int getTrackType()
  {
    return 5;
  }
  
  public void handleMessage(int paramInt, Object paramObject)
    throws ExoPlaybackException
  {}
  
  public final boolean hasReadStreamToEnd()
  {
    return true;
  }
  
  public final boolean isCurrentStreamFinal()
  {
    return this.streamIsFinal;
  }
  
  public boolean isEnded()
  {
    return true;
  }
  
  public boolean isReady()
  {
    return true;
  }
  
  public final void maybeThrowStreamError()
    throws IOException
  {}
  
  protected void onDisabled() {}
  
  protected void onEnabled(boolean paramBoolean)
    throws ExoPlaybackException
  {}
  
  protected void onPositionReset(long paramLong, boolean paramBoolean)
    throws ExoPlaybackException
  {}
  
  protected void onRendererOffsetChanged(long paramLong)
    throws ExoPlaybackException
  {}
  
  protected void onStarted()
    throws ExoPlaybackException
  {}
  
  protected void onStopped()
    throws ExoPlaybackException
  {}
  
  public final void replaceStream(Format[] paramArrayOfFormat, SampleStream paramSampleStream, long paramLong)
    throws ExoPlaybackException
  {
    if (!this.streamIsFinal) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      this.stream = paramSampleStream;
      onRendererOffsetChanged(paramLong);
      return;
    }
  }
  
  public final void resetPosition(long paramLong)
    throws ExoPlaybackException
  {
    this.streamIsFinal = false;
    onPositionReset(paramLong, false);
  }
  
  public final void setCurrentStreamFinal()
  {
    this.streamIsFinal = true;
  }
  
  public final void setIndex(int paramInt)
  {
    this.index = paramInt;
  }
  
  public final void start()
    throws ExoPlaybackException
  {
    boolean bool = true;
    if (this.state == 1) {}
    for (;;)
    {
      Assertions.checkState(bool);
      this.state = 2;
      onStarted();
      return;
      bool = false;
    }
  }
  
  public final void stop()
    throws ExoPlaybackException
  {
    if (this.state == 2) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      this.state = 1;
      onStopped();
      return;
    }
  }
  
  public int supportsFormat(Format paramFormat)
    throws ExoPlaybackException
  {
    return 0;
  }
  
  public int supportsMixedMimeTypeAdaptation()
    throws ExoPlaybackException
  {
    return 0;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/NoSampleRenderer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */