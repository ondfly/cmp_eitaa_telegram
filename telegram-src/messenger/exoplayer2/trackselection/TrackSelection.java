package org.telegram.messenger.exoplayer2.trackselection;

import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.source.TrackGroup;
import org.telegram.messenger.exoplayer2.source.chunk.MediaChunk;

public abstract interface TrackSelection
{
  public abstract boolean blacklist(int paramInt, long paramLong);
  
  public abstract void disable();
  
  public abstract void enable();
  
  public abstract int evaluateQueueSize(long paramLong, List<? extends MediaChunk> paramList);
  
  public abstract Format getFormat(int paramInt);
  
  public abstract int getIndexInTrackGroup(int paramInt);
  
  public abstract Format getSelectedFormat();
  
  public abstract int getSelectedIndex();
  
  public abstract int getSelectedIndexInTrackGroup();
  
  public abstract Object getSelectionData();
  
  public abstract int getSelectionReason();
  
  public abstract TrackGroup getTrackGroup();
  
  public abstract int indexOf(int paramInt);
  
  public abstract int indexOf(Format paramFormat);
  
  public abstract int length();
  
  public abstract void onPlaybackSpeed(float paramFloat);
  
  public abstract void updateSelectedTrack(long paramLong1, long paramLong2, long paramLong3);
  
  public static abstract interface Factory
  {
    public abstract TrackSelection createTrackSelection(TrackGroup paramTrackGroup, int... paramVarArgs);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/trackselection/TrackSelection.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */