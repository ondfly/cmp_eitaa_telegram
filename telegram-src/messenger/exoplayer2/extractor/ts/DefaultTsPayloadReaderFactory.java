package org.telegram.messenger.exoplayer2.extractor.ts;

import android.util.SparseArray;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;

public final class DefaultTsPayloadReaderFactory
  implements TsPayloadReader.Factory
{
  private static final int DESCRIPTOR_TAG_CAPTION_SERVICE = 134;
  public static final int FLAG_ALLOW_NON_IDR_KEYFRAMES = 1;
  public static final int FLAG_DETECT_ACCESS_UNITS = 8;
  public static final int FLAG_IGNORE_AAC_STREAM = 2;
  public static final int FLAG_IGNORE_H264_STREAM = 4;
  public static final int FLAG_IGNORE_SPLICE_INFO_STREAM = 16;
  public static final int FLAG_OVERRIDE_CAPTION_DESCRIPTORS = 32;
  private final List<Format> closedCaptionFormats;
  private final int flags;
  
  public DefaultTsPayloadReaderFactory()
  {
    this(0);
  }
  
  public DefaultTsPayloadReaderFactory(int paramInt)
  {
    this(paramInt, Collections.emptyList());
  }
  
  public DefaultTsPayloadReaderFactory(int paramInt, List<Format> paramList)
  {
    this.flags = paramInt;
    Object localObject = paramList;
    if (!isSet(32))
    {
      localObject = paramList;
      if (paramList.isEmpty()) {
        localObject = Collections.singletonList(Format.createTextSampleFormat(null, "application/cea-608", 0, null));
      }
    }
    this.closedCaptionFormats = ((List)localObject);
  }
  
  private SeiReader buildSeiReader(TsPayloadReader.EsInfo paramEsInfo)
  {
    if (isSet(32)) {
      return new SeiReader(this.closedCaptionFormats);
    }
    ParsableByteArray localParsableByteArray = new ParsableByteArray(paramEsInfo.descriptorBytes);
    paramEsInfo = this.closedCaptionFormats;
    while (localParsableByteArray.bytesLeft() > 0)
    {
      int i = localParsableByteArray.readUnsignedByte();
      int k = localParsableByteArray.readUnsignedByte();
      int m = localParsableByteArray.getPosition();
      if (i == 134)
      {
        ArrayList localArrayList = new ArrayList();
        int n = localParsableByteArray.readUnsignedByte();
        i = 0;
        paramEsInfo = localArrayList;
        if (i < (n & 0x1F))
        {
          String str = localParsableByteArray.readString(3);
          int i1 = localParsableByteArray.readUnsignedByte();
          if ((i1 & 0x80) != 0)
          {
            j = 1;
            label130:
            if (j == 0) {
              break label181;
            }
            paramEsInfo = "application/cea-708";
          }
          for (int j = i1 & 0x3F;; j = 1)
          {
            localArrayList.add(Format.createTextSampleFormat(null, paramEsInfo, null, -1, 0, str, j, null));
            localParsableByteArray.skipBytes(2);
            i += 1;
            break;
            j = 0;
            break label130;
            label181:
            paramEsInfo = "application/cea-608";
          }
        }
      }
      localParsableByteArray.setPosition(m + k);
    }
    return new SeiReader(paramEsInfo);
  }
  
  private boolean isSet(int paramInt)
  {
    return (this.flags & paramInt) != 0;
  }
  
  public SparseArray<TsPayloadReader> createInitialPayloadReaders()
  {
    return new SparseArray();
  }
  
  public TsPayloadReader createPayloadReader(int paramInt, TsPayloadReader.EsInfo paramEsInfo)
  {
    switch (paramInt)
    {
    default: 
    case 3: 
    case 4: 
    case 15: 
    case 17: 
    case 129: 
    case 135: 
    case 130: 
    case 138: 
    case 2: 
    case 27: 
    case 36: 
    case 134: 
      do
      {
        do
        {
          do
          {
            do
            {
              return null;
              return new PesReader(new MpegAudioReader(paramEsInfo.language));
            } while (isSet(2));
            return new PesReader(new AdtsReader(false, paramEsInfo.language));
          } while (isSet(2));
          return new PesReader(new LatmReader(paramEsInfo.language));
          return new PesReader(new Ac3Reader(paramEsInfo.language));
          return new PesReader(new DtsReader(paramEsInfo.language));
          return new PesReader(new H262Reader());
        } while (isSet(4));
        return new PesReader(new H264Reader(buildSeiReader(paramEsInfo), isSet(1), isSet(8)));
        return new PesReader(new H265Reader(buildSeiReader(paramEsInfo)));
      } while (isSet(16));
      return new SectionReader(new SpliceInfoSectionReader());
    case 21: 
      return new PesReader(new Id3Reader());
    }
    return new PesReader(new DvbSubtitleReader(paramEsInfo.dvbSubtitleInfos));
  }
  
  @Retention(RetentionPolicy.SOURCE)
  public static @interface Flags {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/ts/DefaultTsPayloadReaderFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */