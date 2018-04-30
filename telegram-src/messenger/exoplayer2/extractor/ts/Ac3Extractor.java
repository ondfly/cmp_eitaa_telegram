package org.telegram.messenger.exoplayer2.extractor.ts;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.audio.Ac3Util;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorOutput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorsFactory;
import org.telegram.messenger.exoplayer2.extractor.PositionHolder;
import org.telegram.messenger.exoplayer2.extractor.SeekMap.Unseekable;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.Util;

public final class Ac3Extractor
  implements Extractor
{
  private static final int AC3_SYNC_WORD = 2935;
  public static final ExtractorsFactory FACTORY = new ExtractorsFactory()
  {
    public Extractor[] createExtractors()
    {
      return new Extractor[] { new Ac3Extractor() };
    }
  };
  private static final int ID3_TAG = Util.getIntegerCodeForString("ID3");
  private static final int MAX_SNIFF_BYTES = 8192;
  private static final int MAX_SYNC_FRAME_SIZE = 2786;
  private final long firstSampleTimestampUs;
  private final Ac3Reader reader;
  private final ParsableByteArray sampleData;
  private boolean startedPacket;
  
  public Ac3Extractor()
  {
    this(0L);
  }
  
  public Ac3Extractor(long paramLong)
  {
    this.firstSampleTimestampUs = paramLong;
    this.reader = new Ac3Reader();
    this.sampleData = new ParsableByteArray(2786);
  }
  
  public void init(ExtractorOutput paramExtractorOutput)
  {
    this.reader.createTracks(paramExtractorOutput, new TsPayloadReader.TrackIdGenerator(0, 1));
    paramExtractorOutput.endTracks();
    paramExtractorOutput.seekMap(new SeekMap.Unseekable(-9223372036854775807L));
  }
  
  public int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    int i = paramExtractorInput.read(this.sampleData.data, 0, 2786);
    if (i == -1) {
      return -1;
    }
    this.sampleData.setPosition(0);
    this.sampleData.setLimit(i);
    if (!this.startedPacket)
    {
      this.reader.packetStarted(this.firstSampleTimestampUs, true);
      this.startedPacket = true;
    }
    this.reader.consume(this.sampleData);
    return 0;
  }
  
  public void release() {}
  
  public void seek(long paramLong1, long paramLong2)
  {
    this.startedPacket = false;
    this.reader.seek();
  }
  
  public boolean sniff(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    ParsableByteArray localParsableByteArray = new ParsableByteArray(10);
    int i = 0;
    paramExtractorInput.peekFully(localParsableByteArray.data, 0, 10);
    localParsableByteArray.setPosition(0);
    int k;
    int j;
    if (localParsableByteArray.readUnsignedInt24() != ID3_TAG)
    {
      paramExtractorInput.resetPeekPosition();
      paramExtractorInput.advancePeekPosition(i);
      k = i;
      j = 0;
    }
    for (;;)
    {
      label62:
      paramExtractorInput.peekFully(localParsableByteArray.data, 0, 5);
      localParsableByteArray.setPosition(0);
      if (localParsableByteArray.readUnsignedShort() != 2935)
      {
        j = 0;
        paramExtractorInput.resetPeekPosition();
        k += 1;
        if (k - i < 8192) {}
      }
      int m;
      do
      {
        return false;
        localParsableByteArray.skipBytes(3);
        j = localParsableByteArray.readSynchSafeInt();
        i += j + 10;
        paramExtractorInput.advancePeekPosition(j);
        break;
        paramExtractorInput.advancePeekPosition(k);
        break label62;
        j += 1;
        if (j >= 4) {
          return true;
        }
        m = Ac3Util.parseAc3SyncframeSize(localParsableByteArray.data);
      } while (m == -1);
      paramExtractorInput.advancePeekPosition(m - 5);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/ts/Ac3Extractor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */