package org.telegram.messenger.exoplayer2.ext.opus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.telegram.messenger.exoplayer2.decoder.CryptoInfo;
import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;
import org.telegram.messenger.exoplayer2.decoder.SimpleDecoder;
import org.telegram.messenger.exoplayer2.decoder.SimpleOutputBuffer;
import org.telegram.messenger.exoplayer2.drm.DecryptionException;
import org.telegram.messenger.exoplayer2.drm.ExoMediaCrypto;

final class OpusDecoder
  extends SimpleDecoder<DecoderInputBuffer, SimpleOutputBuffer, OpusDecoderException>
{
  private static final int DECODE_ERROR = -1;
  private static final int DEFAULT_SEEK_PRE_ROLL_SAMPLES = 3840;
  private static final int DRM_ERROR = -2;
  private static final int NO_ERROR = 0;
  private static final int SAMPLE_RATE = 48000;
  private final int channelCount;
  private final ExoMediaCrypto exoMediaCrypto;
  private final int headerSeekPreRollSamples;
  private final int headerSkipSamples;
  private final long nativeDecoderContext;
  private int skipSamples;
  
  public OpusDecoder(int paramInt1, int paramInt2, int paramInt3, List<byte[]> paramList, ExoMediaCrypto paramExoMediaCrypto)
    throws OpusDecoderException
  {
    super(new DecoderInputBuffer[paramInt1], new SimpleOutputBuffer[paramInt2]);
    this.exoMediaCrypto = paramExoMediaCrypto;
    if ((paramExoMediaCrypto != null) && (!OpusLibrary.opusIsSecureDecodeSupported())) {
      throw new OpusDecoderException("Opus decoder does not support secure decode.");
    }
    paramExoMediaCrypto = (byte[])paramList.get(0);
    if (paramExoMediaCrypto.length < 19) {
      throw new OpusDecoderException("Header size is too small.");
    }
    this.channelCount = (paramExoMediaCrypto[9] & 0xFF);
    if (this.channelCount > 8) {
      throw new OpusDecoderException("Invalid channel count: " + this.channelCount);
    }
    int j = readLittleEndian16(paramExoMediaCrypto, 10);
    int k = readLittleEndian16(paramExoMediaCrypto, 16);
    byte[] arrayOfByte = new byte[8];
    if (paramExoMediaCrypto[18] == 0)
    {
      if (this.channelCount > 2) {
        throw new OpusDecoderException("Invalid Header, missing stream map.");
      }
      int i = 1;
      if (this.channelCount == 2)
      {
        paramInt1 = 1;
        arrayOfByte[0] = 0;
        arrayOfByte[1] = 1;
        paramInt2 = paramInt1;
        paramInt1 = i;
      }
    }
    long l2;
    for (;;)
    {
      if (paramList.size() == 3)
      {
        if ((((byte[])paramList.get(1)).length != 8) || (((byte[])paramList.get(2)).length != 8))
        {
          throw new OpusDecoderException("Invalid Codec Delay or Seek Preroll");
          paramInt1 = 0;
          break;
          if (paramExoMediaCrypto.length < this.channelCount + 21) {
            throw new OpusDecoderException("Header size is too small.");
          }
          paramInt1 = paramExoMediaCrypto[19] & 0xFF;
          paramInt2 = paramExoMediaCrypto[20] & 0xFF;
          System.arraycopy(paramExoMediaCrypto, 21, arrayOfByte, 0, this.channelCount);
          continue;
        }
        long l1 = ByteBuffer.wrap((byte[])paramList.get(1)).order(ByteOrder.nativeOrder()).getLong();
        l2 = ByteBuffer.wrap((byte[])paramList.get(2)).order(ByteOrder.nativeOrder()).getLong();
        this.headerSkipSamples = nsToSamples(l1);
      }
    }
    for (this.headerSeekPreRollSamples = nsToSamples(l2);; this.headerSeekPreRollSamples = 3840)
    {
      this.nativeDecoderContext = opusInit(48000, this.channelCount, paramInt1, paramInt2, k, arrayOfByte);
      if (this.nativeDecoderContext != 0L) {
        break;
      }
      throw new OpusDecoderException("Failed to initialize decoder");
      this.headerSkipSamples = j;
    }
    setInitialInputBufferSize(paramInt3);
  }
  
  private static int nsToSamples(long paramLong)
  {
    return (int)(48000L * paramLong / 1000000000L);
  }
  
  private native void opusClose(long paramLong);
  
  private native int opusDecode(long paramLong1, long paramLong2, ByteBuffer paramByteBuffer, int paramInt, SimpleOutputBuffer paramSimpleOutputBuffer);
  
  private native int opusGetErrorCode(long paramLong);
  
  private native String opusGetErrorMessage(long paramLong);
  
  private native long opusInit(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, byte[] paramArrayOfByte);
  
  private native void opusReset(long paramLong);
  
  private native int opusSecureDecode(long paramLong1, long paramLong2, ByteBuffer paramByteBuffer, int paramInt1, SimpleOutputBuffer paramSimpleOutputBuffer, int paramInt2, ExoMediaCrypto paramExoMediaCrypto, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt4, int[] paramArrayOfInt1, int[] paramArrayOfInt2);
  
  private static int readLittleEndian16(byte[] paramArrayOfByte, int paramInt)
  {
    return paramArrayOfByte[paramInt] & 0xFF | (paramArrayOfByte[(paramInt + 1)] & 0xFF) << 8;
  }
  
  protected DecoderInputBuffer createInputBuffer()
  {
    return new DecoderInputBuffer(2);
  }
  
  protected SimpleOutputBuffer createOutputBuffer()
  {
    return new SimpleOutputBuffer(this);
  }
  
  protected OpusDecoderException createUnexpectedDecodeException(Throwable paramThrowable)
  {
    return new OpusDecoderException("Unexpected decode error", paramThrowable);
  }
  
  protected OpusDecoderException decode(DecoderInputBuffer paramDecoderInputBuffer, SimpleOutputBuffer paramSimpleOutputBuffer, boolean paramBoolean)
  {
    int i;
    ByteBuffer localByteBuffer;
    if (paramBoolean)
    {
      opusReset(this.nativeDecoderContext);
      if (paramDecoderInputBuffer.timeUs == 0L)
      {
        i = this.headerSkipSamples;
        this.skipSamples = i;
      }
    }
    else
    {
      localByteBuffer = paramDecoderInputBuffer.data;
      CryptoInfo localCryptoInfo = paramDecoderInputBuffer.cryptoInfo;
      if (!paramDecoderInputBuffer.isEncrypted()) {
        break label183;
      }
      i = opusSecureDecode(this.nativeDecoderContext, paramDecoderInputBuffer.timeUs, localByteBuffer, localByteBuffer.limit(), paramSimpleOutputBuffer, 48000, this.exoMediaCrypto, localCryptoInfo.mode, localCryptoInfo.key, localCryptoInfo.iv, localCryptoInfo.numSubSamples, localCryptoInfo.numBytesOfClearData, localCryptoInfo.numBytesOfEncryptedData);
    }
    for (;;)
    {
      if (i < 0)
      {
        if (i == -2)
        {
          paramDecoderInputBuffer = "Drm error: " + opusGetErrorMessage(this.nativeDecoderContext);
          return new OpusDecoderException(paramDecoderInputBuffer, new DecryptionException(opusGetErrorCode(this.nativeDecoderContext), paramDecoderInputBuffer));
          i = this.headerSeekPreRollSamples;
          break;
          label183:
          i = opusDecode(this.nativeDecoderContext, paramDecoderInputBuffer.timeUs, localByteBuffer, localByteBuffer.limit(), paramSimpleOutputBuffer);
          continue;
        }
        return new OpusDecoderException("Decode error: " + opusGetErrorMessage(i));
      }
    }
    paramDecoderInputBuffer = paramSimpleOutputBuffer.data;
    paramDecoderInputBuffer.position(0);
    paramDecoderInputBuffer.limit(i);
    int k;
    if (this.skipSamples > 0)
    {
      int j = this.channelCount * 2;
      k = this.skipSamples * j;
      if (i > k) {
        break label319;
      }
      this.skipSamples -= i / j;
      paramSimpleOutputBuffer.addFlag(Integer.MIN_VALUE);
      paramDecoderInputBuffer.position(i);
    }
    for (;;)
    {
      return null;
      label319:
      this.skipSamples = 0;
      paramDecoderInputBuffer.position(k);
    }
  }
  
  public int getChannelCount()
  {
    return this.channelCount;
  }
  
  public String getName()
  {
    return "libopus" + OpusLibrary.getVersion();
  }
  
  public int getSampleRate()
  {
    return 48000;
  }
  
  public void release()
  {
    super.release();
    opusClose(this.nativeDecoderContext);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/ext/opus/OpusDecoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */