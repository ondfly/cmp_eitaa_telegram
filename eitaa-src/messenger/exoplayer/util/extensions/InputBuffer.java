package ir.eitaa.messenger.exoplayer.util.extensions;

import ir.eitaa.messenger.exoplayer.SampleHolder;

public class InputBuffer
  extends Buffer
{
  public final SampleHolder sampleHolder = new SampleHolder(2);
  
  public void reset()
  {
    super.reset();
    this.sampleHolder.clearData();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/util/extensions/InputBuffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */