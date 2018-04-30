package org.telegram.messenger.exoplayer2.source.hls;

import android.net.Uri;
import android.os.SystemClock;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.source.BehindLiveWindowException;
import org.telegram.messenger.exoplayer2.source.TrackGroup;
import org.telegram.messenger.exoplayer2.source.chunk.Chunk;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkedTrackBlacklistUtil;
import org.telegram.messenger.exoplayer2.source.chunk.DataChunk;
import org.telegram.messenger.exoplayer2.source.hls.playlist.HlsMasterPlaylist.HlsUrl;
import org.telegram.messenger.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import org.telegram.messenger.exoplayer2.source.hls.playlist.HlsMediaPlaylist.Segment;
import org.telegram.messenger.exoplayer2.source.hls.playlist.HlsPlaylistTracker;
import org.telegram.messenger.exoplayer2.trackselection.BaseTrackSelection;
import org.telegram.messenger.exoplayer2.trackselection.TrackSelection;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.util.TimestampAdjuster;
import org.telegram.messenger.exoplayer2.util.UriUtil;
import org.telegram.messenger.exoplayer2.util.Util;

class HlsChunkSource
{
  private final DataSource encryptionDataSource;
  private byte[] encryptionIv;
  private String encryptionIvString;
  private byte[] encryptionKey;
  private Uri encryptionKeyUri;
  private HlsMasterPlaylist.HlsUrl expectedPlaylistUrl;
  private final HlsExtractorFactory extractorFactory;
  private IOException fatalError;
  private boolean independentSegments;
  private boolean isTimestampMaster;
  private long liveEdgeTimeUs;
  private final DataSource mediaDataSource;
  private final List<Format> muxedCaptionFormats;
  private final HlsPlaylistTracker playlistTracker;
  private byte[] scratchSpace;
  private final TimestampAdjusterProvider timestampAdjusterProvider;
  private final TrackGroup trackGroup;
  private TrackSelection trackSelection;
  private final HlsMasterPlaylist.HlsUrl[] variants;
  
  public HlsChunkSource(HlsExtractorFactory paramHlsExtractorFactory, HlsPlaylistTracker paramHlsPlaylistTracker, HlsMasterPlaylist.HlsUrl[] paramArrayOfHlsUrl, HlsDataSourceFactory paramHlsDataSourceFactory, TimestampAdjusterProvider paramTimestampAdjusterProvider, List<Format> paramList)
  {
    this.extractorFactory = paramHlsExtractorFactory;
    this.playlistTracker = paramHlsPlaylistTracker;
    this.variants = paramArrayOfHlsUrl;
    this.timestampAdjusterProvider = paramTimestampAdjusterProvider;
    this.muxedCaptionFormats = paramList;
    this.liveEdgeTimeUs = -9223372036854775807L;
    paramHlsExtractorFactory = new Format[paramArrayOfHlsUrl.length];
    paramHlsPlaylistTracker = new int[paramArrayOfHlsUrl.length];
    int i = 0;
    while (i < paramArrayOfHlsUrl.length)
    {
      paramHlsExtractorFactory[i] = paramArrayOfHlsUrl[i].format;
      paramHlsPlaylistTracker[i] = i;
      i += 1;
    }
    this.mediaDataSource = paramHlsDataSourceFactory.createDataSource(1);
    this.encryptionDataSource = paramHlsDataSourceFactory.createDataSource(3);
    this.trackGroup = new TrackGroup(paramHlsExtractorFactory);
    this.trackSelection = new InitializationTrackSelection(this.trackGroup, paramHlsPlaylistTracker);
  }
  
  private void clearEncryptionData()
  {
    this.encryptionKeyUri = null;
    this.encryptionKey = null;
    this.encryptionIvString = null;
    this.encryptionIv = null;
  }
  
  private EncryptionKeyChunk newEncryptionKeyChunk(Uri paramUri, String paramString, int paramInt1, int paramInt2, Object paramObject)
  {
    paramUri = new DataSpec(paramUri, 0L, -1L, null, 1);
    return new EncryptionKeyChunk(this.encryptionDataSource, paramUri, this.variants[paramInt1].format, paramInt2, paramObject, this.scratchSpace, paramString);
  }
  
  private long resolveTimeToLiveEdgeUs(long paramLong)
  {
    long l = -9223372036854775807L;
    if (this.liveEdgeTimeUs != -9223372036854775807L) {}
    for (int i = 1;; i = 0)
    {
      if (i != 0) {
        l = this.liveEdgeTimeUs - paramLong;
      }
      return l;
    }
  }
  
  private void setEncryptionData(Uri paramUri, String paramString, byte[] paramArrayOfByte)
  {
    Object localObject;
    byte[] arrayOfByte;
    if (Util.toLowerInvariant(paramString).startsWith("0x"))
    {
      localObject = paramString.substring(2);
      localObject = new BigInteger((String)localObject, 16).toByteArray();
      arrayOfByte = new byte[16];
      if (localObject.length <= 16) {
        break label110;
      }
    }
    label110:
    for (int i = localObject.length - 16;; i = 0)
    {
      System.arraycopy(localObject, i, arrayOfByte, arrayOfByte.length - localObject.length + i, localObject.length - i);
      this.encryptionKeyUri = paramUri;
      this.encryptionKey = paramArrayOfByte;
      this.encryptionIvString = paramString;
      this.encryptionIv = arrayOfByte;
      return;
      localObject = paramString;
      break;
    }
  }
  
  private void updateLiveEdgeTimeUs(HlsMediaPlaylist paramHlsMediaPlaylist)
  {
    if (paramHlsMediaPlaylist.hasEndTag) {}
    for (long l = -9223372036854775807L;; l = paramHlsMediaPlaylist.getEndTimeUs())
    {
      this.liveEdgeTimeUs = l;
      return;
    }
  }
  
  public void getNextChunk(HlsMediaChunk paramHlsMediaChunk, long paramLong1, long paramLong2, HlsChunkHolder paramHlsChunkHolder)
  {
    int m;
    if (paramHlsMediaChunk == null)
    {
      j = -1;
      this.expectedPlaylistUrl = null;
      long l4 = paramLong2 - paramLong1;
      long l3 = resolveTimeToLiveEdgeUs(paramLong1);
      long l1 = l4;
      long l2 = l3;
      if (paramHlsMediaChunk != null)
      {
        l1 = l4;
        l2 = l3;
        if (!this.independentSegments)
        {
          long l5 = paramHlsMediaChunk.getDurationUs();
          l4 = Math.max(0L, l4 - l5);
          l1 = l4;
          l2 = l3;
          if (l3 != -9223372036854775807L)
          {
            l2 = Math.max(0L, l3 - l5);
            l1 = l4;
          }
        }
      }
      this.trackSelection.updateSelectedTrack(paramLong1, l1, l2);
      m = this.trackSelection.getSelectedIndexInTrackGroup();
      if (j == m) {
        break label187;
      }
    }
    Object localObject3;
    label187:
    for (int i = 1;; i = 0)
    {
      localObject3 = this.variants[m];
      if (this.playlistTracker.isSnapshotValid((HlsMasterPlaylist.HlsUrl)localObject3)) {
        break label193;
      }
      paramHlsChunkHolder.playlist = ((HlsMasterPlaylist.HlsUrl)localObject3);
      this.expectedPlaylistUrl = ((HlsMasterPlaylist.HlsUrl)localObject3);
      return;
      j = this.trackGroup.indexOf(paramHlsMediaChunk.trackFormat);
      break;
    }
    label193:
    Object localObject4 = this.playlistTracker.getPlaylistSnapshot((HlsMasterPlaylist.HlsUrl)localObject3);
    this.independentSegments = ((HlsMediaPlaylist)localObject4).hasIndependentSegmentsTag;
    updateLiveEdgeTimeUs((HlsMediaPlaylist)localObject4);
    Object localObject2;
    Object localObject1;
    int k;
    if ((paramHlsMediaChunk == null) || (i != 0)) {
      if ((paramHlsMediaChunk == null) || (this.independentSegments))
      {
        paramLong1 = paramLong2;
        if ((((HlsMediaPlaylist)localObject4).hasEndTag) || (paramLong1 < ((HlsMediaPlaylist)localObject4).getEndTimeUs())) {
          break label320;
        }
        i = ((HlsMediaPlaylist)localObject4).mediaSequence + ((HlsMediaPlaylist)localObject4).segments.size();
        localObject2 = localObject4;
        localObject1 = localObject3;
        k = m;
      }
    }
    for (;;)
    {
      if (i >= ((HlsMediaPlaylist)localObject2).mediaSequence) {
        break label480;
      }
      this.fatalError = new BehindLiveWindowException();
      return;
      paramLong1 = paramHlsMediaChunk.startTimeUs;
      break;
      label320:
      localObject1 = ((HlsMediaPlaylist)localObject4).segments;
      paramLong2 = ((HlsMediaPlaylist)localObject4).startTimeUs;
      if ((!this.playlistTracker.isLive()) || (paramHlsMediaChunk == null)) {}
      for (boolean bool = true;; bool = false)
      {
        int n = Util.binarySearchFloor((List)localObject1, Long.valueOf(paramLong1 - paramLong2), true, bool) + ((HlsMediaPlaylist)localObject4).mediaSequence;
        k = m;
        localObject1 = localObject3;
        i = n;
        localObject2 = localObject4;
        if (n >= ((HlsMediaPlaylist)localObject4).mediaSequence) {
          break;
        }
        k = m;
        localObject1 = localObject3;
        i = n;
        localObject2 = localObject4;
        if (paramHlsMediaChunk == null) {
          break;
        }
        localObject1 = this.variants[j];
        localObject2 = this.playlistTracker.getPlaylistSnapshot((HlsMasterPlaylist.HlsUrl)localObject1);
        i = paramHlsMediaChunk.getNextChunkIndex();
        k = j;
        break;
      }
      i = paramHlsMediaChunk.getNextChunkIndex();
      k = m;
      localObject1 = localObject3;
      localObject2 = localObject4;
    }
    label480:
    int j = i - ((HlsMediaPlaylist)localObject2).mediaSequence;
    if (j >= ((HlsMediaPlaylist)localObject2).segments.size())
    {
      if (((HlsMediaPlaylist)localObject2).hasEndTag)
      {
        paramHlsChunkHolder.endOfStream = true;
        return;
      }
      paramHlsChunkHolder.playlist = ((HlsMasterPlaylist.HlsUrl)localObject1);
      this.expectedPlaylistUrl = ((HlsMasterPlaylist.HlsUrl)localObject1);
      return;
    }
    localObject4 = (HlsMediaPlaylist.Segment)((HlsMediaPlaylist)localObject2).segments.get(j);
    if (((HlsMediaPlaylist.Segment)localObject4).fullSegmentEncryptionKeyUri != null)
    {
      localObject3 = UriUtil.resolveToUri(((HlsMediaPlaylist)localObject2).baseUri, ((HlsMediaPlaylist.Segment)localObject4).fullSegmentEncryptionKeyUri);
      if (!((Uri)localObject3).equals(this.encryptionKeyUri))
      {
        paramHlsChunkHolder.chunk = newEncryptionKeyChunk((Uri)localObject3, ((HlsMediaPlaylist.Segment)localObject4).encryptionIV, k, this.trackSelection.getSelectionReason(), this.trackSelection.getSelectionData());
        return;
      }
      if (!Util.areEqual(((HlsMediaPlaylist.Segment)localObject4).encryptionIV, this.encryptionIvString)) {
        setEncryptionData((Uri)localObject3, ((HlsMediaPlaylist.Segment)localObject4).encryptionIV, this.encryptionKey);
      }
    }
    for (;;)
    {
      localObject3 = null;
      Object localObject5 = ((HlsMediaPlaylist)localObject2).initializationSegment;
      if (localObject5 != null) {
        localObject3 = new DataSpec(UriUtil.resolveToUri(((HlsMediaPlaylist)localObject2).baseUri, ((HlsMediaPlaylist.Segment)localObject5).url), ((HlsMediaPlaylist.Segment)localObject5).byterangeOffset, ((HlsMediaPlaylist.Segment)localObject5).byterangeLength, null);
      }
      paramLong1 = ((HlsMediaPlaylist)localObject2).startTimeUs + ((HlsMediaPlaylist.Segment)localObject4).relativeStartTimeUs;
      j = ((HlsMediaPlaylist)localObject2).discontinuitySequence + ((HlsMediaPlaylist.Segment)localObject4).relativeDiscontinuitySequence;
      localObject5 = this.timestampAdjusterProvider.getAdjuster(j);
      DataSpec localDataSpec = new DataSpec(UriUtil.resolveToUri(((HlsMediaPlaylist)localObject2).baseUri, ((HlsMediaPlaylist.Segment)localObject4).url), ((HlsMediaPlaylist.Segment)localObject4).byterangeOffset, ((HlsMediaPlaylist.Segment)localObject4).byterangeLength, null);
      paramHlsChunkHolder.chunk = new HlsMediaChunk(this.extractorFactory, this.mediaDataSource, localDataSpec, (DataSpec)localObject3, (HlsMasterPlaylist.HlsUrl)localObject1, this.muxedCaptionFormats, this.trackSelection.getSelectionReason(), this.trackSelection.getSelectionData(), paramLong1, paramLong1 + ((HlsMediaPlaylist.Segment)localObject4).durationUs, i, j, this.isTimestampMaster, (TimestampAdjuster)localObject5, paramHlsMediaChunk, ((HlsMediaPlaylist)localObject2).drmInitData, this.encryptionKey, this.encryptionIv);
      return;
      clearEncryptionData();
    }
  }
  
  public TrackGroup getTrackGroup()
  {
    return this.trackGroup;
  }
  
  public TrackSelection getTrackSelection()
  {
    return this.trackSelection;
  }
  
  public void maybeThrowError()
    throws IOException
  {
    if (this.fatalError != null) {
      throw this.fatalError;
    }
    if (this.expectedPlaylistUrl != null) {
      this.playlistTracker.maybeThrowPlaylistRefreshError(this.expectedPlaylistUrl);
    }
  }
  
  public void onChunkLoadCompleted(Chunk paramChunk)
  {
    if ((paramChunk instanceof EncryptionKeyChunk))
    {
      paramChunk = (EncryptionKeyChunk)paramChunk;
      this.scratchSpace = paramChunk.getDataHolder();
      setEncryptionData(paramChunk.dataSpec.uri, paramChunk.iv, paramChunk.getResult());
    }
  }
  
  public boolean onChunkLoadError(Chunk paramChunk, boolean paramBoolean, IOException paramIOException)
  {
    return (paramBoolean) && (ChunkedTrackBlacklistUtil.maybeBlacklistTrack(this.trackSelection, this.trackSelection.indexOf(this.trackGroup.indexOf(paramChunk.trackFormat)), paramIOException));
  }
  
  public void onPlaylistBlacklisted(HlsMasterPlaylist.HlsUrl paramHlsUrl, long paramLong)
  {
    int i = this.trackGroup.indexOf(paramHlsUrl.format);
    if (i != -1)
    {
      i = this.trackSelection.indexOf(i);
      if (i != -1) {
        this.trackSelection.blacklist(i, paramLong);
      }
    }
  }
  
  public void reset()
  {
    this.fatalError = null;
  }
  
  public void selectTracks(TrackSelection paramTrackSelection)
  {
    this.trackSelection = paramTrackSelection;
  }
  
  public void setIsTimestampMaster(boolean paramBoolean)
  {
    this.isTimestampMaster = paramBoolean;
  }
  
  private static final class EncryptionKeyChunk
    extends DataChunk
  {
    public final String iv;
    private byte[] result;
    
    public EncryptionKeyChunk(DataSource paramDataSource, DataSpec paramDataSpec, Format paramFormat, int paramInt, Object paramObject, byte[] paramArrayOfByte, String paramString)
    {
      super(paramDataSpec, 3, paramFormat, paramInt, paramObject, paramArrayOfByte);
      this.iv = paramString;
    }
    
    protected void consume(byte[] paramArrayOfByte, int paramInt)
      throws IOException
    {
      this.result = Arrays.copyOf(paramArrayOfByte, paramInt);
    }
    
    public byte[] getResult()
    {
      return this.result;
    }
  }
  
  public static final class HlsChunkHolder
  {
    public Chunk chunk;
    public boolean endOfStream;
    public HlsMasterPlaylist.HlsUrl playlist;
    
    public HlsChunkHolder()
    {
      clear();
    }
    
    public void clear()
    {
      this.chunk = null;
      this.endOfStream = false;
      this.playlist = null;
    }
  }
  
  private static final class InitializationTrackSelection
    extends BaseTrackSelection
  {
    private int selectedIndex = indexOf(paramTrackGroup.getFormat(0));
    
    public InitializationTrackSelection(TrackGroup paramTrackGroup, int[] paramArrayOfInt)
    {
      super(paramArrayOfInt);
    }
    
    public int getSelectedIndex()
    {
      return this.selectedIndex;
    }
    
    public Object getSelectionData()
    {
      return null;
    }
    
    public int getSelectionReason()
    {
      return 0;
    }
    
    public void updateSelectedTrack(long paramLong1, long paramLong2, long paramLong3)
    {
      paramLong1 = SystemClock.elapsedRealtime();
      if (!isBlacklisted(this.selectedIndex, paramLong1)) {
        return;
      }
      int i = this.length - 1;
      while (i >= 0)
      {
        if (!isBlacklisted(i, paramLong1))
        {
          this.selectedIndex = i;
          return;
        }
        i -= 1;
      }
      throw new IllegalStateException();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/HlsChunkSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */