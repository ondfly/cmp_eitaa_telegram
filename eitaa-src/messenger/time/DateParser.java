package ir.eitaa.messenger.time;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract interface DateParser
{
  public abstract Locale getLocale();
  
  public abstract String getPattern();
  
  public abstract TimeZone getTimeZone();
  
  public abstract Date parse(String paramString)
    throws ParseException;
  
  public abstract Date parse(String paramString, ParsePosition paramParsePosition);
  
  public abstract Object parseObject(String paramString)
    throws ParseException;
  
  public abstract Object parseObject(String paramString, ParsePosition paramParsePosition);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/time/DateParser.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */