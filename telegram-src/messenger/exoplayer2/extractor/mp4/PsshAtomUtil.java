package org.telegram.messenger.exoplayer2.extractor.mp4;

import android.util.Log;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;

public final class PsshAtomUtil
{
  private static final String TAG = "PsshAtomUtil";
  
  public static byte[] buildPsshAtom(UUID paramUUID, byte[] paramArrayOfByte)
  {
    return buildPsshAtom(paramUUID, null, paramArrayOfByte);
  }
  
  public static byte[] buildPsshAtom(UUID paramUUID, UUID[] paramArrayOfUUID, byte[] paramArrayOfByte)
  {
    int m = 0;
    int j;
    int i;
    label17:
    ByteBuffer localByteBuffer;
    if (paramArrayOfUUID != null)
    {
      j = 1;
      if (paramArrayOfByte == null) {
        break label173;
      }
      i = paramArrayOfByte.length;
      int n = i + 32;
      k = n;
      if (j != 0) {
        k = n + (paramArrayOfUUID.length * 16 + 4);
      }
      localByteBuffer = ByteBuffer.allocate(k);
      localByteBuffer.putInt(k);
      localByteBuffer.putInt(Atom.TYPE_pssh);
      if (j == 0) {
        break label178;
      }
    }
    label173:
    label178:
    for (int k = 16777216;; k = 0)
    {
      localByteBuffer.putInt(k);
      localByteBuffer.putLong(paramUUID.getMostSignificantBits());
      localByteBuffer.putLong(paramUUID.getLeastSignificantBits());
      if (j == 0) {
        break label184;
      }
      localByteBuffer.putInt(paramArrayOfUUID.length);
      k = paramArrayOfUUID.length;
      j = m;
      while (j < k)
      {
        paramUUID = paramArrayOfUUID[j];
        localByteBuffer.putLong(paramUUID.getMostSignificantBits());
        localByteBuffer.putLong(paramUUID.getLeastSignificantBits());
        j += 1;
      }
      j = 0;
      break;
      i = 0;
      break label17;
    }
    label184:
    if (i != 0)
    {
      localByteBuffer.putInt(paramArrayOfByte.length);
      localByteBuffer.put(paramArrayOfByte);
    }
    return localByteBuffer.array();
  }
  
  private static PsshAtom parsePsshAtom(byte[] paramArrayOfByte)
  {
    paramArrayOfByte = new ParsableByteArray(paramArrayOfByte);
    if (paramArrayOfByte.limit() < 32) {
      return null;
    }
    paramArrayOfByte.setPosition(0);
    if (paramArrayOfByte.readInt() != paramArrayOfByte.bytesLeft() + 4) {
      return null;
    }
    if (paramArrayOfByte.readInt() != Atom.TYPE_pssh) {
      return null;
    }
    int i = Atom.parseFullAtomVersion(paramArrayOfByte.readInt());
    if (i > 1)
    {
      Log.w("PsshAtomUtil", "Unsupported pssh version: " + i);
      return null;
    }
    UUID localUUID = new UUID(paramArrayOfByte.readLong(), paramArrayOfByte.readLong());
    if (i == 1) {
      paramArrayOfByte.skipBytes(paramArrayOfByte.readUnsignedIntToInt() * 16);
    }
    int j = paramArrayOfByte.readUnsignedIntToInt();
    if (j != paramArrayOfByte.bytesLeft()) {
      return null;
    }
    byte[] arrayOfByte = new byte[j];
    paramArrayOfByte.readBytes(arrayOfByte, 0, j);
    return new PsshAtom(localUUID, i, arrayOfByte);
  }
  
  public static byte[] parseSchemeSpecificData(byte[] paramArrayOfByte, UUID paramUUID)
  {
    paramArrayOfByte = parsePsshAtom(paramArrayOfByte);
    if (paramArrayOfByte == null) {
      return null;
    }
    if ((paramUUID != null) && (!paramUUID.equals(paramArrayOfByte.uuid)))
    {
      Log.w("PsshAtomUtil", "UUID mismatch. Expected: " + paramUUID + ", got: " + paramArrayOfByte.uuid + ".");
      return null;
    }
    return paramArrayOfByte.schemeData;
  }
  
  public static UUID parseUuid(byte[] paramArrayOfByte)
  {
    paramArrayOfByte = parsePsshAtom(paramArrayOfByte);
    if (paramArrayOfByte == null) {
      return null;
    }
    return paramArrayOfByte.uuid;
  }
  
  public static int parseVersion(byte[] paramArrayOfByte)
  {
    paramArrayOfByte = parsePsshAtom(paramArrayOfByte);
    if (paramArrayOfByte == null) {
      return -1;
    }
    return paramArrayOfByte.version;
  }
  
  private static class PsshAtom
  {
    private final byte[] schemeData;
    private final UUID uuid;
    private final int version;
    
    public PsshAtom(UUID paramUUID, int paramInt, byte[] paramArrayOfByte)
    {
      this.uuid = paramUUID;
      this.version = paramInt;
      this.schemeData = paramArrayOfByte;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/mp4/PsshAtomUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */