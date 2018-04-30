package org.telegram.messenger.exoplayer2.util;

public final class ParsableBitArray
{
  private int bitOffset;
  private int byteLimit;
  private int byteOffset;
  public byte[] data;
  
  public ParsableBitArray() {}
  
  public ParsableBitArray(byte[] paramArrayOfByte)
  {
    this(paramArrayOfByte, paramArrayOfByte.length);
  }
  
  public ParsableBitArray(byte[] paramArrayOfByte, int paramInt)
  {
    this.data = paramArrayOfByte;
    this.byteLimit = paramInt;
  }
  
  private void assertValidOffset()
  {
    if ((this.byteOffset >= 0) && ((this.byteOffset < this.byteLimit) || ((this.byteOffset == this.byteLimit) && (this.bitOffset == 0)))) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      return;
    }
  }
  
  public int bitsLeft()
  {
    return (this.byteLimit - this.byteOffset) * 8 - this.bitOffset;
  }
  
  public void byteAlign()
  {
    if (this.bitOffset == 0) {
      return;
    }
    this.bitOffset = 0;
    this.byteOffset += 1;
    assertValidOffset();
  }
  
  public int getBytePosition()
  {
    if (this.bitOffset == 0) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      return this.byteOffset;
    }
  }
  
  public int getPosition()
  {
    return this.byteOffset * 8 + this.bitOffset;
  }
  
  public boolean readBit()
  {
    if ((this.data[this.byteOffset] & 128 >> this.bitOffset) != 0) {}
    for (boolean bool = true;; bool = false)
    {
      skipBit();
      return bool;
    }
  }
  
  public int readBits(int paramInt)
  {
    if (paramInt == 0) {
      return 0;
    }
    int i = 0;
    this.bitOffset += paramInt;
    while (this.bitOffset > 8)
    {
      this.bitOffset -= 8;
      byte[] arrayOfByte = this.data;
      j = this.byteOffset;
      this.byteOffset = (j + 1);
      i |= (arrayOfByte[j] & 0xFF) << this.bitOffset;
    }
    int j = this.data[this.byteOffset];
    int k = this.bitOffset;
    if (this.bitOffset == 8)
    {
      this.bitOffset = 0;
      this.byteOffset += 1;
    }
    assertValidOffset();
    return (i | (j & 0xFF) >> 8 - k) & -1 >>> 32 - paramInt;
  }
  
  public void readBits(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    int i = paramInt1 + (paramInt2 >> 3);
    byte[] arrayOfByte;
    while (paramInt1 < i)
    {
      arrayOfByte = this.data;
      j = this.byteOffset;
      this.byteOffset = (j + 1);
      paramArrayOfByte[paramInt1] = ((byte)(arrayOfByte[j] << this.bitOffset));
      paramArrayOfByte[paramInt1] = ((byte)(paramArrayOfByte[paramInt1] | (this.data[this.byteOffset] & 0xFF) >> 8 - this.bitOffset));
      paramInt1 += 1;
    }
    paramInt1 = paramInt2 & 0x7;
    if (paramInt1 == 0) {
      return;
    }
    paramArrayOfByte[i] = ((byte)(paramArrayOfByte[i] & 255 >> paramInt1));
    if (this.bitOffset + paramInt1 > 8)
    {
      paramInt2 = paramArrayOfByte[i];
      arrayOfByte = this.data;
      j = this.byteOffset;
      this.byteOffset = (j + 1);
      paramArrayOfByte[i] = ((byte)(paramInt2 | (byte)((arrayOfByte[j] & 0xFF) << this.bitOffset)));
      this.bitOffset -= 8;
    }
    this.bitOffset += paramInt1;
    paramInt2 = this.data[this.byteOffset];
    int j = this.bitOffset;
    paramArrayOfByte[i] = ((byte)(paramArrayOfByte[i] | (byte)((paramInt2 & 0xFF) >> 8 - j << 8 - paramInt1)));
    if (this.bitOffset == 8)
    {
      this.bitOffset = 0;
      this.byteOffset += 1;
    }
    assertValidOffset();
  }
  
  public void readBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (this.bitOffset == 0) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      System.arraycopy(this.data, this.byteOffset, paramArrayOfByte, paramInt1, paramInt2);
      this.byteOffset += paramInt2;
      assertValidOffset();
      return;
    }
  }
  
  public void reset(ParsableByteArray paramParsableByteArray)
  {
    reset(paramParsableByteArray.data, paramParsableByteArray.limit());
    setPosition(paramParsableByteArray.getPosition() * 8);
  }
  
  public void reset(byte[] paramArrayOfByte)
  {
    reset(paramArrayOfByte, paramArrayOfByte.length);
  }
  
  public void reset(byte[] paramArrayOfByte, int paramInt)
  {
    this.data = paramArrayOfByte;
    this.byteOffset = 0;
    this.bitOffset = 0;
    this.byteLimit = paramInt;
  }
  
  public void setPosition(int paramInt)
  {
    this.byteOffset = (paramInt / 8);
    this.bitOffset = (paramInt - this.byteOffset * 8);
    assertValidOffset();
  }
  
  public void skipBit()
  {
    int i = this.bitOffset + 1;
    this.bitOffset = i;
    if (i == 8)
    {
      this.bitOffset = 0;
      this.byteOffset += 1;
    }
    assertValidOffset();
  }
  
  public void skipBits(int paramInt)
  {
    int i = paramInt / 8;
    this.byteOffset += i;
    this.bitOffset += paramInt - i * 8;
    if (this.bitOffset > 7)
    {
      this.byteOffset += 1;
      this.bitOffset -= 8;
    }
    assertValidOffset();
  }
  
  public void skipBytes(int paramInt)
  {
    if (this.bitOffset == 0) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      this.byteOffset += paramInt;
      assertValidOffset();
      return;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/util/ParsableBitArray.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */