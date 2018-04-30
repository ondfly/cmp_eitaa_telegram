package ir.eitaa.messenger.exoplayer.extractor.ts;

import ir.eitaa.messenger.exoplayer.extractor.TrackOutput;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;

abstract class ElementaryStreamReader
{
  protected final TrackOutput output;
  
  protected ElementaryStreamReader(TrackOutput paramTrackOutput)
  {
    this.output = paramTrackOutput;
  }
  
  public abstract void consume(ParsableByteArray paramParsableByteArray);
  
  public abstract void packetFinished();
  
  public abstract void packetStarted(long paramLong, boolean paramBoolean);
  
  public abstract void seek();
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/ts/ElementaryStreamReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */