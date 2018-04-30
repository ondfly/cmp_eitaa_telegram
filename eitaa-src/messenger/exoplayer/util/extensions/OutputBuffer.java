package ir.eitaa.messenger.exoplayer.util.extensions;

public abstract class OutputBuffer
  extends Buffer
{
  public long timestampUs;
  
  public abstract void release();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/util/extensions/OutputBuffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */