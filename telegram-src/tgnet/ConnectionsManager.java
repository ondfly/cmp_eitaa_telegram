package org.telegram.tgnet;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Base64;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder;
import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.KeepAliveJob;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.StatsController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;

public class ConnectionsManager
{
  public static final int ConnectionStateConnected = 3;
  public static final int ConnectionStateConnecting = 1;
  public static final int ConnectionStateConnectingToProxy = 4;
  public static final int ConnectionStateUpdating = 5;
  public static final int ConnectionStateWaitingForNetwork = 2;
  public static final int ConnectionTypeDownload = 2;
  public static final int ConnectionTypeDownload2 = 65538;
  public static final int ConnectionTypeGeneric = 1;
  public static final int ConnectionTypePush = 8;
  public static final int ConnectionTypeUpload = 4;
  public static final int DEFAULT_DATACENTER_ID = Integer.MAX_VALUE;
  public static final int FileTypeAudio = 50331648;
  public static final int FileTypeFile = 67108864;
  public static final int FileTypePhoto = 16777216;
  public static final int FileTypeVideo = 33554432;
  private static volatile ConnectionsManager[] Instance = new ConnectionsManager[3];
  public static final int RequestFlagCanCompress = 4;
  public static final int RequestFlagEnableUnauthorized = 1;
  public static final int RequestFlagFailOnServerErrors = 2;
  public static final int RequestFlagForceDownload = 32;
  public static final int RequestFlagInvokeAfter = 64;
  public static final int RequestFlagNeedQuickAck = 128;
  public static final int RequestFlagTryDifferentDc = 16;
  public static final int RequestFlagWithoutLogin = 8;
  private static AsyncTask currentTask;
  private static ThreadLocal<HashMap<String, ResolvedDomain>> dnsCache = new ThreadLocal()
  {
    protected HashMap<String, ConnectionsManager.ResolvedDomain> initialValue()
    {
      return new HashMap();
    }
  };
  private static final int dnsConfigVersion = 0;
  private static int lastClassGuid = 1;
  private static long lastDnsRequestTime;
  private boolean appPaused = true;
  private int appResumeCount;
  private int connectionState;
  private int currentAccount;
  private boolean isUpdating;
  private long lastPauseTime = System.currentTimeMillis();
  private AtomicInteger lastRequestToken = new AtomicInteger(1);
  
  public ConnectionsManager(int paramInt)
  {
    this.currentAccount = paramInt;
    this.connectionState = native_getConnectionState(this.currentAccount);
    Object localObject2 = ApplicationLoader.getFilesDirFixed();
    Object localObject1 = localObject2;
    if (paramInt != 0)
    {
      localObject1 = new File((File)localObject2, "account" + paramInt);
      ((File)localObject1).mkdirs();
    }
    String str3 = ((File)localObject1).toString();
    boolean bool = MessagesController.getGlobalNotificationsSettings().getBoolean("pushConnection", true);
    try
    {
      localObject4 = LocaleController.getSystemLocaleStringIso639().toLowerCase();
      str2 = LocaleController.getLocaleStringIso639().toLowerCase();
      localObject3 = Build.MANUFACTURER + Build.MODEL;
      localObject1 = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
      localObject2 = ((PackageInfo)localObject1).versionName + " (" + ((PackageInfo)localObject1).versionCode + ")";
      localObject1 = "SDK " + Build.VERSION.SDK_INT;
      Object localObject5 = localObject4;
      if (((String)localObject4).trim().length() == 0) {
        localObject5 = "en";
      }
      localObject4 = localObject3;
      if (((String)localObject3).trim().length() == 0) {
        localObject4 = "Android unknown";
      }
      localObject3 = localObject2;
      if (((String)localObject2).trim().length() == 0) {
        localObject3 = "App version unknown";
      }
      localObject2 = localObject1;
      if (((String)localObject1).trim().length() == 0) {
        localObject2 = "SDK Unknown";
      }
      UserConfig.getInstance(this.currentAccount).loadConfig();
      init(BuildVars.BUILD_VERSION, 76, BuildVars.APP_ID, (String)localObject4, (String)localObject2, (String)localObject3, str2, (String)localObject5, str3, FileLog.getNetworkLogPath(), UserConfig.getInstance(this.currentAccount).getClientUserId(), bool);
      return;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        Object localObject4 = "en";
        String str2 = "";
        Object localObject3 = "Android unknown";
        localObject2 = "App version unknown";
        String str1 = "SDK " + Build.VERSION.SDK_INT;
      }
    }
  }
  
  private void checkConnection()
  {
    native_setUseIpv6(this.currentAccount, useIpv6Address());
    native_setNetworkAvailable(this.currentAccount, isNetworkOnline(), getCurrentNetworkType());
  }
  
  public static int generateClassGuid()
  {
    int i = lastClassGuid;
    lastClassGuid = i + 1;
    return i;
  }
  
  public static int getCurrentNetworkType()
  {
    if (isConnectedOrConnectingToWiFi()) {
      return 1;
    }
    if (isRoaming()) {
      return 2;
    }
    return 0;
  }
  
  /* Error */
  public static String getHostByName(String paramString, int paramInt)
  {
    // Byte code:
    //   0: getstatic 122	org/telegram/tgnet/ConnectionsManager:dnsCache	Ljava/lang/ThreadLocal;
    //   3: invokevirtual 368	java/lang/ThreadLocal:get	()Ljava/lang/Object;
    //   6: checkcast 370	java/util/HashMap
    //   9: astore 9
    //   11: aload 9
    //   13: aload_0
    //   14: invokevirtual 373	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   17: checkcast 53	org/telegram/tgnet/ConnectionsManager$ResolvedDomain
    //   20: astore_2
    //   21: aload_2
    //   22: ifnull +25 -> 47
    //   25: invokestatic 378	android/os/SystemClock:uptimeMillis	()J
    //   28: aload_2
    //   29: getfield 381	org/telegram/tgnet/ConnectionsManager$ResolvedDomain:ttl	J
    //   32: lsub
    //   33: ldc2_w 382
    //   36: lcmp
    //   37: ifge +10 -> 47
    //   40: aload_2
    //   41: getfield 386	org/telegram/tgnet/ConnectionsManager$ResolvedDomain:address	Ljava/lang/String;
    //   44: astore_0
    //   45: aload_0
    //   46: areturn
    //   47: aconst_null
    //   48: astore 6
    //   50: aconst_null
    //   51: astore 7
    //   53: aconst_null
    //   54: astore 8
    //   56: aconst_null
    //   57: astore_3
    //   58: aload_3
    //   59: astore_2
    //   60: aload 6
    //   62: astore 4
    //   64: aload 8
    //   66: astore 5
    //   68: new 388	java/net/URL
    //   71: dup
    //   72: new 165	java/lang/StringBuilder
    //   75: dup
    //   76: invokespecial 166	java/lang/StringBuilder:<init>	()V
    //   79: ldc_w 390
    //   82: invokevirtual 172	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   85: aload_0
    //   86: invokevirtual 172	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   89: ldc_w 392
    //   92: invokevirtual 172	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   95: invokevirtual 179	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   98: invokespecial 395	java/net/URL:<init>	(Ljava/lang/String;)V
    //   101: invokevirtual 399	java/net/URL:openConnection	()Ljava/net/URLConnection;
    //   104: astore 10
    //   106: aload_3
    //   107: astore_2
    //   108: aload 6
    //   110: astore 4
    //   112: aload 8
    //   114: astore 5
    //   116: aload 10
    //   118: ldc_w 401
    //   121: ldc_w 403
    //   124: invokevirtual 409	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   127: aload_3
    //   128: astore_2
    //   129: aload 6
    //   131: astore 4
    //   133: aload 8
    //   135: astore 5
    //   137: aload 10
    //   139: ldc_w 411
    //   142: ldc_w 413
    //   145: invokevirtual 409	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   148: aload_3
    //   149: astore_2
    //   150: aload 6
    //   152: astore 4
    //   154: aload 8
    //   156: astore 5
    //   158: aload 10
    //   160: sipush 1000
    //   163: invokevirtual 416	java/net/URLConnection:setConnectTimeout	(I)V
    //   166: aload_3
    //   167: astore_2
    //   168: aload 6
    //   170: astore 4
    //   172: aload 8
    //   174: astore 5
    //   176: aload 10
    //   178: sipush 2000
    //   181: invokevirtual 419	java/net/URLConnection:setReadTimeout	(I)V
    //   184: aload_3
    //   185: astore_2
    //   186: aload 6
    //   188: astore 4
    //   190: aload 8
    //   192: astore 5
    //   194: aload 10
    //   196: invokevirtual 422	java/net/URLConnection:connect	()V
    //   199: aload_3
    //   200: astore_2
    //   201: aload 6
    //   203: astore 4
    //   205: aload 8
    //   207: astore 5
    //   209: aload 10
    //   211: invokevirtual 426	java/net/URLConnection:getInputStream	()Ljava/io/InputStream;
    //   214: astore_3
    //   215: aload_3
    //   216: astore_2
    //   217: aload 6
    //   219: astore 4
    //   221: aload_3
    //   222: astore 5
    //   224: new 428	java/io/ByteArrayOutputStream
    //   227: dup
    //   228: invokespecial 429	java/io/ByteArrayOutputStream:<init>	()V
    //   231: astore 6
    //   233: ldc_w 430
    //   236: newarray <illegal type>
    //   238: astore_2
    //   239: aload_3
    //   240: aload_2
    //   241: invokevirtual 436	java/io/InputStream:read	([B)I
    //   244: istore_1
    //   245: iload_1
    //   246: ifle +51 -> 297
    //   249: aload 6
    //   251: aload_2
    //   252: iconst_0
    //   253: iload_1
    //   254: invokevirtual 440	java/io/ByteArrayOutputStream:write	([BII)V
    //   257: goto -18 -> 239
    //   260: astore_2
    //   261: aload 6
    //   263: astore_0
    //   264: aload_2
    //   265: astore 6
    //   267: aload_3
    //   268: astore_2
    //   269: aload_0
    //   270: astore 4
    //   272: aload 6
    //   274: invokestatic 444	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   277: aload_3
    //   278: ifnull +7 -> 285
    //   281: aload_3
    //   282: invokevirtual 447	java/io/InputStream:close	()V
    //   285: aload_0
    //   286: ifnull +7 -> 293
    //   289: aload_0
    //   290: invokevirtual 448	java/io/ByteArrayOutputStream:close	()V
    //   293: ldc_w 307
    //   296: areturn
    //   297: iload_1
    //   298: iconst_m1
    //   299: if_icmpne +3 -> 302
    //   302: new 450	org/json/JSONObject
    //   305: dup
    //   306: new 208	java/lang/String
    //   309: dup
    //   310: aload 6
    //   312: invokevirtual 454	java/io/ByteArrayOutputStream:toByteArray	()[B
    //   315: invokespecial 457	java/lang/String:<init>	([B)V
    //   318: invokespecial 458	org/json/JSONObject:<init>	(Ljava/lang/String;)V
    //   321: ldc_w 460
    //   324: invokevirtual 464	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
    //   327: astore_2
    //   328: aload_2
    //   329: invokevirtual 467	org/json/JSONArray:length	()I
    //   332: ifle +75 -> 407
    //   335: aload_2
    //   336: getstatic 473	org/telegram/messenger/Utilities:random	Ljava/security/SecureRandom;
    //   339: aload_2
    //   340: invokevirtual 467	org/json/JSONArray:length	()I
    //   343: invokevirtual 478	java/security/SecureRandom:nextInt	(I)I
    //   346: invokevirtual 482	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
    //   349: ldc_w 484
    //   352: invokevirtual 488	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   355: astore_2
    //   356: aload 9
    //   358: aload_0
    //   359: new 53	org/telegram/tgnet/ConnectionsManager$ResolvedDomain
    //   362: dup
    //   363: aload_2
    //   364: invokestatic 378	android/os/SystemClock:uptimeMillis	()J
    //   367: invokespecial 491	org/telegram/tgnet/ConnectionsManager$ResolvedDomain:<init>	(Ljava/lang/String;J)V
    //   370: invokevirtual 495	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   373: pop
    //   374: aload_3
    //   375: ifnull +7 -> 382
    //   378: aload_3
    //   379: invokevirtual 447	java/io/InputStream:close	()V
    //   382: aload_2
    //   383: astore_0
    //   384: aload 6
    //   386: ifnull -341 -> 45
    //   389: aload 6
    //   391: invokevirtual 448	java/io/ByteArrayOutputStream:close	()V
    //   394: aload_2
    //   395: areturn
    //   396: astore_0
    //   397: aload_2
    //   398: areturn
    //   399: astore_0
    //   400: aload_0
    //   401: invokestatic 444	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   404: goto -22 -> 382
    //   407: aload_3
    //   408: ifnull +7 -> 415
    //   411: aload_3
    //   412: invokevirtual 447	java/io/InputStream:close	()V
    //   415: aload 6
    //   417: ifnull +8 -> 425
    //   420: aload 6
    //   422: invokevirtual 448	java/io/ByteArrayOutputStream:close	()V
    //   425: goto -132 -> 293
    //   428: astore_0
    //   429: aload_0
    //   430: invokestatic 444	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   433: goto -18 -> 415
    //   436: astore_0
    //   437: goto -144 -> 293
    //   440: astore_2
    //   441: aload_2
    //   442: invokestatic 444	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   445: goto -160 -> 285
    //   448: astore_0
    //   449: aload_2
    //   450: ifnull +7 -> 457
    //   453: aload_2
    //   454: invokevirtual 447	java/io/InputStream:close	()V
    //   457: aload 4
    //   459: ifnull +8 -> 467
    //   462: aload 4
    //   464: invokevirtual 448	java/io/ByteArrayOutputStream:close	()V
    //   467: aload_0
    //   468: athrow
    //   469: astore_2
    //   470: aload_2
    //   471: invokestatic 444	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   474: goto -17 -> 457
    //   477: astore_0
    //   478: goto -185 -> 293
    //   481: astore_2
    //   482: goto -15 -> 467
    //   485: astore_0
    //   486: aload 6
    //   488: astore 4
    //   490: aload_3
    //   491: astore_2
    //   492: goto -43 -> 449
    //   495: astore 6
    //   497: aload 5
    //   499: astore_3
    //   500: aload 7
    //   502: astore_0
    //   503: goto -236 -> 267
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	506	0	paramString	String
    //   0	506	1	paramInt	int
    //   20	232	2	localObject1	Object
    //   260	5	2	localThrowable1	Throwable
    //   268	130	2	localObject2	Object
    //   440	14	2	localThrowable2	Throwable
    //   469	2	2	localThrowable3	Throwable
    //   481	1	2	localException	Exception
    //   491	1	2	localObject3	Object
    //   57	443	3	localObject4	Object
    //   62	427	4	localObject5	Object
    //   66	432	5	localObject6	Object
    //   48	439	6	localObject7	Object
    //   495	1	6	localThrowable4	Throwable
    //   51	450	7	localObject8	Object
    //   54	152	8	localObject9	Object
    //   9	348	9	localHashMap	HashMap
    //   104	106	10	localURLConnection	java.net.URLConnection
    // Exception table:
    //   from	to	target	type
    //   233	239	260	java/lang/Throwable
    //   239	245	260	java/lang/Throwable
    //   249	257	260	java/lang/Throwable
    //   302	374	260	java/lang/Throwable
    //   389	394	396	java/lang/Exception
    //   378	382	399	java/lang/Throwable
    //   411	415	428	java/lang/Throwable
    //   420	425	436	java/lang/Exception
    //   281	285	440	java/lang/Throwable
    //   68	106	448	finally
    //   116	127	448	finally
    //   137	148	448	finally
    //   158	166	448	finally
    //   176	184	448	finally
    //   194	199	448	finally
    //   209	215	448	finally
    //   224	233	448	finally
    //   272	277	448	finally
    //   453	457	469	java/lang/Throwable
    //   289	293	477	java/lang/Exception
    //   462	467	481	java/lang/Exception
    //   233	239	485	finally
    //   239	245	485	finally
    //   249	257	485	finally
    //   302	374	485	finally
    //   68	106	495	java/lang/Throwable
    //   116	127	495	java/lang/Throwable
    //   137	148	495	java/lang/Throwable
    //   158	166	495	java/lang/Throwable
    //   176	184	495	java/lang/Throwable
    //   194	199	495	java/lang/Throwable
    //   209	215	495	java/lang/Throwable
    //   224	233	495	java/lang/Throwable
  }
  
  public static ConnectionsManager getInstance(int paramInt)
  {
    Object localObject1 = Instance[paramInt];
    if (localObject1 == null) {}
    try
    {
      Object localObject3 = Instance[paramInt];
      localObject1 = localObject3;
      if (localObject3 == null)
      {
        localObject3 = Instance;
        localObject1 = new ConnectionsManager(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (ConnectionsManager)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (ConnectionsManager)localObject1;
  }
  
  public static boolean isConnectedOrConnectingToWiFi()
  {
    try
    {
      Object localObject = ((ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity")).getNetworkInfo(1);
      NetworkInfo.State localState = ((NetworkInfo)localObject).getState();
      if (localObject != null) {
        if ((localState != NetworkInfo.State.CONNECTED) && (localState != NetworkInfo.State.CONNECTING))
        {
          localObject = NetworkInfo.State.SUSPENDED;
          if (localState != localObject) {}
        }
        else
        {
          return true;
        }
      }
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return false;
  }
  
  public static boolean isConnectedToWiFi()
  {
    try
    {
      Object localObject = ((ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity")).getNetworkInfo(1);
      if (localObject != null)
      {
        localObject = ((NetworkInfo)localObject).getState();
        NetworkInfo.State localState = NetworkInfo.State.CONNECTED;
        if (localObject == localState) {
          return true;
        }
      }
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return false;
  }
  
  public static boolean isNetworkOnline()
  {
    try
    {
      Object localObject = (ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity");
      NetworkInfo localNetworkInfo = ((ConnectivityManager)localObject).getActiveNetworkInfo();
      if (localNetworkInfo != null)
      {
        if (localNetworkInfo.isConnectedOrConnecting()) {
          break label81;
        }
        if (localNetworkInfo.isAvailable()) {
          return true;
        }
      }
      localNetworkInfo = ((ConnectivityManager)localObject).getNetworkInfo(0);
      if ((localNetworkInfo == null) || (!localNetworkInfo.isConnectedOrConnecting()))
      {
        localObject = ((ConnectivityManager)localObject).getNetworkInfo(1);
        if (localObject != null)
        {
          boolean bool = ((NetworkInfo)localObject).isConnectedOrConnecting();
          if (bool) {}
        }
        else
        {
          return false;
        }
      }
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    label81:
    return true;
  }
  
  public static boolean isRoaming()
  {
    try
    {
      NetworkInfo localNetworkInfo = ((ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity")).getActiveNetworkInfo();
      if (localNetworkInfo != null)
      {
        boolean bool = localNetworkInfo.isRoaming();
        return bool;
      }
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return false;
  }
  
  public static native void native_applyDatacenterAddress(int paramInt1, int paramInt2, String paramString, int paramInt3);
  
  public static native void native_applyDnsConfig(int paramInt, long paramLong);
  
  public static native void native_bindRequestToGuid(int paramInt1, int paramInt2, int paramInt3);
  
  public static native void native_cancelRequest(int paramInt1, int paramInt2, boolean paramBoolean);
  
  public static native void native_cancelRequestsForGuid(int paramInt1, int paramInt2);
  
  public static native void native_cleanUp(int paramInt);
  
  public static native int native_getConnectionState(int paramInt);
  
  public static native int native_getCurrentTime(int paramInt);
  
  public static native long native_getCurrentTimeMillis(int paramInt);
  
  public static native int native_getTimeDifference(int paramInt);
  
  public static native void native_init(int paramInt1, int paramInt2, int paramInt3, int paramInt4, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, int paramInt5, boolean paramBoolean1, boolean paramBoolean2, int paramInt6);
  
  public static native int native_isTestBackend(int paramInt);
  
  public static native void native_pauseNetwork(int paramInt);
  
  public static native void native_resumeNetwork(int paramInt, boolean paramBoolean);
  
  public static native void native_sendRequest(int paramInt1, long paramLong, RequestDelegateInternal paramRequestDelegateInternal, QuickAckDelegate paramQuickAckDelegate, WriteToSocketDelegate paramWriteToSocketDelegate, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean, int paramInt5);
  
  public static native void native_setJava(boolean paramBoolean);
  
  public static native void native_setLangCode(int paramInt, String paramString);
  
  public static native void native_setNetworkAvailable(int paramInt1, boolean paramBoolean, int paramInt2);
  
  public static native void native_setProxySettings(int paramInt1, String paramString1, int paramInt2, String paramString2, String paramString3);
  
  public static native void native_setPushConnectionEnabled(int paramInt, boolean paramBoolean);
  
  public static native void native_setUseIpv6(int paramInt, boolean paramBoolean);
  
  public static native void native_setUserId(int paramInt1, int paramInt2);
  
  public static native void native_switchBackend(int paramInt);
  
  public static native void native_updateDcSettings(int paramInt);
  
  public static void onBytesReceived(int paramInt1, int paramInt2, int paramInt3)
  {
    try
    {
      StatsController.getInstance(paramInt3).incrementReceivedBytesCount(paramInt2, 6, paramInt1);
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public static void onBytesSent(int paramInt1, int paramInt2, int paramInt3)
  {
    try
    {
      StatsController.getInstance(paramInt3).incrementSentBytesCount(paramInt2, 6, paramInt1);
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public static void onConnectionStateChanged(final int paramInt1, int paramInt2)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        ConnectionsManager.access$202(ConnectionsManager.getInstance(this.val$currentAccount), paramInt1);
        NotificationCenter.getInstance(this.val$currentAccount).postNotificationName(NotificationCenter.didUpdatedConnectionState, new Object[0]);
      }
    });
  }
  
  public static void onInternalPushReceived(int paramInt) {}
  
  public static void onLogout(int paramInt)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (UserConfig.getInstance(this.val$currentAccount).getClientUserId() != 0)
        {
          UserConfig.getInstance(this.val$currentAccount).clearConfig();
          MessagesController.getInstance(this.val$currentAccount).performLogout(false);
        }
      }
    });
  }
  
  public static void onRequestNewServerIpAndPort(int paramInt1, final int paramInt2)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if ((ConnectionsManager.currentTask != null) || ((this.val$second == 0) && (Math.abs(ConnectionsManager.lastDnsRequestTime - System.currentTimeMillis()) < 10000L)) || (!ConnectionsManager.isNetworkOnline()))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("don't start task, current task = " + ConnectionsManager.currentTask + " next task = " + this.val$second + " time diff = " + Math.abs(ConnectionsManager.lastDnsRequestTime - System.currentTimeMillis()) + " network = " + ConnectionsManager.isNetworkOnline());
          }
          return;
        }
        ConnectionsManager.access$402(System.currentTimeMillis());
        if (this.val$second == 2)
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("start dns txt task");
          }
          localObject = new ConnectionsManager.DnsTxtLoadTask(paramInt2);
          ((ConnectionsManager.DnsTxtLoadTask)localObject).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
          ConnectionsManager.access$302((AsyncTask)localObject);
          return;
        }
        if (this.val$second == 1)
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("start azure dns task");
          }
          localObject = new ConnectionsManager.AzureLoadTask(paramInt2);
          ((ConnectionsManager.AzureLoadTask)localObject).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
          ConnectionsManager.access$302((AsyncTask)localObject);
          return;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("start firebase task");
        }
        Object localObject = new ConnectionsManager.FirebaseTask(paramInt2);
        ((ConnectionsManager.FirebaseTask)localObject).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
        ConnectionsManager.access$302((AsyncTask)localObject);
      }
    });
  }
  
  public static void onSessionCreated(int paramInt)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance(this.val$currentAccount).getDifference();
      }
    });
  }
  
  public static void onUnparsedMessageReceived(long paramLong, int paramInt)
  {
    try
    {
      final Object localObject = NativeByteBuffer.wrap(paramLong);
      ((NativeByteBuffer)localObject).reused = true;
      localObject = TLClassStore.Instance().TLdeserialize((NativeByteBuffer)localObject, ((NativeByteBuffer)localObject).readInt32(true), true);
      if ((localObject instanceof TLRPC.Updates))
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("java received " + localObject);
        }
        KeepAliveJob.finishJob();
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            MessagesController.getInstance(this.val$currentAccount).processUpdates((TLRPC.Updates)localObject, false);
          }
        });
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public static void onUpdate(int paramInt)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance(this.val$currentAccount).updateTimerProc();
      }
    });
  }
  
  public static void onUpdateConfig(long paramLong, int paramInt)
  {
    try
    {
      final Object localObject = NativeByteBuffer.wrap(paramLong);
      ((NativeByteBuffer)localObject).reused = true;
      localObject = TLRPC.TL_config.TLdeserialize((AbstractSerializedData)localObject, ((NativeByteBuffer)localObject).readInt32(true), true);
      if (localObject != null) {
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            MessagesController.getInstance(this.val$currentAccount).updateConfig(localObject);
          }
        });
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public static void setLangCode(String paramString)
  {
    paramString = paramString.replace('_', '-').toLowerCase();
    int i = 0;
    while (i < 3)
    {
      native_setLangCode(i, paramString);
      i += 1;
    }
  }
  
  @SuppressLint({"NewApi"})
  protected static boolean useIpv6Address()
  {
    if (Build.VERSION.SDK_INT < 19) {
      return false;
    }
    Object localObject;
    int i;
    label111:
    InetAddress localInetAddress;
    if (BuildVars.LOGS_ENABLED) {
      try
      {
        Enumeration localEnumeration1 = NetworkInterface.getNetworkInterfaces();
        while (localEnumeration1.hasMoreElements())
        {
          localObject = (NetworkInterface)localEnumeration1.nextElement();
          if ((((NetworkInterface)localObject).isUp()) && (!((NetworkInterface)localObject).isLoopback()) && (!((NetworkInterface)localObject).getInterfaceAddresses().isEmpty()))
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("valid interface: " + localObject);
            }
            localObject = ((NetworkInterface)localObject).getInterfaceAddresses();
            i = 0;
            if (i < ((List)localObject).size())
            {
              localInetAddress = ((InterfaceAddress)((List)localObject).get(i)).getAddress();
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("address: " + localInetAddress.getHostAddress());
              }
              if ((localInetAddress.isLinkLocalAddress()) || (localInetAddress.isLoopbackAddress()) || (localInetAddress.isMulticastAddress()) || (!BuildVars.LOGS_ENABLED)) {
                break label436;
              }
              FileLog.d("address is good");
            }
          }
        }
      }
      catch (Throwable localThrowable1)
      {
        FileLog.e(localThrowable1);
      }
    }
    for (;;)
    {
      int n;
      int m;
      try
      {
        Enumeration localEnumeration2 = NetworkInterface.getNetworkInterfaces();
        n = 0;
        m = 0;
        if (localEnumeration2.hasMoreElements())
        {
          localObject = (NetworkInterface)localEnumeration2.nextElement();
          if ((!((NetworkInterface)localObject).isUp()) || (((NetworkInterface)localObject).isLoopback())) {
            continue;
          }
          localObject = ((NetworkInterface)localObject).getInterfaceAddresses();
          i = 0;
          k = m;
          j = n;
          n = j;
          m = k;
          if (i >= ((List)localObject).size()) {
            continue;
          }
          localInetAddress = ((InterfaceAddress)((List)localObject).get(i)).getAddress();
          m = j;
          n = k;
          if (localInetAddress.isLinkLocalAddress()) {
            break label443;
          }
          m = j;
          n = k;
          if (localInetAddress.isLoopbackAddress()) {
            break label443;
          }
          if (localInetAddress.isMulticastAddress())
          {
            m = j;
            n = k;
            break label443;
          }
          if ((localInetAddress instanceof Inet6Address))
          {
            n = 1;
            m = j;
            break label443;
          }
          m = j;
          n = k;
          if (!(localInetAddress instanceof Inet4Address)) {
            break label443;
          }
          boolean bool = localInetAddress.getHostAddress().startsWith("192.0.0.");
          m = j;
          n = k;
          if (bool) {
            break label443;
          }
          m = 1;
          n = k;
          break label443;
        }
        if ((n != 0) || (m == 0)) {
          break;
        }
        return true;
      }
      catch (Throwable localThrowable2)
      {
        FileLog.e(localThrowable2);
        return false;
      }
      label436:
      i += 1;
      break label111;
      label443:
      i += 1;
      int j = m;
      int k = n;
    }
  }
  
  public void applyDatacenterAddress(int paramInt1, String paramString, int paramInt2)
  {
    native_applyDatacenterAddress(this.currentAccount, paramInt1, paramString, paramInt2);
  }
  
  public void bindRequestToGuid(int paramInt1, int paramInt2)
  {
    native_bindRequestToGuid(this.currentAccount, paramInt1, paramInt2);
  }
  
  public void cancelRequest(int paramInt, boolean paramBoolean)
  {
    native_cancelRequest(this.currentAccount, paramInt, paramBoolean);
  }
  
  public void cancelRequestsForGuid(int paramInt)
  {
    native_cancelRequestsForGuid(this.currentAccount, paramInt);
  }
  
  public void cleanup()
  {
    native_cleanUp(this.currentAccount);
  }
  
  public int getConnectionState()
  {
    if ((this.connectionState == 3) && (this.isUpdating)) {
      return 5;
    }
    return this.connectionState;
  }
  
  public int getCurrentTime()
  {
    return native_getCurrentTime(this.currentAccount);
  }
  
  public long getCurrentTimeMillis()
  {
    return native_getCurrentTimeMillis(this.currentAccount);
  }
  
  public long getPauseTime()
  {
    return this.lastPauseTime;
  }
  
  public int getTimeDifference()
  {
    return native_getTimeDifference(this.currentAccount);
  }
  
  public void init(int paramInt1, int paramInt2, int paramInt3, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, int paramInt4, boolean paramBoolean)
  {
    SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
    String str1 = localSharedPreferences.getString("proxy_ip", "");
    String str2 = localSharedPreferences.getString("proxy_user", "");
    String str3 = localSharedPreferences.getString("proxy_pass", "");
    int i = localSharedPreferences.getInt("proxy_port", 1080);
    if ((localSharedPreferences.getBoolean("proxy_enabled", false)) && (!TextUtils.isEmpty(str1))) {
      native_setProxySettings(this.currentAccount, str1, i, str2, str3);
    }
    native_init(this.currentAccount, paramInt1, paramInt2, paramInt3, paramString1, paramString2, paramString3, paramString4, paramString5, paramString6, paramString7, paramInt4, paramBoolean, isNetworkOnline(), getCurrentNetworkType());
    checkConnection();
    paramString1 = new BroadcastReceiver()
    {
      public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
      {
        ConnectionsManager.this.checkConnection();
      }
    };
    paramString2 = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    ApplicationLoader.applicationContext.registerReceiver(paramString1, paramString2);
  }
  
  public void resumeNetworkMaybe()
  {
    native_resumeNetwork(this.currentAccount, true);
  }
  
  public int sendRequest(TLObject paramTLObject, RequestDelegate paramRequestDelegate)
  {
    return sendRequest(paramTLObject, paramRequestDelegate, null, 0);
  }
  
  public int sendRequest(TLObject paramTLObject, RequestDelegate paramRequestDelegate, int paramInt)
  {
    return sendRequest(paramTLObject, paramRequestDelegate, null, null, paramInt, Integer.MAX_VALUE, 1, true);
  }
  
  public int sendRequest(TLObject paramTLObject, RequestDelegate paramRequestDelegate, int paramInt1, int paramInt2)
  {
    return sendRequest(paramTLObject, paramRequestDelegate, null, null, paramInt1, Integer.MAX_VALUE, paramInt2, true);
  }
  
  public int sendRequest(TLObject paramTLObject, RequestDelegate paramRequestDelegate, QuickAckDelegate paramQuickAckDelegate, int paramInt)
  {
    return sendRequest(paramTLObject, paramRequestDelegate, paramQuickAckDelegate, null, paramInt, Integer.MAX_VALUE, 1, true);
  }
  
  public int sendRequest(final TLObject paramTLObject, final RequestDelegate paramRequestDelegate, final QuickAckDelegate paramQuickAckDelegate, final WriteToSocketDelegate paramWriteToSocketDelegate, final int paramInt1, final int paramInt2, final int paramInt3, final boolean paramBoolean)
  {
    final int i = this.lastRequestToken.getAndIncrement();
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("send request " + paramTLObject + " with token = " + i);
        }
        try
        {
          NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(paramTLObject.getObjectSize());
          paramTLObject.serializeToStream(localNativeByteBuffer);
          paramTLObject.freeResources();
          ConnectionsManager.native_sendRequest(ConnectionsManager.this.currentAccount, localNativeByteBuffer.address, new RequestDelegateInternal()
          {
            public void run(long paramAnonymous2Long, int paramAnonymous2Int1, String paramAnonymous2String, int paramAnonymous2Int2)
            {
              Object localObject3 = null;
              final Object localObject2 = null;
              if (paramAnonymous2Long != 0L) {}
              try
              {
                paramAnonymous2String = NativeByteBuffer.wrap(paramAnonymous2Long);
                paramAnonymous2String.reused = true;
                localObject1 = ConnectionsManager.2.this.val$object.deserializeResponse(paramAnonymous2String, paramAnonymous2String.readInt32(true), true);
                do
                {
                  if (localObject1 != null) {
                    ((TLObject)localObject1).networkType = paramAnonymous2Int2;
                  }
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("java received " + localObject1 + " error = " + localObject2);
                  }
                  Utilities.stageQueue.postRunnable(new Runnable()
                  {
                    public void run()
                    {
                      ConnectionsManager.2.this.val$onComplete.run(localObject1, localObject2);
                      if (localObject1 != null) {
                        localObject1.freeResources();
                      }
                    }
                  });
                  return;
                  localObject1 = localObject3;
                } while (paramAnonymous2String == null);
                localObject2 = new TLRPC.TL_error();
              }
              catch (Exception paramAnonymous2String)
              {
                try
                {
                  ((TLRPC.TL_error)localObject2).code = paramAnonymous2Int1;
                  ((TLRPC.TL_error)localObject2).text = paramAnonymous2String;
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.e(ConnectionsManager.2.this.val$object + " got error " + ((TLRPC.TL_error)localObject2).code + " " + ((TLRPC.TL_error)localObject2).text);
                  }
                  final Object localObject1 = localObject3;
                }
                catch (Exception paramAnonymous2String)
                {
                  for (;;) {}
                }
                paramAnonymous2String = paramAnonymous2String;
              }
              FileLog.e(paramAnonymous2String);
            }
          }, paramQuickAckDelegate, paramWriteToSocketDelegate, paramInt1, paramInt2, paramInt3, paramBoolean, i);
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
    return i;
  }
  
  public void setAppPaused(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (!paramBoolean2)
    {
      this.appPaused = paramBoolean1;
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("app paused = " + paramBoolean1);
      }
      if (!paramBoolean1) {
        break label127;
      }
      this.appResumeCount -= 1;
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("app resume count " + this.appResumeCount);
      }
      if (this.appResumeCount < 0) {
        this.appResumeCount = 0;
      }
    }
    if (this.appResumeCount == 0)
    {
      if (this.lastPauseTime == 0L) {
        this.lastPauseTime = System.currentTimeMillis();
      }
      native_pauseNetwork(this.currentAccount);
    }
    label127:
    while (this.appPaused)
    {
      return;
      this.appResumeCount += 1;
      break;
    }
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("reset app pause time");
    }
    if ((this.lastPauseTime != 0L) && (System.currentTimeMillis() - this.lastPauseTime > 5000L)) {
      ContactsController.getInstance(this.currentAccount).checkContacts();
    }
    this.lastPauseTime = 0L;
    native_resumeNetwork(this.currentAccount, false);
  }
  
  public void setIsUpdating(final boolean paramBoolean)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (ConnectionsManager.this.isUpdating == paramBoolean) {}
        do
        {
          return;
          ConnectionsManager.access$502(ConnectionsManager.this, paramBoolean);
        } while (ConnectionsManager.this.connectionState != 3);
        NotificationCenter.getInstance(ConnectionsManager.this.currentAccount).postNotificationName(NotificationCenter.didUpdatedConnectionState, new Object[0]);
      }
    });
  }
  
  public void setPushConnectionEnabled(boolean paramBoolean)
  {
    native_setPushConnectionEnabled(this.currentAccount, paramBoolean);
  }
  
  public void setUserId(int paramInt)
  {
    native_setUserId(this.currentAccount, paramInt);
  }
  
  public void switchBackend()
  {
    MessagesController.getGlobalMainSettings().edit().remove("language_showed2").commit();
    native_switchBackend(this.currentAccount);
  }
  
  public void updateDcSettings()
  {
    native_updateDcSettings(this.currentAccount);
  }
  
  private static class AzureLoadTask
    extends AsyncTask<Void, Void, NativeByteBuffer>
  {
    private int currentAccount;
    
    public AzureLoadTask(int paramInt)
    {
      this.currentAccount = paramInt;
    }
    
    /* Error */
    protected NativeByteBuffer doInBackground(Void... paramVarArgs)
    {
      // Byte code:
      //   0: aconst_null
      //   1: astore 6
      //   3: aconst_null
      //   4: astore 7
      //   6: aconst_null
      //   7: astore 9
      //   9: aconst_null
      //   10: astore 8
      //   12: aload 8
      //   14: astore_1
      //   15: aload 6
      //   17: astore 4
      //   19: aload 9
      //   21: astore 5
      //   23: aload_0
      //   24: getfield 19	org/telegram/tgnet/ConnectionsManager$AzureLoadTask:currentAccount	I
      //   27: invokestatic 37	org/telegram/tgnet/ConnectionsManager:native_isTestBackend	(I)I
      //   30: ifeq +223 -> 253
      //   33: aload 8
      //   35: astore_1
      //   36: aload 6
      //   38: astore 4
      //   40: aload 9
      //   42: astore 5
      //   44: new 39	java/net/URL
      //   47: dup
      //   48: ldc 41
      //   50: invokespecial 44	java/net/URL:<init>	(Ljava/lang/String;)V
      //   53: astore_3
      //   54: aload 8
      //   56: astore_1
      //   57: aload 6
      //   59: astore 4
      //   61: aload 9
      //   63: astore 5
      //   65: aload_3
      //   66: invokevirtual 48	java/net/URL:openConnection	()Ljava/net/URLConnection;
      //   69: astore_3
      //   70: aload 8
      //   72: astore_1
      //   73: aload 6
      //   75: astore 4
      //   77: aload 9
      //   79: astore 5
      //   81: aload_3
      //   82: ldc 50
      //   84: ldc 52
      //   86: invokevirtual 58	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
      //   89: aload 8
      //   91: astore_1
      //   92: aload 6
      //   94: astore 4
      //   96: aload 9
      //   98: astore 5
      //   100: aload_3
      //   101: ldc 60
      //   103: ldc 62
      //   105: invokevirtual 58	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
      //   108: aload 8
      //   110: astore_1
      //   111: aload 6
      //   113: astore 4
      //   115: aload 9
      //   117: astore 5
      //   119: aload_3
      //   120: sipush 5000
      //   123: invokevirtual 65	java/net/URLConnection:setConnectTimeout	(I)V
      //   126: aload 8
      //   128: astore_1
      //   129: aload 6
      //   131: astore 4
      //   133: aload 9
      //   135: astore 5
      //   137: aload_3
      //   138: sipush 5000
      //   141: invokevirtual 68	java/net/URLConnection:setReadTimeout	(I)V
      //   144: aload 8
      //   146: astore_1
      //   147: aload 6
      //   149: astore 4
      //   151: aload 9
      //   153: astore 5
      //   155: aload_3
      //   156: invokevirtual 71	java/net/URLConnection:connect	()V
      //   159: aload 8
      //   161: astore_1
      //   162: aload 6
      //   164: astore 4
      //   166: aload 9
      //   168: astore 5
      //   170: aload_3
      //   171: invokevirtual 75	java/net/URLConnection:getInputStream	()Ljava/io/InputStream;
      //   174: astore_3
      //   175: aload_3
      //   176: astore_1
      //   177: aload 6
      //   179: astore 4
      //   181: aload_3
      //   182: astore 5
      //   184: new 77	java/io/ByteArrayOutputStream
      //   187: dup
      //   188: invokespecial 78	java/io/ByteArrayOutputStream:<init>	()V
      //   191: astore 6
      //   193: ldc 79
      //   195: newarray <illegal type>
      //   197: astore_1
      //   198: aload_0
      //   199: invokevirtual 83	org/telegram/tgnet/ConnectionsManager$AzureLoadTask:isCancelled	()Z
      //   202: ifeq +75 -> 277
      //   205: aload 6
      //   207: invokevirtual 87	java/io/ByteArrayOutputStream:toByteArray	()[B
      //   210: iconst_0
      //   211: invokestatic 93	android/util/Base64:decode	([BI)[B
      //   214: astore 4
      //   216: new 95	org/telegram/tgnet/NativeByteBuffer
      //   219: dup
      //   220: aload 4
      //   222: arraylength
      //   223: invokespecial 97	org/telegram/tgnet/NativeByteBuffer:<init>	(I)V
      //   226: astore_1
      //   227: aload_1
      //   228: aload 4
      //   230: invokevirtual 101	org/telegram/tgnet/NativeByteBuffer:writeBytes	([B)V
      //   233: aload_3
      //   234: ifnull +7 -> 241
      //   237: aload_3
      //   238: invokevirtual 106	java/io/InputStream:close	()V
      //   241: aload 6
      //   243: ifnull +8 -> 251
      //   246: aload 6
      //   248: invokevirtual 107	java/io/ByteArrayOutputStream:close	()V
      //   251: aload_1
      //   252: areturn
      //   253: aload 8
      //   255: astore_1
      //   256: aload 6
      //   258: astore 4
      //   260: aload 9
      //   262: astore 5
      //   264: new 39	java/net/URL
      //   267: dup
      //   268: ldc 109
      //   270: invokespecial 44	java/net/URL:<init>	(Ljava/lang/String;)V
      //   273: astore_3
      //   274: goto -220 -> 54
      //   277: aload_3
      //   278: aload_1
      //   279: invokevirtual 113	java/io/InputStream:read	([B)I
      //   282: istore_2
      //   283: iload_2
      //   284: ifle +53 -> 337
      //   287: aload 6
      //   289: aload_1
      //   290: iconst_0
      //   291: iload_2
      //   292: invokevirtual 117	java/io/ByteArrayOutputStream:write	([BII)V
      //   295: goto -97 -> 198
      //   298: astore_1
      //   299: aload 6
      //   301: astore 5
      //   303: aload_1
      //   304: astore 6
      //   306: aload_3
      //   307: astore_1
      //   308: aload 5
      //   310: astore 4
      //   312: aload 6
      //   314: invokestatic 123	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   317: aload_3
      //   318: ifnull +7 -> 325
      //   321: aload_3
      //   322: invokevirtual 106	java/io/InputStream:close	()V
      //   325: aload 5
      //   327: ifnull +8 -> 335
      //   330: aload 5
      //   332: invokevirtual 107	java/io/ByteArrayOutputStream:close	()V
      //   335: aconst_null
      //   336: areturn
      //   337: iload_2
      //   338: iconst_m1
      //   339: if_icmpne -134 -> 205
      //   342: goto -137 -> 205
      //   345: astore_3
      //   346: aload_3
      //   347: invokestatic 123	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   350: goto -109 -> 241
      //   353: astore_1
      //   354: aload_1
      //   355: invokestatic 123	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   358: goto -33 -> 325
      //   361: astore_3
      //   362: aload_1
      //   363: ifnull +7 -> 370
      //   366: aload_1
      //   367: invokevirtual 106	java/io/InputStream:close	()V
      //   370: aload 4
      //   372: ifnull +8 -> 380
      //   375: aload 4
      //   377: invokevirtual 107	java/io/ByteArrayOutputStream:close	()V
      //   380: aload_3
      //   381: athrow
      //   382: astore_1
      //   383: aload_1
      //   384: invokestatic 123	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   387: goto -17 -> 370
      //   390: astore_3
      //   391: goto -140 -> 251
      //   394: astore_1
      //   395: goto -60 -> 335
      //   398: astore_1
      //   399: goto -19 -> 380
      //   402: astore 5
      //   404: aload 6
      //   406: astore 4
      //   408: aload_3
      //   409: astore_1
      //   410: aload 5
      //   412: astore_3
      //   413: goto -51 -> 362
      //   416: astore 6
      //   418: aload 5
      //   420: astore_3
      //   421: aload 7
      //   423: astore 5
      //   425: goto -119 -> 306
      // Local variable table:
      //   start	length	slot	name	signature
      //   0	428	0	this	AzureLoadTask
      //   0	428	1	paramVarArgs	Void[]
      //   282	58	2	i	int
      //   53	269	3	localObject1	Object
      //   345	2	3	localThrowable1	Throwable
      //   361	20	3	localObject2	Object
      //   390	19	3	localException	Exception
      //   412	9	3	localObject3	Object
      //   17	390	4	localObject4	Object
      //   21	310	5	localObject5	Object
      //   402	17	5	localObject6	Object
      //   423	1	5	localObject7	Object
      //   1	404	6	localObject8	Object
      //   416	1	6	localThrowable2	Throwable
      //   4	418	7	localObject9	Object
      //   10	244	8	localObject10	Object
      //   7	254	9	localObject11	Object
      // Exception table:
      //   from	to	target	type
      //   193	198	298	java/lang/Throwable
      //   198	205	298	java/lang/Throwable
      //   205	233	298	java/lang/Throwable
      //   277	283	298	java/lang/Throwable
      //   287	295	298	java/lang/Throwable
      //   237	241	345	java/lang/Throwable
      //   321	325	353	java/lang/Throwable
      //   23	33	361	finally
      //   44	54	361	finally
      //   65	70	361	finally
      //   81	89	361	finally
      //   100	108	361	finally
      //   119	126	361	finally
      //   137	144	361	finally
      //   155	159	361	finally
      //   170	175	361	finally
      //   184	193	361	finally
      //   264	274	361	finally
      //   312	317	361	finally
      //   366	370	382	java/lang/Throwable
      //   246	251	390	java/lang/Exception
      //   330	335	394	java/lang/Exception
      //   375	380	398	java/lang/Exception
      //   193	198	402	finally
      //   198	205	402	finally
      //   205	233	402	finally
      //   277	283	402	finally
      //   287	295	402	finally
      //   23	33	416	java/lang/Throwable
      //   44	54	416	java/lang/Throwable
      //   65	70	416	java/lang/Throwable
      //   81	89	416	java/lang/Throwable
      //   100	108	416	java/lang/Throwable
      //   119	126	416	java/lang/Throwable
      //   137	144	416	java/lang/Throwable
      //   155	159	416	java/lang/Throwable
      //   170	175	416	java/lang/Throwable
      //   184	193	416	java/lang/Throwable
      //   264	274	416	java/lang/Throwable
    }
    
    protected void onPostExecute(final NativeByteBuffer paramNativeByteBuffer)
    {
      Utilities.stageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          if (paramNativeByteBuffer != null)
          {
            ConnectionsManager.access$302(null);
            ConnectionsManager.native_applyDnsConfig(ConnectionsManager.AzureLoadTask.this.currentAccount, paramNativeByteBuffer.address);
            return;
          }
          if (BuildVars.LOGS_ENABLED)
          {
            FileLog.d("failed to get azure result");
            FileLog.d("start dns txt task");
          }
          ConnectionsManager.DnsTxtLoadTask localDnsTxtLoadTask = new ConnectionsManager.DnsTxtLoadTask(ConnectionsManager.AzureLoadTask.this.currentAccount);
          localDnsTxtLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
          ConnectionsManager.access$302(localDnsTxtLoadTask);
        }
      });
    }
  }
  
  private static class DnsTxtLoadTask
    extends AsyncTask<Void, Void, NativeByteBuffer>
  {
    private int currentAccount;
    
    public DnsTxtLoadTask(int paramInt)
    {
      this.currentAccount = paramInt;
    }
    
    /* Error */
    protected NativeByteBuffer doInBackground(Void... paramVarArgs)
    {
      // Byte code:
      //   0: aconst_null
      //   1: astore 7
      //   3: aconst_null
      //   4: astore 8
      //   6: aconst_null
      //   7: astore 10
      //   9: aconst_null
      //   10: astore 9
      //   12: aload 9
      //   14: astore_1
      //   15: aload 7
      //   17: astore 5
      //   19: aload 10
      //   21: astore 6
      //   23: getstatic 41	java/util/Locale:US	Ljava/util/Locale;
      //   26: astore 11
      //   28: aload 9
      //   30: astore_1
      //   31: aload 7
      //   33: astore 5
      //   35: aload 10
      //   37: astore 6
      //   39: aload_0
      //   40: getfield 21	org/telegram/tgnet/ConnectionsManager$DnsTxtLoadTask:currentAccount	I
      //   43: invokestatic 45	org/telegram/tgnet/ConnectionsManager:native_isTestBackend	(I)I
      //   46: ifeq +301 -> 347
      //   49: ldc 47
      //   51: astore 4
      //   53: aload 9
      //   55: astore_1
      //   56: aload 7
      //   58: astore 5
      //   60: aload 10
      //   62: astore 6
      //   64: aload 11
      //   66: aload 4
      //   68: iconst_1
      //   69: anewarray 49	java/lang/Object
      //   72: dup
      //   73: iconst_0
      //   74: ldc 51
      //   76: aastore
      //   77: invokestatic 57	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
      //   80: astore 4
      //   82: aload 9
      //   84: astore_1
      //   85: aload 7
      //   87: astore 5
      //   89: aload 10
      //   91: astore 6
      //   93: new 59	java/net/URL
      //   96: dup
      //   97: new 61	java/lang/StringBuilder
      //   100: dup
      //   101: invokespecial 62	java/lang/StringBuilder:<init>	()V
      //   104: ldc 64
      //   106: invokevirtual 68	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
      //   109: aload 4
      //   111: invokevirtual 68	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
      //   114: ldc 70
      //   116: invokevirtual 68	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
      //   119: invokevirtual 74	java/lang/StringBuilder:toString	()Ljava/lang/String;
      //   122: invokespecial 77	java/net/URL:<init>	(Ljava/lang/String;)V
      //   125: invokevirtual 81	java/net/URL:openConnection	()Ljava/net/URLConnection;
      //   128: astore 4
      //   130: aload 9
      //   132: astore_1
      //   133: aload 7
      //   135: astore 5
      //   137: aload 10
      //   139: astore 6
      //   141: aload 4
      //   143: ldc 83
      //   145: ldc 85
      //   147: invokevirtual 91	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
      //   150: aload 9
      //   152: astore_1
      //   153: aload 7
      //   155: astore 5
      //   157: aload 10
      //   159: astore 6
      //   161: aload 4
      //   163: ldc 93
      //   165: ldc 95
      //   167: invokevirtual 91	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
      //   170: aload 9
      //   172: astore_1
      //   173: aload 7
      //   175: astore 5
      //   177: aload 10
      //   179: astore 6
      //   181: aload 4
      //   183: sipush 5000
      //   186: invokevirtual 98	java/net/URLConnection:setConnectTimeout	(I)V
      //   189: aload 9
      //   191: astore_1
      //   192: aload 7
      //   194: astore 5
      //   196: aload 10
      //   198: astore 6
      //   200: aload 4
      //   202: sipush 5000
      //   205: invokevirtual 101	java/net/URLConnection:setReadTimeout	(I)V
      //   208: aload 9
      //   210: astore_1
      //   211: aload 7
      //   213: astore 5
      //   215: aload 10
      //   217: astore 6
      //   219: aload 4
      //   221: invokevirtual 104	java/net/URLConnection:connect	()V
      //   224: aload 9
      //   226: astore_1
      //   227: aload 7
      //   229: astore 5
      //   231: aload 10
      //   233: astore 6
      //   235: aload 4
      //   237: invokevirtual 108	java/net/URLConnection:getInputStream	()Ljava/io/InputStream;
      //   240: astore 4
      //   242: aload 4
      //   244: astore_1
      //   245: aload 7
      //   247: astore 5
      //   249: aload 4
      //   251: astore 6
      //   253: new 110	java/io/ByteArrayOutputStream
      //   256: dup
      //   257: invokespecial 111	java/io/ByteArrayOutputStream:<init>	()V
      //   260: astore 7
      //   262: ldc 112
      //   264: newarray <illegal type>
      //   266: astore_1
      //   267: aload_0
      //   268: invokevirtual 116	org/telegram/tgnet/ConnectionsManager$DnsTxtLoadTask:isCancelled	()Z
      //   271: ifeq +83 -> 354
      //   274: new 118	org/json/JSONObject
      //   277: dup
      //   278: new 53	java/lang/String
      //   281: dup
      //   282: aload 7
      //   284: invokevirtual 122	java/io/ByteArrayOutputStream:toByteArray	()[B
      //   287: ldc 124
      //   289: invokespecial 127	java/lang/String:<init>	([BLjava/lang/String;)V
      //   292: invokespecial 128	org/json/JSONObject:<init>	(Ljava/lang/String;)V
      //   295: ldc -126
      //   297: invokevirtual 134	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
      //   300: astore 5
      //   302: aload 5
      //   304: invokevirtual 140	org/json/JSONArray:length	()I
      //   307: istore_3
      //   308: new 142	java/util/ArrayList
      //   311: dup
      //   312: iload_3
      //   313: invokespecial 144	java/util/ArrayList:<init>	(I)V
      //   316: astore_1
      //   317: iconst_0
      //   318: istore_2
      //   319: iload_2
      //   320: iload_3
      //   321: if_icmpge +105 -> 426
      //   324: aload_1
      //   325: aload 5
      //   327: iload_2
      //   328: invokevirtual 148	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
      //   331: ldc -106
      //   333: invokevirtual 154	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
      //   336: invokevirtual 158	java/util/ArrayList:add	(Ljava/lang/Object;)Z
      //   339: pop
      //   340: iload_2
      //   341: iconst_1
      //   342: iadd
      //   343: istore_2
      //   344: goto -25 -> 319
      //   347: ldc -96
      //   349: astore 4
      //   351: goto -298 -> 53
      //   354: aload 4
      //   356: aload_1
      //   357: invokevirtual 166	java/io/InputStream:read	([B)I
      //   360: istore_2
      //   361: iload_2
      //   362: ifle +56 -> 418
      //   365: aload 7
      //   367: aload_1
      //   368: iconst_0
      //   369: iload_2
      //   370: invokevirtual 170	java/io/ByteArrayOutputStream:write	([BII)V
      //   373: goto -106 -> 267
      //   376: astore_1
      //   377: aload 7
      //   379: astore 6
      //   381: aload_1
      //   382: astore 7
      //   384: aload 4
      //   386: astore_1
      //   387: aload 6
      //   389: astore 5
      //   391: aload 7
      //   393: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   396: aload 4
      //   398: ifnull +8 -> 406
      //   401: aload 4
      //   403: invokevirtual 179	java/io/InputStream:close	()V
      //   406: aload 6
      //   408: ifnull +8 -> 416
      //   411: aload 6
      //   413: invokevirtual 180	java/io/ByteArrayOutputStream:close	()V
      //   416: aconst_null
      //   417: areturn
      //   418: iload_2
      //   419: iconst_m1
      //   420: if_icmpne -146 -> 274
      //   423: goto -149 -> 274
      //   426: aload_1
      //   427: new 10	org/telegram/tgnet/ConnectionsManager$DnsTxtLoadTask$1
      //   430: dup
      //   431: aload_0
      //   432: invokespecial 183	org/telegram/tgnet/ConnectionsManager$DnsTxtLoadTask$1:<init>	(Lorg/telegram/tgnet/ConnectionsManager$DnsTxtLoadTask;)V
      //   435: invokestatic 189	java/util/Collections:sort	(Ljava/util/List;Ljava/util/Comparator;)V
      //   438: new 61	java/lang/StringBuilder
      //   441: dup
      //   442: invokespecial 62	java/lang/StringBuilder:<init>	()V
      //   445: astore 5
      //   447: iconst_0
      //   448: istore_2
      //   449: iload_2
      //   450: aload_1
      //   451: invokevirtual 192	java/util/ArrayList:size	()I
      //   454: if_icmpge +31 -> 485
      //   457: aload 5
      //   459: aload_1
      //   460: iload_2
      //   461: invokevirtual 196	java/util/ArrayList:get	(I)Ljava/lang/Object;
      //   464: checkcast 53	java/lang/String
      //   467: ldc -58
      //   469: ldc 51
      //   471: invokevirtual 202	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
      //   474: invokevirtual 68	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
      //   477: pop
      //   478: iload_2
      //   479: iconst_1
      //   480: iadd
      //   481: istore_2
      //   482: goto -33 -> 449
      //   485: aload 5
      //   487: invokevirtual 74	java/lang/StringBuilder:toString	()Ljava/lang/String;
      //   490: iconst_0
      //   491: invokestatic 208	android/util/Base64:decode	(Ljava/lang/String;I)[B
      //   494: astore 5
      //   496: new 210	org/telegram/tgnet/NativeByteBuffer
      //   499: dup
      //   500: aload 5
      //   502: arraylength
      //   503: invokespecial 211	org/telegram/tgnet/NativeByteBuffer:<init>	(I)V
      //   506: astore_1
      //   507: aload_1
      //   508: aload 5
      //   510: invokevirtual 215	org/telegram/tgnet/NativeByteBuffer:writeBytes	([B)V
      //   513: aload 4
      //   515: ifnull +8 -> 523
      //   518: aload 4
      //   520: invokevirtual 179	java/io/InputStream:close	()V
      //   523: aload 7
      //   525: ifnull +8 -> 533
      //   528: aload 7
      //   530: invokevirtual 180	java/io/ByteArrayOutputStream:close	()V
      //   533: aload_1
      //   534: areturn
      //   535: astore 4
      //   537: aload 4
      //   539: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   542: goto -19 -> 523
      //   545: astore_1
      //   546: aload_1
      //   547: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   550: goto -144 -> 406
      //   553: astore 4
      //   555: aload_1
      //   556: ifnull +7 -> 563
      //   559: aload_1
      //   560: invokevirtual 179	java/io/InputStream:close	()V
      //   563: aload 5
      //   565: ifnull +8 -> 573
      //   568: aload 5
      //   570: invokevirtual 180	java/io/ByteArrayOutputStream:close	()V
      //   573: aload 4
      //   575: athrow
      //   576: astore_1
      //   577: aload_1
      //   578: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   581: goto -18 -> 563
      //   584: astore 4
      //   586: goto -53 -> 533
      //   589: astore_1
      //   590: goto -174 -> 416
      //   593: astore_1
      //   594: goto -21 -> 573
      //   597: astore 6
      //   599: aload 7
      //   601: astore 5
      //   603: aload 4
      //   605: astore_1
      //   606: aload 6
      //   608: astore 4
      //   610: goto -55 -> 555
      //   613: astore 7
      //   615: aload 6
      //   617: astore 4
      //   619: aload 8
      //   621: astore 6
      //   623: goto -239 -> 384
      // Local variable table:
      //   start	length	slot	name	signature
      //   0	626	0	this	DnsTxtLoadTask
      //   0	626	1	paramVarArgs	Void[]
      //   318	164	2	i	int
      //   307	15	3	j	int
      //   51	468	4	localObject1	Object
      //   535	3	4	localThrowable1	Throwable
      //   553	21	4	localObject2	Object
      //   584	20	4	localException	Exception
      //   608	10	4	localObject3	Object
      //   17	585	5	localObject4	Object
      //   21	391	6	localObject5	Object
      //   597	19	6	localObject6	Object
      //   621	1	6	localObject7	Object
      //   1	599	7	localObject8	Object
      //   613	1	7	localThrowable2	Throwable
      //   4	616	8	localObject9	Object
      //   10	215	9	localObject10	Object
      //   7	225	10	localObject11	Object
      //   26	39	11	localLocale	java.util.Locale
      // Exception table:
      //   from	to	target	type
      //   262	267	376	java/lang/Throwable
      //   267	274	376	java/lang/Throwable
      //   274	317	376	java/lang/Throwable
      //   324	340	376	java/lang/Throwable
      //   354	361	376	java/lang/Throwable
      //   365	373	376	java/lang/Throwable
      //   426	447	376	java/lang/Throwable
      //   449	478	376	java/lang/Throwable
      //   485	513	376	java/lang/Throwable
      //   518	523	535	java/lang/Throwable
      //   401	406	545	java/lang/Throwable
      //   23	28	553	finally
      //   39	49	553	finally
      //   64	82	553	finally
      //   93	130	553	finally
      //   141	150	553	finally
      //   161	170	553	finally
      //   181	189	553	finally
      //   200	208	553	finally
      //   219	224	553	finally
      //   235	242	553	finally
      //   253	262	553	finally
      //   391	396	553	finally
      //   559	563	576	java/lang/Throwable
      //   528	533	584	java/lang/Exception
      //   411	416	589	java/lang/Exception
      //   568	573	593	java/lang/Exception
      //   262	267	597	finally
      //   267	274	597	finally
      //   274	317	597	finally
      //   324	340	597	finally
      //   354	361	597	finally
      //   365	373	597	finally
      //   426	447	597	finally
      //   449	478	597	finally
      //   485	513	597	finally
      //   23	28	613	java/lang/Throwable
      //   39	49	613	java/lang/Throwable
      //   64	82	613	java/lang/Throwable
      //   93	130	613	java/lang/Throwable
      //   141	150	613	java/lang/Throwable
      //   161	170	613	java/lang/Throwable
      //   181	189	613	java/lang/Throwable
      //   200	208	613	java/lang/Throwable
      //   219	224	613	java/lang/Throwable
      //   235	242	613	java/lang/Throwable
      //   253	262	613	java/lang/Throwable
    }
    
    protected void onPostExecute(final NativeByteBuffer paramNativeByteBuffer)
    {
      Utilities.stageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          if (paramNativeByteBuffer != null) {
            ConnectionsManager.native_applyDnsConfig(ConnectionsManager.DnsTxtLoadTask.this.currentAccount, paramNativeByteBuffer.address);
          }
          for (;;)
          {
            ConnectionsManager.access$302(null);
            return;
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("failed to get dns txt result");
            }
          }
        }
      });
    }
  }
  
  private static class FirebaseTask
    extends AsyncTask<Void, Void, NativeByteBuffer>
  {
    private int currentAccount;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    
    public FirebaseTask(int paramInt)
    {
      this.currentAccount = paramInt;
    }
    
    protected NativeByteBuffer doInBackground(Void... paramVarArgs)
    {
      try
      {
        this.firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        paramVarArgs = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(false).build();
        this.firebaseRemoteConfig.setConfigSettings(paramVarArgs);
        paramVarArgs = this.firebaseRemoteConfig.getString("ipconfig");
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("current firebase value = " + paramVarArgs);
        }
        this.firebaseRemoteConfig.fetch(0L).addOnCompleteListener(new OnCompleteListener()
        {
          public void onComplete(Task<Void> paramAnonymousTask)
          {
            final boolean bool = paramAnonymousTask.isSuccessful();
            Utilities.stageQueue.postRunnable(new Runnable()
            {
              public void run()
              {
                ConnectionsManager.access$302(null);
                Object localObject = null;
                if (bool)
                {
                  ConnectionsManager.FirebaseTask.this.firebaseRemoteConfig.activateFetched();
                  localObject = ConnectionsManager.FirebaseTask.this.firebaseRemoteConfig.getString("ipconfig");
                }
                if (!TextUtils.isEmpty((CharSequence)localObject))
                {
                  localObject = Base64.decode((String)localObject, 0);
                  try
                  {
                    NativeByteBuffer localNativeByteBuffer = new NativeByteBuffer(localObject.length);
                    localNativeByteBuffer.writeBytes((byte[])localObject);
                    ConnectionsManager.native_applyDnsConfig(ConnectionsManager.FirebaseTask.this.currentAccount, localNativeByteBuffer.address);
                    return;
                  }
                  catch (Exception localException)
                  {
                    FileLog.e(localException);
                    return;
                  }
                }
                if (BuildVars.LOGS_ENABLED)
                {
                  FileLog.d("failed to get firebase result");
                  FileLog.d("start azure task");
                }
                ConnectionsManager.AzureLoadTask localAzureLoadTask = new ConnectionsManager.AzureLoadTask(ConnectionsManager.FirebaseTask.this.currentAccount);
                localAzureLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
                ConnectionsManager.access$302(localAzureLoadTask);
              }
            });
          }
        });
      }
      catch (Throwable paramVarArgs)
      {
        for (;;)
        {
          Utilities.stageQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              if (BuildVars.LOGS_ENABLED)
              {
                FileLog.d("failed to get firebase result");
                FileLog.d("start azure task");
              }
              ConnectionsManager.AzureLoadTask localAzureLoadTask = new ConnectionsManager.AzureLoadTask(ConnectionsManager.FirebaseTask.this.currentAccount);
              localAzureLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
              ConnectionsManager.access$302(localAzureLoadTask);
            }
          });
          FileLog.e(paramVarArgs);
        }
      }
      return null;
    }
    
    protected void onPostExecute(NativeByteBuffer paramNativeByteBuffer) {}
  }
  
  private static class ResolvedDomain
  {
    public String address;
    long ttl;
    
    public ResolvedDomain(String paramString, long paramLong)
    {
      this.address = paramString;
      this.ttl = paramLong;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/tgnet/ConnectionsManager.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */