package org.telegram.messenger.exoplayer2.drm;

import android.annotation.TargetApi;
import android.net.Uri;
import android.text.TextUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.telegram.messenger.exoplayer2.C;
import org.telegram.messenger.exoplayer2.upstream.DataSourceInputStream;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.HttpDataSource;
import org.telegram.messenger.exoplayer2.upstream.HttpDataSource.Factory;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.Util;

@TargetApi(18)
public final class HttpMediaDrmCallback
  implements MediaDrmCallback
{
  private final HttpDataSource.Factory dataSourceFactory;
  private final String defaultLicenseUrl;
  private final boolean forceDefaultLicenseUrl;
  private final Map<String, String> keyRequestProperties;
  
  public HttpMediaDrmCallback(String paramString, HttpDataSource.Factory paramFactory)
  {
    this(paramString, false, paramFactory);
  }
  
  public HttpMediaDrmCallback(String paramString, boolean paramBoolean, HttpDataSource.Factory paramFactory)
  {
    this.dataSourceFactory = paramFactory;
    this.defaultLicenseUrl = paramString;
    this.forceDefaultLicenseUrl = paramBoolean;
    this.keyRequestProperties = new HashMap();
  }
  
  private static byte[] executePost(HttpDataSource.Factory paramFactory, String paramString, byte[] paramArrayOfByte, Map<String, String> paramMap)
    throws IOException
  {
    paramFactory = paramFactory.createDataSource();
    if (paramMap != null)
    {
      paramMap = paramMap.entrySet().iterator();
      while (paramMap.hasNext())
      {
        Map.Entry localEntry = (Map.Entry)paramMap.next();
        paramFactory.setRequestProperty((String)localEntry.getKey(), (String)localEntry.getValue());
      }
    }
    paramFactory = new DataSourceInputStream(paramFactory, new DataSpec(Uri.parse(paramString), paramArrayOfByte, 0L, 0L, -1L, null, 1));
    try
    {
      paramString = Util.toByteArray(paramFactory);
      return paramString;
    }
    finally
    {
      Util.closeQuietly(paramFactory);
    }
  }
  
  public void clearAllKeyRequestProperties()
  {
    synchronized (this.keyRequestProperties)
    {
      this.keyRequestProperties.clear();
      return;
    }
  }
  
  public void clearKeyRequestProperty(String paramString)
  {
    Assertions.checkNotNull(paramString);
    synchronized (this.keyRequestProperties)
    {
      this.keyRequestProperties.remove(paramString);
      return;
    }
  }
  
  public byte[] executeKeyRequest(UUID arg1, ExoMediaDrm.KeyRequest paramKeyRequest)
    throws Exception
  {
    String str1 = paramKeyRequest.getDefaultUrl();
    String str2;
    if (!this.forceDefaultLicenseUrl)
    {
      str2 = str1;
      if (!TextUtils.isEmpty(str1)) {}
    }
    else
    {
      str2 = this.defaultLicenseUrl;
    }
    HashMap localHashMap = new HashMap();
    if (C.PLAYREADY_UUID.equals(???)) {
      str1 = "text/xml";
    }
    for (;;)
    {
      localHashMap.put("Content-Type", str1);
      if (C.PLAYREADY_UUID.equals(???)) {
        localHashMap.put("SOAPAction", "http://schemas.microsoft.com/DRM/2007/03/protocols/AcquireLicense");
      }
      synchronized (this.keyRequestProperties)
      {
        localHashMap.putAll(this.keyRequestProperties);
        return executePost(this.dataSourceFactory, str2, paramKeyRequest.getData(), localHashMap);
        if (C.CLEARKEY_UUID.equals(???))
        {
          str1 = "application/json";
          continue;
        }
        str1 = "application/octet-stream";
      }
    }
  }
  
  public byte[] executeProvisionRequest(UUID paramUUID, ExoMediaDrm.ProvisionRequest paramProvisionRequest)
    throws IOException
  {
    paramUUID = paramProvisionRequest.getDefaultUrl() + "&signedRequest=" + new String(paramProvisionRequest.getData());
    return executePost(this.dataSourceFactory, paramUUID, new byte[0], null);
  }
  
  public void setKeyRequestProperty(String paramString1, String paramString2)
  {
    Assertions.checkNotNull(paramString1);
    Assertions.checkNotNull(paramString2);
    synchronized (this.keyRequestProperties)
    {
      this.keyRequestProperties.put(paramString1, paramString2);
      return;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/drm/HttpMediaDrmCallback.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */