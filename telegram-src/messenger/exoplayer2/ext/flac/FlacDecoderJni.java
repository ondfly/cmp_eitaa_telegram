package org.telegram.messenger.exoplayer2.ext.flac;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.util.FlacStreamInfo;

final class FlacDecoderJni
{
  private static final int TEMP_BUFFER_SIZE = 8192;
  private ByteBuffer byteBufferData;
  private boolean endOfExtractorInput;
  private ExtractorInput extractorInput;
  private final long nativeDecoderContext = flacInit();
  private byte[] tempBuffer;
  
  public FlacDecoderJni()
    throws FlacDecoderException
  {
    if (this.nativeDecoderContext == 0L) {
      throw new FlacDecoderException("Failed to initialize decoder");
    }
  }
  
  private native FlacStreamInfo flacDecodeMetadata(long paramLong)
    throws IOException, InterruptedException;
  
  private native int flacDecodeToArray(long paramLong, byte[] paramArrayOfByte)
    throws IOException, InterruptedException;
  
  private native int flacDecodeToBuffer(long paramLong, ByteBuffer paramByteBuffer)
    throws IOException, InterruptedException;
  
  private native void flacFlush(long paramLong);
  
  private native long flacGetDecodePosition(long paramLong);
  
  private native long flacGetLastTimestamp(long paramLong);
  
  private native long flacGetSeekPosition(long paramLong1, long paramLong2);
  
  private native String flacGetStateString(long paramLong);
  
  private native long flacInit();
  
  private native void flacRelease(long paramLong);
  
  private native void flacReset(long paramLong1, long paramLong2);
  
  private int readFromExtractorInput(int paramInt1, int paramInt2)
    throws IOException, InterruptedException
  {
    paramInt2 = this.extractorInput.read(this.tempBuffer, paramInt1, paramInt2);
    paramInt1 = paramInt2;
    if (paramInt2 == -1)
    {
      this.endOfExtractorInput = true;
      paramInt1 = 0;
    }
    return paramInt1;
  }
  
  public FlacStreamInfo decodeMetadata()
    throws IOException, InterruptedException
  {
    return flacDecodeMetadata(this.nativeDecoderContext);
  }
  
  public int decodeSample(ByteBuffer paramByteBuffer)
    throws IOException, InterruptedException
  {
    if (paramByteBuffer.isDirect()) {
      return flacDecodeToBuffer(this.nativeDecoderContext, paramByteBuffer);
    }
    return flacDecodeToArray(this.nativeDecoderContext, paramByteBuffer.array());
  }
  
  public void flush()
  {
    flacFlush(this.nativeDecoderContext);
  }
  
  public long getDecodePosition()
  {
    return flacGetDecodePosition(this.nativeDecoderContext);
  }
  
  public long getLastSampleTimestamp()
  {
    return flacGetLastTimestamp(this.nativeDecoderContext);
  }
  
  public long getSeekPosition(long paramLong)
  {
    return flacGetSeekPosition(this.nativeDecoderContext, paramLong);
  }
  
  public String getStateString()
  {
    return flacGetStateString(this.nativeDecoderContext);
  }
  
  public boolean isEndOfData()
  {
    if (this.byteBufferData != null) {
      if (this.byteBufferData.remaining() != 0) {}
    }
    while (this.extractorInput == null)
    {
      return true;
      return false;
    }
    return this.endOfExtractorInput;
  }
  
  public int read(ByteBuffer paramByteBuffer)
    throws IOException, InterruptedException
  {
    int i = paramByteBuffer.remaining();
    int j;
    if (this.byteBufferData != null)
    {
      i = Math.min(i, this.byteBufferData.remaining());
      j = this.byteBufferData.limit();
      this.byteBufferData.limit(this.byteBufferData.position() + i);
      paramByteBuffer.put(this.byteBufferData);
      this.byteBufferData.limit(j);
    }
    for (;;)
    {
      return i;
      if (this.extractorInput == null) {
        break;
      }
      int k = Math.min(i, 8192);
      j = readFromExtractorInput(0, k);
      i = j;
      if (j < 4) {
        i = j + readFromExtractorInput(j, k - j);
      }
      paramByteBuffer.put(this.tempBuffer, 0, i);
    }
    return -1;
  }
  
  public void release()
  {
    flacRelease(this.nativeDecoderContext);
  }
  
  public void reset(long paramLong)
  {
    flacReset(this.nativeDecoderContext, paramLong);
  }
  
  public void setData(ByteBuffer paramByteBuffer)
  {
    this.byteBufferData = paramByteBuffer;
    this.extractorInput = null;
    this.tempBuffer = null;
  }
  
  public void setData(ExtractorInput paramExtractorInput)
  {
    this.byteBufferData = null;
    this.extractorInput = paramExtractorInput;
    if (this.tempBuffer == null) {
      this.tempBuffer = new byte[' '];
    }
    this.endOfExtractorInput = false;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/ext/flac/FlacDecoderJni.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */