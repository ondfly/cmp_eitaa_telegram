package org.telegram.messenger;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.OutputStreamWriter;
import org.telegram.messenger.time.FastDateFormat;

public class FileLog
{
  private static volatile FileLog Instance = null;
  private static final String tag = "tmessages";
  private File currentFile = null;
  private FastDateFormat dateFormat = null;
  private boolean initied;
  private DispatchQueue logQueue = null;
  private File networkFile = null;
  private OutputStreamWriter streamWriter = null;
  
  public FileLog()
  {
    if (!BuildVars.LOGS_ENABLED) {
      return;
    }
    init();
  }
  
  public static void cleanupLogs()
  {
    ensureInitied();
    Object localObject1 = ApplicationLoader.applicationContext.getExternalFilesDir(null);
    if (localObject1 == null) {}
    do
    {
      return;
      localObject1 = new File(((File)localObject1).getAbsolutePath() + "/logs").listFiles();
    } while (localObject1 == null);
    int i = 0;
    label55:
    Object localObject2;
    if (i < localObject1.length)
    {
      localObject2 = localObject1[i];
      if ((getInstance().currentFile == null) || (!((File)localObject2).getAbsolutePath().equals(getInstance().currentFile.getAbsolutePath()))) {
        break label100;
      }
    }
    for (;;)
    {
      i += 1;
      break label55;
      break;
      label100:
      if ((getInstance().networkFile == null) || (!((File)localObject2).getAbsolutePath().equals(getInstance().networkFile.getAbsolutePath()))) {
        ((File)localObject2).delete();
      }
    }
  }
  
  public static void d(String paramString)
  {
    if (!BuildVars.LOGS_ENABLED) {}
    do
    {
      return;
      ensureInitied();
      Log.d("tmessages", paramString);
    } while (getInstance().streamWriter == null);
    getInstance().logQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " D/tmessages: " + this.val$message + "\n");
          FileLog.getInstance().streamWriter.flush();
          return;
        }
        catch (Exception localException)
        {
          localException.printStackTrace();
        }
      }
    });
  }
  
  public static void e(String paramString)
  {
    if (!BuildVars.LOGS_ENABLED) {}
    do
    {
      return;
      ensureInitied();
      Log.e("tmessages", paramString);
    } while (getInstance().streamWriter == null);
    getInstance().logQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + this.val$message + "\n");
          FileLog.getInstance().streamWriter.flush();
          return;
        }
        catch (Exception localException)
        {
          localException.printStackTrace();
        }
      }
    });
  }
  
  public static void e(String paramString, final Throwable paramThrowable)
  {
    if (!BuildVars.LOGS_ENABLED) {}
    do
    {
      return;
      ensureInitied();
      Log.e("tmessages", paramString, paramThrowable);
    } while (getInstance().streamWriter == null);
    getInstance().logQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + this.val$message + "\n");
          FileLog.getInstance().streamWriter.write(paramThrowable.toString());
          FileLog.getInstance().streamWriter.flush();
          return;
        }
        catch (Exception localException)
        {
          localException.printStackTrace();
        }
      }
    });
  }
  
  public static void e(Throwable paramThrowable)
  {
    if (!BuildVars.LOGS_ENABLED) {
      return;
    }
    ensureInitied();
    paramThrowable.printStackTrace();
    if (getInstance().streamWriter != null)
    {
      getInstance().logQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          try
          {
            FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + this.val$e + "\n");
            StackTraceElement[] arrayOfStackTraceElement = this.val$e.getStackTrace();
            int i = 0;
            while (i < arrayOfStackTraceElement.length)
            {
              FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + arrayOfStackTraceElement[i] + "\n");
              i += 1;
            }
            FileLog.getInstance().streamWriter.flush();
            return;
          }
          catch (Exception localException)
          {
            localException.printStackTrace();
          }
        }
      });
      return;
    }
    paramThrowable.printStackTrace();
  }
  
  public static void ensureInitied()
  {
    getInstance().init();
  }
  
  public static FileLog getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          FileLog localFileLog2 = Instance;
          localObject1 = localFileLog2;
          if (localFileLog2 == null) {
            localObject1 = new FileLog();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (FileLog)localObject1;
          return (FileLog)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localFileLog1;
  }
  
  public static String getNetworkLogPath()
  {
    if (!BuildVars.LOGS_ENABLED) {
      return "";
    }
    try
    {
      Object localObject = ApplicationLoader.applicationContext.getExternalFilesDir(null);
      if (localObject == null) {
        return "";
      }
      localObject = new File(((File)localObject).getAbsolutePath() + "/logs");
      ((File)localObject).mkdirs();
      getInstance().networkFile = new File((File)localObject, getInstance().dateFormat.format(System.currentTimeMillis()) + "_net.txt");
      localObject = getInstance().networkFile.getAbsolutePath();
      return (String)localObject;
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
    return "";
  }
  
  public static void w(String paramString)
  {
    if (!BuildVars.LOGS_ENABLED) {}
    do
    {
      return;
      ensureInitied();
      Log.w("tmessages", paramString);
    } while (getInstance().streamWriter == null);
    getInstance().logQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        try
        {
          FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " W/tmessages: " + this.val$message + "\n");
          FileLog.getInstance().streamWriter.flush();
          return;
        }
        catch (Exception localException)
        {
          localException.printStackTrace();
        }
      }
    });
  }
  
  /* Error */
  public void init()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 179	org/telegram/messenger/FileLog:initied	Z
    //   4: ifeq +4 -> 8
    //   7: return
    //   8: aload_0
    //   9: ldc -75
    //   11: getstatic 187	java/util/Locale:US	Ljava/util/Locale;
    //   14: invokestatic 190	org/telegram/messenger/time/FastDateFormat:getInstance	(Ljava/lang/String;Ljava/util/Locale;)Lorg/telegram/messenger/time/FastDateFormat;
    //   17: putfield 43	org/telegram/messenger/FileLog:dateFormat	Lorg/telegram/messenger/time/FastDateFormat;
    //   20: getstatic 71	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   23: aconst_null
    //   24: invokevirtual 77	android/content/Context:getExternalFilesDir	(Ljava/lang/String;)Ljava/io/File;
    //   27: astore_1
    //   28: aload_1
    //   29: ifnull -22 -> 7
    //   32: new 79	java/io/File
    //   35: dup
    //   36: new 81	java/lang/StringBuilder
    //   39: dup
    //   40: invokespecial 82	java/lang/StringBuilder:<init>	()V
    //   43: aload_1
    //   44: invokevirtual 86	java/io/File:getAbsolutePath	()Ljava/lang/String;
    //   47: invokevirtual 90	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   50: ldc 92
    //   52: invokevirtual 90	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   55: invokevirtual 95	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   58: invokespecial 98	java/io/File:<init>	(Ljava/lang/String;)V
    //   61: astore_1
    //   62: aload_1
    //   63: invokevirtual 154	java/io/File:mkdirs	()Z
    //   66: pop
    //   67: aload_0
    //   68: new 79	java/io/File
    //   71: dup
    //   72: aload_1
    //   73: new 81	java/lang/StringBuilder
    //   76: dup
    //   77: invokespecial 82	java/lang/StringBuilder:<init>	()V
    //   80: aload_0
    //   81: getfield 43	org/telegram/messenger/FileLog:dateFormat	Lorg/telegram/messenger/time/FastDateFormat;
    //   84: invokestatic 160	java/lang/System:currentTimeMillis	()J
    //   87: invokevirtual 166	org/telegram/messenger/time/FastDateFormat:format	(J)Ljava/lang/String;
    //   90: invokevirtual 90	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   93: ldc -64
    //   95: invokevirtual 90	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   98: invokevirtual 95	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   101: invokespecial 171	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   104: putfield 47	org/telegram/messenger/FileLog:currentFile	Ljava/io/File;
    //   107: aload_0
    //   108: new 125	org/telegram/messenger/DispatchQueue
    //   111: dup
    //   112: ldc -63
    //   114: invokespecial 194	org/telegram/messenger/DispatchQueue:<init>	(Ljava/lang/String;)V
    //   117: putfield 45	org/telegram/messenger/FileLog:logQueue	Lorg/telegram/messenger/DispatchQueue;
    //   120: aload_0
    //   121: getfield 47	org/telegram/messenger/FileLog:currentFile	Ljava/io/File;
    //   124: invokevirtual 197	java/io/File:createNewFile	()Z
    //   127: pop
    //   128: aload_0
    //   129: new 199	java/io/OutputStreamWriter
    //   132: dup
    //   133: new 201	java/io/FileOutputStream
    //   136: dup
    //   137: aload_0
    //   138: getfield 47	org/telegram/messenger/FileLog:currentFile	Ljava/io/File;
    //   141: invokespecial 204	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   144: invokespecial 207	java/io/OutputStreamWriter:<init>	(Ljava/io/OutputStream;)V
    //   147: putfield 41	org/telegram/messenger/FileLog:streamWriter	Ljava/io/OutputStreamWriter;
    //   150: aload_0
    //   151: getfield 41	org/telegram/messenger/FileLog:streamWriter	Ljava/io/OutputStreamWriter;
    //   154: new 81	java/lang/StringBuilder
    //   157: dup
    //   158: invokespecial 82	java/lang/StringBuilder:<init>	()V
    //   161: ldc -47
    //   163: invokevirtual 90	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   166: aload_0
    //   167: getfield 43	org/telegram/messenger/FileLog:dateFormat	Lorg/telegram/messenger/time/FastDateFormat;
    //   170: invokestatic 160	java/lang/System:currentTimeMillis	()J
    //   173: invokevirtual 166	org/telegram/messenger/time/FastDateFormat:format	(J)Ljava/lang/String;
    //   176: invokevirtual 90	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   179: ldc -45
    //   181: invokevirtual 90	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   184: invokevirtual 95	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   187: invokevirtual 214	java/io/OutputStreamWriter:write	(Ljava/lang/String;)V
    //   190: aload_0
    //   191: getfield 41	org/telegram/messenger/FileLog:streamWriter	Ljava/io/OutputStreamWriter;
    //   194: invokevirtual 217	java/io/OutputStreamWriter:flush	()V
    //   197: aload_0
    //   198: iconst_1
    //   199: putfield 179	org/telegram/messenger/FileLog:initied	Z
    //   202: return
    //   203: astore_1
    //   204: aload_1
    //   205: invokevirtual 218	java/lang/Exception:printStackTrace	()V
    //   208: goto -101 -> 107
    //   211: astore_1
    //   212: aload_1
    //   213: invokevirtual 218	java/lang/Exception:printStackTrace	()V
    //   216: goto -19 -> 197
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	219	0	this	FileLog
    //   27	46	1	localFile	File
    //   203	2	1	localException1	Exception
    //   211	2	1	localException2	Exception
    // Exception table:
    //   from	to	target	type
    //   20	28	203	java/lang/Exception
    //   32	107	203	java/lang/Exception
    //   107	197	211	java/lang/Exception
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/FileLog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */