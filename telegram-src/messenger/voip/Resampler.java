package org.telegram.messenger.voip;

import java.nio.ByteBuffer;

public class Resampler
{
  public static native int convert44to48(ByteBuffer paramByteBuffer1, ByteBuffer paramByteBuffer2);
  
  public static native int convert48to44(ByteBuffer paramByteBuffer1, ByteBuffer paramByteBuffer2);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/Resampler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */