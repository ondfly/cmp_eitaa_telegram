package org.telegram.messenger.exoplayer2.metadata;

import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.metadata.emsg.EventMessageDecoder;
import org.telegram.messenger.exoplayer2.metadata.id3.Id3Decoder;
import org.telegram.messenger.exoplayer2.metadata.scte35.SpliceInfoDecoder;

public abstract interface MetadataDecoderFactory
{
  public static final MetadataDecoderFactory DEFAULT = new MetadataDecoderFactory()
  {
    public MetadataDecoder createDecoder(Format paramAnonymousFormat)
    {
      paramAnonymousFormat = paramAnonymousFormat.sampleMimeType;
      int i = -1;
      switch (paramAnonymousFormat.hashCode())
      {
      }
      for (;;)
      {
        switch (i)
        {
        default: 
          throw new IllegalArgumentException("Attempted to create decoder for unsupported format");
          if (paramAnonymousFormat.equals("application/id3"))
          {
            i = 0;
            continue;
            if (paramAnonymousFormat.equals("application/x-emsg"))
            {
              i = 1;
              continue;
              if (paramAnonymousFormat.equals("application/x-scte35")) {
                i = 2;
              }
            }
          }
          break;
        }
      }
      return new Id3Decoder();
      return new EventMessageDecoder();
      return new SpliceInfoDecoder();
    }
    
    public boolean supportsFormat(Format paramAnonymousFormat)
    {
      paramAnonymousFormat = paramAnonymousFormat.sampleMimeType;
      return ("application/id3".equals(paramAnonymousFormat)) || ("application/x-emsg".equals(paramAnonymousFormat)) || ("application/x-scte35".equals(paramAnonymousFormat));
    }
  };
  
  public abstract MetadataDecoder createDecoder(Format paramFormat);
  
  public abstract boolean supportsFormat(Format paramFormat);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/metadata/MetadataDecoderFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */