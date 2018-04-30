package ir.eitaa.messenger.exoplayer.text;

import ir.eitaa.messenger.exoplayer.ParserException;

public abstract interface SubtitleParser
{
  public abstract boolean canParse(String paramString);
  
  public abstract Subtitle parse(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws ParserException;
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/SubtitleParser.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */