package ir.eitaa.messenger.audioinfo.m4a;

import ir.eitaa.messenger.audioinfo.util.PositionInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class MP4Input
  extends MP4Box<PositionInputStream>
{
  public MP4Input(InputStream paramInputStream)
  {
    super(new PositionInputStream(paramInputStream), null, "");
  }
  
  public MP4Atom nextChildUpTo(String paramString)
    throws IOException
  {
    MP4Atom localMP4Atom;
    do
    {
      localMP4Atom = nextChild();
    } while (!localMP4Atom.getType().matches(paramString));
    return localMP4Atom;
  }
  
  public String toString()
  {
    return "mp4[pos=" + getPosition() + "]";
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/audioinfo/m4a/MP4Input.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */