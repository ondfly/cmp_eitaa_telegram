package org.telegram.messenger.exoplayer2.upstream.cache;

import android.net.Uri;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.telegram.messenger.exoplayer2.upstream.DataSink;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSourceException;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.FileDataSource;
import org.telegram.messenger.exoplayer2.upstream.TeeDataSource;
import org.telegram.messenger.exoplayer2.util.Assertions;

public final class CacheDataSource
  implements DataSource
{
  public static final long DEFAULT_MAX_CACHE_FILE_SIZE = 2097152L;
  public static final int FLAG_BLOCK_ON_CACHE = 1;
  public static final int FLAG_IGNORE_CACHE_FOR_UNSET_LENGTH_REQUESTS = 4;
  public static final int FLAG_IGNORE_CACHE_ON_ERROR = 2;
  private static final long MIN_READ_BEFORE_CHECKING_CACHE = 102400L;
  private final boolean blockOnCache;
  private long bytesRemaining;
  private final Cache cache;
  private final DataSource cacheReadDataSource;
  private final DataSource cacheWriteDataSource;
  private long checkCachePosition;
  private DataSource currentDataSource;
  private boolean currentDataSpecLengthUnset;
  private CacheSpan currentHoleSpan;
  private boolean currentRequestIgnoresCache;
  private final EventListener eventListener;
  private int flags;
  private final boolean ignoreCacheForUnsetLengthRequests;
  private final boolean ignoreCacheOnError;
  private String key;
  private long readPosition;
  private boolean seenCacheError;
  private long totalCachedBytesRead;
  private final DataSource upstreamDataSource;
  private Uri uri;
  
  public CacheDataSource(Cache paramCache, DataSource paramDataSource)
  {
    this(paramCache, paramDataSource, 0, 2097152L);
  }
  
  public CacheDataSource(Cache paramCache, DataSource paramDataSource, int paramInt)
  {
    this(paramCache, paramDataSource, paramInt, 2097152L);
  }
  
  public CacheDataSource(Cache paramCache, DataSource paramDataSource, int paramInt, long paramLong)
  {
    this(paramCache, paramDataSource, new FileDataSource(), new CacheDataSink(paramCache, paramLong), paramInt, null);
  }
  
  public CacheDataSource(Cache paramCache, DataSource paramDataSource1, DataSource paramDataSource2, DataSink paramDataSink, int paramInt, EventListener paramEventListener)
  {
    this.cache = paramCache;
    this.cacheReadDataSource = paramDataSource2;
    boolean bool1;
    if ((paramInt & 0x1) != 0)
    {
      bool1 = true;
      this.blockOnCache = bool1;
      if ((paramInt & 0x2) == 0) {
        break label103;
      }
      bool1 = true;
      label43:
      this.ignoreCacheOnError = bool1;
      if ((paramInt & 0x4) == 0) {
        break label109;
      }
      bool1 = bool2;
      label60:
      this.ignoreCacheForUnsetLengthRequests = bool1;
      this.upstreamDataSource = paramDataSource1;
      if (paramDataSink == null) {
        break label115;
      }
    }
    label103:
    label109:
    label115:
    for (this.cacheWriteDataSource = new TeeDataSource(paramDataSource1, paramDataSink);; this.cacheWriteDataSource = null)
    {
      this.eventListener = paramEventListener;
      return;
      bool1 = false;
      break;
      bool1 = false;
      break label43;
      bool1 = false;
      break label60;
    }
  }
  
  private void closeCurrentSource()
    throws IOException
  {
    if (this.currentDataSource == null) {}
    for (;;)
    {
      return;
      try
      {
        this.currentDataSource.close();
        return;
      }
      finally
      {
        this.currentDataSource = null;
        this.currentDataSpecLengthUnset = false;
        if (this.currentHoleSpan != null)
        {
          this.cache.releaseHoleSpan(this.currentHoleSpan);
          this.currentHoleSpan = null;
        }
      }
    }
  }
  
  private void handleBeforeThrow(IOException paramIOException)
  {
    if ((this.currentDataSource == this.cacheReadDataSource) || ((paramIOException instanceof Cache.CacheException))) {
      this.seenCacheError = true;
    }
  }
  
  private static boolean isCausedByPositionOutOfRange(IOException paramIOException)
  {
    while (paramIOException != null)
    {
      if (((paramIOException instanceof DataSourceException)) && (((DataSourceException)paramIOException).reason == 0)) {
        return true;
      }
      paramIOException = paramIOException.getCause();
    }
    return false;
  }
  
  private boolean isWritingToCache()
  {
    return this.currentDataSource == this.cacheWriteDataSource;
  }
  
  private void notifyBytesRead()
  {
    if ((this.eventListener != null) && (this.totalCachedBytesRead > 0L))
    {
      this.eventListener.onCachedBytesRead(this.cache.getCacheSpace(), this.totalCachedBytesRead);
      this.totalCachedBytesRead = 0L;
    }
  }
  
  private void openNextSource(boolean paramBoolean)
    throws IOException
  {
    CacheSpan localCacheSpan1;
    Object localObject;
    DataSpec localDataSpec;
    label50:
    long l1;
    if (this.currentRequestIgnoresCache)
    {
      localCacheSpan1 = null;
      if (localCacheSpan1 != null) {
        break label172;
      }
      localObject = this.upstreamDataSource;
      localDataSpec = new DataSpec(this.uri, this.readPosition, this.bytesRemaining, this.key, this.flags);
      if ((this.currentRequestIgnoresCache) || (localObject != this.upstreamDataSource)) {
        break label383;
      }
      l1 = this.readPosition + 102400L;
      label75:
      this.checkCachePosition = l1;
      if (!paramBoolean) {
        break label399;
      }
      if (this.currentDataSource != this.upstreamDataSource) {
        break label390;
      }
    }
    CacheSpan localCacheSpan2;
    label172:
    label360:
    label383:
    label390:
    for (paramBoolean = true;; paramBoolean = false)
    {
      Assertions.checkState(paramBoolean);
      if (localObject != this.upstreamDataSource) {
        break label395;
      }
      return;
      if (this.blockOnCache) {
        try
        {
          localCacheSpan1 = this.cache.startReadWrite(this.key, this.readPosition);
        }
        catch (InterruptedException localInterruptedException)
        {
          throw new InterruptedIOException();
        }
      }
      localCacheSpan2 = this.cache.startReadWriteNonBlocking(this.key, this.readPosition);
      break;
      long l2;
      if (localCacheSpan2.isCached)
      {
        localObject = Uri.fromFile(localCacheSpan2.file);
        long l3 = this.readPosition - localCacheSpan2.position;
        l2 = localCacheSpan2.length - l3;
        l1 = l2;
        if (this.bytesRemaining != -1L) {
          l1 = Math.min(l2, this.bytesRemaining);
        }
        localDataSpec = new DataSpec((Uri)localObject, this.readPosition, l3, l1, this.key, this.flags);
        localObject = this.cacheReadDataSource;
        break label50;
      }
      if (localCacheSpan2.isOpenEnded()) {
        l1 = this.bytesRemaining;
      }
      for (;;)
      {
        localDataSpec = new DataSpec(this.uri, this.readPosition, l1, this.key, this.flags);
        if (this.cacheWriteDataSource == null) {
          break label360;
        }
        localObject = this.cacheWriteDataSource;
        break;
        l2 = localCacheSpan2.length;
        l1 = l2;
        if (this.bytesRemaining != -1L) {
          l1 = Math.min(l2, this.bytesRemaining);
        }
      }
      localObject = this.upstreamDataSource;
      this.cache.releaseHoleSpan(localCacheSpan2);
      localCacheSpan2 = null;
      break label50;
      l1 = Long.MAX_VALUE;
      break label75;
    }
    for (;;)
    {
      try
      {
        label395:
        closeCurrentSource();
        label399:
        if ((localCacheSpan2 != null) && (localCacheSpan2.isHoleSpan())) {
          this.currentHoleSpan = localCacheSpan2;
        }
        this.currentDataSource = ((DataSource)localObject);
        if (localDataSpec.length == -1L)
        {
          paramBoolean = true;
          this.currentDataSpecLengthUnset = paramBoolean;
          l1 = ((DataSource)localObject).open(localDataSpec);
          if ((!this.currentDataSpecLengthUnset) || (l1 == -1L)) {
            break;
          }
          setBytesRemaining(l1);
          return;
        }
      }
      catch (Throwable localThrowable)
      {
        if (localCacheSpan2.isHoleSpan()) {
          this.cache.releaseHoleSpan(localCacheSpan2);
        }
        throw localThrowable;
      }
      paramBoolean = false;
    }
  }
  
  private void setBytesRemaining(long paramLong)
    throws IOException
  {
    this.bytesRemaining = paramLong;
    if (isWritingToCache()) {
      this.cache.setContentLength(this.key, this.readPosition + paramLong);
    }
  }
  
  public void close()
    throws IOException
  {
    this.uri = null;
    notifyBytesRead();
    try
    {
      closeCurrentSource();
      return;
    }
    catch (IOException localIOException)
    {
      handleBeforeThrow(localIOException);
      throw localIOException;
    }
  }
  
  public Uri getUri()
  {
    if (this.currentDataSource == this.upstreamDataSource) {
      return this.currentDataSource.getUri();
    }
    return this.uri;
  }
  
  public long open(DataSpec paramDataSpec)
    throws IOException
  {
    boolean bool2 = false;
    for (;;)
    {
      try
      {
        this.uri = paramDataSpec.uri;
        this.flags = paramDataSpec.flags;
        this.key = CacheUtil.getKey(paramDataSpec);
        this.readPosition = paramDataSpec.position;
        if ((!this.ignoreCacheOnError) || (!this.seenCacheError))
        {
          bool1 = bool2;
          if (paramDataSpec.length == -1L)
          {
            bool1 = bool2;
            if (this.ignoreCacheForUnsetLengthRequests) {}
          }
          else
          {
            this.currentRequestIgnoresCache = bool1;
            if ((paramDataSpec.length != -1L) || (this.currentRequestIgnoresCache))
            {
              this.bytesRemaining = paramDataSpec.length;
              openNextSource(false);
              return this.bytesRemaining;
            }
            this.bytesRemaining = this.cache.getContentLength(this.key);
            if (this.bytesRemaining == -1L) {
              continue;
            }
            this.bytesRemaining -= paramDataSpec.position;
            if (this.bytesRemaining > 0L) {
              continue;
            }
            throw new DataSourceException(0);
          }
        }
      }
      catch (IOException paramDataSpec)
      {
        handleBeforeThrow(paramDataSpec);
        throw paramDataSpec;
      }
      boolean bool1 = true;
    }
  }
  
  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    int i = 0;
    if (paramInt2 == 0) {}
    for (;;)
    {
      return i;
      if (this.bytesRemaining == 0L) {
        return -1;
      }
      try
      {
        if (this.readPosition >= this.checkCachePosition) {
          openNextSource(true);
        }
        j = this.currentDataSource.read(paramArrayOfByte, paramInt1, paramInt2);
        if (j != -1)
        {
          if (this.currentDataSource == this.cacheReadDataSource) {
            this.totalCachedBytesRead += j;
          }
          this.readPosition += j;
          i = j;
          if (this.bytesRemaining == -1L) {
            continue;
          }
          this.bytesRemaining -= j;
          return j;
        }
      }
      catch (IOException paramArrayOfByte)
      {
        int j;
        if ((this.currentDataSpecLengthUnset) && (isCausedByPositionOutOfRange(paramArrayOfByte)))
        {
          setBytesRemaining(0L);
          return -1;
          if (this.currentDataSpecLengthUnset)
          {
            setBytesRemaining(0L);
            return j;
          }
          if (this.bytesRemaining <= 0L)
          {
            i = j;
            if (this.bytesRemaining != -1L) {
              continue;
            }
          }
          closeCurrentSource();
          openNextSource(false);
          paramInt1 = read(paramArrayOfByte, paramInt1, paramInt2);
          return paramInt1;
        }
        handleBeforeThrow(paramArrayOfByte);
        throw paramArrayOfByte;
      }
    }
  }
  
  public static abstract interface EventListener
  {
    public abstract void onCachedBytesRead(long paramLong1, long paramLong2);
  }
  
  @Retention(RetentionPolicy.SOURCE)
  public static @interface Flags {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/cache/CacheDataSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */