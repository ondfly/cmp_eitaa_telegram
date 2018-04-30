package ir.eitaa.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.AnimatorListenerAdapterProxy;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MediaController;
import ir.eitaa.messenger.NotificationCenter;
import ir.eitaa.messenger.NotificationCenter.NotificationCenterDelegate;
import ir.eitaa.messenger.support.widget.LinearLayoutManager;
import ir.eitaa.tgnet.TLRPC.BotInlineResult;
import ir.eitaa.tgnet.TLRPC.User;
import ir.eitaa.ui.ActionBar.ActionBar;
import ir.eitaa.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import ir.eitaa.ui.ActionBar.ActionBarMenu;
import ir.eitaa.ui.ActionBar.ActionBarMenuItem;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.ActionBar.Theme;
import ir.eitaa.ui.Adapters.MentionsAdapter;
import ir.eitaa.ui.Adapters.MentionsAdapter.MentionsAdapterDelegate;
import ir.eitaa.ui.Components.LayoutHelper;
import ir.eitaa.ui.Components.PhotoViewerCaptionEnterView;
import ir.eitaa.ui.Components.PhotoViewerCaptionEnterView.PhotoViewerCaptionEnterViewDelegate;
import ir.eitaa.ui.Components.PickerBottomLayoutViewer;
import ir.eitaa.ui.Components.RecyclerListView;
import ir.eitaa.ui.Components.RecyclerListView.OnItemClickListener;
import ir.eitaa.ui.Components.RecyclerListView.OnItemLongClickListener;
import ir.eitaa.ui.Components.SizeNotifierFrameLayoutPhoto;
import ir.eitaa.ui.Components.VideoSeekBarView;
import ir.eitaa.ui.Components.VideoSeekBarView.SeekBarDelegate;
import ir.eitaa.ui.Components.VideoTimelineView;
import ir.eitaa.ui.Components.VideoTimelineView.VideoTimelineViewDelegate;
import java.io.File;
import java.util.Iterator;
import java.util.List;

@TargetApi(16)
public class VideoEditorActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private boolean allowMentions;
  private long audioFramesSize;
  private int bitrate;
  private ActionBarMenuItem captionDoneItem;
  private PhotoViewerCaptionEnterView captionEditText;
  private ImageView captionItem;
  private ImageView compressItem;
  private boolean created;
  private CharSequence currentCaption;
  private VideoEditorActivityDelegate delegate;
  private long endTime;
  private long esimatedDuration;
  private int estimatedSize;
  private float lastProgress;
  private LinearLayoutManager mentionLayoutManager;
  private AnimatorSet mentionListAnimation;
  private RecyclerListView mentionListView;
  private MentionsAdapter mentionsAdapter;
  private ImageView muteItem;
  private boolean muteVideo;
  private boolean needCompressVideo;
  private boolean needSeek;
  private String oldTitle;
  private int originalBitrate;
  private int originalHeight;
  private long originalSize;
  private int originalWidth;
  private ChatActivity parentChatActivity;
  private PickerBottomLayoutViewer pickerView;
  private ImageView playButton;
  private boolean playerPrepared;
  private Runnable progressRunnable = new Runnable()
  {
    /* Error */
    public void run()
    {
      // Byte code:
      //   0: aload_0
      //   1: getfield 16	ir/eitaa/ui/VideoEditorActivity$1:this$0	Lir/eitaa/ui/VideoEditorActivity;
      //   4: invokestatic 27	ir/eitaa/ui/VideoEditorActivity:access$000	(Lir/eitaa/ui/VideoEditorActivity;)Ljava/lang/Object;
      //   7: astore_3
      //   8: aload_3
      //   9: monitorenter
      //   10: aload_0
      //   11: getfield 16	ir/eitaa/ui/VideoEditorActivity$1:this$0	Lir/eitaa/ui/VideoEditorActivity;
      //   14: invokestatic 31	ir/eitaa/ui/VideoEditorActivity:access$100	(Lir/eitaa/ui/VideoEditorActivity;)Landroid/media/MediaPlayer;
      //   17: ifnull +48 -> 65
      //   20: aload_0
      //   21: getfield 16	ir/eitaa/ui/VideoEditorActivity$1:this$0	Lir/eitaa/ui/VideoEditorActivity;
      //   24: invokestatic 31	ir/eitaa/ui/VideoEditorActivity:access$100	(Lir/eitaa/ui/VideoEditorActivity;)Landroid/media/MediaPlayer;
      //   27: invokevirtual 37	android/media/MediaPlayer:isPlaying	()Z
      //   30: istore_2
      //   31: iload_2
      //   32: ifeq +33 -> 65
      //   35: iconst_1
      //   36: istore_1
      //   37: aload_3
      //   38: monitorexit
      //   39: iload_1
      //   40: ifne +51 -> 91
      //   43: aload_0
      //   44: getfield 16	ir/eitaa/ui/VideoEditorActivity$1:this$0	Lir/eitaa/ui/VideoEditorActivity;
      //   47: invokestatic 27	ir/eitaa/ui/VideoEditorActivity:access$000	(Lir/eitaa/ui/VideoEditorActivity;)Ljava/lang/Object;
      //   50: astore_3
      //   51: aload_3
      //   52: monitorenter
      //   53: aload_0
      //   54: getfield 16	ir/eitaa/ui/VideoEditorActivity$1:this$0	Lir/eitaa/ui/VideoEditorActivity;
      //   57: aconst_null
      //   58: invokestatic 41	ir/eitaa/ui/VideoEditorActivity:access$702	(Lir/eitaa/ui/VideoEditorActivity;Ljava/lang/Thread;)Ljava/lang/Thread;
      //   61: pop
      //   62: aload_3
      //   63: monitorexit
      //   64: return
      //   65: iconst_0
      //   66: istore_1
      //   67: goto -30 -> 37
      //   70: astore 4
      //   72: iconst_0
      //   73: istore_1
      //   74: ldc 43
      //   76: aload 4
      //   78: invokestatic 49	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
      //   81: goto -44 -> 37
      //   84: astore 4
      //   86: aload_3
      //   87: monitorexit
      //   88: aload 4
      //   90: athrow
      //   91: new 10	ir/eitaa/ui/VideoEditorActivity$1$1
      //   94: dup
      //   95: aload_0
      //   96: invokespecial 52	ir/eitaa/ui/VideoEditorActivity$1$1:<init>	(Lir/eitaa/ui/VideoEditorActivity$1;)V
      //   99: invokestatic 58	ir/eitaa/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;)V
      //   102: ldc2_w 59
      //   105: invokestatic 66	java/lang/Thread:sleep	(J)V
      //   108: goto -108 -> 0
      //   111: astore_3
      //   112: ldc 43
      //   114: aload_3
      //   115: invokestatic 49	ir/eitaa/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
      //   118: goto -118 -> 0
      //   121: astore 4
      //   123: aload_3
      //   124: monitorexit
      //   125: aload 4
      //   127: athrow
      // Local variable table:
      //   start	length	slot	name	signature
      //   0	128	0	this	1
      //   36	38	1	i	int
      //   30	2	2	bool	boolean
      //   111	13	3	localException1	Exception
      //   70	7	4	localException2	Exception
      //   84	5	4	localObject2	Object
      //   121	5	4	localObject3	Object
      // Exception table:
      //   from	to	target	type
      //   10	31	70	java/lang/Exception
      //   10	31	84	finally
      //   37	39	84	finally
      //   74	81	84	finally
      //   86	88	84	finally
      //   102	108	111	java/lang/Exception
      //   53	64	121	finally
      //   123	125	121	finally
    }
  };
  private int resultHeight;
  private int resultWidth;
  private int rotationValue;
  private long startTime;
  private final Object sync = new Object();
  private TextureView textureView;
  private Thread thread;
  private float videoDuration;
  private long videoFramesSize;
  private String videoPath;
  private MediaPlayer videoPlayer;
  private VideoSeekBarView videoSeekBarView;
  private VideoTimelineView videoTimelineView;
  
  public VideoEditorActivity(Bundle paramBundle)
  {
    super(paramBundle);
    this.videoPath = paramBundle.getString("videoPath");
  }
  
  private void closeCaptionEnter(boolean paramBoolean)
  {
    if (paramBoolean) {
      this.currentCaption = this.captionEditText.getFieldCharSequence();
    }
    this.actionBar.setSubtitle(this.oldTitle);
    this.captionDoneItem.setVisibility(8);
    this.pickerView.setVisibility(0);
    this.videoSeekBarView.setVisibility(0);
    this.videoTimelineView.setVisibility(0);
    Object localObject = (FrameLayout.LayoutParams)this.captionEditText.getLayoutParams();
    ((FrameLayout.LayoutParams)localObject).bottomMargin = (-AndroidUtilities.dp(400.0F));
    this.captionEditText.setLayoutParams((ViewGroup.LayoutParams)localObject);
    localObject = (FrameLayout.LayoutParams)this.mentionListView.getLayoutParams();
    ((FrameLayout.LayoutParams)localObject).bottomMargin = (-AndroidUtilities.dp(400.0F));
    this.mentionListView.setLayoutParams((ViewGroup.LayoutParams)localObject);
    ActionBar localActionBar = this.actionBar;
    if (this.muteVideo)
    {
      localObject = LocaleController.getString("AttachGif", 2131165340);
      localActionBar.setTitle((CharSequence)localObject);
      localObject = this.captionItem;
      if (!TextUtils.isEmpty(this.currentCaption)) {
        break label203;
      }
    }
    label203:
    for (int i = 2130837890;; i = 2130837891)
    {
      ((ImageView)localObject).setImageResource(i);
      if (!this.captionEditText.isPopupShowing()) {
        break label210;
      }
      this.captionEditText.hidePopup();
      return;
      localObject = LocaleController.getString("AttachVideo", 2131165345);
      break;
    }
    label210:
    this.captionEditText.closeKeyboard();
  }
  
  private void onPlayComplete()
  {
    if (this.playButton != null) {
      this.playButton.setImageResource(2130838025);
    }
    if ((this.videoSeekBarView != null) && (this.videoTimelineView != null)) {
      this.videoSeekBarView.setProgress(this.videoTimelineView.getLeftProgress());
    }
    try
    {
      if ((this.videoPlayer != null) && (this.videoTimelineView != null)) {
        this.videoPlayer.seekTo((int)(this.videoTimelineView.getLeftProgress() * this.videoDuration));
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
  }
  
  private boolean processOpenVideo()
  {
    Object localObject2;
    int j;
    try
    {
      this.originalSize = new File(this.videoPath).length();
      localObject2 = new IsoFile(this.videoPath);
      localObject3 = Path.getPaths((Container)localObject2, "/moov/trak/");
      localObject1 = null;
      j = 1;
      i = 1;
      if (Path.getPath((Container)localObject2, "/moov/trak/mdia/minf/stbl/stsd/mp4a/") != null) {
        break label635;
      }
      i = 0;
    }
    catch (Exception localException1)
    {
      Object localObject3;
      Object localObject1;
      FileLog.e("TSMS", localException1);
      return false;
    }
    int i = j;
    if (Path.getPath((Container)localObject2, "/moov/trak/mdia/minf/stbl/stsd/avc1/") == null) {
      i = 0;
    }
    localObject3 = ((List)localObject3).iterator();
    for (;;)
    {
      if (!((Iterator)localObject3).hasNext()) {
        break label667;
      }
      localObject2 = (TrackBox)((Iterator)localObject3).next();
      l1 = 0L;
      l3 = 0L;
      l2 = l1;
      try
      {
        localObject4 = ((TrackBox)localObject2).getMediaBox();
        l2 = l1;
        localMediaHeaderBox = ((MediaBox)localObject4).getMediaHeaderBox();
        l2 = l1;
        localObject4 = ((MediaBox)localObject4).getMediaInformationBox().getSampleTableBox().getSampleSizeBox().getSampleSizes();
        l2 = l1;
        k = localObject4.length;
        j = 0;
      }
      catch (Exception localException2)
      {
        for (;;)
        {
          FileLog.e("TSMS", localException2);
          l1 = l2;
          l2 = l3;
        }
        this.audioFramesSize += l1;
      }
      l2 = l1;
      this.videoDuration = ((float)localMediaHeaderBox.getDuration() / (float)localMediaHeaderBox.getTimescale());
      f1 = (float)(8L * l1);
      l2 = l1;
      f2 = this.videoDuration;
      l2 = (int)(f1 / f2);
      localObject2 = ((TrackBox)localObject2).getTrackHeaderBox();
      if ((((TrackHeaderBox)localObject2).getWidth() != 0.0D) && (((TrackHeaderBox)localObject2).getHeight() != 0.0D))
      {
        localObject1 = localObject2;
        j = (int)(l2 / 100000L * 100000L);
        this.bitrate = j;
        this.originalBitrate = j;
        if (this.bitrate > 900000) {
          this.bitrate = 900000;
        }
        this.videoFramesSize += l1;
      }
    }
    label600:
    label617:
    label635:
    label667:
    while (localException1 != null) {
      for (;;)
      {
        long l1;
        long l3;
        long l2;
        Object localObject4;
        MediaHeaderBox localMediaHeaderBox;
        int k;
        float f2;
        localObject2 = localException1.getMatrix();
        if (((Matrix)localObject2).equals(Matrix.ROTATE_90))
        {
          this.rotationValue = 90;
          j = (int)localException1.getWidth();
          this.originalWidth = j;
          this.resultWidth = j;
          j = (int)localException1.getHeight();
          this.originalHeight = j;
          this.resultHeight = j;
          if ((this.resultWidth > 640) || (this.resultHeight > 640)) {
            if (this.resultWidth <= this.resultHeight) {
              break label600;
            }
          }
        }
        for (float f1 = 640.0F / this.resultWidth;; f1 = 640.0F / j)
        {
          this.resultWidth = ((int)(this.resultWidth * f1));
          this.resultHeight = ((int)(this.resultHeight * f1));
          if (this.bitrate != 0)
          {
            this.bitrate = ((int)(this.bitrate * Math.max(0.5F, f1)));
            this.videoFramesSize = ((this.bitrate / 8 * this.videoDuration));
          }
          if (i != 0) {
            break label617;
          }
          if (this.resultWidth == this.originalWidth) {
            break label674;
          }
          if (this.resultHeight != this.originalHeight) {
            break label617;
          }
          break label674;
          if (((Matrix)localObject2).equals(Matrix.ROTATE_180))
          {
            this.rotationValue = 180;
            break;
          }
          if (!((Matrix)localObject2).equals(Matrix.ROTATE_270)) {
            break;
          }
          this.rotationValue = 270;
          break;
          j = this.resultHeight;
        }
        this.videoDuration *= 1000.0F;
        updateVideoInfo();
        return true;
        if (i != 0) {
          break;
        }
        return false;
        while (j < k)
        {
          l1 += localObject4[j];
          j += 1;
        }
      }
    }
    return false;
    label674:
    return false;
  }
  
  private void updateVideoInfo()
  {
    if (this.actionBar == null) {
      return;
    }
    this.esimatedDuration = (Math.ceil((this.videoTimelineView.getRightProgress() - this.videoTimelineView.getLeftProgress()) * this.videoDuration));
    int i;
    int j;
    if ((this.compressItem.getVisibility() == 8) || ((this.compressItem.getVisibility() == 0) && (!this.needCompressVideo))) {
      if ((this.rotationValue == 90) || (this.rotationValue == 270))
      {
        i = this.originalHeight;
        if ((this.rotationValue != 90) && (this.rotationValue != 270)) {
          break label301;
        }
        j = this.originalWidth;
        label114:
        this.estimatedSize = ((int)((float)this.originalSize * ((float)this.esimatedDuration / this.videoDuration)));
        if (this.videoTimelineView.getLeftProgress() != 0.0F) {
          break label422;
        }
        this.startTime = -1L;
        label154:
        if (this.videoTimelineView.getRightProgress() != 1.0F) {
          break label446;
        }
      }
    }
    label301:
    label333:
    label414:
    label422:
    label446:
    for (this.endTime = -1L;; this.endTime = ((this.videoTimelineView.getRightProgress() * this.videoDuration) * 1000L))
    {
      String str1 = String.format("%dx%d", new Object[] { Integer.valueOf(i), Integer.valueOf(j) });
      i = (int)(this.esimatedDuration / 1000L / 60L);
      String str2 = String.format("%d:%02d, ~%s", new Object[] { Integer.valueOf(i), Integer.valueOf((int)Math.ceil(this.esimatedDuration / 1000L) - i * 60), AndroidUtilities.formatFileSize(this.estimatedSize) });
      this.actionBar.setSubtitle(String.format("%s, %s", new Object[] { str1, str2 }));
      return;
      i = this.originalWidth;
      break;
      j = this.originalHeight;
      break label114;
      if ((this.rotationValue == 90) || (this.rotationValue == 270))
      {
        i = this.resultHeight;
        if ((this.rotationValue != 90) && (this.rotationValue != 270)) {
          break label414;
        }
      }
      for (j = this.resultWidth;; j = this.resultHeight)
      {
        this.estimatedSize = ((int)((float)(this.audioFramesSize + this.videoFramesSize) * ((float)this.esimatedDuration / this.videoDuration)));
        this.estimatedSize += this.estimatedSize / 32768 * 16;
        break;
        i = this.resultWidth;
        break label333;
      }
      this.startTime = ((this.videoTimelineView.getLeftProgress() * this.videoDuration) * 1000L);
      break label154;
    }
  }
  
  public View createView(Context paramContext)
  {
    this.needCompressVideo = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("compress_video", true);
    this.actionBar.setBackgroundColor(-16777216);
    this.actionBar.setItemsBackgroundColor(-12763843);
    this.actionBar.setBackButtonImage(2130837705);
    this.actionBar.setTitle(LocaleController.getString("AttachVideo", 2131165345));
    this.actionBar.setSubtitleColor(-1);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          if ((VideoEditorActivity.this.captionEditText.isPopupShowing()) || (VideoEditorActivity.this.captionEditText.isKeyboardVisible())) {
            VideoEditorActivity.this.closeCaptionEnter(false);
          }
        }
        while (paramAnonymousInt != 1)
        {
          return;
          VideoEditorActivity.this.finishFragment();
          return;
        }
        VideoEditorActivity.this.closeCaptionEnter(true);
      }
    });
    this.captionDoneItem = this.actionBar.createMenu().addItemWithWidth(1, 2130837725, AndroidUtilities.dp(56.0F));
    this.captionDoneItem.setVisibility(8);
    this.fragmentView = new SizeNotifierFrameLayoutPhoto(paramContext)
    {
      int lastWidth;
      
      public boolean dispatchKeyEventPreIme(KeyEvent paramAnonymousKeyEvent)
      {
        if ((paramAnonymousKeyEvent != null) && (paramAnonymousKeyEvent.getKeyCode() == 4) && (paramAnonymousKeyEvent.getAction() == 1) && ((VideoEditorActivity.this.captionEditText.isPopupShowing()) || (VideoEditorActivity.this.captionEditText.isKeyboardVisible())))
        {
          VideoEditorActivity.this.closeCaptionEnter(false);
          return false;
        }
        return super.dispatchKeyEventPreIme(paramAnonymousKeyEvent);
      }
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        int i2 = getChildCount();
        if ((getKeyboardHeight() <= AndroidUtilities.dp(20.0F)) && (!AndroidUtilities.isInMultiwindow)) {}
        int i3;
        View localView;
        for (int m = VideoEditorActivity.this.captionEditText.getEmojiPadding();; m = 0)
        {
          i3 = AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight();
          int n = 0;
          for (;;)
          {
            if (n >= i2) {
              break label738;
            }
            localView = getChildAt(n);
            if (localView.getVisibility() != 8) {
              break;
            }
            n += 1;
          }
        }
        FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)localView.getLayoutParams();
        int i4 = localView.getMeasuredWidth();
        int i5 = localView.getMeasuredHeight();
        int j = localLayoutParams.gravity;
        int i = j;
        if (j == -1) {
          i = 51;
        }
        int k;
        label179:
        label227:
        int i1;
        switch (i & 0x7 & 0x7)
        {
        default: 
          k = localLayoutParams.leftMargin;
          switch (i & 0x70)
          {
          default: 
            j = localLayoutParams.topMargin;
            if (localView == VideoEditorActivity.this.mentionListView)
            {
              i = paramAnonymousInt4 - m - paramAnonymousInt2 - i5 - localLayoutParams.bottomMargin;
              if ((!VideoEditorActivity.this.captionEditText.isPopupShowing()) && (!VideoEditorActivity.this.captionEditText.isKeyboardVisible()) && (VideoEditorActivity.this.captionEditText.getEmojiPadding() == 0))
              {
                i += AndroidUtilities.dp(400.0F);
                i1 = k;
              }
            }
            break;
          }
          break;
        }
        for (;;)
        {
          localView.layout(i1, i, i1 + i4, i + i5);
          break;
          k = (paramAnonymousInt3 - paramAnonymousInt1 - i4) / 2 + localLayoutParams.leftMargin - localLayoutParams.rightMargin;
          break label179;
          k = paramAnonymousInt3 - i4 - localLayoutParams.rightMargin;
          break label179;
          j = localLayoutParams.topMargin;
          break label227;
          j = (i3 - i5) / 2 + localLayoutParams.topMargin - localLayoutParams.bottomMargin;
          break label227;
          j = i3 - i5 - localLayoutParams.bottomMargin;
          break label227;
          i -= VideoEditorActivity.this.captionEditText.getMeasuredHeight();
          i1 = k;
          continue;
          if (localView == VideoEditorActivity.this.captionEditText)
          {
            j = paramAnonymousInt4 - m - paramAnonymousInt2 - i5 - localLayoutParams.bottomMargin;
            i1 = k;
            i = j;
            if (!VideoEditorActivity.this.captionEditText.isPopupShowing())
            {
              i1 = k;
              i = j;
              if (!VideoEditorActivity.this.captionEditText.isKeyboardVisible())
              {
                i1 = k;
                i = j;
                if (VideoEditorActivity.this.captionEditText.getEmojiPadding() == 0)
                {
                  i = j + AndroidUtilities.dp(400.0F);
                  i1 = k;
                }
              }
            }
          }
          else if (localView == VideoEditorActivity.this.pickerView)
          {
            if (!VideoEditorActivity.this.captionEditText.isPopupShowing())
            {
              i1 = k;
              i = j;
              if (!VideoEditorActivity.this.captionEditText.isKeyboardVisible()) {}
            }
            else
            {
              i = j + AndroidUtilities.dp(400.0F);
              i1 = k;
            }
          }
          else if (VideoEditorActivity.this.captionEditText.isPopupView(localView))
          {
            if (AndroidUtilities.isInMultiwindow)
            {
              i = VideoEditorActivity.this.captionEditText.getTop() - localView.getMeasuredHeight() + AndroidUtilities.dp(1.0F);
              i1 = k;
            }
            else
            {
              i = VideoEditorActivity.this.captionEditText.getBottom();
              i1 = k;
            }
          }
          else
          {
            i1 = k;
            i = j;
            if (localView == VideoEditorActivity.this.textureView)
            {
              i1 = (paramAnonymousInt3 - paramAnonymousInt1 - VideoEditorActivity.this.textureView.getMeasuredWidth()) / 2;
              i = AndroidUtilities.dp(14.0F);
            }
          }
        }
        label738:
        notifyHeightChanged();
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        int n = View.MeasureSpec.getSize(paramAnonymousInt1);
        setMeasuredDimension(n, View.MeasureSpec.getSize(paramAnonymousInt2));
        int i2 = AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight();
        measureChildWithMargins(VideoEditorActivity.this.captionEditText, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
        int i3 = VideoEditorActivity.this.captionEditText.getMeasuredHeight();
        int i4 = getChildCount();
        int i = 0;
        if (i < i4)
        {
          View localView = getChildAt(i);
          if ((localView.getVisibility() == 8) || (localView == VideoEditorActivity.this.captionEditText)) {}
          for (;;)
          {
            i += 1;
            break;
            if (VideoEditorActivity.this.captionEditText.isPopupView(localView))
            {
              if (AndroidUtilities.isInMultiwindow)
              {
                if (AndroidUtilities.isTablet()) {
                  localView.measure(View.MeasureSpec.makeMeasureSpec(n, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320.0F), i2 - i3 - AndroidUtilities.statusBarHeight), 1073741824));
                } else {
                  localView.measure(View.MeasureSpec.makeMeasureSpec(n, 1073741824), View.MeasureSpec.makeMeasureSpec(i2 - i3 - AndroidUtilities.statusBarHeight, 1073741824));
                }
              }
              else {
                localView.measure(View.MeasureSpec.makeMeasureSpec(n, 1073741824), View.MeasureSpec.makeMeasureSpec(localView.getLayoutParams().height, 1073741824));
              }
            }
            else
            {
              if (localView == VideoEditorActivity.this.textureView)
              {
                int j = n;
                int i1 = i2 - AndroidUtilities.dp(166.0F);
                label291:
                int m;
                label325:
                float f3;
                if ((VideoEditorActivity.this.rotationValue == 90) || (VideoEditorActivity.this.rotationValue == 270))
                {
                  k = VideoEditorActivity.this.originalHeight;
                  if ((VideoEditorActivity.this.rotationValue != 90) && (VideoEditorActivity.this.rotationValue != 270)) {
                    break label405;
                  }
                  m = VideoEditorActivity.this.originalWidth;
                  float f1 = j / k;
                  float f2 = i1 / m;
                  f3 = k / m;
                  if (f1 <= f2) {
                    break label417;
                  }
                  j = (int)(i1 * f3);
                }
                label405:
                label417:
                for (int k = i1;; k = (int)(j / f3))
                {
                  localView.measure(View.MeasureSpec.makeMeasureSpec(j, 1073741824), View.MeasureSpec.makeMeasureSpec(k, 1073741824));
                  break;
                  k = VideoEditorActivity.this.originalWidth;
                  break label291;
                  m = VideoEditorActivity.this.originalHeight;
                  break label325;
                }
              }
              measureChildWithMargins(localView, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
            }
          }
        }
        if (this.lastWidth != n)
        {
          VideoEditorActivity.this.videoTimelineView.clearFrames();
          this.lastWidth = n;
        }
      }
    };
    this.fragmentView.setBackgroundColor(-16777216);
    Object localObject1 = (SizeNotifierFrameLayoutPhoto)this.fragmentView;
    ((SizeNotifierFrameLayoutPhoto)localObject1).setWithoutWindow(true);
    this.pickerView = new PickerBottomLayoutViewer(paramContext);
    this.pickerView.setBackgroundColor(0);
    this.pickerView.updateSelectedCount(0, false);
    ((SizeNotifierFrameLayoutPhoto)localObject1).addView(this.pickerView, LayoutHelper.createFrame(-1, 48, 83));
    this.pickerView.cancelButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        VideoEditorActivity.this.finishFragment();
      }
    });
    this.pickerView.doneButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View arg1)
      {
        int i;
        long l3;
        long l4;
        for (;;)
        {
          synchronized (VideoEditorActivity.this.sync)
          {
            Object localObject1 = VideoEditorActivity.this.videoPlayer;
            if (localObject1 != null) {}
            try
            {
              VideoEditorActivity.this.videoPlayer.stop();
              VideoEditorActivity.this.videoPlayer.release();
              VideoEditorActivity.access$102(VideoEditorActivity.this, null);
              if (VideoEditorActivity.this.delegate != null)
              {
                if ((VideoEditorActivity.this.compressItem.getVisibility() != 8) && ((VideoEditorActivity.this.compressItem.getVisibility() != 0) || (VideoEditorActivity.this.needCompressVideo))) {
                  break;
                }
                localObject1 = VideoEditorActivity.this.delegate;
                str = VideoEditorActivity.this.videoPath;
                l1 = VideoEditorActivity.this.startTime;
                l2 = VideoEditorActivity.this.endTime;
                j = VideoEditorActivity.this.originalWidth;
                k = VideoEditorActivity.this.originalHeight;
                m = VideoEditorActivity.this.rotationValue;
                n = VideoEditorActivity.this.originalWidth;
                i1 = VideoEditorActivity.this.originalHeight;
                if (VideoEditorActivity.this.muteVideo)
                {
                  i = -1;
                  l3 = VideoEditorActivity.this.estimatedSize;
                  l4 = VideoEditorActivity.this.esimatedDuration;
                  if (VideoEditorActivity.this.currentCaption == null) {
                    break label303;
                  }
                  ??? = VideoEditorActivity.this.currentCaption.toString();
                  ((VideoEditorActivity.VideoEditorActivityDelegate)localObject1).didFinishEditVideo(str, l1, l2, j, k, m, n, i1, i, l3, l4, ???);
                }
              }
              else
              {
                VideoEditorActivity.this.finishFragment();
                return;
              }
            }
            catch (Exception localException)
            {
              FileLog.e("TSMS", localException);
              continue;
            }
          }
          i = VideoEditorActivity.this.originalBitrate;
          continue;
          label303:
          ??? = null;
        }
        VideoEditorActivity.VideoEditorActivityDelegate localVideoEditorActivityDelegate = VideoEditorActivity.this.delegate;
        String str = VideoEditorActivity.this.videoPath;
        long l1 = VideoEditorActivity.this.startTime;
        long l2 = VideoEditorActivity.this.endTime;
        int j = VideoEditorActivity.this.resultWidth;
        int k = VideoEditorActivity.this.resultHeight;
        int m = VideoEditorActivity.this.rotationValue;
        int n = VideoEditorActivity.this.originalWidth;
        int i1 = VideoEditorActivity.this.originalHeight;
        if (VideoEditorActivity.this.muteVideo)
        {
          i = -1;
          label400:
          l3 = VideoEditorActivity.this.estimatedSize;
          l4 = VideoEditorActivity.this.esimatedDuration;
          if (VideoEditorActivity.this.currentCaption == null) {
            break label484;
          }
        }
        label484:
        for (??? = VideoEditorActivity.this.currentCaption.toString();; ??? = null)
        {
          localVideoEditorActivityDelegate.didFinishEditVideo(str, l1, l2, j, k, m, n, i1, i, l3, l4, ???);
          break;
          i = VideoEditorActivity.this.bitrate;
          break label400;
        }
      }
    });
    LinearLayout localLinearLayout = new LinearLayout(paramContext);
    localLinearLayout.setOrientation(0);
    this.pickerView.addView(localLinearLayout, LayoutHelper.createFrame(-2, 48, 49));
    this.captionItem = new ImageView(paramContext);
    this.captionItem.setScaleType(ImageView.ScaleType.CENTER);
    Object localObject2 = this.captionItem;
    int i;
    if (TextUtils.isEmpty(this.currentCaption))
    {
      i = 2130837890;
      ((ImageView)localObject2).setImageResource(i);
      this.captionItem.setBackgroundDrawable(Theme.createBarSelectorDrawable(1090519039));
      localLinearLayout.addView(this.captionItem, LayoutHelper.createLinear(56, 48));
      this.captionItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          VideoEditorActivity.this.captionEditText.setFieldText(VideoEditorActivity.this.currentCaption);
          VideoEditorActivity.this.captionDoneItem.setVisibility(0);
          VideoEditorActivity.this.videoSeekBarView.setVisibility(8);
          VideoEditorActivity.this.videoTimelineView.setVisibility(8);
          VideoEditorActivity.this.pickerView.setVisibility(8);
          paramAnonymousView = (FrameLayout.LayoutParams)VideoEditorActivity.this.captionEditText.getLayoutParams();
          paramAnonymousView.bottomMargin = 0;
          VideoEditorActivity.this.captionEditText.setLayoutParams(paramAnonymousView);
          paramAnonymousView = (FrameLayout.LayoutParams)VideoEditorActivity.this.mentionListView.getLayoutParams();
          paramAnonymousView.bottomMargin = 0;
          VideoEditorActivity.this.mentionListView.setLayoutParams(paramAnonymousView);
          VideoEditorActivity.this.captionEditText.openKeyboard();
          VideoEditorActivity.access$3202(VideoEditorActivity.this, VideoEditorActivity.this.actionBar.getSubtitle());
          VideoEditorActivity.this.actionBar.setTitle(LocaleController.getString("VideoCaption", 2131166388));
          VideoEditorActivity.this.actionBar.setSubtitle(null);
        }
      });
      this.compressItem = new ImageView(paramContext);
      this.compressItem.setScaleType(ImageView.ScaleType.CENTER);
      localObject2 = this.compressItem;
      if (!this.needCompressVideo) {
        break label1098;
      }
      i = 2130837697;
      label407:
      ((ImageView)localObject2).setImageResource(i);
      this.compressItem.setBackgroundDrawable(Theme.createBarSelectorDrawable(1090519039));
      localObject2 = this.compressItem;
      if ((this.originalHeight == this.resultHeight) && (this.originalWidth == this.resultWidth)) {
        break label1105;
      }
      i = 0;
      label456:
      ((ImageView)localObject2).setVisibility(i);
      localLinearLayout.addView(this.compressItem, LayoutHelper.createLinear(56, 48));
      this.compressItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = VideoEditorActivity.this;
          boolean bool;
          if (!VideoEditorActivity.this.needCompressVideo)
          {
            bool = true;
            VideoEditorActivity.access$1902(paramAnonymousView, bool);
            paramAnonymousView = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
            paramAnonymousView.putBoolean("compress_video", VideoEditorActivity.this.needCompressVideo);
            paramAnonymousView.commit();
            paramAnonymousView = VideoEditorActivity.this.compressItem;
            if (!VideoEditorActivity.this.needCompressVideo) {
              break label100;
            }
          }
          label100:
          for (int i = 2130837697;; i = 2130837698)
          {
            paramAnonymousView.setImageResource(i);
            VideoEditorActivity.this.updateVideoInfo();
            return;
            bool = false;
            break;
          }
        }
      });
      if (Build.VERSION.SDK_INT >= 18) {}
    }
    for (;;)
    {
      try
      {
        localObject2 = MediaController.selectCodec("video/avc");
        if (localObject2 != null) {
          continue;
        }
        this.compressItem.setVisibility(8);
      }
      catch (Exception localException)
      {
        label1098:
        label1105:
        String str;
        this.compressItem.setVisibility(8);
        FileLog.e("TSMS", localException);
        continue;
        if (MediaController.selectColorFormat(localException, "video/avc") != 0) {
          continue;
        }
        this.compressItem.setVisibility(8);
        continue;
      }
      this.muteItem = new ImageView(paramContext);
      this.muteItem.setScaleType(ImageView.ScaleType.CENTER);
      this.muteItem.setBackgroundDrawable(Theme.createBarSelectorDrawable(1090519039));
      localLinearLayout.addView(this.muteItem, LayoutHelper.createLinear(56, 48));
      this.muteItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = VideoEditorActivity.this;
          if (!VideoEditorActivity.this.muteVideo) {}
          for (boolean bool = true;; bool = false)
          {
            VideoEditorActivity.access$2302(paramAnonymousView, bool);
            VideoEditorActivity.this.updateMuteButton();
            return;
          }
        }
      });
      this.videoTimelineView = new VideoTimelineView(paramContext);
      this.videoTimelineView.setVideoPath(this.videoPath);
      this.videoTimelineView.setDelegate(new VideoTimelineView.VideoTimelineViewDelegate()
      {
        public void onLeftProgressChanged(float paramAnonymousFloat)
        {
          if ((VideoEditorActivity.this.videoPlayer == null) || (!VideoEditorActivity.this.playerPrepared)) {
            return;
          }
          try
          {
            if (VideoEditorActivity.this.videoPlayer.isPlaying())
            {
              VideoEditorActivity.this.videoPlayer.pause();
              VideoEditorActivity.this.playButton.setImageResource(2130838025);
            }
            VideoEditorActivity.this.videoPlayer.setOnSeekCompleteListener(null);
            VideoEditorActivity.this.videoPlayer.seekTo((int)(VideoEditorActivity.this.videoDuration * paramAnonymousFloat));
          }
          catch (Exception localException)
          {
            for (;;)
            {
              FileLog.e("TSMS", localException);
            }
          }
          VideoEditorActivity.access$3802(VideoEditorActivity.this, true);
          VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.videoTimelineView.getLeftProgress());
          VideoEditorActivity.this.updateVideoInfo();
        }
        
        public void onRifhtProgressChanged(float paramAnonymousFloat)
        {
          if ((VideoEditorActivity.this.videoPlayer == null) || (!VideoEditorActivity.this.playerPrepared)) {
            return;
          }
          try
          {
            if (VideoEditorActivity.this.videoPlayer.isPlaying())
            {
              VideoEditorActivity.this.videoPlayer.pause();
              VideoEditorActivity.this.playButton.setImageResource(2130838025);
            }
            VideoEditorActivity.this.videoPlayer.setOnSeekCompleteListener(null);
            VideoEditorActivity.this.videoPlayer.seekTo((int)(VideoEditorActivity.this.videoDuration * paramAnonymousFloat));
          }
          catch (Exception localException)
          {
            for (;;)
            {
              FileLog.e("TSMS", localException);
            }
          }
          VideoEditorActivity.access$3802(VideoEditorActivity.this, true);
          VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.videoTimelineView.getLeftProgress());
          VideoEditorActivity.this.updateVideoInfo();
        }
      });
      ((SizeNotifierFrameLayoutPhoto)localObject1).addView(this.videoTimelineView, LayoutHelper.createFrame(-1, 44.0F, 83, 0.0F, 0.0F, 0.0F, 67.0F));
      this.videoSeekBarView = new VideoSeekBarView(paramContext);
      this.videoSeekBarView.setDelegate(new VideoSeekBarView.SeekBarDelegate()
      {
        public void onSeekBarDrag(float paramAnonymousFloat)
        {
          float f;
          if (paramAnonymousFloat < VideoEditorActivity.this.videoTimelineView.getLeftProgress())
          {
            f = VideoEditorActivity.this.videoTimelineView.getLeftProgress();
            VideoEditorActivity.this.videoSeekBarView.setProgress(f);
          }
          while ((VideoEditorActivity.this.videoPlayer == null) || (!VideoEditorActivity.this.playerPrepared))
          {
            return;
            f = paramAnonymousFloat;
            if (paramAnonymousFloat > VideoEditorActivity.this.videoTimelineView.getRightProgress())
            {
              f = VideoEditorActivity.this.videoTimelineView.getRightProgress();
              VideoEditorActivity.this.videoSeekBarView.setProgress(f);
            }
          }
          if (VideoEditorActivity.this.videoPlayer.isPlaying()) {
            try
            {
              VideoEditorActivity.this.videoPlayer.seekTo((int)(VideoEditorActivity.this.videoDuration * f));
              VideoEditorActivity.access$402(VideoEditorActivity.this, f);
              return;
            }
            catch (Exception localException)
            {
              FileLog.e("TSMS", localException);
              return;
            }
          }
          VideoEditorActivity.access$402(VideoEditorActivity.this, f);
          VideoEditorActivity.access$3802(VideoEditorActivity.this, true);
        }
      });
      ((SizeNotifierFrameLayoutPhoto)localObject1).addView(this.videoSeekBarView, LayoutHelper.createFrame(-1, 40.0F, 83, 11.0F, 0.0F, 11.0F, 112.0F));
      this.textureView = new TextureView(paramContext);
      this.textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener()
      {
        public void onSurfaceTextureAvailable(SurfaceTexture paramAnonymousSurfaceTexture, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          if ((VideoEditorActivity.this.textureView == null) || (!VideoEditorActivity.this.textureView.isAvailable()) || (VideoEditorActivity.this.videoPlayer == null)) {}
          for (;;)
          {
            return;
            try
            {
              paramAnonymousSurfaceTexture = new Surface(VideoEditorActivity.this.textureView.getSurfaceTexture());
              VideoEditorActivity.this.videoPlayer.setSurface(paramAnonymousSurfaceTexture);
              if (VideoEditorActivity.this.playerPrepared)
              {
                VideoEditorActivity.this.videoPlayer.seekTo((int)(VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration));
                return;
              }
            }
            catch (Exception paramAnonymousSurfaceTexture)
            {
              FileLog.e("TSMS", paramAnonymousSurfaceTexture);
            }
          }
        }
        
        public boolean onSurfaceTextureDestroyed(SurfaceTexture paramAnonymousSurfaceTexture)
        {
          if (VideoEditorActivity.this.videoPlayer == null) {
            return true;
          }
          VideoEditorActivity.this.videoPlayer.setDisplay(null);
          return true;
        }
        
        public void onSurfaceTextureSizeChanged(SurfaceTexture paramAnonymousSurfaceTexture, int paramAnonymousInt1, int paramAnonymousInt2) {}
        
        public void onSurfaceTextureUpdated(SurfaceTexture paramAnonymousSurfaceTexture) {}
      });
      ((SizeNotifierFrameLayoutPhoto)localObject1).addView(this.textureView, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 14.0F, 0.0F, 140.0F));
      this.playButton = new ImageView(paramContext);
      this.playButton.setScaleType(ImageView.ScaleType.CENTER);
      this.playButton.setImageResource(2130838025);
      this.playButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View arg1)
        {
          if ((VideoEditorActivity.this.videoPlayer == null) || (!VideoEditorActivity.this.playerPrepared)) {
            return;
          }
          if (VideoEditorActivity.this.videoPlayer.isPlaying())
          {
            VideoEditorActivity.this.videoPlayer.pause();
            VideoEditorActivity.this.playButton.setImageResource(2130838025);
            return;
          }
          try
          {
            VideoEditorActivity.this.playButton.setImageDrawable(null);
            VideoEditorActivity.access$402(VideoEditorActivity.this, 0.0F);
            if (VideoEditorActivity.this.needSeek)
            {
              VideoEditorActivity.this.videoPlayer.seekTo((int)(VideoEditorActivity.this.videoDuration * VideoEditorActivity.this.videoSeekBarView.getProgress()));
              VideoEditorActivity.access$3802(VideoEditorActivity.this, false);
            }
            VideoEditorActivity.this.videoPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener()
            {
              public void onSeekComplete(MediaPlayer paramAnonymous2MediaPlayer)
              {
                float f2 = VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration;
                float f3 = VideoEditorActivity.this.videoTimelineView.getRightProgress() * VideoEditorActivity.this.videoDuration;
                float f1 = f2;
                if (f2 == f3) {
                  f1 = f3 - 0.01F;
                }
                VideoEditorActivity.access$402(VideoEditorActivity.this, (VideoEditorActivity.this.videoPlayer.getCurrentPosition() - f1) / (f3 - f1));
                f1 = VideoEditorActivity.this.videoTimelineView.getRightProgress();
                f2 = VideoEditorActivity.this.videoTimelineView.getLeftProgress();
                VideoEditorActivity.access$402(VideoEditorActivity.this, VideoEditorActivity.this.videoTimelineView.getLeftProgress() + VideoEditorActivity.this.lastProgress * (f1 - f2));
                VideoEditorActivity.this.videoSeekBarView.setProgress(VideoEditorActivity.this.lastProgress);
              }
            });
            VideoEditorActivity.this.videoPlayer.start();
            synchronized (VideoEditorActivity.this.sync)
            {
              if (VideoEditorActivity.this.thread == null)
              {
                VideoEditorActivity.access$702(VideoEditorActivity.this, new Thread(VideoEditorActivity.this.progressRunnable));
                VideoEditorActivity.this.thread.start();
              }
              return;
            }
            return;
          }
          catch (Exception ???)
          {
            FileLog.e("TSMS", ???);
          }
        }
      });
      ((SizeNotifierFrameLayoutPhoto)localObject1).addView(this.playButton, LayoutHelper.createFrame(100, 100.0F, 17, 0.0F, 0.0F, 0.0F, 70.0F));
      if (this.captionEditText != null) {
        this.captionEditText.onDestroy();
      }
      this.captionEditText = new PhotoViewerCaptionEnterView(paramContext, (SizeNotifierFrameLayoutPhoto)localObject1, null);
      this.captionEditText.setDelegate(new PhotoViewerCaptionEnterView.PhotoViewerCaptionEnterViewDelegate()
      {
        public void onCaptionEnter()
        {
          VideoEditorActivity.this.closeCaptionEnter(true);
        }
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence)
        {
          if ((VideoEditorActivity.this.mentionsAdapter != null) && (VideoEditorActivity.this.captionEditText != null) && (VideoEditorActivity.this.parentChatActivity != null) && (paramAnonymousCharSequence != null)) {
            VideoEditorActivity.this.mentionsAdapter.searchUsernameOrHashtag(paramAnonymousCharSequence.toString(), VideoEditorActivity.this.captionEditText.getCursorPosition(), VideoEditorActivity.this.parentChatActivity.messages);
          }
        }
        
        public void onWindowSizeChanged(int paramAnonymousInt)
        {
          int j = Math.min(3, VideoEditorActivity.this.mentionsAdapter.getItemCount());
          int i;
          if (VideoEditorActivity.this.mentionsAdapter.getItemCount() > 3)
          {
            i = 18;
            i = AndroidUtilities.dp(i + j * 36);
            if (paramAnonymousInt - ActionBar.getCurrentActionBarHeight() * 2 >= i) {
              break label113;
            }
            VideoEditorActivity.access$4202(VideoEditorActivity.this, false);
            if ((VideoEditorActivity.this.mentionListView != null) && (VideoEditorActivity.this.mentionListView.getVisibility() == 0)) {
              VideoEditorActivity.this.mentionListView.setVisibility(4);
            }
          }
          for (;;)
          {
            VideoEditorActivity.this.fragmentView.requestLayout();
            return;
            i = 0;
            break;
            label113:
            VideoEditorActivity.access$4202(VideoEditorActivity.this, true);
            if ((VideoEditorActivity.this.mentionListView != null) && (VideoEditorActivity.this.mentionListView.getVisibility() == 4)) {
              VideoEditorActivity.this.mentionListView.setVisibility(0);
            }
          }
        }
      });
      ((SizeNotifierFrameLayoutPhoto)localObject1).addView(this.captionEditText, LayoutHelper.createFrame(-1, -2.0F, 83, 0.0F, 0.0F, 0.0F, -400.0F));
      this.captionEditText.onCreate();
      this.mentionListView = new RecyclerListView(paramContext);
      this.mentionListView.setTag(Integer.valueOf(5));
      this.mentionLayoutManager = new LinearLayoutManager(paramContext)
      {
        public boolean supportsPredictiveItemAnimations()
        {
          return false;
        }
      };
      this.mentionLayoutManager.setOrientation(1);
      this.mentionListView.setLayoutManager(this.mentionLayoutManager);
      this.mentionListView.setBackgroundColor(2130706432);
      this.mentionListView.setVisibility(8);
      this.mentionListView.setClipToPadding(true);
      this.mentionListView.setOverScrollMode(2);
      ((SizeNotifierFrameLayoutPhoto)localObject1).addView(this.mentionListView, LayoutHelper.createFrame(-1, 110, 83));
      localObject1 = this.mentionListView;
      paramContext = new MentionsAdapter(paramContext, true, 0L, new MentionsAdapter.MentionsAdapterDelegate()
      {
        public void needChangePanelVisibility(boolean paramAnonymousBoolean)
        {
          int i;
          if (paramAnonymousBoolean)
          {
            FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)VideoEditorActivity.this.mentionListView.getLayoutParams();
            int j = Math.min(3, VideoEditorActivity.this.mentionsAdapter.getItemCount());
            if (VideoEditorActivity.this.mentionsAdapter.getItemCount() > 3)
            {
              i = 18;
              i = j * 36 + i;
              localLayoutParams.height = AndroidUtilities.dp(i);
              localLayoutParams.topMargin = (-AndroidUtilities.dp(i));
              VideoEditorActivity.this.mentionListView.setLayoutParams(localLayoutParams);
              if (VideoEditorActivity.this.mentionListAnimation != null)
              {
                VideoEditorActivity.this.mentionListAnimation.cancel();
                VideoEditorActivity.access$4402(VideoEditorActivity.this, null);
              }
              if (VideoEditorActivity.this.mentionListView.getVisibility() != 0) {
                break label150;
              }
              VideoEditorActivity.this.mentionListView.setAlpha(1.0F);
            }
          }
          label150:
          do
          {
            return;
            i = 0;
            break;
            VideoEditorActivity.this.mentionLayoutManager.scrollToPositionWithOffset(0, 10000);
            if (VideoEditorActivity.this.allowMentions)
            {
              VideoEditorActivity.this.mentionListView.setVisibility(0);
              VideoEditorActivity.access$4402(VideoEditorActivity.this, new AnimatorSet());
              VideoEditorActivity.this.mentionListAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(VideoEditorActivity.this.mentionListView, "alpha", new float[] { 0.0F, 1.0F }) });
              VideoEditorActivity.this.mentionListAnimation.addListener(new AnimatorListenerAdapterProxy()
              {
                public void onAnimationEnd(Animator paramAnonymous2Animator)
                {
                  if ((VideoEditorActivity.this.mentionListAnimation != null) && (VideoEditorActivity.this.mentionListAnimation.equals(paramAnonymous2Animator))) {
                    VideoEditorActivity.access$4402(VideoEditorActivity.this, null);
                  }
                }
              });
              VideoEditorActivity.this.mentionListAnimation.setDuration(200L);
              VideoEditorActivity.this.mentionListAnimation.start();
              return;
            }
            VideoEditorActivity.this.mentionListView.setAlpha(1.0F);
            VideoEditorActivity.this.mentionListView.setVisibility(4);
            return;
            if (VideoEditorActivity.this.mentionListAnimation != null)
            {
              VideoEditorActivity.this.mentionListAnimation.cancel();
              VideoEditorActivity.access$4402(VideoEditorActivity.this, null);
            }
          } while (VideoEditorActivity.this.mentionListView.getVisibility() == 8);
          if (VideoEditorActivity.this.allowMentions)
          {
            VideoEditorActivity.access$4402(VideoEditorActivity.this, new AnimatorSet());
            VideoEditorActivity.this.mentionListAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(VideoEditorActivity.this.mentionListView, "alpha", new float[] { 0.0F }) });
            VideoEditorActivity.this.mentionListAnimation.addListener(new AnimatorListenerAdapterProxy()
            {
              public void onAnimationEnd(Animator paramAnonymous2Animator)
              {
                if ((VideoEditorActivity.this.mentionListAnimation != null) && (VideoEditorActivity.this.mentionListAnimation.equals(paramAnonymous2Animator)))
                {
                  VideoEditorActivity.this.mentionListView.setVisibility(8);
                  VideoEditorActivity.access$4402(VideoEditorActivity.this, null);
                }
              }
            });
            VideoEditorActivity.this.mentionListAnimation.setDuration(200L);
            VideoEditorActivity.this.mentionListAnimation.start();
            return;
          }
          VideoEditorActivity.this.mentionListView.setVisibility(8);
        }
        
        public void onContextClick(TLRPC.BotInlineResult paramAnonymousBotInlineResult) {}
        
        public void onContextSearch(boolean paramAnonymousBoolean) {}
      });
      this.mentionsAdapter = paramContext;
      ((RecyclerListView)localObject1).setAdapter(paramContext);
      this.mentionsAdapter.setAllowNewMentions(false);
      this.mentionListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          paramAnonymousView = VideoEditorActivity.this.mentionsAdapter.getItem(paramAnonymousInt);
          paramAnonymousInt = VideoEditorActivity.this.mentionsAdapter.getResultStartPosition();
          int i = VideoEditorActivity.this.mentionsAdapter.getResultLength();
          if ((paramAnonymousView instanceof TLRPC.User))
          {
            paramAnonymousView = (TLRPC.User)paramAnonymousView;
            if (paramAnonymousView != null) {
              VideoEditorActivity.this.captionEditText.replaceWithText(paramAnonymousInt, i, "@" + paramAnonymousView.username + " ");
            }
          }
          while (!(paramAnonymousView instanceof String)) {
            return;
          }
          VideoEditorActivity.this.captionEditText.replaceWithText(paramAnonymousInt, i, paramAnonymousView + " ");
        }
      });
      this.mentionListView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (VideoEditorActivity.this.getParentActivity() == null) {}
          while (!(VideoEditorActivity.this.mentionsAdapter.getItem(paramAnonymousInt) instanceof String)) {
            return false;
          }
          paramAnonymousView = new AlertDialog.Builder(VideoEditorActivity.this.getParentActivity());
          paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131165299));
          paramAnonymousView.setMessage(LocaleController.getString("ClearSearch", 2131165514));
          paramAnonymousView.setPositiveButton(LocaleController.getString("ClearButton", 2131165508).toUpperCase(), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              VideoEditorActivity.this.mentionsAdapter.clearRecentHashtags();
            }
          });
          paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131165386), null);
          VideoEditorActivity.this.showDialog(paramAnonymousView.create());
          return true;
        }
      });
      updateVideoInfo();
      updateMuteButton();
      return this.fragmentView;
      i = 2130837891;
      break;
      i = 2130837698;
      break label407;
      i = 8;
      break label456;
      str = ((MediaCodecInfo)localObject2).getName();
      if ((!str.equals("OMX.google.h264.encoder")) && (!str.equals("OMX.ST.VFM.H264Enc")) && (!str.equals("OMX.Exynos.avc.enc")) && (!str.equals("OMX.MARVELL.VIDEO.HW.CODA7542ENCODER")) && (!str.equals("OMX.MARVELL.VIDEO.H264ENCODER")) && (!str.equals("OMX.k3.video.encoder.avc")) && (!str.equals("OMX.TI.DUCATI1.VIDEO.H264E"))) {
        continue;
      }
      this.compressItem.setVisibility(8);
    }
  }
  
  public void didReceivedNotification(int paramInt, Object... paramVarArgs)
  {
    if (paramInt == NotificationCenter.closeChats) {
      removeSelfFromStack();
    }
  }
  
  public boolean onFragmentCreate()
  {
    if (this.created) {
      return true;
    }
    if ((this.videoPath == null) || (!processOpenVideo())) {
      return false;
    }
    this.videoPlayer = new MediaPlayer();
    this.videoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
    {
      public void onCompletion(MediaPlayer paramAnonymousMediaPlayer)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            VideoEditorActivity.this.onPlayComplete();
          }
        });
      }
    });
    this.videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
    {
      public void onPrepared(MediaPlayer paramAnonymousMediaPlayer)
      {
        VideoEditorActivity.access$802(VideoEditorActivity.this, true);
        if ((VideoEditorActivity.this.videoTimelineView != null) && (VideoEditorActivity.this.videoPlayer != null)) {
          VideoEditorActivity.this.videoPlayer.seekTo((int)(VideoEditorActivity.this.videoTimelineView.getLeftProgress() * VideoEditorActivity.this.videoDuration));
        }
      }
    });
    try
    {
      this.videoPlayer.setDataSource(this.videoPath);
      this.videoPlayer.prepareAsync();
      NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
      this.created = true;
      return super.onFragmentCreate();
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
    return false;
  }
  
  public void onFragmentDestroy()
  {
    if (this.videoTimelineView != null) {
      this.videoTimelineView.destroy();
    }
    if (this.videoPlayer != null) {}
    try
    {
      this.videoPlayer.stop();
      this.videoPlayer.release();
      this.videoPlayer = null;
      if (this.captionEditText != null) {
        this.captionEditText.onDestroy();
      }
      NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
      super.onFragmentDestroy();
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
  
  public void onPause()
  {
    super.onPause();
    if (this.captionDoneItem.getVisibility() != 8) {
      closeCaptionEnter(true);
    }
  }
  
  public void setDelegate(VideoEditorActivityDelegate paramVideoEditorActivityDelegate)
  {
    this.delegate = paramVideoEditorActivityDelegate;
  }
  
  public void setParentChatActivity(ChatActivity paramChatActivity)
  {
    this.parentChatActivity = paramChatActivity;
  }
  
  public void updateMuteButton()
  {
    float f;
    if (this.videoPlayer != null)
    {
      if (!this.muteVideo) {
        break label117;
      }
      f = 0.0F;
      if (this.videoPlayer != null) {
        this.videoPlayer.setVolume(f, f);
      }
    }
    if (this.muteVideo)
    {
      this.actionBar.setTitle(LocaleController.getString("AttachGif", 2131165340));
      this.muteItem.setImageResource(2130838029);
      if (this.captionItem.getVisibility() == 0)
      {
        this.needCompressVideo = true;
        this.compressItem.setImageResource(2130837697);
        this.compressItem.setClickable(false);
        this.compressItem.setAlpha(0.8F);
        this.compressItem.setEnabled(false);
      }
    }
    label117:
    do
    {
      return;
      f = 1.0F;
      break;
      this.actionBar.setTitle(LocaleController.getString("AttachVideo", 2131165345));
      this.muteItem.setImageResource(2130838030);
    } while (this.captionItem.getVisibility() != 0);
    this.compressItem.setClickable(true);
    this.compressItem.setAlpha(1.0F);
    this.compressItem.setEnabled(true);
  }
  
  public static abstract interface VideoEditorActivityDelegate
  {
    public abstract void didFinishEditVideo(String paramString1, long paramLong1, long paramLong2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, long paramLong3, long paramLong4, String paramString2);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/VideoEditorActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */