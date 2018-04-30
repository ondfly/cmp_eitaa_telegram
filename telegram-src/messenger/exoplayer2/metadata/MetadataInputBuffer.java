package org.telegram.messenger.exoplayer2.metadata;

import org.telegram.messenger.exoplayer2.decoder.DecoderInputBuffer;

public final class MetadataInputBuffer
  extends DecoderInputBuffer
{
  public long subsampleOffsetUs;
  
  public MetadataInputBuffer()
  {
    super(1);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/metadata/MetadataInputBuffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */