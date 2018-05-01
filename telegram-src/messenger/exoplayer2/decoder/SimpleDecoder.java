package org.telegram.messenger.exoplayer2.decoder;

import java.util.LinkedList;
import org.telegram.messenger.exoplayer2.util.Assertions;

public abstract class SimpleDecoder<I extends DecoderInputBuffer, O extends OutputBuffer, E extends Exception>
  implements Decoder<I, O, E>
{
  private int availableInputBufferCount;
  private final I[] availableInputBuffers;
  private int availableOutputBufferCount;
  private final O[] availableOutputBuffers;
  private final Thread decodeThread;
  private I dequeuedInputBuffer;
  private E exception;
  private boolean flushed;
  private final Object lock = new Object();
  private final LinkedList<I> queuedInputBuffers = new LinkedList();
  private final LinkedList<O> queuedOutputBuffers = new LinkedList();
  private boolean released;
  private int skippedOutputBufferCount;
  
  protected SimpleDecoder(I[] paramArrayOfI, O[] paramArrayOfO)
  {
    this.availableInputBuffers = paramArrayOfI;
    this.availableInputBufferCount = paramArrayOfI.length;
    int i = 0;
    while (i < this.availableInputBufferCount)
    {
      this.availableInputBuffers[i] = createInputBuffer();
      i += 1;
    }
    this.availableOutputBuffers = paramArrayOfO;
    this.availableOutputBufferCount = paramArrayOfO.length;
    i = 0;
    while (i < this.availableOutputBufferCount)
    {
      this.availableOutputBuffers[i] = createOutputBuffer();
      i += 1;
    }
    this.decodeThread = new Thread()
    {
      public void run()
      {
        SimpleDecoder.this.run();
      }
    };
    this.decodeThread.start();
  }
  
  private boolean canDecodeBuffer()
  {
    return (!this.queuedInputBuffers.isEmpty()) && (this.availableOutputBufferCount > 0);
  }
  
  private boolean decode()
    throws InterruptedException
  {
    synchronized (this.lock)
    {
      if ((!this.released) && (!canDecodeBuffer())) {
        this.lock.wait();
      }
    }
    if (this.released) {
      return false;
    }
    ??? = (DecoderInputBuffer)this.queuedInputBuffers.removeFirst();
    Object localObject4 = this.availableOutputBuffers;
    int i = this.availableOutputBufferCount - 1;
    this.availableOutputBufferCount = i;
    localObject4 = localObject4[i];
    boolean bool = this.flushed;
    this.flushed = false;
    if (((DecoderInputBuffer)???).isEndOfStream()) {
      ((OutputBuffer)localObject4).addFlag(4);
    }
    for (;;)
    {
      synchronized (this.lock)
      {
        if (this.flushed)
        {
          releaseOutputBufferInternal((OutputBuffer)localObject4);
          releaseInputBufferInternal((DecoderInputBuffer)???);
          return true;
          if (((DecoderInputBuffer)???).isDecodeOnly()) {
            ((OutputBuffer)localObject4).addFlag(Integer.MIN_VALUE);
          }
          try
          {
            this.exception = decode((DecoderInputBuffer)???, (OutputBuffer)localObject4, bool);
            if (this.exception == null) {
              continue;
            }
            synchronized (this.lock)
            {
              return false;
            }
          }
          catch (RuntimeException localRuntimeException)
          {
            this.exception = createUnexpectedDecodeException(localRuntimeException);
            continue;
          }
          catch (OutOfMemoryError localOutOfMemoryError)
          {
            this.exception = createUnexpectedDecodeException(localOutOfMemoryError);
            continue;
          }
        }
        if (localOutputBuffer.isDecodeOnly())
        {
          this.skippedOutputBufferCount += 1;
          releaseOutputBufferInternal(localOutputBuffer);
        }
      }
      localOutputBuffer.skippedOutputBufferCount = this.skippedOutputBufferCount;
      this.skippedOutputBufferCount = 0;
      this.queuedOutputBuffers.addLast(localOutputBuffer);
    }
  }
  
  private void maybeNotifyDecodeLoop()
  {
    if (canDecodeBuffer()) {
      this.lock.notify();
    }
  }
  
  private void maybeThrowException()
    throws Exception
  {
    if (this.exception != null) {
      throw this.exception;
    }
  }
  
  private void releaseInputBufferInternal(I paramI)
  {
    paramI.clear();
    DecoderInputBuffer[] arrayOfDecoderInputBuffer = this.availableInputBuffers;
    int i = this.availableInputBufferCount;
    this.availableInputBufferCount = (i + 1);
    arrayOfDecoderInputBuffer[i] = paramI;
  }
  
  private void releaseOutputBufferInternal(O paramO)
  {
    paramO.clear();
    OutputBuffer[] arrayOfOutputBuffer = this.availableOutputBuffers;
    int i = this.availableOutputBufferCount;
    this.availableOutputBufferCount = (i + 1);
    arrayOfOutputBuffer[i] = paramO;
  }
  
  private void run()
  {
    try
    {
      boolean bool;
      do
      {
        bool = decode();
      } while (bool);
      return;
    }
    catch (InterruptedException localInterruptedException)
    {
      throw new IllegalStateException(localInterruptedException);
    }
  }
  
  protected abstract I createInputBuffer();
  
  protected abstract O createOutputBuffer();
  
  protected abstract E createUnexpectedDecodeException(Throwable paramThrowable);
  
  protected abstract E decode(I paramI, O paramO, boolean paramBoolean);
  
  public final I dequeueInputBuffer()
    throws Exception
  {
    for (;;)
    {
      synchronized (this.lock)
      {
        maybeThrowException();
        if (this.dequeuedInputBuffer == null)
        {
          bool = true;
          Assertions.checkState(bool);
          if (this.availableInputBufferCount == 0)
          {
            localObject1 = null;
            this.dequeuedInputBuffer = ((DecoderInputBuffer)localObject1);
            localObject1 = this.dequeuedInputBuffer;
            return (I)localObject1;
          }
          Object localObject1 = this.availableInputBuffers;
          int i = this.availableInputBufferCount - 1;
          this.availableInputBufferCount = i;
          localObject1 = localObject1[i];
        }
      }
      boolean bool = false;
    }
  }
  
  public final O dequeueOutputBuffer()
    throws Exception
  {
    synchronized (this.lock)
    {
      maybeThrowException();
      if (this.queuedOutputBuffers.isEmpty()) {
        return null;
      }
      OutputBuffer localOutputBuffer = (OutputBuffer)this.queuedOutputBuffers.removeFirst();
      return localOutputBuffer;
    }
  }
  
  public final void flush()
  {
    synchronized (this.lock)
    {
      this.flushed = true;
      this.skippedOutputBufferCount = 0;
      if (this.dequeuedInputBuffer != null)
      {
        releaseInputBufferInternal(this.dequeuedInputBuffer);
        this.dequeuedInputBuffer = null;
      }
      if (!this.queuedInputBuffers.isEmpty()) {
        releaseInputBufferInternal((DecoderInputBuffer)this.queuedInputBuffers.removeFirst());
      }
    }
    while (!this.queuedOutputBuffers.isEmpty()) {
      releaseOutputBufferInternal((OutputBuffer)this.queuedOutputBuffers.removeFirst());
    }
  }
  
  public final void queueInputBuffer(I paramI)
    throws Exception
  {
    for (;;)
    {
      synchronized (this.lock)
      {
        maybeThrowException();
        if (paramI == this.dequeuedInputBuffer)
        {
          bool = true;
          Assertions.checkArgument(bool);
          this.queuedInputBuffers.addLast(paramI);
          maybeNotifyDecodeLoop();
          this.dequeuedInputBuffer = null;
          return;
        }
      }
      boolean bool = false;
    }
  }
  
  public void release()
  {
    synchronized (this.lock)
    {
      this.released = true;
      this.lock.notify();
    }
  }
  
  protected void releaseOutputBuffer(O paramO)
  {
    synchronized (this.lock)
    {
      releaseOutputBufferInternal(paramO);
      maybeNotifyDecodeLoop();
      return;
    }
  }
  
  protected final void setInitialInputBufferSize(int paramInt)
  {
    int i = 0;
    if (this.availableInputBufferCount == this.availableInputBuffers.length) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      DecoderInputBuffer[] arrayOfDecoderInputBuffer = this.availableInputBuffers;
      int j = arrayOfDecoderInputBuffer.length;
      while (i < j)
      {
        arrayOfDecoderInputBuffer[i].ensureSpaceForWrite(paramInt);
        i += 1;
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/decoder/SimpleDecoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */