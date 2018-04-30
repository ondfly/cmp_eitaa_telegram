package ir.eitaa.messenger.exoplayer.upstream.cache;

public abstract interface CacheEvictor
  extends Cache.Listener
{
  public abstract void onCacheInitialized();
  
  public abstract void onStartFile(Cache paramCache, String paramString, long paramLong1, long paramLong2);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/upstream/cache/CacheEvictor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */