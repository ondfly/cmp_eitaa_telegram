package ir.eitaa.messenger.exoplayer;

public final class CodecCounters
{
  public int codecInitCount;
  public int codecReleaseCount;
  public int droppedOutputBufferCount;
  public int inputBufferCount;
  public int maxConsecutiveDroppedOutputBufferCount;
  public int outputBuffersChangedCount;
  public int outputFormatChangedCount;
  public int renderedOutputBufferCount;
  public int skippedOutputBufferCount;
  
  public void ensureUpdated() {}
  
  public String getDebugString()
  {
    ensureUpdated();
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("cic:").append(this.codecInitCount);
    localStringBuilder.append(" crc:").append(this.codecReleaseCount);
    localStringBuilder.append(" ibc:").append(this.inputBufferCount);
    localStringBuilder.append(" ofc:").append(this.outputFormatChangedCount);
    localStringBuilder.append(" obc:").append(this.outputBuffersChangedCount);
    localStringBuilder.append(" ren:").append(this.renderedOutputBufferCount);
    localStringBuilder.append(" sob:").append(this.skippedOutputBufferCount);
    localStringBuilder.append(" dob:").append(this.droppedOutputBufferCount);
    localStringBuilder.append(" mcdob:").append(this.maxConsecutiveDroppedOutputBufferCount);
    return localStringBuilder.toString();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/CodecCounters.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */