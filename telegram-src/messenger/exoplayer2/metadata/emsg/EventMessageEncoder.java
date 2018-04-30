package org.telegram.messenger.exoplayer2.metadata.emsg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.Util;

public final class EventMessageEncoder
{
  private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(512);
  private final DataOutputStream dataOutputStream = new DataOutputStream(this.byteArrayOutputStream);
  
  private static void writeNullTerminatedString(DataOutputStream paramDataOutputStream, String paramString)
    throws IOException
  {
    paramDataOutputStream.writeBytes(paramString);
    paramDataOutputStream.writeByte(0);
  }
  
  private static void writeUnsignedInt(DataOutputStream paramDataOutputStream, long paramLong)
    throws IOException
  {
    paramDataOutputStream.writeByte((int)(paramLong >>> 24) & 0xFF);
    paramDataOutputStream.writeByte((int)(paramLong >>> 16) & 0xFF);
    paramDataOutputStream.writeByte((int)(paramLong >>> 8) & 0xFF);
    paramDataOutputStream.writeByte((int)paramLong & 0xFF);
  }
  
  public byte[] encode(EventMessage paramEventMessage, long paramLong)
  {
    boolean bool;
    if (paramLong >= 0L) {
      bool = true;
    }
    for (;;)
    {
      Assertions.checkArgument(bool);
      this.byteArrayOutputStream.reset();
      try
      {
        writeNullTerminatedString(this.dataOutputStream, paramEventMessage.schemeIdUri);
        if (paramEventMessage.value != null) {}
        for (String str = paramEventMessage.value;; str = "")
        {
          writeNullTerminatedString(this.dataOutputStream, str);
          writeUnsignedInt(this.dataOutputStream, paramLong);
          long l = Util.scaleLargeTimestamp(paramEventMessage.presentationTimeUs, paramLong, 1000000L);
          writeUnsignedInt(this.dataOutputStream, l);
          paramLong = Util.scaleLargeTimestamp(paramEventMessage.durationMs, paramLong, 1000L);
          writeUnsignedInt(this.dataOutputStream, paramLong);
          writeUnsignedInt(this.dataOutputStream, paramEventMessage.id);
          this.dataOutputStream.write(paramEventMessage.messageData);
          this.dataOutputStream.flush();
          return this.byteArrayOutputStream.toByteArray();
        }
        bool = false;
      }
      catch (IOException paramEventMessage)
      {
        throw new RuntimeException(paramEventMessage);
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/metadata/emsg/EventMessageEncoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */