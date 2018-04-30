package ir.eitaa.messenger.exoplayer.extractor.ts;

import ir.eitaa.messenger.exoplayer.MediaFormat;
import ir.eitaa.messenger.exoplayer.extractor.TrackOutput;
import ir.eitaa.messenger.exoplayer.text.eia608.Eia608Parser;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;

final class SeiReader
{
  private final TrackOutput output;
  
  public SeiReader(TrackOutput paramTrackOutput)
  {
    this.output = paramTrackOutput;
    paramTrackOutput.format(MediaFormat.createTextFormat(null, "application/eia-608", -1, -1L, null));
  }
  
  public void consume(long paramLong, ParsableByteArray paramParsableByteArray)
  {
    while (paramParsableByteArray.bytesLeft() > 1)
    {
      int i = 0;
      int k;
      int j;
      do
      {
        k = paramParsableByteArray.readUnsignedByte();
        j = i + k;
        i = j;
      } while (k == 255);
      i = 0;
      int m;
      do
      {
        m = paramParsableByteArray.readUnsignedByte();
        k = i + m;
        i = k;
      } while (m == 255);
      if (Eia608Parser.isSeiMessageEia608(j, k, paramParsableByteArray))
      {
        this.output.sampleData(paramParsableByteArray, k);
        this.output.sampleMetadata(paramLong, 1, k, 0, null);
      }
      else
      {
        paramParsableByteArray.skipBytes(k);
      }
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/ts/SeiReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */