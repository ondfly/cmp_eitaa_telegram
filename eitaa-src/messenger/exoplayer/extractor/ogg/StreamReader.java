package ir.eitaa.messenger.exoplayer.extractor.ogg;

import ir.eitaa.messenger.exoplayer.extractor.ExtractorInput;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorOutput;
import ir.eitaa.messenger.exoplayer.extractor.PositionHolder;
import ir.eitaa.messenger.exoplayer.extractor.TrackOutput;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;
import java.io.IOException;

abstract class StreamReader
{
  protected ExtractorOutput extractorOutput;
  protected final OggParser oggParser = new OggParser();
  protected final ParsableByteArray scratch = new ParsableByteArray(new byte[65025], 0);
  protected TrackOutput trackOutput;
  
  void init(ExtractorOutput paramExtractorOutput, TrackOutput paramTrackOutput)
  {
    this.extractorOutput = paramExtractorOutput;
    this.trackOutput = paramTrackOutput;
  }
  
  abstract int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException;
  
  void seek()
  {
    this.oggParser.reset();
    this.scratch.reset();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/ogg/StreamReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */