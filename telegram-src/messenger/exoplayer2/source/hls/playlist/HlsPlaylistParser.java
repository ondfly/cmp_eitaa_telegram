package org.telegram.messenger.exoplayer2.source.hls.playlist;

import android.net.Uri;
import android.util.Base64;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.messenger.exoplayer2.C;
import org.telegram.messenger.exoplayer2.Format;
import org.telegram.messenger.exoplayer2.ParserException;
import org.telegram.messenger.exoplayer2.drm.DrmInitData;
import org.telegram.messenger.exoplayer2.drm.DrmInitData.SchemeData;
import org.telegram.messenger.exoplayer2.source.UnrecognizedInputFormatException;
import org.telegram.messenger.exoplayer2.upstream.ParsingLoadable.Parser;
import org.telegram.messenger.exoplayer2.util.MimeTypes;
import org.telegram.messenger.exoplayer2.util.Util;

public final class HlsPlaylistParser
  implements ParsingLoadable.Parser<HlsPlaylist>
{
  private static final String ATTR_CLOSED_CAPTIONS_NONE = "CLOSED-CAPTIONS=NONE";
  private static final String BOOLEAN_FALSE = "NO";
  private static final String BOOLEAN_TRUE = "YES";
  private static final String KEYFORMAT_IDENTITY = "identity";
  private static final String KEYFORMAT_WIDEVINE_PSSH_BINARY = "urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed";
  private static final String KEYFORMAT_WIDEVINE_PSSH_JSON = "com.widevine";
  private static final String METHOD_AES_128 = "AES-128";
  private static final String METHOD_NONE = "NONE";
  private static final String METHOD_SAMPLE_AES = "SAMPLE-AES";
  private static final String METHOD_SAMPLE_AES_CENC = "SAMPLE-AES-CENC";
  private static final String METHOD_SAMPLE_AES_CTR = "SAMPLE-AES-CTR";
  private static final String PLAYLIST_HEADER = "#EXTM3U";
  private static final Pattern REGEX_ATTR_BYTERANGE;
  private static final Pattern REGEX_AUDIO;
  private static final Pattern REGEX_AUTOSELECT = compileBooleanAttrPattern("AUTOSELECT");
  private static final Pattern REGEX_AVERAGE_BANDWIDTH = Pattern.compile("AVERAGE-BANDWIDTH=(\\d+)\\b");
  private static final Pattern REGEX_BANDWIDTH;
  private static final Pattern REGEX_BYTERANGE;
  private static final Pattern REGEX_CODECS;
  private static final Pattern REGEX_DEFAULT = compileBooleanAttrPattern("DEFAULT");
  private static final Pattern REGEX_FORCED = compileBooleanAttrPattern("FORCED");
  private static final Pattern REGEX_FRAME_RATE;
  private static final Pattern REGEX_GROUP_ID;
  private static final Pattern REGEX_INSTREAM_ID;
  private static final Pattern REGEX_IV;
  private static final Pattern REGEX_KEYFORMAT;
  private static final Pattern REGEX_LANGUAGE;
  private static final Pattern REGEX_MEDIA_DURATION;
  private static final Pattern REGEX_MEDIA_SEQUENCE;
  private static final Pattern REGEX_METHOD;
  private static final Pattern REGEX_NAME;
  private static final Pattern REGEX_PLAYLIST_TYPE;
  private static final Pattern REGEX_RESOLUTION;
  private static final Pattern REGEX_TARGET_DURATION;
  private static final Pattern REGEX_TIME_OFFSET;
  private static final Pattern REGEX_TYPE;
  private static final Pattern REGEX_URI;
  private static final Pattern REGEX_VERSION;
  private static final String TAG_BYTERANGE = "#EXT-X-BYTERANGE";
  private static final String TAG_DISCONTINUITY = "#EXT-X-DISCONTINUITY";
  private static final String TAG_DISCONTINUITY_SEQUENCE = "#EXT-X-DISCONTINUITY-SEQUENCE";
  private static final String TAG_ENDLIST = "#EXT-X-ENDLIST";
  private static final String TAG_INDEPENDENT_SEGMENTS = "#EXT-X-INDEPENDENT-SEGMENTS";
  private static final String TAG_INIT_SEGMENT = "#EXT-X-MAP";
  private static final String TAG_KEY = "#EXT-X-KEY";
  private static final String TAG_MEDIA = "#EXT-X-MEDIA";
  private static final String TAG_MEDIA_DURATION = "#EXTINF";
  private static final String TAG_MEDIA_SEQUENCE = "#EXT-X-MEDIA-SEQUENCE";
  private static final String TAG_PLAYLIST_TYPE = "#EXT-X-PLAYLIST-TYPE";
  private static final String TAG_PREFIX = "#EXT";
  private static final String TAG_PROGRAM_DATE_TIME = "#EXT-X-PROGRAM-DATE-TIME";
  private static final String TAG_START = "#EXT-X-START";
  private static final String TAG_STREAM_INF = "#EXT-X-STREAM-INF";
  private static final String TAG_TARGET_DURATION = "#EXT-X-TARGETDURATION";
  private static final String TAG_VERSION = "#EXT-X-VERSION";
  private static final String TYPE_AUDIO = "AUDIO";
  private static final String TYPE_CLOSED_CAPTIONS = "CLOSED-CAPTIONS";
  private static final String TYPE_SUBTITLES = "SUBTITLES";
  private static final String TYPE_VIDEO = "VIDEO";
  
  static
  {
    REGEX_AUDIO = Pattern.compile("AUDIO=\"(.+?)\"");
    REGEX_BANDWIDTH = Pattern.compile("[^-]BANDWIDTH=(\\d+)\\b");
    REGEX_CODECS = Pattern.compile("CODECS=\"(.+?)\"");
    REGEX_RESOLUTION = Pattern.compile("RESOLUTION=(\\d+x\\d+)");
    REGEX_FRAME_RATE = Pattern.compile("FRAME-RATE=([\\d\\.]+)\\b");
    REGEX_TARGET_DURATION = Pattern.compile("#EXT-X-TARGETDURATION:(\\d+)\\b");
    REGEX_VERSION = Pattern.compile("#EXT-X-VERSION:(\\d+)\\b");
    REGEX_PLAYLIST_TYPE = Pattern.compile("#EXT-X-PLAYLIST-TYPE:(.+)\\b");
    REGEX_MEDIA_SEQUENCE = Pattern.compile("#EXT-X-MEDIA-SEQUENCE:(\\d+)\\b");
    REGEX_MEDIA_DURATION = Pattern.compile("#EXTINF:([\\d\\.]+)\\b");
    REGEX_TIME_OFFSET = Pattern.compile("TIME-OFFSET=(-?[\\d\\.]+)\\b");
    REGEX_BYTERANGE = Pattern.compile("#EXT-X-BYTERANGE:(\\d+(?:@\\d+)?)\\b");
    REGEX_ATTR_BYTERANGE = Pattern.compile("BYTERANGE=\"(\\d+(?:@\\d+)?)\\b\"");
    REGEX_METHOD = Pattern.compile("METHOD=(NONE|AES-128|SAMPLE-AES|SAMPLE-AES-CENC|SAMPLE-AES-CTR)");
    REGEX_KEYFORMAT = Pattern.compile("KEYFORMAT=\"(.+?)\"");
    REGEX_URI = Pattern.compile("URI=\"(.+?)\"");
    REGEX_IV = Pattern.compile("IV=([^,.*]+)");
    REGEX_TYPE = Pattern.compile("TYPE=(AUDIO|VIDEO|SUBTITLES|CLOSED-CAPTIONS)");
    REGEX_LANGUAGE = Pattern.compile("LANGUAGE=\"(.+?)\"");
    REGEX_NAME = Pattern.compile("NAME=\"(.+?)\"");
    REGEX_GROUP_ID = Pattern.compile("GROUP-ID=\"(.+?)\"");
    REGEX_INSTREAM_ID = Pattern.compile("INSTREAM-ID=\"((?:CC|SERVICE)\\d+)\"");
  }
  
  private static boolean checkPlaylistHeader(BufferedReader paramBufferedReader)
    throws IOException
  {
    int j = paramBufferedReader.read();
    int i = j;
    if (j == 239)
    {
      if ((paramBufferedReader.read() != 187) || (paramBufferedReader.read() != 191)) {
        return false;
      }
      i = paramBufferedReader.read();
    }
    j = skipIgnorableWhitespace(paramBufferedReader, true, i);
    int k = "#EXTM3U".length();
    i = 0;
    for (;;)
    {
      if (i >= k) {
        break label83;
      }
      if (j != "#EXTM3U".charAt(i)) {
        break;
      }
      j = paramBufferedReader.read();
      i += 1;
    }
    label83:
    return Util.isLinebreak(skipIgnorableWhitespace(paramBufferedReader, false, j));
  }
  
  private static Pattern compileBooleanAttrPattern(String paramString)
  {
    return Pattern.compile(paramString + "=(" + "NO" + "|" + "YES" + ")");
  }
  
  private static boolean parseBooleanAttribute(String paramString, Pattern paramPattern, boolean paramBoolean)
  {
    paramString = paramPattern.matcher(paramString);
    if (paramString.find()) {
      paramBoolean = paramString.group(1).equals("YES");
    }
    return paramBoolean;
  }
  
  private static double parseDoubleAttr(String paramString, Pattern paramPattern)
    throws ParserException
  {
    return Double.parseDouble(parseStringAttr(paramString, paramPattern));
  }
  
  private static int parseIntAttr(String paramString, Pattern paramPattern)
    throws ParserException
  {
    return Integer.parseInt(parseStringAttr(paramString, paramPattern));
  }
  
  private static HlsMasterPlaylist parseMasterPlaylist(LineIterator paramLineIterator, String paramString)
    throws IOException
  {
    Object localObject3 = new HashSet();
    HashMap localHashMap = new HashMap();
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    ArrayList localArrayList3 = new ArrayList();
    ArrayList localArrayList5 = new ArrayList();
    ArrayList localArrayList4 = new ArrayList();
    Object localObject2 = null;
    Object localObject1 = null;
    int k = 0;
    String str2;
    int m;
    String str1;
    Object localObject4;
    int i;
    while (paramLineIterator.hasNext())
    {
      str2 = paramLineIterator.next();
      if (str2.startsWith("#EXT")) {
        localArrayList4.add(str2);
      }
      if (str2.startsWith("#EXT-X-MEDIA"))
      {
        localArrayList5.add(str2);
      }
      else if (str2.startsWith("#EXT-X-STREAM-INF"))
      {
        int n = k | str2.contains("CLOSED-CAPTIONS=NONE");
        m = parseIntAttr(str2, REGEX_BANDWIDTH);
        str1 = parseOptionalStringAttr(str2, REGEX_AVERAGE_BANDWIDTH);
        if (str1 != null) {
          m = Integer.parseInt(str1);
        }
        str1 = parseOptionalStringAttr(str2, REGEX_CODECS);
        localObject4 = parseOptionalStringAttr(str2, REGEX_RESOLUTION);
        if (localObject4 != null)
        {
          localObject4 = ((String)localObject4).split("x");
          i = Integer.parseInt(localObject4[0]);
          k = Integer.parseInt(localObject4[1]);
          if (i > 0)
          {
            j = k;
            if (k > 0) {}
          }
          else
          {
            i = -1;
          }
        }
        for (j = -1;; j = -1)
        {
          float f = -1.0F;
          localObject4 = parseOptionalStringAttr(str2, REGEX_FRAME_RATE);
          if (localObject4 != null) {
            f = Float.parseFloat((String)localObject4);
          }
          str2 = parseOptionalStringAttr(str2, REGEX_AUDIO);
          if ((str2 != null) && (str1 != null)) {
            localHashMap.put(str2, Util.getCodecsOfType(str1, 1));
          }
          str2 = paramLineIterator.next();
          k = n;
          if (!((HashSet)localObject3).add(str2)) {
            break;
          }
          localArrayList1.add(new HlsMasterPlaylist.HlsUrl(str2, Format.createVideoContainerFormat(Integer.toString(localArrayList1.size()), "application/x-mpegURL", null, str1, m, i, j, f, null, 0)));
          k = n;
          break;
          i = -1;
        }
      }
    }
    int j = 0;
    paramLineIterator = (LineIterator)localObject1;
    if (j < localArrayList5.size())
    {
      localObject1 = (String)localArrayList5.get(j);
      m = parseSelectionFlags((String)localObject1);
      localObject3 = parseOptionalStringAttr((String)localObject1, REGEX_URI);
      str1 = parseStringAttr((String)localObject1, REGEX_NAME);
      str2 = parseOptionalStringAttr((String)localObject1, REGEX_LANGUAGE);
      localObject4 = parseOptionalStringAttr((String)localObject1, REGEX_GROUP_ID);
      String str3 = parseStringAttr((String)localObject1, REGEX_TYPE);
      i = -1;
      switch (str3.hashCode())
      {
      default: 
        label508:
        switch (i)
        {
        }
        break;
      }
      for (;;)
      {
        j += 1;
        break;
        if (!str3.equals("AUDIO")) {
          break label508;
        }
        i = 0;
        break label508;
        if (!str3.equals("SUBTITLES")) {
          break label508;
        }
        i = 1;
        break label508;
        if (!str3.equals("CLOSED-CAPTIONS")) {
          break label508;
        }
        i = 2;
        break label508;
        localObject4 = (String)localHashMap.get(localObject4);
        if (localObject4 != null) {}
        for (localObject1 = MimeTypes.getMediaMimeType((String)localObject4);; localObject1 = null)
        {
          localObject1 = Format.createAudioContainerFormat(str1, "application/x-mpegURL", (String)localObject1, (String)localObject4, -1, -1, -1, null, m, str2);
          if (localObject3 != null) {
            break label654;
          }
          localObject2 = localObject1;
          break;
        }
        label654:
        localArrayList2.add(new HlsMasterPlaylist.HlsUrl((String)localObject3, (Format)localObject1));
        continue;
        localArrayList3.add(new HlsMasterPlaylist.HlsUrl((String)localObject3, Format.createTextContainerFormat(str1, "application/x-mpegURL", "text/vtt", null, -1, m, str2)));
      }
      localObject1 = parseStringAttr((String)localObject1, REGEX_INSTREAM_ID);
      if (((String)localObject1).startsWith("CC")) {
        localObject3 = "application/cea-608";
      }
      for (i = Integer.parseInt(((String)localObject1).substring(2));; i = Integer.parseInt(((String)localObject1).substring(7)))
      {
        localObject1 = paramLineIterator;
        if (paramLineIterator == null) {
          localObject1 = new ArrayList();
        }
        ((List)localObject1).add(Format.createTextContainerFormat(str1, null, (String)localObject3, null, -1, m, str2, i));
        paramLineIterator = (LineIterator)localObject1;
        break;
        localObject3 = "application/cea-708";
      }
    }
    if (k != 0) {
      paramLineIterator = Collections.emptyList();
    }
    return new HlsMasterPlaylist(paramString, localArrayList4, localArrayList1, localArrayList2, localArrayList3, (Format)localObject2, paramLineIterator);
  }
  
  private static HlsMediaPlaylist parseMediaPlaylist(LineIterator paramLineIterator, String paramString)
    throws IOException
  {
    int i = 0;
    long l6 = -9223372036854775807L;
    int m = 0;
    int k = 1;
    long l4 = -9223372036854775807L;
    boolean bool2 = false;
    boolean bool1 = false;
    Object localObject4 = null;
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    long l3 = 0L;
    boolean bool3 = false;
    int n = 0;
    int i1 = 0;
    long l5 = 0L;
    long l7 = 0L;
    long l1 = 0L;
    long l2 = -1L;
    int j = 0;
    Object localObject2 = null;
    Object localObject1 = null;
    DrmInitData localDrmInitData = null;
    while (paramLineIterator.hasNext())
    {
      Object localObject5 = paramLineIterator.next();
      if (((String)localObject5).startsWith("#EXT")) {
        localArrayList2.add(localObject5);
      }
      Object localObject3;
      if (((String)localObject5).startsWith("#EXT-X-PLAYLIST-TYPE"))
      {
        localObject3 = parseStringAttr((String)localObject5, REGEX_PLAYLIST_TYPE);
        if ("VOD".equals(localObject3)) {
          i = 1;
        } else if ("EVENT".equals(localObject3)) {
          i = 2;
        }
      }
      else if (((String)localObject5).startsWith("#EXT-X-START"))
      {
        l6 = (parseDoubleAttr((String)localObject5, REGEX_TIME_OFFSET) * 1000000.0D);
      }
      else
      {
        long l8;
        if (((String)localObject5).startsWith("#EXT-X-MAP"))
        {
          localObject3 = parseStringAttr((String)localObject5, REGEX_URI);
          localObject4 = parseOptionalStringAttr((String)localObject5, REGEX_ATTR_BYTERANGE);
          l8 = l1;
          if (localObject4 != null)
          {
            localObject4 = ((String)localObject4).split("@");
            long l9 = Long.parseLong(localObject4[0]);
            l8 = l1;
            l2 = l9;
            if (localObject4.length > 1)
            {
              l8 = Long.parseLong(localObject4[1]);
              l2 = l9;
            }
          }
          localObject4 = new HlsMediaPlaylist.Segment((String)localObject3, l8, l2);
          l1 = 0L;
          l2 = -1L;
        }
        else if (((String)localObject5).startsWith("#EXT-X-TARGETDURATION"))
        {
          l4 = parseIntAttr((String)localObject5, REGEX_TARGET_DURATION) * 1000000L;
        }
        else if (((String)localObject5).startsWith("#EXT-X-MEDIA-SEQUENCE"))
        {
          m = parseIntAttr((String)localObject5, REGEX_MEDIA_SEQUENCE);
          j = m;
        }
        else if (((String)localObject5).startsWith("#EXT-X-VERSION"))
        {
          k = parseIntAttr((String)localObject5, REGEX_VERSION);
        }
        else if (((String)localObject5).startsWith("#EXTINF"))
        {
          l3 = (parseDoubleAttr((String)localObject5, REGEX_MEDIA_DURATION) * 1000000.0D);
        }
        else if (((String)localObject5).startsWith("#EXT-X-KEY"))
        {
          String str2 = parseOptionalStringAttr((String)localObject5, REGEX_METHOD);
          String str3 = parseOptionalStringAttr((String)localObject5, REGEX_KEYFORMAT);
          localObject3 = null;
          localObject1 = null;
          localObject2 = localObject3;
          if (!"NONE".equals(str2))
          {
            String str1 = parseOptionalStringAttr((String)localObject5, REGEX_IV);
            if (("identity".equals(str3)) || (str3 == null))
            {
              localObject2 = localObject3;
              localObject1 = str1;
              if ("AES-128".equals(str2))
              {
                localObject2 = parseStringAttr((String)localObject5, REGEX_URI);
                localObject1 = str1;
              }
            }
            else
            {
              localObject2 = localObject3;
              localObject1 = str1;
              if (str2 != null)
              {
                localObject5 = parseWidevineSchemeData((String)localObject5, str3);
                localObject2 = localObject3;
                localObject1 = str1;
                if (localObject5 != null)
                {
                  if (("SAMPLE-AES-CENC".equals(str2)) || ("SAMPLE-AES-CTR".equals(str2))) {}
                  for (localObject1 = "cenc";; localObject1 = "cbcs")
                  {
                    localDrmInitData = new DrmInitData((String)localObject1, new DrmInitData.SchemeData[] { localObject5 });
                    localObject2 = localObject3;
                    localObject1 = str1;
                    break;
                  }
                }
              }
            }
          }
        }
        else if (((String)localObject5).startsWith("#EXT-X-BYTERANGE"))
        {
          localObject3 = parseStringAttr((String)localObject5, REGEX_BYTERANGE).split("@");
          l8 = Long.parseLong(localObject3[0]);
          l2 = l8;
          if (localObject3.length > 1)
          {
            l1 = Long.parseLong(localObject3[1]);
            l2 = l8;
          }
        }
        else if (((String)localObject5).startsWith("#EXT-X-DISCONTINUITY-SEQUENCE"))
        {
          bool3 = true;
          n = Integer.parseInt(((String)localObject5).substring(((String)localObject5).indexOf(':') + 1));
        }
        else if (((String)localObject5).equals("#EXT-X-DISCONTINUITY"))
        {
          i1 += 1;
        }
        else if (((String)localObject5).startsWith("#EXT-X-PROGRAM-DATE-TIME"))
        {
          if (l5 == 0L) {
            l5 = C.msToUs(Util.parseXsDateTime(((String)localObject5).substring(((String)localObject5).indexOf(':') + 1))) - l7;
          }
        }
        else
        {
          if (!((String)localObject5).startsWith("#"))
          {
            if (localObject2 == null) {
              localObject3 = null;
            }
            for (;;)
            {
              j += 1;
              if (l2 == -1L) {
                l1 = 0L;
              }
              localArrayList1.add(new HlsMediaPlaylist.Segment((String)localObject5, l3, i1, l7, (String)localObject2, (String)localObject3, l1, l2));
              l7 += l3;
              l8 = 0L;
              l3 = l1;
              if (l2 != -1L) {
                l3 = l1 + l2;
              }
              l2 = -1L;
              l1 = l3;
              l3 = l8;
              break;
              if (localObject1 != null) {
                localObject3 = localObject1;
              } else {
                localObject3 = Integer.toHexString(j);
              }
            }
          }
          if (((String)localObject5).equals("#EXT-X-INDEPENDENT-SEGMENTS")) {
            bool2 = true;
          } else if (((String)localObject5).equals("#EXT-X-ENDLIST")) {
            bool1 = true;
          }
        }
      }
    }
    if (l5 != 0L) {}
    for (boolean bool4 = true;; bool4 = false) {
      return new HlsMediaPlaylist(i, paramString, localArrayList2, l6, l5, bool3, n, m, k, l4, bool2, bool1, bool4, localDrmInitData, (HlsMediaPlaylist.Segment)localObject4, localArrayList1);
    }
  }
  
  private static String parseOptionalStringAttr(String paramString, Pattern paramPattern)
  {
    paramString = paramPattern.matcher(paramString);
    if (paramString.find()) {
      return paramString.group(1);
    }
    return null;
  }
  
  private static int parseSelectionFlags(String paramString)
  {
    int k = 0;
    int i;
    if (parseBooleanAttribute(paramString, REGEX_DEFAULT, false))
    {
      i = 1;
      if (!parseBooleanAttribute(paramString, REGEX_FORCED, false)) {
        break label52;
      }
    }
    label52:
    for (int j = 2;; j = 0)
    {
      if (parseBooleanAttribute(paramString, REGEX_AUTOSELECT, false)) {
        k = 4;
      }
      return i | j | k;
      i = 0;
      break;
    }
  }
  
  private static String parseStringAttr(String paramString, Pattern paramPattern)
    throws ParserException
  {
    Matcher localMatcher = paramPattern.matcher(paramString);
    if ((localMatcher.find()) && (localMatcher.groupCount() == 1)) {
      return localMatcher.group(1);
    }
    throw new ParserException("Couldn't match " + paramPattern.pattern() + " in " + paramString);
  }
  
  private static DrmInitData.SchemeData parseWidevineSchemeData(String paramString1, String paramString2)
    throws ParserException
  {
    if ("urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed".equals(paramString2))
    {
      paramString1 = parseStringAttr(paramString1, REGEX_URI);
      return new DrmInitData.SchemeData(C.WIDEVINE_UUID, "video/mp4", Base64.decode(paramString1.substring(paramString1.indexOf(',')), 0));
    }
    if ("com.widevine".equals(paramString2)) {
      try
      {
        paramString1 = new DrmInitData.SchemeData(C.WIDEVINE_UUID, "hls", paramString1.getBytes("UTF-8"));
        return paramString1;
      }
      catch (UnsupportedEncodingException paramString1)
      {
        throw new ParserException(paramString1);
      }
    }
    return null;
  }
  
  private static int skipIgnorableWhitespace(BufferedReader paramBufferedReader, boolean paramBoolean, int paramInt)
    throws IOException
  {
    while ((paramInt != -1) && (Character.isWhitespace(paramInt)) && ((paramBoolean) || (!Util.isLinebreak(paramInt)))) {
      paramInt = paramBufferedReader.read();
    }
    return paramInt;
  }
  
  public HlsPlaylist parse(Uri paramUri, InputStream paramInputStream)
    throws IOException
  {
    paramInputStream = new BufferedReader(new InputStreamReader(paramInputStream));
    ArrayDeque localArrayDeque = new ArrayDeque();
    try
    {
      if (!checkPlaylistHeader(paramInputStream)) {
        throw new UnrecognizedInputFormatException("Input does not start with the #EXTM3U header.", paramUri);
      }
    }
    finally
    {
      Util.closeQuietly(paramInputStream);
    }
    for (;;)
    {
      String str = paramInputStream.readLine();
      if (str == null) {
        break;
      }
      str = str.trim();
      if (!str.isEmpty())
      {
        if (str.startsWith("#EXT-X-STREAM-INF"))
        {
          localArrayDeque.add(str);
          paramUri = parseMasterPlaylist(new LineIterator(localArrayDeque, paramInputStream), paramUri.toString());
          Util.closeQuietly(paramInputStream);
          return paramUri;
        }
        if ((str.startsWith("#EXT-X-TARGETDURATION")) || (str.startsWith("#EXT-X-MEDIA-SEQUENCE")) || (str.startsWith("#EXTINF")) || (str.startsWith("#EXT-X-KEY")) || (str.startsWith("#EXT-X-BYTERANGE")) || (str.equals("#EXT-X-DISCONTINUITY")) || (str.equals("#EXT-X-DISCONTINUITY-SEQUENCE")) || (str.equals("#EXT-X-ENDLIST")))
        {
          localArrayDeque.add(str);
          paramUri = parseMediaPlaylist(new LineIterator(localArrayDeque, paramInputStream), paramUri.toString());
          Util.closeQuietly(paramInputStream);
          return paramUri;
        }
        localArrayDeque.add(str);
      }
    }
    Util.closeQuietly(paramInputStream);
    throw new ParserException("Failed to parse the playlist, could not identify any tags.");
  }
  
  private static class LineIterator
  {
    private final Queue<String> extraLines;
    private String next;
    private final BufferedReader reader;
    
    public LineIterator(Queue<String> paramQueue, BufferedReader paramBufferedReader)
    {
      this.extraLines = paramQueue;
      this.reader = paramBufferedReader;
    }
    
    public boolean hasNext()
      throws IOException
    {
      if (this.next != null) {
        return true;
      }
      if (!this.extraLines.isEmpty())
      {
        this.next = ((String)this.extraLines.poll());
        return true;
      }
      do
      {
        String str = this.reader.readLine();
        this.next = str;
        if (str == null) {
          break;
        }
        this.next = this.next.trim();
      } while (this.next.isEmpty());
      return true;
      return false;
    }
    
    public String next()
      throws IOException
    {
      String str = null;
      if (hasNext())
      {
        str = this.next;
        this.next = null;
      }
      return str;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/hls/playlist/HlsPlaylistParser.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */