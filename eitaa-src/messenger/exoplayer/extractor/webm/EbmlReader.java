package ir.eitaa.messenger.exoplayer.extractor.webm;

import ir.eitaa.messenger.exoplayer.ParserException;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorInput;
import java.io.IOException;

abstract interface EbmlReader
{
  public static final int TYPE_BINARY = 4;
  public static final int TYPE_FLOAT = 5;
  public static final int TYPE_MASTER = 1;
  public static final int TYPE_STRING = 3;
  public static final int TYPE_UNKNOWN = 0;
  public static final int TYPE_UNSIGNED_INT = 2;
  
  public abstract void init(EbmlReaderOutput paramEbmlReaderOutput);
  
  public abstract boolean read(ExtractorInput paramExtractorInput)
    throws ParserException, IOException, InterruptedException;
  
  public abstract void reset();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/webm/EbmlReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */