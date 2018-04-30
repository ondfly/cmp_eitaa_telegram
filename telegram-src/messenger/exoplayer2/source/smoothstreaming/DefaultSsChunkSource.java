package org.telegram.messenger.exoplayer2.source.smoothstreaming;

import android.net.Uri;
import java.io.IOException;
import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.SeekParameters;
import org.telegram.messenger.exoplayer2.extractor.mp4.FragmentedMp4Extractor;
import org.telegram.messenger.exoplayer2.extractor.mp4.Track;
import org.telegram.messenger.exoplayer2.extractor.mp4.TrackEncryptionBox;
import org.telegram.messenger.exoplayer2.source.BehindLiveWindowException;
import org.telegram.messenger.exoplayer2.source.chunk.Chunk;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkExtractorWrapper;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkHolder;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkedTrackBlacklistUtil;
import org.telegram.messenger.exoplayer2.source.chunk.ContainerMediaChunk;
import org.telegram.messenger.exoplayer2.source.chunk.MediaChunk;
import org.telegram.messenger.exoplayer2.source.smoothstreaming.manifest.SsManifest;
import org.telegram.messenger.exoplayer2.source.smoothstreaming.manifest.SsManifest.StreamElement;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSource.Factory;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.LoaderErrorThrower;
import org.telegram.messenger.exoplayer2.util.Util;

public class DefaultSsChunkSource
  implements SsChunkSource
{
  private int currentManifestChunkOffset;
  private final DataSource dataSource;
  private final ChunkExtractorWrapper[] extractorWrappers;
  private IOException fatalError;
  private SsManifest manifest;
  private final LoaderErrorThrower manifestLoaderErrorThrower;
  private final int streamElementIndex;
  private final TrackSelection trackSelection;
  
  public DefaultSsChunkSource(LoaderErrorThrower paramLoaderErrorThrower, SsManifest paramSsManifest, int paramInt, TrackSelection paramTrackSelection, DataSource paramDataSource, TrackEncryptionBox[] paramArrayOfTrackEncryptionBox)
  {
    this.manifestLoaderErrorThrower = paramLoaderErrorThrower;
    this.manifest = paramSsManifest;
    this.streamElementIndex = paramInt;
    this.trackSelection = paramTrackSelection;
    this.dataSource = paramDataSource;
    paramLoaderErrorThrower = paramSsManifest.streamElements[paramInt];
    this.extractorWrappers = new ChunkExtractorWrapper[paramTrackSelection.length()];
    paramInt = 0;
    if (paramInt < this.extractorWrappers.length)
    {
      int j = paramTrackSelection.getIndexInTrackGroup(paramInt);
      paramDataSource = paramLoaderErrorThrower.formats[j];
      if (paramLoaderErrorThrower.type == 2) {}
      for (int i = 4;; i = 0)
      {
        FragmentedMp4Extractor localFragmentedMp4Extractor = new FragmentedMp4Extractor(3, null, new Track(j, paramLoaderErrorThrower.type, paramLoaderErrorThrower.timescale, -9223372036854775807L, paramSsManifest.durationUs, paramDataSource, 0, paramArrayOfTrackEncryptionBox, i, null, null), null);
        this.extractorWrappers[paramInt] = new ChunkExtractorWrapper(localFragmentedMp4Extractor, paramLoaderErrorThrower.type, paramDataSource);
        paramInt += 1;
        break;
      }
    }
  }
  
  private static MediaChunk newMediaChunk(Format paramFormat, DataSource paramDataSource, Uri paramUri, String paramString, int paramInt1, long paramLong1, long paramLong2, int paramInt2, Object paramObject, ChunkExtractorWrapper paramChunkExtractorWrapper)
  {
    return new ContainerMediaChunk(paramDataSource, new DataSpec(paramUri, 0L, -1L, paramString), paramFormat, paramInt2, paramObject, paramLong1, paramLong2, paramInt1, 1, paramLong1, paramChunkExtractorWrapper);
  }
  
  private long resolveTimeToLiveEdgeUs(long paramLong)
  {
    if (!this.manifest.isLive) {
      return -9223372036854775807L;
    }
    SsManifest.StreamElement localStreamElement = this.manifest.streamElements[this.streamElementIndex];
    int i = localStreamElement.chunkCount - 1;
    return localStreamElement.getStartTimeUs(i) + localStreamElement.getChunkDurationUs(i) - paramLong;
  }
  
  public long getAdjustedSeekPositionUs(long paramLong, SeekParameters paramSeekParameters)
  {
    SsManifest.StreamElement localStreamElement = this.manifest.streamElements[this.streamElementIndex];
    int i = localStreamElement.getChunkIndex(paramLong);
    long l2 = localStreamElement.getStartTimeUs(i);
    if ((l2 < paramLong) && (i < localStreamElement.chunkCount - 1)) {}
    for (long l1 = localStreamElement.getStartTimeUs(i + 1);; l1 = l2) {
      return Util.resolveSeekPositionUs(paramLong, paramSeekParameters, l2, l1);
    }
  }
  
  public final void getNextChunk(MediaChunk paramMediaChunk, long paramLong1, long paramLong2, ChunkHolder paramChunkHolder)
  {
    if (this.fatalError != null) {
      return;
    }
    Object localObject = this.manifest.streamElements[this.streamElementIndex];
    if (((SsManifest.StreamElement)localObject).chunkCount == 0)
    {
      if (!this.manifest.isLive) {}
      for (bool = true;; bool = false)
      {
        paramChunkHolder.endOfStream = bool;
        return;
      }
    }
    int i;
    if (paramMediaChunk == null)
    {
      i = ((SsManifest.StreamElement)localObject).getChunkIndex(paramLong2);
      if (i < ((SsManifest.StreamElement)localObject).chunkCount) {
        break label139;
      }
      if (this.manifest.isLive) {
        break label133;
      }
    }
    label133:
    for (boolean bool = true;; bool = false)
    {
      paramChunkHolder.endOfStream = bool;
      return;
      j = paramMediaChunk.getNextChunkIndex() - this.currentManifestChunkOffset;
      i = j;
      if (j >= 0) {
        break;
      }
      this.fatalError = new BehindLiveWindowException();
      return;
    }
    label139:
    long l = resolveTimeToLiveEdgeUs(paramLong1);
    this.trackSelection.updateSelectedTrack(paramLong1, paramLong2 - paramLong1, l);
    paramLong1 = ((SsManifest.StreamElement)localObject).getStartTimeUs(i);
    paramLong2 = ((SsManifest.StreamElement)localObject).getChunkDurationUs(i);
    int j = this.currentManifestChunkOffset;
    int k = this.trackSelection.getSelectedIndex();
    paramMediaChunk = this.extractorWrappers[k];
    localObject = ((SsManifest.StreamElement)localObject).buildRequestUri(this.trackSelection.getIndexInTrackGroup(k), i);
    paramChunkHolder.chunk = newMediaChunk(this.trackSelection.getSelectedFormat(), this.dataSource, (Uri)localObject, null, i + j, paramLong1, paramLong1 + paramLong2, this.trackSelection.getSelectionReason(), this.trackSelection.getSelectionData(), paramMediaChunk);
  }
  
  public int getPreferredQueueSize(long paramLong, List<? extends MediaChunk> paramList)
  {
    if ((this.fatalError != null) || (this.trackSelection.length() < 2)) {
      return paramList.size();
    }
    return this.trackSelection.evaluateQueueSize(paramLong, paramList);
  }
  
  public void maybeThrowError()
    throws IOException
  {
    if (this.fatalError != null) {
      throw this.fatalError;
    }
    this.manifestLoaderErrorThrower.maybeThrowError();
  }
  
  public void onChunkLoadCompleted(Chunk paramChunk) {}
  
  public boolean onChunkLoadError(Chunk paramChunk, boolean paramBoolean, Exception paramException)
  {
    return (paramBoolean) && (ChunkedTrackBlacklistUtil.maybeBlacklistTrack(this.trackSelection, this.trackSelection.indexOf(paramChunk.trackFormat), paramException));
  }
  
  public void updateManifest(SsManifest paramSsManifest)
  {
    SsManifest.StreamElement localStreamElement1 = this.manifest.streamElements[this.streamElementIndex];
    int i = localStreamElement1.chunkCount;
    SsManifest.StreamElement localStreamElement2 = paramSsManifest.streamElements[this.streamElementIndex];
    if ((i == 0) || (localStreamElement2.chunkCount == 0)) {
      this.currentManifestChunkOffset += i;
    }
    for (;;)
    {
      this.manifest = paramSsManifest;
      return;
      long l1 = localStreamElement1.getStartTimeUs(i - 1);
      long l2 = localStreamElement1.getChunkDurationUs(i - 1);
      long l3 = localStreamElement2.getStartTimeUs(0);
      if (l1 + l2 <= l3) {
        this.currentManifestChunkOffset += i;
      } else {
        this.currentManifestChunkOffset += localStreamElement1.getChunkIndex(l3);
      }
    }
  }
  
  public static final class Factory
    implements SsChunkSource.Factory
  {
    private final DataSource.Factory dataSourceFactory;
    
    public Factory(DataSource.Factory paramFactory)
    {
      this.dataSourceFactory = paramFactory;
    }
    
    public SsChunkSource createChunkSource(LoaderErrorThrower paramLoaderErrorThrower, SsManifest paramSsManifest, int paramInt, TrackSelection paramTrackSelection, TrackEncryptionBox[] paramArrayOfTrackEncryptionBox)
    {
      return new DefaultSsChunkSource(paramLoaderErrorThrower, paramSsManifest, paramInt, paramTrackSelection, this.dataSourceFactory.createDataSource(), paramArrayOfTrackEncryptionBox);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/smoothstreaming/DefaultSsChunkSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */