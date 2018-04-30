package org.telegram.messenger.exoplayer2.source.ads;

import android.net.Uri;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import org.telegram.messenger.exoplayer2.util.Assertions;

public final class AdPlaybackState
{
  public static final int AD_STATE_AVAILABLE = 1;
  public static final int AD_STATE_ERROR = 4;
  public static final int AD_STATE_PLAYED = 3;
  public static final int AD_STATE_SKIPPED = 2;
  public static final int AD_STATE_UNAVAILABLE = 0;
  public static final AdPlaybackState NONE = new AdPlaybackState(new long[0]);
  public final int adGroupCount;
  public final long[] adGroupTimesUs;
  public final AdGroup[] adGroups;
  public final long adResumePositionUs;
  public final long contentDurationUs;
  
  public AdPlaybackState(long[] paramArrayOfLong)
  {
    int j = paramArrayOfLong.length;
    this.adGroupCount = j;
    this.adGroupTimesUs = Arrays.copyOf(paramArrayOfLong, j);
    this.adGroups = new AdGroup[j];
    int i = 0;
    while (i < j)
    {
      this.adGroups[i] = new AdGroup();
      i += 1;
    }
    this.adResumePositionUs = 0L;
    this.contentDurationUs = -9223372036854775807L;
  }
  
  private AdPlaybackState(long[] paramArrayOfLong, AdGroup[] paramArrayOfAdGroup, long paramLong1, long paramLong2)
  {
    this.adGroupCount = paramArrayOfAdGroup.length;
    this.adGroupTimesUs = paramArrayOfLong;
    this.adGroups = paramArrayOfAdGroup;
    this.adResumePositionUs = paramLong1;
    this.contentDurationUs = paramLong2;
  }
  
  public AdPlaybackState withAdCount(int paramInt1, int paramInt2)
  {
    if (paramInt2 > 0) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkArgument(bool);
      if (this.adGroups[paramInt1].count != paramInt2) {
        break;
      }
      return this;
    }
    AdGroup[] arrayOfAdGroup = (AdGroup[])Arrays.copyOf(this.adGroups, this.adGroups.length);
    arrayOfAdGroup[paramInt1] = this.adGroups[paramInt1].withAdCount(paramInt2);
    return new AdPlaybackState(this.adGroupTimesUs, arrayOfAdGroup, this.adResumePositionUs, this.contentDurationUs);
  }
  
  public AdPlaybackState withAdDurationsUs(long[][] paramArrayOfLong)
  {
    AdGroup[] arrayOfAdGroup = (AdGroup[])Arrays.copyOf(this.adGroups, this.adGroups.length);
    int i = 0;
    while (i < this.adGroupCount)
    {
      arrayOfAdGroup[i] = arrayOfAdGroup[i].withAdDurationsUs(paramArrayOfLong[i]);
      i += 1;
    }
    return new AdPlaybackState(this.adGroupTimesUs, arrayOfAdGroup, this.adResumePositionUs, this.contentDurationUs);
  }
  
  public AdPlaybackState withAdLoadError(int paramInt1, int paramInt2)
  {
    AdGroup[] arrayOfAdGroup = (AdGroup[])Arrays.copyOf(this.adGroups, this.adGroups.length);
    arrayOfAdGroup[paramInt1] = arrayOfAdGroup[paramInt1].withAdState(4, paramInt2);
    return new AdPlaybackState(this.adGroupTimesUs, arrayOfAdGroup, this.adResumePositionUs, this.contentDurationUs);
  }
  
  public AdPlaybackState withAdResumePositionUs(long paramLong)
  {
    if (this.adResumePositionUs == paramLong) {
      return this;
    }
    return new AdPlaybackState(this.adGroupTimesUs, this.adGroups, paramLong, this.contentDurationUs);
  }
  
  public AdPlaybackState withAdUri(int paramInt1, int paramInt2, Uri paramUri)
  {
    AdGroup[] arrayOfAdGroup = (AdGroup[])Arrays.copyOf(this.adGroups, this.adGroups.length);
    arrayOfAdGroup[paramInt1] = arrayOfAdGroup[paramInt1].withAdUri(paramUri, paramInt2);
    return new AdPlaybackState(this.adGroupTimesUs, arrayOfAdGroup, this.adResumePositionUs, this.contentDurationUs);
  }
  
  public AdPlaybackState withContentDurationUs(long paramLong)
  {
    if (this.contentDurationUs == paramLong) {
      return this;
    }
    return new AdPlaybackState(this.adGroupTimesUs, this.adGroups, this.adResumePositionUs, paramLong);
  }
  
  public AdPlaybackState withPlayedAd(int paramInt1, int paramInt2)
  {
    AdGroup[] arrayOfAdGroup = (AdGroup[])Arrays.copyOf(this.adGroups, this.adGroups.length);
    arrayOfAdGroup[paramInt1] = arrayOfAdGroup[paramInt1].withAdState(3, paramInt2);
    return new AdPlaybackState(this.adGroupTimesUs, arrayOfAdGroup, this.adResumePositionUs, this.contentDurationUs);
  }
  
  public AdPlaybackState withSkippedAdGroup(int paramInt)
  {
    AdGroup[] arrayOfAdGroup = (AdGroup[])Arrays.copyOf(this.adGroups, this.adGroups.length);
    arrayOfAdGroup[paramInt] = arrayOfAdGroup[paramInt].withAllAdsSkipped();
    return new AdPlaybackState(this.adGroupTimesUs, arrayOfAdGroup, this.adResumePositionUs, this.contentDurationUs);
  }
  
  public static final class AdGroup
  {
    public final int count;
    public final long[] durationsUs;
    public final int nextAdIndexToPlay;
    public final int[] states;
    public final Uri[] uris;
    
    public AdGroup()
    {
      this(-1, new int[0], new Uri[0], new long[0]);
    }
    
    private AdGroup(int paramInt, int[] paramArrayOfInt, Uri[] paramArrayOfUri, long[] paramArrayOfLong)
    {
      boolean bool;
      if (paramArrayOfInt.length == paramArrayOfUri.length)
      {
        bool = true;
        Assertions.checkArgument(bool);
        this.count = paramInt;
        this.states = paramArrayOfInt;
        this.uris = paramArrayOfUri;
        this.durationsUs = paramArrayOfLong;
        paramInt = 0;
      }
      for (;;)
      {
        if ((paramInt >= paramArrayOfInt.length) || (paramArrayOfInt[paramInt] == 0) || (paramArrayOfInt[paramInt] == 1))
        {
          this.nextAdIndexToPlay = paramInt;
          return;
          bool = false;
          break;
        }
        paramInt += 1;
      }
    }
    
    private static long[] copyDurationsUsWithSpaceForAdCount(long[] paramArrayOfLong, int paramInt)
    {
      int i = paramArrayOfLong.length;
      paramInt = Math.max(paramInt, i);
      paramArrayOfLong = Arrays.copyOf(paramArrayOfLong, paramInt);
      Arrays.fill(paramArrayOfLong, i, paramInt, -9223372036854775807L);
      return paramArrayOfLong;
    }
    
    private static int[] copyStatesWithSpaceForAdCount(int[] paramArrayOfInt, int paramInt)
    {
      int i = paramArrayOfInt.length;
      paramInt = Math.max(paramInt, i);
      paramArrayOfInt = Arrays.copyOf(paramArrayOfInt, paramInt);
      Arrays.fill(paramArrayOfInt, i, paramInt, 0);
      return paramArrayOfInt;
    }
    
    public AdGroup withAdCount(int paramInt)
    {
      if ((this.count == -1) && (this.states.length <= paramInt)) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkArgument(bool);
        int[] arrayOfInt = copyStatesWithSpaceForAdCount(this.states, paramInt);
        long[] arrayOfLong = copyDurationsUsWithSpaceForAdCount(this.durationsUs, paramInt);
        return new AdGroup(paramInt, arrayOfInt, (Uri[])Arrays.copyOf(this.uris, paramInt), arrayOfLong);
      }
    }
    
    public AdGroup withAdDurationsUs(long[] paramArrayOfLong)
    {
      if ((this.count == -1) || (paramArrayOfLong.length <= this.uris.length)) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkArgument(bool);
        long[] arrayOfLong = paramArrayOfLong;
        if (paramArrayOfLong.length < this.uris.length) {
          arrayOfLong = copyDurationsUsWithSpaceForAdCount(paramArrayOfLong, this.uris.length);
        }
        return new AdGroup(this.count, this.states, this.uris, arrayOfLong);
      }
    }
    
    public AdGroup withAdState(int paramInt1, int paramInt2)
    {
      boolean bool2 = false;
      boolean bool1;
      int[] arrayOfInt;
      long[] arrayOfLong;
      if ((this.count == -1) || (paramInt2 < this.count))
      {
        bool1 = true;
        Assertions.checkArgument(bool1);
        arrayOfInt = copyStatesWithSpaceForAdCount(this.states, paramInt2 + 1);
        if (arrayOfInt[paramInt2] != 0)
        {
          bool1 = bool2;
          if (arrayOfInt[paramInt2] != 1) {}
        }
        else
        {
          bool1 = true;
        }
        Assertions.checkArgument(bool1);
        if (this.durationsUs.length != arrayOfInt.length) {
          break label123;
        }
        arrayOfLong = this.durationsUs;
        label78:
        if (this.uris.length != arrayOfInt.length) {
          break label138;
        }
      }
      label123:
      label138:
      for (Uri[] arrayOfUri = this.uris;; arrayOfUri = (Uri[])Arrays.copyOf(this.uris, arrayOfInt.length))
      {
        arrayOfInt[paramInt2] = paramInt1;
        return new AdGroup(this.count, arrayOfInt, arrayOfUri, arrayOfLong);
        bool1 = false;
        break;
        arrayOfLong = copyDurationsUsWithSpaceForAdCount(this.durationsUs, arrayOfInt.length);
        break label78;
      }
    }
    
    public AdGroup withAdUri(Uri paramUri, int paramInt)
    {
      boolean bool2 = false;
      boolean bool1;
      int[] arrayOfInt;
      if ((this.count == -1) || (paramInt < this.count))
      {
        bool1 = true;
        Assertions.checkArgument(bool1);
        arrayOfInt = copyStatesWithSpaceForAdCount(this.states, paramInt + 1);
        bool1 = bool2;
        if (arrayOfInt[paramInt] == 0) {
          bool1 = true;
        }
        Assertions.checkArgument(bool1);
        if (this.durationsUs.length != arrayOfInt.length) {
          break label118;
        }
      }
      label118:
      for (long[] arrayOfLong = this.durationsUs;; arrayOfLong = copyDurationsUsWithSpaceForAdCount(this.durationsUs, arrayOfInt.length))
      {
        Uri[] arrayOfUri = (Uri[])Arrays.copyOf(this.uris, arrayOfInt.length);
        arrayOfUri[paramInt] = paramUri;
        arrayOfInt[paramInt] = 1;
        return new AdGroup(this.count, arrayOfInt, arrayOfUri, arrayOfLong);
        bool1 = false;
        break;
      }
    }
    
    public AdGroup withAllAdsSkipped()
    {
      if (this.count == -1) {
        return new AdGroup(0, new int[0], new Uri[0], new long[0]);
      }
      int j = this.states.length;
      int[] arrayOfInt = Arrays.copyOf(this.states, j);
      int i = 0;
      while (i < j)
      {
        if ((arrayOfInt[i] == 1) || (arrayOfInt[i] == 0)) {
          arrayOfInt[i] = 2;
        }
        i += 1;
      }
      return new AdGroup(j, arrayOfInt, this.uris, this.durationsUs);
    }
  }
  
  @Retention(RetentionPolicy.SOURCE)
  public static @interface AdState {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/ads/AdPlaybackState.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */