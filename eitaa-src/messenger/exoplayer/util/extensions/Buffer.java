package ir.eitaa.messenger.exoplayer.util.extensions;

public abstract class Buffer
{
  public static final int FLAG_DECODE_ONLY = 2;
  public static final int FLAG_END_OF_STREAM = 1;
  private int flags;
  
  public final boolean getFlag(int paramInt)
  {
    return (this.flags & paramInt) == paramInt;
  }
  
  public void reset()
  {
    this.flags = 0;
  }
  
  public final void setFlag(int paramInt)
  {
    this.flags |= paramInt;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/util/extensions/Buffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */