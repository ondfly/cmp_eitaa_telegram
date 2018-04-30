package org.telegram.messenger.exoplayer2.source.hls;

import java.io.IOException;

public final class SampleQueueMappingException
  extends IOException
{
  public SampleQueueMappingException(String paramString)
  {
    super("Unable to bind a sample queue to TrackGroup with mime type " + paramString + ".");
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/SampleQueueMappingException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */