package org.telegram.messenger.exoplayer2.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class ResamplingAudioProcessor
  implements AudioProcessor
{
  private ByteBuffer buffer = EMPTY_BUFFER;
  private int channelCount = -1;
  private int encoding = 0;
  private boolean inputEnded;
  private ByteBuffer outputBuffer = EMPTY_BUFFER;
  private int sampleRateHz = -1;
  
  public boolean configure(int paramInt1, int paramInt2, int paramInt3)
    throws AudioProcessor.UnhandledFormatException
  {
    if ((paramInt3 != 3) && (paramInt3 != 2) && (paramInt3 != Integer.MIN_VALUE) && (paramInt3 != 1073741824)) {
      throw new AudioProcessor.UnhandledFormatException(paramInt1, paramInt2, paramInt3);
    }
    if ((this.sampleRateHz == paramInt1) && (this.channelCount == paramInt2) && (this.encoding == paramInt3)) {
      return false;
    }
    this.sampleRateHz = paramInt1;
    this.channelCount = paramInt2;
    this.encoding = paramInt3;
    if (paramInt3 == 2) {
      this.buffer = EMPTY_BUFFER;
    }
    return true;
  }
  
  public void flush()
  {
    this.outputBuffer = EMPTY_BUFFER;
    this.inputEnded = false;
  }
  
  public ByteBuffer getOutput()
  {
    ByteBuffer localByteBuffer = this.outputBuffer;
    this.outputBuffer = EMPTY_BUFFER;
    return localByteBuffer;
  }
  
  public int getOutputChannelCount()
  {
    return this.channelCount;
  }
  
  public int getOutputEncoding()
  {
    return 2;
  }
  
  public int getOutputSampleRateHz()
  {
    return this.sampleRateHz;
  }
  
  public boolean isActive()
  {
    return (this.encoding != 0) && (this.encoding != 2);
  }
  
  public boolean isEnded()
  {
    return (this.inputEnded) && (this.outputBuffer == EMPTY_BUFFER);
  }
  
  public void queueEndOfStream()
  {
    this.inputEnded = true;
  }
  
  public void queueInput(ByteBuffer paramByteBuffer)
  {
    int i = paramByteBuffer.position();
    int k = paramByteBuffer.limit();
    int j = k - i;
    switch (this.encoding)
    {
    default: 
      throw new IllegalStateException();
    case 3: 
      j *= 2;
      label68:
      if (this.buffer.capacity() < j) {
        this.buffer = ByteBuffer.allocateDirect(j).order(ByteOrder.nativeOrder());
      }
      break;
    }
    for (;;)
    {
      switch (this.encoding)
      {
      default: 
        throw new IllegalStateException();
        j = j / 3 * 2;
        break label68;
        j /= 2;
        break label68;
        this.buffer.clear();
      }
    }
    while (i < k)
    {
      this.buffer.put((byte)0);
      this.buffer.put((byte)((paramByteBuffer.get(i) & 0xFF) - 128));
      i += 1;
      continue;
      while (i < k)
      {
        this.buffer.put(paramByteBuffer.get(i + 1));
        this.buffer.put(paramByteBuffer.get(i + 2));
        i += 3;
        continue;
        while (i < k)
        {
          this.buffer.put(paramByteBuffer.get(i + 2));
          this.buffer.put(paramByteBuffer.get(i + 3));
          i += 4;
        }
      }
    }
    paramByteBuffer.position(paramByteBuffer.limit());
    this.buffer.flip();
    this.outputBuffer = this.buffer;
  }
  
  public void reset()
  {
    flush();
    this.buffer = EMPTY_BUFFER;
    this.sampleRateHz = -1;
    this.channelCount = -1;
    this.encoding = 0;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/audio/ResamplingAudioProcessor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */