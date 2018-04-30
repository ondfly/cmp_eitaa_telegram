package org.telegram.messenger.exoplayer2.text.cea;

import java.util.LinkedList;
import java.util.PriorityQueue;
import org.telegram.messenger.exoplayer2.text.Subtitle;
import org.telegram.messenger.exoplayer2.text.SubtitleDecoder;
import org.telegram.messenger.exoplayer2.text.SubtitleDecoderException;
import org.telegram.messenger.exoplayer2.text.SubtitleInputBuffer;
import org.telegram.messenger.exoplayer2.text.SubtitleOutputBuffer;
import org.telegram.messenger.exoplayer2.util.Assertions;

abstract class CeaDecoder
  implements SubtitleDecoder
{
  private static final int NUM_INPUT_BUFFERS = 10;
  private static final int NUM_OUTPUT_BUFFERS = 2;
  private final LinkedList<SubtitleInputBuffer> availableInputBuffers = new LinkedList();
  private final LinkedList<SubtitleOutputBuffer> availableOutputBuffers;
  private SubtitleInputBuffer dequeuedInputBuffer;
  private long playbackPositionUs;
  private final PriorityQueue<SubtitleInputBuffer> queuedInputBuffers;
  
  public CeaDecoder()
  {
    int i = 0;
    while (i < 10)
    {
      this.availableInputBuffers.add(new SubtitleInputBuffer());
      i += 1;
    }
    this.availableOutputBuffers = new LinkedList();
    i = 0;
    while (i < 2)
    {
      this.availableOutputBuffers.add(new CeaOutputBuffer(this));
      i += 1;
    }
    this.queuedInputBuffers = new PriorityQueue();
  }
  
  private void releaseInputBuffer(SubtitleInputBuffer paramSubtitleInputBuffer)
  {
    paramSubtitleInputBuffer.clear();
    this.availableInputBuffers.add(paramSubtitleInputBuffer);
  }
  
  protected abstract Subtitle createSubtitle();
  
  protected abstract void decode(SubtitleInputBuffer paramSubtitleInputBuffer);
  
  public SubtitleInputBuffer dequeueInputBuffer()
    throws SubtitleDecoderException
  {
    if (this.dequeuedInputBuffer == null) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      if (!this.availableInputBuffers.isEmpty()) {
        break;
      }
      return null;
    }
    this.dequeuedInputBuffer = ((SubtitleInputBuffer)this.availableInputBuffers.pollFirst());
    return this.dequeuedInputBuffer;
  }
  
  public SubtitleOutputBuffer dequeueOutputBuffer()
    throws SubtitleDecoderException
  {
    if (this.availableOutputBuffers.isEmpty()) {
      return null;
    }
    SubtitleInputBuffer localSubtitleInputBuffer;
    Object localObject;
    do
    {
      do
      {
        releaseInputBuffer(localSubtitleInputBuffer);
        if ((this.queuedInputBuffers.isEmpty()) || (((SubtitleInputBuffer)this.queuedInputBuffers.peek()).timeUs > this.playbackPositionUs)) {
          break;
        }
        localSubtitleInputBuffer = (SubtitleInputBuffer)this.queuedInputBuffers.poll();
        if (localSubtitleInputBuffer.isEndOfStream())
        {
          localObject = (SubtitleOutputBuffer)this.availableOutputBuffers.pollFirst();
          ((SubtitleOutputBuffer)localObject).addFlag(4);
          releaseInputBuffer(localSubtitleInputBuffer);
          return (SubtitleOutputBuffer)localObject;
        }
        decode(localSubtitleInputBuffer);
      } while (!isNewSubtitleDataAvailable());
      localObject = createSubtitle();
    } while (localSubtitleInputBuffer.isDecodeOnly());
    SubtitleOutputBuffer localSubtitleOutputBuffer = (SubtitleOutputBuffer)this.availableOutputBuffers.pollFirst();
    localSubtitleOutputBuffer.setContent(localSubtitleInputBuffer.timeUs, (Subtitle)localObject, Long.MAX_VALUE);
    releaseInputBuffer(localSubtitleInputBuffer);
    return localSubtitleOutputBuffer;
  }
  
  public void flush()
  {
    this.playbackPositionUs = 0L;
    while (!this.queuedInputBuffers.isEmpty()) {
      releaseInputBuffer((SubtitleInputBuffer)this.queuedInputBuffers.poll());
    }
    if (this.dequeuedInputBuffer != null)
    {
      releaseInputBuffer(this.dequeuedInputBuffer);
      this.dequeuedInputBuffer = null;
    }
  }
  
  public abstract String getName();
  
  protected abstract boolean isNewSubtitleDataAvailable();
  
  public void queueInputBuffer(SubtitleInputBuffer paramSubtitleInputBuffer)
    throws SubtitleDecoderException
  {
    boolean bool;
    if (paramSubtitleInputBuffer == this.dequeuedInputBuffer)
    {
      bool = true;
      Assertions.checkArgument(bool);
      if (!paramSubtitleInputBuffer.isDecodeOnly()) {
        break label37;
      }
      releaseInputBuffer(paramSubtitleInputBuffer);
    }
    for (;;)
    {
      this.dequeuedInputBuffer = null;
      return;
      bool = false;
      break;
      label37:
      this.queuedInputBuffers.add(paramSubtitleInputBuffer);
    }
  }
  
  public void release() {}
  
  protected void releaseOutputBuffer(SubtitleOutputBuffer paramSubtitleOutputBuffer)
  {
    paramSubtitleOutputBuffer.clear();
    this.availableOutputBuffers.add(paramSubtitleOutputBuffer);
  }
  
  public void setPositionUs(long paramLong)
  {
    this.playbackPositionUs = paramLong;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/cea/CeaDecoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */