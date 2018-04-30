package org.telegram.messenger.exoplayer2.source.dash;

import android.net.Uri;
import java.io.IOException;
import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.drm.DrmInitData;
import org.telegram.messenger.exoplayer2.extractor.ChunkIndex;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.mkv.MatroskaExtractor;
import org.telegram.messenger.exoplayer2.extractor.mp4.FragmentedMp4Extractor;
import org.telegram.messenger.exoplayer2.source.chunk.ChunkExtractorWrapper;
import org.telegram.messenger.exoplayer2.source.chunk.InitializationChunk;
import org.telegram.messenger.exoplayer2.source.dash.manifest.AdaptationSet;
import org.telegram.messenger.exoplayer2.source.dash.manifest.DashManifest;
import org.telegram.messenger.exoplayer2.source.dash.manifest.DashManifestParser;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Period;
import org.telegram.messenger.exoplayer2.source.dash.manifest.RangedUri;
import org.telegram.messenger.exoplayer2.source.dash.manifest.Representation;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.DataSpec;
import org.telegram.messenger.exoplayer2.upstream.ParsingLoadable;

public final class DashUtil
{
  private static Representation getFirstRepresentation(Period paramPeriod, int paramInt)
  {
    paramInt = paramPeriod.getAdaptationSetIndex(paramInt);
    if (paramInt == -1) {
      return null;
    }
    paramPeriod = ((AdaptationSet)paramPeriod.adaptationSets.get(paramInt)).representations;
    if (paramPeriod.isEmpty()) {}
    for (paramPeriod = null;; paramPeriod = (Representation)paramPeriod.get(0)) {
      return paramPeriod;
    }
  }
  
  public static ChunkIndex loadChunkIndex(DataSource paramDataSource, int paramInt, Representation paramRepresentation)
    throws IOException, InterruptedException
  {
    paramDataSource = loadInitializationData(paramDataSource, paramInt, paramRepresentation, true);
    if (paramDataSource == null) {
      return null;
    }
    return (ChunkIndex)paramDataSource.getSeekMap();
  }
  
  public static DrmInitData loadDrmInitData(DataSource paramDataSource, Period paramPeriod)
    throws IOException, InterruptedException
  {
    int i = 2;
    Representation localRepresentation = getFirstRepresentation(paramPeriod, 2);
    Object localObject = localRepresentation;
    if (localRepresentation == null)
    {
      i = 1;
      paramPeriod = getFirstRepresentation(paramPeriod, 1);
      localObject = paramPeriod;
      if (paramPeriod == null) {
        return null;
      }
    }
    paramPeriod = ((Representation)localObject).format;
    paramDataSource = loadSampleFormat(paramDataSource, i, (Representation)localObject);
    if (paramDataSource == null) {
      return paramPeriod.drmInitData;
    }
    return paramDataSource.copyWithManifestFormatInfo(paramPeriod).drmInitData;
  }
  
  private static ChunkExtractorWrapper loadInitializationData(DataSource paramDataSource, int paramInt, Representation paramRepresentation, boolean paramBoolean)
    throws IOException, InterruptedException
  {
    RangedUri localRangedUri2 = paramRepresentation.getInitializationUri();
    if (localRangedUri2 == null) {
      return null;
    }
    ChunkExtractorWrapper localChunkExtractorWrapper = newWrappedExtractor(paramInt, paramRepresentation.format);
    RangedUri localRangedUri3;
    if (paramBoolean)
    {
      localRangedUri3 = paramRepresentation.getIndexUri();
      if (localRangedUri3 == null) {
        return null;
      }
      RangedUri localRangedUri4 = localRangedUri2.attemptMerge(localRangedUri3, paramRepresentation.baseUrl);
      localRangedUri1 = localRangedUri4;
      if (localRangedUri4 == null) {
        loadInitializationData(paramDataSource, paramRepresentation, localChunkExtractorWrapper, localRangedUri2);
      }
    }
    for (RangedUri localRangedUri1 = localRangedUri3;; localRangedUri1 = localRangedUri2)
    {
      loadInitializationData(paramDataSource, paramRepresentation, localChunkExtractorWrapper, localRangedUri1);
      return localChunkExtractorWrapper;
    }
  }
  
  private static void loadInitializationData(DataSource paramDataSource, Representation paramRepresentation, ChunkExtractorWrapper paramChunkExtractorWrapper, RangedUri paramRangedUri)
    throws IOException, InterruptedException
  {
    new InitializationChunk(paramDataSource, new DataSpec(paramRangedUri.resolveUri(paramRepresentation.baseUrl), paramRangedUri.start, paramRangedUri.length, paramRepresentation.getCacheKey()), paramRepresentation.format, 0, null, paramChunkExtractorWrapper).load();
  }
  
  public static DashManifest loadManifest(DataSource paramDataSource, Uri paramUri)
    throws IOException
  {
    paramDataSource = new ParsingLoadable(paramDataSource, new DataSpec(paramUri, 3), 4, new DashManifestParser());
    paramDataSource.load();
    return (DashManifest)paramDataSource.getResult();
  }
  
  public static Format loadSampleFormat(DataSource paramDataSource, int paramInt, Representation paramRepresentation)
    throws IOException, InterruptedException
  {
    paramDataSource = loadInitializationData(paramDataSource, paramInt, paramRepresentation, false);
    if (paramDataSource == null) {
      return null;
    }
    return paramDataSource.getSampleFormats()[0];
  }
  
  private static ChunkExtractorWrapper newWrappedExtractor(int paramInt, Format paramFormat)
  {
    Object localObject = paramFormat.containerMimeType;
    int i;
    if ((((String)localObject).startsWith("video/webm")) || (((String)localObject).startsWith("audio/webm")))
    {
      i = 1;
      if (i == 0) {
        break label53;
      }
    }
    label53:
    for (localObject = new MatroskaExtractor();; localObject = new FragmentedMp4Extractor())
    {
      return new ChunkExtractorWrapper((Extractor)localObject, paramInt, paramFormat);
      i = 0;
      break;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/DashUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */