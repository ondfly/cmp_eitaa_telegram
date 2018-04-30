package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Keep;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.PhotoEntry;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.camera.CameraController;
import org.telegram.messenger.camera.CameraInfo;
import org.telegram.messenger.camera.CameraSession;
import org.telegram.messenger.camera.Size;
import org.telegram.messenger.video.MP4Builder;
import org.telegram.messenger.video.Mp4Movie;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;

@TargetApi(18)
public class InstantCameraView
  extends FrameLayout
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final String FRAGMENT_SCREEN_SHADER = "#extension GL_OES_EGL_image_external : require\nprecision lowp float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n   gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n";
  private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nvarying vec2 vTextureCoord;\nuniform float scaleX;\nuniform float scaleY;\nuniform float alpha;\nuniform samplerExternalOES sTexture;\nvoid main() {\n   vec2 coord = vec2((vTextureCoord.x - 0.5) * scaleX, (vTextureCoord.y - 0.5) * scaleY);\n   float coef = ceil(clamp(0.2601 - dot(coord, coord), 0.0, 1.0));\n   vec3 color = texture2D(sTexture, vTextureCoord).rgb * coef + (1.0 - step(0.001, coef));\n   gl_FragColor = vec4(color * alpha, alpha);\n}\n";
  private static final int MSG_AUDIOFRAME_AVAILABLE = 3;
  private static final int MSG_START_RECORDING = 0;
  private static final int MSG_STOP_RECORDING = 1;
  private static final int MSG_VIDEOFRAME_AVAILABLE = 2;
  private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n   gl_Position = uMVPMatrix * aPosition;\n   vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n";
  private AnimatorSet animatorSet;
  private Size aspectRatio;
  private ChatActivity baseFragment;
  private FrameLayout cameraContainer;
  private File cameraFile;
  private volatile boolean cameraReady;
  private CameraSession cameraSession;
  private int[] cameraTexture = new int[1];
  private float cameraTextureAlpha = 1.0F;
  private CameraGLThread cameraThread;
  private boolean cancelled;
  private int currentAccount = UserConfig.selectedAccount;
  private boolean deviceHasGoodCamera;
  private long duration;
  private TLRPC.InputEncryptedFile encryptedFile;
  private TLRPC.InputFile file;
  private boolean isFrontface = true;
  private boolean isSecretChat;
  private byte[] iv;
  private byte[] key;
  private float[] mMVPMatrix;
  private float[] mSTMatrix;
  private float[] moldSTMatrix;
  private AnimatorSet muteAnimation;
  private ImageView muteImageView;
  private int[] oldCameraTexture = new int[1];
  private Paint paint;
  private Size pictureSize;
  private int[] position = new int[2];
  private Size previewSize;
  private float progress;
  private Timer progressTimer;
  private long recordStartTime;
  private long recordedTime;
  private boolean recording;
  private RectF rect;
  private boolean requestingPermissions;
  private float scaleX;
  private float scaleY;
  private CameraInfo selectedCamera;
  private long size;
  private ImageView switchCameraButton;
  private FloatBuffer textureBuffer;
  private TextureView textureView;
  private Runnable timerRunnable = new Runnable()
  {
    public void run()
    {
      if (!InstantCameraView.this.recording) {
        return;
      }
      NotificationCenter.getInstance(InstantCameraView.this.currentAccount).postNotificationName(NotificationCenter.recordProgressChanged, new Object[] { Long.valueOf(InstantCameraView.access$102(InstantCameraView.this, System.currentTimeMillis() - InstantCameraView.this.recordStartTime)), Double.valueOf(0.0D) });
      AndroidUtilities.runOnUIThread(InstantCameraView.this.timerRunnable, 50L);
    }
  };
  private FloatBuffer vertexBuffer;
  private VideoEditedInfo videoEditedInfo;
  private VideoPlayer videoPlayer;
  
  public InstantCameraView(Context paramContext, final ChatActivity paramChatActivity)
  {
    super(paramContext);
    final Object localObject;
    boolean bool;
    if (SharedConfig.roundCamera16to9)
    {
      localObject = new Size(16, 9);
      this.aspectRatio = ((Size)localObject);
      this.mMVPMatrix = new float[16];
      this.mSTMatrix = new float[16];
      this.moldSTMatrix = new float[16];
      setOnTouchListener(new View.OnTouchListener()
      {
        public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
        {
          float f2 = 1.0F;
          boolean bool;
          label116:
          Object localObject;
          label146:
          ImageView localImageView;
          if ((paramAnonymousMotionEvent.getAction() == 0) && (InstantCameraView.this.baseFragment != null))
          {
            if (InstantCameraView.this.videoPlayer == null) {
              break label297;
            }
            if (InstantCameraView.this.videoPlayer.isMuted()) {
              break label274;
            }
            bool = true;
            InstantCameraView.this.videoPlayer.setMute(bool);
            if (InstantCameraView.this.muteAnimation != null) {
              InstantCameraView.this.muteAnimation.cancel();
            }
            InstantCameraView.access$702(InstantCameraView.this, new AnimatorSet());
            paramAnonymousView = InstantCameraView.this.muteAnimation;
            paramAnonymousMotionEvent = InstantCameraView.this.muteImageView;
            if (!bool) {
              break label280;
            }
            f1 = 1.0F;
            paramAnonymousMotionEvent = ObjectAnimator.ofFloat(paramAnonymousMotionEvent, "alpha", new float[] { f1 });
            localObject = InstantCameraView.this.muteImageView;
            if (!bool) {
              break label285;
            }
            f1 = 1.0F;
            localObject = ObjectAnimator.ofFloat(localObject, "scaleX", new float[] { f1 });
            localImageView = InstantCameraView.this.muteImageView;
            if (!bool) {
              break label291;
            }
          }
          label274:
          label280:
          label285:
          label291:
          for (float f1 = f2;; f1 = 0.5F)
          {
            paramAnonymousView.playTogether(new Animator[] { paramAnonymousMotionEvent, localObject, ObjectAnimator.ofFloat(localImageView, "scaleY", new float[] { f1 }) });
            InstantCameraView.this.muteAnimation.addListener(new AnimatorListenerAdapter()
            {
              public void onAnimationEnd(Animator paramAnonymous2Animator)
              {
                if (paramAnonymous2Animator.equals(InstantCameraView.this.muteAnimation)) {
                  InstantCameraView.access$702(InstantCameraView.this, null);
                }
              }
            });
            InstantCameraView.this.muteAnimation.setDuration(180L);
            InstantCameraView.this.muteAnimation.setInterpolator(new DecelerateInterpolator());
            InstantCameraView.this.muteAnimation.start();
            return true;
            bool = false;
            break;
            f1 = 0.0F;
            break label116;
            f1 = 0.5F;
            break label146;
          }
          label297:
          InstantCameraView.this.baseFragment.checkRecordLocked();
          return true;
        }
      });
      setWillNotDraw(false);
      setBackgroundColor(-1073741824);
      this.baseFragment = paramChatActivity;
      if (this.baseFragment.getCurrentEncryptedChat() == null) {
        break label454;
      }
      bool = true;
      label144:
      this.isSecretChat = bool;
      this.paint = new Paint(1)
      {
        public void setAlpha(int paramAnonymousInt)
        {
          super.setAlpha(paramAnonymousInt);
          InstantCameraView.this.invalidate();
        }
      };
      this.paint.setStyle(Paint.Style.STROKE);
      this.paint.setStrokeCap(Paint.Cap.ROUND);
      this.paint.setStrokeWidth(AndroidUtilities.dp(3.0F));
      this.paint.setColor(-1);
      this.rect = new RectF();
      if (Build.VERSION.SDK_INT < 21) {
        break label459;
      }
      this.cameraContainer = new FrameLayout(paramContext)
      {
        public void setAlpha(float paramAnonymousFloat)
        {
          super.setAlpha(paramAnonymousFloat);
          InstantCameraView.this.invalidate();
        }
        
        public void setScaleX(float paramAnonymousFloat)
        {
          super.setScaleX(paramAnonymousFloat);
          InstantCameraView.this.invalidate();
        }
      };
      this.cameraContainer.setOutlineProvider(new ViewOutlineProvider()
      {
        @TargetApi(21)
        public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
        {
          paramAnonymousOutline.setOval(0, 0, AndroidUtilities.roundMessageSize, AndroidUtilities.roundMessageSize);
        }
      });
      this.cameraContainer.setClipToOutline(true);
      this.cameraContainer.setWillNotDraw(false);
    }
    for (;;)
    {
      addView(this.cameraContainer, new FrameLayout.LayoutParams(AndroidUtilities.roundMessageSize, AndroidUtilities.roundMessageSize, 17));
      this.switchCameraButton = new ImageView(paramContext);
      this.switchCameraButton.setScaleType(ImageView.ScaleType.CENTER);
      addView(this.switchCameraButton, LayoutHelper.createFrame(48, 48.0F, 83, 20.0F, 0.0F, 0.0F, 14.0F));
      this.switchCameraButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if ((!InstantCameraView.this.cameraReady) || (InstantCameraView.this.cameraSession == null) || (!InstantCameraView.this.cameraSession.isInitied()) || (InstantCameraView.this.cameraThread == null)) {
            return;
          }
          InstantCameraView.this.switchCamera();
          paramAnonymousView = ObjectAnimator.ofFloat(InstantCameraView.this.switchCameraButton, "scaleX", new float[] { 0.0F }).setDuration(100L);
          paramAnonymousView.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymous2Animator)
            {
              paramAnonymous2Animator = InstantCameraView.this.switchCameraButton;
              if (InstantCameraView.this.isFrontface) {}
              for (int i = 2131165263;; i = 2131165264)
              {
                paramAnonymous2Animator.setImageResource(i);
                ObjectAnimator.ofFloat(InstantCameraView.this.switchCameraButton, "scaleX", new float[] { 1.0F }).setDuration(100L).start();
                return;
              }
            }
          });
          paramAnonymousView.start();
        }
      });
      this.muteImageView = new ImageView(paramContext);
      this.muteImageView.setScaleType(ImageView.ScaleType.CENTER);
      this.muteImageView.setImageResource(2131165698);
      this.muteImageView.setAlpha(0.0F);
      addView(this.muteImageView, LayoutHelper.createFrame(48, 48, 17));
      ((FrameLayout.LayoutParams)this.muteImageView.getLayoutParams()).topMargin = (AndroidUtilities.roundMessageSize / 2 - AndroidUtilities.dp(24.0F));
      setVisibility(4);
      return;
      localObject = new Size(4, 3);
      break;
      label454:
      bool = false;
      break label144;
      label459:
      paramChatActivity = new Path();
      localObject = new Paint(1);
      ((Paint)localObject).setColor(-16777216);
      ((Paint)localObject).setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      this.cameraContainer = new FrameLayout(paramContext)
      {
        protected void dispatchDraw(Canvas paramAnonymousCanvas)
        {
          try
          {
            super.dispatchDraw(paramAnonymousCanvas);
            paramAnonymousCanvas.drawPath(paramChatActivity, localObject);
            return;
          }
          catch (Exception paramAnonymousCanvas) {}
        }
        
        protected void onSizeChanged(int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          super.onSizeChanged(paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
          paramChatActivity.reset();
          paramChatActivity.addCircle(paramAnonymousInt1 / 2, paramAnonymousInt2 / 2, paramAnonymousInt1 / 2, Path.Direction.CW);
          paramChatActivity.toggleInverseFillType();
        }
        
        public void setScaleX(float paramAnonymousFloat)
        {
          super.setScaleX(paramAnonymousFloat);
          InstantCameraView.this.invalidate();
        }
      };
      this.cameraContainer.setWillNotDraw(false);
      this.cameraContainer.setLayerType(2, null);
    }
  }
  
  private void createCamera(final SurfaceTexture paramSurfaceTexture)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (InstantCameraView.this.cameraThread == null) {
          return;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("create camera session");
        }
        paramSurfaceTexture.setDefaultBufferSize(InstantCameraView.this.previewSize.getWidth(), InstantCameraView.this.previewSize.getHeight());
        InstantCameraView.access$1002(InstantCameraView.this, new CameraSession(InstantCameraView.this.selectedCamera, InstantCameraView.this.previewSize, InstantCameraView.this.pictureSize, 256));
        InstantCameraView.this.cameraThread.setCurrentSession(InstantCameraView.this.cameraSession);
        CameraController.getInstance().openRound(InstantCameraView.this.cameraSession, paramSurfaceTexture, new Runnable()new Runnable
        {
          public void run()
          {
            if (InstantCameraView.this.cameraSession != null)
            {
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("camera initied");
              }
              InstantCameraView.this.cameraSession.setInitied();
            }
          }
        }, new Runnable()
        {
          public void run()
          {
            InstantCameraView.this.cameraThread.setCurrentSession(InstantCameraView.this.cameraSession);
          }
        });
      }
    });
  }
  
  private boolean initCamera()
  {
    Object localObject3 = CameraController.getInstance().getCameras();
    if (localObject3 == null) {}
    int i;
    do
    {
      return false;
      localObject1 = null;
      i = 0;
      localObject2 = localObject1;
      if (i < ((ArrayList)localObject3).size())
      {
        localObject2 = (CameraInfo)((ArrayList)localObject3).get(i);
        if (!((CameraInfo)localObject2).isFrontface()) {
          localObject1 = localObject2;
        }
        if (((!this.isFrontface) || (!((CameraInfo)localObject2).isFrontface())) && ((this.isFrontface) || (((CameraInfo)localObject2).isFrontface()))) {
          break;
        }
        this.selectedCamera = ((CameraInfo)localObject2);
        localObject2 = localObject1;
      }
      if (this.selectedCamera == null) {
        this.selectedCamera = ((CameraInfo)localObject2);
      }
    } while (this.selectedCamera == null);
    Object localObject1 = this.selectedCamera.getPreviewSizes();
    Object localObject2 = this.selectedCamera.getPictureSizes();
    this.previewSize = CameraController.chooseOptimalSize((List)localObject1, 480, 270, this.aspectRatio);
    this.pictureSize = CameraController.chooseOptimalSize((List)localObject2, 480, 270, this.aspectRatio);
    int k;
    label199:
    int j;
    int m;
    label225:
    Size localSize;
    if (this.previewSize.mWidth != this.pictureSize.mWidth)
    {
      i = 0;
      k = ((ArrayList)localObject1).size() - 1;
      j = i;
      if (k >= 0)
      {
        localObject3 = (Size)((ArrayList)localObject1).get(k);
        m = ((ArrayList)localObject2).size() - 1;
        j = i;
        if (m >= 0)
        {
          localSize = (Size)((ArrayList)localObject2).get(m);
          if ((((Size)localObject3).mWidth < this.pictureSize.mWidth) || (((Size)localObject3).mHeight < this.pictureSize.mHeight) || (((Size)localObject3).mWidth != localSize.mWidth) || (((Size)localObject3).mHeight != localSize.mHeight)) {
            break label503;
          }
          this.previewSize = ((Size)localObject3);
          this.pictureSize = localSize;
          j = 1;
        }
        if (j == 0) {
          break label512;
        }
      }
      if (j == 0) {
        i = ((ArrayList)localObject1).size() - 1;
      }
    }
    for (;;)
    {
      if (i >= 0)
      {
        localObject3 = (Size)((ArrayList)localObject1).get(i);
        m = ((ArrayList)localObject2).size() - 1;
      }
      for (;;)
      {
        k = j;
        if (m >= 0)
        {
          localSize = (Size)((ArrayList)localObject2).get(m);
          if ((((Size)localObject3).mWidth >= 240) && (((Size)localObject3).mHeight >= 240) && (((Size)localObject3).mWidth == localSize.mWidth) && (((Size)localObject3).mHeight == localSize.mHeight))
          {
            this.previewSize = ((Size)localObject3);
            this.pictureSize = localSize;
            k = 1;
          }
        }
        else
        {
          if (k == 0) {
            break label530;
          }
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("preview w = " + this.previewSize.mWidth + " h = " + this.previewSize.mHeight);
          }
          return true;
          localObject1 = localObject2;
          i += 1;
          break;
          label503:
          m -= 1;
          break label225;
          label512:
          k -= 1;
          i = j;
          break label199;
        }
        m -= 1;
      }
      label530:
      i -= 1;
      j = k;
    }
  }
  
  private int loadShader(int paramInt, String paramString)
  {
    int i = GLES20.glCreateShader(paramInt);
    GLES20.glShaderSource(i, paramString);
    GLES20.glCompileShader(i);
    paramString = new int[1];
    GLES20.glGetShaderiv(i, 35713, paramString, 0);
    paramInt = i;
    if (paramString[0] == 0)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.e(GLES20.glGetShaderInfoLog(i));
      }
      GLES20.glDeleteShader(i);
      paramInt = 0;
    }
    return paramInt;
  }
  
  private void startProgressTimer()
  {
    if (this.progressTimer != null) {}
    try
    {
      this.progressTimer.cancel();
      this.progressTimer = null;
      this.progressTimer = new Timer();
      this.progressTimer.schedule(new TimerTask()
      {
        public void run()
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              long l = 0L;
              try
              {
                if ((InstantCameraView.this.videoPlayer != null) && (InstantCameraView.this.videoEditedInfo != null) && (InstantCameraView.this.videoEditedInfo.endTime > 0L) && (InstantCameraView.this.videoPlayer.getCurrentPosition() >= InstantCameraView.this.videoEditedInfo.endTime))
                {
                  VideoPlayer localVideoPlayer = InstantCameraView.this.videoPlayer;
                  if (InstantCameraView.this.videoEditedInfo.startTime > 0L) {
                    l = InstantCameraView.this.videoEditedInfo.startTime;
                  }
                  localVideoPlayer.seekTo(l);
                }
                return;
              }
              catch (Exception localException)
              {
                FileLog.e(localException);
              }
            }
          });
        }
      }, 0L, 17L);
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
  
  private void stopProgressTimer()
  {
    if (this.progressTimer != null) {}
    try
    {
      this.progressTimer.cancel();
      this.progressTimer = null;
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  private void switchCamera()
  {
    if (this.cameraSession != null)
    {
      this.cameraSession.destroy();
      CameraController.getInstance().close(this.cameraSession, null, null);
      this.cameraSession = null;
    }
    if (!this.isFrontface) {}
    for (boolean bool = true;; bool = false)
    {
      this.isFrontface = bool;
      initCamera();
      this.cameraReady = false;
      this.cameraThread.reinitForNewCamera();
      return;
    }
  }
  
  public void cancel()
  {
    stopProgressTimer();
    if (this.videoPlayer != null)
    {
      this.videoPlayer.releasePlayer();
      this.videoPlayer = null;
    }
    if (this.textureView == null) {
      return;
    }
    this.cancelled = true;
    this.recording = false;
    AndroidUtilities.cancelRunOnUIThread(this.timerRunnable);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.recordStopped, new Object[] { Integer.valueOf(0) });
    if (this.cameraThread != null)
    {
      this.cameraThread.shutdown(0);
      this.cameraThread = null;
    }
    if (this.cameraFile != null)
    {
      this.cameraFile.delete();
      this.cameraFile = null;
    }
    startAnimation(false);
  }
  
  public void changeVideoPreviewState(int paramInt, float paramFloat)
  {
    if (this.videoPlayer == null) {}
    do
    {
      return;
      if (paramInt == 0)
      {
        startProgressTimer();
        this.videoPlayer.play();
        return;
      }
      if (paramInt == 1)
      {
        stopProgressTimer();
        this.videoPlayer.pause();
        return;
      }
    } while (paramInt != 2);
    this.videoPlayer.seekTo(((float)this.videoPlayer.getDuration() * paramFloat));
  }
  
  public void destroy(boolean paramBoolean, Runnable paramRunnable)
  {
    CameraController localCameraController;
    CameraSession localCameraSession;
    if (this.cameraSession != null)
    {
      this.cameraSession.destroy();
      localCameraController = CameraController.getInstance();
      localCameraSession = this.cameraSession;
      if (paramBoolean) {
        break label48;
      }
    }
    label48:
    for (CountDownLatch localCountDownLatch = new CountDownLatch(1);; localCountDownLatch = null)
    {
      localCameraController.close(localCameraSession, localCountDownLatch, paramRunnable);
      return;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.recordProgressChanged)
    {
      long l = ((Long)paramVarArgs[0]).longValue();
      this.progress = ((float)l / 60000.0F);
      this.recordedTime = l;
      invalidate();
    }
    do
    {
      String str;
      do
      {
        do
        {
          return;
        } while (paramInt1 != NotificationCenter.FileDidUpload);
        str = (String)paramVarArgs[0];
      } while ((this.cameraFile == null) || (!this.cameraFile.getAbsolutePath().equals(str)));
      this.file = ((TLRPC.InputFile)paramVarArgs[1]);
      this.encryptedFile = ((TLRPC.InputEncryptedFile)paramVarArgs[2]);
      this.size = ((Long)paramVarArgs[5]).longValue();
    } while (this.encryptedFile == null);
    this.key = ((byte[])paramVarArgs[3]);
    this.iv = ((byte[])paramVarArgs[4]);
  }
  
  public FrameLayout getCameraContainer()
  {
    return this.cameraContainer;
  }
  
  public Rect getCameraRect()
  {
    this.cameraContainer.getLocationOnScreen(this.position);
    return new Rect(this.position[0], this.position[1], this.cameraContainer.getWidth(), this.cameraContainer.getHeight());
  }
  
  public View getMuteImageView()
  {
    return this.muteImageView;
  }
  
  public Paint getPaint()
  {
    return this.paint;
  }
  
  public View getSwitchButtonView()
  {
    return this.switchCameraButton;
  }
  
  public void hideCamera(boolean paramBoolean)
  {
    destroy(paramBoolean, null);
    this.cameraContainer.removeView(this.textureView);
    this.cameraContainer.setTranslationX(0.0F);
    this.cameraContainer.setTranslationY(0.0F);
    this.textureView = null;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordProgressChanged);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileDidUpload);
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordProgressChanged);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileDidUpload);
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    float f1 = this.cameraContainer.getX();
    float f2 = this.cameraContainer.getY();
    this.rect.set(f1 - AndroidUtilities.dp(8.0F), f2 - AndroidUtilities.dp(8.0F), this.cameraContainer.getMeasuredWidth() + f1 + AndroidUtilities.dp(8.0F), this.cameraContainer.getMeasuredHeight() + f2 + AndroidUtilities.dp(8.0F));
    if (this.progress != 0.0F) {
      paramCanvas.drawArc(this.rect, -90.0F, this.progress * 360.0F, false, this.paint);
    }
    if (Theme.chat_roundVideoShadow != null)
    {
      int i = (int)f1 - AndroidUtilities.dp(3.0F);
      int j = (int)f2 - AndroidUtilities.dp(2.0F);
      paramCanvas.save();
      paramCanvas.scale(this.cameraContainer.getScaleX(), this.cameraContainer.getScaleY(), AndroidUtilities.roundMessageSize / 2 + i + AndroidUtilities.dp(3.0F), AndroidUtilities.roundMessageSize / 2 + j + AndroidUtilities.dp(3.0F));
      Theme.chat_roundVideoShadow.setAlpha((int)(this.cameraContainer.getAlpha() * 255.0F));
      Theme.chat_roundVideoShadow.setBounds(i, j, AndroidUtilities.roundMessageSize + i + AndroidUtilities.dp(6.0F), AndroidUtilities.roundMessageSize + j + AndroidUtilities.dp(6.0F));
      Theme.chat_roundVideoShadow.draw(paramCanvas);
      paramCanvas.restore();
    }
  }
  
  public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
  {
    getParent().requestDisallowInterceptTouchEvent(true);
    return super.onInterceptTouchEvent(paramMotionEvent);
  }
  
  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    if (getVisibility() != 0) {
      this.cameraContainer.setTranslationY(getMeasuredHeight() / 2);
    }
  }
  
  public void send(int paramInt)
  {
    if (this.textureView == null) {
      return;
    }
    stopProgressTimer();
    if (this.videoPlayer != null)
    {
      this.videoPlayer.releasePlayer();
      this.videoPlayer = null;
    }
    label117:
    Object localObject;
    if (paramInt == 4)
    {
      long l1;
      long l2;
      if (this.videoEditedInfo.needConvert())
      {
        this.file = null;
        this.encryptedFile = null;
        this.key = null;
        this.iv = null;
        double d = this.videoEditedInfo.estimatedDuration;
        if (this.videoEditedInfo.startTime >= 0L)
        {
          l1 = this.videoEditedInfo.startTime;
          if (this.videoEditedInfo.endTime < 0L) {
            break label326;
          }
          l2 = this.videoEditedInfo.endTime;
          this.videoEditedInfo.estimatedDuration = (l2 - l1);
          this.videoEditedInfo.estimatedSize = ((this.size * (this.videoEditedInfo.estimatedDuration / d)));
          this.videoEditedInfo.bitrate = 400000;
          if (this.videoEditedInfo.startTime > 0L)
          {
            localObject = this.videoEditedInfo;
            ((VideoEditedInfo)localObject).startTime *= 1000L;
          }
          if (this.videoEditedInfo.endTime > 0L)
          {
            localObject = this.videoEditedInfo;
            ((VideoEditedInfo)localObject).endTime *= 1000L;
          }
          FileLoader.getInstance(this.currentAccount).cancelUploadFile(this.cameraFile.getAbsolutePath(), false);
        }
      }
      for (;;)
      {
        this.videoEditedInfo.file = this.file;
        this.videoEditedInfo.encryptedFile = this.encryptedFile;
        this.videoEditedInfo.key = this.key;
        this.videoEditedInfo.iv = this.iv;
        this.baseFragment.sendMedia(new MediaController.PhotoEntry(0, 0, 0L, this.cameraFile.getAbsolutePath(), 0, true), this.videoEditedInfo);
        return;
        l1 = 0L;
        break;
        label326:
        l2 = this.videoEditedInfo.estimatedDuration;
        break label117;
        this.videoEditedInfo.estimatedSize = this.size;
      }
    }
    boolean bool;
    label366:
    int i;
    if (this.recordedTime < 800L)
    {
      bool = true;
      this.cancelled = bool;
      this.recording = false;
      AndroidUtilities.cancelRunOnUIThread(this.timerRunnable);
      if (this.cameraThread != null)
      {
        localObject = NotificationCenter.getInstance(this.currentAccount);
        int j = NotificationCenter.recordStopped;
        if ((this.cancelled) || (paramInt != 3)) {
          break label480;
        }
        i = 2;
        label420:
        ((NotificationCenter)localObject).postNotificationName(j, new Object[] { Integer.valueOf(i) });
        if (!this.cancelled) {
          break label486;
        }
        paramInt = 0;
      }
    }
    for (;;)
    {
      this.cameraThread.shutdown(paramInt);
      this.cameraThread = null;
      if (!this.cancelled) {
        break;
      }
      startAnimation(false);
      return;
      bool = false;
      break label366;
      label480:
      i = 0;
      break label420;
      label486:
      if (paramInt == 3) {
        paramInt = 2;
      } else {
        paramInt = 1;
      }
    }
  }
  
  @Keep
  public void setAlpha(float paramFloat)
  {
    ((ColorDrawable)getBackground()).setAlpha((int)(192.0F * paramFloat));
    invalidate();
  }
  
  public void setVisibility(int paramInt)
  {
    super.setVisibility(paramInt);
    setAlpha(0.0F);
    this.switchCameraButton.setAlpha(0.0F);
    this.cameraContainer.setAlpha(0.0F);
    this.muteImageView.setAlpha(0.0F);
    this.muteImageView.setScaleX(1.0F);
    this.muteImageView.setScaleY(1.0F);
    this.cameraContainer.setScaleX(0.1F);
    this.cameraContainer.setScaleY(0.1F);
    if (this.cameraContainer.getMeasuredWidth() != 0)
    {
      this.cameraContainer.setPivotX(this.cameraContainer.getMeasuredWidth() / 2);
      this.cameraContainer.setPivotY(this.cameraContainer.getMeasuredHeight() / 2);
    }
    if (paramInt == 0) {}
    try
    {
      ((Activity)getContext()).getWindow().addFlags(128);
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    ((Activity)getContext()).getWindow().clearFlags(128);
    return;
  }
  
  public void showCamera()
  {
    if (this.textureView != null) {}
    do
    {
      return;
      this.switchCameraButton.setImageResource(2131165263);
      this.isFrontface = true;
      this.selectedCamera = null;
      this.recordedTime = 0L;
      this.progress = 0.0F;
      this.cancelled = false;
      this.file = null;
      this.encryptedFile = null;
      this.key = null;
      this.iv = null;
    } while (!initCamera());
    MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
    this.cameraFile = new File(FileLoader.getDirectory(4), SharedConfig.getLastLocalId() + ".mp4");
    SharedConfig.saveConfig();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("show round camera");
    }
    this.textureView = new TextureView(getContext());
    this.textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener()
    {
      public void onSurfaceTextureAvailable(SurfaceTexture paramAnonymousSurfaceTexture, int paramAnonymousInt1, int paramAnonymousInt2)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("camera surface available");
        }
        if ((InstantCameraView.this.cameraThread != null) || (paramAnonymousSurfaceTexture == null) || (InstantCameraView.this.cancelled)) {
          return;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("start create thread");
        }
        InstantCameraView.access$1102(InstantCameraView.this, new InstantCameraView.CameraGLThread(InstantCameraView.this, paramAnonymousSurfaceTexture, paramAnonymousInt1, paramAnonymousInt2));
      }
      
      public boolean onSurfaceTextureDestroyed(SurfaceTexture paramAnonymousSurfaceTexture)
      {
        if (InstantCameraView.this.cameraThread != null)
        {
          InstantCameraView.this.cameraThread.shutdown(0);
          InstantCameraView.access$1102(InstantCameraView.this, null);
        }
        if (InstantCameraView.this.cameraSession != null) {
          CameraController.getInstance().close(InstantCameraView.this.cameraSession, null, null);
        }
        return true;
      }
      
      public void onSurfaceTextureSizeChanged(SurfaceTexture paramAnonymousSurfaceTexture, int paramAnonymousInt1, int paramAnonymousInt2) {}
      
      public void onSurfaceTextureUpdated(SurfaceTexture paramAnonymousSurfaceTexture) {}
    });
    this.cameraContainer.addView(this.textureView, LayoutHelper.createFrame(-1, -1.0F));
    setVisibility(0);
    startAnimation(true);
  }
  
  public void startAnimation(boolean paramBoolean)
  {
    float f3 = 1.0F;
    float f2 = 0.0F;
    if (this.animatorSet != null) {
      this.animatorSet.cancel();
    }
    Object localObject1 = PipRoundVideoView.getInstance();
    boolean bool;
    float f1;
    label66:
    ObjectAnimator localObjectAnimator1;
    Object localObject2;
    label94:
    ObjectAnimator localObjectAnimator2;
    Object localObject3;
    int i;
    label145:
    Object localObject4;
    label175:
    Object localObject5;
    label204:
    Object localObject6;
    label234:
    FrameLayout localFrameLayout;
    if (localObject1 != null)
    {
      if (!paramBoolean)
      {
        bool = true;
        ((PipRoundVideoView)localObject1).showTemporary(bool);
      }
    }
    else
    {
      this.animatorSet = new AnimatorSet();
      localObject1 = this.animatorSet;
      if (!paramBoolean) {
        break label400;
      }
      f1 = 1.0F;
      localObjectAnimator1 = ObjectAnimator.ofFloat(this, "alpha", new float[] { f1 });
      localObject2 = this.switchCameraButton;
      if (!paramBoolean) {
        break label405;
      }
      f1 = 1.0F;
      localObject2 = ObjectAnimator.ofFloat(localObject2, "alpha", new float[] { f1 });
      localObjectAnimator2 = ObjectAnimator.ofFloat(this.muteImageView, "alpha", new float[] { 0.0F });
      localObject3 = this.paint;
      if (!paramBoolean) {
        break label410;
      }
      i = 255;
      localObject3 = ObjectAnimator.ofInt(localObject3, "alpha", new int[] { i });
      localObject4 = this.cameraContainer;
      if (!paramBoolean) {
        break label416;
      }
      f1 = 1.0F;
      localObject4 = ObjectAnimator.ofFloat(localObject4, "alpha", new float[] { f1 });
      localObject5 = this.cameraContainer;
      if (!paramBoolean) {
        break label421;
      }
      f1 = 1.0F;
      localObject5 = ObjectAnimator.ofFloat(localObject5, "scaleX", new float[] { f1 });
      localObject6 = this.cameraContainer;
      if (!paramBoolean) {
        break label428;
      }
      f1 = f3;
      localObject6 = ObjectAnimator.ofFloat(localObject6, "scaleY", new float[] { f1 });
      localFrameLayout = this.cameraContainer;
      if (!paramBoolean) {
        break label435;
      }
      f1 = getMeasuredHeight() / 2;
      label269:
      if (!paramBoolean) {
        break label440;
      }
    }
    for (;;)
    {
      ((AnimatorSet)localObject1).playTogether(new Animator[] { localObjectAnimator1, localObject2, localObjectAnimator2, localObject3, localObject4, localObject5, localObject6, ObjectAnimator.ofFloat(localFrameLayout, "translationY", new float[] { f1, f2 }) });
      if (!paramBoolean) {
        this.animatorSet.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            if (paramAnonymousAnimator.equals(InstantCameraView.this.animatorSet))
            {
              InstantCameraView.this.hideCamera(true);
              InstantCameraView.this.setVisibility(4);
            }
          }
        });
      }
      this.animatorSet.setDuration(180L);
      this.animatorSet.setInterpolator(new DecelerateInterpolator());
      this.animatorSet.start();
      return;
      bool = false;
      break;
      label400:
      f1 = 0.0F;
      break label66;
      label405:
      f1 = 0.0F;
      break label94;
      label410:
      i = 0;
      break label145;
      label416:
      f1 = 0.0F;
      break label175;
      label421:
      f1 = 0.1F;
      break label204;
      label428:
      f1 = 0.1F;
      break label234;
      label435:
      f1 = 0.0F;
      break label269;
      label440:
      f2 = getMeasuredHeight() / 2;
    }
  }
  
  private class AudioBufferInfo
  {
    byte[] buffer = new byte['倀'];
    boolean last;
    int lastWroteBuffer;
    long[] offset = new long[10];
    int[] read = new int[10];
    int results;
    
    private AudioBufferInfo() {}
  }
  
  public class CameraGLThread
    extends DispatchQueue
  {
    private final int DO_REINIT_MESSAGE = 2;
    private final int DO_RENDER_MESSAGE = 0;
    private final int DO_SETSESSION_MESSAGE = 3;
    private final int DO_SHUTDOWN_MESSAGE = 1;
    private final int EGL_CONTEXT_CLIENT_VERSION = 12440;
    private final int EGL_OPENGL_ES2_BIT = 4;
    private Integer cameraId = Integer.valueOf(0);
    private SurfaceTexture cameraSurface;
    private CameraSession currentSession;
    private int drawProgram;
    private EGL10 egl10;
    private javax.microedition.khronos.egl.EGLConfig eglConfig;
    private javax.microedition.khronos.egl.EGLContext eglContext;
    private javax.microedition.khronos.egl.EGLDisplay eglDisplay;
    private javax.microedition.khronos.egl.EGLSurface eglSurface;
    private GL gl;
    private boolean initied;
    private int positionHandle;
    private boolean recording;
    private int rotationAngle;
    private SurfaceTexture surfaceTexture;
    private int textureHandle;
    private int textureMatrixHandle;
    private int vertexMatrixHandle;
    private InstantCameraView.VideoRecorder videoEncoder;
    
    public CameraGLThread(SurfaceTexture paramSurfaceTexture, int paramInt1, int paramInt2)
    {
      super();
      this.surfaceTexture = paramSurfaceTexture;
      int j = InstantCameraView.this.previewSize.getWidth();
      int i = InstantCameraView.this.previewSize.getHeight();
      float f = paramInt1 / Math.min(j, i);
      j = (int)(j * f);
      i = (int)(i * f);
      if (j > i)
      {
        InstantCameraView.access$2102(InstantCameraView.this, 1.0F);
        InstantCameraView.access$2202(InstantCameraView.this, j / paramInt2);
        return;
      }
      InstantCameraView.access$2102(InstantCameraView.this, i / paramInt1);
      InstantCameraView.access$2202(InstantCameraView.this, 1.0F);
    }
    
    private boolean initGL()
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("start init gl");
      }
      this.egl10 = ((EGL10)javax.microedition.khronos.egl.EGLContext.getEGL());
      this.eglDisplay = this.egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
      if (this.eglDisplay == EGL10.EGL_NO_DISPLAY)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("eglGetDisplay failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
        }
        finish();
        return false;
      }
      Object localObject1 = new int[2];
      if (!this.egl10.eglInitialize(this.eglDisplay, (int[])localObject1))
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("eglInitialize failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
        }
        finish();
        return false;
      }
      localObject1 = new int[1];
      Object localObject2 = new javax.microedition.khronos.egl.EGLConfig[1];
      if (!this.egl10.eglChooseConfig(this.eglDisplay, new int[] { 12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 0, 12325, 0, 12326, 0, 12344 }, (javax.microedition.khronos.egl.EGLConfig[])localObject2, 1, (int[])localObject1))
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("eglChooseConfig failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
        }
        finish();
        return false;
      }
      if (localObject1[0] > 0)
      {
        this.eglConfig = localObject2[0];
        this.eglContext = this.egl10.eglCreateContext(this.eglDisplay, this.eglConfig, EGL10.EGL_NO_CONTEXT, new int[] { 12440, 2, 12344 });
        if (this.eglContext == null)
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.e("eglCreateContext failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
          }
          finish();
          return false;
        }
      }
      else
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("eglConfig not initialized");
        }
        finish();
        return false;
      }
      if ((this.surfaceTexture instanceof SurfaceTexture))
      {
        this.eglSurface = this.egl10.eglCreateWindowSurface(this.eglDisplay, this.eglConfig, this.surfaceTexture, null);
        if ((this.eglSurface == null) || (this.eglSurface == EGL10.EGL_NO_SURFACE))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.e("createWindowSurface failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
          }
          finish();
          return false;
        }
      }
      else
      {
        finish();
        return false;
      }
      if (!this.egl10.eglMakeCurrent(this.eglDisplay, this.eglSurface, this.eglSurface, this.eglContext))
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("eglMakeCurrent failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
        }
        finish();
        return false;
      }
      this.gl = this.eglContext.getGL();
      float f1 = 1.0F / InstantCameraView.this.scaleX / 2.0F;
      float f2 = 1.0F / InstantCameraView.this.scaleY / 2.0F;
      localObject1 = new float[12];
      Object tmp676_674 = localObject1;
      tmp676_674[0] = -1.0F;
      Object tmp681_676 = tmp676_674;
      tmp681_676[1] = -1.0F;
      Object tmp686_681 = tmp681_676;
      tmp686_681[2] = 0.0F;
      Object tmp690_686 = tmp686_681;
      tmp690_686[3] = 1.0F;
      Object tmp694_690 = tmp690_686;
      tmp694_690[4] = -1.0F;
      Object tmp699_694 = tmp694_690;
      tmp699_694[5] = 0.0F;
      Object tmp703_699 = tmp699_694;
      tmp703_699[6] = -1.0F;
      Object tmp709_703 = tmp703_699;
      tmp709_703[7] = 1.0F;
      Object tmp714_709 = tmp709_703;
      tmp714_709[8] = 0.0F;
      Object tmp719_714 = tmp714_709;
      tmp719_714[9] = 1.0F;
      Object tmp724_719 = tmp719_714;
      tmp724_719[10] = 1.0F;
      Object tmp729_724 = tmp724_719;
      tmp729_724[11] = 0.0F;
      tmp729_724;
      localObject2 = new float[8];
      localObject2[0] = (0.5F - f1);
      localObject2[1] = (0.5F - f2);
      localObject2[2] = (0.5F + f1);
      localObject2[3] = (0.5F - f2);
      localObject2[4] = (0.5F - f1);
      localObject2[5] = (0.5F + f2);
      localObject2[6] = (0.5F + f1);
      localObject2[7] = (0.5F + f2);
      this.videoEncoder = new InstantCameraView.VideoRecorder(InstantCameraView.this, null);
      InstantCameraView.access$2402(InstantCameraView.this, ByteBuffer.allocateDirect(localObject1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer());
      InstantCameraView.this.vertexBuffer.put((float[])localObject1).position(0);
      InstantCameraView.access$2502(InstantCameraView.this, ByteBuffer.allocateDirect(localObject2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer());
      InstantCameraView.this.textureBuffer.put((float[])localObject2).position(0);
      Matrix.setIdentityM(InstantCameraView.this.mSTMatrix, 0);
      int i = InstantCameraView.this.loadShader(35633, "uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n   gl_Position = uMVPMatrix * aPosition;\n   vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n");
      int j = InstantCameraView.this.loadShader(35632, "#extension GL_OES_EGL_image_external : require\nprecision lowp float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n   gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n");
      if ((i != 0) && (j != 0))
      {
        this.drawProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.drawProgram, i);
        GLES20.glAttachShader(this.drawProgram, j);
        GLES20.glLinkProgram(this.drawProgram);
        localObject1 = new int[1];
        GLES20.glGetProgramiv(this.drawProgram, 35714, (int[])localObject1, 0);
        if (localObject1[0] == 0)
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.e("failed link shader");
          }
          GLES20.glDeleteProgram(this.drawProgram);
          this.drawProgram = 0;
        }
        for (;;)
        {
          GLES20.glGenTextures(1, InstantCameraView.this.cameraTexture, 0);
          GLES20.glBindTexture(36197, InstantCameraView.this.cameraTexture[0]);
          GLES20.glTexParameteri(36197, 10241, 9729);
          GLES20.glTexParameteri(36197, 10240, 9729);
          GLES20.glTexParameteri(36197, 10242, 33071);
          GLES20.glTexParameteri(36197, 10243, 33071);
          Matrix.setIdentityM(InstantCameraView.this.mMVPMatrix, 0);
          this.cameraSurface = new SurfaceTexture(InstantCameraView.this.cameraTexture[0]);
          this.cameraSurface.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener()
          {
            public void onFrameAvailable(SurfaceTexture paramAnonymousSurfaceTexture)
            {
              InstantCameraView.CameraGLThread.this.requestRender();
            }
          });
          InstantCameraView.this.createCamera(this.cameraSurface);
          if (BuildVars.LOGS_ENABLED) {
            FileLog.e("gl initied");
          }
          return true;
          this.positionHandle = GLES20.glGetAttribLocation(this.drawProgram, "aPosition");
          this.textureHandle = GLES20.glGetAttribLocation(this.drawProgram, "aTextureCoord");
          this.vertexMatrixHandle = GLES20.glGetUniformLocation(this.drawProgram, "uMVPMatrix");
          this.textureMatrixHandle = GLES20.glGetUniformLocation(this.drawProgram, "uSTMatrix");
        }
      }
      if (BuildVars.LOGS_ENABLED) {
        FileLog.e("failed creating shader");
      }
      finish();
      return false;
    }
    
    private void onDraw(Integer paramInteger)
    {
      if (!this.initied) {}
      do
      {
        return;
        if (((this.eglContext.equals(this.egl10.eglGetCurrentContext())) && (this.eglSurface.equals(this.egl10.eglGetCurrentSurface(12377)))) || (this.egl10.eglMakeCurrent(this.eglDisplay, this.eglSurface, this.eglSurface, this.eglContext))) {
          break;
        }
      } while (!BuildVars.LOGS_ENABLED);
      FileLog.e("eglMakeCurrent failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
      return;
      this.cameraSurface.updateTexImage();
      if (!this.recording)
      {
        this.videoEncoder.startRecording(InstantCameraView.this.cameraFile, EGL14.eglGetCurrentContext());
        this.recording = true;
        int i = this.currentSession.getCurrentOrientation();
        if ((i == 90) || (i == 270))
        {
          float f = InstantCameraView.this.scaleX;
          InstantCameraView.access$2102(InstantCameraView.this, InstantCameraView.this.scaleY);
          InstantCameraView.access$2202(InstantCameraView.this, f);
        }
      }
      this.videoEncoder.frameAvailable(this.cameraSurface, paramInteger, System.nanoTime());
      this.cameraSurface.getTransformMatrix(InstantCameraView.this.mSTMatrix);
      GLES20.glUseProgram(this.drawProgram);
      GLES20.glActiveTexture(33984);
      GLES20.glBindTexture(36197, InstantCameraView.this.cameraTexture[0]);
      GLES20.glVertexAttribPointer(this.positionHandle, 3, 5126, false, 12, InstantCameraView.this.vertexBuffer);
      GLES20.glEnableVertexAttribArray(this.positionHandle);
      GLES20.glVertexAttribPointer(this.textureHandle, 2, 5126, false, 8, InstantCameraView.this.textureBuffer);
      GLES20.glEnableVertexAttribArray(this.textureHandle);
      GLES20.glUniformMatrix4fv(this.textureMatrixHandle, 1, false, InstantCameraView.this.mSTMatrix, 0);
      GLES20.glUniformMatrix4fv(this.vertexMatrixHandle, 1, false, InstantCameraView.this.mMVPMatrix, 0);
      GLES20.glDrawArrays(5, 0, 4);
      GLES20.glDisableVertexAttribArray(this.positionHandle);
      GLES20.glDisableVertexAttribArray(this.textureHandle);
      GLES20.glBindTexture(36197, 0);
      GLES20.glUseProgram(0);
      this.egl10.eglSwapBuffers(this.eglDisplay, this.eglSurface);
    }
    
    public void finish()
    {
      if (this.eglSurface != null)
      {
        this.egl10.eglMakeCurrent(this.eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        this.egl10.eglDestroySurface(this.eglDisplay, this.eglSurface);
        this.eglSurface = null;
      }
      if (this.eglContext != null)
      {
        this.egl10.eglDestroyContext(this.eglDisplay, this.eglContext);
        this.eglContext = null;
      }
      if (this.eglDisplay != null)
      {
        this.egl10.eglTerminate(this.eglDisplay);
        this.eglDisplay = null;
      }
    }
    
    public void handleMessage(Message paramMessage)
    {
      switch (paramMessage.what)
      {
      }
      do
      {
        do
        {
          do
          {
            return;
            onDraw((Integer)paramMessage.obj);
            return;
            finish();
            if (this.recording) {
              this.videoEncoder.stopRecording(paramMessage.arg1);
            }
            paramMessage = Looper.myLooper();
          } while (paramMessage == null);
          paramMessage.quit();
          return;
          if (this.egl10.eglMakeCurrent(this.eglDisplay, this.eglSurface, this.eglSurface, this.eglContext)) {
            break;
          }
        } while (!BuildVars.LOGS_ENABLED);
        FileLog.d("eglMakeCurrent failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
        return;
        if (this.cameraSurface != null)
        {
          this.cameraSurface.getTransformMatrix(InstantCameraView.this.moldSTMatrix);
          this.cameraSurface.setOnFrameAvailableListener(null);
          this.cameraSurface.release();
          InstantCameraView.this.oldCameraTexture[0] = InstantCameraView.this.cameraTexture[0];
          InstantCameraView.access$3402(InstantCameraView.this, 0.0F);
          InstantCameraView.this.cameraTexture[0] = 0;
        }
        paramMessage = this.cameraId;
        this.cameraId = Integer.valueOf(this.cameraId.intValue() + 1);
        InstantCameraView.access$902(InstantCameraView.this, false);
        GLES20.glGenTextures(1, InstantCameraView.this.cameraTexture, 0);
        GLES20.glBindTexture(36197, InstantCameraView.this.cameraTexture[0]);
        GLES20.glTexParameteri(36197, 10241, 9729);
        GLES20.glTexParameteri(36197, 10240, 9729);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        this.cameraSurface = new SurfaceTexture(InstantCameraView.this.cameraTexture[0]);
        this.cameraSurface.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener()
        {
          public void onFrameAvailable(SurfaceTexture paramAnonymousSurfaceTexture)
          {
            InstantCameraView.CameraGLThread.this.requestRender();
          }
        });
        InstantCameraView.this.createCamera(this.cameraSurface);
        return;
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("set gl rednderer session");
        }
        paramMessage = (CameraSession)paramMessage.obj;
        if (this.currentSession != paramMessage) {
          break;
        }
        this.rotationAngle = this.currentSession.getWorldAngle();
        Matrix.setIdentityM(InstantCameraView.this.mMVPMatrix, 0);
      } while (this.rotationAngle == 0);
      Matrix.rotateM(InstantCameraView.this.mMVPMatrix, 0, this.rotationAngle, 0.0F, 0.0F, 1.0F);
      return;
      this.currentSession = paramMessage;
    }
    
    public void reinitForNewCamera()
    {
      Handler localHandler = InstantCameraView.this.getHandler();
      if (localHandler != null) {
        sendMessage(localHandler.obtainMessage(2), 0);
      }
    }
    
    public void requestRender()
    {
      Handler localHandler = InstantCameraView.this.getHandler();
      if (localHandler != null) {
        sendMessage(localHandler.obtainMessage(0, this.cameraId), 0);
      }
    }
    
    public void run()
    {
      this.initied = initGL();
      super.run();
    }
    
    public void setCurrentSession(CameraSession paramCameraSession)
    {
      Handler localHandler = InstantCameraView.this.getHandler();
      if (localHandler != null) {
        sendMessage(localHandler.obtainMessage(3, paramCameraSession), 0);
      }
    }
    
    public void shutdown(int paramInt)
    {
      Handler localHandler = InstantCameraView.this.getHandler();
      if (localHandler != null) {
        sendMessage(localHandler.obtainMessage(1, paramInt, 0), 0);
      }
    }
  }
  
  private static class EncoderHandler
    extends Handler
  {
    private WeakReference<InstantCameraView.VideoRecorder> mWeakEncoder;
    
    public EncoderHandler(InstantCameraView.VideoRecorder paramVideoRecorder)
    {
      this.mWeakEncoder = new WeakReference(paramVideoRecorder);
    }
    
    public void exit()
    {
      Looper.myLooper().quit();
    }
    
    public void handleMessage(Message paramMessage)
    {
      int i = paramMessage.what;
      Object localObject = paramMessage.obj;
      localObject = (InstantCameraView.VideoRecorder)this.mWeakEncoder.get();
      if (localObject == null) {
        return;
      }
      switch (i)
      {
      default: 
        return;
      case 0: 
        try
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.e("start encoder");
          }
          InstantCameraView.VideoRecorder.access$3500((InstantCameraView.VideoRecorder)localObject);
          return;
        }
        catch (Exception paramMessage)
        {
          FileLog.e(paramMessage);
          InstantCameraView.VideoRecorder.access$3600((InstantCameraView.VideoRecorder)localObject, 0);
          Looper.myLooper().quit();
          return;
        }
      case 1: 
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("stop encoder");
        }
        InstantCameraView.VideoRecorder.access$3600((InstantCameraView.VideoRecorder)localObject, paramMessage.arg1);
        return;
      case 2: 
        InstantCameraView.VideoRecorder.access$3700((InstantCameraView.VideoRecorder)localObject, paramMessage.arg1 << 32 | paramMessage.arg2 & 0xFFFFFFFF, (Integer)paramMessage.obj);
        return;
      }
      InstantCameraView.VideoRecorder.access$3800((InstantCameraView.VideoRecorder)localObject, (InstantCameraView.AudioBufferInfo)paramMessage.obj);
    }
  }
  
  private class VideoRecorder
    implements Runnable
  {
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int FRAME_RATE = 30;
    private static final int IFRAME_INTERVAL = 1;
    private static final String VIDEO_MIME_TYPE = "video/avc";
    private int alphaHandle;
    private MediaCodec.BufferInfo audioBufferInfo;
    private MediaCodec audioEncoder;
    private long audioFirst = -1L;
    private AudioRecord audioRecorder;
    private long audioStartTime = -1L;
    private boolean audioStopedByTime;
    private int audioTrackIndex = -5;
    private boolean blendEnabled;
    private ArrayBlockingQueue<InstantCameraView.AudioBufferInfo> buffers = new ArrayBlockingQueue(10);
    private ArrayList<InstantCameraView.AudioBufferInfo> buffersToWrite = new ArrayList();
    private long currentTimestamp = 0L;
    private long desyncTime;
    private int drawProgram;
    private android.opengl.EGLConfig eglConfig;
    private android.opengl.EGLContext eglContext = EGL14.EGL_NO_CONTEXT;
    private android.opengl.EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;
    private android.opengl.EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private volatile InstantCameraView.EncoderHandler handler;
    private Integer lastCameraId = Integer.valueOf(0);
    private long lastCommitedFrameTime;
    private long lastTimestamp = -1L;
    private MP4Builder mediaMuxer;
    private int positionHandle;
    private boolean ready;
    private Runnable recorderRunnable = new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: ldc2_w 25
        //   3: lstore 4
        //   5: iconst_0
        //   6: istore_1
        //   7: iload_1
        //   8: ifne +53 -> 61
        //   11: iload_1
        //   12: istore_2
        //   13: aload_0
        //   14: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   17: invokestatic 30	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$3900	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Z
        //   20: ifne +89 -> 109
        //   23: iload_1
        //   24: istore_2
        //   25: aload_0
        //   26: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   29: invokestatic 34	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4000	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Landroid/media/AudioRecord;
        //   32: invokevirtual 40	android/media/AudioRecord:getRecordingState	()I
        //   35: iconst_1
        //   36: if_icmpeq +73 -> 109
        //   39: aload_0
        //   40: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   43: invokestatic 34	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4000	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Landroid/media/AudioRecord;
        //   46: invokevirtual 43	android/media/AudioRecord:stop	()V
        //   49: iload_1
        //   50: istore_2
        //   51: aload_0
        //   52: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   55: invokestatic 47	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4100	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)I
        //   58: ifne +51 -> 109
        //   61: aload_0
        //   62: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   65: invokestatic 34	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4000	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Landroid/media/AudioRecord;
        //   68: invokevirtual 50	android/media/AudioRecord:release	()V
        //   71: aload_0
        //   72: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   75: invokestatic 54	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4400	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Lorg/telegram/ui/Components/InstantCameraView$EncoderHandler;
        //   78: aload_0
        //   79: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   82: invokestatic 54	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4400	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Lorg/telegram/ui/Components/InstantCameraView$EncoderHandler;
        //   85: iconst_1
        //   86: aload_0
        //   87: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   90: invokestatic 47	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4100	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)I
        //   93: iconst_0
        //   94: invokevirtual 60	org/telegram/ui/Components/InstantCameraView$EncoderHandler:obtainMessage	(III)Landroid/os/Message;
        //   97: invokevirtual 64	org/telegram/ui/Components/InstantCameraView$EncoderHandler:sendMessage	(Landroid/os/Message;)Z
        //   100: pop
        //   101: return
        //   102: astore 10
        //   104: iconst_1
        //   105: istore_1
        //   106: goto -57 -> 49
        //   109: aload_0
        //   110: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   113: invokestatic 68	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4200	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Ljava/util/concurrent/ArrayBlockingQueue;
        //   116: invokevirtual 74	java/util/concurrent/ArrayBlockingQueue:isEmpty	()Z
        //   119: ifeq +198 -> 317
        //   122: new 76	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo
        //   125: dup
        //   126: aload_0
        //   127: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   130: getfield 80	org/telegram/ui/Components/InstantCameraView$VideoRecorder:this$0	Lorg/telegram/ui/Components/InstantCameraView;
        //   133: aconst_null
        //   134: invokespecial 83	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:<init>	(Lorg/telegram/ui/Components/InstantCameraView;Lorg/telegram/ui/Components/InstantCameraView$1;)V
        //   137: astore 10
        //   139: aload 10
        //   141: iconst_0
        //   142: putfield 87	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:lastWroteBuffer	I
        //   145: aload 10
        //   147: bipush 10
        //   149: putfield 90	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:results	I
        //   152: iconst_0
        //   153: istore_1
        //   154: lload 4
        //   156: lstore 8
        //   158: iload_1
        //   159: bipush 10
        //   161: if_icmpge +83 -> 244
        //   164: lload 4
        //   166: lstore 6
        //   168: lload 4
        //   170: ldc2_w 25
        //   173: lcmp
        //   174: ifne +12 -> 186
        //   177: invokestatic 96	java/lang/System:nanoTime	()J
        //   180: ldc2_w 97
        //   183: ldiv
        //   184: lstore 6
        //   186: aload_0
        //   187: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   190: invokestatic 34	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4000	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Landroid/media/AudioRecord;
        //   193: aload 10
        //   195: getfield 102	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:buffer	[B
        //   198: iload_1
        //   199: sipush 2048
        //   202: imul
        //   203: sipush 2048
        //   206: invokevirtual 106	android/media/AudioRecord:read	([BII)I
        //   209: istore_3
        //   210: iload_3
        //   211: ifgt +124 -> 335
        //   214: aload 10
        //   216: iload_1
        //   217: putfield 90	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:results	I
        //   220: lload 6
        //   222: lstore 8
        //   224: aload_0
        //   225: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   228: invokestatic 30	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$3900	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Z
        //   231: ifne +13 -> 244
        //   234: aload 10
        //   236: iconst_1
        //   237: putfield 110	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:last	Z
        //   240: lload 6
        //   242: lstore 8
        //   244: aload 10
        //   246: getfield 90	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:results	I
        //   249: ifge +11 -> 260
        //   252: aload 10
        //   254: getfield 110	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:last	Z
        //   257: ifeq +117 -> 374
        //   260: iload_2
        //   261: istore_1
        //   262: aload_0
        //   263: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   266: invokestatic 30	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$3900	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Z
        //   269: ifne +17 -> 286
        //   272: iload_2
        //   273: istore_1
        //   274: aload 10
        //   276: getfield 90	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:results	I
        //   279: bipush 10
        //   281: if_icmpge +5 -> 286
        //   284: iconst_1
        //   285: istore_1
        //   286: aload_0
        //   287: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   290: invokestatic 54	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4400	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Lorg/telegram/ui/Components/InstantCameraView$EncoderHandler;
        //   293: aload_0
        //   294: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   297: invokestatic 54	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4400	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Lorg/telegram/ui/Components/InstantCameraView$EncoderHandler;
        //   300: iconst_3
        //   301: aload 10
        //   303: invokevirtual 113	org/telegram/ui/Components/InstantCameraView$EncoderHandler:obtainMessage	(ILjava/lang/Object;)Landroid/os/Message;
        //   306: invokevirtual 64	org/telegram/ui/Components/InstantCameraView$EncoderHandler:sendMessage	(Landroid/os/Message;)Z
        //   309: pop
        //   310: lload 8
        //   312: lstore 4
        //   314: goto -307 -> 7
        //   317: aload_0
        //   318: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   321: invokestatic 68	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4200	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Ljava/util/concurrent/ArrayBlockingQueue;
        //   324: invokevirtual 117	java/util/concurrent/ArrayBlockingQueue:poll	()Ljava/lang/Object;
        //   327: checkcast 76	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo
        //   330: astore 10
        //   332: goto -193 -> 139
        //   335: aload 10
        //   337: getfield 121	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:offset	[J
        //   340: iload_1
        //   341: lload 6
        //   343: lastore
        //   344: aload 10
        //   346: getfield 124	org/telegram/ui/Components/InstantCameraView$AudioBufferInfo:read	[I
        //   349: iload_1
        //   350: iload_3
        //   351: iastore
        //   352: lload 6
        //   354: ldc 125
        //   356: iload_3
        //   357: imul
        //   358: ldc 126
        //   360: idiv
        //   361: iconst_2
        //   362: idiv
        //   363: i2l
        //   364: ladd
        //   365: lstore 4
        //   367: iload_1
        //   368: iconst_1
        //   369: iadd
        //   370: istore_1
        //   371: goto -217 -> 154
        //   374: aload_0
        //   375: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   378: invokestatic 30	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$3900	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Z
        //   381: ifne +12 -> 393
        //   384: iconst_1
        //   385: istore_1
        //   386: lload 8
        //   388: lstore 4
        //   390: goto -383 -> 7
        //   393: aload_0
        //   394: getfield 17	org/telegram/ui/Components/InstantCameraView$VideoRecorder$1:this$1	Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;
        //   397: invokestatic 68	org/telegram/ui/Components/InstantCameraView$VideoRecorder:access$4200	(Lorg/telegram/ui/Components/InstantCameraView$VideoRecorder;)Ljava/util/concurrent/ArrayBlockingQueue;
        //   400: aload 10
        //   402: invokevirtual 130	java/util/concurrent/ArrayBlockingQueue:put	(Ljava/lang/Object;)V
        //   405: lload 8
        //   407: lstore 4
        //   409: iload_2
        //   410: istore_1
        //   411: goto -404 -> 7
        //   414: astore 10
        //   416: lload 8
        //   418: lstore 4
        //   420: iload_2
        //   421: istore_1
        //   422: goto -415 -> 7
        //   425: astore 10
        //   427: aload 10
        //   429: invokestatic 136	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   432: goto -361 -> 71
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	435	0	this	1
        //   6	416	1	i	int
        //   12	409	2	j	int
        //   209	149	3	k	int
        //   3	416	4	l1	long
        //   166	187	6	l2	long
        //   156	261	8	l3	long
        //   102	1	10	localException1	Exception
        //   137	264	10	localAudioBufferInfo	InstantCameraView.AudioBufferInfo
        //   414	1	10	localException2	Exception
        //   425	3	10	localException3	Exception
        // Exception table:
        //   from	to	target	type
        //   39	49	102	java/lang/Exception
        //   393	405	414	java/lang/Exception
        //   61	71	425	java/lang/Exception
      }
    };
    private volatile boolean running;
    private int scaleXHandle;
    private int scaleYHandle;
    private volatile int sendWhenDone;
    private android.opengl.EGLContext sharedEglContext;
    private boolean skippedFirst;
    private long skippedTime;
    private Surface surface;
    private final Object sync = new Object();
    private int textureHandle;
    private int textureMatrixHandle;
    private int vertexMatrixHandle;
    private int videoBitrate;
    private MediaCodec.BufferInfo videoBufferInfo;
    private boolean videoConvertFirstWrite = true;
    private MediaCodec videoEncoder;
    private File videoFile;
    private long videoFirst = -1L;
    private int videoHeight;
    private long videoLast;
    private int videoTrackIndex = -5;
    private int videoWidth;
    private int zeroTimeStamps;
    
    private VideoRecorder() {}
    
    private void didWriteData(File paramFile, boolean paramBoolean)
    {
      long l1 = 0L;
      if (this.videoConvertFirstWrite)
      {
        FileLoader.getInstance(InstantCameraView.this.currentAccount).uploadFile(paramFile.toString(), InstantCameraView.this.isSecretChat, false, 1, 33554432);
        this.videoConvertFirstWrite = false;
        if (paramBoolean)
        {
          localFileLoader = FileLoader.getInstance(InstantCameraView.this.currentAccount);
          str = paramFile.toString();
          bool = InstantCameraView.this.isSecretChat;
          l2 = paramFile.length();
          if (paramBoolean) {
            l1 = paramFile.length();
          }
          localFileLoader.checkUploadNewDataAvailable(str, bool, l2, l1);
        }
        return;
      }
      FileLoader localFileLoader = FileLoader.getInstance(InstantCameraView.this.currentAccount);
      String str = paramFile.toString();
      boolean bool = InstantCameraView.this.isSecretChat;
      long l2 = paramFile.length();
      if (paramBoolean) {
        l1 = paramFile.length();
      }
      localFileLoader.checkUploadNewDataAvailable(str, bool, l2, l1);
    }
    
    private void handleAudioFrameAvailable(InstantCameraView.AudioBufferInfo paramAudioBufferInfo)
    {
      if (this.audioStopedByTime) {}
      for (;;)
      {
        return;
        this.buffersToWrite.add(paramAudioBufferInfo);
        Object localObject = paramAudioBufferInfo;
        label55:
        int k;
        int j;
        int i;
        if (this.audioFirst == -1L)
        {
          if (this.videoFirst == -1L)
          {
            if (!BuildVars.LOGS_ENABLED) {
              continue;
            }
            FileLog.d("video record not yet started");
            return;
          }
          k = 0;
          j = 0;
          for (;;)
          {
            i = k;
            if (j < paramAudioBufferInfo.results)
            {
              if ((j != 0) || (Math.abs(this.videoFirst - paramAudioBufferInfo.offset[j]) <= 100000000L)) {
                break label232;
              }
              this.desyncTime = (this.videoFirst - paramAudioBufferInfo.offset[j]);
              this.audioFirst = paramAudioBufferInfo.offset[j];
              j = 1;
              i = j;
              if (BuildVars.LOGS_ENABLED)
              {
                FileLog.d("detected desync between audio and video " + this.desyncTime);
                i = j;
              }
            }
            for (;;)
            {
              localObject = paramAudioBufferInfo;
              if (i != 0) {
                break label368;
              }
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("first audio frame not found, removing buffers " + paramAudioBufferInfo.results);
              }
              this.buffersToWrite.remove(paramAudioBufferInfo);
              if (this.buffersToWrite.isEmpty()) {
                break;
              }
              paramAudioBufferInfo = (InstantCameraView.AudioBufferInfo)this.buffersToWrite.get(0);
              break label55;
              label232:
              if (paramAudioBufferInfo.offset[j] < this.videoFirst) {
                break label317;
              }
              paramAudioBufferInfo.lastWroteBuffer = j;
              this.audioFirst = paramAudioBufferInfo.offset[j];
              k = 1;
              i = k;
              if (BuildVars.LOGS_ENABLED)
              {
                FileLog.d("found first audio frame at " + j + " timestamp = " + paramAudioBufferInfo.offset[j]);
                i = k;
              }
            }
            label317:
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("ignore first audio frame at " + j + " timestamp = " + paramAudioBufferInfo.offset[j]);
            }
            j += 1;
          }
        }
        label368:
        if (this.audioStartTime == -1L) {
          this.audioStartTime = localObject.offset[localObject.lastWroteBuffer];
        }
        paramAudioBufferInfo = (InstantCameraView.AudioBufferInfo)localObject;
        if (this.buffersToWrite.size() > 1) {
          paramAudioBufferInfo = (InstantCameraView.AudioBufferInfo)this.buffersToWrite.get(0);
        }
        try
        {
          drainEncoder(false);
          for (bool2 = false; paramAudioBufferInfo != null; bool2 = bool1)
          {
            try
            {
              label428:
              j = this.audioEncoder.dequeueInputBuffer(0L);
              if (j < 0) {
                break label428;
              }
              if (Build.VERSION.SDK_INT < 21) {
                break label660;
              }
              localByteBuffer = this.audioEncoder.getInputBuffer(j);
              l1 = paramAudioBufferInfo.offset[paramAudioBufferInfo.lastWroteBuffer];
              i = paramAudioBufferInfo.lastWroteBuffer;
              localObject = paramAudioBufferInfo;
              bool1 = bool2;
              paramAudioBufferInfo = (InstantCameraView.AudioBufferInfo)localObject;
              if (i <= ((InstantCameraView.AudioBufferInfo)localObject).results)
              {
                if (i >= ((InstantCameraView.AudioBufferInfo)localObject).results) {
                  break label733;
                }
                if ((this.running) || (localObject.offset[i] < this.videoLast - this.desyncTime)) {
                  break label680;
                }
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.d("stop audio encoding because of stoped video recording at " + localObject.offset[i] + " last video " + this.videoLast);
                }
                this.audioStopedByTime = true;
                bool1 = true;
                paramAudioBufferInfo = null;
                this.buffersToWrite.clear();
              }
              localObject = this.audioEncoder;
              k = localByteBuffer.position();
              if (l1 != 0L) {
                break label810;
              }
              l1 = 0L;
            }
            catch (Throwable paramAudioBufferInfo)
            {
              FileLog.e(paramAudioBufferInfo);
              return;
            }
            ((MediaCodec)localObject).queueInputBuffer(j, 0, k, l1, i);
          }
        }
        catch (Exception localException)
        {
          for (;;)
          {
            boolean bool2;
            long l1;
            boolean bool1;
            FileLog.e(localException);
            continue;
            label660:
            ByteBuffer localByteBuffer = this.audioEncoder.getInputBuffers()[j];
            localByteBuffer.clear();
            continue;
            label680:
            if (localByteBuffer.remaining() < localException.read[i])
            {
              localException.lastWroteBuffer = i;
              paramAudioBufferInfo = null;
              bool1 = bool2;
            }
            else
            {
              localByteBuffer.put(localException.buffer, i * 2048, localException.read[i]);
              label733:
              paramAudioBufferInfo = localException;
              if (i >= localException.results - 1)
              {
                this.buffersToWrite.remove(localException);
                if (this.running) {
                  this.buffers.put(localException);
                }
                if (!this.buffersToWrite.isEmpty())
                {
                  paramAudioBufferInfo = (InstantCameraView.AudioBufferInfo)this.buffersToWrite.get(0);
                }
                else
                {
                  bool1 = localException.last;
                  paramAudioBufferInfo = null;
                  continue;
                  label810:
                  long l2 = this.audioStartTime;
                  l1 -= l2;
                  while (!bool1)
                  {
                    i = 0;
                    break;
                  }
                  i = 4;
                  continue;
                }
              }
              i += 1;
              InstantCameraView.AudioBufferInfo localAudioBufferInfo = paramAudioBufferInfo;
            }
          }
        }
      }
    }
    
    private void handleStopRecording(final int paramInt)
    {
      if (this.running)
      {
        this.sendWhenDone = paramInt;
        this.running = false;
        return;
      }
      try
      {
        drainEncoder(true);
        if (this.videoEncoder == null) {}
      }
      catch (Exception localException3)
      {
        try
        {
          this.videoEncoder.stop();
          this.videoEncoder.release();
          this.videoEncoder = null;
          if (this.audioEncoder == null) {}
        }
        catch (Exception localException3)
        {
          try
          {
            this.audioEncoder.stop();
            this.audioEncoder.release();
            this.audioEncoder = null;
            if (this.mediaMuxer == null) {}
          }
          catch (Exception localException3)
          {
            try
            {
              for (;;)
              {
                this.mediaMuxer.finishMovie();
                if (paramInt == 0) {
                  break;
                }
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    InstantCameraView.access$2002(InstantCameraView.this, new VideoEditedInfo());
                    InstantCameraView.this.videoEditedInfo.roundVideo = true;
                    InstantCameraView.this.videoEditedInfo.startTime = -1L;
                    InstantCameraView.this.videoEditedInfo.endTime = -1L;
                    InstantCameraView.this.videoEditedInfo.file = InstantCameraView.this.file;
                    InstantCameraView.this.videoEditedInfo.encryptedFile = InstantCameraView.this.encryptedFile;
                    InstantCameraView.this.videoEditedInfo.key = InstantCameraView.this.key;
                    InstantCameraView.this.videoEditedInfo.iv = InstantCameraView.this.iv;
                    InstantCameraView.this.videoEditedInfo.estimatedSize = InstantCameraView.this.size;
                    InstantCameraView.this.videoEditedInfo.framerate = 25;
                    Object localObject = InstantCameraView.this.videoEditedInfo;
                    InstantCameraView.this.videoEditedInfo.originalWidth = 240;
                    ((VideoEditedInfo)localObject).resultWidth = 240;
                    localObject = InstantCameraView.this.videoEditedInfo;
                    InstantCameraView.this.videoEditedInfo.originalHeight = 240;
                    ((VideoEditedInfo)localObject).resultHeight = 240;
                    InstantCameraView.this.videoEditedInfo.originalPath = InstantCameraView.VideoRecorder.this.videoFile.getAbsolutePath();
                    if (paramInt == 1) {
                      InstantCameraView.this.baseFragment.sendMedia(new MediaController.PhotoEntry(0, 0, 0L, InstantCameraView.VideoRecorder.this.videoFile.getAbsolutePath(), 0, true), InstantCameraView.this.videoEditedInfo);
                    }
                    for (;;)
                    {
                      InstantCameraView.VideoRecorder.this.didWriteData(InstantCameraView.VideoRecorder.this.videoFile, true);
                      return;
                      InstantCameraView.access$602(InstantCameraView.this, new VideoPlayer());
                      InstantCameraView.this.videoPlayer.setDelegate(new VideoPlayer.VideoPlayerDelegate()
                      {
                        public void onError(Exception paramAnonymous2Exception)
                        {
                          FileLog.e(paramAnonymous2Exception);
                        }
                        
                        public void onRenderedFirstFrame() {}
                        
                        public void onStateChanged(boolean paramAnonymous2Boolean, int paramAnonymous2Int)
                        {
                          long l = 0L;
                          if (InstantCameraView.this.videoPlayer == null) {}
                          while ((!InstantCameraView.this.videoPlayer.isPlaying()) || (paramAnonymous2Int != 4)) {
                            return;
                          }
                          VideoPlayer localVideoPlayer = InstantCameraView.this.videoPlayer;
                          if (InstantCameraView.this.videoEditedInfo.startTime > 0L) {
                            l = InstantCameraView.this.videoEditedInfo.startTime;
                          }
                          localVideoPlayer.seekTo(l);
                        }
                        
                        public boolean onSurfaceDestroyed(SurfaceTexture paramAnonymous2SurfaceTexture)
                        {
                          return false;
                        }
                        
                        public void onSurfaceTextureUpdated(SurfaceTexture paramAnonymous2SurfaceTexture) {}
                        
                        public void onVideoSizeChanged(int paramAnonymous2Int1, int paramAnonymous2Int2, int paramAnonymous2Int3, float paramAnonymous2Float) {}
                      });
                      InstantCameraView.this.videoPlayer.setTextureView(InstantCameraView.this.textureView);
                      InstantCameraView.this.videoPlayer.preparePlayer(Uri.fromFile(InstantCameraView.VideoRecorder.this.videoFile), "other");
                      InstantCameraView.this.videoPlayer.play();
                      InstantCameraView.this.videoPlayer.setMute(true);
                      InstantCameraView.this.startProgressTimer();
                      localObject = new AnimatorSet();
                      ((AnimatorSet)localObject).playTogether(new Animator[] { ObjectAnimator.ofFloat(InstantCameraView.this.switchCameraButton, "alpha", new float[] { 0.0F }), ObjectAnimator.ofInt(InstantCameraView.this.paint, "alpha", new int[] { 0 }), ObjectAnimator.ofFloat(InstantCameraView.this.muteImageView, "alpha", new float[] { 1.0F }) });
                      ((AnimatorSet)localObject).setDuration(180L);
                      ((AnimatorSet)localObject).setInterpolator(new DecelerateInterpolator());
                      ((AnimatorSet)localObject).start();
                      InstantCameraView.this.videoEditedInfo.estimatedDuration = InstantCameraView.this.duration;
                      NotificationCenter.getInstance(InstantCameraView.this.currentAccount).postNotificationName(NotificationCenter.audioDidSent, new Object[] { InstantCameraView.this.videoEditedInfo, InstantCameraView.VideoRecorder.this.videoFile.getAbsolutePath() });
                    }
                  }
                });
                EGL14.eglDestroySurface(this.eglDisplay, this.eglSurface);
                this.eglSurface = EGL14.EGL_NO_SURFACE;
                if (this.surface != null)
                {
                  this.surface.release();
                  this.surface = null;
                }
                if (this.eglDisplay != EGL14.EGL_NO_DISPLAY)
                {
                  EGL14.eglMakeCurrent(this.eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                  EGL14.eglDestroyContext(this.eglDisplay, this.eglContext);
                  EGL14.eglReleaseThread();
                  EGL14.eglTerminate(this.eglDisplay);
                }
                this.eglDisplay = EGL14.EGL_NO_DISPLAY;
                this.eglContext = EGL14.EGL_NO_CONTEXT;
                this.eglConfig = null;
                this.handler.exit();
                return;
                localException1 = localException1;
                FileLog.e(localException1);
                continue;
                localException2 = localException2;
                FileLog.e(localException2);
              }
              localException3 = localException3;
              FileLog.e(localException3);
            }
            catch (Exception localException4)
            {
              for (;;)
              {
                FileLog.e(localException4);
                continue;
                FileLoader.getInstance(InstantCameraView.this.currentAccount).cancelUploadFile(this.videoFile.getAbsolutePath(), false);
                this.videoFile.delete();
              }
            }
          }
        }
      }
    }
    
    private void handleVideoFrameAvailable(long paramLong, Integer paramInteger)
    {
      try
      {
        drainEncoder(false);
        if (!this.lastCameraId.equals(paramInteger))
        {
          this.lastTimestamp = -1L;
          this.lastCameraId = paramInteger;
        }
        if (this.lastTimestamp == -1L)
        {
          this.lastTimestamp = paramLong;
          if (this.currentTimestamp != 0L)
          {
            l2 = (System.currentTimeMillis() - this.lastCommitedFrameTime) * 1000000L;
            l1 = 0L;
            this.lastCommitedFrameTime = System.currentTimeMillis();
            if (this.skippedFirst) {
              break label151;
            }
            this.skippedTime += l2;
            if (this.skippedTime >= 200000000L) {
              break label146;
            }
            return;
          }
        }
      }
      catch (Exception localException)
      {
        label146:
        label151:
        do
        {
          do
          {
            do
            {
              long l2;
              long l1;
              for (;;)
              {
                FileLog.e(localException);
                continue;
                l2 = 0L;
                l1 = 0L;
                continue;
                l2 = paramLong - this.lastTimestamp;
                l1 = l2;
                this.lastTimestamp = paramLong;
              }
              this.skippedFirst = true;
              this.currentTimestamp += l2;
              if (this.videoFirst == -1L)
              {
                this.videoFirst = (paramLong / 1000L);
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.d("first video frame was at " + this.videoFirst);
                }
              }
              this.videoLast = paramLong;
              GLES20.glUseProgram(this.drawProgram);
              GLES20.glVertexAttribPointer(this.positionHandle, 3, 5126, false, 12, InstantCameraView.this.vertexBuffer);
              GLES20.glEnableVertexAttribArray(this.positionHandle);
              GLES20.glVertexAttribPointer(this.textureHandle, 2, 5126, false, 8, InstantCameraView.this.textureBuffer);
              GLES20.glEnableVertexAttribArray(this.textureHandle);
              GLES20.glUniform1f(this.scaleXHandle, InstantCameraView.this.scaleX);
              GLES20.glUniform1f(this.scaleYHandle, InstantCameraView.this.scaleY);
              GLES20.glUniformMatrix4fv(this.vertexMatrixHandle, 1, false, InstantCameraView.this.mMVPMatrix, 0);
              GLES20.glActiveTexture(33984);
              if (InstantCameraView.this.oldCameraTexture[0] != 0)
              {
                if (!this.blendEnabled)
                {
                  GLES20.glEnable(3042);
                  this.blendEnabled = true;
                }
                GLES20.glUniformMatrix4fv(this.textureMatrixHandle, 1, false, InstantCameraView.this.moldSTMatrix, 0);
                GLES20.glUniform1f(this.alphaHandle, 1.0F);
                GLES20.glBindTexture(36197, InstantCameraView.this.oldCameraTexture[0]);
                GLES20.glDrawArrays(5, 0, 4);
              }
              GLES20.glUniformMatrix4fv(this.textureMatrixHandle, 1, false, InstantCameraView.this.mSTMatrix, 0);
              GLES20.glUniform1f(this.alphaHandle, InstantCameraView.this.cameraTextureAlpha);
              GLES20.glBindTexture(36197, InstantCameraView.this.cameraTexture[0]);
              GLES20.glDrawArrays(5, 0, 4);
              GLES20.glDisableVertexAttribArray(this.positionHandle);
              GLES20.glDisableVertexAttribArray(this.textureHandle);
              GLES20.glBindTexture(36197, 0);
              GLES20.glUseProgram(0);
              EGLExt.eglPresentationTimeANDROID(this.eglDisplay, this.eglSurface, this.currentTimestamp);
              EGL14.eglSwapBuffers(this.eglDisplay, this.eglSurface);
              if ((InstantCameraView.this.oldCameraTexture[0] == 0) || (InstantCameraView.this.cameraTextureAlpha >= 1.0F)) {
                break;
              }
              InstantCameraView.access$3402(InstantCameraView.this, InstantCameraView.this.cameraTextureAlpha + (float)l1 / 2.0E8F);
            } while (InstantCameraView.this.cameraTextureAlpha <= 1.0F);
            GLES20.glDisable(3042);
            this.blendEnabled = false;
            InstantCameraView.access$3402(InstantCameraView.this, 1.0F);
            GLES20.glDeleteTextures(1, InstantCameraView.this.oldCameraTexture, 0);
            InstantCameraView.this.oldCameraTexture[0] = 0;
          } while (InstantCameraView.this.cameraReady);
          InstantCameraView.access$902(InstantCameraView.this, true);
          return;
        } while (InstantCameraView.this.cameraReady);
        InstantCameraView.access$902(InstantCameraView.this, true);
      }
    }
    
    private void prepareEncoder()
    {
      for (;;)
      {
        int i;
        int j;
        try
        {
          i = AudioRecord.getMinBufferSize(44100, 16, 2);
          j = i;
          if (i <= 0) {
            j = 3584;
          }
          i = 49152;
          if (49152 >= j) {
            break label1097;
          }
          i = (j / 2048 + 1) * 2048 * 2;
        }
        catch (Exception localException)
        {
          Object localObject1;
          throw new RuntimeException(localException);
        }
        if (j < 3)
        {
          this.buffers.add(new InstantCameraView.AudioBufferInfo(InstantCameraView.this, null));
          j += 1;
        }
        else
        {
          this.audioRecorder = new AudioRecord(1, 44100, 16, 2, i);
          this.audioRecorder.startRecording();
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("initied audio record with channels " + this.audioRecorder.getChannelCount() + " sample rate = " + this.audioRecorder.getSampleRate() + " bufferSize = " + i);
          }
          localObject1 = new Thread(this.recorderRunnable);
          ((Thread)localObject1).setPriority(10);
          ((Thread)localObject1).start();
          this.audioBufferInfo = new MediaCodec.BufferInfo();
          this.videoBufferInfo = new MediaCodec.BufferInfo();
          localObject1 = new MediaFormat();
          ((MediaFormat)localObject1).setString("mime", "audio/mp4a-latm");
          ((MediaFormat)localObject1).setInteger("aac-profile", 2);
          ((MediaFormat)localObject1).setInteger("sample-rate", 44100);
          ((MediaFormat)localObject1).setInteger("channel-count", 1);
          ((MediaFormat)localObject1).setInteger("bitrate", 32000);
          ((MediaFormat)localObject1).setInteger("max-input-size", 20480);
          this.audioEncoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
          this.audioEncoder.configure((MediaFormat)localObject1, null, null, 1);
          this.audioEncoder.start();
          this.videoEncoder = MediaCodec.createEncoderByType("video/avc");
          localObject1 = MediaFormat.createVideoFormat("video/avc", this.videoWidth, this.videoHeight);
          ((MediaFormat)localObject1).setInteger("color-format", 2130708361);
          ((MediaFormat)localObject1).setInteger("bitrate", this.videoBitrate);
          ((MediaFormat)localObject1).setInteger("frame-rate", 30);
          ((MediaFormat)localObject1).setInteger("i-frame-interval", 1);
          this.videoEncoder.configure((MediaFormat)localObject1, null, null, 1);
          this.surface = this.videoEncoder.createInputSurface();
          this.videoEncoder.start();
          localObject1 = new Mp4Movie();
          ((Mp4Movie)localObject1).setCacheFile(this.videoFile);
          ((Mp4Movie)localObject1).setRotation(0);
          ((Mp4Movie)localObject1).setSize(this.videoWidth, this.videoHeight);
          this.mediaMuxer = new MP4Builder().createMovie((Mp4Movie)localObject1, InstantCameraView.this.isSecretChat);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (InstantCameraView.this.cancelled) {
                return;
              }
              try
              {
                InstantCameraView.this.performHapticFeedback(3, 2);
                AndroidUtilities.lockOrientation(InstantCameraView.this.baseFragment.getParentActivity());
                InstantCameraView.access$002(InstantCameraView.this, true);
                InstantCameraView.access$202(InstantCameraView.this, System.currentTimeMillis());
                AndroidUtilities.runOnUIThread(InstantCameraView.this.timerRunnable);
                NotificationCenter.getInstance(InstantCameraView.this.currentAccount).postNotificationName(NotificationCenter.recordStarted, new Object[0]);
                return;
              }
              catch (Exception localException)
              {
                for (;;) {}
              }
            }
          });
          if (this.eglDisplay != EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("EGL already set up");
          }
          this.eglDisplay = EGL14.eglGetDisplay(0);
          if (this.eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
          }
          Object localObject2 = new int[2];
          if (!EGL14.eglInitialize(this.eglDisplay, (int[])localObject2, 0, (int[])localObject2, 1))
          {
            this.eglDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
          }
          if (this.eglContext == EGL14.EGL_NO_CONTEXT)
          {
            localObject2 = new android.opengl.EGLConfig[1];
            int[] arrayOfInt = new int[1];
            android.opengl.EGLDisplay localEGLDisplay = this.eglDisplay;
            i = localObject2.length;
            if (!EGL14.eglChooseConfig(localEGLDisplay, new int[] { 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12352, 4, 12610, 1, 12344 }, 0, (android.opengl.EGLConfig[])localObject2, 0, i, arrayOfInt, 0)) {
              throw new RuntimeException("Unable to find a suitable EGLConfig");
            }
            this.eglContext = EGL14.eglCreateContext(this.eglDisplay, localObject2[0], this.sharedEglContext, new int[] { 12440, 2, 12344 }, 0);
            this.eglConfig = localObject2[0];
          }
          localObject2 = new int[1];
          EGL14.eglQueryContext(this.eglDisplay, this.eglContext, 12440, (int[])localObject2, 0);
          if (this.eglSurface != EGL14.EGL_NO_SURFACE) {
            throw new IllegalStateException("surface already created");
          }
          this.eglSurface = EGL14.eglCreateWindowSurface(this.eglDisplay, this.eglConfig, this.surface, new int[] { 12344 }, 0);
          if (this.eglSurface == null) {
            throw new RuntimeException("surface was null");
          }
          if (!EGL14.eglMakeCurrent(this.eglDisplay, this.eglSurface, this.eglSurface, this.eglContext))
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.e("eglMakeCurrent failed " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }
            throw new RuntimeException("eglMakeCurrent failed");
          }
          GLES20.glBlendFunc(770, 771);
          i = InstantCameraView.this.loadShader(35633, "uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n   gl_Position = uMVPMatrix * aPosition;\n   vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n");
          j = InstantCameraView.this.loadShader(35632, "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nvarying vec2 vTextureCoord;\nuniform float scaleX;\nuniform float scaleY;\nuniform float alpha;\nuniform samplerExternalOES sTexture;\nvoid main() {\n   vec2 coord = vec2((vTextureCoord.x - 0.5) * scaleX, (vTextureCoord.y - 0.5) * scaleY);\n   float coef = ceil(clamp(0.2601 - dot(coord, coord), 0.0, 1.0));\n   vec3 color = texture2D(sTexture, vTextureCoord).rgb * coef + (1.0 - step(0.001, coef));\n   gl_FragColor = vec4(color * alpha, alpha);\n}\n");
          if ((i != 0) && (j != 0))
          {
            this.drawProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(this.drawProgram, i);
            GLES20.glAttachShader(this.drawProgram, j);
            GLES20.glLinkProgram(this.drawProgram);
            localObject2 = new int[1];
            GLES20.glGetProgramiv(this.drawProgram, 35714, (int[])localObject2, 0);
            if (localObject2[0] == 0)
            {
              GLES20.glDeleteProgram(this.drawProgram);
              this.drawProgram = 0;
            }
          }
          else
          {
            return;
          }
          this.positionHandle = GLES20.glGetAttribLocation(this.drawProgram, "aPosition");
          this.textureHandle = GLES20.glGetAttribLocation(this.drawProgram, "aTextureCoord");
          this.scaleXHandle = GLES20.glGetUniformLocation(this.drawProgram, "scaleX");
          this.scaleYHandle = GLES20.glGetUniformLocation(this.drawProgram, "scaleY");
          this.alphaHandle = GLES20.glGetUniformLocation(this.drawProgram, "alpha");
          this.vertexMatrixHandle = GLES20.glGetUniformLocation(this.drawProgram, "uMVPMatrix");
          this.textureMatrixHandle = GLES20.glGetUniformLocation(this.drawProgram, "uSTMatrix");
          return;
          label1097:
          j = 0;
        }
      }
    }
    
    public void drainEncoder(boolean paramBoolean)
      throws Exception
    {
      if (paramBoolean) {
        this.videoEncoder.signalEndOfInputStream();
      }
      ByteBuffer[] arrayOfByteBuffer = null;
      if (Build.VERSION.SDK_INT < 21) {
        arrayOfByteBuffer = this.videoEncoder.getOutputBuffers();
      }
      int j;
      do
      {
        j = this.videoEncoder.dequeueOutputBuffer(this.videoBufferInfo, 10000L);
        if (j != -1) {
          break;
        }
      } while (paramBoolean);
      label55:
      if (Build.VERSION.SDK_INT < 21) {
        arrayOfByteBuffer = this.audioEncoder.getOutputBuffers();
      }
      label323:
      label589:
      label663:
      do
      {
        int i;
        do
        {
          for (;;)
          {
            i = this.audioEncoder.dequeueOutputBuffer(this.audioBufferInfo, 0L);
            if (i == -1)
            {
              if ((paramBoolean) && ((this.running) || (this.sendWhenDone != 0))) {
                continue;
              }
              return;
              if (j == -3)
              {
                if (Build.VERSION.SDK_INT >= 21) {
                  break;
                }
                arrayOfByteBuffer = this.videoEncoder.getOutputBuffers();
                break;
              }
              if (j == -2)
              {
                localObject1 = this.videoEncoder.getOutputFormat();
                if (this.videoTrackIndex != -5) {
                  break;
                }
                this.videoTrackIndex = this.mediaMuxer.addTrack((MediaFormat)localObject1, false);
                break;
              }
              if (j < 0) {
                break;
              }
              if (Build.VERSION.SDK_INT < 21) {}
              for (localObject1 = arrayOfByteBuffer[j]; localObject1 == null; localObject1 = this.videoEncoder.getOutputBuffer(j)) {
                throw new RuntimeException("encoderOutputBuffer " + j + " was null");
              }
              if (this.videoBufferInfo.size > 1)
              {
                if ((this.videoBufferInfo.flags & 0x2) != 0) {
                  break label323;
                }
                if (this.mediaMuxer.writeSampleData(this.videoTrackIndex, (ByteBuffer)localObject1, this.videoBufferInfo, true)) {
                  didWriteData(this.videoFile, false);
                }
              }
              while (this.videoTrackIndex != -5)
              {
                this.videoEncoder.releaseOutputBuffer(j, false);
                if ((this.videoBufferInfo.flags & 0x4) == 0) {
                  break;
                }
                break label55;
              }
              byte[] arrayOfByte = new byte[this.videoBufferInfo.size];
              ((ByteBuffer)localObject1).limit(this.videoBufferInfo.offset + this.videoBufferInfo.size);
              ((ByteBuffer)localObject1).position(this.videoBufferInfo.offset);
              ((ByteBuffer)localObject1).get(arrayOfByte);
              MediaFormat localMediaFormat = null;
              Object localObject3 = null;
              i = this.videoBufferInfo.size - 1;
              for (;;)
              {
                Object localObject2 = localObject3;
                localObject1 = localMediaFormat;
                if (i >= 0)
                {
                  localObject2 = localObject3;
                  localObject1 = localMediaFormat;
                  if (i > 3)
                  {
                    if ((arrayOfByte[i] != 1) || (arrayOfByte[(i - 1)] != 0) || (arrayOfByte[(i - 2)] != 0) || (arrayOfByte[(i - 3)] != 0)) {
                      break label589;
                    }
                    localObject1 = ByteBuffer.allocate(i - 3);
                    localObject2 = ByteBuffer.allocate(this.videoBufferInfo.size - (i - 3));
                    ((ByteBuffer)localObject1).put(arrayOfByte, 0, i - 3).position(0);
                    ((ByteBuffer)localObject2).put(arrayOfByte, i - 3, this.videoBufferInfo.size - (i - 3)).position(0);
                  }
                }
                localMediaFormat = MediaFormat.createVideoFormat("video/avc", this.videoWidth, this.videoHeight);
                if ((localObject1 != null) && (localObject2 != null))
                {
                  localMediaFormat.setByteBuffer("csd-0", (ByteBuffer)localObject1);
                  localMediaFormat.setByteBuffer("csd-1", (ByteBuffer)localObject2);
                }
                this.videoTrackIndex = this.mediaMuxer.addTrack(localMediaFormat, false);
                break;
                i -= 1;
              }
            }
            if (i == -3)
            {
              if (Build.VERSION.SDK_INT < 21) {
                arrayOfByteBuffer = this.audioEncoder.getOutputBuffers();
              }
            }
            else
            {
              if (i != -2) {
                break label663;
              }
              localObject1 = this.audioEncoder.getOutputFormat();
              if (this.audioTrackIndex == -5) {
                this.audioTrackIndex = this.mediaMuxer.addTrack((MediaFormat)localObject1, true);
              }
            }
          }
        } while (i < 0);
        if (Build.VERSION.SDK_INT < 21) {}
        for (Object localObject1 = arrayOfByteBuffer[i]; localObject1 == null; localObject1 = this.audioEncoder.getOutputBuffer(i)) {
          throw new RuntimeException("encoderOutputBuffer " + i + " was null");
        }
        if ((this.audioBufferInfo.flags & 0x2) != 0) {
          this.audioBufferInfo.size = 0;
        }
        if ((this.audioBufferInfo.size != 0) && (this.mediaMuxer.writeSampleData(this.audioTrackIndex, (ByteBuffer)localObject1, this.audioBufferInfo, false))) {
          didWriteData(this.videoFile, false);
        }
        this.audioEncoder.releaseOutputBuffer(i, false);
      } while ((this.audioBufferInfo.flags & 0x4) == 0);
    }
    
    protected void finalize()
      throws Throwable
    {
      try
      {
        if (this.eglDisplay != EGL14.EGL_NO_DISPLAY)
        {
          EGL14.eglMakeCurrent(this.eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
          EGL14.eglDestroyContext(this.eglDisplay, this.eglContext);
          EGL14.eglReleaseThread();
          EGL14.eglTerminate(this.eglDisplay);
          this.eglDisplay = EGL14.EGL_NO_DISPLAY;
          this.eglContext = EGL14.EGL_NO_CONTEXT;
          this.eglConfig = null;
        }
        return;
      }
      finally
      {
        super.finalize();
      }
    }
    
    public void frameAvailable(SurfaceTexture paramSurfaceTexture, Integer paramInteger, long paramLong)
    {
      for (;;)
      {
        long l;
        synchronized (this.sync)
        {
          if (!this.ready) {
            return;
          }
          l = paramSurfaceTexture.getTimestamp();
          if (l == 0L)
          {
            this.zeroTimeStamps += 1;
            if (this.zeroTimeStamps <= 1) {
              break;
            }
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("fix timestamp enabled");
            }
            this.handler.sendMessage(this.handler.obtainMessage(2, (int)(paramLong >> 32), (int)paramLong, paramInteger));
            return;
          }
        }
        this.zeroTimeStamps = 0;
        paramLong = l;
      }
    }
    
    public Surface getInputSurface()
    {
      return this.surface;
    }
    
    public void run()
    {
      
      synchronized (this.sync)
      {
        this.handler = new InstantCameraView.EncoderHandler(this);
        this.ready = true;
        this.sync.notify();
        Looper.loop();
      }
      synchronized (this.sync)
      {
        this.ready = false;
        return;
        localObject2 = finally;
        throw ((Throwable)localObject2);
      }
    }
    
    public void startRecording(File arg1, android.opengl.EGLContext paramEGLContext)
    {
      String str2 = Build.DEVICE;
      String str1 = str2;
      if (str2 == null) {
        str1 = "";
      }
      int j;
      if ((str1.startsWith("zeroflte")) || (str1.startsWith("zenlte"))) {
        j = 320;
      }
      for (int i = 600000;; i = 400000)
      {
        this.videoFile = ???;
        this.videoWidth = j;
        this.videoHeight = j;
        this.videoBitrate = i;
        this.sharedEglContext = paramEGLContext;
        synchronized (this.sync)
        {
          if (this.running) {
            return;
          }
          this.running = true;
          paramEGLContext = new Thread(this, "TextureMovieEncoder");
          paramEGLContext.setPriority(10);
          paramEGLContext.start();
          for (;;)
          {
            boolean bool = this.ready;
            if (bool) {
              break;
            }
            try
            {
              this.sync.wait();
            }
            catch (InterruptedException paramEGLContext) {}
          }
          this.handler.sendMessage(this.handler.obtainMessage(0));
          return;
        }
        j = 240;
      }
    }
    
    public void stopRecording(int paramInt)
    {
      this.handler.sendMessage(this.handler.obtainMessage(1, paramInt, 0));
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/InstantCameraView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */