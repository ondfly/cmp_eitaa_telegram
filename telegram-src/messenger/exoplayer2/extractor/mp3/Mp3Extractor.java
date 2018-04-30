package org.telegram.messenger.exoplayer2.extractor.mp3;

import java.io.EOFException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.ParserException;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorOutput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorsFactory;
import org.telegram.messenger.exoplayer2.extractor.GaplessInfoHolder;
import org.telegram.messenger.exoplayer2.extractor.MpegAudioHeader;
import org.telegram.messenger.exoplayer2.extractor.PositionHolder;
import org.telegram.messenger.exoplayer2.extractor.SeekMap;
import org.telegram.messenger.exoplayer2.extractor.TrackOutput;
import org.telegram.messenger.exoplayer2.metadata.Metadata;
import org.telegram.messenger.exoplayer2.metadata.id3.Id3Decoder;
import org.telegram.messenger.exoplayer2.metadata.id3.Id3Decoder.FramePredicate;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.Util;

public final class Mp3Extractor
  implements Extractor
{
  public static final ExtractorsFactory FACTORY = new ExtractorsFactory()
  {
    public Extractor[] createExtractors()
    {
      return new Extractor[] { new Mp3Extractor() };
    }
  };
  public static final int FLAG_DISABLE_ID3_METADATA = 2;
  public static final int FLAG_ENABLE_CONSTANT_BITRATE_SEEKING = 1;
  private static final int MAX_SNIFF_BYTES = 16384;
  private static final int MAX_SYNC_BYTES = 131072;
  private static final int MPEG_AUDIO_HEADER_MASK = -128000;
  private static final int SCRATCH_LENGTH = 10;
  private static final int SEEK_HEADER_INFO = Util.getIntegerCodeForString("Info");
  private static final int SEEK_HEADER_UNSET = 0;
  private static final int SEEK_HEADER_VBRI = Util.getIntegerCodeForString("VBRI");
  private static final int SEEK_HEADER_XING = Util.getIntegerCodeForString("Xing");
  private long basisTimeUs;
  private ExtractorOutput extractorOutput;
  private final int flags;
  private final long forcedFirstSampleTimestampUs;
  private final GaplessInfoHolder gaplessInfoHolder;
  private Metadata metadata;
  private int sampleBytesRemaining;
  private long samplesRead;
  private final ParsableByteArray scratch;
  private Seeker seeker;
  private final MpegAudioHeader synchronizedHeader;
  private int synchronizedHeaderData;
  private TrackOutput trackOutput;
  
  public Mp3Extractor()
  {
    this(0);
  }
  
  public Mp3Extractor(int paramInt)
  {
    this(paramInt, -9223372036854775807L);
  }
  
  public Mp3Extractor(int paramInt, long paramLong)
  {
    this.flags = paramInt;
    this.forcedFirstSampleTimestampUs = paramLong;
    this.scratch = new ParsableByteArray(10);
    this.synchronizedHeader = new MpegAudioHeader();
    this.gaplessInfoHolder = new GaplessInfoHolder();
    this.basisTimeUs = -9223372036854775807L;
  }
  
  private Seeker getConstantBitrateSeeker(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    paramExtractorInput.peekFully(this.scratch.data, 0, 4);
    this.scratch.setPosition(0);
    MpegAudioHeader.populateHeader(this.scratch.readInt(), this.synchronizedHeader);
    return new ConstantBitrateSeeker(paramExtractorInput.getLength(), paramExtractorInput.getPosition(), this.synchronizedHeader);
  }
  
  private static int getSeekFrameHeader(ParsableByteArray paramParsableByteArray, int paramInt)
  {
    if (paramParsableByteArray.limit() >= paramInt + 4)
    {
      paramParsableByteArray.setPosition(paramInt);
      paramInt = paramParsableByteArray.readInt();
      if ((paramInt == SEEK_HEADER_XING) || (paramInt == SEEK_HEADER_INFO)) {
        return paramInt;
      }
    }
    if (paramParsableByteArray.limit() >= 40)
    {
      paramParsableByteArray.setPosition(36);
      if (paramParsableByteArray.readInt() == SEEK_HEADER_VBRI) {
        return SEEK_HEADER_VBRI;
      }
    }
    return 0;
  }
  
  private static boolean headersMatch(int paramInt, long paramLong)
  {
    return (0xFFFE0C00 & paramInt) == (0xFFFFFFFFFFFE0C00 & paramLong);
  }
  
  private Seeker maybeReadSeekFrame(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    int i = 21;
    Object localObject = new ParsableByteArray(this.synchronizedHeader.frameSize);
    paramExtractorInput.peekFully(((ParsableByteArray)localObject).data, 0, this.synchronizedHeader.frameSize);
    if ((this.synchronizedHeader.version & 0x1) != 0) {
      if (this.synchronizedHeader.channels != 1) {
        i = 36;
      }
    }
    int j;
    for (;;)
    {
      j = getSeekFrameHeader((ParsableByteArray)localObject, i);
      if ((j != SEEK_HEADER_XING) && (j != SEEK_HEADER_INFO)) {
        break;
      }
      XingSeeker localXingSeeker = XingSeeker.create(paramExtractorInput.getLength(), paramExtractorInput.getPosition(), this.synchronizedHeader, (ParsableByteArray)localObject);
      if ((localXingSeeker != null) && (!this.gaplessInfoHolder.hasGaplessInfo()))
      {
        paramExtractorInput.resetPeekPosition();
        paramExtractorInput.advancePeekPosition(i + 141);
        paramExtractorInput.peekFully(this.scratch.data, 0, 3);
        this.scratch.setPosition(0);
        this.gaplessInfoHolder.setFromXingHeaderValue(this.scratch.readUnsignedInt24());
      }
      paramExtractorInput.skipFully(this.synchronizedHeader.frameSize);
      localObject = localXingSeeker;
      if (localXingSeeker == null) {
        break label291;
      }
      localObject = localXingSeeker;
      if (localXingSeeker.isSeekable()) {
        break label291;
      }
      localObject = localXingSeeker;
      if (j != SEEK_HEADER_INFO) {
        break label291;
      }
      return getConstantBitrateSeeker(paramExtractorInput);
      if (this.synchronizedHeader.channels == 1) {
        i = 13;
      }
    }
    if (j == SEEK_HEADER_VBRI)
    {
      localObject = VbriSeeker.create(paramExtractorInput.getLength(), paramExtractorInput.getPosition(), this.synchronizedHeader, (ParsableByteArray)localObject);
      paramExtractorInput.skipFully(this.synchronizedHeader.frameSize);
    }
    for (;;)
    {
      label291:
      return (Seeker)localObject;
      localObject = null;
      paramExtractorInput.resetPeekPosition();
    }
  }
  
  private void peekId3Data(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    int i = 0;
    paramExtractorInput.peekFully(this.scratch.data, 0, 10);
    this.scratch.setPosition(0);
    if (this.scratch.readUnsignedInt24() != Id3Decoder.ID3_TAG)
    {
      paramExtractorInput.resetPeekPosition();
      paramExtractorInput.advancePeekPosition(i);
      return;
    }
    this.scratch.skipBytes(3);
    int j = this.scratch.readSynchSafeInt();
    int k = j + 10;
    Id3Decoder.FramePredicate localFramePredicate;
    if (this.metadata == null)
    {
      byte[] arrayOfByte = new byte[k];
      System.arraycopy(this.scratch.data, 0, arrayOfByte, 0, 10);
      paramExtractorInput.peekFully(arrayOfByte, 10, j);
      if ((this.flags & 0x2) != 0)
      {
        localFramePredicate = GaplessInfoHolder.GAPLESS_INFO_ID3_FRAME_PREDICATE;
        label129:
        this.metadata = new Id3Decoder(localFramePredicate).decode(arrayOfByte, k);
        if (this.metadata != null) {
          this.gaplessInfoHolder.setFromMetadata(this.metadata);
        }
      }
    }
    for (;;)
    {
      i += k;
      break;
      localFramePredicate = null;
      break label129;
      paramExtractorInput.advancePeekPosition(j);
    }
  }
  
  private int readSample(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    if (this.sampleBytesRemaining == 0)
    {
      paramExtractorInput.resetPeekPosition();
      if (!paramExtractorInput.peekFully(this.scratch.data, 0, 4, true)) {
        return -1;
      }
      this.scratch.setPosition(0);
      i = this.scratch.readInt();
      if ((!headersMatch(i, this.synchronizedHeaderData)) || (MpegAudioHeader.getFrameSize(i) == -1))
      {
        paramExtractorInput.skipFully(1);
        this.synchronizedHeaderData = 0;
        return 0;
      }
      MpegAudioHeader.populateHeader(i, this.synchronizedHeader);
      if (this.basisTimeUs == -9223372036854775807L)
      {
        this.basisTimeUs = this.seeker.getTimeUs(paramExtractorInput.getPosition());
        if (this.forcedFirstSampleTimestampUs != -9223372036854775807L)
        {
          l1 = this.seeker.getTimeUs(0L);
          this.basisTimeUs += this.forcedFirstSampleTimestampUs - l1;
        }
      }
      this.sampleBytesRemaining = this.synchronizedHeader.frameSize;
    }
    int i = this.trackOutput.sampleData(paramExtractorInput, this.sampleBytesRemaining, true);
    if (i == -1) {
      return -1;
    }
    this.sampleBytesRemaining -= i;
    if (this.sampleBytesRemaining > 0) {
      return 0;
    }
    long l1 = this.basisTimeUs;
    long l2 = this.samplesRead * 1000000L / this.synchronizedHeader.sampleRate;
    this.trackOutput.sampleMetadata(l1 + l2, 1, this.synchronizedHeader.frameSize, 0, null);
    this.samplesRead += this.synchronizedHeader.samplesPerFrame;
    this.sampleBytesRemaining = 0;
    return 0;
  }
  
  private boolean synchronize(ExtractorInput paramExtractorInput, boolean paramBoolean)
    throws IOException, InterruptedException
  {
    int i1 = 0;
    int i2 = 0;
    int n = 0;
    int i3 = 0;
    int m;
    int i;
    int j;
    int k;
    label105:
    boolean bool;
    if (paramBoolean)
    {
      m = 16384;
      paramExtractorInput.resetPeekPosition();
      i = i2;
      j = i3;
      k = i1;
      if (paramExtractorInput.getPosition() == 0L)
      {
        peekId3Data(paramExtractorInput);
        int i4 = (int)paramExtractorInput.getPeekPosition();
        i = i2;
        n = i4;
        j = i3;
        k = i1;
        if (!paramBoolean)
        {
          paramExtractorInput.skipFully(i4);
          k = i1;
          j = i3;
          n = i4;
          i = i2;
        }
      }
      byte[] arrayOfByte = this.scratch.data;
      if (k <= 0) {
        break label166;
      }
      bool = true;
      label122:
      if (paramExtractorInput.peekFully(arrayOfByte, 0, 4, bool)) {
        break label172;
      }
      label137:
      if (!paramBoolean) {
        break label354;
      }
      paramExtractorInput.skipFully(n + j);
    }
    for (;;)
    {
      this.synchronizedHeaderData = i;
      return true;
      m = 131072;
      break;
      label166:
      bool = false;
      break label122;
      label172:
      this.scratch.setPosition(0);
      i2 = this.scratch.readInt();
      if ((i == 0) || (headersMatch(i2, i)))
      {
        i3 = MpegAudioHeader.getFrameSize(i2);
        if (i3 != -1) {}
      }
      else
      {
        i = j + 1;
        if (j == m)
        {
          if (!paramBoolean) {
            throw new ParserException("Searched too many bytes.");
          }
          return false;
        }
        k = 0;
        i1 = 0;
        if (paramBoolean)
        {
          paramExtractorInput.resetPeekPosition();
          paramExtractorInput.advancePeekPosition(n + i);
          j = i;
          i = i1;
          break label105;
        }
        paramExtractorInput.skipFully(1);
        j = i;
        i = i1;
        break label105;
      }
      i1 = k + 1;
      if (i1 == 1)
      {
        MpegAudioHeader.populateHeader(i2, this.synchronizedHeader);
        k = i2;
      }
      do
      {
        paramExtractorInput.advancePeekPosition(i3 - 4);
        i = k;
        k = i1;
        break;
        k = i;
      } while (i1 != 4);
      break label137;
      label354:
      paramExtractorInput.resetPeekPosition();
    }
  }
  
  public void init(ExtractorOutput paramExtractorOutput)
  {
    this.extractorOutput = paramExtractorOutput;
    this.trackOutput = this.extractorOutput.track(0, 1);
    this.extractorOutput.endTracks();
  }
  
  public int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    if (this.synchronizedHeaderData == 0) {}
    for (;;)
    {
      try
      {
        synchronize(paramExtractorInput, false);
        if (this.seeker == null)
        {
          this.seeker = maybeReadSeekFrame(paramExtractorInput);
          if ((this.seeker == null) || ((!this.seeker.isSeekable()) && ((this.flags & 0x1) != 0))) {
            this.seeker = getConstantBitrateSeeker(paramExtractorInput);
          }
          this.extractorOutput.seekMap(this.seeker);
          TrackOutput localTrackOutput = this.trackOutput;
          String str = this.synchronizedHeader.mimeType;
          int i = this.synchronizedHeader.channels;
          int j = this.synchronizedHeader.sampleRate;
          int k = this.gaplessInfoHolder.encoderDelay;
          int m = this.gaplessInfoHolder.encoderPadding;
          if ((this.flags & 0x2) != 0)
          {
            paramPositionHolder = null;
            localTrackOutput.format(Format.createAudioSampleFormat(null, str, null, -1, 4096, i, j, -1, k, m, null, null, 0, null, paramPositionHolder));
          }
        }
        else
        {
          return readSample(paramExtractorInput);
        }
      }
      catch (EOFException paramExtractorInput)
      {
        return -1;
      }
      paramPositionHolder = this.metadata;
    }
  }
  
  public void release() {}
  
  public void seek(long paramLong1, long paramLong2)
  {
    this.synchronizedHeaderData = 0;
    this.basisTimeUs = -9223372036854775807L;
    this.samplesRead = 0L;
    this.sampleBytesRemaining = 0;
  }
  
  public boolean sniff(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    return synchronize(paramExtractorInput, true);
  }
  
  @Retention(RetentionPolicy.SOURCE)
  public static @interface Flags {}
  
  static abstract interface Seeker
    extends SeekMap
  {
    public abstract long getTimeUs(long paramLong);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/mp3/Mp3Extractor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */