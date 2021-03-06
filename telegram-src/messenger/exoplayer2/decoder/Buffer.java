package org.telegram.messenger.exoplayer2.decoder;

public abstract class Buffer
{
  private int flags;
  
  public final void addFlag(int paramInt)
  {
    this.flags |= paramInt;
  }
  
  public void clear()
  {
    this.flags = 0;
  }
  
  public final void clearFlag(int paramInt)
  {
    this.flags &= (paramInt ^ 0xFFFFFFFF);
  }
  
  protected final boolean getFlag(int paramInt)
  {
    return (this.flags & paramInt) == paramInt;
  }
  
  public final boolean isDecodeOnly()
  {
    return getFlag(Integer.MIN_VALUE);
  }
  
  public final boolean isEndOfStream()
  {
    return getFlag(4);
  }
  
  public final boolean isKeyFrame()
  {
    return getFlag(1);
  }
  
  public final void setFlags(int paramInt)
  {
    this.flags = paramInt;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/decoder/Buffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */