package ir.eitaa.messenger.exoplayer.text.webvtt;

import ir.eitaa.messenger.exoplayer.ParserException;
import ir.eitaa.messenger.exoplayer.util.ParsableByteArray;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebvttParserUtil
{
  private static final Pattern HEADER = Pattern.compile("^﻿?WEBVTT(( |\t).*)?$");
  
  public static float parsePercentage(String paramString)
    throws NumberFormatException
  {
    if (!paramString.endsWith("%")) {
      throw new NumberFormatException("Percentages must end with %");
    }
    return Float.parseFloat(paramString.substring(0, paramString.length() - 1)) / 100.0F;
  }
  
  public static long parseTimestampUs(String paramString)
    throws NumberFormatException
  {
    long l = 0L;
    paramString = paramString.split("\\.", 2);
    String[] arrayOfString = paramString[0].split(":");
    int i = 0;
    while (i < arrayOfString.length)
    {
      l = 60L * l + Long.parseLong(arrayOfString[i]);
      i += 1;
    }
    return (l * 1000L + Long.parseLong(paramString[1])) * 1000L;
  }
  
  public static void validateWebvttHeaderLine(ParsableByteArray paramParsableByteArray)
    throws ParserException
  {
    paramParsableByteArray = paramParsableByteArray.readLine();
    if ((paramParsableByteArray == null) || (!HEADER.matcher(paramParsableByteArray).matches())) {
      throw new ParserException("Expected WEBVTT. Got " + paramParsableByteArray);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/webvtt/WebvttParserUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */