package org.telegram.messenger.exoplayer2.text.dvb;

import java.util.List;
import org.telegram.messenger.exoplayer2.text.Cue;
import org.telegram.messenger.exoplayer2.text.Subtitle;

final class DvbSubtitle
  implements Subtitle
{
  private final List<Cue> cues;
  
  public DvbSubtitle(List<Cue> paramList)
  {
    this.cues = paramList;
  }
  
  public List<Cue> getCues(long paramLong)
  {
    return this.cues;
  }
  
  public long getEventTime(int paramInt)
  {
    return 0L;
  }
  
  public int getEventTimeCount()
  {
    return 1;
  }
  
  public int getNextEventTimeIndex(long paramLong)
  {
    return -1;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/dvb/DvbSubtitle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */