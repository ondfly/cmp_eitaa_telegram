package org.telegram.messenger.exoplayer2.extractor;

import java.io.IOException;

public abstract interface Extractor
{
  public static final int RESULT_CONTINUE = 0;
  public static final int RESULT_END_OF_INPUT = -1;
  public static final int RESULT_SEEK = 1;
  
  public abstract void init(ExtractorOutput paramExtractorOutput);
  
  public abstract int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException;
  
  public abstract void release();
  
  public abstract void seek(long paramLong1, long paramLong2);
  
  public abstract boolean sniff(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException;
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/Extractor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */