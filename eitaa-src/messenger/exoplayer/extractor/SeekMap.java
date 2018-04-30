package ir.eitaa.messenger.exoplayer.extractor;

public abstract interface SeekMap
{
  public static final SeekMap UNSEEKABLE = new SeekMap()
  {
    public long getPosition(long paramAnonymousLong)
    {
      return 0L;
    }
    
    public boolean isSeekable()
    {
      return false;
    }
  };
  
  public abstract long getPosition(long paramLong);
  
  public abstract boolean isSeekable();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/SeekMap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */