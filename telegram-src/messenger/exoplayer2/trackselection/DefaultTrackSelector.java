package org.telegram.messenger.exoplayer2.trackselection;

import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.telegram.messenger.exoplayer2.ExoPlaybackException;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.RendererCapabilities;
import org.telegram.messenger.exoplayer2.source.TrackGroup;
import org.telegram.messenger.exoplayer2.source.TrackGroupArray;
import org.telegram.messenger.exoplayer2.upstream.BandwidthMeter;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.Util;

public class DefaultTrackSelector
  extends MappingTrackSelector
{
  private static final float FRACTION_TO_CONSIDER_FULLSCREEN = 0.98F;
  private static final int[] NO_TRACKS = new int[0];
  private static final int WITHIN_RENDERER_CAPABILITIES_BONUS = 1000;
  private final TrackSelection.Factory adaptiveTrackSelectionFactory;
  private final AtomicReference<Parameters> paramsReference;
  
  public DefaultTrackSelector()
  {
    this((TrackSelection.Factory)null);
  }
  
  public DefaultTrackSelector(TrackSelection.Factory paramFactory)
  {
    this.adaptiveTrackSelectionFactory = paramFactory;
    this.paramsReference = new AtomicReference(Parameters.DEFAULT);
  }
  
  public DefaultTrackSelector(BandwidthMeter paramBandwidthMeter)
  {
    this(new AdaptiveTrackSelection.Factory(paramBandwidthMeter));
  }
  
  private static int compareFormatValues(int paramInt1, int paramInt2)
  {
    int i = -1;
    if (paramInt1 == -1)
    {
      paramInt1 = i;
      if (paramInt2 == -1) {
        paramInt1 = 0;
      }
      return paramInt1;
    }
    if (paramInt2 == -1) {
      return 1;
    }
    return paramInt1 - paramInt2;
  }
  
  private static int compareInts(int paramInt1, int paramInt2)
  {
    if (paramInt1 > paramInt2) {
      return 1;
    }
    if (paramInt2 > paramInt1) {
      return -1;
    }
    return 0;
  }
  
  private static void filterAdaptiveVideoTrackCountForMimeType(TrackGroup paramTrackGroup, int[] paramArrayOfInt, int paramInt1, String paramString, int paramInt2, int paramInt3, int paramInt4, List<Integer> paramList)
  {
    int i = paramList.size() - 1;
    while (i >= 0)
    {
      int j = ((Integer)paramList.get(i)).intValue();
      if (!isSupportedAdaptiveVideoTrack(paramTrackGroup.getFormat(j), paramString, paramArrayOfInt[j], paramInt1, paramInt2, paramInt3, paramInt4)) {
        paramList.remove(i);
      }
      i -= 1;
    }
  }
  
  protected static boolean formatHasLanguage(Format paramFormat, String paramString)
  {
    return (paramString != null) && (TextUtils.equals(paramString, Util.normalizeLanguageCode(paramFormat.language)));
  }
  
  protected static boolean formatHasNoLanguage(Format paramFormat)
  {
    return (TextUtils.isEmpty(paramFormat.language)) || (formatHasLanguage(paramFormat, "und"));
  }
  
  private static int getAdaptiveAudioTrackCount(TrackGroup paramTrackGroup, int[] paramArrayOfInt, AudioConfigurationTuple paramAudioConfigurationTuple)
  {
    int j = 0;
    int i = 0;
    while (i < paramTrackGroup.length)
    {
      int k = j;
      if (isSupportedAdaptiveAudioTrack(paramTrackGroup.getFormat(i), paramArrayOfInt[i], paramAudioConfigurationTuple)) {
        k = j + 1;
      }
      i += 1;
      j = k;
    }
    return j;
  }
  
  private static int[] getAdaptiveAudioTracks(TrackGroup paramTrackGroup, int[] paramArrayOfInt, boolean paramBoolean)
  {
    int j = 0;
    Object localObject1 = null;
    HashSet localHashSet = new HashSet();
    int i = 0;
    int k;
    Object localObject3;
    if (i < paramTrackGroup.length)
    {
      localObject2 = paramTrackGroup.getFormat(i);
      k = ((Format)localObject2).channelCount;
      int m = ((Format)localObject2).sampleRate;
      if (paramBoolean) {}
      for (localObject2 = null;; localObject2 = ((Format)localObject2).sampleMimeType)
      {
        localObject3 = new AudioConfigurationTuple(k, m, (String)localObject2);
        localObject2 = localObject1;
        k = j;
        if (localHashSet.add(localObject3))
        {
          m = getAdaptiveAudioTrackCount(paramTrackGroup, paramArrayOfInt, (AudioConfigurationTuple)localObject3);
          localObject2 = localObject1;
          k = j;
          if (m > j)
          {
            localObject2 = localObject3;
            k = m;
          }
        }
        i += 1;
        localObject1 = localObject2;
        j = k;
        break;
      }
    }
    if (j > 1)
    {
      localObject3 = new int[j];
      j = 0;
      i = 0;
      for (;;)
      {
        localObject2 = localObject3;
        if (i >= paramTrackGroup.length) {
          break;
        }
        k = j;
        if (isSupportedAdaptiveAudioTrack(paramTrackGroup.getFormat(i), paramArrayOfInt[i], (AudioConfigurationTuple)localObject1))
        {
          localObject3[j] = i;
          k = j + 1;
        }
        i += 1;
        j = k;
      }
    }
    Object localObject2 = NO_TRACKS;
    return (int[])localObject2;
  }
  
  private static int getAdaptiveVideoTrackCountForMimeType(TrackGroup paramTrackGroup, int[] paramArrayOfInt, int paramInt1, String paramString, int paramInt2, int paramInt3, int paramInt4, List<Integer> paramList)
  {
    int j = 0;
    int i = 0;
    while (i < paramList.size())
    {
      int m = ((Integer)paramList.get(i)).intValue();
      int k = j;
      if (isSupportedAdaptiveVideoTrack(paramTrackGroup.getFormat(m), paramString, paramArrayOfInt[m], paramInt1, paramInt2, paramInt3, paramInt4)) {
        k = j + 1;
      }
      i += 1;
      j = k;
    }
    return j;
  }
  
  private static int[] getAdaptiveVideoTracksForGroup(TrackGroup paramTrackGroup, int[] paramArrayOfInt, boolean paramBoolean1, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean2)
  {
    if (paramTrackGroup.length < 2) {
      return NO_TRACKS;
    }
    List localList = getViewportFilteredTrackIndices(paramTrackGroup, paramInt5, paramInt6, paramBoolean2);
    if (localList.size() < 2) {
      return NO_TRACKS;
    }
    Object localObject2 = null;
    Object localObject1 = null;
    if (!paramBoolean1)
    {
      HashSet localHashSet = new HashSet();
      paramInt6 = 0;
      paramInt5 = 0;
      for (;;)
      {
        localObject2 = localObject1;
        if (paramInt5 >= localList.size()) {
          break;
        }
        String str = paramTrackGroup.getFormat(((Integer)localList.get(paramInt5)).intValue()).sampleMimeType;
        localObject2 = localObject1;
        int i = paramInt6;
        if (localHashSet.add(str))
        {
          int j = getAdaptiveVideoTrackCountForMimeType(paramTrackGroup, paramArrayOfInt, paramInt1, str, paramInt2, paramInt3, paramInt4, localList);
          localObject2 = localObject1;
          i = paramInt6;
          if (j > paramInt6)
          {
            localObject2 = str;
            i = j;
          }
        }
        paramInt5 += 1;
        localObject1 = localObject2;
        paramInt6 = i;
      }
    }
    filterAdaptiveVideoTrackCountForMimeType(paramTrackGroup, paramArrayOfInt, paramInt1, (String)localObject2, paramInt2, paramInt3, paramInt4, localList);
    if (localList.size() < 2) {
      return NO_TRACKS;
    }
    return Util.toArray(localList);
  }
  
  private static Point getMaxVideoSizeInViewport(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int m = 1;
    int k = paramInt1;
    int j = paramInt2;
    int i;
    if (paramBoolean)
    {
      if (paramInt3 <= paramInt4) {
        break label77;
      }
      i = 1;
      if (paramInt1 <= paramInt2) {
        break label83;
      }
    }
    for (;;)
    {
      k = paramInt1;
      j = paramInt2;
      if (i != m)
      {
        j = paramInt1;
        k = paramInt2;
      }
      if (paramInt3 * j < paramInt4 * k) {
        break label89;
      }
      return new Point(k, Util.ceilDivide(k * paramInt4, paramInt3));
      label77:
      i = 0;
      break;
      label83:
      m = 0;
    }
    label89:
    return new Point(Util.ceilDivide(j * paramInt3, paramInt4), j);
  }
  
  private static List<Integer> getViewportFilteredTrackIndices(TrackGroup paramTrackGroup, int paramInt1, int paramInt2, boolean paramBoolean)
  {
    ArrayList localArrayList = new ArrayList(paramTrackGroup.length);
    int i = 0;
    while (i < paramTrackGroup.length)
    {
      localArrayList.add(Integer.valueOf(i));
      i += 1;
    }
    if ((paramInt1 == Integer.MAX_VALUE) || (paramInt2 == Integer.MAX_VALUE)) {}
    for (;;)
    {
      return localArrayList;
      i = Integer.MAX_VALUE;
      int j = 0;
      while (j < paramTrackGroup.length)
      {
        Format localFormat = paramTrackGroup.getFormat(j);
        int k = i;
        if (localFormat.width > 0)
        {
          k = i;
          if (localFormat.height > 0)
          {
            Point localPoint = getMaxVideoSizeInViewport(paramBoolean, paramInt1, paramInt2, localFormat.width, localFormat.height);
            int m = localFormat.width * localFormat.height;
            k = i;
            if (localFormat.width >= (int)(localPoint.x * 0.98F))
            {
              k = i;
              if (localFormat.height >= (int)(localPoint.y * 0.98F))
              {
                k = i;
                if (m < i) {
                  k = m;
                }
              }
            }
          }
        }
        j += 1;
        i = k;
      }
      if (i != Integer.MAX_VALUE)
      {
        paramInt1 = localArrayList.size() - 1;
        while (paramInt1 >= 0)
        {
          paramInt2 = paramTrackGroup.getFormat(((Integer)localArrayList.get(paramInt1)).intValue()).getPixelCount();
          if ((paramInt2 == -1) || (paramInt2 > i)) {
            localArrayList.remove(paramInt1);
          }
          paramInt1 -= 1;
        }
      }
    }
  }
  
  protected static boolean isSupported(int paramInt, boolean paramBoolean)
  {
    paramInt &= 0x7;
    return (paramInt == 4) || ((paramBoolean) && (paramInt == 3));
  }
  
  private static boolean isSupportedAdaptiveAudioTrack(Format paramFormat, int paramInt, AudioConfigurationTuple paramAudioConfigurationTuple)
  {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (isSupported(paramInt, false))
    {
      bool1 = bool2;
      if (paramFormat.channelCount == paramAudioConfigurationTuple.channelCount)
      {
        bool1 = bool2;
        if (paramFormat.sampleRate == paramAudioConfigurationTuple.sampleRate) {
          if (paramAudioConfigurationTuple.mimeType != null)
          {
            bool1 = bool2;
            if (!TextUtils.equals(paramAudioConfigurationTuple.mimeType, paramFormat.sampleMimeType)) {}
          }
          else
          {
            bool1 = true;
          }
        }
      }
    }
    return bool1;
  }
  
  private static boolean isSupportedAdaptiveVideoTrack(Format paramFormat, String paramString, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (isSupported(paramInt1, false))
    {
      bool1 = bool2;
      if ((paramInt1 & paramInt2) != 0) {
        if (paramString != null)
        {
          bool1 = bool2;
          if (!Util.areEqual(paramFormat.sampleMimeType, paramString)) {}
        }
        else if (paramFormat.width != -1)
        {
          bool1 = bool2;
          if (paramFormat.width > paramInt3) {}
        }
        else if (paramFormat.height != -1)
        {
          bool1 = bool2;
          if (paramFormat.height > paramInt4) {}
        }
        else if (paramFormat.bitrate != -1)
        {
          bool1 = bool2;
          if (paramFormat.bitrate > paramInt5) {}
        }
        else
        {
          bool1 = true;
        }
      }
    }
    return bool1;
  }
  
  private static TrackSelection selectAdaptiveVideoTrack(RendererCapabilities paramRendererCapabilities, TrackGroupArray paramTrackGroupArray, int[][] paramArrayOfInt, Parameters paramParameters, TrackSelection.Factory paramFactory)
    throws ExoPlaybackException
  {
    int i;
    boolean bool;
    label33:
    int j;
    if (paramParameters.allowNonSeamlessAdaptiveness)
    {
      i = 24;
      if ((!paramParameters.allowMixedMimeAdaptiveness) || ((paramRendererCapabilities.supportsMixedMimeTypeAdaptation() & i) == 0)) {
        break label114;
      }
      bool = true;
      j = 0;
    }
    for (;;)
    {
      if (j >= paramTrackGroupArray.length) {
        break label129;
      }
      paramRendererCapabilities = paramTrackGroupArray.get(j);
      int[] arrayOfInt = getAdaptiveVideoTracksForGroup(paramRendererCapabilities, paramArrayOfInt[j], bool, i, paramParameters.maxVideoWidth, paramParameters.maxVideoHeight, paramParameters.maxVideoBitrate, paramParameters.viewportWidth, paramParameters.viewportHeight, paramParameters.viewportOrientationMayChange);
      if (arrayOfInt.length > 0)
      {
        return paramFactory.createTrackSelection(paramRendererCapabilities, arrayOfInt);
        i = 16;
        break;
        label114:
        bool = false;
        break label33;
      }
      j += 1;
    }
    label129:
    return null;
  }
  
  private static TrackSelection selectFixedVideoTrack(TrackGroupArray paramTrackGroupArray, int[][] paramArrayOfInt, Parameters paramParameters)
  {
    Object localObject1 = null;
    int i1 = 0;
    int n = 0;
    int i3 = -1;
    int i2 = -1;
    int m = 0;
    while (m < paramTrackGroupArray.length)
    {
      TrackGroup localTrackGroup = paramTrackGroupArray.get(m);
      List localList = getViewportFilteredTrackIndices(localTrackGroup, paramParameters.viewportWidth, paramParameters.viewportHeight, paramParameters.viewportOrientationMayChange);
      int[] arrayOfInt = paramArrayOfInt[m];
      int j = 0;
      if (j < localTrackGroup.length)
      {
        int i4 = i3;
        Object localObject2 = localObject1;
        int i5 = i2;
        int i6 = i1;
        int i7 = n;
        Format localFormat;
        if (isSupported(arrayOfInt[j], paramParameters.exceedRendererCapabilitiesIfNecessary))
        {
          localFormat = localTrackGroup.getFormat(j);
          if ((!localList.contains(Integer.valueOf(j))) || ((localFormat.width != -1) && (localFormat.width > paramParameters.maxVideoWidth)) || ((localFormat.height != -1) && (localFormat.height > paramParameters.maxVideoHeight)) || ((localFormat.bitrate != -1) && (localFormat.bitrate > paramParameters.maxVideoBitrate))) {
            break label259;
          }
        }
        label259:
        for (i4 = 1;; i4 = 0)
        {
          if ((i4 != 0) || (paramParameters.exceedVideoConstraintsIfNecessary)) {
            break label265;
          }
          i7 = n;
          i6 = i1;
          i5 = i2;
          localObject2 = localObject1;
          i4 = i3;
          j += 1;
          i3 = i4;
          localObject1 = localObject2;
          i2 = i5;
          i1 = i6;
          n = i7;
          break;
        }
        label265:
        label272:
        boolean bool;
        int k;
        if (i4 != 0)
        {
          i = 2;
          bool = isSupported(arrayOfInt[j], false);
          k = i;
          if (bool) {
            k = i + 1000;
          }
          if (k <= n) {
            break label394;
          }
          i = 1;
          label307:
          if (k == n)
          {
            if (!paramParameters.forceLowestBitrate) {
              break label404;
            }
            if (compareFormatValues(localFormat.bitrate, i3) >= 0) {
              break label399;
            }
          }
        }
        label394:
        label399:
        for (int i = 1;; i = 0)
        {
          i4 = i3;
          localObject2 = localObject1;
          i5 = i2;
          i6 = i1;
          i7 = n;
          if (i == 0) {
            break;
          }
          localObject2 = localTrackGroup;
          i6 = j;
          i4 = localFormat.bitrate;
          i5 = localFormat.getPixelCount();
          i7 = k;
          break;
          i = 1;
          break label272;
          i = 0;
          break label307;
        }
        label404:
        i = localFormat.getPixelCount();
        if (i != i2)
        {
          i = compareFormatValues(i, i2);
          label423:
          if ((!bool) || (i4 == 0)) {
            break label461;
          }
          if (i <= 0) {
            break label456;
          }
          i = 1;
        }
        for (;;)
        {
          break;
          i = compareFormatValues(localFormat.bitrate, i3);
          break label423;
          label456:
          i = 0;
          continue;
          label461:
          if (i < 0) {
            i = 1;
          } else {
            i = 0;
          }
        }
      }
      m += 1;
    }
    if (localObject1 == null) {
      return null;
    }
    return new FixedTrackSelection((TrackGroup)localObject1, i1);
  }
  
  public Parameters getParameters()
  {
    return (Parameters)this.paramsReference.get();
  }
  
  protected TrackSelection selectAudioTrack(TrackGroupArray paramTrackGroupArray, int[][] paramArrayOfInt, Parameters paramParameters, TrackSelection.Factory paramFactory)
    throws ExoPlaybackException
  {
    int k = -1;
    int m = -1;
    Object localObject1 = null;
    int i = 0;
    while (i < paramTrackGroupArray.length)
    {
      TrackGroup localTrackGroup = paramTrackGroupArray.get(i);
      int[] arrayOfInt = paramArrayOfInt[i];
      int j = 0;
      while (j < localTrackGroup.length)
      {
        int i1 = m;
        int n = k;
        Object localObject2 = localObject1;
        if (isSupported(arrayOfInt[j], paramParameters.exceedRendererCapabilitiesIfNecessary))
        {
          AudioTrackScore localAudioTrackScore = new AudioTrackScore(localTrackGroup.getFormat(j), paramParameters, arrayOfInt[j]);
          if (localObject1 != null)
          {
            i1 = m;
            n = k;
            localObject2 = localObject1;
            if (localAudioTrackScore.compareTo((AudioTrackScore)localObject1) <= 0) {}
          }
          else
          {
            i1 = i;
            n = j;
            localObject2 = localAudioTrackScore;
          }
        }
        j += 1;
        m = i1;
        k = n;
        localObject1 = localObject2;
      }
      i += 1;
    }
    if (m == -1) {
      return null;
    }
    paramTrackGroupArray = paramTrackGroupArray.get(m);
    if ((!paramParameters.forceLowestBitrate) && (paramFactory != null))
    {
      paramArrayOfInt = getAdaptiveAudioTracks(paramTrackGroupArray, paramArrayOfInt[m], paramParameters.allowMixedMimeAdaptiveness);
      if (paramArrayOfInt.length > 0) {
        return paramFactory.createTrackSelection(paramTrackGroupArray, paramArrayOfInt);
      }
    }
    return new FixedTrackSelection(paramTrackGroupArray, k);
  }
  
  protected TrackSelection selectOtherTrack(int paramInt, TrackGroupArray paramTrackGroupArray, int[][] paramArrayOfInt, Parameters paramParameters)
    throws ExoPlaybackException
  {
    Object localObject1 = null;
    int n = 0;
    int m = 0;
    int k = 0;
    while (k < paramTrackGroupArray.length)
    {
      TrackGroup localTrackGroup = paramTrackGroupArray.get(k);
      int[] arrayOfInt = paramArrayOfInt[k];
      paramInt = 0;
      if (paramInt < localTrackGroup.length)
      {
        Object localObject2 = localObject1;
        int i1 = n;
        int j = m;
        int i;
        if (isSupported(arrayOfInt[paramInt], paramParameters.exceedRendererCapabilitiesIfNecessary))
        {
          if ((localTrackGroup.getFormat(paramInt).selectionFlags & 0x1) == 0) {
            break label170;
          }
          i = 1;
          label90:
          if (i == 0) {
            break label176;
          }
        }
        label170:
        label176:
        for (j = 2;; j = 1)
        {
          i = j;
          if (isSupported(arrayOfInt[paramInt], false)) {
            i = j + 1000;
          }
          localObject2 = localObject1;
          i1 = n;
          j = m;
          if (i > m)
          {
            localObject2 = localTrackGroup;
            i1 = paramInt;
            j = i;
          }
          paramInt += 1;
          localObject1 = localObject2;
          n = i1;
          m = j;
          break;
          i = 0;
          break label90;
        }
      }
      k += 1;
    }
    if (localObject1 == null) {
      return null;
    }
    return new FixedTrackSelection((TrackGroup)localObject1, n);
  }
  
  protected TrackSelection selectTextTrack(TrackGroupArray paramTrackGroupArray, int[][] paramArrayOfInt, Parameters paramParameters)
    throws ExoPlaybackException
  {
    Object localObject1 = null;
    int i1 = 0;
    int n = 0;
    int m = 0;
    while (m < paramTrackGroupArray.length)
    {
      TrackGroup localTrackGroup = paramTrackGroupArray.get(m);
      int[] arrayOfInt = paramArrayOfInt[m];
      int j = 0;
      if (j < localTrackGroup.length)
      {
        Object localObject2 = localObject1;
        int i2 = i1;
        int i = n;
        Format localFormat;
        int k;
        if (isSupported(arrayOfInt[j], paramParameters.exceedRendererCapabilitiesIfNecessary))
        {
          localFormat = localTrackGroup.getFormat(j);
          k = localFormat.selectionFlags & (paramParameters.disabledTextTrackSelectionFlags ^ 0xFFFFFFFF);
          if ((k & 0x1) == 0) {
            break label249;
          }
          i = 1;
          label108:
          if ((k & 0x2) == 0) {
            break label255;
          }
          k = 1;
          label118:
          boolean bool = formatHasLanguage(localFormat, paramParameters.preferredTextLanguage);
          if ((!bool) && ((!paramParameters.selectUndeterminedTextLanguage) || (!formatHasNoLanguage(localFormat)))) {
            break label285;
          }
          if (i == 0) {
            break label261;
          }
          i = 8;
          label158:
          if (!bool) {
            break label279;
          }
          k = 1;
          label166:
          i += k;
        }
        for (;;)
        {
          label173:
          k = i;
          if (isSupported(arrayOfInt[j], false)) {
            k = i + 1000;
          }
          localObject2 = localObject1;
          i2 = i1;
          i = n;
          if (k > n)
          {
            localObject2 = localTrackGroup;
            i2 = j;
            i = k;
          }
          label249:
          label255:
          label261:
          label279:
          label285:
          do
          {
            j += 1;
            localObject1 = localObject2;
            i1 = i2;
            n = i;
            break;
            i = 0;
            break label108;
            k = 0;
            break label118;
            if (k == 0)
            {
              i = 6;
              break label158;
            }
            i = 4;
            break label158;
            k = 0;
            break label166;
            if (i != 0)
            {
              i = 3;
              break label173;
            }
            localObject2 = localObject1;
            i2 = i1;
            i = n;
          } while (k == 0);
          if (formatHasLanguage(localFormat, paramParameters.preferredAudioLanguage)) {
            i = 2;
          } else {
            i = 1;
          }
        }
      }
      m += 1;
    }
    if (localObject1 == null) {
      return null;
    }
    return new FixedTrackSelection((TrackGroup)localObject1, i1);
  }
  
  protected TrackSelection[] selectTracks(RendererCapabilities[] paramArrayOfRendererCapabilities, TrackGroupArray[] paramArrayOfTrackGroupArray, int[][][] paramArrayOfInt)
    throws ExoPlaybackException
  {
    int i2 = paramArrayOfRendererCapabilities.length;
    TrackSelection[] arrayOfTrackSelection = new TrackSelection[i2];
    Parameters localParameters = (Parameters)this.paramsReference.get();
    int k = 0;
    int j = 0;
    int m = 0;
    int i;
    if (m < i2)
    {
      n = k;
      i = j;
      if (2 == paramArrayOfRendererCapabilities[m].getTrackType())
      {
        i = j;
        if (j == 0)
        {
          arrayOfTrackSelection[m] = selectVideoTrack(paramArrayOfRendererCapabilities[m], paramArrayOfTrackGroupArray[m], paramArrayOfInt[m], localParameters, this.adaptiveTrackSelectionFactory);
          if (arrayOfTrackSelection[m] == null) {
            break label144;
          }
          i = 1;
        }
        label107:
        if (paramArrayOfTrackGroupArray[m].length <= 0) {
          break label150;
        }
      }
      label144:
      label150:
      for (j = 1;; j = 0)
      {
        n = k | j;
        m += 1;
        k = n;
        j = i;
        break;
        i = 0;
        break label107;
      }
    }
    int n = 0;
    m = 0;
    j = 0;
    if (j < i2)
    {
      int i1 = n;
      i = m;
      switch (paramArrayOfRendererCapabilities[j].getTrackType())
      {
      default: 
        arrayOfTrackSelection[j] = selectOtherTrack(paramArrayOfRendererCapabilities[j].getTrackType(), paramArrayOfTrackGroupArray[j], paramArrayOfInt[j], localParameters);
        i = m;
        i1 = n;
      }
      label302:
      label350:
      do
      {
        do
        {
          j += 1;
          n = i1;
          m = i;
          break;
          i1 = n;
          i = m;
        } while (n != 0);
        TrackGroupArray localTrackGroupArray = paramArrayOfTrackGroupArray[j];
        int[][] arrayOfInt = paramArrayOfInt[j];
        TrackSelection.Factory localFactory;
        if (k != 0)
        {
          localFactory = null;
          arrayOfTrackSelection[j] = selectAudioTrack(localTrackGroupArray, arrayOfInt, localParameters, localFactory);
          if (arrayOfTrackSelection[j] == null) {
            break label350;
          }
        }
        for (i = 1;; i = 0)
        {
          i1 = i;
          i = m;
          break;
          localFactory = this.adaptiveTrackSelectionFactory;
          break label302;
        }
        i1 = n;
        i = m;
      } while (m != 0);
      arrayOfTrackSelection[j] = selectTextTrack(paramArrayOfTrackGroupArray[j], paramArrayOfInt[j], localParameters);
      if (arrayOfTrackSelection[j] != null) {}
      for (i = 1;; i = 0)
      {
        i1 = n;
        break;
      }
    }
    return arrayOfTrackSelection;
  }
  
  protected TrackSelection selectVideoTrack(RendererCapabilities paramRendererCapabilities, TrackGroupArray paramTrackGroupArray, int[][] paramArrayOfInt, Parameters paramParameters, TrackSelection.Factory paramFactory)
    throws ExoPlaybackException
  {
    Object localObject2 = null;
    Object localObject1 = localObject2;
    if (!paramParameters.forceLowestBitrate)
    {
      localObject1 = localObject2;
      if (paramFactory != null) {
        localObject1 = selectAdaptiveVideoTrack(paramRendererCapabilities, paramTrackGroupArray, paramArrayOfInt, paramParameters, paramFactory);
      }
    }
    paramRendererCapabilities = (RendererCapabilities)localObject1;
    if (localObject1 == null) {
      paramRendererCapabilities = selectFixedVideoTrack(paramTrackGroupArray, paramArrayOfInt, paramParameters);
    }
    return paramRendererCapabilities;
  }
  
  public void setParameters(Parameters paramParameters)
  {
    Assertions.checkNotNull(paramParameters);
    if (!((Parameters)this.paramsReference.getAndSet(paramParameters)).equals(paramParameters)) {
      invalidate();
    }
  }
  
  private static final class AudioConfigurationTuple
  {
    public final int channelCount;
    public final String mimeType;
    public final int sampleRate;
    
    public AudioConfigurationTuple(int paramInt1, int paramInt2, String paramString)
    {
      this.channelCount = paramInt1;
      this.sampleRate = paramInt2;
      this.mimeType = paramString;
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
        paramObject = (AudioConfigurationTuple)paramObject;
      } while ((this.channelCount == ((AudioConfigurationTuple)paramObject).channelCount) && (this.sampleRate == ((AudioConfigurationTuple)paramObject).sampleRate) && (TextUtils.equals(this.mimeType, ((AudioConfigurationTuple)paramObject).mimeType)));
      return false;
    }
    
    public int hashCode()
    {
      int j = this.channelCount;
      int k = this.sampleRate;
      if (this.mimeType != null) {}
      for (int i = this.mimeType.hashCode();; i = 0) {
        return (j * 31 + k) * 31 + i;
      }
    }
  }
  
  private static final class AudioTrackScore
    implements Comparable<AudioTrackScore>
  {
    private final int bitrate;
    private final int channelCount;
    private final int defaultSelectionFlagScore;
    private final int matchLanguageScore;
    private final DefaultTrackSelector.Parameters parameters;
    private final int sampleRate;
    private final int withinRendererCapabilitiesScore;
    
    public AudioTrackScore(Format paramFormat, DefaultTrackSelector.Parameters paramParameters, int paramInt)
    {
      this.parameters = paramParameters;
      if (DefaultTrackSelector.isSupported(paramInt, false))
      {
        paramInt = 1;
        this.withinRendererCapabilitiesScore = paramInt;
        if (!DefaultTrackSelector.formatHasLanguage(paramFormat, paramParameters.preferredAudioLanguage)) {
          break label92;
        }
        paramInt = 1;
        label40:
        this.matchLanguageScore = paramInt;
        if ((paramFormat.selectionFlags & 0x1) == 0) {
          break label97;
        }
      }
      label92:
      label97:
      for (paramInt = i;; paramInt = 0)
      {
        this.defaultSelectionFlagScore = paramInt;
        this.channelCount = paramFormat.channelCount;
        this.sampleRate = paramFormat.sampleRate;
        this.bitrate = paramFormat.bitrate;
        return;
        paramInt = 0;
        break;
        paramInt = 0;
        break label40;
      }
    }
    
    public int compareTo(AudioTrackScore paramAudioTrackScore)
    {
      int i = 1;
      if (this.withinRendererCapabilitiesScore != paramAudioTrackScore.withinRendererCapabilitiesScore) {
        return DefaultTrackSelector.compareInts(this.withinRendererCapabilitiesScore, paramAudioTrackScore.withinRendererCapabilitiesScore);
      }
      if (this.matchLanguageScore != paramAudioTrackScore.matchLanguageScore) {
        return DefaultTrackSelector.compareInts(this.matchLanguageScore, paramAudioTrackScore.matchLanguageScore);
      }
      if (this.defaultSelectionFlagScore != paramAudioTrackScore.defaultSelectionFlagScore) {
        return DefaultTrackSelector.compareInts(this.defaultSelectionFlagScore, paramAudioTrackScore.defaultSelectionFlagScore);
      }
      if (this.parameters.forceLowestBitrate) {
        return DefaultTrackSelector.compareInts(paramAudioTrackScore.bitrate, this.bitrate);
      }
      if (this.withinRendererCapabilitiesScore == 1) {}
      while (this.channelCount != paramAudioTrackScore.channelCount)
      {
        return DefaultTrackSelector.compareInts(this.channelCount, paramAudioTrackScore.channelCount) * i;
        i = -1;
      }
      if (this.sampleRate != paramAudioTrackScore.sampleRate) {
        return DefaultTrackSelector.compareInts(this.sampleRate, paramAudioTrackScore.sampleRate) * i;
      }
      return DefaultTrackSelector.compareInts(this.bitrate, paramAudioTrackScore.bitrate) * i;
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
        paramObject = (AudioTrackScore)paramObject;
      } while ((this.withinRendererCapabilitiesScore == ((AudioTrackScore)paramObject).withinRendererCapabilitiesScore) && (this.matchLanguageScore == ((AudioTrackScore)paramObject).matchLanguageScore) && (this.defaultSelectionFlagScore == ((AudioTrackScore)paramObject).defaultSelectionFlagScore) && (this.channelCount == ((AudioTrackScore)paramObject).channelCount) && (this.sampleRate == ((AudioTrackScore)paramObject).sampleRate) && (this.bitrate == ((AudioTrackScore)paramObject).bitrate));
      return false;
    }
    
    public int hashCode()
    {
      return ((((this.withinRendererCapabilitiesScore * 31 + this.matchLanguageScore) * 31 + this.defaultSelectionFlagScore) * 31 + this.channelCount) * 31 + this.sampleRate) * 31 + this.bitrate;
    }
  }
  
  public static final class Parameters
  {
    public static final Parameters DEFAULT = new Parameters();
    public final boolean allowMixedMimeAdaptiveness;
    public final boolean allowNonSeamlessAdaptiveness;
    public final int disabledTextTrackSelectionFlags;
    public final boolean exceedRendererCapabilitiesIfNecessary;
    public final boolean exceedVideoConstraintsIfNecessary;
    public final boolean forceLowestBitrate;
    public final int maxVideoBitrate;
    public final int maxVideoHeight;
    public final int maxVideoWidth;
    public final String preferredAudioLanguage;
    public final String preferredTextLanguage;
    public final boolean selectUndeterminedTextLanguage;
    public final int viewportHeight;
    public final boolean viewportOrientationMayChange;
    public final int viewportWidth;
    
    private Parameters()
    {
      this(null, null, false, 0, false, false, true, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, Integer.MAX_VALUE, Integer.MAX_VALUE, true);
    }
    
    private Parameters(String paramString1, String paramString2, boolean paramBoolean1, int paramInt1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean5, boolean paramBoolean6, int paramInt5, int paramInt6, boolean paramBoolean7)
    {
      this.preferredAudioLanguage = Util.normalizeLanguageCode(paramString1);
      this.preferredTextLanguage = Util.normalizeLanguageCode(paramString2);
      this.selectUndeterminedTextLanguage = paramBoolean1;
      this.disabledTextTrackSelectionFlags = paramInt1;
      this.forceLowestBitrate = paramBoolean2;
      this.allowMixedMimeAdaptiveness = paramBoolean3;
      this.allowNonSeamlessAdaptiveness = paramBoolean4;
      this.maxVideoWidth = paramInt2;
      this.maxVideoHeight = paramInt3;
      this.maxVideoBitrate = paramInt4;
      this.exceedVideoConstraintsIfNecessary = paramBoolean5;
      this.exceedRendererCapabilitiesIfNecessary = paramBoolean6;
      this.viewportWidth = paramInt5;
      this.viewportHeight = paramInt6;
      this.viewportOrientationMayChange = paramBoolean7;
    }
    
    public DefaultTrackSelector.ParametersBuilder buildUpon()
    {
      return new DefaultTrackSelector.ParametersBuilder(this, null);
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
        paramObject = (Parameters)paramObject;
      } while ((this.selectUndeterminedTextLanguage == ((Parameters)paramObject).selectUndeterminedTextLanguage) && (this.disabledTextTrackSelectionFlags == ((Parameters)paramObject).disabledTextTrackSelectionFlags) && (this.forceLowestBitrate == ((Parameters)paramObject).forceLowestBitrate) && (this.allowMixedMimeAdaptiveness == ((Parameters)paramObject).allowMixedMimeAdaptiveness) && (this.allowNonSeamlessAdaptiveness == ((Parameters)paramObject).allowNonSeamlessAdaptiveness) && (this.maxVideoWidth == ((Parameters)paramObject).maxVideoWidth) && (this.maxVideoHeight == ((Parameters)paramObject).maxVideoHeight) && (this.exceedVideoConstraintsIfNecessary == ((Parameters)paramObject).exceedVideoConstraintsIfNecessary) && (this.exceedRendererCapabilitiesIfNecessary == ((Parameters)paramObject).exceedRendererCapabilitiesIfNecessary) && (this.viewportOrientationMayChange == ((Parameters)paramObject).viewportOrientationMayChange) && (this.viewportWidth == ((Parameters)paramObject).viewportWidth) && (this.viewportHeight == ((Parameters)paramObject).viewportHeight) && (this.maxVideoBitrate == ((Parameters)paramObject).maxVideoBitrate) && (TextUtils.equals(this.preferredAudioLanguage, ((Parameters)paramObject).preferredAudioLanguage)) && (TextUtils.equals(this.preferredTextLanguage, ((Parameters)paramObject).preferredTextLanguage)));
      return false;
    }
    
    public int hashCode()
    {
      int i2 = 1;
      int i;
      int i3;
      int j;
      label27:
      int k;
      label36:
      int m;
      label46:
      int i4;
      int i5;
      int n;
      label68:
      int i1;
      if (this.selectUndeterminedTextLanguage)
      {
        i = 1;
        i3 = this.disabledTextTrackSelectionFlags;
        if (!this.forceLowestBitrate) {
          break label190;
        }
        j = 1;
        if (!this.allowMixedMimeAdaptiveness) {
          break label195;
        }
        k = 1;
        if (!this.allowNonSeamlessAdaptiveness) {
          break label200;
        }
        m = 1;
        i4 = this.maxVideoWidth;
        i5 = this.maxVideoHeight;
        if (!this.exceedVideoConstraintsIfNecessary) {
          break label206;
        }
        n = 1;
        if (!this.exceedRendererCapabilitiesIfNecessary) {
          break label212;
        }
        i1 = 1;
        label78:
        if (!this.viewportOrientationMayChange) {
          break label218;
        }
      }
      for (;;)
      {
        return (((((((((((((i * 31 + i3) * 31 + j) * 31 + k) * 31 + m) * 31 + i4) * 31 + i5) * 31 + n) * 31 + i1) * 31 + i2) * 31 + this.viewportWidth) * 31 + this.viewportHeight) * 31 + this.maxVideoBitrate) * 31 + this.preferredAudioLanguage.hashCode()) * 31 + this.preferredTextLanguage.hashCode();
        i = 0;
        break;
        label190:
        j = 0;
        break label27;
        label195:
        k = 0;
        break label36;
        label200:
        m = 0;
        break label46;
        label206:
        n = 0;
        break label68;
        label212:
        i1 = 0;
        break label78;
        label218:
        i2 = 0;
      }
    }
  }
  
  public static final class ParametersBuilder
  {
    private boolean allowMixedMimeAdaptiveness;
    private boolean allowNonSeamlessAdaptiveness;
    private int disabledTextTrackSelectionFlags;
    private boolean exceedRendererCapabilitiesIfNecessary;
    private boolean exceedVideoConstraintsIfNecessary;
    private boolean forceLowestBitrate;
    private int maxVideoBitrate;
    private int maxVideoHeight;
    private int maxVideoWidth;
    private String preferredAudioLanguage;
    private String preferredTextLanguage;
    private boolean selectUndeterminedTextLanguage;
    private int viewportHeight;
    private boolean viewportOrientationMayChange;
    private int viewportWidth;
    
    public ParametersBuilder()
    {
      this(DefaultTrackSelector.Parameters.DEFAULT);
    }
    
    private ParametersBuilder(DefaultTrackSelector.Parameters paramParameters)
    {
      this.preferredAudioLanguage = paramParameters.preferredAudioLanguage;
      this.preferredTextLanguage = paramParameters.preferredTextLanguage;
      this.selectUndeterminedTextLanguage = paramParameters.selectUndeterminedTextLanguage;
      this.disabledTextTrackSelectionFlags = paramParameters.disabledTextTrackSelectionFlags;
      this.forceLowestBitrate = paramParameters.forceLowestBitrate;
      this.allowMixedMimeAdaptiveness = paramParameters.allowMixedMimeAdaptiveness;
      this.allowNonSeamlessAdaptiveness = paramParameters.allowNonSeamlessAdaptiveness;
      this.maxVideoWidth = paramParameters.maxVideoWidth;
      this.maxVideoHeight = paramParameters.maxVideoHeight;
      this.maxVideoBitrate = paramParameters.maxVideoBitrate;
      this.exceedVideoConstraintsIfNecessary = paramParameters.exceedVideoConstraintsIfNecessary;
      this.exceedRendererCapabilitiesIfNecessary = paramParameters.exceedRendererCapabilitiesIfNecessary;
      this.viewportWidth = paramParameters.viewportWidth;
      this.viewportHeight = paramParameters.viewportHeight;
      this.viewportOrientationMayChange = paramParameters.viewportOrientationMayChange;
    }
    
    public DefaultTrackSelector.Parameters build()
    {
      return new DefaultTrackSelector.Parameters(this.preferredAudioLanguage, this.preferredTextLanguage, this.selectUndeterminedTextLanguage, this.disabledTextTrackSelectionFlags, this.forceLowestBitrate, this.allowMixedMimeAdaptiveness, this.allowNonSeamlessAdaptiveness, this.maxVideoWidth, this.maxVideoHeight, this.maxVideoBitrate, this.exceedVideoConstraintsIfNecessary, this.exceedRendererCapabilitiesIfNecessary, this.viewportWidth, this.viewportHeight, this.viewportOrientationMayChange, null);
    }
    
    public ParametersBuilder clearVideoSizeConstraints()
    {
      return setMaxVideoSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    public ParametersBuilder clearViewportSizeConstraints()
    {
      return setViewportSize(Integer.MAX_VALUE, Integer.MAX_VALUE, true);
    }
    
    public ParametersBuilder setAllowMixedMimeAdaptiveness(boolean paramBoolean)
    {
      this.allowMixedMimeAdaptiveness = paramBoolean;
      return this;
    }
    
    public ParametersBuilder setAllowNonSeamlessAdaptiveness(boolean paramBoolean)
    {
      this.allowNonSeamlessAdaptiveness = paramBoolean;
      return this;
    }
    
    public ParametersBuilder setDisabledTextTrackSelectionFlags(int paramInt)
    {
      this.disabledTextTrackSelectionFlags = paramInt;
      return this;
    }
    
    public ParametersBuilder setExceedRendererCapabilitiesIfNecessary(boolean paramBoolean)
    {
      this.exceedRendererCapabilitiesIfNecessary = paramBoolean;
      return this;
    }
    
    public ParametersBuilder setExceedVideoConstraintsIfNecessary(boolean paramBoolean)
    {
      this.exceedVideoConstraintsIfNecessary = paramBoolean;
      return this;
    }
    
    public ParametersBuilder setForceLowestBitrate(boolean paramBoolean)
    {
      this.forceLowestBitrate = paramBoolean;
      return this;
    }
    
    public ParametersBuilder setMaxVideoBitrate(int paramInt)
    {
      this.maxVideoBitrate = paramInt;
      return this;
    }
    
    public ParametersBuilder setMaxVideoSize(int paramInt1, int paramInt2)
    {
      this.maxVideoWidth = paramInt1;
      this.maxVideoHeight = paramInt2;
      return this;
    }
    
    public ParametersBuilder setMaxVideoSizeSd()
    {
      return setMaxVideoSize(1279, 719);
    }
    
    public ParametersBuilder setPreferredAudioLanguage(String paramString)
    {
      this.preferredAudioLanguage = paramString;
      return this;
    }
    
    public ParametersBuilder setPreferredTextLanguage(String paramString)
    {
      this.preferredTextLanguage = paramString;
      return this;
    }
    
    public ParametersBuilder setSelectUndeterminedTextLanguage(boolean paramBoolean)
    {
      this.selectUndeterminedTextLanguage = paramBoolean;
      return this;
    }
    
    public ParametersBuilder setViewportSize(int paramInt1, int paramInt2, boolean paramBoolean)
    {
      this.viewportWidth = paramInt1;
      this.viewportHeight = paramInt2;
      this.viewportOrientationMayChange = paramBoolean;
      return this;
    }
    
    public ParametersBuilder setViewportSizeToPhysicalDisplaySize(Context paramContext, boolean paramBoolean)
    {
      paramContext = Util.getPhysicalDisplaySize(paramContext);
      return setViewportSize(paramContext.x, paramContext.y, paramBoolean);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/trackselection/DefaultTrackSelector.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */