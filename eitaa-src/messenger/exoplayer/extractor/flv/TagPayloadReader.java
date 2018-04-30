package ir.eitaa.messenger.exoplayer.extractor.flv;

import ir.eitaa.messenger.exoplayer.ParserException;
import ir.eitaa.messenger.exoplayer.extractor.TrackOutput;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;

abstract class TagPayloadReader
{
  private long durationUs;
  protected final TrackOutput output;
  
  protected TagPayloadReader(TrackOutput paramTrackOutput)
  {
    this.output = paramTrackOutput;
    this.durationUs = -1L;
  }
  
  public final void consume(ParsableByteArray paramParsableByteArray, long paramLong)
    throws ParserException
  {
    if (parseHeader(paramParsableByteArray)) {
      parsePayload(paramParsableByteArray, paramLong);
    }
  }
  
  public final long getDurationUs()
  {
    return this.durationUs;
  }
  
  protected abstract boolean parseHeader(ParsableByteArray paramParsableByteArray)
    throws ParserException;
  
  protected abstract void parsePayload(ParsableByteArray paramParsableByteArray, long paramLong)
    throws ParserException;
  
  public abstract void seek();
  
  public final void setDurationUs(long paramLong)
  {
    this.durationUs = paramLong;
  }
  
  public static final class UnsupportedFormatException
    extends ParserException
  {
    public UnsupportedFormatException(String paramString)
    {
      super();
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/flv/TagPayloadReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */