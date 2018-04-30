package org.telegram.messenger.exoplayer2.text.cea;

import android.util.Log;
import org.telegram.messenger.exoplayer2.extractor.TrackOutput;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;

public final class CeaUtil
{
  private static final int COUNTRY_CODE = 181;
  private static final int PAYLOAD_TYPE_CC = 4;
  private static final int PROVIDER_CODE = 49;
  private static final String TAG = "CeaUtil";
  private static final int USER_DATA_TYPE_CODE = 3;
  private static final int USER_ID = 1195456820;
  
  public static void consume(long paramLong, ParsableByteArray paramParsableByteArray, TrackOutput[] paramArrayOfTrackOutput)
  {
    while (paramParsableByteArray.bytesLeft() > 1)
    {
      int i = readNon255TerminatedValue(paramParsableByteArray);
      int j = readNon255TerminatedValue(paramParsableByteArray);
      if ((j == -1) || (j > paramParsableByteArray.bytesLeft()))
      {
        Log.w("CeaUtil", "Skipping remainder of malformed SEI NAL unit.");
        paramParsableByteArray.setPosition(paramParsableByteArray.limit());
      }
      else if (isSeiMessageCea608(i, j, paramParsableByteArray))
      {
        paramParsableByteArray.skipBytes(8);
        int k = paramParsableByteArray.readUnsignedByte() & 0x1F;
        paramParsableByteArray.skipBytes(1);
        int m = k * 3;
        int n = paramParsableByteArray.getPosition();
        int i1 = paramArrayOfTrackOutput.length;
        i = 0;
        while (i < i1)
        {
          TrackOutput localTrackOutput = paramArrayOfTrackOutput[i];
          paramParsableByteArray.setPosition(n);
          localTrackOutput.sampleData(paramParsableByteArray, m);
          localTrackOutput.sampleMetadata(paramLong, 1, m, 0, null);
          i += 1;
        }
        paramParsableByteArray.skipBytes(j - (k * 3 + 10));
      }
      else
      {
        paramParsableByteArray.skipBytes(j);
      }
    }
  }
  
  private static boolean isSeiMessageCea608(int paramInt1, int paramInt2, ParsableByteArray paramParsableByteArray)
  {
    if ((paramInt1 != 4) || (paramInt2 < 8)) {}
    int i;
    int j;
    int k;
    do
    {
      return false;
      paramInt1 = paramParsableByteArray.getPosition();
      paramInt2 = paramParsableByteArray.readUnsignedByte();
      i = paramParsableByteArray.readUnsignedShort();
      j = paramParsableByteArray.readInt();
      k = paramParsableByteArray.readUnsignedByte();
      paramParsableByteArray.setPosition(paramInt1);
    } while ((paramInt2 != 181) || (i != 49) || (j != 1195456820) || (k != 3));
    return true;
  }
  
  private static int readNon255TerminatedValue(ParsableByteArray paramParsableByteArray)
  {
    int i = 0;
    int k;
    int j;
    do
    {
      if (paramParsableByteArray.bytesLeft() == 0) {
        return -1;
      }
      k = paramParsableByteArray.readUnsignedByte();
      j = i + k;
      i = j;
    } while (k == 255);
    return j;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/cea/CeaUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */