package org.telegram.messenger.exoplayer2.trackselection;

import org.telegram.messenger.exoplayer2.RendererConfiguration;
import org.telegram.messenger.exoplayer2.source.TrackGroupArray;
import org.telegram.messenger.exoplayer2.util.Util;

public final class TrackSelectorResult
{
  public final TrackGroupArray groups;
  public final Object info;
  public final RendererConfiguration[] rendererConfigurations;
  public final boolean[] renderersEnabled;
  public final TrackSelectionArray selections;
  
  public TrackSelectorResult(TrackGroupArray paramTrackGroupArray, boolean[] paramArrayOfBoolean, TrackSelectionArray paramTrackSelectionArray, Object paramObject, RendererConfiguration[] paramArrayOfRendererConfiguration)
  {
    this.groups = paramTrackGroupArray;
    this.renderersEnabled = paramArrayOfBoolean;
    this.selections = paramTrackSelectionArray;
    this.info = paramObject;
    this.rendererConfigurations = paramArrayOfRendererConfiguration;
  }
  
  public boolean isEquivalent(TrackSelectorResult paramTrackSelectorResult)
  {
    if ((paramTrackSelectorResult == null) || (paramTrackSelectorResult.selections.length != this.selections.length)) {
      return false;
    }
    int i = 0;
    for (;;)
    {
      if (i >= this.selections.length) {
        break label52;
      }
      if (!isEquivalent(paramTrackSelectorResult, i)) {
        break;
      }
      i += 1;
    }
    label52:
    return true;
  }
  
  public boolean isEquivalent(TrackSelectorResult paramTrackSelectorResult, int paramInt)
  {
    if (paramTrackSelectorResult == null) {}
    while ((this.renderersEnabled[paramInt] != paramTrackSelectorResult.renderersEnabled[paramInt]) || (!Util.areEqual(this.selections.get(paramInt), paramTrackSelectorResult.selections.get(paramInt))) || (!Util.areEqual(this.rendererConfigurations[paramInt], paramTrackSelectorResult.rendererConfigurations[paramInt]))) {
      return false;
    }
    return true;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/trackselection/TrackSelectorResult.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */