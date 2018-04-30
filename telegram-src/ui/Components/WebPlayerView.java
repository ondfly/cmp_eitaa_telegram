package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Bitmaps;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.exoplayer2.ui.AspectRatioFrameLayout;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;

public class WebPlayerView
  extends ViewGroup
  implements AudioManager.OnAudioFocusChangeListener, VideoPlayer.VideoPlayerDelegate
{
  private static final int AUDIO_FOCUSED = 2;
  private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
  private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
  private static final Pattern aparatFileListPattern;
  private static final Pattern aparatIdRegex;
  private static final Pattern coubIdRegex;
  private static final String exprName = "[a-zA-Z_$][a-zA-Z_$0-9]*";
  private static final Pattern exprParensPattern = Pattern.compile("[()]");
  private static final Pattern jsPattern;
  private static int lastContainerId = 4001;
  private static final Pattern playerIdPattern = Pattern.compile(".*?-([a-zA-Z0-9_-]+)(?:/watch_as3|/html5player(?:-new)?|(?:/[a-z]{2}_[A-Z]{2})?/base)?\\.([a-z]+)$");
  private static final Pattern sigPattern;
  private static final Pattern sigPattern2;
  private static final Pattern stmtReturnPattern;
  private static final Pattern stmtVarPattern;
  private static final Pattern stsPattern;
  private static final Pattern twitchClipFilePattern;
  private static final Pattern twitchClipIdRegex;
  private static final Pattern twitchStreamIdRegex;
  private static final Pattern vimeoIdRegex;
  private static final Pattern youtubeIdRegex = Pattern.compile("(?:youtube(?:-nocookie)?\\.com/(?:[^/\\n\\s]+/\\S+/|(?:v|e(?:mbed)?)/|\\S*?[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})");
  private boolean allowInlineAnimation;
  private AspectRatioFrameLayout aspectRatioFrameLayout;
  private int audioFocus;
  private Paint backgroundPaint;
  private TextureView changedTextureView;
  private boolean changingTextureView;
  private ControlsView controlsView;
  private float currentAlpha;
  private Bitmap currentBitmap;
  private AsyncTask currentTask;
  private String currentYoutubeId;
  private WebPlayerViewDelegate delegate;
  private boolean drawImage;
  private boolean firstFrameRendered;
  private int fragment_container_id;
  private ImageView fullscreenButton;
  private boolean hasAudioFocus;
  private boolean inFullscreen;
  private boolean initFailed;
  private boolean initied;
  private ImageView inlineButton;
  private String interfaceName;
  private boolean isAutoplay;
  private boolean isCompleted;
  private boolean isInline;
  private boolean isLoading;
  private boolean isStream;
  private long lastUpdateTime;
  private String playAudioType;
  private String playAudioUrl;
  private ImageView playButton;
  private String playVideoType;
  private String playVideoUrl;
  private AnimatorSet progressAnimation;
  private Runnable progressRunnable;
  private RadialProgressView progressView;
  private boolean resumeAudioOnFocusGain;
  private int seekToTime;
  private ImageView shareButton;
  private TextureView.SurfaceTextureListener surfaceTextureListener;
  private Runnable switchToInlineRunnable;
  private boolean switchingInlineMode;
  private ImageView textureImageView;
  private TextureView textureView;
  private ViewGroup textureViewContainer;
  private VideoPlayer videoPlayer;
  private int waitingForFirstTextureUpload;
  private WebView webView;
  
  static
  {
    vimeoIdRegex = Pattern.compile("https?://(?:(?:www|(player))\\.)?vimeo(pro)?\\.com/(?!(?:channels|album)/[^/?#]+/?(?:$|[?#])|[^/]+/review/|ondemand/)(?:.*?/)?(?:(?:play_redirect_hls|moogaloop\\.swf)\\?clip_id=)?(?:videos?/)?([0-9]+)(?:/[\\da-f]+)?/?(?:[?&].*)?(?:[#].*)?$");
    coubIdRegex = Pattern.compile("(?:coub:|https?://(?:coub\\.com/(?:view|embed|coubs)/|c-cdn\\.coub\\.com/fb-player\\.swf\\?.*\\bcoub(?:ID|id)=))([\\da-z]+)");
    aparatIdRegex = Pattern.compile("^https?://(?:www\\.)?aparat\\.com/(?:v/|video/video/embed/videohash/)([a-zA-Z0-9]+)");
    twitchClipIdRegex = Pattern.compile("https?://clips\\.twitch\\.tv/(?:[^/]+/)*([^/?#&]+)");
    twitchStreamIdRegex = Pattern.compile("https?://(?:(?:www\\.)?twitch\\.tv/|player\\.twitch\\.tv/\\?.*?\\bchannel=)([^/#?]+)");
    aparatFileListPattern = Pattern.compile("fileList\\s*=\\s*JSON\\.parse\\('([^']+)'\\)");
    twitchClipFilePattern = Pattern.compile("clipInfo\\s*=\\s*(\\{[^']+\\});");
    stsPattern = Pattern.compile("\"sts\"\\s*:\\s*(\\d+)");
    jsPattern = Pattern.compile("\"assets\":.+?\"js\":\\s*(\"[^\"]+\")");
    sigPattern = Pattern.compile("\\.sig\\|\\|([a-zA-Z0-9$]+)\\(");
    sigPattern2 = Pattern.compile("[\"']signature[\"']\\s*,\\s*([a-zA-Z0-9$]+)\\(");
    stmtVarPattern = Pattern.compile("var\\s");
    stmtReturnPattern = Pattern.compile("return(?:\\s+|$)");
  }
  
  @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
  public WebPlayerView(Context paramContext, boolean paramBoolean1, boolean paramBoolean2, WebPlayerViewDelegate paramWebPlayerViewDelegate)
  {
    super(paramContext);
    int i = lastContainerId;
    lastContainerId = i + 1;
    this.fragment_container_id = i;
    boolean bool;
    if (Build.VERSION.SDK_INT >= 21)
    {
      bool = true;
      this.allowInlineAnimation = bool;
      this.backgroundPaint = new Paint();
      this.progressRunnable = new Runnable()
      {
        public void run()
        {
          if ((WebPlayerView.this.videoPlayer == null) || (!WebPlayerView.this.videoPlayer.isPlaying())) {
            return;
          }
          WebPlayerView.this.controlsView.setProgress((int)(WebPlayerView.this.videoPlayer.getCurrentPosition() / 1000L));
          WebPlayerView.this.controlsView.setBufferedProgress((int)(WebPlayerView.this.videoPlayer.getBufferedPosition() / 1000L));
          AndroidUtilities.runOnUIThread(WebPlayerView.this.progressRunnable, 1000L);
        }
      };
      this.surfaceTextureListener = new TextureView.SurfaceTextureListener()
      {
        public void onSurfaceTextureAvailable(SurfaceTexture paramAnonymousSurfaceTexture, int paramAnonymousInt1, int paramAnonymousInt2) {}
        
        public boolean onSurfaceTextureDestroyed(SurfaceTexture paramAnonymousSurfaceTexture)
        {
          if (WebPlayerView.this.changingTextureView)
          {
            if (WebPlayerView.this.switchingInlineMode) {
              WebPlayerView.access$3102(WebPlayerView.this, 2);
            }
            WebPlayerView.this.textureView.setSurfaceTexture(paramAnonymousSurfaceTexture);
            WebPlayerView.this.textureView.setVisibility(0);
            WebPlayerView.access$2902(WebPlayerView.this, false);
            return false;
          }
          return true;
        }
        
        public void onSurfaceTextureSizeChanged(SurfaceTexture paramAnonymousSurfaceTexture, int paramAnonymousInt1, int paramAnonymousInt2) {}
        
        public void onSurfaceTextureUpdated(SurfaceTexture paramAnonymousSurfaceTexture)
        {
          if (WebPlayerView.this.waitingForFirstTextureUpload == 1)
          {
            WebPlayerView.this.changedTextureView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
            {
              public boolean onPreDraw()
              {
                WebPlayerView.this.changedTextureView.getViewTreeObserver().removeOnPreDrawListener(this);
                if (WebPlayerView.this.textureImageView != null)
                {
                  WebPlayerView.this.textureImageView.setVisibility(4);
                  WebPlayerView.this.textureImageView.setImageDrawable(null);
                  if (WebPlayerView.this.currentBitmap != null)
                  {
                    WebPlayerView.this.currentBitmap.recycle();
                    WebPlayerView.access$3502(WebPlayerView.this, null);
                  }
                }
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    WebPlayerView.this.delegate.onInlineSurfaceTextureReady();
                  }
                });
                WebPlayerView.access$3102(WebPlayerView.this, 0);
                return true;
              }
            });
            WebPlayerView.this.changedTextureView.invalidate();
          }
        }
      };
      this.switchToInlineRunnable = new Runnable()
      {
        public void run()
        {
          WebPlayerView.access$3002(WebPlayerView.this, false);
          if (WebPlayerView.this.currentBitmap != null)
          {
            WebPlayerView.this.currentBitmap.recycle();
            WebPlayerView.access$3502(WebPlayerView.this, null);
          }
          WebPlayerView.access$2902(WebPlayerView.this, true);
          if (WebPlayerView.this.textureImageView != null) {}
          try
          {
            WebPlayerView.access$3502(WebPlayerView.this, Bitmaps.createBitmap(WebPlayerView.this.textureView.getWidth(), WebPlayerView.this.textureView.getHeight(), Bitmap.Config.ARGB_8888));
            WebPlayerView.this.textureView.getBitmap(WebPlayerView.this.currentBitmap);
            if (WebPlayerView.this.currentBitmap != null)
            {
              WebPlayerView.this.textureImageView.setVisibility(0);
              WebPlayerView.this.textureImageView.setImageBitmap(WebPlayerView.this.currentBitmap);
              WebPlayerView.access$3702(WebPlayerView.this, true);
              WebPlayerView.this.updatePlayButton();
              WebPlayerView.this.updateShareButton();
              WebPlayerView.this.updateFullscreenButton();
              WebPlayerView.this.updateInlineButton();
              ViewGroup localViewGroup = (ViewGroup)WebPlayerView.this.controlsView.getParent();
              if (localViewGroup != null) {
                localViewGroup.removeView(WebPlayerView.this.controlsView);
              }
              WebPlayerView.access$3302(WebPlayerView.this, WebPlayerView.this.delegate.onSwitchInlineMode(WebPlayerView.this.controlsView, WebPlayerView.this.isInline, WebPlayerView.this.aspectRatioFrameLayout.getAspectRatio(), WebPlayerView.this.aspectRatioFrameLayout.getVideoRotation(), WebPlayerView.this.allowInlineAnimation));
              WebPlayerView.this.changedTextureView.setVisibility(4);
              localViewGroup = (ViewGroup)WebPlayerView.this.textureView.getParent();
              if (localViewGroup != null) {
                localViewGroup.removeView(WebPlayerView.this.textureView);
              }
              WebPlayerView.this.controlsView.show(false, false);
              return;
            }
          }
          catch (Throwable localThrowable)
          {
            for (;;)
            {
              if (WebPlayerView.this.currentBitmap != null)
              {
                WebPlayerView.this.currentBitmap.recycle();
                WebPlayerView.access$3502(WebPlayerView.this, null);
              }
              FileLog.e(localThrowable);
              continue;
              WebPlayerView.this.textureImageView.setImageDrawable(null);
            }
          }
        }
      };
      setWillNotDraw(false);
      this.delegate = paramWebPlayerViewDelegate;
      this.backgroundPaint.setColor(-16777216);
      this.aspectRatioFrameLayout = new AspectRatioFrameLayout(paramContext)
      {
        protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          super.onMeasure(paramAnonymousInt1, paramAnonymousInt2);
          if (WebPlayerView.this.textureViewContainer != null)
          {
            ViewGroup.LayoutParams localLayoutParams = WebPlayerView.this.textureView.getLayoutParams();
            localLayoutParams.width = getMeasuredWidth();
            localLayoutParams.height = getMeasuredHeight();
            if (WebPlayerView.this.textureImageView != null)
            {
              localLayoutParams = WebPlayerView.this.textureImageView.getLayoutParams();
              localLayoutParams.width = getMeasuredWidth();
              localLayoutParams.height = getMeasuredHeight();
            }
          }
        }
      };
      addView(this.aspectRatioFrameLayout, LayoutHelper.createFrame(-1, -1, 17));
      this.interfaceName = "JavaScriptInterface";
      this.webView = new WebView(paramContext);
      this.webView.addJavascriptInterface(new JavaScriptInterface(new CallJavaResultInterface()
      {
        public void jsCallFinished(String paramAnonymousString)
        {
          if ((WebPlayerView.this.currentTask != null) && (!WebPlayerView.this.currentTask.isCancelled()) && ((WebPlayerView.this.currentTask instanceof WebPlayerView.YoutubeVideoTask))) {
            WebPlayerView.YoutubeVideoTask.access$5200((WebPlayerView.YoutubeVideoTask)WebPlayerView.this.currentTask, paramAnonymousString);
          }
        }
      }), this.interfaceName);
      paramWebPlayerViewDelegate = this.webView.getSettings();
      paramWebPlayerViewDelegate.setJavaScriptEnabled(true);
      paramWebPlayerViewDelegate.setDefaultTextEncodingName("utf-8");
      this.textureViewContainer = this.delegate.getTextureViewContainer();
      this.textureView = new TextureView(paramContext);
      this.textureView.setPivotX(0.0F);
      this.textureView.setPivotY(0.0F);
      if (this.textureViewContainer == null) {
        break label709;
      }
      this.textureViewContainer.addView(this.textureView);
      label264:
      if ((this.allowInlineAnimation) && (this.textureViewContainer != null))
      {
        this.textureImageView = new ImageView(paramContext);
        this.textureImageView.setBackgroundColor(-65536);
        this.textureImageView.setPivotX(0.0F);
        this.textureImageView.setPivotY(0.0F);
        this.textureImageView.setVisibility(4);
        this.textureViewContainer.addView(this.textureImageView);
      }
      this.videoPlayer = new VideoPlayer();
      this.videoPlayer.setDelegate(this);
      this.videoPlayer.setTextureView(this.textureView);
      this.controlsView = new ControlsView(paramContext);
      if (this.textureViewContainer == null) {
        break label730;
      }
      this.textureViewContainer.addView(this.controlsView);
    }
    for (;;)
    {
      this.progressView = new RadialProgressView(paramContext);
      this.progressView.setProgressColor(-1);
      addView(this.progressView, LayoutHelper.createFrame(48, 48, 17));
      this.fullscreenButton = new ImageView(paramContext);
      this.fullscreenButton.setScaleType(ImageView.ScaleType.CENTER);
      this.controlsView.addView(this.fullscreenButton, LayoutHelper.createFrame(56, 56.0F, 85, 0.0F, 0.0F, 0.0F, 5.0F));
      this.fullscreenButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if ((!WebPlayerView.this.initied) || (WebPlayerView.this.changingTextureView) || (WebPlayerView.this.switchingInlineMode) || (!WebPlayerView.this.firstFrameRendered)) {
            return;
          }
          paramAnonymousView = WebPlayerView.this;
          if (!WebPlayerView.this.inFullscreen) {}
          for (boolean bool = true;; bool = false)
          {
            WebPlayerView.access$4502(paramAnonymousView, bool);
            WebPlayerView.this.updateFullscreenState(true);
            return;
          }
        }
      });
      this.playButton = new ImageView(paramContext);
      this.playButton.setScaleType(ImageView.ScaleType.CENTER);
      this.controlsView.addView(this.playButton, LayoutHelper.createFrame(48, 48, 17));
      this.playButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if ((!WebPlayerView.this.initied) || (WebPlayerView.this.playVideoUrl == null)) {
            return;
          }
          if (!WebPlayerView.this.videoPlayer.isPlayerPrepared()) {
            WebPlayerView.this.preparePlayer();
          }
          if (WebPlayerView.this.videoPlayer.isPlaying()) {
            WebPlayerView.this.videoPlayer.pause();
          }
          for (;;)
          {
            WebPlayerView.this.updatePlayButton();
            return;
            WebPlayerView.access$5402(WebPlayerView.this, false);
            WebPlayerView.this.videoPlayer.play();
          }
        }
      });
      if (paramBoolean1)
      {
        this.inlineButton = new ImageView(paramContext);
        this.inlineButton.setScaleType(ImageView.ScaleType.CENTER);
        this.controlsView.addView(this.inlineButton, LayoutHelper.createFrame(56, 48, 53));
        this.inlineButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if ((WebPlayerView.this.textureView == null) || (!WebPlayerView.this.delegate.checkInlinePermissions()) || (WebPlayerView.this.changingTextureView) || (WebPlayerView.this.switchingInlineMode) || (!WebPlayerView.this.firstFrameRendered)) {
              return;
            }
            WebPlayerView.access$3002(WebPlayerView.this, true);
            if (!WebPlayerView.this.isInline)
            {
              WebPlayerView.access$4502(WebPlayerView.this, false);
              WebPlayerView.this.delegate.prepareToSwitchInlineMode(true, WebPlayerView.this.switchToInlineRunnable, WebPlayerView.this.aspectRatioFrameLayout.getAspectRatio(), WebPlayerView.this.allowInlineAnimation);
              return;
            }
            paramAnonymousView = (ViewGroup)WebPlayerView.this.aspectRatioFrameLayout.getParent();
            if (paramAnonymousView != WebPlayerView.this)
            {
              if (paramAnonymousView != null) {
                paramAnonymousView.removeView(WebPlayerView.this.aspectRatioFrameLayout);
              }
              WebPlayerView.this.addView(WebPlayerView.this.aspectRatioFrameLayout, 0, LayoutHelper.createFrame(-1, -1, 17));
              WebPlayerView.this.aspectRatioFrameLayout.measure(View.MeasureSpec.makeMeasureSpec(WebPlayerView.this.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(WebPlayerView.this.getMeasuredHeight() - AndroidUtilities.dp(10.0F), 1073741824));
            }
            if (WebPlayerView.this.currentBitmap != null)
            {
              WebPlayerView.this.currentBitmap.recycle();
              WebPlayerView.access$3502(WebPlayerView.this, null);
            }
            WebPlayerView.access$2902(WebPlayerView.this, true);
            WebPlayerView.access$3702(WebPlayerView.this, false);
            WebPlayerView.this.updatePlayButton();
            WebPlayerView.this.updateShareButton();
            WebPlayerView.this.updateFullscreenButton();
            WebPlayerView.this.updateInlineButton();
            WebPlayerView.this.textureView.setVisibility(4);
            if (WebPlayerView.this.textureViewContainer != null)
            {
              WebPlayerView.this.textureViewContainer.addView(WebPlayerView.this.textureView);
              paramAnonymousView = (ViewGroup)WebPlayerView.this.controlsView.getParent();
              if (paramAnonymousView != WebPlayerView.this)
              {
                if (paramAnonymousView != null) {
                  paramAnonymousView.removeView(WebPlayerView.this.controlsView);
                }
                if (WebPlayerView.this.textureViewContainer == null) {
                  break label462;
                }
                WebPlayerView.this.textureViewContainer.addView(WebPlayerView.this.controlsView);
              }
            }
            for (;;)
            {
              WebPlayerView.this.controlsView.show(false, false);
              WebPlayerView.this.delegate.prepareToSwitchInlineMode(false, null, WebPlayerView.this.aspectRatioFrameLayout.getAspectRatio(), WebPlayerView.this.allowInlineAnimation);
              return;
              WebPlayerView.this.aspectRatioFrameLayout.addView(WebPlayerView.this.textureView);
              break;
              label462:
              WebPlayerView.this.addView(WebPlayerView.this.controlsView, 1);
            }
          }
        });
      }
      if (paramBoolean2)
      {
        this.shareButton = new ImageView(paramContext);
        this.shareButton.setScaleType(ImageView.ScaleType.CENTER);
        this.shareButton.setImageResource(2131165409);
        this.controlsView.addView(this.shareButton, LayoutHelper.createFrame(56, 48, 53));
        this.shareButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (WebPlayerView.this.delegate != null) {
              WebPlayerView.this.delegate.onSharePressed();
            }
          }
        });
      }
      updatePlayButton();
      updateFullscreenButton();
      updateInlineButton();
      updateShareButton();
      return;
      bool = false;
      break;
      label709:
      this.aspectRatioFrameLayout.addView(this.textureView, LayoutHelper.createFrame(-1, -1, 17));
      break label264;
      label730:
      addView(this.controlsView, LayoutHelper.createFrame(-1, -1.0F));
    }
  }
  
  private void checkAudioFocus()
  {
    if (!this.hasAudioFocus)
    {
      AudioManager localAudioManager = (AudioManager)ApplicationLoader.applicationContext.getSystemService("audio");
      this.hasAudioFocus = true;
      if (localAudioManager.requestAudioFocus(this, 3, 1) == 1) {
        this.audioFocus = 2;
      }
    }
  }
  
  private View getControlView()
  {
    return this.controlsView;
  }
  
  private View getProgressView()
  {
    return this.progressView;
  }
  
  private void onInitFailed()
  {
    if (this.controlsView.getParent() != this) {
      this.controlsView.setVisibility(8);
    }
    this.delegate.onInitFailed();
  }
  
  private void preparePlayer()
  {
    if (this.playVideoUrl == null) {
      return;
    }
    if ((this.playVideoUrl != null) && (this.playAudioUrl != null))
    {
      this.videoPlayer.preparePlayerLoop(Uri.parse(this.playVideoUrl), this.playVideoType, Uri.parse(this.playAudioUrl), this.playAudioType);
      label51:
      this.videoPlayer.setPlayWhenReady(this.isAutoplay);
      this.isLoading = false;
      if (this.videoPlayer.getDuration() == -9223372036854775807L) {
        break label165;
      }
      this.controlsView.setDuration((int)(this.videoPlayer.getDuration() / 1000L));
    }
    for (;;)
    {
      updateFullscreenButton();
      updateShareButton();
      updateInlineButton();
      this.controlsView.invalidate();
      if (this.seekToTime == -1) {
        break;
      }
      this.videoPlayer.seekTo(this.seekToTime * 1000);
      return;
      this.videoPlayer.preparePlayer(Uri.parse(this.playVideoUrl), this.playVideoType);
      break label51;
      label165:
      this.controlsView.setDuration(0);
    }
  }
  
  private void showProgress(boolean paramBoolean1, boolean paramBoolean2)
  {
    float f = 1.0F;
    if (paramBoolean2)
    {
      if (this.progressAnimation != null) {
        this.progressAnimation.cancel();
      }
      this.progressAnimation = new AnimatorSet();
      localObject = this.progressAnimation;
      RadialProgressView localRadialProgressView = this.progressView;
      if (paramBoolean1) {}
      for (;;)
      {
        ((AnimatorSet)localObject).playTogether(new Animator[] { ObjectAnimator.ofFloat(localRadialProgressView, "alpha", new float[] { f }) });
        this.progressAnimation.setDuration(150L);
        this.progressAnimation.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            WebPlayerView.access$5802(WebPlayerView.this, null);
          }
        });
        this.progressAnimation.start();
        return;
        f = 0.0F;
      }
    }
    Object localObject = this.progressView;
    if (paramBoolean1) {}
    for (;;)
    {
      ((RadialProgressView)localObject).setAlpha(f);
      return;
      f = 0.0F;
    }
  }
  
  private void updateFullscreenButton()
  {
    if ((!this.videoPlayer.isPlayerPrepared()) || (this.isInline))
    {
      this.fullscreenButton.setVisibility(8);
      return;
    }
    this.fullscreenButton.setVisibility(0);
    if (!this.inFullscreen)
    {
      this.fullscreenButton.setImageResource(2131165381);
      this.fullscreenButton.setLayoutParams(LayoutHelper.createFrame(56, 56.0F, 85, 0.0F, 0.0F, 0.0F, 5.0F));
      return;
    }
    this.fullscreenButton.setImageResource(2131165396);
    this.fullscreenButton.setLayoutParams(LayoutHelper.createFrame(56, 56.0F, 85, 0.0F, 0.0F, 0.0F, 1.0F));
  }
  
  private void updateFullscreenState(boolean paramBoolean)
  {
    if (this.textureView == null) {
      return;
    }
    updateFullscreenButton();
    ViewGroup localViewGroup;
    if (this.textureViewContainer == null)
    {
      this.changingTextureView = true;
      if (!this.inFullscreen)
      {
        if (this.textureViewContainer != null) {
          this.textureViewContainer.addView(this.textureView);
        }
      }
      else
      {
        if (!this.inFullscreen) {
          break label182;
        }
        localViewGroup = (ViewGroup)this.controlsView.getParent();
        if (localViewGroup != null) {
          localViewGroup.removeView(this.controlsView);
        }
      }
      for (;;)
      {
        this.changedTextureView = this.delegate.onSwitchToFullscreen(this.controlsView, this.inFullscreen, this.aspectRatioFrameLayout.getAspectRatio(), this.aspectRatioFrameLayout.getVideoRotation(), paramBoolean);
        this.changedTextureView.setVisibility(4);
        if ((this.inFullscreen) && (this.changedTextureView != null))
        {
          localViewGroup = (ViewGroup)this.textureView.getParent();
          if (localViewGroup != null) {
            localViewGroup.removeView(this.textureView);
          }
        }
        this.controlsView.checkNeedHide();
        return;
        this.aspectRatioFrameLayout.addView(this.textureView);
        break;
        label182:
        localViewGroup = (ViewGroup)this.controlsView.getParent();
        if (localViewGroup != this)
        {
          if (localViewGroup != null) {
            localViewGroup.removeView(this.controlsView);
          }
          if (this.textureViewContainer != null) {
            this.textureViewContainer.addView(this.controlsView);
          } else {
            addView(this.controlsView, 1);
          }
        }
      }
    }
    if (this.inFullscreen)
    {
      localViewGroup = (ViewGroup)this.aspectRatioFrameLayout.getParent();
      if (localViewGroup != null) {
        localViewGroup.removeView(this.aspectRatioFrameLayout);
      }
    }
    for (;;)
    {
      this.delegate.onSwitchToFullscreen(this.controlsView, this.inFullscreen, this.aspectRatioFrameLayout.getAspectRatio(), this.aspectRatioFrameLayout.getVideoRotation(), paramBoolean);
      return;
      localViewGroup = (ViewGroup)this.aspectRatioFrameLayout.getParent();
      if (localViewGroup != this)
      {
        if (localViewGroup != null) {
          localViewGroup.removeView(this.aspectRatioFrameLayout);
        }
        addView(this.aspectRatioFrameLayout, 0);
      }
    }
  }
  
  private void updateInlineButton()
  {
    if (this.inlineButton == null) {
      return;
    }
    ImageView localImageView = this.inlineButton;
    if (this.isInline)
    {
      i = 2131165382;
      localImageView.setImageResource(i);
      localImageView = this.inlineButton;
      if (!this.videoPlayer.isPlayerPrepared()) {
        break label82;
      }
    }
    label82:
    for (int i = 0;; i = 8)
    {
      localImageView.setVisibility(i);
      if (!this.isInline) {
        break label88;
      }
      this.inlineButton.setLayoutParams(LayoutHelper.createFrame(40, 40, 53));
      return;
      i = 2131165397;
      break;
    }
    label88:
    this.inlineButton.setLayoutParams(LayoutHelper.createFrame(56, 50, 53));
  }
  
  private void updatePlayButton()
  {
    this.controlsView.checkNeedHide();
    AndroidUtilities.cancelRunOnUIThread(this.progressRunnable);
    if (!this.videoPlayer.isPlaying())
    {
      if (this.isCompleted)
      {
        localImageView = this.playButton;
        if (this.isInline) {}
        for (i = 2131165363;; i = 2131165362)
        {
          localImageView.setImageResource(i);
          return;
        }
      }
      localImageView = this.playButton;
      if (this.isInline) {}
      for (i = 2131165403;; i = 2131165401)
      {
        localImageView.setImageResource(i);
        return;
      }
    }
    ImageView localImageView = this.playButton;
    if (this.isInline) {}
    for (int i = 2131165399;; i = 2131165398)
    {
      localImageView.setImageResource(i);
      AndroidUtilities.runOnUIThread(this.progressRunnable, 500L);
      checkAudioFocus();
      return;
    }
  }
  
  private void updateShareButton()
  {
    if (this.shareButton == null) {
      return;
    }
    ImageView localImageView = this.shareButton;
    if ((this.isInline) || (!this.videoPlayer.isPlayerPrepared())) {}
    for (int i = 8;; i = 0)
    {
      localImageView.setVisibility(i);
      return;
    }
  }
  
  public void destroy()
  {
    this.videoPlayer.releasePlayer();
    if (this.currentTask != null)
    {
      this.currentTask.cancel(true);
      this.currentTask = null;
    }
    this.webView.stopLoading();
  }
  
  protected String downloadUrlContent(AsyncTask paramAsyncTask, String paramString)
  {
    return downloadUrlContent(paramAsyncTask, paramString, null, true);
  }
  
  /* Error */
  protected String downloadUrlContent(AsyncTask paramAsyncTask, String paramString, HashMap<String, String> paramHashMap, boolean paramBoolean)
  {
    // Byte code:
    //   0: iconst_1
    //   1: istore 5
    //   3: iconst_1
    //   4: istore 8
    //   6: aconst_null
    //   7: astore 16
    //   9: iconst_0
    //   10: istore 6
    //   12: iconst_0
    //   13: istore 7
    //   15: aconst_null
    //   16: astore 15
    //   18: aconst_null
    //   19: astore 13
    //   21: aconst_null
    //   22: astore 14
    //   24: aconst_null
    //   25: astore 11
    //   27: aload 11
    //   29: astore 10
    //   31: new 793	java/net/URL
    //   34: dup
    //   35: aload_2
    //   36: invokespecial 795	java/net/URL:<init>	(Ljava/lang/String;)V
    //   39: astore 17
    //   41: aload 11
    //   43: astore 10
    //   45: aload 17
    //   47: invokevirtual 799	java/net/URL:openConnection	()Ljava/net/URLConnection;
    //   50: astore 12
    //   52: aload 12
    //   54: astore 10
    //   56: aload 12
    //   58: ldc_w 801
    //   61: ldc_w 803
    //   64: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   67: iload 4
    //   69: ifeq +18 -> 87
    //   72: aload 12
    //   74: astore 10
    //   76: aload 12
    //   78: ldc_w 811
    //   81: ldc_w 813
    //   84: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   87: aload 12
    //   89: astore 10
    //   91: aload 12
    //   93: ldc_w 815
    //   96: ldc_w 817
    //   99: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   102: aload 12
    //   104: astore 10
    //   106: aload 12
    //   108: ldc_w 819
    //   111: ldc_w 821
    //   114: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   117: aload 12
    //   119: astore 10
    //   121: aload 12
    //   123: ldc_w 823
    //   126: ldc_w 825
    //   129: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   132: aload_3
    //   133: ifnull +233 -> 366
    //   136: aload 12
    //   138: astore 10
    //   140: aload_3
    //   141: invokevirtual 831	java/util/HashMap:entrySet	()Ljava/util/Set;
    //   144: invokeinterface 837 1 0
    //   149: astore 11
    //   151: aload 12
    //   153: astore 10
    //   155: aload 11
    //   157: invokeinterface 842 1 0
    //   162: ifeq +204 -> 366
    //   165: aload 12
    //   167: astore 10
    //   169: aload 11
    //   171: invokeinterface 846 1 0
    //   176: checkcast 848	java/util/Map$Entry
    //   179: astore_2
    //   180: aload 12
    //   182: astore 10
    //   184: aload 12
    //   186: aload_2
    //   187: invokeinterface 851 1 0
    //   192: checkcast 853	java/lang/String
    //   195: aload_2
    //   196: invokeinterface 856 1 0
    //   201: checkcast 853	java/lang/String
    //   204: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   207: goto -56 -> 151
    //   210: astore_2
    //   211: aload_2
    //   212: instanceof 858
    //   215: ifeq +576 -> 791
    //   218: iload 8
    //   220: istore 5
    //   222: invokestatic 863	org/telegram/tgnet/ConnectionsManager:isNetworkOnline	()Z
    //   225: ifeq +6 -> 231
    //   228: iconst_0
    //   229: istore 5
    //   231: aload_2
    //   232: invokestatic 869	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   235: aload 16
    //   237: astore_3
    //   238: aload 10
    //   240: astore_2
    //   241: aload 15
    //   243: astore 10
    //   245: iload 5
    //   247: ifeq +108 -> 355
    //   250: aload_2
    //   251: ifnull +43 -> 294
    //   254: aload_2
    //   255: instanceof 871
    //   258: ifeq +36 -> 294
    //   261: aload_2
    //   262: checkcast 871	java/net/HttpURLConnection
    //   265: invokevirtual 874	java/net/HttpURLConnection:getResponseCode	()I
    //   268: istore 5
    //   270: iload 5
    //   272: sipush 200
    //   275: if_icmpeq +19 -> 294
    //   278: iload 5
    //   280: sipush 202
    //   283: if_icmpeq +11 -> 294
    //   286: iload 5
    //   288: sipush 304
    //   291: if_icmpeq +3 -> 294
    //   294: iload 7
    //   296: istore 5
    //   298: aload 14
    //   300: astore_2
    //   301: aload_3
    //   302: ifnull +31 -> 333
    //   305: aload 13
    //   307: astore 10
    //   309: ldc_w 875
    //   312: newarray <illegal type>
    //   314: astore 11
    //   316: aconst_null
    //   317: astore_2
    //   318: aload_1
    //   319: invokevirtual 878	android/os/AsyncTask:isCancelled	()Z
    //   322: istore 4
    //   324: iload 4
    //   326: ifeq +544 -> 870
    //   329: iload 7
    //   331: istore 5
    //   333: iload 5
    //   335: istore 6
    //   337: aload_2
    //   338: astore 10
    //   340: aload_3
    //   341: ifnull +14 -> 355
    //   344: aload_3
    //   345: invokevirtual 883	java/io/InputStream:close	()V
    //   348: aload_2
    //   349: astore 10
    //   351: iload 5
    //   353: istore 6
    //   355: iload 6
    //   357: ifeq +632 -> 989
    //   360: aload 10
    //   362: invokevirtual 889	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   365: areturn
    //   366: aload 12
    //   368: astore 10
    //   370: aload 12
    //   372: sipush 5000
    //   375: invokevirtual 892	java/net/URLConnection:setConnectTimeout	(I)V
    //   378: aload 12
    //   380: astore 10
    //   382: aload 12
    //   384: sipush 5000
    //   387: invokevirtual 895	java/net/URLConnection:setReadTimeout	(I)V
    //   390: aload 12
    //   392: astore 10
    //   394: aload 17
    //   396: astore 11
    //   398: aload 12
    //   400: astore_2
    //   401: aload 12
    //   403: instanceof 871
    //   406: ifeq +301 -> 707
    //   409: aload 12
    //   411: astore 10
    //   413: aload 12
    //   415: checkcast 871	java/net/HttpURLConnection
    //   418: astore 18
    //   420: aload 12
    //   422: astore 10
    //   424: aload 18
    //   426: iconst_1
    //   427: invokevirtual 898	java/net/HttpURLConnection:setInstanceFollowRedirects	(Z)V
    //   430: aload 12
    //   432: astore 10
    //   434: aload 18
    //   436: invokevirtual 874	java/net/HttpURLConnection:getResponseCode	()I
    //   439: istore 9
    //   441: iload 9
    //   443: sipush 302
    //   446: if_icmpeq +26 -> 472
    //   449: iload 9
    //   451: sipush 301
    //   454: if_icmpeq +18 -> 472
    //   457: aload 17
    //   459: astore 11
    //   461: aload 12
    //   463: astore_2
    //   464: iload 9
    //   466: sipush 303
    //   469: if_icmpne +238 -> 707
    //   472: aload 12
    //   474: astore 10
    //   476: aload 18
    //   478: ldc_w 900
    //   481: invokevirtual 904	java/net/HttpURLConnection:getHeaderField	(Ljava/lang/String;)Ljava/lang/String;
    //   484: astore 11
    //   486: aload 12
    //   488: astore 10
    //   490: aload 18
    //   492: ldc_w 906
    //   495: invokevirtual 904	java/net/HttpURLConnection:getHeaderField	(Ljava/lang/String;)Ljava/lang/String;
    //   498: astore_2
    //   499: aload 12
    //   501: astore 10
    //   503: new 793	java/net/URL
    //   506: dup
    //   507: aload 11
    //   509: invokespecial 795	java/net/URL:<init>	(Ljava/lang/String;)V
    //   512: astore 17
    //   514: aload 12
    //   516: astore 10
    //   518: aload 17
    //   520: invokevirtual 799	java/net/URL:openConnection	()Ljava/net/URLConnection;
    //   523: astore 12
    //   525: aload 12
    //   527: astore 10
    //   529: aload 12
    //   531: ldc_w 908
    //   534: aload_2
    //   535: invokevirtual 911	java/net/URLConnection:setRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   538: aload 12
    //   540: astore 10
    //   542: aload 12
    //   544: ldc_w 801
    //   547: ldc_w 803
    //   550: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   553: iload 4
    //   555: ifeq +18 -> 573
    //   558: aload 12
    //   560: astore 10
    //   562: aload 12
    //   564: ldc_w 811
    //   567: ldc_w 813
    //   570: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   573: aload 12
    //   575: astore 10
    //   577: aload 12
    //   579: ldc_w 815
    //   582: ldc_w 817
    //   585: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   588: aload 12
    //   590: astore 10
    //   592: aload 12
    //   594: ldc_w 819
    //   597: ldc_w 821
    //   600: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   603: aload 12
    //   605: astore 10
    //   607: aload 12
    //   609: ldc_w 823
    //   612: ldc_w 825
    //   615: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   618: aload 17
    //   620: astore 11
    //   622: aload 12
    //   624: astore_2
    //   625: aload_3
    //   626: ifnull +81 -> 707
    //   629: aload 12
    //   631: astore 10
    //   633: aload_3
    //   634: invokevirtual 831	java/util/HashMap:entrySet	()Ljava/util/Set;
    //   637: invokeinterface 837 1 0
    //   642: astore_3
    //   643: aload 12
    //   645: astore 10
    //   647: aload 17
    //   649: astore 11
    //   651: aload 12
    //   653: astore_2
    //   654: aload_3
    //   655: invokeinterface 842 1 0
    //   660: ifeq +47 -> 707
    //   663: aload 12
    //   665: astore 10
    //   667: aload_3
    //   668: invokeinterface 846 1 0
    //   673: checkcast 848	java/util/Map$Entry
    //   676: astore_2
    //   677: aload 12
    //   679: astore 10
    //   681: aload 12
    //   683: aload_2
    //   684: invokeinterface 851 1 0
    //   689: checkcast 853	java/lang/String
    //   692: aload_2
    //   693: invokeinterface 856 1 0
    //   698: checkcast 853	java/lang/String
    //   701: invokevirtual 809	java/net/URLConnection:addRequestProperty	(Ljava/lang/String;Ljava/lang/String;)V
    //   704: goto -61 -> 643
    //   707: aload_2
    //   708: astore 10
    //   710: aload_2
    //   711: invokevirtual 914	java/net/URLConnection:connect	()V
    //   714: iload 4
    //   716: ifeq +64 -> 780
    //   719: aload_2
    //   720: astore 10
    //   722: new 916	java/util/zip/GZIPInputStream
    //   725: dup
    //   726: aload_2
    //   727: invokevirtual 920	java/net/URLConnection:getInputStream	()Ljava/io/InputStream;
    //   730: invokespecial 923	java/util/zip/GZIPInputStream:<init>	(Ljava/io/InputStream;)V
    //   733: astore_3
    //   734: goto -493 -> 241
    //   737: astore_3
    //   738: iconst_0
    //   739: ifeq +14 -> 753
    //   742: aload_2
    //   743: astore 10
    //   745: new 925	java/lang/NullPointerException
    //   748: dup
    //   749: invokespecial 926	java/lang/NullPointerException:<init>	()V
    //   752: athrow
    //   753: aload_2
    //   754: astore 10
    //   756: aload 11
    //   758: invokevirtual 799	java/net/URL:openConnection	()Ljava/net/URLConnection;
    //   761: astore_2
    //   762: aload_2
    //   763: astore 10
    //   765: aload_2
    //   766: invokevirtual 914	java/net/URLConnection:connect	()V
    //   769: aload_2
    //   770: astore 10
    //   772: aload_2
    //   773: invokevirtual 920	java/net/URLConnection:getInputStream	()Ljava/io/InputStream;
    //   776: astore_3
    //   777: goto -536 -> 241
    //   780: aload_2
    //   781: astore 10
    //   783: aload_2
    //   784: invokevirtual 920	java/net/URLConnection:getInputStream	()Ljava/io/InputStream;
    //   787: astore_3
    //   788: goto -547 -> 241
    //   791: aload_2
    //   792: instanceof 928
    //   795: ifeq +9 -> 804
    //   798: iconst_0
    //   799: istore 5
    //   801: goto -570 -> 231
    //   804: aload_2
    //   805: instanceof 930
    //   808: ifeq +37 -> 845
    //   811: iload 8
    //   813: istore 5
    //   815: aload_2
    //   816: invokevirtual 933	java/lang/Throwable:getMessage	()Ljava/lang/String;
    //   819: ifnull -588 -> 231
    //   822: iload 8
    //   824: istore 5
    //   826: aload_2
    //   827: invokevirtual 933	java/lang/Throwable:getMessage	()Ljava/lang/String;
    //   830: ldc_w 935
    //   833: invokevirtual 939	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   836: ifeq -605 -> 231
    //   839: iconst_0
    //   840: istore 5
    //   842: goto -611 -> 231
    //   845: iload 8
    //   847: istore 5
    //   849: aload_2
    //   850: instanceof 941
    //   853: ifeq -622 -> 231
    //   856: iconst_0
    //   857: istore 5
    //   859: goto -628 -> 231
    //   862: astore_2
    //   863: aload_2
    //   864: invokestatic 869	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   867: goto -573 -> 294
    //   870: aload_3
    //   871: aload 11
    //   873: invokevirtual 945	java/io/InputStream:read	([B)I
    //   876: istore 5
    //   878: iload 5
    //   880: ifle +45 -> 925
    //   883: aload_2
    //   884: ifnonnull +119 -> 1003
    //   887: new 885	java/lang/StringBuilder
    //   890: dup
    //   891: invokespecial 946	java/lang/StringBuilder:<init>	()V
    //   894: astore 10
    //   896: aload 10
    //   898: astore_2
    //   899: aload_2
    //   900: astore 10
    //   902: aload_2
    //   903: new 853	java/lang/String
    //   906: dup
    //   907: aload 11
    //   909: iconst_0
    //   910: iload 5
    //   912: ldc_w 948
    //   915: invokespecial 951	java/lang/String:<init>	([BIILjava/lang/String;)V
    //   918: invokevirtual 955	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   921: pop
    //   922: goto -604 -> 318
    //   925: iload 5
    //   927: iconst_m1
    //   928: if_icmpne +9 -> 937
    //   931: iconst_1
    //   932: istore 5
    //   934: goto -601 -> 333
    //   937: iload 7
    //   939: istore 5
    //   941: goto -608 -> 333
    //   944: astore_1
    //   945: aload_2
    //   946: astore 10
    //   948: aload_1
    //   949: invokestatic 869	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   952: iload 7
    //   954: istore 5
    //   956: goto -623 -> 333
    //   959: astore_1
    //   960: aload 10
    //   962: astore_2
    //   963: aload_1
    //   964: invokestatic 869	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   967: iload 7
    //   969: istore 5
    //   971: goto -638 -> 333
    //   974: astore_1
    //   975: aload_1
    //   976: invokestatic 869	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   979: iload 5
    //   981: istore 6
    //   983: aload_2
    //   984: astore 10
    //   986: goto -631 -> 355
    //   989: aconst_null
    //   990: areturn
    //   991: astore_3
    //   992: goto -239 -> 753
    //   995: astore_1
    //   996: goto -33 -> 963
    //   999: astore_1
    //   1000: goto -55 -> 945
    //   1003: goto -104 -> 899
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1006	0	this	WebPlayerView
    //   0	1006	1	paramAsyncTask	AsyncTask
    //   0	1006	2	paramString	String
    //   0	1006	3	paramHashMap	HashMap<String, String>
    //   0	1006	4	paramBoolean	boolean
    //   1	979	5	i	int
    //   10	972	6	j	int
    //   13	955	7	k	int
    //   4	842	8	m	int
    //   439	31	9	n	int
    //   29	956	10	localObject1	Object
    //   25	883	11	localObject2	Object
    //   50	632	12	localURLConnection	java.net.URLConnection
    //   19	287	13	localObject3	Object
    //   22	277	14	localObject4	Object
    //   16	226	15	localObject5	Object
    //   7	229	16	localObject6	Object
    //   39	609	17	localURL	java.net.URL
    //   418	73	18	localHttpURLConnection	java.net.HttpURLConnection
    // Exception table:
    //   from	to	target	type
    //   31	41	210	java/lang/Throwable
    //   45	52	210	java/lang/Throwable
    //   56	67	210	java/lang/Throwable
    //   76	87	210	java/lang/Throwable
    //   91	102	210	java/lang/Throwable
    //   106	117	210	java/lang/Throwable
    //   121	132	210	java/lang/Throwable
    //   140	151	210	java/lang/Throwable
    //   155	165	210	java/lang/Throwable
    //   169	180	210	java/lang/Throwable
    //   184	207	210	java/lang/Throwable
    //   370	378	210	java/lang/Throwable
    //   382	390	210	java/lang/Throwable
    //   401	409	210	java/lang/Throwable
    //   413	420	210	java/lang/Throwable
    //   424	430	210	java/lang/Throwable
    //   434	441	210	java/lang/Throwable
    //   476	486	210	java/lang/Throwable
    //   490	499	210	java/lang/Throwable
    //   503	514	210	java/lang/Throwable
    //   518	525	210	java/lang/Throwable
    //   529	538	210	java/lang/Throwable
    //   542	553	210	java/lang/Throwable
    //   562	573	210	java/lang/Throwable
    //   577	588	210	java/lang/Throwable
    //   592	603	210	java/lang/Throwable
    //   607	618	210	java/lang/Throwable
    //   633	643	210	java/lang/Throwable
    //   654	663	210	java/lang/Throwable
    //   667	677	210	java/lang/Throwable
    //   681	704	210	java/lang/Throwable
    //   710	714	210	java/lang/Throwable
    //   722	734	210	java/lang/Throwable
    //   745	753	210	java/lang/Throwable
    //   756	762	210	java/lang/Throwable
    //   765	769	210	java/lang/Throwable
    //   772	777	210	java/lang/Throwable
    //   783	788	210	java/lang/Throwable
    //   722	734	737	java/lang/Exception
    //   254	270	862	java/lang/Exception
    //   870	878	944	java/lang/Exception
    //   887	896	944	java/lang/Exception
    //   309	316	959	java/lang/Throwable
    //   902	922	959	java/lang/Throwable
    //   948	952	959	java/lang/Throwable
    //   344	348	974	java/lang/Throwable
    //   745	753	991	java/lang/Exception
    //   318	324	995	java/lang/Throwable
    //   870	878	995	java/lang/Throwable
    //   887	896	995	java/lang/Throwable
    //   902	922	999	java/lang/Exception
  }
  
  public void enterFullscreen()
  {
    if (this.inFullscreen) {
      return;
    }
    this.inFullscreen = true;
    updateInlineButton();
    updateFullscreenState(false);
  }
  
  public void exitFullscreen()
  {
    if (!this.inFullscreen) {
      return;
    }
    this.inFullscreen = false;
    updateInlineButton();
    updateFullscreenState(false);
  }
  
  public View getAspectRatioView()
  {
    return this.aspectRatioFrameLayout;
  }
  
  public View getControlsView()
  {
    return this.controlsView;
  }
  
  public ImageView getTextureImageView()
  {
    return this.textureImageView;
  }
  
  public TextureView getTextureView()
  {
    return this.textureView;
  }
  
  public String getYoutubeId()
  {
    return this.currentYoutubeId;
  }
  
  public boolean isInFullscreen()
  {
    return this.inFullscreen;
  }
  
  public boolean isInitied()
  {
    return this.initied;
  }
  
  public boolean isInline()
  {
    return (this.isInline) || (this.switchingInlineMode);
  }
  
  public boolean loadVideo(String paramString1, TLRPC.Photo paramPhoto, String paramString2, boolean paramBoolean)
  {
    Object localObject3 = null;
    Object localObject1 = null;
    Object localObject16 = null;
    Object localObject17 = null;
    Object localObject14 = null;
    Object localObject7 = null;
    Object localObject18 = null;
    String str2 = null;
    Object localObject19 = null;
    Object localObject15 = null;
    Object localObject6 = null;
    Object localObject20 = null;
    this.seekToTime = -1;
    Object localObject8 = localObject20;
    Object localObject12 = localObject14;
    Object localObject13 = localObject15;
    Object localObject9 = localObject18;
    Object localObject10 = localObject19;
    Object localObject11 = localObject17;
    Object localObject5 = localObject1;
    if (paramString1 != null)
    {
      if (paramString1.endsWith(".mp4"))
      {
        localObject13 = paramString1;
        localObject5 = localObject1;
        localObject11 = localObject17;
        localObject10 = localObject19;
        localObject9 = localObject18;
        localObject12 = localObject14;
        localObject8 = localObject20;
      }
    }
    else
    {
      this.initied = false;
      this.isCompleted = false;
      this.isAutoplay = paramBoolean;
      this.playVideoUrl = null;
      this.playAudioUrl = null;
      destroy();
      this.firstFrameRendered = false;
      this.currentAlpha = 1.0F;
      if (this.currentTask != null)
      {
        this.currentTask.cancel(true);
        this.currentTask = null;
      }
      updateFullscreenButton();
      updateShareButton();
      updateInlineButton();
      updatePlayButton();
      if (paramPhoto == null) {
        break label977;
      }
      paramString2 = FileLoader.getClosestPhotoSizeWithSize(paramPhoto.sizes, 80, true);
      if (paramString2 != null)
      {
        localObject1 = this.controlsView.imageReceiver;
        if (paramPhoto == null) {
          break label967;
        }
        paramString2 = paramString2.location;
        label224:
        if (paramPhoto == null) {
          break label972;
        }
        paramPhoto = "80_80_b";
        label232:
        ((ImageReceiver)localObject1).setImage(null, null, paramString2, paramPhoto, 0, null, 1);
      }
    }
    label967:
    label972:
    label977:
    for (this.drawImage = true;; this.drawImage = false)
    {
      if (this.progressAnimation != null)
      {
        this.progressAnimation.cancel();
        this.progressAnimation = null;
      }
      this.isLoading = true;
      this.controlsView.setProgress(0);
      paramPhoto = (TLRPC.Photo)localObject5;
      if (localObject5 != null)
      {
        paramPhoto = (TLRPC.Photo)localObject5;
        if (!BuildVars.DEBUG_PRIVATE_VERSION)
        {
          this.currentYoutubeId = ((String)localObject5);
          paramPhoto = null;
        }
      }
      if (localObject13 == null) {
        break label985;
      }
      this.initied = true;
      this.playVideoUrl = ((String)localObject13);
      this.playVideoType = "other";
      if (this.isAutoplay) {
        preparePlayer();
      }
      showProgress(false, false);
      this.controlsView.show(true, true);
      if ((paramPhoto == null) && (localObject11 == null) && (localObject12 == null) && (localObject8 == null) && (localObject13 == null) && (localObject9 == null) && (localObject10 == null)) {
        break label1298;
      }
      this.controlsView.setVisibility(0);
      return true;
      if (paramString2 != null) {}
      for (;;)
      {
        try
        {
          localObject5 = Uri.parse(paramString2);
          localObject1 = ((Uri)localObject5).getQueryParameter("t");
          paramString2 = (String)localObject1;
          if (localObject1 == null) {
            paramString2 = ((Uri)localObject5).getQueryParameter("time_continue");
          }
          if (paramString2 != null)
          {
            if (!paramString2.contains("m")) {
              continue;
            }
            paramString2 = paramString2.split("m");
            this.seekToTime = (Utilities.parseInt(paramString2[0]).intValue() * 60 + Utilities.parseInt(paramString2[1]).intValue());
          }
        }
        catch (Exception paramString2)
        {
          FileLog.e(paramString2);
          continue;
        }
        try
        {
          paramString2 = youtubeIdRegex.matcher(paramString1);
          localObject1 = null;
          if (paramString2.find()) {
            localObject1 = paramString2.group(1);
          }
          paramString2 = (String)localObject3;
          if (localObject1 != null) {
            paramString2 = (String)localObject1;
          }
        }
        catch (Exception paramString2)
        {
          FileLog.e(paramString2);
          paramString2 = (String)localObject3;
          continue;
        }
        localObject1 = localObject16;
        if (paramString2 == null) {}
        try
        {
          localObject1 = vimeoIdRegex.matcher(paramString1);
          localObject3 = null;
          if (((Matcher)localObject1).find()) {
            localObject3 = ((Matcher)localObject1).group(3);
          }
          localObject1 = localObject16;
          if (localObject3 != null) {
            localObject1 = localObject3;
          }
        }
        catch (Exception localException1)
        {
          FileLog.e(localException1);
          localObject2 = localObject16;
          continue;
        }
        localObject3 = localObject6;
        if (localObject1 == null) {}
        try
        {
          localObject3 = aparatIdRegex.matcher(paramString1);
          localObject5 = null;
          if (((Matcher)localObject3).find()) {
            localObject5 = ((Matcher)localObject3).group(1);
          }
          localObject3 = localObject6;
          if (localObject5 != null) {
            localObject3 = localObject5;
          }
        }
        catch (Exception localException2)
        {
          FileLog.e(localException2);
          localObject4 = localObject6;
          continue;
        }
        localObject6 = localObject7;
        if (localObject3 == null) {}
        try
        {
          localObject6 = twitchClipIdRegex.matcher(paramString1);
          localObject5 = null;
          if (((Matcher)localObject6).find()) {
            localObject5 = ((Matcher)localObject6).group(1);
          }
          localObject6 = localObject7;
          if (localObject5 != null) {
            localObject6 = localObject5;
          }
        }
        catch (Exception localException3)
        {
          FileLog.e(localException3);
          localObject6 = localObject7;
          continue;
        }
        localObject7 = str2;
        if (localObject6 == null) {}
        try
        {
          localObject7 = twitchStreamIdRegex.matcher(paramString1);
          localObject5 = null;
          if (((Matcher)localObject7).find()) {
            localObject5 = ((Matcher)localObject7).group(1);
          }
          localObject7 = str2;
          if (localObject5 != null) {
            localObject7 = localObject5;
          }
        }
        catch (Exception localException4)
        {
          FileLog.e(localException4);
          localObject7 = str2;
          continue;
        }
        localObject8 = localObject3;
        localObject12 = localObject14;
        localObject13 = localObject15;
        localObject9 = localObject6;
        localObject10 = localObject7;
        localObject11 = localObject1;
        localObject5 = paramString2;
        if (localObject7 != null) {
          break;
        }
        try
        {
          localObject5 = coubIdRegex.matcher(paramString1);
          str2 = null;
          if (((Matcher)localObject5).find()) {
            str2 = ((Matcher)localObject5).group(1);
          }
          localObject8 = localObject3;
          localObject12 = localObject14;
          localObject13 = localObject15;
          localObject9 = localObject6;
          localObject10 = localObject7;
          localObject11 = localObject1;
          localObject5 = paramString2;
          if (str2 == null) {
            break;
          }
          localObject8 = localObject3;
          localObject12 = str2;
          localObject13 = localObject15;
          localObject9 = localObject6;
          localObject10 = localObject7;
          localObject11 = localObject1;
          localObject5 = paramString2;
        }
        catch (Exception localException5)
        {
          Object localObject2;
          Object localObject4;
          FileLog.e(localException5);
          localObject8 = localObject4;
          localObject12 = localObject14;
          localObject13 = localObject15;
          localObject9 = localObject6;
          localObject10 = localObject7;
          localObject11 = localObject2;
          String str1 = paramString2;
        }
        this.seekToTime = Utilities.parseInt(paramString2).intValue();
      }
      break;
      paramString2 = null;
      break label224;
      paramPhoto = null;
      break label232;
    }
    label985:
    if (paramPhoto != null)
    {
      paramString1 = new YoutubeVideoTask(paramPhoto);
      paramString1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
      this.currentTask = paramString1;
    }
    for (;;)
    {
      this.controlsView.show(false, false);
      showProgress(true, false);
      break;
      if (localObject11 != null)
      {
        paramString1 = new VimeoVideoTask((String)localObject11);
        paramString1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
        this.currentTask = paramString1;
      }
      else if (localObject12 != null)
      {
        paramString1 = new CoubVideoTask((String)localObject12);
        paramString1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
        this.currentTask = paramString1;
        this.isStream = true;
      }
      else if (localObject8 != null)
      {
        paramString1 = new AparatVideoTask((String)localObject8);
        paramString1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
        this.currentTask = paramString1;
      }
      else if (localObject9 != null)
      {
        paramString1 = new TwitchClipVideoTask(paramString1, (String)localObject9);
        paramString1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
        this.currentTask = paramString1;
      }
      else if (localObject10 != null)
      {
        paramString1 = new TwitchStreamVideoTask(paramString1, (String)localObject10);
        paramString1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
        this.currentTask = paramString1;
        this.isStream = true;
      }
    }
    label1298:
    this.controlsView.setVisibility(8);
    return false;
  }
  
  public void onAudioFocusChange(int paramInt)
  {
    if (paramInt == -1)
    {
      if (this.videoPlayer.isPlaying())
      {
        this.videoPlayer.pause();
        updatePlayButton();
      }
      this.hasAudioFocus = false;
      this.audioFocus = 0;
    }
    do
    {
      do
      {
        do
        {
          return;
          if (paramInt != 1) {
            break;
          }
          this.audioFocus = 2;
        } while (!this.resumeAudioOnFocusGain);
        this.resumeAudioOnFocusGain = false;
        this.videoPlayer.play();
        return;
        if (paramInt == -3)
        {
          this.audioFocus = 1;
          return;
        }
      } while (paramInt != -2);
      this.audioFocus = 0;
    } while (!this.videoPlayer.isPlaying());
    this.resumeAudioOnFocusGain = true;
    this.videoPlayer.pause();
    updatePlayButton();
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    paramCanvas.drawRect(0.0F, 0.0F, getMeasuredWidth(), getMeasuredHeight() - AndroidUtilities.dp(10.0F), this.backgroundPaint);
  }
  
  public void onError(Exception paramException)
  {
    FileLog.e(paramException);
    onInitFailed();
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = (paramInt3 - paramInt1 - this.aspectRatioFrameLayout.getMeasuredWidth()) / 2;
    int j = (paramInt4 - paramInt2 - AndroidUtilities.dp(10.0F) - this.aspectRatioFrameLayout.getMeasuredHeight()) / 2;
    this.aspectRatioFrameLayout.layout(i, j, this.aspectRatioFrameLayout.getMeasuredWidth() + i, this.aspectRatioFrameLayout.getMeasuredHeight() + j);
    if (this.controlsView.getParent() == this) {
      this.controlsView.layout(0, 0, this.controlsView.getMeasuredWidth(), this.controlsView.getMeasuredHeight());
    }
    paramInt1 = (paramInt3 - paramInt1 - this.progressView.getMeasuredWidth()) / 2;
    paramInt2 = (paramInt4 - paramInt2 - this.progressView.getMeasuredHeight()) / 2;
    this.progressView.layout(paramInt1, paramInt2, this.progressView.getMeasuredWidth() + paramInt1, this.progressView.getMeasuredHeight() + paramInt2);
    this.controlsView.imageReceiver.setImageCoords(0, 0, getMeasuredWidth(), getMeasuredHeight() - AndroidUtilities.dp(10.0F));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt1 = View.MeasureSpec.getSize(paramInt1);
    paramInt2 = View.MeasureSpec.getSize(paramInt2);
    this.aspectRatioFrameLayout.measure(View.MeasureSpec.makeMeasureSpec(paramInt1, 1073741824), View.MeasureSpec.makeMeasureSpec(paramInt2 - AndroidUtilities.dp(10.0F), 1073741824));
    if (this.controlsView.getParent() == this) {
      this.controlsView.measure(View.MeasureSpec.makeMeasureSpec(paramInt1, 1073741824), View.MeasureSpec.makeMeasureSpec(paramInt2, 1073741824));
    }
    this.progressView.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(44.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(44.0F), 1073741824));
    setMeasuredDimension(paramInt1, paramInt2);
  }
  
  public void onRenderedFirstFrame()
  {
    this.firstFrameRendered = true;
    this.lastUpdateTime = System.currentTimeMillis();
    this.controlsView.invalidate();
  }
  
  public void onStateChanged(boolean paramBoolean, int paramInt)
  {
    if (paramInt != 2)
    {
      if (this.videoPlayer.getDuration() != -9223372036854775807L) {
        this.controlsView.setDuration((int)(this.videoPlayer.getDuration() / 1000L));
      }
    }
    else
    {
      if ((paramInt == 4) || (paramInt == 1) || (!this.videoPlayer.isPlaying())) {
        break label100;
      }
      this.delegate.onPlayStateChanged(this, true);
      label69:
      if ((!this.videoPlayer.isPlaying()) || (paramInt == 4)) {
        break label114;
      }
      updatePlayButton();
    }
    label100:
    label114:
    while (paramInt != 4)
    {
      return;
      this.controlsView.setDuration(0);
      break;
      this.delegate.onPlayStateChanged(this, false);
      break label69;
    }
    this.isCompleted = true;
    this.videoPlayer.pause();
    this.videoPlayer.seekTo(0L);
    updatePlayButton();
    this.controlsView.show(true, true);
  }
  
  public boolean onSurfaceDestroyed(SurfaceTexture paramSurfaceTexture)
  {
    if (this.changingTextureView)
    {
      this.changingTextureView = false;
      if ((this.inFullscreen) || (this.isInline))
      {
        if (this.isInline) {
          this.waitingForFirstTextureUpload = 1;
        }
        this.changedTextureView.setSurfaceTexture(paramSurfaceTexture);
        this.changedTextureView.setSurfaceTextureListener(this.surfaceTextureListener);
        this.changedTextureView.setVisibility(0);
        return true;
      }
    }
    return false;
  }
  
  public void onSurfaceTextureUpdated(SurfaceTexture paramSurfaceTexture)
  {
    if (this.waitingForFirstTextureUpload == 2)
    {
      if (this.textureImageView != null)
      {
        this.textureImageView.setVisibility(4);
        this.textureImageView.setImageDrawable(null);
        if (this.currentBitmap != null)
        {
          this.currentBitmap.recycle();
          this.currentBitmap = null;
        }
      }
      this.switchingInlineMode = false;
      this.delegate.onSwitchInlineMode(this.controlsView, false, this.aspectRatioFrameLayout.getAspectRatio(), this.aspectRatioFrameLayout.getVideoRotation(), this.allowInlineAnimation);
      this.waitingForFirstTextureUpload = 0;
    }
  }
  
  public void onVideoSizeChanged(int paramInt1, int paramInt2, int paramInt3, float paramFloat)
  {
    int j;
    int i;
    if (this.aspectRatioFrameLayout != null)
    {
      if (paramInt3 != 90)
      {
        j = paramInt1;
        i = paramInt2;
        if (paramInt3 != 270) {}
      }
      else
      {
        i = paramInt1;
        j = paramInt2;
      }
      if (i != 0) {
        break label70;
      }
    }
    label70:
    for (paramFloat = 1.0F;; paramFloat = j * paramFloat / i)
    {
      this.aspectRatioFrameLayout.setAspectRatio(paramFloat, paramInt3);
      if (this.inFullscreen) {
        this.delegate.onVideoSizeChanged(paramFloat, paramInt3);
      }
      return;
    }
  }
  
  public void pause()
  {
    this.videoPlayer.pause();
    updatePlayButton();
    this.controlsView.show(true, true);
  }
  
  public void updateTextureImageView()
  {
    if (this.textureImageView == null) {
      return;
    }
    try
    {
      this.currentBitmap = Bitmaps.createBitmap(this.textureView.getWidth(), this.textureView.getHeight(), Bitmap.Config.ARGB_8888);
      this.changedTextureView.getBitmap(this.currentBitmap);
      if (this.currentBitmap != null)
      {
        this.textureImageView.setVisibility(0);
        this.textureImageView.setImageBitmap(this.currentBitmap);
        return;
      }
    }
    catch (Throwable localThrowable)
    {
      for (;;)
      {
        if (this.currentBitmap != null)
        {
          this.currentBitmap.recycle();
          this.currentBitmap = null;
        }
        FileLog.e(localThrowable);
      }
      this.textureImageView.setImageDrawable(null);
    }
  }
  
  private class AparatVideoTask
    extends AsyncTask<Void, Void, String>
  {
    private boolean canRetry = true;
    private String[] results = new String[2];
    private String videoId;
    
    public AparatVideoTask(String paramString)
    {
      this.videoId = paramString;
    }
    
    protected String doInBackground(Void... paramVarArgs)
    {
      paramVarArgs = WebPlayerView.this.downloadUrlContent(this, String.format(Locale.US, "http://www.aparat.com/video/video/embed/vt/frame/showvideo/yes/videohash/%s", new Object[] { this.videoId }));
      if (isCancelled()) {
        return null;
      }
      for (;;)
      {
        int i;
        try
        {
          paramVarArgs = WebPlayerView.aparatFileListPattern.matcher(paramVarArgs);
          if (paramVarArgs.find())
          {
            paramVarArgs = new JSONArray(paramVarArgs.group(1));
            i = 0;
            if (i < paramVarArgs.length())
            {
              Object localObject = paramVarArgs.getJSONArray(i);
              if (((JSONArray)localObject).length() == 0) {
                break label150;
              }
              localObject = ((JSONArray)localObject).getJSONObject(0);
              if (!((JSONObject)localObject).has("file")) {
                break label150;
              }
              this.results[0] = ((JSONObject)localObject).getString("file");
              this.results[1] = "other";
            }
          }
        }
        catch (Exception paramVarArgs)
        {
          FileLog.e(paramVarArgs);
          if (isCancelled()) {
            return null;
          }
          return this.results[0];
        }
        label150:
        i += 1;
      }
    }
    
    protected void onPostExecute(String paramString)
    {
      if (paramString != null)
      {
        WebPlayerView.access$1702(WebPlayerView.this, true);
        WebPlayerView.access$1802(WebPlayerView.this, paramString);
        WebPlayerView.access$1902(WebPlayerView.this, this.results[1]);
        if (WebPlayerView.this.isAutoplay) {
          WebPlayerView.this.preparePlayer();
        }
        WebPlayerView.this.showProgress(false, true);
        WebPlayerView.this.controlsView.show(true, true);
      }
      while (isCancelled()) {
        return;
      }
      WebPlayerView.this.onInitFailed();
    }
  }
  
  public static abstract interface CallJavaResultInterface
  {
    public abstract void jsCallFinished(String paramString);
  }
  
  private class ControlsView
    extends FrameLayout
  {
    private int bufferedPosition;
    private AnimatorSet currentAnimation;
    private int currentProgressX;
    private int duration;
    private StaticLayout durationLayout;
    private int durationWidth;
    private Runnable hideRunnable = new Runnable()
    {
      public void run()
      {
        WebPlayerView.ControlsView.this.show(false, true);
      }
    };
    private ImageReceiver imageReceiver;
    private boolean isVisible = true;
    private int lastProgressX;
    private int progress;
    private Paint progressBufferedPaint;
    private Paint progressInnerPaint;
    private StaticLayout progressLayout;
    private Paint progressPaint;
    private boolean progressPressed;
    private TextPaint textPaint;
    
    public ControlsView(Context paramContext)
    {
      super();
      setWillNotDraw(false);
      this.textPaint = new TextPaint(1);
      this.textPaint.setColor(-1);
      this.textPaint.setTextSize(AndroidUtilities.dp(12.0F));
      this.progressPaint = new Paint(1);
      this.progressPaint.setColor(-15095832);
      this.progressInnerPaint = new Paint();
      this.progressInnerPaint.setColor(-6975081);
      this.progressBufferedPaint = new Paint(1);
      this.progressBufferedPaint.setColor(-1);
      this.imageReceiver = new ImageReceiver(this);
    }
    
    private void checkNeedHide()
    {
      AndroidUtilities.cancelRunOnUIThread(this.hideRunnable);
      if ((this.isVisible) && (WebPlayerView.this.videoPlayer.isPlaying())) {
        AndroidUtilities.runOnUIThread(this.hideRunnable, 3000L);
      }
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      if (WebPlayerView.this.drawImage)
      {
        if ((WebPlayerView.this.firstFrameRendered) && (WebPlayerView.this.currentAlpha != 0.0F))
        {
          long l1 = System.currentTimeMillis();
          long l2 = WebPlayerView.this.lastUpdateTime;
          WebPlayerView.access$4902(WebPlayerView.this, l1);
          WebPlayerView.access$4802(WebPlayerView.this, WebPlayerView.this.currentAlpha - (float)(l1 - l2) / 150.0F);
          if (WebPlayerView.this.currentAlpha < 0.0F) {
            WebPlayerView.access$4802(WebPlayerView.this, 0.0F);
          }
          invalidate();
        }
        this.imageReceiver.setAlpha(WebPlayerView.this.currentAlpha);
        this.imageReceiver.draw(paramCanvas);
      }
      int i;
      int n;
      int j;
      label278:
      int m;
      int k;
      label356:
      label405:
      float f2;
      float f3;
      Paint localPaint;
      if ((WebPlayerView.this.videoPlayer.isPlayerPrepared()) && (!WebPlayerView.this.isStream))
      {
        i = getMeasuredWidth();
        n = getMeasuredHeight();
        if (!WebPlayerView.this.isInline)
        {
          if (this.durationLayout != null)
          {
            paramCanvas.save();
            f1 = i - AndroidUtilities.dp(58.0F) - this.durationWidth;
            if (!WebPlayerView.this.inFullscreen) {
              break label570;
            }
            j = 6;
            paramCanvas.translate(f1, n - AndroidUtilities.dp(j + 29));
            this.durationLayout.draw(paramCanvas);
            paramCanvas.restore();
          }
          if (this.progressLayout != null)
          {
            paramCanvas.save();
            f1 = AndroidUtilities.dp(18.0F);
            if (!WebPlayerView.this.inFullscreen) {
              break label577;
            }
            j = 6;
            paramCanvas.translate(f1, n - AndroidUtilities.dp(j + 29));
            this.progressLayout.draw(paramCanvas);
            paramCanvas.restore();
          }
        }
        if (this.duration != 0)
        {
          if (!WebPlayerView.this.isInline) {
            break label584;
          }
          m = n - AndroidUtilities.dp(3.0F);
          k = 0;
          n -= AndroidUtilities.dp(7.0F);
          j = i;
          i = n;
          if (WebPlayerView.this.inFullscreen) {
            paramCanvas.drawRect(k, m, j, AndroidUtilities.dp(3.0F) + m, this.progressInnerPaint);
          }
          if (!this.progressPressed) {
            break label674;
          }
          n = this.currentProgressX;
          if ((this.bufferedPosition != 0) && (this.duration != 0))
          {
            f1 = k;
            f2 = m;
            f3 = k;
            float f4 = j - k;
            float f5 = this.bufferedPosition / this.duration;
            float f6 = AndroidUtilities.dp(3.0F) + m;
            if (!WebPlayerView.this.inFullscreen) {
              break label701;
            }
            localPaint = this.progressBufferedPaint;
            label480:
            paramCanvas.drawRect(f1, f2, f4 * f5 + f3, f6, localPaint);
          }
          paramCanvas.drawRect(k, m, n, AndroidUtilities.dp(3.0F) + m, this.progressPaint);
          if (!WebPlayerView.this.isInline)
          {
            f2 = n;
            f3 = i;
            if (!this.progressPressed) {
              break label710;
            }
          }
        }
      }
      label570:
      label577:
      label584:
      label674:
      label701:
      label710:
      for (float f1 = 7.0F;; f1 = 5.0F)
      {
        paramCanvas.drawCircle(f2, f3, AndroidUtilities.dp(f1), this.progressPaint);
        return;
        j = 10;
        break;
        j = 10;
        break label278;
        if (WebPlayerView.this.inFullscreen)
        {
          m = n - AndroidUtilities.dp(29.0F);
          k = AndroidUtilities.dp(36.0F) + this.durationWidth;
          j = i - AndroidUtilities.dp(76.0F) - this.durationWidth;
          i = n - AndroidUtilities.dp(28.0F);
          break label356;
        }
        m = n - AndroidUtilities.dp(13.0F);
        k = 0;
        j = i;
        i = n - AndroidUtilities.dp(12.0F);
        break label356;
        n = k + (int)((j - k) * (this.progress / this.duration));
        break label405;
        localPaint = this.progressInnerPaint;
        break label480;
      }
    }
    
    public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
    {
      if (paramMotionEvent.getAction() == 0)
      {
        if (!this.isVisible)
        {
          show(true, true);
          return true;
        }
        onTouchEvent(paramMotionEvent);
        return this.progressPressed;
      }
      return super.onInterceptTouchEvent(paramMotionEvent);
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent)
    {
      int k;
      int j;
      int m;
      if (WebPlayerView.this.inFullscreen)
      {
        k = AndroidUtilities.dp(36.0F) + this.durationWidth;
        j = getMeasuredWidth() - AndroidUtilities.dp(76.0F) - this.durationWidth;
        i = getMeasuredHeight() - AndroidUtilities.dp(28.0F);
        if (this.duration == 0) {
          break label256;
        }
        m = (int)((j - k) * (this.progress / this.duration));
        label76:
        m = k + m;
        if (paramMotionEvent.getAction() != 0) {
          break label271;
        }
        if ((!this.isVisible) || (WebPlayerView.this.isInline) || (WebPlayerView.this.isStream)) {
          break label262;
        }
        if (this.duration != 0)
        {
          j = (int)paramMotionEvent.getX();
          k = (int)paramMotionEvent.getY();
          if ((j >= m - AndroidUtilities.dp(10.0F)) && (j <= AndroidUtilities.dp(10.0F) + m) && (k >= i - AndroidUtilities.dp(10.0F)) && (k <= AndroidUtilities.dp(10.0F) + i))
          {
            this.progressPressed = true;
            this.lastProgressX = j;
            this.currentProgressX = m;
            getParent().requestDisallowInterceptTouchEvent(true);
            invalidate();
          }
        }
        label219:
        AndroidUtilities.cancelRunOnUIThread(this.hideRunnable);
      }
      label256:
      label262:
      label271:
      label389:
      do
      {
        for (;;)
        {
          super.onTouchEvent(paramMotionEvent);
          return true;
          k = 0;
          j = getMeasuredWidth();
          i = getMeasuredHeight() - AndroidUtilities.dp(12.0F);
          break;
          m = 0;
          break label76;
          show(true, true);
          break label219;
          if ((paramMotionEvent.getAction() != 1) && (paramMotionEvent.getAction() != 3)) {
            break label389;
          }
          if ((WebPlayerView.this.initied) && (WebPlayerView.this.videoPlayer.isPlaying())) {
            AndroidUtilities.runOnUIThread(this.hideRunnable, 3000L);
          }
          if (this.progressPressed)
          {
            this.progressPressed = false;
            if (WebPlayerView.this.initied)
            {
              this.progress = ((int)(this.duration * ((this.currentProgressX - k) / (j - k))));
              WebPlayerView.this.videoPlayer.seekTo(this.progress * 1000L);
            }
          }
        }
      } while ((paramMotionEvent.getAction() != 2) || (!this.progressPressed));
      int i = (int)paramMotionEvent.getX();
      this.currentProgressX -= this.lastProgressX - i;
      this.lastProgressX = i;
      if (this.currentProgressX < k) {
        this.currentProgressX = k;
      }
      for (;;)
      {
        setProgress((int)(this.duration * 1000 * ((this.currentProgressX - k) / (j - k))));
        invalidate();
        break;
        if (this.currentProgressX > j) {
          this.currentProgressX = j;
        }
      }
    }
    
    public void requestDisallowInterceptTouchEvent(boolean paramBoolean)
    {
      super.requestDisallowInterceptTouchEvent(paramBoolean);
      checkNeedHide();
    }
    
    public void setBufferedProgress(int paramInt)
    {
      this.bufferedPosition = paramInt;
      invalidate();
    }
    
    public void setDuration(int paramInt)
    {
      if ((this.duration == paramInt) || (paramInt < 0) || (WebPlayerView.this.isStream)) {
        return;
      }
      this.duration = paramInt;
      this.durationLayout = new StaticLayout(String.format(Locale.US, "%d:%02d", new Object[] { Integer.valueOf(this.duration / 60), Integer.valueOf(this.duration % 60) }), this.textPaint, AndroidUtilities.dp(1000.0F), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      if (this.durationLayout.getLineCount() > 0) {
        this.durationWidth = ((int)Math.ceil(this.durationLayout.getLineWidth(0)));
      }
      invalidate();
    }
    
    public void setProgress(int paramInt)
    {
      if ((this.progressPressed) || (paramInt < 0) || (WebPlayerView.this.isStream)) {
        return;
      }
      this.progress = paramInt;
      this.progressLayout = new StaticLayout(String.format(Locale.US, "%d:%02d", new Object[] { Integer.valueOf(this.progress / 60), Integer.valueOf(this.progress % 60) }), this.textPaint, AndroidUtilities.dp(1000.0F), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      invalidate();
    }
    
    public void show(boolean paramBoolean1, boolean paramBoolean2)
    {
      if (this.isVisible == paramBoolean1) {
        return;
      }
      this.isVisible = paramBoolean1;
      if (this.currentAnimation != null) {
        this.currentAnimation.cancel();
      }
      if (this.isVisible) {
        if (paramBoolean2)
        {
          this.currentAnimation = new AnimatorSet();
          this.currentAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "alpha", new float[] { 1.0F }) });
          this.currentAnimation.setDuration(150L);
          this.currentAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              WebPlayerView.ControlsView.access$4402(WebPlayerView.ControlsView.this, null);
            }
          });
          this.currentAnimation.start();
        }
      }
      for (;;)
      {
        checkNeedHide();
        return;
        setAlpha(1.0F);
        continue;
        if (paramBoolean2)
        {
          this.currentAnimation = new AnimatorSet();
          this.currentAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "alpha", new float[] { 0.0F }) });
          this.currentAnimation.setDuration(150L);
          this.currentAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              WebPlayerView.ControlsView.access$4402(WebPlayerView.ControlsView.this, null);
            }
          });
          this.currentAnimation.start();
        }
        else
        {
          setAlpha(0.0F);
        }
      }
    }
  }
  
  private class CoubVideoTask
    extends AsyncTask<Void, Void, String>
  {
    private boolean canRetry = true;
    private String[] results = new String[4];
    private String videoId;
    
    public CoubVideoTask(String paramString)
    {
      this.videoId = paramString;
    }
    
    private String decodeUrl(String paramString)
    {
      paramString = new StringBuilder(paramString);
      int i = 0;
      while (i < paramString.length())
      {
        char c3 = paramString.charAt(i);
        char c2 = Character.toLowerCase(c3);
        char c1 = c2;
        if (c3 == c2) {
          c1 = Character.toUpperCase(c3);
        }
        paramString.setCharAt(i, c1);
        i += 1;
      }
      try
      {
        paramString = new String(Base64.decode(paramString.toString(), 0), "UTF-8");
        return paramString;
      }
      catch (Exception paramString) {}
      return null;
    }
    
    protected String doInBackground(Void... paramVarArgs)
    {
      paramVarArgs = WebPlayerView.this.downloadUrlContent(this, String.format(Locale.US, "https://coub.com/api/v2/coubs/%s.json", new Object[] { this.videoId }));
      if (isCancelled()) {}
      do
      {
        return null;
        try
        {
          Object localObject = new JSONObject(paramVarArgs).getJSONObject("file_versions").getJSONObject("mobile");
          paramVarArgs = decodeUrl(((JSONObject)localObject).getString("gifv"));
          localObject = ((JSONObject)localObject).getJSONArray("audio").getString(0);
          if ((paramVarArgs != null) && (localObject != null))
          {
            this.results[0] = paramVarArgs;
            this.results[1] = "other";
            this.results[2] = localObject;
            this.results[3] = "other";
          }
        }
        catch (Exception paramVarArgs)
        {
          for (;;)
          {
            FileLog.e(paramVarArgs);
          }
        }
      } while (isCancelled());
      return this.results[0];
    }
    
    protected void onPostExecute(String paramString)
    {
      if (paramString != null)
      {
        WebPlayerView.access$1702(WebPlayerView.this, true);
        WebPlayerView.access$1802(WebPlayerView.this, paramString);
        WebPlayerView.access$1902(WebPlayerView.this, this.results[1]);
        WebPlayerView.access$2702(WebPlayerView.this, this.results[2]);
        WebPlayerView.access$2802(WebPlayerView.this, this.results[3]);
        if (WebPlayerView.this.isAutoplay) {
          WebPlayerView.this.preparePlayer();
        }
        WebPlayerView.this.showProgress(false, true);
        WebPlayerView.this.controlsView.show(true, true);
      }
      while (isCancelled()) {
        return;
      }
      WebPlayerView.this.onInitFailed();
    }
  }
  
  private class JSExtractor
  {
    private String[] assign_operators = { "|=", "^=", "&=", ">>=", "<<=", "-=", "+=", "%=", "/=", "*=", "=" };
    ArrayList<String> codeLines = new ArrayList();
    private String jsCode;
    private String[] operators = { "|", "^", "&", ">>", "<<", "-", "+", "%", "/", "*" };
    
    public JSExtractor(String paramString)
    {
      this.jsCode = paramString;
    }
    
    private void buildFunction(String[] paramArrayOfString, String paramString)
      throws Exception
    {
      HashMap localHashMap = new HashMap();
      int i = 0;
      while (i < paramArrayOfString.length)
      {
        localHashMap.put(paramArrayOfString[i], "");
        i += 1;
      }
      paramArrayOfString = paramString.split(";");
      paramString = new boolean[1];
      i = 0;
      for (;;)
      {
        if (i < paramArrayOfString.length)
        {
          interpretStatement(paramArrayOfString[i], localHashMap, paramString, 100);
          if (paramString[0] == 0) {}
        }
        else
        {
          return;
        }
        i += 1;
      }
    }
    
    private String extractFunction(String paramString)
      throws Exception
    {
      try
      {
        paramString = Pattern.quote(paramString);
        paramString = Pattern.compile(String.format(Locale.US, "(?x)(?:function\\s+%s|[{;,]\\s*%s\\s*=\\s*function|var\\s+%s\\s*=\\s*function)\\s*\\(([^)]*)\\)\\s*\\{([^}]+)\\}", new Object[] { paramString, paramString, paramString })).matcher(this.jsCode);
        if (paramString.find())
        {
          String str = paramString.group();
          if (!this.codeLines.contains(str)) {
            this.codeLines.add(str + ";");
          }
          buildFunction(paramString.group(1).split(","), paramString.group(2));
        }
      }
      catch (Exception paramString)
      {
        for (;;)
        {
          this.codeLines.clear();
          FileLog.e(paramString);
        }
      }
      return TextUtils.join("", this.codeLines);
    }
    
    private HashMap<String, Object> extractObject(String paramString)
      throws Exception
    {
      HashMap localHashMap = new HashMap();
      Matcher localMatcher = Pattern.compile(String.format(Locale.US, "(?:var\\s+)?%s\\s*=\\s*\\{\\s*((%s\\s*:\\s*function\\(.*?\\)\\s*\\{.*?\\}(?:,\\s*)?)*)\\}\\s*;", new Object[] { Pattern.quote(paramString), "(?:[a-zA-Z$0-9]+|\"[a-zA-Z$0-9]+\"|'[a-zA-Z$0-9]+')" })).matcher(this.jsCode);
      paramString = null;
      while (localMatcher.find())
      {
        String str2 = localMatcher.group();
        String str1 = localMatcher.group(2);
        paramString = str1;
        if (!TextUtils.isEmpty(str1))
        {
          paramString = str1;
          if (!this.codeLines.contains(str2))
          {
            this.codeLines.add(localMatcher.group());
            paramString = str1;
          }
        }
      }
      paramString = Pattern.compile(String.format("(%s)\\s*:\\s*function\\(([a-z,]+)\\)\\{([^}]+)\\}", new Object[] { "(?:[a-zA-Z$0-9]+|\"[a-zA-Z$0-9]+\"|'[a-zA-Z$0-9]+')" })).matcher(paramString);
      while (paramString.find()) {
        buildFunction(paramString.group(2).split(","), paramString.group(3));
      }
      return localHashMap;
    }
    
    private void interpretExpression(String paramString, HashMap<String, String> paramHashMap, int paramInt)
      throws Exception
    {
      Object localObject2 = paramString.trim();
      if (TextUtils.isEmpty((CharSequence)localObject2)) {}
      for (;;)
      {
        return;
        Object localObject1 = localObject2;
        if (((String)localObject2).charAt(0) == '(')
        {
          i = 0;
          localObject1 = WebPlayerView.exprParensPattern.matcher((CharSequence)localObject2);
          int j;
          do
          {
            for (;;)
            {
              j = i;
              paramString = (String)localObject2;
              if (!((Matcher)localObject1).find()) {
                break label136;
              }
              if (((Matcher)localObject1).group(0).indexOf('0') != 40) {
                break;
              }
              i += 1;
            }
            j = i - 1;
            i = j;
          } while (j != 0);
          interpretExpression(((String)localObject2).substring(1, ((Matcher)localObject1).start()), paramHashMap, paramInt);
          paramString = ((String)localObject2).substring(((Matcher)localObject1).end()).trim();
          if (TextUtils.isEmpty(paramString)) {
            continue;
          }
          label136:
          localObject1 = paramString;
          if (j != 0) {
            throw new Exception(String.format("Premature end of parens in %s", new Object[] { paramString }));
          }
        }
        int i = 0;
        while (i < this.assign_operators.length)
        {
          paramString = this.assign_operators[i];
          paramString = Pattern.compile(String.format(Locale.US, "(?x)(%s)(?:\\[([^\\]]+?)\\])?\\s*%s(.*)$", new Object[] { "[a-zA-Z_$][a-zA-Z_$0-9]*", Pattern.quote(paramString) })).matcher((CharSequence)localObject1);
          if (!paramString.find())
          {
            i += 1;
          }
          else
          {
            interpretExpression(paramString.group(3), paramHashMap, paramInt - 1);
            localObject1 = paramString.group(2);
            if (!TextUtils.isEmpty((CharSequence)localObject1))
            {
              interpretExpression((String)localObject1, paramHashMap, paramInt);
              return;
            }
            paramHashMap.put(paramString.group(1), "");
            return;
          }
        }
        try
        {
          Integer.parseInt((String)localObject1);
          return;
        }
        catch (Exception paramString) {}
        if ((Pattern.compile(String.format(Locale.US, "(?!if|return|true|false)(%s)$", tmp302_299)).matcher((CharSequence)localObject1).find()) || ((((String)localObject1).charAt(0) == '"') && (((String)localObject1).charAt(((String)localObject1).length() - 1) == '"'))) {
          continue;
        }
        try
        {
          new JSONObject((String)localObject1).toString();
          return;
        }
        catch (Exception paramString)
        {
          paramString = Pattern.compile(String.format(Locale.US, "(%s)\\[(.+)\\]$", new Object[] { "[a-zA-Z_$][a-zA-Z_$0-9]*" })).matcher((CharSequence)localObject1);
          if (paramString.find())
          {
            paramString.group(1);
            interpretExpression(paramString.group(2), paramHashMap, paramInt - 1);
            return;
          }
          Matcher localMatcher = Pattern.compile(String.format(Locale.US, "(%s)(?:\\.([^(]+)|\\[([^]]+)\\])\\s*(?:\\(+([^()]*)\\))?$", new Object[] { "[a-zA-Z_$][a-zA-Z_$0-9]*" })).matcher((CharSequence)localObject1);
          Object localObject3;
          if (localMatcher.find())
          {
            localObject3 = localMatcher.group(1);
            paramString = localMatcher.group(2);
            localObject2 = localMatcher.group(3);
            if (TextUtils.isEmpty(paramString)) {
              paramString = (String)localObject2;
            }
            for (;;)
            {
              paramString.replace("\"", "");
              paramString = localMatcher.group(4);
              if (paramHashMap.get(localObject3) == null) {
                extractObject((String)localObject3);
              }
              if (paramString == null) {
                break;
              }
              if (((String)localObject1).charAt(((String)localObject1).length() - 1) == ')') {
                break label558;
              }
              throw new Exception("last char not ')'");
            }
            label558:
            if (paramString.length() == 0) {
              continue;
            }
            paramString = paramString.split(",");
            i = 0;
            while (i < paramString.length)
            {
              interpretExpression(paramString[i], paramHashMap, paramInt);
              i += 1;
            }
          }
          paramString = Pattern.compile(String.format(Locale.US, "(%s)\\[(.+)\\]$", new Object[] { "[a-zA-Z_$][a-zA-Z_$0-9]*" })).matcher((CharSequence)localObject1);
          if (paramString.find())
          {
            paramHashMap.get(paramString.group(1));
            interpretExpression(paramString.group(2), paramHashMap, paramInt - 1);
            return;
          }
          i = 0;
          if (i < this.operators.length)
          {
            paramString = this.operators[i];
            localObject2 = Pattern.compile(String.format(Locale.US, "(.+?)%s(.+)", new Object[] { Pattern.quote(paramString) })).matcher((CharSequence)localObject1);
            if (!((Matcher)localObject2).find()) {}
            do
            {
              i += 1;
              break;
              localObject3 = new boolean[1];
              interpretStatement(((Matcher)localObject2).group(1), paramHashMap, (boolean[])localObject3, paramInt - 1);
              if (localObject3[0] != 0) {
                throw new Exception(String.format("Premature left-side return of %s in %s", new Object[] { paramString, localObject1 }));
              }
              interpretStatement(((Matcher)localObject2).group(2), paramHashMap, (boolean[])localObject3, paramInt - 1);
            } while (localObject3[0] == 0);
            throw new Exception(String.format("Premature right-side return of %s in %s", new Object[] { paramString, localObject1 }));
          }
          paramString = Pattern.compile(String.format(Locale.US, "^(%s)\\(([a-zA-Z0-9_$,]*)\\)$", new Object[] { "[a-zA-Z_$][a-zA-Z_$0-9]*" })).matcher((CharSequence)localObject1);
          if (paramString.find()) {
            extractFunction(paramString.group(1));
          }
          throw new Exception(String.format("Unsupported JS expression %s", new Object[] { localObject1 }));
        }
      }
    }
    
    private void interpretStatement(String paramString, HashMap<String, String> paramHashMap, boolean[] paramArrayOfBoolean, int paramInt)
      throws Exception
    {
      if (paramInt < 0) {
        throw new Exception("recursion limit reached");
      }
      paramArrayOfBoolean[0] = false;
      paramString = paramString.trim();
      Matcher localMatcher = WebPlayerView.stmtVarPattern.matcher(paramString);
      if (localMatcher.find()) {
        paramString = paramString.substring(localMatcher.group(0).length());
      }
      for (;;)
      {
        interpretExpression(paramString, paramHashMap, paramInt);
        return;
        localMatcher = WebPlayerView.stmtReturnPattern.matcher(paramString);
        if (localMatcher.find())
        {
          paramString = paramString.substring(localMatcher.group(0).length());
          paramArrayOfBoolean[0] = true;
        }
      }
    }
  }
  
  public class JavaScriptInterface
  {
    private final WebPlayerView.CallJavaResultInterface callJavaResultInterface;
    
    public JavaScriptInterface(WebPlayerView.CallJavaResultInterface paramCallJavaResultInterface)
    {
      this.callJavaResultInterface = paramCallJavaResultInterface;
    }
    
    @JavascriptInterface
    public void returnResultToJava(String paramString)
    {
      this.callJavaResultInterface.jsCallFinished(paramString);
    }
  }
  
  private class TwitchClipVideoTask
    extends AsyncTask<Void, Void, String>
  {
    private boolean canRetry = true;
    private String currentUrl;
    private String[] results = new String[2];
    private String videoId;
    
    public TwitchClipVideoTask(String paramString1, String paramString2)
    {
      this.videoId = paramString2;
      this.currentUrl = paramString1;
    }
    
    protected String doInBackground(Void... paramVarArgs)
    {
      paramVarArgs = WebPlayerView.this.downloadUrlContent(this, this.currentUrl, null, false);
      if (isCancelled()) {}
      for (;;)
      {
        return null;
        try
        {
          paramVarArgs = WebPlayerView.twitchClipFilePattern.matcher(paramVarArgs);
          if (paramVarArgs.find())
          {
            paramVarArgs = new JSONObject(paramVarArgs.group(1)).getJSONArray("quality_options").getJSONObject(0);
            this.results[0] = paramVarArgs.getString("source");
            this.results[1] = "other";
          }
          if (isCancelled()) {
            continue;
          }
          return this.results[0];
        }
        catch (Exception paramVarArgs)
        {
          for (;;)
          {
            FileLog.e(paramVarArgs);
          }
        }
      }
    }
    
    protected void onPostExecute(String paramString)
    {
      if (paramString != null)
      {
        WebPlayerView.access$1702(WebPlayerView.this, true);
        WebPlayerView.access$1802(WebPlayerView.this, paramString);
        WebPlayerView.access$1902(WebPlayerView.this, this.results[1]);
        if (WebPlayerView.this.isAutoplay) {
          WebPlayerView.this.preparePlayer();
        }
        WebPlayerView.this.showProgress(false, true);
        WebPlayerView.this.controlsView.show(true, true);
      }
      while (isCancelled()) {
        return;
      }
      WebPlayerView.this.onInitFailed();
    }
  }
  
  private class TwitchStreamVideoTask
    extends AsyncTask<Void, Void, String>
  {
    private boolean canRetry = true;
    private String currentUrl;
    private String[] results = new String[2];
    private String videoId;
    
    public TwitchStreamVideoTask(String paramString1, String paramString2)
    {
      this.videoId = paramString2;
      this.currentUrl = paramString1;
    }
    
    protected String doInBackground(Void... paramVarArgs)
    {
      paramVarArgs = new HashMap();
      paramVarArgs.put("Client-ID", "jzkbprff40iqj646a697cyrvl0zt2m6");
      int i = this.videoId.indexOf('&');
      if (i > 0) {
        this.videoId = this.videoId.substring(0, i);
      }
      Object localObject = WebPlayerView.this.downloadUrlContent(this, String.format(Locale.US, "https://api.twitch.tv/kraken/streams/%s?stream_type=all", new Object[] { this.videoId }), paramVarArgs, false);
      if (isCancelled()) {
        return null;
      }
      try
      {
        new JSONObject((String)localObject).getJSONObject("stream");
        localObject = new JSONObject(WebPlayerView.this.downloadUrlContent(this, String.format(Locale.US, "https://api.twitch.tv/api/channels/%s/access_token", new Object[] { this.videoId }), paramVarArgs, false));
        paramVarArgs = URLEncoder.encode(((JSONObject)localObject).getString("sig"), "UTF-8");
        localObject = URLEncoder.encode(((JSONObject)localObject).getString("token"), "UTF-8");
        URLEncoder.encode("https://youtube.googleapis.com/v/" + this.videoId, "UTF-8");
        paramVarArgs = "allow_source=true&allow_audio_only=true&allow_spectre=true&player=twitchweb&segment_preference=4&p=" + (int)(Math.random() * 1.0E7D) + "&sig=" + paramVarArgs + "&token=" + (String)localObject;
        paramVarArgs = String.format(Locale.US, "https://usher.ttvnw.net/api/channel/hls/%s.m3u8?%s", new Object[] { this.videoId, paramVarArgs });
        this.results[0] = paramVarArgs;
        this.results[1] = "hls";
        if (isCancelled()) {
          return null;
        }
      }
      catch (Exception paramVarArgs)
      {
        for (;;)
        {
          FileLog.e(paramVarArgs);
        }
      }
      return this.results[0];
    }
    
    protected void onPostExecute(String paramString)
    {
      if (paramString != null)
      {
        WebPlayerView.access$1702(WebPlayerView.this, true);
        WebPlayerView.access$1802(WebPlayerView.this, paramString);
        WebPlayerView.access$1902(WebPlayerView.this, this.results[1]);
        if (WebPlayerView.this.isAutoplay) {
          WebPlayerView.this.preparePlayer();
        }
        WebPlayerView.this.showProgress(false, true);
        WebPlayerView.this.controlsView.show(true, true);
      }
      while (isCancelled()) {
        return;
      }
      WebPlayerView.this.onInitFailed();
    }
  }
  
  private class VimeoVideoTask
    extends AsyncTask<Void, Void, String>
  {
    private boolean canRetry = true;
    private String[] results = new String[2];
    private String videoId;
    
    public VimeoVideoTask(String paramString)
    {
      this.videoId = paramString;
    }
    
    protected String doInBackground(Void... paramVarArgs)
    {
      paramVarArgs = WebPlayerView.this.downloadUrlContent(this, String.format(Locale.US, "https://player.vimeo.com/video/%s/config", new Object[] { this.videoId }));
      if (isCancelled()) {
        return null;
      }
      try
      {
        paramVarArgs = new JSONObject(paramVarArgs).getJSONObject("request").getJSONObject("files");
        if (!paramVarArgs.has("hls")) {
          break label143;
        }
        paramVarArgs = paramVarArgs.getJSONObject("hls");
      }
      catch (Exception paramVarArgs)
      {
        for (;;)
        {
          label84:
          String str;
          FileLog.e(paramVarArgs);
          continue;
          label143:
          if (paramVarArgs.has("progressive"))
          {
            this.results[1] = "other";
            paramVarArgs = paramVarArgs.getJSONArray("progressive").getJSONObject(0);
            this.results[0] = paramVarArgs.getString("url");
          }
        }
      }
      try
      {
        this.results[0] = paramVarArgs.getString("url");
        this.results[1] = "hls";
        if (isCancelled()) {
          return null;
        }
      }
      catch (Exception localException)
      {
        str = paramVarArgs.getString("default_cdn");
        paramVarArgs = paramVarArgs.getJSONObject("cdns").getJSONObject(str);
        this.results[0] = paramVarArgs.getString("url");
        break label84;
      }
      return this.results[0];
    }
    
    protected void onPostExecute(String paramString)
    {
      if (paramString != null)
      {
        WebPlayerView.access$1702(WebPlayerView.this, true);
        WebPlayerView.access$1802(WebPlayerView.this, paramString);
        WebPlayerView.access$1902(WebPlayerView.this, this.results[1]);
        if (WebPlayerView.this.isAutoplay) {
          WebPlayerView.this.preparePlayer();
        }
        WebPlayerView.this.showProgress(false, true);
        WebPlayerView.this.controlsView.show(true, true);
      }
      while (isCancelled()) {
        return;
      }
      WebPlayerView.this.onInitFailed();
    }
  }
  
  public static abstract interface WebPlayerViewDelegate
  {
    public abstract boolean checkInlinePermissions();
    
    public abstract ViewGroup getTextureViewContainer();
    
    public abstract void onInitFailed();
    
    public abstract void onInlineSurfaceTextureReady();
    
    public abstract void onPlayStateChanged(WebPlayerView paramWebPlayerView, boolean paramBoolean);
    
    public abstract void onSharePressed();
    
    public abstract TextureView onSwitchInlineMode(View paramView, boolean paramBoolean1, float paramFloat, int paramInt, boolean paramBoolean2);
    
    public abstract TextureView onSwitchToFullscreen(View paramView, boolean paramBoolean1, float paramFloat, int paramInt, boolean paramBoolean2);
    
    public abstract void onVideoSizeChanged(float paramFloat, int paramInt);
    
    public abstract void prepareToSwitchInlineMode(boolean paramBoolean1, Runnable paramRunnable, float paramFloat, boolean paramBoolean2);
  }
  
  private class YoutubeVideoTask
    extends AsyncTask<Void, Void, String[]>
  {
    private boolean canRetry = true;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private String[] result = new String[2];
    private String sig;
    private String videoId;
    
    public YoutubeVideoTask(String paramString)
    {
      this.videoId = paramString;
    }
    
    private void onInterfaceResult(String paramString)
    {
      this.result[0] = this.result[0].replace(this.sig, "/signature/" + paramString);
      this.countDownLatch.countDown();
    }
    
    protected String[] doInBackground(final Void... paramVarArgs)
    {
      Object localObject7 = WebPlayerView.this.downloadUrlContent(this, "https://www.youtube.com/embed/" + this.videoId);
      if (isCancelled()) {
        return null;
      }
      paramVarArgs = "video_id=" + this.videoId + "&ps=default&gl=US&hl=en";
      int i;
      int n;
      try
      {
        localObject1 = paramVarArgs + "&eurl=" + URLEncoder.encode(new StringBuilder().append("https://youtube.googleapis.com/v/").append(this.videoId).toString(), "UTF-8");
        paramVarArgs = (Void[])localObject1;
      }
      catch (Exception localException1)
      {
        for (;;)
        {
          Object localObject1;
          String[] arrayOfString1;
          FileLog.e(localException1);
          continue;
          localObject2 = paramVarArgs + "&sts=";
        }
        label337:
        i2 = 0;
        k = 0;
        localObject6 = null;
        localObject4 = null;
        i3 = 0;
        j = 0;
        m = i;
        localObject5 = paramVarArgs;
        if (localObject3 == null) {
          break label1118;
        }
      }
      localObject1 = paramVarArgs;
      if (localObject7 != null)
      {
        localObject1 = WebPlayerView.stsPattern.matcher((CharSequence)localObject7);
        if (((Matcher)localObject1).find()) {
          localObject1 = paramVarArgs + "&sts=" + ((String)localObject7).substring(((Matcher)localObject1).start() + 6, ((Matcher)localObject1).end());
        }
      }
      else
      {
        this.result[1] = "dash";
        i = 0;
        paramVarArgs = null;
        arrayOfString1 = new String[5];
        arrayOfString1[0] = "";
        arrayOfString1[1] = "&el=leanback";
        arrayOfString1[2] = "&el=embedded";
        arrayOfString1[3] = "&el=detailpage";
        arrayOfString1[4] = "&el=vevo";
        n = 0;
        m = i;
        localObject5 = paramVarArgs;
        if (n >= arrayOfString1.length) {
          break label1166;
        }
        localObject3 = WebPlayerView.this.downloadUrlContent(this, "https://www.youtube.com/get_video_info?" + (String)localObject1 + arrayOfString1[n]);
        if (!isCancelled()) {
          break label337;
        }
        return null;
      }
      Object localObject2;
      int k;
      Object localObject4;
      int j;
      String[] arrayOfString2 = ((String)localObject3).split("&");
      int i1 = 0;
      Object localObject3 = paramVarArgs;
      label380:
      int m = i;
      int i2 = k;
      Object localObject6 = localObject4;
      int i3 = j;
      Object localObject5 = localObject3;
      int i4;
      if (i1 < arrayOfString2.length) {
        if (arrayOfString2[i1].startsWith("dashmpd"))
        {
          k = 1;
          localObject6 = arrayOfString2[i1].split("=");
          m = i;
          i2 = k;
          paramVarArgs = (Void[])localObject4;
          i4 = j;
          localObject5 = localObject3;
          if (localObject6.length != 2) {}
        }
      }
      for (;;)
      {
        try
        {
          this.result[0] = URLDecoder.decode(localObject6[1], "UTF-8");
          localObject5 = localObject3;
          i4 = j;
          paramVarArgs = (Void[])localObject4;
          i2 = k;
          m = i;
        }
        catch (Exception paramVarArgs)
        {
          label490:
          FileLog.e(paramVarArgs);
          m = i;
          i2 = k;
          paramVarArgs = (Void[])localObject4;
          i4 = j;
          localObject5 = localObject3;
          continue;
        }
        i1 += 1;
        i = m;
        k = i2;
        localObject4 = paramVarArgs;
        j = i4;
        localObject3 = localObject5;
        break label380;
        if (arrayOfString2[i1].startsWith("url_encoded_fmt_stream_map"))
        {
          localObject6 = arrayOfString2[i1].split("=");
          m = i;
          i2 = k;
          paramVarArgs = (Void[])localObject4;
          i4 = j;
          localObject5 = localObject3;
          if (localObject6.length != 2) {}
        }
        else
        {
          label1118:
          label1166:
          label1588:
          label1629:
          label1792:
          label1875:
          label1888:
          label2013:
          do
          {
            for (;;)
            {
              try
              {
                String[] arrayOfString3 = URLDecoder.decode(localObject6[1], "UTF-8").split("[&,]");
                localObject6 = null;
                i5 = 0;
                i3 = 0;
                m = i;
                i2 = k;
                paramVarArgs = (Void[])localObject4;
                i4 = j;
                localObject5 = localObject3;
                if (i3 >= arrayOfString3.length) {
                  break;
                }
                localObject5 = arrayOfString3[i3].split("=");
                if (localObject5[0].startsWith("type"))
                {
                  paramVarArgs = (Void[])localObject6;
                  m = i5;
                  if (URLDecoder.decode(localObject5[1], "UTF-8").contains("video/mp4"))
                  {
                    m = 1;
                    paramVarArgs = (Void[])localObject6;
                  }
                }
                else if (localObject5[0].startsWith("url"))
                {
                  paramVarArgs = URLDecoder.decode(localObject5[1], "UTF-8");
                  m = i5;
                }
                else
                {
                  boolean bool = localObject5[0].startsWith("itag");
                  paramVarArgs = (Void[])localObject6;
                  m = i5;
                  if (bool)
                  {
                    paramVarArgs = null;
                    m = 0;
                  }
                }
              }
              catch (Exception paramVarArgs)
              {
                int i5;
                FileLog.e(paramVarArgs);
                m = i;
                i2 = k;
                paramVarArgs = (Void[])localObject4;
                i4 = j;
                localObject5 = localObject3;
              }
              i3 += 1;
              localObject6 = paramVarArgs;
              i5 = m;
            }
            break label490;
            if (arrayOfString2[i1].startsWith("use_cipher_signature"))
            {
              localObject6 = arrayOfString2[i1].split("=");
              m = i;
              i2 = k;
              paramVarArgs = (Void[])localObject4;
              i4 = j;
              localObject5 = localObject3;
              if (localObject6.length != 2) {
                break label490;
              }
              m = i;
              i2 = k;
              paramVarArgs = (Void[])localObject4;
              i4 = j;
              localObject5 = localObject3;
              if (!localObject6[1].toLowerCase().equals("true")) {
                break label490;
              }
              m = 1;
              i2 = k;
              paramVarArgs = (Void[])localObject4;
              i4 = j;
              localObject5 = localObject3;
              break label490;
            }
            if (arrayOfString2[i1].startsWith("hlsvp"))
            {
              localObject6 = arrayOfString2[i1].split("=");
              m = i;
              i2 = k;
              paramVarArgs = (Void[])localObject4;
              i4 = j;
              localObject5 = localObject3;
              if (localObject6.length != 2) {
                break label490;
              }
              try
              {
                paramVarArgs = URLDecoder.decode(localObject6[1], "UTF-8");
                m = i;
                i2 = k;
                i4 = j;
                localObject5 = localObject3;
              }
              catch (Exception paramVarArgs)
              {
                FileLog.e(paramVarArgs);
                m = i;
                i2 = k;
                paramVarArgs = (Void[])localObject4;
                i4 = j;
                localObject5 = localObject3;
              }
              break label490;
            }
            m = i;
            i2 = k;
            paramVarArgs = (Void[])localObject4;
            i4 = j;
            localObject5 = localObject3;
            if (!arrayOfString2[i1].startsWith("livestream")) {
              break label490;
            }
            localObject6 = arrayOfString2[i1].split("=");
            m = i;
            i2 = k;
            paramVarArgs = (Void[])localObject4;
            i4 = j;
            localObject5 = localObject3;
            if (localObject6.length != 2) {
              break label490;
            }
            m = i;
            i2 = k;
            paramVarArgs = (Void[])localObject4;
            i4 = j;
            localObject5 = localObject3;
            if (!localObject6[1].toLowerCase().equals("1")) {
              break label490;
            }
            i4 = 1;
            m = i;
            i2 = k;
            paramVarArgs = (Void[])localObject4;
            localObject5 = localObject3;
            break label490;
            if (i3 != 0)
            {
              if ((localObject6 == null) || (m != 0) || (((String)localObject6).contains("/s/"))) {
                return null;
              }
              this.result[0] = localObject6;
              this.result[1] = "hls";
            }
            if (i2 != 0)
            {
              if ((this.result[0] == null) && (localObject5 != null))
              {
                this.result[0] = localObject5;
                this.result[1] = "other";
              }
              i = m;
              if (this.result[0] == null) {
                break label1875;
              }
              if (m == 0)
              {
                i = m;
                if (!this.result[0].contains("/s/")) {
                  break label1875;
                }
              }
              i = m;
              if (localObject7 == null) {
                break label1875;
              }
              j = 1;
              m = this.result[0].indexOf("/s/");
              k = this.result[0].indexOf('/', m + 10);
              i = j;
              if (m == -1) {
                break label1875;
              }
              i = k;
              if (k == -1) {
                i = this.result[0].length();
              }
              this.sig = this.result[0].substring(m, i);
              paramVarArgs = null;
              localObject2 = WebPlayerView.jsPattern.matcher((CharSequence)localObject7);
              localObject3 = paramVarArgs;
              if (!((Matcher)localObject2).find()) {}
            }
            try
            {
              localObject2 = new JSONTokener(((Matcher)localObject2).group(1)).nextValue();
              localObject3 = paramVarArgs;
              if ((localObject2 instanceof String)) {
                localObject3 = (String)localObject2;
              }
            }
            catch (Exception localException2)
            {
              for (;;)
              {
                FileLog.e(localException2);
                localObject3 = paramVarArgs;
                continue;
                localObject5 = null;
                continue;
                localObject4 = localObject3;
                if (((String)localObject3).startsWith("/")) {
                  localObject4 = "https://www.youtube.com" + (String)localObject3;
                }
              }
              localObject4 = paramVarArgs;
              localObject6 = localException2;
              if (localObject3 == null) {
                break label1792;
              }
            }
            i = j;
            String str;
            if (localObject3 != null)
            {
              paramVarArgs = WebPlayerView.playerIdPattern.matcher((CharSequence)localObject3);
              if (paramVarArgs.find())
              {
                localObject5 = paramVarArgs.group(1) + paramVarArgs.group(2);
                paramVarArgs = null;
                localObject2 = null;
                localObject7 = ApplicationLoader.applicationContext.getSharedPreferences("youtubecode", 0);
                if (localObject5 != null)
                {
                  paramVarArgs = ((SharedPreferences)localObject7).getString((String)localObject5, null);
                  localObject2 = ((SharedPreferences)localObject7).getString((String)localObject5 + "n", null);
                }
                localObject4 = paramVarArgs;
                localObject6 = localObject2;
                if (paramVarArgs != null) {
                  break label1792;
                }
                if (!((String)localObject3).startsWith("//")) {
                  break label1588;
                }
                localObject4 = "https:" + (String)localObject3;
                localObject3 = WebPlayerView.this.downloadUrlContent(this, (String)localObject4);
                if (!isCancelled()) {
                  break label1629;
                }
                return null;
                n += 1;
                i = m;
                paramVarArgs = (Void[])localObject5;
                break;
              }
              localObject4 = WebPlayerView.sigPattern.matcher((CharSequence)localObject3);
              if (!((Matcher)localObject4).find()) {
                break label1888;
              }
              str = ((Matcher)localObject4).group(1);
            }
            for (;;)
            {
              localObject4 = paramVarArgs;
              localObject6 = str;
              if (str != null) {}
              try
              {
                localObject3 = new WebPlayerView.JSExtractor(WebPlayerView.this, (String)localObject3).extractFunction(str);
                localObject4 = localObject3;
                localObject6 = str;
                paramVarArgs = (Void[])localObject3;
                if (!TextUtils.isEmpty((CharSequence)localObject3))
                {
                  localObject4 = localObject3;
                  localObject6 = str;
                  if (localObject5 != null)
                  {
                    paramVarArgs = (Void[])localObject3;
                    ((SharedPreferences)localObject7).edit().putString((String)localObject5, (String)localObject3).putString((String)localObject5 + "n", str).commit();
                    localObject6 = str;
                    localObject4 = localObject3;
                  }
                }
              }
              catch (Exception localException3)
              {
                for (;;)
                {
                  FileLog.e(localException3);
                  localObject4 = paramVarArgs;
                  localObject6 = str;
                  continue;
                  paramVarArgs = (String)localObject4 + "window." + WebPlayerView.this.interfaceName + ".returnResultToJava(" + (String)localObject6 + "('" + this.sig.substring(3) + "'));";
                }
              }
              i = j;
              if (!TextUtils.isEmpty((CharSequence)localObject4))
              {
                if (Build.VERSION.SDK_INT < 21) {
                  break;
                }
                paramVarArgs = (String)localObject4 + (String)localObject6 + "('" + this.sig.substring(3) + "');";
              }
              try
              {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    if (Build.VERSION.SDK_INT >= 21)
                    {
                      WebPlayerView.this.webView.evaluateJavascript(paramVarArgs, new ValueCallback()
                      {
                        public void onReceiveValue(String paramAnonymous2String)
                        {
                          WebPlayerView.YoutubeVideoTask.this.result[0] = WebPlayerView.YoutubeVideoTask.this.result[0].replace(WebPlayerView.YoutubeVideoTask.this.sig, "/signature/" + paramAnonymous2String.substring(1, paramAnonymous2String.length() - 1));
                          WebPlayerView.YoutubeVideoTask.this.countDownLatch.countDown();
                        }
                      });
                      return;
                    }
                    try
                    {
                      String str = Base64.encodeToString(("<script>" + paramVarArgs + "</script>").getBytes("UTF-8"), 0);
                      WebPlayerView.this.webView.loadUrl("data:text/html;charset=utf-8;base64," + str);
                      return;
                    }
                    catch (Exception localException)
                    {
                      FileLog.e(localException);
                    }
                  }
                });
                this.countDownLatch.await();
                i = 0;
              }
              catch (Exception paramVarArgs)
              {
                for (;;)
                {
                  FileLog.e(paramVarArgs);
                  i = j;
                }
              }
              if ((!isCancelled()) && (i == 0)) {
                break label2013;
              }
              return null;
              localObject4 = WebPlayerView.sigPattern2.matcher((CharSequence)localObject3);
              if (((Matcher)localObject4).find()) {
                str = ((Matcher)localObject4).group(1);
              }
            }
            return this.result;
          } while ((m == 0) || (paramVarArgs == null));
          localObject5 = paramVarArgs;
          m = i;
          i2 = k;
          paramVarArgs = (Void[])localObject4;
          i4 = j;
        }
      }
    }
    
    protected void onPostExecute(String[] paramArrayOfString)
    {
      if (paramArrayOfString[0] != null)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("start play youtube video " + paramArrayOfString[1] + " " + paramArrayOfString[0]);
        }
        WebPlayerView.access$1702(WebPlayerView.this, true);
        WebPlayerView.access$1802(WebPlayerView.this, paramArrayOfString[0]);
        WebPlayerView.access$1902(WebPlayerView.this, paramArrayOfString[1]);
        if (WebPlayerView.this.playVideoType.equals("hls")) {
          WebPlayerView.access$2002(WebPlayerView.this, true);
        }
        if (WebPlayerView.this.isAutoplay) {
          WebPlayerView.this.preparePlayer();
        }
        WebPlayerView.this.showProgress(false, true);
        WebPlayerView.this.controlsView.show(true, true);
      }
      while (isCancelled()) {
        return;
      }
      WebPlayerView.this.onInitFailed();
    }
  }
  
  private abstract class function
  {
    private function() {}
    
    public abstract Object run(Object[] paramArrayOfObject);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/WebPlayerView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */