package ir.eitaa.messenger.exoplayer.text;

import java.util.List;

public abstract interface Subtitle
{
  public abstract List<Cue> getCues(long paramLong);
  
  public abstract long getEventTime(int paramInt);
  
  public abstract int getEventTimeCount();
  
  public abstract long getLastEventTime();
  
  public abstract int getNextEventTimeIndex(long paramLong);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/Subtitle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */