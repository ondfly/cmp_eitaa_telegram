package ir.eitaa.messenger.exoplayer.extractor.ogg;

import ir.eitaa.messenger.exoplayer.ParserException;
import ir.eitaa.messenger.exoplayer.extractor.Extractor;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorInput;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorOutput;
import ir.eitaa.messenger.exoplayer.extractor.PositionHolder;
import ir.eitaa.messenger.exoplayer.extractor.TrackOutput;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;
import java.io.IOException;

public class OggExtractor
  implements Extractor
{
  private StreamReader streamReader;
  
  public void init(ExtractorOutput paramExtractorOutput)
  {
    TrackOutput localTrackOutput = paramExtractorOutput.track(0);
    paramExtractorOutput.endTracks();
    this.streamReader.init(paramExtractorOutput, localTrackOutput);
  }
  
  public int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    return this.streamReader.read(paramExtractorInput, paramPositionHolder);
  }
  
  public void release() {}
  
  public void seek()
  {
    this.streamReader.seek();
  }
  
  public boolean sniff(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    try
    {
      ParsableByteArray localParsableByteArray = new ParsableByteArray(new byte[27], 0);
      OggUtil.PageHeader localPageHeader = new OggUtil.PageHeader();
      if ((OggUtil.populatePageHeader(paramExtractorInput, localPageHeader, localParsableByteArray, true)) && ((localPageHeader.type & 0x2) == 2))
      {
        if (localPageHeader.bodySize < 7) {
          return false;
        }
        localParsableByteArray.reset();
        paramExtractorInput.peekFully(localParsableByteArray.data, 0, 7);
        if (FlacReader.verifyBitstreamType(localParsableByteArray))
        {
          this.streamReader = new FlacReader();
          break label123;
        }
        localParsableByteArray.reset();
        if (VorbisReader.verifyBitstreamType(localParsableByteArray))
        {
          this.streamReader = new VorbisReader();
          break label123;
        }
      }
    }
    catch (ParserException paramExtractorInput)
    {
      paramExtractorInput = paramExtractorInput;
      return false;
    }
    finally {}
    return false;
    label123:
    return true;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/ogg/OggExtractor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */