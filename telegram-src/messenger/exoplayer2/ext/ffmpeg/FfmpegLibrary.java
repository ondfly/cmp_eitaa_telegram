package org.telegram.messenger.exoplayer2.ext.ffmpeg;

import org.telegram.messenger.exoplayer2.ExoPlayerLibraryInfo;

public final class FfmpegLibrary
{
  static
  {
    ExoPlayerLibraryInfo.registerModule("goog.exo.ffmpeg");
  }
  
  private static native String ffmpegGetVersion();
  
  private static native boolean ffmpegHasDecoder(String paramString);
  
  static String getCodecName(String paramString)
  {
    int i = -1;
    switch (paramString.hashCode())
    {
    }
    for (;;)
    {
      switch (i)
      {
      default: 
        return null;
        if (paramString.equals("audio/mp4a-latm"))
        {
          i = 0;
          continue;
          if (paramString.equals("audio/mpeg"))
          {
            i = 1;
            continue;
            if (paramString.equals("audio/mpeg-L1"))
            {
              i = 2;
              continue;
              if (paramString.equals("audio/mpeg-L2"))
              {
                i = 3;
                continue;
                if (paramString.equals("audio/ac3"))
                {
                  i = 4;
                  continue;
                  if (paramString.equals("audio/eac3"))
                  {
                    i = 5;
                    continue;
                    if (paramString.equals("audio/true-hd"))
                    {
                      i = 6;
                      continue;
                      if (paramString.equals("audio/vnd.dts"))
                      {
                        i = 7;
                        continue;
                        if (paramString.equals("audio/vnd.dts.hd"))
                        {
                          i = 8;
                          continue;
                          if (paramString.equals("audio/vorbis"))
                          {
                            i = 9;
                            continue;
                            if (paramString.equals("audio/opus"))
                            {
                              i = 10;
                              continue;
                              if (paramString.equals("audio/3gpp"))
                              {
                                i = 11;
                                continue;
                                if (paramString.equals("audio/amr-wb"))
                                {
                                  i = 12;
                                  continue;
                                  if (paramString.equals("audio/flac"))
                                  {
                                    i = 13;
                                    continue;
                                    if (paramString.equals("audio/alac")) {
                                      i = 14;
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        break;
      }
    }
    return "aac";
    return "mp3";
    return "ac3";
    return "eac3";
    return "truehd";
    return "dca";
    return "vorbis";
    return "opus";
    return "amrnb";
    return "amrwb";
    return "flac";
    return "alac";
  }
  
  public static String getVersion()
  {
    return ffmpegGetVersion();
  }
  
  public static boolean supportsFormat(String paramString)
  {
    paramString = getCodecName(paramString);
    return (paramString != null) && (ffmpegHasDecoder(paramString));
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/ext/ffmpeg/FfmpegLibrary.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */