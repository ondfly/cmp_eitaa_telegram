package org.telegram.messenger.exoplayer2.decoder;

public abstract interface Decoder<I, O, E extends Exception>
{
  public abstract I dequeueInputBuffer()
    throws Exception;
  
  public abstract O dequeueOutputBuffer()
    throws Exception;
  
  public abstract void flush();
  
  public abstract String getName();
  
  public abstract void queueInputBuffer(I paramI)
    throws Exception;
  
  public abstract void release();
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/decoder/Decoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */