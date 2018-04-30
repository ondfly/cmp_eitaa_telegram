package org.telegram.messenger.exoplayer2.source;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.ExoPlayer;
import org.telegram.messenger.exoplayer2.Timeline;
import org.telegram.messenger.exoplayer2.upstream.Allocator;

public abstract interface MediaSource
{
  public static final String MEDIA_SOURCE_REUSED_ERROR_MESSAGE = "MediaSource instances are not allowed to be reused.";
  
  public abstract MediaPeriod createPeriod(MediaPeriodId paramMediaPeriodId, Allocator paramAllocator);
  
  public abstract void maybeThrowSourceInfoRefreshError()
    throws IOException;
  
  public abstract void prepareSource(ExoPlayer paramExoPlayer, boolean paramBoolean, Listener paramListener);
  
  public abstract void releasePeriod(MediaPeriod paramMediaPeriod);
  
  public abstract void releaseSource();
  
  public static abstract interface Listener
  {
    public abstract void onSourceInfoRefreshed(MediaSource paramMediaSource, Timeline paramTimeline, Object paramObject);
  }
  
  public static final class MediaPeriodId
  {
    public static final MediaPeriodId UNSET = new MediaPeriodId(-1, -1, -1);
    public final int adGroupIndex;
    public final int adIndexInAdGroup;
    public final int periodIndex;
    
    public MediaPeriodId(int paramInt)
    {
      this(paramInt, -1, -1);
    }
    
    public MediaPeriodId(int paramInt1, int paramInt2, int paramInt3)
    {
      this.periodIndex = paramInt1;
      this.adGroupIndex = paramInt2;
      this.adIndexInAdGroup = paramInt3;
    }
    
    public MediaPeriodId copyWithPeriodIndex(int paramInt)
    {
      if (this.periodIndex == paramInt) {
        return this;
      }
      return new MediaPeriodId(paramInt, this.adGroupIndex, this.adIndexInAdGroup);
    }
    
    public boolean equals(Object paramObject)
    {
      if (this == paramObject) {}
      do
      {
        return true;
        if ((paramObject == null) || (getClass() != paramObject.getClass())) {
          return false;
        }
        paramObject = (MediaPeriodId)paramObject;
      } while ((this.periodIndex == ((MediaPeriodId)paramObject).periodIndex) && (this.adGroupIndex == ((MediaPeriodId)paramObject).adGroupIndex) && (this.adIndexInAdGroup == ((MediaPeriodId)paramObject).adIndexInAdGroup));
      return false;
    }
    
    public int hashCode()
    {
      return ((this.periodIndex + 527) * 31 + this.adGroupIndex) * 31 + this.adIndexInAdGroup;
    }
    
    public boolean isAd()
    {
      return this.adGroupIndex != -1;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/MediaSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */