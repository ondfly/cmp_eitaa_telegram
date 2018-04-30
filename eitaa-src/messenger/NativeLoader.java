package ir.eitaa.messenger;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import java.io.File;
import java.lang.reflect.Field;

public class NativeLoader
{
  private static final String LIB_NAME = "tsms.24";
  private static final String LIB_SO_NAME = "libtsms.24.so";
  private static final int LIB_VERSION = 24;
  private static final String LOCALE_LIB_SO_NAME = "libtsms.24loc.so";
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
  
  public static void initNativeLibs(Context paramContext)
  {
    for (;;)
    {
      boolean bool;
      try
      {
        bool = nativeLoaded;
        if (bool) {
          return;
        }
      }
      finally {}
      try
      {
        if (!Build.CPU_ABI.equalsIgnoreCase("armeabi-v7a")) {
          break label255;
        }
        localObject1 = "armeabi-v7a";
      }
      catch (Exception localException)
      {
        FileLog.e("TSMS", localException);
        String str = "armeabi";
        continue;
      }
      catch (Throwable paramContext)
      {
        paramContext.printStackTrace();
        continue;
        continue;
      }
      Object localObject4 = System.getProperty("os.arch");
      Object localObject3 = localObject1;
      if (localObject4 != null)
      {
        localObject3 = localObject1;
        if (((String)localObject4).contains("686")) {
          localObject3 = "x86";
        }
      }
      Object localObject1 = getNativeLibraryDir(paramContext);
      if (localObject1 != null)
      {
        if (!new File((File)localObject1, "libtsms.24.so").exists()) {
          break;
        }
        FileLog.d("TSMS", "load normal lib");
        try
        {
          System.loadLibrary("tsms.24");
          nativeLoaded = true;
        }
        catch (Error localError1)
        {
          FileLog.e("TSMS", localError1);
          break;
        }
      }
      else
      {
        Object localObject2 = new File(paramContext.getFilesDir(), "lib");
        ((File)localObject2).mkdirs();
        localObject4 = new File((File)localObject2, "libtsms.24loc.so");
        bool = ((File)localObject4).exists();
        if (bool)
        {
          try
          {
            FileLog.d("TSMS", "Load local lib");
            System.load(((File)localObject4).getAbsolutePath());
            nativeLoaded = true;
          }
          catch (Error localError2)
          {
            FileLog.e("TSMS", localError2);
            ((File)localObject4).delete();
          }
        }
        else
        {
          FileLog.e("TSMS", "Library not found, arch = " + (String)localObject3);
          bool = loadFromZip(paramContext, (File)localObject2, (File)localObject4, (String)localObject3);
          if (!bool)
          {
            try
            {
              System.loadLibrary("tsms.24");
              nativeLoaded = true;
            }
            catch (Error paramContext)
            {
              FileLog.e("TSMS", paramContext);
            }
            continue;
            label255:
            if (Build.CPU_ABI.equalsIgnoreCase("armeabi"))
            {
              localObject2 = "armeabi";
            }
            else if (Build.CPU_ABI.equalsIgnoreCase("x86"))
            {
              localObject2 = "x86";
            }
            else if (Build.CPU_ABI.equalsIgnoreCase("mips"))
            {
              localObject2 = "mips";
            }
            else
            {
              localObject2 = "armeabi";
              FileLog.e("TSMS", "Unsupported arch: " + Build.CPU_ABI);
            }
          }
        }
      }
    }
  }
  
  /* Error */
  private static boolean loadFromZip(Context paramContext, File paramFile1, File paramFile2, String paramString)
  {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual 186	java/io/File:listFiles	()[Ljava/io/File;
    //   4: astore_1
    //   5: aload_1
    //   6: arraylength
    //   7: istore 5
    //   9: iconst_0
    //   10: istore 4
    //   12: iload 4
    //   14: iload 5
    //   16: if_icmpge +27 -> 43
    //   19: aload_1
    //   20: iload 4
    //   22: aaload
    //   23: invokevirtual 158	java/io/File:delete	()Z
    //   26: pop
    //   27: iload 4
    //   29: iconst_1
    //   30: iadd
    //   31: istore 4
    //   33: goto -21 -> 12
    //   36: astore_1
    //   37: ldc 125
    //   39: aload_1
    //   40: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   43: aconst_null
    //   44: astore 6
    //   46: aconst_null
    //   47: astore 8
    //   49: aconst_null
    //   50: astore 11
    //   52: aconst_null
    //   53: astore 10
    //   55: aconst_null
    //   56: astore_1
    //   57: aconst_null
    //   58: astore 9
    //   60: new 188	java/util/zip/ZipFile
    //   63: dup
    //   64: aload_0
    //   65: invokevirtual 54	android/content/Context:getApplicationInfo	()Landroid/content/pm/ApplicationInfo;
    //   68: getfield 191	android/content/pm/ApplicationInfo:sourceDir	Ljava/lang/String;
    //   71: invokespecial 192	java/util/zip/ZipFile:<init>	(Ljava/lang/String;)V
    //   74: astore 7
    //   76: aload 9
    //   78: astore_1
    //   79: aload 10
    //   81: astore_0
    //   82: aload 7
    //   84: new 160	java/lang/StringBuilder
    //   87: dup
    //   88: invokespecial 161	java/lang/StringBuilder:<init>	()V
    //   91: ldc -62
    //   93: invokevirtual 167	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   96: aload_3
    //   97: invokevirtual 167	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   100: ldc -60
    //   102: invokevirtual 167	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   105: ldc 11
    //   107: invokevirtual 167	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   110: invokevirtual 170	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   113: invokevirtual 200	java/util/zip/ZipFile:getEntry	(Ljava/lang/String;)Ljava/util/zip/ZipEntry;
    //   116: astore 6
    //   118: aload 6
    //   120: ifnonnull +81 -> 201
    //   123: aload 9
    //   125: astore_1
    //   126: aload 10
    //   128: astore_0
    //   129: new 86	java/lang/Exception
    //   132: dup
    //   133: new 160	java/lang/StringBuilder
    //   136: dup
    //   137: invokespecial 161	java/lang/StringBuilder:<init>	()V
    //   140: ldc -54
    //   142: invokevirtual 167	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   145: aload_3
    //   146: invokevirtual 167	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   149: ldc -60
    //   151: invokevirtual 167	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   154: ldc 8
    //   156: invokevirtual 167	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   159: invokevirtual 170	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   162: invokespecial 203	java/lang/Exception:<init>	(Ljava/lang/String;)V
    //   165: athrow
    //   166: astore_3
    //   167: aload 7
    //   169: astore_0
    //   170: aload_1
    //   171: astore_2
    //   172: aload_2
    //   173: astore_1
    //   174: aload_0
    //   175: astore 6
    //   177: ldc 125
    //   179: aload_3
    //   180: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   183: aload_2
    //   184: ifnull +7 -> 191
    //   187: aload_2
    //   188: invokevirtual 208	java/io/InputStream:close	()V
    //   191: aload_0
    //   192: ifnull +7 -> 199
    //   195: aload_0
    //   196: invokevirtual 209	java/util/zip/ZipFile:close	()V
    //   199: iconst_0
    //   200: ireturn
    //   201: aload 9
    //   203: astore_1
    //   204: aload 10
    //   206: astore_0
    //   207: aload 7
    //   209: aload 6
    //   211: invokevirtual 213	java/util/zip/ZipFile:getInputStream	(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;
    //   214: astore_3
    //   215: aload_3
    //   216: astore_1
    //   217: aload_3
    //   218: astore_0
    //   219: new 215	java/io/FileOutputStream
    //   222: dup
    //   223: aload_2
    //   224: invokespecial 218	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   227: astore 6
    //   229: aload_3
    //   230: astore_1
    //   231: aload_3
    //   232: astore_0
    //   233: sipush 4096
    //   236: newarray <illegal type>
    //   238: astore 8
    //   240: aload_3
    //   241: astore_1
    //   242: aload_3
    //   243: astore_0
    //   244: aload_3
    //   245: aload 8
    //   247: invokevirtual 222	java/io/InputStream:read	([B)I
    //   250: istore 4
    //   252: iload 4
    //   254: ifle +52 -> 306
    //   257: aload_3
    //   258: astore_1
    //   259: aload_3
    //   260: astore_0
    //   261: invokestatic 227	java/lang/Thread:yield	()V
    //   264: aload_3
    //   265: astore_1
    //   266: aload_3
    //   267: astore_0
    //   268: aload 6
    //   270: aload 8
    //   272: iconst_0
    //   273: iload 4
    //   275: invokevirtual 233	java/io/OutputStream:write	([BII)V
    //   278: goto -38 -> 240
    //   281: astore_1
    //   282: aload 7
    //   284: astore 6
    //   286: aload_0
    //   287: ifnull +7 -> 294
    //   290: aload_0
    //   291: invokevirtual 208	java/io/InputStream:close	()V
    //   294: aload 6
    //   296: ifnull +8 -> 304
    //   299: aload 6
    //   301: invokevirtual 209	java/util/zip/ZipFile:close	()V
    //   304: aload_1
    //   305: athrow
    //   306: aload_3
    //   307: astore_1
    //   308: aload_3
    //   309: astore_0
    //   310: aload 6
    //   312: invokevirtual 234	java/io/OutputStream:close	()V
    //   315: aload_3
    //   316: astore_1
    //   317: aload_3
    //   318: astore_0
    //   319: aload_2
    //   320: iconst_1
    //   321: iconst_0
    //   322: invokevirtual 238	java/io/File:setReadable	(ZZ)Z
    //   325: pop
    //   326: aload_3
    //   327: astore_1
    //   328: aload_3
    //   329: astore_0
    //   330: aload_2
    //   331: iconst_1
    //   332: iconst_0
    //   333: invokevirtual 241	java/io/File:setExecutable	(ZZ)Z
    //   336: pop
    //   337: aload_3
    //   338: astore_1
    //   339: aload_3
    //   340: astore_0
    //   341: aload_2
    //   342: iconst_1
    //   343: invokevirtual 245	java/io/File:setWritable	(Z)Z
    //   346: pop
    //   347: aload_3
    //   348: astore_1
    //   349: aload_3
    //   350: astore_0
    //   351: aload_2
    //   352: invokevirtual 152	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   355: invokestatic 155	java/lang/System:load	(Ljava/lang/String;)V
    //   358: aload_3
    //   359: astore_1
    //   360: aload_3
    //   361: astore_0
    //   362: iconst_1
    //   363: putstatic 24	ir/eitaa/messenger/NativeLoader:nativeLoaded	Z
    //   366: aload_3
    //   367: ifnull +7 -> 374
    //   370: aload_3
    //   371: invokevirtual 208	java/io/InputStream:close	()V
    //   374: aload 7
    //   376: ifnull +8 -> 384
    //   379: aload 7
    //   381: invokevirtual 209	java/util/zip/ZipFile:close	()V
    //   384: iconst_1
    //   385: ireturn
    //   386: astore_2
    //   387: aload_3
    //   388: astore_1
    //   389: aload_3
    //   390: astore_0
    //   391: ldc 125
    //   393: aload_2
    //   394: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   397: goto -31 -> 366
    //   400: astore_0
    //   401: ldc 125
    //   403: aload_0
    //   404: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   407: goto -33 -> 374
    //   410: astore_0
    //   411: ldc 125
    //   413: aload_0
    //   414: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   417: goto -33 -> 384
    //   420: astore_1
    //   421: ldc 125
    //   423: aload_1
    //   424: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   427: goto -236 -> 191
    //   430: astore_0
    //   431: ldc 125
    //   433: aload_0
    //   434: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   437: goto -238 -> 199
    //   440: astore_0
    //   441: ldc 125
    //   443: aload_0
    //   444: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   447: goto -153 -> 294
    //   450: astore_0
    //   451: ldc 125
    //   453: aload_0
    //   454: invokestatic 139	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   457: goto -153 -> 304
    //   460: astore_2
    //   461: aload_1
    //   462: astore_0
    //   463: aload_2
    //   464: astore_1
    //   465: goto -179 -> 286
    //   468: astore_3
    //   469: aload 11
    //   471: astore_2
    //   472: aload 8
    //   474: astore_0
    //   475: goto -303 -> 172
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	478	0	paramContext	Context
    //   0	478	1	paramFile1	File
    //   0	478	2	paramFile2	File
    //   0	478	3	paramString	String
    //   10	264	4	i	int
    //   7	10	5	j	int
    //   44	267	6	localObject1	Object
    //   74	306	7	localZipFile	java.util.zip.ZipFile
    //   47	426	8	arrayOfByte	byte[]
    //   58	144	9	localObject2	Object
    //   53	152	10	localObject3	Object
    //   50	420	11	localObject4	Object
    // Exception table:
    //   from	to	target	type
    //   0	9	36	java/lang/Exception
    //   19	27	36	java/lang/Exception
    //   82	118	166	java/lang/Exception
    //   129	166	166	java/lang/Exception
    //   207	215	166	java/lang/Exception
    //   219	229	166	java/lang/Exception
    //   233	240	166	java/lang/Exception
    //   244	252	166	java/lang/Exception
    //   261	264	166	java/lang/Exception
    //   268	278	166	java/lang/Exception
    //   310	315	166	java/lang/Exception
    //   319	326	166	java/lang/Exception
    //   330	337	166	java/lang/Exception
    //   341	347	166	java/lang/Exception
    //   351	358	166	java/lang/Exception
    //   362	366	166	java/lang/Exception
    //   391	397	166	java/lang/Exception
    //   82	118	281	finally
    //   129	166	281	finally
    //   207	215	281	finally
    //   219	229	281	finally
    //   233	240	281	finally
    //   244	252	281	finally
    //   261	264	281	finally
    //   268	278	281	finally
    //   310	315	281	finally
    //   319	326	281	finally
    //   330	337	281	finally
    //   341	347	281	finally
    //   351	358	281	finally
    //   362	366	281	finally
    //   391	397	281	finally
    //   351	358	386	java/lang/Error
    //   362	366	386	java/lang/Error
    //   370	374	400	java/lang/Exception
    //   379	384	410	java/lang/Exception
    //   187	191	420	java/lang/Exception
    //   195	199	430	java/lang/Exception
    //   290	294	440	java/lang/Exception
    //   299	304	450	java/lang/Exception
    //   60	76	460	finally
    //   177	183	460	finally
    //   60	76	468	java/lang/Exception
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/NativeLoader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */