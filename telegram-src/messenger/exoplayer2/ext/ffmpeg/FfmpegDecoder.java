package org.telegram.messenger.exoplayer2.ext.ffmpeg;

import java.nio.ByteBuffer;
import java.util.List;
import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;
import org.telegram.messenger.exoplayer2.decoder.SimpleDecoder;
import org.telegram.messenger.exoplayer2.decoder.SimpleOutputBuffer;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;

final class FfmpegDecoder
  extends SimpleDecoder<DecoderInputBuffer, SimpleOutputBuffer, FfmpegDecoderException>
{
  private static final int OUTPUT_BUFFER_SIZE_16BIT = 49152;
  private static final int OUTPUT_BUFFER_SIZE_32BIT = 98304;
  private volatile int channelCount;
  private final String codecName;
  private final int encoding;
  private final byte[] extraData;
  private boolean hasOutputFormat;
  private long nativeContext;
  private final int outputBufferSize;
  private volatile int sampleRate;
  
  public FfmpegDecoder(int paramInt1, int paramInt2, int paramInt3, String paramString, List<byte[]> paramList, boolean paramBoolean)
    throws FfmpegDecoderException
  {
    super(new DecoderInputBuffer[paramInt1], new SimpleOutputBuffer[paramInt2]);
    this.codecName = FfmpegLibrary.getCodecName(paramString);
    this.extraData = getExtraData(paramString, paramList);
    if (paramBoolean)
    {
      paramInt1 = 4;
      this.encoding = paramInt1;
      if (!paramBoolean) {
        break label99;
      }
    }
    label99:
    for (paramInt1 = 98304;; paramInt1 = 49152)
    {
      this.outputBufferSize = paramInt1;
      this.nativeContext = ffmpegInitialize(this.codecName, this.extraData, paramBoolean);
      if (this.nativeContext != 0L) {
        break label105;
      }
      throw new FfmpegDecoderException("Initialization failed.");
      paramInt1 = 2;
      break;
    }
    label105:
    setInitialInputBufferSize(paramInt3);
  }
  
  private native int ffmpegDecode(long paramLong, ByteBuffer paramByteBuffer1, int paramInt1, ByteBuffer paramByteBuffer2, int paramInt2);
  
  private native int ffmpegGetChannelCount(long paramLong);
  
  private native int ffmpegGetSampleRate(long paramLong);
  
  private native long ffmpegInitialize(String paramString, byte[] paramArrayOfByte, boolean paramBoolean);
  
  private native void ffmpegRelease(long paramLong);
  
  private native long ffmpegReset(long paramLong, byte[] paramArrayOfByte);
  
  private static byte[] getExtraData(String paramString, List<byte[]> paramList)
  {
    int i = -1;
    switch (paramString.hashCode())
    {
    }
    for (;;)
    {
      switch (i)
      {
      default: 
        return null;
        if (paramString.equals("audio/mp4a-latm"))
        {
          i = 0;
          continue;
          if (paramString.equals("audio/alac"))
          {
            i = 1;
            continue;
            if (paramString.equals("audio/opus"))
            {
              i = 2;
              continue;
              if (paramString.equals("audio/vorbis")) {
                i = 3;
              }
            }
          }
        }
        break;
      }
    }
    return (byte[])paramList.get(0);
    paramString = (byte[])paramList.get(0);
    paramList = (byte[])paramList.get(1);
    byte[] arrayOfByte = new byte[paramString.length + paramList.length + 6];
    arrayOfByte[0] = ((byte)(paramString.length >> 8));
    arrayOfByte[1] = ((byte)(paramString.length & 0xFF));
    System.arraycopy(paramString, 0, arrayOfByte, 2, paramString.length);
    arrayOfByte[(paramString.length + 2)] = 0;
    arrayOfByte[(paramString.length + 3)] = 0;
    arrayOfByte[(paramString.length + 4)] = ((byte)(paramList.length >> 8));
    arrayOfByte[(paramString.length + 5)] = ((byte)(paramList.length & 0xFF));
    System.arraycopy(paramList, 0, arrayOfByte, paramString.length + 6, paramList.length);
    return arrayOfByte;
  }
  
  protected DecoderInputBuffer createInputBuffer()
  {
    return new DecoderInputBuffer(2);
  }
  
  protected SimpleOutputBuffer createOutputBuffer()
  {
    return new SimpleOutputBuffer(this);
  }
  
  protected FfmpegDecoderException createUnexpectedDecodeException(Throwable paramThrowable)
  {
    return new FfmpegDecoderException("Unexpected decode error", paramThrowable);
  }
  
  protected FfmpegDecoderException decode(DecoderInputBuffer paramDecoderInputBuffer, SimpleOutputBuffer paramSimpleOutputBuffer, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      this.nativeContext = ffmpegReset(this.nativeContext, this.extraData);
      if (this.nativeContext == 0L) {
        return new FfmpegDecoderException("Error resetting (see logcat).");
      }
    }
    ByteBuffer localByteBuffer = paramDecoderInputBuffer.data;
    int i = localByteBuffer.limit();
    paramDecoderInputBuffer = paramSimpleOutputBuffer.init(paramDecoderInputBuffer.timeUs, this.outputBufferSize);
    i = ffmpegDecode(this.nativeContext, localByteBuffer, i, paramDecoderInputBuffer, this.outputBufferSize);
    if (i < 0) {
      return new FfmpegDecoderException("Error decoding (see logcat). Code: " + i);
    }
    if (!this.hasOutputFormat)
    {
      this.channelCount = ffmpegGetChannelCount(this.nativeContext);
      this.sampleRate = ffmpegGetSampleRate(this.nativeContext);
      if ((this.sampleRate == 0) && ("alac".equals(this.codecName)))
      {
        paramDecoderInputBuffer = new ParsableByteArray(this.extraData);
        paramDecoderInputBuffer.setPosition(this.extraData.length - 4);
        this.sampleRate = paramDecoderInputBuffer.readUnsignedIntToInt();
      }
      this.hasOutputFormat = true;
    }
    paramSimpleOutputBuffer.data.position(0);
    paramSimpleOutputBuffer.data.limit(i);
    return null;
  }
  
  public int getChannelCount()
  {
    return this.channelCount;
  }
  
  public int getEncoding()
  {
    return this.encoding;
  }
  
  public String getName()
  {
    return "ffmpeg" + FfmpegLibrary.getVersion() + "-" + this.codecName;
  }
  
  public int getSampleRate()
  {
    return this.sampleRate;
  }
  
  public void release()
  {
    super.release();
    ffmpegRelease(this.nativeContext);
    this.nativeContext = 0L;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/ext/ffmpeg/FfmpegDecoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */