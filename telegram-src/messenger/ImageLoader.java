package org.telegram.messenger;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseArray;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentEncrypted;
import org.telegram.tgnet.TLRPC.TL_fileEncryptedLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_webDocument;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.Components.AnimatedFileDrawable;

public class ImageLoader
{
  private static volatile ImageLoader Instance = null;
  private static byte[] bytes;
  private static byte[] bytesThumb;
  private static byte[] header = new byte[12];
  private static byte[] headerThumb = new byte[12];
  private HashMap<String, Integer> bitmapUseCounts = new HashMap();
  private DispatchQueue cacheOutQueue = new DispatchQueue("cacheOutQueue");
  private DispatchQueue cacheThumbOutQueue = new DispatchQueue("cacheThumbOutQueue");
  private int currentHttpFileLoadTasksCount = 0;
  private int currentHttpTasksCount = 0;
  private ConcurrentHashMap<String, Float> fileProgresses = new ConcurrentHashMap();
  private HashMap<String, Integer> forceLoadingImages = new HashMap();
  private LinkedList<HttpFileTask> httpFileLoadTasks = new LinkedList();
  private HashMap<String, HttpFileTask> httpFileLoadTasksByKeys = new HashMap();
  private LinkedList<HttpImageTask> httpTasks = new LinkedList();
  private String ignoreRemoval = null;
  private DispatchQueue imageLoadQueue = new DispatchQueue("imageLoadQueue");
  private HashMap<String, CacheImage> imageLoadingByKeys = new HashMap();
  private SparseArray<CacheImage> imageLoadingByTag = new SparseArray();
  private HashMap<String, CacheImage> imageLoadingByUrl = new HashMap();
  private volatile long lastCacheOutTime = 0L;
  private int lastImageNum = 0;
  private long lastProgressUpdateTime = 0L;
  private LruCache memCache;
  private HashMap<String, Runnable> retryHttpsTasks = new HashMap();
  private File telegramPath = null;
  private HashMap<String, ThumbGenerateTask> thumbGenerateTasks = new HashMap();
  private DispatchQueue thumbGeneratingQueue = new DispatchQueue("thumbGeneratingQueue");
  private HashMap<String, ThumbGenerateInfo> waitingForQualityThumb = new HashMap();
  private SparseArray<String> waitingForQualityThumbByTag = new SparseArray();
  
  public ImageLoader()
  {
    this.thumbGeneratingQueue.setPriority(1);
    this.memCache = new LruCache(Math.min(15, ((ActivityManager)ApplicationLoader.applicationContext.getSystemService("activity")).getMemoryClass() / 7) * 1024 * 1024)
    {
      protected void entryRemoved(boolean paramAnonymousBoolean, String paramAnonymousString, BitmapDrawable paramAnonymousBitmapDrawable1, BitmapDrawable paramAnonymousBitmapDrawable2)
      {
        if ((ImageLoader.this.ignoreRemoval != null) && (paramAnonymousString != null) && (ImageLoader.this.ignoreRemoval.equals(paramAnonymousString))) {}
        do
        {
          do
          {
            return;
            paramAnonymousString = (Integer)ImageLoader.this.bitmapUseCounts.get(paramAnonymousString);
          } while ((paramAnonymousString != null) && (paramAnonymousString.intValue() != 0));
          paramAnonymousString = paramAnonymousBitmapDrawable1.getBitmap();
        } while (paramAnonymousString.isRecycled());
        paramAnonymousString.recycle();
      }
      
      protected int sizeOf(String paramAnonymousString, BitmapDrawable paramAnonymousBitmapDrawable)
      {
        return paramAnonymousBitmapDrawable.getBitmap().getByteCount();
      }
    };
    Object localObject1 = new SparseArray();
    Object localObject2 = AndroidUtilities.getCacheDir();
    if (!((File)localObject2).isDirectory()) {}
    try
    {
      ((File)localObject2).mkdirs();
    }
    catch (Exception localException1)
    {
      try
      {
        for (;;)
        {
          new File((File)localObject2, ".nomedia").createNewFile();
          ((SparseArray)localObject1).put(4, localObject2);
          final int i = 0;
          while (i < 3)
          {
            FileLoader.getInstance(i).setDelegate(new FileLoader.FileLoaderDelegate()
            {
              public void fileDidFailedLoad(final String paramAnonymousString, final int paramAnonymousInt)
              {
                ImageLoader.this.fileProgresses.remove(paramAnonymousString);
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    ImageLoader.this.fileDidFailedLoad(paramAnonymousString, paramAnonymousInt);
                    NotificationCenter.getInstance(ImageLoader.2.this.val$currentAccount).postNotificationName(NotificationCenter.FileDidFailedLoad, new Object[] { paramAnonymousString, Integer.valueOf(paramAnonymousInt) });
                  }
                });
              }
              
              public void fileDidFailedUpload(final String paramAnonymousString, final boolean paramAnonymousBoolean)
              {
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        NotificationCenter.getInstance(ImageLoader.2.this.val$currentAccount).postNotificationName(NotificationCenter.FileDidFailUpload, new Object[] { ImageLoader.2.3.this.val$location, Boolean.valueOf(ImageLoader.2.3.this.val$isEncrypted) });
                      }
                    });
                    ImageLoader.this.fileProgresses.remove(paramAnonymousString);
                  }
                });
              }
              
              public void fileDidLoaded(final String paramAnonymousString, final File paramAnonymousFile, final int paramAnonymousInt)
              {
                ImageLoader.this.fileProgresses.remove(paramAnonymousString);
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    if ((SharedConfig.saveToGallery) && (ImageLoader.this.telegramPath != null) && (paramAnonymousFile != null) && ((paramAnonymousString.endsWith(".mp4")) || (paramAnonymousString.endsWith(".jpg"))) && (paramAnonymousFile.toString().startsWith(ImageLoader.this.telegramPath.toString()))) {
                      AndroidUtilities.addMediaToGallery(paramAnonymousFile.toString());
                    }
                    NotificationCenter.getInstance(ImageLoader.2.this.val$currentAccount).postNotificationName(NotificationCenter.FileDidLoaded, new Object[] { paramAnonymousString });
                    ImageLoader.this.fileDidLoaded(paramAnonymousString, paramAnonymousFile, paramAnonymousInt);
                  }
                });
              }
              
              public void fileDidUploaded(final String paramAnonymousString, final TLRPC.InputFile paramAnonymousInputFile, final TLRPC.InputEncryptedFile paramAnonymousInputEncryptedFile, final byte[] paramAnonymousArrayOfByte1, final byte[] paramAnonymousArrayOfByte2, final long paramAnonymousLong)
              {
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        NotificationCenter.getInstance(ImageLoader.2.this.val$currentAccount).postNotificationName(NotificationCenter.FileDidUpload, new Object[] { ImageLoader.2.2.this.val$location, ImageLoader.2.2.this.val$inputFile, ImageLoader.2.2.this.val$inputEncryptedFile, ImageLoader.2.2.this.val$key, ImageLoader.2.2.this.val$iv, Long.valueOf(ImageLoader.2.2.this.val$totalFileSize) });
                      }
                    });
                    ImageLoader.this.fileProgresses.remove(paramAnonymousString);
                  }
                });
              }
              
              public void fileLoadProgressChanged(final String paramAnonymousString, final float paramAnonymousFloat)
              {
                ImageLoader.this.fileProgresses.put(paramAnonymousString, Float.valueOf(paramAnonymousFloat));
                long l = System.currentTimeMillis();
                if ((ImageLoader.this.lastProgressUpdateTime == 0L) || (ImageLoader.this.lastProgressUpdateTime < l - 500L))
                {
                  ImageLoader.access$2802(ImageLoader.this, l);
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      NotificationCenter.getInstance(ImageLoader.2.this.val$currentAccount).postNotificationName(NotificationCenter.FileLoadProgressChanged, new Object[] { paramAnonymousString, Float.valueOf(paramAnonymousFloat) });
                    }
                  });
                }
              }
              
              public void fileUploadProgressChanged(final String paramAnonymousString, final float paramAnonymousFloat, final boolean paramAnonymousBoolean)
              {
                ImageLoader.this.fileProgresses.put(paramAnonymousString, Float.valueOf(paramAnonymousFloat));
                long l = System.currentTimeMillis();
                if ((ImageLoader.this.lastProgressUpdateTime == 0L) || (ImageLoader.this.lastProgressUpdateTime < l - 500L))
                {
                  ImageLoader.access$2802(ImageLoader.this, l);
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      NotificationCenter.getInstance(ImageLoader.2.this.val$currentAccount).postNotificationName(NotificationCenter.FileUploadProgressChanged, new Object[] { paramAnonymousString, Float.valueOf(paramAnonymousFloat), Boolean.valueOf(paramAnonymousBoolean) });
                    }
                  });
                }
              }
            });
            i += 1;
          }
          localException1 = localException1;
          FileLog.e(localException1);
        }
      }
      catch (Exception localException2)
      {
        for (;;)
        {
          FileLog.e(localException2);
        }
        FileLoader.setMediaDirs((SparseArray)localObject1);
        localObject1 = new BroadcastReceiver()
        {
          public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("file system changed");
            }
            paramAnonymousContext = new Runnable()
            {
              public void run()
              {
                ImageLoader.this.checkMediaPaths();
              }
            };
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(paramAnonymousIntent.getAction()))
            {
              AndroidUtilities.runOnUIThread(paramAnonymousContext, 1000L);
              return;
            }
            paramAnonymousContext.run();
          }
        };
        localObject2 = new IntentFilter();
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_CHECKING");
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_EJECT");
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_MOUNTED");
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_NOFS");
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_REMOVED");
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_SHARED");
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_UNMOUNTABLE");
        ((IntentFilter)localObject2).addAction("android.intent.action.MEDIA_UNMOUNTED");
        ((IntentFilter)localObject2).addDataScheme("file");
      }
    }
    try
    {
      ApplicationLoader.applicationContext.registerReceiver((BroadcastReceiver)localObject1, (IntentFilter)localObject2);
      checkMediaPaths();
      return;
    }
    catch (Throwable localThrowable)
    {
      for (;;) {}
    }
  }
  
  /* Error */
  private boolean canMoveFiles(File paramFile1, File paramFile2, int paramInt)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 8
    //   3: aconst_null
    //   4: astore 9
    //   6: aconst_null
    //   7: astore 6
    //   9: aconst_null
    //   10: astore 5
    //   12: iload_3
    //   13: ifne +143 -> 156
    //   16: aload 8
    //   18: astore 7
    //   20: new 279	java/io/File
    //   23: dup
    //   24: aload_1
    //   25: ldc_w 456
    //   28: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   31: astore 5
    //   33: aload 8
    //   35: astore 7
    //   37: aload 5
    //   39: astore_1
    //   40: new 279	java/io/File
    //   43: dup
    //   44: aload_2
    //   45: ldc_w 458
    //   48: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   51: astore_1
    //   52: aload 5
    //   54: astore 6
    //   56: aload_1
    //   57: astore 5
    //   59: aload 8
    //   61: astore 7
    //   63: sipush 1024
    //   66: newarray <illegal type>
    //   68: astore_2
    //   69: aload 8
    //   71: astore 7
    //   73: aload 6
    //   75: invokevirtual 294	java/io/File:createNewFile	()Z
    //   78: pop
    //   79: aload 8
    //   81: astore 7
    //   83: new 460	java/io/RandomAccessFile
    //   86: dup
    //   87: aload 6
    //   89: ldc_w 462
    //   92: invokespecial 463	java/io/RandomAccessFile:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   95: astore_1
    //   96: aload_1
    //   97: aload_2
    //   98: invokevirtual 467	java/io/RandomAccessFile:write	([B)V
    //   101: aload_1
    //   102: invokevirtual 470	java/io/RandomAccessFile:close	()V
    //   105: aconst_null
    //   106: astore_1
    //   107: aload_1
    //   108: astore 7
    //   110: aload 6
    //   112: aload 5
    //   114: invokevirtual 474	java/io/File:renameTo	(Ljava/io/File;)Z
    //   117: istore 4
    //   119: aload_1
    //   120: astore 7
    //   122: aload 6
    //   124: invokevirtual 477	java/io/File:delete	()Z
    //   127: pop
    //   128: aload_1
    //   129: astore 7
    //   131: aload 5
    //   133: invokevirtual 477	java/io/File:delete	()Z
    //   136: pop
    //   137: iload 4
    //   139: ifeq +177 -> 316
    //   142: iconst_0
    //   143: ifeq +11 -> 154
    //   146: new 479	java/lang/NullPointerException
    //   149: dup
    //   150: invokespecial 480	java/lang/NullPointerException:<init>	()V
    //   153: athrow
    //   154: iconst_1
    //   155: ireturn
    //   156: iload_3
    //   157: iconst_3
    //   158: if_icmpne +49 -> 207
    //   161: aload 8
    //   163: astore 7
    //   165: new 279	java/io/File
    //   168: dup
    //   169: aload_1
    //   170: ldc_w 482
    //   173: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   176: astore 5
    //   178: aload 8
    //   180: astore 7
    //   182: aload 5
    //   184: astore_1
    //   185: new 279	java/io/File
    //   188: dup
    //   189: aload_2
    //   190: ldc_w 484
    //   193: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   196: astore_1
    //   197: aload 5
    //   199: astore 6
    //   201: aload_1
    //   202: astore 5
    //   204: goto -145 -> 59
    //   207: iload_3
    //   208: iconst_1
    //   209: if_icmpne +49 -> 258
    //   212: aload 8
    //   214: astore 7
    //   216: new 279	java/io/File
    //   219: dup
    //   220: aload_1
    //   221: ldc_w 486
    //   224: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   227: astore 5
    //   229: aload 8
    //   231: astore 7
    //   233: aload 5
    //   235: astore_1
    //   236: new 279	java/io/File
    //   239: dup
    //   240: aload_2
    //   241: ldc_w 488
    //   244: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   247: astore_1
    //   248: aload 5
    //   250: astore 6
    //   252: aload_1
    //   253: astore 5
    //   255: goto -196 -> 59
    //   258: iload_3
    //   259: iconst_2
    //   260: if_icmpne -201 -> 59
    //   263: aload 8
    //   265: astore 7
    //   267: new 279	java/io/File
    //   270: dup
    //   271: aload_1
    //   272: ldc_w 490
    //   275: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   278: astore 5
    //   280: aload 8
    //   282: astore 7
    //   284: aload 5
    //   286: astore_1
    //   287: new 279	java/io/File
    //   290: dup
    //   291: aload_2
    //   292: ldc_w 492
    //   295: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   298: astore_1
    //   299: aload 5
    //   301: astore 6
    //   303: aload_1
    //   304: astore 5
    //   306: goto -247 -> 59
    //   309: astore_1
    //   310: aload_1
    //   311: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   314: iconst_1
    //   315: ireturn
    //   316: iconst_0
    //   317: ifeq +11 -> 328
    //   320: new 479	java/lang/NullPointerException
    //   323: dup
    //   324: invokespecial 480	java/lang/NullPointerException:<init>	()V
    //   327: athrow
    //   328: iconst_0
    //   329: ireturn
    //   330: astore_1
    //   331: aload_1
    //   332: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   335: goto -7 -> 328
    //   338: aconst_null
    //   339: astore_1
    //   340: astore_2
    //   341: aload_1
    //   342: astore 7
    //   344: aload_2
    //   345: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   348: aload_1
    //   349: ifnull -21 -> 328
    //   352: aload_1
    //   353: invokevirtual 470	java/io/RandomAccessFile:close	()V
    //   356: goto -28 -> 328
    //   359: astore_1
    //   360: aload_1
    //   361: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   364: goto -36 -> 328
    //   367: astore_1
    //   368: aload 7
    //   370: ifnull +8 -> 378
    //   373: aload 7
    //   375: invokevirtual 470	java/io/RandomAccessFile:close	()V
    //   378: aload_1
    //   379: athrow
    //   380: astore_2
    //   381: aload_2
    //   382: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   385: goto -7 -> 378
    //   388: astore_2
    //   389: aload_1
    //   390: astore 7
    //   392: aload_2
    //   393: astore_1
    //   394: goto -26 -> 368
    //   397: astore_2
    //   398: aload 9
    //   400: astore_1
    //   401: goto -60 -> 341
    //   404: astore_2
    //   405: goto -64 -> 341
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	408	0	this	ImageLoader
    //   0	408	1	paramFile1	File
    //   0	408	2	paramFile2	File
    //   0	408	3	paramInt	int
    //   117	21	4	bool	boolean
    //   10	295	5	localFile	File
    //   7	295	6	localObject1	Object
    //   18	373	7	localObject2	Object
    //   1	280	8	localObject3	Object
    //   4	395	9	localObject4	Object
    // Exception table:
    //   from	to	target	type
    //   146	154	309	java/lang/Exception
    //   320	328	330	java/lang/Exception
    //   20	33	338	java/lang/Exception
    //   63	69	338	java/lang/Exception
    //   73	79	338	java/lang/Exception
    //   83	96	338	java/lang/Exception
    //   110	119	338	java/lang/Exception
    //   122	128	338	java/lang/Exception
    //   131	137	338	java/lang/Exception
    //   165	178	338	java/lang/Exception
    //   216	229	338	java/lang/Exception
    //   267	280	338	java/lang/Exception
    //   352	356	359	java/lang/Exception
    //   20	33	367	finally
    //   40	52	367	finally
    //   63	69	367	finally
    //   73	79	367	finally
    //   83	96	367	finally
    //   110	119	367	finally
    //   122	128	367	finally
    //   131	137	367	finally
    //   165	178	367	finally
    //   185	197	367	finally
    //   216	229	367	finally
    //   236	248	367	finally
    //   267	280	367	finally
    //   287	299	367	finally
    //   344	348	367	finally
    //   373	378	380	java/lang/Exception
    //   96	105	388	finally
    //   40	52	397	java/lang/Exception
    //   185	197	397	java/lang/Exception
    //   236	248	397	java/lang/Exception
    //   287	299	397	java/lang/Exception
    //   96	105	404	java/lang/Exception
  }
  
  private void createLoadOperationForImageReceiver(final ImageReceiver paramImageReceiver, final String paramString1, final String paramString2, final String paramString3, final TLObject paramTLObject, final String paramString4, final String paramString5, final int paramInt1, final int paramInt2, final int paramInt3)
  {
    if ((paramImageReceiver == null) || (paramString2 == null) || (paramString1 == null)) {
      return;
    }
    final int j;
    final int i;
    if (paramInt3 != 0)
    {
      bool1 = true;
      j = paramImageReceiver.getTag(bool1);
      i = j;
      if (j == 0)
      {
        j = this.lastImageNum;
        if (paramInt3 == 0) {
          break label166;
        }
      }
    }
    label166:
    for (final boolean bool1 = true;; bool1 = false)
    {
      paramImageReceiver.setTag(j, bool1);
      this.lastImageNum += 1;
      i = j;
      if (this.lastImageNum == Integer.MAX_VALUE)
      {
        this.lastImageNum = 0;
        i = j;
      }
      bool1 = paramImageReceiver.isNeedsQualityThumb();
      final MessageObject localMessageObject = paramImageReceiver.getParentMessageObject();
      final boolean bool2 = paramImageReceiver.isShouldGenerateQualityThumb();
      j = paramImageReceiver.getcurrentAccount();
      this.imageLoadQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          int i = 0;
          int j = 0;
          Object localObject1;
          Object localObject3;
          Object localObject4;
          String str1;
          boolean bool;
          if (paramInt3 != 2)
          {
            localObject1 = (ImageLoader.CacheImage)ImageLoader.this.imageLoadingByUrl.get(paramString2);
            localObject2 = (ImageLoader.CacheImage)ImageLoader.this.imageLoadingByKeys.get(paramString1);
            localObject3 = (ImageLoader.CacheImage)ImageLoader.this.imageLoadingByTag.get(i);
            i = j;
            if (localObject3 != null)
            {
              if (localObject3 != localObject2) {
                break label717;
              }
              i = 1;
            }
            j = i;
            if (i == 0)
            {
              j = i;
              if (localObject2 != null)
              {
                localObject3 = paramImageReceiver;
                localObject4 = paramString1;
                str1 = paramString5;
                if (paramInt3 == 0) {
                  break label795;
                }
                bool = true;
                label126:
                ((ImageLoader.CacheImage)localObject2).addImageReceiver((ImageReceiver)localObject3, (String)localObject4, str1, bool);
                j = 1;
              }
            }
            i = j;
            if (j == 0)
            {
              i = j;
              if (localObject1 != null)
              {
                localObject2 = paramImageReceiver;
                localObject3 = paramString1;
                localObject4 = paramString5;
                if (paramInt3 == 0) {
                  break label801;
                }
                bool = true;
                label182:
                ((ImageLoader.CacheImage)localObject1).addImageReceiver((ImageReceiver)localObject2, (String)localObject3, (String)localObject4, bool);
                i = 1;
              }
            }
          }
          int m;
          int n;
          int k;
          if (i == 0)
          {
            m = 0;
            localObject3 = null;
            localObject1 = null;
            n = 0;
            j = 0;
            if (paramString4 == null) {
              break label895;
            }
            localObject2 = localObject1;
            i = j;
            k = m;
            if (!paramString4.startsWith("http"))
            {
              m = 1;
              if (!paramString4.startsWith("thumb://")) {
                break label807;
              }
              n = paramString4.indexOf(":", 8);
              localObject2 = localObject1;
              i = j;
              k = m;
              if (n >= 0)
              {
                localObject2 = new File(paramString4.substring(n + 1));
                k = m;
                i = j;
              }
            }
            label310:
            if (paramInt3 != 2)
            {
              if ((!(paramTLObject instanceof TLRPC.TL_documentEncrypted)) && (!(paramTLObject instanceof TLRPC.TL_fileEncryptedLocation))) {
                break label1283;
              }
              j = 1;
              label340:
              localObject3 = new ImageLoader.CacheImage(ImageLoader.this, null);
              if ((paramString4 == null) || (paramString4.startsWith("vthumb")) || (paramString4.startsWith("thumb"))) {
                break label1288;
              }
              localObject1 = ImageLoader.getHttpUrlExtension(paramString4, "jpg");
              if ((((String)localObject1).equals("mp4")) || (((String)localObject1).equals("gif"))) {
                ((ImageLoader.CacheImage)localObject3).animatedFile = true;
              }
              label422:
              localObject1 = localObject2;
              m = i;
              if (localObject2 == null)
              {
                if ((paramInt2 == 0) && (paramInt1 > 0) && (paramString4 == null) && (j == 0)) {
                  break label1409;
                }
                localObject1 = new File(FileLoader.getDirectory(4), paramString2);
                if (!((File)localObject1).exists()) {
                  break label1356;
                }
                m = 1;
              }
              label487:
              if (paramInt3 == 0) {
                break label1534;
              }
              bool = true;
              label497:
              ((ImageLoader.CacheImage)localObject3).selfThumb = bool;
              ((ImageLoader.CacheImage)localObject3).key = paramString1;
              ((ImageLoader.CacheImage)localObject3).filter = paramString5;
              ((ImageLoader.CacheImage)localObject3).httpUrl = paramString4;
              ((ImageLoader.CacheImage)localObject3).ext = paramString3;
              ((ImageLoader.CacheImage)localObject3).currentAccount = j;
              if (paramInt2 == 2) {
                ((ImageLoader.CacheImage)localObject3).encryptionKeyPath = new File(FileLoader.getInternalCacheDir(), paramString2 + ".enc.key");
              }
              localObject2 = paramImageReceiver;
              localObject4 = paramString1;
              str1 = paramString5;
              if (paramInt3 == 0) {
                break label1540;
              }
              bool = true;
              label622:
              ((ImageLoader.CacheImage)localObject3).addImageReceiver((ImageReceiver)localObject2, (String)localObject4, str1, bool);
              if ((k == 0) && (m == 0) && (!((File)localObject1).exists())) {
                break label1562;
              }
              ((ImageLoader.CacheImage)localObject3).finalFilePath = ((File)localObject1);
              ((ImageLoader.CacheImage)localObject3).cacheTask = new ImageLoader.CacheOutTask(ImageLoader.this, (ImageLoader.CacheImage)localObject3);
              ImageLoader.this.imageLoadingByKeys.put(paramString1, localObject3);
              if (paramInt3 == 0) {
                break label1546;
              }
              ImageLoader.this.cacheThumbOutQueue.postRunnable(((ImageLoader.CacheImage)localObject3).cacheTask);
            }
          }
          for (;;)
          {
            return;
            label717:
            if (localObject3 == localObject1)
            {
              String str2;
              if (localObject2 == null)
              {
                localObject4 = paramImageReceiver;
                str1 = paramString1;
                str2 = paramString5;
                if (paramInt3 == 0) {
                  break label775;
                }
              }
              label775:
              for (bool = true;; bool = false)
              {
                ((ImageLoader.CacheImage)localObject3).replaceImageReceiver((ImageReceiver)localObject4, str1, str2, bool);
                i = 1;
                break;
              }
            }
            ((ImageLoader.CacheImage)localObject3).removeImageReceiver(paramImageReceiver);
            i = j;
            break;
            label795:
            bool = false;
            break label126;
            label801:
            bool = false;
            break label182;
            label807:
            if (paramString4.startsWith("vthumb://"))
            {
              n = paramString4.indexOf(":", 9);
              localObject2 = localObject1;
              i = j;
              k = m;
              if (n < 0) {
                break label310;
              }
              localObject2 = new File(paramString4.substring(n + 1));
              i = j;
              k = m;
              break label310;
            }
            localObject2 = new File(paramString4);
            i = j;
            k = m;
            break label310;
            label895:
            localObject2 = localObject1;
            i = j;
            k = m;
            if (paramInt3 == 0) {
              break label310;
            }
            localObject1 = localObject3;
            j = n;
            if (bool1)
            {
              localObject1 = new File(FileLoader.getDirectory(4), "q_" + paramString2);
              if (((File)localObject1).exists()) {
                break label1278;
              }
              localObject1 = null;
            }
            label1278:
            for (j = n;; j = 1)
            {
              localObject2 = localObject1;
              i = j;
              k = m;
              if (localMessageObject == null) {
                break;
              }
              localObject3 = null;
              localObject2 = localObject3;
              if (localMessageObject.messageOwner.attachPath != null)
              {
                localObject2 = localObject3;
                if (localMessageObject.messageOwner.attachPath.length() > 0)
                {
                  localObject3 = new File(localMessageObject.messageOwner.attachPath);
                  localObject2 = localObject3;
                  if (!((File)localObject3).exists()) {
                    localObject2 = null;
                  }
                }
              }
              localObject3 = localObject2;
              if (localObject2 == null) {
                localObject3 = FileLoader.getPathToMessage(localMessageObject.messageOwner);
              }
              if ((bool1) && (localObject1 == null))
              {
                str1 = localMessageObject.getFileName();
                localObject4 = (ImageLoader.ThumbGenerateInfo)ImageLoader.this.waitingForQualityThumb.get(str1);
                localObject2 = localObject4;
                if (localObject4 == null)
                {
                  localObject2 = new ImageLoader.ThumbGenerateInfo(ImageLoader.this, null);
                  ImageLoader.ThumbGenerateInfo.access$3602((ImageLoader.ThumbGenerateInfo)localObject2, (TLRPC.FileLocation)paramTLObject);
                  ImageLoader.ThumbGenerateInfo.access$3702((ImageLoader.ThumbGenerateInfo)localObject2, paramString5);
                  ImageLoader.this.waitingForQualityThumb.put(str1, localObject2);
                }
                ImageLoader.ThumbGenerateInfo.access$3108((ImageLoader.ThumbGenerateInfo)localObject2);
                ImageLoader.this.waitingForQualityThumbByTag.put(i, str1);
              }
              localObject2 = localObject1;
              i = j;
              k = m;
              if (!((File)localObject3).exists()) {
                break;
              }
              localObject2 = localObject1;
              i = j;
              k = m;
              if (!bool2) {
                break;
              }
              ImageLoader.this.generateThumb(localMessageObject.getFileType(), (File)localObject3, (TLRPC.FileLocation)paramTLObject, paramString5);
              localObject2 = localObject1;
              i = j;
              k = m;
              break;
            }
            label1283:
            j = 0;
            break label340;
            label1288:
            if (((!(paramTLObject instanceof TLRPC.TL_webDocument)) || (!MessageObject.isGifDocument((TLRPC.TL_webDocument)paramTLObject))) && ((!(paramTLObject instanceof TLRPC.Document)) || ((!MessageObject.isGifDocument((TLRPC.Document)paramTLObject)) && (!MessageObject.isRoundVideoDocument((TLRPC.Document)paramTLObject))))) {
              break label422;
            }
            ((ImageLoader.CacheImage)localObject3).animatedFile = true;
            break label422;
            label1356:
            m = i;
            if (paramInt2 != 2) {
              break label487;
            }
            localObject1 = new File(FileLoader.getDirectory(4), paramString2 + ".enc");
            m = i;
            break label487;
            label1409:
            if ((paramTLObject instanceof TLRPC.Document))
            {
              if (MessageObject.isVideoDocument((TLRPC.Document)paramTLObject))
              {
                localObject1 = new File(FileLoader.getDirectory(2), paramString2);
                m = i;
                break label487;
              }
              localObject1 = new File(FileLoader.getDirectory(3), paramString2);
              m = i;
              break label487;
            }
            if ((paramTLObject instanceof TLRPC.TL_webDocument))
            {
              localObject1 = new File(FileLoader.getDirectory(3), paramString2);
              m = i;
              break label487;
            }
            localObject1 = new File(FileLoader.getDirectory(0), paramString2);
            m = i;
            break label487;
            label1534:
            bool = false;
            break label497;
            label1540:
            bool = false;
            break label622;
            label1546:
            ImageLoader.this.cacheOutQueue.postRunnable(((ImageLoader.CacheImage)localObject3).cacheTask);
            return;
            label1562:
            ((ImageLoader.CacheImage)localObject3).url = paramString2;
            ((ImageLoader.CacheImage)localObject3).location = paramTLObject;
            ImageLoader.this.imageLoadingByUrl.put(paramString2, localObject3);
            if (paramString4 != null) {
              break label1775;
            }
            if ((paramTLObject instanceof TLRPC.FileLocation))
            {
              localObject1 = (TLRPC.FileLocation)paramTLObject;
              j = paramInt2;
              i = j;
              if (j == 0) {
                if (paramInt1 > 0)
                {
                  i = j;
                  if (((TLRPC.FileLocation)localObject1).key == null) {}
                }
                else
                {
                  i = 1;
                }
              }
              FileLoader.getInstance(j).loadFile((TLRPC.FileLocation)localObject1, paramString3, paramInt1, i);
            }
            while (paramImageReceiver.isForceLoding())
            {
              ImageLoader.this.forceLoadingImages.put(((ImageLoader.CacheImage)localObject3).key, Integer.valueOf(0));
              return;
              if ((paramTLObject instanceof TLRPC.Document)) {
                FileLoader.getInstance(j).loadFile((TLRPC.Document)paramTLObject, true, paramInt2);
              } else if ((paramTLObject instanceof TLRPC.TL_webDocument)) {
                FileLoader.getInstance(j).loadFile((TLRPC.TL_webDocument)paramTLObject, true, paramInt2);
              }
            }
          }
          label1775:
          Object localObject2 = Utilities.MD5(paramString4);
          ((ImageLoader.CacheImage)localObject3).tempFilePath = new File(FileLoader.getDirectory(4), (String)localObject2 + "_temp.jpg");
          ((ImageLoader.CacheImage)localObject3).finalFilePath = ((File)localObject1);
          ((ImageLoader.CacheImage)localObject3).httpTask = new ImageLoader.HttpImageTask(ImageLoader.this, (ImageLoader.CacheImage)localObject3, paramInt1);
          ImageLoader.this.httpTasks.add(((ImageLoader.CacheImage)localObject3).httpTask);
          ImageLoader.this.runHttpTasks(false);
        }
      });
      return;
      bool1 = false;
      break;
    }
  }
  
  private void fileDidFailedLoad(final String paramString, int paramInt)
  {
    if (paramInt == 1) {
      return;
    }
    this.imageLoadQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ImageLoader.CacheImage localCacheImage = (ImageLoader.CacheImage)ImageLoader.this.imageLoadingByUrl.get(paramString);
        if (localCacheImage != null) {
          localCacheImage.setImageAndClear(null);
        }
      }
    });
  }
  
  private void fileDidLoaded(final String paramString, final File paramFile, final int paramInt)
  {
    this.imageLoadQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject = (ImageLoader.ThumbGenerateInfo)ImageLoader.this.waitingForQualityThumb.get(paramString);
        if (localObject != null)
        {
          ImageLoader.this.generateThumb(paramInt, paramFile, ImageLoader.ThumbGenerateInfo.access$3600((ImageLoader.ThumbGenerateInfo)localObject), ImageLoader.ThumbGenerateInfo.access$3700((ImageLoader.ThumbGenerateInfo)localObject));
          ImageLoader.this.waitingForQualityThumb.remove(paramString);
        }
        ImageLoader.CacheImage localCacheImage2 = (ImageLoader.CacheImage)ImageLoader.this.imageLoadingByUrl.get(paramString);
        if (localCacheImage2 == null) {
          return;
        }
        ImageLoader.this.imageLoadingByUrl.remove(paramString);
        ArrayList localArrayList = new ArrayList();
        int i = 0;
        while (i < localCacheImage2.imageReceiverArray.size())
        {
          String str1 = (String)localCacheImage2.keys.get(i);
          String str2 = (String)localCacheImage2.filters.get(i);
          Boolean localBoolean = (Boolean)localCacheImage2.thumbs.get(i);
          ImageReceiver localImageReceiver = (ImageReceiver)localCacheImage2.imageReceiverArray.get(i);
          ImageLoader.CacheImage localCacheImage1 = (ImageLoader.CacheImage)ImageLoader.this.imageLoadingByKeys.get(str1);
          localObject = localCacheImage1;
          if (localCacheImage1 == null)
          {
            localObject = new ImageLoader.CacheImage(ImageLoader.this, null);
            ((ImageLoader.CacheImage)localObject).currentAccount = localCacheImage2.currentAccount;
            ((ImageLoader.CacheImage)localObject).finalFilePath = paramFile;
            ((ImageLoader.CacheImage)localObject).key = str1;
            ((ImageLoader.CacheImage)localObject).httpUrl = localCacheImage2.httpUrl;
            ((ImageLoader.CacheImage)localObject).selfThumb = localBoolean.booleanValue();
            ((ImageLoader.CacheImage)localObject).ext = localCacheImage2.ext;
            ((ImageLoader.CacheImage)localObject).encryptionKeyPath = localCacheImage2.encryptionKeyPath;
            ((ImageLoader.CacheImage)localObject).cacheTask = new ImageLoader.CacheOutTask(ImageLoader.this, (ImageLoader.CacheImage)localObject);
            ((ImageLoader.CacheImage)localObject).filter = str2;
            ((ImageLoader.CacheImage)localObject).animatedFile = localCacheImage2.animatedFile;
            ImageLoader.this.imageLoadingByKeys.put(str1, localObject);
            localArrayList.add(((ImageLoader.CacheImage)localObject).cacheTask);
          }
          ((ImageLoader.CacheImage)localObject).addImageReceiver(localImageReceiver, str1, str2, localBoolean.booleanValue());
          i += 1;
        }
        i = 0;
        label352:
        if (i < localArrayList.size())
        {
          localObject = (ImageLoader.CacheOutTask)localArrayList.get(i);
          if (!ImageLoader.CacheOutTask.access$1800((ImageLoader.CacheOutTask)localObject).selfThumb) {
            break label399;
          }
          ImageLoader.this.cacheThumbOutQueue.postRunnable((Runnable)localObject);
        }
        for (;;)
        {
          i += 1;
          break label352;
          break;
          label399:
          ImageLoader.this.cacheOutQueue.postRunnable((Runnable)localObject);
        }
      }
    });
  }
  
  public static void fillPhotoSizeWithBytes(TLRPC.PhotoSize paramPhotoSize)
  {
    if ((paramPhotoSize == null) || (paramPhotoSize.bytes != null)) {}
    for (;;)
    {
      return;
      Object localObject = FileLoader.getPathToAttach(paramPhotoSize, true);
      try
      {
        localObject = new RandomAccessFile((File)localObject, "r");
        if ((int)((RandomAccessFile)localObject).length() < 20000)
        {
          paramPhotoSize.bytes = new byte[(int)((RandomAccessFile)localObject).length()];
          ((RandomAccessFile)localObject).readFully(paramPhotoSize.bytes, 0, paramPhotoSize.bytes.length);
          return;
        }
      }
      catch (Throwable paramPhotoSize)
      {
        FileLog.e(paramPhotoSize);
      }
    }
  }
  
  private void generateThumb(int paramInt, File paramFile, TLRPC.FileLocation paramFileLocation, String paramString)
  {
    if (((paramInt != 0) && (paramInt != 2) && (paramInt != 3)) || (paramFile == null) || (paramFileLocation == null)) {}
    String str;
    do
    {
      return;
      str = FileLoader.getAttachFileName(paramFileLocation);
    } while ((ThumbGenerateTask)this.thumbGenerateTasks.get(str) != null);
    paramFile = new ThumbGenerateTask(paramInt, paramFile, paramFileLocation, paramString);
    this.thumbGeneratingQueue.postRunnable(paramFile);
  }
  
  public static String getHttpUrlExtension(String paramString1, String paramString2)
  {
    Object localObject = null;
    String str2 = Uri.parse(paramString1).getLastPathSegment();
    String str1 = paramString1;
    if (!TextUtils.isEmpty(str2))
    {
      str1 = paramString1;
      if (str2.length() > 1) {
        str1 = str2;
      }
    }
    int i = str1.lastIndexOf('.');
    paramString1 = (String)localObject;
    if (i != -1) {
      paramString1 = str1.substring(i + 1);
    }
    if ((paramString1 != null) && (paramString1.length() != 0))
    {
      str1 = paramString1;
      if (paramString1.length() <= 4) {}
    }
    else
    {
      str1 = paramString2;
    }
    return str1;
  }
  
  public static ImageLoader getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          ImageLoader localImageLoader2 = Instance;
          localObject1 = localImageLoader2;
          if (localImageLoader2 == null) {
            localObject1 = new ImageLoader();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (ImageLoader)localObject1;
          return (ImageLoader)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localImageLoader1;
  }
  
  private void httpFileLoadError(final String paramString)
  {
    this.imageLoadQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ImageLoader.CacheImage localCacheImage = (ImageLoader.CacheImage)ImageLoader.this.imageLoadingByUrl.get(paramString);
        if (localCacheImage == null) {
          return;
        }
        ImageLoader.HttpImageTask localHttpImageTask = localCacheImage.httpTask;
        localCacheImage.httpTask = new ImageLoader.HttpImageTask(ImageLoader.this, ImageLoader.HttpImageTask.access$400(localHttpImageTask), ImageLoader.HttpImageTask.access$4100(localHttpImageTask));
        ImageLoader.this.httpTasks.add(localCacheImage.httpTask);
        ImageLoader.this.runHttpTasks(false);
      }
    });
  }
  
  /* Error */
  public static Bitmap loadBitmap(String paramString, Uri paramUri, float paramFloat1, float paramFloat2, boolean paramBoolean)
  {
    // Byte code:
    //   0: new 596	android/graphics/BitmapFactory$Options
    //   3: dup
    //   4: invokespecial 597	android/graphics/BitmapFactory$Options:<init>	()V
    //   7: astore 13
    //   9: aload 13
    //   11: iconst_1
    //   12: putfield 601	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
    //   15: aconst_null
    //   16: astore 10
    //   18: aload_0
    //   19: astore 8
    //   21: aload_0
    //   22: ifnonnull +39 -> 61
    //   25: aload_0
    //   26: astore 8
    //   28: aload_1
    //   29: ifnull +32 -> 61
    //   32: aload_0
    //   33: astore 8
    //   35: aload_1
    //   36: invokevirtual 604	android/net/Uri:getScheme	()Ljava/lang/String;
    //   39: ifnull +22 -> 61
    //   42: aload_1
    //   43: invokevirtual 604	android/net/Uri:getScheme	()Ljava/lang/String;
    //   46: ldc_w 348
    //   49: invokevirtual 607	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   52: ifeq +112 -> 164
    //   55: aload_1
    //   56: invokevirtual 610	android/net/Uri:getPath	()Ljava/lang/String;
    //   59: astore 8
    //   61: aload 8
    //   63: ifnull +123 -> 186
    //   66: aload 8
    //   68: aload 13
    //   70: invokestatic 616	android/graphics/BitmapFactory:decodeFile	(Ljava/lang/String;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
    //   73: pop
    //   74: aload 13
    //   76: getfield 619	android/graphics/BitmapFactory$Options:outWidth	I
    //   79: i2f
    //   80: fstore 5
    //   82: aload 13
    //   84: getfield 622	android/graphics/BitmapFactory$Options:outHeight	I
    //   87: i2f
    //   88: fstore 6
    //   90: iload 4
    //   92: ifeq +145 -> 237
    //   95: fload 5
    //   97: fload_2
    //   98: fdiv
    //   99: fload 6
    //   101: fload_3
    //   102: fdiv
    //   103: invokestatic 626	java/lang/Math:max	(FF)F
    //   106: fstore_2
    //   107: fload_2
    //   108: fstore_3
    //   109: fload_2
    //   110: fconst_1
    //   111: fcmpg
    //   112: ifge +5 -> 117
    //   115: fconst_1
    //   116: fstore_3
    //   117: aload 13
    //   119: iconst_0
    //   120: putfield 601	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
    //   123: aload 13
    //   125: fload_3
    //   126: f2i
    //   127: putfield 629	android/graphics/BitmapFactory$Options:inSampleSize	I
    //   130: aload 13
    //   132: getfield 629	android/graphics/BitmapFactory$Options:inSampleSize	I
    //   135: iconst_2
    //   136: irem
    //   137: ifeq +122 -> 259
    //   140: iconst_1
    //   141: istore 7
    //   143: iload 7
    //   145: iconst_2
    //   146: imul
    //   147: aload 13
    //   149: getfield 629	android/graphics/BitmapFactory$Options:inSampleSize	I
    //   152: if_icmpge +100 -> 252
    //   155: iload 7
    //   157: iconst_2
    //   158: imul
    //   159: istore 7
    //   161: goto -18 -> 143
    //   164: aload_1
    //   165: invokestatic 632	org/telegram/messenger/AndroidUtilities:getPath	(Landroid/net/Uri;)Ljava/lang/String;
    //   168: astore 8
    //   170: goto -109 -> 61
    //   173: astore 8
    //   175: aload 8
    //   177: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   180: aload_0
    //   181: astore 8
    //   183: goto -122 -> 61
    //   186: aload_1
    //   187: ifnull -113 -> 74
    //   190: getstatic 246	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   193: invokevirtual 636	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
    //   196: aload_1
    //   197: invokevirtual 642	android/content/ContentResolver:openInputStream	(Landroid/net/Uri;)Ljava/io/InputStream;
    //   200: astore_0
    //   201: aload_0
    //   202: aconst_null
    //   203: aload 13
    //   205: invokestatic 646	android/graphics/BitmapFactory:decodeStream	(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
    //   208: pop
    //   209: aload_0
    //   210: invokevirtual 649	java/io/InputStream:close	()V
    //   213: getstatic 246	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   216: invokevirtual 636	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
    //   219: aload_1
    //   220: invokevirtual 642	android/content/ContentResolver:openInputStream	(Landroid/net/Uri;)Ljava/io/InputStream;
    //   223: astore 10
    //   225: goto -151 -> 74
    //   228: astore_0
    //   229: aload_0
    //   230: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   233: aconst_null
    //   234: astore_0
    //   235: aload_0
    //   236: areturn
    //   237: fload 5
    //   239: fload_2
    //   240: fdiv
    //   241: fload 6
    //   243: fload_3
    //   244: fdiv
    //   245: invokestatic 651	java/lang/Math:min	(FF)F
    //   248: fstore_2
    //   249: goto -142 -> 107
    //   252: aload 13
    //   254: iload 7
    //   256: putfield 629	android/graphics/BitmapFactory$Options:inSampleSize	I
    //   259: getstatic 656	android/os/Build$VERSION:SDK_INT	I
    //   262: bipush 21
    //   264: if_icmpge +188 -> 452
    //   267: iconst_1
    //   268: istore 4
    //   270: aload 13
    //   272: iload 4
    //   274: putfield 659	android/graphics/BitmapFactory$Options:inPurgeable	Z
    //   277: aconst_null
    //   278: astore_0
    //   279: aload 8
    //   281: ifnull +177 -> 458
    //   284: aload 8
    //   286: astore_0
    //   287: aconst_null
    //   288: astore 11
    //   290: aload 11
    //   292: astore 9
    //   294: aload_0
    //   295: ifnull +72 -> 367
    //   298: new 661	android/support/media/ExifInterface
    //   301: dup
    //   302: aload_0
    //   303: invokespecial 662	android/support/media/ExifInterface:<init>	(Ljava/lang/String;)V
    //   306: ldc_w 664
    //   309: iconst_1
    //   310: invokevirtual 668	android/support/media/ExifInterface:getAttributeInt	(Ljava/lang/String;I)I
    //   313: istore 7
    //   315: new 670	android/graphics/Matrix
    //   318: dup
    //   319: invokespecial 671	android/graphics/Matrix:<init>	()V
    //   322: astore_0
    //   323: iload 7
    //   325: tableswitch	default:+39->364, 3:+156->481, 4:+39->364, 5:+39->364, 6:+145->470, 7:+39->364, 8:+167->492
    //   364: aload_0
    //   365: astore 9
    //   367: aconst_null
    //   368: astore 12
    //   370: aconst_null
    //   371: astore 11
    //   373: aconst_null
    //   374: astore_0
    //   375: aload 8
    //   377: ifnull +237 -> 614
    //   380: aload 12
    //   382: astore_0
    //   383: aload 8
    //   385: aload 13
    //   387: invokestatic 616	android/graphics/BitmapFactory:decodeFile	(Ljava/lang/String;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
    //   390: astore_1
    //   391: aload_1
    //   392: astore_0
    //   393: aload_1
    //   394: ifnull -159 -> 235
    //   397: aload_1
    //   398: astore_0
    //   399: aload 13
    //   401: getfield 659	android/graphics/BitmapFactory$Options:inPurgeable	Z
    //   404: ifeq +10 -> 414
    //   407: aload_1
    //   408: astore_0
    //   409: aload_1
    //   410: invokestatic 677	org/telegram/messenger/Utilities:pinBitmap	(Landroid/graphics/Bitmap;)I
    //   413: pop
    //   414: aload_1
    //   415: astore_0
    //   416: aload_1
    //   417: iconst_0
    //   418: iconst_0
    //   419: aload_1
    //   420: invokevirtual 682	android/graphics/Bitmap:getWidth	()I
    //   423: aload_1
    //   424: invokevirtual 685	android/graphics/Bitmap:getHeight	()I
    //   427: aload 9
    //   429: iconst_1
    //   430: invokestatic 691	org/telegram/messenger/Bitmaps:createBitmap	(Landroid/graphics/Bitmap;IIIILandroid/graphics/Matrix;Z)Landroid/graphics/Bitmap;
    //   433: astore 10
    //   435: aload_1
    //   436: astore_0
    //   437: aload 10
    //   439: aload_1
    //   440: if_acmpeq -205 -> 235
    //   443: aload_1
    //   444: astore_0
    //   445: aload_1
    //   446: invokevirtual 694	android/graphics/Bitmap:recycle	()V
    //   449: aload 10
    //   451: areturn
    //   452: iconst_0
    //   453: istore 4
    //   455: goto -185 -> 270
    //   458: aload_1
    //   459: ifnull -172 -> 287
    //   462: aload_1
    //   463: invokestatic 632	org/telegram/messenger/AndroidUtilities:getPath	(Landroid/net/Uri;)Ljava/lang/String;
    //   466: astore_0
    //   467: goto -180 -> 287
    //   470: aload_0
    //   471: ldc_w 695
    //   474: invokevirtual 699	android/graphics/Matrix:postRotate	(F)Z
    //   477: pop
    //   478: goto -114 -> 364
    //   481: aload_0
    //   482: ldc_w 700
    //   485: invokevirtual 699	android/graphics/Matrix:postRotate	(F)Z
    //   488: pop
    //   489: goto -125 -> 364
    //   492: aload_0
    //   493: ldc_w 701
    //   496: invokevirtual 699	android/graphics/Matrix:postRotate	(F)Z
    //   499: pop
    //   500: goto -136 -> 364
    //   503: astore_1
    //   504: aload_1
    //   505: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   508: invokestatic 703	org/telegram/messenger/ImageLoader:getInstance	()Lorg/telegram/messenger/ImageLoader;
    //   511: invokevirtual 706	org/telegram/messenger/ImageLoader:clearMemory	()V
    //   514: aload_0
    //   515: astore_1
    //   516: aload_0
    //   517: ifnonnull +46 -> 563
    //   520: aload 8
    //   522: aload 13
    //   524: invokestatic 616	android/graphics/BitmapFactory:decodeFile	(Ljava/lang/String;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
    //   527: astore 8
    //   529: aload 8
    //   531: astore_1
    //   532: aload 8
    //   534: ifnull +29 -> 563
    //   537: aload 8
    //   539: astore_1
    //   540: aload 8
    //   542: astore_0
    //   543: aload 13
    //   545: getfield 659	android/graphics/BitmapFactory$Options:inPurgeable	Z
    //   548: ifeq +15 -> 563
    //   551: aload 8
    //   553: astore_0
    //   554: aload 8
    //   556: invokestatic 677	org/telegram/messenger/Utilities:pinBitmap	(Landroid/graphics/Bitmap;)I
    //   559: pop
    //   560: aload 8
    //   562: astore_1
    //   563: aload_1
    //   564: astore_0
    //   565: aload_1
    //   566: ifnull -331 -> 235
    //   569: aload_1
    //   570: astore_0
    //   571: aload_1
    //   572: iconst_0
    //   573: iconst_0
    //   574: aload_1
    //   575: invokevirtual 682	android/graphics/Bitmap:getWidth	()I
    //   578: aload_1
    //   579: invokevirtual 685	android/graphics/Bitmap:getHeight	()I
    //   582: aload 9
    //   584: iconst_1
    //   585: invokestatic 691	org/telegram/messenger/Bitmaps:createBitmap	(Landroid/graphics/Bitmap;IIIILandroid/graphics/Matrix;Z)Landroid/graphics/Bitmap;
    //   588: astore 8
    //   590: aload_1
    //   591: astore_0
    //   592: aload 8
    //   594: aload_1
    //   595: if_acmpeq -360 -> 235
    //   598: aload_1
    //   599: astore_0
    //   600: aload_1
    //   601: invokevirtual 694	android/graphics/Bitmap:recycle	()V
    //   604: aload 8
    //   606: areturn
    //   607: astore_1
    //   608: aload_1
    //   609: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   612: aload_0
    //   613: areturn
    //   614: aload_1
    //   615: ifnull -380 -> 235
    //   618: aload 11
    //   620: astore_0
    //   621: aload 10
    //   623: aconst_null
    //   624: aload 13
    //   626: invokestatic 646	android/graphics/BitmapFactory:decodeStream	(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
    //   629: astore_1
    //   630: aload_1
    //   631: astore_0
    //   632: aload_1
    //   633: ifnull +58 -> 691
    //   636: aload_1
    //   637: astore_0
    //   638: aload 13
    //   640: getfield 659	android/graphics/BitmapFactory$Options:inPurgeable	Z
    //   643: ifeq +10 -> 653
    //   646: aload_1
    //   647: astore_0
    //   648: aload_1
    //   649: invokestatic 677	org/telegram/messenger/Utilities:pinBitmap	(Landroid/graphics/Bitmap;)I
    //   652: pop
    //   653: aload_1
    //   654: astore_0
    //   655: aload_1
    //   656: iconst_0
    //   657: iconst_0
    //   658: aload_1
    //   659: invokevirtual 682	android/graphics/Bitmap:getWidth	()I
    //   662: aload_1
    //   663: invokevirtual 685	android/graphics/Bitmap:getHeight	()I
    //   666: aload 9
    //   668: iconst_1
    //   669: invokestatic 691	org/telegram/messenger/Bitmaps:createBitmap	(Landroid/graphics/Bitmap;IIIILandroid/graphics/Matrix;Z)Landroid/graphics/Bitmap;
    //   672: astore 8
    //   674: aload_1
    //   675: astore_0
    //   676: aload 8
    //   678: aload_1
    //   679: if_acmpeq +12 -> 691
    //   682: aload_1
    //   683: astore_0
    //   684: aload_1
    //   685: invokevirtual 694	android/graphics/Bitmap:recycle	()V
    //   688: aload 8
    //   690: astore_0
    //   691: aload 10
    //   693: invokevirtual 649	java/io/InputStream:close	()V
    //   696: aload_0
    //   697: areturn
    //   698: astore_1
    //   699: aload_1
    //   700: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   703: aload_0
    //   704: areturn
    //   705: astore_1
    //   706: aload_1
    //   707: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   710: aload 10
    //   712: invokevirtual 649	java/io/InputStream:close	()V
    //   715: aload_0
    //   716: areturn
    //   717: astore_1
    //   718: aload_1
    //   719: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   722: aload_0
    //   723: areturn
    //   724: astore_0
    //   725: aload 10
    //   727: invokevirtual 649	java/io/InputStream:close	()V
    //   730: aload_0
    //   731: athrow
    //   732: astore_1
    //   733: aload_1
    //   734: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   737: goto -7 -> 730
    //   740: astore_0
    //   741: aload 11
    //   743: astore 9
    //   745: goto -378 -> 367
    //   748: astore 9
    //   750: aload_0
    //   751: astore 9
    //   753: goto -386 -> 367
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	756	0	paramString	String
    //   0	756	1	paramUri	Uri
    //   0	756	2	paramFloat1	float
    //   0	756	3	paramFloat2	float
    //   0	756	4	paramBoolean	boolean
    //   80	158	5	f1	float
    //   88	154	6	f2	float
    //   141	183	7	i	int
    //   19	150	8	str1	String
    //   173	3	8	localThrowable1	Throwable
    //   181	508	8	localObject1	Object
    //   292	452	9	localObject2	Object
    //   748	1	9	localThrowable2	Throwable
    //   751	1	9	str2	String
    //   16	710	10	localObject3	Object
    //   288	454	11	localObject4	Object
    //   368	13	12	localObject5	Object
    //   7	632	13	localOptions	android.graphics.BitmapFactory.Options
    // Exception table:
    //   from	to	target	type
    //   164	170	173	java/lang/Throwable
    //   190	225	228	java/lang/Throwable
    //   383	391	503	java/lang/Throwable
    //   399	407	503	java/lang/Throwable
    //   409	414	503	java/lang/Throwable
    //   416	435	503	java/lang/Throwable
    //   445	449	503	java/lang/Throwable
    //   520	529	607	java/lang/Throwable
    //   543	551	607	java/lang/Throwable
    //   554	560	607	java/lang/Throwable
    //   571	590	607	java/lang/Throwable
    //   600	604	607	java/lang/Throwable
    //   691	696	698	java/lang/Throwable
    //   621	630	705	java/lang/Throwable
    //   638	646	705	java/lang/Throwable
    //   648	653	705	java/lang/Throwable
    //   655	674	705	java/lang/Throwable
    //   684	688	705	java/lang/Throwable
    //   710	715	717	java/lang/Throwable
    //   621	630	724	finally
    //   638	646	724	finally
    //   648	653	724	finally
    //   655	674	724	finally
    //   684	688	724	finally
    //   706	710	724	finally
    //   725	730	732	java/lang/Throwable
    //   298	323	740	java/lang/Throwable
    //   470	478	748	java/lang/Throwable
    //   481	489	748	java/lang/Throwable
    //   492	500	748	java/lang/Throwable
  }
  
  private void performReplace(String paramString1, String paramString2)
  {
    Object localObject1 = this.memCache.get(paramString1);
    if (localObject1 != null)
    {
      Object localObject2 = this.memCache.get(paramString2);
      int j = 0;
      int i = j;
      if (localObject2 != null)
      {
        i = j;
        if (((BitmapDrawable)localObject2).getBitmap() != null)
        {
          i = j;
          if (((BitmapDrawable)localObject1).getBitmap() != null)
          {
            localObject2 = ((BitmapDrawable)localObject2).getBitmap();
            Bitmap localBitmap = ((BitmapDrawable)localObject1).getBitmap();
            if (((Bitmap)localObject2).getWidth() <= localBitmap.getWidth())
            {
              i = j;
              if (((Bitmap)localObject2).getHeight() <= localBitmap.getHeight()) {}
            }
            else
            {
              i = 1;
            }
          }
        }
      }
      if (i != 0) {
        break label176;
      }
      this.ignoreRemoval = paramString1;
      this.memCache.remove(paramString1);
      this.memCache.put(paramString2, (BitmapDrawable)localObject1);
      this.ignoreRemoval = null;
    }
    for (;;)
    {
      localObject1 = (Integer)this.bitmapUseCounts.get(paramString1);
      if (localObject1 != null)
      {
        this.bitmapUseCounts.put(paramString2, localObject1);
        this.bitmapUseCounts.remove(paramString1);
      }
      return;
      label176:
      this.memCache.remove(paramString1);
    }
  }
  
  private void removeFromWaitingForThumb(int paramInt)
  {
    String str = (String)this.waitingForQualityThumbByTag.get(paramInt);
    if (str != null)
    {
      ThumbGenerateInfo localThumbGenerateInfo = (ThumbGenerateInfo)this.waitingForQualityThumb.get(str);
      if (localThumbGenerateInfo != null)
      {
        ThumbGenerateInfo.access$3110(localThumbGenerateInfo);
        if (localThumbGenerateInfo.count == 0) {
          this.waitingForQualityThumb.remove(str);
        }
      }
      this.waitingForQualityThumbByTag.remove(paramInt);
    }
  }
  
  private void replaceImageInCacheInternal(String paramString1, String paramString2, TLRPC.FileLocation paramFileLocation)
  {
    ArrayList localArrayList = this.memCache.getFilterKeys(paramString1);
    if (localArrayList != null)
    {
      int i = 0;
      while (i < localArrayList.size())
      {
        String str2 = (String)localArrayList.get(i);
        String str1 = paramString1 + "@" + str2;
        str2 = paramString2 + "@" + str2;
        performReplace(str1, str2);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didReplacedPhotoInMemCache, new Object[] { str1, str2, paramFileLocation });
        i += 1;
      }
    }
    performReplace(paramString1, paramString2);
    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didReplacedPhotoInMemCache, new Object[] { paramString1, paramString2, paramFileLocation });
  }
  
  private void runHttpFileLoadTasks(final HttpFileTask paramHttpFileTask, final int paramInt)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (paramHttpFileTask != null) {
          ImageLoader.access$4210(ImageLoader.this);
        }
        Object localObject;
        if (paramHttpFileTask != null)
        {
          if (paramInt != 1) {
            break label243;
          }
          if (!ImageLoader.HttpFileTask.access$4300(paramHttpFileTask)) {
            break label185;
          }
          localObject = new Runnable()
          {
            public void run()
            {
              ImageLoader.this.httpFileLoadTasks.add(this.val$newTask);
              ImageLoader.this.runHttpFileLoadTasks(null, 0);
            }
          };
          ImageLoader.this.retryHttpsTasks.put(ImageLoader.HttpFileTask.access$000(paramHttpFileTask), localObject);
          AndroidUtilities.runOnUIThread((Runnable)localObject, 1000L);
        }
        while ((ImageLoader.this.currentHttpFileLoadTasksCount < 2) && (!ImageLoader.this.httpFileLoadTasks.isEmpty()))
        {
          ((ImageLoader.HttpFileTask)ImageLoader.this.httpFileLoadTasks.poll()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
          ImageLoader.access$4208(ImageLoader.this);
          continue;
          label185:
          ImageLoader.this.httpFileLoadTasksByKeys.remove(ImageLoader.HttpFileTask.access$000(paramHttpFileTask));
          NotificationCenter.getInstance(ImageLoader.HttpFileTask.access$200(paramHttpFileTask)).postNotificationName(NotificationCenter.httpFileDidFailedLoad, new Object[] { ImageLoader.HttpFileTask.access$000(paramHttpFileTask), Integer.valueOf(0) });
          continue;
          label243:
          if (paramInt == 2)
          {
            ImageLoader.this.httpFileLoadTasksByKeys.remove(ImageLoader.HttpFileTask.access$000(paramHttpFileTask));
            localObject = new File(FileLoader.getDirectory(4), Utilities.MD5(ImageLoader.HttpFileTask.access$000(paramHttpFileTask)) + "." + ImageLoader.HttpFileTask.access$4500(paramHttpFileTask));
            if (ImageLoader.HttpFileTask.access$4400(paramHttpFileTask).renameTo((File)localObject)) {}
            for (localObject = ((File)localObject).toString();; localObject = ImageLoader.HttpFileTask.access$4400(paramHttpFileTask).toString())
            {
              NotificationCenter.getInstance(ImageLoader.HttpFileTask.access$200(paramHttpFileTask)).postNotificationName(NotificationCenter.httpFileDidLoaded, new Object[] { ImageLoader.HttpFileTask.access$000(paramHttpFileTask), localObject });
              break;
            }
          }
        }
      }
    });
  }
  
  private void runHttpTasks(boolean paramBoolean)
  {
    if (paramBoolean) {}
    for (this.currentHttpTasksCount -= 1; (this.currentHttpTasksCount < 4) && (!this.httpTasks.isEmpty()); this.currentHttpTasksCount += 1) {
      ((HttpImageTask)this.httpTasks.poll()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
    }
  }
  
  public static void saveMessageThumbs(TLRPC.Message paramMessage)
  {
    Object localObject2 = null;
    int i;
    int j;
    Object localObject1;
    label65:
    Object localObject3;
    if ((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto))
    {
      i = 0;
      j = paramMessage.media.photo.sizes.size();
      localObject1 = localObject2;
      if (i < j)
      {
        localObject1 = (TLRPC.PhotoSize)paramMessage.media.photo.sizes.get(i);
        if (!(localObject1 instanceof TLRPC.TL_photoCachedSize)) {}
      }
      else if ((localObject1 != null) && (((TLRPC.PhotoSize)localObject1).bytes != null) && (((TLRPC.PhotoSize)localObject1).bytes.length != 0))
      {
        if ((((TLRPC.PhotoSize)localObject1).location instanceof TLRPC.TL_fileLocationUnavailable))
        {
          ((TLRPC.PhotoSize)localObject1).location = new TLRPC.TL_fileLocation();
          ((TLRPC.PhotoSize)localObject1).location.volume_id = -2147483648L;
          ((TLRPC.PhotoSize)localObject1).location.dc_id = Integer.MIN_VALUE;
          ((TLRPC.PhotoSize)localObject1).location.local_id = SharedConfig.getLastLocalId();
        }
        localObject3 = FileLoader.getPathToAttach((TLObject)localObject1, true);
        i = 0;
        localObject2 = localObject3;
        if (MessageObject.shouldEncryptPhotoOrVideo(paramMessage))
        {
          localObject2 = new File(((File)localObject3).getAbsolutePath() + ".enc");
          i = 1;
        }
        if ((!((File)localObject2).exists()) && (i == 0)) {}
      }
    }
    try
    {
      localObject3 = new RandomAccessFile(new File(FileLoader.getInternalCacheDir(), ((File)localObject2).getName() + ".key"), "rws");
      long l = ((RandomAccessFile)localObject3).length();
      arrayOfByte1 = new byte[32];
      arrayOfByte2 = new byte[16];
      if ((l <= 0L) || (l % 48L != 0L)) {
        break label646;
      }
      ((RandomAccessFile)localObject3).read(arrayOfByte1, 0, 32);
      ((RandomAccessFile)localObject3).read(arrayOfByte2, 0, 16);
      label313:
      ((RandomAccessFile)localObject3).close();
      Utilities.aesCtrDecryptionByteArray(((TLRPC.PhotoSize)localObject1).bytes, arrayOfByte1, arrayOfByte2, 0, ((TLRPC.PhotoSize)localObject1).bytes.length, 0);
      localObject2 = new RandomAccessFile((File)localObject2, "rws");
      ((RandomAccessFile)localObject2).write(((TLRPC.PhotoSize)localObject1).bytes);
      ((RandomAccessFile)localObject2).close();
    }
    catch (Exception localException)
    {
      do
      {
        for (;;)
        {
          byte[] arrayOfByte1;
          byte[] arrayOfByte2;
          FileLog.e(localException);
          continue;
          i += 1;
        }
        if ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument))
        {
          paramMessage.media.document.thumb = localException;
          return;
        }
      } while (!(paramMessage.media instanceof TLRPC.TL_messageMediaWebPage));
      i = 0;
      j = paramMessage.media.webpage.photo.sizes.size();
    }
    localObject2 = new TLRPC.TL_photoSize();
    ((TLRPC.TL_photoSize)localObject2).w = ((TLRPC.PhotoSize)localObject1).w;
    ((TLRPC.TL_photoSize)localObject2).h = ((TLRPC.PhotoSize)localObject1).h;
    ((TLRPC.TL_photoSize)localObject2).location = ((TLRPC.PhotoSize)localObject1).location;
    ((TLRPC.TL_photoSize)localObject2).size = ((TLRPC.PhotoSize)localObject1).size;
    ((TLRPC.TL_photoSize)localObject2).type = ((TLRPC.PhotoSize)localObject1).type;
    if ((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto))
    {
      i = 0;
      j = paramMessage.media.photo.sizes.size();
      if (i < j)
      {
        if (!(paramMessage.media.photo.sizes.get(i) instanceof TLRPC.TL_photoCachedSize)) {
          break label689;
        }
        paramMessage.media.photo.sizes.set(i, localObject2);
      }
    }
    for (;;)
    {
      return;
      i += 1;
      break;
      if ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument))
      {
        localObject1 = localObject2;
        if (!(paramMessage.media.document.thumb instanceof TLRPC.TL_photoCachedSize)) {
          break label65;
        }
        localObject1 = paramMessage.media.document.thumb;
        break label65;
      }
      localObject1 = localObject2;
      if (!(paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
        break label65;
      }
      localObject1 = localObject2;
      if (paramMessage.media.webpage.photo == null) {
        break label65;
      }
      i = 0;
      j = paramMessage.media.webpage.photo.sizes.size();
      for (;;)
      {
        localObject1 = localObject2;
        if (i >= j) {
          break;
        }
        localObject1 = (TLRPC.PhotoSize)paramMessage.media.webpage.photo.sizes.get(i);
        if ((localObject1 instanceof TLRPC.TL_photoCachedSize)) {
          break;
        }
        i += 1;
      }
      label646:
      Utilities.random.nextBytes(arrayOfByte1);
      Utilities.random.nextBytes(arrayOfByte2);
      ((RandomAccessFile)localObject3).write(arrayOfByte1);
      ((RandomAccessFile)localObject3).write(arrayOfByte2);
      break label313;
      label689:
      while (i < j)
      {
        if ((paramMessage.media.webpage.photo.sizes.get(i) instanceof TLRPC.TL_photoCachedSize))
        {
          paramMessage.media.webpage.photo.sizes.set(i, localException);
          return;
        }
        i += 1;
      }
    }
  }
  
  public static void saveMessagesThumbs(ArrayList<TLRPC.Message> paramArrayList)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {}
    for (;;)
    {
      return;
      int i = 0;
      while (i < paramArrayList.size())
      {
        saveMessageThumbs((TLRPC.Message)paramArrayList.get(i));
        i += 1;
      }
    }
  }
  
  public static TLRPC.PhotoSize scaleAndSaveImage(Bitmap paramBitmap, float paramFloat1, float paramFloat2, int paramInt, boolean paramBoolean)
  {
    return scaleAndSaveImage(paramBitmap, paramFloat1, paramFloat2, paramInt, paramBoolean, 0, 0);
  }
  
  public static TLRPC.PhotoSize scaleAndSaveImage(Bitmap paramBitmap, float paramFloat1, float paramFloat2, int paramInt1, boolean paramBoolean, int paramInt2, int paramInt3)
  {
    if (paramBitmap == null) {
      return null;
    }
    float f1 = paramBitmap.getWidth();
    float f2 = paramBitmap.getHeight();
    if ((f1 == 0.0F) || (f2 == 0.0F)) {
      return null;
    }
    boolean bool2 = false;
    paramFloat2 = Math.max(f1 / paramFloat1, f2 / paramFloat2);
    paramFloat1 = paramFloat2;
    boolean bool1 = bool2;
    if (paramInt2 != 0)
    {
      paramFloat1 = paramFloat2;
      bool1 = bool2;
      if (paramInt3 != 0) {
        if (f1 >= paramInt2)
        {
          paramFloat1 = paramFloat2;
          bool1 = bool2;
          if (f2 >= paramInt3) {}
        }
        else
        {
          if ((f1 >= paramInt2) || (f2 <= paramInt3)) {
            break label151;
          }
          paramFloat1 = f1 / paramInt2;
        }
      }
    }
    for (;;)
    {
      bool1 = true;
      paramInt2 = (int)(f1 / paramFloat1);
      paramInt3 = (int)(f2 / paramFloat1);
      if ((paramInt3 != 0) && (paramInt2 != 0)) {
        break;
      }
      return null;
      label151:
      if ((f1 > paramInt2) && (f2 < paramInt3)) {
        paramFloat1 = f2 / paramInt3;
      } else {
        paramFloat1 = Math.max(f1 / paramInt2, f2 / paramInt3);
      }
    }
    try
    {
      TLRPC.PhotoSize localPhotoSize = scaleAndSaveImageInternal(paramBitmap, paramInt2, paramInt3, f1, f2, paramFloat1, paramInt1, paramBoolean, bool1);
      return localPhotoSize;
    }
    catch (Throwable localThrowable)
    {
      FileLog.e(localThrowable);
      getInstance().clearMemory();
      System.gc();
      try
      {
        paramBitmap = scaleAndSaveImageInternal(paramBitmap, paramInt2, paramInt3, f1, f2, paramFloat1, paramInt1, paramBoolean, bool1);
        return paramBitmap;
      }
      catch (Throwable paramBitmap)
      {
        FileLog.e(paramBitmap);
      }
    }
    return null;
  }
  
  private static TLRPC.PhotoSize scaleAndSaveImageInternal(Bitmap paramBitmap, int paramInt1, int paramInt2, float paramFloat1, float paramFloat2, float paramFloat3, int paramInt3, boolean paramBoolean1, boolean paramBoolean2)
    throws Exception
  {
    Bitmap localBitmap;
    Object localObject;
    TLRPC.TL_photoSize localTL_photoSize;
    if ((paramFloat3 > 1.0F) || (paramBoolean2))
    {
      localBitmap = Bitmaps.createScaledBitmap(paramBitmap, paramInt1, paramInt2, true);
      localObject = new TLRPC.TL_fileLocation();
      ((TLRPC.TL_fileLocation)localObject).volume_id = -2147483648L;
      ((TLRPC.TL_fileLocation)localObject).dc_id = Integer.MIN_VALUE;
      ((TLRPC.TL_fileLocation)localObject).local_id = SharedConfig.getLastLocalId();
      localTL_photoSize = new TLRPC.TL_photoSize();
      localTL_photoSize.location = ((TLRPC.FileLocation)localObject);
      localTL_photoSize.w = localBitmap.getWidth();
      localTL_photoSize.h = localBitmap.getHeight();
      if ((localTL_photoSize.w > 100) || (localTL_photoSize.h > 100)) {
        break label271;
      }
      localTL_photoSize.type = "s";
      label118:
      localObject = ((TLRPC.TL_fileLocation)localObject).volume_id + "_" + ((TLRPC.TL_fileLocation)localObject).local_id + ".jpg";
      localObject = new FileOutputStream(new File(FileLoader.getDirectory(4), (String)localObject));
      localBitmap.compress(Bitmap.CompressFormat.JPEG, paramInt3, (OutputStream)localObject);
      if (!paramBoolean1) {
        break label381;
      }
      ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
      localBitmap.compress(Bitmap.CompressFormat.JPEG, paramInt3, localByteArrayOutputStream);
      localTL_photoSize.bytes = localByteArrayOutputStream.toByteArray();
      localTL_photoSize.size = localTL_photoSize.bytes.length;
      localByteArrayOutputStream.close();
    }
    for (;;)
    {
      ((FileOutputStream)localObject).close();
      if (localBitmap != paramBitmap) {
        localBitmap.recycle();
      }
      return localTL_photoSize;
      localBitmap = paramBitmap;
      break;
      label271:
      if ((localTL_photoSize.w <= 320) && (localTL_photoSize.h <= 320))
      {
        localTL_photoSize.type = "m";
        break label118;
      }
      if ((localTL_photoSize.w <= 800) && (localTL_photoSize.h <= 800))
      {
        localTL_photoSize.type = "x";
        break label118;
      }
      if ((localTL_photoSize.w <= 1280) && (localTL_photoSize.h <= 1280))
      {
        localTL_photoSize.type = "y";
        break label118;
      }
      localTL_photoSize.type = "w";
      break label118;
      label381:
      localTL_photoSize.size = ((int)((FileOutputStream)localObject).getChannel().size());
    }
  }
  
  public void cancelForceLoadingForImageReceiver(final ImageReceiver paramImageReceiver)
  {
    if (paramImageReceiver == null) {}
    do
    {
      return;
      paramImageReceiver = paramImageReceiver.getKey();
    } while (paramImageReceiver == null);
    this.imageLoadQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ImageLoader.this.forceLoadingImages.remove(paramImageReceiver);
      }
    });
  }
  
  public void cancelLoadHttpFile(String paramString)
  {
    HttpFileTask localHttpFileTask = (HttpFileTask)this.httpFileLoadTasksByKeys.get(paramString);
    if (localHttpFileTask != null)
    {
      localHttpFileTask.cancel(true);
      this.httpFileLoadTasksByKeys.remove(paramString);
      this.httpFileLoadTasks.remove(localHttpFileTask);
    }
    paramString = (Runnable)this.retryHttpsTasks.get(paramString);
    if (paramString != null) {
      AndroidUtilities.cancelRunOnUIThread(paramString);
    }
    runHttpFileLoadTasks(null, 0);
  }
  
  public void cancelLoadingForImageReceiver(final ImageReceiver paramImageReceiver, final int paramInt)
  {
    if (paramImageReceiver == null) {
      return;
    }
    this.imageLoadQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i = 0;
        int k = 2;
        int j;
        Object localObject;
        if (paramInt == 1)
        {
          j = 1;
          if (i >= j) {
            return;
          }
          localObject = paramImageReceiver;
          if (i != 0) {
            break label110;
          }
        }
        label110:
        for (boolean bool = true;; bool = false)
        {
          k = ((ImageReceiver)localObject).getTag(bool);
          if (i == 0) {
            ImageLoader.this.removeFromWaitingForThumb(k);
          }
          if (k != 0)
          {
            localObject = (ImageLoader.CacheImage)ImageLoader.this.imageLoadingByTag.get(k);
            if (localObject != null) {
              ((ImageLoader.CacheImage)localObject).removeImageReceiver(paramImageReceiver);
            }
          }
          i += 1;
          break;
          j = k;
          if (paramInt != 2) {
            break;
          }
          i = 1;
          j = k;
          break;
        }
      }
    });
  }
  
  public void checkMediaPaths()
  {
    this.cacheOutQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            FileLoader.setMediaDirs(this.val$paths);
          }
        });
      }
    });
  }
  
  public void clearMemory()
  {
    this.memCache.evictAll();
  }
  
  /* Error */
  public SparseArray<File> createMediaPaths()
  {
    // Byte code:
    //   0: new 178	android/util/SparseArray
    //   3: dup
    //   4: invokespecial 179	android/util/SparseArray:<init>	()V
    //   7: astore_2
    //   8: invokestatic 277	org/telegram/messenger/AndroidUtilities:getCacheDir	()Ljava/io/File;
    //   11: astore_3
    //   12: aload_3
    //   13: invokevirtual 283	java/io/File:isDirectory	()Z
    //   16: ifne +8 -> 24
    //   19: aload_3
    //   20: invokevirtual 286	java/io/File:mkdirs	()Z
    //   23: pop
    //   24: new 279	java/io/File
    //   27: dup
    //   28: aload_3
    //   29: ldc_w 288
    //   32: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   35: invokevirtual 294	java/io/File:createNewFile	()Z
    //   38: pop
    //   39: aload_2
    //   40: iconst_4
    //   41: aload_3
    //   42: invokevirtual 298	android/util/SparseArray:put	(ILjava/lang/Object;)V
    //   45: getstatic 1060	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   48: ifeq +26 -> 74
    //   51: new 756	java/lang/StringBuilder
    //   54: dup
    //   55: invokespecial 757	java/lang/StringBuilder:<init>	()V
    //   58: ldc_w 1062
    //   61: invokevirtual 761	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   64: aload_3
    //   65: invokevirtual 1065	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   68: invokevirtual 766	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   71: invokestatic 1068	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   74: ldc_w 1070
    //   77: invokestatic 1075	android/os/Environment:getExternalStorageState	()Ljava/lang/String;
    //   80: invokevirtual 1078	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   83: ifeq +454 -> 537
    //   86: aload_0
    //   87: new 279	java/io/File
    //   90: dup
    //   91: invokestatic 1081	android/os/Environment:getExternalStorageDirectory	()Ljava/io/File;
    //   94: ldc_w 1083
    //   97: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   100: putfield 236	org/telegram/messenger/ImageLoader:telegramPath	Ljava/io/File;
    //   103: aload_0
    //   104: getfield 236	org/telegram/messenger/ImageLoader:telegramPath	Ljava/io/File;
    //   107: invokevirtual 286	java/io/File:mkdirs	()Z
    //   110: pop
    //   111: aload_0
    //   112: getfield 236	org/telegram/messenger/ImageLoader:telegramPath	Ljava/io/File;
    //   115: invokevirtual 283	java/io/File:isDirectory	()Z
    //   118: istore_1
    //   119: iload_1
    //   120: ifeq +347 -> 467
    //   123: new 279	java/io/File
    //   126: dup
    //   127: aload_0
    //   128: getfield 236	org/telegram/messenger/ImageLoader:telegramPath	Ljava/io/File;
    //   131: ldc_w 1085
    //   134: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   137: astore 4
    //   139: aload 4
    //   141: invokevirtual 1088	java/io/File:mkdir	()Z
    //   144: pop
    //   145: aload 4
    //   147: invokevirtual 283	java/io/File:isDirectory	()Z
    //   150: ifeq +51 -> 201
    //   153: aload_0
    //   154: aload_3
    //   155: aload 4
    //   157: iconst_0
    //   158: invokespecial 1090	org/telegram/messenger/ImageLoader:canMoveFiles	(Ljava/io/File;Ljava/io/File;I)Z
    //   161: ifeq +40 -> 201
    //   164: aload_2
    //   165: iconst_0
    //   166: aload 4
    //   168: invokevirtual 298	android/util/SparseArray:put	(ILjava/lang/Object;)V
    //   171: getstatic 1060	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   174: ifeq +27 -> 201
    //   177: new 756	java/lang/StringBuilder
    //   180: dup
    //   181: invokespecial 757	java/lang/StringBuilder:<init>	()V
    //   184: ldc_w 1092
    //   187: invokevirtual 761	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   190: aload 4
    //   192: invokevirtual 1065	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   195: invokevirtual 766	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   198: invokestatic 1068	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   201: new 279	java/io/File
    //   204: dup
    //   205: aload_0
    //   206: getfield 236	org/telegram/messenger/ImageLoader:telegramPath	Ljava/io/File;
    //   209: ldc_w 1094
    //   212: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   215: astore 4
    //   217: aload 4
    //   219: invokevirtual 1088	java/io/File:mkdir	()Z
    //   222: pop
    //   223: aload 4
    //   225: invokevirtual 283	java/io/File:isDirectory	()Z
    //   228: ifeq +51 -> 279
    //   231: aload_0
    //   232: aload_3
    //   233: aload 4
    //   235: iconst_2
    //   236: invokespecial 1090	org/telegram/messenger/ImageLoader:canMoveFiles	(Ljava/io/File;Ljava/io/File;I)Z
    //   239: ifeq +40 -> 279
    //   242: aload_2
    //   243: iconst_2
    //   244: aload 4
    //   246: invokevirtual 298	android/util/SparseArray:put	(ILjava/lang/Object;)V
    //   249: getstatic 1060	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   252: ifeq +27 -> 279
    //   255: new 756	java/lang/StringBuilder
    //   258: dup
    //   259: invokespecial 757	java/lang/StringBuilder:<init>	()V
    //   262: ldc_w 1096
    //   265: invokevirtual 761	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   268: aload 4
    //   270: invokevirtual 1065	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   273: invokevirtual 766	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   276: invokestatic 1068	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   279: new 279	java/io/File
    //   282: dup
    //   283: aload_0
    //   284: getfield 236	org/telegram/messenger/ImageLoader:telegramPath	Ljava/io/File;
    //   287: ldc_w 1098
    //   290: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   293: astore 4
    //   295: aload 4
    //   297: invokevirtual 1088	java/io/File:mkdir	()Z
    //   300: pop
    //   301: aload 4
    //   303: invokevirtual 283	java/io/File:isDirectory	()Z
    //   306: ifeq +67 -> 373
    //   309: aload_0
    //   310: aload_3
    //   311: aload 4
    //   313: iconst_1
    //   314: invokespecial 1090	org/telegram/messenger/ImageLoader:canMoveFiles	(Ljava/io/File;Ljava/io/File;I)Z
    //   317: ifeq +56 -> 373
    //   320: new 279	java/io/File
    //   323: dup
    //   324: aload 4
    //   326: ldc_w 288
    //   329: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   332: invokevirtual 294	java/io/File:createNewFile	()Z
    //   335: pop
    //   336: aload_2
    //   337: iconst_1
    //   338: aload 4
    //   340: invokevirtual 298	android/util/SparseArray:put	(ILjava/lang/Object;)V
    //   343: getstatic 1060	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   346: ifeq +27 -> 373
    //   349: new 756	java/lang/StringBuilder
    //   352: dup
    //   353: invokespecial 757	java/lang/StringBuilder:<init>	()V
    //   356: ldc_w 1100
    //   359: invokevirtual 761	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   362: aload 4
    //   364: invokevirtual 1065	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   367: invokevirtual 766	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   370: invokestatic 1068	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   373: new 279	java/io/File
    //   376: dup
    //   377: aload_0
    //   378: getfield 236	org/telegram/messenger/ImageLoader:telegramPath	Ljava/io/File;
    //   381: ldc_w 1102
    //   384: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   387: astore 4
    //   389: aload 4
    //   391: invokevirtual 1088	java/io/File:mkdir	()Z
    //   394: pop
    //   395: aload 4
    //   397: invokevirtual 283	java/io/File:isDirectory	()Z
    //   400: ifeq +67 -> 467
    //   403: aload_0
    //   404: aload_3
    //   405: aload 4
    //   407: iconst_3
    //   408: invokespecial 1090	org/telegram/messenger/ImageLoader:canMoveFiles	(Ljava/io/File;Ljava/io/File;I)Z
    //   411: ifeq +56 -> 467
    //   414: new 279	java/io/File
    //   417: dup
    //   418: aload 4
    //   420: ldc_w 288
    //   423: invokespecial 291	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   426: invokevirtual 294	java/io/File:createNewFile	()Z
    //   429: pop
    //   430: aload_2
    //   431: iconst_3
    //   432: aload 4
    //   434: invokevirtual 298	android/util/SparseArray:put	(ILjava/lang/Object;)V
    //   437: getstatic 1060	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   440: ifeq +27 -> 467
    //   443: new 756	java/lang/StringBuilder
    //   446: dup
    //   447: invokespecial 757	java/lang/StringBuilder:<init>	()V
    //   450: ldc_w 1104
    //   453: invokevirtual 761	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   456: aload 4
    //   458: invokevirtual 1065	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   461: invokevirtual 766	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   464: invokestatic 1068	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   467: invokestatic 1107	org/telegram/messenger/SharedConfig:checkSaveToGalleryFiles	()V
    //   470: aload_2
    //   471: areturn
    //   472: astore 4
    //   474: aload 4
    //   476: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   479: goto -455 -> 24
    //   482: astore 4
    //   484: aload 4
    //   486: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   489: goto -450 -> 39
    //   492: astore 4
    //   494: aload 4
    //   496: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   499: goto -298 -> 201
    //   502: astore_3
    //   503: aload_3
    //   504: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   507: aload_2
    //   508: areturn
    //   509: astore 4
    //   511: aload 4
    //   513: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   516: goto -237 -> 279
    //   519: astore 4
    //   521: aload 4
    //   523: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   526: goto -153 -> 373
    //   529: astore_3
    //   530: aload_3
    //   531: invokestatic 315	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   534: goto -67 -> 467
    //   537: getstatic 1060	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   540: ifeq -73 -> 467
    //   543: ldc_w 1109
    //   546: invokestatic 1068	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   549: goto -82 -> 467
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	552	0	this	ImageLoader
    //   118	2	1	bool	boolean
    //   7	501	2	localSparseArray	SparseArray
    //   11	394	3	localFile1	File
    //   502	2	3	localException1	Exception
    //   529	2	3	localException2	Exception
    //   137	320	4	localFile2	File
    //   472	3	4	localException3	Exception
    //   482	3	4	localException4	Exception
    //   492	3	4	localException5	Exception
    //   509	3	4	localException6	Exception
    //   519	3	4	localException7	Exception
    // Exception table:
    //   from	to	target	type
    //   19	24	472	java/lang/Exception
    //   24	39	482	java/lang/Exception
    //   123	201	492	java/lang/Exception
    //   74	119	502	java/lang/Exception
    //   467	470	502	java/lang/Exception
    //   494	499	502	java/lang/Exception
    //   511	516	502	java/lang/Exception
    //   521	526	502	java/lang/Exception
    //   530	534	502	java/lang/Exception
    //   537	549	502	java/lang/Exception
    //   201	279	509	java/lang/Exception
    //   279	373	519	java/lang/Exception
    //   373	467	529	java/lang/Exception
  }
  
  public boolean decrementUseCount(String paramString)
  {
    Integer localInteger = (Integer)this.bitmapUseCounts.get(paramString);
    if (localInteger == null) {
      return true;
    }
    if (localInteger.intValue() == 1)
    {
      this.bitmapUseCounts.remove(paramString);
      return true;
    }
    this.bitmapUseCounts.put(paramString, Integer.valueOf(localInteger.intValue() - 1));
    return false;
  }
  
  public Float getFileProgress(String paramString)
  {
    if (paramString == null) {
      return null;
    }
    return (Float)this.fileProgresses.get(paramString);
  }
  
  public BitmapDrawable getImageFromMemory(String paramString)
  {
    return this.memCache.get(paramString);
  }
  
  public BitmapDrawable getImageFromMemory(TLObject paramTLObject, String paramString1, String paramString2)
  {
    if ((paramTLObject == null) && (paramString1 == null)) {
      return null;
    }
    Object localObject = null;
    if (paramString1 != null) {
      paramString1 = Utilities.MD5(paramString1);
    }
    for (;;)
    {
      paramTLObject = paramString1;
      if (paramString2 != null) {
        paramTLObject = paramString1 + "@" + paramString2;
      }
      return this.memCache.get(paramTLObject);
      if ((paramTLObject instanceof TLRPC.FileLocation))
      {
        paramTLObject = (TLRPC.FileLocation)paramTLObject;
        paramString1 = paramTLObject.volume_id + "_" + paramTLObject.local_id;
      }
      else if ((paramTLObject instanceof TLRPC.Document))
      {
        paramTLObject = (TLRPC.Document)paramTLObject;
        if (paramTLObject.version == 0) {
          paramString1 = paramTLObject.dc_id + "_" + paramTLObject.id;
        } else {
          paramString1 = paramTLObject.dc_id + "_" + paramTLObject.id + "_" + paramTLObject.version;
        }
      }
      else
      {
        paramString1 = (String)localObject;
        if ((paramTLObject instanceof TLRPC.TL_webDocument)) {
          paramString1 = Utilities.MD5(((TLRPC.TL_webDocument)paramTLObject).url);
        }
      }
    }
  }
  
  public void incrementUseCount(String paramString)
  {
    Integer localInteger = (Integer)this.bitmapUseCounts.get(paramString);
    if (localInteger == null)
    {
      this.bitmapUseCounts.put(paramString, Integer.valueOf(1));
      return;
    }
    this.bitmapUseCounts.put(paramString, Integer.valueOf(localInteger.intValue() + 1));
  }
  
  public boolean isInCache(String paramString)
  {
    return this.memCache.get(paramString) != null;
  }
  
  public boolean isLoadingHttpFile(String paramString)
  {
    return this.httpFileLoadTasksByKeys.containsKey(paramString);
  }
  
  public void loadHttpFile(String paramString1, String paramString2, int paramInt)
  {
    if ((paramString1 == null) || (paramString1.length() == 0) || (this.httpFileLoadTasksByKeys.containsKey(paramString1))) {
      return;
    }
    paramString2 = getHttpUrlExtension(paramString1, paramString2);
    File localFile = new File(FileLoader.getDirectory(4), Utilities.MD5(paramString1) + "_temp." + paramString2);
    localFile.delete();
    paramString2 = new HttpFileTask(paramString1, localFile, paramString2, paramInt);
    this.httpFileLoadTasks.add(paramString2);
    this.httpFileLoadTasksByKeys.put(paramString1, paramString2);
    runHttpFileLoadTasks(null, 0);
  }
  
  public void loadImageForImageReceiver(ImageReceiver paramImageReceiver)
  {
    if (paramImageReceiver == null) {}
    do
    {
      do
      {
        return;
        j = 0;
        localObject1 = paramImageReceiver.getKey();
        i = j;
        if (localObject1 == null) {
          break;
        }
        localObject2 = this.memCache.get((String)localObject1);
        i = j;
        if (localObject2 == null) {
          break;
        }
        cancelLoadingForImageReceiver(paramImageReceiver, 0);
        paramImageReceiver.setImageBitmapByKey((BitmapDrawable)localObject2, (String)localObject1, false, true);
        i = 1;
      } while (!paramImageReceiver.isForcePreview());
      k = 0;
      localObject1 = paramImageReceiver.getThumbKey();
      j = k;
      if (localObject1 == null) {
        break;
      }
      localObject2 = this.memCache.get((String)localObject1);
      j = k;
      if (localObject2 == null) {
        break;
      }
      paramImageReceiver.setImageBitmapByKey((BitmapDrawable)localObject2, (String)localObject1, true, true);
      cancelLoadingForImageReceiver(paramImageReceiver, 1);
    } while ((i != 0) && (paramImageReceiver.isForcePreview()));
    int j = 1;
    TLRPC.FileLocation localFileLocation = paramImageReceiver.getThumbLocation();
    Object localObject8 = paramImageReceiver.getImageLocation();
    String str2 = paramImageReceiver.getHttpImageLocation();
    int m = 0;
    int k = 0;
    Object localObject11 = null;
    Object localObject3 = null;
    Object localObject10 = null;
    Object localObject9 = null;
    Object localObject5 = null;
    Object localObject2 = null;
    Object localObject1 = null;
    String str1 = null;
    Object localObject4 = paramImageReceiver.getExt();
    Object localObject6 = localObject4;
    if (localObject4 == null) {
      localObject6 = "jpg";
    }
    Object localObject7;
    if (str2 != null)
    {
      localObject1 = Utilities.MD5(str2);
      localObject3 = (String)localObject1 + "." + getHttpUrlExtension(str2, "jpg");
      localObject7 = localObject8;
      label248:
      localObject2 = str1;
      if (localFileLocation != null)
      {
        localObject2 = localFileLocation.volume_id + "_" + localFileLocation.local_id;
        localObject5 = (String)localObject2 + "." + (String)localObject6;
      }
      str1 = paramImageReceiver.getFilter();
      localObject8 = paramImageReceiver.getThumbFilter();
      localObject4 = localObject1;
      if (localObject1 != null)
      {
        localObject4 = localObject1;
        if (str1 != null) {
          localObject4 = (String)localObject1 + "@" + str1;
        }
      }
      localObject1 = localObject2;
      if (localObject2 != null)
      {
        localObject1 = localObject2;
        if (localObject8 != null) {
          localObject1 = (String)localObject2 + "@" + (String)localObject8;
        }
      }
      if (str2 == null) {
        break label1081;
      }
      if (j == 0) {
        break label1076;
      }
    }
    label764:
    label850:
    label880:
    label920:
    label1052:
    label1063:
    label1071:
    label1076:
    for (int i = 2;; i = 1)
    {
      createLoadOperationForImageReceiver(paramImageReceiver, (String)localObject1, (String)localObject5, (String)localObject6, localFileLocation, null, (String)localObject8, 0, 1, i);
      createLoadOperationForImageReceiver(paramImageReceiver, (String)localObject4, (String)localObject3, (String)localObject6, null, str2, str1, 0, 1, 0);
      return;
      localObject7 = localObject8;
      if (localObject8 == null) {
        break label248;
      }
      if ((localObject8 instanceof TLRPC.FileLocation))
      {
        localObject7 = (TLRPC.FileLocation)localObject8;
        localObject3 = ((TLRPC.FileLocation)localObject7).volume_id + "_" + ((TLRPC.FileLocation)localObject7).local_id;
        localObject5 = (String)localObject3 + "." + (String)localObject6;
        if ((paramImageReceiver.getExt() == null) && (((TLRPC.FileLocation)localObject7).key == null))
        {
          localObject4 = localObject10;
          localObject1 = localObject5;
          localObject2 = localObject3;
          i = m;
          if (((TLRPC.FileLocation)localObject7).volume_id == -2147483648L)
          {
            localObject4 = localObject10;
            localObject1 = localObject5;
            localObject2 = localObject3;
            i = m;
            if (((TLRPC.FileLocation)localObject7).local_id >= 0) {}
          }
        }
        else
        {
          i = 1;
          localObject2 = localObject3;
          localObject1 = localObject5;
          localObject4 = localObject10;
        }
      }
      do
      {
        for (;;)
        {
          localObject5 = localObject4;
          localObject3 = localObject1;
          localObject7 = localObject8;
          localObject1 = localObject2;
          k = i;
          if (localObject8 != localFileLocation) {
            break;
          }
          localObject7 = null;
          localObject1 = null;
          localObject3 = null;
          localObject5 = localObject4;
          k = i;
          break;
          if (!(localObject8 instanceof TLRPC.TL_webDocument)) {
            break label764;
          }
          localObject1 = (TLRPC.TL_webDocument)localObject8;
          localObject3 = FileLoader.getExtensionByMime(((TLRPC.TL_webDocument)localObject1).mime_type);
          localObject2 = Utilities.MD5(((TLRPC.TL_webDocument)localObject1).url);
          localObject1 = (String)localObject2 + "." + getHttpUrlExtension(((TLRPC.TL_webDocument)localObject1).url, (String)localObject3);
          localObject4 = localObject10;
          i = m;
        }
        localObject4 = localObject10;
        localObject1 = localObject11;
        i = m;
      } while (!(localObject8 instanceof TLRPC.Document));
      localObject5 = (TLRPC.Document)localObject8;
      if ((((TLRPC.Document)localObject5).id == 0L) || (((TLRPC.Document)localObject5).dc_id == 0)) {
        break;
      }
      if (((TLRPC.Document)localObject5).version == 0)
      {
        localObject2 = ((TLRPC.Document)localObject5).dc_id + "_" + ((TLRPC.Document)localObject5).id;
        localObject1 = FileLoader.getDocumentFileName((TLRPC.Document)localObject5);
        if (localObject1 != null)
        {
          i = ((String)localObject1).lastIndexOf('.');
          if (i != -1) {
            break label1052;
          }
        }
        localObject1 = "";
        localObject3 = localObject1;
        if (((String)localObject1).length() <= 1)
        {
          if ((((TLRPC.Document)localObject5).mime_type == null) || (!((TLRPC.Document)localObject5).mime_type.equals("video/mp4"))) {
            break label1063;
          }
          localObject3 = ".mp4";
        }
        localObject1 = (String)localObject2 + (String)localObject3;
        localObject4 = localObject9;
        if (0 != 0) {
          localObject4 = null + "." + (String)localObject6;
        }
        if ((MessageObject.isGifDocument((TLRPC.Document)localObject5)) || (MessageObject.isRoundVideoDocument((TLRPC.Document)localObject8))) {
          break label1071;
        }
      }
      for (i = 1;; i = 0)
      {
        break;
        localObject2 = ((TLRPC.Document)localObject5).dc_id + "_" + ((TLRPC.Document)localObject5).id + "_" + ((TLRPC.Document)localObject5).version;
        break label850;
        localObject1 = ((String)localObject1).substring(i);
        break label880;
        localObject3 = "";
        break label920;
      }
    }
    label1081:
    m = paramImageReceiver.getCacheType();
    i = m;
    if (m == 0)
    {
      i = m;
      if (k != 0) {
        i = 1;
      }
    }
    if (i == 0)
    {
      k = 1;
      if (j == 0) {
        break label1167;
      }
    }
    label1167:
    for (j = 2;; j = 1)
    {
      createLoadOperationForImageReceiver(paramImageReceiver, (String)localObject1, (String)localObject5, (String)localObject6, localFileLocation, null, (String)localObject8, 0, k, j);
      createLoadOperationForImageReceiver(paramImageReceiver, (String)localObject4, (String)localObject3, (String)localObject6, (TLObject)localObject7, null, str1, paramImageReceiver.getSize(), i, 0);
      return;
      k = i;
      break;
    }
  }
  
  public void putImageToCache(BitmapDrawable paramBitmapDrawable, String paramString)
  {
    this.memCache.put(paramString, paramBitmapDrawable);
  }
  
  public void removeImage(String paramString)
  {
    this.bitmapUseCounts.remove(paramString);
    this.memCache.remove(paramString);
  }
  
  public void replaceImageInCache(final String paramString1, final String paramString2, final TLRPC.FileLocation paramFileLocation, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          ImageLoader.this.replaceImageInCacheInternal(paramString1, paramString2, paramFileLocation);
        }
      });
      return;
    }
    replaceImageInCacheInternal(paramString1, paramString2, paramFileLocation);
  }
  
  private class CacheImage
  {
    protected boolean animatedFile;
    protected ImageLoader.CacheOutTask cacheTask;
    protected int currentAccount;
    protected File encryptionKeyPath;
    protected String ext;
    protected String filter;
    protected ArrayList<String> filters = new ArrayList();
    protected File finalFilePath;
    protected ImageLoader.HttpImageTask httpTask;
    protected String httpUrl;
    protected ArrayList<ImageReceiver> imageReceiverArray = new ArrayList();
    protected String key;
    protected ArrayList<String> keys = new ArrayList();
    protected TLObject location;
    protected boolean selfThumb;
    protected File tempFilePath;
    protected ArrayList<Boolean> thumbs = new ArrayList();
    protected String url;
    
    private CacheImage() {}
    
    public void addImageReceiver(ImageReceiver paramImageReceiver, String paramString1, String paramString2, boolean paramBoolean)
    {
      if (this.imageReceiverArray.contains(paramImageReceiver)) {
        return;
      }
      this.imageReceiverArray.add(paramImageReceiver);
      this.keys.add(paramString1);
      this.filters.add(paramString2);
      this.thumbs.add(Boolean.valueOf(paramBoolean));
      ImageLoader.this.imageLoadingByTag.put(paramImageReceiver.getTag(paramBoolean), this);
    }
    
    public void removeImageReceiver(ImageReceiver paramImageReceiver)
    {
      Boolean localBoolean = Boolean.valueOf(this.selfThumb);
      int j;
      for (int i = 0; i < this.imageReceiverArray.size(); i = j + 1)
      {
        ImageReceiver localImageReceiver = (ImageReceiver)this.imageReceiverArray.get(i);
        if (localImageReceiver != null)
        {
          j = i;
          if (localImageReceiver != paramImageReceiver) {}
        }
        else
        {
          this.imageReceiverArray.remove(i);
          this.keys.remove(i);
          this.filters.remove(i);
          localBoolean = (Boolean)this.thumbs.remove(i);
          if (localImageReceiver != null) {
            ImageLoader.this.imageLoadingByTag.remove(localImageReceiver.getTag(localBoolean.booleanValue()));
          }
          j = i - 1;
        }
      }
      if (this.imageReceiverArray.size() == 0)
      {
        i = 0;
        while (i < this.imageReceiverArray.size())
        {
          ImageLoader.this.imageLoadingByTag.remove(((ImageReceiver)this.imageReceiverArray.get(i)).getTag(localBoolean.booleanValue()));
          i += 1;
        }
        this.imageReceiverArray.clear();
        if ((this.location != null) && (!ImageLoader.this.forceLoadingImages.containsKey(this.key)))
        {
          if (!(this.location instanceof TLRPC.FileLocation)) {
            break label366;
          }
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.FileLocation)this.location, this.ext);
        }
        if (this.cacheTask != null)
        {
          if (!this.selfThumb) {
            break label426;
          }
          ImageLoader.this.cacheThumbOutQueue.cancelRunnable(this.cacheTask);
        }
      }
      for (;;)
      {
        this.cacheTask.cancel();
        this.cacheTask = null;
        if (this.httpTask != null)
        {
          ImageLoader.this.httpTasks.remove(this.httpTask);
          this.httpTask.cancel(true);
          this.httpTask = null;
        }
        if (this.url != null) {
          ImageLoader.this.imageLoadingByUrl.remove(this.url);
        }
        if (this.key != null) {
          ImageLoader.this.imageLoadingByKeys.remove(this.key);
        }
        return;
        label366:
        if ((this.location instanceof TLRPC.Document))
        {
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.Document)this.location);
          break;
        }
        if (!(this.location instanceof TLRPC.TL_webDocument)) {
          break;
        }
        FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.TL_webDocument)this.location);
        break;
        label426:
        ImageLoader.this.cacheOutQueue.cancelRunnable(this.cacheTask);
      }
    }
    
    public void replaceImageReceiver(ImageReceiver paramImageReceiver, String paramString1, String paramString2, boolean paramBoolean)
    {
      int j = this.imageReceiverArray.indexOf(paramImageReceiver);
      if (j == -1) {}
      int i;
      do
      {
        return;
        i = j;
        if (((Boolean)this.thumbs.get(j)).booleanValue() == paramBoolean) {
          break;
        }
        i = this.imageReceiverArray.subList(j + 1, this.imageReceiverArray.size()).indexOf(paramImageReceiver);
      } while (i == -1);
      this.keys.set(i, paramString1);
      this.filters.set(i, paramString2);
    }
    
    public void setImageAndClear(final BitmapDrawable paramBitmapDrawable)
    {
      if (paramBitmapDrawable != null) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            int i;
            if ((paramBitmapDrawable instanceof AnimatedFileDrawable))
            {
              int j = 0;
              AnimatedFileDrawable localAnimatedFileDrawable2 = (AnimatedFileDrawable)paramBitmapDrawable;
              i = 0;
              if (i < this.val$finalImageReceiverArray.size())
              {
                ImageReceiver localImageReceiver = (ImageReceiver)this.val$finalImageReceiverArray.get(i);
                if (i == 0) {}
                for (AnimatedFileDrawable localAnimatedFileDrawable1 = localAnimatedFileDrawable2;; localAnimatedFileDrawable1 = localAnimatedFileDrawable2.makeCopy())
                {
                  if (localImageReceiver.setImageBitmapByKey(localAnimatedFileDrawable1, ImageLoader.CacheImage.this.key, ImageLoader.CacheImage.this.selfThumb, false)) {
                    j = 1;
                  }
                  i += 1;
                  break;
                }
              }
              if (j == 0) {
                ((AnimatedFileDrawable)paramBitmapDrawable).recycle();
              }
            }
            for (;;)
            {
              return;
              i = 0;
              while (i < this.val$finalImageReceiverArray.size())
              {
                ((ImageReceiver)this.val$finalImageReceiverArray.get(i)).setImageBitmapByKey(paramBitmapDrawable, ImageLoader.CacheImage.this.key, ImageLoader.CacheImage.this.selfThumb, false);
                i += 1;
              }
            }
          }
        });
      }
      int i = 0;
      while (i < this.imageReceiverArray.size())
      {
        paramBitmapDrawable = (ImageReceiver)this.imageReceiverArray.get(i);
        ImageLoader.this.imageLoadingByTag.remove(paramBitmapDrawable.getTag(this.selfThumb));
        i += 1;
      }
      this.imageReceiverArray.clear();
      if (this.url != null) {
        ImageLoader.this.imageLoadingByUrl.remove(this.url);
      }
      if (this.key != null) {
        ImageLoader.this.imageLoadingByKeys.remove(this.key);
      }
    }
  }
  
  private class CacheOutTask
    implements Runnable
  {
    private ImageLoader.CacheImage cacheImage;
    private boolean isCancelled;
    private Thread runningThread;
    private final Object sync = new Object();
    
    public CacheOutTask(ImageLoader.CacheImage paramCacheImage)
    {
      this.cacheImage = paramCacheImage;
    }
    
    private void onPostExecute(final BitmapDrawable paramBitmapDrawable)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          final BitmapDrawable localBitmapDrawable = null;
          if ((paramBitmapDrawable instanceof AnimatedFileDrawable)) {
            localBitmapDrawable = paramBitmapDrawable;
          }
          for (;;)
          {
            ImageLoader.this.imageLoadQueue.postRunnable(new Runnable()
            {
              public void run()
              {
                ImageLoader.CacheOutTask.this.cacheImage.setImageAndClear(localBitmapDrawable);
              }
            });
            return;
            if (paramBitmapDrawable != null)
            {
              localBitmapDrawable = ImageLoader.this.memCache.get(ImageLoader.CacheOutTask.this.cacheImage.key);
              if (localBitmapDrawable == null)
              {
                ImageLoader.this.memCache.put(ImageLoader.CacheOutTask.this.cacheImage.key, paramBitmapDrawable);
                localBitmapDrawable = paramBitmapDrawable;
              }
              else
              {
                paramBitmapDrawable.getBitmap().recycle();
              }
            }
          }
        }
      });
    }
    
    public void cancel()
    {
      try
      {
        synchronized (this.sync)
        {
          this.isCancelled = true;
          if (this.runningThread != null) {
            this.runningThread.interrupt();
          }
          return;
        }
      }
      catch (Exception localException)
      {
        for (;;) {}
      }
    }
    
    /* Error */
    public void run()
    {
      // Byte code:
      //   0: aload_0
      //   1: getfield 32	org/telegram/messenger/ImageLoader$CacheOutTask:sync	Ljava/lang/Object;
      //   4: astore 15
      //   6: aload 15
      //   8: monitorenter
      //   9: aload_0
      //   10: invokestatic 67	java/lang/Thread:currentThread	()Ljava/lang/Thread;
      //   13: putfield 55	org/telegram/messenger/ImageLoader$CacheOutTask:runningThread	Ljava/lang/Thread;
      //   16: invokestatic 71	java/lang/Thread:interrupted	()Z
      //   19: pop
      //   20: aload_0
      //   21: getfield 53	org/telegram/messenger/ImageLoader$CacheOutTask:isCancelled	Z
      //   24: ifeq +7 -> 31
      //   27: aload 15
      //   29: monitorexit
      //   30: return
      //   31: aload 15
      //   33: monitorexit
      //   34: aload_0
      //   35: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   38: getfield 76	org/telegram/messenger/ImageLoader$CacheImage:animatedFile	Z
      //   41: ifeq +109 -> 150
      //   44: aload_0
      //   45: getfield 32	org/telegram/messenger/ImageLoader$CacheOutTask:sync	Ljava/lang/Object;
      //   48: astore 15
      //   50: aload 15
      //   52: monitorenter
      //   53: aload_0
      //   54: getfield 53	org/telegram/messenger/ImageLoader$CacheOutTask:isCancelled	Z
      //   57: ifeq +23 -> 80
      //   60: aload 15
      //   62: monitorexit
      //   63: return
      //   64: astore 16
      //   66: aload 15
      //   68: monitorexit
      //   69: aload 16
      //   71: athrow
      //   72: astore 16
      //   74: aload 15
      //   76: monitorexit
      //   77: aload 16
      //   79: athrow
      //   80: aload 15
      //   82: monitorexit
      //   83: aload_0
      //   84: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   87: getfield 80	org/telegram/messenger/ImageLoader$CacheImage:finalFilePath	Ljava/io/File;
      //   90: astore 15
      //   92: aload_0
      //   93: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   96: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   99: ifnull +45 -> 144
      //   102: aload_0
      //   103: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   106: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   109: ldc 86
      //   111: invokevirtual 92	java/lang/String:equals	(Ljava/lang/Object;)Z
      //   114: ifeq +30 -> 144
      //   117: iconst_1
      //   118: istore 14
      //   120: new 94	org/telegram/ui/Components/AnimatedFileDrawable
      //   123: dup
      //   124: aload 15
      //   126: iload 14
      //   128: invokespecial 97	org/telegram/ui/Components/AnimatedFileDrawable:<init>	(Ljava/io/File;Z)V
      //   131: astore 15
      //   133: invokestatic 71	java/lang/Thread:interrupted	()Z
      //   136: pop
      //   137: aload_0
      //   138: aload 15
      //   140: invokespecial 99	org/telegram/messenger/ImageLoader$CacheOutTask:onPostExecute	(Landroid/graphics/drawable/BitmapDrawable;)V
      //   143: return
      //   144: iconst_0
      //   145: istore 14
      //   147: goto -27 -> 120
      //   150: aconst_null
      //   151: astore 21
      //   153: aconst_null
      //   154: astore 22
      //   156: aconst_null
      //   157: astore 20
      //   159: iconst_0
      //   160: istore 11
      //   162: iconst_0
      //   163: istore 9
      //   165: aload_0
      //   166: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   169: getfield 80	org/telegram/messenger/ImageLoader$CacheImage:finalFilePath	Ljava/io/File;
      //   172: astore 23
      //   174: aload_0
      //   175: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   178: getfield 102	org/telegram/messenger/ImageLoader$CacheImage:encryptionKeyPath	Ljava/io/File;
      //   181: ifnull +316 -> 497
      //   184: aload 23
      //   186: ifnull +311 -> 497
      //   189: aload 23
      //   191: invokevirtual 108	java/io/File:getAbsolutePath	()Ljava/lang/String;
      //   194: ldc 110
      //   196: invokevirtual 114	java/lang/String:endsWith	(Ljava/lang/String;)Z
      //   199: ifeq +298 -> 497
      //   202: iconst_1
      //   203: istore 10
      //   205: iconst_1
      //   206: istore 12
      //   208: iconst_0
      //   209: istore 7
      //   211: iconst_0
      //   212: istore 6
      //   214: iconst_0
      //   215: istore 13
      //   217: iconst_0
      //   218: istore 8
      //   220: getstatic 120	android/os/Build$VERSION:SDK_INT	I
      //   223: bipush 19
      //   225: if_icmpge +147 -> 372
      //   228: aconst_null
      //   229: astore 15
      //   231: aconst_null
      //   232: astore 17
      //   234: new 122	java/io/RandomAccessFile
      //   237: dup
      //   238: aload 23
      //   240: ldc 124
      //   242: invokespecial 127	java/io/RandomAccessFile:<init>	(Ljava/io/File;Ljava/lang/String;)V
      //   245: astore 16
      //   247: iload 13
      //   249: istore 6
      //   251: aload_0
      //   252: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   255: getfield 130	org/telegram/messenger/ImageLoader$CacheImage:selfThumb	Z
      //   258: ifeq +245 -> 503
      //   261: iload 13
      //   263: istore 6
      //   265: invokestatic 134	org/telegram/messenger/ImageLoader:access$1300	()[B
      //   268: astore 15
      //   270: iload 13
      //   272: istore 6
      //   274: aload 16
      //   276: aload 15
      //   278: iconst_0
      //   279: aload 15
      //   281: arraylength
      //   282: invokevirtual 138	java/io/RandomAccessFile:readFully	([BII)V
      //   285: iload 13
      //   287: istore 6
      //   289: new 88	java/lang/String
      //   292: dup
      //   293: aload 15
      //   295: invokespecial 141	java/lang/String:<init>	([B)V
      //   298: invokevirtual 144	java/lang/String:toLowerCase	()Ljava/lang/String;
      //   301: invokevirtual 144	java/lang/String:toLowerCase	()Ljava/lang/String;
      //   304: astore 15
      //   306: iload 8
      //   308: istore 5
      //   310: iload 13
      //   312: istore 6
      //   314: aload 15
      //   316: ldc -110
      //   318: invokevirtual 149	java/lang/String:startsWith	(Ljava/lang/String;)Z
      //   321: ifeq +24 -> 345
      //   324: iload 8
      //   326: istore 5
      //   328: iload 13
      //   330: istore 6
      //   332: aload 15
      //   334: ldc -105
      //   336: invokevirtual 114	java/lang/String:endsWith	(Ljava/lang/String;)Z
      //   339: ifeq +6 -> 345
      //   342: iconst_1
      //   343: istore 5
      //   345: iload 5
      //   347: istore 6
      //   349: aload 16
      //   351: invokevirtual 154	java/io/RandomAccessFile:close	()V
      //   354: iload 5
      //   356: istore 7
      //   358: aload 16
      //   360: ifnull +12 -> 372
      //   363: aload 16
      //   365: invokevirtual 154	java/io/RandomAccessFile:close	()V
      //   368: iload 5
      //   370: istore 7
      //   372: aload_0
      //   373: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   376: getfield 130	org/telegram/messenger/ImageLoader$CacheImage:selfThumb	Z
      //   379: ifeq +1047 -> 1426
      //   382: iconst_0
      //   383: istore 6
      //   385: iload 6
      //   387: istore 5
      //   389: aload_0
      //   390: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   393: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   396: ifnull +21 -> 417
      //   399: aload_0
      //   400: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   403: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   406: ldc -100
      //   408: invokevirtual 160	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
      //   411: ifeq +205 -> 616
      //   414: iconst_3
      //   415: istore 5
      //   417: aload_0
      //   418: getfield 27	org/telegram/messenger/ImageLoader$CacheOutTask:this$0	Lorg/telegram/messenger/ImageLoader;
      //   421: invokestatic 166	java/lang/System:currentTimeMillis	()J
      //   424: invokestatic 170	org/telegram/messenger/ImageLoader:access$1502	(Lorg/telegram/messenger/ImageLoader;J)J
      //   427: pop2
      //   428: aload_0
      //   429: getfield 32	org/telegram/messenger/ImageLoader$CacheOutTask:sync	Ljava/lang/Object;
      //   432: astore 15
      //   434: aload 15
      //   436: monitorenter
      //   437: aload_0
      //   438: getfield 53	org/telegram/messenger/ImageLoader$CacheOutTask:isCancelled	Z
      //   441: ifeq +221 -> 662
      //   444: aload 15
      //   446: monitorexit
      //   447: return
      //   448: astore 16
      //   450: aload 15
      //   452: monitorexit
      //   453: aload 16
      //   455: athrow
      //   456: astore 15
      //   458: aconst_null
      //   459: astore 16
      //   461: aload 15
      //   463: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   466: aload 16
      //   468: astore 17
      //   470: invokestatic 71	java/lang/Thread:interrupted	()Z
      //   473: pop
      //   474: aload 17
      //   476: ifnull +2784 -> 3260
      //   479: new 178	android/graphics/drawable/BitmapDrawable
      //   482: dup
      //   483: aload 17
      //   485: invokespecial 181	android/graphics/drawable/BitmapDrawable:<init>	(Landroid/graphics/Bitmap;)V
      //   488: astore 15
      //   490: aload_0
      //   491: aload 15
      //   493: invokespecial 99	org/telegram/messenger/ImageLoader$CacheOutTask:onPostExecute	(Landroid/graphics/drawable/BitmapDrawable;)V
      //   496: return
      //   497: iconst_0
      //   498: istore 10
      //   500: goto -295 -> 205
      //   503: iload 13
      //   505: istore 6
      //   507: invokestatic 184	org/telegram/messenger/ImageLoader:access$1400	()[B
      //   510: astore 15
      //   512: goto -242 -> 270
      //   515: astore 15
      //   517: aload 15
      //   519: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   522: iload 5
      //   524: istore 7
      //   526: goto -154 -> 372
      //   529: astore 15
      //   531: aload 17
      //   533: astore 16
      //   535: aload 15
      //   537: astore 17
      //   539: aload 16
      //   541: astore 15
      //   543: aload 17
      //   545: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   548: iload 6
      //   550: istore 7
      //   552: aload 16
      //   554: ifnull -182 -> 372
      //   557: aload 16
      //   559: invokevirtual 154	java/io/RandomAccessFile:close	()V
      //   562: iload 6
      //   564: istore 7
      //   566: goto -194 -> 372
      //   569: astore 15
      //   571: aload 15
      //   573: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   576: iload 6
      //   578: istore 7
      //   580: goto -208 -> 372
      //   583: astore 17
      //   585: aload 15
      //   587: astore 16
      //   589: aload 17
      //   591: astore 15
      //   593: aload 16
      //   595: ifnull +8 -> 603
      //   598: aload 16
      //   600: invokevirtual 154	java/io/RandomAccessFile:close	()V
      //   603: aload 15
      //   605: athrow
      //   606: astore 16
      //   608: aload 16
      //   610: invokestatic 176	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
      //   613: goto -10 -> 603
      //   616: aload_0
      //   617: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   620: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   623: ldc -70
      //   625: invokevirtual 160	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
      //   628: ifeq +9 -> 637
      //   631: iconst_2
      //   632: istore 5
      //   634: goto -217 -> 417
      //   637: iload 6
      //   639: istore 5
      //   641: aload_0
      //   642: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   645: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   648: ldc -68
      //   650: invokevirtual 160	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
      //   653: ifeq -236 -> 417
      //   656: iconst_1
      //   657: istore 5
      //   659: goto -242 -> 417
      //   662: aload 15
      //   664: monitorexit
      //   665: new 190	android/graphics/BitmapFactory$Options
      //   668: dup
      //   669: invokespecial 191	android/graphics/BitmapFactory$Options:<init>	()V
      //   672: astore 18
      //   674: aload 18
      //   676: iconst_1
      //   677: putfield 194	android/graphics/BitmapFactory$Options:inSampleSize	I
      //   680: getstatic 120	android/os/Build$VERSION:SDK_INT	I
      //   683: bipush 21
      //   685: if_icmpge +9 -> 694
      //   688: aload 18
      //   690: iconst_1
      //   691: putfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   694: iload 7
      //   696: ifeq +200 -> 896
      //   699: new 122	java/io/RandomAccessFile
      //   702: dup
      //   703: aload 23
      //   705: ldc 124
      //   707: invokespecial 127	java/io/RandomAccessFile:<init>	(Ljava/io/File;Ljava/lang/String;)V
      //   710: astore 17
      //   712: aload 17
      //   714: invokevirtual 201	java/io/RandomAccessFile:getChannel	()Ljava/nio/channels/FileChannel;
      //   717: getstatic 207	java/nio/channels/FileChannel$MapMode:READ_ONLY	Ljava/nio/channels/FileChannel$MapMode;
      //   720: lconst_0
      //   721: aload 23
      //   723: invokevirtual 210	java/io/File:length	()J
      //   726: invokevirtual 216	java/nio/channels/FileChannel:map	(Ljava/nio/channels/FileChannel$MapMode;JJ)Ljava/nio/MappedByteBuffer;
      //   729: astore 19
      //   731: new 190	android/graphics/BitmapFactory$Options
      //   734: dup
      //   735: invokespecial 191	android/graphics/BitmapFactory$Options:<init>	()V
      //   738: astore 15
      //   740: aload 15
      //   742: iconst_1
      //   743: putfield 219	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
      //   746: aconst_null
      //   747: aload 19
      //   749: aload 19
      //   751: invokevirtual 225	java/nio/ByteBuffer:limit	()I
      //   754: aload 15
      //   756: iconst_1
      //   757: invokestatic 231	org/telegram/messenger/Utilities:loadWebpImage	(Landroid/graphics/Bitmap;Ljava/nio/ByteBuffer;ILandroid/graphics/BitmapFactory$Options;Z)Z
      //   760: pop
      //   761: aload 15
      //   763: getfield 234	android/graphics/BitmapFactory$Options:outWidth	I
      //   766: aload 15
      //   768: getfield 237	android/graphics/BitmapFactory$Options:outHeight	I
      //   771: getstatic 243	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
      //   774: invokestatic 249	org/telegram/messenger/Bitmaps:createBitmap	(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;
      //   777: astore 15
      //   779: aload 15
      //   781: astore 16
      //   783: aload 19
      //   785: invokevirtual 225	java/nio/ByteBuffer:limit	()I
      //   788: istore 6
      //   790: aload 15
      //   792: astore 16
      //   794: aload 18
      //   796: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   799: ifne +91 -> 890
      //   802: iconst_1
      //   803: istore 14
      //   805: aload 15
      //   807: astore 16
      //   809: aload 15
      //   811: aload 19
      //   813: iload 6
      //   815: aconst_null
      //   816: iload 14
      //   818: invokestatic 231	org/telegram/messenger/Utilities:loadWebpImage	(Landroid/graphics/Bitmap;Ljava/nio/ByteBuffer;ILandroid/graphics/BitmapFactory$Options;Z)Z
      //   821: pop
      //   822: aload 15
      //   824: astore 16
      //   826: aload 17
      //   828: invokevirtual 154	java/io/RandomAccessFile:close	()V
      //   831: aload 15
      //   833: ifnonnull +242 -> 1075
      //   836: aload 15
      //   838: astore 16
      //   840: aload 23
      //   842: invokevirtual 210	java/io/File:length	()J
      //   845: lconst_0
      //   846: lcmp
      //   847: ifeq +21 -> 868
      //   850: aload 15
      //   852: astore 17
      //   854: aload 15
      //   856: astore 16
      //   858: aload_0
      //   859: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   862: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   865: ifnonnull -395 -> 470
      //   868: aload 15
      //   870: astore 16
      //   872: aload 23
      //   874: invokevirtual 252	java/io/File:delete	()Z
      //   877: pop
      //   878: aload 15
      //   880: astore 17
      //   882: goto -412 -> 470
      //   885: astore 15
      //   887: goto -426 -> 461
      //   890: iconst_0
      //   891: istore 14
      //   893: goto -88 -> 805
      //   896: aload 18
      //   898: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   901: ifeq +115 -> 1016
      //   904: new 122	java/io/RandomAccessFile
      //   907: dup
      //   908: aload 23
      //   910: ldc 124
      //   912: invokespecial 127	java/io/RandomAccessFile:<init>	(Ljava/io/File;Ljava/lang/String;)V
      //   915: astore 17
      //   917: aload 17
      //   919: invokevirtual 253	java/io/RandomAccessFile:length	()J
      //   922: l2i
      //   923: istore 6
      //   925: invokestatic 256	org/telegram/messenger/ImageLoader:access$1600	()[B
      //   928: ifnull +2360 -> 3288
      //   931: invokestatic 256	org/telegram/messenger/ImageLoader:access$1600	()[B
      //   934: arraylength
      //   935: iload 6
      //   937: if_icmplt +2351 -> 3288
      //   940: invokestatic 256	org/telegram/messenger/ImageLoader:access$1600	()[B
      //   943: astore 15
      //   945: aload 15
      //   947: astore 16
      //   949: aload 15
      //   951: ifnonnull +15 -> 966
      //   954: iload 6
      //   956: newarray <illegal type>
      //   958: astore 16
      //   960: aload 16
      //   962: invokestatic 260	org/telegram/messenger/ImageLoader:access$1602	([B)[B
      //   965: pop
      //   966: aload 17
      //   968: aload 16
      //   970: iconst_0
      //   971: iload 6
      //   973: invokevirtual 138	java/io/RandomAccessFile:readFully	([BII)V
      //   976: aload 17
      //   978: invokevirtual 154	java/io/RandomAccessFile:close	()V
      //   981: iload 10
      //   983: ifeq +18 -> 1001
      //   986: aload 16
      //   988: iconst_0
      //   989: iload 6
      //   991: aload_0
      //   992: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   995: getfield 102	org/telegram/messenger/ImageLoader$CacheImage:encryptionKeyPath	Ljava/io/File;
      //   998: invokestatic 266	org/telegram/messenger/secretmedia/EncryptedFileInputStream:decryptBytesWithKeyFile	([BIILjava/io/File;)V
      //   1001: aload 16
      //   1003: iconst_0
      //   1004: iload 6
      //   1006: aload 18
      //   1008: invokestatic 272	android/graphics/BitmapFactory:decodeByteArray	([BIILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   1011: astore 15
      //   1013: goto -182 -> 831
      //   1016: iload 10
      //   1018: ifeq +43 -> 1061
      //   1021: new 262	org/telegram/messenger/secretmedia/EncryptedFileInputStream
      //   1024: dup
      //   1025: aload 23
      //   1027: aload_0
      //   1028: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1031: getfield 102	org/telegram/messenger/ImageLoader$CacheImage:encryptionKeyPath	Ljava/io/File;
      //   1034: invokespecial 275	org/telegram/messenger/secretmedia/EncryptedFileInputStream:<init>	(Ljava/io/File;Ljava/io/File;)V
      //   1037: astore 17
      //   1039: aload 17
      //   1041: aconst_null
      //   1042: aload 18
      //   1044: invokestatic 279	android/graphics/BitmapFactory:decodeStream	(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   1047: astore 15
      //   1049: aload 15
      //   1051: astore 16
      //   1053: aload 17
      //   1055: invokevirtual 282	java/io/FileInputStream:close	()V
      //   1058: goto -227 -> 831
      //   1061: new 281	java/io/FileInputStream
      //   1064: dup
      //   1065: aload 23
      //   1067: invokespecial 285	java/io/FileInputStream:<init>	(Ljava/io/File;)V
      //   1070: astore 17
      //   1072: goto -33 -> 1039
      //   1075: iload 5
      //   1077: iconst_1
      //   1078: if_icmpne +71 -> 1149
      //   1081: aload 15
      //   1083: astore 17
      //   1085: aload 15
      //   1087: astore 16
      //   1089: aload 15
      //   1091: invokevirtual 291	android/graphics/Bitmap:getConfig	()Landroid/graphics/Bitmap$Config;
      //   1094: getstatic 243	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
      //   1097: if_acmpne -627 -> 470
      //   1100: aload 15
      //   1102: astore 16
      //   1104: aload 18
      //   1106: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   1109: ifeq +2185 -> 3294
      //   1112: iconst_0
      //   1113: istore 5
      //   1115: aload 15
      //   1117: astore 16
      //   1119: aload 15
      //   1121: iconst_3
      //   1122: iload 5
      //   1124: aload 15
      //   1126: invokevirtual 294	android/graphics/Bitmap:getWidth	()I
      //   1129: aload 15
      //   1131: invokevirtual 297	android/graphics/Bitmap:getHeight	()I
      //   1134: aload 15
      //   1136: invokevirtual 300	android/graphics/Bitmap:getRowBytes	()I
      //   1139: invokestatic 304	org/telegram/messenger/Utilities:blurBitmap	(Ljava/lang/Object;IIIII)V
      //   1142: aload 15
      //   1144: astore 17
      //   1146: goto -676 -> 470
      //   1149: iload 5
      //   1151: iconst_2
      //   1152: if_icmpne +71 -> 1223
      //   1155: aload 15
      //   1157: astore 17
      //   1159: aload 15
      //   1161: astore 16
      //   1163: aload 15
      //   1165: invokevirtual 291	android/graphics/Bitmap:getConfig	()Landroid/graphics/Bitmap$Config;
      //   1168: getstatic 243	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
      //   1171: if_acmpne -701 -> 470
      //   1174: aload 15
      //   1176: astore 16
      //   1178: aload 18
      //   1180: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   1183: ifeq +2117 -> 3300
      //   1186: iconst_0
      //   1187: istore 5
      //   1189: aload 15
      //   1191: astore 16
      //   1193: aload 15
      //   1195: iconst_1
      //   1196: iload 5
      //   1198: aload 15
      //   1200: invokevirtual 294	android/graphics/Bitmap:getWidth	()I
      //   1203: aload 15
      //   1205: invokevirtual 297	android/graphics/Bitmap:getHeight	()I
      //   1208: aload 15
      //   1210: invokevirtual 300	android/graphics/Bitmap:getRowBytes	()I
      //   1213: invokestatic 304	org/telegram/messenger/Utilities:blurBitmap	(Ljava/lang/Object;IIIII)V
      //   1216: aload 15
      //   1218: astore 17
      //   1220: goto -750 -> 470
      //   1223: iload 5
      //   1225: iconst_3
      //   1226: if_icmpne +158 -> 1384
      //   1229: aload 15
      //   1231: astore 17
      //   1233: aload 15
      //   1235: astore 16
      //   1237: aload 15
      //   1239: invokevirtual 291	android/graphics/Bitmap:getConfig	()Landroid/graphics/Bitmap$Config;
      //   1242: getstatic 243	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
      //   1245: if_acmpne -775 -> 470
      //   1248: aload 15
      //   1250: astore 16
      //   1252: aload 18
      //   1254: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   1257: ifeq +2049 -> 3306
      //   1260: iconst_0
      //   1261: istore 5
      //   1263: aload 15
      //   1265: astore 16
      //   1267: aload 15
      //   1269: bipush 7
      //   1271: iload 5
      //   1273: aload 15
      //   1275: invokevirtual 294	android/graphics/Bitmap:getWidth	()I
      //   1278: aload 15
      //   1280: invokevirtual 297	android/graphics/Bitmap:getHeight	()I
      //   1283: aload 15
      //   1285: invokevirtual 300	android/graphics/Bitmap:getRowBytes	()I
      //   1288: invokestatic 304	org/telegram/messenger/Utilities:blurBitmap	(Ljava/lang/Object;IIIII)V
      //   1291: aload 15
      //   1293: astore 16
      //   1295: aload 18
      //   1297: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   1300: ifeq +2012 -> 3312
      //   1303: iconst_0
      //   1304: istore 5
      //   1306: aload 15
      //   1308: astore 16
      //   1310: aload 15
      //   1312: bipush 7
      //   1314: iload 5
      //   1316: aload 15
      //   1318: invokevirtual 294	android/graphics/Bitmap:getWidth	()I
      //   1321: aload 15
      //   1323: invokevirtual 297	android/graphics/Bitmap:getHeight	()I
      //   1326: aload 15
      //   1328: invokevirtual 300	android/graphics/Bitmap:getRowBytes	()I
      //   1331: invokestatic 304	org/telegram/messenger/Utilities:blurBitmap	(Ljava/lang/Object;IIIII)V
      //   1334: aload 15
      //   1336: astore 16
      //   1338: aload 18
      //   1340: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   1343: ifeq +1975 -> 3318
      //   1346: iconst_0
      //   1347: istore 5
      //   1349: aload 15
      //   1351: astore 16
      //   1353: aload 15
      //   1355: bipush 7
      //   1357: iload 5
      //   1359: aload 15
      //   1361: invokevirtual 294	android/graphics/Bitmap:getWidth	()I
      //   1364: aload 15
      //   1366: invokevirtual 297	android/graphics/Bitmap:getHeight	()I
      //   1369: aload 15
      //   1371: invokevirtual 300	android/graphics/Bitmap:getRowBytes	()I
      //   1374: invokestatic 304	org/telegram/messenger/Utilities:blurBitmap	(Ljava/lang/Object;IIIII)V
      //   1377: aload 15
      //   1379: astore 17
      //   1381: goto -911 -> 470
      //   1384: aload 15
      //   1386: astore 17
      //   1388: iload 5
      //   1390: ifne -920 -> 470
      //   1393: aload 15
      //   1395: astore 17
      //   1397: aload 15
      //   1399: astore 16
      //   1401: aload 18
      //   1403: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   1406: ifeq -936 -> 470
      //   1409: aload 15
      //   1411: astore 16
      //   1413: aload 15
      //   1415: invokestatic 308	org/telegram/messenger/Utilities:pinBitmap	(Landroid/graphics/Bitmap;)I
      //   1418: pop
      //   1419: aload 15
      //   1421: astore 17
      //   1423: goto -953 -> 470
      //   1426: aconst_null
      //   1427: astore 15
      //   1429: aconst_null
      //   1430: astore 16
      //   1432: iload 12
      //   1434: istore 5
      //   1436: aload 21
      //   1438: astore 18
      //   1440: iload 9
      //   1442: istore 8
      //   1444: aload 15
      //   1446: astore 19
      //   1448: aload_0
      //   1449: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1452: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1455: ifnull +1879 -> 3334
      //   1458: aload_0
      //   1459: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1462: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1465: ldc_w 313
      //   1468: invokevirtual 149	java/lang/String:startsWith	(Ljava/lang/String;)Z
      //   1471: ifeq +162 -> 1633
      //   1474: aload_0
      //   1475: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1478: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1481: ldc_w 315
      //   1484: bipush 8
      //   1486: invokevirtual 319	java/lang/String:indexOf	(Ljava/lang/String;I)I
      //   1489: istore 5
      //   1491: aload 20
      //   1493: astore 18
      //   1495: aload 16
      //   1497: astore 15
      //   1499: iload 5
      //   1501: iflt +1823 -> 3324
      //   1504: aload_0
      //   1505: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1508: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1511: bipush 8
      //   1513: iload 5
      //   1515: invokevirtual 323	java/lang/String:substring	(II)Ljava/lang/String;
      //   1518: invokestatic 329	java/lang/Long:parseLong	(Ljava/lang/String;)J
      //   1521: invokestatic 333	java/lang/Long:valueOf	(J)Ljava/lang/Long;
      //   1524: astore 18
      //   1526: aload_0
      //   1527: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1530: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1533: iload 5
      //   1535: iconst_1
      //   1536: iadd
      //   1537: invokevirtual 336	java/lang/String:substring	(I)Ljava/lang/String;
      //   1540: astore 15
      //   1542: goto +1782 -> 3324
      //   1545: iload 6
      //   1547: ifeq +47 -> 1594
      //   1550: aload_0
      //   1551: getfield 27	org/telegram/messenger/ImageLoader$CacheOutTask:this$0	Lorg/telegram/messenger/ImageLoader;
      //   1554: invokestatic 340	org/telegram/messenger/ImageLoader:access$1500	(Lorg/telegram/messenger/ImageLoader;)J
      //   1557: lconst_0
      //   1558: lcmp
      //   1559: ifeq +35 -> 1594
      //   1562: aload_0
      //   1563: getfield 27	org/telegram/messenger/ImageLoader$CacheOutTask:this$0	Lorg/telegram/messenger/ImageLoader;
      //   1566: invokestatic 340	org/telegram/messenger/ImageLoader:access$1500	(Lorg/telegram/messenger/ImageLoader;)J
      //   1569: invokestatic 166	java/lang/System:currentTimeMillis	()J
      //   1572: iload 6
      //   1574: i2l
      //   1575: lsub
      //   1576: lcmp
      //   1577: ifle +17 -> 1594
      //   1580: getstatic 120	android/os/Build$VERSION:SDK_INT	I
      //   1583: bipush 21
      //   1585: if_icmpge +9 -> 1594
      //   1588: iload 6
      //   1590: i2l
      //   1591: invokestatic 344	java/lang/Thread:sleep	(J)V
      //   1594: aload_0
      //   1595: getfield 27	org/telegram/messenger/ImageLoader$CacheOutTask:this$0	Lorg/telegram/messenger/ImageLoader;
      //   1598: invokestatic 166	java/lang/System:currentTimeMillis	()J
      //   1601: invokestatic 170	org/telegram/messenger/ImageLoader:access$1502	(Lorg/telegram/messenger/ImageLoader;J)J
      //   1604: pop2
      //   1605: aload_0
      //   1606: getfield 32	org/telegram/messenger/ImageLoader$CacheOutTask:sync	Ljava/lang/Object;
      //   1609: astore 15
      //   1611: aload 15
      //   1613: monitorenter
      //   1614: aload_0
      //   1615: getfield 53	org/telegram/messenger/ImageLoader$CacheOutTask:isCancelled	Z
      //   1618: ifeq +143 -> 1761
      //   1621: aload 15
      //   1623: monitorexit
      //   1624: return
      //   1625: astore 16
      //   1627: aload 15
      //   1629: monitorexit
      //   1630: aload 16
      //   1632: athrow
      //   1633: aload_0
      //   1634: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1637: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1640: ldc_w 346
      //   1643: invokevirtual 149	java/lang/String:startsWith	(Ljava/lang/String;)Z
      //   1646: ifeq +61 -> 1707
      //   1649: aload_0
      //   1650: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1653: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1656: ldc_w 315
      //   1659: bipush 9
      //   1661: invokevirtual 319	java/lang/String:indexOf	(Ljava/lang/String;I)I
      //   1664: istore 5
      //   1666: aload 22
      //   1668: astore 18
      //   1670: iload 11
      //   1672: istore 8
      //   1674: iload 5
      //   1676: iflt +1681 -> 3357
      //   1679: aload_0
      //   1680: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1683: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1686: bipush 9
      //   1688: iload 5
      //   1690: invokevirtual 323	java/lang/String:substring	(II)Ljava/lang/String;
      //   1693: invokestatic 329	java/lang/Long:parseLong	(Ljava/lang/String;)J
      //   1696: invokestatic 333	java/lang/Long:valueOf	(J)Ljava/lang/Long;
      //   1699: astore 18
      //   1701: iconst_1
      //   1702: istore 8
      //   1704: goto +1653 -> 3357
      //   1707: aload_0
      //   1708: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1711: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   1714: ldc_w 348
      //   1717: invokevirtual 149	java/lang/String:startsWith	(Ljava/lang/String;)Z
      //   1720: istore 14
      //   1722: iload 12
      //   1724: istore 5
      //   1726: aload 21
      //   1728: astore 18
      //   1730: iload 9
      //   1732: istore 8
      //   1734: aload 15
      //   1736: astore 19
      //   1738: iload 14
      //   1740: ifne +1594 -> 3334
      //   1743: iconst_0
      //   1744: istore 5
      //   1746: aload 21
      //   1748: astore 18
      //   1750: iload 9
      //   1752: istore 8
      //   1754: aload 15
      //   1756: astore 19
      //   1758: goto +1576 -> 3334
      //   1761: aload 15
      //   1763: monitorexit
      //   1764: new 190	android/graphics/BitmapFactory$Options
      //   1767: dup
      //   1768: invokespecial 191	android/graphics/BitmapFactory$Options:<init>	()V
      //   1771: astore 20
      //   1773: aload 20
      //   1775: iconst_1
      //   1776: putfield 194	android/graphics/BitmapFactory$Options:inSampleSize	I
      //   1779: fconst_0
      //   1780: fstore_3
      //   1781: fconst_0
      //   1782: fstore 4
      //   1784: fconst_0
      //   1785: fstore_1
      //   1786: fconst_0
      //   1787: fstore_2
      //   1788: iconst_0
      //   1789: istore 11
      //   1791: iconst_0
      //   1792: istore 9
      //   1794: iconst_0
      //   1795: istore 6
      //   1797: aload_0
      //   1798: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1801: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   1804: ifnull +336 -> 2140
      //   1807: aload_0
      //   1808: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1811: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   1814: ldc_w 350
      //   1817: invokevirtual 354	java/lang/String:split	(Ljava/lang/String;)[Ljava/lang/String;
      //   1820: astore 15
      //   1822: fload_2
      //   1823: fstore_3
      //   1824: aload 15
      //   1826: arraylength
      //   1827: iconst_2
      //   1828: if_icmplt +27 -> 1855
      //   1831: aload 15
      //   1833: iconst_0
      //   1834: aaload
      //   1835: invokestatic 360	java/lang/Float:parseFloat	(Ljava/lang/String;)F
      //   1838: getstatic 364	org/telegram/messenger/AndroidUtilities:density	F
      //   1841: fmul
      //   1842: fstore_1
      //   1843: aload 15
      //   1845: iconst_1
      //   1846: aaload
      //   1847: invokestatic 360	java/lang/Float:parseFloat	(Ljava/lang/String;)F
      //   1850: getstatic 364	org/telegram/messenger/AndroidUtilities:density	F
      //   1853: fmul
      //   1854: fstore_3
      //   1855: aload_0
      //   1856: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   1859: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   1862: ldc -68
      //   1864: invokevirtual 160	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
      //   1867: ifeq +6 -> 1873
      //   1870: iconst_1
      //   1871: istore 6
      //   1873: iload 6
      //   1875: istore 9
      //   1877: fload_1
      //   1878: fstore_2
      //   1879: fload_1
      //   1880: fconst_0
      //   1881: fcmpl
      //   1882: ifeq +1394 -> 3276
      //   1885: iload 6
      //   1887: istore 9
      //   1889: fload_1
      //   1890: fstore_2
      //   1891: fload_3
      //   1892: fconst_0
      //   1893: fcmpl
      //   1894: ifeq +1382 -> 3276
      //   1897: aload 20
      //   1899: iconst_1
      //   1900: putfield 219	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
      //   1903: aload 18
      //   1905: ifnull +176 -> 2081
      //   1908: aload 19
      //   1910: ifnonnull +171 -> 2081
      //   1913: iload 8
      //   1915: ifeq +142 -> 2057
      //   1918: getstatic 370	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
      //   1921: invokevirtual 376	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
      //   1924: aload 18
      //   1926: invokevirtual 379	java/lang/Long:longValue	()J
      //   1929: iconst_1
      //   1930: aload 20
      //   1932: invokestatic 385	android/provider/MediaStore$Video$Thumbnails:getThumbnail	(Landroid/content/ContentResolver;JILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   1935: pop
      //   1936: aconst_null
      //   1937: astore 16
      //   1939: aload 16
      //   1941: astore 15
      //   1943: aload 20
      //   1945: getfield 234	android/graphics/BitmapFactory$Options:outWidth	I
      //   1948: i2f
      //   1949: fstore_2
      //   1950: aload 16
      //   1952: astore 15
      //   1954: aload 20
      //   1956: getfield 237	android/graphics/BitmapFactory$Options:outHeight	I
      //   1959: i2f
      //   1960: fstore 4
      //   1962: aload 16
      //   1964: astore 15
      //   1966: fload_2
      //   1967: fload_1
      //   1968: fdiv
      //   1969: fload 4
      //   1971: fload_3
      //   1972: fdiv
      //   1973: invokestatic 391	java/lang/Math:max	(FF)F
      //   1976: fstore_3
      //   1977: fload_3
      //   1978: fstore_2
      //   1979: fload_3
      //   1980: fconst_1
      //   1981: fcmpg
      //   1982: ifge +5 -> 1987
      //   1985: fconst_1
      //   1986: fstore_2
      //   1987: aload 16
      //   1989: astore 15
      //   1991: aload 20
      //   1993: iconst_0
      //   1994: putfield 219	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
      //   1997: aload 16
      //   1999: astore 15
      //   2001: aload 20
      //   2003: fload_2
      //   2004: f2i
      //   2005: putfield 194	android/graphics/BitmapFactory$Options:inSampleSize	I
      //   2008: aload 16
      //   2010: astore 15
      //   2012: aload_0
      //   2013: getfield 32	org/telegram/messenger/ImageLoader$CacheOutTask:sync	Ljava/lang/Object;
      //   2016: astore 17
      //   2018: aload 16
      //   2020: astore 15
      //   2022: aload 17
      //   2024: monitorenter
      //   2025: aload_0
      //   2026: getfield 53	org/telegram/messenger/ImageLoader$CacheOutTask:isCancelled	Z
      //   2029: ifeq +269 -> 2298
      //   2032: aload 17
      //   2034: monitorexit
      //   2035: return
      //   2036: astore 18
      //   2038: aload 17
      //   2040: monitorexit
      //   2041: aload 16
      //   2043: astore 15
      //   2045: aload 18
      //   2047: athrow
      //   2048: astore 16
      //   2050: aload 15
      //   2052: astore 17
      //   2054: goto -1584 -> 470
      //   2057: getstatic 370	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
      //   2060: invokevirtual 376	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
      //   2063: aload 18
      //   2065: invokevirtual 379	java/lang/Long:longValue	()J
      //   2068: iconst_1
      //   2069: aload 20
      //   2071: invokestatic 394	android/provider/MediaStore$Images$Thumbnails:getThumbnail	(Landroid/content/ContentResolver;JILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   2074: pop
      //   2075: aconst_null
      //   2076: astore 16
      //   2078: goto -139 -> 1939
      //   2081: iload 10
      //   2083: ifeq +43 -> 2126
      //   2086: new 262	org/telegram/messenger/secretmedia/EncryptedFileInputStream
      //   2089: dup
      //   2090: aload 23
      //   2092: aload_0
      //   2093: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   2096: getfield 102	org/telegram/messenger/ImageLoader$CacheImage:encryptionKeyPath	Ljava/io/File;
      //   2099: invokespecial 275	org/telegram/messenger/secretmedia/EncryptedFileInputStream:<init>	(Ljava/io/File;Ljava/io/File;)V
      //   2102: astore 17
      //   2104: aload 17
      //   2106: aconst_null
      //   2107: aload 20
      //   2109: invokestatic 279	android/graphics/BitmapFactory:decodeStream	(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   2112: astore 16
      //   2114: aload 16
      //   2116: astore 15
      //   2118: aload 17
      //   2120: invokevirtual 282	java/io/FileInputStream:close	()V
      //   2123: goto -184 -> 1939
      //   2126: new 281	java/io/FileInputStream
      //   2129: dup
      //   2130: aload 23
      //   2132: invokespecial 285	java/io/FileInputStream:<init>	(Ljava/io/File;)V
      //   2135: astore 17
      //   2137: goto -33 -> 2104
      //   2140: fload 4
      //   2142: fstore_2
      //   2143: aload 19
      //   2145: ifnull +1131 -> 3276
      //   2148: aload 20
      //   2150: iconst_1
      //   2151: putfield 219	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
      //   2154: aload 20
      //   2156: getstatic 397	android/graphics/Bitmap$Config:RGB_565	Landroid/graphics/Bitmap$Config;
      //   2159: putfield 400	android/graphics/BitmapFactory$Options:inPreferredConfig	Landroid/graphics/Bitmap$Config;
      //   2162: new 281	java/io/FileInputStream
      //   2165: dup
      //   2166: aload 23
      //   2168: invokespecial 285	java/io/FileInputStream:<init>	(Ljava/io/File;)V
      //   2171: astore 17
      //   2173: aload 17
      //   2175: aconst_null
      //   2176: aload 20
      //   2178: invokestatic 279	android/graphics/BitmapFactory:decodeStream	(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   2181: astore 16
      //   2183: aload 16
      //   2185: astore 15
      //   2187: aload 17
      //   2189: invokevirtual 282	java/io/FileInputStream:close	()V
      //   2192: aload 16
      //   2194: astore 15
      //   2196: aload 20
      //   2198: getfield 234	android/graphics/BitmapFactory$Options:outWidth	I
      //   2201: istore 6
      //   2203: aload 16
      //   2205: astore 15
      //   2207: aload 20
      //   2209: getfield 237	android/graphics/BitmapFactory$Options:outHeight	I
      //   2212: istore 9
      //   2214: aload 16
      //   2216: astore 15
      //   2218: aload 20
      //   2220: iconst_0
      //   2221: putfield 219	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
      //   2224: aload 16
      //   2226: astore 15
      //   2228: iload 6
      //   2230: sipush 200
      //   2233: idiv
      //   2234: iload 9
      //   2236: sipush 200
      //   2239: idiv
      //   2240: invokestatic 403	java/lang/Math:max	(II)I
      //   2243: i2f
      //   2244: fstore_2
      //   2245: fload_2
      //   2246: fstore_1
      //   2247: fload_2
      //   2248: fconst_1
      //   2249: fcmpg
      //   2250: ifge +1117 -> 3367
      //   2253: fconst_1
      //   2254: fstore_1
      //   2255: goto +1112 -> 3367
      //   2258: iload 6
      //   2260: iconst_2
      //   2261: imul
      //   2262: istore 9
      //   2264: iload 9
      //   2266: istore 6
      //   2268: iload 9
      //   2270: iconst_2
      //   2271: imul
      //   2272: i2f
      //   2273: fload_1
      //   2274: fcmpg
      //   2275: iflt -17 -> 2258
      //   2278: aload 16
      //   2280: astore 15
      //   2282: aload 20
      //   2284: iload 9
      //   2286: putfield 194	android/graphics/BitmapFactory$Options:inSampleSize	I
      //   2289: iload 11
      //   2291: istore 6
      //   2293: fload_3
      //   2294: fstore_1
      //   2295: goto -287 -> 2008
      //   2298: aload 17
      //   2300: monitorexit
      //   2301: aload 16
      //   2303: astore 15
      //   2305: aload_0
      //   2306: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   2309: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   2312: ifnull +22 -> 2334
      //   2315: iload 6
      //   2317: ifne +17 -> 2334
      //   2320: aload 16
      //   2322: astore 15
      //   2324: aload_0
      //   2325: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   2328: getfield 311	org/telegram/messenger/ImageLoader$CacheImage:httpUrl	Ljava/lang/String;
      //   2331: ifnull +326 -> 2657
      //   2334: aload 16
      //   2336: astore 15
      //   2338: aload 20
      //   2340: getstatic 243	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
      //   2343: putfield 400	android/graphics/BitmapFactory$Options:inPreferredConfig	Landroid/graphics/Bitmap$Config;
      //   2346: aload 16
      //   2348: astore 15
      //   2350: getstatic 120	android/os/Build$VERSION:SDK_INT	I
      //   2353: bipush 21
      //   2355: if_icmpge +13 -> 2368
      //   2358: aload 16
      //   2360: astore 15
      //   2362: aload 20
      //   2364: iconst_1
      //   2365: putfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   2368: aload 16
      //   2370: astore 15
      //   2372: aload 20
      //   2374: iconst_0
      //   2375: putfield 406	android/graphics/BitmapFactory$Options:inDither	Z
      //   2378: aload 16
      //   2380: astore 17
      //   2382: aload 18
      //   2384: ifnull +40 -> 2424
      //   2387: aload 16
      //   2389: astore 17
      //   2391: aload 19
      //   2393: ifnonnull +31 -> 2424
      //   2396: iload 8
      //   2398: ifeq +274 -> 2672
      //   2401: aload 16
      //   2403: astore 15
      //   2405: getstatic 370	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
      //   2408: invokevirtual 376	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
      //   2411: aload 18
      //   2413: invokevirtual 379	java/lang/Long:longValue	()J
      //   2416: iconst_1
      //   2417: aload 20
      //   2419: invokestatic 385	android/provider/MediaStore$Video$Thumbnails:getThumbnail	(Landroid/content/ContentResolver;JILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   2422: astore 17
      //   2424: aload 17
      //   2426: astore 16
      //   2428: aload 17
      //   2430: ifnonnull +164 -> 2594
      //   2433: iload 7
      //   2435: ifeq +263 -> 2698
      //   2438: aload 17
      //   2440: astore 15
      //   2442: new 122	java/io/RandomAccessFile
      //   2445: dup
      //   2446: aload 23
      //   2448: ldc 124
      //   2450: invokespecial 127	java/io/RandomAccessFile:<init>	(Ljava/io/File;Ljava/lang/String;)V
      //   2453: astore 18
      //   2455: aload 17
      //   2457: astore 15
      //   2459: aload 18
      //   2461: invokevirtual 201	java/io/RandomAccessFile:getChannel	()Ljava/nio/channels/FileChannel;
      //   2464: getstatic 207	java/nio/channels/FileChannel$MapMode:READ_ONLY	Ljava/nio/channels/FileChannel$MapMode;
      //   2467: lconst_0
      //   2468: aload 23
      //   2470: invokevirtual 210	java/io/File:length	()J
      //   2473: invokevirtual 216	java/nio/channels/FileChannel:map	(Ljava/nio/channels/FileChannel$MapMode;JJ)Ljava/nio/MappedByteBuffer;
      //   2476: astore 19
      //   2478: aload 17
      //   2480: astore 15
      //   2482: new 190	android/graphics/BitmapFactory$Options
      //   2485: dup
      //   2486: invokespecial 191	android/graphics/BitmapFactory$Options:<init>	()V
      //   2489: astore 16
      //   2491: aload 17
      //   2493: astore 15
      //   2495: aload 16
      //   2497: iconst_1
      //   2498: putfield 219	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
      //   2501: aload 17
      //   2503: astore 15
      //   2505: aconst_null
      //   2506: aload 19
      //   2508: aload 19
      //   2510: invokevirtual 225	java/nio/ByteBuffer:limit	()I
      //   2513: aload 16
      //   2515: iconst_1
      //   2516: invokestatic 231	org/telegram/messenger/Utilities:loadWebpImage	(Landroid/graphics/Bitmap;Ljava/nio/ByteBuffer;ILandroid/graphics/BitmapFactory$Options;Z)Z
      //   2519: pop
      //   2520: aload 17
      //   2522: astore 15
      //   2524: aload 16
      //   2526: getfield 234	android/graphics/BitmapFactory$Options:outWidth	I
      //   2529: aload 16
      //   2531: getfield 237	android/graphics/BitmapFactory$Options:outHeight	I
      //   2534: getstatic 243	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
      //   2537: invokestatic 249	org/telegram/messenger/Bitmaps:createBitmap	(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;
      //   2540: astore 16
      //   2542: aload 16
      //   2544: astore 15
      //   2546: aload 19
      //   2548: invokevirtual 225	java/nio/ByteBuffer:limit	()I
      //   2551: istore 7
      //   2553: aload 16
      //   2555: astore 15
      //   2557: aload 20
      //   2559: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   2562: ifne +811 -> 3373
      //   2565: iconst_1
      //   2566: istore 14
      //   2568: aload 16
      //   2570: astore 15
      //   2572: aload 16
      //   2574: aload 19
      //   2576: iload 7
      //   2578: aconst_null
      //   2579: iload 14
      //   2581: invokestatic 231	org/telegram/messenger/Utilities:loadWebpImage	(Landroid/graphics/Bitmap;Ljava/nio/ByteBuffer;ILandroid/graphics/BitmapFactory$Options;Z)Z
      //   2584: pop
      //   2585: aload 16
      //   2587: astore 15
      //   2589: aload 18
      //   2591: invokevirtual 154	java/io/RandomAccessFile:close	()V
      //   2594: aload 16
      //   2596: ifnonnull +349 -> 2945
      //   2599: aload 16
      //   2601: astore 17
      //   2603: iload 5
      //   2605: ifeq -2135 -> 470
      //   2608: aload 16
      //   2610: astore 15
      //   2612: aload 23
      //   2614: invokevirtual 210	java/io/File:length	()J
      //   2617: lconst_0
      //   2618: lcmp
      //   2619: ifeq +21 -> 2640
      //   2622: aload 16
      //   2624: astore 17
      //   2626: aload 16
      //   2628: astore 15
      //   2630: aload_0
      //   2631: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   2634: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   2637: ifnonnull -2167 -> 470
      //   2640: aload 16
      //   2642: astore 15
      //   2644: aload 23
      //   2646: invokevirtual 252	java/io/File:delete	()Z
      //   2649: pop
      //   2650: aload 16
      //   2652: astore 17
      //   2654: goto -2184 -> 470
      //   2657: aload 16
      //   2659: astore 15
      //   2661: aload 20
      //   2663: getstatic 397	android/graphics/Bitmap$Config:RGB_565	Landroid/graphics/Bitmap$Config;
      //   2666: putfield 400	android/graphics/BitmapFactory$Options:inPreferredConfig	Landroid/graphics/Bitmap$Config;
      //   2669: goto -323 -> 2346
      //   2672: aload 16
      //   2674: astore 15
      //   2676: getstatic 370	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
      //   2679: invokevirtual 376	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
      //   2682: aload 18
      //   2684: invokevirtual 379	java/lang/Long:longValue	()J
      //   2687: iconst_1
      //   2688: aload 20
      //   2690: invokestatic 394	android/provider/MediaStore$Images$Thumbnails:getThumbnail	(Landroid/content/ContentResolver;JILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   2693: astore 17
      //   2695: goto -271 -> 2424
      //   2698: aload 17
      //   2700: astore 15
      //   2702: aload 20
      //   2704: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   2707: ifeq +163 -> 2870
      //   2710: aload 17
      //   2712: astore 15
      //   2714: new 122	java/io/RandomAccessFile
      //   2717: dup
      //   2718: aload 23
      //   2720: ldc 124
      //   2722: invokespecial 127	java/io/RandomAccessFile:<init>	(Ljava/io/File;Ljava/lang/String;)V
      //   2725: astore 18
      //   2727: aload 17
      //   2729: astore 15
      //   2731: aload 18
      //   2733: invokevirtual 253	java/io/RandomAccessFile:length	()J
      //   2736: l2i
      //   2737: istore 7
      //   2739: aload 17
      //   2741: astore 15
      //   2743: invokestatic 409	org/telegram/messenger/ImageLoader:access$1700	()[B
      //   2746: ifnull +633 -> 3379
      //   2749: aload 17
      //   2751: astore 15
      //   2753: invokestatic 409	org/telegram/messenger/ImageLoader:access$1700	()[B
      //   2756: arraylength
      //   2757: iload 7
      //   2759: if_icmplt +620 -> 3379
      //   2762: aload 17
      //   2764: astore 15
      //   2766: invokestatic 409	org/telegram/messenger/ImageLoader:access$1700	()[B
      //   2769: astore 16
      //   2771: aload 16
      //   2773: astore 15
      //   2775: aload 15
      //   2777: astore 16
      //   2779: aload 15
      //   2781: ifnonnull +23 -> 2804
      //   2784: aload 17
      //   2786: astore 15
      //   2788: iload 7
      //   2790: newarray <illegal type>
      //   2792: astore 16
      //   2794: aload 17
      //   2796: astore 15
      //   2798: aload 16
      //   2800: invokestatic 412	org/telegram/messenger/ImageLoader:access$1702	([B)[B
      //   2803: pop
      //   2804: aload 17
      //   2806: astore 15
      //   2808: aload 18
      //   2810: aload 16
      //   2812: iconst_0
      //   2813: iload 7
      //   2815: invokevirtual 138	java/io/RandomAccessFile:readFully	([BII)V
      //   2818: aload 17
      //   2820: astore 15
      //   2822: aload 18
      //   2824: invokevirtual 154	java/io/RandomAccessFile:close	()V
      //   2827: iload 10
      //   2829: ifeq +22 -> 2851
      //   2832: aload 17
      //   2834: astore 15
      //   2836: aload 16
      //   2838: iconst_0
      //   2839: iload 7
      //   2841: aload_0
      //   2842: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   2845: getfield 102	org/telegram/messenger/ImageLoader$CacheImage:encryptionKeyPath	Ljava/io/File;
      //   2848: invokestatic 266	org/telegram/messenger/secretmedia/EncryptedFileInputStream:decryptBytesWithKeyFile	([BIILjava/io/File;)V
      //   2851: aload 17
      //   2853: astore 15
      //   2855: aload 16
      //   2857: iconst_0
      //   2858: iload 7
      //   2860: aload 20
      //   2862: invokestatic 272	android/graphics/BitmapFactory:decodeByteArray	([BIILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   2865: astore 16
      //   2867: goto -273 -> 2594
      //   2870: iload 10
      //   2872: ifeq +55 -> 2927
      //   2875: aload 17
      //   2877: astore 15
      //   2879: new 262	org/telegram/messenger/secretmedia/EncryptedFileInputStream
      //   2882: dup
      //   2883: aload 23
      //   2885: aload_0
      //   2886: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   2889: getfield 102	org/telegram/messenger/ImageLoader$CacheImage:encryptionKeyPath	Ljava/io/File;
      //   2892: invokespecial 275	org/telegram/messenger/secretmedia/EncryptedFileInputStream:<init>	(Ljava/io/File;Ljava/io/File;)V
      //   2895: astore 16
      //   2897: aload 17
      //   2899: astore 15
      //   2901: aload 16
      //   2903: aconst_null
      //   2904: aload 20
      //   2906: invokestatic 279	android/graphics/BitmapFactory:decodeStream	(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
      //   2909: astore 17
      //   2911: aload 17
      //   2913: astore 15
      //   2915: aload 16
      //   2917: invokevirtual 282	java/io/FileInputStream:close	()V
      //   2920: aload 17
      //   2922: astore 16
      //   2924: goto -330 -> 2594
      //   2927: aload 17
      //   2929: astore 15
      //   2931: new 281	java/io/FileInputStream
      //   2934: dup
      //   2935: aload 23
      //   2937: invokespecial 285	java/io/FileInputStream:<init>	(Ljava/io/File;)V
      //   2940: astore 16
      //   2942: goto -45 -> 2897
      //   2945: iconst_0
      //   2946: istore 7
      //   2948: aload 16
      //   2950: astore 15
      //   2952: aload 16
      //   2954: astore 18
      //   2956: iload 7
      //   2958: istore 5
      //   2960: aload_0
      //   2961: getfield 34	org/telegram/messenger/ImageLoader$CacheOutTask:cacheImage	Lorg/telegram/messenger/ImageLoader$CacheImage;
      //   2964: getfield 84	org/telegram/messenger/ImageLoader$CacheImage:filter	Ljava/lang/String;
      //   2967: ifnull +245 -> 3212
      //   2970: aload 16
      //   2972: astore 15
      //   2974: aload 16
      //   2976: invokevirtual 294	android/graphics/Bitmap:getWidth	()I
      //   2979: i2f
      //   2980: fstore_2
      //   2981: aload 16
      //   2983: astore 15
      //   2985: aload 16
      //   2987: invokevirtual 297	android/graphics/Bitmap:getHeight	()I
      //   2990: i2f
      //   2991: fstore_3
      //   2992: aload 16
      //   2994: astore 15
      //   2996: aload 16
      //   2998: astore 17
      //   3000: aload 20
      //   3002: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   3005: ifne +89 -> 3094
      //   3008: aload 16
      //   3010: astore 17
      //   3012: fload_1
      //   3013: fconst_0
      //   3014: fcmpl
      //   3015: ifeq +79 -> 3094
      //   3018: aload 16
      //   3020: astore 17
      //   3022: fload_2
      //   3023: fload_1
      //   3024: fcmpl
      //   3025: ifeq +69 -> 3094
      //   3028: aload 16
      //   3030: astore 17
      //   3032: fload_2
      //   3033: ldc_w 413
      //   3036: fload_1
      //   3037: fadd
      //   3038: fcmpl
      //   3039: ifle +55 -> 3094
      //   3042: aload 16
      //   3044: astore 15
      //   3046: fload_2
      //   3047: fload_1
      //   3048: fdiv
      //   3049: fstore 4
      //   3051: aload 16
      //   3053: astore 15
      //   3055: aload 16
      //   3057: fload_1
      //   3058: f2i
      //   3059: fload_3
      //   3060: fload 4
      //   3062: fdiv
      //   3063: f2i
      //   3064: iconst_1
      //   3065: invokestatic 417	org/telegram/messenger/Bitmaps:createScaledBitmap	(Landroid/graphics/Bitmap;IIZ)Landroid/graphics/Bitmap;
      //   3068: astore 18
      //   3070: aload 16
      //   3072: astore 17
      //   3074: aload 16
      //   3076: aload 18
      //   3078: if_acmpeq +16 -> 3094
      //   3081: aload 16
      //   3083: astore 15
      //   3085: aload 16
      //   3087: invokevirtual 420	android/graphics/Bitmap:recycle	()V
      //   3090: aload 18
      //   3092: astore 17
      //   3094: aload 17
      //   3096: astore 18
      //   3098: iload 7
      //   3100: istore 5
      //   3102: aload 17
      //   3104: ifnull +108 -> 3212
      //   3107: aload 17
      //   3109: astore 18
      //   3111: iload 7
      //   3113: istore 5
      //   3115: iload 6
      //   3117: ifeq +95 -> 3212
      //   3120: aload 17
      //   3122: astore 18
      //   3124: iload 7
      //   3126: istore 5
      //   3128: fload_3
      //   3129: ldc_w 421
      //   3132: fcmpg
      //   3133: ifge +79 -> 3212
      //   3136: aload 17
      //   3138: astore 18
      //   3140: iload 7
      //   3142: istore 5
      //   3144: fload_2
      //   3145: ldc_w 421
      //   3148: fcmpg
      //   3149: ifge +63 -> 3212
      //   3152: aload 17
      //   3154: astore 15
      //   3156: aload 17
      //   3158: invokevirtual 291	android/graphics/Bitmap:getConfig	()Landroid/graphics/Bitmap$Config;
      //   3161: getstatic 243	android/graphics/Bitmap$Config:ARGB_8888	Landroid/graphics/Bitmap$Config;
      //   3164: if_acmpne +221 -> 3385
      //   3167: aload 17
      //   3169: astore 15
      //   3171: aload 20
      //   3173: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   3176: ifeq +78 -> 3254
      //   3179: iconst_0
      //   3180: istore 5
      //   3182: aload 17
      //   3184: astore 15
      //   3186: aload 17
      //   3188: iconst_3
      //   3189: iload 5
      //   3191: aload 17
      //   3193: invokevirtual 294	android/graphics/Bitmap:getWidth	()I
      //   3196: aload 17
      //   3198: invokevirtual 297	android/graphics/Bitmap:getHeight	()I
      //   3201: aload 17
      //   3203: invokevirtual 300	android/graphics/Bitmap:getRowBytes	()I
      //   3206: invokestatic 304	org/telegram/messenger/Utilities:blurBitmap	(Ljava/lang/Object;IIIII)V
      //   3209: goto +176 -> 3385
      //   3212: aload 18
      //   3214: astore 17
      //   3216: iload 5
      //   3218: ifne -2748 -> 470
      //   3221: aload 18
      //   3223: astore 17
      //   3225: aload 18
      //   3227: astore 15
      //   3229: aload 20
      //   3231: getfield 197	android/graphics/BitmapFactory$Options:inPurgeable	Z
      //   3234: ifeq -2764 -> 470
      //   3237: aload 18
      //   3239: astore 15
      //   3241: aload 18
      //   3243: invokestatic 308	org/telegram/messenger/Utilities:pinBitmap	(Landroid/graphics/Bitmap;)I
      //   3246: pop
      //   3247: aload 18
      //   3249: astore 17
      //   3251: goto -2781 -> 470
      //   3254: iconst_1
      //   3255: istore 5
      //   3257: goto -75 -> 3182
      //   3260: aconst_null
      //   3261: astore 15
      //   3263: goto -2773 -> 490
      //   3266: astore 15
      //   3268: goto -2675 -> 593
      //   3271: astore 17
      //   3273: goto -2734 -> 539
      //   3276: aconst_null
      //   3277: astore 16
      //   3279: iload 9
      //   3281: istore 6
      //   3283: fload_2
      //   3284: fstore_1
      //   3285: goto -1277 -> 2008
      //   3288: aconst_null
      //   3289: astore 15
      //   3291: goto -2346 -> 945
      //   3294: iconst_1
      //   3295: istore 5
      //   3297: goto -2182 -> 1115
      //   3300: iconst_1
      //   3301: istore 5
      //   3303: goto -2114 -> 1189
      //   3306: iconst_1
      //   3307: istore 5
      //   3309: goto -2046 -> 1263
      //   3312: iconst_1
      //   3313: istore 5
      //   3315: goto -2009 -> 1306
      //   3318: iconst_1
      //   3319: istore 5
      //   3321: goto -1972 -> 1349
      //   3324: iconst_0
      //   3325: istore 8
      //   3327: iconst_0
      //   3328: istore 5
      //   3330: aload 15
      //   3332: astore 19
      //   3334: bipush 20
      //   3336: istore 6
      //   3338: aload 18
      //   3340: ifnull -1795 -> 1545
      //   3343: iconst_0
      //   3344: istore 6
      //   3346: goto -1801 -> 1545
      //   3349: astore 15
      //   3351: aconst_null
      //   3352: astore 17
      //   3354: goto -2884 -> 470
      //   3357: iconst_0
      //   3358: istore 5
      //   3360: aload 15
      //   3362: astore 19
      //   3364: goto -30 -> 3334
      //   3367: iconst_1
      //   3368: istore 6
      //   3370: goto -1112 -> 2258
      //   3373: iconst_0
      //   3374: istore 14
      //   3376: goto -808 -> 2568
      //   3379: aconst_null
      //   3380: astore 15
      //   3382: goto -607 -> 2775
      //   3385: iconst_1
      //   3386: istore 5
      //   3388: aload 17
      //   3390: astore 18
      //   3392: goto -180 -> 3212
      // Local variable table:
      //   start	length	slot	name	signature
      //   0	3395	0	this	CacheOutTask
      //   1785	1500	1	f1	float
      //   1787	1497	2	f2	float
      //   1780	1349	3	f3	float
      //   1782	1279	4	f4	float
      //   308	3079	5	i	int
      //   212	3157	6	j	int
      //   209	2932	7	k	int
      //   218	3108	8	m	int
      //   163	3117	9	n	int
      //   203	2668	10	i1	int
      //   160	2130	11	i2	int
      //   206	1517	12	i3	int
      //   215	289	13	i4	int
      //   118	3257	14	bool	boolean
      //   456	6	15	localThrowable1	Throwable
      //   488	23	15	localObject2	Object
      //   515	3	15	localException1	Exception
      //   529	7	15	localException2	Exception
      //   541	1	15	localObject3	Object
      //   569	17	15	localException3	Exception
      //   591	288	15	localObject4	Object
      //   885	1	15	localThrowable2	Throwable
      //   3266	1	15	localObject6	Object
      //   3289	42	15	localObject7	Object
      //   3349	12	15	localThrowable3	Throwable
      //   3380	1	15	localObject8	Object
      //   64	6	16	localObject9	Object
      //   72	6	16	localObject10	Object
      //   245	119	16	localRandomAccessFile	RandomAccessFile
      //   448	6	16	localObject11	Object
      //   459	140	16	localObject12	Object
      //   606	3	16	localException4	Exception
      //   781	715	16	localObject13	Object
      //   1625	6	16	localObject14	Object
      //   1937	105	16	localObject15	Object
      //   2048	1	16	localThrowable4	Throwable
      //   2076	1202	16	localObject16	Object
      //   232	312	17	localObject17	Object
      //   583	7	17	localObject18	Object
      //   710	2540	17	localObject19	Object
      //   3271	1	17	localException5	Exception
      //   3352	37	17	localObject20	Object
      //   672	1253	18	localObject21	Object
      //   2036	376	18	localObject22	Object
      //   2453	938	18	localObject23	Object
      //   729	2634	19	localObject24	Object
      //   157	3073	20	localOptions	android.graphics.BitmapFactory.Options
      //   151	1596	21	localObject25	Object
      //   154	1513	22	localObject26	Object
      //   172	2764	23	localFile	File
      // Exception table:
      //   from	to	target	type
      //   53	63	64	finally
      //   66	69	64	finally
      //   80	83	64	finally
      //   9	30	72	finally
      //   31	34	72	finally
      //   74	77	72	finally
      //   437	447	448	finally
      //   450	453	448	finally
      //   662	665	448	finally
      //   417	437	456	java/lang/Throwable
      //   453	456	456	java/lang/Throwable
      //   665	694	456	java/lang/Throwable
      //   699	779	456	java/lang/Throwable
      //   896	945	456	java/lang/Throwable
      //   954	966	456	java/lang/Throwable
      //   966	981	456	java/lang/Throwable
      //   986	1001	456	java/lang/Throwable
      //   1001	1013	456	java/lang/Throwable
      //   1021	1039	456	java/lang/Throwable
      //   1039	1049	456	java/lang/Throwable
      //   1061	1072	456	java/lang/Throwable
      //   363	368	515	java/lang/Exception
      //   234	247	529	java/lang/Exception
      //   557	562	569	java/lang/Exception
      //   234	247	583	finally
      //   543	548	583	finally
      //   598	603	606	java/lang/Exception
      //   783	790	885	java/lang/Throwable
      //   794	802	885	java/lang/Throwable
      //   809	822	885	java/lang/Throwable
      //   826	831	885	java/lang/Throwable
      //   840	850	885	java/lang/Throwable
      //   858	868	885	java/lang/Throwable
      //   872	878	885	java/lang/Throwable
      //   1053	1058	885	java/lang/Throwable
      //   1089	1100	885	java/lang/Throwable
      //   1104	1112	885	java/lang/Throwable
      //   1119	1142	885	java/lang/Throwable
      //   1163	1174	885	java/lang/Throwable
      //   1178	1186	885	java/lang/Throwable
      //   1193	1216	885	java/lang/Throwable
      //   1237	1248	885	java/lang/Throwable
      //   1252	1260	885	java/lang/Throwable
      //   1267	1291	885	java/lang/Throwable
      //   1295	1303	885	java/lang/Throwable
      //   1310	1334	885	java/lang/Throwable
      //   1338	1346	885	java/lang/Throwable
      //   1353	1377	885	java/lang/Throwable
      //   1401	1409	885	java/lang/Throwable
      //   1413	1419	885	java/lang/Throwable
      //   1614	1624	1625	finally
      //   1627	1630	1625	finally
      //   1761	1764	1625	finally
      //   2025	2035	2036	finally
      //   2038	2041	2036	finally
      //   2298	2301	2036	finally
      //   1943	1950	2048	java/lang/Throwable
      //   1954	1962	2048	java/lang/Throwable
      //   1966	1977	2048	java/lang/Throwable
      //   1991	1997	2048	java/lang/Throwable
      //   2001	2008	2048	java/lang/Throwable
      //   2012	2018	2048	java/lang/Throwable
      //   2022	2025	2048	java/lang/Throwable
      //   2045	2048	2048	java/lang/Throwable
      //   2118	2123	2048	java/lang/Throwable
      //   2187	2192	2048	java/lang/Throwable
      //   2196	2203	2048	java/lang/Throwable
      //   2207	2214	2048	java/lang/Throwable
      //   2218	2224	2048	java/lang/Throwable
      //   2228	2245	2048	java/lang/Throwable
      //   2282	2289	2048	java/lang/Throwable
      //   2305	2315	2048	java/lang/Throwable
      //   2324	2334	2048	java/lang/Throwable
      //   2338	2346	2048	java/lang/Throwable
      //   2350	2358	2048	java/lang/Throwable
      //   2362	2368	2048	java/lang/Throwable
      //   2372	2378	2048	java/lang/Throwable
      //   2405	2424	2048	java/lang/Throwable
      //   2442	2455	2048	java/lang/Throwable
      //   2459	2478	2048	java/lang/Throwable
      //   2482	2491	2048	java/lang/Throwable
      //   2495	2501	2048	java/lang/Throwable
      //   2505	2520	2048	java/lang/Throwable
      //   2524	2542	2048	java/lang/Throwable
      //   2546	2553	2048	java/lang/Throwable
      //   2557	2565	2048	java/lang/Throwable
      //   2572	2585	2048	java/lang/Throwable
      //   2589	2594	2048	java/lang/Throwable
      //   2612	2622	2048	java/lang/Throwable
      //   2630	2640	2048	java/lang/Throwable
      //   2644	2650	2048	java/lang/Throwable
      //   2661	2669	2048	java/lang/Throwable
      //   2676	2695	2048	java/lang/Throwable
      //   2702	2710	2048	java/lang/Throwable
      //   2714	2727	2048	java/lang/Throwable
      //   2731	2739	2048	java/lang/Throwable
      //   2743	2749	2048	java/lang/Throwable
      //   2753	2762	2048	java/lang/Throwable
      //   2766	2771	2048	java/lang/Throwable
      //   2788	2794	2048	java/lang/Throwable
      //   2798	2804	2048	java/lang/Throwable
      //   2808	2818	2048	java/lang/Throwable
      //   2822	2827	2048	java/lang/Throwable
      //   2836	2851	2048	java/lang/Throwable
      //   2855	2867	2048	java/lang/Throwable
      //   2879	2897	2048	java/lang/Throwable
      //   2901	2911	2048	java/lang/Throwable
      //   2915	2920	2048	java/lang/Throwable
      //   2931	2942	2048	java/lang/Throwable
      //   2960	2970	2048	java/lang/Throwable
      //   2974	2981	2048	java/lang/Throwable
      //   2985	2992	2048	java/lang/Throwable
      //   3000	3008	2048	java/lang/Throwable
      //   3046	3051	2048	java/lang/Throwable
      //   3055	3070	2048	java/lang/Throwable
      //   3085	3090	2048	java/lang/Throwable
      //   3156	3167	2048	java/lang/Throwable
      //   3171	3179	2048	java/lang/Throwable
      //   3186	3209	2048	java/lang/Throwable
      //   3229	3237	2048	java/lang/Throwable
      //   3241	3247	2048	java/lang/Throwable
      //   251	261	3266	finally
      //   265	270	3266	finally
      //   274	285	3266	finally
      //   289	306	3266	finally
      //   314	324	3266	finally
      //   332	342	3266	finally
      //   349	354	3266	finally
      //   507	512	3266	finally
      //   251	261	3271	java/lang/Exception
      //   265	270	3271	java/lang/Exception
      //   274	285	3271	java/lang/Exception
      //   289	306	3271	java/lang/Exception
      //   314	324	3271	java/lang/Exception
      //   332	342	3271	java/lang/Exception
      //   349	354	3271	java/lang/Exception
      //   507	512	3271	java/lang/Exception
      //   1448	1491	3349	java/lang/Throwable
      //   1504	1542	3349	java/lang/Throwable
      //   1550	1594	3349	java/lang/Throwable
      //   1594	1614	3349	java/lang/Throwable
      //   1630	1633	3349	java/lang/Throwable
      //   1633	1666	3349	java/lang/Throwable
      //   1679	1701	3349	java/lang/Throwable
      //   1707	1722	3349	java/lang/Throwable
      //   1764	1779	3349	java/lang/Throwable
      //   1797	1822	3349	java/lang/Throwable
      //   1824	1855	3349	java/lang/Throwable
      //   1855	1870	3349	java/lang/Throwable
      //   1897	1903	3349	java/lang/Throwable
      //   1918	1936	3349	java/lang/Throwable
      //   2057	2075	3349	java/lang/Throwable
      //   2086	2104	3349	java/lang/Throwable
      //   2104	2114	3349	java/lang/Throwable
      //   2126	2137	3349	java/lang/Throwable
      //   2148	2183	3349	java/lang/Throwable
    }
  }
  
  private class HttpFileTask
    extends AsyncTask<Void, Void, Boolean>
  {
    private boolean canRetry = true;
    private int currentAccount;
    private String ext;
    private RandomAccessFile fileOutputStream = null;
    private int fileSize;
    private long lastProgressTime;
    private File tempFile;
    private String url;
    
    public HttpFileTask(String paramString1, File paramFile, String paramString2, int paramInt)
    {
      this.url = paramString1;
      this.tempFile = paramFile;
      this.ext = paramString2;
      this.currentAccount = paramInt;
    }
    
    private void reportProgress(final float paramFloat)
    {
      long l = System.currentTimeMillis();
      if ((paramFloat == 1.0F) || (this.lastProgressTime == 0L) || (this.lastProgressTime < l - 500L))
      {
        this.lastProgressTime = l;
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            ImageLoader.this.fileProgresses.put(ImageLoader.HttpFileTask.this.url, Float.valueOf(paramFloat));
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(ImageLoader.HttpFileTask.this.currentAccount).postNotificationName(NotificationCenter.FileLoadProgressChanged, new Object[] { ImageLoader.HttpFileTask.this.url, Float.valueOf(ImageLoader.HttpFileTask.1.this.val$progress) });
              }
            });
          }
        });
      }
    }
    
    protected Boolean doInBackground(Void... paramVarArgs)
    {
      Object localObject4 = null;
      bool2 = false;
      bool5 = false;
      bool6 = false;
      bool4 = false;
      paramVarArgs = null;
      localObject2 = localObject4;
      try
      {
        Object localObject3 = new URL(this.url).openConnection();
        paramVarArgs = (Void[])localObject3;
        localObject2 = localObject4;
        ((URLConnection)localObject3).addRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1");
        paramVarArgs = (Void[])localObject3;
        localObject2 = localObject4;
        ((URLConnection)localObject3).setConnectTimeout(5000);
        paramVarArgs = (Void[])localObject3;
        localObject2 = localObject4;
        ((URLConnection)localObject3).setReadTimeout(5000);
        Object localObject1 = localObject3;
        paramVarArgs = (Void[])localObject3;
        localObject2 = localObject4;
        if ((localObject3 instanceof HttpURLConnection))
        {
          paramVarArgs = (Void[])localObject3;
          localObject2 = localObject4;
          Object localObject5 = (HttpURLConnection)localObject3;
          paramVarArgs = (Void[])localObject3;
          localObject2 = localObject4;
          ((HttpURLConnection)localObject5).setInstanceFollowRedirects(true);
          paramVarArgs = (Void[])localObject3;
          localObject2 = localObject4;
          i = ((HttpURLConnection)localObject5).getResponseCode();
          if ((i != 302) && (i != 301))
          {
            localObject1 = localObject3;
            if (i != 303) {}
          }
          else
          {
            paramVarArgs = (Void[])localObject3;
            localObject2 = localObject4;
            localObject1 = ((HttpURLConnection)localObject5).getHeaderField("Location");
            paramVarArgs = (Void[])localObject3;
            localObject2 = localObject4;
            localObject5 = ((HttpURLConnection)localObject5).getHeaderField("Set-Cookie");
            paramVarArgs = (Void[])localObject3;
            localObject2 = localObject4;
            localObject1 = new URL((String)localObject1).openConnection();
            paramVarArgs = (Void[])localObject1;
            localObject2 = localObject4;
            ((URLConnection)localObject1).setRequestProperty("Cookie", (String)localObject5);
            paramVarArgs = (Void[])localObject1;
            localObject2 = localObject4;
            ((URLConnection)localObject1).addRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1");
          }
        }
        paramVarArgs = (Void[])localObject1;
        localObject2 = localObject4;
        ((URLConnection)localObject1).connect();
        paramVarArgs = (Void[])localObject1;
        localObject2 = localObject4;
        localObject3 = ((URLConnection)localObject1).getInputStream();
        paramVarArgs = (Void[])localObject1;
        localObject2 = localObject3;
        this.fileOutputStream = new RandomAccessFile(this.tempFile, "rws");
        localObject2 = localObject3;
        paramVarArgs = (Void[])localObject1;
      }
      catch (Throwable paramVarArgs)
      {
        try
        {
          if (!(paramVarArgs instanceof HttpURLConnection)) {
            break label361;
          }
          i = ((HttpURLConnection)paramVarArgs).getResponseCode();
          if ((i == 200) || (i == 202) || (i == 304)) {
            break label361;
          }
          this.canRetry = false;
          if (paramVarArgs == null) {
            break label425;
          }
        }
        catch (Exception paramVarArgs)
        {
          try
          {
            paramVarArgs = paramVarArgs.getHeaderFields();
            if (paramVarArgs == null) {
              break label425;
            }
            paramVarArgs = (List)paramVarArgs.get("content-Length");
            if ((paramVarArgs == null) || (paramVarArgs.isEmpty())) {
              break label425;
            }
            paramVarArgs = (String)paramVarArgs.get(0);
            if (paramVarArgs == null) {
              break label425;
            }
            this.fileSize = Utilities.parseInt(paramVarArgs).intValue();
            bool1 = bool4;
            if (localObject2 == null) {
              break label464;
            }
            bool2 = bool6;
          }
          catch (Exception paramVarArgs)
          {
            try
            {
              paramVarArgs = new byte[32768];
              i = 0;
              bool2 = bool6;
              bool1 = isCancelled();
              if (!bool1) {
                break label623;
              }
              bool1 = bool4;
            }
            catch (Throwable paramVarArgs)
            {
              try
              {
                for (;;)
                {
                  int i;
                  if (this.fileOutputStream != null)
                  {
                    this.fileOutputStream.close();
                    this.fileOutputStream = null;
                  }
                  bool2 = bool1;
                  if (localObject2 != null) {}
                  try
                  {
                    ((InputStream)localObject2).close();
                    bool2 = bool1;
                  }
                  catch (Throwable paramVarArgs)
                  {
                    for (;;)
                    {
                      boolean bool3;
                      int j;
                      FileLog.e(paramVarArgs);
                      bool2 = bool1;
                    }
                  }
                  return Boolean.valueOf(bool2);
                  localThrowable = localThrowable;
                  if ((localThrowable instanceof SocketTimeoutException)) {
                    if (ConnectionsManager.isNetworkOnline()) {
                      this.canRetry = false;
                    }
                  }
                  for (;;)
                  {
                    FileLog.e(localThrowable);
                    break;
                    if ((localThrowable instanceof UnknownHostException)) {
                      this.canRetry = false;
                    } else if ((localThrowable instanceof SocketException))
                    {
                      if ((localThrowable.getMessage() != null) && (localThrowable.getMessage().contains("ECONNRESET"))) {
                        this.canRetry = false;
                      }
                    }
                    else if ((localThrowable instanceof FileNotFoundException)) {
                      this.canRetry = false;
                    }
                  }
                  localException = localException;
                  FileLog.e(localException);
                  continue;
                  paramVarArgs = paramVarArgs;
                  FileLog.e(paramVarArgs);
                  continue;
                  bool3 = bool5;
                  bool2 = bool6;
                  try
                  {
                    j = ((InputStream)localObject2).read(paramVarArgs);
                    if (j <= 0) {
                      break label734;
                    }
                    bool3 = bool5;
                    bool2 = bool6;
                    this.fileOutputStream.write(paramVarArgs, 0, j);
                    j = i + j;
                    i = j;
                    bool3 = bool5;
                    bool2 = bool6;
                    if (this.fileSize <= 0) {
                      continue;
                    }
                    bool3 = bool5;
                    bool2 = bool6;
                    reportProgress(j / this.fileSize);
                    i = j;
                  }
                  catch (Exception paramVarArgs)
                  {
                    bool2 = bool3;
                    FileLog.e(paramVarArgs);
                    bool1 = bool3;
                  }
                  continue;
                  paramVarArgs = paramVarArgs;
                  FileLog.e(paramVarArgs);
                  bool1 = bool2;
                  continue;
                  bool1 = bool4;
                  if (j == -1)
                  {
                    bool5 = true;
                    bool6 = true;
                    bool4 = true;
                    bool1 = bool4;
                    bool3 = bool5;
                    bool2 = bool6;
                    if (this.fileSize != 0)
                    {
                      bool3 = bool5;
                      bool2 = bool6;
                      reportProgress(1.0F);
                      bool1 = bool4;
                    }
                  }
                }
              }
              catch (Throwable paramVarArgs)
              {
                for (;;)
                {
                  FileLog.e(paramVarArgs);
                }
              }
            }
          }
        }
      }
      if (this.canRetry) {
        if (paramVarArgs == null) {}
      }
    }
    
    protected void onCancelled()
    {
      ImageLoader.this.runHttpFileLoadTasks(this, 2);
    }
    
    protected void onPostExecute(Boolean paramBoolean)
    {
      ImageLoader localImageLoader = ImageLoader.this;
      if (paramBoolean.booleanValue()) {}
      for (int i = 2;; i = 1)
      {
        localImageLoader.runHttpFileLoadTasks(this, i);
        return;
      }
    }
  }
  
  private class HttpImageTask
    extends AsyncTask<Void, Void, Boolean>
  {
    private ImageLoader.CacheImage cacheImage = null;
    private boolean canRetry = true;
    private RandomAccessFile fileOutputStream = null;
    private HttpURLConnection httpConnection = null;
    private int imageSize;
    private long lastProgressTime;
    
    public HttpImageTask(ImageLoader.CacheImage paramCacheImage, int paramInt)
    {
      this.cacheImage = paramCacheImage;
      this.imageSize = paramInt;
    }
    
    private void reportProgress(final float paramFloat)
    {
      long l = System.currentTimeMillis();
      if ((paramFloat == 1.0F) || (this.lastProgressTime == 0L) || (this.lastProgressTime < l - 500L))
      {
        this.lastProgressTime = l;
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            ImageLoader.this.fileProgresses.put(ImageLoader.HttpImageTask.this.cacheImage.url, Float.valueOf(paramFloat));
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(ImageLoader.HttpImageTask.this.cacheImage.currentAccount).postNotificationName(NotificationCenter.FileLoadProgressChanged, new Object[] { ImageLoader.HttpImageTask.this.cacheImage.url, Float.valueOf(ImageLoader.HttpImageTask.1.this.val$progress) });
              }
            });
          }
        });
      }
    }
    
    protected Boolean doInBackground(Void... paramVarArgs)
    {
      Object localObject3 = null;
      Object localObject2 = null;
      bool5 = false;
      bool6 = false;
      bool4 = false;
      paramVarArgs = (Void[])localObject2;
      if (!isCancelled()) {
        localObject1 = localObject3;
      }
      try
      {
        this.httpConnection = ((HttpURLConnection)new URL(this.cacheImage.httpUrl).openConnection());
        localObject1 = localObject3;
        this.httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1");
        localObject1 = localObject3;
        this.httpConnection.setConnectTimeout(5000);
        localObject1 = localObject3;
        this.httpConnection.setReadTimeout(5000);
        localObject1 = localObject3;
        this.httpConnection.setInstanceFollowRedirects(true);
        paramVarArgs = (Void[])localObject2;
        localObject1 = localObject3;
        if (!isCancelled())
        {
          localObject1 = localObject3;
          this.httpConnection.connect();
          localObject1 = localObject3;
          paramVarArgs = this.httpConnection.getInputStream();
          localObject1 = paramVarArgs;
          this.fileOutputStream = new RandomAccessFile(this.cacheImage.tempFilePath, "rws");
        }
      }
      catch (Throwable localThrowable2)
      {
        try
        {
          if ((this.httpConnection == null) || (!(this.httpConnection instanceof HttpURLConnection))) {
            break label230;
          }
          i = this.httpConnection.getResponseCode();
          if ((i == 200) || (i == 202) || (i == 304)) {
            break label230;
          }
          this.canRetry = false;
          if ((this.imageSize != 0) || (this.httpConnection == null)) {
            break label317;
          }
        }
        catch (Exception localThrowable2)
        {
          try
          {
            localObject1 = this.httpConnection.getHeaderFields();
            if (localObject1 == null) {
              break label317;
            }
            localObject1 = (List)((Map)localObject1).get("content-Length");
            if ((localObject1 == null) || (((List)localObject1).isEmpty())) {
              break label317;
            }
            localObject1 = (String)((List)localObject1).get(0);
            if (localObject1 == null) {
              break label317;
            }
            this.imageSize = Utilities.parseInt((String)localObject1).intValue();
            bool2 = bool4;
            if (paramVarArgs == null) {
              break label357;
            }
            bool1 = bool6;
          }
          catch (Exception localThrowable2)
          {
            try
            {
              localObject1 = new byte[' '];
              i = 0;
              bool1 = bool6;
              bool2 = isCancelled();
              if (!bool2) {
                break label566;
              }
              bool2 = bool4;
            }
            catch (Throwable localThrowable2)
            {
              try
              {
                if (this.fileOutputStream == null) {
                  break label376;
                }
                this.fileOutputStream.close();
                this.fileOutputStream = null;
              }
              catch (Throwable localThrowable2)
              {
                try
                {
                  for (;;)
                  {
                    boolean bool2;
                    int i;
                    boolean bool1;
                    if (this.httpConnection == null) {
                      break label390;
                    }
                    this.httpConnection.disconnect();
                    if (paramVarArgs == null) {
                      break label398;
                    }
                    try
                    {
                      paramVarArgs.close();
                      if ((!bool2) || (this.cacheImage.tempFilePath == null) || (this.cacheImage.tempFilePath.renameTo(this.cacheImage.finalFilePath))) {
                        break label447;
                      }
                      this.cacheImage.finalFilePath = this.cacheImage.tempFilePath;
                      return Boolean.valueOf(bool2);
                      paramVarArgs = paramVarArgs;
                      if (!(paramVarArgs instanceof SocketTimeoutException)) {
                        break label482;
                      }
                      if (!ConnectionsManager.isNetworkOnline()) {
                        break label472;
                      }
                      this.canRetry = false;
                      for (;;)
                      {
                        FileLog.e(paramVarArgs);
                        paramVarArgs = (Void[])localObject1;
                        break;
                        if ((paramVarArgs instanceof UnknownHostException)) {
                          this.canRetry = false;
                        } else if ((paramVarArgs instanceof SocketException))
                        {
                          if ((paramVarArgs.getMessage() != null) && (paramVarArgs.getMessage().contains("ECONNRESET"))) {
                            this.canRetry = false;
                          }
                        }
                        else if ((paramVarArgs instanceof FileNotFoundException)) {
                          this.canRetry = false;
                        }
                      }
                      localException1 = localException1;
                      FileLog.e(localException1);
                      continue;
                      localException2 = localException2;
                      FileLog.e(localException2);
                      continue;
                      boolean bool3 = bool5;
                      bool1 = bool6;
                      int k;
                      try
                      {
                        k = paramVarArgs.read(localException2);
                        if (k <= 0) {
                          break label686;
                        }
                        int j = i + k;
                        bool3 = bool5;
                        bool1 = bool6;
                        this.fileOutputStream.write(localException2, 0, k);
                        i = j;
                        bool3 = bool5;
                        bool1 = bool6;
                        if (this.imageSize == 0) {
                          continue;
                        }
                        bool3 = bool5;
                        bool1 = bool6;
                        reportProgress(j / this.imageSize);
                        i = j;
                      }
                      catch (Exception localException3)
                      {
                        bool1 = bool3;
                        FileLog.e(localException3);
                        bool2 = bool3;
                      }
                      continue;
                      localThrowable1 = localThrowable1;
                      FileLog.e(localThrowable1);
                      bool2 = bool1;
                      continue;
                      bool2 = bool4;
                      if (k != -1) {
                        continue;
                      }
                      bool5 = true;
                      bool6 = true;
                      bool4 = true;
                      bool2 = bool4;
                      bool3 = bool5;
                      bool1 = bool6;
                      if (this.imageSize == 0) {
                        continue;
                      }
                      bool3 = bool5;
                      bool1 = bool6;
                      reportProgress(1.0F);
                      bool2 = bool4;
                      continue;
                      localThrowable2 = localThrowable2;
                      FileLog.e(localThrowable2);
                    }
                    catch (Throwable paramVarArgs)
                    {
                      for (;;)
                      {
                        FileLog.e(paramVarArgs);
                      }
                    }
                  }
                }
                catch (Throwable localThrowable3)
                {
                  for (;;) {}
                }
              }
            }
          }
        }
      }
      bool2 = bool4;
      if (isCancelled()) {}
    }
    
    protected void onCancelled()
    {
      ImageLoader.this.imageLoadQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          ImageLoader.this.runHttpTasks(true);
        }
      });
      Utilities.stageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          ImageLoader.this.fileProgresses.remove(ImageLoader.HttpImageTask.this.cacheImage.url);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationCenter.getInstance(ImageLoader.HttpImageTask.this.cacheImage.currentAccount).postNotificationName(NotificationCenter.FileDidFailedLoad, new Object[] { ImageLoader.HttpImageTask.this.cacheImage.url, Integer.valueOf(1) });
            }
          });
        }
      });
    }
    
    protected void onPostExecute(final Boolean paramBoolean)
    {
      if ((paramBoolean.booleanValue()) || (!this.canRetry)) {
        ImageLoader.this.fileDidLoaded(this.cacheImage.url, this.cacheImage.finalFilePath, 0);
      }
      for (;;)
      {
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            ImageLoader.this.fileProgresses.remove(ImageLoader.HttpImageTask.this.cacheImage.url);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                if (ImageLoader.HttpImageTask.2.this.val$result.booleanValue())
                {
                  NotificationCenter.getInstance(ImageLoader.HttpImageTask.this.cacheImage.currentAccount).postNotificationName(NotificationCenter.FileDidLoaded, new Object[] { ImageLoader.HttpImageTask.this.cacheImage.url });
                  return;
                }
                NotificationCenter.getInstance(ImageLoader.HttpImageTask.this.cacheImage.currentAccount).postNotificationName(NotificationCenter.FileDidFailedLoad, new Object[] { ImageLoader.HttpImageTask.this.cacheImage.url, Integer.valueOf(2) });
              }
            });
          }
        });
        ImageLoader.this.imageLoadQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            ImageLoader.this.runHttpTasks(true);
          }
        });
        return;
        ImageLoader.this.httpFileLoadError(this.cacheImage.url);
      }
    }
  }
  
  private class ThumbGenerateInfo
  {
    private int count;
    private TLRPC.FileLocation fileLocation;
    private String filter;
    
    private ThumbGenerateInfo() {}
  }
  
  private class ThumbGenerateTask
    implements Runnable
  {
    private String filter;
    private int mediaType;
    private File originalPath;
    private TLRPC.FileLocation thumbLocation;
    
    public ThumbGenerateTask(int paramInt, File paramFile, TLRPC.FileLocation paramFileLocation, String paramString)
    {
      this.mediaType = paramInt;
      this.originalPath = paramFile;
      this.thumbLocation = paramFileLocation;
      this.filter = paramString;
    }
    
    private void removeTask()
    {
      if (this.thumbLocation == null) {
        return;
      }
      final String str = FileLoader.getAttachFileName(this.thumbLocation);
      ImageLoader.this.imageLoadQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          ImageLoader.this.thumbGenerateTasks.remove(str);
        }
      });
    }
    
    public void run()
    {
      final String str;
      File localFile;
      try
      {
        if (this.thumbLocation == null)
        {
          removeTask();
          return;
        }
        str = this.thumbLocation.volume_id + "_" + this.thumbLocation.local_id;
        localFile = new File(FileLoader.getDirectory(4), "q_" + str + ".jpg");
        if ((localFile.exists()) || (!this.originalPath.exists()))
        {
          removeTask();
          return;
        }
      }
      catch (Throwable localThrowable)
      {
        FileLog.e(localThrowable);
        removeTask();
        return;
      }
      int i = Math.min(180, Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) / 4);
      Object localObject1 = null;
      if (this.mediaType == 0) {
        localObject1 = ImageLoader.loadBitmap(this.originalPath.toString(), null, i, i, false);
      }
      while (localObject1 == null)
      {
        removeTask();
        return;
        if (this.mediaType == 2)
        {
          localObject1 = ThumbnailUtils.createVideoThumbnail(this.originalPath.toString(), 1);
        }
        else if (this.mediaType == 3)
        {
          localObject1 = this.originalPath.toString().toLowerCase();
          if ((!((String)localObject1).endsWith(".jpg")) && (!((String)localObject1).endsWith(".jpeg")) && (!((String)localObject1).endsWith(".png")) && (!((String)localObject1).endsWith(".gif")))
          {
            removeTask();
            return;
          }
          localObject1 = ImageLoader.loadBitmap((String)localObject1, null, i, i, false);
        }
      }
      int j = ((Bitmap)localObject1).getWidth();
      int k = ((Bitmap)localObject1).getHeight();
      if ((j == 0) || (k == 0))
      {
        removeTask();
        return;
      }
      float f = Math.min(j / i, k / i);
      Bitmap localBitmap = Bitmaps.createScaledBitmap((Bitmap)localObject1, (int)(j / f), (int)(k / f), true);
      Object localObject2 = localObject1;
      if (localBitmap != localObject1)
      {
        ((Bitmap)localObject1).recycle();
        localObject2 = localBitmap;
      }
      localObject1 = new FileOutputStream(localFile);
      ((Bitmap)localObject2).compress(Bitmap.CompressFormat.JPEG, 60, (OutputStream)localObject1);
      try
      {
        ((FileOutputStream)localObject1).close();
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            ImageLoader.ThumbGenerateTask.this.removeTask();
            String str2 = str;
            String str1 = str2;
            if (ImageLoader.ThumbGenerateTask.this.filter != null) {
              str1 = str2 + "@" + ImageLoader.ThumbGenerateTask.this.filter;
            }
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.messageThumbGenerated, new Object[] { this.val$bitmapDrawable, str1 });
            ImageLoader.this.memCache.put(str1, this.val$bitmapDrawable);
          }
        });
        return;
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
        }
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/ImageLoader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */