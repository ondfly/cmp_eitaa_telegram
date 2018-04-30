package org.telegram.messenger.camera;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Build;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.Utilities;

public class CameraController
  implements MediaRecorder.OnInfoListener
{
  private static final int CORE_POOL_SIZE = 1;
  private static volatile CameraController Instance = null;
  private static final int KEEP_ALIVE_SECONDS = 60;
  private static final int MAX_POOL_SIZE = 1;
  protected ArrayList<String> availableFlashModes = new ArrayList();
  protected ArrayList<CameraInfo> cameraInfos = null;
  private boolean cameraInitied;
  private boolean loadingCameras;
  private VideoTakeCallback onVideoTakeCallback;
  private String recordedFile;
  private MediaRecorder recorder;
  private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue());
  
  public static Size chooseOptimalSize(List<Size> paramList, int paramInt1, int paramInt2, Size paramSize)
  {
    ArrayList localArrayList = new ArrayList();
    int j = paramSize.getWidth();
    int k = paramSize.getHeight();
    int i = 0;
    while (i < paramList.size())
    {
      paramSize = (Size)paramList.get(i);
      if ((paramSize.getHeight() == paramSize.getWidth() * k / j) && (paramSize.getWidth() >= paramInt1) && (paramSize.getHeight() >= paramInt2)) {
        localArrayList.add(paramSize);
      }
      i += 1;
    }
    if (localArrayList.size() > 0) {
      return (Size)Collections.min(localArrayList, new CompareSizesByArea());
    }
    return (Size)Collections.max(paramList, new CompareSizesByArea());
  }
  
  /* Error */
  private void finishRecordingVideo()
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 7
    //   3: aconst_null
    //   4: astore 9
    //   6: lconst_0
    //   7: lstore 5
    //   9: new 176	android/media/MediaMetadataRetriever
    //   12: dup
    //   13: invokespecial 177	android/media/MediaMetadataRetriever:<init>	()V
    //   16: astore 8
    //   18: aload 8
    //   20: aload_0
    //   21: getfield 127	org/telegram/messenger/camera/CameraController:recordedFile	Ljava/lang/String;
    //   24: invokevirtual 181	android/media/MediaMetadataRetriever:setDataSource	(Ljava/lang/String;)V
    //   27: aload 8
    //   29: bipush 9
    //   31: invokevirtual 185	android/media/MediaMetadataRetriever:extractMetadata	(I)Ljava/lang/String;
    //   34: astore 7
    //   36: lload 5
    //   38: lstore_3
    //   39: aload 7
    //   41: ifnull +21 -> 62
    //   44: aload 7
    //   46: invokestatic 191	java/lang/Long:parseLong	(Ljava/lang/String;)J
    //   49: l2f
    //   50: ldc -64
    //   52: fdiv
    //   53: f2d
    //   54: invokestatic 198	java/lang/Math:ceil	(D)D
    //   57: dstore_1
    //   58: dload_1
    //   59: d2i
    //   60: i2l
    //   61: lstore_3
    //   62: aload 8
    //   64: ifnull +8 -> 72
    //   67: aload 8
    //   69: invokevirtual 201	android/media/MediaMetadataRetriever:release	()V
    //   72: aload_0
    //   73: getfield 127	org/telegram/messenger/camera/CameraController:recordedFile	Ljava/lang/String;
    //   76: iconst_1
    //   77: invokestatic 207	android/media/ThumbnailUtils:createVideoThumbnail	(Ljava/lang/String;I)Landroid/graphics/Bitmap;
    //   80: astore 7
    //   82: new 209	java/lang/StringBuilder
    //   85: dup
    //   86: invokespecial 210	java/lang/StringBuilder:<init>	()V
    //   89: ldc -44
    //   91: invokevirtual 216	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   94: invokestatic 221	org/telegram/messenger/SharedConfig:getLastLocalId	()I
    //   97: invokevirtual 224	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   100: ldc -30
    //   102: invokevirtual 216	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   105: invokevirtual 230	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   108: astore 8
    //   110: new 232	java/io/File
    //   113: dup
    //   114: iconst_4
    //   115: invokestatic 238	org/telegram/messenger/FileLoader:getDirectory	(I)Ljava/io/File;
    //   118: aload 8
    //   120: invokespecial 241	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   123: astore 8
    //   125: new 243	java/io/FileOutputStream
    //   128: dup
    //   129: aload 8
    //   131: invokespecial 246	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   134: astore 9
    //   136: aload 7
    //   138: getstatic 252	android/graphics/Bitmap$CompressFormat:JPEG	Landroid/graphics/Bitmap$CompressFormat;
    //   141: bipush 55
    //   143: aload 9
    //   145: invokevirtual 258	android/graphics/Bitmap:compress	(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z
    //   148: pop
    //   149: invokestatic 261	org/telegram/messenger/SharedConfig:saveConfig	()V
    //   152: new 16	org/telegram/messenger/camera/CameraController$10
    //   155: dup
    //   156: aload_0
    //   157: aload 8
    //   159: aload 7
    //   161: lload_3
    //   162: invokespecial 264	org/telegram/messenger/camera/CameraController$10:<init>	(Lorg/telegram/messenger/camera/CameraController;Ljava/io/File;Landroid/graphics/Bitmap;J)V
    //   165: invokestatic 270	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
    //   168: return
    //   169: astore 7
    //   171: aload 7
    //   173: invokestatic 276	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   176: goto -104 -> 72
    //   179: astore 7
    //   181: aload 9
    //   183: astore 8
    //   185: aload 7
    //   187: astore 9
    //   189: aload 8
    //   191: astore 7
    //   193: aload 9
    //   195: invokestatic 276	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   198: lload 5
    //   200: lstore_3
    //   201: aload 8
    //   203: ifnull -131 -> 72
    //   206: aload 8
    //   208: invokevirtual 201	android/media/MediaMetadataRetriever:release	()V
    //   211: lload 5
    //   213: lstore_3
    //   214: goto -142 -> 72
    //   217: astore 7
    //   219: aload 7
    //   221: invokestatic 276	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   224: lload 5
    //   226: lstore_3
    //   227: goto -155 -> 72
    //   230: astore 9
    //   232: aload 7
    //   234: astore 8
    //   236: aload 9
    //   238: astore 7
    //   240: aload 8
    //   242: ifnull +8 -> 250
    //   245: aload 8
    //   247: invokevirtual 201	android/media/MediaMetadataRetriever:release	()V
    //   250: aload 7
    //   252: athrow
    //   253: astore 8
    //   255: aload 8
    //   257: invokestatic 276	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   260: goto -10 -> 250
    //   263: astore 9
    //   265: aload 9
    //   267: invokestatic 276	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   270: goto -121 -> 149
    //   273: astore 7
    //   275: goto -35 -> 240
    //   278: astore 9
    //   280: goto -91 -> 189
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	283	0	this	CameraController
    //   57	2	1	d	double
    //   38	189	3	l1	long
    //   7	218	5	l2	long
    //   1	159	7	localObject1	Object
    //   169	3	7	localException1	Exception
    //   179	7	7	localException2	Exception
    //   191	1	7	localObject2	Object
    //   217	16	7	localException3	Exception
    //   238	13	7	localObject3	Object
    //   273	1	7	localObject4	Object
    //   16	230	8	localObject5	Object
    //   253	3	8	localException4	Exception
    //   4	190	9	localObject6	Object
    //   230	7	9	localObject7	Object
    //   263	3	9	localThrowable	Throwable
    //   278	1	9	localException5	Exception
    // Exception table:
    //   from	to	target	type
    //   67	72	169	java/lang/Exception
    //   9	18	179	java/lang/Exception
    //   206	211	217	java/lang/Exception
    //   9	18	230	finally
    //   193	198	230	finally
    //   245	250	253	java/lang/Exception
    //   125	149	263	java/lang/Throwable
    //   18	36	273	finally
    //   44	58	273	finally
    //   18	36	278	java/lang/Exception
    //   44	58	278	java/lang/Exception
  }
  
  public static CameraController getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          CameraController localCameraController2 = Instance;
          localObject1 = localCameraController2;
          if (localCameraController2 == null) {
            localObject1 = new CameraController();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (CameraController)localObject1;
          return (CameraController)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localCameraController1;
  }
  
  private static int getOrientation(byte[] paramArrayOfByte)
  {
    boolean bool = true;
    if (paramArrayOfByte == null) {}
    label11:
    label119:
    label294:
    label411:
    label413:
    label417:
    for (;;)
    {
      return 0;
      int i = 0;
      int n;
      int i1;
      int m;
      do
      {
        do
        {
          for (;;)
          {
            n = 0;
            k = n;
            j = i;
            if (i + 3 >= paramArrayOfByte.length) {
              break label119;
            }
            j = i + 1;
            if ((paramArrayOfByte[i] & 0xFF) != 255) {
              break label413;
            }
            i1 = paramArrayOfByte[j] & 0xFF;
            if (i1 != 255) {
              break;
            }
            i = j;
          }
          m = j + 1;
          i = m;
        } while (i1 == 216);
        i = m;
      } while (i1 == 1);
      int k = n;
      int j = m;
      if (i1 != 217)
      {
        if (i1 != 218) {
          break label294;
        }
        j = m;
        k = n;
      }
      for (;;)
      {
        if (k <= 8) {
          break label417;
        }
        i = pack(paramArrayOfByte, j, 4, false);
        if ((i != 1229531648) && (i != 1296891946)) {
          break;
        }
        if (i == 1229531648)
        {
          m = pack(paramArrayOfByte, j + 4, 4, bool) + 2;
          if ((m < 10) || (m > k)) {
            break;
          }
          i = j + m;
          j = k - m;
          m = pack(paramArrayOfByte, i - 2, 2, bool);
          k = i;
          i = m;
        }
        for (;;)
        {
          if ((i <= 0) || (j < 12)) {
            break label411;
          }
          if (pack(paramArrayOfByte, k, 2, bool) == 274) {
            switch (pack(paramArrayOfByte, k + 8, 2, bool))
            {
            case 1: 
            case 2: 
            case 4: 
            case 5: 
            case 7: 
            default: 
              return 0;
            case 3: 
              return 180;
              i = pack(paramArrayOfByte, m, 2, false);
              if ((i < 2) || (m + i > paramArrayOfByte.length)) {
                break;
              }
              if ((i1 == 225) && (i >= 8) && (pack(paramArrayOfByte, m + 2, 4, false) == 1165519206) && (pack(paramArrayOfByte, m + 6, 2, false) == 0))
              {
                j = m + 8;
                k = i - 8;
                break label119;
              }
              i = m + i;
              break label11;
              bool = false;
              break;
            case 6: 
              return 90;
            case 8: 
              return 270;
            }
          }
          k += 12;
          j -= 12;
          i -= 1;
        }
        break;
        k = n;
      }
    }
  }
  
  private static int pack(byte[] paramArrayOfByte, int paramInt1, int paramInt2, boolean paramBoolean)
  {
    int j = 1;
    int i = paramInt1;
    if (paramBoolean)
    {
      i = paramInt1 + (paramInt2 - 1);
      j = -1;
    }
    paramInt1 = 0;
    while (paramInt2 > 0)
    {
      paramInt1 = paramInt1 << 8 | paramArrayOfByte[i] & 0xFF;
      i += j;
      paramInt2 -= 1;
    }
    return paramInt1;
  }
  
  public void cleanup()
  {
    this.threadPool.execute(new Runnable()
    {
      public void run()
      {
        if ((CameraController.this.cameraInfos == null) || (CameraController.this.cameraInfos.isEmpty())) {
          return;
        }
        int i = 0;
        while (i < CameraController.this.cameraInfos.size())
        {
          CameraInfo localCameraInfo = (CameraInfo)CameraController.this.cameraInfos.get(i);
          if (localCameraInfo.camera != null)
          {
            localCameraInfo.camera.stopPreview();
            localCameraInfo.camera.setPreviewCallbackWithBuffer(null);
            localCameraInfo.camera.release();
            localCameraInfo.camera = null;
          }
          i += 1;
        }
        CameraController.this.cameraInfos = null;
      }
    });
  }
  
  public void close(final CameraSession paramCameraSession, final CountDownLatch paramCountDownLatch, final Runnable paramRunnable)
  {
    paramCameraSession.destroy();
    this.threadPool.execute(new Runnable()
    {
      public void run()
      {
        if (paramRunnable != null) {
          paramRunnable.run();
        }
        if (paramCameraSession.cameraInfo.camera == null) {}
        for (;;)
        {
          return;
          try
          {
            paramCameraSession.cameraInfo.camera.stopPreview();
            paramCameraSession.cameraInfo.camera.setPreviewCallbackWithBuffer(null);
          }
          catch (Exception localException1)
          {
            try
            {
              for (;;)
              {
                paramCameraSession.cameraInfo.camera.release();
                paramCameraSession.cameraInfo.camera = null;
                if (paramCountDownLatch == null) {
                  break;
                }
                paramCountDownLatch.countDown();
                return;
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
            }
          }
        }
      }
    });
    if (paramCountDownLatch != null) {}
    try
    {
      paramCountDownLatch.await();
      return;
    }
    catch (Exception paramCameraSession)
    {
      FileLog.e(paramCameraSession);
    }
  }
  
  public ArrayList<CameraInfo> getCameras()
  {
    return this.cameraInfos;
  }
  
  public void initCamera()
  {
    if ((this.loadingCameras) || (this.cameraInitied)) {
      return;
    }
    this.loadingCameras = true;
    this.threadPool.execute(new Runnable()
    {
      public void run()
      {
        int i;
        CameraInfo localCameraInfo1;
        Object localObject1;
        Object localObject3;
        try
        {
          if (CameraController.this.cameraInfos != null) {
            break label471;
          }
          int k = Camera.getNumberOfCameras();
          ArrayList localArrayList = new ArrayList();
          Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
          i = 0;
          if (i >= k) {
            break label462;
          }
          Camera.getCameraInfo(i, localCameraInfo);
          localCameraInfo1 = new CameraInfo(i, localCameraInfo);
          localObject1 = Camera.open(localCameraInfo1.getCameraId());
          localObject2 = ((Camera)localObject1).getParameters();
          localObject3 = ((Camera.Parameters)localObject2).getSupportedPreviewSizes();
          j = 0;
          label83:
          if (j < ((List)localObject3).size())
          {
            Camera.Size localSize = (Camera.Size)((List)localObject3).get(j);
            if (((localSize.width == 1280) && (localSize.height != 720)) || (localSize.height >= 2160) || (localSize.width >= 2160)) {
              break label483;
            }
            localCameraInfo1.previewSizes.add(new Size(localSize.width, localSize.height));
            if (!BuildVars.LOGS_ENABLED) {
              break label483;
            }
            FileLog.d("preview size = " + localSize.width + " " + localSize.height);
          }
        }
        catch (Exception localException)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              CameraController.access$002(CameraController.this, false);
              CameraController.access$102(CameraController.this, false);
            }
          });
          FileLog.e(localException);
          return;
        }
        Object localObject2 = ((Camera.Parameters)localObject2).getSupportedPictureSizes();
        int j = 0;
        for (;;)
        {
          if (j < ((List)localObject2).size())
          {
            localObject3 = (Camera.Size)((List)localObject2).get(j);
            if (((((Camera.Size)localObject3).width != 1280) || (((Camera.Size)localObject3).height == 720)) && ((!"samsung".equals(Build.MANUFACTURER)) || (!"jflteuc".equals(Build.PRODUCT)) || (((Camera.Size)localObject3).width < 2048)))
            {
              localCameraInfo1.pictureSizes.add(new Size(((Camera.Size)localObject3).width, ((Camera.Size)localObject3).height));
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("picture size = " + ((Camera.Size)localObject3).width + " " + ((Camera.Size)localObject3).height);
              }
            }
          }
          else
          {
            ((Camera)localObject1).release();
            localException.add(localCameraInfo1);
            localObject1 = new Comparator()
            {
              public int compare(Size paramAnonymous2Size1, Size paramAnonymous2Size2)
              {
                if (paramAnonymous2Size1.mWidth < paramAnonymous2Size2.mWidth) {}
                do
                {
                  return 1;
                  if (paramAnonymous2Size1.mWidth > paramAnonymous2Size2.mWidth) {
                    return -1;
                  }
                } while (paramAnonymous2Size1.mHeight < paramAnonymous2Size2.mHeight);
                if (paramAnonymous2Size1.mHeight > paramAnonymous2Size2.mHeight) {
                  return -1;
                }
                return 0;
              }
            };
            Collections.sort(localCameraInfo1.previewSizes, (Comparator)localObject1);
            Collections.sort(localCameraInfo1.pictureSizes, (Comparator)localObject1);
            i += 1;
            break;
            label462:
            CameraController.this.cameraInfos = localException;
            label471:
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                CameraController.access$002(CameraController.this, false);
                CameraController.access$102(CameraController.this, true);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.cameraInitied, new Object[0]);
              }
            });
            return;
            label483:
            j += 1;
            break label83;
          }
          j += 1;
        }
      }
    });
  }
  
  public boolean isCameraInitied()
  {
    return (this.cameraInitied) && (this.cameraInfos != null) && (!this.cameraInfos.isEmpty());
  }
  
  public void onInfo(MediaRecorder paramMediaRecorder, int paramInt1, int paramInt2)
  {
    if ((paramInt1 == 800) || (paramInt1 == 801) || (paramInt1 == 1))
    {
      paramMediaRecorder = this.recorder;
      this.recorder = null;
      if (paramMediaRecorder != null)
      {
        paramMediaRecorder.stop();
        paramMediaRecorder.release();
      }
      if (this.onVideoTakeCallback != null) {
        finishRecordingVideo();
      }
    }
  }
  
  public void open(final CameraSession paramCameraSession, final SurfaceTexture paramSurfaceTexture, final Runnable paramRunnable1, final Runnable paramRunnable2)
  {
    if ((paramCameraSession == null) || (paramSurfaceTexture == null)) {
      return;
    }
    this.threadPool.execute(new Runnable()
    {
      @SuppressLint({"NewApi"})
      public void run()
      {
        Object localObject3 = paramCameraSession.cameraInfo.camera;
        Object localObject1 = localObject3;
        Object localObject2;
        if (localObject3 == null) {
          localObject2 = localObject3;
        }
        for (;;)
        {
          int i;
          try
          {
            Object localObject4 = paramCameraSession.cameraInfo;
            localObject2 = localObject3;
            localObject1 = Camera.open(paramCameraSession.cameraInfo.cameraId);
            localObject2 = localObject3;
            ((CameraInfo)localObject4).camera = ((Camera)localObject1);
            localObject2 = localObject1;
            localObject3 = ((Camera)localObject1).getParameters().getSupportedFlashModes();
            localObject2 = localObject1;
            CameraController.this.availableFlashModes.clear();
            if (localObject3 != null)
            {
              i = 0;
              localObject2 = localObject1;
              if (i < ((List)localObject3).size())
              {
                localObject2 = localObject1;
                localObject4 = (String)((List)localObject3).get(i);
                localObject2 = localObject1;
                if (!((String)localObject4).equals("off"))
                {
                  localObject2 = localObject1;
                  if (!((String)localObject4).equals("on"))
                  {
                    localObject2 = localObject1;
                    if (!((String)localObject4).equals("auto")) {
                      break label282;
                    }
                  }
                }
                localObject2 = localObject1;
                CameraController.this.availableFlashModes.add(localObject4);
              }
              else
              {
                localObject2 = localObject1;
                paramCameraSession.checkFlashMode((String)CameraController.this.availableFlashModes.get(0));
              }
            }
            else
            {
              localObject2 = localObject1;
              if (paramRunnable2 != null)
              {
                localObject2 = localObject1;
                paramRunnable2.run();
              }
              localObject2 = localObject1;
              paramCameraSession.configurePhotoCamera();
              localObject2 = localObject1;
              ((Camera)localObject1).setPreviewTexture(paramSurfaceTexture);
              localObject2 = localObject1;
              ((Camera)localObject1).startPreview();
              localObject2 = localObject1;
              if (paramRunnable1 != null)
              {
                localObject2 = localObject1;
                AndroidUtilities.runOnUIThread(paramRunnable1);
              }
              return;
            }
          }
          catch (Exception localException)
          {
            paramCameraSession.cameraInfo.camera = null;
            if (localObject2 != null) {
              ((Camera)localObject2).release();
            }
            FileLog.e(localException);
            return;
          }
          label282:
          i += 1;
        }
      }
    });
  }
  
  public void openRound(final CameraSession paramCameraSession, final SurfaceTexture paramSurfaceTexture, final Runnable paramRunnable1, final Runnable paramRunnable2)
  {
    if ((paramCameraSession == null) || (paramSurfaceTexture == null))
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("failed to open round " + paramCameraSession + " tex = " + paramSurfaceTexture);
      }
      return;
    }
    this.threadPool.execute(new Runnable()
    {
      @SuppressLint({"NewApi"})
      public void run()
      {
        Camera localCamera2 = paramCameraSession.cameraInfo.camera;
        Object localObject = localCamera2;
        try
        {
          if (BuildVars.LOGS_ENABLED)
          {
            localObject = localCamera2;
            FileLog.d("start creating round camera session");
          }
          Camera localCamera1 = localCamera2;
          if (localCamera2 == null)
          {
            localObject = localCamera2;
            CameraInfo localCameraInfo = paramCameraSession.cameraInfo;
            localObject = localCamera2;
            localCamera1 = Camera.open(paramCameraSession.cameraInfo.cameraId);
            localObject = localCamera2;
            localCameraInfo.camera = localCamera1;
          }
          localObject = localCamera1;
          localCamera1.getParameters();
          localObject = localCamera1;
          paramCameraSession.configureRoundCamera();
          localObject = localCamera1;
          if (paramRunnable2 != null)
          {
            localObject = localCamera1;
            paramRunnable2.run();
          }
          localObject = localCamera1;
          localCamera1.setPreviewTexture(paramSurfaceTexture);
          localObject = localCamera1;
          localCamera1.startPreview();
          localObject = localCamera1;
          if (paramRunnable1 != null)
          {
            localObject = localCamera1;
            AndroidUtilities.runOnUIThread(paramRunnable1);
          }
          localObject = localCamera1;
          if (BuildVars.LOGS_ENABLED)
          {
            localObject = localCamera1;
            FileLog.d("round camera session created");
          }
          return;
        }
        catch (Exception localException)
        {
          paramCameraSession.cameraInfo.camera = null;
          if (localObject != null) {
            ((Camera)localObject).release();
          }
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void recordVideo(final CameraSession paramCameraSession, final File paramFile, final VideoTakeCallback paramVideoTakeCallback, final Runnable paramRunnable)
  {
    if (paramCameraSession == null) {
      return;
    }
    final CameraInfo localCameraInfo = paramCameraSession.cameraInfo;
    final Camera localCamera = localCameraInfo.camera;
    this.threadPool.execute(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: aload_0
        //   1: getfield 31	org/telegram/messenger/camera/CameraController$9:val$camera	Landroid/hardware/Camera;
        //   4: astore_1
        //   5: aload_1
        //   6: ifnull +303 -> 309
        //   9: aload_0
        //   10: getfield 31	org/telegram/messenger/camera/CameraController$9:val$camera	Landroid/hardware/Camera;
        //   13: invokevirtual 54	android/hardware/Camera:getParameters	()Landroid/hardware/Camera$Parameters;
        //   16: astore_2
        //   17: aload_0
        //   18: getfield 33	org/telegram/messenger/camera/CameraController$9:val$session	Lorg/telegram/messenger/camera/CameraSession;
        //   21: invokevirtual 60	org/telegram/messenger/camera/CameraSession:getCurrentFlashMode	()Ljava/lang/String;
        //   24: ldc 62
        //   26: invokevirtual 68	java/lang/String:equals	(Ljava/lang/Object;)Z
        //   29: ifeq +281 -> 310
        //   32: ldc 70
        //   34: astore_1
        //   35: aload_2
        //   36: aload_1
        //   37: invokevirtual 76	android/hardware/Camera$Parameters:setFlashMode	(Ljava/lang/String;)V
        //   40: aload_0
        //   41: getfield 31	org/telegram/messenger/camera/CameraController$9:val$camera	Landroid/hardware/Camera;
        //   44: aload_2
        //   45: invokevirtual 80	android/hardware/Camera:setParameters	(Landroid/hardware/Camera$Parameters;)V
        //   48: aload_0
        //   49: getfield 31	org/telegram/messenger/camera/CameraController$9:val$camera	Landroid/hardware/Camera;
        //   52: invokevirtual 83	android/hardware/Camera:unlock	()V
        //   55: aload_0
        //   56: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   59: new 85	android/media/MediaRecorder
        //   62: dup
        //   63: invokespecial 86	android/media/MediaRecorder:<init>	()V
        //   66: invokestatic 90	org/telegram/messenger/camera/CameraController:access$302	(Lorg/telegram/messenger/camera/CameraController;Landroid/media/MediaRecorder;)Landroid/media/MediaRecorder;
        //   69: pop
        //   70: aload_0
        //   71: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   74: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   77: aload_0
        //   78: getfield 31	org/telegram/messenger/camera/CameraController$9:val$camera	Landroid/hardware/Camera;
        //   81: invokevirtual 98	android/media/MediaRecorder:setCamera	(Landroid/hardware/Camera;)V
        //   84: aload_0
        //   85: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   88: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   91: iconst_1
        //   92: invokevirtual 102	android/media/MediaRecorder:setVideoSource	(I)V
        //   95: aload_0
        //   96: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   99: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   102: iconst_5
        //   103: invokevirtual 105	android/media/MediaRecorder:setAudioSource	(I)V
        //   106: aload_0
        //   107: getfield 33	org/telegram/messenger/camera/CameraController$9:val$session	Lorg/telegram/messenger/camera/CameraSession;
        //   110: iconst_1
        //   111: aload_0
        //   112: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   115: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   118: invokevirtual 109	org/telegram/messenger/camera/CameraSession:configureRecorder	(ILandroid/media/MediaRecorder;)V
        //   121: aload_0
        //   122: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   125: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   128: aload_0
        //   129: getfield 35	org/telegram/messenger/camera/CameraController$9:val$path	Ljava/io/File;
        //   132: invokevirtual 114	java/io/File:getAbsolutePath	()Ljava/lang/String;
        //   135: invokevirtual 117	android/media/MediaRecorder:setOutputFile	(Ljava/lang/String;)V
        //   138: aload_0
        //   139: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   142: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   145: ldc2_w 118
        //   148: invokevirtual 123	android/media/MediaRecorder:setMaxFileSize	(J)V
        //   151: aload_0
        //   152: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   155: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   158: bipush 30
        //   160: invokevirtual 126	android/media/MediaRecorder:setVideoFrameRate	(I)V
        //   163: aload_0
        //   164: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   167: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   170: iconst_0
        //   171: invokevirtual 129	android/media/MediaRecorder:setMaxDuration	(I)V
        //   174: new 131	org/telegram/messenger/camera/Size
        //   177: dup
        //   178: bipush 16
        //   180: bipush 9
        //   182: invokespecial 134	org/telegram/messenger/camera/Size:<init>	(II)V
        //   185: astore_1
        //   186: aload_0
        //   187: getfield 37	org/telegram/messenger/camera/CameraController$9:val$info	Lorg/telegram/messenger/camera/CameraInfo;
        //   190: invokevirtual 140	org/telegram/messenger/camera/CameraInfo:getPictureSizes	()Ljava/util/ArrayList;
        //   193: sipush 720
        //   196: sipush 480
        //   199: aload_1
        //   200: invokestatic 144	org/telegram/messenger/camera/CameraController:chooseOptimalSize	(Ljava/util/List;IILorg/telegram/messenger/camera/Size;)Lorg/telegram/messenger/camera/Size;
        //   203: astore_1
        //   204: aload_0
        //   205: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   208: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   211: ldc -111
        //   213: invokevirtual 148	android/media/MediaRecorder:setVideoEncodingBitRate	(I)V
        //   216: aload_0
        //   217: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   220: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   223: aload_1
        //   224: invokevirtual 152	org/telegram/messenger/camera/Size:getWidth	()I
        //   227: aload_1
        //   228: invokevirtual 155	org/telegram/messenger/camera/Size:getHeight	()I
        //   231: invokevirtual 158	android/media/MediaRecorder:setVideoSize	(II)V
        //   234: aload_0
        //   235: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   238: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   241: aload_0
        //   242: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   245: invokevirtual 162	android/media/MediaRecorder:setOnInfoListener	(Landroid/media/MediaRecorder$OnInfoListener;)V
        //   248: aload_0
        //   249: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   252: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   255: invokevirtual 165	android/media/MediaRecorder:prepare	()V
        //   258: aload_0
        //   259: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   262: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   265: invokevirtual 168	android/media/MediaRecorder:start	()V
        //   268: aload_0
        //   269: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   272: aload_0
        //   273: getfield 39	org/telegram/messenger/camera/CameraController$9:val$callback	Lorg/telegram/messenger/camera/CameraController$VideoTakeCallback;
        //   276: invokestatic 172	org/telegram/messenger/camera/CameraController:access$402	(Lorg/telegram/messenger/camera/CameraController;Lorg/telegram/messenger/camera/CameraController$VideoTakeCallback;)Lorg/telegram/messenger/camera/CameraController$VideoTakeCallback;
        //   279: pop
        //   280: aload_0
        //   281: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   284: aload_0
        //   285: getfield 35	org/telegram/messenger/camera/CameraController$9:val$path	Ljava/io/File;
        //   288: invokevirtual 114	java/io/File:getAbsolutePath	()Ljava/lang/String;
        //   291: invokestatic 176	org/telegram/messenger/camera/CameraController:access$502	(Lorg/telegram/messenger/camera/CameraController;Ljava/lang/String;)Ljava/lang/String;
        //   294: pop
        //   295: aload_0
        //   296: getfield 41	org/telegram/messenger/camera/CameraController$9:val$onVideoStartRecord	Ljava/lang/Runnable;
        //   299: ifnull +10 -> 309
        //   302: aload_0
        //   303: getfield 41	org/telegram/messenger/camera/CameraController$9:val$onVideoStartRecord	Ljava/lang/Runnable;
        //   306: invokestatic 182	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
        //   309: return
        //   310: ldc -72
        //   312: astore_1
        //   313: goto -278 -> 35
        //   316: astore_1
        //   317: aload_1
        //   318: invokestatic 190	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   321: goto -273 -> 48
        //   324: astore_1
        //   325: aload_1
        //   326: invokestatic 190	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   329: return
        //   330: astore_1
        //   331: aload_0
        //   332: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   335: invokestatic 94	org/telegram/messenger/camera/CameraController:access$300	(Lorg/telegram/messenger/camera/CameraController;)Landroid/media/MediaRecorder;
        //   338: invokevirtual 193	android/media/MediaRecorder:release	()V
        //   341: aload_0
        //   342: getfield 29	org/telegram/messenger/camera/CameraController$9:this$0	Lorg/telegram/messenger/camera/CameraController;
        //   345: aconst_null
        //   346: invokestatic 90	org/telegram/messenger/camera/CameraController:access$302	(Lorg/telegram/messenger/camera/CameraController;Landroid/media/MediaRecorder;)Landroid/media/MediaRecorder;
        //   349: pop
        //   350: aload_1
        //   351: invokestatic 190	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   354: return
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	355	0	this	9
        //   4	309	1	localObject	Object
        //   316	2	1	localException1	Exception
        //   324	2	1	localException2	Exception
        //   330	21	1	localException3	Exception
        //   16	29	2	localParameters	Camera.Parameters
        // Exception table:
        //   from	to	target	type
        //   9	32	316	java/lang/Exception
        //   35	48	316	java/lang/Exception
        //   0	5	324	java/lang/Exception
        //   48	55	324	java/lang/Exception
        //   317	321	324	java/lang/Exception
        //   331	354	324	java/lang/Exception
        //   55	309	330	java/lang/Exception
      }
    });
  }
  
  public void startPreview(final CameraSession paramCameraSession)
  {
    if (paramCameraSession == null) {
      return;
    }
    this.threadPool.execute(new Runnable()
    {
      @SuppressLint({"NewApi"})
      public void run()
      {
        Camera localCamera3 = paramCameraSession.cameraInfo.camera;
        Camera localCamera1 = localCamera3;
        Camera localCamera2;
        if (localCamera3 == null) {
          localCamera2 = localCamera3;
        }
        try
        {
          CameraInfo localCameraInfo = paramCameraSession.cameraInfo;
          localCamera2 = localCamera3;
          localCamera1 = Camera.open(paramCameraSession.cameraInfo.cameraId);
          localCamera2 = localCamera3;
          localCameraInfo.camera = localCamera1;
          localCamera2 = localCamera1;
          localCamera1.startPreview();
          return;
        }
        catch (Exception localException)
        {
          paramCameraSession.cameraInfo.camera = null;
          if (localCamera2 != null) {
            localCamera2.release();
          }
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void stopPreview(final CameraSession paramCameraSession)
  {
    if (paramCameraSession == null) {
      return;
    }
    this.threadPool.execute(new Runnable()
    {
      @SuppressLint({"NewApi"})
      public void run()
      {
        Camera localCamera3 = paramCameraSession.cameraInfo.camera;
        Camera localCamera1 = localCamera3;
        Camera localCamera2;
        if (localCamera3 == null) {
          localCamera2 = localCamera3;
        }
        try
        {
          CameraInfo localCameraInfo = paramCameraSession.cameraInfo;
          localCamera2 = localCamera3;
          localCamera1 = Camera.open(paramCameraSession.cameraInfo.cameraId);
          localCamera2 = localCamera3;
          localCameraInfo.camera = localCamera1;
          localCamera2 = localCamera1;
          localCamera1.stopPreview();
          return;
        }
        catch (Exception localException)
        {
          paramCameraSession.cameraInfo.camera = null;
          if (localCamera2 != null) {
            localCamera2.release();
          }
          FileLog.e(localException);
        }
      }
    });
  }
  
  public void stopVideoRecording(final CameraSession paramCameraSession, final boolean paramBoolean)
  {
    this.threadPool.execute(new Runnable()
    {
      public void run()
      {
        try
        {
          final Camera localCamera = paramCameraSession.cameraInfo.camera;
          Object localObject;
          if ((localCamera != null) && (CameraController.this.recorder != null))
          {
            localObject = CameraController.this.recorder;
            CameraController.access$302(CameraController.this, null);
          }
          try
          {
            ((MediaRecorder)localObject).stop();
          }
          catch (Exception localException4)
          {
            try
            {
              ((MediaRecorder)localObject).release();
            }
            catch (Exception localException4)
            {
              try
              {
                localCamera.reconnect();
                localCamera.startPreview();
              }
              catch (Exception localException4)
              {
                try
                {
                  paramCameraSession.stopVideoRecording();
                }
                catch (Exception localException4)
                {
                  try
                  {
                    for (;;)
                    {
                      localObject = localCamera.getParameters();
                      ((Camera.Parameters)localObject).setFlashMode("off");
                      localCamera.setParameters((Camera.Parameters)localObject);
                      CameraController.this.threadPool.execute(new Runnable()
                      {
                        public void run()
                        {
                          try
                          {
                            Camera.Parameters localParameters = localCamera.getParameters();
                            localParameters.setFlashMode(CameraController.11.this.val$session.getCurrentFlashMode());
                            localCamera.setParameters(localParameters);
                            return;
                          }
                          catch (Exception localException)
                          {
                            FileLog.e(localException);
                          }
                        }
                      });
                      if ((paramBoolean) || (CameraController.this.onVideoTakeCallback == null)) {
                        break;
                      }
                      CameraController.this.finishRecordingVideo();
                      return;
                      localException6 = localException6;
                      FileLog.e(localException6);
                      continue;
                      localException2 = localException2;
                      FileLog.e(localException2);
                      continue;
                      localException3 = localException3;
                      FileLog.e(localException3);
                      continue;
                      localException4 = localException4;
                      FileLog.e(localException4);
                    }
                  }
                  catch (Exception localException5)
                  {
                    for (;;)
                    {
                      FileLog.e(localException5);
                    }
                    CameraController.access$402(CameraController.this, null);
                    return;
                  }
                }
              }
            }
          }
          return;
        }
        catch (Exception localException1) {}
      }
    });
  }
  
  public boolean takePicture(final File paramFile, final CameraSession paramCameraSession, final Runnable paramRunnable)
  {
    if (paramCameraSession == null) {
      return false;
    }
    paramCameraSession = paramCameraSession.cameraInfo;
    Camera localCamera = paramCameraSession.camera;
    try
    {
      localCamera.takePicture(null, null, new Camera.PictureCallback()
      {
        /* Error */
        public void onPictureTaken(byte[] paramAnonymousArrayOfByte, Camera paramAnonymousCamera)
        {
          // Byte code:
          //   0: aconst_null
          //   1: astore_2
          //   2: invokestatic 45	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
          //   5: i2f
          //   6: getstatic 49	org/telegram/messenger/AndroidUtilities:density	F
          //   9: fdiv
          //   10: f2i
          //   11: istore 5
          //   13: getstatic 55	java/util/Locale:US	Ljava/util/Locale;
          //   16: ldc 57
          //   18: iconst_3
          //   19: anewarray 4	java/lang/Object
          //   22: dup
          //   23: iconst_0
          //   24: aload_0
          //   25: getfield 25	org/telegram/messenger/camera/CameraController$4:val$path	Ljava/io/File;
          //   28: invokevirtual 63	java/io/File:getAbsolutePath	()Ljava/lang/String;
          //   31: invokestatic 69	org/telegram/messenger/Utilities:MD5	(Ljava/lang/String;)Ljava/lang/String;
          //   34: aastore
          //   35: dup
          //   36: iconst_1
          //   37: iload 5
          //   39: invokestatic 75	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
          //   42: aastore
          //   43: dup
          //   44: iconst_2
          //   45: iload 5
          //   47: invokestatic 75	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
          //   50: aastore
          //   51: invokestatic 81	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
          //   54: astore 7
          //   56: new 83	android/graphics/BitmapFactory$Options
          //   59: dup
          //   60: invokespecial 84	android/graphics/BitmapFactory$Options:<init>	()V
          //   63: astore 6
          //   65: aload 6
          //   67: iconst_1
          //   68: putfield 88	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
          //   71: aload_1
          //   72: iconst_0
          //   73: aload_1
          //   74: arraylength
          //   75: aload 6
          //   77: invokestatic 94	android/graphics/BitmapFactory:decodeByteArray	([BIILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
          //   80: pop
          //   81: aload 6
          //   83: getfield 98	android/graphics/BitmapFactory$Options:outWidth	I
          //   86: i2f
          //   87: invokestatic 45	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
          //   90: i2f
          //   91: fdiv
          //   92: aload 6
          //   94: getfield 101	android/graphics/BitmapFactory$Options:outHeight	I
          //   97: i2f
          //   98: invokestatic 45	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
          //   101: i2f
          //   102: fdiv
          //   103: invokestatic 107	java/lang/Math:max	(FF)F
          //   106: fstore 4
          //   108: fload 4
          //   110: fstore_3
          //   111: fload 4
          //   113: fconst_1
          //   114: fcmpg
          //   115: ifge +5 -> 120
          //   118: fconst_1
          //   119: fstore_3
          //   120: aload 6
          //   122: iconst_0
          //   123: putfield 88	android/graphics/BitmapFactory$Options:inJustDecodeBounds	Z
          //   126: aload 6
          //   128: fload_3
          //   129: f2i
          //   130: putfield 110	android/graphics/BitmapFactory$Options:inSampleSize	I
          //   133: aload 6
          //   135: iconst_1
          //   136: putfield 113	android/graphics/BitmapFactory$Options:inPurgeable	Z
          //   139: aload_1
          //   140: iconst_0
          //   141: aload_1
          //   142: arraylength
          //   143: aload 6
          //   145: invokestatic 94	android/graphics/BitmapFactory:decodeByteArray	([BIILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
          //   148: astore 6
          //   150: aload 6
          //   152: astore_2
          //   153: aload_0
          //   154: getfield 27	org/telegram/messenger/camera/CameraController$4:val$info	Lorg/telegram/messenger/camera/CameraInfo;
          //   157: getfield 118	org/telegram/messenger/camera/CameraInfo:frontCamera	I
          //   160: istore 5
          //   162: iload 5
          //   164: ifeq +160 -> 324
          //   167: new 120	android/graphics/Matrix
          //   170: dup
          //   171: invokespecial 121	android/graphics/Matrix:<init>	()V
          //   174: astore 6
          //   176: aload 6
          //   178: aload_1
          //   179: invokestatic 125	org/telegram/messenger/camera/CameraController:access$200	([B)I
          //   182: i2f
          //   183: invokevirtual 129	android/graphics/Matrix:setRotate	(F)V
          //   186: aload 6
          //   188: ldc -126
          //   190: fconst_1
          //   191: invokevirtual 134	android/graphics/Matrix:postScale	(FF)Z
          //   194: pop
          //   195: aload_2
          //   196: iconst_0
          //   197: iconst_0
          //   198: aload_2
          //   199: invokevirtual 139	android/graphics/Bitmap:getWidth	()I
          //   202: aload_2
          //   203: invokevirtual 142	android/graphics/Bitmap:getHeight	()I
          //   206: aload 6
          //   208: iconst_0
          //   209: invokestatic 148	org/telegram/messenger/Bitmaps:createBitmap	(Landroid/graphics/Bitmap;IIIILandroid/graphics/Matrix;Z)Landroid/graphics/Bitmap;
          //   212: astore 6
          //   214: aload 6
          //   216: aload_2
          //   217: if_acmpeq +7 -> 224
          //   220: aload_2
          //   221: invokevirtual 151	android/graphics/Bitmap:recycle	()V
          //   224: new 153	java/io/FileOutputStream
          //   227: dup
          //   228: aload_0
          //   229: getfield 25	org/telegram/messenger/camera/CameraController$4:val$path	Ljava/io/File;
          //   232: invokespecial 156	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
          //   235: astore 8
          //   237: aload 6
          //   239: getstatic 162	android/graphics/Bitmap$CompressFormat:JPEG	Landroid/graphics/Bitmap$CompressFormat;
          //   242: bipush 80
          //   244: aload 8
          //   246: invokevirtual 166	android/graphics/Bitmap:compress	(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z
          //   249: pop
          //   250: aload 8
          //   252: invokevirtual 169	java/io/FileOutputStream:flush	()V
          //   255: aload 8
          //   257: invokevirtual 173	java/io/FileOutputStream:getFD	()Ljava/io/FileDescriptor;
          //   260: invokevirtual 178	java/io/FileDescriptor:sync	()V
          //   263: aload 8
          //   265: invokevirtual 181	java/io/FileOutputStream:close	()V
          //   268: aload 6
          //   270: ifnull +20 -> 290
          //   273: invokestatic 187	org/telegram/messenger/ImageLoader:getInstance	()Lorg/telegram/messenger/ImageLoader;
          //   276: new 189	android/graphics/drawable/BitmapDrawable
          //   279: dup
          //   280: aload 6
          //   282: invokespecial 192	android/graphics/drawable/BitmapDrawable:<init>	(Landroid/graphics/Bitmap;)V
          //   285: aload 7
          //   287: invokevirtual 196	org/telegram/messenger/ImageLoader:putImageToCache	(Landroid/graphics/drawable/BitmapDrawable;Ljava/lang/String;)V
          //   290: aload_0
          //   291: getfield 29	org/telegram/messenger/camera/CameraController$4:val$callback	Ljava/lang/Runnable;
          //   294: ifnull +12 -> 306
          //   297: aload_0
          //   298: getfield 29	org/telegram/messenger/camera/CameraController$4:val$callback	Ljava/lang/Runnable;
          //   301: invokeinterface 201 1 0
          //   306: return
          //   307: astore 6
          //   309: aload 6
          //   311: invokestatic 207	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
          //   314: goto -161 -> 153
          //   317: astore 6
          //   319: aload 6
          //   321: invokestatic 207	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
          //   324: new 153	java/io/FileOutputStream
          //   327: dup
          //   328: aload_0
          //   329: getfield 25	org/telegram/messenger/camera/CameraController$4:val$path	Ljava/io/File;
          //   332: invokespecial 156	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
          //   335: astore 6
          //   337: aload 6
          //   339: aload_1
          //   340: invokevirtual 211	java/io/FileOutputStream:write	([B)V
          //   343: aload 6
          //   345: invokevirtual 169	java/io/FileOutputStream:flush	()V
          //   348: aload 6
          //   350: invokevirtual 173	java/io/FileOutputStream:getFD	()Ljava/io/FileDescriptor;
          //   353: invokevirtual 178	java/io/FileDescriptor:sync	()V
          //   356: aload 6
          //   358: invokevirtual 181	java/io/FileOutputStream:close	()V
          //   361: aload_2
          //   362: ifnull +19 -> 381
          //   365: invokestatic 187	org/telegram/messenger/ImageLoader:getInstance	()Lorg/telegram/messenger/ImageLoader;
          //   368: new 189	android/graphics/drawable/BitmapDrawable
          //   371: dup
          //   372: aload_2
          //   373: invokespecial 192	android/graphics/drawable/BitmapDrawable:<init>	(Landroid/graphics/Bitmap;)V
          //   376: aload 7
          //   378: invokevirtual 196	org/telegram/messenger/ImageLoader:putImageToCache	(Landroid/graphics/drawable/BitmapDrawable;Ljava/lang/String;)V
          //   381: aload_0
          //   382: getfield 29	org/telegram/messenger/camera/CameraController$4:val$callback	Ljava/lang/Runnable;
          //   385: ifnull -79 -> 306
          //   388: aload_0
          //   389: getfield 29	org/telegram/messenger/camera/CameraController$4:val$callback	Ljava/lang/Runnable;
          //   392: invokeinterface 201 1 0
          //   397: return
          //   398: astore_1
          //   399: aload_1
          //   400: invokestatic 207	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
          //   403: goto -22 -> 381
          // Local variable table:
          //   start	length	slot	name	signature
          //   0	406	0	this	4
          //   0	406	1	paramAnonymousArrayOfByte	byte[]
          //   0	406	2	paramAnonymousCamera	Camera
          //   110	19	3	f1	float
          //   106	6	4	f2	float
          //   11	152	5	i	int
          //   63	218	6	localObject	Object
          //   307	3	6	localThrowable1	Throwable
          //   317	3	6	localThrowable2	Throwable
          //   335	22	6	localFileOutputStream1	java.io.FileOutputStream
          //   54	323	7	str	String
          //   235	29	8	localFileOutputStream2	java.io.FileOutputStream
          // Exception table:
          //   from	to	target	type
          //   56	108	307	java/lang/Throwable
          //   120	150	307	java/lang/Throwable
          //   167	214	317	java/lang/Throwable
          //   220	224	317	java/lang/Throwable
          //   224	268	317	java/lang/Throwable
          //   273	290	317	java/lang/Throwable
          //   290	306	317	java/lang/Throwable
          //   153	162	398	java/lang/Exception
          //   167	214	398	java/lang/Exception
          //   220	224	398	java/lang/Exception
          //   224	268	398	java/lang/Exception
          //   273	290	398	java/lang/Exception
          //   290	306	398	java/lang/Exception
          //   319	324	398	java/lang/Exception
          //   324	361	398	java/lang/Exception
          //   365	381	398	java/lang/Exception
        }
      });
      return true;
    }
    catch (Exception paramFile)
    {
      FileLog.e(paramFile);
    }
    return false;
  }
  
  static class CompareSizesByArea
    implements Comparator<Size>
  {
    public int compare(Size paramSize1, Size paramSize2)
    {
      return Long.signum(paramSize1.getWidth() * paramSize1.getHeight() - paramSize2.getWidth() * paramSize2.getHeight());
    }
  }
  
  public static abstract interface VideoTakeCallback
  {
    public abstract void onFinishVideoRecording(String paramString, long paramLong);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/camera/CameraController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */