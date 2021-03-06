package org.telegram.messenger.exoplayer2.text.cea;

import org.telegram.messenger.exoplayer2.text.SubtitleOutputBuffer;

public final class CeaOutputBuffer
  extends SubtitleOutputBuffer
{
  private final CeaDecoder owner;
  
  public CeaOutputBuffer(CeaDecoder paramCeaDecoder)
  {
    this.owner = paramCeaDecoder;
  }
  
  public final void release()
  {
    this.owner.releaseOutputBuffer(this);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/cea/CeaOutputBuffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */