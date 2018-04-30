package ir.eitaa.messenger.exoplayer.extractor.ogg;

import ir.eitaa.messenger.exoplayer.MediaFormat;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorInput;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorOutput;
import ir.eitaa.messenger.exoplayer.extractor.PositionHolder;
import ir.eitaa.messenger.exoplayer.extractor.SeekMap;
import ir.eitaa.messenger.exoplayer.extractor.TrackOutput;
import ir.eitaa.messenger.exoplayer.util.FlacSeekTable;
import ir.eitaa.messenger.exoplayer.util.FlacStreamInfo;
import ir.eitaa.messenger.exoplayer.util.FlacUtil;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

final class FlacReader
  extends StreamReader
{
  private static final byte AUDIO_PACKET_TYPE = -1;
  private static final byte SEEKTABLE_PACKET_TYPE = 3;
  private boolean firstAudioPacketProcessed;
  private FlacSeekTable seekTable;
  private FlacStreamInfo streamInfo;
  
  static boolean verifyBitstreamType(ParsableByteArray paramParsableByteArray)
  {
    return (paramParsableByteArray.readUnsignedByte() == 127) && (paramParsableByteArray.readUnsignedInt() == 1179402563L);
  }
  
  public int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    long l = paramExtractorInput.getPosition();
    if (!this.oggParser.readPacket(paramExtractorInput, this.scratch)) {
      return -1;
    }
    paramExtractorInput = this.scratch.data;
    if (this.streamInfo == null)
    {
      this.streamInfo = new FlacStreamInfo(paramExtractorInput, 17);
      paramExtractorInput = Arrays.copyOfRange(paramExtractorInput, 9, this.scratch.limit());
      paramExtractorInput[4] = -128;
      paramExtractorInput = Collections.singletonList(paramExtractorInput);
      paramExtractorInput = MediaFormat.createAudioFormat(null, "audio/x-flac", this.streamInfo.bitRate(), -1, this.streamInfo.durationUs(), this.streamInfo.channels, this.streamInfo.sampleRate, paramExtractorInput, null);
      this.trackOutput.format(paramExtractorInput);
    }
    for (;;)
    {
      this.scratch.reset();
      return 0;
      if (paramExtractorInput[0] == -1)
      {
        if (!this.firstAudioPacketProcessed)
        {
          if (this.seekTable == null) {
            break label253;
          }
          this.extractorOutput.seekMap(this.seekTable.createSeekMap(l, this.streamInfo.sampleRate));
          this.seekTable = null;
        }
        for (;;)
        {
          this.firstAudioPacketProcessed = true;
          this.trackOutput.sampleData(this.scratch, this.scratch.limit());
          this.scratch.setPosition(0);
          l = FlacUtil.extractSampleTimestamp(this.streamInfo, this.scratch);
          this.trackOutput.sampleMetadata(l, 1, this.scratch.limit(), 0, null);
          break;
          label253:
          this.extractorOutput.seekMap(SeekMap.UNSEEKABLE);
        }
      }
      if (((paramExtractorInput[0] & 0x7F) == 3) && (this.seekTable == null)) {
        this.seekTable = FlacSeekTable.parseSeekTable(this.scratch);
      }
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/ogg/FlacReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */