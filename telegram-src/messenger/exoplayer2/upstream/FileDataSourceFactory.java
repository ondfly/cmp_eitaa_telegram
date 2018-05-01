package org.telegram.messenger.exoplayer2.upstream;

public final class FileDataSourceFactory
  implements DataSource.Factory
{
  private final TransferListener<? super FileDataSource> listener;
  
  public FileDataSourceFactory()
  {
    this(null);
  }
  
  public FileDataSourceFactory(TransferListener<? super FileDataSource> paramTransferListener)
  {
    this.listener = paramTransferListener;
  }
  
  public DataSource createDataSource()
  {
    return new FileDataSource(this.listener);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/FileDataSourceFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */