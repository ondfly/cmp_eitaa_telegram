package org.telegram.messenger.exoplayer2.text;

import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.text.cea.Cea608Decoder;
import org.telegram.messenger.exoplayer2.text.cea.Cea708Decoder;
import org.telegram.messenger.exoplayer2.text.dvb.DvbDecoder;
import org.telegram.messenger.exoplayer2.text.pgs.PgsDecoder;
import org.telegram.messenger.exoplayer2.text.ssa.SsaDecoder;
import org.telegram.messenger.exoplayer2.text.subrip.SubripDecoder;
import org.telegram.messenger.exoplayer2.text.ttml.TtmlDecoder;
import org.telegram.messenger.exoplayer2.text.tx3g.Tx3gDecoder;
import org.telegram.messenger.exoplayer2.text.webvtt.Mp4WebvttDecoder;
import org.telegram.messenger.exoplayer2.text.webvtt.WebvttDecoder;

public abstract interface SubtitleDecoderFactory
{
  public static final SubtitleDecoderFactory DEFAULT = new SubtitleDecoderFactory()
  {
    public SubtitleDecoder createDecoder(Format paramAnonymousFormat)
    {
      String str = paramAnonymousFormat.sampleMimeType;
      int i = -1;
      switch (str.hashCode())
      {
      }
      for (;;)
      {
        switch (i)
        {
        default: 
          throw new IllegalArgumentException("Attempted to create decoder for unsupported format");
          if (str.equals("text/vtt"))
          {
            i = 0;
            continue;
            if (str.equals("text/x-ssa"))
            {
              i = 1;
              continue;
              if (str.equals("application/x-mp4-vtt"))
              {
                i = 2;
                continue;
                if (str.equals("application/ttml+xml"))
                {
                  i = 3;
                  continue;
                  if (str.equals("application/x-subrip"))
                  {
                    i = 4;
                    continue;
                    if (str.equals("application/x-quicktime-tx3g"))
                    {
                      i = 5;
                      continue;
                      if (str.equals("application/cea-608"))
                      {
                        i = 6;
                        continue;
                        if (str.equals("application/x-mp4-cea-608"))
                        {
                          i = 7;
                          continue;
                          if (str.equals("application/cea-708"))
                          {
                            i = 8;
                            continue;
                            if (str.equals("application/dvbsubs"))
                            {
                              i = 9;
                              continue;
                              if (str.equals("application/pgs")) {
                                i = 10;
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
      return new WebvttDecoder();
      return new SsaDecoder(paramAnonymousFormat.initializationData);
      return new Mp4WebvttDecoder();
      return new TtmlDecoder();
      return new SubripDecoder();
      return new Tx3gDecoder(paramAnonymousFormat.initializationData);
      return new Cea608Decoder(paramAnonymousFormat.sampleMimeType, paramAnonymousFormat.accessibilityChannel);
      return new Cea708Decoder(paramAnonymousFormat.accessibilityChannel);
      return new DvbDecoder(paramAnonymousFormat.initializationData);
      return new PgsDecoder();
    }
    
    public boolean supportsFormat(Format paramAnonymousFormat)
    {
      paramAnonymousFormat = paramAnonymousFormat.sampleMimeType;
      return ("text/vtt".equals(paramAnonymousFormat)) || ("text/x-ssa".equals(paramAnonymousFormat)) || ("application/ttml+xml".equals(paramAnonymousFormat)) || ("application/x-mp4-vtt".equals(paramAnonymousFormat)) || ("application/x-subrip".equals(paramAnonymousFormat)) || ("application/x-quicktime-tx3g".equals(paramAnonymousFormat)) || ("application/cea-608".equals(paramAnonymousFormat)) || ("application/x-mp4-cea-608".equals(paramAnonymousFormat)) || ("application/cea-708".equals(paramAnonymousFormat)) || ("application/dvbsubs".equals(paramAnonymousFormat)) || ("application/pgs".equals(paramAnonymousFormat));
    }
  };
  
  public abstract SubtitleDecoder createDecoder(Format paramFormat);
  
  public abstract boolean supportsFormat(Format paramFormat);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/SubtitleDecoderFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */