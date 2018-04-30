package ir.eitaa.messenger.exoplayer.text.eia608;

abstract class ClosedCaption
{
  public static final int TYPE_CTRL = 0;
  public static final int TYPE_TEXT = 1;
  public final int type;
  
  protected ClosedCaption(int paramInt)
  {
    this.type = paramInt;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/eia608/ClosedCaption.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */