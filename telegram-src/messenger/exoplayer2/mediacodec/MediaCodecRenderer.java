package org.telegram.messenger.exoplayer2.mediacodec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.CodecException;
import android.media.MediaCodec.CryptoException;
import android.media.MediaCodec.CryptoInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.telegram.messenger.exoplayer2.BaseRenderer;
import org.telegram.messenger.exoplayer2.ExoPlaybackException;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.FormatHolder;
import org.telegram.messenger.exoplayer2.decoder.CryptoInfo;
import org.telegram.messenger.exoplayer2.decoder.DecoderCounters;
import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;
import org.telegram.messenger.exoplayer2.drm.DrmInitData;
import org.telegram.messenger.exoplayer2.drm.DrmSession;
import org.telegram.messenger.exoplayer2.drm.DrmSessionManager;
import org.telegram.messenger.exoplayer2.drm.FrameworkMediaCrypto;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.NalUnitUtil;
import org.telegram.messenger.exoplayer2.util.TraceUtil;
import org.telegram.messenger.exoplayer2.util.Util;

@TargetApi(16)
public abstract class MediaCodecRenderer
  extends BaseRenderer
{
  private static final byte[] ADAPTATION_WORKAROUND_BUFFER = Util.getBytesFromHexString("0000016742C00BDA259000000168CE0F13200000016588840DCE7118A0002FBF1C31C3275D78");
  private static final int ADAPTATION_WORKAROUND_MODE_ALWAYS = 2;
  private static final int ADAPTATION_WORKAROUND_MODE_NEVER = 0;
  private static final int ADAPTATION_WORKAROUND_MODE_SAME_RESOLUTION = 1;
  private static final int ADAPTATION_WORKAROUND_SLICE_WIDTH_HEIGHT = 32;
  private static final long MAX_CODEC_HOTSWAP_TIME_MS = 1000L;
  private static final int RECONFIGURATION_STATE_NONE = 0;
  private static final int RECONFIGURATION_STATE_QUEUE_PENDING = 2;
  private static final int RECONFIGURATION_STATE_WRITE_PENDING = 1;
  private static final int REINITIALIZATION_STATE_NONE = 0;
  private static final int REINITIALIZATION_STATE_SIGNAL_END_OF_STREAM = 1;
  private static final int REINITIALIZATION_STATE_WAIT_END_OF_STREAM = 2;
  private static final String TAG = "MediaCodecRenderer";
  private final DecoderInputBuffer buffer;
  private MediaCodec codec;
  private int codecAdaptationWorkaroundMode;
  private long codecHotswapDeadlineMs;
  private MediaCodecInfo codecInfo;
  private boolean codecNeedsAdaptationWorkaroundBuffer;
  private boolean codecNeedsDiscardToSpsWorkaround;
  private boolean codecNeedsEosFlushWorkaround;
  private boolean codecNeedsEosOutputExceptionWorkaround;
  private boolean codecNeedsEosPropagationWorkaround;
  private boolean codecNeedsFlushWorkaround;
  private boolean codecNeedsMonoChannelCountWorkaround;
  private boolean codecReceivedBuffers;
  private boolean codecReceivedEos;
  private int codecReconfigurationState;
  private boolean codecReconfigured;
  private int codecReinitializationState;
  private final List<Long> decodeOnlyPresentationTimestamps;
  protected DecoderCounters decoderCounters;
  private DrmSession<FrameworkMediaCrypto> drmSession;
  private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
  private final DecoderInputBuffer flagsOnlyBuffer;
  private Format format;
  private final FormatHolder formatHolder;
  private ByteBuffer[] inputBuffers;
  private int inputIndex;
  private boolean inputStreamEnded;
  private final MediaCodecSelector mediaCodecSelector;
  private ByteBuffer outputBuffer;
  private final MediaCodec.BufferInfo outputBufferInfo;
  private ByteBuffer[] outputBuffers;
  private int outputIndex;
  private boolean outputStreamEnded;
  private DrmSession<FrameworkMediaCrypto> pendingDrmSession;
  private final boolean playClearSamplesWithoutKeys;
  private boolean shouldSkipAdaptationWorkaroundOutputBuffer;
  private boolean shouldSkipOutputBuffer;
  private boolean waitingForFirstSyncFrame;
  private boolean waitingForKeys;
  
  public MediaCodecRenderer(int paramInt, MediaCodecSelector paramMediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, boolean paramBoolean)
  {
    super(paramInt);
    if (Util.SDK_INT >= 16) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      this.mediaCodecSelector = ((MediaCodecSelector)Assertions.checkNotNull(paramMediaCodecSelector));
      this.drmSessionManager = paramDrmSessionManager;
      this.playClearSamplesWithoutKeys = paramBoolean;
      this.buffer = new DecoderInputBuffer(0);
      this.flagsOnlyBuffer = DecoderInputBuffer.newFlagsOnlyInstance();
      this.formatHolder = new FormatHolder();
      this.decodeOnlyPresentationTimestamps = new ArrayList();
      this.outputBufferInfo = new MediaCodec.BufferInfo();
      this.codecReconfigurationState = 0;
      this.codecReinitializationState = 0;
      return;
    }
  }
  
  private int codecAdaptationWorkaroundMode(String paramString)
  {
    if ((Util.SDK_INT <= 25) && ("OMX.Exynos.avc.dec.secure".equals(paramString)) && ((Util.MODEL.startsWith("SM-T585")) || (Util.MODEL.startsWith("SM-A510")) || (Util.MODEL.startsWith("SM-A520")) || (Util.MODEL.startsWith("SM-J700")))) {
      return 2;
    }
    if ((Util.SDK_INT < 24) && (("OMX.Nvidia.h264.decode".equals(paramString)) || ("OMX.Nvidia.h264.decode.secure".equals(paramString))) && (("flounder".equals(Util.DEVICE)) || ("flounder_lte".equals(Util.DEVICE)) || ("grouper".equals(Util.DEVICE)) || ("tilapia".equals(Util.DEVICE)))) {
      return 1;
    }
    return 0;
  }
  
  private static boolean codecNeedsDiscardToSpsWorkaround(String paramString, Format paramFormat)
  {
    return (Util.SDK_INT < 21) && (paramFormat.initializationData.isEmpty()) && ("OMX.MTK.VIDEO.DECODER.AVC".equals(paramString));
  }
  
  private static boolean codecNeedsEosFlushWorkaround(String paramString)
  {
    return ((Util.SDK_INT <= 23) && ("OMX.google.vorbis.decoder".equals(paramString))) || ((Util.SDK_INT <= 19) && ("hb2000".equals(Util.DEVICE)) && (("OMX.amlogic.avc.decoder.awesome".equals(paramString)) || ("OMX.amlogic.avc.decoder.awesome.secure".equals(paramString))));
  }
  
  private static boolean codecNeedsEosOutputExceptionWorkaround(String paramString)
  {
    return (Util.SDK_INT == 21) && ("OMX.google.aac.decoder".equals(paramString));
  }
  
  private static boolean codecNeedsEosPropagationWorkaround(String paramString)
  {
    return (Util.SDK_INT <= 17) && (("OMX.rk.video_decoder.avc".equals(paramString)) || ("OMX.allwinner.video.decoder.avc".equals(paramString)));
  }
  
  private static boolean codecNeedsFlushWorkaround(String paramString)
  {
    return (Util.SDK_INT < 18) || ((Util.SDK_INT == 18) && (("OMX.SEC.avc.dec".equals(paramString)) || ("OMX.SEC.avc.dec.secure".equals(paramString)))) || ((Util.SDK_INT == 19) && (Util.MODEL.startsWith("SM-G800")) && (("OMX.Exynos.avc.dec".equals(paramString)) || ("OMX.Exynos.avc.dec.secure".equals(paramString))));
  }
  
  private static boolean codecNeedsMonoChannelCountWorkaround(String paramString, Format paramFormat)
  {
    return (Util.SDK_INT <= 18) && (paramFormat.channelCount == 1) && ("OMX.MTK.AUDIO.DECODER.MP3".equals(paramString));
  }
  
  @TargetApi(23)
  private static void configureMediaFormatForPlaybackV23(MediaFormat paramMediaFormat)
  {
    paramMediaFormat.setInteger("priority", 0);
  }
  
  private boolean drainOutputBuffer(long paramLong1, long paramLong2)
    throws ExoPlaybackException
  {
    int i;
    if (!hasOutputBuffer())
    {
      if ((this.codecNeedsEosOutputExceptionWorkaround) && (this.codecReceivedEos)) {}
      for (;;)
      {
        try
        {
          i = this.codec.dequeueOutputBuffer(this.outputBufferInfo, getDequeueOutputBufferTimeoutUs());
          if (i < 0) {
            break label274;
          }
          if (!this.shouldSkipAdaptationWorkaroundOutputBuffer) {
            break;
          }
          this.shouldSkipAdaptationWorkaroundOutputBuffer = false;
          this.codec.releaseOutputBuffer(i, false);
          return true;
        }
        catch (IllegalStateException localIllegalStateException1)
        {
          processEndOfStream();
          if (this.outputStreamEnded) {
            releaseCodec();
          }
          return false;
        }
        i = this.codec.dequeueOutputBuffer(this.outputBufferInfo, getDequeueOutputBufferTimeoutUs());
      }
      if ((this.outputBufferInfo.flags & 0x4) != 0)
      {
        processEndOfStream();
        return false;
      }
      this.outputIndex = i;
      this.outputBuffer = getOutputBuffer(i);
      if (this.outputBuffer != null)
      {
        this.outputBuffer.position(this.outputBufferInfo.offset);
        this.outputBuffer.limit(this.outputBufferInfo.offset + this.outputBufferInfo.size);
      }
      this.shouldSkipOutputBuffer = shouldSkipOutputBuffer(this.outputBufferInfo.presentationTimeUs);
    }
    if ((this.codecNeedsEosOutputExceptionWorkaround) && (this.codecReceivedEos)) {}
    for (;;)
    {
      try
      {
        bool = processOutputBuffer(paramLong1, paramLong2, this.codec, this.outputBuffer, this.outputIndex, this.outputBufferInfo.flags, this.outputBufferInfo.presentationTimeUs, this.shouldSkipOutputBuffer);
        if (!bool) {
          break;
        }
        onProcessedOutputBuffer(this.outputBufferInfo.presentationTimeUs);
        resetOutputBuffer();
        return true;
      }
      catch (IllegalStateException localIllegalStateException2)
      {
        label274:
        processEndOfStream();
        if (!this.outputStreamEnded) {
          continue;
        }
        releaseCodec();
        return false;
      }
      if (i == -2)
      {
        processOutputFormat();
        return true;
      }
      if (i == -3)
      {
        processOutputBuffersChanged();
        return true;
      }
      if ((this.codecNeedsEosPropagationWorkaround) && ((this.inputStreamEnded) || (this.codecReinitializationState == 2))) {
        processEndOfStream();
      }
      return false;
      boolean bool = processOutputBuffer(paramLong1, paramLong2, this.codec, this.outputBuffer, this.outputIndex, this.outputBufferInfo.flags, this.outputBufferInfo.presentationTimeUs, this.shouldSkipOutputBuffer);
    }
    return false;
  }
  
  private boolean feedInputBuffer()
    throws ExoPlaybackException
  {
    if ((this.codec == null) || (this.codecReinitializationState == 2) || (this.inputStreamEnded)) {
      return false;
    }
    if (this.inputIndex < 0)
    {
      this.inputIndex = this.codec.dequeueInputBuffer(0L);
      if (this.inputIndex < 0) {
        return false;
      }
      this.buffer.data = getInputBuffer(this.inputIndex);
      this.buffer.clear();
    }
    if (this.codecReinitializationState == 1)
    {
      if (this.codecNeedsEosPropagationWorkaround) {}
      for (;;)
      {
        this.codecReinitializationState = 2;
        return false;
        this.codecReceivedEos = true;
        this.codec.queueInputBuffer(this.inputIndex, 0, 0, 0L, 4);
        resetInputBuffer();
      }
    }
    if (this.codecNeedsAdaptationWorkaroundBuffer)
    {
      this.codecNeedsAdaptationWorkaroundBuffer = false;
      this.buffer.data.put(ADAPTATION_WORKAROUND_BUFFER);
      this.codec.queueInputBuffer(this.inputIndex, 0, ADAPTATION_WORKAROUND_BUFFER.length, 0L, 0);
      resetInputBuffer();
      this.codecReceivedBuffers = true;
      return true;
    }
    int i = 0;
    if (this.waitingForKeys) {}
    for (int j = -4; j == -3; j = readSource(this.formatHolder, this.buffer, false))
    {
      return false;
      if (this.codecReconfigurationState == 1)
      {
        i = 0;
        while (i < this.format.initializationData.size())
        {
          byte[] arrayOfByte = (byte[])this.format.initializationData.get(i);
          this.buffer.data.put(arrayOfByte);
          i += 1;
        }
        this.codecReconfigurationState = 2;
      }
      i = this.buffer.data.position();
    }
    if (j == -5)
    {
      if (this.codecReconfigurationState == 2)
      {
        this.buffer.clear();
        this.codecReconfigurationState = 1;
      }
      onInputFormatChanged(this.formatHolder.format);
      return true;
    }
    if (this.buffer.isEndOfStream())
    {
      if (this.codecReconfigurationState == 2)
      {
        this.buffer.clear();
        this.codecReconfigurationState = 1;
      }
      this.inputStreamEnded = true;
      if (!this.codecReceivedBuffers)
      {
        processEndOfStream();
        return false;
      }
      try
      {
        if (this.codecNeedsEosPropagationWorkaround) {
          break label696;
        }
        this.codecReceivedEos = true;
        this.codec.queueInputBuffer(this.inputIndex, 0, 0, 0L, 4);
        resetInputBuffer();
      }
      catch (MediaCodec.CryptoException localCryptoException1)
      {
        throw ExoPlaybackException.createForRenderer(localCryptoException1, getIndex());
      }
    }
    if ((this.waitingForFirstSyncFrame) && (!this.buffer.isKeyFrame()))
    {
      this.buffer.clear();
      if (this.codecReconfigurationState == 2) {
        this.codecReconfigurationState = 1;
      }
      return true;
    }
    this.waitingForFirstSyncFrame = false;
    boolean bool = this.buffer.isEncrypted();
    this.waitingForKeys = shouldWaitForKeys(bool);
    if (this.waitingForKeys) {
      return false;
    }
    if ((this.codecNeedsDiscardToSpsWorkaround) && (!bool))
    {
      NalUnitUtil.discardToSps(this.buffer.data);
      if (this.buffer.data.position() == 0) {
        return true;
      }
      this.codecNeedsDiscardToSpsWorkaround = false;
    }
    try
    {
      long l = this.buffer.timeUs;
      if (this.buffer.isDecodeOnly()) {
        this.decodeOnlyPresentationTimestamps.add(Long.valueOf(l));
      }
      this.buffer.flip();
      onQueueInputBuffer(this.buffer);
      Object localObject;
      if (bool)
      {
        localObject = getFrameworkCryptoInfo(this.buffer, i);
        this.codec.queueSecureInputBuffer(this.inputIndex, 0, (MediaCodec.CryptoInfo)localObject, l, 0);
      }
      for (;;)
      {
        resetInputBuffer();
        this.codecReceivedBuffers = true;
        this.codecReconfigurationState = 0;
        localObject = this.decoderCounters;
        ((DecoderCounters)localObject).inputBufferCount += 1;
        return true;
        this.codec.queueInputBuffer(this.inputIndex, 0, this.buffer.data.limit(), l, 0);
      }
      return false;
    }
    catch (MediaCodec.CryptoException localCryptoException2)
    {
      throw ExoPlaybackException.createForRenderer(localCryptoException2, getIndex());
    }
  }
  
  private void getCodecBuffers()
  {
    if (Util.SDK_INT < 21)
    {
      this.inputBuffers = this.codec.getInputBuffers();
      this.outputBuffers = this.codec.getOutputBuffers();
    }
  }
  
  private static MediaCodec.CryptoInfo getFrameworkCryptoInfo(DecoderInputBuffer paramDecoderInputBuffer, int paramInt)
  {
    paramDecoderInputBuffer = paramDecoderInputBuffer.cryptoInfo.getFrameworkCryptoInfoV16();
    if (paramInt == 0) {
      return paramDecoderInputBuffer;
    }
    if (paramDecoderInputBuffer.numBytesOfClearData == null) {
      paramDecoderInputBuffer.numBytesOfClearData = new int[1];
    }
    int[] arrayOfInt = paramDecoderInputBuffer.numBytesOfClearData;
    arrayOfInt[0] += paramInt;
    return paramDecoderInputBuffer;
  }
  
  private ByteBuffer getInputBuffer(int paramInt)
  {
    if (Util.SDK_INT >= 21) {
      return this.codec.getInputBuffer(paramInt);
    }
    return this.inputBuffers[paramInt];
  }
  
  private ByteBuffer getOutputBuffer(int paramInt)
  {
    if (Util.SDK_INT >= 21) {
      return this.codec.getOutputBuffer(paramInt);
    }
    return this.outputBuffers[paramInt];
  }
  
  private boolean hasOutputBuffer()
  {
    return this.outputIndex >= 0;
  }
  
  private void processEndOfStream()
    throws ExoPlaybackException
  {
    if (this.codecReinitializationState == 2)
    {
      releaseCodec();
      maybeInitCodec();
      return;
    }
    this.outputStreamEnded = true;
    renderToEndOfStream();
  }
  
  private void processOutputBuffersChanged()
  {
    if (Util.SDK_INT < 21) {
      this.outputBuffers = this.codec.getOutputBuffers();
    }
  }
  
  private void processOutputFormat()
    throws ExoPlaybackException
  {
    MediaFormat localMediaFormat = this.codec.getOutputFormat();
    if ((this.codecAdaptationWorkaroundMode != 0) && (localMediaFormat.getInteger("width") == 32) && (localMediaFormat.getInteger("height") == 32))
    {
      this.shouldSkipAdaptationWorkaroundOutputBuffer = true;
      return;
    }
    if (this.codecNeedsMonoChannelCountWorkaround) {
      localMediaFormat.setInteger("channel-count", 1);
    }
    onOutputFormatChanged(this.codec, localMediaFormat);
  }
  
  private void resetCodecBuffers()
  {
    if (Util.SDK_INT < 21)
    {
      this.inputBuffers = null;
      this.outputBuffers = null;
    }
  }
  
  private void resetInputBuffer()
  {
    this.inputIndex = -1;
    this.buffer.data = null;
  }
  
  private void resetOutputBuffer()
  {
    this.outputIndex = -1;
    this.outputBuffer = null;
  }
  
  private boolean shouldSkipOutputBuffer(long paramLong)
  {
    int j = this.decodeOnlyPresentationTimestamps.size();
    int i = 0;
    while (i < j)
    {
      if (((Long)this.decodeOnlyPresentationTimestamps.get(i)).longValue() == paramLong)
      {
        this.decodeOnlyPresentationTimestamps.remove(i);
        return true;
      }
      i += 1;
    }
    return false;
  }
  
  private boolean shouldWaitForKeys(boolean paramBoolean)
    throws ExoPlaybackException
  {
    boolean bool = true;
    if ((this.drmSession == null) || ((!paramBoolean) && (this.playClearSamplesWithoutKeys))) {
      paramBoolean = false;
    }
    int i;
    do
    {
      return paramBoolean;
      i = this.drmSession.getState();
      if (i == 1) {
        throw ExoPlaybackException.createForRenderer(this.drmSession.getError(), getIndex());
      }
      paramBoolean = bool;
    } while (i != 4);
    return false;
  }
  
  private void throwDecoderInitError(DecoderInitializationException paramDecoderInitializationException)
    throws ExoPlaybackException
  {
    throw ExoPlaybackException.createForRenderer(paramDecoderInitializationException, getIndex());
  }
  
  protected boolean canReconfigureCodec(MediaCodec paramMediaCodec, boolean paramBoolean, Format paramFormat1, Format paramFormat2)
  {
    return false;
  }
  
  protected abstract void configureCodec(MediaCodecInfo paramMediaCodecInfo, MediaCodec paramMediaCodec, Format paramFormat, MediaCrypto paramMediaCrypto)
    throws MediaCodecUtil.DecoderQueryException;
  
  protected void flushCodec()
    throws ExoPlaybackException
  {
    this.codecHotswapDeadlineMs = -9223372036854775807L;
    resetInputBuffer();
    resetOutputBuffer();
    this.waitingForFirstSyncFrame = true;
    this.waitingForKeys = false;
    this.shouldSkipOutputBuffer = false;
    this.decodeOnlyPresentationTimestamps.clear();
    this.codecNeedsAdaptationWorkaroundBuffer = false;
    this.shouldSkipAdaptationWorkaroundOutputBuffer = false;
    if ((this.codecNeedsFlushWorkaround) || ((this.codecNeedsEosFlushWorkaround) && (this.codecReceivedEos)))
    {
      releaseCodec();
      maybeInitCodec();
    }
    for (;;)
    {
      if ((this.codecReconfigured) && (this.format != null)) {
        this.codecReconfigurationState = 1;
      }
      return;
      if (this.codecReinitializationState != 0)
      {
        releaseCodec();
        maybeInitCodec();
      }
      else
      {
        this.codec.flush();
        this.codecReceivedBuffers = false;
      }
    }
  }
  
  protected final MediaCodec getCodec()
  {
    return this.codec;
  }
  
  protected final MediaCodecInfo getCodecInfo()
  {
    return this.codecInfo;
  }
  
  protected MediaCodecInfo getDecoderInfo(MediaCodecSelector paramMediaCodecSelector, Format paramFormat, boolean paramBoolean)
    throws MediaCodecUtil.DecoderQueryException
  {
    return paramMediaCodecSelector.getDecoderInfo(paramFormat.sampleMimeType, paramBoolean);
  }
  
  protected long getDequeueOutputBufferTimeoutUs()
  {
    return 0L;
  }
  
  protected final MediaFormat getMediaFormatForPlayback(Format paramFormat)
  {
    paramFormat = paramFormat.getFrameworkMediaFormatV16();
    if (Util.SDK_INT >= 23) {
      configureMediaFormatForPlaybackV23(paramFormat);
    }
    return paramFormat;
  }
  
  public boolean isEnded()
  {
    return this.outputStreamEnded;
  }
  
  public boolean isReady()
  {
    return (this.format != null) && (!this.waitingForKeys) && ((isSourceReady()) || (hasOutputBuffer()) || ((this.codecHotswapDeadlineMs != -9223372036854775807L) && (SystemClock.elapsedRealtime() < this.codecHotswapDeadlineMs)));
  }
  
  protected final void maybeInitCodec()
    throws ExoPlaybackException
  {
    if ((this.codec != null) || (this.format == null)) {}
    for (;;)
    {
      return;
      this.drmSession = this.pendingDrmSession;
      String str2 = this.format.sampleMimeType;
      String str1 = null;
      boolean bool2 = false;
      boolean bool1 = bool2;
      Object localObject = str1;
      FrameworkMediaCrypto localFrameworkMediaCrypto;
      if (this.drmSession != null)
      {
        localFrameworkMediaCrypto = (FrameworkMediaCrypto)this.drmSession.getMediaCrypto();
        if (localFrameworkMediaCrypto != null) {
          break label471;
        }
        if (this.drmSession.getError() == null) {
          continue;
        }
        localObject = str1;
        bool1 = bool2;
      }
      label87:
      if (this.codecInfo == null) {}
      try
      {
        this.codecInfo = getDecoderInfo(this.mediaCodecSelector, this.format, bool1);
        if ((this.codecInfo == null) && (bool1))
        {
          this.codecInfo = getDecoderInfo(this.mediaCodecSelector, this.format, false);
          if (this.codecInfo != null) {
            Log.w("MediaCodecRenderer", "Drm session requires secure decoder for " + str2 + ", but no secure decoder available. Trying to proceed with " + this.codecInfo.name + ".");
          }
        }
        if (this.codecInfo == null) {
          throwDecoderInitError(new DecoderInitializationException(this.format, null, bool1, -49999));
        }
        if (!shouldInitCodec(this.codecInfo)) {
          continue;
        }
        str1 = this.codecInfo.name;
        this.codecAdaptationWorkaroundMode = codecAdaptationWorkaroundMode(str1);
        this.codecNeedsDiscardToSpsWorkaround = codecNeedsDiscardToSpsWorkaround(str1, this.format);
        this.codecNeedsFlushWorkaround = codecNeedsFlushWorkaround(str1);
        this.codecNeedsEosPropagationWorkaround = codecNeedsEosPropagationWorkaround(str1);
        this.codecNeedsEosFlushWorkaround = codecNeedsEosFlushWorkaround(str1);
        this.codecNeedsEosOutputExceptionWorkaround = codecNeedsEosOutputExceptionWorkaround(str1);
        this.codecNeedsMonoChannelCountWorkaround = codecNeedsMonoChannelCountWorkaround(str1, this.format);
      }
      catch (MediaCodecUtil.DecoderQueryException localDecoderQueryException)
      {
        try
        {
          l1 = SystemClock.elapsedRealtime();
          TraceUtil.beginSection("createCodec:" + str1);
          this.codec = MediaCodec.createByCodecName(str1);
          TraceUtil.endSection();
          TraceUtil.beginSection("configureCodec");
          configureCodec(this.codecInfo, this.codec, this.format, (MediaCrypto)localObject);
          TraceUtil.endSection();
          TraceUtil.beginSection("startCodec");
          this.codec.start();
          TraceUtil.endSection();
          long l2 = SystemClock.elapsedRealtime();
          onCodecInitialized(str1, l2, l2 - l1);
          getCodecBuffers();
          if (getState() == 2)
          {
            l1 = SystemClock.elapsedRealtime() + 1000L;
            this.codecHotswapDeadlineMs = l1;
            resetInputBuffer();
            resetOutputBuffer();
            this.waitingForFirstSyncFrame = true;
            localObject = this.decoderCounters;
            ((DecoderCounters)localObject).decoderInitCount += 1;
            return;
            label471:
            localObject = localFrameworkMediaCrypto.getWrappedMediaCrypto();
            bool1 = localFrameworkMediaCrypto.requiresSecureDecoderComponent(str2);
            break label87;
            localDecoderQueryException = localDecoderQueryException;
            throwDecoderInitError(new DecoderInitializationException(this.format, localDecoderQueryException, bool1, -49998));
          }
        }
        catch (Exception localException)
        {
          for (;;)
          {
            throwDecoderInitError(new DecoderInitializationException(this.format, localException, bool1, localDecoderQueryException));
            continue;
            long l1 = -9223372036854775807L;
          }
        }
      }
    }
  }
  
  protected void onCodecInitialized(String paramString, long paramLong1, long paramLong2) {}
  
  /* Error */
  protected void onDisabled()
  {
    // Byte code:
    //   0: aload_0
    //   1: aconst_null
    //   2: putfield 396	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:format	Lorg/telegram/messenger/exoplayer2/Format;
    //   5: aload_0
    //   6: invokevirtual 303	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:releaseCodec	()V
    //   9: aload_0
    //   10: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   13: ifnull +16 -> 29
    //   16: aload_0
    //   17: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   20: aload_0
    //   21: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   24: invokeinterface 728 2 0
    //   29: aload_0
    //   30: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   33: ifnull +27 -> 60
    //   36: aload_0
    //   37: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   40: aload_0
    //   41: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   44: if_acmpeq +16 -> 60
    //   47: aload_0
    //   48: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   51: aload_0
    //   52: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   55: invokeinterface 728 2 0
    //   60: aload_0
    //   61: aconst_null
    //   62: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   65: aload_0
    //   66: aconst_null
    //   67: putfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   70: return
    //   71: astore_1
    //   72: aload_0
    //   73: aconst_null
    //   74: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   77: aload_0
    //   78: aconst_null
    //   79: putfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   82: aload_1
    //   83: athrow
    //   84: astore_1
    //   85: aload_0
    //   86: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   89: ifnull +27 -> 116
    //   92: aload_0
    //   93: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   96: aload_0
    //   97: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   100: if_acmpeq +16 -> 116
    //   103: aload_0
    //   104: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   107: aload_0
    //   108: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   111: invokeinterface 728 2 0
    //   116: aload_0
    //   117: aconst_null
    //   118: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   121: aload_0
    //   122: aconst_null
    //   123: putfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   126: aload_1
    //   127: athrow
    //   128: astore_1
    //   129: aload_0
    //   130: aconst_null
    //   131: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   134: aload_0
    //   135: aconst_null
    //   136: putfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   139: aload_1
    //   140: athrow
    //   141: astore_1
    //   142: aload_0
    //   143: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   146: ifnull +16 -> 162
    //   149: aload_0
    //   150: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   153: aload_0
    //   154: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   157: invokeinterface 728 2 0
    //   162: aload_0
    //   163: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   166: ifnull +27 -> 193
    //   169: aload_0
    //   170: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   173: aload_0
    //   174: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   177: if_acmpeq +16 -> 193
    //   180: aload_0
    //   181: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   184: aload_0
    //   185: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   188: invokeinterface 728 2 0
    //   193: aload_0
    //   194: aconst_null
    //   195: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   198: aload_0
    //   199: aconst_null
    //   200: putfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   203: aload_1
    //   204: athrow
    //   205: astore_1
    //   206: aload_0
    //   207: aconst_null
    //   208: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   211: aload_0
    //   212: aconst_null
    //   213: putfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   216: aload_1
    //   217: athrow
    //   218: astore_1
    //   219: aload_0
    //   220: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   223: ifnull +27 -> 250
    //   226: aload_0
    //   227: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   230: aload_0
    //   231: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   234: if_acmpeq +16 -> 250
    //   237: aload_0
    //   238: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   241: aload_0
    //   242: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   245: invokeinterface 728 2 0
    //   250: aload_0
    //   251: aconst_null
    //   252: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   255: aload_0
    //   256: aconst_null
    //   257: putfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   260: aload_1
    //   261: athrow
    //   262: astore_1
    //   263: aload_0
    //   264: aconst_null
    //   265: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   268: aload_0
    //   269: aconst_null
    //   270: putfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   273: aload_1
    //   274: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	275	0	this	MediaCodecRenderer
    //   71	12	1	localObject1	Object
    //   84	43	1	localObject2	Object
    //   128	12	1	localObject3	Object
    //   141	63	1	localObject4	Object
    //   205	12	1	localObject5	Object
    //   218	43	1	localObject6	Object
    //   262	12	1	localObject7	Object
    // Exception table:
    //   from	to	target	type
    //   29	60	71	finally
    //   9	29	84	finally
    //   85	116	128	finally
    //   5	9	141	finally
    //   162	193	205	finally
    //   142	162	218	finally
    //   219	250	262	finally
  }
  
  protected void onEnabled(boolean paramBoolean)
    throws ExoPlaybackException
  {
    this.decoderCounters = new DecoderCounters();
  }
  
  protected void onInputFormatChanged(Format paramFormat)
    throws ExoPlaybackException
  {
    Format localFormat = this.format;
    this.format = paramFormat;
    DrmInitData localDrmInitData = this.format.drmInitData;
    int i;
    if (localFormat == null)
    {
      paramFormat = null;
      if (Util.areEqual(localDrmInitData, paramFormat)) {
        break label86;
      }
      i = 1;
    }
    for (;;)
    {
      if (i != 0)
      {
        if (this.format.drmInitData == null) {
          break label244;
        }
        if (this.drmSessionManager == null)
        {
          throw ExoPlaybackException.createForRenderer(new IllegalStateException("Media requires a DrmSessionManager"), getIndex());
          paramFormat = localFormat.drmInitData;
          break;
          label86:
          i = 0;
          continue;
        }
        this.pendingDrmSession = this.drmSessionManager.acquireSession(Looper.myLooper(), this.format.drmInitData);
        if (this.pendingDrmSession == this.drmSession) {
          this.drmSessionManager.releaseSession(this.pendingDrmSession);
        }
      }
    }
    if ((this.pendingDrmSession == this.drmSession) && (this.codec != null) && (canReconfigureCodec(this.codec, this.codecInfo.adaptive, localFormat, this.format)))
    {
      this.codecReconfigured = true;
      this.codecReconfigurationState = 1;
      if ((this.codecAdaptationWorkaroundMode == 2) || ((this.codecAdaptationWorkaroundMode == 1) && (this.format.width == localFormat.width) && (this.format.height == localFormat.height))) {}
      for (boolean bool = true;; bool = false)
      {
        this.codecNeedsAdaptationWorkaroundBuffer = bool;
        return;
        label244:
        this.pendingDrmSession = null;
        break;
      }
    }
    if (this.codecReceivedBuffers)
    {
      this.codecReinitializationState = 1;
      return;
    }
    releaseCodec();
    maybeInitCodec();
  }
  
  protected void onOutputFormatChanged(MediaCodec paramMediaCodec, MediaFormat paramMediaFormat)
    throws ExoPlaybackException
  {}
  
  protected void onPositionReset(long paramLong, boolean paramBoolean)
    throws ExoPlaybackException
  {
    this.inputStreamEnded = false;
    this.outputStreamEnded = false;
    if (this.codec != null) {
      flushCodec();
    }
  }
  
  protected void onProcessedOutputBuffer(long paramLong) {}
  
  protected void onQueueInputBuffer(DecoderInputBuffer paramDecoderInputBuffer) {}
  
  protected void onStarted() {}
  
  protected void onStopped() {}
  
  protected abstract boolean processOutputBuffer(long paramLong1, long paramLong2, MediaCodec paramMediaCodec, ByteBuffer paramByteBuffer, int paramInt1, int paramInt2, long paramLong3, boolean paramBoolean)
    throws ExoPlaybackException;
  
  /* Error */
  protected void releaseCodec()
  {
    // Byte code:
    //   0: aload_0
    //   1: ldc2_w 569
    //   4: putfield 572	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecHotswapDeadlineMs	J
    //   7: aload_0
    //   8: invokespecial 384	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:resetInputBuffer	()V
    //   11: aload_0
    //   12: invokespecial 348	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:resetOutputBuffer	()V
    //   15: aload_0
    //   16: iconst_0
    //   17: putfield 394	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:waitingForKeys	Z
    //   20: aload_0
    //   21: iconst_0
    //   22: putfield 337	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:shouldSkipOutputBuffer	Z
    //   25: aload_0
    //   26: getfield 161	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:decodeOnlyPresentationTimestamps	Ljava/util/List;
    //   29: invokeinterface 573 1 0
    //   34: aload_0
    //   35: invokespecial 769	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:resetCodecBuffers	()V
    //   38: aload_0
    //   39: aconst_null
    //   40: putfield 588	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecInfo	Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecInfo;
    //   43: aload_0
    //   44: iconst_0
    //   45: putfield 579	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecReconfigured	Z
    //   48: aload_0
    //   49: iconst_0
    //   50: putfield 392	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecReceivedBuffers	Z
    //   53: aload_0
    //   54: iconst_0
    //   55: putfield 439	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecNeedsDiscardToSpsWorkaround	Z
    //   58: aload_0
    //   59: iconst_0
    //   60: putfield 575	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecNeedsFlushWorkaround	Z
    //   63: aload_0
    //   64: iconst_0
    //   65: putfield 526	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecAdaptationWorkaroundMode	I
    //   68: aload_0
    //   69: iconst_0
    //   70: putfield 356	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecNeedsEosPropagationWorkaround	Z
    //   73: aload_0
    //   74: iconst_0
    //   75: putfield 577	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecNeedsEosFlushWorkaround	Z
    //   78: aload_0
    //   79: iconst_0
    //   80: putfield 535	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecNeedsMonoChannelCountWorkaround	Z
    //   83: aload_0
    //   84: iconst_0
    //   85: putfield 386	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecNeedsAdaptationWorkaroundBuffer	Z
    //   88: aload_0
    //   89: iconst_0
    //   90: putfield 291	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:shouldSkipAdaptationWorkaroundOutputBuffer	Z
    //   93: aload_0
    //   94: iconst_0
    //   95: putfield 277	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecReceivedEos	Z
    //   98: aload_0
    //   99: iconst_0
    //   100: putfield 168	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecReconfigurationState	I
    //   103: aload_0
    //   104: iconst_0
    //   105: putfield 170	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codecReinitializationState	I
    //   108: aload_0
    //   109: getfield 279	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codec	Landroid/media/MediaCodec;
    //   112: ifnull +73 -> 185
    //   115: aload_0
    //   116: getfield 477	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:decoderCounters	Lorg/telegram/messenger/exoplayer2/decoder/DecoderCounters;
    //   119: astore_1
    //   120: aload_1
    //   121: aload_1
    //   122: getfield 772	org/telegram/messenger/exoplayer2/decoder/DecoderCounters:decoderReleaseCount	I
    //   125: iconst_1
    //   126: iadd
    //   127: putfield 772	org/telegram/messenger/exoplayer2/decoder/DecoderCounters:decoderReleaseCount	I
    //   130: aload_0
    //   131: getfield 279	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codec	Landroid/media/MediaCodec;
    //   134: invokevirtual 775	android/media/MediaCodec:stop	()V
    //   137: aload_0
    //   138: getfield 279	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codec	Landroid/media/MediaCodec;
    //   141: invokevirtual 778	android/media/MediaCodec:release	()V
    //   144: aload_0
    //   145: aconst_null
    //   146: putfield 279	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codec	Landroid/media/MediaCodec;
    //   149: aload_0
    //   150: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   153: ifnull +32 -> 185
    //   156: aload_0
    //   157: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   160: aload_0
    //   161: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   164: if_acmpeq +21 -> 185
    //   167: aload_0
    //   168: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   171: aload_0
    //   172: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   175: invokeinterface 728 2 0
    //   180: aload_0
    //   181: aconst_null
    //   182: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   185: return
    //   186: astore_1
    //   187: aload_0
    //   188: aconst_null
    //   189: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   192: aload_1
    //   193: athrow
    //   194: astore_1
    //   195: aload_0
    //   196: aconst_null
    //   197: putfield 279	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codec	Landroid/media/MediaCodec;
    //   200: aload_0
    //   201: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   204: ifnull +32 -> 236
    //   207: aload_0
    //   208: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   211: aload_0
    //   212: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   215: if_acmpeq +21 -> 236
    //   218: aload_0
    //   219: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   222: aload_0
    //   223: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   226: invokeinterface 728 2 0
    //   231: aload_0
    //   232: aconst_null
    //   233: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   236: aload_1
    //   237: athrow
    //   238: astore_1
    //   239: aload_0
    //   240: aconst_null
    //   241: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   244: aload_1
    //   245: athrow
    //   246: astore_1
    //   247: aload_0
    //   248: getfield 279	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codec	Landroid/media/MediaCodec;
    //   251: invokevirtual 778	android/media/MediaCodec:release	()V
    //   254: aload_0
    //   255: aconst_null
    //   256: putfield 279	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codec	Landroid/media/MediaCodec;
    //   259: aload_0
    //   260: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   263: ifnull +32 -> 295
    //   266: aload_0
    //   267: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   270: aload_0
    //   271: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   274: if_acmpeq +21 -> 295
    //   277: aload_0
    //   278: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   281: aload_0
    //   282: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   285: invokeinterface 728 2 0
    //   290: aload_0
    //   291: aconst_null
    //   292: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   295: aload_1
    //   296: athrow
    //   297: astore_1
    //   298: aload_0
    //   299: aconst_null
    //   300: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   303: aload_1
    //   304: athrow
    //   305: astore_1
    //   306: aload_0
    //   307: aconst_null
    //   308: putfield 279	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:codec	Landroid/media/MediaCodec;
    //   311: aload_0
    //   312: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   315: ifnull +32 -> 347
    //   318: aload_0
    //   319: getfield 617	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:pendingDrmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   322: aload_0
    //   323: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   326: if_acmpeq +21 -> 347
    //   329: aload_0
    //   330: getfield 137	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSessionManager	Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;
    //   333: aload_0
    //   334: getfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   337: invokeinterface 728 2 0
    //   342: aload_0
    //   343: aconst_null
    //   344: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   347: aload_1
    //   348: athrow
    //   349: astore_1
    //   350: aload_0
    //   351: aconst_null
    //   352: putfield 550	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer:drmSession	Lorg/telegram/messenger/exoplayer2/drm/DrmSession;
    //   355: aload_1
    //   356: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	357	0	this	MediaCodecRenderer
    //   119	3	1	localDecoderCounters	DecoderCounters
    //   186	7	1	localObject1	Object
    //   194	43	1	localObject2	Object
    //   238	7	1	localObject3	Object
    //   246	50	1	localObject4	Object
    //   297	7	1	localObject5	Object
    //   305	43	1	localObject6	Object
    //   349	7	1	localObject7	Object
    // Exception table:
    //   from	to	target	type
    //   167	180	186	finally
    //   137	144	194	finally
    //   218	231	238	finally
    //   130	137	246	finally
    //   277	290	297	finally
    //   247	254	305	finally
    //   329	342	349	finally
  }
  
  public void render(long paramLong1, long paramLong2)
    throws ExoPlaybackException
  {
    if (this.outputStreamEnded)
    {
      renderToEndOfStream();
      return;
    }
    int i;
    if (this.format == null)
    {
      this.flagsOnlyBuffer.clear();
      i = readSource(this.formatHolder, this.flagsOnlyBuffer, true);
      if (i == -5) {
        onInputFormatChanged(this.formatHolder.format);
      }
    }
    else
    {
      maybeInitCodec();
      if (this.codec == null) {
        break label130;
      }
      TraceUtil.beginSection("drainAndFeed");
      while (drainOutputBuffer(paramLong1, paramLong2)) {}
      while (feedInputBuffer()) {}
      TraceUtil.endSection();
    }
    for (;;)
    {
      this.decoderCounters.ensureUpdated();
      return;
      if (i != -4) {
        break;
      }
      Assertions.checkState(this.flagsOnlyBuffer.isEndOfStream());
      this.inputStreamEnded = true;
      processEndOfStream();
      return;
      label130:
      DecoderCounters localDecoderCounters = this.decoderCounters;
      localDecoderCounters.skippedInputBufferCount += skipSource(paramLong1);
      this.flagsOnlyBuffer.clear();
      i = readSource(this.formatHolder, this.flagsOnlyBuffer, false);
      if (i == -5)
      {
        onInputFormatChanged(this.formatHolder.format);
      }
      else if (i == -4)
      {
        Assertions.checkState(this.flagsOnlyBuffer.isEndOfStream());
        this.inputStreamEnded = true;
        processEndOfStream();
      }
    }
  }
  
  protected void renderToEndOfStream()
    throws ExoPlaybackException
  {}
  
  protected boolean shouldInitCodec(MediaCodecInfo paramMediaCodecInfo)
  {
    return true;
  }
  
  public final int supportsFormat(Format paramFormat)
    throws ExoPlaybackException
  {
    try
    {
      int i = supportsFormat(this.mediaCodecSelector, this.drmSessionManager, paramFormat);
      return i;
    }
    catch (MediaCodecUtil.DecoderQueryException paramFormat)
    {
      throw ExoPlaybackException.createForRenderer(paramFormat, getIndex());
    }
  }
  
  protected abstract int supportsFormat(MediaCodecSelector paramMediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, Format paramFormat)
    throws MediaCodecUtil.DecoderQueryException;
  
  public final int supportsMixedMimeTypeAdaptation()
  {
    return 8;
  }
  
  @Retention(RetentionPolicy.SOURCE)
  private static @interface AdaptationWorkaroundMode {}
  
  public static class DecoderInitializationException
    extends Exception
  {
    private static final int CUSTOM_ERROR_CODE_BASE = -50000;
    private static final int DECODER_QUERY_ERROR = -49998;
    private static final int NO_SUITABLE_DECODER_ERROR = -49999;
    public final String decoderName;
    public final String diagnosticInfo;
    public final String mimeType;
    public final boolean secureDecoderRequired;
    
    public DecoderInitializationException(Format paramFormat, Throwable paramThrowable, boolean paramBoolean, int paramInt)
    {
      super(paramThrowable);
      this.mimeType = paramFormat.sampleMimeType;
      this.secureDecoderRequired = paramBoolean;
      this.decoderName = null;
      this.diagnosticInfo = buildCustomDiagnosticInfo(paramInt);
    }
    
    public DecoderInitializationException(Format paramFormat, Throwable paramThrowable, boolean paramBoolean, String paramString)
    {
      super(paramThrowable);
      this.mimeType = paramFormat.sampleMimeType;
      this.secureDecoderRequired = paramBoolean;
      this.decoderName = paramString;
      if (Util.SDK_INT >= 21) {}
      for (paramFormat = getDiagnosticInfoV21(paramThrowable);; paramFormat = null)
      {
        this.diagnosticInfo = paramFormat;
        return;
      }
    }
    
    private static String buildCustomDiagnosticInfo(int paramInt)
    {
      if (paramInt < 0) {}
      for (String str = "neg_";; str = "") {
        return "com.google.android.exoplayer.MediaCodecTrackRenderer_" + str + Math.abs(paramInt);
      }
    }
    
    @TargetApi(21)
    private static String getDiagnosticInfoV21(Throwable paramThrowable)
    {
      if ((paramThrowable instanceof MediaCodec.CodecException)) {
        return ((MediaCodec.CodecException)paramThrowable).getDiagnosticInfo();
      }
      return null;
    }
  }
  
  @Retention(RetentionPolicy.SOURCE)
  private static @interface ReconfigurationState {}
  
  @Retention(RetentionPolicy.SOURCE)
  private static @interface ReinitializationState {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/mediacodec/MediaCodecRenderer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */