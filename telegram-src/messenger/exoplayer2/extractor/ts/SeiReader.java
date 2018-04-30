package org.telegram.messenger.exoplayer2.extractor.ts;

import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.extractor.ExtractorOutput;
import org.telegram.messenger.exoplayer2.extractor.TrackOutput;
import org.telegram.messenger.exoplayer2.text.cea.CeaUtil;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;

final class SeiReader
{
  private final List<Format> closedCaptionFormats;
  private final TrackOutput[] outputs;
  
  public SeiReader(List<Format> paramList)
  {
    this.closedCaptionFormats = paramList;
    this.outputs = new TrackOutput[paramList.size()];
  }
  
  public void consume(long paramLong, ParsableByteArray paramParsableByteArray)
  {
    CeaUtil.consume(paramLong, paramParsableByteArray, this.outputs);
  }
  
  public void createTracks(ExtractorOutput paramExtractorOutput, TsPayloadReader.TrackIdGenerator paramTrackIdGenerator)
  {
    int i = 0;
    if (i < this.outputs.length)
    {
      paramTrackIdGenerator.generateNewId();
      TrackOutput localTrackOutput = paramExtractorOutput.track(paramTrackIdGenerator.getTrackId(), 3);
      Format localFormat = (Format)this.closedCaptionFormats.get(i);
      String str2 = localFormat.sampleMimeType;
      boolean bool;
      if (("application/cea-608".equals(str2)) || ("application/cea-708".equals(str2)))
      {
        bool = true;
        label73:
        Assertions.checkArgument(bool, "Invalid closed caption mime type provided: " + str2);
        if (localFormat.id == null) {
          break label166;
        }
      }
      label166:
      for (String str1 = localFormat.id;; str1 = paramTrackIdGenerator.getFormatId())
      {
        localTrackOutput.format(Format.createTextSampleFormat(str1, str2, null, -1, localFormat.selectionFlags, localFormat.language, localFormat.accessibilityChannel, null));
        this.outputs[i] = localTrackOutput;
        i += 1;
        break;
        bool = false;
        break label73;
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/ts/SeiReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */