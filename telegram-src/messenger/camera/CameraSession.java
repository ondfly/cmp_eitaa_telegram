package org.telegram.messenger.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.List;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

public class CameraSession
{
  public static final int ORIENTATION_HYSTERESIS = 5;
  private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback()
  {
    public void onAutoFocus(boolean paramAnonymousBoolean, Camera paramAnonymousCamera)
    {
      if (paramAnonymousBoolean) {}
    }
  };
  protected CameraInfo cameraInfo;
  private String currentFlashMode = "off";
  private int currentOrientation;
  private int diffOrientation;
  private boolean initied;
  private boolean isVideo;
  private int jpegOrientation;
  private int lastDisplayOrientation = -1;
  private int lastOrientation = -1;
  private boolean meteringAreaSupported;
  private OrientationEventListener orientationEventListener;
  private final int pictureFormat;
  private final Size pictureSize;
  private final Size previewSize;
  private boolean sameTakePictureOrientation;
  
  public CameraSession(CameraInfo paramCameraInfo, Size paramSize1, Size paramSize2, int paramInt)
  {
    this.previewSize = paramSize1;
    this.pictureSize = paramSize2;
    this.pictureFormat = paramInt;
    this.cameraInfo = paramCameraInfo;
    paramSize1 = ApplicationLoader.applicationContext.getSharedPreferences("camera", 0);
    if (this.cameraInfo.frontCamera != 0) {}
    for (paramCameraInfo = "flashMode_front";; paramCameraInfo = "flashMode")
    {
      this.currentFlashMode = paramSize1.getString(paramCameraInfo, "off");
      this.orientationEventListener = new OrientationEventListener(ApplicationLoader.applicationContext)
      {
        public void onOrientationChanged(int paramAnonymousInt)
        {
          if ((CameraSession.this.orientationEventListener == null) || (!CameraSession.this.initied) || (paramAnonymousInt == -1)) {}
          do
          {
            return;
            CameraSession.access$202(CameraSession.this, CameraSession.this.roundOrientation(paramAnonymousInt, CameraSession.this.jpegOrientation));
            paramAnonymousInt = ((WindowManager)ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
          } while ((CameraSession.this.lastOrientation == CameraSession.this.jpegOrientation) && (paramAnonymousInt == CameraSession.this.lastDisplayOrientation));
          if (!CameraSession.this.isVideo) {
            CameraSession.this.configurePhotoCamera();
          }
          CameraSession.access$502(CameraSession.this, paramAnonymousInt);
          CameraSession.access$402(CameraSession.this, CameraSession.this.jpegOrientation);
        }
      };
      if (!this.orientationEventListener.canDetectOrientation()) {
        break;
      }
      this.orientationEventListener.enable();
      return;
    }
    this.orientationEventListener.disable();
    this.orientationEventListener = null;
  }
  
  private int getDisplayOrientation(Camera.CameraInfo paramCameraInfo, boolean paramBoolean)
  {
    int j = ((WindowManager)ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
    int i = 0;
    switch (j)
    {
    }
    while (paramCameraInfo.facing == 1)
    {
      j = (360 - (paramCameraInfo.orientation + i) % 360) % 360;
      i = j;
      if (!paramBoolean)
      {
        i = j;
        if (j == 90) {
          i = 270;
        }
      }
      j = i;
      if (!paramBoolean)
      {
        j = i;
        if ("Huawei".equals(Build.MANUFACTURER))
        {
          j = i;
          if ("angler".equals(Build.PRODUCT))
          {
            j = i;
            if (i == 270) {
              j = 90;
            }
          }
        }
      }
      return j;
      i = 0;
      continue;
      i = 90;
      continue;
      i = 180;
      continue;
      i = 270;
    }
    return (paramCameraInfo.orientation - i + 360) % 360;
  }
  
  private int getHigh()
  {
    if (("LGE".equals(Build.MANUFACTURER)) && ("g3_tmo_us".equals(Build.PRODUCT))) {
      return 4;
    }
    return 1;
  }
  
  private int roundOrientation(int paramInt1, int paramInt2)
  {
    if (paramInt2 == -1)
    {
      i = 1;
      if (i != 0) {
        paramInt2 = (paramInt1 + 45) / 90 * 90 % 360;
      }
      return paramInt2;
    }
    int i = Math.abs(paramInt1 - paramInt2);
    if (Math.min(i, 360 - i) >= 50) {}
    for (i = 1;; i = 0) {
      break;
    }
  }
  
  public void checkFlashMode(String paramString)
  {
    if (CameraController.getInstance().availableFlashModes.contains(this.currentFlashMode)) {
      return;
    }
    this.currentFlashMode = paramString;
    configurePhotoCamera();
    SharedPreferences.Editor localEditor = ApplicationLoader.applicationContext.getSharedPreferences("camera", 0).edit();
    if (this.cameraInfo.frontCamera != 0) {}
    for (String str = "flashMode_front";; str = "flashMode")
    {
      localEditor.putString(str, paramString).commit();
      return;
    }
  }
  
  /* Error */
  protected void configurePhotoCamera()
  {
    // Byte code:
    //   0: iconst_1
    //   1: istore 4
    //   3: iconst_1
    //   4: istore_3
    //   5: aload_0
    //   6: getfield 59	org/telegram/messenger/camera/CameraSession:cameraInfo	Lorg/telegram/messenger/camera/CameraInfo;
    //   9: getfield 231	org/telegram/messenger/camera/CameraInfo:camera	Landroid/hardware/Camera;
    //   12: astore 7
    //   14: aload 7
    //   16: ifnull +430 -> 446
    //   19: new 153	android/hardware/Camera$CameraInfo
    //   22: dup
    //   23: invokespecial 232	android/hardware/Camera$CameraInfo:<init>	()V
    //   26: astore 8
    //   28: aconst_null
    //   29: astore 5
    //   31: aload 7
    //   33: invokevirtual 238	android/hardware/Camera:getParameters	()Landroid/hardware/Camera$Parameters;
    //   36: astore 6
    //   38: aload 6
    //   40: astore 5
    //   42: aload_0
    //   43: getfield 59	org/telegram/messenger/camera/CameraSession:cameraInfo	Lorg/telegram/messenger/camera/CameraInfo;
    //   46: invokevirtual 241	org/telegram/messenger/camera/CameraInfo:getCameraId	()I
    //   49: aload 8
    //   51: invokestatic 245	android/hardware/Camera:getCameraInfo	(ILandroid/hardware/Camera$CameraInfo;)V
    //   54: aload_0
    //   55: aload 8
    //   57: iconst_1
    //   58: invokespecial 247	org/telegram/messenger/camera/CameraSession:getDisplayOrientation	(Landroid/hardware/Camera$CameraInfo;Z)I
    //   61: istore_2
    //   62: ldc -7
    //   64: getstatic 166	android/os/Build:MANUFACTURER	Ljava/lang/String;
    //   67: invokevirtual 172	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   70: ifeq +222 -> 292
    //   73: ldc -5
    //   75: getstatic 177	android/os/Build:PRODUCT	Ljava/lang/String;
    //   78: invokevirtual 172	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   81: ifeq +211 -> 292
    //   84: iconst_0
    //   85: istore_1
    //   86: aload_0
    //   87: iload_1
    //   88: putfield 253	org/telegram/messenger/camera/CameraSession:currentOrientation	I
    //   91: aload 7
    //   93: iload_1
    //   94: invokevirtual 257	android/hardware/Camera:setDisplayOrientation	(I)V
    //   97: aload 5
    //   99: ifnull +347 -> 446
    //   102: aload 5
    //   104: aload_0
    //   105: getfield 53	org/telegram/messenger/camera/CameraSession:previewSize	Lorg/telegram/messenger/camera/Size;
    //   108: invokevirtual 262	org/telegram/messenger/camera/Size:getWidth	()I
    //   111: aload_0
    //   112: getfield 53	org/telegram/messenger/camera/CameraSession:previewSize	Lorg/telegram/messenger/camera/Size;
    //   115: invokevirtual 265	org/telegram/messenger/camera/Size:getHeight	()I
    //   118: invokevirtual 271	android/hardware/Camera$Parameters:setPreviewSize	(II)V
    //   121: aload 5
    //   123: aload_0
    //   124: getfield 55	org/telegram/messenger/camera/CameraSession:pictureSize	Lorg/telegram/messenger/camera/Size;
    //   127: invokevirtual 262	org/telegram/messenger/camera/Size:getWidth	()I
    //   130: aload_0
    //   131: getfield 55	org/telegram/messenger/camera/CameraSession:pictureSize	Lorg/telegram/messenger/camera/Size;
    //   134: invokevirtual 265	org/telegram/messenger/camera/Size:getHeight	()I
    //   137: invokevirtual 274	android/hardware/Camera$Parameters:setPictureSize	(II)V
    //   140: aload 5
    //   142: aload_0
    //   143: getfield 57	org/telegram/messenger/camera/CameraSession:pictureFormat	I
    //   146: invokevirtual 277	android/hardware/Camera$Parameters:setPictureFormat	(I)V
    //   149: aload 5
    //   151: invokevirtual 281	android/hardware/Camera$Parameters:getSupportedFocusModes	()Ljava/util/List;
    //   154: ldc_w 283
    //   157: invokeinterface 286 2 0
    //   162: ifeq +11 -> 173
    //   165: aload 5
    //   167: ldc_w 283
    //   170: invokevirtual 289	android/hardware/Camera$Parameters:setFocusMode	(Ljava/lang/String;)V
    //   173: iconst_0
    //   174: istore_1
    //   175: aload_0
    //   176: getfield 116	org/telegram/messenger/camera/CameraSession:jpegOrientation	I
    //   179: iconst_m1
    //   180: if_icmpeq +31 -> 211
    //   183: aload 8
    //   185: getfield 156	android/hardware/Camera$CameraInfo:facing	I
    //   188: iconst_1
    //   189: if_icmpne +203 -> 392
    //   192: aload 8
    //   194: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   197: aload_0
    //   198: getfield 116	org/telegram/messenger/camera/CameraSession:jpegOrientation	I
    //   201: isub
    //   202: sipush 360
    //   205: iadd
    //   206: sipush 360
    //   209: irem
    //   210: istore_1
    //   211: aload 5
    //   213: iload_1
    //   214: invokevirtual 292	android/hardware/Camera$Parameters:setRotation	(I)V
    //   217: aload 8
    //   219: getfield 156	android/hardware/Camera$CameraInfo:facing	I
    //   222: iconst_1
    //   223: if_icmpne +192 -> 415
    //   226: sipush 360
    //   229: iload_2
    //   230: isub
    //   231: sipush 360
    //   234: irem
    //   235: iload_1
    //   236: if_icmpne +174 -> 410
    //   239: aload_0
    //   240: iload_3
    //   241: putfield 294	org/telegram/messenger/camera/CameraSession:sameTakePictureOrientation	Z
    //   244: aload 5
    //   246: aload_0
    //   247: getfield 42	org/telegram/messenger/camera/CameraSession:currentFlashMode	Ljava/lang/String;
    //   250: invokevirtual 297	android/hardware/Camera$Parameters:setFlashMode	(Ljava/lang/String;)V
    //   253: aload 7
    //   255: aload 5
    //   257: invokevirtual 301	android/hardware/Camera:setParameters	(Landroid/hardware/Camera$Parameters;)V
    //   260: aload 5
    //   262: invokevirtual 304	android/hardware/Camera$Parameters:getMaxNumMeteringAreas	()I
    //   265: ifle +181 -> 446
    //   268: aload_0
    //   269: iconst_1
    //   270: putfield 306	org/telegram/messenger/camera/CameraSession:meteringAreaSupported	Z
    //   273: return
    //   274: astore 6
    //   276: aload 6
    //   278: invokestatic 312	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   281: goto -239 -> 42
    //   284: astore 5
    //   286: aload 5
    //   288: invokestatic 312	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   291: return
    //   292: iconst_0
    //   293: istore_1
    //   294: iload_2
    //   295: tableswitch	default:+29->324, 0:+155->450, 1:+160->455, 2:+166->461, 3:+173->468
    //   324: aload 8
    //   326: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   329: bipush 90
    //   331: irem
    //   332: ifeq +9 -> 341
    //   335: aload 8
    //   337: iconst_0
    //   338: putfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   341: aload 8
    //   343: getfield 156	android/hardware/Camera$CameraInfo:facing	I
    //   346: iconst_1
    //   347: if_icmpne +26 -> 373
    //   350: sipush 360
    //   353: aload 8
    //   355: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   358: iload_1
    //   359: iadd
    //   360: sipush 360
    //   363: irem
    //   364: isub
    //   365: sipush 360
    //   368: irem
    //   369: istore_1
    //   370: goto +77 -> 447
    //   373: aload 8
    //   375: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   378: iload_1
    //   379: isub
    //   380: sipush 360
    //   383: iadd
    //   384: sipush 360
    //   387: irem
    //   388: istore_1
    //   389: goto +58 -> 447
    //   392: aload 8
    //   394: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   397: aload_0
    //   398: getfield 116	org/telegram/messenger/camera/CameraSession:jpegOrientation	I
    //   401: iadd
    //   402: sipush 360
    //   405: irem
    //   406: istore_1
    //   407: goto -196 -> 211
    //   410: iconst_0
    //   411: istore_3
    //   412: goto -173 -> 239
    //   415: iload_2
    //   416: iload_1
    //   417: if_icmpne +19 -> 436
    //   420: iload 4
    //   422: istore_3
    //   423: aload_0
    //   424: iload_3
    //   425: putfield 294	org/telegram/messenger/camera/CameraSession:sameTakePictureOrientation	Z
    //   428: goto -184 -> 244
    //   431: astore 6
    //   433: goto -189 -> 244
    //   436: iconst_0
    //   437: istore_3
    //   438: goto -15 -> 423
    //   441: astore 6
    //   443: goto -183 -> 260
    //   446: return
    //   447: goto -361 -> 86
    //   450: iconst_0
    //   451: istore_1
    //   452: goto -128 -> 324
    //   455: bipush 90
    //   457: istore_1
    //   458: goto -134 -> 324
    //   461: sipush 180
    //   464: istore_1
    //   465: goto -141 -> 324
    //   468: sipush 270
    //   471: istore_1
    //   472: goto -148 -> 324
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	475	0	this	CameraSession
    //   85	387	1	i	int
    //   61	357	2	j	int
    //   4	434	3	bool1	boolean
    //   1	420	4	bool2	boolean
    //   29	232	5	localObject	Object
    //   284	3	5	localThrowable	Throwable
    //   36	3	6	localParameters	Camera.Parameters
    //   274	3	6	localException1	Exception
    //   431	1	6	localException2	Exception
    //   441	1	6	localException3	Exception
    //   12	242	7	localCamera	Camera
    //   26	367	8	localCameraInfo	Camera.CameraInfo
    // Exception table:
    //   from	to	target	type
    //   31	38	274	java/lang/Exception
    //   5	14	284	java/lang/Throwable
    //   19	28	284	java/lang/Throwable
    //   31	38	284	java/lang/Throwable
    //   42	84	284	java/lang/Throwable
    //   86	97	284	java/lang/Throwable
    //   102	173	284	java/lang/Throwable
    //   175	211	284	java/lang/Throwable
    //   211	226	284	java/lang/Throwable
    //   239	244	284	java/lang/Throwable
    //   244	253	284	java/lang/Throwable
    //   253	260	284	java/lang/Throwable
    //   260	273	284	java/lang/Throwable
    //   276	281	284	java/lang/Throwable
    //   324	341	284	java/lang/Throwable
    //   341	370	284	java/lang/Throwable
    //   373	389	284	java/lang/Throwable
    //   392	407	284	java/lang/Throwable
    //   423	428	284	java/lang/Throwable
    //   211	226	431	java/lang/Exception
    //   239	244	431	java/lang/Exception
    //   423	428	431	java/lang/Exception
    //   253	260	441	java/lang/Exception
  }
  
  protected void configureRecorder(int paramInt, MediaRecorder paramMediaRecorder)
  {
    Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
    Camera.getCameraInfo(this.cameraInfo.cameraId, localCameraInfo);
    getDisplayOrientation(localCameraInfo, false);
    int i = 0;
    boolean bool2;
    if (this.jpegOrientation != -1)
    {
      if (localCameraInfo.facing == 1) {
        i = (localCameraInfo.orientation - this.jpegOrientation + 360) % 360;
      }
    }
    else
    {
      paramMediaRecorder.setOrientationHint(i);
      i = getHigh();
      boolean bool1 = CamcorderProfile.hasProfile(this.cameraInfo.cameraId, i);
      bool2 = CamcorderProfile.hasProfile(this.cameraInfo.cameraId, 0);
      if ((!bool1) || ((paramInt != 1) && (bool2))) {
        break label157;
      }
      paramMediaRecorder.setProfile(CamcorderProfile.get(this.cameraInfo.cameraId, i));
    }
    for (;;)
    {
      this.isVideo = true;
      return;
      i = (localCameraInfo.orientation + this.jpegOrientation) % 360;
      break;
      label157:
      if (!bool2) {
        break label180;
      }
      paramMediaRecorder.setProfile(CamcorderProfile.get(this.cameraInfo.cameraId, 0));
    }
    label180:
    throw new IllegalStateException("cannot find valid CamcorderProfile");
  }
  
  /* Error */
  protected void configureRoundCamera()
  {
    // Byte code:
    //   0: iconst_1
    //   1: istore 4
    //   3: iconst_1
    //   4: istore_3
    //   5: aload_0
    //   6: iconst_1
    //   7: putfield 131	org/telegram/messenger/camera/CameraSession:isVideo	Z
    //   10: aload_0
    //   11: getfield 59	org/telegram/messenger/camera/CameraSession:cameraInfo	Lorg/telegram/messenger/camera/CameraInfo;
    //   14: getfield 231	org/telegram/messenger/camera/CameraInfo:camera	Landroid/hardware/Camera;
    //   17: astore 7
    //   19: aload 7
    //   21: ifnull +549 -> 570
    //   24: new 153	android/hardware/Camera$CameraInfo
    //   27: dup
    //   28: invokespecial 232	android/hardware/Camera$CameraInfo:<init>	()V
    //   31: astore 8
    //   33: aconst_null
    //   34: astore 5
    //   36: aload 7
    //   38: invokevirtual 238	android/hardware/Camera:getParameters	()Landroid/hardware/Camera$Parameters;
    //   41: astore 6
    //   43: aload 6
    //   45: astore 5
    //   47: aload_0
    //   48: getfield 59	org/telegram/messenger/camera/CameraSession:cameraInfo	Lorg/telegram/messenger/camera/CameraInfo;
    //   51: invokevirtual 241	org/telegram/messenger/camera/CameraInfo:getCameraId	()I
    //   54: aload 8
    //   56: invokestatic 245	android/hardware/Camera:getCameraInfo	(ILandroid/hardware/Camera$CameraInfo;)V
    //   59: aload_0
    //   60: aload 8
    //   62: iconst_1
    //   63: invokespecial 247	org/telegram/messenger/camera/CameraSession:getDisplayOrientation	(Landroid/hardware/Camera$CameraInfo;Z)I
    //   66: istore_2
    //   67: ldc -7
    //   69: getstatic 166	android/os/Build:MANUFACTURER	Ljava/lang/String;
    //   72: invokevirtual 172	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   75: ifeq +338 -> 413
    //   78: ldc -5
    //   80: getstatic 177	android/os/Build:PRODUCT	Ljava/lang/String;
    //   83: invokevirtual 172	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   86: ifeq +327 -> 413
    //   89: iconst_0
    //   90: istore_1
    //   91: aload_0
    //   92: iload_1
    //   93: putfield 253	org/telegram/messenger/camera/CameraSession:currentOrientation	I
    //   96: aload 7
    //   98: iload_1
    //   99: invokevirtual 257	android/hardware/Camera:setDisplayOrientation	(I)V
    //   102: aload_0
    //   103: aload_0
    //   104: getfield 253	org/telegram/messenger/camera/CameraSession:currentOrientation	I
    //   107: iload_2
    //   108: isub
    //   109: putfield 347	org/telegram/messenger/camera/CameraSession:diffOrientation	I
    //   112: aload 5
    //   114: ifnull +456 -> 570
    //   117: getstatic 352	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   120: ifeq +48 -> 168
    //   123: new 354	java/lang/StringBuilder
    //   126: dup
    //   127: invokespecial 355	java/lang/StringBuilder:<init>	()V
    //   130: ldc_w 357
    //   133: invokevirtual 361	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   136: aload_0
    //   137: getfield 53	org/telegram/messenger/camera/CameraSession:previewSize	Lorg/telegram/messenger/camera/Size;
    //   140: invokevirtual 262	org/telegram/messenger/camera/Size:getWidth	()I
    //   143: invokevirtual 364	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   146: ldc_w 366
    //   149: invokevirtual 361	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   152: aload_0
    //   153: getfield 53	org/telegram/messenger/camera/CameraSession:previewSize	Lorg/telegram/messenger/camera/Size;
    //   156: invokevirtual 265	org/telegram/messenger/camera/Size:getHeight	()I
    //   159: invokevirtual 364	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   162: invokevirtual 370	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   165: invokestatic 373	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   168: aload 5
    //   170: aload_0
    //   171: getfield 53	org/telegram/messenger/camera/CameraSession:previewSize	Lorg/telegram/messenger/camera/Size;
    //   174: invokevirtual 262	org/telegram/messenger/camera/Size:getWidth	()I
    //   177: aload_0
    //   178: getfield 53	org/telegram/messenger/camera/CameraSession:previewSize	Lorg/telegram/messenger/camera/Size;
    //   181: invokevirtual 265	org/telegram/messenger/camera/Size:getHeight	()I
    //   184: invokevirtual 271	android/hardware/Camera$Parameters:setPreviewSize	(II)V
    //   187: getstatic 352	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   190: ifeq +48 -> 238
    //   193: new 354	java/lang/StringBuilder
    //   196: dup
    //   197: invokespecial 355	java/lang/StringBuilder:<init>	()V
    //   200: ldc_w 375
    //   203: invokevirtual 361	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   206: aload_0
    //   207: getfield 55	org/telegram/messenger/camera/CameraSession:pictureSize	Lorg/telegram/messenger/camera/Size;
    //   210: invokevirtual 262	org/telegram/messenger/camera/Size:getWidth	()I
    //   213: invokevirtual 364	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   216: ldc_w 366
    //   219: invokevirtual 361	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   222: aload_0
    //   223: getfield 55	org/telegram/messenger/camera/CameraSession:pictureSize	Lorg/telegram/messenger/camera/Size;
    //   226: invokevirtual 265	org/telegram/messenger/camera/Size:getHeight	()I
    //   229: invokevirtual 364	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   232: invokevirtual 370	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   235: invokestatic 373	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   238: aload 5
    //   240: aload_0
    //   241: getfield 55	org/telegram/messenger/camera/CameraSession:pictureSize	Lorg/telegram/messenger/camera/Size;
    //   244: invokevirtual 262	org/telegram/messenger/camera/Size:getWidth	()I
    //   247: aload_0
    //   248: getfield 55	org/telegram/messenger/camera/CameraSession:pictureSize	Lorg/telegram/messenger/camera/Size;
    //   251: invokevirtual 265	org/telegram/messenger/camera/Size:getHeight	()I
    //   254: invokevirtual 274	android/hardware/Camera$Parameters:setPictureSize	(II)V
    //   257: aload 5
    //   259: aload_0
    //   260: getfield 57	org/telegram/messenger/camera/CameraSession:pictureFormat	I
    //   263: invokevirtual 277	android/hardware/Camera$Parameters:setPictureFormat	(I)V
    //   266: aload 5
    //   268: iconst_1
    //   269: invokevirtual 379	android/hardware/Camera$Parameters:setRecordingHint	(Z)V
    //   272: aload 5
    //   274: invokevirtual 281	android/hardware/Camera$Parameters:getSupportedFocusModes	()Ljava/util/List;
    //   277: ldc_w 381
    //   280: invokeinterface 286 2 0
    //   285: ifeq +11 -> 296
    //   288: aload 5
    //   290: ldc_w 381
    //   293: invokevirtual 289	android/hardware/Camera$Parameters:setFocusMode	(Ljava/lang/String;)V
    //   296: iconst_0
    //   297: istore_1
    //   298: aload_0
    //   299: getfield 116	org/telegram/messenger/camera/CameraSession:jpegOrientation	I
    //   302: iconst_m1
    //   303: if_icmpeq +31 -> 334
    //   306: aload 8
    //   308: getfield 156	android/hardware/Camera$CameraInfo:facing	I
    //   311: iconst_1
    //   312: if_icmpne +204 -> 516
    //   315: aload 8
    //   317: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   320: aload_0
    //   321: getfield 116	org/telegram/messenger/camera/CameraSession:jpegOrientation	I
    //   324: isub
    //   325: sipush 360
    //   328: iadd
    //   329: sipush 360
    //   332: irem
    //   333: istore_1
    //   334: aload 5
    //   336: iload_1
    //   337: invokevirtual 292	android/hardware/Camera$Parameters:setRotation	(I)V
    //   340: aload 8
    //   342: getfield 156	android/hardware/Camera$CameraInfo:facing	I
    //   345: iconst_1
    //   346: if_icmpne +193 -> 539
    //   349: sipush 360
    //   352: iload_2
    //   353: isub
    //   354: sipush 360
    //   357: irem
    //   358: iload_1
    //   359: if_icmpne +175 -> 534
    //   362: aload_0
    //   363: iload_3
    //   364: putfield 294	org/telegram/messenger/camera/CameraSession:sameTakePictureOrientation	Z
    //   367: aload 5
    //   369: ldc 40
    //   371: invokevirtual 297	android/hardware/Camera$Parameters:setFlashMode	(Ljava/lang/String;)V
    //   374: aload 7
    //   376: aload 5
    //   378: invokevirtual 301	android/hardware/Camera:setParameters	(Landroid/hardware/Camera$Parameters;)V
    //   381: aload 5
    //   383: invokevirtual 304	android/hardware/Camera$Parameters:getMaxNumMeteringAreas	()I
    //   386: ifle +184 -> 570
    //   389: aload_0
    //   390: iconst_1
    //   391: putfield 306	org/telegram/messenger/camera/CameraSession:meteringAreaSupported	Z
    //   394: return
    //   395: astore 6
    //   397: aload 6
    //   399: invokestatic 312	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   402: goto -355 -> 47
    //   405: astore 5
    //   407: aload 5
    //   409: invokestatic 312	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   412: return
    //   413: iconst_0
    //   414: istore_1
    //   415: iload_2
    //   416: tableswitch	default:+32->448, 0:+158->574, 1:+163->579, 2:+169->585, 3:+176->592
    //   448: aload 8
    //   450: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   453: bipush 90
    //   455: irem
    //   456: ifeq +9 -> 465
    //   459: aload 8
    //   461: iconst_0
    //   462: putfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   465: aload 8
    //   467: getfield 156	android/hardware/Camera$CameraInfo:facing	I
    //   470: iconst_1
    //   471: if_icmpne +26 -> 497
    //   474: sipush 360
    //   477: aload 8
    //   479: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   482: iload_1
    //   483: iadd
    //   484: sipush 360
    //   487: irem
    //   488: isub
    //   489: sipush 360
    //   492: irem
    //   493: istore_1
    //   494: goto +77 -> 571
    //   497: aload 8
    //   499: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   502: iload_1
    //   503: isub
    //   504: sipush 360
    //   507: iadd
    //   508: sipush 360
    //   511: irem
    //   512: istore_1
    //   513: goto +58 -> 571
    //   516: aload 8
    //   518: getfield 159	android/hardware/Camera$CameraInfo:orientation	I
    //   521: aload_0
    //   522: getfield 116	org/telegram/messenger/camera/CameraSession:jpegOrientation	I
    //   525: iadd
    //   526: sipush 360
    //   529: irem
    //   530: istore_1
    //   531: goto -197 -> 334
    //   534: iconst_0
    //   535: istore_3
    //   536: goto -174 -> 362
    //   539: iload_2
    //   540: iload_1
    //   541: if_icmpne +19 -> 560
    //   544: iload 4
    //   546: istore_3
    //   547: aload_0
    //   548: iload_3
    //   549: putfield 294	org/telegram/messenger/camera/CameraSession:sameTakePictureOrientation	Z
    //   552: goto -185 -> 367
    //   555: astore 6
    //   557: goto -190 -> 367
    //   560: iconst_0
    //   561: istore_3
    //   562: goto -15 -> 547
    //   565: astore 6
    //   567: goto -186 -> 381
    //   570: return
    //   571: goto -480 -> 91
    //   574: iconst_0
    //   575: istore_1
    //   576: goto -128 -> 448
    //   579: bipush 90
    //   581: istore_1
    //   582: goto -134 -> 448
    //   585: sipush 180
    //   588: istore_1
    //   589: goto -141 -> 448
    //   592: sipush 270
    //   595: istore_1
    //   596: goto -148 -> 448
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	599	0	this	CameraSession
    //   90	506	1	i	int
    //   66	476	2	j	int
    //   4	558	3	bool1	boolean
    //   1	544	4	bool2	boolean
    //   34	348	5	localObject	Object
    //   405	3	5	localThrowable	Throwable
    //   41	3	6	localParameters	Camera.Parameters
    //   395	3	6	localException1	Exception
    //   555	1	6	localException2	Exception
    //   565	1	6	localException3	Exception
    //   17	358	7	localCamera	Camera
    //   31	486	8	localCameraInfo	Camera.CameraInfo
    // Exception table:
    //   from	to	target	type
    //   36	43	395	java/lang/Exception
    //   5	19	405	java/lang/Throwable
    //   24	33	405	java/lang/Throwable
    //   36	43	405	java/lang/Throwable
    //   47	89	405	java/lang/Throwable
    //   91	112	405	java/lang/Throwable
    //   117	168	405	java/lang/Throwable
    //   168	238	405	java/lang/Throwable
    //   238	296	405	java/lang/Throwable
    //   298	334	405	java/lang/Throwable
    //   334	349	405	java/lang/Throwable
    //   362	367	405	java/lang/Throwable
    //   367	374	405	java/lang/Throwable
    //   374	381	405	java/lang/Throwable
    //   381	394	405	java/lang/Throwable
    //   397	402	405	java/lang/Throwable
    //   448	465	405	java/lang/Throwable
    //   465	494	405	java/lang/Throwable
    //   497	513	405	java/lang/Throwable
    //   516	531	405	java/lang/Throwable
    //   547	552	405	java/lang/Throwable
    //   334	349	555	java/lang/Exception
    //   362	367	555	java/lang/Exception
    //   547	552	555	java/lang/Exception
    //   374	381	565	java/lang/Exception
  }
  
  public void destroy()
  {
    this.initied = false;
    if (this.orientationEventListener != null)
    {
      this.orientationEventListener.disable();
      this.orientationEventListener = null;
    }
  }
  
  protected void focusToRect(Rect paramRect1, Rect paramRect2)
  {
    try
    {
      Camera localCamera = this.cameraInfo.camera;
      Object localObject1;
      if (localCamera != null)
      {
        localCamera.cancelAutoFocus();
        localObject1 = null;
      }
      try
      {
        localObject2 = localCamera.getParameters();
        localObject1 = localObject2;
      }
      catch (Exception localException)
      {
        for (;;)
        {
          try
          {
            Object localObject2;
            localCamera.setParameters((Camera.Parameters)localObject1);
            localCamera.autoFocus(this.autoFocusCallback);
            return;
          }
          catch (Exception paramRect1)
          {
            FileLog.e(paramRect1);
          }
          localException = localException;
          FileLog.e(localException);
        }
      }
      if (localObject1 != null)
      {
        ((Camera.Parameters)localObject1).setFocusMode("auto");
        localObject2 = new ArrayList();
        ((ArrayList)localObject2).add(new Camera.Area(paramRect1, 1000));
        ((Camera.Parameters)localObject1).setFocusAreas((List)localObject2);
        if (this.meteringAreaSupported)
        {
          paramRect1 = new ArrayList();
          paramRect1.add(new Camera.Area(paramRect2, 1000));
          ((Camera.Parameters)localObject1).setMeteringAreas(paramRect1);
        }
      }
      return;
    }
    catch (Exception paramRect1)
    {
      FileLog.e(paramRect1);
      return;
    }
  }
  
  public String getCurrentFlashMode()
  {
    return this.currentFlashMode;
  }
  
  public int getCurrentOrientation()
  {
    return this.currentOrientation;
  }
  
  public int getDisplayOrientation()
  {
    try
    {
      Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
      Camera.getCameraInfo(this.cameraInfo.getCameraId(), localCameraInfo);
      int i = getDisplayOrientation(localCameraInfo, true);
      return i;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return 0;
  }
  
  public String getNextFlashMode()
  {
    ArrayList localArrayList = CameraController.getInstance().availableFlashModes;
    int i = 0;
    while (i < localArrayList.size())
    {
      if (((String)localArrayList.get(i)).equals(this.currentFlashMode))
      {
        if (i < localArrayList.size() - 1) {
          return (String)localArrayList.get(i + 1);
        }
        return (String)localArrayList.get(0);
      }
      i += 1;
    }
    return this.currentFlashMode;
  }
  
  public int getWorldAngle()
  {
    return this.diffOrientation;
  }
  
  public boolean isInitied()
  {
    return this.initied;
  }
  
  public boolean isSameTakePictureOrientation()
  {
    return this.sameTakePictureOrientation;
  }
  
  public void setCurrentFlashMode(String paramString)
  {
    this.currentFlashMode = paramString;
    configurePhotoCamera();
    SharedPreferences.Editor localEditor = ApplicationLoader.applicationContext.getSharedPreferences("camera", 0).edit();
    if (this.cameraInfo.frontCamera != 0) {}
    for (String str = "flashMode_front";; str = "flashMode")
    {
      localEditor.putString(str, paramString).commit();
      return;
    }
  }
  
  public void setInitied()
  {
    this.initied = true;
  }
  
  protected void stopVideoRecording()
  {
    this.isVideo = false;
    configurePhotoCamera();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/camera/CameraSession.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */