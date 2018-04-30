package org.telegram.messenger.exoplayer2.source.smoothstreaming.offline;

import android.net.Uri;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.telegram.messenger.exoplayer2.offline.DownloaderConstructorHelper;
import org.telegram.messenger.exoplayer2.offline.SegmentDownloader;
import org.telegram.messenger.exoplayer2.offline.SegmentDownloader.Segment;
import org.telegram.messenger.exoplayer2.source.smoothstreaming.manifest.SsManifest;
import org.telegram.messenger.exoplayer2.source.smoothstreaming.manifest.SsManifest.StreamElement;
import org.telegram.messenger.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import org.telegram.messenger.exoplayer2.source.smoothstreaming.manifest.TrackKey;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.ParsingLoadable;

public final class SsDownloader
  extends SegmentDownloader<SsManifest, TrackKey>
{
  public SsDownloader(Uri paramUri, DownloaderConstructorHelper paramDownloaderConstructorHelper)
  {
    super(paramUri, paramDownloaderConstructorHelper);
  }
  
  protected List<SegmentDownloader.Segment> getAllSegments(DataSource paramDataSource, SsManifest paramSsManifest, boolean paramBoolean)
    throws InterruptedException, IOException
  {
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    while (i < paramSsManifest.streamElements.length)
    {
      SsManifest.StreamElement localStreamElement = paramSsManifest.streamElements[i];
      int j = 0;
      while (j < localStreamElement.formats.length)
      {
        localArrayList.addAll(getSegments(paramDataSource, paramSsManifest, new TrackKey[] { new TrackKey(i, j) }, paramBoolean));
        j += 1;
      }
      i += 1;
    }
    return localArrayList;
  }
  
  public SsManifest getManifest(DataSource paramDataSource, Uri paramUri)
    throws IOException
  {
    paramDataSource = new ParsingLoadable(paramDataSource, new DataSpec(paramUri, 3), 4, new SsManifestParser());
    paramDataSource.load();
    return (SsManifest)paramDataSource.getResult();
  }
  
  protected List<SegmentDownloader.Segment> getSegments(DataSource paramDataSource, SsManifest paramSsManifest, TrackKey[] paramArrayOfTrackKey, boolean paramBoolean)
    throws InterruptedException, IOException
  {
    paramDataSource = new ArrayList();
    int k = paramArrayOfTrackKey.length;
    int i = 0;
    while (i < k)
    {
      TrackKey localTrackKey = paramArrayOfTrackKey[i];
      SsManifest.StreamElement localStreamElement = paramSsManifest.streamElements[localTrackKey.streamElementIndex];
      int j = 0;
      while (j < localStreamElement.chunkCount)
      {
        paramDataSource.add(new SegmentDownloader.Segment(localStreamElement.getStartTimeUs(j), new DataSpec(localStreamElement.buildRequestUri(localTrackKey.trackIndex, j))));
        j += 1;
      }
      i += 1;
    }
    return paramDataSource;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/smoothstreaming/offline/SsDownloader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */