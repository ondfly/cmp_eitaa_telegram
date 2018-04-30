package ir.eitaa.helper.http;

import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.exoplayer.util.Util;
import ir.eitaa.tgnet.NativeByteBuffer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public class HelperHttp
{
  public static int connectionTimeout = 60000;
  private final DefaultHttpClient httpClient;
  private final HttpPost httpPost;
  private HttpResponse httpResponse;
  
  public HelperHttp()
  {
    this(null, 0, "");
  }
  
  public HelperHttp(String paramString1, int paramInt, String paramString2)
  {
    BasicHttpParams localBasicHttpParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(localBasicHttpParams, connectionTimeout);
    HttpConnectionParams.setSoTimeout(localBasicHttpParams, connectionTimeout);
    HttpConnectionParams.setTcpNoDelay(localBasicHttpParams, true);
    this.httpClient = new DefaultHttpClient(localBasicHttpParams);
    this.httpPost = new HttpPost();
    if (paramString1 != null) {
      if (paramInt != 443) {
        break label106;
      }
    }
    label106:
    for (;;)
    {
      paramString1 = "https://" + paramString1 + paramString2;
      this.httpPost.setURI(URI.create(paramString1));
      return;
    }
  }
  
  public HelperHttp ConnectionTimeout(int paramInt)
  {
    connectionTimeout = paramInt;
    return this;
  }
  
  public HelperHttp URI(String paramString)
  {
    this.httpPost.setURI(URI.create(paramString));
    return this;
  }
  
  public void consumeContent()
  {
    try
    {
      this.httpResponse.getEntity().consumeContent();
      this.httpPost.abort();
      return;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }
  
  public NativeByteBuffer send(byte[] paramArrayOfByte)
  {
    paramArrayOfByte = new ByteArrayEntity(paramArrayOfByte);
    try
    {
      this.httpPost.setEntity(paramArrayOfByte);
      this.httpResponse = this.httpClient.execute(this.httpPost);
      if (this.httpResponse != null)
      {
        paramArrayOfByte = this.httpResponse.getEntity().getContent();
        if (paramArrayOfByte == null) {
          return null;
        }
        if ((this.httpResponse.getStatusLine().getStatusCode() == 403) || (this.httpResponse.getStatusLine().getStatusCode() == 404) || (this.httpResponse.getStatusLine().getStatusCode() == 500) || (this.httpResponse.getStatusLine().getStatusCode() == 501) || (this.httpResponse.getStatusLine().getStatusCode() == 502) || (this.httpResponse.getStatusLine().getStatusCode() == 503) || (this.httpResponse.getStatusLine().getStatusCode() == 504)) {
          break label285;
        }
        paramArrayOfByte = Util.toByteArray(paramArrayOfByte);
        NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(paramArrayOfByte.length);
        localNativeByteBuffer.writeBytes(paramArrayOfByte);
        localNativeByteBuffer.rewind();
        consumeContent();
        return localNativeByteBuffer;
      }
    }
    catch (UnsupportedEncodingException paramArrayOfByte)
    {
      paramArrayOfByte.printStackTrace();
      for (;;)
      {
        return null;
        FileLog.e("TSMS", "httpResponse is null");
      }
    }
    catch (ClientProtocolException paramArrayOfByte)
    {
      for (;;)
      {
        paramArrayOfByte.printStackTrace();
      }
    }
    catch (IOException paramArrayOfByte)
    {
      for (;;)
      {
        paramArrayOfByte.printStackTrace();
      }
    }
    catch (IllegalStateException paramArrayOfByte)
    {
      for (;;)
      {
        paramArrayOfByte.printStackTrace();
      }
    }
    catch (Exception paramArrayOfByte)
    {
      for (;;)
      {
        FileLog.e("TSMS", "De Serialized Data to ByteArray Error.");
      }
    }
    label285:
    return null;
  }
  
  /* Error */
  public String send(org.json.JSONObject paramJSONObject)
  {
    // Byte code:
    //   0: aload_1
    //   1: ifnonnull +5 -> 6
    //   4: aconst_null
    //   5: areturn
    //   6: new 175	org/apache/http/entity/StringEntity
    //   9: dup
    //   10: aload_1
    //   11: invokevirtual 178	org/json/JSONObject:toString	()Ljava/lang/String;
    //   14: ldc -76
    //   16: invokespecial 182	org/apache/http/entity/StringEntity:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   19: astore_1
    //   20: aload_1
    //   21: ldc -72
    //   23: invokevirtual 188	org/apache/http/entity/StringEntity:setContentType	(Ljava/lang/String;)V
    //   26: aload_0
    //   27: getfield 54	ir/eitaa/helper/http/HelperHttp:httpPost	Lorg/apache/http/client/methods/HttpPost;
    //   30: aload_1
    //   31: invokevirtual 121	org/apache/http/client/methods/HttpPost:setEntity	(Lorg/apache/http/HttpEntity;)V
    //   34: aload_0
    //   35: aload_0
    //   36: getfield 49	ir/eitaa/helper/http/HelperHttp:httpClient	Lorg/apache/http/impl/client/DefaultHttpClient;
    //   39: aload_0
    //   40: getfield 54	ir/eitaa/helper/http/HelperHttp:httpPost	Lorg/apache/http/client/methods/HttpPost;
    //   43: invokevirtual 125	org/apache/http/impl/client/DefaultHttpClient:execute	(Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/HttpResponse;
    //   46: putfield 86	ir/eitaa/helper/http/HelperHttp:httpResponse	Lorg/apache/http/HttpResponse;
    //   49: aload_0
    //   50: getfield 86	ir/eitaa/helper/http/HelperHttp:httpResponse	Lorg/apache/http/HttpResponse;
    //   53: ifnull +37 -> 90
    //   56: aload_0
    //   57: getfield 86	ir/eitaa/helper/http/HelperHttp:httpResponse	Lorg/apache/http/HttpResponse;
    //   60: invokeinterface 92 1 0
    //   65: invokeinterface 129 1 0
    //   70: astore_1
    //   71: aload_1
    //   72: ifnull +8 -> 80
    //   75: aload_1
    //   76: invokestatic 194	ir/eitaa/helper/http/HelperInputStream:convertInputStreamToString	(Ljava/io/InputStream;)Ljava/lang/String;
    //   79: areturn
    //   80: ldc -96
    //   82: ldc -60
    //   84: invokestatic 168	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   87: goto +57 -> 144
    //   90: ldc -96
    //   92: ldc -94
    //   94: invokestatic 168	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/String;)V
    //   97: goto +47 -> 144
    //   100: astore_1
    //   101: aload_1
    //   102: invokevirtual 158	java/io/UnsupportedEncodingException:printStackTrace	()V
    //   105: aconst_null
    //   106: areturn
    //   107: astore_1
    //   108: aload_1
    //   109: invokevirtual 169	org/apache/http/client/ClientProtocolException:printStackTrace	()V
    //   112: aconst_null
    //   113: areturn
    //   114: astore_1
    //   115: aload_1
    //   116: invokevirtual 102	java/io/IOException:printStackTrace	()V
    //   119: aconst_null
    //   120: areturn
    //   121: astore_1
    //   122: aload_1
    //   123: invokevirtual 170	java/lang/IllegalStateException:printStackTrace	()V
    //   126: aconst_null
    //   127: areturn
    //   128: astore_1
    //   129: goto -7 -> 122
    //   132: astore_1
    //   133: goto -18 -> 115
    //   136: astore_1
    //   137: goto -29 -> 108
    //   140: astore_1
    //   141: goto -40 -> 101
    //   144: aconst_null
    //   145: areturn
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	146	0	this	HelperHttp
    //   0	146	1	paramJSONObject	org.json.JSONObject
    // Exception table:
    //   from	to	target	type
    //   20	71	100	java/io/UnsupportedEncodingException
    //   75	80	100	java/io/UnsupportedEncodingException
    //   80	87	100	java/io/UnsupportedEncodingException
    //   90	97	100	java/io/UnsupportedEncodingException
    //   6	20	107	org/apache/http/client/ClientProtocolException
    //   6	20	114	java/io/IOException
    //   6	20	121	java/lang/IllegalStateException
    //   20	71	128	java/lang/IllegalStateException
    //   75	80	128	java/lang/IllegalStateException
    //   80	87	128	java/lang/IllegalStateException
    //   90	97	128	java/lang/IllegalStateException
    //   20	71	132	java/io/IOException
    //   75	80	132	java/io/IOException
    //   80	87	132	java/io/IOException
    //   90	97	132	java/io/IOException
    //   20	71	136	org/apache/http/client/ClientProtocolException
    //   75	80	136	org/apache/http/client/ClientProtocolException
    //   80	87	136	org/apache/http/client/ClientProtocolException
    //   90	97	136	org/apache/http/client/ClientProtocolException
    //   6	20	140	java/io/UnsupportedEncodingException
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/helper/http/HelperHttp.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */