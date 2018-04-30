package org.telegram.messenger.exoplayer2.ext.ffmpeg;

import android.os.Handler;
import org.telegram.messenger.exoplayer2.ExoPlaybackException;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.audio.AudioProcessor;
import org.telegram.messenger.exoplayer2.audio.AudioRendererEventListener;
import org.telegram.messenger.exoplayer2.audio.AudioSink;
import org.telegram.messenger.exoplayer2.audio.DefaultAudioSink;
import org.telegram.messenger.exoplayer2.audio.SimpleDecoderAudioRenderer;
import org.telegram.messenger.exoplayer2.drm.DrmSessionManager;
import org.telegram.messenger.exoplayer2.drm.ExoMediaCrypto;
import org.telegram.messenger.exoplayer2.util.MimeTypes;

public final class FfmpegAudioRenderer
  extends SimpleDecoderAudioRenderer
{
  private static final int INITIAL_INPUT_BUFFER_SIZE = 5760;
  private static final int NUM_BUFFERS = 16;
  private FfmpegDecoder decoder;
  private final boolean enableFloatOutput;
  
  public FfmpegAudioRenderer()
  {
    this(null, null, new AudioProcessor[0]);
  }
  
  public FfmpegAudioRenderer(Handler paramHandler, AudioRendererEventListener paramAudioRendererEventListener, AudioSink paramAudioSink, boolean paramBoolean)
  {
    super(paramHandler, paramAudioRendererEventListener, null, false, paramAudioSink);
    this.enableFloatOutput = paramBoolean;
  }
  
  public FfmpegAudioRenderer(Handler paramHandler, AudioRendererEventListener paramAudioRendererEventListener, AudioProcessor... paramVarArgs)
  {
    this(paramHandler, paramAudioRendererEventListener, new DefaultAudioSink(null, paramVarArgs), false);
  }
  
  private boolean isOutputSupported(Format paramFormat)
  {
    return (shouldUseFloatOutput(paramFormat)) || (supportsOutputEncoding(2));
  }
  
  private boolean shouldUseFloatOutput(Format paramFormat)
  {
    if ((!this.enableFloatOutput) || (!supportsOutputEncoding(4))) {}
    do
    {
      return false;
      String str = paramFormat.sampleMimeType;
      int i = -1;
      switch (str.hashCode())
      {
      }
      for (;;)
      {
        switch (i)
        {
        case 1: 
        default: 
          return true;
          if (str.equals("audio/raw"))
          {
            i = 0;
            continue;
            if (str.equals("audio/ac3")) {
              i = 1;
            }
          }
          break;
        }
      }
    } while ((paramFormat.pcmEncoding != Integer.MIN_VALUE) && (paramFormat.pcmEncoding != 1073741824) && (paramFormat.pcmEncoding != 4));
    return true;
  }
  
  protected FfmpegDecoder createDecoder(Format paramFormat, ExoMediaCrypto paramExoMediaCrypto)
    throws FfmpegDecoderException
  {
    this.decoder = new FfmpegDecoder(16, 16, 5760, paramFormat.sampleMimeType, paramFormat.initializationData, shouldUseFloatOutput(paramFormat));
    return this.decoder;
  }
  
  public Format getOutputFormat()
  {
    return Format.createAudioSampleFormat(null, "audio/raw", null, -1, -1, this.decoder.getChannelCount(), this.decoder.getSampleRate(), this.decoder.getEncoding(), null, null, 0, null);
  }
  
  protected int supportsFormatInternal(DrmSessionManager<ExoMediaCrypto> paramDrmSessionManager, Format paramFormat)
  {
    String str = paramFormat.sampleMimeType;
    if (!MimeTypes.isAudio(str)) {
      return 0;
    }
    if ((!FfmpegLibrary.supportsFormat(str)) || (!isOutputSupported(paramFormat))) {
      return 1;
    }
    if (!supportsFormatDrm(paramDrmSessionManager, paramFormat.drmInitData)) {
      return 2;
    }
    return 4;
  }
  
  public final int supportsMixedMimeTypeAdaptation()
    throws ExoPlaybackException
  {
    return 8;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/ext/ffmpeg/FfmpegAudioRenderer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */