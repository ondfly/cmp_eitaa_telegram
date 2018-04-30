package org.telegram.messenger.exoplayer2.source.dash.manifest;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.messenger.exoplayer2.C;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.ParserException;
import org.telegram.messenger.exoplayer2.drm.DrmInitData;
import org.telegram.messenger.exoplayer2.drm.DrmInitData.SchemeData;
import org.telegram.messenger.exoplayer2.extractor.mp4.PsshAtomUtil;
import org.telegram.messenger.exoplayer2.metadata.emsg.EventMessage;
import org.telegram.messenger.exoplayer2.upstream.ParsingLoadable.Parser;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.MimeTypes;
import org.telegram.messenger.exoplayer2.util.UriUtil;
import org.telegram.messenger.exoplayer2.util.Util;
import org.telegram.messenger.exoplayer2.util.XmlPullParserUtil;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class DashManifestParser
  extends DefaultHandler
  implements ParsingLoadable.Parser<DashManifest>
{
  private static final Pattern CEA_608_ACCESSIBILITY_PATTERN = Pattern.compile("CC([1-4])=.*");
  private static final Pattern CEA_708_ACCESSIBILITY_PATTERN = Pattern.compile("([1-9]|[1-5][0-9]|6[0-3])=.*");
  private static final Pattern FRAME_RATE_PATTERN = Pattern.compile("(\\d+)(?:/(\\d+))?");
  private static final String TAG = "MpdParser";
  private final String contentId;
  private final XmlPullParserFactory xmlParserFactory;
  
  public DashManifestParser()
  {
    this(null);
  }
  
  public DashManifestParser(String paramString)
  {
    this.contentId = paramString;
    try
    {
      this.xmlParserFactory = XmlPullParserFactory.newInstance();
      return;
    }
    catch (XmlPullParserException paramString)
    {
      throw new RuntimeException("Couldn't create XmlPullParserFactory instance", paramString);
    }
  }
  
  private static int checkContentTypeConsistency(int paramInt1, int paramInt2)
  {
    int i;
    if (paramInt1 == -1) {
      i = paramInt2;
    }
    do
    {
      return i;
      i = paramInt1;
    } while (paramInt2 == -1);
    if (paramInt1 == paramInt2) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      return paramInt1;
    }
  }
  
  private static String checkLanguageConsistency(String paramString1, String paramString2)
  {
    String str;
    if (paramString1 == null) {
      str = paramString2;
    }
    do
    {
      return str;
      str = paramString1;
    } while (paramString2 == null);
    Assertions.checkState(paramString1.equals(paramString2));
    return paramString1;
  }
  
  private static void filterRedundantIncompleteSchemeDatas(ArrayList<DrmInitData.SchemeData> paramArrayList)
  {
    int i = paramArrayList.size() - 1;
    if (i >= 0)
    {
      DrmInitData.SchemeData localSchemeData = (DrmInitData.SchemeData)paramArrayList.get(i);
      int j;
      if (!localSchemeData.hasData()) {
        j = 0;
      }
      for (;;)
      {
        if (j < paramArrayList.size())
        {
          if (((DrmInitData.SchemeData)paramArrayList.get(j)).canReplace(localSchemeData)) {
            paramArrayList.remove(i);
          }
        }
        else
        {
          i -= 1;
          break;
        }
        j += 1;
      }
    }
  }
  
  private static String getSampleMimeType(String paramString1, String paramString2)
  {
    String str;
    if (MimeTypes.isAudio(paramString1)) {
      str = MimeTypes.getAudioMediaMimeType(paramString2);
    }
    do
    {
      return str;
      if (MimeTypes.isVideo(paramString1)) {
        return MimeTypes.getVideoMediaMimeType(paramString2);
      }
      str = paramString1;
    } while (mimeTypeIsRawText(paramString1));
    if ("application/mp4".equals(paramString1))
    {
      if ("stpp".equals(paramString2)) {
        return "application/ttml+xml";
      }
      if ("wvtt".equals(paramString2)) {
        return "application/x-mp4-vtt";
      }
    }
    else if ("application/x-rawcc".equals(paramString1))
    {
      if (paramString2 != null)
      {
        if (paramString2.contains("cea708")) {
          return "application/cea-708";
        }
        if ((paramString2.contains("eia608")) || (paramString2.contains("cea608"))) {
          return "application/cea-608";
        }
      }
      return null;
    }
    return null;
  }
  
  private static boolean mimeTypeIsRawText(String paramString)
  {
    return (MimeTypes.isText(paramString)) || ("application/ttml+xml".equals(paramString)) || ("application/x-mp4-vtt".equals(paramString)) || ("application/cea-708".equals(paramString)) || ("application/cea-608".equals(paramString));
  }
  
  protected static String parseBaseUrl(XmlPullParser paramXmlPullParser, String paramString)
    throws XmlPullParserException, IOException
  {
    paramXmlPullParser.next();
    return UriUtil.resolve(paramString, paramXmlPullParser.getText());
  }
  
  protected static int parseCea608AccessibilityChannel(List<Descriptor> paramList)
  {
    int i = 0;
    while (i < paramList.size())
    {
      Descriptor localDescriptor = (Descriptor)paramList.get(i);
      if (("urn:scte:dash:cc:cea-608:2015".equals(localDescriptor.schemeIdUri)) && (localDescriptor.value != null))
      {
        Matcher localMatcher = CEA_608_ACCESSIBILITY_PATTERN.matcher(localDescriptor.value);
        if (localMatcher.matches()) {
          return Integer.parseInt(localMatcher.group(1));
        }
        Log.w("MpdParser", "Unable to parse CEA-608 channel number from: " + localDescriptor.value);
      }
      i += 1;
    }
    return -1;
  }
  
  protected static int parseCea708AccessibilityChannel(List<Descriptor> paramList)
  {
    int i = 0;
    while (i < paramList.size())
    {
      Descriptor localDescriptor = (Descriptor)paramList.get(i);
      if (("urn:scte:dash:cc:cea-708:2015".equals(localDescriptor.schemeIdUri)) && (localDescriptor.value != null))
      {
        Matcher localMatcher = CEA_708_ACCESSIBILITY_PATTERN.matcher(localDescriptor.value);
        if (localMatcher.matches()) {
          return Integer.parseInt(localMatcher.group(1));
        }
        Log.w("MpdParser", "Unable to parse CEA-708 service block number from: " + localDescriptor.value);
      }
      i += 1;
    }
    return -1;
  }
  
  protected static long parseDateTime(XmlPullParser paramXmlPullParser, String paramString, long paramLong)
    throws ParserException
  {
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, paramString);
    if (paramXmlPullParser == null) {
      return paramLong;
    }
    return Util.parseXsDateTime(paramXmlPullParser);
  }
  
  protected static Descriptor parseDescriptor(XmlPullParser paramXmlPullParser, String paramString)
    throws XmlPullParserException, IOException
  {
    String str1 = parseString(paramXmlPullParser, "schemeIdUri", "");
    String str2 = parseString(paramXmlPullParser, "value", null);
    String str3 = parseString(paramXmlPullParser, "id", null);
    do
    {
      paramXmlPullParser.next();
    } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, paramString));
    return new Descriptor(str1, str2, str3);
  }
  
  protected static int parseDolbyChannelConfiguration(XmlPullParser paramXmlPullParser)
  {
    paramXmlPullParser = Util.toLowerInvariant(paramXmlPullParser.getAttributeValue(null, "value"));
    if (paramXmlPullParser == null) {
      return -1;
    }
    label68:
    int i;
    switch (paramXmlPullParser.hashCode())
    {
    default: 
      i = -1;
    }
    for (;;)
    {
      switch (i)
      {
      default: 
        return -1;
      case 0: 
        return 1;
        if (!paramXmlPullParser.equals("4000")) {
          break label68;
        }
        i = 0;
        continue;
        if (!paramXmlPullParser.equals("a000")) {
          break label68;
        }
        i = 1;
        continue;
        if (!paramXmlPullParser.equals("f801")) {
          break label68;
        }
        i = 2;
        continue;
        if (!paramXmlPullParser.equals("fa01")) {
          break label68;
        }
        i = 3;
      }
    }
    return 2;
    return 6;
    return 8;
  }
  
  protected static long parseDuration(XmlPullParser paramXmlPullParser, String paramString, long paramLong)
  {
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, paramString);
    if (paramXmlPullParser == null) {
      return paramLong;
    }
    return Util.parseXsDuration(paramXmlPullParser);
  }
  
  protected static String parseEac3SupplementalProperties(List<Descriptor> paramList)
  {
    int i = 0;
    while (i < paramList.size())
    {
      Descriptor localDescriptor = (Descriptor)paramList.get(i);
      if (("tag:dolby.com,2014:dash:DolbyDigitalPlusExtensionType:2014".equals(localDescriptor.schemeIdUri)) && ("ec+3".equals(localDescriptor.value))) {
        return "audio/eac3-joc";
      }
      i += 1;
    }
    return "audio/eac3";
  }
  
  protected static float parseFrameRate(XmlPullParser paramXmlPullParser, float paramFloat)
  {
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, "frameRate");
    float f = paramFloat;
    int i;
    if (paramXmlPullParser != null)
    {
      paramXmlPullParser = FRAME_RATE_PATTERN.matcher(paramXmlPullParser);
      f = paramFloat;
      if (paramXmlPullParser.matches())
      {
        i = Integer.parseInt(paramXmlPullParser.group(1));
        paramXmlPullParser = paramXmlPullParser.group(2);
        if (TextUtils.isEmpty(paramXmlPullParser)) {
          break label67;
        }
        f = i / Integer.parseInt(paramXmlPullParser);
      }
    }
    return f;
    label67:
    return i;
  }
  
  protected static int parseInt(XmlPullParser paramXmlPullParser, String paramString, int paramInt)
  {
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, paramString);
    if (paramXmlPullParser == null) {
      return paramInt;
    }
    return Integer.parseInt(paramXmlPullParser);
  }
  
  protected static long parseLong(XmlPullParser paramXmlPullParser, String paramString, long paramLong)
  {
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, paramString);
    if (paramXmlPullParser == null) {
      return paramLong;
    }
    return Long.parseLong(paramXmlPullParser);
  }
  
  protected static String parseString(XmlPullParser paramXmlPullParser, String paramString1, String paramString2)
  {
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, paramString1);
    if (paramXmlPullParser == null) {
      return paramString2;
    }
    return paramXmlPullParser;
  }
  
  protected AdaptationSet buildAdaptationSet(int paramInt1, int paramInt2, List<Representation> paramList, List<Descriptor> paramList1, List<Descriptor> paramList2)
  {
    return new AdaptationSet(paramInt1, paramInt2, paramList, paramList1, paramList2);
  }
  
  protected EventMessage buildEvent(String paramString1, String paramString2, long paramLong1, long paramLong2, byte[] paramArrayOfByte, long paramLong3)
  {
    return new EventMessage(paramString1, paramString2, paramLong2, paramLong1, paramArrayOfByte, paramLong3);
  }
  
  protected EventStream buildEventStream(String paramString1, String paramString2, long paramLong, long[] paramArrayOfLong, EventMessage[] paramArrayOfEventMessage)
  {
    return new EventStream(paramString1, paramString2, paramLong, paramArrayOfLong, paramArrayOfEventMessage);
  }
  
  protected Format buildFormat(String paramString1, String paramString2, int paramInt1, int paramInt2, float paramFloat, int paramInt3, int paramInt4, int paramInt5, String paramString3, int paramInt6, List<Descriptor> paramList1, String paramString4, List<Descriptor> paramList2)
  {
    String str2 = getSampleMimeType(paramString2, paramString4);
    Object localObject = str2;
    if (str2 != null)
    {
      String str1 = str2;
      if ("audio/eac3".equals(str2)) {
        str1 = parseEac3SupplementalProperties(paramList2);
      }
      if (MimeTypes.isVideo(str1)) {
        return Format.createVideoContainerFormat(paramString1, paramString2, str1, paramString4, paramInt5, paramInt1, paramInt2, paramFloat, null, paramInt6);
      }
      if (MimeTypes.isAudio(str1)) {
        return Format.createAudioContainerFormat(paramString1, paramString2, str1, paramString4, paramInt5, paramInt3, paramInt4, null, paramInt6, paramString3);
      }
      localObject = str1;
      if (mimeTypeIsRawText(str1))
      {
        if ("application/cea-608".equals(str1)) {
          paramInt1 = parseCea608AccessibilityChannel(paramList1);
        }
        for (;;)
        {
          return Format.createTextContainerFormat(paramString1, paramString2, str1, paramString4, paramInt5, paramInt6, paramString3, paramInt1);
          if ("application/cea-708".equals(str1)) {
            paramInt1 = parseCea708AccessibilityChannel(paramList1);
          } else {
            paramInt1 = -1;
          }
        }
      }
    }
    return Format.createContainerFormat(paramString1, paramString2, (String)localObject, paramString4, paramInt5, paramInt6, paramString3);
  }
  
  protected DashManifest buildMediaPresentationDescription(long paramLong1, long paramLong2, long paramLong3, boolean paramBoolean, long paramLong4, long paramLong5, long paramLong6, long paramLong7, UtcTimingElement paramUtcTimingElement, Uri paramUri, List<Period> paramList)
  {
    return new DashManifest(paramLong1, paramLong2, paramLong3, paramBoolean, paramLong4, paramLong5, paramLong6, paramLong7, paramUtcTimingElement, paramUri, paramList);
  }
  
  protected Period buildPeriod(String paramString, long paramLong, List<AdaptationSet> paramList, List<EventStream> paramList1)
  {
    return new Period(paramString, paramLong, paramList, paramList1);
  }
  
  protected RangedUri buildRangedUri(String paramString, long paramLong1, long paramLong2)
  {
    return new RangedUri(paramString, paramLong1, paramLong2);
  }
  
  protected Representation buildRepresentation(RepresentationInfo paramRepresentationInfo, String paramString1, String paramString2, ArrayList<DrmInitData.SchemeData> paramArrayList, ArrayList<Descriptor> paramArrayList1)
  {
    Format localFormat = paramRepresentationInfo.format;
    if (paramRepresentationInfo.drmSchemeType != null) {
      paramString2 = paramRepresentationInfo.drmSchemeType;
    }
    for (;;)
    {
      ArrayList localArrayList = paramRepresentationInfo.drmSchemeDatas;
      localArrayList.addAll(paramArrayList);
      paramArrayList = localFormat;
      if (!localArrayList.isEmpty())
      {
        filterRedundantIncompleteSchemeDatas(localArrayList);
        paramArrayList = localFormat.copyWithDrmInitData(new DrmInitData(paramString2, localArrayList));
      }
      paramString2 = paramRepresentationInfo.inbandEventStreams;
      paramString2.addAll(paramArrayList1);
      return Representation.newInstance(paramString1, paramRepresentationInfo.revisionId, paramArrayList, paramRepresentationInfo.baseUrl, paramRepresentationInfo.segmentBase, paramString2);
    }
  }
  
  protected SegmentBase.SegmentList buildSegmentList(RangedUri paramRangedUri, long paramLong1, long paramLong2, int paramInt, long paramLong3, List<SegmentBase.SegmentTimelineElement> paramList, List<RangedUri> paramList1)
  {
    return new SegmentBase.SegmentList(paramRangedUri, paramLong1, paramLong2, paramInt, paramLong3, paramList, paramList1);
  }
  
  protected SegmentBase.SegmentTemplate buildSegmentTemplate(RangedUri paramRangedUri, long paramLong1, long paramLong2, int paramInt, long paramLong3, List<SegmentBase.SegmentTimelineElement> paramList, UrlTemplate paramUrlTemplate1, UrlTemplate paramUrlTemplate2)
  {
    return new SegmentBase.SegmentTemplate(paramRangedUri, paramLong1, paramLong2, paramInt, paramLong3, paramList, paramUrlTemplate1, paramUrlTemplate2);
  }
  
  protected SegmentBase.SegmentTimelineElement buildSegmentTimelineElement(long paramLong1, long paramLong2)
  {
    return new SegmentBase.SegmentTimelineElement(paramLong1, paramLong2);
  }
  
  protected SegmentBase.SingleSegmentBase buildSingleSegmentBase(RangedUri paramRangedUri, long paramLong1, long paramLong2, long paramLong3, long paramLong4)
  {
    return new SegmentBase.SingleSegmentBase(paramRangedUri, paramLong1, paramLong2, paramLong3, paramLong4);
  }
  
  protected UtcTimingElement buildUtcTimingElement(String paramString1, String paramString2)
  {
    return new UtcTimingElement(paramString1, paramString2);
  }
  
  protected int getContentType(Format paramFormat)
  {
    paramFormat = paramFormat.sampleMimeType;
    if (TextUtils.isEmpty(paramFormat)) {}
    do
    {
      return -1;
      if (MimeTypes.isVideo(paramFormat)) {
        return 2;
      }
      if (MimeTypes.isAudio(paramFormat)) {
        return 1;
      }
    } while (!mimeTypeIsRawText(paramFormat));
    return 3;
  }
  
  public DashManifest parse(Uri paramUri, InputStream paramInputStream)
    throws IOException
  {
    XmlPullParser localXmlPullParser;
    try
    {
      localXmlPullParser = this.xmlParserFactory.newPullParser();
      localXmlPullParser.setInput(paramInputStream, null);
      if ((localXmlPullParser.next() != 2) || (!"MPD".equals(localXmlPullParser.getName()))) {
        throw new ParserException("inputStream does not contain a valid media presentation description");
      }
    }
    catch (XmlPullParserException paramUri)
    {
      throw new ParserException(paramUri);
    }
    paramUri = parseMediaPresentationDescription(localXmlPullParser, paramUri.toString());
    return paramUri;
  }
  
  protected AdaptationSet parseAdaptationSet(XmlPullParser paramXmlPullParser, String paramString, SegmentBase paramSegmentBase)
    throws XmlPullParserException, IOException
  {
    int i4 = parseInt(paramXmlPullParser, "id", -1);
    int m = parseContentType(paramXmlPullParser);
    String str1 = paramXmlPullParser.getAttributeValue(null, "mimeType");
    String str2 = paramXmlPullParser.getAttributeValue(null, "codecs");
    int i5 = parseInt(paramXmlPullParser, "width", -1);
    int i6 = parseInt(paramXmlPullParser, "height", -1);
    float f = parseFrameRate(paramXmlPullParser, -1.0F);
    int n = -1;
    int i7 = parseInt(paramXmlPullParser, "audioSamplingRate", -1);
    Object localObject3 = paramXmlPullParser.getAttributeValue(null, "lang");
    SegmentBase localSegmentBase = null;
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    ArrayList localArrayList3 = new ArrayList();
    ArrayList localArrayList4 = new ArrayList();
    ArrayList localArrayList5 = new ArrayList();
    int k = 0;
    int j = 0;
    Object localObject2 = paramSegmentBase;
    Object localObject1 = paramString;
    paramSegmentBase = localSegmentBase;
    paramXmlPullParser.next();
    int i1;
    Object localObject4;
    int i2;
    int i;
    int i3;
    Object localObject5;
    if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "BaseURL"))
    {
      i1 = n;
      localObject4 = localObject3;
      i2 = k;
      localSegmentBase = paramSegmentBase;
      i = m;
      i3 = j;
      localObject5 = localObject1;
      paramString = (String)localObject2;
      if (j == 0)
      {
        localObject5 = parseBaseUrl(paramXmlPullParser, (String)localObject1);
        i3 = 1;
        paramString = (String)localObject2;
        i = m;
        localSegmentBase = paramSegmentBase;
        i2 = k;
        localObject4 = localObject3;
        i1 = n;
      }
    }
    for (;;)
    {
      n = i1;
      localObject3 = localObject4;
      k = i2;
      paramSegmentBase = localSegmentBase;
      m = i;
      j = i3;
      localObject1 = localObject5;
      localObject2 = paramString;
      if (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "AdaptationSet")) {
        break;
      }
      paramXmlPullParser = new ArrayList(localArrayList5.size());
      j = 0;
      while (j < localArrayList5.size())
      {
        paramXmlPullParser.add(buildRepresentation((RepresentationInfo)localArrayList5.get(j), this.contentId, localSegmentBase, localArrayList1, localArrayList2));
        j += 1;
      }
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "ContentProtection"))
      {
        Pair localPair = parseContentProtection(paramXmlPullParser);
        if (localPair.first != null) {
          paramSegmentBase = (String)localPair.first;
        }
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
        if (localPair.second != null)
        {
          localArrayList1.add(localPair.second);
          i1 = n;
          localObject4 = localObject3;
          i2 = k;
          localSegmentBase = paramSegmentBase;
          i = m;
          i3 = j;
          localObject5 = localObject1;
          paramString = (String)localObject2;
        }
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "ContentComponent"))
      {
        localObject4 = checkLanguageConsistency((String)localObject3, paramXmlPullParser.getAttributeValue(null, "lang"));
        i = checkContentTypeConsistency(m, parseContentType(paramXmlPullParser));
        i1 = n;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "Role"))
      {
        i2 = k | parseRole(paramXmlPullParser);
        i1 = n;
        localObject4 = localObject3;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "AudioChannelConfiguration"))
      {
        i1 = parseAudioChannelConfiguration(paramXmlPullParser);
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "Accessibility"))
      {
        localArrayList3.add(parseDescriptor(paramXmlPullParser, "Accessibility"));
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SupplementalProperty"))
      {
        localArrayList4.add(parseDescriptor(paramXmlPullParser, "SupplementalProperty"));
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "Representation"))
      {
        paramString = parseRepresentation(paramXmlPullParser, (String)localObject1, str1, str2, i5, i6, f, n, i7, (String)localObject3, k, localArrayList3, (SegmentBase)localObject2);
        i = checkContentTypeConsistency(m, getContentType(paramString.format));
        localArrayList5.add(paramString);
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentBase"))
      {
        paramString = parseSegmentBase(paramXmlPullParser, (SegmentBase.SingleSegmentBase)localObject2);
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentList"))
      {
        paramString = parseSegmentList(paramXmlPullParser, (SegmentBase.SegmentList)localObject2);
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentTemplate"))
      {
        paramString = parseSegmentTemplate(paramXmlPullParser, (SegmentBase.SegmentTemplate)localObject2);
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "InbandEventStream"))
      {
        localArrayList2.add(parseDescriptor(paramXmlPullParser, "InbandEventStream"));
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
      }
      else
      {
        i1 = n;
        localObject4 = localObject3;
        i2 = k;
        localSegmentBase = paramSegmentBase;
        i = m;
        i3 = j;
        localObject5 = localObject1;
        paramString = (String)localObject2;
        if (XmlPullParserUtil.isStartTag(paramXmlPullParser))
        {
          parseAdaptationSetChild(paramXmlPullParser);
          i1 = n;
          localObject4 = localObject3;
          i2 = k;
          localSegmentBase = paramSegmentBase;
          i = m;
          i3 = j;
          localObject5 = localObject1;
          paramString = (String)localObject2;
        }
      }
    }
    return buildAdaptationSet(i4, i, paramXmlPullParser, localArrayList3, localArrayList4);
  }
  
  protected void parseAdaptationSetChild(XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {}
  
  protected int parseAudioChannelConfiguration(XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    int i = -1;
    String str = parseString(paramXmlPullParser, "schemeIdUri", null);
    if ("urn:mpeg:dash:23003:3:audio_channel_configuration:2011".equals(str)) {
      i = parseInt(paramXmlPullParser, "value", -1);
    }
    for (;;)
    {
      paramXmlPullParser.next();
      if (XmlPullParserUtil.isEndTag(paramXmlPullParser, "AudioChannelConfiguration"))
      {
        return i;
        if ("tag:dolby.com,2014:dash:audio_channel_configuration:2011".equals(str)) {
          i = parseDolbyChannelConfiguration(paramXmlPullParser);
        }
      }
    }
  }
  
  protected Pair<String, DrmInitData.SchemeData> parseContentProtection(XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    Object localObject5 = null;
    Object localObject2 = null;
    Object localObject6 = null;
    boolean bool1 = false;
    String str = paramXmlPullParser.getAttributeValue(null, "schemeIdUri");
    Object localObject3 = localObject2;
    boolean bool2 = bool1;
    Object localObject4 = localObject5;
    Object localObject1 = localObject6;
    int i;
    if (str != null)
    {
      localObject1 = Util.toLowerInvariant(str);
      i = -1;
    }
    switch (((String)localObject1).hashCode())
    {
    default: 
      switch (i)
      {
      default: 
        localObject1 = localObject6;
        localObject4 = localObject5;
        bool2 = bool1;
        localObject3 = localObject2;
        label135:
        label190:
        do
        {
          paramXmlPullParser.next();
          if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "widevine:license")) {
            break label482;
          }
          localObject2 = paramXmlPullParser.getAttributeValue(null, "robustness_level");
          if ((localObject2 == null) || (!((String)localObject2).startsWith("HW"))) {
            break;
          }
          bool1 = true;
          localObject5 = localObject1;
          localObject2 = localObject3;
          localObject3 = localObject2;
          bool2 = bool1;
          localObject1 = localObject5;
        } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "ContentProtection"));
        if (localObject5 == null) {}
        break;
      }
      break;
    }
    for (paramXmlPullParser = new DrmInitData.SchemeData((UUID)localObject5, "video/mp4", (byte[])localObject2, bool1);; paramXmlPullParser = null)
    {
      return Pair.create(localObject4, paramXmlPullParser);
      if (!((String)localObject1).equals("urn:mpeg:dash:mp4protection:2011")) {
        break;
      }
      i = 0;
      break;
      if (!((String)localObject1).equals("urn:uuid:9a04f079-9840-4286-ab92-e65be0885f95")) {
        break;
      }
      i = 1;
      break;
      if (!((String)localObject1).equals("urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")) {
        break;
      }
      i = 2;
      break;
      localObject5 = paramXmlPullParser.getAttributeValue(null, "value");
      str = paramXmlPullParser.getAttributeValue(null, "cenc:default_KID");
      localObject3 = localObject2;
      bool2 = bool1;
      localObject4 = localObject5;
      localObject1 = localObject6;
      if (TextUtils.isEmpty(str)) {
        break label135;
      }
      localObject3 = localObject2;
      bool2 = bool1;
      localObject4 = localObject5;
      localObject1 = localObject6;
      if ("00000000-0000-0000-0000-000000000000".equals(str)) {
        break label135;
      }
      localObject1 = str.split("\\s+");
      localObject2 = new UUID[localObject1.length];
      i = 0;
      while (i < localObject1.length)
      {
        localObject2[i] = UUID.fromString(localObject1[i]);
        i += 1;
      }
      localObject3 = PsshAtomUtil.buildPsshAtom(C.COMMON_PSSH_UUID, (UUID[])localObject2, null);
      localObject1 = C.COMMON_PSSH_UUID;
      bool2 = bool1;
      localObject4 = localObject5;
      break label135;
      localObject1 = C.PLAYREADY_UUID;
      localObject3 = localObject2;
      bool2 = bool1;
      localObject4 = localObject5;
      break label135;
      localObject1 = C.WIDEVINE_UUID;
      localObject3 = localObject2;
      bool2 = bool1;
      localObject4 = localObject5;
      break label135;
      bool1 = false;
      localObject2 = localObject3;
      localObject5 = localObject1;
      break label190;
      label482:
      localObject2 = localObject3;
      bool1 = bool2;
      localObject5 = localObject1;
      if (localObject3 != null) {
        break label190;
      }
      if ((XmlPullParserUtil.isStartTag(paramXmlPullParser, "cenc:pssh")) && (paramXmlPullParser.next() == 4))
      {
        localObject2 = Base64.decode(paramXmlPullParser.getText(), 0);
        localObject1 = PsshAtomUtil.parseUuid((byte[])localObject2);
        bool1 = bool2;
        localObject5 = localObject1;
        if (localObject1 != null) {
          break label190;
        }
        Log.w("MpdParser", "Skipping malformed cenc:pssh data");
        localObject2 = null;
        bool1 = bool2;
        localObject5 = localObject1;
        break label190;
      }
      localObject2 = localObject3;
      bool1 = bool2;
      localObject5 = localObject1;
      if (!C.PLAYREADY_UUID.equals(localObject1)) {
        break label190;
      }
      localObject2 = localObject3;
      bool1 = bool2;
      localObject5 = localObject1;
      if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "mspr:pro")) {
        break label190;
      }
      localObject2 = localObject3;
      bool1 = bool2;
      localObject5 = localObject1;
      if (paramXmlPullParser.next() != 4) {
        break label190;
      }
      localObject2 = PsshAtomUtil.buildPsshAtom(C.PLAYREADY_UUID, Base64.decode(paramXmlPullParser.getText(), 0));
      bool1 = bool2;
      localObject5 = localObject1;
      break label190;
    }
  }
  
  protected int parseContentType(XmlPullParser paramXmlPullParser)
  {
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, "contentType");
    if (TextUtils.isEmpty(paramXmlPullParser)) {}
    do
    {
      return -1;
      if ("audio".equals(paramXmlPullParser)) {
        return 1;
      }
      if ("video".equals(paramXmlPullParser)) {
        return 2;
      }
    } while (!"text".equals(paramXmlPullParser));
    return 3;
  }
  
  protected EventMessage parseEvent(XmlPullParser paramXmlPullParser, String paramString1, String paramString2, long paramLong, ByteArrayOutputStream paramByteArrayOutputStream)
    throws IOException, XmlPullParserException
  {
    long l1 = parseLong(paramXmlPullParser, "id", 0L);
    long l3 = parseLong(paramXmlPullParser, "duration", -9223372036854775807L);
    long l2 = parseLong(paramXmlPullParser, "presentationTime", 0L);
    l3 = Util.scaleLargeTimestamp(l3, 1000L, paramLong);
    paramLong = Util.scaleLargeTimestamp(l2, 1000000L, paramLong);
    return buildEvent(paramString1, paramString2, l1, l3, parseEventObject(paramXmlPullParser, paramByteArrayOutputStream), paramLong);
  }
  
  protected byte[] parseEventObject(XmlPullParser paramXmlPullParser, ByteArrayOutputStream paramByteArrayOutputStream)
    throws XmlPullParserException, IOException
  {
    paramByteArrayOutputStream.reset();
    XmlSerializer localXmlSerializer = Xml.newSerializer();
    localXmlSerializer.setOutput(paramByteArrayOutputStream, null);
    paramXmlPullParser.nextToken();
    if (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "Event"))
    {
      switch (paramXmlPullParser.getEventType())
      {
      }
      for (;;)
      {
        paramXmlPullParser.nextToken();
        break;
        localXmlSerializer.startDocument(null, Boolean.valueOf(false));
        continue;
        localXmlSerializer.endDocument();
        continue;
        localXmlSerializer.startTag(paramXmlPullParser.getNamespace(), paramXmlPullParser.getName());
        int i = 0;
        while (i < paramXmlPullParser.getAttributeCount())
        {
          localXmlSerializer.attribute(paramXmlPullParser.getAttributeNamespace(i), paramXmlPullParser.getAttributeName(i), paramXmlPullParser.getAttributeValue(i));
          i += 1;
        }
        localXmlSerializer.endTag(paramXmlPullParser.getNamespace(), paramXmlPullParser.getName());
        continue;
        localXmlSerializer.text(paramXmlPullParser.getText());
        continue;
        localXmlSerializer.cdsect(paramXmlPullParser.getText());
        continue;
        localXmlSerializer.entityRef(paramXmlPullParser.getText());
        continue;
        localXmlSerializer.ignorableWhitespace(paramXmlPullParser.getText());
        continue;
        localXmlSerializer.processingInstruction(paramXmlPullParser.getText());
        continue;
        localXmlSerializer.comment(paramXmlPullParser.getText());
        continue;
        localXmlSerializer.docdecl(paramXmlPullParser.getText());
      }
    }
    localXmlSerializer.flush();
    return paramByteArrayOutputStream.toByteArray();
  }
  
  protected EventStream parseEventStream(XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    String str1 = parseString(paramXmlPullParser, "schemeIdUri", "");
    String str2 = parseString(paramXmlPullParser, "value", "");
    long l = parseLong(paramXmlPullParser, "timescale", 1L);
    ArrayList localArrayList = new ArrayList();
    Object localObject = new ByteArrayOutputStream(512);
    do
    {
      paramXmlPullParser.next();
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "Event")) {
        localArrayList.add(parseEvent(paramXmlPullParser, str1, str2, l, (ByteArrayOutputStream)localObject));
      }
    } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "EventStream"));
    paramXmlPullParser = new long[localArrayList.size()];
    localObject = new EventMessage[localArrayList.size()];
    int i = 0;
    while (i < localArrayList.size())
    {
      EventMessage localEventMessage = (EventMessage)localArrayList.get(i);
      paramXmlPullParser[i] = localEventMessage.presentationTimeUs;
      localObject[i] = localEventMessage;
      i += 1;
    }
    return buildEventStream(str1, str2, l, paramXmlPullParser, (EventMessage[])localObject);
  }
  
  protected RangedUri parseInitialization(XmlPullParser paramXmlPullParser)
  {
    return parseRangedUrl(paramXmlPullParser, "sourceURL", "range");
  }
  
  protected DashManifest parseMediaPresentationDescription(XmlPullParser paramXmlPullParser, String paramString)
    throws XmlPullParserException, IOException
  {
    long l7 = parseDateTime(paramXmlPullParser, "availabilityStartTime", -9223372036854775807L);
    long l6 = parseDuration(paramXmlPullParser, "mediaPresentationDuration", -9223372036854775807L);
    long l8 = parseDuration(paramXmlPullParser, "minBufferTime", -9223372036854775807L);
    Object localObject1 = paramXmlPullParser.getAttributeValue(null, "type");
    boolean bool;
    long l2;
    label84:
    long l3;
    label101:
    long l4;
    label118:
    long l9;
    Object localObject2;
    ArrayList localArrayList;
    long l1;
    label155:
    int j;
    int i;
    long l5;
    Object localObject3;
    Object localObject4;
    int k;
    int m;
    String str;
    if ((localObject1 != null) && (((String)localObject1).equals("dynamic")))
    {
      bool = true;
      if (!bool) {
        break label318;
      }
      l2 = parseDuration(paramXmlPullParser, "minimumUpdatePeriod", -9223372036854775807L);
      if (!bool) {
        break label326;
      }
      l3 = parseDuration(paramXmlPullParser, "timeShiftBufferDepth", -9223372036854775807L);
      if (!bool) {
        break label334;
      }
      l4 = parseDuration(paramXmlPullParser, "suggestedPresentationDelay", -9223372036854775807L);
      l9 = parseDateTime(paramXmlPullParser, "publishTime", -9223372036854775807L);
      localObject2 = null;
      localObject1 = null;
      localArrayList = new ArrayList();
      if (!bool) {
        break label342;
      }
      l1 = -9223372036854775807L;
      j = 0;
      i = 0;
      l5 = l1;
      label233:
      do
      {
        paramXmlPullParser.next();
        if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "BaseURL")) {
          break;
        }
        localObject3 = localObject2;
        localObject4 = localObject1;
        l1 = l5;
        k = j;
        m = i;
        str = paramString;
        if (i == 0)
        {
          str = parseBaseUrl(paramXmlPullParser, paramString);
          m = 1;
          k = j;
          l1 = l5;
          localObject4 = localObject1;
          localObject3 = localObject2;
        }
        localObject2 = localObject3;
        localObject1 = localObject4;
        l5 = l1;
        j = k;
        i = m;
        paramString = str;
      } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "MPD"));
      l5 = l6;
      if (l6 == -9223372036854775807L)
      {
        if (l1 == -9223372036854775807L) {
          break label653;
        }
        l5 = l1;
      }
    }
    for (;;)
    {
      if (localArrayList.isEmpty())
      {
        throw new ParserException("No periods found.");
        bool = false;
        break;
        label318:
        l2 = -9223372036854775807L;
        break label84;
        label326:
        l3 = -9223372036854775807L;
        break label101;
        label334:
        l4 = -9223372036854775807L;
        break label118;
        label342:
        l1 = 0L;
        break label155;
        if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "UTCTiming"))
        {
          localObject3 = parseUtcTiming(paramXmlPullParser);
          localObject4 = localObject1;
          l1 = l5;
          k = j;
          m = i;
          str = paramString;
          break label233;
        }
        if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "Location"))
        {
          localObject4 = Uri.parse(paramXmlPullParser.nextText());
          localObject3 = localObject2;
          l1 = l5;
          k = j;
          m = i;
          str = paramString;
          break label233;
        }
        localObject3 = localObject2;
        localObject4 = localObject1;
        l1 = l5;
        k = j;
        m = i;
        str = paramString;
        if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "Period")) {
          break label233;
        }
        localObject3 = localObject2;
        localObject4 = localObject1;
        l1 = l5;
        k = j;
        m = i;
        str = paramString;
        if (j != 0) {
          break label233;
        }
        localObject3 = parsePeriod(paramXmlPullParser, paramString, l5);
        localObject4 = (Period)((Pair)localObject3).first;
        if (((Period)localObject4).startMs == -9223372036854775807L)
        {
          if (bool)
          {
            k = 1;
            localObject3 = localObject2;
            localObject4 = localObject1;
            l1 = l5;
            m = i;
            str = paramString;
            break label233;
          }
          throw new ParserException("Unable to determine start of period " + localArrayList.size());
        }
        l1 = ((Long)((Pair)localObject3).second).longValue();
        if (l1 == -9223372036854775807L) {}
        for (l1 = -9223372036854775807L;; l1 = ((Period)localObject4).startMs + l1)
        {
          localArrayList.add(localObject4);
          localObject3 = localObject2;
          localObject4 = localObject1;
          k = j;
          m = i;
          str = paramString;
          break;
        }
        label653:
        l5 = l6;
        if (!bool) {
          throw new ParserException("Unable to determine duration of static manifest.");
        }
      }
    }
    return buildMediaPresentationDescription(l7, l5, l8, bool, l2, l3, l4, l9, (UtcTimingElement)localObject3, (Uri)localObject4, localArrayList);
  }
  
  protected Pair<Period, Long> parsePeriod(XmlPullParser paramXmlPullParser, String paramString, long paramLong)
    throws XmlPullParserException, IOException
  {
    String str2 = paramXmlPullParser.getAttributeValue(null, "id");
    paramLong = parseDuration(paramXmlPullParser, "start", paramLong);
    long l = parseDuration(paramXmlPullParser, "duration", -9223372036854775807L);
    String str1 = null;
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    int i = 0;
    Object localObject1 = paramString;
    paramXmlPullParser.next();
    int j;
    Object localObject2;
    if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "BaseURL"))
    {
      j = i;
      paramString = str1;
      localObject2 = localObject1;
      if (i == 0)
      {
        localObject2 = parseBaseUrl(paramXmlPullParser, (String)localObject1);
        j = 1;
        paramString = str1;
      }
    }
    for (;;)
    {
      i = j;
      str1 = paramString;
      localObject1 = localObject2;
      if (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "Period")) {
        break;
      }
      return Pair.create(buildPeriod(str2, paramLong, localArrayList1, localArrayList2), Long.valueOf(l));
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "AdaptationSet"))
      {
        localArrayList1.add(parseAdaptationSet(paramXmlPullParser, (String)localObject1, str1));
        j = i;
        paramString = str1;
        localObject2 = localObject1;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "EventStream"))
      {
        localArrayList2.add(parseEventStream(paramXmlPullParser));
        j = i;
        paramString = str1;
        localObject2 = localObject1;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentBase"))
      {
        paramString = parseSegmentBase(paramXmlPullParser, null);
        j = i;
        localObject2 = localObject1;
      }
      else if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentList"))
      {
        paramString = parseSegmentList(paramXmlPullParser, null);
        j = i;
        localObject2 = localObject1;
      }
      else
      {
        j = i;
        paramString = str1;
        localObject2 = localObject1;
        if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentTemplate"))
        {
          paramString = parseSegmentTemplate(paramXmlPullParser, null);
          j = i;
          localObject2 = localObject1;
        }
      }
    }
  }
  
  protected RangedUri parseRangedUrl(XmlPullParser paramXmlPullParser, String paramString1, String paramString2)
  {
    paramString1 = paramXmlPullParser.getAttributeValue(null, paramString1);
    long l1 = 0L;
    long l3 = -1L;
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, paramString2);
    long l2 = l3;
    if (paramXmlPullParser != null)
    {
      paramXmlPullParser = paramXmlPullParser.split("-");
      long l4 = Long.parseLong(paramXmlPullParser[0]);
      l1 = l4;
      l2 = l3;
      if (paramXmlPullParser.length == 2)
      {
        l2 = Long.parseLong(paramXmlPullParser[1]) - l4 + 1L;
        l1 = l4;
      }
    }
    return buildRangedUri(paramString1, l1, l2);
  }
  
  protected RepresentationInfo parseRepresentation(XmlPullParser paramXmlPullParser, String paramString1, String paramString2, String paramString3, int paramInt1, int paramInt2, float paramFloat, int paramInt3, int paramInt4, String paramString4, int paramInt5, List<Descriptor> paramList, SegmentBase paramSegmentBase)
    throws XmlPullParserException, IOException
  {
    String str1 = paramXmlPullParser.getAttributeValue(null, "id");
    int i = parseInt(paramXmlPullParser, "bandwidth", -1);
    String str2 = parseString(paramXmlPullParser, "mimeType", paramString2);
    String str3 = parseString(paramXmlPullParser, "codecs", paramString3);
    int j = parseInt(paramXmlPullParser, "width", paramInt1);
    int k = parseInt(paramXmlPullParser, "height", paramInt2);
    paramFloat = parseFrameRate(paramXmlPullParser, paramFloat);
    paramInt2 = paramInt3;
    int m = parseInt(paramXmlPullParser, "audioSamplingRate", paramInt4);
    paramString2 = null;
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    ArrayList localArrayList3 = new ArrayList();
    paramInt1 = 0;
    Object localObject2 = paramSegmentBase;
    Object localObject1 = paramString1;
    do
    {
      paramXmlPullParser.next();
      if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "BaseURL")) {
        break;
      }
      paramInt3 = paramInt2;
      paramString3 = paramString2;
      paramInt4 = paramInt1;
      paramSegmentBase = (SegmentBase)localObject1;
      paramString1 = (String)localObject2;
      if (paramInt1 == 0)
      {
        paramSegmentBase = parseBaseUrl(paramXmlPullParser, (String)localObject1);
        paramInt4 = 1;
        paramString1 = (String)localObject2;
        paramString3 = paramString2;
        paramInt3 = paramInt2;
      }
      paramInt2 = paramInt3;
      paramString2 = paramString3;
      paramInt1 = paramInt4;
      localObject1 = paramSegmentBase;
      localObject2 = paramString1;
    } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "Representation"));
    paramXmlPullParser = buildFormat(str1, str2, j, k, paramFloat, paramInt3, m, i, paramString4, paramInt5, paramList, str3, localArrayList3);
    if (paramString1 != null) {}
    for (;;)
    {
      return new RepresentationInfo(paramXmlPullParser, paramSegmentBase, paramString1, paramString3, localArrayList1, localArrayList2, -1L);
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "AudioChannelConfiguration"))
      {
        paramInt3 = parseAudioChannelConfiguration(paramXmlPullParser);
        paramString3 = paramString2;
        paramInt4 = paramInt1;
        paramSegmentBase = (SegmentBase)localObject1;
        paramString1 = (String)localObject2;
        break;
      }
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentBase"))
      {
        paramString1 = parseSegmentBase(paramXmlPullParser, (SegmentBase.SingleSegmentBase)localObject2);
        paramInt3 = paramInt2;
        paramString3 = paramString2;
        paramInt4 = paramInt1;
        paramSegmentBase = (SegmentBase)localObject1;
        break;
      }
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentList"))
      {
        paramString1 = parseSegmentList(paramXmlPullParser, (SegmentBase.SegmentList)localObject2);
        paramInt3 = paramInt2;
        paramString3 = paramString2;
        paramInt4 = paramInt1;
        paramSegmentBase = (SegmentBase)localObject1;
        break;
      }
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentTemplate"))
      {
        paramString1 = parseSegmentTemplate(paramXmlPullParser, (SegmentBase.SegmentTemplate)localObject2);
        paramInt3 = paramInt2;
        paramString3 = paramString2;
        paramInt4 = paramInt1;
        paramSegmentBase = (SegmentBase)localObject1;
        break;
      }
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "ContentProtection"))
      {
        Pair localPair = parseContentProtection(paramXmlPullParser);
        if (localPair.first != null) {
          paramString2 = (String)localPair.first;
        }
        paramInt3 = paramInt2;
        paramString3 = paramString2;
        paramInt4 = paramInt1;
        paramSegmentBase = (SegmentBase)localObject1;
        paramString1 = (String)localObject2;
        if (localPair.second == null) {
          break;
        }
        localArrayList1.add(localPair.second);
        paramInt3 = paramInt2;
        paramString3 = paramString2;
        paramInt4 = paramInt1;
        paramSegmentBase = (SegmentBase)localObject1;
        paramString1 = (String)localObject2;
        break;
      }
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "InbandEventStream"))
      {
        localArrayList2.add(parseDescriptor(paramXmlPullParser, "InbandEventStream"));
        paramInt3 = paramInt2;
        paramString3 = paramString2;
        paramInt4 = paramInt1;
        paramSegmentBase = (SegmentBase)localObject1;
        paramString1 = (String)localObject2;
        break;
      }
      paramInt3 = paramInt2;
      paramString3 = paramString2;
      paramInt4 = paramInt1;
      paramSegmentBase = (SegmentBase)localObject1;
      paramString1 = (String)localObject2;
      if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "SupplementalProperty")) {
        break;
      }
      localArrayList3.add(parseDescriptor(paramXmlPullParser, "SupplementalProperty"));
      paramInt3 = paramInt2;
      paramString3 = paramString2;
      paramInt4 = paramInt1;
      paramSegmentBase = (SegmentBase)localObject1;
      paramString1 = (String)localObject2;
      break;
      paramString1 = new SegmentBase.SingleSegmentBase();
    }
  }
  
  protected int parseRole(XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    String str1 = parseString(paramXmlPullParser, "schemeIdUri", null);
    String str2 = parseString(paramXmlPullParser, "value", null);
    do
    {
      paramXmlPullParser.next();
    } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "Role"));
    if (("urn:mpeg:dash:role:2011".equals(str1)) && ("main".equals(str2))) {
      return 1;
    }
    return 0;
  }
  
  protected SegmentBase.SingleSegmentBase parseSegmentBase(XmlPullParser paramXmlPullParser, SegmentBase.SingleSegmentBase paramSingleSegmentBase)
    throws XmlPullParserException, IOException
  {
    long l1;
    long l3;
    label28:
    long l4;
    label47:
    long l2;
    label57:
    Object localObject;
    if (paramSingleSegmentBase != null)
    {
      l1 = paramSingleSegmentBase.timescale;
      l3 = parseLong(paramXmlPullParser, "timescale", l1);
      if (paramSingleSegmentBase == null) {
        break label173;
      }
      l1 = paramSingleSegmentBase.presentationTimeOffset;
      l4 = parseLong(paramXmlPullParser, "presentationTimeOffset", l1);
      if (paramSingleSegmentBase == null) {
        break label178;
      }
      l1 = paramSingleSegmentBase.indexStart;
      if (paramSingleSegmentBase == null) {
        break label183;
      }
      l2 = paramSingleSegmentBase.indexLength;
      localObject = paramXmlPullParser.getAttributeValue(null, "indexRange");
      if (localObject != null)
      {
        localObject = ((String)localObject).split("-");
        l1 = Long.parseLong(localObject[0]);
        l2 = Long.parseLong(localObject[1]) - l1 + 1L;
      }
      if (paramSingleSegmentBase == null) {
        break label189;
      }
      paramSingleSegmentBase = paramSingleSegmentBase.initialization;
    }
    for (;;)
    {
      paramXmlPullParser.next();
      localObject = paramSingleSegmentBase;
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "Initialization")) {
        localObject = parseInitialization(paramXmlPullParser);
      }
      paramSingleSegmentBase = (SegmentBase.SingleSegmentBase)localObject;
      if (XmlPullParserUtil.isEndTag(paramXmlPullParser, "SegmentBase"))
      {
        return buildSingleSegmentBase((RangedUri)localObject, l3, l4, l1, l2);
        l1 = 1L;
        break;
        label173:
        l1 = 0L;
        break label28;
        label178:
        l1 = 0L;
        break label47;
        label183:
        l2 = 0L;
        break label57;
        label189:
        paramSingleSegmentBase = null;
      }
    }
  }
  
  protected SegmentBase.SegmentList parseSegmentList(XmlPullParser paramXmlPullParser, SegmentBase.SegmentList paramSegmentList)
    throws XmlPullParserException, IOException
  {
    long l1;
    long l2;
    label31:
    long l3;
    label52:
    int i;
    label72:
    Object localObject6;
    Object localObject5;
    Object localObject4;
    Object localObject2;
    Object localObject3;
    Object localObject1;
    if (paramSegmentList != null)
    {
      l1 = paramSegmentList.timescale;
      l2 = parseLong(paramXmlPullParser, "timescale", l1);
      if (paramSegmentList == null) {
        break label208;
      }
      l1 = paramSegmentList.presentationTimeOffset;
      l3 = parseLong(paramXmlPullParser, "presentationTimeOffset", l1);
      if (paramSegmentList == null) {
        break label214;
      }
      l1 = paramSegmentList.duration;
      l1 = parseLong(paramXmlPullParser, "duration", l1);
      if (paramSegmentList == null) {
        break label222;
      }
      i = paramSegmentList.startNumber;
      i = parseInt(paramXmlPullParser, "startNumber", i);
      localObject6 = null;
      localObject5 = null;
      localObject4 = null;
      label122:
      do
      {
        paramXmlPullParser.next();
        if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "Initialization")) {
          break;
        }
        localObject2 = parseInitialization(paramXmlPullParser);
        localObject3 = localObject4;
        localObject1 = localObject5;
        localObject6 = localObject2;
        localObject5 = localObject1;
        localObject4 = localObject3;
      } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "SegmentList"));
      paramXmlPullParser = (XmlPullParser)localObject2;
      localObject4 = localObject1;
      localObject5 = localObject3;
      if (paramSegmentList != null)
      {
        if (localObject2 == null) {
          break label319;
        }
        label164:
        if (localObject1 == null) {
          break label328;
        }
        label169:
        if (localObject3 == null) {
          break label337;
        }
        localObject5 = localObject3;
        localObject4 = localObject1;
        paramXmlPullParser = (XmlPullParser)localObject2;
      }
    }
    for (;;)
    {
      return buildSegmentList(paramXmlPullParser, l2, l3, i, l1, (List)localObject4, (List)localObject5);
      l1 = 1L;
      break;
      label208:
      l1 = 0L;
      break label31;
      label214:
      l1 = -9223372036854775807L;
      break label52;
      label222:
      i = 1;
      break label72;
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentTimeline"))
      {
        localObject1 = parseSegmentTimeline(paramXmlPullParser);
        localObject2 = localObject6;
        localObject3 = localObject4;
        break label122;
      }
      localObject2 = localObject6;
      localObject1 = localObject5;
      localObject3 = localObject4;
      if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentURL")) {
        break label122;
      }
      localObject3 = localObject4;
      if (localObject4 == null) {
        localObject3 = new ArrayList();
      }
      ((List)localObject3).add(parseSegmentUrl(paramXmlPullParser));
      localObject2 = localObject6;
      localObject1 = localObject5;
      break label122;
      label319:
      localObject2 = paramSegmentList.initialization;
      break label164;
      label328:
      localObject1 = paramSegmentList.segmentTimeline;
      break label169;
      label337:
      localObject5 = paramSegmentList.mediaSegments;
      paramXmlPullParser = (XmlPullParser)localObject2;
      localObject4 = localObject1;
    }
  }
  
  protected SegmentBase.SegmentTemplate parseSegmentTemplate(XmlPullParser paramXmlPullParser, SegmentBase.SegmentTemplate paramSegmentTemplate)
    throws XmlPullParserException, IOException
  {
    long l1;
    long l2;
    label31:
    long l3;
    label52:
    int i;
    label72:
    Object localObject1;
    label91:
    UrlTemplate localUrlTemplate1;
    label113:
    UrlTemplate localUrlTemplate2;
    Object localObject4;
    Object localObject3;
    Object localObject2;
    if (paramSegmentTemplate != null)
    {
      l1 = paramSegmentTemplate.timescale;
      l2 = parseLong(paramXmlPullParser, "timescale", l1);
      if (paramSegmentTemplate == null) {
        break label230;
      }
      l1 = paramSegmentTemplate.presentationTimeOffset;
      l3 = parseLong(paramXmlPullParser, "presentationTimeOffset", l1);
      if (paramSegmentTemplate == null) {
        break label236;
      }
      l1 = paramSegmentTemplate.duration;
      l1 = parseLong(paramXmlPullParser, "duration", l1);
      if (paramSegmentTemplate == null) {
        break label244;
      }
      i = paramSegmentTemplate.startNumber;
      i = parseInt(paramXmlPullParser, "startNumber", i);
      if (paramSegmentTemplate == null) {
        break label249;
      }
      localObject1 = paramSegmentTemplate.mediaTemplate;
      localUrlTemplate1 = parseUrlTemplate(paramXmlPullParser, "media", (UrlTemplate)localObject1);
      if (paramSegmentTemplate == null) {
        break label255;
      }
      localObject1 = paramSegmentTemplate.initializationTemplate;
      localUrlTemplate2 = parseUrlTemplate(paramXmlPullParser, "initialization", (UrlTemplate)localObject1);
      localObject4 = null;
      localObject3 = null;
      label159:
      do
      {
        paramXmlPullParser.next();
        if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "Initialization")) {
          break;
        }
        localObject1 = parseInitialization(paramXmlPullParser);
        localObject2 = localObject3;
        localObject4 = localObject1;
        localObject3 = localObject2;
      } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "SegmentTemplate"));
      paramXmlPullParser = (XmlPullParser)localObject1;
      localObject3 = localObject2;
      if (paramSegmentTemplate != null)
      {
        if (localObject1 == null) {
          break label293;
        }
        label193:
        if (localObject2 == null) {
          break label302;
        }
        localObject3 = localObject2;
      }
    }
    for (paramXmlPullParser = (XmlPullParser)localObject1;; paramXmlPullParser = (XmlPullParser)localObject1)
    {
      return buildSegmentTemplate(paramXmlPullParser, l2, l3, i, l1, (List)localObject3, localUrlTemplate2, localUrlTemplate1);
      l1 = 1L;
      break;
      label230:
      l1 = 0L;
      break label31;
      label236:
      l1 = -9223372036854775807L;
      break label52;
      label244:
      i = 1;
      break label72;
      label249:
      localObject1 = null;
      break label91;
      label255:
      localObject1 = null;
      break label113;
      localObject1 = localObject4;
      localObject2 = localObject3;
      if (!XmlPullParserUtil.isStartTag(paramXmlPullParser, "SegmentTimeline")) {
        break label159;
      }
      localObject2 = parseSegmentTimeline(paramXmlPullParser);
      localObject1 = localObject4;
      break label159;
      label293:
      localObject1 = paramSegmentTemplate.initialization;
      break label193;
      label302:
      localObject3 = paramSegmentTemplate.segmentTimeline;
    }
  }
  
  protected List<SegmentBase.SegmentTimelineElement> parseSegmentTimeline(XmlPullParser paramXmlPullParser)
    throws XmlPullParserException, IOException
  {
    ArrayList localArrayList = new ArrayList();
    long l1 = 0L;
    do
    {
      paramXmlPullParser.next();
      long l2 = l1;
      if (XmlPullParserUtil.isStartTag(paramXmlPullParser, "S"))
      {
        l1 = parseLong(paramXmlPullParser, "t", l1);
        long l3 = parseLong(paramXmlPullParser, "d", -9223372036854775807L);
        int j = parseInt(paramXmlPullParser, "r", 0);
        int i = 0;
        for (;;)
        {
          l2 = l1;
          if (i >= j + 1) {
            break;
          }
          localArrayList.add(buildSegmentTimelineElement(l1, l3));
          l1 += l3;
          i += 1;
        }
      }
      l1 = l2;
    } while (!XmlPullParserUtil.isEndTag(paramXmlPullParser, "SegmentTimeline"));
    return localArrayList;
  }
  
  protected RangedUri parseSegmentUrl(XmlPullParser paramXmlPullParser)
  {
    return parseRangedUrl(paramXmlPullParser, "media", "mediaRange");
  }
  
  protected UrlTemplate parseUrlTemplate(XmlPullParser paramXmlPullParser, String paramString, UrlTemplate paramUrlTemplate)
  {
    paramXmlPullParser = paramXmlPullParser.getAttributeValue(null, paramString);
    if (paramXmlPullParser != null) {
      paramUrlTemplate = UrlTemplate.compile(paramXmlPullParser);
    }
    return paramUrlTemplate;
  }
  
  protected UtcTimingElement parseUtcTiming(XmlPullParser paramXmlPullParser)
  {
    return buildUtcTimingElement(paramXmlPullParser.getAttributeValue(null, "schemeIdUri"), paramXmlPullParser.getAttributeValue(null, "value"));
  }
  
  protected static final class RepresentationInfo
  {
    public final String baseUrl;
    public final ArrayList<DrmInitData.SchemeData> drmSchemeDatas;
    public final String drmSchemeType;
    public final Format format;
    public final ArrayList<Descriptor> inbandEventStreams;
    public final long revisionId;
    public final SegmentBase segmentBase;
    
    public RepresentationInfo(Format paramFormat, String paramString1, SegmentBase paramSegmentBase, String paramString2, ArrayList<DrmInitData.SchemeData> paramArrayList, ArrayList<Descriptor> paramArrayList1, long paramLong)
    {
      this.format = paramFormat;
      this.baseUrl = paramString1;
      this.segmentBase = paramSegmentBase;
      this.drmSchemeType = paramString2;
      this.drmSchemeDatas = paramArrayList;
      this.inbandEventStreams = paramArrayList1;
      this.revisionId = paramLong;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/manifest/DashManifestParser.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */