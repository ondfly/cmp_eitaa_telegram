package ir.eitaa.messenger.exoplayer.chunk;

import ir.eitaa.messenger.exoplayer.MediaFormat;
import ir.eitaa.messenger.exoplayer.drm.DrmInitData;
import ir.eitaa.messenger.exoplayer.extractor.Extractor;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorInput;
import ir.eitaa.messenger.exoplayer.extractor.ExtractorOutput;
import ir.eitaa.messenger.exoplayer.extractor.SeekMap;
import ir.eitaa.messenger.exoplayer.extractor.TrackOutput;
import ir.eitaa.messenger.exoplayer.util.Assertions;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;
import java.io.IOException;

public class ChunkExtractorWrapper
  implements ExtractorOutput, TrackOutput
{
  private final Extractor extractor;
  private boolean extractorInitialized;
  private SingleTrackOutput output;
  private boolean seenTrack;
  
  public ChunkExtractorWrapper(Extractor paramExtractor)
  {
    this.extractor = paramExtractor;
  }
  
  public void drmInitData(DrmInitData paramDrmInitData)
  {
    this.output.drmInitData(paramDrmInitData);
  }
  
  public void endTracks()
  {
    Assertions.checkState(this.seenTrack);
  }
  
  public void format(MediaFormat paramMediaFormat)
  {
    this.output.format(paramMediaFormat);
  }
  
  public void init(SingleTrackOutput paramSingleTrackOutput)
  {
    this.output = paramSingleTrackOutput;
    if (!this.extractorInitialized)
    {
      this.extractor.init(this);
      this.extractorInitialized = true;
      return;
    }
    this.extractor.seek();
  }
  
  public int read(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    boolean bool = true;
    int i = this.extractor.read(paramExtractorInput, null);
    if (i != 1) {}
    for (;;)
    {
      Assertions.checkState(bool);
      return i;
      bool = false;
    }
  }
  
  public int sampleData(ExtractorInput paramExtractorInput, int paramInt, boolean paramBoolean)
    throws IOException, InterruptedException
  {
    return this.output.sampleData(paramExtractorInput, paramInt, paramBoolean);
  }
  
  public void sampleData(ParsableByteArray paramParsableByteArray, int paramInt)
  {
    this.output.sampleData(paramParsableByteArray, paramInt);
  }
  
  public void sampleMetadata(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte)
  {
    this.output.sampleMetadata(paramLong, paramInt1, paramInt2, paramInt3, paramArrayOfByte);
  }
  
  public void seekMap(SeekMap paramSeekMap)
  {
    this.output.seekMap(paramSeekMap);
  }
  
  public TrackOutput track(int paramInt)
  {
    if (!this.seenTrack) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      this.seenTrack = true;
      return this;
    }
  }
  
  public static abstract interface SingleTrackOutput
    extends TrackOutput
  {
    public abstract void drmInitData(DrmInitData paramDrmInitData);
    
    public abstract void seekMap(SeekMap paramSeekMap);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/chunk/ChunkExtractorWrapper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */