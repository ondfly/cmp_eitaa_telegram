package org.telegram.messenger.exoplayer2.extractor.ts;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorOutput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorsFactory;
import org.telegram.messenger.exoplayer2.extractor.PositionHolder;
import org.telegram.messenger.exoplayer2.extractor.SeekMap.Unseekable;
import org.telegram.messenger.exoplayer2.util.ParsableBitArray;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.Util;

public final class AdtsExtractor
  implements Extractor
{
  public static final ExtractorsFactory FACTORY = new ExtractorsFactory()
  {
    public Extractor[] createExtractors()
    {
      return new Extractor[] { new AdtsExtractor() };
    }
  };
  private static final int ID3_TAG = Util.getIntegerCodeForString("ID3");
  private static final int MAX_PACKET_SIZE = 200;
  private static final int MAX_SNIFF_BYTES = 8192;
  private final long firstSampleTimestampUs;
  private final ParsableByteArray packetBuffer;
  private final AdtsReader reader;
  private boolean startedPacket;
  
  public AdtsExtractor()
  {
    this(0L);
  }
  
  public AdtsExtractor(long paramLong)
  {
    this.firstSampleTimestampUs = paramLong;
    this.reader = new AdtsReader(true);
    this.packetBuffer = new ParsableByteArray(200);
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
    int i = paramExtractorInput.read(this.packetBuffer.data, 0, 200);
    if (i == -1) {
      return -1;
    }
    this.packetBuffer.setPosition(0);
    this.packetBuffer.setLimit(i);
    if (!this.startedPacket)
    {
      this.reader.packetStarted(this.firstSampleTimestampUs, true);
      this.startedPacket = true;
    }
    this.reader.consume(this.packetBuffer);
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
    ParsableBitArray localParsableBitArray = new ParsableBitArray(localParsableByteArray.data);
    int i = 0;
    paramExtractorInput.peekFully(localParsableByteArray.data, 0, 10);
    localParsableByteArray.setPosition(0);
    int m;
    int j;
    int k;
    if (localParsableByteArray.readUnsignedInt24() != ID3_TAG)
    {
      paramExtractorInput.resetPeekPosition();
      paramExtractorInput.advancePeekPosition(i);
      m = i;
      j = 0;
      k = 0;
    }
    for (;;)
    {
      label79:
      paramExtractorInput.peekFully(localParsableByteArray.data, 0, 2);
      localParsableByteArray.setPosition(0);
      if ((0xFFF6 & localParsableByteArray.readUnsignedShort()) != 65520)
      {
        k = 0;
        j = 0;
        paramExtractorInput.resetPeekPosition();
        m += 1;
        if (m - i < 8192) {}
      }
      int n;
      do
      {
        return false;
        localParsableByteArray.skipBytes(3);
        j = localParsableByteArray.readSynchSafeInt();
        i += j + 10;
        paramExtractorInput.advancePeekPosition(j);
        break;
        paramExtractorInput.advancePeekPosition(m);
        break label79;
        k += 1;
        if ((k >= 4) && (j > 188)) {
          return true;
        }
        paramExtractorInput.peekFully(localParsableByteArray.data, 0, 4);
        localParsableBitArray.setPosition(14);
        n = localParsableBitArray.readBits(13);
      } while (n <= 6);
      paramExtractorInput.advancePeekPosition(n - 6);
      j += n;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/ts/AdtsExtractor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */