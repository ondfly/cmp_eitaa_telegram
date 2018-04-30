package org.telegram.messenger.exoplayer2.offline;

import android.net.Uri;
import java.io.IOException;
import java.util.List;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.cache.Cache;
import org.telegram.messenger.exoplayer2.upstream.cache.CacheDataSource;
import org.telegram.messenger.exoplayer2.upstream.cache.CacheUtil;
import org.telegram.messenger.exoplayer2.upstream.cache.CacheUtil.CachingCounters;
import org.telegram.messenger.exoplayer2.util.PriorityTaskManager;

public abstract class SegmentDownloader<M, K>
  implements Downloader
{
  private static final int BUFFER_SIZE_BYTES = 131072;
  private final Cache cache;
  private final CacheDataSource dataSource;
  private volatile long downloadedBytes;
  private volatile int downloadedSegments;
  private K[] keys;
  private M manifest;
  private final Uri manifestUri;
  private final CacheDataSource offlineDataSource;
  private final PriorityTaskManager priorityTaskManager;
  private volatile int totalSegments;
  
  public SegmentDownloader(Uri paramUri, DownloaderConstructorHelper paramDownloaderConstructorHelper)
  {
    this.manifestUri = paramUri;
    this.cache = paramDownloaderConstructorHelper.getCache();
    this.dataSource = paramDownloaderConstructorHelper.buildCacheDataSource(false);
    this.offlineDataSource = paramDownloaderConstructorHelper.buildCacheDataSource(true);
    this.priorityTaskManager = paramDownloaderConstructorHelper.getPriorityTaskManager();
    resetCounters();
  }
  
  private DataSource getDataSource(boolean paramBoolean)
  {
    if (paramBoolean) {
      return this.offlineDataSource;
    }
    return this.dataSource;
  }
  
  private M getManifestIfNeeded(boolean paramBoolean)
    throws IOException
  {
    if (this.manifest == null) {
      this.manifest = getManifest(getDataSource(paramBoolean), this.manifestUri);
    }
    return (M)this.manifest;
  }
  
  private List<Segment> initStatus(boolean paramBoolean)
    throws IOException, InterruptedException
  {
    for (;;)
    {
      int i;
      try
      {
        Object localObject1 = getDataSource(paramBoolean);
        if ((this.keys != null) && (this.keys.length > 0))
        {
          localObject1 = getSegments((DataSource)localObject1, this.manifest, this.keys, paramBoolean);
          CacheUtil.CachingCounters localCachingCounters = new CacheUtil.CachingCounters();
          this.totalSegments = ((List)localObject1).size();
          this.downloadedSegments = 0;
          this.downloadedBytes = 0L;
          i = ((List)localObject1).size() - 1;
          if (i >= 0)
          {
            CacheUtil.getCached(((Segment)((List)localObject1).get(i)).dataSpec, this.cache, localCachingCounters);
            this.downloadedBytes += localCachingCounters.alreadyCachedBytes;
            if (localCachingCounters.alreadyCachedBytes != localCachingCounters.contentLength) {
              break label174;
            }
            this.downloadedSegments += 1;
            ((List)localObject1).remove(i);
            break label174;
          }
        }
        else
        {
          localObject1 = getAllSegments((DataSource)localObject1, this.manifest, paramBoolean);
          continue;
        }
        return (List<Segment>)localObject1;
      }
      finally {}
      label174:
      i -= 1;
    }
  }
  
  private void notifyListener(Downloader.ProgressListener paramProgressListener)
  {
    if (paramProgressListener != null) {
      paramProgressListener.onDownloadProgress(this, getDownloadPercentage(), this.downloadedBytes);
    }
  }
  
  private void remove(Uri paramUri)
  {
    CacheUtil.remove(this.cache, CacheUtil.generateKey(paramUri));
  }
  
  private void resetCounters()
  {
    this.totalSegments = -1;
    this.downloadedSegments = -1;
    this.downloadedBytes = -1L;
  }
  
  /* Error */
  public final void download(Downloader.ProgressListener paramProgressListener)
    throws IOException, InterruptedException
  {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield 61	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:priorityTaskManager	Lorg/telegram/messenger/exoplayer2/util/PriorityTaskManager;
    //   6: sipush 64536
    //   9: invokevirtual 164	org/telegram/messenger/exoplayer2/util/PriorityTaskManager:add	(I)V
    //   12: aload_0
    //   13: iconst_0
    //   14: invokespecial 166	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:getManifestIfNeeded	(Z)Ljava/lang/Object;
    //   17: pop
    //   18: aload_0
    //   19: iconst_0
    //   20: invokespecial 168	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:initStatus	(Z)Ljava/util/List;
    //   23: astore_3
    //   24: aload_0
    //   25: aload_1
    //   26: invokespecial 170	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:notifyListener	(Lorg/telegram/messenger/exoplayer2/offline/Downloader$ProgressListener;)V
    //   29: aload_3
    //   30: invokestatic 176	java/util/Collections:sort	(Ljava/util/List;)V
    //   33: ldc 13
    //   35: newarray <illegal type>
    //   37: astore 4
    //   39: new 94	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters
    //   42: dup
    //   43: invokespecial 95	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters:<init>	()V
    //   46: astore 5
    //   48: iconst_0
    //   49: istore_2
    //   50: iload_2
    //   51: aload_3
    //   52: invokeinterface 101 1 0
    //   57: if_icmpge +75 -> 132
    //   60: aload_3
    //   61: iload_2
    //   62: invokeinterface 111 2 0
    //   67: checkcast 9	org/telegram/messenger/exoplayer2/offline/SegmentDownloader$Segment
    //   70: getfield 115	org/telegram/messenger/exoplayer2/offline/SegmentDownloader$Segment:dataSpec	Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;
    //   73: aload_0
    //   74: getfield 47	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:cache	Lorg/telegram/messenger/exoplayer2/upstream/cache/Cache;
    //   77: aload_0
    //   78: getfield 53	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:dataSource	Lorg/telegram/messenger/exoplayer2/upstream/cache/CacheDataSource;
    //   81: aload 4
    //   83: aload_0
    //   84: getfield 61	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:priorityTaskManager	Lorg/telegram/messenger/exoplayer2/util/PriorityTaskManager;
    //   87: sipush 64536
    //   90: aload 5
    //   92: iconst_1
    //   93: invokestatic 179	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil:cache	(Lorg/telegram/messenger/exoplayer2/upstream/DataSpec;Lorg/telegram/messenger/exoplayer2/upstream/cache/Cache;Lorg/telegram/messenger/exoplayer2/upstream/cache/CacheDataSource;[BLorg/telegram/messenger/exoplayer2/util/PriorityTaskManager;ILorg/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters;Z)V
    //   96: aload_0
    //   97: aload_0
    //   98: getfield 107	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:downloadedBytes	J
    //   101: aload 5
    //   103: getfield 182	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters:newlyCachedBytes	J
    //   106: ladd
    //   107: putfield 107	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:downloadedBytes	J
    //   110: aload_0
    //   111: aload_0
    //   112: getfield 105	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:downloadedSegments	I
    //   115: iconst_1
    //   116: iadd
    //   117: putfield 105	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:downloadedSegments	I
    //   120: aload_0
    //   121: aload_1
    //   122: invokespecial 170	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:notifyListener	(Lorg/telegram/messenger/exoplayer2/offline/Downloader$ProgressListener;)V
    //   125: iload_2
    //   126: iconst_1
    //   127: iadd
    //   128: istore_2
    //   129: goto -79 -> 50
    //   132: aload_0
    //   133: getfield 61	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:priorityTaskManager	Lorg/telegram/messenger/exoplayer2/util/PriorityTaskManager;
    //   136: sipush 64536
    //   139: invokevirtual 184	org/telegram/messenger/exoplayer2/util/PriorityTaskManager:remove	(I)V
    //   142: aload_0
    //   143: monitorexit
    //   144: return
    //   145: astore_1
    //   146: aload_0
    //   147: getfield 61	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:priorityTaskManager	Lorg/telegram/messenger/exoplayer2/util/PriorityTaskManager;
    //   150: sipush 64536
    //   153: invokevirtual 184	org/telegram/messenger/exoplayer2/util/PriorityTaskManager:remove	(I)V
    //   156: aload_1
    //   157: athrow
    //   158: astore_1
    //   159: aload_0
    //   160: monitorexit
    //   161: aload_1
    //   162: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	163	0	this	SegmentDownloader
    //   0	163	1	paramProgressListener	Downloader.ProgressListener
    //   49	80	2	i	int
    //   23	38	3	localList	List
    //   37	45	4	arrayOfByte	byte[]
    //   46	56	5	localCachingCounters	CacheUtil.CachingCounters
    // Exception table:
    //   from	to	target	type
    //   12	48	145	finally
    //   50	125	145	finally
    //   2	12	158	finally
    //   132	142	158	finally
    //   146	158	158	finally
  }
  
  protected abstract List<Segment> getAllSegments(DataSource paramDataSource, M paramM, boolean paramBoolean)
    throws InterruptedException, IOException;
  
  public float getDownloadPercentage()
  {
    float f = 100.0F;
    int i = this.totalSegments;
    int j = this.downloadedSegments;
    if ((i == -1) || (j == -1)) {
      f = NaN.0F;
    }
    while (i == 0) {
      return f;
    }
    return 100.0F * j / i;
  }
  
  public final long getDownloadedBytes()
  {
    return this.downloadedBytes;
  }
  
  public final int getDownloadedSegments()
  {
    return this.downloadedSegments;
  }
  
  public final M getManifest()
    throws IOException
  {
    return (M)getManifestIfNeeded(false);
  }
  
  protected abstract M getManifest(DataSource paramDataSource, Uri paramUri)
    throws IOException;
  
  protected abstract List<Segment> getSegments(DataSource paramDataSource, M paramM, K[] paramArrayOfK, boolean paramBoolean)
    throws InterruptedException, IOException;
  
  public final int getTotalSegments()
  {
    return this.totalSegments;
  }
  
  /* Error */
  public final void init()
    throws InterruptedException, IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: iconst_1
    //   2: invokespecial 166	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:getManifestIfNeeded	(Z)Ljava/lang/Object;
    //   5: pop
    //   6: aload_0
    //   7: iconst_1
    //   8: invokespecial 168	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:initStatus	(Z)Ljava/util/List;
    //   11: pop
    //   12: return
    //   13: astore_1
    //   14: return
    //   15: astore_1
    //   16: aload_0
    //   17: invokespecial 64	org/telegram/messenger/exoplayer2/offline/SegmentDownloader:resetCounters	()V
    //   20: aload_1
    //   21: athrow
    //   22: astore_1
    //   23: goto -7 -> 16
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	26	0	this	SegmentDownloader
    //   13	1	1	localIOException1	IOException
    //   15	6	1	localIOException2	IOException
    //   22	1	1	localInterruptedException	InterruptedException
    // Exception table:
    //   from	to	target	type
    //   0	6	13	java/io/IOException
    //   6	12	15	java/io/IOException
    //   6	12	22	java/lang/InterruptedException
  }
  
  public final void remove()
    throws InterruptedException
  {
    try
    {
      getManifestIfNeeded(true);
      resetCounters();
      Object localObject;
      if (this.manifest != null) {
        localObject = null;
      }
      try
      {
        List localList = getAllSegments(this.offlineDataSource, this.manifest, true);
        localObject = localList;
      }
      catch (IOException localIOException2)
      {
        int i;
        for (;;) {}
      }
      if (localObject != null)
      {
        i = 0;
        while (i < ((List)localObject).size())
        {
          remove(((Segment)((List)localObject).get(i)).dataSpec.uri);
          i += 1;
        }
      }
      this.manifest = null;
      remove(this.manifestUri);
      return;
    }
    catch (IOException localIOException1)
    {
      for (;;) {}
    }
  }
  
  public final void selectRepresentations(K[] paramArrayOfK)
  {
    if (paramArrayOfK != null) {}
    for (paramArrayOfK = (Object[])paramArrayOfK.clone();; paramArrayOfK = null)
    {
      this.keys = paramArrayOfK;
      resetCounters();
      return;
    }
  }
  
  protected static class Segment
    implements Comparable<Segment>
  {
    public final DataSpec dataSpec;
    public final long startTimeUs;
    
    public Segment(long paramLong, DataSpec paramDataSpec)
    {
      this.startTimeUs = paramLong;
      this.dataSpec = paramDataSpec;
    }
    
    public int compareTo(Segment paramSegment)
    {
      long l = this.startTimeUs - paramSegment.startTimeUs;
      if (l == 0L) {
        return 0;
      }
      if (l < 0L) {
        return -1;
      }
      return 1;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/offline/SegmentDownloader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */