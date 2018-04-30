package org.telegram.messenger.exoplayer2.text.dvb;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region.Op;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.telegram.messenger.exoplayer2.text.Cue;
import org.telegram.messenger.exoplayer2.util.ParsableBitArray;
import org.telegram.messenger.exoplayer2.util.Util;

final class DvbParser
{
  private static final int DATA_TYPE_24_TABLE_DATA = 32;
  private static final int DATA_TYPE_28_TABLE_DATA = 33;
  private static final int DATA_TYPE_2BP_CODE_STRING = 16;
  private static final int DATA_TYPE_48_TABLE_DATA = 34;
  private static final int DATA_TYPE_4BP_CODE_STRING = 17;
  private static final int DATA_TYPE_8BP_CODE_STRING = 18;
  private static final int DATA_TYPE_END_LINE = 240;
  private static final int OBJECT_CODING_PIXELS = 0;
  private static final int OBJECT_CODING_STRING = 1;
  private static final int PAGE_STATE_NORMAL = 0;
  private static final int REGION_DEPTH_4_BIT = 2;
  private static final int REGION_DEPTH_8_BIT = 3;
  private static final int SEGMENT_TYPE_CLUT_DEFINITION = 18;
  private static final int SEGMENT_TYPE_DISPLAY_DEFINITION = 20;
  private static final int SEGMENT_TYPE_OBJECT_DATA = 19;
  private static final int SEGMENT_TYPE_PAGE_COMPOSITION = 16;
  private static final int SEGMENT_TYPE_REGION_COMPOSITION = 17;
  private static final String TAG = "DvbParser";
  private static final byte[] defaultMap2To4 = { 0, 7, 8, 15 };
  private static final byte[] defaultMap2To8 = { 0, 119, -120, -1 };
  private static final byte[] defaultMap4To8 = { 0, 17, 34, 51, 68, 85, 102, 119, -120, -103, -86, -69, -52, -35, -18, -1 };
  private Bitmap bitmap;
  private final Canvas canvas;
  private final ClutDefinition defaultClutDefinition;
  private final DisplayDefinition defaultDisplayDefinition;
  private final Paint defaultPaint = new Paint();
  private final Paint fillRegionPaint;
  private final SubtitleService subtitleService;
  
  public DvbParser(int paramInt1, int paramInt2)
  {
    this.defaultPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    this.defaultPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    this.defaultPaint.setPathEffect(null);
    this.fillRegionPaint = new Paint();
    this.fillRegionPaint.setStyle(Paint.Style.FILL);
    this.fillRegionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
    this.fillRegionPaint.setPathEffect(null);
    this.canvas = new Canvas();
    this.defaultDisplayDefinition = new DisplayDefinition(719, 575, 0, 719, 0, 575);
    this.defaultClutDefinition = new ClutDefinition(0, generateDefault2BitClutEntries(), generateDefault4BitClutEntries(), generateDefault8BitClutEntries());
    this.subtitleService = new SubtitleService(paramInt1, paramInt2);
  }
  
  private static byte[] buildClutMapTable(int paramInt1, int paramInt2, ParsableBitArray paramParsableBitArray)
  {
    byte[] arrayOfByte = new byte[paramInt1];
    int i = 0;
    while (i < paramInt1)
    {
      arrayOfByte[i] = ((byte)paramParsableBitArray.readBits(paramInt2));
      i += 1;
    }
    return arrayOfByte;
  }
  
  private static int[] generateDefault2BitClutEntries()
  {
    return new int[] { 0, -1, -16777216, -8421505 };
  }
  
  private static int[] generateDefault4BitClutEntries()
  {
    int[] arrayOfInt = new int[16];
    arrayOfInt[0] = 0;
    int i = 1;
    if (i < arrayOfInt.length)
    {
      int j;
      label36:
      int k;
      if (i < 8)
      {
        if ((i & 0x1) != 0)
        {
          j = 255;
          if ((i & 0x2) == 0) {
            break label81;
          }
          k = 255;
          label46:
          if ((i & 0x4) == 0) {
            break label86;
          }
        }
        label81:
        label86:
        for (m = 255;; m = 0)
        {
          arrayOfInt[i] = getColor(255, j, k, m);
          i += 1;
          break;
          j = 0;
          break label36;
          k = 0;
          break label46;
        }
      }
      if ((i & 0x1) != 0)
      {
        j = 127;
        label100:
        if ((i & 0x2) == 0) {
          break label139;
        }
        k = 127;
        label109:
        if ((i & 0x4) == 0) {
          break label144;
        }
      }
      label139:
      label144:
      for (int m = 127;; m = 0)
      {
        arrayOfInt[i] = getColor(255, j, k, m);
        break;
        j = 0;
        break label100;
        k = 0;
        break label109;
      }
    }
    return arrayOfInt;
  }
  
  private static int[] generateDefault8BitClutEntries()
  {
    int[] arrayOfInt = new int['Ā'];
    arrayOfInt[0] = 0;
    int i = 0;
    if (i < arrayOfInt.length)
    {
      int j;
      label37:
      int k;
      label47:
      int m;
      if (i < 8) {
        if ((i & 0x1) != 0)
        {
          j = 255;
          if ((i & 0x2) == 0) {
            break label81;
          }
          k = 255;
          if ((i & 0x4) == 0) {
            break label86;
          }
          m = 255;
          label57:
          arrayOfInt[i] = getColor(63, j, k, m);
        }
      }
      for (;;)
      {
        i += 1;
        break;
        j = 0;
        break label37;
        label81:
        k = 0;
        break label47;
        label86:
        m = 0;
        break label57;
        switch (i & 0x88)
        {
        }
      }
      label152:
      label163:
      label172:
      int n;
      label184:
      int i1;
      if ((i & 0x1) != 0)
      {
        j = 85;
        if ((i & 0x10) == 0) {
          break label236;
        }
        k = 170;
        if ((i & 0x2) == 0) {
          break label241;
        }
        m = 85;
        if ((i & 0x20) == 0) {
          break label246;
        }
        n = 170;
        if ((i & 0x4) == 0) {
          break label252;
        }
        i1 = 85;
        label194:
        if ((i & 0x40) == 0) {
          break label258;
        }
      }
      label236:
      label241:
      label246:
      label252:
      label258:
      for (int i2 = 170;; i2 = 0)
      {
        arrayOfInt[i] = getColor(255, j + k, m + n, i2 + i1);
        break;
        j = 0;
        break label152;
        k = 0;
        break label163;
        m = 0;
        break label172;
        n = 0;
        break label184;
        i1 = 0;
        break label194;
      }
      if ((i & 0x1) != 0)
      {
        j = 85;
        label273:
        if ((i & 0x10) == 0) {
          break label356;
        }
        k = 170;
        label284:
        if ((i & 0x2) == 0) {
          break label361;
        }
        m = 85;
        label293:
        if ((i & 0x20) == 0) {
          break label366;
        }
        n = 170;
        label305:
        if ((i & 0x4) == 0) {
          break label372;
        }
        i1 = 85;
        label315:
        if ((i & 0x40) == 0) {
          break label378;
        }
      }
      label356:
      label361:
      label366:
      label372:
      label378:
      for (i2 = 170;; i2 = 0)
      {
        arrayOfInt[i] = getColor(127, j + k, m + n, i2 + i1);
        break;
        j = 0;
        break label273;
        k = 0;
        break label284;
        m = 0;
        break label293;
        n = 0;
        break label305;
        i1 = 0;
        break label315;
      }
      if ((i & 0x1) != 0)
      {
        j = 43;
        label393:
        if ((i & 0x10) == 0) {
          break label483;
        }
        k = 85;
        label403:
        if ((i & 0x2) == 0) {
          break label488;
        }
        m = 43;
        label412:
        if ((i & 0x20) == 0) {
          break label493;
        }
        n = 85;
        label423:
        if ((i & 0x4) == 0) {
          break label499;
        }
        i1 = 43;
        label433:
        if ((i & 0x40) == 0) {
          break label505;
        }
      }
      label483:
      label488:
      label493:
      label499:
      label505:
      for (i2 = 85;; i2 = 0)
      {
        arrayOfInt[i] = getColor(255, j + 127 + k, m + 127 + n, i2 + (i1 + 127));
        break;
        j = 0;
        break label393;
        k = 0;
        break label403;
        m = 0;
        break label412;
        n = 0;
        break label423;
        i1 = 0;
        break label433;
      }
      if ((i & 0x1) != 0)
      {
        j = 43;
        label520:
        if ((i & 0x10) == 0) {
          break label601;
        }
        k = 85;
        label530:
        if ((i & 0x2) == 0) {
          break label606;
        }
        m = 43;
        label539:
        if ((i & 0x20) == 0) {
          break label611;
        }
        n = 85;
        label550:
        if ((i & 0x4) == 0) {
          break label617;
        }
        i1 = 43;
        label560:
        if ((i & 0x40) == 0) {
          break label623;
        }
      }
      label601:
      label606:
      label611:
      label617:
      label623:
      for (i2 = 85;; i2 = 0)
      {
        arrayOfInt[i] = getColor(255, j + k, m + n, i2 + i1);
        break;
        j = 0;
        break label520;
        k = 0;
        break label530;
        m = 0;
        break label539;
        n = 0;
        break label550;
        i1 = 0;
        break label560;
      }
    }
    return arrayOfInt;
  }
  
  private static int getColor(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return paramInt1 << 24 | paramInt2 << 16 | paramInt3 << 8 | paramInt4;
  }
  
  private static int paint2BitPixelCodeString(ParsableBitArray paramParsableBitArray, int[] paramArrayOfInt, byte[] paramArrayOfByte, int paramInt1, int paramInt2, Paint paramPaint, Canvas paramCanvas)
  {
    int k = 0;
    int j = paramInt1;
    paramInt1 = 0;
    int i = 0;
    int m = paramParsableBitArray.readBits(2);
    if (m != 0)
    {
      paramInt1 = 1;
      i = m;
      m = k;
    }
    for (;;)
    {
      if ((paramInt1 != 0) && (paramPaint != null))
      {
        k = i;
        if (paramArrayOfByte != null) {
          k = paramArrayOfByte[i];
        }
        paramPaint.setColor(paramArrayOfInt[k]);
        paramCanvas.drawRect(j, paramInt2, j + paramInt1, paramInt2 + 1, paramPaint);
      }
      paramInt1 = j + paramInt1;
      k = m;
      j = paramInt1;
      if (m == 0) {
        break;
      }
      return paramInt1;
      if (paramParsableBitArray.readBit())
      {
        paramInt1 = paramParsableBitArray.readBits(3) + 3;
        i = paramParsableBitArray.readBits(2);
        m = k;
      }
      else if (paramParsableBitArray.readBit())
      {
        paramInt1 = 1;
        m = k;
      }
      else
      {
        switch (paramParsableBitArray.readBits(2))
        {
        default: 
          m = k;
          break;
        case 0: 
          m = 1;
          break;
        case 1: 
          paramInt1 = 2;
          m = k;
          break;
        case 2: 
          paramInt1 = paramParsableBitArray.readBits(4) + 12;
          i = paramParsableBitArray.readBits(2);
          m = k;
          break;
        case 3: 
          paramInt1 = paramParsableBitArray.readBits(8) + 29;
          i = paramParsableBitArray.readBits(2);
          m = k;
        }
      }
    }
  }
  
  private static int paint4BitPixelCodeString(ParsableBitArray paramParsableBitArray, int[] paramArrayOfInt, byte[] paramArrayOfByte, int paramInt1, int paramInt2, Paint paramPaint, Canvas paramCanvas)
  {
    int k = 0;
    int j = paramInt1;
    paramInt1 = 0;
    int i = 0;
    int m = paramParsableBitArray.readBits(4);
    if (m != 0)
    {
      paramInt1 = 1;
      i = m;
      m = k;
    }
    for (;;)
    {
      if ((paramInt1 != 0) && (paramPaint != null))
      {
        k = i;
        if (paramArrayOfByte != null) {
          k = paramArrayOfByte[i];
        }
        paramPaint.setColor(paramArrayOfInt[k]);
        paramCanvas.drawRect(j, paramInt2, j + paramInt1, paramInt2 + 1, paramPaint);
      }
      paramInt1 = j + paramInt1;
      k = m;
      j = paramInt1;
      if (m == 0) {
        break;
      }
      return paramInt1;
      if (!paramParsableBitArray.readBit())
      {
        m = paramParsableBitArray.readBits(3);
        if (m != 0)
        {
          paramInt1 = m + 2;
          i = 0;
          m = k;
        }
        else
        {
          m = 1;
        }
      }
      else if (!paramParsableBitArray.readBit())
      {
        paramInt1 = paramParsableBitArray.readBits(2) + 4;
        i = paramParsableBitArray.readBits(4);
        m = k;
      }
      else
      {
        switch (paramParsableBitArray.readBits(2))
        {
        default: 
          m = k;
          break;
        case 0: 
          paramInt1 = 1;
          m = k;
          break;
        case 1: 
          paramInt1 = 2;
          m = k;
          break;
        case 2: 
          paramInt1 = paramParsableBitArray.readBits(4) + 9;
          i = paramParsableBitArray.readBits(4);
          m = k;
          break;
        case 3: 
          paramInt1 = paramParsableBitArray.readBits(8) + 25;
          i = paramParsableBitArray.readBits(4);
          m = k;
        }
      }
    }
  }
  
  private static int paint8BitPixelCodeString(ParsableBitArray paramParsableBitArray, int[] paramArrayOfInt, byte[] paramArrayOfByte, int paramInt1, int paramInt2, Paint paramPaint, Canvas paramCanvas)
  {
    int k = 0;
    int j = paramInt1;
    int i = 0;
    paramInt1 = 0;
    int m = paramParsableBitArray.readBits(8);
    if (m != 0)
    {
      i = 1;
      paramInt1 = m;
      m = k;
    }
    for (;;)
    {
      if ((i != 0) && (paramPaint != null))
      {
        k = paramInt1;
        if (paramArrayOfByte != null) {
          k = paramArrayOfByte[paramInt1];
        }
        paramPaint.setColor(paramArrayOfInt[k]);
        paramCanvas.drawRect(j, paramInt2, j + i, paramInt2 + 1, paramPaint);
      }
      paramInt1 = j + i;
      k = m;
      j = paramInt1;
      if (m == 0) {
        break;
      }
      return paramInt1;
      if (!paramParsableBitArray.readBit())
      {
        m = paramParsableBitArray.readBits(7);
        if (m != 0)
        {
          i = m;
          paramInt1 = 0;
          m = k;
        }
        else
        {
          m = 1;
        }
      }
      else
      {
        i = paramParsableBitArray.readBits(7);
        paramInt1 = paramParsableBitArray.readBits(8);
        m = k;
      }
    }
  }
  
  private static void paintPixelDataSubBlock(byte[] paramArrayOfByte, int[] paramArrayOfInt, int paramInt1, int paramInt2, int paramInt3, Paint paramPaint, Canvas paramCanvas)
  {
    ParsableBitArray localParsableBitArray = new ParsableBitArray(paramArrayOfByte);
    int j = paramInt2;
    byte[] arrayOfByte2 = null;
    paramArrayOfByte = null;
    int i = paramInt3;
    paramInt3 = j;
    while (localParsableBitArray.bitsLeft() != 0)
    {
      byte[] arrayOfByte1;
      switch (localParsableBitArray.readBits(8))
      {
      default: 
        break;
      case 16: 
        if (paramInt1 == 3) {
          if (paramArrayOfByte == null) {
            arrayOfByte1 = defaultMap2To8;
          }
        }
        for (;;)
        {
          paramInt3 = paint2BitPixelCodeString(localParsableBitArray, paramArrayOfInt, arrayOfByte1, paramInt3, i, paramPaint, paramCanvas);
          localParsableBitArray.byteAlign();
          break;
          arrayOfByte1 = paramArrayOfByte;
          continue;
          if (paramInt1 == 2)
          {
            if (arrayOfByte2 == null) {}
            for (arrayOfByte1 = defaultMap2To4;; arrayOfByte1 = arrayOfByte2) {
              break;
            }
          }
          arrayOfByte1 = null;
        }
      case 17: 
        if (paramInt1 == 3) {
          if (0 == 0) {
            arrayOfByte1 = defaultMap4To8;
          }
        }
        for (;;)
        {
          paramInt3 = paint4BitPixelCodeString(localParsableBitArray, paramArrayOfInt, arrayOfByte1, paramInt3, i, paramPaint, paramCanvas);
          localParsableBitArray.byteAlign();
          break;
          arrayOfByte1 = null;
          continue;
          arrayOfByte1 = null;
        }
      case 18: 
        paramInt3 = paint8BitPixelCodeString(localParsableBitArray, paramArrayOfInt, null, paramInt3, i, paramPaint, paramCanvas);
        break;
      case 32: 
        arrayOfByte2 = buildClutMapTable(4, 4, localParsableBitArray);
        break;
      case 33: 
        paramArrayOfByte = buildClutMapTable(4, 8, localParsableBitArray);
        break;
      case 34: 
        paramArrayOfByte = buildClutMapTable(16, 8, localParsableBitArray);
        break;
      case 240: 
        paramInt3 = paramInt2;
        i += 2;
      }
    }
  }
  
  private static void paintPixelDataSubBlocks(ObjectData paramObjectData, ClutDefinition paramClutDefinition, int paramInt1, int paramInt2, int paramInt3, Paint paramPaint, Canvas paramCanvas)
  {
    if (paramInt1 == 3) {
      paramClutDefinition = paramClutDefinition.clutEntries8Bit;
    }
    for (;;)
    {
      paintPixelDataSubBlock(paramObjectData.topFieldData, paramClutDefinition, paramInt1, paramInt2, paramInt3, paramPaint, paramCanvas);
      paintPixelDataSubBlock(paramObjectData.bottomFieldData, paramClutDefinition, paramInt1, paramInt2, paramInt3 + 1, paramPaint, paramCanvas);
      return;
      if (paramInt1 == 2) {
        paramClutDefinition = paramClutDefinition.clutEntries4Bit;
      } else {
        paramClutDefinition = paramClutDefinition.clutEntries2Bit;
      }
    }
  }
  
  private static ClutDefinition parseClutDefinition(ParsableBitArray paramParsableBitArray, int paramInt)
  {
    int n = paramParsableBitArray.readBits(8);
    paramParsableBitArray.skipBits(8);
    paramInt -= 2;
    int[] arrayOfInt2 = generateDefault2BitClutEntries();
    int[] arrayOfInt3 = generateDefault4BitClutEntries();
    int[] arrayOfInt4 = generateDefault8BitClutEntries();
    if (paramInt > 0)
    {
      int i1 = paramParsableBitArray.readBits(8);
      int i = paramParsableBitArray.readBits(8);
      paramInt -= 2;
      int[] arrayOfInt1;
      label68:
      int m;
      int j;
      int k;
      if ((i & 0x80) != 0)
      {
        arrayOfInt1 = arrayOfInt2;
        if ((i & 0x1) == 0) {
          break label255;
        }
        m = paramParsableBitArray.readBits(8);
        j = paramParsableBitArray.readBits(8);
        k = paramParsableBitArray.readBits(8);
        i = paramParsableBitArray.readBits(8);
        paramInt -= 4;
      }
      for (;;)
      {
        if (m == 0)
        {
          j = 0;
          k = 0;
          i = 255;
        }
        i = (byte)(255 - (i & 0xFF));
        int i2 = (int)(m + 1.402D * (j - 128));
        j = (int)(m - 0.34414D * (k - 128) - 0.71414D * (j - 128));
        k = (int)(m + 1.772D * (k - 128));
        arrayOfInt1[i1] = getColor(i, Util.constrainValue(i2, 0, 255), Util.constrainValue(j, 0, 255), Util.constrainValue(k, 0, 255));
        break;
        if ((i & 0x40) != 0)
        {
          arrayOfInt1 = arrayOfInt3;
          break label68;
        }
        arrayOfInt1 = arrayOfInt4;
        break label68;
        label255:
        m = paramParsableBitArray.readBits(6) << 2;
        j = paramParsableBitArray.readBits(4) << 4;
        k = paramParsableBitArray.readBits(4) << 4;
        i = paramParsableBitArray.readBits(2) << 6;
        paramInt -= 2;
      }
    }
    return new ClutDefinition(n, arrayOfInt2, arrayOfInt3, arrayOfInt4);
  }
  
  private static DisplayDefinition parseDisplayDefinition(ParsableBitArray paramParsableBitArray)
  {
    paramParsableBitArray.skipBits(4);
    boolean bool = paramParsableBitArray.readBit();
    paramParsableBitArray.skipBits(3);
    int n = paramParsableBitArray.readBits(16);
    int i1 = paramParsableBitArray.readBits(16);
    int i;
    int j;
    int k;
    if (bool)
    {
      i = paramParsableBitArray.readBits(16);
      j = paramParsableBitArray.readBits(16);
      k = paramParsableBitArray.readBits(16);
    }
    for (int m = paramParsableBitArray.readBits(16);; m = i1)
    {
      return new DisplayDefinition(n, i1, i, j, k, m);
      i = 0;
      j = n;
      k = 0;
    }
  }
  
  private static ObjectData parseObjectData(ParsableBitArray paramParsableBitArray)
  {
    int i = paramParsableBitArray.readBits(16);
    paramParsableBitArray.skipBits(4);
    int j = paramParsableBitArray.readBits(2);
    boolean bool = paramParsableBitArray.readBit();
    paramParsableBitArray.skipBits(1);
    Object localObject3 = null;
    Object localObject1 = null;
    Object localObject2 = null;
    if (j == 1) {
      paramParsableBitArray.skipBits(paramParsableBitArray.readBits(8) * 16);
    }
    for (;;)
    {
      return new ObjectData(i, bool, (byte[])localObject1, (byte[])localObject2);
      if (j == 0)
      {
        j = paramParsableBitArray.readBits(16);
        int k = paramParsableBitArray.readBits(16);
        localObject1 = localObject3;
        if (j > 0)
        {
          localObject1 = new byte[j];
          paramParsableBitArray.readBytes((byte[])localObject1, 0, j);
        }
        if (k > 0)
        {
          localObject2 = new byte[k];
          paramParsableBitArray.readBytes((byte[])localObject2, 0, k);
        }
        else
        {
          localObject2 = localObject1;
        }
      }
    }
  }
  
  private static PageComposition parsePageComposition(ParsableBitArray paramParsableBitArray, int paramInt)
  {
    int i = paramParsableBitArray.readBits(8);
    int j = paramParsableBitArray.readBits(4);
    int k = paramParsableBitArray.readBits(2);
    paramParsableBitArray.skipBits(2);
    paramInt -= 2;
    SparseArray localSparseArray = new SparseArray();
    while (paramInt > 0)
    {
      int m = paramParsableBitArray.readBits(8);
      paramParsableBitArray.skipBits(8);
      int n = paramParsableBitArray.readBits(16);
      int i1 = paramParsableBitArray.readBits(16);
      paramInt -= 6;
      localSparseArray.put(m, new PageRegion(n, i1));
    }
    return new PageComposition(i, j, k, localSparseArray);
  }
  
  private static RegionComposition parseRegionComposition(ParsableBitArray paramParsableBitArray, int paramInt)
  {
    int m = paramParsableBitArray.readBits(8);
    paramParsableBitArray.skipBits(4);
    boolean bool = paramParsableBitArray.readBit();
    paramParsableBitArray.skipBits(3);
    int n = paramParsableBitArray.readBits(16);
    int i1 = paramParsableBitArray.readBits(16);
    int i2 = paramParsableBitArray.readBits(3);
    int i3 = paramParsableBitArray.readBits(3);
    paramParsableBitArray.skipBits(2);
    int i4 = paramParsableBitArray.readBits(8);
    int i5 = paramParsableBitArray.readBits(8);
    int i6 = paramParsableBitArray.readBits(4);
    int i7 = paramParsableBitArray.readBits(2);
    paramParsableBitArray.skipBits(2);
    paramInt -= 10;
    SparseArray localSparseArray = new SparseArray();
    while (paramInt > 0)
    {
      int i8 = paramParsableBitArray.readBits(16);
      int i9 = paramParsableBitArray.readBits(2);
      int i10 = paramParsableBitArray.readBits(2);
      int i11 = paramParsableBitArray.readBits(12);
      paramParsableBitArray.skipBits(4);
      int i12 = paramParsableBitArray.readBits(12);
      int k = paramInt - 6;
      int i = 0;
      int j = 0;
      if (i9 != 1)
      {
        paramInt = k;
        if (i9 != 2) {}
      }
      else
      {
        i = paramParsableBitArray.readBits(8);
        j = paramParsableBitArray.readBits(8);
        paramInt = k - 2;
      }
      localSparseArray.put(i8, new RegionObject(i9, i10, i11, i12, i, j));
    }
    return new RegionComposition(m, bool, n, i1, i2, i3, i4, i5, i6, i7, localSparseArray);
  }
  
  private static void parseSubtitlingSegment(ParsableBitArray paramParsableBitArray, SubtitleService paramSubtitleService)
  {
    int i = paramParsableBitArray.readBits(8);
    int j = paramParsableBitArray.readBits(16);
    int k = paramParsableBitArray.readBits(16);
    int m = paramParsableBitArray.getBytePosition();
    if (k * 8 > paramParsableBitArray.bitsLeft())
    {
      Log.w("DvbParser", "Data field length exceeds limit");
      paramParsableBitArray.skipBits(paramParsableBitArray.bitsLeft());
      return;
    }
    switch (i)
    {
    }
    for (;;)
    {
      paramParsableBitArray.skipBytes(m + k - paramParsableBitArray.getBytePosition());
      return;
      if (j == paramSubtitleService.subtitlePageId)
      {
        paramSubtitleService.displayDefinition = parseDisplayDefinition(paramParsableBitArray);
        continue;
        if (j == paramSubtitleService.subtitlePageId)
        {
          Object localObject1 = paramSubtitleService.pageComposition;
          Object localObject2 = parsePageComposition(paramParsableBitArray, k);
          if (((PageComposition)localObject2).state != 0)
          {
            paramSubtitleService.pageComposition = ((PageComposition)localObject2);
            paramSubtitleService.regions.clear();
            paramSubtitleService.cluts.clear();
            paramSubtitleService.objects.clear();
          }
          else if ((localObject1 != null) && (((PageComposition)localObject1).version != ((PageComposition)localObject2).version))
          {
            paramSubtitleService.pageComposition = ((PageComposition)localObject2);
            continue;
            localObject1 = paramSubtitleService.pageComposition;
            if ((j == paramSubtitleService.subtitlePageId) && (localObject1 != null))
            {
              localObject2 = parseRegionComposition(paramParsableBitArray, k);
              if (((PageComposition)localObject1).state == 0) {
                ((RegionComposition)localObject2).mergeFrom((RegionComposition)paramSubtitleService.regions.get(((RegionComposition)localObject2).id));
              }
              paramSubtitleService.regions.put(((RegionComposition)localObject2).id, localObject2);
              continue;
              if (j == paramSubtitleService.subtitlePageId)
              {
                localObject1 = parseClutDefinition(paramParsableBitArray, k);
                paramSubtitleService.cluts.put(((ClutDefinition)localObject1).id, localObject1);
              }
              else if (j == paramSubtitleService.ancillaryPageId)
              {
                localObject1 = parseClutDefinition(paramParsableBitArray, k);
                paramSubtitleService.ancillaryCluts.put(((ClutDefinition)localObject1).id, localObject1);
                continue;
                if (j == paramSubtitleService.subtitlePageId)
                {
                  localObject1 = parseObjectData(paramParsableBitArray);
                  paramSubtitleService.objects.put(((ObjectData)localObject1).id, localObject1);
                }
                else if (j == paramSubtitleService.ancillaryPageId)
                {
                  localObject1 = parseObjectData(paramParsableBitArray);
                  paramSubtitleService.ancillaryObjects.put(((ObjectData)localObject1).id, localObject1);
                }
              }
            }
          }
        }
      }
    }
  }
  
  public List<Cue> decode(byte[] paramArrayOfByte, int paramInt)
  {
    paramArrayOfByte = new ParsableBitArray(paramArrayOfByte, paramInt);
    while ((paramArrayOfByte.bitsLeft() >= 48) && (paramArrayOfByte.readBits(8) == 15)) {
      parseSubtitlingSegment(paramArrayOfByte, this.subtitleService);
    }
    if (this.subtitleService.pageComposition == null) {
      paramArrayOfByte = Collections.emptyList();
    }
    DisplayDefinition localDisplayDefinition;
    ArrayList localArrayList;
    SparseArray localSparseArray1;
    int i;
    do
    {
      return paramArrayOfByte;
      if (this.subtitleService.displayDefinition == null) {
        break;
      }
      localDisplayDefinition = this.subtitleService.displayDefinition;
      if ((this.bitmap == null) || (localDisplayDefinition.width + 1 != this.bitmap.getWidth()) || (localDisplayDefinition.height + 1 != this.bitmap.getHeight()))
      {
        this.bitmap = Bitmap.createBitmap(localDisplayDefinition.width + 1, localDisplayDefinition.height + 1, Bitmap.Config.ARGB_8888);
        this.canvas.setBitmap(this.bitmap);
      }
      localArrayList = new ArrayList();
      localSparseArray1 = this.subtitleService.pageComposition.regions;
      i = 0;
      paramArrayOfByte = localArrayList;
    } while (i >= localSparseArray1.size());
    paramArrayOfByte = (PageRegion)localSparseArray1.valueAt(i);
    paramInt = localSparseArray1.keyAt(i);
    RegionComposition localRegionComposition = (RegionComposition)this.subtitleService.regions.get(paramInt);
    int j = paramArrayOfByte.horizontalAddress + localDisplayDefinition.horizontalPositionMinimum;
    int k = paramArrayOfByte.verticalAddress + localDisplayDefinition.verticalPositionMinimum;
    paramInt = Math.min(localRegionComposition.width + j, localDisplayDefinition.horizontalPositionMaximum);
    int m = Math.min(localRegionComposition.height + k, localDisplayDefinition.verticalPositionMaximum);
    this.canvas.clipRect(j, k, paramInt, m, Region.Op.REPLACE);
    Object localObject1 = (ClutDefinition)this.subtitleService.cluts.get(localRegionComposition.clutId);
    paramArrayOfByte = (byte[])localObject1;
    if (localObject1 == null)
    {
      localObject1 = (ClutDefinition)this.subtitleService.ancillaryCluts.get(localRegionComposition.clutId);
      paramArrayOfByte = (byte[])localObject1;
      if (localObject1 == null) {
        paramArrayOfByte = this.defaultClutDefinition;
      }
    }
    SparseArray localSparseArray2 = localRegionComposition.regionObjects;
    paramInt = 0;
    label371:
    if (paramInt < localSparseArray2.size())
    {
      m = localSparseArray2.keyAt(paramInt);
      RegionObject localRegionObject = (RegionObject)localSparseArray2.valueAt(paramInt);
      Object localObject2 = (ObjectData)this.subtitleService.objects.get(m);
      localObject1 = localObject2;
      if (localObject2 == null) {
        localObject1 = (ObjectData)this.subtitleService.ancillaryObjects.get(m);
      }
      if (localObject1 != null) {
        if (!((ObjectData)localObject1).nonModifyingColorFlag) {
          break label507;
        }
      }
      label507:
      for (localObject2 = null;; localObject2 = this.defaultPaint)
      {
        paintPixelDataSubBlocks((ObjectData)localObject1, paramArrayOfByte, localRegionComposition.depth, localRegionObject.horizontalPosition + j, localRegionObject.verticalPosition + k, (Paint)localObject2, this.canvas);
        paramInt += 1;
        break label371;
        localDisplayDefinition = this.defaultDisplayDefinition;
        break;
      }
    }
    if (localRegionComposition.fillFlag)
    {
      if (localRegionComposition.depth != 3) {
        break label689;
      }
      paramInt = paramArrayOfByte.clutEntries8Bit[localRegionComposition.pixelCode8Bit];
    }
    for (;;)
    {
      this.fillRegionPaint.setColor(paramInt);
      this.canvas.drawRect(j, k, localRegionComposition.width + j, localRegionComposition.height + k, this.fillRegionPaint);
      localArrayList.add(new Cue(Bitmap.createBitmap(this.bitmap, j, k, localRegionComposition.width, localRegionComposition.height), j / localDisplayDefinition.width, 0, k / localDisplayDefinition.height, 0, localRegionComposition.width / localDisplayDefinition.width, localRegionComposition.height / localDisplayDefinition.height));
      this.canvas.drawColor(0, PorterDuff.Mode.CLEAR);
      i += 1;
      break;
      label689:
      if (localRegionComposition.depth == 2) {
        paramInt = paramArrayOfByte.clutEntries4Bit[localRegionComposition.pixelCode4Bit];
      } else {
        paramInt = paramArrayOfByte.clutEntries2Bit[localRegionComposition.pixelCode2Bit];
      }
    }
  }
  
  public void reset()
  {
    this.subtitleService.reset();
  }
  
  private static final class ClutDefinition
  {
    public final int[] clutEntries2Bit;
    public final int[] clutEntries4Bit;
    public final int[] clutEntries8Bit;
    public final int id;
    
    public ClutDefinition(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
    {
      this.id = paramInt;
      this.clutEntries2Bit = paramArrayOfInt1;
      this.clutEntries4Bit = paramArrayOfInt2;
      this.clutEntries8Bit = paramArrayOfInt3;
    }
  }
  
  private static final class DisplayDefinition
  {
    public final int height;
    public final int horizontalPositionMaximum;
    public final int horizontalPositionMinimum;
    public final int verticalPositionMaximum;
    public final int verticalPositionMinimum;
    public final int width;
    
    public DisplayDefinition(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      this.width = paramInt1;
      this.height = paramInt2;
      this.horizontalPositionMinimum = paramInt3;
      this.horizontalPositionMaximum = paramInt4;
      this.verticalPositionMinimum = paramInt5;
      this.verticalPositionMaximum = paramInt6;
    }
  }
  
  private static final class ObjectData
  {
    public final byte[] bottomFieldData;
    public final int id;
    public final boolean nonModifyingColorFlag;
    public final byte[] topFieldData;
    
    public ObjectData(int paramInt, boolean paramBoolean, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    {
      this.id = paramInt;
      this.nonModifyingColorFlag = paramBoolean;
      this.topFieldData = paramArrayOfByte1;
      this.bottomFieldData = paramArrayOfByte2;
    }
  }
  
  private static final class PageComposition
  {
    public final SparseArray<DvbParser.PageRegion> regions;
    public final int state;
    public final int timeOutSecs;
    public final int version;
    
    public PageComposition(int paramInt1, int paramInt2, int paramInt3, SparseArray<DvbParser.PageRegion> paramSparseArray)
    {
      this.timeOutSecs = paramInt1;
      this.version = paramInt2;
      this.state = paramInt3;
      this.regions = paramSparseArray;
    }
  }
  
  private static final class PageRegion
  {
    public final int horizontalAddress;
    public final int verticalAddress;
    
    public PageRegion(int paramInt1, int paramInt2)
    {
      this.horizontalAddress = paramInt1;
      this.verticalAddress = paramInt2;
    }
  }
  
  private static final class RegionComposition
  {
    public final int clutId;
    public final int depth;
    public final boolean fillFlag;
    public final int height;
    public final int id;
    public final int levelOfCompatibility;
    public final int pixelCode2Bit;
    public final int pixelCode4Bit;
    public final int pixelCode8Bit;
    public final SparseArray<DvbParser.RegionObject> regionObjects;
    public final int width;
    
    public RegionComposition(int paramInt1, boolean paramBoolean, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, int paramInt9, SparseArray<DvbParser.RegionObject> paramSparseArray)
    {
      this.id = paramInt1;
      this.fillFlag = paramBoolean;
      this.width = paramInt2;
      this.height = paramInt3;
      this.levelOfCompatibility = paramInt4;
      this.depth = paramInt5;
      this.clutId = paramInt6;
      this.pixelCode8Bit = paramInt7;
      this.pixelCode4Bit = paramInt8;
      this.pixelCode2Bit = paramInt9;
      this.regionObjects = paramSparseArray;
    }
    
    public void mergeFrom(RegionComposition paramRegionComposition)
    {
      if (paramRegionComposition == null) {}
      for (;;)
      {
        return;
        paramRegionComposition = paramRegionComposition.regionObjects;
        int i = 0;
        while (i < paramRegionComposition.size())
        {
          this.regionObjects.put(paramRegionComposition.keyAt(i), paramRegionComposition.valueAt(i));
          i += 1;
        }
      }
    }
  }
  
  private static final class RegionObject
  {
    public final int backgroundPixelCode;
    public final int foregroundPixelCode;
    public final int horizontalPosition;
    public final int provider;
    public final int type;
    public final int verticalPosition;
    
    public RegionObject(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      this.type = paramInt1;
      this.provider = paramInt2;
      this.horizontalPosition = paramInt3;
      this.verticalPosition = paramInt4;
      this.foregroundPixelCode = paramInt5;
      this.backgroundPixelCode = paramInt6;
    }
  }
  
  private static final class SubtitleService
  {
    public final SparseArray<DvbParser.ClutDefinition> ancillaryCluts = new SparseArray();
    public final SparseArray<DvbParser.ObjectData> ancillaryObjects = new SparseArray();
    public final int ancillaryPageId;
    public final SparseArray<DvbParser.ClutDefinition> cluts = new SparseArray();
    public DvbParser.DisplayDefinition displayDefinition;
    public final SparseArray<DvbParser.ObjectData> objects = new SparseArray();
    public DvbParser.PageComposition pageComposition;
    public final SparseArray<DvbParser.RegionComposition> regions = new SparseArray();
    public final int subtitlePageId;
    
    public SubtitleService(int paramInt1, int paramInt2)
    {
      this.subtitlePageId = paramInt1;
      this.ancillaryPageId = paramInt2;
    }
    
    public void reset()
    {
      this.regions.clear();
      this.cluts.clear();
      this.objects.clear();
      this.ancillaryCluts.clear();
      this.ancillaryObjects.clear();
      this.displayDefinition = null;
      this.pageComposition = null;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/dvb/DvbParser.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */