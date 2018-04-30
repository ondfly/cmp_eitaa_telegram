package org.telegram.messenger.exoplayer2.upstream;

import android.net.Uri;
import android.util.Base64;
import java.io.IOException;
import java.net.URLDecoder;
import org.telegram.messenger.exoplayer2.ParserException;

public final class DataSchemeDataSource
  implements DataSource
{
  public static final String SCHEME_DATA = "data";
  private int bytesRead;
  private byte[] data;
  private DataSpec dataSpec;
  
  public void close()
    throws IOException
  {
    this.dataSpec = null;
    this.data = null;
  }
  
  public Uri getUri()
  {
    if (this.dataSpec != null) {
      return this.dataSpec.uri;
    }
    return null;
  }
  
  public long open(DataSpec paramDataSpec)
    throws IOException
  {
    this.dataSpec = paramDataSpec;
    paramDataSpec = paramDataSpec.uri;
    Object localObject = paramDataSpec.getScheme();
    if (!"data".equals(localObject)) {
      throw new ParserException("Unsupported scheme: " + (String)localObject);
    }
    localObject = paramDataSpec.getSchemeSpecificPart().split(",");
    if (localObject.length > 2) {
      throw new ParserException("Unexpected URI format: " + paramDataSpec);
    }
    paramDataSpec = localObject[1];
    if (localObject[0].contains(";base64")) {}
    for (;;)
    {
      try
      {
        this.data = Base64.decode(paramDataSpec, 0);
        return this.data.length;
      }
      catch (IllegalArgumentException localIllegalArgumentException)
      {
        throw new ParserException("Error while parsing Base64 encoded string: " + paramDataSpec, localIllegalArgumentException);
      }
      this.data = URLDecoder.decode(paramDataSpec, "US-ASCII").getBytes();
    }
  }
  
  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramInt2 == 0) {
      return 0;
    }
    int i = this.data.length - this.bytesRead;
    if (i == 0) {
      return -1;
    }
    paramInt2 = Math.min(paramInt2, i);
    System.arraycopy(this.data, this.bytesRead, paramArrayOfByte, paramInt1, paramInt2);
    this.bytesRead += paramInt2;
    return paramInt2;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/DataSchemeDataSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */