package ir.eitaa.messenger;

import java.io.OutputStream;

public class ByteArrayOutputStreamExpand
  extends OutputStream
{
  protected byte[] buf;
  protected int count;
  
  public ByteArrayOutputStreamExpand()
  {
    this.buf = new byte[32];
  }
  
  public ByteArrayOutputStreamExpand(int paramInt)
  {
    if (paramInt >= 0)
    {
      this.buf = new byte[paramInt];
      return;
    }
    throw new IllegalArgumentException("size < 0");
  }
  
  private void expand(int paramInt)
  {
    if (this.count + paramInt <= this.buf.length) {
      return;
    }
    byte[] arrayOfByte = new byte[this.count + paramInt];
    System.arraycopy(this.buf, 0, arrayOfByte, 0, this.count);
    this.buf = arrayOfByte;
  }
  
  public void checkOffsetAndCount(int paramInt1, int paramInt2, int paramInt3)
  {
    if (((paramInt2 | paramInt3) < 0) || (paramInt2 > paramInt1) || (paramInt1 - paramInt2 < paramInt3)) {
      throw new ArrayIndexOutOfBoundsException("length=" + paramInt1 + "; regionStart=" + paramInt2 + "; regionLength=" + paramInt3);
    }
  }
  
  public void reset()
  {
    try
    {
      this.count = 0;
      return;
    }
    finally
    {
      localObject = finally;
      throw ((Throwable)localObject);
    }
  }
  
  public int size()
  {
    return this.count;
  }
  
  public byte[] toByteArray()
  {
    return this.buf;
  }
  
  public String toString()
  {
    return new String(this.buf, 0, this.count);
  }
  
  public void write(int paramInt)
  {
    if (this.count == this.buf.length) {
      expand(1);
    }
    byte[] arrayOfByte = this.buf;
    int i = this.count;
    this.count = (i + 1);
    arrayOfByte[i] = ((byte)paramInt);
  }
  
  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    checkOffsetAndCount(paramArrayOfByte.length, paramInt1, paramInt2);
    if (paramInt2 == 0) {
      return;
    }
    expand(paramInt2);
    System.arraycopy(paramArrayOfByte, paramInt1, this.buf, this.count, paramInt2);
    this.count += paramInt2;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/ByteArrayOutputStreamExpand.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */