package ir.eitaa.messenger.exoplayer.text.subrip;

import ir.eitaa.messenger.exoplayer.text.Cue;
import ir.eitaa.messenger.exoplayer.text.Subtitle;
import ir.eitaa.messenger.exoplayer.util.Assertions;
import ir.eitaa.messenger.exoplayer.util.Util;
import java.util.Collections;
import java.util.List;

final class SubripSubtitle
  implements Subtitle
{
  private final long[] cueTimesUs;
  private final Cue[] cues;
  
  public SubripSubtitle(Cue[] paramArrayOfCue, long[] paramArrayOfLong)
  {
    this.cues = paramArrayOfCue;
    this.cueTimesUs = paramArrayOfLong;
  }
  
  public List<Cue> getCues(long paramLong)
  {
    int i = Util.binarySearchFloor(this.cueTimesUs, paramLong, true, false);
    if ((i == -1) || (this.cues[i] == null)) {
      return Collections.emptyList();
    }
    return Collections.singletonList(this.cues[i]);
  }
  
  public long getEventTime(int paramInt)
  {
    boolean bool2 = true;
    if (paramInt >= 0)
    {
      bool1 = true;
      Assertions.checkArgument(bool1);
      if (paramInt >= this.cueTimesUs.length) {
        break label39;
      }
    }
    label39:
    for (boolean bool1 = bool2;; bool1 = false)
    {
      Assertions.checkArgument(bool1);
      return this.cueTimesUs[paramInt];
      bool1 = false;
      break;
    }
  }
  
  public int getEventTimeCount()
  {
    return this.cueTimesUs.length;
  }
  
  public long getLastEventTime()
  {
    if (getEventTimeCount() == 0) {
      return -1L;
    }
    return this.cueTimesUs[(this.cueTimesUs.length - 1)];
  }
  
  public int getNextEventTimeIndex(long paramLong)
  {
    int i = Util.binarySearchCeil(this.cueTimesUs, paramLong, false, false);
    if (i < this.cueTimesUs.length) {
      return i;
    }
    return -1;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/subrip/SubripSubtitle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */