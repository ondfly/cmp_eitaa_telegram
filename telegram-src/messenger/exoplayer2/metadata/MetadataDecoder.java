package org.telegram.messenger.exoplayer2.metadata;

public abstract interface MetadataDecoder
{
  public abstract Metadata decode(MetadataInputBuffer paramMetadataInputBuffer)
    throws MetadataDecoderException;
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/metadata/MetadataDecoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */