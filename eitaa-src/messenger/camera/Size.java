package ir.eitaa.messenger.camera;

public final class Size
{
  private final int mHeight;
  private final int mWidth;
  
  public Size(int paramInt1, int paramInt2)
  {
    this.mWidth = paramInt1;
    this.mHeight = paramInt2;
  }
  
  private static NumberFormatException invalidSize(String paramString)
  {
    throw new NumberFormatException("Invalid Size: \"" + paramString + "\"");
  }
  
  public static Size parseSize(String paramString)
    throws NumberFormatException
  {
    int j = paramString.indexOf('*');
    int i = j;
    if (j < 0) {
      i = paramString.indexOf('x');
    }
    if (i < 0) {
      throw invalidSize(paramString);
    }
    try
    {
      Size localSize = new Size(Integer.parseInt(paramString.substring(0, i)), Integer.parseInt(paramString.substring(i + 1)));
      return localSize;
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw invalidSize(paramString);
    }
  }
  
  public boolean equals(Object paramObject)
  {
    boolean bool = true;
    if (paramObject == null) {}
    do
    {
      return false;
      if (this == paramObject) {
        return true;
      }
    } while (!(paramObject instanceof Size));
    paramObject = (Size)paramObject;
    if ((this.mWidth == ((Size)paramObject).mWidth) && (this.mHeight == ((Size)paramObject).mHeight)) {}
    for (;;)
    {
      return bool;
      bool = false;
    }
  }
  
  public int getHeight()
  {
    return this.mHeight;
  }
  
  public int getWidth()
  {
    return this.mWidth;
  }
  
  public int hashCode()
  {
    return this.mHeight ^ (this.mWidth << 16 | this.mWidth >>> 16);
  }
  
  public String toString()
  {
    return this.mWidth + "x" + this.mHeight;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/camera/Size.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */