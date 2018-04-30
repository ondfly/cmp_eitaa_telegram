package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaCodecInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Keep;
import android.text.Layout.Alignment;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TimeToSampleBox.Entry;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Bitmaps;
import org.telegram.messenger.BringAppForegroundService;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.EmojiSuggestion;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.ImageReceiver.BitmapHolder;
import org.telegram.messenger.ImageReceiver.ImageReceiverDelegate;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.PhotoEntry;
import org.telegram.messenger.MediaController.SavedFilterState;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.exoplayer2.ui.AspectRatioFrameLayout;
import org.telegram.messenger.support.widget.DefaultItemAnimator;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.LinearSmoothScrollerEnd;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ItemDecoration;
import org.telegram.messenger.support.widget.RecyclerView.State;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInlineResult;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputChannel;
import org.telegram.tgnet.TLRPC.InputPhoto;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Page;
import org.telegram.tgnet.TLRPC.PageBlock;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_inputPhoto;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_pageBlockAuthorDate;
import org.telegram.tgnet.TLRPC.TL_pageFull;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.TL_webDocument;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.WebDocument;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.MentionsAdapter;
import org.telegram.ui.Adapters.MentionsAdapter.MentionsAdapterDelegate;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.PhotoPickerPhotoCell;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatAttachAlert;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.ClippingImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.NumberPicker.Formatter;
import org.telegram.ui.Components.Paint.Views.ColorPicker;
import org.telegram.ui.Components.PhotoCropView;
import org.telegram.ui.Components.PhotoCropView.PhotoCropViewDelegate;
import org.telegram.ui.Components.PhotoFilterView;
import org.telegram.ui.Components.PhotoPaintView;
import org.telegram.ui.Components.PhotoViewerCaptionEnterView;
import org.telegram.ui.Components.PhotoViewerCaptionEnterView.PhotoViewerCaptionEnterViewDelegate;
import org.telegram.ui.Components.PickerBottomLayoutViewer;
import org.telegram.ui.Components.PipVideoView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.SeekBar;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate;
import org.telegram.ui.Components.SizeNotifierFrameLayoutPhoto;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.URLSpanUserMentionPhotoViewer;
import org.telegram.ui.Components.VideoPlayer;
import org.telegram.ui.Components.VideoPlayer.VideoPlayerDelegate;
import org.telegram.ui.Components.VideoTimelinePlayView;
import org.telegram.ui.Components.VideoTimelinePlayView.VideoTimelineViewDelegate;

public class PhotoViewer
  implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener, NotificationCenter.NotificationCenterDelegate
{
  @SuppressLint({"StaticFieldLeak"})
  private static volatile PhotoViewer Instance = null;
  private static volatile PhotoViewer PipInstance = null;
  private static DecelerateInterpolator decelerateInterpolator;
  private static final int gallery_menu_cancel_loading = 7;
  private static final int gallery_menu_delete = 6;
  private static final int gallery_menu_masks = 13;
  private static final int gallery_menu_openin = 11;
  private static final int gallery_menu_pip = 5;
  private static final int gallery_menu_save = 1;
  private static final int gallery_menu_send = 3;
  private static final int gallery_menu_share = 10;
  private static final int gallery_menu_showall = 2;
  private static final int gallery_menu_showinchat = 4;
  private static Drawable[] progressDrawables;
  private static Paint progressPaint;
  private ActionBar actionBar;
  private AnimatorSet actionBarAnimator;
  private Context actvityContext;
  private boolean allowMentions;
  private boolean allowShare;
  private float animateToScale;
  private float animateToX;
  private float animateToY;
  private ClippingImageView animatingImageView;
  private Runnable animationEndRunnable;
  private int animationInProgress;
  private long animationStartTime;
  private float animationValue;
  private float[][] animationValues = (float[][])Array.newInstance(Float.TYPE, new int[] { 2, 8 });
  private boolean applying;
  private AspectRatioFrameLayout aspectRatioFrameLayout;
  private boolean attachedToWindow;
  private long audioFramesSize;
  private ArrayList<TLRPC.Photo> avatarsArr = new ArrayList();
  private int avatarsDialogId;
  private BackgroundDrawable backgroundDrawable = new BackgroundDrawable(-16777216);
  private int bitrate;
  private Paint blackPaint = new Paint();
  private FrameLayout bottomLayout;
  private boolean bottomTouchEnabled = true;
  private ImageView cameraItem;
  private boolean canDragDown = true;
  private boolean canZoom = true;
  private PhotoViewerCaptionEnterView captionEditText;
  private TextView captionTextView;
  private ImageReceiver centerImage = new ImageReceiver();
  private AnimatorSet changeModeAnimation;
  private TextureView changedTextureView;
  private boolean changingPage;
  private boolean changingTextureView;
  private CheckBox checkImageView;
  private int classGuid;
  private ImageView compressItem;
  private AnimatorSet compressItemAnimation;
  private int compressionsCount = -1;
  private FrameLayoutDrawer containerView;
  private ImageView cropItem;
  private int currentAccount;
  private AnimatedFileDrawable currentAnimation;
  private Bitmap currentBitmap;
  private TLRPC.BotInlineResult currentBotInlineResult;
  private AnimatorSet currentCaptionAnimation;
  private long currentDialogId;
  private int currentEditMode;
  private TLRPC.FileLocation currentFileLocation;
  private String[] currentFileNames = new String[3];
  private int currentIndex;
  private AnimatorSet currentListViewAnimation;
  private Runnable currentLoadingVideoRunnable;
  private MessageObject currentMessageObject;
  private String currentPathObject;
  private PlaceProviderObject currentPlaceObject;
  private Uri currentPlayingVideoFile;
  private String currentSubtitle;
  private ImageReceiver.BitmapHolder currentThumb;
  private TLRPC.FileLocation currentUserAvatarLocation = null;
  private boolean currentVideoFinishedLoading;
  private int dateOverride;
  private TextView dateTextView;
  private boolean disableShowCheck;
  private boolean discardTap;
  private boolean doneButtonPressed;
  private boolean dontResetZoomOnFirstLayout;
  private boolean doubleTap;
  private float dragY;
  private boolean draggingDown;
  private PickerBottomLayoutViewer editorDoneLayout;
  private boolean[] endReached = { 0, 1 };
  private long endTime;
  private long estimatedDuration;
  private int estimatedSize;
  private boolean firstAnimationDelay;
  boolean fromCamera;
  private GestureDetector gestureDetector;
  private GroupedPhotosListView groupedPhotosListView;
  private PlaceProviderObject hideAfterAnimation;
  private AnimatorSet hintAnimation;
  private Runnable hintHideRunnable;
  private TextView hintTextView;
  private boolean ignoreDidSetImage;
  private AnimatorSet imageMoveAnimation;
  private ArrayList<MessageObject> imagesArr = new ArrayList();
  private ArrayList<Object> imagesArrLocals = new ArrayList();
  private ArrayList<TLRPC.FileLocation> imagesArrLocations = new ArrayList();
  private ArrayList<Integer> imagesArrLocationsSizes = new ArrayList();
  private ArrayList<MessageObject> imagesArrTemp = new ArrayList();
  private SparseArray<MessageObject>[] imagesByIds = { new SparseArray(), new SparseArray() };
  private SparseArray<MessageObject>[] imagesByIdsTemp = { new SparseArray(), new SparseArray() };
  private boolean inPreview;
  private DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5F);
  private boolean invalidCoords;
  private boolean isActionBarVisible = true;
  private boolean isCurrentVideo;
  private boolean isEvent;
  private boolean isFirstLoading;
  private boolean isInline;
  private boolean isPhotosListViewVisible;
  private boolean isPlaying;
  private boolean isStreaming;
  private boolean isVisible;
  private LinearLayout itemsLayout;
  private boolean keepScreenOnFlagSet;
  private long lastBufferedPositionCheck;
  private Object lastInsets;
  private String lastTitle;
  private ImageReceiver leftImage = new ImageReceiver();
  private boolean loadInitialVideo;
  private boolean loadingMoreImages;
  private ActionBarMenuItem masksItem;
  private float maxX;
  private float maxY;
  private LinearLayoutManager mentionLayoutManager;
  private AnimatorSet mentionListAnimation;
  private RecyclerListView mentionListView;
  private MentionsAdapter mentionsAdapter;
  private ActionBarMenuItem menuItem;
  private long mergeDialogId;
  private float minX;
  private float minY;
  private AnimatorSet miniProgressAnimator;
  private Runnable miniProgressShowRunnable = new Runnable()
  {
    public void run()
    {
      PhotoViewer.this.toggleMiniProgressInternal(true);
    }
  };
  private RadialProgressView miniProgressView;
  private float moveStartX;
  private float moveStartY;
  private boolean moving;
  private ImageView muteItem;
  private boolean muteVideo;
  private String nameOverride;
  private TextView nameTextView;
  private boolean needCaptionLayout;
  private boolean needSearchImageInArr;
  private boolean opennedFromMedia;
  private int originalBitrate;
  private int originalHeight;
  private long originalSize;
  private int originalWidth;
  private ImageView paintItem;
  private Activity parentActivity;
  private ChatAttachAlert parentAlert;
  private ChatActivity parentChatActivity;
  private PhotoCropView photoCropView;
  private PhotoFilterView photoFilterView;
  private PhotoPaintView photoPaintView;
  private PhotoProgressView[] photoProgressViews = new PhotoProgressView[3];
  private CounterView photosCounterView;
  private FrameLayout pickerView;
  private ImageView pickerViewSendButton;
  private float pinchCenterX;
  private float pinchCenterY;
  private float pinchStartDistance;
  private float pinchStartScale = 1.0F;
  private float pinchStartX;
  private float pinchStartY;
  private boolean pipAnimationInProgress;
  private boolean pipAvailable;
  private ActionBarMenuItem pipItem;
  private int[] pipPosition = new int[2];
  private PipVideoView pipVideoView;
  private PhotoViewerProvider placeProvider;
  private int previewViewEnd;
  private int previousCompression;
  private RadialProgressView progressView;
  private QualityChooseView qualityChooseView;
  private AnimatorSet qualityChooseViewAnimation;
  private PickerBottomLayoutViewer qualityPicker;
  private boolean requestingPreview;
  private TextView resetButton;
  private int resultHeight;
  private int resultWidth;
  private ImageReceiver rightImage = new ImageReceiver();
  private int rotationValue;
  private float scale = 1.0F;
  private Scroller scroller;
  private float seekToProgressPending;
  private int selectedCompression;
  private ListAdapter selectedPhotosAdapter;
  private RecyclerListView selectedPhotosListView;
  private ActionBarMenuItem sendItem;
  private int sendPhotoType;
  private ImageView shareButton;
  private PlaceProviderObject showAfterAnimation;
  private int slideshowMessageId;
  private long startTime;
  private long startedPlayTime;
  private boolean streamingAlertShown;
  private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener()
  {
    public void onSurfaceTextureAvailable(SurfaceTexture paramAnonymousSurfaceTexture, int paramAnonymousInt1, int paramAnonymousInt2) {}
    
    public boolean onSurfaceTextureDestroyed(SurfaceTexture paramAnonymousSurfaceTexture)
    {
      if (PhotoViewer.this.videoTextureView == null) {}
      while (!PhotoViewer.this.changingTextureView) {
        return true;
      }
      if (PhotoViewer.this.switchingInlineMode) {
        PhotoViewer.access$2702(PhotoViewer.this, 2);
      }
      PhotoViewer.this.videoTextureView.setSurfaceTexture(paramAnonymousSurfaceTexture);
      PhotoViewer.this.videoTextureView.setVisibility(0);
      PhotoViewer.access$2002(PhotoViewer.this, false);
      PhotoViewer.this.containerView.invalidate();
      return false;
    }
    
    public void onSurfaceTextureSizeChanged(SurfaceTexture paramAnonymousSurfaceTexture, int paramAnonymousInt1, int paramAnonymousInt2) {}
    
    public void onSurfaceTextureUpdated(SurfaceTexture paramAnonymousSurfaceTexture)
    {
      if (PhotoViewer.this.waitingForFirstTextureUpload == 1)
      {
        PhotoViewer.this.changedTextureView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
          public boolean onPreDraw()
          {
            PhotoViewer.this.changedTextureView.getViewTreeObserver().removeOnPreDrawListener(this);
            if (PhotoViewer.this.textureImageView != null)
            {
              PhotoViewer.this.textureImageView.setVisibility(4);
              PhotoViewer.this.textureImageView.setImageDrawable(null);
              if (PhotoViewer.this.currentBitmap != null)
              {
                PhotoViewer.this.currentBitmap.recycle();
                PhotoViewer.access$1902(PhotoViewer.this, null);
              }
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                if (PhotoViewer.this.isInline) {
                  PhotoViewer.this.dismissInternal();
                }
              }
            });
            PhotoViewer.access$2702(PhotoViewer.this, 0);
            return true;
          }
        });
        PhotoViewer.this.changedTextureView.invalidate();
      }
    }
  };
  private TextView switchCaptionTextView;
  private int switchImageAfterAnimation;
  private Runnable switchToInlineRunnable = new Runnable()
  {
    public void run()
    {
      PhotoViewer.access$1802(PhotoViewer.this, false);
      if (PhotoViewer.this.currentBitmap != null)
      {
        PhotoViewer.this.currentBitmap.recycle();
        PhotoViewer.access$1902(PhotoViewer.this, null);
      }
      PhotoViewer.access$2002(PhotoViewer.this, true);
      if (PhotoViewer.this.textureImageView != null) {}
      try
      {
        PhotoViewer.access$1902(PhotoViewer.this, Bitmaps.createBitmap(PhotoViewer.this.videoTextureView.getWidth(), PhotoViewer.this.videoTextureView.getHeight(), Bitmap.Config.ARGB_8888));
        PhotoViewer.this.videoTextureView.getBitmap(PhotoViewer.this.currentBitmap);
        if (PhotoViewer.this.currentBitmap != null)
        {
          PhotoViewer.this.textureImageView.setVisibility(0);
          PhotoViewer.this.textureImageView.setImageBitmap(PhotoViewer.this.currentBitmap);
          PhotoViewer.access$2302(PhotoViewer.this, true);
          PhotoViewer.access$1402(PhotoViewer.this, new PipVideoView());
          PhotoViewer.access$2402(PhotoViewer.this, PhotoViewer.this.pipVideoView.show(PhotoViewer.this.parentActivity, PhotoViewer.this, PhotoViewer.this.aspectRatioFrameLayout.getAspectRatio(), PhotoViewer.this.aspectRatioFrameLayout.getVideoRotation()));
          PhotoViewer.this.changedTextureView.setVisibility(4);
          PhotoViewer.this.aspectRatioFrameLayout.removeView(PhotoViewer.this.videoTextureView);
          return;
        }
      }
      catch (Throwable localThrowable)
      {
        for (;;)
        {
          if (PhotoViewer.this.currentBitmap != null)
          {
            PhotoViewer.this.currentBitmap.recycle();
            PhotoViewer.access$1902(PhotoViewer.this, null);
          }
          FileLog.e(localThrowable);
          continue;
          PhotoViewer.this.textureImageView.setImageDrawable(null);
        }
      }
    }
  };
  private boolean switchingInlineMode;
  private int switchingToIndex;
  private ImageView textureImageView;
  private boolean textureUploaded;
  private ImageView timeItem;
  private int totalImagesCount;
  private int totalImagesCountMerge;
  private long transitionAnimationStartTime;
  private float translationX;
  private float translationY;
  private boolean tryStartRequestPreviewOnFinish;
  private ImageView tuneItem;
  private Runnable updateProgressRunnable = new Runnable()
  {
    public void run()
    {
      float f1;
      if (PhotoViewer.this.videoPlayer != null)
      {
        if (!PhotoViewer.this.isCurrentVideo) {
          break label276;
        }
        if (!PhotoViewer.this.videoTimelineView.isDragging())
        {
          f1 = (float)PhotoViewer.this.videoPlayer.getCurrentPosition() / (float)PhotoViewer.this.videoPlayer.getDuration();
          if ((PhotoViewer.this.inPreview) || (PhotoViewer.this.videoTimelineView.getVisibility() != 0)) {
            break label262;
          }
          if (f1 < PhotoViewer.this.videoTimelineView.getRightProgress()) {
            break label191;
          }
          PhotoViewer.this.videoPlayer.pause();
          PhotoViewer.this.videoTimelineView.setProgress(0.0F);
          PhotoViewer.this.videoPlayer.seekTo((int)(PhotoViewer.this.videoTimelineView.getLeftProgress() * (float)PhotoViewer.this.videoPlayer.getDuration()));
          PhotoViewer.this.containerView.invalidate();
          PhotoViewer.this.updateVideoPlayerTime();
        }
      }
      label191:
      label262:
      label276:
      while (PhotoViewer.this.videoPlayerSeekbar.isDragging()) {
        for (;;)
        {
          if (PhotoViewer.this.isPlaying) {
            AndroidUtilities.runOnUIThread(PhotoViewer.this.updateProgressRunnable, 17L);
          }
          return;
          f2 = f1 - PhotoViewer.this.videoTimelineView.getLeftProgress();
          f1 = f2;
          if (f2 < 0.0F) {
            f1 = 0.0F;
          }
          f2 = f1 / (PhotoViewer.this.videoTimelineView.getRightProgress() - PhotoViewer.this.videoTimelineView.getLeftProgress());
          f1 = f2;
          if (f2 > 1.0F) {
            f1 = 1.0F;
          }
          PhotoViewer.this.videoTimelineView.setProgress(f1);
          continue;
          PhotoViewer.this.videoTimelineView.setProgress(f1);
        }
      }
      float f2 = (float)PhotoViewer.this.videoPlayer.getCurrentPosition() / (float)PhotoViewer.this.videoPlayer.getDuration();
      if (PhotoViewer.this.currentVideoFinishedLoading)
      {
        f1 = 1.0F;
        label325:
        if ((PhotoViewer.this.inPreview) || (PhotoViewer.this.videoTimelineView.getVisibility() != 0)) {
          break label628;
        }
        if (f2 < PhotoViewer.this.videoTimelineView.getRightProgress()) {
          break label557;
        }
        PhotoViewer.this.videoPlayer.pause();
        PhotoViewer.this.videoPlayerSeekbar.setProgress(0.0F);
        PhotoViewer.this.videoPlayer.seekTo((int)(PhotoViewer.this.videoTimelineView.getLeftProgress() * (float)PhotoViewer.this.videoPlayer.getDuration()));
        PhotoViewer.this.containerView.invalidate();
      }
      for (;;)
      {
        PhotoViewer.this.videoPlayerControlFrameLayout.invalidate();
        PhotoViewer.this.updateVideoPlayerTime();
        break;
        long l = SystemClock.uptimeMillis();
        if (Math.abs(l - PhotoViewer.this.lastBufferedPositionCheck) >= 500L)
        {
          FileLoader localFileLoader;
          if (PhotoViewer.this.isStreaming)
          {
            localFileLoader = FileLoader.getInstance(PhotoViewer.this.currentAccount);
            if (PhotoViewer.this.seekToProgressPending != 0.0F) {
              f1 = PhotoViewer.this.seekToProgressPending;
            }
          }
          label513:
          for (f1 = localFileLoader.getBufferedProgressFromPosition(f1, PhotoViewer.this.currentFileNames[0]);; f1 = 1.0F)
          {
            PhotoViewer.access$902(PhotoViewer.this, l);
            break;
            f1 = f2;
            break label513;
          }
        }
        f1 = -1.0F;
        break label325;
        label557:
        f2 -= PhotoViewer.this.videoTimelineView.getLeftProgress();
        f1 = f2;
        if (f2 < 0.0F) {
          f1 = 0.0F;
        }
        f2 = f1 / (PhotoViewer.this.videoTimelineView.getRightProgress() - PhotoViewer.this.videoTimelineView.getLeftProgress());
        f1 = f2;
        if (f2 > 1.0F) {
          f1 = 1.0F;
        }
        PhotoViewer.this.videoPlayerSeekbar.setProgress(f1);
        continue;
        label628:
        if (PhotoViewer.this.seekToProgressPending == 0.0F) {
          PhotoViewer.this.videoPlayerSeekbar.setProgress(f2);
        }
        if (f1 != -1.0F)
        {
          PhotoViewer.this.videoPlayerSeekbar.setBufferedProgress(f1);
          if (PhotoViewer.this.pipVideoView != null) {
            PhotoViewer.this.pipVideoView.setBufferedProgress(f1);
          }
        }
      }
    }
  };
  private VelocityTracker velocityTracker;
  private ImageView videoBackwardButton;
  private float videoCrossfadeAlpha;
  private long videoCrossfadeAlphaLastTime;
  private boolean videoCrossfadeStarted;
  private float videoDuration;
  private ImageView videoForwardButton;
  private int videoFramerate;
  private long videoFramesSize;
  private boolean videoHasAudio;
  private ImageView videoPlayButton;
  private VideoPlayer videoPlayer;
  private FrameLayout videoPlayerControlFrameLayout;
  private SeekBar videoPlayerSeekbar;
  private SimpleTextView videoPlayerTime;
  private MessageObject videoPreviewMessageObject;
  private TextureView videoTextureView;
  private VideoTimelinePlayView videoTimelineView;
  private AlertDialog visibleDialog;
  private int waitingForDraw;
  private int waitingForFirstTextureUpload;
  private boolean wasLayout;
  private WindowManager.LayoutParams windowLayoutParams;
  private FrameLayout windowView;
  private boolean zoomAnimation;
  private boolean zooming;
  
  public PhotoViewer()
  {
    this.blackPaint.setColor(-16777216);
  }
  
  private void animateTo(float paramFloat1, float paramFloat2, float paramFloat3, boolean paramBoolean)
  {
    animateTo(paramFloat1, paramFloat2, paramFloat3, paramBoolean, 250);
  }
  
  private void animateTo(float paramFloat1, float paramFloat2, float paramFloat3, boolean paramBoolean, int paramInt)
  {
    if ((this.scale == paramFloat1) && (this.translationX == paramFloat2) && (this.translationY == paramFloat3)) {
      return;
    }
    this.zoomAnimation = paramBoolean;
    this.animateToScale = paramFloat1;
    this.animateToX = paramFloat2;
    this.animateToY = paramFloat3;
    this.animationStartTime = System.currentTimeMillis();
    this.imageMoveAnimation = new AnimatorSet();
    this.imageMoveAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "animationValue", new float[] { 0.0F, 1.0F }) });
    this.imageMoveAnimation.setInterpolator(this.interpolator);
    this.imageMoveAnimation.setDuration(paramInt);
    this.imageMoveAnimation.addListener(new AnimatorListenerAdapter()
    {
      public void onAnimationEnd(Animator paramAnonymousAnimator)
      {
        PhotoViewer.access$14502(PhotoViewer.this, null);
        PhotoViewer.this.containerView.invalidate();
      }
    });
    this.imageMoveAnimation.start();
  }
  
  private void applyCurrentEditMode()
  {
    Bitmap localBitmap = null;
    Object localObject2 = null;
    TLRPC.PhotoSize localPhotoSize = null;
    int i = 0;
    Object localObject1;
    Object localObject3;
    label205:
    label217:
    float f1;
    float f2;
    if (this.currentEditMode == 1)
    {
      localBitmap = this.photoCropView.getBitmap();
      i = 1;
      localObject1 = localPhotoSize;
      if (localBitmap != null)
      {
        localPhotoSize = ImageLoader.scaleAndSaveImage(localBitmap, AndroidUtilities.getPhotoSize(), AndroidUtilities.getPhotoSize(), 80, false, 101, 101);
        if (localPhotoSize != null)
        {
          localObject3 = this.imagesArrLocals.get(this.currentIndex);
          if (!(localObject3 instanceof MediaController.PhotoEntry)) {
            break label589;
          }
          localObject3 = (MediaController.PhotoEntry)localObject3;
          ((MediaController.PhotoEntry)localObject3).imagePath = FileLoader.getPathToAttach(localPhotoSize, true).toString();
          localPhotoSize = ImageLoader.scaleAndSaveImage(localBitmap, AndroidUtilities.dp(120.0F), AndroidUtilities.dp(120.0F), 70, false, 101, 101);
          if (localPhotoSize != null) {
            ((MediaController.PhotoEntry)localObject3).thumbPath = FileLoader.getPathToAttach(localPhotoSize, true).toString();
          }
          if (localObject2 != null) {
            ((MediaController.PhotoEntry)localObject3).stickers.addAll((Collection)localObject2);
          }
          if (this.currentEditMode != 1) {
            break label502;
          }
          this.cropItem.setColorFilter(new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY));
          ((MediaController.PhotoEntry)localObject3).isCropped = true;
          if (localObject1 == null) {
            break label576;
          }
          ((MediaController.PhotoEntry)localObject3).savedFilterState = ((MediaController.SavedFilterState)localObject1);
          if ((this.sendPhotoType == 0) && (this.placeProvider != null))
          {
            this.placeProvider.updatePhotoAtIndex(this.currentIndex);
            if (!this.placeProvider.isPhotoChecked(this.currentIndex)) {
              setPhotoChecked();
            }
          }
          if (this.currentEditMode == 1)
          {
            f1 = this.photoCropView.getRectSizeX() / getContainerViewWidth();
            f2 = this.photoCropView.getRectSizeY() / getContainerViewHeight();
            if (f1 <= f2) {
              break label817;
            }
          }
        }
      }
    }
    for (;;)
    {
      this.scale = f1;
      this.translationX = (this.photoCropView.getRectX() + this.photoCropView.getRectSizeX() / 2.0F - getContainerViewWidth() / 2);
      this.translationY = (this.photoCropView.getRectY() + this.photoCropView.getRectSizeY() / 2.0F - getContainerViewHeight() / 2);
      this.zoomAnimation = true;
      this.applying = true;
      this.photoCropView.onDisappear();
      this.centerImage.setParentView(null);
      this.centerImage.setOrientation(0, true);
      this.ignoreDidSetImage = true;
      this.centerImage.setImageBitmap(localBitmap);
      this.ignoreDidSetImage = false;
      this.centerImage.setParentView(this.containerView);
      return;
      if (this.currentEditMode == 2)
      {
        localBitmap = this.photoFilterView.getBitmap();
        localObject1 = this.photoFilterView.getSavedFilterState();
        break;
      }
      localObject1 = localPhotoSize;
      if (this.currentEditMode != 3) {
        break;
      }
      localBitmap = this.photoPaintView.getBitmap();
      localObject2 = this.photoPaintView.getMasks();
      i = 1;
      localObject1 = localPhotoSize;
      break;
      label502:
      if (this.currentEditMode == 2)
      {
        this.tuneItem.setColorFilter(new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY));
        ((MediaController.PhotoEntry)localObject3).isFiltered = true;
        break label205;
      }
      if (this.currentEditMode != 3) {
        break label205;
      }
      this.paintItem.setColorFilter(new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY));
      ((MediaController.PhotoEntry)localObject3).isPainted = true;
      break label205;
      label576:
      if (i == 0) {
        break label217;
      }
      ((MediaController.PhotoEntry)localObject3).savedFilterState = null;
      break label217;
      label589:
      if (!(localObject3 instanceof MediaController.SearchImage)) {
        break label217;
      }
      localObject3 = (MediaController.SearchImage)localObject3;
      ((MediaController.SearchImage)localObject3).imagePath = FileLoader.getPathToAttach(localPhotoSize, true).toString();
      localPhotoSize = ImageLoader.scaleAndSaveImage(localBitmap, AndroidUtilities.dp(120.0F), AndroidUtilities.dp(120.0F), 70, false, 101, 101);
      if (localPhotoSize != null) {
        ((MediaController.SearchImage)localObject3).thumbPath = FileLoader.getPathToAttach(localPhotoSize, true).toString();
      }
      if (localObject2 != null) {
        ((MediaController.SearchImage)localObject3).stickers.addAll((Collection)localObject2);
      }
      if (this.currentEditMode == 1)
      {
        this.cropItem.setColorFilter(new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY));
        ((MediaController.SearchImage)localObject3).isCropped = true;
      }
      for (;;)
      {
        if (localObject1 == null) {
          break label804;
        }
        ((MediaController.SearchImage)localObject3).savedFilterState = ((MediaController.SavedFilterState)localObject1);
        break;
        if (this.currentEditMode == 2)
        {
          this.tuneItem.setColorFilter(new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY));
          ((MediaController.SearchImage)localObject3).isFiltered = true;
        }
        else if (this.currentEditMode == 3)
        {
          this.paintItem.setColorFilter(new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY));
          ((MediaController.SearchImage)localObject3).isPainted = true;
        }
      }
      label804:
      if (i == 0) {
        break label217;
      }
      ((MediaController.SearchImage)localObject3).savedFilterState = null;
      break label217;
      label817:
      f1 = f2;
    }
  }
  
  private boolean checkAnimation()
  {
    boolean bool = false;
    if ((this.animationInProgress != 0) && (Math.abs(this.transitionAnimationStartTime - System.currentTimeMillis()) >= 500L))
    {
      if (this.animationEndRunnable != null)
      {
        this.animationEndRunnable.run();
        this.animationEndRunnable = null;
      }
      this.animationInProgress = 0;
    }
    if (this.animationInProgress != 0) {
      bool = true;
    }
    return bool;
  }
  
  private void checkBufferedProgress(float paramFloat)
  {
    if ((!this.isStreaming) || (this.parentActivity == null) || (this.streamingAlertShown) || (this.videoPlayer == null) || (this.currentMessageObject == null)) {}
    TLRPC.Document localDocument;
    do
    {
      return;
      localDocument = this.currentMessageObject.getDocument();
    } while ((localDocument == null) || (paramFloat >= 0.9F) || ((localDocument.size * paramFloat < 5242880.0F) && (paramFloat < 0.5F)) || (Math.abs(SystemClock.elapsedRealtime() - this.startedPlayTime) < 2000L));
    if (this.videoPlayer.getDuration() == -9223372036854775807L) {
      Toast.makeText(this.parentActivity, LocaleController.getString("VideoDoesNotSupportStreaming", 2131494578), 1).show();
    }
    this.streamingAlertShown = true;
  }
  
  private boolean checkInlinePermissions()
  {
    if (this.parentActivity == null) {
      return false;
    }
    if ((Build.VERSION.SDK_INT < 23) || (Settings.canDrawOverlays(this.parentActivity))) {
      return true;
    }
    new AlertDialog.Builder(this.parentActivity).setTitle(LocaleController.getString("AppName", 2131492981)).setMessage(LocaleController.getString("PermissionDrawAboveOtherApps", 2131494141)).setPositiveButton(LocaleController.getString("PermissionOpenSettings", 2131494147), new DialogInterface.OnClickListener()
    {
      @TargetApi(23)
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        if (PhotoViewer.this.parentActivity != null) {}
        try
        {
          PhotoViewer.this.parentActivity.startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + PhotoViewer.this.parentActivity.getPackageName())));
          return;
        }
        catch (Exception paramAnonymousDialogInterface)
        {
          FileLog.e(paramAnonymousDialogInterface);
        }
      }
    }).show();
    return false;
  }
  
  private void checkMinMax(boolean paramBoolean)
  {
    float f1 = this.translationX;
    float f2 = this.translationY;
    updateMinMax(this.scale);
    if (this.translationX < this.minX)
    {
      f1 = this.minX;
      if (this.translationY >= this.minY) {
        break label84;
      }
      f2 = this.minY;
    }
    for (;;)
    {
      animateTo(this.scale, f1, f2, paramBoolean);
      return;
      if (this.translationX <= this.maxX) {
        break;
      }
      f1 = this.maxX;
      break;
      label84:
      if (this.translationY > this.maxY) {
        f2 = this.maxY;
      }
    }
  }
  
  private void checkProgress(int paramInt, boolean paramBoolean)
  {
    int j = this.currentIndex;
    int i;
    if (paramInt == 1) {
      i = j + 1;
    }
    Object localObject1;
    while (this.currentFileNames[paramInt] != null)
    {
      Object localObject3 = null;
      Object localObject2 = null;
      MessageObject localMessageObject = null;
      localObject1 = null;
      bool1 = false;
      bool2 = false;
      j = 0;
      if (this.currentMessageObject != null)
      {
        if ((i < 0) || (i >= this.imagesArr.size()))
        {
          this.photoProgressViews[paramInt].setBackgroundState(-1, paramBoolean);
          return;
          i = j;
          if (paramInt != 2) {
            continue;
          }
          i = j - 1;
          continue;
        }
        localMessageObject = (MessageObject)this.imagesArr.get(i);
        if (!TextUtils.isEmpty(localMessageObject.messageOwner.attachPath))
        {
          localObject2 = new File(localMessageObject.messageOwner.attachPath);
          localObject1 = localObject2;
          if (!((File)localObject2).exists()) {
            localObject1 = null;
          }
        }
        localObject2 = localObject1;
        if (localObject1 == null)
        {
          if (((localMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && (localMessageObject.messageOwner.media.webpage != null) && (localMessageObject.messageOwner.media.webpage.document == null)) {
            localObject2 = FileLoader.getPathToAttach(getFileLocation(i, null), true);
          }
        }
        else
        {
          label221:
          if ((!SharedConfig.streamMedia) || (!localMessageObject.isVideo()) || ((int)localMessageObject.getDialogId() == 0)) {
            break label392;
          }
          i = 1;
          label246:
          bool1 = localMessageObject.isVideo();
          localObject1 = localObject2;
          label257:
          bool2 = ((File)localObject1).exists();
          if ((localObject1 == null) || ((!bool2) && (i == 0))) {
            break label905;
          }
          if (!bool1) {
            break label867;
          }
          this.photoProgressViews[paramInt].setBackgroundState(3, paramBoolean);
          label294:
          if (paramInt == 0)
          {
            if (bool2) {
              break label893;
            }
            if (FileLoader.getInstance(this.currentAccount).isLoadingFile(this.currentFileNames[paramInt])) {
              break label881;
            }
            this.menuItem.hideSubItem(7);
          }
          label331:
          if (paramInt != 0) {
            break label985;
          }
          if ((this.imagesArrLocals.isEmpty()) && ((this.currentFileNames[0] == null) || (bool1) || (this.photoProgressViews[0].backgroundState == 0))) {
            break label1015;
          }
        }
      }
      label392:
      label487:
      label867:
      label881:
      label893:
      label905:
      label985:
      label1015:
      for (paramBoolean = true;; paramBoolean = false)
      {
        this.canZoom = paramBoolean;
        return;
        localObject2 = FileLoader.getPathToMessage(localMessageObject.messageOwner);
        break label221;
        i = 0;
        break label246;
        if (this.currentBotInlineResult != null)
        {
          if ((i < 0) || (i >= this.imagesArrLocals.size()))
          {
            this.photoProgressViews[paramInt].setBackgroundState(-1, paramBoolean);
            return;
          }
          localObject1 = (TLRPC.BotInlineResult)this.imagesArrLocals.get(i);
          if ((((TLRPC.BotInlineResult)localObject1).type.equals("video")) || (MessageObject.isVideoDocument(((TLRPC.BotInlineResult)localObject1).document))) {
            if (((TLRPC.BotInlineResult)localObject1).document != null)
            {
              localObject2 = FileLoader.getPathToAttach(((TLRPC.BotInlineResult)localObject1).document);
              bool2 = true;
            }
          }
          for (;;)
          {
            if (localObject2 != null)
            {
              i = j;
              localObject1 = localObject2;
              bool1 = bool2;
              if (((File)localObject2).exists()) {
                break;
              }
            }
            localObject1 = new File(FileLoader.getDirectory(4), this.currentFileNames[paramInt]);
            i = j;
            bool1 = bool2;
            break;
            if (!(((TLRPC.BotInlineResult)localObject1).content instanceof TLRPC.TL_webDocument)) {
              break label487;
            }
            localObject2 = new File(FileLoader.getDirectory(4), Utilities.MD5(((TLRPC.BotInlineResult)localObject1).content.url) + "." + ImageLoader.getHttpUrlExtension(((TLRPC.BotInlineResult)localObject1).content.url, "mp4"));
            break label487;
            if (((TLRPC.BotInlineResult)localObject1).document != null)
            {
              localObject2 = new File(FileLoader.getDirectory(3), this.currentFileNames[paramInt]);
              bool2 = bool1;
            }
            else
            {
              localObject2 = localMessageObject;
              bool2 = bool1;
              if (((TLRPC.BotInlineResult)localObject1).photo != null)
              {
                localObject2 = new File(FileLoader.getDirectory(0), this.currentFileNames[paramInt]);
                bool2 = bool1;
              }
            }
          }
        }
        if (this.currentFileLocation != null)
        {
          if ((i < 0) || (i >= this.imagesArrLocations.size()))
          {
            this.photoProgressViews[paramInt].setBackgroundState(-1, paramBoolean);
            return;
          }
          localObject1 = (TLRPC.FileLocation)this.imagesArrLocations.get(i);
          if ((this.avatarsDialogId != 0) || (this.isEvent)) {}
          for (bool1 = true;; bool1 = false)
          {
            localObject1 = FileLoader.getPathToAttach((TLObject)localObject1, bool1);
            i = j;
            bool1 = bool2;
            break;
          }
        }
        i = j;
        localObject1 = localObject3;
        bool1 = bool2;
        if (this.currentPathObject == null) {
          break label257;
        }
        localObject2 = new File(FileLoader.getDirectory(3), this.currentFileNames[paramInt]);
        i = j;
        localObject1 = localObject2;
        bool1 = bool2;
        if (((File)localObject2).exists()) {
          break label257;
        }
        localObject1 = new File(FileLoader.getDirectory(4), this.currentFileNames[paramInt]);
        i = j;
        bool1 = bool2;
        break label257;
        this.photoProgressViews[paramInt].setBackgroundState(-1, paramBoolean);
        break label294;
        this.menuItem.showSubItem(7);
        break label331;
        this.menuItem.hideSubItem(7);
        break label331;
        if (bool1) {
          if (!FileLoader.getInstance(this.currentAccount).isLoadingFile(this.currentFileNames[paramInt])) {
            this.photoProgressViews[paramInt].setBackgroundState(2, false);
          }
        }
        for (;;)
        {
          localObject2 = ImageLoader.getInstance().getFileProgress(this.currentFileNames[paramInt]);
          localObject1 = localObject2;
          if (localObject2 == null) {
            localObject1 = Float.valueOf(0.0F);
          }
          this.photoProgressViews[paramInt].setProgress(((Float)localObject1).floatValue(), false);
          break label331;
          break;
          this.photoProgressViews[paramInt].setBackgroundState(1, false);
          continue;
          this.photoProgressViews[paramInt].setBackgroundState(0, paramBoolean);
        }
      }
    }
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (!this.imagesArrLocals.isEmpty())
    {
      bool1 = bool2;
      if (i >= 0)
      {
        bool1 = bool2;
        if (i < this.imagesArrLocals.size())
        {
          localObject1 = this.imagesArrLocals.get(i);
          bool1 = bool2;
          if ((localObject1 instanceof MediaController.PhotoEntry)) {
            bool1 = ((MediaController.PhotoEntry)localObject1).isVideo;
          }
        }
      }
    }
    if (bool1)
    {
      this.photoProgressViews[paramInt].setBackgroundState(3, paramBoolean);
      return;
    }
    this.photoProgressViews[paramInt].setBackgroundState(-1, paramBoolean);
  }
  
  private ByteArrayInputStream cleanBuffer(byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte = new byte[paramArrayOfByte.length];
    int j = 0;
    int i = 0;
    while (j < paramArrayOfByte.length) {
      if ((paramArrayOfByte[j] == 0) && (paramArrayOfByte[(j + 1)] == 0) && (paramArrayOfByte[(j + 2)] == 3))
      {
        arrayOfByte[i] = 0;
        arrayOfByte[(i + 1)] = 0;
        j += 3;
        i += 2;
      }
      else
      {
        arrayOfByte[i] = paramArrayOfByte[j];
        j += 1;
        i += 1;
      }
    }
    return new ByteArrayInputStream(arrayOfByte, 0, i);
  }
  
  private void closeCaptionEnter(boolean paramBoolean)
  {
    if ((this.currentIndex < 0) || (this.currentIndex >= this.imagesArrLocals.size())) {
      return;
    }
    Object localObject2 = this.imagesArrLocals.get(this.currentIndex);
    Object localObject1 = this.captionEditText.getFieldCharSequence();
    CharSequence[] arrayOfCharSequence = new CharSequence[1];
    arrayOfCharSequence[0] = localObject1;
    Object localObject3;
    if (paramBoolean)
    {
      localObject1 = DataQuery.getInstance(this.currentAccount).getEntities(arrayOfCharSequence);
      if (!(localObject2 instanceof MediaController.PhotoEntry)) {
        break label232;
      }
      localObject3 = (MediaController.PhotoEntry)localObject2;
      ((MediaController.PhotoEntry)localObject3).caption = arrayOfCharSequence[0];
      ((MediaController.PhotoEntry)localObject3).entities = ((ArrayList)localObject1);
      if ((this.captionEditText.getFieldCharSequence().length() != 0) && (!this.placeProvider.isPhotoChecked(this.currentIndex))) {
        setPhotoChecked();
      }
    }
    this.captionEditText.setTag(null);
    if (this.lastTitle != null)
    {
      this.actionBar.setTitle(this.lastTitle);
      this.lastTitle = null;
    }
    if (this.isCurrentVideo)
    {
      localObject3 = this.actionBar;
      if (!this.muteVideo) {
        break label263;
      }
    }
    label232:
    label263:
    for (localObject1 = null;; localObject1 = this.currentSubtitle)
    {
      ((ActionBar)localObject3).setSubtitle((CharSequence)localObject1);
      updateCaptionTextForCurrentPhoto(localObject2);
      setCurrentCaption(null, arrayOfCharSequence[0], false);
      if (this.captionEditText.isPopupShowing()) {
        this.captionEditText.hidePopup();
      }
      this.captionEditText.closeKeyboard();
      return;
      if (!(localObject2 instanceof MediaController.SearchImage)) {
        break;
      }
      localObject3 = (MediaController.SearchImage)localObject2;
      ((MediaController.SearchImage)localObject3).caption = arrayOfCharSequence[0];
      ((MediaController.SearchImage)localObject3).entities = ((ArrayList)localObject1);
      break;
    }
  }
  
  private TextView createCaptionTextView()
  {
    TextView local41 = new TextView(this.actvityContext)
    {
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return (PhotoViewer.this.bottomTouchEnabled) && (super.onTouchEvent(paramAnonymousMotionEvent));
      }
    };
    local41.setMovementMethod(new LinkMovementMethodMy(null));
    local41.setPadding(AndroidUtilities.dp(20.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(20.0F), AndroidUtilities.dp(8.0F));
    local41.setLinkTextColor(-1);
    local41.setTextColor(-1);
    local41.setHighlightColor(872415231);
    local41.setEllipsize(TextUtils.TruncateAt.END);
    if (LocaleController.isRTL) {}
    for (int i = 5;; i = 3)
    {
      local41.setGravity(i | 0x10);
      local41.setTextSize(1, 16.0F);
      local41.setVisibility(4);
      local41.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (!PhotoViewer.this.needCaptionLayout) {
            return;
          }
          PhotoViewer.this.openCaptionEnter();
        }
      });
      return local41;
    }
  }
  
  private void createVideoControlsInterface()
  {
    this.videoPlayerSeekbar = new SeekBar(this.containerView.getContext());
    this.videoPlayerSeekbar.setLineHeight(AndroidUtilities.dp(4.0F));
    this.videoPlayerSeekbar.setColors(1728053247, 1728053247, -2764585, -1, -1);
    this.videoPlayerSeekbar.setDelegate(new SeekBar.SeekBarDelegate()
    {
      public void onSeekBarDrag(float paramAnonymousFloat)
      {
        float f;
        long l;
        if (PhotoViewer.this.videoPlayer != null)
        {
          f = paramAnonymousFloat;
          if (!PhotoViewer.this.inPreview)
          {
            f = paramAnonymousFloat;
            if (PhotoViewer.this.videoTimelineView.getVisibility() == 0) {
              f = PhotoViewer.this.videoTimelineView.getLeftProgress() + (PhotoViewer.this.videoTimelineView.getRightProgress() - PhotoViewer.this.videoTimelineView.getLeftProgress()) * paramAnonymousFloat;
            }
          }
          l = PhotoViewer.this.videoPlayer.getDuration();
          if (l == -9223372036854775807L) {
            PhotoViewer.access$1102(PhotoViewer.this, f);
          }
        }
        else
        {
          return;
        }
        PhotoViewer.this.videoPlayer.seekTo((int)((float)l * f));
      }
    });
    this.videoPlayerControlFrameLayout = new FrameLayout(this.containerView.getContext())
    {
      protected void onDraw(Canvas paramAnonymousCanvas)
      {
        paramAnonymousCanvas.save();
        paramAnonymousCanvas.translate(AndroidUtilities.dp(48.0F), 0.0F);
        PhotoViewer.this.videoPlayerSeekbar.draw(paramAnonymousCanvas);
        paramAnonymousCanvas.restore();
      }
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
        float f1 = 0.0F;
        if (PhotoViewer.this.videoPlayer != null)
        {
          float f2 = (float)PhotoViewer.this.videoPlayer.getCurrentPosition() / (float)PhotoViewer.this.videoPlayer.getDuration();
          f1 = f2;
          if (!PhotoViewer.this.inPreview)
          {
            f1 = f2;
            if (PhotoViewer.this.videoTimelineView.getVisibility() == 0)
            {
              f2 -= PhotoViewer.this.videoTimelineView.getLeftProgress();
              f1 = f2;
              if (f2 < 0.0F) {
                f1 = 0.0F;
              }
              f2 = f1 / (PhotoViewer.this.videoTimelineView.getRightProgress() - PhotoViewer.this.videoTimelineView.getLeftProgress());
              f1 = f2;
              if (f2 > 1.0F) {
                f1 = 1.0F;
              }
            }
          }
        }
        PhotoViewer.this.videoPlayerSeekbar.setProgress(f1);
        PhotoViewer.this.videoTimelineView.setProgress(f1);
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        super.onMeasure(paramAnonymousInt1, paramAnonymousInt2);
        if (PhotoViewer.this.videoPlayer != null)
        {
          long l2 = PhotoViewer.this.videoPlayer.getDuration();
          l1 = l2;
          if (l2 != -9223372036854775807L) {}
        }
        for (long l1 = 0L;; l1 = 0L)
        {
          l1 /= 1000L;
          paramAnonymousInt1 = (int)Math.ceil(PhotoViewer.this.videoPlayerTime.getPaint().measureText(String.format("%02d:%02d / %02d:%02d", new Object[] { Long.valueOf(l1 / 60L), Long.valueOf(l1 % 60L), Long.valueOf(l1 / 60L), Long.valueOf(l1 % 60L) })));
          PhotoViewer.this.videoPlayerSeekbar.setSize(getMeasuredWidth() - AndroidUtilities.dp(64.0F) - paramAnonymousInt1, getMeasuredHeight());
          return;
        }
      }
      
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        int i = (int)paramAnonymousMotionEvent.getX();
        i = (int)paramAnonymousMotionEvent.getY();
        if (PhotoViewer.this.videoPlayerSeekbar.onTouch(paramAnonymousMotionEvent.getAction(), paramAnonymousMotionEvent.getX() - AndroidUtilities.dp(48.0F), paramAnonymousMotionEvent.getY()))
        {
          getParent().requestDisallowInterceptTouchEvent(true);
          invalidate();
        }
        return true;
      }
    };
    this.videoPlayerControlFrameLayout.setWillNotDraw(false);
    this.bottomLayout.addView(this.videoPlayerControlFrameLayout, LayoutHelper.createFrame(-1, -1, 51));
    this.videoPlayButton = new ImageView(this.containerView.getContext());
    this.videoPlayButton.setScaleType(ImageView.ScaleType.CENTER);
    this.videoPlayerControlFrameLayout.addView(this.videoPlayButton, LayoutHelper.createFrame(48, 48.0F, 51, 4.0F, 0.0F, 0.0F, 0.0F));
    this.videoPlayButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (PhotoViewer.this.videoPlayer == null) {
          return;
        }
        if (PhotoViewer.this.isPlaying)
        {
          PhotoViewer.this.videoPlayer.pause();
          PhotoViewer.this.containerView.invalidate();
          return;
        }
        if (PhotoViewer.this.isCurrentVideo) {
          if ((Math.abs(PhotoViewer.this.videoTimelineView.getProgress() - 1.0F) < 0.01F) || (PhotoViewer.this.videoPlayer.getCurrentPosition() == PhotoViewer.this.videoPlayer.getDuration())) {
            PhotoViewer.this.videoPlayer.seekTo(0L);
          }
        }
        for (;;)
        {
          PhotoViewer.this.videoPlayer.play();
          break;
          if ((Math.abs(PhotoViewer.this.videoPlayerSeekbar.getProgress() - 1.0F) < 0.01F) || (PhotoViewer.this.videoPlayer.getCurrentPosition() == PhotoViewer.this.videoPlayer.getDuration())) {
            PhotoViewer.this.videoPlayer.seekTo(0L);
          }
        }
      }
    });
    this.videoPlayerTime = new SimpleTextView(this.containerView.getContext());
    this.videoPlayerTime.setTextColor(-1);
    this.videoPlayerTime.setGravity(53);
    this.videoPlayerTime.setTextSize(13);
    this.videoPlayerControlFrameLayout.addView(this.videoPlayerTime, LayoutHelper.createFrame(-2, -1.0F, 53, 0.0F, 17.0F, 7.0F, 0.0F));
  }
  
  private void didChangedCompressionLevel(boolean paramBoolean)
  {
    SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
    localEditor.putInt("compress_video2", this.selectedCompression);
    localEditor.commit();
    updateWidthHeightBitrateForCompression();
    updateVideoInfo();
    if (paramBoolean) {
      requestVideoPreview(1);
    }
  }
  
  private void dismissInternal()
  {
    try
    {
      if (this.windowView.getParent() != null)
      {
        ((LaunchActivity)this.parentActivity).drawerLayoutContainer.setAllowDrawContent(true);
        ((WindowManager)this.parentActivity.getSystemService("window")).removeView(this.windowView);
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  private int getAdditionX()
  {
    if ((this.currentEditMode != 0) && (this.currentEditMode != 3)) {
      return AndroidUtilities.dp(14.0F);
    }
    return 0;
  }
  
  private int getAdditionY()
  {
    int k = 0;
    int j = 0;
    int i = 0;
    if (this.currentEditMode == 3)
    {
      j = AndroidUtilities.dp(8.0F);
      if (Build.VERSION.SDK_INT >= 21) {
        i = AndroidUtilities.statusBarHeight;
      }
      i += j;
    }
    do
    {
      return i;
      i = k;
    } while (this.currentEditMode == 0);
    k = AndroidUtilities.dp(14.0F);
    i = j;
    if (Build.VERSION.SDK_INT >= 21) {
      i = AndroidUtilities.statusBarHeight;
    }
    return i + k;
  }
  
  private int getContainerViewHeight()
  {
    return getContainerViewHeight(this.currentEditMode);
  }
  
  private int getContainerViewHeight(int paramInt)
  {
    int j = AndroidUtilities.displaySize.y;
    int i = j;
    if (paramInt == 0)
    {
      i = j;
      if (Build.VERSION.SDK_INT >= 21) {
        i = j + AndroidUtilities.statusBarHeight;
      }
    }
    if (paramInt == 1) {
      j = i - AndroidUtilities.dp(144.0F);
    }
    do
    {
      return j;
      if (paramInt == 2) {
        return i - AndroidUtilities.dp(214.0F);
      }
      j = i;
    } while (paramInt != 3);
    return i - (AndroidUtilities.dp(48.0F) + ActionBar.getCurrentActionBarHeight());
  }
  
  private int getContainerViewWidth()
  {
    return getContainerViewWidth(this.currentEditMode);
  }
  
  private int getContainerViewWidth(int paramInt)
  {
    int j = this.containerView.getWidth();
    int i = j;
    if (paramInt != 0)
    {
      i = j;
      if (paramInt != 3) {
        i = j - AndroidUtilities.dp(28.0F);
      }
    }
    return i;
  }
  
  private VideoEditedInfo getCurrentVideoEditedInfo()
  {
    int i = -1;
    if ((!this.isCurrentVideo) || (this.currentPlayingVideoFile == null) || (this.compressionsCount == 0)) {
      return null;
    }
    VideoEditedInfo localVideoEditedInfo = new VideoEditedInfo();
    localVideoEditedInfo.startTime = this.startTime;
    localVideoEditedInfo.endTime = this.endTime;
    localVideoEditedInfo.rotationValue = this.rotationValue;
    localVideoEditedInfo.originalWidth = this.originalWidth;
    localVideoEditedInfo.originalHeight = this.originalHeight;
    localVideoEditedInfo.bitrate = this.bitrate;
    localVideoEditedInfo.originalPath = this.currentPlayingVideoFile.getPath();
    localVideoEditedInfo.estimatedSize = this.estimatedSize;
    localVideoEditedInfo.estimatedDuration = this.estimatedDuration;
    localVideoEditedInfo.framerate = this.videoFramerate;
    if ((!this.muteVideo) && ((this.compressItem.getTag() == null) || (this.selectedCompression == this.compressionsCount - 1)))
    {
      localVideoEditedInfo.resultWidth = this.originalWidth;
      localVideoEditedInfo.resultHeight = this.originalHeight;
      if (this.muteVideo) {}
      for (;;)
      {
        localVideoEditedInfo.bitrate = i;
        localVideoEditedInfo.muted = this.muteVideo;
        return localVideoEditedInfo;
        i = this.originalBitrate;
      }
    }
    if (this.muteVideo)
    {
      this.selectedCompression = 1;
      updateWidthHeightBitrateForCompression();
    }
    localVideoEditedInfo.resultWidth = this.resultWidth;
    localVideoEditedInfo.resultHeight = this.resultHeight;
    if (this.muteVideo) {}
    for (;;)
    {
      localVideoEditedInfo.bitrate = i;
      localVideoEditedInfo.muted = this.muteVideo;
      return localVideoEditedInfo;
      i = this.bitrate;
    }
  }
  
  private TLObject getFileLocation(int paramInt, int[] paramArrayOfInt)
  {
    if (paramInt < 0) {}
    Object localObject;
    do
    {
      do
      {
        do
        {
          do
          {
            do
            {
              return null;
              if (this.imagesArrLocations.isEmpty()) {
                break;
              }
            } while (paramInt >= this.imagesArrLocations.size());
            if (paramArrayOfInt != null) {
              paramArrayOfInt[0] = ((Integer)this.imagesArrLocationsSizes.get(paramInt)).intValue();
            }
            return (TLObject)this.imagesArrLocations.get(paramInt);
          } while ((this.imagesArr.isEmpty()) || (paramInt >= this.imagesArr.size()));
          localObject = (MessageObject)this.imagesArr.get(paramInt);
          if (!(((MessageObject)localObject).messageOwner instanceof TLRPC.TL_messageService)) {
            break;
          }
          if ((((MessageObject)localObject).messageOwner.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto)) {
            return ((MessageObject)localObject).messageOwner.action.newUserPhoto.photo_big;
          }
          localObject = FileLoader.getClosestPhotoSizeWithSize(((MessageObject)localObject).photoThumbs, AndroidUtilities.getPhotoSize());
          if (localObject != null)
          {
            if (paramArrayOfInt != null)
            {
              paramArrayOfInt[0] = ((TLRPC.PhotoSize)localObject).size;
              if (paramArrayOfInt[0] == 0) {
                paramArrayOfInt[0] = -1;
              }
            }
            return ((TLRPC.PhotoSize)localObject).location;
          }
        } while (paramArrayOfInt == null);
        paramArrayOfInt[0] = -1;
        return null;
        if (((!(((MessageObject)localObject).messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) || (((MessageObject)localObject).messageOwner.media.photo == null)) && ((!(((MessageObject)localObject).messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) || (((MessageObject)localObject).messageOwner.media.webpage == null))) {
          break;
        }
        localObject = FileLoader.getClosestPhotoSizeWithSize(((MessageObject)localObject).photoThumbs, AndroidUtilities.getPhotoSize());
        if (localObject != null)
        {
          if (paramArrayOfInt != null)
          {
            paramArrayOfInt[0] = ((TLRPC.PhotoSize)localObject).size;
            if (paramArrayOfInt[0] == 0) {
              paramArrayOfInt[0] = -1;
            }
          }
          return ((TLRPC.PhotoSize)localObject).location;
        }
      } while (paramArrayOfInt == null);
      paramArrayOfInt[0] = -1;
      return null;
      if ((((MessageObject)localObject).messageOwner.media instanceof TLRPC.TL_messageMediaInvoice)) {
        return ((TLRPC.TL_messageMediaInvoice)((MessageObject)localObject).messageOwner.media).photo;
      }
    } while ((((MessageObject)localObject).getDocument() == null) || (((MessageObject)localObject).getDocument().thumb == null));
    if (paramArrayOfInt != null)
    {
      paramArrayOfInt[0] = ((MessageObject)localObject).getDocument().thumb.size;
      if (paramArrayOfInt[0] == 0) {
        paramArrayOfInt[0] = -1;
      }
    }
    return ((MessageObject)localObject).getDocument().thumb.location;
  }
  
  private String getFileName(int paramInt)
  {
    if (paramInt < 0) {}
    Object localObject;
    label135:
    do
    {
      do
      {
        do
        {
          do
          {
            do
            {
              return null;
              if ((this.imagesArrLocations.isEmpty()) && (this.imagesArr.isEmpty())) {
                break label135;
              }
              if (this.imagesArrLocations.isEmpty()) {
                break;
              }
            } while (paramInt >= this.imagesArrLocations.size());
            localObject = (TLRPC.FileLocation)this.imagesArrLocations.get(paramInt);
            return ((TLRPC.FileLocation)localObject).volume_id + "_" + ((TLRPC.FileLocation)localObject).local_id + ".jpg";
          } while ((this.imagesArr.isEmpty()) || (paramInt >= this.imagesArr.size()));
          return FileLoader.getMessageFileName(((MessageObject)this.imagesArr.get(paramInt)).messageOwner);
        } while ((this.imagesArrLocals.isEmpty()) || (paramInt >= this.imagesArrLocals.size()));
        localObject = this.imagesArrLocals.get(paramInt);
        if ((localObject instanceof MediaController.SearchImage))
        {
          localObject = (MediaController.SearchImage)localObject;
          if (((MediaController.SearchImage)localObject).document != null) {
            return FileLoader.getAttachFileName(((MediaController.SearchImage)localObject).document);
          }
          if ((((MediaController.SearchImage)localObject).type != 1) && (((MediaController.SearchImage)localObject).localUrl != null) && (((MediaController.SearchImage)localObject).localUrl.length() > 0))
          {
            File localFile = new File(((MediaController.SearchImage)localObject).localUrl);
            if (localFile.exists()) {
              return localFile.getName();
            }
            ((MediaController.SearchImage)localObject).localUrl = "";
          }
          return Utilities.MD5(((MediaController.SearchImage)localObject).imageUrl) + "." + ImageLoader.getHttpUrlExtension(((MediaController.SearchImage)localObject).imageUrl, "jpg");
        }
      } while (!(localObject instanceof TLRPC.BotInlineResult));
      localObject = (TLRPC.BotInlineResult)localObject;
      if (((TLRPC.BotInlineResult)localObject).document != null) {
        return FileLoader.getAttachFileName(((TLRPC.BotInlineResult)localObject).document);
      }
      if (((TLRPC.BotInlineResult)localObject).photo != null) {
        return FileLoader.getAttachFileName(FileLoader.getClosestPhotoSizeWithSize(((TLRPC.BotInlineResult)localObject).photo.sizes, AndroidUtilities.getPhotoSize()));
      }
    } while (!(((TLRPC.BotInlineResult)localObject).content instanceof TLRPC.TL_webDocument));
    return Utilities.MD5(((TLRPC.BotInlineResult)localObject).content.url) + "." + ImageLoader.getHttpUrlExtension(((TLRPC.BotInlineResult)localObject).content.url, FileLoader.getExtensionByMime(((TLRPC.BotInlineResult)localObject).content.mime_type));
  }
  
  public static PhotoViewer getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          PhotoViewer localPhotoViewer2 = Instance;
          localObject1 = localPhotoViewer2;
          if (localPhotoViewer2 == null) {
            localObject1 = new PhotoViewer();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (PhotoViewer)localObject1;
          return (PhotoViewer)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localPhotoViewer1;
  }
  
  private int getLeftInset()
  {
    if ((this.lastInsets != null) && (Build.VERSION.SDK_INT >= 21)) {
      return ((WindowInsets)this.lastInsets).getSystemWindowInsetLeft();
    }
    return 0;
  }
  
  public static PhotoViewer getPipInstance()
  {
    return PipInstance;
  }
  
  private void goToNext()
  {
    float f = 0.0F;
    if (this.scale != 1.0F) {
      f = (getContainerViewWidth() - this.centerImage.getImageWidth()) / 2 * this.scale;
    }
    this.switchImageAfterAnimation = 1;
    animateTo(this.scale, this.minX - getContainerViewWidth() - f - AndroidUtilities.dp(30.0F) / 2, this.translationY, false);
  }
  
  private void goToPrev()
  {
    float f = 0.0F;
    if (this.scale != 1.0F) {
      f = (getContainerViewWidth() - this.centerImage.getImageWidth()) / 2 * this.scale;
    }
    this.switchImageAfterAnimation = 2;
    animateTo(this.scale, this.maxX + getContainerViewWidth() + f + AndroidUtilities.dp(30.0F) / 2, this.translationY, false);
  }
  
  public static boolean hasInstance()
  {
    return Instance != null;
  }
  
  private void hideHint()
  {
    this.hintAnimation = new AnimatorSet();
    this.hintAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.hintTextView, "alpha", new float[] { 0.0F }) });
    this.hintAnimation.addListener(new AnimatorListenerAdapter()
    {
      public void onAnimationCancel(Animator paramAnonymousAnimator)
      {
        if (paramAnonymousAnimator.equals(PhotoViewer.this.hintAnimation))
        {
          PhotoViewer.access$17102(PhotoViewer.this, null);
          PhotoViewer.access$17102(PhotoViewer.this, null);
        }
      }
      
      public void onAnimationEnd(Animator paramAnonymousAnimator)
      {
        if (paramAnonymousAnimator.equals(PhotoViewer.this.hintAnimation))
        {
          PhotoViewer.access$17002(PhotoViewer.this, null);
          PhotoViewer.access$17102(PhotoViewer.this, null);
          if (PhotoViewer.this.hintTextView != null) {
            PhotoViewer.this.hintTextView.setVisibility(8);
          }
        }
      }
    });
    this.hintAnimation.setDuration(300L);
    this.hintAnimation.start();
  }
  
  public static boolean isShowingImage(String paramString)
  {
    boolean bool = false;
    if (Instance != null)
    {
      if ((Instance.isVisible) && (!Instance.disableShowCheck) && (paramString != null) && (Instance.currentPathObject != null) && (paramString.equals(Instance.currentPathObject))) {
        bool = true;
      }
    }
    else {
      return bool;
    }
    return false;
  }
  
  public static boolean isShowingImage(MessageObject paramMessageObject)
  {
    boolean bool1 = false;
    if (Instance != null) {
      if ((Instance.pipAnimationInProgress) || (!Instance.isVisible) || (Instance.disableShowCheck) || (paramMessageObject == null) || (Instance.currentMessageObject == null) || (Instance.currentMessageObject.getId() != paramMessageObject.getId())) {
        break label131;
      }
    }
    label131:
    for (bool1 = true;; bool1 = false)
    {
      boolean bool2 = bool1;
      if (!bool1)
      {
        bool2 = bool1;
        if (PipInstance != null)
        {
          if ((!PipInstance.isVisible) || (PipInstance.disableShowCheck) || (paramMessageObject == null) || (PipInstance.currentMessageObject == null) || (PipInstance.currentMessageObject.getId() != paramMessageObject.getId())) {
            break;
          }
          bool2 = true;
        }
      }
      return bool2;
    }
    return false;
  }
  
  public static boolean isShowingImage(TLRPC.BotInlineResult paramBotInlineResult)
  {
    boolean bool = false;
    if (Instance != null)
    {
      if ((Instance.isVisible) && (!Instance.disableShowCheck) && (paramBotInlineResult != null) && (Instance.currentBotInlineResult != null) && (paramBotInlineResult.id == Instance.currentBotInlineResult.id)) {
        bool = true;
      }
    }
    else {
      return bool;
    }
    return false;
  }
  
  public static boolean isShowingImage(TLRPC.FileLocation paramFileLocation)
  {
    boolean bool = false;
    if (Instance != null)
    {
      if ((Instance.isVisible) && (!Instance.disableShowCheck) && (paramFileLocation != null) && (Instance.currentFileLocation != null) && (paramFileLocation.local_id == Instance.currentFileLocation.local_id) && (paramFileLocation.volume_id == Instance.currentFileLocation.volume_id) && (paramFileLocation.dc_id == Instance.currentFileLocation.dc_id)) {
        bool = true;
      }
    }
    else {
      return bool;
    }
    return false;
  }
  
  private void onActionClick(boolean paramBoolean)
  {
    if (((this.currentMessageObject == null) && (this.currentBotInlineResult == null)) || (this.currentFileNames[0] == null)) {}
    do
    {
      do
      {
        return;
        Object localObject6 = null;
        Object localObject5 = null;
        localObject4 = null;
        Object localObject1 = null;
        this.isStreaming = false;
        Object localObject3;
        if (this.currentMessageObject != null)
        {
          localObject4 = localObject1;
          if (this.currentMessageObject.messageOwner.attachPath != null)
          {
            localObject4 = localObject1;
            if (this.currentMessageObject.messageOwner.attachPath.length() != 0)
            {
              localObject1 = new File(this.currentMessageObject.messageOwner.attachPath);
              localObject4 = localObject1;
              if (!((File)localObject1).exists()) {
                localObject4 = null;
              }
            }
          }
          localObject1 = localObject4;
          localObject3 = localObject5;
          if (localObject4 == null)
          {
            localObject4 = FileLoader.getPathToMessage(this.currentMessageObject.messageOwner);
            localObject1 = localObject4;
            localObject3 = localObject5;
            if (!((File)localObject4).exists())
            {
              localObject4 = null;
              localObject1 = localObject4;
              localObject3 = localObject5;
              if (SharedConfig.streamMedia)
              {
                localObject1 = localObject4;
                localObject3 = localObject5;
                if ((int)this.currentMessageObject.getDialogId() != 0)
                {
                  localObject1 = localObject4;
                  localObject3 = localObject5;
                  if (this.currentMessageObject.isVideo())
                  {
                    localObject1 = localObject4;
                    localObject3 = localObject5;
                    if (this.currentMessageObject.canStreamVideo()) {
                      localObject3 = localObject6;
                    }
                  }
                }
              }
            }
          }
        }
        for (;;)
        {
          try
          {
            FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.getDocument(), true, 0);
            localObject3 = localObject6;
            localObject1 = this.currentMessageObject.getDocument();
            localObject3 = localObject6;
            localObject1 = "?account=" + this.currentMessageObject.currentAccount + "&id=" + ((TLRPC.Document)localObject1).id + "&hash=" + ((TLRPC.Document)localObject1).access_hash + "&dc=" + ((TLRPC.Document)localObject1).dc_id + "&size=" + ((TLRPC.Document)localObject1).size + "&mime=" + URLEncoder.encode(((TLRPC.Document)localObject1).mime_type, "UTF-8") + "&name=" + URLEncoder.encode(FileLoader.getDocumentFileName((TLRPC.Document)localObject1), "UTF-8");
            localObject3 = localObject6;
            localObject1 = Uri.parse("tg://" + this.currentMessageObject.getFileName() + (String)localObject1);
            localObject3 = localObject1;
            this.isStreaming = true;
            localObject3 = localObject1;
            checkProgress(0, false);
            localObject3 = localObject1;
            localObject1 = localObject4;
          }
          catch (Exception localException)
          {
            Object localObject2 = localObject4;
            continue;
          }
          localObject4 = localObject3;
          if (localObject1 != null)
          {
            localObject4 = localObject3;
            if (localObject3 == null) {
              localObject4 = Uri.fromFile((File)localObject1);
            }
          }
          if (localObject4 != null) {
            continue;
          }
          if (!paramBoolean) {
            break;
          }
          if (this.currentMessageObject == null) {
            continue;
          }
          if (FileLoader.getInstance(this.currentAccount).isLoadingFile(this.currentFileNames[0])) {
            continue;
          }
          FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.getDocument(), true, 0);
          return;
          localObject1 = localObject4;
          localObject3 = localObject5;
          if (this.currentBotInlineResult != null) {
            if (this.currentBotInlineResult.document != null)
            {
              localObject4 = FileLoader.getPathToAttach(this.currentBotInlineResult.document);
              localObject1 = localObject4;
              localObject3 = localObject5;
              if (!((File)localObject4).exists())
              {
                localObject1 = null;
                localObject3 = localObject5;
              }
            }
            else
            {
              localObject1 = localObject4;
              localObject3 = localObject5;
              if ((this.currentBotInlineResult.content instanceof TLRPC.TL_webDocument))
              {
                localObject4 = new File(FileLoader.getDirectory(4), Utilities.MD5(this.currentBotInlineResult.content.url) + "." + ImageLoader.getHttpUrlExtension(this.currentBotInlineResult.content.url, "mp4"));
                localObject1 = localObject4;
                localObject3 = localObject5;
                if (!((File)localObject4).exists())
                {
                  localObject1 = null;
                  localObject3 = localObject5;
                }
              }
            }
          }
        }
        FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.currentMessageObject.getDocument());
        return;
      } while (this.currentBotInlineResult == null);
      if (this.currentBotInlineResult.document != null)
      {
        if (!FileLoader.getInstance(this.currentAccount).isLoadingFile(this.currentFileNames[0]))
        {
          FileLoader.getInstance(this.currentAccount).loadFile(this.currentBotInlineResult.document, true, 0);
          return;
        }
        FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.currentBotInlineResult.document);
        return;
      }
    } while (!(this.currentBotInlineResult.content instanceof TLRPC.TL_webDocument));
    if (!ImageLoader.getInstance().isLoadingHttpFile(this.currentBotInlineResult.content.url))
    {
      ImageLoader.getInstance().loadHttpFile(this.currentBotInlineResult.content.url, "mp4", this.currentAccount);
      return;
    }
    ImageLoader.getInstance().cancelLoadHttpFile(this.currentBotInlineResult.content.url);
    return;
    preparePlayer((Uri)localObject4, true, false);
  }
  
  @SuppressLint({"NewApi", "DrawAllocation"})
  private void onDraw(Canvas paramCanvas)
  {
    if ((this.animationInProgress == 1) || ((!this.isVisible) && (this.animationInProgress != 2) && (!this.pipAnimationInProgress))) {
      return;
    }
    float f1 = -1.0F;
    float f4;
    float f9;
    float f5;
    float f6;
    float f7;
    float f8;
    float f3;
    float f2;
    if (this.imageMoveAnimation != null)
    {
      if (!this.scroller.isFinished()) {
        this.scroller.abortAnimation();
      }
      f4 = this.scale;
      f9 = this.animateToScale;
      float f10 = this.scale;
      float f11 = this.animationValue;
      f5 = this.translationX;
      f6 = this.animateToX;
      f7 = this.translationX;
      f8 = this.animationValue;
      f3 = this.translationY + (this.animateToY - this.translationY) * this.animationValue;
      if (this.currentEditMode == 1) {
        this.photoCropView.setAnimationProgress(this.animationValue);
      }
      f2 = f1;
      if (this.animateToScale == 1.0F)
      {
        f2 = f1;
        if (this.scale == 1.0F)
        {
          f2 = f1;
          if (this.translationX == 0.0F) {
            f2 = f3;
          }
        }
      }
      f4 += (f9 - f10) * f11;
      f1 = f5 + (f6 - f7) * f8;
      this.containerView.invalidate();
    }
    for (;;)
    {
      label311:
      Object localObject1;
      Object localObject2;
      label382:
      boolean bool;
      label390:
      int i;
      if ((this.animationInProgress != 2) && (!this.pipAnimationInProgress) && (!this.isInline))
      {
        if ((this.currentEditMode == 0) && (this.scale == 1.0F) && (f2 != -1.0F) && (!this.zoomAnimation))
        {
          f5 = getContainerViewHeight() / 4.0F;
          this.backgroundDrawable.setAlpha((int)Math.max(127.0F, 255.0F * (1.0F - Math.min(Math.abs(f2), f5) / f5)));
        }
      }
      else
      {
        localObject1 = null;
        localObject2 = null;
        if (this.currentEditMode == 0)
        {
          localObject1 = localObject2;
          if (this.scale >= 1.0F)
          {
            localObject1 = localObject2;
            if (!this.zoomAnimation)
            {
              localObject1 = localObject2;
              if (!this.zooming)
              {
                if (f1 <= this.maxX + AndroidUtilities.dp(5.0F)) {
                  break label2182;
                }
                localObject1 = this.leftImage;
              }
            }
          }
          if (localObject1 == null) {
            break label2223;
          }
          bool = true;
          this.changingPage = bool;
        }
        int j;
        if (localObject1 == this.rightImage)
        {
          f7 = f1;
          f8 = 0.0F;
          f9 = 1.0F;
          f6 = f9;
          f5 = f8;
          f2 = f7;
          if (!this.zoomAnimation)
          {
            f6 = f9;
            f5 = f8;
            f2 = f7;
            if (f7 < this.minX)
            {
              f6 = Math.min(1.0F, (this.minX - f7) / paramCanvas.getWidth());
              f5 = (1.0F - f6) * 0.3F;
              f2 = -paramCanvas.getWidth() - AndroidUtilities.dp(30.0F) / 2;
            }
          }
          if (((ImageReceiver)localObject1).hasBitmapImage())
          {
            paramCanvas.save();
            paramCanvas.translate(getContainerViewWidth() / 2, getContainerViewHeight() / 2);
            paramCanvas.translate(paramCanvas.getWidth() + AndroidUtilities.dp(30.0F) / 2 + f2, 0.0F);
            paramCanvas.scale(1.0F - f5, 1.0F - f5);
            j = ((ImageReceiver)localObject1).getBitmapWidth();
            i = ((ImageReceiver)localObject1).getBitmapHeight();
            f7 = getContainerViewWidth() / j;
            f8 = getContainerViewHeight() / i;
            if (f7 <= f8) {
              break label2229;
            }
            f7 = f8;
            label610:
            j = (int)(j * f7);
            i = (int)(i * f7);
            ((ImageReceiver)localObject1).setAlpha(f6);
            ((ImageReceiver)localObject1).setImageCoords(-j / 2, -i / 2, j, i);
            ((ImageReceiver)localObject1).draw(paramCanvas);
            paramCanvas.restore();
          }
          this.groupedPhotosListView.setMoveProgress(-f6);
          paramCanvas.save();
          paramCanvas.translate(f2, f3 / f4);
          paramCanvas.translate((paramCanvas.getWidth() * (this.scale + 1.0F) + AndroidUtilities.dp(30.0F)) / 2.0F, -f3 / f4);
          this.photoProgressViews[1].setScale(1.0F - f5);
          this.photoProgressViews[1].setAlpha(f6);
          this.photoProgressViews[1].onDraw(paramCanvas);
          paramCanvas.restore();
        }
        f7 = f1;
        f8 = 0.0F;
        f9 = 1.0F;
        f2 = f9;
        f6 = f8;
        f5 = f7;
        if (!this.zoomAnimation)
        {
          f2 = f9;
          f6 = f8;
          f5 = f7;
          if (f7 > this.maxX)
          {
            f2 = f9;
            f6 = f8;
            f5 = f7;
            if (this.currentEditMode == 0)
            {
              f2 = Math.min(1.0F, (f7 - this.maxX) / paramCanvas.getWidth());
              f6 = f2 * 0.3F;
              f2 = 1.0F - f2;
              f5 = this.maxX;
            }
          }
        }
        if ((this.aspectRatioFrameLayout == null) || (this.aspectRatioFrameLayout.getVisibility() != 0)) {
          break label2232;
        }
        i = 1;
        label881:
        if (this.centerImage.hasBitmapImage())
        {
          paramCanvas.save();
          paramCanvas.translate(getContainerViewWidth() / 2 + getAdditionX(), getContainerViewHeight() / 2 + getAdditionY());
          paramCanvas.translate(f5, f3);
          paramCanvas.scale(f4 - f6, f4 - f6);
          if (this.currentEditMode == 1) {
            this.photoCropView.setBitmapParams(f4, f5, f3);
          }
          int m = this.centerImage.getBitmapWidth();
          int n = this.centerImage.getBitmapHeight();
          j = n;
          int k = m;
          if (i != 0)
          {
            j = n;
            k = m;
            if (this.textureUploaded)
            {
              j = n;
              k = m;
              if (Math.abs(m / n - this.videoTextureView.getMeasuredWidth() / this.videoTextureView.getMeasuredHeight()) > 0.01F)
              {
                k = this.videoTextureView.getMeasuredWidth();
                j = this.videoTextureView.getMeasuredHeight();
              }
            }
          }
          f7 = getContainerViewWidth() / k;
          f8 = getContainerViewHeight() / j;
          if (f7 <= f8) {
            break label2238;
          }
          f7 = f8;
          label1108:
          k = (int)(k * f7);
          j = (int)(j * f7);
          if ((i == 0) || (!this.textureUploaded) || (!this.videoCrossfadeStarted) || (this.videoCrossfadeAlpha != 1.0F))
          {
            this.centerImage.setAlpha(f2);
            this.centerImage.setImageCoords(-k / 2, -j / 2, k, j);
            this.centerImage.draw(paramCanvas);
          }
          if (i != 0)
          {
            if ((!this.videoCrossfadeStarted) && (this.textureUploaded))
            {
              this.videoCrossfadeStarted = true;
              this.videoCrossfadeAlpha = 0.0F;
              this.videoCrossfadeAlphaLastTime = System.currentTimeMillis();
            }
            paramCanvas.translate(-k / 2, -j / 2);
            this.videoTextureView.setAlpha(this.videoCrossfadeAlpha * f2);
            this.aspectRatioFrameLayout.draw(paramCanvas);
            if ((this.videoCrossfadeStarted) && (this.videoCrossfadeAlpha < 1.0F))
            {
              long l1 = System.currentTimeMillis();
              long l2 = this.videoCrossfadeAlphaLastTime;
              this.videoCrossfadeAlphaLastTime = l1;
              this.videoCrossfadeAlpha += (float)(l1 - l2) / 200.0F;
              this.containerView.invalidate();
              if (this.videoCrossfadeAlpha > 1.0F) {
                this.videoCrossfadeAlpha = 1.0F;
              }
            }
          }
          paramCanvas.restore();
        }
        if (!this.isCurrentVideo) {
          break label2247;
        }
        if ((this.progressView.getVisibility() == 0) || ((this.videoPlayer != null) && (this.videoPlayer.isPlaying()))) {
          break label2241;
        }
        i = 1;
        label1379:
        if (i != 0)
        {
          paramCanvas.save();
          paramCanvas.translate(f5, f3 / f4);
          this.photoProgressViews[0].setScale(1.0F - f6);
          this.photoProgressViews[0].setAlpha(f2);
          this.photoProgressViews[0].onDraw(paramCanvas);
          paramCanvas.restore();
        }
        if ((!this.pipAnimationInProgress) && ((this.miniProgressView.getVisibility() == 0) || (this.miniProgressAnimator != null)))
        {
          paramCanvas.save();
          paramCanvas.translate(this.miniProgressView.getLeft() + f5, this.miniProgressView.getTop() + f3 / f4);
          this.miniProgressView.draw(paramCanvas);
          paramCanvas.restore();
        }
        if (localObject1 == this.leftImage)
        {
          if (((ImageReceiver)localObject1).hasBitmapImage())
          {
            paramCanvas.save();
            paramCanvas.translate(getContainerViewWidth() / 2, getContainerViewHeight() / 2);
            paramCanvas.translate(-(paramCanvas.getWidth() * (this.scale + 1.0F) + AndroidUtilities.dp(30.0F)) / 2.0F + f1, 0.0F);
            j = ((ImageReceiver)localObject1).getBitmapWidth();
            i = ((ImageReceiver)localObject1).getBitmapHeight();
            f5 = getContainerViewWidth() / j;
            f6 = getContainerViewHeight() / i;
            if (f5 <= f6) {
              break label2274;
            }
            f5 = f6;
            j = (int)(j * f5);
            i = (int)(i * f5);
            ((ImageReceiver)localObject1).setAlpha(1.0F);
            ((ImageReceiver)localObject1).setImageCoords(-j / 2, -i / 2, j, i);
            ((ImageReceiver)localObject1).draw(paramCanvas);
            paramCanvas.restore();
          }
          this.groupedPhotosListView.setMoveProgress(1.0F - f2);
          paramCanvas.save();
          paramCanvas.translate(f1, f3 / f4);
          paramCanvas.translate(-(paramCanvas.getWidth() * (this.scale + 1.0F) + AndroidUtilities.dp(30.0F)) / 2.0F, -f3 / f4);
          this.photoProgressViews[2].setScale(1.0F);
          this.photoProgressViews[2].setAlpha(1.0F);
          this.photoProgressViews[2].onDraw(paramCanvas);
          paramCanvas.restore();
        }
        if (this.waitingForDraw == 0) {
          break;
        }
        this.waitingForDraw -= 1;
        if (this.waitingForDraw != 0) {
          break label2315;
        }
        if (this.textureImageView == null) {}
      }
      try
      {
        this.currentBitmap = Bitmaps.createBitmap(this.videoTextureView.getWidth(), this.videoTextureView.getHeight(), Bitmap.Config.ARGB_8888);
        this.changedTextureView.getBitmap(this.currentBitmap);
        if (this.currentBitmap != null)
        {
          this.textureImageView.setVisibility(0);
          this.textureImageView.setImageBitmap(this.currentBitmap);
          this.pipVideoView.close();
          this.pipVideoView = null;
          return;
          if (this.animationStartTime != 0L)
          {
            this.translationX = this.animateToX;
            this.translationY = this.animateToY;
            this.scale = this.animateToScale;
            this.animationStartTime = 0L;
            if (this.currentEditMode == 1) {
              this.photoCropView.setAnimationProgress(1.0F);
            }
            updateMinMax(this.scale);
            this.zoomAnimation = false;
          }
          if ((!this.scroller.isFinished()) && (this.scroller.computeScrollOffset()))
          {
            if ((this.scroller.getStartX() < this.maxX) && (this.scroller.getStartX() > this.minX)) {
              this.translationX = this.scroller.getCurrX();
            }
            if ((this.scroller.getStartY() < this.maxY) && (this.scroller.getStartY() > this.minY)) {
              this.translationY = this.scroller.getCurrY();
            }
            this.containerView.invalidate();
          }
          if (this.switchImageAfterAnimation != 0)
          {
            if (this.switchImageAfterAnimation != 1) {
              break label2147;
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                PhotoViewer.this.setImageIndex(PhotoViewer.this.currentIndex + 1, false);
              }
            });
          }
          for (;;)
          {
            this.switchImageAfterAnimation = 0;
            f5 = this.scale;
            f6 = this.translationY;
            f7 = this.translationX;
            f2 = f1;
            f4 = f5;
            f1 = f7;
            f3 = f6;
            if (this.moving) {
              break;
            }
            f2 = this.translationY;
            f4 = f5;
            f1 = f7;
            f3 = f6;
            break;
            label2147:
            if (this.switchImageAfterAnimation == 2) {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  PhotoViewer.this.setImageIndex(PhotoViewer.this.currentIndex - 1, false);
                }
              });
            }
          }
          this.backgroundDrawable.setAlpha(255);
          break label311;
          label2182:
          if (f1 < this.minX - AndroidUtilities.dp(5.0F))
          {
            localObject1 = this.rightImage;
            break label382;
          }
          this.groupedPhotosListView.setMoveProgress(0.0F);
          localObject1 = localObject2;
          break label382;
          label2223:
          bool = false;
          break label390;
          label2229:
          break label610;
          label2232:
          i = 0;
          break label881;
          label2238:
          break label1108;
          label2241:
          i = 0;
          break label1379;
          label2247:
          if ((i == 0) && (this.videoPlayerControlFrameLayout.getVisibility() != 0)) {}
          for (i = 1;; i = 0) {
            break;
          }
        }
      }
      catch (Throwable paramCanvas)
      {
        for (;;)
        {
          label2274:
          if (this.currentBitmap != null)
          {
            this.currentBitmap.recycle();
            this.currentBitmap = null;
          }
          FileLog.e(paramCanvas);
          continue;
          this.textureImageView.setImageDrawable(null);
        }
      }
    }
    label2315:
    this.containerView.invalidate();
  }
  
  private void onPhotoClosed(PlaceProviderObject paramPlaceProviderObject)
  {
    this.isVisible = false;
    this.disableShowCheck = true;
    this.currentMessageObject = null;
    this.currentBotInlineResult = null;
    this.currentFileLocation = null;
    this.currentPathObject = null;
    if (this.currentThumb != null)
    {
      this.currentThumb.release();
      this.currentThumb = null;
    }
    this.parentAlert = null;
    if (this.currentAnimation != null)
    {
      this.currentAnimation.setSecondParentView(null);
      this.currentAnimation = null;
    }
    int i = 0;
    while (i < 3)
    {
      if (this.photoProgressViews[i] != null) {
        this.photoProgressViews[i].setBackgroundState(-1, false);
      }
      i += 1;
    }
    requestVideoPreview(0);
    if (this.videoTimelineView != null) {
      this.videoTimelineView.destroy();
    }
    this.centerImage.setImageBitmap((Bitmap)null);
    this.leftImage.setImageBitmap((Bitmap)null);
    this.rightImage.setImageBitmap((Bitmap)null);
    this.containerView.post(new Runnable()
    {
      public void run()
      {
        PhotoViewer.this.animatingImageView.setImageBitmap(null);
        try
        {
          if (PhotoViewer.this.windowView.getParent() != null) {
            ((WindowManager)PhotoViewer.this.parentActivity.getSystemService("window")).removeView(PhotoViewer.this.windowView);
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
    if (this.placeProvider != null) {
      this.placeProvider.willHidePhotoViewer();
    }
    this.groupedPhotosListView.clear();
    this.placeProvider = null;
    this.selectedPhotosAdapter.notifyDataSetChanged();
    this.disableShowCheck = false;
    if (paramPlaceProviderObject != null) {
      paramPlaceProviderObject.imageReceiver.setVisible(true, true);
    }
  }
  
  private void onPhotoShow(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, ArrayList<MessageObject> paramArrayList, ArrayList<Object> paramArrayList1, int paramInt, PlaceProviderObject paramPlaceProviderObject)
  {
    this.classGuid = ConnectionsManager.generateClassGuid();
    this.currentMessageObject = null;
    this.currentFileLocation = null;
    this.currentPathObject = null;
    this.fromCamera = false;
    this.currentBotInlineResult = null;
    this.currentIndex = -1;
    this.currentFileNames[0] = null;
    this.currentFileNames[1] = null;
    this.currentFileNames[2] = null;
    this.avatarsDialogId = 0;
    this.totalImagesCount = 0;
    this.totalImagesCountMerge = 0;
    this.currentEditMode = 0;
    this.isFirstLoading = true;
    this.needSearchImageInArr = false;
    this.loadingMoreImages = false;
    this.endReached[0] = false;
    Object localObject = this.endReached;
    if (this.mergeDialogId == 0L) {}
    int i;
    for (boolean bool = true;; bool = false)
    {
      localObject[1] = bool;
      this.opennedFromMedia = false;
      this.needCaptionLayout = false;
      this.containerView.setTag(Integer.valueOf(1));
      this.isCurrentVideo = false;
      this.imagesArr.clear();
      this.imagesArrLocations.clear();
      this.imagesArrLocationsSizes.clear();
      this.avatarsArr.clear();
      this.imagesArrLocals.clear();
      i = 0;
      while (i < 2)
      {
        this.imagesByIds[i].clear();
        this.imagesByIdsTemp[i].clear();
        i += 1;
      }
    }
    this.imagesArrTemp.clear();
    this.currentUserAvatarLocation = null;
    this.containerView.setPadding(0, 0, 0, 0);
    if (this.currentThumb != null) {
      this.currentThumb.release();
    }
    if (paramPlaceProviderObject != null)
    {
      localObject = paramPlaceProviderObject.thumb;
      this.currentThumb = ((ImageReceiver.BitmapHolder)localObject);
      if ((paramPlaceProviderObject == null) || (!paramPlaceProviderObject.isEvent)) {
        break label813;
      }
    }
    label813:
    for (bool = true;; bool = false)
    {
      this.isEvent = bool;
      this.menuItem.setVisibility(0);
      this.sendItem.setVisibility(8);
      this.pipItem.setVisibility(8);
      this.cameraItem.setVisibility(8);
      this.cameraItem.setTag(null);
      this.bottomLayout.setVisibility(0);
      this.bottomLayout.setTag(Integer.valueOf(1));
      this.bottomLayout.setTranslationY(0.0F);
      this.captionTextView.setTranslationY(0.0F);
      this.shareButton.setVisibility(8);
      if (this.qualityChooseView != null)
      {
        this.qualityChooseView.setVisibility(4);
        this.qualityPicker.setVisibility(4);
        this.qualityChooseView.setTag(null);
      }
      if (this.qualityChooseViewAnimation != null)
      {
        this.qualityChooseViewAnimation.cancel();
        this.qualityChooseViewAnimation = null;
      }
      this.allowShare = false;
      this.slideshowMessageId = 0;
      this.nameOverride = null;
      this.dateOverride = 0;
      this.menuItem.hideSubItem(2);
      this.menuItem.hideSubItem(4);
      this.menuItem.hideSubItem(10);
      this.menuItem.hideSubItem(11);
      this.actionBar.setTranslationY(0.0F);
      this.checkImageView.setAlpha(1.0F);
      this.checkImageView.setVisibility(8);
      this.actionBar.setTitleRightMargin(0);
      this.photosCounterView.setAlpha(1.0F);
      this.photosCounterView.setVisibility(8);
      this.pickerView.setVisibility(8);
      this.pickerViewSendButton.setVisibility(8);
      this.pickerViewSendButton.setTranslationY(0.0F);
      this.pickerView.setAlpha(1.0F);
      this.pickerViewSendButton.setAlpha(1.0F);
      this.pickerView.setTranslationY(0.0F);
      this.paintItem.setVisibility(8);
      this.cropItem.setVisibility(8);
      this.tuneItem.setVisibility(8);
      this.timeItem.setVisibility(8);
      this.videoTimelineView.setVisibility(8);
      this.compressItem.setVisibility(8);
      this.captionEditText.setVisibility(8);
      this.mentionListView.setVisibility(8);
      this.muteItem.setVisibility(8);
      this.actionBar.setSubtitle(null);
      this.masksItem.setVisibility(8);
      this.muteVideo = false;
      this.muteItem.setImageResource(2131165702);
      this.editorDoneLayout.setVisibility(8);
      this.captionTextView.setTag(null);
      this.captionTextView.setVisibility(4);
      if (this.photoCropView != null) {
        this.photoCropView.setVisibility(8);
      }
      if (this.photoFilterView != null) {
        this.photoFilterView.setVisibility(8);
      }
      i = 0;
      while (i < 3)
      {
        if (this.photoProgressViews[i] != null) {
          this.photoProgressViews[i].setBackgroundState(-1, false);
        }
        i += 1;
      }
      localObject = null;
      break;
    }
    if ((paramMessageObject != null) && (paramArrayList == null))
    {
      if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && (paramMessageObject.messageOwner.media.webpage != null))
      {
        paramFileLocation = paramMessageObject.messageOwner.media.webpage;
        paramArrayList = paramFileLocation.site_name;
        if (paramArrayList != null)
        {
          paramArrayList = paramArrayList.toLowerCase();
          if ((paramArrayList.equals("instagram")) || (paramArrayList.equals("twitter")) || ("telegram_album".equals(paramFileLocation.type)))
          {
            if (!TextUtils.isEmpty(paramFileLocation.author)) {
              this.nameOverride = paramFileLocation.author;
            }
            if ((paramFileLocation.cached_page instanceof TLRPC.TL_pageFull))
            {
              i = 0;
              if (i < paramFileLocation.cached_page.blocks.size())
              {
                paramArrayList = (TLRPC.PageBlock)paramFileLocation.cached_page.blocks.get(i);
                if (!(paramArrayList instanceof TLRPC.TL_pageBlockAuthorDate)) {
                  break label1232;
                }
                this.dateOverride = ((TLRPC.TL_pageBlockAuthorDate)paramArrayList).published_date;
              }
            }
            paramFileLocation = paramMessageObject.getWebPagePhotos(null, null);
            if (!paramFileLocation.isEmpty())
            {
              this.slideshowMessageId = paramMessageObject.getId();
              this.needSearchImageInArr = false;
              this.imagesArr.addAll(paramFileLocation);
              this.totalImagesCount = this.imagesArr.size();
              setImageIndex(this.imagesArr.indexOf(paramMessageObject), true);
            }
          }
        }
      }
      if (this.slideshowMessageId == 0)
      {
        this.imagesArr.add(paramMessageObject);
        if ((this.currentAnimation != null) || (paramMessageObject.eventId != 0L))
        {
          this.needSearchImageInArr = false;
          label1088:
          setImageIndex(0, true);
        }
      }
      else
      {
        label1094:
        if ((this.currentAnimation == null) && (!this.isEvent))
        {
          if ((this.currentDialogId == 0L) || (this.totalImagesCount != 0)) {
            break label2133;
          }
          DataQuery.getInstance(this.currentAccount).getMediaCount(this.currentDialogId, 0, this.classGuid, true);
          if (this.mergeDialogId != 0L) {
            DataQuery.getInstance(this.currentAccount).getMediaCount(this.mergeDialogId, 0, this.classGuid, true);
          }
        }
        label1173:
        if (((this.currentMessageObject == null) || (!this.currentMessageObject.isVideo())) && ((this.currentBotInlineResult == null) || ((!this.currentBotInlineResult.type.equals("video")) && (!MessageObject.isVideoDocument(this.currentBotInlineResult.document))))) {
          break label2165;
        }
        onActionClick(false);
      }
    }
    label1232:
    label1399:
    label1458:
    label1763:
    label1766:
    label1833:
    label1881:
    label2079:
    label2108:
    label2114:
    label2120:
    label2126:
    label2133:
    label2165:
    while (this.imagesArrLocals.isEmpty())
    {
      return;
      i += 1;
      break;
      if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice)) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) || ((paramMessageObject.messageOwner.action != null) && (!(paramMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionEmpty)))) {
        break label1088;
      }
      this.needSearchImageInArr = true;
      this.imagesByIds[0].put(paramMessageObject.getId(), paramMessageObject);
      this.menuItem.showSubItem(2);
      this.sendItem.setVisibility(0);
      break label1088;
      if (paramFileLocation != null)
      {
        this.avatarsDialogId = paramPlaceProviderObject.dialogId;
        this.imagesArrLocations.add(paramFileLocation);
        this.imagesArrLocationsSizes.add(Integer.valueOf(paramPlaceProviderObject.size));
        this.avatarsArr.add(new TLRPC.TL_photoEmpty());
        paramMessageObject = this.shareButton;
        if (this.videoPlayerControlFrameLayout.getVisibility() != 0)
        {
          i = 0;
          paramMessageObject.setVisibility(i);
          this.allowShare = true;
          this.menuItem.hideSubItem(2);
          if (this.shareButton.getVisibility() != 0) {
            break label1458;
          }
          this.menuItem.hideSubItem(10);
        }
        for (;;)
        {
          setImageIndex(0, true);
          this.currentUserAvatarLocation = paramFileLocation;
          break;
          i = 8;
          break label1399;
          this.menuItem.showSubItem(10);
        }
      }
      if (paramArrayList != null)
      {
        this.opennedFromMedia = true;
        this.menuItem.showSubItem(4);
        this.sendItem.setVisibility(0);
        this.imagesArr.addAll(paramArrayList);
        i = 0;
        if (i < this.imagesArr.size())
        {
          paramMessageObject = (MessageObject)this.imagesArr.get(i);
          paramFileLocation = this.imagesByIds;
          if (paramMessageObject.getDialogId() == this.currentDialogId) {}
          for (int j = 0;; j = 1)
          {
            paramFileLocation[j].put(paramMessageObject.getId(), paramMessageObject);
            i += 1;
            break;
          }
        }
        setImageIndex(paramInt, true);
        break label1094;
      }
      if (paramArrayList1 == null) {
        break label1094;
      }
      if ((this.sendPhotoType == 0) || ((this.sendPhotoType == 2) && (paramArrayList1.size() > 1)))
      {
        this.checkImageView.setVisibility(0);
        this.photosCounterView.setVisibility(0);
        this.actionBar.setTitleRightMargin(AndroidUtilities.dp(100.0F));
      }
      if (this.sendPhotoType == 2)
      {
        this.cameraItem.setVisibility(0);
        this.cameraItem.setTag(Integer.valueOf(1));
      }
      this.menuItem.setVisibility(8);
      this.imagesArrLocals.addAll(paramArrayList1);
      paramMessageObject = this.imagesArrLocals.get(paramInt);
      if ((paramMessageObject instanceof MediaController.PhotoEntry)) {
        if (((MediaController.PhotoEntry)paramMessageObject).isVideo)
        {
          this.cropItem.setVisibility(8);
          this.bottomLayout.setVisibility(0);
          this.bottomLayout.setTag(Integer.valueOf(1));
          this.bottomLayout.setTranslationY(-AndroidUtilities.dp(48.0F));
          i = 1;
          if ((this.parentChatActivity != null) && ((this.parentChatActivity.currentEncryptedChat == null) || (AndroidUtilities.getPeerLayerVersion(this.parentChatActivity.currentEncryptedChat.layer) >= 46)))
          {
            this.mentionsAdapter.setChatInfo(this.parentChatActivity.info);
            paramMessageObject = this.mentionsAdapter;
            if (this.parentChatActivity.currentChat == null) {
              break label2114;
            }
            bool = true;
            paramMessageObject.setNeedUsernames(bool);
            this.mentionsAdapter.setNeedBotContext(false);
            if ((i == 0) || ((this.placeProvider != null) && ((this.placeProvider == null) || (!this.placeProvider.allowCaption())))) {
              break label2120;
            }
            bool = true;
            this.needCaptionLayout = bool;
            paramMessageObject = this.captionEditText;
            if (!this.needCaptionLayout) {
              break label2126;
            }
          }
        }
      }
      for (i = 0;; i = 8)
      {
        paramMessageObject.setVisibility(i);
        if (this.needCaptionLayout) {
          this.captionEditText.onCreate();
        }
        this.pickerView.setVisibility(0);
        this.pickerViewSendButton.setVisibility(0);
        this.pickerViewSendButton.setTranslationY(0.0F);
        this.pickerViewSendButton.setAlpha(1.0F);
        this.bottomLayout.setVisibility(8);
        this.bottomLayout.setTag(null);
        this.containerView.setTag(null);
        setImageIndex(paramInt, true);
        this.paintItem.setVisibility(this.cropItem.getVisibility());
        this.tuneItem.setVisibility(this.cropItem.getVisibility());
        updateSelectedCount();
        break;
        this.cropItem.setVisibility(0);
        break label1763;
        if ((paramMessageObject instanceof TLRPC.BotInlineResult))
        {
          this.cropItem.setVisibility(8);
          i = 0;
          break label1766;
        }
        paramFileLocation = this.cropItem;
        if (((paramMessageObject instanceof MediaController.SearchImage)) && (((MediaController.SearchImage)paramMessageObject).type == 0))
        {
          i = 0;
          paramFileLocation.setVisibility(i);
          if (this.cropItem.getVisibility() != 0) {
            break label2108;
          }
        }
        for (i = 1;; i = 0)
        {
          break;
          i = 8;
          break label2079;
        }
        bool = false;
        break label1833;
        bool = false;
        break label1881;
      }
      if (this.avatarsDialogId == 0) {
        break label1173;
      }
      MessagesController.getInstance(this.currentAccount).loadDialogPhotos(this.avatarsDialogId, 80, 0L, true, this.classGuid);
      break label1173;
    }
    paramFileLocation = this.imagesArrLocals.get(paramInt);
    if (this.parentChatActivity != null)
    {
      paramMessageObject = this.parentChatActivity.getCurrentUser();
      if ((this.parentChatActivity == null) || (this.parentChatActivity.isSecretChat()) || (paramMessageObject == null) || (paramMessageObject.bot)) {
        break label2297;
      }
      paramInt = 1;
      label2231:
      if (!(paramFileLocation instanceof MediaController.PhotoEntry)) {
        break label2303;
      }
      paramMessageObject = (MediaController.PhotoEntry)paramFileLocation;
      i = paramInt;
      if (paramMessageObject.isVideo)
      {
        preparePlayer(Uri.fromFile(new File(paramMessageObject.path)), false, false);
        i = paramInt;
      }
    }
    for (;;)
    {
      label2278:
      if (i != 0)
      {
        this.timeItem.setVisibility(0);
        return;
        paramMessageObject = null;
        break;
        label2297:
        paramInt = 0;
        break label2231;
        label2303:
        i = paramInt;
        if (paramInt != 0)
        {
          i = paramInt;
          if ((paramFileLocation instanceof MediaController.SearchImage)) {
            if (((MediaController.SearchImage)paramFileLocation).type != 0) {
              break label2343;
            }
          }
        }
      }
    }
    label2343:
    for (paramInt = 1;; paramInt = 0)
    {
      i = paramInt;
      break label2278;
      break;
    }
  }
  
  /* Error */
  private void onSharePressed()
  {
    // Byte code:
    //   0: iconst_1
    //   1: istore 4
    //   3: aload_0
    //   4: getfield 1147	org/telegram/ui/PhotoViewer:parentActivity	Landroid/app/Activity;
    //   7: ifnull +10 -> 17
    //   10: aload_0
    //   11: getfield 2727	org/telegram/ui/PhotoViewer:allowShare	Z
    //   14: ifne +4 -> 18
    //   17: return
    //   18: aconst_null
    //   19: astore 5
    //   21: aconst_null
    //   22: astore 6
    //   24: iconst_0
    //   25: istore_3
    //   26: aload_0
    //   27: getfield 1365	org/telegram/ui/PhotoViewer:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   30: ifnull +188 -> 218
    //   33: aload_0
    //   34: getfield 1365	org/telegram/ui/PhotoViewer:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   37: invokevirtual 1791	org/telegram/messenger/MessageObject:isVideo	()Z
    //   40: istore_3
    //   41: aload_0
    //   42: getfield 1365	org/telegram/ui/PhotoViewer:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   45: getfield 1748	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   48: getfield 1753	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   51: invokestatic 1759	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   54: ifne +35 -> 89
    //   57: new 1506	java/io/File
    //   60: dup
    //   61: aload_0
    //   62: getfield 1365	org/telegram/ui/PhotoViewer:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   65: getfield 1748	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   68: getfield 1753	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   71: invokespecial 1762	java/io/File:<init>	(Ljava/lang/String;)V
    //   74: astore 6
    //   76: aload 6
    //   78: invokevirtual 1765	java/io/File:exists	()Z
    //   81: istore_2
    //   82: iload_2
    //   83: ifne +264 -> 347
    //   86: aconst_null
    //   87: astore 6
    //   89: aload 6
    //   91: astore 5
    //   93: iload_3
    //   94: istore_2
    //   95: aload 6
    //   97: ifnonnull +17 -> 114
    //   100: aload_0
    //   101: getfield 1365	org/telegram/ui/PhotoViewer:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   104: getfield 1748	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   107: invokestatic 1817	org/telegram/messenger/FileLoader:getPathToMessage	(Lorg/telegram/tgnet/TLRPC$Message;)Ljava/io/File;
    //   110: astore 5
    //   112: iload_3
    //   113: istore_2
    //   114: aload 5
    //   116: invokevirtual 1765	java/io/File:exists	()Z
    //   119: ifeq +218 -> 337
    //   122: new 2893	android/content/Intent
    //   125: dup
    //   126: ldc_w 2895
    //   129: invokespecial 2896	android/content/Intent:<init>	(Ljava/lang/String;)V
    //   132: astore 6
    //   134: iload_2
    //   135: ifeq +131 -> 266
    //   138: aload 6
    //   140: ldc_w 2898
    //   143: invokevirtual 2902	android/content/Intent:setType	(Ljava/lang/String;)Landroid/content/Intent;
    //   146: pop
    //   147: getstatic 1694	android/os/Build$VERSION:SDK_INT	I
    //   150: istore_1
    //   151: iload_1
    //   152: bipush 24
    //   154: if_icmplt +166 -> 320
    //   157: aload 6
    //   159: ldc_w 2904
    //   162: aload_0
    //   163: getfield 1147	org/telegram/ui/PhotoViewer:parentActivity	Landroid/app/Activity;
    //   166: ldc_w 2906
    //   169: aload 5
    //   171: invokestatic 2912	android/support/v4/content/FileProvider:getUriForFile	(Landroid/content/Context;Ljava/lang/String;Ljava/io/File;)Landroid/net/Uri;
    //   174: invokevirtual 2916	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   177: pop
    //   178: aload 6
    //   180: iconst_1
    //   181: invokevirtual 2920	android/content/Intent:setFlags	(I)Landroid/content/Intent;
    //   184: pop
    //   185: aload_0
    //   186: getfield 1147	org/telegram/ui/PhotoViewer:parentActivity	Landroid/app/Activity;
    //   189: aload 6
    //   191: ldc_w 2922
    //   194: ldc_w 2923
    //   197: invokestatic 1679	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   200: invokestatic 2927	android/content/Intent:createChooser	(Landroid/content/Intent;Ljava/lang/CharSequence;)Landroid/content/Intent;
    //   203: sipush 500
    //   206: invokevirtual 2931	android/app/Activity:startActivityForResult	(Landroid/content/Intent;I)V
    //   209: return
    //   210: astore 5
    //   212: aload 5
    //   214: invokestatic 2163	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   217: return
    //   218: iload_3
    //   219: istore_2
    //   220: aload_0
    //   221: getfield 1377	org/telegram/ui/PhotoViewer:currentFileLocation	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   224: ifnull -110 -> 114
    //   227: aload_0
    //   228: getfield 1377	org/telegram/ui/PhotoViewer:currentFileLocation	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   231: astore 5
    //   233: iload 4
    //   235: istore_2
    //   236: aload_0
    //   237: getfield 1380	org/telegram/ui/PhotoViewer:avatarsDialogId	I
    //   240: ifne +13 -> 253
    //   243: aload_0
    //   244: getfield 1383	org/telegram/ui/PhotoViewer:isEvent	Z
    //   247: ifeq +103 -> 350
    //   250: iload 4
    //   252: istore_2
    //   253: aload 5
    //   255: iload_2
    //   256: invokestatic 1504	org/telegram/messenger/FileLoader:getPathToAttach	(Lorg/telegram/tgnet/TLObject;Z)Ljava/io/File;
    //   259: astore 5
    //   261: iload_3
    //   262: istore_2
    //   263: goto -149 -> 114
    //   266: aload_0
    //   267: getfield 1365	org/telegram/ui/PhotoViewer:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   270: ifnull +19 -> 289
    //   273: aload 6
    //   275: aload_0
    //   276: getfield 1365	org/telegram/ui/PhotoViewer:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   279: invokevirtual 2934	org/telegram/messenger/MessageObject:getMimeType	()Ljava/lang/String;
    //   282: invokevirtual 2902	android/content/Intent:setType	(Ljava/lang/String;)Landroid/content/Intent;
    //   285: pop
    //   286: goto -139 -> 147
    //   289: aload 6
    //   291: ldc_w 2936
    //   294: invokevirtual 2902	android/content/Intent:setType	(Ljava/lang/String;)Landroid/content/Intent;
    //   297: pop
    //   298: goto -151 -> 147
    //   301: astore 7
    //   303: aload 6
    //   305: ldc_w 2904
    //   308: aload 5
    //   310: invokestatic 2423	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   313: invokevirtual 2916	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   316: pop
    //   317: goto -132 -> 185
    //   320: aload 6
    //   322: ldc_w 2904
    //   325: aload 5
    //   327: invokestatic 2423	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   330: invokevirtual 2916	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   333: pop
    //   334: goto -149 -> 185
    //   337: aload_0
    //   338: invokespecial 1387	org/telegram/ui/PhotoViewer:showDownloadAlert	()V
    //   341: return
    //   342: astore 5
    //   344: goto -132 -> 212
    //   347: goto -258 -> 89
    //   350: iconst_0
    //   351: istore_2
    //   352: goto -99 -> 253
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	355	0	this	PhotoViewer
    //   150	5	1	i	int
    //   81	271	2	bool1	boolean
    //   25	237	3	bool2	boolean
    //   1	250	4	bool3	boolean
    //   19	151	5	localObject1	Object
    //   210	3	5	localException1	Exception
    //   231	95	5	localObject2	Object
    //   342	1	5	localException2	Exception
    //   22	299	6	localObject3	Object
    //   301	1	7	localException3	Exception
    // Exception table:
    //   from	to	target	type
    //   26	41	210	java/lang/Exception
    //   41	76	210	java/lang/Exception
    //   100	112	210	java/lang/Exception
    //   114	134	210	java/lang/Exception
    //   138	147	210	java/lang/Exception
    //   147	151	210	java/lang/Exception
    //   185	209	210	java/lang/Exception
    //   220	233	210	java/lang/Exception
    //   236	250	210	java/lang/Exception
    //   253	261	210	java/lang/Exception
    //   266	286	210	java/lang/Exception
    //   289	298	210	java/lang/Exception
    //   303	317	210	java/lang/Exception
    //   320	334	210	java/lang/Exception
    //   337	341	210	java/lang/Exception
    //   157	185	301	java/lang/Exception
    //   76	82	342	java/lang/Exception
  }
  
  private boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    if ((this.animationInProgress != 0) || (this.animationStartTime != 0L)) {
      return false;
    }
    if (this.currentEditMode == 2)
    {
      this.photoFilterView.onTouch(paramMotionEvent);
      return true;
    }
    if (this.currentEditMode == 1) {
      return true;
    }
    if ((this.captionEditText.isPopupShowing()) || (this.captionEditText.isKeyboardVisible()))
    {
      if (paramMotionEvent.getAction() == 1) {
        closeCaptionEnter(true);
      }
      return true;
    }
    if ((this.currentEditMode == 0) && (paramMotionEvent.getPointerCount() == 1) && (this.gestureDetector.onTouchEvent(paramMotionEvent)) && (this.doubleTap))
    {
      this.doubleTap = false;
      this.moving = false;
      this.zooming = false;
      checkMinMax(false);
      return true;
    }
    if ((paramMotionEvent.getActionMasked() == 0) || (paramMotionEvent.getActionMasked() == 5))
    {
      if (this.currentEditMode == 1) {
        this.photoCropView.cancelAnimationRunnable();
      }
      this.discardTap = false;
      if (!this.scroller.isFinished()) {
        this.scroller.abortAnimation();
      }
      if ((!this.draggingDown) && (!this.changingPage))
      {
        if ((!this.canZoom) || (paramMotionEvent.getPointerCount() != 2)) {
          break label333;
        }
        this.pinchStartDistance = ((float)Math.hypot(paramMotionEvent.getX(1) - paramMotionEvent.getX(0), paramMotionEvent.getY(1) - paramMotionEvent.getY(0)));
        this.pinchStartScale = this.scale;
        this.pinchCenterX = ((paramMotionEvent.getX(0) + paramMotionEvent.getX(1)) / 2.0F);
        this.pinchCenterY = ((paramMotionEvent.getY(0) + paramMotionEvent.getY(1)) / 2.0F);
        this.pinchStartX = this.translationX;
        this.pinchStartY = this.translationY;
        this.zooming = true;
        this.moving = false;
        if (this.velocityTracker != null) {
          this.velocityTracker.clear();
        }
      }
    }
    label333:
    label1069:
    label1448:
    label1495:
    do
    {
      do
      {
        for (;;)
        {
          return false;
          if (paramMotionEvent.getPointerCount() == 1)
          {
            this.moveStartX = paramMotionEvent.getX();
            f1 = paramMotionEvent.getY();
            this.moveStartY = f1;
            this.dragY = f1;
            this.draggingDown = false;
            this.canDragDown = true;
            if (this.velocityTracker != null)
            {
              this.velocityTracker.clear();
              continue;
              if (paramMotionEvent.getActionMasked() != 2) {
                break;
              }
              if (this.currentEditMode == 1) {
                this.photoCropView.cancelAnimationRunnable();
              }
              if ((this.canZoom) && (paramMotionEvent.getPointerCount() == 2) && (!this.draggingDown) && (this.zooming) && (!this.changingPage))
              {
                this.discardTap = true;
                this.scale = ((float)Math.hypot(paramMotionEvent.getX(1) - paramMotionEvent.getX(0), paramMotionEvent.getY(1) - paramMotionEvent.getY(0)) / this.pinchStartDistance * this.pinchStartScale);
                this.translationX = (this.pinchCenterX - getContainerViewWidth() / 2 - (this.pinchCenterX - getContainerViewWidth() / 2 - this.pinchStartX) * (this.scale / this.pinchStartScale));
                this.translationY = (this.pinchCenterY - getContainerViewHeight() / 2 - (this.pinchCenterY - getContainerViewHeight() / 2 - this.pinchStartY) * (this.scale / this.pinchStartScale));
                updateMinMax(this.scale);
                this.containerView.invalidate();
              }
              else if (paramMotionEvent.getPointerCount() == 1)
              {
                if (this.velocityTracker != null) {
                  this.velocityTracker.addMovement(paramMotionEvent);
                }
                f1 = Math.abs(paramMotionEvent.getX() - this.moveStartX);
                f2 = Math.abs(paramMotionEvent.getY() - this.dragY);
                if ((f1 > AndroidUtilities.dp(3.0F)) || (f2 > AndroidUtilities.dp(3.0F)))
                {
                  this.discardTap = true;
                  if ((this.qualityChooseView != null) && (this.qualityChooseView.getVisibility() == 0)) {
                    return true;
                  }
                }
                if ((this.placeProvider.canScrollAway()) && (this.currentEditMode == 0) && (this.canDragDown) && (!this.draggingDown) && (this.scale == 1.0F) && (f2 >= AndroidUtilities.dp(30.0F)) && (f2 / 2.0F > f1))
                {
                  this.draggingDown = true;
                  this.moving = false;
                  this.dragY = paramMotionEvent.getY();
                  if ((this.isActionBarVisible) && (this.containerView.getTag() != null)) {
                    toggleActionBar(false, true);
                  }
                  for (;;)
                  {
                    return true;
                    if (this.pickerView.getVisibility() == 0)
                    {
                      toggleActionBar(false, true);
                      togglePhotosListView(false, true);
                      toggleCheckImageView(false);
                    }
                  }
                }
                if (this.draggingDown)
                {
                  this.translationY = (paramMotionEvent.getY() - this.dragY);
                  this.containerView.invalidate();
                }
                else if ((!this.invalidCoords) && (this.animationStartTime == 0L))
                {
                  f1 = this.moveStartX - paramMotionEvent.getX();
                  f2 = this.moveStartY - paramMotionEvent.getY();
                  if ((this.moving) || (this.currentEditMode != 0) || ((this.scale == 1.0F) && (Math.abs(f2) + AndroidUtilities.dp(12.0F) < Math.abs(f1))) || (this.scale != 1.0F))
                  {
                    if (!this.moving)
                    {
                      f1 = 0.0F;
                      f2 = 0.0F;
                      this.moving = true;
                      this.canDragDown = false;
                    }
                    this.moveStartX = paramMotionEvent.getX();
                    this.moveStartY = paramMotionEvent.getY();
                    updateMinMax(this.scale);
                    if ((this.translationX >= this.minX) || ((this.currentEditMode == 0) && (this.rightImage.hasImage())))
                    {
                      f3 = f1;
                      if (this.translationX <= this.maxX) {
                        break label1069;
                      }
                      if (this.currentEditMode == 0)
                      {
                        f3 = f1;
                        if (this.leftImage.hasImage()) {
                          break label1069;
                        }
                      }
                    }
                    f3 = f1 / 3.0F;
                    if ((this.maxY == 0.0F) && (this.minY == 0.0F) && (this.currentEditMode == 0)) {
                      if (this.translationY - f2 < this.minY)
                      {
                        this.translationY = this.minY;
                        f1 = 0.0F;
                      }
                    }
                    for (;;)
                    {
                      this.translationX -= f3;
                      if ((this.scale != 1.0F) || (this.currentEditMode != 0)) {
                        this.translationY -= f1;
                      }
                      this.containerView.invalidate();
                      break;
                      f1 = f2;
                      if (this.translationY - f2 > this.maxY)
                      {
                        this.translationY = this.maxY;
                        f1 = 0.0F;
                        continue;
                        if (this.translationY >= this.minY)
                        {
                          f1 = f2;
                          if (this.translationY <= this.maxY) {}
                        }
                        else
                        {
                          f1 = f2 / 3.0F;
                        }
                      }
                    }
                  }
                }
                else
                {
                  this.invalidCoords = false;
                  this.moveStartX = paramMotionEvent.getX();
                  this.moveStartY = paramMotionEvent.getY();
                }
              }
            }
          }
        }
      } while ((paramMotionEvent.getActionMasked() != 3) && (paramMotionEvent.getActionMasked() != 1) && (paramMotionEvent.getActionMasked() != 6));
      if (this.currentEditMode == 1) {
        this.photoCropView.startAnimationRunnable();
      }
      if (this.zooming)
      {
        this.invalidCoords = true;
        if (this.scale < 1.0F)
        {
          updateMinMax(1.0F);
          animateTo(1.0F, 0.0F, 0.0F, true);
        }
        for (;;)
        {
          this.zooming = false;
          break;
          if (this.scale > 3.0F)
          {
            f2 = this.pinchCenterX - getContainerViewWidth() / 2 - (this.pinchCenterX - getContainerViewWidth() / 2 - this.pinchStartX) * (3.0F / this.pinchStartScale);
            f3 = this.pinchCenterY - getContainerViewHeight() / 2 - (this.pinchCenterY - getContainerViewHeight() / 2 - this.pinchStartY) * (3.0F / this.pinchStartScale);
            updateMinMax(3.0F);
            if (f2 < this.minX)
            {
              f1 = this.minX;
              if (f3 >= this.minY) {
                break label1495;
              }
              f2 = this.minY;
            }
            for (;;)
            {
              animateTo(3.0F, f1, f2, true);
              break;
              f1 = f2;
              if (f2 <= this.maxX) {
                break label1448;
              }
              f1 = this.maxX;
              break label1448;
              f2 = f3;
              if (f3 > this.maxY) {
                f2 = this.maxY;
              }
            }
          }
          checkMinMax(true);
        }
      }
      if (this.draggingDown)
      {
        if (Math.abs(this.dragY - paramMotionEvent.getY()) > getContainerViewHeight() / 6.0F) {
          closePhoto(true, false);
        }
        for (;;)
        {
          this.draggingDown = false;
          break;
          if (this.pickerView.getVisibility() == 0)
          {
            toggleActionBar(true, true);
            toggleCheckImageView(true);
          }
          animateTo(1.0F, 0.0F, 0.0F, false);
        }
      }
    } while (!this.moving);
    float f3 = this.translationX;
    float f2 = this.translationY;
    updateMinMax(this.scale);
    this.moving = false;
    this.canDragDown = true;
    float f4 = 0.0F;
    float f1 = f4;
    if (this.velocityTracker != null)
    {
      f1 = f4;
      if (this.scale == 1.0F)
      {
        this.velocityTracker.computeCurrentVelocity(1000);
        f1 = this.velocityTracker.getXVelocity();
      }
    }
    if (this.currentEditMode == 0)
    {
      if (((this.translationX < this.minX - getContainerViewWidth() / 3) || (f1 < -AndroidUtilities.dp(650.0F))) && (this.rightImage.hasImage()))
      {
        goToNext();
        return true;
      }
      if (((this.translationX > this.maxX + getContainerViewWidth() / 3) || (f1 > AndroidUtilities.dp(650.0F))) && (this.leftImage.hasImage()))
      {
        goToPrev();
        return true;
      }
    }
    if (this.translationX < this.minX)
    {
      f1 = this.minX;
      label1802:
      if (this.translationY >= this.minY) {
        break label1856;
      }
      f2 = this.minY;
    }
    for (;;)
    {
      animateTo(this.scale, f1, f2, false);
      break;
      f1 = f3;
      if (this.translationX <= this.maxX) {
        break label1802;
      }
      f1 = this.maxX;
      break label1802;
      label1856:
      if (this.translationY > this.maxY) {
        f2 = this.maxY;
      }
    }
  }
  
  private void openCaptionEnter()
  {
    if ((this.imageMoveAnimation != null) || (this.changeModeAnimation != null) || (this.currentEditMode != 0)) {
      return;
    }
    this.selectedPhotosListView.setVisibility(8);
    this.selectedPhotosListView.setEnabled(false);
    this.selectedPhotosListView.setAlpha(0.0F);
    this.selectedPhotosListView.setTranslationY(-AndroidUtilities.dp(10.0F));
    this.photosCounterView.setRotationX(0.0F);
    this.isPhotosListViewVisible = false;
    this.captionEditText.setTag(Integer.valueOf(1));
    this.captionEditText.openKeyboard();
    this.lastTitle = this.actionBar.getTitle();
    if (this.isCurrentVideo)
    {
      ActionBar localActionBar = this.actionBar;
      if (this.muteVideo) {}
      for (String str = LocaleController.getString("GifCaption", 2131493627);; str = LocaleController.getString("VideoCaption", 2131494577))
      {
        localActionBar.setTitle(str);
        this.actionBar.setSubtitle(null);
        return;
      }
    }
    this.actionBar.setTitle(LocaleController.getString("PhotoCaption", 2131494159));
  }
  
  private void preparePlayer(Uri paramUri, boolean paramBoolean1, boolean paramBoolean2)
  {
    int i = 0;
    if (!paramBoolean2) {
      this.currentPlayingVideoFile = paramUri;
    }
    if (this.parentActivity == null) {
      return;
    }
    this.streamingAlertShown = false;
    this.startedPlayTime = SystemClock.elapsedRealtime();
    this.currentVideoFinishedLoading = false;
    this.lastBufferedPositionCheck = 0L;
    this.seekToProgressPending = 0.0F;
    this.firstAnimationDelay = true;
    this.inPreview = paramBoolean2;
    releasePlayer();
    if (this.videoTextureView == null)
    {
      this.aspectRatioFrameLayout = new AspectRatioFrameLayout(this.parentActivity)
      {
        protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          super.onMeasure(paramAnonymousInt1, paramAnonymousInt2);
          if (PhotoViewer.this.textureImageView != null)
          {
            ViewGroup.LayoutParams localLayoutParams = PhotoViewer.this.textureImageView.getLayoutParams();
            localLayoutParams.width = getMeasuredWidth();
            localLayoutParams.height = getMeasuredHeight();
          }
        }
      };
      this.aspectRatioFrameLayout.setVisibility(4);
      this.containerView.addView(this.aspectRatioFrameLayout, 0, LayoutHelper.createFrame(-1, -1, 17));
      this.videoTextureView = new TextureView(this.parentActivity);
      this.videoTextureView.setPivotX(0.0F);
      this.videoTextureView.setPivotY(0.0F);
      this.videoTextureView.setOpaque(false);
      this.aspectRatioFrameLayout.addView(this.videoTextureView, LayoutHelper.createFrame(-1, -1, 17));
    }
    if ((Build.VERSION.SDK_INT >= 21) && (this.textureImageView == null))
    {
      this.textureImageView = new ImageView(this.parentActivity);
      this.textureImageView.setBackgroundColor(-65536);
      this.textureImageView.setPivotX(0.0F);
      this.textureImageView.setPivotY(0.0F);
      this.textureImageView.setVisibility(4);
      this.containerView.addView(this.textureImageView);
    }
    this.textureUploaded = false;
    this.videoCrossfadeStarted = false;
    TextureView localTextureView = this.videoTextureView;
    this.videoCrossfadeAlpha = 0.0F;
    localTextureView.setAlpha(0.0F);
    this.videoPlayButton.setImageResource(2131165428);
    if (this.videoPlayer == null)
    {
      this.videoPlayer = new VideoPlayer();
      this.videoPlayer.setTextureView(this.videoTextureView);
      this.videoPlayer.setDelegate(new VideoPlayer.VideoPlayerDelegate()
      {
        public void onError(Exception paramAnonymousException)
        {
          FileLog.e(paramAnonymousException);
          if (!PhotoViewer.this.menuItem.isSubItemVisible(11)) {
            return;
          }
          paramAnonymousException = new AlertDialog.Builder(PhotoViewer.this.parentActivity);
          paramAnonymousException.setTitle(LocaleController.getString("AppName", 2131492981));
          paramAnonymousException.setMessage(LocaleController.getString("CantPlayVideo", 2131493133));
          paramAnonymousException.setPositiveButton(LocaleController.getString("Open", 2131494040), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              try
              {
                AndroidUtilities.openForView(PhotoViewer.this.currentMessageObject, PhotoViewer.this.parentActivity);
                PhotoViewer.this.closePhoto(false, false);
                return;
              }
              catch (Exception paramAnonymous2DialogInterface)
              {
                FileLog.e(paramAnonymous2DialogInterface);
              }
            }
          });
          paramAnonymousException.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          PhotoViewer.this.showAlertDialog(paramAnonymousException);
        }
        
        public void onRenderedFirstFrame()
        {
          if (!PhotoViewer.this.textureUploaded)
          {
            PhotoViewer.access$13602(PhotoViewer.this, true);
            PhotoViewer.this.containerView.invalidate();
          }
        }
        
        public void onStateChanged(boolean paramAnonymousBoolean, int paramAnonymousInt)
        {
          if (PhotoViewer.this.videoPlayer == null) {
            return;
          }
          boolean bool;
          if (PhotoViewer.this.isStreaming)
          {
            PhotoViewer localPhotoViewer = PhotoViewer.this;
            if (paramAnonymousInt == 2)
            {
              bool = true;
              localPhotoViewer.toggleMiniProgress(bool, true);
            }
          }
          else
          {
            if ((!paramAnonymousBoolean) || (paramAnonymousInt == 4) || (paramAnonymousInt == 1)) {
              break label322;
            }
          }
          label322:
          label360:
          do
          {
            do
            {
              for (;;)
              {
                try
                {
                  PhotoViewer.this.parentActivity.getWindow().addFlags(128);
                  PhotoViewer.access$13002(PhotoViewer.this, true);
                  if ((PhotoViewer.this.seekToProgressPending != 0.0F) && ((paramAnonymousInt == 3) || (paramAnonymousInt == 1)))
                  {
                    int i = (int)((float)PhotoViewer.this.videoPlayer.getDuration() * PhotoViewer.this.seekToProgressPending);
                    PhotoViewer.this.videoPlayer.seekTo(i);
                    PhotoViewer.access$1102(PhotoViewer.this, 0.0F);
                  }
                  if (paramAnonymousInt == 3)
                  {
                    if (PhotoViewer.this.aspectRatioFrameLayout.getVisibility() != 0) {
                      PhotoViewer.this.aspectRatioFrameLayout.setVisibility(0);
                    }
                    if (!PhotoViewer.this.pipItem.isEnabled())
                    {
                      PhotoViewer.access$13102(PhotoViewer.this, true);
                      PhotoViewer.this.pipItem.setEnabled(true);
                      PhotoViewer.this.pipItem.setAlpha(1.0F);
                    }
                  }
                  if ((!PhotoViewer.this.videoPlayer.isPlaying()) || (paramAnonymousInt == 4)) {
                    break label360;
                  }
                  if (!PhotoViewer.this.isPlaying)
                  {
                    PhotoViewer.access$1602(PhotoViewer.this, true);
                    PhotoViewer.this.videoPlayButton.setImageResource(2131165427);
                    AndroidUtilities.runOnUIThread(PhotoViewer.this.updateProgressRunnable);
                  }
                  if (PhotoViewer.this.pipVideoView != null) {
                    PhotoViewer.this.pipVideoView.updatePlayButton();
                  }
                  PhotoViewer.this.updateVideoPlayerTime();
                  return;
                  bool = false;
                }
                catch (Exception localException1)
                {
                  FileLog.e(localException1);
                  continue;
                }
                try
                {
                  PhotoViewer.this.parentActivity.getWindow().clearFlags(128);
                  PhotoViewer.access$13002(PhotoViewer.this, false);
                }
                catch (Exception localException2)
                {
                  FileLog.e(localException2);
                }
              }
            } while (!PhotoViewer.this.isPlaying);
            PhotoViewer.access$1602(PhotoViewer.this, false);
            PhotoViewer.this.videoPlayButton.setImageResource(2131165428);
            AndroidUtilities.cancelRunOnUIThread(PhotoViewer.this.updateProgressRunnable);
          } while (paramAnonymousInt != 4);
          if (PhotoViewer.this.isCurrentVideo) {
            if (!PhotoViewer.this.videoTimelineView.isDragging())
            {
              PhotoViewer.this.videoTimelineView.setProgress(0.0F);
              if ((PhotoViewer.this.inPreview) || (PhotoViewer.this.videoTimelineView.getVisibility() != 0)) {
                break label540;
              }
              PhotoViewer.this.videoPlayer.seekTo((int)(PhotoViewer.this.videoTimelineView.getLeftProgress() * (float)PhotoViewer.this.videoPlayer.getDuration()));
              label497:
              PhotoViewer.this.videoPlayer.pause();
              PhotoViewer.this.containerView.invalidate();
            }
          }
          for (;;)
          {
            label517:
            if (PhotoViewer.this.pipVideoView != null)
            {
              PhotoViewer.this.pipVideoView.onVideoCompleted();
              break;
              label540:
              PhotoViewer.this.videoPlayer.seekTo(0L);
              break label497;
              if (!PhotoViewer.this.isActionBarVisible) {
                PhotoViewer.this.toggleActionBar(true, true);
              }
              if (!PhotoViewer.this.videoPlayerSeekbar.isDragging())
              {
                PhotoViewer.this.videoPlayerSeekbar.setProgress(0.0F);
                PhotoViewer.this.videoPlayerControlFrameLayout.invalidate();
                if ((PhotoViewer.this.inPreview) || (PhotoViewer.this.videoTimelineView.getVisibility() != 0)) {
                  break label677;
                }
                PhotoViewer.this.videoPlayer.seekTo((int)(PhotoViewer.this.videoTimelineView.getLeftProgress() * (float)PhotoViewer.this.videoPlayer.getDuration()));
              }
            }
          }
          for (;;)
          {
            PhotoViewer.this.videoPlayer.pause();
            break label517;
            break;
            label677:
            PhotoViewer.this.videoPlayer.seekTo(0L);
          }
        }
        
        public boolean onSurfaceDestroyed(SurfaceTexture paramAnonymousSurfaceTexture)
        {
          if (PhotoViewer.this.changingTextureView)
          {
            PhotoViewer.access$2002(PhotoViewer.this, false);
            if (PhotoViewer.this.isInline)
            {
              if (PhotoViewer.this.isInline) {
                PhotoViewer.access$2702(PhotoViewer.this, 1);
              }
              PhotoViewer.this.changedTextureView.setSurfaceTexture(paramAnonymousSurfaceTexture);
              PhotoViewer.this.changedTextureView.setSurfaceTextureListener(PhotoViewer.this.surfaceTextureListener);
              PhotoViewer.this.changedTextureView.setVisibility(0);
              return true;
            }
          }
          return false;
        }
        
        public void onSurfaceTextureUpdated(SurfaceTexture paramAnonymousSurfaceTexture)
        {
          if (PhotoViewer.this.waitingForFirstTextureUpload == 2)
          {
            if (PhotoViewer.this.textureImageView != null)
            {
              PhotoViewer.this.textureImageView.setVisibility(4);
              PhotoViewer.this.textureImageView.setImageDrawable(null);
              if (PhotoViewer.this.currentBitmap != null)
              {
                PhotoViewer.this.currentBitmap.recycle();
                PhotoViewer.access$1902(PhotoViewer.this, null);
              }
            }
            PhotoViewer.access$1802(PhotoViewer.this, false);
            if (Build.VERSION.SDK_INT >= 21)
            {
              PhotoViewer.this.aspectRatioFrameLayout.getLocationInWindow(PhotoViewer.this.pipPosition);
              paramAnonymousSurfaceTexture = PhotoViewer.this.pipPosition;
              paramAnonymousSurfaceTexture[0] -= PhotoViewer.this.getLeftInset();
              paramAnonymousSurfaceTexture = PhotoViewer.this.pipPosition;
              paramAnonymousSurfaceTexture[1] = ((int)(paramAnonymousSurfaceTexture[1] - PhotoViewer.this.containerView.getTranslationY()));
              paramAnonymousSurfaceTexture = new AnimatorSet();
              paramAnonymousSurfaceTexture.playTogether(new Animator[] { ObjectAnimator.ofFloat(PhotoViewer.this.textureImageView, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.textureImageView, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.textureImageView, "translationX", new float[] { PhotoViewer.this.pipPosition[0] }), ObjectAnimator.ofFloat(PhotoViewer.this.textureImageView, "translationY", new float[] { PhotoViewer.this.pipPosition[1] }), ObjectAnimator.ofFloat(PhotoViewer.this.videoTextureView, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.videoTextureView, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.videoTextureView, "translationX", new float[] { PhotoViewer.this.pipPosition[0] - PhotoViewer.this.aspectRatioFrameLayout.getX() }), ObjectAnimator.ofFloat(PhotoViewer.this.videoTextureView, "translationY", new float[] { PhotoViewer.this.pipPosition[1] - PhotoViewer.this.aspectRatioFrameLayout.getY() }), ObjectAnimator.ofInt(PhotoViewer.this.backgroundDrawable, "alpha", new int[] { 255 }), ObjectAnimator.ofFloat(PhotoViewer.this.actionBar, "alpha", new float[] { 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.bottomLayout, "alpha", new float[] { 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.captionTextView, "alpha", new float[] { 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.groupedPhotosListView, "alpha", new float[] { 1.0F }) });
              paramAnonymousSurfaceTexture.setInterpolator(new DecelerateInterpolator());
              paramAnonymousSurfaceTexture.setDuration(250L);
              paramAnonymousSurfaceTexture.addListener(new AnimatorListenerAdapter()
              {
                public void onAnimationEnd(Animator paramAnonymous2Animator)
                {
                  PhotoViewer.access$12602(PhotoViewer.this, false);
                }
              });
              paramAnonymousSurfaceTexture.start();
            }
            PhotoViewer.access$2702(PhotoViewer.this, 0);
          }
        }
        
        public void onVideoSizeChanged(int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, float paramAnonymousFloat)
        {
          int j;
          int i;
          AspectRatioFrameLayout localAspectRatioFrameLayout;
          if (PhotoViewer.this.aspectRatioFrameLayout != null)
          {
            if (paramAnonymousInt3 != 90)
            {
              j = paramAnonymousInt1;
              i = paramAnonymousInt2;
              if (paramAnonymousInt3 != 270) {}
            }
            else
            {
              i = paramAnonymousInt1;
              j = paramAnonymousInt2;
            }
            localAspectRatioFrameLayout = PhotoViewer.this.aspectRatioFrameLayout;
            if (i != 0) {
              break label61;
            }
          }
          label61:
          for (paramAnonymousFloat = 1.0F;; paramAnonymousFloat = j * paramAnonymousFloat / i)
          {
            localAspectRatioFrameLayout.setAspectRatio(paramAnonymousFloat, paramAnonymousInt3);
            return;
          }
        }
      });
    }
    this.videoPlayer.preparePlayer(paramUri, "other");
    this.videoPlayerSeekbar.setProgress(0.0F);
    this.videoTimelineView.setProgress(0.0F);
    this.videoPlayerSeekbar.setBufferedProgress(0.0F);
    if ((this.currentBotInlineResult != null) && ((this.currentBotInlineResult.type.equals("video")) || (MessageObject.isVideoDocument(this.currentBotInlineResult.document))))
    {
      this.bottomLayout.setVisibility(0);
      this.bottomLayout.setTranslationY(-AndroidUtilities.dp(48.0F));
    }
    paramUri = this.videoPlayerControlFrameLayout;
    if (this.isCurrentVideo) {
      i = 8;
    }
    paramUri.setVisibility(i);
    this.dateTextView.setVisibility(8);
    this.nameTextView.setVisibility(8);
    if (this.allowShare)
    {
      this.shareButton.setVisibility(8);
      this.menuItem.showSubItem(10);
    }
    this.videoPlayer.setPlayWhenReady(paramBoolean1);
    this.inPreview = paramBoolean2;
  }
  
  private void processOpenVideo(final String paramString, boolean paramBoolean)
  {
    if (this.currentLoadingVideoRunnable != null)
    {
      Utilities.globalQueue.cancelRunnable(this.currentLoadingVideoRunnable);
      this.currentLoadingVideoRunnable = null;
    }
    this.videoPreviewMessageObject = null;
    setCompressItemEnabled(false, true);
    this.muteVideo = paramBoolean;
    this.videoTimelineView.setVideoPath(paramString);
    this.compressionsCount = -1;
    this.rotationValue = 0;
    this.videoFramerate = 25;
    this.originalSize = new File(paramString).length();
    DispatchQueue localDispatchQueue = Utilities.globalQueue;
    paramString = new Runnable()
    {
      public void run()
      {
        if (PhotoViewer.this.currentLoadingVideoRunnable != this) {
          return;
        }
        final Object localObject4 = null;
        Object localObject2 = null;
        boolean bool1 = true;
        Object localObject1 = localObject4;
        for (;;)
        {
          int i;
          final boolean bool2;
          long l1;
          int k;
          try
          {
            Object localObject5 = new IsoFile(paramString);
            localObject1 = localObject4;
            List localList = Path.getPaths((Container)localObject5, "/moov/trak/");
            localObject1 = localObject4;
            if (Path.getPath((Container)localObject5, "/moov/trak/mdia/minf/stbl/stsd/mp4a/") == null)
            {
              localObject1 = localObject4;
              if (BuildVars.LOGS_ENABLED)
              {
                localObject1 = localObject4;
                FileLog.d("video hasn't mp4a atom");
              }
            }
            localObject1 = localObject4;
            if (Path.getPath((Container)localObject5, "/moov/trak/mdia/minf/stbl/stsd/avc1/") == null)
            {
              localObject1 = localObject4;
              if (!BuildVars.LOGS_ENABLED) {
                break label892;
              }
              localObject1 = localObject4;
              FileLog.d("video hasn't avc1 atom");
              break label892;
            }
            localObject1 = localObject4;
            PhotoViewer.access$18002(PhotoViewer.this, 0L);
            localObject1 = localObject4;
            PhotoViewer.access$18102(PhotoViewer.this, 0L);
            i = 0;
            localObject1 = localObject2;
            bool2 = bool1;
            localObject4 = localObject2;
            long l3;
            long l2;
            if (i < localList.size())
            {
              localObject1 = localObject2;
              if (PhotoViewer.this.currentLoadingVideoRunnable != this) {
                break;
              }
              localObject1 = localObject2;
              Object localObject7 = (TrackBox)localList.get(i);
              l1 = 0L;
              l3 = 0L;
              localObject5 = null;
              Object localObject6 = null;
              localObject4 = localObject6;
              l2 = l1;
              try
              {
                localObject1 = ((TrackBox)localObject7).getMediaBox();
                localObject5 = localObject1;
                localObject4 = localObject6;
                l2 = l1;
                localObject6 = ((MediaBox)localObject1).getMediaHeaderBox();
                localObject5 = localObject1;
                localObject4 = localObject6;
                l2 = l1;
                long[] arrayOfLong = ((MediaBox)localObject1).getMediaInformationBox().getSampleTableBox().getSampleSizeBox().getSampleSizes();
                j = 0;
                localObject5 = localObject1;
                localObject4 = localObject6;
                l2 = l1;
                if (j < arrayOfLong.length)
                {
                  localObject5 = localObject1;
                  localObject4 = localObject6;
                  l2 = l1;
                  if (PhotoViewer.this.currentLoadingVideoRunnable != this) {
                    break;
                  }
                  l1 += arrayOfLong[j];
                  j += 1;
                  continue;
                }
                localObject5 = localObject1;
                localObject4 = localObject6;
                l2 = l1;
                PhotoViewer.access$10102(PhotoViewer.this, (float)((MediaHeaderBox)localObject6).getDuration() / (float)((MediaHeaderBox)localObject6).getTimescale());
                float f1 = (float)(8L * l1);
                localObject5 = localObject1;
                localObject4 = localObject6;
                l2 = l1;
                float f2 = PhotoViewer.this.videoDuration;
                l2 = (int)(f1 / f2);
                localObject4 = localObject6;
                localObject5 = localObject1;
              }
              catch (Exception localException2)
              {
                int j;
                localObject1 = localObject2;
                FileLog.e(localException2);
                l1 = l2;
                l2 = l3;
                continue;
              }
              localObject1 = localObject2;
              if (PhotoViewer.this.currentLoadingVideoRunnable != this) {
                break;
              }
              localObject1 = localObject2;
              localObject7 = ((TrackBox)localObject7).getTrackHeaderBox();
              localObject1 = localObject2;
              if (((TrackHeaderBox)localObject7).getWidth() == 0.0D) {
                break label863;
              }
              localObject1 = localObject2;
              if (((TrackHeaderBox)localObject7).getHeight() == 0.0D) {
                break label863;
              }
              if (localObject2 != null)
              {
                localObject1 = localObject2;
                if (((TrackHeaderBox)localObject2).getWidth() >= ((TrackHeaderBox)localObject7).getWidth())
                {
                  localObject1 = localObject2;
                  localObject6 = localObject2;
                  if (((TrackHeaderBox)localObject2).getHeight() >= ((TrackHeaderBox)localObject7).getHeight()) {
                    break label898;
                  }
                }
              }
              localObject2 = localObject7;
              localObject1 = localObject2;
              PhotoViewer.access$18202(PhotoViewer.this, PhotoViewer.access$18302(PhotoViewer.this, (int)(l2 / 100000L * 100000L)));
              localObject1 = localObject2;
              if (PhotoViewer.this.bitrate > 900000)
              {
                localObject1 = localObject2;
                PhotoViewer.access$18302(PhotoViewer.this, 900000);
              }
              localObject1 = localObject2;
              PhotoViewer.access$18102(PhotoViewer.this, PhotoViewer.this.videoFramesSize + l1);
              localObject6 = localObject2;
              if (localObject5 == null) {
                break label898;
              }
              localObject6 = localObject2;
              if (localObject4 == null) {
                break label898;
              }
              localObject1 = localObject2;
              localObject5 = ((MediaBox)localObject5).getMediaInformationBox().getSampleTableBox().getTimeToSampleBox();
              localObject6 = localObject2;
              if (localObject5 == null) {
                break label898;
              }
              localObject1 = localObject2;
              localObject5 = ((TimeToSampleBox)localObject5).getEntries();
              l1 = 0L;
              localObject1 = localObject2;
              k = Math.min(((List)localObject5).size(), 11);
              j = 1;
              if (j >= k) {
                break label816;
              }
              localObject1 = localObject2;
              l1 += ((TimeToSampleBox.Entry)((List)localObject5).get(j)).getDelta();
              j += 1;
              continue;
            }
            if (PhotoViewer.this.currentLoadingVideoRunnable != this) {
              break;
            }
          }
          catch (Exception localException1)
          {
            FileLog.e(localException1);
            bool2 = false;
            localObject4 = localObject1;
            if (localObject4 == null)
            {
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("video hasn't trackHeaderBox atom");
              }
              bool2 = false;
            }
          }
          PhotoViewer.access$17902(PhotoViewer.this, null);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (PhotoViewer.this.parentActivity == null) {
                return;
              }
              PhotoViewer.access$18502(PhotoViewer.this, bool2);
              Object localObject;
              label217:
              boolean bool;
              if (bool2)
              {
                localObject = localObject4.getMatrix();
                if (((Matrix)localObject).equals(Matrix.ROTATE_90))
                {
                  PhotoViewer.access$18602(PhotoViewer.this, 90);
                  PhotoViewer.access$18702(PhotoViewer.this, PhotoViewer.access$17402(PhotoViewer.this, (int)localObject4.getWidth()));
                  PhotoViewer.access$18802(PhotoViewer.this, PhotoViewer.access$17502(PhotoViewer.this, (int)localObject4.getHeight()));
                  PhotoViewer.access$10102(PhotoViewer.this, PhotoViewer.this.videoDuration * 1000.0F);
                  localObject = MessagesController.getGlobalMainSettings();
                  PhotoViewer.access$9602(PhotoViewer.this, ((SharedPreferences)localObject).getInt("compress_video2", 1));
                  if ((PhotoViewer.this.originalWidth <= 1280) && (PhotoViewer.this.originalHeight <= 1280)) {
                    break label490;
                  }
                  PhotoViewer.access$17302(PhotoViewer.this, 5);
                  PhotoViewer.this.updateWidthHeightBitrateForCompression();
                  localObject = PhotoViewer.this;
                  if (PhotoViewer.this.compressionsCount <= 1) {
                    break label646;
                  }
                  bool = true;
                  label251:
                  ((PhotoViewer)localObject).setCompressItemEnabled(bool, true);
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("compressionsCount = " + PhotoViewer.this.compressionsCount + " w = " + PhotoViewer.this.originalWidth + " h = " + PhotoViewer.this.originalHeight);
                  }
                  if ((Build.VERSION.SDK_INT >= 18) || (PhotoViewer.this.compressItem.getTag() == null)) {}
                }
                try
                {
                  localObject = MediaController.selectCodec("video/avc");
                  if (localObject != null) {
                    break label651;
                  }
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("no codec info for video/avc");
                  }
                  PhotoViewer.this.setCompressItemEnabled(false, true);
                }
                catch (Exception localException)
                {
                  for (;;)
                  {
                    label387:
                    PhotoViewer.this.setCompressItemEnabled(false, true);
                    FileLog.e(localException);
                    continue;
                    if (MediaController.selectColorFormat(localException, "video/avc") == 0)
                    {
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("no color format for video/avc");
                      }
                      PhotoViewer.this.setCompressItemEnabled(false, true);
                    }
                  }
                }
                PhotoViewer.this.qualityChooseView.invalidate();
              }
              for (;;)
              {
                PhotoViewer.this.updateVideoInfo();
                PhotoViewer.this.updateMuteButton();
                return;
                if (((Matrix)localObject).equals(Matrix.ROTATE_180))
                {
                  PhotoViewer.access$18602(PhotoViewer.this, 180);
                  break;
                }
                if (((Matrix)localObject).equals(Matrix.ROTATE_270))
                {
                  PhotoViewer.access$18602(PhotoViewer.this, 270);
                  break;
                }
                PhotoViewer.access$18602(PhotoViewer.this, 0);
                break;
                label490:
                if ((PhotoViewer.this.originalWidth > 854) || (PhotoViewer.this.originalHeight > 854))
                {
                  PhotoViewer.access$17302(PhotoViewer.this, 4);
                  break label217;
                }
                if ((PhotoViewer.this.originalWidth > 640) || (PhotoViewer.this.originalHeight > 640))
                {
                  PhotoViewer.access$17302(PhotoViewer.this, 3);
                  break label217;
                }
                if ((PhotoViewer.this.originalWidth > 480) || (PhotoViewer.this.originalHeight > 480))
                {
                  PhotoViewer.access$17302(PhotoViewer.this, 2);
                  break label217;
                }
                PhotoViewer.access$17302(PhotoViewer.this, 1);
                break label217;
                label646:
                bool = false;
                break label251;
                label651:
                String str = ((MediaCodecInfo)localObject).getName();
                if ((str.equals("OMX.google.h264.encoder")) || (str.equals("OMX.ST.VFM.H264Enc")) || (str.equals("OMX.Exynos.avc.enc")) || (str.equals("OMX.MARVELL.VIDEO.HW.CODA7542ENCODER")) || (str.equals("OMX.MARVELL.VIDEO.H264ENCODER")) || (str.equals("OMX.k3.video.encoder.avc")) || (str.equals("OMX.TI.DUCATI1.VIDEO.H264E")))
                {
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("unsupported encoder = " + str);
                  }
                  PhotoViewer.this.setCompressItemEnabled(false, true);
                  break label387;
                }
                PhotoViewer.access$17302(PhotoViewer.this, 0);
              }
            }
          });
          return;
          label816:
          Exception localException3 = localException1;
          if (l1 != 0L)
          {
            localObject1 = localException1;
            PhotoViewer.access$18402(PhotoViewer.this, (int)(((MediaHeaderBox)localObject4).getTimescale() / (l1 / (k - 1))));
            localException3 = localException1;
            break label898;
            label863:
            localObject1 = localException1;
            PhotoViewer.access$18002(PhotoViewer.this, PhotoViewer.this.audioFramesSize + l1);
            localException3 = localException1;
            break label898;
            label892:
            bool1 = false;
            continue;
          }
          label898:
          i += 1;
          Object localObject3 = localException3;
        }
      }
    };
    this.currentLoadingVideoRunnable = paramString;
    localDispatchQueue.postRunnable(paramString);
  }
  
  private void redraw(final int paramInt)
  {
    if ((paramInt < 6) && (this.containerView != null))
    {
      this.containerView.invalidate();
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          PhotoViewer.this.redraw(paramInt + 1);
        }
      }, 100L);
    }
  }
  
  private void releasePlayer()
  {
    if (this.videoPlayer != null)
    {
      this.videoPlayer.releasePlayer();
      this.videoPlayer = null;
    }
    toggleMiniProgress(false, false);
    this.pipAvailable = false;
    if (this.pipItem.isEnabled())
    {
      this.pipItem.setEnabled(false);
      this.pipItem.setAlpha(0.5F);
    }
    if (this.keepScreenOnFlagSet) {}
    try
    {
      this.parentActivity.getWindow().clearFlags(128);
      this.keepScreenOnFlagSet = false;
      if (this.aspectRatioFrameLayout != null)
      {
        this.containerView.removeView(this.aspectRatioFrameLayout);
        this.aspectRatioFrameLayout = null;
      }
      if (this.videoTextureView != null) {
        this.videoTextureView = null;
      }
      if (this.isPlaying)
      {
        this.isPlaying = false;
        this.videoPlayButton.setImageResource(2131165428);
        AndroidUtilities.cancelRunOnUIThread(this.updateProgressRunnable);
      }
      if ((!this.inPreview) && (!this.requestingPreview))
      {
        this.videoPlayerControlFrameLayout.setVisibility(8);
        this.dateTextView.setVisibility(0);
        this.nameTextView.setVisibility(0);
        if (this.allowShare)
        {
          this.shareButton.setVisibility(0);
          this.menuItem.hideSubItem(10);
        }
      }
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
  
  private void removeObservers()
  {
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileDidFailedLoad);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileLoadProgressChanged);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mediaCountDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mediaDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.dialogPhotosLoaded);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FilePreparingFailed);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileNewChunkAvailable);
    ConnectionsManager.getInstance(this.currentAccount).cancelRequestsForGuid(this.classGuid);
  }
  
  private void requestVideoPreview(int paramInt)
  {
    if (this.videoPreviewMessageObject != null) {
      MediaController.getInstance().cancelVideoConvert(this.videoPreviewMessageObject);
    }
    int i;
    if ((this.requestingPreview) && (!this.tryStartRequestPreviewOnFinish))
    {
      i = 1;
      this.requestingPreview = false;
      this.loadInitialVideo = false;
      this.progressView.setVisibility(4);
      if (paramInt != 1) {
        break label496;
      }
      if (this.selectedCompression != this.compressionsCount - 1) {
        break label117;
      }
      this.tryStartRequestPreviewOnFinish = false;
      if (i != 0) {
        break label101;
      }
      preparePlayer(this.currentPlayingVideoFile, false, false);
    }
    for (;;)
    {
      this.containerView.invalidate();
      return;
      i = 0;
      break;
      label101:
      this.progressView.setVisibility(0);
      this.loadInitialVideo = true;
      continue;
      label117:
      this.requestingPreview = true;
      releasePlayer();
      if (this.videoPreviewMessageObject == null)
      {
        localObject = new TLRPC.TL_message();
        ((TLRPC.TL_message)localObject).id = 0;
        ((TLRPC.TL_message)localObject).message = "";
        ((TLRPC.TL_message)localObject).media = new TLRPC.TL_messageMediaEmpty();
        ((TLRPC.TL_message)localObject).action = new TLRPC.TL_messageActionEmpty();
        this.videoPreviewMessageObject = new MessageObject(UserConfig.selectedAccount, (TLRPC.Message)localObject, false);
        this.videoPreviewMessageObject.messageOwner.attachPath = new File(FileLoader.getDirectory(4), "video_preview.mp4").getAbsolutePath();
        this.videoPreviewMessageObject.videoEditedInfo = new VideoEditedInfo();
        this.videoPreviewMessageObject.videoEditedInfo.rotationValue = this.rotationValue;
        this.videoPreviewMessageObject.videoEditedInfo.originalWidth = this.originalWidth;
        this.videoPreviewMessageObject.videoEditedInfo.originalHeight = this.originalHeight;
        this.videoPreviewMessageObject.videoEditedInfo.framerate = this.videoFramerate;
        this.videoPreviewMessageObject.videoEditedInfo.originalPath = this.currentPlayingVideoFile.getPath();
      }
      Object localObject = this.videoPreviewMessageObject.videoEditedInfo;
      long l2 = this.startTime;
      ((VideoEditedInfo)localObject).startTime = l2;
      localObject = this.videoPreviewMessageObject.videoEditedInfo;
      long l3 = this.endTime;
      ((VideoEditedInfo)localObject).endTime = l3;
      long l1 = l2;
      if (l2 == -1L) {
        l1 = 0L;
      }
      l2 = l3;
      if (l3 == -1L) {
        l2 = (this.videoDuration * 1000.0F);
      }
      if (l2 - l1 > 5000000L) {
        this.videoPreviewMessageObject.videoEditedInfo.endTime = (5000000L + l1);
      }
      this.videoPreviewMessageObject.videoEditedInfo.bitrate = this.bitrate;
      this.videoPreviewMessageObject.videoEditedInfo.resultWidth = this.resultWidth;
      this.videoPreviewMessageObject.videoEditedInfo.resultHeight = this.resultHeight;
      if (!MediaController.getInstance().scheduleVideoConvert(this.videoPreviewMessageObject, true)) {
        this.tryStartRequestPreviewOnFinish = true;
      }
      this.requestingPreview = true;
      this.progressView.setVisibility(0);
      continue;
      label496:
      this.tryStartRequestPreviewOnFinish = false;
      if (paramInt == 2) {
        preparePlayer(this.currentPlayingVideoFile, false, false);
      }
    }
  }
  
  private void setCompressItemEnabled(boolean paramBoolean1, boolean paramBoolean2)
  {
    float f = 1.0F;
    if (this.compressItem == null) {}
    while (((paramBoolean1) && (this.compressItem.getTag() != null)) || ((!paramBoolean1) && (this.compressItem.getTag() == null))) {
      return;
    }
    ImageView localImageView = this.compressItem;
    if (paramBoolean1)
    {
      localObject = Integer.valueOf(1);
      localImageView.setTag(localObject);
      this.compressItem.setEnabled(paramBoolean1);
      this.compressItem.setClickable(paramBoolean1);
      if (this.compressItemAnimation != null)
      {
        this.compressItemAnimation.cancel();
        this.compressItemAnimation = null;
      }
      if (!paramBoolean2) {
        break label198;
      }
      this.compressItemAnimation = new AnimatorSet();
      localObject = this.compressItemAnimation;
      localImageView = this.compressItem;
      if (!paramBoolean1) {
        break label191;
      }
    }
    label191:
    for (f = 1.0F;; f = 0.5F)
    {
      ((AnimatorSet)localObject).playTogether(new Animator[] { ObjectAnimator.ofFloat(localImageView, "alpha", new float[] { f }) });
      this.compressItemAnimation.setDuration(180L);
      this.compressItemAnimation.setInterpolator(decelerateInterpolator);
      this.compressItemAnimation.start();
      return;
      localObject = null;
      break;
    }
    label198:
    Object localObject = this.compressItem;
    if (paramBoolean1) {}
    for (;;)
    {
      ((ImageView)localObject).setAlpha(f);
      return;
      f = 0.5F;
    }
  }
  
  private void setCurrentCaption(MessageObject paramMessageObject, CharSequence paramCharSequence, boolean paramBoolean)
  {
    label91:
    int i;
    if (this.needCaptionLayout)
    {
      if (this.captionTextView.getParent() != this.pickerView)
      {
        this.captionTextView.setBackgroundDrawable(null);
        this.containerView.removeView(this.captionTextView);
        this.pickerView.addView(this.captionTextView, LayoutHelper.createFrame(-1, -2.0F, 83, 0.0F, 0.0F, 76.0F, 48.0F));
      }
      if (!this.isCurrentVideo) {
        break label452;
      }
      this.captionTextView.setMaxLines(1);
      this.captionTextView.setSingleLine(true);
      if (this.captionTextView.getTag() == null) {
        break label472;
      }
      i = 1;
      label104:
      if (TextUtils.isEmpty(paramCharSequence)) {
        break label560;
      }
      Theme.createChatResources(null, true);
      if ((paramMessageObject == null) || (paramMessageObject.messageOwner.entities.isEmpty())) {
        break label478;
      }
      paramCharSequence = SpannableString.valueOf(paramCharSequence.toString());
      paramMessageObject.addEntitiesToText(paramCharSequence, true, false);
      paramMessageObject = Emoji.replaceEmoji(paramCharSequence, this.captionTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
      this.captionTextView.setTag(paramMessageObject);
      if (this.currentCaptionAnimation != null)
      {
        this.currentCaptionAnimation.cancel();
        this.currentCaptionAnimation = null;
      }
    }
    label452:
    label472:
    label478:
    label524:
    do
    {
      try
      {
        this.captionTextView.setText(paramMessageObject);
        this.captionTextView.setTextColor(-1);
        if ((this.isActionBarVisible) && ((this.bottomLayout.getVisibility() == 0) || (this.pickerView.getVisibility() == 0)))
        {
          j = 1;
          if (j == 0) {
            continue;
          }
          this.captionTextView.setVisibility(0);
          if ((!paramBoolean) || (i != 0)) {
            break label524;
          }
          this.currentCaptionAnimation = new AnimatorSet();
          this.currentCaptionAnimation.setDuration(200L);
          this.currentCaptionAnimation.setInterpolator(decelerateInterpolator);
          this.currentCaptionAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              if (paramAnonymousAnimator.equals(PhotoViewer.this.currentCaptionAnimation)) {
                PhotoViewer.access$16002(PhotoViewer.this, null);
              }
            }
          });
          this.currentCaptionAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.captionTextView, "alpha", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofFloat(this.captionTextView, "translationY", new float[] { AndroidUtilities.dp(5.0F), 0.0F }) });
          this.currentCaptionAnimation.start();
          return;
          if (this.captionTextView.getParent() == this.containerView) {
            break;
          }
          this.captionTextView.setBackgroundColor(2130706432);
          this.pickerView.removeView(this.captionTextView);
          this.containerView.addView(this.captionTextView, LayoutHelper.createFrame(-1, -2.0F, 83, 0.0F, 0.0F, 0.0F, 48.0F));
          break;
          this.captionTextView.setSingleLine(false);
          this.captionTextView.setMaxLines(10);
          break label91;
          i = 0;
          break label104;
          paramMessageObject = Emoji.replaceEmoji(new SpannableStringBuilder(paramCharSequence), this.captionTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
        }
      }
      catch (Exception paramMessageObject)
      {
        for (;;)
        {
          FileLog.e(paramMessageObject);
          continue;
          int j = 0;
        }
        this.captionTextView.setAlpha(1.0F);
        return;
      }
    } while (this.captionTextView.getVisibility() != 0);
    this.captionTextView.setVisibility(4);
    this.captionTextView.setAlpha(0.0F);
    return;
    label560:
    if (this.needCaptionLayout)
    {
      this.captionTextView.setText(LocaleController.getString("AddCaption", 2131492925));
      this.captionTextView.setTag("empty");
      this.captionTextView.setVisibility(0);
      this.captionTextView.setTextColor(-1291845633);
      return;
    }
    this.captionTextView.setTextColor(-1);
    this.captionTextView.setTag(null);
    if ((paramBoolean) && (i != 0))
    {
      this.currentCaptionAnimation = new AnimatorSet();
      this.currentCaptionAnimation.setDuration(200L);
      this.currentCaptionAnimation.setInterpolator(decelerateInterpolator);
      this.currentCaptionAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PhotoViewer.this.currentCaptionAnimation)) {
            PhotoViewer.access$16002(PhotoViewer.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PhotoViewer.this.currentCaptionAnimation))
          {
            PhotoViewer.this.captionTextView.setVisibility(4);
            PhotoViewer.access$16002(PhotoViewer.this, null);
          }
        }
      });
      this.currentCaptionAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.captionTextView, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.captionTextView, "translationY", new float[] { AndroidUtilities.dp(5.0F) }) });
      this.currentCaptionAnimation.start();
      return;
    }
    this.captionTextView.setVisibility(4);
  }
  
  private void setImageIndex(int paramInt, boolean paramBoolean)
  {
    if ((this.currentIndex == paramInt) || (this.placeProvider == null)) {}
    int m;
    label207:
    label223:
    label249:
    label300:
    label1036:
    label1047:
    label1058:
    label1064:
    do
    {
      return;
      if ((!paramBoolean) && (this.currentThumb != null))
      {
        this.currentThumb.release();
        this.currentThumb = null;
      }
      this.currentFileNames[0] = getFileName(paramInt);
      this.currentFileNames[1] = getFileName(paramInt + 1);
      this.currentFileNames[2] = getFileName(paramInt - 1);
      this.placeProvider.willSwitchFromPhoto(this.currentMessageObject, this.currentFileLocation, this.currentIndex);
      m = this.currentIndex;
      this.currentIndex = paramInt;
      setIsAboutToSwitchToIndex(this.currentIndex, paramBoolean);
      boolean bool = false;
      int k = 0;
      int j = 0;
      Object localObject2 = null;
      int i;
      if (!this.imagesArr.isEmpty())
      {
        if ((this.currentIndex < 0) || (this.currentIndex >= this.imagesArr.size()))
        {
          closePhoto(false, false);
          return;
        }
        localObject1 = (MessageObject)this.imagesArr.get(this.currentIndex);
        if ((this.currentMessageObject != null) && (this.currentMessageObject.getId() == ((MessageObject)localObject1).getId()))
        {
          i = 1;
          this.currentMessageObject = ((MessageObject)localObject1);
          paramBoolean = ((MessageObject)localObject1).isVideo();
          localObject1 = localObject2;
          if (this.currentPlaceObject != null)
          {
            if (this.animationInProgress != 0) {
              break label1036;
            }
            this.currentPlaceObject.imageReceiver.setVisible(true, true);
          }
          this.currentPlaceObject = this.placeProvider.getPlaceForPhoto(this.currentMessageObject, this.currentFileLocation, this.currentIndex);
          if (this.currentPlaceObject != null)
          {
            if (this.animationInProgress != 0) {
              break label1047;
            }
            this.currentPlaceObject.imageReceiver.setVisible(false, true);
          }
          if (i == 0)
          {
            this.draggingDown = false;
            this.translationX = 0.0F;
            this.translationY = 0.0F;
            this.scale = 1.0F;
            this.animateToX = 0.0F;
            this.animateToY = 0.0F;
            this.animateToScale = 1.0F;
            this.animationStartTime = 0L;
            this.imageMoveAnimation = null;
            this.changeModeAnimation = null;
            if (this.aspectRatioFrameLayout != null) {
              this.aspectRatioFrameLayout.setVisibility(4);
            }
            this.pinchStartDistance = 0.0F;
            this.pinchStartScale = 1.0F;
            this.pinchCenterX = 0.0F;
            this.pinchCenterY = 0.0F;
            this.pinchStartX = 0.0F;
            this.pinchStartY = 0.0F;
            this.moveStartX = 0.0F;
            this.moveStartY = 0.0F;
            this.zooming = false;
            this.moving = false;
            this.doubleTap = false;
            this.invalidCoords = false;
            this.canDragDown = true;
            this.changingPage = false;
            this.switchImageAfterAnimation = 0;
            if ((this.imagesArrLocals.isEmpty()) && ((this.currentFileNames[0] == null) || (paramBoolean) || (this.photoProgressViews[0].backgroundState == 0))) {
              break label1058;
            }
          }
        }
      }
      for (bool = true;; bool = false)
      {
        this.canZoom = bool;
        updateMinMax(this.scale);
        releasePlayer();
        if ((paramBoolean) && (localObject1 != null))
        {
          this.isStreaming = false;
          preparePlayer((Uri)localObject1, false, false);
        }
        if (m != -1) {
          break label1064;
        }
        setImages();
        paramInt = 0;
        while (paramInt < 3)
        {
          checkProgress(paramInt, false);
          paramInt += 1;
        }
        break;
        i = 0;
        break label207;
        if (!this.imagesArrLocations.isEmpty())
        {
          if ((paramInt < 0) || (paramInt >= this.imagesArrLocations.size()))
          {
            closePhoto(false, false);
            return;
          }
          localObject1 = this.currentFileLocation;
          localObject3 = (TLRPC.FileLocation)this.imagesArrLocations.get(paramInt);
          i = k;
          if (localObject1 != null)
          {
            i = k;
            if (localObject3 != null)
            {
              i = k;
              if (((TLRPC.FileLocation)localObject1).local_id == ((TLRPC.FileLocation)localObject3).local_id)
              {
                i = k;
                if (((TLRPC.FileLocation)localObject1).volume_id == ((TLRPC.FileLocation)localObject3).volume_id) {
                  i = 1;
                }
              }
            }
          }
          this.currentFileLocation = ((TLRPC.FileLocation)this.imagesArrLocations.get(paramInt));
          paramBoolean = bool;
          localObject1 = localObject2;
          break label223;
        }
        paramBoolean = bool;
        i = j;
        localObject1 = localObject2;
        if (this.imagesArrLocals.isEmpty()) {
          break label223;
        }
        if ((paramInt < 0) || (paramInt >= this.imagesArrLocals.size()))
        {
          closePhoto(false, false);
          return;
        }
        Object localObject3 = this.imagesArrLocals.get(paramInt);
        if ((localObject3 instanceof TLRPC.BotInlineResult))
        {
          localObject3 = (TLRPC.BotInlineResult)localObject3;
          this.currentBotInlineResult = ((TLRPC.BotInlineResult)localObject3);
          if (((TLRPC.BotInlineResult)localObject3).document != null)
          {
            this.currentPathObject = FileLoader.getPathToAttach(((TLRPC.BotInlineResult)localObject3).document).getAbsolutePath();
            paramBoolean = MessageObject.isVideoDocument(((TLRPC.BotInlineResult)localObject3).document);
            i = j;
            localObject1 = localObject2;
            break label223;
          }
          if (((TLRPC.BotInlineResult)localObject3).photo != null)
          {
            this.currentPathObject = FileLoader.getPathToAttach(FileLoader.getClosestPhotoSizeWithSize(((TLRPC.BotInlineResult)localObject3).photo.sizes, AndroidUtilities.getPhotoSize())).getAbsolutePath();
            paramBoolean = bool;
            i = j;
            localObject1 = localObject2;
            break label223;
          }
          paramBoolean = bool;
          i = j;
          localObject1 = localObject2;
          if (!(((TLRPC.BotInlineResult)localObject3).content instanceof TLRPC.TL_webDocument)) {
            break label223;
          }
          this.currentPathObject = ((TLRPC.BotInlineResult)localObject3).content.url;
          paramBoolean = ((TLRPC.BotInlineResult)localObject3).type.equals("video");
          i = j;
          localObject1 = localObject2;
          break label223;
        }
        if ((localObject3 instanceof MediaController.PhotoEntry))
        {
          localObject1 = (MediaController.PhotoEntry)localObject3;
          this.currentPathObject = ((MediaController.PhotoEntry)localObject1).path;
          paramBoolean = ((MediaController.PhotoEntry)localObject1).isVideo;
          localObject1 = Uri.fromFile(new File(((MediaController.PhotoEntry)localObject1).path));
          i = j;
          break label223;
        }
        paramBoolean = bool;
        i = j;
        localObject1 = localObject2;
        if (!(localObject3 instanceof MediaController.SearchImage)) {
          break label223;
        }
        localObject1 = (MediaController.SearchImage)localObject3;
        if (((MediaController.SearchImage)localObject1).document != null)
        {
          this.currentPathObject = FileLoader.getPathToAttach(((MediaController.SearchImage)localObject1).document, true).getAbsolutePath();
          paramBoolean = bool;
          i = j;
          localObject1 = localObject2;
          break label223;
        }
        this.currentPathObject = ((MediaController.SearchImage)localObject1).imageUrl;
        paramBoolean = bool;
        i = j;
        localObject1 = localObject2;
        break label223;
        this.showAfterAnimation = this.currentPlaceObject;
        break label249;
        this.hideAfterAnimation = this.currentPlaceObject;
        break label300;
      }
      checkProgress(0, false);
      if (m > this.currentIndex)
      {
        localObject1 = this.rightImage;
        this.rightImage = this.centerImage;
        this.centerImage = this.leftImage;
        this.leftImage = ((ImageReceiver)localObject1);
        localObject1 = this.photoProgressViews[0];
        this.photoProgressViews[0] = this.photoProgressViews[2];
        this.photoProgressViews[2] = localObject1;
        setIndexToImage(this.leftImage, this.currentIndex - 1);
        checkProgress(1, false);
        checkProgress(2, false);
        return;
      }
    } while (m >= this.currentIndex);
    Object localObject1 = this.leftImage;
    this.leftImage = this.centerImage;
    this.centerImage = this.rightImage;
    this.rightImage = ((ImageReceiver)localObject1);
    localObject1 = this.photoProgressViews[0];
    this.photoProgressViews[0] = this.photoProgressViews[1];
    this.photoProgressViews[1] = localObject1;
    setIndexToImage(this.rightImage, this.currentIndex + 1);
    checkProgress(1, false);
    checkProgress(2, false);
  }
  
  private void setImages()
  {
    if (this.animationInProgress == 0)
    {
      setIndexToImage(this.centerImage, this.currentIndex);
      setIndexToImage(this.rightImage, this.currentIndex + 1);
      setIndexToImage(this.leftImage, this.currentIndex - 1);
    }
  }
  
  private void setIndexToImage(ImageReceiver paramImageReceiver, int paramInt)
  {
    paramImageReceiver.setOrientation(0, false);
    Object localObject2;
    Object localObject1;
    int i;
    Object localObject4;
    Object localObject3;
    if (!this.imagesArrLocals.isEmpty())
    {
      paramImageReceiver.setParentMessageObject(null);
      if ((paramInt >= 0) && (paramInt < this.imagesArrLocals.size()))
      {
        Object localObject13 = this.imagesArrLocals.get(paramInt);
        int k = (int)(AndroidUtilities.getPhotoSize() / AndroidUtilities.density);
        localObject2 = null;
        localObject1 = localObject2;
        if (this.currentThumb != null)
        {
          localObject1 = localObject2;
          if (paramImageReceiver == this.centerImage) {
            localObject1 = this.currentThumb;
          }
        }
        localObject6 = localObject1;
        if (localObject1 == null) {
          localObject6 = this.placeProvider.getThumbForPhoto(null, null, paramInt);
        }
        Object localObject12 = null;
        Object localObject10 = null;
        TLRPC.BotInlineResult localBotInlineResult = null;
        Object localObject9 = null;
        Object localObject8 = null;
        Object localObject7 = null;
        int j = 0;
        i = 0;
        Object localObject11 = null;
        boolean bool2 = false;
        boolean bool1;
        if ((localObject13 instanceof MediaController.PhotoEntry))
        {
          localObject1 = (MediaController.PhotoEntry)localObject13;
          bool1 = ((MediaController.PhotoEntry)localObject1).isVideo;
          if (!((MediaController.PhotoEntry)localObject1).isVideo) {
            if (((MediaController.PhotoEntry)localObject1).imagePath != null)
            {
              localObject2 = ((MediaController.PhotoEntry)localObject1).imagePath;
              localObject1 = String.format(Locale.US, "%d_%d", new Object[] { Integer.valueOf(k), Integer.valueOf(k) });
              localObject5 = localObject7;
              paramInt = i;
              localObject4 = localObject8;
              localObject3 = localObject9;
              label231:
              if (localObject3 == null) {
                break label1008;
              }
              if (localObject6 == null) {
                break label996;
              }
              localObject1 = new BitmapDrawable(((ImageReceiver.BitmapHolder)localObject6).bitmap);
              label255:
              if (localObject6 != null) {
                break label1002;
              }
            }
          }
        }
        label996:
        label1002:
        for (localObject2 = ((TLRPC.Document)localObject3).thumb.location;; localObject2 = null)
        {
          paramImageReceiver.setImage((TLObject)localObject3, null, "d", (Drawable)localObject1, (TLRPC.FileLocation)localObject2, String.format(Locale.US, "%d_%d", new Object[] { Integer.valueOf(k), Integer.valueOf(k) }), paramInt, null, 0);
          return;
          paramImageReceiver.setOrientation(((MediaController.PhotoEntry)localObject1).orientation, false);
          localObject2 = ((MediaController.PhotoEntry)localObject1).path;
          break;
          if (((MediaController.PhotoEntry)localObject1).thumbPath != null)
          {
            localObject2 = ((MediaController.PhotoEntry)localObject1).thumbPath;
            localObject3 = localObject9;
            localObject4 = localObject8;
            paramInt = i;
            localObject5 = localObject7;
            localObject1 = localObject11;
            break label231;
          }
          localObject2 = "vthumb://" + ((MediaController.PhotoEntry)localObject1).imageId + ":" + ((MediaController.PhotoEntry)localObject1).path;
          localObject3 = localObject9;
          localObject4 = localObject8;
          paramInt = i;
          localObject5 = localObject7;
          localObject1 = localObject11;
          break label231;
          if ((localObject13 instanceof TLRPC.BotInlineResult))
          {
            localBotInlineResult = (TLRPC.BotInlineResult)localObject13;
            if ((localBotInlineResult.type.equals("video")) || (MessageObject.isVideoDocument(localBotInlineResult.document)))
            {
              if (localBotInlineResult.document != null)
              {
                localObject5 = localBotInlineResult.document.thumb.location;
                localObject3 = localObject9;
                localObject4 = localObject8;
                paramInt = i;
                localObject1 = localObject11;
                localObject2 = localObject10;
                bool1 = bool2;
                break label231;
              }
              localObject3 = localObject9;
              localObject4 = localObject8;
              paramInt = i;
              localObject5 = localObject7;
              localObject1 = localObject11;
              localObject2 = localObject10;
              bool1 = bool2;
              if (!(localBotInlineResult.thumb instanceof TLRPC.TL_webDocument)) {
                break label231;
              }
              localObject4 = (TLRPC.TL_webDocument)localBotInlineResult.thumb;
              localObject3 = localObject9;
              paramInt = i;
              localObject5 = localObject7;
              localObject1 = localObject11;
              localObject2 = localObject10;
              bool1 = bool2;
              break label231;
            }
            if ((localBotInlineResult.type.equals("gif")) && (localBotInlineResult.document != null))
            {
              localObject3 = localBotInlineResult.document;
              paramInt = localBotInlineResult.document.size;
              localObject1 = "d";
              localObject4 = localObject8;
              localObject5 = localObject7;
              localObject2 = localObject10;
              bool1 = bool2;
              break label231;
            }
            if (localBotInlineResult.photo != null)
            {
              localObject1 = FileLoader.getClosestPhotoSizeWithSize(localBotInlineResult.photo.sizes, AndroidUtilities.getPhotoSize());
              localObject5 = ((TLRPC.PhotoSize)localObject1).location;
              paramInt = ((TLRPC.PhotoSize)localObject1).size;
              localObject1 = String.format(Locale.US, "%d_%d", new Object[] { Integer.valueOf(k), Integer.valueOf(k) });
              localObject3 = localObject9;
              localObject4 = localObject8;
              localObject2 = localObject10;
              bool1 = bool2;
              break label231;
            }
            localObject3 = localObject9;
            localObject4 = localObject8;
            paramInt = i;
            localObject5 = localObject7;
            localObject1 = localObject11;
            localObject2 = localObject10;
            bool1 = bool2;
            if (!(localBotInlineResult.content instanceof TLRPC.TL_webDocument)) {
              break label231;
            }
            if (localBotInlineResult.type.equals("gif")) {}
            for (localObject1 = "d";; localObject1 = String.format(Locale.US, "%d_%d", new Object[] { Integer.valueOf(k), Integer.valueOf(k) }))
            {
              localObject4 = (TLRPC.TL_webDocument)localBotInlineResult.content;
              localObject3 = localObject9;
              paramInt = i;
              localObject5 = localObject7;
              localObject2 = localObject10;
              bool1 = bool2;
              break;
            }
          }
          localObject3 = localObject9;
          localObject4 = localObject8;
          paramInt = i;
          localObject5 = localObject7;
          localObject1 = localObject11;
          localObject2 = localObject10;
          bool1 = bool2;
          if (!(localObject13 instanceof MediaController.SearchImage)) {
            break label231;
          }
          localObject1 = (MediaController.SearchImage)localObject13;
          if (((MediaController.SearchImage)localObject1).imagePath != null)
          {
            localObject2 = ((MediaController.SearchImage)localObject1).imagePath;
            paramInt = j;
            localObject3 = localBotInlineResult;
          }
          for (;;)
          {
            localObject1 = "d";
            localObject4 = localObject8;
            localObject5 = localObject7;
            bool1 = bool2;
            break;
            if (((MediaController.SearchImage)localObject1).document != null)
            {
              localObject3 = ((MediaController.SearchImage)localObject1).document;
              paramInt = ((MediaController.SearchImage)localObject1).document.size;
              localObject2 = localObject12;
            }
            else
            {
              localObject2 = ((MediaController.SearchImage)localObject1).imageUrl;
              paramInt = ((MediaController.SearchImage)localObject1).size;
              localObject3 = localBotInlineResult;
            }
          }
          localObject1 = null;
          break label255;
        }
        label1008:
        if (localObject5 != null)
        {
          if (localObject6 != null) {}
          for (localObject2 = new BitmapDrawable(((ImageReceiver.BitmapHolder)localObject6).bitmap);; localObject2 = null)
          {
            paramImageReceiver.setImage((TLObject)localObject5, null, (String)localObject1, (Drawable)localObject2, null, String.format(Locale.US, "%d_%d", new Object[] { Integer.valueOf(k), Integer.valueOf(k) }), paramInt, null, 0);
            return;
          }
        }
        if (localObject4 != null)
        {
          if (localObject6 != null) {
            localObject2 = new BitmapDrawable(((ImageReceiver.BitmapHolder)localObject6).bitmap);
          }
          for (;;)
          {
            paramImageReceiver.setImage((TLObject)localObject4, (String)localObject1, (Drawable)localObject2, null, paramInt);
            return;
            if ((bool1) && (this.parentActivity != null)) {
              localObject2 = this.parentActivity.getResources().getDrawable(2131165542);
            } else {
              localObject2 = null;
            }
          }
        }
        if (localObject6 != null) {
          localObject3 = new BitmapDrawable(((ImageReceiver.BitmapHolder)localObject6).bitmap);
        }
        for (;;)
        {
          paramImageReceiver.setImage((String)localObject2, (String)localObject1, (Drawable)localObject3, null, paramInt);
          return;
          if ((bool1) && (this.parentActivity != null)) {
            localObject3 = this.parentActivity.getResources().getDrawable(2131165542);
          } else {
            localObject3 = null;
          }
        }
      }
      paramImageReceiver.setImageBitmap((Bitmap)null);
      return;
    }
    Object localObject5 = new int[1];
    Object localObject6 = getFileLocation(paramInt, (int[])localObject5);
    if (localObject6 != null)
    {
      localObject2 = null;
      if (!this.imagesArr.isEmpty()) {
        localObject2 = (MessageObject)this.imagesArr.get(paramInt);
      }
      paramImageReceiver.setParentMessageObject((MessageObject)localObject2);
      if (localObject2 != null) {
        paramImageReceiver.setShouldGenerateQualityThumb(true);
      }
      if ((localObject2 != null) && (((MessageObject)localObject2).isVideo()))
      {
        paramImageReceiver.setNeedsQualityThumb(true);
        if ((((MessageObject)localObject2).photoThumbs != null) && (!((MessageObject)localObject2).photoThumbs.isEmpty()))
        {
          localObject3 = null;
          localObject1 = localObject3;
          if (this.currentThumb != null)
          {
            localObject1 = localObject3;
            if (paramImageReceiver == this.centerImage) {
              localObject1 = this.currentThumb;
            }
          }
          localObject2 = FileLoader.getClosestPhotoSizeWithSize(((MessageObject)localObject2).photoThumbs, 100);
          if (localObject1 != null) {}
          for (localObject1 = new BitmapDrawable(((ImageReceiver.BitmapHolder)localObject1).bitmap);; localObject1 = null)
          {
            paramImageReceiver.setImage(null, null, null, (Drawable)localObject1, ((TLRPC.PhotoSize)localObject2).location, "b", 0, null, 1);
            return;
          }
        }
        paramImageReceiver.setImageBitmap(this.parentActivity.getResources().getDrawable(2131165596));
        return;
      }
      if ((localObject2 != null) && (this.currentAnimation != null))
      {
        paramImageReceiver.setImageBitmap(this.currentAnimation);
        this.currentAnimation.setSecondParentView(this.containerView);
        return;
      }
      paramImageReceiver.setNeedsQualityThumb(true);
      localObject1 = null;
      localObject3 = localObject1;
      if (this.currentThumb != null)
      {
        localObject3 = localObject1;
        if (paramImageReceiver == this.centerImage) {
          localObject3 = this.currentThumb;
        }
      }
      if (localObject5[0] == 0) {
        localObject5[0] = -1;
      }
      if (localObject2 != null)
      {
        localObject1 = FileLoader.getClosestPhotoSizeWithSize(((MessageObject)localObject2).photoThumbs, 100);
        localObject4 = localObject1;
        if (localObject1 != null)
        {
          localObject4 = localObject1;
          if (((TLRPC.PhotoSize)localObject1).location == localObject6) {
            localObject4 = null;
          }
        }
        if (((localObject2 == null) || (!((MessageObject)localObject2).isWebpage())) && (this.avatarsDialogId == 0) && (!this.isEvent)) {
          break label1659;
        }
        paramInt = 1;
        label1592:
        if (localObject3 == null) {
          break label1664;
        }
        localObject1 = new BitmapDrawable(((ImageReceiver.BitmapHolder)localObject3).bitmap);
        label1611:
        if (localObject4 == null) {
          break label1670;
        }
        localObject2 = ((TLRPC.PhotoSize)localObject4).location;
        label1623:
        i = localObject5[0];
        if (paramInt == 0) {
          break label1676;
        }
      }
      label1659:
      label1664:
      label1670:
      label1676:
      for (paramInt = 1;; paramInt = 0)
      {
        paramImageReceiver.setImage((TLObject)localObject6, null, null, (Drawable)localObject1, (TLRPC.FileLocation)localObject2, "b", i, null, paramInt);
        return;
        localObject1 = null;
        break;
        paramInt = 0;
        break label1592;
        localObject1 = null;
        break label1611;
        localObject2 = null;
        break label1623;
      }
    }
    paramImageReceiver.setNeedsQualityThumb(true);
    paramImageReceiver.setParentMessageObject(null);
    if (localObject5[0] == 0)
    {
      paramImageReceiver.setImageBitmap((Bitmap)null);
      return;
    }
    paramImageReceiver.setImageBitmap(this.parentActivity.getResources().getDrawable(2131165596));
  }
  
  private void setIsAboutToSwitchToIndex(int paramInt, boolean paramBoolean)
  {
    if ((!paramBoolean) && (this.switchingToIndex == paramInt)) {}
    Object localObject3;
    Object localObject1;
    Object localObject5;
    Object localObject4;
    do
    {
      return;
      this.switchingToIndex = paramInt;
      bool2 = false;
      bool1 = false;
      localObject3 = null;
      localObject1 = null;
      localObject5 = getFileName(paramInt);
      localObject4 = null;
      if (this.imagesArr.isEmpty()) {
        break;
      }
    } while ((this.switchingToIndex < 0) || (this.switchingToIndex >= this.imagesArr.size()));
    Object localObject2 = (MessageObject)this.imagesArr.get(this.switchingToIndex);
    boolean bool1 = ((MessageObject)localObject2).isVideo();
    boolean bool2 = ((MessageObject)localObject2).isInvoice();
    if (bool2)
    {
      this.masksItem.setVisibility(8);
      this.menuItem.hideSubItem(6);
      this.menuItem.hideSubItem(11);
      localObject1 = ((MessageObject)localObject2).messageOwner.media.description;
      this.allowShare = false;
      this.bottomLayout.setTranslationY(AndroidUtilities.dp(48.0F));
      this.captionTextView.setTranslationY(AndroidUtilities.dp(48.0F));
      if (this.currentAnimation == null) {
        break label740;
      }
      this.menuItem.hideSubItem(1);
      this.menuItem.hideSubItem(10);
      if (!((MessageObject)localObject2).canDeleteMessage(null)) {
        this.menuItem.setVisibility(8);
      }
      this.allowShare = true;
      this.shareButton.setVisibility(0);
      this.actionBar.setTitle(LocaleController.getString("AttachGif", 2131493028));
      label250:
      this.groupedPhotosListView.fillList();
      label257:
      if (paramBoolean) {
        break label2871;
      }
    }
    label298:
    label329:
    label386:
    label404:
    label422:
    label542:
    label554:
    label587:
    label710:
    label728:
    label740:
    label820:
    label933:
    label1104:
    label1159:
    label1354:
    label1372:
    label1474:
    label1539:
    label1632:
    label1655:
    label1686:
    label1737:
    label1785:
    label1791:
    label1892:
    label1922:
    label1979:
    label2012:
    label2094:
    label2116:
    label2138:
    label2235:
    label2301:
    label2525:
    label2531:
    label2667:
    label2673:
    label2743:
    label2762:
    label2810:
    label2847:
    label2853:
    label2859:
    label2865:
    label2871:
    for (paramBoolean = true;; paramBoolean = false)
    {
      setCurrentCaption((MessageObject)localObject2, (CharSequence)localObject1, paramBoolean);
      return;
      localObject1 = this.masksItem;
      long l;
      if ((((MessageObject)localObject2).hasPhotoStickers()) && ((int)((MessageObject)localObject2).getDialogId() != 0))
      {
        paramInt = 0;
        ((ActionBarMenuItem)localObject1).setVisibility(paramInt);
        if ((!((MessageObject)localObject2).canDeleteMessage(null)) || (this.slideshowMessageId != 0)) {
          break label542;
        }
        this.menuItem.showSubItem(6);
        if (!bool1) {
          break label554;
        }
        this.menuItem.showSubItem(11);
        if (this.pipItem.getVisibility() != 0) {
          this.pipItem.setVisibility(0);
        }
        if (!this.pipAvailable)
        {
          this.pipItem.setEnabled(false);
          this.pipItem.setAlpha(0.5F);
        }
        if (this.nameOverride == null) {
          break label587;
        }
        this.nameTextView.setText(this.nameOverride);
        if (this.dateOverride == 0) {
          break label710;
        }
        l = this.dateOverride * 1000L;
        localObject1 = LocaleController.formatString("formatDateAtTime", 2131494696, new Object[] { LocaleController.getInstance().formatterYear.format(new Date(l)), LocaleController.getInstance().formatterDay.format(new Date(l)) });
        if ((localObject5 == null) || (!bool1)) {
          break label728;
        }
        this.dateTextView.setText(String.format("%s (%s)", new Object[] { localObject1, AndroidUtilities.formatFileSize(((MessageObject)localObject2).getDocument().size) }));
      }
      for (;;)
      {
        localObject1 = ((MessageObject)localObject2).caption;
        break;
        paramInt = 8;
        break label298;
        this.menuItem.hideSubItem(6);
        break label329;
        this.menuItem.hideSubItem(11);
        if (this.pipItem.getVisibility() == 8) {
          break label386;
        }
        this.pipItem.setVisibility(8);
        break label386;
        if (((MessageObject)localObject2).isFromUser())
        {
          localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((MessageObject)localObject2).messageOwner.from_id));
          if (localObject1 != null)
          {
            this.nameTextView.setText(UserObject.getUserName((TLRPC.User)localObject1));
            break label404;
          }
          this.nameTextView.setText("");
          break label404;
        }
        localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(((MessageObject)localObject2).messageOwner.to_id.channel_id));
        if (localObject1 != null)
        {
          this.nameTextView.setText(((TLRPC.Chat)localObject1).title);
          break label404;
        }
        this.nameTextView.setText("");
        break label404;
        l = ((MessageObject)localObject2).messageOwner.date * 1000L;
        break label422;
        this.dateTextView.setText((CharSequence)localObject1);
      }
      int k;
      int j;
      if ((this.totalImagesCount + this.totalImagesCountMerge != 0) && (!this.needSearchImageInArr)) {
        if (this.opennedFromMedia) {
          if ((this.imagesArr.size() < this.totalImagesCount + this.totalImagesCountMerge) && (!this.loadingMoreImages) && (this.switchingToIndex > this.imagesArr.size() - 5))
          {
            if (this.imagesArr.isEmpty())
            {
              paramInt = 0;
              k = 0;
              j = paramInt;
              i = k;
              if (this.endReached[0] != 0)
              {
                j = paramInt;
                i = k;
                if (this.mergeDialogId != 0L)
                {
                  k = 1;
                  j = paramInt;
                  i = k;
                  if (!this.imagesArr.isEmpty())
                  {
                    j = paramInt;
                    i = k;
                    if (((MessageObject)this.imagesArr.get(this.imagesArr.size() - 1)).getDialogId() != this.mergeDialogId)
                    {
                      j = 0;
                      i = k;
                    }
                  }
                }
              }
              localObject3 = DataQuery.getInstance(this.currentAccount);
              if (i != 0) {
                break label1104;
              }
              l = this.currentDialogId;
              ((DataQuery)localObject3).loadMedia(l, 80, j, 0, true, this.classGuid);
              this.loadingMoreImages = true;
            }
          }
          else {
            this.actionBar.setTitle(LocaleController.formatString("Of", 2131494029, new Object[] { Integer.valueOf(this.switchingToIndex + 1), Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge) }));
          }
        }
      }
      for (;;)
      {
        if ((int)this.currentDialogId == 0) {
          this.sendItem.setVisibility(8);
        }
        if ((((MessageObject)localObject2).messageOwner.ttl == 0) || (((MessageObject)localObject2).messageOwner.ttl >= 3600)) {
          break label1474;
        }
        this.allowShare = false;
        this.menuItem.hideSubItem(1);
        this.shareButton.setVisibility(8);
        this.menuItem.hideSubItem(10);
        break;
        paramInt = ((MessageObject)this.imagesArr.get(this.imagesArr.size() - 1)).getId();
        break label820;
        l = this.mergeDialogId;
        break label933;
        if ((this.imagesArr.size() < this.totalImagesCount + this.totalImagesCountMerge) && (!this.loadingMoreImages) && (this.switchingToIndex < 5))
        {
          if (!this.imagesArr.isEmpty()) {
            break label1354;
          }
          paramInt = 0;
          k = 0;
          j = paramInt;
          i = k;
          if (this.endReached[0] != 0)
          {
            j = paramInt;
            i = k;
            if (this.mergeDialogId != 0L)
            {
              k = 1;
              j = paramInt;
              i = k;
              if (!this.imagesArr.isEmpty())
              {
                j = paramInt;
                i = k;
                if (((MessageObject)this.imagesArr.get(0)).getDialogId() != this.mergeDialogId)
                {
                  j = 0;
                  i = k;
                }
              }
            }
          }
          localObject3 = DataQuery.getInstance(this.currentAccount);
          if (i != 0) {
            break label1372;
          }
        }
        for (l = this.currentDialogId;; l = this.mergeDialogId)
        {
          ((DataQuery)localObject3).loadMedia(l, 80, j, 0, true, this.classGuid);
          this.loadingMoreImages = true;
          this.actionBar.setTitle(LocaleController.formatString("Of", 2131494029, new Object[] { Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge - this.imagesArr.size() + this.switchingToIndex + 1), Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge) }));
          break;
          paramInt = ((MessageObject)this.imagesArr.get(0)).getId();
          break label1159;
        }
        if ((this.slideshowMessageId == 0) && ((((MessageObject)localObject2).messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)))
        {
          if (((MessageObject)localObject2).isVideo()) {
            this.actionBar.setTitle(LocaleController.getString("AttachVideo", 2131493043));
          } else {
            this.actionBar.setTitle(LocaleController.getString("AttachPhoto", 2131493037));
          }
        }
        else if (bool2) {
          this.actionBar.setTitle(((MessageObject)localObject2).messageOwner.media.title);
        }
      }
      this.allowShare = true;
      this.menuItem.showSubItem(1);
      localObject3 = this.shareButton;
      if (this.videoPlayerControlFrameLayout.getVisibility() != 0) {}
      for (paramInt = 0;; paramInt = 8)
      {
        ((ImageView)localObject3).setVisibility(paramInt);
        if (this.shareButton.getVisibility() != 0) {
          break label1539;
        }
        this.menuItem.hideSubItem(10);
        break;
      }
      this.menuItem.showSubItem(10);
      break label250;
      if (!this.imagesArrLocations.isEmpty())
      {
        if ((paramInt < 0) || (paramInt >= this.imagesArrLocations.size())) {
          break;
        }
        this.nameTextView.setText("");
        this.dateTextView.setText("");
        if ((this.avatarsDialogId == UserConfig.getInstance(this.currentAccount).getClientUserId()) && (!this.avatarsArr.isEmpty()))
        {
          this.menuItem.showSubItem(6);
          if (!this.isEvent) {
            break label1737;
          }
          this.actionBar.setTitle(LocaleController.getString("AttachPhoto", 2131493037));
          this.menuItem.showSubItem(1);
          this.allowShare = true;
          localObject2 = this.shareButton;
          if (this.videoPlayerControlFrameLayout.getVisibility() == 0) {
            break label1785;
          }
          paramInt = 0;
          ((ImageView)localObject2).setVisibility(paramInt);
          if (this.shareButton.getVisibility() != 0) {
            break label1791;
          }
          this.menuItem.hideSubItem(10);
        }
        for (;;)
        {
          this.groupedPhotosListView.fillList();
          localObject2 = localObject4;
          break;
          this.menuItem.hideSubItem(6);
          break label1632;
          this.actionBar.setTitle(LocaleController.formatString("Of", 2131494029, new Object[] { Integer.valueOf(this.switchingToIndex + 1), Integer.valueOf(this.imagesArrLocations.size()) }));
          break label1655;
          paramInt = 8;
          break label1686;
          this.menuItem.showSubItem(10);
        }
      }
      localObject2 = localObject4;
      if (this.imagesArrLocals.isEmpty()) {
        break label257;
      }
      if ((paramInt < 0) || (paramInt >= this.imagesArrLocals.size())) {
        break;
      }
      localObject5 = this.imagesArrLocals.get(paramInt);
      int i = 0;
      boolean bool3 = false;
      boolean bool4 = false;
      boolean bool6 = false;
      boolean bool5;
      if ((localObject5 instanceof TLRPC.BotInlineResult))
      {
        localObject1 = (TLRPC.BotInlineResult)localObject5;
        this.currentBotInlineResult = ((TLRPC.BotInlineResult)localObject1);
        if (((TLRPC.BotInlineResult)localObject1).document != null)
        {
          bool1 = MessageObject.isVideoDocument(((TLRPC.BotInlineResult)localObject1).document);
          this.pickerView.setPadding(0, AndroidUtilities.dp(14.0F), 0, 0);
          paramInt = i;
          bool5 = bool1;
          bool2 = bool6;
          localObject1 = localObject3;
          if (this.bottomLayout.getVisibility() != 8) {
            this.bottomLayout.setVisibility(8);
          }
          this.bottomLayout.setTag(null);
          if (!this.fromCamera) {
            break label2762;
          }
          if (!bool5) {
            break label2743;
          }
          this.actionBar.setTitle(LocaleController.getString("AttachVideo", 2131493043));
          if (this.parentChatActivity != null)
          {
            localObject2 = this.parentChatActivity.getCurrentChat();
            if (localObject2 == null) {
              break label2810;
            }
            this.actionBar.setTitle(((TLRPC.Chat)localObject2).title);
          }
          if ((this.sendPhotoType == 0) || ((this.sendPhotoType == 2) && (this.imagesArrLocals.size() > 1))) {
            this.checkImageView.setChecked(this.placeProvider.isPhotoChecked(this.switchingToIndex), false);
          }
          updateCaptionTextForCurrentPhoto(localObject5);
          localObject2 = new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY);
          localObject5 = this.timeItem;
          if (paramInt == 0) {
            break label2847;
          }
          localObject3 = localObject2;
          ((ImageView)localObject5).setColorFilter((ColorFilter)localObject3);
          localObject5 = this.paintItem;
          if (!bool4) {
            break label2853;
          }
          localObject3 = localObject2;
          ((ImageView)localObject5).setColorFilter((ColorFilter)localObject3);
          localObject5 = this.cropItem;
          if (!bool2) {
            break label2859;
          }
          localObject3 = localObject2;
          ((ImageView)localObject5).setColorFilter((ColorFilter)localObject3);
          localObject3 = this.tuneItem;
          if (!bool3) {
            break label2865;
          }
        }
      }
      for (;;)
      {
        ((ImageView)localObject3).setColorFilter((ColorFilter)localObject2);
        localObject2 = localObject4;
        break;
        if (!(((TLRPC.BotInlineResult)localObject1).content instanceof TLRPC.TL_webDocument)) {
          break label1892;
        }
        bool1 = ((TLRPC.BotInlineResult)localObject1).type.equals("video");
        break label1892;
        localObject1 = null;
        j = 0;
        if ((localObject5 instanceof MediaController.PhotoEntry))
        {
          localObject2 = (MediaController.PhotoEntry)localObject5;
          localObject1 = ((MediaController.PhotoEntry)localObject2).path;
          bool1 = ((MediaController.PhotoEntry)localObject2).isVideo;
          paramInt = j;
          if (!bool1) {
            break label2531;
          }
          this.muteItem.setVisibility(0);
          this.compressItem.setVisibility(0);
          this.isCurrentVideo = true;
          bool2 = false;
          if ((localObject5 instanceof MediaController.PhotoEntry))
          {
            localObject2 = (MediaController.PhotoEntry)localObject5;
            if ((((MediaController.PhotoEntry)localObject2).editedInfo == null) || (!((MediaController.PhotoEntry)localObject2).editedInfo.muted)) {
              break label2525;
            }
            bool2 = true;
          }
          processOpenVideo((String)localObject1, bool2);
          this.videoTimelineView.setVisibility(0);
          this.paintItem.setVisibility(8);
          this.cropItem.setVisibility(8);
          this.tuneItem.setVisibility(8);
          if (!(localObject5 instanceof MediaController.PhotoEntry)) {
            break label2673;
          }
          localObject2 = (MediaController.PhotoEntry)localObject5;
          if ((((MediaController.PhotoEntry)localObject2).bucketId != 0) || (((MediaController.PhotoEntry)localObject2).dateTaken != 0L) || (this.imagesArrLocals.size() != 1)) {
            break label2667;
          }
        }
        for (bool2 = true;; bool2 = false)
        {
          this.fromCamera = bool2;
          localObject1 = ((MediaController.PhotoEntry)localObject2).caption;
          paramInt = ((MediaController.PhotoEntry)localObject2).ttl;
          bool3 = ((MediaController.PhotoEntry)localObject2).isFiltered;
          bool4 = ((MediaController.PhotoEntry)localObject2).isPainted;
          bool2 = ((MediaController.PhotoEntry)localObject2).isCropped;
          bool5 = bool1;
          break;
          paramInt = j;
          bool1 = bool2;
          if (!(localObject5 instanceof MediaController.SearchImage)) {
            break label2235;
          }
          MediaController.SearchImage localSearchImage = (MediaController.SearchImage)localObject5;
          if (localSearchImage.document != null) {}
          for (localObject2 = FileLoader.getPathToAttach(localSearchImage.document, true).getAbsolutePath();; localObject2 = localSearchImage.imageUrl)
          {
            paramInt = j;
            bool1 = bool2;
            localObject1 = localObject2;
            if (localSearchImage.type != 1) {
              break;
            }
            paramInt = 1;
            bool1 = bool2;
            localObject1 = localObject2;
            break;
          }
          bool2 = false;
          break label2301;
          this.videoTimelineView.setVisibility(8);
          this.muteItem.setVisibility(8);
          this.isCurrentVideo = false;
          this.compressItem.setVisibility(8);
          if (paramInt != 0)
          {
            this.pickerView.setPadding(0, AndroidUtilities.dp(14.0F), 0, 0);
            this.paintItem.setVisibility(8);
            this.cropItem.setVisibility(8);
            this.tuneItem.setVisibility(8);
          }
          for (;;)
          {
            this.actionBar.setSubtitle(null);
            break;
            if (this.sendPhotoType != 1) {
              this.pickerView.setPadding(0, 0, 0, 0);
            }
            this.paintItem.setVisibility(0);
            this.cropItem.setVisibility(0);
            this.tuneItem.setVisibility(0);
          }
        }
        localObject1 = localObject3;
        bool2 = bool6;
        bool5 = bool1;
        paramInt = i;
        if (!(localObject5 instanceof MediaController.SearchImage)) {
          break label1922;
        }
        localObject2 = (MediaController.SearchImage)localObject5;
        localObject1 = ((MediaController.SearchImage)localObject2).caption;
        paramInt = ((MediaController.SearchImage)localObject2).ttl;
        bool3 = ((MediaController.SearchImage)localObject2).isFiltered;
        bool4 = ((MediaController.SearchImage)localObject2).isPainted;
        bool2 = ((MediaController.SearchImage)localObject2).isCropped;
        bool5 = bool1;
        break label1922;
        this.actionBar.setTitle(LocaleController.getString("AttachPhoto", 2131493037));
        break label1979;
        this.actionBar.setTitle(LocaleController.formatString("Of", 2131494029, new Object[] { Integer.valueOf(this.switchingToIndex + 1), Integer.valueOf(this.imagesArrLocals.size()) }));
        break label1979;
        localObject2 = this.parentChatActivity.getCurrentUser();
        if (localObject2 == null) {
          break label2012;
        }
        this.actionBar.setTitle(ContactsController.formatName(((TLRPC.User)localObject2).first_name, ((TLRPC.User)localObject2).last_name));
        break label2012;
        localObject3 = null;
        break label2094;
        localObject3 = null;
        break label2116;
        localObject3 = null;
        break label2138;
        localObject2 = null;
      }
    }
  }
  
  private void setPhotoChecked()
  {
    int i;
    if (this.placeProvider != null)
    {
      int j = this.placeProvider.setPhotoChecked(this.currentIndex, getCurrentVideoEditedInfo());
      boolean bool = this.placeProvider.isPhotoChecked(this.currentIndex);
      this.checkImageView.setChecked(bool, true);
      if (j >= 0)
      {
        i = j;
        if (this.placeProvider.allowGroupPhotos()) {
          i = j + 1;
        }
        if (!bool) {
          break label95;
        }
        this.selectedPhotosAdapter.notifyItemInserted(i);
        this.selectedPhotosListView.smoothScrollToPosition(i);
      }
    }
    for (;;)
    {
      updateSelectedCount();
      return;
      label95:
      this.selectedPhotosAdapter.notifyItemRemoved(i);
    }
  }
  
  private void setScaleToFill()
  {
    float f5 = this.centerImage.getBitmapWidth();
    float f1 = getContainerViewWidth();
    float f3 = this.centerImage.getBitmapHeight();
    float f2 = getContainerViewHeight();
    float f4 = Math.min(f2 / f3, f1 / f5);
    f5 = (int)(f5 * f4);
    f3 = (int)(f3 * f4);
    this.scale = Math.max(f1 / f5, f2 / f3);
    updateMinMax(this.scale);
  }
  
  private void showDownloadAlert()
  {
    int j = 0;
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(this.parentActivity);
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    int i = j;
    if (this.currentMessageObject != null)
    {
      i = j;
      if (this.currentMessageObject.isVideo())
      {
        i = j;
        if (FileLoader.getInstance(this.currentMessageObject.currentAccount).isLoadingFile(this.currentFileNames[0])) {
          i = 1;
        }
      }
    }
    if (i != 0) {
      localBuilder.setMessage(LocaleController.getString("PleaseStreamDownload", 2131494178));
    }
    for (;;)
    {
      showAlertDialog(localBuilder);
      return;
      localBuilder.setMessage(LocaleController.getString("PleaseDownload", 2131494173));
    }
  }
  
  private void showHint(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((this.containerView == null) || ((paramBoolean1) && (this.hintTextView == null))) {
      return;
    }
    if (this.hintTextView == null)
    {
      this.hintTextView = new TextView(this.containerView.getContext());
      this.hintTextView.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(3.0F), Theme.getColor("chat_gifSaveHintBackground")));
      this.hintTextView.setTextColor(Theme.getColor("chat_gifSaveHintText"));
      this.hintTextView.setTextSize(1, 14.0F);
      this.hintTextView.setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(7.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(7.0F));
      this.hintTextView.setGravity(16);
      this.hintTextView.setAlpha(0.0F);
      this.containerView.addView(this.hintTextView, LayoutHelper.createFrame(-2, -2.0F, 51, 5.0F, 0.0F, 5.0F, 3.0F));
    }
    if (paramBoolean1)
    {
      if (this.hintAnimation != null)
      {
        this.hintAnimation.cancel();
        this.hintAnimation = null;
      }
      AndroidUtilities.cancelRunOnUIThread(this.hintHideRunnable);
      this.hintHideRunnable = null;
      hideHint();
      return;
    }
    TextView localTextView = this.hintTextView;
    Object localObject;
    if (paramBoolean2)
    {
      localObject = LocaleController.getString("GroupPhotosHelp", 2131493635);
      localTextView.setText((CharSequence)localObject);
      if (this.hintHideRunnable == null) {
        break label387;
      }
      if (this.hintAnimation == null) {
        break label358;
      }
      this.hintAnimation.cancel();
      this.hintAnimation = null;
    }
    label358:
    label387:
    while (this.hintAnimation == null)
    {
      this.hintTextView.setVisibility(0);
      this.hintAnimation = new AnimatorSet();
      this.hintAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.hintTextView, "alpha", new float[] { 1.0F }) });
      this.hintAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PhotoViewer.this.hintAnimation)) {
            PhotoViewer.access$17002(PhotoViewer.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PhotoViewer.this.hintAnimation))
          {
            PhotoViewer.access$17002(PhotoViewer.this, null);
            AndroidUtilities.runOnUIThread(PhotoViewer.access$17102(PhotoViewer.this, new Runnable()
            {
              public void run()
              {
                PhotoViewer.this.hideHint();
              }
            }), 2000L);
          }
        }
      });
      this.hintAnimation.setDuration(300L);
      this.hintAnimation.start();
      return;
      localObject = LocaleController.getString("SinglePhotosHelp", 2131494407);
      break;
      AndroidUtilities.cancelRunOnUIThread(this.hintHideRunnable);
      localObject = new Runnable()
      {
        public void run()
        {
          PhotoViewer.this.hideHint();
        }
      };
      this.hintHideRunnable = ((Runnable)localObject);
      AndroidUtilities.runOnUIThread((Runnable)localObject, 2000L);
      return;
    }
  }
  
  private void showQualityView(final boolean paramBoolean)
  {
    if (paramBoolean) {
      this.previousCompression = this.selectedCompression;
    }
    if (this.qualityChooseViewAnimation != null) {
      this.qualityChooseViewAnimation.cancel();
    }
    this.qualityChooseViewAnimation = new AnimatorSet();
    if (paramBoolean)
    {
      this.qualityChooseView.setTag(Integer.valueOf(1));
      this.qualityChooseViewAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.pickerView, "translationY", new float[] { 0.0F, AndroidUtilities.dp(152.0F) }), ObjectAnimator.ofFloat(this.pickerViewSendButton, "translationY", new float[] { 0.0F, AndroidUtilities.dp(152.0F) }), ObjectAnimator.ofFloat(this.bottomLayout, "translationY", new float[] { -AndroidUtilities.dp(48.0F), AndroidUtilities.dp(104.0F) }) });
    }
    for (;;)
    {
      this.qualityChooseViewAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          PhotoViewer.access$17602(PhotoViewer.this, null);
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (!paramAnonymousAnimator.equals(PhotoViewer.this.qualityChooseViewAnimation)) {
            return;
          }
          PhotoViewer.access$17602(PhotoViewer.this, new AnimatorSet());
          if (paramBoolean)
          {
            PhotoViewer.this.qualityChooseView.setVisibility(0);
            PhotoViewer.this.qualityPicker.setVisibility(0);
            PhotoViewer.this.qualityChooseViewAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(PhotoViewer.this.qualityChooseView, "translationY", new float[] { 0.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.qualityPicker, "translationY", new float[] { 0.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.bottomLayout, "translationY", new float[] { -AndroidUtilities.dp(48.0F) }) });
          }
          for (;;)
          {
            PhotoViewer.this.qualityChooseViewAnimation.addListener(new AnimatorListenerAdapter()
            {
              public void onAnimationEnd(Animator paramAnonymous2Animator)
              {
                if (paramAnonymous2Animator.equals(PhotoViewer.this.qualityChooseViewAnimation)) {
                  PhotoViewer.access$17602(PhotoViewer.this, null);
                }
              }
            });
            PhotoViewer.this.qualityChooseViewAnimation.setDuration(200L);
            PhotoViewer.this.qualityChooseViewAnimation.setInterpolator(new AccelerateInterpolator());
            PhotoViewer.this.qualityChooseViewAnimation.start();
            return;
            PhotoViewer.this.qualityChooseView.setVisibility(4);
            PhotoViewer.this.qualityPicker.setVisibility(4);
            PhotoViewer.this.qualityChooseViewAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(PhotoViewer.this.pickerView, "translationY", new float[] { 0.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.pickerViewSendButton, "translationY", new float[] { 0.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.bottomLayout, "translationY", new float[] { -AndroidUtilities.dp(48.0F) }) });
          }
        }
      });
      this.qualityChooseViewAnimation.setDuration(200L);
      this.qualityChooseViewAnimation.setInterpolator(new DecelerateInterpolator());
      this.qualityChooseViewAnimation.start();
      return;
      this.qualityChooseView.setTag(null);
      this.qualityChooseViewAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.qualityChooseView, "translationY", new float[] { 0.0F, AndroidUtilities.dp(166.0F) }), ObjectAnimator.ofFloat(this.qualityPicker, "translationY", new float[] { 0.0F, AndroidUtilities.dp(166.0F) }), ObjectAnimator.ofFloat(this.bottomLayout, "translationY", new float[] { -AndroidUtilities.dp(48.0F), AndroidUtilities.dp(118.0F) }) });
    }
  }
  
  private void switchToEditMode(final int paramInt)
  {
    if ((this.currentEditMode == paramInt) || (this.centerImage.getBitmap() == null) || (this.changeModeAnimation != null) || (this.imageMoveAnimation != null) || (this.photoProgressViews[0].backgroundState != -1) || (this.captionEditText.getTag() != null)) {}
    label152:
    label205:
    label226:
    label250:
    label420:
    label423:
    label430:
    label440:
    label484:
    label1656:
    label1704:
    do
    {
      return;
      int i;
      int j;
      if (paramInt == 0)
      {
        float f1;
        float f4;
        float f2;
        float f3;
        if (this.centerImage.getBitmap() != null)
        {
          i = this.centerImage.getBitmapWidth();
          j = this.centerImage.getBitmapHeight();
          f1 = getContainerViewWidth() / i;
          f4 = getContainerViewHeight() / j;
          f2 = getContainerViewWidth(0) / i;
          f3 = getContainerViewHeight(0) / j;
          if (f1 > f4)
          {
            f1 = f4;
            if (f2 <= f3) {
              break label420;
            }
            f2 = f3;
            if ((this.sendPhotoType != 1) || (this.applying)) {
              break label430;
            }
            f4 = Math.min(getContainerViewWidth(), getContainerViewHeight());
            f3 = f4 / i;
            f4 /= j;
            if (f3 <= f4) {
              break label423;
            }
            this.scale = (f3 / f1);
            this.animateToScale = (this.scale * f2 / f3);
            this.animateToX = 0.0F;
            if (this.currentEditMode != 1) {
              break label440;
            }
            this.animateToY = AndroidUtilities.dp(58.0F);
            if (Build.VERSION.SDK_INT >= 21) {
              this.animateToY -= AndroidUtilities.statusBarHeight / 2;
            }
            this.animationStartTime = System.currentTimeMillis();
            this.zoomAnimation = true;
          }
        }
        else
        {
          this.imageMoveAnimation = new AnimatorSet();
          if (this.currentEditMode != 1) {
            break label484;
          }
          this.imageMoveAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.editorDoneLayout, "translationY", new float[] { AndroidUtilities.dp(48.0F) }), ObjectAnimator.ofFloat(this, "animationValue", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofFloat(this.photoCropView, "alpha", new float[] { 0.0F }) });
        }
        for (;;)
        {
          this.imageMoveAnimation.setDuration(200L);
          this.imageMoveAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              if (PhotoViewer.this.currentEditMode == 1)
              {
                PhotoViewer.this.editorDoneLayout.setVisibility(8);
                PhotoViewer.this.photoCropView.setVisibility(8);
              }
              for (;;)
              {
                PhotoViewer.access$14502(PhotoViewer.this, null);
                PhotoViewer.access$11002(PhotoViewer.this, paramInt);
                PhotoViewer.access$14602(PhotoViewer.this, false);
                PhotoViewer.access$14702(PhotoViewer.this, 1.0F);
                PhotoViewer.access$14802(PhotoViewer.this, 0.0F);
                PhotoViewer.access$14902(PhotoViewer.this, 0.0F);
                PhotoViewer.access$6802(PhotoViewer.this, 1.0F);
                PhotoViewer.this.updateMinMax(PhotoViewer.this.scale);
                PhotoViewer.this.containerView.invalidate();
                paramAnonymousAnimator = new AnimatorSet();
                ArrayList localArrayList = new ArrayList();
                localArrayList.add(ObjectAnimator.ofFloat(PhotoViewer.this.pickerView, "translationY", new float[] { 0.0F }));
                localArrayList.add(ObjectAnimator.ofFloat(PhotoViewer.this.pickerViewSendButton, "translationY", new float[] { 0.0F }));
                localArrayList.add(ObjectAnimator.ofFloat(PhotoViewer.this.actionBar, "translationY", new float[] { 0.0F }));
                if (PhotoViewer.this.needCaptionLayout) {
                  localArrayList.add(ObjectAnimator.ofFloat(PhotoViewer.this.captionTextView, "translationY", new float[] { 0.0F }));
                }
                if (PhotoViewer.this.sendPhotoType == 0)
                {
                  localArrayList.add(ObjectAnimator.ofFloat(PhotoViewer.this.checkImageView, "alpha", new float[] { 1.0F }));
                  localArrayList.add(ObjectAnimator.ofFloat(PhotoViewer.this.photosCounterView, "alpha", new float[] { 1.0F }));
                }
                if (PhotoViewer.this.cameraItem.getTag() != null)
                {
                  PhotoViewer.this.cameraItem.setVisibility(0);
                  localArrayList.add(ObjectAnimator.ofFloat(PhotoViewer.this.cameraItem, "alpha", new float[] { 1.0F }));
                }
                paramAnonymousAnimator.playTogether(localArrayList);
                paramAnonymousAnimator.setDuration(200L);
                paramAnonymousAnimator.addListener(new AnimatorListenerAdapter()
                {
                  public void onAnimationStart(Animator paramAnonymous2Animator)
                  {
                    PhotoViewer.this.pickerView.setVisibility(0);
                    PhotoViewer.this.pickerViewSendButton.setVisibility(0);
                    PhotoViewer.this.actionBar.setVisibility(0);
                    if (PhotoViewer.this.needCaptionLayout)
                    {
                      paramAnonymous2Animator = PhotoViewer.this.captionTextView;
                      if (PhotoViewer.this.captionTextView.getTag() == null) {
                        break label162;
                      }
                    }
                    label162:
                    for (int i = 0;; i = 4)
                    {
                      paramAnonymous2Animator.setVisibility(i);
                      if ((PhotoViewer.this.sendPhotoType == 0) || ((PhotoViewer.this.sendPhotoType == 2) && (PhotoViewer.this.imagesArrLocals.size() > 1)))
                      {
                        PhotoViewer.this.checkImageView.setVisibility(0);
                        PhotoViewer.this.photosCounterView.setVisibility(0);
                      }
                      return;
                    }
                  }
                });
                paramAnonymousAnimator.start();
                return;
                if (PhotoViewer.this.currentEditMode == 2)
                {
                  PhotoViewer.this.containerView.removeView(PhotoViewer.this.photoFilterView);
                  PhotoViewer.access$14302(PhotoViewer.this, null);
                }
                else if (PhotoViewer.this.currentEditMode == 3)
                {
                  PhotoViewer.this.containerView.removeView(PhotoViewer.this.photoPaintView);
                  PhotoViewer.access$14402(PhotoViewer.this, null);
                }
              }
            }
          });
          this.imageMoveAnimation.start();
          return;
          break;
          break label152;
          f3 = f4;
          break label205;
          this.animateToScale = (f2 / f1);
          break label226;
          if (this.currentEditMode == 2)
          {
            this.animateToY = AndroidUtilities.dp(92.0F);
            break label250;
          }
          if (this.currentEditMode != 3) {
            break label250;
          }
          this.animateToY = AndroidUtilities.dp(44.0F);
          break label250;
          if (this.currentEditMode == 2)
          {
            this.photoFilterView.shutdown();
            this.imageMoveAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.photoFilterView.getToolsView(), "translationY", new float[] { AndroidUtilities.dp(186.0F) }), ObjectAnimator.ofFloat(this, "animationValue", new float[] { 0.0F, 1.0F }) });
          }
          else if (this.currentEditMode == 3)
          {
            this.photoPaintView.shutdown();
            this.imageMoveAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.photoPaintView.getToolsView(), "translationY", new float[] { AndroidUtilities.dp(126.0F) }), ObjectAnimator.ofFloat(this.photoPaintView.getColorPicker(), "translationY", new float[] { AndroidUtilities.dp(126.0F) }), ObjectAnimator.ofFloat(this, "animationValue", new float[] { 0.0F, 1.0F }) });
          }
        }
      }
      if (paramInt == 1)
      {
        if (this.photoCropView == null)
        {
          this.photoCropView = new PhotoCropView(this.actvityContext);
          this.photoCropView.setVisibility(8);
          this.containerView.addView(this.photoCropView, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, 48.0F));
          this.photoCropView.setDelegate(new PhotoCropView.PhotoCropViewDelegate()
          {
            public Bitmap getBitmap()
            {
              return PhotoViewer.this.centerImage.getBitmap();
            }
            
            public void needMoveImageTo(float paramAnonymousFloat1, float paramAnonymousFloat2, float paramAnonymousFloat3, boolean paramAnonymousBoolean)
            {
              if (paramAnonymousBoolean)
              {
                PhotoViewer.this.animateTo(paramAnonymousFloat3, paramAnonymousFloat1, paramAnonymousFloat2, true);
                return;
              }
              PhotoViewer.access$6902(PhotoViewer.this, paramAnonymousFloat1);
              PhotoViewer.access$7002(PhotoViewer.this, paramAnonymousFloat2);
              PhotoViewer.access$6802(PhotoViewer.this, paramAnonymousFloat3);
              PhotoViewer.this.containerView.invalidate();
            }
            
            public void onChange(boolean paramAnonymousBoolean)
            {
              TextView localTextView = PhotoViewer.this.resetButton;
              if (paramAnonymousBoolean) {}
              for (int i = 8;; i = 0)
              {
                localTextView.setVisibility(i);
                return;
              }
            }
          });
        }
        this.photoCropView.onAppear();
        this.editorDoneLayout.doneButton.setText(LocaleController.getString("Crop", 2131493312));
        this.editorDoneLayout.doneButton.setTextColor(-11420173);
        this.changeModeAnimation = new AnimatorSet();
        localObject1 = new ArrayList();
        ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.pickerView, "translationY", new float[] { 0.0F, AndroidUtilities.dp(96.0F) }));
        ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.pickerViewSendButton, "translationY", new float[] { 0.0F, AndroidUtilities.dp(96.0F) }));
        ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.actionBar, "translationY", new float[] { 0.0F, -this.actionBar.getHeight() }));
        if (this.needCaptionLayout) {
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.captionTextView, "translationY", new float[] { 0.0F, AndroidUtilities.dp(96.0F) }));
        }
        if (this.sendPhotoType == 0)
        {
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.checkImageView, "alpha", new float[] { 1.0F, 0.0F }));
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.photosCounterView, "alpha", new float[] { 1.0F, 0.0F }));
        }
        if (this.selectedPhotosListView.getVisibility() == 0) {
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.selectedPhotosListView, "alpha", new float[] { 1.0F, 0.0F }));
        }
        if (this.cameraItem.getTag() != null) {
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.cameraItem, "alpha", new float[] { 1.0F, 0.0F }));
        }
        this.changeModeAnimation.playTogether((Collection)localObject1);
        this.changeModeAnimation.setDuration(200L);
        this.changeModeAnimation.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            PhotoViewer.access$15202(PhotoViewer.this, null);
            PhotoViewer.this.pickerView.setVisibility(8);
            PhotoViewer.this.pickerViewSendButton.setVisibility(8);
            PhotoViewer.this.cameraItem.setVisibility(8);
            PhotoViewer.this.selectedPhotosListView.setVisibility(8);
            PhotoViewer.this.selectedPhotosListView.setAlpha(0.0F);
            PhotoViewer.this.selectedPhotosListView.setTranslationY(-AndroidUtilities.dp(10.0F));
            PhotoViewer.this.photosCounterView.setRotationX(0.0F);
            PhotoViewer.this.selectedPhotosListView.setEnabled(false);
            PhotoViewer.access$11702(PhotoViewer.this, false);
            if (PhotoViewer.this.needCaptionLayout) {
              PhotoViewer.this.captionTextView.setVisibility(4);
            }
            if ((PhotoViewer.this.sendPhotoType == 0) || ((PhotoViewer.this.sendPhotoType == 2) && (PhotoViewer.this.imagesArrLocals.size() > 1)))
            {
              PhotoViewer.this.checkImageView.setVisibility(8);
              PhotoViewer.this.photosCounterView.setVisibility(8);
            }
            paramAnonymousAnimator = PhotoViewer.this.centerImage.getBitmap();
            boolean bool;
            int j;
            float f1;
            float f3;
            if (paramAnonymousAnimator != null)
            {
              PhotoCropView localPhotoCropView = PhotoViewer.this.photoCropView;
              i = PhotoViewer.this.centerImage.getOrientation();
              if (PhotoViewer.this.sendPhotoType == 1) {
                break label642;
              }
              bool = true;
              localPhotoCropView.setBitmap(paramAnonymousAnimator, i, bool);
              i = PhotoViewer.this.centerImage.getBitmapWidth();
              j = PhotoViewer.this.centerImage.getBitmapHeight();
              float f2 = PhotoViewer.this.getContainerViewWidth() / i;
              float f4 = PhotoViewer.this.getContainerViewHeight() / j;
              f1 = PhotoViewer.this.getContainerViewWidth(1) / i;
              f3 = PhotoViewer.this.getContainerViewHeight(1) / j;
              if (f2 <= f4) {
                break label648;
              }
              f2 = f4;
              label346:
              if (f1 <= f3) {
                break label651;
              }
              f1 = f3;
              label356:
              if (PhotoViewer.this.sendPhotoType == 1)
              {
                f3 = Math.min(PhotoViewer.this.getContainerViewWidth(1), PhotoViewer.this.getContainerViewHeight(1));
                f1 = f3 / i;
                f3 /= j;
                if (f1 <= f3) {
                  break label654;
                }
              }
              label411:
              PhotoViewer.access$14702(PhotoViewer.this, f1 / f2);
              PhotoViewer.access$14802(PhotoViewer.this, 0.0F);
              paramAnonymousAnimator = PhotoViewer.this;
              j = -AndroidUtilities.dp(56.0F);
              if (Build.VERSION.SDK_INT < 21) {
                break label660;
              }
            }
            label642:
            label648:
            label651:
            label654:
            label660:
            for (int i = AndroidUtilities.statusBarHeight / 2;; i = 0)
            {
              PhotoViewer.access$14902(paramAnonymousAnimator, i + j);
              PhotoViewer.access$15502(PhotoViewer.this, System.currentTimeMillis());
              PhotoViewer.access$15602(PhotoViewer.this, true);
              PhotoViewer.access$14502(PhotoViewer.this, new AnimatorSet());
              PhotoViewer.this.imageMoveAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(PhotoViewer.this.editorDoneLayout, "translationY", new float[] { AndroidUtilities.dp(48.0F), 0.0F }), ObjectAnimator.ofFloat(PhotoViewer.this, "animationValue", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.photoCropView, "alpha", new float[] { 0.0F, 1.0F }) });
              PhotoViewer.this.imageMoveAnimation.setDuration(200L);
              PhotoViewer.this.imageMoveAnimation.addListener(new AnimatorListenerAdapter()
              {
                public void onAnimationEnd(Animator paramAnonymous2Animator)
                {
                  PhotoViewer.this.photoCropView.onAppeared();
                  PhotoViewer.access$14502(PhotoViewer.this, null);
                  PhotoViewer.access$11002(PhotoViewer.this, PhotoViewer.52.this.val$mode);
                  PhotoViewer.access$14702(PhotoViewer.this, 1.0F);
                  PhotoViewer.access$14802(PhotoViewer.this, 0.0F);
                  PhotoViewer.access$14902(PhotoViewer.this, 0.0F);
                  PhotoViewer.access$6802(PhotoViewer.this, 1.0F);
                  PhotoViewer.this.updateMinMax(PhotoViewer.this.scale);
                  PhotoViewer.this.containerView.invalidate();
                }
                
                public void onAnimationStart(Animator paramAnonymous2Animator)
                {
                  PhotoViewer.this.editorDoneLayout.setVisibility(0);
                  PhotoViewer.this.photoCropView.setVisibility(0);
                }
              });
              PhotoViewer.this.imageMoveAnimation.start();
              return;
              bool = false;
              break;
              break label346;
              break label356;
              f1 = f3;
              break label411;
            }
          }
        });
        this.changeModeAnimation.start();
        return;
      }
      if (paramInt == 2)
      {
        MediaController.PhotoEntry localPhotoEntry;
        Object localObject5;
        Object localObject2;
        Object localObject6;
        if (this.photoFilterView == null)
        {
          localPhotoEntry = null;
          Object localObject3 = null;
          localObject5 = null;
          Object localObject4 = null;
          j = 0;
          i = j;
          localObject2 = localObject5;
          localObject1 = localPhotoEntry;
          if (!this.imagesArrLocals.isEmpty())
          {
            localObject6 = this.imagesArrLocals.get(this.currentIndex);
            if (!(localObject6 instanceof MediaController.PhotoEntry)) {
              break label1656;
            }
            localPhotoEntry = (MediaController.PhotoEntry)localObject6;
            localObject2 = localObject4;
            localObject1 = localObject3;
            if (localPhotoEntry.imagePath == null)
            {
              localObject2 = localPhotoEntry.path;
              localObject1 = localPhotoEntry.savedFilterState;
            }
            i = localPhotoEntry.orientation;
          }
          if (localObject1 != null) {
            break label1704;
          }
          localObject2 = this.centerImage.getBitmap();
          i = this.centerImage.getOrientation();
        }
        for (;;)
        {
          this.photoFilterView = new PhotoFilterView(this.parentActivity, (Bitmap)localObject2, i, (MediaController.SavedFilterState)localObject1);
          this.containerView.addView(this.photoFilterView, LayoutHelper.createFrame(-1, -1.0F));
          this.photoFilterView.getDoneTextView().setOnClickListener(new View.OnClickListener()
          {
            public void onClick(View paramAnonymousView)
            {
              PhotoViewer.this.applyCurrentEditMode();
              PhotoViewer.this.switchToEditMode(0);
            }
          });
          this.photoFilterView.getCancelTextView().setOnClickListener(new View.OnClickListener()
          {
            public void onClick(View paramAnonymousView)
            {
              if (PhotoViewer.this.photoFilterView.hasChanges())
              {
                if (PhotoViewer.this.parentActivity == null) {
                  return;
                }
                paramAnonymousView = new AlertDialog.Builder(PhotoViewer.this.parentActivity);
                paramAnonymousView.setMessage(LocaleController.getString("DiscardChanges", 2131493388));
                paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
                paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                {
                  public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                  {
                    PhotoViewer.this.switchToEditMode(0);
                  }
                });
                paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                PhotoViewer.this.showAlertDialog(paramAnonymousView);
                return;
              }
              PhotoViewer.this.switchToEditMode(0);
            }
          });
          this.photoFilterView.getToolsView().setTranslationY(AndroidUtilities.dp(186.0F));
          this.changeModeAnimation = new AnimatorSet();
          localObject1 = new ArrayList();
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.pickerView, "translationY", new float[] { 0.0F, AndroidUtilities.dp(96.0F) }));
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.pickerViewSendButton, "translationY", new float[] { 0.0F, AndroidUtilities.dp(96.0F) }));
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.actionBar, "translationY", new float[] { 0.0F, -this.actionBar.getHeight() }));
          if (this.sendPhotoType == 0)
          {
            ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.checkImageView, "alpha", new float[] { 1.0F, 0.0F }));
            ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.photosCounterView, "alpha", new float[] { 1.0F, 0.0F }));
          }
          if (this.selectedPhotosListView.getVisibility() == 0) {
            ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.selectedPhotosListView, "alpha", new float[] { 1.0F, 0.0F }));
          }
          if (this.cameraItem.getTag() != null) {
            ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.cameraItem, "alpha", new float[] { 1.0F, 0.0F }));
          }
          this.changeModeAnimation.playTogether((Collection)localObject1);
          this.changeModeAnimation.setDuration(200L);
          this.changeModeAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              PhotoViewer.access$15202(PhotoViewer.this, null);
              PhotoViewer.this.pickerView.setVisibility(8);
              PhotoViewer.this.pickerViewSendButton.setVisibility(8);
              PhotoViewer.this.actionBar.setVisibility(8);
              PhotoViewer.this.cameraItem.setVisibility(8);
              PhotoViewer.this.selectedPhotosListView.setVisibility(8);
              PhotoViewer.this.selectedPhotosListView.setAlpha(0.0F);
              PhotoViewer.this.selectedPhotosListView.setTranslationY(-AndroidUtilities.dp(10.0F));
              PhotoViewer.this.photosCounterView.setRotationX(0.0F);
              PhotoViewer.this.selectedPhotosListView.setEnabled(false);
              PhotoViewer.access$11702(PhotoViewer.this, false);
              if (PhotoViewer.this.needCaptionLayout) {
                PhotoViewer.this.captionTextView.setVisibility(4);
              }
              if ((PhotoViewer.this.sendPhotoType == 0) || ((PhotoViewer.this.sendPhotoType == 2) && (PhotoViewer.this.imagesArrLocals.size() > 1)))
              {
                PhotoViewer.this.checkImageView.setVisibility(8);
                PhotoViewer.this.photosCounterView.setVisibility(8);
              }
              int j;
              if (PhotoViewer.this.centerImage.getBitmap() != null)
              {
                i = PhotoViewer.this.centerImage.getBitmapWidth();
                j = PhotoViewer.this.centerImage.getBitmapHeight();
                float f1 = PhotoViewer.this.getContainerViewWidth() / i;
                float f4 = PhotoViewer.this.getContainerViewHeight() / j;
                float f2 = PhotoViewer.this.getContainerViewWidth(2) / i;
                float f3 = PhotoViewer.this.getContainerViewHeight(2) / j;
                if (f1 <= f4) {
                  break label529;
                }
                f1 = f4;
                if (f2 <= f3) {
                  break label532;
                }
                f2 = f3;
                label321:
                PhotoViewer.access$14702(PhotoViewer.this, f2 / f1);
                PhotoViewer.access$14802(PhotoViewer.this, 0.0F);
                paramAnonymousAnimator = PhotoViewer.this;
                j = -AndroidUtilities.dp(92.0F);
                if (Build.VERSION.SDK_INT < 21) {
                  break label535;
                }
              }
              label529:
              label532:
              label535:
              for (int i = AndroidUtilities.statusBarHeight / 2;; i = 0)
              {
                PhotoViewer.access$14902(paramAnonymousAnimator, i + j);
                PhotoViewer.access$15502(PhotoViewer.this, System.currentTimeMillis());
                PhotoViewer.access$15602(PhotoViewer.this, true);
                PhotoViewer.access$14502(PhotoViewer.this, new AnimatorSet());
                PhotoViewer.this.imageMoveAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(PhotoViewer.this, "animationValue", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.photoFilterView.getToolsView(), "translationY", new float[] { AndroidUtilities.dp(186.0F), 0.0F }) });
                PhotoViewer.this.imageMoveAnimation.setDuration(200L);
                PhotoViewer.this.imageMoveAnimation.addListener(new AnimatorListenerAdapter()
                {
                  public void onAnimationEnd(Animator paramAnonymous2Animator)
                  {
                    PhotoViewer.this.photoFilterView.init();
                    PhotoViewer.access$14502(PhotoViewer.this, null);
                    PhotoViewer.access$11002(PhotoViewer.this, PhotoViewer.55.this.val$mode);
                    PhotoViewer.access$14702(PhotoViewer.this, 1.0F);
                    PhotoViewer.access$14802(PhotoViewer.this, 0.0F);
                    PhotoViewer.access$14902(PhotoViewer.this, 0.0F);
                    PhotoViewer.access$6802(PhotoViewer.this, 1.0F);
                    PhotoViewer.this.updateMinMax(PhotoViewer.this.scale);
                    PhotoViewer.this.containerView.invalidate();
                  }
                  
                  public void onAnimationStart(Animator paramAnonymous2Animator) {}
                });
                PhotoViewer.this.imageMoveAnimation.start();
                return;
                break;
                break label321;
              }
            }
          });
          this.changeModeAnimation.start();
          return;
          i = j;
          localObject2 = localObject5;
          localObject1 = localPhotoEntry;
          if (!(localObject6 instanceof MediaController.SearchImage)) {
            break;
          }
          localObject2 = (MediaController.SearchImage)localObject6;
          localObject1 = ((MediaController.SearchImage)localObject2).savedFilterState;
          localObject2 = ((MediaController.SearchImage)localObject2).imageUrl;
          i = j;
          break;
          localObject2 = BitmapFactory.decodeFile((String)localObject2);
        }
      }
    } while (paramInt != 3);
    if (this.photoPaintView == null)
    {
      this.photoPaintView = new PhotoPaintView(this.parentActivity, this.centerImage.getBitmap(), this.centerImage.getOrientation());
      this.containerView.addView(this.photoPaintView, LayoutHelper.createFrame(-1, -1.0F));
      this.photoPaintView.getDoneTextView().setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          PhotoViewer.this.applyCurrentEditMode();
          PhotoViewer.this.switchToEditMode(0);
        }
      });
      this.photoPaintView.getCancelTextView().setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          PhotoViewer.this.photoPaintView.maybeShowDismissalAlert(PhotoViewer.this, PhotoViewer.this.parentActivity, new Runnable()
          {
            public void run()
            {
              PhotoViewer.this.switchToEditMode(0);
            }
          });
        }
      });
      this.photoPaintView.getColorPicker().setTranslationY(AndroidUtilities.dp(126.0F));
      this.photoPaintView.getToolsView().setTranslationY(AndroidUtilities.dp(126.0F));
    }
    this.changeModeAnimation = new AnimatorSet();
    Object localObject1 = new ArrayList();
    ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.pickerView, "translationY", new float[] { 0.0F, AndroidUtilities.dp(96.0F) }));
    ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.pickerViewSendButton, "translationY", new float[] { 0.0F, AndroidUtilities.dp(96.0F) }));
    ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.actionBar, "translationY", new float[] { 0.0F, -this.actionBar.getHeight() }));
    if (this.needCaptionLayout) {
      ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.captionTextView, "translationY", new float[] { 0.0F, AndroidUtilities.dp(96.0F) }));
    }
    if (this.sendPhotoType == 0)
    {
      ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.checkImageView, "alpha", new float[] { 1.0F, 0.0F }));
      ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.photosCounterView, "alpha", new float[] { 1.0F, 0.0F }));
    }
    if (this.selectedPhotosListView.getVisibility() == 0) {
      ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.selectedPhotosListView, "alpha", new float[] { 1.0F, 0.0F }));
    }
    if (this.cameraItem.getTag() != null) {
      ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(this.cameraItem, "alpha", new float[] { 1.0F, 0.0F }));
    }
    this.changeModeAnimation.playTogether((Collection)localObject1);
    this.changeModeAnimation.setDuration(200L);
    this.changeModeAnimation.addListener(new AnimatorListenerAdapter()
    {
      public void onAnimationEnd(Animator paramAnonymousAnimator)
      {
        PhotoViewer.access$15202(PhotoViewer.this, null);
        PhotoViewer.this.pickerView.setVisibility(8);
        PhotoViewer.this.pickerViewSendButton.setVisibility(8);
        PhotoViewer.this.cameraItem.setVisibility(8);
        PhotoViewer.this.selectedPhotosListView.setVisibility(8);
        PhotoViewer.this.selectedPhotosListView.setAlpha(0.0F);
        PhotoViewer.this.selectedPhotosListView.setTranslationY(-AndroidUtilities.dp(10.0F));
        PhotoViewer.this.photosCounterView.setRotationX(0.0F);
        PhotoViewer.this.selectedPhotosListView.setEnabled(false);
        PhotoViewer.access$11702(PhotoViewer.this, false);
        if (PhotoViewer.this.needCaptionLayout) {
          PhotoViewer.this.captionTextView.setVisibility(4);
        }
        if ((PhotoViewer.this.sendPhotoType == 0) || ((PhotoViewer.this.sendPhotoType == 2) && (PhotoViewer.this.imagesArrLocals.size() > 1)))
        {
          PhotoViewer.this.checkImageView.setVisibility(8);
          PhotoViewer.this.photosCounterView.setVisibility(8);
        }
        int j;
        if (PhotoViewer.this.centerImage.getBitmap() != null)
        {
          i = PhotoViewer.this.centerImage.getBitmapWidth();
          j = PhotoViewer.this.centerImage.getBitmapHeight();
          float f1 = PhotoViewer.this.getContainerViewWidth() / i;
          float f4 = PhotoViewer.this.getContainerViewHeight() / j;
          float f2 = PhotoViewer.this.getContainerViewWidth(3) / i;
          float f3 = PhotoViewer.this.getContainerViewHeight(3) / j;
          if (f1 <= f4) {
            break label551;
          }
          f1 = f4;
          if (f2 <= f3) {
            break label554;
          }
          f2 = f3;
          label309:
          PhotoViewer.access$14702(PhotoViewer.this, f2 / f1);
          PhotoViewer.access$14802(PhotoViewer.this, 0.0F);
          paramAnonymousAnimator = PhotoViewer.this;
          j = -AndroidUtilities.dp(44.0F);
          if (Build.VERSION.SDK_INT < 21) {
            break label557;
          }
        }
        label551:
        label554:
        label557:
        for (int i = AndroidUtilities.statusBarHeight / 2;; i = 0)
        {
          PhotoViewer.access$14902(paramAnonymousAnimator, i + j);
          PhotoViewer.access$15502(PhotoViewer.this, System.currentTimeMillis());
          PhotoViewer.access$15602(PhotoViewer.this, true);
          PhotoViewer.access$14502(PhotoViewer.this, new AnimatorSet());
          PhotoViewer.this.imageMoveAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(PhotoViewer.this, "animationValue", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.photoPaintView.getColorPicker(), "translationY", new float[] { AndroidUtilities.dp(126.0F), 0.0F }), ObjectAnimator.ofFloat(PhotoViewer.this.photoPaintView.getToolsView(), "translationY", new float[] { AndroidUtilities.dp(126.0F), 0.0F }) });
          PhotoViewer.this.imageMoveAnimation.setDuration(200L);
          PhotoViewer.this.imageMoveAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymous2Animator)
            {
              PhotoViewer.this.photoPaintView.init();
              PhotoViewer.access$14502(PhotoViewer.this, null);
              PhotoViewer.access$11002(PhotoViewer.this, PhotoViewer.58.this.val$mode);
              PhotoViewer.access$14702(PhotoViewer.this, 1.0F);
              PhotoViewer.access$14802(PhotoViewer.this, 0.0F);
              PhotoViewer.access$14902(PhotoViewer.this, 0.0F);
              PhotoViewer.access$6802(PhotoViewer.this, 1.0F);
              PhotoViewer.this.updateMinMax(PhotoViewer.this.scale);
              PhotoViewer.this.containerView.invalidate();
            }
            
            public void onAnimationStart(Animator paramAnonymous2Animator) {}
          });
          PhotoViewer.this.imageMoveAnimation.start();
          return;
          break;
          break label309;
        }
      }
    });
    this.changeModeAnimation.start();
  }
  
  private void switchToPip()
  {
    if ((this.videoPlayer == null) || (!this.textureUploaded) || (!checkInlinePermissions()) || (this.changingTextureView) || (this.switchingInlineMode) || (this.isInline)) {
      return;
    }
    if (PipInstance != null) {
      PipInstance.destroyPhotoViewer();
    }
    PipInstance = Instance;
    Instance = null;
    this.switchingInlineMode = true;
    this.isVisible = false;
    if (this.currentPlaceObject != null) {
      this.currentPlaceObject.imageReceiver.setVisible(true, true);
    }
    if (Build.VERSION.SDK_INT >= 21)
    {
      this.pipAnimationInProgress = true;
      org.telegram.ui.Components.Rect localRect = PipVideoView.getPipRect(this.aspectRatioFrameLayout.getAspectRatio());
      float f = localRect.width / this.videoTextureView.getWidth();
      localRect.y += AndroidUtilities.statusBarHeight;
      AnimatorSet localAnimatorSet = new AnimatorSet();
      localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.textureImageView, "scaleX", new float[] { f }), ObjectAnimator.ofFloat(this.textureImageView, "scaleY", new float[] { f }), ObjectAnimator.ofFloat(this.textureImageView, "translationX", new float[] { localRect.x }), ObjectAnimator.ofFloat(this.textureImageView, "translationY", new float[] { localRect.y }), ObjectAnimator.ofFloat(this.videoTextureView, "scaleX", new float[] { f }), ObjectAnimator.ofFloat(this.videoTextureView, "scaleY", new float[] { f }), ObjectAnimator.ofFloat(this.videoTextureView, "translationX", new float[] { localRect.x - this.aspectRatioFrameLayout.getX() }), ObjectAnimator.ofFloat(this.videoTextureView, "translationY", new float[] { localRect.y - this.aspectRatioFrameLayout.getY() }), ObjectAnimator.ofInt(this.backgroundDrawable, "alpha", new int[] { 0 }), ObjectAnimator.ofFloat(this.actionBar, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.bottomLayout, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.captionTextView, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.groupedPhotosListView, "alpha", new float[] { 0.0F }) });
      localAnimatorSet.setInterpolator(new DecelerateInterpolator());
      localAnimatorSet.setDuration(250L);
      localAnimatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          PhotoViewer.access$12602(PhotoViewer.this, false);
          PhotoViewer.this.switchToInlineRunnable.run();
        }
      });
      localAnimatorSet.start();
      return;
    }
    this.switchToInlineRunnable.run();
    dismissInternal();
  }
  
  private void toggleActionBar(final boolean paramBoolean1, boolean paramBoolean2)
  {
    float f1 = 1.0F;
    if (this.actionBarAnimator != null) {
      this.actionBarAnimator.cancel();
    }
    if (paramBoolean1)
    {
      this.actionBar.setVisibility(0);
      if (this.bottomLayout.getTag() != null) {
        this.bottomLayout.setVisibility(0);
      }
      if (this.captionTextView.getTag() != null) {
        this.captionTextView.setVisibility(0);
      }
    }
    this.isActionBarVisible = paramBoolean1;
    Object localObject1;
    if (Build.VERSION.SDK_INT >= 21)
    {
      if (!paramBoolean1) {
        break label369;
      }
      if ((this.windowLayoutParams.flags & 0x400) != 0)
      {
        localObject1 = this.windowLayoutParams;
        ((WindowManager.LayoutParams)localObject1).flags &= 0xFBFF;
        if (this.windowView == null) {}
      }
    }
    try
    {
      ((WindowManager)this.parentActivity.getSystemService("window")).updateViewLayout(this.windowView, this.windowLayoutParams);
      float f2;
      if (paramBoolean2)
      {
        localObject1 = new ArrayList();
        Object localObject3 = this.actionBar;
        if (paramBoolean1)
        {
          f2 = 1.0F;
          label174:
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(localObject3, "alpha", new float[] { f2 }));
          if (this.bottomLayout != null)
          {
            localObject3 = this.bottomLayout;
            if (!paramBoolean1) {
              break label450;
            }
            f2 = 1.0F;
            label216:
            ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(localObject3, "alpha", new float[] { f2 }));
          }
          localObject3 = this.groupedPhotosListView;
          if (!paramBoolean1) {
            break label456;
          }
          f2 = 1.0F;
          label251:
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(localObject3, "alpha", new float[] { f2 }));
          if (this.captionTextView.getTag() != null)
          {
            localObject3 = this.captionTextView;
            if (!paramBoolean1) {
              break label462;
            }
          }
        }
        for (;;)
        {
          ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(localObject3, "alpha", new float[] { f1 }));
          this.actionBarAnimator = new AnimatorSet();
          this.actionBarAnimator.playTogether((Collection)localObject1);
          this.actionBarAnimator.setDuration(200L);
          this.actionBarAnimator.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationCancel(Animator paramAnonymousAnimator)
            {
              if (paramAnonymousAnimator.equals(PhotoViewer.this.actionBarAnimator)) {
                PhotoViewer.access$15802(PhotoViewer.this, null);
              }
            }
            
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              if (paramAnonymousAnimator.equals(PhotoViewer.this.actionBarAnimator))
              {
                if (!paramBoolean1)
                {
                  PhotoViewer.this.actionBar.setVisibility(4);
                  if (PhotoViewer.this.bottomLayout.getTag() != null) {
                    PhotoViewer.this.bottomLayout.setVisibility(4);
                  }
                  if (PhotoViewer.this.captionTextView.getTag() != null) {
                    PhotoViewer.this.captionTextView.setVisibility(4);
                  }
                }
                PhotoViewer.access$15802(PhotoViewer.this, null);
              }
            }
          });
          this.actionBarAnimator.start();
          return;
          label369:
          if ((this.windowLayoutParams.flags & 0x400) != 0) {
            break;
          }
          localObject1 = this.windowLayoutParams;
          ((WindowManager.LayoutParams)localObject1).flags |= 0x400;
          if (this.windowView == null) {
            break;
          }
          try
          {
            ((WindowManager)this.parentActivity.getSystemService("window")).updateViewLayout(this.windowView, this.windowLayoutParams);
          }
          catch (Exception localException1) {}
          break;
          f2 = 0.0F;
          break label174;
          label450:
          f2 = 0.0F;
          break label216;
          label456:
          f2 = 0.0F;
          break label251;
          label462:
          f1 = 0.0F;
        }
      }
      Object localObject2 = this.actionBar;
      if (paramBoolean1)
      {
        f2 = 1.0F;
        ((ActionBar)localObject2).setAlpha(f2);
        localObject2 = this.bottomLayout;
        if (!paramBoolean1) {
          break label550;
        }
        f2 = 1.0F;
        label500:
        ((FrameLayout)localObject2).setAlpha(f2);
        localObject2 = this.groupedPhotosListView;
        if (!paramBoolean1) {
          break label556;
        }
        f2 = 1.0F;
        label520:
        ((GroupedPhotosListView)localObject2).setAlpha(f2);
        localObject2 = this.captionTextView;
        if (!paramBoolean1) {
          break label562;
        }
      }
      for (;;)
      {
        ((TextView)localObject2).setAlpha(f1);
        return;
        f2 = 0.0F;
        break;
        label550:
        f2 = 0.0F;
        break label500;
        label556:
        f2 = 0.0F;
        break label520;
        label562:
        f1 = 0.0F;
      }
    }
    catch (Exception localException2)
    {
      for (;;) {}
    }
  }
  
  private void toggleCheckImageView(boolean paramBoolean)
  {
    float f2 = 1.0F;
    AnimatorSet localAnimatorSet = new AnimatorSet();
    ArrayList localArrayList = new ArrayList();
    Object localObject = this.pickerView;
    if (paramBoolean)
    {
      f1 = 1.0F;
      localArrayList.add(ObjectAnimator.ofFloat(localObject, "alpha", new float[] { f1 }));
      localObject = this.pickerViewSendButton;
      if (!paramBoolean) {
        break label226;
      }
      f1 = 1.0F;
      label65:
      localArrayList.add(ObjectAnimator.ofFloat(localObject, "alpha", new float[] { f1 }));
      if (this.needCaptionLayout)
      {
        localObject = this.captionTextView;
        if (!paramBoolean) {
          break label231;
        }
        f1 = 1.0F;
        label105:
        localArrayList.add(ObjectAnimator.ofFloat(localObject, "alpha", new float[] { f1 }));
      }
      if (this.sendPhotoType == 0)
      {
        localObject = this.checkImageView;
        if (!paramBoolean) {
          break label236;
        }
        f1 = 1.0F;
        label145:
        localArrayList.add(ObjectAnimator.ofFloat(localObject, "alpha", new float[] { f1 }));
        localObject = this.photosCounterView;
        if (!paramBoolean) {
          break label241;
        }
      }
    }
    label226:
    label231:
    label236:
    label241:
    for (float f1 = f2;; f1 = 0.0F)
    {
      localArrayList.add(ObjectAnimator.ofFloat(localObject, "alpha", new float[] { f1 }));
      localAnimatorSet.playTogether(localArrayList);
      localAnimatorSet.setDuration(200L);
      localAnimatorSet.start();
      return;
      f1 = 0.0F;
      break;
      f1 = 0.0F;
      break label65;
      f1 = 0.0F;
      break label105;
      f1 = 0.0F;
      break label145;
    }
  }
  
  private void toggleMiniProgress(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean2)
    {
      toggleMiniProgressInternal(paramBoolean1);
      if (paramBoolean1)
      {
        if (this.miniProgressAnimator != null)
        {
          this.miniProgressAnimator.cancel();
          this.miniProgressAnimator = null;
        }
        AndroidUtilities.cancelRunOnUIThread(this.miniProgressShowRunnable);
        if (this.firstAnimationDelay)
        {
          this.firstAnimationDelay = false;
          toggleMiniProgressInternal(true);
        }
      }
      do
      {
        return;
        AndroidUtilities.runOnUIThread(this.miniProgressShowRunnable, 500L);
        return;
        AndroidUtilities.cancelRunOnUIThread(this.miniProgressShowRunnable);
      } while (this.miniProgressAnimator == null);
      this.miniProgressAnimator.cancel();
      toggleMiniProgressInternal(false);
      return;
    }
    if (this.miniProgressAnimator != null)
    {
      this.miniProgressAnimator.cancel();
      this.miniProgressAnimator = null;
    }
    RadialProgressView localRadialProgressView = this.miniProgressView;
    float f;
    if (paramBoolean1)
    {
      f = 1.0F;
      localRadialProgressView.setAlpha(f);
      localRadialProgressView = this.miniProgressView;
      if (!paramBoolean1) {
        break label158;
      }
    }
    label158:
    for (int i = 0;; i = 4)
    {
      localRadialProgressView.setVisibility(i);
      return;
      f = 0.0F;
      break;
    }
  }
  
  private void toggleMiniProgressInternal(final boolean paramBoolean)
  {
    if (paramBoolean) {
      this.miniProgressView.setVisibility(0);
    }
    this.miniProgressAnimator = new AnimatorSet();
    AnimatorSet localAnimatorSet = this.miniProgressAnimator;
    RadialProgressView localRadialProgressView = this.miniProgressView;
    if (paramBoolean) {}
    for (float f = 1.0F;; f = 0.0F)
    {
      localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(localRadialProgressView, "alpha", new float[] { f }) });
      this.miniProgressAnimator.setDuration(200L);
      this.miniProgressAnimator.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PhotoViewer.this.miniProgressAnimator)) {
            PhotoViewer.access$15702(PhotoViewer.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PhotoViewer.this.miniProgressAnimator))
          {
            if (!paramBoolean) {
              PhotoViewer.this.miniProgressView.setVisibility(4);
            }
            PhotoViewer.access$15702(PhotoViewer.this, null);
          }
        }
      });
      this.miniProgressAnimator.start();
      return;
    }
  }
  
  private void togglePhotosListView(boolean paramBoolean1, boolean paramBoolean2)
  {
    float f1 = 1.0F;
    if (paramBoolean1 == this.isPhotosListViewVisible) {
      return;
    }
    if (paramBoolean1) {
      this.selectedPhotosListView.setVisibility(0);
    }
    this.isPhotosListViewVisible = paramBoolean1;
    this.selectedPhotosListView.setEnabled(paramBoolean1);
    float f2;
    if (paramBoolean2)
    {
      localObject1 = new ArrayList();
      Object localObject2 = this.selectedPhotosListView;
      if (paramBoolean1)
      {
        f2 = 1.0F;
        ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(localObject2, "alpha", new float[] { f2 }));
        localObject2 = this.selectedPhotosListView;
        if (!paramBoolean1) {
          break label214;
        }
        f2 = 0.0F;
        label97:
        ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(localObject2, "translationY", new float[] { f2 }));
        localObject2 = this.photosCounterView;
        if (!paramBoolean1) {
          break label227;
        }
      }
      for (;;)
      {
        ((ArrayList)localObject1).add(ObjectAnimator.ofFloat(localObject2, "rotationX", new float[] { f1 }));
        this.currentListViewAnimation = new AnimatorSet();
        this.currentListViewAnimation.playTogether((Collection)localObject1);
        if (!paramBoolean1) {
          this.currentListViewAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              if ((PhotoViewer.this.currentListViewAnimation != null) && (PhotoViewer.this.currentListViewAnimation.equals(paramAnonymousAnimator)))
              {
                PhotoViewer.this.selectedPhotosListView.setVisibility(8);
                PhotoViewer.access$15902(PhotoViewer.this, null);
              }
            }
          });
        }
        this.currentListViewAnimation.setDuration(200L);
        this.currentListViewAnimation.start();
        return;
        f2 = 0.0F;
        break;
        label214:
        f2 = -AndroidUtilities.dp(10.0F);
        break label97;
        label227:
        f1 = 0.0F;
      }
    }
    Object localObject1 = this.selectedPhotosListView;
    if (paramBoolean1)
    {
      f2 = 1.0F;
      label245:
      ((RecyclerListView)localObject1).setAlpha(f2);
      localObject1 = this.selectedPhotosListView;
      if (!paramBoolean1) {
        break label308;
      }
      f2 = 0.0F;
      label265:
      ((RecyclerListView)localObject1).setTranslationY(f2);
      localObject1 = this.photosCounterView;
      if (!paramBoolean1) {
        break label321;
      }
    }
    for (;;)
    {
      ((CounterView)localObject1).setRotationX(f1);
      if (paramBoolean1) {
        break;
      }
      this.selectedPhotosListView.setVisibility(8);
      return;
      f2 = 0.0F;
      break label245;
      label308:
      f2 = -AndroidUtilities.dp(10.0F);
      break label265;
      label321:
      f1 = 0.0F;
    }
  }
  
  private void updateCaptionTextForCurrentPhoto(Object paramObject)
  {
    Object localObject2 = null;
    Object localObject1;
    if ((paramObject instanceof MediaController.PhotoEntry)) {
      localObject1 = ((MediaController.PhotoEntry)paramObject).caption;
    }
    while ((localObject1 == null) || (((CharSequence)localObject1).length() == 0))
    {
      this.captionEditText.setFieldText("");
      return;
      localObject1 = localObject2;
      if (!(paramObject instanceof TLRPC.BotInlineResult))
      {
        localObject1 = localObject2;
        if ((paramObject instanceof MediaController.SearchImage)) {
          localObject1 = ((MediaController.SearchImage)paramObject).caption;
        }
      }
    }
    this.captionEditText.setFieldText((CharSequence)localObject1);
  }
  
  private void updateMinMax(float paramFloat)
  {
    int i = (int)(this.centerImage.getImageWidth() * paramFloat - getContainerViewWidth()) / 2;
    int j = (int)(this.centerImage.getImageHeight() * paramFloat - getContainerViewHeight()) / 2;
    if (i > 0)
    {
      this.minX = (-i);
      this.maxX = i;
      if (j <= 0) {
        break label160;
      }
      this.minY = (-j);
      this.maxY = j;
    }
    for (;;)
    {
      if (this.currentEditMode == 1)
      {
        this.maxX += this.photoCropView.getLimitX();
        this.maxY += this.photoCropView.getLimitY();
        this.minX -= this.photoCropView.getLimitWidth();
        this.minY -= this.photoCropView.getLimitHeight();
      }
      return;
      this.maxX = 0.0F;
      this.minX = 0.0F;
      break;
      label160:
      this.maxY = 0.0F;
      this.minY = 0.0F;
    }
  }
  
  private void updateSelectedCount()
  {
    if (this.placeProvider == null) {}
    int i;
    do
    {
      return;
      i = this.placeProvider.getSelectedCount();
      this.photosCounterView.setCount(i);
    } while (i != 0);
    togglePhotosListView(false, true);
  }
  
  private void updateVideoInfo()
  {
    if (this.actionBar == null) {
      return;
    }
    if (this.compressionsCount == 0)
    {
      this.actionBar.setSubtitle(null);
      return;
    }
    int i;
    label117:
    int j;
    label141:
    label181:
    label200:
    ActionBar localActionBar;
    if (this.selectedCompression == 0)
    {
      this.compressItem.setImageResource(2131165688);
      this.estimatedDuration = (Math.ceil((this.videoTimelineView.getRightProgress() - this.videoTimelineView.getLeftProgress()) * this.videoDuration));
      if ((this.compressItem.getTag() != null) && (this.selectedCompression != this.compressionsCount - 1)) {
        break label434;
      }
      if ((this.rotationValue != 90) && (this.rotationValue != 270)) {
        break label418;
      }
      i = this.originalHeight;
      if ((this.rotationValue != 90) && (this.rotationValue != 270)) {
        break label426;
      }
      j = this.originalWidth;
      this.estimatedSize = ((int)((float)this.originalSize * ((float)this.estimatedDuration / this.videoDuration)));
      if (this.videoTimelineView.getLeftProgress() != 0.0F) {
        break label547;
      }
      this.startTime = -1L;
      if (this.videoTimelineView.getRightProgress() != 1.0F) {
        break label571;
      }
      this.endTime = -1L;
      str = String.format("%dx%d", new Object[] { Integer.valueOf(i), Integer.valueOf(j) });
      i = (int)(this.estimatedDuration / 1000L / 60L);
      this.currentSubtitle = String.format("%s, %s", new Object[] { str, String.format("%d:%02d, ~%s", new Object[] { Integer.valueOf(i), Integer.valueOf((int)Math.ceil(this.estimatedDuration / 1000L) - i * 60), AndroidUtilities.formatFileSize(this.estimatedSize) }) });
      localActionBar = this.actionBar;
      if (!this.muteVideo) {
        break label595;
      }
    }
    label418:
    label426:
    label434:
    label458:
    label539:
    label547:
    label571:
    label595:
    for (String str = null;; str = this.currentSubtitle)
    {
      localActionBar.setSubtitle(str);
      return;
      if (this.selectedCompression == 1)
      {
        this.compressItem.setImageResource(2131165689);
        break;
      }
      if (this.selectedCompression == 2)
      {
        this.compressItem.setImageResource(2131165690);
        break;
      }
      if (this.selectedCompression == 3)
      {
        this.compressItem.setImageResource(2131165691);
        break;
      }
      if (this.selectedCompression != 4) {
        break;
      }
      this.compressItem.setImageResource(2131165687);
      break;
      i = this.originalWidth;
      break label117;
      j = this.originalHeight;
      break label141;
      if ((this.rotationValue == 90) || (this.rotationValue == 270))
      {
        i = this.resultHeight;
        if ((this.rotationValue != 90) && (this.rotationValue != 270)) {
          break label539;
        }
      }
      for (j = this.resultWidth;; j = this.resultHeight)
      {
        this.estimatedSize = ((int)((float)(this.audioFramesSize + this.videoFramesSize) * ((float)this.estimatedDuration / this.videoDuration)));
        this.estimatedSize += this.estimatedSize / 32768 * 16;
        break;
        i = this.resultWidth;
        break label458;
      }
      this.startTime = ((this.videoTimelineView.getLeftProgress() * this.videoDuration) * 1000L);
      break label181;
      this.endTime = ((this.videoTimelineView.getRightProgress() * this.videoDuration) * 1000L);
      break label200;
    }
  }
  
  private void updateVideoPlayerTime()
  {
    String str;
    if (this.videoPlayer == null) {
      str = String.format("%02d:%02d / %02d:%02d", new Object[] { Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0) });
    }
    for (;;)
    {
      this.videoPlayerTime.setText(str);
      return;
      long l2 = this.videoPlayer.getCurrentPosition();
      long l1 = l2;
      if (l2 < 0L) {
        l1 = 0L;
      }
      l2 = this.videoPlayer.getDuration();
      long l3 = l2;
      if (l2 < 0L) {
        l3 = 0L;
      }
      if ((l3 != -9223372036854775807L) && (l1 != -9223372036854775807L))
      {
        l2 = l1;
        long l4 = l3;
        if (!this.inPreview)
        {
          l2 = l1;
          l4 = l3;
          if (this.videoTimelineView.getVisibility() == 0)
          {
            l3 = ((float)l3 * (this.videoTimelineView.getRightProgress() - this.videoTimelineView.getLeftProgress()));
            l1 = ((float)l1 - this.videoTimelineView.getLeftProgress() * (float)l3);
            l2 = l1;
            l4 = l3;
            if (l1 > l3)
            {
              l2 = l3;
              l4 = l3;
            }
          }
        }
        l1 = l2 / 1000L;
        l2 = l4 / 1000L;
        str = String.format("%02d:%02d / %02d:%02d", new Object[] { Long.valueOf(l1 / 60L), Long.valueOf(l1 % 60L), Long.valueOf(l2 / 60L), Long.valueOf(l2 % 60L) });
      }
      else
      {
        str = String.format("%02d:%02d / %02d:%02d", new Object[] { Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0) });
      }
    }
  }
  
  private void updateWidthHeightBitrateForCompression()
  {
    if (this.compressionsCount <= 0) {}
    do
    {
      return;
      if (this.selectedCompression >= this.compressionsCount) {
        this.selectedCompression = (this.compressionsCount - 1);
      }
    } while (this.selectedCompression == this.compressionsCount - 1);
    int i;
    float f;
    switch (this.selectedCompression)
    {
    default: 
      i = 2500000;
      f = 1280.0F;
      label80:
      if (this.originalWidth <= this.originalHeight) {
        break;
      }
    }
    for (f /= this.originalWidth;; f /= this.originalHeight)
    {
      this.resultWidth = (Math.round(this.originalWidth * f / 2.0F) * 2);
      this.resultHeight = (Math.round(this.originalHeight * f / 2.0F) * 2);
      if (this.bitrate == 0) {
        break;
      }
      this.bitrate = Math.min(i, (int)(this.originalBitrate / f));
      this.videoFramesSize = ((this.bitrate / 8 * this.videoDuration / 1000.0F));
      return;
      f = 426.0F;
      i = 400000;
      break label80;
      f = 640.0F;
      i = 900000;
      break label80;
      f = 854.0F;
      i = 1100000;
      break label80;
    }
  }
  
  public void closePhoto(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((!paramBoolean2) && (this.currentEditMode != 0)) {
      if ((this.currentEditMode == 3) && (this.photoPaintView != null)) {
        this.photoPaintView.maybeShowDismissalAlert(this, this.parentActivity, new Runnable()
        {
          public void run()
          {
            PhotoViewer.this.switchToEditMode(0);
          }
        });
      }
    }
    final PlaceProviderObject localPlaceProviderObject;
    for (;;)
    {
      return;
      if (this.currentEditMode == 1) {
        this.photoCropView.cancelAnimationRunnable();
      }
      switchToEditMode(0);
      return;
      if ((this.qualityChooseView != null) && (this.qualityChooseView.getTag() != null))
      {
        this.qualityPicker.cancelButton.callOnClick();
        return;
      }
      try
      {
        if (this.visibleDialog != null)
        {
          this.visibleDialog.dismiss();
          this.visibleDialog = null;
        }
        if ((Build.VERSION.SDK_INT >= 21) && (this.actionBar != null) && ((this.windowLayoutParams.flags & 0x400) != 0))
        {
          WindowManager.LayoutParams localLayoutParams = this.windowLayoutParams;
          localLayoutParams.flags &= 0xFBFF;
          ((WindowManager)this.parentActivity.getSystemService("window")).updateViewLayout(this.windowView, this.windowLayoutParams);
        }
        if (this.currentEditMode != 0)
        {
          if (this.currentEditMode == 2)
          {
            this.photoFilterView.shutdown();
            this.containerView.removeView(this.photoFilterView);
            this.photoFilterView = null;
            this.currentEditMode = 0;
          }
        }
        else
        {
          if ((this.parentActivity == null) || ((!this.isInline) && (!this.isVisible)) || (checkAnimation()) || (this.placeProvider == null) || ((this.captionEditText.hideActionMode()) && (!paramBoolean2))) {
            continue;
          }
          releasePlayer();
          this.captionEditText.onDestroy();
          this.parentChatActivity = null;
          removeObservers();
          this.isActionBarVisible = false;
          if (this.velocityTracker != null)
          {
            this.velocityTracker.recycle();
            this.velocityTracker = null;
          }
          localPlaceProviderObject = this.placeProvider.getPlaceForPhoto(this.currentMessageObject, this.currentFileLocation, this.currentIndex);
          if (!this.isInline) {
            break label429;
          }
          this.isInline = false;
          this.animationInProgress = 0;
          onPhotoClosed(localPlaceProviderObject);
          this.containerView.setScaleX(1.0F);
          this.containerView.setScaleY(1.0F);
          return;
        }
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
          continue;
          if (this.currentEditMode == 1)
          {
            this.editorDoneLayout.setVisibility(8);
            this.photoCropView.setVisibility(8);
          }
        }
      }
    }
    label429:
    AnimatorSet localAnimatorSet;
    Object localObject2;
    Object localObject1;
    int j;
    int i;
    label556:
    label616:
    float f1;
    if (paramBoolean1)
    {
      this.animationInProgress = 1;
      this.animatingImageView.setVisibility(0);
      this.containerView.invalidate();
      localAnimatorSet = new AnimatorSet();
      localObject2 = this.animatingImageView.getLayoutParams();
      localObject1 = null;
      j = this.centerImage.getOrientation();
      int k = 0;
      i = k;
      if (localPlaceProviderObject != null)
      {
        i = k;
        if (localPlaceProviderObject.imageReceiver != null) {
          i = localPlaceProviderObject.imageReceiver.getAnimatedOrientation();
        }
      }
      if (i != 0) {
        j = i;
      }
      this.animatingImageView.setOrientation(j);
      if (localPlaceProviderObject != null)
      {
        localObject1 = this.animatingImageView;
        if (localPlaceProviderObject.radius != 0)
        {
          paramBoolean1 = true;
          ((ClippingImageView)localObject1).setNeedRadius(paramBoolean1);
          localObject1 = localPlaceProviderObject.imageReceiver.getDrawRegion();
          ((ViewGroup.LayoutParams)localObject2).width = (((android.graphics.Rect)localObject1).right - ((android.graphics.Rect)localObject1).left);
          ((ViewGroup.LayoutParams)localObject2).height = (((android.graphics.Rect)localObject1).bottom - ((android.graphics.Rect)localObject1).top);
          this.animatingImageView.setImageBitmap(localPlaceProviderObject.thumb);
          this.animatingImageView.setLayoutParams((ViewGroup.LayoutParams)localObject2);
          f1 = AndroidUtilities.displaySize.x / ((ViewGroup.LayoutParams)localObject2).width;
          j = AndroidUtilities.displaySize.y;
          if (Build.VERSION.SDK_INT < 21) {
            break label1565;
          }
          i = AndroidUtilities.statusBarHeight;
          label661:
          float f2 = (i + j) / ((ViewGroup.LayoutParams)localObject2).height;
          if (f1 <= f2) {
            break label1571;
          }
          f1 = f2;
          label686:
          float f4 = ((ViewGroup.LayoutParams)localObject2).width;
          float f5 = this.scale;
          f2 = ((ViewGroup.LayoutParams)localObject2).height;
          float f3 = this.scale;
          f4 = (AndroidUtilities.displaySize.x - f4 * f5 * f1) / 2.0F;
          j = AndroidUtilities.displaySize.y;
          if (Build.VERSION.SDK_INT < 21) {
            break label1574;
          }
          i = AndroidUtilities.statusBarHeight;
          label754:
          f2 = (i + j - f2 * f3 * f1) / 2.0F;
          this.animatingImageView.setTranslationX(this.translationX + f4);
          this.animatingImageView.setTranslationY(this.translationY + f2);
          this.animatingImageView.setScaleX(this.scale * f1);
          this.animatingImageView.setScaleY(this.scale * f1);
          if (localPlaceProviderObject == null) {
            break label1596;
          }
          localPlaceProviderObject.imageReceiver.setVisible(false, true);
          int m = Math.abs(((android.graphics.Rect)localObject1).left - localPlaceProviderObject.imageReceiver.getImageX());
          int n = Math.abs(((android.graphics.Rect)localObject1).top - localPlaceProviderObject.imageReceiver.getImageY());
          localObject2 = new int[2];
          localPlaceProviderObject.parentView.getLocationInWindow((int[])localObject2);
          j = localObject2[1];
          if (Build.VERSION.SDK_INT < 21) {
            break label1580;
          }
          i = 0;
          label911:
          j = j - i - (localPlaceProviderObject.viewY + ((android.graphics.Rect)localObject1).top) + localPlaceProviderObject.clipTopAddition;
          i = j;
          if (j < 0) {
            i = 0;
          }
          k = localPlaceProviderObject.viewY;
          int i1 = ((android.graphics.Rect)localObject1).top;
          int i2 = ((android.graphics.Rect)localObject1).bottom;
          int i3 = ((android.graphics.Rect)localObject1).top;
          int i4 = localObject2[1];
          int i5 = localPlaceProviderObject.parentView.getHeight();
          if (Build.VERSION.SDK_INT < 21) {
            break label1588;
          }
          j = 0;
          label1003:
          k = i2 - i3 + (k + i1) - (i5 + i4 - j) + localPlaceProviderObject.clipBottomAddition;
          j = k;
          if (k < 0) {
            j = 0;
          }
          i = Math.max(i, n);
          j = Math.max(j, n);
          this.animationValues[0][0] = this.animatingImageView.getScaleX();
          this.animationValues[0][1] = this.animatingImageView.getScaleY();
          this.animationValues[0][2] = this.animatingImageView.getTranslationX();
          this.animationValues[0][3] = this.animatingImageView.getTranslationY();
          this.animationValues[0][4] = 0;
          this.animationValues[0][5] = 0;
          this.animationValues[0][6] = 0;
          this.animationValues[0][7] = 0;
          this.animationValues[1][0] = localPlaceProviderObject.scale;
          this.animationValues[1][1] = localPlaceProviderObject.scale;
          this.animationValues[1][2] = (localPlaceProviderObject.viewX + ((android.graphics.Rect)localObject1).left * localPlaceProviderObject.scale - getLeftInset());
          this.animationValues[1][3] = (localPlaceProviderObject.viewY + ((android.graphics.Rect)localObject1).top * localPlaceProviderObject.scale);
          this.animationValues[1][4] = (m * localPlaceProviderObject.scale);
          this.animationValues[1][5] = (i * localPlaceProviderObject.scale);
          this.animationValues[1][6] = (j * localPlaceProviderObject.scale);
          this.animationValues[1][7] = localPlaceProviderObject.radius;
          localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.animatingImageView, "animationProgress", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofInt(this.backgroundDrawable, "alpha", new int[] { 0 }), ObjectAnimator.ofFloat(this.containerView, "alpha", new float[] { 0.0F }) });
          this.animationEndRunnable = new Runnable()
          {
            public void run()
            {
              if (Build.VERSION.SDK_INT >= 18) {
                PhotoViewer.this.containerView.setLayerType(0, null);
              }
              PhotoViewer.access$7502(PhotoViewer.this, 0);
              PhotoViewer.this.onPhotoClosed(localPlaceProviderObject);
            }
          };
          localAnimatorSet.setDuration(200L);
          localAnimatorSet.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (PhotoViewer.this.animationEndRunnable != null)
                  {
                    PhotoViewer.this.animationEndRunnable.run();
                    PhotoViewer.access$16602(PhotoViewer.this, null);
                  }
                }
              });
            }
          });
          this.transitionAnimationStartTime = System.currentTimeMillis();
          if (Build.VERSION.SDK_INT >= 18) {
            this.containerView.setLayerType(2, null);
          }
          localAnimatorSet.start();
        }
      }
    }
    for (;;)
    {
      if (this.currentAnimation != null)
      {
        this.currentAnimation.setSecondParentView(null);
        this.currentAnimation = null;
        this.centerImage.setImageBitmap((Drawable)null);
      }
      if ((this.placeProvider == null) || (this.placeProvider.canScrollAway())) {
        break;
      }
      this.placeProvider.cancelButtonPressed();
      return;
      paramBoolean1 = false;
      break label556;
      this.animatingImageView.setNeedRadius(false);
      ((ViewGroup.LayoutParams)localObject2).width = this.centerImage.getImageWidth();
      ((ViewGroup.LayoutParams)localObject2).height = this.centerImage.getImageHeight();
      this.animatingImageView.setImageBitmap(this.centerImage.getBitmapSafe());
      break label616;
      label1565:
      i = 0;
      break label661;
      label1571:
      break label686;
      label1574:
      i = 0;
      break label754;
      label1580:
      i = AndroidUtilities.statusBarHeight;
      break label911;
      label1588:
      j = AndroidUtilities.statusBarHeight;
      break label1003;
      label1596:
      j = AndroidUtilities.displaySize.y;
      label1617:
      ClippingImageView localClippingImageView;
      if (Build.VERSION.SDK_INT >= 21)
      {
        i = AndroidUtilities.statusBarHeight;
        i = j + i;
        localObject1 = ObjectAnimator.ofInt(this.backgroundDrawable, "alpha", new int[] { 0 });
        localObject2 = ObjectAnimator.ofFloat(this.animatingImageView, "alpha", new float[] { 0.0F });
        localClippingImageView = this.animatingImageView;
        if (this.translationY < 0.0F) {
          break label1747;
        }
      }
      label1747:
      for (f1 = i;; f1 = -i)
      {
        localAnimatorSet.playTogether(new Animator[] { localObject1, localObject2, ObjectAnimator.ofFloat(localClippingImageView, "translationY", new float[] { f1 }), ObjectAnimator.ofFloat(this.containerView, "alpha", new float[] { 0.0F }) });
        break;
        i = 0;
        break label1617;
      }
      localObject1 = new AnimatorSet();
      ((AnimatorSet)localObject1).playTogether(new Animator[] { ObjectAnimator.ofFloat(this.containerView, "scaleX", new float[] { 0.9F }), ObjectAnimator.ofFloat(this.containerView, "scaleY", new float[] { 0.9F }), ObjectAnimator.ofInt(this.backgroundDrawable, "alpha", new int[] { 0 }), ObjectAnimator.ofFloat(this.containerView, "alpha", new float[] { 0.0F }) });
      this.animationInProgress = 2;
      this.animationEndRunnable = new Runnable()
      {
        public void run()
        {
          if (PhotoViewer.this.containerView == null) {
            return;
          }
          if (Build.VERSION.SDK_INT >= 18) {
            PhotoViewer.this.containerView.setLayerType(0, null);
          }
          PhotoViewer.access$7502(PhotoViewer.this, 0);
          PhotoViewer.this.onPhotoClosed(localPlaceProviderObject);
          PhotoViewer.this.containerView.setScaleX(1.0F);
          PhotoViewer.this.containerView.setScaleY(1.0F);
        }
      };
      ((AnimatorSet)localObject1).setDuration(200L);
      ((AnimatorSet)localObject1).addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (PhotoViewer.this.animationEndRunnable != null)
          {
            PhotoViewer.this.animationEndRunnable.run();
            PhotoViewer.access$16602(PhotoViewer.this, null);
          }
        }
      });
      this.transitionAnimationStartTime = System.currentTimeMillis();
      if (Build.VERSION.SDK_INT >= 18) {
        this.containerView.setLayerType(2, null);
      }
      ((AnimatorSet)localObject1).start();
    }
  }
  
  public void destroyPhotoViewer()
  {
    if ((this.parentActivity == null) || (this.windowView == null)) {
      return;
    }
    if (this.pipVideoView != null)
    {
      this.pipVideoView.close();
      this.pipVideoView = null;
    }
    removeObservers();
    releasePlayer();
    try
    {
      if (this.windowView.getParent() != null) {
        ((WindowManager)this.parentActivity.getSystemService("window")).removeViewImmediate(this.windowView);
      }
      this.windowView = null;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
      Instance = null;
    }
    if (this.currentThumb != null)
    {
      this.currentThumb.release();
      this.currentThumb = null;
    }
    this.animatingImageView.setImageBitmap(null);
    if (this.captionEditText != null) {
      this.captionEditText.onDestroy();
    }
    if (this == PipInstance)
    {
      PipInstance = null;
      return;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.FileDidFailedLoad)
    {
      paramVarArgs = (String)paramVarArgs[0];
      paramInt1 = 0;
      if (paramInt1 < 3)
      {
        if ((this.currentFileNames[paramInt1] == null) || (!this.currentFileNames[paramInt1].equals(paramVarArgs))) {
          break label61;
        }
        this.photoProgressViews[paramInt1].setProgress(1.0F, true);
        checkProgress(paramInt1, true);
      }
    }
    label61:
    label219:
    Object localObject1;
    label238:
    label374:
    label455:
    label506:
    label672:
    label917:
    label963:
    label965:
    label1231:
    label1508:
    label1723:
    label1822:
    label2011:
    label2032:
    label2041:
    label2085:
    label2211:
    label2251:
    label2273:
    label2381:
    do
    {
      do
      {
        do
        {
          boolean bool;
          do
          {
            do
            {
              Object localObject2;
              int j;
              do
              {
                do
                {
                  do
                  {
                    for (;;)
                    {
                      return;
                      paramInt1 += 1;
                      break;
                      if (paramInt1 == NotificationCenter.FileDidLoaded)
                      {
                        paramVarArgs = (String)paramVarArgs[0];
                        paramInt1 = 0;
                        for (;;)
                        {
                          if (paramInt1 >= 3) {
                            break label219;
                          }
                          if ((this.currentFileNames[paramInt1] != null) && (this.currentFileNames[paramInt1].equals(paramVarArgs)))
                          {
                            this.photoProgressViews[paramInt1].setProgress(1.0F, true);
                            checkProgress(paramInt1, true);
                            if ((this.videoPlayer == null) && (paramInt1 == 0) && (((this.currentMessageObject != null) && (this.currentMessageObject.isVideo())) || ((this.currentBotInlineResult != null) && ((this.currentBotInlineResult.type.equals("video")) || (MessageObject.isVideoDocument(this.currentBotInlineResult.document)))))) {
                              onActionClick(false);
                            }
                            if ((paramInt1 != 0) || (this.videoPlayer == null)) {
                              break;
                            }
                            this.currentVideoFinishedLoading = true;
                            return;
                          }
                          paramInt1 += 1;
                        }
                      }
                      else
                      {
                        if (paramInt1 == NotificationCenter.FileLoadProgressChanged)
                        {
                          localObject1 = (String)paramVarArgs[0];
                          paramInt1 = 0;
                          float f;
                          if (paramInt1 < 3) {
                            if ((this.currentFileNames[paramInt1] != null) && (this.currentFileNames[paramInt1].equals(localObject1)))
                            {
                              localObject2 = (Float)paramVarArgs[1];
                              this.photoProgressViews[paramInt1].setProgress(((Float)localObject2).floatValue(), true);
                              if ((paramInt1 == 0) && (this.videoPlayer != null) && (this.videoPlayerSeekbar != null))
                              {
                                if (!this.currentVideoFinishedLoading) {
                                  break label374;
                                }
                                f = 1.0F;
                              }
                            }
                          }
                          for (;;)
                          {
                            if (f != -1.0F)
                            {
                              this.videoPlayerSeekbar.setBufferedProgress(f);
                              if (this.pipVideoView != null) {
                                this.pipVideoView.setBufferedProgress(f);
                              }
                              this.videoPlayerControlFrameLayout.invalidate();
                            }
                            checkBufferedProgress(((Float)localObject2).floatValue());
                            paramInt1 += 1;
                            break label238;
                            break;
                            l1 = SystemClock.uptimeMillis();
                            if (Math.abs(l1 - this.lastBufferedPositionCheck) >= 500L)
                            {
                              if (this.seekToProgressPending == 0.0F)
                              {
                                long l2 = this.videoPlayer.getDuration();
                                long l3 = this.videoPlayer.getCurrentPosition();
                                if ((l2 >= 0L) && (l2 != -9223372036854775807L) && (l3 >= 0L))
                                {
                                  f = (float)l3 / (float)l2;
                                  if (!this.isStreaming) {
                                    break label506;
                                  }
                                }
                              }
                              for (f = FileLoader.getInstance(this.currentAccount).getBufferedProgressFromPosition(f, this.currentFileNames[0]);; f = 1.0F)
                              {
                                this.lastBufferedPositionCheck = l1;
                                break;
                                f = 0.0F;
                                break label455;
                                f = this.seekToProgressPending;
                                break label455;
                              }
                            }
                            f = -1.0F;
                          }
                        }
                        if (paramInt1 != NotificationCenter.dialogPhotosLoaded) {
                          break label965;
                        }
                        paramInt1 = ((Integer)paramVarArgs[3]).intValue();
                        paramInt2 = ((Integer)paramVarArgs[0]).intValue();
                        if ((this.avatarsDialogId == paramInt2) && (this.classGuid == paramInt1))
                        {
                          bool = ((Boolean)paramVarArgs[2]).booleanValue();
                          paramInt1 = -1;
                          paramVarArgs = (ArrayList)paramVarArgs[4];
                          if (!paramVarArgs.isEmpty())
                          {
                            this.imagesArrLocations.clear();
                            this.imagesArrLocationsSizes.clear();
                            this.avatarsArr.clear();
                            i = 0;
                            if (i < paramVarArgs.size())
                            {
                              localObject1 = (TLRPC.Photo)paramVarArgs.get(i);
                              paramInt2 = paramInt1;
                              if (localObject1 != null)
                              {
                                paramInt2 = paramInt1;
                                if (!(localObject1 instanceof TLRPC.TL_photoEmpty))
                                {
                                  if (((TLRPC.Photo)localObject1).sizes != null) {
                                    break label672;
                                  }
                                  paramInt2 = paramInt1;
                                }
                              }
                              do
                              {
                                i += 1;
                                paramInt1 = paramInt2;
                                break;
                                localObject2 = FileLoader.getClosestPhotoSizeWithSize(((TLRPC.Photo)localObject1).sizes, 640);
                                paramInt2 = paramInt1;
                              } while (localObject2 == null);
                              paramInt2 = paramInt1;
                              if (paramInt1 == -1)
                              {
                                paramInt2 = paramInt1;
                                if (this.currentFileLocation != null) {
                                  j = 0;
                                }
                              }
                              for (;;)
                              {
                                paramInt2 = paramInt1;
                                if (j < ((TLRPC.Photo)localObject1).sizes.size())
                                {
                                  TLRPC.PhotoSize localPhotoSize = (TLRPC.PhotoSize)((TLRPC.Photo)localObject1).sizes.get(j);
                                  if ((localPhotoSize.location.local_id == this.currentFileLocation.local_id) && (localPhotoSize.location.volume_id == this.currentFileLocation.volume_id)) {
                                    paramInt2 = this.imagesArrLocations.size();
                                  }
                                }
                                else
                                {
                                  this.imagesArrLocations.add(((TLRPC.PhotoSize)localObject2).location);
                                  this.imagesArrLocationsSizes.add(Integer.valueOf(((TLRPC.PhotoSize)localObject2).size));
                                  this.avatarsArr.add(localObject1);
                                  break;
                                }
                                j += 1;
                              }
                            }
                            if (!this.avatarsArr.isEmpty())
                            {
                              this.menuItem.showSubItem(6);
                              this.needSearchImageInArr = false;
                              this.currentIndex = -1;
                              if (paramInt1 == -1) {
                                break label917;
                              }
                              setImageIndex(paramInt1, true);
                            }
                            for (;;)
                            {
                              if (!bool) {
                                break label963;
                              }
                              MessagesController.getInstance(this.currentAccount).loadDialogPhotos(this.avatarsDialogId, 80, 0L, false, this.classGuid);
                              return;
                              this.menuItem.hideSubItem(6);
                              break;
                              this.avatarsArr.add(0, new TLRPC.TL_photoEmpty());
                              this.imagesArrLocations.add(0, this.currentFileLocation);
                              this.imagesArrLocationsSizes.add(0, Integer.valueOf(0));
                              setImageIndex(0, true);
                            }
                          }
                        }
                      }
                    }
                    if (paramInt1 != NotificationCenter.mediaCountDidLoaded) {
                      break label1231;
                    }
                    l1 = ((Long)paramVarArgs[0]).longValue();
                  } while ((l1 != this.currentDialogId) && (l1 != this.mergeDialogId));
                  if (l1 == this.currentDialogId) {
                    this.totalImagesCount = ((Integer)paramVarArgs[1]).intValue();
                  }
                  while ((this.needSearchImageInArr) && (this.isFirstLoading))
                  {
                    this.isFirstLoading = false;
                    this.loadingMoreImages = true;
                    DataQuery.getInstance(this.currentAccount).loadMedia(this.currentDialogId, 80, 0, 0, true, this.classGuid);
                    return;
                    if (l1 == this.mergeDialogId) {
                      this.totalImagesCountMerge = ((Integer)paramVarArgs[1]).intValue();
                    }
                  }
                } while (this.imagesArr.isEmpty());
                if (this.opennedFromMedia)
                {
                  this.actionBar.setTitle(LocaleController.formatString("Of", 2131494029, new Object[] { Integer.valueOf(this.currentIndex + 1), Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge) }));
                  return;
                }
                this.actionBar.setTitle(LocaleController.formatString("Of", 2131494029, new Object[] { Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge - this.imagesArr.size() + this.currentIndex + 1), Integer.valueOf(this.totalImagesCount + this.totalImagesCountMerge) }));
                return;
                if (paramInt1 != NotificationCenter.mediaDidLoaded) {
                  break label2251;
                }
                l1 = ((Long)paramVarArgs[0]).longValue();
                paramInt1 = ((Integer)paramVarArgs[3]).intValue();
              } while (((l1 != this.currentDialogId) && (l1 != this.mergeDialogId)) || (paramInt1 != this.classGuid));
              this.loadingMoreImages = false;
              if (l1 == this.currentDialogId) {}
              for (paramInt2 = 0;; paramInt2 = 1)
              {
                localObject1 = (ArrayList)paramVarArgs[2];
                this.endReached[paramInt2] = ((Boolean)paramVarArgs[5]).booleanValue();
                if (!this.needSearchImageInArr) {
                  break label2085;
                }
                if ((!((ArrayList)localObject1).isEmpty()) || ((paramInt2 == 0) && (this.mergeDialogId != 0L))) {
                  break;
                }
                this.needSearchImageInArr = false;
                return;
              }
              paramInt1 = -1;
              paramVarArgs = (MessageObject)this.imagesArr.get(this.currentIndex);
              int i = 0;
              int m = 0;
              if (m < ((ArrayList)localObject1).size())
              {
                localObject2 = (MessageObject)((ArrayList)localObject1).get(m);
                j = i;
                int k = paramInt1;
                if (this.imagesByIdsTemp[paramInt2].indexOfKey(((MessageObject)localObject2).getId()) < 0)
                {
                  this.imagesByIdsTemp[paramInt2].put(((MessageObject)localObject2).getId(), localObject2);
                  if (!this.opennedFromMedia) {
                    break label1508;
                  }
                  this.imagesArrTemp.add(localObject2);
                  if (((MessageObject)localObject2).getId() == paramVarArgs.getId()) {
                    paramInt1 = i;
                  }
                  j = i + 1;
                  k = paramInt1;
                }
                for (;;)
                {
                  m += 1;
                  i = j;
                  paramInt1 = k;
                  break;
                  i += 1;
                  this.imagesArrTemp.add(0, localObject2);
                  j = i;
                  k = paramInt1;
                  if (((MessageObject)localObject2).getId() == paramVarArgs.getId())
                  {
                    k = ((ArrayList)localObject1).size() - i;
                    j = i;
                  }
                }
              }
              if ((i == 0) && ((paramInt2 != 0) || (this.mergeDialogId == 0L)))
              {
                this.totalImagesCount = this.imagesArr.size();
                this.totalImagesCountMerge = 0;
              }
              if (paramInt1 != -1)
              {
                this.imagesArr.clear();
                this.imagesArr.addAll(this.imagesArrTemp);
                paramInt2 = 0;
                while (paramInt2 < 2)
                {
                  this.imagesByIds[paramInt2] = this.imagesByIdsTemp[paramInt2].clone();
                  this.imagesByIdsTemp[paramInt2].clear();
                  paramInt2 += 1;
                }
                this.imagesArrTemp.clear();
                this.needSearchImageInArr = false;
                this.currentIndex = -1;
                paramInt2 = paramInt1;
                if (paramInt1 >= this.imagesArr.size()) {
                  paramInt2 = this.imagesArr.size() - 1;
                }
                setImageIndex(paramInt2, true);
                return;
              }
              if (this.opennedFromMedia) {
                if (this.imagesArrTemp.isEmpty())
                {
                  j = 0;
                  paramInt1 = j;
                  i = paramInt2;
                  if (paramInt2 == 0)
                  {
                    paramInt1 = j;
                    i = paramInt2;
                    if (this.endReached[paramInt2] != 0)
                    {
                      paramInt1 = j;
                      i = paramInt2;
                      if (this.mergeDialogId != 0L)
                      {
                        paramInt2 = 1;
                        paramInt1 = j;
                        i = paramInt2;
                        if (!this.imagesArrTemp.isEmpty())
                        {
                          paramInt1 = j;
                          i = paramInt2;
                          if (((MessageObject)this.imagesArrTemp.get(this.imagesArrTemp.size() - 1)).getDialogId() != this.mergeDialogId)
                          {
                            paramInt1 = 0;
                            i = paramInt2;
                          }
                        }
                      }
                    }
                  }
                  if (this.endReached[i] != 0) {
                    break label2011;
                  }
                  this.loadingMoreImages = true;
                  if (!this.opennedFromMedia) {
                    break label2041;
                  }
                  paramVarArgs = DataQuery.getInstance(this.currentAccount);
                  if (i != 0) {
                    break label2032;
                  }
                }
              }
              for (long l1 = this.currentDialogId;; l1 = this.mergeDialogId)
              {
                paramVarArgs.loadMedia(l1, 80, paramInt1, 0, true, this.classGuid);
                return;
                j = ((MessageObject)this.imagesArrTemp.get(this.imagesArrTemp.size() - 1)).getId();
                break label1723;
                if (this.imagesArrTemp.isEmpty()) {}
                for (j = 0;; j = ((MessageObject)this.imagesArrTemp.get(0)).getId())
                {
                  paramInt1 = j;
                  i = paramInt2;
                  if (paramInt2 != 0) {
                    break label1822;
                  }
                  paramInt1 = j;
                  i = paramInt2;
                  if (this.endReached[paramInt2] == 0) {
                    break label1822;
                  }
                  paramInt1 = j;
                  i = paramInt2;
                  if (this.mergeDialogId == 0L) {
                    break label1822;
                  }
                  paramInt2 = 1;
                  paramInt1 = j;
                  i = paramInt2;
                  if (this.imagesArrTemp.isEmpty()) {
                    break label1822;
                  }
                  paramInt1 = j;
                  i = paramInt2;
                  if (((MessageObject)this.imagesArrTemp.get(0)).getDialogId() == this.mergeDialogId) {
                    break label1822;
                  }
                  paramInt1 = 0;
                  i = paramInt2;
                  break label1822;
                  break;
                }
              }
              paramVarArgs = DataQuery.getInstance(this.currentAccount);
              if (i == 0) {}
              for (l1 = this.currentDialogId;; l1 = this.mergeDialogId)
              {
                paramVarArgs.loadMedia(l1, 80, paramInt1, 0, true, this.classGuid);
                return;
              }
              paramInt1 = 0;
              paramVarArgs = ((ArrayList)localObject1).iterator();
              while (paramVarArgs.hasNext())
              {
                localObject1 = (MessageObject)paramVarArgs.next();
                if (this.imagesByIds[paramInt2].indexOfKey(((MessageObject)localObject1).getId()) < 0)
                {
                  paramInt1 += 1;
                  if (this.opennedFromMedia) {
                    this.imagesArr.add(localObject1);
                  }
                  for (;;)
                  {
                    this.imagesByIds[paramInt2].put(((MessageObject)localObject1).getId(), localObject1);
                    break;
                    this.imagesArr.add(0, localObject1);
                  }
                }
              }
              if (!this.opennedFromMedia) {
                break label2211;
              }
            } while (paramInt1 != 0);
            this.totalImagesCount = this.imagesArr.size();
            this.totalImagesCountMerge = 0;
            return;
            if (paramInt1 != 0)
            {
              paramInt2 = this.currentIndex;
              this.currentIndex = -1;
              setImageIndex(paramInt2 + paramInt1, true);
              return;
            }
            this.totalImagesCount = this.imagesArr.size();
            this.totalImagesCountMerge = 0;
            return;
            if (paramInt1 != NotificationCenter.emojiDidLoaded) {
              break label2273;
            }
          } while (this.captionTextView == null);
          this.captionTextView.invalidate();
          return;
          if (paramInt1 != NotificationCenter.FilePreparingFailed) {
            break label2381;
          }
          paramVarArgs = (MessageObject)paramVarArgs[0];
          if (this.loadInitialVideo)
          {
            this.loadInitialVideo = false;
            this.progressView.setVisibility(4);
            preparePlayer(this.currentPlayingVideoFile, false, false);
            return;
          }
          if (this.tryStartRequestPreviewOnFinish)
          {
            releasePlayer();
            if (!MediaController.getInstance().scheduleVideoConvert(this.videoPreviewMessageObject, true)) {}
            for (bool = true;; bool = false)
            {
              this.tryStartRequestPreviewOnFinish = bool;
              return;
            }
          }
        } while (paramVarArgs != this.videoPreviewMessageObject);
        this.requestingPreview = false;
        this.progressView.setVisibility(4);
        return;
      } while ((paramInt1 != NotificationCenter.FileNewChunkAvailable) || ((MessageObject)paramVarArgs[0] != this.videoPreviewMessageObject));
      localObject1 = (String)paramVarArgs[1];
    } while (((Long)paramVarArgs[3]).longValue() == 0L);
    this.requestingPreview = false;
    this.progressView.setVisibility(4);
    preparePlayer(Uri.fromFile(new File((String)localObject1)), false, true);
  }
  
  public void exitFromPip()
  {
    if (!this.isInline) {}
    for (;;)
    {
      return;
      if (Instance != null) {
        Instance.closePhoto(false, true);
      }
      Instance = PipInstance;
      PipInstance = null;
      this.switchingInlineMode = true;
      if (this.currentBitmap != null)
      {
        this.currentBitmap.recycle();
        this.currentBitmap = null;
      }
      this.changingTextureView = true;
      this.isInline = false;
      this.videoTextureView.setVisibility(4);
      this.aspectRatioFrameLayout.addView(this.videoTextureView);
      if (ApplicationLoader.mainInterfacePaused) {}
      try
      {
        this.parentActivity.startService(new Intent(ApplicationLoader.applicationContext, BringAppForegroundService.class));
        if (Build.VERSION.SDK_INT >= 21)
        {
          this.pipAnimationInProgress = true;
          org.telegram.ui.Components.Rect localRect = PipVideoView.getPipRect(this.aspectRatioFrameLayout.getAspectRatio());
          float f = localRect.width / this.textureImageView.getLayoutParams().width;
          localRect.y += AndroidUtilities.statusBarHeight;
          this.textureImageView.setScaleX(f);
          this.textureImageView.setScaleY(f);
          this.textureImageView.setTranslationX(localRect.x);
          this.textureImageView.setTranslationY(localRect.y);
          this.videoTextureView.setScaleX(f);
          this.videoTextureView.setScaleY(f);
          this.videoTextureView.setTranslationX(localRect.x - this.aspectRatioFrameLayout.getX());
          this.videoTextureView.setTranslationY(localRect.y - this.aspectRatioFrameLayout.getY());
        }
      }
      catch (Throwable localThrowable)
      {
        try
        {
          for (;;)
          {
            this.isVisible = true;
            ((WindowManager)this.parentActivity.getSystemService("window")).addView(this.windowView, this.windowLayoutParams);
            if (this.currentPlaceObject != null) {
              this.currentPlaceObject.imageReceiver.setVisible(false, false);
            }
            if (Build.VERSION.SDK_INT < 21) {
              break;
            }
            this.waitingForDraw = 4;
            return;
            localThrowable = localThrowable;
            FileLog.e(localThrowable);
            continue;
            this.pipVideoView.close();
            this.pipVideoView = null;
          }
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
  
  @Keep
  public float getAnimationValue()
  {
    return this.animationValue;
  }
  
  public int getSelectiongLength()
  {
    if (this.captionEditText != null) {
      return this.captionEditText.getSelectionLength();
    }
    return 0;
  }
  
  public VideoPlayer getVideoPlayer()
  {
    return this.videoPlayer;
  }
  
  public boolean isMuteVideo()
  {
    return this.muteVideo;
  }
  
  public boolean isVisible()
  {
    return (this.isVisible) && (this.placeProvider != null);
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    if (this.pipVideoView != null) {
      this.pipVideoView.onConfigurationChanged();
    }
  }
  
  public boolean onDoubleTap(MotionEvent paramMotionEvent)
  {
    float f1;
    if ((this.videoPlayer != null) && (this.videoPlayerControlFrameLayout.getVisibility() == 0))
    {
      long l4 = this.videoPlayer.getCurrentPosition();
      long l3 = this.videoPlayer.getDuration();
      if ((l3 >= 0L) && (l4 >= 0L) && (l3 != -9223372036854775807L) && (l4 != -9223372036854775807L))
      {
        int i = getContainerViewWidth();
        f1 = paramMotionEvent.getX();
        long l1;
        long l2;
        if (f1 >= i / 3 * 2)
        {
          l1 = l4 + 10000L;
          l2 = l1;
          if (l4 == l2) {
            break label203;
          }
          if (l2 <= l3) {
            break label186;
          }
          l1 = l3;
        }
        for (;;)
        {
          this.videoPlayer.seekTo(l1);
          this.containerView.invalidate();
          this.videoPlayerSeekbar.setProgress((float)l1 / (float)l3);
          this.videoPlayerControlFrameLayout.invalidate();
          return true;
          l1 = l4;
          if (f1 >= i / 3) {
            break;
          }
          l1 = l4 - 10000L;
          break;
          label186:
          l1 = l2;
          if (l2 < 0L) {
            l1 = 0L;
          }
        }
      }
    }
    label203:
    if ((!this.canZoom) || ((this.scale == 1.0F) && ((this.translationY != 0.0F) || (this.translationX != 0.0F)))) {
      return false;
    }
    if ((this.animationStartTime != 0L) || (this.animationInProgress != 0)) {
      return false;
    }
    float f2;
    float f3;
    if (this.scale == 1.0F)
    {
      f2 = paramMotionEvent.getX() - getContainerViewWidth() / 2 - (paramMotionEvent.getX() - getContainerViewWidth() / 2 - this.translationX) * (3.0F / this.scale);
      f3 = paramMotionEvent.getY() - getContainerViewHeight() / 2 - (paramMotionEvent.getY() - getContainerViewHeight() / 2 - this.translationY) * (3.0F / this.scale);
      updateMinMax(3.0F);
      if (f2 < this.minX)
      {
        f1 = this.minX;
        if (f3 >= this.minY) {
          break label419;
        }
        f2 = this.minY;
        label383:
        animateTo(3.0F, f1, f2, true);
      }
    }
    for (;;)
    {
      this.doubleTap = true;
      return true;
      f1 = f2;
      if (f2 <= this.maxX) {
        break;
      }
      f1 = this.maxX;
      break;
      label419:
      f2 = f3;
      if (f3 <= this.maxY) {
        break label383;
      }
      f2 = this.maxY;
      break label383;
      animateTo(1.0F, 0.0F, 0.0F, true);
    }
  }
  
  public boolean onDoubleTapEvent(MotionEvent paramMotionEvent)
  {
    return false;
  }
  
  public boolean onDown(MotionEvent paramMotionEvent)
  {
    return false;
  }
  
  public boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2)
  {
    if (this.scale != 1.0F)
    {
      this.scroller.abortAnimation();
      this.scroller.fling(Math.round(this.translationX), Math.round(this.translationY), Math.round(paramFloat1), Math.round(paramFloat2), (int)this.minX, (int)this.maxX, (int)this.minY, (int)this.maxY);
      this.containerView.postInvalidate();
    }
    return false;
  }
  
  public void onLongPress(MotionEvent paramMotionEvent) {}
  
  public void onPause()
  {
    if (this.currentAnimation != null) {
      closePhoto(false, false);
    }
    while (this.lastTitle == null) {
      return;
    }
    closeCaptionEnter(true);
  }
  
  public void onResume()
  {
    redraw(0);
    if (this.videoPlayer != null) {
      this.videoPlayer.seekTo(this.videoPlayer.getCurrentPosition() + 1L);
    }
  }
  
  public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2)
  {
    return false;
  }
  
  public void onShowPress(MotionEvent paramMotionEvent) {}
  
  public boolean onSingleTapConfirmed(MotionEvent paramMotionEvent)
  {
    boolean bool1 = false;
    boolean bool2 = true;
    if (this.discardTap) {
      bool1 = false;
    }
    do
    {
      do
      {
        float f2;
        do
        {
          do
          {
            float f1;
            do
            {
              do
              {
                int i;
                do
                {
                  do
                  {
                    return bool1;
                    if (this.containerView.getTag() != null)
                    {
                      if ((this.aspectRatioFrameLayout != null) && (this.aspectRatioFrameLayout.getVisibility() == 0)) {}
                      for (i = 1;; i = 0)
                      {
                        f1 = paramMotionEvent.getX();
                        f2 = paramMotionEvent.getY();
                        if ((this.photoProgressViews[0] == null) || (this.containerView == null) || (i != 0)) {
                          break;
                        }
                        i = this.photoProgressViews[0].backgroundState;
                        if ((i <= 0) || (i > 3) || (f1 < (getContainerViewWidth() - AndroidUtilities.dp(100.0F)) / 2.0F) || (f1 > (getContainerViewWidth() + AndroidUtilities.dp(100.0F)) / 2.0F) || (f2 < (getContainerViewHeight() - AndroidUtilities.dp(100.0F)) / 2.0F) || (f2 > (getContainerViewHeight() + AndroidUtilities.dp(100.0F)) / 2.0F)) {
                          break;
                        }
                        onActionClick(true);
                        checkProgress(0, true);
                        return true;
                      }
                      if (!this.isActionBarVisible) {
                        bool1 = true;
                      }
                      toggleActionBar(bool1, true);
                      return true;
                    }
                    if (this.sendPhotoType == 0)
                    {
                      if (this.isCurrentVideo)
                      {
                        this.videoPlayButton.callOnClick();
                        return true;
                      }
                      this.checkImageView.performClick();
                      return true;
                    }
                    if ((this.currentBotInlineResult == null) || ((!this.currentBotInlineResult.type.equals("video")) && (!MessageObject.isVideoDocument(this.currentBotInlineResult.document)))) {
                      break;
                    }
                    i = this.photoProgressViews[0].backgroundState;
                    bool1 = bool2;
                  } while (i <= 0);
                  bool1 = bool2;
                } while (i > 3);
                f1 = paramMotionEvent.getX();
                f2 = paramMotionEvent.getY();
                bool1 = bool2;
              } while (f1 < (getContainerViewWidth() - AndroidUtilities.dp(100.0F)) / 2.0F);
              bool1 = bool2;
            } while (f1 > (getContainerViewWidth() + AndroidUtilities.dp(100.0F)) / 2.0F);
            bool1 = bool2;
          } while (f2 < (getContainerViewHeight() - AndroidUtilities.dp(100.0F)) / 2.0F);
          bool1 = bool2;
        } while (f2 > (getContainerViewHeight() + AndroidUtilities.dp(100.0F)) / 2.0F);
        onActionClick(true);
        checkProgress(0, true);
        return true;
        bool1 = bool2;
      } while (this.sendPhotoType != 2);
      bool1 = bool2;
    } while (!this.isCurrentVideo);
    this.videoPlayButton.callOnClick();
    return true;
  }
  
  public boolean onSingleTapUp(MotionEvent paramMotionEvent)
  {
    return false;
  }
  
  public boolean openPhoto(ArrayList<MessageObject> paramArrayList, int paramInt, long paramLong1, long paramLong2, PhotoViewerProvider paramPhotoViewerProvider)
  {
    return openPhoto((MessageObject)paramArrayList.get(paramInt), null, paramArrayList, null, paramInt, paramPhotoViewerProvider, null, paramLong1, paramLong2);
  }
  
  public boolean openPhoto(MessageObject paramMessageObject, long paramLong1, long paramLong2, PhotoViewerProvider paramPhotoViewerProvider)
  {
    return openPhoto(paramMessageObject, null, null, null, 0, paramPhotoViewerProvider, null, paramLong1, paramLong2);
  }
  
  public boolean openPhoto(final MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, ArrayList<MessageObject> paramArrayList, final ArrayList<Object> paramArrayList1, int paramInt, PhotoViewerProvider paramPhotoViewerProvider, ChatActivity paramChatActivity, long paramLong1, long paramLong2)
  {
    if ((this.parentActivity == null) || (this.isVisible) || ((paramPhotoViewerProvider == null) && (checkAnimation())) || ((paramMessageObject == null) && (paramFileLocation == null) && (paramArrayList == null) && (paramArrayList1 == null))) {
      return false;
    }
    final PlaceProviderObject localPlaceProviderObject = paramPhotoViewerProvider.getPlaceForPhoto(paramMessageObject, paramFileLocation, paramInt);
    if ((localPlaceProviderObject == null) && (paramArrayList1 == null)) {
      return false;
    }
    this.lastInsets = null;
    WindowManager localWindowManager = (WindowManager)this.parentActivity.getSystemService("window");
    if (this.attachedToWindow) {}
    try
    {
      localWindowManager.removeView(this.windowView);
      for (;;)
      {
        try
        {
          this.windowLayoutParams.type = 99;
          if (Build.VERSION.SDK_INT >= 21)
          {
            this.windowLayoutParams.flags = -2147417848;
            this.windowLayoutParams.softInputMode = 272;
            this.windowView.setFocusable(false);
            this.containerView.setFocusable(false);
            localWindowManager.addView(this.windowView, this.windowLayoutParams);
            this.doneButtonPressed = false;
            this.parentChatActivity = paramChatActivity;
            this.actionBar.setTitle(LocaleController.formatString("Of", 2131494029, new Object[] { Integer.valueOf(1), Integer.valueOf(1) }));
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileDidFailedLoad);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileLoadProgressChanged);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mediaCountDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mediaDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.dialogPhotosLoaded);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FilePreparingFailed);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileNewChunkAvailable);
            this.placeProvider = paramPhotoViewerProvider;
            this.mergeDialogId = paramLong2;
            this.currentDialogId = paramLong1;
            this.selectedPhotosAdapter.notifyDataSetChanged();
            if (this.velocityTracker == null) {
              this.velocityTracker = VelocityTracker.obtain();
            }
            this.isVisible = true;
            toggleActionBar(true, false);
            togglePhotosListView(false, false);
            if (localPlaceProviderObject == null) {
              break;
            }
            this.disableShowCheck = true;
            this.animationInProgress = 1;
            if (paramMessageObject != null) {
              this.currentAnimation = localPlaceProviderObject.imageReceiver.getAnimation();
            }
            onPhotoShow(paramMessageObject, paramFileLocation, paramArrayList, paramArrayList1, paramInt, localPlaceProviderObject);
            paramMessageObject = localPlaceProviderObject.imageReceiver.getDrawRegion();
            paramInt = localPlaceProviderObject.imageReceiver.getOrientation();
            i = localPlaceProviderObject.imageReceiver.getAnimatedOrientation();
            if (i != 0) {
              paramInt = i;
            }
            this.animatingImageView.setVisibility(0);
            this.animatingImageView.setRadius(localPlaceProviderObject.radius);
            this.animatingImageView.setOrientation(paramInt);
            paramFileLocation = this.animatingImageView;
            if (localPlaceProviderObject.radius != 0)
            {
              bool = true;
              paramFileLocation.setNeedRadius(bool);
              this.animatingImageView.setImageBitmap(localPlaceProviderObject.thumb);
              this.animatingImageView.setAlpha(1.0F);
              this.animatingImageView.setPivotX(0.0F);
              this.animatingImageView.setPivotY(0.0F);
              this.animatingImageView.setScaleX(localPlaceProviderObject.scale);
              this.animatingImageView.setScaleY(localPlaceProviderObject.scale);
              this.animatingImageView.setTranslationX(localPlaceProviderObject.viewX + paramMessageObject.left * localPlaceProviderObject.scale);
              this.animatingImageView.setTranslationY(localPlaceProviderObject.viewY + paramMessageObject.top * localPlaceProviderObject.scale);
              paramFileLocation = this.animatingImageView.getLayoutParams();
              paramFileLocation.width = (paramMessageObject.right - paramMessageObject.left);
              paramFileLocation.height = (paramMessageObject.bottom - paramMessageObject.top);
              this.animatingImageView.setLayoutParams(paramFileLocation);
              float f1 = AndroidUtilities.displaySize.x / paramFileLocation.width;
              i = AndroidUtilities.displaySize.y;
              if (Build.VERSION.SDK_INT < 21) {
                break label1451;
              }
              paramInt = AndroidUtilities.statusBarHeight;
              float f2 = (paramInt + i) / paramFileLocation.height;
              if (f1 <= f2) {
                break label1457;
              }
              f1 = f2;
              float f3 = paramFileLocation.width;
              f2 = paramFileLocation.height;
              f3 = (AndroidUtilities.displaySize.x - f3 * f1) / 2.0F;
              i = AndroidUtilities.displaySize.y;
              if (Build.VERSION.SDK_INT < 21) {
                break label1460;
              }
              paramInt = AndroidUtilities.statusBarHeight;
              f2 = (paramInt + i - f2 * f1) / 2.0F;
              int k = Math.abs(paramMessageObject.left - localPlaceProviderObject.imageReceiver.getImageX());
              int m = Math.abs(paramMessageObject.top - localPlaceProviderObject.imageReceiver.getImageY());
              paramArrayList = new int[2];
              localPlaceProviderObject.parentView.getLocationInWindow(paramArrayList);
              i = paramArrayList[1];
              if (Build.VERSION.SDK_INT < 21) {
                break label1466;
              }
              paramInt = 0;
              i = i - paramInt - (localPlaceProviderObject.viewY + paramMessageObject.top) + localPlaceProviderObject.clipTopAddition;
              paramInt = i;
              if (i < 0) {
                paramInt = 0;
              }
              int j = localPlaceProviderObject.viewY;
              int n = paramMessageObject.top;
              int i1 = paramFileLocation.height;
              int i2 = paramArrayList[1];
              int i3 = localPlaceProviderObject.parentView.getHeight();
              if (Build.VERSION.SDK_INT < 21) {
                break label1474;
              }
              i = 0;
              j = i1 + (j + n) - (i3 + i2 - i) + localPlaceProviderObject.clipBottomAddition;
              i = j;
              if (j < 0) {
                i = 0;
              }
              paramInt = Math.max(paramInt, m);
              i = Math.max(i, m);
              this.animationValues[0][0] = this.animatingImageView.getScaleX();
              this.animationValues[0][1] = this.animatingImageView.getScaleY();
              this.animationValues[0][2] = this.animatingImageView.getTranslationX();
              this.animationValues[0][3] = this.animatingImageView.getTranslationY();
              this.animationValues[0][4] = (k * localPlaceProviderObject.scale);
              this.animationValues[0][5] = (paramInt * localPlaceProviderObject.scale);
              this.animationValues[0][6] = (i * localPlaceProviderObject.scale);
              this.animationValues[0][7] = this.animatingImageView.getRadius();
              this.animationValues[1][0] = f1;
              this.animationValues[1][1] = f1;
              this.animationValues[1][2] = f3;
              this.animationValues[1][3] = f2;
              this.animationValues[1][4] = 0;
              this.animationValues[1][5] = 0;
              this.animationValues[1][6] = 0;
              this.animationValues[1][7] = 0;
              this.animatingImageView.setAnimationProgress(0.0F);
              this.backgroundDrawable.setAlpha(0);
              this.containerView.setAlpha(0.0F);
              paramMessageObject = new AnimatorSet();
              paramMessageObject.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.animatingImageView, "animationProgress", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofInt(this.backgroundDrawable, "alpha", new int[] { 0, 255 }), ObjectAnimator.ofFloat(this.containerView, "alpha", new float[] { 0.0F, 1.0F }) });
              this.animationEndRunnable = new Runnable()
              {
                public void run()
                {
                  if ((PhotoViewer.this.containerView == null) || (PhotoViewer.this.windowView == null)) {}
                  do
                  {
                    return;
                    if (Build.VERSION.SDK_INT >= 18) {
                      PhotoViewer.this.containerView.setLayerType(0, null);
                    }
                    PhotoViewer.access$7502(PhotoViewer.this, 0);
                    PhotoViewer.access$16102(PhotoViewer.this, 0L);
                    PhotoViewer.this.setImages();
                    PhotoViewer.this.containerView.invalidate();
                    PhotoViewer.this.animatingImageView.setVisibility(8);
                    if (PhotoViewer.this.showAfterAnimation != null) {
                      PhotoViewer.this.showAfterAnimation.imageReceiver.setVisible(true, true);
                    }
                    if (PhotoViewer.this.hideAfterAnimation != null) {
                      PhotoViewer.this.hideAfterAnimation.imageReceiver.setVisible(false, true);
                    }
                  } while ((paramArrayList1 == null) || (PhotoViewer.this.sendPhotoType == 3));
                  if (Build.VERSION.SDK_INT >= 21) {}
                  for (PhotoViewer.this.windowLayoutParams.flags = -2147417856;; PhotoViewer.this.windowLayoutParams.flags = 0)
                  {
                    PhotoViewer.this.windowLayoutParams.softInputMode = 272;
                    ((WindowManager)PhotoViewer.this.parentActivity.getSystemService("window")).updateViewLayout(PhotoViewer.this.windowView, PhotoViewer.this.windowLayoutParams);
                    PhotoViewer.this.windowView.setFocusable(true);
                    PhotoViewer.this.containerView.setFocusable(true);
                    return;
                  }
                }
              };
              paramMessageObject.setDuration(200L);
              paramMessageObject.addListener(new AnimatorListenerAdapter()
              {
                public void onAnimationEnd(Animator paramAnonymousAnimator)
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      NotificationCenter.getInstance(PhotoViewer.this.currentAccount).setAnimationInProgress(false);
                      if (PhotoViewer.this.animationEndRunnable != null)
                      {
                        PhotoViewer.this.animationEndRunnable.run();
                        PhotoViewer.access$16602(PhotoViewer.this, null);
                      }
                    }
                  });
                }
              });
              this.transitionAnimationStartTime = System.currentTimeMillis();
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  NotificationCenter.getInstance(PhotoViewer.this.currentAccount).setAllowedNotificationsDutingAnimation(new int[] { NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.mediaCountDidLoaded, NotificationCenter.mediaDidLoaded, NotificationCenter.dialogPhotosLoaded });
                  NotificationCenter.getInstance(PhotoViewer.this.currentAccount).setAnimationInProgress(true);
                  paramMessageObject.start();
                }
              });
              if (Build.VERSION.SDK_INT >= 18) {
                this.containerView.setLayerType(2, null);
              }
              BackgroundDrawable.access$16702(this.backgroundDrawable, new Runnable()
              {
                public void run()
                {
                  PhotoViewer.access$8702(PhotoViewer.this, false);
                  localPlaceProviderObject.imageReceiver.setVisible(false, true);
                }
              });
              return true;
            }
          }
          else
          {
            this.windowLayoutParams.flags = 8;
            continue;
          }
          boolean bool = false;
        }
        catch (Exception paramMessageObject)
        {
          FileLog.e(paramMessageObject);
          return false;
        }
        continue;
        label1451:
        paramInt = 0;
        continue;
        label1457:
        continue;
        label1460:
        paramInt = 0;
        continue;
        label1466:
        paramInt = AndroidUtilities.statusBarHeight;
        continue;
        label1474:
        int i = AndroidUtilities.statusBarHeight;
      }
      if ((paramArrayList1 != null) && (this.sendPhotoType != 3)) {
        if (Build.VERSION.SDK_INT < 21) {
          break label1588;
        }
      }
      label1588:
      for (this.windowLayoutParams.flags = -2147417856;; this.windowLayoutParams.flags = 0)
      {
        this.windowLayoutParams.softInputMode = 272;
        localWindowManager.updateViewLayout(this.windowView, this.windowLayoutParams);
        this.windowView.setFocusable(true);
        this.containerView.setFocusable(true);
        this.backgroundDrawable.setAlpha(255);
        this.containerView.setAlpha(1.0F);
        onPhotoShow(paramMessageObject, paramFileLocation, paramArrayList, paramArrayList1, paramInt, localPlaceProviderObject);
        break;
      }
    }
    catch (Exception localException)
    {
      for (;;) {}
    }
  }
  
  public boolean openPhoto(TLRPC.FileLocation paramFileLocation, PhotoViewerProvider paramPhotoViewerProvider)
  {
    return openPhoto(null, paramFileLocation, null, null, 0, paramPhotoViewerProvider, null, 0L, 0L);
  }
  
  public boolean openPhotoForSelect(ArrayList<Object> paramArrayList, int paramInt1, int paramInt2, PhotoViewerProvider paramPhotoViewerProvider, ChatActivity paramChatActivity)
  {
    this.sendPhotoType = paramInt2;
    FrameLayout.LayoutParams localLayoutParams;
    if (this.pickerViewSendButton != null)
    {
      localLayoutParams = (FrameLayout.LayoutParams)this.itemsLayout.getLayoutParams();
      if (this.sendPhotoType != 1) {
        break label108;
      }
      this.pickerView.setPadding(0, AndroidUtilities.dp(14.0F), 0, 0);
      this.pickerViewSendButton.setImageResource(2131165231);
      this.pickerViewSendButton.setPadding(0, AndroidUtilities.dp(1.0F), 0, 0);
    }
    for (localLayoutParams.bottomMargin = AndroidUtilities.dp(16.0F);; localLayoutParams.bottomMargin = 0)
    {
      this.itemsLayout.setLayoutParams(localLayoutParams);
      return openPhoto(null, null, null, paramArrayList, paramInt1, paramPhotoViewerProvider, paramChatActivity, 0L, 0L);
      label108:
      this.pickerView.setPadding(0, 0, 0, 0);
      this.pickerViewSendButton.setImageResource(2131165407);
      this.pickerViewSendButton.setPadding(AndroidUtilities.dp(4.0F), 0, 0, 0);
    }
  }
  
  @Keep
  public void setAnimationValue(float paramFloat)
  {
    this.animationValue = paramFloat;
    this.containerView.invalidate();
  }
  
  public void setParentActivity(Activity paramActivity)
  {
    this.currentAccount = UserConfig.selectedAccount;
    this.centerImage.setCurrentAccount(this.currentAccount);
    this.leftImage.setCurrentAccount(this.currentAccount);
    this.rightImage.setCurrentAccount(this.currentAccount);
    if (this.parentActivity == paramActivity) {
      return;
    }
    this.parentActivity = paramActivity;
    this.actvityContext = new ContextThemeWrapper(this.parentActivity, 2131558412);
    if (progressDrawables == null)
    {
      progressDrawables = new Drawable[4];
      progressDrawables[0] = this.parentActivity.getResources().getDrawable(2131165275);
      progressDrawables[1] = this.parentActivity.getResources().getDrawable(2131165265);
      progressDrawables[2] = this.parentActivity.getResources().getDrawable(2131165469);
      progressDrawables[3] = this.parentActivity.getResources().getDrawable(2131165606);
    }
    this.scroller = new Scroller(paramActivity);
    this.windowView = new FrameLayout(paramActivity)
    {
      private Runnable attachRunnable;
      
      public boolean dispatchKeyEventPreIme(KeyEvent paramAnonymousKeyEvent)
      {
        if ((paramAnonymousKeyEvent != null) && (paramAnonymousKeyEvent.getKeyCode() == 4) && (paramAnonymousKeyEvent.getAction() == 1))
        {
          if ((PhotoViewer.this.captionEditText.isPopupShowing()) || (PhotoViewer.this.captionEditText.isKeyboardVisible()))
          {
            PhotoViewer.this.closeCaptionEnter(false);
            return false;
          }
          PhotoViewer.getInstance().closePhoto(true, false);
          return true;
        }
        return super.dispatchKeyEventPreIme(paramAnonymousKeyEvent);
      }
      
      protected boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
      {
        boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
        if ((Build.VERSION.SDK_INT >= 21) && (paramAnonymousView == PhotoViewer.this.animatingImageView) && (PhotoViewer.this.lastInsets != null))
        {
          paramAnonymousView = (WindowInsets)PhotoViewer.this.lastInsets;
          paramAnonymousCanvas.drawRect(0.0F, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight() + paramAnonymousView.getSystemWindowInsetBottom(), PhotoViewer.this.blackPaint);
        }
        return bool;
      }
      
      protected void onAttachedToWindow()
      {
        super.onAttachedToWindow();
        PhotoViewer.access$7302(PhotoViewer.this, true);
      }
      
      protected void onDetachedFromWindow()
      {
        super.onDetachedFromWindow();
        PhotoViewer.access$7302(PhotoViewer.this, false);
        PhotoViewer.access$6602(PhotoViewer.this, false);
      }
      
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return (PhotoViewer.this.isVisible) && (super.onInterceptTouchEvent(paramAnonymousMotionEvent));
      }
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        paramAnonymousInt2 = 0;
        paramAnonymousInt1 = paramAnonymousInt2;
        if (Build.VERSION.SDK_INT >= 21)
        {
          paramAnonymousInt1 = paramAnonymousInt2;
          if (PhotoViewer.this.lastInsets != null) {
            paramAnonymousInt1 = 0 + ((WindowInsets)PhotoViewer.this.lastInsets).getSystemWindowInsetLeft();
          }
        }
        PhotoViewer.this.animatingImageView.layout(paramAnonymousInt1, 0, PhotoViewer.this.animatingImageView.getMeasuredWidth() + paramAnonymousInt1, PhotoViewer.this.animatingImageView.getMeasuredHeight());
        PhotoViewer.this.containerView.layout(paramAnonymousInt1, 0, PhotoViewer.this.containerView.getMeasuredWidth() + paramAnonymousInt1, PhotoViewer.this.containerView.getMeasuredHeight());
        PhotoViewer.access$6602(PhotoViewer.this, true);
        if (paramAnonymousBoolean)
        {
          if (!PhotoViewer.this.dontResetZoomOnFirstLayout)
          {
            PhotoViewer.access$6802(PhotoViewer.this, 1.0F);
            PhotoViewer.access$6902(PhotoViewer.this, 0.0F);
            PhotoViewer.access$7002(PhotoViewer.this, 0.0F);
            PhotoViewer.this.updateMinMax(PhotoViewer.this.scale);
          }
          if (PhotoViewer.this.checkImageView != null) {
            PhotoViewer.this.checkImageView.post(new Runnable()
            {
              public void run()
              {
                int j = 0;
                FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)PhotoViewer.this.checkImageView.getLayoutParams();
                ((WindowManager)ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
                int k = (ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(40.0F)) / 2;
                if (Build.VERSION.SDK_INT >= 21) {}
                for (int i = AndroidUtilities.statusBarHeight;; i = 0)
                {
                  localLayoutParams.topMargin = (i + k);
                  PhotoViewer.this.checkImageView.setLayoutParams(localLayoutParams);
                  localLayoutParams = (FrameLayout.LayoutParams)PhotoViewer.this.photosCounterView.getLayoutParams();
                  k = (ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(40.0F)) / 2;
                  i = j;
                  if (Build.VERSION.SDK_INT >= 21) {
                    i = AndroidUtilities.statusBarHeight;
                  }
                  localLayoutParams.topMargin = (k + i);
                  PhotoViewer.this.photosCounterView.setLayoutParams(localLayoutParams);
                  return;
                }
              }
            });
          }
        }
        if (PhotoViewer.this.dontResetZoomOnFirstLayout)
        {
          PhotoViewer.this.setScaleToFill();
          PhotoViewer.access$6702(PhotoViewer.this, false);
        }
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        int j = View.MeasureSpec.getSize(paramAnonymousInt1);
        paramAnonymousInt1 = View.MeasureSpec.getSize(paramAnonymousInt2);
        Object localObject;
        int i;
        if ((Build.VERSION.SDK_INT >= 21) && (PhotoViewer.this.lastInsets != null))
        {
          localObject = (WindowInsets)PhotoViewer.this.lastInsets;
          paramAnonymousInt2 = paramAnonymousInt1;
          if (AndroidUtilities.incorrectDisplaySizeFix)
          {
            paramAnonymousInt2 = paramAnonymousInt1;
            if (paramAnonymousInt1 > AndroidUtilities.displaySize.y) {
              paramAnonymousInt2 = AndroidUtilities.displaySize.y;
            }
            paramAnonymousInt2 += AndroidUtilities.statusBarHeight;
          }
          paramAnonymousInt2 -= ((WindowInsets)localObject).getSystemWindowInsetBottom();
          i = j - ((WindowInsets)localObject).getSystemWindowInsetRight();
        }
        for (;;)
        {
          setMeasuredDimension(i, paramAnonymousInt2);
          paramAnonymousInt1 = i;
          if (Build.VERSION.SDK_INT >= 21)
          {
            paramAnonymousInt1 = i;
            if (PhotoViewer.this.lastInsets != null) {
              paramAnonymousInt1 = i - ((WindowInsets)PhotoViewer.this.lastInsets).getSystemWindowInsetLeft();
            }
          }
          localObject = PhotoViewer.this.animatingImageView.getLayoutParams();
          PhotoViewer.this.animatingImageView.measure(View.MeasureSpec.makeMeasureSpec(((ViewGroup.LayoutParams)localObject).width, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(((ViewGroup.LayoutParams)localObject).height, Integer.MIN_VALUE));
          PhotoViewer.this.containerView.measure(View.MeasureSpec.makeMeasureSpec(paramAnonymousInt1, 1073741824), View.MeasureSpec.makeMeasureSpec(paramAnonymousInt2, 1073741824));
          return;
          paramAnonymousInt2 = paramAnonymousInt1;
          i = j;
          if (paramAnonymousInt1 > AndroidUtilities.displaySize.y)
          {
            paramAnonymousInt2 = AndroidUtilities.displaySize.y;
            i = j;
          }
        }
      }
      
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return (PhotoViewer.this.isVisible) && (PhotoViewer.this.onTouchEvent(paramAnonymousMotionEvent));
      }
      
      public ActionMode startActionModeForChild(View paramAnonymousView, ActionMode.Callback paramAnonymousCallback, int paramAnonymousInt)
      {
        if (Build.VERSION.SDK_INT >= 23)
        {
          Object localObject = PhotoViewer.this.parentActivity.findViewById(16908290);
          if ((localObject instanceof ViewGroup)) {
            try
            {
              localObject = ((ViewGroup)localObject).startActionModeForChild(paramAnonymousView, paramAnonymousCallback, paramAnonymousInt);
              return (ActionMode)localObject;
            }
            catch (Throwable localThrowable)
            {
              FileLog.e(localThrowable);
            }
          }
        }
        return super.startActionModeForChild(paramAnonymousView, paramAnonymousCallback, paramAnonymousInt);
      }
    };
    this.windowView.setBackgroundDrawable(this.backgroundDrawable);
    this.windowView.setClipChildren(true);
    this.windowView.setFocusable(false);
    this.animatingImageView = new ClippingImageView(paramActivity);
    this.animatingImageView.setAnimationValues(this.animationValues);
    this.windowView.addView(this.animatingImageView, LayoutHelper.createFrame(40, 40.0F));
    this.containerView = new FrameLayoutDrawer(paramActivity);
    this.containerView.setFocusable(false);
    this.windowView.addView(this.containerView, LayoutHelper.createFrame(-1, -1, 51));
    if (Build.VERSION.SDK_INT >= 21)
    {
      this.containerView.setFitsSystemWindows(true);
      this.containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener()
      {
        @SuppressLint({"NewApi"})
        public WindowInsets onApplyWindowInsets(View paramAnonymousView, WindowInsets paramAnonymousWindowInsets)
        {
          paramAnonymousView = (WindowInsets)PhotoViewer.this.lastInsets;
          PhotoViewer.access$6402(PhotoViewer.this, paramAnonymousWindowInsets);
          if ((paramAnonymousView == null) || (!paramAnonymousView.toString().equals(paramAnonymousWindowInsets.toString())))
          {
            if (PhotoViewer.this.animationInProgress == 1)
            {
              PhotoViewer.this.animatingImageView.setTranslationX(PhotoViewer.this.animatingImageView.getTranslationX() - PhotoViewer.this.getLeftInset());
              PhotoViewer.this.animationValues[0][2] = PhotoViewer.this.animatingImageView.getTranslationX();
            }
            PhotoViewer.this.windowView.requestLayout();
          }
          return paramAnonymousWindowInsets.consumeSystemWindowInsets();
        }
      });
      this.containerView.setSystemUiVisibility(1280);
    }
    this.windowLayoutParams = new WindowManager.LayoutParams();
    this.windowLayoutParams.height = -1;
    this.windowLayoutParams.format = -3;
    this.windowLayoutParams.width = -1;
    this.windowLayoutParams.gravity = 51;
    this.windowLayoutParams.type = 99;
    if (Build.VERSION.SDK_INT >= 21)
    {
      this.windowLayoutParams.flags = -2147417848;
      this.actionBar = new ActionBar(paramActivity)
      {
        public void setAlpha(float paramAnonymousFloat)
        {
          super.setAlpha(paramAnonymousFloat);
          PhotoViewer.this.containerView.invalidate();
        }
      };
      this.actionBar.setTitleColor(-1);
      this.actionBar.setSubtitleColor(-1);
      this.actionBar.setBackgroundColor(2130706432);
      paramActivity = this.actionBar;
      if (Build.VERSION.SDK_INT < 21) {
        break label948;
      }
    }
    int i;
    label948:
    for (boolean bool = true;; bool = false)
    {
      paramActivity.setOccupyStatusBar(bool);
      this.actionBar.setItemsBackgroundColor(1090519039, false);
      this.actionBar.setBackButtonImage(2131165346);
      this.actionBar.setTitle(LocaleController.formatString("Of", 2131494029, new Object[] { Integer.valueOf(1), Integer.valueOf(1) }));
      this.containerView.addView(this.actionBar, LayoutHelper.createFrame(-1, -2.0F));
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public boolean canOpenMenu()
        {
          if (PhotoViewer.this.currentMessageObject != null) {
            if (!FileLoader.getPathToMessage(PhotoViewer.this.currentMessageObject.messageOwner).exists()) {
              break label82;
            }
          }
          for (;;)
          {
            return true;
            TLRPC.FileLocation localFileLocation;
            if (PhotoViewer.this.currentFileLocation != null)
            {
              localFileLocation = PhotoViewer.this.currentFileLocation;
              if ((PhotoViewer.this.avatarsDialogId == 0) && (!PhotoViewer.this.isEvent)) {
                break label84;
              }
            }
            label82:
            label84:
            for (boolean bool = true; !FileLoader.getPathToAttach(localFileLocation, bool).exists(); bool = false) {
              return false;
            }
          }
        }
        
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1) {
            if ((PhotoViewer.this.needCaptionLayout) && ((PhotoViewer.this.captionEditText.isPopupShowing()) || (PhotoViewer.this.captionEditText.isKeyboardVisible()))) {
              PhotoViewer.this.closeCaptionEnter(false);
            }
          }
          label343:
          label348:
          label732:
          label1035:
          label1228:
          label1241:
          label1424:
          label1450:
          label1484:
          label1494:
          do
          {
            do
            {
              do
              {
                Object localObject1;
                final Object localObject2;
                do
                {
                  do
                  {
                    do
                    {
                      do
                      {
                        return;
                        PhotoViewer.this.closePhoto(true, false);
                        return;
                        if (paramAnonymousInt == 1)
                        {
                          if ((Build.VERSION.SDK_INT >= 23) && (PhotoViewer.this.parentActivity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0))
                          {
                            PhotoViewer.this.parentActivity.requestPermissions(new String[] { "android.permission.WRITE_EXTERNAL_STORAGE" }, 4);
                            return;
                          }
                          localObject1 = null;
                          if (PhotoViewer.this.currentMessageObject != null) {
                            if (((PhotoViewer.this.currentMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && (PhotoViewer.this.currentMessageObject.messageOwner.media.webpage != null) && (PhotoViewer.this.currentMessageObject.messageOwner.media.webpage.document == null))
                            {
                              localObject1 = FileLoader.getPathToAttach(PhotoViewer.this.getFileLocation(PhotoViewer.this.currentIndex, null), true);
                              if ((localObject1 == null) || (!((File)localObject1).exists())) {
                                break label348;
                              }
                              localObject1 = ((File)localObject1).toString();
                              localObject2 = PhotoViewer.this.parentActivity;
                              if ((PhotoViewer.this.currentMessageObject == null) || (!PhotoViewer.this.currentMessageObject.isVideo())) {
                                break label343;
                              }
                            }
                          }
                          for (paramAnonymousInt = 1;; paramAnonymousInt = 0)
                          {
                            MediaController.saveFile((String)localObject1, (Context)localObject2, paramAnonymousInt, null, null);
                            return;
                            localObject1 = FileLoader.getPathToMessage(PhotoViewer.this.currentMessageObject.messageOwner);
                            break;
                            if (PhotoViewer.this.currentFileLocation == null) {
                              break;
                            }
                            localObject1 = PhotoViewer.this.currentFileLocation;
                            if ((PhotoViewer.this.avatarsDialogId != 0) || (PhotoViewer.this.isEvent)) {}
                            for (bool = true;; bool = false)
                            {
                              localObject1 = FileLoader.getPathToAttach((TLObject)localObject1, bool);
                              break;
                            }
                          }
                          PhotoViewer.this.showDownloadAlert();
                          return;
                        }
                        if (paramAnonymousInt != 2) {
                          break;
                        }
                      } while (PhotoViewer.this.currentDialogId == 0L);
                      PhotoViewer.access$8702(PhotoViewer.this, true);
                      localObject1 = new Bundle();
                      ((Bundle)localObject1).putLong("dialog_id", PhotoViewer.this.currentDialogId);
                      localObject1 = new MediaActivity((Bundle)localObject1);
                      if (PhotoViewer.this.parentChatActivity != null) {
                        ((MediaActivity)localObject1).setChatInfo(PhotoViewer.this.parentChatActivity.getCurrentChatInfo());
                      }
                      PhotoViewer.this.closePhoto(false, false);
                      ((LaunchActivity)PhotoViewer.this.parentActivity).presentFragment((BaseFragment)localObject1, false, true);
                      return;
                      if (paramAnonymousInt != 4) {
                        break;
                      }
                    } while (PhotoViewer.this.currentMessageObject == null);
                    localObject1 = new Bundle();
                    i = (int)PhotoViewer.this.currentDialogId;
                    paramAnonymousInt = (int)(PhotoViewer.this.currentDialogId >> 32);
                    if (i != 0) {
                      if (paramAnonymousInt == 1)
                      {
                        ((Bundle)localObject1).putInt("chat_id", i);
                        ((Bundle)localObject1).putInt("message_id", PhotoViewer.this.currentMessageObject.getId());
                        NotificationCenter.getInstance(PhotoViewer.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        localObject2 = (LaunchActivity)PhotoViewer.this.parentActivity;
                        if ((((LaunchActivity)localObject2).getMainFragmentsCount() <= 1) && (!AndroidUtilities.isTablet())) {
                          break label732;
                        }
                      }
                    }
                    for (boolean bool = true;; bool = false)
                    {
                      ((LaunchActivity)localObject2).presentFragment(new ChatActivity((Bundle)localObject1), bool, true);
                      PhotoViewer.access$8002(PhotoViewer.this, null);
                      PhotoViewer.this.closePhoto(false, false);
                      return;
                      if (i > 0)
                      {
                        ((Bundle)localObject1).putInt("user_id", i);
                        break;
                      }
                      if (i >= 0) {
                        break;
                      }
                      localObject2 = MessagesController.getInstance(PhotoViewer.this.currentAccount).getChat(Integer.valueOf(-i));
                      paramAnonymousInt = i;
                      if (localObject2 != null)
                      {
                        paramAnonymousInt = i;
                        if (((TLRPC.Chat)localObject2).migrated_to != null)
                        {
                          ((Bundle)localObject1).putInt("migrated_to", i);
                          paramAnonymousInt = -((TLRPC.Chat)localObject2).migrated_to.channel_id;
                        }
                      }
                      ((Bundle)localObject1).putInt("chat_id", -paramAnonymousInt);
                      break;
                      ((Bundle)localObject1).putInt("enc_id", paramAnonymousInt);
                      break;
                    }
                    if (paramAnonymousInt != 3) {
                      break;
                    }
                  } while ((PhotoViewer.this.currentMessageObject == null) || (PhotoViewer.this.parentActivity == null));
                  ((LaunchActivity)PhotoViewer.this.parentActivity).switchToAccount(PhotoViewer.this.currentMessageObject.currentAccount, true);
                  localObject1 = new Bundle();
                  ((Bundle)localObject1).putBoolean("onlySelect", true);
                  ((Bundle)localObject1).putInt("dialogsType", 3);
                  localObject1 = new DialogsActivity((Bundle)localObject1);
                  localObject2 = new ArrayList();
                  ((ArrayList)localObject2).add(PhotoViewer.this.currentMessageObject);
                  ((DialogsActivity)localObject1).setDelegate(new DialogsActivity.DialogsActivityDelegate()
                  {
                    public void didSelectDialogs(DialogsActivity paramAnonymous2DialogsActivity, ArrayList<Long> paramAnonymous2ArrayList, CharSequence paramAnonymous2CharSequence, boolean paramAnonymous2Boolean)
                    {
                      if ((paramAnonymous2ArrayList.size() > 1) || (((Long)paramAnonymous2ArrayList.get(0)).longValue() == UserConfig.getInstance(PhotoViewer.this.currentAccount).getClientUserId()) || (paramAnonymous2CharSequence != null))
                      {
                        i = 0;
                        while (i < paramAnonymous2ArrayList.size())
                        {
                          l = ((Long)paramAnonymous2ArrayList.get(i)).longValue();
                          if (paramAnonymous2CharSequence != null) {
                            SendMessagesHelper.getInstance(PhotoViewer.this.currentAccount).sendMessage(paramAnonymous2CharSequence.toString(), l, null, null, true, null, null, null);
                          }
                          SendMessagesHelper.getInstance(PhotoViewer.this.currentAccount).sendMessage(localObject2, l);
                          i += 1;
                        }
                        paramAnonymous2DialogsActivity.finishFragment();
                        return;
                      }
                      long l = ((Long)paramAnonymous2ArrayList.get(0)).longValue();
                      int i = (int)l;
                      int j = (int)(l >> 32);
                      paramAnonymous2ArrayList = new Bundle();
                      paramAnonymous2ArrayList.putBoolean("scrollToTopOnResume", true);
                      if (i != 0) {
                        if (i > 0) {
                          paramAnonymous2ArrayList.putInt("user_id", i);
                        }
                      }
                      for (;;)
                      {
                        NotificationCenter.getInstance(PhotoViewer.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        paramAnonymous2ArrayList = new ChatActivity(paramAnonymous2ArrayList);
                        if (!((LaunchActivity)PhotoViewer.this.parentActivity).presentFragment(paramAnonymous2ArrayList, true, false)) {
                          break;
                        }
                        paramAnonymous2ArrayList.showReplyPanel(true, null, localObject2, null, false);
                        return;
                        if (i < 0)
                        {
                          paramAnonymous2ArrayList.putInt("chat_id", -i);
                          continue;
                          paramAnonymous2ArrayList.putInt("enc_id", j);
                        }
                      }
                      paramAnonymous2DialogsActivity.finishFragment();
                    }
                  });
                  ((LaunchActivity)PhotoViewer.this.parentActivity).presentFragment((BaseFragment)localObject1, false, true);
                  PhotoViewer.this.closePhoto(false, false);
                  return;
                  if (paramAnonymousInt != 6) {
                    break;
                  }
                } while (PhotoViewer.this.parentActivity == null);
                AlertDialog.Builder localBuilder = new AlertDialog.Builder(PhotoViewer.this.parentActivity);
                final boolean[] arrayOfBoolean;
                FrameLayout localFrameLayout;
                CheckBoxCell localCheckBoxCell;
                if ((PhotoViewer.this.currentMessageObject != null) && (PhotoViewer.this.currentMessageObject.isVideo()))
                {
                  localBuilder.setMessage(LocaleController.formatString("AreYouSureDeleteVideo", 2131493008, new Object[0]));
                  localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
                  arrayOfBoolean = new boolean[1];
                  if (PhotoViewer.this.currentMessageObject != null)
                  {
                    paramAnonymousInt = (int)PhotoViewer.this.currentMessageObject.getDialogId();
                    if (paramAnonymousInt != 0)
                    {
                      if (paramAnonymousInt <= 0) {
                        break label1424;
                      }
                      localObject2 = MessagesController.getInstance(PhotoViewer.this.currentAccount).getUser(Integer.valueOf(paramAnonymousInt));
                      localObject1 = null;
                      if ((localObject2 != null) || (!ChatObject.isChannel((TLRPC.Chat)localObject1)))
                      {
                        paramAnonymousInt = ConnectionsManager.getInstance(PhotoViewer.this.currentAccount).getCurrentTime();
                        if (((localObject2 != null) && (((TLRPC.User)localObject2).id != UserConfig.getInstance(PhotoViewer.this.currentAccount).getClientUserId())) || ((localObject1 != null) && ((PhotoViewer.this.currentMessageObject.messageOwner.action == null) || ((PhotoViewer.this.currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionEmpty))) && (PhotoViewer.this.currentMessageObject.isOut()) && (paramAnonymousInt - PhotoViewer.this.currentMessageObject.messageOwner.date <= 172800)))
                        {
                          localFrameLayout = new FrameLayout(PhotoViewer.this.parentActivity);
                          localCheckBoxCell = new CheckBoxCell(PhotoViewer.this.parentActivity, 1);
                          localCheckBoxCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                          if (localObject1 == null) {
                            break label1450;
                          }
                          localCheckBoxCell.setText(LocaleController.getString("DeleteForAll", 2131493367), "", false, false);
                          if (!LocaleController.isRTL) {
                            break label1484;
                          }
                          paramAnonymousInt = AndroidUtilities.dp(16.0F);
                          if (!LocaleController.isRTL) {
                            break label1494;
                          }
                        }
                      }
                    }
                  }
                }
                for (int i = AndroidUtilities.dp(8.0F);; i = AndroidUtilities.dp(16.0F))
                {
                  localCheckBoxCell.setPadding(paramAnonymousInt, 0, i, 0);
                  localFrameLayout.addView(localCheckBoxCell, LayoutHelper.createFrame(-1, 48.0F, 51, 0.0F, 0.0F, 0.0F, 0.0F));
                  localCheckBoxCell.setOnClickListener(new View.OnClickListener()
                  {
                    public void onClick(View paramAnonymous2View)
                    {
                      paramAnonymous2View = (CheckBoxCell)paramAnonymous2View;
                      boolean[] arrayOfBoolean = arrayOfBoolean;
                      if (arrayOfBoolean[0] == 0) {}
                      for (int i = 1;; i = 0)
                      {
                        arrayOfBoolean[0] = i;
                        paramAnonymous2View.setChecked(arrayOfBoolean[0], true);
                        return;
                      }
                    }
                  });
                  localBuilder.setView(localFrameLayout);
                  localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                  {
                    public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                    {
                      if (!PhotoViewer.this.imagesArr.isEmpty()) {
                        if ((PhotoViewer.this.currentIndex >= 0) && (PhotoViewer.this.currentIndex < PhotoViewer.this.imagesArr.size())) {}
                      }
                      label561:
                      do
                      {
                        do
                        {
                          MessageObject localMessageObject;
                          do
                          {
                            return;
                            localMessageObject = (MessageObject)PhotoViewer.this.imagesArr.get(PhotoViewer.this.currentIndex);
                          } while (!localMessageObject.isSent());
                          PhotoViewer.this.closePhoto(false, false);
                          ArrayList localArrayList = new ArrayList();
                          if (PhotoViewer.this.slideshowMessageId != 0) {
                            localArrayList.add(Integer.valueOf(PhotoViewer.this.slideshowMessageId));
                          }
                          for (;;)
                          {
                            localObject2 = null;
                            Object localObject3 = null;
                            localObject1 = localObject2;
                            paramAnonymous2DialogInterface = (DialogInterface)localObject3;
                            if ((int)localMessageObject.getDialogId() == 0)
                            {
                              localObject1 = localObject2;
                              paramAnonymous2DialogInterface = (DialogInterface)localObject3;
                              if (localMessageObject.messageOwner.random_id != 0L)
                              {
                                localObject1 = new ArrayList();
                                ((ArrayList)localObject1).add(Long.valueOf(localMessageObject.messageOwner.random_id));
                                paramAnonymous2DialogInterface = MessagesController.getInstance(PhotoViewer.this.currentAccount).getEncryptedChat(Integer.valueOf((int)(localMessageObject.getDialogId() >> 32)));
                              }
                            }
                            MessagesController.getInstance(PhotoViewer.this.currentAccount).deleteMessages(localArrayList, (ArrayList)localObject1, paramAnonymous2DialogInterface, localMessageObject.messageOwner.to_id.channel_id, arrayOfBoolean[0]);
                            return;
                            localArrayList.add(Integer.valueOf(localMessageObject.getId()));
                          }
                        } while ((PhotoViewer.this.avatarsArr.isEmpty()) || (PhotoViewer.this.currentIndex < 0) || (PhotoViewer.this.currentIndex >= PhotoViewer.this.avatarsArr.size()));
                        localObject1 = (TLRPC.Photo)PhotoViewer.this.avatarsArr.get(PhotoViewer.this.currentIndex);
                        Object localObject2 = (TLRPC.FileLocation)PhotoViewer.this.imagesArrLocations.get(PhotoViewer.this.currentIndex);
                        paramAnonymous2DialogInterface = (DialogInterface)localObject1;
                        if ((localObject1 instanceof TLRPC.TL_photoEmpty)) {
                          paramAnonymous2DialogInterface = null;
                        }
                        i = 0;
                        paramAnonymous2Int = i;
                        if (PhotoViewer.this.currentUserAvatarLocation != null)
                        {
                          if (paramAnonymous2DialogInterface == null) {
                            break label561;
                          }
                          localObject1 = paramAnonymous2DialogInterface.sizes.iterator();
                          do
                          {
                            paramAnonymous2Int = i;
                            if (!((Iterator)localObject1).hasNext()) {
                              break;
                            }
                            localObject2 = (TLRPC.PhotoSize)((Iterator)localObject1).next();
                          } while ((((TLRPC.PhotoSize)localObject2).location.local_id != PhotoViewer.this.currentUserAvatarLocation.local_id) || (((TLRPC.PhotoSize)localObject2).location.volume_id != PhotoViewer.this.currentUserAvatarLocation.volume_id));
                          paramAnonymous2Int = 1;
                        }
                        while (paramAnonymous2Int != 0)
                        {
                          MessagesController.getInstance(PhotoViewer.this.currentAccount).deleteUserPhoto(null);
                          PhotoViewer.this.closePhoto(false, false);
                          return;
                          paramAnonymous2Int = i;
                          if (((TLRPC.FileLocation)localObject2).local_id == PhotoViewer.this.currentUserAvatarLocation.local_id)
                          {
                            paramAnonymous2Int = i;
                            if (((TLRPC.FileLocation)localObject2).volume_id == PhotoViewer.this.currentUserAvatarLocation.volume_id) {
                              paramAnonymous2Int = 1;
                            }
                          }
                        }
                      } while (paramAnonymous2DialogInterface == null);
                      Object localObject1 = new TLRPC.TL_inputPhoto();
                      ((TLRPC.TL_inputPhoto)localObject1).id = paramAnonymous2DialogInterface.id;
                      ((TLRPC.TL_inputPhoto)localObject1).access_hash = paramAnonymous2DialogInterface.access_hash;
                      MessagesController.getInstance(PhotoViewer.this.currentAccount).deleteUserPhoto((TLRPC.InputPhoto)localObject1);
                      MessagesStorage.getInstance(PhotoViewer.this.currentAccount).clearUserPhoto(PhotoViewer.this.avatarsDialogId, paramAnonymous2DialogInterface.id);
                      PhotoViewer.this.imagesArrLocations.remove(PhotoViewer.this.currentIndex);
                      PhotoViewer.this.imagesArrLocationsSizes.remove(PhotoViewer.this.currentIndex);
                      PhotoViewer.this.avatarsArr.remove(PhotoViewer.this.currentIndex);
                      if (PhotoViewer.this.imagesArrLocations.isEmpty())
                      {
                        PhotoViewer.this.closePhoto(false, false);
                        return;
                      }
                      int i = PhotoViewer.this.currentIndex;
                      paramAnonymous2Int = i;
                      if (i >= PhotoViewer.this.avatarsArr.size()) {
                        paramAnonymous2Int = PhotoViewer.this.avatarsArr.size() - 1;
                      }
                      PhotoViewer.access$3002(PhotoViewer.this, -1);
                      PhotoViewer.this.setImageIndex(paramAnonymous2Int, true);
                    }
                  });
                  localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                  PhotoViewer.this.showAlertDialog(localBuilder);
                  return;
                  if ((PhotoViewer.this.currentMessageObject != null) && (PhotoViewer.this.currentMessageObject.isGif()))
                  {
                    localBuilder.setMessage(LocaleController.formatString("AreYouSure", 2131492998, new Object[0]));
                    break;
                  }
                  localBuilder.setMessage(LocaleController.formatString("AreYouSureDeletePhoto", 2131493006, new Object[0]));
                  break;
                  localObject2 = null;
                  localObject1 = MessagesController.getInstance(PhotoViewer.this.currentAccount).getChat(Integer.valueOf(-paramAnonymousInt));
                  break label1035;
                  localCheckBoxCell.setText(LocaleController.formatString("DeleteForUser", 2131493368, new Object[] { UserObject.getFirstName((TLRPC.User)localObject2) }), "", false, false);
                  break label1228;
                  paramAnonymousInt = AndroidUtilities.dp(8.0F);
                  break label1241;
                }
                if (paramAnonymousInt == 10)
                {
                  PhotoViewer.this.onSharePressed();
                  return;
                }
                if (paramAnonymousInt == 11) {
                  try
                  {
                    AndroidUtilities.openForView(PhotoViewer.this.currentMessageObject, PhotoViewer.this.parentActivity);
                    PhotoViewer.this.closePhoto(false, false);
                    return;
                  }
                  catch (Exception localException)
                  {
                    FileLog.e(localException);
                    return;
                  }
                }
                if (paramAnonymousInt != 13) {
                  break;
                }
              } while ((PhotoViewer.this.parentActivity == null) || (PhotoViewer.this.currentMessageObject == null) || (PhotoViewer.this.currentMessageObject.messageOwner.media == null) || (PhotoViewer.this.currentMessageObject.messageOwner.media.photo == null));
              new StickersAlert(PhotoViewer.this.parentActivity, PhotoViewer.this.currentMessageObject.messageOwner.media.photo).show();
              return;
              if (paramAnonymousInt != 5) {
                break;
              }
            } while (PhotoViewer.this.pipItem.getAlpha() != 1.0F);
            PhotoViewer.this.switchToPip();
            return;
          } while ((paramAnonymousInt != 7) || (PhotoViewer.this.currentMessageObject == null));
          FileLoader.getInstance(PhotoViewer.this.currentAccount).cancelLoadFile(PhotoViewer.this.currentMessageObject.getDocument());
          PhotoViewer.this.releasePlayer();
          PhotoViewer.this.bottomLayout.setTag(Integer.valueOf(1));
          PhotoViewer.this.bottomLayout.setVisibility(0);
        }
      });
      paramActivity = this.actionBar.createMenu();
      this.masksItem = paramActivity.addItem(13, 2131165387);
      this.pipItem = paramActivity.addItem(5, 2131165382);
      this.sendItem = paramActivity.addItem(3, 2131165522);
      this.menuItem = paramActivity.addItem(0, 2131165353);
      this.menuItem.addSubItem(11, LocaleController.getString("OpenInExternalApp", 2131494044)).setTextColor(-328966);
      this.menuItem.addSubItem(2, LocaleController.getString("ShowAllMedia", 2131494403)).setTextColor(-328966);
      this.menuItem.addSubItem(4, LocaleController.getString("ShowInChat", 2131494404)).setTextColor(-328966);
      this.menuItem.addSubItem(10, LocaleController.getString("ShareFile", 2131494383)).setTextColor(-328966);
      this.menuItem.addSubItem(1, LocaleController.getString("SaveToGallery", 2131494290)).setTextColor(-328966);
      this.menuItem.addSubItem(6, LocaleController.getString("Delete", 2131493356)).setTextColor(-328966);
      this.menuItem.addSubItem(7, LocaleController.getString("StopDownload", 2131494437)).setTextColor(-328966);
      this.menuItem.redrawPopup(-115203550);
      this.bottomLayout = new FrameLayout(this.actvityContext);
      this.bottomLayout.setBackgroundColor(2130706432);
      this.containerView.addView(this.bottomLayout, LayoutHelper.createFrame(-1, 48, 83));
      this.groupedPhotosListView = new GroupedPhotosListView(this.actvityContext);
      this.containerView.addView(this.groupedPhotosListView, LayoutHelper.createFrame(-1, 62.0F, 83, 0.0F, 0.0F, 0.0F, 48.0F));
      this.captionTextView = createCaptionTextView();
      this.switchCaptionTextView = createCaptionTextView();
      i = 0;
      while (i < 3)
      {
        this.photoProgressViews[i] = new PhotoProgressView(this.containerView.getContext(), this.containerView);
        this.photoProgressViews[i].setBackgroundState(0, false);
        i += 1;
      }
      this.windowLayoutParams.flags = 8;
      break;
    }
    this.miniProgressView = new RadialProgressView(this.actvityContext)
    {
      public void invalidate()
      {
        super.invalidate();
        if (PhotoViewer.this.containerView != null) {
          PhotoViewer.this.containerView.invalidate();
        }
      }
      
      public void setAlpha(float paramAnonymousFloat)
      {
        super.setAlpha(paramAnonymousFloat);
        if (PhotoViewer.this.containerView != null) {
          PhotoViewer.this.containerView.invalidate();
        }
      }
    };
    this.miniProgressView.setUseSelfAlpha(true);
    this.miniProgressView.setProgressColor(-1);
    this.miniProgressView.setSize(AndroidUtilities.dp(54.0F));
    this.miniProgressView.setBackgroundResource(2131165275);
    this.miniProgressView.setVisibility(4);
    this.miniProgressView.setAlpha(0.0F);
    this.containerView.addView(this.miniProgressView, LayoutHelper.createFrame(64, 64, 17));
    this.shareButton = new ImageView(this.containerView.getContext());
    this.shareButton.setImageResource(2131165637);
    this.shareButton.setScaleType(ImageView.ScaleType.CENTER);
    this.shareButton.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.bottomLayout.addView(this.shareButton, LayoutHelper.createFrame(50, -1, 53));
    this.shareButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoViewer.this.onSharePressed();
      }
    });
    this.nameTextView = new TextView(this.containerView.getContext());
    this.nameTextView.setTextSize(1, 14.0F);
    this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.nameTextView.setSingleLine(true);
    this.nameTextView.setMaxLines(1);
    this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
    this.nameTextView.setTextColor(-1);
    this.nameTextView.setGravity(3);
    this.bottomLayout.addView(this.nameTextView, LayoutHelper.createFrame(-1, -2.0F, 51, 16.0F, 5.0F, 60.0F, 0.0F));
    this.dateTextView = new TextView(this.containerView.getContext());
    this.dateTextView.setTextSize(1, 13.0F);
    this.dateTextView.setSingleLine(true);
    this.dateTextView.setMaxLines(1);
    this.dateTextView.setEllipsize(TextUtils.TruncateAt.END);
    this.dateTextView.setTextColor(-1);
    this.dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.dateTextView.setGravity(3);
    this.bottomLayout.addView(this.dateTextView, LayoutHelper.createFrame(-1, -2.0F, 51, 16.0F, 25.0F, 50.0F, 0.0F));
    createVideoControlsInterface();
    this.progressView = new RadialProgressView(this.parentActivity);
    this.progressView.setProgressColor(-1);
    this.progressView.setBackgroundResource(2131165275);
    this.progressView.setVisibility(4);
    this.containerView.addView(this.progressView, LayoutHelper.createFrame(54, 54, 17));
    this.qualityPicker = new PickerBottomLayoutViewer(this.parentActivity);
    this.qualityPicker.setBackgroundColor(2130706432);
    this.qualityPicker.updateSelectedCount(0, false);
    this.qualityPicker.setTranslationY(AndroidUtilities.dp(120.0F));
    this.qualityPicker.doneButton.setText(LocaleController.getString("Done", 2131493395).toUpperCase());
    this.containerView.addView(this.qualityPicker, LayoutHelper.createFrame(-1, 48, 83));
    this.qualityPicker.cancelButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoViewer.access$9602(PhotoViewer.this, PhotoViewer.this.previousCompression);
        PhotoViewer.this.didChangedCompressionLevel(false);
        PhotoViewer.this.showQualityView(false);
        PhotoViewer.this.requestVideoPreview(2);
      }
    });
    this.qualityPicker.doneButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoViewer.this.showQualityView(false);
        PhotoViewer.this.requestVideoPreview(2);
      }
    });
    this.qualityChooseView = new QualityChooseView(this.parentActivity);
    this.qualityChooseView.setTranslationY(AndroidUtilities.dp(120.0F));
    this.qualityChooseView.setVisibility(4);
    this.qualityChooseView.setBackgroundColor(2130706432);
    this.containerView.addView(this.qualityChooseView, LayoutHelper.createFrame(-1, 70.0F, 83, 0.0F, 0.0F, 0.0F, 48.0F));
    this.pickerView = new FrameLayout(this.actvityContext)
    {
      public boolean dispatchTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return (PhotoViewer.this.bottomTouchEnabled) && (super.dispatchTouchEvent(paramAnonymousMotionEvent));
      }
      
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return (PhotoViewer.this.bottomTouchEnabled) && (super.onInterceptTouchEvent(paramAnonymousMotionEvent));
      }
      
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return (PhotoViewer.this.bottomTouchEnabled) && (super.onTouchEvent(paramAnonymousMotionEvent));
      }
    };
    this.pickerView.setBackgroundColor(2130706432);
    this.containerView.addView(this.pickerView, LayoutHelper.createFrame(-1, -2, 83));
    this.videoTimelineView = new VideoTimelinePlayView(this.parentActivity);
    this.videoTimelineView.setDelegate(new VideoTimelinePlayView.VideoTimelineViewDelegate()
    {
      public void didStartDragging() {}
      
      public void didStopDragging() {}
      
      public void onLeftProgressChanged(float paramAnonymousFloat)
      {
        if (PhotoViewer.this.videoPlayer == null) {
          return;
        }
        if (PhotoViewer.this.videoPlayer.isPlaying())
        {
          PhotoViewer.this.videoPlayer.pause();
          PhotoViewer.this.containerView.invalidate();
        }
        PhotoViewer.this.videoPlayer.seekTo((int)(PhotoViewer.this.videoDuration * paramAnonymousFloat));
        PhotoViewer.this.videoPlayerSeekbar.setProgress(0.0F);
        PhotoViewer.this.videoTimelineView.setProgress(0.0F);
        PhotoViewer.this.updateVideoInfo();
      }
      
      public void onPlayProgressChanged(float paramAnonymousFloat)
      {
        if (PhotoViewer.this.videoPlayer == null) {
          return;
        }
        PhotoViewer.this.videoPlayer.seekTo((int)(PhotoViewer.this.videoDuration * paramAnonymousFloat));
      }
      
      public void onRightProgressChanged(float paramAnonymousFloat)
      {
        if (PhotoViewer.this.videoPlayer == null) {
          return;
        }
        if (PhotoViewer.this.videoPlayer.isPlaying())
        {
          PhotoViewer.this.videoPlayer.pause();
          PhotoViewer.this.containerView.invalidate();
        }
        PhotoViewer.this.videoPlayer.seekTo((int)(PhotoViewer.this.videoDuration * paramAnonymousFloat));
        PhotoViewer.this.videoPlayerSeekbar.setProgress(0.0F);
        PhotoViewer.this.videoTimelineView.setProgress(0.0F);
        PhotoViewer.this.updateVideoInfo();
      }
    });
    this.pickerView.addView(this.videoTimelineView, LayoutHelper.createFrame(-1, 58.0F, 51, 0.0F, 8.0F, 0.0F, 88.0F));
    this.pickerViewSendButton = new ImageView(this.parentActivity);
    this.pickerViewSendButton.setScaleType(ImageView.ScaleType.CENTER);
    paramActivity = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0F), -10043398, -10043398);
    this.pickerViewSendButton.setBackgroundDrawable(paramActivity);
    this.pickerViewSendButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chats_actionIcon"), PorterDuff.Mode.MULTIPLY));
    this.pickerViewSendButton.setPadding(AndroidUtilities.dp(4.0F), 0, 0, 0);
    this.pickerViewSendButton.setImageResource(2131165407);
    this.containerView.addView(this.pickerViewSendButton, LayoutHelper.createFrame(56, 56.0F, 85, 0.0F, 0.0F, 14.0F, 14.0F));
    this.pickerViewSendButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (PhotoViewer.this.captionEditText.getTag() != null) {}
        while ((PhotoViewer.this.placeProvider == null) || (PhotoViewer.this.doneButtonPressed)) {
          return;
        }
        paramAnonymousView = PhotoViewer.this.getCurrentVideoEditedInfo();
        PhotoViewer.this.placeProvider.sendButtonPressed(PhotoViewer.this.currentIndex, paramAnonymousView);
        PhotoViewer.access$10402(PhotoViewer.this, true);
        PhotoViewer.this.closePhoto(false, false);
      }
    });
    this.itemsLayout = new LinearLayout(this.parentActivity);
    this.itemsLayout.setOrientation(0);
    this.pickerView.addView(this.itemsLayout, LayoutHelper.createFrame(-2, 48.0F, 81, 0.0F, 0.0F, 34.0F, 0.0F));
    this.cropItem = new ImageView(this.parentActivity);
    this.cropItem.setScaleType(ImageView.ScaleType.CENTER);
    this.cropItem.setImageResource(2131165578);
    this.cropItem.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.itemsLayout.addView(this.cropItem, LayoutHelper.createLinear(70, 48));
    this.cropItem.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (PhotoViewer.this.captionEditText.getTag() != null) {
          return;
        }
        PhotoViewer.this.switchToEditMode(1);
      }
    });
    this.paintItem = new ImageView(this.parentActivity);
    this.paintItem.setScaleType(ImageView.ScaleType.CENTER);
    this.paintItem.setImageResource(2131165581);
    this.paintItem.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.itemsLayout.addView(this.paintItem, LayoutHelper.createLinear(70, 48));
    this.paintItem.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (PhotoViewer.this.captionEditText.getTag() != null) {
          return;
        }
        PhotoViewer.this.switchToEditMode(3);
      }
    });
    this.compressItem = new ImageView(this.parentActivity);
    this.compressItem.setTag(Integer.valueOf(1));
    this.compressItem.setScaleType(ImageView.ScaleType.CENTER);
    this.compressItem.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.selectedCompression = MessagesController.getGlobalMainSettings().getInt("compress_video2", 1);
    Object localObject;
    if (this.selectedCompression <= 0)
    {
      this.compressItem.setImageResource(2131165688);
      this.itemsLayout.addView(this.compressItem, LayoutHelper.createLinear(70, 48));
      this.compressItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (PhotoViewer.this.captionEditText.getTag() != null) {
            return;
          }
          PhotoViewer.this.showQualityView(true);
          PhotoViewer.this.requestVideoPreview(1);
        }
      });
      this.muteItem = new ImageView(this.parentActivity);
      this.muteItem.setScaleType(ImageView.ScaleType.CENTER);
      this.muteItem.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
      this.itemsLayout.addView(this.muteItem, LayoutHelper.createLinear(70, 48));
      this.muteItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (PhotoViewer.this.captionEditText.getTag() != null) {
            return;
          }
          paramAnonymousView = PhotoViewer.this;
          boolean bool;
          if (!PhotoViewer.this.muteVideo)
          {
            bool = true;
            PhotoViewer.access$10702(paramAnonymousView, bool);
            if ((!PhotoViewer.this.muteVideo) || (PhotoViewer.this.checkImageView.isChecked())) {
              break label84;
            }
            PhotoViewer.this.checkImageView.callOnClick();
          }
          for (;;)
          {
            PhotoViewer.this.updateMuteButton();
            return;
            bool = false;
            break;
            label84:
            paramAnonymousView = PhotoViewer.this.imagesArrLocals.get(PhotoViewer.this.currentIndex);
            if ((paramAnonymousView instanceof MediaController.PhotoEntry)) {
              ((MediaController.PhotoEntry)paramAnonymousView).editedInfo = PhotoViewer.this.getCurrentVideoEditedInfo();
            }
          }
        }
      });
      this.cameraItem = new ImageView(this.parentActivity);
      this.cameraItem.setScaleType(ImageView.ScaleType.CENTER);
      this.cameraItem.setImageResource(2131165577);
      this.cameraItem.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
      this.containerView.addView(this.cameraItem, LayoutHelper.createFrame(48, 48.0F, 85, 0.0F, 0.0F, 16.0F, 0.0F));
      this.cameraItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if ((PhotoViewer.this.placeProvider == null) || (PhotoViewer.this.captionEditText.getTag() != null)) {
            return;
          }
          PhotoViewer.this.placeProvider.needAddMorePhotos();
          PhotoViewer.this.closePhoto(true, false);
        }
      });
      this.tuneItem = new ImageView(this.parentActivity);
      this.tuneItem.setScaleType(ImageView.ScaleType.CENTER);
      this.tuneItem.setImageResource(2131165587);
      this.tuneItem.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
      this.itemsLayout.addView(this.tuneItem, LayoutHelper.createLinear(70, 48));
      this.tuneItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (PhotoViewer.this.captionEditText.getTag() != null) {
            return;
          }
          PhotoViewer.this.switchToEditMode(2);
        }
      });
      this.timeItem = new ImageView(this.parentActivity);
      this.timeItem.setScaleType(ImageView.ScaleType.CENTER);
      this.timeItem.setImageResource(2131165586);
      this.timeItem.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
      this.itemsLayout.addView(this.timeItem, LayoutHelper.createLinear(70, 48));
      this.timeItem.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(final View paramAnonymousView)
        {
          if ((PhotoViewer.this.parentActivity == null) || (PhotoViewer.this.captionEditText.getTag() != null)) {
            return;
          }
          final Object localObject2 = new BottomSheet.Builder(PhotoViewer.this.parentActivity);
          ((BottomSheet.Builder)localObject2).setUseHardwareLayer(false);
          Object localObject1 = new LinearLayout(PhotoViewer.this.parentActivity);
          ((LinearLayout)localObject1).setOrientation(1);
          ((BottomSheet.Builder)localObject2).setCustomView((View)localObject1);
          paramAnonymousView = new TextView(PhotoViewer.this.parentActivity);
          paramAnonymousView.setLines(1);
          paramAnonymousView.setSingleLine(true);
          paramAnonymousView.setText(LocaleController.getString("MessageLifetime", 2131493818));
          paramAnonymousView.setTextColor(-1);
          paramAnonymousView.setTextSize(1, 16.0F);
          paramAnonymousView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
          paramAnonymousView.setPadding(AndroidUtilities.dp(21.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(21.0F), AndroidUtilities.dp(4.0F));
          paramAnonymousView.setGravity(16);
          ((LinearLayout)localObject1).addView(paramAnonymousView, LayoutHelper.createFrame(-1, -2.0F));
          paramAnonymousView.setOnTouchListener(new View.OnTouchListener()
          {
            public boolean onTouch(View paramAnonymous2View, MotionEvent paramAnonymous2MotionEvent)
            {
              return true;
            }
          });
          Object localObject3 = new TextView(PhotoViewer.this.parentActivity);
          int i;
          if (PhotoViewer.this.isCurrentVideo)
          {
            paramAnonymousView = LocaleController.getString("MessageLifetimeVideo", 2131493823);
            ((TextView)localObject3).setText(paramAnonymousView);
            ((TextView)localObject3).setTextColor(-8355712);
            ((TextView)localObject3).setTextSize(1, 14.0F);
            ((TextView)localObject3).setEllipsize(TextUtils.TruncateAt.MIDDLE);
            ((TextView)localObject3).setPadding(AndroidUtilities.dp(21.0F), 0, AndroidUtilities.dp(21.0F), AndroidUtilities.dp(8.0F));
            ((TextView)localObject3).setGravity(16);
            ((LinearLayout)localObject1).addView((View)localObject3, LayoutHelper.createFrame(-1, -2.0F));
            ((TextView)localObject3).setOnTouchListener(new View.OnTouchListener()
            {
              public boolean onTouch(View paramAnonymous2View, MotionEvent paramAnonymous2MotionEvent)
              {
                return true;
              }
            });
            paramAnonymousView = ((BottomSheet.Builder)localObject2).create();
            localObject2 = new NumberPicker(PhotoViewer.this.parentActivity);
            ((NumberPicker)localObject2).setMinValue(0);
            ((NumberPicker)localObject2).setMaxValue(28);
            localObject3 = PhotoViewer.this.imagesArrLocals.get(PhotoViewer.this.currentIndex);
            if (!(localObject3 instanceof MediaController.PhotoEntry)) {
              break label766;
            }
            i = ((MediaController.PhotoEntry)localObject3).ttl;
            label369:
            if (i != 0) {
              break label791;
            }
            ((NumberPicker)localObject2).setValue(MessagesController.getGlobalMainSettings().getInt("self_destruct", 7));
          }
          for (;;)
          {
            ((NumberPicker)localObject2).setTextColor(-1);
            ((NumberPicker)localObject2).setSelectorColor(-11711155);
            ((NumberPicker)localObject2).setFormatter(new NumberPicker.Formatter()
            {
              public String format(int paramAnonymous2Int)
              {
                if (paramAnonymous2Int == 0) {
                  return LocaleController.getString("ShortMessageLifetimeForever", 2131494402);
                }
                if ((paramAnonymous2Int >= 1) && (paramAnonymous2Int < 21)) {
                  return LocaleController.formatTTLString(paramAnonymous2Int);
                }
                return LocaleController.formatTTLString((paramAnonymous2Int - 16) * 5);
              }
            });
            ((LinearLayout)localObject1).addView((View)localObject2, LayoutHelper.createLinear(-1, -2));
            localObject3 = new FrameLayout(PhotoViewer.this.parentActivity)
            {
              protected void onLayout(boolean paramAnonymous2Boolean, int paramAnonymous2Int1, int paramAnonymous2Int2, int paramAnonymous2Int3, int paramAnonymous2Int4)
              {
                paramAnonymous2Int4 = getChildCount();
                Object localObject = null;
                int i = paramAnonymous2Int3 - paramAnonymous2Int1;
                paramAnonymous2Int1 = 0;
                if (paramAnonymous2Int1 < paramAnonymous2Int4)
                {
                  View localView = getChildAt(paramAnonymous2Int1);
                  if (((Integer)localView.getTag()).intValue() == -1)
                  {
                    localObject = localView;
                    localView.layout(i - getPaddingRight() - localView.getMeasuredWidth(), getPaddingTop(), i - getPaddingRight() + localView.getMeasuredWidth(), getPaddingTop() + localView.getMeasuredHeight());
                  }
                  for (;;)
                  {
                    paramAnonymous2Int1 += 1;
                    break;
                    if (((Integer)localView.getTag()).intValue() == -2)
                    {
                      paramAnonymous2Int3 = i - getPaddingRight() - localView.getMeasuredWidth();
                      paramAnonymous2Int2 = paramAnonymous2Int3;
                      if (localObject != null) {
                        paramAnonymous2Int2 = paramAnonymous2Int3 - (((View)localObject).getMeasuredWidth() + AndroidUtilities.dp(8.0F));
                      }
                      localView.layout(paramAnonymous2Int2, getPaddingTop(), localView.getMeasuredWidth() + paramAnonymous2Int2, getPaddingTop() + localView.getMeasuredHeight());
                    }
                    else
                    {
                      localView.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + localView.getMeasuredWidth(), getPaddingTop() + localView.getMeasuredHeight());
                    }
                  }
                }
              }
            };
            ((FrameLayout)localObject3).setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F));
            ((LinearLayout)localObject1).addView((View)localObject3, LayoutHelper.createLinear(-1, 52));
            localObject1 = new TextView(PhotoViewer.this.parentActivity);
            ((TextView)localObject1).setMinWidth(AndroidUtilities.dp(64.0F));
            ((TextView)localObject1).setTag(Integer.valueOf(-1));
            ((TextView)localObject1).setTextSize(1, 14.0F);
            ((TextView)localObject1).setTextColor(-11944718);
            ((TextView)localObject1).setGravity(17);
            ((TextView)localObject1).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            ((TextView)localObject1).setText(LocaleController.getString("Done", 2131493395).toUpperCase());
            ((TextView)localObject1).setBackgroundDrawable(Theme.getRoundRectSelectorDrawable());
            ((TextView)localObject1).setPadding(AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(10.0F), 0);
            ((FrameLayout)localObject3).addView((View)localObject1, LayoutHelper.createFrame(-2, 36, 53));
            ((TextView)localObject1).setOnClickListener(new View.OnClickListener()
            {
              public void onClick(View paramAnonymous2View)
              {
                int i = localObject2.getValue();
                paramAnonymous2View = MessagesController.getGlobalMainSettings().edit();
                paramAnonymous2View.putInt("self_destruct", i);
                paramAnonymous2View.commit();
                paramAnonymousView.dismiss();
                label90:
                ImageView localImageView;
                if ((i >= 0) && (i < 21))
                {
                  paramAnonymous2View = PhotoViewer.this.imagesArrLocals.get(PhotoViewer.this.currentIndex);
                  if (!(paramAnonymous2View instanceof MediaController.PhotoEntry)) {
                    break label164;
                  }
                  ((MediaController.PhotoEntry)paramAnonymous2View).ttl = i;
                  localImageView = PhotoViewer.this.timeItem;
                  if (i == 0) {
                    break label182;
                  }
                }
                label164:
                label182:
                for (paramAnonymous2View = new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY);; paramAnonymous2View = null)
                {
                  localImageView.setColorFilter(paramAnonymous2View);
                  if (!PhotoViewer.this.checkImageView.isChecked()) {
                    PhotoViewer.this.checkImageView.callOnClick();
                  }
                  return;
                  i = (i - 16) * 5;
                  break;
                  if (!(paramAnonymous2View instanceof MediaController.SearchImage)) {
                    break label90;
                  }
                  ((MediaController.SearchImage)paramAnonymous2View).ttl = i;
                  break label90;
                }
              }
            });
            localObject1 = new TextView(PhotoViewer.this.parentActivity);
            ((TextView)localObject1).setMinWidth(AndroidUtilities.dp(64.0F));
            ((TextView)localObject1).setTag(Integer.valueOf(-2));
            ((TextView)localObject1).setTextSize(1, 14.0F);
            ((TextView)localObject1).setTextColor(-11944718);
            ((TextView)localObject1).setGravity(17);
            ((TextView)localObject1).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            ((TextView)localObject1).setText(LocaleController.getString("Cancel", 2131493127).toUpperCase());
            ((TextView)localObject1).setBackgroundDrawable(Theme.getRoundRectSelectorDrawable());
            ((TextView)localObject1).setPadding(AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(10.0F), 0);
            ((FrameLayout)localObject3).addView((View)localObject1, LayoutHelper.createFrame(-2, 36, 53));
            ((TextView)localObject1).setOnClickListener(new View.OnClickListener()
            {
              public void onClick(View paramAnonymous2View)
              {
                paramAnonymousView.dismiss();
              }
            });
            paramAnonymousView.show();
            paramAnonymousView.setBackgroundColor(-16777216);
            return;
            paramAnonymousView = LocaleController.getString("MessageLifetimePhoto", 2131493821);
            break;
            label766:
            if ((localObject3 instanceof MediaController.SearchImage))
            {
              i = ((MediaController.SearchImage)localObject3).ttl;
              break label369;
            }
            i = 0;
            break label369;
            label791:
            if ((i >= 0) && (i < 21)) {
              ((NumberPicker)localObject2).setValue(i);
            } else {
              ((NumberPicker)localObject2).setValue(i / 5 + 21 - 5);
            }
          }
        }
      });
      this.editorDoneLayout = new PickerBottomLayoutViewer(this.actvityContext);
      this.editorDoneLayout.setBackgroundColor(2130706432);
      this.editorDoneLayout.updateSelectedCount(0, false);
      this.editorDoneLayout.setVisibility(8);
      this.containerView.addView(this.editorDoneLayout, LayoutHelper.createFrame(-1, 48, 83));
      this.editorDoneLayout.cancelButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (PhotoViewer.this.currentEditMode == 1) {
            PhotoViewer.this.photoCropView.cancelAnimationRunnable();
          }
          PhotoViewer.this.switchToEditMode(0);
        }
      });
      this.editorDoneLayout.doneButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if ((PhotoViewer.this.currentEditMode == 1) && (!PhotoViewer.this.photoCropView.isReady())) {
            return;
          }
          PhotoViewer.this.applyCurrentEditMode();
          PhotoViewer.this.switchToEditMode(0);
        }
      });
      this.resetButton = new TextView(this.actvityContext);
      this.resetButton.setVisibility(8);
      this.resetButton.setTextSize(1, 14.0F);
      this.resetButton.setTextColor(-1);
      this.resetButton.setGravity(17);
      this.resetButton.setBackgroundDrawable(Theme.createSelectorDrawable(-12763843, 0));
      this.resetButton.setPadding(AndroidUtilities.dp(20.0F), 0, AndroidUtilities.dp(20.0F), 0);
      this.resetButton.setText(LocaleController.getString("Reset", 2131493315).toUpperCase());
      this.resetButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.editorDoneLayout.addView(this.resetButton, LayoutHelper.createFrame(-2, -1, 49));
      this.resetButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          PhotoViewer.this.photoCropView.reset();
        }
      });
      this.gestureDetector = new GestureDetector(this.containerView.getContext(), this);
      this.gestureDetector.setOnDoubleTapListener(this);
      paramActivity = new ImageReceiver.ImageReceiverDelegate()
      {
        public void didSetImage(ImageReceiver paramAnonymousImageReceiver, boolean paramAnonymousBoolean1, boolean paramAnonymousBoolean2)
        {
          Bitmap localBitmap;
          PhotoCropView localPhotoCropView;
          int i;
          if ((paramAnonymousImageReceiver == PhotoViewer.this.centerImage) && (paramAnonymousBoolean1) && (!paramAnonymousBoolean2) && (PhotoViewer.this.currentEditMode == 1) && (PhotoViewer.this.photoCropView != null))
          {
            localBitmap = paramAnonymousImageReceiver.getBitmap();
            if (localBitmap != null)
            {
              localPhotoCropView = PhotoViewer.this.photoCropView;
              i = paramAnonymousImageReceiver.getOrientation();
              if (PhotoViewer.this.sendPhotoType == 1) {
                break label159;
              }
            }
          }
          label159:
          for (paramAnonymousBoolean2 = true;; paramAnonymousBoolean2 = false)
          {
            localPhotoCropView.setBitmap(localBitmap, i, paramAnonymousBoolean2);
            if ((paramAnonymousImageReceiver == PhotoViewer.this.centerImage) && (paramAnonymousBoolean1) && (PhotoViewer.this.placeProvider != null) && (PhotoViewer.this.placeProvider.scaleToFill()) && (!PhotoViewer.this.ignoreDidSetImage))
            {
              if (PhotoViewer.this.wasLayout) {
                break;
              }
              PhotoViewer.access$6702(PhotoViewer.this, true);
            }
            return;
          }
          PhotoViewer.this.setScaleToFill();
        }
      };
      this.centerImage.setParentView(this.containerView);
      this.centerImage.setCrossfadeAlpha((byte)2);
      this.centerImage.setInvalidateAll(true);
      this.centerImage.setDelegate(paramActivity);
      this.leftImage.setParentView(this.containerView);
      this.leftImage.setCrossfadeAlpha((byte)2);
      this.leftImage.setInvalidateAll(true);
      this.leftImage.setDelegate(paramActivity);
      this.rightImage.setParentView(this.containerView);
      this.rightImage.setCrossfadeAlpha((byte)2);
      this.rightImage.setInvalidateAll(true);
      this.rightImage.setDelegate(paramActivity);
      i = ((WindowManager)ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
      this.checkImageView = new CheckBox(this.containerView.getContext(), 2131165635)
      {
        public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          return (PhotoViewer.this.bottomTouchEnabled) && (super.onTouchEvent(paramAnonymousMotionEvent));
        }
      };
      this.checkImageView.setDrawBackground(true);
      this.checkImageView.setHasBorder(true);
      this.checkImageView.setSize(40);
      this.checkImageView.setCheckOffset(AndroidUtilities.dp(1.0F));
      this.checkImageView.setColor(-10043398, -1);
      this.checkImageView.setVisibility(8);
      paramActivity = this.containerView;
      localObject = this.checkImageView;
      if ((i != 3) && (i != 1)) {
        break label3732;
      }
      f = 58.0F;
      label3033:
      paramActivity.addView((View)localObject, LayoutHelper.createFrame(40, 40.0F, 53, 0.0F, f, 10.0F, 0.0F));
      if (Build.VERSION.SDK_INT >= 21)
      {
        paramActivity = (FrameLayout.LayoutParams)this.checkImageView.getLayoutParams();
        paramActivity.topMargin += AndroidUtilities.statusBarHeight;
      }
      this.checkImageView.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (PhotoViewer.this.captionEditText.getTag() != null) {
            return;
          }
          PhotoViewer.this.setPhotoChecked();
        }
      });
      this.photosCounterView = new CounterView(this.parentActivity);
      paramActivity = this.containerView;
      localObject = this.photosCounterView;
      if ((i != 3) && (i != 1)) {
        break label3739;
      }
    }
    label3732:
    label3739:
    for (float f = 58.0F;; f = 68.0F)
    {
      paramActivity.addView((View)localObject, LayoutHelper.createFrame(40, 40.0F, 53, 0.0F, f, 66.0F, 0.0F));
      if (Build.VERSION.SDK_INT >= 21)
      {
        paramActivity = (FrameLayout.LayoutParams)this.photosCounterView.getLayoutParams();
        paramActivity.topMargin += AndroidUtilities.statusBarHeight;
      }
      this.photosCounterView.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if ((PhotoViewer.this.captionEditText.getTag() != null) || (PhotoViewer.this.placeProvider == null) || (PhotoViewer.this.placeProvider.getSelectedPhotosOrder() == null) || (PhotoViewer.this.placeProvider.getSelectedPhotosOrder().isEmpty())) {
            return;
          }
          paramAnonymousView = PhotoViewer.this;
          if (!PhotoViewer.this.isPhotosListViewVisible) {}
          for (boolean bool = true;; bool = false)
          {
            paramAnonymousView.togglePhotosListView(bool, true);
            return;
          }
        }
      });
      this.selectedPhotosListView = new RecyclerListView(this.parentActivity);
      this.selectedPhotosListView.setVisibility(8);
      this.selectedPhotosListView.setAlpha(0.0F);
      this.selectedPhotosListView.setTranslationY(-AndroidUtilities.dp(10.0F));
      this.selectedPhotosListView.addItemDecoration(new RecyclerView.ItemDecoration()
      {
        public void getItemOffsets(android.graphics.Rect paramAnonymousRect, View paramAnonymousView, RecyclerView paramAnonymousRecyclerView, RecyclerView.State paramAnonymousState)
        {
          int i = paramAnonymousRecyclerView.getChildAdapterPosition(paramAnonymousView);
          if (((paramAnonymousView instanceof PhotoPickerPhotoCell)) && (i == 0)) {}
          for (paramAnonymousRect.left = AndroidUtilities.dp(3.0F);; paramAnonymousRect.left = 0)
          {
            paramAnonymousRect.right = AndroidUtilities.dp(3.0F);
            return;
          }
        }
      });
      ((DefaultItemAnimator)this.selectedPhotosListView.getItemAnimator()).setDelayAnimations(false);
      this.selectedPhotosListView.setBackgroundColor(2130706432);
      this.selectedPhotosListView.setPadding(0, AndroidUtilities.dp(3.0F), 0, AndroidUtilities.dp(3.0F));
      this.selectedPhotosListView.setLayoutManager(new LinearLayoutManager(this.parentActivity, 0, false)
      {
        public void smoothScrollToPosition(RecyclerView paramAnonymousRecyclerView, RecyclerView.State paramAnonymousState, int paramAnonymousInt)
        {
          paramAnonymousRecyclerView = new LinearSmoothScrollerEnd(paramAnonymousRecyclerView.getContext());
          paramAnonymousRecyclerView.setTargetPosition(paramAnonymousInt);
          startSmoothScroll(paramAnonymousRecyclerView);
        }
      });
      paramActivity = this.selectedPhotosListView;
      localObject = new ListAdapter(this.parentActivity);
      this.selectedPhotosAdapter = ((ListAdapter)localObject);
      paramActivity.setAdapter((RecyclerView.Adapter)localObject);
      this.containerView.addView(this.selectedPhotosListView, LayoutHelper.createFrame(-1, 88, 51));
      this.selectedPhotosListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt == 0) && (PhotoViewer.this.placeProvider.allowGroupPhotos()))
          {
            boolean bool = SharedConfig.groupPhotosEnabled;
            SharedConfig.toggleGroupPhotosEnabled();
            PhotoViewer.this.placeProvider.toggleGroupPhotosEnabled();
            ImageView localImageView = (ImageView)paramAnonymousView;
            if (!bool)
            {
              paramAnonymousView = new PorterDuffColorFilter(-10043398, PorterDuff.Mode.MULTIPLY);
              localImageView.setColorFilter(paramAnonymousView);
              paramAnonymousView = PhotoViewer.this;
              if (bool) {
                break label90;
              }
            }
            label90:
            for (bool = true;; bool = false)
            {
              paramAnonymousView.showHint(false, bool);
              return;
              paramAnonymousView = null;
              break;
            }
          }
          PhotoViewer.access$11502(PhotoViewer.this, true);
          paramAnonymousInt = PhotoViewer.this.imagesArrLocals.indexOf(paramAnonymousView.getTag());
          if (paramAnonymousInt >= 0)
          {
            PhotoViewer.access$3002(PhotoViewer.this, -1);
            PhotoViewer.this.setImageIndex(paramAnonymousInt, true);
          }
          PhotoViewer.access$11502(PhotoViewer.this, false);
        }
      });
      this.captionEditText = new PhotoViewerCaptionEnterView(this.actvityContext, this.containerView, this.windowView)
      {
        public boolean dispatchTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          boolean bool2 = false;
          boolean bool1 = bool2;
          try
          {
            if (!PhotoViewer.this.bottomTouchEnabled)
            {
              boolean bool3 = super.dispatchTouchEvent(paramAnonymousMotionEvent);
              bool1 = bool2;
              if (bool3) {
                bool1 = true;
              }
            }
            return bool1;
          }
          catch (Exception paramAnonymousMotionEvent)
          {
            FileLog.e(paramAnonymousMotionEvent);
          }
          return false;
        }
        
        public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          boolean bool2 = false;
          boolean bool1 = bool2;
          try
          {
            if (!PhotoViewer.this.bottomTouchEnabled)
            {
              boolean bool3 = super.onInterceptTouchEvent(paramAnonymousMotionEvent);
              bool1 = bool2;
              if (bool3) {
                bool1 = true;
              }
            }
            return bool1;
          }
          catch (Exception paramAnonymousMotionEvent)
          {
            FileLog.e(paramAnonymousMotionEvent);
          }
          return false;
        }
        
        public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          return (!PhotoViewer.this.bottomTouchEnabled) && (super.onTouchEvent(paramAnonymousMotionEvent));
        }
      };
      this.captionEditText.setDelegate(new PhotoViewerCaptionEnterView.PhotoViewerCaptionEnterViewDelegate()
      {
        public void onCaptionEnter()
        {
          PhotoViewer.this.closeCaptionEnter(true);
        }
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence)
        {
          if ((PhotoViewer.this.mentionsAdapter != null) && (PhotoViewer.this.captionEditText != null) && (PhotoViewer.this.parentChatActivity != null) && (paramAnonymousCharSequence != null)) {
            PhotoViewer.this.mentionsAdapter.searchUsernameOrHashtag(paramAnonymousCharSequence.toString(), PhotoViewer.this.captionEditText.getCursorPosition(), PhotoViewer.this.parentChatActivity.messages, false);
          }
        }
        
        public void onWindowSizeChanged(int paramAnonymousInt)
        {
          int j = Math.min(3, PhotoViewer.this.mentionsAdapter.getItemCount());
          int i;
          if (PhotoViewer.this.mentionsAdapter.getItemCount() > 3)
          {
            i = 18;
            i = AndroidUtilities.dp(i + j * 36);
            if (paramAnonymousInt - ActionBar.getCurrentActionBarHeight() * 2 >= i) {
              break label103;
            }
            PhotoViewer.access$12102(PhotoViewer.this, false);
            if ((PhotoViewer.this.mentionListView != null) && (PhotoViewer.this.mentionListView.getVisibility() == 0)) {
              PhotoViewer.this.mentionListView.setVisibility(4);
            }
          }
          label103:
          do
          {
            return;
            i = 0;
            break;
            PhotoViewer.access$12102(PhotoViewer.this, true);
          } while ((PhotoViewer.this.mentionListView == null) || (PhotoViewer.this.mentionListView.getVisibility() != 4));
          PhotoViewer.this.mentionListView.setVisibility(0);
        }
      });
      this.containerView.addView(this.captionEditText, LayoutHelper.createFrame(-1, -2, 83));
      this.mentionListView = new RecyclerListView(this.actvityContext)
      {
        public boolean dispatchTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          return (!PhotoViewer.this.bottomTouchEnabled) && (super.dispatchTouchEvent(paramAnonymousMotionEvent));
        }
        
        public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          return (!PhotoViewer.this.bottomTouchEnabled) && (super.onInterceptTouchEvent(paramAnonymousMotionEvent));
        }
        
        public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          return (!PhotoViewer.this.bottomTouchEnabled) && (super.onTouchEvent(paramAnonymousMotionEvent));
        }
      };
      this.mentionListView.setTag(Integer.valueOf(5));
      this.mentionLayoutManager = new LinearLayoutManager(this.actvityContext)
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
      this.containerView.addView(this.mentionListView, LayoutHelper.createFrame(-1, 110, 83));
      paramActivity = this.mentionListView;
      localObject = new MentionsAdapter(this.actvityContext, true, 0L, new MentionsAdapter.MentionsAdapterDelegate()
      {
        public void needChangePanelVisibility(boolean paramAnonymousBoolean)
        {
          int i;
          if (paramAnonymousBoolean)
          {
            FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)PhotoViewer.this.mentionListView.getLayoutParams();
            int j = Math.min(3, PhotoViewer.this.mentionsAdapter.getItemCount());
            if (PhotoViewer.this.mentionsAdapter.getItemCount() > 3)
            {
              i = 18;
              i = j * 36 + i;
              localLayoutParams.height = AndroidUtilities.dp(i);
              localLayoutParams.topMargin = (-AndroidUtilities.dp(i));
              PhotoViewer.this.mentionListView.setLayoutParams(localLayoutParams);
              if (PhotoViewer.this.mentionListAnimation != null)
              {
                PhotoViewer.this.mentionListAnimation.cancel();
                PhotoViewer.access$12202(PhotoViewer.this, null);
              }
              if (PhotoViewer.this.mentionListView.getVisibility() != 0) {
                break label150;
              }
              PhotoViewer.this.mentionListView.setAlpha(1.0F);
            }
          }
          label150:
          do
          {
            return;
            i = 0;
            break;
            PhotoViewer.this.mentionLayoutManager.scrollToPositionWithOffset(0, 10000);
            if (PhotoViewer.this.allowMentions)
            {
              PhotoViewer.this.mentionListView.setVisibility(0);
              PhotoViewer.access$12202(PhotoViewer.this, new AnimatorSet());
              PhotoViewer.this.mentionListAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(PhotoViewer.this.mentionListView, "alpha", new float[] { 0.0F, 1.0F }) });
              PhotoViewer.this.mentionListAnimation.addListener(new AnimatorListenerAdapter()
              {
                public void onAnimationEnd(Animator paramAnonymous2Animator)
                {
                  if ((PhotoViewer.this.mentionListAnimation != null) && (PhotoViewer.this.mentionListAnimation.equals(paramAnonymous2Animator))) {
                    PhotoViewer.access$12202(PhotoViewer.this, null);
                  }
                }
              });
              PhotoViewer.this.mentionListAnimation.setDuration(200L);
              PhotoViewer.this.mentionListAnimation.start();
              return;
            }
            PhotoViewer.this.mentionListView.setAlpha(1.0F);
            PhotoViewer.this.mentionListView.setVisibility(4);
            return;
            if (PhotoViewer.this.mentionListAnimation != null)
            {
              PhotoViewer.this.mentionListAnimation.cancel();
              PhotoViewer.access$12202(PhotoViewer.this, null);
            }
          } while (PhotoViewer.this.mentionListView.getVisibility() == 8);
          if (PhotoViewer.this.allowMentions)
          {
            PhotoViewer.access$12202(PhotoViewer.this, new AnimatorSet());
            PhotoViewer.this.mentionListAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(PhotoViewer.this.mentionListView, "alpha", new float[] { 0.0F }) });
            PhotoViewer.this.mentionListAnimation.addListener(new AnimatorListenerAdapter()
            {
              public void onAnimationEnd(Animator paramAnonymous2Animator)
              {
                if ((PhotoViewer.this.mentionListAnimation != null) && (PhotoViewer.this.mentionListAnimation.equals(paramAnonymous2Animator)))
                {
                  PhotoViewer.this.mentionListView.setVisibility(8);
                  PhotoViewer.access$12202(PhotoViewer.this, null);
                }
              }
            });
            PhotoViewer.this.mentionListAnimation.setDuration(200L);
            PhotoViewer.this.mentionListAnimation.start();
            return;
          }
          PhotoViewer.this.mentionListView.setVisibility(8);
        }
        
        public void onContextClick(TLRPC.BotInlineResult paramAnonymousBotInlineResult) {}
        
        public void onContextSearch(boolean paramAnonymousBoolean) {}
      });
      this.mentionsAdapter = ((MentionsAdapter)localObject);
      paramActivity.setAdapter((RecyclerView.Adapter)localObject);
      this.mentionListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          paramAnonymousView = PhotoViewer.this.mentionsAdapter.getItem(paramAnonymousInt);
          paramAnonymousInt = PhotoViewer.this.mentionsAdapter.getResultStartPosition();
          int i = PhotoViewer.this.mentionsAdapter.getResultLength();
          if ((paramAnonymousView instanceof TLRPC.User))
          {
            paramAnonymousView = (TLRPC.User)paramAnonymousView;
            if (paramAnonymousView.username != null) {
              PhotoViewer.this.captionEditText.replaceWithText(paramAnonymousInt, i, "@" + paramAnonymousView.username + " ", false);
            }
          }
          do
          {
            return;
            Object localObject = UserObject.getFirstName(paramAnonymousView);
            localObject = new SpannableString((String)localObject + " ");
            ((Spannable)localObject).setSpan(new URLSpanUserMentionPhotoViewer("" + paramAnonymousView.id, true), 0, ((Spannable)localObject).length(), 33);
            PhotoViewer.this.captionEditText.replaceWithText(paramAnonymousInt, i, (CharSequence)localObject, false);
            return;
            if ((paramAnonymousView instanceof String))
            {
              PhotoViewer.this.captionEditText.replaceWithText(paramAnonymousInt, i, paramAnonymousView + " ", false);
              return;
            }
          } while (!(paramAnonymousView instanceof EmojiSuggestion));
          paramAnonymousView = ((EmojiSuggestion)paramAnonymousView).emoji;
          PhotoViewer.this.captionEditText.addEmojiToRecent(paramAnonymousView);
          PhotoViewer.this.captionEditText.replaceWithText(paramAnonymousInt, i, paramAnonymousView, true);
        }
      });
      this.mentionListView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((PhotoViewer.this.mentionsAdapter.getItem(paramAnonymousInt) instanceof String))
          {
            paramAnonymousView = new AlertDialog.Builder(PhotoViewer.this.parentActivity);
            paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
            paramAnonymousView.setMessage(LocaleController.getString("ClearSearch", 2131493264));
            paramAnonymousView.setPositiveButton(LocaleController.getString("ClearButton", 2131493257).toUpperCase(), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                PhotoViewer.this.mentionsAdapter.clearRecentHashtags();
              }
            });
            paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            PhotoViewer.this.showAlertDialog(paramAnonymousView);
            return true;
          }
          return false;
        }
      });
      return;
      if (this.selectedCompression == 1)
      {
        this.compressItem.setImageResource(2131165689);
        break;
      }
      if (this.selectedCompression == 2)
      {
        this.compressItem.setImageResource(2131165690);
        break;
      }
      if (this.selectedCompression == 3)
      {
        this.compressItem.setImageResource(2131165691);
        break;
      }
      if (this.selectedCompression != 4) {
        break;
      }
      this.compressItem.setImageResource(2131165687);
      break;
      f = 68.0F;
      break label3033;
    }
  }
  
  public void setParentAlert(ChatAttachAlert paramChatAttachAlert)
  {
    this.parentAlert = paramChatAttachAlert;
  }
  
  public void setParentChatActivity(ChatActivity paramChatActivity)
  {
    this.parentChatActivity = paramChatActivity;
  }
  
  public void showAlertDialog(AlertDialog.Builder paramBuilder)
  {
    if (this.parentActivity == null) {
      return;
    }
    try
    {
      if (this.visibleDialog != null)
      {
        this.visibleDialog.dismiss();
        this.visibleDialog = null;
      }
      try
      {
        this.visibleDialog = paramBuilder.show();
        this.visibleDialog.setCanceledOnTouchOutside(true);
        this.visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
          public void onDismiss(DialogInterface paramAnonymousDialogInterface)
          {
            PhotoViewer.access$14002(PhotoViewer.this, null);
          }
        });
        return;
      }
      catch (Exception paramBuilder)
      {
        FileLog.e(paramBuilder);
        return;
      }
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public void updateMuteButton()
  {
    if (this.videoPlayer != null) {
      this.videoPlayer.setMute(this.muteVideo);
    }
    if (!this.videoHasAudio)
    {
      this.muteItem.setEnabled(false);
      this.muteItem.setClickable(false);
      this.muteItem.setAlpha(0.5F);
      return;
    }
    this.muteItem.setEnabled(true);
    this.muteItem.setClickable(true);
    this.muteItem.setAlpha(1.0F);
    if (this.muteVideo)
    {
      this.actionBar.setSubtitle(null);
      this.muteItem.setImageResource(2131165701);
      this.muteItem.setColorFilter(new PorterDuffColorFilter(-12734994, PorterDuff.Mode.MULTIPLY));
      if (this.compressItem.getTag() != null)
      {
        this.compressItem.setClickable(false);
        this.compressItem.setAlpha(0.5F);
        this.compressItem.setEnabled(false);
      }
      this.videoTimelineView.setMaxProgressDiff(30000.0F / this.videoDuration);
      return;
    }
    this.muteItem.setColorFilter(null);
    this.actionBar.setSubtitle(this.currentSubtitle);
    this.muteItem.setImageResource(2131165702);
    if (this.compressItem.getTag() != null)
    {
      this.compressItem.setClickable(true);
      this.compressItem.setAlpha(1.0F);
      this.compressItem.setEnabled(true);
    }
    this.videoTimelineView.setMaxProgressDiff(1.0F);
  }
  
  private class BackgroundDrawable
    extends ColorDrawable
  {
    private boolean allowDrawContent;
    private Runnable drawRunnable;
    
    public BackgroundDrawable(int paramInt)
    {
      super();
    }
    
    public void draw(Canvas paramCanvas)
    {
      super.draw(paramCanvas);
      if ((getAlpha() != 0) && (this.drawRunnable != null))
      {
        AndroidUtilities.runOnUIThread(this.drawRunnable);
        this.drawRunnable = null;
      }
    }
    
    @Keep
    public void setAlpha(int paramInt)
    {
      boolean bool;
      if ((PhotoViewer.this.parentActivity instanceof LaunchActivity))
      {
        if ((PhotoViewer.this.isVisible) && (paramInt == 255)) {
          break label94;
        }
        bool = true;
        this.allowDrawContent = bool;
        ((LaunchActivity)PhotoViewer.this.parentActivity).drawerLayoutContainer.setAllowDrawContent(this.allowDrawContent);
        if (PhotoViewer.this.parentAlert != null)
        {
          if (this.allowDrawContent) {
            break label99;
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (PhotoViewer.this.parentAlert != null) {
                PhotoViewer.this.parentAlert.setAllowDrawContent(PhotoViewer.BackgroundDrawable.this.allowDrawContent);
              }
            }
          }, 50L);
        }
      }
      for (;;)
      {
        super.setAlpha(paramInt);
        return;
        label94:
        bool = false;
        break;
        label99:
        if (PhotoViewer.this.parentAlert != null) {
          PhotoViewer.this.parentAlert.setAllowDrawContent(this.allowDrawContent);
        }
      }
    }
  }
  
  private class CounterView
    extends View
  {
    private int currentCount = 0;
    private int height;
    private Paint paint;
    private RectF rect;
    private float rotation;
    private StaticLayout staticLayout;
    private TextPaint textPaint = new TextPaint(1);
    private int width;
    
    public CounterView(Context paramContext)
    {
      super();
      this.textPaint.setTextSize(AndroidUtilities.dp(18.0F));
      this.textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.textPaint.setColor(-1);
      this.paint = new Paint(1);
      this.paint.setColor(-1);
      this.paint.setStrokeWidth(AndroidUtilities.dp(2.0F));
      this.paint.setStyle(Paint.Style.STROKE);
      this.paint.setStrokeJoin(Paint.Join.ROUND);
      this.rect = new RectF();
      setCount(0);
    }
    
    public float getRotationX()
    {
      return this.rotation;
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      int i = getMeasuredHeight() / 2;
      this.paint.setAlpha(255);
      this.rect.set(AndroidUtilities.dp(1.0F), i - AndroidUtilities.dp(14.0F), getMeasuredWidth() - AndroidUtilities.dp(1.0F), AndroidUtilities.dp(14.0F) + i);
      paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(15.0F), AndroidUtilities.dp(15.0F), this.paint);
      if (this.staticLayout != null)
      {
        this.textPaint.setAlpha((int)((1.0F - this.rotation) * 255.0F));
        paramCanvas.save();
        paramCanvas.translate((getMeasuredWidth() - this.width) / 2, (getMeasuredHeight() - this.height) / 2 + AndroidUtilities.dpf2(0.2F) + this.rotation * AndroidUtilities.dp(5.0F));
        this.staticLayout.draw(paramCanvas);
        paramCanvas.restore();
        this.paint.setAlpha((int)(this.rotation * 255.0F));
        i = (int)this.rect.centerX();
        int j = (int)((int)this.rect.centerY() - (AndroidUtilities.dp(5.0F) * (1.0F - this.rotation) + AndroidUtilities.dp(3.0F)));
        paramCanvas.drawLine(AndroidUtilities.dp(0.5F) + i, j - AndroidUtilities.dp(0.5F), i - AndroidUtilities.dp(6.0F), AndroidUtilities.dp(6.0F) + j, this.paint);
        paramCanvas.drawLine(i - AndroidUtilities.dp(0.5F), j - AndroidUtilities.dp(0.5F), AndroidUtilities.dp(6.0F) + i, AndroidUtilities.dp(6.0F) + j, this.paint);
      }
    }
    
    protected void onMeasure(int paramInt1, int paramInt2)
    {
      super.onMeasure(View.MeasureSpec.makeMeasureSpec(Math.max(this.width + AndroidUtilities.dp(20.0F), AndroidUtilities.dp(30.0F)), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40.0F), 1073741824));
    }
    
    public void setCount(int paramInt)
    {
      this.staticLayout = new StaticLayout("" + Math.max(1, paramInt), this.textPaint, AndroidUtilities.dp(100.0F), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      this.width = ((int)Math.ceil(this.staticLayout.getLineWidth(0)));
      this.height = this.staticLayout.getLineBottom(0);
      AnimatorSet localAnimatorSet = new AnimatorSet();
      if (paramInt == 0)
      {
        localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "scaleX", new float[] { 0.0F }), ObjectAnimator.ofFloat(this, "scaleY", new float[] { 0.0F }), ObjectAnimator.ofInt(this.paint, "alpha", new int[] { 0 }), ObjectAnimator.ofInt(this.textPaint, "alpha", new int[] { 0 }) });
        localAnimatorSet.setInterpolator(new DecelerateInterpolator());
      }
      for (;;)
      {
        localAnimatorSet.setDuration(180L);
        localAnimatorSet.start();
        requestLayout();
        this.currentCount = paramInt;
        return;
        if (this.currentCount == 0)
        {
          localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "scaleX", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofFloat(this, "scaleY", new float[] { 0.0F, 1.0F }), ObjectAnimator.ofInt(this.paint, "alpha", new int[] { 0, 255 }), ObjectAnimator.ofInt(this.textPaint, "alpha", new int[] { 0, 255 }) });
          localAnimatorSet.setInterpolator(new DecelerateInterpolator());
        }
        else if (paramInt < this.currentCount)
        {
          localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "scaleX", new float[] { 1.1F, 1.0F }), ObjectAnimator.ofFloat(this, "scaleY", new float[] { 1.1F, 1.0F }) });
          localAnimatorSet.setInterpolator(new OvershootInterpolator());
        }
        else
        {
          localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "scaleX", new float[] { 0.9F, 1.0F }), ObjectAnimator.ofFloat(this, "scaleY", new float[] { 0.9F, 1.0F }) });
          localAnimatorSet.setInterpolator(new OvershootInterpolator());
        }
      }
    }
    
    @Keep
    public void setRotationX(float paramFloat)
    {
      this.rotation = paramFloat;
      invalidate();
    }
    
    @Keep
    public void setScaleX(float paramFloat)
    {
      super.setScaleX(paramFloat);
      invalidate();
    }
  }
  
  public static class EmptyPhotoViewerProvider
    implements PhotoViewer.PhotoViewerProvider
  {
    public boolean allowCaption()
    {
      return true;
    }
    
    public boolean allowGroupPhotos()
    {
      return true;
    }
    
    public boolean canScrollAway()
    {
      return true;
    }
    
    public boolean cancelButtonPressed()
    {
      return true;
    }
    
    public int getPhotoIndex(int paramInt)
    {
      return -1;
    }
    
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt)
    {
      return null;
    }
    
    public int getSelectedCount()
    {
      return 0;
    }
    
    public HashMap<Object, Object> getSelectedPhotos()
    {
      return null;
    }
    
    public ArrayList<Object> getSelectedPhotosOrder()
    {
      return null;
    }
    
    public ImageReceiver.BitmapHolder getThumbForPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt)
    {
      return null;
    }
    
    public boolean isPhotoChecked(int paramInt)
    {
      return false;
    }
    
    public void needAddMorePhotos() {}
    
    public boolean scaleToFill()
    {
      return false;
    }
    
    public void sendButtonPressed(int paramInt, VideoEditedInfo paramVideoEditedInfo) {}
    
    public int setPhotoChecked(int paramInt, VideoEditedInfo paramVideoEditedInfo)
    {
      return -1;
    }
    
    public void toggleGroupPhotosEnabled() {}
    
    public void updatePhotoAtIndex(int paramInt) {}
    
    public void willHidePhotoViewer() {}
    
    public void willSwitchFromPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt) {}
  }
  
  private class FrameLayoutDrawer
    extends SizeNotifierFrameLayoutPhoto
  {
    private Paint paint = new Paint();
    
    public FrameLayoutDrawer(Context paramContext)
    {
      super();
      setWillNotDraw(false);
      this.paint.setColor(855638016);
    }
    
    protected boolean drawChild(Canvas paramCanvas, View paramView, long paramLong)
    {
      if ((paramView == PhotoViewer.this.mentionListView) || (paramView == PhotoViewer.this.captionEditText))
      {
        if ((!PhotoViewer.this.captionEditText.isPopupShowing()) && (PhotoViewer.this.captionEditText.getEmojiPadding() == 0) && (((AndroidUtilities.usingHardwareInput) && (PhotoViewer.this.captionEditText.getTag() == null)) || (getKeyboardHeight() == 0))) {
          return false;
        }
      }
      else
      {
        if ((paramView != PhotoViewer.this.cameraItem) && (paramView != PhotoViewer.this.pickerView) && (paramView != PhotoViewer.this.pickerViewSendButton) && (paramView != PhotoViewer.this.captionTextView) && ((PhotoViewer.this.muteItem.getVisibility() != 0) || (paramView != PhotoViewer.this.bottomLayout))) {
          break label271;
        }
        if ((getKeyboardHeight() <= AndroidUtilities.dp(20.0F)) && (!AndroidUtilities.isInMultiwindow)) {}
        for (int i = PhotoViewer.this.captionEditText.getEmojiPadding(); (PhotoViewer.this.captionEditText.isPopupShowing()) || ((AndroidUtilities.usingHardwareInput) && (PhotoViewer.this.captionEditText.getTag() != null)) || (getKeyboardHeight() > 0) || (i != 0); i = 0)
        {
          PhotoViewer.access$5802(PhotoViewer.this, false);
          return false;
        }
        PhotoViewer.access$5802(PhotoViewer.this, true);
      }
      label271:
      do
      {
        for (;;)
        {
          try
          {
            if (paramView == PhotoViewer.this.aspectRatioFrameLayout) {
              break;
            }
            boolean bool = super.drawChild(paramCanvas, paramView, paramLong);
            if (!bool) {
              break;
            }
            return true;
          }
          catch (Throwable paramCanvas) {}
          if ((paramView != PhotoViewer.this.checkImageView) && (paramView != PhotoViewer.this.photosCounterView)) {
            continue;
          }
          if (PhotoViewer.this.captionEditText.getTag() != null)
          {
            PhotoViewer.access$5802(PhotoViewer.this, false);
            return false;
          }
          PhotoViewer.access$5802(PhotoViewer.this, true);
        }
      } while (paramView != PhotoViewer.this.miniProgressView);
      return false;
      return true;
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      PhotoViewer.this.onDraw(paramCanvas);
      if ((Build.VERSION.SDK_INT >= 21) && (AndroidUtilities.statusBarHeight != 0) && (PhotoViewer.this.actionBar != null))
      {
        this.paint.setAlpha((int)(255.0F * PhotoViewer.this.actionBar.getAlpha() * 0.2F));
        paramCanvas.drawRect(0.0F, 0.0F, getMeasuredWidth(), AndroidUtilities.statusBarHeight, this.paint);
      }
    }
    
    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      int i1 = getChildCount();
      if ((getKeyboardHeight() <= AndroidUtilities.dp(20.0F)) && (!AndroidUtilities.isInMultiwindow)) {}
      View localView;
      for (int k = PhotoViewer.this.captionEditText.getEmojiPadding();; k = 0)
      {
        int m = 0;
        for (;;)
        {
          if (m >= i1) {
            break label611;
          }
          localView = getChildAt(m);
          if (localView.getVisibility() != 8) {
            break;
          }
          m += 1;
        }
      }
      FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)localView.getLayoutParams();
      int i2 = localView.getMeasuredWidth();
      int i3 = localView.getMeasuredHeight();
      int j = localLayoutParams.gravity;
      int i = j;
      if (j == -1) {
        i = 51;
      }
      switch (i & 0x7 & 0x7)
      {
      default: 
        j = localLayoutParams.leftMargin;
        label167:
        switch (i & 0x70)
        {
        default: 
          i = localLayoutParams.topMargin;
          label215:
          if (localView == PhotoViewer.this.mentionListView) {
            i -= PhotoViewer.this.captionEditText.getMeasuredHeight();
          }
          break;
        }
        break;
      }
      for (;;)
      {
        localView.layout(j, i, j + i2, i + i3);
        break;
        j = (paramInt3 - paramInt1 - i2) / 2 + localLayoutParams.leftMargin - localLayoutParams.rightMargin;
        break label167;
        j = paramInt3 - paramInt1 - i2 - localLayoutParams.rightMargin;
        break label167;
        i = localLayoutParams.topMargin;
        break label215;
        i = (paramInt4 - k - paramInt2 - i3) / 2 + localLayoutParams.topMargin - localLayoutParams.bottomMargin;
        break label215;
        i = paramInt4 - k - paramInt2 - i3 - localLayoutParams.bottomMargin;
        break label215;
        if (PhotoViewer.this.captionEditText.isPopupView(localView))
        {
          if (AndroidUtilities.isInMultiwindow) {
            i = PhotoViewer.this.captionEditText.getTop() - localView.getMeasuredHeight() + AndroidUtilities.dp(1.0F);
          } else {
            i = PhotoViewer.this.captionEditText.getBottom();
          }
        }
        else if (localView == PhotoViewer.this.selectedPhotosListView)
        {
          i = PhotoViewer.this.actionBar.getMeasuredHeight();
        }
        else if ((localView == PhotoViewer.this.captionTextView) || (localView == PhotoViewer.this.switchCaptionTextView))
        {
          int n = 0;
          if (!PhotoViewer.GroupedPhotosListView.access$5000(PhotoViewer.this.groupedPhotosListView).isEmpty()) {
            n = 0 + PhotoViewer.this.groupedPhotosListView.getMeasuredHeight();
          }
          i -= n;
        }
        else if ((PhotoViewer.this.hintTextView != null) && (localView == PhotoViewer.this.hintTextView))
        {
          i = PhotoViewer.this.selectedPhotosListView.getBottom() + AndroidUtilities.dp(3.0F);
        }
        else if (localView == PhotoViewer.this.cameraItem)
        {
          i = PhotoViewer.this.pickerView.getTop() - AndroidUtilities.dp(15.0F) - PhotoViewer.this.cameraItem.getMeasuredHeight();
        }
      }
      label611:
      notifyHeightChanged();
    }
    
    protected void onMeasure(int paramInt1, int paramInt2)
    {
      int k = View.MeasureSpec.getSize(paramInt1);
      int m = View.MeasureSpec.getSize(paramInt2);
      setMeasuredDimension(k, m);
      measureChildWithMargins(PhotoViewer.this.captionEditText, paramInt1, 0, paramInt2, 0);
      int n = PhotoViewer.this.captionEditText.getMeasuredHeight();
      int i1 = getChildCount();
      int i = 0;
      if (i < i1)
      {
        View localView = getChildAt(i);
        if ((localView.getVisibility() == 8) || (localView == PhotoViewer.this.captionEditText)) {}
        for (;;)
        {
          i += 1;
          break;
          if (localView == PhotoViewer.this.aspectRatioFrameLayout)
          {
            int i2 = AndroidUtilities.displaySize.y;
            if (Build.VERSION.SDK_INT >= 21) {}
            for (int j = AndroidUtilities.statusBarHeight;; j = 0)
            {
              measureChildWithMargins(localView, paramInt1, 0, View.MeasureSpec.makeMeasureSpec(j + i2, 1073741824), 0);
              break;
            }
          }
          if (PhotoViewer.this.captionEditText.isPopupView(localView))
          {
            if (AndroidUtilities.isInMultiwindow)
            {
              if (AndroidUtilities.isTablet()) {
                localView.measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320.0F), m - n - AndroidUtilities.statusBarHeight), 1073741824));
              } else {
                localView.measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(m - n - AndroidUtilities.statusBarHeight, 1073741824));
              }
            }
            else {
              localView.measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(localView.getLayoutParams().height, 1073741824));
            }
          }
          else {
            measureChildWithMargins(localView, paramInt1, 0, paramInt2, 0);
          }
        }
      }
    }
  }
  
  private class GroupedPhotosListView
    extends View
    implements GestureDetector.OnGestureListener
  {
    private boolean animateAllLine;
    private int animateToDX;
    private int animateToDXStart;
    private int animateToItem = -1;
    private Paint backgroundPaint = new Paint();
    private long currentGroupId;
    private int currentImage;
    private float currentItemProgress = 1.0F;
    private ArrayList<Object> currentObjects = new ArrayList();
    private ArrayList<TLObject> currentPhotos = new ArrayList();
    private int drawDx;
    private GestureDetector gestureDetector = new GestureDetector(paramContext, this);
    private boolean ignoreChanges;
    private ArrayList<ImageReceiver> imagesToDraw = new ArrayList();
    private int itemHeight;
    private int itemSpacing;
    private int itemWidth;
    private int itemY;
    private long lastUpdateTime;
    private float moveLineProgress;
    private boolean moving;
    private int nextImage;
    private float nextItemProgress = 0.0F;
    private int nextPhotoScrolling = -1;
    private Scroller scroll;
    private boolean scrolling;
    private boolean stopedScrolling;
    private ArrayList<ImageReceiver> unusedReceivers = new ArrayList();
    
    public GroupedPhotosListView(Context paramContext)
    {
      super();
      this.scroll = new Scroller(paramContext);
      this.itemWidth = AndroidUtilities.dp(42.0F);
      this.itemHeight = AndroidUtilities.dp(56.0F);
      this.itemSpacing = AndroidUtilities.dp(1.0F);
      this.itemY = AndroidUtilities.dp(3.0F);
      this.backgroundPaint.setColor(2130706432);
    }
    
    private void fillImages(boolean paramBoolean, int paramInt)
    {
      if ((!paramBoolean) && (!this.imagesToDraw.isEmpty()))
      {
        this.unusedReceivers.addAll(this.imagesToDraw);
        this.imagesToDraw.clear();
        this.moving = false;
        this.moveLineProgress = 1.0F;
        this.currentItemProgress = 1.0F;
        this.nextItemProgress = 0.0F;
      }
      invalidate();
      if ((getMeasuredWidth() == 0) || (this.currentPhotos.isEmpty())) {}
      label566:
      for (;;)
      {
        return;
        int i3 = getMeasuredWidth();
        int i2 = getMeasuredWidth() / 2 - this.itemWidth / 2;
        int i;
        int j;
        int k;
        Object localObject1;
        if (paramBoolean)
        {
          i = Integer.MIN_VALUE;
          j = Integer.MAX_VALUE;
          int i1 = this.imagesToDraw.size();
          k = 0;
          for (;;)
          {
            m = j;
            n = i;
            if (k >= i1) {
              break;
            }
            localObject1 = (ImageReceiver)this.imagesToDraw.get(k);
            int i4 = ((ImageReceiver)localObject1).getParam();
            int i5 = (i4 - this.currentImage) * (this.itemWidth + this.itemSpacing) + i2 + paramInt;
            if (i5 <= i3)
            {
              n = k;
              m = i1;
              if (this.itemWidth + i5 >= 0) {}
            }
            else
            {
              this.unusedReceivers.add(localObject1);
              this.imagesToDraw.remove(k);
              m = i1 - 1;
              n = k - 1;
            }
            j = Math.min(j, i4 - 1);
            i = Math.max(i, i4 + 1);
            k = n + 1;
            i1 = m;
          }
        }
        int n = this.currentImage;
        int m = this.currentImage - 1;
        Object localObject2;
        if (n != Integer.MIN_VALUE)
        {
          j = this.currentPhotos.size();
          i = n;
          while (i < j)
          {
            k = (i - this.currentImage) * (this.itemWidth + this.itemSpacing) + i2 + paramInt;
            if (k >= i3) {
              break;
            }
            localObject2 = (TLObject)this.currentPhotos.get(i);
            localObject1 = localObject2;
            if ((localObject2 instanceof TLRPC.PhotoSize)) {
              localObject1 = ((TLRPC.PhotoSize)localObject2).location;
            }
            localObject2 = getFreeReceiver();
            ((ImageReceiver)localObject2).setImageCoords(k, this.itemY, this.itemWidth, this.itemHeight);
            ((ImageReceiver)localObject2).setImage(null, null, null, null, (TLRPC.FileLocation)localObject1, "80_80", 0, null, 1);
            ((ImageReceiver)localObject2).setParam(i);
            i += 1;
          }
        }
        if (m != Integer.MAX_VALUE)
        {
          i = m;
          for (;;)
          {
            if (i < 0) {
              break label566;
            }
            j = (i - this.currentImage) * (this.itemWidth + this.itemSpacing) + i2 + paramInt + this.itemWidth;
            if (j <= 0) {
              break;
            }
            localObject2 = (TLObject)this.currentPhotos.get(i);
            localObject1 = localObject2;
            if ((localObject2 instanceof TLRPC.PhotoSize)) {
              localObject1 = ((TLRPC.PhotoSize)localObject2).location;
            }
            localObject2 = getFreeReceiver();
            ((ImageReceiver)localObject2).setImageCoords(j, this.itemY, this.itemWidth, this.itemHeight);
            ((ImageReceiver)localObject2).setImage(null, null, null, null, (TLRPC.FileLocation)localObject1, "80_80", 0, null, 1);
            ((ImageReceiver)localObject2).setParam(i);
            i -= 1;
          }
        }
      }
    }
    
    private ImageReceiver getFreeReceiver()
    {
      ImageReceiver localImageReceiver;
      if (this.unusedReceivers.isEmpty()) {
        localImageReceiver = new ImageReceiver(this);
      }
      for (;;)
      {
        this.imagesToDraw.add(localImageReceiver);
        localImageReceiver.setCurrentAccount(PhotoViewer.this.currentAccount);
        return localImageReceiver;
        localImageReceiver = (ImageReceiver)this.unusedReceivers.get(0);
        this.unusedReceivers.remove(0);
      }
    }
    
    private int getMaxScrollX()
    {
      return this.currentImage * (this.itemWidth + this.itemSpacing * 2);
    }
    
    private int getMinScrollX()
    {
      return -(this.currentPhotos.size() - this.currentImage - 1) * (this.itemWidth + this.itemSpacing * 2);
    }
    
    private void stopScrolling()
    {
      this.scrolling = false;
      if (!this.scroll.isFinished()) {
        this.scroll.abortAnimation();
      }
      if ((this.nextPhotoScrolling >= 0) && (this.nextPhotoScrolling < this.currentObjects.size()))
      {
        this.stopedScrolling = true;
        int i = this.nextPhotoScrolling;
        this.animateToItem = i;
        this.nextImage = i;
        this.animateToDX = ((this.currentImage - this.nextPhotoScrolling) * (this.itemWidth + this.itemSpacing));
        this.animateToDXStart = this.drawDx;
        this.moveLineProgress = 1.0F;
        this.nextPhotoScrolling = -1;
      }
      invalidate();
    }
    
    private void updateAfterScroll()
    {
      int i = 0;
      int j = this.drawDx;
      Object localObject;
      if (Math.abs(j) > this.itemWidth / 2 + this.itemSpacing)
      {
        if (j > 0)
        {
          i = j - (this.itemWidth / 2 + this.itemSpacing);
          j = 0 + 1;
          i = j + i / (this.itemWidth + this.itemSpacing * 2);
        }
      }
      else
      {
        this.nextPhotoScrolling = (this.currentImage - i);
        if ((PhotoViewer.this.currentIndex != this.nextPhotoScrolling) && (this.nextPhotoScrolling >= 0) && (this.nextPhotoScrolling < this.currentPhotos.size()))
        {
          localObject = this.currentObjects.get(this.nextPhotoScrolling);
          i = -1;
          if (PhotoViewer.this.imagesArr.isEmpty()) {
            break label256;
          }
          localObject = (MessageObject)localObject;
          i = PhotoViewer.this.imagesArr.indexOf(localObject);
        }
      }
      for (;;)
      {
        if (i >= 0)
        {
          this.ignoreChanges = true;
          PhotoViewer.access$3002(PhotoViewer.this, -1);
          if (PhotoViewer.this.currentThumb != null)
          {
            PhotoViewer.this.currentThumb.release();
            PhotoViewer.access$3302(PhotoViewer.this, null);
          }
          PhotoViewer.this.setImageIndex(i, true);
        }
        if (!this.scrolling)
        {
          this.scrolling = true;
          this.stopedScrolling = false;
        }
        fillImages(true, this.drawDx);
        return;
        i = j + (this.itemWidth / 2 + this.itemSpacing);
        j = 0 - 1;
        break;
        label256:
        if (!PhotoViewer.this.imagesArrLocations.isEmpty())
        {
          localObject = (TLRPC.FileLocation)localObject;
          i = PhotoViewer.this.imagesArrLocations.indexOf(localObject);
        }
      }
    }
    
    public void clear()
    {
      this.currentPhotos.clear();
      this.currentObjects.clear();
      this.imagesToDraw.clear();
    }
    
    public void fillList()
    {
      label12:
      int n;
      int k;
      int m;
      Object localObject1;
      int j;
      int i;
      if (this.ignoreChanges)
      {
        this.ignoreChanges = false;
        return;
      }
      else
      {
        n = 0;
        k = 0;
        m = 0;
        localObject1 = null;
        if (PhotoViewer.this.imagesArrLocations.isEmpty()) {
          break label224;
        }
        localObject1 = (TLRPC.FileLocation)PhotoViewer.this.imagesArrLocations.get(PhotoViewer.this.currentIndex);
        j = PhotoViewer.this.imagesArrLocations.size();
        i = n;
      }
      label73:
      if (localObject1 != null)
      {
        k = i;
        if (i == 0)
        {
          if ((j == this.currentPhotos.size()) && (this.currentObjects.indexOf(localObject1) != -1)) {
            break label513;
          }
          k = 1;
        }
        label110:
        if (k == 0) {
          break label606;
        }
        this.animateAllLine = false;
        this.currentPhotos.clear();
        this.currentObjects.clear();
        if (PhotoViewer.this.imagesArrLocations.isEmpty()) {
          break label642;
        }
        this.currentObjects.addAll(PhotoViewer.this.imagesArrLocations);
        this.currentPhotos.addAll(PhotoViewer.this.imagesArrLocations);
        this.currentImage = PhotoViewer.this.currentIndex;
        this.animateToItem = -1;
      }
      label224:
      label513:
      label606:
      label642:
      label915:
      for (;;)
      {
        if (this.currentPhotos.size() == 1)
        {
          this.currentPhotos.clear();
          this.currentObjects.clear();
        }
        fillImages(false, 0);
        return;
        i = n;
        j = m;
        if (PhotoViewer.this.imagesArr.isEmpty()) {
          break label73;
        }
        localObject1 = (MessageObject)PhotoViewer.this.imagesArr.get(PhotoViewer.this.currentIndex);
        Object localObject2 = localObject1;
        if (((MessageObject)localObject1).messageOwner.grouped_id != this.currentGroupId)
        {
          i = 1;
          this.currentGroupId = ((MessageObject)localObject1).messageOwner.grouped_id;
          localObject1 = localObject2;
          j = m;
          break label73;
          break label12;
        }
        m = Math.min(PhotoViewer.this.currentIndex + 10, PhotoViewer.this.imagesArr.size());
        j = PhotoViewer.this.currentIndex;
        i = k;
        while (j < m)
        {
          localObject1 = (MessageObject)PhotoViewer.this.imagesArr.get(j);
          if ((PhotoViewer.this.slideshowMessageId == 0) && (((MessageObject)localObject1).messageOwner.grouped_id != this.currentGroupId)) {
            break;
          }
          i += 1;
          j += 1;
        }
        int i1 = Math.max(PhotoViewer.this.currentIndex - 10, 0);
        m = PhotoViewer.this.currentIndex - 1;
        k = i;
        for (;;)
        {
          i = n;
          localObject1 = localObject2;
          j = k;
          if (m < i1) {
            break;
          }
          MessageObject localMessageObject = (MessageObject)PhotoViewer.this.imagesArr.get(m);
          if (PhotoViewer.this.slideshowMessageId == 0)
          {
            i = n;
            localObject1 = localObject2;
            j = k;
            if (localMessageObject.messageOwner.grouped_id != this.currentGroupId) {
              break;
            }
          }
          k += 1;
          m -= 1;
        }
        j = this.currentObjects.indexOf(localObject1);
        k = i;
        if (this.currentImage == j) {
          break label110;
        }
        k = i;
        if (j == -1) {
          break label110;
        }
        if (this.animateAllLine)
        {
          this.animateToItem = j;
          this.nextImage = j;
          this.animateToDX = ((this.currentImage - j) * (this.itemWidth + this.itemSpacing));
          this.moving = true;
          this.animateAllLine = false;
          this.lastUpdateTime = System.currentTimeMillis();
          invalidate();
        }
        for (;;)
        {
          this.drawDx = 0;
          k = i;
          break label110;
          break;
          fillImages(true, (this.currentImage - j) * (this.itemWidth + this.itemSpacing));
          this.currentImage = j;
          this.moving = false;
        }
        if ((!PhotoViewer.this.imagesArr.isEmpty()) && ((this.currentGroupId != 0L) || (PhotoViewer.this.slideshowMessageId != 0)))
        {
          j = Math.min(PhotoViewer.this.currentIndex + 10, PhotoViewer.this.imagesArr.size());
          i = PhotoViewer.this.currentIndex;
          while (i < j)
          {
            localObject1 = (MessageObject)PhotoViewer.this.imagesArr.get(i);
            if ((PhotoViewer.this.slideshowMessageId == 0) && (((MessageObject)localObject1).messageOwner.grouped_id != this.currentGroupId)) {
              break;
            }
            this.currentObjects.add(localObject1);
            this.currentPhotos.add(FileLoader.getClosestPhotoSizeWithSize(((MessageObject)localObject1).photoThumbs, 56, true));
            i += 1;
          }
          this.currentImage = 0;
          this.animateToItem = -1;
          j = Math.max(PhotoViewer.this.currentIndex - 10, 0);
          i = PhotoViewer.this.currentIndex - 1;
          for (;;)
          {
            if (i < j) {
              break label915;
            }
            localObject1 = (MessageObject)PhotoViewer.this.imagesArr.get(i);
            if ((PhotoViewer.this.slideshowMessageId == 0) && (((MessageObject)localObject1).messageOwner.grouped_id != this.currentGroupId)) {
              break;
            }
            this.currentObjects.add(0, localObject1);
            this.currentPhotos.add(0, FileLoader.getClosestPhotoSizeWithSize(((MessageObject)localObject1).photoThumbs, 56, true));
            this.currentImage += 1;
            i -= 1;
          }
        }
      }
    }
    
    public boolean onDown(MotionEvent paramMotionEvent)
    {
      if (!this.scroll.isFinished()) {
        this.scroll.abortAnimation();
      }
      this.animateToItem = -1;
      return true;
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      if (this.imagesToDraw.isEmpty()) {
        return;
      }
      paramCanvas.drawRect(0.0F, 0.0F, getMeasuredWidth(), getMeasuredHeight(), this.backgroundPaint);
      int j = this.imagesToDraw.size();
      int n = this.drawDx;
      int i2 = (int)(this.itemWidth * 2.0F);
      int i1 = AndroidUtilities.dp(8.0F);
      Object localObject = (TLObject)this.currentPhotos.get(this.currentImage);
      int i;
      int m;
      label251:
      label311:
      int i3;
      label362:
      int i4;
      if ((localObject instanceof TLRPC.PhotoSize))
      {
        localObject = (TLRPC.PhotoSize)localObject;
        i = Math.max(this.itemWidth, (int)(((TLRPC.PhotoSize)localObject).w * (this.itemHeight / ((TLRPC.PhotoSize)localObject).h)));
        i = Math.min(i2, i);
        int k = (int)(i1 * 2 * this.currentItemProgress);
        m = this.itemWidth + (int)((i - this.itemWidth) * this.currentItemProgress) + k;
        if ((this.nextImage < 0) || (this.nextImage >= this.currentPhotos.size())) {
          break label458;
        }
        localObject = (TLObject)this.currentPhotos.get(this.nextImage);
        if (!(localObject instanceof TLRPC.PhotoSize)) {
          break label449;
        }
        localObject = (TLRPC.PhotoSize)localObject;
        i = Math.max(this.itemWidth, (int)(((TLRPC.PhotoSize)localObject).w * (this.itemHeight / ((TLRPC.PhotoSize)localObject).h)));
        i2 = Math.min(i2, i);
        i1 = (int)(i1 * 2 * this.nextItemProgress);
        float f1 = n;
        float f2 = (i2 + i1 - this.itemWidth) / 2;
        float f3 = this.nextItemProgress;
        if (this.nextImage <= this.currentImage) {
          break label467;
        }
        i = -1;
        n = (int)(i * (f3 * f2) + f1);
        i2 = this.itemWidth + (int)((i2 - this.itemWidth) * this.nextItemProgress) + i1;
        i3 = (getMeasuredWidth() - m) / 2;
        i = 0;
        if (i >= j) {
          break label818;
        }
        localObject = (ImageReceiver)this.imagesToDraw.get(i);
        i4 = ((ImageReceiver)localObject).getParam();
        if (i4 != this.currentImage) {
          break label473;
        }
        ((ImageReceiver)localObject).setImageX(i3 + n + k / 2);
        ((ImageReceiver)localObject).setImageWidth(m - k);
      }
      for (;;)
      {
        ((ImageReceiver)localObject).draw(paramCanvas);
        i += 1;
        break label362;
        i = this.itemHeight;
        break;
        label449:
        i = this.itemHeight;
        break label251;
        label458:
        i = this.itemWidth;
        break label251;
        label467:
        i = 1;
        break label311;
        label473:
        if (this.nextImage < this.currentImage) {
          if (i4 < this.currentImage) {
            if (i4 <= this.nextImage) {
              ((ImageReceiver)localObject).setImageX((((ImageReceiver)localObject).getParam() - this.currentImage + 1) * (this.itemWidth + this.itemSpacing) + i3 - (this.itemSpacing + i2) + n);
            }
          }
        }
        for (;;)
        {
          if (i4 != this.nextImage) {
            break label806;
          }
          ((ImageReceiver)localObject).setImageWidth(i2 - i1);
          ((ImageReceiver)localObject).setImageX(((ImageReceiver)localObject).getImageX() + i1 / 2);
          break;
          ((ImageReceiver)localObject).setImageX((((ImageReceiver)localObject).getParam() - this.currentImage) * (this.itemWidth + this.itemSpacing) + i3 + n);
          continue;
          ((ImageReceiver)localObject).setImageX(i3 + m + this.itemSpacing + (((ImageReceiver)localObject).getParam() - this.currentImage - 1) * (this.itemWidth + this.itemSpacing) + n);
          continue;
          if (i4 < this.currentImage) {
            ((ImageReceiver)localObject).setImageX((((ImageReceiver)localObject).getParam() - this.currentImage) * (this.itemWidth + this.itemSpacing) + i3 + n);
          } else if (i4 <= this.nextImage) {
            ((ImageReceiver)localObject).setImageX(i3 + m + this.itemSpacing + (((ImageReceiver)localObject).getParam() - this.currentImage - 1) * (this.itemWidth + this.itemSpacing) + n);
          } else {
            ((ImageReceiver)localObject).setImageX(i3 + m + this.itemSpacing + (((ImageReceiver)localObject).getParam() - this.currentImage - 2) * (this.itemWidth + this.itemSpacing) + (this.itemSpacing + i2) + n);
          }
        }
        label806:
        ((ImageReceiver)localObject).setImageWidth(this.itemWidth);
      }
      label818:
      long l3 = System.currentTimeMillis();
      long l2 = l3 - this.lastUpdateTime;
      long l1 = l2;
      if (l2 > 17L) {
        l1 = 17L;
      }
      this.lastUpdateTime = l3;
      if (this.animateToItem >= 0) {
        if (this.moveLineProgress > 0.0F)
        {
          this.moveLineProgress -= (float)l1 / 200.0F;
          if (this.animateToItem != this.currentImage) {
            break label1136;
          }
          if (this.currentItemProgress < 1.0F)
          {
            this.currentItemProgress += (float)l1 / 200.0F;
            if (this.currentItemProgress > 1.0F) {
              this.currentItemProgress = 1.0F;
            }
          }
          this.drawDx = (this.animateToDXStart + (int)Math.ceil(this.currentItemProgress * (this.animateToDX - this.animateToDXStart)));
        }
      }
      for (;;)
      {
        if (this.moveLineProgress <= 0.0F)
        {
          this.currentImage = this.animateToItem;
          this.moveLineProgress = 1.0F;
          this.currentItemProgress = 1.0F;
          this.nextItemProgress = 0.0F;
          this.moving = false;
          this.stopedScrolling = false;
          this.drawDx = 0;
          this.animateToItem = -1;
        }
        fillImages(true, this.drawDx);
        invalidate();
        if ((this.scrolling) && (this.currentItemProgress > 0.0F))
        {
          this.currentItemProgress -= (float)l1 / 200.0F;
          if (this.currentItemProgress < 0.0F) {
            this.currentItemProgress = 0.0F;
          }
          invalidate();
        }
        if (this.scroll.isFinished()) {
          break;
        }
        if (this.scroll.computeScrollOffset())
        {
          this.drawDx = this.scroll.getCurrX();
          updateAfterScroll();
          invalidate();
        }
        if (!this.scroll.isFinished()) {
          break;
        }
        stopScrolling();
        return;
        label1136:
        this.nextItemProgress = CubicBezierInterpolator.EASE_OUT.getInterpolation(1.0F - this.moveLineProgress);
        if (this.stopedScrolling)
        {
          if (this.currentItemProgress > 0.0F)
          {
            this.currentItemProgress -= (float)l1 / 200.0F;
            if (this.currentItemProgress < 0.0F) {
              this.currentItemProgress = 0.0F;
            }
          }
          this.drawDx = (this.animateToDXStart + (int)Math.ceil(this.nextItemProgress * (this.animateToDX - this.animateToDXStart)));
        }
        else
        {
          this.currentItemProgress = CubicBezierInterpolator.EASE_OUT.getInterpolation(this.moveLineProgress);
          this.drawDx = ((int)Math.ceil(this.nextItemProgress * this.animateToDX));
        }
      }
    }
    
    public boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2)
    {
      this.scroll.abortAnimation();
      if (this.currentPhotos.size() >= 10) {
        this.scroll.fling(this.drawDx, 0, Math.round(paramFloat1), 0, getMinScrollX(), getMaxScrollX(), 0, 0);
      }
      return false;
    }
    
    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
      fillImages(false, 0);
    }
    
    public void onLongPress(MotionEvent paramMotionEvent) {}
    
    public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2)
    {
      this.drawDx = ((int)(this.drawDx - paramFloat1));
      int i = getMinScrollX();
      int j = getMaxScrollX();
      if (this.drawDx < i) {
        this.drawDx = i;
      }
      for (;;)
      {
        updateAfterScroll();
        return false;
        if (this.drawDx > j) {
          this.drawDx = j;
        }
      }
    }
    
    public void onShowPress(MotionEvent paramMotionEvent) {}
    
    public boolean onSingleTapUp(MotionEvent paramMotionEvent)
    {
      stopScrolling();
      int j = this.imagesToDraw.size();
      int i = 0;
      for (;;)
      {
        if (i < j)
        {
          ImageReceiver localImageReceiver = (ImageReceiver)this.imagesToDraw.get(i);
          if (localImageReceiver.isInsideImage(paramMotionEvent.getX(), paramMotionEvent.getY()))
          {
            i = localImageReceiver.getParam();
            if ((i < 0) || (i >= this.currentObjects.size())) {}
            do
            {
              return true;
              if (PhotoViewer.this.imagesArr.isEmpty()) {
                break;
              }
              paramMotionEvent = (MessageObject)this.currentObjects.get(i);
              i = PhotoViewer.this.imagesArr.indexOf(paramMotionEvent);
            } while (PhotoViewer.this.currentIndex == i);
            this.moveLineProgress = 1.0F;
            this.animateAllLine = true;
            PhotoViewer.access$3002(PhotoViewer.this, -1);
            if (PhotoViewer.this.currentThumb != null)
            {
              PhotoViewer.this.currentThumb.release();
              PhotoViewer.access$3302(PhotoViewer.this, null);
            }
            PhotoViewer.this.setImageIndex(i, true);
          }
        }
        else
        {
          for (;;)
          {
            return false;
            if (!PhotoViewer.this.imagesArrLocations.isEmpty())
            {
              paramMotionEvent = (TLRPC.FileLocation)this.currentObjects.get(i);
              i = PhotoViewer.this.imagesArrLocations.indexOf(paramMotionEvent);
              if (PhotoViewer.this.currentIndex == i) {
                break;
              }
              this.moveLineProgress = 1.0F;
              this.animateAllLine = true;
              PhotoViewer.access$3002(PhotoViewer.this, -1);
              if (PhotoViewer.this.currentThumb != null)
              {
                PhotoViewer.this.currentThumb.release();
                PhotoViewer.access$3302(PhotoViewer.this, null);
              }
              PhotoViewer.this.setImageIndex(i, true);
            }
          }
        }
        i += 1;
      }
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent)
    {
      boolean bool3 = false;
      boolean bool1 = false;
      boolean bool2 = bool1;
      if (!this.currentPhotos.isEmpty())
      {
        if (getAlpha() == 1.0F) {
          break label30;
        }
        bool2 = bool1;
      }
      label30:
      do
      {
        do
        {
          do
          {
            return bool2;
            if (!this.gestureDetector.onTouchEvent(paramMotionEvent))
            {
              bool1 = bool3;
              if (!super.onTouchEvent(paramMotionEvent)) {}
            }
            else
            {
              bool1 = true;
            }
            bool2 = bool1;
          } while (!this.scrolling);
          bool2 = bool1;
        } while (paramMotionEvent.getAction() != 1);
        bool2 = bool1;
      } while (!this.scroll.isFinished());
      stopScrolling();
      return bool1;
    }
    
    public void setMoveProgress(float paramFloat)
    {
      if ((this.scrolling) || (this.animateToItem >= 0)) {
        return;
      }
      if (paramFloat > 0.0F)
      {
        this.nextImage = (this.currentImage - 1);
        label31:
        if ((this.nextImage < 0) || (this.nextImage >= this.currentPhotos.size())) {
          break label174;
        }
        this.currentItemProgress = (1.0F - Math.abs(paramFloat));
        label62:
        this.nextItemProgress = (1.0F - this.currentItemProgress);
        if (paramFloat == 0.0F) {
          break label182;
        }
      }
      label174:
      label182:
      for (boolean bool = true;; bool = false)
      {
        this.moving = bool;
        invalidate();
        if ((this.currentPhotos.isEmpty()) || ((paramFloat < 0.0F) && (this.currentImage == this.currentPhotos.size() - 1)) || ((paramFloat > 0.0F) && (this.currentImage == 0))) {
          break;
        }
        this.drawDx = ((int)((this.itemWidth + this.itemSpacing) * paramFloat));
        fillImages(true, this.drawDx);
        return;
        this.nextImage = (this.currentImage + 1);
        break label31;
        this.currentItemProgress = 1.0F;
        break label62;
      }
    }
  }
  
  private static class LinkMovementMethodMy
    extends LinkMovementMethod
  {
    public boolean onTouchEvent(TextView paramTextView, Spannable paramSpannable, MotionEvent paramMotionEvent)
    {
      try
      {
        boolean bool = super.onTouchEvent(paramTextView, paramSpannable, paramMotionEvent);
        if ((paramMotionEvent.getAction() == 1) || (paramMotionEvent.getAction() == 3)) {
          Selection.removeSelection(paramSpannable);
        }
        return bool;
      }
      catch (Exception paramTextView)
      {
        FileLog.e(paramTextView);
      }
      return false;
    }
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public int getItemCount()
    {
      if ((PhotoViewer.this.placeProvider != null) && (PhotoViewer.this.placeProvider.getSelectedPhotosOrder() != null))
      {
        if (PhotoViewer.this.placeProvider.allowGroupPhotos()) {
          return PhotoViewer.this.placeProvider.getSelectedPhotosOrder().size() + 1;
        }
        return PhotoViewer.this.placeProvider.getSelectedPhotosOrder().size();
      }
      return 0;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == 0) && (PhotoViewer.this.placeProvider.allowGroupPhotos())) {
        return 1;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      default: 
      case 0: 
        do
        {
          return;
          paramViewHolder = (PhotoPickerPhotoCell)paramViewHolder.itemView;
          paramViewHolder.itemWidth = AndroidUtilities.dp(82.0F);
          localObject1 = paramViewHolder.photoImage;
          ((BackupImageView)localObject1).setOrientation(0, true);
          localObject2 = PhotoViewer.this.placeProvider.getSelectedPhotosOrder();
          int i = paramInt;
          if (PhotoViewer.this.placeProvider.allowGroupPhotos()) {
            i = paramInt - 1;
          }
          localObject2 = PhotoViewer.this.placeProvider.getSelectedPhotos().get(((ArrayList)localObject2).get(i));
          if ((localObject2 instanceof MediaController.PhotoEntry))
          {
            localObject2 = (MediaController.PhotoEntry)localObject2;
            paramViewHolder.setTag(localObject2);
            paramViewHolder.videoInfoContainer.setVisibility(4);
            if (((MediaController.PhotoEntry)localObject2).thumbPath != null) {
              ((BackupImageView)localObject1).setImage(((MediaController.PhotoEntry)localObject2).thumbPath, null, this.mContext.getResources().getDrawable(2131165542));
            }
            for (;;)
            {
              paramViewHolder.setChecked(-1, true, false);
              paramViewHolder.checkBox.setVisibility(0);
              return;
              if (((MediaController.PhotoEntry)localObject2).path != null)
              {
                ((BackupImageView)localObject1).setOrientation(((MediaController.PhotoEntry)localObject2).orientation, true);
                if (((MediaController.PhotoEntry)localObject2).isVideo)
                {
                  paramViewHolder.videoInfoContainer.setVisibility(0);
                  paramInt = ((MediaController.PhotoEntry)localObject2).duration / 60;
                  i = ((MediaController.PhotoEntry)localObject2).duration;
                  paramViewHolder.videoTextView.setText(String.format("%d:%02d", new Object[] { Integer.valueOf(paramInt), Integer.valueOf(i - paramInt * 60) }));
                  ((BackupImageView)localObject1).setImage("vthumb://" + ((MediaController.PhotoEntry)localObject2).imageId + ":" + ((MediaController.PhotoEntry)localObject2).path, null, this.mContext.getResources().getDrawable(2131165542));
                }
                else
                {
                  ((BackupImageView)localObject1).setImage("thumb://" + ((MediaController.PhotoEntry)localObject2).imageId + ":" + ((MediaController.PhotoEntry)localObject2).path, null, this.mContext.getResources().getDrawable(2131165542));
                }
              }
              else
              {
                ((BackupImageView)localObject1).setImageResource(2131165542);
              }
            }
          }
        } while (!(localObject2 instanceof MediaController.SearchImage));
        Object localObject2 = (MediaController.SearchImage)localObject2;
        paramViewHolder.setTag(localObject2);
        if (((MediaController.SearchImage)localObject2).thumbPath != null) {
          ((BackupImageView)localObject1).setImage(((MediaController.SearchImage)localObject2).thumbPath, null, this.mContext.getResources().getDrawable(2131165542));
        }
        for (;;)
        {
          paramViewHolder.videoInfoContainer.setVisibility(4);
          paramViewHolder.setChecked(-1, true, false);
          paramViewHolder.checkBox.setVisibility(0);
          return;
          if ((((MediaController.SearchImage)localObject2).thumbUrl != null) && (((MediaController.SearchImage)localObject2).thumbUrl.length() > 0)) {
            ((BackupImageView)localObject1).setImage(((MediaController.SearchImage)localObject2).thumbUrl, null, this.mContext.getResources().getDrawable(2131165542));
          } else if ((((MediaController.SearchImage)localObject2).document != null) && (((MediaController.SearchImage)localObject2).document.thumb != null)) {
            ((BackupImageView)localObject1).setImage(((MediaController.SearchImage)localObject2).document.thumb.location, null, this.mContext.getResources().getDrawable(2131165542));
          } else {
            ((BackupImageView)localObject1).setImageResource(2131165542);
          }
        }
      }
      Object localObject1 = (ImageView)paramViewHolder.itemView;
      if (SharedConfig.groupPhotosEnabled) {}
      for (paramViewHolder = new PorterDuffColorFilter(-10043398, PorterDuff.Mode.MULTIPLY);; paramViewHolder = null)
      {
        ((ImageView)localObject1).setColorFilter(paramViewHolder);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new ImageView(this.mContext)
        {
          protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
          {
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(66.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramAnonymousInt2), 1073741824));
          }
        };
        paramViewGroup.setScaleType(ImageView.ScaleType.CENTER);
        paramViewGroup.setImageResource(2131165593);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new PhotoPickerPhotoCell(this.mContext, false);
        paramViewGroup.checkFrame.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = ((View)paramAnonymousView.getParent()).getTag();
            int i = PhotoViewer.this.imagesArrLocals.indexOf(paramAnonymousView);
            if (i >= 0)
            {
              int j = PhotoViewer.this.placeProvider.setPhotoChecked(i, PhotoViewer.this.getCurrentVideoEditedInfo());
              PhotoViewer.this.placeProvider.isPhotoChecked(i);
              if (i == PhotoViewer.this.currentIndex) {
                PhotoViewer.this.checkImageView.setChecked(-1, false, true);
              }
              if (j >= 0)
              {
                i = j;
                if (PhotoViewer.this.placeProvider.allowGroupPhotos()) {
                  i = j + 1;
                }
                PhotoViewer.this.selectedPhotosAdapter.notifyItemRemoved(i);
              }
              PhotoViewer.this.updateSelectedCount();
            }
          }
        });
      }
    }
  }
  
  private class PhotoProgressView
  {
    private float alpha = 1.0F;
    private float animatedAlphaValue = 1.0F;
    private float animatedProgressValue = 0.0F;
    private float animationProgressStart = 0.0F;
    private int backgroundState = -1;
    private float currentProgress = 0.0F;
    private long currentProgressTime = 0L;
    private long lastUpdateTime = 0L;
    private View parent = null;
    private int previousBackgroundState = -2;
    private RectF progressRect = new RectF();
    private float radOffset = 0.0F;
    private float scale = 1.0F;
    private int size = AndroidUtilities.dp(64.0F);
    
    public PhotoProgressView(Context paramContext, View paramView)
    {
      if (PhotoViewer.decelerateInterpolator == null)
      {
        PhotoViewer.access$3802(new DecelerateInterpolator(1.5F));
        PhotoViewer.access$3902(new Paint(1));
        PhotoViewer.progressPaint.setStyle(Paint.Style.STROKE);
        PhotoViewer.progressPaint.setStrokeCap(Paint.Cap.ROUND);
        PhotoViewer.progressPaint.setStrokeWidth(AndroidUtilities.dp(3.0F));
        PhotoViewer.progressPaint.setColor(-1);
      }
      this.parent = paramView;
    }
    
    private void updateAnimation()
    {
      long l3 = System.currentTimeMillis();
      long l2 = l3 - this.lastUpdateTime;
      long l1 = l2;
      if (l2 > 18L) {
        l1 = 18L;
      }
      this.lastUpdateTime = l3;
      float f;
      if (this.animatedProgressValue != 1.0F)
      {
        this.radOffset += (float)(360L * l1) / 3000.0F;
        f = this.currentProgress - this.animationProgressStart;
        if (f > 0.0F)
        {
          this.currentProgressTime += l1;
          if (this.currentProgressTime < 300L) {
            break label188;
          }
          this.animatedProgressValue = this.currentProgress;
          this.animationProgressStart = this.currentProgress;
          this.currentProgressTime = 0L;
        }
      }
      for (;;)
      {
        this.parent.invalidate();
        if ((this.animatedProgressValue >= 1.0F) && (this.previousBackgroundState != -2))
        {
          this.animatedAlphaValue -= (float)l1 / 200.0F;
          if (this.animatedAlphaValue <= 0.0F)
          {
            this.animatedAlphaValue = 0.0F;
            this.previousBackgroundState = -2;
          }
          this.parent.invalidate();
        }
        return;
        label188:
        this.animatedProgressValue = (this.animationProgressStart + PhotoViewer.decelerateInterpolator.getInterpolation((float)this.currentProgressTime / 300.0F) * f);
      }
    }
    
    public void onDraw(Canvas paramCanvas)
    {
      int i = (int)(this.size * this.scale);
      int j = (PhotoViewer.this.getContainerViewWidth() - i) / 2;
      int k = (PhotoViewer.this.getContainerViewHeight() - i) / 2;
      Drawable localDrawable;
      if ((this.previousBackgroundState >= 0) && (this.previousBackgroundState < 4))
      {
        localDrawable = PhotoViewer.progressDrawables[this.previousBackgroundState];
        if (localDrawable != null)
        {
          localDrawable.setAlpha((int)(this.animatedAlphaValue * 255.0F * this.alpha));
          localDrawable.setBounds(j, k, j + i, k + i);
          localDrawable.draw(paramCanvas);
        }
      }
      if ((this.backgroundState >= 0) && (this.backgroundState < 4))
      {
        localDrawable = PhotoViewer.progressDrawables[this.backgroundState];
        if (localDrawable != null)
        {
          if (this.previousBackgroundState == -2) {
            break label320;
          }
          localDrawable.setAlpha((int)((1.0F - this.animatedAlphaValue) * 255.0F * this.alpha));
          localDrawable.setBounds(j, k, j + i, k + i);
          localDrawable.draw(paramCanvas);
        }
      }
      int m;
      if ((this.backgroundState == 0) || (this.backgroundState == 1) || (this.previousBackgroundState == 0) || (this.previousBackgroundState == 1))
      {
        m = AndroidUtilities.dp(4.0F);
        if (this.previousBackgroundState == -2) {
          break label336;
        }
        PhotoViewer.progressPaint.setAlpha((int)(this.animatedAlphaValue * 255.0F * this.alpha));
      }
      for (;;)
      {
        this.progressRect.set(j + m, k + m, j + i - m, k + i - m);
        paramCanvas.drawArc(this.progressRect, this.radOffset - 90.0F, Math.max(4.0F, 360.0F * this.animatedProgressValue), false, PhotoViewer.progressPaint);
        updateAnimation();
        return;
        label320:
        localDrawable.setAlpha((int)(this.alpha * 255.0F));
        break;
        label336:
        PhotoViewer.progressPaint.setAlpha((int)(this.alpha * 255.0F));
      }
    }
    
    public void setAlpha(float paramFloat)
    {
      this.alpha = paramFloat;
    }
    
    public void setBackgroundState(int paramInt, boolean paramBoolean)
    {
      if ((this.backgroundState == paramInt) && (paramBoolean)) {
        return;
      }
      this.lastUpdateTime = System.currentTimeMillis();
      if ((paramBoolean) && (this.backgroundState != paramInt))
      {
        this.previousBackgroundState = this.backgroundState;
        this.animatedAlphaValue = 1.0F;
      }
      for (;;)
      {
        this.backgroundState = paramInt;
        this.parent.invalidate();
        return;
        this.previousBackgroundState = -2;
      }
    }
    
    public void setProgress(float paramFloat, boolean paramBoolean)
    {
      if (!paramBoolean) {
        this.animatedProgressValue = paramFloat;
      }
      for (this.animationProgressStart = paramFloat;; this.animationProgressStart = this.animatedProgressValue)
      {
        this.currentProgress = paramFloat;
        this.currentProgressTime = 0L;
        return;
      }
    }
    
    public void setScale(float paramFloat)
    {
      this.scale = paramFloat;
    }
  }
  
  public static abstract interface PhotoViewerProvider
  {
    public abstract boolean allowCaption();
    
    public abstract boolean allowGroupPhotos();
    
    public abstract boolean canScrollAway();
    
    public abstract boolean cancelButtonPressed();
    
    public abstract int getPhotoIndex(int paramInt);
    
    public abstract PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt);
    
    public abstract int getSelectedCount();
    
    public abstract HashMap<Object, Object> getSelectedPhotos();
    
    public abstract ArrayList<Object> getSelectedPhotosOrder();
    
    public abstract ImageReceiver.BitmapHolder getThumbForPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt);
    
    public abstract boolean isPhotoChecked(int paramInt);
    
    public abstract void needAddMorePhotos();
    
    public abstract boolean scaleToFill();
    
    public abstract void sendButtonPressed(int paramInt, VideoEditedInfo paramVideoEditedInfo);
    
    public abstract int setPhotoChecked(int paramInt, VideoEditedInfo paramVideoEditedInfo);
    
    public abstract void toggleGroupPhotosEnabled();
    
    public abstract void updatePhotoAtIndex(int paramInt);
    
    public abstract void willHidePhotoViewer();
    
    public abstract void willSwitchFromPhoto(MessageObject paramMessageObject, TLRPC.FileLocation paramFileLocation, int paramInt);
  }
  
  public static class PlaceProviderObject
  {
    public int clipBottomAddition;
    public int clipTopAddition;
    public int dialogId;
    public ImageReceiver imageReceiver;
    public int index;
    public boolean isEvent;
    public View parentView;
    public int radius;
    public float scale = 1.0F;
    public int size;
    public ImageReceiver.BitmapHolder thumb;
    public int viewX;
    public int viewY;
  }
  
  private class QualityChooseView
    extends View
  {
    private int circleSize;
    private int gapSize;
    private int lineSize;
    private boolean moving;
    private Paint paint = new Paint(1);
    private int sideSide;
    private boolean startMoving;
    private int startMovingQuality;
    private float startX;
    private TextPaint textPaint = new TextPaint(1);
    
    public QualityChooseView(Context paramContext)
    {
      super();
      this.textPaint.setTextSize(AndroidUtilities.dp(12.0F));
      this.textPaint.setColor(-3289651);
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      int j;
      int i;
      label74:
      int k;
      label140:
      String str;
      label191:
      float f2;
      float f3;
      float f4;
      if (PhotoViewer.this.compressionsCount != 1)
      {
        this.lineSize = ((getMeasuredWidth() - this.circleSize * PhotoViewer.this.compressionsCount - this.gapSize * 8 - this.sideSide * 2) / (PhotoViewer.this.compressionsCount - 1));
        j = getMeasuredHeight() / 2 + AndroidUtilities.dp(6.0F);
        i = 0;
        if (i >= PhotoViewer.this.compressionsCount) {
          return;
        }
        k = this.sideSide + (this.lineSize + this.gapSize * 2 + this.circleSize) * i + this.circleSize / 2;
        if (i > PhotoViewer.this.selectedCompression) {
          break label378;
        }
        this.paint.setColor(-11292945);
        if (i != PhotoViewer.this.compressionsCount - 1) {
          break label390;
        }
        str = Math.min(PhotoViewer.this.originalWidth, PhotoViewer.this.originalHeight) + "p";
        f2 = this.textPaint.measureText(str);
        f3 = k;
        f4 = j;
        if (i != PhotoViewer.this.selectedCompression) {
          break label435;
        }
      }
      label378:
      label390:
      label435:
      for (float f1 = AndroidUtilities.dp(8.0F);; f1 = this.circleSize / 2)
      {
        paramCanvas.drawCircle(f3, f4, f1, this.paint);
        paramCanvas.drawText(str, k - f2 / 2.0F, j - AndroidUtilities.dp(16.0F), this.textPaint);
        if (i != 0)
        {
          k = k - this.circleSize / 2 - this.gapSize - this.lineSize;
          paramCanvas.drawRect(k, j - AndroidUtilities.dp(1.0F), this.lineSize + k, AndroidUtilities.dp(2.0F) + j, this.paint);
        }
        i += 1;
        break label74;
        this.lineSize = (getMeasuredWidth() - this.circleSize * PhotoViewer.this.compressionsCount - this.gapSize * 8 - this.sideSide * 2);
        break;
        this.paint.setColor(1728053247);
        break label140;
        if (i == 0)
        {
          str = "240p";
          break label191;
        }
        if (i == 1)
        {
          str = "360p";
          break label191;
        }
        if (i == 2)
        {
          str = "480p";
          break label191;
        }
        str = "720p";
        break label191;
      }
    }
    
    protected void onMeasure(int paramInt1, int paramInt2)
    {
      super.onMeasure(paramInt1, paramInt2);
      this.circleSize = AndroidUtilities.dp(12.0F);
      this.gapSize = AndroidUtilities.dp(2.0F);
      this.sideSide = AndroidUtilities.dp(18.0F);
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent)
    {
      boolean bool = false;
      float f = paramMotionEvent.getX();
      int i;
      int j;
      if (paramMotionEvent.getAction() == 0)
      {
        getParent().requestDisallowInterceptTouchEvent(true);
        i = 0;
        if (i < PhotoViewer.this.compressionsCount)
        {
          j = this.sideSide + (this.lineSize + this.gapSize * 2 + this.circleSize) * i + this.circleSize / 2;
          if ((f <= j - AndroidUtilities.dp(15.0F)) || (f >= AndroidUtilities.dp(15.0F) + j)) {
            break label136;
          }
          if (i == PhotoViewer.this.selectedCompression) {
            bool = true;
          }
          this.startMoving = bool;
          this.startX = f;
          this.startMovingQuality = PhotoViewer.this.selectedCompression;
        }
      }
      label136:
      label322:
      label324:
      do
      {
        for (;;)
        {
          return true;
          i += 1;
          break;
          if (paramMotionEvent.getAction() != 2) {
            break label324;
          }
          if (this.startMoving)
          {
            if (Math.abs(this.startX - f) >= AndroidUtilities.getPixelsInCM(0.5F, true))
            {
              this.moving = true;
              this.startMoving = false;
              return true;
            }
          }
          else if (this.moving)
          {
            i = 0;
            for (;;)
            {
              if (i >= PhotoViewer.this.compressionsCount) {
                break label322;
              }
              j = this.sideSide + (this.lineSize + this.gapSize * 2 + this.circleSize) * i + this.circleSize / 2;
              int k = this.lineSize / 2 + this.circleSize / 2 + this.gapSize;
              if ((f > j - k) && (f < j + k))
              {
                if (PhotoViewer.this.selectedCompression == i) {
                  break;
                }
                PhotoViewer.access$9602(PhotoViewer.this, i);
                PhotoViewer.this.didChangedCompressionLevel(false);
                invalidate();
                return true;
              }
              i += 1;
            }
          }
        }
      } while ((paramMotionEvent.getAction() != 1) && (paramMotionEvent.getAction() != 3));
      if (!this.moving)
      {
        i = 0;
        if (i < PhotoViewer.this.compressionsCount)
        {
          j = this.sideSide + (this.lineSize + this.gapSize * 2 + this.circleSize) * i + this.circleSize / 2;
          if ((f <= j - AndroidUtilities.dp(15.0F)) || (f >= AndroidUtilities.dp(15.0F) + j)) {
            break label464;
          }
          if (PhotoViewer.this.selectedCompression != i)
          {
            PhotoViewer.access$9602(PhotoViewer.this, i);
            PhotoViewer.this.didChangedCompressionLevel(true);
            invalidate();
          }
        }
      }
      for (;;)
      {
        this.startMoving = false;
        this.moving = false;
        return true;
        label464:
        i += 1;
        break;
        if (PhotoViewer.this.selectedCompression != this.startMovingQuality) {
          PhotoViewer.this.requestVideoPreview(1);
        }
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/PhotoViewer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */