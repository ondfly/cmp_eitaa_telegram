package org.telegram.messenger.exoplayer2.mediacodec;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.messenger.exoplayer2.util.Util;

@SuppressLint({"InlinedApi"})
@TargetApi(16)
public final class MediaCodecUtil
{
  private static final SparseIntArray AVC_LEVEL_NUMBER_TO_CONST;
  private static final SparseIntArray AVC_PROFILE_NUMBER_TO_CONST;
  private static final String CODEC_ID_AVC1 = "avc1";
  private static final String CODEC_ID_AVC2 = "avc2";
  private static final String CODEC_ID_HEV1 = "hev1";
  private static final String CODEC_ID_HVC1 = "hvc1";
  private static final String GOOGLE_RAW_DECODER_NAME = "OMX.google.raw.decoder";
  private static final Map<String, Integer> HEVC_CODEC_STRING_TO_PROFILE_LEVEL;
  private static final String MTK_RAW_DECODER_NAME = "OMX.MTK.AUDIO.DECODER.RAW";
  private static final MediaCodecInfo PASSTHROUGH_DECODER_INFO = MediaCodecInfo.newPassthroughInstance("OMX.google.raw.decoder");
  private static final Pattern PROFILE_PATTERN = Pattern.compile("^\\D?(\\d+)$");
  private static final String TAG = "MediaCodecUtil";
  private static final HashMap<CodecKey, List<MediaCodecInfo>> decoderInfosCache = new HashMap();
  private static int maxH264DecodableFrameSize = -1;
  
  static
  {
    AVC_PROFILE_NUMBER_TO_CONST = new SparseIntArray();
    AVC_PROFILE_NUMBER_TO_CONST.put(66, 1);
    AVC_PROFILE_NUMBER_TO_CONST.put(77, 2);
    AVC_PROFILE_NUMBER_TO_CONST.put(88, 4);
    AVC_PROFILE_NUMBER_TO_CONST.put(100, 8);
    AVC_LEVEL_NUMBER_TO_CONST = new SparseIntArray();
    AVC_LEVEL_NUMBER_TO_CONST.put(10, 1);
    AVC_LEVEL_NUMBER_TO_CONST.put(11, 4);
    AVC_LEVEL_NUMBER_TO_CONST.put(12, 8);
    AVC_LEVEL_NUMBER_TO_CONST.put(13, 16);
    AVC_LEVEL_NUMBER_TO_CONST.put(20, 32);
    AVC_LEVEL_NUMBER_TO_CONST.put(21, 64);
    AVC_LEVEL_NUMBER_TO_CONST.put(22, 128);
    AVC_LEVEL_NUMBER_TO_CONST.put(30, 256);
    AVC_LEVEL_NUMBER_TO_CONST.put(31, 512);
    AVC_LEVEL_NUMBER_TO_CONST.put(32, 1024);
    AVC_LEVEL_NUMBER_TO_CONST.put(40, 2048);
    AVC_LEVEL_NUMBER_TO_CONST.put(41, 4096);
    AVC_LEVEL_NUMBER_TO_CONST.put(42, 8192);
    AVC_LEVEL_NUMBER_TO_CONST.put(50, 16384);
    AVC_LEVEL_NUMBER_TO_CONST.put(51, 32768);
    AVC_LEVEL_NUMBER_TO_CONST.put(52, 65536);
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL = new HashMap();
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L30", Integer.valueOf(1));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L60", Integer.valueOf(4));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L63", Integer.valueOf(16));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L90", Integer.valueOf(64));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L93", Integer.valueOf(256));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L120", Integer.valueOf(1024));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L123", Integer.valueOf(4096));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L150", Integer.valueOf(16384));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L153", Integer.valueOf(65536));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L156", Integer.valueOf(262144));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L180", Integer.valueOf(1048576));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L183", Integer.valueOf(4194304));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("L186", Integer.valueOf(16777216));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H30", Integer.valueOf(2));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H60", Integer.valueOf(8));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H63", Integer.valueOf(32));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H90", Integer.valueOf(128));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H93", Integer.valueOf(512));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H120", Integer.valueOf(2048));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H123", Integer.valueOf(8192));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H150", Integer.valueOf(32768));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H153", Integer.valueOf(131072));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H156", Integer.valueOf(524288));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H180", Integer.valueOf(2097152));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H183", Integer.valueOf(8388608));
    HEVC_CODEC_STRING_TO_PROFILE_LEVEL.put("H186", Integer.valueOf(33554432));
  }
  
  private static void applyWorkarounds(List<MediaCodecInfo> paramList)
  {
    int i;
    if ((Util.SDK_INT < 26) && (paramList.size() > 1) && ("OMX.MTK.AUDIO.DECODER.RAW".equals(((MediaCodecInfo)paramList.get(0)).name))) {
      i = 1;
    }
    for (;;)
    {
      if (i < paramList.size())
      {
        MediaCodecInfo localMediaCodecInfo = (MediaCodecInfo)paramList.get(i);
        if ("OMX.google.raw.decoder".equals(localMediaCodecInfo.name))
        {
          paramList.remove(i);
          paramList.add(0, localMediaCodecInfo);
        }
      }
      else
      {
        return;
      }
      i += 1;
    }
  }
  
  private static int avcLevelToMaxFrameSize(int paramInt)
  {
    int i = 25344;
    switch (paramInt)
    {
    default: 
      i = -1;
    case 1: 
    case 2: 
      return i;
    case 8: 
      return 101376;
    case 16: 
      return 101376;
    case 32: 
      return 101376;
    case 64: 
      return 202752;
    case 128: 
      return 414720;
    case 256: 
      return 414720;
    case 512: 
      return 921600;
    case 1024: 
      return 1310720;
    case 2048: 
      return 2097152;
    case 4096: 
      return 2097152;
    case 8192: 
      return 2228224;
    case 16384: 
      return 5652480;
    case 32768: 
      return 9437184;
    }
    return 9437184;
  }
  
  private static boolean codecNeedsDisableAdaptationWorkaround(String paramString)
  {
    return (Util.SDK_INT <= 22) && ((Util.MODEL.equals("ODROID-XU3")) || (Util.MODEL.equals("Nexus 10"))) && (("OMX.Exynos.AVC.Decoder".equals(paramString)) || ("OMX.Exynos.AVC.Decoder.secure".equals(paramString)));
  }
  
  private static Pair<Integer, Integer> getAvcProfileAndLevel(String paramString, String[] paramArrayOfString)
  {
    if (paramArrayOfString.length < 2)
    {
      Log.w("MediaCodecUtil", "Ignoring malformed AVC codec string: " + paramString);
      return null;
    }
    Integer localInteger;
    try
    {
      if (paramArrayOfString[1].length() == 6)
      {
        localInteger = Integer.valueOf(Integer.parseInt(paramArrayOfString[1].substring(0, 2), 16));
        int i = Integer.parseInt(paramArrayOfString[1].substring(4), 16);
        paramString = Integer.valueOf(i);
      }
      for (paramArrayOfString = localInteger;; paramArrayOfString = localInteger)
      {
        localInteger = Integer.valueOf(AVC_PROFILE_NUMBER_TO_CONST.get(paramArrayOfString.intValue()));
        if (localInteger != null) {
          break label215;
        }
        Log.w("MediaCodecUtil", "Unknown AVC profile: " + paramArrayOfString);
        return null;
        if (paramArrayOfString.length < 3) {
          break;
        }
        localInteger = Integer.valueOf(Integer.parseInt(paramArrayOfString[1]));
        paramArrayOfString = Integer.valueOf(Integer.parseInt(paramArrayOfString[2]));
        paramString = paramArrayOfString;
      }
      Log.w("MediaCodecUtil", "Ignoring malformed AVC codec string: " + paramString);
      return null;
    }
    catch (NumberFormatException paramArrayOfString)
    {
      Log.w("MediaCodecUtil", "Ignoring malformed AVC codec string: " + paramString);
      return null;
    }
    label215:
    paramArrayOfString = Integer.valueOf(AVC_LEVEL_NUMBER_TO_CONST.get(paramString.intValue()));
    if (paramArrayOfString == null)
    {
      Log.w("MediaCodecUtil", "Unknown AVC level: " + paramString);
      return null;
    }
    return new Pair(localInteger, paramArrayOfString);
  }
  
  public static Pair<Integer, Integer> getCodecProfileAndLevel(String paramString)
  {
    int i = 0;
    if (paramString == null) {
      return null;
    }
    String[] arrayOfString = paramString.split("\\.");
    String str = arrayOfString[0];
    switch (str.hashCode())
    {
    default: 
      label68:
      i = -1;
    }
    for (;;)
    {
      switch (i)
      {
      default: 
        return null;
      case 0: 
      case 1: 
        return getHevcProfileAndLevel(paramString, arrayOfString);
        if (!str.equals("hev1")) {
          break label68;
        }
        continue;
        if (!str.equals("hvc1")) {
          break label68;
        }
        i = 1;
        continue;
        if (!str.equals("avc1")) {
          break label68;
        }
        i = 2;
        continue;
        if (!str.equals("avc2")) {
          break label68;
        }
        i = 3;
      }
    }
    return getAvcProfileAndLevel(paramString, arrayOfString);
  }
  
  public static MediaCodecInfo getDecoderInfo(String paramString, boolean paramBoolean)
    throws MediaCodecUtil.DecoderQueryException
  {
    paramString = getDecoderInfos(paramString, paramBoolean);
    if (paramString.isEmpty()) {
      return null;
    }
    return (MediaCodecInfo)paramString.get(0);
  }
  
  /* Error */
  public static List<MediaCodecInfo> getDecoderInfos(String paramString, boolean paramBoolean)
    throws MediaCodecUtil.DecoderQueryException
  {
    // Byte code:
    //   0: ldc 2
    //   2: monitorenter
    //   3: new 8	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$CodecKey
    //   6: dup
    //   7: aload_0
    //   8: iload_1
    //   9: invokespecial 328	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$CodecKey:<init>	(Ljava/lang/String;Z)V
    //   12: astore 6
    //   14: getstatic 90	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil:decoderInfosCache	Ljava/util/HashMap;
    //   17: aload 6
    //   19: invokevirtual 331	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   22: checkcast 190	java/util/List
    //   25: astore_2
    //   26: aload_2
    //   27: ifnull +10 -> 37
    //   30: aload_2
    //   31: astore_0
    //   32: ldc 2
    //   34: monitorexit
    //   35: aload_0
    //   36: areturn
    //   37: getstatic 188	org/telegram/messenger/exoplayer2/util/Util:SDK_INT	I
    //   40: bipush 21
    //   42: if_icmplt +216 -> 258
    //   45: new 20	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompatV21
    //   48: dup
    //   49: iload_1
    //   50: invokespecial 334	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompatV21:<init>	(Z)V
    //   53: astore_2
    //   54: aload 6
    //   56: aload_2
    //   57: aload_0
    //   58: invokestatic 338	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil:getDecoderInfosInternal	(Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$CodecKey;Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompat;Ljava/lang/String;)Ljava/util/ArrayList;
    //   61: astore 5
    //   63: aload 5
    //   65: astore_3
    //   66: aload_2
    //   67: astore 4
    //   69: iload_1
    //   70: ifeq +130 -> 200
    //   73: aload 5
    //   75: astore_3
    //   76: aload_2
    //   77: astore 4
    //   79: aload 5
    //   81: invokevirtual 341	java/util/ArrayList:isEmpty	()Z
    //   84: ifeq +116 -> 200
    //   87: aload 5
    //   89: astore_3
    //   90: aload_2
    //   91: astore 4
    //   93: bipush 21
    //   95: getstatic 188	org/telegram/messenger/exoplayer2/util/Util:SDK_INT	I
    //   98: if_icmpgt +102 -> 200
    //   101: aload 5
    //   103: astore_3
    //   104: aload_2
    //   105: astore 4
    //   107: getstatic 188	org/telegram/messenger/exoplayer2/util/Util:SDK_INT	I
    //   110: bipush 23
    //   112: if_icmpgt +88 -> 200
    //   115: new 17	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompatV16
    //   118: dup
    //   119: aconst_null
    //   120: invokespecial 344	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompatV16:<init>	(Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$1;)V
    //   123: astore_2
    //   124: aload 6
    //   126: aload_2
    //   127: aload_0
    //   128: invokestatic 338	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil:getDecoderInfosInternal	(Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$CodecKey;Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompat;Ljava/lang/String;)Ljava/util/ArrayList;
    //   131: astore 5
    //   133: aload 5
    //   135: astore_3
    //   136: aload_2
    //   137: astore 4
    //   139: aload 5
    //   141: invokevirtual 341	java/util/ArrayList:isEmpty	()Z
    //   144: ifne +56 -> 200
    //   147: ldc 58
    //   149: new 245	java/lang/StringBuilder
    //   152: dup
    //   153: invokespecial 246	java/lang/StringBuilder:<init>	()V
    //   156: ldc_w 346
    //   159: invokevirtual 252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   162: aload_0
    //   163: invokevirtual 252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   166: ldc_w 348
    //   169: invokevirtual 252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   172: aload 5
    //   174: iconst_0
    //   175: invokevirtual 349	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   178: checkcast 67	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecInfo
    //   181: getfield 201	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecInfo:name	Ljava/lang/String;
    //   184: invokevirtual 252	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   187: invokevirtual 256	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   190: invokestatic 262	android/util/Log:w	(Ljava/lang/String;Ljava/lang/String;)I
    //   193: pop
    //   194: aload_2
    //   195: astore 4
    //   197: aload 5
    //   199: astore_3
    //   200: ldc_w 351
    //   203: aload_0
    //   204: invokevirtual 207	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   207: ifeq +29 -> 236
    //   210: aload_3
    //   211: new 8	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$CodecKey
    //   214: dup
    //   215: ldc_w 353
    //   218: aload 6
    //   220: getfield 357	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$CodecKey:secure	Z
    //   223: invokespecial 328	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$CodecKey:<init>	(Ljava/lang/String;Z)V
    //   226: aload 4
    //   228: aload_0
    //   229: invokestatic 338	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil:getDecoderInfosInternal	(Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$CodecKey;Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompat;Ljava/lang/String;)Ljava/util/ArrayList;
    //   232: invokevirtual 361	java/util/ArrayList:addAll	(Ljava/util/Collection;)Z
    //   235: pop
    //   236: aload_3
    //   237: invokestatic 363	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil:applyWorkarounds	(Ljava/util/List;)V
    //   240: aload_3
    //   241: invokestatic 369	java/util/Collections:unmodifiableList	(Ljava/util/List;)Ljava/util/List;
    //   244: astore_0
    //   245: getstatic 90	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil:decoderInfosCache	Ljava/util/HashMap;
    //   248: aload 6
    //   250: aload_0
    //   251: invokevirtual 370	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   254: pop
    //   255: goto -223 -> 32
    //   258: new 17	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompatV16
    //   261: dup
    //   262: aconst_null
    //   263: invokespecial 344	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$MediaCodecListCompatV16:<init>	(Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil$1;)V
    //   266: astore_2
    //   267: goto -213 -> 54
    //   270: astore_0
    //   271: ldc 2
    //   273: monitorexit
    //   274: aload_0
    //   275: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	276	0	paramString	String
    //   0	276	1	paramBoolean	boolean
    //   25	242	2	localObject1	Object
    //   65	176	3	localArrayList1	ArrayList
    //   67	160	4	localObject2	Object
    //   61	137	5	localArrayList2	ArrayList
    //   12	237	6	localCodecKey	CodecKey
    // Exception table:
    //   from	to	target	type
    //   3	26	270	finally
    //   37	54	270	finally
    //   54	63	270	finally
    //   79	87	270	finally
    //   93	101	270	finally
    //   107	133	270	finally
    //   139	194	270	finally
    //   200	236	270	finally
    //   236	255	270	finally
    //   258	267	270	finally
  }
  
  private static ArrayList<MediaCodecInfo> getDecoderInfosInternal(CodecKey paramCodecKey, MediaCodecListCompat paramMediaCodecListCompat, String paramString)
    throws MediaCodecUtil.DecoderQueryException
  {
    for (;;)
    {
      int i;
      String str2;
      int j;
      String str3;
      try
      {
        ArrayList localArrayList = new ArrayList();
        String str1 = paramCodecKey.mimeType;
        int k = paramMediaCodecListCompat.getCodecCount();
        boolean bool1 = paramMediaCodecListCompat.secureDecodersExplicit();
        i = 0;
        android.media.MediaCodecInfo localMediaCodecInfo;
        boolean bool2;
        if (i < k)
        {
          localMediaCodecInfo = paramMediaCodecListCompat.getCodecInfoAt(i);
          str2 = localMediaCodecInfo.getName();
          if (!isCodecUsableDecoder(localMediaCodecInfo, str2, bool1, paramString)) {
            break label348;
          }
          String[] arrayOfString = localMediaCodecInfo.getSupportedTypes();
          int m = arrayOfString.length;
          j = 0;
          if (j >= m) {
            break label348;
          }
          str3 = arrayOfString[j];
          bool2 = str3.equalsIgnoreCase(str1);
          if (!bool2) {
            break label355;
          }
        }
        try
        {
          MediaCodecInfo.CodecCapabilities localCodecCapabilities = localMediaCodecInfo.getCapabilitiesForType(str3);
          bool2 = paramMediaCodecListCompat.isSecurePlaybackSupported(str1, localCodecCapabilities);
          boolean bool3 = codecNeedsDisableAdaptationWorkaround(str2);
          if (((bool1) && (paramCodecKey.secure == bool2)) || ((!bool1) && (!paramCodecKey.secure)))
          {
            localArrayList.add(MediaCodecInfo.newInstance(str2, str1, localCodecCapabilities, bool3, false));
            break label355;
          }
          if ((bool1) || (!bool2)) {
            break label355;
          }
          localArrayList.add(MediaCodecInfo.newInstance(str2 + ".secure", str1, localCodecCapabilities, bool3, true));
          return localArrayList;
        }
        catch (Exception localException)
        {
          if (Util.SDK_INT > 23) {
            break label301;
          }
        }
        if (!localArrayList.isEmpty()) {
          Log.e("MediaCodecUtil", "Skipping codec " + str2 + " (failed to query capabilities)");
        }
      }
      catch (Exception paramCodecKey)
      {
        throw new DecoderQueryException(paramCodecKey, null);
      }
      label301:
      Log.e("MediaCodecUtil", "Failed to query codec " + str2 + " (" + str3 + ")");
      throw localException;
      label348:
      i += 1;
      continue;
      label355:
      j += 1;
    }
  }
  
  private static Pair<Integer, Integer> getHevcProfileAndLevel(String paramString, String[] paramArrayOfString)
  {
    if (paramArrayOfString.length < 4)
    {
      Log.w("MediaCodecUtil", "Ignoring malformed HEVC codec string: " + paramString);
      return null;
    }
    Matcher localMatcher = PROFILE_PATTERN.matcher(paramArrayOfString[1]);
    if (!localMatcher.matches())
    {
      Log.w("MediaCodecUtil", "Ignoring malformed HEVC codec string: " + paramString);
      return null;
    }
    paramString = localMatcher.group(1);
    if ("1".equals(paramString)) {}
    for (int i = 1;; i = 2)
    {
      paramString = (Integer)HEVC_CODEC_STRING_TO_PROFILE_LEVEL.get(paramArrayOfString[3]);
      if (paramString != null) {
        break label191;
      }
      Log.w("MediaCodecUtil", "Unknown HEVC level string: " + localMatcher.group(1));
      return null;
      if (!"2".equals(paramString)) {
        break;
      }
    }
    Log.w("MediaCodecUtil", "Unknown HEVC profile string: " + paramString);
    return null;
    label191:
    return new Pair(Integer.valueOf(i), paramString);
  }
  
  public static MediaCodecInfo getPassthroughDecoderInfo()
  {
    return PASSTHROUGH_DECODER_INFO;
  }
  
  private static boolean isCodecUsableDecoder(android.media.MediaCodecInfo paramMediaCodecInfo, String paramString1, boolean paramBoolean, String paramString2)
  {
    if ((paramMediaCodecInfo.isEncoder()) || ((!paramBoolean) && (paramString1.endsWith(".secure")))) {}
    while (((Util.SDK_INT < 21) && (("CIPAACDecoder".equals(paramString1)) || ("CIPMP3Decoder".equals(paramString1)) || ("CIPVorbisDecoder".equals(paramString1)) || ("CIPAMRNBDecoder".equals(paramString1)) || ("AACDecoder".equals(paramString1)) || ("MP3Decoder".equals(paramString1)))) || ((Util.SDK_INT < 18) && ("OMX.SEC.MP3.Decoder".equals(paramString1))) || ((Util.SDK_INT < 18) && ("OMX.MTK.AUDIO.DECODER.AAC".equals(paramString1)) && (("a70".equals(Util.DEVICE)) || (("Xiaomi".equals(Util.MANUFACTURER)) && (Util.DEVICE.startsWith("HM"))))) || ((Util.SDK_INT == 16) && ("OMX.qcom.audio.decoder.mp3".equals(paramString1)) && (("dlxu".equals(Util.DEVICE)) || ("protou".equals(Util.DEVICE)) || ("ville".equals(Util.DEVICE)) || ("villeplus".equals(Util.DEVICE)) || ("villec2".equals(Util.DEVICE)) || (Util.DEVICE.startsWith("gee")) || ("C6602".equals(Util.DEVICE)) || ("C6603".equals(Util.DEVICE)) || ("C6606".equals(Util.DEVICE)) || ("C6616".equals(Util.DEVICE)) || ("L36h".equals(Util.DEVICE)) || ("SO-02E".equals(Util.DEVICE)))) || ((Util.SDK_INT == 16) && ("OMX.qcom.audio.decoder.aac".equals(paramString1)) && (("C1504".equals(Util.DEVICE)) || ("C1505".equals(Util.DEVICE)) || ("C1604".equals(Util.DEVICE)) || ("C1605".equals(Util.DEVICE)))) || ((Util.SDK_INT < 24) && (("OMX.SEC.aac.dec".equals(paramString1)) || ("OMX.Exynos.AAC.Decoder".equals(paramString1))) && (Util.MANUFACTURER.equals("samsung")) && ((Util.DEVICE.startsWith("zeroflte")) || (Util.DEVICE.startsWith("zerolte")) || (Util.DEVICE.startsWith("zenlte")) || (Util.DEVICE.equals("SC-05G")) || (Util.DEVICE.equals("marinelteatt")) || (Util.DEVICE.equals("404SC")) || (Util.DEVICE.equals("SC-04G")) || (Util.DEVICE.equals("SCV31")))) || ((Util.SDK_INT <= 19) && ("OMX.SEC.vp8.dec".equals(paramString1)) && ("samsung".equals(Util.MANUFACTURER)) && ((Util.DEVICE.startsWith("d2")) || (Util.DEVICE.startsWith("serrano")) || (Util.DEVICE.startsWith("jflte")) || (Util.DEVICE.startsWith("santos")) || (Util.DEVICE.startsWith("t0")))) || ((Util.SDK_INT <= 19) && (Util.DEVICE.startsWith("jflte")) && ("OMX.qcom.video.decoder.vp8".equals(paramString1))) || (("audio/eac3-joc".equals(paramString2)) && ("OMX.MTK.AUDIO.DECODER.DSPAC3".equals(paramString1)))) {
      return false;
    }
    return true;
  }
  
  public static int maxH264DecodableFrameSize()
    throws MediaCodecUtil.DecoderQueryException
  {
    int j = 0;
    int i;
    if (maxH264DecodableFrameSize == -1)
    {
      i = 0;
      int k = 0;
      Object localObject = getDecoderInfo("video/avc", false);
      if (localObject != null)
      {
        localObject = ((MediaCodecInfo)localObject).getProfileLevels();
        int m = localObject.length;
        i = k;
        while (j < m)
        {
          i = Math.max(avcLevelToMaxFrameSize(localObject[j].level), i);
          j += 1;
        }
        if (Util.SDK_INT < 21) {
          break label93;
        }
      }
    }
    label93:
    for (j = 345600;; j = 172800)
    {
      i = Math.max(i, j);
      maxH264DecodableFrameSize = i;
      return maxH264DecodableFrameSize;
    }
  }
  
  public static void warmDecoderInfoCache(String paramString, boolean paramBoolean)
  {
    try
    {
      getDecoderInfos(paramString, paramBoolean);
      return;
    }
    catch (DecoderQueryException paramString)
    {
      Log.e("MediaCodecUtil", "Codec warming failed", paramString);
    }
  }
  
  private static final class CodecKey
  {
    public final String mimeType;
    public final boolean secure;
    
    public CodecKey(String paramString, boolean paramBoolean)
    {
      this.mimeType = paramString;
      this.secure = paramBoolean;
    }
    
    public boolean equals(Object paramObject)
    {
      if (this == paramObject) {}
      do
      {
        return true;
        if ((paramObject == null) || (paramObject.getClass() != CodecKey.class)) {
          return false;
        }
        paramObject = (CodecKey)paramObject;
      } while ((TextUtils.equals(this.mimeType, ((CodecKey)paramObject).mimeType)) && (this.secure == ((CodecKey)paramObject).secure));
      return false;
    }
    
    public int hashCode()
    {
      int i;
      if (this.mimeType == null)
      {
        i = 0;
        if (!this.secure) {
          break label41;
        }
      }
      label41:
      for (int j = 1231;; j = 1237)
      {
        return (i + 31) * 31 + j;
        i = this.mimeType.hashCode();
        break;
      }
    }
  }
  
  public static class DecoderQueryException
    extends Exception
  {
    private DecoderQueryException(Throwable paramThrowable)
    {
      super(paramThrowable);
    }
  }
  
  private static abstract interface MediaCodecListCompat
  {
    public abstract int getCodecCount();
    
    public abstract android.media.MediaCodecInfo getCodecInfoAt(int paramInt);
    
    public abstract boolean isSecurePlaybackSupported(String paramString, MediaCodecInfo.CodecCapabilities paramCodecCapabilities);
    
    public abstract boolean secureDecodersExplicit();
  }
  
  private static final class MediaCodecListCompatV16
    implements MediaCodecUtil.MediaCodecListCompat
  {
    public int getCodecCount()
    {
      return MediaCodecList.getCodecCount();
    }
    
    public android.media.MediaCodecInfo getCodecInfoAt(int paramInt)
    {
      return MediaCodecList.getCodecInfoAt(paramInt);
    }
    
    public boolean isSecurePlaybackSupported(String paramString, MediaCodecInfo.CodecCapabilities paramCodecCapabilities)
    {
      return "video/avc".equals(paramString);
    }
    
    public boolean secureDecodersExplicit()
    {
      return false;
    }
  }
  
  @TargetApi(21)
  private static final class MediaCodecListCompatV21
    implements MediaCodecUtil.MediaCodecListCompat
  {
    private final int codecKind;
    private android.media.MediaCodecInfo[] mediaCodecInfos;
    
    public MediaCodecListCompatV21(boolean paramBoolean)
    {
      if (paramBoolean) {}
      for (int i = 1;; i = 0)
      {
        this.codecKind = i;
        return;
      }
    }
    
    private void ensureMediaCodecInfosInitialized()
    {
      if (this.mediaCodecInfos == null) {
        this.mediaCodecInfos = new MediaCodecList(this.codecKind).getCodecInfos();
      }
    }
    
    public int getCodecCount()
    {
      ensureMediaCodecInfosInitialized();
      return this.mediaCodecInfos.length;
    }
    
    public android.media.MediaCodecInfo getCodecInfoAt(int paramInt)
    {
      ensureMediaCodecInfosInitialized();
      return this.mediaCodecInfos[paramInt];
    }
    
    public boolean isSecurePlaybackSupported(String paramString, MediaCodecInfo.CodecCapabilities paramCodecCapabilities)
    {
      return paramCodecCapabilities.isFeatureSupported("secure-playback");
    }
    
    public boolean secureDecodersExplicit()
    {
      return true;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/mediacodec/MediaCodecUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */