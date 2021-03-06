package org.telegram.messenger.exoplayer2.extractor.ogg;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.SeekMap;

abstract interface OggSeeker
{
  public abstract SeekMap createSeekMap();
  
  public abstract long read(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException;
  
  public abstract long startSeek(long paramLong);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/ogg/OggSeeker.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */