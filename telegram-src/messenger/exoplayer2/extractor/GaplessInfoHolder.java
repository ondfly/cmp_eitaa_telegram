package org.telegram.messenger.exoplayer2.extractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.messenger.exoplayer2.metadata.Metadata;
import org.telegram.messenger.exoplayer2.metadata.id3.CommentFrame;
import org.telegram.messenger.exoplayer2.metadata.id3.Id3Decoder.FramePredicate;

public final class GaplessInfoHolder
{
  private static final String GAPLESS_COMMENT_ID = "iTunSMPB";
  private static final Pattern GAPLESS_COMMENT_PATTERN = Pattern.compile("^ [0-9a-fA-F]{8} ([0-9a-fA-F]{8}) ([0-9a-fA-F]{8})");
  public static final Id3Decoder.FramePredicate GAPLESS_INFO_ID3_FRAME_PREDICATE = new Id3Decoder.FramePredicate()
  {
    public boolean evaluate(int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4, int paramAnonymousInt5)
    {
      return (paramAnonymousInt2 == 67) && (paramAnonymousInt3 == 79) && (paramAnonymousInt4 == 77) && ((paramAnonymousInt5 == 77) || (paramAnonymousInt1 == 2));
    }
  };
  public int encoderDelay = -1;
  public int encoderPadding = -1;
  
  private boolean setFromComment(String paramString1, String paramString2)
  {
    if (!"iTunSMPB".equals(paramString1)) {}
    for (;;)
    {
      return false;
      paramString1 = GAPLESS_COMMENT_PATTERN.matcher(paramString2);
      if (paramString1.find()) {
        try
        {
          int i = Integer.parseInt(paramString1.group(1), 16);
          int j = Integer.parseInt(paramString1.group(2), 16);
          if ((i > 0) || (j > 0))
          {
            this.encoderDelay = i;
            this.encoderPadding = j;
            return true;
          }
        }
        catch (NumberFormatException paramString1) {}
      }
    }
    return false;
  }
  
  public boolean hasGaplessInfo()
  {
    return (this.encoderDelay != -1) && (this.encoderPadding != -1);
  }
  
  public boolean setFromMetadata(Metadata paramMetadata)
  {
    int i = 0;
    while (i < paramMetadata.length())
    {
      Object localObject = paramMetadata.get(i);
      if ((localObject instanceof CommentFrame))
      {
        localObject = (CommentFrame)localObject;
        if (setFromComment(((CommentFrame)localObject).description, ((CommentFrame)localObject).text)) {
          return true;
        }
      }
      i += 1;
    }
    return false;
  }
  
  public boolean setFromXingHeaderValue(int paramInt)
  {
    int i = paramInt >> 12;
    paramInt &= 0xFFF;
    if ((i > 0) || (paramInt > 0))
    {
      this.encoderDelay = i;
      this.encoderPadding = paramInt;
      return true;
    }
    return false;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/GaplessInfoHolder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */