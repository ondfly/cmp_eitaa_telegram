package ir.eitaa.messenger.exoplayer.extractor;

import ir.eitaa.messenger.exoplayer.MediaFormat;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;
import java.io.IOException;

public abstract interface TrackOutput
{
  public abstract void format(MediaFormat paramMediaFormat);
  
  public abstract int sampleData(ExtractorInput paramExtractorInput, int paramInt, boolean paramBoolean)
    throws IOException, InterruptedException;
  
  public abstract void sampleData(ParsableByteArray paramParsableByteArray, int paramInt);
  
  public abstract void sampleMetadata(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/TrackOutput.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */