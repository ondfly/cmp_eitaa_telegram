package org.telegram.messenger.exoplayer2.extractor.mp4;

import android.util.Log;
import android.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.ParserException;
import org.telegram.messenger.exoplayer2.audio.Ac3Util;
import org.telegram.messenger.exoplayer2.drm.DrmInitData;
import org.telegram.messenger.exoplayer2.extractor.GaplessInfoHolder;
import org.telegram.messenger.exoplayer2.metadata.Metadata;
import org.telegram.messenger.exoplayer2.metadata.Metadata.Entry;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.CodecSpecificDataUtil;
import org.telegram.messenger.exoplayer2.util.ParsableByteArray;
import org.telegram.messenger.exoplayer2.util.Util;
import org.telegram.messenger.exoplayer2.video.AvcConfig;
import org.telegram.messenger.exoplayer2.video.HevcConfig;

final class AtomParsers
{
  private static final String TAG = "AtomParsers";
  private static final int TYPE_cenc = Util.getIntegerCodeForString("cenc");
  private static final int TYPE_clcp;
  private static final int TYPE_meta = Util.getIntegerCodeForString("meta");
  private static final int TYPE_sbtl;
  private static final int TYPE_soun;
  private static final int TYPE_subt;
  private static final int TYPE_text;
  private static final int TYPE_vide = Util.getIntegerCodeForString("vide");
  
  static
  {
    TYPE_soun = Util.getIntegerCodeForString("soun");
    TYPE_text = Util.getIntegerCodeForString("text");
    TYPE_sbtl = Util.getIntegerCodeForString("sbtl");
    TYPE_subt = Util.getIntegerCodeForString("subt");
    TYPE_clcp = Util.getIntegerCodeForString("clcp");
  }
  
  private static int findEsdsPosition(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2)
  {
    int i = paramParsableByteArray.getPosition();
    while (i - paramInt1 < paramInt2)
    {
      paramParsableByteArray.setPosition(i);
      int j = paramParsableByteArray.readInt();
      if (j > 0) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkArgument(bool, "childAtomSize should be positive");
        if (paramParsableByteArray.readInt() != Atom.TYPE_esds) {
          break;
        }
        return i;
      }
      i += j;
    }
    return -1;
  }
  
  private static void parseAudioSampleEntry(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2, int paramInt3, int paramInt4, String paramString, boolean paramBoolean, DrmInitData paramDrmInitData, StsdData paramStsdData, int paramInt5)
    throws ParserException
  {
    paramParsableByteArray.setPosition(paramInt2 + 8 + 8);
    int k = 0;
    int m;
    int n;
    int i;
    int j;
    label88:
    DrmInitData localDrmInitData;
    Object localObject1;
    label145:
    label185:
    Object localObject3;
    label195:
    boolean bool;
    label221:
    label266:
    Object localObject2;
    if (paramBoolean)
    {
      k = paramParsableByteArray.readUnsignedShort();
      paramParsableByteArray.skipBytes(6);
      if ((k != 0) && (k != 1)) {
        break label416;
      }
      m = paramParsableByteArray.readUnsignedShort();
      paramParsableByteArray.skipBytes(6);
      n = paramParsableByteArray.readUnsignedFixedPoint1616();
      i = m;
      j = n;
      if (k == 1)
      {
        paramParsableByteArray.skipBytes(16);
        j = n;
        i = m;
      }
      m = paramParsableByteArray.getPosition();
      k = paramInt1;
      localDrmInitData = paramDrmInitData;
      if (paramInt1 == Atom.TYPE_enca)
      {
        localObject1 = parseSampleEntryEncryptionData(paramParsableByteArray, paramInt2, paramInt3);
        localDrmInitData = paramDrmInitData;
        if (localObject1 != null)
        {
          paramInt1 = ((Integer)((Pair)localObject1).first).intValue();
          if (paramDrmInitData != null) {
            break label453;
          }
          localDrmInitData = null;
          paramStsdData.trackEncryptionBoxes[paramInt5] = ((TrackEncryptionBox)((Pair)localObject1).second);
        }
        paramParsableByteArray.setPosition(m);
        k = paramInt1;
      }
      paramDrmInitData = null;
      if (k != Atom.TYPE_ac_3) {
        break label474;
      }
      paramDrmInitData = "audio/ac3";
      localObject3 = null;
      paramInt1 = m;
      localObject1 = paramDrmInitData;
      if (paramInt1 - paramInt2 >= paramInt3) {
        break label863;
      }
      paramParsableByteArray.setPosition(paramInt1);
      n = paramParsableByteArray.readInt();
      if (n <= 0) {
        break label625;
      }
      bool = true;
      Assertions.checkArgument(bool, "childAtomSize should be positive");
      m = paramParsableByteArray.readInt();
      if ((m != Atom.TYPE_esds) && ((!paramBoolean) || (m != Atom.TYPE_wave))) {
        break label643;
      }
      if (m != Atom.TYPE_esds) {
        break label631;
      }
      m = paramInt1;
      localObject2 = localObject1;
      k = i;
      paramInt5 = j;
      paramDrmInitData = (DrmInitData)localObject3;
      if (m != -1)
      {
        paramDrmInitData = parseEsdsFromParent(paramParsableByteArray, m);
        localObject1 = (String)paramDrmInitData.first;
        localObject3 = (byte[])paramDrmInitData.second;
        localObject2 = localObject1;
        k = i;
        paramInt5 = j;
        paramDrmInitData = (DrmInitData)localObject3;
        if ("audio/mp4a-latm".equals(localObject1))
        {
          paramDrmInitData = CodecSpecificDataUtil.parseAacAudioSpecificConfig((byte[])localObject3);
          paramInt5 = ((Integer)paramDrmInitData.first).intValue();
          k = ((Integer)paramDrmInitData.second).intValue();
          paramDrmInitData = (DrmInitData)localObject3;
          localObject2 = localObject1;
        }
      }
    }
    for (;;)
    {
      paramInt1 += n;
      localObject1 = localObject2;
      i = k;
      j = paramInt5;
      localObject3 = paramDrmInitData;
      break label195;
      paramParsableByteArray.skipBytes(8);
      break;
      label416:
      if (k != 2) {
        break label928;
      }
      paramParsableByteArray.skipBytes(16);
      j = (int)Math.round(paramParsableByteArray.readDouble());
      i = paramParsableByteArray.readUnsignedIntToInt();
      paramParsableByteArray.skipBytes(20);
      break label88;
      label453:
      localDrmInitData = paramDrmInitData.copyWithSchemeType(((TrackEncryptionBox)((Pair)localObject1).second).schemeType);
      break label145;
      label474:
      if (k == Atom.TYPE_ec_3)
      {
        paramDrmInitData = "audio/eac3";
        break label185;
      }
      if (k == Atom.TYPE_dtsc)
      {
        paramDrmInitData = "audio/vnd.dts";
        break label185;
      }
      if ((k == Atom.TYPE_dtsh) || (k == Atom.TYPE_dtsl))
      {
        paramDrmInitData = "audio/vnd.dts.hd";
        break label185;
      }
      if (k == Atom.TYPE_dtse)
      {
        paramDrmInitData = "audio/vnd.dts.hd;profile=lbr";
        break label185;
      }
      if (k == Atom.TYPE_samr)
      {
        paramDrmInitData = "audio/3gpp";
        break label185;
      }
      if (k == Atom.TYPE_sawb)
      {
        paramDrmInitData = "audio/amr-wb";
        break label185;
      }
      if ((k == Atom.TYPE_lpcm) || (k == Atom.TYPE_sowt))
      {
        paramDrmInitData = "audio/raw";
        break label185;
      }
      if (k == Atom.TYPE__mp3)
      {
        paramDrmInitData = "audio/mpeg";
        break label185;
      }
      if (k != Atom.TYPE_alac) {
        break label185;
      }
      paramDrmInitData = "audio/alac";
      break label185;
      label625:
      bool = false;
      break label221;
      label631:
      m = findEsdsPosition(paramParsableByteArray, paramInt1, n);
      break label266;
      label643:
      if (m == Atom.TYPE_dac3)
      {
        paramParsableByteArray.setPosition(paramInt1 + 8);
        paramStsdData.format = Ac3Util.parseAc3AnnexFFormat(paramParsableByteArray, Integer.toString(paramInt4), paramString, localDrmInitData);
        localObject2 = localObject1;
        k = i;
        paramInt5 = j;
        paramDrmInitData = (DrmInitData)localObject3;
      }
      else if (m == Atom.TYPE_dec3)
      {
        paramParsableByteArray.setPosition(paramInt1 + 8);
        paramStsdData.format = Ac3Util.parseEAc3AnnexFFormat(paramParsableByteArray, Integer.toString(paramInt4), paramString, localDrmInitData);
        localObject2 = localObject1;
        k = i;
        paramInt5 = j;
        paramDrmInitData = (DrmInitData)localObject3;
      }
      else if (m == Atom.TYPE_ddts)
      {
        paramStsdData.format = Format.createAudioSampleFormat(Integer.toString(paramInt4), (String)localObject1, null, -1, -1, i, j, null, localDrmInitData, 0, paramString);
        localObject2 = localObject1;
        k = i;
        paramInt5 = j;
        paramDrmInitData = (DrmInitData)localObject3;
      }
      else
      {
        localObject2 = localObject1;
        k = i;
        paramInt5 = j;
        paramDrmInitData = (DrmInitData)localObject3;
        if (m == Atom.TYPE_alac)
        {
          paramDrmInitData = new byte[n];
          paramParsableByteArray.setPosition(paramInt1);
          paramParsableByteArray.readBytes(paramDrmInitData, 0, n);
          localObject2 = localObject1;
          k = i;
          paramInt5 = j;
        }
      }
    }
    label863:
    if ((paramStsdData.format == null) && (localObject1 != null))
    {
      if (!"audio/raw".equals(localObject1)) {
        break label929;
      }
      paramInt1 = 2;
      paramDrmInitData = Integer.toString(paramInt4);
      if (localObject3 != null) {
        break label934;
      }
    }
    label928:
    label929:
    label934:
    for (paramParsableByteArray = null;; paramParsableByteArray = Collections.singletonList(localObject3))
    {
      paramStsdData.format = Format.createAudioSampleFormat(paramDrmInitData, (String)localObject1, null, -1, -1, i, j, paramInt1, paramParsableByteArray, localDrmInitData, 0, paramString);
      return;
      paramInt1 = -1;
      break;
    }
  }
  
  static Pair<Integer, TrackEncryptionBox> parseCommonEncryptionSinfFromParent(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2)
  {
    boolean bool2 = true;
    int i = paramInt1 + 8;
    int k = -1;
    int j = 0;
    Object localObject2 = null;
    Object localObject1 = null;
    if (i - paramInt1 < paramInt2)
    {
      paramParsableByteArray.setPosition(i);
      int m = paramParsableByteArray.readInt();
      int n = paramParsableByteArray.readInt();
      Object localObject3;
      Object localObject4;
      if (n == Atom.TYPE_frma)
      {
        localObject3 = Integer.valueOf(paramParsableByteArray.readInt());
        localObject4 = localObject2;
      }
      for (;;)
      {
        i += m;
        localObject1 = localObject3;
        localObject2 = localObject4;
        break;
        if (n == Atom.TYPE_schm)
        {
          paramParsableByteArray.skipBytes(4);
          localObject4 = paramParsableByteArray.readString(4);
          localObject3 = localObject1;
        }
        else
        {
          localObject3 = localObject1;
          localObject4 = localObject2;
          if (n == Atom.TYPE_schi)
          {
            k = i;
            j = m;
            localObject3 = localObject1;
            localObject4 = localObject2;
          }
        }
      }
    }
    if (("cenc".equals(localObject2)) || ("cbc1".equals(localObject2)) || ("cens".equals(localObject2)) || ("cbcs".equals(localObject2)))
    {
      if (localObject1 != null)
      {
        bool1 = true;
        Assertions.checkArgument(bool1, "frma atom is mandatory");
        if (k == -1) {
          break label258;
        }
        bool1 = true;
        label210:
        Assertions.checkArgument(bool1, "schi atom is mandatory");
        paramParsableByteArray = parseSchiFromParent(paramParsableByteArray, k, j, (String)localObject2);
        if (paramParsableByteArray == null) {
          break label264;
        }
      }
      label258:
      label264:
      for (boolean bool1 = bool2;; bool1 = false)
      {
        Assertions.checkArgument(bool1, "tenc atom is mandatory");
        return Pair.create(localObject1, paramParsableByteArray);
        bool1 = false;
        break;
        bool1 = false;
        break label210;
      }
    }
    return null;
  }
  
  private static Pair<long[], long[]> parseEdts(Atom.ContainerAtom paramContainerAtom)
  {
    if (paramContainerAtom != null)
    {
      paramContainerAtom = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_elst);
      if (paramContainerAtom != null) {}
    }
    else
    {
      return Pair.create(null, null);
    }
    paramContainerAtom = paramContainerAtom.data;
    paramContainerAtom.setPosition(8);
    int j = Atom.parseFullAtomVersion(paramContainerAtom.readInt());
    int k = paramContainerAtom.readUnsignedIntToInt();
    long[] arrayOfLong1 = new long[k];
    long[] arrayOfLong2 = new long[k];
    int i = 0;
    while (i < k)
    {
      if (j == 1)
      {
        l = paramContainerAtom.readUnsignedLongToLong();
        arrayOfLong1[i] = l;
        if (j != 1) {
          break label125;
        }
      }
      label125:
      for (long l = paramContainerAtom.readLong();; l = paramContainerAtom.readInt())
      {
        arrayOfLong2[i] = l;
        if (paramContainerAtom.readShort() == 1) {
          break label135;
        }
        throw new IllegalArgumentException("Unsupported media rate.");
        l = paramContainerAtom.readUnsignedInt();
        break;
      }
      label135:
      paramContainerAtom.skipBytes(2);
      i += 1;
    }
    return Pair.create(arrayOfLong1, arrayOfLong2);
  }
  
  private static Pair<String, byte[]> parseEsdsFromParent(ParsableByteArray paramParsableByteArray, int paramInt)
  {
    paramParsableByteArray.setPosition(paramInt + 8 + 4);
    paramParsableByteArray.skipBytes(1);
    parseExpandableClassSize(paramParsableByteArray);
    paramParsableByteArray.skipBytes(2);
    paramInt = paramParsableByteArray.readUnsignedByte();
    if ((paramInt & 0x80) != 0) {
      paramParsableByteArray.skipBytes(2);
    }
    if ((paramInt & 0x40) != 0) {
      paramParsableByteArray.skipBytes(paramParsableByteArray.readUnsignedShort());
    }
    if ((paramInt & 0x20) != 0) {
      paramParsableByteArray.skipBytes(2);
    }
    paramParsableByteArray.skipBytes(1);
    parseExpandableClassSize(paramParsableByteArray);
    Object localObject;
    switch (paramParsableByteArray.readUnsignedByte())
    {
    default: 
      localObject = null;
    case 96: 
    case 97: 
    case 32: 
    case 33: 
    case 35: 
    case 107: 
    case 64: 
    case 102: 
    case 103: 
    case 104: 
    case 165: 
    case 166: 
      for (;;)
      {
        paramParsableByteArray.skipBytes(12);
        paramParsableByteArray.skipBytes(1);
        paramInt = parseExpandableClassSize(paramParsableByteArray);
        byte[] arrayOfByte = new byte[paramInt];
        paramParsableByteArray.readBytes(arrayOfByte, 0, paramInt);
        return Pair.create(localObject, arrayOfByte);
        localObject = "video/mpeg2";
        continue;
        localObject = "video/mp4v-es";
        continue;
        localObject = "video/avc";
        continue;
        localObject = "video/hevc";
        continue;
        return Pair.create("audio/mpeg", null);
        localObject = "audio/mp4a-latm";
        continue;
        localObject = "audio/ac3";
        continue;
        localObject = "audio/eac3";
      }
    case 169: 
    case 172: 
      return Pair.create("audio/vnd.dts", null);
    }
    return Pair.create("audio/vnd.dts.hd", null);
  }
  
  private static int parseExpandableClassSize(ParsableByteArray paramParsableByteArray)
  {
    int j = paramParsableByteArray.readUnsignedByte();
    for (int i = j & 0x7F; (j & 0x80) == 128; i = i << 7 | j & 0x7F) {
      j = paramParsableByteArray.readUnsignedByte();
    }
    return i;
  }
  
  private static int parseHdlr(ParsableByteArray paramParsableByteArray)
  {
    paramParsableByteArray.setPosition(16);
    int i = paramParsableByteArray.readInt();
    if (i == TYPE_soun) {
      return 1;
    }
    if (i == TYPE_vide) {
      return 2;
    }
    if ((i == TYPE_text) || (i == TYPE_sbtl) || (i == TYPE_subt) || (i == TYPE_clcp)) {
      return 3;
    }
    if (i == TYPE_meta) {
      return 4;
    }
    return -1;
  }
  
  private static Metadata parseIlst(ParsableByteArray paramParsableByteArray, int paramInt)
  {
    paramParsableByteArray.skipBytes(8);
    ArrayList localArrayList = new ArrayList();
    while (paramParsableByteArray.getPosition() < paramInt)
    {
      Metadata.Entry localEntry = MetadataUtil.parseIlstElement(paramParsableByteArray);
      if (localEntry != null) {
        localArrayList.add(localEntry);
      }
    }
    if (localArrayList.isEmpty()) {
      return null;
    }
    return new Metadata(localArrayList);
  }
  
  private static Pair<Long, String> parseMdhd(ParsableByteArray paramParsableByteArray)
  {
    int j = 8;
    paramParsableByteArray.setPosition(8);
    int k = Atom.parseFullAtomVersion(paramParsableByteArray.readInt());
    if (k == 0) {}
    for (int i = 8;; i = 16)
    {
      paramParsableByteArray.skipBytes(i);
      long l = paramParsableByteArray.readUnsignedInt();
      i = j;
      if (k == 0) {
        i = 4;
      }
      paramParsableByteArray.skipBytes(i);
      i = paramParsableByteArray.readUnsignedShort();
      return Pair.create(Long.valueOf(l), "" + (char)((i >> 10 & 0x1F) + 96) + (char)((i >> 5 & 0x1F) + 96) + (char)((i & 0x1F) + 96));
    }
  }
  
  private static Metadata parseMetaAtom(ParsableByteArray paramParsableByteArray, int paramInt)
  {
    paramParsableByteArray.skipBytes(12);
    while (paramParsableByteArray.getPosition() < paramInt)
    {
      int i = paramParsableByteArray.getPosition();
      int j = paramParsableByteArray.readInt();
      if (paramParsableByteArray.readInt() == Atom.TYPE_ilst)
      {
        paramParsableByteArray.setPosition(i);
        return parseIlst(paramParsableByteArray, i + j);
      }
      paramParsableByteArray.skipBytes(j - 8);
    }
    return null;
  }
  
  private static long parseMvhd(ParsableByteArray paramParsableByteArray)
  {
    int i = 8;
    paramParsableByteArray.setPosition(8);
    if (Atom.parseFullAtomVersion(paramParsableByteArray.readInt()) == 0) {}
    for (;;)
    {
      paramParsableByteArray.skipBytes(i);
      return paramParsableByteArray.readUnsignedInt();
      i = 16;
    }
  }
  
  private static float parsePaspFromParent(ParsableByteArray paramParsableByteArray, int paramInt)
  {
    paramParsableByteArray.setPosition(paramInt + 8);
    paramInt = paramParsableByteArray.readUnsignedIntToInt();
    int i = paramParsableByteArray.readUnsignedIntToInt();
    return paramInt / i;
  }
  
  private static byte[] parseProjFromParent(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2)
  {
    int i = paramInt1 + 8;
    while (i - paramInt1 < paramInt2)
    {
      paramParsableByteArray.setPosition(i);
      int j = paramParsableByteArray.readInt();
      if (paramParsableByteArray.readInt() == Atom.TYPE_proj) {
        return Arrays.copyOfRange(paramParsableByteArray.data, i, i + j);
      }
      i += j;
    }
    return null;
  }
  
  private static Pair<Integer, TrackEncryptionBox> parseSampleEntryEncryptionData(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2)
  {
    int i = paramParsableByteArray.getPosition();
    while (i - paramInt1 < paramInt2)
    {
      paramParsableByteArray.setPosition(i);
      int j = paramParsableByteArray.readInt();
      if (j > 0) {}
      for (boolean bool = true;; bool = false)
      {
        Assertions.checkArgument(bool, "childAtomSize should be positive");
        if (paramParsableByteArray.readInt() != Atom.TYPE_sinf) {
          break;
        }
        Pair localPair = parseCommonEncryptionSinfFromParent(paramParsableByteArray, i, j);
        if (localPair == null) {
          break;
        }
        return localPair;
      }
      i += j;
    }
    return null;
  }
  
  private static TrackEncryptionBox parseSchiFromParent(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2, String paramString)
  {
    int i = paramInt1 + 8;
    while (i - paramInt1 < paramInt2)
    {
      paramParsableByteArray.setPosition(i);
      int j = paramParsableByteArray.readInt();
      if (paramParsableByteArray.readInt() == Atom.TYPE_tenc)
      {
        i = Atom.parseFullAtomVersion(paramParsableByteArray.readInt());
        paramParsableByteArray.skipBytes(1);
        paramInt1 = 0;
        paramInt2 = 0;
        if (i == 0)
        {
          paramParsableByteArray.skipBytes(1);
          if (paramParsableByteArray.readUnsignedByte() != 1) {
            break label179;
          }
        }
        label179:
        for (boolean bool = true;; bool = false)
        {
          i = paramParsableByteArray.readUnsignedByte();
          byte[] arrayOfByte = new byte[16];
          paramParsableByteArray.readBytes(arrayOfByte, 0, arrayOfByte.length);
          Object localObject2 = null;
          Object localObject1 = localObject2;
          if (bool)
          {
            localObject1 = localObject2;
            if (i == 0)
            {
              j = paramParsableByteArray.readUnsignedByte();
              localObject1 = new byte[j];
              paramParsableByteArray.readBytes((byte[])localObject1, 0, j);
            }
          }
          return new TrackEncryptionBox(bool, paramString, i, arrayOfByte, paramInt1, paramInt2, (byte[])localObject1);
          paramInt2 = paramParsableByteArray.readUnsignedByte();
          paramInt1 = (paramInt2 & 0xF0) >> 4;
          paramInt2 &= 0xF;
          break;
        }
      }
      i += j;
    }
    return null;
  }
  
  public static TrackSampleTable parseStbl(Track paramTrack, Atom.ContainerAtom paramContainerAtom, GaplessInfoHolder paramGaplessInfoHolder)
    throws ParserException
  {
    Object localObject1 = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_stsz);
    if (localObject1 != null) {}
    int i13;
    for (Object localObject2 = new StszSampleSizeBox((Atom.LeafAtom)localObject1);; localObject2 = new Stz2SampleSizeBox((Atom.LeafAtom)localObject1))
    {
      i13 = ((SampleSizeBox)localObject2).getSampleCount();
      if (i13 != 0) {
        break;
      }
      return new TrackSampleTable(new long[0], new int[0], 0, new long[0], new int[0]);
      localObject1 = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_stz2);
      if (localObject1 == null) {
        throw new ParserException("Track has no sample table size information");
      }
    }
    boolean bool = false;
    Object localObject3 = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_stco);
    localObject1 = localObject3;
    if (localObject3 == null)
    {
      bool = true;
      localObject1 = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_co64);
    }
    localObject3 = ((Atom.LeafAtom)localObject1).data;
    Object localObject4 = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_stsc).data;
    ParsableByteArray localParsableByteArray = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_stts).data;
    localObject1 = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_stss);
    label201:
    ChunkIterator localChunkIterator;
    int i1;
    int i4;
    int i2;
    int i3;
    int n;
    int j;
    if (localObject1 != null)
    {
      localObject1 = ((Atom.LeafAtom)localObject1).data;
      paramContainerAtom = paramContainerAtom.getLeafAtomOfType(Atom.TYPE_ctts);
      if (paramContainerAtom == null) {
        break label477;
      }
      paramContainerAtom = paramContainerAtom.data;
      localChunkIterator = new ChunkIterator((ParsableByteArray)localObject4, (ParsableByteArray)localObject3, bool);
      localParsableByteArray.setPosition(12);
      i1 = localParsableByteArray.readUnsignedIntToInt() - 1;
      i4 = localParsableByteArray.readUnsignedIntToInt();
      i2 = localParsableByteArray.readUnsignedIntToInt();
      i3 = 0;
      i = 0;
      n = 0;
      if (paramContainerAtom != null)
      {
        paramContainerAtom.setPosition(12);
        i = paramContainerAtom.readUnsignedIntToInt();
      }
      m = -1;
      k = 0;
      j = m;
      localObject3 = localObject1;
      if (localObject1 != null)
      {
        ((ParsableByteArray)localObject1).setPosition(12);
        k = ((ParsableByteArray)localObject1).readUnsignedIntToInt();
        if (k <= 0) {
          break label482;
        }
        j = ((ParsableByteArray)localObject1).readUnsignedIntToInt() - 1;
        localObject3 = localObject1;
      }
      label320:
      if ((!((SampleSizeBox)localObject2).isFixedSampleSize()) || (!"audio/raw".equals(paramTrack.format.sampleMimeType)) || (i1 != 0) || (i != 0) || (k != 0)) {
        break label492;
      }
    }
    long l1;
    Object localObject5;
    long[] arrayOfLong;
    int[] arrayOfInt;
    long l2;
    int i6;
    int i5;
    label477:
    label482:
    label492:
    for (int m = 1;; m = 0)
    {
      i7 = 0;
      l1 = 0L;
      if (m != 0) {
        break label1032;
      }
      localObject4 = new long[i13];
      localObject5 = new int[i13];
      arrayOfLong = new long[i13];
      arrayOfInt = new int[i13];
      l2 = 0L;
      i6 = 0;
      i8 = 0;
      m = n;
      i5 = k;
      n = i6;
      k = i3;
      i6 = j;
      i3 = i8;
      j = i7;
      if (i3 >= i13) {
        break label797;
      }
      while (n == 0)
      {
        Assertions.checkState(localChunkIterator.moveNext());
        l2 = localChunkIterator.offset;
        n = localChunkIterator.numSamples;
      }
      localObject1 = null;
      break;
      paramContainerAtom = null;
      break label201;
      localObject3 = null;
      j = m;
      break label320;
    }
    int i9 = k;
    int i8 = i;
    int i7 = m;
    if (paramContainerAtom != null)
    {
      while ((k == 0) && (i > 0))
      {
        k = paramContainerAtom.readUnsignedIntToInt();
        m = paramContainerAtom.readInt();
        i -= 1;
      }
      i9 = k - 1;
      i7 = m;
      i8 = i;
    }
    localObject4[i3] = l2;
    localObject5[i3] = ((SampleSizeBox)localObject2).readNextSampleSize();
    int k = j;
    if (localObject5[i3] > j) {
      k = localObject5[i3];
    }
    arrayOfLong[i3] = (i7 + l1);
    if (localObject3 == null) {}
    for (int i = 1;; i = 0)
    {
      arrayOfInt[i3] = i;
      int i11 = i6;
      m = i5;
      if (i3 == i6)
      {
        arrayOfInt[i3] = 1;
        i = i5 - 1;
        i11 = i6;
        m = i;
        if (i > 0)
        {
          i11 = ((ParsableByteArray)localObject3).readUnsignedIntToInt() - 1;
          m = i;
        }
      }
      l1 += i2;
      j = i4 - 1;
      i = j;
      int i12 = i1;
      int i10 = i2;
      if (j == 0)
      {
        i = j;
        i12 = i1;
        i10 = i2;
        if (i1 > 0)
        {
          i = localParsableByteArray.readUnsignedIntToInt();
          i10 = localParsableByteArray.readInt();
          i12 = i1 - 1;
        }
      }
      l2 += localObject5[i3];
      n -= 1;
      i3 += 1;
      j = k;
      i6 = i11;
      i4 = i;
      k = i9;
      i5 = m;
      i1 = i12;
      i = i8;
      i2 = i10;
      m = i7;
      break;
    }
    label797:
    if (k == 0)
    {
      bool = true;
      Assertions.checkArgument(bool);
      label810:
      if (i <= 0) {
        break label853;
      }
      if (paramContainerAtom.readUnsignedIntToInt() != 0) {
        break label847;
      }
    }
    label847:
    for (bool = true;; bool = false)
    {
      Assertions.checkArgument(bool);
      paramContainerAtom.readInt();
      i -= 1;
      break label810;
      bool = false;
      break;
    }
    label853:
    if ((i5 == 0) && (i4 == 0) && (n == 0))
    {
      paramContainerAtom = (Atom.ContainerAtom)localObject4;
      localObject1 = localObject5;
      i = j;
      localObject3 = arrayOfLong;
      localObject2 = arrayOfInt;
      l2 = l1;
      if (i1 == 0) {}
    }
    else
    {
      Log.w("AtomParsers", "Inconsistent stbl box for track " + paramTrack.id + ": remainingSynchronizationSamples " + i5 + ", remainingSamplesAtTimestampDelta " + i4 + ", remainingSamplesInChunk " + n + ", remainingTimestampDeltaChanges " + i1);
      l2 = l1;
      localObject2 = arrayOfInt;
      localObject3 = arrayOfLong;
      i = j;
      localObject1 = localObject5;
      paramContainerAtom = (Atom.ContainerAtom)localObject4;
    }
    while ((paramTrack.editListDurations == null) || (paramGaplessInfoHolder.hasGaplessInfo()))
    {
      Util.scaleLargeTimestampsInPlace((long[])localObject3, 1000000L, paramTrack.timescale);
      return new TrackSampleTable(paramContainerAtom, (int[])localObject1, i, (long[])localObject3, (int[])localObject2);
      label1032:
      paramContainerAtom = new long[localChunkIterator.length];
      localObject1 = new int[localChunkIterator.length];
      while (localChunkIterator.moveNext())
      {
        paramContainerAtom[localChunkIterator.index] = localChunkIterator.offset;
        localObject1[localChunkIterator.index] = localChunkIterator.numSamples;
      }
      localObject2 = FixedSampleSizeRechunker.rechunk(((SampleSizeBox)localObject2).readNextSampleSize(), paramContainerAtom, (int[])localObject1, i2);
      paramContainerAtom = ((FixedSampleSizeRechunker.Results)localObject2).offsets;
      localObject1 = ((FixedSampleSizeRechunker.Results)localObject2).sizes;
      i = ((FixedSampleSizeRechunker.Results)localObject2).maximumSize;
      localObject3 = ((FixedSampleSizeRechunker.Results)localObject2).timestamps;
      localObject2 = ((FixedSampleSizeRechunker.Results)localObject2).flags;
      l2 = l1;
    }
    long l3;
    if ((paramTrack.editListDurations.length == 1) && (paramTrack.type == 1) && (localObject3.length >= 2))
    {
      l3 = paramTrack.editListMediaTimes[0];
      l1 = l3 + Util.scaleLargeTimestamp(paramTrack.editListDurations[0], paramTrack.timescale, paramTrack.movieTimescale);
      if ((localObject3[0] <= l3) && (l3 < localObject3[1]) && (localObject3[(localObject3.length - 1)] < l1) && (l1 <= l2))
      {
        l3 = Util.scaleLargeTimestamp(l3 - localObject3[0], paramTrack.format.sampleRate, paramTrack.timescale);
        l1 = Util.scaleLargeTimestamp(l2 - l1, paramTrack.format.sampleRate, paramTrack.timescale);
        if (((l3 != 0L) || (l1 != 0L)) && (l3 <= 2147483647L) && (l1 <= 2147483647L))
        {
          paramGaplessInfoHolder.encoderDelay = ((int)l3);
          paramGaplessInfoHolder.encoderPadding = ((int)l1);
          Util.scaleLargeTimestampsInPlace((long[])localObject3, 1000000L, paramTrack.timescale);
          return new TrackSampleTable(paramContainerAtom, (int[])localObject1, i, (long[])localObject3, (int[])localObject2);
        }
      }
    }
    if ((paramTrack.editListDurations.length == 1) && (paramTrack.editListDurations[0] == 0L))
    {
      j = 0;
      while (j < localObject3.length)
      {
        localObject3[j] = Util.scaleLargeTimestamp(localObject3[j] - paramTrack.editListMediaTimes[0], 1000000L, paramTrack.timescale);
        j += 1;
      }
      return new TrackSampleTable(paramContainerAtom, (int[])localObject1, i, (long[])localObject3, (int[])localObject2);
    }
    if (paramTrack.type == 1)
    {
      bool = true;
      k = 0;
      n = 0;
      j = 0;
      m = 0;
      label1465:
      if (m >= paramTrack.editListDurations.length) {
        break label1611;
      }
      l1 = paramTrack.editListMediaTimes[m];
      i3 = j;
      i2 = k;
      i1 = n;
      if (l1 != -1L)
      {
        l2 = Util.scaleLargeTimestamp(paramTrack.editListDurations[m], paramTrack.timescale, paramTrack.movieTimescale);
        i3 = Util.binarySearchCeil((long[])localObject3, l1, true, true);
        i1 = Util.binarySearchCeil((long[])localObject3, l1 + l2, bool, false);
        i2 = k + (i1 - i3);
        if (n == i3) {
          break label1605;
        }
      }
    }
    label1605:
    for (k = 1;; k = 0)
    {
      i3 = j | k;
      m += 1;
      j = i3;
      k = i2;
      n = i1;
      break label1465;
      bool = false;
      break;
    }
    label1611:
    if (k != i13)
    {
      m = 1;
      i3 = j | m;
      if (i3 == 0) {
        break label1924;
      }
      paramGaplessInfoHolder = new long[k];
      label1638:
      if (i3 == 0) {
        break label1929;
      }
      localObject4 = new int[k];
      label1649:
      if (i3 == 0) {
        break label1936;
      }
      j = 0;
      label1657:
      if (i3 == 0) {
        break label1942;
      }
      localObject5 = new int[k];
      label1668:
      arrayOfLong = new long[k];
      l1 = 0L;
      k = 0;
      m = 0;
    }
    for (;;)
    {
      if (m >= paramTrack.editListDurations.length) {
        break label1973;
      }
      l2 = paramTrack.editListMediaTimes[m];
      l3 = paramTrack.editListDurations[m];
      i1 = j;
      i2 = k;
      if (l2 != -1L)
      {
        long l4 = Util.scaleLargeTimestamp(l3, paramTrack.timescale, paramTrack.movieTimescale);
        n = Util.binarySearchCeil((long[])localObject3, l2, true, true);
        i4 = Util.binarySearchCeil((long[])localObject3, l2 + l4, bool, false);
        if (i3 != 0)
        {
          i1 = i4 - n;
          System.arraycopy(paramContainerAtom, n, paramGaplessInfoHolder, k, i1);
          System.arraycopy(localObject1, n, localObject4, k, i1);
          System.arraycopy(localObject2, n, localObject5, k, i1);
        }
        for (;;)
        {
          i1 = j;
          i2 = k;
          if (n >= i4) {
            break;
          }
          arrayOfLong[k] = (Util.scaleLargeTimestamp(l1, 1000000L, paramTrack.movieTimescale) + Util.scaleLargeTimestamp(localObject3[n] - l2, 1000000L, paramTrack.timescale));
          i1 = j;
          if (i3 != 0)
          {
            i1 = j;
            if (localObject4[k] > j) {
              i1 = localObject1[n];
            }
          }
          k += 1;
          n += 1;
          j = i1;
        }
        m = 0;
        break;
        label1924:
        paramGaplessInfoHolder = paramContainerAtom;
        break label1638;
        label1929:
        localObject4 = localObject1;
        break label1649;
        label1936:
        j = i;
        break label1657;
        label1942:
        localObject5 = localObject2;
        break label1668;
      }
      l1 += l3;
      m += 1;
      j = i1;
      k = i2;
    }
    label1973:
    m = 0;
    k = 0;
    if ((k < localObject5.length) && (m == 0))
    {
      if ((localObject5[k] & 0x1) != 0) {}
      for (n = 1;; n = 0)
      {
        m |= n;
        k += 1;
        break;
      }
    }
    if (m == 0)
    {
      Log.w("AtomParsers", "Ignoring edit list: Edited sample sequence does not contain a sync sample.");
      Util.scaleLargeTimestampsInPlace((long[])localObject3, 1000000L, paramTrack.timescale);
      return new TrackSampleTable(paramContainerAtom, (int[])localObject1, i, (long[])localObject3, (int[])localObject2);
    }
    return new TrackSampleTable(paramGaplessInfoHolder, (int[])localObject4, j, arrayOfLong, (int[])localObject5);
  }
  
  private static StsdData parseStsd(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2, String paramString, DrmInitData paramDrmInitData, boolean paramBoolean)
    throws ParserException
  {
    paramParsableByteArray.setPosition(12);
    int j = paramParsableByteArray.readInt();
    StsdData localStsdData = new StsdData(j);
    int i = 0;
    if (i < j)
    {
      int k = paramParsableByteArray.getPosition();
      int m = paramParsableByteArray.readInt();
      boolean bool;
      label53:
      int n;
      if (m > 0)
      {
        bool = true;
        Assertions.checkArgument(bool, "childAtomSize should be positive");
        n = paramParsableByteArray.readInt();
        if ((n != Atom.TYPE_avc1) && (n != Atom.TYPE_avc3) && (n != Atom.TYPE_encv) && (n != Atom.TYPE_mp4v) && (n != Atom.TYPE_hvc1) && (n != Atom.TYPE_hev1) && (n != Atom.TYPE_s263) && (n != Atom.TYPE_vp08) && (n != Atom.TYPE_vp09)) {
          break label180;
        }
        parseVideoSampleEntry(paramParsableByteArray, n, k, m, paramInt1, paramInt2, paramDrmInitData, localStsdData, i);
      }
      for (;;)
      {
        paramParsableByteArray.setPosition(k + m);
        i += 1;
        break;
        bool = false;
        break label53;
        label180:
        if ((n == Atom.TYPE_mp4a) || (n == Atom.TYPE_enca) || (n == Atom.TYPE_ac_3) || (n == Atom.TYPE_ec_3) || (n == Atom.TYPE_dtsc) || (n == Atom.TYPE_dtse) || (n == Atom.TYPE_dtsh) || (n == Atom.TYPE_dtsl) || (n == Atom.TYPE_samr) || (n == Atom.TYPE_sawb) || (n == Atom.TYPE_lpcm) || (n == Atom.TYPE_sowt) || (n == Atom.TYPE__mp3) || (n == Atom.TYPE_alac)) {
          parseAudioSampleEntry(paramParsableByteArray, n, k, m, paramInt1, paramString, paramBoolean, paramDrmInitData, localStsdData, i);
        } else if ((n == Atom.TYPE_TTML) || (n == Atom.TYPE_tx3g) || (n == Atom.TYPE_wvtt) || (n == Atom.TYPE_stpp) || (n == Atom.TYPE_c608)) {
          parseTextSampleEntry(paramParsableByteArray, n, k, m, paramInt1, paramString, localStsdData);
        } else if (n == Atom.TYPE_camm) {
          localStsdData.format = Format.createSampleFormat(Integer.toString(paramInt1), "application/x-camera-motion", null, -1, null);
        }
      }
    }
    return localStsdData;
  }
  
  private static void parseTextSampleEntry(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2, int paramInt3, int paramInt4, String paramString, StsdData paramStsdData)
    throws ParserException
  {
    paramParsableByteArray.setPosition(paramInt2 + 8 + 8);
    Object localObject1 = null;
    long l = Long.MAX_VALUE;
    if (paramInt1 == Atom.TYPE_TTML) {
      paramParsableByteArray = "application/ttml+xml";
    }
    for (;;)
    {
      paramStsdData.format = Format.createTextSampleFormat(Integer.toString(paramInt4), paramParsableByteArray, null, -1, 0, paramString, -1, null, l, (List)localObject1);
      return;
      if (paramInt1 == Atom.TYPE_tx3g)
      {
        localObject1 = "application/x-quicktime-tx3g";
        paramInt1 = paramInt3 - 8 - 8;
        Object localObject2 = new byte[paramInt1];
        paramParsableByteArray.readBytes((byte[])localObject2, 0, paramInt1);
        localObject2 = Collections.singletonList(localObject2);
        paramParsableByteArray = (ParsableByteArray)localObject1;
        localObject1 = localObject2;
      }
      else if (paramInt1 == Atom.TYPE_wvtt)
      {
        paramParsableByteArray = "application/x-mp4-vtt";
      }
      else if (paramInt1 == Atom.TYPE_stpp)
      {
        paramParsableByteArray = "application/ttml+xml";
        l = 0L;
      }
      else
      {
        if (paramInt1 != Atom.TYPE_c608) {
          break;
        }
        paramParsableByteArray = "application/x-mp4-cea-608";
        paramStsdData.requiredSampleTransformation = 1;
      }
    }
    throw new IllegalStateException();
  }
  
  private static TkhdData parseTkhd(ParsableByteArray paramParsableByteArray)
  {
    paramParsableByteArray.setPosition(8);
    int i1 = Atom.parseFullAtomVersion(paramParsableByteArray.readInt());
    int i;
    int n;
    int m;
    label55:
    int j;
    label57:
    int k;
    long l1;
    if (i1 == 0)
    {
      i = 8;
      paramParsableByteArray.skipBytes(i);
      n = paramParsableByteArray.readInt();
      paramParsableByteArray.skipBytes(4);
      m = 1;
      int i2 = paramParsableByteArray.getPosition();
      if (i1 != 0) {
        break label172;
      }
      i = 4;
      j = 0;
      k = m;
      if (j < i)
      {
        if (paramParsableByteArray.data[(i2 + j)] == -1) {
          break label178;
        }
        k = 0;
      }
      if (k == 0) {
        break label185;
      }
      paramParsableByteArray.skipBytes(i);
      l1 = -9223372036854775807L;
      paramParsableByteArray.skipBytes(16);
      i = paramParsableByteArray.readInt();
      j = paramParsableByteArray.readInt();
      paramParsableByteArray.skipBytes(4);
      k = paramParsableByteArray.readInt();
      m = paramParsableByteArray.readInt();
      if ((i != 0) || (j != 65536) || (k != -65536) || (m != 0)) {
        break label224;
      }
      i = 90;
    }
    for (;;)
    {
      return new TkhdData(n, l1, i);
      i = 16;
      break;
      label172:
      i = 8;
      break label55;
      label178:
      j += 1;
      break label57;
      label185:
      if (i1 == 0) {}
      for (long l2 = paramParsableByteArray.readUnsignedInt();; l2 = paramParsableByteArray.readUnsignedLongToLong())
      {
        l1 = l2;
        if (l2 != 0L) {
          break;
        }
        l1 = -9223372036854775807L;
        break;
      }
      label224:
      if ((i == 0) && (j == -65536) && (k == 65536) && (m == 0)) {
        i = 270;
      } else if ((i == -65536) && (j == 0) && (k == 0) && (m == -65536)) {
        i = 180;
      } else {
        i = 0;
      }
    }
  }
  
  public static Track parseTrak(Atom.ContainerAtom paramContainerAtom, Atom.LeafAtom paramLeafAtom, long paramLong, DrmInitData paramDrmInitData, boolean paramBoolean1, boolean paramBoolean2)
    throws ParserException
  {
    Object localObject = paramContainerAtom.getContainerAtomOfType(Atom.TYPE_mdia);
    int i = parseHdlr(((Atom.ContainerAtom)localObject).getLeafAtomOfType(Atom.TYPE_hdlr).data);
    if (i == -1) {
      return null;
    }
    TkhdData localTkhdData = parseTkhd(paramContainerAtom.getLeafAtomOfType(Atom.TYPE_tkhd).data);
    long l1 = paramLong;
    if (paramLong == -9223372036854775807L) {
      l1 = localTkhdData.duration;
    }
    long l2 = parseMvhd(paramLeafAtom.data);
    if (l1 == -9223372036854775807L) {}
    StsdData localStsdData;
    for (paramLong = -9223372036854775807L;; paramLong = Util.scaleLargeTimestamp(l1, 1000000L, l2))
    {
      paramLeafAtom = ((Atom.ContainerAtom)localObject).getContainerAtomOfType(Atom.TYPE_minf).getContainerAtomOfType(Atom.TYPE_stbl);
      localObject = parseMdhd(((Atom.ContainerAtom)localObject).getLeafAtomOfType(Atom.TYPE_mdhd).data);
      localStsdData = parseStsd(paramLeafAtom.getLeafAtomOfType(Atom.TYPE_stsd).data, localTkhdData.id, localTkhdData.rotationDegrees, (String)((Pair)localObject).second, paramDrmInitData, paramBoolean2);
      paramLeafAtom = null;
      paramDrmInitData = null;
      if (!paramBoolean1)
      {
        paramContainerAtom = parseEdts(paramContainerAtom.getContainerAtomOfType(Atom.TYPE_edts));
        paramLeafAtom = (long[])paramContainerAtom.first;
        paramDrmInitData = (long[])paramContainerAtom.second;
      }
      if (localStsdData.format != null) {
        break;
      }
      return null;
    }
    return new Track(localTkhdData.id, i, ((Long)((Pair)localObject).first).longValue(), l2, paramLong, localStsdData.format, localStsdData.requiredSampleTransformation, localStsdData.trackEncryptionBoxes, localStsdData.nalUnitLengthFieldLength, paramLeafAtom, paramDrmInitData);
  }
  
  public static Metadata parseUdta(Atom.LeafAtom paramLeafAtom, boolean paramBoolean)
  {
    if (paramBoolean) {}
    for (;;)
    {
      return null;
      paramLeafAtom = paramLeafAtom.data;
      paramLeafAtom.setPosition(8);
      while (paramLeafAtom.bytesLeft() >= 8)
      {
        int i = paramLeafAtom.getPosition();
        int j = paramLeafAtom.readInt();
        if (paramLeafAtom.readInt() == Atom.TYPE_meta)
        {
          paramLeafAtom.setPosition(i);
          return parseMetaAtom(paramLeafAtom, i + j);
        }
        paramLeafAtom.skipBytes(j - 8);
      }
    }
  }
  
  private static void parseVideoSampleEntry(ParsableByteArray paramParsableByteArray, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, DrmInitData paramDrmInitData, StsdData paramStsdData, int paramInt6)
    throws ParserException
  {
    paramParsableByteArray.setPosition(paramInt2 + 8 + 8);
    paramParsableByteArray.skipBytes(16);
    int n = paramParsableByteArray.readUnsignedShort();
    int i1 = paramParsableByteArray.readUnsignedShort();
    int j = 0;
    float f2 = 1.0F;
    paramParsableByteArray.skipBytes(50);
    int k = paramParsableByteArray.getPosition();
    int i = paramInt1;
    Object localObject2 = paramDrmInitData;
    if (paramInt1 == Atom.TYPE_encv)
    {
      localObject2 = parseSampleEntryEncryptionData(paramParsableByteArray, paramInt2, paramInt3);
      localObject1 = paramDrmInitData;
      if (localObject2 != null)
      {
        paramInt1 = ((Integer)((Pair)localObject2).first).intValue();
        if (paramDrmInitData != null) {
          break label191;
        }
      }
    }
    Object localObject4;
    Object localObject6;
    Object localObject3;
    int i2;
    label191:
    for (Object localObject1 = null;; localObject1 = paramDrmInitData.copyWithSchemeType(((TrackEncryptionBox)((Pair)localObject2).second).schemeType))
    {
      paramStsdData.trackEncryptionBoxes[paramInt6] = ((TrackEncryptionBox)((Pair)localObject2).second);
      paramParsableByteArray.setPosition(k);
      localObject2 = localObject1;
      i = paramInt1;
      localObject4 = null;
      localObject6 = null;
      localObject3 = null;
      paramInt1 = -1;
      paramInt6 = k;
      k = paramInt1;
      if (paramInt6 - paramInt2 < paramInt3)
      {
        paramParsableByteArray.setPosition(paramInt6);
        paramInt1 = paramParsableByteArray.getPosition();
        i2 = paramParsableByteArray.readInt();
        if ((i2 != 0) || (paramParsableByteArray.getPosition() - paramInt2 != paramInt3)) {
          break;
        }
      }
      if (localObject6 != null) {
        break label970;
      }
      return;
    }
    boolean bool;
    label220:
    int i3;
    label249:
    float f1;
    Object localObject5;
    int m;
    if (i2 > 0)
    {
      bool = true;
      Assertions.checkArgument(bool, "childAtomSize should be positive");
      i3 = paramParsableByteArray.readInt();
      if (i3 != Atom.TYPE_avcC) {
        break label389;
      }
      if (localObject6 != null) {
        break label383;
      }
      bool = true;
      Assertions.checkState(bool);
      localObject4 = "video/avc";
      paramParsableByteArray.setPosition(paramInt1 + 8);
      AvcConfig localAvcConfig = AvcConfig.parse(paramParsableByteArray);
      localObject6 = localAvcConfig.initializationData;
      paramStsdData.nalUnitLengthFieldLength = localAvcConfig.nalUnitLengthFieldLength;
      paramDrmInitData = (DrmInitData)localObject4;
      localObject1 = localObject6;
      f1 = f2;
      localObject5 = localObject3;
      paramInt1 = k;
      m = j;
      if (j == 0)
      {
        f1 = localAvcConfig.pixelWidthAspectRatio;
        m = j;
        paramInt1 = k;
        localObject5 = localObject3;
        localObject1 = localObject6;
        paramDrmInitData = (DrmInitData)localObject4;
      }
    }
    for (;;)
    {
      paramInt6 += i2;
      localObject6 = paramDrmInitData;
      localObject4 = localObject1;
      f2 = f1;
      localObject3 = localObject5;
      k = paramInt1;
      j = m;
      break;
      bool = false;
      break label220;
      label383:
      bool = false;
      break label249;
      label389:
      if (i3 == Atom.TYPE_hvcC)
      {
        if (localObject6 == null) {}
        for (bool = true;; bool = false)
        {
          Assertions.checkState(bool);
          paramDrmInitData = "video/hevc";
          paramParsableByteArray.setPosition(paramInt1 + 8);
          localObject4 = HevcConfig.parse(paramParsableByteArray);
          localObject1 = ((HevcConfig)localObject4).initializationData;
          paramStsdData.nalUnitLengthFieldLength = ((HevcConfig)localObject4).nalUnitLengthFieldLength;
          f1 = f2;
          localObject5 = localObject3;
          paramInt1 = k;
          m = j;
          break;
        }
      }
      if (i3 == Atom.TYPE_vpcC)
      {
        if (localObject6 == null)
        {
          bool = true;
          label486:
          Assertions.checkState(bool);
          if (i != Atom.TYPE_vp08) {
            break label532;
          }
        }
        label532:
        for (paramDrmInitData = "video/x-vnd.on2.vp8";; paramDrmInitData = "video/x-vnd.on2.vp9")
        {
          localObject1 = localObject4;
          f1 = f2;
          localObject5 = localObject3;
          paramInt1 = k;
          m = j;
          break;
          bool = false;
          break label486;
        }
      }
      if (i3 == Atom.TYPE_d263)
      {
        if (localObject6 == null) {}
        for (bool = true;; bool = false)
        {
          Assertions.checkState(bool);
          paramDrmInitData = "video/3gpp";
          localObject1 = localObject4;
          f1 = f2;
          localObject5 = localObject3;
          paramInt1 = k;
          m = j;
          break;
        }
      }
      if (i3 == Atom.TYPE_esds)
      {
        if (localObject6 == null) {}
        for (bool = true;; bool = false)
        {
          Assertions.checkState(bool);
          localObject1 = parseEsdsFromParent(paramParsableByteArray, paramInt1);
          paramDrmInitData = (String)((Pair)localObject1).first;
          localObject1 = Collections.singletonList(((Pair)localObject1).second);
          f1 = f2;
          localObject5 = localObject3;
          paramInt1 = k;
          m = j;
          break;
        }
      }
      if (i3 == Atom.TYPE_pasp)
      {
        f1 = parsePaspFromParent(paramParsableByteArray, paramInt1);
        m = 1;
        paramDrmInitData = (DrmInitData)localObject6;
        localObject1 = localObject4;
        localObject5 = localObject3;
        paramInt1 = k;
      }
      else if (i3 == Atom.TYPE_sv3d)
      {
        localObject5 = parseProjFromParent(paramParsableByteArray, paramInt1, i2);
        paramDrmInitData = (DrmInitData)localObject6;
        localObject1 = localObject4;
        f1 = f2;
        paramInt1 = k;
        m = j;
      }
      else
      {
        paramDrmInitData = (DrmInitData)localObject6;
        localObject1 = localObject4;
        f1 = f2;
        localObject5 = localObject3;
        paramInt1 = k;
        m = j;
        if (i3 == Atom.TYPE_st3d)
        {
          i3 = paramParsableByteArray.readUnsignedByte();
          paramParsableByteArray.skipBytes(3);
          paramDrmInitData = (DrmInitData)localObject6;
          localObject1 = localObject4;
          f1 = f2;
          localObject5 = localObject3;
          paramInt1 = k;
          m = j;
          if (i3 == 0) {
            switch (paramParsableByteArray.readUnsignedByte())
            {
            default: 
              paramDrmInitData = (DrmInitData)localObject6;
              localObject1 = localObject4;
              f1 = f2;
              localObject5 = localObject3;
              paramInt1 = k;
              m = j;
              break;
            case 0: 
              paramInt1 = 0;
              paramDrmInitData = (DrmInitData)localObject6;
              localObject1 = localObject4;
              f1 = f2;
              localObject5 = localObject3;
              m = j;
              break;
            case 1: 
              paramInt1 = 1;
              paramDrmInitData = (DrmInitData)localObject6;
              localObject1 = localObject4;
              f1 = f2;
              localObject5 = localObject3;
              m = j;
              break;
            case 2: 
              paramInt1 = 2;
              paramDrmInitData = (DrmInitData)localObject6;
              localObject1 = localObject4;
              f1 = f2;
              localObject5 = localObject3;
              m = j;
              break;
            case 3: 
              paramInt1 = 3;
              paramDrmInitData = (DrmInitData)localObject6;
              localObject1 = localObject4;
              f1 = f2;
              localObject5 = localObject3;
              m = j;
            }
          }
        }
      }
    }
    label970:
    paramStsdData.format = Format.createVideoSampleFormat(Integer.toString(paramInt4), (String)localObject6, null, -1, -1, n, i1, -1.0F, (List)localObject4, paramInt5, f2, (byte[])localObject3, k, null, (DrmInitData)localObject2);
  }
  
  private static final class ChunkIterator
  {
    private final ParsableByteArray chunkOffsets;
    private final boolean chunkOffsetsAreLongs;
    public int index;
    public final int length;
    private int nextSamplesPerChunkChangeIndex;
    public int numSamples;
    public long offset;
    private int remainingSamplesPerChunkChanges;
    private final ParsableByteArray stsc;
    
    public ChunkIterator(ParsableByteArray paramParsableByteArray1, ParsableByteArray paramParsableByteArray2, boolean paramBoolean)
    {
      this.stsc = paramParsableByteArray1;
      this.chunkOffsets = paramParsableByteArray2;
      this.chunkOffsetsAreLongs = paramBoolean;
      paramParsableByteArray2.setPosition(12);
      this.length = paramParsableByteArray2.readUnsignedIntToInt();
      paramParsableByteArray1.setPosition(12);
      this.remainingSamplesPerChunkChanges = paramParsableByteArray1.readUnsignedIntToInt();
      if (paramParsableByteArray1.readInt() == 1) {}
      for (paramBoolean = bool;; paramBoolean = false)
      {
        Assertions.checkState(paramBoolean, "first_chunk must be 1");
        this.index = -1;
        return;
      }
    }
    
    public boolean moveNext()
    {
      int i = this.index + 1;
      this.index = i;
      if (i == this.length) {
        return false;
      }
      long l;
      if (this.chunkOffsetsAreLongs)
      {
        l = this.chunkOffsets.readUnsignedLongToLong();
        this.offset = l;
        if (this.index == this.nextSamplesPerChunkChangeIndex)
        {
          this.numSamples = this.stsc.readUnsignedIntToInt();
          this.stsc.skipBytes(4);
          i = this.remainingSamplesPerChunkChanges - 1;
          this.remainingSamplesPerChunkChanges = i;
          if (i <= 0) {
            break label116;
          }
        }
      }
      label116:
      for (i = this.stsc.readUnsignedIntToInt() - 1;; i = -1)
      {
        this.nextSamplesPerChunkChangeIndex = i;
        return true;
        l = this.chunkOffsets.readUnsignedInt();
        break;
      }
    }
  }
  
  private static abstract interface SampleSizeBox
  {
    public abstract int getSampleCount();
    
    public abstract boolean isFixedSampleSize();
    
    public abstract int readNextSampleSize();
  }
  
  private static final class StsdData
  {
    public static final int STSD_HEADER_SIZE = 8;
    public Format format;
    public int nalUnitLengthFieldLength;
    public int requiredSampleTransformation;
    public final TrackEncryptionBox[] trackEncryptionBoxes;
    
    public StsdData(int paramInt)
    {
      this.trackEncryptionBoxes = new TrackEncryptionBox[paramInt];
      this.requiredSampleTransformation = 0;
    }
  }
  
  static final class StszSampleSizeBox
    implements AtomParsers.SampleSizeBox
  {
    private final ParsableByteArray data;
    private final int fixedSampleSize;
    private final int sampleCount;
    
    public StszSampleSizeBox(Atom.LeafAtom paramLeafAtom)
    {
      this.data = paramLeafAtom.data;
      this.data.setPosition(12);
      this.fixedSampleSize = this.data.readUnsignedIntToInt();
      this.sampleCount = this.data.readUnsignedIntToInt();
    }
    
    public int getSampleCount()
    {
      return this.sampleCount;
    }
    
    public boolean isFixedSampleSize()
    {
      return this.fixedSampleSize != 0;
    }
    
    public int readNextSampleSize()
    {
      if (this.fixedSampleSize == 0) {
        return this.data.readUnsignedIntToInt();
      }
      return this.fixedSampleSize;
    }
  }
  
  static final class Stz2SampleSizeBox
    implements AtomParsers.SampleSizeBox
  {
    private int currentByte;
    private final ParsableByteArray data;
    private final int fieldSize;
    private final int sampleCount;
    private int sampleIndex;
    
    public Stz2SampleSizeBox(Atom.LeafAtom paramLeafAtom)
    {
      this.data = paramLeafAtom.data;
      this.data.setPosition(12);
      this.fieldSize = (this.data.readUnsignedIntToInt() & 0xFF);
      this.sampleCount = this.data.readUnsignedIntToInt();
    }
    
    public int getSampleCount()
    {
      return this.sampleCount;
    }
    
    public boolean isFixedSampleSize()
    {
      return false;
    }
    
    public int readNextSampleSize()
    {
      if (this.fieldSize == 8) {
        return this.data.readUnsignedByte();
      }
      if (this.fieldSize == 16) {
        return this.data.readUnsignedShort();
      }
      int i = this.sampleIndex;
      this.sampleIndex = (i + 1);
      if (i % 2 == 0)
      {
        this.currentByte = this.data.readUnsignedByte();
        return (this.currentByte & 0xF0) >> 4;
      }
      return this.currentByte & 0xF;
    }
  }
  
  private static final class TkhdData
  {
    private final long duration;
    private final int id;
    private final int rotationDegrees;
    
    public TkhdData(int paramInt1, long paramLong, int paramInt2)
    {
      this.id = paramInt1;
      this.duration = paramLong;
      this.rotationDegrees = paramInt2;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/mp4/AtomParsers.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */