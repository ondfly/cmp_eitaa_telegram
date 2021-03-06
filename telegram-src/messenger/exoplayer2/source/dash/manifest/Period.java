package org.telegram.messenger.exoplayer2.source.dash.manifest;

import java.util.Collections;
import java.util.List;

public class Period
{
  public final List<AdaptationSet> adaptationSets;
  public final List<EventStream> eventStreams;
  public final String id;
  public final long startMs;
  
  public Period(String paramString, long paramLong, List<AdaptationSet> paramList)
  {
    this(paramString, paramLong, paramList, Collections.emptyList());
  }
  
  public Period(String paramString, long paramLong, List<AdaptationSet> paramList, List<EventStream> paramList1)
  {
    this.id = paramString;
    this.startMs = paramLong;
    this.adaptationSets = Collections.unmodifiableList(paramList);
    this.eventStreams = Collections.unmodifiableList(paramList1);
  }
  
  public int getAdaptationSetIndex(int paramInt)
  {
    int j = this.adaptationSets.size();
    int i = 0;
    while (i < j)
    {
      if (((AdaptationSet)this.adaptationSets.get(i)).type == paramInt) {
        return i;
      }
      i += 1;
    }
    return -1;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/manifest/Period.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */