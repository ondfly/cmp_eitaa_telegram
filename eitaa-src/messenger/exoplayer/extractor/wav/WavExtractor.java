package ir.eitaa.messenger.exoplayer.extractor.wav;

import ir.eitaa.messenger.exoplayer.MediaFormat;
import ir.eitaa.messenger.exoplayer.ParserException;
import ir.eitaa.messenger.exoplayer.extractor.Extractor;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorInput;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorOutput;
import ir.eitaa.messenger.exoplayer.extractor.PositionHolder;
import ir.eitaa.messenger.exoplayer.extractor.SeekMap;
import ir.eitaa.messenger.exoplayer.extractor.TrackOutput;
import java.io.IOException;

public final class WavExtractor
  implements Extractor, SeekMap
{
  private static final int MAX_INPUT_SIZE = 32768;
  private int bytesPerFrame;
  private ExtractorOutput extractorOutput;
  private int pendingBytes;
  private TrackOutput trackOutput;
  private WavHeader wavHeader;
  
  public long getPosition(long paramLong)
  {
    return this.wavHeader.getPosition(paramLong);
  }
  
  public void init(ExtractorOutput paramExtractorOutput)
  {
    this.extractorOutput = paramExtractorOutput;
    this.trackOutput = paramExtractorOutput.track(0);
    this.wavHeader = null;
    paramExtractorOutput.endTracks();
  }
  
  public boolean isSeekable()
  {
    return true;
  }
  
  public int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    if (this.wavHeader == null)
    {
      this.wavHeader = WavHeaderReader.peek(paramExtractorInput);
      if (this.wavHeader == null) {
        throw new ParserException("Error initializing WavHeader. Did you sniff first?");
      }
      this.bytesPerFrame = this.wavHeader.getBytesPerFrame();
    }
    if (!this.wavHeader.hasDataBounds())
    {
      WavHeaderReader.skipToData(paramExtractorInput, this.wavHeader);
      this.trackOutput.format(MediaFormat.createAudioFormat(null, "audio/raw", this.wavHeader.getBitrate(), 32768, this.wavHeader.getDurationUs(), this.wavHeader.getNumChannels(), this.wavHeader.getSampleRateHz(), null, null, this.wavHeader.getEncoding()));
      this.extractorOutput.seekMap(this);
    }
    int i = this.trackOutput.sampleData(paramExtractorInput, 32768 - this.pendingBytes, true);
    if (i != -1) {
      this.pendingBytes += i;
    }
    int j = this.pendingBytes / this.bytesPerFrame * this.bytesPerFrame;
    if (j > 0)
    {
      long l1 = paramExtractorInput.getPosition();
      long l2 = this.pendingBytes;
      this.pendingBytes -= j;
      this.trackOutput.sampleMetadata(this.wavHeader.getTimeUs(l1 - l2), 1, j, this.pendingBytes, null);
    }
    if (i == -1) {
      return -1;
    }
    return 0;
  }
  
  public void release() {}
  
  public void seek()
  {
    this.pendingBytes = 0;
  }
  
  public boolean sniff(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    return WavHeaderReader.peek(paramExtractorInput) != null;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/wav/WavExtractor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */