package org.telegram.messenger.exoplayer2.extractor.mp4;

import java.io.IOException;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.Util;

final class Sniffer
{
  private static final int[] COMPATIBLE_BRANDS = { Util.getIntegerCodeForString("isom"), Util.getIntegerCodeForString("iso2"), Util.getIntegerCodeForString("iso3"), Util.getIntegerCodeForString("iso4"), Util.getIntegerCodeForString("iso5"), Util.getIntegerCodeForString("iso6"), Util.getIntegerCodeForString("avc1"), Util.getIntegerCodeForString("hvc1"), Util.getIntegerCodeForString("hev1"), Util.getIntegerCodeForString("mp41"), Util.getIntegerCodeForString("mp42"), Util.getIntegerCodeForString("3g2a"), Util.getIntegerCodeForString("3g2b"), Util.getIntegerCodeForString("3gr6"), Util.getIntegerCodeForString("3gs6"), Util.getIntegerCodeForString("3ge6"), Util.getIntegerCodeForString("3gg6"), Util.getIntegerCodeForString("M4V "), Util.getIntegerCodeForString("M4A "), Util.getIntegerCodeForString("f4v "), Util.getIntegerCodeForString("kddi"), Util.getIntegerCodeForString("M4VP"), Util.getIntegerCodeForString("qt  "), Util.getIntegerCodeForString("MSNV") };
  private static final int SEARCH_LENGTH = 4096;
  
  private static boolean isCompatibleBrand(int paramInt)
  {
    if (paramInt >>> 8 == Util.getIntegerCodeForString("3gp")) {
      return true;
    }
    int[] arrayOfInt = COMPATIBLE_BRANDS;
    int j = arrayOfInt.length;
    int i = 0;
    for (;;)
    {
      if (i >= j) {
        break label42;
      }
      if (arrayOfInt[i] == paramInt) {
        break;
      }
      i += 1;
    }
    label42:
    return false;
  }
  
  public static boolean sniffFragmented(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    return sniffInternal(paramExtractorInput, true);
  }
  
  private static boolean sniffInternal(ExtractorInput paramExtractorInput, boolean paramBoolean)
    throws IOException, InterruptedException
  {
    long l2 = paramExtractorInput.getLength();
    long l1;
    if (l2 != -1L)
    {
      l1 = l2;
      if (l2 <= 4096L) {}
    }
    else
    {
      l1 = 4096L;
    }
    int n = (int)l1;
    ParsableByteArray localParsableByteArray = new ParsableByteArray(64);
    int i = 0;
    int j = 0;
    boolean bool2 = false;
    for (;;)
    {
      boolean bool1 = bool2;
      int m;
      int i1;
      if (i < n)
      {
        m = 8;
        localParsableByteArray.reset(8);
        paramExtractorInput.peekFully(localParsableByteArray.data, 0, 8);
        l2 = localParsableByteArray.readUnsignedInt();
        i1 = localParsableByteArray.readInt();
        if (l2 == 1L)
        {
          k = 16;
          paramExtractorInput.peekFully(localParsableByteArray.data, 8, 8);
          localParsableByteArray.setLimit(16);
          l1 = localParsableByteArray.readUnsignedLongToLong();
        }
        while (l1 < k)
        {
          return false;
          l1 = l2;
          k = m;
          if (l2 == 0L)
          {
            long l3 = paramExtractorInput.getLength();
            l1 = l2;
            k = m;
            if (l3 != -1L)
            {
              l1 = l3 - paramExtractorInput.getPosition() + 8;
              k = m;
            }
          }
        }
        m = i + k;
        i = m;
        if (i1 == Atom.TYPE_moov) {
          continue;
        }
        if ((i1 != Atom.TYPE_moof) && (i1 != Atom.TYPE_mvex)) {
          break label268;
        }
        bool1 = true;
      }
      label268:
      do
      {
        if ((j == 0) || (paramBoolean != bool1)) {
          break;
        }
        return true;
        bool1 = bool2;
      } while (m + l1 - k >= n);
      int k = (int)(l1 - k);
      m += k;
      if (i1 == Atom.TYPE_ftyp)
      {
        if (k < 8) {
          return false;
        }
        localParsableByteArray.reset(k);
        paramExtractorInput.peekFully(localParsableByteArray.data, 0, k);
        i1 = k / 4;
        i = 0;
        k = j;
        if (i < i1)
        {
          if (i == 1) {
            localParsableByteArray.skipBytes(4);
          }
          while (!isCompatibleBrand(localParsableByteArray.readInt()))
          {
            i += 1;
            break;
          }
          k = 1;
        }
        i = m;
        j = k;
        if (k == 0) {
          return false;
        }
      }
      else
      {
        i = m;
        if (k != 0)
        {
          paramExtractorInput.advancePeekPosition(k);
          i = m;
        }
      }
    }
    return false;
  }
  
  public static boolean sniffUnfragmented(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    return sniffInternal(paramExtractorInput, false);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/mp4/Sniffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */