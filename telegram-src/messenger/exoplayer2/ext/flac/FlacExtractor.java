package org.telegram.messenger.exoplayer2.ext.flac;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorOutput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorsFactory;
import org.telegram.messenger.exoplayer2.extractor.PositionHolder;
import org.telegram.messenger.exoplayer2.extractor.SeekMap;
import org.telegram.messenger.exoplayer2.extractor.SeekMap.SeekPoints;
import org.telegram.messenger.exoplayer2.extractor.SeekMap.Unseekable;
import org.telegram.messenger.exoplayer2.extractor.SeekPoint;
import org.telegram.messenger.exoplayer2.extractor.TrackOutput;
import org.telegram.messenger.exoplayer2.util.FlacStreamInfo;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.Util;

public final class FlacExtractor
  implements Extractor
{
  public static final ExtractorsFactory FACTORY = new ExtractorsFactory()
  {
    public Extractor[] createExtractors()
    {
      return new Extractor[] { new FlacExtractor() };
    }
  };
  private static final byte[] FLAC_SIGNATURE = { 102, 76, 97, 67, 0, 0, 0, 34 };
  private FlacDecoderJni decoderJni;
  private ExtractorOutput extractorOutput;
  private boolean metadataParsed;
  private ParsableByteArray outputBuffer;
  private ByteBuffer outputByteBuffer;
  private TrackOutput trackOutput;
  
  public void init(ExtractorOutput paramExtractorOutput)
  {
    this.extractorOutput = paramExtractorOutput;
    this.trackOutput = this.extractorOutput.track(0, 1);
    this.extractorOutput.endTracks();
    try
    {
      this.decoderJni = new FlacDecoderJni();
      return;
    }
    catch (FlacDecoderException paramExtractorOutput)
    {
      throw new RuntimeException(paramExtractorOutput);
    }
  }
  
  public int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    this.decoderJni.setData(paramExtractorInput);
    FlacStreamInfo localFlacStreamInfo;
    int i;
    ExtractorOutput localExtractorOutput;
    if (!this.metadataParsed)
    {
      try
      {
        localFlacStreamInfo = this.decoderJni.decodeMetadata();
        if (localFlacStreamInfo == null) {
          throw new IOException("Metadata decoding failed");
        }
      }
      catch (IOException paramPositionHolder)
      {
        this.decoderJni.reset(0L);
        paramExtractorInput.setRetryPosition(0L, paramPositionHolder);
        throw paramPositionHolder;
      }
      this.metadataParsed = true;
      if (this.decoderJni.getSeekPosition(0L) == -1L) {
        break label225;
      }
      i = 1;
      localExtractorOutput = this.extractorOutput;
      if (i == 0) {
        break label230;
      }
    }
    long l;
    label225:
    label230:
    for (paramPositionHolder = new FlacSeekMap(localFlacStreamInfo.durationUs(), this.decoderJni);; paramPositionHolder = new SeekMap.Unseekable(localFlacStreamInfo.durationUs(), 0L))
    {
      localExtractorOutput.seekMap(paramPositionHolder);
      paramPositionHolder = Format.createAudioSampleFormat(null, "audio/raw", null, localFlacStreamInfo.bitRate(), -1, localFlacStreamInfo.channels, localFlacStreamInfo.sampleRate, Util.getPcmEncoding(localFlacStreamInfo.bitsPerSample), null, null, 0, null);
      this.trackOutput.format(paramPositionHolder);
      this.outputBuffer = new ParsableByteArray(localFlacStreamInfo.maxDecodedFrameSize());
      this.outputByteBuffer = ByteBuffer.wrap(this.outputBuffer.data);
      this.outputBuffer.reset();
      l = this.decoderJni.getDecodePosition();
      try
      {
        i = this.decoderJni.decodeSample(this.outputByteBuffer);
        if (i > 0) {
          break label275;
        }
        return -1;
      }
      catch (IOException paramPositionHolder)
      {
        if (l < 0L) {
          break label273;
        }
        this.decoderJni.reset(l);
        paramExtractorInput.setRetryPosition(l, paramPositionHolder);
        throw paramPositionHolder;
      }
      i = 0;
      break;
    }
    label273:
    label275:
    this.trackOutput.sampleData(this.outputBuffer, i);
    this.trackOutput.sampleMetadata(this.decoderJni.getLastSampleTimestamp(), 1, i, 0, null);
    if (this.decoderJni.isEndOfData()) {
      return -1;
    }
    return 0;
  }
  
  public void release()
  {
    if (this.decoderJni != null)
    {
      this.decoderJni.release();
      this.decoderJni = null;
    }
  }
  
  public void seek(long paramLong1, long paramLong2)
  {
    if (paramLong1 == 0L) {
      this.metadataParsed = false;
    }
    if (this.decoderJni != null) {
      this.decoderJni.reset(paramLong1);
    }
  }
  
  public boolean sniff(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    byte[] arrayOfByte = new byte[FLAC_SIGNATURE.length];
    paramExtractorInput.peekFully(arrayOfByte, 0, FLAC_SIGNATURE.length);
    return Arrays.equals(arrayOfByte, FLAC_SIGNATURE);
  }
  
  private static final class FlacSeekMap
    implements SeekMap
  {
    private final FlacDecoderJni decoderJni;
    private final long durationUs;
    
    public FlacSeekMap(long paramLong, FlacDecoderJni paramFlacDecoderJni)
    {
      this.durationUs = paramLong;
      this.decoderJni = paramFlacDecoderJni;
    }
    
    public long getDurationUs()
    {
      return this.durationUs;
    }
    
    public SeekMap.SeekPoints getSeekPoints(long paramLong)
    {
      return new SeekMap.SeekPoints(new SeekPoint(paramLong, this.decoderJni.getSeekPosition(paramLong)));
    }
    
    public boolean isSeekable()
    {
      return true;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/ext/flac/FlacExtractor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */