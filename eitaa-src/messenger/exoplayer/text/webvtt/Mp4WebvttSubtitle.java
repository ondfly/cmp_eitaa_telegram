package ir.eitaa.messenger.exoplayer.text.webvtt;

import ir.eitaa.messenger.exoplayer.text.Cue;
import ir.eitaa.messenger.exoplayer.text.Subtitle;
import ir.eitaa.messenger.exoplayer.util.Assertions;
import java.util.Collections;
import java.util.List;

final class Mp4WebvttSubtitle
  implements Subtitle
{
  private final List<Cue> cues;
  
  public Mp4WebvttSubtitle(List<Cue> paramList)
  {
    this.cues = Collections.unmodifiableList(paramList);
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


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/webvtt/Mp4WebvttSubtitle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */