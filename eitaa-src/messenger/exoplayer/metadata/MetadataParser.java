package ir.eitaa.messenger.exoplayer.metadata;

import ir.eitaa.messenger.exoplayer.ParserException;

public abstract interface MetadataParser<T>
{
  public abstract boolean canParse(String paramString);
  
  public abstract T parse(byte[] paramArrayOfByte, int paramInt)
    throws ParserException;
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/metadata/MetadataParser.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */