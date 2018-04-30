package org.telegram.messenger;

import android.net.Uri;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.TransferListener;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;

public class FileStreamLoadOperation
  implements DataSource
{
  private long bytesRemaining;
  private CountDownLatch countDownLatch;
  private int currentAccount;
  private int currentOffset;
  private TLRPC.Document document;
  private RandomAccessFile file;
  private final TransferListener<? super FileStreamLoadOperation> listener;
  private FileLoadOperation loadOperation;
  private boolean opened;
  private Uri uri;
  
  public FileStreamLoadOperation()
  {
    this(null);
  }
  
  public FileStreamLoadOperation(TransferListener<? super FileStreamLoadOperation> paramTransferListener)
  {
    this.listener = paramTransferListener;
  }
  
  public void close()
  {
    if (this.loadOperation != null) {
      this.loadOperation.removeStreamListener(this);
    }
    if (this.countDownLatch != null) {
      this.countDownLatch.countDown();
    }
    if (this.file != null) {}
    try
    {
      this.file.close();
      this.file = null;
      this.uri = null;
      if (this.opened)
      {
        this.opened = false;
        if (this.listener != null) {
          this.listener.onTransferEnd(this);
        }
      }
      return;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public Uri getUri()
  {
    return this.uri;
  }
  
  protected void newDataAvailable()
  {
    if (this.countDownLatch != null) {
      this.countDownLatch.countDown();
    }
  }
  
  public long open(DataSpec paramDataSpec)
    throws IOException
  {
    this.uri = paramDataSpec.uri;
    this.currentAccount = Utilities.parseInt(this.uri.getQueryParameter("account")).intValue();
    this.document = new TLRPC.TL_document();
    this.document.access_hash = Utilities.parseLong(this.uri.getQueryParameter("hash")).longValue();
    this.document.id = Utilities.parseLong(this.uri.getQueryParameter("id")).longValue();
    this.document.size = Utilities.parseInt(this.uri.getQueryParameter("size")).intValue();
    this.document.dc_id = Utilities.parseInt(this.uri.getQueryParameter("dc")).intValue();
    this.document.mime_type = this.uri.getQueryParameter("mime");
    Object localObject = new TLRPC.TL_documentAttributeFilename();
    ((TLRPC.TL_documentAttributeFilename)localObject).file_name = this.uri.getQueryParameter("name");
    this.document.attributes.add(localObject);
    if (this.document.mime_type.startsWith("video"))
    {
      this.document.attributes.add(new TLRPC.TL_documentAttributeVideo());
      localObject = FileLoader.getInstance(this.currentAccount);
      TLRPC.Document localDocument = this.document;
      int i = (int)paramDataSpec.position;
      this.currentOffset = i;
      this.loadOperation = ((FileLoader)localObject).loadStreamFile(this, localDocument, i);
      if (paramDataSpec.length != -1L) {
        break label333;
      }
    }
    label333:
    for (long l = this.document.size - paramDataSpec.position;; l = paramDataSpec.length)
    {
      this.bytesRemaining = l;
      if (this.bytesRemaining >= 0L) {
        break label341;
      }
      throw new EOFException();
      if (!this.document.mime_type.startsWith("audio")) {
        break;
      }
      this.document.attributes.add(new TLRPC.TL_documentAttributeAudio());
      break;
    }
    label341:
    this.opened = true;
    if (this.listener != null) {
      this.listener.onTransferStart(this, paramDataSpec);
    }
    this.file = new RandomAccessFile(this.loadOperation.getCurrentFile(), "r");
    this.file.seek(this.currentOffset);
    return this.bytesRemaining;
  }
  
  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (paramInt2 == 0) {
      paramInt1 = 0;
    }
    int i;
    do
    {
      return paramInt1;
      if (this.bytesRemaining == 0L) {
        return -1;
      }
      int k = 0;
      i = k;
      int j = paramInt2;
      try
      {
        if (this.bytesRemaining < paramInt2)
        {
          j = (int)this.bytesRemaining;
          i = k;
        }
        while (i == 0)
        {
          paramInt2 = this.loadOperation.getDownloadedLengthFromOffset(this.currentOffset, j);
          i = paramInt2;
          if (paramInt2 == 0)
          {
            if (this.loadOperation.isPaused()) {
              FileLoader.getInstance(this.currentAccount).loadStreamFile(this, this.document, this.currentOffset);
            }
            this.countDownLatch = new CountDownLatch(1);
            this.countDownLatch.await();
            i = paramInt2;
          }
        }
        this.file.readFully(paramArrayOfByte, paramInt1, i);
      }
      catch (Exception paramArrayOfByte)
      {
        throw new IOException(paramArrayOfByte);
      }
      this.currentOffset += i;
      this.bytesRemaining -= i;
      paramInt1 = i;
    } while (this.listener == null);
    this.listener.onBytesTransferred(this, i);
    return i;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/FileStreamLoadOperation.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */