package org.telegram.messenger.exoplayer2.upstream.crypto;

import android.net.Uri;
import java.io.IOException;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;

public final class AesCipherDataSource
  implements DataSource
{
  private AesFlushingCipher cipher;
  private final byte[] secretKey;
  private final DataSource upstream;
  
  public AesCipherDataSource(byte[] paramArrayOfByte, DataSource paramDataSource)
  {
    this.upstream = paramDataSource;
    this.secretKey = paramArrayOfByte;
  }
  
  public void close()
    throws IOException
  {
    this.cipher = null;
    this.upstream.close();
  }
  
  public Uri getUri()
  {
    return this.upstream.getUri();
  }
  
  public long open(DataSpec paramDataSpec)
    throws IOException
  {
    long l1 = this.upstream.open(paramDataSpec);
    long l2 = CryptoUtil.getFNV64Hash(paramDataSpec.key);
    this.cipher = new AesFlushingCipher(2, this.secretKey, l2, paramDataSpec.absoluteStreamPosition);
    return l1;
  }
  
  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (paramInt2 == 0) {
      return 0;
    }
    paramInt2 = this.upstream.read(paramArrayOfByte, paramInt1, paramInt2);
    if (paramInt2 == -1) {
      return -1;
    }
    this.cipher.updateInPlace(paramArrayOfByte, paramInt1, paramInt2);
    return paramInt2;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/crypto/AesCipherDataSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */