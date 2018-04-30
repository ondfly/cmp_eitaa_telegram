package ir.eitaa.messenger.volley.toolbox;

import android.os.Handler;
import android.os.Looper;
import ir.eitaa.messenger.volley.Cache;
import ir.eitaa.messenger.volley.NetworkResponse;
import ir.eitaa.messenger.volley.Request;
import ir.eitaa.messenger.volley.Request.Priority;
import ir.eitaa.messenger.volley.Response;

public class ClearCacheRequest
  extends Request<Object>
{
  private final Cache mCache;
  private final Runnable mCallback;
  
  public ClearCacheRequest(Cache paramCache, Runnable paramRunnable)
  {
    super(0, null, null);
    this.mCache = paramCache;
    this.mCallback = paramRunnable;
  }
  
  protected void deliverResponse(Object paramObject) {}
  
  public Request.Priority getPriority()
  {
    return Request.Priority.IMMEDIATE;
  }
  
  public boolean isCanceled()
  {
    this.mCache.clear();
    if (this.mCallback != null) {
      new Handler(Looper.getMainLooper()).postAtFrontOfQueue(this.mCallback);
    }
    return true;
  }
  
  protected Response<Object> parseNetworkResponse(NetworkResponse paramNetworkResponse)
  {
    return null;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/volley/toolbox/ClearCacheRequest.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */