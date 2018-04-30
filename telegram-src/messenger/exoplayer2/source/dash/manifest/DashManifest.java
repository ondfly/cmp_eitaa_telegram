package org.telegram.messenger.exoplayer2.source.dash.manifest;

import android.net.Uri;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.telegram.messenger.exoplayer2.C;

public class DashManifest
{
  public final long availabilityStartTimeMs;
  public final long durationMs;
  public final boolean dynamic;
  public final Uri location;
  public final long minBufferTimeMs;
  public final long minUpdatePeriodMs;
  private final List<Period> periods;
  public final long publishTimeMs;
  public final long suggestedPresentationDelayMs;
  public final long timeShiftBufferDepthMs;
  public final UtcTimingElement utcTiming;
  
  public DashManifest(long paramLong1, long paramLong2, long paramLong3, boolean paramBoolean, long paramLong4, long paramLong5, long paramLong6, long paramLong7, UtcTimingElement paramUtcTimingElement, Uri paramUri, List<Period> paramList)
  {
    this.availabilityStartTimeMs = paramLong1;
    this.durationMs = paramLong2;
    this.minBufferTimeMs = paramLong3;
    this.dynamic = paramBoolean;
    this.minUpdatePeriodMs = paramLong4;
    this.timeShiftBufferDepthMs = paramLong5;
    this.suggestedPresentationDelayMs = paramLong6;
    this.publishTimeMs = paramLong7;
    this.utcTiming = paramUtcTimingElement;
    this.location = paramUri;
    paramUtcTimingElement = paramList;
    if (paramList == null) {
      paramUtcTimingElement = Collections.emptyList();
    }
    this.periods = paramUtcTimingElement;
  }
  
  private static ArrayList<AdaptationSet> copyAdaptationSets(List<AdaptationSet> paramList, LinkedList<RepresentationKey> paramLinkedList)
  {
    Object localObject = (RepresentationKey)paramLinkedList.poll();
    int i = ((RepresentationKey)localObject).periodIndex;
    ArrayList localArrayList1 = new ArrayList();
    RepresentationKey localRepresentationKey;
    do
    {
      int j = ((RepresentationKey)localObject).adaptationSetIndex;
      AdaptationSet localAdaptationSet = (AdaptationSet)paramList.get(j);
      List localList = localAdaptationSet.representations;
      ArrayList localArrayList2 = new ArrayList();
      do
      {
        localArrayList2.add((Representation)localList.get(((RepresentationKey)localObject).representationIndex));
        localRepresentationKey = (RepresentationKey)paramLinkedList.poll();
        if (localRepresentationKey.periodIndex != i) {
          break;
        }
        localObject = localRepresentationKey;
      } while (localRepresentationKey.adaptationSetIndex == j);
      localArrayList1.add(new AdaptationSet(localAdaptationSet.id, localAdaptationSet.type, localArrayList2, localAdaptationSet.accessibilityDescriptors, localAdaptationSet.supplementalProperties));
      localObject = localRepresentationKey;
    } while (localRepresentationKey.periodIndex == i);
    paramLinkedList.addFirst(localRepresentationKey);
    return localArrayList1;
  }
  
  public final DashManifest copy(List<RepresentationKey> paramList)
  {
    paramList = new LinkedList(paramList);
    Collections.sort(paramList);
    paramList.add(new RepresentationKey(-1, -1, -1));
    ArrayList localArrayList1 = new ArrayList();
    long l1 = 0L;
    int i = 0;
    if (i < getPeriodCount())
    {
      long l3;
      if (((RepresentationKey)paramList.peek()).periodIndex != i)
      {
        l3 = getPeriodDurationMs(i);
        l2 = l1;
        if (l3 == -9223372036854775807L) {}
      }
      for (long l2 = l1 + l3;; l2 = l1)
      {
        i += 1;
        l1 = l2;
        break;
        Period localPeriod = getPeriod(i);
        ArrayList localArrayList2 = copyAdaptationSets(localPeriod.adaptationSets, paramList);
        localArrayList1.add(new Period(localPeriod.id, localPeriod.startMs - l1, localArrayList2, localPeriod.eventStreams));
      }
    }
    if (this.durationMs != -9223372036854775807L) {}
    for (l1 = this.durationMs - l1;; l1 = -9223372036854775807L) {
      return new DashManifest(this.availabilityStartTimeMs, l1, this.minBufferTimeMs, this.dynamic, this.minUpdatePeriodMs, this.timeShiftBufferDepthMs, this.suggestedPresentationDelayMs, this.publishTimeMs, this.utcTiming, this.location, localArrayList1);
    }
  }
  
  public final Period getPeriod(int paramInt)
  {
    return (Period)this.periods.get(paramInt);
  }
  
  public final int getPeriodCount()
  {
    return this.periods.size();
  }
  
  public final long getPeriodDurationMs(int paramInt)
  {
    if (paramInt == this.periods.size() - 1)
    {
      if (this.durationMs == -9223372036854775807L) {
        return -9223372036854775807L;
      }
      return this.durationMs - ((Period)this.periods.get(paramInt)).startMs;
    }
    return ((Period)this.periods.get(paramInt + 1)).startMs - ((Period)this.periods.get(paramInt)).startMs;
  }
  
  public final long getPeriodDurationUs(int paramInt)
  {
    return C.msToUs(getPeriodDurationMs(paramInt));
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/manifest/DashManifest.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */