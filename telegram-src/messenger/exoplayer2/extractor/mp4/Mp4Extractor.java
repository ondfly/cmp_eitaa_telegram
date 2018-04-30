package org.telegram.messenger.exoplayer2.extractor.mp4;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.ParserException;
import org.telegram.messenger.exoplayer2.extractor.Extractor;
import org.telegram.messenger.exoplayer2.extractor.ExtractorInput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorOutput;
import org.telegram.messenger.exoplayer2.extractor.ExtractorsFactory;
import org.telegram.messenger.exoplayer2.extractor.GaplessInfoHolder;
import org.telegram.messenger.exoplayer2.extractor.PositionHolder;
import org.telegram.messenger.exoplayer2.extractor.SeekMap;
import org.telegram.messenger.exoplayer2.extractor.SeekMap.SeekPoints;
import org.telegram.messenger.exoplayer2.extractor.SeekPoint;
import org.telegram.messenger.exoplayer2.extractor.TrackOutput;
import org.telegram.messenger.exoplayer2.metadata.Metadata;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.NalUnitUtil;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.Util;

public final class Mp4Extractor
  implements Extractor, SeekMap
{
  private static final int BRAND_QUICKTIME = Util.getIntegerCodeForString("qt  ");
  public static final ExtractorsFactory FACTORY = new ExtractorsFactory()
  {
    public Extractor[] createExtractors()
    {
      return new Extractor[] { new Mp4Extractor() };
    }
  };
  public static final int FLAG_WORKAROUND_IGNORE_EDIT_LISTS = 1;
  private static final long MAXIMUM_READ_AHEAD_BYTES_STREAM = 10485760L;
  private static final long RELOAD_MINIMUM_SEEK_DISTANCE = 262144L;
  private static final int STATE_READING_ATOM_HEADER = 0;
  private static final int STATE_READING_ATOM_PAYLOAD = 1;
  private static final int STATE_READING_SAMPLE = 2;
  private long[][] accumulatedSampleSizes;
  private ParsableByteArray atomData;
  private final ParsableByteArray atomHeader;
  private int atomHeaderBytesRead;
  private long atomSize;
  private int atomType;
  private final Stack<Atom.ContainerAtom> containerAtoms;
  private long durationUs;
  private ExtractorOutput extractorOutput;
  private int firstVideoTrackIndex;
  private final int flags;
  private boolean isQuickTime;
  private final ParsableByteArray nalLength;
  private final ParsableByteArray nalStartCode;
  private int parserState;
  private int sampleBytesWritten;
  private int sampleCurrentNalBytesRemaining;
  private int sampleTrackIndex;
  private Mp4Track[] tracks;
  
  public Mp4Extractor()
  {
    this(0);
  }
  
  public Mp4Extractor(int paramInt)
  {
    this.flags = paramInt;
    this.atomHeader = new ParsableByteArray(16);
    this.containerAtoms = new Stack();
    this.nalStartCode = new ParsableByteArray(NalUnitUtil.NAL_START_CODE);
    this.nalLength = new ParsableByteArray(4);
    this.sampleTrackIndex = -1;
  }
  
  private static long[][] calculateAccumulatedSampleSizes(Mp4Track[] paramArrayOfMp4Track)
  {
    long[][] arrayOfLong = new long[paramArrayOfMp4Track.length][];
    int[] arrayOfInt = new int[paramArrayOfMp4Track.length];
    long[] arrayOfLong1 = new long[paramArrayOfMp4Track.length];
    boolean[] arrayOfBoolean = new boolean[paramArrayOfMp4Track.length];
    int i = 0;
    while (i < paramArrayOfMp4Track.length)
    {
      arrayOfLong[i] = new long[paramArrayOfMp4Track[i].sampleTable.sampleCount];
      arrayOfLong1[i] = paramArrayOfMp4Track[i].sampleTable.timestampsUs[0];
      i += 1;
    }
    long l1 = 0L;
    int j = 0;
    while (j < paramArrayOfMp4Track.length)
    {
      long l2 = Long.MAX_VALUE;
      int k = -1;
      i = 0;
      while (i < paramArrayOfMp4Track.length)
      {
        int m = k;
        long l3 = l2;
        if (arrayOfBoolean[i] == 0)
        {
          m = k;
          l3 = l2;
          if (arrayOfLong1[i] <= l2)
          {
            m = i;
            l3 = arrayOfLong1[i];
          }
        }
        i += 1;
        k = m;
        l2 = l3;
      }
      i = arrayOfInt[k];
      arrayOfLong[k][i] = l1;
      l1 += paramArrayOfMp4Track[k].sampleTable.sizes[i];
      i += 1;
      arrayOfInt[k] = i;
      if (i < arrayOfLong[k].length)
      {
        arrayOfLong1[k] = paramArrayOfMp4Track[k].sampleTable.timestampsUs[i];
      }
      else
      {
        arrayOfBoolean[k] = true;
        j += 1;
      }
    }
    return arrayOfLong;
  }
  
  private void enterReadingAtomHeaderState()
  {
    this.parserState = 0;
    this.atomHeaderBytesRead = 0;
  }
  
  private static int getSynchronizationSampleIndex(TrackSampleTable paramTrackSampleTable, long paramLong)
  {
    int j = paramTrackSampleTable.getIndexOfEarlierOrEqualSynchronizationSample(paramLong);
    int i = j;
    if (j == -1) {
      i = paramTrackSampleTable.getIndexOfLaterOrEqualSynchronizationSample(paramLong);
    }
    return i;
  }
  
  private int getTrackIndexOfNextReadSample(long paramLong)
  {
    long l4 = Long.MAX_VALUE;
    int n = 1;
    int j = -1;
    long l3 = Long.MAX_VALUE;
    long l5 = Long.MAX_VALUE;
    int i3 = 1;
    int m = -1;
    int i = 0;
    while (i < this.tracks.length)
    {
      Mp4Track localMp4Track = this.tracks[i];
      int k = localMp4Track.sampleIndex;
      long l7;
      if (k == localMp4Track.sampleTable.sampleCount)
      {
        l7 = l3;
        l3 = l5;
        i += 1;
        l5 = l3;
        l3 = l7;
      }
      else
      {
        long l1 = localMp4Track.sampleTable.offsets[k];
        long l6 = this.accumulatedSampleSizes[i][k];
        l7 = l1 - paramLong;
        if ((l7 < 0L) || (l7 >= 262144L)) {}
        for (k = 1;; k = 0)
        {
          long l2;
          int i2;
          int i1;
          if ((k != 0) || (n == 0))
          {
            l2 = l3;
            i2 = n;
            i1 = j;
            l1 = l4;
            if (k == n)
            {
              l2 = l3;
              i2 = n;
              i1 = j;
              l1 = l4;
              if (l7 >= l4) {}
            }
          }
          else
          {
            i2 = k;
            l1 = l7;
            i1 = i;
            l2 = l6;
          }
          l3 = l5;
          l7 = l2;
          n = i2;
          j = i1;
          l4 = l1;
          if (l6 >= l5) {
            break;
          }
          l3 = l6;
          m = i;
          i3 = k;
          l7 = l2;
          n = i2;
          j = i1;
          l4 = l1;
          break;
        }
      }
    }
    if ((l5 == Long.MAX_VALUE) || (i3 == 0) || (l3 < 10485760L + l5)) {
      m = j;
    }
    return m;
  }
  
  private static long maybeAdjustSeekOffset(TrackSampleTable paramTrackSampleTable, long paramLong1, long paramLong2)
  {
    int i = getSynchronizationSampleIndex(paramTrackSampleTable, paramLong1);
    if (i == -1) {
      return paramLong2;
    }
    return Math.min(paramTrackSampleTable.offsets[i], paramLong2);
  }
  
  private void processAtomEnded(long paramLong)
    throws ParserException
  {
    while ((!this.containerAtoms.isEmpty()) && (((Atom.ContainerAtom)this.containerAtoms.peek()).endPosition == paramLong))
    {
      Atom.ContainerAtom localContainerAtom = (Atom.ContainerAtom)this.containerAtoms.pop();
      if (localContainerAtom.type == Atom.TYPE_moov)
      {
        processMoovAtom(localContainerAtom);
        this.containerAtoms.clear();
        this.parserState = 2;
      }
      else if (!this.containerAtoms.isEmpty())
      {
        ((Atom.ContainerAtom)this.containerAtoms.peek()).add(localContainerAtom);
      }
    }
    if (this.parserState != 2) {
      enterReadingAtomHeaderState();
    }
  }
  
  private static boolean processFtypAtom(ParsableByteArray paramParsableByteArray)
  {
    paramParsableByteArray.setPosition(8);
    if (paramParsableByteArray.readInt() == BRAND_QUICKTIME) {
      return true;
    }
    paramParsableByteArray.skipBytes(4);
    while (paramParsableByteArray.bytesLeft() > 0) {
      if (paramParsableByteArray.readInt() == BRAND_QUICKTIME) {
        return true;
      }
    }
    return false;
  }
  
  private void processMoovAtom(Atom.ContainerAtom paramContainerAtom)
    throws ParserException
  {
    int i = -1;
    long l1 = -9223372036854775807L;
    ArrayList localArrayList = new ArrayList();
    Object localObject3 = null;
    GaplessInfoHolder localGaplessInfoHolder = new GaplessInfoHolder();
    Object localObject1 = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_udta);
    if (localObject1 != null)
    {
      localObject1 = AtomParsers.parseUdta((Atom.LeafAtom)localObject1, this.isQuickTime);
      localObject3 = localObject1;
      if (localObject1 != null)
      {
        localGaplessInfoHolder.setFromMetadata((Metadata)localObject1);
        localObject3 = localObject1;
      }
    }
    int k = 0;
    while (k < paramContainerAtom.containerChildren.size())
    {
      localObject1 = (Atom.ContainerAtom)paramContainerAtom.containerChildren.get(k);
      long l2;
      int j;
      if (((Atom.ContainerAtom)localObject1).type != Atom.TYPE_trak)
      {
        l2 = l1;
        j = i;
        k += 1;
        i = j;
        l1 = l2;
      }
      else
      {
        Object localObject2 = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_mvhd);
        if ((this.flags & 0x1) != 0) {}
        for (boolean bool = true;; bool = false)
        {
          Track localTrack = AtomParsers.parseTrak((Atom.ContainerAtom)localObject1, (Atom.LeafAtom)localObject2, -9223372036854775807L, null, bool, this.isQuickTime);
          j = i;
          l2 = l1;
          if (localTrack == null) {
            break;
          }
          localObject1 = AtomParsers.parseStbl(localTrack, ((Atom.ContainerAtom)localObject1).getContainerAtomOfType(Atom.TYPE_mdia).getContainerAtomOfType(Atom.TYPE_minf).getContainerAtomOfType(Atom.TYPE_stbl), localGaplessInfoHolder);
          j = i;
          l2 = l1;
          if (((TrackSampleTable)localObject1).sampleCount == 0) {
            break;
          }
          Mp4Track localMp4Track = new Mp4Track(localTrack, (TrackSampleTable)localObject1, this.extractorOutput.track(k, localTrack.type));
          j = ((TrackSampleTable)localObject1).maximumSize;
          Format localFormat = localTrack.format.copyWithMaxInputSize(j + 30);
          localObject2 = localFormat;
          if (localTrack.type == 1)
          {
            localObject1 = localFormat;
            if (localGaplessInfoHolder.hasGaplessInfo()) {
              localObject1 = localFormat.copyWithGaplessInfo(localGaplessInfoHolder.encoderDelay, localGaplessInfoHolder.encoderPadding);
            }
            localObject2 = localObject1;
            if (localObject3 != null) {
              localObject2 = ((Format)localObject1).copyWithMetadata((Metadata)localObject3);
            }
          }
          localMp4Track.trackOutput.format((Format)localObject2);
          l2 = Math.max(l1, localTrack.durationUs);
          j = i;
          if (localTrack.type == 2)
          {
            j = i;
            if (i == -1) {
              j = localArrayList.size();
            }
          }
          localArrayList.add(localMp4Track);
          break;
        }
      }
    }
    this.firstVideoTrackIndex = i;
    this.durationUs = l1;
    this.tracks = ((Mp4Track[])localArrayList.toArray(new Mp4Track[localArrayList.size()]));
    this.accumulatedSampleSizes = calculateAccumulatedSampleSizes(this.tracks);
    this.extractorOutput.endTracks();
    this.extractorOutput.seekMap(this);
  }
  
  private boolean readAtomHeader(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    if (this.atomHeaderBytesRead == 0)
    {
      if (!paramExtractorInput.readFully(this.atomHeader.data, 0, 8, true)) {
        return false;
      }
      this.atomHeaderBytesRead = 8;
      this.atomHeader.setPosition(0);
      this.atomSize = this.atomHeader.readUnsignedInt();
      this.atomType = this.atomHeader.readInt();
    }
    if (this.atomSize == 1L)
    {
      paramExtractorInput.readFully(this.atomHeader.data, 8, 8);
      this.atomHeaderBytesRead += 8;
      this.atomSize = this.atomHeader.readUnsignedLongToLong();
    }
    long l1;
    while (this.atomSize < this.atomHeaderBytesRead)
    {
      throw new ParserException("Atom size less than header length (unsupported).");
      if (this.atomSize == 0L)
      {
        long l2 = paramExtractorInput.getLength();
        l1 = l2;
        if (l2 == -1L)
        {
          l1 = l2;
          if (!this.containerAtoms.isEmpty()) {
            l1 = ((Atom.ContainerAtom)this.containerAtoms.peek()).endPosition;
          }
        }
        if (l1 != -1L) {
          this.atomSize = (l1 - paramExtractorInput.getPosition() + this.atomHeaderBytesRead);
        }
      }
    }
    if (shouldParseContainerAtom(this.atomType))
    {
      l1 = paramExtractorInput.getPosition() + this.atomSize - this.atomHeaderBytesRead;
      this.containerAtoms.add(new Atom.ContainerAtom(this.atomType, l1));
      if (this.atomSize == this.atomHeaderBytesRead) {
        processAtomEnded(l1);
      }
    }
    for (;;)
    {
      return true;
      enterReadingAtomHeaderState();
      continue;
      if (shouldParseLeafAtom(this.atomType))
      {
        if (this.atomHeaderBytesRead == 8)
        {
          bool = true;
          label319:
          Assertions.checkState(bool);
          if (this.atomSize > 2147483647L) {
            break label394;
          }
        }
        label394:
        for (boolean bool = true;; bool = false)
        {
          Assertions.checkState(bool);
          this.atomData = new ParsableByteArray((int)this.atomSize);
          System.arraycopy(this.atomHeader.data, 0, this.atomData.data, 0, 8);
          this.parserState = 1;
          break;
          bool = false;
          break label319;
        }
      }
      this.atomData = null;
      this.parserState = 1;
    }
  }
  
  private boolean readAtomPayload(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    long l1 = this.atomSize - this.atomHeaderBytesRead;
    long l2 = paramExtractorInput.getPosition();
    int j = 0;
    int i;
    if (this.atomData != null)
    {
      paramExtractorInput.readFully(this.atomData.data, this.atomHeaderBytesRead, (int)l1);
      if (this.atomType == Atom.TYPE_ftyp)
      {
        this.isQuickTime = processFtypAtom(this.atomData);
        i = j;
      }
    }
    for (;;)
    {
      processAtomEnded(l2 + l1);
      if ((i == 0) || (this.parserState == 2)) {
        break;
      }
      return true;
      i = j;
      if (!this.containerAtoms.isEmpty())
      {
        ((Atom.ContainerAtom)this.containerAtoms.peek()).add(new Atom.LeafAtom(this.atomType, this.atomData));
        i = j;
        continue;
        if (l1 < 262144L)
        {
          paramExtractorInput.skipFully((int)l1);
          i = j;
        }
        else
        {
          paramPositionHolder.position = (paramExtractorInput.getPosition() + l1);
          i = 1;
        }
      }
    }
    return false;
  }
  
  private int readSample(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    long l2 = paramExtractorInput.getPosition();
    if (this.sampleTrackIndex == -1)
    {
      this.sampleTrackIndex = getTrackIndexOfNextReadSample(l2);
      if (this.sampleTrackIndex == -1) {
        return -1;
      }
    }
    Mp4Track localMp4Track = this.tracks[this.sampleTrackIndex];
    TrackOutput localTrackOutput = localMp4Track.trackOutput;
    int k = localMp4Track.sampleIndex;
    long l1 = localMp4Track.sampleTable.offsets[k];
    int j = localMp4Track.sampleTable.sizes[k];
    l2 = l1 - l2 + this.sampleBytesWritten;
    if ((l2 < 0L) || (l2 >= 262144L))
    {
      paramPositionHolder.position = l1;
      return 1;
    }
    int i = j;
    l1 = l2;
    if (localMp4Track.track.sampleTransformation == 1)
    {
      l1 = l2 + 8L;
      i = j - 8;
    }
    paramExtractorInput.skipFully((int)l1);
    if (localMp4Track.track.nalUnitLengthFieldLength != 0)
    {
      paramPositionHolder = this.nalLength.data;
      paramPositionHolder[0] = 0;
      paramPositionHolder[1] = 0;
      paramPositionHolder[2] = 0;
      int m = localMp4Track.track.nalUnitLengthFieldLength;
      int n = 4 - localMp4Track.track.nalUnitLengthFieldLength;
      for (;;)
      {
        j = i;
        if (this.sampleBytesWritten >= i) {
          break;
        }
        if (this.sampleCurrentNalBytesRemaining == 0)
        {
          paramExtractorInput.readFully(this.nalLength.data, n, m);
          this.nalLength.setPosition(0);
          this.sampleCurrentNalBytesRemaining = this.nalLength.readUnsignedIntToInt();
          this.nalStartCode.setPosition(0);
          localTrackOutput.sampleData(this.nalStartCode, 4);
          this.sampleBytesWritten += 4;
          i += n;
        }
        else
        {
          j = localTrackOutput.sampleData(paramExtractorInput, this.sampleCurrentNalBytesRemaining, false);
          this.sampleBytesWritten += j;
          this.sampleCurrentNalBytesRemaining -= j;
        }
      }
    }
    for (;;)
    {
      j = i;
      if (this.sampleBytesWritten >= i) {
        break;
      }
      j = localTrackOutput.sampleData(paramExtractorInput, i - this.sampleBytesWritten, false);
      this.sampleBytesWritten += j;
      this.sampleCurrentNalBytesRemaining -= j;
    }
    localTrackOutput.sampleMetadata(localMp4Track.sampleTable.timestampsUs[k], localMp4Track.sampleTable.flags[k], j, 0, null);
    localMp4Track.sampleIndex += 1;
    this.sampleTrackIndex = -1;
    this.sampleBytesWritten = 0;
    this.sampleCurrentNalBytesRemaining = 0;
    return 0;
  }
  
  private static boolean shouldParseContainerAtom(int paramInt)
  {
    return (paramInt == Atom.TYPE_moov) || (paramInt == Atom.TYPE_trak) || (paramInt == Atom.TYPE_mdia) || (paramInt == Atom.TYPE_minf) || (paramInt == Atom.TYPE_stbl) || (paramInt == Atom.TYPE_edts);
  }
  
  private static boolean shouldParseLeafAtom(int paramInt)
  {
    return (paramInt == Atom.TYPE_mdhd) || (paramInt == Atom.TYPE_mvhd) || (paramInt == Atom.TYPE_hdlr) || (paramInt == Atom.TYPE_stsd) || (paramInt == Atom.TYPE_stts) || (paramInt == Atom.TYPE_stss) || (paramInt == Atom.TYPE_ctts) || (paramInt == Atom.TYPE_elst) || (paramInt == Atom.TYPE_stsc) || (paramInt == Atom.TYPE_stsz) || (paramInt == Atom.TYPE_stz2) || (paramInt == Atom.TYPE_stco) || (paramInt == Atom.TYPE_co64) || (paramInt == Atom.TYPE_tkhd) || (paramInt == Atom.TYPE_ftyp) || (paramInt == Atom.TYPE_udta);
  }
  
  private void updateSampleIndices(long paramLong)
  {
    Mp4Track[] arrayOfMp4Track = this.tracks;
    int m = arrayOfMp4Track.length;
    int i = 0;
    while (i < m)
    {
      Mp4Track localMp4Track = arrayOfMp4Track[i];
      TrackSampleTable localTrackSampleTable = localMp4Track.sampleTable;
      int k = localTrackSampleTable.getIndexOfEarlierOrEqualSynchronizationSample(paramLong);
      int j = k;
      if (k == -1) {
        j = localTrackSampleTable.getIndexOfLaterOrEqualSynchronizationSample(paramLong);
      }
      localMp4Track.sampleIndex = j;
      i += 1;
    }
  }
  
  public long getDurationUs()
  {
    return this.durationUs;
  }
  
  public SeekMap.SeekPoints getSeekPoints(long paramLong)
  {
    if (this.tracks.length == 0) {
      return new SeekMap.SeekPoints(SeekPoint.START);
    }
    long l6 = -9223372036854775807L;
    long l7 = -1L;
    int i;
    long l5;
    long l1;
    long l4;
    long l2;
    long l3;
    if (this.firstVideoTrackIndex != -1)
    {
      localObject = this.tracks[this.firstVideoTrackIndex].sampleTable;
      i = getSynchronizationSampleIndex((TrackSampleTable)localObject, paramLong);
      if (i == -1) {
        return new SeekMap.SeekPoints(SeekPoint.START);
      }
      long l9 = localObject.timestampsUs[i];
      l5 = l9;
      long l8 = localObject.offsets[i];
      l1 = l8;
      l4 = l5;
      l2 = l7;
      l3 = l6;
      if (l9 < paramLong)
      {
        l1 = l8;
        l4 = l5;
        l2 = l7;
        l3 = l6;
        if (i < ((TrackSampleTable)localObject).sampleCount - 1)
        {
          int j = ((TrackSampleTable)localObject).getIndexOfLaterOrEqualSynchronizationSample(paramLong);
          l1 = l8;
          l4 = l5;
          l2 = l7;
          l3 = l6;
          if (j != -1)
          {
            l1 = l8;
            l4 = l5;
            l2 = l7;
            l3 = l6;
            if (j != i)
            {
              l3 = localObject.timestampsUs[j];
              l2 = localObject.offsets[j];
              l4 = l5;
              l1 = l8;
            }
          }
        }
      }
    }
    for (;;)
    {
      i = 0;
      l5 = l1;
      while (i < this.tracks.length)
      {
        paramLong = l5;
        l1 = l2;
        if (i != this.firstVideoTrackIndex)
        {
          localObject = this.tracks[i].sampleTable;
          l5 = maybeAdjustSeekOffset((TrackSampleTable)localObject, l4, l5);
          paramLong = l5;
          l1 = l2;
          if (l3 != -9223372036854775807L)
          {
            l1 = maybeAdjustSeekOffset((TrackSampleTable)localObject, l3, l2);
            paramLong = l5;
          }
        }
        i += 1;
        l5 = paramLong;
        l2 = l1;
      }
      l1 = Long.MAX_VALUE;
      l4 = paramLong;
      l2 = l7;
      l3 = l6;
    }
    Object localObject = new SeekPoint(l4, l5);
    if (l3 == -9223372036854775807L) {
      return new SeekMap.SeekPoints((SeekPoint)localObject);
    }
    return new SeekMap.SeekPoints((SeekPoint)localObject, new SeekPoint(l3, l2));
  }
  
  public void init(ExtractorOutput paramExtractorOutput)
  {
    this.extractorOutput = paramExtractorOutput;
  }
  
  public boolean isSeekable()
  {
    return true;
  }
  
  public int read(ExtractorInput paramExtractorInput, PositionHolder paramPositionHolder)
    throws IOException, InterruptedException
  {
    do
    {
      do
      {
        switch (this.parserState)
        {
        default: 
          throw new IllegalStateException();
        }
      } while (readAtomHeader(paramExtractorInput));
      return -1;
    } while (!readAtomPayload(paramExtractorInput, paramPositionHolder));
    return 1;
    return readSample(paramExtractorInput, paramPositionHolder);
  }
  
  public void release() {}
  
  public void seek(long paramLong1, long paramLong2)
  {
    this.containerAtoms.clear();
    this.atomHeaderBytesRead = 0;
    this.sampleTrackIndex = -1;
    this.sampleBytesWritten = 0;
    this.sampleCurrentNalBytesRemaining = 0;
    if (paramLong1 == 0L) {
      enterReadingAtomHeaderState();
    }
    while (this.tracks == null) {
      return;
    }
    updateSampleIndices(paramLong2);
  }
  
  public boolean sniff(ExtractorInput paramExtractorInput)
    throws IOException, InterruptedException
  {
    return Sniffer.sniffUnfragmented(paramExtractorInput);
  }
  
  @Retention(RetentionPolicy.SOURCE)
  public static @interface Flags {}
  
  private static final class Mp4Track
  {
    public int sampleIndex;
    public final TrackSampleTable sampleTable;
    public final Track track;
    public final TrackOutput trackOutput;
    
    public Mp4Track(Track paramTrack, TrackSampleTable paramTrackSampleTable, TrackOutput paramTrackOutput)
    {
      this.track = paramTrack;
      this.sampleTable = paramTrackSampleTable;
      this.trackOutput = paramTrackOutput;
    }
  }
  
  @Retention(RetentionPolicy.SOURCE)
  private static @interface State {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/mp4/Mp4Extractor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */