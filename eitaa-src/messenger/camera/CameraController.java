package ir.eitaa.messenger.camera;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.media.ThumbnailUtils;
import android.os.Build;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.NotificationCenter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CameraController
  implements MediaRecorder.OnInfoListener
{
  private static final int CORE_POOL_SIZE = 1;
  private static volatile CameraController Instance = null;
  private static final int KEEP_ALIVE_SECONDS = 60;
  private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
  protected ArrayList<String> availableFlashModes = new ArrayList();
  protected ArrayList<CameraInfo> cameraInfos = null;
  private boolean cameraInitied;
  private VideoTakeCallback onVideoTakeCallback;
  private String recordedFile;
  private MediaRecorder recorder;
  private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, MAX_POOL_SIZE, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue());
  
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
    label410:
    label412:
    label416:
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
              break label412;
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
          break label416;
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
            break label410;
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
            localCameraInfo.camera.release();
            localCameraInfo.camera = null;
          }
          i += 1;
        }
        CameraController.this.cameraInfos = null;
      }
    });
  }
  
  public void close(CameraSession paramCameraSession, final Semaphore paramSemaphore)
  {
    paramCameraSession.destroy();
    final Camera localCamera = paramCameraSession.cameraInfo.camera;
    paramCameraSession.cameraInfo.camera = null;
    this.threadPool.execute(new Runnable()
    {
      public void run()
      {
        try
        {
          if (localCamera != null)
          {
            localCamera.stopPreview();
            localCamera.release();
          }
          if (paramSemaphore != null) {
            paramSemaphore.release();
          }
          return;
        }
        catch (Exception localException)
        {
          for (;;)
          {
            FileLog.e("TSMS", localException);
          }
        }
      }
    });
    if (paramSemaphore != null) {}
    try
    {
      paramSemaphore.acquire();
      return;
    }
    catch (Exception paramCameraSession)
    {
      FileLog.e("TSMS", paramCameraSession);
    }
  }
  
  public ArrayList<CameraInfo> getCameras()
  {
    return this.cameraInfos;
  }
  
  public void initCamera()
  {
    if (this.cameraInitied) {
      return;
    }
    this.threadPool.execute(new Runnable()
    {
      public void run()
      {
        for (;;)
        {
          int j;
          try
          {
            if (CameraController.this.cameraInfos == null)
            {
              int k = Camera.getNumberOfCameras();
              ArrayList localArrayList = new ArrayList();
              Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
              int i = 0;
              if (i < k)
              {
                Camera.getCameraInfo(i, localCameraInfo);
                CameraInfo localCameraInfo1 = new CameraInfo(i, localCameraInfo);
                Camera localCamera = Camera.open(localCameraInfo1.getCameraId());
                Object localObject1 = localCamera.getParameters();
                Object localObject2 = ((Camera.Parameters)localObject1).getSupportedPreviewSizes();
                j = 0;
                if (j < ((List)localObject2).size())
                {
                  Camera.Size localSize = (Camera.Size)((List)localObject2).get(j);
                  if ((localSize.height < 2160) && (localSize.width < 2160)) {
                    localCameraInfo1.previewSizes.add(new Size(localSize.width, localSize.height));
                  }
                }
                else
                {
                  localObject1 = ((Camera.Parameters)localObject1).getSupportedPictureSizes();
                  j = 0;
                  if (j < ((List)localObject1).size())
                  {
                    localObject2 = (Camera.Size)((List)localObject1).get(j);
                    if (("samsung".equals(Build.MANUFACTURER)) && ("jflteuc".equals(Build.PRODUCT)) && (((Camera.Size)localObject2).width >= 2048)) {
                      break label311;
                    }
                    localCameraInfo1.pictureSizes.add(new Size(((Camera.Size)localObject2).width, ((Camera.Size)localObject2).height));
                    break label311;
                  }
                  localCamera.release();
                  localArrayList.add(localCameraInfo1);
                  i += 1;
                }
              }
              else
              {
                CameraController.this.cameraInfos = localArrayList;
              }
            }
            else
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  CameraController.access$002(CameraController.this, true);
                  NotificationCenter.getInstance().postNotificationName(NotificationCenter.cameraInitied, new Object[0]);
                }
              });
              return;
            }
          }
          catch (Exception localException)
          {
            FileLog.e("TSMS", localException);
            return;
          }
          j += 1;
          continue;
          label311:
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
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          CameraController.this.onVideoTakeCallback.onFinishVideoRecording(this.val$bitmap);
        }
      });
    }
  }
  
  public void open(final CameraSession paramCameraSession, final SurfaceTexture paramSurfaceTexture, final Runnable paramRunnable)
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
                      break label264;
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
              paramCameraSession.configurePhotoCamera();
              localObject2 = localObject1;
              ((Camera)localObject1).setPreviewTexture(paramSurfaceTexture);
              localObject2 = localObject1;
              ((Camera)localObject1).startPreview();
              localObject2 = localObject1;
              if (paramRunnable != null)
              {
                localObject2 = localObject1;
                AndroidUtilities.runOnUIThread(paramRunnable);
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
            FileLog.e("TSMS", localException);
            return;
          }
          label264:
          i += 1;
        }
      }
    });
  }
  
  public void recordVideo(CameraSession paramCameraSession, File paramFile, VideoTakeCallback paramVideoTakeCallback)
  {
    if (paramCameraSession == null) {}
    for (;;)
    {
      return;
      try
      {
        CameraInfo localCameraInfo = paramCameraSession.cameraInfo;
        Camera localCamera = localCameraInfo.camera;
        if (localCamera == null) {
          continue;
        }
        localCamera.stopPreview();
        localCamera.unlock();
        try
        {
          this.recorder = new MediaRecorder();
          this.recorder.setCamera(localCamera);
          this.recorder.setVideoSource(1);
          this.recorder.setAudioSource(5);
          paramCameraSession.configureRecorder(1, this.recorder);
          this.recorder.setOutputFile(paramFile.getAbsolutePath());
          this.recorder.setMaxFileSize(1073741824L);
          this.recorder.setVideoFrameRate(30);
          this.recorder.setMaxDuration(0);
          paramCameraSession = new Size(16, 9);
          paramCameraSession = chooseOptimalSize(localCameraInfo.getPictureSizes(), 720, 480, paramCameraSession);
          this.recorder.setVideoSize(paramCameraSession.getWidth(), paramCameraSession.getHeight());
          this.recorder.setVideoEncodingBitRate(1800000);
          this.recorder.setOnInfoListener(this);
          this.recorder.prepare();
          this.recorder.start();
          this.onVideoTakeCallback = paramVideoTakeCallback;
          this.recordedFile = paramFile.getAbsolutePath();
          return;
        }
        catch (Exception paramCameraSession)
        {
          this.recorder.release();
          this.recorder = null;
          FileLog.e("TSMS", paramCameraSession);
          return;
        }
        return;
      }
      catch (Exception paramCameraSession)
      {
        FileLog.e("TSMS", paramCameraSession);
      }
    }
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
          FileLog.e("TSMS", localException);
        }
      }
    });
  }
  
  public void stopVideoRecording(CameraSession paramCameraSession, boolean paramBoolean)
  {
    try
    {
      Camera localCamera = paramCameraSession.cameraInfo.camera;
      if ((localCamera != null) && (this.recorder != null))
      {
        MediaRecorder localMediaRecorder = this.recorder;
        this.recorder = null;
        localMediaRecorder.stop();
        localMediaRecorder.release();
        localCamera.reconnect();
        localCamera.startPreview();
        paramCameraSession.stopVideoRecording();
      }
      if (!paramBoolean) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            CameraController.this.onVideoTakeCallback.onFinishVideoRecording(this.val$bitmap);
          }
        });
      }
      return;
    }
    catch (Exception paramCameraSession)
    {
      FileLog.e("TSMS", paramCameraSession);
    }
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
          //   2: invokestatic 45	ir/eitaa/messenger/AndroidUtilities:getPhotoSize	()I
          //   5: i2f
          //   6: getstatic 49	ir/eitaa/messenger/AndroidUtilities:density	F
          //   9: fdiv
          //   10: f2i
          //   11: istore_3
          //   12: getstatic 55	java/util/Locale:US	Ljava/util/Locale;
          //   15: ldc 57
          //   17: iconst_3
          //   18: anewarray 4	java/lang/Object
          //   21: dup
          //   22: iconst_0
          //   23: aload_0
          //   24: getfield 25	ir/eitaa/messenger/camera/CameraController$4:val$path	Ljava/io/File;
          //   27: invokevirtual 63	java/io/File:getAbsolutePath	()Ljava/lang/String;
          //   30: invokestatic 69	ir/eitaa/messenger/Utilities:MD5	(Ljava/lang/String;)Ljava/lang/String;
          //   33: aastore
          //   34: dup
          //   35: iconst_1
          //   36: iload_3
          //   37: invokestatic 75	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
          //   40: aastore
          //   41: dup
          //   42: iconst_2
          //   43: iload_3
          //   44: invokestatic 75	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
          //   47: aastore
          //   48: invokestatic 81	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
          //   51: astore 5
          //   53: new 83	android/graphics/BitmapFactory$Options
          //   56: dup
          //   57: invokespecial 84	android/graphics/BitmapFactory$Options:<init>	()V
          //   60: astore 4
          //   62: aload 4
          //   64: iconst_1
          //   65: putfield 88	android/graphics/BitmapFactory$Options:inPurgeable	Z
          //   68: aload_1
          //   69: iconst_0
          //   70: aload_1
          //   71: arraylength
          //   72: aload 4
          //   74: invokestatic 94	android/graphics/BitmapFactory:decodeByteArray	([BIILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;
          //   77: astore 4
          //   79: aload 4
          //   81: astore_2
          //   82: aload_0
          //   83: getfield 27	ir/eitaa/messenger/camera/CameraController$4:val$info	Lir/eitaa/messenger/camera/CameraInfo;
          //   86: getfield 100	ir/eitaa/messenger/camera/CameraInfo:frontCamera	I
          //   89: istore_3
          //   90: iload_3
          //   91: ifeq +158 -> 249
          //   94: new 102	android/graphics/Matrix
          //   97: dup
          //   98: invokespecial 103	android/graphics/Matrix:<init>	()V
          //   101: astore 4
          //   103: aload 4
          //   105: aload_1
          //   106: invokestatic 107	ir/eitaa/messenger/camera/CameraController:access$100	([B)I
          //   109: i2f
          //   110: invokevirtual 111	android/graphics/Matrix:setRotate	(F)V
          //   113: aload 4
          //   115: ldc 112
          //   117: fconst_1
          //   118: invokevirtual 116	android/graphics/Matrix:postScale	(FF)Z
          //   121: pop
          //   122: aload_2
          //   123: iconst_0
          //   124: iconst_0
          //   125: aload_2
          //   126: invokevirtual 121	android/graphics/Bitmap:getWidth	()I
          //   129: aload_2
          //   130: invokevirtual 124	android/graphics/Bitmap:getHeight	()I
          //   133: aload 4
          //   135: iconst_0
          //   136: invokestatic 130	ir/eitaa/messenger/Bitmaps:createBitmap	(Landroid/graphics/Bitmap;IIIILandroid/graphics/Matrix;Z)Landroid/graphics/Bitmap;
          //   139: astore 4
          //   141: aload_2
          //   142: invokevirtual 133	android/graphics/Bitmap:recycle	()V
          //   145: new 135	java/io/FileOutputStream
          //   148: dup
          //   149: aload_0
          //   150: getfield 25	ir/eitaa/messenger/camera/CameraController$4:val$path	Ljava/io/File;
          //   153: invokespecial 138	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
          //   156: astore 6
          //   158: aload 4
          //   160: getstatic 144	android/graphics/Bitmap$CompressFormat:JPEG	Landroid/graphics/Bitmap$CompressFormat;
          //   163: bipush 80
          //   165: aload 6
          //   167: invokevirtual 148	android/graphics/Bitmap:compress	(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z
          //   170: pop
          //   171: aload 6
          //   173: invokevirtual 151	java/io/FileOutputStream:flush	()V
          //   176: aload 6
          //   178: invokevirtual 155	java/io/FileOutputStream:getFD	()Ljava/io/FileDescriptor;
          //   181: invokevirtual 160	java/io/FileDescriptor:sync	()V
          //   184: aload 6
          //   186: invokevirtual 163	java/io/FileOutputStream:close	()V
          //   189: aload 4
          //   191: ifnull +20 -> 211
          //   194: invokestatic 169	ir/eitaa/messenger/ImageLoader:getInstance	()Lir/eitaa/messenger/ImageLoader;
          //   197: new 171	android/graphics/drawable/BitmapDrawable
          //   200: dup
          //   201: aload 4
          //   203: invokespecial 174	android/graphics/drawable/BitmapDrawable:<init>	(Landroid/graphics/Bitmap;)V
          //   206: aload 5
          //   208: invokevirtual 178	ir/eitaa/messenger/ImageLoader:putImageToCache	(Landroid/graphics/drawable/BitmapDrawable;Ljava/lang/String;)V
          //   211: aload_0
          //   212: getfield 29	ir/eitaa/messenger/camera/CameraController$4:val$callback	Ljava/lang/Runnable;
          //   215: ifnull +12 -> 227
          //   218: aload_0
          //   219: getfield 29	ir/eitaa/messenger/camera/CameraController$4:val$callback	Ljava/lang/Runnable;
          //   222: invokeinterface 183 1 0
          //   227: return
          //   228: astore 4
          //   230: ldc -71
          //   232: aload 4
          //   234: invokestatic 191	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
          //   237: goto -155 -> 82
          //   240: astore 4
          //   242: ldc -71
          //   244: aload 4
          //   246: invokestatic 191	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
          //   249: new 135	java/io/FileOutputStream
          //   252: dup
          //   253: aload_0
          //   254: getfield 25	ir/eitaa/messenger/camera/CameraController$4:val$path	Ljava/io/File;
          //   257: invokespecial 138	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
          //   260: astore 4
          //   262: aload 4
          //   264: aload_1
          //   265: invokevirtual 195	java/io/FileOutputStream:write	([B)V
          //   268: aload 4
          //   270: invokevirtual 151	java/io/FileOutputStream:flush	()V
          //   273: aload 4
          //   275: invokevirtual 155	java/io/FileOutputStream:getFD	()Ljava/io/FileDescriptor;
          //   278: invokevirtual 160	java/io/FileDescriptor:sync	()V
          //   281: aload 4
          //   283: invokevirtual 163	java/io/FileOutputStream:close	()V
          //   286: aload_2
          //   287: ifnull +19 -> 306
          //   290: invokestatic 169	ir/eitaa/messenger/ImageLoader:getInstance	()Lir/eitaa/messenger/ImageLoader;
          //   293: new 171	android/graphics/drawable/BitmapDrawable
          //   296: dup
          //   297: aload_2
          //   298: invokespecial 174	android/graphics/drawable/BitmapDrawable:<init>	(Landroid/graphics/Bitmap;)V
          //   301: aload 5
          //   303: invokevirtual 178	ir/eitaa/messenger/ImageLoader:putImageToCache	(Landroid/graphics/drawable/BitmapDrawable;Ljava/lang/String;)V
          //   306: aload_0
          //   307: getfield 29	ir/eitaa/messenger/camera/CameraController$4:val$callback	Ljava/lang/Runnable;
          //   310: ifnull -83 -> 227
          //   313: aload_0
          //   314: getfield 29	ir/eitaa/messenger/camera/CameraController$4:val$callback	Ljava/lang/Runnable;
          //   317: invokeinterface 183 1 0
          //   322: return
          //   323: astore_1
          //   324: ldc -71
          //   326: aload_1
          //   327: invokestatic 191	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
          //   330: goto -24 -> 306
          // Local variable table:
          //   start	length	slot	name	signature
          //   0	333	0	this	4
          //   0	333	1	paramAnonymousArrayOfByte	byte[]
          //   0	333	2	paramAnonymousCamera	Camera
          //   11	80	3	i	int
          //   60	142	4	localObject	Object
          //   228	5	4	localThrowable1	Throwable
          //   240	5	4	localThrowable2	Throwable
          //   260	22	4	localFileOutputStream1	java.io.FileOutputStream
          //   51	251	5	str	String
          //   156	29	6	localFileOutputStream2	java.io.FileOutputStream
          // Exception table:
          //   from	to	target	type
          //   53	79	228	java/lang/Throwable
          //   94	189	240	java/lang/Throwable
          //   194	211	240	java/lang/Throwable
          //   211	227	240	java/lang/Throwable
          //   82	90	323	java/lang/Exception
          //   94	189	323	java/lang/Exception
          //   194	211	323	java/lang/Exception
          //   211	227	323	java/lang/Exception
          //   242	249	323	java/lang/Exception
          //   249	286	323	java/lang/Exception
          //   290	306	323	java/lang/Exception
        }
      });
      return true;
    }
    catch (Exception paramFile)
    {
      FileLog.e("TSMS", paramFile);
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
    public abstract void onFinishVideoRecording(Bitmap paramBitmap);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/camera/CameraController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */