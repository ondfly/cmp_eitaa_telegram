package org.telegram.messenger.exoplayer2.extractor.ogg;

import org.telegram.messenger.exoplayer2.util.Assertions;

final class VorbisBitArray
{
  private int bitOffset;
  private final int byteLimit;
  private int byteOffset;
  private final byte[] data;
  
  public VorbisBitArray(byte[] paramArrayOfByte)
  {
    this.data = paramArrayOfByte;
    this.byteLimit = paramArrayOfByte.length;
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
  
  public int getPosition()
  {
    return this.byteOffset * 8 + this.bitOffset;
  }
  
  public boolean readBit()
  {
    if (((this.data[this.byteOffset] & 0xFF) >> this.bitOffset & 0x1) == 1) {}
    for (boolean bool = true;; bool = false)
    {
      skipBits(1);
      return bool;
    }
  }
  
  public int readBits(int paramInt)
  {
    int k = this.byteOffset;
    int j = Math.min(paramInt, 8 - this.bitOffset);
    byte[] arrayOfByte = this.data;
    int i = k + 1;
    k = (arrayOfByte[k] & 0xFF) >> this.bitOffset & 255 >> 8 - j;
    while (j < paramInt)
    {
      k |= (this.data[i] & 0xFF) << j;
      j += 8;
      i += 1;
    }
    skipBits(paramInt);
    return k & -1 >>> 32 - paramInt;
  }
  
  public void reset()
  {
    this.byteOffset = 0;
    this.bitOffset = 0;
  }
  
  public void setPosition(int paramInt)
  {
    this.byteOffset = (paramInt / 8);
    this.bitOffset = (paramInt - this.byteOffset * 8);
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
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/ogg/VorbisBitArray.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */