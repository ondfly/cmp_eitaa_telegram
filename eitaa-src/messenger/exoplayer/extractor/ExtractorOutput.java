package ir.eitaa.messenger.exoplayer.extractor;

import ir.eitaa.messenger.exoplayer.drm.DrmInitData;

public abstract interface ExtractorOutput
{
  public abstract void drmInitData(DrmInitData paramDrmInitData);
  
  public abstract void endTracks();
  
  public abstract void seekMap(SeekMap paramSeekMap);
  
  public abstract TrackOutput track(int paramInt);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/extractor/ExtractorOutput.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */