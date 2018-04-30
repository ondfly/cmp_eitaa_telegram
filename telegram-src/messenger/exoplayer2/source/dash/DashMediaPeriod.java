package org.telegram.messenger.exoplayer2.source.dash;

import android.util.Pair;
import android.util.SparseIntArray;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.SeekParameters;
import org.telegram.messenger.exoplayer2.source.CompositeSequenceableLoaderFactory;
import org.telegram.messenger.exoplayer2.source.EmptySampleStream;
import org.telegram.messenger.exoplayer2.source.MediaPeriod;
import org.telegram.messenger.exoplayer2.source.MediaPeriod.Callback;
import org.telegram.messenger.exoplayer2.source.MediaSourceEventListener.EventDispatcher;
import org.telegram.messenger.exoplayer2.source.SampleStream;
import org.telegram.messenger.exoplayer2.source.SequenceableLoader;
import org.telegram.messenger.exoplayer2.source.SequenceableLoader.Callback;
import org.telegram.messenger.exoplayer2.source.TrackGroup;
import org.telegram.messenger.exoplayer2.source.TrackGroupArray;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkSampleStream;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkSampleStream.EmbeddedSampleStream;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkSampleStream.ReleaseCallback;
import org.telegram.messenger.exoplayer2.source.dash.manifest.AdaptationSet;
import org.telegram.messenger.exoplayer2.source.dash.manifest.DashManifest;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Descriptor;
import org.telegram.messenger.exoplayer2.source.dash.manifest.EventStream;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Period;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Representation;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.upstream.Allocator;
import org.telegram.messenger.exoplayer2.upstream.LoaderErrorThrower;

final class DashMediaPeriod
  implements MediaPeriod, SequenceableLoader.Callback<ChunkSampleStream<DashChunkSource>>, ChunkSampleStream.ReleaseCallback<DashChunkSource>
{
  private final Allocator allocator;
  private MediaPeriod.Callback callback;
  private final DashChunkSource.Factory chunkSourceFactory;
  private SequenceableLoader compositeSequenceableLoader;
  private final CompositeSequenceableLoaderFactory compositeSequenceableLoaderFactory;
  private final long elapsedRealtimeOffset;
  private final MediaSourceEventListener.EventDispatcher eventDispatcher;
  private EventSampleStream[] eventSampleStreams;
  private List<EventStream> eventStreams;
  final int id;
  private DashManifest manifest;
  private final LoaderErrorThrower manifestLoaderErrorThrower;
  private final int minLoadableRetryCount;
  private int periodIndex;
  private final PlayerEmsgHandler playerEmsgHandler;
  private ChunkSampleStream<DashChunkSource>[] sampleStreams;
  private final IdentityHashMap<ChunkSampleStream<DashChunkSource>, PlayerEmsgHandler.PlayerTrackEmsgHandler> trackEmsgHandlerBySampleStream;
  private final TrackGroupInfo[] trackGroupInfos;
  private final TrackGroupArray trackGroups;
  
  public DashMediaPeriod(int paramInt1, DashManifest paramDashManifest, int paramInt2, DashChunkSource.Factory paramFactory, int paramInt3, MediaSourceEventListener.EventDispatcher paramEventDispatcher, long paramLong, LoaderErrorThrower paramLoaderErrorThrower, Allocator paramAllocator, CompositeSequenceableLoaderFactory paramCompositeSequenceableLoaderFactory, PlayerEmsgHandler.PlayerEmsgCallback paramPlayerEmsgCallback)
  {
    this.id = paramInt1;
    this.manifest = paramDashManifest;
    this.periodIndex = paramInt2;
    this.chunkSourceFactory = paramFactory;
    this.minLoadableRetryCount = paramInt3;
    this.eventDispatcher = paramEventDispatcher;
    this.elapsedRealtimeOffset = paramLong;
    this.manifestLoaderErrorThrower = paramLoaderErrorThrower;
    this.allocator = paramAllocator;
    this.compositeSequenceableLoaderFactory = paramCompositeSequenceableLoaderFactory;
    this.playerEmsgHandler = new PlayerEmsgHandler(paramDashManifest, paramPlayerEmsgCallback, paramAllocator);
    this.sampleStreams = newSampleStreamArray(0);
    this.eventSampleStreams = new EventSampleStream[0];
    this.trackEmsgHandlerBySampleStream = new IdentityHashMap();
    this.compositeSequenceableLoader = paramCompositeSequenceableLoaderFactory.createCompositeSequenceableLoader(this.sampleStreams);
    paramDashManifest = paramDashManifest.getPeriod(paramInt2);
    this.eventStreams = paramDashManifest.eventStreams;
    paramDashManifest = buildTrackGroups(paramDashManifest.adaptationSets, this.eventStreams);
    this.trackGroups = ((TrackGroupArray)paramDashManifest.first);
    this.trackGroupInfos = ((TrackGroupInfo[])paramDashManifest.second);
  }
  
  private static void buildManifestEventTrackGroupInfos(List<EventStream> paramList, TrackGroup[] paramArrayOfTrackGroup, TrackGroupInfo[] paramArrayOfTrackGroupInfo, int paramInt)
  {
    int i = 0;
    while (i < paramList.size())
    {
      paramArrayOfTrackGroup[paramInt] = new TrackGroup(new Format[] { Format.createSampleFormat(((EventStream)paramList.get(i)).id(), "application/x-emsg", null, -1, null) });
      paramArrayOfTrackGroupInfo[paramInt] = TrackGroupInfo.mpdEventTrack(i);
      i += 1;
      paramInt += 1;
    }
  }
  
  private static int buildPrimaryAndEmbeddedTrackGroupInfos(List<AdaptationSet> paramList, int[][] paramArrayOfInt, int paramInt, boolean[] paramArrayOfBoolean1, boolean[] paramArrayOfBoolean2, TrackGroup[] paramArrayOfTrackGroup, TrackGroupInfo[] paramArrayOfTrackGroupInfo)
  {
    int j = 0;
    int k = 0;
    if (j < paramInt)
    {
      int[] arrayOfInt = paramArrayOfInt[j];
      Object localObject = new ArrayList();
      int m = arrayOfInt.length;
      int i = 0;
      while (i < m)
      {
        ((List)localObject).addAll(((AdaptationSet)paramList.get(arrayOfInt[i])).representations);
        i += 1;
      }
      Format[] arrayOfFormat = new Format[((List)localObject).size()];
      i = 0;
      while (i < arrayOfFormat.length)
      {
        arrayOfFormat[i] = ((Representation)((List)localObject).get(i)).format;
        i += 1;
      }
      localObject = (AdaptationSet)paramList.get(arrayOfInt[0]);
      i = k + 1;
      int n;
      if (paramArrayOfBoolean1[j] != 0)
      {
        n = i + 1;
        m = i;
        i = n;
        label170:
        if (paramArrayOfBoolean2[j] == 0) {
          break label381;
        }
        int i1 = i + 1;
        n = i;
        i = i1;
      }
      for (;;)
      {
        paramArrayOfTrackGroup[k] = new TrackGroup(arrayOfFormat);
        paramArrayOfTrackGroupInfo[k] = TrackGroupInfo.primaryTrack(((AdaptationSet)localObject).type, arrayOfInt, k, m, n);
        if (m != -1)
        {
          paramArrayOfTrackGroup[m] = new TrackGroup(new Format[] { Format.createSampleFormat(((AdaptationSet)localObject).id + ":emsg", "application/x-emsg", null, -1, null) });
          paramArrayOfTrackGroupInfo[m] = TrackGroupInfo.embeddedEmsgTrack(arrayOfInt, k);
        }
        if (n != -1)
        {
          paramArrayOfTrackGroup[n] = new TrackGroup(new Format[] { Format.createTextSampleFormat(((AdaptationSet)localObject).id + ":cea608", "application/cea-608", 0, null) });
          paramArrayOfTrackGroupInfo[n] = TrackGroupInfo.embeddedCea608Track(arrayOfInt, k);
        }
        j += 1;
        k = i;
        break;
        m = -1;
        break label170;
        label381:
        n = -1;
      }
    }
    return k;
  }
  
  private ChunkSampleStream<DashChunkSource> buildSampleStream(TrackGroupInfo paramTrackGroupInfo, TrackSelection paramTrackSelection, long paramLong)
  {
    int i = 0;
    Object localObject2 = new int[2];
    Format[] arrayOfFormat2 = new Format[2];
    boolean bool1;
    boolean bool2;
    label70:
    Object localObject1;
    Format[] arrayOfFormat1;
    if (paramTrackGroupInfo.embeddedEventMessageTrackGroupIndex != -1)
    {
      bool1 = true;
      if (bool1)
      {
        arrayOfFormat2[0] = this.trackGroups.get(paramTrackGroupInfo.embeddedEventMessageTrackGroupIndex).getFormat(0);
        localObject2[0] = 4;
        i = 0 + 1;
      }
      if (paramTrackGroupInfo.embeddedCea608TrackGroupIndex == -1) {
        break label263;
      }
      bool2 = true;
      int j = i;
      if (bool2)
      {
        arrayOfFormat2[i] = this.trackGroups.get(paramTrackGroupInfo.embeddedCea608TrackGroupIndex).getFormat(0);
        localObject2[i] = 3;
        j = i + 1;
      }
      localObject1 = localObject2;
      arrayOfFormat1 = arrayOfFormat2;
      if (j < localObject2.length)
      {
        arrayOfFormat1 = (Format[])Arrays.copyOf(arrayOfFormat2, j);
        localObject1 = Arrays.copyOf((int[])localObject2, j);
      }
      if ((!this.manifest.dynamic) || (!bool1)) {
        break label269;
      }
    }
    label263:
    label269:
    for (localObject2 = this.playerEmsgHandler.newPlayerTrackEmsgHandler();; localObject2 = null)
    {
      paramTrackSelection = this.chunkSourceFactory.createDashChunkSource(this.manifestLoaderErrorThrower, this.manifest, this.periodIndex, paramTrackGroupInfo.adaptationSetIndices, paramTrackSelection, paramTrackGroupInfo.trackType, this.elapsedRealtimeOffset, bool1, bool2, (PlayerEmsgHandler.PlayerTrackEmsgHandler)localObject2);
      paramTrackGroupInfo = new ChunkSampleStream(paramTrackGroupInfo.trackType, (int[])localObject1, arrayOfFormat1, paramTrackSelection, this, this.allocator, paramLong, this.minLoadableRetryCount, this.eventDispatcher);
      this.trackEmsgHandlerBySampleStream.put(paramTrackGroupInfo, localObject2);
      return paramTrackGroupInfo;
      bool1 = false;
      break;
      bool2 = false;
      break label70;
    }
  }
  
  private static Pair<TrackGroupArray, TrackGroupInfo[]> buildTrackGroups(List<AdaptationSet> paramList, List<EventStream> paramList1)
  {
    int[][] arrayOfInt = getGroupedAdaptationSetIndices(paramList);
    int i = arrayOfInt.length;
    boolean[] arrayOfBoolean1 = new boolean[i];
    boolean[] arrayOfBoolean2 = new boolean[i];
    int j = i + identifyEmbeddedTracks(i, paramList, arrayOfInt, arrayOfBoolean1, arrayOfBoolean2) + paramList1.size();
    TrackGroup[] arrayOfTrackGroup = new TrackGroup[j];
    TrackGroupInfo[] arrayOfTrackGroupInfo = new TrackGroupInfo[j];
    buildManifestEventTrackGroupInfos(paramList1, arrayOfTrackGroup, arrayOfTrackGroupInfo, buildPrimaryAndEmbeddedTrackGroupInfos(paramList, arrayOfInt, i, arrayOfBoolean1, arrayOfBoolean2, arrayOfTrackGroup, arrayOfTrackGroupInfo));
    return Pair.create(new TrackGroupArray(arrayOfTrackGroup), arrayOfTrackGroupInfo);
  }
  
  private static Descriptor findAdaptationSetSwitchingProperty(List<Descriptor> paramList)
  {
    int i = 0;
    while (i < paramList.size())
    {
      Descriptor localDescriptor = (Descriptor)paramList.get(i);
      if ("urn:mpeg:dash:adaptation-set-switching:2016".equals(localDescriptor.schemeIdUri)) {
        return localDescriptor;
      }
      i += 1;
    }
    return null;
  }
  
  private static int[][] getGroupedAdaptationSetIndices(List<AdaptationSet> paramList)
  {
    int m = paramList.size();
    SparseIntArray localSparseIntArray = new SparseIntArray(m);
    int i = 0;
    while (i < m)
    {
      localSparseIntArray.put(((AdaptationSet)paramList.get(i)).id, i);
      i += 1;
    }
    int[][] arrayOfInt = new int[m][];
    boolean[] arrayOfBoolean = new boolean[m];
    int j = 0;
    i = 0;
    if (j < m)
    {
      if (arrayOfBoolean[j] != 0) {}
      for (;;)
      {
        j += 1;
        break;
        arrayOfBoolean[j] = true;
        Object localObject = findAdaptationSetSwitchingProperty(((AdaptationSet)paramList.get(j)).supplementalProperties);
        int k;
        if (localObject == null)
        {
          k = i + 1;
          arrayOfInt[i] = { j };
          i = k;
        }
        else
        {
          localObject = ((Descriptor)localObject).value.split(",");
          int[] arrayOfInt1 = new int[localObject.length + 1];
          arrayOfInt1[0] = j;
          k = 0;
          while (k < localObject.length)
          {
            int n = localSparseIntArray.get(Integer.parseInt(localObject[k]));
            arrayOfBoolean[n] = true;
            arrayOfInt1[(k + 1)] = n;
            k += 1;
          }
          k = i + 1;
          arrayOfInt[i] = arrayOfInt1;
          i = k;
        }
      }
    }
    if (i < m) {
      return (int[][])Arrays.copyOf(arrayOfInt, i);
    }
    return arrayOfInt;
  }
  
  private static boolean hasCea608Track(List<AdaptationSet> paramList, int[] paramArrayOfInt)
  {
    int k = paramArrayOfInt.length;
    int i = 0;
    while (i < k)
    {
      List localList = ((AdaptationSet)paramList.get(paramArrayOfInt[i])).accessibilityDescriptors;
      int j = 0;
      while (j < localList.size())
      {
        if ("urn:scte:dash:cc:cea-608:2015".equals(((Descriptor)localList.get(j)).schemeIdUri)) {
          return true;
        }
        j += 1;
      }
      i += 1;
    }
    return false;
  }
  
  private static boolean hasEventMessageTrack(List<AdaptationSet> paramList, int[] paramArrayOfInt)
  {
    int k = paramArrayOfInt.length;
    int i = 0;
    while (i < k)
    {
      List localList = ((AdaptationSet)paramList.get(paramArrayOfInt[i])).representations;
      int j = 0;
      while (j < localList.size())
      {
        if (!((Representation)localList.get(j)).inbandEventStreams.isEmpty()) {
          return true;
        }
        j += 1;
      }
      i += 1;
    }
    return false;
  }
  
  private static int identifyEmbeddedTracks(int paramInt, List<AdaptationSet> paramList, int[][] paramArrayOfInt, boolean[] paramArrayOfBoolean1, boolean[] paramArrayOfBoolean2)
  {
    int i = 0;
    int k = 0;
    while (k < paramInt)
    {
      int j = i;
      if (hasEventMessageTrack(paramList, paramArrayOfInt[k]))
      {
        paramArrayOfBoolean1[k] = true;
        j = i + 1;
      }
      i = j;
      if (hasCea608Track(paramList, paramArrayOfInt[k]))
      {
        paramArrayOfBoolean2[k] = true;
        i = j + 1;
      }
      k += 1;
    }
    return i;
  }
  
  private static ChunkSampleStream<DashChunkSource>[] newSampleStreamArray(int paramInt)
  {
    return new ChunkSampleStream[paramInt];
  }
  
  private static void releaseIfEmbeddedSampleStream(SampleStream paramSampleStream)
  {
    if ((paramSampleStream instanceof ChunkSampleStream.EmbeddedSampleStream)) {
      ((ChunkSampleStream.EmbeddedSampleStream)paramSampleStream).release();
    }
  }
  
  private void selectEmbeddedSampleStreams(TrackSelection[] paramArrayOfTrackSelection, boolean[] paramArrayOfBoolean1, SampleStream[] paramArrayOfSampleStream, boolean[] paramArrayOfBoolean2, long paramLong, Map<Integer, ChunkSampleStream<DashChunkSource>> paramMap)
  {
    int i = 0;
    if (i < paramArrayOfTrackSelection.length)
    {
      if ((((paramArrayOfSampleStream[i] instanceof ChunkSampleStream.EmbeddedSampleStream)) || ((paramArrayOfSampleStream[i] instanceof EmptySampleStream))) && ((paramArrayOfTrackSelection[i] == null) || (paramArrayOfBoolean1[i] == 0)))
      {
        releaseIfEmbeddedSampleStream(paramArrayOfSampleStream[i]);
        paramArrayOfSampleStream[i] = null;
      }
      ChunkSampleStream localChunkSampleStream;
      SampleStream localSampleStream;
      boolean bool;
      if (paramArrayOfTrackSelection[i] != null)
      {
        int j = this.trackGroups.indexOf(paramArrayOfTrackSelection[i].getTrackGroup());
        localObject = this.trackGroupInfos[j];
        if (((TrackGroupInfo)localObject).trackGroupCategory == 1)
        {
          localChunkSampleStream = (ChunkSampleStream)paramMap.get(Integer.valueOf(((TrackGroupInfo)localObject).primaryTrackGroupIndex));
          localSampleStream = paramArrayOfSampleStream[i];
          if (localChunkSampleStream != null) {
            break label182;
          }
          bool = localSampleStream instanceof EmptySampleStream;
          label137:
          if (!bool)
          {
            releaseIfEmbeddedSampleStream(localSampleStream);
            if (localChunkSampleStream != null) {
              break label215;
            }
          }
        }
      }
      label182:
      label215:
      for (Object localObject = new EmptySampleStream();; localObject = localChunkSampleStream.selectEmbeddedTrack(paramLong, ((TrackGroupInfo)localObject).trackType))
      {
        paramArrayOfSampleStream[i] = localObject;
        paramArrayOfBoolean2[i] = true;
        i += 1;
        break;
        if (((localSampleStream instanceof ChunkSampleStream.EmbeddedSampleStream)) && (((ChunkSampleStream.EmbeddedSampleStream)localSampleStream).parent == localChunkSampleStream))
        {
          bool = true;
          break label137;
        }
        bool = false;
        break label137;
      }
    }
  }
  
  private void selectEventSampleStreams(TrackSelection[] paramArrayOfTrackSelection, boolean[] paramArrayOfBoolean1, SampleStream[] paramArrayOfSampleStream, boolean[] paramArrayOfBoolean2, List<EventSampleStream> paramList)
  {
    int i = 0;
    if (i < paramArrayOfTrackSelection.length)
    {
      Object localObject;
      if ((paramArrayOfSampleStream[i] instanceof EventSampleStream))
      {
        localObject = (EventSampleStream)paramArrayOfSampleStream[i];
        if ((paramArrayOfTrackSelection[i] != null) && (paramArrayOfBoolean1[i] != 0)) {
          break label175;
        }
        paramArrayOfSampleStream[i] = null;
      }
      for (;;)
      {
        if ((paramArrayOfSampleStream[i] == null) && (paramArrayOfTrackSelection[i] != null))
        {
          int j = this.trackGroups.indexOf(paramArrayOfTrackSelection[i].getTrackGroup());
          localObject = this.trackGroupInfos[j];
          if (((TrackGroupInfo)localObject).trackGroupCategory == 2)
          {
            localObject = new EventSampleStream((EventStream)this.eventStreams.get(((TrackGroupInfo)localObject).eventStreamGroupIndex), paramArrayOfTrackSelection[i].getTrackGroup().getFormat(0), this.manifest.dynamic);
            paramArrayOfSampleStream[i] = localObject;
            paramArrayOfBoolean2[i] = true;
            paramList.add(localObject);
          }
        }
        i += 1;
        break;
        label175:
        paramList.add(localObject);
      }
    }
  }
  
  private void selectPrimarySampleStreams(TrackSelection[] paramArrayOfTrackSelection, boolean[] paramArrayOfBoolean1, SampleStream[] paramArrayOfSampleStream, boolean[] paramArrayOfBoolean2, long paramLong, Map<Integer, ChunkSampleStream<DashChunkSource>> paramMap)
  {
    int i = 0;
    if (i < paramArrayOfTrackSelection.length)
    {
      Object localObject;
      if ((paramArrayOfSampleStream[i] instanceof ChunkSampleStream))
      {
        localObject = (ChunkSampleStream)paramArrayOfSampleStream[i];
        if ((paramArrayOfTrackSelection[i] != null) && (paramArrayOfBoolean1[i] != 0)) {
          break label153;
        }
        ((ChunkSampleStream)localObject).release(this);
        paramArrayOfSampleStream[i] = null;
      }
      for (;;)
      {
        if ((paramArrayOfSampleStream[i] == null) && (paramArrayOfTrackSelection[i] != null))
        {
          int j = this.trackGroups.indexOf(paramArrayOfTrackSelection[i].getTrackGroup());
          localObject = this.trackGroupInfos[j];
          if (((TrackGroupInfo)localObject).trackGroupCategory == 0)
          {
            localObject = buildSampleStream((TrackGroupInfo)localObject, paramArrayOfTrackSelection[i], paramLong);
            paramMap.put(Integer.valueOf(j), localObject);
            paramArrayOfSampleStream[i] = localObject;
            paramArrayOfBoolean2[i] = true;
          }
        }
        i += 1;
        break;
        label153:
        paramMap.put(Integer.valueOf(this.trackGroups.indexOf(paramArrayOfTrackSelection[i].getTrackGroup())), localObject);
      }
    }
  }
  
  public boolean continueLoading(long paramLong)
  {
    return this.compositeSequenceableLoader.continueLoading(paramLong);
  }
  
  public void discardBuffer(long paramLong, boolean paramBoolean)
  {
    ChunkSampleStream[] arrayOfChunkSampleStream = this.sampleStreams;
    int j = arrayOfChunkSampleStream.length;
    int i = 0;
    while (i < j)
    {
      arrayOfChunkSampleStream[i].discardBuffer(paramLong, paramBoolean);
      i += 1;
    }
  }
  
  public long getAdjustedSeekPositionUs(long paramLong, SeekParameters paramSeekParameters)
  {
    ChunkSampleStream[] arrayOfChunkSampleStream = this.sampleStreams;
    int j = arrayOfChunkSampleStream.length;
    int i = 0;
    for (;;)
    {
      long l = paramLong;
      if (i < j)
      {
        ChunkSampleStream localChunkSampleStream = arrayOfChunkSampleStream[i];
        if (localChunkSampleStream.primaryTrackType == 2) {
          l = localChunkSampleStream.getAdjustedSeekPositionUs(paramLong, paramSeekParameters);
        }
      }
      else
      {
        return l;
      }
      i += 1;
    }
  }
  
  public long getBufferedPositionUs()
  {
    return this.compositeSequenceableLoader.getBufferedPositionUs();
  }
  
  public long getNextLoadPositionUs()
  {
    return this.compositeSequenceableLoader.getNextLoadPositionUs();
  }
  
  public TrackGroupArray getTrackGroups()
  {
    return this.trackGroups;
  }
  
  public void maybeThrowPrepareError()
    throws IOException
  {
    this.manifestLoaderErrorThrower.maybeThrowError();
  }
  
  public void onContinueLoadingRequested(ChunkSampleStream<DashChunkSource> paramChunkSampleStream)
  {
    this.callback.onContinueLoadingRequested(this);
  }
  
  public void onSampleStreamReleased(ChunkSampleStream<DashChunkSource> paramChunkSampleStream)
  {
    paramChunkSampleStream = (PlayerEmsgHandler.PlayerTrackEmsgHandler)this.trackEmsgHandlerBySampleStream.remove(paramChunkSampleStream);
    if (paramChunkSampleStream != null) {
      paramChunkSampleStream.release();
    }
  }
  
  public void prepare(MediaPeriod.Callback paramCallback, long paramLong)
  {
    this.callback = paramCallback;
    paramCallback.onPrepared(this);
  }
  
  public long readDiscontinuity()
  {
    return -9223372036854775807L;
  }
  
  public void reevaluateBuffer(long paramLong)
  {
    this.compositeSequenceableLoader.reevaluateBuffer(paramLong);
  }
  
  public void release()
  {
    this.playerEmsgHandler.release();
    ChunkSampleStream[] arrayOfChunkSampleStream = this.sampleStreams;
    int j = arrayOfChunkSampleStream.length;
    int i = 0;
    while (i < j)
    {
      arrayOfChunkSampleStream[i].release(this);
      i += 1;
    }
  }
  
  public long seekToUs(long paramLong)
  {
    int j = 0;
    Object localObject = this.sampleStreams;
    int k = localObject.length;
    int i = 0;
    while (i < k)
    {
      localObject[i].seekToUs(paramLong);
      i += 1;
    }
    localObject = this.eventSampleStreams;
    k = localObject.length;
    i = j;
    while (i < k)
    {
      localObject[i].seekToUs(paramLong);
      i += 1;
    }
    return paramLong;
  }
  
  public long selectTracks(TrackSelection[] paramArrayOfTrackSelection, boolean[] paramArrayOfBoolean1, SampleStream[] paramArrayOfSampleStream, boolean[] paramArrayOfBoolean2, long paramLong)
  {
    HashMap localHashMap = new HashMap();
    ArrayList localArrayList = new ArrayList();
    selectPrimarySampleStreams(paramArrayOfTrackSelection, paramArrayOfBoolean1, paramArrayOfSampleStream, paramArrayOfBoolean2, paramLong, localHashMap);
    selectEventSampleStreams(paramArrayOfTrackSelection, paramArrayOfBoolean1, paramArrayOfSampleStream, paramArrayOfBoolean2, localArrayList);
    selectEmbeddedSampleStreams(paramArrayOfTrackSelection, paramArrayOfBoolean1, paramArrayOfSampleStream, paramArrayOfBoolean2, paramLong, localHashMap);
    this.sampleStreams = newSampleStreamArray(localHashMap.size());
    localHashMap.values().toArray(this.sampleStreams);
    this.eventSampleStreams = new EventSampleStream[localArrayList.size()];
    localArrayList.toArray(this.eventSampleStreams);
    this.compositeSequenceableLoader = this.compositeSequenceableLoaderFactory.createCompositeSequenceableLoader(this.sampleStreams);
    return paramLong;
  }
  
  public void updateManifest(DashManifest paramDashManifest, int paramInt)
  {
    this.manifest = paramDashManifest;
    this.periodIndex = paramInt;
    this.playerEmsgHandler.updateManifest(paramDashManifest);
    if (this.sampleStreams != null)
    {
      localObject1 = this.sampleStreams;
      int j = localObject1.length;
      i = 0;
      while (i < j)
      {
        ((DashChunkSource)localObject1[i].getChunkSource()).updateManifest(paramDashManifest, paramInt);
        i += 1;
      }
      this.callback.onContinueLoadingRequested(this);
    }
    this.eventStreams = paramDashManifest.getPeriod(paramInt).eventStreams;
    Object localObject1 = this.eventSampleStreams;
    int i = localObject1.length;
    paramInt = 0;
    while (paramInt < i)
    {
      Object localObject2 = localObject1[paramInt];
      Iterator localIterator = this.eventStreams.iterator();
      while (localIterator.hasNext())
      {
        EventStream localEventStream = (EventStream)localIterator.next();
        if (localEventStream.id().equals(((EventSampleStream)localObject2).eventStreamId())) {
          ((EventSampleStream)localObject2).updateEventStream(localEventStream, paramDashManifest.dynamic);
        }
      }
      paramInt += 1;
    }
  }
  
  private static final class TrackGroupInfo
  {
    private static final int CATEGORY_EMBEDDED = 1;
    private static final int CATEGORY_MANIFEST_EVENTS = 2;
    private static final int CATEGORY_PRIMARY = 0;
    public final int[] adaptationSetIndices;
    public final int embeddedCea608TrackGroupIndex;
    public final int embeddedEventMessageTrackGroupIndex;
    public final int eventStreamGroupIndex;
    public final int primaryTrackGroupIndex;
    public final int trackGroupCategory;
    public final int trackType;
    
    private TrackGroupInfo(int paramInt1, int paramInt2, int[] paramArrayOfInt, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      this.trackType = paramInt1;
      this.adaptationSetIndices = paramArrayOfInt;
      this.trackGroupCategory = paramInt2;
      this.primaryTrackGroupIndex = paramInt3;
      this.embeddedEventMessageTrackGroupIndex = paramInt4;
      this.embeddedCea608TrackGroupIndex = paramInt5;
      this.eventStreamGroupIndex = paramInt6;
    }
    
    public static TrackGroupInfo embeddedCea608Track(int[] paramArrayOfInt, int paramInt)
    {
      return new TrackGroupInfo(3, 1, paramArrayOfInt, paramInt, -1, -1, -1);
    }
    
    public static TrackGroupInfo embeddedEmsgTrack(int[] paramArrayOfInt, int paramInt)
    {
      return new TrackGroupInfo(4, 1, paramArrayOfInt, paramInt, -1, -1, -1);
    }
    
    public static TrackGroupInfo mpdEventTrack(int paramInt)
    {
      return new TrackGroupInfo(4, 2, null, -1, -1, -1, paramInt);
    }
    
    public static TrackGroupInfo primaryTrack(int paramInt1, int[] paramArrayOfInt, int paramInt2, int paramInt3, int paramInt4)
    {
      return new TrackGroupInfo(paramInt1, 0, paramArrayOfInt, paramInt2, paramInt3, paramInt4, -1);
    }
    
    @Retention(RetentionPolicy.SOURCE)
    public static @interface TrackGroupCategory {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/DashMediaPeriod.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */