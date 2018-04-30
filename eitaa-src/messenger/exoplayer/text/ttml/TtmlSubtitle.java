package ir.eitaa.messenger.exoplayer.text.ttml;

import ir.eitaa.messenger.exoplayer.text.Cue;
import ir.eitaa.messenger.exoplayer.text.Subtitle;
import ir.eitaa.messenger.exoplayer.util.Util;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class TtmlSubtitle
  implements Subtitle
{
  private final long[] eventTimesUs;
  private final Map<String, TtmlStyle> globalStyles;
  private final Map<String, TtmlRegion> regionMap;
  private final TtmlNode root;
  
  public TtmlSubtitle(TtmlNode paramTtmlNode, Map<String, TtmlStyle> paramMap, Map<String, TtmlRegion> paramMap1)
  {
    this.root = paramTtmlNode;
    this.regionMap = paramMap1;
    if (paramMap != null) {}
    for (paramMap = Collections.unmodifiableMap(paramMap);; paramMap = Collections.emptyMap())
    {
      this.globalStyles = paramMap;
      this.eventTimesUs = paramTtmlNode.getEventTimesUs();
      return;
    }
  }
  
  public List<Cue> getCues(long paramLong)
  {
    return this.root.getCues(paramLong, this.globalStyles, this.regionMap);
  }
  
  public long getEventTime(int paramInt)
  {
    return this.eventTimesUs[paramInt];
  }
  
  public int getEventTimeCount()
  {
    return this.eventTimesUs.length;
  }
  
  Map<String, TtmlStyle> getGlobalStyles()
  {
    return this.globalStyles;
  }
  
  public long getLastEventTime()
  {
    if (this.eventTimesUs.length == 0) {
      return -1L;
    }
    return this.eventTimesUs[(this.eventTimesUs.length - 1)];
  }
  
  public int getNextEventTimeIndex(long paramLong)
  {
    int i = Util.binarySearchCeil(this.eventTimesUs, paramLong, false, false);
    if (i < this.eventTimesUs.length) {
      return i;
    }
    return -1;
  }
  
  TtmlNode getRoot()
  {
    return this.root;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/ttml/TtmlSubtitle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */