package org.telegram.messenger.exoplayer2.source;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.FormatHolder;
import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;

public abstract interface SampleStream
{
  public abstract boolean isReady();
  
  public abstract void maybeThrowError()
    throws IOException;
  
  public abstract int readData(FormatHolder paramFormatHolder, DecoderInputBuffer paramDecoderInputBuffer, boolean paramBoolean);
  
  public abstract int skipData(long paramLong);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/SampleStream.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */