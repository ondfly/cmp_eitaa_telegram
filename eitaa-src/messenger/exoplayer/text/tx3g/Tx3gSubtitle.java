package ir.eitaa.messenger.exoplayer.text.tx3g;

import ir.eitaa.messenger.exoplayer.text.Cue;
import ir.eitaa.messenger.exoplayer.text.Subtitle;
import ir.eitaa.messenger.exoplayer.util.Assertions;
import java.util.Collections;
import java.util.List;

final class Tx3gSubtitle
  implements Subtitle
{
  public static final Tx3gSubtitle EMPTY = new Tx3gSubtitle();
  private final List<Cue> cues;
  
  private Tx3gSubtitle()
  {
    this.cues = Collections.emptyList();
  }
  
  public Tx3gSubtitle(Cue paramCue)
  {
    this.cues = Collections.singletonList(paramCue);
  }
  
  public List<Cue> getCues(long paramLong)
  {
    if (paramLong >= 0L) {
      return this.cues;
    }
    return Collections.emptyList();
  }
  
  public long getEventTime(int paramInt)
  {
    if (paramInt == 0) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkArgument(bool);
      return 0L;
    }
  }
  
  public int getEventTimeCount()
  {
    return 1;
  }
  
  public long getLastEventTime()
  {
    return 0L;
  }
  
  public int getNextEventTimeIndex(long paramLong)
  {
    if (paramLong < 0L) {
      return 0;
    }
    return -1;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/tx3g/Tx3gSubtitle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */