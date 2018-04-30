package org.telegram.messenger.exoplayer2.upstream.cache;

import android.net.Uri;
import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NavigableSet;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.PriorityTaskManager;

public final class CacheUtil
{
  public static final int DEFAULT_BUFFER_SIZE_BYTES = 131072;
  
  public static void cache(DataSpec paramDataSpec, Cache paramCache, DataSource paramDataSource, CachingCounters paramCachingCounters)
    throws IOException, InterruptedException
  {
    cache(paramDataSpec, paramCache, new CacheDataSource(paramCache, paramDataSource), new byte[131072], null, 0, paramCachingCounters, false);
  }
  
  public static void cache(DataSpec paramDataSpec, Cache paramCache, CacheDataSource paramCacheDataSource, byte[] paramArrayOfByte, PriorityTaskManager paramPriorityTaskManager, int paramInt, CachingCounters paramCachingCounters, boolean paramBoolean)
    throws IOException, InterruptedException
  {
    Assertions.checkNotNull(paramCacheDataSource);
    Assertions.checkNotNull(paramArrayOfByte);
    String str;
    long l3;
    long l1;
    label51:
    long l2;
    if (paramCachingCounters != null)
    {
      getCached(paramDataSpec, paramCache, paramCachingCounters);
      str = getKey(paramDataSpec);
      l3 = paramDataSpec.absoluteStreamPosition;
      if (paramDataSpec.length == -1L) {
        break label133;
      }
      l1 = paramDataSpec.length;
      if (l1 == 0L) {
        return;
      }
      if (l1 == -1L) {
        break label146;
      }
      l2 = l1;
      label71:
      l2 = paramCache.getCachedLength(str, l3, l2);
      if (l2 <= 0L) {
        break label154;
      }
    }
    label133:
    label146:
    label154:
    long l4;
    do
    {
      l3 += l2;
      if (l1 == -1L) {
        l2 = 0L;
      }
      l1 -= l2;
      break label51;
      paramCachingCounters = new CachingCounters();
      break;
      l1 = paramCache.getContentLength(str);
      break label51;
      l2 = Long.MAX_VALUE;
      break label71;
      l4 = -l2;
      l2 = l4;
    } while (readAndDiscard(paramDataSpec, l3, l4, paramCacheDataSource, paramArrayOfByte, paramPriorityTaskManager, paramInt, paramCachingCounters) >= l4);
    if ((paramBoolean) && (l1 != -1L)) {
      throw new EOFException();
    }
  }
  
  public static String generateKey(Uri paramUri)
  {
    return paramUri.toString();
  }
  
  public static void getCached(DataSpec paramDataSpec, Cache paramCache, CachingCounters paramCachingCounters)
  {
    String str = getKey(paramDataSpec);
    long l3 = paramDataSpec.absoluteStreamPosition;
    long l1;
    label43:
    long l2;
    if (paramDataSpec.length != -1L)
    {
      l1 = paramDataSpec.length;
      paramCachingCounters.contentLength = l1;
      paramCachingCounters.alreadyCachedBytes = 0L;
      paramCachingCounters.newlyCachedBytes = 0L;
      if (l1 == 0L) {
        return;
      }
      if (l1 == -1L) {
        break label130;
      }
      l2 = l1;
      label60:
      l2 = paramCache.getCachedLength(str, l3, l2);
      if (l2 <= 0L) {
        break label138;
      }
      paramCachingCounters.alreadyCachedBytes += l2;
    }
    label130:
    label138:
    long l4;
    do
    {
      l3 += l2;
      if (l1 == -1L) {
        l2 = 0L;
      }
      l1 -= l2;
      break label43;
      l1 = paramCache.getContentLength(str);
      break;
      l2 = Long.MAX_VALUE;
      break label60;
      l4 = -l2;
      l2 = l4;
    } while (l4 != Long.MAX_VALUE);
  }
  
  public static String getKey(DataSpec paramDataSpec)
  {
    if (paramDataSpec.key != null) {
      return paramDataSpec.key;
    }
    return generateKey(paramDataSpec.uri);
  }
  
  /* Error */
  private static long readAndDiscard(DataSpec paramDataSpec, long paramLong1, long paramLong2, DataSource paramDataSource, byte[] paramArrayOfByte, PriorityTaskManager paramPriorityTaskManager, int paramInt, CachingCounters paramCachingCounters)
    throws IOException, InterruptedException
  {
    // Byte code:
    //   0: aload 7
    //   2: ifnull +10 -> 12
    //   5: aload 7
    //   7: iload 8
    //   9: invokevirtual 110	org/telegram/messenger/exoplayer2/util/PriorityTaskManager:proceed	(I)V
    //   12: invokestatic 116	java/lang/Thread:interrupted	()Z
    //   15: ifeq +21 -> 36
    //   18: new 21	java/lang/InterruptedException
    //   21: dup
    //   22: invokespecial 117	java/lang/InterruptedException:<init>	()V
    //   25: athrow
    //   26: astore 13
    //   28: aload 5
    //   30: invokestatic 123	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;)V
    //   33: goto -33 -> 0
    //   36: new 46	org/telegram/messenger/exoplayer2/upstream/DataSpec
    //   39: dup
    //   40: aload_0
    //   41: getfield 100	org/telegram/messenger/exoplayer2/upstream/DataSpec:uri	Landroid/net/Uri;
    //   44: aload_0
    //   45: getfield 127	org/telegram/messenger/exoplayer2/upstream/DataSpec:postBody	[B
    //   48: lload_1
    //   49: aload_0
    //   50: getfield 130	org/telegram/messenger/exoplayer2/upstream/DataSpec:position	J
    //   53: lload_1
    //   54: ladd
    //   55: aload_0
    //   56: getfield 50	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   59: lsub
    //   60: ldc2_w 54
    //   63: aload_0
    //   64: getfield 96	org/telegram/messenger/exoplayer2/upstream/DataSpec:key	Ljava/lang/String;
    //   67: aload_0
    //   68: getfield 133	org/telegram/messenger/exoplayer2/upstream/DataSpec:flags	I
    //   71: iconst_2
    //   72: ior
    //   73: invokespecial 136	org/telegram/messenger/exoplayer2/upstream/DataSpec:<init>	(Landroid/net/Uri;[BJJJLjava/lang/String;I)V
    //   76: astore 13
    //   78: aload 5
    //   80: aload 13
    //   82: invokeinterface 142 2 0
    //   87: lstore 11
    //   89: aload 9
    //   91: getfield 86	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters:contentLength	J
    //   94: ldc2_w 54
    //   97: lcmp
    //   98: ifne +169 -> 267
    //   101: lload 11
    //   103: ldc2_w 54
    //   106: lcmp
    //   107: ifeq +160 -> 267
    //   110: aload 9
    //   112: aload 13
    //   114: getfield 50	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   117: lload 11
    //   119: ladd
    //   120: putfield 86	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters:contentLength	J
    //   123: goto +144 -> 267
    //   126: lload 11
    //   128: lload_3
    //   129: lcmp
    //   130: ifeq +84 -> 214
    //   133: invokestatic 116	java/lang/Thread:interrupted	()Z
    //   136: ifeq +11 -> 147
    //   139: new 21	java/lang/InterruptedException
    //   142: dup
    //   143: invokespecial 117	java/lang/InterruptedException:<init>	()V
    //   146: athrow
    //   147: lload_3
    //   148: ldc2_w 54
    //   151: lcmp
    //   152: ifeq +70 -> 222
    //   155: aload 6
    //   157: arraylength
    //   158: i2l
    //   159: lload_3
    //   160: lload 11
    //   162: lsub
    //   163: invokestatic 148	java/lang/Math:min	(JJ)J
    //   166: l2i
    //   167: istore 10
    //   169: aload 5
    //   171: aload 6
    //   173: iconst_0
    //   174: iload 10
    //   176: invokeinterface 152 4 0
    //   181: istore 10
    //   183: iload 10
    //   185: iconst_m1
    //   186: if_icmpne +44 -> 230
    //   189: aload 9
    //   191: getfield 86	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters:contentLength	J
    //   194: ldc2_w 54
    //   197: lcmp
    //   198: ifne +16 -> 214
    //   201: aload 9
    //   203: aload 13
    //   205: getfield 50	org/telegram/messenger/exoplayer2/upstream/DataSpec:absoluteStreamPosition	J
    //   208: lload 11
    //   210: ladd
    //   211: putfield 86	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters:contentLength	J
    //   214: aload 5
    //   216: invokestatic 123	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;)V
    //   219: lload 11
    //   221: lreturn
    //   222: aload 6
    //   224: arraylength
    //   225: istore 10
    //   227: goto -58 -> 169
    //   230: lload 11
    //   232: iload 10
    //   234: i2l
    //   235: ladd
    //   236: lstore 11
    //   238: aload 9
    //   240: aload 9
    //   242: getfield 92	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters:newlyCachedBytes	J
    //   245: iload 10
    //   247: i2l
    //   248: ladd
    //   249: putfield 92	org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil$CachingCounters:newlyCachedBytes	J
    //   252: goto -126 -> 126
    //   255: astore_0
    //   256: aload 5
    //   258: invokestatic 123	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Lorg/telegram/messenger/exoplayer2/upstream/DataSource;)V
    //   261: aload_0
    //   262: athrow
    //   263: astore_0
    //   264: goto -8 -> 256
    //   267: lconst_0
    //   268: lstore 11
    //   270: goto -144 -> 126
    //   273: astore_0
    //   274: aload 13
    //   276: astore_0
    //   277: goto -249 -> 28
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	280	0	paramDataSpec	DataSpec
    //   0	280	1	paramLong1	long
    //   0	280	3	paramLong2	long
    //   0	280	5	paramDataSource	DataSource
    //   0	280	6	paramArrayOfByte	byte[]
    //   0	280	7	paramPriorityTaskManager	PriorityTaskManager
    //   0	280	8	paramInt	int
    //   0	280	9	paramCachingCounters	CachingCounters
    //   167	79	10	i	int
    //   87	182	11	l	long
    //   26	1	13	localPriorityTooLowException	org.telegram.messenger.exoplayer2.util.PriorityTaskManager.PriorityTooLowException
    //   76	199	13	localDataSpec	DataSpec
    // Exception table:
    //   from	to	target	type
    //   12	26	26	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   36	78	26	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   78	101	255	finally
    //   110	123	255	finally
    //   133	147	255	finally
    //   155	169	255	finally
    //   169	183	255	finally
    //   189	214	255	finally
    //   222	227	255	finally
    //   238	252	255	finally
    //   12	26	263	finally
    //   36	78	263	finally
    //   78	101	273	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   110	123	273	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   133	147	273	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   155	169	273	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   169	183	273	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   189	214	273	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   222	227	273	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
    //   238	252	273	org/telegram/messenger/exoplayer2/util/PriorityTaskManager$PriorityTooLowException
  }
  
  public static void remove(Cache paramCache, String paramString)
  {
    paramString = paramCache.getCachedSpans(paramString).iterator();
    while (paramString.hasNext())
    {
      CacheSpan localCacheSpan = (CacheSpan)paramString.next();
      try
      {
        paramCache.removeSpan(localCacheSpan);
      }
      catch (Cache.CacheException localCacheException) {}
    }
  }
  
  public static class CachingCounters
  {
    public volatile long alreadyCachedBytes;
    public volatile long contentLength = -1L;
    public volatile long newlyCachedBytes;
    
    public long totalCachedBytes()
    {
      return this.alreadyCachedBytes + this.newlyCachedBytes;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/cache/CacheUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */