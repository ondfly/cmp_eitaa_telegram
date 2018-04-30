package org.telegram.messenger.exoplayer2.source.dash.offline;

import android.net.Uri;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.telegram.messenger.exoplayer2.C;
import org.telegram.messenger.exoplayer2.offline.DownloadException;
import org.telegram.messenger.exoplayer2.offline.DownloaderConstructorHelper;
import org.telegram.messenger.exoplayer2.offline.SegmentDownloader;
import org.telegram.messenger.exoplayer2.offline.SegmentDownloader.Segment;
import org.telegram.messenger.exoplayer2.source.dash.DashSegmentIndex;
import org.telegram.messenger.exoplayer2.source.dash.DashUtil;
import org.telegram.messenger.exoplayer2.source.dash.DashWrappingSegmentIndex;
import org.telegram.messenger.exoplayer2.source.dash.manifest.AdaptationSet;
import org.telegram.messenger.exoplayer2.source.dash.manifest.DashManifest;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Period;
import org.telegram.messenger.exoplayer2.source.dash.manifest.RangedUri;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Representation;
import org.telegram.messenger.exoplayer2.source.dash.manifest.RepresentationKey;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;

public final class DashDownloader
  extends SegmentDownloader<DashManifest, RepresentationKey>
{
  public DashDownloader(Uri paramUri, DownloaderConstructorHelper paramDownloaderConstructorHelper)
  {
    super(paramUri, paramDownloaderConstructorHelper);
  }
  
  private static void addSegment(ArrayList<SegmentDownloader.Segment> paramArrayList, long paramLong, String paramString, RangedUri paramRangedUri)
  {
    paramArrayList.add(new SegmentDownloader.Segment(paramLong, new DataSpec(paramRangedUri.resolveUri(paramString), paramRangedUri.start, paramRangedUri.length, null)));
  }
  
  private DashSegmentIndex getSegmentIndex(DataSource paramDataSource, DashManifest paramDashManifest, RepresentationKey paramRepresentationKey)
    throws IOException, InterruptedException
  {
    paramDashManifest = (AdaptationSet)paramDashManifest.getPeriod(paramRepresentationKey.periodIndex).adaptationSets.get(paramRepresentationKey.adaptationSetIndex);
    paramRepresentationKey = (Representation)paramDashManifest.representations.get(paramRepresentationKey.representationIndex);
    DashSegmentIndex localDashSegmentIndex = paramRepresentationKey.getIndex();
    if (localDashSegmentIndex != null) {
      return localDashSegmentIndex;
    }
    paramDataSource = DashUtil.loadChunkIndex(paramDataSource, paramDashManifest.type, paramRepresentationKey);
    if (paramDataSource == null) {}
    for (paramDataSource = null;; paramDataSource = new DashWrappingSegmentIndex(paramDataSource)) {
      return paramDataSource;
    }
  }
  
  protected List<SegmentDownloader.Segment> getAllSegments(DataSource paramDataSource, DashManifest paramDashManifest, boolean paramBoolean)
    throws InterruptedException, IOException
  {
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    while (i < paramDashManifest.getPeriodCount())
    {
      List localList = paramDashManifest.getPeriod(i).adaptationSets;
      int j = 0;
      while (j < localList.size())
      {
        RepresentationKey[] arrayOfRepresentationKey = new RepresentationKey[((AdaptationSet)localList.get(j)).representations.size()];
        int k = 0;
        while (k < arrayOfRepresentationKey.length)
        {
          arrayOfRepresentationKey[k] = new RepresentationKey(i, j, k);
          k += 1;
        }
        localArrayList.addAll(getSegments(paramDataSource, paramDashManifest, arrayOfRepresentationKey, paramBoolean));
        j += 1;
      }
      i += 1;
    }
    return localArrayList;
  }
  
  public DashManifest getManifest(DataSource paramDataSource, Uri paramUri)
    throws IOException
  {
    return DashUtil.loadManifest(paramDataSource, paramUri);
  }
  
  protected List<SegmentDownloader.Segment> getSegments(DataSource paramDataSource, DashManifest paramDashManifest, RepresentationKey[] paramArrayOfRepresentationKey, boolean paramBoolean)
    throws InterruptedException, IOException
  {
    ArrayList localArrayList = new ArrayList();
    int m = paramArrayOfRepresentationKey.length;
    int i = 0;
    if (i < m)
    {
      Object localObject1 = paramArrayOfRepresentationKey[i];
      try
      {
        DashSegmentIndex localDashSegmentIndex = getSegmentIndex(paramDataSource, paramDashManifest, (RepresentationKey)localObject1);
        if (localDashSegmentIndex == null) {
          throw new DownloadException("No index for representation: " + localObject1);
        }
      }
      catch (IOException localIOException)
      {
        if (!paramBoolean) {}
      }
      for (;;)
      {
        i += 1;
        break;
        throw localIOException;
        int n = localIOException.getSegmentCount(-9223372036854775807L);
        if (n == -1) {
          throw new DownloadException("Unbounded index for representation: " + localObject1);
        }
        Object localObject2 = paramDashManifest.getPeriod(((RepresentationKey)localObject1).periodIndex);
        localObject1 = (Representation)((AdaptationSet)((Period)localObject2).adaptationSets.get(((RepresentationKey)localObject1).adaptationSetIndex)).representations.get(((RepresentationKey)localObject1).representationIndex);
        long l = C.msToUs(((Period)localObject2).startMs);
        localObject2 = ((Representation)localObject1).baseUrl;
        RangedUri localRangedUri = ((Representation)localObject1).getInitializationUri();
        if (localRangedUri != null) {
          addSegment(localArrayList, l, (String)localObject2, localRangedUri);
        }
        localObject1 = ((Representation)localObject1).getIndexUri();
        if (localObject1 != null) {
          addSegment(localArrayList, l, (String)localObject2, (RangedUri)localObject1);
        }
        int k = localIOException.getFirstSegmentNum();
        int j = k;
        while (j <= k + n - 1)
        {
          addSegment(localArrayList, localIOException.getTimeUs(j) + l, (String)localObject2, localIOException.getSegmentUrl(j));
          j += 1;
        }
      }
    }
    return localArrayList;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/offline/DashDownloader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */