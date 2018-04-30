package org.telegram.messenger.exoplayer2.text.pgs;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.telegram.messenger.exoplayer2.text.Cue;
import org.telegram.messenger.exoplayer2.text.SimpleSubtitleDecoder;
import org.telegram.messenger.exoplayer2.text.Subtitle;
import org.telegram.messenger.exoplayer2.text.SubtitleDecoderException;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.Util;

public final class PgsDecoder
  extends SimpleSubtitleDecoder
{
  private static final int SECTION_TYPE_BITMAP_PICTURE = 21;
  private static final int SECTION_TYPE_END = 128;
  private static final int SECTION_TYPE_IDENTIFIER = 22;
  private static final int SECTION_TYPE_PALETTE = 20;
  private final ParsableByteArray buffer = new ParsableByteArray();
  private final CueBuilder cueBuilder = new CueBuilder();
  
  public PgsDecoder()
  {
    super("PgsDecoder");
  }
  
  private static Cue readNextSection(ParsableByteArray paramParsableByteArray, CueBuilder paramCueBuilder)
  {
    int i = paramParsableByteArray.limit();
    int j = paramParsableByteArray.readUnsignedByte();
    int k = paramParsableByteArray.readUnsignedShort();
    int m = paramParsableByteArray.getPosition() + k;
    if (m > i)
    {
      paramParsableByteArray.setPosition(i);
      return null;
    }
    Cue localCue = null;
    switch (j)
    {
    default: 
      paramCueBuilder = localCue;
    }
    for (;;)
    {
      paramParsableByteArray.setPosition(m);
      return paramCueBuilder;
      paramCueBuilder.parsePaletteSection(paramParsableByteArray, k);
      paramCueBuilder = localCue;
      continue;
      paramCueBuilder.parseBitmapSection(paramParsableByteArray, k);
      paramCueBuilder = localCue;
      continue;
      paramCueBuilder.parseIdentifierSection(paramParsableByteArray, k);
      paramCueBuilder = localCue;
      continue;
      localCue = paramCueBuilder.build();
      paramCueBuilder.reset();
      paramCueBuilder = localCue;
    }
  }
  
  protected Subtitle decode(byte[] paramArrayOfByte, int paramInt, boolean paramBoolean)
    throws SubtitleDecoderException
  {
    this.buffer.reset(paramArrayOfByte, paramInt);
    this.cueBuilder.reset();
    paramArrayOfByte = new ArrayList();
    while (this.buffer.bytesLeft() >= 3)
    {
      Cue localCue = readNextSection(this.buffer, this.cueBuilder);
      if (localCue != null) {
        paramArrayOfByte.add(localCue);
      }
    }
    return new PgsSubtitle(Collections.unmodifiableList(paramArrayOfByte));
  }
  
  private static final class CueBuilder
  {
    private final ParsableByteArray bitmapData = new ParsableByteArray();
    private int bitmapHeight;
    private int bitmapWidth;
    private int bitmapX;
    private int bitmapY;
    private final int[] colors = new int['Ā'];
    private boolean colorsSet;
    private int planeHeight;
    private int planeWidth;
    
    private void parseBitmapSection(ParsableByteArray paramParsableByteArray, int paramInt)
    {
      if (paramInt < 4) {
        return;
      }
      paramParsableByteArray.skipBytes(3);
      if ((paramParsableByteArray.readUnsignedByte() & 0x80) != 0) {}
      for (int i = 1;; i = 0)
      {
        int j = paramInt - 4;
        paramInt = j;
        if (i != 0)
        {
          if (j < 7) {
            break;
          }
          paramInt = paramParsableByteArray.readUnsignedInt24();
          if (paramInt < 4) {
            break;
          }
          this.bitmapWidth = paramParsableByteArray.readUnsignedShort();
          this.bitmapHeight = paramParsableByteArray.readUnsignedShort();
          this.bitmapData.reset(paramInt - 4);
          paramInt = j - 7;
        }
        i = this.bitmapData.getPosition();
        j = this.bitmapData.limit();
        if ((i >= j) || (paramInt <= 0)) {
          break;
        }
        paramInt = Math.min(paramInt, j - i);
        paramParsableByteArray.readBytes(this.bitmapData.data, i, paramInt);
        this.bitmapData.setPosition(i + paramInt);
        return;
      }
    }
    
    private void parseIdentifierSection(ParsableByteArray paramParsableByteArray, int paramInt)
    {
      if (paramInt < 19) {
        return;
      }
      this.planeWidth = paramParsableByteArray.readUnsignedShort();
      this.planeHeight = paramParsableByteArray.readUnsignedShort();
      paramParsableByteArray.skipBytes(11);
      this.bitmapX = paramParsableByteArray.readUnsignedShort();
      this.bitmapY = paramParsableByteArray.readUnsignedShort();
    }
    
    private void parsePaletteSection(ParsableByteArray paramParsableByteArray, int paramInt)
    {
      if (paramInt % 5 != 2) {
        return;
      }
      paramParsableByteArray.skipBytes(2);
      Arrays.fill(this.colors, 0);
      int i = paramInt / 5;
      paramInt = 0;
      while (paramInt < i)
      {
        int j = paramParsableByteArray.readUnsignedByte();
        int n = paramParsableByteArray.readUnsignedByte();
        int i2 = paramParsableByteArray.readUnsignedByte();
        int i1 = paramParsableByteArray.readUnsignedByte();
        int k = paramParsableByteArray.readUnsignedByte();
        int m = (int)(n + 1.402D * (i2 - 128));
        i2 = (int)(n - 0.34414D * (i1 - 128) - 0.71414D * (i2 - 128));
        n = (int)(n + 1.772D * (i1 - 128));
        this.colors[j] = (k << 24 | Util.constrainValue(m, 0, 255) << 16 | Util.constrainValue(i2, 0, 255) << 8 | Util.constrainValue(n, 0, 255));
        paramInt += 1;
      }
      this.colorsSet = true;
    }
    
    public Cue build()
    {
      if ((this.planeWidth == 0) || (this.planeHeight == 0) || (this.bitmapWidth == 0) || (this.bitmapHeight == 0) || (this.bitmapData.limit() == 0) || (this.bitmapData.getPosition() != this.bitmapData.limit()) || (!this.colorsSet)) {
        return null;
      }
      this.bitmapData.setPosition(0);
      int[] arrayOfInt = new int[this.bitmapWidth * this.bitmapHeight];
      int i = 0;
      while (i < arrayOfInt.length)
      {
        int j = this.bitmapData.readUnsignedByte();
        if (j != 0)
        {
          arrayOfInt[i] = this.colors[j];
          i += 1;
        }
        else
        {
          int k = this.bitmapData.readUnsignedByte();
          if (k != 0)
          {
            if ((k & 0x40) == 0)
            {
              j = k & 0x3F;
              label147:
              if ((k & 0x80) != 0) {
                break label193;
              }
            }
            label193:
            for (k = 0;; k = this.colors[this.bitmapData.readUnsignedByte()])
            {
              Arrays.fill(arrayOfInt, i, i + j, k);
              i += j;
              break;
              j = (k & 0x3F) << 8 | this.bitmapData.readUnsignedByte();
              break label147;
            }
          }
        }
      }
      return new Cue(Bitmap.createBitmap(arrayOfInt, this.bitmapWidth, this.bitmapHeight, Bitmap.Config.ARGB_8888), this.bitmapX / this.planeWidth, 0, this.bitmapY / this.planeHeight, 0, this.bitmapWidth / this.planeWidth, this.bitmapHeight / this.planeHeight);
    }
    
    public void reset()
    {
      this.planeWidth = 0;
      this.planeHeight = 0;
      this.bitmapX = 0;
      this.bitmapY = 0;
      this.bitmapWidth = 0;
      this.bitmapHeight = 0;
      this.bitmapData.reset(0);
      this.colorsSet = false;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/pgs/PgsDecoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */