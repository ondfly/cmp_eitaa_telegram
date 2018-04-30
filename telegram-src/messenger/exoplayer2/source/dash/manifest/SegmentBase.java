package org.telegram.messenger.exoplayer2.source.dash.manifest;

import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.util.Util;

public abstract class SegmentBase
{
  final RangedUri initialization;
  final long presentationTimeOffset;
  final long timescale;
  
  public SegmentBase(RangedUri paramRangedUri, long paramLong1, long paramLong2)
  {
    this.initialization = paramRangedUri;
    this.timescale = paramLong1;
    this.presentationTimeOffset = paramLong2;
  }
  
  public RangedUri getInitialization(Representation paramRepresentation)
  {
    return this.initialization;
  }
  
  public long getPresentationTimeOffsetUs()
  {
    return Util.scaleLargeTimestamp(this.presentationTimeOffset, 1000000L, this.timescale);
  }
  
  public static abstract class MultiSegmentBase
    extends SegmentBase
  {
    final long duration;
    final List<SegmentBase.SegmentTimelineElement> segmentTimeline;
    final int startNumber;
    
    public MultiSegmentBase(RangedUri paramRangedUri, long paramLong1, long paramLong2, int paramInt, long paramLong3, List<SegmentBase.SegmentTimelineElement> paramList)
    {
      super(paramLong1, paramLong2);
      this.startNumber = paramInt;
      this.duration = paramLong3;
      this.segmentTimeline = paramList;
    }
    
    public int getFirstSegmentNum()
    {
      return this.startNumber;
    }
    
    public abstract int getSegmentCount(long paramLong);
    
    public final long getSegmentDurationUs(int paramInt, long paramLong)
    {
      if (this.segmentTimeline != null) {
        return ((SegmentBase.SegmentTimelineElement)this.segmentTimeline.get(paramInt - this.startNumber)).duration * 1000000L / this.timescale;
      }
      int i = getSegmentCount(paramLong);
      if ((i != -1) && (paramInt == getFirstSegmentNum() + i - 1)) {
        return paramLong - getSegmentTimeUs(paramInt);
      }
      return this.duration * 1000000L / this.timescale;
    }
    
    public int getSegmentNum(long paramLong1, long paramLong2)
    {
      int k = getFirstSegmentNum();
      int j = getSegmentCount(paramLong2);
      if (j == 0) {}
      do
      {
        return k;
        if (this.segmentTimeline != null) {
          break;
        }
        paramLong2 = this.duration * 1000000L / this.timescale;
        i = this.startNumber + (int)(paramLong1 / paramLong2);
      } while (i < k);
      if (j == -1) {
        return i;
      }
      return Math.min(i, k + j - 1);
      int i = k;
      j = k + j - 1;
      while (i <= j)
      {
        int m = i + (j - i) / 2;
        paramLong2 = getSegmentTimeUs(m);
        if (paramLong2 < paramLong1) {
          i = m + 1;
        } else if (paramLong2 > paramLong1) {
          j = m - 1;
        } else {
          return m;
        }
      }
      if (i == k) {}
      for (;;)
      {
        return i;
        i = j;
      }
    }
    
    public final long getSegmentTimeUs(int paramInt)
    {
      if (this.segmentTimeline != null) {}
      for (long l = ((SegmentBase.SegmentTimelineElement)this.segmentTimeline.get(paramInt - this.startNumber)).startTime - this.presentationTimeOffset;; l = (paramInt - this.startNumber) * this.duration) {
        return Util.scaleLargeTimestamp(l, 1000000L, this.timescale);
      }
    }
    
    public abstract RangedUri getSegmentUrl(Representation paramRepresentation, int paramInt);
    
    public boolean isExplicit()
    {
      return this.segmentTimeline != null;
    }
  }
  
  public static class SegmentList
    extends SegmentBase.MultiSegmentBase
  {
    final List<RangedUri> mediaSegments;
    
    public SegmentList(RangedUri paramRangedUri, long paramLong1, long paramLong2, int paramInt, long paramLong3, List<SegmentBase.SegmentTimelineElement> paramList, List<RangedUri> paramList1)
    {
      super(paramLong1, paramLong2, paramInt, paramLong3, paramList);
      this.mediaSegments = paramList1;
    }
    
    public int getSegmentCount(long paramLong)
    {
      return this.mediaSegments.size();
    }
    
    public RangedUri getSegmentUrl(Representation paramRepresentation, int paramInt)
    {
      return (RangedUri)this.mediaSegments.get(paramInt - this.startNumber);
    }
    
    public boolean isExplicit()
    {
      return true;
    }
  }
  
  public static class SegmentTemplate
    extends SegmentBase.MultiSegmentBase
  {
    final UrlTemplate initializationTemplate;
    final UrlTemplate mediaTemplate;
    
    public SegmentTemplate(RangedUri paramRangedUri, long paramLong1, long paramLong2, int paramInt, long paramLong3, List<SegmentBase.SegmentTimelineElement> paramList, UrlTemplate paramUrlTemplate1, UrlTemplate paramUrlTemplate2)
    {
      super(paramLong1, paramLong2, paramInt, paramLong3, paramList);
      this.initializationTemplate = paramUrlTemplate1;
      this.mediaTemplate = paramUrlTemplate2;
    }
    
    public RangedUri getInitialization(Representation paramRepresentation)
    {
      if (this.initializationTemplate != null) {
        return new RangedUri(this.initializationTemplate.buildUri(paramRepresentation.format.id, 0, paramRepresentation.format.bitrate, 0L), 0L, -1L);
      }
      return super.getInitialization(paramRepresentation);
    }
    
    public int getSegmentCount(long paramLong)
    {
      if (this.segmentTimeline != null) {
        return this.segmentTimeline.size();
      }
      if (paramLong != -9223372036854775807L) {
        return (int)Util.ceilDivide(paramLong, this.duration * 1000000L / this.timescale);
      }
      return -1;
    }
    
    public RangedUri getSegmentUrl(Representation paramRepresentation, int paramInt)
    {
      if (this.segmentTimeline != null) {}
      for (long l = ((SegmentBase.SegmentTimelineElement)this.segmentTimeline.get(paramInt - this.startNumber)).startTime;; l = (paramInt - this.startNumber) * this.duration) {
        return new RangedUri(this.mediaTemplate.buildUri(paramRepresentation.format.id, paramInt, paramRepresentation.format.bitrate, l), 0L, -1L);
      }
    }
  }
  
  public static class SegmentTimelineElement
  {
    final long duration;
    final long startTime;
    
    public SegmentTimelineElement(long paramLong1, long paramLong2)
    {
      this.startTime = paramLong1;
      this.duration = paramLong2;
    }
  }
  
  public static class SingleSegmentBase
    extends SegmentBase
  {
    final long indexLength;
    final long indexStart;
    
    public SingleSegmentBase()
    {
      this(null, 1L, 0L, 0L, 0L);
    }
    
    public SingleSegmentBase(RangedUri paramRangedUri, long paramLong1, long paramLong2, long paramLong3, long paramLong4)
    {
      super(paramLong1, paramLong2);
      this.indexStart = paramLong3;
      this.indexLength = paramLong4;
    }
    
    public RangedUri getIndex()
    {
      if (this.indexLength <= 0L) {
        return null;
      }
      return new RangedUri(null, this.indexStart, this.indexLength);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/manifest/SegmentBase.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */