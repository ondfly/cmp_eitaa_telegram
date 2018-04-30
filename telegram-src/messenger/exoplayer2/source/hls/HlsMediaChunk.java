package org.telegram.messenger.exoplayer2.source.hls;

import android.util.Pair;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.drm.DrmInitData;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.metadata.Metadata;
import org.telegram.messenger.exoplayer2.metadata.id3.Id3Decoder;
import org.telegram.messenger.exoplayer2.metadata.id3.PrivFrame;
import org.telegram.messenger.exoplayer2.source.chunk.MediaChunk;
import org.telegram.messenger.exoplayer2.source.hls.playlist.HlsMasterPlaylist.HlsUrl;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.TimestampAdjuster;

final class HlsMediaChunk
  extends MediaChunk
{
  private static final String PRIV_TIMESTAMP_FRAME_OWNER = "com.apple.streaming.transportStreamTimestamp";
  private static final AtomicInteger uidSource = new AtomicInteger();
  private int bytesLoaded;
  public final int discontinuitySequenceNumber;
  private final Extractor extractor;
  public final HlsMasterPlaylist.HlsUrl hlsUrl;
  private final ParsableByteArray id3Data;
  private final Id3Decoder id3Decoder;
  private boolean id3TimestampPeeked;
  private final DataSource initDataSource;
  private final DataSpec initDataSpec;
  private boolean initLoadCompleted;
  private int initSegmentBytesLoaded;
  private final boolean isEncrypted;
  private final boolean isMasterTimestampSource;
  private final boolean isPackedAudioExtractor;
  private volatile boolean loadCanceled;
  private volatile boolean loadCompleted;
  private HlsSampleStreamWrapper output;
  private final boolean reusingExtractor;
  private final boolean shouldSpliceIn;
  private final TimestampAdjuster timestampAdjuster;
  public final int uid;
  
  public HlsMediaChunk(HlsExtractorFactory paramHlsExtractorFactory, DataSource paramDataSource, DataSpec paramDataSpec1, DataSpec paramDataSpec2, HlsMasterPlaylist.HlsUrl paramHlsUrl, List<Format> paramList, int paramInt1, Object paramObject, long paramLong1, long paramLong2, int paramInt2, int paramInt3, boolean paramBoolean, TimestampAdjuster paramTimestampAdjuster, HlsMediaChunk paramHlsMediaChunk, DrmInitData paramDrmInitData, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    super(buildDataSource(paramDataSource, paramArrayOfByte1, paramArrayOfByte2), paramDataSpec1, paramHlsUrl.format, paramInt1, paramObject, paramLong1, paramLong2, paramInt2);
    this.discontinuitySequenceNumber = paramInt3;
    this.initDataSpec = paramDataSpec2;
    this.hlsUrl = paramHlsUrl;
    this.isMasterTimestampSource = paramBoolean;
    this.timestampAdjuster = paramTimestampAdjuster;
    this.isEncrypted = (this.dataSource instanceof Aes128DataSource);
    paramObject = null;
    if (paramHlsMediaChunk != null) {
      if (paramHlsMediaChunk.hlsUrl != paramHlsUrl)
      {
        paramBoolean = true;
        this.shouldSpliceIn = paramBoolean;
        if ((paramHlsMediaChunk.discontinuitySequenceNumber == paramInt3) && (!this.shouldSpliceIn)) {
          break label263;
        }
        paramHlsUrl = null;
        label116:
        paramHlsExtractorFactory = paramHlsExtractorFactory.createExtractor(paramHlsUrl, paramDataSpec1.uri, this.trackFormat, paramList, paramDrmInitData, paramTimestampAdjuster);
        this.extractor = ((Extractor)paramHlsExtractorFactory.first);
        this.isPackedAudioExtractor = ((Boolean)paramHlsExtractorFactory.second).booleanValue();
        if (this.extractor != paramHlsUrl) {
          break label285;
        }
        paramBoolean = true;
        label176:
        this.reusingExtractor = paramBoolean;
        if ((!this.reusingExtractor) || (paramDataSpec2 == null)) {
          break label291;
        }
        paramBoolean = true;
        label197:
        this.initLoadCompleted = paramBoolean;
        if (!this.isPackedAudioExtractor) {
          break label324;
        }
        if ((paramHlsMediaChunk == null) || (paramHlsMediaChunk.id3Data == null)) {
          break label297;
        }
        this.id3Decoder = paramHlsMediaChunk.id3Decoder;
        this.id3Data = paramHlsMediaChunk.id3Data;
      }
    }
    for (;;)
    {
      this.initDataSource = paramDataSource;
      this.uid = uidSource.getAndIncrement();
      return;
      paramBoolean = false;
      break;
      label263:
      paramHlsUrl = paramHlsMediaChunk.extractor;
      break label116;
      this.shouldSpliceIn = false;
      paramHlsUrl = (HlsMasterPlaylist.HlsUrl)paramObject;
      break label116;
      label285:
      paramBoolean = false;
      break label176;
      label291:
      paramBoolean = false;
      break label197;
      label297:
      this.id3Decoder = new Id3Decoder();
      this.id3Data = new ParsableByteArray(10);
      continue;
      label324:
      this.id3Decoder = null;
      this.id3Data = null;
    }
  }
  
  private static DataSource buildDataSource(DataSource paramDataSource, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    Object localObject = paramDataSource;
    if (paramArrayOfByte1 != null) {
      localObject = new Aes128DataSource(paramDataSource, paramArrayOfByte1, paramArrayOfByte2);
    }
    return (DataSource)localObject;
  }
  
  /* Error */
  private void loadMedia()
    throws IOException, InterruptedException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 82	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:isEncrypted	Z
    //   4: ifeq +165 -> 169
    //   7: aload_0
    //   8: getfield 157	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   11: astore 4
    //   13: aload_0
    //   14: getfield 159	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:bytesLoaded	I
    //   17: ifeq +147 -> 164
    //   20: iconst_1
    //   21: istore_1
    //   22: aload_0
    //   23: getfield 73	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:isMasterTimestampSource	Z
    //   26: ifne +162 -> 188
    //   29: aload_0
    //   30: getfield 75	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:timestampAdjuster	Lorg/telegram/messenger/exoplayer2/util/TimestampAdjuster;
    //   33: invokevirtual 164	org/telegram/messenger/exoplayer2/util/TimestampAdjuster:waitUntilInitialized	()V
    //   36: new 166	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorInput
    //   39: dup
    //   40: aload_0
    //   41: getfield 78	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSource	Lorg/telegram/messenger/exoplayer2/upstream/DataSource;
    //   44: aload 4
    //   46: getfield 170	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   49: aload_0
    //   50: getfield 78	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSource	Lorg/telegram/messenger/exoplayer2/upstream/DataSource;
    //   53: aload 4
    //   55: invokeinterface 176 2 0
    //   60: invokespecial 179	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorInput:<init>	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;JJ)V
    //   63: astore 4
    //   65: aload_0
    //   66: getfield 120	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:isPackedAudioExtractor	Z
    //   69: ifeq +51 -> 120
    //   72: aload_0
    //   73: getfield 181	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:id3TimestampPeeked	Z
    //   76: ifne +44 -> 120
    //   79: aload_0
    //   80: aload 4
    //   82: invokespecial 185	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:peekId3PrivTimestamp	(Lorg/telegram/messenger/exoplayer2/extractor/ExtractorInput;)J
    //   85: lstore_2
    //   86: aload_0
    //   87: iconst_1
    //   88: putfield 181	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:id3TimestampPeeked	Z
    //   91: aload_0
    //   92: getfield 187	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:output	Lorg/telegram/messenger/exoplayer2/source/hls/HlsSampleStreamWrapper;
    //   95: astore 5
    //   97: lload_2
    //   98: ldc2_w 188
    //   101: lcmp
    //   102: ifeq +114 -> 216
    //   105: aload_0
    //   106: getfield 75	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:timestampAdjuster	Lorg/telegram/messenger/exoplayer2/util/TimestampAdjuster;
    //   109: lload_2
    //   110: invokevirtual 193	org/telegram/messenger/exoplayer2/util/TimestampAdjuster:adjustTsTimestamp	(J)J
    //   113: lstore_2
    //   114: aload 5
    //   116: lload_2
    //   117: invokevirtual 199	org/telegram/messenger/exoplayer2/source/hls/HlsSampleStreamWrapper:setSampleOffsetUs	(J)V
    //   120: iload_1
    //   121: ifeq +14 -> 135
    //   124: aload 4
    //   126: aload_0
    //   127: getfield 159	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:bytesLoaded	I
    //   130: invokeinterface 204 2 0
    //   135: iconst_0
    //   136: istore_1
    //   137: iload_1
    //   138: ifne +86 -> 224
    //   141: aload_0
    //   142: getfield 206	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:loadCanceled	Z
    //   145: ifne +79 -> 224
    //   148: aload_0
    //   149: getfield 109	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:extractor	Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
    //   152: aload 4
    //   154: aconst_null
    //   155: invokeinterface 210 3 0
    //   160: istore_1
    //   161: goto -24 -> 137
    //   164: iconst_0
    //   165: istore_1
    //   166: goto -144 -> 22
    //   169: aload_0
    //   170: getfield 157	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   173: aload_0
    //   174: getfield 159	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:bytesLoaded	I
    //   177: i2l
    //   178: invokevirtual 214	org/telegram/messenger/exoplayer2/upstream/DataSpec:subrange	(J)Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   181: astore 4
    //   183: iconst_0
    //   184: istore_1
    //   185: goto -163 -> 22
    //   188: aload_0
    //   189: getfield 75	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:timestampAdjuster	Lorg/telegram/messenger/exoplayer2/util/TimestampAdjuster;
    //   192: invokevirtual 218	org/telegram/messenger/exoplayer2/util/TimestampAdjuster:getFirstSampleTimestampUs	()J
    //   195: ldc2_w 219
    //   198: lcmp
    //   199: ifne -163 -> 36
    //   202: aload_0
    //   203: getfield 75	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:timestampAdjuster	Lorg/telegram/messenger/exoplayer2/util/TimestampAdjuster;
    //   206: aload_0
    //   207: getfield 223	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:startTimeUs	J
    //   210: invokevirtual 226	org/telegram/messenger/exoplayer2/util/TimestampAdjuster:setFirstSampleTimestampUs	(J)V
    //   213: goto -177 -> 36
    //   216: aload_0
    //   217: getfield 223	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:startTimeUs	J
    //   220: lstore_2
    //   221: goto -107 -> 114
    //   224: aload_0
    //   225: aload 4
    //   227: invokeinterface 229 1 0
    //   232: aload_0
    //   233: getfield 157	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   236: getfield 170	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   239: lsub
    //   240: l2i
    //   241: putfield 159	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:bytesLoaded	I
    //   244: aload_0
    //   245: getfield 78	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSource	Lorg/telegram/messenger/exoplayer2/upstream/DataSource;
    //   248: invokestatic 235	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;)V
    //   251: aload_0
    //   252: iconst_1
    //   253: putfield 237	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:loadCompleted	Z
    //   256: return
    //   257: astore 5
    //   259: aload_0
    //   260: aload 4
    //   262: invokeinterface 229 1 0
    //   267: aload_0
    //   268: getfield 157	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   271: getfield 170	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   274: lsub
    //   275: l2i
    //   276: putfield 159	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:bytesLoaded	I
    //   279: aload 5
    //   281: athrow
    //   282: astore 4
    //   284: aload_0
    //   285: getfield 78	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSource	Lorg/telegram/messenger/exoplayer2/upstream/DataSource;
    //   288: invokestatic 235	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;)V
    //   291: aload 4
    //   293: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	294	0	this	HlsMediaChunk
    //   21	164	1	i	int
    //   85	136	2	l	long
    //   11	250	4	localObject1	Object
    //   282	10	4	localObject2	Object
    //   95	20	5	localHlsSampleStreamWrapper	HlsSampleStreamWrapper
    //   257	23	5	localObject3	Object
    // Exception table:
    //   from	to	target	type
    //   141	161	257	finally
    //   36	97	282	finally
    //   105	114	282	finally
    //   114	120	282	finally
    //   124	135	282	finally
    //   216	221	282	finally
    //   224	244	282	finally
    //   259	282	282	finally
  }
  
  /* Error */
  private void maybeLoadInitData()
    throws IOException, InterruptedException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 124	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initLoadCompleted	Z
    //   4: ifne +10 -> 14
    //   7: aload_0
    //   8: getfield 69	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initDataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   11: ifnonnull +4 -> 15
    //   14: return
    //   15: aload_0
    //   16: getfield 69	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initDataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   19: aload_0
    //   20: getfield 241	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initSegmentBytesLoaded	I
    //   23: i2l
    //   24: invokevirtual 214	org/telegram/messenger/exoplayer2/upstream/DataSpec:subrange	(J)Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   27: astore_2
    //   28: new 166	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorInput
    //   31: dup
    //   32: aload_0
    //   33: getfield 130	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initDataSource	Lorg/telegram/messenger/exoplayer2/upstream/DataSource;
    //   36: aload_2
    //   37: getfield 170	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   40: aload_0
    //   41: getfield 130	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initDataSource	Lorg/telegram/messenger/exoplayer2/upstream/DataSource;
    //   44: aload_2
    //   45: invokeinterface 176 2 0
    //   50: invokespecial 179	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorInput:<init>	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;JJ)V
    //   53: astore_2
    //   54: iconst_0
    //   55: istore_1
    //   56: iload_1
    //   57: ifne +25 -> 82
    //   60: aload_0
    //   61: getfield 206	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:loadCanceled	Z
    //   64: ifne +18 -> 82
    //   67: aload_0
    //   68: getfield 109	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:extractor	Lorg/telegram/messenger/exoplayer2/extractor/Extractor;
    //   71: aload_2
    //   72: aconst_null
    //   73: invokeinterface 210 3 0
    //   78: istore_1
    //   79: goto -23 -> 56
    //   82: aload_0
    //   83: aload_2
    //   84: invokeinterface 229 1 0
    //   89: aload_0
    //   90: getfield 69	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initDataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   93: getfield 170	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   96: lsub
    //   97: l2i
    //   98: putfield 241	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initSegmentBytesLoaded	I
    //   101: aload_0
    //   102: getfield 78	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSource	Lorg/telegram/messenger/exoplayer2/upstream/DataSource;
    //   105: invokestatic 235	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;)V
    //   108: aload_0
    //   109: iconst_1
    //   110: putfield 124	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initLoadCompleted	Z
    //   113: return
    //   114: astore_3
    //   115: aload_0
    //   116: aload_2
    //   117: invokeinterface 229 1 0
    //   122: aload_0
    //   123: getfield 69	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initDataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   126: getfield 170	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   129: lsub
    //   130: l2i
    //   131: putfield 241	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:initSegmentBytesLoaded	I
    //   134: aload_3
    //   135: athrow
    //   136: astore_2
    //   137: aload_0
    //   138: getfield 78	org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk:dataSource	Lorg/telegram/messenger/exoplayer2/upstream/DataSource;
    //   141: invokestatic 235	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;)V
    //   144: aload_2
    //   145: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	146	0	this	HlsMediaChunk
    //   55	24	1	i	int
    //   27	90	2	localObject1	Object
    //   136	9	2	localObject2	Object
    //   114	21	3	localObject3	Object
    // Exception table:
    //   from	to	target	type
    //   60	79	114	finally
    //   28	54	136	finally
    //   82	101	136	finally
    //   115	136	136	finally
  }
  
  private long peekId3PrivTimestamp(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    paramExtractorInput.resetPeekPosition();
    if (!paramExtractorInput.peekFully(this.id3Data.data, 0, 10, true)) {
      return -9223372036854775807L;
    }
    this.id3Data.reset(10);
    if (this.id3Data.readUnsignedInt24() != Id3Decoder.ID3_TAG) {
      return -9223372036854775807L;
    }
    this.id3Data.skipBytes(3);
    int i = this.id3Data.readSynchSafeInt();
    int j = i + 10;
    Object localObject;
    if (j > this.id3Data.capacity())
    {
      localObject = this.id3Data.data;
      this.id3Data.reset(j);
      System.arraycopy(localObject, 0, this.id3Data.data, 0, 10);
    }
    if (!paramExtractorInput.peekFully(this.id3Data.data, 10, i, true)) {
      return -9223372036854775807L;
    }
    paramExtractorInput = this.id3Decoder.decode(this.id3Data.data, i);
    if (paramExtractorInput == null) {
      return -9223372036854775807L;
    }
    j = paramExtractorInput.length();
    i = 0;
    while (i < j)
    {
      localObject = paramExtractorInput.get(i);
      if ((localObject instanceof PrivFrame))
      {
        localObject = (PrivFrame)localObject;
        if ("com.apple.streaming.transportStreamTimestamp".equals(((PrivFrame)localObject).owner))
        {
          System.arraycopy(((PrivFrame)localObject).privateData, 0, this.id3Data.data, 0, 8);
          this.id3Data.reset(8);
          return this.id3Data.readLong() & 0x1FFFFFFFF;
        }
      }
      i += 1;
    }
    return -9223372036854775807L;
  }
  
  public long bytesLoaded()
  {
    return this.bytesLoaded;
  }
  
  public void cancelLoad()
  {
    this.loadCanceled = true;
  }
  
  public void init(HlsSampleStreamWrapper paramHlsSampleStreamWrapper)
  {
    this.output = paramHlsSampleStreamWrapper;
    paramHlsSampleStreamWrapper.init(this.uid, this.shouldSpliceIn, this.reusingExtractor);
    if (!this.reusingExtractor) {
      this.extractor.init(paramHlsSampleStreamWrapper);
    }
  }
  
  public boolean isLoadCanceled()
  {
    return this.loadCanceled;
  }
  
  public boolean isLoadCompleted()
  {
    return this.loadCompleted;
  }
  
  public void load()
    throws IOException, InterruptedException
  {
    maybeLoadInitData();
    if (!this.loadCanceled) {
      loadMedia();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/HlsMediaChunk.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */