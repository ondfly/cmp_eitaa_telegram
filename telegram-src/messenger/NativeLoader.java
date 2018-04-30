package org.telegram.messenger;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import java.io.File;
import java.lang.reflect.Field;

public class NativeLoader
{
  private static final String LIB_NAME = "tmessages.28";
  private static final String LIB_SO_NAME = "libtmessages.28.so";
  private static final int LIB_VERSION = 28;
  private static final String LOCALE_LIB_SO_NAME = "libtmessages.28loc.so";
  private static volatile boolean nativeLoaded = false;
  private String crashPath = "";
  
  private static File getNativeLibraryDir(Context paramContext)
  {
    Object localObject3 = null;
    Object localObject1 = localObject3;
    if (paramContext != null) {}
    try
    {
      localObject1 = new File((String)ApplicationInfo.class.getField("nativeLibraryDir").get(paramContext.getApplicationInfo()));
      localObject3 = localObject1;
      if (localObject1 == null) {
        localObject3 = new File(paramContext.getApplicationInfo().dataDir, "lib");
      }
      if (((File)localObject3).isDirectory()) {
        return (File)localObject3;
      }
    }
    catch (Throwable localThrowable)
    {
      for (;;)
      {
        localThrowable.printStackTrace();
        Object localObject2 = localObject3;
      }
    }
    return null;
  }
  
  private static native void init(String paramString, boolean paramBoolean);
  
  /* Error */
  @android.annotation.SuppressLint({"UnsafeDynamicallyLoadedCode"})
  public static void initNativeLibs(Context paramContext)
  {
    // Byte code:
    //   0: ldc 2
    //   2: monitorenter
    //   3: getstatic 24	org/telegram/messenger/NativeLoader:nativeLoaded	Z
    //   6: istore_1
    //   7: iload_1
    //   8: ifeq +7 -> 15
    //   11: ldc 2
    //   13: monitorexit
    //   14: return
    //   15: aload_0
    //   16: invokestatic 96	net/hockeyapp/android/Constants:loadFromContext	(Landroid/content/Context;)V
    //   19: ldc 8
    //   21: invokestatic 101	java/lang/System:loadLibrary	(Ljava/lang/String;)V
    //   24: iconst_1
    //   25: putstatic 24	org/telegram/messenger/NativeLoader:nativeLoaded	Z
    //   28: getstatic 106	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   31: ifeq -20 -> 11
    //   34: ldc 108
    //   36: invokestatic 113	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   39: goto -28 -> 11
    //   42: astore_2
    //   43: aload_2
    //   44: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   47: getstatic 122	android/os/Build:CPU_ABI	Ljava/lang/String;
    //   50: astore_2
    //   51: getstatic 122	android/os/Build:CPU_ABI	Ljava/lang/String;
    //   54: ldc 124
    //   56: invokevirtual 128	java/lang/String:equalsIgnoreCase	(Ljava/lang/String;)Z
    //   59: ifeq +182 -> 241
    //   62: ldc 124
    //   64: astore_2
    //   65: ldc -126
    //   67: invokestatic 134	java/lang/System:getProperty	(Ljava/lang/String;)Ljava/lang/String;
    //   70: astore 4
    //   72: aload_2
    //   73: astore_3
    //   74: aload 4
    //   76: ifnull +18 -> 94
    //   79: aload_2
    //   80: astore_3
    //   81: aload 4
    //   83: ldc -120
    //   85: invokevirtual 140	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   88: ifeq +6 -> 94
    //   91: ldc -114
    //   93: astore_3
    //   94: new 38	java/io/File
    //   97: dup
    //   98: aload_0
    //   99: invokevirtual 146	android/content/Context:getFilesDir	()Ljava/io/File;
    //   102: ldc 70
    //   104: invokespecial 149	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   107: astore_2
    //   108: aload_2
    //   109: invokevirtual 152	java/io/File:mkdirs	()Z
    //   112: pop
    //   113: new 38	java/io/File
    //   116: dup
    //   117: aload_2
    //   118: ldc 17
    //   120: invokespecial 149	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   123: astore 4
    //   125: aload 4
    //   127: invokevirtual 155	java/io/File:exists	()Z
    //   130: istore_1
    //   131: iload_1
    //   132: ifeq +42 -> 174
    //   135: getstatic 106	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   138: ifeq +8 -> 146
    //   141: ldc -99
    //   143: invokestatic 113	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   146: aload 4
    //   148: invokevirtual 161	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   151: invokestatic 164	java/lang/System:load	(Ljava/lang/String;)V
    //   154: iconst_1
    //   155: putstatic 24	org/telegram/messenger/NativeLoader:nativeLoaded	Z
    //   158: goto -147 -> 11
    //   161: astore 5
    //   163: aload 5
    //   165: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   168: aload 4
    //   170: invokevirtual 167	java/io/File:delete	()Z
    //   173: pop
    //   174: getstatic 106	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   177: ifeq +25 -> 202
    //   180: new 169	java/lang/StringBuilder
    //   183: dup
    //   184: invokespecial 170	java/lang/StringBuilder:<init>	()V
    //   187: ldc -84
    //   189: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   192: aload_3
    //   193: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   196: invokevirtual 179	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   199: invokestatic 181	org/telegram/messenger/FileLog:e	(Ljava/lang/String;)V
    //   202: aload_0
    //   203: aload_2
    //   204: aload 4
    //   206: aload_3
    //   207: invokestatic 185	org/telegram/messenger/NativeLoader:loadFromZip	(Landroid/content/Context;Ljava/io/File;Ljava/io/File;Ljava/lang/String;)Z
    //   210: istore_1
    //   211: iload_1
    //   212: ifne -201 -> 11
    //   215: ldc 8
    //   217: invokestatic 101	java/lang/System:loadLibrary	(Ljava/lang/String;)V
    //   220: iconst_1
    //   221: putstatic 24	org/telegram/messenger/NativeLoader:nativeLoaded	Z
    //   224: goto -213 -> 11
    //   227: astore_0
    //   228: aload_0
    //   229: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   232: goto -221 -> 11
    //   235: astore_0
    //   236: ldc 2
    //   238: monitorexit
    //   239: aload_0
    //   240: athrow
    //   241: getstatic 122	android/os/Build:CPU_ABI	Ljava/lang/String;
    //   244: ldc -69
    //   246: invokevirtual 128	java/lang/String:equalsIgnoreCase	(Ljava/lang/String;)Z
    //   249: ifeq +9 -> 258
    //   252: ldc -69
    //   254: astore_2
    //   255: goto -190 -> 65
    //   258: getstatic 122	android/os/Build:CPU_ABI	Ljava/lang/String;
    //   261: ldc -67
    //   263: invokevirtual 128	java/lang/String:equalsIgnoreCase	(Ljava/lang/String;)Z
    //   266: ifeq +9 -> 275
    //   269: ldc -67
    //   271: astore_2
    //   272: goto -207 -> 65
    //   275: getstatic 122	android/os/Build:CPU_ABI	Ljava/lang/String;
    //   278: ldc -65
    //   280: invokevirtual 128	java/lang/String:equalsIgnoreCase	(Ljava/lang/String;)Z
    //   283: ifeq +9 -> 292
    //   286: ldc -65
    //   288: astore_2
    //   289: goto -224 -> 65
    //   292: getstatic 122	android/os/Build:CPU_ABI	Ljava/lang/String;
    //   295: ldc -114
    //   297: invokevirtual 128	java/lang/String:equalsIgnoreCase	(Ljava/lang/String;)Z
    //   300: ifeq +9 -> 309
    //   303: ldc -114
    //   305: astore_2
    //   306: goto -241 -> 65
    //   309: getstatic 122	android/os/Build:CPU_ABI	Ljava/lang/String;
    //   312: ldc -63
    //   314: invokevirtual 128	java/lang/String:equalsIgnoreCase	(Ljava/lang/String;)Z
    //   317: ifeq +9 -> 326
    //   320: ldc -63
    //   322: astore_2
    //   323: goto -258 -> 65
    //   326: ldc -65
    //   328: astore_3
    //   329: aload_3
    //   330: astore_2
    //   331: getstatic 106	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   334: ifeq -269 -> 65
    //   337: new 169	java/lang/StringBuilder
    //   340: dup
    //   341: invokespecial 170	java/lang/StringBuilder:<init>	()V
    //   344: ldc -61
    //   346: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   349: getstatic 122	android/os/Build:CPU_ABI	Ljava/lang/String;
    //   352: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   355: invokevirtual 179	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   358: invokestatic 181	org/telegram/messenger/FileLog:e	(Ljava/lang/String;)V
    //   361: aload_3
    //   362: astore_2
    //   363: goto -298 -> 65
    //   366: astore_2
    //   367: aload_2
    //   368: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   371: ldc -65
    //   373: astore_2
    //   374: goto -309 -> 65
    //   377: astore_0
    //   378: aload_0
    //   379: invokevirtual 80	java/lang/Throwable:printStackTrace	()V
    //   382: goto -167 -> 215
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	385	0	paramContext	Context
    //   6	206	1	bool	boolean
    //   42	2	2	localError1	Error
    //   50	313	2	localObject1	Object
    //   366	2	2	localException	Exception
    //   373	1	2	str	String
    //   73	289	3	localObject2	Object
    //   70	135	4	localObject3	Object
    //   161	3	5	localError2	Error
    // Exception table:
    //   from	to	target	type
    //   19	39	42	java/lang/Error
    //   135	146	161	java/lang/Error
    //   146	158	161	java/lang/Error
    //   215	224	227	java/lang/Error
    //   3	7	235	finally
    //   15	19	235	finally
    //   19	39	235	finally
    //   43	47	235	finally
    //   47	62	235	finally
    //   65	72	235	finally
    //   81	91	235	finally
    //   94	131	235	finally
    //   135	146	235	finally
    //   146	158	235	finally
    //   163	174	235	finally
    //   174	202	235	finally
    //   202	211	235	finally
    //   215	224	235	finally
    //   228	232	235	finally
    //   241	252	235	finally
    //   258	269	235	finally
    //   275	286	235	finally
    //   292	303	235	finally
    //   309	320	235	finally
    //   331	361	235	finally
    //   367	371	235	finally
    //   378	382	235	finally
    //   47	62	366	java/lang/Exception
    //   241	252	366	java/lang/Exception
    //   258	269	366	java/lang/Exception
    //   275	286	366	java/lang/Exception
    //   292	303	366	java/lang/Exception
    //   309	320	366	java/lang/Exception
    //   331	361	366	java/lang/Exception
    //   19	39	377	java/lang/Throwable
    //   43	47	377	java/lang/Throwable
    //   47	62	377	java/lang/Throwable
    //   65	72	377	java/lang/Throwable
    //   81	91	377	java/lang/Throwable
    //   94	131	377	java/lang/Throwable
    //   135	146	377	java/lang/Throwable
    //   146	158	377	java/lang/Throwable
    //   163	174	377	java/lang/Throwable
    //   174	202	377	java/lang/Throwable
    //   202	211	377	java/lang/Throwable
    //   241	252	377	java/lang/Throwable
    //   258	269	377	java/lang/Throwable
    //   275	286	377	java/lang/Throwable
    //   292	303	377	java/lang/Throwable
    //   309	320	377	java/lang/Throwable
    //   331	361	377	java/lang/Throwable
    //   367	371	377	java/lang/Throwable
  }
  
  /* Error */
  @android.annotation.SuppressLint({"UnsafeDynamicallyLoadedCode", "SetWorldReadable"})
  private static boolean loadFromZip(Context paramContext, File paramFile1, File paramFile2, String paramString)
  {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual 201	java/io/File:listFiles	()[Ljava/io/File;
    //   4: astore_1
    //   5: aload_1
    //   6: arraylength
    //   7: istore 5
    //   9: iconst_0
    //   10: istore 4
    //   12: iload 4
    //   14: iload 5
    //   16: if_icmpge +25 -> 41
    //   19: aload_1
    //   20: iload 4
    //   22: aaload
    //   23: invokevirtual 167	java/io/File:delete	()Z
    //   26: pop
    //   27: iload 4
    //   29: iconst_1
    //   30: iadd
    //   31: istore 4
    //   33: goto -21 -> 12
    //   36: astore_1
    //   37: aload_1
    //   38: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   41: aconst_null
    //   42: astore 6
    //   44: aconst_null
    //   45: astore 8
    //   47: aconst_null
    //   48: astore 11
    //   50: aconst_null
    //   51: astore 10
    //   53: aconst_null
    //   54: astore_1
    //   55: aconst_null
    //   56: astore 9
    //   58: new 203	java/util/zip/ZipFile
    //   61: dup
    //   62: aload_0
    //   63: invokevirtual 54	android/content/Context:getApplicationInfo	()Landroid/content/pm/ApplicationInfo;
    //   66: getfield 206	android/content/pm/ApplicationInfo:sourceDir	Ljava/lang/String;
    //   69: invokespecial 207	java/util/zip/ZipFile:<init>	(Ljava/lang/String;)V
    //   72: astore 7
    //   74: aload 9
    //   76: astore_1
    //   77: aload 10
    //   79: astore_0
    //   80: aload 7
    //   82: new 169	java/lang/StringBuilder
    //   85: dup
    //   86: invokespecial 170	java/lang/StringBuilder:<init>	()V
    //   89: ldc -47
    //   91: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   94: aload_3
    //   95: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   98: ldc -45
    //   100: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   103: ldc 11
    //   105: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   108: invokevirtual 179	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   111: invokevirtual 215	java/util/zip/ZipFile:getEntry	(Ljava/lang/String;)Ljava/util/zip/ZipEntry;
    //   114: astore 6
    //   116: aload 6
    //   118: ifnonnull +79 -> 197
    //   121: aload 9
    //   123: astore_1
    //   124: aload 10
    //   126: astore_0
    //   127: new 91	java/lang/Exception
    //   130: dup
    //   131: new 169	java/lang/StringBuilder
    //   134: dup
    //   135: invokespecial 170	java/lang/StringBuilder:<init>	()V
    //   138: ldc -39
    //   140: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   143: aload_3
    //   144: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   147: ldc -45
    //   149: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   152: ldc 8
    //   154: invokevirtual 176	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   157: invokevirtual 179	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   160: invokespecial 218	java/lang/Exception:<init>	(Ljava/lang/String;)V
    //   163: athrow
    //   164: astore_3
    //   165: aload 7
    //   167: astore_0
    //   168: aload_1
    //   169: astore_2
    //   170: aload_2
    //   171: astore_1
    //   172: aload_0
    //   173: astore 6
    //   175: aload_3
    //   176: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   179: aload_2
    //   180: ifnull +7 -> 187
    //   183: aload_2
    //   184: invokevirtual 223	java/io/InputStream:close	()V
    //   187: aload_0
    //   188: ifnull +7 -> 195
    //   191: aload_0
    //   192: invokevirtual 224	java/util/zip/ZipFile:close	()V
    //   195: iconst_0
    //   196: ireturn
    //   197: aload 9
    //   199: astore_1
    //   200: aload 10
    //   202: astore_0
    //   203: aload 7
    //   205: aload 6
    //   207: invokevirtual 228	java/util/zip/ZipFile:getInputStream	(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;
    //   210: astore_3
    //   211: aload_3
    //   212: astore_1
    //   213: aload_3
    //   214: astore_0
    //   215: new 230	java/io/FileOutputStream
    //   218: dup
    //   219: aload_2
    //   220: invokespecial 233	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   223: astore 6
    //   225: aload_3
    //   226: astore_1
    //   227: aload_3
    //   228: astore_0
    //   229: sipush 4096
    //   232: newarray <illegal type>
    //   234: astore 8
    //   236: aload_3
    //   237: astore_1
    //   238: aload_3
    //   239: astore_0
    //   240: aload_3
    //   241: aload 8
    //   243: invokevirtual 237	java/io/InputStream:read	([B)I
    //   246: istore 4
    //   248: iload 4
    //   250: ifle +52 -> 302
    //   253: aload_3
    //   254: astore_1
    //   255: aload_3
    //   256: astore_0
    //   257: invokestatic 242	java/lang/Thread:yield	()V
    //   260: aload_3
    //   261: astore_1
    //   262: aload_3
    //   263: astore_0
    //   264: aload 6
    //   266: aload 8
    //   268: iconst_0
    //   269: iload 4
    //   271: invokevirtual 248	java/io/OutputStream:write	([BII)V
    //   274: goto -38 -> 236
    //   277: astore_1
    //   278: aload 7
    //   280: astore 6
    //   282: aload_0
    //   283: ifnull +7 -> 290
    //   286: aload_0
    //   287: invokevirtual 223	java/io/InputStream:close	()V
    //   290: aload 6
    //   292: ifnull +8 -> 300
    //   295: aload 6
    //   297: invokevirtual 224	java/util/zip/ZipFile:close	()V
    //   300: aload_1
    //   301: athrow
    //   302: aload_3
    //   303: astore_1
    //   304: aload_3
    //   305: astore_0
    //   306: aload 6
    //   308: invokevirtual 249	java/io/OutputStream:close	()V
    //   311: aload_3
    //   312: astore_1
    //   313: aload_3
    //   314: astore_0
    //   315: aload_2
    //   316: iconst_1
    //   317: iconst_0
    //   318: invokevirtual 253	java/io/File:setReadable	(ZZ)Z
    //   321: pop
    //   322: aload_3
    //   323: astore_1
    //   324: aload_3
    //   325: astore_0
    //   326: aload_2
    //   327: iconst_1
    //   328: iconst_0
    //   329: invokevirtual 256	java/io/File:setExecutable	(ZZ)Z
    //   332: pop
    //   333: aload_3
    //   334: astore_1
    //   335: aload_3
    //   336: astore_0
    //   337: aload_2
    //   338: iconst_1
    //   339: invokevirtual 260	java/io/File:setWritable	(Z)Z
    //   342: pop
    //   343: aload_3
    //   344: astore_1
    //   345: aload_3
    //   346: astore_0
    //   347: aload_2
    //   348: invokevirtual 161	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   351: invokestatic 164	java/lang/System:load	(Ljava/lang/String;)V
    //   354: aload_3
    //   355: astore_1
    //   356: aload_3
    //   357: astore_0
    //   358: iconst_1
    //   359: putstatic 24	org/telegram/messenger/NativeLoader:nativeLoaded	Z
    //   362: aload_3
    //   363: ifnull +7 -> 370
    //   366: aload_3
    //   367: invokevirtual 223	java/io/InputStream:close	()V
    //   370: aload 7
    //   372: ifnull +8 -> 380
    //   375: aload 7
    //   377: invokevirtual 224	java/util/zip/ZipFile:close	()V
    //   380: iconst_1
    //   381: ireturn
    //   382: astore_2
    //   383: aload_3
    //   384: astore_1
    //   385: aload_3
    //   386: astore_0
    //   387: aload_2
    //   388: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   391: goto -29 -> 362
    //   394: astore_0
    //   395: aload_0
    //   396: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   399: goto -29 -> 370
    //   402: astore_0
    //   403: aload_0
    //   404: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   407: goto -27 -> 380
    //   410: astore_1
    //   411: aload_1
    //   412: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   415: goto -228 -> 187
    //   418: astore_0
    //   419: aload_0
    //   420: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   423: goto -228 -> 195
    //   426: astore_0
    //   427: aload_0
    //   428: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   431: goto -141 -> 290
    //   434: astore_0
    //   435: aload_0
    //   436: invokestatic 117	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   439: goto -139 -> 300
    //   442: astore_2
    //   443: aload_1
    //   444: astore_0
    //   445: aload_2
    //   446: astore_1
    //   447: goto -165 -> 282
    //   450: astore_3
    //   451: aload 11
    //   453: astore_2
    //   454: aload 8
    //   456: astore_0
    //   457: goto -287 -> 170
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	460	0	paramContext	Context
    //   0	460	1	paramFile1	File
    //   0	460	2	paramFile2	File
    //   0	460	3	paramString	String
    //   10	260	4	i	int
    //   7	10	5	j	int
    //   42	265	6	localObject1	Object
    //   72	304	7	localZipFile	java.util.zip.ZipFile
    //   45	410	8	arrayOfByte	byte[]
    //   56	142	9	localObject2	Object
    //   51	150	10	localObject3	Object
    //   48	404	11	localObject4	Object
    // Exception table:
    //   from	to	target	type
    //   0	9	36	java/lang/Exception
    //   19	27	36	java/lang/Exception
    //   80	116	164	java/lang/Exception
    //   127	164	164	java/lang/Exception
    //   203	211	164	java/lang/Exception
    //   215	225	164	java/lang/Exception
    //   229	236	164	java/lang/Exception
    //   240	248	164	java/lang/Exception
    //   257	260	164	java/lang/Exception
    //   264	274	164	java/lang/Exception
    //   306	311	164	java/lang/Exception
    //   315	322	164	java/lang/Exception
    //   326	333	164	java/lang/Exception
    //   337	343	164	java/lang/Exception
    //   347	354	164	java/lang/Exception
    //   358	362	164	java/lang/Exception
    //   387	391	164	java/lang/Exception
    //   80	116	277	finally
    //   127	164	277	finally
    //   203	211	277	finally
    //   215	225	277	finally
    //   229	236	277	finally
    //   240	248	277	finally
    //   257	260	277	finally
    //   264	274	277	finally
    //   306	311	277	finally
    //   315	322	277	finally
    //   326	333	277	finally
    //   337	343	277	finally
    //   347	354	277	finally
    //   358	362	277	finally
    //   387	391	277	finally
    //   347	354	382	java/lang/Error
    //   358	362	382	java/lang/Error
    //   366	370	394	java/lang/Exception
    //   375	380	402	java/lang/Exception
    //   183	187	410	java/lang/Exception
    //   191	195	418	java/lang/Exception
    //   286	290	426	java/lang/Exception
    //   295	300	434	java/lang/Exception
    //   58	74	442	finally
    //   175	179	442	finally
    //   58	74	450	java/lang/Exception
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/NativeLoader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */