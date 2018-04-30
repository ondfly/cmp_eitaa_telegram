package org.telegram.messenger.secretmedia;

import android.net.Uri;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.TransferListener;

public final class EncryptedFileDataSource
  implements DataSource
{
  private long bytesRemaining;
  private RandomAccessFile file;
  private int fileOffset;
  private byte[] iv = new byte[16];
  private byte[] key = new byte[32];
  private final TransferListener<? super EncryptedFileDataSource> listener;
  private boolean opened;
  private Uri uri;
  
  public EncryptedFileDataSource()
  {
    this(null);
  }
  
  public EncryptedFileDataSource(TransferListener<? super EncryptedFileDataSource> paramTransferListener)
  {
    this.listener = paramTransferListener;
  }
  
  public void close()
    throws EncryptedFileDataSource.EncryptedFileDataSourceException
  {
    this.uri = null;
    this.fileOffset = 0;
    try
    {
      if (this.file != null) {
        this.file.close();
      }
      return;
    }
    catch (IOException localIOException)
    {
      throw new EncryptedFileDataSourceException(localIOException);
    }
    finally
    {
      this.file = null;
      if (this.opened)
      {
        this.opened = false;
        if (this.listener != null) {
          this.listener.onTransferEnd(this);
        }
      }
    }
  }
  
  public Uri getUri()
  {
    return this.uri;
  }
  
  public long open(DataSpec paramDataSpec)
    throws EncryptedFileDataSource.EncryptedFileDataSourceException
  {
    for (;;)
    {
      try
      {
        this.uri = paramDataSpec.uri;
        File localFile = new File(paramDataSpec.uri.getPath());
        Object localObject = localFile.getName();
        localObject = new RandomAccessFile(new File(FileLoader.getInternalCacheDir(), (String)localObject + ".key"), "r");
        ((RandomAccessFile)localObject).read(this.key);
        ((RandomAccessFile)localObject).read(this.iv);
        ((RandomAccessFile)localObject).close();
        this.file = new RandomAccessFile(localFile, "r");
        this.file.seek(paramDataSpec.position);
        this.fileOffset = ((int)paramDataSpec.position);
        if (paramDataSpec.length == -1L)
        {
          l = this.file.length() - paramDataSpec.position;
          this.bytesRemaining = l;
          if (this.bytesRemaining >= 0L) {
            break;
          }
          throw new EOFException();
        }
      }
      catch (IOException paramDataSpec)
      {
        throw new EncryptedFileDataSourceException(paramDataSpec);
      }
      long l = paramDataSpec.length;
    }
    this.opened = true;
    if (this.listener != null) {
      this.listener.onTransferStart(this, paramDataSpec);
    }
    return this.bytesRemaining;
  }
  
  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws EncryptedFileDataSource.EncryptedFileDataSourceException
  {
    if (paramInt2 == 0) {
      paramInt1 = 0;
    }
    for (;;)
    {
      return paramInt1;
      if (this.bytesRemaining == 0L) {
        return -1;
      }
      try
      {
        paramInt2 = this.file.read(paramArrayOfByte, paramInt1, (int)Math.min(this.bytesRemaining, paramInt2));
        Utilities.aesCtrDecryptionByteArray(paramArrayOfByte, this.key, this.iv, paramInt1, paramInt2, this.fileOffset);
        this.fileOffset += paramInt2;
        paramInt1 = paramInt2;
        if (paramInt2 <= 0) {
          continue;
        }
        this.bytesRemaining -= paramInt2;
        paramInt1 = paramInt2;
        if (this.listener == null) {
          continue;
        }
        this.listener.onBytesTransferred(this, paramInt2);
        return paramInt2;
      }
      catch (IOException paramArrayOfByte)
      {
        throw new EncryptedFileDataSourceException(paramArrayOfByte);
      }
    }
  }
  
  public static class EncryptedFileDataSourceException
    extends IOException
  {
    public EncryptedFileDataSourceException(IOException paramIOException)
    {
      super();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/secretmedia/EncryptedFileDataSource.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */