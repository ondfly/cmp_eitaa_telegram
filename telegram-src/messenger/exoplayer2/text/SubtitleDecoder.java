package org.telegram.messenger.exoplayer2.text;

import org.telegram.messenger.exoplayer2.decoder.Decoder;

public abstract interface SubtitleDecoder
  extends Decoder<SubtitleInputBuffer, SubtitleOutputBuffer, SubtitleDecoderException>
{
  public abstract void setPositionUs(long paramLong);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/SubtitleDecoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */