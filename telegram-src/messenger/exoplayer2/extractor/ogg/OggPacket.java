package org.telegram.messenger.exoplayer2.extractor.ogg;

import java.io.IOException;
import java.util.Arrays;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;

final class OggPacket
{
  private int currentSegmentIndex = -1;
  private final ParsableByteArray packetArray = new ParsableByteArray(new byte[65025], 0);
  private final OggPageHeader pageHeader = new OggPageHeader();
  private boolean populated;
  private int segmentCount;
  
  private int calculatePacketSize(int paramInt)
  {
    this.segmentCount = 0;
    int i = 0;
    int j;
    int k;
    do
    {
      j = i;
      if (this.segmentCount + paramInt >= this.pageHeader.pageSegmentCount) {
        break;
      }
      int[] arrayOfInt = this.pageHeader.laces;
      j = this.segmentCount;
      this.segmentCount = (j + 1);
      k = arrayOfInt[(j + paramInt)];
      j = i + k;
      i = j;
    } while (k == 255);
    return j;
  }
  
  public OggPageHeader getPageHeader()
  {
    return this.pageHeader;
  }
  
  public ParsableByteArray getPayload()
  {
    return this.packetArray;
  }
  
  public boolean populate(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    if (paramExtractorInput != null) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      if (this.populated)
      {
        this.populated = false;
        this.packetArray.reset();
      }
      if (this.populated) {
        break label300;
      }
      if (this.currentSegmentIndex >= 0) {
        break label140;
      }
      if (this.pageHeader.populate(paramExtractorInput, true)) {
        break;
      }
      return false;
    }
    int k = 0;
    int m = this.pageHeader.headerSize;
    int j = m;
    int i = k;
    if ((this.pageHeader.type & 0x1) == 1)
    {
      j = m;
      i = k;
      if (this.packetArray.limit() == 0)
      {
        j = m + calculatePacketSize(0);
        i = 0 + this.segmentCount;
      }
    }
    paramExtractorInput.skipFully(j);
    this.currentSegmentIndex = i;
    label140:
    i = calculatePacketSize(this.currentSegmentIndex);
    j = this.currentSegmentIndex + this.segmentCount;
    if (i > 0)
    {
      if (this.packetArray.capacity() < this.packetArray.limit() + i) {
        this.packetArray.data = Arrays.copyOf(this.packetArray.data, this.packetArray.limit() + i);
      }
      paramExtractorInput.readFully(this.packetArray.data, this.packetArray.limit(), i);
      this.packetArray.setLimit(this.packetArray.limit() + i);
      if (this.pageHeader.laces[(j - 1)] == 255) {
        break label294;
      }
    }
    label294:
    for (bool = true;; bool = false)
    {
      this.populated = bool;
      i = j;
      if (j == this.pageHeader.pageSegmentCount) {
        i = -1;
      }
      this.currentSegmentIndex = i;
      break;
    }
    label300:
    return true;
  }
  
  public void reset()
  {
    this.pageHeader.reset();
    this.packetArray.reset();
    this.currentSegmentIndex = -1;
    this.populated = false;
  }
  
  public void trimPayload()
  {
    if (this.packetArray.data.length == 65025) {
      return;
    }
    this.packetArray.data = Arrays.copyOf(this.packetArray.data, Math.max(65025, this.packetArray.limit()));
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/ogg/OggPacket.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */