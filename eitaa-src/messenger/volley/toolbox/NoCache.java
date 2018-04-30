package ir.eitaa.messenger.volley.toolbox;

import ir.eitaa.messenger.volley.Cache;
import ir.eitaa.messenger.volley.Cache.Entry;

public class NoCache
  implements Cache
{
  public void clear() {}
  
  public Cache.Entry get(String paramString)
  {
    return null;
  }
  
  public void initialize() {}
  
  public void invalidate(String paramString, boolean paramBoolean) {}
  
  public void put(String paramString, Cache.Entry paramEntry) {}
  
  public void remove(String paramString) {}
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/toolbox/NoCache.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */