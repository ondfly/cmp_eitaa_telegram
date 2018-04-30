package org.telegram.messenger.exoplayer2.extractor.mp3;

import org.telegram.messenger.exoplayer2.extractor.MpegAudioHeader;
import org.telegram.messenger.exoplayer2.extractor.SeekMap.SeekPoints;
import org.telegram.messenger.exoplayer2.extractor.SeekPoint;
import org.telegram.messenger.exoplayer2.util.Util;

final class ConstantBitrateSeeker
  implements Mp3Extractor.Seeker
{
  private static final int BITS_PER_BYTE = 8;
  private final int bitrate;
  private final long dataSize;
  private final long durationUs;
  private final long firstFramePosition;
  private final int frameSize;
  
  public ConstantBitrateSeeker(long paramLong1, long paramLong2, MpegAudioHeader paramMpegAudioHeader)
  {
    this.firstFramePosition = paramLong2;
    this.frameSize = paramMpegAudioHeader.frameSize;
    this.bitrate = paramMpegAudioHeader.bitrate;
    if (paramLong1 == -1L)
    {
      this.dataSize = -1L;
      this.durationUs = -9223372036854775807L;
      return;
    }
    this.dataSize = (paramLong1 - paramLong2);
    this.durationUs = getTimeUs(paramLong1);
  }
  
  public long getDurationUs()
  {
    return this.durationUs;
  }
  
  public SeekMap.SeekPoints getSeekPoints(long paramLong)
  {
    if (this.dataSize == -1L) {
      return new SeekMap.SeekPoints(new SeekPoint(0L, this.firstFramePosition));
    }
    long l1 = Util.constrainValue(this.bitrate * paramLong / 8000000L / this.frameSize * this.frameSize, 0L, this.dataSize - this.frameSize);
    long l2 = this.firstFramePosition + l1;
    long l3 = getTimeUs(l2);
    SeekPoint localSeekPoint = new SeekPoint(l3, l2);
    if ((l3 >= paramLong) || (l1 == this.dataSize - this.frameSize)) {
      return new SeekMap.SeekPoints(localSeekPoint);
    }
    paramLong = l2 + this.frameSize;
    return new SeekMap.SeekPoints(localSeekPoint, new SeekPoint(getTimeUs(paramLong), paramLong));
  }
  
  public long getTimeUs(long paramLong)
  {
    return Math.max(0L, paramLong - this.firstFramePosition) * 1000000L * 8L / this.bitrate;
  }
  
  public boolean isSeekable()
  {
    return this.dataSize != -1L;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/mp3/ConstantBitrateSeeker.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */