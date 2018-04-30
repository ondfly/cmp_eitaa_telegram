package ir.eitaa.messenger.exoplayer.upstream;

import android.content.Context;
import android.net.Uri;
import ir.eitaa.messenger.exoplayer.util.Assertions;
import ir.eitaa.messenger.exoplayer.util.Util;
import java.io.IOException;

public final class DefaultUriDataSource
  implements UriDataSource
{
  private static final String SCHEME_ASSET = "asset";
  private static final String SCHEME_CONTENT = "content";
  private final UriDataSource assetDataSource;
  private final UriDataSource contentDataSource;
  private UriDataSource dataSource;
  private final UriDataSource fileDataSource;
  private final UriDataSource httpDataSource;
  
  public DefaultUriDataSource(Context paramContext, TransferListener paramTransferListener, UriDataSource paramUriDataSource)
  {
    this.httpDataSource = ((UriDataSource)Assertions.checkNotNull(paramUriDataSource));
    this.fileDataSource = new FileDataSource(paramTransferListener);
    this.assetDataSource = new AssetDataSource(paramContext, paramTransferListener);
    this.contentDataSource = new ContentDataSource(paramContext, paramTransferListener);
  }
  
  public DefaultUriDataSource(Context paramContext, TransferListener paramTransferListener, String paramString)
  {
    this(paramContext, paramTransferListener, paramString, false);
  }
  
  public DefaultUriDataSource(Context paramContext, TransferListener paramTransferListener, String paramString, boolean paramBoolean)
  {
    this(paramContext, paramTransferListener, new DefaultHttpDataSource(paramString, null, paramTransferListener, 8000, 8000, paramBoolean));
  }
  
  public DefaultUriDataSource(Context paramContext, String paramString)
  {
    this(paramContext, null, paramString, false);
  }
  
  public void close()
    throws IOException
  {
    if (this.dataSource != null) {}
    try
    {
      this.dataSource.close();
      return;
    }
    finally
    {
      this.dataSource = null;
    }
  }
  
  public String getUri()
  {
    if (this.dataSource == null) {
      return null;
    }
    return this.dataSource.getUri();
  }
  
  public long open(DataSpec paramDataSpec)
    throws IOException
  {
    boolean bool;
    String str;
    if (this.dataSource == null)
    {
      bool = true;
      Assertions.checkState(bool);
      str = paramDataSpec.uri.getScheme();
      if (!Util.isLocalFileUri(paramDataSpec.uri)) {
        break label81;
      }
      if (!paramDataSpec.uri.getPath().startsWith("/android_asset/")) {
        break label70;
      }
      this.dataSource = this.assetDataSource;
    }
    for (;;)
    {
      return this.dataSource.open(paramDataSpec);
      bool = false;
      break;
      label70:
      this.dataSource = this.fileDataSource;
      continue;
      label81:
      if ("asset".equals(str)) {
        this.dataSource = this.assetDataSource;
      } else if ("content".equals(str)) {
        this.dataSource = this.contentDataSource;
      } else {
        this.dataSource = this.httpDataSource;
      }
    }
  }
  
  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    return this.dataSource.read(paramArrayOfByte, paramInt1, paramInt2);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/upstream/DefaultUriDataSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */