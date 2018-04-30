package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.Keep;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat.OnCommitContentListener;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Property;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.camera.CameraController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.ReplyMarkup;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;
import org.telegram.tgnet.TLRPC.TL_channelAdminRights;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_inputMessageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetID;
import org.telegram.tgnet.TLRPC.TL_keyboardButton;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRequestGeoLocation;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRequestPhone;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonSwitchInline;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonUrl;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageEntityBold;
import org.telegram.tgnet.TLRPC.TL_messageEntityCode;
import org.telegram.tgnet.TLRPC.TL_messageEntityItalic;
import org.telegram.tgnet.TLRPC.TL_messageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_messageEntityPre;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_replyKeyboardMarkup;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.DialogsActivity.DialogsActivityDelegate;
import org.telegram.ui.GroupStickersActivity;
import org.telegram.ui.StickersActivity;

public class ChatActivityEnterView
  extends FrameLayout
  implements NotificationCenter.NotificationCenterDelegate, SizeNotifierFrameLayout.SizeNotifierFrameLayoutDelegate, StickersAlert.StickersAlertDelegate
{
  private boolean allowGifs;
  private boolean allowShowTopView;
  private boolean allowStickers;
  private ImageView attachButton;
  private LinearLayout attachLayout;
  private ImageView audioSendButton;
  private TLRPC.TL_document audioToSend;
  private MessageObject audioToSendMessageObject;
  private String audioToSendPath;
  private AnimatorSet audioVideoButtonAnimation;
  private FrameLayout audioVideoButtonContainer;
  private ImageView botButton;
  private MessageObject botButtonsMessageObject;
  private int botCount;
  private PopupWindow botKeyboardPopup;
  private BotKeyboardView botKeyboardView;
  private MessageObject botMessageObject;
  private TLRPC.TL_replyKeyboardMarkup botReplyMarkup;
  private boolean calledRecordRunnable;
  private Drawable cameraDrawable;
  private boolean canWriteToChannel;
  private ImageView cancelBotButton;
  private boolean closeAnimationInProgress;
  private int currentAccount = UserConfig.selectedAccount;
  private int currentPopupContentType = -1;
  private AnimatorSet currentTopViewAnimation;
  private ChatActivityEnterViewDelegate delegate;
  private boolean destroyed;
  private long dialog_id;
  private float distCanMove = AndroidUtilities.dp(80.0F);
  private AnimatorSet doneButtonAnimation;
  private FrameLayout doneButtonContainer;
  private ImageView doneButtonImage;
  private ContextProgressView doneButtonProgress;
  private Paint dotPaint = new Paint(1);
  private boolean editingCaption;
  private MessageObject editingMessageObject;
  private int editingMessageReqId;
  private ImageView emojiButton;
  private int emojiPadding;
  private EmojiView emojiView;
  private ImageView expandStickersButton;
  private boolean forceShowSendButton;
  private boolean gifsTabOpen;
  private boolean hasBotCommands;
  private boolean hasRecordVideo;
  private boolean ignoreTextChange;
  private TLRPC.ChatFull info;
  private int innerTextChange;
  private boolean isPaused = true;
  private int keyboardHeight;
  private int keyboardHeightLand;
  private boolean keyboardVisible;
  private int lastSizeChangeValue1;
  private boolean lastSizeChangeValue2;
  private String lastTimeString;
  private long lastTypingSendTime;
  private long lastTypingTimeSend;
  private Drawable lockArrowDrawable;
  private Drawable lockBackgroundDrawable;
  private Drawable lockDrawable;
  private Drawable lockShadowDrawable;
  private Drawable lockTopDrawable;
  private EditTextCaption messageEditText;
  private TLRPC.WebPage messageWebPage;
  private boolean messageWebPageSearch = true;
  private Drawable micDrawable;
  private boolean needShowTopView;
  private ImageView notifyButton;
  private Runnable openKeyboardRunnable = new Runnable()
  {
    public void run()
    {
      if ((!ChatActivityEnterView.this.destroyed) && (ChatActivityEnterView.this.messageEditText != null) && (ChatActivityEnterView.this.waitingForKeyboardOpen) && (!ChatActivityEnterView.this.keyboardVisible) && (!AndroidUtilities.usingHardwareInput) && (!AndroidUtilities.isInMultiwindow))
      {
        ChatActivityEnterView.this.messageEditText.requestFocus();
        AndroidUtilities.showKeyboard(ChatActivityEnterView.this.messageEditText);
        AndroidUtilities.cancelRunOnUIThread(ChatActivityEnterView.this.openKeyboardRunnable);
        AndroidUtilities.runOnUIThread(ChatActivityEnterView.this.openKeyboardRunnable, 100L);
      }
    }
  };
  private Paint paint = new Paint(1);
  private Paint paintRecord = new Paint(1);
  private Activity parentActivity;
  private ChatActivity parentFragment;
  private Drawable pauseDrawable;
  private TLRPC.KeyboardButton pendingLocationButton;
  private MessageObject pendingMessageObject;
  private Drawable playDrawable;
  private CloseProgressDrawable2 progressDrawable;
  private Runnable recordAudioVideoRunnable = new Runnable()
  {
    public void run()
    {
      if ((ChatActivityEnterView.this.delegate == null) || (ChatActivityEnterView.this.parentActivity == null)) {
        return;
      }
      ChatActivityEnterView.this.delegate.onPreAudioVideoRecord();
      ChatActivityEnterView.access$1502(ChatActivityEnterView.this, true);
      ChatActivityEnterView.access$1602(ChatActivityEnterView.this, false);
      ChatActivityEnterView.this.recordCircle.setLockTranslation(10000.0F);
      ChatActivityEnterView.this.recordSendText.setAlpha(0.0F);
      ChatActivityEnterView.this.slideText.setAlpha(1.0F);
      ChatActivityEnterView.this.slideText.setTranslationY(0.0F);
      if ((ChatActivityEnterView.this.videoSendButton != null) && (ChatActivityEnterView.this.videoSendButton.getTag() != null))
      {
        if (Build.VERSION.SDK_INT >= 23)
        {
          int i;
          int j;
          label162:
          int k;
          label180:
          String[] arrayOfString;
          if (ChatActivityEnterView.this.parentActivity.checkSelfPermission("android.permission.RECORD_AUDIO") == 0)
          {
            i = 1;
            if (ChatActivityEnterView.this.parentActivity.checkSelfPermission("android.permission.CAMERA") != 0) {
              break label225;
            }
            j = 1;
            if ((i != 0) && (j != 0)) {
              break label257;
            }
            if ((i != 0) || (j != 0)) {
              break label230;
            }
            k = 2;
            arrayOfString = new String[k];
            if ((i != 0) || (j != 0)) {
              break label235;
            }
            arrayOfString[0] = "android.permission.RECORD_AUDIO";
            arrayOfString[1] = "android.permission.CAMERA";
          }
          for (;;)
          {
            ChatActivityEnterView.this.parentActivity.requestPermissions(arrayOfString, 3);
            return;
            i = 0;
            break;
            label225:
            j = 0;
            break label162;
            label230:
            k = 1;
            break label180;
            label235:
            if (i == 0) {
              arrayOfString[0] = "android.permission.RECORD_AUDIO";
            } else {
              arrayOfString[0] = "android.permission.CAMERA";
            }
          }
        }
        label257:
        ChatActivityEnterView.this.delegate.needStartRecordVideo(0);
        return;
      }
      if ((ChatActivityEnterView.this.parentFragment != null) && (Build.VERSION.SDK_INT >= 23) && (ChatActivityEnterView.this.parentActivity.checkSelfPermission("android.permission.RECORD_AUDIO") != 0))
      {
        ChatActivityEnterView.this.parentActivity.requestPermissions(new String[] { "android.permission.RECORD_AUDIO" }, 3);
        return;
      }
      ChatActivityEnterView.this.delegate.needStartRecordAudio(1);
      ChatActivityEnterView.access$2202(ChatActivityEnterView.this, -1.0F);
      MediaController.getInstance().startRecording(ChatActivityEnterView.this.currentAccount, ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.replyingMessageObject);
      ChatActivityEnterView.this.updateRecordIntefrace();
      ChatActivityEnterView.this.audioVideoButtonContainer.getParent().requestDisallowInterceptTouchEvent(true);
    }
  };
  private boolean recordAudioVideoRunnableStarted;
  private ImageView recordCancelImage;
  private TextView recordCancelText;
  private RecordCircle recordCircle;
  private ImageView recordDeleteImageView;
  private RecordDot recordDot;
  private int recordInterfaceState;
  private FrameLayout recordPanel;
  private TextView recordSendText;
  private LinearLayout recordTimeContainer;
  private TextView recordTimeText;
  private View recordedAudioBackground;
  private FrameLayout recordedAudioPanel;
  private ImageView recordedAudioPlayButton;
  private SeekBarWaveformView recordedAudioSeekBar;
  private TextView recordedAudioTimeTextView;
  private boolean recordingAudioVideo;
  private RectF rect = new RectF();
  private Paint redDotPaint = new Paint(1);
  private MessageObject replyingMessageObject;
  private Property<View, Integer> roundedTranslationYProperty = new Property(Integer.class, "translationY")
  {
    public Integer get(View paramAnonymousView)
    {
      return Integer.valueOf(Math.round(paramAnonymousView.getTranslationY()));
    }
    
    public void set(View paramAnonymousView, Integer paramAnonymousInteger)
    {
      paramAnonymousView.setTranslationY(paramAnonymousInteger.intValue());
    }
  };
  private AnimatorSet runningAnimation;
  private AnimatorSet runningAnimation2;
  private AnimatorSet runningAnimationAudio;
  private int runningAnimationType;
  private boolean searchingStickers;
  private SeekBarWaveform seekBarWaveform;
  private ImageView sendButton;
  private FrameLayout sendButtonContainer;
  private boolean sendByEnter;
  private Drawable sendDrawable;
  private boolean showKeyboardOnResume;
  private boolean silent;
  private SizeNotifierFrameLayout sizeNotifierLayout;
  private LinearLayout slideText;
  private float startedDraggingX = -1.0F;
  private AnimatedArrowDrawable stickersArrow;
  private boolean stickersDragging;
  private boolean stickersExpanded;
  private int stickersExpandedHeight;
  private Animator stickersExpansionAnim;
  private float stickersExpansionProgress;
  private boolean stickersTabOpen;
  private LinearLayout textFieldContainer;
  private View topView;
  private boolean topViewShowed;
  private Runnable updateExpandabilityRunnable = new Runnable()
  {
    private int lastKnownPage = -1;
    
    public void run()
    {
      boolean bool2;
      ChatActivityEnterView localChatActivityEnterView;
      if (ChatActivityEnterView.this.emojiView != null)
      {
        int i = ChatActivityEnterView.this.emojiView.getCurrentPage();
        if (i != this.lastKnownPage)
        {
          this.lastKnownPage = i;
          bool2 = ChatActivityEnterView.this.stickersTabOpen;
          localChatActivityEnterView = ChatActivityEnterView.this;
          if ((i != 1) && (i != 2)) {
            break label156;
          }
        }
      }
      label156:
      for (boolean bool1 = true;; bool1 = false)
      {
        ChatActivityEnterView.access$802(localChatActivityEnterView, bool1);
        if (bool2 != ChatActivityEnterView.this.stickersTabOpen) {
          ChatActivityEnterView.this.checkSendButton(true);
        }
        if ((!ChatActivityEnterView.this.stickersTabOpen) && (ChatActivityEnterView.this.stickersExpanded))
        {
          if (ChatActivityEnterView.this.searchingStickers)
          {
            ChatActivityEnterView.access$1102(ChatActivityEnterView.this, false);
            ChatActivityEnterView.this.emojiView.closeSearch(true);
            ChatActivityEnterView.this.emojiView.hideSearchKeyboard();
          }
          ChatActivityEnterView.this.setStickersExpanded(false, true);
        }
        return;
      }
    }
  };
  private ImageView videoSendButton;
  private VideoTimelineView videoTimelineView;
  private VideoEditedInfo videoToSendMessageObject;
  private boolean waitingForKeyboardOpen;
  private PowerManager.WakeLock wakeLock;
  
  public ChatActivityEnterView(Activity paramActivity, SizeNotifierFrameLayout paramSizeNotifierFrameLayout, ChatActivity paramChatActivity, boolean paramBoolean)
  {
    super(paramActivity);
    this.dotPaint.setColor(Theme.getColor("chat_emojiPanelNewTrending"));
    setFocusable(true);
    setFocusableInTouchMode(true);
    setWillNotDraw(false);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordStarted);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordStartError);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordStopped);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordProgressChanged);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.audioDidSent);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.audioRouteChanged);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingDidReset);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.featuredStickersDidLoaded);
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
    this.parentActivity = paramActivity;
    this.parentFragment = paramChatActivity;
    this.sizeNotifierLayout = paramSizeNotifierFrameLayout;
    this.sizeNotifierLayout.setDelegate(this);
    this.sendByEnter = MessagesController.getGlobalMainSettings().getBoolean("send_by_enter", false);
    this.textFieldContainer = new LinearLayout(paramActivity);
    this.textFieldContainer.setOrientation(0);
    addView(this.textFieldContainer, LayoutHelper.createFrame(-1, -2.0F, 51, 0.0F, 2.0F, 0.0F, 0.0F));
    paramSizeNotifierFrameLayout = new FrameLayout(paramActivity);
    this.textFieldContainer.addView(paramSizeNotifierFrameLayout, LayoutHelper.createLinear(0, -2, 1.0F));
    this.emojiButton = new ImageView(paramActivity)
    {
      protected void onDraw(Canvas paramAnonymousCanvas)
      {
        super.onDraw(paramAnonymousCanvas);
        if ((ChatActivityEnterView.this.attachLayout != null) && ((ChatActivityEnterView.this.emojiView == null) || (ChatActivityEnterView.this.emojiView.getVisibility() != 0)) && (!DataQuery.getInstance(ChatActivityEnterView.this.currentAccount).getUnreadStickerSets().isEmpty()) && (ChatActivityEnterView.this.dotPaint != null))
        {
          int i = paramAnonymousCanvas.getWidth() / 2;
          int j = AndroidUtilities.dp(9.0F);
          int k = paramAnonymousCanvas.getHeight() / 2;
          int m = AndroidUtilities.dp(8.0F);
          paramAnonymousCanvas.drawCircle(i + j, k - m, AndroidUtilities.dp(5.0F), ChatActivityEnterView.this.dotPaint);
        }
      }
    };
    this.emojiButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelIcons"), PorterDuff.Mode.MULTIPLY));
    this.emojiButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    this.emojiButton.setPadding(0, AndroidUtilities.dp(1.0F), 0, 0);
    setEmojiButtonImage();
    paramSizeNotifierFrameLayout.addView(this.emojiButton, LayoutHelper.createFrame(48, 48.0F, 83, 3.0F, 0.0F, 0.0F, 0.0F));
    this.emojiButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        boolean bool = true;
        if ((!ChatActivityEnterView.this.isPopupShowing()) || (ChatActivityEnterView.this.currentPopupContentType != 0))
        {
          ChatActivityEnterView.this.showPopup(1, 0);
          paramAnonymousView = ChatActivityEnterView.this.emojiView;
          if ((ChatActivityEnterView.this.messageEditText.length() > 0) && (!ChatActivityEnterView.this.messageEditText.getText().toString().startsWith("@gif"))) {}
          for (;;)
          {
            paramAnonymousView.onOpen(bool);
            return;
            bool = false;
          }
        }
        if (ChatActivityEnterView.this.searchingStickers)
        {
          ChatActivityEnterView.access$1102(ChatActivityEnterView.this, false);
          ChatActivityEnterView.this.emojiView.closeSearch(false);
          ChatActivityEnterView.this.messageEditText.requestFocus();
        }
        ChatActivityEnterView.this.openKeyboardInternal();
        ChatActivityEnterView.this.removeGifFromInputField();
      }
    });
    this.messageEditText = new EditTextCaption(paramActivity)
    {
      public InputConnection onCreateInputConnection(EditorInfo paramAnonymousEditorInfo)
      {
        InputConnection localInputConnection = super.onCreateInputConnection(paramAnonymousEditorInfo);
        EditorInfoCompat.setContentMimeTypes(paramAnonymousEditorInfo, new String[] { "image/gif", "image/*", "image/jpg", "image/png" });
        InputConnectionCompat.createWrapper(localInputConnection, paramAnonymousEditorInfo, new InputConnectionCompat.OnCommitContentListener()
        {
          public boolean onCommitContent(InputContentInfoCompat paramAnonymous2InputContentInfoCompat, int paramAnonymous2Int, Bundle paramAnonymous2Bundle)
          {
            if ((BuildCompat.isAtLeastNMR1()) && ((InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION & paramAnonymous2Int) != 0)) {}
            for (;;)
            {
              try
              {
                paramAnonymous2InputContentInfoCompat.requestPermission();
                if (paramAnonymous2InputContentInfoCompat.getDescription().hasMimeType("image/gif"))
                {
                  SendMessagesHelper.prepareSendingDocument(null, null, paramAnonymous2InputContentInfoCompat.getContentUri(), "image/gif", ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.replyingMessageObject, paramAnonymous2InputContentInfoCompat);
                  if (ChatActivityEnterView.this.delegate != null) {
                    ChatActivityEnterView.this.delegate.onMessageSend(null);
                  }
                  return true;
                }
              }
              catch (Exception paramAnonymous2InputContentInfoCompat)
              {
                return false;
              }
              SendMessagesHelper.prepareSendingPhoto(null, paramAnonymous2InputContentInfoCompat.getContentUri(), ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.replyingMessageObject, null, null, null, paramAnonymous2InputContentInfoCompat, 0);
            }
          }
        });
      }
      
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        ChatActivityEnterView localChatActivityEnterView;
        if ((ChatActivityEnterView.this.isPopupShowing()) && (paramAnonymousMotionEvent.getAction() == 0))
        {
          if (ChatActivityEnterView.this.searchingStickers)
          {
            ChatActivityEnterView.access$1102(ChatActivityEnterView.this, false);
            ChatActivityEnterView.this.emojiView.closeSearch(false);
          }
          localChatActivityEnterView = ChatActivityEnterView.this;
          if (!AndroidUtilities.usingHardwareInput) {
            break label83;
          }
        }
        for (int i = 0;; i = 2)
        {
          localChatActivityEnterView.showPopup(i, 0);
          ChatActivityEnterView.this.openKeyboardInternal();
          try
          {
            boolean bool = super.onTouchEvent(paramAnonymousMotionEvent);
            return bool;
          }
          catch (Exception paramAnonymousMotionEvent)
          {
            label83:
            FileLog.e(paramAnonymousMotionEvent);
          }
        }
        return false;
      }
    };
    updateFieldHint();
    int j = 268435456;
    int i = j;
    if (this.parentFragment != null)
    {
      i = j;
      if (this.parentFragment.getCurrentEncryptedChat() != null) {
        i = 0x10000000 | 0x1000000;
      }
    }
    this.messageEditText.setImeOptions(i);
    this.messageEditText.setInputType(this.messageEditText.getInputType() | 0x4000 | 0x20000);
    this.messageEditText.setSingleLine(false);
    this.messageEditText.setMaxLines(4);
    this.messageEditText.setTextSize(1, 18.0F);
    this.messageEditText.setGravity(80);
    this.messageEditText.setPadding(0, AndroidUtilities.dp(11.0F), 0, AndroidUtilities.dp(12.0F));
    this.messageEditText.setBackgroundDrawable(null);
    this.messageEditText.setTextColor(Theme.getColor("chat_messagePanelText"));
    this.messageEditText.setHintColor(Theme.getColor("chat_messagePanelHint"));
    this.messageEditText.setHintTextColor(Theme.getColor("chat_messagePanelHint"));
    paramChatActivity = this.messageEditText;
    float f;
    if (paramBoolean)
    {
      f = 50.0F;
      paramSizeNotifierFrameLayout.addView(paramChatActivity, LayoutHelper.createFrame(-1, -2.0F, 80, 52.0F, 0.0F, f, 0.0F));
      this.messageEditText.setOnKeyListener(new View.OnKeyListener()
      {
        boolean ctrlPressed = false;
        
        public boolean onKey(View paramAnonymousView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          boolean bool = false;
          if ((paramAnonymousInt == 4) && (!ChatActivityEnterView.this.keyboardVisible) && (ChatActivityEnterView.this.isPopupShowing()))
          {
            if (paramAnonymousKeyEvent.getAction() == 1)
            {
              if ((ChatActivityEnterView.this.currentPopupContentType == 1) && (ChatActivityEnterView.this.botButtonsMessageObject != null)) {
                MessagesController.getMainSettings(ChatActivityEnterView.this.currentAccount).edit().putInt("hidekeyboard_" + ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.botButtonsMessageObject.getId()).commit();
              }
              if (ChatActivityEnterView.this.searchingStickers)
              {
                ChatActivityEnterView.access$1102(ChatActivityEnterView.this, false);
                ChatActivityEnterView.this.emojiView.closeSearch(true);
                ChatActivityEnterView.this.messageEditText.requestFocus();
              }
            }
            else
            {
              return true;
            }
            ChatActivityEnterView.this.showPopup(0, 0);
            ChatActivityEnterView.this.removeGifFromInputField();
            return true;
          }
          if ((paramAnonymousInt == 66) && ((this.ctrlPressed) || (ChatActivityEnterView.this.sendByEnter)) && (paramAnonymousKeyEvent.getAction() == 0) && (ChatActivityEnterView.this.editingMessageObject == null))
          {
            ChatActivityEnterView.this.sendMessage();
            return true;
          }
          if ((paramAnonymousInt == 113) || (paramAnonymousInt == 114))
          {
            if (paramAnonymousKeyEvent.getAction() == 0) {
              bool = true;
            }
            this.ctrlPressed = bool;
            return true;
          }
          return false;
        }
      });
      this.messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
      {
        boolean ctrlPressed = false;
        
        public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          boolean bool = false;
          if (paramAnonymousInt == 4)
          {
            ChatActivityEnterView.this.sendMessage();
            return true;
          }
          if ((paramAnonymousKeyEvent != null) && (paramAnonymousInt == 0))
          {
            if (((this.ctrlPressed) || (ChatActivityEnterView.this.sendByEnter)) && (paramAnonymousKeyEvent.getAction() == 0) && (ChatActivityEnterView.this.editingMessageObject == null))
            {
              ChatActivityEnterView.this.sendMessage();
              return true;
            }
            if ((paramAnonymousInt == 113) || (paramAnonymousInt == 114))
            {
              if (paramAnonymousKeyEvent.getAction() == 0) {
                bool = true;
              }
              this.ctrlPressed = bool;
              return true;
            }
          }
          return false;
        }
      });
      this.messageEditText.addTextChangedListener(new TextWatcher()
      {
        boolean processChange = false;
        
        public void afterTextChanged(Editable paramAnonymousEditable)
        {
          if (ChatActivityEnterView.this.innerTextChange != 0) {}
          do
          {
            return;
            if ((ChatActivityEnterView.this.sendByEnter) && (paramAnonymousEditable.length() > 0) && (paramAnonymousEditable.charAt(paramAnonymousEditable.length() - 1) == '\n') && (ChatActivityEnterView.this.editingMessageObject == null)) {
              ChatActivityEnterView.this.sendMessage();
            }
          } while (!this.processChange);
          ImageSpan[] arrayOfImageSpan = (ImageSpan[])paramAnonymousEditable.getSpans(0, paramAnonymousEditable.length(), ImageSpan.class);
          int i = 0;
          while (i < arrayOfImageSpan.length)
          {
            paramAnonymousEditable.removeSpan(arrayOfImageSpan[i]);
            i += 1;
          }
          Emoji.replaceEmoji(paramAnonymousEditable, ChatActivityEnterView.this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
          this.processChange = false;
        }
        
        public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
        {
          if (ChatActivityEnterView.this.innerTextChange == 1) {
            return;
          }
          ChatActivityEnterView.this.checkSendButton(true);
          CharSequence localCharSequence = AndroidUtilities.getTrimmedString(paramAnonymousCharSequence.toString());
          ChatActivityEnterView.ChatActivityEnterViewDelegate localChatActivityEnterViewDelegate;
          if ((ChatActivityEnterView.this.delegate != null) && (!ChatActivityEnterView.this.ignoreTextChange))
          {
            if ((paramAnonymousInt3 > 2) || (paramAnonymousCharSequence == null) || (paramAnonymousCharSequence.length() == 0)) {
              ChatActivityEnterView.access$5202(ChatActivityEnterView.this, true);
            }
            localChatActivityEnterViewDelegate = ChatActivityEnterView.this.delegate;
            if ((paramAnonymousInt2 <= paramAnonymousInt3 + 1) && (paramAnonymousInt3 - paramAnonymousInt2 <= 2)) {
              break label359;
            }
          }
          label359:
          for (boolean bool = true;; bool = false)
          {
            localChatActivityEnterViewDelegate.onTextChanged(paramAnonymousCharSequence, bool);
            if ((ChatActivityEnterView.this.innerTextChange != 2) && (paramAnonymousInt2 != paramAnonymousInt3) && (paramAnonymousInt3 - paramAnonymousInt2 > 1)) {
              this.processChange = true;
            }
            if ((ChatActivityEnterView.this.editingMessageObject != null) || (ChatActivityEnterView.this.canWriteToChannel) || (localCharSequence.length() == 0) || (ChatActivityEnterView.this.lastTypingTimeSend >= System.currentTimeMillis() - 5000L) || (ChatActivityEnterView.this.ignoreTextChange)) {
              break;
            }
            paramAnonymousInt1 = ConnectionsManager.getInstance(ChatActivityEnterView.this.currentAccount).getCurrentTime();
            paramAnonymousCharSequence = null;
            if ((int)ChatActivityEnterView.this.dialog_id > 0) {
              paramAnonymousCharSequence = MessagesController.getInstance(ChatActivityEnterView.this.currentAccount).getUser(Integer.valueOf((int)ChatActivityEnterView.this.dialog_id));
            }
            if ((paramAnonymousCharSequence != null) && ((paramAnonymousCharSequence.id == UserConfig.getInstance(ChatActivityEnterView.this.currentAccount).getClientUserId()) || ((paramAnonymousCharSequence.status != null) && (paramAnonymousCharSequence.status.expires < paramAnonymousInt1) && (!MessagesController.getInstance(ChatActivityEnterView.this.currentAccount).onlinePrivacy.containsKey(Integer.valueOf(paramAnonymousCharSequence.id)))))) {
              break;
            }
            ChatActivityEnterView.access$5402(ChatActivityEnterView.this, System.currentTimeMillis());
            if (ChatActivityEnterView.this.delegate == null) {
              break;
            }
            ChatActivityEnterView.this.delegate.needSendTyping();
            return;
          }
        }
      });
      if (paramBoolean)
      {
        this.attachLayout = new LinearLayout(paramActivity);
        this.attachLayout.setOrientation(0);
        this.attachLayout.setEnabled(false);
        this.attachLayout.setPivotX(AndroidUtilities.dp(48.0F));
        paramSizeNotifierFrameLayout.addView(this.attachLayout, LayoutHelper.createFrame(-2, 48, 85));
        this.botButton = new ImageView(paramActivity);
        this.botButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelIcons"), PorterDuff.Mode.MULTIPLY));
        this.botButton.setImageResource(2131165245);
        this.botButton.setScaleType(ImageView.ScaleType.CENTER);
        this.botButton.setVisibility(8);
        this.attachLayout.addView(this.botButton, LayoutHelper.createLinear(48, 48));
        this.botButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (ChatActivityEnterView.this.searchingStickers)
            {
              ChatActivityEnterView.access$1102(ChatActivityEnterView.this, false);
              ChatActivityEnterView.this.emojiView.closeSearch(false);
              ChatActivityEnterView.this.messageEditText.requestFocus();
            }
            if (ChatActivityEnterView.this.botReplyMarkup != null) {
              if ((!ChatActivityEnterView.this.isPopupShowing()) || (ChatActivityEnterView.this.currentPopupContentType != 1))
              {
                ChatActivityEnterView.this.showPopup(1, 1);
                MessagesController.getMainSettings(ChatActivityEnterView.this.currentAccount).edit().remove("hidekeyboard_" + ChatActivityEnterView.this.dialog_id).commit();
              }
            }
            for (;;)
            {
              if (ChatActivityEnterView.this.stickersExpanded) {
                ChatActivityEnterView.this.setStickersExpanded(false, false);
              }
              return;
              if ((ChatActivityEnterView.this.currentPopupContentType == 1) && (ChatActivityEnterView.this.botButtonsMessageObject != null)) {
                MessagesController.getMainSettings(ChatActivityEnterView.this.currentAccount).edit().putInt("hidekeyboard_" + ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.botButtonsMessageObject.getId()).commit();
              }
              ChatActivityEnterView.this.openKeyboardInternal();
              continue;
              if (ChatActivityEnterView.this.hasBotCommands)
              {
                ChatActivityEnterView.this.setFieldText("/");
                ChatActivityEnterView.this.messageEditText.requestFocus();
                ChatActivityEnterView.this.openKeyboard();
              }
            }
          }
        });
        this.notifyButton = new ImageView(paramActivity);
        paramChatActivity = this.notifyButton;
        if (!this.silent) {
          break label3252;
        }
        i = 2131165560;
        label1005:
        paramChatActivity.setImageResource(i);
        this.notifyButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelIcons"), PorterDuff.Mode.MULTIPLY));
        this.notifyButton.setScaleType(ImageView.ScaleType.CENTER);
        paramChatActivity = this.notifyButton;
        if (!this.canWriteToChannel) {
          break label3260;
        }
        i = 0;
        label1059:
        paramChatActivity.setVisibility(i);
        this.attachLayout.addView(this.notifyButton, LayoutHelper.createLinear(48, 48));
        this.notifyButton.setOnClickListener(new View.OnClickListener()
        {
          private Toast visibleToast;
          
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = ChatActivityEnterView.this;
            boolean bool;
            if (!ChatActivityEnterView.this.silent) {
              bool = true;
            }
            for (;;)
            {
              ChatActivityEnterView.access$5702(paramAnonymousView, bool);
              paramAnonymousView = ChatActivityEnterView.this.notifyButton;
              int i;
              if (ChatActivityEnterView.this.silent)
              {
                i = 2131165560;
                paramAnonymousView.setImageResource(i);
                MessagesController.getNotificationsSettings(ChatActivityEnterView.this.currentAccount).edit().putBoolean("silent_" + ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.silent).commit();
                NotificationsController.getInstance(ChatActivityEnterView.this.currentAccount).updateServerNotificationsSettings(ChatActivityEnterView.this.dialog_id);
              }
              try
              {
                if (this.visibleToast != null) {
                  this.visibleToast.cancel();
                }
                if (ChatActivityEnterView.this.silent)
                {
                  this.visibleToast = Toast.makeText(ChatActivityEnterView.this.parentActivity, LocaleController.getString("ChannelNotifyMembersInfoOff", 2131493194), 0);
                  this.visibleToast.show();
                  ChatActivityEnterView.this.updateFieldHint();
                  return;
                  bool = false;
                  continue;
                  i = 2131165561;
                }
              }
              catch (Exception paramAnonymousView)
              {
                for (;;)
                {
                  FileLog.e(paramAnonymousView);
                  continue;
                  this.visibleToast = Toast.makeText(ChatActivityEnterView.this.parentActivity, LocaleController.getString("ChannelNotifyMembersInfoOn", 2131493195), 0);
                  this.visibleToast.show();
                }
              }
            }
          }
        });
        this.attachButton = new ImageView(paramActivity);
        this.attachButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelIcons"), PorterDuff.Mode.MULTIPLY));
        this.attachButton.setImageResource(2131165345);
        this.attachButton.setScaleType(ImageView.ScaleType.CENTER);
        this.attachLayout.addView(this.attachButton, LayoutHelper.createLinear(48, 48));
        this.attachButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            ChatActivityEnterView.this.delegate.didPressedAttachButton();
          }
        });
      }
      this.recordedAudioPanel = new FrameLayout(paramActivity);
      paramChatActivity = this.recordedAudioPanel;
      if (this.audioToSend != null) {
        break label3267;
      }
    }
    label3252:
    label3260:
    label3267:
    for (i = 8;; i = 0)
    {
      paramChatActivity.setVisibility(i);
      this.recordedAudioPanel.setBackgroundColor(Theme.getColor("chat_messagePanelBackground"));
      this.recordedAudioPanel.setFocusable(true);
      this.recordedAudioPanel.setFocusableInTouchMode(true);
      this.recordedAudioPanel.setClickable(true);
      paramSizeNotifierFrameLayout.addView(this.recordedAudioPanel, LayoutHelper.createFrame(-1, 48, 80));
      this.recordDeleteImageView = new ImageView(paramActivity);
      this.recordDeleteImageView.setScaleType(ImageView.ScaleType.CENTER);
      this.recordDeleteImageView.setImageResource(2131165348);
      this.recordDeleteImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelVoiceDelete"), PorterDuff.Mode.MULTIPLY));
      this.recordedAudioPanel.addView(this.recordDeleteImageView, LayoutHelper.createFrame(48, 48.0F));
      this.recordDeleteImageView.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ChatActivityEnterView.this.videoToSendMessageObject != null) {
            ChatActivityEnterView.this.delegate.needStartRecordVideo(2);
          }
          for (;;)
          {
            if (ChatActivityEnterView.this.audioToSendPath != null) {
              new File(ChatActivityEnterView.this.audioToSendPath).delete();
            }
            ChatActivityEnterView.this.hideRecordedAudioPanel();
            ChatActivityEnterView.this.checkSendButton(true);
            return;
            paramAnonymousView = MediaController.getInstance().getPlayingMessageObject();
            if ((paramAnonymousView != null) && (paramAnonymousView == ChatActivityEnterView.this.audioToSendMessageObject)) {
              MediaController.getInstance().cleanupPlayer(true, true);
            }
          }
        }
      });
      this.videoTimelineView = new VideoTimelineView(paramActivity);
      this.videoTimelineView.setColor(-11817481);
      this.videoTimelineView.setRoundFrames(true);
      this.videoTimelineView.setDelegate(new VideoTimelineView.VideoTimelineViewDelegate()
      {
        public void didStartDragging()
        {
          ChatActivityEnterView.this.delegate.needChangeVideoPreviewState(1, 0.0F);
        }
        
        public void didStopDragging()
        {
          ChatActivityEnterView.this.delegate.needChangeVideoPreviewState(0, 0.0F);
        }
        
        public void onLeftProgressChanged(float paramAnonymousFloat)
        {
          if (ChatActivityEnterView.this.videoToSendMessageObject == null) {
            return;
          }
          ChatActivityEnterView.this.videoToSendMessageObject.startTime = (((float)ChatActivityEnterView.this.videoToSendMessageObject.estimatedDuration * paramAnonymousFloat));
          ChatActivityEnterView.this.delegate.needChangeVideoPreviewState(2, paramAnonymousFloat);
        }
        
        public void onRightProgressChanged(float paramAnonymousFloat)
        {
          if (ChatActivityEnterView.this.videoToSendMessageObject == null) {
            return;
          }
          ChatActivityEnterView.this.videoToSendMessageObject.endTime = (((float)ChatActivityEnterView.this.videoToSendMessageObject.estimatedDuration * paramAnonymousFloat));
          ChatActivityEnterView.this.delegate.needChangeVideoPreviewState(2, paramAnonymousFloat);
        }
      });
      this.recordedAudioPanel.addView(this.videoTimelineView, LayoutHelper.createFrame(-1, 32.0F, 19, 40.0F, 0.0F, 0.0F, 0.0F));
      this.recordedAudioBackground = new View(paramActivity);
      this.recordedAudioBackground.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(16.0F), Theme.getColor("chat_recordedVoiceBackground")));
      this.recordedAudioPanel.addView(this.recordedAudioBackground, LayoutHelper.createFrame(-1, 36.0F, 19, 48.0F, 0.0F, 0.0F, 0.0F));
      this.recordedAudioSeekBar = new SeekBarWaveformView(paramActivity);
      this.recordedAudioPanel.addView(this.recordedAudioSeekBar, LayoutHelper.createFrame(-1, 32.0F, 19, 92.0F, 0.0F, 52.0F, 0.0F));
      this.playDrawable = Theme.createSimpleSelectorDrawable(paramActivity, 2131165626, Theme.getColor("chat_recordedVoicePlayPause"), Theme.getColor("chat_recordedVoicePlayPausePressed"));
      this.pauseDrawable = Theme.createSimpleSelectorDrawable(paramActivity, 2131165625, Theme.getColor("chat_recordedVoicePlayPause"), Theme.getColor("chat_recordedVoicePlayPausePressed"));
      this.recordedAudioPlayButton = new ImageView(paramActivity);
      this.recordedAudioPlayButton.setImageDrawable(this.playDrawable);
      this.recordedAudioPlayButton.setScaleType(ImageView.ScaleType.CENTER);
      this.recordedAudioPanel.addView(this.recordedAudioPlayButton, LayoutHelper.createFrame(48, 48.0F, 83, 48.0F, 0.0F, 0.0F, 0.0F));
      this.recordedAudioPlayButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ChatActivityEnterView.this.audioToSend == null) {
            return;
          }
          if ((MediaController.getInstance().isPlayingMessage(ChatActivityEnterView.this.audioToSendMessageObject)) && (!MediaController.getInstance().isMessagePaused()))
          {
            MediaController.getInstance().pauseMessage(ChatActivityEnterView.this.audioToSendMessageObject);
            ChatActivityEnterView.this.recordedAudioPlayButton.setImageDrawable(ChatActivityEnterView.this.playDrawable);
            return;
          }
          ChatActivityEnterView.this.recordedAudioPlayButton.setImageDrawable(ChatActivityEnterView.this.pauseDrawable);
          MediaController.getInstance().playMessage(ChatActivityEnterView.this.audioToSendMessageObject);
        }
      });
      this.recordedAudioTimeTextView = new TextView(paramActivity);
      this.recordedAudioTimeTextView.setTextColor(Theme.getColor("chat_messagePanelVoiceDuration"));
      this.recordedAudioTimeTextView.setTextSize(1, 13.0F);
      this.recordedAudioPanel.addView(this.recordedAudioTimeTextView, LayoutHelper.createFrame(-2, -2.0F, 21, 0.0F, 0.0F, 13.0F, 0.0F));
      this.recordPanel = new FrameLayout(paramActivity);
      this.recordPanel.setVisibility(8);
      this.recordPanel.setBackgroundColor(Theme.getColor("chat_messagePanelBackground"));
      paramSizeNotifierFrameLayout.addView(this.recordPanel, LayoutHelper.createFrame(-1, 48, 80));
      this.recordPanel.setOnTouchListener(new View.OnTouchListener()
      {
        public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
        {
          return true;
        }
      });
      this.slideText = new LinearLayout(paramActivity);
      this.slideText.setOrientation(0);
      this.recordPanel.addView(this.slideText, LayoutHelper.createFrame(-2, -2.0F, 17, 30.0F, 0.0F, 0.0F, 0.0F));
      this.recordCancelImage = new ImageView(paramActivity);
      this.recordCancelImage.setImageResource(2131165645);
      this.recordCancelImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_recordVoiceCancel"), PorterDuff.Mode.MULTIPLY));
      this.slideText.addView(this.recordCancelImage, LayoutHelper.createLinear(-2, -2, 16, 0, 1, 0, 0));
      this.recordCancelText = new TextView(paramActivity);
      this.recordCancelText.setText(LocaleController.getString("SlideToCancel", 2131494408));
      this.recordCancelText.setTextColor(Theme.getColor("chat_recordVoiceCancel"));
      this.recordCancelText.setTextSize(1, 12.0F);
      this.slideText.addView(this.recordCancelText, LayoutHelper.createLinear(-2, -2, 16, 6, 0, 0, 0));
      this.recordSendText = new TextView(paramActivity);
      this.recordSendText.setText(LocaleController.getString("Cancel", 2131493127).toUpperCase());
      this.recordSendText.setTextColor(Theme.getColor("chat_fieldOverlayText"));
      this.recordSendText.setTextSize(1, 16.0F);
      this.recordSendText.setGravity(17);
      this.recordSendText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.recordSendText.setAlpha(0.0F);
      this.recordSendText.setPadding(AndroidUtilities.dp(36.0F), 0, 0, 0);
      this.recordSendText.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if ((ChatActivityEnterView.this.hasRecordVideo) && (ChatActivityEnterView.this.videoSendButton.getTag() != null)) {
            ChatActivityEnterView.this.delegate.needStartRecordVideo(2);
          }
          for (;;)
          {
            ChatActivityEnterView.access$6802(ChatActivityEnterView.this, false);
            ChatActivityEnterView.this.updateRecordIntefrace();
            return;
            ChatActivityEnterView.this.delegate.needStartRecordAudio(0);
            MediaController.getInstance().stopRecording(0);
          }
        }
      });
      this.recordPanel.addView(this.recordSendText, LayoutHelper.createFrame(-2, -1.0F, 49, 0.0F, 0.0F, 0.0F, 0.0F));
      this.recordTimeContainer = new LinearLayout(paramActivity);
      this.recordTimeContainer.setOrientation(0);
      this.recordTimeContainer.setPadding(AndroidUtilities.dp(13.0F), 0, 0, 0);
      this.recordTimeContainer.setBackgroundColor(Theme.getColor("chat_messagePanelBackground"));
      this.recordPanel.addView(this.recordTimeContainer, LayoutHelper.createFrame(-2, -2, 16));
      this.recordDot = new RecordDot(paramActivity);
      this.recordTimeContainer.addView(this.recordDot, LayoutHelper.createLinear(11, 11, 16, 0, 1, 0, 0));
      this.recordTimeText = new TextView(paramActivity);
      this.recordTimeText.setTextColor(Theme.getColor("chat_recordTime"));
      this.recordTimeText.setTextSize(1, 16.0F);
      this.recordTimeContainer.addView(this.recordTimeText, LayoutHelper.createLinear(-2, -2, 16, 6, 0, 0, 0));
      this.sendButtonContainer = new FrameLayout(paramActivity);
      this.textFieldContainer.addView(this.sendButtonContainer, LayoutHelper.createLinear(48, 48, 80));
      this.audioVideoButtonContainer = new FrameLayout(paramActivity);
      this.audioVideoButtonContainer.setBackgroundColor(Theme.getColor("chat_messagePanelBackground"));
      this.audioVideoButtonContainer.setSoundEffectsEnabled(false);
      this.sendButtonContainer.addView(this.audioVideoButtonContainer, LayoutHelper.createFrame(48, 48.0F));
      this.audioVideoButtonContainer.setOnTouchListener(new View.OnTouchListener()
      {
        public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
        {
          label104:
          Object localObject;
          if (paramAnonymousMotionEvent.getAction() == 0)
          {
            if (ChatActivityEnterView.this.recordCircle.isSendButtonVisible())
            {
              if ((!ChatActivityEnterView.this.hasRecordVideo) || (ChatActivityEnterView.this.calledRecordRunnable))
              {
                ChatActivityEnterView.access$2202(ChatActivityEnterView.this, -1.0F);
                if ((!ChatActivityEnterView.this.hasRecordVideo) || (ChatActivityEnterView.this.videoSendButton.getTag() == null)) {
                  break label104;
                }
                ChatActivityEnterView.this.delegate.needStartRecordVideo(1);
              }
              for (;;)
              {
                ChatActivityEnterView.access$6802(ChatActivityEnterView.this, false);
                ChatActivityEnterView.this.updateRecordIntefrace();
                return false;
                ChatActivityEnterView.this.delegate.needStartRecordAudio(0);
                MediaController.getInstance().stopRecording(1);
              }
            }
            if (ChatActivityEnterView.this.parentFragment != null)
            {
              localObject = ChatActivityEnterView.this.parentFragment.getCurrentChat();
              if ((ChatObject.isChannel((TLRPC.Chat)localObject)) && (((TLRPC.Chat)localObject).banned_rights != null) && (((TLRPC.Chat)localObject).banned_rights.send_media))
              {
                ChatActivityEnterView.this.delegate.needShowMediaBanHint();
                return false;
              }
            }
            if (ChatActivityEnterView.this.hasRecordVideo)
            {
              ChatActivityEnterView.access$1502(ChatActivityEnterView.this, false);
              ChatActivityEnterView.access$1602(ChatActivityEnterView.this, true);
              AndroidUtilities.runOnUIThread(ChatActivityEnterView.this.recordAudioVideoRunnable, 150L);
            }
          }
          label343:
          label391:
          label789:
          label906:
          label1126:
          label1141:
          label1175:
          for (;;)
          {
            paramAnonymousView.onTouchEvent(paramAnonymousMotionEvent);
            return true;
            ChatActivityEnterView.this.recordAudioVideoRunnable.run();
            continue;
            if ((paramAnonymousMotionEvent.getAction() == 1) || (paramAnonymousMotionEvent.getAction() == 3))
            {
              if ((ChatActivityEnterView.this.recordCircle.isSendButtonVisible()) || (ChatActivityEnterView.this.recordedAudioPanel.getVisibility() == 0)) {
                return false;
              }
              if (ChatActivityEnterView.this.recordAudioVideoRunnableStarted)
              {
                AndroidUtilities.cancelRunOnUIThread(ChatActivityEnterView.this.recordAudioVideoRunnable);
                localObject = ChatActivityEnterView.this.delegate;
                if (ChatActivityEnterView.this.videoSendButton.getTag() == null)
                {
                  bool = true;
                  ((ChatActivityEnterView.ChatActivityEnterViewDelegate)localObject).onSwitchRecordMode(bool);
                  localObject = ChatActivityEnterView.this;
                  if (ChatActivityEnterView.this.videoSendButton.getTag() != null) {
                    break label391;
                  }
                }
                for (boolean bool = true;; bool = false)
                {
                  ((ChatActivityEnterView)localObject).setRecordVideoButtonVisible(bool, true);
                  break;
                  bool = false;
                  break label343;
                }
              }
              if ((!ChatActivityEnterView.this.hasRecordVideo) || (ChatActivityEnterView.this.calledRecordRunnable))
              {
                ChatActivityEnterView.access$2202(ChatActivityEnterView.this, -1.0F);
                if ((ChatActivityEnterView.this.hasRecordVideo) && (ChatActivityEnterView.this.videoSendButton.getTag() != null)) {
                  ChatActivityEnterView.this.delegate.needStartRecordVideo(1);
                }
                for (;;)
                {
                  ChatActivityEnterView.access$6802(ChatActivityEnterView.this, false);
                  ChatActivityEnterView.this.updateRecordIntefrace();
                  break;
                  ChatActivityEnterView.this.delegate.needStartRecordAudio(0);
                  MediaController.getInstance().stopRecording(1);
                }
              }
            }
            else if ((paramAnonymousMotionEvent.getAction() == 2) && (ChatActivityEnterView.this.recordingAudioVideo))
            {
              float f1 = paramAnonymousMotionEvent.getX();
              float f2 = paramAnonymousMotionEvent.getY();
              if (ChatActivityEnterView.this.recordCircle.isSendButtonVisible()) {
                return false;
              }
              if (ChatActivityEnterView.this.recordCircle.setLockTranslation(f2) == 2)
              {
                paramAnonymousView = new AnimatorSet();
                paramAnonymousView.playTogether(new Animator[] { ObjectAnimator.ofFloat(ChatActivityEnterView.this.recordCircle, "lockAnimatedTranslation", new float[] { ChatActivityEnterView.RecordCircle.access$7200(ChatActivityEnterView.this.recordCircle) }), ObjectAnimator.ofFloat(ChatActivityEnterView.this.slideText, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(ChatActivityEnterView.this.slideText, "translationY", new float[] { AndroidUtilities.dp(20.0F) }), ObjectAnimator.ofFloat(ChatActivityEnterView.this.recordSendText, "alpha", new float[] { 1.0F }), ObjectAnimator.ofFloat(ChatActivityEnterView.this.recordSendText, "translationY", new float[] { -AndroidUtilities.dp(20.0F), 0.0F }) });
                paramAnonymousView.setInterpolator(new DecelerateInterpolator());
                paramAnonymousView.setDuration(150L);
                paramAnonymousView.start();
                return false;
              }
              if (f1 < -ChatActivityEnterView.this.distCanMove)
              {
                if ((ChatActivityEnterView.this.hasRecordVideo) && (ChatActivityEnterView.this.videoSendButton.getTag() != null))
                {
                  ChatActivityEnterView.this.delegate.needStartRecordVideo(2);
                  ChatActivityEnterView.access$6802(ChatActivityEnterView.this, false);
                  ChatActivityEnterView.this.updateRecordIntefrace();
                }
              }
              else
              {
                float f3 = f1 + ChatActivityEnterView.this.audioVideoButtonContainer.getX();
                localObject = (FrameLayout.LayoutParams)ChatActivityEnterView.this.slideText.getLayoutParams();
                if (ChatActivityEnterView.this.startedDraggingX != -1.0F)
                {
                  f1 = f3 - ChatActivityEnterView.this.startedDraggingX;
                  ((FrameLayout.LayoutParams)localObject).leftMargin = (AndroidUtilities.dp(30.0F) + (int)f1);
                  ChatActivityEnterView.this.slideText.setLayoutParams((ViewGroup.LayoutParams)localObject);
                  f2 = 1.0F + f1 / ChatActivityEnterView.this.distCanMove;
                  if (f2 <= 1.0F) {
                    break label1126;
                  }
                  f1 = 1.0F;
                  ChatActivityEnterView.this.slideText.setAlpha(f1);
                }
                if ((f3 <= ChatActivityEnterView.this.slideText.getX() + ChatActivityEnterView.this.slideText.getWidth() + AndroidUtilities.dp(30.0F)) && (ChatActivityEnterView.this.startedDraggingX == -1.0F))
                {
                  ChatActivityEnterView.access$2202(ChatActivityEnterView.this, f3);
                  ChatActivityEnterView.access$7302(ChatActivityEnterView.this, (ChatActivityEnterView.this.recordPanel.getMeasuredWidth() - ChatActivityEnterView.this.slideText.getMeasuredWidth() - AndroidUtilities.dp(48.0F)) / 2.0F);
                  if (ChatActivityEnterView.this.distCanMove > 0.0F) {
                    break label1141;
                  }
                  ChatActivityEnterView.access$7302(ChatActivityEnterView.this, AndroidUtilities.dp(80.0F));
                }
              }
              for (;;)
              {
                if (((FrameLayout.LayoutParams)localObject).leftMargin <= AndroidUtilities.dp(30.0F)) {
                  break label1175;
                }
                ((FrameLayout.LayoutParams)localObject).leftMargin = AndroidUtilities.dp(30.0F);
                ChatActivityEnterView.this.slideText.setLayoutParams((ViewGroup.LayoutParams)localObject);
                ChatActivityEnterView.this.slideText.setAlpha(1.0F);
                ChatActivityEnterView.access$2202(ChatActivityEnterView.this, -1.0F);
                break;
                ChatActivityEnterView.this.delegate.needStartRecordAudio(0);
                MediaController.getInstance().stopRecording(0);
                break label789;
                f1 = f2;
                if (f2 >= 0.0F) {
                  break label906;
                }
                f1 = 0.0F;
                break label906;
                if (ChatActivityEnterView.this.distCanMove > AndroidUtilities.dp(80.0F)) {
                  ChatActivityEnterView.access$7302(ChatActivityEnterView.this, AndroidUtilities.dp(80.0F));
                }
              }
            }
          }
        }
      });
      this.audioSendButton = new ImageView(paramActivity);
      this.audioSendButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      this.audioSendButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelIcons"), PorterDuff.Mode.MULTIPLY));
      this.audioSendButton.setImageResource(2131165503);
      this.audioSendButton.setPadding(0, 0, AndroidUtilities.dp(4.0F), 0);
      this.audioVideoButtonContainer.addView(this.audioSendButton, LayoutHelper.createFrame(48, 48.0F));
      if (paramBoolean)
      {
        this.videoSendButton = new ImageView(paramActivity);
        this.videoSendButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        this.videoSendButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelIcons"), PorterDuff.Mode.MULTIPLY));
        this.videoSendButton.setImageResource(2131165395);
        this.videoSendButton.setPadding(0, 0, AndroidUtilities.dp(4.0F), 0);
        this.audioVideoButtonContainer.addView(this.videoSendButton, LayoutHelper.createFrame(48, 48.0F));
      }
      this.recordCircle = new RecordCircle(paramActivity);
      this.recordCircle.setVisibility(8);
      this.sizeNotifierLayout.addView(this.recordCircle, LayoutHelper.createFrame(124, 194.0F, 85, 0.0F, 0.0F, -36.0F, 0.0F));
      this.cancelBotButton = new ImageView(paramActivity);
      this.cancelBotButton.setVisibility(4);
      this.cancelBotButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      paramSizeNotifierFrameLayout = this.cancelBotButton;
      paramChatActivity = new CloseProgressDrawable2();
      this.progressDrawable = paramChatActivity;
      paramSizeNotifierFrameLayout.setImageDrawable(paramChatActivity);
      this.progressDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelCancelInlineBot"), PorterDuff.Mode.MULTIPLY));
      this.cancelBotButton.setSoundEffectsEnabled(false);
      this.cancelBotButton.setScaleX(0.1F);
      this.cancelBotButton.setScaleY(0.1F);
      this.cancelBotButton.setAlpha(0.0F);
      this.sendButtonContainer.addView(this.cancelBotButton, LayoutHelper.createFrame(48, 48.0F));
      this.cancelBotButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = ChatActivityEnterView.this.messageEditText.getText().toString();
          int i = paramAnonymousView.indexOf(' ');
          if ((i == -1) || (i == paramAnonymousView.length() - 1))
          {
            ChatActivityEnterView.this.setFieldText("");
            return;
          }
          ChatActivityEnterView.this.setFieldText(paramAnonymousView.substring(0, i + 1));
        }
      });
      this.sendButton = new ImageView(paramActivity);
      this.sendButton.setVisibility(4);
      this.sendButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      this.sendButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelSend"), PorterDuff.Mode.MULTIPLY));
      this.sendButton.setImageResource(2131165407);
      this.sendButton.setSoundEffectsEnabled(false);
      this.sendButton.setScaleX(0.1F);
      this.sendButton.setScaleY(0.1F);
      this.sendButton.setAlpha(0.0F);
      this.sendButtonContainer.addView(this.sendButton, LayoutHelper.createFrame(48, 48.0F));
      this.sendButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          ChatActivityEnterView.this.sendMessage();
        }
      });
      this.expandStickersButton = new ImageView(paramActivity);
      this.expandStickersButton.setScaleType(ImageView.ScaleType.CENTER);
      paramSizeNotifierFrameLayout = this.expandStickersButton;
      paramChatActivity = new AnimatedArrowDrawable(Theme.getColor("chat_messagePanelIcons"));
      this.stickersArrow = paramChatActivity;
      paramSizeNotifierFrameLayout.setImageDrawable(paramChatActivity);
      this.expandStickersButton.setVisibility(8);
      this.expandStickersButton.setScaleX(0.1F);
      this.expandStickersButton.setScaleY(0.1F);
      this.expandStickersButton.setAlpha(0.0F);
      this.sendButtonContainer.addView(this.expandStickersButton, LayoutHelper.createFrame(48, 48.0F));
      this.expandStickersButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          boolean bool = false;
          if ((ChatActivityEnterView.this.expandStickersButton.getVisibility() != 0) || (ChatActivityEnterView.this.expandStickersButton.getAlpha() != 1.0F)) {}
          for (;;)
          {
            return;
            if (ChatActivityEnterView.this.stickersExpanded) {
              if (ChatActivityEnterView.this.searchingStickers)
              {
                ChatActivityEnterView.access$1102(ChatActivityEnterView.this, false);
                ChatActivityEnterView.this.emojiView.closeSearch(true);
                ChatActivityEnterView.this.emojiView.hideSearchKeyboard();
              }
            }
            while (!ChatActivityEnterView.this.stickersDragging)
            {
              paramAnonymousView = ChatActivityEnterView.this;
              if (!ChatActivityEnterView.this.stickersExpanded) {
                bool = true;
              }
              paramAnonymousView.setStickersExpanded(bool, true);
              return;
              if ((!ChatActivityEnterView.this.stickersDragging) && (ChatActivityEnterView.this.emojiView != null))
              {
                ChatActivityEnterView.this.emojiView.showSearchField(false);
                continue;
                if (!ChatActivityEnterView.this.stickersDragging) {
                  ChatActivityEnterView.this.emojiView.showSearchField(true);
                }
              }
            }
          }
        }
      });
      this.doneButtonContainer = new FrameLayout(paramActivity);
      this.doneButtonContainer.setVisibility(8);
      this.textFieldContainer.addView(this.doneButtonContainer, LayoutHelper.createLinear(48, 48, 80));
      this.doneButtonContainer.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          ChatActivityEnterView.this.doneEditingMessage();
        }
      });
      this.doneButtonImage = new ImageView(paramActivity);
      this.doneButtonImage.setScaleType(ImageView.ScaleType.CENTER);
      this.doneButtonImage.setImageResource(2131165311);
      this.doneButtonImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_editDoneIcon"), PorterDuff.Mode.MULTIPLY));
      this.doneButtonContainer.addView(this.doneButtonImage, LayoutHelper.createFrame(48, 48.0F));
      this.doneButtonProgress = new ContextProgressView(paramActivity, 0);
      this.doneButtonProgress.setVisibility(4);
      this.doneButtonContainer.addView(this.doneButtonProgress, LayoutHelper.createFrame(-1, -1.0F));
      paramActivity = MessagesController.getGlobalEmojiSettings();
      this.keyboardHeight = paramActivity.getInt("kbd_height", AndroidUtilities.dp(200.0F));
      this.keyboardHeightLand = paramActivity.getInt("kbd_height_land3", AndroidUtilities.dp(200.0F));
      setRecordVideoButtonVisible(false, false);
      checkSendButton(false);
      checkChannelRights();
      return;
      f = 2.0F;
      break;
      i = 2131165561;
      break label1005;
      i = 8;
      break label1059;
    }
  }
  
  private void checkSendButton(boolean paramBoolean)
  {
    if (this.editingMessageObject != null) {}
    label92:
    label119:
    label603:
    label740:
    label743:
    label825:
    label910:
    label1138:
    label1904:
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
              int i;
              int j;
              do
              {
                return;
                if (this.isPaused) {
                  paramBoolean = false;
                }
                if ((AndroidUtilities.getTrimmedString(this.messageEditText.getText()).length() <= 0) && (!this.forceShowSendButton) && (this.audioToSend == null) && (this.videoToSendMessageObject == null)) {
                  break label1138;
                }
                localObject = this.messageEditText.getCaption();
                if ((localObject == null) || ((this.sendButton.getVisibility() != 0) && (this.expandStickersButton.getVisibility() != 0))) {
                  break;
                }
                i = 1;
                if ((localObject != null) || ((this.cancelBotButton.getVisibility() != 0) && (this.expandStickersButton.getVisibility() != 0))) {
                  break label740;
                }
                j = 1;
                if ((this.audioVideoButtonContainer.getVisibility() != 0) && (i == 0) && (j == 0)) {
                  break label743;
                }
                if (!paramBoolean) {
                  break label910;
                }
              } while (((this.runningAnimationType == 1) && (this.messageEditText.getCaption() == null)) || ((this.runningAnimationType == 3) && (localObject != null)));
              if (this.runningAnimation != null)
              {
                this.runningAnimation.cancel();
                this.runningAnimation = null;
              }
              if (this.runningAnimation2 != null)
              {
                this.runningAnimation2.cancel();
                this.runningAnimation2 = null;
              }
              if (this.attachLayout != null)
              {
                this.runningAnimation2 = new AnimatorSet();
                this.runningAnimation2.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.attachLayout, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.attachLayout, "scaleX", new float[] { 0.0F }) });
                this.runningAnimation2.setDuration(100L);
                this.runningAnimation2.addListener(new AnimatorListenerAdapter()
                {
                  public void onAnimationCancel(Animator paramAnonymousAnimator)
                  {
                    if ((ChatActivityEnterView.this.runningAnimation2 != null) && (ChatActivityEnterView.this.runningAnimation2.equals(paramAnonymousAnimator))) {
                      ChatActivityEnterView.access$8602(ChatActivityEnterView.this, null);
                    }
                  }
                  
                  public void onAnimationEnd(Animator paramAnonymousAnimator)
                  {
                    if ((ChatActivityEnterView.this.runningAnimation2 != null) && (ChatActivityEnterView.this.runningAnimation2.equals(paramAnonymousAnimator))) {
                      ChatActivityEnterView.this.attachLayout.setVisibility(8);
                    }
                  }
                });
                this.runningAnimation2.start();
                updateFieldRight(0);
                if ((this.delegate != null) && (getVisibility() == 0)) {
                  this.delegate.onAttachButtonHidden();
                }
              }
              this.runningAnimation = new AnimatorSet();
              ArrayList localArrayList = new ArrayList();
              if (this.audioVideoButtonContainer.getVisibility() == 0)
              {
                localArrayList.add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "scaleX", new float[] { 0.1F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "scaleY", new float[] { 0.1F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "alpha", new float[] { 0.0F }));
              }
              if (this.expandStickersButton.getVisibility() == 0)
              {
                localArrayList.add(ObjectAnimator.ofFloat(this.expandStickersButton, "scaleX", new float[] { 0.1F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.expandStickersButton, "scaleY", new float[] { 0.1F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.expandStickersButton, "alpha", new float[] { 0.0F }));
              }
              if (i != 0)
              {
                localArrayList.add(ObjectAnimator.ofFloat(this.sendButton, "scaleX", new float[] { 0.1F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.sendButton, "scaleY", new float[] { 0.1F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.sendButton, "alpha", new float[] { 0.0F }));
                if (localObject == null) {
                  break label825;
                }
                this.runningAnimationType = 3;
                localArrayList.add(ObjectAnimator.ofFloat(this.cancelBotButton, "scaleX", new float[] { 1.0F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.cancelBotButton, "scaleY", new float[] { 1.0F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.cancelBotButton, "alpha", new float[] { 1.0F }));
                this.cancelBotButton.setVisibility(0);
              }
              for (;;)
              {
                this.runningAnimation.playTogether(localArrayList);
                this.runningAnimation.setDuration(150L);
                this.runningAnimation.addListener(new AnimatorListenerAdapter()
                {
                  public void onAnimationCancel(Animator paramAnonymousAnimator)
                  {
                    if ((ChatActivityEnterView.this.runningAnimation != null) && (ChatActivityEnterView.this.runningAnimation.equals(paramAnonymousAnimator))) {
                      ChatActivityEnterView.access$8702(ChatActivityEnterView.this, null);
                    }
                  }
                  
                  public void onAnimationEnd(Animator paramAnonymousAnimator)
                  {
                    if ((ChatActivityEnterView.this.runningAnimation != null) && (ChatActivityEnterView.this.runningAnimation.equals(paramAnonymousAnimator)))
                    {
                      if (localObject == null) {
                        break label97;
                      }
                      ChatActivityEnterView.this.cancelBotButton.setVisibility(0);
                      ChatActivityEnterView.this.sendButton.setVisibility(8);
                    }
                    for (;;)
                    {
                      ChatActivityEnterView.this.audioVideoButtonContainer.setVisibility(8);
                      ChatActivityEnterView.this.expandStickersButton.setVisibility(8);
                      ChatActivityEnterView.access$8702(ChatActivityEnterView.this, null);
                      ChatActivityEnterView.access$9002(ChatActivityEnterView.this, 0);
                      return;
                      label97:
                      ChatActivityEnterView.this.sendButton.setVisibility(0);
                      ChatActivityEnterView.this.cancelBotButton.setVisibility(8);
                    }
                  }
                });
                this.runningAnimation.start();
                return;
                i = 0;
                break label92;
                j = 0;
                break label119;
                break;
                if (j == 0) {
                  break label603;
                }
                localArrayList.add(ObjectAnimator.ofFloat(this.cancelBotButton, "scaleX", new float[] { 0.1F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.cancelBotButton, "scaleY", new float[] { 0.1F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.cancelBotButton, "alpha", new float[] { 0.0F }));
                break label603;
                this.runningAnimationType = 1;
                localArrayList.add(ObjectAnimator.ofFloat(this.sendButton, "scaleX", new float[] { 1.0F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.sendButton, "scaleY", new float[] { 1.0F }));
                localArrayList.add(ObjectAnimator.ofFloat(this.sendButton, "alpha", new float[] { 1.0F }));
                this.sendButton.setVisibility(0);
              }
              this.audioVideoButtonContainer.setScaleX(0.1F);
              this.audioVideoButtonContainer.setScaleY(0.1F);
              this.audioVideoButtonContainer.setAlpha(0.0F);
              if (localObject != null)
              {
                this.sendButton.setScaleX(0.1F);
                this.sendButton.setScaleY(0.1F);
                this.sendButton.setAlpha(0.0F);
                this.cancelBotButton.setScaleX(1.0F);
                this.cancelBotButton.setScaleY(1.0F);
                this.cancelBotButton.setAlpha(1.0F);
                this.cancelBotButton.setVisibility(0);
                this.sendButton.setVisibility(8);
              }
              for (;;)
              {
                this.audioVideoButtonContainer.setVisibility(8);
                if (this.attachLayout == null) {
                  break;
                }
                this.attachLayout.setVisibility(8);
                if ((this.delegate != null) && (getVisibility() == 0)) {
                  this.delegate.onAttachButtonHidden();
                }
                updateFieldRight(0);
                return;
                this.cancelBotButton.setScaleX(0.1F);
                this.cancelBotButton.setScaleY(0.1F);
                this.cancelBotButton.setAlpha(0.0F);
                this.sendButton.setScaleX(1.0F);
                this.sendButton.setScaleY(1.0F);
                this.sendButton.setAlpha(1.0F);
                this.sendButton.setVisibility(0);
                this.cancelBotButton.setVisibility(8);
              }
              if ((this.emojiView == null) || (this.emojiView.getVisibility() != 0) || (!this.stickersTabOpen) || (AndroidUtilities.isInMultiwindow)) {
                break label1904;
              }
              if (!paramBoolean) {
                break;
              }
            } while (this.runningAnimationType == 4);
            if (this.runningAnimation != null)
            {
              this.runningAnimation.cancel();
              this.runningAnimation = null;
            }
            if (this.runningAnimation2 != null)
            {
              this.runningAnimation2.cancel();
              this.runningAnimation2 = null;
            }
            if (this.attachLayout != null)
            {
              this.attachLayout.setVisibility(0);
              this.runningAnimation2 = new AnimatorSet();
              this.runningAnimation2.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.attachLayout, "alpha", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.attachLayout, "scaleX", new float[] { 1.0F }) });
              this.runningAnimation2.setDuration(100L);
              this.runningAnimation2.start();
              updateFieldRight(1);
              if (getVisibility() == 0) {
                this.delegate.onAttachButtonShow();
              }
            }
            this.expandStickersButton.setVisibility(0);
            this.runningAnimation = new AnimatorSet();
            this.runningAnimationType = 4;
            localObject = new ArrayList();
            ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.expandStickersButton, "scaleX", new float[] { 1.0F }));
            ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.expandStickersButton, "scaleY", new float[] { 1.0F }));
            ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.expandStickersButton, "alpha", new float[] { 1.0F }));
            if (this.cancelBotButton.getVisibility() == 0)
            {
              ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.cancelBotButton, "scaleX", new float[] { 0.1F }));
              ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.cancelBotButton, "scaleY", new float[] { 0.1F }));
              ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.cancelBotButton, "alpha", new float[] { 0.0F }));
            }
            for (;;)
            {
              this.runningAnimation.playTogether((Collection)localObject);
              this.runningAnimation.setDuration(150L);
              this.runningAnimation.addListener(new AnimatorListenerAdapter()
              {
                public void onAnimationCancel(Animator paramAnonymousAnimator)
                {
                  if ((ChatActivityEnterView.this.runningAnimation != null) && (ChatActivityEnterView.this.runningAnimation.equals(paramAnonymousAnimator))) {
                    ChatActivityEnterView.access$8702(ChatActivityEnterView.this, null);
                  }
                }
                
                public void onAnimationEnd(Animator paramAnonymousAnimator)
                {
                  if ((ChatActivityEnterView.this.runningAnimation != null) && (ChatActivityEnterView.this.runningAnimation.equals(paramAnonymousAnimator)))
                  {
                    ChatActivityEnterView.this.sendButton.setVisibility(8);
                    ChatActivityEnterView.this.cancelBotButton.setVisibility(8);
                    ChatActivityEnterView.this.audioVideoButtonContainer.setVisibility(8);
                    ChatActivityEnterView.this.expandStickersButton.setVisibility(0);
                    ChatActivityEnterView.access$8702(ChatActivityEnterView.this, null);
                    ChatActivityEnterView.access$9002(ChatActivityEnterView.this, 0);
                  }
                }
              });
              this.runningAnimation.start();
              return;
              if (this.audioVideoButtonContainer.getVisibility() == 0)
              {
                ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "scaleX", new float[] { 0.1F }));
                ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "scaleY", new float[] { 0.1F }));
                ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "alpha", new float[] { 0.0F }));
              }
              else
              {
                ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.sendButton, "scaleX", new float[] { 0.1F }));
                ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.sendButton, "scaleY", new float[] { 0.1F }));
                ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.sendButton, "alpha", new float[] { 0.0F }));
              }
            }
            this.sendButton.setScaleX(0.1F);
            this.sendButton.setScaleY(0.1F);
            this.sendButton.setAlpha(0.0F);
            this.cancelBotButton.setScaleX(0.1F);
            this.cancelBotButton.setScaleY(0.1F);
            this.cancelBotButton.setAlpha(0.0F);
            this.audioVideoButtonContainer.setScaleX(0.1F);
            this.audioVideoButtonContainer.setScaleY(0.1F);
            this.audioVideoButtonContainer.setAlpha(0.0F);
            this.expandStickersButton.setScaleX(1.0F);
            this.expandStickersButton.setScaleY(1.0F);
            this.expandStickersButton.setAlpha(1.0F);
            this.cancelBotButton.setVisibility(8);
            this.sendButton.setVisibility(8);
            this.audioVideoButtonContainer.setVisibility(8);
            this.expandStickersButton.setVisibility(0);
          } while (this.attachLayout == null);
          if (getVisibility() == 0) {
            this.delegate.onAttachButtonShow();
          }
          this.attachLayout.setVisibility(0);
          updateFieldRight(1);
          return;
        } while ((this.sendButton.getVisibility() != 0) && (this.cancelBotButton.getVisibility() != 0) && (this.expandStickersButton.getVisibility() != 0));
        if (!paramBoolean) {
          break;
        }
      } while (this.runningAnimationType == 2);
      if (this.runningAnimation != null)
      {
        this.runningAnimation.cancel();
        this.runningAnimation = null;
      }
      if (this.runningAnimation2 != null)
      {
        this.runningAnimation2.cancel();
        this.runningAnimation2 = null;
      }
      if (this.attachLayout != null)
      {
        this.attachLayout.setVisibility(0);
        this.runningAnimation2 = new AnimatorSet();
        this.runningAnimation2.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.attachLayout, "alpha", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.attachLayout, "scaleX", new float[] { 1.0F }) });
        this.runningAnimation2.setDuration(100L);
        this.runningAnimation2.start();
        updateFieldRight(1);
        if (getVisibility() == 0) {
          this.delegate.onAttachButtonShow();
        }
      }
      this.audioVideoButtonContainer.setVisibility(0);
      this.runningAnimation = new AnimatorSet();
      this.runningAnimationType = 2;
      final Object localObject = new ArrayList();
      ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "scaleX", new float[] { 1.0F }));
      ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "scaleY", new float[] { 1.0F }));
      ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "alpha", new float[] { 1.0F }));
      if (this.cancelBotButton.getVisibility() == 0)
      {
        ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.cancelBotButton, "scaleX", new float[] { 0.1F }));
        ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.cancelBotButton, "scaleY", new float[] { 0.1F }));
        ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.cancelBotButton, "alpha", new float[] { 0.0F }));
      }
      for (;;)
      {
        this.runningAnimation.playTogether((Collection)localObject);
        this.runningAnimation.setDuration(150L);
        this.runningAnimation.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationCancel(Animator paramAnonymousAnimator)
          {
            if ((ChatActivityEnterView.this.runningAnimation != null) && (ChatActivityEnterView.this.runningAnimation.equals(paramAnonymousAnimator))) {
              ChatActivityEnterView.access$8702(ChatActivityEnterView.this, null);
            }
          }
          
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            if ((ChatActivityEnterView.this.runningAnimation != null) && (ChatActivityEnterView.this.runningAnimation.equals(paramAnonymousAnimator)))
            {
              ChatActivityEnterView.this.sendButton.setVisibility(8);
              ChatActivityEnterView.this.cancelBotButton.setVisibility(8);
              ChatActivityEnterView.this.audioVideoButtonContainer.setVisibility(0);
              ChatActivityEnterView.access$8702(ChatActivityEnterView.this, null);
              ChatActivityEnterView.access$9002(ChatActivityEnterView.this, 0);
            }
          }
        });
        this.runningAnimation.start();
        return;
        if (this.expandStickersButton.getVisibility() == 0)
        {
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.expandStickersButton, "scaleX", new float[] { 0.1F }));
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.expandStickersButton, "scaleY", new float[] { 0.1F }));
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.expandStickersButton, "alpha", new float[] { 0.0F }));
        }
        else
        {
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.sendButton, "scaleX", new float[] { 0.1F }));
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.sendButton, "scaleY", new float[] { 0.1F }));
          ((ArrayList)localObject).add(ObjectAnimator.ofFloat(this.sendButton, "alpha", new float[] { 0.0F }));
        }
      }
      this.sendButton.setScaleX(0.1F);
      this.sendButton.setScaleY(0.1F);
      this.sendButton.setAlpha(0.0F);
      this.cancelBotButton.setScaleX(0.1F);
      this.cancelBotButton.setScaleY(0.1F);
      this.cancelBotButton.setAlpha(0.0F);
      this.expandStickersButton.setScaleX(0.1F);
      this.expandStickersButton.setScaleY(0.1F);
      this.expandStickersButton.setAlpha(0.0F);
      this.audioVideoButtonContainer.setScaleX(1.0F);
      this.audioVideoButtonContainer.setScaleY(1.0F);
      this.audioVideoButtonContainer.setAlpha(1.0F);
      this.cancelBotButton.setVisibility(8);
      this.sendButton.setVisibility(8);
      this.expandStickersButton.setVisibility(8);
      this.audioVideoButtonContainer.setVisibility(0);
    } while (this.attachLayout == null);
    if (getVisibility() == 0) {
      this.delegate.onAttachButtonShow();
    }
    this.attachLayout.setVisibility(0);
    updateFieldRight(1);
  }
  
  private void createEmojiView()
  {
    if (this.emojiView != null) {
      return;
    }
    this.emojiView = new EmojiView(this.allowStickers, this.allowGifs, this.parentActivity, this.info);
    this.emojiView.setVisibility(8);
    this.emojiView.setListener(new EmojiView.Listener()
    {
      public boolean isExpanded()
      {
        return ChatActivityEnterView.this.stickersExpanded;
      }
      
      public boolean isSearchOpened()
      {
        return ChatActivityEnterView.this.searchingStickers;
      }
      
      public boolean onBackspace()
      {
        if (ChatActivityEnterView.this.messageEditText.length() == 0) {
          return false;
        }
        ChatActivityEnterView.this.messageEditText.dispatchKeyEvent(new KeyEvent(0, 67));
        return true;
      }
      
      public void onClearEmojiRecent()
      {
        if ((ChatActivityEnterView.this.parentFragment == null) || (ChatActivityEnterView.this.parentActivity == null)) {
          return;
        }
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(ChatActivityEnterView.this.parentActivity);
        localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
        localBuilder.setMessage(LocaleController.getString("ClearRecentEmoji", 2131493263));
        localBuilder.setPositiveButton(LocaleController.getString("ClearButton", 2131493257).toUpperCase(), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
          {
            ChatActivityEnterView.this.emojiView.clearRecentEmoji();
          }
        });
        localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
        ChatActivityEnterView.this.parentFragment.showDialog(localBuilder.create());
      }
      
      public void onEmojiSelected(String paramAnonymousString)
      {
        int j = ChatActivityEnterView.this.messageEditText.getSelectionEnd();
        int i = j;
        if (j < 0) {
          i = 0;
        }
        try
        {
          ChatActivityEnterView.access$5002(ChatActivityEnterView.this, 2);
          paramAnonymousString = Emoji.replaceEmoji(paramAnonymousString, ChatActivityEnterView.this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
          ChatActivityEnterView.this.messageEditText.setText(ChatActivityEnterView.this.messageEditText.getText().insert(i, paramAnonymousString));
          i += paramAnonymousString.length();
          ChatActivityEnterView.this.messageEditText.setSelection(i, i);
          return;
        }
        catch (Exception paramAnonymousString)
        {
          FileLog.e(paramAnonymousString);
          return;
        }
        finally
        {
          ChatActivityEnterView.access$5002(ChatActivityEnterView.this, 0);
        }
      }
      
      public void onGifSelected(TLRPC.Document paramAnonymousDocument)
      {
        if (ChatActivityEnterView.this.stickersExpanded) {
          ChatActivityEnterView.this.setStickersExpanded(false, true);
        }
        SendMessagesHelper.getInstance(ChatActivityEnterView.this.currentAccount).sendSticker(paramAnonymousDocument, ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.replyingMessageObject);
        DataQuery.getInstance(ChatActivityEnterView.this.currentAccount).addRecentGif(paramAnonymousDocument, (int)(System.currentTimeMillis() / 1000L));
        if ((int)ChatActivityEnterView.this.dialog_id == 0) {
          MessagesController.getInstance(ChatActivityEnterView.this.currentAccount).saveGif(paramAnonymousDocument);
        }
        if (ChatActivityEnterView.this.delegate != null) {
          ChatActivityEnterView.this.delegate.onMessageSend(null);
        }
      }
      
      public void onGifTab(boolean paramAnonymousBoolean)
      {
        ChatActivityEnterView.this.post(ChatActivityEnterView.this.updateExpandabilityRunnable);
        if (!AndroidUtilities.usingHardwareInput)
        {
          if (!paramAnonymousBoolean) {
            break label71;
          }
          if (ChatActivityEnterView.this.messageEditText.length() == 0)
          {
            ChatActivityEnterView.this.messageEditText.setText("@gif ");
            ChatActivityEnterView.this.messageEditText.setSelection(ChatActivityEnterView.this.messageEditText.length());
          }
        }
        label71:
        while (!ChatActivityEnterView.this.messageEditText.getText().toString().equals("@gif ")) {
          return;
        }
        ChatActivityEnterView.this.messageEditText.setText("");
      }
      
      public void onSearchOpenClose(boolean paramAnonymousBoolean)
      {
        ChatActivityEnterView.access$1102(ChatActivityEnterView.this, paramAnonymousBoolean);
        ChatActivityEnterView.this.setStickersExpanded(paramAnonymousBoolean, false);
      }
      
      public void onShowStickerSet(TLRPC.StickerSet paramAnonymousStickerSet, TLRPC.InputStickerSet paramAnonymousInputStickerSet)
      {
        if ((ChatActivityEnterView.this.parentFragment == null) || (ChatActivityEnterView.this.parentActivity == null)) {
          return;
        }
        if (paramAnonymousStickerSet != null)
        {
          paramAnonymousInputStickerSet = new TLRPC.TL_inputStickerSetID();
          paramAnonymousInputStickerSet.access_hash = paramAnonymousStickerSet.access_hash;
          paramAnonymousInputStickerSet.id = paramAnonymousStickerSet.id;
        }
        ChatActivityEnterView.this.parentFragment.showDialog(new StickersAlert(ChatActivityEnterView.this.parentActivity, ChatActivityEnterView.this.parentFragment, paramAnonymousInputStickerSet, null, ChatActivityEnterView.this));
      }
      
      public void onStickerSelected(TLRPC.Document paramAnonymousDocument)
      {
        if (ChatActivityEnterView.this.stickersExpanded)
        {
          if (ChatActivityEnterView.this.searchingStickers)
          {
            ChatActivityEnterView.access$1102(ChatActivityEnterView.this, false);
            ChatActivityEnterView.this.emojiView.closeSearch(true, MessageObject.getStickerSetId(paramAnonymousDocument));
            ChatActivityEnterView.this.emojiView.hideSearchKeyboard();
          }
          ChatActivityEnterView.this.setStickersExpanded(false, true);
        }
        ChatActivityEnterView.this.onStickerSelected(paramAnonymousDocument);
        DataQuery.getInstance(ChatActivityEnterView.this.currentAccount).addRecentSticker(0, paramAnonymousDocument, (int)(System.currentTimeMillis() / 1000L), false);
        if ((int)ChatActivityEnterView.this.dialog_id == 0) {
          MessagesController.getInstance(ChatActivityEnterView.this.currentAccount).saveGif(paramAnonymousDocument);
        }
      }
      
      public void onStickerSetAdd(TLRPC.StickerSetCovered paramAnonymousStickerSetCovered)
      {
        DataQuery.getInstance(ChatActivityEnterView.this.currentAccount).removeStickersSet(ChatActivityEnterView.this.parentActivity, paramAnonymousStickerSetCovered.set, 2, ChatActivityEnterView.this.parentFragment, false);
      }
      
      public void onStickerSetRemove(TLRPC.StickerSetCovered paramAnonymousStickerSetCovered)
      {
        DataQuery.getInstance(ChatActivityEnterView.this.currentAccount).removeStickersSet(ChatActivityEnterView.this.parentActivity, paramAnonymousStickerSetCovered.set, 0, ChatActivityEnterView.this.parentFragment, false);
      }
      
      public void onStickersGroupClick(int paramAnonymousInt)
      {
        if (ChatActivityEnterView.this.parentFragment != null)
        {
          if (AndroidUtilities.isTablet()) {
            ChatActivityEnterView.this.hidePopup(false);
          }
          GroupStickersActivity localGroupStickersActivity = new GroupStickersActivity(paramAnonymousInt);
          localGroupStickersActivity.setInfo(ChatActivityEnterView.this.info);
          ChatActivityEnterView.this.parentFragment.presentFragment(localGroupStickersActivity);
        }
      }
      
      public void onStickersSettingsClick()
      {
        if (ChatActivityEnterView.this.parentFragment != null) {
          ChatActivityEnterView.this.parentFragment.presentFragment(new StickersActivity(0));
        }
      }
      
      public void onStickersTab(boolean paramAnonymousBoolean)
      {
        ChatActivityEnterView.this.delegate.onStickersTab(paramAnonymousBoolean);
        ChatActivityEnterView.this.post(ChatActivityEnterView.this.updateExpandabilityRunnable);
      }
    });
    this.emojiView.setDragListener(new EmojiView.DragListener()
    {
      int initialOffset;
      boolean wasExpanded;
      
      private boolean allowDragging()
      {
        return (ChatActivityEnterView.this.stickersTabOpen) && ((ChatActivityEnterView.this.stickersExpanded) || (ChatActivityEnterView.this.messageEditText.length() <= 0)) && (ChatActivityEnterView.this.emojiView.areThereAnyStickers());
      }
      
      public void onDrag(int paramAnonymousInt)
      {
        if (!allowDragging()) {
          return;
        }
        if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {}
        for (int i = ChatActivityEnterView.this.keyboardHeightLand;; i = ChatActivityEnterView.this.keyboardHeight)
        {
          paramAnonymousInt = Math.max(Math.min(paramAnonymousInt + this.initialOffset, 0), -(ChatActivityEnterView.this.stickersExpandedHeight - i));
          ChatActivityEnterView.this.emojiView.setTranslationY(paramAnonymousInt);
          ChatActivityEnterView.this.setTranslationY(paramAnonymousInt);
          ChatActivityEnterView.access$10002(ChatActivityEnterView.this, paramAnonymousInt / -(ChatActivityEnterView.this.stickersExpandedHeight - i));
          ChatActivityEnterView.this.sizeNotifierLayout.invalidate();
          return;
        }
      }
      
      public void onDragCancel()
      {
        if (!ChatActivityEnterView.this.stickersTabOpen) {
          return;
        }
        ChatActivityEnterView.access$7602(ChatActivityEnterView.this, false);
        ChatActivityEnterView.this.setStickersExpanded(this.wasExpanded, true);
      }
      
      public void onDragEnd(float paramAnonymousFloat)
      {
        boolean bool = false;
        if (!allowDragging()) {
          return;
        }
        ChatActivityEnterView.access$7602(ChatActivityEnterView.this, false);
        if (((this.wasExpanded) && (paramAnonymousFloat >= AndroidUtilities.dp(200.0F))) || ((!this.wasExpanded) && (paramAnonymousFloat <= AndroidUtilities.dp(-200.0F))) || ((this.wasExpanded) && (ChatActivityEnterView.this.stickersExpansionProgress <= 0.6F)) || ((!this.wasExpanded) && (ChatActivityEnterView.this.stickersExpansionProgress >= 0.4F)))
        {
          ChatActivityEnterView localChatActivityEnterView = ChatActivityEnterView.this;
          if (!this.wasExpanded) {
            bool = true;
          }
          localChatActivityEnterView.setStickersExpanded(bool, true);
          return;
        }
        ChatActivityEnterView.this.setStickersExpanded(this.wasExpanded, true);
      }
      
      public void onDragStart()
      {
        if (!allowDragging()) {
          return;
        }
        if (ChatActivityEnterView.this.stickersExpansionAnim != null) {
          ChatActivityEnterView.this.stickersExpansionAnim.cancel();
        }
        ChatActivityEnterView.access$7602(ChatActivityEnterView.this, true);
        this.wasExpanded = ChatActivityEnterView.this.stickersExpanded;
        ChatActivityEnterView.access$1002(ChatActivityEnterView.this, true);
        ChatActivityEnterView localChatActivityEnterView = ChatActivityEnterView.this;
        int j = ChatActivityEnterView.this.sizeNotifierLayout.getHeight();
        if (Build.VERSION.SDK_INT >= 21) {}
        for (int i = AndroidUtilities.statusBarHeight;; i = 0)
        {
          ChatActivityEnterView.access$9802(localChatActivityEnterView, j - i - ActionBar.getCurrentActionBarHeight() - ChatActivityEnterView.this.getHeight() + Theme.chat_composeShadowDrawable.getIntrinsicHeight());
          ChatActivityEnterView.this.emojiView.getLayoutParams().height = ChatActivityEnterView.this.stickersExpandedHeight;
          ChatActivityEnterView.this.emojiView.setLayerType(2, null);
          ChatActivityEnterView.this.sizeNotifierLayout.requestLayout();
          ChatActivityEnterView.this.sizeNotifierLayout.setForeground(new ChatActivityEnterView.ScrimDrawable(ChatActivityEnterView.this));
          this.initialOffset = ((int)ChatActivityEnterView.this.getTranslationY());
          if (ChatActivityEnterView.this.delegate == null) {
            break;
          }
          ChatActivityEnterView.this.delegate.onStickersExpandedChange();
          return;
        }
      }
    });
    this.sizeNotifierLayout.addView(this.emojiView);
    checkChannelRights();
  }
  
  private void hideRecordedAudioPanel()
  {
    this.audioToSendPath = null;
    this.audioToSend = null;
    this.audioToSendMessageObject = null;
    this.videoToSendMessageObject = null;
    this.videoTimelineView.destroy();
    AnimatorSet localAnimatorSet = new AnimatorSet();
    localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.recordedAudioPanel, "alpha", new float[] { 0.0F }) });
    localAnimatorSet.setDuration(200L);
    localAnimatorSet.addListener(new AnimatorListenerAdapter()
    {
      public void onAnimationEnd(Animator paramAnonymousAnimator)
      {
        ChatActivityEnterView.this.recordedAudioPanel.setVisibility(8);
      }
    });
    localAnimatorSet.start();
  }
  
  private void onWindowSizeChanged()
  {
    int j = this.sizeNotifierLayout.getHeight();
    int i = j;
    if (!this.keyboardVisible) {
      i = j - this.emojiPadding;
    }
    if (this.delegate != null) {
      this.delegate.onWindowSizeChanged(i);
    }
    if (this.topView != null)
    {
      if (i >= AndroidUtilities.dp(72.0F) + ActionBar.getCurrentActionBarHeight()) {
        break label114;
      }
      if (this.allowShowTopView)
      {
        this.allowShowTopView = false;
        if (this.needShowTopView)
        {
          this.topView.setVisibility(8);
          resizeForTopView(false);
          this.topView.setTranslationY(this.topView.getLayoutParams().height);
        }
      }
    }
    label114:
    do
    {
      do
      {
        return;
      } while (this.allowShowTopView);
      this.allowShowTopView = true;
    } while (!this.needShowTopView);
    this.topView.setVisibility(0);
    resizeForTopView(true);
    this.topView.setTranslationY(0.0F);
  }
  
  private void openKeyboardInternal()
  {
    int i;
    if ((AndroidUtilities.usingHardwareInput) || (this.isPaused))
    {
      i = 0;
      showPopup(i, 0);
      this.messageEditText.requestFocus();
      AndroidUtilities.showKeyboard(this.messageEditText);
      if (!this.isPaused) {
        break label54;
      }
      this.showKeyboardOnResume = true;
    }
    label54:
    while ((AndroidUtilities.usingHardwareInput) || (this.keyboardVisible) || (AndroidUtilities.isInMultiwindow))
    {
      return;
      i = 2;
      break;
    }
    this.waitingForKeyboardOpen = true;
    AndroidUtilities.cancelRunOnUIThread(this.openKeyboardRunnable);
    AndroidUtilities.runOnUIThread(this.openKeyboardRunnable, 100L);
  }
  
  private void removeGifFromInputField()
  {
    if ((!AndroidUtilities.usingHardwareInput) && (this.messageEditText.getText().toString().equals("@gif "))) {
      this.messageEditText.setText("");
    }
  }
  
  private void resizeForTopView(boolean paramBoolean)
  {
    FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)this.textFieldContainer.getLayoutParams();
    int j = AndroidUtilities.dp(2.0F);
    if (paramBoolean) {}
    for (int i = this.topView.getLayoutParams().height;; i = 0)
    {
      localLayoutParams.topMargin = (i + j);
      this.textFieldContainer.setLayoutParams(localLayoutParams);
      if (this.stickersExpanded) {
        setStickersExpanded(false, true);
      }
      return;
    }
  }
  
  private void sendMessage()
  {
    if (this.videoToSendMessageObject != null)
    {
      this.delegate.needStartRecordVideo(4);
      hideRecordedAudioPanel();
      checkSendButton(true);
    }
    do
    {
      Object localObject;
      do
      {
        return;
        if (this.audioToSend != null)
        {
          localObject = MediaController.getInstance().getPlayingMessageObject();
          if ((localObject != null) && (localObject == this.audioToSendMessageObject)) {
            MediaController.getInstance().cleanupPlayer(true, true);
          }
          SendMessagesHelper.getInstance(this.currentAccount).sendMessage(this.audioToSend, null, this.audioToSendPath, this.dialog_id, this.replyingMessageObject, null, null, null, null, 0);
          if (this.delegate != null) {
            this.delegate.onMessageSend(null);
          }
          hideRecordedAudioPanel();
          checkSendButton(true);
          return;
        }
        localObject = this.messageEditText.getText();
        if (!processSendingText((CharSequence)localObject)) {
          break;
        }
        this.messageEditText.setText("");
        this.lastTypingTimeSend = 0L;
      } while (this.delegate == null);
      this.delegate.onMessageSend((CharSequence)localObject);
      return;
    } while ((!this.forceShowSendButton) || (this.delegate == null));
    this.delegate.onMessageSend(null);
  }
  
  private void setEmojiButtonImage()
  {
    int i;
    if (this.emojiView == null)
    {
      i = MessagesController.getGlobalEmojiSettings().getInt("selected_page", 0);
      if ((i != 0) && ((this.allowStickers) || (this.allowGifs))) {
        break label60;
      }
      this.emojiButton.setImageResource(2131165393);
    }
    label60:
    do
    {
      return;
      i = this.emojiView.getCurrentPage();
      break;
      if (i == 1)
      {
        this.emojiButton.setImageResource(2131165394);
        return;
      }
    } while (i != 2);
    this.emojiButton.setImageResource(2131165391);
  }
  
  private void setRecordVideoButtonVisible(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.videoSendButton == null) {
      return;
    }
    Object localObject2 = this.videoSendButton;
    int i;
    label110:
    label129:
    label174:
    Object localObject3;
    label203:
    Object localObject4;
    label232:
    Object localObject5;
    label263:
    Object localObject6;
    label294:
    ImageView localImageView;
    if (paramBoolean1)
    {
      localObject1 = Integer.valueOf(1);
      ((ImageView)localObject2).setTag(localObject1);
      if (this.audioVideoButtonAnimation != null)
      {
        this.audioVideoButtonAnimation.cancel();
        this.audioVideoButtonAnimation = null;
      }
      if (!paramBoolean2) {
        break label478;
      }
      localObject1 = MessagesController.getGlobalMainSettings();
      i = 0;
      if ((int)this.dialog_id < 0)
      {
        localObject2 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-(int)this.dialog_id));
        if ((!ChatObject.isChannel((TLRPC.Chat)localObject2)) || (((TLRPC.Chat)localObject2).megagroup)) {
          break label430;
        }
        i = 1;
      }
      localObject2 = ((SharedPreferences)localObject1).edit();
      if (i == 0) {
        break label436;
      }
      localObject1 = "currentModeVideoChannel";
      ((SharedPreferences.Editor)localObject2).putBoolean((String)localObject1, paramBoolean1).commit();
      this.audioVideoButtonAnimation = new AnimatorSet();
      localObject1 = this.audioVideoButtonAnimation;
      localObject2 = this.videoSendButton;
      if (!paramBoolean1) {
        break label444;
      }
      f = 1.0F;
      localObject2 = ObjectAnimator.ofFloat(localObject2, "scaleX", new float[] { f });
      localObject3 = this.videoSendButton;
      if (!paramBoolean1) {
        break label451;
      }
      f = 1.0F;
      localObject3 = ObjectAnimator.ofFloat(localObject3, "scaleY", new float[] { f });
      localObject4 = this.videoSendButton;
      if (!paramBoolean1) {
        break label458;
      }
      f = 1.0F;
      localObject4 = ObjectAnimator.ofFloat(localObject4, "alpha", new float[] { f });
      localObject5 = this.audioSendButton;
      if (!paramBoolean1) {
        break label463;
      }
      f = 0.1F;
      localObject5 = ObjectAnimator.ofFloat(localObject5, "scaleX", new float[] { f });
      localObject6 = this.audioSendButton;
      if (!paramBoolean1) {
        break label468;
      }
      f = 0.1F;
      localObject6 = ObjectAnimator.ofFloat(localObject6, "scaleY", new float[] { f });
      localImageView = this.audioSendButton;
      if (!paramBoolean1) {
        break label473;
      }
    }
    label430:
    label436:
    label444:
    label451:
    label458:
    label463:
    label468:
    label473:
    for (float f = 0.0F;; f = 1.0F)
    {
      ((AnimatorSet)localObject1).playTogether(new Animator[] { localObject2, localObject3, localObject4, localObject5, localObject6, ObjectAnimator.ofFloat(localImageView, "alpha", new float[] { f }) });
      this.audioVideoButtonAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(ChatActivityEnterView.this.audioVideoButtonAnimation)) {
            ChatActivityEnterView.access$7702(ChatActivityEnterView.this, null);
          }
        }
      });
      this.audioVideoButtonAnimation.setInterpolator(new DecelerateInterpolator());
      this.audioVideoButtonAnimation.setDuration(150L);
      this.audioVideoButtonAnimation.start();
      return;
      localObject1 = null;
      break;
      i = 0;
      break label110;
      localObject1 = "currentModeVideo";
      break label129;
      f = 0.1F;
      break label174;
      f = 0.1F;
      break label203;
      f = 0.0F;
      break label232;
      f = 1.0F;
      break label263;
      f = 1.0F;
      break label294;
    }
    label478:
    Object localObject1 = this.videoSendButton;
    if (paramBoolean1)
    {
      f = 1.0F;
      ((ImageView)localObject1).setScaleX(f);
      localObject1 = this.videoSendButton;
      if (!paramBoolean1) {
        break label598;
      }
      f = 1.0F;
      label508:
      ((ImageView)localObject1).setScaleY(f);
      localObject1 = this.videoSendButton;
      if (!paramBoolean1) {
        break label605;
      }
      f = 1.0F;
      label526:
      ((ImageView)localObject1).setAlpha(f);
      localObject1 = this.audioSendButton;
      if (!paramBoolean1) {
        break label610;
      }
      f = 0.1F;
      label546:
      ((ImageView)localObject1).setScaleX(f);
      localObject1 = this.audioSendButton;
      if (!paramBoolean1) {
        break label615;
      }
      f = 0.1F;
      label566:
      ((ImageView)localObject1).setScaleY(f);
      localObject1 = this.audioSendButton;
      if (!paramBoolean1) {
        break label620;
      }
    }
    label598:
    label605:
    label610:
    label615:
    label620:
    for (f = 0.0F;; f = 1.0F)
    {
      ((ImageView)localObject1).setAlpha(f);
      return;
      f = 0.1F;
      break;
      f = 0.1F;
      break label508;
      f = 0.0F;
      break label526;
      f = 1.0F;
      break label546;
      f = 1.0F;
      break label566;
    }
  }
  
  private void setStickersExpanded(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((this.emojiView == null) || ((paramBoolean1) && (!this.emojiView.areThereAnyStickers())) || (this.stickersExpanded == paramBoolean1)) {
      return;
    }
    this.stickersExpanded = paramBoolean1;
    if (this.delegate != null) {
      this.delegate.onStickersExpandedChange();
    }
    final int i;
    int k;
    if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y)
    {
      i = this.keyboardHeightLand;
      if (this.stickersExpansionAnim != null)
      {
        this.stickersExpansionAnim.cancel();
        this.stickersExpansionAnim = null;
      }
      if (!this.stickersExpanded) {
        break label417;
      }
      k = this.sizeNotifierLayout.getHeight();
      if (Build.VERSION.SDK_INT < 21) {
        break label370;
      }
    }
    AnimatorSet localAnimatorSet;
    label370:
    for (int j = AndroidUtilities.statusBarHeight;; j = 0)
    {
      this.stickersExpandedHeight = (k - j - ActionBar.getCurrentActionBarHeight() - getHeight() + Theme.chat_composeShadowDrawable.getIntrinsicHeight());
      this.emojiView.getLayoutParams().height = this.stickersExpandedHeight;
      this.sizeNotifierLayout.requestLayout();
      this.sizeNotifierLayout.setForeground(new ScrimDrawable());
      this.messageEditText.setText(this.messageEditText.getText());
      if (!paramBoolean2) {
        break label376;
      }
      localAnimatorSet = new AnimatorSet();
      localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofInt(this, this.roundedTranslationYProperty, new int[] { -(this.stickersExpandedHeight - i) }), ObjectAnimator.ofInt(this.emojiView, this.roundedTranslationYProperty, new int[] { -(this.stickersExpandedHeight - i) }), ObjectAnimator.ofFloat(this.stickersArrow, "animationProgress", new float[] { 1.0F }) });
      localAnimatorSet.setDuration(400L);
      localAnimatorSet.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
      ((ObjectAnimator)localAnimatorSet.getChildAnimations().get(0)).addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
      {
        public void onAnimationUpdate(ValueAnimator paramAnonymousValueAnimator)
        {
          ChatActivityEnterView.access$10002(ChatActivityEnterView.this, ChatActivityEnterView.this.getTranslationY() / -(ChatActivityEnterView.this.stickersExpandedHeight - i));
          ChatActivityEnterView.this.sizeNotifierLayout.invalidate();
        }
      });
      localAnimatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          ChatActivityEnterView.access$9702(ChatActivityEnterView.this, null);
          ChatActivityEnterView.this.emojiView.setLayerType(0, null);
        }
      });
      this.stickersExpansionAnim = localAnimatorSet;
      this.emojiView.setLayerType(2, null);
      localAnimatorSet.start();
      return;
      i = this.keyboardHeight;
      break;
    }
    label376:
    this.stickersExpansionProgress = 1.0F;
    setTranslationY(-(this.stickersExpandedHeight - i));
    this.emojiView.setTranslationY(-(this.stickersExpandedHeight - i));
    this.stickersArrow.setAnimationProgress(1.0F);
    return;
    label417:
    if (paramBoolean2)
    {
      this.closeAnimationInProgress = true;
      localAnimatorSet = new AnimatorSet();
      localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofInt(this, this.roundedTranslationYProperty, new int[] { 0 }), ObjectAnimator.ofInt(this.emojiView, this.roundedTranslationYProperty, new int[] { 0 }), ObjectAnimator.ofFloat(this.stickersArrow, "animationProgress", new float[] { 0.0F }) });
      localAnimatorSet.setDuration(400L);
      localAnimatorSet.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
      ((ObjectAnimator)localAnimatorSet.getChildAnimations().get(0)).addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
      {
        public void onAnimationUpdate(ValueAnimator paramAnonymousValueAnimator)
        {
          ChatActivityEnterView.access$10002(ChatActivityEnterView.this, ChatActivityEnterView.this.getTranslationY() / -(ChatActivityEnterView.this.stickersExpandedHeight - i));
          ChatActivityEnterView.this.sizeNotifierLayout.invalidate();
        }
      });
      localAnimatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          ChatActivityEnterView.access$10302(ChatActivityEnterView.this, false);
          ChatActivityEnterView.access$9702(ChatActivityEnterView.this, null);
          ChatActivityEnterView.this.emojiView.getLayoutParams().height = i;
          ChatActivityEnterView.this.sizeNotifierLayout.requestLayout();
          ChatActivityEnterView.this.emojiView.setLayerType(0, null);
          ChatActivityEnterView.this.sizeNotifierLayout.setForeground(null);
          ChatActivityEnterView.this.sizeNotifierLayout.setWillNotDraw(false);
        }
      });
      this.stickersExpansionAnim = localAnimatorSet;
      this.emojiView.setLayerType(2, null);
      localAnimatorSet.start();
      return;
    }
    this.stickersExpansionProgress = 0.0F;
    setTranslationY(0.0F);
    this.emojiView.setTranslationY(0.0F);
    this.emojiView.getLayoutParams().height = i;
    this.sizeNotifierLayout.requestLayout();
    this.sizeNotifierLayout.setForeground(null);
    this.sizeNotifierLayout.setWillNotDraw(false);
    this.stickersArrow.setAnimationProgress(0.0F);
  }
  
  private void showPopup(int paramInt1, int paramInt2)
  {
    Object localObject;
    label77:
    int i;
    if (paramInt1 == 1)
    {
      if ((paramInt2 == 0) && (this.emojiView == null))
      {
        if (this.parentActivity == null) {
          return;
        }
        createEmojiView();
      }
      localObject = null;
      if (paramInt2 == 0)
      {
        this.emojiView.setVisibility(0);
        if ((this.botKeyboardView != null) && (this.botKeyboardView.getVisibility() != 8)) {
          this.botKeyboardView.setVisibility(8);
        }
        localObject = this.emojiView;
        this.currentPopupContentType = paramInt2;
        if (this.keyboardHeight <= 0) {
          this.keyboardHeight = MessagesController.getGlobalEmojiSettings().getInt("kbd_height", AndroidUtilities.dp(200.0F));
        }
        if (this.keyboardHeightLand <= 0) {
          this.keyboardHeightLand = MessagesController.getGlobalEmojiSettings().getInt("kbd_height_land3", AndroidUtilities.dp(200.0F));
        }
        if (AndroidUtilities.displaySize.x <= AndroidUtilities.displaySize.y) {
          break label355;
        }
        i = this.keyboardHeightLand;
        label158:
        int j = i;
        if (paramInt2 == 1) {
          j = Math.min(this.botKeyboardView.getKeyboardHeight(), i);
        }
        if (this.botKeyboardView != null) {
          this.botKeyboardView.setPanelHeight(j);
        }
        FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)((View)localObject).getLayoutParams();
        localLayoutParams.height = j;
        ((View)localObject).setLayoutParams(localLayoutParams);
        if (!AndroidUtilities.isInMultiwindow) {
          AndroidUtilities.hideKeyboard(this.messageEditText);
        }
        if (this.sizeNotifierLayout != null)
        {
          this.emojiPadding = j;
          this.sizeNotifierLayout.requestLayout();
          if (paramInt2 != 0) {
            break label363;
          }
          this.emojiButton.setImageResource(2131165392);
          label266:
          updateBotButton();
          onWindowSizeChanged();
        }
      }
    }
    for (;;)
    {
      if (this.stickersTabOpen) {
        checkSendButton(true);
      }
      if ((!this.stickersExpanded) || (paramInt1 == 1)) {
        break;
      }
      setStickersExpanded(false, false);
      return;
      if (paramInt2 != 1) {
        break label77;
      }
      if ((this.emojiView != null) && (this.emojiView.getVisibility() != 8)) {
        this.emojiView.setVisibility(8);
      }
      this.botKeyboardView.setVisibility(0);
      localObject = this.botKeyboardView;
      break label77;
      label355:
      i = this.keyboardHeight;
      break label158;
      label363:
      if (paramInt2 != 1) {
        break label266;
      }
      setEmojiButtonImage();
      break label266;
      if (this.emojiButton != null) {
        setEmojiButtonImage();
      }
      this.currentPopupContentType = -1;
      if (this.emojiView != null) {
        this.emojiView.setVisibility(8);
      }
      if (this.botKeyboardView != null) {
        this.botKeyboardView.setVisibility(8);
      }
      if (this.sizeNotifierLayout != null)
      {
        if (paramInt1 == 0) {
          this.emojiPadding = 0;
        }
        this.sizeNotifierLayout.requestLayout();
        onWindowSizeChanged();
      }
      updateBotButton();
    }
  }
  
  private void updateBotButton()
  {
    if (this.botButton == null) {
      return;
    }
    LinearLayout localLinearLayout;
    if ((this.hasBotCommands) || (this.botReplyMarkup != null))
    {
      if (this.botButton.getVisibility() != 0) {
        this.botButton.setVisibility(0);
      }
      if (this.botReplyMarkup != null) {
        if ((isPopupShowing()) && (this.currentPopupContentType == 1))
        {
          this.botButton.setImageResource(2131165392);
          updateFieldRight(2);
          localLinearLayout = this.attachLayout;
          if (((this.botButton != null) && (this.botButton.getVisibility() != 8)) || ((this.notifyButton != null) && (this.notifyButton.getVisibility() != 8))) {
            break label172;
          }
        }
      }
    }
    label172:
    for (float f = 48.0F;; f = 96.0F)
    {
      localLinearLayout.setPivotX(AndroidUtilities.dp(f));
      return;
      this.botButton.setImageResource(2131165245);
      break;
      this.botButton.setImageResource(2131165244);
      break;
      this.botButton.setVisibility(8);
      break;
    }
  }
  
  private void updateFieldHint()
  {
    int i = 0;
    EditTextCaption localEditTextCaption;
    if ((int)this.dialog_id < 0)
    {
      localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-(int)this.dialog_id));
      if ((ChatObject.isChannel((TLRPC.Chat)localObject)) && (!((TLRPC.Chat)localObject).megagroup)) {
        i = 1;
      }
    }
    else
    {
      if (i == 0) {
        break label144;
      }
      if (this.editingMessageObject == null) {
        break label103;
      }
      localEditTextCaption = this.messageEditText;
      if (!this.editingCaption) {
        break label90;
      }
    }
    label90:
    for (Object localObject = LocaleController.getString("Caption", 2131493134);; localObject = LocaleController.getString("TypeMessage", 2131494502))
    {
      localEditTextCaption.setHintText((String)localObject);
      return;
      i = 0;
      break;
    }
    label103:
    if (this.silent)
    {
      this.messageEditText.setHintText(LocaleController.getString("ChannelSilentBroadcast", 2131493209));
      return;
    }
    this.messageEditText.setHintText(LocaleController.getString("ChannelBroadcast", 2131493157));
    return;
    label144:
    this.messageEditText.setHintText(LocaleController.getString("TypeMessage", 2131494502));
  }
  
  private void updateFieldRight(int paramInt)
  {
    if ((this.messageEditText == null) || (this.editingMessageObject != null)) {
      return;
    }
    FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)this.messageEditText.getLayoutParams();
    if (paramInt == 1) {
      if (((this.botButton != null) && (this.botButton.getVisibility() == 0)) || ((this.notifyButton != null) && (this.notifyButton.getVisibility() == 0))) {
        localLayoutParams.rightMargin = AndroidUtilities.dp(98.0F);
      }
    }
    for (;;)
    {
      this.messageEditText.setLayoutParams(localLayoutParams);
      return;
      localLayoutParams.rightMargin = AndroidUtilities.dp(50.0F);
      continue;
      if (paramInt == 2)
      {
        if (localLayoutParams.rightMargin != AndroidUtilities.dp(2.0F)) {
          if (((this.botButton != null) && (this.botButton.getVisibility() == 0)) || ((this.notifyButton != null) && (this.notifyButton.getVisibility() == 0))) {
            localLayoutParams.rightMargin = AndroidUtilities.dp(98.0F);
          } else {
            localLayoutParams.rightMargin = AndroidUtilities.dp(50.0F);
          }
        }
      }
      else {
        localLayoutParams.rightMargin = AndroidUtilities.dp(2.0F);
      }
    }
  }
  
  private void updateRecordIntefrace()
  {
    if (this.recordingAudioVideo) {
      if (this.recordInterfaceState != 1) {}
    }
    for (;;)
    {
      return;
      this.recordInterfaceState = 1;
      try
      {
        if (this.wakeLock == null)
        {
          this.wakeLock = ((PowerManager)ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(536870918, "audio record lock");
          this.wakeLock.acquire();
        }
        AndroidUtilities.lockOrientation(this.parentActivity);
        this.recordPanel.setVisibility(0);
        this.recordCircle.setVisibility(0);
        this.recordCircle.setAmplitude(0.0D);
        this.recordTimeText.setText(String.format("%02d:%02d.%02d", new Object[] { Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0) }));
        this.recordDot.resetAlpha();
        this.lastTimeString = null;
        this.lastTypingSendTime = -1L;
        FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)this.slideText.getLayoutParams();
        localLayoutParams.leftMargin = AndroidUtilities.dp(30.0F);
        this.slideText.setLayoutParams(localLayoutParams);
        this.slideText.setAlpha(1.0F);
        this.recordPanel.setX(AndroidUtilities.displaySize.x);
        if (this.runningAnimationAudio != null) {
          this.runningAnimationAudio.cancel();
        }
        this.runningAnimationAudio = new AnimatorSet();
        this.runningAnimationAudio.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.recordPanel, "translationX", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.recordCircle, "scale", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "alpha", new float[] { 0.0F }) });
        this.runningAnimationAudio.setDuration(300L);
        this.runningAnimationAudio.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            if ((ChatActivityEnterView.this.runningAnimationAudio != null) && (ChatActivityEnterView.this.runningAnimationAudio.equals(paramAnonymousAnimator)))
            {
              ChatActivityEnterView.this.recordPanel.setX(0.0F);
              ChatActivityEnterView.access$9102(ChatActivityEnterView.this, null);
            }
          }
        });
        this.runningAnimationAudio.setInterpolator(new DecelerateInterpolator());
        this.runningAnimationAudio.start();
        return;
      }
      catch (Exception localException1)
      {
        for (;;)
        {
          FileLog.e(localException1);
        }
      }
      if (this.wakeLock != null) {}
      try
      {
        this.wakeLock.release();
        this.wakeLock = null;
        AndroidUtilities.unlockOrientation(this.parentActivity);
        if (this.recordInterfaceState == 0) {
          continue;
        }
        this.recordInterfaceState = 0;
        if (this.runningAnimationAudio != null) {
          this.runningAnimationAudio.cancel();
        }
        this.runningAnimationAudio = new AnimatorSet();
        this.runningAnimationAudio.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.recordPanel, "translationX", new float[] { AndroidUtilities.displaySize.x }), ObjectAnimator.ofFloat(this.recordCircle, "scale", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.audioVideoButtonContainer, "alpha", new float[] { 1.0F }) });
        this.runningAnimationAudio.setDuration(300L);
        this.runningAnimationAudio.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            if ((ChatActivityEnterView.this.runningAnimationAudio != null) && (ChatActivityEnterView.this.runningAnimationAudio.equals(paramAnonymousAnimator)))
            {
              paramAnonymousAnimator = (FrameLayout.LayoutParams)ChatActivityEnterView.this.slideText.getLayoutParams();
              paramAnonymousAnimator.leftMargin = AndroidUtilities.dp(30.0F);
              ChatActivityEnterView.this.slideText.setLayoutParams(paramAnonymousAnimator);
              ChatActivityEnterView.this.slideText.setAlpha(1.0F);
              ChatActivityEnterView.this.recordPanel.setVisibility(8);
              ChatActivityEnterView.this.recordCircle.setVisibility(8);
              ChatActivityEnterView.this.recordCircle.setSendButtonInvisible();
              ChatActivityEnterView.access$9102(ChatActivityEnterView.this, null);
            }
          }
        });
        this.runningAnimationAudio.setInterpolator(new AccelerateInterpolator());
        this.runningAnimationAudio.start();
        return;
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
  
  public void addEmojiToRecent(String paramString)
  {
    createEmojiView();
    this.emojiView.addEmojiToRecent(paramString);
  }
  
  public void addRecentGif(TLRPC.Document paramDocument)
  {
    DataQuery.getInstance(this.currentAccount).addRecentGif(paramDocument, (int)(System.currentTimeMillis() / 1000L));
    if (this.emojiView != null) {
      this.emojiView.addRecentGif(paramDocument);
    }
  }
  
  public void addStickerToRecent(TLRPC.Document paramDocument)
  {
    createEmojiView();
    this.emojiView.addRecentSticker(paramDocument);
  }
  
  public void addTopView(View paramView, int paramInt)
  {
    if (paramView == null) {
      return;
    }
    this.topView = paramView;
    this.topView.setVisibility(8);
    this.topView.setTranslationY(paramInt);
    addView(this.topView, 0, LayoutHelper.createFrame(-1, paramInt, 51, 0.0F, 2.0F, 0.0F, 0.0F));
    this.needShowTopView = false;
  }
  
  public void cancelRecordingAudioVideo()
  {
    if ((this.hasRecordVideo) && (this.videoSendButton.getTag() != null)) {
      this.delegate.needStartRecordVideo(2);
    }
    for (;;)
    {
      this.recordingAudioVideo = false;
      updateRecordIntefrace();
      return;
      this.delegate.needStartRecordAudio(0);
      MediaController.getInstance().stopRecording(0);
    }
  }
  
  public void checkChannelRights()
  {
    if (this.parentFragment == null) {}
    TLRPC.Chat localChat;
    float f;
    do
    {
      do
      {
        return;
        localChat = this.parentFragment.getCurrentChat();
      } while (!ChatObject.isChannel(localChat));
      localObject = this.audioVideoButtonContainer;
      if ((localChat.banned_rights != null) && (localChat.banned_rights.send_media)) {
        break;
      }
      f = 1.0F;
      ((FrameLayout)localObject).setAlpha(f);
    } while (this.emojiView == null);
    Object localObject = this.emojiView;
    if ((localChat.banned_rights != null) && (localChat.banned_rights.send_stickers)) {}
    for (boolean bool = true;; bool = false)
    {
      ((EmojiView)localObject).setStickersBanned(bool, localChat.id);
      return;
      f = 0.5F;
      break;
    }
  }
  
  public void checkRoundVideo()
  {
    if (this.hasRecordVideo) {
      return;
    }
    if ((this.attachLayout == null) || (Build.VERSION.SDK_INT < 18))
    {
      this.hasRecordVideo = false;
      setRecordVideoButtonVisible(false, false);
      return;
    }
    int i = (int)this.dialog_id;
    int j = (int)(this.dialog_id >> 32);
    boolean bool2;
    boolean bool1;
    label138:
    SharedPreferences localSharedPreferences;
    if ((i == 0) && (j != 0))
    {
      if (AndroidUtilities.getPeerLayerVersion(MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(j)).layer) >= 66) {
        this.hasRecordVideo = true;
      }
      bool2 = false;
      if ((int)this.dialog_id < 0)
      {
        localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-(int)this.dialog_id));
        if ((!ChatObject.isChannel((TLRPC.Chat)localObject)) || (((TLRPC.Chat)localObject).megagroup)) {
          break label250;
        }
        bool1 = true;
        bool2 = bool1;
        if (bool1)
        {
          bool2 = bool1;
          if (!((TLRPC.Chat)localObject).creator) {
            if (((TLRPC.Chat)localObject).admin_rights != null)
            {
              bool2 = bool1;
              if (((TLRPC.Chat)localObject).admin_rights.post_messages) {}
            }
            else
            {
              this.hasRecordVideo = false;
              bool2 = bool1;
            }
          }
        }
      }
      if (!SharedConfig.inappCamera) {
        this.hasRecordVideo = false;
      }
      if (!this.hasRecordVideo) {
        break label263;
      }
      CameraController.getInstance().initCamera();
      localSharedPreferences = MessagesController.getGlobalMainSettings();
      if (!bool2) {
        break label255;
      }
    }
    label250:
    label255:
    for (Object localObject = "currentModeVideoChannel";; localObject = "currentModeVideo")
    {
      setRecordVideoButtonVisible(localSharedPreferences.getBoolean((String)localObject, bool2), false);
      return;
      this.hasRecordVideo = true;
      break;
      bool1 = false;
      break label138;
    }
    label263:
    setRecordVideoButtonVisible(false, false);
  }
  
  public void closeKeyboard()
  {
    AndroidUtilities.hideKeyboard(this.messageEditText);
  }
  
  public void didPressedBotButton(final TLRPC.KeyboardButton paramKeyboardButton, MessageObject paramMessageObject1, final MessageObject paramMessageObject2)
  {
    if ((paramKeyboardButton == null) || (paramMessageObject2 == null)) {}
    do
    {
      do
      {
        return;
        if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButton))
        {
          SendMessagesHelper.getInstance(this.currentAccount).sendMessage(paramKeyboardButton.text, this.dialog_id, paramMessageObject1, null, false, null, null, null);
          return;
        }
        if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonUrl))
        {
          this.parentFragment.showOpenUrlAlert(paramKeyboardButton.url, true);
          return;
        }
        if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonRequestPhone))
        {
          this.parentFragment.shareMyContact(paramMessageObject2);
          return;
        }
        if ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonRequestGeoLocation))
        {
          paramMessageObject1 = new AlertDialog.Builder(this.parentActivity);
          paramMessageObject1.setTitle(LocaleController.getString("ShareYouLocationTitle", 2131494391));
          paramMessageObject1.setMessage(LocaleController.getString("ShareYouLocationInfo", 2131494389));
          paramMessageObject1.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
            {
              if ((Build.VERSION.SDK_INT >= 23) && (ChatActivityEnterView.this.parentActivity.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0))
              {
                ChatActivityEnterView.this.parentActivity.requestPermissions(new String[] { "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION" }, 2);
                ChatActivityEnterView.access$9302(ChatActivityEnterView.this, paramMessageObject2);
                ChatActivityEnterView.access$9402(ChatActivityEnterView.this, paramKeyboardButton);
                return;
              }
              SendMessagesHelper.getInstance(ChatActivityEnterView.this.currentAccount).sendCurrentLocation(paramMessageObject2, paramKeyboardButton);
            }
          });
          paramMessageObject1.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          this.parentFragment.showDialog(paramMessageObject1.create());
          return;
        }
        if (((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonCallback)) || ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonGame)) || ((paramKeyboardButton instanceof TLRPC.TL_keyboardButtonBuy)))
        {
          SendMessagesHelper.getInstance(this.currentAccount).sendCallback(true, paramMessageObject2, paramKeyboardButton, this.parentFragment);
          return;
        }
      } while ((!(paramKeyboardButton instanceof TLRPC.TL_keyboardButtonSwitchInline)) || (this.parentFragment.processSwitchButton((TLRPC.TL_keyboardButtonSwitchInline)paramKeyboardButton)));
      if (!paramKeyboardButton.same_peer) {
        break;
      }
      int i = paramMessageObject2.messageOwner.from_id;
      if (paramMessageObject2.messageOwner.via_bot_id != 0) {
        i = paramMessageObject2.messageOwner.via_bot_id;
      }
      paramMessageObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
    } while (paramMessageObject1 == null);
    setFieldText("@" + paramMessageObject1.username + " " + paramKeyboardButton.query);
    return;
    paramMessageObject1 = new Bundle();
    paramMessageObject1.putBoolean("onlySelect", true);
    paramMessageObject1.putInt("dialogsType", 1);
    paramMessageObject1 = new DialogsActivity(paramMessageObject1);
    paramMessageObject1.setDelegate(new DialogsActivity.DialogsActivityDelegate()
    {
      public void didSelectDialogs(DialogsActivity paramAnonymousDialogsActivity, ArrayList<Long> paramAnonymousArrayList, CharSequence paramAnonymousCharSequence, boolean paramAnonymousBoolean)
      {
        int i = paramMessageObject2.messageOwner.from_id;
        if (paramMessageObject2.messageOwner.via_bot_id != 0) {
          i = paramMessageObject2.messageOwner.via_bot_id;
        }
        paramAnonymousCharSequence = MessagesController.getInstance(ChatActivityEnterView.this.currentAccount).getUser(Integer.valueOf(i));
        if (paramAnonymousCharSequence == null) {
          paramAnonymousDialogsActivity.finishFragment();
        }
        label251:
        for (;;)
        {
          return;
          long l = ((Long)paramAnonymousArrayList.get(0)).longValue();
          DataQuery.getInstance(ChatActivityEnterView.this.currentAccount).saveDraft(l, "@" + paramAnonymousCharSequence.username + " " + paramKeyboardButton.query, null, null, true);
          if (l == ChatActivityEnterView.this.dialog_id) {
            break label263;
          }
          i = (int)l;
          if (i == 0) {
            break label258;
          }
          paramAnonymousArrayList = new Bundle();
          if (i > 0) {
            paramAnonymousArrayList.putInt("user_id", i);
          }
          for (;;)
          {
            if (!MessagesController.getInstance(ChatActivityEnterView.this.currentAccount).checkCanOpenChat(paramAnonymousArrayList, paramAnonymousDialogsActivity)) {
              break label251;
            }
            paramAnonymousArrayList = new ChatActivity(paramAnonymousArrayList);
            if (!ChatActivityEnterView.this.parentFragment.presentFragment(paramAnonymousArrayList, true)) {
              break label253;
            }
            if (AndroidUtilities.isTablet()) {
              break;
            }
            ChatActivityEnterView.this.parentFragment.removeSelfFromStack();
            return;
            if (i < 0) {
              paramAnonymousArrayList.putInt("chat_id", -i);
            }
          }
        }
        label253:
        paramAnonymousDialogsActivity.finishFragment();
        return;
        label258:
        paramAnonymousDialogsActivity.finishFragment();
        return;
        label263:
        paramAnonymousDialogsActivity.finishFragment();
      }
    });
    this.parentFragment.presentFragment(paramMessageObject1);
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.emojiDidLoaded)
    {
      if (this.emojiView != null) {
        this.emojiView.invalidateViews();
      }
      if (this.botKeyboardView != null) {
        this.botKeyboardView.invalidateViews();
      }
    }
    label289:
    label1086:
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
                        do
                        {
                          return;
                          if (paramInt1 == NotificationCenter.recordProgressChanged)
                          {
                            long l1 = ((Long)paramVarArgs[0]).longValue();
                            long l2 = l1 / 1000L;
                            paramInt1 = (int)(l1 % 1000L) / 10;
                            localObject = String.format("%02d:%02d.%02d", new Object[] { Long.valueOf(l2 / 60L), Long.valueOf(l2 % 60L), Integer.valueOf(paramInt1) });
                            MessagesController localMessagesController;
                            if ((this.lastTimeString == null) || (!this.lastTimeString.equals(localObject))) {
                              if ((this.lastTypingSendTime != l2) && (l2 % 5L == 0L))
                              {
                                this.lastTypingSendTime = l2;
                                localMessagesController = MessagesController.getInstance(this.currentAccount);
                                l2 = this.dialog_id;
                                if ((this.videoSendButton == null) || (this.videoSendButton.getTag() == null)) {
                                  break label289;
                                }
                              }
                            }
                            for (paramInt1 = 7;; paramInt1 = 1)
                            {
                              localMessagesController.sendTyping(l2, paramInt1, 0);
                              if (this.recordTimeText != null) {
                                this.recordTimeText.setText((CharSequence)localObject);
                              }
                              if (this.recordCircle != null) {
                                this.recordCircle.setAmplitude(((Double)paramVarArgs[1]).doubleValue());
                              }
                              if ((this.videoSendButton == null) || (this.videoSendButton.getTag() == null) || (l1 < 59500L)) {
                                break;
                              }
                              this.startedDraggingX = -1.0F;
                              this.delegate.needStartRecordVideo(3);
                              return;
                            }
                          }
                          if (paramInt1 != NotificationCenter.closeChats) {
                            break;
                          }
                        } while ((this.messageEditText == null) || (!this.messageEditText.isFocused()));
                        AndroidUtilities.hideKeyboard(this.messageEditText);
                        return;
                        if ((paramInt1 != NotificationCenter.recordStartError) && (paramInt1 != NotificationCenter.recordStopped)) {
                          break;
                        }
                        if (this.recordingAudioVideo)
                        {
                          MessagesController.getInstance(this.currentAccount).sendTyping(this.dialog_id, 2, 0);
                          this.recordingAudioVideo = false;
                          updateRecordIntefrace();
                        }
                      } while (paramInt1 != NotificationCenter.recordStopped);
                      paramVarArgs = (Integer)paramVarArgs[0];
                      if (paramVarArgs.intValue() == 2)
                      {
                        this.videoTimelineView.setVisibility(0);
                        this.recordedAudioBackground.setVisibility(8);
                        this.recordedAudioTimeTextView.setVisibility(8);
                        this.recordedAudioPlayButton.setVisibility(8);
                        this.recordedAudioSeekBar.setVisibility(8);
                        this.recordedAudioPanel.setAlpha(1.0F);
                        this.recordedAudioPanel.setVisibility(0);
                        return;
                      }
                    } while (paramVarArgs.intValue() != 1);
                    return;
                    if (paramInt1 != NotificationCenter.recordStarted) {
                      break;
                    }
                  } while (this.recordingAudioVideo);
                  this.recordingAudioVideo = true;
                  updateRecordIntefrace();
                  return;
                  if (paramInt1 != NotificationCenter.audioDidSent) {
                    break label1086;
                  }
                  localObject = paramVarArgs[0];
                  if ((localObject instanceof VideoEditedInfo))
                  {
                    this.videoToSendMessageObject = ((VideoEditedInfo)localObject);
                    this.audioToSendPath = ((String)paramVarArgs[1]);
                    this.videoTimelineView.setVideoPath(this.audioToSendPath);
                    this.videoTimelineView.setVisibility(0);
                    this.videoTimelineView.setMinProgressDiff(1000.0F / (float)this.videoToSendMessageObject.estimatedDuration);
                    this.recordedAudioBackground.setVisibility(8);
                    this.recordedAudioTimeTextView.setVisibility(8);
                    this.recordedAudioPlayButton.setVisibility(8);
                    this.recordedAudioSeekBar.setVisibility(8);
                    this.recordedAudioPanel.setAlpha(1.0F);
                    this.recordedAudioPanel.setVisibility(0);
                    closeKeyboard();
                    hidePopup(false);
                    checkSendButton(false);
                    return;
                  }
                  this.audioToSend = ((TLRPC.TL_document)paramVarArgs[0]);
                  this.audioToSendPath = ((String)paramVarArgs[1]);
                  if (this.audioToSend == null) {
                    break;
                  }
                } while (this.recordedAudioPanel == null);
                this.videoTimelineView.setVisibility(8);
                this.recordedAudioBackground.setVisibility(0);
                this.recordedAudioTimeTextView.setVisibility(0);
                this.recordedAudioPlayButton.setVisibility(0);
                this.recordedAudioSeekBar.setVisibility(0);
                paramVarArgs = new TLRPC.TL_message();
                paramVarArgs.out = true;
                paramVarArgs.id = 0;
                paramVarArgs.to_id = new TLRPC.TL_peerUser();
                Object localObject = paramVarArgs.to_id;
                paramInt1 = UserConfig.getInstance(this.currentAccount).getClientUserId();
                paramVarArgs.from_id = paramInt1;
                ((TLRPC.Peer)localObject).user_id = paramInt1;
                paramVarArgs.date = ((int)(System.currentTimeMillis() / 1000L));
                paramVarArgs.message = "";
                paramVarArgs.attachPath = this.audioToSendPath;
                paramVarArgs.media = new TLRPC.TL_messageMediaDocument();
                localObject = paramVarArgs.media;
                ((TLRPC.MessageMedia)localObject).flags |= 0x3;
                paramVarArgs.media.document = this.audioToSend;
                paramVarArgs.flags |= 0x300;
                this.audioToSendMessageObject = new MessageObject(UserConfig.selectedAccount, paramVarArgs, false);
                this.recordedAudioPanel.setAlpha(1.0F);
                this.recordedAudioPanel.setVisibility(0);
                int i = 0;
                paramInt2 = 0;
                paramInt1 = i;
                if (paramInt2 < this.audioToSend.attributes.size())
                {
                  paramVarArgs = (TLRPC.DocumentAttribute)this.audioToSend.attributes.get(paramInt2);
                  if ((paramVarArgs instanceof TLRPC.TL_documentAttributeAudio)) {
                    paramInt1 = paramVarArgs.duration;
                  }
                }
                else
                {
                  paramInt2 = 0;
                }
                for (;;)
                {
                  if (paramInt2 < this.audioToSend.attributes.size())
                  {
                    paramVarArgs = (TLRPC.DocumentAttribute)this.audioToSend.attributes.get(paramInt2);
                    if ((paramVarArgs instanceof TLRPC.TL_documentAttributeAudio))
                    {
                      if ((paramVarArgs.waveform == null) || (paramVarArgs.waveform.length == 0)) {
                        paramVarArgs.waveform = MediaController.getInstance().getWaveform(this.audioToSendPath);
                      }
                      this.recordedAudioSeekBar.setWaveform(paramVarArgs.waveform);
                    }
                  }
                  else
                  {
                    this.recordedAudioTimeTextView.setText(String.format("%d:%02d", new Object[] { Integer.valueOf(paramInt1 / 60), Integer.valueOf(paramInt1 % 60) }));
                    closeKeyboard();
                    hidePopup(false);
                    checkSendButton(false);
                    return;
                    paramInt2 += 1;
                    break;
                  }
                  paramInt2 += 1;
                }
              } while (this.delegate == null);
              this.delegate.onMessageSend(null);
              return;
              if (paramInt1 != NotificationCenter.audioRouteChanged) {
                break;
              }
            } while (this.parentActivity == null);
            boolean bool = ((Boolean)paramVarArgs[0]).booleanValue();
            paramVarArgs = this.parentActivity;
            if (bool) {}
            for (paramInt1 = 0;; paramInt1 = Integer.MIN_VALUE)
            {
              paramVarArgs.setVolumeControlStream(paramInt1);
              return;
            }
            if (paramInt1 != NotificationCenter.messagePlayingDidReset) {
              break;
            }
          } while ((this.audioToSendMessageObject == null) || (MediaController.getInstance().isPlayingMessage(this.audioToSendMessageObject)));
          this.recordedAudioPlayButton.setImageDrawable(this.playDrawable);
          this.recordedAudioSeekBar.setProgress(0.0F);
          return;
          if (paramInt1 != NotificationCenter.messagePlayingProgressDidChanged) {
            break;
          }
          paramVarArgs = (Integer)paramVarArgs[0];
        } while ((this.audioToSendMessageObject == null) || (!MediaController.getInstance().isPlayingMessage(this.audioToSendMessageObject)));
        paramVarArgs = MediaController.getInstance().getPlayingMessageObject();
        this.audioToSendMessageObject.audioProgress = paramVarArgs.audioProgress;
        this.audioToSendMessageObject.audioProgressSec = paramVarArgs.audioProgressSec;
      } while (this.recordedAudioSeekBar.isDragging());
      this.recordedAudioSeekBar.setProgress(this.audioToSendMessageObject.audioProgress);
      return;
    } while ((paramInt1 != NotificationCenter.featuredStickersDidLoaded) || (this.emojiButton == null));
    this.emojiButton.invalidate();
  }
  
  public void doneEditingMessage()
  {
    if (this.editingMessageObject != null)
    {
      this.delegate.onMessageEditEnd(true);
      showEditDoneProgress(true, true);
      CharSequence[] arrayOfCharSequence = new CharSequence[1];
      arrayOfCharSequence[0] = this.messageEditText.getText();
      ArrayList localArrayList = DataQuery.getInstance(this.currentAccount).getEntities(arrayOfCharSequence);
      this.editingMessageReqId = SendMessagesHelper.getInstance(this.currentAccount).editMessage(this.editingMessageObject, arrayOfCharSequence[0].toString(), this.messageWebPageSearch, this.parentFragment, localArrayList, new Runnable()
      {
        public void run()
        {
          ChatActivityEnterView.access$8502(ChatActivityEnterView.this, 0);
          ChatActivityEnterView.this.setEditingMessageObject(null, false);
        }
      });
    }
  }
  
  protected boolean drawChild(Canvas paramCanvas, View paramView, long paramLong)
  {
    if (paramView == this.topView)
    {
      paramCanvas.save();
      paramCanvas.clipRect(0, 0, getMeasuredWidth(), paramView.getLayoutParams().height + AndroidUtilities.dp(2.0F));
    }
    boolean bool = super.drawChild(paramCanvas, paramView, paramLong);
    if (paramView == this.topView) {
      paramCanvas.restore();
    }
    return bool;
  }
  
  public ImageView getAttachButton()
  {
    return this.attachButton;
  }
  
  public ImageView getBotButton()
  {
    return this.botButton;
  }
  
  public int getCursorPosition()
  {
    if (this.messageEditText == null) {
      return 0;
    }
    return this.messageEditText.getSelectionStart();
  }
  
  public MessageObject getEditingMessageObject()
  {
    return this.editingMessageObject;
  }
  
  public ImageView getEmojiButton()
  {
    return this.emojiButton;
  }
  
  public int getEmojiPadding()
  {
    return this.emojiPadding;
  }
  
  public EmojiView getEmojiView()
  {
    return this.emojiView;
  }
  
  public CharSequence getFieldText()
  {
    if ((this.messageEditText != null) && (this.messageEditText.length() > 0)) {
      return this.messageEditText.getText();
    }
    return null;
  }
  
  public int getSelectionLength()
  {
    if (this.messageEditText == null) {
      return 0;
    }
    try
    {
      int i = this.messageEditText.getSelectionEnd();
      int j = this.messageEditText.getSelectionStart();
      return i - j;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return 0;
  }
  
  public ImageView getSendButton()
  {
    return this.sendButton;
  }
  
  public boolean hasAudioToSend()
  {
    return (this.audioToSendMessageObject != null) || (this.videoToSendMessageObject != null);
  }
  
  public boolean hasOverlappingRendering()
  {
    return false;
  }
  
  public boolean hasRecordVideo()
  {
    return this.hasRecordVideo;
  }
  
  public boolean hasText()
  {
    return (this.messageEditText != null) && (this.messageEditText.length() > 0);
  }
  
  public void hidePopup(boolean paramBoolean)
  {
    if (isPopupShowing())
    {
      if ((this.currentPopupContentType == 1) && (paramBoolean) && (this.botButtonsMessageObject != null)) {
        MessagesController.getMainSettings(this.currentAccount).edit().putInt("hidekeyboard_" + this.dialog_id, this.botButtonsMessageObject.getId()).commit();
      }
      if ((paramBoolean) && (this.searchingStickers))
      {
        this.searchingStickers = false;
        this.emojiView.closeSearch(true);
        this.messageEditText.requestFocus();
        setStickersExpanded(false, true);
      }
    }
    else
    {
      return;
    }
    if (this.searchingStickers)
    {
      this.searchingStickers = false;
      this.emojiView.closeSearch(false);
      this.messageEditText.requestFocus();
    }
    showPopup(0, 0);
    removeGifFromInputField();
  }
  
  public void hideTopView(boolean paramBoolean)
  {
    if ((this.topView == null) || (!this.topViewShowed)) {}
    do
    {
      return;
      this.topViewShowed = false;
      this.needShowTopView = false;
    } while (!this.allowShowTopView);
    if (this.currentTopViewAnimation != null)
    {
      this.currentTopViewAnimation.cancel();
      this.currentTopViewAnimation = null;
    }
    if (paramBoolean)
    {
      this.currentTopViewAnimation = new AnimatorSet();
      this.currentTopViewAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.topView, "translationY", new float[] { this.topView.getLayoutParams().height }) });
      this.currentTopViewAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if ((ChatActivityEnterView.this.currentTopViewAnimation != null) && (ChatActivityEnterView.this.currentTopViewAnimation.equals(paramAnonymousAnimator))) {
            ChatActivityEnterView.access$7802(ChatActivityEnterView.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((ChatActivityEnterView.this.currentTopViewAnimation != null) && (ChatActivityEnterView.this.currentTopViewAnimation.equals(paramAnonymousAnimator)))
          {
            ChatActivityEnterView.this.topView.setVisibility(8);
            ChatActivityEnterView.this.resizeForTopView(false);
            ChatActivityEnterView.access$7802(ChatActivityEnterView.this, null);
          }
        }
      });
      this.currentTopViewAnimation.setDuration(200L);
      this.currentTopViewAnimation.start();
      return;
    }
    this.topView.setVisibility(8);
    resizeForTopView(false);
    this.topView.setTranslationY(this.topView.getLayoutParams().height);
  }
  
  public boolean isEditingCaption()
  {
    return this.editingCaption;
  }
  
  public boolean isEditingMessage()
  {
    return this.editingMessageObject != null;
  }
  
  public boolean isInVideoMode()
  {
    return this.videoSendButton.getTag() != null;
  }
  
  public boolean isKeyboardVisible()
  {
    return this.keyboardVisible;
  }
  
  public boolean isMessageWebPageSearchEnabled()
  {
    return this.messageWebPageSearch;
  }
  
  public boolean isPopupShowing()
  {
    return ((this.emojiView != null) && (this.emojiView.getVisibility() == 0)) || ((this.botKeyboardView != null) && (this.botKeyboardView.getVisibility() == 0));
  }
  
  public boolean isPopupView(View paramView)
  {
    return (paramView == this.botKeyboardView) || (paramView == this.emojiView);
  }
  
  public boolean isRecordCircle(View paramView)
  {
    return paramView == this.recordCircle;
  }
  
  public boolean isRecordLocked()
  {
    return (this.recordingAudioVideo) && (this.recordCircle.isSendButtonVisible());
  }
  
  public boolean isRecordingAudioVideo()
  {
    return this.recordingAudioVideo;
  }
  
  public boolean isSendButtonVisible()
  {
    return this.sendButton.getVisibility() == 0;
  }
  
  public boolean isStickersExpanded()
  {
    return this.stickersExpanded;
  }
  
  public boolean isTopViewVisible()
  {
    return (this.topView != null) && (this.topView.getVisibility() == 0);
  }
  
  public void onDestroy()
  {
    this.destroyed = true;
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordStarted);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordStartError);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordStopped);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordProgressChanged);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.audioDidSent);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.audioRouteChanged);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.featuredStickersDidLoaded);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
    if (this.emojiView != null) {
      this.emojiView.onDestroy();
    }
    if (this.wakeLock != null) {}
    try
    {
      this.wakeLock.release();
      this.wakeLock = null;
      if (this.sizeNotifierLayout != null) {
        this.sizeNotifierLayout.setDelegate(null);
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
  
  protected void onDraw(Canvas paramCanvas)
  {
    if ((this.topView != null) && (this.topView.getVisibility() == 0)) {}
    for (int i = (int)this.topView.getTranslationY();; i = 0)
    {
      int j = i + Theme.chat_composeShadowDrawable.getIntrinsicHeight();
      Theme.chat_composeShadowDrawable.setBounds(0, i, getMeasuredWidth(), j);
      Theme.chat_composeShadowDrawable.draw(paramCanvas);
      paramCanvas.drawRect(0.0F, j, getMeasuredWidth(), getMeasuredHeight(), Theme.chat_composeBackgroundPaint);
      return;
    }
  }
  
  public void onEditTimeExpired()
  {
    this.doneButtonContainer.setVisibility(8);
  }
  
  public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
  {
    if (this.recordingAudioVideo) {
      getParent().requestDisallowInterceptTouchEvent(true);
    }
    return super.onInterceptTouchEvent(paramMotionEvent);
  }
  
  public void onPause()
  {
    this.isPaused = true;
    closeKeyboard();
  }
  
  public void onRequestPermissionsResultFragment(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if ((paramInt == 2) && (this.pendingLocationButton != null))
    {
      if ((paramArrayOfInt.length > 0) && (paramArrayOfInt[0] == 0)) {
        SendMessagesHelper.getInstance(this.currentAccount).sendCurrentLocation(this.pendingMessageObject, this.pendingLocationButton);
      }
      this.pendingLocationButton = null;
      this.pendingMessageObject = null;
    }
  }
  
  public void onResume()
  {
    this.isPaused = false;
    if (this.showKeyboardOnResume)
    {
      this.showKeyboardOnResume = false;
      if (!this.searchingStickers) {
        this.messageEditText.requestFocus();
      }
      AndroidUtilities.showKeyboard(this.messageEditText);
      if ((!AndroidUtilities.usingHardwareInput) && (!this.keyboardVisible) && (!AndroidUtilities.isInMultiwindow))
      {
        this.waitingForKeyboardOpen = true;
        AndroidUtilities.cancelRunOnUIThread(this.openKeyboardRunnable);
        AndroidUtilities.runOnUIThread(this.openKeyboardRunnable, 100L);
      }
    }
  }
  
  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    if ((paramInt1 != paramInt3) && (this.stickersExpanded))
    {
      this.searchingStickers = false;
      this.emojiView.closeSearch(false);
      setStickersExpanded(false, false);
    }
    this.videoTimelineView.clearFrames();
  }
  
  public void onSizeChanged(int paramInt, boolean paramBoolean)
  {
    boolean bool1 = true;
    boolean bool2 = true;
    if (this.searchingStickers)
    {
      this.lastSizeChangeValue1 = paramInt;
      this.lastSizeChangeValue2 = paramBoolean;
      if (paramInt > 0) {}
      for (paramBoolean = bool2;; paramBoolean = false)
      {
        this.keyboardVisible = paramBoolean;
        return;
      }
    }
    int i;
    label115:
    int j;
    Object localObject;
    if ((paramInt > AndroidUtilities.dp(50.0F)) && (this.keyboardVisible) && (!AndroidUtilities.isInMultiwindow))
    {
      if (paramBoolean)
      {
        this.keyboardHeightLand = paramInt;
        MessagesController.getGlobalEmojiSettings().edit().putInt("kbd_height_land3", this.keyboardHeightLand).commit();
      }
    }
    else if (isPopupShowing())
    {
      if (!paramBoolean) {
        break label339;
      }
      i = this.keyboardHeightLand;
      j = i;
      if (this.currentPopupContentType == 1)
      {
        j = i;
        if (!this.botKeyboardView.isFullSize()) {
          j = Math.min(this.botKeyboardView.getKeyboardHeight(), i);
        }
      }
      localObject = null;
      if (this.currentPopupContentType != 0) {
        break label347;
      }
      localObject = this.emojiView;
    }
    for (;;)
    {
      if (this.botKeyboardView != null) {
        this.botKeyboardView.setPanelHeight(j);
      }
      FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)((View)localObject).getLayoutParams();
      if ((!this.closeAnimationInProgress) && ((localLayoutParams.width != AndroidUtilities.displaySize.x) || (localLayoutParams.height != j)) && (!this.stickersExpanded))
      {
        localLayoutParams.width = AndroidUtilities.displaySize.x;
        localLayoutParams.height = j;
        ((View)localObject).setLayoutParams(localLayoutParams);
        if (this.sizeNotifierLayout != null)
        {
          this.emojiPadding = localLayoutParams.height;
          this.sizeNotifierLayout.requestLayout();
          onWindowSizeChanged();
        }
      }
      if ((this.lastSizeChangeValue1 != paramInt) || (this.lastSizeChangeValue2 != paramBoolean)) {
        break label364;
      }
      onWindowSizeChanged();
      return;
      this.keyboardHeight = paramInt;
      MessagesController.getGlobalEmojiSettings().edit().putInt("kbd_height", this.keyboardHeight).commit();
      break;
      label339:
      i = this.keyboardHeight;
      break label115;
      label347:
      if (this.currentPopupContentType == 1) {
        localObject = this.botKeyboardView;
      }
    }
    label364:
    this.lastSizeChangeValue1 = paramInt;
    this.lastSizeChangeValue2 = paramBoolean;
    bool2 = this.keyboardVisible;
    if (paramInt > 0) {}
    for (paramBoolean = bool1;; paramBoolean = false)
    {
      this.keyboardVisible = paramBoolean;
      if ((this.keyboardVisible) && (isPopupShowing())) {
        showPopup(0, this.currentPopupContentType);
      }
      if ((this.emojiPadding != 0) && (!this.keyboardVisible) && (this.keyboardVisible != bool2) && (!isPopupShowing()))
      {
        this.emojiPadding = 0;
        this.sizeNotifierLayout.requestLayout();
      }
      if ((this.keyboardVisible) && (this.waitingForKeyboardOpen))
      {
        this.waitingForKeyboardOpen = false;
        AndroidUtilities.cancelRunOnUIThread(this.openKeyboardRunnable);
      }
      onWindowSizeChanged();
      return;
    }
  }
  
  public void onStickerSelected(TLRPC.Document paramDocument)
  {
    if (this.searchingStickers)
    {
      this.searchingStickers = false;
      this.emojiView.closeSearch(true);
      this.emojiView.hideSearchKeyboard();
    }
    setStickersExpanded(false, true);
    SendMessagesHelper.getInstance(this.currentAccount).sendSticker(paramDocument, this.dialog_id, this.replyingMessageObject);
    if (this.delegate != null) {
      this.delegate.onMessageSend(null);
    }
  }
  
  public void openKeyboard()
  {
    AndroidUtilities.showKeyboard(this.messageEditText);
  }
  
  public boolean processSendingText(CharSequence paramCharSequence)
  {
    paramCharSequence = AndroidUtilities.getTrimmedString(paramCharSequence);
    if (paramCharSequence.length() != 0)
    {
      int j = (int)Math.ceil(paramCharSequence.length() / 4096.0F);
      int i = 0;
      while (i < j)
      {
        CharSequence[] arrayOfCharSequence = new CharSequence[1];
        arrayOfCharSequence[0] = paramCharSequence.subSequence(i * 4096, Math.min((i + 1) * 4096, paramCharSequence.length()));
        ArrayList localArrayList = DataQuery.getInstance(this.currentAccount).getEntities(arrayOfCharSequence);
        SendMessagesHelper.getInstance(this.currentAccount).sendMessage(arrayOfCharSequence[0].toString(), this.dialog_id, this.replyingMessageObject, this.messageWebPage, this.messageWebPageSearch, localArrayList, null, null);
        i += 1;
      }
      return true;
    }
    return false;
  }
  
  public void replaceWithText(int paramInt1, int paramInt2, CharSequence paramCharSequence, boolean paramBoolean)
  {
    try
    {
      SpannableStringBuilder localSpannableStringBuilder = new SpannableStringBuilder(this.messageEditText.getText());
      localSpannableStringBuilder.replace(paramInt1, paramInt1 + paramInt2, paramCharSequence);
      if (paramBoolean) {
        Emoji.replaceEmoji(localSpannableStringBuilder, this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
      }
      this.messageEditText.setText(localSpannableStringBuilder);
      this.messageEditText.setSelection(paramCharSequence.length() + paramInt1);
      return;
    }
    catch (Exception paramCharSequence)
    {
      FileLog.e(paramCharSequence);
    }
  }
  
  public void setAllowStickersAndGifs(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (((this.allowStickers != paramBoolean1) || (this.allowGifs != paramBoolean2)) && (this.emojiView != null))
    {
      if (this.emojiView.getVisibility() == 0) {
        hidePopup(false);
      }
      this.sizeNotifierLayout.removeView(this.emojiView);
      this.emojiView = null;
    }
    this.allowStickers = paramBoolean1;
    this.allowGifs = paramBoolean2;
    setEmojiButtonImage();
  }
  
  public void setBotsCount(int paramInt, boolean paramBoolean)
  {
    this.botCount = paramInt;
    if (this.hasBotCommands != paramBoolean)
    {
      this.hasBotCommands = paramBoolean;
      updateBotButton();
    }
  }
  
  public void setButtons(MessageObject paramMessageObject)
  {
    setButtons(paramMessageObject, true);
  }
  
  public void setButtons(MessageObject paramMessageObject, boolean paramBoolean)
  {
    if ((this.replyingMessageObject != null) && (this.replyingMessageObject == this.botButtonsMessageObject) && (this.replyingMessageObject != paramMessageObject)) {
      this.botMessageObject = paramMessageObject;
    }
    while ((this.botButton == null) || ((this.botButtonsMessageObject != null) && (this.botButtonsMessageObject == paramMessageObject)) || ((this.botButtonsMessageObject == null) && (paramMessageObject == null))) {
      return;
    }
    if (this.botKeyboardView == null)
    {
      this.botKeyboardView = new BotKeyboardView(this.parentActivity);
      this.botKeyboardView.setVisibility(8);
      this.botKeyboardView.setDelegate(new BotKeyboardView.BotKeyboardViewDelegate()
      {
        public void didPressedButton(TLRPC.KeyboardButton paramAnonymousKeyboardButton)
        {
          MessageObject localMessageObject1;
          MessageObject localMessageObject2;
          if (ChatActivityEnterView.this.replyingMessageObject != null)
          {
            localMessageObject1 = ChatActivityEnterView.this.replyingMessageObject;
            ChatActivityEnterView localChatActivityEnterView = ChatActivityEnterView.this;
            if (ChatActivityEnterView.this.replyingMessageObject == null) {
              break label133;
            }
            localMessageObject2 = ChatActivityEnterView.this.replyingMessageObject;
            label42:
            localChatActivityEnterView.didPressedBotButton(paramAnonymousKeyboardButton, localMessageObject1, localMessageObject2);
            if (ChatActivityEnterView.this.replyingMessageObject == null) {
              break label144;
            }
            ChatActivityEnterView.this.openKeyboardInternal();
            ChatActivityEnterView.this.setButtons(ChatActivityEnterView.this.botMessageObject, false);
          }
          for (;;)
          {
            if (ChatActivityEnterView.this.delegate != null) {
              ChatActivityEnterView.this.delegate.onMessageSend(null);
            }
            return;
            if ((int)ChatActivityEnterView.this.dialog_id < 0)
            {
              localMessageObject1 = ChatActivityEnterView.this.botButtonsMessageObject;
              break;
            }
            localMessageObject1 = null;
            break;
            label133:
            localMessageObject2 = ChatActivityEnterView.this.botButtonsMessageObject;
            break label42;
            label144:
            if (ChatActivityEnterView.this.botButtonsMessageObject.messageOwner.reply_markup.single_use)
            {
              ChatActivityEnterView.this.openKeyboardInternal();
              MessagesController.getMainSettings(ChatActivityEnterView.this.currentAccount).edit().putInt("answered_" + ChatActivityEnterView.this.dialog_id, ChatActivityEnterView.this.botButtonsMessageObject.getId()).commit();
            }
          }
        }
      });
      this.sizeNotifierLayout.addView(this.botKeyboardView);
    }
    this.botButtonsMessageObject = paramMessageObject;
    Object localObject;
    label156:
    int i;
    if ((paramMessageObject != null) && ((paramMessageObject.messageOwner.reply_markup instanceof TLRPC.TL_replyKeyboardMarkup)))
    {
      localObject = (TLRPC.TL_replyKeyboardMarkup)paramMessageObject.messageOwner.reply_markup;
      this.botReplyMarkup = ((TLRPC.TL_replyKeyboardMarkup)localObject);
      localObject = this.botKeyboardView;
      if (AndroidUtilities.displaySize.x <= AndroidUtilities.displaySize.y) {
        break label358;
      }
      i = this.keyboardHeightLand;
      label188:
      ((BotKeyboardView)localObject).setPanelHeight(i);
      this.botKeyboardView.setButtons(this.botReplyMarkup);
      if (this.botReplyMarkup == null) {
        break label371;
      }
      localObject = MessagesController.getMainSettings(this.currentAccount);
      if (((SharedPreferences)localObject).getInt("hidekeyboard_" + this.dialog_id, 0) != paramMessageObject.getId()) {
        break label366;
      }
      i = 1;
      label261:
      if ((this.botButtonsMessageObject != this.replyingMessageObject) && (this.botReplyMarkup.single_use) && (((SharedPreferences)localObject).getInt("answered_" + this.dialog_id, 0) == paramMessageObject.getId())) {
        break label369;
      }
      if ((i == 0) && (this.messageEditText.length() == 0) && (!isPopupShowing())) {
        showPopup(1, 1);
      }
    }
    for (;;)
    {
      updateBotButton();
      return;
      localObject = null;
      break label156;
      label358:
      i = this.keyboardHeight;
      break label188;
      label366:
      i = 0;
      break label261;
      label369:
      break;
      label371:
      if ((isPopupShowing()) && (this.currentPopupContentType == 1)) {
        if (paramBoolean) {
          openKeyboardInternal();
        } else {
          showPopup(0, 1);
        }
      }
    }
  }
  
  public void setCaption(String paramString)
  {
    if (this.messageEditText != null)
    {
      this.messageEditText.setCaption(paramString);
      checkSendButton(true);
    }
  }
  
  public void setChatInfo(TLRPC.ChatFull paramChatFull)
  {
    this.info = paramChatFull;
    if (this.emojiView != null) {
      this.emojiView.setChatInfo(this.info);
    }
  }
  
  public void setCommand(MessageObject paramMessageObject, String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramString == null) || (getVisibility() != 0)) {
      return;
    }
    if (paramBoolean1)
    {
      String str = this.messageEditText.getText().toString();
      if ((paramMessageObject != null) && ((int)this.dialog_id < 0))
      {
        paramMessageObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramMessageObject.messageOwner.from_id));
        label61:
        if (((this.botCount == 1) && (!paramBoolean2)) || (paramMessageObject == null) || (!paramMessageObject.bot) || (paramString.contains("@"))) {
          break label239;
        }
      }
      label239:
      for (paramMessageObject = String.format(Locale.US, "%s@%s", new Object[] { paramString, paramMessageObject.username }) + " " + str.replaceFirst("^/[a-zA-Z@\\d_]{1,255}(\\s|$)", "");; paramMessageObject = paramString + " " + str.replaceFirst("^/[a-zA-Z@\\d_]{1,255}(\\s|$)", ""))
      {
        this.ignoreTextChange = true;
        this.messageEditText.setText(paramMessageObject);
        this.messageEditText.setSelection(this.messageEditText.getText().length());
        this.ignoreTextChange = false;
        if (this.delegate != null) {
          this.delegate.onTextChanged(this.messageEditText.getText(), true);
        }
        if ((this.keyboardVisible) || (this.currentPopupContentType != -1)) {
          break;
        }
        openKeyboard();
        return;
        paramMessageObject = null;
        break label61;
      }
    }
    if ((paramMessageObject != null) && ((int)this.dialog_id < 0)) {}
    for (paramMessageObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramMessageObject.messageOwner.from_id)); ((this.botCount != 1) || (paramBoolean2)) && (paramMessageObject != null) && (paramMessageObject.bot) && (!paramString.contains("@")); paramMessageObject = null)
    {
      SendMessagesHelper.getInstance(this.currentAccount).sendMessage(String.format(Locale.US, "%s@%s", new Object[] { paramString, paramMessageObject.username }), this.dialog_id, this.replyingMessageObject, null, false, null, null, null);
      return;
    }
    SendMessagesHelper.getInstance(this.currentAccount).sendMessage(paramString, this.dialog_id, this.replyingMessageObject, null, false, null, null, null);
  }
  
  public void setDelegate(ChatActivityEnterViewDelegate paramChatActivityEnterViewDelegate)
  {
    this.delegate = paramChatActivityEnterViewDelegate;
  }
  
  public void setDialogId(long paramLong, int paramInt)
  {
    int i = 1;
    this.dialog_id = paramLong;
    if (this.currentAccount != paramInt)
    {
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordStarted);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordStartError);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordStopped);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recordProgressChanged);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.audioDidSent);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.audioRouteChanged);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.featuredStickersDidLoaded);
      this.currentAccount = paramInt;
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordStarted);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordStartError);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordStopped);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recordProgressChanged);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.audioDidSent);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.audioRouteChanged);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingDidReset);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.featuredStickersDidLoaded);
    }
    paramInt = (int)this.dialog_id;
    paramInt = (int)(this.dialog_id >> 32);
    boolean bool;
    label459:
    label482:
    float f;
    if ((int)this.dialog_id < 0)
    {
      Object localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-(int)this.dialog_id));
      this.silent = MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("silent_" + this.dialog_id, false);
      if ((!ChatObject.isChannel((TLRPC.Chat)localObject)) || ((!((TLRPC.Chat)localObject).creator) && ((((TLRPC.Chat)localObject).admin_rights == null) || (!((TLRPC.Chat)localObject).admin_rights.post_messages))) || (((TLRPC.Chat)localObject).megagroup)) {
        break label582;
      }
      bool = true;
      this.canWriteToChannel = bool;
      if (this.notifyButton != null)
      {
        localObject = this.notifyButton;
        if (!this.canWriteToChannel) {
          break label588;
        }
        paramInt = 0;
        ((ImageView)localObject).setVisibility(paramInt);
        localObject = this.notifyButton;
        if (!this.silent) {
          break label594;
        }
        paramInt = 2131165560;
        ((ImageView)localObject).setImageResource(paramInt);
        localObject = this.attachLayout;
        if (((this.botButton != null) && (this.botButton.getVisibility() != 8)) || ((this.notifyButton != null) && (this.notifyButton.getVisibility() != 8))) {
          break label601;
        }
        f = 48.0F;
        label537:
        ((LinearLayout)localObject).setPivotX(AndroidUtilities.dp(f));
      }
      if (this.attachLayout != null) {
        if (this.attachLayout.getVisibility() != 0) {
          break label609;
        }
      }
    }
    label582:
    label588:
    label594:
    label601:
    label609:
    for (paramInt = i;; paramInt = 0)
    {
      updateFieldRight(paramInt);
      checkRoundVideo();
      updateFieldHint();
      return;
      bool = false;
      break;
      paramInt = 8;
      break label459;
      paramInt = 2131165561;
      break label482;
      f = 96.0F;
      break label537;
    }
  }
  
  public void setEditingMessageObject(MessageObject paramMessageObject, boolean paramBoolean)
  {
    if ((this.audioToSend != null) || (this.videoToSendMessageObject != null) || (this.editingMessageObject == paramMessageObject)) {
      return;
    }
    if (this.editingMessageReqId != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.editingMessageReqId, true);
      this.editingMessageReqId = 0;
    }
    this.editingMessageObject = paramMessageObject;
    this.editingCaption = paramBoolean;
    InputFilter[] arrayOfInputFilter;
    ArrayList localArrayList;
    Object localObject;
    int i;
    int k;
    int j;
    if (this.editingMessageObject != null)
    {
      if (this.doneButtonAnimation != null)
      {
        this.doneButtonAnimation.cancel();
        this.doneButtonAnimation = null;
      }
      this.doneButtonContainer.setVisibility(0);
      showEditDoneProgress(true, false);
      arrayOfInputFilter = new InputFilter[1];
      if (paramBoolean)
      {
        arrayOfInputFilter[0] = new InputFilter.LengthFilter(200);
        paramMessageObject = this.editingMessageObject.caption;
      }
      for (;;)
      {
        if (paramMessageObject != null)
        {
          localArrayList = this.editingMessageObject.messageOwner.entities;
          DataQuery.sortEntities(localArrayList);
          paramMessageObject = new SpannableStringBuilder(paramMessageObject);
          localObject = paramMessageObject.getSpans(0, paramMessageObject.length(), Object.class);
          if ((localObject != null) && (localObject.length > 0))
          {
            i = 0;
            for (;;)
            {
              if (i < localObject.length)
              {
                paramMessageObject.removeSpan(localObject[i]);
                i += 1;
                continue;
                arrayOfInputFilter[0] = new InputFilter.LengthFilter(4096);
                paramMessageObject = this.editingMessageObject.messageText;
                break;
              }
            }
          }
          if (localArrayList != null)
          {
            k = 0;
            j = 0;
          }
        }
      }
    }
    for (;;)
    {
      try
      {
        if (j < localArrayList.size())
        {
          localObject = (TLRPC.MessageEntity)localArrayList.get(j);
          if (((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k > paramMessageObject.length())
          {
            i = k;
          }
          else if ((localObject instanceof TLRPC.TL_inputMessageEntityMentionName))
          {
            if ((((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k < paramMessageObject.length()) && (paramMessageObject.charAt(((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k) == ' ')) {
              ((TLRPC.MessageEntity)localObject).length += 1;
            }
            paramMessageObject.setSpan(new URLSpanUserMention("" + ((TLRPC.TL_inputMessageEntityMentionName)localObject).user_id.user_id, 1), ((TLRPC.MessageEntity)localObject).offset + k, ((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k, 33);
            i = k;
          }
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
      setFieldText(Emoji.replaceEmoji(new SpannableStringBuilder(paramMessageObject), this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20.0F), false));
      this.messageEditText.setFilters(arrayOfInputFilter);
      openKeyboard();
      paramMessageObject = (FrameLayout.LayoutParams)this.messageEditText.getLayoutParams();
      paramMessageObject.rightMargin = AndroidUtilities.dp(4.0F);
      this.messageEditText.setLayoutParams(paramMessageObject);
      this.sendButton.setVisibility(8);
      this.cancelBotButton.setVisibility(8);
      this.audioVideoButtonContainer.setVisibility(8);
      this.attachLayout.setVisibility(8);
      this.sendButtonContainer.setVisibility(8);
      for (;;)
      {
        updateFieldHint();
        return;
        if ((localObject instanceof TLRPC.TL_messageEntityMentionName))
        {
          if ((((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k < paramMessageObject.length()) && (paramMessageObject.charAt(((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k) == ' ')) {
            ((TLRPC.MessageEntity)localObject).length += 1;
          }
          paramMessageObject.setSpan(new URLSpanUserMention("" + ((TLRPC.TL_messageEntityMentionName)localObject).user_id, 1), ((TLRPC.MessageEntity)localObject).offset + k, ((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k, 33);
          i = k;
          break label1130;
        }
        if ((localObject instanceof TLRPC.TL_messageEntityCode))
        {
          paramMessageObject.insert(((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k, "`");
          paramMessageObject.insert(((TLRPC.MessageEntity)localObject).offset + k, "`");
          i = k + 2;
          break label1130;
        }
        if ((localObject instanceof TLRPC.TL_messageEntityPre))
        {
          paramMessageObject.insert(((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k, "```");
          paramMessageObject.insert(((TLRPC.MessageEntity)localObject).offset + k, "```");
          i = k + 6;
          break label1130;
        }
        if ((localObject instanceof TLRPC.TL_messageEntityBold))
        {
          paramMessageObject.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), ((TLRPC.MessageEntity)localObject).offset + k, ((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k, 33);
          i = k;
          break label1130;
        }
        i = k;
        if (!(localObject instanceof TLRPC.TL_messageEntityItalic)) {
          break label1130;
        }
        paramMessageObject.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/ritalic.ttf")), ((TLRPC.MessageEntity)localObject).offset + k, ((TLRPC.MessageEntity)localObject).offset + ((TLRPC.MessageEntity)localObject).length + k, 33);
        i = k;
        break label1130;
        setFieldText("");
        break;
        this.doneButtonContainer.setVisibility(8);
        this.messageEditText.setFilters(new InputFilter[0]);
        this.delegate.onMessageEditEnd(false);
        this.audioVideoButtonContainer.setVisibility(0);
        this.attachLayout.setVisibility(0);
        this.sendButtonContainer.setVisibility(0);
        this.attachLayout.setScaleX(1.0F);
        this.attachLayout.setAlpha(1.0F);
        this.sendButton.setScaleX(0.1F);
        this.sendButton.setScaleY(0.1F);
        this.sendButton.setAlpha(0.0F);
        this.cancelBotButton.setScaleX(0.1F);
        this.cancelBotButton.setScaleY(0.1F);
        this.cancelBotButton.setAlpha(0.0F);
        this.audioVideoButtonContainer.setScaleX(1.0F);
        this.audioVideoButtonContainer.setScaleY(1.0F);
        this.audioVideoButtonContainer.setAlpha(1.0F);
        this.sendButton.setVisibility(8);
        this.cancelBotButton.setVisibility(8);
        this.messageEditText.setText("");
        if (getVisibility() == 0) {
          this.delegate.onAttachButtonShow();
        }
        updateFieldRight(1);
      }
      label1130:
      j += 1;
      k = i;
    }
  }
  
  public void setFieldFocused()
  {
    if (this.messageEditText != null) {}
    try
    {
      this.messageEditText.requestFocus();
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public void setFieldFocused(boolean paramBoolean)
  {
    if (this.messageEditText == null) {}
    do
    {
      do
      {
        return;
        if (!paramBoolean) {
          break;
        }
      } while ((this.searchingStickers) || (this.messageEditText.isFocused()));
      this.messageEditText.postDelayed(new Runnable()
      {
        public void run()
        {
          if (ChatActivityEnterView.this.messageEditText != null) {}
          try
          {
            ChatActivityEnterView.this.messageEditText.requestFocus();
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
          }
        }
      }, 600L);
      return;
    } while ((!this.messageEditText.isFocused()) || (this.keyboardVisible));
    this.messageEditText.clearFocus();
  }
  
  public void setFieldText(CharSequence paramCharSequence)
  {
    if (this.messageEditText == null) {}
    do
    {
      return;
      this.ignoreTextChange = true;
      this.messageEditText.setText(paramCharSequence);
      this.messageEditText.setSelection(this.messageEditText.getText().length());
      this.ignoreTextChange = false;
    } while (this.delegate == null);
    this.delegate.onTextChanged(this.messageEditText.getText(), true);
  }
  
  public void setForceShowSendButton(boolean paramBoolean1, boolean paramBoolean2)
  {
    this.forceShowSendButton = paramBoolean1;
    checkSendButton(paramBoolean2);
  }
  
  public void setOpenGifsTabFirst()
  {
    createEmojiView();
    DataQuery.getInstance(this.currentAccount).loadRecents(0, true, true, false);
    this.emojiView.switchToGifRecent();
  }
  
  public void setReplyingMessageObject(MessageObject paramMessageObject)
  {
    if (paramMessageObject != null)
    {
      if ((this.botMessageObject == null) && (this.botButtonsMessageObject != this.replyingMessageObject)) {
        this.botMessageObject = this.botButtonsMessageObject;
      }
      this.replyingMessageObject = paramMessageObject;
      setButtons(this.replyingMessageObject, true);
    }
    for (;;)
    {
      MediaController.getInstance().setReplyingMessage(paramMessageObject);
      return;
      if ((paramMessageObject == null) && (this.replyingMessageObject == this.botButtonsMessageObject))
      {
        this.replyingMessageObject = null;
        setButtons(this.botMessageObject, false);
        this.botMessageObject = null;
      }
      else
      {
        this.replyingMessageObject = paramMessageObject;
      }
    }
  }
  
  public void setSelection(int paramInt)
  {
    if (this.messageEditText == null) {
      return;
    }
    this.messageEditText.setSelection(paramInt, this.messageEditText.length());
  }
  
  public void setWebPage(TLRPC.WebPage paramWebPage, boolean paramBoolean)
  {
    this.messageWebPage = paramWebPage;
    this.messageWebPageSearch = paramBoolean;
  }
  
  public void showContextProgress(boolean paramBoolean)
  {
    if (this.progressDrawable == null) {
      return;
    }
    if (paramBoolean)
    {
      this.progressDrawable.startAnimation();
      return;
    }
    this.progressDrawable.stopAnimation();
  }
  
  public void showEditDoneProgress(final boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.doneButtonAnimation != null) {
      this.doneButtonAnimation.cancel();
    }
    if (!paramBoolean2)
    {
      if (paramBoolean1)
      {
        this.doneButtonImage.setScaleX(0.1F);
        this.doneButtonImage.setScaleY(0.1F);
        this.doneButtonImage.setAlpha(0.0F);
        this.doneButtonProgress.setScaleX(1.0F);
        this.doneButtonProgress.setScaleY(1.0F);
        this.doneButtonProgress.setAlpha(1.0F);
        this.doneButtonImage.setVisibility(4);
        this.doneButtonProgress.setVisibility(0);
        this.doneButtonContainer.setEnabled(false);
        return;
      }
      this.doneButtonProgress.setScaleX(0.1F);
      this.doneButtonProgress.setScaleY(0.1F);
      this.doneButtonProgress.setAlpha(0.0F);
      this.doneButtonImage.setScaleX(1.0F);
      this.doneButtonImage.setScaleY(1.0F);
      this.doneButtonImage.setAlpha(1.0F);
      this.doneButtonImage.setVisibility(0);
      this.doneButtonProgress.setVisibility(4);
      this.doneButtonContainer.setEnabled(true);
      return;
    }
    this.doneButtonAnimation = new AnimatorSet();
    if (paramBoolean1)
    {
      this.doneButtonProgress.setVisibility(0);
      this.doneButtonContainer.setEnabled(false);
      this.doneButtonAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.doneButtonImage, "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.doneButtonImage, "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.doneButtonImage, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.doneButtonProgress, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneButtonProgress, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneButtonProgress, "alpha", new float[] { 1.0F }) });
    }
    for (;;)
    {
      this.doneButtonAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if ((ChatActivityEnterView.this.doneButtonAnimation != null) && (ChatActivityEnterView.this.doneButtonAnimation.equals(paramAnonymousAnimator))) {
            ChatActivityEnterView.access$8002(ChatActivityEnterView.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((ChatActivityEnterView.this.doneButtonAnimation != null) && (ChatActivityEnterView.this.doneButtonAnimation.equals(paramAnonymousAnimator)))
          {
            if (!paramBoolean1) {
              ChatActivityEnterView.this.doneButtonProgress.setVisibility(4);
            }
          }
          else {
            return;
          }
          ChatActivityEnterView.this.doneButtonImage.setVisibility(4);
        }
      });
      this.doneButtonAnimation.setDuration(150L);
      this.doneButtonAnimation.start();
      return;
      this.doneButtonImage.setVisibility(0);
      this.doneButtonContainer.setEnabled(true);
      this.doneButtonAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.doneButtonProgress, "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.doneButtonProgress, "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.doneButtonProgress, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.doneButtonImage, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneButtonImage, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneButtonImage, "alpha", new float[] { 1.0F }) });
    }
  }
  
  public void showTopView(boolean paramBoolean1, final boolean paramBoolean2)
  {
    if ((this.topView == null) || (this.topViewShowed) || (getVisibility() != 0)) {
      if ((this.recordedAudioPanel.getVisibility() != 0) && ((!this.forceShowSendButton) || (paramBoolean2))) {
        openKeyboard();
      }
    }
    do
    {
      do
      {
        do
        {
          return;
          this.needShowTopView = true;
          this.topViewShowed = true;
        } while (!this.allowShowTopView);
        this.topView.setVisibility(0);
        if (this.currentTopViewAnimation != null)
        {
          this.currentTopViewAnimation.cancel();
          this.currentTopViewAnimation = null;
        }
        resizeForTopView(true);
        if (!paramBoolean1) {
          break;
        }
        if ((this.keyboardVisible) || (isPopupShowing()))
        {
          this.currentTopViewAnimation = new AnimatorSet();
          this.currentTopViewAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.topView, "translationY", new float[] { 0.0F }) });
          this.currentTopViewAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationCancel(Animator paramAnonymousAnimator)
            {
              if ((ChatActivityEnterView.this.currentTopViewAnimation != null) && (ChatActivityEnterView.this.currentTopViewAnimation.equals(paramAnonymousAnimator))) {
                ChatActivityEnterView.access$7802(ChatActivityEnterView.this, null);
              }
            }
            
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              if ((ChatActivityEnterView.this.currentTopViewAnimation != null) && (ChatActivityEnterView.this.currentTopViewAnimation.equals(paramAnonymousAnimator)))
              {
                if ((ChatActivityEnterView.this.recordedAudioPanel.getVisibility() != 0) && ((!ChatActivityEnterView.this.forceShowSendButton) || (paramBoolean2))) {
                  ChatActivityEnterView.this.openKeyboard();
                }
                ChatActivityEnterView.access$7802(ChatActivityEnterView.this, null);
              }
            }
          });
          this.currentTopViewAnimation.setDuration(200L);
          this.currentTopViewAnimation.start();
          return;
        }
        this.topView.setTranslationY(0.0F);
      } while ((this.recordedAudioPanel.getVisibility() == 0) || ((this.forceShowSendButton) && (!paramBoolean2)));
      openKeyboard();
      return;
      this.topView.setTranslationY(0.0F);
    } while ((this.recordedAudioPanel.getVisibility() == 0) || ((this.forceShowSendButton) && (!paramBoolean2)));
    openKeyboard();
  }
  
  private class AnimatedArrowDrawable
    extends Drawable
  {
    private float animProgress = 0.0F;
    private Paint paint = new Paint(1);
    private Path path = new Path();
    
    public AnimatedArrowDrawable(int paramInt)
    {
      this.paint.setStyle(Paint.Style.STROKE);
      this.paint.setStrokeWidth(AndroidUtilities.dp(2.0F));
      this.paint.setColor(paramInt);
      updatePath();
    }
    
    private void updatePath()
    {
      this.path.reset();
      float f = this.animProgress * 2.0F - 1.0F;
      this.path.moveTo(AndroidUtilities.dp(3.0F), AndroidUtilities.dp(12.0F) - AndroidUtilities.dp(4.0F) * f);
      this.path.lineTo(AndroidUtilities.dp(13.0F), AndroidUtilities.dp(12.0F) + AndroidUtilities.dp(4.0F) * f);
      this.path.lineTo(AndroidUtilities.dp(23.0F), AndroidUtilities.dp(12.0F) - AndroidUtilities.dp(4.0F) * f);
    }
    
    public void draw(Canvas paramCanvas)
    {
      paramCanvas.drawPath(this.path, this.paint);
    }
    
    public float getAnimationProgress()
    {
      return this.animProgress;
    }
    
    public int getIntrinsicHeight()
    {
      return AndroidUtilities.dp(26.0F);
    }
    
    public int getIntrinsicWidth()
    {
      return AndroidUtilities.dp(26.0F);
    }
    
    public int getOpacity()
    {
      return -2;
    }
    
    public void setAlpha(int paramInt) {}
    
    @Keep
    public void setAnimationProgress(float paramFloat)
    {
      this.animProgress = paramFloat;
      updatePath();
      invalidateSelf();
    }
    
    public void setColorFilter(ColorFilter paramColorFilter) {}
  }
  
  public static abstract interface ChatActivityEnterViewDelegate
  {
    public abstract void didPressedAttachButton();
    
    public abstract void needChangeVideoPreviewState(int paramInt, float paramFloat);
    
    public abstract void needSendTyping();
    
    public abstract void needShowMediaBanHint();
    
    public abstract void needStartRecordAudio(int paramInt);
    
    public abstract void needStartRecordVideo(int paramInt);
    
    public abstract void onAttachButtonHidden();
    
    public abstract void onAttachButtonShow();
    
    public abstract void onMessageEditEnd(boolean paramBoolean);
    
    public abstract void onMessageSend(CharSequence paramCharSequence);
    
    public abstract void onPreAudioVideoRecord();
    
    public abstract void onStickersExpandedChange();
    
    public abstract void onStickersTab(boolean paramBoolean);
    
    public abstract void onSwitchRecordMode(boolean paramBoolean);
    
    public abstract void onTextChanged(CharSequence paramCharSequence, boolean paramBoolean);
    
    public abstract void onWindowSizeChanged(int paramInt);
  }
  
  private class RecordCircle
    extends View
  {
    private float amplitude;
    private float animateAmplitudeDiff;
    private float animateToAmplitude;
    private long lastUpdateTime;
    private float lockAnimatedTranslation;
    private boolean pressed;
    private float scale;
    private boolean sendButtonVisible;
    private float startTranslation;
    
    public RecordCircle(Context paramContext)
    {
      super();
      ChatActivityEnterView.this.paint.setColor(Theme.getColor("chat_messagePanelVoiceBackground"));
      ChatActivityEnterView.this.paintRecord.setColor(Theme.getColor("chat_messagePanelVoiceShadow"));
      ChatActivityEnterView.access$3102(ChatActivityEnterView.this, getResources().getDrawable(2131165475));
      ChatActivityEnterView.this.lockDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("key_chat_messagePanelVoiceLock"), PorterDuff.Mode.MULTIPLY));
      ChatActivityEnterView.access$3202(ChatActivityEnterView.this, getResources().getDrawable(2131165479));
      ChatActivityEnterView.this.lockTopDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("key_chat_messagePanelVoiceLock"), PorterDuff.Mode.MULTIPLY));
      ChatActivityEnterView.access$3302(ChatActivityEnterView.this, getResources().getDrawable(2131165473));
      ChatActivityEnterView.this.lockArrowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("key_chat_messagePanelVoiceLock"), PorterDuff.Mode.MULTIPLY));
      ChatActivityEnterView.access$3402(ChatActivityEnterView.this, getResources().getDrawable(2131165477));
      ChatActivityEnterView.this.lockBackgroundDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("key_chat_messagePanelVoiceLockBackground"), PorterDuff.Mode.MULTIPLY));
      ChatActivityEnterView.access$3502(ChatActivityEnterView.this, getResources().getDrawable(2131165478));
      ChatActivityEnterView.this.lockShadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("key_chat_messagePanelVoiceLockShadow"), PorterDuff.Mode.MULTIPLY));
      ChatActivityEnterView.access$3602(ChatActivityEnterView.this, getResources().getDrawable(2131165503).mutate());
      ChatActivityEnterView.this.micDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelVoicePressed"), PorterDuff.Mode.MULTIPLY));
      ChatActivityEnterView.access$3702(ChatActivityEnterView.this, getResources().getDrawable(2131165395).mutate());
      ChatActivityEnterView.this.cameraDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelVoicePressed"), PorterDuff.Mode.MULTIPLY));
      ChatActivityEnterView.access$3802(ChatActivityEnterView.this, getResources().getDrawable(2131165407).mutate());
      ChatActivityEnterView.this.sendDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_messagePanelVoicePressed"), PorterDuff.Mode.MULTIPLY));
    }
    
    public float getLockAnimatedTranslation()
    {
      return this.lockAnimatedTranslation;
    }
    
    public float getScale()
    {
      return this.scale;
    }
    
    public boolean isSendButtonVisible()
    {
      return this.sendButtonVisible;
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      int i1 = getMeasuredWidth() / 2;
      int i = AndroidUtilities.dp(170.0F);
      float f1 = 0.0F;
      float f2;
      if (this.lockAnimatedTranslation != 10000.0F)
      {
        f2 = Math.max(0, (int)(this.startTranslation - this.lockAnimatedTranslation));
        f1 = f2;
        if (f2 > AndroidUtilities.dp(57.0F)) {
          f1 = AndroidUtilities.dp(57.0F);
        }
      }
      i = (int)(i - f1);
      float f3;
      label165:
      Drawable localDrawable;
      float f4;
      float f5;
      int i2;
      int k;
      int n;
      int m;
      int j;
      if (this.scale <= 0.5F)
      {
        f3 = this.scale / 0.5F;
        f2 = f3;
        long l1 = System.currentTimeMillis();
        long l2 = this.lastUpdateTime;
        if (this.animateToAmplitude != this.amplitude)
        {
          this.amplitude += this.animateAmplitudeDiff * (float)(l1 - l2);
          if (this.animateAmplitudeDiff <= 0.0F) {
            break label930;
          }
          if (this.amplitude > this.animateToAmplitude) {
            this.amplitude = this.animateToAmplitude;
          }
          invalidate();
        }
        this.lastUpdateTime = System.currentTimeMillis();
        if (this.amplitude != 0.0F) {
          paramCanvas.drawCircle(getMeasuredWidth() / 2.0F, i, (AndroidUtilities.dp(42.0F) + AndroidUtilities.dp(20.0F) * this.amplitude) * this.scale, ChatActivityEnterView.this.paintRecord);
        }
        paramCanvas.drawCircle(getMeasuredWidth() / 2.0F, i, AndroidUtilities.dp(42.0F) * f3, ChatActivityEnterView.this.paint);
        if (!isSendButtonVisible()) {
          break label953;
        }
        localDrawable = ChatActivityEnterView.this.sendDrawable;
        localDrawable.setBounds(i1 - localDrawable.getIntrinsicWidth() / 2, i - localDrawable.getIntrinsicHeight() / 2, localDrawable.getIntrinsicWidth() / 2 + i1, localDrawable.getIntrinsicHeight() / 2 + i);
        localDrawable.setAlpha((int)(255.0F * f2));
        localDrawable.draw(paramCanvas);
        f4 = 1.0F - f1 / AndroidUtilities.dp(57.0F);
        f5 = Math.max(0.0F, 1.0F - f1 / AndroidUtilities.dp(57.0F) * 2.0F);
        i2 = (int)(255.0F * f2);
        if (!isSendButtonVisible()) {
          break label1000;
        }
        k = AndroidUtilities.dp(31.0F);
        n = AndroidUtilities.dp(57.0F) + (int)(AndroidUtilities.dp(30.0F) * (1.0F - f3) - f1 + AndroidUtilities.dp(20.0F) * f4);
        m = n + AndroidUtilities.dp(5.0F);
        j = n + AndroidUtilities.dp(11.0F);
        i = n + AndroidUtilities.dp(25.0F);
        i2 = (int)(i2 * (f1 / AndroidUtilities.dp(57.0F)));
        ChatActivityEnterView.this.lockBackgroundDrawable.setAlpha(255);
        ChatActivityEnterView.this.lockShadowDrawable.setAlpha(255);
        ChatActivityEnterView.this.lockTopDrawable.setAlpha(i2);
        ChatActivityEnterView.this.lockDrawable.setAlpha(i2);
        ChatActivityEnterView.this.lockArrowDrawable.setAlpha((int)(i2 * f5));
      }
      for (;;)
      {
        ChatActivityEnterView.this.lockBackgroundDrawable.setBounds(i1 - AndroidUtilities.dp(15.0F), n, AndroidUtilities.dp(15.0F) + i1, n + k);
        ChatActivityEnterView.this.lockBackgroundDrawable.draw(paramCanvas);
        ChatActivityEnterView.this.lockShadowDrawable.setBounds(i1 - AndroidUtilities.dp(16.0F), n - AndroidUtilities.dp(1.0F), AndroidUtilities.dp(16.0F) + i1, n + k + AndroidUtilities.dp(1.0F));
        ChatActivityEnterView.this.lockShadowDrawable.draw(paramCanvas);
        ChatActivityEnterView.this.lockTopDrawable.setBounds(i1 - AndroidUtilities.dp(6.0F), m, AndroidUtilities.dp(6.0F) + i1, AndroidUtilities.dp(14.0F) + m);
        ChatActivityEnterView.this.lockTopDrawable.draw(paramCanvas);
        ChatActivityEnterView.this.lockDrawable.setBounds(i1 - AndroidUtilities.dp(7.0F), j, AndroidUtilities.dp(7.0F) + i1, AndroidUtilities.dp(12.0F) + j);
        ChatActivityEnterView.this.lockDrawable.draw(paramCanvas);
        ChatActivityEnterView.this.lockArrowDrawable.setBounds(i1 - AndroidUtilities.dp(7.5F), i, AndroidUtilities.dp(7.5F) + i1, AndroidUtilities.dp(9.0F) + i);
        ChatActivityEnterView.this.lockArrowDrawable.draw(paramCanvas);
        if (isSendButtonVisible())
        {
          ChatActivityEnterView.this.redDotPaint.setAlpha(255);
          ChatActivityEnterView.this.rect.set(i1 - AndroidUtilities.dp2(6.5F), AndroidUtilities.dp(9.0F) + n, AndroidUtilities.dp(6.5F) + i1, AndroidUtilities.dp(22.0F) + n);
          paramCanvas.drawRoundRect(ChatActivityEnterView.this.rect, AndroidUtilities.dp(1.0F), AndroidUtilities.dp(1.0F), ChatActivityEnterView.this.redDotPaint);
        }
        return;
        if (this.scale <= 0.75F)
        {
          f3 = 1.0F - (this.scale - 0.5F) / 0.25F * 0.1F;
          f2 = 1.0F;
          break;
        }
        f3 = 0.9F + (this.scale - 0.75F) / 0.25F * 0.1F;
        f2 = 1.0F;
        break;
        label930:
        if (this.amplitude >= this.animateToAmplitude) {
          break label165;
        }
        this.amplitude = this.animateToAmplitude;
        break label165;
        label953:
        if ((ChatActivityEnterView.this.videoSendButton != null) && (ChatActivityEnterView.this.videoSendButton.getTag() != null)) {}
        for (localDrawable = ChatActivityEnterView.this.cameraDrawable;; localDrawable = ChatActivityEnterView.this.micDrawable) {
          break;
        }
        label1000:
        k = AndroidUtilities.dp(31.0F) + (int)(AndroidUtilities.dp(29.0F) * f4);
        n = AndroidUtilities.dp(57.0F) + (int)(AndroidUtilities.dp(30.0F) * (1.0F - f3)) - (int)f1;
        m = AndroidUtilities.dp(5.0F) + n + (int)(AndroidUtilities.dp(4.0F) * f4);
        j = AndroidUtilities.dp(11.0F) + n + (int)(AndroidUtilities.dp(10.0F) * f4);
        i = AndroidUtilities.dp(25.0F) + n + (int)(AndroidUtilities.dp(16.0F) * f4);
        ChatActivityEnterView.this.lockBackgroundDrawable.setAlpha(i2);
        ChatActivityEnterView.this.lockShadowDrawable.setAlpha(i2);
        ChatActivityEnterView.this.lockTopDrawable.setAlpha(i2);
        ChatActivityEnterView.this.lockDrawable.setAlpha(i2);
        ChatActivityEnterView.this.lockArrowDrawable.setAlpha((int)(i2 * f5));
      }
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent)
    {
      if (this.sendButtonVisible)
      {
        int i = (int)paramMotionEvent.getX();
        int j = (int)paramMotionEvent.getY();
        if (paramMotionEvent.getAction() == 0)
        {
          boolean bool = ChatActivityEnterView.this.lockBackgroundDrawable.getBounds().contains(i, j);
          this.pressed = bool;
          if (!bool) {}
        }
        else
        {
          do
          {
            return true;
            if (!this.pressed) {
              break;
            }
          } while ((paramMotionEvent.getAction() != 1) || (!ChatActivityEnterView.this.lockBackgroundDrawable.getBounds().contains(i, j)));
          if ((ChatActivityEnterView.this.videoSendButton != null) && (ChatActivityEnterView.this.videoSendButton.getTag() != null))
          {
            ChatActivityEnterView.this.delegate.needStartRecordVideo(3);
            return true;
          }
          MediaController.getInstance().stopRecording(2);
          ChatActivityEnterView.this.delegate.needStartRecordAudio(0);
          return true;
        }
      }
      return false;
    }
    
    public void setAmplitude(double paramDouble)
    {
      this.animateToAmplitude = ((float)Math.min(100.0D, paramDouble) / 100.0F);
      this.animateAmplitudeDiff = ((this.animateToAmplitude - this.amplitude) / 150.0F);
      this.lastUpdateTime = System.currentTimeMillis();
      invalidate();
    }
    
    @Keep
    public void setLockAnimatedTranslation(float paramFloat)
    {
      this.lockAnimatedTranslation = paramFloat;
      invalidate();
    }
    
    public int setLockTranslation(float paramFloat)
    {
      if (paramFloat == 10000.0F)
      {
        this.sendButtonVisible = false;
        this.lockAnimatedTranslation = -1.0F;
        this.startTranslation = -1.0F;
        invalidate();
        return 0;
      }
      if (this.sendButtonVisible) {
        return 2;
      }
      if (this.lockAnimatedTranslation == -1.0F) {
        this.startTranslation = paramFloat;
      }
      this.lockAnimatedTranslation = paramFloat;
      invalidate();
      if (this.startTranslation - this.lockAnimatedTranslation >= AndroidUtilities.dp(57.0F))
      {
        this.sendButtonVisible = true;
        return 2;
      }
      return 1;
    }
    
    @Keep
    public void setScale(float paramFloat)
    {
      this.scale = paramFloat;
      invalidate();
    }
    
    public void setSendButtonInvisible()
    {
      this.sendButtonVisible = false;
      invalidate();
    }
  }
  
  private class RecordDot
    extends View
  {
    private float alpha;
    private boolean isIncr;
    private long lastUpdateTime;
    
    public RecordDot(Context paramContext)
    {
      super();
      ChatActivityEnterView.this.redDotPaint.setColor(Theme.getColor("chat_recordedVoiceDot"));
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      ChatActivityEnterView.this.redDotPaint.setAlpha((int)(255.0F * this.alpha));
      long l = System.currentTimeMillis() - this.lastUpdateTime;
      if (!this.isIncr)
      {
        this.alpha -= (float)l / 400.0F;
        if (this.alpha <= 0.0F)
        {
          this.alpha = 0.0F;
          this.isIncr = true;
        }
      }
      for (;;)
      {
        this.lastUpdateTime = System.currentTimeMillis();
        paramCanvas.drawCircle(AndroidUtilities.dp(5.0F), AndroidUtilities.dp(5.0F), AndroidUtilities.dp(5.0F), ChatActivityEnterView.this.redDotPaint);
        invalidate();
        return;
        this.alpha += (float)l / 400.0F;
        if (this.alpha >= 1.0F)
        {
          this.alpha = 1.0F;
          this.isIncr = false;
        }
      }
    }
    
    public void resetAlpha()
    {
      this.alpha = 1.0F;
      this.lastUpdateTime = System.currentTimeMillis();
      this.isIncr = false;
      invalidate();
    }
  }
  
  private class ScrimDrawable
    extends Drawable
  {
    private Paint paint = new Paint();
    
    public ScrimDrawable()
    {
      this.paint.setColor(0);
    }
    
    public void draw(Canvas paramCanvas)
    {
      this.paint.setAlpha(Math.round(102.0F * ChatActivityEnterView.this.stickersExpansionProgress));
      paramCanvas.drawRect(0.0F, 0.0F, ChatActivityEnterView.this.getWidth(), ChatActivityEnterView.this.emojiView.getY() - ChatActivityEnterView.this.getHeight() + Theme.chat_composeShadowDrawable.getIntrinsicHeight(), this.paint);
    }
    
    public int getOpacity()
    {
      return -2;
    }
    
    public void setAlpha(int paramInt) {}
    
    public void setColorFilter(ColorFilter paramColorFilter) {}
  }
  
  private class SeekBarWaveformView
    extends View
  {
    public SeekBarWaveformView(Context paramContext)
    {
      super();
      ChatActivityEnterView.access$002(ChatActivityEnterView.this, new SeekBarWaveform(paramContext));
      ChatActivityEnterView.this.seekBarWaveform.setDelegate(new SeekBar.SeekBarDelegate()
      {
        public void onSeekBarDrag(float paramAnonymousFloat)
        {
          if (ChatActivityEnterView.this.audioToSendMessageObject != null)
          {
            ChatActivityEnterView.this.audioToSendMessageObject.audioProgress = paramAnonymousFloat;
            MediaController.getInstance().seekToProgress(ChatActivityEnterView.this.audioToSendMessageObject, paramAnonymousFloat);
          }
        }
      });
    }
    
    public boolean isDragging()
    {
      return ChatActivityEnterView.this.seekBarWaveform.isDragging();
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      super.onDraw(paramCanvas);
      ChatActivityEnterView.this.seekBarWaveform.setColors(Theme.getColor("chat_recordedVoiceProgress"), Theme.getColor("chat_recordedVoiceProgressInner"), Theme.getColor("chat_recordedVoiceProgress"));
      ChatActivityEnterView.this.seekBarWaveform.draw(paramCanvas);
    }
    
    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
      ChatActivityEnterView.this.seekBarWaveform.setSize(paramInt3 - paramInt1, paramInt4 - paramInt2);
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent)
    {
      boolean bool = ChatActivityEnterView.this.seekBarWaveform.onTouch(paramMotionEvent.getAction(), paramMotionEvent.getX(), paramMotionEvent.getY());
      if (bool)
      {
        if (paramMotionEvent.getAction() == 0) {
          ChatActivityEnterView.this.requestDisallowInterceptTouchEvent(true);
        }
        invalidate();
      }
      return (bool) || (super.onTouchEvent(paramMotionEvent));
    }
    
    public void setProgress(float paramFloat)
    {
      ChatActivityEnterView.this.seekBarWaveform.setProgress(paramFloat);
      invalidate();
    }
    
    public void setWaveform(byte[] paramArrayOfByte)
    {
      ChatActivityEnterView.this.seekBarWaveform.setWaveform(paramArrayOfByte);
      invalidate();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/ChatActivityEnterView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */