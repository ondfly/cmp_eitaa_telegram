package org.telegram.messenger.exoplayer2.text.cea;

import java.util.Collections;
import java.util.List;
import org.telegram.messenger.exoplayer2.text.Cue;
import org.telegram.messenger.exoplayer2.text.Subtitle;
import org.telegram.messenger.exoplayer2.util.Assertions;

final class CeaSubtitle
  implements Subtitle
{
  private final List<Cue> cues;
  
  public CeaSubtitle(List<Cue> paramList)
  {
    this.cues = paramList;
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
  
  public int getNextEventTimeIndex(long paramLong)
  {
    if (paramLong < 0L) {
      return 0;
    }
    return -1;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/cea/CeaSubtitle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */