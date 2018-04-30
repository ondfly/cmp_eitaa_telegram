package ir.eitaa.messenger.exoplayer.util.extensions;

public abstract interface Decoder<I, O, E extends Exception>
{
  public abstract I dequeueInputBuffer()
    throws Exception;
  
  public abstract O dequeueOutputBuffer()
    throws Exception;
  
  public abstract void flush();
  
  public abstract void queueInputBuffer(I paramI)
    throws Exception;
  
  public abstract void release();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/util/extensions/Decoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */