package ir.eitaa.helper.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HelperInputStream
{
  public static String convertInputStreamToString(InputStream paramInputStream)
  {
    try
    {
      Object localObject = new BufferedReader(new InputStreamReader(paramInputStream, "UTF-8"), 8192);
      paramInputStream = new StringBuilder();
      localObject = ((BufferedReader)localObject).readLine();
      if (localObject != null)
      {
        paramInputStream.append((String)localObject);
        paramInputStream = paramInputStream.toString();
        return paramInputStream;
      }
    }
    catch (IOException paramInputStream)
    {
      paramInputStream.printStackTrace();
    }
    return null;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/helper/http/HelperInputStream.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */