package ir.eitaa.messenger.exoplayer;

import java.io.IOException;

public class ParserException
  extends IOException
{
  public ParserException() {}
  
  public ParserException(String paramString)
  {
    super(paramString);
  }
  
  public ParserException(String paramString, Throwable paramThrowable)
  {
    super(paramString, paramThrowable);
  }
  
  public ParserException(Throwable paramThrowable)
  {
    super(paramThrowable);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/ParserException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */