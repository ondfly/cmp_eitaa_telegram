package org.telegram.messenger.exoplayer2.audio;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Handler;
import java.nio.ByteBuffer;
import org.telegram.messenger.exoplayer2.ExoPlaybackException;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.PlaybackParameters;
import org.telegram.messenger.exoplayer2.RendererConfiguration;
import org.telegram.messenger.exoplayer2.decoder.DecoderCounters;
import org.telegram.messenger.exoplayer2.drm.DrmInitData;
import org.telegram.messenger.exoplayer2.drm.DrmInitData.SchemeData;
import org.telegram.messenger.exoplayer2.drm.DrmSessionManager;
import org.telegram.messenger.exoplayer2.drm.FrameworkMediaCrypto;
import org.telegram.messenger.exoplayer2.mediacodec.MediaCodecInfo;
import org.telegram.messenger.exoplayer2.mediacodec.MediaCodecRenderer;
import org.telegram.messenger.exoplayer2.mediacodec.MediaCodecSelector;
import org.telegram.messenger.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import org.telegram.messenger.exoplayer2.util.MediaClock;
import org.telegram.messenger.exoplayer2.util.MimeTypes;
import org.telegram.messenger.exoplayer2.util.Util;

@TargetApi(16)
public class MediaCodecAudioRenderer
  extends MediaCodecRenderer
  implements MediaClock
{
  private boolean allowPositionDiscontinuity;
  private final AudioSink audioSink;
  private int channelCount;
  private boolean codecNeedsDiscardChannelsWorkaround;
  private long currentPositionUs;
  private int encoderDelay;
  private int encoderPadding;
  private final AudioRendererEventListener.EventDispatcher eventDispatcher;
  private boolean passthroughEnabled;
  private MediaFormat passthroughMediaFormat;
  private int pcmEncoding;
  
  public MediaCodecAudioRenderer(MediaCodecSelector paramMediaCodecSelector)
  {
    this(paramMediaCodecSelector, null, true);
  }
  
  public MediaCodecAudioRenderer(MediaCodecSelector paramMediaCodecSelector, Handler paramHandler, AudioRendererEventListener paramAudioRendererEventListener)
  {
    this(paramMediaCodecSelector, null, true, paramHandler, paramAudioRendererEventListener);
  }
  
  public MediaCodecAudioRenderer(MediaCodecSelector paramMediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, boolean paramBoolean)
  {
    this(paramMediaCodecSelector, paramDrmSessionManager, paramBoolean, null, null);
  }
  
  public MediaCodecAudioRenderer(MediaCodecSelector paramMediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, boolean paramBoolean, Handler paramHandler, AudioRendererEventListener paramAudioRendererEventListener)
  {
    this(paramMediaCodecSelector, paramDrmSessionManager, paramBoolean, paramHandler, paramAudioRendererEventListener, (AudioCapabilities)null, new AudioProcessor[0]);
  }
  
  public MediaCodecAudioRenderer(MediaCodecSelector paramMediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, boolean paramBoolean, Handler paramHandler, AudioRendererEventListener paramAudioRendererEventListener, AudioCapabilities paramAudioCapabilities, AudioProcessor... paramVarArgs)
  {
    this(paramMediaCodecSelector, paramDrmSessionManager, paramBoolean, paramHandler, paramAudioRendererEventListener, new DefaultAudioSink(paramAudioCapabilities, paramVarArgs));
  }
  
  public MediaCodecAudioRenderer(MediaCodecSelector paramMediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, boolean paramBoolean, Handler paramHandler, AudioRendererEventListener paramAudioRendererEventListener, AudioSink paramAudioSink)
  {
    super(1, paramMediaCodecSelector, paramDrmSessionManager, paramBoolean);
    this.eventDispatcher = new AudioRendererEventListener.EventDispatcher(paramHandler, paramAudioRendererEventListener);
    this.audioSink = paramAudioSink;
    paramAudioSink.setListener(new AudioSinkListener(null));
  }
  
  private static boolean codecNeedsDiscardChannelsWorkaround(String paramString)
  {
    return (Util.SDK_INT < 24) && ("OMX.SEC.aac.dec".equals(paramString)) && ("samsung".equals(Util.MANUFACTURER)) && ((Util.DEVICE.startsWith("zeroflte")) || (Util.DEVICE.startsWith("herolte")) || (Util.DEVICE.startsWith("heroqlte")));
  }
  
  private void updateCurrentPosition()
  {
    long l = this.audioSink.getCurrentPositionUs(isEnded());
    if (l != Long.MIN_VALUE) {
      if (!this.allowPositionDiscontinuity) {
        break label40;
      }
    }
    for (;;)
    {
      this.currentPositionUs = l;
      this.allowPositionDiscontinuity = false;
      return;
      label40:
      l = Math.max(this.currentPositionUs, l);
    }
  }
  
  protected boolean allowPassthrough(String paramString)
  {
    int i = MimeTypes.getEncoding(paramString);
    return (i != 0) && (this.audioSink.isEncodingSupported(i));
  }
  
  protected void configureCodec(MediaCodecInfo paramMediaCodecInfo, MediaCodec paramMediaCodec, Format paramFormat, MediaCrypto paramMediaCrypto)
  {
    this.codecNeedsDiscardChannelsWorkaround = codecNeedsDiscardChannelsWorkaround(paramMediaCodecInfo.name);
    paramMediaCodecInfo = getMediaFormatForPlayback(paramFormat);
    if (this.passthroughEnabled)
    {
      this.passthroughMediaFormat = paramMediaCodecInfo;
      this.passthroughMediaFormat.setString("mime", "audio/raw");
      paramMediaCodec.configure(this.passthroughMediaFormat, null, paramMediaCrypto, 0);
      this.passthroughMediaFormat.setString("mime", paramFormat.sampleMimeType);
      return;
    }
    paramMediaCodec.configure(paramMediaCodecInfo, null, paramMediaCrypto, 0);
    this.passthroughMediaFormat = null;
  }
  
  protected MediaCodecInfo getDecoderInfo(MediaCodecSelector paramMediaCodecSelector, Format paramFormat, boolean paramBoolean)
    throws MediaCodecUtil.DecoderQueryException
  {
    if (allowPassthrough(paramFormat.sampleMimeType))
    {
      MediaCodecInfo localMediaCodecInfo = paramMediaCodecSelector.getPassthroughDecoderInfo();
      if (localMediaCodecInfo != null)
      {
        this.passthroughEnabled = true;
        return localMediaCodecInfo;
      }
    }
    this.passthroughEnabled = false;
    return super.getDecoderInfo(paramMediaCodecSelector, paramFormat, paramBoolean);
  }
  
  public MediaClock getMediaClock()
  {
    return this;
  }
  
  public PlaybackParameters getPlaybackParameters()
  {
    return this.audioSink.getPlaybackParameters();
  }
  
  public long getPositionUs()
  {
    if (getState() == 2) {
      updateCurrentPosition();
    }
    return this.currentPositionUs;
  }
  
  public void handleMessage(int paramInt, Object paramObject)
    throws ExoPlaybackException
  {
    switch (paramInt)
    {
    default: 
      super.handleMessage(paramInt, paramObject);
      return;
    case 2: 
      this.audioSink.setVolume(((Float)paramObject).floatValue());
      return;
    }
    paramObject = (AudioAttributes)paramObject;
    this.audioSink.setAudioAttributes((AudioAttributes)paramObject);
  }
  
  public boolean isEnded()
  {
    return (super.isEnded()) && (this.audioSink.isEnded());
  }
  
  public boolean isReady()
  {
    return (this.audioSink.hasPendingData()) || (super.isReady());
  }
  
  protected void onAudioSessionId(int paramInt) {}
  
  protected void onAudioTrackPositionDiscontinuity() {}
  
  protected void onAudioTrackUnderrun(int paramInt, long paramLong1, long paramLong2) {}
  
  protected void onCodecInitialized(String paramString, long paramLong1, long paramLong2)
  {
    this.eventDispatcher.decoderInitialized(paramString, paramLong1, paramLong2);
  }
  
  /* Error */
  protected void onDisabled()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 72	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:audioSink	Lorg/telegram/messenger/exoplayer2/audio/AudioSink;
    //   4: invokeinterface 264 1 0
    //   9: aload_0
    //   10: invokespecial 266	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:onDisabled	()V
    //   13: aload_0
    //   14: getfield 270	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   17: invokevirtual 275	org/telegram/messenger/exoplayer2/decoder/DecoderCounters:ensureUpdated	()V
    //   20: aload_0
    //   21: getfield 70	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:eventDispatcher	Lorg/telegram/messenger/exoplayer2/audio/AudioRendererEventListener$EventDispatcher;
    //   24: aload_0
    //   25: getfield 270	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   28: invokevirtual 279	org/telegram/messenger/exoplayer2/audio/AudioRendererEventListener$EventDispatcher:disabled	(Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;)V
    //   31: return
    //   32: astore_1
    //   33: aload_0
    //   34: getfield 270	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   37: invokevirtual 275	org/telegram/messenger/exoplayer2/decoder/DecoderCounters:ensureUpdated	()V
    //   40: aload_0
    //   41: getfield 70	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:eventDispatcher	Lorg/telegram/messenger/exoplayer2/audio/AudioRendererEventListener$EventDispatcher;
    //   44: aload_0
    //   45: getfield 270	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   48: invokevirtual 279	org/telegram/messenger/exoplayer2/audio/AudioRendererEventListener$EventDispatcher:disabled	(Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;)V
    //   51: aload_1
    //   52: athrow
    //   53: astore_1
    //   54: aload_0
    //   55: invokespecial 266	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:onDisabled	()V
    //   58: aload_0
    //   59: getfield 270	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   62: invokevirtual 275	org/telegram/messenger/exoplayer2/decoder/DecoderCounters:ensureUpdated	()V
    //   65: aload_0
    //   66: getfield 70	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:eventDispatcher	Lorg/telegram/messenger/exoplayer2/audio/AudioRendererEventListener$EventDispatcher;
    //   69: aload_0
    //   70: getfield 270	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   73: invokevirtual 279	org/telegram/messenger/exoplayer2/audio/AudioRendererEventListener$EventDispatcher:disabled	(Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;)V
    //   76: aload_1
    //   77: athrow
    //   78: astore_1
    //   79: aload_0
    //   80: getfield 270	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   83: invokevirtual 275	org/telegram/messenger/exoplayer2/decoder/DecoderCounters:ensureUpdated	()V
    //   86: aload_0
    //   87: getfield 70	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:eventDispatcher	Lorg/telegram/messenger/exoplayer2/audio/AudioRendererEventListener$EventDispatcher;
    //   90: aload_0
    //   91: getfield 270	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   94: invokevirtual 279	org/telegram/messenger/exoplayer2/audio/AudioRendererEventListener$EventDispatcher:disabled	(Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;)V
    //   97: aload_1
    //   98: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	99	0	this	MediaCodecAudioRenderer
    //   32	20	1	localObject1	Object
    //   53	24	1	localObject2	Object
    //   78	20	1	localObject3	Object
    // Exception table:
    //   from	to	target	type
    //   9	13	32	finally
    //   0	9	53	finally
    //   54	58	78	finally
  }
  
  protected void onEnabled(boolean paramBoolean)
    throws ExoPlaybackException
  {
    super.onEnabled(paramBoolean);
    this.eventDispatcher.enabled(this.decoderCounters);
    int i = getConfiguration().tunnelingAudioSessionId;
    if (i != 0)
    {
      this.audioSink.enableTunnelingV21(i);
      return;
    }
    this.audioSink.disableTunneling();
  }
  
  protected void onInputFormatChanged(Format paramFormat)
    throws ExoPlaybackException
  {
    int j = 0;
    super.onInputFormatChanged(paramFormat);
    this.eventDispatcher.inputFormatChanged(paramFormat);
    if ("audio/raw".equals(paramFormat.sampleMimeType))
    {
      i = paramFormat.pcmEncoding;
      this.pcmEncoding = i;
      this.channelCount = paramFormat.channelCount;
      if (paramFormat.encoderDelay == -1) {
        break label89;
      }
    }
    label89:
    for (int i = paramFormat.encoderDelay;; i = 0)
    {
      this.encoderDelay = i;
      i = j;
      if (paramFormat.encoderPadding != -1) {
        i = paramFormat.encoderPadding;
      }
      this.encoderPadding = i;
      return;
      i = 2;
      break;
    }
  }
  
  protected void onOutputFormatChanged(MediaCodec paramMediaCodec, MediaFormat paramMediaFormat)
    throws ExoPlaybackException
  {
    int i;
    if (this.passthroughMediaFormat != null) {
      i = MimeTypes.getEncoding(this.passthroughMediaFormat.getString("mime"));
    }
    int k;
    int m;
    for (paramMediaCodec = this.passthroughMediaFormat;; paramMediaCodec = paramMediaFormat)
    {
      k = paramMediaCodec.getInteger("channel-count");
      m = paramMediaCodec.getInteger("sample-rate");
      if ((!this.codecNeedsDiscardChannelsWorkaround) || (k != 6) || (this.channelCount >= 6)) {
        break;
      }
      paramMediaFormat = new int[this.channelCount];
      int j = 0;
      for (;;)
      {
        paramMediaCodec = paramMediaFormat;
        if (j >= this.channelCount) {
          break;
        }
        paramMediaFormat[j] = j;
        j += 1;
      }
      i = this.pcmEncoding;
    }
    paramMediaCodec = null;
    try
    {
      this.audioSink.configure(i, k, m, 0, paramMediaCodec, this.encoderDelay, this.encoderPadding);
      return;
    }
    catch (AudioSink.ConfigurationException paramMediaCodec)
    {
      throw ExoPlaybackException.createForRenderer(paramMediaCodec, getIndex());
    }
  }
  
  protected void onPositionReset(long paramLong, boolean paramBoolean)
    throws ExoPlaybackException
  {
    super.onPositionReset(paramLong, paramBoolean);
    this.audioSink.reset();
    this.currentPositionUs = paramLong;
    this.allowPositionDiscontinuity = true;
  }
  
  protected void onStarted()
  {
    super.onStarted();
    this.audioSink.play();
  }
  
  protected void onStopped()
  {
    this.audioSink.pause();
    updateCurrentPosition();
    super.onStopped();
  }
  
  protected boolean processOutputBuffer(long paramLong1, long paramLong2, MediaCodec paramMediaCodec, ByteBuffer paramByteBuffer, int paramInt1, int paramInt2, long paramLong3, boolean paramBoolean)
    throws ExoPlaybackException
  {
    if ((this.passthroughEnabled) && ((paramInt2 & 0x2) != 0))
    {
      paramMediaCodec.releaseOutputBuffer(paramInt1, false);
      return true;
    }
    if (paramBoolean)
    {
      paramMediaCodec.releaseOutputBuffer(paramInt1, false);
      paramMediaCodec = this.decoderCounters;
      paramMediaCodec.skippedOutputBufferCount += 1;
      this.audioSink.handleDiscontinuity();
      return true;
    }
    try
    {
      if (this.audioSink.handleBuffer(paramByteBuffer, paramLong3))
      {
        paramMediaCodec.releaseOutputBuffer(paramInt1, false);
        paramMediaCodec = this.decoderCounters;
        paramMediaCodec.renderedOutputBufferCount += 1;
        return true;
      }
    }
    catch (AudioSink.InitializationException paramMediaCodec)
    {
      throw ExoPlaybackException.createForRenderer(paramMediaCodec, getIndex());
      return false;
    }
    catch (AudioSink.WriteException paramMediaCodec)
    {
      for (;;) {}
    }
  }
  
  protected void renderToEndOfStream()
    throws ExoPlaybackException
  {
    try
    {
      this.audioSink.playToEndOfStream();
      return;
    }
    catch (AudioSink.WriteException localWriteException)
    {
      throw ExoPlaybackException.createForRenderer(localWriteException, getIndex());
    }
  }
  
  public PlaybackParameters setPlaybackParameters(PlaybackParameters paramPlaybackParameters)
  {
    return this.audioSink.setPlaybackParameters(paramPlaybackParameters);
  }
  
  protected int supportsFormat(MediaCodecSelector paramMediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, Format paramFormat)
    throws MediaCodecUtil.DecoderQueryException
  {
    String str = paramFormat.sampleMimeType;
    if (!MimeTypes.isAudio(str)) {
      return 0;
    }
    if (Util.SDK_INT >= 21) {}
    boolean bool3;
    for (int i = 32;; i = 0)
    {
      bool3 = supportsFormatDrm(paramDrmSessionManager, paramFormat.drmInitData);
      if ((!bool3) || (!allowPassthrough(str)) || (paramMediaCodecSelector.getPassthroughDecoderInfo() == null)) {
        break;
      }
      return i | 0x8 | 0x4;
    }
    if ((("audio/raw".equals(str)) && (!this.audioSink.isEncodingSupported(paramFormat.pcmEncoding))) || (!this.audioSink.isEncodingSupported(2))) {
      return 1;
    }
    boolean bool2 = false;
    boolean bool1 = false;
    paramDrmSessionManager = paramFormat.drmInitData;
    if (paramDrmSessionManager != null)
    {
      j = 0;
      for (;;)
      {
        bool2 = bool1;
        if (j >= paramDrmSessionManager.schemeDataCount) {
          break;
        }
        bool1 |= paramDrmSessionManager.get(j).requiresSecureDecryption;
        j += 1;
      }
    }
    paramDrmSessionManager = paramMediaCodecSelector.getDecoderInfo(str, bool2);
    if (paramDrmSessionManager == null)
    {
      if ((bool2) && (paramMediaCodecSelector.getDecoderInfo(str, false) != null)) {
        return 2;
      }
      return 1;
    }
    if (!bool3) {
      return 2;
    }
    if ((Util.SDK_INT < 21) || (((paramFormat.sampleRate == -1) || (paramDrmSessionManager.isAudioSampleRateSupportedV21(paramFormat.sampleRate))) && ((paramFormat.channelCount == -1) || (paramDrmSessionManager.isAudioChannelCountSupportedV21(paramFormat.channelCount)))))
    {
      j = 1;
      if (j == 0) {
        break label285;
      }
    }
    label285:
    for (int j = 4;; j = 3)
    {
      return i | 0x8 | j;
      j = 0;
      break;
    }
  }
  
  private final class AudioSinkListener
    implements AudioSink.Listener
  {
    private AudioSinkListener() {}
    
    public void onAudioSessionId(int paramInt)
    {
      MediaCodecAudioRenderer.this.eventDispatcher.audioSessionId(paramInt);
      MediaCodecAudioRenderer.this.onAudioSessionId(paramInt);
    }
    
    public void onPositionDiscontinuity()
    {
      MediaCodecAudioRenderer.this.onAudioTrackPositionDiscontinuity();
      MediaCodecAudioRenderer.access$202(MediaCodecAudioRenderer.this, true);
    }
    
    public void onUnderrun(int paramInt, long paramLong1, long paramLong2)
    {
      MediaCodecAudioRenderer.this.eventDispatcher.audioTrackUnderrun(paramInt, paramLong1, paramLong2);
      MediaCodecAudioRenderer.this.onAudioTrackUnderrun(paramInt, paramLong1, paramLong2);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */