package org.telegram.messenger.exoplayer2.text;

import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;

public final class SubtitleInputBuffer
  extends DecoderInputBuffer
  implements Comparable<SubtitleInputBuffer>
{
  public long subsampleOffsetUs;
  
  public SubtitleInputBuffer()
  {
    super(1);
  }
  
  public int compareTo(SubtitleInputBuffer paramSubtitleInputBuffer)
  {
    if (isEndOfStream() != paramSubtitleInputBuffer.isEndOfStream()) {
      if (!isEndOfStream()) {}
    }
    long l;
    do
    {
      return 1;
      return -1;
      l = this.timeUs - paramSubtitleInputBuffer.timeUs;
      if (l == 0L) {
        return 0;
      }
    } while (l > 0L);
    return -1;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/SubtitleInputBuffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */