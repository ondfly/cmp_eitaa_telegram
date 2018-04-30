package org.telegram.messenger.exoplayer2.source;

import android.net.Uri;
import org.telegram.messenger.exoplayer2.ParserException;

public class UnrecognizedInputFormatException
  extends ParserException
{
  public final Uri uri;
  
  public UnrecognizedInputFormatException(String paramString, Uri paramUri)
  {
    super(paramString);
    this.uri = paramUri;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/UnrecognizedInputFormatException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */